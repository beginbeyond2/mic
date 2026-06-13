package com.micsig.tbook.scope.Bus; // 包声明：I2C总线类所属包路径

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                          I2CBus 类说明文档                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   I2C总线触发配置类 - MHO系列示波器串行总线解码系统的I2C协议触发配置组件       │
 * │   继承自IBus基类，负责I2C总线触发参数的存储和管理                              │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 触发类型定义：定义9种I2C触发类型常量                                     │
 * │      - START_CONDITION：起始条件触发                                        │
 * │      - STOP_CONDITION：停止条件触发                                         │
 * │      - ACK_LOST：ACK丢失触发                                                │
 * │      - ADDRESS_NO_ACK：地址无应答触发                                       │
 * │      - RESTART：重启条件触发                                                │
 * │      - EEPROM_READ_DATA：EEPROM读数据触发                                   │
 * │      - FRAME1：帧1触发                                                      │
 * │      - FRAME2：帧2触发                                                      │
 * │      - WRITE_FRAME：写帧触发                                                │
 * │   2. 通道配置：管理SDA和SCL两个信号通道的索引                                 │
 * │   3. 触发参数配置：管理触发类型、触发关系、触发地址、触发数据等参数             │
 * │   4. 变更通知：通过chChange()和busChange()方法通知父类配置变更                │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                      I2CBus (继承IBus)                           │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发类型常量层                          │   │       │
 * │   │  │  START_CONDITION │ STOP_CONDITION │ ACK_LOST │ ...       │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    通道配置层                              │   │       │
 * │   │  │  sdaChIdx (SDA通道索引)  │  sclChIdx (SCL通道索引)        │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    触发参数配置层                          │   │       │
 * │   │  │  triggerType │ triggerRelation │ triggerAddrs[] │         │   │       │
 * │   │  │  triggerData1s[] │ triggerData2                             │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    变更通知层                              │   │       │
 * │   │  │  chChange() (通道变更)  │  busChange() (总线变更)         │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【I2C协议说明】                                                              │
 * │   I2C（Inter-Integrated Circuit）是一种两线式串行总线协议：                   │
 * │   - SDA（Serial Data）：串行数据线，用于传输数据                             │
 * │   - SCL（Serial Clock）：串行时钟线，用于同步数据传输                        │
 * │   - 支持多主多从架构                                                         │
 * │   - 每个设备有唯一的7位或10位地址                                            │
 * │                                                                             │
 * │ 【触发类型详解】                                                             │
 * │   1. START_CONDITION：检测到起始条件（SCL高电平时SDA下降沿）                  │
 * │   2. STOP_CONDITION：检测到停止条件（SCL高电平时SDA上升沿）                   │
 * │   3. ACK_LOST：检测到ACK丢失（主机未收到从机应答）                            │
 * │   4. ADDRESS_NO_ACK：检测到地址无应答（从机未响应地址）                       │
 * │   5. RESTART：检测到重启条件（重复起始条件）                                  │
 * │   6. EEPROM_READ_DATA：检测到EEPROM读数据操作                               │
 * │   7. FRAME1：检测到帧1（自定义帧格式）                                       │
 * │   8. FRAME2：检测到帧2（自定义帧格式）                                       │
 * │   9. WRITE_FRAME：检测到写帧操作                                            │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   用户配置 → setTriggerType() → busChange() → IBus父类 → FPGA寄存器        │
 * │   用户配置 → setSdaChIdx() → chChange() → IBus父类 → 通道管理器             │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   父类依赖：IBus（总线基类，提供chChange()和busChange()方法）                │
 * │   下游依赖：FPGA寄存器配置、触发系统、解码系统                                │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 创建I2C总线实例                                                         │
 * │   I2CBus i2cBus = new I2CBus(0);                                            │
 * │   // 设置SDA和SCL通道                                                        │
 * │   i2cBus.setSdaChIdx(0);                                                    │
 * │   i2cBus.setSclChIdx(1);                                                    │
 * │   // 设置触发类型为起始条件                                                   │
 * │   i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_START_CONDITION);                │
 * │   // 设置触发地址                                                            │
 * │   i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_START_CONDITION, 0x50);         │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. triggerAddrs和triggerData1s数组长度为I2C_TRIGGER_TYPE_MAX              │
 * │   2. 设置触发类型前应调用isTriggerTypeVaild()验证有效性                       │
 * │   3. SDA和SCL通道变更会触发chChange()通知父类                                 │
 * │   4. 触发参数变更会触发busChange()通知父类                                    │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018-5-29                                              │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * I2C总线触发配置类
 * <p>
 * 继承自IBus基类，负责I2C总线触发参数的存储和管理。
 * 支持多种I2C触发类型，包括起始条件、停止条件、ACK丢失等。
 * <p>
 * 核心功能：
 * - 定义I2C触发类型常量
 * - 管理SDA和SCL通道配置
 * - 管理触发参数（类型、关系、地址、数据）
 * - 提供参数变更通知机制
 */
public class I2CBus extends IBus {
    
    // ==================== 触发类型常量定义 ====================
    
    /**
     * 起始条件触发
     * 触发条件：SCL高电平时SDA下降沿
     * 用途：捕获I2C通信的开始
     */
    public static final int I2C_TRIGGER_START_CONDITION = 0;

    /**
     * 停止条件触发
     * 触发条件：SCL高电平时SDA上升沿
     * 用途：捕获I2C通信的结束
     */
    public static final int I2C_TRIGGER_STOP_CONDITION = 1;

    /**
     * ACK丢失触发
     * 触发条件：主机未收到从机应答信号
     * 用途：检测通信异常
     */
    public static final int I2C_TRIGGER_ACK_LOST = 2;

    /**
     * 地址无应答触发
     * 触发条件：从机未响应地址字节
     * 用途：检测从机不存在或地址错误
     */
    public static final int I2C_TRIGGER_ADDRESS_NO_ACK = 3;

    /**
     * 重启条件触发
     * 触发条件：重复起始条件
     * 用途：捕获I2C通信的方向切换
     */
    public static final int I2C_TRIGGER_RESTART = 4;

    /**
     * EEPROM读数据触发
     * 触发条件：EEPROM读数据操作
     * 用途：捕获EEPROM读取操作
     */
    public static final int I2C_TRIGGER_EEPROM_READ_DATA = 5;

    /**
     * 帧1触发
     * 触发条件：自定义帧格式1
     * 用途：用户自定义帧触发
     */
    public static final int I2C_TRIGGER_FRAME1 = 6;

    /**
     * 帧2触发
     * 触发条件：自定义帧格式2
     * 用途：用户自定义帧触发
     */
    public static final int I2C_TRIGGER_FRAME2 = 7;

    /**
     * 写帧触发
     * 触发条件：写帧操作
     * 用途：捕获I2C写操作
     */
    public static final int I2C_TRIGGER_WRITE_FRAME = 8;

    /**
     * 触发类型最大值
     * 用途：数组长度定义和边界检查
     */
    public static final int I2C_TRIGGER_TYPE_MAX = 9;

    // ==================== 成员变量定义 ====================
    
    /**
     * SDA（Serial Data）通道索引
     * 取值范围：0 ~ 通道数-1
     * 用途：指定SDA信号连接的物理通道
     */
    private int sdaChIdx = 0;

    /**
     * SCL（Serial Clock）通道索引
     * 取值范围：0 ~ 通道数-1
     * 用途：指定SCL信号连接的物理通道
     */
    private int sclChIdx = 0;

    /**
     * 当前触发类型
     * 取值范围：I2C_TRIGGER_START_CONDITION ~ I2C_TRIGGER_TYPE_MAX-1
     * 默认值：I2C_TRIGGER_START_CONDITION
     * 用途：指定当前使用的触发类型
     */
    private int triggerType = I2C_TRIGGER_START_CONDITION;

    /**
     * 触发关系
     * 取值范围：TRIGGER_RELATION_EQUAL、TRIGGER_RELATION_GREATER等（继承自IBus）
     * 用途：指定触发条件的比较关系
     */
    private int triggerRelation = TRIGGER_RELATION_EQUAL;

    /**
     * 触发地址数组
     * 长度：I2C_TRIGGER_TYPE_MAX
     * 用途：存储每种触发类型对应的地址值
     * 说明：不同触发类型可能需要不同的地址参数
     */
    private int []triggerAddrs = new int[I2C_TRIGGER_TYPE_MAX];

    /**
     * 触发数据1数组
     * 长度：I2C_TRIGGER_TYPE_MAX
     * 用途：存储每种触发类型对应的数据值
     * 说明：不同触发类型可能需要不同的数据参数
     */
    private int []triggerData1s = new int[I2C_TRIGGER_TYPE_MAX];

    /**
     * 触发数据2
     * 用途：存储额外的触发数据参数
     * 说明：某些触发类型可能需要两个数据参数
     */
    private int triggerData2 = 0;

    // ==================== 静态方法 ====================
    
    /**
     * 验证触发类型是否有效
     * <p>
     * 功能：检查触发类型是否在有效范围内
     * 范围：I2C_TRIGGER_START_CONDITION ~ I2C_TRIGGER_TYPE_MAX-1
     * 
     * @param triggerType 要验证的触发类型
     * @return true-有效，false-无效
     */
    public static boolean isTriggerTypeVaild(int triggerType){
        // 检查触发类型是否在有效范围内
        return triggerType>= I2C_TRIGGER_START_CONDITION && triggerType<I2C_TRIGGER_TYPE_MAX;
    }

    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * <p>
     * 功能：创建I2C总线实例，初始化总线索引和类型
     * 
     * @param busIdx 总线索引（0表示第一个I2C总线）
     */
    public I2CBus(int busIdx) {
        super(busIdx,I2C); // 调用父类构造函数，设置总线索引和类型为I2C
    }

    // ==================== SDA通道配置方法 ====================
    
    /**
     * 获取SDA通道索引
     * 
     * @return SDA通道索引
     */
    public int getSdaChIdx() {
        return sdaChIdx; // 返回SDA通道索引
    }

    /**
     * 设置SDA通道索引
     * <p>
     * 功能：设置SDA信号连接的物理通道
     * 副作用：调用chChange()通知父类通道配置变更
     * 
     * @param sdaChIdx SDA通道索引
     */
    public void setSdaChIdx(int sdaChIdx) {
        this.sdaChIdx = sdaChIdx; // 设置SDA通道索引
        chChange(); // 通知父类通道配置变更
    }

    // ==================== SCL通道配置方法 ====================
    
    /**
     * 获取SCL通道索引
     * 
     * @return SCL通道索引
     */
    public int getSclChIdx() {
        return sclChIdx; // 返回SCL通道索引
    }

    /**
     * 设置SCL通道索引
     * <p>
     * 功能：设置SCL信号连接的物理通道
     * 副作用：调用chChange()通知父类通道配置变更
     * 
     * @param sclChIdx SCL通道索引
     */
    public void setSclChIdx(int sclChIdx) {
        this.sclChIdx = sclChIdx; // 设置SCL通道索引
        chChange(); // 通知父类通道配置变更
    }

    // ==================== 触发类型配置方法 ====================
    
    /**
     * 获取当前触发类型
     * 
     * @return 当前触发类型（I2C_TRIGGER_START_CONDITION ~ I2C_TRIGGER_TYPE_MAX-1）
     */
    public int getTriggerType() {
        return triggerType; // 返回当前触发类型
    }

    /**
     * 设置触发类型
     * <p>
     * 功能：设置当前使用的触发类型
     * 验证：调用isTriggerTypeVaild()验证类型有效性
     * 副作用：验证通过后调用busChange()通知父类配置变更
     * 
     * @param triggerType 触发类型
     */
    public void setTriggerType(int triggerType) {
        // 验证触发类型是否有效
        if(isTriggerTypeVaild(triggerType)) {
            this.triggerType = triggerType; // 设置触发类型
            busChange(); // 通知父类总线配置变更
        }
    }

    // ==================== 触发关系配置方法 ====================
    
    /**
     * 获取触发关系
     * 
     * @return 触发关系（TRIGGER_RELATION_EQUAL等）
     */
    public int getTriggerRelation() {
        return triggerRelation; // 返回触发关系
    }

    /**
     * 设置触发关系
     * <p>
     * 功能：设置触发条件的比较关系
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param triggerRelation 触发关系
     */
    public void setTriggerRelation(int triggerRelation) {
        this.triggerRelation = triggerRelation; // 设置触发关系
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 触发数据2配置方法 ====================
    
    /**
     * 获取触发数据2
     * 
     * @return 触发数据2
     */
    public int getTriggerData2() {
        return triggerData2; // 返回触发数据2
    }

    /**
     * 设置触发数据2
     * <p>
     * 功能：设置额外的触发数据参数
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param triggerData2 触发数据2
     */
    public void setTriggerData2(int triggerData2) {
        this.triggerData2 = triggerData2; // 设置触发数据2
        busChange(); // 通知父类总线配置变更
    }

    // ==================== 触发数据1配置方法 ====================
    
    /**
     * 获取当前触发类型的触发数据1
     * <p>
     * 功能：获取triggerData1s数组中当前触发类型对应的数据
     * 
     * @return 当前触发类型的触发数据1
     */
    public int getTriggerData1() {
        return triggerData1s[triggerType]; // 返回当前触发类型的数据1
    }

    /**
     * 设置当前触发类型的触发数据1
     * <p>
     * 功能：设置triggerData1s数组中当前触发类型对应的数据
     * 副作用：调用busChange()通知父类配置变更
     * 
     * @param triggerData1 触发数据1
     */
    public void setTriggerData1(int triggerData1) {
        this.triggerData1s[triggerType] = triggerData1; // 设置当前触发类型的数据1
        busChange(); // 通知父类总线配置变更
    }

    /**
     * 获取指定触发类型的触发数据1
     * <p>
     * 功能：获取triggerData1s数组中指定触发类型对应的数据
     * 验证：调用isTriggerTypeVaild()验证类型有效性
     * 
     * @param triggerType 触发类型
     * @return 指定触发类型的触发数据1，无效类型返回0
     */
    public int getTriggerData1(int triggerType) {
        // 验证触发类型是否有效
        if(isTriggerTypeVaild(triggerType))
            return triggerData1s[triggerType]; // 返回指定触发类型的数据1
        return 0; // 无效类型返回0
    }

    /**
     * 设置指定触发类型的触发数据1
     * <p>
     * 功能：设置triggerData1s数组中指定触发类型对应的数据
     * 验证：调用isTriggerTypeVaild()验证类型有效性
     * 副作用：验证通过后调用busChange()通知父类配置变更
     * 
     * @param triggerType 触发类型
     * @param triggerData1 触发数据1
     */
    public void setTriggerData1(int triggerType,int triggerData1) {
        // 验证触发类型是否有效
        if(isTriggerTypeVaild(triggerType)) {
            this.triggerData1s[triggerType] = triggerData1; // 设置指定触发类型的数据1
            busChange(); // 通知父类总线配置变更
        }
    }

    // ==================== 触发地址配置方法 ====================
    
    /**
     * 获取指定触发类型的触发地址
     * <p>
     * 功能：获取triggerAddrs数组中指定触发类型对应的地址
     * 验证：调用isTriggerTypeVaild()验证类型有效性
     * 
     * @param triggerType 触发类型
     * @return 指定触发类型的触发地址，无效类型返回0
     */
    public int getTriggerAddr(int triggerType) {
        // 验证触发类型是否有效
        if(isTriggerTypeVaild(triggerType))
            return triggerAddrs[triggerType]; // 返回指定触发类型的地址
        return 0; // 无效类型返回0
    }

    /**
     * 设置指定触发类型的触发地址
     * <p>
     * 功能：设置triggerAddrs数组中指定触发类型对应的地址
     * 验证：调用isTriggerTypeVaild()验证类型有效性
     * 副作用：验证通过后调用busChange()通知父类配置变更
     * 
     * @param triggerType 触发类型
     * @param triggerAddr 触发地址
     */
    public void setTriggerAddrs(int triggerType,int triggerAddr) {
        // 验证触发类型是否有效
        if(isTriggerTypeVaild(triggerType)) {
            this.triggerAddrs[triggerType] = triggerAddr; // 设置指定触发类型的地址
            busChange(); // 通知父类总线配置变更
        }
    }

    // ==================== IBus抽象方法实现 ====================
    
    /**
     * 获取采样通道数量
     * <p>
     * 功能：返回I2C总线需要的采样通道数量
     * I2C协议需要2个通道：SDA和SCL
     * 
     * @return 采样通道数量（固定为2）
     */
    @Override
    public int getChSampleCnt() {
        return 2; // I2C需要SDA和SCL两个通道
    }

    /**
     * 检查指定通道是否在采样范围内
     * <p>
     * 功能：判断指定通道是否为SDA或SCL通道
     * 用途：通道管理器判断通道是否被I2C总线占用
     * 
     * @param chIdx 通道索引
     * @return true-该通道被I2C使用，false-该通道未被I2C使用
     */
    @Override
    public boolean isChInSample(int chIdx) {
        // 检查通道是否为SDA或SCL通道
        return sdaChIdx == chIdx || sclChIdx == chIdx;
    }
}
