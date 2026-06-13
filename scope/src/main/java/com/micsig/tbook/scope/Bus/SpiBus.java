package com.micsig.tbook.scope.Bus; // 包声明：SPI总线类所属包路径

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                          SpiBus 类说明文档                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   SPI总线触发配置类 - MHO系列示波器串行总线解码系统的SPI协议触发配置组件       │
 * │   继承自IBus基类，负责SPI总线触发参数的存储和管理                              │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 触发类型定义：定义3种SPI触发类型常量                                     │
 * │      - FRAME_CS：片选帧触发                                                 │
 * │      - FRAME_DATA：数据帧触发                                               │
 * │      - FRAME_X_DATA：扩展数据帧触发                                         │
 * │   2. 时钟采样边沿配置：支持上升沿和下降沿采样                                  │
 * │   3. 通道配置：管理CLK、DATA、CS三个信号通道的索引                            │
 * │   4. 触发参数配置：管理触发类型、触发掩码、触发数据等参数                       │
 * │   5. 变更通知：通过chChange()和busChange()通知父类配置变更                    │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                      SpiBus (继承IBus)                           │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发类型常量层                          │   │       │
 * │   │  │  FRAME_CS │ FRAME_DATA │ FRAME_X_DATA                    │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    时钟采样边沿常量层                       │   │       │
 * │   │  │  SPI_CLK_RISE_EDGE │ SPI_CLK_FALL_EDGE                    │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    通道配置层                              │   │       │
 * │   │  │  clkChIdx (时钟通道) │ dataChIdx (数据通道) │ csChIdx (片选)│   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发参数配置层                          │   │       │
 * │   │  │  triggerType │ triggerMask │ triggerData │ bits │         │   │       │
 * │   │  │  clkSample │ dataIdleLevel │ csValid │ csIdleLevel       │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    变更通知层                              │   │       │
 * │   │  │  chChange() │ busChange()                                │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【SPI协议说明】                                                              │
 * │   SPI（Serial Peripheral Interface）是一种高速全双工同步串行通信协议：        │
 * │   - 四线制：SCK（时钟）、MOSI（主出从入）、MISO（主入从出）、CS（片选）        │
 * │   - 全双工：同时发送和接收数据                                               │
 * │   - 主从模式：一个主设备控制一个或多个从设备                                  │
 * │   - 高速：可达数MHz甚至数十MHz                                              │
 * │   - 灵活：支持多种时钟极性和相位配置                                         │
 * │                                                                             │
 * │ 【SPI信号线】                                                                │
 * │   1. SCK（Serial Clock）：串行时钟，由主设备产生                             │
 * │   2. MOSI（Master Out Slave In）：主设备输出，从设备输入                     │
 * │   3. MISO（Master In Slave Out）：主设备输入，从设备输出                     │
 * │   4. CS（Chip Select）：片选信号，低电平有效                                 │
 * │                                                                             │
 * │ 【触发类型详解】                                                             │
 * │   1. FRAME_CS：检测到片选信号有效时的帧                                      │
 * │   2. FRAME_DATA：检测到指定数据值的帧                                       │
 * │   3. FRAME_X_DATA：检测到扩展数据值的帧                                     │
 * │                                                                             │
 * │ 【时钟采样边沿】                                                             │
 * │   1. SPI_CLK_RISE_EDGE：在时钟上升沿采样数据                                 │
 * │   2. SPI_CLK_FALL_EDGE：在时钟下降沿采样数据                                 │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   用户配置 → setTriggerType() → busChange() → IBus父类 → FPGA寄存器        │
 * │   用户配置 → setClkChIdx() → chChange() → IBus父类 → 通道管理器             │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   父类依赖：IBus（总线基类，提供chChange()和busChange()方法）                │
 * │   下游依赖：FPGA寄存器配置、触发系统、解码系统                                │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 创建SPI总线实例                                                         │
 * │   SpiBus spiBus = new SpiBus(0);                                            │
 * │   // 设置时钟通道索引                                                        │
 * │   spiBus.setClkChIdx(0);                                                    │
 * │   // 设置数据通道索引                                                        │
 * │   spiBus.setDataChIdx(1);                                                   │
 * │   // 设置片选通道索引                                                        │
 * │   spiBus.setCsChIdx(2);                                                     │
 * │   // 启用片选信号                                                            │
 * │   spiBus.setCsValid(true);                                                  │
 * │   // 设置时钟采样边沿为上升沿                                                 │
 * │   spiBus.setClkSample(SpiBus.SPI_CLK_RISE_EDGE);                            │
 * │   // 设置数据位数为8位                                                       │
 * │   spiBus.setBits(8);                                                        │
 * │   // 设置触发类型为数据帧触发                                                 │
 * │   spiBus.setTriggerType(SpiBus.SPI_TRIGGER_FRAME_DATA);                     │
 * │   // 设置触发数据                                                            │
 * │   spiBus.setTriggerData(0x55);                                              │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. SPI总线需要2-3个采样通道（取决于是否启用CS）                             │
 * │   2. 片选信号CS是可选的，通过csValid控制                                      │
 * │   3. 数据位数可配置，通常为8位                                               │
 * │   4. 时钟采样边沿需与从设备配置一致                                          │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018-5-29                                              │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * SPI总线触发配置类
 * <p>
 * 继承自IBus基类，负责SPI总线触发参数的存储和管理。
 * 支持多种SPI触发类型，包括片选帧触发、数据帧触发、扩展数据帧触发等。
 * <p>
 * 核心功能：
 * - 定义SPI触发类型常量
 * - 定义时钟采样边沿常量
 * - 管理SPI通道配置（CLK、DATA、CS）
 * - 管理触发参数（类型、掩码、数据）
 * - 支持片选信号可选配置
 * - 提供参数变更通知机制
 */
public class SpiBus extends IBus {
    
    // ==================== 触发类型常量定义 ====================
    
    /**
     * 片选帧触发
     * 触发条件：检测到片选信号有效时的帧
     * 用途：捕获SPI通信的开始
     */
    public static final int SPI_TRIGGER_FRAME_CS = 1;

    /**
     * 数据帧触发
     * 触发条件：检测到指定数据值的帧
     * 用途：捕获特定数据的SPI帧
     */
    public static final int SPI_TRIGGER_FRAME_DATA = 2;

    /**
     * 扩展数据帧触发
     * 触发条件：检测到扩展数据值的帧
     * 用途：捕获扩展数据的SPI帧
     */
    public static final int SPI_TRIGGER_FRAME_X_DATA = 3;

    // ==================== 时钟采样边沿常量定义 ====================
    
    /**
     * 时钟上升沿采样
     * 用途：在时钟上升沿采样数据
     */
    public static final int SPI_CLK_RISE_EDGE = 0;

    /**
     * 时钟下降沿采样
     * 用途：在时钟下降沿采样数据
     */
    public static final int SPI_CLK_FALL_EDGE = 1;

    // ==================== 成员变量定义 ====================
    
    /**
     * 时钟通道索引
     * 取值范围：0 ~ 通道数-1
     * 默认值：0
     * 用途：指定SCK（时钟）信号连接的物理通道
     */
    private int clkChIdx = 0;

    /**
     * 数据通道索引
     * 取值范围：0 ~ 通道数-1
     * 默认值：1
     * 用途：指定MOSI或MISO信号连接的物理通道
     */
    private int dataChIdx = 1;

    /**
     * 片选通道索引
     * 取值范围：0 ~ 通道数-1
     * 默认值：2
     * 用途：指定CS（片选）信号连接的物理通道
     */
    private int csChIdx = 2;

    /**
     * 数据位数
     * 单位：位
     * 默认值：8
     * 取值范围：1 ~ 32
     * 用途：设置SPI数据帧的位数
     */
    private int bits = 8;

    /**
     * 时钟采样边沿
     * 取值范围：SPI_CLK_RISE_EDGE、SPI_CLK_FALL_EDGE
     * 默认值：SPI_CLK_RISE_EDGE
     * 用途：指定在时钟的哪个边沿采样数据
     */
    private int clkSample = SPI_CLK_RISE_EDGE;

    /**
     * 数据空闲电平
     * 取值范围：IDLE_LEVEL_HIGH、IDLE_LEVEL_LOW（继承自IBus）
     * 默认值：IDLE_LEVEL_HIGH
     * 用途：定义数据线空闲状态时的电平极性
     */
    private int dataIdleLevel = IDLE_LEVEL_HIGH;

    /**
     * 片选信号是否有效
     * 默认值：false
     * 用途：控制是否使用片选信号
     * 影响：启用后采样通道数从2变为3
     */
    private boolean csValid = false;

    /**
     * 片选空闲电平
     * 取值范围：IDLE_LEVEL_HIGH、IDLE_LEVEL_LOW（继承自IBus）
     * 默认值：IDLE_LEVEL_HIGH
     * 用途：定义片选线空闲状态时的电平极性
     */
    private int csIdleLevel = IDLE_LEVEL_HIGH;

    /**
     * 当前触发类型
     * 取值范围：SPI_TRIGGER_FRAME_CS ~ SPI_TRIGGER_FRAME_X_DATA
     * 默认值：SPI_TRIGGER_FRAME_CS
     * 用途：指定当前使用的触发类型
     */
    private int triggerType = SPI_TRIGGER_FRAME_CS;

    /**
     * 触发掩码
     * 用途：设置触发数据的掩码，用于屏蔽某些位
     * 说明：与triggerData配合使用，实现位级别的触发条件
     */
    private int triggerMask = 0;

    /**
     * 触发数据
     * 用途：设置触发数据值，用于FRAME_DATA和FRAME_X_DATA触发类型
     * 说明：与triggerMask配合使用
     */
    private int triggerData = 0;

    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * <p>
     * 功能：创建SPI总线实例，初始化总线索引和类型
     * 
     * @param busIdx 总线索引（0表示第一个SPI总线）
     */
    public SpiBus(int busIdx) {
        super(busIdx,SPI); // 调用父类构造函数，设置总线索引和类型为SPI
    }

    // ==================== 时钟通道配置方法 ====================
    
    /**
     * 获取时钟通道索引
     * 
     * @return 时钟通道索引
     */
    public int getClkChIdx() {
        return clkChIdx; // 返回时钟通道索引
    }

    /**
     * 设置时钟通道索引
     * <p>
     * 功能：设置SCK（时钟）信号连接的物理通道
     * 副作用：调用chChange()通知父类通道配置变更
     * 
     * @param clkChIdx 时钟通道索引
     */
    public void setClkChIdx(int clkChIdx) {
        this.clkChIdx = clkChIdx; // 设置时钟通道索引
        chChange(); // 通知父类通道配置变更
    }

    // ==================== 数据通道配置方法 ====================
    
    /**
     * 获取数据通道索引
     * 
     * @return 数据通道索引
     */
    public int getDataChIdx() {
        return dataChIdx; // 返回数据通道索引
    }

    /**
     * 设置数据通道索引
     * <p>
     * 功能：设置MOSI或MISO信号连接的物理通道
     * 副作用：调用chChange()通知父类通道配置变更
     * 
     * @param dataChIdx 数据通道索引
     */
    public void setDataChIdx(int dataChIdx) {
        this.dataChIdx = dataChIdx; // 设置数据通道索引
        chChange(); // 通知父类通道配置变更
    }

    // ==================== 片选通道配置方法 ====================
    
    /**
     * 获取片选通道索引
     * 
     * @return 片选通道索引
     */
    public int getCsChIdx() {
        return csChIdx; // 返回片选通道索引
    }

    /**
     * 设置片选通道索引
     * <p>
     * 功能：设置CS（片选）信号连接的物理通道
     * 副作用：调用chChange()通知父类通道配置变更
     * 
     * @param csChIdx 片选通道索引
     */
    public void setCsChIdx(int csChIdx) {
        this.csChIdx = csChIdx; // 设置片选通道索引
        chChange(); // 通知父类通道配置变更
    }

    // ==================== 数据位数配置方法 ====================
    
    /**
     * 获取数据位数
     * 
     * @return 数据位数（单位：位）
     */
    public int getBits() {
        return bits; // 返回数据位数
    }

    /**
     * 设置数据位数
     * <p>
     * 功能：设置SPI数据帧的位数
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param bits 数据位数（单位：位，范围：1-32）
     */
    public void setBits(int bits) {
        this.bits = bits; // 设置数据位数
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 时钟采样边沿配置方法 ====================
    
    /**
     * 获取时钟采样边沿
     * 
     * @return 时钟采样边沿（SPI_CLK_RISE_EDGE/SPI_CLK_FALL_EDGE）
     */
    public int getClkSample() {
        return clkSample; // 返回时钟采样边沿
    }

    /**
     * 设置时钟采样边沿
     * <p>
     * 功能：设置在时钟的哪个边沿采样数据
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param clkSample 时钟采样边沿（SPI_CLK_RISE_EDGE/SPI_CLK_FALL_EDGE）
     */
    public void setClkSample(int clkSample) {
        this.clkSample = clkSample; // 设置时钟采样边沿
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 数据空闲电平配置方法 ====================
    
    /**
     * 获取数据空闲电平
     * 
     * @return 数据空闲电平（IDLE_LEVEL_HIGH/IDLE_LEVEL_LOW）
     */
    public int getDataIdleLevel() {
        return dataIdleLevel; // 返回数据空闲电平
    }

    /**
     * 设置数据空闲电平
     * <p>
     * 功能：设置数据线空闲状态时的电平极性
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param dataIdleLevel 数据空闲电平（IDLE_LEVEL_HIGH/IDLE_LEVEL_LOW）
     */
    public void setDataIdleLevel(int dataIdleLevel) {
        this.dataIdleLevel = dataIdleLevel; // 设置数据空闲电平
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 片选有效性配置方法 ====================
    
    /**
     * 检查片选信号是否有效
     * 
     * @return true-片选有效，false-片选无效
     */
    public boolean isCsValid() {
        return csValid; // 返回片选有效性
    }

    /**
     * 设置片选信号是否有效
     * <p>
     * 功能：控制是否使用片选信号
     * 影响：启用后采样通道数从2变为3
     * 副作用：调用chChange()通知父类通道配置变更
     * 
     * @param csValid true-启用片选，false-禁用片选
     */
    public void setCsValid(boolean csValid) {
        this.csValid = csValid; // 设置片选有效性
        chChange(); // 通知父类通道配置变更
    }

    // ==================== 片选空闲电平配置方法 ====================
    
    /**
     * 获取片选空闲电平
     * 
     * @return 片选空闲电平（IDLE_LEVEL_HIGH/IDLE_LEVEL_LOW）
     */
    public int getCsIdleLevel() {
        return csIdleLevel; // 返回片选空闲电平
    }

    /**
     * 设置片选空闲电平
     * <p>
     * 功能：设置片选线空闲状态时的电平极性
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param csIdleLevel 片选空闲电平（IDLE_LEVEL_HIGH/IDLE_LEVEL_LOW）
     */
    public void setCsIdleLevel(int csIdleLevel) {
        this.csIdleLevel = csIdleLevel; // 设置片选空闲电平
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 触发类型配置方法 ====================
    
    /**
     * 获取当前触发类型
     * 
     * @return 当前触发类型（SPI_TRIGGER_FRAME_CS ~ SPI_TRIGGER_FRAME_X_DATA）
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

    // ==================== 触发掩码配置方法 ====================
    
    /**
     * 获取触发掩码
     * 
     * @return 触发掩码
     */
    public int getTriggerMask() {
        return triggerMask; // 返回触发掩码
    }

    /**
     * 设置触发掩码
     * <p>
     * 功能：设置触发数据的掩码，用于屏蔽某些位
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param triggerMask 触发掩码
     */
    public void setTriggerMask(int triggerMask) {
        this.triggerMask = triggerMask; // 设置触发掩码
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 触发数据配置方法 ====================
    
    /**
     * 获取触发数据
     * 
     * @return 触发数据
     */
    public int getTriggerData() {
        return triggerData; // 返回触发数据
    }

    /**
     * 设置触发数据
     * <p>
     * 功能：设置触发数据值
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param triggerData 触发数据
     */
    public void setTriggerData(int triggerData) {
        this.triggerData = triggerData; // 设置触发数据
        busChange(); // 通知父类总线配置变更
    }

    // ==================== IBus抽象方法实现 ====================
    
    /**
     * 获取采样通道数量
     * <p>
     * 功能：返回SPI总线需要的采样通道数量
     * 说明：根据csValid决定采样通道数
     *       - csValid=false：2个通道（CLK和DATA）
     *       - csValid=true：3个通道（CLK、DATA和CS）
     * 
     * @return 采样通道数量（2或3）
     */
    @Override
    public int getChSampleCnt() {
        // 检查片选是否有效
        if(csValid)
            return 3; // 启用片选，需要3个通道
        return 2; // 未启用片选，需要2个通道
    }

    /**
     * 检查指定通道是否在采样范围内
     * <p>
     * 功能：判断指定通道是否被SPI总线使用
     * 用途：通道管理器判断通道是否被SPI总线占用
     * 
     * @param chIdx 通道索引
     * @return true-该通道被SPI使用，false-该通道未被SPI使用
     */
    @Override
    public boolean isChInSample(int chIdx) {
        // 检查通道是否为时钟或数据通道
        boolean bSample = (clkChIdx == chIdx) || (dataChIdx == chIdx);
        // 如果片选有效，检查是否为片选通道
        if(csValid) {
            bSample = (bSample) || (csChIdx == chIdx);
        }
        return bSample; // 返回检查结果
    }
}
