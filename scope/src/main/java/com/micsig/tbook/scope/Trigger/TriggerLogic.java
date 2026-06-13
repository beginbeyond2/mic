package com.micsig.tbook.scope.Trigger;

import com.micsig.tbook.scope.channel.ChannelFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    TriggerLogic - 逻辑触发器类                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的逻辑触发器实现类，支持多通道逻辑组合触发。                     ║
 * ║   逻辑触发允许将多个通道（最多4个）按照逻辑关系（AND/OR/NAND/NOR）组合，     ║
 * ║   当逻辑条件满足时触发采集。                                                 ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理逻辑模式（AND/OR/NAND/NOR）                                        ║
 * ║   2. 管理逻辑条件（小于/大于/等于/不等于/变真/变假）                         ║
 * ║   3. 管理多触发源（最多4个通道）                                             ║
 * ║   4. 管理每个通道的逻辑电平极性（高/低/无）                                  ║
 * ║   5. 管理逻辑时间参数                                                        ║
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
 * ║        ↑                                                                   ║
 * ║      本类                                                                   ║
 * ║                                                                              ║
 * ║ 【逻辑触发说明】                                                             ║
 * ║   逻辑触发是一种高级触发方式，允许将多个通道按照逻辑关系组合。               ║
 * ║   当逻辑条件满足时，示波器触发采集。                                         ║
 * ║                                                                              ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                     逻辑触发示意图                                   │   ║
 * ║   │                                                                     │   ║
 * ║   │  CH1 ────┐                                                         │   ║
 * ║   │          │                                                         │   ║
 * ║   │  CH2 ────┼──── [逻辑运算] ──── 触发输出                            │   ║
 * ║   │          │        (AND/OR)                                         │   ║
 * ║   │  CH3 ────┤                                                         │   ║
 * ║   │          │                                                         │   ║
 * ║   │  CH4 ────┘                                                         │   ║
 * ║   │                                                                     │   ║
 * ║   │  每个通道可设置逻辑电平极性（高/低/无）                              │   ║
 * ║   │  所有参与通道按逻辑模式组合，满足条件时触发                          │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【逻辑模式说明】                                                             ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 模式           │ 说明                                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ AND（与）      │ 所有通道都满足条件时触发                            │  ║
 * ║   │                │ CH1 ∧ CH2 ∧ CH3 ∧ CH4                              │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ OR（或）       │ 任一通道满足条件时触发                              │  ║
 * ║   │                │ CH1 ∨ CH2 ∨ CH3 ∨ CH4                              │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ NAND（与非）   │ 所有通道都满足条件的非值时触发                      │  ║
 * ║   │                │ ¬(CH1 ∧ CH2 ∧ CH3 ∧ CH4)                           │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ NOR（或非）    │ 任一通道满足条件的非值时触发                        │  ║
 * ║   │                │ ¬(CH1 ∨ CH2 ∨ CH3 ∨ CH4)                           │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【逻辑电平极性说明】                                                         ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 极性           │ 说明                                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ HIGH（高）     │ 通道信号高于触发电平时为真                          │  ║
 * ║   │                │ 适用于检测高电平信号                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ LOW（低）      │ 通道信号低于触发电平时为真                          │  ║
 * ║   │                │ 适用于检测低电平信号                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ NONE（无）     │ 通道不参与逻辑触发                                  │  ║
 * ║   │                │ 该通道被排除在逻辑运算之外                          │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【逻辑条件说明】                                                             ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 条件           │ 说明                                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 小于           │ 逻辑时间小于设定值时触发                            │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 大于           │ 逻辑时间大于设定值时触发                            │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 等于           │ 逻辑时间等于设定值时触发                            │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 不等于         │ 逻辑时间不等于设定值时触发                          │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 变真           │ 逻辑条件从假变为真时触发                            │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 变假           │ 逻辑条件从真变为假时触发                            │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → Trigger: 继承触发器基类                                                 ║
 * ║   → ChannelFactory: 通道工厂，获取通道实例和通道数量                        ║
 * ║   → TriggerFactory: 工厂类，管理触发器实例                                  ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户选择逻辑触发类型时，TriggerFactory创建TriggerLogic实例            ║
 * ║   2. 用户配置逻辑模式（AND/OR/NAND/NOR）时                                  ║
 * ║   3. 用户配置每个通道的逻辑电平极性时                                       ║
 * ║   4. 用户设置逻辑时间参数时                                                 ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 模板方法模式（Template Method）：继承Trigger的抽象方法                 ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   逻辑参数未加同步保护，建议在UI线程操作。                                   ║
 * ║   触发源数组操作通过父类方法，继承父类的同步机制。                           ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-6-1                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerLogic extends Trigger {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 逻辑电平极性常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 逻辑低电平极性
     * 通道信号低于触发电平时为真
     * 适用于检测低电平信号
     */
    public static final int LOGIC_LOW = 1;

    /**
     * 逻辑高电平极性
     * 通道信号高于触发电平时为真
     * 适用于检测高电平信号
     */
    public static final int LOGIC_HIGH = 0;

    /**
     * 逻辑无极性
     * 通道不参与逻辑触发
     * 该通道被排除在逻辑运算之外
     */
    public static final int LOGIC_NONE = 2;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 逻辑模式常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 逻辑与模式（AND）
     * 所有通道都满足条件时触发
     * 逻辑表达式：CH1 ∧ CH2 ∧ CH3 ∧ CH4
     *
     * <p><b>示例：</b>
     * <pre>
     * 配置：CH1=HIGH, CH2=LOW, CH3=NONE, CH4=NONE, 模式=AND
     * 触发条件：CH1高于触发电平 AND CH2低于触发电平
     * </pre>
     */
    public static final int LOGIC_AND = 0;

    /**
     * 逻辑或模式（OR）
     * 任一通道满足条件时触发
     * 逻辑表达式：CH1 ∨ CH2 ∨ CH3 ∨ CH4
     *
     * <p><b>示例：</b>
     * <pre>
     * 配置：CH1=HIGH, CH2=LOW, CH3=NONE, CH4=NONE, 模式=OR
     * 触发条件：CH1高于触发电平 OR CH2低于触发电平
     * </pre>
     */
    public static final int LOGIC_OR = 1;

    /**
     * 逻辑与非模式（NAND）
     * 所有通道都满足条件的非值时触发
     * 逻辑表达式：¬(CH1 ∧ CH2 ∧ CH3 ∧ CH4)
     */
    public static final int LOGIC_NAND  = 2;

    /**
     * 逻辑或非模式（NOR）
     * 任一通道满足条件的非值时触发
     * 逻辑表达式：¬(CH1 ∨ CH2 ∨ CH3 ∨ CH4)
     */
    public static final int LOGIC_NOR = 3;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 逻辑条件常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 逻辑条件：小于
     * 逻辑时间小于设定值时触发
     */
    public static final int LOGIC_RELATION_LESSER = TRIGGER_RELATION_LESS_THAN;

    /**
     * 逻辑条件：大于
     * 逻辑时间大于设定值时触发
     */
    public static final int LOGIC_RELATION_GREATER = TRIGGER_RELATION_MORE_THAN;

    /**
     * 逻辑条件：等于
     * 逻辑时间等于设定值时触发
     */
    public static final int LOGIC_RELATION_EQUAL = TRIGGER_RELATION_EQUAL;

    /**
     * 逻辑条件：不等于
     * 逻辑时间不等于设定值时触发
     */
    public static final int LOGIC_RELATION_NOT_EQUAL = TRIGGER_RELATION_NOT_EQUAL;

    /**
     * 逻辑条件：变真
     * 逻辑条件从假变为真时触发
     */
    public static final int LOGIC_RELATION_TRUE = TRIGGER_RELATION_TRUE;

    /**
     * 逻辑条件：变假
     * 逻辑条件从真变为假时触发
     */
    public static final int LOGIC_RELATION_FALSE = TRIGGER_RELATION_FALSE;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 当前逻辑模式
     * 取值：LOGIC_AND、LOGIC_OR、LOGIC_NAND 或 LOGIC_NOR
     * 默认值：LOGIC_AND（逻辑与）
     */
    private int logic = LOGIC_AND;

    /**
     * 当前逻辑条件
     * 取值：LOGIC_RELATION_LESSER、LOGIC_RELATION_GREATER、LOGIC_RELATION_EQUAL、
     *       LOGIC_RELATION_NOT_EQUAL、LOGIC_RELATION_TRUE 或 LOGIC_RELATION_FALSE
     * 默认值：LOGIC_RELATION_LESSER（小于）
     */
    private int condition = LOGIC_RELATION_LESSER;

    /**
     * 逻辑时间（FPGA单位）
     * 单位：纳秒
     * 默认值：8ns
     *
     * 用于设置逻辑条件满足的时间阈值
     */
    private long logicTime = 8;

    /**
     * 高电平时间（FPGA单位）
     * 单位：纳秒
     * 默认值：8ns
     *
     * 用于设置高电平持续时间的阈值
     */
    private long timeHigh = 8;

    /**
     * 低电平时间（FPGA单位）
     * 单位：纳秒
     * 默认值：8ns
     *
     * 用于设置低电平持续时间的阈值
     */
    private long timeLow = 8;

    /**
     * 每个通道的逻辑电平极性配置数组
     * 索引对应通道ID（CH1=0, CH2=1, ...）
     * 值：LOGIC_HIGH、LOGIC_LOW 或 LOGIC_NONE
     *
     * <p><b>示例：</b>
     * <pre>
     * logicValids[0] = LOGIC_HIGH  → CH1参与，高电平有效
     * logicValids[1] = LOGIC_LOW   → CH2参与，低电平有效
     * logicValids[2] = LOGIC_NONE  → CH3不参与
     * logicValids[3] = LOGIC_NONE  → CH4不参与
     * </pre>
     */
    private int [] logicValids = new int[ChannelFactory.CH_CNT];

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造逻辑触发器实例
     * 调用父类Trigger构造方法设置触发类型为逻辑触发，并初始化触发源
     *
     * <p><b>初始化流程：</b>
     * <ol>
     *   <li>调用父类Trigger构造方法，设置触发类型为TRIG_TYPE_LOGIC</li>
     *   <li>调用InitTriggerSrc()初始化触发源</li>
     * </ol>
     *
     * @example
     *   TriggerLogic trigger = new TriggerLogic();
     */
    public TriggerLogic() {
        super(Trigger.TRIG_TYPE_LOGIC);
        InitTriggerSrc();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 逻辑模式访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前逻辑模式
     *
     * @return 逻辑模式
     *         LOGIC_AND (0): 逻辑与
     *         LOGIC_OR (1): 逻辑或
     *         LOGIC_NAND (2): 逻辑与非
     *         LOGIC_NOR (3): 逻辑或非
     */
    public int getLogic() {
        return logic;
    }

    /**
     * 设置逻辑模式
     * 更新逻辑模式并发送FPGA消息
     *
     * @param logic 逻辑模式
     *              LOGIC_AND: 逻辑与
     *              LOGIC_OR: 逻辑或
     *              LOGIC_NAND: 逻辑与非
     *              LOGIC_NOR: 逻辑或非
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setLogic(int logic) {
        this.logic = logic;
        triggerParamChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时间参数访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取高电平时间
     *
     * @return 高电平时间（纳秒）
     */
    public long getTimeHigh() {
        return timeHigh;
    }

    /**
     * 设置高电平时间
     * 更新高电平时间并发送FPGA消息
     *
     * @param timeHigh 高电平时间（纳秒）
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setTimeHigh(long timeHigh) {
        this.timeHigh = timeHigh;
        triggerParamChange();
    }

    /**
     * 获取低电平时间
     *
     * @return 低电平时间（纳秒）
     */
    public long getTimeLow() {
        return timeLow;
    }

    /**
     * 设置低电平时间
     * 更新低电平时间并发送FPGA消息
     *
     * @param timeLow 低电平时间（纳秒）
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setTimeLow(long timeLow) {
        this.timeLow = timeLow;
        triggerParamChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 逻辑条件访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前逻辑条件
     *
     * @return 逻辑条件
     *         LOGIC_RELATION_LESSER: 小于
     *         LOGIC_RELATION_GREATER: 大于
     *         LOGIC_RELATION_EQUAL: 等于
     *         LOGIC_RELATION_NOT_EQUAL: 不等于
     *         LOGIC_RELATION_TRUE: 变真
     *         LOGIC_RELATION_FALSE: 变假
     */
    public int getCondition() {
        return condition;
    }

    /**
     * 设置逻辑条件
     * 更新逻辑条件并发送FPGA消息
     *
     * @param condition 逻辑条件
     *                  LOGIC_RELATION_LESSER: 小于
     *                  LOGIC_RELATION_GREATER: 大于
     *                  LOGIC_RELATION_EQUAL: 等于
     *                  LOGIC_RELATION_NOT_EQUAL: 不等于
     *                  LOGIC_RELATION_TRUE: 变真
     *                  LOGIC_RELATION_FALSE: 变假
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setCondition(int condition) {
        this.condition = condition;
        triggerParamChange();
    }

    /**
     * 获取逻辑时间
     *
     * @return 逻辑时间（纳秒）
     */
    public long getLogicTime() {
        return logicTime;
    }

    /**
     * 设置逻辑时间
     * 更新逻辑时间并发送FPGA消息
     *
     * @param logicTime 逻辑时间（纳秒）
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setLogicTime(long logicTime) {
        this.logicTime = logicTime;
        triggerParamChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 逻辑电平极性访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取指定通道的逻辑电平极性
     *
     * @param chIdx 通道索引（0-7对应CH1-CH8）
     * @return 逻辑电平极性
     *         LOGIC_HIGH: 高电平有效
     *         LOGIC_LOW: 低电平有效
     *         LOGIC_NONE: 不参与逻辑触发
     *
     * @note 如果通道不是动态通道（CH1-CH8），返回LOGIC_NONE
     */
    public int getLogicValid(int chIdx) {
        if(ChannelFactory.isDynamicCh(chIdx)){
            return logicValids[chIdx];
        }
        return LOGIC_NONE;
    }

    /**
     * 设置指定通道的逻辑电平极性
     * 更新逻辑电平极性，重新初始化触发源，并发送FPGA消息
     *
     * @param chIdx 通道索引（0-7对应CH1-CH8）
     * @param logicValid 逻辑电平极性
     *                   LOGIC_HIGH: 高电平有效
     *                   LOGIC_LOW: 低电平有效
     *                   LOGIC_NONE: 不参与逻辑触发
     *
     * @note 设置后会：
     *       1. 重新初始化触发源（InitTriggerSrc）
     *       2. 发送FPGA_CMD_TRIG命令
     *
     * @see #InitTriggerSrc()
     */
    public void setLogicValids(int chIdx,int logicValid) {
        if(ChannelFactory.isDynamicCh(chIdx)) {
            this.logicValids[chIdx] = logicValid;
            InitTriggerSrc();
            triggerParamChange();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 私有方法 - 触发源初始化
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化触发源
     * 根据逻辑电平极性配置，设置触发源通道索引
     *
     * <p><b>初始化逻辑：</b>
     * <ol>
     *   <li>遍历所有通道（CH1-CH8）</li>
     *   <li>查找参与逻辑触发的通道（logicValids[i] != LOGIC_NONE）</li>
     *   <li>将参与通道设置为触发源</li>
     *   <li>将未参与通道的触发源设置为-1</li>
     * </ol>
     *
     * <p><b>触发源数组说明：</b>
     * <pre>
     * Trigger.triggerSource[4]: 4个触发源位置
     * 并非和通道一一对应，当逻辑触发只有2个源时，则只用triggerSource[0]和triggerSource[1]
     *
     * 示例：
     *   logicValids[0] = LOGIC_HIGH  → CH1参与
     *   logicValids[1] = LOGIC_LOW   → CH2参与
     *   logicValids[2] = LOGIC_NONE  → CH3不参与
     *   logicValids[3] = LOGIC_NONE  → CH4不参与
     *
     * 初始化后：
     *   triggerSource[0] = 0 (CH1)
     *   triggerSource[1] = 1 (CH2)
     *   triggerSource[2] = -1 (未配置)
     *   triggerSource[3] = -1 (未配置)
     * </pre>
     */
    private void InitTriggerSrc(){
        int idx = 0;
        int maxIdx = ChannelFactory.getMaxChIdx();
        for(int i=ChannelFactory.CH1;i<maxIdx;i++){
            if(logicValids[i] != LOGIC_NONE){
                setTriggerSource(idx++,i);
            }
        }
        for(int i=idx; i<maxIdx;i++){
            setTriggerSource(i,-1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 抽象方法实现 - 继承自Trigger
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取触发源数量
     * 统计参与逻辑触发的通道数量
     *
     * @return 触发源数量（0-4）
     *
     * <p><b>计算逻辑：</b>
     * <pre>
     * 遍历所有通道，统计logicValids[i] != LOGIC_NONE的通道数量
     * </pre>
     */
    @Override
    public int getTriggerSourceCnt() {

        AtomicInteger cnt = new AtomicInteger();
        ChannelFactory.forEachCh((channel)->{
            int i = channel.getChId();
            if(logicValids[i] != LOGIC_NONE){
                cnt.getAndIncrement();
            }
        });

        return cnt.get();
    }

    /**
     * 判断是否支持多触发源
     * 逻辑触发支持多触发源（最多4个通道）
     *
     * @return 始终返回true，表示支持多触发源
     */
    @Override
    protected boolean isMultitriggerSource() {
        return true;
    }
}
