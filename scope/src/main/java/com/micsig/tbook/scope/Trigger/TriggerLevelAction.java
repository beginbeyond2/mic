package com.micsig.tbook.scope.Trigger;

import com.micsig.base.FilterThread;
import com.micsig.tbook.scope.Action.FPGAMessage;
import com.micsig.tbook.scope.Action.XAction;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Scope;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                TriggerLevelAction - 触发电平动作代理类                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的触发电平动作代理类，负责触发电平变更时的消息发送。             ║
 * ║   继承自XAction基类，复用统一的消息发送机制。                                 ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 触发电平变更时发送FPGA消息（带防抖机制）                                ║
 * ║   2. 触发电平变更时发送UI事件                                                ║
 * ║   3. 用户触发电平变更时发送用户事件                                          ║
 * ║                                                                              ║
 * ║ 【防抖机制说明】                                                             ║
 * ║   使用FilterThread实现100ms防抖，避免用户快速拖动触发电平时发送过多FPGA消息。║
 * ║                                                                              ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                     防抖机制流程图                                   │   ║
 * ║   │                                                                     │   ║
 * ║   │  用户拖动触发电平                                                   │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  setPos() → TriggerLevelChange()                                   │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  filterThread.run() → 启动100ms定时器                               │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  100ms内新的调用 → 重置定时器                                       │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  100ms无新调用 → 执行Runnable                                       │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  检查电平值是否变化                                                 │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  发送FPGA_CMD_TRIG_LEVEL命令                                        │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
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
 * ║   │Action    │ │Action    │ │Action    │ │Action    │ │Level     │         ║
 * ║   │触发器    │ │水平轴    │ │垂直轴    │ │通道      │ │Action    │ ← 本类   ║
 * ║   └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘         ║
 * ║                                                                              ║
 * ║ 【消息流程】                                                                 ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │  用户操作（UI）                                                     │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  TriggerLevel.setPos(2.5, true)                                     │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  TriggerLevelAction.TriggerLevelChange()                           │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  ┌─────────────────────────────────────────────────────────────┐   │   ║
 * ║   │  │ filterThread.run()      → 启动100ms防抖定时器               │   │   ║
 * ║   │  │ sendEvent(EVENT_TRIGGER_LEVEL) → 发送UI事件                 │   │   ║
 * ║   │  └─────────────────────────────────────────────────────────────┘   │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  100ms后执行Runnable：                                              │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  ┌─────────────────────────────────────────────────────────────┐   │   ║
 * ║   │  │ 检查电平值是否变化                                           │   │   ║
 * ║   │  │ sendFpgaMsg(FPGA_CMD_TRIG_LEVEL) → 发送FPGA命令             │   │   ║
 * ║   │  └─────────────────────────────────────────────────────────────┘   │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  triggerLevelUserChange()（如果bUser为true）                       │   ║
 * ║   │       ↓                                                             │   ║
 * ║   │  sendEvent(EVENT_TRIGGER_LEVEL_USER) → 发送用户事件                │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【FPGA命令说明】                                                             ║
 * ║   ┌────────────────────────┬────────────────────────────────────────────┐  ║
 * ║   │ 命令                   │ 说明                                        │  ║
 * ║   ├────────────────────────┼────────────────────────────────────────────┤  ║
 * ║   │ FPGA_CMD_TRIG_LEVEL    │ 触发电平（触发阈值电压）                    │  ║
 * ║   └────────────────────────┴────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → TriggerLevel: 持有触发电平引用，获取触发电平值                          ║
 * ║   → XAction: 继承基类，复用消息发送方法                                     ║
 * ║   → FilterThread: 防抖线程，实现100ms防抖                                   ║
 * ║   → FPGAMessage: FPGA命令常量定义                                           ║
 * ║   → EventFactory: 事件常量定义                                              ║
 * ║   → Scope: 示波器实例，判断是否为单次触发模式                               ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户拖动触发电平时，通过防抖机制发送FPGA消息                           ║
 * ║   2. 程序自动调整触发电平时，发送FPGA消息                                   ║
 * ║   3. 显示触发电平位置时，发送UI事件刷新界面                                 ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 代理模式（Proxy）：作为TriggerLevel的消息发送代理                       ║
 * ║   - 防抖模式（Debounce）：使用FilterThread实现防抖                          ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   本类无状态（level变量仅用于防抖判断），所有方法通过XAction委托给           ║
 * ║   ScopeMessage，线程安全由ScopeMessage保证。                                ║
 * ║   FilterThread内部有同步机制，保证Runnable只执行一次。                       ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-4-10                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerLevelAction extends XAction {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于FilterThread的线程命名和日志输出
     */
    private static final String TAG = "TriggerLevelAction";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 关联的触发电平对象引用
     * 用于获取触发电平值（FPGA单位）
     */
    private TriggerLevel triggerLevel;

    /**
     * 防抖线程
     * 实现100ms防抖，避免用户快速拖动触发电平时发送过多FPGA消息
     *
     * <p><b>防抖原理：</b>
     * <ul>
     *   <li>每次调用run()方法时，重置100ms定时器</li>
     *   <li>只有100ms内没有新的调用，才执行Runnable</li>
     *   <li>避免短时间内发送多个FPGA消息</li>
     * </ul>
     *
     * @see FilterThread
     */
    private FilterThread filterThread = new FilterThread(TAG);

    /**
     * 上次发送的触发电平值
     * 用于判断电平值是否变化，避免重复发送FPGA消息
     *
     * <p><b>初始值：</b>Integer.MAX_VALUE，表示首次一定会发送
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>用户拖动触发电平到同一位置，不重复发送FPGA消息</li>
     *   <li>单次触发模式下，即使电平值相同也会发送（因为需要重新触发）</li>
     * </ul>
     */
    private int level = Integer.MAX_VALUE;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造触发电平动作代理
     * 初始化防抖线程和Runnable
     *
     * @param triggerLevel 关联的触发电平对象
     *                     用于获取触发电平值
     *
     * <p><b>初始化流程：</b>
     * <ol>
     *   <li>保存触发电平对象引用</li>
     *   <li>设置防抖延迟时间为100ms</li>
     *   <li>创建Runnable，在防抖延迟后执行：
     *     <ul>
     *       <li>获取当前触发电平值（FPGA单位）</li>
     *       <li>判断电平值是否变化或是否为单次触发模式</li>
     *       <li>如果需要，发送FPGA_CMD_TRIG_LEVEL命令</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><b>Runnable执行逻辑：</b>
     * <pre>
     * int v = (int) Math.round(triggerLevel.getPos());  // 获取当前电平值
     * if (level != v || !Scope.getInstance().isSingle()) {
     *     // 电平值变化 或 非单次触发模式 → 发送FPGA消息
     *     level = v;  // 保存当前电平值
     *     sendFpgaMsg(FPGA_CMD_TRIG_LEVEL);  // 发送FPGA命令
     * }
     * </pre>
     *
     * <p><b>为什么单次触发模式下即使电平值相同也要发送？</b>
     * 单次触发模式下，用户可能需要重新触发采集，即使电平值没有变化。
     *
     * @example
     *   TriggerLevel triggerLevel = new TriggerLevel(TriggerLevel.TRIGGER_LEVEL_NORMAL);
     *   TriggerLevelAction action = new TriggerLevelAction(triggerLevel);
     */
    public TriggerLevelAction(final TriggerLevel triggerLevel){
        this.triggerLevel = triggerLevel;
        filterThread.setDelayMillis(100);
        filterThread.setRunnable(new Runnable() {
            @Override
            public void run() {
                int v = (int) Math.round(triggerLevel.getPos());
                if(level != v || !Scope.getInstance().isSingle() ) {
                    level = v;
                    sendFpgaMsg(FPGAMessage.FPGA_CMD_TRIG_LEVEL);
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发电平变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发电平变更通知
     * 启动防抖定时器并发送UI事件
     *
     * <p><b>发送的消息：</b>
     * <ol>
     *   <li>filterThread.run()：启动100ms防抖定时器
     *     <ul>
     *       <li>100ms后执行Runnable</li>
     *       <li>如果电平值变化，发送FPGA_CMD_TRIG_LEVEL命令</li>
     *     </ul>
     *   </li>
     *   <li>sendEvent(EVENT_TRIGGER_LEVEL)：发送UI事件
     *     <ul>
     *       <li>通知界面刷新触发电平显示</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><b>防抖机制：</b>
     * <pre>
     * 用户快速拖动触发电平：
     *   t=0ms:   setPos(1.0) → TriggerLevelChange() → filterThread.run() → 启动100ms定时器
     *   t=50ms:  setPos(1.5) → TriggerLevelChange() → filterThread.run() → 重置100ms定时器
     *   t=80ms:  setPos(2.0) → TriggerLevelChange() → filterThread.run() → 重置100ms定时器
     *   t=180ms: 定时器到期 → 执行Runnable → 发送FPGA消息（电平值=2.0）
     *
     * 结果：只在用户停止拖动100ms后发送一次FPGA消息，而不是发送3次
     * </pre>
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerLevel.setPos(2.5, true)
     *     → TriggerLevelAction.TriggerLevelChange()
     *     → filterThread.run() → 启动100ms防抖定时器
     *     → sendEvent(EVENT_TRIGGER_LEVEL) → 发送UI事件
     * </pre>
     *
     * @see FilterThread#run()
     * @see EventFactory#EVENT_TRIGGER_LEVEL
     */
    public void TriggerLevelChange(){
        filterThread.run();
        sendEvent(EventFactory.EVENT_TRIGGER_LEVEL);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 用户触发电平变更方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 用户触发电平变更通知
     * 当用户手动调整触发电平时，发送用户变更事件
     *
     * <p><b>发送的消息：</b>
     * <ul>
     *   <li>UI事件：EVENT_TRIGGER_LEVEL_USER（用户触发电平变更事件）</li>
     * </ul>
     *
     * <p><b>用户事件 vs 普通事件：</b>
     * <ul>
     *   <li><b>EVENT_TRIGGER_LEVEL：</b>触发电平变更事件，所有变更都会发送</li>
     *   <li><b>EVENT_TRIGGER_LEVEL_USER：</b>用户触发电平变更事件，只有用户手动调整时才发送</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>记录用户操作日志</li>
     *   <li>触发自动设置功能</li>
     *   <li>更新触发电平指示器位置</li>
     * </ul>
     *
     * <p><b>调用链：</b>
     * <pre>
     * TriggerLevel.setPos(2.5, true)  // bUser=true
     *     → TriggerLevelAction.TriggerLevelChange()
     *     → TriggerLevelAction.triggerLevelUserChange()  // 因为bUser=true
     *     → sendEvent(EVENT_TRIGGER_LEVEL_USER)
     * </pre>
     *
     * @see EventFactory#EVENT_TRIGGER_LEVEL_USER
     */
    public void triggerLevelUserChange(){
        sendEvent(EventFactory.EVENT_TRIGGER_LEVEL_USER);
    }
}
