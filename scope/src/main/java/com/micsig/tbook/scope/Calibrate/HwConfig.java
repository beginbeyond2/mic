package com.micsig.tbook.scope.Calibrate;

import android.os.Build;

import com.micsig.tbook.hardware.Hardware;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Calibrate.MHO38v1.HW_MHO38V1;
import com.micsig.tbook.scope.Calibrate.MHO68v1.HW_MHO68V1;
import com.micsig.tbook.scope.Calibrate.MHO68v2.HW_MHO68V2;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.fpga.CHAZHI_COEF;
import com.micsig.tbook.scope.fpga.MHO38_CHAZHI_COEF;
import com.micsig.tbook.scope.fpga.MHO68V1_CHAZHI_COEF;
import com.micsig.tbook.scope.fpga.MHO68V2_CHAZHI_COEF;
import com.micsig.tbook.scope.vertical.VerticalAxis;

/**
 * 硬件配置管理类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准与硬件控制模块）</li>
 *   <li>架构层级：硬件配置层 - 产品配置管理</li>
 *   <li>设计模式：单例模式 + 工厂方法模式</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>根据产品型号创建对应的硬件配置实例</li>
 *   <li>存储和管理ADC、硬件参数、PGA配置、通道系数等</li>
 *   <li>提供统一的硬件参数访问接口</li>
 *   <li>封装不同产品型号的硬件差异</li>
 * </ul>
 * 
 * <p><b>支持的产品型号：</b>
 * <pre>
 * 产品型号              ADC芯片           FPGA数量    ADC数量    特点
 * ─────────────────────────────────────────────────────────────────
 * MHO28/MHO38 V1       MXT2022           2           4          中端示波器
 * MHO68 V1             EV12AQ60x         2           2          高端示波器V1
 * MHO68 V2             B12DJ3200NBB/     2           4          高端示波器V2
 *                      CAE2400
 * </pre>
 * 
 * <p><b>配置参数说明：</b>
 * <ul>
 *   <li><b>ADC配置：</b>ADC芯片驱动实例，负责采样控制</li>
 *   <li><b>HW配置：</b>硬件抽象层实例，负责继电器/增益控制</li>
 *   <li><b>adBits：</b>ADC位数（如12位）</li>
 *   <li><b>adMaxVal：</b>ADC最大值（如1023，对应10位有效位）</li>
 *   <li><b>triggerLevelFactor：</b>触发电平因子</li>
 *   <li><b>adValPerPix：</b>每像素对应的ADC值</li>
 *   <li><b>wavFactor：</b>波形缩放因子</li>
 *   <li><b>adFSR：</b>ADC满量程范围</li>
 *   <li><b>triggerHuiChaFactor：</b>触发回差因子</li>
 *   <li><b>defaultChCoef：</b>默认通道系数（各档位偏移系数）</li>
 *   <li><b>defaultAdFS：</b>默认ADC采样频率配置</li>
 *   <li><b>defaultPga：</b>默认PGA增益配置</li>
 *   <li><b>yPlaceFactor：</b>Y轴位置因子</li>
 *   <li><b>pix2AdFactor：</b>像素到ADC转换因子</li>
 *   <li><b>fpgaNums：</b>FPGA数量</li>
 *   <li><b>adcNums：</b>ADC数量</li>
 *   <li><b>digitDang：</b>数字档位值（1MΩ和50Ω阻抗）</li>
 * </ul>
 * 
 * <p><b>PGA配置数组说明：</b>
 * <pre>
 * PGA（可编程增益放大器）配置数组，每个元素对应一个垂直档位：
 *   索引0: 250uV/div
 *   索引1: 500uV/div
 *   索引2: 1mV/div
 *   ...
 *   索引14: 10V/div
 * 
 * 每个PGA值包含继电器控制和增益设置信息
 * </pre>
 * 
 * <p><b>通道系数数组说明：</b>
 * <pre>
 * 通道系数用于计算各档位的偏移校准值：
 *   - 不同产品型号有不同的系数数组
 *   - 系数与垂直档位一一对应
 *   - 用于波形显示的Y轴校准
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：ADC（ADC芯片驱动）</li>
 *   <li>依赖：HW（硬件抽象层）</li>
 *   <li>依赖：Hardware（硬件信息获取）</li>
 *   <li>依赖：HardwareProduct（产品型号定义）</li>
 *   <li>依赖：CHAZHI_COEF（插值系数）</li>
 *   <li>被依赖：Scope、MemDepth18MV2等需要硬件参数的类</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>系统启动时根据产品型号初始化硬件配置</li>
 *   <li>获取ADC参数（位数、最大值、采样率等）</li>
 *   <li>获取PGA配置和通道系数</li>
 *   <li>计算存储深度时获取ADC数量</li>
 *   <li>波形显示时获取缩放因子和校准参数</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018
 * @see ADC ADC芯片驱动抽象基类
 * @see HW 硬件抽象层接口
 * @see HardwareProduct 产品型号定义
 * @see CHAZHI_COEF 插值系数管理
 */
public class HwConfig {

    /**
     * 私有构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>初始化所有硬件配置参数</li>
     *   <li>通过工厂方法allocMHO()创建实例</li>
     * </ul>
     * 
     * @param adc ADC芯片驱动实例
     * @param hw 硬件抽象层实例
     * @param adBits ADC位数（如12位）
     * @param adMaxVal ADC最大值（如1023）
     * @param triggerLevelFactor 触发电平因子
     * @param adValPerPix 每像素对应的ADC值
     * @param wavFactor 波形缩放因子
     * @param adFSR ADC满量程范围
     * @param triggerHuiChaFactor 触发回差因子
     * @param defaultChCoef 默认通道系数数组（15个档位）
     * @param defaultAdFS 默认ADC采样频率数组（15个档位）
     * @param defaultPga 默认PGA配置数组（15个档位）
     * @param yPlaceFactor Y轴位置因子
     * @param pix2AdFactor 像素到ADC转换因子
     * @param fpgaNums FPGA数量
     * @param adcNums ADC数量
     * @param digitDang1M 1MΩ阻抗下的数字档位值
     * @param digitDang50 50Ω阻抗下的数字档位值
     */
    private HwConfig(ADC adc,
                     HW hw,
                     int adBits,
                     int adMaxVal,
                     int triggerLevelFactor,
                     double adValPerPix,
                     double wavFactor,
                     double adFSR,
                     double triggerHuiChaFactor,
                     float [] defaultChCoef,
                     int [] defaultAdFS,
                     short [] defaultPga,
                     int yPlaceFactor,
                     int pix2AdFactor,
                     int fpgaNums,
                     int adcNums,
                     double digitDang1M,
                     double digitDang50){
        this.adc = adc;
        this.hw = hw;
        this.adBits = adBits;
        this.adMaxVal = adMaxVal;
        this.triggerLevelFactor = triggerLevelFactor;
        this.adValPerPix = adValPerPix;
        this.wavFactor = wavFactor;
        this.adFSR = adFSR;
        this.triggerHuiChaFactor = triggerHuiChaFactor;
        this.defaultChCoef = defaultChCoef;
        this.defaultAdFS = defaultAdFS;
        this.defaultPga = defaultPga;
        this.yPlaceFactor = yPlaceFactor;
        this.pix2AdFactor = pix2AdFactor;
        this.fpgaNums = fpgaNums;
        this.adcNums = adcNums;
        this.digitDang_1M = digitDang1M;
        this.digitDang_50 = digitDang50;
    }




    /**
     * 工厂方法：根据产品型号创建硬件配置实例
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据Build.PRODUCT判断产品型号</li>
     *   <li>初始化对应的插值系数</li>
     *   <li>创建对应的ADC和HW实例</li>
     *   <li>配置所有硬件参数</li>
     * </ul>
     * 
     * <p><b>产品配置详情：</b>
     * <pre>
     * MHO28/MHO38 V1:
     *   - ADC: MXT2022
     *   - HW: HW_MHO38V1
     *   - ADC位数: 12位
     *   - ADC最大值: 1023
     *   - FPGA数量: 2
     *   - ADC数量: 4
     * 
     * MHO68 V1:
     *   - ADC: EV12AQ60x
     *   - HW: HW_MHO68V1
     *   - ADC位数: 12位
     *   - ADC最大值: 1023
     *   - FPGA数量: 2
     *   - ADC数量: 2
     * 
     * MHO68 V2:
     *   - ADC: B12DJ3200NBB 或 CAE2400（动态检测）
     *   - HW: HW_MHO68V2
     *   - ADC位数: 12位
     *   - ADC最大值: 1023
     *   - FPGA数量: 2
     *   - ADC数量: 4
     * </pre>
     * 
     * @return 硬件配置实例，未匹配产品返回null
     */
    private static HwConfig allocMHO(){

        HwConfig hwConfig = null;
        
        // 根据产品型号创建对应的硬件配置
        switch (Build.PRODUCT){
            case HardwareProduct.RK3588_MHO28_V1:
            case HardwareProduct.RK3588_MHO38_V1:
                // MHO28/MHO38 V1：中端示波器
                // 初始化MHO38插值系数
                CHAZHI_COEF.Init(MHO38_CHAZHI_COEF.chazhiCoef_sgl,
                        MHO38_CHAZHI_COEF.chazhiCoef_dub,
                        MHO38_CHAZHI_COEF.chazhiCoef_Line
                );
                // 创建配置：ADC_MXT2022, HW_MHO38V1, 12位ADC, 4个ADC
                hwConfig =  new HwConfig(new ADC_MXT2022(),new HW_MHO38V1(2),
                        12,1023,16,1,
                        0.25,0.86,0.8,
                        cn_chCoef_mho38_v1, adfs_mho38_v1, pga_mho38_v1,4,
                        4,2,4,0.0001,0.0001);
                break;
                
            case HardwareProduct.RK3588_MHO68_V1:
                // MHO68 V1：高端示波器第一代
                // 初始化MHO68V1插值系数
                CHAZHI_COEF.Init(MHO68V1_CHAZHI_COEF.chazhiCoef_sgl,
                        MHO68V1_CHAZHI_COEF.chazhiCoef_dub,
                        MHO68V1_CHAZHI_COEF.chazhiCoef_Line
                );
                // 创建配置：ADC_EV12AQ60x, HW_MHO68V1, 12位ADC, 2个ADC
                hwConfig = new HwConfig(new ADC_EV12AQ60x(),new HW_MHO68V1(2),
                        12,1023,16,1,
                        0.25,0.9658,1,
                        cn_chCoef_mho68_v1, adfs_mho68_v1, pga_mho68_v1,4,
                        4,2,2,0.0015,0.0015);
                break;
                
            case HardwareProduct.RK3588_MHO68_V2:
                // MHO68 V2：高端示波器第二代
                // 初始化MHO68V2插值系数
                CHAZHI_COEF.Init(MHO68V2_CHAZHI_COEF.chazhiCoef_sgl,
                        MHO68V2_CHAZHI_COEF.chazhiCoef_dub,
                        MHO68V2_CHAZHI_COEF.chazhiCoef_Line
                );
                // 动态检测ADC型号：B12DJ3200NBB 或 CAE2400
                hwConfig = new HwConfig(Hardware.getInstance().isAdcB12DJ3200NBB() ? new ADC_B12DJ3200NBB() : new ADC_CAE2400(),
                        new HW_MHO68V2(2),
                        12, 1023, 16, 1,
                        0.25, 0.9658, 1,
                        cn_chCoef_mho68_v2, adfs_mho68_v2, pga_mho68_v2, 4,
                        4, 2,4, 0.0008,0.0015);
                break;
        }

        return hwConfig;

    }

    /** 单例实例 */
    private static HwConfig instance = null;
    
    /**
     * 获取硬件配置单例实例
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>线程安全的单例获取</li>
     *   <li>首次调用时创建实例</li>
     * </ul>
     * 
     * @return 硬件配置实例
     */
    public static HwConfig getInstance(){
        synchronized (HwConfig.class){
            if(instance ==null){
                instance = allocMHO();
            }
            return instance;
        }
    }
    
    /**
     * 获取ADC驱动实例
     * 
     * @return ADC驱动实例
     */
    public ADC getAdc(){
        return adc;
    }

    /** ADC芯片驱动实例 */
    ADC adc;

    /** ADC位数，默认8位 */
    private int adBits = 8;
    
    /** ADC最大值，默认255（8位） */
    private int adMaxVal = 255;
    
    /** 触发电平因子，默认64 */
    private int triggerLevelFactor =64;
    
    /** 每像素对应的ADC值，默认4 */
    private double adValPerPix = 4;
    
    /** 波形缩放因子，默认1 */
    private double wavFactor = 1;
    
    /** ADC满量程范围，默认0.67V */
    private double adFSR = 0.67;

    /** 触发回差因子，默认1.0 */
    private double triggerHuiChaFactor = 1.0;
    
    /** 默认通道系数数组 */
    private float [] defaultChCoef = cn_chCoef;
    
    /** 默认ADC采样频率数组 */
    private int [] defaultAdFS = adfs_mho68_v2;
    
    /** 默认PGA配置数组 */
    private short [] defaultPga = pga_eto;

    /** Y轴位置因子，默认256 */
    private int yPlaceFactor = 256;

    /** 像素到ADC转换因子，默认1 */
    private int pix2AdFactor = 1;

    /** FPGA数量，默认1 */
    private int fpgaNums = 1;

    /** ADC数量，默认2 */
    private int adcNums = 2;

    /** 1MΩ阻抗下的数字档位值 */
    private double digitDang_1M = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV);
    
    /** 50Ω阻抗下的数字档位值 */
    private double digitDang_50 = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV);

    /** 硬件抽象层实例 */
    private HW hw;
    
    /**
     * 获取硬件抽象层实例
     * 
     * @return HW实例
     */
    public HW getHW(){
        return hw;
    }

    /**
     * 获取数字档位值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据阻抗类型返回对应的数字档位值</li>
     *   <li>用于数字档位的校准和显示</li>
     * </ul>
     * 
     * @param resistanceType 阻抗类型（Channel.RESISTANCE_1M 或 Channel.RESISTANCE_50）
     * @return 对应的数字档位值
     */
    public double getDigitDang(int resistanceType){
        if(resistanceType == Channel.RESISTANCE_1M) {
            return digitDang_1M;
        }else{
            return digitDang_50;
        }
    }

    /**
     * 获取FPGA数量
     * 
     * @return FPGA数量
     */
    public int getFpgaNums(){
        return fpgaNums;
    }
    
    /**
     * 获取ADC数量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回系统中ADC芯片的数量</li>
     *   <li>用于存储深度计算时的通道系数计算</li>
     * </ul>
     * 
     * <p><b>典型值：</b>
     * <ul>
     *   <li>MHO38 V1: 4个ADC</li>
     *   <li>MHO68 V1: 2个ADC</li>
     *   <li>MHO68 V2: 4个ADC</li>
     * </ul>
     * 
     * @return ADC数量
     */
    public int getAdcNums(){
        return adcNums;
    }

    /**
     * 获取ADC最大通道数
     * 
     * @return ADC最大通道数
     */
    public int getAdcMaxChNums(){
        return adc.getMaxChNums();
    }

    /**
     * 获取像素到ADC转换因子
     * 
     * @return 转换因子
     */
    public int getPix2AdFactor(){
        return pix2AdFactor;
    }

    /**
     * 获取Y轴位置因子
     * 
     * @return Y轴位置因子
     */
    public int getYPlaceFactor(){
        return yPlaceFactor;
    }
    
    /**
     * 获取ADC位数
     * 
     * @return ADC位数（如12）
     */
    public int getAdbits(){

        return adBits;
    }

    /**
     * 获取ADC最大值
     * 
     * @return ADC最大值（如1023）
     */
    public int getAdMaxVal(){
        return adMaxVal;
    }

    /**
     * 获取触发电平因子
     * 
     * @return 触发电平因子
     */
    public int getTriggerLevelFactor(){
        return triggerLevelFactor;
    }

    /**
     * 获取每像素对应的ADC值
     * 
     * @return 每像素ADC值
     */
    public double getAdValPerPix(){
        return adValPerPix;
    }
    
    /**
     * 获取波形缩放因子
     * 
     * @return 波形缩放因子
     */
    public double getWavFactor(){
        return wavFactor;
    }
    
    /**
     * 获取ADC满量程范围
     * 
     * @return ADC满量程范围（单位：V）
     */
    public double getAdFSR(){
        return adFSR;
    }
    
    /**
     * 获取触发回差因子
     * 
     * @return 触发回差因子
     */
    public double getTriggerHuiChaFactor(){
       return triggerHuiChaFactor;
    }

    /**
     * 获取默认通道系数数组
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回各垂直档位的偏移校准系数</li>
     *   <li>数组长度为15，对应250uV到10V共15个档位</li>
     * </ul>
     * 
     * @return 通道系数数组
     */
    public float [] getChCoefDefault(){
        return defaultChCoef;
    }
    
    /**
     * 获取默认PGA配置数组
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回各垂直档位的PGA配置值</li>
     *   <li>数组长度为15，对应250uV到10V共15个档位</li>
     * </ul>
     * 
     * @return PGA配置数组
     */
    public short [] getPgaDefault(){
        return defaultPga;
    }
    
    /**
     * 获取默认ADC采样频率数组
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回各垂直档位的ADC采样频率配置</li>
     *   <li>数组长度为15，对应250uV到10V共15个档位</li>
     * </ul>
     * 
     * @return ADC采样频率数组
     */
    public int [] getADFsDefault(){
        return defaultAdFS;
    }

    /**
     * MHO38 V1 ADC采样频率配置数组
     * 
     * <p><b>档位对应：</b>
     * <pre>
     * 索引  档位      配置值    说明
     * ─────────────────────────────────
     * 0     250uV     16384    16K采样
     * 1     500uV     16384    16K采样
     * ...
     * 14    10V       16384    16K采样
     * </pre>
     */
    private static int[] adfs_mho38_v1 ={
            16384,  // 250uV
            16384,  // 500uV
            16384,  // 1mV
            16384,  // 2mV
            16384,  // 5mV
            16384,  // 10mV
            16384,  // 20mV
            16384,  // 50mV
            16384,  // 100mV
            16384,  // 200mV
            16384,  // 500mV
            16384,  // 1V
            16384,  // 2V
            16384,  // 5V
            16384,  // 10V
    };
    
    /**
     * MHO68 V1 ADC采样频率配置数组
     * 
     * <p><b>配置值说明：</b>
     * <ul>
     *   <li>0x800 = 2048，表示2K采样频率配置</li>
     * </ul>
     */
    private static int[] adfs_mho68_v1 ={
            0x800,  // 250uV
            0x800,  // 500uV
            0x800,  // 1mV
            0x800,  // 2mV
            0x800,  // 5mV
            0x800,  // 10mV
            0x800,  // 20mV
            0x800,  // 50mV
            0x800,  // 100mV
            0x800,  // 200mV
            0x800,  // 500mV
            0x800,  // 1V
            0x800,  // 2V
            0x800,  // 5V
            0x800,  // 10V
    };
    
    /**
     * MHO68 V2 ADC采样频率配置数组
     * 
     * <p><b>配置值说明：</b>
     * <ul>
     *   <li>0xA000 = 40960，表示40K采样频率配置</li>
     * </ul>
     */
    private static int[] adfs_mho68_v2 ={
            0xA000,  // 250uV
            0xA000,  // 500uV
            0xA000,  // 1mV
            0xA000,  // 2mV
            0xA000,  // 5mV
            0xA000,  // 10mV
            0xA000,  // 20mV
            0xA000,  // 50mV
            0xA000,  // 100mV
            0xA000,  // 200mV
            0xA000,  // 500mV
            0xA000,  // 1V
            0xA000,  // 2V
            0xA000,  // 5V
            0xA000,  // 10V
    };
    
    /**
     * MHO38 V1 PGA配置数组
     * 
     * <p><b>PGA配置格式：</b>
     * <pre>
     * 16位配置值格式：
     *   高8位：继电器控制位
     *   低8位：PGA增益设置
     * 
     * 示例：
     *   0x400 = 继电器关闭，增益设置0x00
     *   0x407 = 继电器关闭，增益设置0x07
     *   0x41F = 继电器关闭，增益设置0x1F
     * </pre>
     */
    private static short[] pga_mho38_v1 ={
            0x400,  // 250uV
            0x400,  // 500uV
            0x400,  // 1mV
            0x407,  // 2mV
            0x40E,  // 5mV
            0x414,  // 10mV
            0x41B,  // 20mV
            0x405,  // 50mV
            0x40B,  // 100mV
            0x411,  // 200mV
            0x419,  // 500mV
            0x41F,  // 1V
            0x406,  // 2V
            0x404,  // 5V
            0x407,  // 10V
    };
    
    /**
     * MHO68 V1 PGA配置数组
     * 
     * <p><b>PGA配置格式：</b>
     * <pre>
     * 16位配置值格式：
     *   高8位：继电器控制位（0x02表示特定继电器组合）
     *   低8位：PGA增益设置
     * </pre>
     */
    public static short[] pga_mho68_v1 ={
            0x201,  // 250uV
            0x201,  // 500uV
            0x202,  // 1mV
            0x208,  // 2mV
            0x210,  // 5mV
            0x216,  // 10mV
            0x21C,  // 20mV
            0x207,  // 50mV
            0x20D,  // 100mV
            0x213,  // 200mV
            0x21B,  // 500mV
            0x215,  // 1V
            0x21B,  // 2V
            0x217,  // 5V
            0x21D,  // 10V
    };

    /**
     * MHO68 V2 PGA配置数组
     */
    public static short[] pga_mho68_v2 ={
            0x200,  // 250uV
            0x200,  // 500uV
            0x206,  // 1mV
            0x20C,  // 2mV
            0x214,  // 5mV
            0x21A,  // 10mV
            0x220,  // 20mV
            0x209,  // 50mV
            0x20F,  // 100mV
            0x215,  // 200mV
            0x21D,  // 500mV
            0x20C,  // 1V
            0x212,  // 2V
            0x21A,  // 5V
            0x220,  // 10V
    }
    
    /**
     * MHO8 50Ω阻抗 PGA配置数组
     */
    public static short[] pga_mho_8_50O ={
            0x200,  // 250uV
            0x200,  // 500uV
            0x200,  // 1mV
            0x200,  // 2mV
            0x208,  // 5mV
            0x20E,  // 10mV
            0x214,  // 20mV
            0x21C,  // 50mV
            0x208,  // 100mV
            0x20E,  // 200mV
            0x216,  // 500mV
            0x21C,  // 1V
            0x219,  // 2V
            0x215,  // 5V
            0x21B,  // 10V
    };

    /**
     * MHO8 50Ω阻抗 V2版本 PGA配置数组
     */
    public static short[] pga_mho_8_50O_v2 ={
            0x200,  // 250uV
            0x200,  // 500uV
            0x200,  // 1mV
            0x206,  // 2mV
            0x20E,  // 5mV
            0x214,  // 10mV
            0x21A,  // 20mV
            0x214,  // 50mV
            0x21A,  // 100mV
            0x212,  // 200mV
            0x21A,  // 500mV
            0x220,  // 1V
            0x219,  // 2V
            0x215,  // 5V
            0x21B,  // 10V
    };

    /**
     * ETO PGA配置数组（默认配置）
     */
    private static short[] pga_eto ={
            0x410,  // 250uV
            0x410,  // 500uV
            0x411,  // 1mV
            0x414,  // 2mV
            0x418,  // 5mV
            0x401,  // 10mV
            0x404,  // 20mV
            0x408,  // 50mV
            0x405,  // 100mV
            0x408,  // 200mV
            0x401,  // 500mV
            0x404,  // 1V
            0x407,  // 2V
            0x405,  // 5V
            0x408,  // 10V
    };



    /**
     * 默认通道系数数组
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>各垂直档位的偏移校准系数</li>
     *   <li>用于波形显示的Y轴校准</li>
     * </ul>
     */
    private static float[] cn_chCoef ={
            //各档位偏移系数
            0.148120879f,   // 250uV
            0.148120879f,   // 500uV
            0.12648448f,    // 1mV
            0.25296896f,    // 2mV
            0.6324224f,     // 5mV
            1.2648448f,     // 10mV
            2.5296896f,     // 20mV
            6.324224f,      // 50mV
            12.648448f,     // 100mV
            25.296896f,     // 200mV
            1.2648448f,     // 500mV
            2.5296896f,     // 1V
            5.0593792f,     // 2V
            12.648448f,     // 5V
            25.296896f,     // 10V
    };

    /**
     * MHO38 V1 通道系数数组
     */
    private static float[] cn_chCoef_mho38_v1 ={
            //各档位偏移系数
            0.064633569f, // 250uV
            0.064633569f, // 500uV
            0.129267139f, // 1mV
            0.258534277f, // 2mV
            0.646335693f, // 5mV
            1.292671386f, // 10mV
            2.585342771f, // 20mV
            6.463356928f, // 50mV
            12.92671386f, // 100mV
            25.85342771f, // 200mV
            1.292671386f, // 500mV
            2.585342771f, // 1V
            5.170685542f, // 2V
            12.92671386f, // 5V
            25.85342771f, // 10V
    }
    
    /**
     * MHO68 V1 通道系数数组
     */
    private static float[] cn_chCoef_mho68_v1 ={
            //各档位偏移系数
            0.063991399f, // 250uV
            0.063991399f, // 500uV
            0.127982797f, // 1mV
            0.255965595f, // 2mV
            0.639913987f, // 5mV
            1.279827974f, // 10mV
            2.559655948f, // 20mV
            0.4186112f, // 50mV
            0.8372224f, // 100mV
            1.6744448f, // 200mV
            4.186112f, // 500mV
            2.093056f, // 1V
            4.186112f, // 2V
            2.61632f, // 5V
            5.23264f, // 10V
    }
    
    /**
     * MHO68 V2 通道系数数组
     */
    public static float[] cn_chCoef_mho68_v2 ={
            //各档位偏移系数
            0.476250112f, // 250uV
            0.476250112f, // 500uV
            0.952500224f, // 1mV
            1.905000448f, // 2mV
            4.76250112f,  // 5mV
            9.52500224f,  // 10mV
            19.05000448f, // 20mV
            1.422374851f, // 50mV
            2.844749702f, // 100mV
            5.689499404f, // 200mV
            14.22374851f, // 500mV
            1.935654057f, // 1V
            3.871308114f, // 2V
            9.678270285f, // 5V
            19.35654057f, // 10V
    }
    
    /**
     * MHO8 50Ω V2 通道系数数组
     */
    public static float[] cn_chCoef_mho_8_50O_v2 ={
            //各档位偏移系数
            0.476250112f, // 250uV
            0.27496274f,  // 500uV
            0.54992548f,  // 1mV
            1.099850961f,  // 2mV
            2.749627402f,  // 5mV
            5.499106426f,  // 10mV
            10.99821285f,  // 20mV
            5.426446733f,  // 50mV
            10.85289347f,  // 100mV
            4.065862889f,  // 200mV
            10.16465722f,  // 500mV
            20.32931444f,  // 1V
            3.871308114f, // 2V
            9.678270285f, // 5V
            19.35654057f, // 10V
    }
    
    /**
     * 获取最大ADC输入时钟频率
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取当前ADC芯片的最大采样时钟频率</li>
     *   <li>用于存储深度自动计算</li>
     * </ul>
     * 
     * <p><b>典型值：</b>
     * <ul>
     *   <li>MXT2022: 2GHz</li>
     *   <li>EV12AQ60x: 1.6GHz</li>
     *   <li>B12DJ3200NBB: 3.2GHz</li>
     *   <li>CAE2400: 2.4GHz</li>
     * </ul>
     * 
     * @return 最大ADC时钟频率（单位：MHz）
     */
    public static long getMaxAdInClk(){
        return HwConfig.getInstance().adc.getMaxAdInClk();
    }
}
