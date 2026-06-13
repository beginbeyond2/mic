package com.micsig.tbook.scope.Trigger;

import com.micsig.tbook.scope.Action.FPGAMessage;
import com.micsig.tbook.scope.Action.XAction;
import com.micsig.tbook.scope.Event.EventFactory;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║               TriggerCommonAction - 触发器公共参数动作代理类                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的公共参数动作代理类，负责触发器公共参数变更时的消息发送。       ║
 * ║   继承自XAction基类，复用统一的消息发送机制。                                 ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 触发释抑时间变更时发送FPGA消息                                          ║
 * ║   2. 触发模式变更时发送FPGA消息                                              ║
 * ║   3. 触发类型变更时发送复合FPGA消息                                          ║
 * ║   4. 外部触发参数变更时发送FPGA消息                                          ║
 * ║   5. 触发灵敏度变更时发送FPGA消息                                            ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │    XAction      │ ← 动作代理基类                 ║
 * ║                          │   (消息发送)    │                                 ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║         ┌───────────┬─────────────┼─────────────┬───────────┐               ║
 * ║         │           │             │             │           │               ║
 * ║         ▼           ▼             ▼             ▼           ▼               ║
 * ║   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐         ║
 * ║   │Trigger   │ │Horizontal│ │Vertical  │ │Channel   │ │Trigger   │         ║
 * ║   │Action    │ │Action    │ │Action    │ │Action    │ │Common    │         ║
 * ║   │触发器    │ │水平轴    │ │垂直轴    │ │通道      │ │Action    │ ← 本类   ║
 * ║   └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘         ║
 * ║                                                                              ║
 * ║ 【消息流程】                                                                 ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │  用户操作（UI）                                                     │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  TriggerCommon.setTriggerType() / setTriggerMode() / ...           │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  TriggerCommonAction.TriggerTypeChange() / TriggerModeChange()     │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  ┌─────────────────────────────────────────────────────────────┐   │   ║
 * ║   │  │ sendChSample()        → 更新通道采样状态                    │   │   ║
 * ║   │  │ sendFpgaMsg(cmd)      → 发送FPGA命令                       │   │   ║
 * ║   │  │ sendEvent(EVENT)      → 发送UI事件                         │   │   ║
 * ║   │  └─────────────────────────────────────────────────────────────┘   │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  FPGA硬件更新 / UI界面刷新                                          │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【FPGA命令说明】                                                             ║
 * ║   ┌────────────────────────────┬────────────────────────────────────────┐  ║
 * ║   │ 命令                       │ 说明                                    │  ║
 * ║   ├────────────────────────────┼────────────────────────────────────────┤  ║
 * ║   │ FPGA_CMD_TRIG_RETRAIN      │ 触发释抑时间                            │  ║
 * ║   │ FPGA_CMD_TRIG              │ 触发参数（模式/类型等）                 │  ║
 * ║   │ FPGA_CMD_AUTO_TRIG_T       │ 自动触发时间                            │  ║
 * ║   │ FPGA_CMD_SAMP_MODE         │ 采样模式                                │  ║
 * ║   │ FPGA_CMD_COUY              │ 耦合                                    │  ║
 * ║   │ FPGA_CMD_TRIG_HUICHA       │ 触发回差                                │  ║
 * ║   │ FPGA_CMD_TRIG_COUPLE       │ 触发耦合                                │  ║
 * ║   │ FPGA_CMD_TRIG_LEVEL        │ 触发电平                                │  ║
 * ║   │ FPGA_CMD_TRIG_OFFSET       │ 触发偏移                                │  ║
 * ║   │ FPGA_CMD_EXT_RESISTANCE    │ 外部触发输入阻抗                        │  ║
 * ║   │ FPGA_CMD_EXT_TRIGGER_LEVEL │ 外部触发电平                            │  ║
 * ║   └────────────────────────────┴────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → TriggerCommon: 持有公共参数引用，获取参数值                             ║
 * ║   → XAction: 继承基类，复用消息发送方法                                     ║
 * ║   → FPGAMessage: FPGA命令常量定义                                           ║
 * ║   → EventFactory: 事件常量定义                                              ║
 * ║   → ScopeMessage: 消息处理中心（通过XAction间接调用）                       ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户切换触发模式（自动/正常）时                                        ║
 * ║   2. 用户切换触发类型（边沿/脉宽/逻辑等）时                                 ║
 * ║   3. 用户调整触发释抑时间时                                                 ║
 * ║   4. 用户配置外部触发参数时                                                 ║
 * ║   5. 用户调整触发灵敏度时                                                   ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 代理模式（Proxy）：作为TriggerCommon的消息发送代理                      ║
 * ║   - 外观模式（Facade）：封装复杂的FPGA命令组合                              ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   本类无状态，所有方法通过XAction委托给ScopeMessage，线程安全由              ║
 * ║   ScopeMessage保证。                                                        ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018/3/20                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerCommonAction extends XAction {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 关联的触发器公共参数对象引用
     * 用于获取触发器参数（当前实现中未使用，预留扩展）
     *
     * @note 通过此引用，TriggerCommonAction可以访问触发器的完整参数，
     *       未来可能用于构建更复杂的FPGA命令参数
     */
    private TriggerCommon triggerCommon;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造触发器公共参数动作代理
     * 调用父类XAction构造方法初始化消息发送机制
     *
     * @param triggerCommon 关联的触发器公共参数对象
     *                       用于获取触发器参数，支持后续扩展
     *
     * @example
     *   TriggerCommon common = new TriggerCommon();
     *   TriggerCommonAction action = new TriggerCommonAction(common);
     */
    public TriggerCommonAction(TriggerCommon triggerCommon){
        this.triggerCommon = triggerCommon;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发释抑时间变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发释抑时间变更通知
     * 当触发释抑时间发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：FPGA_CMD_TRIG_RETRAIN（触发释抑时间命令）</li>
     *   <li>UI事件：EVENT_TRIGGER_COMMON_HOLDOFFTIME（触发释抑时间变更事件）</li>
     * </ul>
     *
     * <p><b>触发释抑时间说明：</b>
     * 触发释抑（Holdoff）是指两次触发之间的最小时间间隔。
     * 在释抑时间内，即使满足触发条件也不会触发。
     * 用于稳定显示周期性复杂信号，避免误触发。
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerCommon.setTriggerHoldOffTime(200*1000*1000)
     *     → TriggerHoldOffTimeChange()
     *     → sendFpgaMsg(FPGA_CMD_TRIG_RETRAIN)
     *     → sendEvent(EVENT_TRIGGER_COMMON_HOLDOFFTIME)
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_TRIG_RETRAIN
     * @see EventFactory#EVENT_TRIGGER_COMMON_HOLDOFFTIME
     */
    public void TriggerHoldOffTimeChange(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_TRIG_RETRAIN);
        sendEvent(EventFactory.EVENT_TRIGGER_COMMON_HOLDOFFTIME);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发模式变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发模式变更通知
     * 当触发模式（自动/正常）发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：FPGA_CMD_TRIG（触发参数命令）</li>
     *   <li>UI事件：EVENT_TRIGGER_COMMON_MODE（触发模式变更事件）</li>
     * </ul>
     *
     * <p><b>触发模式说明：</b>
     * <ul>
     *   <li><b>自动触发（TM_AUTO）：</b>无论是否满足触发条件，示波器都会定期强制触发采集</li>
     *   <li><b>正常触发（TM_NORMAL）：</b>只有满足触发条件时才触发采集</li>
     * </ul>
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerCommon.setTriggerMode(TM_NORMAL)
     *     → TriggerModeChange()
     *     → sendFpgaMsg(FPGA_CMD_TRIG)
     *     → sendEvent(EVENT_TRIGGER_COMMON_MODE)
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_TRIG
     * @see EventFactory#EVENT_TRIGGER_COMMON_MODE
     * @see TriggerCommon#TM_AUTO
     * @see TriggerCommon#TM_NORMAL
     */
    public void TriggerModeChange(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_TRIG);
        sendEvent(EventFactory.EVENT_TRIGGER_COMMON_MODE);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 自动触发时间变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 进入自动触发时间变更通知
     * 当进入自动触发的时间阈值发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：FPGA_CMD_AUTO_TRIG_T（自动触发时间命令）</li>
     * </ul>
     *
     * <p><b>自动触发时间说明：</b>
     * 当在正常触发模式下，超过此时间未触发时，自动切换到自动触发模式。
     * 单位：毫秒，默认值：1000ms（1秒）。
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerCommon.setTriggerEnterAutoTime(2000)
     *     → TriggerEnterAutoTimeChange()
     *     → sendFpgaMsg(FPGA_CMD_AUTO_TRIG_T)
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_AUTO_TRIG_T
     */
    public void TriggerEnterAutoTimeChange(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_AUTO_TRIG_T);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发类型变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发类型变更通知
     * 当触发类型（边沿/脉宽/逻辑等）发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ol>
     *   <li>sendChSample()：更新通道采样状态
     *       <ul>
     *         <li>通知硬件消息处理更新采样参数</li>
     *         <li>刷新UI的存储深度和采样率显示</li>
     *       </ul>
     *   </li>
     *   <li>sendFpgaMsg()：发送复合FPGA命令
     *       <ul>
     *         <li>FPGA_CMD_SAMP_MODE：采样模式</li>
     *         <li>FPGA_CMD_COUY：耦合</li>
     *         <li>FPGA_CMD_TRIG_HUICHA：触发回差</li>
     *         <li>FPGA_CMD_TRIG_COUPLE：触发耦合</li>
     *         <li>FPGA_CMD_TRIG_LEVEL：触发电平</li>
     *         <li>FPGA_CMD_TRIG：触发参数</li>
     *         <li>FPGA_CMD_TRIG_OFFSET：触发偏移</li>
     *         <li>FPGA_CMD_AUTO_TRIG_T：自动触发时间</li>
     *       </ul>
     *   </li>
     *   <li>sendEvent()：发送UI事件
     *       <ul>
     *         <li>EVENT_TRIGGER_TYPE：通知界面刷新触发类型显示</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <p><b>为什么触发类型变更需要发送更多命令？</b>
     * <ul>
     *   <li>不同触发类型可能使用不同的采样模式</li>
     *   <li>不同触发类型可能需要不同的耦合设置</li>
     *   <li>不同触发类型可能需要不同的触发电平和回差</li>
     *   <li>切换触发类型时需要重新配置完整的触发系统</li>
     * </ul>
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerCommon.setTriggerType(TRIG_TYPE_PULSE)
     *     → TriggerTypeChange()
     *     → sendChSample()
     *     → sendFpgaMsg(SAMP_MODE|COUY|TRIG_HUICHA|TRIG_COUPLE|TRIG_LEVEL|TRIG|TRIG_OFFSET|AUTO_TRIG_T)
     *     → sendEvent(EVENT_TRIGGER_TYPE)
     * </pre>
     *
     * <p><b>FPGA命令位运算说明：</b>
     * <pre>
     * FPGA_CMD_SAMP_MODE | FPGA_CMD_COUY | FPGA_CMD_TRIG_HUICHA | FPGA_CMD_TRIG_COUPLE
     *     | FPGA_CMD_TRIG_LEVEL | FPGA_CMD_TRIG | FPGA_CMD_TRIG_OFFSET | FPGA_CMD_AUTO_TRIG_T
     *
     * 组合结果是一个8位掩码，FPGAMessage.run() 会根据各bit位执行对应的寄存器更新
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_SAMP_MODE
     * @see FPGAMessage#FPGA_CMD_COUY
     * @see FPGAMessage#FPGA_CMD_TRIG_HUICHA
     * @see FPGAMessage#FPGA_CMD_TRIG_COUPLE
     * @see FPGAMessage#FPGA_CMD_TRIG_LEVEL
     * @see FPGAMessage#FPGA_CMD_TRIG
     * @see FPGAMessage#FPGA_CMD_TRIG_OFFSET
     * @see FPGAMessage#FPGA_CMD_AUTO_TRIG_T
     * @see EventFactory#EVENT_TRIGGER_TYPE
     */
    public void TriggerTypeChange(){
        sendChSample();
        sendFpgaMsg(FPGAMessage.FPGA_CMD_SAMP_MODE|FPGAMessage.FPGA_CMD_COUY
                |FPGAMessage.FPGA_CMD_TRIG_HUICHA|FPGAMessage.FPGA_CMD_TRIG_COUPLE
                |FPGAMessage.FPGA_CMD_TRIG_LEVEL
                |FPGAMessage.FPGA_CMD_TRIG
                |FPGAMessage.FPGA_CMD_TRIG_OFFSET
                |FPGAMessage.FPGA_CMD_AUTO_TRIG_T);
        sendEvent(EventFactory.EVENT_TRIGGER_TYPE);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 外部触发参数变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 外部触发输入阻抗变更通知
     * 当外部触发输入阻抗（1MΩ/50Ω）发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：FPGA_CMD_EXT_RESISTANCE（外部触发输入阻抗命令）</li>
     * </ul>
     *
     * <p><b>输入阻抗说明：</b>
     * <ul>
     *   <li><b>1MΩ（EXT_TRIGGER_1M）：</b>高阻抗输入，适用于一般信号测量</li>
     *   <li><b>50Ω（EXT_TRIGGER_50）：</b>低阻抗输入，适用于高频信号测量</li>
     * </ul>
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerCommon.setExtTriggerInputRes(EXT_TRIGGER_50)
     *     → extTriggerRes()
     *     → sendFpgaMsg(0, FPGA_CMD_EXT_RESISTANCE)
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_EXT_RESISTANCE
     * @see TriggerCommon#EXT_TRIGGER_1M
     * @see TriggerCommon#EXT_TRIGGER_50
     */
    public void extTriggerRes(){
        sendFpgaMsg(0,FPGAMessage.FPGA_CMD_EXT_RESISTANCE);
    }

    /**
     * 外部触发电平变更通知
     * 当外部触发电平发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：FPGA_CMD_EXT_TRIGGER_LEVEL（外部触发电平命令）</li>
     * </ul>
     *
     * <p><b>外部触发电平说明：</b>
     * 外部触发信号的触发阈值电压，单位：伏特（V）。
     * 取值范围：根据示波器规格确定，通常 -5V ~ +5V。
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerCommon.setExtTriggerLevle(1.5)
     *     → extTriggerLevel()
     *     → sendFpgaMsg(0, FPGA_CMD_EXT_TRIGGER_LEVEL)
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_EXT_TRIGGER_LEVEL
     */
    public void extTriggerLevel(){
        sendFpgaMsg(0,FPGAMessage.FPGA_CMD_EXT_TRIGGER_LEVEL);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发灵敏度变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发灵敏度变更通知
     * 当触发灵敏度发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：FPGA_CMD_TRIG_LEVEL（触发电平命令）</li>
     * </ul>
     *
     * <p><b>触发灵敏度说明：</b>
     * 触发灵敏度用于设置触发系统的迟滞范围。
     * 灵敏度越高（值越小），触发越灵敏，但也越容易受噪声影响。
     * 单位：格（div），取值范围：通常 0.1 ~ 1.0。
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerCommon.setTriggerSensitivity(0.3)
     *     → triggersensitivity()
     *     → sendFpgaMsg(FPGA_CMD_TRIG_LEVEL, 0)
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_TRIG_LEVEL
     */
    public void triggersensitivity(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_TRIG_LEVEL,0);
    }
}
