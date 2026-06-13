package com.micsig.tbook.scope.Trigger;

import com.micsig.tbook.scope.Action.FPGAMessage;
import com.micsig.tbook.scope.Action.XAction;
import com.micsig.tbook.scope.Event.EventFactory;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    TriggerAction - 触发器动作代理类                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的动作代理类，负责触发器参数变更时的消息发送和事件通知。         ║
 * ║   继承自XAction基类，复用统一的消息发送机制。                                 ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 触发参数变更时发送FPGA消息（触发类型、触发条件等）                       ║
 * ║   2. 触发源变更时发送复合FPGA消息（触发源、偏移、回差、电平）                 ║
 * ║   3. 发送UI事件通知界面刷新                                                  ║
 * ║   4. 触发源变更时同步通道采样状态                                            ║
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
 * ║   │Trigger   │ │Horizontal│ │Vertical  │ │Channel   │ │Math      │         ║
 * ║   │Action    │ │Action    │ │Action    │ │Action    │ │Action    │         ║
 * ║   │触发器    │ │水平轴    │ │垂直轴    │ │通道      │ │数学运算  │         ║
 * ║   └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘         ║
 * ║                                                                              ║
 * ║ 【消息流程】                                                                 ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │  用户操作（UI）                                                     │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  Trigger.setTriggerSource() / setTriggerParam()                    │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  Trigger.triggerSourceChange() / triggerParamChange()              │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  TriggerAction.triggerSourceChange() / triggerParamChange()        │   ║
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
 * ║   ┌────────────────────────┬────────────────────────────────────────────┐  ║
 * ║   │ 命令                   │ 说明                                        │  ║
 * ║   ├────────────────────────┼────────────────────────────────────────────┤  ║
 * ║   │ FPGA_CMD_TRIG          │ 触发参数（类型/条件/模式等）                │  ║
 * ║   │ FPGA_CMD_TRIG_OFFSET   │ 触发偏移（触发点在波形中的位置）            │  ║
 * ║   │ FPGA_CMD_TRIG_HUICHA   │ 触发回差（滞后电压，防止噪声误触发）        │  ║
 * ║   │ FPGA_CMD_TRIG_LEVEL    │ 触发电平（触发阈值电压）                    │  ║
 * ║   └────────────────────────┴────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → Trigger: 持有触发器引用，获取触发参数                                   ║
 * ║   → XAction: 继承基类，复用消息发送方法                                     ║
 * ║   → FPGAMessage: FPGA命令常量定义                                           ║
 * ║   → EventFactory: 事件常量定义                                              ║
 * ║   → ScopeMessage: 消息处理中心（通过XAction间接调用）                       ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户切换触发源时，Trigger调用triggerSourceChange()                     ║
 * ║   2. 用户调整触发电平时，子类调用triggerParamChange()                       ║
 * ║   3. 用户切换触发类型时，TriggerFactory更新触发器                           ║
 * ║   4. 自动设置功能调整触发参数时                                             ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 代理模式（Proxy）：作为Trigger的消息发送代理                            ║
 * ║   - 外观模式（Facade）：封装复杂的FPGA命令组合                              ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   本类无状态，所有方法通过XAction委托给ScopeMessage，线程安全由              ║
 * ║   ScopeMessage保证。                                                        ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-6-27                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerAction extends XAction {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 关联的触发器对象引用
     * 用于获取触发器参数（当前实现中未使用，预留扩展）
     *
     * @note 通过此引用，TriggerAction可以访问触发器的完整参数，
     *       未来可能用于构建更复杂的FPGA命令参数
     */
    public Trigger trigger;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造触发器动作代理
     * 调用父类XAction构造方法初始化消息发送机制
     *
     * @param trigger 关联的触发器对象
     *                 用于获取触发器参数，支持后续扩展
     *
     * @example
     *   Trigger trigger = new TriggerEdge();
     *   TriggerAction action = new TriggerAction(trigger);
     */
    public TriggerAction(Trigger trigger){
        this.trigger = trigger;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发参数变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发参数变更通知
     * 当触发器的参数（如触发条件、触发模式等）发生变化时调用
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>FPGA消息：FPGA_CMD_TRIG（触发参数命令）</li>
     *   <li>UI事件：EVENT_TRIGGER_PARAM（触发参数变更事件）</li>
     * </ul>
     *
     * <p><b>适用场景：</b>
     * <ul>
     *   <li>边沿触发：上升沿/下降沿/双边沿切换</li>
     *   <li>脉宽触发：脉宽条件变更（大于/小于/等于/不等于）</li>
     *   <li>逻辑触发：逻辑模式变更（AND/OR/NAND/NOR）</li>
     *   <li>斜率触发：斜率条件变更</li>
     *   <li>超时触发：超时时间变更</li>
     * </ul>
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerEdge.setTriggerEdge() → triggerEdgeAction.TriggerEdgeChange()
     *     → triggerParamChange() → sendFpgaMsg(FPGA_CMD_TRIG)
     * </pre>
     *
     * @see FPGAMessage#FPGA_CMD_TRIG
     * @see EventFactory#EVENT_TRIGGER_PARAM
     */
    public void triggerParamChange(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_TRIG);
        sendEvent(EventFactory.EVENT_TRIGGER_PARAM);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发源变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发源变更通知
     * 当触发器的触发源（触发通道）发生变化时调用
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
     *         <li>FPGA_CMD_TRIG：触发参数</li>
     *         <li>FPGA_CMD_TRIG_OFFSET：触发偏移</li>
     *         <li>FPGA_CMD_TRIG_HUICHA：触发回差</li>
     *         <li>FPGA_CMD_TRIG_LEVEL：触发电平</li>
     *       </ul>
     *   </li>
     *   <li>sendEvent()：发送UI事件
     *       <ul>
     *         <li>EVENT_TRIGGER_PARAM：通知界面刷新触发参数显示</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <p><b>为什么触发源变更需要发送更多命令？</b>
     * <ul>
     *   <li>触发源变更意味着切换到不同的通道，需要更新该通道的触发电平</li>
     *   <li>不同通道可能有不同的偏移和回差设置</li>
     *   <li>通道采样状态可能需要调整（如通道开启/关闭状态）</li>
     * </ul>
     *
     * <p><b>调用链：</b>
     * <pre>
     * Trigger.setTriggerSource(CH1) → triggerSourceChange()
     *     → sendChSample() → 更新采样状态
     *     → sendFpgaMsg(TRIG|OFFSET|HUICHA|LEVEL) → 更新FPGA
     *     → sendEvent(EVENT_TRIGGER_PARAM) → 刷新UI
     * </pre>
     *
     * <p><b>FPGA命令位运算说明：</b>
     * <pre>
     * FPGA_CMD_TRIG | FPGA_CMD_TRIG_OFFSET | FPGA_CMD_TRIG_HUICHA | FPGA_CMD_TRIG_LEVEL
     *
     * 假设各命令定义如下：
     * FPGA_CMD_TRIG        = 0x01 (bit 0)
     * FPGA_CMD_TRIG_OFFSET = 0x02 (bit 1)
     * FPGA_CMD_TRIG_HUICHA = 0x04 (bit 2)
     * FPGA_CMD_TRIG_LEVEL  = 0x08 (bit 3)
     *
     * 组合结果 = 0x0F (bit 0-3 全部置1)
     * FPGAMessage.run() 会根据各bit位执行对应的寄存器更新
     * </pre>
     *
     * @see #triggerParamChange() 触发参数变更（仅发送TRIG命令）
     * @see FPGAMessage#FPGA_CMD_TRIG
     * @see FPGAMessage#FPGA_CMD_TRIG_OFFSET
     * @see FPGAMessage#FPGA_CMD_TRIG_HUICHA
     * @see FPGAMessage#FPGA_CMD_TRIG_LEVEL
     * @see EventFactory#EVENT_TRIGGER_PARAM
     */
    public void triggerSourceChange(){
        sendChSample();
        sendFpgaMsg(FPGAMessage.FPGA_CMD_TRIG|FPGAMessage.FPGA_CMD_TRIG_OFFSET
                | FPGAMessage.FPGA_CMD_TRIG_HUICHA|FPGAMessage.FPGA_CMD_TRIG_LEVEL);
        sendEvent(EventFactory.EVENT_TRIGGER_PARAM);
    }
}
