package com.micsig.tbook.scope.Event;                                                     // 包声明：事件模块

import android.os.Handler;                                                                // 导入：Handler类，用于异步消息处理
import android.os.HandlerThread;                                                          // 导入：HandlerThread类，用于创建后台线程
import android.os.Message;                                                                // 导入：Message类，用于消息传递

import java.util.ArrayList;                                                               // 导入：动态数组类
import java.util.List;                                                                    // 导入：列表接口
import java.util.Observable;                                                              // 导入：被观察者类
import java.util.Observer;                                                                // 导入：观察者接口

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              EventFactory - 示波器事件工厂类                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Event模块的事件工厂类，位于Event包下，                                       ║
 * ║   是示波器事件系统的核心管理类。                                               ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 定义所有事件类型常量（共117个事件）                                       ║
 * ║   2. 管理事件观察者的注册和注销                                                ║
 * ║   3. 分发事件到所有注册的观察者                                                ║
 * ║   4. 支持同步和异步事件分发                                                    ║
 * ║   5. 支持事件启用/禁用机制                                                     ║
 * ║                                                                              ║
 * ║ 【单例模式】                                                                 ║
 * ║   采用双重检查锁定（Double-Check Locking）实现单例模式                        ║
 * ║   - volatile保证instance变量的可见性                                         ║
 * ║   - synchronized保证线程安全                                                 ║
 * ║                                                                              ║
 * ║ 【事件系统架构】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        事件系统架构                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【事件生产者】                                                       │ ║
 * ║   │   - 各种模块（通道、触发、水平轴、测量等）                             │ ║
 * ║   │   - 调用EventFactory.sendEvent()发送事件                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【EventFactory】事件工厂                                             │ ║
 * ║   │   - 管理事件常量定义                                                  │ ║
 * ║   │   - 维护Observable数组（每个事件类型一个Observable）                  │ ║
 * ║   │   - 同步/异步分发事件                                                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   【事件消费者】                                                       │ ║
 * ║   │   - 实现Observer接口的类                                              │ ║
 * ║   │   - 调用addEventObserver()注册                                        │ ║
 * ║   │   - 在update()方法中处理事件                                          │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【事件分类】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        事件分类                                       │ ║
 * ║   │                                                                      │ ║
 * ║   │   【通道事件】                                                         │ ║
 * ║   │   - EVENT_CHANNEL_OPEN: 通道打开                                     │ ║
 * ║   │   - EVENT_CHANNEL_CLOSE: 通道关闭                                    │ ║
 * ║   │   - EVENT_CHANNEL_ACTIVE: 通道激活                                   │ ║
 * ║   │   - EVENT_CHANNEL_VSCALE: 通道档位                                   │ ║
 * ║   │   - EVENT_CHANNEL_POS: 通道位置                                      │ ║
 * ║   │                                                                      │ ║
 * ║   │   【时基事件】                                                         │ ║
 * ║   │   - EVENT_TIME_SCALE: 时基档位                                       │ ║
 * ║   │   - EVENT_TIME_POS: 时基位置                                         │ ║
 * ║   │                                                                      │ ║
 * ║   │   【触发事件】                                                         │ ║
 * ║   │   - EVENT_TRIGGER_TYPE: 触发类型                                     │ ║
 * ║   │   - EVENT_TRIGGER_LEVEL: 触发电平位置                                │ ║
 * ║   │   - EVENT_TRIGGER_PARAM: 触发参数                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【波形事件】                                                         │ ║
 * ║   │   - EVENT_CH_WAVE_UPDATE: 通道波形更新                               │ ║
 * ║   │   - EVENT_MATH_WAVE_UPDATE: 数学波形更新                             │ ║
 * ║   │   - EVENT_REF_WAVE_UPDATE: 参考波形更新                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【测量事件】                                                         │ ║
 * ║   │   - EVENT_CH_MEASURE_UPDATE: 通道测量更新                            │ ║
 * ║   │   - EVENT_MATH_MEASURE_UPDATE: 数学测量更新                          │ ║
 * ║   │   - EVENT_REF_MEASURE_UPDATE: 参考测量更新                           │ ║
 * ║   │                                                                      │ ║
 * ║   │   【显示事件】                                                         │ ║
 * ║   │   - EVENT_DISPLAY_ZOOM: 显示缩放                                     │ ║
 * ║   │   - EVENT_DISPLAY_MODE: 显示模式                                     │ ║
 * ║   │   - EVENT_DISPLAY_CCT: 色温设置                                      │ ║
 * ║   │   - EVENT_DISPLAY_BACKGROUND: 背景设置                               │ ║
 * ║   │                                                                      │ ║
 * ║   │   【余辉事件】                                                         │ ║
 * ║   │   - EVENT_AFTERGLOW_CLEAR: 清除余辉                                  │ ║
 * ║   │   - EVENT_AFTERGLOW_TIME: 余辉时间                                   │ ║
 * ║   │   - EVENT_AFTERGLOW_ENABLE: 余辉使能                                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   【探头事件】                                                         │ ║
 * ║   │   - EVENT_PROBE_EVENT: 探头事件                                      │ ║
 * ║   │   - EVENT_PROBE_ZERO: 探头零点校准                                   │ ║
 * ║   │   - EVENT_PROBE_UPGRADE: 探头升级                                    │ ║
 * ║   │   - EVENT_PROBE_ALARM: 探头告警                                      │ ║
 * ║   │   - EVENT_PROBE_PLUG: 探头插入                                       │ ║
 * ║   │   - EVENT_PROBE_UNPLUG: 探头拔出                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【校准事件】                                                         │ ║
 * ║   │   - EVENT_SELF_CALIBRATE_BEGIN: 自校准开始                           │ ║
 * ║   │   - EVENT_SELF_CALIBRATE_END: 自校准结束                             │ ║
 * ║   │   - EVENT_FACTOR_CALIBRATE_BEGIN: 工厂校准开始                       │ ║
 * ║   │   - EVENT_FACTOR_CALIBRATE_END: 工厂校准结束                         │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【同步/异步事件】                                                            ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        同步/异步事件对比                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【同步事件】                                                         │ ║
 * ║   │   - 在调用线程中立即执行                                              │ ║
 * ║   │   - 阻塞调用线程直到所有观察者处理完成                                │ ║
 * ║   │   - 适用于需要立即响应的事件                                          │ ║
 * ║   │   - 调用方式：sendEvent(eventBase, false)                            │ ║
 * ║   │                                                                      │ ║
 * ║   │   【异步事件】                                                         │ ║
 * ║   │   - 在事件线程（EventFactory线程）中执行                              │ ║
 * ║   │   - 不阻塞调用线程                                                    │ ║
 * ║   │   - 适用于耗时操作或需要延迟处理的事件                                │ ║
 * ║   │   - 调用方式：sendEvent(eventBase, true)                             │ ║
 * ║   │   - 支持延迟发送：sendEvent(eventBase, true, delayMs)                │ ║
 * ║   │   - 相同事件会被合并（只保留最后一个）                                │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【事件启用/禁用机制】                                                        ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        事件启用/禁用机制                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【禁用状态】                                                         │ ║
 * ║   │   - setEnable(false)禁用事件分发                                     │ ║
 * ║   │   - 部分关键事件会被缓存到列表中                                      │ ║
 * ║   │   - 缓存的事件包括：                                                  │ ║
 * ║   │     EVENT_CHANNEL_OPEN, EVENT_WAVE_CLEAR, EVENT_SCOPE_STATE,        │ ║
 * ║   │     EVENT_DISPLAY_CCT, EVENT_AFTERGLOW_ENABLE, EVENT_DISPLAY_MODE,  │ ║
 * ║   │     EVENT_AFTERGLOW_CLEAR, EVENT_BUS_CH_CHANGE, EVENT_DISPLAY_      │ ║
 * ║   │     BACKGROUND                                                       │ ║
 * ║   │                                                                      │ ║
 * ║   │   【启用状态】                                                         │ ║
 * ║   │   - setEnable(true)启用事件分发                                      │ ║
 * ║   │   - 发送所有缓存的事件                                                │ ║
 * ║   │   - 清空缓存列表                                                      │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 通道状态变化：通知通道打开/关闭/激活                                   ║
 * ║   2. 时基变化：通知界面更新时基显示                                         ║
 * ║   3. 触发设置变化：通知触发模式更新                                         ║
 * ║   4. 波形更新：通知波形重绘                                                 ║
 * ║   5. 测量更新：通知测量值更新                                               ║
 * ║   6. 校准过程：通知校准开始/结束                                            ║
 * ║   7. 探头事件：通知探头插入/拔出/校准                                       ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 单例模式：保证全局只有一个EventFactory实例                              ║
 * ║   - 观察者模式：管理事件观察者                                              ║
 * ║   - 工厂模式：创建和分发事件                                                ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 单例使用双重检查锁定保证线程安全                                        ║
 * ║   - 事件启用/禁用使用synchronized保护                                      ║
 * ║   - 异步事件在HandlerThread中处理                                          ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - EventBase: 事件基类                                                   ║
 * ║   - EventObservable: 事件被观察者类                                       ║
 * ║   - Observer: 观察者接口                                                  ║
 * ║   - Handler/HandlerThread: 异步消息处理                                   ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器事件工厂类
 * 单例模式，管理示波器的事件系统
 * 负责事件常量定义、观察者管理、事件分发
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>定义117个事件类型常量</li>
 *   <li>管理事件观察者的注册和注销</li>
 *   <li>支持同步和异步事件分发</li>
 *   <li>支持事件启用/禁用机制</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 注册事件观察者
 * EventFactory.addEventObserver(EventFactory.EVENT_TIME_SCALE, this);
 *
 * // 发送同步事件
 * EventFactory.sendEvent(EventFactory.EVENT_TIME_SCALE);
 *
 * // 发送异步事件
 * EventFactory.sendEvent(new EventBase(EventFactory.EVENT_TIME_SCALE), true);
 *
 * // 发送延迟事件（100ms后）
 * EventFactory.sendEvent(new EventBase(EventFactory.EVENT_TIME_SCALE), true, 100);
 *
 * // 注销事件观察者
 * EventFactory.delEventObserver(EventFactory.EVENT_TIME_SCALE, this);
 * </pre>
 *
 * @see EventBase
 * @see EventObservable
 * @see Observer
 */
public class EventFactory {                                                                // 类声明：事件工厂类

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例实例
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 单例实例，使用volatile保证多线程可见性 */
    private static volatile EventFactory instance = null;                                  // 静态变量：单例实例，使用volatile修饰

    /**
     * 获取单例实例
     * 使用双重检查锁定（Double-Check Locking）保证线程安全
     *
     * @return EventFactory单例实例
     */
    public static EventFactory getInstance() {                                             // 静态方法：获取单例实例
        if (instance == null) {                                                            // 第一次检查：实例是否为null
            synchronized (EventFactory.class) {                                            // 同步块：锁定类对象
                if (instance == null) {                                                    // 第二次检查：实例是否为null
                    instance = new EventFactory();                                         // 创建单例实例
                }                                                                           // if语句结束
            }                                                                               // 同步块结束
        }                                                                                   // if语句结束
        return instance;                                                                   // 返回单例实例
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件类型常量定义（共117个事件）
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 最小事件ID */
    public static final int EVENT_MIN = 0;                                                 // 静态常量：最小事件ID，值为0

    /** 通道打开事件 */
    public static final int EVENT_CHANNEL_OPEN = EVENT_MIN;                                // 静态常量：通道打开事件，值为0

    /** 通道关闭事件 */
    public static final int EVENT_CHANNEL_CLOSE = EVENT_CHANNEL_OPEN + 1;                  // 静态常量：通道关闭事件，值为1

    /** 通道激活事件 */
    public static final int EVENT_CHANNEL_ACTIVE = EVENT_CHANNEL_CLOSE + 1;                // 静态常量：通道激活事件，值为2

    /** 显示缩放事件 */
    public static final int EVENT_DISPLAY_ZOOM = EVENT_CHANNEL_ACTIVE + 1;                 // 静态常量：显示缩放事件，值为3

    /** 显示模式事件 */
    public static final int EVENT_DISPLAY_MODE = EVENT_DISPLAY_ZOOM + 1;                   // 静态常量：显示模式事件，值为4

    /** FPGA加载成功事件 */
    public static final int EVENT_FPGA_LOAD_OK = EVENT_DISPLAY_MODE + 1;                   // 静态常量：FPGA加载成功事件，值为5

    /** FPGA加载错误事件 */
    public static final int EVENT_FPGA_LOAD_ERR = EVENT_FPGA_LOAD_OK + 1;                  // 静态常量：FPGA加载错误事件，值为6

    /** 通道档位事件 */
    public static final int EVENT_CHANNEL_VSCALE = EVENT_FPGA_LOAD_ERR + 1;                // 静态常量：通道档位事件，值为7

    /** 通道位置事件 */
    public static final int EVENT_CHANNEL_POS = EVENT_CHANNEL_VSCALE + 1;                  // 静态常量：通道位置事件，值为8

    /** 通道偏移事件 */
    public static final int EVENT_CHANNEL_OFFSET = EVENT_CHANNEL_POS + 1;                  // 静态常量：通道偏移事件，值为9

    /** 存储深度事件 */
    public static final int EVENT_MEM_DEPTH = EVENT_CHANNEL_OFFSET + 1;                    // 静态常量：存储深度事件，值为10

    /** 时基位置事件 */
    public static final int EVENT_TIME_POS = EVENT_MEM_DEPTH + 1;                          // 静态常量：时基位置事件，值为11

    /** 时基档位事件 */
    public static final int EVENT_TIME_SCALE = EVENT_TIME_POS + 1;                         // 静态常量：时基档位事件，值为12

    /** 触发类型事件 */
    public static final int EVENT_TRIGGER_TYPE = EVENT_TIME_SCALE + 1;                     // 静态常量：触发类型事件，值为13

    /** 通道波形更新事件 */
    public static final int EVENT_CH_WAVE_UPDATE = EVENT_TRIGGER_TYPE + 1;                 // 静态常量：通道波形更新事件，值为14

    /** 参考波形更新事件 */
    public static final int EVENT_REF_WAVE_UPDATE = EVENT_CH_WAVE_UPDATE + 1;              // 静态常量：参考波形更新事件，值为15

    /** 数学波形更新事件 */
    public static final int EVENT_MATH_WAVE_UPDATE = EVENT_REF_WAVE_UPDATE + 1;            // 静态常量：数学波形更新事件，值为16

    /** 通道测量更新事件 */
    public static final int EVENT_CH_MEASURE_UPDATE = EVENT_MATH_WAVE_UPDATE + 1;          // 静态常量：通道测量更新事件，值为17

    /** 数学测量更新事件 */
    public static final int EVENT_MATH_MEASURE_UPDATE = EVENT_CH_MEASURE_UPDATE + 1;       // 静态常量：数学测量更新事件，值为18

    /** 参考测量更新事件 */
    public static final int EVENT_REF_MEASURE_UPDATE = EVENT_MATH_MEASURE_UPDATE + 1;      // 静态常量：参考测量更新事件，值为19

    /** 串行更新事件 */
    public static final int EVENT_SERIAL_UPDATE = EVENT_REF_MEASURE_UPDATE + 1;            // 静态常量：串行更新事件，值为20

    /** 总线类型更新事件 */
    public static final int EVENT_BUS_TYPE_UPDATE = EVENT_SERIAL_UPDATE + 1;               // 静态常量：总线类型更新事件，值为21

    /** 校准项完成事件 */
    public static final int EVENT_CALIBRATE_ITEM_FINISH = EVENT_BUS_TYPE_UPDATE + 1;       // 静态常量：校准项完成事件，值为22

    /** 自校准开始事件（屏幕键盘全部锁定） */
    public static final int EVENT_SELF_CALIBRATE_BEGIN = EVENT_CALIBRATE_ITEM_FINISH + 1;  // 静态常量：自校准开始事件，值为23

    /** 自校准结束事件 */
    public static final int EVENT_SELF_CALIBRATE_END = EVENT_SELF_CALIBRATE_BEGIN + 1;     // 静态常量：自校准结束事件，值为24

    /** 自动打开事件 */
    public static final int EVENT_AUTO_START = EVENT_SELF_CALIBRATE_END + 1;               // 静态常量：自动打开事件，值为25

    /** 自动停止事件 */
    public static final int EVENT_AUTO_STOP = EVENT_AUTO_START + 1;                        // 静态常量：自动停止事件，值为26

    /** 触发电平位置变化事件 */
    public static final int EVENT_TRIGGER_LEVEL = EVENT_AUTO_STOP + 1;                     // 静态常量：触发电平位置变化事件，值为27

    /** 触发电平参数变化事件 */
    public static final int EVENT_TRIGGER_PARAM = EVENT_TRIGGER_LEVEL + 1;                 // 静态常量：触发电平参数变化事件，值为28

    /** 阈值电平参数变化事件 */
    public static final int EVENT_BUS_PARAM = EVENT_TRIGGER_PARAM + 1;                     // 静态常量：阈值电平参数变化事件，值为29

    /** 阈值电平位置变化事件 */
    public static final int EVENT_BUS_LEVEL = EVENT_BUS_PARAM + 1;                         // 静态常量：阈值电平位置变化事件，值为30

    /** 频率计数器事件 */
    public static final int EVENT_FREQ_COUNTER = EVENT_BUS_LEVEL + 1;                      // 静态常量：频率计数器事件，值为31

    /** 示波器状态事件（运行/停止） */
    public static final int EVENT_SCOPE_STATE = EVENT_FREQ_COUNTER + 1;                    // 静态常量：示波器状态事件，值为32

    /** 示波器单次事件 */
    public static final int EVENT_SCOPE_SINGLE = EVENT_SCOPE_STATE + 1;                    // 静态常量：示波器单次事件，值为33

    /** 采样有效事件 */
    public static final int EVENT_SAMPLE_VALID = EVENT_SCOPE_SINGLE + 1;                   // 静态常量：采样有效事件，值为34

    /** 同步头变化事件 */
    public static final int EVENT_SYNCHEADER_CHANGE = EVENT_SAMPLE_VALID + 1;              // 静态常量：同步头变化事件，值为35

    /** 时基档位列表事件 */
    public static final int EVENT_TIME_SCALE_LIST = EVENT_SYNCHEADER_CHANGE + 1;           // 静态常量：时基档位列表事件，值为36

    /** 强制存储深度事件 */
    public static final int EVENT_FORCE_MEM_DEPTH = EVENT_TIME_SCALE_LIST + 1;             // 静态常量：强制存储深度事件，值为37

    /** 工厂校准开始事件（屏幕键盘全部锁定） */
    public static final int EVENT_FACTOR_CALIBRATE_BEGIN = EVENT_FORCE_MEM_DEPTH + 1;      // 静态常量：工厂校准开始事件，值为38

    /** 工厂校准项开始事件 */
    public static final int EVENT_FACTOR_CALIBRATE_ITEM_START = EVENT_FACTOR_CALIBRATE_BEGIN + 1; // 静态常量：工厂校准项开始事件，值为39

    /** 工厂校准结束事件 */
    public static final int EVENT_FACTOR_CALIBRATE_END = EVENT_FACTOR_CALIBRATE_ITEM_START + 1; // 静态常量：工厂校准结束事件，值为40

    /** 总线通道变化事件 */
    public static final int EVENT_BUS_CH_CHANGE = EVENT_FACTOR_CALIBRATE_END + 1;          // 静态常量：总线通道变化事件，值为41

    /** UI深度采样频率刷新事件 */
    public static final int EVENT_UI_DEPTH_SAMPFRE_REFLASH = EVENT_BUS_CH_CHANGE + 1;      // 静态常量：UI深度采样频率刷新事件，值为42

    /** UI采样图形事件 */
    public static final int EVENT_UI_SAMPLE_GRAPH = EVENT_UI_DEPTH_SAMPFRE_REFLASH + 1;    // 静态常量：UI采样图形事件，值为43

    /** Surface创建事件 */
    public static final int EVENT_SURFACE_CREATED = EVENT_UI_SAMPLE_GRAPH + 1;             // 静态常量：Surface创建事件，值为44

    /** Surface销毁事件 */
    public static final int EVENT_SURFACE_DESTROYED = EVENT_SURFACE_CREATED + 1;           // 静态常量：Surface销毁事件，值为45

    /** 显示波形亮度事件 */
    public static final int EVENT_DIAPLAY_WAVE_BRIGHTNESS = EVENT_SURFACE_DESTROYED + 1;   // 静态常量：显示波形亮度事件，值为46

    /** 清除余辉事件 */
    public static final int EVENT_AFTERGLOW_CLEAR = EVENT_DIAPLAY_WAVE_BRIGHTNESS + 1;     // 静态常量：清除余辉事件，值为47

    /** 余辉时间事件 */
    public static final int EVENT_AFTERGLOW_TIME = EVENT_AFTERGLOW_CLEAR + 1;              // 静态常量：余辉时间事件，值为48

    /** 余辉使能事件 */
    public static final int EVENT_AFTERGLOW_ENABLE = EVENT_AFTERGLOW_TIME + 1;             // 静态常量：余辉使能事件，值为49

    /** 波形清除事件 */
    public static final int EVENT_WAVE_CLEAR = EVENT_AFTERGLOW_ENABLE + 1;                 // 静态常量：波形清除事件，值为50

    /** 数学通道垂直位置事件 */
    public static final int EVENT_MATH_VPOS = EVENT_WAVE_CLEAR + 1;                        // 静态常量：数学通道垂直位置事件，值为51

    /** 显示缩放进入事件 */
    public static final int EVENT_DISPLAY_ZOOM_ENTER = EVENT_MATH_VPOS + 1;                // 静态常量：显示缩放进入事件，值为52

    /** 显示色温事件 */
    public static final int EVENT_DISPLAY_CCT = EVENT_DISPLAY_ZOOM_ENTER + 1;              // 静态常量：显示色温事件，值为53

    /** 显示背景事件 */
    public static final int EVENT_DISPLAY_BACKGROUND = EVENT_DISPLAY_CCT + 1;              // 静态常量：显示背景事件，值为54

    /** 保存二进制运行事件 */
    public static final int EVENT_SAVEBIN_RUN = EVENT_DISPLAY_BACKGROUND + 1;              // 静态常量：保存二进制运行事件，值为55

    /** 串行文本更新事件 */
    public static final int EVENT_SERIAL_TXT_UPDATE = EVENT_SAVEBIN_RUN + 1;               // 静态常量：串行文本更新事件，值为56

    /** 深度采样频率变化事件 */
    public static final int EVENT_DEPTH_SAMPFRE_CHANGE = EVENT_SERIAL_TXT_UPDATE + 1;      // 静态常量：深度采样频率变化事件，值为57

    /** 数学刷新事件 */
    public static final int EVENT_MATH_REFRESH = EVENT_DEPTH_SAMPFRE_CHANGE + 1;           // 静态常量：数学刷新事件，值为58

    /** 绘制类型事件 */
    public static final int EVENT_DRAW_TYPE = EVENT_MATH_REFRESH + 1;                      // 静态常量：绘制类型事件，值为59

    /** 水平参考事件 */
    public static final int EVENT_HORREF = EVENT_DRAW_TYPE + 1;                            // 静态常量：水平参考事件，值为60

    /** 通道耦合事件 */
    public static final int EVENT_CHANNEL_COUPLE = EVENT_HORREF + 1;                       // 静态常量：通道耦合事件，值为61

    /** 通道带宽事件 */
    public static final int EVENT_CHANNEL_BANDWIDTH = EVENT_CHANNEL_COUPLE + 1;            // 静态常量：通道带宽事件，值为62

    /** 通道反相事件 */
    public static final int EVENT_CHANNEL_INVERT = EVENT_CHANNEL_BANDWIDTH + 1;            // 静态常量：通道反相事件，值为63

    /** 采样类型事件 */
    public static final int EVENT_SAMPLE_TYPE = EVENT_CHANNEL_INVERT + 1;                  // 静态常量：采样类型事件，值为64

    /** 触发普通模式事件 */
    public static final int EVENT_TRIGGER_COMMON_MODE = EVENT_SAMPLE_TYPE + 1;             // 静态常量：触发普通模式事件，值为65

    /** 触发普通释抑时间事件 */
    public static final int EVENT_TRIGGER_COMMON_HOLDOFFTIME = EVENT_TRIGGER_COMMON_MODE + 1; // 静态常量：触发普通释抑时间事件，值为66

    /** 同步头错误事件 */
    public static final int EVENT_SYNCHEADER_ERROR = EVENT_TRIGGER_COMMON_HOLDOFFTIME + 1; // 静态常量：同步头错误事件，值为67

    /** 数学FFT档位事件（数学通道中，FFT是当前选择但不是当前通道的情况下，档位发生变化时发送） */
    public static final int EVENT_MATH_FFT_SCALE = EVENT_SYNCHEADER_ERROR + 1;             // 静态常量：数学FFT档位事件，值为68

    /** 参考垂直位置事件 */
    public static final int EVENT_REF_VPOS = EVENT_MATH_FFT_SCALE + 1;                     // 静态常量：参考垂直位置事件，值为69

    /** 垂直模式事件 */
    public static final int EVENT_VERTICAL_MODE = EVENT_REF_VPOS + 1;                      // 静态常量：垂直模式事件，值为70

    /** 触发电平用户事件 */
    public static final int EVENT_TRIGGER_LEVEL_USER = EVENT_VERTICAL_MODE + 1;            // 静态常量：触发电平用户事件，值为71

    /** 通道档位用户事件 */
    public static final int EVENT_CHANNEL_VSCALE_USER = EVENT_TRIGGER_LEVEL_USER + 1;      // 静态常量：通道档位用户事件，值为72

    /** 通道获取波形事件 */
    public static final int EVENT_CHANNEL_GET_WAVE = EVENT_CHANNEL_VSCALE_USER + 1;        // 静态常量：通道获取波形事件，值为73

    /** 分段时间戳事件 */
    public static final int EVENT_SEGMENT_TIMESTAMP = EVENT_CHANNEL_GET_WAVE + 1;          // 静态常量：分段时间戳事件，值为74

    /** 通道电阻类型事件 */
    public static final int EVENT_CHANNEL_RESISTANCETYPE = EVENT_SEGMENT_TIMESTAMP + 1;    // 静态常量：通道电阻类型事件，值为75

    /** 分段帧数事件 */
    public static final int EVENT_SEGMENT_FRAMES = EVENT_CHANNEL_RESISTANCETYPE + 1;       // 静态常量：分段帧数事件，值为76

    /** FPGA状态事件 */
    public static final int EVENT_FPGA_STATUS = EVENT_SEGMENT_FRAMES + 1;                  // 静态常量：FPGA状态事件，值为77

    /** 探头事件 */
    public static final int EVENT_PROBE_EVENT = EVENT_FPGA_STATUS + 1;                     // 静态常量：探头事件，值为78

    /** 探头零点校准事件 */
    public static final int EVENT_PROBE_ZERO = EVENT_PROBE_EVENT + 1;                      // 静态常量：探头零点校准事件，值为79

    /** 探头升级事件 */
    public static final int EVENT_PROBE_UPGRADE = EVENT_PROBE_ZERO + 1;                    // 静态常量：探头升级事件，值为80

    /** 探头告警事件 */
    public static final int EVENT_PROBE_ALARM = EVENT_PROBE_UPGRADE + 1;                   // 静态常量：探头告警事件，值为81

    /** 探头调整事件 */
    public static final int EVENT_PROBE_ADJUST = EVENT_PROBE_ALARM + 1;                    // 静态常量：探头调整事件，值为82

    /** 探头插入事件 */
    public static final int EVENT_PROBE_PLUG = EVENT_PROBE_ADJUST + 1;                     // 静态常量：探头插入事件，值为83

    /** 探头拔出事件 */
    public static final int EVENT_PROBE_UNPLUG = EVENT_PROBE_PLUG + 1;                     // 静态常量：探头拔出事件，值为84

    /** 通道零点校准事件 */
    public static final int EVENT_CHANNEL_ZERO = EVENT_PROBE_UNPLUG + 1;                   // 静态常量：通道零点校准事件，值为85

    /** 探头校准开始事件 */
    public static final int EVENT_PROBE_CALIBRATE_BEGIN = EVENT_CHANNEL_ZERO + 1;          // 静态常量：探头校准开始事件，值为86

    /** 探头校准结束事件 */
    public static final int EVENT_PROBE_CALIBRATE_END = EVENT_PROBE_CALIBRATE_BEGIN + 1;   // 静态常量：探头校准结束事件，值为87

    /** 通道延迟事件 */
    public static final int EVENT_CHANNEL_DELAY = EVENT_PROBE_CALIBRATE_END + 1;           // 静态常量：通道延迟事件，值为88

    /** 清除数学余辉事件 */
    public static final int EVENT_AFTERGLOW_MATH_CLEAR = EVENT_CHANNEL_DELAY + 1;          // 静态常量：清除数学余辉事件，值为89

    /** 数学业辉事件 */
    public static final int EVENT_AFTERGLOW_MATH = EVENT_AFTERGLOW_MATH_CLEAR + 1;         // 静态常量：数学业辉事件，值为90

    /** 测量范围事件 */
    public static final int EVENT_MEASURE_RANGE = EVENT_AFTERGLOW_MATH + 1;                // 静态常量：测量范围事件，值为91

    /** 数学源事件 */
    public static final int EVENT_MATH_SOURCE = EVENT_MEASURE_RANGE + 1;                   // 静态常量：数学源事件，值为92

    /** 波形偏移事件 */
    public static final int EVENT_WAVE_OFFSET = EVENT_MATH_SOURCE + 1;                     // 静态常量：波形偏移事件，值为93

    /** 保存CSV运行事件 */
    public static final int EVENT_SAVECSV_RUN = EVENT_WAVE_OFFSET + 1;                     // 静态常量：保存CSV运行事件，值为94

    /** 波形标签选择事件 */
    public static final int EVENT_WAVE_LABEL_SELECT = EVENT_SAVECSV_RUN + 1;               // 静态常量：波形标签选择事件，值为95

    /** 波形标签移动事件 */
    public static final int EVENT_WAVE_LABEL_MOVE = EVENT_WAVE_LABEL_SELECT + 1;           // 静态常量：波形标签移动事件，值为96

    /** 加载CSV运行事件 */
    public static final int EVENT_LOADCSV_RUN = EVENT_WAVE_LABEL_MOVE + 1;                 // 静态常量：加载CSV运行事件，值为97

    /** 改变通道时基事件 */
    public static final int EVENT_CHANGE_CH_TIMEBASE = EVENT_LOADCSV_RUN + 1;              // 静态常量：改变通道时基事件，值为98

    /** 设置通道时基位置事件 */
    public static final int EVENT_SET_CH_TIMEPOS = EVENT_CHANGE_CH_TIMEBASE + 1;           // 静态常量：设置通道时基位置事件，值为99

    /** 最大事件ID */
    public static final int EVENT_MAX = EVENT_SET_CH_TIMEPOS + 1;                          // 静态常量：最大事件ID，值为100

    /** 事件总数 */
    public static final int EVENT_CNT = EVENT_MAX - EVENT_MIN + 1;                         // 静态常量：事件总数，值为101

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** Observable数组，每个事件类型对应一个Observable */
    private Observable[] observables = new EventObservable[EVENT_CNT];                      // 成员变量：Observable数组，大小为事件总数

    /** 异步事件标识（高位标记） */
    private static final int EVENT_ASYNC = (0x55AA << 16);                                  // 静态常量：异步事件标识，值为0x55AA左移16位

    /** 事件Handler，用于处理异步事件 */
    private Handler eventHandler;                                                           // 成员变量：事件Handler

    /** 事件HandlerThread，用于创建事件处理线程 */
    private HandlerThread eventHandlerThread;                                               // 成员变量：事件HandlerThread

    /** 事件启用标识 */
    boolean bEnable = true;                                                                 // 成员变量：事件启用标识，初始值为true

    /** 缓存事件列表（事件禁用时使用） */
    private List<EventBase> list = new ArrayList<>();                                       // 成员变量：缓存事件列表

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数
     * 初始化事件处理线程和Observable数组
     */
    private EventFactory() {                                                                // 私有构造方法：初始化事件工厂
        eventHandlerThread = new HandlerThread("EventFactory");                             // 创建事件处理线程，名称为"EventFactory"
        eventHandlerThread.start();                                                         // 启动事件处理线程
        eventHandler = new Handler(eventHandlerThread.getLooper()) {                        // 创建事件Handler，使用事件处理线程的Looper
            @Override
            public void handleMessage(Message msg) {                                        // 重写方法：处理消息
                if ((msg.what & EVENT_ASYNC) != 0) {                                        // 如果是异步事件（检查高位标记）
                    sendEvent((EventBase) msg.obj);                                         // 发送事件
                }                                                                           // if语句结束
            }                                                                               // 方法结束
        };                                                                                  // Handler初始化结束

        for (int i = 0; i < EVENT_CNT; i++) {                                               // 循环：遍历所有事件类型
            observables[i] = new EventObservable();                                         // 为每个事件类型创建Observable
        }                                                                                   // 循环结束
    }                                                                                       // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // Observable获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取指定事件类型的Observable
     *
     * @param event 事件类型ID
     * @return Observable对象，如果事件ID无效则返回null
     */
    private Observable getObservable(int event) {                                           // 私有方法：获取指定事件类型的Observable
        if (isValidEvent(event)) {                                                          // 如果事件ID有效
            return observables[event];                                                      // 返回对应的Observable
        }                                                                                   // if语句结束
        return null;                                                                        // 否则返回null
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件启用/禁用方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置事件启用状态
     * 禁用时，部分关键事件会被缓存；启用时，发送所有缓存的事件
     *
     * @param bEnable true=启用事件分发，false=禁用事件分发
     */
    public void setEnable(boolean bEnable) {                                                // 公有方法：设置事件启用状态
        synchronized (this) {                                                               // 同步块：保护bEnable和list的访问
            this.bEnable = bEnable;                                                         // 更新事件启用标识
        }                                                                                   // 同步块结束
        if (bEnable) {                                                                      // 如果启用事件
            for (EventBase e : list) {                                                      // 循环：遍历缓存事件列表
                this.notifyObservers(e, e.isAsync());                                       // 发送缓存的事件
            }                                                                               // 循环结束
            list.clear();                                                                   // 清空缓存事件列表
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    /**
     * 判断事件是否启用
     *
     * @return true=启用，false=禁用
     */
    public boolean isEnable() {                                                             // 公有方法：判断事件是否启用
        return bEnable;                                                                     // 返回事件启用标识
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件通知方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 通知观察者（同步，无延迟）
     *
     * @param eventBase 事件对象
     * @param async     是否异步
     */
    public void notifyObservers(EventBase eventBase, boolean async) {                       // 公有方法：通知观察者，无延迟
        notifyObservers(eventBase, async, 0);                                               // 调用重载方法，延迟为0
    }                                                                                       // 方法结束

    /**
     * 通知观察者
     * 根据事件启用状态和异步标识分发事件
     *
     * @param eventBase 事件对象
     * @param async     是否异步
     * @param ms        延迟时间（毫秒），0表示不延迟
     */
    public void notifyObservers(EventBase eventBase, boolean async, long ms) {              // 公有方法：通知观察者，带延迟
        synchronized (this) {                                                               // 同步块：保护bEnable和list的访问
            if (!bEnable) {                                                                 // 如果事件被禁用
                switch (eventBase.getId()) {                                                // 根据事件ID进行分支处理
                    case EVENT_CHANNEL_OPEN:                                                // 通道打开事件
                    case EVENT_WAVE_CLEAR:                                                  // 波形清除事件
                    case EVENT_SCOPE_STATE:                                                 // 示波器状态事件
                    case EVENT_DISPLAY_CCT:                                                 // 显示色温事件
                    case EVENT_AFTERGLOW_ENABLE:                                            // 余辉使能事件
                    case EVENT_DISPLAY_MODE:                                                // 显示模式事件
                    case EVENT_AFTERGLOW_CLEAR:                                             // 清除余辉事件
                    case EVENT_BUS_CH_CHANGE:                                               // 总线通道变化事件
                    case EVENT_DISPLAY_BACKGROUND:                                          // 显示背景事件
                        eventBase.setAsync(async);                                          // 设置异步标识
                        list.add(eventBase);                                                // 将事件添加到缓存列表
                        break;                                                              // 跳出switch语句
                }                                                                           // switch语句结束
                return;                                                                     // 返回，不发送事件
            }                                                                               // if语句结束
        }                                                                                   // 同步块结束

        int id = eventBase.getId();                                                         // 获取事件ID
        if (isValidEvent(id)) {                                                             // 如果事件ID有效
            if (async) {                                                                    // 如果是异步事件
                Message msg = Message.obtain();                                             // 获取消息对象
                msg.arg1 = msg.arg2 = eventBase.getId();                                    // 设置消息参数为事件ID
                msg.what = EVENT_ASYNC | (msg.arg1 & 0xFFFF);                               // 设置消息what，包含异步标识和事件ID
                msg.obj = eventBase;                                                        // 设置消息对象为事件对象
                if (eventHandler.hasMessages(msg.what)) {                                   // 如果Handler中已有相同消息
                    eventHandler.removeMessages(msg.what);                                  // 移除旧消息（合并相同事件）
                }                                                                           // if语句结束
                if (ms > 0) {                                                               // 如果有延迟时间
                    eventHandler.sendMessageDelayed(msg, ms);                               // 延迟发送消息
                } else {                                                                    // 否则（无延迟）
                    eventHandler.sendMessage(msg);                                          // 立即发送消息
                }                                                                           // if-else语句结束
            } else {                                                                        // 否则（同步事件）
                observables[id].notifyObservers(eventBase);                                 // 直接通知观察者
            }                                                                               // if-else语句结束
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    /**
     * 通知观察者（同步，无延迟）
     *
     * @param eventBase 事件对象
     */
    public void notifyObservers(EventBase eventBase) {                                      // 公有方法：通知观察者，同步无延迟
        notifyObservers(eventBase, false);                                                  // 调用重载方法，异步为false
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 静态事件发送方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 发送事件（同步）
     *
     * @param eventBase 事件对象
     */
    public static void sendEvent(EventBase eventBase) {                                     // 静态方法：发送事件，同步
        sendEvent(eventBase, false);                                                        // 调用重载方法，异步为false
    }                                                                                       // 方法结束

    /**
     * 发送事件（同步）
     *
     * @param event 事件ID
     */
    public static void sendEvent(int event) {                                               // 静态方法：发送事件，参数为事件ID
        sendEvent(new EventBase(event), false);                                             // 创建事件对象并发送，同步
    }                                                                                       // 方法结束

    /**
     * 发送事件
     *
     * @param event 事件ID
     * @param async 是否异步
     */
    public static void sendEvent(int event, boolean async) {                                // 静态方法：发送事件，参数为事件ID和异步标识
        sendEvent(new EventBase(event), async);                                             // 创建事件对象并发送
    }                                                                                       // 方法结束

    /**
     * 发送事件
     *
     * @param eventBase 事件对象
     * @param async     是否异步
     */
    public static void sendEvent(EventBase eventBase, boolean async) {                      // 静态方法：发送事件，参数为事件对象和异步标识
        getInstance().notifyObservers(eventBase, async);                                    // 调用实例方法通知观察者
    }                                                                                       // 方法结束

    /**
     * 发送事件（带延迟）
     *
     * @param eventBase 事件对象
     * @param async     是否异步
     * @param ms        延迟时间（毫秒）
     */
    public static void sendEvent(EventBase eventBase, boolean async, long ms) {             // 静态方法：发送事件，带延迟
        getInstance().notifyObservers(eventBase, async, ms);                                // 调用实例方法通知观察者，带延迟
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 观察者注册和注销方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 注册事件观察者
     *
     * @param event    事件类型ID
     * @param observer 观察者对象
     */
    public static void addEventObserver(int event, Observer observer) {                     // 静态方法：注册事件观察者
        if (isValidEvent(event)) {                                                          // 如果事件ID有效
            getInstance().getObservable(event).addObserver(observer);                       // 添加观察者到对应的Observable
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    /**
     * 注销事件观察者
     *
     * @param event    事件类型ID
     * @param observer 观察者对象
     */
    public static void delEventObserver(int event, Observer observer) {                     // 静态方法：注销事件观察者
        if (isValidEvent(event)) {                                                          // 如果事件ID有效
            getInstance().getObservable(event).deleteObserver(observer);                    // 从对应的Observable删除观察者
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件验证方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断事件ID是否有效
     *
     * @param event 事件ID
     * @return true=有效，false=无效
     */
    public static boolean isValidEvent(int event) {                                         // 静态方法：判断事件ID是否有效
        return (event >= EVENT_MIN && event <= EVENT_MAX);                                  // 返回事件ID是否在有效范围内
    }                                                                                       // 方法结束
}                                                                                           // 类结束
