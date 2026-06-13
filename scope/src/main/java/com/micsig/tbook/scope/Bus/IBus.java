package com.micsig.tbook.scope.Bus; // 包声明：总线基类所属包路径

import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类，用于获取通道常量

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                           IBus 类说明文档                                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   总线抽象基类 - MHO系列示波器串行总线解码系统的核心基础组件                    │
 * │   为所有具体总线类型（UART、LIN、CAN、SPI、I2C、ARINC429、MIL-STD-1553B、     │
 * │   CAN-FD）提供统一的接口定义和通用功能实现                                     │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 总线类型定义：定义8种串行总线类型常量                                     │
 * │      - UART：通用异步收发传输器                                              │
 * │      - LIN：局域互联网络                                                     │
 * │      - CAN：控制器局域网                                                     │
 * │      - SPI：串行外设接口                                                     │
 * │      - I2C：两线式串行总线                                                   │
 * │      - ARINC429：航空电子数字数据总线                                        │
 * │      - MILSTD1553B：军用标准1553B总线                                       │
 * │      - CAN_FD：CAN with Flexible Data-Rate                                  │
 * │   2. 常量定义：定义空闲电平、显示模式、触发关系等通用常量                       │
 * │   3. 变更通知：通过BusAction对象通知配置变更                                  │
 * │   4. 抽象接口：定义子类必须实现的抽象方法                                      │
 * │   5. 启用状态管理：静态管理各总线类型的启用状态                                │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                        IBus (抽象基类)                           │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    常量定义层                              │   │       │
 * │   │  │  总线类型 │ 空闲电平 │ 显示模式 │ 触发关系                │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    成员变量层                              │   │       │
 * │   │  │  busType │ busIdx │ busAction │ busEnable[]              │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    抽象方法层                              │   │       │
 * │   │  │  getChSampleCnt() │ isChInSample()                       │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    变更通知层                              │   │       │
 * │   │  │  busChange() │ chChange() │ busTypeChange()              │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                              ↓ 继承                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                    具体总线实现类                                 │       │
 * │   │  UARTBus │ LINBus │ CANBus │ SPIBus │ I2CBus │ ARINC429Bus │   │       │
 * │   │  MILSTD1553BBus │ CANFDBus                               │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【继承关系】                                                                 │
 * │   IBus (抽象基类)                                                            │
 * │     ├── UARTBus (UART总线实现)                                               │
 * │     ├── LINBus (LIN总线实现)                                                 │
 * │     ├── CANBus (CAN总线实现)                                                 │
 * │     ├── SPIBus (SPI总线实现)                                                 │
 * │     ├── I2CBus (I2C总线实现)                                                 │
 * │     ├── ARINC429Bus (ARINC429总线实现)                                       │
 * │     ├── MILSTD1553BBus (MIL-STD-1553B总线实现)                               │
 * │     └── CANFDBus (CAN-FD总线实现)                                            │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   子类配置变更 → busChange()/chChange()/busTypeChange() → BusAction        │
 * │                → FPGA寄存器配置 / 解码系统 / 触发系统                        │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   外部依赖：ChannelFactory（通道工厂）、BusAction（总线动作处理类）           │
 * │   子类依赖：UARTBus、LINBus、CANBus、SPIBus、I2CBus、ARINC429Bus等           │
 * │                                                                             │
 * │ 【设计模式】                                                                 │
 * │   1. 模板方法模式：定义抽象方法，由子类实现具体逻辑                            │
 * │   2. 观察者模式：通过BusAction通知配置变更                                   │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 创建I2C总线实例                                                         │
 * │   IBus i2cBus = new I2CBus(0);                                              │
 * │   // 获取总线类型                                                            │
 * │   int type = i2cBus.getBusType(); // 返回IBus.I2C                           │
 * │   // 获取采样通道数量                                                         │
 * │   int cnt = i2cBus.getChSampleCnt(); // 返回2（SDA和SCL）                    │
 * │   // 检查通道是否被使用                                                       │
 * │   boolean used = i2cBus.isChInSample(0);                                    │
 * │   // 启用总线类型                                                            │
 * │   IBus.setBusEnable(IBus.I2C, true);                                        │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. 该类为抽象类，不能直接实例化                                             │
 * │   2. 子类必须实现getChSampleCnt()和isChInSample()两个抽象方法                 │
 * │   3. 配置变更应调用protected方法触发通知，不应直接调用BusAction                │
 * │   4. busEnable数组为静态变量，所有实例共享                                    │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018-5-29                                              │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 总线抽象基类
 * <p>
 * 为所有具体总线类型提供统一的接口定义和通用功能实现。
 * 定义了总线类型、空闲电平、显示模式、触发关系等通用常量。
 * <p>
 * 核心功能：
 * - 定义总线类型常量（UART、LIN、CAN、SPI、I2C、ARINC429、MILSTD1553B、CAN_FD）
 * - 定义通用常量（空闲电平、显示模式、触发关系）
 * - 提供配置变更通知机制
 * - 定义抽象方法供子类实现
 * - 管理总线启用状态
 */
public abstract class IBus {

    // ==================== 总线类型常量定义 ====================
    
    /**
     * UART总线类型
     * UART（Universal Asynchronous Receiver/Transmitter）：通用异步收发传输器
     * 特点：异步通信、点对点、全双工
     */
    public static final int UART = 0;

    /**
     * LIN总线类型
     * LIN（Local Interconnect Network）：局域互联网络
     * 特点：单主多从、低成本、低速通信
     */
    public static final int LIN = 1;

    /**
     * CAN总线类型
     * CAN（Controller Area Network）：控制器局域网
     * 特点：多主、高可靠性、实时性强
     */
    public static final int CAN = 2;

    /**
     * SPI总线类型
     * SPI（Serial Peripheral Interface）：串行外设接口
     * 特点：同步通信、全双工、高速
     */
    public static final int SPI = 3;

    /**
     * I2C总线类型
     * I2C（Inter-Integrated Circuit）：两线式串行总线
     * 特点：两线制、多主多从、半双工
     */
    public static final int I2C = 4;

    /**
     * ARINC429总线类型
     * ARINC429：航空电子数字数据总线标准
     * 特点：单工、高可靠性、航空专用
     */
    public static final int ARINC429 = 5;

    /**
     * MIL-STD-1553B总线类型
     * MIL-STD-1553B：军用标准数据总线
     * 特点：时分复用、命令/响应协议、军用专用
     */
    public static final int MILSTD1553B = 6;

    /**
     * CAN-FD总线类型
     * CAN-FD（CAN with Flexible Data-Rate）：可变速率CAN
     * 特点：兼容CAN、更高数据速率、更长数据帧
     */
    public static final int CAN_FD = 7;

    /**
     * 总线类型总数
     * 用途：数组长度定义和边界检查
     */
    public static final int BUS_CNT = 8;

    // ==================== 空闲电平常量定义 ====================
    
    /**
     * 空闲电平为高电平
     * 用途：定义总线空闲状态时的电平极性
     */
    public static final int IDLE_LEVEL_HIGH = 0;

    /**
     * 空闲电平为低电平
     * 用途：定义总线空闲状态时的电平极性
     */
    public static final int IDLE_LEVEL_LOW  = 1;

    // ==================== 显示模式常量定义 ====================
    
    /**
     * 二进制显示模式
     * 用途：将解码数据显示为二进制格式
     */
    public static final int DISPLAY_BIN_DISPLAY = 0;

    /**
     * 十六进制显示模式
     * 用途：将解码数据显示为十六进制格式
     */
    public static final int DISPLAY_HEX_DISPLAY = 1;

    /**
     * ASCII显示模式
     * 用途：将解码数据显示为ASCII字符
     */
    public static final int DISPLAY_ASC_DISPLAY = 2;

    // ==================== 触发关系常量定义 ====================
    
    /**
     * 触发关系：等于
     * 用途：触发条件为等于指定值时触发
     */
    public static final int TRIGGER_RELATION_EQUAL      = 0;

    /**
     * 触发关系：大于
     * 用途：触发条件为大于指定值时触发
     */
    public static final int TRIGGER_RELATION_MORE_THAN  = 1;

    /**
     * 触发关系：小于
     * 用途：触发条件为小于指定值时触发
     */
    public static final int TRIGGER_RELATION_LESS_THAN  = 2;

    /**
     * 触发关系：不等于
     * 用途：触发条件为不等于指定值时触发
     */
    public static final int TRIGGER_RELATION_NOT_EQUAL  = 3;

    // ==================== 成员变量定义 ====================
    
    /**
     * 总线类型
     * 取值范围：UART ~ CAN_FD
     * 用途：标识当前总线实例的类型
     */
    private int busType = UART;

    /**
     * 总线索引
     * 取值范围：ChannelFactory.S1 ~ ChannelFactory.S4（对应串行总线通道S1-S4）
     * 用途：标识当前总线实例的通道编号
     */
    private int busIdx = ChannelFactory.S1;

    /**
     * 总线动作处理器
     * 用途：处理总线配置变更通知，通知FPGA寄存器、解码系统、触发系统等
     */
    private BusAction busAction;

    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * <p>
     * 功能：创建总线实例，初始化总线索引、类型和动作处理器
     * 
     * @param busIdx 总线索引（ChannelFactory.S1/S2/S3/S4）
     * @param busType 总线类型（UART/LIN/CAN/SPI/I2C/ARINC429/MILSTD1553B/CAN_FD）
     */
    public IBus(int busIdx,int busType){
        this.busIdx = busIdx; // 设置总线索引
        this.busType = busType; // 设置总线类型
        busAction = new BusAction(this); // 创建总线动作处理器
    }

    // ==================== Getter方法 ====================
    
    /**
     * 获取总线类型
     * 
     * @return 总线类型（UART ~ CAN_FD）
     */
    public int getBusType(){
        return busType; // 返回总线类型
    }

    /**
     * 获取总线索引
     * 
     * @return 总线索引（ChannelFactory.S1/S2/S3/S4）
     */
    public int getBusIdx() {
        return busIdx; // 返回总线索引
    }

    // ==================== 静态方法 ====================
    
    /**
     * 验证总线类型是否有效
     * <p>
     * 功能：检查总线类型是否在有效范围内
     * 范围：UART ~ BUS_CNT-1
     * 
     * @param busType 要验证的总线类型
     * @return true-有效，false-无效
     */
    public static boolean isValid(int busType){
        // 检查总线类型是否在有效范围内
        return (busType >= UART && busType < BUS_CNT);
    }

    // ==================== 变更通知方法 ====================
    
    /**
     * 通知总线配置变更
     * <p>
     * 功能：触发总线配置变更通知
     * 用途：子类在修改总线参数后调用，通知FPGA寄存器、解码系统等
     * 可见性：protected，仅供子类调用
     */
    protected void busChange(){
        // 调用BusAction的总线变更方法
        busAction.busChange();
    }

    /**
     * 通知通道采样配置变更
     * <p>
     * 功能：触发通道采样配置变更通知
     * 用途：子类在修改采样通道配置后调用，通知通道管理器
     * 可见性：protected，仅供子类调用
     */
    protected void chChange(){
        // 调用BusAction的通道采样变更方法
        busAction.chSampleChange();
    }

    /**
     * 通知总线类型变更
     * <p>
     * 功能：触发总线类型变更通知
     * 用途：子类在修改总线类型后调用，通知系统重新初始化
     * 可见性：protected，仅供子类调用
     */
    protected void busTypeChange(){
        // 调用BusAction的总线类型变更方法
        busAction.busTypeChange();
    }

    // ==================== 抽象方法 ====================
    
    /**
     * 获取采样通道数量
     * <p>
     * 功能：返回当前总线类型需要的采样通道数量
     * 说明：不同总线类型需要不同数量的采样通道
     *       - UART：1个通道（TX或RX）
     *       - I2C：2个通道（SDA和SCL）
     *       - SPI：4个通道（MOSI、MISO、SCK、CS）
     * 
     * @return 采样通道数量
     */
    public abstract int getChSampleCnt();

    /**
     * 检查指定通道是否在采样范围内
     * <p>
     * 功能：判断指定通道是否被当前总线使用
     * 用途：通道管理器判断通道是否被总线占用，避免冲突
     * 
     * @param chIdx 通道索引
     * @return true-该通道被当前总线使用，false-该通道未被使用
     */
    public abstract boolean isChInSample(int chIdx);

    // ==================== 静态启用状态管理 ====================
    
    /**
     * 总线启用状态数组
     * 长度：BUS_CNT（8种总线类型）
     * 用途：静态管理各总线类型的启用状态
     * 说明：静态变量，所有实例共享
     */
    private static boolean [] busEnable = {false,false,false,false,false,false,false,false};

    /**
     * 设置总线启用状态
     * <p>
     * 功能：启用或禁用指定类型的总线
     * 验证：调用isValid()验证总线类型有效性
     * 
     * @param busType 总线类型（UART ~ CAN_FD）
     * @param bEnable true-启用，false-禁用
     */
    public static void setBusEnable(int busType,boolean bEnable){
        // 验证总线类型是否有效
        if(isValid(busType)){
            busEnable[busType] = bEnable; // 设置启用状态
        }
    }

    /**
     * 获取总线启用状态
     * <p>
     * 功能：检查指定类型的总线是否启用
     * 验证：调用isValid()验证总线类型有效性
     * 
     * @param busType 总线类型（UART ~ CAN_FD）
     * @return true-已启用，false-未启用或无效类型
     */
    public static boolean isBusEnable(int busType){
        // 验证总线类型是否有效
        if(isValid(busType)){
            return busEnable[busType]; // 返回启用状态
        }
        return false; // 无效类型返回false
    }
}
