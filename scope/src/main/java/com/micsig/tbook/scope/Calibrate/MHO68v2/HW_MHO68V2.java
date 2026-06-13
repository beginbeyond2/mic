package com.micsig.tbook.scope.Calibrate.MHO68v2;

import android.util.Log;

import com.micsig.base.DoubleUtil;
import com.micsig.tbook.hardware.Hardware;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.HW;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.fpga.FPGACommand;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * MHO68 V2示波器硬件操作实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO68v2（MHO68 V2示波器校准模块）</li>
 *   <li>架构层级：硬件抽象层 - 具体产品实现</li>
 *   <li>设计模式：继承HW抽象基类，实现具体硬件控制</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO68 V2示波器的继电器控制逻辑</li>
 *   <li>管理PGA增益和ADC增益配置</li>
 *   <li>处理校准数据的存储和加载</li>
 *   <li>计算通道系数和零点校准值</li>
 *   <li>支持1MΩ和50Ω两种阻抗模式</li>
 * </ul>
 * 
 * <p><b>硬件架构特点：</b>
 * <pre>
 * MHO68 V2硬件架构：
 *   ┌─────────────────────────────────────────────────────────┐
 *   │                    通道模拟前端                          │
 *   │   ┌─────────┐    ┌─────────┐    ┌─────────┐            │
 *   │   │ 继电器组 │───▶│ PGA放大器│───▶│ ADC芯片 │            │
 *   │   │ (档位控制)│    │(增益控制)│    │(采样)   │            │
 *   │   └─────────┘    └─────────┘    └─────────┘            │
 *   │        │              │              │                  │
 *   │        ▼              ▼              ▼                  │
 *   │   移位寄存器      AD8370控制      SPI通信               │
 *   │   (64位)          (PGA芯片)       (ADC配置)             │
 *   └─────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继电器控制原理：</b>
 * <pre>
 * 移位寄存器控制字结构（每通道8位，共64位）：
 *   ┌────────────────────────────────────────────────────────┐
 *   │  CH8   │  CH7   │  CH6   │  CH5   │  CH4   │  CH3   │  CH2   │  CH1   │
 *   │ [7:0]  │ [7:0]  │ [7:0]  │ [7:0]  │ [7:0]  │ [7:0]  │ [7:0]  │ [7:0]  │
 *   └────────────────────────────────────────────────────────┘
 * 
 * 每通道8位控制字定义：
 *   bit0: DC耦合使能（0=使能，1=禁止）
 *   bit1: 采样使能（0=使能，1=禁止）
 *   bit2: 1MΩ阻抗选择（0=选择，1=不选择）
 *   bit3: 50Ω阻抗选择（0=选择，1=不选择）
 *   bit4-7: 档位继电器控制
 * </pre>
 * 
 * <p><b>档位分组说明：</b>
 * <pre>
 * 1MΩ阻抗档位分组：
 *   第一档(RATIO_DANG_1): 10mV/div ~ 100mV/div   （小量程）
 *   第二档(RATIO_DANG_2): 200mV/div ~ 2V/div     （中量程）
 *   第三档(RATIO_DANG_3): 5V/div ~ 10V/div       （大量程）
 * 
 * 50Ω阻抗档位分组：
 *   第一档(RATIO_DANG_1): 10mV/div ~ 20mV/div    （小量程）
 *   第二档(RATIO_DANG_2): 50mV/div ~ 200mV/div   （中量程）
 *   第三档(RATIO_DANG_3): 500mV/div ~ 1V/div     （大量程）
 * </pre>
 * 
 * <p><b>校准数据结构：</b>
 * <pre>
 * 校准数据存储布局：
 *   ├── 通道增益系数（1MΩ）: [CH_CNT][DANG_CNT][2]
 *   ├── 通道零点（1MΩ）: [CH_CNT][RATIO_DANG_CNT][33]
 *   ├── PGA步进dB: [CH_CNT]
 *   ├── ADC满量程系数A: [CH_CNT][6][2]
 *   ├── ADC满量程系数D: [CH_CNT][6]
 *   ├── 通道电容高值: [CH_CNT * RATIO_DANG_CNT]
 *   ├── 通道增益系数（50Ω）: [CH_CNT][RATIO_DANG_CNT][2]
 *   ├── 通道零点（50Ω）: [CH_CNT][RATIO_DANG_CNT][33]
 *   ├── 通道增益: [CH_CNT][DANG_CNT][2]
 *   └── 校准时间字符串
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：HW（硬件操作抽象基类）</li>
 *   <li>依赖：Hardware（硬件操作接口）</li>
 *   <li>依赖：FPGACommand（FPGA命令管理器）</li>
 *   <li>依赖：CabteRegister（校准寄存器管理）</li>
 *   <li>依赖：Channel/ChannelFactory（通道管理）</li>
 *   <li>依赖：VerticalAxis（垂直轴档位管理）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>垂直档位调整时控制继电器切换</li>
 *   <li>阻抗切换（1MΩ/50Ω）时更新硬件配置</li>
 *   <li>校准过程中保存/加载校准数据</li>
 *   <li>波形显示时计算通道系数</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 2.0
 * @since 2019
 * @see HW 硬件操作抽象基类
 * @see HwConfig 硬件配置管理
 * @see FPGACommand FPGA命令管理器
 * @see CabteRegister 校准寄存器管理
 */
public class HW_MHO68V2 extends HW {
    
    /** 日志标签 */
    private static final String TAG = "HW_MHO68V2";
    
    /** 通道电源使能标志 */
    private boolean bPowerEnable = true;
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数初始化FPGA数量</li>
     *   <li>添加校准项目列表</li>
     *   <li>根据ADC型号配置增益代码数组</li>
     * </ul>
     * 
     * @param fpgaNums FPGA数量（MHO68 V2为2个）
     */
    public HW_MHO68V2(int fpgaNums){
        super(fpgaNums);
        
        // 添加校准项目列表
        // 包括：零点校准、精细增益校准、标准增益校准、偏移量校准、电容校准
        addItemAll(Arrays.asList("零点校准","精细增益校准(需要信号)","标准增益校准(需要信号)","偏移量校准(需要信号)","电容校准(需要信号)"));

        // 根据ADC型号配置增益代码数组
        // 如果不是B12DJ3200NBB型号，使用V3版本的增益代码
        if(!Hardware.getInstance().isAdcB12DJ3200NBB()){
            System.arraycopy(GAIN_ADFS_CODE_V3,0,GAIN_ADFS_CODE,0,GAIN_ADFS_CODE_V3.length);
        }
    }

    /**
     * 设置通道PGA增益
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据通道档位计算PGA增益值</li>
     *   <li>校准模式下使用固定PGA值</li>
     *   <li>正常模式下根据档位动态计算</li>
     * </ul>
     * 
     * <p><b>计算流程：</b>
     * <ol>
     *   <li>判断是否在校准模式</li>
     *   <li>校准模式：使用FPGA命令中的固定PGA值</li>
     *   <li>正常模式：调用CabteRegister.calc_pga_fs_gain计算</li>
     *   <li>发送PGA增益值到硬件</li>
     * </ol>
     */
    @Override
    public void setChPga() {
        Channel channel;
        final CabteRegister cabteRegister = CabteRegister.getInstance();
        final int [] vol = new int[ChannelFactory.CH_CNT];
        final int [] result = {0,0,0,0,0};

        int maxIdx = ChannelFactory.getMaxChIdx();

        FPGACommand fpgaCommand = FPGACommand.getInstance();
        
        // 判断是否在校准模式且为零点校准或增益校准
        if(fpgaCommand.isCalibrate()
                && (fpgaCommand.isZeroCalibrate() || fpgaCommand.isChGainCalibrate())){
            // 校准模式：使用固定PGA值
            for(int i=ChannelFactory.CH1;i<maxIdx;i++){
                vol[i] = fpgaCommand.getPgaVal();
            }
        }else{
            // 正常模式：根据档位计算PGA值
            for(int i=ChannelFactory.CH1;i<maxIdx;i++) {
                channel = ChannelFactory.getDynamicChannel(i);
                if(channel != null) {
                    // 计算实际电压档位值（考虑探头衰减）
                    double vScaleVal = channel.getVScaleVal() / channel.getProbeRate();
                    // 计算PGA、满量程和增益值
                    cabteRegister.calc_pga_fs_gain(channel.getChId(),
                            vScaleVal,
                            result);
                    vol[i] = result[0] & 0xFFFF;
                }
            }
        }

        // 发送PGA增益值到硬件
        setChPgaGain(vol);
    }

    /**
     * 设置ADC增益
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>为指定FPGA的通道设置ADC增益</li>
     *   <li>根据通道采样状态决定是否设置</li>
     *   <li>校准模式下的增益校准不执行设置</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引（0或1）
     */
    @Override
    public void setAdGain(int fpgaIdx) {
        boolean[] sle = new boolean[ChannelFactory.CH_CNT];
        Scope scope = Scope.getInstance();
        // 获取采样通道数和采样状态数组
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true),sle);

        int [] result = {0,0,0,0,0};
        CabteRegister pCabReg = CabteRegister.getInstance();
        FPGACommand fpgaCommand = FPGACommand.getInstance();
        Channel channel;
        float [] gain_bc = {1,1,1,1};

        int adcMaxChNums = HwConfig.getInstance().getAdcMaxChNums();
        // 计算当前FPGA对应的通道范围
        int beginIdx = FPGACommand.beginChIdx(fpgaIdx);
        int endIdx = FPGACommand.endChIdx(fpgaIdx);
        int adcIdx = 0;

        // 校准模式下的增益校准不执行设置
        if(fpgaCommand.isCalibrate()
                && fpgaCommand.isChGainCalibrate()){
            return;
        }

        // 遍历当前FPGA对应的通道
        for(int i=beginIdx;i<endIdx;i++){
            if(sle[i]){
                channel = ChannelFactory.getDynamicChannel(i);
                if(channel != null) {
                    // 计算ADC索引
                    adcIdx = (i - beginIdx) / adcMaxChNums;
                    // 计算PGA、满量程和增益值
                    pCabReg.calc_pga_fs_gain(channel.getChId(),
                            channel.getVScaleVal() / channel.getProbeRate()
                            , result);
                    // 写入ADC增益到FPGA
                    fpgaCommand.writeAD_gain(fpgaIdx,adcIdx,i % adcMaxChNums,result[1] & 0xFFFF);
                }
            }
        }

        // 发送增益广播和设备命令
        fpgaCommand.sendFpga_gain_bc(fpgaIdx,gain_bc);
        fpgaCommand.cmdDevice(fpgaIdx,50);
    }

    /**
     * 通道电源使能控制
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制通道模拟电路的电源开关</li>
     *   <li>关闭电源时设置移位寄存器关闭DC耦合</li>
     * </ul>
     * 
     * @param bEnable true表示使能电源，false表示关闭电源
     */
    @Override
    public void ChPowerEnable(boolean bEnable) {
        bPowerEnable = bEnable;
        if(!bEnable) {
            // 关闭电源时设置移位寄存器
            long iTmpValue = SHIFTREG_INIT_VAL_MHO10008;

            Scope scope = Scope.getInstance();
            int chMax = scope.getChNum() + ChannelFactory.CH1;
            int idx = 0;
            for (int i = ChannelFactory.CH1; i < chMax; i++) {
                idx = ChannelFactory.CH8 - i;
                Channel channel = ChannelFactory.getDynamicChannel(i);
                // 如果通道不在采样中或为AC耦合，关闭DC耦合
                if (!scope.isChannelInSample(i)
                        || channel.getCoupleType() == Channel.COUPLE_TYPE_AC) {
                    iTmpValue &= ~(1L << (idx * 8 + 0));
                }
            }

            sendShiftRegister(iTmpValue);
        }
    }

    /**
     * 改变通道垂直档位 - 控制继电器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据通道档位和阻抗类型计算继电器控制字</li>
     *   <li>发送移位寄存器命令控制继电器动作</li>
     *   <li>发送功率检测配置</li>
     * </ul>
     * 
     * <p><b>控制流程：</b>
     * <ol>
     *   <li>检查电源使能状态</li>
     *   <li>遍历所有通道计算档位索引</li>
     *   <li>根据阻抗类型和档位设置继电器控制位</li>
     *   <li>发送移位寄存器命令（两次发送确保稳定）</li>
     *   <li>发送功率检测配置</li>
     * </ol>
     */
    @Override
    public void changeChVolScale() {
        // 检查电源使能状态
        if(!bPowerEnable){
            return;
        }
        
        // 移位寄存器初始值（全1的64位掩码）
        long marsk= SHIFTREG_INIT_VAL_MHO10008;
        Scope scope = Scope.getInstance();

        Channel channel;
        int dang = 0;
        int idx = 0;
        int chMax = scope.getChNum() + ChannelFactory.CH1;
        
        // 获取基础移位寄存器配置
        long temp = ChVolScaleShiftRegister();
        
        // 功率检测控制值
        long pdVal = 0;
        int resistanceType;
        
        // 遍历所有通道
        for(int ch=ChannelFactory.CH1;ch<chMax;ch++){
            // 计算反转索引（CH1→7, CH8→0）
            idx = ChannelFactory.CH8 - ch;
            channel = ChannelFactory.getDynamicChannel(ch);
            resistanceType = channel.getResistanceType();

            // 根据当前档位值计算继电器档位索引
            dang = getRatioIdx(resistanceType,channel.getVScaleVal()/channel.getProbeRate());

            Log.d(TAG,"调整档位-------10------dang=" + dang + ",ch=" + ch + "," +
                    "resistanceType=" + resistanceType+",getVScaleVal="+channel.getVScaleVal()
                    +",getProbeRate="+channel.getProbeRate());
            
            // 根据档位设置功率检测控制值
            switch (dang){
                default:
                case HW.RATIO_DANG_1:
                    // 第一档(小量程): 0b00000110
                    pdVal |= (0x6L << ((ch * 8)));
                    break;
                case HW.RATIO_DANG_2:
                    // 第二档（中量程）: 0b00001101
                    pdVal |= (0xDL << ((ch * 8)));
                    break;
                case HW.RATIO_DANG_3:
                    // 第三档（大量程）: 0b00001011
                    pdVal |= (0xBL << ((ch * 8)));
                    break;
            }
            
            // 根据阻抗类型(1MΩ/50Ω)设置不同的继电器控制位
            if(resistanceType== Channel.RESISTANCE_50){
                // 50Ω阻抗的继电器控制
                marsk &= ~(1L << (idx * 8 + 3));
                marsk &= ~(1L << (idx * 8 + 4));
                if(dang == HW.RATIO_DANG_1){
                    marsk &= ~(1L << (idx * 8 + 7));
                }else{
                    marsk &= ~(1L << (idx * 8 + 6));
                }
            }else{
                // 1MΩ阻抗的继电器控制
                marsk &= ~(1L << (idx * 8 + 2));
                switch (dang){
                    default:
                    case HW.RATIO_DANG_1:
                        marsk &= ~(1L << (idx * 8 + 7));
                        marsk &= ~(1L << (idx * 8 + 4));
                        break;
                    case HW.RATIO_DANG_2:
                        marsk &= ~(1L << (idx * 8 + 6));
                        marsk &= ~(1L << (idx * 8 + 4));
                        break;
                    case HW.RATIO_DANG_3:
                        marsk &= ~(1L << (idx * 8 + 6));
                        marsk &= ~(1L << (idx * 8 + 5));
                        break;
                }
            }
        }

        Log.d(TAG,"调整档位-------11------dang=" + dang + ",pdVal=" + Long.toHexString(pdVal) + ",marsk=" + Long.toHexString(marsk) + ",temp=" + Long.toHexString(temp));

        // 发送移位寄存器命令 - 控制继电器动作
        sendShiftRegister(temp & marsk);
        usleep(3000);
        sendShiftRegister(temp);
        
        // 发送功率检测配置
        FPGACommand.getInstance().SendChPD_v2(pdVal);
    }
    
    /**
     * 将long类型的值以二进制方式打印输出
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>用于调试和查看long类型数值的二进制表示形式</li>
     *   <li>输出格式为完整的64位二进制字符串，每8位之间用空格分隔</li>
     * </ul>
     * 
     * @param value 待打印的long类型数值
     */
    private void printLongAsBinary(long value) {
        StringBuilder binaryStr = new StringBuilder();
        for (int i = 63; i >= 0; i--) {
            long mask = 1L << i;
            binaryStr.append((value & mask) != 0 ? "1" : "0");
            if (i % 8 == 0 && i != 0) {
                binaryStr.append(" ");
            }
        }
    }

    /**
     * 计算通道电压比例移位寄存器的值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据通道的采样状态和耦合类型设置相应的寄存器位</li>
     *   <li>用于硬件校准过程中配置通道电压相关的寄存器</li>
     * </ul>
     * 
     * <p><b>返回值结构：</b>
     * <pre>
     * 每通道8位控制字：
     *   bit0: 当通道不在采样中或耦合类型为AC时清零
     *   bit1: 当通道在采样中时清零
     *   其他位保持初始值
     * </pre>
     * 
     * @return long类型的移位寄存器值
     */
    private long ChVolScaleShiftRegister(){
        long iTmpValue = SHIFTREG_INIT_VAL_MHO10008;

        Scope scope = Scope.getInstance();
        int chMax = scope.getChNum() + ChannelFactory.CH1;
        int idx = 0;
        
        for(int i=ChannelFactory.CH1;i<chMax;i++){
            idx =  ChannelFactory.CH8 - i;
            Channel channel = ChannelFactory.getDynamicChannel(i);
            
            // 如果通道不在采样中或耦合类型为AC交流耦合
            if(!scope.isChannelInSample(i)
                    || channel.getCoupleType() == Channel.COUPLE_TYPE_AC){
                iTmpValue &= ~(1L << (idx * 8 + 0));
            }
            
            // 如果通道在采样中
            if(scope.isChannelInSample(i)){
                iTmpValue &= ~(1L << (idx * 8 + 1));
            }
        }

        return iTmpValue;
    }

    // ==================== 校准相关常量定义 ====================

    /** 档位数量 */
    public static final int MHO68V2_RATIO_DANG_CNT = RATIO_DANG_3 + 1;

    // 校准状态索引常量定义
    public static final int CALIBRATION_TOP_ZERO = 0;
    public static final int CALIBRATION_CENTER_CH1GAIN_1 = CALIBRATION_TOP_ZERO + 1;
    public static final int CALIBRATION_CENTER_CH2GAIN_1 = CALIBRATION_CENTER_CH1GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH3GAIN_1 = CALIBRATION_CENTER_CH2GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH4GAIN_1 = CALIBRATION_CENTER_CH3GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH5GAIN_1 = CALIBRATION_CENTER_CH4GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH6GAIN_1 = CALIBRATION_CENTER_CH5GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH7GAIN_1 = CALIBRATION_CENTER_CH6GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH8GAIN_1 = CALIBRATION_CENTER_CH7GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH1GAIN_2 = CALIBRATION_CENTER_CH8GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH2GAIN_2 = CALIBRATION_CENTER_CH1GAIN_2 + VerticalAxis.DANG_CNT * 2;
    public static final int CALIBRATION_CENTER_CH3GAIN_2 = CALIBRATION_CENTER_CH2GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH4GAIN_2 = CALIBRATION_CENTER_CH3GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH5GAIN_2 = CALIBRATION_CENTER_CH4GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH6GAIN_2 = CALIBRATION_CENTER_CH5GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH7GAIN_2 = CALIBRATION_CENTER_CH6GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH8GAIN_2 = CALIBRATION_CENTER_CH7GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CHOFFSET_CALIBRATE_CH1 = CALIBRATION_CENTER_CH8GAIN_2 + VerticalAxis.DANG_CNT * 2;
    public static final int CHOFFSET_CALIBRATE_CH2 = CHOFFSET_CALIBRATE_CH1 + 12;
    public static final int CHOFFSET_CALIBRATE_CH3 = CHOFFSET_CALIBRATE_CH2 + 12;
    public static final int CHOFFSET_CALIBRATE_CH4 = CHOFFSET_CALIBRATE_CH3 + 12;
    public static final int CHOFFSET_CALIBRATE_CH5 = CHOFFSET_CALIBRATE_CH4 + 12;
    public static final int CHOFFSET_CALIBRATE_CH6 = CHOFFSET_CALIBRATE_CH5 + 12;
    public static final int CHOFFSET_CALIBRATE_CH7 = CHOFFSET_CALIBRATE_CH6 + 12;
    public static final int CHOFFSET_CALIBRATE_CH8 = CHOFFSET_CALIBRATE_CH7 + 12;
    public static final int CHCAP_CALIBRATE_CH1 = CHOFFSET_CALIBRATE_CH8 + 12;
    public static final int CHCAP_CALIBRATE_CH2 = CHCAP_CALIBRATE_CH1 + MHO68V2_RATIO_DANG_CNT;
    public static final int CHCAP_CALIBRATE_CH3 = CHCAP_CALIBRATE_CH2 + MHO68V2_RATIO_DANG_CNT;
    public static final int CHCAP_CALIBRATE_CH4 = CHCAP_CALIBRATE_CH3 + MHO68V2_RATIO_DANG_CNT;

    public static final int CHCAP_CALIBRATE_CH5 = CHCAP_CALIBRATE_CH4 + MHO68V2_RATIO_DANG_CNT;
    public static final int CHCAP_CALIBRATE_CH6 = CHCAP_CALIBRATE_CH5 + MHO68V2_RATIO_DANG_CNT;
    public static final int CHCAP_CALIBRATE_CH7 = CHCAP_CALIBRATE_CH6 + MHO68V2_RATIO_DANG_CNT;
    public static final int CHCAP_CALIBRATE_CH8 = CHCAP_CALIBRATE_CH7 + MHO68V2_RATIO_DANG_CNT;
    public static final int CALIBRATE_STATE_MAX = CHCAP_CALIBRATE_CH8 + MHO68V2_RATIO_DANG_CNT;
    public static final int FLASH_MAX = 100 * 1024;

    private static final int MAX_COFIT_SIZE = 30 * 1024;


    /** 校准状态字节数组 */
    byte [] calibartionState = new byte[CALIBRATE_STATE_MAX/8 + 1];

    /**
     * 获取校准系数最大尺寸
     * 
     * @return 校准系数最大尺寸（字节）
     */
    @Override
    protected int getMaxCofitSize() {
        return MAX_COFIT_SIZE;
    }

    /**
     * 获取用户Flash最大地址
     * 
     * @return 用户Flash最大地址
     */
    @Override
    protected int getUseFlashMax() {
        return getCalibrateStateAddr() +  CALIBRATE_STATE_MAX/8 + 1;
    }

    /**
     * 获取Flash最大地址
     * 
     * @return Flash最大地址
     */
    @Override
    protected int getFlashMax() {
        return FLASH_MAX;
    }

    /**
     * 清除校准状态
     * 
     * <p>将所有校准状态位清零并写入E2PROM
     */
    @Override
    public void clearCalibrationState() {
        Arrays.fill(calibartionState, (byte) 0);
        write(getCalibrateStateAddr(),calibartionState);
    }

    /**
     * 保存校准状态
     * 
     * <p>将当前校准状态写入E2PROM
     */
    @Override
    public void saveCalibrationState() {
        write(getCalibrateStateAddr(),calibartionState);
    }

    /**
     * 设置校准状态
     * 
     * @param idx 校准项目索引
     * @param bState 状态值（true=已完成，false=未完成）
     */
    @Override
    public void setCalibrationState(int idx, boolean bState) {
        if(idx >=CALIBRATION_TOP_ZERO && idx < CALIBRATE_STATE_MAX){
            int i = idx / 8;
            idx = idx % 8;
            if(bState){
                calibartionState[i] |= (1 << idx);
            }else{
                calibartionState[i] &= ~(1 << idx);
            }
        }
    }
    
    /**
     * 获取校准状态
     * 
     * @param idx 校准项目索引
     * @return 状态值（true=已完成，false=未完成）
     */
    @Override
    public boolean isCalibrationState(int idx){
        if(idx >=CALIBRATION_TOP_ZERO && idx < CALIBRATE_STATE_MAX){
            int i = idx / 8;
            idx = idx % 8;
            return (calibartionState[i] & (1 << idx)) != 0;
        }
        return false;
    }
    
    /**
     * 加载校准状态
     * 
     * <p>从E2PROM读取校准状态数据
     */
    @Override
    public void loadCalibrationState(){
        read(getCalibrateStateAddr(),calibartionState);
    }

    /**
     * 判断是否完成顶层校准
     * 
     * @return true=已完成，false=未完成
     */
    @Override
    public boolean isTopCalibration(){
        return isCalibrationState(CALIBRATION_TOP_ZERO);
    }
    
    /**
     * 获取校准项目状态
     * 
     * @param idx 校准项目索引
     * @param sb 用于存储状态详情的字符串构建器
     * @return true=该项目所有子项都已完成
     */
    @Override
    public boolean getCalibrationItemState(int idx,StringBuilder sb){
        switch (idx){
            case 0:
                return isTopCalibration();
            case 1:
                return getChGain(sb);
            case 2:
                return getChGain2(sb);
            case 3:
                return getChOffset(sb);
            case 4:
                return getChCap(sb);
        }
        return false;
    }

    /**
     * 获取通道电容校准状态
     * 
     * @param sb 用于存储状态详情的字符串构建器
     * @return true=所有通道电容校准都已完成
     */
    private boolean getChCap(StringBuilder sb) {
        boolean bit = true;
        for (int i = 1; i < getRatioDangCnt(); i++){
            boolean bEnter = false;
            for (int j = ChannelFactory.CH1; j < ChannelFactory.getMaxChIdx(); j++) {

                boolean b1 = isCalibrationState(CHCAP_CALIBRATE_CH1 + j * getRatioDangCnt() + i);
                double v = VerticalAxis.getScaleIdValById(CabteRegister.getRatioIdx2Dang(Channel.RESISTANCE_1M,i));
                if(!b1) {
                    sb.append("ch").append(j).append("[").append(v).append("]=").append("0").append("    ");
                    bEnter = true;
                }
                bit = bit &&  b1;
            }
            if(bEnter) {
                sb.append("\n");
            }
        }
        return bit;
    }

    /**
     * 获取通道偏移校准状态
     * 
     * @param stringBuilder 用于存储状态详情的字符串构建器
     * @return true=所有通道偏移校准都已完成
     */
    private boolean getChOffset(StringBuilder stringBuilder) {

        boolean bit = true;
        for (int j = ChannelFactory.CH1; j < ChannelFactory.getMaxChIdx(); j++) {
            Channel channel = ChannelFactory.getDynamicChannel(j);
            boolean bEnter = false;
            for (int i = 0; i < 6; i++) {
                for(int k=0; k< 2; k++) {
                    double v = VerticalAxis.getScaleIdValById(getRatioIdx2Dang(channel.getResistanceType(),i % getRatioDangCnt()));
                    boolean b1 = isCalibrationState(CHOFFSET_CALIBRATE_CH1 + j * 12 + i * 2 + k);
                    if(k==0) v = -v;
                    if(!b1) {
                        stringBuilder.append("ch").append(j).append((i / getRatioDangCnt() > 0) ? ",50Ω" : ",1M").append("[").append(v).append("]=").append("0").append("    ");
                        bEnter = true;
                    }
                    bit = bit &&  b1;
                }
            }
            if(bEnter) {
                stringBuilder.append("\n");
            }
        }
        return bit;
    }
    
    /**
     * 获取通道增益校准状态（第二组）
     * 
     * @param sb 用于存储状态详情的字符串构建器
     * @return true=所有通道增益校准都已完成
     */
    private boolean getChGain2(StringBuilder sb){
        boolean bit = true;
        for(int k=0;k<2;k++) {
            int maxGear = VerticalAxis.getMaxGear();
            if(k == 1){
                maxGear = VerticalAxis.DANG_1V;
            }
            for(int j = ChannelFactory.CH1;j<ChannelFactory.getMaxChIdx();j++){
                boolean bEnter = false;
                for (int i = VerticalAxis.getMinGear(); i <= maxGear; i++) {
                    boolean b1 = isCalibrationState(CALIBRATION_CENTER_CH1GAIN_2 + j * VerticalAxis.DANG_CNT * 2 + k * VerticalAxis.DANG_CNT + i);
                    double v = VerticalAxis.getScaleIdValById(i);
                    if(!b1) {
                        sb.append("ch").append(j).append((k > 0) ? ",50" : ",1M").append("[").append(v).append("]=").append("0").append("   ");
                        bEnter = true;
                    }
                    bit = bit && b1;
                }
                if(bEnter) {
                    sb.append("\n");
                }
            }
        }
        return bit;
    }
    
    /**
     * 获取通道增益校准状态（第一组）
     * 
     * @param sb 用于存储状态详情的字符串构建器
     * @return true=所有通道增益校准都已完成
     */
    private boolean getChGain(StringBuilder sb){

        boolean bit = true;
        for(int j = ChannelFactory.CH1;j<ChannelFactory.getMaxChIdx();j++){
            boolean bEnter = false;
            for(int i=0;i<GAIN_INPUT_AMP.length;i++){
                boolean b1 = isCalibrationState(CALIBRATION_CENTER_CH1GAIN_1 + j * GAIN_INPUT_AMP.length + i);
                if(!b1) {
                    sb.append("ch").append(j).append("[").append(GAIN_INPUT_AMP[i]).append("]=").append("0").append("   ");
                    bEnter = true;
                }
                bit = bit && b1;
            }
            if(bEnter) {
                sb.append("\n");
            }
        }
        return bit;
    }
    
    /**
     * 从ByteBuffer反序列化校准系数
     * 
     * <p><b>数据读取顺序：</b>
     * <ol>
     *   <li>通道系数（1MΩ）</li>
     *   <li>通道零点（1MΩ）</li>
     *   <li>PGA步进dB</li>
     *   <li>ADC满量程系数A</li>
     *   <li>ADC满量程系数D</li>
     *   <li>通道电容高值</li>
     *   <li>通道系数（50Ω）</li>
     *   <li>通道零点（50Ω）</li>
     *   <li>通道增益</li>
     *   <li>校准时间字符串</li>
     * </ol>
     * 
     * @param byteBuffer 字节缓冲区
     * @param idx 起始索引
     * @return 下一个可用索引
     */
    @Override
    protected int ByteBuffer2cofit(ByteBuffer byteBuffer, int idx) {
        idx = getFloatCoef3(coefChannel, byteBuffer, idx);
        idx = getFloatCoef3(chZero, byteBuffer, idx);
        idx = getFloatCoef1(ch_pga_stepdb,byteBuffer,idx);
        idx = getFloatCoef3(ad_fs_a, byteBuffer, idx);
        idx = getFloatCoef2(ad_fs_d,byteBuffer,idx);
        idx = getIntCoef1(chCapacitanceHigh,byteBuffer,idx);
        idx = getFloatCoef3(coefChannel_50, byteBuffer, idx);
        idx = getFloatCoef3(chZero_50, byteBuffer, idx);
        idx = getIntCoef3(chGain,byteBuffer,idx);
        
        // 这个需要放在结尾
        StringBuilder sb = new StringBuilder();
        idx = getString(sb,byteBuffer,idx);
        Log.d(TAG,"ByteBuffer2cofit:" + sb);
        cabteTime = sb.toString();
        return idx;
    }


    /**
     * 序列化校准系数到ByteBuffer
     * 
     * <p><b>数据写入顺序：</b>
     * <ol>
     *   <li>通道系数（1MΩ）</li>
     *   <li>通道零点（1MΩ）</li>
     *   <li>PGA步进dB</li>
     *   <li>ADC满量程系数A</li>
     *   <li>ADC满量程系数D</li>
     *   <li>通道电容高值</li>
     *   <li>通道系数（50Ω）</li>
     *   <li>通道零点（50Ω）</li>
     *   <li>通道增益</li>
     *   <li>校准时间字符串（保存时）</li>
     * </ol>
     * 
     * @param byteBuffer 字节缓冲区
     * @param idx 起始索引
     * @param bSave true表示保存时间戳
     * @return 下一个可用索引
     */
    @Override
    protected int cofit2ByteBuffer(ByteBuffer byteBuffer, int idx,boolean bSave) {

        idx = setFloatCoef3(coefChannel, byteBuffer, idx);
        idx = setFloatCoef3(chZero, byteBuffer, idx);
        idx = setFloatCoef1(ch_pga_stepdb,byteBuffer,idx);
        idx = setFloatCoef3(ad_fs_a, byteBuffer, idx);
        idx = setFloatCoef2(ad_fs_d,byteBuffer,idx);
        idx = setIntCoef1(chCapacitanceHigh,byteBuffer,idx);
        idx = setFloatCoef3(coefChannel_50, byteBuffer, idx);
        idx = setFloatCoef3(chZero_50, byteBuffer, idx);
        idx = setIntCoef3(chGain,byteBuffer,idx);
        
        // 这个放在结尾
        if(bSave) {
            String s = getNowTime();
            idx = setString(s, byteBuffer, idx);
            Log.d(TAG, "cofit2ByteBuffer:" + s);
        }
        return idx;
    }


    // ==================== 校准系数数组定义 ====================

    /** 通道增益数组 [通道][档位][阻抗类型] */
    private int[][][] chGain = new int[ChannelFactory.CH_CNT][VerticalAxis.DANG_CNT][2];
    
    /** 50Ω阻抗通道偏移量系数 [通道][档位][索引] */
    private float [][][]coefChannel_50 = new float[ChannelFactory.CH_CNT][MHO68V2_RATIO_DANG_CNT][2];
    
    /** 50Ω阻抗通道零点 [通道][档位][PGA值] */
    private float [][][]chZero_50 = new float[ChannelFactory.CH_CNT][MHO68V2_RATIO_DANG_CNT][33];
    
    /** PGA步进dB值 [通道] */
    private float [] ch_pga_stepdb = new float[ChannelFactory.CH_CNT];
    
    /** 1MΩ阻抗通道偏移量系数 [通道][档位][索引] */
    private float [][][]coefChannel = new float[ChannelFactory.CH_CNT][MHO68V2_RATIO_DANG_CNT][2];
    
    /** 1MΩ阻抗通道零点 [通道][档位][PGA值] */
    private float [][][]chZero = new float[ChannelFactory.CH_CNT][MHO68V2_RATIO_DANG_CNT][33];
    
    /** 通道电容高值 [通道 * 档位] */
    private int [] chCapacitanceHigh = new int[ChannelFactory.CH_CNT * MHO68V2_RATIO_DANG_CNT] ;

    // ==================== ADC满量程系数数组 ====================

    /**
     * ADC满量程系数A数组
     * 
     * <p><b>数组结构：</b> [通道][增益组][正负索引]
     * 
     * <p><b>增益组说明：</b>
     * <pre>
     * 索引0: 1MΩ 档位1
     * 索引1: 1MΩ 档位2
     * 索引2: 1MΩ 档位3
     * 索引3: 50Ω 档位1
     * 索引4: 50Ω 档位2
     * 索引5: 50Ω 档位3
     * </pre>
     */
    private float [][][] ad_fs_a = new float[ChannelFactory.CH_CNT][6][2];
    
    /** ADC满量程系数D数组 [通道][增益组] */
    private float [][] ad_fs_d = new float[ChannelFactory.CH_CNT][6];

    /** DA通道零点偏移值 */
    private static final float DA_CH_OFFSET_ZERO = 32768f;
    
    /**
     * 增益输入幅度数组
     * 
     * <p><b>数组结构：</b> 共21个值，分为7组，每组3个值
     * <pre>
     * 组0-2: 1MΩ阻抗的三个档位
     * 组3-6: 50Ω阻抗的三个档位
     * </pre>
     */
    public static double [] GAIN_INPUT_AMP ={
            0.036,0.143,0.570,
            // 直通
            0.015416,0.0164,0.017384,
            //
            0.18988,0.202,0.21412,
            //
            3.1396,3.34,3.5404,
            // 50Ω
            0.02773,0.0295,0.03127,
            // 50Ω
            0.13536,0.144,0.15264,
            // 50Ω
            0.45684,0.486,0.51516
    };
    
    /**
     * 增益PGA代码数组
     * 
     * <p><b>代码格式：</b> 16位，高8位为继电器控制，低8位为PGA设置
     */
    public static int [] GAIN_PGA_CODE = {
            0x204,0x210,0x21C,
            // 1
            0x21C,0x21C,0x21C,
            //
            0x213,0x213,0x213,
            //
            0x214,0x214,0x214,
            // 50Ω
            0x21B,0x21B,0x21B,
            // 50Ω
            0x21B,0x21B,0x21B,
            // 50Ω
            0x217,0x217,0x217,

    };

    /**
     * 增益ADC满量程代码数组（默认版本）
     * 
     * <p>用于B12DJ3200NBB型号ADC
     */
    public static int [] GAIN_ADFS_CODE = {
            0xA000,0xA000,0xA000,
            // 1
            0x88F6,0xA000,0xB70A,
            //
            0x88F6,0xA000,0xB70A,
            //
            0x88F6,0xA000,0xB70A,
            // 50Ω
            0x88F6,0xA000,0xB70A,
            // 50Ω
            0x88F6,0xA000,0xB70A,
            // 50Ω
            0x88F6,0xA000,0xB70A,
    };

    /**
     * 增益ADC满量程代码数组（V3版本）
     * 
     * <p>用于CAE2400型号ADC
     */
    public static int [] GAIN_ADFS_CODE_V3 = {
            0x80,0x80,0x80,
            // 1
            0x19,0x80,0xE6,
            //
            0x19,0x80,0xE6,
            //
            0x19,0x80,0xE6,
            // 50Ω
            0x19,0x80,0xE6,
            // 50Ω
            0x19,0x80,0xE6,
            // 50Ω
            0x19,0x80,0xE6,
    };

    /** 增益dB值数组 */
    public static int [] GAIN_DB = {-2,7,6,-1,-1,3};

    /**
     * 档位区间阈值定义
     * 
     * <p><b>1MΩ阻抗档位阈值：</b>
     * <pre>
     * RATIO_DANG_VAL[0] = 0.025: ≤25mV/div 为第一档
     * RATIO_DANG_VAL[1] = 0.6:   ≤600mV/div 为第二档
     * RATIO_DANG_VAL[2] = 10:    >600mV/div 为第三档
     * </pre>
     */
    public static double [] RATIO_DANG_VAL = {0.025,0.6,10};
    
    /**
     * 50Ω阻抗档位阈值
     * 
     * <p><b>50Ω阻抗档位阈值：</b>
     * <pre>
     * RATIO_DANG_VAL_50[0] = 0.015: ≤15mV/div 为第一档
     * RATIO_DANG_VAL_50[1] = 0.1:   ≤100mV/div 为第二档
     * RATIO_DANG_VAL_50[2] = 1:     >100mV/div 为第三档
     * </pre>
     */
    public static double [] RATIO_DANG_VAL_50 = {0.015,0.1,1};
    
    /**
     * 获取档位数量
     * 
     * @return 档位数量（3个）
     */
    @Override
    public int getRatioDangCnt() {
        return MHO68V2_RATIO_DANG_CNT;
    }

    /**
     * 设置默认校准系数值
     * 
     * <p>初始化所有校准系数为默认值
     */
    @Override
    protected void defaultVal() {
        defaultVal_ChGain();
        defaultVal_AdFullscale();
        defaultVal_coefChannel();
        defaultVal_chZero();
        defaultVal_ChCapacitance();
        defaultVal_AdGain();
    }
    
    /**
     * 设置默认通道增益值
     */
    public void defaultVal_ChGain(){
        for (int[][] ints : chGain) {
            for (int[] anInt : ints) {
                Arrays.fill(anInt, GAIN_ADFS_CODE[0]);
            }
        }
    }
    
    /**
     * 设置默认ADC满量程值
     */
    public void defaultVal_AdFullscale(){
        Arrays.fill(ch_pga_stepdb, 1.0f);

        for(int i=0;i<ad_fs_d.length;i++){
            for(int k=0;k<6;k++){
                ad_fs_d[i][k] = (float) GAIN_INPUT_AMP[(k + 1) * 3 + 1];
            }
        }

        for(int i=0;i<ad_fs_a.length;i++){
            for(int k=0;k<6;k++){
                int idx = (k + 1) * 3;
                ad_fs_a[i][k][0] = (float) ((GAIN_ADFS_CODE[idx + 1] - GAIN_ADFS_CODE[idx]) / (GAIN_INPUT_AMP[ idx + 1] - GAIN_INPUT_AMP[idx]));
                ad_fs_a[i][k][1] = (float) ((GAIN_ADFS_CODE[idx + 2] - GAIN_ADFS_CODE[idx + 1]) / (GAIN_INPUT_AMP[ idx + 2] - GAIN_INPUT_AMP[idx + 1]));
            }
        }

    }
    
    /**
     * 设置默认通道系数值
     */
    private void defaultVal_coefChannel(){

        for(int i=0;i<coefChannel.length;i++){
            for(int j=0;j<coefChannel[i].length;j++){
                coefChannel[i][j][1] = coefChannel[i][j][0] = (float) vol_ChannelCoef_default(Channel.RESISTANCE_1M,j);
            }
            for(int j=0;j<coefChannel_50[i].length;j++){
                coefChannel_50[i][j][1] = coefChannel_50[i][j][0] = (float) vol_ChannelCoef_default(Channel.RESISTANCE_50,j);
            }
        }

    }

    /**
     * 设置默认通道零点值
     */
    private void defaultVal_chZero(){

        for (float [][] v1 : chZero) {
            for(float[] v2:v1){
                Arrays.fill(v2, DA_CH_OFFSET_ZERO);
            }
        }
        for (float [][] v1 : chZero_50) {
            for(float[] v2:v1){
                Arrays.fill(v2, DA_CH_OFFSET_ZERO);
            }
        }
    }

    /**
     * 设置默认通道电容高值
     */
    private void defaultVal_ChCapacitance(){
        int val = 0xFFFF;
        val /= 2;
        Arrays.fill(chCapacitanceHigh, val);
    }

    /**
     * 设置默认ADC增益值
     */
    private void defaultVal_AdGain(){
        Arrays.fill(ch_pga_stepdb,1.0f);
    }
    
    /**
     * 设置指定通道的默认通道系数
     * 
     * @param ch 通道索引
     * @param dang 档位索引
     * @param resistanceType 阻抗类型
     */
    public void defaultVal_coefChannel(int ch,int dang,int resistanceType){

        if(resistanceType == Channel.RESISTANCE_1M) {
            coefChannel[ch][dang][1] = coefChannel[ch][dang][0] = (float) vol_ChannelCoef_default(Channel.RESISTANCE_1M, dang);
        }else{
            coefChannel_50[ch][dang][1] = coefChannel_50[ch][dang][0] = (float) vol_ChannelCoef_default(Channel.RESISTANCE_50, dang);
        }
    }

    /**
     * 计算LMH6401 PGA芯片的增益
     * 
     * @param pgaval PGA设置值
     * @return 增益倍数
     */
    private double getLMH6401(int pgaval){
        return Math.pow (10,(26 - pgaval)/20.0);
    }


    /**
     * 计算默认通道系数（扩展版本）
     * 
     * <p>根据通道索引、档位和PGA值计算默认通道系数
     * 
     * @param chIdx 通道索引
     * @param dangwei 档位索引
     * @param pgaVal PGA值
     * @return 默认通道系数
     */
    public double vol_ChannelCoef_defaultEx(int chIdx,int dangwei,int pgaVal){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int resistanceType = channel.getResistanceType();

        pgaVal &= 0xFF;
        double v = 1;
        double r = 0.5;

        int dang = getRatioIdx(resistanceType, VerticalAxis.getScaleIdValById(dangwei));
        if (resistanceType == Channel.RESISTANCE_50) {
            switch (dang) {
                case RATIO_DANG_1:
                    r = (100.0/48)*0.428232256;
                    v = 0.764;
                    break;
                case RATIO_DANG_2:
                    r = (100.0/39)*0.443946547;
                    v = 0.784;
                    break;
                default:
                case RATIO_DANG_3:
                    r = 0.443946547;
                    v = 0.825;
                    break;
            }

        } else {
            switch (dang){
                case RATIO_DANG_1:
                    v = 0.775;
                    r = (100.0 / 24)*0.5000450496;
                    break;
                case RATIO_DANG_2:
                    v = 0.79;
                    r = 0.480289107;
                    break;
                default:
                case RATIO_DANG_3:
                    v = 0.84;
                    r = 0.480289107;
                    break;
            }
        }
        return (v/(r*4*(100.0/122)*getLMH6401(pgaVal)*(100.0/(100 + 39*2 + 10*2))*1))*65536/(2.5*2*100*10.23);
    }

    /**
     * 计算默认通道系数
     * 
     * <p>根据阻抗类型和档位计算默认通道系数
     * 
     * @param resistanceType 阻抗类型
     * @param dang 档位索引
     * @return 默认通道系数
     */
    public double vol_ChannelCoef_default(int resistanceType,int dang){
        double vScaleVal = VerticalAxis.getScaleIdValById(getRatioIdx2Dang(resistanceType,dang));
        if(resistanceType == Channel.RESISTANCE_1M){
            double [] vv = {0.1376,4.60,68.5};
            double [] mm = {4.1667,1,1};
            return vScaleVal / ScopeBase.getVerticalPerGridPixels() / vv[dang] / mm[dang] * 65536 / 5;
        }else{
            double [] vv = {0.283,1.207,6.447};
            double [] mm = {2.083,2.564,1};
            return vScaleVal /ScopeBase.getVerticalPerGridPixels() / vv[dang] / mm[dang] * 65536 / 5;
        }
    }
    
    /**
     * 计算档位索引
     * 
     * <p>根据电阻类型和电压值确定对应的档位索引
     * 
     * @param resistanceType 阻抗类型
     * @param v 电压值
     * @return 档位索引
     */
    public int getRatioIdx(int resistanceType,double v){
        int i = 0;
        double [] ratioDangVal = resistanceType == 0 ? RATIO_DANG_VAL : RATIO_DANG_VAL_50;

        for(i=0;i<ratioDangVal.length;i++){
            if(v <= ratioDangVal[i]){
                break;
            }
        }
        if(i >= ratioDangVal.length){
            i = ratioDangVal.length - 1;
        }
        return i;
    }


    /**
     * 将比例索引转换为档位索引
     * 
     * @param resistanceType 阻抗类型
     * @param idx 比例索引
     * @return 档位索引
     */
    @Override
    public int getRatioIdx2Dang(int resistanceType,int idx){

        switch (idx) {
            default:
            case RATIO_DANG_1:
                return VerticalAxis.DANG_10mV;
            case RATIO_DANG_2:
                return resistanceType == Channel.RESISTANCE_50 ? VerticalAxis.DANG_50mV : VerticalAxis.DANG_200mV;
            case RATIO_DANG_3:
                return resistanceType == Channel.RESISTANCE_50 ? VerticalAxis.DANG_500mV : VerticalAxis.DANG_5V;
        }

    }
    
    /**
     * 获取垂直量程
     * 
     * @param resistanceType 阻抗类型
     * @param dang 档位索引
     * @return 垂直量程值（V）
     */
    @Override
    public double getVerticalRange(int resistanceType,int dang){
        double vRange = 2.5;

        switch (dang) {
            case RATIO_DANG_1:
                vRange = resistanceType == Channel.RESISTANCE_50 ? 1 : 1;
                break;
            case RATIO_DANG_2:
                vRange = resistanceType == Channel.RESISTANCE_50 ? 5 : 10;
                break;
            default:
            case RATIO_DANG_3:
                vRange = resistanceType == Channel.RESISTANCE_50 ? 10 : 150;
                break;
        }

        return vRange;
    }

    /**
     * 获取通道系数
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param idx 系数索引
     * @return 通道系数值
     */
    @Override
    public float getChannelCoef(int chIdx,int dang,int idx){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if(channel.getResistanceType() == Channel.RESISTANCE_50){
            return coefChannel_50[chIdx][dang][idx];
        }else{
            return coefChannel[chIdx][dang][idx];
        }
    }
    
    /**
     * 设置通道系数
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param idx 系数索引
     * @param val 系数值
     */
    @Override
    public void setChannelCoef(int chIdx,int dang,int idx,float val){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if(channel.getResistanceType() == Channel.RESISTANCE_50){
            coefChannel_50[chIdx][dang][idx] = val;
        }else{
            coefChannel[chIdx][dang][idx] = val;
        }
    }

    /**
     * 设置通道零点校准值
     * 
     * @param chIdx 通道索引
     * @param dwIdx 档位索引
     * @param pga PGA值
     * @param val 零点校准值
     */
    @Override
    public void setChannelZero(int chIdx,int dwIdx,int pga,float val){

        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int resistanceType = channel.getResistanceType();
        if(resistanceType == Channel.RESISTANCE_50){
            chZero_50[chIdx][dwIdx][pga] = val;
        }else {
            chZero[chIdx][dwIdx][pga] = val;
        }
    }

    /**
     * 获取通道零点校准值
     * 
     * @param chIdx 通道索引
     * @param dwIdx 档位索引
     * @param pga PGA值
     * @return 零点校准值
     */
    @Override
    public float getChannelZero(int chIdx,int dwIdx,int pga) {

        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int resistanceType = channel.getResistanceType();
        if(resistanceType == Channel.RESISTANCE_50){
            return chZero_50[chIdx][dwIdx][pga];
        }else {
            return chZero[chIdx][dwIdx][pga];
        }
    }

    /**
     * 获取通道电容高值
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @return 电容高值
     */
    @Override
    public int getChCapacitanceHigh(int chIdx,int dang){
        return chCapacitanceHigh[chIdx * MHO68V2_RATIO_DANG_CNT + dang];
    }

    /**
     * 设置通道电容高值
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param val 电容高值
     */
    @Override
    public void setChCapacitanceHigh(int chIdx,int dang ,int val){
        chCapacitanceHigh[chIdx * MHO68V2_RATIO_DANG_CNT + dang] = val;
    }


    /**
     * 计算通道系数
     * 
     * @param chIdx 通道索引
     * @param scaleVal 档位值
     * @param idx 系数索引
     * @return 计算出的通道系数
     */
    public double calc_coefChannel(int chIdx,double scaleVal,int idx){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int resistanceType = channel.getResistanceType();
        int refScaleId = getRatioIdx(resistanceType,scaleVal);
        double v = VerticalAxis.getScaleIdValById(getRatioIdx2Dang(resistanceType,refScaleId));

        if(resistanceType == Channel.RESISTANCE_50) {
            v = (coefChannel_50[chIdx][refScaleId][idx] * scaleVal / v);
        }else {
            v = (coefChannel[chIdx][refScaleId][idx] * scaleVal / v);
        }
        return v;
    }

    /**
     * 计算PGA满量程增益
     * 
     * <p>计算PGA增益和ADC满量程配置
     * 
     * @param chIdx 通道索引
     * @param scaleVal 档位值
     * @param result 结果数组 [PGA值, ADC满量程1, ADC满量程2, ...]
     */
    @Override
    public void calc_pga_fs_gain(int chIdx,double scaleVal,int []result){
        int cnt = 2;
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int resistanceType = channel.getResistanceType();
        calc_pga_gain(cnt,chIdx,resistanceType,scaleVal, result);

        int scaleId = VerticalAxis.DANG_NONE;
        for(int i=VerticalAxis.getMinGear();i<=VerticalAxis.getMaxGear();i++){
            if(DoubleUtil.FuzzyCompare(scaleVal,VerticalAxis.getScaleIdValById(i))){
                scaleId = i;
                break;
            }
        }

        if(VerticalAxis.isValidScaleIdExt(scaleId)){
            int [] result_bak = new int[result.length];
            calc_pga_gain(2,chIdx,resistanceType,scaleVal, result_bak);
            result[0] = result_bak[0];
            int N = 4 - (cnt * 2);
            if(N == 0) N = 1;
            for(int i=0;i<N;i++){
                int val = getChGain(chIdx,resistanceType,scaleId,cnt,i);
                result[1 + i] = (result[1 + i] & 0xFFFF0000) | (val & 0xFFFF);
            }
        }
    }
    
    /**
     * 计算PGA增益
     * 
     * <p>根据档位值计算PGA增益和ADC满量程配置
     * 
     * @param cnt 通道数量
     * @param chIdx 通道索引
     * @param resistanceType 阻抗类型
     * @param scaleVal 档位值
     * @param result 结果数组
     */
    @Override
    public void calc_pga_gain(int cnt,int chIdx,int resistanceType,double scaleVal,int []result){

        scaleVal = VerticalAxis.clampMin(scaleVal,resistanceType);

        int dang = getRatioIdx(resistanceType,scaleVal);
        double xs = 1.0;
        if(resistanceType == Channel.RESISTANCE_50){
            dang += getRatioDangCnt();
        }


        int N = 4 - (cnt * 2);
        if(N == 0) N = 1;
        double y = 0;

        for(int i=0;i<N;i++){
            y += 20 * Math.log10(getAdFsD1(chIdx,dang,cnt,i)/scaleVal)/ch_pga_stepdb[chIdx];
        }
        y /= N;

        int p = GAIN_DB[dang];
        y += p;
        int m = (int)Math.round(y);

        if(m  > 26) m  = 26;
        else if(m < -6) m = -6;
        result[0] = 26 - m + 0x200;

        N = 4 - (cnt * 2);
        if(N == 0) N = 1;
        for(int i=0;i<N;i++){
            double db = ch_pga_stepdb[chIdx] * xs;
            y = scaleVal * Math.pow(10,(m - p)/db/20) - getAdFsD1(chIdx,dang,cnt,i);
            result[1 + i] = calcAdfs(y,getAdFsA(chIdx,dang,cnt,i,y < 0 ? 0 : 1)) | getAdOffset(chIdx,cnt,i) << 16;
        }
    }
    
    /**
     * 计算ADC满量程值
     * 
     * @param y 输入值
     * @param a 系数
     * @return ADC满量程值
     */
    private int calcAdfs(double y,double a){
        int m = 0;
        if(Hardware.getInstance().isAdcB12DJ3200NBB()) {
            m = (int) (Math.round(0xA000 + y * a));
            if (m > 0xFFFF) {
                m = 0xFFFF;
            } else if (m < 0x2000) {
                m = 0x2000;
            }
        }else{
            m = (int) (Math.round(0x80 + y * a));
            if (m > 0xFF) {
                m = 0xFF;
            } else if (m < 0) {
                m = 0;
            }
        }
        return m;
    }
    
    /**
     * 设置ADC满量程系数D
     * 
     * @param chIdx 通道索引
     * @param idx 增益组索引
     * @param chMode 通道模式
     * @param adcIdx ADC索引
     * @param val 系数值
     */
    public void setADFsD1(int chIdx,int idx,int chMode,int adcIdx,float val){
        switch (chMode){
            case 0:
                // ad_fs_d_single[chIdx][idx][adcIdx] = val;
                break;
            case 1:
//                    ad_fs_d_dual[chIdx][idx][adcIdx] = val;
                break;
            case 2:
                ad_fs_d[chIdx][idx] = val;
                break;
        }

    }

    /**
     * 获取ADC满量程系数D
     * 
     * @param chIdx 通道索引
     * @param idx 增益组索引
     * @param chMode 通道模式
     * @param adcIdx ADC索引
     * @return 系数值
     */
    public float getAdFsD1(int chIdx,int idx,int chMode,int adcIdx){

        return ad_fs_d[chIdx][idx];
    }


    /**
     * 设置ADC满量程系数A
     * 
     * @param chIdx 通道索引
     * @param Idx 增益组索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @param pn 正负索引
     * @param val 系数值
     */
    public void setAdFsA(int chIdx,int Idx,int chMode,int adIdx,int pn,float val){
        switch (chMode){
            case 0:
//                    ad_fs_a_single[chIdx][Idx][pn][adIdx] = val;
                break;
            case 1:
//                    ad_fs_a_dual[chIdx][Idx][pn][adIdx] = val;
                break;
            case 2:
                ad_fs_a[chIdx][Idx][pn] = val;
                break;
        }
    }

    /**
     * 获取ADC满量程系数A
     * 
     * @param chIdx 通道索引
     * @param Idx 增益组索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @param pn 正负索引
     * @return 系数值
     */
    public float getAdFsA(int chIdx,int Idx,int chMode,int adIdx,int pn){

        return  ad_fs_a[chIdx][Idx][pn];
    }

    /**
     * 设置ADC偏移值
     * 
     * @param chIdx 通道索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @param val 偏移值
     */
    public void setAdOffset(int chIdx,int chMode,int adIdx,int val){

    }
    
    /**
     * 获取ADC偏移值
     * 
     * @param chIdx 通道索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @return 偏移值
     */
    public int getAdOffset(int chIdx,int chMode,int adIdx){

        return 0;
    }
    
    /**
     * 设置通道增益值
     * 
     * @param chIdx 通道索引
     * @param resistanceType 阻抗类型
     * @param vIdx 电压索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @param val 增益值
     */
    public void setChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx,int val){

        chGain[chIdx][vIdx][resistanceType] =  val;
    }

    /**
     * 获取通道增益值
     * 
     * @param chIdx 通道索引
     * @param resistanceType 阻抗类型
     * @param vIdx 电压索引
     * @param chMode 通道模式
     * @param adIdx ADC索引
     * @return 增益值
     */
    public int getChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx){

        return chGain[chIdx][vIdx][resistanceType];
    }

    /**
     * 计算增益系数
     * 
     * <p>根据校准数据计算ADC满量程系数
     * 
     * @param flag 标志位（包含索引和通道选择信息）
     */
    @Override
    public void calcGain(int flag){

        int idx = (flag >>> 24) & 0xFF;
        int chMode = (flag >>> 16) & 0xF;
        flag = flag & 0xFFFF;

        int k = (idx + 1) * 3;

        for(int i=ChannelFactory.CH1;i<ChannelFactory.CH_CNT;i++){

            if((flag & (1<<i)) != 0) {

                switch (chMode){
                    case 0:
                        for(int j=0;j<4;j++) {
                            setAdFsA(i,idx,chMode,j,0,
                                    (float) ((GAIN_ADFS_CODE[k+1] - GAIN_ADFS_CODE[k])/(getGainAdFsD (i, chMode, 1, j) - getGainAdFsD (i, chMode, 0, j))));
                            setAdFsA(i,idx,chMode,j,1,
                                    (float) ((GAIN_ADFS_CODE[k+2] - GAIN_ADFS_CODE[k+1])/(getGainAdFsD (i, chMode, 2, j) - getGainAdFsD (i, chMode, 1, j))));
                            setADFsD1(i,idx,chMode, j, (float)getGainAdFsD (i, chMode, 1, j));
                        }

                        break;
                    case 1:
                        for(int j=0;j<2;j++) {
                            setAdFsA(i,idx,chMode,j,0,
                                    (float) ((GAIN_ADFS_CODE[k+1] - GAIN_ADFS_CODE[k])/(getGainAdFsD (i, chMode, 1, j) - getGainAdFsD (i, chMode, 0, j))));
                            setAdFsA(i,idx,chMode,j,1,
                                    (float) ((GAIN_ADFS_CODE[k+2] - GAIN_ADFS_CODE[k+1])/(getGainAdFsD (i, chMode, 2, j) - getGainAdFsD (i, chMode, 1, j))));

                            setADFsD1(i,idx,chMode, j, (float)getGainAdFsD (i, chMode, 1, j));
                        }
                        break;
                    case 2:
                        setAdFsA(i,idx,chMode,0,0,
                                (float) ((GAIN_ADFS_CODE[k+1] - GAIN_ADFS_CODE[k])/(getGainAdFsD (i, chMode, 1, 0) - getGainAdFsD (i, chMode, 0, 0))));
                        setAdFsA(i,idx,chMode,0,1,
                                (float) ((GAIN_ADFS_CODE[k+2] - GAIN_ADFS_CODE[k+1])/(getGainAdFsD (i, chMode, 2, 0) - getGainAdFsD (i, chMode, 1, 0))));


                        setADFsD1(i,idx,chMode, 0, (float)getGainAdFsD (i, chMode, 1, 0));
                        break;
                }
            }
        }
    }

    /**
     * 计算PGA步进值
     * 
     * <p>根据校准数据计算PGA步进dB值
     * 
     * @param flag 标志位（包含通道选择信息）
     */
    @Override
    public void calcPgaSetp(int flag){
        int maxIdx = ChannelFactory.getMaxChIdx();
        for(int i=ChannelFactory.CH1;i<maxIdx;i++) {

            if ((flag & (1 << i)) != 0) {

                double n1 = 20 * Math.log10(GAIN_INPUT_AMP[2]*1000/gain_pga_stepdb_a3[i]);
                double n2 = 20 * Math.log10(GAIN_INPUT_AMP[1]*1000/gain_pga_stepdb_a2[i]);
                double n3 = 20 * Math.log10(GAIN_INPUT_AMP[0]*1000/gain_pga_stepdb_a1[i]);

                double k = ((n2 + n3) / 2 - (n1 + n2)/2 ) / (GAIN_PGA_CODE[1] - GAIN_PGA_CODE[0]);;
                ch_pga_stepdb[i] = (float)(-k);

            }
        }
    }
    
    /** PGA步进校准值数组1 */
    public double [] gain_pga_stepdb_a1 = new double[ChannelFactory.CH_CNT];
    
    /** PGA步进校准值数组2 */
    public double [] gain_pga_stepdb_a2 = new double[ChannelFactory.CH_CNT];
    
    /** PGA步进校准值数组3 */
    public double [] gain_pga_stepdb_a3 = new double[ChannelFactory.CH_CNT];

    /**
     * 设置PGA步进校准值1
     * 
     * @param chIdx 通道索引
     * @param v 校准值
     */
    public void setGainPgaA1(int chIdx,double v){
        gain_pga_stepdb_a1[chIdx] = v;
    }
    
    /**
     * 设置PGA步进校准值2
     * 
     * @param chIdx 通道索引
     * @param v 校准值
     */
    public void setGainPgaA2(int chIdx,double v){
        gain_pga_stepdb_a2[chIdx] = v;
    }

    /**
     * 设置PGA步进校准值3
     * 
     * @param chIdx 通道索引
     * @param v 校准值
     */
    public void setGainPgaA3(int chIdx,double v){
        gain_pga_stepdb_a3[chIdx] = v;
    }



    /** 增益ADC满量程系数D数组 [通道][通道模式][增益组][ADC索引] */
    public double [][][][] gain_ad_fs_d = new double[ChannelFactory.CH_CNT][3][3][4];

    /**
     * 设置增益ADC满量程系数D
     * 
     * @param chIdx 通道索引
     * @param chMode 通道模式
     * @param idx 增益组索引
     * @param adcIdx ADC索引
     * @param val 系数值
     */
    public void setGainAdFsD(int chIdx,int chMode,int idx,int adcIdx,double val){
        gain_ad_fs_d[chIdx][chMode][idx][adcIdx] = val;
    }
    
    /**
     * 获取增益ADC满量程系数D
     * 
     * @param chIdx 通道索引
     * @param chMode 通道模式
     * @param idx 增益组索引
     * @param adcIdx ADC索引
     * @return 系数值
     */
    public double getGainAdFsD(int chIdx,int chMode,int idx,int adcIdx){
        return gain_ad_fs_d[chIdx][chMode][idx][adcIdx];
    }
}
