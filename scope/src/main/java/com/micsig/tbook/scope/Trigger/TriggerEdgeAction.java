package com.micsig.tbook.scope.Trigger;

import com.micsig.tbook.scope.Action.FPGAMessage;
import com.micsig.tbook.scope.Action.XAction;
import com.micsig.tbook.scope.Event.EventFactory;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                 TriggerEdgeAction - 边沿触发器动作代理类                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的边沿触发器动作代理类，负责边沿触发器参数变更时的消息发送。     ║
 * ║   继承自XAction基类，复用统一的消息发送机制。                                 ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 触发源变更时发送复合FPGA消息                                            ║
 * ║   2. 触发边沿类型变更时发送复合FPGA消息                                      ║
 * ║   3. 触发耦合方式变更时发送FPGA消息                                          ║
 * ║   4. 发送UI事件通知界面刷新                                                  ║
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
 * ║   │Action    │ │Action    │ │Action    │ │Action    │ │Edge      │         ║
 * ║   │触发器    │ │水平轴    │ │垂直轴    │ │通道      │ │Action    │ ← 本类   ║
 * ║   └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘         ║
 * ║                                                                              ║
 * ║ 【消息流程】                                                                 ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │  用户操作（UI）                                                     │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  TriggerEdge.setTriggerEdge() / setTriggerCouple()                 │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  TriggerEdgeAction.TriggerEdgeChange() / TriggerCoupleChange()     │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  ┌─────────────────────────────────────────────────────────────┐   │   ║
 * ║   │  │ sendFpgaMsg(cmd)      → 发送FPGA命令                       │   │   ║
 * ║   │  │ sendEvent(EVENT)      → 发送UI事件                         │   │   ║
 * ║   │  └─────────────────────────────────────────────────────────────┘   │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  FPGA硬件更新 / UI界面刷新                                          │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【FPGA命令说明】                                                             ║
 * ║   ┌────────────────────────┬────────────────────────────────────────────┐  ║
 * ║   │ 命令                   │ 说明                                        │  ║
 * ║   ├────────────────────────┼────────────────────────────────────────────┤  ║
 * ║   │ FPGA_CMD_TRIG          │ 触发参数（边沿类型/耦合方式等）             │  ║
 * ║   │ FPGA_CMD_TRIG_OFFSET   │ 触发偏移（触发点在波形中的位置）            │  ║
 * ║   │ FPGA_CMD_TRIG_HUICHA   │ 触发回差（滞后电压，防止噪声误触发）        │  ║
 * ║   │ FPGA_CMD_TRIG_LEVEL    │ 触发电平（触发阈值电压）                    │  ║
 * ║   │ FPGA_CMD_TRIG_COUPLE   │ 触发耦合（直流/交流/高频抑制等）            │  ║
 * ║   └────────────────────────┴────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → TriggerEdge: 持有边沿触发器引用，获取触发参数                           ║
 * ║   → XAction: 继承基类，复用消息发送方法                                     ║
 * ║   → FPGAMessage: FPGA命令常量定义                                           ║
 * ║   → EventFactory: 事件常量定义                                              ║
 * ║   → ScopeMessage: 消息处理中心（通过XAction间接调用）                       ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户切换触发边沿（上升沿/下降沿/双边沿）时                              ║
 * ║   2. 用户切换触发耦合方式时                                                 ║
 * ║   3. 用户选择触发源通道时                                                   ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 代理模式（Proxy）：作为TriggerEdge的消息发送代理                        ║
 * ║   - 外观模式（Facade）：封装复杂的FPGA命令组合                              ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   本类无状态，所有方法通过XAction委托给ScopeMessage，线程安全由              ║
 * ║   ScopeMessage保证。                                                        ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018/3/20                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerEdgeAction extends XAction {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 关联的边沿触发器对象引用
     * 用于获取触发器参数（当前实现中未使用，预留扩展）
     *
     * @note 通过此引用，TriggerEdgeAction可以访问触发器的完整参数，
     *       未来可能用于构建更复杂的FPGA命令参数
     */
    private TriggerEdge triggerEdge;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造边沿触发器动作代理
     * 调用父类XAction构造方法初始化消息发送机制
     *
     * @param triggerEdge 关联的边沿触发器对象
     *                    用于获取触发器参数，支持后续扩展
     *
     * @example
     *   TriggerEdge trigger = new TriggerEdge();
     *   TriggerEdgeAction action = new TriggerEdgeAction(trigger);
     */
    public TriggerEdgeAction(TriggerEdge triggerEdge){
        this.triggerEdge = triggerEdge;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发源变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发源变更通知
     * 当边沿触发器的触发源发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：复合命令
     *     <ul>
     *       <li>FPGA_CMD_TRIG：触发参数</li>
     *       <li>FPGA_CMD_TRIG_OFFSET：触发偏移</li>
     *       <li>FPGA_CMD_TRIG_HUICHA：触发回差</li>
     *       <li>FPGA_CMD_TRIG_LEVEL：触发电平</li>
     *     </ul>
     *   </li>
     *   <li>UI事件：EVENT_TRIGGER_PARAM（触发参数变更事件）</li>
     * </ul>
     *
     * <p><b>为什么触发源变更需要发送更多命令？</b>
     * <ul>
     *   <li>触发源变更意味着切换到不同的通道，需要更新该通道的触发电平</li>
     *   <li>不同通道可能有不同的偏移和回差设置</li>
     *   <li>需要重新配置完整的触发系统</li>
     * </ul>
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerEdge.triggerSourceChange()
     *     → super.triggerSourceChange() → TriggerAction.triggerSourceChange()
     *     → TriggerEdgeAction.TriggerSourceChange()
     *     → sendFpgaMsg(TRIG|OFFSET|HUICHA|LEVEL)
     *     → sendEvent(EVENT_TRIGGER_PARAM)
     * </pre>
     *
     * <p><b>FPGA命令位运算说明：</b>
     * <pre>
     * FPGA_CMD_TRIG | FPGA_CMD_TRIG_OFFSET | FPGA_CMD_TRIG_HUICHA | FPGA_CMD_TRIG_LEVEL
     *
     * 组合结果是一个4位掩码，FPGAMessage.run() 会根据各bit位执行对应的寄存器更新
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_TRIG
     * @see FPGAMessage#FPGA_CMD_TRIG_OFFSET
     * @see FPGAMessage#FPGA_CMD_TRIG_HUICHA
     * @see FPGAMessage#FPGA_CMD_TRIG_LEVEL
     * @see EventFactory#EVENT_TRIGGER_PARAM
     */
    public void TriggerSourceChange() {
        sendFpgaMsg(FPGAMessage.FPGA_CMD_TRIG|FPGAMessage.FPGA_CMD_TRIG_OFFSET
                | FPGAMessage.FPGA_CMD_TRIG_HUICHA|FPGAMessage.FPGA_CMD_TRIG_LEVEL);
        sendEvent(EventFactory.EVENT_TRIGGER_PARAM);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发边沿类型变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发边沿类型变更通知
     * 当触发边沿类型（上升沿/下降沿/双边沿）发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：复合命令
     *     <ul>
     *       <li>FPGA_CMD_TRIG：触发参数（边沿类型）</li>
     *       <li>FPGA_CMD_TRIG_HUICHA：触发回差（不同边沿类型可能需要不同回差）</li>
     *       <li>FPGA_CMD_TRIG_LEVEL：触发电平</li>
     *       <li>FPGA_CMD_TRIG_COUPLE：触发耦合</li>
     *     </ul>
     *   </li>
     *   <li>UI事件：EVENT_TRIGGER_PARAM（触发参数变更事件）</li>
     * </ul>
     *
     * <p><b>边沿类型说明：</b>
     * <ul>
     *   <li><b>上升沿（TET_ASC）：</b>信号从低电平跳变到高电平时触发</li>
     *   <li><b>下降沿（TET_DSC）：</b>信号从高电平跳变到低电平时触发</li>
     *   <li><b>双边沿（TET_DUAL）：</b>上升沿和下降沿都触发</li>
     * </ul>
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerEdge.setTriggerEdge(TET_DSC)
     *     → TriggerEdgeAction.TriggerEdgeChange()
     *     → sendFpgaMsg(TRIG|HUICHA|LEVEL|COUPLE)
     *     → sendEvent(EVENT_TRIGGER_PARAM)
     * </pre>
     *
     * <p><b>FPGA命令位运算说明：</b>
     * <pre>
     * FPGA_CMD_TRIG | FPGA_CMD_TRIG_HUICHA | FPGA_CMD_TRIG_LEVEL | FPGA_CMD_TRIG_COUPLE
     *
     * 组合结果是一个4位掩码，FPGAMessage.run() 会根据各bit位执行对应的寄存器更新
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_TRIG
     * @see FPGAMessage#FPGA_CMD_TRIG_HUICHA
     * @see FPGAMessage#FPGA_CMD_TRIG_LEVEL
     * @see FPGAMessage#FPGA_CMD_TRIG_COUPLE
     * @see EventFactory#EVENT_TRIGGER_PARAM
     * @see TriggerEdge#TET_ASC
     * @see TriggerEdge#TET_DSC
     * @see TriggerEdge#TET_DUAL
     */
    public void TriggerEdgeChange() {
        sendFpgaMsg(FPGAMessage.FPGA_CMD_TRIG | FPGAMessage.FPGA_CMD_TRIG_HUICHA
                |FPGAMessage.FPGA_CMD_TRIG_LEVEL|FPGAMessage.FPGA_CMD_TRIG_COUPLE);
        sendEvent(EventFactory.EVENT_TRIGGER_PARAM);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发耦合方式变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发耦合方式变更通知
     * 当触发耦合方式（直流/交流/高频抑制等）发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：复合命令
     *     <ul>
     *       <li>FPGA_CMD_TRIG_COUPLE：触发耦合方式</li>
     *       <li>FPGA_CMD_TRIG_LEVEL：触发电平（耦合方式变更可能影响电平）</li>
     *     </ul>
     *   </li>
     *   <li>UI事件：EVENT_TRIGGER_PARAM（触发参数变更事件）</li>
     * </ul>
     *
     * <p><b>耦合方式说明：</b>
     * <ul>
     *   <li><b>直流耦合（COUPLING_DIRECT）：</b>直接耦合，通过所有频率分量</li>
     *   <li><b>交流耦合（COUPLING_AC）：</b>阻断直流分量，只通过交流分量</li>
     *   <li><b>高频抑制（COUPLING_HFRS）：</b>滤除高频噪声，只通过低频分量</li>
     *   <li><b>低频抑制（COUPLING_LFRS）：</b>滤除低频分量，只通过高频分量</li>
     *   <li><b>噪声抑制（COUPLING_NOISERS）：</b>增加迟滞范围，抑制噪声误触发</li>
     * </ul>
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerEdge.setTriggerCouple(COUPLING_AC)
     *     → TriggerEdgeAction.TriggerCoupleChange()
     *     → sendFpgaMsg(TRIG_COUPLE|TRIG_LEVEL)
     *     → sendEvent(EVENT_TRIGGER_PARAM)
     * </pre>
     *
     * <p><b>FPGA命令位运算说明：</b>
     * <pre>
     * FPGA_CMD_TRIG_COUPLE | FPGA_CMD_TRIG_LEVEL
     *
     * 组合结果是一个2位掩码，FPGAMessage.run() 会根据各bit位执行对应的寄存器更新
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_TRIG_COUPLE
     * @see FPGAMessage#FPGA_CMD_TRIG_LEVEL
     * @see EventFactory#EVENT_TRIGGER_PARAM
     * @see TriggerEdge#COUPLING_DIRECT
     * @see TriggerEdge#COUPLING_AC
     * @see TriggerEdge#COUPLING_HFRS
     * @see TriggerEdge#COUPLING_LFRS
     * @see TriggerEdge#COUPLING_NOISERS
     */
    public void TriggerCoupleChange(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_TRIG_COUPLE|FPGAMessage.FPGA_CMD_TRIG_LEVEL);
        sendEvent(EventFactory.EVENT_TRIGGER_PARAM);
    }

}
