package com.micsig.tbook.hardware; // 包声明：硬件GPIO管理器类所属包路径

import android.annotation.SuppressLint; // 导入SuppressLint注解，用于抑制lint警告
import android.content.Context; // 导入Android上下文类
import android.hardware.GpioManager; // 导入Android GPIO管理器类

import androidx.annotation.IntDef; // 导入IntDef注解，用于定义整型常量集合

import java.lang.annotation.Retention; // 导入Retention注解
import java.lang.annotation.RetentionPolicy; // 导入RetentionPolicy枚举

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                       HwGpioManager 类说明文档                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   硬件GPIO管理器类 - MHO系列示波器硬件层的GPIO引脚管理组件                    │
 * │   采用单例模式，负责所有GPIO引脚的初始化和管理                                │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. GPIO引脚定义：定义所有GPIO引脚编号常量                                  │
 * │   2. GPIO初始化：初始化所有GPIO引脚的方向、上下拉和初始值                     │
 * │   3. GPIO管理：提供获取GPIO设备实例的方法                                    │
 * │   4. 待机控制：提供待机模式控制方法                                          │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                       HwGpioManager                             │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    GPIO引脚常量层                          │   │       │
 * │   │  │  PIN_FPGA_nCONFIG │ PIN_FPGA_CONFIG_DONE │ ...            │   │       │
 * │   │  │  (共31个GPIO引脚定义)                                     │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    GPIO设备管理层                          │   │       │
 * │   │  │  mGpioDev[] (GPIO设备数组) │ mGpioManager (Android GPIO)   │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    GPIO初始化层                            │   │       │
 * │   │  │  init3588Gpio() (初始化RK3588平台的GPIO)                  │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    GPIO访问层                              │   │       │
 * │   │  │  getGpioDev() (获取GPIO设备实例)                          │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【单例模式】                                                                 │
 * │   采用双重检查锁定（Double-Check Locking）实现线程安全的单例模式              │
 * │   - volatile关键字：保证instance变量的可见性                                 │
 * │   - synchronized块：保证线程安全                                            │
 * │   - 双重检查：避免不必要的同步开销                                           │
 * │                                                                             │
 * │ 【GPIO引脚分类】                                                             │
 * │   1. FPGA控制引脚（5个）：                                                   │
 * │      - PIN_FPGA_nCONFIG：FPGA配置启动引脚                                   │
 * │      - PIN_FPGA_CONFIG_DONE：FPGA配置完成引脚                               │
 * │      - PIN_FPGA_nSTATUS：FPGA状态引脚                                       │
 * │      - PIN_FPGA_nRST：FPGA复位引脚                                          │
 * │      - PIN_FPGA_ACK12：FPGA应答引脚                                         │
 * │   2. 电源控制引脚（8个）：                                                   │
 * │      - PIN_POWER_ANALOG：模拟电源控制                                        │
 * │      - PIN_POWER_ANALOG58：5-8通道模拟电源控制                              │
 * │      - PIN_POWER_ADC：ADC电源控制                                           │
 * │      - PIN_ADC2V5_EN：ADC 2.5V电源使能                                      │
 * │      - PIN_POWER_PANEL：面板电源控制                                        │
 * │      - PIN_POWER58_PANEL：5-8通道面板电源控制                               │
 * │      - PIN_PROBE_EN：探头电源使能                                           │
 * │      - PIN_PROBE58_EN：5-8通道探头电源使能                                  │
 * │   3. FPGA电源控制引脚（5个）：                                               │
 * │      - FPGA_VCC1V0：FPGA 1.0V电源                                          │
 * │      - FPGA_VCC1V2：FPGA 1.2V电源                                          │
 * │      - FPGA_VCC1V8：FPGA 1.8V电源                                          │
 * │      - FPGA_VCC3V3：FPGA 3.3V电源                                          │
 * │      - FPGA_VCC3V3_EXT：FPGA外部3.3V电源                                   │
 * │   4. 通道模型控制引脚（5个）：                                               │
 * │      - PIN_CH_MODEL_OE：输出使能                                            │
 * │      - PIN_CH_MODEL_DS：数据选择                                            │
 * │      - PIN_CH_MODEL_SRCLR：移位寄存器清零                                   │
 * │      - PIN_CH_MODEL_SRCLK：移位寄存器时钟                                   │
 * │      - PIN_CH_MODEL_RCLK：锁存时钟                                          │
 * │   5. 时钟控制引脚（2个）：                                                   │
 * │      - PIN_CLK_PLL_LE：PLL时钟锁存使能                                      │
 * │      - PIN_CLK_PLL_CE：PLL时钟芯片使能                                      │
 * │   6. 探头控制引脚（2个）：                                                   │
 * │      - PIN_PROBE_IO：探头IO控制                                             │
 * │      - PIN_PROBE58_IO：5-8通道探头IO控制                                    │
 * │   7. 触发和时钟控制引脚（2个）：                                             │
 * │      - PIN_TRIG_CTL：触发控制                                               │
 * │      - PIN_10M_CLK_CTL：10MHz时钟控制                                       │
 * │   8. FPGA唤醒引脚（2个）：                                                   │
 * │      - PIN_FPGA_AWAKE：FPGA唤醒引脚1                                        │
 * │      - PIN_FPGA_AWAKE2：FPGA唤醒引脚2                                       │
 * │   9. 硬件版本引脚（8个）：                                                   │
 * │      - PIN_VERSION_0~7：硬件版本位0-7                                       │
 * │   10. 其他引脚（1个）：                                                      │
 * │      - PIN_50V_PWR：50V电源控制                                             │
 * │                                                                             │
 * │ 【GPIO初始化流程】                                                           │
 * │   1. 获取Android GPIO管理器服务                                             │
 * │   2. 调用init3588Gpio()初始化所有GPIO引脚                                   │
 * │   3. 根据产品型号配置不同的GPIO引脚映射                                      │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   上游依赖：Context（Android上下文）                                        │
 * │   下游依赖：GpioManager（Android GPIO服务）、GpioDev（GPIO设备类）、         │
 * │            HardwareProduct（硬件产品型号类）                                │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 获取HwGpioManager单例                                                  │
 * │   HwGpioManager gpioManager = HwGpioManager.getInstance(context);           │
 * │   // 获取GPIO设备实例                                                        │
 * │   GpioDev gpio = gpioManager.getGpioDev(HwGpioManager.PIN_FPGA_nCONFIG);   │
 * │   // 设置GPIO值                                                              │
 * │   gpio.setVal(GpioDev.GPIO_VAL_HIGH);                                      │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. HwGpioManager是单例类，全局只有一个实例                                 │
 * │   2. GPIO引脚编号从0开始，最大为PIN_MAX-1                                   │
 * │   3. 不同产品型号的GPIO引脚映射可能不同                                      │
 * │   4. 使用注解可以确保编译时类型安全                                          │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018/3/9                                               │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 硬件GPIO管理器类
 * <p>
 * 采用单例模式，负责所有GPIO引脚的初始化和管理。
 * 定义了所有GPIO引脚编号常量，并提供获取GPIO设备实例的方法。
 * <p>
 * 核心功能：
 * - GPIO引脚定义
 * - GPIO初始化
 * - GPIO管理
 * - 待机控制
 */
public class HwGpioManager {

    /**
     * 日志标签
     * 用途：用于日志输出时的标识
     */
    private static final String TAG = "HwGpioManager";

    /**
     * Android上下文
     * 用途：访问Android系统服务
     */
    private Context mContext;

    /**
     * Android GPIO管理器
     * 用途：访问Android GPIO硬件服务
     */
    private GpioManager mGpioManager;

    /**
     * GPIO引脚注解
     * 用途：定义所有有效的GPIO引脚编号，用于类型安全检查
     */
    @IntDef({PIN_FPGA_nCONFIG, // FPGA配置启动引脚
            PIN_FPGA_CONFIG_DONE,PIN_FPGA_nSTATUS,PIN_FPGA_nRST,PIN_FPGA_ACK12, // FPGA控制引脚
            PIN_POWER_ANALOG,PIN_CH_MODEL_OE,PIN_CH_MODEL_DS,PIN_CH_MODEL_SRCLR, // 电源和通道模型控制引脚
            PIN_CH_MODEL_SRCLK,PIN_CH_MODEL_RCLK,PIN_FPGA_SUSPEND, // 通道模型和FPGA控制引脚
            PIN_FPGA_AWAKE,PIN_POWER_ADC,PIN_POWER_PANEL,PIN_CLK_PLL_LE,PIN_CLK_PLL_CE,PIN_PROBE_EN, // 唤醒、电源、时钟、探头引脚
            FPGA_VCC1V0,FPGA_VCC1V2,FPGA_VCC1V8,FPGA_VCC3V3,PIN_PROBE_IO,PIN_POWER_ANALOG58, // FPGA电源和探头引脚
            PIN_FPGA_AWAKE2,PIN_ADC2V5_EN,PIN_POWER58_PANEL,PIN_PROBE58_EN,PIN_PROBE58_IO, // 唤醒、电源、探头引脚
            PIN_TRIG_CTL,PIN_10M_CLK_CTL,PIN_50V_PWR // 触发、时钟、电源引脚

    })
    @Retention(RetentionPolicy.SOURCE) // 注解仅在源码级别保留
    public @interface GPIO_PIN {} // GPIO引脚注解定义

    // ==================== GPIO引脚编号常量定义 ====================
    
    /**
     * FPGA配置启动引脚
     * 用途：启动FPGA配置过程
     * 低电平有效：拉低启动配置
     */
    public static final int PIN_FPGA_nCONFIG = 0;

    /**
     * FPGA配置完成引脚
     * 用途：指示FPGA配置完成
     * 高电平有效：配置完成后输出高电平
     */
    public static final int PIN_FPGA_CONFIG_DONE = PIN_FPGA_nCONFIG + 1;

    /**
     * FPGA状态引脚
     * 用途：指示FPGA状态
     * 低电平有效：配置错误时拉低
     */
    public static final int PIN_FPGA_nSTATUS = PIN_FPGA_CONFIG_DONE + 1;

    /**
     * FPGA复位引脚
     * 用途：复位FPGA
     * 低电平有效：拉低复位FPGA
     */
    public static final int PIN_FPGA_nRST = PIN_FPGA_nSTATUS + 1;

    /**
     * FPGA应答引脚
     * 用途：FPGA应答信号
     */
    public static final int PIN_FPGA_ACK12 = PIN_FPGA_nRST + 1;

    /**
     * 模拟电源控制引脚
     * 用途：控制模拟电路电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int PIN_POWER_ANALOG = PIN_FPGA_ACK12 + 1;

    /**
     * 5-8通道模拟电源控制引脚
     * 用途：控制5-8通道模拟电路电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int PIN_POWER_ANALOG58 = PIN_POWER_ANALOG + 1;

    /**
     * 通道模型输出使能引脚
     * 用途：控制通道模型输出使能
     * 低电平：使能输出
     * 高电平：禁用输出
     */
    public static final int PIN_CH_MODEL_OE = PIN_POWER_ANALOG58 + 1;

    /**
     * 通道模型数据选择引脚
     * 用途：控制移位寄存器数据选择
     */
    public static final int PIN_CH_MODEL_DS = PIN_CH_MODEL_OE + 1;

    /**
     * 通道模型移位寄存器清零引脚
     * 用途：清零移位寄存器
     * 低电平有效：拉低清零
     */
    public static final int PIN_CH_MODEL_SRCLR = PIN_CH_MODEL_DS + 1;

    /**
     * 通道模型移位寄存器时钟引脚
     * 用途：移位寄存器时钟信号
     */
    public static final int PIN_CH_MODEL_SRCLK = PIN_CH_MODEL_SRCLR + 1;

    /**
     * 通道模型锁存时钟引脚
     * 用途：锁存移位寄存器数据
     */
    public static final int PIN_CH_MODEL_RCLK = PIN_CH_MODEL_SRCLK + 1;

    /**
     * FPGA休眠控制引脚
     * 用途：控制FPGA进入休眠模式
     * 高电平：休眠
     * 低电平：唤醒
     */
    public static final int PIN_FPGA_SUSPEND = PIN_CH_MODEL_RCLK + 1;

    /**
     * FPGA唤醒引脚1
     * 用途：FPGA唤醒信号输入
     */
    public static final int PIN_FPGA_AWAKE = PIN_FPGA_SUSPEND + 1;

    /**
     * FPGA唤醒引脚2
     * 用途：FPGA唤醒信号输入
     */
    public static final int PIN_FPGA_AWAKE2 = PIN_FPGA_AWAKE + 1;

    /**
     * ADC 2.5V电源使能引脚
     * 用途：控制ADC 2.5V电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int PIN_ADC2V5_EN = PIN_FPGA_AWAKE2 + 1;

    /**
     * ADC电源控制引脚
     * 用途：控制ADC电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int PIN_POWER_ADC = PIN_ADC2V5_EN + 1;

    /**
     * 硬件版本位0
     * 用途：读取硬件版本位0
     */
    public static final int PIN_VERSION_0 = PIN_POWER_ADC + 1;

    /**
     * 硬件版本位1
     * 用途：读取硬件版本位1
     */
    public static final int PIN_VERSION_1 = PIN_VERSION_0 + 1;

    /**
     * 硬件版本位2
     * 用途：读取硬件版本位2
     */
    public static final int PIN_VERSION_2 = PIN_VERSION_1 + 1;

    /**
     * 硬件版本位3
     * 用途：读取硬件版本位3
     */
    public static final int PIN_VERSION_3 = PIN_VERSION_2 + 1;

    /**
     * 硬件版本位4
     * 用途：读取硬件版本位4
     */
    public static final int PIN_VERSION_4 = PIN_VERSION_3 + 1;

    /**
     * 硬件版本位5
     * 用途：读取硬件版本位5
     */
    public static final int PIN_VERSION_5 = PIN_VERSION_4 + 1;

    /**
     * 硬件版本位6
     * 用途：读取硬件版本位6
     */
    public static final int PIN_VERSION_6 = PIN_VERSION_5 + 1;

    /**
     * 硬件版本位7
     * 用途：读取硬件版本位7
     */
    public static final int PIN_VERSION_7 = PIN_VERSION_6 + 1;

    /**
     * PLL时钟锁存使能引脚
     * 用途：控制PLL时钟芯片锁存使能
     * 上升沿有效：锁存SPI数据
     */
    public static final int PIN_CLK_PLL_LE = PIN_VERSION_7 + 1;

    /**
     * PLL时钟芯片使能引脚
     * 用途：控制PLL时钟芯片使能
     * 高电平：使能
     * 低电平：禁用
     */
    public static final int PIN_CLK_PLL_CE = PIN_CLK_PLL_LE + 1;

    /**
     * FPGA 1.0V电源控制引脚
     * 用途：控制FPGA 1.0V电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int FPGA_VCC1V0 = PIN_CLK_PLL_CE + 1;

    /**
     * FPGA 1.2V电源控制引脚
     * 用途：控制FPGA 1.2V电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int FPGA_VCC1V2 = FPGA_VCC1V0 + 1;

    /**
     * FPGA 1.8V电源控制引脚
     * 用途：控制FPGA 1.8V电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int FPGA_VCC1V8 = FPGA_VCC1V2 + 1;

    /**
     * FPGA 3.3V电源控制引脚
     * 用途：控制FPGA 3.3V电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int FPGA_VCC3V3 = FPGA_VCC1V8 + 1;

    /**
     * FPGA外部3.3V电源控制引脚
     * 用途：控制FPGA外部3.3V电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int FPGA_VCC3V3_EXT = FPGA_VCC3V3 + 1;

    /**
     * 面板电源控制引脚
     * 用途：控制前面板电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int PIN_POWER_PANEL = FPGA_VCC3V3_EXT + 1;

    /**
     * 5-8通道面板电源控制引脚
     * 用途：控制5-8通道前面板电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int PIN_POWER58_PANEL = PIN_POWER_PANEL + 1;

    /**
     * 探头电源使能引脚
     * 用途：控制有源探头电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int PIN_PROBE_EN = PIN_POWER58_PANEL + 1;

    /**
     * 5-8通道探头电源使能引脚
     * 用途：控制5-8通道有源探头电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int PIN_PROBE58_EN = PIN_PROBE_EN + 1;

    /**
     * 探头IO控制引脚
     * 用途：控制探头IO信号
     */
    public static final int PIN_PROBE_IO = PIN_PROBE58_EN + 1;

    /**
     * 5-8通道探头IO控制引脚
     * 用途：控制5-8通道探头IO信号
     */
    public static final int PIN_PROBE58_IO = PIN_PROBE_IO + 1;

    /**
     * 触发控制引脚
     * 用途：控制触发信号输入输出方向
     * 高电平：输入
     * 低电平：输出
     */
    public static final int PIN_TRIG_CTL = PIN_PROBE58_IO + 1;

    /**
     * 10MHz时钟控制引脚
     * 用途：控制10MHz时钟信号输入输出方向
     * 高电平：输出
     * 低电平：输入
     */
    public static final int PIN_10M_CLK_CTL = PIN_TRIG_CTL + 1;

    /**
     * 50V电源控制引脚
     * 用途：控制50V电源
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    public static final int PIN_50V_PWR = PIN_10M_CLK_CTL + 1;

    /**
     * GPIO引脚最大数量
     * 用途：定义GPIO设备数组长度
     */
    private static final int PIN_MAX = PIN_50V_PWR + 1;

    // ==================== 成员变量定义 ====================
    
    /**
     * GPIO设备数组
     * 长度：PIN_MAX
     * 用途：存储所有GPIO设备实例
     */
    private GpioDev mGpioDev[] = new GpioDev[PIN_MAX];

    /**
     * HwGpioManager单例实例
     * volatile关键字：保证多线程环境下的可见性
     */
    private static volatile HwGpioManager instance = null;

    // ==================== 单例模式实现 ====================
    
    /**
     * 获取HwGpioManager单例实例（无参数）
     * <p>
     * 功能：返回已创建的HwGpioManager实例
     * 说明：如果实例未创建，返回null
     * 
     * @return HwGpioManager实例，如果未创建则返回null
     */
    public static HwGpioManager getInstance(){
        return instance; // 返回单例实例
    }

    /**
     * 获取HwGpioManager单例实例（带Context参数）
     * <p>
     * 功能：创建或返回HwGpioManager单例实例
     * 实现：双重检查锁定（Double-Check Locking）保证线程安全
     * 
     * @param context Android上下文
     * @return HwGpioManager实例
     */
    public static HwGpioManager getInstance(Context context) {
        // 第一次检查：避免不必要的同步
        if (instance == null) {
            synchronized (SpiDevManager.class) { // 同步锁（注意：这里使用了SpiDevManager.class作为锁）
                // 第二次检查：确保只创建一个实例
                if (instance == null && context != null) {
                    instance = new HwGpioManager(context); // 创建单例实例
                }
            }
        }
        return instance; // 返回单例实例
    }

    /**
     * 硬件版本号
     * 用途：存储硬件版本号
     */
    private int hwVersion = 0;

    /**
     * 私有构造函数
     * <p>
     * 功能：创建HwGpioManager实例，初始化GPIO管理器和所有GPIO引脚
     * 说明：私有构造函数，防止外部直接创建实例
     * 
     * @param context Android上下文
     */
    @SuppressLint("WrongConstant") // 抑制lint警告：使用自定义服务名称
    private HwGpioManager(Context context){
        mContext = context; // 保存Android上下文
        hwVersion = 0; // 初始化硬件版本号
        mGpioManager = (GpioManager)mContext.getSystemService(HwServiceName.GPIO_SERVICE); // 获取GPIO管理器服务

        init3588Gpio(); // 初始化RK3588平台的GPIO引脚

    };

    /**
     * 初始化RK3588平台的GPIO引脚
     * <p>
     * 功能：初始化所有GPIO引脚的方向、上下拉和初始值
     * 说明：根据产品型号配置不同的GPIO引脚映射
     */
    private void init3588Gpio(){
        // 初始化FPGA控制引脚
        mGpioDev[PIN_FPGA_nCONFIG] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D6),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // FPGA配置启动引脚，输出高电平
        mGpioDev[PIN_FPGA_CONFIG_DONE] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_B0),GpioDev.GPIO_DIRECTION_IN,GpioDev.GPIO_PULL_UP); // FPGA配置完成引脚，输入，上拉
        mGpioDev[PIN_FPGA_nSTATUS] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D7),GpioDev.GPIO_DIRECTION_IN,GpioDev.GPIO_PULL_UP); // FPGA状态引脚，输入，上拉
        mGpioDev[PIN_FPGA_nRST] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A3),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // FPGA复位引脚，输出高电平
        
        // 初始化电源控制引脚
        mGpioDev[PIN_POWER_ANALOG] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D0),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // 模拟电源，输出高电平
        mGpioDev[PIN_POWER_ANALOG58] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D2),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // 5-8通道模拟电源，输出高电平

        // 初始化通道模型控制引脚
        mGpioDev[PIN_CH_MODEL_OE] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_B2),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE); // 输出使能，输出低电平
        mGpioDev[PIN_CH_MODEL_DS] =  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_B6),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE); // 数据选择，输出低电平
        mGpioDev[PIN_CH_MODEL_SRCLR]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_B7),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE); // 移位寄存器清零，输出低电平
        mGpioDev[PIN_CH_MODEL_SRCLK]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_D1),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE); // 移位寄存器时钟，输出低电平
        mGpioDev[PIN_CH_MODEL_RCLK]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_D2),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE); // 锁存时钟，输出低电平


        // 初始化FPGA唤醒引脚
        mGpioDev[PIN_FPGA_AWAKE]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A4),GpioDev.GPIO_DIRECTION_IN,GpioDev.GPIO_PULL_DISABLE); // FPGA唤醒引脚1，输入
        mGpioDev[PIN_FPGA_AWAKE2]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A5),GpioDev.GPIO_DIRECTION_IN,GpioDev.GPIO_PULL_DISABLE); // FPGA唤醒引脚2，输入
        
        // 初始化面板电源控制引脚
        mGpioDev[PIN_POWER_PANEL]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D1),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // 面板电源，输出高电平
        mGpioDev[PIN_POWER58_PANEL]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D3),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // 5-8通道面板电源，输出高电平


        // 初始化FPGA电源控制引脚
        mGpioDev[FPGA_VCC1V0] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO2_B6),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // FPGA 1.0V电源，输出高电平
        // 根据产品型号配置FPGA 1.2V电源引脚
        if(HardwareProduct.isMHO28V1()){
            mGpioDev[FPGA_VCC1V2] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A7), GpioDev.GPIO_DIRECTION_OUT_HIGH, GpioDev.GPIO_PULL_DISABLE); // MHO28 V1的FPGA 1.2V电源引脚
        }else {
            mGpioDev[FPGA_VCC1V2] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO2_B7), GpioDev.GPIO_DIRECTION_OUT_HIGH, GpioDev.GPIO_PULL_DISABLE); // 其他型号的FPGA 1.2V电源引脚
        }
        mGpioDev[FPGA_VCC1V8] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO2_C0),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // FPGA 1.8V电源，输出高电平
        // 根据产品型号配置FPGA 3.3V电源引脚
        if(HardwareProduct.isMHO28V1()){
            mGpioDev[FPGA_VCC3V3] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_A5),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // MHO28 V1的FPGA 3.3V电源引脚
        }else {
            mGpioDev[FPGA_VCC3V3] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO2_C1),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // 其他型号的FPGA 3.3V电源引脚
        }

        mGpioDev[FPGA_VCC3V3_EXT] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_B7),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // FPGA外部3.3V电源，输出高电平

        mGpioDev[PIN_ADC2V5_EN] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_D0), GpioDev.GPIO_DIRECTION_OUT_HIGH, GpioDev.GPIO_PULL_DISABLE); // ADC 2.5V电源使能，输出高电平


        mGpioDev[PIN_POWER_ADC] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_D1), GpioDev.GPIO_DIRECTION_OUT_HIGH, GpioDev.GPIO_PULL_DISABLE); // ADC电源，输出高电平


        // 初始化探头电源控制引脚
        mGpioDev[PIN_PROBE_EN] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_D5), GpioDev.GPIO_DIRECTION_OUT_LOW, GpioDev.GPIO_PULL_DISABLE); // 探头电源使能，输出低电平

        // 根据产品型号配置5-8通道探头电源使能引脚
        if(!HardwareProduct.isMHO28V1()) {
            mGpioDev[PIN_PROBE58_EN] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A7), GpioDev.GPIO_DIRECTION_OUT_LOW, GpioDev.GPIO_PULL_DISABLE); // 5-8通道探头电源使能，输出低电平
        }

        // 初始化外部探头供电引脚
        mGpioDev[PIN_PROBE_IO] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO0_A4),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // 探头IO，输出高电平
        mGpioDev[PIN_PROBE58_IO] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO0_B2),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // 5-8通道探头IO，输出高电平

        // 初始化触发和时钟控制引脚
        mGpioDev[PIN_TRIG_CTL] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_C4),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE); // 触发控制，输出低电平
        mGpioDev[PIN_10M_CLK_CTL] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_C6),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE); // 10MHz时钟控制，输出低电平
        mGpioDev[PIN_50V_PWR] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D5),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE); // 50V电源，输出高电平

    }

    // ==================== GPIO访问方法 ====================
    
    /**
     * 获取GPIO设备实例
     * <p>
     * 功能：根据引脚编号获取对应的GPIO设备实例
     * 
     * @param pin GPIO引脚编号（使用GPIO_PIN注解确保类型安全）
     * @return GPIO设备实例
     */
    public GpioDev getGpioDev(@GPIO_PIN int pin){

        return mGpioDev[pin]; // 返回指定引脚的GPIO设备实例
    }

    /**
     * 获取硬件版本号
     * <p>
     * 功能：获取硬件版本号
     * 
     * @return 硬件版本号
     */
    public int getHWVersion(){
        return hwVersion; // 返回硬件版本号
    }


    /**
     * 进入待机模式
     * <p>
     * 功能：使硬件进入待机模式
     * 说明：当前方法为空，待实现
     */
    public void standby(){

    }
}
