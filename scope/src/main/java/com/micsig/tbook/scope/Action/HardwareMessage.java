package com.micsig.tbook.scope.Action;  // 定义包名：示波器动作处理模块

import android.util.Log;  // 导入Log类：Android日志输出工具

import com.micsig.base.Logger;  // 导入Logger类：项目基础日志工具
import com.micsig.tbook.hardware.HardwareProduct;  // 导入HardwareProduct类：硬件产品型号判断工具
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入HwConfig类：硬件配置管理，提供FPGA数量等配置
import com.micsig.tbook.scope.Display.Display;  // 导入Display类：显示管理，处理缩放等显示状态
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理，提供运行状态判断
import com.micsig.tbook.scope.channel.Channel;  // 导入Channel类：通道数据模型
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂，提供通道遍历和索引常量
import com.micsig.tbook.scope.fpga.FPGACommand;  // 导入FPGACommand类：FPGA命令管理器，所有硬件命令的实际执行者
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入HorizontalAxis类：水平轴管理，控制时基档位

/**
 * 硬件消息处理与命令调度中心
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Action（示波器动作处理模块）</li>
 *   <li>架构层级：业务逻辑层 - 硬件命令调度层</li>
 *   <li>设计模式：命令模式 + 位掩码合并</li>
 *   <li>职责类型：硬件命令定义、命令队列管理与调度执行</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义和管理硬件命令常量（通道偏移、耦合方式、垂直档位）</li>
 *   <li>维护硬件命令队列（HW_CMD），支持命令合并与批量执行</li>
 *   <li>执行硬件命令调度（通道偏移控制、耦合控制、垂直档位控制）</li>
 *   <li>管理通道采样状态变化处理</li>
 *   <li>协调FPGA初始化和硬件恢复</li>
 * </ul>
 * 
 * <p><b>硬件命令寄存器布局（32位HW_CMD）：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   HW_CMD ─ 硬件命令寄存器 (32位)                                          │
 * │                                                                          │
 * │   bit[1-8]   ─ 通道偏移命令 (HARD_ID_OFFSET_MASK)                         │
 * │   ├── bit[1]:   CH1偏移修改 (HARD_ID_OFFSET_CH1)                         │
 * │   ├── bit[2]:   CH2偏移修改 (HARD_ID_OFFSET_CH2)                         │
 * │   ├── ...                                                                 │
 * │   └── bit[8]:   CH8偏移修改 (HARD_ID_OFFSET_CH8)                         │
 * │                                                                          │
 * │   bit[9-16]  ─ 通道耦合命令 (HARD_ID_CH_COUP_MASK)                        │
 * │   ├── bit[9]:   CH1耦合修改 (HARD_ID_CH_COUP_CH1)                        │
 * │   ├── bit[10]:  CH2耦合修改 (HARD_ID_CH_COUP_CH2)                        │
 * │   ├── ...                                                                 │
 * │   └── bit[16]:  CH8耦合修改 (HARD_ID_CH_COUP_CH8)                        │
 * │                                                                          │
 * │   bit[17-24] ─ 通道垂直档位命令 (HARD_ID_CH_vSCALE_MASK)                   │
 * │   ├── bit[17]:  CH1垂直档位修改 (HARD_ID_CH_vSCALE_CH1)                  │
 * │   ├── bit[18]:  CH2垂直档位修改 (HARD_ID_CH_vSCALE_CH2)                  │
 * │   ├── ...                                                                 │
 * │   └── bit[24]:  CH8垂直档位修改 (HARD_ID_CH_vSCALE_CH8)                  │
 * │                                                                          │
 * │   bit[0], bit[25-31] ─ 保留位（未使用）                                    │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>命令合并策略：</b>
 * <ul>
 *   <li>使用位或操作（|=）将多个命令合并到命令寄存器中</li>
 *   <li>延迟执行策略：先收集命令，在run()中批量执行</li>
 *   <li>避免频繁的硬件操作，提升系统性能</li>
 * </ul>
 * 
 * <p><b>与FPGAMessage的区别：</b>
 * <ul>
 *   <li>FPGAMessage：管理FPGA内部寄存器命令（采样、触发、显示等）</li>
 *   <li>HardwareMessage：管理外部硬件控制命令（继电器、PGA、AD等）</li>
 *   <li>两者协同工作，共同完成示波器的硬件控制</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：FPGACommand（FPGA命令管理器，负责底层硬件交互）</li>
 *   <li>依赖：ChannelHardw（通道硬件控制，管理耦合/档位/增益）</li>
 *   <li>依赖：Scope（示波器状态管理，提供运行/停止判断）</li>
 *   <li>依赖：ChannelFactory（通道工厂，提供通道遍历）</li>
 *   <li>依赖：HorizontalAxis（水平轴控制，处理时基档位限制）</li>
 *   <li>依赖：HwConfig（硬件配置，提供FPGA数量）</li>
 *   <li>依赖：HardwareProduct（硬件产品型号判断）</li>
 *   <li>被依赖：ScopeMessage（消息处理中心，调用resume/run）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/13
 * @see FPGAMessage FPGA消息处理
 * @see ChannelHardw 通道硬件控制
 * @see FPGACommand FPGA命令管理器
 */
public class HardwareMessage {
    
    private static final String TAG = "HardwareMessage";  // 日志标签：用于Log输出时标识来源
    
    // ==================== 通道偏移命令常量 (HW_CMD bit 1-8) ====================
    
    /** 通道偏移命令掩码（bit 1-8），用于判断是否有偏移相关命令 */
    public static final int HARD_ID_OFFSET_MASK = 0xFF << 1;  // 0x000001FE：bit[1-8]全1，掩码出偏移命令
    
    /** CH1偏移修改命令：通道1的垂直偏移调整 */
    public static final int HARD_ID_OFFSET_CH1 = 1<<1;  // bit[1] = 0x00000002
    
    /** CH2偏移修改命令：通道2的垂直偏移调整 */
    public static final int HARD_ID_OFFSET_CH2 = 1<<2;  // bit[2] = 0x00000004
    
    /** CH3偏移修改命令：通道3的垂直偏移调整 */
    public static final int HARD_ID_OFFSET_CH3 = 1<<3;  // bit[3] = 0x00000008
    
    /** CH4偏移修改命令：通道4的垂直偏移调整 */
    public static final int HARD_ID_OFFSET_CH4 = 1<<4;  // bit[4] = 0x00000010
    
    /** CH5偏移修改命令：通道5的垂直偏移调整 */
    public static final int HARD_ID_OFFSET_CH5 = 1<<5;  // bit[5] = 0x00000020
    
    /** CH6偏移修改命令：通道6的垂直偏移调整 */
    public static final int HARD_ID_OFFSET_CH6 = 1<<6;  // bit[6] = 0x00000040
    
    /** CH7偏移修改命令：通道7的垂直偏移调整 */
    public static final int HARD_ID_OFFSET_CH7 = 1<<7;  // bit[7] = 0x00000080
    
    /** CH8偏移修改命令：通道8的垂直偏移调整 */
    public static final int HARD_ID_OFFSET_CH8 = 1<<8;  // bit[8] = 0x00000100

    // ==================== 通道耦合命令常量 (HW_CMD bit 9-16) ====================
    
    /** 通道耦合命令掩码（bit 9-16），用于判断是否有耦合相关命令 */
    public static final int HARD_ID_CH_COUP_MASK = 0xFF<<9;  // 0x0001FE00：bit[9-16]全1，掩码出耦合命令
    
    /** CH1耦合修改命令：通道1的耦合方式切换（AC/DC/GND） */
    public static final int HARD_ID_CH_COUP_CH1= 1<<9;  // bit[9] = 0x00000200
    
    /** CH2耦合修改命令：通道2的耦合方式切换 */
    public static final int HARD_ID_CH_COUP_CH2= 1<<10;  // bit[10] = 0x00000400
    
    /** CH3耦合修改命令：通道3的耦合方式切换 */
    public static final int HARD_ID_CH_COUP_CH3= 1<<11;  // bit[11] = 0x00000800
    
    /** CH4耦合修改命令：通道4的耦合方式切换 */
    public static final int HARD_ID_CH_COUP_CH4= 1<<12;  // bit[12] = 0x00001000
    
    /** CH5耦合修改命令：通道5的耦合方式切换 */
    public static final int HARD_ID_CH_COUP_CH5= 1<<13;  // bit[13] = 0x00002000
    
    /** CH6耦合修改命令：通道6的耦合方式切换 */
    public static final int HARD_ID_CH_COUP_CH6= 1<<14;  // bit[14] = 0x00004000
    
    /** CH7耦合修改命令：通道7的耦合方式切换 */
    public static final int HARD_ID_CH_COUP_CH7= 1<<15;  // bit[15] = 0x00008000
    
    /** CH8耦合修改命令：通道8的耦合方式切换 */
    public static final int HARD_ID_CH_COUP_CH8= 1<<16;  // bit[16] = 0x00010000

    // ==================== 通道垂直档位命令常量 (HW_CMD bit 17-24) ====================
    
    /** 通道垂直档位命令掩码（bit 17-24），用于判断是否有垂直档位相关命令 */
    public static final int HARD_ID_CH_vSCALE_MASK = 0xFF<<17;  // 0x01FE0000：bit[17-24]全1，掩码出档位命令
    
    /** CH1垂直档位修改命令：通道1的电压档位切换 */
    public static final int HARD_ID_CH_vSCALE_CH1=1<<17;  // bit[17] = 0x00020000
    
    /** CH2垂直档位修改命令：通道2的电压档位切换 */
    public static final int HARD_ID_CH_vSCALE_CH2=1<<18;  // bit[18] = 0x00040000
    
    /** CH3垂直档位修改命令：通道3的电压档位切换 */
    public static final int HARD_ID_CH_vSCALE_CH3=1<<19;  // bit[19] = 0x00080000
    
    /** CH4垂直档位修改命令：通道4的电压档位切换 */
    public static final int HARD_ID_CH_vSCALE_CH4=1<<20;  // bit[20] = 0x00100000
    
    /** CH5垂直档位修改命令：通道5的电压档位切换 */
    public static final int HARD_ID_CH_vSCALE_CH5=1<<21;  // bit[21] = 0x00200000
    
    /** CH6垂直档位修改命令：通道6的电压档位切换 */
    public static final int HARD_ID_CH_vSCALE_CH6=1<<22;  // bit[22] = 0x00400000
    
    /** CH7垂直档位修改命令：通道7的电压档位切换 */
    public static final int HARD_ID_CH_vSCALE_CH7=1<<23;  // bit[23] = 0x00800000
    
    /** CH8垂直档位修改命令：通道8的电压档位切换 */
    public static final int HARD_ID_CH_vSCALE_CH8=1<<24;  // bit[24] = 0x01000000


    // ==================== 成员变量 ====================
    
    /** FPGA数量：决定循环执行命令的次数 */
    private int fpgaNums = 1;  // 默认为1个FPGA

    // ==================== 构造方法 ====================
    
    /**
     * 构造方法：初始化硬件消息处理器
     * 
     * <p>从硬件配置中获取FPGA数量，用于后续命令执行时的循环次数。
     */
    public HardwareMessage(){
        fpgaNums = HwConfig.getInstance().getFpgaNums();  // 从硬件配置获取FPGA数量
    }

    // ==================== 生命周期方法 ====================
    
    /**
     * 恢复硬件状态
     * 
     * <p>在示波器从暂停状态恢复时调用，重新初始化硬件状态。
     * 执行顺序：通道偏移 → 通道耦合 → 通道垂直档位 → AD初始化
     */
    public void resume(){
        ctrlChOffset();  // 控制通道偏移：设置所有通道的垂直偏移
        ctrlChCoup();  // 控制通道耦合：设置所有通道的耦合方式
        ctrlChV();  // 控制通道垂直档位：设置所有通道的电压档位和增益
        FPGACommand cmd = FPGACommand.getInstance();  // 获取FPGA命令管理器单例
        for(int i=0;i<fpgaNums;i++) {  // 遍历所有FPGA
            cmd.AdInit(i);  // 初始化AD转换器
        }
    }
    
    /**
     * 暂停硬件状态
     * 
     * <p>当前为空实现，暂停时无需特殊处理。
     */
    public void pause(){
        // 暂停时无需特殊处理
    }
    
    // ==================== 通道采样状态管理 ====================
    
    /**
     * 处理通道采样状态变化
     * 
     * <p>遍历所有通道，根据示波器的采样配置更新通道的采样状态。
     * 当采样状态发生变化时，处理缩放更新和时基档位限制。
     * 
     * <p><b>业务逻辑：</b>
     * <ol>
     *   <li>遍历所有通道，检查是否在采样列表中</li>
     *   <li>更新通道的采样标志（isSample）</li>
     *   <li>如果采样状态变化，处理缩放更新和时基限制</li>
     *   <li>对于MHO68系列，根据采样通道数限制最大时基档位</li>
     * </ol>
     */
    public void chSampleChange(){
        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例

        boolean[] bSampleChange = {false};  // 采样状态变化标志（使用数组以便在lambda中修改）
        ChannelFactory.forEachCh(channel -> {  // 遍历所有通道
            if(scope.isChannelInSample(channel.getChId())){  // 检查通道是否在采样列表中
                if(!channel.isSample()){  // 通道当前未采样，需要开启
                    channel.setSample(true);  // 设置采样标志为true
                    bSampleChange[0] = true;  // 标记采样状态已变化
                }
            }else {  // 通道不在采样列表中
                if(channel.isSample()){  // 通道当前正在采样，需要关闭
                    channel.setSample(false);  // 设置采样标志为false
                    bSampleChange[0] = true;  // 标记采样状态已变化
                }
            }
            channel.setNeedWave(true);  // 标记通道需要波形数据（用于显示更新）
//            Log.i(TAG,"CH" + (channel.getChId()+1) + ",isChannelInSample = "
//                    + scope.isChannelInSample(channel.getChId()) + ",channel.isSample = " + channel.isSample()
//                    + ",needWave = " + channel.isNeedWave());
        });
        if(bSampleChange[0]) {  // 如果采样状态发生变化
            if (Display.getInstance().isZoom()) {  // 检查是否处于缩放模式
                Display.getInstance().zoomChange();  // 触发缩放变化处理，更新缩放窗口
            }
            if(HardwareProduct.isMHO68V1()  // 检查是否为MHO68 V1版本
                    || HardwareProduct.isMHO68V2()) {  // 或MHO68 V2版本
                // MHO68系列特殊处理：根据采样通道数限制最大时基档位
                int cnt = scope.getChannelSampOnCnt();  // 获取当前采样的通道数量
                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();  // 获取水平轴管理单例
                int scaleId = horizontalAxis.getTimeScaleIdOfView();  // 获取当前时基档位ID
                // 根据采样通道数确定最大时基档位：全通道采样时最大500ps，否则最大250ps
                int maxGear = cnt == ChannelFactory.CH_CNT ? HorizontalAxis.TSI_500pS : HorizontalAxis.TSI_250pS;
                if (scaleId > maxGear) {  // 如果当前档位超过最大限制
                    scaleId = maxGear;  // 限制到最大档位
                }
                HorizontalAxis.setMaxGear(maxGear);  // 设置最大时基档位限制
                horizontalAxis.initXAxis();  // 重新初始化X轴
                horizontalAxis.setTimeScaleIdOfView(scaleId);  // 设置时基档位
            }
        }
    }
    
    // ==================== 命令队列管理 ====================
    
    /** 硬件命令寄存器：32位，存储待执行的硬件命令位掩码 */
    volatile int HW_CMD = 0;  // 使用volatile确保多线程可见性
    
    /**
     * 添加硬件命令到命令队列
     * 
     * <p>使用位或操作将命令合并到命令寄存器中，支持多个命令的批量执行。
     * 
     * @param arg1 命令位掩码（主命令，当前未使用）
     * @param arg2 命令位掩码（实际使用的命令值）
     */
    public void add(int arg1,int arg2){
        HW_CMD |= arg1;  // 将命令位合并到命令寄存器（使用arg1，arg2当前未使用）
    }
    
    /**
     * 获取当前命令寄存器值
     * 
     * @return 当前待执行的硬件命令位掩码
     */
    private int getCmd(){
        return HW_CMD;  // 返回命令寄存器当前值
    }
    
    /**
     * 清除已执行的命令位
     * 
     * <p>使用位与操作清除指定的命令位，保留其他未执行的命令。
     * 
     * @param cmd 要清除的命令位掩码
     */
    private  void clearCmd(int cmd){
        HW_CMD = HW_CMD & ~cmd;  // 使用位与非操作清除指定命令位
    }

    /**
     * 检查值是否非零（是否有命令）
     * 
     * @param val 要检查的值
     * @return true表示有命令，false表示无命令
     */
    private boolean isMask(int val){
        return val != 0;  // 非零表示有命令位被设置
    }
    
    /**
     * 检查是否有延迟执行的命令
     * 
     * <p>用于判断是否需要延迟执行硬件命令。
     * 当HW_CMD非零时，表示有待执行的硬件命令。
     * 
     * @return true表示有待执行的命令，false表示命令队列为空
     */
    public synchronized boolean isDelayRun(){
        return (HW_CMD != 0);  // 命令寄存器非零表示有待执行命令
    }

    // ==================== 命令执行调度 ====================
    
    /**
     * 执行硬件命令调度
     * 
     * <p>解析命令寄存器中的命令位，依次执行对应的硬件控制操作。
     * 执行顺序：通道偏移 → 通道耦合 → 通道垂直档位
     * 
     * <p><b>执行条件：</b>仅在示波器运行状态下执行命令。
     */
    public void run(){
        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
        if(!scope.isRun()) return;  // 如果示波器未运行，直接返回不执行
        int cmd = getCmd();  // 获取当前命令寄存器值
        Log.d(TAG,"beging hw:" + Integer.toHexString(cmd));  // 输出开始执行的命令日志（十六进制）

        if (isMask(cmd & (HARD_ID_OFFSET_MASK))) {  // 检查是否有通道偏移命令
            ctrlChOffset();  // 执行通道偏移控制
        }
        if (isMask(cmd & (HARD_ID_CH_COUP_MASK))) {  // 检查是否有通道耦合命令
            ctrlChCoup();  // 执行通道耦合控制
        }
        if (isMask(cmd & (HARD_ID_CH_vSCALE_MASK))) {// 检查是否有垂直档位变化命令
            Log.d(TAG,"调整档位---------7---------ctrlChV");  // 输出档位调整日志
            ctrlChV();  // 执行通道垂直档位控制
        }
        clearCmd(cmd);  // 清除已执行的命令位
        Log.d(TAG,"end hw:" + Integer.toHexString(cmd));  // 输出执行完成的命令日志（十六进制）
    }
    
    // ==================== 通道硬件控制方法 ====================
    
    /**
     * 控制通道偏移
     * 
     * <p>设置所有通道的垂直偏移（零点位置）。
     * 通过FPGACommand设置偏移DAC值，并发送设备命令。
     */
    private void ctrlChOffset(){
        FPGACommand cmd = FPGACommand.getInstance();  // 获取FPGA命令管理器单例
        for(int i=0;i<fpgaNums;i++) {  // 遍历所有FPGA
            cmd.gntR_ChOffsetDa(i);  // 设置当前FPGA管理的通道偏移DAC值
        }
        cmd.cmdDevice(cmd.isCalibrate() ? 100 : 90);  // 发送设备命令：校准模式100ms，正常模式90ms
    }
    
    /**
     * 控制通道耦合方式
     * 
     * <p>切换所有通道的耦合方式（AC/DC/GND）。
     * 通过ChannelHardw控制继电器切换耦合方式。
     */
    private void ctrlChCoup(){
        FPGACommand cmd = FPGACommand.getInstance();  // 获取FPGA命令管理器单例
        ChannelHardw.getInstance().changeChCoup();  // 调用通道硬件控制切换耦合方式
        cmd.cmdDevice(cmd.isCalibrate() ? 200 : 150);  // 发送设备命令：校准模式200ms，正常模式150ms
    }
    
    /**
     * 控制通道垂直档位
     * 
     * <p>切换所有通道的电压档位，包括继电器控制、PGA增益设置、
     * FPGA寄存器更新等完整流程。
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>发送设备命令开始档位切换</li>
     *   <li>调用ChannelHardw切换继电器（硬件档位）</li>
     *   <li>设置PGA增益（AD8370可编程增益放大器）</li>
     *   <li>更新FPGA寄存器（AD增益、电容、电阻等）</li>
     *   <li>同步AD并完成</li>
     * </ol>
     */
    private void ctrlChV(){
        Log.d(TAG,"调整档位---------8---------");  // 输出档位调整日志
        ChannelHardw hardw = ChannelHardw.getInstance();  // 获取通道硬件控制单例
        FPGACommand cmd = FPGACommand.getInstance();  // 获取FPGA命令管理器单例
        cmd.cmdDevice(cmd.isCalibrate() ? 300 : 200);  // 发送设备命令：校准模式300ms，正常模式200ms
        hardw.changeChVolScale();  // 关键：调用硬件控制改变垂直档位（继电器控制）
        hardw.ctrlAD8370Gain();  // 设置PGA增益（AD8370可编程增益放大器）
      // 更新FPGA寄存器
        for(int i=0;i<fpgaNums;i++) {  // 遍历所有FPGA
            hardw.wrte_ad_gain(i);  // 写入AD增益到FPGA寄存器
            cmd.updateChCapacitance(i);  // 更新通道电容配置
            cmd.ADCalibrate();  // 执行AD校准
            cmd.ChangeResistance(i);  // 改变电阻配置
            cmd.updataReg(i);  // 更新FPGA寄存器
        }
        cmd.adSync();  // 同步AD配置
        cmd.cmdDevice(cmd.isCalibrate() ? 200 : 150);  // 发送设备命令完成：校准模式200ms，正常模式150ms

    }
}
