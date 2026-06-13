package com.micsig.tbook.hardware; // 硬件层包名，包含示波器硬件相关操作类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────────────────┐
 * │                                   HwServiceName                                         │
 * │                               硬件服务名称常量定义类                                       │
 * ├─────────────────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                             │
 * │   硬件抽象层基础 - 系统服务名称常量容器，提供统一的服务标识符                                │
 * │                                                                                         │
 * │ 【核心职责】                                                                             │
 * │   1. 定义硬件相关系统服务的名称常量                                                       │
 * │   2. 提供类型安全的服务标识符访问                                                         │
 * │   3. 集中管理服务名称，便于维护和修改                                                     │
 * │                                                                                         │
 * │ 【架构设计】                                                                             │
 * │   ┌───────────────────────────────────────────────────────────────────┐                 │
 * │   │                     HwServiceName (常量容器)                      │                 │
 * │   │  ┌─────────────────────────────────────────────────────────────┐  │                 │
 * │   │  │                    服务名称常量定义                          │  │                 │
 * │   │  │  - SPI_SERVICE    → SPI总线服务                             │  │                 │
 * │   │  │  - SERIAL_SERVICE → 串口通信服务                            │  │                 │
 * │   │  │  - OTHER_SERVICE  → 其他硬件服务                            │  │                 │
 * │   │  │  - GPIO_SERVICE   → GPIO控制服务                            │  │                 │
 * │   │  └─────────────────────────────────────────────────────────────┘  │                 │
 * │   └───────────────────────────────────────────────────────────────────┘                 │
 * │                                                                                         │
 * │ 【服务映射】                                                                             │
 * │   ┌────────────────┬─────────────────────────────────────────────────┐                 │
 * │   │     常量名      │                  服务说明                        │                 │
 * │   ├────────────────┼─────────────────────────────────────────────────┤                 │
 * │   │ SPI_SERVICE    │ SPI总线服务，用于SPI设备通信                      │                 │
 * │   │ SERIAL_SERVICE │ 串口服务，用于UART串口通信                        │                 │
 * │   │ OTHER_SERVICE  │ 其他硬件服务，包含EEPROM/温度/风扇等              │                 │
 * │   │ GPIO_SERVICE   │ GPIO服务，用于GPIO引脚控制                       │                 │
 * │   └────────────────┴─────────────────────────────────────────────────┘                 │
 * │                                                                                         │
 * │ 【依赖关系】                                                                             │
 * │   被以下类使用：                                                                         │
 * │   - HwManager: 使用OTHER_SERVICE获取OtherManager                                        │
 * │   - SpiDevManager: 使用SPI_SERVICE获取SpiManager                                        │
 * │   - HwGpioManager: 使用GPIO_SERVICE获取GpioManager                                      │
 * │                                                                                         │
 * │ 【使用示例】                                                                             │
 * │   // 获取SPI管理器                                                                      │
 * │   SpiManager spiManager = (SpiManager) context.getSystemService(                        │
 * │       HwServiceName.SPI_SERVICE);                                                       │
 * │                                                                                         │
 * │   // 获取其他硬件管理器                                                                  │
 * │   OtherManager otherManager = (OtherManager) context.getSystemService(                  │
 * │       HwServiceName.OTHER_SERVICE);                                                     │
 * │                                                                                         │
 * │ 【设计模式】                                                                             │
 * │   常量类模式 - 私有构造函数防止实例化，所有成员为静态常量                                  │
 * │                                                                                         │
 * │ 【注意事项】                                                                             │
 * │   - 服务名称需与Android系统服务注册名称一致                                              │
 * │   - 修改服务名称需同步更新系统服务注册代码                                                │
 * │   - 此类不可被实例化                                                                     │
 * │                                                                                         │
 * │ 【作者】zhuzh                                                                            │
 * │ 【日期】2018/3/12                                                                        │
 * └─────────────────────────────────────────────────────────────────────────────────────────┘
 */

public class HwServiceName {

    // ==================== 构造方法 ====================

    /**
     * 私有构造函数 - 防止类被实例化
     * 
     * 【功能说明】
     *   此类为常量容器，不应当被实例化
     *   私有构造函数确保外部无法创建实例
     */
    private HwServiceName(){ // 私有构造函数，防止实例化

    } // 构造函数结束

    // ==================== 服务名称常量定义 ====================

    /**
     * SPI总线服务名称
     * 
     * 【服务说明】
     *   SPI（Serial Peripheral Interface）总线服务
     *   用于管理SPI设备通信，支持全双工同步串行通信
     * 
     * 【使用场景】
     *   - FPGA配置数据传输
     *   - ADC/DAC芯片通信
     *   - Flash存储器读写
     *   - 其他SPI外设通信
     * 
     * 【获取方式】
     *   SpiManager spiManager = (SpiManager) context.getSystemService(SPI_SERVICE);
     */
    public static final String SPI_SERVICE = "spi"; // SPI总线服务名称常量

    /**
     * 串口通信服务名称
     * 
     * 【服务说明】
     *   串口（UART）通信服务
     *   用于管理串口设备通信，支持异步串行通信
     * 
     * 【使用场景】
     *   - 调试串口输出
     *   - 外部设备通信
     *   - 数据采集传输
     * 
     * 【获取方式】
     *   SerialManager serialManager = (SerialManager) context.getSystemService(SERIAL_SERVICE);
     */
    public static final String SERIAL_SERVICE = "serial"; // 串口通信服务名称常量

    /**
     * 其他硬件服务名称
     * 
     * 【服务说明】
     *   其他硬件管理服务（OtherManager）
     *   提供EEPROM、温度监控、风扇控制、ADC采集等综合硬件功能
     * 
     * 【使用场景】
     *   - EEPROM读写操作
     *   - 系统温度和CPU温度获取
     *   - 风扇转速控制
     *   - 设备UUID获取
     *   - USB信息配置
     *   - 通道探头ADC值读取
     * 
     * 【获取方式】
     *   OtherManager otherManager = (OtherManager) context.getSystemService(OTHER_SERVICE);
     */
    public static final String OTHER_SERVICE = "other"; // 其他硬件服务名称常量

    /**
     * GPIO控制服务名称
     * 
     * 【服务说明】
     *   GPIO（General Purpose Input/Output）控制服务
     *   用于管理通用输入输出引脚
     * 
     * 【使用场景】
     *   - FPGA配置控制信号（nCONFIG, nSTATUS, CONFIG_DONE）
     *   - FPGA电源控制
     *   - LED指示灯控制
     *   - 按键输入检测
     *   - 其他GPIO控制需求
     * 
     * 【获取方式】
     *   GpioManager gpioManager = (GpioManager) context.getSystemService(GPIO_SERVICE);
     */
    public static final String GPIO_SERVICE = "gpio"; // GPIO控制服务名称常量

} // HwServiceName类结束
