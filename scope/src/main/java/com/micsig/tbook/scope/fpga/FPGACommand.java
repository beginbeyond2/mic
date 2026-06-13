package com.micsig.tbook.scope.fpga;  // 包声明：FPGA命令管理模块

import android.os.SystemClock;  // 导入：系统时钟工具类
import android.util.Log;  // 导入：Android日志工具类

import com.micsig.base.DoubleUtil;  // 导入：双精度浮点数工具类
import com.micsig.base.Logger;  // 导入：日志工具类
import com.micsig.tbook.hardware.Hardware;  // 导入：硬件抽象层
import com.micsig.tbook.hardware.HardwareProduct;  // 导入：硬件产品型号定义
import com.micsig.tbook.scope.Action.ChannelHardw;  // 导入：通道硬件控制
import com.micsig.tbook.scope.Auto.FreqCounter;  // 导入：频率计数器
import com.micsig.tbook.scope.BuildConfig;  // 导入：构建配置
import com.micsig.tbook.scope.Bus.ARINC429Bus;  // 导入：ARINC429总线
import com.micsig.tbook.scope.Bus.CanBus;  // 导入：CAN总线
import com.micsig.tbook.scope.Bus.IBus;  // 导入：总线接口
import com.micsig.tbook.scope.Bus.LinBus;  // 导入：LIN总线
import com.micsig.tbook.scope.Calibrate.ADC;  // 导入：ADC芯片驱动抽象基类
import com.micsig.tbook.scope.Calibrate.CabteRegister;  // 导入：校准寄存器管理
import com.micsig.tbook.scope.Calibrate.CalibrateService;  // 导入：校准服务
import com.micsig.tbook.scope.Calibrate.HW;  // 导入：硬件抽象层接口
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入：硬件配置管理
import com.micsig.tbook.scope.Data.SaveBin;  // 导入：二进制保存
import com.micsig.tbook.scope.Data.SyncHeader;  // 导入：同步头管理
import com.micsig.tbook.scope.Display.Display;  // 导入：显示管理
import com.micsig.tbook.scope.Sample.MemDepthFactory;  // 导入：存储深度工厂
import com.micsig.tbook.scope.Sample.Sample;  // 导入：采样管理
import com.micsig.tbook.scope.Sample.SegmentSample;  // 导入：分段采样
import com.micsig.tbook.scope.Scope;  // 导入：示波器核心管理
import com.micsig.tbook.scope.ScopeBase;  // 导入：示波器基础配置
import com.micsig.tbook.scope.ScopeFrozen;  // 导入：示波器冻结状态
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入：触发基类
import com.micsig.tbook.scope.Trigger.TriggerCommon;  // 导入：触发通用设置
import com.micsig.tbook.scope.Trigger.TriggerEdge;  // 导入：边沿触发
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入：触发工厂
import com.micsig.tbook.scope.Trigger.TriggerLogic;  // 导入：逻辑触发
import com.micsig.tbook.scope.Trigger.TriggerNEdge;  // 导入：非边沿触发
import com.micsig.tbook.scope.Trigger.TriggerPulseWidth;  // 导入：脉宽触发
import com.micsig.tbook.scope.Trigger.TriggerRunt;  // 导入：矮脉冲触发
import com.micsig.tbook.scope.Trigger.TriggerSlope;  // 导入：斜率触发
import com.micsig.tbook.scope.Trigger.TriggerTimeOut;  // 导入：超时触发
import com.micsig.tbook.scope.Trigger.TriggerVideo;  // 导入：视频触发
import com.micsig.tbook.scope.channel.Channel;  // 导入：物理通道
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入：通道工厂
import com.micsig.tbook.scope.channel.MathChannel;  // 导入：数学通道
import com.micsig.tbook.scope.channel.SerialChannel;  // 导入：串行通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入：水平轴管理
import com.micsig.tbook.scope.probe.BaseProbe;  // 导入：探头基类
import com.micsig.tbook.scope.probe.ProbeFactory;  // 导入：探头工厂
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入：垂直轴管理

import java.math.BigInteger;  // 导入：大整数类
import java.nio.ByteBuffer;  // 导入：字节缓冲区
import java.util.Arrays;  // 导入：数组工具类

/**
 * FPGA命令管理类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（FPGA命令管理模块）</li>
 *   <li>架构层级：硬件控制层 - FPGA命令生成与发送</li>
 *   <li>设计模式：单例模式，统一管理所有FPGA寄存器操作</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理所有FPGA寄存器的创建和配置</li>
 *   <li>生成并发送FPGA控制命令</li>
 *   <li>控制采样模式、触发、显示等硬件功能</li>
 *   <li>管理通道位置、增益、电容等参数</li>
 *   <li>支持多FPGA架构（MHO68双FPGA）</li>
 * </ul>
 * 
 * <p><b>FPGA寄存器分类：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 类别           │ 寄存器示例              │ 功能说明           │
 * ├────────────────┼─────────────────────────┼────────────────────┤
 * │ 采样控制       │ SAMPLE_MODE, ZUN_DEPTH  │ 采样模式、存储深度 │
 * │ 触发控制       │ TRIG_MODE, TRIG_LEVEL   │ 触发模式、触发电平 │
 * │ 显示控制       │ DISP_CHA, DISP_WAVE     │ 插值、波形显示     │
 * │ 通道控制       │ CH_Y_PLACE, CH_OFFSET   │ 位置、偏移         │
 * │ 总线解码       │ BUS_TYPE, BUS_LEVEL     │ 总线类型、电平     │
 * │ 校准相关       │ AD_ZERO, CH_CAPACITANCE │ ADC零点、通道电容  │
 * └────────────────┴─────────────────────────┴────────────────────┘
 * </pre>
 * 
 * <p><b>多FPGA架构说明：</b>
 * <pre>
 * MHO38 V1: 单FPGA架构
 *   └── fpgaIdx = 0，管理4个通道
 * 
 * MHO68 V1/V2: 双FPGA架构
 *   ├── fpgaIdx = 0，管理CH1-CH2
 *   └── fpgaIdx = 1，管理CH3-CH4
 * </pre>
 * 
 * <p><b>关键方法命名规则：</b>
 * <pre>
 * gntR_xxx(): 生成FPGA寄存器配置（Generate Register）
 * sendCommand(): 发送命令到FPGA
 * runCommand(): 执行命令并发送
 * updataReg(): 更新寄存器到硬件
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：Hardware（硬件抽象层）</li>
 *   <li>依赖：Scope（示波器核心）</li>
 *   <li>依赖：HwConfig（硬件配置）</li>
 *   <li>依赖：ADC（ADC芯片驱动）</li>
 *   <li>依赖：FPGAReg（FPGA寄存器基类）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>系统初始化时配置FPGA寄存器</li>
 *   <li>通道参数变化时更新FPGA配置</li>
 *   <li>触发模式切换时更新触发配置</li>
 *   <li>校准时配置特殊参数</li>
 * </ul>
 * 
 * @author zhuzh  // 作者：zhuzh
 * @version 1.0  // 版本号：1.0
 * @since 2018/3/13  // 创建日期：2018年3月13日
 * @see FPGAReg FPGA寄存器基类  // 参见：FPGAReg类
 * @see Hardware 硬件抽象层  // 参见：Hardware类
 * @see ADC ADC芯片驱动  // 参见：ADC类
 */
/** Created by zhuzh on 2018/3/13. */
public class FPGACommand {  // FPGA命令管理类
    
    /**
     * 日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于日志输出时标识FPGACommand类</li>
     *   <li>便于日志过滤和调试</li>
     * </ul>
     */
    public static final String TAG = "FPGACommand";  // 日志标签，固定为"FPGACommand"
    
    /**
     * 单例实例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>FPGACommand的全局唯一实例</li>
     *   <li>使用volatile保证多线程可见性</li>
     *   <li>使用双重检查锁定保证线程安全</li>
     * </ul>
     */
    private static volatile FPGACommand instance = null;  // 单例实例，volatile保证可见性

    /**
     * 获取单例实例
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>使用双重检查锁定保证线程安全</li>
     *   <li>延迟初始化，减少内存占用</li>
     * </ul>
     * 
     * @return FPGACommand单例实例
     */
    public static FPGACommand getInstance() {  // 获取单例实例方法
        if (instance == null) {  // 判断：实例是否为null
            synchronized (FPGACommand.class) {  // 同步锁，保证线程安全
                if (instance == null) {  // 判断：实例是否为null（双重检查）
                    instance = new FPGACommand();  // 创建新实例
                }  // 判断结束
            }  // 同步块结束
        }  // 判断结束
        return instance;  // 返回单例实例
    }  // 方法结束

    /**
     * FPGA寄存器数量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义每个FPGA支持的寄存器数量上限</li>
     *   <li>用于初始化寄存器数组</li>
     * </ul>
     */
    private static final int FPGA_REG_COUNT = 256;  // FPGA寄存器数量，固定为256
    
    /**
     * FPGA寄存器数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储所有FPGA寄存器实例</li>
     *   <li>二维数组：[fpgaIdx][regIdx]</li>
     *   <li>支持多FPGA架构</li>
     * </ul>
     */
    private FPGAReg[][] fpgaRegs;  // FPGA寄存器二维数组
    
    /**
     * 硬件抽象层实例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于发送FPGA命令到硬件</li>
     *   <li>提供底层硬件访问接口</li>
     * </ul>
     */
    private Hardware mHw;  // 硬件抽象层实例
    
    /**
     * 示波器核心实例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于获取示波器状态和参数</li>
     *   <li>提供采样率、时基等信息</li>
     * </ul>
     */
    private Scope scope;  // 示波器核心实例

    /**
     * 硬件配置实例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于获取硬件配置参数</li>
     *   <li>提供ADC数量、通道数等信息</li>
     * </ul>
     */
    private HwConfig hwConfig;  // 硬件配置实例
    
    /**
     * 校准标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>true表示正在进行校准</li>
     *   <li>校准期间某些参数需要特殊处理</li>
     * </ul>
     */
    private boolean bCalibrate = false;  // 校准标志，初始为false，注释：用于校准
    
    /**
     * 校准类型
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标识当前正在进行的校准类型</li>
     *   <li>-1表示未校准</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * CalibrateService.ZERO_CALIBRATE: 零点校准
     * CalibrateService.CH_ZERO_CALIBRATE: 通道零点校准
     * CalibrateService.CHGAIN_CALIBRATE: 通道增益校准
     * CalibrateService.AD_OFFSET_CALIBRATE: ADC偏移校准
     * </pre>
     */
    private int CalibrateType = -1;  // 校准类型，初始为-1（未校准）
    
    /**
     * PGA值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准的PGA增益值</li>
     *   <li>用于校准过程中记录PGA设置</li>
     * </ul>
     */
    private int pgaVal;  // PGA值
    
    /**
     * ADC差分增益校准标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>true表示正在进行ADC差分增益校准</li>
     *   <li>校准时需要设置特殊参数</li>
     * </ul>
     */
    private boolean bCalibrate_ad_diffGain = false;  // ADC差分增益校准标志，初始为false，注释：AD增益校准时需要设置为1

    /**
     * 设置ADC差分增益校准标志
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置ADC差分增益校准状态</li>
     *   <li>校准时由校准模块调用</li>
     * </ul>
     * 
     * @param bx true表示启用ADC差分增益校准
     */
    public void setADdiffGainCalib(boolean bx) {  // 设置ADC差分增益校准标志方法
        bCalibrate_ad_diffGain = bx;  // 保存标志值
    }  // 方法结束

    /**
     * 私有构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>单例模式，私有构造函数</li>
     *   <li>初始化硬件引用和寄存器数组</li>
     * </ul>
     * 
     * <p><b>初始化流程：</b>
     * <pre>
     * 1. 获取Hardware实例
     * 2. 获取Scope实例
     * 3. 获取HwConfig实例
     * 4. 调用init()初始化寄存器
     * </pre>
     */
    private FPGACommand() {  // 私有构造函数
        mHw = Hardware.getInstance();  // 获取硬件抽象层实例
        scope = Scope.getInstance();  // 获取示波器核心实例
        hwConfig = HwConfig.getInstance();  // 获取硬件配置实例
        init();  // 初始化寄存器
    }  // 构造函数结束

    /**
     * 设置校准标志
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置校准状态标志</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @param flag true表示正在校准
     */
    public synchronized void setFlagCalibrate(boolean flag) {  // 设置校准标志方法
        bCalibrate = flag;  // 保存校准标志
    }  // 方法结束

    /**
     * 设置校准类型
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置当前校准类型</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @param calibrateType 校准类型（CalibrateService常量）
     */
    public synchronized void setCalibrateType(int calibrateType) {  // 设置校准类型方法
        this.CalibrateType = calibrateType;  // 保存校准类型
    }  // 方法结束
    
    /**
     * 获取校准类型
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回当前校准类型</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return 校准类型
     */
    public synchronized int getCalibrateType(){  // 获取校准类型方法
        return this.CalibrateType;  // 返回校准类型
    }  // 方法结束
    
    /**
     * 判断是否为零点校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查当前校准类型是否为零点相关校准</li>
     *   <li>包括零点校准、通道零点校准、ADC偏移校准</li>
     * </ul>
     * 
     * @return true表示是零点校准
     */
    public synchronized boolean isZeroCalibrate(){  // 判断是否为零点校准方法
        switch (this.CalibrateType){  // 根据校准类型判断
            case CalibrateService.ZERO_CALIBRATE:  // 零点校准
            case CalibrateService.CH_ZERO_CALIBRATE:  // 通道零点校准
            case CalibrateService.AD_OFFSET_CALIBRATE:  // ADC偏移校准
                return true;  // 返回true，是零点校准
            default:  // 其他类型
                return false;  // 返回false，不是零点校准

        }  // switch结束
    }  // 方法结束
    
    /**
     * 判断是否为通道增益校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查当前校准类型是否为通道增益校准</li>
     * </ul>
     * 
     * @return true表示是通道增益校准
     */
    public synchronized boolean isChGainCalibrate(){  // 判断是否为通道增益校准方法
        return CalibrateType == CalibrateService.CHGAIN_CALIBRATE;  // 返回判断结果
    }  // 方法结束

    /**
     * 设置PGA值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置当前校准的PGA值</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @param pgaVal PGA值
     */
    public synchronized void setPgaVal(int pgaVal) {  // 设置PGA值方法
        this.pgaVal = pgaVal;  // 保存PGA值
    }  // 方法结束

    /**
     * 获取PGA值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回当前校准的PGA值</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return PGA值
     */
    public synchronized int getPgaVal(){  // 获取PGA值方法
        return this.pgaVal;  // 返回PGA值
    }  // 方法结束
    
    /**
     * 判断是否正在校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回校准状态标志</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return true表示正在校准
     */
    public synchronized boolean isCalibrate() {  // 判断是否正在校准方法
        return bCalibrate;  // 返回校准标志
    }  // 方法结束

    /**
     * ADC实例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>ADC芯片驱动实例</li>
     *   <li>用于控制ADC采样参数</li>
     * </ul>
     */
    private ADC adc;  // ADC实例

    /**
     * FPGA数量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>系统中的FPGA芯片数量</li>
     *   <li>MHO38 V1: 1个FPGA</li>
     *   <li>MHO68 V1/V2: 2个FPGA</li>
     * </ul>
     */
    private int fpgaNums = 1;  // FPGA数量，初始为1

    /**
     * ADC最大通道数
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>单个ADC芯片支持的最大通道数</li>
     *   <li>用于通道映射计算</li>
     * </ul>
     */
    private int maxAdcChNums = 2;  // ADC最大通道数，初始为2

    /**
     * ADC数量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>系统中的ADC芯片数量</li>
     *   <li>用于采样率计算</li>
     * </ul>
     */
    private int adcNums = 2;  // ADC数量，初始为2

    /**
     * 初始化FPGA寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取硬件配置参数</li>
     *   <li>创建所有FPGA寄存器实例</li>
     *   <li>设置寄存器的FPGA索引</li>
     * </ul>
     * 
     * <p><b>初始化流程：</b>
     * <pre>
     * 1. 获取FPGA数量、ADC数量等配置
     * 2. 创建寄存器二维数组
     * 3. 为每个FPGA创建所有寄存器实例
     * 4. 设置寄存器的FPGA索引
     * </pre>
     */
    private void init() {  // 初始化方法
        fpgaNums = hwConfig.getFpgaNums();  // 获取FPGA数量
        maxAdcChNums = hwConfig.getAdcMaxChNums();  // 获取ADC最大通道数
        adcNums = hwConfig.getAdcNums();  // 获取ADC数量
        fpgaRegs = new FPGAReg[fpgaNums][FPGA_REG_COUNT];  // 创建寄存器二维数组
        adc = hwConfig.getAdc();  // 获取ADC实例
        adc.setFpgaCommand(this);  // 设置ADC的FPGA命令引用
        for (int i = 0; i < fpgaNums; i++) {  // 循环：遍历所有FPGA
            fpgaRegs[i][FPGAReg.FPGA_SAMPLE_MODE] = new FPGAReg_SAMPLE_MODE();  // 创建采样模式寄存器
            fpgaRegs[i][FPGAReg.FPGA_PJBL_TIMES] = new FPGAReg_PJBL_TIMES();  // 创建平均/包络次数寄存器
            fpgaRegs[i][FPGAReg.FPGA_ZUN_DEPTH] = new FPGAReg_ZUN_DEPTH();  // 创建存储深度寄存器
            fpgaRegs[i][FPGAReg.FPGA_PRE_SAMP] = new FPGAReg_PRE_SAMP();  // 创建预采样寄存器
            fpgaRegs[i][FPGAReg.FPGA_SCROLL_ROW] = new FPGAReg_SCROLL_ROW();  // 创建滚屏行数寄存器
            fpgaRegs[i][FPGAReg.FPGA_SLOW_SCALE_SET] = new FPGAReg_SLOW_SCALE_SET();  // 创建慢时基设置寄存器
            fpgaRegs[i][FPGAReg.FPGA_NUM_CY12] = new FPGAReg_NUM_CY12();  // 创建抽样系数寄存器
            fpgaRegs[i][FPGAReg.FPGA_CY_BUCONG] = new FPGAReg_CY_BUCONG();  // 创建抽样补偿寄存器
            fpgaRegs[i][FPGAReg.FPGA_SERIAL_DEC_DEPTH] = new FPGAReg_SERIAL_DEC_DEPTH();  // 创建串行解码深度寄存器
            fpgaRegs[i][FPGAReg.FPGA_CH_Y_PLACE] = new FPGAReg_CH_Y_PLACE();  // 创建通道Y位置寄存器
            fpgaRegs[i][FPGAReg.FPGA_CH_Y_PLACE_BC] = new FPGAReg_CH_Y_PLACE_BC();  // 创建通道Y位置补偿寄存器
            fpgaRegs[i][FPGAReg.FPGA_TRIG_MODE] = new FPGAReg_TRIG_MODE();  // 创建触发模式寄存器
            fpgaRegs[i][FPGAReg.FPGA_TRIG_AUTO_TRIG_TIME] = new FPGAReg_TRIG_AUTO_TRIG_TIME();  // 创建自动触发时间寄存器
            fpgaRegs[i][FPGAReg.FPGA_TRIG_RESTRAIN_TIME] = new FPGAReg_TRIG_RESTRAIN_TIME();  // 创建触发抑制时间寄存器
            fpgaRegs[i][FPGAReg.FPGA_TRIG_LEVEL] = new FPGAReg_TRIG_LEVEL();  // 创建触发电平寄存器
            fpgaRegs[i][FPGAReg.FPGA_TRIG_COUPLE] = new FPGAReg_TRIG_COUPLE();  // 创建触发耦合寄存器
            fpgaRegs[i][FPGAReg.FPGA_DIS_MODE] = new FPGAReg_DIS_MODE();  // 创建显示模式寄存器
            fpgaRegs[i][FPGAReg.FPGA_SEGMENT_FRAME] = new FPGAReg_SegmentFrame();  // 创建分段帧数寄存器
            fpgaRegs[i][FPGAReg.FPGA_SEGMENT_NUMS] = new FPGAReg_SEGMENT_NUMS();  // 创建分段数寄存器
            fpgaRegs[i][FPGAReg.FPGA_SEGMENT_START] = new FPGAReg_SEGMENT_START();  // 创建分段启动寄存器
            fpgaRegs[i][FPGAReg.FPGA_DISP_CHA1] = new FPGAReg_DISP_CHA();  // 创建显示插值寄存器
            fpgaRegs[i][FPGAReg.FPGA_DISP_SL] = new FPGAReg_DISP_SL();  // 创建缩略视图寄存器
            fpgaRegs[i][FPGAReg.FPGA_DISP_PLACE_MAIN] = new FPGAReg_DISP_PLACE_MAIN();  // 创建主视图位置寄存器
            fpgaRegs[i][FPGAReg.FPGA_DISP_WAVE] = new FPGAReg_DISP_WAVE();  // 创建波形显示寄存器
            fpgaRegs[i][FPGAReg.FPGA_DISP_PLACE_SL] = new FPGAReg_DISP_PLACE_SL();  // 创建缩略视图位置寄存器
            fpgaRegs[i][FPGAReg.FPGA_DISP_SERI] = new FPGAReg_DISP_SERI();  // 创建串行显示寄存器
            fpgaRegs[i][FPGAReg.FPGA_AD_ZERO] = new FPGAReg_AD_ZERO();  // 创建ADC零点寄存器
            fpgaRegs[i][FPGAReg.FPGA_CH_OFFSET_DA12] = new FPGAReg_CH_OFFSET_DA12();  // 创建通道偏移DA寄存器
            fpgaRegs[i][FPGAReg.FPGA_DOT_MATRIX] = new FPGAReg_DOT_MATRIX();  // 创建点阵寄存器
            fpgaRegs[i][FPGAReg.FPGA_COMMAND] = new FPGAReg_COMMAND();  // 创建命令寄存器
            fpgaRegs[i][FPGAReg.FPGA_CH_DISPLAY] = new FPGAReg_CH_DISPLAY();  // 创建通道显示寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS_TYPE] = new FPGAReg_BUS_TYPE();  // 创建总线类型寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS_LEVEL] = new FPGAReg_BUS_LEVEL();  // 创建总线电平寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS_PRIMARY] = new FPGAReg_BUS_PRIMARY();  // 创建主总线寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS_SECONDARY] = new FPGAReg_BUS_SECONDARY();  // 创建次总线寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS_PRIMARY_EXT] = new FPGAReg_BUS_PRIMARY_EXT();  // 创建主总线扩展寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS_SECONDARY_EXT] = new FPGAReg_BUS_SECONDARY_EXT();  // 创建次总线扩展寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS1_ADDR] = new FPGAReg_BUS_ADDR(FPGAReg.FPGA_BUS1_ADDR);  // 创建总线1地址寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS2_ADDR] = new FPGAReg_BUS_ADDR(FPGAReg.FPGA_BUS2_ADDR);  // 创建总线2地址寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS1_CAN_EXT] = new FPGAReg_BUS_CAN_EXT(FPGAReg.FPGA_BUS1_CAN_EXT);  // 创建总线1 CAN扩展寄存器
            fpgaRegs[i][FPGAReg.FPGA_BUS2_CAN_EXT] = new FPGAReg_BUS_CAN_EXT(FPGAReg.FPGA_BUS2_CAN_EXT);  // 创建总线2 CAN扩展寄存器
            fpgaRegs[i][FPGAReg.FPGA_CH_RESISTANCE] = new FPGAReg_Resistance();  // 创建通道电阻寄存器
            fpgaRegs[i][FPGAReg.FPGA_EXT_GRIGGER_LEVEL] = new FPGAReg_ExtTriggerLevel();  // 创建外部触发电平寄存器
            fpgaRegs[i][FPGAReg.FPGA_SPI_EXT] = new FPGAReg_SPI_EXT();  // 创建SPI扩展寄存器
            fpgaRegs[i][FPGAReg.FPGA_DISP_RESOLUTION] = new FPGAReg_DISP_RESOLUTION();  // 创建显示分辨率寄存器
            fpgaRegs[i][FPGAReg.FPGA_SIGNAL_FREQ] = new FPGAReg_SIGNAL_FREQ();  // 创建信号频率寄存器
            fpgaRegs[i][FPGAReg.FPGA_CH_CAPACITANCE1] = new FPGAReg_CH_CAPACITANCE();  // 创建通道电容寄存器
            fpgaRegs[i][FPGAReg.FPGA_PROBE_DA12] = new FPGAReg_PROBE_DA12();  // 创建探头DA寄存器
//            fpgaRegs[i][FPGAReg.FPGA_TRIG_OFFSET1] = new FPGAReg_TRIG_OFFSET();  // 已注释：触发偏移寄存器
            fpgaRegs[i][FPGAReg.FPGA_TEMPERATURE] = new FPGAReg_TEMPERATURE();  // 创建温度寄存器
            fpgaRegs[i][FPGAReg.FPGA_FAN_SPEED] = new FPGAReg_FanSpeed();  // 创建风扇转速寄存器
            fpgaRegs[i][FPGAReg.FPGA_CH_DSIPLAY_POS] = new FPGAReg_CH_DISPLAY_POS();  // 创建通道显示位置寄存器
            fpgaRegs[i][FPGAReg.FPGA_ZOOM_SMALL_PLACE] = new FPGAReg_ZoomSmallPlace();  // 创建缩放小窗口位置寄存器
            fpgaRegs[i][FPGAReg.FPGA_CH_CAPACITANCE1_16BIT] = new FPGAReg_CH_CAPACITANCE_16BIT();  // 创建通道电容16位寄存器
            fpgaRegs[i][FPGAReg.FPGA_CH_CAP_DA12] = new FPGAReg_CH_CAP_DA();  // 创建通道电容DA寄存器
            for (int j = 0; j < FPGA_REG_COUNT; j++) {  // 循环：遍历所有寄存器
                if (fpgaRegs[i][j] != null) {  // 判断：寄存器是否已创建
                    fpgaRegs[i][j].setFpgaIdx(i);  // 设置寄存器的FPGA索引
                }  // 判断结束
            }  // 循环结束
        }  // 循环结束
    }  // 方法结束

    /**
     * 判断通道索引是否属于指定FPGA
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查通道索引是否在指定FPGA的管理范围内</li>
     *   <li>用于多FPGA架构下的通道归属判断</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param chIdx 通道索引
     * @return true表示通道属于该FPGA
     */
    public static boolean isFpgaChIdx(int fpgaIdx, int chIdx){  // 判断通道索引是否属于指定FPGA方法
        return chIdx >= beginChIdx(fpgaIdx) && chIdx < endChIdx(fpgaIdx);  // 返回判断结果
    }  // 方法结束

    /**
     * 获取FPGA管理的起始通道索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算指定FPGA管理的第一个通道索引</li>
     *   <li>用于多FPGA架构下的通道映射</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 起始通道 = CH1 + fpgaIdx × (通道数 / 2)
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     * @return 起始通道索引
     */
    public static int beginChIdx(int fpgaIdx) {  // 获取FPGA管理的起始通道索引方法
        return ChannelFactory.CH1 + fpgaIdx * ChannelFactory.CH_CNT / 2;  // 返回起始通道索引
    }  // 方法结束

    /**
     * 获取FPGA管理的结束通道索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算指定FPGA管理的最后一个通道索引（不包含）</li>
     *   <li>用于多FPGA架构下的通道映射</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 结束通道 = CH1 + (fpgaIdx + 1) × (通道数 / 2)
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     * @return 结束通道索引（不包含）
     */
    public static int endChIdx(int fpgaIdx) {  // 获取FPGA管理的结束通道索引方法
        return ChannelFactory.CH1 + (fpgaIdx + 1) * ChannelFactory.CH_CNT / 2;  // 返回结束通道索引
    }  // 方法结束

    /**
     * 获取FPGA管理的起始串行通道索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算指定FPGA管理的第一个串行通道索引</li>
     *   <li>用于多FPGA架构下的串行通道映射</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return 起始串行通道索引
     */
    public static int beginSerialIdx(int fpgaIdx) {  // 获取FPGA管理的起始串行通道索引方法
        return ChannelFactory.S1 + fpgaIdx * ChannelFactory.SERIAL_CNT / 2;  // 返回起始串行通道索引
    }  // 方法结束

    /**
     * 获取FPGA管理的结束串行通道索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算指定FPGA管理的最后一个串行通道索引（不包含）</li>
     *   <li>用于多FPGA架构下的串行通道映射</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return 结束串行通道索引（不包含）
     */
    public static int endSerialIdx(int fpgaIdx) {  // 获取FPGA管理的结束串行通道索引方法
        return ChannelFactory.S1 + (fpgaIdx + 1) * ChannelFactory.SERIAL_CNT / 2;  // 返回结束串行通道索引
    }  // 方法结束

    /**
     * 根据通道索引获取FPGA索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算通道所属的FPGA索引</li>
     *   <li>用于多FPGA架构下的通道归属判断</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * FPGA索引 = (通道索引 - CH1) / (通道数 / 2)
     * </pre>
     * 
     * @param chIdx 通道索引
     * @return FPGA索引
     */
    public static int chIdxToFpgaIdx(int chIdx) {  // 根据通道索引获取FPGA索引方法
        return (chIdx - ChannelFactory.CH1) / (ChannelFactory.CH_CNT / 2);  // 返回FPGA索引
    }  // 方法结束

    /**
     * 根据串行通道索引获取FPGA索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算串行通道所属的FPGA索引</li>
     *   <li>用于多FPGA架构下的串行通道归属判断</li>
     * </ul>
     * 
     * @param serialIdx 串行通道索引
     * @return FPGA索引
     */
    public static int serialIdxToFpgaIdx(int serialIdx) {  // 根据串行通道索引获取FPGA索引方法
        return (serialIdx - ChannelFactory.S1) / (ChannelFactory.SERIAL_CNT / 2);  // 返回FPGA索引
    }  // 方法结束

    /**
     * 更新通道电容配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据校准数据更新通道电容配置</li>
     *   <li>设置16位电容寄存器和DA输出</li>
     *   <li>用于补偿通道输入电容</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 获取校准寄存器实例
     * 2. 遍历FPGA管理的所有通道
     * 3. 根据档位获取电容校准值
     * 4. 设置电容寄存器和DA输出
     * 5. 发送命令并更新寄存器
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void updateChCapacitance(int fpgaIdx) {  // 更新通道电容配置方法
        CabteRegister cabteRegister = CabteRegister.getInstance();  // 获取校准寄存器实例
        FPGAReg_CH_CAPACITANCE_16BIT regChCapacitance16BIT =  // 获取16位电容寄存器
                (FPGAReg_CH_CAPACITANCE_16BIT)
                        getFPGAReg(fpgaIdx, FPGAReg.FPGA_CH_CAPACITANCE1_16BIT);
        FPGAReg_CH_CAPACITANCE reg =  // 获取电容寄存器
                (FPGAReg_CH_CAPACITANCE) getFPGAReg(fpgaIdx, FPGAReg.FPGA_CH_CAPACITANCE1);
        FPGAReg_CH_CAP_DA regChCapDa = (FPGAReg_CH_CAP_DA)getFPGAReg(fpgaIdx,FPGAReg.FPGA_CH_CAP_DA12);  // 获取电容DA寄存器
        Channel channel = null;  // 通道对象
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        int dwidx = 0;  // 档位索引
        int [] daChMap = {1,3,2,0};  // DA通道映射表
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
            if (channel != null) {  // 判断：通道是否有效
                dwidx = CabteRegister.getRatioIdx(channel.getResistanceType(),channel.getVScaleVal() / channel.getProbeRate());  // 计算档位索引
                int xvv = cabteRegister.getChCapacitanceHigh(i,dwidx);  // 获取电容校准值
                regChCapacitance16BIT.setHigh(i-beginIdx, xvv);  // 设置16位电容寄存器
                regChCapDa.setHigh(daChMap[i-beginIdx], xvv);  // 设置电容DA输出
                reg.setHigh(i-beginIdx, xvv);  // 设置电容寄存器
                reg.setTotal(i-beginIdx, 0xFF);  // 设置总电容值
            }  // 判断结束
        }  // 循环结束
        sendCommand(regChCapDa);  // 发送电容DA命令
        sendCommand(regChCapacitance16BIT);  // 发送16位电容命令
        sendCommand(reg);  // 发送电容命令
        updataReg(fpgaIdx);  // 更新寄存器到硬件
    }  // 方法结束

    /**
     * 恢复初始状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>重置所有缓存的状态变量</li>
     *   <li>用于系统恢复或重新初始化</li>
     * </ul>
     */
    public void resume() {  // 恢复初始状态方法
        Log.d("MHO68V2","resume" );  // 输出调试日志
        Arrays.fill(vol2A_last, -1);  // 重置电压A缓存
        Arrays.fill(vol2B_last, -1);  // 重置电压B缓存
        Arrays.fill(vScaleLast, -1);  // 重置档位缓存
        coef_fpga_mho_bak[0] = null;  // 清空FPGA系数备份0
        coef_fpga_mho_bak[1] = null;  // 清空FPGA系数备份1
        coef_seach_mho_bak[0] = null;  // 清空搜索系数备份0
        coef_seach_mho_bak[1] = null;  // 清空搜索系数备份1
        Arrays.fill(Val0x31_last, -1);  // 重置0x31寄存器缓存
        Arrays.fill(Val0x3A_last, -1);  // 重置0x3A寄存器缓存
        Arrays.fill(Val0x3B_last, -1);  // 重置0x3B寄存器缓存
        Arrays.fill(cnt_last, -1);  // 重置计数缓存
        Arrays.fill(sampleChState,false);  // 重置采样通道状态
        for (int i = 0; i < fpgaNums; i++) {  // 循环：遍历所有FPGA
            FPGAReg_DOT_MATRIX reg = (FPGAReg_DOT_MATRIX) getReg(i, FPGAReg.FPGA_DOT_MATRIX);  // 获取点阵寄存器
            reg.resetBak();  // 重置寄存器备份
        }  // 循环结束
    }  // 方法结束

    /**
     * 静态方法：发送命令
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>便捷方法，通过单例发送命令</li>
     *   <li>无需获取实例即可调用</li>
     * </ul>
     * 
     * @param reg FPGA寄存器对象
     */
    public static void sendCmd(FPGAReg reg) {  // 静态方法：发送命令
        getInstance().sendCommand(reg);  // 调用实例方法发送命令
    }  // 方法结束

    /**
     * 静态方法：获取寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>便捷方法，通过单例获取寄存器</li>
     *   <li>无需获取实例即可调用</li>
     * </ul>
     * 
     * @param idx FPGA索引
     * @param regIdx 寄存器索引
     * @return FPGA寄存器对象
     */
    public static FPGAReg getReg(int idx, int regIdx) {  // 静态方法：获取寄存器
        return getInstance().getFPGAReg(idx, regIdx);  // 调用实例方法获取寄存器
    }  // 方法结束

    /**
     * 获取FPGA寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据索引获取FPGA寄存器对象</li>
     *   <li>检查索引有效性</li>
     * </ul>
     * 
     * @param idx FPGA索引
     * @param regIdx 寄存器索引
     * @return FPGA寄存器对象，无效索引返回null
     */
    public FPGAReg getFPGAReg(int idx, int regIdx) {  // 获取FPGA寄存器方法
        if(fpgaRegs.length > idx && fpgaRegs[idx].length > regIdx) {  // 判断：索引是否有效
            return fpgaRegs[idx][regIdx];  // 返回寄存器对象
        }  // 判断结束
        return null;  // 返回null
    }  // 方法结束

    /**
     * 执行命令并发送
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用寄存器的onCommand方法</li>
     *   <li>发送命令到硬件</li>
     * </ul>
     * 
     * @param idx FPGA索引
     * @param regIdx 寄存器索引
     */
    public void runCommand(int idx, int regIdx) {  // 执行命令并发送方法
        FPGAReg reg = getFPGAReg(idx, regIdx);  // 获取寄存器对象
        if (reg != null) {  // 判断：寄存器是否有效
            reg.onCommand();  // 调用寄存器的命令处理方法
            mHw.sendFpgaCmd(reg.getFpgaIdx(), reg.getCommand(), reg.getCommandLength());  // 发送命令到硬件
        }  // 判断结束
    }  // 方法结束

    /**
     * 发送命令（指定长度）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送指定长度的命令到硬件</li>
     *   <li>用于需要自定义命令长度的场景</li>
     * </ul>
     * 
     * @param reg FPGA寄存器对象
     * @param len 命令长度
     */
    public void sendCommand(FPGAReg reg, int len) {  // 发送命令方法（指定长度）
        if (reg != null) {  // 判断：寄存器是否有效
            mHw.sendFpgaCmd(reg.getFpgaIdx(), reg.getCommand(), len);  // 发送指定长度的命令
        }  // 判断结束
    }  // 方法结束

    /**
     * 发送命令
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送命令到硬件</li>
     *   <li>使用寄存器默认长度</li>
     * </ul>
     * 
     * @param reg FPGA寄存器对象
     */
    public void sendCommand(FPGAReg reg) {  // 发送命令方法
        sendCommand(reg, reg.getCommandLength());  // 调用重载方法发送命令
    }  // 方法结束

    /**
     * 接收命令（写入寄存器，读取到寄存器）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送写入命令，读取数据到指定寄存器</li>
     *   <li>用于从FPGA读取数据</li>
     * </ul>
     * 
     * @param w_reg 写入寄存器
     * @param r_reg 读取寄存器
     */
    public void recvCommand(FPGAReg w_reg, FPGAReg r_reg) {  // 接收命令方法
        if (w_reg != null && r_reg != null) {  // 判断：寄存器是否有效
            mHw.recvFpgaCmd(  // 调用硬件方法接收命令
                    w_reg.getFpgaIdx(),  // FPGA索引
                    w_reg.getCommand(),  // 写入命令
                    w_reg.getCommandLength(),  // 命令长度
                    r_reg.getCommand());  // 读取缓冲区
        }  // 判断结束
    }  // 方法结束

    /**
     * 接收命令（写入寄存器，读取到ByteBuffer）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送写入命令，读取数据到ByteBuffer</li>
     *   <li>用于从FPGA读取大量数据</li>
     * </ul>
     * 
     * @param w_reg 写入寄存器
     * @param byteBuffer 字节缓冲区
     */
    public void recvCommand(FPGAReg w_reg, ByteBuffer byteBuffer) {  // 接收命令方法
        if (w_reg != null && byteBuffer != null) {  // 判断：参数是否有效
            mHw.recvFpgaCmd(  // 调用硬件方法接收命令
                    w_reg.getFpgaIdx(), w_reg.getCommand(), w_reg.getCommandLength(), byteBuffer);  // 参数
        }  // 判断结束
    }  // 方法结束

    /**
     * 发送分段采样命令
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送分段启动和分段数命令</li>
     *   <li>用于分段采样模式</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdSegment(int fpgaIdx) {  // 发送分段采样命令方法
        runCommand(fpgaIdx, FPGAReg.FPGA_SEGMENT_START);  // 发送分段启动命令
        runCommand(fpgaIdx, FPGAReg.FPGA_SEGMENT_NUMS);  // 发送分段数命令
    }  // 方法结束

    /**
     * 时钟输出状态
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录上次的时钟输出状态</li>
     *   <li>用于检测状态变化</li>
     * </ul>
     */
    boolean bClkOut = false;  // 时钟输出状态，初始为false
    
    /**
     * 通道电源使能状态
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录通道电源是否已使能</li>
     *   <li>用于通道切换时的电源管理</li>
     * </ul>
     */
    boolean bChPowerEnable = true;  // 通道电源使能状态，初始为true
    
    /**
     * 生成采样模式配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置采样模式和分段帧数</li>
     *   <li>更新通道电容配置</li>
     *   <li>设置触发输入输出和时钟输入输出</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 发送采样模式命令
     * 2. 发送分段帧数命令
     * 3. 更新通道电容配置
     * 4. 配置触发和时钟输入输出
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_sampMode(int fpgaIdx) {  // 生成采样模式配置方法
        runCommand(fpgaIdx, FPGAReg.FPGA_SAMPLE_MODE);  // 发送采样模式命令
        runCommand(fpgaIdx, FPGAReg.FPGA_SEGMENT_FRAME);  // 发送分段帧数命令
        updateChCapacitance(fpgaIdx);  // 更新通道电容配置
        int val = 0;  // 配置值，初始为0
        Sample sample = Sample.getInstance();  // 获取采样管理实例
        if(sample.isTriggerInOut()){  // 判断：是否启用触发输入输出
            val |= 1;  // 设置bit0
        }  // 判断结束
        if(sample.isClkInOut()){  // 判断：是否启用时钟输入输出
            val |= 2;  // 设置bit1
        }  // 判断结束
        if(bClkOut != sample.isClkInOut()){  // 判断：时钟输出状态是否变化
            bChPowerEnable = false;  // 清除通道电源使能标志
            bClkOut = sample.isClkInOut();  // 更新时钟输出状态
        }  // 判断结束
        SendClkInOut(fpgaIdx,val);  // 发送时钟输入输出配置
        mHw.setTriggerInOut(sample.isTriggerInOut());  // 设置硬件触发输入输出
        mHw.setClkInOut(sample.isClkInOut());  // 设置硬件时钟输入输出
    }  // 方法结束
    
    /**
     * 通道电源使能
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>在通道切换时重置ADC状态</li>
     *   <li>仅当ADC支持通道切换复位时执行</li>
     * </ul>
     */
    public void chPowerEnable(){  // 通道电源使能方法
        if(adc.isChChangeReset()) {  // 判断：ADC是否支持通道切换复位
            if (!bChPowerEnable) {  // 判断：通道电源是否未使能
                Arrays.fill(sampleChState, false);  // 重置采样通道状态
                SendAdc_ch_change(0);  // 发送ADC通道切换命令
                bChPowerEnable = true;  // 设置通道电源使能标志
            }  // 判断结束
        }  // 判断结束
    }  // 方法结束

    /**
     * 生成平均/包络次数配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送平均或包络采样次数命令</li>
     *   <li>用于平均采样和包络采样模式</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_pinJ_baoL(int fpgaIdx) {  // 生成平均/包络次数配置方法
        runCommand(fpgaIdx, FPGAReg.FPGA_PJBL_TIMES);  // 发送平均/包络次数命令
    }  // 方法结束

    /**
     * 生成存储深度配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送存储深度命令</li>
     *   <li>用于配置采样存储深度</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_zun_depth(int fpgaIdx) {  // 生成存储深度配置方法
        runCommand(fpgaIdx, FPGAReg.FPGA_ZUN_DEPTH);  // 发送存储深度命令
    }  // 方法结束

    /**
     * 计算滚屏模式下每次刷新的列数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据一屏时间计算每次刷新的列数</li>
     *   <li>用于滚屏模式的显示更新</li>
     * </ul>
     * 
     * @param colsOneScreen 一屏的列数
     * @return 每次刷新的列数
     */
    // 计算每次刷新多少列
    private int getColsPerRefresh_scollMode(int colsOneScreen) {  // 计算滚屏模式下每次刷新的列数方法
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        // 计算一屏的时间
        int t1 = (int) (scope.timeScale_mainBoard() * ScopeBase.getHorizonGridCnt() * 1000 + 0.1);  // 计算一屏时间（微秒）
        // Logger.i(TAG, "one screeen time(ms)="+t1);
        if (t1 < 1) {  // 判断：一屏时间是否小于1微秒
            t1 = 1;  // 设置最小值为1微秒
        }  // 判断结束

        int perT = 36;  // 每次刷新的时间间隔（毫秒），初始为36
//        if (HardwareProduct.isMHO())  // 已注释：如果是MHO产品
        {  // 代码块开始
            perT = 20;  // 设置刷新间隔为20毫秒
        }  // 代码块结束

        int cols = perT * colsOneScreen;  // 计算总列数
        if (cols % t1 != 0) cols = cols / t1 + 1;  // 计算每次刷新列数（有余数则加1）
        else cols = cols / t1;  // 计算每次刷新列数（无余数）
        if (cols == 0) cols = 1;  // 确保至少刷新1列
        Logger.i(TAG, "每次刷新列数=" + cols + ", 时间=" + (cols * t1 / colsOneScreen));  // 输出日志
        return cols;  // 返回每次刷新的列数
    }  // 方法结束

    /**
     * 生成采样位置配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算预采样和后采样深度</li>
     *   <li>配置滚动模式和慢时基模式参数</li>
     *   <li>设置触发位置相关参数</li>
     * </ul>
     * 
     * <p><b>采样位置计算：</b>
     * <pre>
     * 预采样深度 = 存储深度/2 - 触发位置对应的采样点数
     * 后采样深度 = 存储深度/2 + 触发位置对应的采样点数
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_samp_place(int fpgaIdx) {  // 生成采样位置配置方法
        FPGAReg_PRE_SAMP preSampReg = (FPGAReg_PRE_SAMP) getFPGAReg(fpgaIdx, FPGAReg.FPGA_PRE_SAMP);  // 获取预采样寄存器
        FPGAReg_SCROLL_ROW scrollRowReg =  // 获取滚屏行数寄存器
                (FPGAReg_SCROLL_ROW) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SCROLL_ROW);
        FPGAReg_SLOW_SCALE_SET slowScaleSetReg =  // 获取慢时基设置寄存器
                (FPGAReg_SLOW_SCALE_SET) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SLOW_SCALE_SET);

        HorizontalAxis xAxis = HorizontalAxis.getInstance();  // 获取水平轴实例
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        Sample sample = Sample.getInstance();  // 获取采样管理实例
        int zunDepth = sample.getSampleMemDepth();  // 获取存储深度
        int virSceenCols = ScopeBase.getWidth();  // 获取屏幕宽度（像素）

        // 计算屏数
        double screeNum = scope.screenNum_Main(scope.isRun(true));  // 计算显示屏数，注释：最多只会有2位小数

        int type;  // 模式类型
        BigInteger t = BigInteger.valueOf(0);  // 触发位置对应的采样点数

        if (scope.isInScrollMode())  // 判断：是否为滚动模式
        {
            type = 0;  // 设置类型为滚动模式
        } else {  // 否则：非滚动模式
            type = 1;  // 设置类型为非滚动模式
            // 计算触发位置对应的采样点数
            // 公式：(触发位置时间*准存储深度)/(显示屏数*一屏的时间)
            try {  // 异常捕获
                long m = xAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD);  // 获取触发位置时间

                t = BigInteger.valueOf(m);  // 转换为大整数
                m = (long) zunDepth * 100;  // 存储深度乘以100，注释：因为屏数可能为小数，所以放大100倍
                t = t.multiply(BigInteger.valueOf(m));  // 乘法运算

                m = (long) (screeNum * 100 + 0.1);  // 屏数乘以100
                BigInteger t2 = BigInteger.valueOf(m);  // 转换为大整数
                m = scope.timeOneScreen_main();  // 获取一屏的时间
                t2 = t2.multiply(BigInteger.valueOf(m));  // 乘法运算
                t = t.divide(t2);  // 除法运算，得到触发位置对应的采样点数
            } catch (Exception e) {  // 异常处理
                e.printStackTrace();  // 打印异常堆栈
                Log.e(TAG, e.toString());  // 输出错误日志
            }  // 异常处理结束
        }  // 判断结束

        if (type == 0)  // 判断：是否为滚动模式
        {
            // 预采样深度计算

            preSampReg.setPreVol(0);  // 滚动模式下预采样深度为0

            // 后采样时间计算
            // 公式：=准存储深度*每次滚动的时间Tg/一屏的时间
            int x = virSceenCols;  // 获取屏幕宽度
            int cols = getColsPerRefresh_scollMode(x);  // 计算每次刷新的列数
            if ((cols & 0x01) == 0x01) {  // 判断：列数是否为奇数
                cols++;  // 奇数则加1，确保为偶数
            }  // 判断结束
            // 计算最后一次刷新时，超出屏幕部分
            int n = x / cols;  // 计算刷新次数
            int rev = x % cols;  // 计算剩余列数
            if (rev != 0) {  // 判断：是否有剩余
                n += 1;  // 刷新次数加1
                rev = cols - rev;  // 计算无效列数
            }  // 判断结束
            scrollRowReg.setColsPerRefresh(cols);  // 设置每次刷新的列数
            scrollRowReg.setLastInvalidCols(rev);  // 设置最后一次刷新的无效列数

            slowScaleSetReg.setNumsReferesh(n);  // 设置刷新次数

            long c = zunDepth;  // 获取存储深度

            c = cols * c / x;  // 计算后采样深度
            preSampReg.setPostVolL((int) (c & 0xFFFFFFFFL));  // 设置后采样深度低32位
            preSampReg.setPostVolH((int) ((c >>> 32) & 0xFFFFFFFFL));  // 设置后采样深度高32位

        } else {  // 否则：非滚动模式
            // 预采样深度计算
            // 延迟模式
            // =准存储深度/2-(触发位置时间*准存储深度)/(显示屏数*一屏的时间);
            // 非延迟模式
            // =准存储深度/2-(触发位置像素时间*准存储深度)/一屏的像素时间；
            long m = (long) zunDepth / 2;
            BigInteger c = BigInteger.valueOf(m);
            c = c.subtract(t);
            // 钳位处理
            m = (long) zunDepth;
            if (c.compareTo(BigInteger.valueOf(0)) < 0) {
                c = BigInteger.valueOf(0);
            } else if (c.compareTo(BigInteger.valueOf(m)) > 0) {
                c = BigInteger.valueOf(m);
            }
            int val = c.intValue();

            preSampReg.setPreVol(val);
            if (bPrintf) Logger.i("preSampVol=" + val);

            // 后采样时间计算
            // 延迟模式
            // =准存储深度/2+(触发位置时间*准存储深度)/(显示屏数*一屏的时间);
            // 非延迟模式
            // =准存储深度/2+(触发位置像素时间*准存储深度)/一屏的像素时间；
            m = (long) zunDepth / 2;
            c = BigInteger.valueOf(m);
            c = c.add(t);
            // 钳位处理
            if (c.compareTo(BigInteger.valueOf(0)) < 0) {
                c = BigInteger.valueOf(0);
            }
            long lval = c.longValue();
            if (bPrintf) Logger.i("PostVol=" + lval);
            preSampReg.setPostVolL((int) (lval & 0xFFFFFFFFL));
            preSampReg.setPostVolH((int) ((lval >>> 32) & 0xFFFFFFFFL));

            if (scope.isInSlowScaleMode()) { // 慢时基模式处理
                // 后采样时间计算
                // =准存储深度*每次滚动的时间Tg/一屏的时间;
                int x = virSceenCols;
                int cols = getColsPerRefresh_scollMode(x);
                if ((cols & 0x01) == 0x01) {
                    cols++;
                }

                // 前采样列数=预采样深度/每列多少组采样点
                double y = (double) zunDepth / x;
                slowScaleSetReg.setColsPreSamp((int) (preSampReg.getPreVol() / y + 0.5));
                slowScaleSetReg.setSampsPerLie((int) (y + 0.1));
                int temp = (int) (y * 100 + 0.5);
                temp = temp % 100;

                // 每列采样数有可能是小数
                switch (temp) {
                    case 25: // .25
                        slowScaleSetReg.setSampsPerLie_pt(1);
                        temp = 4;
                        break;
                    case 50: // .5
                        slowScaleSetReg.setSampsPerLie_pt(2);
                        temp = 2;
                        break;
                    case 75: // .75
                        slowScaleSetReg.setSampsPerLie_pt(3);
                        temp = 4;
                        break;
                    default:
                        slowScaleSetReg.setSampsPerLie_pt(0);
                        temp = 0;
                        break;
                }
                if (temp != 0) cols = cols + temp - cols % temp;  // 判断：如果temp不为0，调整列数为temp的整数倍
                scrollRowReg.setColsPerRefresh(cols);  // 设置每次刷新的列数

                // 计算触发位置
                long z = (long) (lval / y + 0.01); // 后采样列数
                // 后采样时间必须是整数列，否者会产生误差
                lval = (long) (z * y + 0.01);
                int xx = 0; // 触发位置在屏幕内
                if (z > x) {
                    // 触发位置在屏幕外，左边
                    xx = (int) (z - x);
                }
                slowScaleSetReg.setColsTrigPlace(xx);

                // 计算后采样需要完成多少次刷新
                // 计算最后一次刷新时，超出屏幕部分
                int n = (int) (z / cols);  // 计算刷新次数
                int rev = (int) (z % cols);  // 计算剩余列数
                if (rev != 0) {  // 判断：是否有剩余列数
                    n += 1;  // 刷新次数加1
                    rev = cols - rev;  // 计算最后一次刷新的无效列数
                }  // 判断结束

                scrollRowReg.setLastInvalidCols(rev);  // 设置最后一次刷新的无效列数
                slowScaleSetReg.setNumsReferesh(n);  // 设置刷新次数
                // long cc = zunDepth;  // 已注释：原始计算方式
                // cc = cc * cols / virSceenCols;  // 已注释：原始计算方式
                long cc = (long) (cols * y + 0.1);  // 计算后采样数据量
                preSampReg.setPostVolL((int) (cc & 0xFFFFFFFFL));  // 设置后采样数据量低32位
                preSampReg.setPostVolH((int) ((cc >>> 32) & 0xFFFFFFFFL));  // 设置后采样数据量高32位

                preSampReg.setPreVol((int) (preSampReg.getPreVol() + y));  // 更新预采样数据量

                // 计算第一次刷新时，需要多少数据
                long z1 = lval - zunDepth;  // 计算差值
                FPGAReg_ZUN_DEPTH zun_depthReg =  // 获取缩放深度寄存器
                        (FPGAReg_ZUN_DEPTH) getFPGAReg(fpgaIdx, FPGAReg.FPGA_ZUN_DEPTH);
                if (z1 <= 0) {  // 判断：差值是否小于等于0
                    zun_depthReg.Set_zunDepth((int) (cc - z1));  // 设置缩放深度
                } else {  // 否则：差值大于0
                    zun_depthReg.Set_zunDepth((int) (cc - z1 % cc));  // 设置缩放深度（取模）
                }  // 判断结束
                sendCommand(zun_depthReg);  // 发送缩放深度命令
            }  // 判断结束
        }  // 判断结束
        sendCommand(scrollRowReg);  // 发送滚动行配置命令
        sendCommand(preSampReg);  // 发送预采样配置命令
        sendCommand(slowScaleSetReg);  // 发送慢速缩放配置命令
        if (type == 0) {  // 判断：类型是否为0
            setDataFreq(fpgaIdx, 30, 5);  // 设置数据刷新频率为30Hz和5Hz
        }  // 判断结束
    }  // 方法结束

    /**
     * 生成抽样系数配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算FPGA采样抽样系数</li>
     *   <li>根据通道数调整抽样系数</li>
     *   <li>配置抽样补偿参数</li>
     * </ul>
     * 
     * <p><b>抽样系数计算公式：</b>
     * <pre>
     * 基本抽样系数 = maxSampClk / 实际采样率
     * 
     * 根据通道数调整：
     *   - 2/4通道模式：cy = ceil(cy / 5.0)
     *   - 8通道模式：  cy = ceil(cy / 2.5)
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_CouY(int fpgaIdx) {  // 生成抽样系数配置方法
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        FPGAReg_NUM_CY12 couyReg = (FPGAReg_NUM_CY12) getFPGAReg(fpgaIdx, FPGAReg.FPGA_NUM_CY12);  // 获取抽样系数寄存器
        FPGAReg_CY_BUCONG couybcReg =  // 获取抽样补偿寄存器
                (FPGAReg_CY_BUCONG) getFPGAReg(fpgaIdx, FPGAReg.FPGA_CY_BUCONG);

        // 始终以最高采样速率采样，然后抽�?
        int couY = 1;  // 抽样类型标志，初始为1
        long a = scope.maxSampClk();  // 获取最大采样时钟
        a = a * 1000 * 1000 * 1000L;  // 转换为mHz，注释：换算成mHz
        double fs = scope.getSampleRate(scope.isRun(true));  // 获取实际采样率

        long b = (long) (fs * 1000 + 0.1);  // 采样率转换为mHz

        int cy = (int) (a / b);  // 计算基本抽样系数
        if (a % b != 0) {  // 判断：是否能整除
            Log.e(TAG, "samp: can not couY to " + b + "mHz");  // 输出错误日志
        }  // 判断结束
        couyReg.setVol12(cy);  // 设置基本抽样系数

        if (cy == 1) {  // 判断：抽样系数是否为1
            couY = 0;  // 设置抽样类型为0
        }  // 判断结束

        switch (scope.getChannelSampOnCnt(scope.isRun(true))) {  // 根据通道数调整抽样系数
            case 2:  // 2通道模式
            case 4:  // 4通道模式
                cy = (int) Math.ceil((double) cy / 5.0);  // 抽样系数除以5
                break;  // 跳出switch
            case 8:  // 8通道模式
            default:  // 默认情况
                cy = (int) Math.ceil((double) cy / 2.5);  // 抽样系数除以2.5
                break;  // 跳出switch
        }  // switch结束

        couyReg.setVol34(cy);  // 设置调整后的抽样系数

        sendCommand(couyReg);  // 发送抽样系数命令
        couybcReg.setCyType12(couY);  // 设置抽样类型12
        couybcReg.setCyType34(couY);  // 设置抽样类型34
        sendCommand(couybcReg);  // 发送抽样补偿命令
    }  // 方法结束

    /**
     * 生成串行解码深度配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算串行解码的采样深度</li>
     *   <li>支持主视图和缩放视图</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_serial_dec_depth(int fpgaIdx) {  // 生成串行解码深度配置方法
        FPGAReg_SERIAL_DEC_DEPTH serialDecReg =  // 获取串行解码深度寄存器
                (FPGAReg_SERIAL_DEC_DEPTH) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SERIAL_DEC_DEPTH);
        Display display = Display.getInstance();  // 获取显示管理实例
        Scope scope = Scope.getInstance();  // 获取示波器核心实例

        // 显示屏数*一屏的时间/40ns�?
        // 计算屏数
        double screeNum; // 最多只会有2位小�?
        long timeOneScreen;

        if (display.isZoom()) {  // 判断：是否为缩放视图
            screeNum = scope.screenNum_zoom(scope.isRun(true));  // 获取缩放视图屏数
            timeOneScreen = scope.timeOneScreen_zoom();  // 获取缩放视图一屏时间
        } else {  // 否则：主视图
            screeNum = scope.screenNum_Main(scope.isRun(true));  // 获取主视图屏数
            timeOneScreen = scope.timeOneScreen_main();  // 获取主视图一屏时间
        }  // 判断结束

        long x = (long) (screeNum * 100 + 0.1);  // 屏数乘以100
        x = x * timeOneScreen / (100 * 40 * 10000);  // 计算解码深度，注释：0.1ps
        serialDecReg.setDecDepth((int) x);  // 设置解码深度

        sendCommand(serialDecReg);  // 发送命令
    }  // 方法结束

    /**
     * 获取通道系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算通道的DA转换系数</li>
     *   <li>校准模式下使用默认系数</li>
     *   <li>非校准模式下使用计算系数</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param idx 系数索引（0:正向偏移，1:负向偏移）
     * @return 通道系数（取负值）
     */
    private double channelCoef(int chIdx, int idx) {  // 获取通道系数方法
        double val = 0;  // 系数值，初始为0
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取通道对象
        if (channel != null) {  // 判断：通道是否有效
            CabteRegister cabteRegister = CabteRegister.getInstance();  // 获取校准寄存器实例
            double vScaleVal =  // 计算档位值
                    VerticalAxis.clampMin(channel.getVScaleVal() / channel.getProbeRate(),channel.getResistanceType());
            if (isCalibrate()  // 判断：是否正在校准
                    && isZeroCalibrate()) {  // 判断：是否为零点校准

                val =  // 获取默认系数
                        cabteRegister.vol_ChannelCoef_defaultEx(channel.getChId(),
                                channel.getVScaleId(), pgaVal & 0xFF);
            } else {  // 否则：非校准模式
                val = cabteRegister.calc_coefChannel(chIdx, vScaleVal, idx);  // 计算系数
            }  // 判断结束
        }  // 判断结束
        Log.d("MHO38V1","ch" + chIdx + ":" + (-val) + "," + channel.getVScaleIdVal());  // 输出调试日志
        return -val;  // 返回系数的负值
    }  // 方法结束

    /**
     * 获取通道零点值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取通道的DA零点值</li>
     *   <li>校准模式下使用指定PGA值</li>
     *   <li>非校准模式下计算PGA和满量程增益</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @return 通道零点值
     */
    private synchronized double getChannelZero(int chIdx) {  // 获取通道零点值方法
        float val = 0;  // 零点值，初始为0

        CabteRegister cabteRegister = CabteRegister.getInstance();  // 获取校准寄存器实例
        int[] result = {pgaVal, 0, 0, 0,0};  // 结果数组，初始PGA值
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取通道对象
        if (channel != null) {  // 判断：通道是否有效
            double vScaleVal =  // 计算档位值
                    VerticalAxis.clampMin(channel.getVScaleVal() / channel.getProbeRate(),channel.getResistanceType());
            int idx = CabteRegister.getRatioIdx(channel.getResistanceType(),vScaleVal);  // 获取档位索引
            if (isCalibrate()  // 判断：是否正在校准
                    && isZeroCalibrate()) {  // 判断：是否为零点校准
                val = cabteRegister.getChannelZero(chIdx, idx, pgaVal & 0xFF);  // 获取指定PGA的零点值
            } else {  // 否则：非校准模式
                cabteRegister.calc_pga_fs_gain(channel.getChId(), vScaleVal, result);  // 计算PGA和满量程增益

                val = cabteRegister.getChannelZero(chIdx, idx, result[0] & 0xFF);  // 获取计算后的零点值

            }  // 判断结束
        }  // 判断结束

        return DoubleUtil.Float2Double(val);  // 转换为double并返回
    }  // 方法结束

    /**
     * 获取通道DA电压值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据通道位置和偏移计算DA输出值</li>
     *   <li>支持通道反相</li>
     *   <li>DA值钳位到0-65535范围</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 正常模式：vol = zero + coef * offset
     * 反相模式：vol = zero - coef * offset
     * </pre>
     * 
     * @param channel 通道对象
     * @return DA电压值（0-65535）
     */
    private int getDA_Vol(Channel channel) {  // 获取通道DA电压值方法

        boolean bInvert = channel.isInvert();  // 获取通道反相状态
        double chOffset = channel.getZero() + channel.getChOffset() * ScopeBase.getToFPGACoff();  // 计算通道偏移
        double offset,coef,zero;  // 偏移、系数、零点变量
        int vol;  // DA电压值
        zero = getChannelZero(channel.getChId());  // 获取通道零点值

        if(!(HardwareProduct.isMHO38V1()  // 判断：是否非MHO38V1
                || HardwareProduct.isMHO28V1())){  // 判断：是否非MHO28V1
            int dang = CabteRegister.getRatioIdx(Channel.RESISTANCE_1M,channel.getVScaleVal()/channel.getProbeRate());  // 获取档位索引
            if (channel.getResistanceType() == Channel.RESISTANCE_1M  // 判断：是否为1M阻抗
                    && dang != HW.RATIO_DANG_1) {  // 判断：是否非1档
                bInvert = !bInvert;  // 反转反相状态
                chOffset = - chOffset;  // 反转偏移值
            }  // 判断结束
        }  // 判断结束
        if (bInvert) {  // 判断：是否反相
            offset = (double) channel.getPos() - chOffset;  // 计算偏移（反相）
            offset /= channel.getYFactor();  // 除以Y因子
            coef = channelCoef(channel.getChId(), offset >= 0 ? 0 : 1);  // 获取系数
            vol = (int) Math.round(zero - coef * offset);  // 计算DA值（反相）

        }else{  // 否则：正常模式
            offset = (double) channel.getPos() + chOffset;  // 计算偏移（正常）
            offset /= channel.getYFactor();  // 除以Y因子
            coef = channelCoef(channel.getChId(), offset >= 0 ? 0 : 1);  // 获取系数
            vol = (int) Math.round(zero + coef * offset);  // 计算DA值（正常）
        }  // 判断结束
        if (vol < 0) {  // 判断：DA值是否小于0
            vol = 0;  // 钳位到0
        } else if (vol > 65535){  // 判断：DA值是否大于65535
            vol = 65535;  // 钳位到65535
        }  // 判断结束
        return vol;  // 返回DA电压值
    }  // 方法结束

    /**
     * 获取值差异系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回通道值差异系数</li>
     *   <li>当前固定返回1.0</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @return 差异系数（固定为1.0）
     */
    private double getValueDifferentCoef(int chIdx) {  // 获取值差异系数方法
        return 1.0;  // 返回固定值1.0
    }  // 方法结束

    /**
     * 根据档位获取AD值与像素的转换系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回AD值与屏幕像素的对应系数</li>
     *   <li>用于通道位置计算</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param volScaleId 档位ID
     * @return AD值与像素的转换系数
     */
    private double ADGear2PixBuf_fromVScale(int chIdx, int volScaleId) {  // 根据档位获取AD值与像素转换系数方法
        return hwConfig.getAdValPerPix();  // 返回硬件配置的AD值与像素系数
    }  // 方法结束

    /**
     * 位置参数m的最大值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>FPGA位置参数m的钳位上限</li>
     *   <li>防止位置溢出</li>
     * </ul>
     */
    private static final int MAX_m = ((1 << 17) - 5);  // 位置参数m的最大值，131067
    
    /**
     * 位置参数m的最小值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>FPGA位置参数m的钳位下限</li>
     *   <li>防止位置溢出</li>
     * </ul>
     */
    private static final int MIN_m = (-(1 << 17) + 5);  // 位置参数m的最小值，-131067

    /**
     * 生成通道Y位置配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算通道在FPGA中的垂直位置参数</li>
     *   <li>设置位置补偿参数</li>
     *   <li>处理位置溢出钳位</li>
     * </ul>
     * 
     * <p><b>位置计算公式：</b>
     * <pre>
     * iValue = zero + pos * coef
     * z = (iValue - zero) / coef
     * fx = z * YPlaceFactor / k
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_ch_y_place(int fpgaIdx) {  // 生成通道Y位置配置方法
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        if (!scope.isRun(true)) return;  // 判断：是否运行中，未运行则返回
        FPGAReg_CH_Y_PLACE chyplaceReg =  // 获取通道Y位置寄存器
                (FPGAReg_CH_Y_PLACE) getFPGAReg(fpgaIdx, FPGAReg.FPGA_CH_Y_PLACE);
        FPGAReg_CH_Y_PLACE_BC chyplacebcReg =  // 获取通道Y位置补偿寄存器
                (FPGAReg_CH_Y_PLACE_BC) getFPGAReg(fpgaIdx, FPGAReg.FPGA_CH_Y_PLACE_BC);

        Display display = Display.getInstance();  // 获取显示管理实例
        int[] m = {0, 0, 0, 0};  // 位置参数m数组
        int[] n = {0, 0, 0, 0};  // 位置参数n数组
        int[] place_bc = {0, 0, 0, 0};  // 位置补偿数组
        // int chIdx = 0;  // 已注释：通道索引
        Channel channel;  // 通道对象

        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        int j;  // 循环变量
        boolean[] chSample = new boolean[ChannelFactory.CH_CNT];  // 采样通道状态数组
        int chCnt = scope.getChannelSampOnCnt(scope.isRun(true), chSample);  // 获取采样通道数
        int halfHeight = ScopeBase.getHeight() / 2;  // 计算屏幕半高
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            j = i - beginIdx;  // 计算相对索引
            if (!chSample[i]) continue;  // 判断：通道是否参与采样，不参与则跳过
            channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
            if (channel == null) {  // 判断：通道是否有效
                continue;  // 无效则跳过
            }  // 判断结束

            int dang = channel.getVScaleId();  // 获取档位ID
            double pos = channel.getPos() / channel.getYFactor();  // 计算位置（归一化）
            double zero = getChannelZero(channel.getChId());  // 获取通道零点值
            double coef = channelCoef(channel.getChId(), pos >= 0 ? 0 : 1);  // 获取系数，注释：取偏移量系数
            double iValue = zero + pos * coef;  // 计算DA值
            if (iValue < 0) {  // 判断：DA值是否小于0
                iValue = 0;  // 钳位到0
            } else if (iValue > 65535) {  // 判断：DA值是否大于65535
                iValue = 65535;  // 钳位到65535
            }else{  // 否则：在有效范围内
                Logger.d(TAG, "ch:" + channel.getChId() + ",zero:" + zero + ",pos:" + pos + ",coef:" + coef);  // 输出调试日志
            }  // 判断结束

            double z = (Math.round(iValue) - zero) / coef;  // 计算通道指示器相对与中心的偏移，注释：上负下正

            // config.vPosOfZeroFix(true, chIdx) = z;  // 已注释：保存修正值
            channel.setPosFix(z * channel.getYFactor());  // 保存修正值，注释：在静态波形、触发、查找表中会用到

            // 因档位不同，取不同的【AD值与屏幕像素的对应系数】
            double a = getValueDifferentCoef(channel.getChId());  // 获取值差异系数
            double k = ADGear2PixBuf_fromVScale(channel.getChId(), dang) * a;  // 计算AD值与像素系数

            // printf("channel %d diffent vol=%f\n", i+1, a);  // 已注释：打印调试信息
            // fAD2Pix[i] = k;  // 已注释：保存系数
            channel.setAdPix(k);  // 设置通道的AD像素系数
            // 当通道偏移量移动到比较远时，可能导致m计算时溢出，这是进行相关处理
            if (z > 500) {  // 判断：位置是否超过上限
                place_bc[j] = (int) (-(z - 500));  // 计算补偿值
                z = 500;  // 钳位位置

            } else if (z < -500) {  // 判断：位置是否低于下限
                place_bc[j] = (int) (-(z + 500));  // 计算补偿值
                z = -500;  // 钳位位置
            }  // 判断结束

            // iPlace_bc[i] = place_bc[i];  // 已注释：保存补偿值

            channel.setPlaceVal(place_bc[j] * hwConfig.getPix2AdFactor());  // 设置通道位置值
            place_bc[j] = 0;  // 重置补偿值

            // place_bc[i] = z1;  // 已注释：保存补偿值

            double fx = z * hwConfig.getYPlaceFactor() / k ;  // 计算FPGA位置参数

            if (fx > MAX_m) fx = MAX_m;  // 钳位到最大值
            else if (fx < MIN_m) fx = MIN_m;  // 钳位到最小值

            int vol_m = (int) (fx + (fx > 0 ? 0.5 : -0.5));  // 计算m值，注释：进行舍入
            int vol_n = (int) (k * 256 + 0.5);  // 计算n值
            // if(bXYMode) vol_n = 1*256;  // 已注释：XY模式处理
            if (display.isXYMode()  // 判断：是否为XY模式
                    || (isCalibrate() && isChGainCalibrate())) {  // 判断：是否为通道增益校准
                vol_n = 256;  // 设置固定n值
                channel.setPlaceVal(0);  // 清零位置值
            }  // 判断结束

            m[j] = -vol_m;  // 保存m值（取负）
            n[j] = vol_n;  // 保存n值

            channel.setM(m[j]);  // 设置通道的m值
            channel.setN(n[j]);  // 设置通道的n值

        }  // 循环结束


        int xcnt = 0;  // 每个通道的扩展计数
        int sIdx = 0;  // 起始索引
        int s1 = 0;  // 补偿寄存器索引
        int sbit = 0;  // 补偿寄存器位偏移
        switch (chCnt) {  // 根据通道数设置扩展计数
            case 2:  // 2通道模式
                xcnt = 4;  // 每个通道扩展4次
                break;  // 跳出switch
            case 4:  // 4通道模式
                xcnt = 2;  // 每个通道扩展2次
                break;  // 跳出switch
            case 8:  // 8通道模式
            default:  // 默认情况
                xcnt = 1;  // 每个通道扩展1次
                break;  // 跳出switch
        }  // switch结束

        int idx = 0;  // 相对索引
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            idx = i - beginIdx;  // 计算相对索引
            if (chSample[i]) {  // 判断：通道是否参与采样
                for (j = 0; j < xcnt; j++) {  // 循环：扩展设置
                    chyplaceReg.setVolm(sIdx + j, m[idx]);  // 设置m值
                    chyplaceReg.setVoln(sIdx + j, n[idx]);  // 设置n值
                    chyplacebcReg.setVal(s1, sbit, 16, place_bc[idx]);  // 设置补偿值
                    sbit += 16;  // 位偏移加16
                    if (sbit >= 32) {  // 判断：是否超过32位
                        sbit = 0;  // 重置位偏移
                        s1 += 1;  // 寄存器索引加1
                    }  // 判断结束
                }  // 循环结束
                sIdx += xcnt;  // 起始索引增加
                chCnt--;  // 通道计数减1
            }  // 判断结束
            if (chCnt == 0) break;  // 判断：所有通道处理完毕，跳出循环
        }  // 循环结束
        sendCommand(chyplaceReg);  // 发送通道Y位置命令
        sendCommand(chyplacebcReg);  // 发送通道Y位置补偿命令
    }  // 方法结束

    /**
     * 生成触发模式配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置触发源、触发类型、触发参数</li>
     *   <li>支持多种触发类型：边沿、脉冲、视频等</li>
     *   <li>支持外部触发</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_trigMode(int fpgaIdx) {  // 生成触发模式配置方法
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        FPGAReg_TRIG_MODE reg = (FPGAReg_TRIG_MODE) getFPGAReg(fpgaIdx, FPGAReg.FPGA_TRIG_MODE);  // 获取触发模式寄存器
        reg.setVal(0, 4, 0);  // 设置默认值
        reg.setVal(12, 15, 0xF);  // 设置默认值
        reg.setJiaoZhun(bCalibrate_ad_diffGain ? 1 : 0);  // 设置校准标志，注释：AD增益校准时需要设置为1

        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();  // 获取触发通用设置
        Trigger trigger = TriggerFactory.getInstance().getTrigger();  // 获取触发对象
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        boolean[] sel = new boolean[ChannelFactory.CH_CNT];  // 通道选择数组
        int chCnt = scope.getChannelSampOnCnt(scope.isRun(true), sel);  // 获取采样通道数
        int selCh = trigger.getTriggerSource();  // 获取触发源

        if (selCh == 8) {  // 判断：是否为外部触发，注释：外部触发
            reg.setExternalTrigger(1);  // 设置外部触发标志
        } else {  // 否则：内部触发
            reg.setExternalTrigger(0);  // 清除外部触发标志
        }  // 判断结束
//        chCnt /= adcNums;  // 已注释：通道数除以ADC数
        if(chCnt <= adcNums){  // 判断：通道数是否小于等于ADC数
            selCh = (selCh / 4) * 4  + (selCh % 4) / 2;  // 重新计算触发源索引
        }  // 判断结束
        reg.setTrigSource(selCh);  // 设置触发源
        reg.setNomalTrig(triggerCommon.getTriggerMode());  // 设置触发模式
        reg.setForceTrig(0);  // 清除强制触发标志
        int triggerType = triggerCommon.getTriggerType();  // 获取触发类型
        switch (triggerType) {  // 根据触发类型设置参数
            case Trigger.TRIG_TYPE_EDGE:  // 边沿触发
                {
                    TriggerEdge triggerEdge = (TriggerEdge) trigger;  // 获取边沿触发对象
                    reg.setTrigType(0);  // 设置触发类型为边沿
                    reg.setTrigParam(triggerEdge.getTriggerEdge() & 0x07);  // 设置边沿参数
                }  // 代码块结束
                break;  // 跳出switch


            case Trigger.TRIG_TYPE_SERIAL1:  // 串行触发1
            case Trigger.TRIG_TYPE_SERIAL2:  // 串行触发2
            case Trigger.TRIG_TYPE_SERIAL3:  // 串行触发3
            case Trigger.TRIG_TYPE_SERIAL4:  // 串行触发4
                reg.setTrigType(4);  // 设置触发类型为串行
                reg.setTrigBus(triggerType - Trigger.TRIG_TYPE_SERIAL1);  // 设置总线索引
                break;  // 跳出switch
            case Trigger.TRIG_TYPE_LOW_PULSE:  // 低脉冲触发
                {
                    TriggerRunt triggerRunt = (TriggerRunt) trigger;  // 获取矮脉冲触发对象
                    reg.setTrigType(5);  // 设置触发类型为矮脉冲
                    reg.setTrigParam(triggerRunt.getCondition() | (triggerRunt.getPolarity() << 4));  // 设置触发参数
                    int lowTime = (int) Trigger.triggerTime2FpgaUnit(triggerRunt.getTimeLow());  // 计算低时间（FPGA单位）
                    int highTime = (int) Trigger.triggerTime2FpgaUnit(triggerRunt.getTimeHigh());  // 计算高时间（FPGA单位）
                    if (triggerRunt.getCondition() == TriggerRunt.RUNT_RELATION_LESSER) {  // 判断：条件是否为小于
                        reg.setTrigValL(highTime);  // 设置触发低值
                        reg.setTrigValH(highTime);  // 设置触发高值
                    } else {  // 否则：其他条件
                        reg.setTrigValL(lowTime);  // 设置触发低值
                        reg.setTrigValH(highTime);  // 设置触发高值
                    }  // 判断结束
                    break;  // 跳出switch
                }  // 代码块结束
            case Trigger.TRIG_TYPE_PULSE:  // 脉冲触发
                {
                    TriggerPulseWidth triggerPulseWidth = (TriggerPulseWidth) trigger;  // 获取脉宽触发对象
                    reg.setTrigType(1);  // 设置触发类型为脉冲
                    if (triggerPulseWidth.getCondition() == TriggerPulseWidth.PW_RELATION_NOT_EQUAL) {  // 判断：条件是否为不等于
                        reg.setTrigParam(TriggerPulseWidth.PW_RELATION_EQUAL);  // 设置参数为等于
                    } else {  // 否则：其他条件
                        reg.setTrigParam(triggerPulseWidth.getCondition());  // 设置条件参数
                    }  // 判断结束
                    int x = triggerPulseWidth.getPolarity();  // 获取极性
                    for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                        reg.setTrigPolarityCh(i-beginIdx, x);  // 设置通道极性
                    }  // 循环结束

                    int vol = (int) Trigger.triggerTime2FpgaUnit(triggerPulseWidth.getPwTime());  // 计算脉冲宽度时间
                    int lowTime = (int) Trigger.triggerTime2FpgaUnit(triggerPulseWidth.getTimeLow());  // 计算低时间
                    int highTime = (int) Trigger.triggerTime2FpgaUnit(triggerPulseWidth.getTimeHigh());  // 计算高时间
                    switch (triggerPulseWidth.getCondition()) {  // 根据条件设置触发值
                        case TriggerPulseWidth.PW_RELATION_EQUAL:  // 等于条件
                            reg.setTrigValL((int) Math.floor(1.0 * vol * 0.95 + 0.5));  // 设置低值（95%）
                            reg.setTrigValH((int) Math.ceil(1.0 * vol * 1.05 + 0.5));  // 设置高值（105%）
                            break;  // 跳出switch
                        case TriggerPulseWidth.PW_RELATION_LESSER:  // 小于条件，注释：小于的时候 FPGA用最小值来判断的
                            reg.setTrigValL(highTime);  // 设置低值
                            reg.setTrigValH(0);  // 设置高值为0
                            break;  // 跳出switch
                        default:  // 默认情况
                            reg.setTrigValL(lowTime);  // 设置低值
                            reg.setTrigValH(highTime);  // 设置高值
                            break;  // 跳出switch
                    }  // switch结束
                    break;  // 跳出switch
                }  // 代码块结束
            case Trigger.TRIG_TYPE_LOGIC:  // 逻辑触发
                {
                    TriggerLogic triggerLogic = (TriggerLogic) trigger;  // 获取逻辑触发对象
                    reg.setTrigType(2);  // 设置触发类型为逻辑
                    if (triggerLogic.getCondition() == TriggerLogic.LOGIC_RELATION_NOT_EQUAL) {  // 判断：条件是否为不等于
                        reg.setTrigParam(TriggerLogic.LOGIC_RELATION_EQUAL | ((triggerLogic.getLogic() << 3)));  // 设置参数
                    } else {  // 否则：其他条件
                        reg.setTrigParam(triggerLogic.getCondition() | ((triggerLogic.getLogic() << 3)));  // 设置参数
                    }  // 判断结束

                    switch (chCnt) {  // 根据通道数设置逻辑触发参数
                        case 2:  // 2通道模式
                            {
                                for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                                    if (sel[i]) {  // 判断：通道是否选中
                                        if (TriggerFactory.isTriggerSource(i)) {  // 判断：是否为触发源
                                            for (int j = 0; j < 4; j++) {  // 循环：设置极性
                                                reg.setTrigPolarityCh(
                                                        j, triggerLogic.getLogicValid(i));  // 设置通道极性
                                            }  // 循环结束
                                        }  // 判断结束
                                        for (int j = 0; j < 4; j++) {  // 循环：设置逻辑有效
                                            reg.setLogicCh(
                                                    j,
                                                    triggerLogic.getLogicValid(i)
                                                                    == TriggerLogic.LOGIC_NONE
                                                            ? 1
                                                            : 0);  // 设置逻辑通道
                                        }  // 循环结束
                                    }  // 判断结束
                                }  // 循环结束
                                break;  // 跳出代码块
                            }  // 代码块结束
                        case 4:  // 4通道模式
                            {
                                int it = 0;  // 迭代变量
                                for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                                    if (sel[i]) {  // 判断：通道是否选中
                                        if (TriggerFactory.isTriggerSource(i)) {  // 判断：是否为触发源
                                            reg.setTrigPolarityCh(
                                                    it, triggerLogic.getLogicValid(i));  // 设置通道极性
                                            reg.setTrigPolarityCh(
                                                    it + 1, triggerLogic.getLogicValid(i));  // 设置通道极性
                                        }  // 判断结束
                                        reg.setLogicCh(
                                                it,
                                                triggerLogic.getLogicValid(i)
                                                                == TriggerLogic.LOGIC_NONE
                                                        ? 1
                                                        : 0);  // 设置逻辑通道
                                        reg.setLogicCh(
                                                it + 1,
                                                triggerLogic.getLogicValid(i)
                                                                == TriggerLogic.LOGIC_NONE
                                                        ? 1
                                                        : 0);  // 设置逻辑通道
                                        it += 2;  // 迭代变量加2
                                    }  // 判断结束
                                }  // 循环结束
                                break;  // 跳出代码块
                            }  // 代码块结束
                        case 8:  // 8通道模式
                        default:  // 默认情况
                        {
                            for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                                reg.setTrigPolarityCh(
                                        i - beginIdx, triggerLogic.getLogicValid(i));  // 设置通道极性
                                reg.setLogicCh(
                                        i - beginIdx,
                                        triggerLogic.getLogicValid(i) == TriggerLogic.LOGIC_NONE
                                                ? 1
                                                : 0);  // 设置逻辑通道
                            }  // 循环结束
                            break;  // 跳出代码块
                        }  // 代码块结束
                    }  // switch结束
                    long vol = Trigger.triggerTime2FpgaUnit(triggerLogic.getLogicTime());  // 计算逻辑时间
                    int lowTime = (int) Trigger.triggerTime2FpgaUnit(triggerLogic.getTimeLow());  // 计算低时间
                    int highTime = (int) Trigger.triggerTime2FpgaUnit(triggerLogic.getTimeHigh());  // 计算高时间
                    switch (triggerLogic.getCondition()) {  // 根据条件设置触发值
                        case TriggerLogic.LOGIC_RELATION_EQUAL:  // 等于条件
                            reg.setTrigValL((int) (vol * 95 / 100));  // 设置低值（95%）
                            reg.setTrigValH((int) (vol * 105 / 100));  // 设置高值（105%）
                            break;  // 跳出switch
                        case TriggerPulseWidth.PW_RELATION_LESSER:  // 小于条件，注释：小于的时候 FPGA用最小值来判断的
                            reg.setTrigValL(highTime);  // 设置低值
                            reg.setTrigValH(0);  // 设置高值为0
                            break;  // 跳出switch
                        default:  // 默认情况
                            reg.setTrigValL(lowTime);  // 设置低值
                            reg.setTrigValH(highTime);  // 设置高值
                            break;  // 跳出switch
                    }  // switch结束
                    break;  // 跳出switch
                }  // 代码块结束
            case Trigger.TRIG_TYPE_VIDEO:  // 视频触发
                {
                    TriggerVideo triggerVideo = (TriggerVideo) trigger;  // 获取视频触发对象
                    reg.setTrigType(3);  // 设置触发类型为视频
                    for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                        reg.setTrigPolarityCh(i - beginIdx, triggerVideo.getPolarity());  // 设置通道极性
                    }  // 循环结束

                    reg.setTrigParam(
                            triggerVideo.getVideoTrigger()
                                    | (triggerVideo.getStandard() << 3)
                                    | (triggerVideo.getVideoFrequency() << 6)
                                    | (triggerVideo.getStandard()
                                                    >= TriggerVideo.VIDEO_STANDARD_720P
                                            ? (1 << 9)
                                            : 0));  // 设置触发参数
                    reg.setTrigValL(triggerVideo.getLine());  // 设置触发行号
                    reg.setTrigValH(triggerVideo.triggerVideoTimeFieldLine());  // 设置触发场行
                    break;  // 跳出switch
                }  // 代码块结束
            case Trigger.TRIG_TYPE_TIMEOUT:  // 超时触发
                {
                    TriggerTimeOut triggerTimeOut = (TriggerTimeOut) trigger;  // 获取超时触发对象
                    reg.setTrigType(6);  // 设置触发类型为超时
                    reg.setTrigParam(triggerTimeOut.getPolarity());  // 设置极性参数
                    reg.setTrigValL(
                            (int) Trigger.triggerTime2FpgaUnit(triggerTimeOut.getTimeOutTime()));  // 设置超时时间

                    break;  // 跳出switch
                }  // 代码块结束
            case Trigger.TRIG_TYPE_SLOPE:  // 斜率触发
                {
                    TriggerSlope triggerSlope = (TriggerSlope) trigger;  // 获取斜率触发对象
                    reg.setTrigType(7);  // 设置触发类型为斜率
                    reg.setTrigParam(triggerSlope.getCondition() | (triggerSlope.getEdge() << 4));  // 设置触发参数
                    int lowTime = (int) Trigger.triggerTime2FpgaUnit(triggerSlope.getTimeLow());  // 计算低时间
                    int highTime = (int) Trigger.triggerTime2FpgaUnit(triggerSlope.getTimeHigh());  // 计算高时间
                    if (triggerSlope.getCondition() == TriggerSlope.SLOPE_RELATION_LESSER) {  // 判断：条件是否为小于
                        reg.setTrigValL(highTime);  // 设置低值
                        reg.setTrigValH(highTime);  // 设置高值
                    } else {  // 否则：其他条件
                        reg.setTrigValL(lowTime);  // 设置低值
                        reg.setTrigValH(highTime);  // 设置高值
                    }  // 判断结束
                    break;  // 跳出switch
                }  // 代码块结束
            case Trigger.TRIG_TYPE_NEDGE:  // 非边沿触发
                {
                    TriggerNEdge triggerNEdge = (TriggerNEdge) trigger;  // 获取非边沿触发对象
                    reg.setTrigType(8);  // 设置触发类型为非边沿
                    reg.setTrigParam(triggerNEdge.getSlope());  // 设置斜率参数
                    reg.setTrigValL((int) Trigger.triggerTime2FpgaUnit(triggerNEdge.getIdleTime()));  // 设置空闲时间
                    reg.setTrigValH(triggerNEdge.getEdge());  // 设置边沿参数
                    break;  // 跳出switch
                }  // 代码块结束
        }  // switch结束
        sendCommand(reg);  // 发送触发模式命令
    }  // 方法结束

    /**
     * 生成触发抑制时间配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置触发后的抑制时间</li>
     *   <li>防止在抑制时间内再次触发</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_trigRestrainTime(int fpgaIdx) {  // 生成触发抑制时间配置方法
        FPGAReg_TRIG_RESTRAIN_TIME reg =  // 获取触发抑制时间寄存器
                (FPGAReg_TRIG_RESTRAIN_TIME) getFPGAReg(fpgaIdx, FPGAReg.FPGA_TRIG_RESTRAIN_TIME);
        long lx = TriggerFactory.getInstance().getTriggerCommon().getTriggerHoldOffTime();  // 获取触发释抑时间
        reg.setRestrainTime((int) TriggerCommon.trriggerHoldOffTime2FpgaUnit(lx));  // 设置抑制时间（FPGA单位）
        sendCommand(reg);  // 发送命令
    }  // 方法结束

    /**
     * 获取进入自动触发的时间
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算自动触发模式的等待时间</li>
     *   <li>根据时基和触发类型调整时间</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 时间 = 时基 × 屏数 × 格数 × 1500
     * </pre>
     * 
     * @return 自动触发时间（毫秒）
     */
    private int getTriggerEnterAutoTime() {  // 获取进入自动触发的时间方法
        long time =  // 计算基础时间
                (long)
                        (scope.timeScale_mainBoard()  // 获取主时基
                                        * scope.screenNum_Main(scope.isRun(true))  // 获取屏数
                                        * ScopeBase.getHorizonGridCnt()  // 获取水平格数
                                        * 1500  // 乘以系数
                                + 0.1);  // 加0.1避免浮点误差
        int type = TriggerFactory.getTriggerType();  // 获取触发类型
        if (type == Trigger.TRIG_TYPE_NEDGE) {  // 判断：是否为非边沿触发
            if (time < 1000) time = 1000;  // 设置最小时间为1秒
            TriggerNEdge nEdge = (TriggerNEdge) TriggerFactory.getTriggerObj();  // 获取非边沿触发对象

            long idleTime = nEdge.getIdleTime() * 8 * 2;  // 计算空闲时间
            if (time * 1e6 < idleTime) {  // 判断：时间是否小于空闲时间
                time = (long) (idleTime / 1e6);  // 设置时间为空闲时间
            }  // 判断结束
        }  // 判断结束
        if ((type == Trigger.TRIG_TYPE_SERIAL1)  // 判断：是否为串行触发1
                || (type == Trigger.TRIG_TYPE_SERIAL2)  // 判断：是否为串行触发2
                || (type == Trigger.TRIG_TYPE_SERIAL3)  // 判断：是否为串行触发3
                || (type == Trigger.TRIG_TYPE_SERIAL4)) {  // 判断：是否为串行触发4
            time = 1000;  // 设置固定时间为1秒
        }  // 判断结束
        // if(time < 1000) time = 1000;  // 已注释：设置最小时间

        if (time < 200) time = 200;  // 设置最小时间为200毫秒
        // lao yang rang da yu 5秒  // 注释：老杨让大于5秒
        if (time > 5 * 1000) {  // 判断：时间是否超过5秒
            time = 5 * 1000;  // 设置最大时间为5秒
        }  // 判断结束
        return (int) time;  // 返回时间值
    }  // 方法结束

    /**
     * 生成自动触发时间配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置自动触发模式的等待时间</li>
     *   <li>根据时基调整触发参数</li>
     *   <li>校准模式使用最小时间</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_trigAutoTime(int fpgaIdx) {  // 生成自动触发时间配置方法
        FPGAReg_TRIG_AUTO_TRIG_TIME reg =  // 获取自动触发时间寄存器
                (FPGAReg_TRIG_AUTO_TRIG_TIME) getFPGAReg(fpgaIdx, FPGAReg.FPGA_TRIG_AUTO_TRIG_TIME);
        if (isCalibrate()) reg.setTrigTime(1, 0);  // 判断：是否校准，设置最小时间
        else {  // 否则：正常模式
            int ixx = 0;  // 附加参数
            int ix = getTriggerEnterAutoTime();  // 获取自动触发时间
            if (ix < 0 || ix > ((1 << 25) - 1)) ix = (1 << 25) - 1;  // 钳位到25位最大值
            int x = HorizontalAxis.getInstance().getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);  // 获取时基档位

            if (x <= HorizontalAxis.TSI_20mS) ix |= (3 << 30);  // 设置时基标志位（20ms及以上）
            else if (x == HorizontalAxis.TSI_10mS) ix |= (2 << 30);  // 设置时基标志位（10ms）
            else if (x == HorizontalAxis.TSI_5mS) ix |= (1 << 30);  // 设置时基标志位（5ms）

            if (x <= HorizontalAxis.TSI_20mS) {  // 判断：时基是否大于等于20ms
                ixx = 100;  // 设置附加参数为100
            } else if (x <= HorizontalAxis.TSI_10mS) {  // 判断：时基是否大于等于10ms
                ixx = 50;  // 设置附加参数为50
            } else if (x <= HorizontalAxis.TSI_5mS) {  // 判断：时基是否大于等于5ms
                ixx = 20;  // 设置附加参数为20
            } else {  // 否则：其他时基
                ixx = 9;  // 设置附加参数为9
            }  // 判断结束
            reg.setTrigTime(ix, ixx);  // 设置触发时间参数
        }  // 判断结束

        sendCommand(reg);  // 发送命令
    }  // 方法结束

    /**
     * 设置触发回差值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据档位计算触发电平回差值</li>
     *   <li>防止触发抖动</li>
     *   <li>回差值与AD值扩展倍数相关</li>
     * </ul>
     * 
     * @param x_rise_fall 回差值数组，[0]为上升沿回差，[1]为下降沿回差
     */
    public void setTrigHuiCha(int[] x_rise_fall /*Integer x_rise,Integer x_fall*/) {  // 设置触发回差值方法

        // 注意：由于fpga使用触发电平时扩展2倍，所以回差值也是相对于AD值扩展2倍
        Channel channel;  // 通道对象
        int tmp = 0;  // 临时变量
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        Trigger trigger = TriggerFactory.getInstance().getTrigger();  // 获取触发对象
        x_rise_fall[0] = 8;  // 初始化上升沿回差值为8
        x_rise_fall[1] = 8;  // 初始化下降沿回差值为8

        int trigSrcCnt = trigger.getTriggerSourceCnt();  // 获取触发源数量
        if (trigSrcCnt == 1) {  // 判断：是否为单触发源
            channel = ChannelFactory.getDynamicChannel(trigger.getTriggerSource());  // 获取触发源通道
            if (channel == null) return;  // 判断：通道是否有效，无效则返回
            int y = channel.getVScaleId();  // 获取通道档位ID
            // 触发源为静态变量，所以以何种触发类型去获取都一样，这里用边沿触发

            switch (y) {  // 根据档位设置回差值
                case VerticalAxis.DANG_500uV:  // 500微伏档位
                    x_rise_fall[0] = 50;  // 设置上升沿回差值为50
                    break;  // 跳出switch
                case VerticalAxis.DANG_1mV:  // 1毫伏档位
                    x_rise_fall[0] = 38;  // 设置上升沿回差值为38
                    break;  // 跳出switch
                case VerticalAxis.DANG_2mV:  // 2毫伏档位
                    x_rise_fall[0] = 20;  // 设置上升沿回差值为20
                    break;  // 跳出switch
                case VerticalAxis.DANG_5mV:
                    x_rise_fall[0] = 20;  // 设置上升沿回差值为20
                    break;  // 跳出switch
                case VerticalAxis.DANG_10mV:  // 10毫伏档位
                case VerticalAxis.DANG_20mV:  // 20毫伏档位
                case VerticalAxis.DANG_50mV:  // 50毫伏档位
                    x_rise_fall[0] = 10;  // 设置上升沿回差值为10，注释：4
                    break;  // 跳出switch
                default:  // 默认情况
                    x_rise_fall[0] = 8;  // 设置上升沿回差值为8
                    break;  // 跳出switch
            }  // switch结束
            x_rise_fall[0] += 8;  // 回差值加8

            x_rise_fall[1] = x_rise_fall[0];  // 下降沿回差值等于上升沿回差值

            switch (trigger.getTriggerType()) {  // 根据触发类型调整回差值
                case Trigger.TRIG_TYPE_LOGIC:  // 逻辑触发
                    TriggerLogic triggerLogic = (TriggerLogic) trigger;  // 获取逻辑触发对象
                    int logicCondition = triggerLogic.getCondition();  // 获取逻辑条件
                    if (logicCondition == Trigger.TRIGGER_RELATION_TRUE) {  // 判断：条件为真
                        x_rise_fall[0] = 0;  // 清零上升沿回差值
                    } else if (logicCondition == Trigger.TRIGGER_RELATION_FALSE) {  // 判断：条件为假
                        x_rise_fall[1] = 0;  // 清零下降沿回差值
                    }  // 判断结束
                    break;  // 跳出switch
                case Trigger.TRIG_TYPE_EDGE:  // 边沿触发
                    {
                        TriggerEdge triggerEdge = (TriggerEdge) trigger;  // 获取边沿触发对象
                        int tedge = triggerEdge.getTriggerEdge();  // 获取边沿类型
                        if (tedge == TriggerEdge.TET_ASC)  // 判断：是否为上升沿，注释：上升沿
                        {
                            x_rise_fall[0] = 0;  // 清零上升沿回差值
                        } else if (tedge == TriggerEdge.TET_DSC)  // 判断：是否为下降沿，注释：下降沿
                        {
                            x_rise_fall[1] = 0;  // 清零下降沿回差值
                        }  // 判断结束
                        //                    else {  // 已注释：其他情况
                        //                        //x_rise_fall[0] = 0;  // 已注释：清零上升沿回差值
                        //                        //x_rise_fall[1] = 0;  // 已注释：清零下降沿回差值
                        //                    }  // 已注释：判断结束
                    }  // 代码块结束
                    break;  // 跳出switch
                case Trigger.TRIG_TYPE_LOW_PULSE:  // 矮脉冲触发，注释：矮脉冲
                    {
                        TriggerRunt triggerRunt = (TriggerRunt) trigger;  // 获取矮脉冲触发对象
                        int polarity = triggerRunt.getPolarity();  // 获取极性
                        if (polarity == TriggerRunt.RUNT_POLARITY_POSITIVE) {  // 判断：是否为正极性
                            x_rise_fall[1] = 0;  // 清零下降沿回差值
                        } else if (polarity == TriggerRunt.RUNT_POLARITY_NEGATIVE) {  // 判断：是否为负极性
                            x_rise_fall[0] = 0;  // 清零上升沿回差值
                        }  // 判断结束
                        break;  // 跳出switch
                    }  // 代码块结束
                case Trigger.TRIG_TYPE_PULSE:  // 脉宽触发，注释：脉宽触发
                    {
                        TriggerPulseWidth triggerPulseWidth = (TriggerPulseWidth) trigger;  // 获取脉宽触发对象
                        if (triggerPulseWidth.getPolarity()  // 判断：极性是否为正
                                == TriggerPulseWidth.PW_POLARITY_POSITIVE) {  // 判断：正极性
                            x_rise_fall[1] = 0;  // 清零下降沿回差值
                        } else {  // 否则：负极性
                            x_rise_fall[0] = 0;  // 清零上升沿回差值
                        }  // 判断结束
                        break;  // 跳出switch
                    }  // 代码块结束
                case Trigger.TRIG_TYPE_TIMEOUT:  // 超时触发，注释：超时触发
                    {
                        TriggerTimeOut triggerTimeOut = (TriggerTimeOut) trigger;  // 获取超时触发对象
                        if (triggerTimeOut.getPolarity()  // 判断：极性是否为正
                                == TriggerTimeOut.TIMEOUT_POLARITY_POSITIVE) {  // 判断：正极性
                            x_rise_fall[0] = 0;  // 清零上升沿回差值
                        } else {  // 否则：负极性
                            x_rise_fall[1] = 0;  // 清零下降沿回差值
                        }  // 判断结束
                        break;  // 跳出switch
                    }  // 代码块结束
                default:  // 默认情况
                    break;  // 跳出switch
//                case Trigger.TRIG_TYPE_LOW_PULSE: // 矮脉冲  // 已注释：矮脉冲触发
                case Trigger.TRIG_TYPE_SLOPE:  // 斜率触发，注释：斜率触发
                    x_rise_fall[0] = 0;  // 清零上升沿回差值
                    x_rise_fall[1] = 0;  // 清零下降沿回差值
                    break;  // 跳出switch
                case Trigger.TRIG_TYPE_NEDGE:  // 第n边沿触发，注释：第n边沿触发
                    {
                        TriggerNEdge triggerNEdge = (TriggerNEdge) trigger;  // 获取非边沿触发对象
                        int edgeType = triggerNEdge.getSlope();  // 获取边沿类型
                        if (edgeType == TriggerNEdge.TET_ASC) {  // 判断：是否为上升沿
                            x_rise_fall[0] = 0;  // 清零上升沿回差值
                        } else if (edgeType == TriggerNEdge.TET_DSC) {  // 判断：是否为下降沿
                            x_rise_fall[1] = 0;  // 清零下降沿回差值
                        }  // 判断结束
                        break;  // 跳出switch
                    }  // 代码块结束
            }  // switch结束
        }  // 判断结束
        // Logger.d("x_rise = " + x_rise_fall[0] + ",x_fall = " + x_rise_fall[1]);  // 已注释：输出调试日志
    }  // 方法结束

    /**
     * 设置触发电平寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置触发电平值和回差值到FPGA寄存器</li>
     *   <li>支持多触发源</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param value 触发电平值
     * @param trigIdx 触发源索引
     * @param riseHuiCha 上升沿回差值
     * @param fallHuiCha 下降沿回差值
     */
    public void setTriggerLevelReg(
            int fpgaIdx, int value, int trigIdx, int riseHuiCha, int fallHuiCha) {  // 设置触发电平寄存器方法

        if (!ChannelFactory.isDynamicCh(trigIdx)
                || !isFpgaChIdx(fpgaIdx,trigIdx))  // 判断：触发源是否有效，注释：NONE触发源不用处理
            return;  // 无效则返回
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        FPGAReg reg = getFPGAReg(fpgaIdx, FPGAReg.FPGA_TRIG_LEVEL);  // 获取触发电平寄存器

        int x1 = value + 512 * 32 + riseHuiCha * 32;  // 计算上升沿触发电平值（含回差）
        int x2 = value + 512 * 32 - fallHuiCha * 32;  // 计算下降沿触发电平值（含回差）

        int bOF = 0;  // 溢出标志，初始为0
        if (x1 > 32767 || x1 < 0) bOF = 1;  // 判断：上升沿值是否溢出
        if (x2 > 32767 || x2 < 0) bOF = 1;  // 判断：下降沿值是否溢出
        int cnt = 0;  // 通道计数
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        boolean[] sel = new boolean[ChannelFactory.CH_CNT];  // 通道选择数组
        cnt = scope.getChannelSampOnCnt(scope.isRun(true), sel);  // 获取采样通道数
        int idx = 0;  // 触发源索引
        switch (cnt) {  // 根据通道数计算索引
            case 2:  // 2通道模式
                cnt = 4;  // 设置计数为4
                idx = 0;  // 设置索引为0
                break;  // 跳出switch
            case 4:  // 4通道模式
                for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                    if (sel[i]) {  // 判断：通道是否选中
                        if (i == trigIdx) idx = 0;  // 判断：是否为触发源
                        else idx = 2;  // 否则：设置索引为2
                        break;  // 跳出循环
                    }  // 判断结束
                }  // 循环结束
                cnt = 2;  // 设置计数为2
                break;  // 跳出switch
            case 8:  // 8通道模式
            default:  // 默认情况
                idx = trigIdx-beginIdx;  // 计算触发源索引
                if(idx < 0) idx = 0;  // 钳位到最小值
                cnt = 1;  // 设置计数为1
                break;  // 跳出switch
        }  // switch结束
        cnt = cnt + idx;  // 计算结束索引
        for (int i = idx; i < cnt; i++) {  // 循环：设置触发电平
            reg.setVal(i, 0, 15, x1);  // 设置上升沿触发电平值
            reg.setVal(i, 15, 1, bOF);  // 设置溢出标志
            reg.setVal(i, 16, 15, x2);  // 设置下降沿触发电平值
            reg.setVal(i, 31, 1, bOF);  // 设置溢出标志
        }  // 循环结束
    }  // 方法结束

    /**
     * 设置触发电平寄存器（第二触发源）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置第二触发源的触发电平值</li>
     *   <li>固定使用索引4</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param value 触发电平值
     * @param riseHuiCha 上升沿回差值
     * @param fallHuiCha 下降沿回差值
     */
    public void setTriggerLevelReg_snd(int fpgaIdx, int value, int riseHuiCha, int fallHuiCha) {  // 设置触发电平寄存器方法（第二触发源）
        FPGAReg reg = getFPGAReg(fpgaIdx, FPGAReg.FPGA_TRIG_LEVEL);  // 获取触发电平寄存器
        int x1 = value + 512 * 32 + riseHuiCha * 32;  // 计算上升沿触发电平值（含回差）
        int x2 = value + 512 * 32 - fallHuiCha * 32;  // 计算下降沿触发电平值（含回差）

        int bOF = 0;  // 溢出标志，初始为0，注释：溢出标记=1溢出
        if (x1 > 32767 || x1 < 0) bOF = 1;  // 判断：上升沿值是否溢出
        if (x2 > 32767 || x2 < 0) bOF = 1;  // 判断：下降沿值是否溢出

        reg.setVal(4, 0, 15, x1);  // 设置索引4的上升沿触发电平值
        reg.setVal(4, 15, 1, bOF);  // 设置索引4的溢出标志
        reg.setVal(4, 16, 15, x2);  // 设置索引4的下降沿触发电平值
        reg.setVal(4, 31, 1, bOF);  // 设置索引4的溢出标志
    }  // 方法结束

    /**
     * 根据通道档位获取AD值与像素的转换系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取通道的AD值与像素转换系数</li>
     *   <li>支持自动调整档位</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param autoAdj 是否自动调整档位
     * @return AD值与像素的转换系数
     */
    public double ADGear2PixBuf(int chIdx, boolean autoAdj) {  // 根据通道档位获取AD值与像素转换系数方法

        int adGear = 0;  // AD档位，初始为0
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取通道对象
        if (channel != null) {  // 判断：通道是否有效
            adGear = channel.getVScaleId();  // 获取通道档位ID
            if (!Scope.getInstance().isRun(true) && autoAdj) {  // 判断：是否停止运行且需要自动调整
                VerticalAxis verticalAxis = ScopeFrozen.getInstance().getChVertical(chIdx);  // 获取冻结状态的垂直轴
                if (verticalAxis != null) {  // 判断：垂直轴是否有效
                    adGear = verticalAxis.getScaleId();  // 获取冻结状态的档位ID
                }  // 判断结束
            }  // 判断结束
        }  // 判断结束

        return ADGear2PixBuf_fromVScale(chIdx, adGear);  // 返回AD值与像素转换系数
    }  // 方法结束

    /**
     * 生成触发电平配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算并设置触发电平值</li>
     *   <li>考虑触发灵敏度和回差值</li>
     *   <li>支持多触发源</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_trigLevel(int fpgaIdx) {  // 生成触发电平配置方法

        FPGAReg reg = getFPGAReg(fpgaIdx, FPGAReg.FPGA_TRIG_LEVEL);  // 获取触发电平寄存器
        int[] x_rise_fall = {0, 0};  // 回差值数组，初始为0

        setTrigHuiCha(x_rise_fall);  // 设置回差值
        double sen = TriggerFactory.getInstance().getTriggerCommon().getTriggerSensitivity();  // 获取触发灵敏度
        if(sen < 0.1) sen = 0.1;  // 钳位最小灵敏度
        else if(sen > 1) sen = 1;  // 钳位最大灵敏度

        double r = hwConfig.getTriggerHuiChaFactor() * sen / 0.5;  // 计算回差系数
        x_rise_fall[0] = (int)Math.round(r * x_rise_fall[0]);  // 调整上升沿回差值
        x_rise_fall[1] = (int)Math.round(r * x_rise_fall[1]);  // 调整下降沿回差值

        Trigger trigger = TriggerFactory.getInstance().getTrigger();  // 获取触发对象
        switch (trigger.getTriggerType()) {  // 根据触发类型处理
            case Trigger.TRIG_TYPE_EDGE:  // 边沿触发
            case Trigger.TRIG_TYPE_PULSE:  // 脉宽触发
            case Trigger.TRIG_TYPE_LOW_PULSE:  // 矮脉冲
            case Trigger.TRIG_TYPE_SLOPE:  // 斜率触发
            case Trigger.TRIG_TYPE_NEDGE:  // 非边沿触发
            case Trigger.TRIG_TYPE_TIMEOUT:  // 超时触发
            case Trigger.TRIG_TYPE_LOGIC:  // 逻辑触发
                {
                    for (int k = 0; k < trigger.getTriggerSourceCnt(); k++) {  // 循环：遍历所有触发源
                        int trigSouce = trigger.getTriggerSource(k);  // 获取触发源索引
                        Channel channel = ChannelFactory.getDynamicChannel(trigSouce);  // 获取触发源通道
                        if (channel != null) {  // 判断：通道是否有效
                            int cnt = trigger.getSrcTriggerLevelCnt();  // 获取触发源触发电平数量
                            for (int i = 0; i < cnt; i++) {  // 循环：遍历所有触发电平
                                double level = trigger.getTriggerLevel(i, trigSouce).getPos();  // 获取触发电平位置
                                level += channel.getPosFix();  // 加上位置修正值

                                double fx =  // 计算FPGA触发电平值
                                        level
                                                        * hwConfig.getTriggerLevelFactor()  // 乘以触发电平因子
                                                        / channel.getYFactor()  // 除以Y因子
                                                        / ADGear2PixBuf(trigSouce, false)  // 除以AD像素系数
                                                + (level > 0 ? 0.5 : -0.5);  // 四舍五入

                                if (i == 0)  // 判断：是否为第一个触发电平
                                    setTriggerLevelReg(  // 设置触发电平寄存器
                                            fpgaIdx,  // FPGA索引
                                            (int) fx,  // 触发电平值
                                            trigSouce,  // 触发源索引
                                            x_rise_fall[0],  // 上升沿回差值
                                            x_rise_fall[1]);  // 下降沿回差值
                                else  // 否则：第二个触发电平
                                    setTriggerLevelReg_snd(  // 设置触发电平寄存器（第二触发源）
                                            fpgaIdx, (int) fx, x_rise_fall[0], x_rise_fall[1]);  // 参数
                            }  // 循环结束
                        }  // 判断结束
                    }  // 循环结束
                }  // 代码块结束
            case Trigger.TRIG_TYPE_VIDEO:  // 视频触发
            case Trigger.TRIG_TYPE_SERIAL1:  // 串行触发1
            case Trigger.TRIG_TYPE_SERIAL2:  // 串行触发2
            case Trigger.TRIG_TYPE_SERIAL3:  // 串行触发3
            case Trigger.TRIG_TYPE_SERIAL4:  // 串行触发4
            default:  // 默认情况
                break;  // 跳出switch
        }  // switch结束

        sendCommand(reg);
    }

    /**
     * AC耦合滤波器系数（10Hz）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于触发耦合的AC模式滤波</li>
     *   <li>截止频率约10Hz</li>
     *   <li>数组格式：[配置值, p4, p3, p2, p1]</li>
     * </ul>
     */
    int[] cnTrigCoup_AC = {  // AC耦合滤波器系数数组，截止频率10Hz
        0x0010, 65534, // p4：第4级滤波器系数
        65534, // p3：第3级滤波器系数
        65535, // p2：第2级滤波器系数
        65536, // p1：第1级滤波器系数
    };  // 数组结束
    
    /**
     * 低频抑制滤波器系数（50kHz）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于触发耦合的低频抑制模式</li>
     *   <li>截止频率约50kHz</li>
     *   <li>抑制低频信号干扰</li>
     * </ul>
     */
    int[] cnTrigCoup_LFRS = {  // 低频抑制滤波器系数数组，截止频率50kHz
        0x0010, 65290, // p4：第4级滤波器系数
        65331, // p3：第3级滤波器系数
        65413, // p2：第2级滤波器系数
        65495, // p1：第1级滤波器系数
    };  // 数组结束
    
    /**
     * 高频抑制滤波器系数（50kHz）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于触发耦合的高频抑制模式</li>
     *   <li>截止频率约50kHz</li>
     *   <li>抑制高频噪声干扰</li>
     * </ul>
     */
    int[] cnTrigCoup_HFRS = {  // 高频抑制滤波器系数数组，截止频率50kHz
        0x0000, 65289, // p4：第4级滤波器系数
        41, // p3：第3级滤波器系数
        41, // p2：第2级滤波器系数
        41, // p1：第1级滤波器系数
    };  // 数组结束
    
    /**
     * 噪声抑制滤波器系数（10MHz）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于触发耦合的噪声抑制模式</li>
     *   <li>截止频率约10MHz</li>
     *   <li>抑制高频噪声干扰</li>
     * </ul>
     */
    int[] cnTrigCoup_NOISERS = {  // 噪声抑制滤波器系数数组，截止频率10MHz
        0x0000, 30530, // p4：第4级滤波器系数
        4427, // p3：第3级滤波器系数
        5710, // p2：第2级滤波器系数
        7366, // p1：第1级滤波器系数
    };  // 数组结束

    /**
     * 生成触发耦合配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置触发耦合模式（直流、交流、高频抑制、低频抑制、噪声抑制）</li>
     *   <li>设置对应的滤波器参数</li>
     *   <li>仅对边沿触发有效</li>
     * </ul>
     * 
     * <p><b>耦合模式说明：</b>
     * <pre>
     * 直流耦合(DC)：直通，无滤波
     * 交流耦合(AC)：截止频率10Hz，滤除直流分量
     * 高频抑制(HFRS)：截止频率50kHz，滤除高频噪声
     * 低频抑制(LFRS)：截止频率50kHz，滤除低频干扰
     * 噪声抑制(NOISERS)：截止频率10MHz，滤除高频噪声
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_trigCouple(int fpgaIdx) {  // 生成触发耦合配置方法
        FPGAReg_TRIG_COUPLE reg =  // 获取触发耦合寄存器
                (FPGAReg_TRIG_COUPLE) getFPGAReg(fpgaIdx, FPGAReg.FPGA_TRIG_COUPLE);

        TriggerEdge triggerEdge =  // 获取边沿触发对象
                (TriggerEdge) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_EDGE);

        int[] coef_fpga = null;  // 滤波器系数数组，初始为null
        reg.setVal(0, 0);  // 重置寄存器值
        switch (triggerEdge.getTriggerCouple()) {  // 根据触发耦合类型设置参数
            case TriggerEdge.COUPLING_DIRECT:  // 直流耦合
                coef_fpga = null;  // 无需滤波器系数
                reg.setDC(1);  // 设置直流耦合标志
                break;  // 跳出case
            case TriggerEdge.COUPLING_AC:  // 交流耦合
                coef_fpga = cnTrigCoup_AC;  // 使用AC耦合滤波器系数
                reg.setAC(1);  // 设置交流耦合标志
                break;  // 跳出case
            case TriggerEdge.COUPLING_HFRS:  // 高频抑制
                coef_fpga = cnTrigCoup_HFRS;  // 使用高频抑制滤波器系数
                reg.setHig(1);  // 设置高频抑制标志
                break;  // 跳出case
            case TriggerEdge.COUPLING_LFRS:  // 低频抑制
                coef_fpga = cnTrigCoup_LFRS;  // 使用低频抑制滤波器系数
                reg.setLow(1);  // 设置低频抑制标志
                break;  // 跳出case
            case TriggerEdge.COUPLING_NOISERS:  // 噪声抑制
                coef_fpga = cnTrigCoup_NOISERS;  // 使用噪声抑制滤波器系数
                reg.setNoi(1);  // 设置噪声抑制标志
                break;  // 跳出case
            default:  // 默认情况
                coef_fpga = null;  // 无需滤波器系数
                break;  // 跳出case
        }  // switch结束
        sendCommand(reg);  // 发送触发耦合命令
        /*
        if (coef_fpga != null) {  // 已注释：如果滤波器系数不为空
            FPGAReg regx = new FPGAReg(FPGAReg.FPGA_COUP_COEF, 20);  // 已注释：创建滤波器系数寄存器
            regx.setVal(coef_fpga);  // 已注释：设置滤波器系数
            sendCommand(regx);  // 已注释：发送命令
        }  // 已注释：判断结束
         */
    }  // 方法结束

    /**
     * 检查通道是否打开
     * 
     * @param chIdx 通道索引
     * @return true表示通道打开
     */
    private boolean isChOpen(int chIdx) {  // 检查通道是否打开方法
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取通道对象
        if (channel != null) {  // 判断：通道是否有效
            if (scope.isRun(true)) {  // 判断：是否运行中
                return channel.isOpen();  // 返回通道开关状态
            } else {  // 否则：停止状态
                return ScopeFrozen.getInstance().isChSamped(chIdx) && channel.isOpen();  // 返回采样状态和开关状态
            }  // 判断结束
        }  // 判断结束
        return false;  // 返回false，通道无效
    }  // 方法结束

    /**
     * 显示点阵图形成
     * 
     * @param fpgaIdx FPGA索引
     */
    public void disDianZhengTuCheng(int fpgaIdx) {  // 显示点阵图形成方法

        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        FPGAReg_CH_DISPLAY chDisplayReg =  // 获取通道显示寄存器
                (FPGAReg_CH_DISPLAY) getFPGAReg(fpgaIdx, FPGAReg.FPGA_CH_DISPLAY);
        Channel channel;  // 通道对象
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            chDisplayReg.setChEnable(i - beginIdx, 0);  // 初始化通道使能为0
        }  // 循环结束
        boolean[] sle = new boolean[ChannelFactory.CH_CNT];  // 通道选择数组
        int ChOpenCnt = scope.getChannelSampOnCnt(scope.isRun(true), sle);  // 获取采样通道数
        switch (ChOpenCnt) {  // 根据通道数设置显示参数
            case 4:  // 4通道模式
            case 2:  // 2通道模式
                {
                    int k = 0;  // 通道计数
                    for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                        if (sle[i]) {  // 判断：通道是否选中
                            if (isChOpen(i)) {  // 判断：通道是否打开
                                chDisplayReg.setChEnable(k, 1);  // 设置通道使能
                                if (k == 0) chDisplayReg.setCh1Color(i - beginIdx);  // 设置通道1颜色
                                else chDisplayReg.setCh2Color(i - beginIdx);  // 设置通道2颜色
                            }  // 判断结束
                            k++;  // 通道计数加1
                        }
                    }  // 循环结束
                    break;  // 跳出代码块
                }  // 代码块结束

            case 8:  // 8通道模式
            default:  // 默认情况
                {
                    for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                        if (sle[i] && isChOpen(i)) {  // 判断：通道是否选中且打开
                            chDisplayReg.setChEnable(i - beginIdx, 1);  // 设置通道使能
                        }  // 判断结束
                    }  // 循环结束
                    break;  // 跳出代码块
                }  // 代码块结束
        }  // switch结束

        // 反向
        int bitsWidth;  // 位宽
        int bitsVal;  // 位值

        int val;  // 临时值
        int startBit = 0;  // 起始位

        switch (ChOpenCnt) {  // 根据通道数设置位宽和位值
            case 2:  // 2通道模式
                bitsWidth = 2;  // 设置位宽为2
                bitsVal = 0x03;  // 设置位值为0x03
                break;  // 跳出switch
            case 4:  // 4通道模式
            case 8:  // 8通道模式
            default:  // 默认情况
                bitsWidth = 1;  // 设置位宽为1
                bitsVal = 0x01;  // 设置位值为0x01
                break;  // 跳出switch
        }  // switch结束

        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            if (sle[i]) {  // 判断：通道是否选中
                channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
                if (channel != null) {  // 判断：通道是否有效
                    val = channel.isInvert() ? bitsVal : 0;  // 计算反相值
                    chDisplayReg.setVal(16 + startBit, bitsWidth, val);  // 设置反相标志
                    startBit += bitsWidth;  // 更新起始位
                }  // 判断结束
            }  // 判断结束
        }  // 循环结束

        sendCommand(chDisplayReg);  // 发送通道显示命令
    }  // 方法结束

    /**
     * 灰度映射
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据亮度设置灰度映射表</li>
     *   <li>用于波形显示的灰度调整</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param light 亮度值（0-100）
     */
    private void huidu_yingse(int fpgaIdx, int light) {  // 灰度映射方法
        int high = 200;  // 高亮度值
        int low = 150 * light / 100;  // 低亮度值
        int max1 = 25 * 2048 / 255;  // 最大值1
        int min1 = 1;  // 最小值

        high = 200 + 40 * light / 100;  // 调整高亮度值
        low = (150 + 50 * light / 100) * light / 100;  // 调整低亮度值
        max1 = 25 * 2047 / 255;  // 调整最大值1
        if (light < 20) max1 = max1 + (20 - light) * 1024 / 20;  // 低亮度时调整最大值

        FPGAReg xreg = new FPGAReg(FPGAReg.FPGA_GRAY_LEVEL, 2048);  // 创建灰度寄存器
        xreg.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        for (int n = 0; n < 2048; n++) {  // 循环：遍历所有灰度级
            double temp = n + 1;  // 计算临时值
            if (temp < min1) temp = temp * low / min1;  // 低灰度区域映射
            else if (temp < max1) temp = low + (high - low) * (temp - min1) / (max1 - min1);  // 中灰度区域映射
            else temp = high + (255 - high) * (temp - max1) / (2048 - max1);  // 高灰度区域映射
            xreg.byteBuffer.put(n + FPGAReg.FPGA_REG_HEADER_LEN, (byte) Math.round(temp));  // 写入灰度值
        }  // 循环结束

        sendCommand(xreg);  // 发送灰度映射命令
    }  // 方法结束

    /**
     * 生成显示模式配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置显示模式（矢量/点显示）</li>
     *   <li>设置波形类型和显示参数</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_disMode(int fpgaIdx) {  // 生成显示模式配置方法
        Display display = Display.getInstance();  // 获取显示管理实例
        FPGAReg_DIS_MODE reg = (FPGAReg_DIS_MODE) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DIS_MODE);  // 获取显示模式寄存器

        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true));  // 获取采样通道数

        switch (cnt) {  // 根据通道数设置显示模式
            case 2:  // 2通道模式
                reg.setChNum(0);  // 设置通道数为0
                break;  // 跳出switch
            case 4:  // 4通道模式
                reg.setChNum(2);  // 设置通道数为2
                break;  // 跳出switch
            case 8:  // 8通道模式
            default:  // 默认情况
                reg.setChNum(4);  // 设置通道数为4
                break;  // 跳出switch
        }  // switch结束
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            reg.setChValid(i - beginIdx, scope.isChannelInSample(i, scope.isRun(true)));  // 设置通道有效标志
        }  // 循环结束
        reg.setMaxGray(255);  // 设置最大灰度值为255
        reg.setZoom(display.isZoom() ? 1 : 0);  // 设置缩放标志
        reg.setNeedStaticBitBuf(1);  // 设置需要静态位缓冲

        reg.setNeedStaticWave(isCalibrate() ? 1 : 0);  // 设置需要静态波形
        reg.setDisType(display.getDrawType() == Display.DRAWTYPE_DOTS ? 1 : 0);  // 设置显示类型
        disDianZhengTuCheng(fpgaIdx);  // 生成点阵显示配置

        sendCommand(reg);  // 发送显示模式命令

        huidu_yingse(fpgaIdx, Display.getInstance().getBrightness());  // 设置灰度映射

        FPGAReg_DISP_RESOLUTION r =  // 获取显示分辨率寄存器
                (FPGAReg_DISP_RESOLUTION) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DISP_RESOLUTION);
        r.setWidth(ScopeBase.getWidth());  // 设置显示宽度
        r.setHeight(ScopeBase.getHeight());  // 设置显示高度
        sendCommand(r);  // 发送分辨率命令

        cmdTrigOffset(fpgaIdx);  // 设置触发偏移
    }  // 方法结束
    public void gntR_yuHui(int fpgaIdx) {  // 生成余晖显示配置方法
        //临时配合tBookMini网表，smart就没有这个参�?

        FPGAReg regx = new FPGAReg(FPGAReg.FPGA_DISP_YUHUI, 4);  // 创建余晖寄存器
        Display display = Display.getInstance();  // 获取显示管理实例
        regx.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        if (display.getPersistType() == Display.PERSIST_TYPE_NONE) {  // 判断：是否为无余晖模式
            regx.setVal(0, 32, 1 << 15);  // 设置无余晖标志，清除历史波形
        } else {  // 否则：有余晖模式
            regx.setVal(0, 32, 0);  // 清除无余晖标志，保留历史波形
        }  // 判断结束

        sendCommand(regx);  // 发送余晖配置命令
    }  // 方法结束

    public int num_chazhi(double chaZhiNumIn, int[] chaZhiNumOut) {  // 计算插值倍数方法
        if (chaZhiNumIn < 0.999) {  // 判断：插值倍数是否小于1
            if (chaZhiNumOut[0] != 0) chaZhiNumOut[0] = 0;  // 设置输出为0
            return 0;  // 返回0，无需插值
        }  // 判断结束

        // printf("chazhiNumIn=%f\n", chaZhiNumIn);

        // 处理带小数的插值倍数
        long k = (long) (chaZhiNumIn * 100 + 0.01);  // 乘以100转换为整数，避免浮点误差
        int base = 1;  // 基础倍数，初始为1
        if (k % 100 != 0) {  // 判断：是否有小数部分
            k *= 2;  // 乘以2
            base = 2;  // 设置基础倍数为2
            if (k % 100 != 0) {  // 判断：是否还有小数部分
                k *= 2;  // 乘以2
                base = 4;  // 设置基础倍数为4
                if (k % 100 != 0) {  // 判断：是否还有小数部分
                    k *= 2;  // 乘以2
                    base = 8;  // 设置基础倍数为8
                    if (k % 100 != 0) {  // 判断：是否还有小数部分
                        Log.e(TAG, "!!!!chaZhi error,chaZhiNumIn=" + chaZhiNumIn + ",k:" + k );  // 输出错误日志
                        return 0;  // 返回0，插值错误
                    }  // 判断结束
                }  // 判断结束
            }  // 判断结束
        }  // 判断结束

        k /= 100;  // 除以100，恢复原始倍数

        // 处理8倍�?0倍和80倍插值，处理后每�?.25个数�?
        int len = ScopeBase.getWidth() * base;  // 计算数据长度，屏幕宽度×基础倍数
        if (k % 8 == 0) {  // 判断：是否为8的倍数
            k = k * 5 / 4;  // 调整倍数
            len = len * 5 / 4;  // 调整长度
        }  // 判断结束

        if (k > 100) {  // 判断：倍数是否超过100
            k = 0;  // 设置为0
            len = 0;  // 设置长度为0
        }  // 判断结束
        if (chaZhiNumOut[0] != 0) chaZhiNumOut[0] = (int) k;  // 设置输出倍数
        return len;  // 返回数据长度
    }  // 方法结束

    public void gntR_Dis(int fpgaIdx) {  // 生成显示配置方法（简化版）
        gntR_Dis(fpgaIdx, scope.isRun(true), 0);  // 调用完整版方法
    }  // 方法结束

    private boolean bPrintf = true;  // 调试打印开关，初始为true

      /**
     * 生成显示配置方法，用于计算和配置FPGA波形显示相关参数
     * 包括插值计算、主视图和缩略视图的取数位置计算、数据显示范围等
     *
     * @param fpgaIdx FPGA索引，标识要配置的FPGA设备编号,1
     * @param isRun 运行状态标志，true表示示波器正在运行（采集数据），false表示停止状态,1
     * @param screenNum 屏幕数量，1表示单屏模式，其他值表示多屏模式,1
     */
    public void gntR_Dis(int fpgaIdx, boolean isRun, int screenNum) {  // 生成显示配置方法，1
        if (bPrintf) Logger.i("=============gntR_Dis()=======start===>");  // 输出调试日志，1
        Logger.i("isRun:" + isRun + " screenNum:" + screenNum);  // 输出参数日志，1
        Display display = Display.getInstance();  // 获取显示管理实例，1
        Scope scope = Scope.getInstance();  // 获取示波器核心实例，1

        ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();  // 获取冻结状态实例，1
        long zunDepth = scope.zunMemDepth(isRun);  // 获取存储深度，1
        double num1 = scope.screenNum_Main(isRun);  // 获取主视图屏数，1
        if (screenNum == 1) num1 = 1;  // 判断：是否为单屏模式，1
        int x0 = ScopeBase.getWidth();  // 获取屏幕宽度，1
        double num2 = 1;  // 缩放视图屏数，初始为1，1
        boolean zoom = display.isZoom();  // 获取缩放模式状态，1
        if (zoom) {  // 判断：是否为缩放模式，1
            num2 = scope.screenNum_zoom(isRun);  // 获取缩放视图屏数，1
            if (screenNum == 1) num2 = 1;  // 判断：是否为单屏模式，1
        }  // 判断结束，1

        double cols1, cols2;  // 存储深度对应的总列数，1
        cols1 = num1 * x0;  // 计算主视图总列数，1
        cols2 = num2 * x0;  // 计算缩放视图总列数，1
        if (cols1 < 1.0) cols1 = 1.0;  // 钳位最小值，1
        if (cols2 < 1.0) cols2 = 1.0;  // 钳位最小值，1
        if (bPrintf) {  // 判断：是否启用调试日志，1
            Logger.i("x0:" + x0 + " zoom:" + zoom + " zunDepth:" + zunDepth);  // 输出调试日志，1
            Logger.i("num1:" + num1 + " num2:" + num2 + " cols1:" + cols1 + " cols2:" + cols2);  // 输出调试日志，1
        }  // 判断结束，1

        num2 *= num1;  // 计算缩放视图相对屏数，1
        cols2 *= num1;  // 计算缩放视图相对列数，1

        // 1. 插值计算----------------------------------------------------------------------------------
        // 计算每列有多少组数据
        // 在zoom模式，只有放大窗口需要插值，缩略窗口不需要插?
        double lie = 0; // 定义插值相关的变量lie，用于后续计算，1

        int[] chaZhiNum = {1}; // 插值倍数（最终结果），1
        int noDisp = 0; // 定义无显示标志，0表示正常显示，非0表示不显示，1
        FPGAReg_DISP_CHA disChaZhiReg =
                (FPGAReg_DISP_CHA) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DISP_CHA1); // 获取FPGA插值寄存器对象，1
        disChaZhiReg.setNeedChaZhi(0); // 初始化不需要插值标志为0，1

        if (!isRun && !ScopeFrozen.getInstance().isValid()) { // 判断：如果示波器停止且冻结数据无效，1
            noDisp = 1; // 设置无显示标志为1，1
        } else { // 否则进行插值计算，1
            double temp; // 插值倍数，1
            if (zoom) { // 判断：如果是缩放模式，1
                temp = (num2 * x0) / zunDepth; // 计算缩放模式下的插值倍数，1
            } else { // 非缩放模式，1
                temp = (num1 * x0) / zunDepth; // 计算正常模式下的插值倍数，1
            }  // 判断结束，1

            if (bPrintf) Logger.i("chaZhiRateTemp:" + temp); // 输出临时插值倍率日志，1

            if (temp > 1.001) { // 判断：如果插值倍数大于1.001，需要进行插值处理，1
                double c = temp; // 保存原始插值倍数，1
                double [] r={1.6,3.2,160}; // 定义特殊插值倍率数组，用于模糊匹配，1
                for (double v: r) { // 遍历特殊倍率数组，1
                    if(DoubleUtil.FuzzyCompare(temp, v)){ // 判断：如果temp与特殊值模糊相等，1
                        temp  = temp * 5 / 4; // 将temp调整为原来的1.25倍，1
                        break; // 跳出循环，1
                    }  // 判断结束，1
                }  // 循环结束，1

                long k = DoubleUtil.floor(temp); // 对temp向下取整得到k，1
                if(k <= 100) { // 判断：如果k小于等于100，1
                    if (100 % k != 0) { // 判断：如果100不能被k整除，1
                        if (k % 8 == 0) { // 判断：如果k能被8整除，1
                            temp = temp * 5 / 4; // 将temp调整为原来的1.25倍，1
                        }  // 判断结束，1
                    }  // 判断结束，1
                }  // 判断结束，1

                r = new double[]{0.5,0.25}; // 重新定义小数部分匹配数组，1
                k = DoubleUtil.floor(temp); // 再次对temp向下取整，1
                for(int i=0;i < r.length;i++){ // 遍历小数部分数组，1
                    if(DoubleUtil.FuzzyCompare(temp -  k,r[i])){ // 判断：如果temp的小数部分与r[i]模糊相等，1
                        temp *= Math.pow(2,i + 1); // 将temp乘以2的(i+1)次方进行修正，1
                        break; // 跳出循环，1
                    }  // 判断结束，1
                }  // 循环结束，1

                int len = 0; // 定义长度变量，初始化为0，1
                k = DoubleUtil.floor(temp); // 再次对temp向下取整，1
                if(k <= 200){ // 判断：如果k小于等于200，1
                    if(k != 200) { // 判断：如果k不等于200，1
                        if (100 % k == 0) { // 判断：如果100能被k整除，1
                            len = (int)DoubleUtil.floor(ScopeBase.getWidth() * temp / c); // 计算len为屏幕宽度乘以temp除以原始倍率c，1
                        } else { // 否则，1
                            Log.e(TAG, "k:" + k + ",temp:" + temp); // 输出错误日志，1
                        }  // 判断结束，1
                        chaZhiNum[0] = (int)k; // 将k赋值给插值倍数数组的第一个元素，1
                    }else { // k等于200的情况，1
                        len = (int)ScopeBase.getWidth(); // len等于屏幕宽度，1
                        chaZhiNum[0] = 100; // 插值倍数设置为100，1
                    }  // 判断结束，1
                }  // 判断结束，1

                if (len == 0) { // 判断：如果len为0，1
                    noDisp = 2; // noDisp = 2;，1
                } else { // len不为0的情况，1
                    disChaZhiReg.setTotalMul(chaZhiNum[0]); // 设置总乘数为插值倍数，1
                    disChaZhiReg.setFirstCoefJianGe(100 / chaZhiNum[0]); // 设置首系数间隔为100除以插值倍数，1

                    if(k == 200){ // 判断：如果k等于200，1
                        chaZhiNum[0] = 200; // 将插值倍数设置为200，1
                    }  // 判断结束，1

                    disChaZhiReg.setDataNFirstCha(
                            len / chaZhiNum[0] + (len % chaZhiNum[0] == 0 ? 0 : 1)); // 设置首次插值数据数量为len除以插值倍数向上取整，1
                    disChaZhiReg.setNeedChaZhi(1); // 设置需要插值标志为1，1

                    disChaZhiReg.setChaZhi200(k > 100 ? 1 : 0); // 如果k大于100则设置200倍插值标志为1，否则为0，1
                }  // 判断结束，1

                if (bPrintf) Logger.i("chaZhiRate:" + temp + " - " + chaZhiNum[0] + ", len:" + len); // 输出插值倍率和长度日志，1

            }  // 判断结束，1

            lie = 1 / temp; // 计算lie为temp的倒数，1

            if (bPrintf) Logger.i("chaZhiRateTemp:" + temp + ", lie:" + lie); // 输出插值倍率和lie的日志，1

        }  // 判断结束，1
        // 注意：设置的值必须能被虚拟屏幕列数整除
        // 注：只有在示波器设置采样模式为平均或包络时才进行点阵抽样
        int cnDataCyNum1 = 350 * 1000; // 点阵抽样，1
        int cnDataCyNum2 = 280 * 1000; // 点阵抽样，1
        int cnDataCyNum3 = 175 * 1000; // 点阵抽样，1

        FPGAReg_DISP_PLACE_MAIN disWavePlaceMainReg =
                (FPGAReg_DISP_PLACE_MAIN) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DISP_PLACE_MAIN); // 获取主视图波形位置寄存器对象，1
        FPGAReg_DISP_WAVE disStaticWave =
                (FPGAReg_DISP_WAVE) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DISP_WAVE); // 获取静态波形寄存器对象，1
        long dataLen = 0; // 定义数据长度变量，初始化为0，1
        // 缩略视图的xstart和xend计算
        if (zoom) { // 判断：如果是缩放模式，1
            long datasPerLie; // 定义每列数据量变量，1
            FPGAReg_ZoomSmallPlace zoomSmallPlace =
                    (FPGAReg_ZoomSmallPlace) getFPGAReg(fpgaIdx, FPGAReg.FPGA_ZOOM_SMALL_PLACE); // 获取缩放小视图位置寄存器对象，1
            FPGAReg_DISP_SL disSlReg = (FPGAReg_DISP_SL) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DISP_SL); // 获取缩略视图显示寄存器对象，1
            FPGAReg_DISP_PLACE_SL dispWavePlaceSlReg =
                    (FPGAReg_DISP_PLACE_SL) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DISP_PLACE_SL); // 获取缩略视图波形位置寄存器对象，1

            datasPerLie = (int) (zunDepth *  4 / cols1 + 0.1); // 计算每列数据量为存储深度乘以4除以cols1并加0.1后取整，1
            if (datasPerLie == 0) datasPerLie = 1; // 防护一?，1

            if (bPrintf) { // 判断：如果启用调试日志，1
                Logger.i(TAG, "datasPerLie : " + datasPerLie); // 输出每列数据量日志，1
            }  // 判断结束，1

            dispWavePlaceSlReg.setNumsPerLie((int)(datasPerLie / 4)); // 设置缩略视图每列数据数量为datasPerLie除以4，1
            disWavePlaceMainReg.setNumsPerLie2((int)(datasPerLie % 4) << 2); // 设置主视图每列数据余数左移2位，1
            dispWavePlaceSlReg.setDaoShuLieNum((int)((262144 * 4) / datasPerLie)); // 设置倒数列数为262144乘以4除以datasPerLie，1
            dispWavePlaceSlReg.setNeedCouY(0); // 设置不需要Y轴耦合标志为0，1
            dispWavePlaceSlReg.setNumCouY(1); // 设置Y轴耦合数量为1，1

            long timeOneScreen = scope.timeOneScreen_main(); // 获取主视图一屏的时间，1

            double tm3; // 定义时间变量tm3，1
            tm3 = num1 * timeOneScreen / 2; // 计算tm3为num1乘以一屏时间除以2，1

            long halfDataTime = (long) (tm3 + 0.1); // 计算半数据时间为tm3加0.1后转换为长整型，1

            HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴实例，1
            long t_small = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_SMALL); // 获取小视图的时间位置，1

            long t = scopeFrozen.getTimePosOfView(); // 获取冻结视图的时间位置，1

            Log.d(TAG,"zoom t:" + t + ",halfDataTime:" + halfDataTime + ",t_small:" + t_small); // 输出调试日志，1

            if (t < -halfDataTime) { // 判断：如果t小于负的半数据时间，1
                t = 2 * halfDataTime + t_small; // 重新计算t为2倍半数据时间加t_small，1
            } else { // 否则，1
                t =
                        halfDataTime
                                - t
                                + t_small
                                + (t
                                % (1000
                                * 1000
                                * 10
                                / scope.maxSampClk())); // 数据头距离屏幕中心的时间计算，1
            }  // 判断结束，1

            long halfScreen = timeOneScreen / 2; // 计算半屏时间为oneScreenTime除以2，1

            int place = 0; // 取数相对位置，1
            int start = 0; // 显示起始位置，1
            int end = x0 - 1; // 显示结束位置，1

            if (t < halfScreen) { // 判断：如果t小于半屏时间，1

                // 数据头在屏幕中
                place = 0; // 设置取数位置为0，1
                BigInteger t3 = BigInteger.valueOf(halfScreen - t); // 创建BigInteger对象t3，值为halfScreen减t，1
                t3 = t3.multiply(BigInteger.valueOf(x0)); // t3乘以x0，1
                t3 = t3.divide(BigInteger.valueOf(timeOneScreen)); // t3除以timeOneScreen，1
                start = t3.intValue(); // 将t3转换为int赋值给start，1
                if (start >= x0) { // 判断：如果start大于等于x0，1
                    start = x0; // 设置start为x0，1
                }  // 判断结束，1
            } else { // start大于等于halfScreen的情况，1
                start = 0; // 设置start为0，1
                // 计算取数位置
                BigInteger t3 = BigInteger.valueOf(t - halfScreen); // 创建BigInteger对象t3，值为t减halfScreen，1
                t3 = t3.multiply(BigInteger.valueOf(zunDepth)); // t3乘以zunDepth，1
                t3 = t3.divide(BigInteger.valueOf(halfDataTime * 2)); // t3除以halfDataTime的2倍，1
                if (t3.compareTo(BigInteger.valueOf(zunDepth)) >= 0) { // 判断：如果t3大于等于zunDepth，1
                    t3 = BigInteger.valueOf(zunDepth); // 设置t3为zunDepth，1
                }  // 判断结束，1
                place = t3.intValue(); // 将t3转换为int赋值给place，1
            }  // 判断结束，1

            { // 代码块开始，计算end位置，1
                BigInteger t3 = BigInteger.valueOf(halfDataTime * 2); // 创建BigInteger对象t3，值为halfDataTime的2倍，1
                t3 = t3.subtract(BigInteger.valueOf(t)); // t3减去t，1
                t3 = t3.add(BigInteger.valueOf(halfScreen)); // t3加上halfScreen，1
                if (t3.compareTo(BigInteger.ZERO) < 0) { // 判断：如果t3小于0，1
                    start = x0; // 设置start为x0，1
                    end = x0; // 设置end为x0，1
                } else if (t3.compareTo(BigInteger.valueOf(oneScreenTime)) < 0) { // 判断：如果t3小于oneScreenTime，1
                    BigInteger t2 =
                            t3.multiply(BigInteger.valueOf(x0))
                                    .divide(BigInteger.valueOf(oneScreenTime)); // 计算t2为t3乘以x0除以oneScreenTime，1
                    end = t2.intValue() - 1; // 设置end为t2的int值减1，1
                    if (isRun) { // 判断：如果示波器正在运行，1
                        long a = timeOneScreen / x0; // 计算a为timeOneScreen除以x0，1
                        t3 = BigInteger.valueOf(timeOneScreen).subtract(t3); // t3等于timeOneScreen减去t3，1
                        long b = t3.longValue(); // 将t3转换为long赋值给b，1
                        if (bPrintf) Logger.i("b:" + b + " a:" + a); // 输出b和a的日志，1
                        if (b > 0 && b < a) { // 判断：如果b大于0且小于a，1
                            end++; // end自增1，1
                        }  // 判断结束，1
                    }  // 判断结束，1
                }  // 判断结束，1
            }  // 代码块结束，1
            dataLen = datasPerLie * (end + 1 - start) / 4; // 计算数据长度为datasPerLie乘以(end+1-start)除以4，1

            if (isRun) { // 判断：如果示波器正在运行，1

                disSlReg.setXStart(0); // 设置缩略视图X轴起始位置为0，1
                disSlReg.setXEnd(x0 - 1); // 设置缩略视图X轴结束位置为x0-1，1

                zoomSmallPlace.setPlace(place); // 设置缩放小视图的位置为place，1
                zoomSmallPlace.setNums((int)dataLen); // 设置缩放小视图的数据数量为dataLen，1
            }
            else
            { // 示波器停止的情况，1

                int validLie = scopeFrozen.getVaildAddr(); // 获取冻结状态的有效地址，1
                if (scopeFrozen.isRool()) { // 判断：如果是滚动模式，1
                    dataLen = ((x0 - validLie) * zunDepth / x0); // 计算数据长度为(x0-validLie)乘以zunDepth除以x0，1
                    start = (int)(end - dataLen * 4 / datasPerLie + 1); // 计算start为end减去dataLen乘以4除以datasPerLie加1，1
                    if (start < 0) start = 0; // 如果start小于0则设置为0，1
                    disSlReg.setXStart(start); // 设置缩略视图X轴起始位置为start，1
                    disSlReg.setXEnd(end); // 设置缩略视图X轴结束位置为end，1
                    place = (int)((end - start + 1) * datasPerLie / 4); // 计算place为(end-start+1)乘以datasPerLie除以4，1

                    zoomSmallPlace.setPlace((int)(dataLen - place)); // 设置缩放小视图的位置为dataLen减place，1
                    zoomSmallPlace.setNums(place); // 设置缩放小视图的数据数量为place，1
                } else if (scopeFrozen.isSlowScale()) { // 判断：如果是慢扫描模式，1
                    dataLen = (int) ((validLie + 1) * zunDepth / x0); // 计算数据长度为(validLie+1)乘以zunDepth除以x0，1
                    validLie = (int)(dataLen * 4 / datasPerLie); // 重新计算validLie为dataLen乘以4除以datasPerLie，1
                    int e = start + validLie - 1; // 计算e为start加validLie减1，1
                    if (e < end) { // 判断：如果e小于end，1
                        end = e; // 设置end为e，1
                    }  // 判断结束，1
                    dataLen = (end - start + 1) * datasPerLie / 4; // 重新计算数据长度，1
                    disSlReg.setXStart(start); // 设置缩略视图X轴起始位置为start，1
                    disSlReg.setXEnd(end); // 设置缩略视图X轴结束位置为end，1
                    zoomSmallPlace.setPlace(place); // 设置缩放小视图的位置为place，1
                    zoomSmallPlace.setNums((int)dataLen); // 设置缩放小视图的数据数量为dataLen，1

                    if (scopeFrozen.isSegmentEnable()) { // 判断：如果分段功能启用，1
                        disSlReg.setXLastStart(0); // 设置缩略视图上次X轴起始位置为0，1
                        disSlReg.setXLastEnd(x0 - 1); // 设置缩略视图上次X轴结束位置为x0-1，1
                    }  // 判断结束，1

                } else { // 既不是滚动也不是慢扫描模式，1
                    disSlReg.setXStart(start); // 设置缩略视图X轴起始位置为start，1
                    disSlReg.setXEnd(end); // 设置缩略视图X轴结束位置为end，1
                    zoomSmallPlace.setPlace(place); // 设置缩放小视图的位置为place，1
                    zoomSmallPlace.setNums((int)dataLen); // 设置缩放小视图的数据数量为dataLen，1
                }  // 判断结束，1
                if (noDisp == 1) { // 判断：如果无显示标志为1，1
                    disSlReg.setXStart(x0); // 设置缩略视图X轴起始位置为x0，1
                    disSlReg.setXEnd(x0); // 设置缩略视图X轴结束位置为x0，1
                }  // 判断结束，1
            }  // 判断结束，1
            sendCommand(disSlReg); // 发送缩略视图显示寄存器命令，1
            sendCommand(dispWavePlaceSlReg); // 发送缩略视图波形位置寄存器命令，1
            sendCommand(zoomSmallPlace); // 发送缩放小视图位置寄存器命令，1
        } else { // 非缩放模式，1
            disWavePlaceMainReg.setNumsPerLie2(0); // 设置主视图每列数据余数为0，1
        }  // 判断结束，1

        // config.flagNoDispInterpWave(1) = (noDisp?true:false);
        if (noDisp != 0) { // 判断：如果无显示标志不为0，1
            // 此时波形数据无效，对应测量和光标无效
            disWavePlaceMainReg.setXStart(x0); // 设置主视图X轴起始位置为x0，1
            disWavePlaceMainReg.setXEnd(x0); // 设置主视图X轴结束位置为x0，1
            disWavePlaceMainReg.setLastXStart(x0); // 设置主视图上次X轴起始位置为x0，1
            disWavePlaceMainReg.setLastXEnd(x0); // 设置主视图上次X轴结束位置为x0，1
            disWavePlaceMainReg.setLen(0); // 设置主视图长度为0，1
            disWavePlaceMainReg.setLastLen(0); // 设置主视图上次长度为0，1
            disWavePlaceMainReg.setPlace(0); // 设置主视图位置为0，1
            disWavePlaceMainReg.setNumsPerLie1(1); // 设置主视图每列数据数量1为1，1
            disWavePlaceMainReg.setNumsPerLie2(0); // 设置主视图每列数据数量2为0，1
            disWavePlaceMainReg.setDaoShuLieNum(262144); // 设置主视图倒数列数为262144，1
            disWavePlaceMainReg.setNeedCouY(0); // 设置主视图不需要Y轴耦合标志为0，1
            disWavePlaceMainReg.setNumCouY(1); // 设置主视图Y轴耦合数量为1，1

            disStaticWave.setCouY(1); // 设置静态波形Y轴耦合为1，1
            disStaticWave.setNeedCY(0); // 设置静态波形不需要Y轴耦合标志为0，1
            disStaticWave.setLen(0); // 设置静态波形长度为0，1
            disStaticWave.setWavePlace(0); // 设置静态波形位置为0，1
        }  // 判断结束，1

        { // 代码块开始，主视图计算，1
            // 2.
            // 缩略视图取数计算-----------------------------------------------------------------------------------
            long datasPerLie; // 每列有多少组数据，1
            int cou = 1; // 数据的抽样比，1
            // 缩略视图
            // 计算每屏的数据量tm2:非zoom时主视图，或zoom时缩略视?
            // int tm1 = (int) (num1 * 100 + 0.1);

            long zunDepth1 = zunDepth / cou; // 计算zunDepth1为zunDepth除以cou，1

            //            if (zoom) {
            //                int num = 4;//4列的数据肯定为整?
            //                datasPerLie = (int) (zunDepth1 * num / cols1 + 0.1);
            //                if (datasPerLie == 0) datasPerLie = 1;//防护一?
            //                if(bPrintf){
            //                    Logger.i(TAG,"datasPerLie : " + datasPerLie);
            //                }
            //                FPGAReg_DISP_PLACE_SL dispWavePlaceSlReg = (FPGAReg_DISP_PLACE_SL)
            // getFPGAReg(FPGAReg.FPGA_DISP_PLACE_SL);
            //
            //                dispWavePlaceSlReg.setNumsPerLie(datasPerLie / 4);
            //                disWavePlaceMainReg.setNumsPerLie2((datasPerLie % 4) << 2);
            //                dispWavePlaceSlReg.setDaoShuLieNum((262144 * num) / datasPerLie);
            //                dispWavePlaceSlReg.setNeedCouY(cou > 1 ? 1 : 0);
            //                dispWavePlaceSlReg.setNumCouY(cou);
            //                sendCommand(dispWavePlaceSlReg);
            //            } else {
            //                disWavePlaceMainReg.setNumsPerLie2(0);
            //            }

            // 3.
            // 计算主视图抽样后每列的数据量-----------------------------------------------------------------------------------
            double cols; // 存储深度对应的总列?主视?，1
            if (zoom) // 放大模式，1
            {
                // 主视图，即放大视图
                // 计算每屏的数据量: 主视图
                //                tm1 = (int) (num2 * 100 + 0.1);
                //                int tm2;
                //                if (tm1 < 100) {//小于1?
                //                    tm2 = (int) zunDepth;
                //                } else {
                //                    tm2 = (int) (zunDepth * 100 / tm1);
                //                }
                cou = 1; // 设置抽样比为1，1
                // 抽样?.2M?.5M

                zunDepth1 = zunDepth / cou; // 重新计算zunDepth1为zunDepth除以cou，1

                cols = cols2; // 设置cols为cols2，1
            } else { // 非缩放模式，1
                cols = cols1; // 设置cols为cols1，1
            }  // 判断结束，1
            int num = 4; // 4?，1
            if (bPrintf) // 判断：如果启用调试日志，1
                Logger.i(
                        "zoom:"
                                + zoom
                                + " cols:"
                                + cols
                                + " num:"
                                + num
                                + " zunDepth1:"
                                + zunDepth1); // 输出调试信息，1
            // 抽样以及插值后，每列的数据组数(可能?n.25?n.5的小??4整数化。实际列数传递时
            // 反向处理可能不整?，需要进行余数传递，余数形式???
            datasPerLie = (int) (zunDepth1 * chaZhiNum[0] * num / cols + 0.1); // 计算每列数据量为zunDepth1乘以插值倍数乘以num除以cols加0.1后取整，1
            if (bPrintf) Logger.i("--datasPerLie:" + datasPerLie); // 输出每列数据量日志，1

            if (noDisp == 0) { // 判断：如果需要正常显示，1
                if (scopeFrozen.isSlowScale() && !isRun) { // 判断：如果是慢扫描模式且示波器停止，1
                    if (bPrintf) Logger.i("InSlowScale and Scope is Stop ===> "); // 输出慢扫描和停止状态日志，1
                    // datasPerLie时按照全屏波形计算的，对于滚屏非满屏时放大一些位置并不合适，
                    // 所以这里要重新修正计算，以保证参数numsPerLie1设置正确
                    int validLie = scopeFrozen.getVaildAddr(); // 获取冻结状态的有效地址，1
                    if (validLie > 0) { // 判断：如果有效地址大于0，1
                        int waveLen = validLie + 1; // 计算波形长度为validLie加1，1
                        int dataLenx = (int) (waveLen * zunDepth / x0); // 计算dataLenx为waveLen乘以zunDepth除以x0，1

                        if (bPrintf) // 判断：如果启用调试日志，1
                            Logger.i(
                                    "waveLen:"
                                            + waveLen
                                            + " dataLenX:"
                                            + dataLenx
                                            + " datasPerLie:"
                                            + datasPerLie
                                            + " num:"
                                            + num
                                            + " num1:"
                                            + num1); // 输出多个变量的调试日志，1
                        if (dataLenx < datasPerLie / num && num1 < 1) { // 判断：如果dataLenx小于datasPerLie除以num且num1小于1，1
                            int _datasPerLie = dataLenx; // 定义_datasPerLie为dataLenx，1
                            if (_datasPerLie != 0) datasPerLie = _datasPerLie; // 如果_datasPerLie不为0则更新datasPerLie，1
                            if (bPrintf) Logger.i("refresh datasPerLie:" + datasPerLie); // 输出刷新后的datasPerLie日志，1
                        }  // 判断结束，1
                    }  // 判断结束，1
                }  // 判断结束，1

                disWavePlaceMainReg.setNumsPerLie1((int)(datasPerLie / 4)); // 设置主视图每列数据数量1为datasPerLie除以4，1
                if (num1 > 2.0 / 14) // 判断：如果num1大于2.0/14，1
                    disWavePlaceMainReg.setNumsPerLie2((int)
                            (disWavePlaceMainReg.getNumsPerLie2() | datasPerLie % 4)); // 设置主视图每列数据数量2为当前值或上datasPerLie除以4的余数，1
                disWavePlaceMainReg.setDaoShuLieNum((int)((262144 * num) / datasPerLie)); // 设置主视图倒数列数为262144乘以num除以datasPerLie，1
            }  // 判断结束，1

            // 4. 主视图取数计?----------------------------------------------------------
            long t_sy = 0; // 定义时间余数变量t_sy，初始化为0，1
            // S64 t_sy=0;//计算取数位置对应时间精确到数据点后，还有余数，单?.1ps
            if (noDisp == 0) { // 判断：如果需要正常显示，1
                int place = 0; // 取数相对位置，1
                int start = 0; // 显示起始位置，1
                int end = x0 - 1; // 显示结束位置，1

                double tm3; // 数据总列?，1
                tm3 = num1 * scope.timeOneScreen_main() / 2; // 计算tm3为num1乘以主视图一屏时间除以2，1

                long halfDataTime = (long) (tm3 + 0.1); // 1半数据的时间，1
                if (bPrintf) { // 判断：如果启用调试日志，1
                    Logger.i("num1:" + num1 + " timeOneScreen_main:" + scope.timeOneScreen_main()); // 输出num1和主视图一屏时间日志，1
                    Logger.i("fullDataTime:" + tm3 + ",halfDataTime:" + halfDataTime); // 输出完整数据时间和半数据时间日志，1
                }  // 判断结束，1

                // 计算数据头距离缩略窗口中心的时间: tst
                long t_std =
                        HorizontalAxis.getInstance().getTimePosOfView(HorizontalAxis.WPI_STANDARD); // 获取标准视图的时间位置，1

                long tst; // 触发位置距离数据中心的时?，1
                if (isRun) tst = t_std; // 如果示波器运行，tst等于t_std，1
                else tst = scopeFrozen.getTimePosOfView(); // config.freezeWave.timePosOfView，1
                if (bPrintf) Logger.i("t_std:" + t_std + " tst:" + tst); // 输出t_std和tst的日志，1

                long t_main; // 主视图的触发位置，1
                long oneScreenTime; // 主视图一屏的时间:，1
                // oneScreenTime：主视图1屏对应的时间
                // t_main: 主视图触发位置距离屏幕中心的时间?
                if (zoom) { // 判断：如果是缩放模式，1
                    if (screenNum == 1) { // 判断：如果是单屏模式，1
                        t_main = t_std; // config.timePosOfView(0, Wave::WPI_STANDARD)，1
                        oneScreenTime = scope.timeOneScreen_main(); // 获取主视图一屏时间，1
                    } else { // 多屏模式，1
                        t_main =
                                HorizontalAxis.getInstance()
                                        .getTimePosOfView(HorizontalAxis.WPI_LARGE); // 获取大视图的时间位置，1
                        oneScreenTime = scope.timeOneScreen_zoom(); // 获取缩放视图一屏时间，1
                    }  // 判断结束，1
                } else { // 非缩放模式，1
                    t_main = t_std; // 设置t_main为t_std，1
                    oneScreenTime = scope.timeOneScreen_main(); // 获取主视图一屏时间，1
                }  // 判断结束，1
                if (bPrintf) Logger.i("t_main:" + t_main + " oneScreenTime:" + oneScreenTime); // 输出t_main和oneScreenTime的日志，1
                if (tst < -halfDataTime) { // 判断：如果tst小于负的半数据时间，1
                    // 触发位置在右，并且到达数据尾巴还超过，这时候单屏时，屏幕左边应该有空白显示?
                    // 即触发位置位于数据的最后一个；
                    // 主视图左右不对称
                    // 详细公式：t = halfDataTime*2+t+(config.timePosOfView(0, Wave::WPI_STANDARD)-t)
                    //        +(config.timePosOfView(0, Wave::WPI_LARGE)
                    //        -config.timePosOfView(0, Wave::WPI_STANDARD));
                    tst = halfDataTime * 2 + t_main; // 数据头距离屏幕中心的时间计算，1
                } else { // tst大于等于-halfDataTime的情况，1
                    // 详细公式：t = halfDataTime+(config.timePosOfView(0, Wave::WPI_STANDARD)-t)
                    //        +(config.timePosOfView(0, Wave::WPI_LARGE)
                    //        -config.timePosOfView(0, Wave::WPI_STANDARD));
                    // tst = halfDataTime - tst + t_main;//数据头距离屏幕中心的时间计算
                    // 还需要考虑动态时，触发不在采样点上；
                    tst =
                            halfDataTime
                                    - tst
                                    + t_main
                                    + (tst
                                            % (1000
                                                    * 1000
                                                    * 10
                                                    / scope.maxSampClk())); // 数据头距离屏幕中心的时间计算，1
                }  // 判断结束，1
                // 计算主窗口的取数位置
                long halfScreen = oneScreenTime / 2; // 半屏对应的时?，1
                if (bPrintf) Logger.i("new tst:" + tst + " halfScreen:" + halfScreen); // 输出新的tst和halfScreen日志，1

                if (halfScreen > tst) { // 判断：如果半屏时间大于tst，1

                    // 数据头在屏幕?

                    // 显示窗口的左边有"无波形区"
                    place = 0; // 设置取数位置为0，1
                    // 计算起始?
                    BigInteger t3 = BigInteger.valueOf(halfScreen - tst); // 创建BigInteger对象t3，值为halfScreen减tst，1
                    t3 = t3.multiply(BigInteger.valueOf(x0)); // t3乘以x0，1
                    t3 = t3.divide(BigInteger.valueOf(oneScreenTime)); // t3除以oneScreenTime，1

                    start = t3.intValue(); // 将t3转换为int赋值给start，1
                    if (start > x0 - 1) { // 判断：如果start大于x0-1，1
                        start = x0; // 设置start为x0，1
                    }  // 判断结束，1
                } else { // halfScreen小于等于tst的情况，1
                    start = 0; // 从第0列开始显?，1
                    // 计算取数位置
                    BigInteger t3 = BigInteger.valueOf(tst - halfScreen); // 创建BigInteger对象t3，值为tst减halfScreen，1
                    BigInteger temp = BigInteger.valueOf(tst - halfScreen); // 创建BigInteger对象temp，值为tst减halfScreen，1
                    t3 = t3.multiply(BigInteger.valueOf(zunDepth)); // t3乘以zunDepth，1
                    BigInteger th = BigInteger.valueOf(halfDataTime * 2); // 创建BigInteger对象th，值为halfDataTime的2倍，1
                    BigInteger tn = t3.divide(th); // 计算tn为t3除以th，1
                    BigInteger tm = t3.mod(th); // 计算tm为t3对th取模，1

                    if (tm.compareTo(th.divide(BigInteger.valueOf(2))) > 0) { // 判断：如果tm大于th除以2，1
                        t3 = tn.add(BigInteger.ONE); // t3等于tn加1，1
                    } else { // tm小于等于th除以2的情况，1
                        t3 = tn; // t3等于tn，1
                    }  // 判断结束，1

                    if (t3.compareTo(BigInteger.valueOf(zunDepth - 1)) > 0) { // 判断：如果t3大于zunDepth-1，1
                        t3 = BigInteger.valueOf(zunDepth - 1); // 设置t3为zunDepth-1，1
                    }  // 判断结束，1
                    place = t3.intValue(); // 将t3转换为int赋值给place，1
                    t3 = BigInteger.valueOf(place); // 重新创建t3为place，1
                    t3 =
                            t3.multiply(BigInteger.valueOf(halfDataTime * 2))
                                    .divide(BigInteger.valueOf(zunDepth)); // t3乘以halfDataTime的2倍除以zunDepth，1
                    t3 = temp.subtract(t3); // t3等于temp减去t3，1
                    t_sy = t3.longValue(); // 将t3转换为long赋值给t_sy，1

                    if (t_sy < 0) t_sy = 0; // 如果t_sy小于0则设置为0，1
                }  // 判断结束，1
                // 计算结束?
                { // 代码块开始，计算end位置，1
                    BigInteger t3 = BigInteger.valueOf(halfDataTime * 2); // 创建BigInteger对象t3，值为halfDataTime的2倍，1
                    t3 = t3.subtract(BigInteger.valueOf(tst)); // t3减去tst，1
                    t3 = t3.add(BigInteger.valueOf(halfScreen)); // t3加上halfScreen，1
                    if (t3.compareTo(BigInteger.ZERO) < 0) { // 判断：如果t3小于0，1
                        start = x0; // 设置start为x0，1
                        end = x0; // 设置end为x0，1
                    } else if (t3.compareTo(BigInteger.valueOf(oneScreenTime)) < 0) { // 判断：如果t3小于oneScreenTime，1

                        BigInteger t2 =
                                t3.multiply(BigInteger.valueOf(x0))
                                        .divide(BigInteger.valueOf(oneScreenTime)); // 计算t2为t3乘以x0除以oneScreenTime，1
                        end = t2.intValue() - 1; // 设置end为t2的int值减1，1
                        // 静态波形多一列可能多屏是错误。待验证
                        //if (isRun)
                        { // 代码块开始，1
                            long a = oneScreenTime / x0; // 计算a为oneScreenTime除以x0，1
                            t3 = BigInteger.valueOf(oneScreenTime).subtract(t3); // t3等于oneScreenTime减去t3，1
                            long b = t3.longValue(); // 将t3转换为long赋值给b，1
                            if (bPrintf) Logger.i("b:" + b + " a:" + a); // 输出b和a的日志，1
                            if (b > 0 && b < a) { // 判断：如果b大于0且小于a，1
                                end++; // end自增1，1
                            }  // 判断结束，1
                        }  // 代码块结束，1
                    }  // 判断结束，1
                }  // 代码块结束，1
                if (bPrintf) Logger.i("place:" + place + " start:" + start + " end:" + end); // 输出place、start和end的日志，1

                if (start == x0) end = x0; // 如果start等于x0则设置end为x0，1
                if (start > end) end = start; // 如果start大于end则设置end为start，1
                int lastStart = start; // 定义lastStart并赋值为start，1
                int lastEnd = end; // 定义lastEnd并赋值为end，1
                int lastLen = (int)dataLen; // 定义lastLen并赋值为dataLen，1
                if (!isRun) { // 判断：如果示波器停止，1
                    if (bPrintf) { // 判断：如果启用调试日志，1
                        Logger.i("Scope is stop===> validAdr:" + scopeFrozen.getVaildAddr()); // 输出示波器停止和有效地址日志，1
                        Logger.i(
                                "isRoll:"
                                        + scopeFrozen.isRool()
                                        + " isSlow:"
                                        + scopeFrozen.isSlowScale()); // 输出滚动和慢扫描状态日志，1
                    }  // 判断结束，1
                    if (scopeFrozen.isRool()) { // 判断：如果是滚动模式，1
                        // 静态滚屏时处理
                        long startD = scopeFrozen.getVaildAddr(); // 获取冻结状态的有效地址，1

                        // 计算无效数据?
                        startD = startD * zunDepth / x0; // 计算startD为startD乘以zunDepth除以x0，1
                        place -= startD; // place减去startD，1
                        if (bPrintf) Logger.i("is roll!! new place:" + place); // 输出滚动模式和新的place日志，1
                        if (place < 0) { // 判断：如果place小于0，1
                            // 修正start的?
                            if (start < x0) { // 判断：如果start小于x0，1
                                long tm1x = start; // 定义tm1x并赋值为start，1
                                tm1x -= (long) (place / lie - 0.1); // tm1x减去(place/lie-0.1)，1
                                if (tm1x > x0 - 1) { // 判断：如果tm1x大于x0-1，1
                                    start = x0; // 设置start为x0，1
                                    end = x0; // 设置end为x0，1
                                } else { // tm1x小于等于x0-1的情况，1
                                    start = (int) tm1x; // 设置start为tm1x的int值，1
                                }  // 判断结束，1
                                if (bPrintf) // 判断：如果启用调试日志，1
                                    Logger.i(
                                            "ROLL new start:"
                                                    + start
                                                    + " end:"
                                                    + end
                                                    + " place = 0"); // 输出滚动模式下的新start、end和place日志，1
                            }  // 判断结束，1
                            place = 0; // 设置place为0，1
                        }  // 判断结束，1
                    } else if (scopeFrozen.isSlowScale()) { // 判断：如果是慢扫描模式，1

                        long xn = scope.getSegmentFrameNums(); // 获取分段帧数，1
                        long xno = SegmentSample.getInstance().getFrameNo(); // 获取当前帧号，1
//                        Log.d(TAG,"xn:" + xn + ",xno:" + xno);

                        long endD = scopeFrozen.getVaildAddr(); // 获取冻结状态的有效地址，1

                        if(xn > 0 && xno >=0 ){ // 判断：如果xn大于0且xno大于等于0，1
                            if((xno + 1) != xn){ // 判断：如果xno+1不等于xn，1
                                endD = ScopeBase.getWidth() - 1; // 设置endD为屏幕宽度减1，1
                            }  // 判断结束，1
                        }  // 判断结束，1

                        endD = (long) ((endD + 1) * zunDepth / x0 - lie + 0.1); // 计算endD为(endD+1)乘以zunDepth除以x0减lie加0.1，1
                        long tm1x = (long) place; // 定义tm1x并赋值为place，1
                        tm1x += (long) ((end - start + 1) * lie + 0.1); // tm1x加上(end-start+1)乘以lie加0.1，1
                        tm1x = endD - tm1x; // tm1x等于endD减去tm1x，1
                        if (bPrintf) { // 判断：如果启用调试日志，1
                            Logger.i(
                                    "is slow!! zunDepth:"
                                            + zunDepth
                                            + " lie:"
                                            + lie
                                            + " endD:"
                                            + endD); // 输出慢扫描模式的调试信息，1
                            Logger.i(
                                    "place:" + place + " end:" + end + " start:" + start + " tm1x:"
                                            + tm1x); // 输出place、end、start和tm1x的日志，1
                        }  // 判断结束，1
                        if (tm1x < 0) { // 判断：如果tm1x小于0，1
                            // 修正end的?
                            if (end < x0) { // 判断：如果end小于x0，1
                                tm1x = end + (long) (tm1x / lie + 0.1); // tm1x等于end加上(tm1x/lie+0.1)，1
                                if (tm1x < 0) { // 判断：如果tm1x小于0，1
                                    start = x0; // 设置start为x0，1
                                    end = x0; // 设置end为x0，1
                                } else { // tm1x大于等于0的情况，1
                                    end = (int) tm1x; // 设置end为tm1x的int值，1
                                }  // 判断结束，1
                                if (bPrintf) // 判断：如果启用调试日志，1
                                    Logger.i(
                                            "SLOW new start:"
                                                    + start
                                                    + " end:"
                                                    + end
                                                    + " place = "
                                                    + place); // 输出慢扫描模式下的新start、end和place日志，1
                            }  // 判断结束，1
                        }  // 判断结束，1
                    }  // 判断结束，1
                }  // 判断结束，1
                if (start == x0) end = x0; // 如果start等于x0则设置end为x0，1
                if (start > end) end = start; // 如果start大于end则设置end为start，1
                // bug 0004915
                if (screenNum == 1) { // 判断：如果是单屏模式，1
                    if (!isRun) { // 判断：如果示波器停止，1
                        if (scopeFrozen.isSlowScale()) { // 判断：如果是慢扫描模式，1
                            end = end + start; // end等于end加start，1
                            start = 0; // 设置start为0，1
                            if (end >= x0) end = x0 - 1; // 如果end大于等于x0则设置end为x0-1，1
                            place = 0; // 设置place为0，1

                        } else if (scopeFrozen.isRool()) { // 判断：如果是滚动模式，1

                        } else { // 既不是慢扫描也不是滚动模式，1
                            start = 0; // 设置start为0，1
                            end = x0 - 1; // 设置end为x0-1，1
                            place = 0; // 设置place为0，1
                        }  // 判断结束，1
                        if (bPrintf) // 判断：如果启用调试日志，1
                            Logger.i(
                                    "screenNum=1 and ScopeIsStop ==> start:"
                                            + start
                                            + " end:"
                                            + end
                                            + " place:"
                                            + place); // 输出单屏模式且示波器停止时的start、end和place日志，1
                    }  // 判断结束，1
                }  // 判断结束，1
                dataLen = (int)(datasPerLie * (end + 1 - start) / num); // 计算数据长度为datasPerLie乘以(end+1-start)除以num，1
                lastLen = (int)(datasPerLie * (lastEnd + 1 - lastStart) / num); // 计算lastLen为datasPerLie乘以(lastEnd+1-lastStart)除以num，1
                if (bPrintf) Logger.i("wavelen:" + dataLen + ",datasPerLie:"
                        + datasPerLie + ",end:" + end + ",start:"
                        + start + ",num:" +num); // 输出波形长度、每列数据量、end、start和num的日志，1

                // 防止压缩时，误差导致数据变多
//                if (!isRun) {
//                    int validLine = x0;
//                    if (scopeFrozen.isRool()) {
//                        validLine = x0 - scopeFrozen.getVaildAddr();
//                    } else if (scopeFrozen.isSlowScale()) {
//                        validLine = scopeFrozen.getVaildAddr() + 1;
//                    }
//
////                    if(lie < 1) {
////                        temp /= lie;
////                    }
//                    if (bPrintf) Logger.i("validLine:" + validLine + ",lie:" + lie);
//                    int temp = (int) (zunDepth * validLine / x0);
//
//                    if (bPrintf) Logger.i( "dataLen:" + dataLen + ",temp:" + temp);
//                    if (dataLen > temp) {
//                        if (scopeFrozen.isRool() || scopeFrozen.isSlowScale()) {
//                            dataLen = temp;
//                            end -= 1;
//                            if (end < start)
//                                end = start;
//                        }
//                        if (bPrintf)
//                            Logger.i("start:" + start + " end:" + end + " wavelen:" + dataLen);
//                    }
//
//
//                }

                int len; // 定义长度变量len，1
                len = (int)dataLen; // 将dataLen转换为int赋值给len，1

                disWavePlaceMainReg.setPlace(place); // 设置主视图位置为place，1
                disWavePlaceMainReg.setXStart(start); // 设置主视图X轴起始位置为start，1
                disWavePlaceMainReg.setXEnd(end); // 设置主视图X轴结束位置为end，1
                disWavePlaceMainReg.setLen(len); // 设置主视图长度为len，1

                if (scopeFrozen.isSlowScale() && scopeFrozen.isSegmentEnable()) { // 判断：如果是慢扫描模式且分段功能启用，1
                    disWavePlaceMainReg.setLastXStart(lastStart); // 设置主视图上次X轴起始位置为lastStart，1
                    disWavePlaceMainReg.setLastXEnd(lastEnd); // 设置主视图上次X轴结束位置为lastEnd，1
                    disWavePlaceMainReg.setLastLen(lastLen); // 设置主视图上次长度为lastLen，1
                }  // 判断结束，1

                disWavePlaceMainReg.setNeedCouY(cou > 1 ? 1 : 0); // 如果cou大于1则设置需要Y轴耦合标志为1，否则为0，1
                disWavePlaceMainReg.setNumCouY(cou); // 设置主视图Y轴耦合数量为cou，1

                double lieT = (double) oneScreenTime / x0; // 计算lieT为oneScreenTime除以x0，1
                double sLast = (((zunDepth - place) / lie) * lieT / 4e4 + 13); // 计算sLast为((zunDepth-place)/lie)乘以lieT除以40000加13，1
                double sLen =  (lieT * (end - start + 1) / 4e4); // 计算sLen为lieT乘以(end-start+1)除以40000，1
                //Logger.d(TAG, "lieT:" + lieT + ",sLast:" + sLast + ",sLen:" + sLen + ",place:" + place + ",lie:" + place + ",zunDepth:" + zunDepth);

                FPGAReg_DISP_SERI dispSeriReg =
                        (FPGAReg_DISP_SERI) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DISP_SERI); // 获取串行显示寄存器对象，1
                dispSeriReg.setCodepos((int) sLast); // 设置编码位置为sLast的int值，1
                dispSeriReg.setCodelen((int) sLen); // 设置编码长度为sLen的int值，1
                dispSeriReg.setZipparam((int) (lieT / 4e4)); // 设置压缩参数为lieT除以40000的int值，1
                sendCommand(dispSeriReg); // 发送串行显示寄存器命令，1
            }  // 判断结束，1

            if (chaZhiNum[0] > 1 && noDisp == 0) { // 判断：如果插值倍数大于1且需要正常显示，1

                // fpga的最高采样频率为双通道1G,所以采样时间的精度只有1ns

                // AD采样精度?G，即1ns=10*1000(0.1ps)
                // AD采样精度?00M，即2ns=20*1000(0.1ps)
                // int step = 1000*1000*10/IModuleDevice::instance()->maxInputClk_AD();
                // 计算采样精度
                double fx = scope.getSampleRate(isRun); // 获取采样率，1

                long fx_dd = (long) (fx * (1000 * 10) + 0.1); // 采样频率单位换算?.1mHz，1

                long step =
                        (1000L * 1000 * 1000 * 1000 * 1000 * 10 * 10)
                                / fx_dd; // 周期，即两个采样的时间，单位0.1ps，1

                long t = t_sy * chaZhiNum[0] / step; // 计算t为t_sy乘以插值倍数除以step，1

                if (t != 0) { // 判断：如果t不等于0，1
                    if (t > chaZhiNum[0]) t = t - chaZhiNum[0]; // 如果t大于插值倍数则t减去插值倍数，1
                    if (t > chaZhiNum[0]) t = chaZhiNum[0]; // 如果t仍然大于插值倍数则设置t为插值倍数，1
                }  // 判断结束，1

                if (bPrintf) Logger.i("Chazhi setXiaoShu=" + t); // 输出插值小数设置日志，1
                disWavePlaceMainReg.setXiaoShu((int) t * 100 / chaZhiNum[0]); // 设置小数为t乘以100除以插值倍数的int值，1

            } else { // 插值倍数小于等于1或无显示的情况，1
                disWavePlaceMainReg.setXiaoShu(0); // 设置小数为0，1
            }  // 判断结束，1
        }  // 代码块结束，1
        sendCommand(disWavePlaceMainReg); // 发送主视图波形位置寄存器命令，1
        sendCommand(disChaZhiReg); // 发送插值寄存器命令，1

        // 数学波形
        if (noDisp == 0) { // 判断：如果需要正常显示，1
            int tm1; // 定义临时变量tm1，1
            int tm2; // 定义临时变量tm2，1
            // 数学波形取数计算:
            if (zoom) // 放大模式，1
            {
                tm1 = (int) (num2 * 100 + 0.1); // 计算tm1为num2乘以100加0.1后取整，1
            } else { // 非缩放模式，1
                tm1 = (int) (num1 * 100 + 0.1); // 计算tm1为num1乘以100加0.1后取整，1
            }  // 判断结束，1

            // 计算每屏的数据量: 主视?
            if (tm1 < 100) { // 判断：如果tm1小于100，1
                // 小于1屏时?屏处?
                tm2 = (int) zunDepth; // 设置tm2为zunDepth的int值，1
            } else { // tm1大于等于100的情况，1
                tm2 = (int) (zunDepth * 100 * chaZhiNum[0] / tm1); // 计算tm2为zunDepth乘以100乘以插值倍数除以tm1，1
            }  // 判断结束，1

            // 数学波形的抽样在点阵波形抽样的基础上进行再抽样
            tm2 /= disWavePlaceMainReg.getNumCouY(); // tm2除以主视图Y轴耦合数量，1

            int cou = mathCouYang(tm2); // 调用mathCouYang函数计算cou，1

            disStaticWave.setCouY(cou); // 设置静态波形Y轴耦合为cou，1
            disStaticWave.setNeedCY(cou > 1 ? 1 : 0); // 如果cou大于1则设置需要Y轴耦合标志为1，否则为0，1

            // 数学波形是从点阵数据中抽?
            disStaticWave.setLen((int)(dataLen / cou)); // 设置静态波形长度为dataLen除以cou，1
            //            disStaticWave.setWavePlace(disWavePlaceMainReg.getPlace());
            disStaticWave.setWavePlace((int)(
                    dataLen
                            / cou
                            / (disWavePlaceMainReg.getXEnd()
                                    - disWavePlaceMainReg.getXStart()
                                    + 1))); // 设置静态波形位置为dataLen除以cou再除以(end-start+1)，1

            double fs = scope.getSampleRate(isRun); // 获取采样率，1
            fs /= cou; // fs除以cou，1
            MathChannel mathChannel; // 定义数学通道变量，1
            int maxIdx = ChannelFactory.getMaxMathIdx(); // 获取最大数学通道索引，1
            for (int i = ChannelFactory.MATH1; i < maxIdx; i++) { // 遍历数学通道，1
                mathChannel = ChannelFactory.getMathChannel(i); // 获取第i个数学通道，1
                if (mathChannel != null) { // 判断：如果数学通道不为空，1
                    mathChannel.setSampleRate(fs, chaZhiNum[0]); // 设置数学通道的采样率为fs和插值倍数，1
                    mathChannel.setWaveLen((int)(dataLen / cou)); // 设置数学通道的波形长度为dataLen除以cou，1
                }  // 判断结束，1
            }  // 循环结束，1
        }  // 判断结束，1
        sendCommand(disStaticWave); // 发送静态波形寄存器命令，1
        HorizontalAxis.getInstance().setStartX(disWavePlaceMainReg.getXStart()); // 设置水平轴的起始X坐标，1
        HorizontalAxis.getInstance().setEndX(disWavePlaceMainReg.getXEnd()); // 设置水平轴的结束X坐标，1
    }


    /**
     * 计算数学波形抽样比
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据数据长度计算数学波形的抽样比</li>
     *   <li>确保抽样后的数据适合屏幕显示</li>
     *   <li>优化数学波形计算性能</li>
     * </ul>
     * 
     * @param len 数据长度
     * @return 抽样比
     */
    private int mathCouYang(int len) {  // 计算数学波形抽样比方法
        int cou = 1;  // 抽样比，初始为1
        int []cnDataCyMath = MemDepthFactory.getMemDepth().getMathCouArray();  // 获取数学波形抽样数组
        int k = cnDataCyMath[0];  // 获取基准数据量
        if(len > k){  // 判断：数据长度是否超过基准
            cou = len / k;  // 计算初始抽样比
            while ( k >= ScopeBase.getWidth()  // 循环：确保抽样后数据适合屏幕
                    && (len % k) != 0  // 判断：是否能整除
                    || (k % ScopeBase.getWidth() != 0)) {  // 判断：屏幕宽度是否能整除
                cou++;  // 增加抽样比
                k = len / cou;  // 重新计算基准数据量
            }  // 循环结束
        }  // 判断结束
        return cou;  // 返回抽样比
    }  // 方法结束

    /**
     * 电压A缓存
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录上次设置的电压A值</li>
     *   <li>用于检测电压变化</li>
     * </ul>
     */
    static int[] vol2A_last = {-1, -1};  // 电压A缓存数组，初始为-1
    
    /**
     * 电压B缓存
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录上次设置的电压B值</li>
     *   <li>用于检测电压变化</li>
     * </ul>
     */
    static int[] vol2B_last = {-1, -1};  // 电压B缓存数组，初始为-1

    /**
     * 设置外部触发电平
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置外部触发输入的电平阈值</li>
     *   <li>将电压值转换为FPGA寄存器值</li>
     *   <li>电平范围：-2.5V ~ +2.5V</li>
     * </ul>
     * 
     * <p><b>转换公式：</b>
     * <pre>
     * 寄存器值 = 触发电平(V) × 65536 / 5
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void ExtTriggerLevel(int fpgaIdx){  // 设置外部触发电平方法
        FPGAReg_ExtTriggerLevel level = (FPGAReg_ExtTriggerLevel)getFPGAReg(fpgaIdx,FPGAReg.FPGA_EXT_GRIGGER_LEVEL);  // 获取外部触发电平寄存器
        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();  // 获取触发通用设置
        int tempVal = (int) Math.round(triggerCommon.getExtTriggerLevel() * 65536 / 5);  // 计算寄存器值
        if (tempVal >= 65536) {  // 判断：是否超过最大值
            tempVal = 65535;  // 钳位到最大值
        }  // 判断结束
        level.setLevel(tempVal);  // 设置触发电平值
        sendCmd(level);  // 发送命令
    }  // 方法结束

    /**
     * 改变通道阻抗配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置通道输入阻抗（1MΩ或50Ω）</li>
     *   <li>设置外部触发输入阻抗</li>
     *   <li>不同阻抗影响带宽和测量精度</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void ChangeResistance(int fpgaIdx) {  // 改变通道阻抗配置方法
        FPGAReg_Resistance regResistance =  // 获取阻抗寄存器
                (FPGAReg_Resistance) getFPGAReg(fpgaIdx, FPGAReg.FPGA_CH_RESISTANCE);
        Channel channel;  // 通道对象
        int val;  // 阻抗值
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        int dang;  // 档位索引
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
            if (channel != null) {  // 判断：通道是否有效
                val = channel.getResistanceType();  // 获取阻抗类型
                regResistance.setResistance(i - beginIdx, val);  // 设置阻抗值
                regResistance.setResistanceSel(i - beginIdx, 0);  // 设置阻抗选择
            }  // 判断结束
        }  // 循环结束
        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();  // 获取触发通用设置
        regResistance.setExtTrigger(triggerCommon.getExtTriggerInputRes());  // 设置外部触发输入阻抗
        sendCmd(regResistance);  // 发送命令
    }  // 方法结束

    /**
     * 改变探头DA值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置探头DA输出值</li>
     *   <li>用于有源探头的偏置调整</li>
     *   <li>支持DA探头接口</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void ChangeProbeDa(int fpgaIdx) {  // 改变探头DA值方法
        FPGAReg_PROBE_DA12 regProbeDa12 =  // 获取探头DA寄存器
                (FPGAReg_PROBE_DA12) getFPGAReg(fpgaIdx, FPGAReg.FPGA_PROBE_DA12);
        Channel channel;  // 通道对象
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
            if (channel != null) {  // 判断：通道是否有效
                BaseProbe baseProbe = channel.getProbe();  // 获取探头对象
                if (baseProbe != null && baseProbe.isDa()) {  // 判断：探头是否支持DA
                    Log.d(TAG,"zhuzh" + ",ch:" + i  +",daVal:" +  baseProbe.getDaValue());  // 输出调试日志
                    regProbeDa12.setDaValue(i - beginIdx, baseProbe.getDaValue());  // 设置DA值
                } else {  // 否则：不支持DA
                    regProbeDa12.setDaValue(i - beginIdx, 0);  // 设置DA值为0
                }  // 判断结束
            }  // 判断结束
        }  // 循环结束

        sendCmd(regProbeDa12);  // 发送命令

    }  // 方法结束

    /**
     * 发送增益补偿系数给FPGA
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将增益补偿系数发送给FPGA</li>
     *   <li>用于校准不同档位的增益误差</li>
     *   <li>系数范围：4096（1.0）~8191（2.0）</li>
     * </ul>
     * 
     * <p><b>转换公式：</b>
     * <pre>
     * 寄存器值 = 增益系数 × 4096
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     * @param pgain_bc 增益补偿系数数组
     */
    public void sendFpga_gain_bc(int fpgaIdx, float[] pgain_bc) {  // 发送增益补偿系数方法
        // 发送增益补偿系数给fpga
        short[] bc = {0, 0, 0, 0};  // 补偿值数组，初始为0
        FPGAReg reg = new FPGAReg(FPGAReg.FPGA_GAIN_BC_AD, 2 * 4);  // 创建增益补偿寄存器
        reg.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        for (int i = 0; i < bc.length; i++) {  // 循环：遍历所有通道
            short ix = (short) (pgain_bc[i] * 4096 + 0.5);  // 计算补偿值
            if (ix < 4096) ix = 4096;  // 钳位最小值
            bc[i] = ix;  // 保存补偿值
        }  // 循环结束
        reg.setVal(bc);  // 设置寄存器值
        sendCommand(reg);  // 发送命令
    }  // 方法结束

    /**
     * 寄存器0x31上次值缓存
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于检测寄存器值变化</li>
     *   <li>避免重复发送相同配置</li>
     * </ul>
     */
    static int[] Val0x31_last = {-1, -1};  // 寄存器0x31缓存，初始为-1
    
    /**
     * 寄存器0x3A上次值缓存
     */
    static int[] Val0x3A_last = {-1, -1};  // 寄存器0x3A缓存，初始为-1
    
    /**
     * 寄存器0x3B上次值缓存
     */
    static int[] Val0x3B_last = {-1, -1};  // 寄存器0x3B缓存，初始为-1
    
    /**
     * 计数器上次值缓存
     */
    static int[] cnt_last = {-1, -1};  // 计数器缓存，初始为-1
    
    /**
     * 采样通道状态缓存
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录各通道的采样状态</li>
     *   <li>用于检测通道状态变化</li>
     * </ul>
     */
    static boolean[] sampleChState = {false,false,false,false,false,false,false,false};  // 采样通道状态数组，初始为false

    /**
     * 发送ADC通道变化配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检测通道开关状态变化</li>
     *   <li>重新配置ADC通道</li>
     *   <li>支持通道切换时的硬件复位</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return true表示通道状态发生变化，false表示无变化
     */
    public boolean SendAdc_ch_change(int fpgaIdx) {  // 发送ADC通道变化配置方法
        if(!scope.isRun()){  // 判断：是否运行中
            return false;  // 未运行时返回false
        }  // 判断结束

        boolean[] sle = new boolean[ChannelFactory.CH_CNT];  // 通道选择数组
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true), sle);  // 获取采样通道数

        boolean bChange = false;  // 变化标志，初始为false
        for (int i = 0; i < sle.length; i++) {  // 循环：检测通道状态变化
            if (sampleChState[i] != sle[i]) {  // 判断：通道状态是否变化
                bChange = true;  // 设置变化标志
                break;  // 跳出循环
            }  // 判断结束
        }  // 循环结束
        System.arraycopy(sle, 0, sampleChState, 0, sle.length);  // 复制通道状态
        if (bChange) {  // 判断：是否有变化
            ChannelHardw channelHardw = ChannelHardw.getInstance();  // 获取通道硬件实例
            if(adc.isChChangeReset()) {  // 判断：是否需要通道切换复位

                for (int i = 0; i < fpgaNums; i++) {  // 循环：遍历所有FPGA
                    cmdFpgaPause(i);  // 暂停FPGA
                    resetChOffset(i);  // 复位通道偏移
                }  // 循环结束
                channelHardw.ChPowerEnable(false);  // 关闭通道电源
                ms_sleep(10);  // 延时10ms
                adc.setChannel(cnt, sle);  // 设置ADC通道
                channelHardw.ChPowerEnable(true);  // 打开通道电源
                channelHardw.wrte_ad_gain(1);  // 写入AD增益（通道1）
                channelHardw.wrte_ad_gain(0);  // 写入AD增益（通道0）
                adc.setCalibrate();  // 设置校准
                channelHardw.changeChVolScale();  // 改变通道电压档位
                for (int i = 0; i < fpgaNums; i++) {  // 循环：遍历所有FPGA
                    gntR_ChOffsetDa(i);  // 生成通道偏移DA
                }  // 循环结束

                cmdDevice(100);  // 发送设备命令
                cmdFpgaResume(1);  // 恢复FPGA（索引1）
                cmdFpgaResume(0);  // 恢复FPGA（索引0）

            }else {  // 否则：不需要复位
                adc.setChannel(cnt, sle);  // 设置ADC通道
                channelHardw.wrte_ad_gain(fpgaIdx);  // 写入AD增益
                adc.setCalibrate();  // 设置校准
            }  // 判断结束
            cmdChNoise(fpgaIdx);  // 发送通道噪声配置
        }  // 判断结束
        return bChange;  // 返回变化标志
    }  // 方法结束

    /**
     * 写入AD增益
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置ADC的增益值</li>
     *   <li>支持不同ADC芯片的增益控制</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param adcIdx ADC索引
     * @param i0_q1 通道选择（0:I通道，1:Q通道）
     * @param vol 增益值
     */
    public void writeAD_gain(int fpgaIdx,int adcIdx,int i0_q1, int vol){  // 写入AD增益方法
        adc.setGain(fpgaIdx,adcIdx, i0_q1, vol);  // 设置ADC增益
    }  // 方法结束

    /**
     * 写入AD偏移
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置ADC的偏移值</li>
     *   <li>用于零点校准</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param adIdx ADC索引
     * @param i0_q1 通道选择（0:I通道，1:Q通道）
     * @param vol 偏移值
     */
    public void writeAD_offset(int fpgaIdx,int adIdx, int i0_q1, int vol){  // 写入AD偏移方法
        adc.setOffset(fpgaIdx,adIdx,i0_q1,vol);  // 设置ADC偏移
    }  // 方法结束

    /**
     * 设置ADC通道（4通道版本）
     * 
     * @param b1 通道1开关
     * @param b2 通道2开关
     * @param b3 通道3开关
     * @param b4 通道4开关
     */
    public void setADCChannel(boolean b1, boolean b2, boolean b3, boolean b4) {  // 设置ADC通道方法（4通道版本）
        adc.setChannel(new boolean[] {b1, b2, b3, b4});  // 设置ADC通道
    }  // 方法结束

    /**
     * 设置ADC通道（数组版本）
     * 
     * @param sel 通道选择数组
     */
    public void setADCChannel(boolean[] sel) {  // 设置ADC通道方法（数组版本）
//        cmdFpgaPause(1);
//        cmdFpgaPause(0);
//        ChannelHardw.getInstance().ChPowerEnable(false);
//        adc.setChannel(sel);
//        ChannelHardw.getInstance().changeChVolScale();
//        cmdFpgaResume(1);
//        cmdFpgaResume(0);
        adc.setChannel(sel);  // 设置ADC通道
    }  // 方法结束

    /**
     * 发送AD数据
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>通过SPI扩展接口发送ADC配置数据</li>
     *   <li>支持不同ADC芯片的寄存器配置</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param adIdx ADC索引
     * @param addr 寄存器地址
     * @param val 寄存器值
     */
    public void SendADData(int fpgaIdx, int adIdx, int addr, int val) {  // 发送AD数据方法
        Log.d("MHO38V1",  // 输出调试日志
                "SendADData() called with: fpgaIdx = ["
                        + fpgaIdx
                        + "], adIdx = ["
                        + adIdx
                        + "], addr = ["
                        + Integer.toHexString(addr)
                        + "], val = ["
                        + val
                        + "]");
        cmdDevice(0,100);  // 发送设备命令
        FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SPI_EXT);  // 获取SPI扩展寄存器
        reg.setType(FPGAReg_SPI_EXT.SPI_EXT_AD);  // 设置类型为ADC
        reg.setWriteAd(1 << adIdx);  // 设置写ADC标志
        reg.setAdData(addr, val);  // 设置ADC数据
        reg.setDataLength((HardwareProduct.isMHO68V2() && !mHw.isAdcB12DJ3200NBB()) ? 2 : 1);  // 设置数据长度
        reg.setByteValid(0);  // 设置字节有效标志
        sendCommand(reg);  // 发送命令
    }  // 方法结束

    /**
     * 设置ADC J模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置ADC的工作模式</li>
     *   <li>用于ADC特殊功能设置</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param val 模式值
     * @param v 配置值
     */
    public void AD_JMode(int fpgaIdx,int val,int v){  // 设置ADC J模式方法
        Log.d("MHO68V2", "AD_JMode() called with: fpgaIdx = [" + fpgaIdx + "], val = [" + val + "], v = [" +Integer.toHexString(v&0xFFFF) + "]");  // 输出调试日志
        cmdDevice(0,100);  // 发送设备命令
        FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SPI_EXT);  // 获取SPI扩展寄存器
        reg.setType(FPGAReg_SPI_EXT.SPI_EXT_AD);  // 设置类型为ADC
        reg.setADMode(val,v);  // 设置ADC模式
        reg.setDataLength(1);  // 设置数据长度为1
        reg.setByteValid(0);  // 设置字节有效标志
        sendCommand(reg);  // 发送命令
    }  // 方法结束

    /**
     * ADC初始化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>初始化ADC芯片</li>
     *   <li>配置ADC默认参数</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void AdInit(int fpgaIdx) {  // ADC初始化方法
//        if(!HardwareProduct.isMHO10008()) {
//            FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SPI_EXT);
//            reg.setType(FPGAReg_SPI_EXT.SPI_EXT_AD);
//            reg.setVal(12, 4, 0x0F);
//            reg.setDataLength(1);
//            reg.setByteValid(0);
//            reg.setVal(1, 1);
//            sendCommand(reg);
//            reg.setVal(12, 4, 0x00);
//            reg.setVal(16, 4, 0x0F);
//            reg.setVal(1, 0);
//            sendCommand(reg);
//        }
    }  // 方法结束

    /**
     * 调试PGA值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于调试PGA增益设置</li>
     *   <li>-1表示不使用调试值</li>
     * </ul>
     */
    int debugPGA = -1;  // 调试PGA值，初始为-1
    
    /**
     * 设置调试PGA值
     * 
     * @param val PGA调试值
     */
    public void setDebugPGA(int val){  // 设置调试PGA值方法
        debugPGA = val;  // 设置调试PGA值
    }  // 方法结束
    
    /**
     * 发送AD8370增益数据
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置AD8370可编程增益放大器</li>
     *   <li>AD8370是数字控制增益放大器</li>
     *   <li>增益范围：-11dB到+17dB</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param val 增益值数组
     */
    public void SendAD8370Data(int fpgaIdx, int[] val) {  // 发送AD8370增益数据方法
        if(BuildConfig.DEBUG) {  // 判断：是否为调试模式
            if (debugPGA > 0) {  // 判断：是否有调试PGA值
                Arrays.fill(val, debugPGA);  // 填充调试值
            }  // 判断结束
        }  // 判断结束
        StringBuilder str = new StringBuilder();  // 创建字符串构建器
        for (int j : val) {  // 循环：遍历增益值
            str.append(Integer.toHexString(j & 0xFFFF));  // 转换为十六进制字符串
            str.append(",");  // 添加分隔符
        }  // 循环结束
        Log.d("MHO38V1", "fpgaIdx:" + fpgaIdx + ",pga val:" + str);  // 输出调试日志

        FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SPI_EXT);  // 获取SPI扩展寄存器
        reg.setType(FPGAReg_SPI_EXT.SPI_EXT_CHGAIN);  // 设置类型为通道增益
        reg.setDataLength(4);  // 设置数据长度为4
        reg.setByteValid(0);  // 设置字节有效标志
        reg.setVGAIdx(0);  // 设置VGA索引
        reg.setCh(0xF);  // 设置通道标志
        for (int i = 0; i < val.length; i++) {  // 循环：设置增益值
            reg.setVal(1 + i, val[i]);  // 设置增益值
        }  // 循环结束
        sendCommand(reg);  // 发送命令
    }  // 方法结束
    
    /**
     * 发送通道PD配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置通道电源管理</li>
     *   <li>控制通道的电源开关状态</li>
     * </ul>
     * 
     * @param pdVal PD配置值
     */
    public void SendChPD(int pdVal){  // 发送通道PD配置方法
        //Log.d("MHO68V2", "SendChPD() called with: pdVal = [" + Integer.toHexString(pdVal) + "]");

        SendChPD(0,(pdVal & 0xF) | (pdVal>>12) & 0xF0);  // 发送到FPGA0
        SendChPD(1,((pdVal>>4) & 0xF) | (pdVal>>16) & 0xF0);  // 发送到FPGA1
    }  // 方法结束
    
    /**
     * 发送通道PD配置（V2版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>支持64位PD配置值</li>
     *   <li>用于更多通道的电源管理</li>
     * </ul>
     * 
     * @param pdVal 64位PD配置值
     */
    public void SendChPD_v2(long pdVal){  // 发送通道PD配置V2方法
        Log.d("MHO68V2", "SendChPD_v2() called with: pdVal = [" + Long.toHexString(pdVal) + "]");  // 输出调试日志
        SendChPD(0,(int)(pdVal & 0xFFFFFFFFL));  // 发送低32位到FPGA0
        SendChPD(1,(int)((pdVal >> 32) & 0xFFFFFFFFL));  // 发送高32位到FPGA1
    }  // 方法结束
    
    /**
     * 发送通道PD配置（带FPGA索引版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>向指定FPGA发送通道电源管理配置</li>
     *   <li>控制通道的电源开关状态</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param val PD配置值
     */
    public void SendChPD(int fpgaIdx,int val){  // 发送通道PD配置方法（带FPGA索引版本）
        Log.d(TAG, "SendChPD() called with: fpgaIdx = [" + fpgaIdx + "], val = [" + Integer.toHexString(val & 0xFF) + "]");  // 输出调试日志
        FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) getFPGAReg(fpgaIdx,FPGAReg.FPGA_SPI_EXT);  // 获取SPI扩展寄存器
        reg.setType(FPGAReg_SPI_EXT.SPI_EXT_CHPD);  // 设置类型为通道PD
        reg.setDataLength(1);  // 设置数据长度为1
        reg.setByteValid(0);  // 设置字节有效标志
        reg.setVal(1,val);  // 设置PD值
        sendCommand(reg);  // 发送命令
    }  // 方法结束
    
    /**
     * 发送时钟输入输出配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置时钟输入输出模式</li>
     *   <li>控制外部时钟和内部时钟切换</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param val 时钟配置值
     */
    public void SendClkInOut(int fpgaIdx,int val){  // 发送时钟输入输出配置方法

        Log.d(TAG, "SendClkInOut() called with: val = [" + Integer.toHexString(val&0xFFFFFFFF) + "]");  // 输出调试日志
        FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) getFPGAReg(fpgaIdx,FPGAReg.FPGA_SPI_EXT);  // 获取SPI扩展寄存器
        reg.setType(FPGAReg_SPI_EXT.SPI_EXT_CLKINOUT);  // 设置类型为时钟输入输出
        reg.setDataLength(1);  // 设置数据长度为1
        reg.setByteValid(0);  // 设置字节有效标志
        reg.setVal(1,val);  // 设置时钟配置值
        sendCommand(reg);  // 发送命令
    }  // 方法结束
    
    /**
     * 发送探头数据
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>通过SPI扩展接口发送探头数据</li>
     *   <li>用于智能探头通信</li>
     *   <li>数据格式：4字节对齐</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @param bytes 探头数据字节数组
     */
    public void SendProbe(int chIdx, byte[] bytes) {  // 发送探头数据方法
        int nums = bytes.length / 4;  // 计算数据个数（4字节对齐）
        int fpgaIdx = chIdxToFpgaIdx(chIdx);  // 获取FPGA索引  // 获取FPGA索引
        FPGAReg_SPI_EXT reg = new FPGAReg_SPI_EXT(bytes.length + 4);  // 创建SPI扩展寄存器
        reg.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        reg.setType(FPGAReg_SPI_EXT.SPI_EXT_UART);  // 设置类型为UART
        reg.setDataLength(nums);  // 设置数据长度
        reg.setByteValid(0);  // 设置字节有效标志
        reg.setVal(8, 4, 1 << chIdx);  // 设置通道标志
        for (int i = 0; i < nums; i++) {  // 循环：设置数据
            reg.setVal(  // 设置数据值
                    i + 1,  // 数据索引
                    (bytes[i * 4] & 0xFF) << 24  // 第1字节
                            | (bytes[i * 4 + 1] & 0xFF) << 16  // 第2字节
                            | (bytes[i * 4 + 2] & 0xFF) << 8  // 第3字节
                            | (bytes[i * 4 + 3] & 0xFF));  // 第4字节
        }  // 循环结束
        sendCommand(reg);  // 发送命令
    }  // 方法结束

    /**
     * 毫秒级延时
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>提供毫秒级延时功能</li>
     *   <li>用于硬件操作时的等待</li>
     * </ul>
     * 
     * @param ms 延时时间（毫秒）
     */
    private void ms_sleep(long ms) {  // 毫秒级延时方法
        try {  // 尝试延时
            Thread.sleep(ms);  // 线程休眠
        } catch (InterruptedException e) {  // 捕获中断异常
            e.printStackTrace();  // 打印异常堆栈
        }  // try-catch结束
    }  // 方法结束

    /**
     * 生成通道偏移DA配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算并设置各通道的零点偏移DA值</li>
     *   <li>用于通道零点校准</li>
     *   <li>DA值范围：0-65535（16位）</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_ChOffsetDa(int fpgaIdx) {  // 生成通道偏移DA配置方法
        Channel channel;  // 通道对象
        int[] chValue = {0, 0, 0, 0};  // 通道DA值数组，初始为0
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
            if (channel != null) {  // 判断：通道是否有效
                chValue[i - beginIdx] = getDA_Vol(channel);  // 计算通道DA值
            }  // 判断结束
        }  // 循环结束
        gntR_ChOffsetDa(fpgaIdx, chValue[0], chValue[1], chValue[2], chValue[3]);  // 发送DA配置
    }  // 方法结束

    /**
     * 复位通道偏移
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将通道偏移复位到零点校准值</li>
     *   <li>用于通道切换时的零点复位</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void resetChOffset(int fpgaIdx){  // 复位通道偏移方法
        Channel channel;  // 通道对象
        int[] chValue = {0, 0, 0, 0};  // 通道DA值数组，初始为0
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
            if (channel != null) {  // 判断：通道是否有效
                chValue[i - beginIdx] = (int)Math.round(getChannelZero(channel.getChId()));  // 获取零点校准值
            }  // 判断结束
        }  // 循环结束
        gntR_ChOffsetDa(fpgaIdx, chValue[0], chValue[1], chValue[2], chValue[3]);  // 发送DA配置
    }  // 方法结束



    /**
     * 生成通道偏移DA配置（指定值版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置各通道的零点偏移DA值</li>
     *   <li>DA值范围：0-65535（16位）</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param ch1 通道1 DA值
     * @param ch2 通道2 DA值
     * @param ch3 通道3 DA值
     * @param ch4 通道4 DA值
     */
    public void gntR_ChOffsetDa(int fpgaIdx, int ch1, int ch2, int ch3, int ch4) {  // 生成通道偏移DA配置方法（指定值版本）
        Log.d(  // 输出调试日志
                "MHO38V1",
                "gntR_ChOffsetDa() called with: fpgaIdx = ["
                        + fpgaIdx
                        + "], ch1 = ["
                        + ch1
                        + "], ch2 = ["
                        + ch2
                        + "], ch3 = ["
                        + ch3
                        + "], ch4 = ["
                        + ch4
                        + "]");
        FPGAReg_CH_OFFSET_DA12 reg =  // 获取通道偏移DA寄存器
                (FPGAReg_CH_OFFSET_DA12) getFPGAReg(fpgaIdx, FPGAReg.FPGA_CH_OFFSET_DA12);
        reg.setCh1(ch1);  // 设置通道1 DA值
        reg.setCh2(ch2);  // 设置通道2 DA值
        reg.setCh3(ch3);  // 设置通道3 DA值
        reg.setCh4(ch4);  // 设置通道4 DA值
        sendCommand(reg);  // 发送命令
    }  // 方法结束

    static final double x1_suolue = (double) ScopeBase.zoomYGrid_suolue() / ScopeBase.YGridWave();  // 缩略视图X方向缩放系数
    static final double x1_main = (double) ScopeBase.zoomYGrid_fangda() / ScopeBase.YGridWave();  // 放大视图X方向缩放系数
    static final double y1_suolue =  // 缩略视图Y方向偏移
            (double) (ScopeBase.YGridWave() - ScopeBase.zoomYGrid_suolue()) / 2;  // 计算偏移量
    static final double y1_main =  // 放大视图Y方向偏移
            (double) (ScopeBase.zoomYGrid_fangda() - ScopeBase.YGridWave()) / 2;  // 计算偏移量

    public void cmdFpgaDotMatrix(int chIdx) {  // 发送点阵显示配置方法
        int fpgaIdx = chIdxToFpgaIdx(chIdx);
        Scope scope = Scope.getInstance();
        if (!scope.isChannelInSample(chIdx, scope.isRun(true))) return;
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if (channel == null) return;
        double chPos = (double) channel.getPos();
        FPGAReg_DOT_MATRIX dotMatrixReg =
                (FPGAReg_DOT_MATRIX) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DOT_MATRIX);
        dotMatrixReg.reset();
        dotMatrixReg.matrixParam.bChange = true;
        dotMatrixReg.matrixParam.x1 = 1.0d; // 放大镜引起的衰减
        dotMatrixReg.matrixParam.y1 = 0; // 放大镜引起的零点偏移
        dotMatrixReg.matrixParam.y0 = chPos; // 现在通道位置
        dotMatrixReg.matrixParam.k =
                ADGear2PixBuf(chIdx, true) * getValueDifferentCoef(chIdx); // AD与屏幕对应关�?
        // dotMatrixReg.matrixParam.y1 = chPos - dotMatrixReg.matrixParam.k *
        // Math.floor(chPos/dotMatrixReg.matrixParam.k);
        boolean[] chEn = new boolean[ChannelFactory.CH_CNT];
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true), chEn);

        int beginIdx = beginChIdx(fpgaIdx);
        int endIdx = endChIdx(fpgaIdx);
        int idx = chIdx - beginIdx;
        switch (cnt) {
            case 2:
                for (int i = beginIdx; i < endIdx; i++) {
                    if (chEn[i]) {
                        if (i == chIdx) {
                            dotMatrixReg.setChannel(1);
                            break;
                        }
                    }
                }
                break;
            case 4: {
                int k = 0;
                for (int i = beginIdx; i < endIdx; i++) {
                    if (chEn[i]) {
                        if (i == chIdx) {
                            dotMatrixReg.setChannel(1 << k);
                            break;
                        }
                        k++;
                    }
                }
            }
                break;
            case 8:
            default:
                dotMatrixReg.setChannel(1 << idx);

                break;
        }

        if (scope.isRun(true)) {
            dotMatrixReg.matrixParam.x0 = 1.0d;
            dotMatrixReg.matrixParam.z = channel.getPosFix();
        } else {
            ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();
            dotMatrixReg.matrixParam.x0 = (double) scope.zoomYScale(chIdx) / channel.getYFactor();
            dotMatrixReg.matrixParam.z =
                    scopeFrozen.getChPosFix(chIdx) / scopeFrozen.getYFactor(chIdx)
                            - channel.getZPos();  // 计算Y1坐标（减去缩放位置）
        }  // 判断结束
        int len;  // 数据长度变量

        if (scope.isZoom()) {  // 判断：是否处于缩放模式

            dotMatrixReg.setUpDisSuolue(0);  // 设置显示缩放标志为0（主窗口）
            dotMatrixReg.matrixParam.y1 = y1_main;  // 设置主窗口Y1坐标
            dotMatrixReg.matrixParam.x1 = x1_main;  // 设置主窗口X1坐标
            if (dotMatrixReg.judgeDotMatrixChange(dotMatrixReg.matrixParam, 0, chIdx)) {  // 判断：点阵参数是否改变

                len =  // 计算点阵数据长度
                        dotMatrixReg.dotMatrixCal(  // 调用点阵计算方法
                                dotMatrixReg.matrixParam,  // 点阵参数
                                hwConfig.getAdMaxVal(),  // ADC最大值
                                0,  // 起始Y坐标
                                511 + ScopeBase.getHeight() / 2 - ScopeBase.zoomYGrid_suolue(),  // 结束Y坐标
                                0);  // X偏移
                dotMatrixReg.setCmdLength(len);  // 设置命令长度
                sendCommand(dotMatrixReg, len + FPGAReg.FPGA_REG_HEADER_LEN);  // 发送点阵命令
            }  // 判断结束

            dotMatrixReg.setUpDisSuolue(1);  // 设置显示缩放标志为1（缩放窗口）
            dotMatrixReg.matrixParam.y1 = y1_suolue;  // 设置缩放窗口Y1坐标
            dotMatrixReg.matrixParam.x1 = x1_suolue;  // 设置缩放窗口X1坐标

            if (dotMatrixReg.judgeDotMatrixChange(dotMatrixReg.matrixParam, 1, chIdx)) {  // 判断：点阵参数是否改变
                len =  // 计算点阵数据长度
                        dotMatrixReg.dotMatrixCal(  // 调用点阵计算方法
                                dotMatrixReg.matrixParam,  // 点阵参数
                                hwConfig.getAdMaxVal(),  // ADC最大值
                                0,  // 起始Y坐标
                                1023,  // 结束Y坐标
                                511 + ScopeBase.getHeight() / 2 - ScopeBase.zoomYGrid_suolue());  // X偏移
                dotMatrixReg.setCmdLength(len);  // 设置命令长度
                sendCommand(dotMatrixReg, len + FPGAReg.FPGA_REG_HEADER_LEN);  // 发送点阵命令
            }  // 判断结束
        } else {  // 否则：非缩放模式
            dotMatrixReg.setUpDisSuolue(0);  // 设置显示缩放标志为0
            if (dotMatrixReg.judgeDotMatrixChange(dotMatrixReg.matrixParam, 0, chIdx)) {  // 判断：点阵参数是否改变
                len =  // 计算点阵数据长度
                        dotMatrixReg.dotMatrixCal(  // 调用点阵计算方法
                                dotMatrixReg.matrixParam, hwConfig.getAdMaxVal(), 0, 1023, 0);  // 参数：点阵参数、ADC最大值、起始Y、结束Y、X偏移
                dotMatrixReg.setCmdLength(len);  // 设置命令长度
                sendCommand(dotMatrixReg, len + FPGAReg.FPGA_REG_HEADER_LEN);  // 发送点阵命令
            }  // 判断结束
        }  // 判断结束
    }  // 方法结束
    
    /**
     * 重置备份
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>重置所有通道的点阵变化标志</li>
     *   <li>用于强制刷新点阵显示</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void resetBak(int fpgaIdx){  // 重置备份方法
        FPGAReg_DOT_MATRIX dotMatrixReg =  // 获取点阵寄存器
                (FPGAReg_DOT_MATRIX) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DOT_MATRIX);
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        for(int i=beginIdx;i<endIdx;i++) {  // 循环：遍历所有通道
            dotMatrixReg.dotMatrixChange(i);  // 重置点阵变化标志
        }  // 循环结束
    }  // 方法结束

    /**
     * 更新垂直缩放
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算并更新垂直缩放参数</li>
     *   <li>处理运行和停止状态下的不同计算逻辑</li>
     *   <li>返回缩放状态</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return 缩放状态值
     */
    public int update_v_Zoom(int fpgaIdx) {  // 更新垂直缩放方法

        int n = hwConfig.getAdbits();  // 获取ADC位数
        int r, a, b, c, e = hwConfig.getAdMaxVal() + 1;  // 定义变量，e为ADC最大值加1
        double f = 1, f1, f2, bakPos;  // 定义缩放因子变量
        boolean bRun = scope.isRun(true);  // 获取运行状态
        Channel channel;  // 通道对象
        int[] vol = new int[8 * 3 + 1];  // 创建电压数组
        Arrays.fill(vol, 0);  // 初始化电压数组为0
        double vScaleVal;  // 电压档位值
        int ret = 0;  // 返回值，初始为0
        if (n < 12) return 0;  // 判断：ADC位数是否小于12，如果是则返回0
        double chPos;  // 通道位置
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        FPGAReg_DOT_MATRIX dotMatrixReg =  // 获取点阵寄存器
                (FPGAReg_DOT_MATRIX) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DOT_MATRIX);
        double digitV = 1;  // 数字电压值，初始为1
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
            if (channel == null) continue;  // 判断：通道是否有效，无效则跳过
            vScaleVal = channel.getVScaleVal() / channel.getProbeRate();  // 计算实际电压档位值
            f1 = channel.getYFactor();  // 获取Y轴缩放因子
            bakPos = channel.getZPos();  // 获取备份位置
            digitV = HwConfig.getInstance().getDigitDang(channel.getResistanceType());  // 获取数字档位值
            if (bRun) {  // 判断：是否处于运行状态
                f = digitV / vScaleVal;  // 计算缩放因子
                chPos = 0;  // 设置通道位置为0
                vScaleVal = 0;  // 设置电压档位为0
                f2 = 1;  // 设置缩放因子2为1
            } else {  // 否则：停止状态
                ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();  // 获取冻结状态实例
                f1 = scopeFrozen.getYFactor(i);  // 获取冻结状态的Y轴缩放因子
                VerticalAxis verticalAxis = scopeFrozen.getChVertical(i);  // 获取垂直轴对象
                double v = verticalAxis.getScaleVal() / verticalAxis.getProbeRate();  // 计算电压值
                v *= f1;  // 乘以Y轴缩放因子
                f = f2 = v / vScaleVal;  // 计算缩放因子

                vScaleVal = scopeFrozen.getChPosFix(i) / f1;  // 计算电压档位值
                chPos = channel.getPos();  // 获取通道位置
            }  // 判断结束

            if (f < 1) {  // 判断：缩放因子是否小于1
                f = 1;  // 限制最小值为1
            } else if (f > 4) {  // 判断：缩放因子是否大于4
                f = 4;  // 限制最大值为4
            }  // 判断结束
            if (!bRun) {  // 判断：是否处于停止状态

                if(f <= 1){  // 判断：缩放因子是否小于等于1
                    channel.setZPos(0);  // 设置缩放位置为0
                    chPos = 0;  // 设置通道位置为0
                }else {  // 否则：缩放因子大于1
                    r = e / 2;  // 计算范围值
                    r = (int) Math.round(r * f + r);  // 计算缩放后的范围值
                    f2 /= f;  // 更新缩放因子2
                    int z = (int) Math.round(chPos / f2 - vScaleVal * f);  // 计算缩放位置
                    if (z > r) {  // 判断：是否超过最大值
                        z = r;  // 限制为最大值
                    } else if (z < -r) {  // 判断：是否小于最小值
                        z = -r;  // 限制为最小值
                    }  // 判断结束
                    channel.setZPos(vScaleVal - chPos / f2);  // 设置通道缩放位置
                    chPos = z;  // 更新通道位置
                }  // 判断结束
            }  // 判断结束

            a = (int) (Math.pow(2, n - 1) + 0.1);  // 计算参数a（2的n-1次方）

            b = (int) (e * f * Math.pow(2, 16 - n) + 0.1);  // 计算参数b
            c = (int) Math.round((double) e / 2 + chPos);  // 计算参数c
            vol[i - beginIdx + 1] = vol[i - beginIdx + 5] = a;  // 设置电压数组值
            vol[i - beginIdx + 9] = vol[i - beginIdx + 13] = b;  // 设置电压数组值
            vol[i - beginIdx + 17] = vol[i - beginIdx + 21] = c;  // 设置电压数组值
//            Log.d("1234","ch:" + channel.getChId() + ",f:" + f + ",a:" + a + ",b:" + b + ",c:" + c);  // 已注释：调试日志
            channel.setYFactor(f);  // 设置通道Y轴缩放因子
            channel.setABC(a,b,c);  // 设置通道ABC参数

            if (!DoubleUtil.FuzzyCompare(channel.getYFactor(), f1)  // 判断：Y轴缩放因子是否改变
                    || !DoubleUtil.FuzzyCompare(channel.getZPos(), bakPos)) {  // 判断：缩放位置是否改变
                dotMatrixReg.dotMatrixChange(i);  // 标记点阵变化
                ret |= 1 << i;  // 设置返回值标志位
            }  // 判断结束
        }  // 循环结束
        FPGAReg reg = new FPGAReg(FPGAReg.FPGA_V_ZOOM, vol.length * 4);  // 创建垂直缩放寄存器
        reg.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        reg.setVal(vol);  // 设置寄存器值
        sendCommand(reg);  // 发送命令
        return ret;  // 返回缩放状态值
    }  // 方法结束

    static int[][] coef_fpga_mho_bak = {null, null};  // 静态变量：FPGA插值系数备份
    static int[][] coef_seach_mho_bak = {null, null};  // 静态变量：搜索插值系数备份

    /**
     * 重新加载MHO插值系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据插值类型加载对应的插值系数</li>
     *   <li>支持线性插值和升采样正弦滤波</li>
     *   <li>根据通道数选择单点或双点插值</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdFpgaReloadChazhiCoef_mho(int fpgaIdx) {  // 重新加载MHO插值系数方法
        Scope scope = Scope.getInstance();  // 获取示波器实例
        int[] coef_fpga;  // FPGA插值系数数组
        int[] coef_seach;  // 搜索插值系数数组
        FPGAReg_DISP_CHA dispChaReg =  // 获取显示通道寄存器
                (FPGAReg_DISP_CHA) getFPGAReg(fpgaIdx, FPGAReg.FPGA_DISP_CHA1);
        if (!dispChaReg.isNeedChaZhi()) {  // 判断：是否需要插值
            return;  // 不需要则返回
        }  // 判断结束

        FPGAReg_CY_BUCONG cyBucongReg =  // 获取抽样补偿寄存器
                (FPGAReg_CY_BUCONG) getFPGAReg(fpgaIdx, FPGAReg.FPGA_CY_BUCONG);
        if (cyBucongReg.getCyType12() > 0) {  // 判断：是否为线性插值
            coef_fpga = CHAZHI_COEF.chazhiCoef_Line;  // 使用线性插值系数
            coef_seach = coef_fpga;  // 搜索系数与FPGA系数相同
        } else {  // 否则：升采样正弦滤波
            boolean[] sle = new boolean[ChannelFactory.CH_CNT];  // 创建通道选择数组
            int cnt = scope.getChannelSampOnCnt(scope.isRun(true), sle);  // 获取采样通道数

            cnt /= adcNums;  // 计算每个ADC的通道数

            if(cnt > 1){  // 判断：通道数是否大于1
                coef_seach = CHAZHI_COEF.chazhiCoef_dub;  // 使用双点插值系数
            }else{
                coef_seach = CHAZHI_COEF.chazhiCoef_sgl;
            }
            if (Display.getInstance().getDrawType() == Display.DRAWTYPE_DOTS)
                coef_fpga = CHAZHI_COEF.chazhiCoef_Line;
            else {
                coef_fpga = coef_seach;
            }
        }
        //
        if (coef_fpga != coef_fpga_mho_bak[fpgaIdx]) {
            coef_fpga_mho_bak[fpgaIdx] = coef_fpga;

            FPGAReg reg = new FPGAReg(FPGAReg.FPGA_CHAZHI_COEF, coef_fpga.length * 4);
            reg.setFpgaIdx(fpgaIdx);
            reg.setVal(coef_fpga);
            Log.d(TAG, "coef_fpga len:" + reg.getCommandLength());
            sendCommand(reg);
        }

        if (coef_seach != coef_seach_mho_bak[fpgaIdx]) {
            coef_seach_mho_bak[fpgaIdx] = coef_seach;

            FPGAReg reg = new FPGAReg(FPGAReg.FPGA_SEACH_COEF, coef_seach.length * 4);
            reg.setFpgaIdx(fpgaIdx);
            reg.setVal(coef_seach);
            Log.d(TAG, "coef_seach len:" + reg.getCommandLength());
            sendCommand(reg);
        }
    }

    public void cmdFpgaReloadChazhiCoef(int fpgaIdx) {
        cmdFpgaReloadChazhiCoef_mho(fpgaIdx);
    }

    /**
     * 发送FPGA命令（无返回值版本）
     * 
     * @param fpgaIdx FPGA索引
     * @param param 参数值
     * @param cmd 命令值
     * @return 命令执行结果
     */
    private int[] FPGA_Command(int fpgaIdx, int param, int cmd) {  // 发送FPGA命令方法（无返回值版本）
        return FPGA_Command(fpgaIdx, param, cmd, false);  // 调用完整版本
    }  // 方法结束

    /**
     * 发送FPGA命令（完整版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送FPGA命令并可选接收返回值</li>
     *   <li>支持同步机制避免命令冲突</li>
     *   <li>支持AD校准延时等待</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param param 参数值
     * @param cmd 命令值
     * @param bRecv 是否接收返回值
     * @return 命令执行结果数组[同步头, 列地址, 帧数]
     */
    private int[] FPGA_Command(int fpgaIdx, int param, int cmd, boolean bRecv) {  // 发送FPGA命令方法（完整版本）
        // 因为在校准线程中直接调用了FPGA_Command 可能会与 命令线程中的 FPGA_Command 不同步
        int[] res = {-1, -1, -1};  // 结果数组，初始为-1
        synchronized (this) {  // 同步块，避免命令冲突
            if (bAdCablition) {  // 判断：是否需要AD校准延时
                long t = SystemClock.elapsedRealtime() - adCablitionTs;  // 计算已过时间
                t =  50 - t;  // 计算剩余等待时间
                if (t > 0) {  // 判断：是否需要等待
                    ms_sleep(t);  // 等待剩余时间
                }  // 判断结束
                bAdCablition = false;  // 清除AD校准标志
                adCablitionTs = 0;  // 清除时间戳
            }  // 判断结束
            FPGAReg_COMMAND reg = (FPGAReg_COMMAND) getFPGAReg(fpgaIdx, FPGAReg.FPGA_COMMAND);  // 获取命令寄存器
            reg.setParam(param);  // 设置参数
            reg.setCmd(cmd);  // 设置命令
            reg.setCmdCntValid(1);  // 设置命令计数有效标志
            reg.setCmdCnt(SyncHeader.getSyncHeader());  // 设置同步头
            if (bRecv) {  // 判断：是否需要接收返回值
                recvCommand(reg, reg.getRecvReg());  // 接收命令返回值
                res[0] = reg.getSyncHeader();  // 获取同步头
                res[1] = reg.getColAddr();  // 获取列地址
                res[2] = reg.getFrameNums();  // 获取帧数
                return res;  // 返回结果
            } else {  // 否则：不需要接收返回值
                sendCommand(reg);  // 发送命令
            }  // 判断结束
            return res;  // 返回结果
        }  // 同步块结束
    }  // 方法结束

    /**
     * 分段存储时间戳
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>在分段存储模式下记录时间戳</li>
     *   <li>用于分段波形的时序标记</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void segmentTimestamp(int fpgaIdx) {  // 分段存储时间戳方法
        if (scope.isSegmentEnable(scope.isRun(true))) {  // 判断：是否启用分段存储
            FPGAReg reg = new FPGAReg(FPGAReg.FPGA_SEGMENT_TS, 4);  // 创建分段时间戳寄存器
            reg.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
            reg.setVal(0, 0x01);  // 设置时间戳标志
            sendCommand(reg);  // 发送命令
        }  // 判断结束
    }  // 方法结束

    /**
     * 强制触发
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>手动触发一次采集</li>
     *   <li>用于单次触发模式或调试</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void forceTrigger(int fpgaIdx) {  // 强制触发方法

        FPGAReg reg = new FPGAReg(FPGAReg.FPGA_FORCE_TRIGGER,4);  // 创建强制触发寄存器
        reg.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        reg.setVal(0,0x01);  // 设置触发标志
        sendCommand(reg);  // 发送命令
    }  // 方法结束

    /**
     * AD校准标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>true表示需要AD校准延时</li>
     *   <li>用于避免AD校准与命令冲突</li>
     * </ul>
     */
    private boolean bAdCablition = false;  // AD校准标志，初始为false
    
    /**
     * AD校准时间戳
     */
    private long adCablitionTs = 0;  // AD校准时间戳，初始为0

    /**
     * 强制AD校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>触发ADC内部校准</li>
     *   <li>仅支持MHO38V1和MHO28V1型号</li>
     *   <li>校准后需要等待50ms</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void forceAdCablition(int fpgaIdx) {  // 强制AD校准方法
        Log.d(TAG, "forceAdCablition() called with: fpgaIdx = [" + fpgaIdx + "]");  // 输出调试日志
        if(HardwareProduct.isMHO38V1()  // 判断：是否为MHO38V1型号
                || HardwareProduct.isMHO28V1()){  // 判断：是否为MHO28V1型号

            FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SPI_EXT);  // 获取SPI扩展寄存器
            reg.setType(FPGAReg_SPI_EXT.SPI_EXT_AD);  // 设置类型为ADC
            reg.setCalibrationAd(0xF);  // 设置校准ADC标志
            reg.setDataLength(1);  // 设置数据长度为1
            reg.setVal(1, 1);  // 设置校准值
            synchronized (this) {  // 同步块
                sendCommand(reg);  // 发送命令
                bAdCablition = true;  // 设置AD校准标志
                adCablitionTs = SystemClock.elapsedRealtime();  // 记录时间戳
            }  // 同步块结束
        }  // 判断结束
    }  // 方法结束

    /**
     * ADC同步
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>同步多个ADC芯片的采样</li>
     *   <li>用于多通道同步采样</li>
     * </ul>
     */
    public void adSync(){  // ADC同步方法
        Log.d(TAG, "adSync() called");  // 输出调试日志
        FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) getFPGAReg(0, FPGAReg.FPGA_SPI_EXT);  // 获取SPI扩展寄存器
        reg.setType(FPGAReg_SPI_EXT.SPI_EXT_AD);  // 设置类型为ADC
        reg.adSync();  // 设置ADC同步
        reg.setDataLength(1);  // 设置数据长度为1
        reg.setVal(1, 1);  // 设置同步值
        sendCommand(reg);  // 发送命令
    }  // 方法结束

    /**
     * 更新显示寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>通知FPGA更新显示参数</li>
     *   <li>用于显示配置更新后的刷新</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void updataRegDis(int fpgaIdx) {  // 更新显示寄存器方法
        Log.d(TAG, "updataRegDis() called with: fpgaIdx = [" + fpgaIdx + "]");  // 输出调试日志
        FPGA_Command(fpgaIdx, 0x00, 0x33);  // 发送更新显示命令
    }  // 方法结束

    /**
     * 更新寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>仅在运行状态下更新寄存器</li>
     *   <li>用于运行时的参数更新</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void updataReg(int fpgaIdx) {  // 更新寄存器方法
        if (scope.isRun(true)) {  // 判断：是否运行中
            updataRegEx(fpgaIdx);  // 调用扩展更新方法
        }  // 判断结束
    }  // 方法结束

    /**
     * 更新寄存器（扩展版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>强制更新寄存器</li>
     *   <li>不考虑运行状态</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void updataRegEx(int fpgaIdx) {  // 更新寄存器扩展方法
        FPGA_Command(fpgaIdx, 0x00, 0x55);  // 发送更新命令
    }  // 方法结束

    /**
     * 发送设备命令
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送设备控制命令</li>
     *   <li>支持延时等待</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param time_ms 延时时间（毫秒）
     */
    public void cmdDevice(int fpgaIdx, int time_ms) {  // 发送设备命令方法
        if (!scope.isRun(true)) time_ms = 0;  // 判断：停止状态下不需要延时
        FPGA_Command(fpgaIdx, time_ms / 5, 0x22);
    }

    public void cmdDevice(int time_ms) {
        cmdDevice(1, time_ms);
        cmdDevice(0,time_ms);
    }



    public void cmdFpgaRun(int fpgaIdx) {
        chPowerEnable();
        FPGA_Command(fpgaIdx, 0x01, 0x11);
    }

    public int[] cmdFpgaStop(int fpgaIdx) {
        int[] val = FPGA_Command(fpgaIdx, 0x00, 0x11, true);
        Logger.d("cmdFpgaStop:" + val[0] + "," + val[1]);
        return val;
    }

    public void cmdFpgaSingle(int fpgaIdx) {
        FPGA_Command(fpgaIdx, 0x02, 0x11);
    }

    /**
     * 暂停FPGA
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>暂停FPGA的数据采集</li>
     *   <li>返回当前帧信息</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return 帧信息数组[同步头, 列地址, 帧数]
     */
    public int[] cmdFpgaPause(int fpgaIdx) {  // 暂停FPGA方法
        return FPGA_Command(fpgaIdx, 0x04, 0x11, true);  // 发送暂停命令并接收返回值
    }  // 方法结束

    /**
     * 恢复FPGA
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>恢复FPGA的数据采集</li>
     *   <li>用于暂停后的恢复</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdFpgaResume(int fpgaIdx) {  // 恢复FPGA方法
        FPGA_Command(fpgaIdx, 0x05, 0x11, false);  // 发送恢复命令
    }  // 方法结束

    /**
     * 同步FPGA数据
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>同步FPGA的数据输出</li>
     *   <li>用于多FPGA同步</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdFpgaSyncData(int fpgaIdx) {  // 同步FPGA数据方法
        FPGA_Command(fpgaIdx, 0x01, 0x66, false);  // 发送同步命令
    }  // 方法结束

    /**
     * 清除FPGA数据
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>清除FPGA的存储数据</li>
     *   <li>用于重新采集前的清空</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdFpgaClear(int fpgaIdx) {  // 清除FPGA数据方法
        FPGA_Command(fpgaIdx, 0x00, 0x66, false);  // 发送清除命令
    }  // 方法结束

    /**
     * 设置FPGA高刷新模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置FPGA的高刷新率模式</li>
     *   <li>用于提高波形显示刷新率</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdFpgaHighRefresh(int fpgaIdx) {  // 设置FPGA高刷新模式方法
        int val = 1;  // 刷新模式值，初始为1
        FPGA_Command(fpgaIdx, val, 0x77);  // 发送高刷新命令
    }  // 方法结束

    /**
     * 初始化命令
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>初始化所有FPGA</li>
     *   <li>设置高刷新模式</li>
     *   <li>配置探头DA</li>
     *   <li>更新寄存器</li>
     * </ul>
     */
    public void cmdInit() {  // 初始化命令方法
        for (int i = 0; i < fpgaNums; i++) {  // 循环：遍历所有FPGA
            cmdFpgaHighRefresh(i);  // 设置高刷新模式
            ChangeProbeDa(i);  // 配置探头DA
            updataReg(i);  // 更新寄存器
        }  // 循环结束
        ms_sleep(1);  // 延时1ms
        mHw.probeIo();  // 配置探头IO
    }  // 方法结束

    /**
     * ADC校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>执行ADC校准</li>
     *   <li>用于ADC增益和偏移校准</li>
     * </ul>
     */
    public void ADCalibrate() {  // ADC校准方法
        adc.setCalibrate();  // 设置ADC校准
    }  // 方法结束

    // --------------------------------------------------------------------------------------------------------------------------
    // AD前置滤波
    // 1阶IIR滤波系数计算
    // Fc为-3dB频点；Fs：采样频率（单位均为Hz）
    // type: =0，低通；=1，高通；
    // out：滤波结果，长度4个数据，顺序为：b1、b2、a1、a2；
    /**
     * 圆周率常量
     */
    static final double PI = 3.14159265358979323846264338328;  // 圆周率

    /**
     * IIR滤波器系数计算
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算1阶IIR滤波器的系数</li>
     *   <li>支持低通和高通滤波器</li>
     *   <li>使用双线性变换法</li>
     * </ul>
     * 
     * <p><b>滤波器类型：</b>
     * <pre>
     * type=0：低通滤波器
     * type=1：高通滤波器
     * </pre>
     * 
     * @param Fc 截止频率（Hz）
     * @param Fs 采样频率（Hz）
     * @param type 滤波器类型（0:低通，1:高通）
     * @param out 输出系数数组[b1, b2, a1, a2]
     * @return true表示计算成功，false表示参数错误
     */
    private boolean iir_coef_cal(double Fc, double Fs, int type, double[] out) {  // IIR滤波器系数计算方法
        double[] a = {0, 0, 0};  // 分母系数数组
        double[] b = {0, 0, 0};  // 分子系数数组
        double p;  // 中间变量

        if (Fc <= 1e-6 || Fc >= (Fs / 2 - 1e-6)) return false;  // 判断：截止频率是否有效
        // 频率校准
        Fc = Fs * Math.tan(PI * Fc / Fs) / PI;  // 预畸变校正
        if (type == 0) {  // 判断：是否为低通滤波器
            // 低通
            p = 0.31755496498600060962 * Fs / Fc;  // 计算极点
            b[0] = 1 / (1 + p);  // 计算分子系数b0
            b[1] = b[0];  // 计算分子系数b1
        } else if (type == 1) {  // 判断：是否为高通滤波器
            // 高通
            p = 0.31906660205046560863 * Fs / Fc;  // 计算极点
            b[0] = 1 / (1 + 1 / p);  // 计算分子系数b0
            b[1] = -b[0];  // 计算分子系数b1
        } else return false;  // 否则：类型错误

        a[0] = 1;  // 分母系数a0
        a[1] = (1 - p) / (1 + p);  // 分母系数a1
        out[0] = b[0];  // 输出b1
        out[1] = b[1];  // 输出b2
        out[2] = a[0];  // 输出a1
        out[3] = a[1];  // 输出a2
        return true;  // 返回成功
    }  // 方法结束

    // IIR系数归一化
    // in：a、b系数输入，顺序为：b1、b2、a1、a2；
    // out：归一化系数输出，长度8个数据，顺序为p1~p8；
    // bit：归一化后的系数位数，=0，自动归一化，其他值，固定归一化值
    /**
     * IIR系数归一化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将IIR滤波器系数归一化为整数</li>
     *   <li>用于FPGA实现</li>
     *   <li>支持自动和固定归一化</li>
     * </ul>
     * 
     * @param in 输入系数数组[b1, b2, a1, a2]
     * @param out 输出归一化系数数组[p1~p8]
     * @param idx 输出数组起始索引
     * @param bit 归一化位数（0:自动，其他:固定）
     */
    private void iir_coef_normalize(double[] in, int[] out, int idx, int bit) {  // IIR系数归一化方法

        double[] p = new double[16];  // 中间变量数组
        Arrays.fill(p, 0);  // 初始化为0
        double b = in[0];  // 获取分子系数b
        double a = in[3];  // 获取分母系数a

        p[0] = b;  // 计算p1
        p[1] = -a * b;  // 计算p2
        p[2] = a * a * b;  // 计算p3
        p[3] = -a * a * a * b;  // 计算p4

        p[4] = -a * (1L << bit);  // 计算p5
        p[5] = Math.pow(a, 4) * (1L << bit);  // 计算p6
        p[6] = Math.pow(a, 8) * (1L << bit);  // 计算p7
        p[7] = Math.pow(a, 16) * (1L << bit);  // 计算p8
        double v = 0;  // 最大值变量
        double m = Math.abs(p[0]);  // 初始最大值
        for (int i = 1; i < 4; i++) {  // 循环：查找最大值
            v = Math.abs(p[i]);  // 计算绝对值
            if (m < v) {  // 判断：是否更大
                m = v;  // 更新最大值
            }  // 判断结束
        }  // 循环结束

        int j = 0;  // 移位计数器
        while (m * (1L << j) < 0xFFFF) {  // 循环：计算归一化移位量
            j++;  // 移位计数器加1
            if (j > 62) {  // 判断：是否超过最大移位量
                break;  // 跳出循环
            }  // 判断结束
        }  // 循环结束
        j--;  // 移位量减1

        long n = 0;  // 中间变量
        for (int i = 0; i < 4; i++) {  // 循环：输出归一化系数
            out[idx + i] = (int) Math.round(p[i] * (1L << j));  // 输出归一化后的系数
            n = Math.round(p[4 + i]);  // 计算长整型系数
            out[idx + 4 + i * 2] = (int) (n & 0x3FFFF);  // 输出低18位
            out[idx + 5 + i * 2] = (int) ((n >>> 18) & 0x3FFFF);  // 输出高18位
        }  // 循环结束
        out[idx + 12] = j;  // 输出移位量
    }  // 方法结束

    /**
     * IIR系数归一化（8阶版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将8阶IIR滤波器系数归一化为整数</li>
     *   <li>用于FPGA实现</li>
     *   <li>支持自动和固定归一化</li>
     * </ul>
     * 
     * @param in 输入系数数组[b1, b2, a1, a2]
     * @param out 输出归一化系数数组[p1~p18]
     * @param idx 输出数组起始索引
     * @param bit 归一化位数（0:自动，其他:固定）
     */
    private void iir_coef_normalize_8(double[] in, int[] out, int idx, int bit) {  // IIR系数归一化方法（8阶版本）

        double[] p = new double[16];  // 中间变量数组
        Arrays.fill(p, 0);  // 初始化为0
        double b = in[0];  // 获取分子系数b
        double a = in[3];  // 获取分母系数a
        for(int i=0;i<8;i++){  // 循环：计算8阶系数
            p[i] = Math.pow(-a,i) * b;  // 计算p[i]，使用-a的i次方乘以b
        }  // 循环结束
//        for(int i=0;i<4;i++){  // 已注释：循环计算4阶系数
//            p[8 + i] = Math.pow(a,Math.pow(2,i)) * (1L << bit);  // 已注释：计算长整型系数
//        }  // 已注释：循环结束
        p[8] = -a * (1L << bit);  // 计算p[8]，用于补偿
        double A = Math.pow(a, 8);  // 计算A为a的8次方
        p[9] = Math.pow(A, 1) * (1L << bit);  // 计算p[9]，A的1次方
        p[10] = Math.pow(A, 2) * (1L << bit);  // 计算p[10]，A的2次方
        p[11] = Math.pow(A, 4) * (1L << bit);  // 计算p[11]，A的4次方
        p[12] = Math.pow(A, 6) * (1L << bit);  // 计算p[12]，A的6次方

        double v = 0;  // 中间变量，用于比较
        double m = Math.abs(p[0]);  // 初始最大值为p[0]的绝对值
        for (int i = 1; i < 8; i++) {  // 循环：查找最大值
            v = Math.abs(p[i]);  // 计算p[i]的绝对值
            if (m < v) {  // 判断：如果当前值大于最大值
                m = v;  // 更新最大值
            }  // 判断结束
        }  // 循环结束

        int j = 0;  // 移位计数器，初始为0
        while (m * (1L << j) < 0xFFFF) {  // 循环：计算归一化移位量
            j++;  // 移位计数器加1
            if (j > 62) {  // 判断：是否超过最大移位量
                break;  // 跳出循环
            }  // 判断结束
        }  // 循环结束
        j--;  // 移位量减1，确保不溢出

        long n = 0;  // 中间变量，用于存储长整型系数
        for (int i = 0; i < 8; i++) {  // 循环：输出归一化系数
            out[idx + i] = (int) Math.round(p[i] * (1L << j));  // 输出归一化后的系数
        }  // 循环结束
        for (int i = 0; i <= 4; i++) {  // 循环：输出长整型系数
            n = Math.round(p[8 + i]);  // 计算长整型系数
            out[idx + 8 + i * 2] = (int) (n & 0x3FFFF);  // 输出低18位
            out[idx + 9 + i * 2] = (int) ((n >>> 18) & 0x3FFFF);  // 输出高18位
        }  // 循环结束
        out[idx + 18] = j;  // 输出移位量
    }  // 方法结束
    /**
     * 重载电压档位改变引起的前置滤波系数变化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>当电压档位改变时，检查是否需要重新加载AD前置滤波系数</li>
     *   <li>200uV档位需要限制带宽，其他档位不需要</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     */
    public void cmdReload_AD_coef_VOL_dangChange(int chIdx) {  // 重载电压档位改变引起的前置滤波系数变化方法

        if (!ChannelFactory.isDynamicCh(chIdx)) return;  // 判断：是否为动态通道，如果不是则返回
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取通道对象
        if (channel != null) {  // 判断：通道是否有效
            double ix = channel.getVScaleVal()/channel.getProbeRate();  // 计算实际电压档位值
            if (vScaleLast[chIdx] != ix) {  // 判断：档位是否改变
                vScaleLast[chIdx] = ix;  // 更新上次档位值
                cmdReload_AD_coef(chIdxToFpgaIdx(chIdx));  // 重新加载AD前置滤波系数
            }  // 判断结束
        }  // 判断结束
    }  // 方法结束

    /**
     * 设置通道噪声参数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据通道采样数和电压档位设置噪声参数</li>
     *   <li>特定档位（20mV、500mV）需要增加噪声参数</li>
     *   <li>校准模式下噪声参数设为0</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdChNoise(int fpgaIdx){  // 设置通道噪声参数方法
        boolean[] sle = new boolean[ChannelFactory.CH_CNT];  // 通道采样使能数组
        int chCnt = scope.getChannelSampOnCnt(scope.isRun(true), sle);  // 获取采样通道数
        int [] vv = {0,0,0,0};  // 噪声参数数组，初始为0
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引

        switch (chCnt) {  // 根据采样通道数设置噪声参数
            case 2:  // 2通道模式
                for(int i=beginIdx;i<endIdx;i++){  // 循环：遍历所有通道
                    if(sle[i]) {  // 判断：通道是否采样
                        Channel channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象

                        if(channel.getResistanceType() == Channel.RESISTANCE_1M) {  // 判断：是否为1M阻抗
                            double xv = channel.getVScaleVal() / channel.getProbeRate();  // 计算实际电压档位值
                            if ((xv > 0.015 && xv < 0.0250001) || (xv > 0.4 && xv < 0.6000001)) {  // 判断：是否为20mV或500mV档位
                                Arrays.fill(vv, 5);  // 设置噪声参数为5
                            }  // 判断结束
                        }  // 判断结束
                    }  // 判断结束
                }  // 循环结束

                break;  // 跳出switch
            case 4:  // 4通道模式
                int k = 0;  // 通道计数器
                for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                    if (sle[i]) {  // 判断：通道是否采样
                        Channel channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
                        if(channel.getResistanceType() == Channel.RESISTANCE_1M) {  // 判断：是否为1M阻抗
                            double xv = channel.getVScaleVal() / channel.getProbeRate();  // 计算实际电压档位值
                            if ((xv > 0.015 && xv < 0.0250001) || (xv > 0.4 && xv < 0.6000001)) {  // 判断：是否为20mV或500mV档位
                                vv[2 * k] = 5;  // 设置当前通道噪声参数为5
                                vv[2 * k + 1] = 5;  // 设置配对通道噪声参数为5
                            }  // 判断结束
                        }  // 判断结束
                        k++;  // 通道计数器加1
                    }  // 判断结束
                }  // 循环结束
                break;  // 跳出switch
            case 8:  // 8通道模式
                for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                    Channel channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
                    if(channel.getResistanceType() == Channel.RESISTANCE_1M) {  // 判断：是否为1M阻抗
                        double xv = channel.getVScaleVal() / channel.getProbeRate();  // 计算实际电压档位值
                        if ((xv > 0.015 && xv < 0.0250001)  // 判断：是否为20mV档位
                                || (xv > 0.4 && xv < 0.6000001)) {  // 判断：是否为500mV档位
                            vv[i - beginIdx] = 5;  // 设置噪声参数为5
                        }  // 判断结束
                    }  // 判断结束
                }  // 循环结束
                break;  // 跳出switch
        }  // switch结束

        if(isCalibrate()) {  // 判断：是否为校准模式
            Arrays.fill(vv,0);  // 校准模式下噪声参数设为0
        }  // 判断结束

        setChNosie(fpgaIdx, vv);  // 设置通道噪声参数
    }  // 方法结束
    
    /**
     * 带宽步进值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>带宽限制的步进值为50MHz</li>
     *   <li>用于计算带宽限制滤波器的截止频率</li>
     * </ul>
     */
    final static long BANDWIDTH_SETP = 50_000_000L;  // 带宽步进值，50MHz
    
    /**
     * 带宽修正值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>带宽限制的修正值为150MHz</li>
     *   <li>用于补偿滤波器的过渡带</li>
     * </ul>
     */
    final static long BANDWIDTH_CHA = 150_000_000L;  // 带宽修正值，150MHz
    
    /**
     * 带宽限制频率数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>预定义的带宽限制频率值</li>
     *   <li>从20MHz到1GHz，步进50MHz</li>
     *   <li>每个值都加上修正值</li>
     * </ul>
     */
    final static long [] sBandWidth ={  // 带宽限制频率数组
        20_000_000L  + BANDWIDTH_CHA,  // 20MHz + 150MHz
        50_000_000L  + BANDWIDTH_CHA,  // 50MHz + 150MHz
        100_000_000L + BANDWIDTH_CHA,  // 100MHz + 150MHz
        150_000_000L + BANDWIDTH_CHA,  // 150MHz + 150MHz
        200_000_000L + BANDWIDTH_CHA,  // 200MHz + 150MHz
        250_000_000L + BANDWIDTH_CHA,  // 250MHz + 150MHz
        300_000_000L + BANDWIDTH_CHA,  // 300MHz + 150MHz
        350_000_000L + BANDWIDTH_CHA,  // 350MHz + 150MHz
        400_000_000L + BANDWIDTH_CHA,  // 400MHz + 150MHz
        450_000_000L + BANDWIDTH_CHA,  // 450MHz + 150MHz
        500_000_000L + BANDWIDTH_CHA,  // 500MHz + 150MHz
        550_000_000L + BANDWIDTH_CHA,  // 550MHz + 150MHz
        600_000_000L + BANDWIDTH_CHA,  // 600MHz + 150MHz
        650_000_000L + BANDWIDTH_CHA,  // 650MHz + 150MHz
        700_000_000L + BANDWIDTH_CHA,  // 700MHz + 150MHz
        750_000_000L + BANDWIDTH_CHA,  // 750MHz + 150MHz
        800_000_000L + BANDWIDTH_CHA,  // 800MHz + 150MHz
        850_000_000L + BANDWIDTH_CHA,  // 850MHz + 150MHz
        900_000_000L + BANDWIDTH_CHA,  // 900MHz + 150MHz
        950_000_000L + BANDWIDTH_CHA,  // 950MHz + 150MHz
        1000_000_000L+ BANDWIDTH_CHA,  // 1GHz + 150MHz
    };  // 数组结束

    /**
     * 重载前置滤波系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据通道带宽设置重新计算AD前置滤波系数</li>
     *   <li>支持低通、高通、全通等滤波模式</li>
     *   <li>支持20MHz、200MHz、300MHz等固定带宽</li>
     * </ul>
     * 
     * <p><b>滤波器类型：</b>
     * <pre>
     * BANDWIDTH_TYPE_FULL：全带宽，无滤波
     * BANDWIDTH_TYPE_LOWPASS：低通滤波
     * BANDWIDTH_TYPE_HIGHPASS：高通滤波
     * BANDWIDTH_TYPE_20M：20MHz带宽限制
     * BANDWIDTH_TYPE_200M：200MHz带宽限制
     * BANDWIDTH_TYPE_300M：300MHz带宽限制
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdReload_AD_coef(int fpgaIdx) {  // 重载前置滤波系数方法

        double fc = 0;  // 截止频率，初始为0
        int type = 0;  // 滤波器类型，初始为0
        int bandWidthType;  // 带宽类型
        Channel channel = null;  // 通道对象，初始为null

        // 判断每个通道的滤波方式和Fc值
        int[] type1 = {0, 0, 0, 0};  // 滤波器类型数组
        double[] fc1 = {0, 0, 0, 0};  // 截止频率数组
        double[] fc12 = {0, 0, 0, 0};  // 截止频率数组（带修正）

        int[] type3 = {0,0,0,0};  // 特殊滤波器类型数组


        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        long fs = adc.getMaxAdInClk() * 1000L * 1000 / maxAdcChNums;  // 计算采样频率

        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            int j = i - beginIdx;  // 计算相对索引
            channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
            if (channel == null) continue;  // 判断：通道是否有效

            fc = channel.getBandWidth();  // 获取带宽限制值
            bandWidthType = channel.getBandWidthType();  // 获取带宽类型

            if(isCalibrate()) {  // 判断：是否为校准模式
                type = 0;  // 设置滤波器类型为0
                bandWidthType = Channel.BANDWIDTH_TYPE_FULL;  // 设置为全带宽
                fc12[j] = fc = Channel.MAX_BANDWIDTH;  // 设置为最大带宽
            }else{  // 否则：正常模式
                type = 1;  // 设置滤波器类型为1
                int idx = (int) (Math.ceil(Channel.getMaxBandWidth() / BANDWIDTH_SETP) + 0.01);  // 计算带宽索引

                if (idx < sBandWidth.length) {  // 判断：索引是否在数组范围内
                    fc12[j] = sBandWidth[idx];  // 从数组获取带宽值
                }else{  // 否则：索引超出范围
                    fc12[j] = Channel.MAX_BANDWIDTH;  // 设置为最大带宽
                }  // 判断结束
                if(channel.getResistanceType() == Channel.RESISTANCE_1M){  // 判断：是否为1M阻抗
                    if(fc12[j] > Channel.MAX_BANDWIDTH/2){  // 判断：带宽是否超过最大带宽的一半
                        fc12[j] = Channel.MAX_BANDWIDTH;  // 设置为最大带宽
                    }  // 判断结束
                    if(HardwareProduct.isMHO68V2()) {  // 判断：是否为MHO68V2型号
                        double v = channel.getVScaleVal() / channel.getProbeRate();  // 计算实际电压档位值
                        if ((v > 0.025 && v < 0.08)  // 判断：是否在50mV档位范围
                                || (v > 0.6 && v < 1.5)) {  // 判断：是否在1V档位范围
                            fc12[j] = 700 * 1e6;  // 设置带宽为700MHz
                            type3[j] = (1 << 17);  // 设置特殊滤波器类型标志
                        }  // 判断结束
                    }  // 判断结束
                }  // 判断结束
                if(HardwareProduct.isMHO68V1() || HardwareProduct.isMHO68V2()) {  // 判断：是否为MHO68V1或V2型号
                    if (fc12[j] >= Channel.MAX_BANDWIDTH * 3 / 4) {  // 判断：带宽是否超过最大带宽的3/4
                        type = 0;  // 设置滤波器类型为0
                    }  // 判断结束
                }else {  // 否则：其他型号
                    if(bandWidthType == Channel.BANDWIDTH_TYPE_FULL){  // 判断：是否为全带宽类型
                        type = 0;  // 设置滤波器类型为0
                    }  // 判断结束
                }  // 判断结束
            }  // 判断结束


            switch (bandWidthType) {  // 根据带宽类型设置参数
                case Channel.BANDWIDTH_TYPE_LOWPASS:  // 低通滤波
                    type |= (0 << 16)  // =0低通；=1高通
                            | (1 << 17);  // =0关闭滤波器；=1开始滤波器

                    break;  // 跳出case
                case Channel.BANDWIDTH_TYPE_HIGHPASS:  // 高通滤波
                    type |= (1 << 16)  // =0低通；=1高通
                            | (1 << 17);  // =0关闭滤波器；=1开始滤波器

                    break;  // 跳出case
                case Channel.BANDWIDTH_TYPE_20M:  // 20MHz带宽限制
                case Channel.BANDWIDTH_TYPE_200M:  // 200MHz带宽限制
                case Channel.BANDWIDTH_TYPE_300M:  // 300MHz带宽限制
                    type |= 1;  // 设置类型标志
                    fc12[j] = fc;  // 使用原始带宽值
                    break;  // 跳出case
            }  // switch结束

            type1[j] = type;  // 保存滤波器类型
            fc1[j] = fc;  // 保存截止频率
            Log.d(TAG,"lvbb -- ch:" + i + ",type:" + Integer.toHexString(type) + ",fc:" + fc + ",fc2:" + fc12[j]);  // 输出日志
        }  // 循环结束

        // 结合fpga对单双四通道的特殊要求
        int[] type2 = {0, 0, 0, 0};  // 滤波器类型数组2
        double[] fc2 = {0, 0, 0, 0};  // 截止频率数组2
        double[] fc22 = {0, 0, 0, 0};  // 截止频率数组2（带修正）
        Scope scope = Scope.getInstance();  // 获取示波器实例
        boolean[] sle = new boolean[ChannelFactory.CH_CNT];  // 通道选择数组
        int chCnt = scope.getChannelSampOnCnt(scope.isRun(true), sle);  // 获取采样通道数

        switch (chCnt) {  // 根据采样通道数处理
            case 2:  // 2通道模式
                {
                    for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                        if (sle[i]) {  // 判断：通道是否采样
                            int idx = i - beginIdx;  // 计算相对索引
                            Arrays.fill(fc22,fc12[idx]);  // 填充截止频率数组
                            Arrays.fill(fc2,fc1[idx]);  // 填充截止频率数组
                            Arrays.fill(type2,type1[idx]);  // 填充滤波器类型数组
                            Arrays.fill(type3,type3[idx]);  // 填充特殊滤波器类型数组
                            break;  // 跳出循环
                        }  // 判断结束
                    }  // 循环结束
                    break;  // 跳出case
                }  // 代码块结束
            case 4:  // 4通道模式
            {
                int k = 0;  // 通道计数器
                for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                    if (sle[i]) {  // 判断：通道是否采样
                        int idx = i - beginIdx;  // 计算相对索引
                        fc22[2 * k] = fc12[idx];  // 设置偶数索引截止频率
                        fc22[2 * k + 1] = fc12[idx];  // 设置奇数索引截止频率
                        fc2[2 * k] = fc1[idx];  // 设置偶数索引截止频率
                        fc2[2 * k + 1] = fc1[idx];  // 设置奇数索引截止频率
                        type2[2 * k] = type1[idx];  // 设置偶数索引滤波器类型
                        type2[2 * k + 1] = type1[idx];  // 设置奇数索引滤波器类型
                        type3[2 * k] = type3[idx];  // 设置偶数索引特殊滤波器类型
                        type3[2 * k + 1] = type3[idx];  // 设置奇数索引特殊滤波器类型
                        k++;  // 通道计数器加1
                    }  // 判断结束
                }  // 循环结束
                break;  // 跳出case
            }  // 代码块结束
            case 8:  // 8通道模式
            default:  // 默认情况
                for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                    int idx = i - beginIdx;  // 计算相对索引
                    fc2[idx] = fc1[idx];  // 复制截止频率
                    fc22[idx] = fc12[idx];  // 复制截止频率（带修正）
                    type2[idx] = type1[idx];  // 复制滤波器类型
                }  // 循环结束
                break;  // 跳出case
        }  // switch结束


        {  // 代码块：计算并发送滤波器系数

            int N = HardwareProduct.isMHO68V2() ? 32 : 16;  // 根据硬件型号确定系数数量
            int[] vol = new int[1 + N * 4 + N * 4];  // 创建系数数组
            Arrays.fill(vol, 0);  // 初始化数组为0
            int bits = 30;  // 归一化位数


            for (int i = 3; i >= 0; i--) {  // 循环：遍历所有通道（从后向前）
                if ((type2[i] & (1 << 17)) != 0) {  // 判断：滤波器是否使能
                    // 滤波器使能
                    double[] coef = {0, 0, 0, 0};  // 滤波器系数数组
                    int idx = 1 + i * N;  // 计算数组索引
                    fc = fc2[i];  // 获取截止频率
                    if (iir_coef_cal(fc, fs, 0, coef)) {  // 判断：IIR系数计算是否成功
                        // 归一化
                        if(N > 16) {  // 判断：系数数量是否大于16
                            iir_coef_normalize_8(coef, vol, idx, bits);  // 使用8阶归一化
                        }else{  // 否则
                            iir_coef_normalize(coef,vol,idx,bits);  // 使用普通归一化
                        }  // 判断结束
                    }  // 判断结束
                }  // 判断结束

                if(type3[i] != 0){  // 判断：特殊滤波器类型是否有效
                    type2[i] |= 0x01;  // 设置特殊滤波器标志
                }  // 判断结束

                if((type2[i] &0x01) != 0){  // 判断：是否需要计算第二组系数
                    double[] coef = {0, 0, 0, 0};  // 滤波器系数数组
                    int idx = 1 + 4 * N + i * N;  // 计算数组索引
                    fc = fc22[i];  // 获取截止频率
                    if (iir_coef_cal(fc, fs, 0, coef)) {  // 判断：IIR系数计算是否成功
                        // 归一化
                        if(N > 16) {  // 判断：系数数量是否大于16
                            iir_coef_normalize_8(coef, vol, idx, bits);  // 使用8阶归一化
                        }else{  // 否则
                            iir_coef_normalize(coef,vol,idx,bits);  // 使用普通归一化
                        }  // 判断结束
                    }  // 判断结束
                }  // 判断结束

                vol[0] <<= 4;  // 左移4位
                vol[0] |= type2[i];  // 设置滤波器类型
            }  // 循环结束

            int len = vol.length * 4;  // 计算数据长度
            FPGAReg reg = new FPGAReg(FPGAReg.FPGA_AD_COEF, len);  // 创建AD系数寄存器
            reg.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
            reg.setVal(vol);  // 设置寄存器值
            sendCommand(reg);  // 发送命令
        }  // 代码块结束
    }  // 方法结束

    /**
     * 生成总线类型配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置串行通道的总线类型</li>
     *   <li>支持UART、LIN、CAN、SPI、I2C、ARINC429、MILSTD1553B等总线</li>
     *   <li>根据显示模式调整使能状态</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_Bus_Type(int fpgaIdx) {  // 生成总线类型配置方法
        Log.d(TAG, "gntR_Bus_Type() called with: fpgaIdx = [" + fpgaIdx + "]");  // 输出日志
        FPGAReg_BUS_TYPE regBusType = (FPGAReg_BUS_TYPE) getFPGAReg(fpgaIdx, FPGAReg.FPGA_BUS_TYPE);  // 获取总线类型寄存器
        regBusType.reset();  // 重置寄存器

        SerialChannel serialChannel;  // 串行通道对象
        int beginIdx = beginSerialIdx(fpgaIdx);  // 获取起始串行通道索引
        int endIdx = endSerialIdx(fpgaIdx);  // 获取结束串行通道索引
        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有串行通道
            serialChannel = ChannelFactory.getSerialChannel(i);  // 获取串行通道对象
            if (serialChannel.isOpen()  // 判断：串行通道是否打开
                    || TriggerFactory.getTriggerType()  // 或：触发类型是否匹配
                            == (Trigger.TRIG_TYPE_SERIAL1 + (i - ChannelFactory.S1))) {
                Logger.d(TAG, "getBusType : " + serialChannel.getBusType() + ",S:" + serialChannel.getChId());  // 输出日志
                int j = i - beginIdx;  // 计算相对索引
                switch (serialChannel.getBusType()) {  // 根据总线类型设置参数
                    case IBus.UART:  // UART总线
                        regBusType.setUART(j, 1);  // 设置UART使能
                        break;  // 跳出case
                    case IBus.LIN:  // LIN总线
                        {
                            LinBus lin = (LinBus) serialChannel.getBus(IBus.LIN);  // 获取LIN总线对象
                            switch (lin.getLinType()) {  // 根据LIN类型设置参数
                                default:  // 默认情况
                                case LinBus.LIN_TYPE_1_3:  // LIN 1.3版本
                                    regBusType.setLin(j, 1);  // 设置LIN 1.3使能
                                    break;  // 跳出case
                                case LinBus.LIN_TYPE_2_0:  // LIN 2.0版本
                                    regBusType.setLin2_0(j, 1);  // 设置LIN 2.0使能
                                    break;  // 跳出case
                                case LinBus.LIN_TYPE_1_3_LONG:  // LIN 1.3长帧
                                    regBusType.setLin1_3_Long(j, 1);  // 设置LIN 1.3长帧使能
                                    break;  // 跳出case
                            }  // switch结束
                        }  // 代码块结束
                        break;  // 跳出case
                    case IBus.CAN:  // CAN总线
                        {
                            CanBus bus = (CanBus) serialChannel.getBus(IBus.CAN);  // 获取CAN总线对象
                            if (bus.isCanFDEnable()) {  // 判断：是否启用CAN FD
                                regBusType.setCanFD(j, 1);  // 设置CAN FD使能
                            } else {  // 否则：标准CAN
                                regBusType.setCan(j, 1);  // 设置CAN使能
                            }  // 判断结束
                        }  // 代码块结束
                        break;  // 跳出case
                    case IBus.SPI:  // SPI总线
                        regBusType.setSPI(j, 1);  // 设置SPI使能
                        break;  // 跳出case
                    case IBus.I2C:  // I2C总线
                        regBusType.setI2C(j, 1);  // 设置I2C使能
                        break;  // 跳出case
                    case IBus.ARINC429:  // ARINC429总线
                        regBusType.set429(j, 1);  // 设置ARINC429使能
                        break;  // 跳出case
                    case IBus.MILSTD1553B:  // MILSTD1553B总线
                        regBusType.set1553B(j, 1);  // 设置MILSTD1553B使能
                        break;  // 跳出case
                }  // switch结束
                regBusType.setEnable(j, 1);  // 设置总线使能
            }  // 判断结束
        }  // 循环结束

        Scope scope = Scope.getInstance();  // 获取示波器实例
        if (scope.isInScrollMode() || scope.isInSlowScaleMode() || scope.isInXYMode()) {  // 判断：是否处于滚动、慢速缩放或XY模式
            regBusType.setEnable(0, 0);  // 禁用总线0
            regBusType.setEnable(1, 0);  // 禁用总线1
        }  // 判断结束
        sendCommand(regBusType);  // 发送总线类型命令
        if (regBusType.isEnable(0) || regBusType.isEnable(1)) {  // 判断：是否有总线使能
            setDataFreq(fpgaIdx, 16, 16);  // 设置数据刷新频率为16Hz
        } else {  // 否则：无总线使能
            setDataFreq(fpgaIdx,  60 ,  60 );  // 设置数据刷新频率为60Hz
        }  // 判断结束
    }  // 方法结束

    /**
     * 获取通道主触发电平
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算通道的主触发电平值</li>
     *   <li>用于总线触发功能</li>
     *   <li>将电压值转换为ADC码值</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @return 主触发电平ADC码值
     */
    private int getCh_Primary_Level(int chIdx) {  // 获取通道主触发电平方法
        int chLevel = 0;  // 电平值，初始为0
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取通道对象
        if (channel != null) {  // 判断：通道是否有效
            int v = (int) (Math.pow(2, hwConfig.getAdbits()) + 0.1);  // 计算ADC最大值
            int vol = (int) Math.round(channel.getBusPrimaryLevel()) + (int) Math.round(channel.getPosFix());  // 计算电压值（含位置修正）

            vol *= hwConfig.getPix2AdFactor();  // 转换为ADC单位
            vol /= channel.getYFactor();  // 除以垂直缩放因子
            chLevel = (int) (vol / ADGear2PixBuf(chIdx, false) + v / 2);  // 计算电平ADC码值
            if (chLevel > v) {  // 判断：是否超过最大值
                chLevel = v;  // 限制为最大值
            } else if (chLevel < 0) {  // 判断：是否小于最小值
                chLevel = 0;  // 限制为最小值
            }  // 判断结束
        }  // 判断结束
        return chLevel;  // 返回电平值
    }  // 方法结束

    /**
     * 获取通道副触发电平
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算通道的副触发电平值</li>
     *   <li>用于总线触发功能</li>
     *   <li>将电压值转换为ADC码值</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @return 副触发电平ADC码值
     */
    private int getCh_Secondary_Level(int chIdx) {  // 获取通道副触发电平方法
        int chLevel = 0;  // 电平值，初始为0
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取通道对象
        if (channel != null) {  // 判断：通道是否有效
            int vol = (int) Math.round(channel.getBusSecondaryLevel()) + (int) Math.round(channel.getPosFix());  // 计算电压值（含位置修正）
            int v = (int) (Math.pow(2, hwConfig.getAdbits()) + 0.1);  // 计算ADC最大值
            vol *= hwConfig.getPix2AdFactor();  // 转换为ADC单位
            vol /= channel.getYFactor();  // 除以垂直缩放因子
            chLevel = (int) (vol / ADGear2PixBuf(chIdx, false) + v / 2);  // 计算电平ADC码值
            if (chLevel > v) {  // 判断：是否超过最大值
                chLevel = v;  // 限制为最大值
            } else if (chLevel < 0) {  // 判断：是否小于最小值
                chLevel = 0;  // 限制为最小值
            }  // 判断结束
        }  // 判断结束
        return chLevel;  // 返回电平值
    }  // 方法结束

    /**
     * 生成总线电平配置命令
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置总线触发的主电平和副电平</li>
     *   <li>支持不同硬件型号的通道映射</li>
     *   <li>发送电平配置到FPGA</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_Bus_Level(int fpgaIdx) {  // 生成总线电平配置命令方法
        Scope scope = Scope.getInstance();  // 获取示波器实例
        FPGAReg_BUS_PRIMARY regBusPrimary =  // 获取主总线电平寄存器
                (FPGAReg_BUS_PRIMARY) getFPGAReg(fpgaIdx, FPGAReg.FPGA_BUS_PRIMARY);
        FPGAReg_BUS_PRIMARY_EXT regBusPrimaryExt =  // 获取主总线电平扩展寄存器
                (FPGAReg_BUS_PRIMARY_EXT) getFPGAReg(fpgaIdx, FPGAReg.FPGA_BUS_PRIMARY_EXT);

        FPGAReg_BUS_SECONDARY busSecondary =  // 获取副总线电平寄存器
                (FPGAReg_BUS_SECONDARY) getFPGAReg(fpgaIdx, FPGAReg.FPGA_BUS_SECONDARY);
        FPGAReg_BUS_SECONDARY_EXT busSecondaryExt =  // 获取副总线电平扩展寄存器
                (FPGAReg_BUS_SECONDARY_EXT) getFPGAReg(fpgaIdx, FPGAReg.FPGA_BUS_SECONDARY_EXT);

        boolean[] sel = new boolean[ChannelFactory.CH_CNT];  // 通道选择数组
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true), sel);  // 获取采样通道数
        int nums = hwConfig.getAdcMaxChNums();  // 获取ADC最大通道数

        for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
            if (sel[i]) {  // 判断：通道是否采样
                int j = i - beginIdx;  // 计算相对索引
                if(HardwareProduct.isMHO68V1()){  // 判断：是否为MHO68V1型号
                    if(cnt <=2){  // 判断：采样通道数是否小于等于2
                        j = 0;  // 映射到通道0
                    }else if(cnt <= 4) {  // 判断：采样通道数是否小于等于4
                        j = j == 0 ? 0 : 2;  // 映射到通道0或2
                    }  // 判断结束
                }else{  // 否则：其他型号
                    if(cnt <= 4) {  // 判断：采样通道数是否小于等于4
                        j = (j / nums) * nums;  // 按ADC通道数分组映射
                    }  // 判断结束
                }  // 判断结束

                int primaryLevel = getCh_Primary_Level(i);  // 获取主触发电平
                regBusPrimary.setLevel(j, primaryLevel);  // 设置主电平低8位
                regBusPrimaryExt.setLevel(j, (primaryLevel >>> 8) & 0xFF);  // 设置主电平高8位

                int secondaryLevel = getCh_Secondary_Level(i);  // 获取副触发电平
                busSecondary.setLevel(j,secondaryLevel);  // 设置副电平低8位
                busSecondaryExt.setLevel(j,(secondaryLevel >>> 8) & 0xFF);  // 设置副电平高8位
            }  // 判断结束
        }  // 循环结束

//        FPGAReg_BUS_SECONDARY busSecondary =  // 已注释：获取副总线电平寄存器
//                (FPGAReg_BUS_SECONDARY) getFPGAReg(fpgaIdx, FPGAReg.FPGA_BUS_SECONDARY);  // 已注释
//        FPGAReg_BUS_SECONDARY_EXT busSecondaryExt =  // 已注释：获取副总线电平扩展寄存器
//                (FPGAReg_BUS_SECONDARY_EXT) getFPGAReg(fpgaIdx, FPGAReg.FPGA_BUS_SECONDARY_EXT);  // 已注释
//        SerialChannel serialChannel;  // 已注释：串行通道对象
//        IBus iBus;  // 已注释：总线接口对象
//        int tmpLevel = 0;  // 已注释：临时电平值
//        beginIdx = beginSerialIdx(fpgaIdx);  // 已注释：获取起始串行通道索引
//        endIdx = endSerialIdx(fpgaIdx);  // 已注释：获取结束串行通道索引
//        for (int i = beginIdx; i < endIdx; i++) {  // 已注释：循环遍历串行通道
//            tmpLevel = 0;  // 已注释：初始化临时电平值
//            serialChannel = ChannelFactory.getSerialChannel(i);  // 已注释：获取串行通道
//            if (serialChannel != null) {  // 已注释：判断串行通道是否有效
//                int j = i - beginIdx;  // 已注释：计算相对索引
//                iBus = serialChannel.getBus();  // 已注释：获取总线接口
//                if (iBus.getBusType() == IBus.ARINC429){  // 已注释：判断是否为ARINC429总线
//                    int idx = ((ARINC429Bus) iBus).getSrcChIdx();  // 已注释：获取源通道索引
//                    tmpLevel = getCh_Secondary_Level(idx);  // 已注释：获取副触发电平
//                }  // 已注释：判断结束
//                busSecondary.setLevel(j, tmpLevel);  // 已注释：设置副电平低8位
//                busSecondaryExt.setLevel(j, (tmpLevel >>> 8) & 0xFF);  // 已注释：设置副电平高8位
//            }  // 已注释：判断结束
//        }  // 已注释：循环结束
        sendCommand(regBusPrimary);  // 发送主电平配置命令
        sendCommand(regBusPrimaryExt);  // 发送主电平扩展配置命令
        sendCommand(busSecondary);  // 发送副电平配置命令
        sendCommand(busSecondaryExt);  // 发送副电平扩展配置命令
    }  // 方法结束

    /**
     * 总线配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置串行通道的总线参数</li>
     *   <li>支持CAN总线扩展配置</li>
     *   <li>发送配置到FPGA</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     */
    public void Bus_Config(int chIdx) {  // 总线配置方法
        if (ChannelFactory.isSerialCh(chIdx)) {  // 判断：是否为串行通道
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(chIdx);  // 获取串行通道对象
            if (serialChannel != null) {  // 判断：串行通道是否有效
                int[] addr = {FPGAReg.FPGA_BUS1_ADDR, FPGAReg.FPGA_BUS2_ADDR};  // 总线地址寄存器数组
                int fpgaIdx = serialIdxToFpgaIdx(chIdx);  // 获取FPGA索引
                chIdx = (chIdx - ChannelFactory.S1) % (ChannelFactory.SERIAL_CNT / 2);  // 计算相对通道索引
                FPGAReg_BUS_ADDR busAddr = (FPGAReg_BUS_ADDR) getFPGAReg(fpgaIdx, addr[chIdx]);  // 获取总线地址寄存器
                IBus iBus = serialChannel.getBus();  // 获取总线接口
                busAddr.configBus(fpgaIdx, iBus);  // 配置总线参数

                if (iBus instanceof CanBus) {  // 判断：是否为CAN总线
                    int[] canExt = {FPGAReg.FPGA_BUS1_CAN_EXT, FPGAReg.FPGA_BUS2_CAN_EXT};  // CAN扩展寄存器数组
                    FPGAReg_BUS_CAN_EXT busCanExt = (FPGAReg_BUS_CAN_EXT) FPGACommand.getReg(fpgaIdx, canExt[chIdx]);  // 获取CAN扩展寄存器
                    busCanExt.configBus(fpgaIdx, iBus);  // 配置CAN总线扩展参数
                }  // 判断结束

            }  // 判断结束
        }  // 判断结束
    }  // 方法结束

    /**
     * 生成总线配置命令
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置总线类型</li>
     *   <li>配置总线地址</li>
     *   <li>配置总线电平</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void gntR_Bus(int fpgaIdx) {  // 生成总线配置命令方法
        gntR_Bus_Type(fpgaIdx);  // 配置总线类型
        Bus_Config(ChannelFactory.S1 + fpgaIdx * ChannelFactory.SERIAL_CNT / 2);  // 配置总线1
        Bus_Config(ChannelFactory.S2 + fpgaIdx * ChannelFactory.SERIAL_CNT / 2);  // 配置总线2
        gntR_Bus_Level(fpgaIdx);  // 配置总线电平
    }  // 方法结束

    /**
     * 读取FPGA ID
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从FPGA读取芯片ID</li>
     *   <li>用于识别FPGA型号</li>
     *   <li>调试时输出ID信息</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return FPGA ID值
     */
    public int readFpgaId(int fpgaIdx) {  // 读取FPGA ID方法
        FPGAReg_STATUS_ID regStatusId = new FPGAReg_STATUS_ID();  // 创建状态ID寄存器对象
        regStatusId.reset(true);  // 重置寄存器
        regStatusId.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        recvCommand(regStatusId, regStatusId.getRecvReg());  // 接收命令
        Log.d(TAG, "fpga id = " + Integer.toHexString(regStatusId.getId()) + ",fpgaIdx:" + fpgaIdx);  // 输出日志
        regStatusId.getRecvReg().Dump();  // 打印寄存器内容
        return regStatusId.getId();  // 返回FPGA ID
    }  // 方法结束

    /**
     * 读取FPGA状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从FPGA读取状态寄存器</li>
     *   <li>用于监控FPGA工作状态</li>
     *   <li>调试时输出状态信息</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return FPGA状态值
     */
    public int readFpgaStatus(int fpgaIdx) {  // 读取FPGA状态方法
        FPGAReg_STATUS regStatus = new FPGAReg_STATUS();  // 创建状态寄存器对象
        regStatus.reset(true);  // 重置寄存器
        regStatus.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        recvCommand(regStatus, regStatus.getRecvReg());  // 接收命令
        Log.d(TAG, "fpga status = " + Integer.toHexString(regStatus.getStatus()));  // 输出日志
        return regStatus.getStatus();  // 返回FPGA状态
    }  // 方法结束

    /**
     * 获取FPGA时钟输入/输出状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>读取FPGA时钟状态</li>
     *   <li>支持时钟输入和时钟输出状态查询</li>
     *   <li>用于时钟同步检测</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param isIn true=时钟输入状态，false=时钟输出状态
     * @return 时钟状态（true=正常，false=异常）
     */
    public boolean getFpgaClockInOutStatus(int fpgaIdx, boolean isIn) {  // 获取FPGA时钟输入/输出状态方法
        FPGAReg_STATUS regStatus = new FPGAReg_STATUS();  // 创建状态寄存器对象
        regStatus.reset(true);  // 重置寄存器
        regStatus.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        recvCommand(regStatus, regStatus.getRecvReg());  // 接收命令
        return isIn ? regStatus.getClockInStatus() : regStatus.getClockOutStatus();  // 返回时钟输入或输出状态
    }  // 方法结束

    /**
     * 读取FPGA版本号
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从FPGA读取固件版本号</li>
     *   <li>用于版本检查和兼容性判断</li>
     *   <li>调试时输出版本信息</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return FPGA版本号
     */
    public int readFpgaVer(int fpgaIdx) {  // 读取FPGA版本号方法
        FPGAReg_VER ver = new FPGAReg_VER();  // 创建版本寄存器对象
        ver.reset(true);  // 重置寄存器
        ver.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        recvCommand(ver, ver.getRecvReg());  // 接收命令
        Log.d(TAG, "fpga ver = " + Integer.toHexString(ver.getVersion()));  // 输出日志
        return ver.getVersion();  // 返回版本号
    }  // 方法结束

    /**
     * 获取FPGA温度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从FPGA读取芯片温度</li>
     *   <li>用于温度监控和过热保护</li>
     *   <li>单位：摄氏度</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return FPGA温度值（摄氏度）
     */
    public int getFpgaTemperature(int fpgaIdx) {  // 获取FPGA温度方法
        FPGAReg_TEMPERATURE fpgaReg_temperature =  // 创建温度寄存器对象
                (FPGAReg_TEMPERATURE) getFPGAReg(fpgaIdx, FPGAReg.FPGA_TEMPERATURE);
        fpgaReg_temperature.reset(true);  // 重置寄存器
        fpgaReg_temperature.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        recvCommand(fpgaReg_temperature, fpgaReg_temperature.getRecvReg());  // 接收命令
        return fpgaReg_temperature.getTemperature();  // 返回温度值
    }  // 方法结束

    /**
     * 获取FPGA风扇转速
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从FPGA读取风扇转速</li>
     *   <li>用于风扇监控和散热控制</li>
     *   <li>支持多个风扇</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param fans 风扇转速数组（输出参数）
     * @return 风扇数量
     */
    public int getFpgaFanSpeed(int fpgaIdx, int[] fans) {  // 获取FPGA风扇转速方法
        FPGAReg_FanSpeed fpgaRegFanSpeed =  // 创建风扇转速寄存器对象
                (FPGAReg_FanSpeed) getFPGAReg(fpgaIdx, FPGAReg.FPGA_FAN_SPEED);
        fpgaRegFanSpeed.reset(true);  // 重置寄存器
        fpgaRegFanSpeed.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        recvCommand(fpgaRegFanSpeed, fpgaRegFanSpeed.getRecvReg());  // 接收命令
        return fpgaRegFanSpeed.getFanSpeed(fans);  // 返回风扇数量并填充转速数组
    }  // 方法结束

    /**
     * 获取所有FPGA状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>读取所有FPGA的状态寄存器</li>
     *   <li>组合成一个综合状态值</li>
     *   <li>用于系统状态监控</li>
     * </ul>
     * 
     * @return 综合状态值（每个FPGA占4位）
     */
    public int getFpgaStatus(){  // 获取所有FPGA状态方法
        int v = 0;  // 综合状态值，初始为0
        FPGAReg_STATUS regStatus = new FPGAReg_STATUS();  // 创建状态寄存器对象
        for(int i=0;i<fpgaNums;i++) {  // 循环：遍历所有FPGA
            regStatus.reset(true);  // 重置寄存器
            regStatus.setFpgaIdx(i);  // 设置FPGA索引
            recvCommand(regStatus, regStatus.getRecvReg());  // 接收命令
            Log.d("123456789","" + Integer.toHexString(regStatus.getStatus()));  // 输出日志
            v |= ((regStatus.getStatus()>>25) & 0x0F) << (i * 4);  // 提取状态位并组合
        }  // 循环结束
        return v;  // 返回综合状态值
    }  // 方法结束

    /**
     * 探头处理
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>处理探头通信数据</li>
     *   <li>读取探头FIFO数据</li>
     *   <li>检测探头在线状态</li>
     *   <li>处理探头消息</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void probeProcess(int fpgaIdx) {  // 探头处理方法
        boolean[] b = {false, false, false, false};  // 通道数据标志数组
        boolean bAlive = false;  // 探头在线标志
        ProbeFactory probeFactory = ProbeFactory.getInstance();  // 获取探头工厂实例
        FPGAReg_PROBE fpgaReg_probe = new FPGAReg_PROBE();  // 创建探头寄存器对象
        fpgaReg_probe.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        int beginIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        int endIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        do {  // 循环：处理所有通道的探头数据
            for (int i = 0; i < 3; i++) {  // 循环：尝试3次读取探头状态
                fpgaReg_probe.reset(true);  // 重置寄存器
                recvCommand(fpgaReg_probe, fpgaReg_probe.getRecvReg());  // 接收命令
                if (fpgaReg_probe.getRecvReg().isHeaderValid()) {  // 判断：头部是否有效
                    break;  // 跳出循环
                }  // 判断结束
            }  // 循环结束

            if (fpgaReg_probe.getRecvReg().isHeaderValid()) {  // 判断：头部是否有效
                for (int i = beginIdx; i < endIdx; i++) {  // 循环：遍历所有通道
                    int idx = i - beginIdx;  // 计算相对索引
                    Channel channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
                    if (channel == null) continue;  // 判断：通道是否有效
                    b[idx] = bAlive = fpgaReg_probe.isProbeAlive(idx);  // 获取探头在线状态
                    if (bAlive) {  // 判断：探头是否在线
                        int s = fpgaReg_probe.getProbeFifoSize(idx);  // 获取探头FIFO大小

                        if (s > 0) {  // 判断：FIFO是否有数据
                            s += 2;  // 调整数据长度
                            FPGAReg_SPI_Read r = new FPGAReg_SPI_Read();  // 创建SPI读取寄存器
                            r.setType(FPGAReg_SPI_Read.SPI_EXT_UART);  // 设置类型为外部UART
                            r.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
                            r.setByteValid(0);  // 设置有效字节为0
                            r.setDataLength(s);  // 设置数据长度
                            r.setVal(8, 4, 1 << idx);  // 设置通道选择
                            r.setCmdLength(s * 4);  // 设置命令长度
                            ByteBuffer byteBuffer = ByteBuffer.allocate(256 /*s*4*/);  // 分配字节缓冲区
                            recvCommand(r, byteBuffer);  // 接收命令
                            if ((byteBuffer.get(6) & 0xFF) == 0xAA  // 判断：是否为帧头0xAA
                                    && (byteBuffer.get(7) & 0xFF) == 0xAA) {  // 判断：是否为帧头0xAA
                                int len =  // 计算数据长度
                                        byteBuffer.get(4) & 0xFF | (byteBuffer.get(5) & 0xFF << 8);  // 从字节4和5读取长度
                                len /= 4;  // 转换为字长度
                                len *= 4;  // 对齐到字边界
                                len = Math.min(len, byteBuffer.capacity() - 8);  // 限制最大长度
                                probeFactory.Input(channel, byteBuffer, 8, len);  // 输入数据到探头工厂
                                probeFactory.process(channel);  // 处理探头消息
                            } else {  // 否则：帧头错误
                                StringBuilder sb = new StringBuilder();  // 创建字符串构建器
                                sb.append("ch:").append(i).append(",data:");  // 添加通道信息
                                for (int j = 0; j < byteBuffer.capacity(); j++) {  // 循环：遍历所有字节
                                    sb.append(",")  // 添加逗号
                                            .append(Integer.toHexString(byteBuffer.get(j) & 0xFF));  // 添加十六进制字节值
                                }  // 循环结束
                                Log.e(TAG, "Probe 0xAAAA error," + sb.toString());  // 输出错误日志
                            }  // 判断结束
                        }  // 判断结束
                        b[idx] = s > 0;  // 设置数据标志
                    }  // 判断结束
                    probeFactory.alive(channel, bAlive);  // 通知探头在线状态
                }  // 循环结束
            } else {  // 否则：头部无效
                Log.e(TAG, "Probe Header error:" + fpgaIdx);  // 输出错误日志
            }  // 判断结束
        } while (b[0] || b[1] || b[2] || b[3]);  // 循环：直到所有通道数据处理完毕
    }  // 方法结束

    /**
     * 读取FPGA状态（带自动测量和频率计数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>读取FPGA中断状态</li>
     *   <li>读取自动测量结果</li>
     *   <li>读取频率计数结果</li>
     *   <li>填充FPGA_Status对象</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param fpgaStatus FPGA状态对象（输出参数）
     */
    public void readFpgaStatus(int fpgaIdx, FPGA_Status fpgaStatus) {  // 读取FPGA状态方法（带自动测量和频率计数）
        int sIdx, eIdx;  // 起始和结束索引
        sIdx = beginChIdx(fpgaIdx);  // 获取起始通道索引
        eIdx = endChIdx(fpgaIdx);  // 获取结束通道索引
        FPGAReg_INTRPT regIntrpt = new FPGAReg_INTRPT();  // 创建中断寄存器对象
        regIntrpt.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        regIntrpt.reset(true);  // 重置寄存器
        recvCommand(regIntrpt, regIntrpt.getRecvReg());  // 接收命令
        regIntrpt.onRecv();  // 处理接收数据
        if (regIntrpt.isAutoFinish()) {  // 判断：自动测量是否完成

            FPGAReg_STATUS_AUTO_V[] statusAutoV = new FPGAReg_STATUS_AUTO_V[4];  // 自动测量电压寄存器数组
            FPGAReg_STATUS_AUTO_CYCLE[] statusAutoCycle = new FPGAReg_STATUS_AUTO_CYCLE[4];  // 自动测量周期寄存器数组
            FPGA_Status.FpgaAuto a;  // 自动测量结果对象

            for (int i = 0; i < statusAutoV.length; i++) {  // 循环：读取所有通道的自动测量电压
                statusAutoV[i] = new FPGAReg_STATUS_AUTO_V(FPGAReg.FPGA_AUTO_V1 + i);  // 创建寄存器对象
                statusAutoV[i].setFpgaIdx(fpgaIdx);  // 设置FPGA索引
                recvCommand(statusAutoV[i], statusAutoV[i].getRecvReg());  // 接收命令
                statusAutoV[i].onRecv();  // 处理接收数据
            }  // 循环结束

            for (int i = 0; i < statusAutoCycle.length; i++) {  // 循环：读取所有通道的自动测量周期
                statusAutoCycle[i] =  // 创建寄存器对象
                        new FPGAReg_STATUS_AUTO_CYCLE(FPGAReg.FPGA_STATUS_AUTO_CYCLE_1 + i);
                statusAutoCycle[i].setFpgaIdx(fpgaIdx);  // 设置FPGA索引
                recvCommand(statusAutoCycle[i], statusAutoCycle[i].getRecvReg());  // 接收命令
            }  // 循环结束

            for (int i = sIdx; i < eIdx; i++) {  // 循环：填充所有通道的自动测量结果
                a = fpgaStatus.getAuto(i);  // 获取自动测量结果对象
                a.setVaild(true);  // 设置有效标志

                a.setMinVal(statusAutoV[i - sIdx].getMinVal());  // 设置最小值
                a.setMaxVal(statusAutoV[i - sIdx].getMaxVal());  // 设置最大值
                a.setCycle(statusAutoCycle[i - sIdx].getRecvReg().getVal(0));  // 设置周期值

                if(a.getMinVal() == a.getMaxVal()  // 判断：最小值是否等于最大值
                        && a.getMinVal() == 0){  // 判断：最小值是否为0
                    a.setVaild(false);  // 设置无效标志
                }  // 判断结束
            }  // 循环结束


        }  // 判断结束

        if (regIntrpt.isFre()) {  // 判断：频率计数是否有效
            FPGA_Status.FpgaFreq f = fpgaStatus.getFreq();  // 获取频率计数结果对象
            FreqCounter freqCounter = FreqCounter.getInstance();  // 获取频率计数器实例
            if(freqCounter.isFreqCounterEnable()) {  // 判断：频率计数器是否使能

                int idx = freqCounter.getChIdx()/4;  // 计算FPGA索引
                if(idx == fpgaIdx) {  // 判断：FPGA索引是否匹配
                    FPGAReg_STATUS_FREQ[] statusFreqs = new FPGAReg_STATUS_FREQ[2];  // 创建频率寄存器数组
                    for (int i = 0; i < 2; i++) {  // 循环：读取频率计数值
                        statusFreqs[i] = new FPGAReg_STATUS_FREQ(FPGAReg.FPGA_STATUS_CH_T + i);  // 创建寄存器对象
                        statusFreqs[i].setFpgaIdx(fpgaIdx);  // 设置FPGA索引
                        recvCommand(statusFreqs[i], statusFreqs[i].getRecvReg());  // 接收命令
                        f.setVaild(true);  // 设置有效标志
                        f.setTN(i, statusFreqs[i].getRecvReg().getVal(0));  // 设置频率计数值
                    }  // 循环结束
                }  // 判断结束
            }  // 判断结束
        }  // 判断结束
    }  // 方法结束

    /**
     * 获取FPGA分段帧数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从FPGA读取分段存储的帧数</li>
     *   <li>用于分段存储功能</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return 分段帧数
     */
    public int cmdFpgaGetSegmentFrames(int fpgaIdx) {  // 获取FPGA分段帧数方法
        FPGAReg_STATUS_FREQ status_freq = new FPGAReg_STATUS_FREQ(FPGAReg.FPGA_STATUS_FRAMES);  // 创建状态频率寄存器
        status_freq.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        recvCommand(status_freq, status_freq.getRecvReg());  // 接收命令
        return status_freq.getRecvReg().getVal(0);  // 返回分段帧数
    }  // 方法结束

    /**
     * 获取FPGA 50欧姆状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从FPGA读取50欧姆阻抗状态</li>
     *   <li>用于阻抗匹配检测</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @return 50欧姆状态值
     */
    public int cmdFpgaGet50O(int fpgaIdx) {  // 获取FPGA 50欧姆状态方法
        FPGAReg_STATUS_FREQ status_freq = new FPGAReg_STATUS_FREQ(FPGAReg.FPGA_50O_STATUS);  // 创建状态频率寄存器
        status_freq.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        recvCommand(status_freq, status_freq.getRecvReg());  // 接收命令
        return status_freq.getRecvReg().getVal(0);  // 返回50欧姆状态值
    }  // 方法结束

    /**
     * 获取FPGA数据
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从FPGA读取分段存储数据</li>
     *   <li>用于数据导出功能</li>
     *   <li>支持分段读取</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdFpgaGetData(int fpgaIdx) {  // 获取FPGA数据方法

        int ch = 0;  // 通道索引，初始为0
        int status = 0;  // 状态值，初始为0
        long num = 0;  // 数据数量，初始为0
        SaveBin bin = SaveBin.getInstance();  // 获取SaveBin实例

        ch = bin.getChIdx();  // 获取通道索引
        if(chIdxToFpgaIdx(ch) != fpgaIdx){  // 判断：FPGA索引是否匹配
            return;  // 不匹配则返回
        }  // 判断结束


        status = bin.getStatus();  // 获取状态
        num = bin.getNums();  // 获取数据数量

        FPGAReg_SEGMENT_NUMS regNums = new FPGAReg_SEGMENT_NUMS();  // 创建分段数量寄存器
        FPGAReg_SEGMENT_START regStart = new FPGAReg_SEGMENT_START();  // 创建分段起始寄存器
        regNums.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        regStart.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        int n = 1;  // 分段数量，初始为1
        int segmentLen = bin.getSegmentspace();  // 获取分段空间

        if (segmentLen > SaveBin.MAX_GET_VAL) {  // 判断：分段空间是否超过最大值
            n = 1;  // 设置为1段
        } else {  // 否则
            n = (int) num / segmentLen;  // 计算分段数量
            if (num % segmentLen > 0) {  // 判断：是否有余数
                n++;  // 分段数量加1
            }  // 判断结束
            segmentLen *= n;  // 计算总分段长度
        }  // 判断结束
        regStart.setVal(0, bin.getSegmentIdx());  // 设置分段起始索引
        regNums.setVal(0, n);  // 设置分段数量
        sendCommand(regStart);  // 发送分段起始命令
        sendCommand(regNums);  // 发送分段数量命令

        if (scope.isSegmentEnable(scope.isRun(true))) {  // 判断：是否启用分段
            if (num > segmentLen) {  // 判断：数据数量是否超过分段长度
                num = segmentLen;  // 限制为分段长度
            }  // 判断结束
        }  // 判断结束
        this.updataRegEx(0);  // 更新寄存器扩展

        Logger.d(  // 输出日志
                TAG,
                "num:"
                        + num
                        + ",segmentLen:"
                        + segmentLen
                        + ",idx:"
                        + bin.getSegmentIdx()
                        + ",n:"
                        + n);
        FPGAReg_GET_DATA reg = new FPGAReg_GET_DATA();  // 创建获取数据寄存器
        boolean[] sle = new boolean[ChannelFactory.CH_CNT];  // 通道选择数组
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true), sle);  // 获取采样通道数
        switch (cnt) {  // 根据采样通道数选择通道
            case 2:  // 2通道模式
                ch = 0;  // 设置通道为0
                break;  // 跳出switch
            case 4:  // 4通道模式
                if(HardwareProduct.isMHO68V1()){  // 判断：是否为MHO68V1型号
                    ch = ch % 2;  // 计算通道索引
                }else {  // 否则：其他型号
                    ch = (ch % 4) / 2;  // 计算通道索引
                }  // 判断结束
                break;  // 跳出switch
            default:  // 默认情况
                break;  // 跳出switch
        }  // switch结束
        reg.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
        reg.setReg(ch % 4, status, (int) num);  // 设置寄存器参数

        sendCommand(reg);  // 发送获取数据命令

    }  // 方法结束

    /**
     * 设置触发偏移
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算并设置各通道的触发延迟偏移</li>
     *   <li>补偿不同档位的延迟差异</li>
     *   <li>支持不同硬件型号的延迟补偿</li>
     * </ul>
     * 
     * <p><b>延迟计算：</b>
     * <pre>
     * 总延迟 = 通道延迟 + 档位补偿
     * n = 总延迟 / 采样周期
     * m = 总延迟 % 采样周期
     * </pre>
     * 
     * @param fpgaIdx FPGA索引
     */
    public void cmdTrigOffset(int fpgaIdx) {  // 设置触发偏移方法

        int cnt = 0;  // 采样周期计数，初始为0
        cnt = (int) (1e6 / Scope.getInstance().maxSampClk() + 0.1);  // 计算采样周期（微秒）
        int dang = 0;  // 档位索引
        for (int i = 0; i < ChannelFactory.CH_CNT; i++) {  // 循环：遍历所有通道
            Channel channel = ChannelFactory.getDynamicChannel(i);  // 获取通道对象
            FPGAReg_TRIG_OFFSET fpgaReg_trig_offset = new FPGAReg_TRIG_OFFSET(FPGAReg.FPGA_TRIG_OFFSET1 + i);  // 创建触发偏移寄存器
            if (channel != null) {  // 判断：通道是否有效
                int xd = 0;  // 档位补偿值，初始为0
                dang = CabteRegister.getRatioIdx(channel.getResistanceType(),channel.getVScaleVal()/channel.getProbeRate());  // 获取档位索引
                if(HardwareProduct.isMHO68V1()){  // 判断：是否为MHO68V1型号
                    if (channel.getResistanceType() == Channel.RESISTANCE_1M) {  // 判断：是否为1M阻抗
                        switch (dang) {  // 根据档位设置补偿值
                            case HW.RATIO_DANG_1:  // 第1档
                                xd = 0;  // 补偿值为0
                                break;  // 跳出switch
                            case HW.RATIO_DANG_3:  // 第3档
                            case HW.RATIO_DANG_2:  // 第2档
                            case HW.RATIO_DANG_4:  // 第4档
                                xd = -2666;  // 补偿值为-2666
                                break;  // 跳出switch
                        }  // switch结束
                    } else {  // 否则：50欧阻抗
                        xd = 667;  // 补偿值为667
                    }  // 判断结束
                }else if(HardwareProduct.isMHO68V2()) {  // 判断：是否为MHO68V2型号
                    if (channel.getResistanceType() == Channel.RESISTANCE_1M) {  // 判断：是否为1M阻抗
                        switch (dang) {  // 根据档位设置补偿值
                            case HW.RATIO_DANG_1:  // 第1档
                                xd = 0;  // 补偿值为0
                                break;  // 跳出switch
                            case HW.RATIO_DANG_3:  // 第3档
                            case HW.RATIO_DANG_2:  // 第2档
                                xd = -2666;  // 补偿值为-2666
                                break;  // 跳出switch
                        }  // switch结束
                    } else {  // 否则：50欧阻抗
                        xd = 667;  // 补偿值为667
                    }  // 判断结束
                }  // 判断结束

                int delay = channel.getDelay() + xd;  // 计算总延迟 = 通道延迟 + 档位补偿
                int n = delay / cnt;  // 计算整数周期数
                int m = delay % cnt;  // 计算余数
                if (m > 0) {  // 判断：余数是否大于0
                    n += 1;  // 周期数加1
                    m = cnt - Math.abs(m);  // 计算反向余数
                } else {  // 否则：余数小于等于0
                    m = Math.abs(m);  // 取绝对值
                }  // 判断结束
                fpgaReg_trig_offset.setChOffset(i, n, m * 100 / cnt);  // 设置通道偏移参数
            }  // 判断结束
            if (fpgaReg_trig_offset.getFpgaIdx() != fpgaIdx) {  // 判断：FPGA索引是否匹配
                fpgaReg_trig_offset.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
            }  // 判断结束
            sendCommand(fpgaReg_trig_offset);  // 发送触发偏移命令
        }  // 循环结束
    }  // 方法结束

    /**
     * 设置数据刷新频率
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置波形显示的刷新频率</li>
     *   <li>设置位图刷新频率</li>
     *   <li>控制FPGA数据输出速率</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param bmpFreq 位图刷新频率（Hz）
     * @param waveFreq 波形刷新频率（Hz）
     */
    public void setDataFreq(int fpgaIdx, int bmpFreq, int waveFreq) {  // 设置数据刷新频率方法

        FPGAReg_SIGNAL_FREQ regSignalFreq =  // 获取信号频率寄存器
                (FPGAReg_SIGNAL_FREQ) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SIGNAL_FREQ);
        regSignalFreq.setSignalFreq(60, 60);  // 设置刷新频率为60Hz
        {
            sendCommand(regSignalFreq);  // 发送频率配置命令
        }  // 代码块结束
    }  // 方法结束

    /**
     * 设置通道噪声参数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置各通道的噪声抑制参数</li>
     *   <li>用于优化特定档位的噪声性能</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA索引
     * @param val 噪声参数数组，每个通道一个值
     */
    public void setChNosie(int fpgaIdx,int [] val){  // 设置通道噪声参数方法
        FPGAReg_SIGNAL_FREQ regSignalFreq =  // 获取信号频率寄存器
                (FPGAReg_SIGNAL_FREQ) getFPGAReg(fpgaIdx, FPGAReg.FPGA_SIGNAL_FREQ);
        for(int i=0;i<val.length;i++) {  // 循环：遍历所有通道
            regSignalFreq.setChNosie(i, val[i]);  // 设置通道噪声参数
        }  // 循环结束
        sendCommand(regSignalFreq);  // 发送噪声配置命令
    }  // 方法结束
}  // 类结束
