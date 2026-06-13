package com.micsig.tbook.scope.Bus; // 包声明：UART总线类所属包路径

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                          UartBus 类说明文档                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   UART总线触发配置类 - MHO系列示波器串行总线解码系统的UART协议触发配置组件     │
 * │   继承自IBus基类，负责UART总线触发参数的存储和管理                              │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 校验类型定义：定义3种UART校验类型常量                                    │
 * │      - NONE_VERIFY：无校验                                                  │
 * │      - ODD_VERIFY：奇校验                                                   │
 * │      - EVEN_VERIFY：偶校验                                                  │
 * │   2. 触发类型定义：定义7种UART触发类型常量                                    │
 * │      - START_BIT：起始位触发                                                │
 * │      - STOP_BIT：停止位触发                                                 │
 * │      - DATA：数据触发                                                       │
 * │      - DATA1：数据1触发                                                     │
 * │      - DATA0：数据0触发                                                     │
 * │      - DATAx：数据x触发                                                     │
 * │      - OOD_EVEN_BIT_ERROR：奇偶校验错误触发                                 │
 * │   3. 通道配置：管理UART接收信号通道索引                                       │
 * │   4. 触发参数配置：管理触发类型、触发数据、触发关系等参数                       │
 * │   5. 通信参数配置：管理波特率、数据位数、校验类型、空闲电平等参数               │
 * │   6. 变更通知：通过chChange()和busChange()通知父类配置变更                    │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                      UartBus (继承IBus)                          │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    校验类型常量层                          │   │       │
 * │   │  │  NONE_VERIFY │ ODD_VERIFY │ EVEN_VERIFY                   │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发类型常量层                          │   │       │
 * │   │  │  START_BIT │ STOP_BIT │ DATA │ DATA1 │ DATA0 │ ...       │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    通道配置层                              │   │       │
 * │   │  │  rxChIdx (UART接收通道索引)                               │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    通信参数配置层                          │   │       │
 * │   │  │  baudRate │ bits │ verify │ idleLevel │ displayFormat   │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发参数配置层                          │   │       │
 * │   │  │  triggerType │ triggerDatas[] │ triggerRelation          │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    变更通知层                              │   │       │
 * │   │  │  chChange() │ busChange()                                │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【UART协议说明】                                                             │
 * │   UART（Universal Asynchronous Receiver/Transmitter）是一种异步串行通信协议：│
 * │   - 异步通信：无需时钟线，发送方和接收方使用约定的波特率                        │
 * │   - 单线制：仅需一根信号线（TX或RX）                                         │
 * │   - 全双工：TX和RX独立，可同时收发                                          │
 * │   - 帧结构：起始位(1位) + 数据位(5-9位) + 校验位(0-1位) + 停止位(1-2位)      │
 * │   - 应用场景：串口通信、调试接口、传感器通信等                                │
 * │                                                                             │
 * │ 【UART帧结构】                                                               │
 * │   ┌─────────┬──────────────┬─────────┬──────────┐                         │
 * │   │ 起始位   │   数据位      │ 校验位   │ 停止位    │                         │
 * │   │  1 bit  │  5-9 bits    │ 0-1 bit │ 1-2 bits │                         │
 * │   └─────────┴──────────────┴─────────┴──────────┘                         │
 * │                                                                             │
 * │ 【校验类型详解】                                                             │
 * │   1. NONE_VERIFY：无校验，不使用校验位                                      │
 * │   2. ODD_VERIFY：奇校验，数据位+校验位中1的个数为奇数                        │
 * │   3. EVEN_VERIFY：偶校验，数据位+校验位中1的个数为偶数                       │
 * │                                                                             │
 * │ 【触发类型详解】                                                             │
 * │   1. START_BIT：检测到起始位                                                │
 * │   2. STOP_BIT：检测到停止位                                                 │
 * │   3. DATA：检测到指定数据                                                   │
 * │   4. DATA1：检测到数据位为1                                                 │
 * │   5. DATA0：检测到数据位为0                                                 │
 * │   6. DATAx：检测到数据位为x                                                 │
 * │   7. OOD_EVEN_BIT_ERROR：检测到奇偶校验错误                                 │
 * │                                                                             │
 * │ 【触发关系】                                                                 │
 * │   - TRIGGER_RELATION_EQUAL：等于                                           │
 * │   - TRIGGER_RELATION_MORE_THAN：大于                                       │
 * │   - TRIGGER_RELATION_LESS_THAN：小于                                       │
 * │   - TRIGGER_RELATION_NOT_EQUAL：不等于                                     │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   用户配置 → setTriggerType() → busChange() → IBus父类 → FPGA寄存器        │
 * │   用户配置 → setRxChIdx() → chChange() → IBus父类 → 通道管理器             │
 * │                                                                             │
 * │ 【线程安全】                                                                 │
 * │   - setRxChIdx()和getRxChIdx()使用synchronized保证线程安全                  │
 * │   - isChInSample()使用synchronized保证线程安全                              │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   父类依赖：IBus（总线基类，提供chChange()和busChange()方法）                │
 * │   下游依赖：FPGA寄存器配置、触发系统、解码系统                                │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 创建UART总线实例                                                        │
 * │   UartBus uartBus = new UartBus(0);                                         │
 * │   // 设置接收通道索引                                                        │
 * │   uartBus.setRxChIdx(0);                                                    │
 * │   // 设置波特率                                                              │
 * │   uartBus.setBaudRate(115200);                                              │
 * │   // 设置数据位数                                                            │
 * │   uartBus.setBits(8);                                                       │
 * │   // 设置校验类型为无校验                                                     │
 * │   uartBus.setVerify(UartBus.UART_NONE_VERIFY);                              │
 * │   // 设置触发类型为数据触发                                                   │
 * │   uartBus.setTriggerType(UartBus.UART_TRIGGER_DATA);                        │
 * │   // 设置触发数据                                                            │
 * │   uartBus.setTriggerData(0x55);                                             │
 * │   // 设置触发关系为等于                                                       │
 * │   uartBus.setTriggerRelation(IBus.TRIGGER_RELATION_EQUAL);                  │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. UART总线只需要1个采样通道                                               │
 * │   2. 波特率需与通信设备一致                                                  │
 * │   3. 数据位数、校验类型、停止位数需与通信设备配置一致                          │
 * │   4. triggerDatas数组长度为UART_TRIGGER_TYPE_MAX                            │
 * │   5. 设置触发类型前应调用isTriggerTypeValid()验证有效性                       │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018-5-29                                              │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * UART总线触发配置类
 * <p>
 * 继承自IBus基类，负责UART总线触发参数的存储和管理。
 * 支持多种UART触发类型，包括起始位触发、停止位触发、数据触发等。
 * <p>
 * 核心功能：
 * - 定义UART校验类型常量
 * - 定义UART触发类型常量
 * - 管理UART通道配置
 * - 管理通信参数（波特率、数据位数、校验类型等）
 * - 管理触发参数（类型、数据、关系）
 * - 提供参数变更通知机制
 * - 线程安全的通道配置方法
 */
public class UartBus extends IBus {

    // ==================== 校验类型常量定义 ====================
    
    /**
     * 无校验
     * 用途：不使用校验位
     */
    public static final int UART_NONE_VERIFY = 0;

    /**
     * 奇校验
     * 用途：数据位+校验位中1的个数为奇数
     */
    public static final int UART_ODD_VERIFY = 1;

    /**
     * 偶校验
     * 用途：数据位+校验位中1的个数为偶数
     */
    public static final int UART_EVEN_VERIFY = 3;

    // ==================== 触发类型常量定义 ====================
    
    /**
     * 起始位触发
     * 触发条件：检测到起始位
     * 用途：捕获UART帧的开始
     */
    public static final int UART_TRIGGER_START_BIT          = 1;

    /**
     * 停止位触发
     * 触发条件：检测到停止位
     * 用途：捕获UART帧的结束
     */
    public static final int UART_TRIGGER_STOP_BIT           = 2;

    /**
     * 数据触发
     * 触发条件：检测到指定数据
     * 用途：捕获特定数据的UART帧
     */
    public static final int UART_TRIGGER_DATA               = 3;

    /**
     * 数据1触发
     * 触发条件：检测到数据位为1
     * 用途：捕获数据位为1的UART帧
     */
    public static final int UART_TRIGGER_DATA1              = 4;

    /**
     * 数据0触发
     * 触发条件：检测到数据位为0
     * 用途：捕获数据位为0的UART帧
     */
    public static final int UART_TRIGGER_DATA0              = 5;

    /**
     * 数据x触发
     * 触发条件：检测到数据位为x
     * 用途：捕获数据位为x的UART帧
     */
    public static final int UART_TRIGGER_DATAx              = 6;

    /**
     * 奇偶校验错误触发
     * 触发条件：检测到奇偶校验错误
     * 用途：检测UART通信错误
     */
    public static final int UART_TRIGGER_OOD_EVEN_BIT_ERROR = 7;

    /**
     * 触发类型最大值
     * 用途：数组长度定义和边界检查
     */
    public static final int UART_TRIGGER_TYPE_MAX = 8;

    // ==================== 成员变量定义 ====================
    
    /**
     * UART接收通道索引
     * 取值范围：0 ~ 通道数-1
     * 默认值：0
     * 用途：指定UART RX信号连接的物理通道
     * 线程安全：使用synchronized保护
     */
    private int rxChIdx = 0;

    /**
     * 空闲电平
     * 取值范围：IDLE_LEVEL_HIGH、IDLE_LEVEL_LOW（继承自IBus）
     * 默认值：IDLE_LEVEL_HIGH
     * 用途：定义UART总线空闲状态时的电平极性
     */
    private int idleLevel = IDLE_LEVEL_HIGH;

    /**
     * 校验类型
     * 取值范围：UART_NONE_VERIFY、UART_ODD_VERIFY、UART_EVEN_VERIFY
     * 默认值：UART_NONE_VERIFY
     * 用途：指定UART校验类型
     */
    private int verify = UART_NONE_VERIFY;

    /**
     * 数据位数
     * 单位：位
     * 默认值：0（未设置）
     * 取值范围：5 ~ 9
     * 用途：设置UART数据帧的数据位数
     */
    private int bits = 0;

    /**
     * 波特率
     * 单位：bps（位/秒）
     * 默认值：9600
     * 取值范围：常见值有9600、19200、38400、57600、115200等
     * 用途：设置UART通信的波特率
     */
    private int baudRate = 9600;

    /**
     * 显示格式
     * 取值范围：DISPLAY_BIN_DISPLAY、DISPLAY_HEX_DISPLAY、DISPLAY_ASC_DISPLAY（继承自IBus）
     * 默认值：DISPLAY_HEX_DISPLAY
     * 用途：指定解码数据的显示格式
     */
    private int displayFormat = DISPLAY_HEX_DISPLAY;

    /**
     * 当前触发类型
     * 取值范围：UART_TRIGGER_START_BIT ~ UART_TRIGGER_OOD_EVEN_BIT_ERROR
     * 默认值：UART_TRIGGER_START_BIT
     * 用途：指定当前使用的触发类型
     */
    private int triggerType = UART_TRIGGER_START_BIT;

    /**
     * 触发数据数组
     * 长度：UART_TRIGGER_TYPE_MAX
     * 用途：存储每种触发类型对应的触发数据值
     * 说明：不同触发类型可能需要不同的触发数据参数
     */
    private int []triggerDatas = new int[UART_TRIGGER_TYPE_MAX];

    /**
     * 触发关系
     * 取值范围：TRIGGER_RELATION_EQUAL、TRIGGER_RELATION_MORE_THAN、
     *          TRIGGER_RELATION_LESS_THAN、TRIGGER_RELATION_NOT_EQUAL（继承自IBus）
     * 默认值：TRIGGER_RELATION_EQUAL
     * 用途：指定触发数据与实际数据的比较关系
     */
    private int triggerRelation = TRIGGER_RELATION_EQUAL;

    // ==================== 静态方法 ====================
    
    /**
     * 验证触发类型是否有效
     * <p>
     * 功能：检查触发类型是否在有效范围内
     * 范围：UART_TRIGGER_START_BIT ~ UART_TRIGGER_OOD_EVEN_BIT_ERROR
     * 
     * @param uartTriggerType 要验证的触发类型
     * @return true-有效，false-无效
     */
    public static boolean isTriggerTypeValid(int uartTriggerType){
        // 检查触发类型是否在有效范围内
        return (uartTriggerType>=UART_TRIGGER_START_BIT && uartTriggerType<=UART_TRIGGER_OOD_EVEN_BIT_ERROR);
    }

    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * <p>
     * 功能：创建UART总线实例，初始化总线索引和类型
     * 
     * @param busIdx 总线索引（0表示第一个UART总线）
     */
    public UartBus(int busIdx) {
        super(busIdx,UART); // 调用父类构造函数，设置总线索引和类型为UART
    }

    // ==================== 通道配置方法 ====================
    
    /**
     * 设置UART接收通道索引
     * <p>
     * 功能：设置UART RX信号连接的物理通道
     * 线程安全：使用synchronized保证线程安全
     * 副作用：调用chChange()通知父类通道配置变更
     * 
     * @param chIdx UART接收通道索引
     */
    public void setRxChIdx(int chIdx){
        synchronized (this) { // 同步锁，保证线程安全
            this.rxChIdx = chIdx; // 设置UART接收通道索引
        }
        chChange(); // 通知父类通道配置变更
    }

    /**
     * 获取UART接收通道索引
     * <p>
     * 线程安全：使用synchronized保证线程安全
     * 
     * @return UART接收通道索引
     */
    public synchronized int getRxChIdx(){
        return rxChIdx; // 返回UART接收通道索引
    }

    // ==================== 空闲电平配置方法 ====================
    
    /**
     * 设置空闲电平
     * <p>
     * 功能：设置UART总线空闲状态时的电平极性
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param idleLevel 空闲电平（IDLE_LEVEL_HIGH/IDLE_LEVEL_LOW）
     */
    public void setIdleLevel(int idleLevel){
        this.idleLevel = idleLevel; // 设置空闲电平
        busChange(); // 通知父类总线配置变更
    }

    /**
     * 获取空闲电平
     * 
     * @return 空闲电平（IDLE_LEVEL_HIGH/IDLE_LEVEL_LOW）
     */
    public int getIdleLevel(){
        return idleLevel; // 返回空闲电平
    }

    // ==================== 校验类型配置方法 ====================
    
    /**
     * 设置校验类型
     * <p>
     * 功能：设置UART校验类型（无校验、奇校验、偶校验）
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param verify 校验类型（UART_NONE_VERIFY/UART_ODD_VERIFY/UART_EVEN_VERIFY）
     */
    public void setVerify(int verify){
        this.verify = verify; // 设置校验类型
        busChange(); // 通知父类总线配置变更
    }

    /**
     * 获取校验类型
     * 
     * @return 校验类型（UART_NONE_VERIFY/UART_ODD_VERIFY/UART_EVEN_VERIFY）
     */
    public int getVerify(){
        return verify; // 返回校验类型
    }

    // ==================== 数据位数配置方法 ====================
    
    /**
     * 设置数据位数
     * <p>
     * 功能：设置UART数据帧的数据位数
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param bits 数据位数（范围：5-9）
     */
    public void setBits(int bits){
        this.bits = bits; // 设置数据位数
        busChange(); // 通知父类总线配置变更
    }

    /**
     * 获取数据位数
     * 
     * @return 数据位数
     */
    public int getBits(){
        return bits; // 返回数据位数
    }

    // ==================== 波特率配置方法 ====================
    
    /**
     * 设置波特率
     * <p>
     * 功能：设置UART通信的波特率
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param baudRate 波特率（单位：bps）
     */
    public void setBaudRate(int baudRate){
        this.baudRate = baudRate; // 设置波特率
        busChange(); // 通知父类总线配置变更
    }

    /**
     * 获取波特率
     * 
     * @return 波特率（单位：bps）
     */
    public int getBaudRate(){
        return baudRate; // 返回波特率
    }

    // ==================== 显示格式配置方法 ====================
    
    /**
     * 设置显示格式
     * <p>
     * 功能：设置解码数据的显示格式
     * 说明：不触发变更通知，仅设置本地值
     * 
     * @param displayFormat 显示格式（DISPLAY_BIN_DISPLAY/DISPLAY_HEX_DISPLAY/DISPLAY_ASC_DISPLAY）
     */
    public void setDisplayFormat(int displayFormat){
        this.displayFormat = displayFormat; // 设置显示格式
    }

    /**
     * 获取显示格式
     * 
     * @return 显示格式（DISPLAY_BIN_DISPLAY/DISPLAY_HEX_DISPLAY/DISPLAY_ASC_DISPLAY）
     */
    public int getDisplayFormat(){
        return displayFormat; // 返回显示格式
    }

    // ==================== 触发类型配置方法 ====================
    
    /**
     * 设置触发类型
     * <p>
     * 功能：设置当前使用的触发类型
     * 验证：调用isTriggerTypeValid()验证类型有效性
     * 副作用：验证通过后调用busChange()通知父类配置变更
     * 
     * @param triggerType 触发类型
     */
    public void setTriggerType(int triggerType){
        // 验证触发类型是否有效
        if(isTriggerTypeValid(triggerType)) {
            this.triggerType = triggerType; // 设置触发类型
            busChange(); // 通知父类总线配置变更
        }
    }

    /**
     * 获取当前触发类型
     * 
     * @return 当前触发类型（UART_TRIGGER_START_BIT ~ UART_TRIGGER_OOD_EVEN_BIT_ERROR）
     */
    public int getTriggerType(){
        return triggerType; // 返回当前触发类型
    }

    // ==================== 触发数据配置方法 ====================
    
    /**
     * 设置当前触发类型的触发数据
     * <p>
     * 功能：设置triggerDatas数组中当前触发类型对应的触发数据
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param val 触发数据值
     */
    public void setTriggerData(int val){
        triggerDatas[triggerType] = val; // 设置当前触发类型的触发数据
        busChange(); // 通知父类总线配置变更
    }

    /**
     * 获取当前触发类型的触发数据
     * 
     * @return 当前触发类型的触发数据
     */
    public int getTriggerData(){
        return triggerDatas[triggerType]; // 返回当前触发类型的触发数据
    }

    /**
     * 设置指定触发类型的触发数据
     * <p>
     * 功能：设置triggerDatas数组中指定触发类型对应的触发数据
     * 验证：调用isTriggerTypeValid()验证类型有效性
     * 副作用：验证通过后调用busChange()通知父类配置变更
     * 
     * @param triggerType 触发类型
     * @param val 触发数据值
     */
    public void setTriggerData(int triggerType,int val){
        // 验证触发类型是否有效
        if(isTriggerTypeValid(triggerType)) {
            triggerDatas[triggerType] = val; // 设置指定触发类型的触发数据
            busChange(); // 通知父类总线配置变更
        }
    }

    /**
     * 获取指定触发类型的触发数据
     * <p>
     * 功能：获取triggerDatas数组中指定触发类型对应的触发数据
     * 验证：调用isTriggerTypeValid()验证类型有效性
     * 
     * @param triggerType 触发类型
     * @return 指定触发类型的触发数据，无效类型返回0
     */
    public int getTriggerData(int triggerType){
        // 验证触发类型是否有效
        if(isTriggerTypeValid(triggerType))
            return triggerDatas[triggerType]; // 返回指定触发类型的触发数据
        return 0; // 无效类型返回0
    }

    // ==================== 触发关系配置方法 ====================
    
    /**
     * 设置触发关系
     * <p>
     * 功能：设置触发数据与实际数据的比较关系
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param triggerRelation 触发关系（TRIGGER_RELATION_EQUAL等）
     */
    public void setTriggerRelation(int triggerRelation){
        this.triggerRelation = triggerRelation; // 设置触发关系
        busChange(); // 通知父类总线配置变更
    }

    /**
     * 获取触发关系
     * 
     * @return 触发关系（TRIGGER_RELATION_EQUAL等）
     */
    public int getTriggerRelation(){
        return triggerRelation; // 返回触发关系
    }

    // ==================== IBus抽象方法实现 ====================
    
    /**
     * 获取采样通道数量
     * <p>
     * 功能：返回UART总线需要的采样通道数量
     * UART协议只需要1个通道：RX信号线
     * 
     * @return 采样通道数量（固定为1）
     */
    @Override
    public int getChSampleCnt() {
        return 1; // UART只需要1个通道
    }

    /**
     * 检查指定通道是否在采样范围内
     * <p>
     * 功能：判断指定通道是否为UART接收通道
     * 用途：通道管理器判断通道是否被UART总线占用
     * 线程安全：使用synchronized保证线程安全
     * 
     * @param chIdx 通道索引
     * @return true-该通道被UART使用，false-该通道未被UART使用
     */
    @Override
    public boolean isChInSample(int chIdx) {
        synchronized (this) { // 同步锁，保证线程安全
            return rxChIdx == chIdx; // 检查通道是否为UART接收通道
        }
    }
}
