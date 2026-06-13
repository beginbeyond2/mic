package com.micsig.tbook.scope.Bus; // 包声明：MIL-STD-1553B总线类所属包路径

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                       MILSTD1553BBus 类说明文档                             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   MIL-STD-1553B总线触发配置类 - MHO系列示波器串行总线解码系统的军用标准        │
 * │   1553B协议触发配置组件，继承自IBus基类，负责MIL-STD-1553B总线触发参数的      │
 * │   存储和管理                                                                 │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 触发类型定义：定义8种MIL-STD-1553B触发类型常量                           │
 * │      - COMMAND_STATUS_SYNC：命令/状态字同步触发                             │
 * │      - DATA_WORD_SYNC：数据字同步触发                                       │
 * │      - COMMAND_STATUS_WORD：命令/状态字触发                                 │
 * │      - DATA_WORD：数据字触发                                                │
 * │      - RT_ADDRESS：远程终端地址触发                                         │
 * │      - ODD_PARITY_ERROR：奇校验错误触发                                     │
 * │      - MANCHESTER_ERROR：曼彻斯特编码错误触发                                │
 * │      - ALL_ERROR：所有错误触发                                              │
 * │   2. 通道配置：管理MIL-STD-1553B信号通道索引                                 │
 * │   3. 触发参数配置：管理触发类型、命令/状态字、数据、地址等参数                 │
 * │   4. 显示格式配置：支持二进制、十六进制、ASCII三种显示格式                     │
 * │   5. 变更通知：通过chChange()和busChange()通知父类配置变更                    │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                   MILSTD1553BBus (继承IBus)                      │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发类型常量层                          │   │       │
 * │   │  │  COMMAND_STATUS_SYNC │ DATA_WORD_SYNC │ COMMAND_STATUS   │   │       │
 * │   │  │  DATA_WORD │ RT_ADDRESS │ ODD_PARITY_ERROR │ ...         │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    通道配置层                              │   │       │
 * │   │  │  srcChIdx (MIL-STD-1553B信号通道索引)                     │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发参数配置层                          │   │       │
 * │   │  │  triggerType │ cmdStatus │ data │ addr │ displayFormat  │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    变更通知层                              │   │       │
 * │   │  │  chChange() │ busChange()                                │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【MIL-STD-1553B协议说明】                                                    │
 * │   MIL-STD-1553B是美国军用标准数据总线协议：                                   │
 * │   - 时分复用：时间分割多路复用                                               │
 * │   - 命令/响应：主从式架构，总线控制器(BC)控制远程终端(RT)                      │
 * │   - 高可靠性：双冗余总线设计                                                 │
 * │   - 数据速率：1Mbps                                                         │
 * │   - 字格式：20位字（3位同步头+16位数据+1位奇校验）                            │
 * │   - 应用场景：航空电子、军用车辆、航天器等                                    │
 * │                                                                             │
 * │ 【MIL-STD-1553B字类型】                                                      │
 * │   1. 命令字：BC发送给RT的控制命令                                            │
 * │   2. 状态字：RT返回给BC的状态信息                                            │
 * │   3. 数据字：BC和RT之间传输的数据                                            │
 * │                                                                             │
 * │ 【触发类型详解】                                                             │
 * │   1. COMMAND_STATUS_SYNC：检测到命令/状态字同步头                           │
 * │   2. DATA_WORD_SYNC：检测到数据字同步头                                     │
 * │   3. COMMAND_STATUS_WORD：检测到指定命令/状态字                             │
 * │   4. DATA_WORD：检测到指定数据字                                            │
 * │   5. RT_ADDRESS：检测到指定远程终端地址                                      │
 * │   6. ODD_PARITY_ERROR：检测到奇校验错误                                     │
 * │   7. MANCHESTER_ERROR：检测到曼彻斯特编码错误                                │
 * │   8. ALL_ERROR：检测到任何错误                                              │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   用户配置 → setTriggerType() → busChange() → IBus父类 → FPGA寄存器        │
 * │   用户配置 → setSrcChIdx() → chChange() → IBus父类 → 通道管理器             │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   父类依赖：IBus（总线基类，提供chChange()和busChange()方法）                │
 * │   下游依赖：FPGA寄存器配置、触发系统、解码系统                                │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 创建MIL-STD-1553B总线实例                                               │
 * │   MILSTD1553BBus bus = new MILSTD1553BBus(0);                              │
 * │   // 设置通道索引                                                            │
 * │   bus.setSrcChIdx(0);                                                       │
 * │   // 设置触发类型为远程终端地址触发                                           │
 * │   bus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_RT_ADDRESS);       │
 * │   // 设置远程终端地址                                                        │
 * │   bus.setAddr(0x10);                                                        │
 * │   // 设置显示格式为十六进制                                                   │
 * │   bus.setDisplayFormat(IBus.DISPLAY_HEX_DISPLAY);                          │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. MIL-STD-1553B总线只需要1个采样通道                                      │
 * │   2. 命令/状态字和数据字使用不同的同步头格式                                  │
 * │   3. 远程终端地址范围为0-30（5位地址）                                        │
 * │   4. 显示格式设置不触发变更通知                                              │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018-5-29                                              │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * MIL-STD-1553B总线触发配置类
 * <p>
 * 继承自IBus基类，负责MIL-STD-1553B总线触发参数的存储和管理。
 * 支持多种MIL-STD-1553B触发类型，包括命令/状态字同步、数据字同步、命令/状态字、
 * 数据字、远程终端地址、奇校验错误、曼彻斯特编码错误、所有错误等。
 * <p>
 * 核心功能：
 * - 定义MIL-STD-1553B触发类型常量
 * - 管理MIL-STD-1553B通道配置
 * - 管理触发参数（类型、命令/状态字、数据、地址）
 * - 支持多种显示格式
 * - 提供参数变更通知机制
 */
public class MILSTD1553BBus extends IBus {
    
    // ==================== 触发类型常量定义 ====================
    
    /**
     * 命令/状态字同步触发
     * 触发条件：检测到命令字或状态字的同步头
     * 用途：捕获MIL-STD-1553B命令/状态字的开始
     */
    public static final int MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC = 1;

    /**
     * 数据字同步触发
     * 触发条件：检测到数据字的同步头
     * 用途：捕获MIL-STD-1553B数据字的开始
     */
    public static final int MILSTD1553B_TRIGGER_DATA_WORD_SYNC = 2;

    /**
     * 命令/状态字触发
     * 触发条件：检测到指定的命令字或状态字
     * 用途：捕获特定命令或状态字
     */
    public static final int MILSTD1553B_TRIGGER_COMMAND_STATUS_WORD = 3;

    /**
     * 数据字触发
     * 触发条件：检测到指定的数据字
     * 用途：捕获特定数据字
     */
    public static final int MILSTD1553B_TRIGGER_DATA_WORD = 4;

    /**
     * 远程终端地址触发
     * 触发条件：检测到指定的远程终端(RT)地址
     * 用途：捕获特定RT的通信
     */
    public static final int MILSTD1553B_TRIGGER_RT_ADDRESS = 5;

    /**
     * 奇校验错误触发
     * 触发条件：检测到奇校验错误
     * 用途：检测MIL-STD-1553B通信错误
     */
    public static final int MILSTD1553B_TRIGGER_ODD_PARITY_ERROR = 6;

    /**
     * 曼彻斯特编码错误触发
     * 触发条件：检测到曼彻斯特编码错误
     * 用途：检测MIL-STD-1553B通信错误
     */
    public static final int MILSTD1553B_TRIGGER_MANCHESTER_ERROR = 7;

    /**
     * 所有错误触发
     * 触发条件：检测到任何错误（奇校验错误、曼彻斯特编码错误等）
     * 用途：检测所有MIL-STD-1553B通信错误
     */
    public static final int MILSTD1553B_TRIGGER_ALL_ERROR = 8;

    // ==================== 成员变量定义 ====================
    
    /**
     * MIL-STD-1553B信号通道索引
     * 取值范围：0 ~ 通道数-1
     * 用途：指定MIL-STD-1553B信号连接的物理通道
     */
    private int srcChIdx = 0;

    /**
     * 显示格式
     * 取值范围：DISPLAY_BIN_DISPLAY、DISPLAY_HEX_DISPLAY、DISPLAY_ASC_DISPLAY（继承自IBus）
     * 默认值：DISPLAY_HEX_DISPLAY
     * 用途：指定解码数据的显示格式
     */
    private int displayFormat = DISPLAY_HEX_DISPLAY;

    /**
     * 当前触发类型
     * 取值范围：MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC ~ MILSTD1553B_TRIGGER_ALL_ERROR
     * 默认值：MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC
     * 用途：指定当前使用的触发类型
     */
    private int triggerType = MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC;

    /**
     * 命令/状态字
     * 用途：存储命令字或状态字的值，用于COMMAND_STATUS_WORD触发类型
     * 格式：20位字（3位同步头+16位数据+1位奇校验）
     */
    private int cmdStatus = 0;

    /**
     * 数据字
     * 用途：存储数据字的值，用于DATA_WORD触发类型
     * 格式：16位数据
     */
    private int data;

    /**
     * 远程终端地址
     * 用途：存储远程终端(RT)地址，用于RT_ADDRESS触发类型
     * 取值范围：0-30（5位地址）
     */
    private int addr;

    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * <p>
     * 功能：创建MIL-STD-1553B总线实例，初始化总线索引和类型
     * 
     * @param busIdx 总线索引（0表示第一个MIL-STD-1553B总线）
     */
    public MILSTD1553BBus(int busIdx) {
        super(busIdx,MILSTD1553B); // 调用父类构造函数，设置总线索引和类型为MILSTD1553B
    }

    // ==================== 通道配置方法 ====================
    
    /**
     * 获取MIL-STD-1553B信号通道索引
     * 
     * @return MIL-STD-1553B信号通道索引
     */
    public int getSrcChIdx() {
        return srcChIdx; // 返回MIL-STD-1553B信号通道索引
    }

    /**
     * 设置MIL-STD-1553B信号通道索引
     * <p>
     * 功能：设置MIL-STD-1553B信号连接的物理通道
     * 副作用：调用chChange()通知父类通道配置变更
     * 
     * @param srcChIdx MIL-STD-1553B信号通道索引
     */
    public void setSrcChIdx(int srcChIdx) {
        this.srcChIdx = srcChIdx; // 设置MIL-STD-1553B信号通道索引
        chChange(); // 通知父类通道配置变更
    }

    // ==================== 显示格式配置方法 ====================
    
    /**
     * 获取显示格式
     * 
     * @return 显示格式（DISPLAY_BIN_DISPLAY/DISPLAY_HEX_DISPLAY/DISPLAY_ASC_DISPLAY）
     */
    public int getDisplayFormat() {
        return displayFormat; // 返回显示格式
    }

    /**
     * 设置显示格式
     * <p>
     * 功能：设置解码数据的显示格式
     * 说明：不触发变更通知，仅设置本地值
     * 
     * @param displayFormat 显示格式（DISPLAY_BIN_DISPLAY/DISPLAY_HEX_DISPLAY/DISPLAY_ASC_DISPLAY）
     */
    public void setDisplayFormat(int displayFormat) {
        this.displayFormat = displayFormat; // 设置显示格式
    }

    // ==================== 触发类型配置方法 ====================
    
    /**
     * 获取当前触发类型
     * 
     * @return 当前触发类型（MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC ~ MILSTD1553B_TRIGGER_ALL_ERROR）
     */
    public int getTriggerType() {
        return triggerType; // 返回当前触发类型
    }

    /**
     * 设置触发类型
     * <p>
     * 功能：设置当前使用的触发类型
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param triggerType 触发类型
     */
    public void setTriggerType(int triggerType) {
        this.triggerType = triggerType; // 设置触发类型
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 命令/状态字配置方法 ====================
    
    /**
     * 获取命令/状态字
     * 
     * @return 命令/状态字（20位字）
     */
    public int getCmdStatus() {
        return cmdStatus; // 返回命令/状态字
    }

    /**
     * 设置命令/状态字
     * <p>
     * 功能：设置命令字或状态字的值
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param cmdStatus 命令/状态字（20位字）
     */
    public void setCmdStatus(int cmdStatus) {
        this.cmdStatus = cmdStatus; // 设置命令/状态字
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 数据字配置方法 ====================
    
    /**
     * 获取数据字
     * 
     * @return 数据字（16位）
     */
    public int getData() {
        return data; // 返回数据字
    }

    /**
     * 设置数据字
     * <p>
     * 功能：设置数据字的值
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param data 数据字（16位）
     */
    public void setData(int data) {
        this.data = data; // 设置数据字
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 远程终端地址配置方法 ====================
    
    /**
     * 获取远程终端地址
     * 
     * @return 远程终端地址（0-30）
     */
    public int getAddr() {
        return addr; // 返回远程终端地址
    }

    /**
     * 设置远程终端地址
     * <p>
     * 功能：设置远程终端(RT)地址
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param addr 远程终端地址（0-30）
     */
    public void setAddr(int addr) {
        this.addr = addr; // 设置远程终端地址
        busChange(); // 通知父类总线配置变更
    }

    // ==================== IBus抽象方法实现 ====================
    
    /**
     * 获取采样通道数量
     * <p>
     * 功能：返回MIL-STD-1553B总线需要的采样通道数量
     * MIL-STD-1553B协议只需要1个通道：MIL-STD-1553B信号线
     * 
     * @return 采样通道数量（固定为1）
     */
    @Override
    public int getChSampleCnt() {
        return 1; // MIL-STD-1553B只需要1个通道
    }

    /**
     * 检查指定通道是否在采样范围内
     * <p>
     * 功能：判断指定通道是否为MIL-STD-1553B信号通道
     * 用途：通道管理器判断通道是否被MIL-STD-1553B总线占用
     * 
     * @param chIdx 通道索引
     * @return true-该通道被MIL-STD-1553B使用，false-该通道未被MIL-STD-1553B使用
     */
    @Override
    public boolean isChInSample(int chIdx) {
        // 检查通道是否为MIL-STD-1553B信号通道
        return srcChIdx == chIdx;
    }
}
