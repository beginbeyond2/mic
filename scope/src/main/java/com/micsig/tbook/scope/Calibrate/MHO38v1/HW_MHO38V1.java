package com.micsig.tbook.scope.Calibrate.MHO38v1;  // 包声明：MHO38 V1示波器校准模块

import android.util.Log;  // 导入：Android日志工具，用于输出调试信息

import com.micsig.base.DoubleUtil;  // 导入：双精度工具类，用于数学计算
import com.micsig.tbook.hardware.Hardware;  // 导入：硬件抽象类，用于硬件操作
import com.micsig.tbook.hardware.HardwareProduct;  // 导入：硬件产品类，用于产品信息
import com.micsig.tbook.scope.Calibrate.CabteRegister;  // 导入：校准寄存器管理类，用于管理校准数据
import com.micsig.tbook.scope.Calibrate.HW;  // 导入：硬件抽象基类，定义硬件操作接口
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入：硬件配置类，用于获取硬件参数
import com.micsig.tbook.scope.Scope;  // 导入：示波器主类，用于获取示波器状态
import com.micsig.tbook.scope.ScopeBase;  // 导入：示波器基类，包含基础配置
import com.micsig.tbook.scope.channel.Channel;  // 导入：通道类，定义通道属性和操作
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入：通道工厂类，用于通道管理
import com.micsig.tbook.scope.fpga.FPGACommand;  // 导入：FPGA命令类，用于发送FPGA控制命令
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入：垂直轴类，用于垂直档位转换

import java.nio.ByteBuffer;  // 导入：字节缓冲区类，用于数据序列化
import java.util.Arrays;  // 导入：数组工具类，用于数组操作

/**
 * MHO38 V1示波器硬件操作实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO38v1（MHO38 V1示波器校准模块）</li>
 *   <li>架构层级：硬件抽象层 - 具体硬件操作实现</li>
 *   <li>设计模式：继承HW抽象基类，实现MHO38 V1硬件操作</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO38 V1示波器的硬件操作接口</li>
 *   <li>管理通道PGA增益和ADC满量程配置</li>
 *   <li>控制通道电压档位切换</li>
 *   <li>管理校准状态和校准数据</li>
 *   <li>提供校准系数计算方法</li>
 * </ul>
 * 
 * <p><b>硬件特性：</b>
 * <pre>
 * 通道数量：8通道
 * 档位数量：4档（10mV, 100mV, 500mV, 5V）
 * 阻抗类型：仅支持1MΩ
 * PGA芯片：LMH6518（可编程增益放大器）
 * ADC分辨率：10位（0-1023）
 * DA分辨率：16位（0-65535）
 * </pre>
 * 
 * <p><b>校准项目：</b>
 * <pre>
 * 1. 零点校准（CALIBRATION_TOP_ZERO）
 *    - 消除通道直流偏移
 *    - 每个通道每个档位需要校准
 * 
 * 2. 通道增益校准（CALIBRATION_CENTER_CHxGAIN）
 *    - 校准通道增益系数
 *    - 每个通道8个输入幅度点
 * 
 * 3. 偏移量校准（CHOFFSET_CALIBRATE_CHx）
 *    - 校准通道偏移量系数
 *    - 每个通道8个档位组合
 * 
 * 4. 电容校准（CHCAP_CALIBRATE_CHx）
 *    - 校准输入电容
 *    - 每个通道1个值
 * </pre>
 * 
 * <p><b>档位映射：</b>
 * <pre>
 * RATIO_DANG_1 (0): 10mV/div
 * RATIO_DANG_2 (1): 100mV/div
 * RATIO_DANG_3 (2): 500mV/div
 * RATIO_DANG_4 (3): 5V/div
 * </pre>
 * 
 * <p><b>PGA增益控制：</b>
 * <pre>
 * PGA值格式：0x4XY
 *   X: 增益档位选择（0=低增益，1=高增益）
 *   Y: 增益步进值（0-10）
 * 
 * 低增益模式（X=0）：
 *   PGA = 0x400 + y
 *   增益 = 10^((1-y) × pga_stepdb / 20)
 * 
 * 高增益模式（X=1）：
 *   PGA = 0x410 + y
 *   增益 = 10^((1-y) × pga_stepdb / 20) × lghg
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：HW（硬件抽象基类）</li>
 *   <li>依赖：CabteRegister（校准寄存器管理）</li>
 *   <li>依赖：FPGACommand（FPGA命令管理）</li>
 *   <li>依赖：ChannelFactory（通道工厂）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>示波器启动时初始化硬件配置</li>
 *   <li>用户切换档位时更新硬件参数</li>
 *   <li>校准过程中读取和写入校准数据</li>
 *   <li>正常运行时计算通道系数</li>
 * </ul>
 * 
 * @author zhuzh  // 作者：zhuzh
 * @version 1.0  // 版本号：1.0
 * @see HW 硬件抽象基类  // 参见：HW类
 * @see CabteRegister 校准寄存器管理  // 参见：CabteRegister类
 */
public class HW_MHO38V1 extends HW {  // 继承HW抽象基类，实现MHO38 V1硬件操作
    
    /**
     * 日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于日志输出时标识MHO38 V1硬件操作模块</li>
     *   <li>简化日志输出，便于日志过滤</li>
     * </ul>
     */
    private static final String TAG = "HW_MHO38V1";  // 日志标签，固定为"HW_MHO38V1"
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，初始化FPGA数量</li>
     *   <li>添加校准项目列表</li>
     * </ul>
     * 
     * <p><b>校准项目列表：</b>
     * <pre>
     * 1. 零点校准
     * 2. 通道增益校准（需要信号）
     * 3. 偏移量校准（需要信号）
     * 4. 电容校准（需要信号）
     * </pre>
     * 
     * @param fpgaNums FPGA数量
     */
    public HW_MHO38V1(int fpgaNums){  // 构造函数，接收FPGA数量参数
        super(fpgaNums);  // 调用父类HW的构造函数，传递FPGA数量
        addItemAll(Arrays.asList("零点校准","通道增益校准(需要信号)","偏移量校准(需要信号)","电容校准(需要信号)"));  // 添加校准项目列表
    }  // 构造函数结束

    /**
     * 设置通道PGA增益
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setChPga方法</li>
     *   <li>根据通道档位计算PGA增益值</li>
     *   <li>设置所有通道的PGA增益</li>
     * </ul>
     * 
     * <p><b>计算流程：</b>
     * <pre>
     * 1. 遍历所有通道
     * 2. 获取通道的垂直档位值（考虑探头衰减）
     * 3. 计算PGA、满量程和增益值
     * 4. 提取PGA值（低16位）
     * 5. 设置通道PGA增益
     * </pre>
     */
    @Override  // 覆盖父类方法
    public void setChPga() {  // 设置通道PGA增益方法
        Channel channel;  // 通道对象，用于获取通道参数
        CabteRegister cabteRegister = CabteRegister.getInstance();  // 获取校准寄存器实例
        final int [] vol = new int[ChannelFactory.CH_CNT];  // PGA值数组，每个通道一个值
        int [] result = {0,0,0,0,0};  // 结果数组：[PGA, 满量程I, 满量程Q, 增益I, 增益Q]
        int maxIdx = ChannelFactory.getMaxChIdx();  // 获取最大通道索引
        for(int i=0;i<maxIdx;i++) {  // 循环：遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取动态通道对象
            double vScaleVal = channel.getVScaleVal()/channel.getProbeRate();  // 计算实际档位值（考虑探头衰减）
            cabteRegister.calc_pga_fs_gain(channel.getChId(), vScaleVal,result);  // 计算PGA、满量程和增益值
            vol[i] = result[0] & 0xFFFF;  // 提取PGA值（低16位）
        }  // 循环结束
        setChPgaGain(vol);  // 设置所有通道的PGA增益
    }  // 方法结束

    /**
     * 设置ADC增益
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setAdGain方法</li>
     *   <li>根据通道采样状态计算ADC满量程值</li>
     *   <li>写入ADC增益配置到FPGA</li>
     * </ul>
     * 
     * <p><b>计算流程：</b>
     * <pre>
     * 1. 获取采样通道状态
     * 2. 根据采样通道数量选择计算方式
     *    - 4通道：相邻通道互不影响
     *    - 其他：所有通道独立计算
     * 3. 计算每个通道的满量程值
     * 4. 写入ADC增益配置到FPGA
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    @Override  // 覆盖父类方法
    public void setAdGain(int fpgaIdx) {  // 设置ADC增益方法
        boolean[] sle = new boolean[ChannelFactory.CH_CNT];  // 通道采样状态数组
        Scope scope = Scope.getInstance();  // 获取示波器实例
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true), sle);  // 获取采样通道数量和状态
        int[] fullscale_i = {0, 0, 0, 0};  // I通道满量程值数组
        int[] fullscale_q = {0, 0, 0, 0};  // Q通道满量程值数组
        int[] result = {0, 0, 0, 0,0};  // 结果数组：[PGA, 满量程I, 满量程Q, 增益I, 增益Q]
        CabteRegister pCabReg = CabteRegister.getInstance();  // 获取校准寄存器实例
        FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令实例
        Channel channel;  // 通道对象，用于获取通道参数
        float[] gain_bc = {1, 1, 1, 1};  // 增益补偿数组，默认为1
        int adcMaxChNums = HwConfig.getInstance().getAdcMaxChNums();  // 获取ADC最大通道数
        int beginIdx = FPGACommand.beginChIdx(fpgaIdx);  // 获取FPGA对应的起始通道索引
        int endIdx = FPGACommand.endChIdx(fpgaIdx);  // 获取FPGA对应的结束通道索引
        switch (cnt) {  // 根据采样通道数量选择计算方式
            case 4:  // 4通道模式
                for (int i = beginIdx; i < endIdx; i += 2) {  // 循环：每隔2个通道处理一次（处理通道对）
                    channel = ChannelFactory.getDynamicChannel(i);  // 获取偶数通道对象
                    int idx = (i - beginIdx) / 2;  // 计算索引
                    if (sle[i] && sle[i + 1]) {  // 判断：两个通道都在采样
                        pCabReg.calc_pga_fs_gain(channel.getChId(),channel.getVScaleVal()/channel.getProbeRate(), result);  // 计算偶数通道的PGA和满量程
                        fullscale_i[idx] = result[1];  // 保存I通道满量程值

                        channel = ChannelFactory.getDynamicChannel(i + 1);  // 获取奇数通道对象
                        pCabReg.calc_pga_fs_gain(channel.getChId(),channel.getVScaleVal()/channel.getProbeRate(), result);  // 计算奇数通道的PGA和满量程
                        fullscale_q[idx] = result[1];  // 保存Q通道满量程值

                    } else {  // 否则：只有一个通道在采样
                        if (sle[i]) {  // 判断：偶数通道在采样
                            pCabReg.calc_pga_fs_gain(channel.getChId(),channel.getVScaleVal()/channel.getProbeRate(), result);  // 计算偶数通道的PGA和满量程
                            fullscale_i[idx] = result[2];  // 保存I通道满量程值（单通道模式）
                            fullscale_q[idx] = result[3];  // 保存Q通道满量程值（单通道模式）
                        } else {  // 否则：奇数通道在采样
                            channel = ChannelFactory.getDynamicChannel(i + 1);  // 获取奇数通道对象
                            pCabReg.calc_pga_fs_gain(channel.getChId(),channel.getVScaleVal()/channel.getProbeRate(), result);  // 计算奇数通道的PGA和满量程
                            fullscale_i[idx] = result[2];  // 保存I通道满量程值（单通道模式）
                            fullscale_q[idx] = result[3];  // 保存Q通道满量程值（单通道模式）
                        }  // 判断结束
                    }  // 判断结束


                }  // 循环结束
                break;  // 跳出switch
            default:  // 默认模式（非4通道）
                for (int i = beginIdx; i < endIdx; i += 2) {  // 循环：每隔2个通道处理一次
                    channel = ChannelFactory.getDynamicChannel(i);  // 获取偶数通道对象
                    int idx = (i - beginIdx)/2;  // 计算索引
                    pCabReg.calc_pga_fs_gain(channel.getChId(),channel.getVScaleVal()/channel.getProbeRate(), result);  // 计算偶数通道的PGA和满量程
                    fullscale_i[idx] = result[1];  // 保存I通道满量程值

                    channel = ChannelFactory.getDynamicChannel(i + 1);  // 获取奇数通道对象
                    pCabReg.calc_pga_fs_gain(channel.getChId(),channel.getVScaleVal()/channel.getProbeRate(), result);  // 计算奇数通道的PGA和满量程
                    fullscale_q[idx] = result[1];  // 保存Q通道满量程值
                }  // 循环结束
                break;  // 跳出switch
        }  // switch结束

        for (int i = beginIdx; i < endIdx; i += 2) {  // 循环：遍历通道对
            int idx = (i - beginIdx) / 2;  // 计算索引
            fpgaCommand.writeAD_gain(fpgaIdx,idx, 0, fullscale_i[idx]);  // 写入I通道ADC增益
            fpgaCommand.writeAD_gain(fpgaIdx,idx , 1, fullscale_q[idx]);  // 写入Q通道ADC增益
            fpgaCommand.sendFpga_gain_bc(fpgaIdx,gain_bc);  // 发送增益补偿
            fpgaCommand.cmdDevice(50);  // 发送设备命令50，确认ADC配置
        }  // 循环结束

    }  // 方法结束



    /**
     * 改变通道电压档位
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的changeChVolScale方法</li>
     *   <li>根据通道档位设置移位寄存器值</li>
     *   <li>发送移位寄存器命令到FPGA</li>
     * </ul>
     * 
     * <p><b>档位映射：</b>
     * <pre>
     * RATIO_DANG_1: 清除bit7和bit2
     * RATIO_DANG_2: 清除bit7和bit3
     * RATIO_DANG_3: 清除bit6和bit2
     * RATIO_DANG_4: 清除bit6和bit3
     * </pre>
     * 
     * <p><b>移位寄存器格式：</b>
     * <pre>
     * 每个通道占用8位
     * bit0: 通道使能（AC耦合时清除）
     * bit2-3: 档位选择低位
     * bit6-7: 档位选择高位
     * </pre>
     */
    @Override  // 覆盖父类方法
    public void changeChVolScale() {  // 改变通道电压档位方法

        long marsk= SHIFTREG_INIT_VAL;  // 初始化掩码为移位寄存器初始值
        Scope scope = Scope.getInstance();  // 获取示波器实例

        Channel channel;  // 通道对象，用于获取通道参数
        int dang = 0;  // 档位索引
        int idx = 0;  // 通道索引
        int chMax = scope.getChNum();  // 获取通道数量
        for(int i=0;i<chMax;i++)  // 循环：遍历所有通道
        {  // 循环体开始
            idx = i;  // 保存通道索引
            channel = ChannelFactory.getDynamicChannel(i);  // 获取动态通道对象

            dang = CabteRegister.getRatioIdx(channel.getResistanceType(),channel.getVScaleVal()/channel.getProbeRate());  // 获取档位索引

            idx = ChannelFactory.CH8 - idx;  // 反转通道索引（从CH8开始）

            switch (dang){  // 根据档位设置移位寄存器位
                default:  // 默认情况
                case HW.RATIO_DANG_1:  // 档位1：10mV/div
                    marsk &= ~(1L << (idx * 8 + 7));  // 清除bit7
                    marsk &= ~(1L << (idx * 8 + 2));  // 清除bit2
                    break;  // 跳出switch
                case HW.RATIO_DANG_2:  // 档位2：100mV/div

                    marsk &= ~(1L << (idx * 8 + 7));  // 清除bit7
                    marsk &= ~(1L << (idx * 8 + 3));  // 清除bit3
                    break;  // 跳出switch
                case HW.RATIO_DANG_3:  // 档位3：500mV/div
                    marsk &= ~(1L << (idx * 8 + 6));  // 清除bit6
                    marsk &= ~(1L << (idx * 8 + 2));  // 清除bit2
                    break;  // 跳出switch
                case HW.RATIO_DANG_4:  // 档位4：5V/div
                    marsk &= ~(1L << (idx * 8 + 6));  // 清除bit6
                    marsk &= ~(1L << (idx * 8 + 3));  // 清除bit3
                    break;  // 跳出switch
            }  // switch结束
        }  // 循环结束
        long temp=ChVolScaleShiftRegister();  // 获取通道电压档位移位寄存器值
        sendShiftRegister((temp & marsk));  // 发送移位寄存器值（应用掩码）
        usleep(2400);  // 延时2400微秒，等待硬件稳定
        sendShiftRegister(temp);  // 恢复移位寄存器值
    }  // 方法结束


    /**
     * 获取通道电压档位移位寄存器值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算通道电压档位对应的移位寄存器值</li>
     *   <li>根据通道采样状态和耦合类型设置bit0</li>
     *   <li>AC耦合或未采样的通道清除bit0</li>
     * </ul>
     * 
     * @return 移位寄存器值
     */
    private long ChVolScaleShiftRegister(){  // 获取通道电压档位移位寄存器值方法
        long iTmpValue = SHIFTREG_INIT_VAL;  // 初始化为移位寄存器初始值

        Scope scope = Scope.getInstance();  // 获取示波器实例
        int dang = 0;  // 档位索引（未使用）
        int chMax = scope.getChNum();  // 获取通道数量
        int idx = 0;  // 通道索引
        for(int i=0;i<chMax;i++){  // 循环：遍历所有通道
            idx =  i;  // 保存通道索引

            Channel channel = ChannelFactory.getDynamicChannel(i);  // 获取动态通道对象
            if(!scope.isChannelInSample(i)  // 判断：通道是否未在采样
                    || channel.getCoupleType() == Channel.COUPLE_TYPE_AC){  // 判断：通道是否为AC耦合
                idx = ChannelFactory.CH8 - idx;  // 反转通道索引
                iTmpValue &= ~(1L << (idx * 8 + 0));  // 清除bit0（禁用通道）
            }  // 判断结束
        }  // 循环结束
        return iTmpValue;  // 返回移位寄存器值
    }  // 方法结束




    //校准-----------------------------------------------------------------------------------------

    /**
     * MHO38 V1档位数量常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义MHO38 V1示波器的档位总数</li>
     *   <li>值为4（档位1-4）</li>
     * </ul>
     */
    public static final int MHO38V1_RATIO_DANG_CNT = RATIO_DANG_4 + 1;  // 档位数量：4

    /**
     * 校准状态索引常量定义
     * 
     * <p><b>索引分配：</b>
     * <pre>
     * CALIBRATION_TOP_ZERO (0): 零点校准状态
     * CALIBRATION_CENTER_CHxGAIN (1-64): 通道增益校准状态（每通道8个）
     * CHOFFSET_CALIBRATE_CHx (65-128): 偏移量校准状态（每通道8个）
     * CHCAP_CALIBRATE_CHx (129-136): 电容校准状态（每通道1个）
     * </pre>
     */
    public static final int CALIBRATION_TOP_ZERO = 0;  // 零点校准状态索引：0
    public static final int CALIBRATION_CENTER_CH1GAIN = CALIBRATION_TOP_ZERO + 1;  // CH1增益校准起始索引：1
    public static final int CALIBRATION_CENTER_CH2GAIN = CALIBRATION_CENTER_CH1GAIN + 8;  // CH2增益校准起始索引：9
    public static final int CALIBRATION_CENTER_CH3GAIN = CALIBRATION_CENTER_CH2GAIN + 8;  // CH3增益校准起始索引：17
    public static final int CALIBRATION_CENTER_CH4GAIN = CALIBRATION_CENTER_CH3GAIN + 8;  // CH4增益校准起始索引：25
    public static final int CALIBRATION_CENTER_CH5GAIN = CALIBRATION_CENTER_CH4GAIN + 8;  // CH5增益校准起始索引：33
    public static final int CALIBRATION_CENTER_CH6GAIN = CALIBRATION_CENTER_CH5GAIN + 8;  // CH6增益校准起始索引：41
    public static final int CALIBRATION_CENTER_CH7GAIN = CALIBRATION_CENTER_CH6GAIN + 8;  // CH7增益校准起始索引：49
    public static final int CALIBRATION_CENTER_CH8GAIN = CALIBRATION_CENTER_CH7GAIN + 8;  // CH8增益校准起始索引：57
    public static final int CHOFFSET_CALIBRATE_CH1 = CALIBRATION_CENTER_CH8GAIN + 8;  // CH1偏移量校准起始索引：65
    public static final int CHOFFSET_CALIBRATE_CH2 = CHOFFSET_CALIBRATE_CH1 + 8;  // CH2偏移量校准起始索引：73
    public static final int CHOFFSET_CALIBRATE_CH3 = CHOFFSET_CALIBRATE_CH2 + 8;  // CH3偏移量校准起始索引：81
    public static final int CHOFFSET_CALIBRATE_CH4 = CHOFFSET_CALIBRATE_CH3 + 8;  // CH4偏移量校准起始索引：89
    public static final int CHOFFSET_CALIBRATE_CH5 = CHOFFSET_CALIBRATE_CH4 + 8;  // CH5偏移量校准起始索引：97
    public static final int CHOFFSET_CALIBRATE_CH6 = CHOFFSET_CALIBRATE_CH5 + 8;  // CH6偏移量校准起始索引：105
    public static final int CHOFFSET_CALIBRATE_CH7 = CHOFFSET_CALIBRATE_CH6 + 8;  // CH7偏移量校准起始索引：113
    public static final int CHOFFSET_CALIBRATE_CH8 = CHOFFSET_CALIBRATE_CH7 + 8;  // CH8偏移量校准起始索引：121
    public static final int CHCAP_CALIBRATE_CH1 = CHOFFSET_CALIBRATE_CH8 + 8;  // CH1电容校准索引：129
    public static final int CHCAP_CALIBRATE_CH2 = CHCAP_CALIBRATE_CH1 + 1;  // CH2电容校准索引：130
    public static final int CHCAP_CALIBRATE_CH3 = CHCAP_CALIBRATE_CH2 + 1;  // CH3电容校准索引：131
    public static final int CHCAP_CALIBRATE_CH4 = CHCAP_CALIBRATE_CH3 + 1;  // CH4电容校准索引：132
    public static final int CHCAP_CALIBRATE_CH5 = CHCAP_CALIBRATE_CH4 + 1;  // CH5电容校准索引：133
    public static final int CHCAP_CALIBRATE_CH6 = CHCAP_CALIBRATE_CH5 + 1;  // CH6电容校准索引：134
    public static final int CHCAP_CALIBRATE_CH7 = CHCAP_CALIBRATE_CH6 + 1;  // CH7电容校准索引：135
    public static final int CHCAP_CALIBRATE_CH8 = CHCAP_CALIBRATE_CH7 + 1;  // CH8电容校准索引：136
    public static final int CALIBRATE_STATE_MAX = CHCAP_CALIBRATE_CH8 + 1;  // 校准状态最大索引：137

    /**
     * Flash存储最大容量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义校准数据存储的最大Flash空间</li>
     *   <li>值为100KB</li>
     * </ul>
     */
    public static final int FLASH_MAX = 100 * 1024;  // Flash最大容量：100KB

    /**
     * 校准数据最大尺寸
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义校准数据的最大存储空间</li>
     *   <li>值为30KB</li>
     * </ul>
     */
    private static final int MAX_COFIT_SIZE = 30 * 1024;  // 校准数据最大尺寸：30KB


    /**
     * 校准状态字节数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储所有校准项目的完成状态</li>
     *   <li>每个bit表示一个校准项目的状态</li>
     *   <li>长度为CALIBRATE_STATE_MAX/8 + 1字节</li>
     * </ul>
     */
    byte [] calibartionState = new byte[CALIBRATE_STATE_MAX/8 + 1];  // 校准状态字节数组

    /**
     * 获取校准数据最大尺寸
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getMaxCofitSize方法</li>
     *   <li>返回校准数据的最大存储空间</li>
     * </ul>
     * 
     * @return 校准数据最大尺寸（30KB）
     */
    @Override  // 覆盖父类方法
    protected int getMaxCofitSize() {  // 获取校准数据最大尺寸方法
        return MAX_COFIT_SIZE;  // 返回校准数据最大尺寸
    }  // 方法结束

    /**
     * 获取已使用的Flash最大地址
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getUseFlashMax方法</li>
     *   <li>计算校准数据占用的最大Flash地址</li>
     * </ul>
     * 
     * @return 已使用的Flash最大地址
     */
    @Override  // 覆盖父类方法
    protected int getUseFlashMax() {  // 获取已使用的Flash最大地址方法
        return getCalibrateStateAddr() +  CALIBRATE_STATE_MAX/8 + 1;  // 返回校准状态起始地址 + 状态数组长度
    }  // 方法结束

    /**
     * 获取Flash最大容量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getFlashMax方法</li>
     *   <li>返回Flash存储的最大容量</li>
     * </ul>
     * 
     * @return Flash最大容量（100KB）
     */
    @Override  // 覆盖父类方法
    protected int getFlashMax() {  // 获取Flash最大容量方法
        return FLASH_MAX;  // 返回Flash最大容量
    }  // 方法结束

    /**
     * 清除校准状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的clearCalibrationState方法</li>
     *   <li>将所有校准状态位清零</li>
     *   <li>写入Flash存储</li>
     * </ul>
     */
    @Override  // 覆盖父类方法
    public void clearCalibrationState() {  // 清除校准状态方法
        Arrays.fill(calibartionState, (byte) 0);  // 将校准状态数组所有字节清零
        write(getCalibrateStateAddr(),calibartionState);  // 写入Flash存储
    }  // 方法结束

    /**
     * 保存校准状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的saveCalibrationState方法</li>
     *   <li>将校准状态数组写入Flash存储</li>
     * </ul>
     */
    @Override  // 覆盖父类方法
    public void saveCalibrationState() {  // 保存校准状态方法
        write(getCalibrateStateAddr(),calibartionState);  // 将校准状态数组写入Flash存储
    }  // 方法结束

    /**
     * 设置校准状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setCalibrationState方法</li>
     *   <li>设置指定索引的校准状态位</li>
     *   <li>使用位操作设置或清除对应位</li>
     * </ul>
     * 
     * <p><b>位操作说明：</b>
     * <pre>
     * 字节索引：idx / 8
     * 位索引：idx % 8
     * 设置位：byte |= (1 << bit)
     * 清除位：byte &= ~(1 << bit)
     * </pre>
     * 
     * @param idx 校准状态索引
     * @param bState 状态值，true表示已完成，false表示未完成
     */
    @Override  // 覆盖父类方法
    public void setCalibrationState(int idx, boolean bState) {  // 设置校准状态方法
        if(idx >=CALIBRATION_TOP_ZERO && idx < CALIBRATE_STATE_MAX){  // 判断：索引是否在有效范围内
            int i = idx / 8;  // 计算字节索引
            idx = idx % 8;  // 计算位索引
            if(bState){  // 判断：是否设置状态为true
                calibartionState[i] |= (1 << idx);  // 设置对应位为1
            }else{  // 否则：设置状态为false
                calibartionState[i] &= ~(1 << idx);  // 清除对应位为0
            }  // 判断结束
        }  // 判断结束
    }  // 方法结束
    
    /**
     * 获取校准状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的isCalibrationState方法</li>
     *   <li>查询指定索引的校准状态位</li>
     *   <li>使用位操作读取对应位</li>
     * </ul>
     * 
     * @param idx 校准状态索引
     * @return true表示已完成，false表示未完成或索引无效
     */
    @Override  // 覆盖父类方法
    public boolean isCalibrationState(int idx){  // 获取校准状态方法
        if(idx >=CALIBRATION_TOP_ZERO && idx < CALIBRATE_STATE_MAX){  // 判断：索引是否在有效范围内
            int i = idx / 8;  // 计算字节索引
            idx = idx % 8;  // 计算位索引
            return (calibartionState[i] & (1 << idx)) != 0;  // 返回对应位的值
        }  // 判断结束
        return false;  // 索引无效，返回false
    }  // 方法结束
    
    /**
     * 加载校准状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的loadCalibrationState方法</li>
     *   <li>从Flash存储读取校准状态数组</li>
     * </ul>
     */
    @Override  // 覆盖父类方法
    public void loadCalibrationState(){  // 加载校准状态方法
        read(getCalibrateStateAddr(),calibartionState);  // 从Flash存储读取校准状态数组
    }  // 方法结束

    /**
     * 判断是否完成顶层校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的isTopCalibration方法</li>
     *   <li>查询零点校准状态</li>
     * </ul>
     * 
     * @return true表示零点校准已完成，false表示未完成
     */
    @Override  // 覆盖父类方法
    public boolean isTopCalibration(){  // 判断是否完成顶层校准方法
        return isCalibrationState(CALIBRATION_TOP_ZERO);  // 返回零点校准状态
    }  // 方法结束
    
    /**
     * 获取校准项目状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getCalibrationItemState方法</li>
     *   <li>根据索引查询对应校准项目的完成状态</li>
     *   <li>生成未完成项目的详细信息</li>
     * </ul>
     * 
     * <p><b>项目索引映射：</b>
     * <pre>
     * 0: 零点校准
     * 1: 通道增益校准
     * 2: 偏移量校准
     * 3: 电容校准
     * </pre>
     * 
     * @param idx 校准项目索引
     * @param sb 字符串构建器，用于存储未完成项目信息
     * @return true表示该项目所有子项已完成，false表示有未完成子项
     */
    @Override  // 覆盖父类方法
    public boolean getCalibrationItemState(int idx,StringBuilder sb){  // 获取校准项目状态方法
        switch (idx){  // 根据索引查询对应校准项目
            case 0:  // 零点校准
                return isTopCalibration();  // 返回零点校准状态
            case 1:  // 通道增益校准
                return getChGain(sb);  // 返回通道增益校准状态
            case 2:  // 偏移量校准
                return getChOffset(sb);  // 返回偏移量校准状态
            case 3:  // 电容校准
                return getChCap(sb);  // 返回电容校准状态
        }  // switch结束
        return false;  // 索引无效，返回false
    }  // 方法结束
    
    /**
     * 获取电容校准状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>遍历所有通道的电容校准状态</li>
     *   <li>生成未完成通道的详细信息</li>
     * </ul>
     * 
     * @param sb 字符串构建器，用于存储未完成通道信息
     * @return true表示所有通道电容校准已完成，false表示有未完成通道
     */
    private boolean getChCap(StringBuilder sb) {  // 获取电容校准状态方法
        boolean bit = true;  // 完成标志，初始为true
        boolean bEnter = false;  // 换行标志，初始为false
        for (int j = ChannelFactory.CH1; j < ChannelFactory.getMaxChIdx(); j++) {  // 循环：遍历所有通道

            boolean b1 = isCalibrationState(CHCAP_CALIBRATE_CH1 + j);  // 获取通道j的电容校准状态
            double v = VerticalAxis.getScaleIdValById(CabteRegister.getRatioIdx2Dang(Channel.RESISTANCE_1M,RATIO_DANG_3));  // 获取档位3的电压值
            if(!b1) {  // 判断：通道未完成电容校准
                sb.append("ch").append(j).append("[").append(v).append("]=").append("0").append("    ");  // 添加未完成通道信息
                bEnter = true;  // 设置换行标志
            }  // 判断结束
            bit = bit &&  b1;  // 更新完成标志
        }  // 循环结束
        if(bEnter) {  // 判断：是否有未完成通道
            sb.append("\n");  // 添加换行符
        }  // 判断结束
        return bit;  // 返回完成标志
    }  // 方法结束

    /**
     * 获取偏移量校准状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>遍历所有通道和档位的偏移量校准状态</li>
     *   <li>生成未完成项的详细信息</li>
     * </ul>
     * 
     * @param stringBuilder 字符串构建器，用于存储未完成项信息
     * @return true表示所有偏移量校准已完成，false表示有未完成项
     */
    private boolean getChOffset(StringBuilder stringBuilder) {  // 获取偏移量校准状态方法

        boolean bit = true;  // 完成标志，初始为true
        for (int j = ChannelFactory.CH1; j < ChannelFactory.getMaxChIdx(); j++) {  // 循环：遍历所有通道
            Channel channel = ChannelFactory.getDynamicChannel(j);  // 获取动态通道对象
            boolean bEnter = false;  // 换行标志，初始为false
            for (int i = 0; i < 4; i++) {  // 循环：遍历4个档位
                for(int k=0; k< 2; k++) {  // 循环：遍历2个方向（正负）
                    double v = VerticalAxis.getScaleIdValById(getRatioIdx2Dang(channel.getResistanceType(),i % getRatioDangCnt()));  // 获取档位电压值
                    boolean b1 = isCalibrationState(CHOFFSET_CALIBRATE_CH1 + j * 8 + i * 2 + k);  // 获取偏移量校准状态
                    if(k==0) v = -v;  // 第一个方向为负值
                    if(!b1) {  // 判断：该项未完成
                        stringBuilder.append("ch").append(j).append("[").append(v).append("]=").append("0").append("    ");  // 添加未完成项信息
                        bEnter = true;  // 设置换行标志
                    }  // 判断结束
                    bit = bit &&  b1;  // 更新完成标志
                }  // 循环结束：方向
            }  // 循环结束：档位
            if(bEnter) {  // 判断：是否有未完成项
                stringBuilder.append("\n");  // 添加换行符
            }  // 判断结束
        }  // 循环结束：通道
        return bit;  // 返回完成标志
    }  // 方法结束

    /**
     * 获取通道增益校准状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>遍历所有通道和输入幅度的增益校准状态</li>
     *   <li>生成未完成项的详细信息</li>
     * </ul>
     * 
     * @param sb 字符串构建器，用于存储未完成项信息
     * @return true表示所有增益校准已完成，false表示有未完成项
     */
    private boolean getChGain(StringBuilder sb){  // 获取通道增益校准状态方法

        boolean bit = true;  // 完成标志，初始为true
        for(int j = ChannelFactory.CH1;j<ChannelFactory.getMaxChIdx();j++){  // 循环：遍历所有通道
            boolean bEnter = false;  // 换行标志，初始为false
            for(int i=0;i<GAIN_INPUT_AMP.length;i++){  // 循环：遍历所有输入幅度
                boolean b1 = isCalibrationState(CALIBRATION_CENTER_CH1GAIN + j * GAIN_INPUT_AMP.length + i);  // 获取增益校准状态
                if(!b1) {  // 判断：该项未完成
                    sb.append("ch").append(j).append("[").append(GAIN_INPUT_AMP[i]).append("]=").append("0").append("   ");  // 添加未完成项信息
                    bEnter = true;  // 设置换行标志
                }  // 判断结束
                bit = bit && b1;  // 更新完成标志
            }  // 循环结束：输入幅度
            if(bEnter) {  // 判断：是否有未完成项
                sb.append("\n");  // 添加换行符
            }  // 判断结束
        }  // 循环结束：通道
        return bit;  // 返回完成标志
    }  // 方法结束

    /**
     * 从ByteBuffer读取校准数据
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的ByteBuffer2cofit方法</li>
     *   <li>从ByteBuffer读取校准系数数据</li>
     *   <li>按顺序读取各种校准系数</li>
     * </ul>
     * 
     * <p><b>读取顺序：</b>
     * <pre>
     * 1. 通道系数（coefChannel）
     * 2. 通道零点（chZero）
     * 3. PGA步进dB（ch_pga_stepdb）
     * 4. 低增益高增益比（ch_lghg）
     * 5. ADC满量程A（ch_fs_a）
     * 6. ADC满量程D（ch_fs_d）
     * 7. 电容高值（chCapacitanceHigh）
     * 8. 校准时间字符串
     * </pre>
     * 
     * @param byteBuffer 字节缓冲区
     * @param idx 起始索引
     * @return 读取后的索引
     */
    @Override  // 覆盖父类方法
    protected int ByteBuffer2cofit(ByteBuffer byteBuffer, int idx) {  // 从ByteBuffer读取校准数据方法
        idx = getFloatCoef3(coefChannel, byteBuffer, idx);  // 读取通道系数（三维float数组）
        idx = getFloatCoef3(chZero, byteBuffer, idx);  // 读取通道零点（三维float数组）
        idx = getFloatCoef1(ch_pga_stepdb,byteBuffer,idx);  // 读取PGA步进dB（一维float数组）
        idx = getFloatCoef1(ch_lghg,byteBuffer,idx);  // 读取低增益高增益比（一维float数组）
        idx = getFloatCoef3(ch_fs_a, byteBuffer, idx);  // 读取ADC满量程A（三维float数组）
        idx = getFloatCoef3(ch_fs_d, byteBuffer, idx);  // 读取ADC满量程D（三维float数组）
        idx = getIntCoef1(chCapacitanceHigh,byteBuffer,idx);  // 读取电容高值（一维int数组）
        StringBuilder sb = new StringBuilder();  // 创建字符串构建器
        idx = getString(sb,byteBuffer,idx);  // 读取校准时间字符串
        Log.d(TAG,"ByteBuffer2cofit:" + sb);  // 输出调试日志
        cabteTime = sb.toString();  // 保存校准时间
        return idx;  // 返回读取后的索引
    }  // 方法结束


    /**
     * 将校准数据写入ByteBuffer
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的cofit2ByteBuffer方法</li>
     *   <li>将校准系数数据写入ByteBuffer</li>
     *   <li>按顺序写入各种校准系数</li>
     * </ul>
     * 
     * <p><b>写入顺序：</b>
     * <pre>
     * 1. 通道系数（coefChannel）
     * 2. 通道零点（chZero）
     * 3. PGA步进dB（ch_pga_stepdb）
     * 4. 低增益高增益比（ch_lghg）
     * 5. ADC满量程A（ch_fs_a）
     * 6. ADC满量程D（ch_fs_d）
     * 7. 电容高值（chCapacitanceHigh）
     * 8. 校准时间字符串（保存时写入）
     * </pre>
     * 
     * @param byteBuffer 字节缓冲区
     * @param idx 起始索引
     * @param bSave 是否保存标志，true表示保存时写入校准时间
     * @return 写入后的索引
     */
    @Override  // 覆盖父类方法
    protected int cofit2ByteBuffer(ByteBuffer byteBuffer, int idx,boolean bSave) {  // 将校准数据写入ByteBuffer方法

        idx = setFloatCoef3(coefChannel, byteBuffer, idx);  // 写入通道系数（三维float数组）
        idx = setFloatCoef3(chZero, byteBuffer, idx);  // 写入通道零点（三维float数组）
        idx = setFloatCoef1(ch_pga_stepdb,byteBuffer,idx);  // 写入PGA步进dB（一维float数组）
        idx = setFloatCoef1(ch_lghg,byteBuffer,idx);  // 写入低增益高增益比（一维float数组）
        idx = setFloatCoef3(ch_fs_a, byteBuffer, idx);  // 写入ADC满量程A（三维float数组）
        idx = setFloatCoef3(ch_fs_d, byteBuffer, idx);  // 写入ADC满量程D（三维float数组）
        idx = setIntCoef1(chCapacitanceHigh,byteBuffer,idx);  // 写入电容高值（一维int数组）
        if(bSave) {  // 判断：是否保存
            String s = getNowTime();  // 获取当前时间
            Log.d(TAG, "cofit2ByteBuffer:" + s);  // 输出调试日志
            idx = setString(getNowTime(), byteBuffer, idx);  // 写入校准时间字符串
        }  // 判断结束
        return idx;  // 返回写入后的索引
    }  // 方法结束

    /**
     * PGA步进dB默认值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>PGA每步的增益变化量</li>
     *   <li>值为2dB</li>
     * </ul>
     */
    private static final float pga_stepdb = 2;  // PGA步进dB：2dB
    
    /**
     * 低增益高增益比默认值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>低增益模式和高增益模式的增益比</li>
     *   <li>值为10</li>
     * </ul>
     */
    private static final float pga_lghg = 10;  // 低增益高增益比：10
    
    /**
     * ADC满量程A默认值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>每个档位的ADC满量程A值</li>
     *   <li>用于计算ADC满量程配置</li>
     * </ul>
     * 
     * <p><b>档位对应：</b>
     * <pre>
     * [0]: 128000 (档位1)
     * [1]: 32000 (档位2)
     * [2]: 2560 (档位3)
     * [3]: 640 (档位4)
     * </pre>
     */
    private final float [] fullscale_a = {  // ADC满量程A默认值数组
            128000,  // 档位1：128000
            32000,  // 档位2：32000
            2560,  // 档位3：2560
            640  // 档位4：640
    };  // 数组定义结束
    
    /**
     * ADC满量程D默认值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>每个档位的ADC满量程D值（电压值）</li>
     *   <li>用于计算ADC满量程配置</li>
     * </ul>
     * 
     * <p><b>档位对应：</b>
     * <pre>
     * [0]: 0.0102V (档位1)
     * [1]: 0.0408V (档位2)
     * [2]: 0.510V (档位3)
     * [3]: 2.04V (档位4)
     * </pre>
     */
    private final float [] fullscale_d = {  // ADC满量程D默认值数组
            0.0102f,  // 档位1：0.0102V
            0.0408f,  // 档位2：0.0408V
            0.510f,  // 档位3：0.510V
            2.04f,  // 档位4：2.04V
    };  // 数组定义结束
    
    /**
     * 低增益模式PGA值常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>LMH6518芯片低增益模式PGA值</li>
     *   <li>值为0x401</li>
     * </ul>
     */
    public static final int LG1 = 0x401;  // 低增益模式PGA值：0x401
    
    /**
     * 低增益模式PGA值常量（9步）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>LMH6518芯片低增益模式PGA值（9步）</li>
     *   <li>值为0x409</li>
     * </ul>
     */
    public static final int LG9 = 0x409;  // 低增益模式PGA值（9步）：0x409
    
    /**
     * 高增益模式PGA值常量（9步）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>LMH6518芯片高增益模式PGA值（9步）</li>
     *   <li>值为0x419</li>
     * </ul>
     */
    public static final int HG9 = 0x419;  // 高增益模式PGA值（9步）：0x419
    
    /**
     * 满量程电压值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>ADC满量程对应的电压值</li>
     *   <li>值为10.22V</li>
     * </ul>
     */
    private static final double FULL_VV = 10.22;  // 满量程电压：10.22V
    
    /**
     * 增益校准数据数组（40.8mV输入）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道在40.8mV输入时的PGA步进dB增益</li>
     *   <li>用于计算PGA步进dB系数</li>
     * </ul>
     */
    public static double [] gain_pga_stepdb_a40_8 = new double[ChannelFactory.CH_CNT];  // 增益数组：40.8mV输入
    
    /**
     * 增益校准数据数组（257mV输入）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道在257mV输入时的PGA步进dB增益</li>
     *   <li>用于计算PGA步进dB系数</li>
     * </ul>
     */
    public static double [] gain_pga_stepdb_a257 = new double[ChannelFactory.CH_CNT];  // 增益数组：257mV输入
    
    /**
     * 增益校准数据数组（25.7mV输入）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道在25.7mV输入时的低增益高增益比</li>
     *   <li>用于计算低增益高增益比系数</li>
     * </ul>
     */
    public static double [] gain_pga_lghg_a25_7 = new double[ChannelFactory.CH_CNT];  // 增益数组：25.7mV输入
    
    /**
     * ADC满量程增益数组（10.2mV输入）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个ADC在10.2mV输入时的增益</li>
     *   <li>用于计算ADC满量程系数</li>
     * </ul>
     */
    public static double [][] gain_fs_a10_2 = new double[ChannelFactory.CH_CNT][3];  // ADC满量程增益数组：10.2mV输入
    
    /**
     * ADC满量程增益数组（11.2mV输入）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个ADC在11.2mV输入时的增益</li>
     *   <li>用于计算ADC满量程系数</li>
     * </ul>
     */
    public static double [][] gain_fs_a11_2 = new double[ChannelFactory.CH_CNT][3];  // ADC满量程增益数组：11.2mV输入
    
    /**
     * ADC满量程增益数组（40.8mV输入）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个ADC在40.8mV输入时的增益</li>
     *   <li>用于计算ADC满量程系数</li>
     * </ul>
     */
    public static double [][] gain_fs_a40_8 = new double[ChannelFactory.CH_CNT][3];  // ADC满量程增益数组：40.8mV输入
    
    /**
     * ADC满量程增益数组（44.8mV输入）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个ADC在44.8mV输入时的增益</li>
     *   <li>用于计算ADC满量程系数</li>
     * </ul>
     */
    public static double [][] gain_fs_a44_8 = new double[ChannelFactory.CH_CNT][3];  // ADC满量程增益数组：44.8mV输入
    
    /**
     * ADC满量程增益数组（510mV输入）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个ADC在510mV输入时的增益</li>
     *   <li>用于计算ADC满量程系数</li>
     * </ul>
     */
    public static double [][] gain_fs_a510 = new double[ChannelFactory.CH_CNT][3];  // ADC满量程增益数组：510mV输入
    
    /**
     * ADC满量程增益数组（560mV输入）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个ADC在560mV输入时的增益</li>
     *   <li>用于计算ADC满量程系数</li>
     * </ul>
     */
    public static double [][] gain_fs_a560 = new double[ChannelFactory.CH_CNT][3];  // ADC满量程增益数组：560mV输入

    /**
     * 通道PGA步进dB系数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道的PGA步进dB系数</li>
     *   <li>用于计算PGA增益</li>
     * </ul>
     */
    private float [] ch_pga_stepdb = new float[ChannelFactory.CH_CNT];  // 通道PGA步进dB系数数组
    
    /**
     * 通道低增益高增益比系数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道的低增益高增益比系数</li>
     *   <li>用于计算PGA增益</li>
     * </ul>
     */
    private float [] ch_lghg = new float[ChannelFactory.CH_CNT];  // 通道低增益高增益比系数数组

    /**
     * 通道ADC满量程A系数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个档位每个ADC的满量程A系数</li>
     *   <li>用于计算ADC满量程配置</li>
     * </ul>
     * 
     * <p><b>数组维度：</b>
     * <pre>
     * 第一维：通道索引（0-7）
     * 第二维：档位索引（0-3）
     * 第三维：ADC索引（0-2）
     * </pre>
     */
    private float [][][] ch_fs_a = new float[ChannelFactory.CH_CNT][4][3];  // 通道ADC满量程A系数数组
    
    /**
     * 通道ADC满量程D系数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个档位每个ADC的满量程D系数</li>
     *   <li>用于计算ADC满量程配置</li>
     * </ul>
     * 
     * <p><b>数组维度：</b>
     * <pre>
     * 第一维：通道索引（0-7）
     * 第二维：档位索引（0-3）
     * 第三维：ADC索引（0-2）
     * </pre>
     */
    private float [][][] ch_fs_d = new float[ChannelFactory.CH_CNT][4][3];  // 通道ADC满量程D系数数组
    
    /**
     * 通道系数数组（偏移量系数）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个档位的偏移量系数</li>
     *   <li>用于计算通道偏移量</li>
     * </ul>
     * 
     * <p><b>数组维度：</b>
     * <pre>
     * 第一维：通道索引（0-7）
     * 第二维：档位索引（0-3）
     * 第三维：系数索引（0-1）
     * </pre>
     */
    private float [][][]coefChannel = new float[ChannelFactory.CH_CNT][4][2];  // 通道系数数组（偏移量系数）
    
    /**
     * 通道零点数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道每个档位每个PGA值的零点DA值</li>
     *   <li>用于零点校准</li>
     * </ul>
     * 
     * <p><b>数组维度：</b>
     * <pre>
     * 第一维：通道索引（0-7）
     * 第二维：档位索引（0-3）
     * 第三维：PGA索引（0-29）
     * </pre>
     */
    private float [][][]chZero = new float[ChannelFactory.CH_CNT][4][30];  // 通道零点数组
    
    /**
     * 通道电容高值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道的电容高值</li>
     *   <li>用于电容校准</li>
     * </ul>
     */
    private int [] chCapacitanceHigh = new int[ChannelFactory.CH_CNT] ;  // 通道电容高值数组


    /**
     * 输入幅度索引常量：40.8mV
     */
    public static final int GAIN_INPUT_AMP_40_8_MV = 0;  // 输入幅度索引：40.8mV
    
    /**
     * 输入幅度索引常量：257mV
     */
    public static final int GAIN_INPUT_AMP_257_MV = GAIN_INPUT_AMP_40_8_MV + 1;  // 输入幅度索引：257mV
    
    /**
     * 输入幅度索引常量：25.7mV
     */
    public static final int GAIN_INPUT_AMP_25_7_MV = GAIN_INPUT_AMP_257_MV + 1;  // 输入幅度索引：25.7mV
    
    /**
     * 输入幅度索引常量：10.2mV
     */
    public static final int GAIN_INPUT_AMP_10_2_MV = GAIN_INPUT_AMP_25_7_MV + 1;  // 输入幅度索引：10.2mV
    
    /**
     * 输入幅度索引常量：11.2mV
     */
    public static final int GAIN_INPUT_AMP_11_2_MV = GAIN_INPUT_AMP_10_2_MV + 1;  // 输入幅度索引：11.2mV
    
    /**
     * 输入幅度索引常量：44.8mV
     */
    public static final int GAIN_INPUT_AMP_44_8_MV = GAIN_INPUT_AMP_11_2_MV + 1;  // 输入幅度索引：44.8mV
    
    /**
     * 输入幅度索引常量：510mV
     */
    public static final int GAIN_INPUT_AMP_510_MV = GAIN_INPUT_AMP_44_8_MV + 1;  // 输入幅度索引：510mV
    
    /**
     * 输入幅度索引常量：560mV
     */
    public static final int GAIN_INPUT_AMP_560_MV = GAIN_INPUT_AMP_510_MV + 1;  // 输入幅度索引：560mV
    
    /**
     * DA通道偏移零点默认值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>DA芯片零点的默认值</li>
     *   <li>值为32886（接近中间值32768）</li>
     * </ul>
     */
    private static final float DA_CH_OFFSET_ZERO = 32886f;  // DA通道偏移零点：32886
    
    /**
     * 输入幅度值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义增益校准的输入幅度值</li>
     *   <li>单位：V</li>
     * </ul>
     * 
     * <p><b>幅度对应：</b>
     * <pre>
     * [0]: 0.0408V (40.8mV)
     * [1]: 0.257V (257mV)
     * [2]: 0.0257V (25.7mV)
     * [3]: 0.0102V (10.2mV)
     * [4]: 0.0112V (11.2mV)
     * [5]: 0.0448V (44.8mV)
     * [6]: 0.510V (510mV)
     * [7]: 0.560V (560mV)
     * </pre>
     */
    public static double [] GAIN_INPUT_AMP ={  // 输入幅度值数组
            0.0408,         //40.8mV
            0.257,          //257mV
            0.0257,         //25.7mV
            0.0102,         //10.2mV
            0.0112,         //11.2mV
            0.0448,         //44.8mV
            0.510,          //510mV
            0.560           //560mV
    };  // 数组定义结束
    
    /**
     * PGA代码数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义每个输入幅度对应的PGA代码</li>
     *   <li>每个幅度对应3个PGA值（不同档位）</li>
     * </ul>
     */
    public static int [] GAIN_PGA_CODE = {  // PGA代码数组
            0x204,0x210,0x21C,  // 40.8mV对应的PGA值
            //1
            0x21C,0x21C,0x21C,  // 257mV对应的PGA值
            //
            0x213,0x213,0x213,  // 25.7mV对应的PGA值
            //
            0x214,0x214,0x214,  // 10.2mV对应的PGA值
            //50
            0x21B,0x21B,0x21B,  // 11.2mV对应的PGA值
            //50
            0x21B,0x21B,0x21B,  // 44.8mV对应的PGA值
            //50
            0x217,0x217,0x217,  // 510mV对应的PGA值

    };  // 数组定义结束

    /**
     * ADC满量程代码数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义每个输入幅度对应的ADC满量程代码</li>
     *   <li>每个幅度对应3个满量程值（不同档位）</li>
     * </ul>
     */
    public static int [] GAIN_ADFS_CODE = {  // ADC满量程代码数组
            0xA000,0xA000,0xA000,  // 40.8mV对应的满量程值
            //1
            0x88F6,0xA000,0xB70A,  // 257mV对应的满量程值
            //
            0x88F6,0xA000,0xB70A,  // 25.7mV对应的满量程值
            //
            0x88F6,0xA000,0xB70A,  // 10.2mV对应的满量程值
            //50
            0x88F6,0xA000,0xB70A,  // 11.2mV对应的满量程值
            //50
            0x88F6,0xA000,0xB70A,  // 44.8mV对应的满量程值
            //50
            0x88F6,0xA000,0xB70A,  // 510mV对应的满量程值
    };  // 数组定义结束

    /**
     * 增益dB数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义不同输入幅度的增益dB值</li>
     * </ul>
     */
    public static int [] GAIN_DB = {-2,7,6,-1,-1,3};  // 增益dB数组


    /**
     * 1MΩ阻抗档位值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义1MΩ阻抗下各档位的电压值</li>
     *   <li>单位：V/div</li>
     * </ul>
     * 
     * <p><b>档位对应：</b>
     * <pre>
     * [0]: 0.07V (70mV/div)
     * [1]: 0.2V (200mV/div)
     * [2]: 4V (4V/div)
     * [3]: 10V (10V/div)
     * </pre>
     */
    public static double [] RATIO_DANG_VAL = {0.07,0.2,4,10};  // 1MΩ档位值数组
    
    /**
     * 50Ω阻抗档位值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义50Ω阻抗下各档位的电压值</li>
     *   <li>单位：V/div</li>
     *   <li>当前版本与1MΩ相同</li>
     * </ul>
     */
    public static double [] RATIO_DANG_VAL_50 = {0.07,0.2,4,10};  // 50Ω档位值数组
    
    /**
     * 获取档位数量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getRatioDangCnt方法</li>
     *   <li>返回MHO38 V1的档位总数</li>
     * </ul>
     * 
     * @return 档位数量（4）
     */
    @Override  // 覆盖父类方法
    public int getRatioDangCnt() {  // 获取档位数量方法
        return MHO38V1_RATIO_DANG_CNT;  // 返回档位数量：4
    }  // 方法结束

    /**
     * 设置默认值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的defaultVal方法</li>
     *   <li>初始化所有校准系数为默认值</li>
     * </ul>
     * 
     * <p><b>初始化内容：</b>
     * <pre>
     * 1. ADC满量程默认值
     * 2. 通道系数默认值
     * 3. 通道零点默认值
     * 4. 电容高值默认值
     * </pre>
     */
    @Override  // 覆盖父类方法
    protected void defaultVal() {  // 设置默认值方法

        defaultVal_AdFullscale();  // 初始化ADC满量程默认值
        defaultVal_coefChannel();  // 初始化通道系数默认值
        defaultVal_chZero();  // 初始化通道零点默认值
        defaultVal_ChCapacitance();  // 初始化电容高值默认值
    }  // 方法结束

    /**
     * 初始化ADC满量程默认值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置所有通道的ADC满量程系数为默认值</li>
     *   <li>包括PGA步进dB、低增益高增益比、满量程A和D</li>
     * </ul>
     */
    public void defaultVal_AdFullscale(){  // 初始化ADC满量程默认值方法
        for(int i=0;i<ch_pga_stepdb.length;i++){  // 循环：遍历所有通道
            ch_pga_stepdb[i] = pga_stepdb;  // 设置PGA步进dB默认值
            ch_lghg[i] = pga_lghg;  // 设置低增益高增益比默认值
            for(int j=0;j<4;j++) {  // 循环：遍历所有档位
                for(int k=0;k<3;k++) {  // 循环：遍历所有ADC
                    ch_fs_a[i][j][k] = fullscale_a[j];  // 设置满量程A默认值
                    ch_fs_d[i][j][k] = fullscale_d[j];  // 设置满量程D默认值
                }  // 循环结束：ADC
            }  // 循环结束：档位
        }  // 循环结束：通道

    }  // 方法结束
    
    /**
     * 初始化通道系数默认值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置所有通道所有档位的偏移量系数为默认值</li>
     *   <li>从HwConfig获取默认系数</li>
     * </ul>
     */
    private void defaultVal_coefChannel(){  // 初始化通道系数默认值方法

        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置实例
        for(int i=0;i<ChannelFactory.CH_CNT;i++){  // 循环：遍历所有通道
            for(int j=0;j<4;j++){  // 循环：遍历所有档位
                coefChannel[i][j][0] = hwConfig.getChCoefDefault()[getRatioIdx2Dang(Channel.RESISTANCE_1M,j)];  // 设置系数0默认值
                coefChannel[i][j][1] = hwConfig.getChCoefDefault()[getRatioIdx2Dang(Channel.RESISTANCE_1M,j)];  // 设置系数1默认值
            }  // 循环结束：档位
        }  // 循环结束：通道

    }  // 方法结束

    /**
     * 初始化通道零点默认值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置所有通道所有档位所有PGA值的零点为默认值</li>
     *   <li>默认值为DA_CH_OFFSET_ZERO（32886）</li>
     * </ul>
     */
    private void defaultVal_chZero(){  // 初始化通道零点默认值方法
        for(int i = 0; i< 30; i++){  // 循环：遍历所有PGA索引
            for(int j=0; j<ChannelFactory.CH_CNT; j++){  // 循环：遍历所有通道
                chZero[j][0][i] = DA_CH_OFFSET_ZERO;  // 设置档位1零点默认值
                chZero[j][1][i] = DA_CH_OFFSET_ZERO;  // 设置档位2零点默认值
                chZero[j][2][i] = DA_CH_OFFSET_ZERO;  // 设置档位3零点默认值
                chZero[j][3][i] = DA_CH_OFFSET_ZERO;  // 设置档位4零点默认值
            }  // 循环结束：通道
        }  // 循环结束：PGA索引
    }  // 方法结束

    /**
     * 初始化电容高值默认值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置所有通道的电容高值为默认值</li>
     *   <li>默认值为0xFFFF的一半（32767）</li>
     * </ul>
     */
    private void defaultVal_ChCapacitance(){  // 初始化电容高值默认值方法
        int val = 0xFFFF;  // 初始化为最大值
        val /= 2;  // 除以2，得到中间值
        Arrays.fill(chCapacitanceHigh, val);  // 填充所有通道的电容高值
    }  // 方法结束

    /**
     * 初始化指定通道档位的系数默认值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置指定通道指定档位的偏移量系数为默认值</li>
     *   <li>从vol_ChannelCoef_default获取默认值</li>
     * </ul>
     * 
     * @param ch 通道索引
     * @param dang 档位索引
     * @param resistanceType 阻抗类型（未使用，固定为1MΩ）
     */
    public void defaultVal_coefChannel(int ch,int dang,int resistanceType){  // 初始化指定通道档位系数默认值方法
        coefChannel[ch][dang][1] = coefChannel[ch][dang][0] = (float) vol_ChannelCoef_default(Channel.RESISTANCE_1M, dang);  // 设置系数0和1为默认值
    }  // 方法结束

    /**
     * 计算LMH6518增益
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据PGA值计算LMH6518芯片的增益</li>
     *   <li>支持低增益和高增益两种模式</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 低增益模式（bit4=0）：
     *   增益 = 10^((10 + 8.86 - (pga & 0xF) × 2) / 20)
     * 
     * 高增益模式（bit4=1）：
     *   增益 = 10^((30 + 8.86 - (pga & 0xF) × 2) / 20)
     * </pre>
     * 
     * @param pgaval PGA值
     * @return 增益值（线性）
     */
    private static double getLMH6518(int pgaval){  // 计算LMH6518增益方法
        double val = 10;  // 初始化基础增益值
        if((pgaval & 0x10) != 0){  // 判断：是否为高增益模式（bit4=1）
            val = 30;  // 高增益模式基础值
        }  // 判断结束
        val += 8.86 + (pgaval & 0xF) * (-2);  // 计算dB值：基础值 + 偏移 - 步进
        return Math.pow(10,val/20);  // 将dB转换为线性增益
    }  // 方法结束


    /**
     * 计算通道系数默认值（扩展版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据通道、档位和PGA值计算通道系数</li>
     *   <li>考虑档位衰减和PGA增益</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 档位1和3：v = 1
     * 档位2和4：v = 0.25
     * 
     * 系数 = (ADC满量程 × 65536) / (2.5 × 2 × v × PGA增益 × 1024)
     * </pre>
     * 
     * @param chIdx 通道索引
     * @param dangwei 档位ID
     * @param pgaVal PGA值
     * @return 通道系数
     */
    public double vol_ChannelCoef_defaultEx(int chIdx,int dangwei,int pgaVal){  // 计算通道系数默认值（扩展版本）方法
        double v = 1;  // 初始化衰减系数
        int dang = getRatioIdx(Channel.RESISTANCE_1M,VerticalAxis.getScaleIdValById(dangwei));  // 获取档位索引
        switch (dang){  // 根据档位设置衰减系数
            default:  // 默认情况
            case RATIO_DANG_1:  // 档位1
            case RATIO_DANG_3:  // 档位3
                v = 1;  // 无衰减
                break;  // 跳出switch
            case RATIO_DANG_2:  // 档位2
            case RATIO_DANG_4:  // 档位4
                v = 0.25;  // 衰减为1/4
                break;  // 跳出switch
        }  // switch结束
        v = v * getLMH6518(pgaVal);  // 乘以PGA增益
        return (HwConfig.getInstance().getAdFSR() * 65536) /( 2.5 * 2 * v * 1024);  // 计算并返回通道系数
    }  // 方法结束

    /**
     * 获取通道系数默认值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从HwConfig获取指定档位的默认通道系数</li>
     * </ul>
     * 
     * @param resistanceType 阻抗类型（未使用）
     * @param dang 档位索引
     * @return 通道系数，档位无效时返回0
     */
    public double vol_ChannelCoef_default(int resistanceType,int dang){  // 获取通道系数默认值方法
        float [] chCoef = HwConfig.getInstance().getChCoefDefault();  // 获取默认系数数组
        if(dang < chCoef.length) {  // 判断：档位索引是否有效
            return chCoef[dang];  // 返回对应档位的系数
        }  // 判断结束
        Log.e(TAG,"dangwei error:" + dang);  // 输出错误日志
        return 0;  // 档位无效，返回0
    }  // 方法结束
    
    /**
     * 根据电压值获取档位索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据电压值查找对应的档位索引</li>
     *   <li>返回第一个大于等于输入电压的档位索引</li>
     * </ul>
     * 
     * @param resistanceType 阻抗类型（未使用）
     * @param v 电压值（V/div）
     * @return 档位索引（0-3）
     */
    public int getRatioIdx(int resistanceType,double v){  // 根据电压值获取档位索引方法
        int i = 0;  // 初始化索引
        for(i=0;i<RATIO_DANG_VAL.length;i++){  // 循环：遍历所有档位
            if(v <= RATIO_DANG_VAL[i]){  // 判断：电压是否小于等于当前档位值
                break;  // 找到对应档位，跳出循环
            }  // 判断结束
        }  // 循环结束
        if(i >= RATIO_DANG_VAL.length){  // 判断：索引是否超出范围
            i = RATIO_DANG_VAL.length - 1;  // 限制为最大档位索引
        }  // 判断结束
        return i;  // 返回档位索引
    }  // 方法结束

    /**
     * 根据档位索引获取档位ID
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getRatioIdx2Dang方法</li>
     *   <li>将档位索引转换为VerticalAxis档位ID</li>
     * </ul>
     * 
     * <p><b>映射关系：</b>
     * <pre>
     * RATIO_DANG_1 → DANG_10mV
     * RATIO_DANG_2 → DANG_100mV
     * RATIO_DANG_3 → DANG_500mV
     * RATIO_DANG_4 → DANG_5V
     * </pre>
     * 
     * @param resistanceType 阻抗类型（未使用）
     * @param idx 档位索引
     * @return 档位ID
     */
    @Override  // 覆盖父类方法
    public int getRatioIdx2Dang(int resistanceType,int idx){  // 根据档位索引获取档位ID方法
        switch (idx){  // 根据档位索引返回档位ID
            default:  // 默认情况
            case RATIO_DANG_1:  // 档位1
                return  VerticalAxis.DANG_10mV;  // 返回10mV/div
            case RATIO_DANG_2:  // 档位2
                return  VerticalAxis.DANG_100mV;  // 返回100mV/div
            case RATIO_DANG_3:  // 档位3
                return  VerticalAxis.DANG_500mV;  // 返回500mV/div
            case RATIO_DANG_4:  // 档位4
                return  VerticalAxis.DANG_5V;  // 返回5V/div
        }  // switch结束
    }  // 方法结束
    
    /**
     * 获取垂直范围
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getVerticalRange方法</li>
     *   <li>根据档位返回垂直范围（满量程电压）</li>
     * </ul>
     * 
     * <p><b>范围映射：</b>
     * <pre>
     * 档位1和2：2.5V
     * 档位3和4：125V
     * </pre>
     * 
     * @param resistanceType 阻抗类型（未使用）
     * @param dang 档位索引
     * @return 垂直范围（V）
     */
    @Override  // 覆盖父类方法
    public double getVerticalRange(int resistanceType,int dang){  // 获取垂直范围方法
        double vRange = 2.5;  // 初始化垂直范围
        switch (dang){  // 根据档位设置垂直范围
            case RATIO_DANG_1:  // 档位1
            case RATIO_DANG_2:  // 档位2
                vRange = 2.5;  // 小档位范围：2.5V
                break;  // 跳出switch
            default:  // 默认情况
            case RATIO_DANG_3:  // 档位3
            case RATIO_DANG_4:  // 档位4
                vRange =  125;  // 大档位范围：125V
                break;  // 跳出switch
        }  // switch结束
        return vRange;  // 返回垂直范围
    }  // 方法结束

    /**
     * 获取通道系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getChannelCoef方法</li>
     *   <li>返回指定通道档位的偏移量系数</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param idx 系数索引（0或1）
     * @return 通道系数
     */
    @Override  // 覆盖父类方法
    public float getChannelCoef(int chIdx,int dang,int idx){  // 获取通道系数方法
        return coefChannel[chIdx][dang][idx];  // 返回指定通道档位的系数
    }  // 方法结束
    
    /**
     * 设置通道系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setChannelCoef方法</li>
     *   <li>设置指定通道档位的偏移量系数</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param idx 系数索引（0或1）
     * @param val 系数值
     */
    @Override  // 覆盖父类方法
    public void setChannelCoef(int chIdx,int dang,int idx,float val){  // 设置通道系数方法
        coefChannel[chIdx][dang][idx] = val;  // 设置指定通道档位的系数
    }  // 方法结束

    /**
     * 设置通道零点
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setChannelZero方法</li>
     *   <li>设置指定通道档位PGA的零点DA值</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param dwIdx 档位索引
     * @param pga PGA索引
     * @param val 零点DA值
     */
    @Override  // 覆盖父类方法
    public void setChannelZero(int chIdx,int dwIdx,int pga,float val){  // 设置通道零点方法
        chZero[chIdx][dwIdx][pga] = val;  // 设置指定通道档位PGA的零点
    }  // 方法结束

    /**
     * 获取通道零点
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getChannelZero方法</li>
     *   <li>返回指定通道档位PGA的零点DA值</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param dwIdx 档位索引
     * @param pga PGA索引
     * @return 零点DA值
     */
    @Override  // 覆盖父类方法
    public float getChannelZero(int chIdx,int dwIdx,int pga) {  // 获取通道零点方法
        return chZero[chIdx][dwIdx][pga];  // 返回指定通道档位PGA的零点
    }  // 方法结束

    /**
     * 获取通道电容高值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getChCapacitanceHigh方法</li>
     *   <li>返回指定通道的电容高值</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引（未使用）
     * @return 电容高值
     */
    @Override  // 覆盖父类方法
    public int getChCapacitanceHigh(int chIdx,int dang){  // 获取通道电容高值方法
        return chCapacitanceHigh[chIdx];  // 返回指定通道的电容高值
    }  // 方法结束

    /**
     * 设置通道电容高值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setChCapacitanceHigh方法</li>
     *   <li>设置指定通道的电容高值</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引（未使用）
     * @param val 电容高值
     */
    @Override  // 覆盖父类方法
    public void setChCapacitanceHigh(int chIdx,int dang ,int val){  // 设置通道电容高值方法
        chCapacitanceHigh[chIdx] = val;  // 设置指定通道的电容高值
    }  // 方法结束


    /**
     * 计算通道系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据通道和电压值计算通道系数</li>
     *   <li>用于偏移量计算</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 系数 = coefChannel[ch][档位][idx] × scaleVal / 档位电压
     * </pre>
     * 
     * @param chIdx 通道索引
     * @param scaleVal 电压值（V/div）
     * @param idx 系数索引（0或1）
     * @return 通道系数
     */
    public double calc_coefChannel(int chIdx,double scaleVal,int idx){  // 计算通道系数方法
        int resistanceType = Channel.RESISTANCE_1M;  // 阻抗类型固定为1MΩ
        int refScaleId = getRatioIdx(resistanceType,scaleVal);  // 获取档位索引
        double v = VerticalAxis.getScaleIdValById(getRatioIdx2Dang(resistanceType,refScaleId));  // 获取档位电压值
        v = (coefChannel[chIdx][refScaleId][idx] * scaleVal / v);  // 计算通道系数
        return v;  // 返回通道系数
    }  // 方法结束

    /**
     * 计算PGA、满量程和增益值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的calc_pga_fs_gain方法</li>
     *   <li>计算指定通道档位的PGA值和ADC满量程值</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param scaleVal 电压值（V/div）
     * @param result 结果数组：[PGA, 满量程0, 满量程1, 满量程2, ...]
     */
    @Override  // 覆盖父类方法
    public void calc_pga_fs_gain(int chIdx,double scaleVal,int []result){  // 计算PGA、满量程和增益值方法
        calc_pga_gain(0,chIdx,Channel.RESISTANCE_1M, scaleVal, result);  // 调用calc_pga_gain计算
        //result[1] = result[2] = result[3] = 16384;
    }  // 方法结束
    
    /**
     * 计算PGA增益
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的calc_pga_gain方法</li>
     *   <li>根据通道档位计算PGA值和ADC满量程值</li>
     * </ul>
     * 
     * <p><b>计算流程：</b>
     * <pre>
     * 1. 限制档位值在有效范围内
     * 2. 获取档位索引
     * 3. 获取满量程A和D系数
     * 4. 计算PGA步进值y
     * 5. 根据y值选择低增益或高增益模式
     * 6. 计算PGA值和增益系数k
     * 7. 计算每个ADC的满量程值
     * </pre>
     * 
     * <p><b>PGA计算公式：</b>
     * <pre>
     * 低增益模式：PGA = 0x400 + y
     * 高增益模式：PGA = 0x410 + y
     * 
     * y = 1 - round(20 × log10(D / scaleVal) / pga_stepdb)
     * </pre>
     * 
     * @param cnt 采样通道数（未使用）
     * @param chIdx 通道索引
     * @param resistanceType 阻抗类型（未使用）
     * @param scaleVal 电压值（V/div）
     * @param result 结果数组：[PGA, 满量程0, 满量程1, 满量程2]
     */
    @Override  // 覆盖父类方法
    public void calc_pga_gain(int cnt,int chIdx,int resistanceType,double scaleVal,int []result){  // 计算PGA增益方法

        scaleVal = VerticalAxis.clampMin(scaleVal,resistanceType);  // 限制档位值在有效范围内

        int pga = 0;  // 初始化PGA值
        int dang = getRatioIdx(resistanceType,scaleVal);  // 获取档位索引

        float a = ch_fs_a[chIdx][dang][0];  // 获取满量程A系数
        float d = ch_fs_d[chIdx][dang][0];  // 获取满量程D系数

        int y = (int) (1 - Math.round(20 * Math.log10(d / scaleVal) / ch_pga_stepdb[chIdx]));  // 计算PGA步进值

        if (y > 10) {  // 判断：步进值是否超过最大值
            y = 10;  // 限制为最大值
        }  // 判断结束

        double k;  // 增益系数
        if (y >= 0 && y <= 10) {  // 判断：是否在低增益模式范围
            pga = 0x400 + y;  // 低增益模式PGA值
            k = Math.pow(10, (1 - y) * ch_pga_stepdb[chIdx] / 20);  // 计算增益系数
        } else {  // 否则：使用高增益模式
            y = (int) (1 - Math.round(20 * Math.log10(d / ch_lghg[chIdx] / scaleVal) / ch_pga_stepdb[chIdx]));  // 重新计算步进值
            if (y > 10) {  // 判断：步进值是否超过最大值
                y = 10;  // 限制为最大值
            } else if (y < 0) {  // 判断：步进值是否小于最小值
                y = 0;  // 限制为最小值
            }  // 判断结束
            pga = 0x410 + y;  // 高增益模式PGA值
            k = Math.pow(10, (1 - y) * ch_pga_stepdb[chIdx] / 20) * ch_lghg[chIdx];  // 计算增益系数（考虑低增益高增益比）
        }  // 判断结束
        result[0] = pga;  // 保存PGA值
        for (int i = 0; i < 3; i++) {  // 循环：遍历3个ADC
            a = ch_fs_a[chIdx][dang][i];  // 获取当前ADC的满量程A系数
            d = ch_fs_d[chIdx][dang][i];  // 获取当前ADC的满量程D系数

            int yy = (int) Math.round(getAdFsDefault() + (scaleVal * k - d) * a);  // 计算ADC满量程值


            if (yy > 488) {  // 判断：满量程值是否过大（未处理）

            } else if (yy < 64) {  // 判断：满量程值是否过小（未处理）

            } else {  // 正常范围（未处理）

            }  // 判断结束

            if(yy < 0) {  // 判断：满量程值是否为负
                yy = 0;  // 限制为最小值
            }else if(yy > 0x7FFF){  // 判断：满量程值是否超过最大值
                yy = 0x7FFF;  // 限制为最大值
            }  // 判断结束
            result[i + 1] = yy;  // 保存ADC满量程值
        }  // 循环结束
    }  // 方法结束

    /**
     * 获取ADC满量程默认值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回ADC满量程的默认值</li>
     *   <li>值为16384</li>
     * </ul>
     * 
     * @return ADC满量程默认值
     */
    private int getAdFsDefault(){  // 获取ADC满量程默认值方法
        return 16384;  // 返回默认值：16384
    }  // 方法结束
    
    /**
     * 获取ADC最大值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回ADC的最大采样值</li>
     *   <li>10位ADC，最大值为1023</li>
     * </ul>
     * 
     * @return ADC最大值（1023）
     */
    private int getMaxAdVal(){  // 获取ADC最大值方法
        return 1023;  // 返回ADC最大值：1023
    }  // 方法结束


    /**
     * 设置ADC偏移
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置ADC偏移值</li>
     *   <li>当前为空实现</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @val 偏移值
     */
    public void setAdOffset(int chIdx,int chMode,int adIdx,int val){  // 设置ADC偏移方法（空实现）

    }  // 方法结束
    
    /**
     * 获取ADC偏移
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取ADC偏移值</li>
     *   <li>当前返回0</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @return 偏移值（固定为0）
     */
    public int getAdOffset(int chIdx,int chMode,int adIdx){  // 获取ADC偏移方法

        return 0;  // 返回0
    }  // 方法结束
    
    /**
     * 设置通道增益
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置通道增益值</li>
     *   <li>当前为空实现</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param resistanceType 阻抗类型
     * @param vIdx 电压索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @param val 增益值
     */
    public void setChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx,int val){  // 设置通道增益方法（空实现）

//        chGain[chIdx][vIdx][resistanceType] =  val;
    }  // 方法结束

    /**
     * 获取通道增益
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取通道增益值</li>
     *   <li>当前返回0</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param resistanceType 阻抗类型
     * @param vIdx 电压索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @return 增益值（固定为0）
     */
    public int getChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx){  // 获取通道增益方法

        return 0;  // 返回0
    }  // 方法结束

    /**
     * 计算增益系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的calcGain方法</li>
     *   <li>根据增益校准数据计算PGA步进dB、低增益高增益比和ADC满量程系数</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * PGA步进dB：
     *   ch_pga_stepdb = 20 × log10(0.257 × gain_40_8 / 0.0408 / gain_257) / 8
     * 
     * 低增益高增益比：
     *   ch_lghg = 0.257 × gain_25_7 / 0.0257 / gain_257
     * 
     * ADC满量程D：
     *   ch_fs_d[档位1] = 0.0102 × 8 × 1023 / 10.22 / gain_10_2
     *   ch_fs_d[档位2] = 0.0408 × 8 × 1023 / 10.22 / gain_40_8
     *   ch_fs_d[档位3] = 0.510 × 8 × 1023 / 10.22 / gain_510
     *   ch_fs_d[档位4] = ch_fs_d[档位3] × ch_fs_d[档位2] / ch_fs_d[档位1]
     * 
     * ADC满量程A：
     *   ch_fs_a[档位1] = 16384/2 / (0.0112 × 8 × 1023 / 10.22 / gain_11_2 - ch_fs_d[档位1])
     *   ch_fs_a[档位2] = 16384/2 / (0.0448 × 8 × 1023 / 10.22 / gain_44_8 - ch_fs_d[档位2])
     *   ch_fs_a[档位3] = 16384/2 / (0.560 × 8 × 1023 / 10.22 / gain_560 - ch_fs_d[档位3])
     *   ch_fs_a[档位4] = ch_fs_a[档位3] × ch_fs_a[档位2] / ch_fs_a[档位1]
     * </pre>
     * 
     * @param flag 通道标志位，每位表示对应通道需要计算
     */
    @Override  // 覆盖父类方法
    public void calcGain(int flag){  // 计算增益系数方法

        for(int i=0;i<ChannelFactory.CH_CNT;i++){  // 循环：遍历所有通道

            if((flag & (1<<i)) != 0) {  // 判断：当前通道是否需要计算
                ch_pga_stepdb[i] = (float) (20 * Math.log10(0.257 * gain_pga_stepdb_a40_8[i] / 0.0408 / gain_pga_stepdb_a257[i]) / 8);  // 计算PGA步进dB
                ch_lghg[i] = (float) (0.257 * gain_pga_lghg_a25_7[i] / 0.0257 / gain_pga_stepdb_a257[i]);  // 计算低增益高增益比

                for(int k=0;k<3;k++) {  // 循环：遍历3个ADC
                    ch_fs_d[i][RATIO_DANG_1][k] = (float) (0.0102 * 8 * getMaxAdVal() / FULL_VV / gain_fs_a10_2[i][k]);  // 计算档位1满量程D
                    ch_fs_d[i][RATIO_DANG_2][k] = (float) (0.0408 * 8 * getMaxAdVal() / FULL_VV / gain_fs_a40_8[i][k]);  // 计算档位2满量程D
                    ch_fs_d[i][RATIO_DANG_3][k] = (float) (0.510 * 8 * getMaxAdVal() / FULL_VV / gain_fs_a510[i][k]);  // 计算档位3满量程D
                    ch_fs_d[i][RATIO_DANG_4][k] = ch_fs_d[i][RATIO_DANG_3][k] * ch_fs_d[i][RATIO_DANG_2][k] / ch_fs_d[i][RATIO_DANG_1][k];  // 计算档位4满量程D（推导值）

                    ch_fs_a[i][RATIO_DANG_1][k] = (float) (getAdFsDefault()/2 / (0.0112 * 8 * getMaxAdVal() / FULL_VV / gain_fs_a11_2[i][k] - ch_fs_d[i][RATIO_DANG_1][k]));  // 计算档位1满量程A
                    ch_fs_a[i][RATIO_DANG_2][k] = (float) (getAdFsDefault()/2  / (0.0448 * 8 * getMaxAdVal() / FULL_VV / gain_fs_a44_8[i][k] - ch_fs_d[i][RATIO_DANG_2][k]));  // 计算档位2满量程A
                    ch_fs_a[i][RATIO_DANG_3][k] = (float) (getAdFsDefault()/2  / (0.560 * 8 * getMaxAdVal() / FULL_VV / gain_fs_a560[i][k] - ch_fs_d[i][RATIO_DANG_3][k]));  // 计算档位3满量程A
                    ch_fs_a[i][RATIO_DANG_4][k] = ch_fs_a[i][RATIO_DANG_3][k] * ch_fs_a[i][RATIO_DANG_2][k] / ch_fs_a[i][RATIO_DANG_1][k];  // 计算档位4满量程A（推导值）
                }  // 循环结束：ADC
            }  // 判断结束
        }  // 循环结束：通道
    }  // 方法结束

    /**
     * 计算PGA步进
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的calcPgaSetp方法</li>
     *   <li>当前为空实现</li>
     * </ul>
     * 
     * @param flag 通道标志位
     */
    @Override  // 覆盖父类方法
    public void calcPgaSetp(int flag){  // 计算PGA步进方法（空实现）

    }  // 方法结束





}  // 类结束
