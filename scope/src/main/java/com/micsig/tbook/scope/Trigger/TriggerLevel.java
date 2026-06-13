package com.micsig.tbook.scope.Trigger;

import android.util.Log;

import com.micsig.tbook.scope.ScopeBase;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    TriggerLevel - 触发电平管理类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的触发电平管理类，管理示波器触发系统的触发电平参数。             ║
 * ║   触发电平是触发系统的核心参数，决定了信号在什么电压值时触发采集。            ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理触发电平位置（触发阈值电压）                                        ║
 * ║   2. 支持普通电平和高电平两种类型                                            ║
 * ║   3. 提供UI单位和FPGA单位的双向转换                                          ║
 * ║   4. 触发电平变更通知                                                        ║
 * ║                                                                              ║
 * ║ 【触发电平说明】                                                             ║
 * ║   触发电平是触发系统的核心参数，决定了信号在什么电压值时触发采集。            ║
 * ║                                                                              ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                         触发电平示意图                               │   ║
 * ║   │                                                                     │   ║
 * ║   │      ┌──────┐                                                      │   ║
 * ║   │      │      │                                                      │   ║
 * ║   │      │      │                                                      │   ║
 * ║   │ ─────┘      └──────                                                │   ║
 * ║   │      ↑                                                            │   ║
 * ║   │   触发电平                                                          │   ║
 * ║   │   (Trigger Level)                                                  │   ║
 * ║   │                                                                     │   ║
 * ║   │ 当信号穿过触发电平时，触发采集                                       │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【触发电平类型说明】                                                         ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 类型           │ 说明                                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 普通电平       │ 标准触发电平，用于大多数触发类型                    │  ║
 * ║   │ (NORMAL)       │ 如边沿触发、脉宽触发等                              │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 高电平         │ 第二触发电平，用于需要两个电平的触发类型            │  ║
 * ║   │ (HIGH)         │ 如矮脉冲触发（Runt）需要上下两个电平                │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【单位转换说明】                                                             ║
 * ║   示波器内部使用两种单位系统：                                               ║
 * ║   - UI单位：用户界面显示的单位（伏特V）                                      ║
 * ║   - FPGA单位：FPGA内部使用的ADC计数值（0-4095等）                            ║
 * ║                                                                              ║
 * ║   转换公式：                                                                 ║
 * ║   - UI → FPGA: pos_fpga = pos_ui * getToFPGACoff()                         ║
 * ║   - FPGA → UI: pos_ui = pos_fpga * getToUICoff()                           ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → ScopeBase: 提供单位转换系数                                             ║
 * ║   → TriggerLevelAction: 触发电平变更时的消息发送代理                        ║
 * ║   → Channel: 每个通道持有两个TriggerLevel实例（普通电平和高电平）           ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户调整触发电平时，调用setPos()更新电平值                             ║
 * ║   2. 显示触发电平时，调用getPosUI()获取UI单位值                             ║
 * ║   3. FPGA消息发送时，调用getPos()获取FPGA单位值                             ║
 * ║   4. 矮脉冲触发时，使用高电平作为第二触发电平                                ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   触发电平位置使用volatile和synchronized保护，保证线程安全。                 ║
 * ║   volatile保证可见性，synchronized保证原子性。                               ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018/3/20                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerLevel {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     */
    public static final String TAG = "TriggerLevel";

    /**
     * 普通触发电平类型ID
     * 标准触发电平，用于大多数触发类型
     * 如边沿触发、脉宽触发等
     */
    public static final int TRIGGER_LEVEL_NORMAL = 0;

    /**
     * 低触发电平类型ID
     * 别名，等同于TRIGGER_LEVEL_NORMAL
     */
    public static final int TRIGGER_LEVEL_LOW = TRIGGER_LEVEL_NORMAL;

    /**
     * 高触发电平类型ID
     * 第二触发电平，用于需要两个电平的触发类型
     * 如矮脉冲触发（Runt）需要上下两个电平
     */
    public static final int TRIGGER_LEVEL_HIGH = 1;

    /**
     * 触发电平类型最大值
     * 用于类型有效性检查
     */
    public static final int TRIGGER_LEVEL_MAX = 2;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发电平位置（FPGA单位）
     * 使用volatile保证多线程可见性
     *
     * <p><b>单位说明：</b>
     * FPGA内部使用ADC计数值表示电平位置，范围通常是0-4095（12位ADC）
     *
     * <p><b>示例：</b>
     * <pre>
     * 假设ADC范围0-4095，对应电压-5V ~ +5V
     * pos = 2048 → 0V（中间电平）
     * pos = 3072 → +2.5V
     * pos = 1024 → -2.5V
     * </pre>
     */
    private volatile double pos = 0;

    /**
     * 触发电平类型
     * 取值：TRIGGER_LEVEL_NORMAL 或 TRIGGER_LEVEL_HIGH
     */
    private int triggerLevelType = TRIGGER_LEVEL_NORMAL;

    /**
     * 触发电平动作代理
     * 负责触发电平变更时发送FPGA消息和事件通知
     */
    private TriggerLevelAction triggerLevelAction;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 工具方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断触发电平类型是否有效
     * 有效范围：TRIGGER_LEVEL_NORMAL ~ TRIGGER_LEVEL_MAX - 1
     *
     * @param triggerLevelType 触发电平类型ID
     * @return true: 类型有效
     *         false: 类型无效
     */
    public static boolean isTriggerLevelVaild(int triggerLevelType){
        return triggerLevelType>= TRIGGER_LEVEL_NORMAL && triggerLevelType < TRIGGER_LEVEL_MAX;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造触发电平管理对象
     *
     * @param triggerLevelType 触发电平类型
     *                          TRIGGER_LEVEL_NORMAL (0): 普通电平
     *                          TRIGGER_LEVEL_HIGH (1): 高电平
     *
     * <p><b>初始化流程：</b>
     * <ol>
     *   <li>设置触发电平类型</li>
     *   <li>创建TriggerLevelAction动作代理</li>
     * </ol>
     *
     * @example
     *   TriggerLevel normalLevel = new TriggerLevel(TriggerLevel.TRIGGER_LEVEL_NORMAL);
     *   TriggerLevel highLevel = new TriggerLevel(TriggerLevel.TRIGGER_LEVEL_HIGH);
     */
    public TriggerLevel(int triggerLevelType){
        this.triggerLevelType = triggerLevelType;
        triggerLevelAction = new TriggerLevelAction(this);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发电平位置访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取触发电平位置（UI单位）
     * 返回用户界面显示的电压值
     *
     * @return 触发电平位置（伏特V）
     *
     * <p><b>单位转换：</b>
     * <pre>
     * pos_ui = pos_fpga * getToUICoff()
     * </pre>
     *
     * @note 线程安全：使用synchronized保护
     *
     * @see ScopeBase#getToUICoff()
     */
    public synchronized double getPosUI(){
        return ScopeBase.changeAccuracy(pos * ScopeBase.getToUICoff());
    }

    /**
     * 获取触发电平位置（FPGA单位）
     * 返回FPGA内部使用的ADC计数值
     *
     * @return 触发电平位置（ADC计数值）
     *
     * @note 线程安全：使用synchronized保护
     */
    public synchronized double getPos(){
        return pos;
    }

    /**
     * 设置触发电平位置
     * 更新触发电平并发送FPGA消息和事件通知
     *
     * @param pos 触发电平位置（UI单位，伏特V）
     * @param bUser 是否为用户操作
     *              true: 用户手动调整触发电平，发送用户变更事件
     *              false: 程序自动调整触发电平，不发送用户变更事件
     *
     * <p><b>单位转换：</b>
     * <pre>
     * pos_fpga = pos_ui * getToFPGACoff()
     * </pre>
     *
     * <p><b>发送的消息：</b>
     * <ol>
     *   <li>TriggerLevelChange():
     *     <ul>
     *       <li>发送FPGA_CMD_TRIG_LEVEL命令（通过FilterThread防抖）</li>
     *       <li>发送EVENT_TRIGGER_LEVEL事件</li>
     *     </ul>
     *   </li>
     *   <li>triggerLevelUserChange()（如果bUser为true）:
     *     <ul>
     *       <li>发送EVENT_TRIGGER_LEVEL_USER事件</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><b>防抖机制：</b>
     * TriggerLevelAction使用FilterThread实现100ms防抖，
     * 避免用户快速拖动触发电平时发送过多FPGA消息。
     *
     * @note 线程安全：使用synchronized保护pos变量
     *
     * @see ScopeBase#getToFPGACoff()
     * @see TriggerLevelAction#TriggerLevelChange()
     * @see TriggerLevelAction#triggerLevelUserChange()
     */
    public void setPos(double pos,boolean bUser){

        synchronized (this) {
            this.pos = pos * ScopeBase.getToFPGACoff();
        }
        triggerLevelAction.TriggerLevelChange();
        if (bUser) {
            triggerLevelAction.triggerLevelUserChange();
        }
    }

    /**
     * 设置触发电平位置（非用户操作）
     * 更新触发电平并发送FPGA消息，但不发送用户变更事件
     *
     * @param pos 触发电平位置（UI单位，伏特V）
     *
     * @see #setPos(double, boolean)
     */
    public void setPos(double pos){
        setPos(pos,false);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发电平类型访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取触发电平类型
     *
     * @return 触发电平类型
     *         TRIGGER_LEVEL_NORMAL (0): 普通电平
     *         TRIGGER_LEVEL_HIGH (1): 高电平
     */
    public int getTriggerLevelType() {
        return triggerLevelType;
    }

}
