package com.micsig.tbook.scope.Action;  // 定义包名：示波器动作处理模块

import android.os.SystemClock;  // 导入SystemClock类：获取系统启动以来的时间（不含休眠时间）
import android.util.Log;  // 导入Log类：Android日志输出工具

import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入HwConfig类：硬件配置管理，提供FPGA数量等配置
import com.micsig.tbook.scope.Data.SyncHeader;  // 导入SyncHeader类：同步头管理，标识采集周期
import com.micsig.tbook.scope.Sample.Sample;  // 导入Sample类：采样状态管理（运行/停止/瞬态等）
import com.micsig.tbook.scope.Sample.SegmentSample;  // 导入SegmentSample类：段采样管理，记录段采样时间
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理，提供运行状态判断
import com.micsig.tbook.scope.ScopeFrozen;  // 导入ScopeFrozen类：冻结状态管理，保存/恢复停止状态
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂，提供通道索引常量
import com.micsig.tbook.scope.fpga.FPGACommand;  // 导入FPGACommand类：FPGA命令管理器，所有硬件命令的实际执行者
import com.micsig.tbook.scope.fpga.FPGA_Status;  // 导入FPGA_Status类：FPGA状态数据结构
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入HorizontalAxis类：水平轴管理，修正时间位置
import com.micsig.tbook.scope.probe.ProbeCommand;  // 导入ProbeCommand类：探头命令数据模型
import com.micsig.tbook.scope.probe.ProbeFactory;  // 导入ProbeFactory类：探头工厂，管理探头通信

import java.nio.ByteBuffer;  // 导入ByteBuffer类：字节缓冲区（用于探头数据解析）
import java.util.List;  // 导入List接口：用于探头超时检测的列表

/**
 * FPGA消息处理与命令调度中心
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Action（示波器动作处理模块）</li>
 *   <li>架构层级：业务逻辑层 - FPGA命令调度层</li>
 *   <li>设计模式：命令模式 + 位掩码合并</li>
 *   <li>职责类型：FPGA命令定义、命令队列管理与调度执行</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义和管理FPGA命令常量（采样、显示、触发、通道等）</li>
 *   <li>维护FPGA命令队列（FPGA_CMD[0]/[1]），支持命令合并与批量执行</li>
 *   <li>执行FPGA命令调度（运行、停止、暂停、单次触发）</li>
 *   <li>处理显示相关的FPGA命令</li>
 *   <li>管理探头通信与状态检测</li>
 *   <li>读取FPGA状态信息（温度、风扇、状态等）</li>
 * </ul>
 * 
 * <p><b>FPGA命令寄存器布局（2个32位寄存器）：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   FPGA_CMD[0] ─ 主命令寄存器 (32位)                                      │
 * │                                                                          │
 * │   bit[0-15]  ─ 采样相关命令 (FPGA_CMD_MASK_SAMP)                         │
 * │   ├── bit[0]:   采样模式 (SAMP_MODE)                                     │
 * │   ├── bit[1]:   平均/包络次数 (PJBL_NUM)                                  │
 * │   ├── bit[2]:   存储深度 (SAMP_ZUN_DEPTH)                                 │
 * │   ├── bit[3]:   采样位置 (SAMP_PLACE)                                    │
 * │   ├── bit[4]:   抽样 (COUY)                                              │
 * │   ├── bit[5]:   串行码字深度 (SERIAL_CODE_DEPTH)                         │
 * │   ├── bit[6]:   Y轴偏移 (Y_PLACE)                                        │
 * │   ├── bit[7]:   触发参数 (TRIG)                                          │
 * │   ├── bit[8]:   触发抑制时间 (TRIG_RETRAIN)                              │
 * │   ├── bit[9]:   自动触发时间 (AUTO_TRIG_T)                               │
 * │   ├── bit[10]:  触发偏移 (TRIG_OFFSET)                                   │
 * │   ├── bit[11]:  通道偏移 (CH_OFFSET)                                     │
 * │   ├── bit[12]:  触发电平 (TRIG_LEVEL)                                    │
 * │   ├── bit[13]:  触发耦合 (TRIG_COUPLE)                                   │
 * │   ├── bit[14]:  AD通道切换 (AD_CH_CHANGE)                                │
 * │   └── bit[15]:  触发回差 (TRIG_HUICHA)                                   │
 * │                                                                          │
 * │   bit[16-27] ─ 显示相关命令 (FPGA_CMD_MASK_DIS)                          │
 * │   ├── bit[16]:  显示模式 (DIS_MODE) ─ 波形亮度、放大镜、点线模式等       │
 * │   ├── bit[17]:  显示参数 (DIS) ─ 插值、波形取数位置、静态波形抽样        │
 * │   ├── bit[18]:  获取数据 (GET_DATA)                                      │
 * │   └── bit[19-26]: 通道8-1显示 (DIS_PIX_ch1~ch8)                          │
 * │                                                                          │
 * │   bit[28-31] ─ 特殊操作 (FPGA_CMD_MASK_T)                                │
 * │   ├── bit[28]:  运行/停止 (RUN_STOP)                                     │
 * │   ├── bit[29]:  单次触发 (SINGLE)                                        │
 * │   ├── bit[30]:  外设操作 (DEVICE) ─ 已废弃                               │
 * │   └── bit[31]:  段更新 (SEGMENT) ─ 使FPGA重新发送数据                    │
 * └──────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   FPGA_CMD[1] ─ 扩展命令寄存器 (32位)                                    │
 * │                                                                          │
 * │   bit[0-15]  ─ 扩展采样命令 (FPGA_CMD_EXT_MASK_SAMP)                     │
 * │   ├── bit[0]:   插值系数 (CHAZHI_COEF)                                   │
 * │   ├── bit[1]:   AD滤波系数 (AD_COEF)                                     │
 * │   ├── bit[2-9]: 通道1-8电压档位变化 (vScale_CHANGE_CH1~CH8)              │
 * │   ├── bit[10]:  AD零点 (AD_ZERO)                                         │
 * │   ├── bit[11]:  阻抗 (RESISTANCE) ─ 1MΩ/50Ω切换                          │
 * │   ├── bit[12]:  探头DA (PROBE_DA)                                        │
 * │   └── bit[13]:  外部触发电平 (TRIGGER_LEVEL)                              │
 * │                                                                          │
 * │   bit[26-31] ─ 总线命令 (FPGA_CMD_EXT_MASK_T)                            │
 * │   ├── bit[26-29]: 总线S1-S4 (BUS_S1~S4)                                  │
 * │   ├── bit[30]:    总线类型 (BUS_TYPE) ─ CAN/LIN/ARINC429等               │
 * │   └── bit[31]:    总线电平 (BUS_LEVEL)                                   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>命令合并策略：</b>
 * <ul>
 *   <li>使用位或操作（|=）将多个命令合并到命令寄存器中</li>
 *   <li>延迟执行策略：先收集命令，延迟20ms后批量执行</li>
 *   <li>避免频繁的FPGA同步操作，提升系统性能</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：FPGACommand（FPGA命令管理器，负责底层硬件交互）</li>
 *   <li>依赖：Scope（示波器状态管理，提供运行/停止判断）</li>
 *   <li>依赖：Sample（采样状态管理，提供采样模式）</li>
 *   <li>依赖：ScopeFrozen（冻结状态管理）</li>
 *   <li>依赖：ProbeFactory（探头工厂，管理探头通信）</li>
 *   <li>依赖：HorizontalAxis（水平轴控制）</li>
 *   <li>被依赖：ChannelAction（通道动作处理）</li>
 *   <li>被依赖：ScopeMessage（消息处理中心）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018
 * @see FPGACommand FPGA命令管理器
 * @see Scope 示波器状态管理
 * @see Sample 采样状态管理
 * @see ProbeFactory 探头工厂
 */
public class FPGAMessage {
    
    private static final String TAG = "FPGAMessage";  // 日志标签：用于Log输出时标识来源
    
    // ==================== 采样相关命令常量 (FPGA_CMD[0] bit 0-15) ====================
    
    /** 采样命令掩码（bit 0-15），用于判断是否有采样相关命令 */
    public static final int FPGA_CMD_MASK_SAMP = 0xFFFF;  // 0x0000FFFF：低16位全1，掩码出采样命令

    /** 采样模式命令：自动/YT/滚屏/慢时基/相位/单双通道/耦合方式等 */
    public static final int FPGA_CMD_SAMP_MODE = 1 << 0;  // bit[0] = 0x00000001

    /** 平均/包络次数修改命令 */
    public static final int FPGA_CMD_PJBL_NUM = 1 << 1;  // bit[1] = 0x00000002

    /** 存储深度修改命令 */
    public static final int FPGA_CMD_SAMP_ZUN_DEPTH = 1 << 2;  // bit[2] = 0x00000004

    /** 采样位置命令：预采样时间、后采样时间 */
    public static final int FPGA_CMD_SAMP_PLACE = 1 << 3;  // bit[3] = 0x00000008

    /** AD抽样命令：对AD实际采样数据进行抽取，有10ms同步等待 */
    public static final int FPGA_CMD_COUY = 1 << 4;  // bit[4] = 0x00000010

    /** 串口码字采样深度命令 */
    public static final int FPGA_CMD_SERIAL_CODE_DEPTH = 1 << 5;  // bit[5] = 0x00000020

    /** Y轴波形偏移量命令 */
    public static final int FPGA_CMD_Y_PLACE = 1 << 6;  // bit[6] = 0x00000040

    /** 触发参数命令：触发模式、触发源、触发斜率等 */
    public static final int FPGA_CMD_TRIG = 1 << 7;  // bit[7] = 0x00000080

    /** 触发抑制时间命令 */
    public static final int FPGA_CMD_TRIG_RETRAIN = 1 << 8;  // bit[8] = 0x00000100

    /** 自动触发时间设置命令 */
    public static final int FPGA_CMD_AUTO_TRIG_T = 1 << 9;  // bit[9] = 0x00000200

    /** 精确触发参数命令(tBook偏移) */
    public static final int FPGA_CMD_TRIG_OFFSET = 1 << 10;  // bit[10] = 0x00000400

    /** mini通道偏移命令 */
    public static final int FPGA_CMD_CH_OFFSET = 1 << 11;  // bit[11] = 0x00000800

    /** mini触发电平修改命令 */
    public static final int FPGA_CMD_TRIG_LEVEL = 1 << 12;  // bit[12] = 0x00001000

    /** mini触发耦合修改命令 */
    public static final int FPGA_CMD_TRIG_COUPLE = 1 << 13;  // bit[13] = 0x00002000

    /** AD通道切换命令：单双四通道切换时给AD发命令 */
    public static final int FPGA_CMD_AD_CH_CHANGE = 1 << 14;  // bit[14] = 0x00004000

    /** 触发回差修改命令 */
    public static final int FPGA_CMD_TRIG_HUICHA = 1 << 15;  // bit[15] = 0x00008000

    // ==================== 显示相关命令常量 (FPGA_CMD[0] bit 16-27) ====================
    
    /** 显示命令掩码（bit 16-27），用于判断是否有显示相关命令 */
    public static final int FPGA_CMD_MASK_DIS = 0x0FFF0000;  // bit[16-27]全1

    /** 显示模式命令：波形亮度、放大镜、点线模式、点阵、图层变化 */
    public static final int FPGA_CMD_DIS_MODE = 1 << 16;  // bit[16] = 0x00010000

    /** 显示参数命令：插值、显示波形取数位置、静态波形抽样 */
    public static final int FPGA_CMD_DIS = 1 << 17;  // bit[17] = 0x00020000

    /** 获取数据命令 */
    public static final int FPGA_CMD_GET_DATA = 1 << 18;  // bit[18] = 0x00040000

    /** 通道1显示命令：通道1上下移动或Y轴缩放 */
    public static final int FPGA_CMD_DIS_PIX_ch1 = 1 << 19;  // bit[19] = 0x00080000

    /** 通道2显示命令：通道2上下移动或Y轴缩放 */
    public static final int FPGA_CMD_DIS_PIX_ch2 = 1 << 20;  // bit[20] = 0x00100000

    /** 通道3显示命令：通道3上下移动或Y轴缩放 */
    public static final int FPGA_CMD_DIS_PIX_ch3 = 1 << 21;  // bit[21] = 0x00200000

    /** 通道4显示命令：通道4上下移动或Y轴缩放 */
    public static final int FPGA_CMD_DIS_PIX_ch4 = 1 << 22;  // bit[22] = 0x00400000

    /** 通道5显示命令：通道5上下移动或Y轴缩放 */
    public static final int FPGA_CMD_DIS_PIX_ch5 = 1 << 23;  // bit[23] = 0x00800000

    /** 通道6显示命令：通道6上下移动或Y轴缩放 */
    public static final int FPGA_CMD_DIS_PIX_ch6 = 1 << 24;  // bit[24] = 0x01000000

    /** 通道7显示命令：通道7上下移动或Y轴缩放 */
    public static final int FPGA_CMD_DIS_PIX_ch7 = 1 << 25;  // bit[25] = 0x02000000

    /** 通道8显示命令：通道8上下移动或Y轴缩放 */
    public static final int FPGA_CMD_DIS_PIX_ch8 = 1 << 26;  // bit[26] = 0x04000000

    /** 所有通道显示命令掩码：bit[19-26]全1 */
    public static final int FPGA_CMD_DIS_PIX = 0xFF << 19;  // 0x07F80000：8个通道的显示bit

    // ==================== 特殊操作命令常量 (FPGA_CMD[0] bit 28-31) ====================
    
    /** 特殊操作掩码（bit 28-31），用于判断是否有特殊操作命令 */
    public static final int FPGA_CMD_MASK_T = 0xF0000000;  // bit[28-31]全1

    /** 运行/停止命令 */
    public static final int FPGA_CMD_RUN_STOP = 1 << 28;  // bit[28] = 0x10000000

    /** 单次触发命令 */
    public static final int FPGA_CMD_SINGLE = 1 << 29;  // bit[29] = 0x20000000

    /** 外设操作命令（已废弃） */
    public static final int FPGA_CMD_DEVICE = 1 << 30;  // bit[30] = 0x40000000

    /** 段更新命令：使FPGA重新发送数据 */
    public static final int FPGA_CMD_SEGMENT = 1 << 31;  // bit[31] = 0x80000000，最高位

    // ==================== 扩展采样命令常量 (FPGA_CMD[1] bit 0-15) ====================
    
    /** 扩展采样命令掩码（bit 0-15） */
    public static final int FPGA_CMD_EXT_MASK_SAMP = 0xFFFF;  // 0x0000FFFF：低16位全1

    /** 重载插值系数命令：单双四通道变化时重载 */
    public static final int FPGA_CMD_EXT_CHAZHI_COEF = 1 << 0;  // bit[0] = 0x00000001

    /** 重载AD前置滤波系数命令 */
    public static final int FPGA_CMD_EXT_AD_COEF = 1 << 1;  // bit[1] = 0x00000002

    /** 通道1电压档位变化命令 */
    public static final int FPGA_CMD_EXT_vScale_CHANGE_CH1 = 1 << 2;  // bit[2] = 0x00000004

    /** 通道2电压档位变化命令 */
    public static final int FPGA_CMD_EXT_vScale_CHANGE_CH2 = 1 << 3;  // bit[3] = 0x00000008

    /** 通道3电压档位变化命令 */
    public static final int FPGA_CMD_EXT_vScale_CHANGE_CH3 = 1 << 4;  // bit[4] = 0x00000010

    /** 通道4电压档位变化命令 */
    public static final int FPGA_CMD_EXT_vScale_CHANGE_CH4 = 1 << 5;  // bit[5] = 0x00000020

    /** 通道5电压档位变化命令 */
    public static final int FPGA_CMD_EXT_vScale_CHANGE_CH5 = 1 << 6;  // bit[6] = 0x00000040

    /** 通道6电压档位变化命令 */
    public static final int FPGA_CMD_EXT_vScale_CHANGE_CH6 = 1 << 7;  // bit[7] = 0x00000080

    /** 通道7电压档位变化命令 */
    public static final int FPGA_CMD_EXT_vScale_CHANGE_CH7 = 1 << 8;  // bit[8] = 0x00000100

    /** 通道8电压档位变化命令 */
    public static final int FPGA_CMD_EXT_vScale_CHANGE_CH8 = 1 << 9;  // bit[9] = 0x00000200

    /** 所有通道电压档位变化掩码：bit[2-9]全1 */
    public static final int FPGA_CMD_EXT_vScale_CHANGE = 0xFF << 2;  // 0x000003FC：8个通道的位

    /** AD零点校准命令 */
    public static final int FPGA_CMD_EXT_AD_ZERO = 1 << 10;  // bit[10] = 0x00000400

    /** 阻抗设置命令：1MΩ/50Ω切换 */
    public static final int FPGA_CMD_EXT_RESISTANCE = 1 << 11;  // bit[11] = 0x00000800

    /** 探头DA设置命令 */
    public static final int FPGA_CMD_EXT_PROBE_DA = (1 << 12);  // bit[12] = 0x00001000

    /** 外部触发电平设置命令 */
    public static final int FPGA_CMD_EXT_TRIGGER_LEVEL = (1 << 13);  // bit[13] = 0x00002000

    // ==================== 扩展总线命令常量 (FPGA_CMD[1] bit 26-31) ====================
    
    /** 扩展特殊操作掩码（bit 26-31） */
    public static final int FPGA_CMD_EXT_MASK_T = 0xFC000000;  // bit[26-31]全1

    /** 总线S1配置命令 */
    public static final int FPGA_CMD_BUS_S1 = 1 << 26;  // bit[26] = 0x04000000

    /** 总线S2配置命令 */
    public static final int FPGA_CMD_BUS_S2 = 1 << 27;  // bit[27] = 0x08000000

    /** 总线S3配置命令 */
    public static final int FPGA_CMD_BUS_S3 = 1 << 28;  // bit[28] = 0x10000000

    /** 总线S4配置命令 */
    public static final int FPGA_CMD_BUS_S4 = 1 << 29;  // bit[29] = 0x20000000

    /** 总线类型切换命令 */
    public static final int FPGA_CMD_BUS_TYPE = 1 << 30;  // bit[30] = 0x40000000

    /** 总线电平设置命令 */
    public static final int FPGA_CMD_BUS_LEVEL = 1 << 31;  // bit[31] = 0x80000000，最高位

    // ==================== 成员变量 ====================
    
    /** 
     * FPGA命令寄存器数组
     * [0] = 主命令寄存器（采样+显示+特殊操作）
     * [1] = 扩展命令寄存器（扩展采样+总线命令）
     */
    private int[] FPGA_CMD = new int[2];  // 两个32位命令寄存器，初始值0

    /** 命令计数器：记录添加的命令数量，用于判断队列是否变化 */
    private int mNum = 0;  // 每次add()调用时递增1

    /** FPGA数量：1或2，从HwConfig获取 */
    private int fpgaNums = 1;  // 默认为1个FPGA

    /** FPGA命令管理器引用：所有硬件操作的最终执行者 */
    private FPGACommand fpgaCommand;  // 通过FPGACommand.getInstance()获取单例

    /** 备份命令计数：用于display()方法判断命令是否变化 */
    private int bakNum = 0;  // 与mNum比较，不相等时触发显示更新

    /** 显示时间戳：记录上次显示命令执行时间 */
    public long displayTimestamp = 0;  // 使用SystemClock.elapsedRealtime()

    /** 瞬态显示时间戳：记录瞬态显示的时间点 */
    public long transientDisplayTimestamp = 0;  // 用于onTransientDisplay()中的时间判断

    /** 单次命令标志：标记是否有单次触发命令待执行 */
    boolean bCmdSingle = false;  // fpgaSync()中根据此标志发送cmdFpgaSingle

    /** 运行命令标志：标记是否有运行命令待执行 */
    boolean bCmdRun = false;  // fpgaSync()中根据此标志发送cmdFpgaRun

    // ==================== 构造函数与初始化 ====================
    
    /**
     * 构造函数：初始化FPGA消息处理器
     * 
     * <p>初始化流程：
     * <ol>
     *   <li>清空命令寄存器</li>
     *   <li>获取FPGACommand单例</li>
     *   <li>读取FPGA数量配置</li>
     * </ol>
     */
    public FPGAMessage() {
        FPGA_CMD[0] = 0;  // 初始化主命令寄存器为0
        FPGA_CMD[1] = 0;  // 初始化扩展命令寄存器为0
        mNum = 0;  // 初始化命令计数器为0
        
        fpgaCommand = FPGACommand.getInstance();  // 获取FPGACommand单例实例
        
        fpgaNums = HwConfig.getInstance().getFpgaNums();  // 从硬件配置读取FPGA数量
    }

    // ==================== 命令管理方法 ====================
    
    /**
     * 检查是否有待处理的命令
     * 
     * @return true表示命令寄存器非空（有待处理命令），false表示命令寄存器全为0
     */
    public boolean isValid() {
        return isMask(FPGA_CMD[0]) || isMask(FPGA_CMD[1]);  // 主命令或扩展命令非零即有效
    }

    /**
     * 获取当前命令计数
     * 
     * @return 当前累积的命令数量
     */
    public int getCmdNum() {
        return mNum;  // 返回命令计数器
    }

    /**
     * 清除命令寄存器
     * 
     * <p>当命令计数匹配时执行清除操作，确保不会误清除新加入的命令。
     * 同时清除波形显示。
     * 
     * @param n 期望的命令计数，只有与当前mNum匹配时才清除
     */
    private void clr_Cmd(int n) {
        Log.d(TAG, "clr_Cmd() ==>"  // 输出调试日志
                + " FPGA_CMD[0]=0x" + Integer.toHexString(FPGA_CMD[0])  // 输出主命令十六进制
                + ",FPGA_CMD[1]=0x" + Integer.toHexString(FPGA_CMD[1]));  // 输出扩展命令十六进制
        
        if (n == getCmdNum()) {  // 命令计数匹配时（无新命令加入）
            FPGA_CMD[0] = 0;  // 清空主命令寄存器
            FPGA_CMD[1] = 0;  // 清空扩展命令寄存器
            bakNum = mNum = 0;  // 重置备份计数和命令计数
        }
        
        clearWave();  // 清除波形显示
    }

    /**
     * 添加命令到命令寄存器
     * 
     * <p>使用位或操作（|=）合并命令位，支持多个命令在一帧内批量发送。
     * 
     * @param arg1 主命令掩码（对应FPGA_CMD[0]的位）
     * @param arg2 扩展命令掩码（对应FPGA_CMD[1]的位）
     */
    public void add(int arg1, int arg2) {
        FPGA_CMD[0] |= arg1;  // 将arg1的位合并到主命令寄存器
        FPGA_CMD[1] |= arg2;  // 将arg2的位合并到扩展命令寄存器
        mNum++;  // 命令计数器递增
    }

    /**
     * 清除波形显示
     * 
     * <p>根据采样状态选择清除方式：
     * <ul>
     *   <li>SAMPLE_RUN：清除实时波形数据</li>
     *   <li>其他状态：清除持久化显示</li>
     * </ul>
     */
    private void clearWave() {
        Sample sample = Sample.getInstance();  // 获取采样管理单例
        int s = sample.getSampleState();  // 获取当前采样状态

        Log.d(TAG, "clearWave() called :" + s);  // 输出采样状态日志
        
        if (s == Sample.SAMPLE_RUN) {  // 运行状态
            Scope.getInstance().clearWave();  // 清除实时波形数据
        } else if (s > Sample.SAMPLE_RUN) {  // 停止/暂停等非运行状态
            Scope.getInstance().clearPersist();  // 清除持久化显示
        }
    }

    /**
     * 检查整数值是否为非零（即是否有有效掩码）
     * 
     * @param val 待检查的值
     * @return true表示非零（有命令位设置），false表示为零（无命令）
     */
    private boolean isMask(int val) {
        return val != 0;  // 非零表示有命令需要处理
    }

    // ==================== FPGA控制方法 ====================
    
    /**
     * 执行运行命令：发送所有采样和显示参数到FPGA
     * 
     * <p>用于示波器从停止状态切换到运行状态时的完整初始化。
     * 依次发送采样参数、触发参数、显示参数、扩展参数到所有FPGA。
     */
    private void cmdRun() {
        Log.i(TAG, "cmdRun() ==>" + " cmdRun start");  // 输出运行开始日志
        
        HorizontalAxis.getInstance().correctTimePose();  // 修正时间位置（触发位置校准）
        
        int beginIdx, endIdx;  // 通道起始和结束索引
        
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            beginIdx = FPGACommand.beginChIdx(i);  // 获取当前FPGA管理的起始通道索引
            endIdx = FPGACommand.endChIdx(i);  // 获取当前FPGA管理的结束通道索引
            
            // --- 采样参数 ---
            fpgaCommand.gntR_sampMode(i);  // 发送采样模式
            fpgaCommand.gntR_pinJ_baoL(i);  // 发送平均/包络次数
            fpgaCommand.gntR_zun_depth(i);  // 发送存储深度
            fpgaCommand.cmdSegment(i);  // 发送段命令
            fpgaCommand.update_v_Zoom(i);  // 发送垂直缩放
            
            fpgaCommand.gntR_samp_place(i);  // 发送采样位置（预采样时间、后采样时间）
            fpgaCommand.gntR_CouY(i);  // 发送抽样参数
            fpgaCommand.gntR_serial_dec_depth(i);  // 发送串行码字存储深度
            fpgaCommand.gntR_ch_y_place(i);  // 发送通道指示器Y轴位置
            
            // --- 触发参数 ---
            fpgaCommand.gntR_trigMode(i);  // 发送触发模式
            fpgaCommand.gntR_trigLevel(i);  // 发送触发电平
            fpgaCommand.gntR_trigCouple(i);  // 发送触发耦合
            fpgaCommand.gntR_trigRestrainTime(i);  // 发送触发抑制时间
            fpgaCommand.gntR_trigAutoTime(i);  // 发送自动触发时间
            
            fpgaCommand.gntR_ChOffsetDa(i);  // 发送通道偏移DAC
            fpgaCommand.SendAdc_ch_change(i);  // 发送ADC通道切换
            
            // --- 显示参数 ---
            fpgaCommand.gntR_disMode(i);  // 发送显示模式
            fpgaCommand.gntR_Dis(i);  // 发送显示参数（插值、取数位置等）
            fpgaCommand.gntR_yuHui(i);  // 发送余晖参数
            
            for (int j = beginIdx; j < endIdx; j++) {  // 遍历当前FPGA管理的通道
                fpgaCommand.cmdFpgaDotMatrix(j);  // 发送通道点阵命令
            }
            
            // --- 扩展参数 ---
            fpgaCommand.cmdFpgaReloadChazhiCoef(i);  // 重载插值系数
            fpgaCommand.cmdReload_AD_coef(i);  // 重载AD滤波系数
            
            for (int j = beginIdx; j < endIdx; j++) {  // 遍历当前FPGA管理的通道
                fpgaCommand.cmdReload_AD_coef_VOL_dangChange(j);  // 发送通道电压档位变化
            }
            
            fpgaCommand.cmdChNoise(i);  // 发送通道噪声参数
            fpgaCommand.gntR_Bus(i);  // 发送总线配置
            fpgaCommand.ChangeProbeDa(i);  // 发送探头DA设置
            fpgaCommand.ChangeResistance(i);  // 发送阻抗设置
            
            fpgaCommand.updataReg(i);  // 更新FPGA寄存器，使命令生效
        }
        
        Log.i(TAG, "cmdRun() ==>" + " cmdRun end");  // 输出运行结束日志
    }

    /**
     * 恢复FPGA运行
     * 
     * <p>恢复FPGA通信后发送完整的运行命令。
     * 用于Scope.resume场景。
     */
    public void resume() {
        fpgaCommand.resume();  // 恢复FPGA底层通信
        
        cmdRun();  // 发送完整运行命令（所有采样+显示参数）
        
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            fpgaCommand.cmdFpgaRun(i);  // 发送FPGA运行命令
        }
        
        fpgaCommand.adSync();  // AD同步：确保各ADC采样同步
    }

    /**
     * 停止FPGA
     * 
     * <p>发送停止命令到所有FPGA（先停止FPGA1，再停止FPGA0）。
     */
    public void stop() {
        fpgaCommand.cmdFpgaStop(1);  // 停止FPGA 1
        fpgaCommand.cmdFpgaStop(0);  // 停止FPGA 0
    }

    /**
     * 暂停FPGA
     * 
     * <p>发送暂停命令到所有FPGA。
     */
    public void pause() {
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            fpgaCommand.cmdFpgaPause(i);  // 发送暂停命令到当前FPGA
        }
    }

    /**
     * 初始化FPGA命令通信
     * 
     * <p>恢复FPGA命令发送并执行初始化命令。
     */
    public void cmdInit() {
        fpgaCommand.resume();  // 恢复FPGA底层通信
        fpgaCommand.cmdInit();  // 执行FPGA初始化命令
    }

    /**
     * 触摸暂停
     * 
     * <p>根据示波器运行状态选择暂停或停止：
     * <ul>
     *   <li>运行状态：暂停FPGA</li>
     *   <li>停止状态：停止FPGA</li>
     * </ul>
     */
    public void touchPause() {
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            if (Scope.getInstance().isRun()) {  // 示波器正在运行
                fpgaCommand.cmdFpgaPause(i);  // 发送暂停命令
            } else {  // 示波器已停止
                fpgaCommand.cmdFpgaStop(i);  // 发送停止命令
            }
        }
    }

    /**
     * 触摸恢复：从触摸模式恢复显示
     */
    public void touchResume() {
        Log.d(TAG, "touchResume() called");  // 输出调试日志（当前为空操作）
    }

    /**
     * 执行ADC校准
     */
    public void ADCalibrate() {
        fpgaCommand.ADCalibrate();  // 委托给FPGACommand执行ADC校准
    }

    // ==================== 显示命令处理 ====================
    
    /**
     * 执行显示命令：调用带参数的displayCmd(false)
     */
    private void displayCmd() {
        displayCmd(false);  // false表示非停止状态（暂停状态）
    }

    /**
     * 执行显示命令处理
     * 
     * <p>在停止或暂停状态下，执行显示寄存器的更新。
     * 包括冻结状态处理和显示参数更新。
     * 
     * @param bStop true表示停止状态，false表示暂停状态
     */
    private void displayCmd(boolean bStop) {
        int beginIdx, endIdx;  // 通道起始和结束索引
        ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();  // 获取冻结状态管理单例
        
        int[] val = bStop ? fpgaCommand.cmdFpgaStop(0) : fpgaCommand.cmdFpgaPause(0);  // 执行停止/暂停并获取返回值[syncHeader, segmentInfo]

        Log.d(TAG, "fpga SyncHeader:" + val[0] + ",syncheader:" + SyncHeader.getSyncHeader() + "," + val[1]);  // 输出同步头日志
        
        ScopeFrozen.ScopeFrozenBean bean = scopeFrozen.getScopeBean(val[0]);  // 获取/创建冻结Bean
        scopeFrozen.doFrozenEx(bean, val);  // 执行冻结状态处理
        Log.d(TAG, "bean valid:" + bean.isValid());  // 输出Bean有效性日志

        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            beginIdx = FPGACommand.beginChIdx(i);  // 获取当前FPGA管理的起始通道索引
            endIdx = FPGACommand.endChIdx(i);  // 获取当前FPGA管理的结束通道索引

            fpgaCommand.update_v_Zoom(i);  // 更新垂直缩放参数
            fpgaCommand.gntR_disMode(i);  // 更新显示模式参数
            fpgaCommand.gntR_Dis(i);  // 更新显示参数（插值等）

            for (int j = beginIdx; j < endIdx; j++) {  // 遍历当前FPGA管理的通道
                fpgaCommand.cmdFpgaDotMatrix(j);  // 更新通道点阵
            }
            
            fpgaCommand.cmdFpgaReloadChazhiCoef(i);  // 重载插值系数
        }
        
        fpgaCommand.updataRegDis(0);  // 更新FPGA 0的显示寄存器
        setTransientDisplayTimestamp(SystemClock.elapsedRealtime());  // 记录瞬态显示时间戳
    }

    /**
     * 检查是否仅触发电平变化
     * 
     * <p>仅触发电平变化不需要完整的显示更新。
     * 
     * @return true表示只有TRIG_LEVEL命令
     */
    private boolean isTriggerLevelChange() {
        return (FPGA_CMD[0] == FPGA_CMD_TRIG_LEVEL && FPGA_CMD[1] == 0);  // 主命令仅TRIG_LEVEL且扩展命令为空
    }

    /**
     * 检查是否是运行/停止命令
     * 
     * @return true表示仅RUN_STOP命令
     */
    private boolean isCmdRunStop() {
        return (FPGA_CMD[0] == FPGA_CMD_RUN_STOP && FPGA_CMD[1] == 0);  // 主命令仅RUN_STOP且扩展命令为空
    }

    /**
     * 执行显示更新
     * 
     * <p>当命令计数变化或超过3秒时执行显示命令，
     * 确保停止状态下的显示参数及时刷新。
     * 
     * @return true表示执行了显示更新，false表示不需要更新
     */
    public boolean display() {
        long ts = SystemClock.elapsedRealtime();  // 获取当前时间戳
        int n = getCmdNum();  // 获取当前命令计数
        
        if (bakNum != n || ts - displayTimestamp > 3000) {  // 命令计数变化 或 距上次显示超过3秒
            displayCmd();  // 执行显示命令
            
            FPGA_CMD[0] |= FPGA_CMD_DIS_MODE | FPGA_CMD_DIS_PIX  // 添加显示模式命令
                    | FPGA_CMD_DIS | FPGA_CMD_SEGMENT | FPGA_CMD_CH_OFFSET;  // 添加显示参数和偏移命令
            FPGA_CMD[1] |= FPGA_CMD_EXT_CHAZHI_COEF;  // 添加插值系数命令
            
            bakNum = n;  // 更新备份计数
            displayTimestamp = ts;  // 更新时间戳
            return true;  // 返回true表示执行了更新
        }
        return false;  // 返回false表示无需更新
    }

    /**
     * 设置瞬态显示时间戳
     * 
     * @param ts 时间戳值
     */
    public void setTransientDisplayTimestamp(long ts) {
        this.transientDisplayTimestamp = ts;  // 记录瞬态显示时间戳
    }

    /**
     * 处理瞬态显示超时
     * 
     * <p>当瞬态显示状态超过100ms时，自动切换到瞬态运行状态。
     */
    public void onTransientDisplay() {
        Sample sample = Sample.getInstance();  // 获取采样管理单例
        
        if (sample.getSampleState() == Sample.SAMPLE_TRANSIENT_DIAPLAY) {  // 当前处于瞬态显示状态
            if ((SystemClock.elapsedRealtime() - transientDisplayTimestamp) > 100) {  // 距离上次瞬态显示超过100ms
                sample.setSampleState(Sample.SAMPLE_TRANSIENT_RUN);  // 切换到瞬态运行状态
            }
        }
    }

    /**
     * 判断是否需要瞬态显示
     * 
     * <p>瞬态显示模式：在运行状态下仅更新显示，不重新采样。
     * 提高显示更新的响应速度。
     * 
     * @return true表示需要瞬态显示，false表示不需要
     */
    public boolean isDisplayTransient() {
        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
        Sample sample = Sample.getInstance();  // 获取采样管理单例
        boolean bDisplay = false;  // 是否需要瞬态显示
        
        if (sample.isRunSample()) {  // 检查是否处于运行采样状态
            onTransientDisplay();  // 处理瞬态显示超时

            switch (sample.getSampleState()) {  // 根据采样状态判断
                case Sample.SAMPLE_TRANSIENT_DIAPLAY:  // 瞬态显示状态
                case Sample.SAMPLE_TRANSIENT:  // 瞬态状态
                    bDisplay = true;  // 需要瞬态显示
                    break;  // 跳出
                case Sample.SAMPLE_TRANSIENT_RUN:  // 瞬态运行状态
                    if (bakNum != mNum) {  // 命令计数变化
                        sample.setSampleState(Sample.SAMPLE_TRANSIENT);  // 切换到瞬态状态
                        bDisplay = true;  // 需要瞬态显示
                    }
                    break;  // 跳出
            }

            if (bDisplay) {  // 需要瞬态显示时检查排除条件
                if (scope.isAuto()  // 自动模式
                        || scope.isInXYMode()  // XY显示模式
                        || sample.isSingle()  // 单次模式
                        || scope.isSerialText()  // 串口文本模式
                        || scope.isEnableChannelZero()  // 零通道使能
                        || isTriggerLevelChange()  // 仅触发电平变化
                        || isCmdRunStop()  // 仅运行/停止命令
                        || isCalibrate()) {  // 校准模式
                    bDisplay = false;  // 排除：不需要瞬态显示
                }
                
                if (!bDisplay) {  // 不需要瞬态显示
                    sample.setSampleState(Sample.SAMPLE_TRANSIENT_RUN);  // 恢复瞬态运行状态
                }
            }
        }
        return bDisplay;  // 返回是否需要瞬态显示
    }

    // ==================== 核心命令执行方法 ====================
    
    /**
     * 执行FPGA命令
     * 
     * <p>核心方法：解析FPGA_CMD[0]和FPGA_CMD[1]中的命令位，
     * 按类别执行相应的FPGA操作。这是FPGA消息处理的调度入口。
     * 
     * <p>执行流程：
     * <ol>
     *   <li>更新同步头</li>
     *   <li>处理AD通道切换</li>
     *   <li>遍历FPGA执行采样命令（bit 0-15）</li>
     *   <li>遍历FPGA执行显示命令（bit 16-27）</li>
     *   <li>遍历FPGA执行扩展命令（bit 0-15, bit 26-31）</li>
     *   <li>处理运行/停止/单次命令</li>
     *   <li>清除命令寄存器并更新采样时间</li>
     * </ol>
     */
    public void run() {
        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
        Sample sample = Sample.getInstance();  // 获取采样管理单例
        ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();  // 获取冻结状态管理单例
        boolean bDisplay = false;  // 是否需要显示更新
        boolean bSampleTime = false;  // 是否需要更新采样时间

        Log.d(TAG, "run() ==>" + "[begin]"  // 输出命令执行开始日志
                + " FPGA_CMD[0]=0x" + Integer.toHexString(FPGA_CMD[0])  // 输出主命令十六进制
                + ",FPGA_CMD[1]=0x" + Integer.toHexString(FPGA_CMD[1]));  // 输出扩展命令十六进制

        int syncheader = -1;  // 同步头，-1表示无效
        if (scope.isRun(true)) {  // 示波器正在运行（立即判断）
            syncheader = SyncHeader.upSyncHeader();  // 更新同步头，获取新值
            scopeFrozen.setSyncHeader(syncheader);  // 同步冻结状态的同步头
        }

        int n = getCmdNum();  // 获取本次命令计数（用于清除时匹配）
        int beginIdx, endIdx;  // 通道起始和结束索引
        
        if (isMask(FPGA_CMD[0] & FPGA_CMD_AD_CH_CHANGE)) {  // 如果有AD通道切换命令
            FPGA_CMD[0] |= FPGA_CMD_DIS_PIX;  // 添加通道显示命令
            fpgaCommand.resume();  // 恢复FPGA通信
        }

        boolean bUpdataRegDis = false;  // 是否需要更新显示寄存器
        bCmdRun = bCmdSingle = false;  // 重置运行/单次命令标志
        
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            beginIdx = FPGACommand.beginChIdx(i);  // 获取当前FPGA管理的起始通道索引
            endIdx = FPGACommand.endChIdx(i);  // 获取当前FPGA管理的结束通道索引
            
            // --- 第1步：处理显示和Y轴偏移命令 ---
            if (isMask(FPGA_CMD[0] & (FPGA_CMD_DIS | FPGA_CMD_DIS_PIX | FPGA_CMD_Y_PLACE))  // 有显示、通道显示或Y偏移命令
                    || isMask(FPGA_CMD[1] & FPGA_CMD_EXT_RESISTANCE)) {  // 或有阻抗变化命令
                FPGA_CMD[0] |= fpgaCommand.update_v_Zoom(i) << 19;  // 更新垂直缩放并合并到显示通道位
            }

            // --- 第2步：处理采样相关命令 (FPGA_CMD[0] bit 0-15) ---
            if (isMask(FPGA_CMD[0] & FPGA_CMD_MASK_SAMP)) {  // 有采样相关命令
                if (isMask(FPGA_CMD[0] & FPGA_CMD_SAMP_MODE)) {  // 采样模式
                    fpgaCommand.gntR_sampMode(i);  // 发送采样模式
                }
                if (isMask(FPGA_CMD[0] & FPGA_CMD_PJBL_NUM)) {  // 平均/包络次数
                    fpgaCommand.gntR_pinJ_baoL(i);  // 发送平均/包络参数
                }
                if (isMask(FPGA_CMD[0] & FPGA_CMD_SAMP_ZUN_DEPTH)) {  // 存储深度
                    fpgaCommand.gntR_zun_depth(i);  // 发送存储深度
                }
                if (isMask(FPGA_CMD[0] & FPGA_CMD_SAMP_PLACE)) {  // 采样位置
                    fpgaCommand.gntR_samp_place(i);  // 发送采样位置
                }
                if (isMask(FPGA_CMD[0] & FPGA_CMD_COUY)) {  // 抽样
                    fpgaCommand.gntR_CouY(i);  // 发送抽样参数
                }
                if (isMask(FPGA_CMD[0] & FPGA_CMD_SERIAL_CODE_DEPTH))  // 串行码字深度
                    fpgaCommand.gntR_serial_dec_depth(i);  // 发送串行码字深度
                if (isMask(FPGA_CMD[0] & FPGA_CMD_Y_PLACE)) {  // Y偏移
                    fpgaCommand.gntR_ch_y_place(i);  // 发送Y偏移
                }
                if (isMask(FPGA_CMD[0] & FPGA_CMD_TRIG)) {  // 触发参数
                    fpgaCommand.gntR_trigMode(i);  // 发送触发参数
                }
                if (isMask(FPGA_CMD[0] & (FPGA_CMD_TRIG_HUICHA | FPGA_CMD_TRIG_LEVEL))) {  // 触发回差或触发电平
                    fpgaCommand.gntR_trigLevel(i);  // 发送触发电平（含回差）
                }
                if (isMask(FPGA_CMD[0] & FPGA_CMD_TRIG_COUPLE)) fpgaCommand.gntR_trigCouple(i);  // 触发耦合
                if (isMask(FPGA_CMD[0] & FPGA_CMD_TRIG_RETRAIN))  // 触发抑制时间
                    fpgaCommand.gntR_trigRestrainTime(i);  // 发送触发抑制时间
                if (isMask(FPGA_CMD[0] & FPGA_CMD_AUTO_TRIG_T)) fpgaCommand.gntR_trigAutoTime(i);  // 自动触发时间
                if (isMask(FPGA_CMD[0] & FPGA_CMD_CH_OFFSET)) fpgaCommand.gntR_ChOffsetDa(i);  // 通道偏移DAC
                if (isMask(FPGA_CMD[0] & FPGA_CMD_AD_CH_CHANGE)) {  // AD通道切换
                    if (fpgaCommand.SendAdc_ch_change(i)) {  // 发送ADC通道切换并检查结果
                        FPGA_CMD[0] |= FPGA_CMD_DIS_PIX;  // 添加通道显示命令
                        fpgaCommand.resetBak(i);  // 重置备份数据
                    }
                }
            }

            // --- 第3步：处理显示相关命令 (FPGA_CMD[0] bit 16-27) ---
            if (isMask(FPGA_CMD[0] & FPGA_CMD_MASK_DIS)) {  // 有显示相关命令
                if (isMask(FPGA_CMD[0] & FPGA_CMD_DIS_MODE)) {  // 显示模式
                    fpgaCommand.gntR_disMode(i);  // 发送显示模式
                    bDisplay = true;  // 标记需要显示更新
                }

                if (isMask(FPGA_CMD[0] & (FPGA_CMD_DIS))) {  // 显示参数
                    fpgaCommand.gntR_Dis(i);  // 发送显示参数
                    fpgaCommand.gntR_yuHui(i);  // 发送余晖参数
                    bDisplay = true;  // 标记需要显示更新
                }
                if (isMask(FPGA_CMD[0] & FPGA_CMD_GET_DATA)) {  // 获取数据
                    fpgaCommand.cmdFpgaGetData(i);  // 发送获取数据命令
                }
                for (int j = beginIdx; j < endIdx; j++) {  // 遍历当前FPGA管理的通道
                    if (isMask(FPGA_CMD[0] & (FPGA_CMD_DIS_PIX_ch1 << (j - ChannelFactory.CH1)))) {  // 当前通道显示位
                        fpgaCommand.cmdFpgaDotMatrix(j);  // 发送通道点阵命令
                        bDisplay = true;  // 标记需要显示更新
                    }
                }
                if (isMask(FPGA_CMD[0] & FPGA_CMD_DIS_PIX)) {  // 通道显示命令
                    fpgaCommand.cmdTrigOffset(i);  // 发送触发偏移命令
                }
            }

            if (isMask(FPGA_CMD[0] & FPGA_CMD_SEGMENT)) {  // 段更新命令
                fpgaCommand.cmdSegment(i);  // 发送段命令
                bDisplay = true;  // 标记需要显示更新
            }

            // --- 第4步：处理扩展命令 (FPGA_CMD[1]) ---
            if (isMask(FPGA_CMD[1] & FPGA_CMD_EXT_MASK_SAMP)) {  // 有扩展采样命令
                if (isMask(FPGA_CMD[1] & FPGA_CMD_EXT_CHAZHI_COEF)) {  // 插值系数
                    fpgaCommand.cmdFpgaReloadChazhiCoef(i);  // 发送插值系数
                }
                if (isMask(FPGA_CMD[1] & FPGA_CMD_EXT_AD_COEF)) fpgaCommand.cmdReload_AD_coef(i);  // AD滤波系数
                boolean b = false;  // 是否有通道电压档位变化
                for (int j = beginIdx; j < endIdx; j++) {  // 遍历当前FPGA管理的通道
                    if (isMask(  // 检查当前通道电压档位是否变化
                            FPGA_CMD[1]
                                    & (FPGA_CMD_EXT_vScale_CHANGE_CH1 << (j - ChannelFactory.CH1)))) {
                        fpgaCommand.cmdReload_AD_coef_VOL_dangChange(j);  // 发送通道电压档位变化
                        b = true;  // 标记有变化
                    }
                }
                if (b) {  // 有通道电压档位变化
                    fpgaCommand.cmdChNoise(i);  // 更新通道噪声参数
                }

                if (isMask(FPGA_CMD[1] & FPGA_CMD_EXT_RESISTANCE)) {  // 阻抗设置
                    fpgaCommand.ChangeResistance(i);  // 发送阻抗设置
                }
                if (isMask(FPGA_CMD[1] & FPGA_CMD_EXT_PROBE_DA)) {  // 探头DA
                    fpgaCommand.ChangeProbeDa(i);  // 发送探头DA
                }
                if (isMask(FPGA_CMD[1] & FPGA_CMD_EXT_TRIGGER_LEVEL)) {  // 外部触发电平
                    fpgaCommand.ExtTriggerLevel(i);  // 发送外部触发电平
                }
            }
            
            // --- 第5步：处理总线命令 (FPGA_CMD[1] bit 26-31) ---
            if (isMask(FPGA_CMD[1] & FPGA_CMD_EXT_MASK_T)) {  // 有总线命令
                if (isMask(FPGA_CMD[1] & FPGA_CMD_BUS_TYPE)) {  // 总线类型
                    fpgaCommand.gntR_Bus(i);  // 发送总线配置
                }
                for (int j = 0; j < ChannelFactory.SERIAL_CNT / 2; j++) {  // 遍历当前FPGA管理的总线
                    int idx = i * ChannelFactory.SERIAL_CNT / 2 + j;  // 计算总线索引
                    if (isMask(FPGA_CMD[1] & (FPGA_CMD_BUS_S1 << idx))) {  // 检查对应总线Sx命令
                        fpgaCommand.Bus_Config(ChannelFactory.S1 + idx);  // 发送总线配置
                    }
                }
                if (isMask(FPGA_CMD[1] & FPGA_CMD_BUS_LEVEL)) {  // 总线电平
                    fpgaCommand.gntR_Bus_Level(i);  // 发送总线电平
                }
                bDisplay = true;  // 总线变化需要显示更新
            }
            
            // --- 第6步：更新寄存器 ---
            if (isMask(FPGA_CMD[0]) || isMask(FPGA_CMD[1])) {  // 有命令需要执行
                if (scope.isRun(true)) {  // 示波器正在运行
                    fpgaCommand.updataReg(i);  // 更新FPGA寄存器
                    bSampleTime = true;  // 标记需要更新采样时间
                } else {  // 示波器已停止
                    if (!isMask(FPGA_CMD[0] & FPGA_CMD_RUN_STOP) && bDisplay) {  // 非运行/停止命令且有显示更新
                        bUpdataRegDis = true;  // 标记需要更新显示寄存器
                    }
                }
            }

            // --- 第7步：处理运行/停止命令 ---
            if (isMask(FPGA_CMD[0] & FPGA_CMD_RUN_STOP)) {  // 有运行/停止命令
                if (scope.isRun(true)) {  // 示波器将要运行
                    if (sample.getSampleState() == Sample.SAMPLE_RUN) {  // 采样处于运行状态
                        cmdRun();  // 发送完整运行命令
                        bCmdRun = bSampleTime = true;  // 标记运行命令和采样时间
                    }
                } else {  // 示波器将要停止
                    displayCmd(true);  // 执行停止显示命令
                }
            }

            // --- 第8步：处理单次命令 ---
            if (isMask(FPGA_CMD[0] & FPGA_CMD_SINGLE)) {  // 有单次触发命令
                if (scope.isRun(true) && scope.isSingle()) {  // 示波器运行且处于单次模式
                    bSampleTime = true;  // 标记需要更新采样时间
                    bCmdSingle = true;  // 标记单次命令
                }
            }
            
            // --- 第9步：瞬态显示恢复 ---
            if (isMask(FPGA_CMD[0]) || isMask(FPGA_CMD[1])) {  // 有命令
                if (sample.getSampleState() == Sample.SAMPLE_TRANSIENT_RUN) {  // 瞬态运行状态
                    fpgaCommand.cmdFpgaResume(i);  // 恢复FPGA
                    bSampleTime = true;  // 标记需要更新采样时间
                }
                if (scope.isRun(true) && scope.isSingle()) {  // 运行且单次模式
                    bCmdSingle = true;  // 标记单次命令
                }
            }
        }
        
        if (bUpdataRegDis) {  // 需要更新显示寄存器
            fpgaCommand.updataRegDis(0);  // 更新FPGA 0的显示寄存器
        }

        if (scope.isRun(true) && syncheader >= 0) {  // 运行状态且有有效同步头
            scopeFrozen.setSyncHeader(syncheader);  // 同步冻结状态的同步头
        }
        
        clr_Cmd(n);  // 清除命令寄存器
        
        if (bSampleTime) {  // 需要更新采样时间
            updateSampleTime();  // 更新段采样开始时间
        }
    }

    /**
     * FPGA同步
     * 
     * <p>在命令执行后执行FPGA同步操作。
     * 根据命令标志选择发送运行、单次或更新寄存器命令。
     */
    public void fpgaSync() {
        if (Scope.getInstance().isRun(true)) {  // 示波器正在运行
            if (bCmdSingle) {  // 有单次命令待执行
                fpgaCommand.cmdFpgaSingle(0);  // 发送FPGA单次命令
            } else if (bCmdRun) {  // 有运行命令待执行
                fpgaCommand.cmdFpgaRun(0);  // 发送FPGA运行命令
            } else {  // 普通命令
                fpgaCommand.updataReg(0);  // 更新FPGA寄存器
            }
            bCmdRun = bCmdSingle = false;  // 重置命令标志
            Log.d(TAG, "fpgaSync() called");  // 输出同步日志
        }
    }

    /**
     * 线程休眠
     * 
     * @param ms 休眠时间（毫秒）
     */
    private void sleep_ms(long ms) {
        try {
            Thread.sleep(ms);  // 线程休眠
        } catch (Exception e) {  // 捕获中断异常
            e.printStackTrace();  // 打印异常堆栈
        }
    }

    /**
     * 更新采样开始时间
     */
    private void updateSampleTime() {
        SegmentSample.getInstance().updateBeginSampleTime();  // 委托段采样管理更新开始时间
    }

    /**
     * 获取段采样时间戳
     */
    public void segmentTimestamp() {
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            fpgaCommand.segmentTimestamp(i);  // 发送段时间戳命令
        }
    }

    /**
     * 强制触发
     */
    public void forceTrigger() {
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            fpgaCommand.forceTrigger(i);  // 发送强制触发命令
        }
    }

    /**
     * 强制AD校准
     */
    public void forceAdCalibation() {
        if (!fpgaCommand.isCalibrate()) {  // 不是校准状态
            for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
                fpgaCommand.forceAdCablition(i);  // 发送强制AD校准命令
            }
        }
    }

    // ==================== FPGA状态读取方法 ====================
    
    /**
     * 读取FPGA ID
     * 
     * @return FPGA ID（两个FPGA ID的按位或结果）
     */
    public int fpgaId() {
        return fpgaCommand.readFpgaId(0) | fpgaCommand.readFpgaId(1);  // 读取两个FPGA ID并合并
    }

    /**
     * 读取FPGA状态
     * 
     * @return FPGA状态值
     */
    public int fpgaStatus() {
        return fpgaCommand.readFpgaStatus(0);  // 读取FPGA 0的状态
    }

    /**
     * 读取FPGA版本号
     * 
     * @return FPGA版本号
     */
    public int fpgaVersion() {
        return fpgaCommand.readFpgaVer(0);  // 读取FPGA 0的版本
    }

    /**
     * 发送段命令到所有FPGA
     */
    public void cmdSegment() {
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            fpgaCommand.cmdSegment(i);  // 发送段命令
        }
    }

    /**
     * 获取FPGA状态信息
     * 
     * @param fpgaStatus FPGA状态对象（被填充）
     */
    public void getFPGA_Status(FPGA_Status fpgaStatus) {
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            fpgaCommand.readFpgaStatus(i, fpgaStatus);  // 读取FPGA状态并填充对象
        }
        fpgaStatus.check();  // 检查FPGA状态有效性
    }

    /**
     * 判断是否有延迟运行的命令
     * 
     * <p>命令寄存器非空且命令计数大于0时返回true。
     * 用于ScopeMessage判断是否需要延迟20ms后执行。
     * 
     * @return true表示有待执行命令
     */
    public boolean isDelayRun() {
        return ((FPGA_CMD[0] != 0 || FPGA_CMD[1] != 0) && mNum > 0);  // 命令寄存器非空且计数有效
    }

    /**
     * 获取段帧数
     * 
     * <p>从FPGA读取当前采集的段帧数。
     * 
     * @return 段帧数，负数表示无效
     */
    public int getSegmentFrames() {
        int numFrames = fpgaCommand.cmdFpgaGetSegmentFrames(0);  // 读取FPGA 0的段帧数
        return numFrames;  // 返回段帧数
    }

    /**
     * 获取50Ω阻抗状态
     * 
     * <p>每个FPGA管理一半通道的阻抗，结果合并到返回值。
     * 
     * @return 50Ω阻抗状态位掩码（每位对应一个通道）
     */
    public int get50O() {
        int v = 0;  // 阻抗状态变量
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            v |= fpgaCommand.cmdFpgaGet50O(i) << (i * ChannelFactory.CH_CNT / 2);  // 读取并移位合并
        }
        return v;  // 返回合并后的阻抗状态
    }

    /**
     * 处理探头事件
     */
    public void probeProcess() {
        for (int i = 0; i < fpgaNums; i++) {  // 遍历所有FPGA
            fpgaCommand.probeProcess(i);  // 处理探头检测
        }
    }

    /**
     * 获取FPGA温度
     * 
     * @param fpgaIdx FPGA索引（0或1）
     * @return 温度值（摄氏度）
     */
    public int getFpgaTemperature(int fpgaIdx) {
        return fpgaCommand.getFpgaTemperature(fpgaIdx);  // 读取指定FPGA的温度
    }

    /**
     * 获取FPGA时钟输入/输出状态
     * 
     * @param isIn true获取输入时钟状态，false获取输出时钟状态
     * @return 时钟状态（true正常，false异常）
     */
    public boolean getFpgaClockInOutStatus(boolean isIn) {
        return fpgaCommand.getFpgaClockInOutStatus(0, isIn);  // 读取FPGA 0的时钟状态
    }

    /**
     * 获取风扇转速
     * 
     * <p>从FPGA读取风扇转速并填充数组。
     * 
     * @param fans 风扇转速数组（4个元素，[0][1]对应FPGA0，[2][3]对应FPGA1）
     * @return 风扇转速值
     */
    public int getFanSpeed(int[] fans) {
        int[] tmp = {0, 0};  // 临时数组存储单个FPGA的转速
        fpgaCommand.getFpgaFanSpeed(0, tmp);  // 读取FPGA 0的风扇转速
        fans[0] = tmp[0];  // 风扇1转速
        fans[1] = tmp[1];  // 风扇2转速
        fpgaCommand.getFpgaFanSpeed(1, tmp);  // 读取FPGA 1的风扇转速
        fans[2] = tmp[0];  // 风扇3转速
        fans[3] = tmp[1];  // 风扇4转速
        return fans[0];  // 返回风扇1转速
    }

    /**
     * 获取FPGA物理状态
     * 
     * @return FPGA状态值
     */
    public int getfpgaStatus() {
        return fpgaCommand.getFpgaStatus();  // 读取FPGA物理状态
    }

    // ==================== 探头通信方法 ====================
    
    /**
     * 发送探头命令
     * 
     * <p>通过ProbeFactory封装探头命令，委托给FPGACommand发送。
     * 
     * @param chIdx 通道索引
     * @param bytes 探头命令字节数据
     */
    public void sendProbe(int chIdx, byte[] bytes) {
        ProbeFactory probeFactory = ProbeFactory.getInstance();  // 获取探头工厂单例

        probeFactory.sendProbeCommand(chIdx, bytes, new ProbeCommand.ProbeCommandlistener() {  // 通过工厂发送探头命令
            @Override
            public void onProbeCommand(ProbeCommand command) {  // 探头命令回调
                fpgaCommand.SendProbe(command.getChIdx(), command.getCmd());  // 委托FPGACommand发送探头命令到硬件
            }
        });
    }

    /**
     * 检查探头命令超时
     * 
     * <p>检查待发送的探头命令是否超时，超时的探头标记为异常。
     */
    public void checkProbe() {
        ProbeFactory probeFactory = ProbeFactory.getInstance();  // 获取探头工厂单例
        List<ProbeCommand> list = probeFactory.checkProbeCommandTimeout(new ProbeCommand.ProbeCommandlistener() {  // 检查超时并重试
            @Override
            public void onProbeCommand(ProbeCommand command) {  // 超时重试回调
                fpgaCommand.SendProbe(command.getChIdx(), command.getCmd());  // 重新发送探头命令
            }
        });
        
        for (ProbeCommand probeCommand : list) {  // 遍历超时的探头命令
            probeFactory.probeAbnormal(probeCommand.getChIdx());  // 标记探头异常
        }
    }

    /**
     * 检查是否在校准状态
     * 
     * @return true表示正在执行校准（需要特殊处理采样状态）
     */
    public boolean isCalibrate() {
        return fpgaCommand.isCalibrate();  // 委托FPGACommand判断校准状态
    }
}