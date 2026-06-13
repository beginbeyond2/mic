package com.micsig.tbook.scope.Trigger;

import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;

import java.util.Arrays;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         Trigger - 触发器抽象基类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块核心抽象类，定义示波器触发系统的统一接口和通用逻辑。              ║
 * ║   触发器是示波器的核心功能，用于在特定条件下捕获波形，实现波形的稳定显示。     ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 定义触发类型常量（边沿/脉宽/逻辑/超时/视频/斜率/第N边沿/串行触发）       ║
 * ║   2. 管理触发源（单触发源 vs 多触发源）                                       ║
 * ║   3. 提供触发电平访问接口                                                    ║
 * ║   4. 触发参数变更通知机制                                                    ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │   Trigger       │ ← 抽象基类                      ║
 * ║                          │   (abstract)    │                                 ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║         ┌───────────┬─────────────┼─────────────┬───────────┐               ║
 * ║         │           │             │             │           │               ║
 * ║         ▼           ▼             ▼             ▼           ▼               ║
 * ║   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐         ║
 * ║   │Edge      │ │PulseWidth│ │Logic     │ │Slope     │ │TimeOut   │         ║
 * ║   │边沿触发  │ │脉宽触发  │ │逻辑触发  │ │斜率触发  │ │超时触发  │         ║
 * ║   │(单源)    │ │(单源)    │ │(多源)    │ │(单源)    │ │(单源)    │         ║
 * ║   └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘         ║
 * ║         │                                                       │           ║
 * ║         ├─────────────┬─────────────┐                           │           ║
 * ║         ▼             ▼             ▼                           ▼           ║
 * ║   ┌──────────┐ ┌──────────┐ ┌──────────┐                 ┌──────────┐       ║
 * ║   │NEdge     │ │Video     │ │Runt      │                 │Bus       │       ║
 * ║   │第N边沿   │ │视频触发  │ │矮脉冲    │                 │串行触发  │       ║
 * ║   └──────────┘ └──────────┘ └──────────┘                 └──────────┘       ║
 * ║                                                                              ║
 * ║ 【触发类型说明】                                                             ║
 * ║   ┌────────────────┬─────────────────────────────────────────────────────┐  ║
 * ║   │ 类型           │ 说明                                                │  ║
 * ║   ├────────────────┼─────────────────────────────────────────────────────┤  ║
 * ║   │ 边沿触发       │ 信号上升沿/下降沿/双边沿触发，最常用的触发方式       │  ║
 * ║   │ 矮脉冲触发     │ 检测低于阈值的矮脉冲                                │  ║
 * ║   │ 脉宽触发       │ 检测特定宽度的脉冲                                  │  ║
 * ║   │ 逻辑触发       │ 多通道逻辑组合触发（AND/OR/NAND/NOR）               │  ║
 * ║   │ 超时触发       │ 信号在指定时间内无跳变时触发                        │  ║
 * ║   │ 视频触发       │ 视频信号行/场同步触发                               │  ║
 * ║   │ 斜率触发       │ 信号斜率满足条件时触发                              │  ║
 * ║   │ 第N边沿触发    │ 在第N个边沿后触发                                   │  ║
 * ║   │ 串行触发       │ 串行总线协议触发（S1-S4）                           │  ║
 * ║   └────────────────┴─────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【触发源管理】                                                               ║
 * ║   单触发源：triggerSource_one 保存当前触发源                                ║
 * ║             triggerSource_pre 保存切换前的触发源（用于外部触发恢复）        ║
 * ║   多触发源：triggerSource[] 数组保存多个触发源（如逻辑触发支持最多4通道）   ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → ChannelFactory: 获取通道实例和通道数量                                  ║
 * ║   → Channel: 获取通道的触发电平                                             ║
 * ║   → TriggerAction: 触发参数变更时发送FPGA消息                               ║
 * ║   → TriggerLevel: 触发电平管理                                              ║
 * ║   → TriggerFactory: 工厂类，管理所有触发器实例                              ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户设置触发类型时，TriggerFactory创建对应的触发器实例                 ║
 * ║   2. 用户选择触发源时，调用setTriggerSource()更新触发源                     ║
 * ║   3. 用户调整触发电平时，通过getTriggerLevel()访问通道的触发电平            ║
 * ║   4. 触发参数变更时，通过TriggerAction通知FPGA更新硬件配置                  ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   触发源变量未加同步保护，应在UI线程操作                                     ║
 * ║   触发电平通过Channel访问，Channel内部有同步机制                             ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018/3/20                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public abstract class Trigger {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发类型ID起始值
     * 用于触发类型ID的有效范围检查
     */
    public static final int TRIG_TYPE_START_ID = 0;

    /**
     * 边沿触发类型ID
     * 最常用的触发方式，检测信号的上升沿、下降沿或双边沿
     * 对应实现类：TriggerEdge
     */
    public static final int TRIG_TYPE_EDGE = TRIG_TYPE_START_ID;

    /**
     * 矮脉冲触发类型ID
     * 检测低于设定阈值的矮脉冲信号
     * 对应实现类：TriggerRunt
     */
    public static final int TRIG_TYPE_LOW_PULSE = 1;

    /**
     * 脉宽触发类型ID
     * 检测特定宽度的脉冲，可设置脉宽条件（大于/小于/等于/不等于）
     * 对应实现类：TriggerPulseWidth
     */
    public static final int TRIG_TYPE_PULSE = 2;

    /**
     * 逻辑触发类型ID
     * 多通道逻辑组合触发，支持AND/OR/NAND/NOR逻辑运算
     * 最多支持4个通道同时作为触发源
     * 对应实现类：TriggerLogic
     */
    public static final int TRIG_TYPE_LOGIC = 3;

    /**
     * 超时触发类型ID
     * 当信号在指定时间内没有发生跳变时触发
     * 对应实现类：TriggerTimeOut
     */
    public static final int TRIG_TYPE_TIMEOUT = 4;

    /**
     * 视频触发类型ID
     * 用于视频信号的行同步或场同步触发
     * 对应实现类：TriggerVideo
     */
    public static final int TRIG_TYPE_VIDEO = 5;

    /**
     * 斜率触发类型ID
     * 当信号斜率满足设定条件时触发
     * 对应实现类：TriggerSlope
     */
    public static final int TRIG_TYPE_SLOPE = 6;

    /**
     * 第N边沿触发类型ID
     * 在第N个边沿后触发，用于复杂信号分析
     * 对应实现类：TriggerNEdge
     */
    public static final int TRIG_TYPE_NEDGE = 7;

    /**
     * 串行触发1类型ID
     * 串行总线协议触发，对应串口S1
     * 对应实现类：TriggerBus
     */
    public static final int TRIG_TYPE_SERIAL1 = 8;

    /**
     * 串行触发2类型ID
     * 串行总线协议触发，对应串口S2
     * 对应实现类：TriggerBus
     */
    public static final int TRIG_TYPE_SERIAL2 = 9;

    /**
     * 串行触发3类型ID
     * 串行总线协议触发，对应串口S3
     * 对应实现类：TriggerBus
     */
    public static final int TRIG_TYPE_SERIAL3 = 10;

    /**
     * 串行触发4类型ID
     * 串行总线协议触发，对应串口S4
     * 对应实现类：TriggerBus
     */
    public static final int TRIG_TYPE_SERIAL4 = 11;

    /**
     * 触发类型ID最大值
     * 用于触发类型ID的有效范围检查，实际最大ID + 1
     */
    public static final int TRIG_TYPE_MAX_ID = 12;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发条件关系常量定义
    // 用于脉宽触发、逻辑触发等需要条件判断的触发类型
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发条件：小于
     * 用于脉宽触发、逻辑触发等
     */
    public static final int TRIGGER_RELATION_LESS_THAN = 0;

    /**
     * 触发条件：大于
     * 用于脉宽触发、逻辑触发等
     */
    public static final int TRIGGER_RELATION_MORE_THAN = 1;

    /**
     * 触发条件：等于
     * 用于脉宽触发、逻辑触发等
     */
    public static final int TRIGGER_RELATION_EQUAL = 2;

    /**
     * 触发条件：不等于
     * 用于脉宽触发、逻辑触发等
     */
    public static final int TRIGGER_RELATION_NOT_EQUAL = 3;

    /**
     * 触发条件：变到真值时触发
     * 用于逻辑触发，当逻辑条件从假变为真时触发
     */
    public static final int TRIGGER_RELATION_TRUE = 4;

    /**
     * 触发条件：变到假值时触发
     * 用于逻辑触发，当逻辑条件从真变为假时触发
     */
    public static final int TRIGGER_RELATION_FALSE = 5;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发源相关常量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发源最大数量
     * 等于通道总数（CH1-CH8 + MATH1-8 + REF1-8 + S1-4）
     */
    public static final int TRIG_SRC_MAX = ChannelFactory.CH_CNT;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时间转换常量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * FPGA时间单位：8纳秒
     * FPGA内部时钟周期，用于时间值到FPGA单位的转换
     */
    public static final int TRIG_TIME_UNIT = 8;

    /**
     * 将时间值转换为FPGA单位
     * FPGA内部使用8ns为基本时间单位，需要将纳秒值转换为FPGA时钟周期数
     *
     * @param val 时间值（纳秒）
     * @return FPGA时间单位值（时钟周期数）
     *
     * @example
     *   triggerTime2FpgaUnit(100) = 100 / 8 = 12 (个8ns周期)
     */
    public static long triggerTime2FpgaUnit(long val){
        return val/TRIG_TIME_UNIT;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发源管理成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 保存选中外部触发之前的触发源
     * 用于从外部触发恢复到之前的触发源
     *
     * @example
     *   用户设置CH1为触发源 → triggerSource_pre = CH1
     *   用户切换到外部触发 → triggerSource_pre 保持CH1
     *   用户从外部触发切回 → 恢复到CH1
     */
    private static int triggerSource_pre;

    /**
     * 单触发源模式下保存当前触发源对应的通道号
     * 用于边沿触发、脉宽触发等只需要一个触发源的类型
     *
     * @see #getTriggerSource()
     * @see #setTriggerSource(int)
     */
    private static int triggerSource_one;

    /**
     * 多触发源模式下保存触发源对应的通道号数组
     * 用于逻辑触发等需要多个触发源的类型
     * 数组索引对应触发源位置，值为通道索引
     * -1表示该位置未配置触发源
     *
     * @example
     *   逻辑触发配置CH1和CH3为触发源：
     *   triggerSource[0] = 0 (CH1)
     *   triggerSource[1] = 2 (CH3)
     *   triggerSource[2] = -1 (未配置)
     *   triggerSource[3] = -1 (未配置)
     */
    private int []triggerSource =new int[ChannelFactory.CH_CNT];

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发器核心成员
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发器动作代理
     * 负责在触发参数变更时发送FPGA消息和事件通知
     *
     * @see TriggerAction
     */
    private TriggerAction triggerAction;

    /**
     * 当前触发类型
     * 取值范围：TRIG_TYPE_START_ID ~ TRIG_TYPE_MAX_ID
     */
    private int triggerType = TRIG_TYPE_START_ID;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造触发器实例
     * 初始化触发源数组为无效值(-1)，创建触发器动作代理
     *
     * @param triggerType 触发类型ID
     *                     取值范围：TRIG_TYPE_START_ID ~ TRIG_TYPE_MAX_ID
     *
     * @example
     *   new TriggerEdge() 调用 super(TRIG_TYPE_EDGE)
     *   new TriggerLogic() 调用 super(TRIG_TYPE_LOGIC)
     */
    public Trigger(int triggerType){
        Arrays.fill(triggerSource,-1);
        this.triggerType = triggerType;
        triggerAction = new TriggerAction(this);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发类型访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前触发类型
     *
     * @return 触发类型ID
     *         TRIG_TYPE_EDGE: 边沿触发
     *         TRIG_TYPE_PULSE: 脉宽触发
     *         TRIG_TYPE_LOGIC: 逻辑触发
     *         等等...
     */
    public int getTriggerType() {
        return triggerType;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单触发源访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前触发源（单触发源模式）
     * 用于边沿触发、脉宽触发等只需要一个触发源的类型
     *
     * @return 触发源通道索引
     *         0-7: CH1-CH8
     *         8-15: MATH1-8
     *         16-23: REF1-8
     *         24-27: S1-4
     */
    public int getTriggerSource() {
        return triggerSource_one;
    }

    /**
     * 获取切换前的触发源
     * 用于从外部触发恢复到之前的触发源
     *
     * @return 切换前的触发源通道索引
     */
    public int getPreTriggerSource() {
        return triggerSource_pre;
    }

    /**
     * 设置触发源（单触发源模式）
     * 更新触发源并触发触发源变更通知
     *
     * @param triggerSource 触发源通道索引
     *                      0-7: CH1-CH8
     *                      8-15: MATH1-8
     *                      16-23: REF1-8
     *                      24-27: S1-4
     *
     * @note 如果触发源是动态通道（CH1-CH8），会保存到triggerSource_pre用于恢复
     * @note 如果触发源发生变化，会调用triggerSourceChange()通知FPGA
     */
    public void setTriggerSource(int triggerSource) {
        if (ChannelFactory.isDynamicCh(triggerSource)) {
            triggerSource_pre = triggerSource;
        }
        if (triggerSource != triggerSource_one) {
            triggerSource_one = triggerSource;
            triggerSourceChange();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 抽象方法 - 由子类实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断当前触发类型是否支持多触发源
     * 由子类实现，决定触发源访问方式
     *
     * @return true: 支持多触发源（如逻辑触发）
     *         false: 只支持单触发源（如边沿触发）
     *
     * @example
     *   TriggerEdge.isMultitriggerSource() → false
     *   TriggerLogic.isMultitriggerSource() → true
     */
    protected abstract boolean isMultitriggerSource();

    // ═══════════════════════════════════════════════════════════════════════════════
    // 多触发源访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取指定索引的触发源（多触发源模式）
     * 用于逻辑触发等需要多个触发源的类型
     *
     * @param idx 触发源索引（0 ~ getTriggerSourceCnt()-1）
     * @return 触发源通道索引，-1表示该位置未配置触发源
     *
     * @note 如果当前触发类型不支持多触发源，返回单触发源的值
     */
    public int getTriggerSource(int idx) {
        if(isMultitriggerSource() && isValidIdx(idx)) {
            return triggerSource[idx];
        }
        return getTriggerSource();
    }

    /**
     * 判断指定通道是否为当前触发源
     * 遍历所有触发源，检查是否包含指定通道
     *
     * @param chIdx 通道索引
     * @return true: 该通道是触发源
     *         false: 该通道不是触发源
     */
    public boolean isTriggerSource(int chIdx) {
        for (int i = 0; i < getTriggerSourceCnt(); i++) {
            if (getTriggerSource(i) == chIdx) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置指定索引的触发源（多触发源模式）
     * 用于逻辑触发等需要多个触发源的类型
     *
     * @param idx 触发源索引（0 ~ getTriggerSourceCnt()-1）
     * @param triggerSource 触发源通道索引
     *
     * @note 如果当前触发类型不支持多触发源，会调用单触发源设置方法
     * @note 如果触发源发生变化，会调用triggerSourceChange()通知FPGA
     */
    public void setTriggerSource(int idx,int triggerSource) {
        if(isMultitriggerSource()) {
            if (isValidIdx(idx)) {
                if (triggerSource != this.triggerSource[idx]) {
                    this.triggerSource[idx] = triggerSource;
                    triggerSourceChange();
                }
            }
        }
        else {
            setTriggerSource(triggerSource);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发电平访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取指定通道的普通触发电平
     * 触发电平由通道管理，触发器只是提供访问接口
     *
     * @param chIdx 通道索引
     * @return 触发电平对象，如果通道不存在返回null
     *
     * @see TriggerLevel
     */
    public TriggerLevel getTriggerLevel(int chIdx){
        return getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_NORMAL,chIdx);
    }

    /**
     * 获取当前触发源的普通触发电平
     *
     * @return 触发电平对象，如果通道不存在返回null
     */
    public TriggerLevel getTriggerLevel(){
        return getTriggerLevel(getTriggerSource());
    }

    /**
     * 获取指定通道的指定类型触发电平
     * 支持普通电平和高电平两种类型
     *
     * @param triggerLevelType 触发电平类型
     *                          TRIGGER_LEVEL_NORMAL: 普通电平
     *                          TRIGGER_LEVEL_HIGH: 高电平
     * @param chIdx 通道索引
     * @return 触发电平对象，如果通道不存在返回null
     *
     * @see TriggerLevel#TRIGGER_LEVEL_NORMAL
     * @see TriggerLevel#TRIGGER_LEVEL_HIGH
     */
    public TriggerLevel getTriggerLevel(int triggerLevelType,int chIdx){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if(channel != null) {
            return channel.getTriggerLevel(triggerLevelType);
        }
        return null;
    }

    /**
     * 获取触发源对应的触发电平数量
     * 默认返回1，子类可重写
     *
     * @return 触发电平数量
     */
    public int getSrcTriggerLevelCnt(){
        return 1;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 工具方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断触发源索引是否有效
     * 有效范围：0 ~ TRIG_SRC_MAX
     *
     * @param idx 触发源索引
     * @return true: 索引有效
     *         false: 索引无效
     */
    public static boolean isValidIdx(int idx){
        if (idx >= 0 && idx <= TRIG_SRC_MAX) {
            return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 抽象方法 - 由子类实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前触发类型的触发源数量
     * 由子类实现
     *
     * @return 触发源数量
     *
     * @example
     *   TriggerEdge.getTriggerSourceCnt() → 1
     *   TriggerLogic.getTriggerSourceCnt() → 2~4（根据配置的逻辑通道数）
     */
    public abstract int getTriggerSourceCnt();

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发参数变更通知方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发源变更通知
     * 当触发源发生变化时调用，通过TriggerAction发送FPGA消息和事件通知
     *
     * @note 会发送以下FPGA命令：
     *       - FPGA_CMD_TRIG: 触发参数
     *       - FPGA_CMD_TRIG_OFFSET: 触发偏移
     *       - FPGA_CMD_TRIG_HUICHA: 回差
     *       - FPGA_CMD_TRIG_LEVEL: 触发电平
     *
     * @see TriggerAction#triggerSourceChange()
     */
    public  void triggerSourceChange(){
        triggerAction.triggerSourceChange();
    }

    /**
     * 触发参数变更通知
     * 当触发参数（如触发电平、触发条件等）发生变化时调用
     * 通过TriggerAction发送FPGA消息和事件通知
     *
     * @note 只发送FPGA_CMD_TRIG命令
     * @note 访问权限为protected，供子类调用
     *
     * @see TriggerAction#triggerParamChange()
     */
    protected void triggerParamChange(){
        triggerAction.triggerParamChange();
    }

}
