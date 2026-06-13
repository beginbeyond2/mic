package com.micsig.tbook.scope.Bus; // 包声明：LIN总线类所属包路径

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                          LinBus 类说明文档                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   LIN总线触发配置类 - MHO系列示波器串行总线解码系统的LIN协议触发配置组件       │
 * │   继承自IBus基类，负责LIN总线触发参数的存储和管理                              │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 触发类型定义：定义6种LIN触发类型常量                                     │
 * │      - SYNC_RISING_EDGE：同步上升沿触发                                     │
 * │      - FRAME_ID：帧ID触发                                                  │
 * │      - ID_AND_DATA：ID和数据触发                                            │
 * │      - DATA：数据触发                                                       │
 * │      - PARITY_ERROR：奇偶校验错误触发                                       │
 * │      - CHECKSUM_ERROR：校验和错误触发                                       │
 * │   2. LIN协议版本配置：支持LIN 1.3、LIN 1.3长帧、LIN 2.0三种协议版本           │
 * │   3. 通道配置：管理LIN信号通道索引                                            │
 * │   4. 触发参数配置：管理触发类型、帧ID、数据、波特率等参数                       │
 * │   5. 变更通知：通过chChange()、busChange()、busTypeChange()通知父类变更       │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                      LinBus (继承IBus)                           │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发类型常量层                          │   │       │
 * │   │  │  SYNC_RISING_EDGE │ FRAME_ID │ ID_AND_DATA │ DATA │ ...  │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    LIN协议版本层                           │   │       │
 * │   │  │  LIN_TYPE_1_3 │ LIN_TYPE_1_3_LONG │ LIN_TYPE_2_0          │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    通道配置层                              │   │       │
 * │   │  │  srcChIdx (LIN信号通道索引)                                │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发参数配置层                          │   │       │
 * │   │  │  triggerType │ frameIds[] │ data[] │ dataLength │         │   │       │
 * │   │  │  idleLevel │ baudRate │ linType                         │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    变更通知层                              │   │       │
 * │   │  │  chChange() │ busChange() │ busTypeChange()              │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【LIN协议说明】                                                              │
 * │   LIN（Local Interconnect Network）是一种单主多从的串行通信协议：             │
 * │   - 单线制：仅需一根信号线                                                   │
 * │   - 单主多从：一个主节点控制多个从节点                                        │
 * │   - 低成本：适用于汽车车身电子系统                                            │
 * │   - 低速：最高波特率20kbps                                                  │
 * │   - 帧结构：同步间隔+同步字段+标识符+数据+校验和                              │
 * │                                                                             │
 * │ 【LIN协议版本】                                                              │
 * │   1. LIN 1.3：基础版本，支持标准帧格式                                       │
 * │   2. LIN 1.3长帧：支持更长的数据帧                                           │
 * │   3. LIN 2.0：增强版本，支持更灵活的帧格式和诊断功能                          │
 * │                                                                             │
 * │ 【触发类型详解】                                                             │
 * │   1. SYNC_RISING_EDGE：检测到同步字段上升沿                                  │
 * │   2. FRAME_ID：检测到指定帧ID                                               │
 * │   3. ID_AND_DATA：检测到指定ID和数据                                        │
 * │   4. DATA：检测到指定数据                                                   │
 * │   5. PARITY_ERROR：检测到奇偶校验错误                                       │
 * │   6. CHECKSUM_ERROR：检测到校验和错误                                       │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   用户配置 → setTriggerType() → busChange() → IBus父类 → FPGA寄存器        │
 * │   用户配置 → setSrcChIdx() → chChange() → IBus父类 → 通道管理器             │
 * │   用户配置 → setLinType() → busTypeChange() → IBus父类 → 协议解码器         │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   父类依赖：IBus（总线基类，提供chChange()、busChange()、busTypeChange()）   │
 * │   下游依赖：FPGA寄存器配置、触发系统、解码系统                                │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 创建LIN总线实例                                                         │
 * │   LinBus linBus = new LinBus(0);                                            │
 * │   // 设置LIN协议版本                                                         │
 * │   linBus.setLinType(LinBus.LIN_TYPE_2_0);                                   │
 * │   // 设置通道索引                                                            │
 * │   linBus.setSrcChIdx(0);                                                    │
 * │   // 设置波特率                                                              │
 * │   linBus.setBaudRate(19200);                                                │
 * │   // 设置触发类型为帧ID触发                                                   │
 * │   linBus.setTriggerType(LinBus.LIN_TRIGGER_FRAME_ID);                       │
 * │   // 设置帧ID                                                               │
 * │   linBus.setFrameId(LinBus.LIN_TRIGGER_FRAME_ID, 0x10);                     │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. frameIds和data数组长度为LIN_TRIGGER_TYPE_MAX                           │
 * │   2. 设置触发类型前应调用isTriggerTypeValid()验证有效性                       │
 * │   3. LIN总线只需要1个采样通道                                                │
 * │   4. 波特率范围为1-20000bps                                                 │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018-5-29                                              │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * LIN总线触发配置类
 * <p>
 * 继承自IBus基类，负责LIN总线触发参数的存储和管理。
 * 支持多种LIN触发类型，包括同步上升沿、帧ID、ID和数据、数据、奇偶校验错误、校验和错误等。
 * <p>
 * 核心功能：
 * - 定义LIN触发类型常量
 * - 支持LIN 1.3、LIN 1.3长帧、LIN 2.0协议版本
 * - 管理LIN通道配置
 * - 管理触发参数（类型、帧ID、数据、波特率等）
 * - 提供参数变更通知机制
 */
public class LinBus extends IBus {
    
    // ==================== 触发类型常量定义 ====================
    
    /**
     * 同步上升沿触发
     * 触发条件：检测到LIN同步字段的上升沿
     * 用途：捕获LIN帧的开始
     */
    public static final int LIN_TRIGGER_SYNC_RISING_EDGE = 0;

    /**
     * 帧ID触发
     * 触发条件：检测到指定的帧ID
     * 用途：捕获特定ID的LIN帧
     */
    public static final int LIN_TRIGGER_FRAME_ID = LIN_TRIGGER_SYNC_RISING_EDGE + 1;

    /**
     * ID和数据触发
     * 触发条件：检测到指定的帧ID和数据
     * 用途：捕获特定ID和数据的LIN帧
     */
    public static final int LIN_TRIGGER_ID_AND_DATA = LIN_TRIGGER_FRAME_ID + 1;

    /**
     * 数据触发
     * 触发条件：检测到指定的数据
     * 用途：捕获包含特定数据的LIN帧
     */
    public static final int LIN_TRIGGER_DATA = LIN_TRIGGER_ID_AND_DATA + 1;

    /**
     * 奇偶校验错误触发
     * 触发条件：检测到奇偶校验错误
     * 用途：检测LIN通信错误
     */
    public static final int LIN_TRIGGER_PARITY_ERROR = LIN_TRIGGER_DATA + 1;

    /**
     * 校验和错误触发
     * 触发条件：检测到校验和错误
     * 用途：检测LIN通信错误
     */
    public static final int LIN_TRIGGER_CHECKSUM_ERROR = LIN_TRIGGER_PARITY_ERROR + 1;

    /**
     * 触发类型最大值
     * 用途：数组长度定义和边界检查
     */
    public static final int LIN_TRIGGER_TYPE_MAX = LIN_TRIGGER_CHECKSUM_ERROR + 1;

    // ==================== LIN协议版本常量定义 ====================
    
    /**
     * LIN 1.3协议版本
     * 特点：基础版本，支持标准帧格式
     */
    public static final int LIN_TYPE_1_3 = 0;

    /**
     * LIN 1.3长帧协议版本
     * 特点：支持更长的数据帧
     */
    public static final int LIN_TYPE_1_3_LONG = LIN_TYPE_1_3 + 1;

    /**
     * LIN 2.0协议版本
     * 特点：增强版本，支持更灵活的帧格式和诊断功能
     */
    public static final int LIN_TYPE_2_0 = LIN_TYPE_1_3_LONG + 1;

    // ==================== 成员变量定义 ====================
    
    /**
     * LIN信号通道索引
     * 取值范围：0 ~ 通道数-1
     * 用途：指定LIN信号连接的物理通道
     */
    private int srcChIdx = 0;

    /**
     * 空闲电平
     * 取值范围：IDLE_LEVEL_HIGH、IDLE_LEVEL_LOW（继承自IBus）
     * 默认值：IDLE_LEVEL_HIGH
     * 用途：定义LIN总线空闲状态时的电平极性
     */
    private int idleLevel = IDLE_LEVEL_HIGH;

    /**
     * 波特率
     * 单位：bps（位/秒）
     * 默认值：9600
     * 取值范围：1 ~ 20000
     * 用途：设置LIN通信的波特率
     */
    private int baudRate = 9600;

    /**
     * 当前触发类型
     * 取值范围：LIN_TRIGGER_SYNC_RISING_EDGE ~ LIN_TRIGGER_TYPE_MAX-1
     * 默认值：LIN_TRIGGER_SYNC_RISING_EDGE
     * 用途：指定当前使用的触发类型
     */
    private int triggerType = LIN_TRIGGER_SYNC_RISING_EDGE;

    /**
     * 帧ID数组
     * 长度：LIN_TRIGGER_TYPE_MAX
     * 用途：存储每种触发类型对应的帧ID值
     * 说明：不同触发类型可能需要不同的帧ID参数
     */
    private int []frameIds = new int[LIN_TRIGGER_TYPE_MAX];

    /**
     * LIN协议版本
     * 取值范围：LIN_TYPE_1_3、LIN_TYPE_1_3_LONG、LIN_TYPE_2_0
     * 默认值：LIN_TYPE_1_3
     * 用途：指定LIN协议版本
     */
    private int linType = LIN_TYPE_1_3;

    /**
     * 数据数组
     * 长度：LIN_TRIGGER_TYPE_MAX
     * 用途：存储每种触发类型对应的数据值
     * 说明：用于ID_AND_DATA和DATA触发类型
     */
    private long []data = new long[LIN_TRIGGER_TYPE_MAX];

    /**
     * 数据长度
     * 单位：位
     * 默认值：16
     * 用途：指定数据触发输入的有效位数
     * 取值范围：1 ~ 64
     */
    private int dataLength = 16;

    // ==================== 静态方法 ====================
    
    /**
     * 验证触发类型是否有效
     * <p>
     * 功能：检查触发类型是否在有效范围内
     * 范围：LIN_TRIGGER_SYNC_RISING_EDGE ~ LIN_TRIGGER_TYPE_MAX-1
     * 
     * @param triggerType 要验证的触发类型
     * @return true-有效，false-无效
     */
    public static boolean isTriggerTypeValid(int triggerType){
        // 检查触发类型是否在有效范围内
        return triggerType>=LIN_TRIGGER_SYNC_RISING_EDGE && triggerType<LIN_TRIGGER_TYPE_MAX;
    }

    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * <p>
     * 功能：创建LIN总线实例，初始化总线索引和类型
     * 
     * @param busIdx 总线索引（0表示第一个LIN总线）
     */
    public LinBus(int busIdx) {
        super(busIdx,LIN); // 调用父类构造函数，设置总线索引和类型为LIN
    }

    // ==================== LIN协议版本配置方法 ====================
    
    /**
     * 设置LIN协议版本
     * <p>
     * 功能：设置LIN协议版本（LIN 1.3、LIN 1.3长帧、LIN 2.0）
     * 副作用：调用busTypeChange()通知父类协议版本变更
     * 
     * @param linType LIN协议版本（LIN_TYPE_1_3/LIN_TYPE_1_3_LONG/LIN_TYPE_2_0）
     */
    public void setLinType(int linType){
        this.linType = linType; // 设置LIN协议版本
        busTypeChange(); // 通知父类总线类型变更
    }

    /**
     * 获取LIN协议版本
     * 
     * @return LIN协议版本（LIN_TYPE_1_3/LIN_TYPE_1_3_LONG/LIN_TYPE_2_0）
     */
    public int getLinType(){
        return linType; // 返回LIN协议版本
    }

    // ==================== 通道配置方法 ====================
    
    /**
     * 设置LIN信号通道索引
     * <p>
     * 功能：设置LIN信号连接的物理通道
     * 副作用：调用chChange()通知父类通道配置变更
     * 
     * @param srcChIdx LIN信号通道索引
     */
    public void setSrcChIdx(int srcChIdx){
        this.srcChIdx = srcChIdx; // 设置LIN信号通道索引
        chChange(); // 通知父类通道配置变更
    }

    /**
     * 获取LIN信号通道索引
     * 
     * @return LIN信号通道索引
     */
    public int getSrcChIdx(){
        return srcChIdx; // 返回LIN信号通道索引
    }

    // ==================== 空闲电平配置方法 ====================
    
    /**
     * 设置空闲电平
     * <p>
     * 功能：设置LIN总线空闲状态时的电平极性
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

    // ==================== 波特率配置方法 ====================
    
    /**
     * 设置波特率
     * <p>
     * 功能：设置LIN通信的波特率
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param baudRate 波特率（单位：bps，范围：1-20000）
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

    // ==================== 触发类型配置方法 ====================
    
    /**
     * 获取当前触发类型
     * 
     * @return 当前触发类型（LIN_TRIGGER_SYNC_RISING_EDGE ~ LIN_TRIGGER_TYPE_MAX-1）
     */
    public int getTriggerType() {
        return triggerType; // 返回当前触发类型
    }

    /**
     * 设置触发类型
     * <p>
     * 功能：设置当前使用的触发类型
     * 验证：调用isTriggerTypeValid()验证类型有效性
     * 副作用：验证通过后调用busChange()通知父类配置变更
     * 
     * @param triggerType 触发类型
     */
    public void setTriggerType(int triggerType) {
        // 验证触发类型是否有效
        if(isTriggerTypeValid(triggerType)) {
            this.triggerType = triggerType; // 设置触发类型
            busChange(); // 通知父类总线配置变更
        }
    }

    // ==================== 帧ID配置方法 ====================
    
    /**
     * 获取指定触发类型的帧ID
     * <p>
     * 功能：获取frameIds数组中指定触发类型对应的帧ID
     * 验证：调用isTriggerTypeValid()验证类型有效性
     * 
     * @param triggerType 触发类型
     * @return 指定触发类型的帧ID，无效类型返回0
     */
    public int getFrameId(int triggerType) {
        // 验证触发类型是否有效
        if(isTriggerTypeValid(triggerType))
            return frameIds[triggerType]; // 返回指定触发类型的帧ID
        return 0; // 无效类型返回0
    }

    /**
     * 设置指定触发类型的帧ID
     * <p>
     * 功能：设置frameIds数组中指定触发类型对应的帧ID
     * 验证：调用isTriggerTypeValid()验证类型有效性
     * 副作用：验证通过后调用busChange()通知父类配置变更
     * 
     * @param triggerType 触发类型
     * @param frameId 帧ID（范围：0x00-0x3F，LIN帧ID为6位）
     */
    public void setFrameId(int triggerType,int frameId) {
        // 验证触发类型是否有效
        if(isTriggerTypeValid(triggerType)) {
            frameIds[triggerType] = frameId; // 设置指定触发类型的帧ID
            busChange(); // 通知父类总线配置变更
        }
    }

    // ==================== 数据配置方法 ====================
    
    /**
     * 获取当前触发类型的数据
     * <p>
     * 功能：获取data数组中当前触发类型对应的数据
     * 
     * @return 当前触发类型的数据
     */
    public long getData() {
        return data[triggerType]; // 返回当前触发类型的数据
    }

    /**
     * 设置当前触发类型的数据
     * <p>
     * 功能：设置data数组中当前触发类型对应的数据
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param data 数据值
     */
    public void setData(long data) {
        this.data[triggerType] = data; // 设置当前触发类型的数据
        busChange(); // 通知父类总线配置变更
    }

    /**
     * 设置指定触发类型的数据
     * <p>
     * 功能：设置data数组中指定触发类型对应的数据
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param triggerType 触发类型
     * @param data 数据值
     */
    public void setData(int triggerType,int data){
        this.data[triggerType] = data; // 设置指定触发类型的数据
        busChange(); // 通知父类总线配置变更
    }

    /**
     * 获取指定触发类型的数据
     * <p>
     * 功能：获取data数组中指定触发类型对应的数据
     * 
     * @param triggerType 触发类型
     * @return 指定触发类型的数据
     */
    public long getData(int triggerType){
        return data[triggerType]; // 返回指定触发类型的数据
    }

    // ==================== 数据长度配置方法 ====================
    
    /**
     * 获取数据长度
     * 
     * @return 数据长度（单位：位）
     */
    public int getDataLength() {
        return dataLength; // 返回数据长度
    }

    /**
     * 设置数据长度
     * <p>
     * 功能：设置数据触发输入的有效位数
     * 说明：不触发变更通知，仅设置本地值
     * 
     * @param dataLength 数据长度（单位：位，范围：1-64）
     */
    public void setDataLength(int dataLength) {
        this.dataLength = dataLength; // 设置数据长度
    }

    // ==================== IBus抽象方法实现 ====================
    
    /**
     * 获取采样通道数量
     * <p>
     * 功能：返回LIN总线需要的采样通道数量
     * LIN协议只需要1个通道：LIN信号线
     * 
     * @return 采样通道数量（固定为1）
     */
    @Override
    public int getChSampleCnt() {
        return 1; // LIN只需要1个通道
    }

    /**
     * 检查指定通道是否在采样范围内
     * <p>
     * 功能：判断指定通道是否为LIN信号通道
     * 用途：通道管理器判断通道是否被LIN总线占用
     * 
     * @param chIdx 通道索引
     * @return true-该通道被LIN使用，false-该通道未被LIN使用
     */
    @Override
    public boolean isChInSample(int chIdx) {
        // 检查通道是否为LIN信号通道
        return srcChIdx == chIdx;
    }
}
