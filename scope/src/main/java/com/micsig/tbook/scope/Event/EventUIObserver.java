package com.micsig.tbook.scope.Event;                                                     // 包声明：事件模块

import android.os.Handler;                                                                // 导入：Handler类，用于线程间通信
import android.os.Looper;                                                                 // 导入：Looper类，用于消息循环
import android.os.Message;                                                                // 导入：Message类，用于消息传递

import java.util.Observable;                                                              // 导入：被观察者类
import java.util.Observer;                                                                // 导入：观察者接口

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              EventUIObserver - UI线程事件观察者抽象类                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Event模块的UI线程事件观察者抽象类，位于Event包下，                           ║
 * ║   实现Observer接口，用于在UI线程中处理事件。                                   ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 实现Observer接口，作为事件观察者                                         ║
 * ║   2. 确保事件在UI线程（主线程）中处理                                         ║
 * ║   3. 区分事件来源：主线程或非主线程                                           ║
 * ║   4. 设置mainSender标识，避免循环调用                                         ║
 * ║                                                                              ║
 * ║ 【为什么需要这个类】                                                         ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        UI线程事件处理问题                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   【问题1】Android UI操作必须在主线程中执行                           │ ║
 * ║   │   - 事件可能从任何线程发送（如采样线程、事件线程等）                  │ ║
 * ║   │   - 直接在非UI线程中更新UI会导致异常                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【问题2】避免循环调用                                               │ ║
 * ║   │   - UI界面设置可能触发事件                                            │ ║
 * ║   │   - 事件处理又可能更新UI                                              │ ║
 * ║   │   - 如果不区分来源，可能形成循环调用                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【解决方案】                                                         │ ║
 * ║   │   - EventUIObserver自动将事件切换到主线程处理                         │ ║
 * ║   │   - 通过mainSender标识区分事件来源                                    │ ║
 * ║   │   - 主线程发送的事件不触发update()，避免循环                          │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【线程切换机制】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        线程切换流程                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【情况1：事件来自主线程】                                           │ ║
 * ║   │   用户操作UI ──▶ 事件发送 ──▶ EventUIObserver.update()               │ ║
 * ║   │                                    │                                 │ ║
 * ║   │                                    ▼                                 │ ║
 * ║   │                           isOnMainThread() = true                    │ ║
 * ║   │                                    │                                 │ ║
 * ║   │                                    ▼                                 │ ║
 * ║   │                           setMainSender(true)                        │ ║
 * ║   │                                    │                                 │ ║
 * ║   │                                    ▼                                 │ ║
 * ║   │                           不调用update()（避免循环）                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【情况2：事件来自非主线程】                                         │ ║
 * ║   │   采样线程 ──▶ 事件发送 ──▶ EventUIObserver.update()                 │ ║
 * ║   │                                    │                                 │ ║
 * ║   │                                    ▼                                 │ ║
 * ║   │                           isOnMainThread() = false                   │ ║
 * ║   │                                    │                                 │ ║
 * ║   │                                    ▼                                 │ ║
 * ║   │                           setMainSender(false)                       │ ║
 * ║   │                                    │                                 │ ║
 * ║   │                                    ▼                                 │ ║
 * ║   │                           发送Message到主线程                         │ ║
 * ║   │                                    │                                 │ ║
 * ║   │                                    ▼                                 │ ║
 * ║   │                           主线程Handler处理                           │ ║
 * ║   │                                    │                                 │ ║
 * ║   │                                    ▼                                 │ ║
 * ║   │                           调用update(Object data)                    │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【mainSender标识说明】                                                       ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        mainSender标识说明                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   【mainSender = true】                                               │ ║
 * ║   │   - 事件来自主线程（UI线程）                                          │ ║
 * ║   - 通常是用户操作UI触发的                                               │ ║
 * ║   │   - 不需要再次更新UI，避免循环                                        │ ║
 * ║   │                                                                      │ ║
 * ║   │   【mainSender = false】                                              │ ║
 * ║   │   - 事件来自非主线程（如采样线程、事件线程）                          │ ║
 * ║   │   - 需要更新UI显示                                                    │ ║
 * ║   │   - 通过Handler切换到主线程执行                                       │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【使用示例】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │   // 创建UI观察者                                                     │ ║
 * ║   │   public class MyUIObserver extends EventUIObserver {                │ ║
 * ║   │       @Override                                                      │ ║
 * ║   │       public void update(Object data) {                              │ ║
 * ║   │           EventBase event = (EventBase) data;                        │ ║
 * ║   │           // 在主线程中处理事件，可以安全更新UI                       │ ║
 * ║   │           textView.setText("Event: " + event.getId());               │ ║
 * ║   │       }                                                              │ ║
 * ║   │   }                                                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   // 注册观察者                                                       │ ║
 * ║   │   EventFactory.addEventObserver(                                     │ ║
 * ║   │       EventFactory.EVENT_TIME_SCALE,                                 │ ║
 * ║   │       new MyUIObserver()                                             │ ║
 * ║   │   );                                                                 │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【与普通Observer的区别】                                                     ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        Observer对比                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【普通Observer】                                                     │ ║
 * ║   │   - 事件在发送线程中处理                                              │ ║
 * ║   │   - 不能直接更新UI                                                    │ ║
 * ║   │   - 需要手动切换线程                                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【EventUIObserver】                                                  │ ║
 * ║   │   - 事件自动切换到主线程处理                                          │ ║
 * ║   │   - 可以直接更新UI                                                    │ ║
 * ║   │   - 自动区分事件来源                                                  │ ║
 * ║   │   - 避免循环调用                                                      │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. UI界面更新：在主线程中更新界面显示                                     ║
 * ║   2. 波形绘制：在主线程中触发波形重绘                                       ║
 * ║   3. 测量值显示：在主线程中更新测量值                                       ║
 * ║   4. 状态提示：在主线程中显示状态信息                                       ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 观察者模式：实现Observer接口                                            ║
 * ║   - 模板方法模式：抽象类定义流程，子类实现具体处理                          ║
 * ║   - 线程切换模式：使用Handler切换线程                                       ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - Handler保证消息在主线程中处理                                          ║
 * ║   - isOnMainThread()判断当前线程                                           ║
 * ║   - mainSender标识区分事件来源                                             ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - Observer: 观察者接口                                                  ║
 * ║   - Observable: 被观察者类                                                ║
 * ║   - EventBase: 事件基类                                                   ║
 * ║   - EventFactory: 事件工厂                                                ║
 * ║   - Handler/Looper: Android线程通信机制                                   ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * UI线程事件观察者抽象类
 * 实现Observer接口，确保事件在UI线程（主线程）中处理
 * 区分事件来源，避免循环调用
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>实现Observer接口</li>
 *   <li>自动将事件切换到主线程处理</li>
 *   <li>区分主线程和非主线程事件来源</li>
 *   <li>设置mainSender标识</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * public class MyUIObserver extends EventUIObserver {
 *     @Override
 *     public void update(Object data) {
 *         EventBase event = (EventBase) data;
 *         // 在主线程中处理事件，可以安全更新UI
 *         textView.setText("Event: " + event.getId());
 *     }
 * }
 *
 * // 注册观察者
 * EventFactory.addEventObserver(EventFactory.EVENT_TIME_SCALE, new MyUIObserver());
 * </pre>
 *
 * @see Observer
 * @see EventBase
 * @see EventFactory
 */
public abstract class EventUIObserver implements Observer {                                 // 类声明：UI线程事件观察者抽象类，实现Observer接口

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 消息类型：切换到主线程执行 */
    public static final int EVENT_TO_MAIN_RUN = 0x1826;                                    // 静态常量：消息类型，用于标识需要切换到主线程执行的消息

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 主线程Handler，用于将事件切换到主线程处理 */
    private Handler mainHandler;                                                           // 成员变量：主线程Handler

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数
     * 初始化主线程Handler，用于将事件切换到主线程处理
     */
    public EventUIObserver() {                                                             // 构造方法：初始化UI观察者
        mainHandler = new Handler(Looper.getMainLooper()) {                                // 创建Handler，绑定到主线程的Looper
            @Override
            public void handleMessage(Message msg) {                                       // 重写方法：处理消息
                if (msg.what == EVENT_TO_MAIN_RUN) {                                       // 如果是切换到主线程执行的消息
                    update(msg.obj);                                                       // 调用抽象方法update(Object)处理事件
                }                                                                           // if语句结束
            }                                                                               // 方法结束
        };                                                                                  // Handler初始化结束
    }                                                                                       // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 抽象方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 抽象方法：处理事件
     * 子类必须实现此方法，在主线程中处理事件
     *
     * <p><b>注意：</b></p>
     * <ul>
     *   <li>此方法在主线程中调用，可以安全更新UI</li>
     *   <li>参数data通常是EventBase对象</li>
     * </ul>
     *
     * @param data 事件数据对象
     */
    public abstract void update(Object data);                                              // 抽象方法：处理事件，子类必须实现

    // ═══════════════════════════════════════════════════════════════════════════════
    // Observer接口实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Observer接口实现：接收事件通知
     * 根据当前线程判断事件来源，并决定是否切换线程
     *
     * <p><b>处理逻辑：</b></p>
     * <ul>
     *   <li>如果在主线程：设置mainSender=true，不调用update()避免循环</li>
     *   <li>如果在非主线程：设置mainSender=false，切换到主线程执行update()</li>
     * </ul>
     *
     * @param observable 被观察者对象
     * @param data       事件数据对象
     */
    @Override
    public void update(Observable observable, Object data) {                               // 重写方法：Observer接口的update方法
        EventBase eventBase = (EventBase) data;                                            // 将事件数据转换为EventBase类型
        if (isOnMainThread()) {                                                            // 如果当前在主线程
            eventBase.setMainSender(true);                                                 // 设置mainSender为true，表示事件来自主线程
            // 如果是主线程发过来的消息，则都是来自于界面设置的反馈，暂时不需要进行下发更新，以免形成循环调用
            // 注释掉：update(data);                                                       // 不调用update()，避免循环调用
        } else {                                                                           // 否则（当前不在主线程）
            eventBase.setMainSender(false);                                                // 设置mainSender为false，表示事件来自非主线程
            Message msg = mainHandler.obtainMessage();                                     // 从Handler获取消息对象
            msg.obj = data;                                                                // 设置消息对象为事件数据
            msg.what = EVENT_TO_MAIN_RUN;                                                  // 设置消息类型为切换到主线程执行
            mainHandler.sendMessage(msg);                                                  // 发送消息到主线程
        }                                                                                   // if-else语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 工具方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断当前是否在主线程
     * 通过比较当前线程和主线程Looper的线程来判断
     *
     * @return true=当前在主线程，false=当前不在主线程
     */
    public static boolean isOnMainThread() {                                               // 静态方法：判断当前是否在主线程
        return Thread.currentThread() == Looper.getMainLooper().getThread();               // 比较当前线程和主线程Looper的线程是否相同
    }                                                                                       // 方法结束
}                                                                                           // 类结束
