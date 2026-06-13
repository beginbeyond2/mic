package com.micsig.tbook.scope.Display;                                                // 包声明：显示模块

import com.micsig.base.FilterThread;                                                     // 导入：过滤线程基类
import com.micsig.tbook.scope.Calibrate.HwConfig;                                        // 导入：硬件配置类
import com.micsig.tbook.scope.Data.BaseDirectBuffer;                                     // 导入：直接缓冲区基类
import com.micsig.tbook.scope.Data.IDataBuffer;                                          // 导入：数据缓冲区接口
import com.micsig.tbook.scope.Event.EventBase;                                           // 导入：事件基类
import com.micsig.tbook.scope.Event.EventFactory;                                        // 导入：事件工厂类
import com.micsig.tbook.scope.Scope;                                                     // 导入：示波器主类
import com.micsig.tbook.scope.ScopeBase;                                                 // 导入：示波器基础配置类
import com.micsig.tbook.scope.channel.Channel;                                           // 导入：通道类
import com.micsig.tbook.scope.channel.ChannelFactory;                                    // 导入：通道工厂类
import com.micsig.tbook.scope.surface.SurfaceDataRecv;                                   // 导入：Surface数据接收类
import com.micsig.tbook.scope.surface.SurfaceNative;                                     // 导入：Surface本地方法类
import com.micsig.tbook.scope.surface.SurfacePreview;                                    // 导入：Surface预览类

import java.nio.ByteBuffer;                                                              // 导入：字节缓冲区类
import java.util.Observable;                                                             // 导入：被观察者类
import java.util.Observer;                                                               // 导入：观察者接口

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║            DisplayXYService - 示波器XY模式显示服务类                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Display模块的XY模式显示服务类，位于Display包下，                             ║
 * ║   继承自FilterThread过滤线程类，实现Observer观察者接口，                       ║
 * ║   负责XY模式的波形绘制和显示。                                                ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理XY模式显示服务的单例实例                                             ║
 * ║   2. 绘制XY模式波形（CH1作为X轴，CH2作为Y轴）                                 ║
 * ║   3. 管理XY显示参数（宽度、高度、颜色、亮度等）                               ║
 * ║   4. 监听事件并响应（波形更新、Surface创建/销毁、显示模式变化等）             ║
 * ║   5. 管理SurfaceNative本地绘制对象                                            ║
 * ║   6. 清除XY波形显示                                                          ║
 * ║                                                                              ║
 * ║ 【架构位置】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        Display模块架构                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │     Display     │────────▶│DisplayXYService │                   │ ║
 * ║   │   │   (显示管理类)   │         │  (XY模式服务类)  │                   │ ║
 * ║   │   └─────────────────┘         └────────┬────────┘                   │ ║
 * ║   │          │                             │                            │ ║
 * ║   │          ▼                             ▼                            │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │   显示设置数据   │         │  SurfaceNative  │                   │ ║
 * ║   │   └─────────────────┘         └─────────────────┘                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【XY模式说明】                                                               ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        XY模式原理                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【基本概念】                                                         │ ║
 * ║   │   - XY模式是一种特殊的示波器显示模式                                   │ ║
 * ║   │   - CH1通道信号作为X轴输入                                            │ ║
 * ║   │   - CH2通道信号作为Y轴输入                                            │ ║
 * ║   │   - 屏幕上显示的是两个信号的合成波形（李萨如图形）                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【应用场景】                                                         │ ║
 * ║   │   - 测量相位差：通过李萨如图形分析两信号的相位关系                     │ ║
 * ║   │   - 频率比较：通过图形形状判断两信号频率比                             │ ║
 * ║   │   - 信号分析：分析调制信号、非线性失真等                               │ ║
 * ║   │                                                                      │ ║
 * ║   │   【李萨如图形】                                                       │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  频率比  │  相位差  │        图形形状                        │   │ ║
 * ║   │   ├─────────────────────────────────────────────────────────────┤   │ ║
 * ║   │   │   1:1    │   0°     │    直线（对角线）                     │   │ ║
 * ║   │   │   1:1    │   90°    │    圆                                 │   │ ║
 * ║   │   │   1:1    │   其他   │    椭圆                               │   │ ║
 * ║   │   │   1:2    │   90°    │    抛物线                             │   │ ║
 * ║   │   │   2:1    │   90°    │    8字形                              │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【数据流程】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        XY模式绘制流程                                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   CH1通道数据 ──┐                                                    │ ║
 * ║   │                 ├──▶ DisplayXYService ──▶ SurfaceNative ──▶ 屏幕   │ ║
 * ║   │   CH2通道数据 ──┘                                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【绘制步骤】                                                         │ ║
 * ║   │   1. 从CH1和CH2通道获取波形数据                                       │ ║
 * ║   │   2. 将CH1数据作为X坐标，CH2数据作为Y坐标                             │ ║
 * ║   │   3. 调用SurfaceNative进行本地绘制                                    │ ║
 * ║   │   4. 显示在屏幕上                                                     │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【事件监听】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        监听的事件类型                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【波形相关事件】                                                     │ ║
 * ║   │   - EVENT_CH_WAVE_UPDATE: 通道波形更新事件                           │ ║
 * ║   │     触发时机：通道数据更新时                                          │ ║
 * ║   │     处理逻辑：触发XY波形绘制                                          │ ║
 * ║   │                                                                      │ ║
 * ║   │   【Surface相关事件】                                                  │ ║
 * ║   │   - EVENT_SURFACE_CREATED: Surface创建事件                           │ ║
 * ║   │     触发时机：显示Surface创建时                                       │ ║
 * ║   │     处理逻辑：获取SurfaceNative，初始化绘制环境                       │ ║
 * ║   │   - EVENT_SURFACE_DESTROYED: Surface销毁事件                          │ ║
 * ║   │     触发时机：显示Surface销毁时                                       │ ║
 * ║   │     处理逻辑：释放SurfaceNative资源                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【显示相关事件】                                                     │ ║
 * ║   │   - EVENT_DISPLAY_CCT: 显示色温事件                                   │ ║
 * ║   │     触发时机：色温设置变化时                                          │ ║
 * ║   │     处理逻辑：更新CCT使能状态                                         │ ║
 * ║   │   - EVENT_DIAPLAY_WAVE_BRIGHTNESS: 波形亮度事件                       │ ║
 * ║   │     触发时机：波形亮度变化时                                          │ ║
 * ║   │     处理逻辑：更新亮度值                                              │ ║
 * ║   │   - EVENT_DISPLAY_MODE: 显示模式事件                                  │ ║
 * ║   │     触发时机：显示模式变化时                                          │ ║
 * ║   │     处理逻辑：清除XY波形                                              │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【线程模型】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        线程架构                                       │ ║
 * ║   │                                                                      │ ║
 * ║   │   DisplayXYService继承FilterThread                                   │ ║
 * ║   │   - FilterThread是一个可延迟执行的线程基类                            │ ║
 * ║   │   - 设置延迟时间为50ms                                                │ ║
 * ║   │   - 定期检查是否为XY模式，如果是则绘制XY波形                          │ ║
 * ║   │                                                                      │ ║
 * ║   │   【线程安全】                                                         │ ║
 * ║   │   - 使用synchronized保护共享资源                                      │ ║
 * ║   │   - 使用volatile修饰bWaveVaild保证可见性                              │ ║
 * ║   │   - 单例模式使用双重检查锁定（Double-Check Locking）                  │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【XYParam参数结构】                                                          ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        XY参数内存布局                                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   偏移量  │  参数名称         │  类型    │  说明                      │ ║
 * ║   │   ───────┼──────────────────┼─────────┼─────────────────────────── │ ║
 * ║   │     0    │  perPixelByte    │  int    │  每像素字节数              │ ║
 * ║   │     4    │  x               │  int    │  X偏移                     │ ║
 * ║   │     8    │  y               │  int    │  Y偏移                     │ ║
 * ║   │    12    │  width           │  int    │  宽度                      │ ║
 * ║   │    16    │  height          │  int    │  高度                      │ ║
 * ║   │    20    │  foregroundColor │  int    │  前景色（波形颜色）         │ ║
 * ║   │    24    │  backgroundColor │  int    │  背景色                    │ ║
 * ║   │    28    │  brightness      │  int    │  亮度                      │ ║
 * ║   │    32    │  flags           │  int    │  标志位                    │ ║
 * ║   │    36    │  xDstVScaleVal   │  double │  X方向垂直缩放值           │ ║
 * ║   │    44    │  yDstVScaleVal   │  double │  Y方向垂直缩放值           │ ║
 * ║   │    52    │  cctEnable       │  int    │  色温使能                  │ ║
 * ║   │    56    │  adMaxVal        │  int    │  ADC最大值                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   总大小：128字节                                                      │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 单例模式：使用双重检查锁定确保线程安全的单例实例                        ║
 * ║   - 观察者模式：实现Observer接口，监听事件变化                              ║
 * ║   - 过滤器模式：继承FilterThread，实现延迟执行和过滤功能                    ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - Display: 显示管理类                                                   ║
 * ║   - FilterThread: 过滤线程基类                                            ║
 * ║   - Observer: 观察者接口                                                  ║
 * ║   - SurfaceNative: Surface本地绘制类                                      ║
 * ║   - Channel: 通道类                                                       ║
 * ║   - ChannelFactory: 通道工厂类                                            ║
 * ║   - EventFactory: 事件工厂类                                              ║
 * ║   - Scope: 示波器主类                                                     ║
 * ║   - ScopeBase: 示波器基础配置类                                           ║
 * ║   - HwConfig: 硬件配置类                                                  ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. XY模式显示：用户切换到XY模式时，显示CH1-CH2合成波形                   ║
 * ║   2. 相位测量：通过李萨如图形测量两信号相位差                              ║
 * ║   3. 频率比较：通过李萨如图形比较两信号频率                                ║
 * ║   4. 信号分析：分析调制信号、非线性失真等                                  ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ║ 【创建日期】 2018-5-28                                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器XY模式显示服务类
 * 继承自FilterThread过滤线程类，实现Observer观察者接口
 * 负责XY模式的波形绘制和显示
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>XY模式波形绘制：CH1作为X轴，CH2作为Y轴</li>
 *   <li>XY显示参数管理：宽度、高度、颜色、亮度等</li>
 *   <li>事件监听：波形更新、Surface创建/销毁、显示模式变化等</li>
 *   <li>SurfaceNative管理：获取和释放本地绘制资源</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 获取DisplayXYService单例实例
 * DisplayXYService xyService = DisplayXYService.getInstance();
 *
 * // 设置X偏移
 * xyService.setX(10);
 *
 * // 设置Y偏移
 * xyService.setY(20);
 *
 * // 设置波形颜色
 * xyService.setForegroundColor(0xFF00FFFF);
 *
 * // 设置亮度
 * xyService.setBrightness(80);
 *
 * // 清除XY波形
 * xyService.ClearXY();
 * </pre>
 *
 * @see Display
 * @see FilterThread
 * @see Observer
 * @see SurfaceNative
 * @see XYParam
 */
public class DisplayXYService extends FilterThread implements Observer {                // 类声明：XY模式显示服务类，继承FilterThread，实现Observer

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签，用于日志输出时标识此类 */
    private static final String TAG = "DisplayXYService";                                 // 静态常量：日志标签

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** Surface本地绘制对象，用于调用本地方法绘制XY波形 */
    private SurfaceNative surfaceNative = null;                                           // 成员变量：Surface本地绘制对象，初始值为null

    /** XY参数对象，包含绘制XY波形所需的各种参数 */
    private XYParam xyParam;                                                              // 成员变量：XY参数对象

    /** 单例实例，使用volatile保证多线程可见性 */
    private static volatile DisplayXYService instance = null;                             // 静态变量：单例实例，volatile保证可见性

    /** 波形有效标志，使用volatile保证多线程可见性 */
    private volatile boolean bWaveVaild = false;                                          // 成员变量：波形有效标志，volatile保证可见性，初始值为false

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例模式实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取DisplayXYService单例实例
     * 使用双重检查锁定（Double-Check Locking）确保线程安全
     *
     * @return DisplayXYService单例实例
     */
    public static DisplayXYService getInstance() {                                        // 静态公有方法：获取单例实例
        if (instance == null) {                                                           // 第一次检查：如果实例为null
            synchronized (DisplayXYService.class) {                                       // 同步块：锁定类对象
                if (instance == null) {                                                   // 第二次检查：如果实例仍为null
                    instance = new DisplayXYService();                                    // 创建单例实例
                }                                                                           // if语句结束
            }                                                                               // 同步块结束
        }                                                                                   // if语句结束
        return instance;                                                                   // 返回单例实例
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 私有构造函数
     * 初始化XY模式显示服务
     *
     * <p><b>初始化步骤：</b></p>
     * <ol>
     *   <li>调用父类构造函数，设置线程名称</li>
     *   <li>设置延迟时间为50ms</li>
     *   <li>创建XYParam参数对象</li>
     *   <li>初始化参数</li>
     *   <li>设置运行任务（定期检查是否需要绘制XY波形）</li>
     *   <li>注册事件观察者</li>
     * </ol>
     */
    private DisplayXYService() {                                                          // 私有构造方法：初始化XY模式显示服务
        super(TAG);                                                                       // 调用父类构造函数，传入日志标签作为线程名称
        setDelayMillis(50);                                                               // 设置延迟时间为50ms
        xyParam = new XYParam();                                                          // 创建XYParam参数对象
        InitParam();                                                                      // 初始化参数

        // ─────────────────────────────────────────────────────────────────────────
        // 设置运行任务
        // ─────────────────────────────────────────────────────────────────────────
        this.setRunnable(new Runnable() {                                                 // 设置运行任务
            @Override                                                                     // 注解：重写run方法
            public void run() {                                                           // run方法：线程执行体
                // Thread.currentThread().setName("DisplayXYServer");                    // 注释掉的代码：设置线程名称
                if(isXY()) {                                                              // 如果当前是XY模式
                    drawXY();                                                             // 绘制XY波形
                }                                                                           // if语句结束
            }                                                                               // run方法结束
        });                                                                                 // 设置运行任务结束

        // ─────────────────────────────────────────────────────────────────────────
        // 注册事件观察者
        // ─────────────────────────────────────────────────────────────────────────
        EventFactory.addEventObserver(EventFactory.EVENT_CH_WAVE_UPDATE, this);           // 注册：通道波形更新事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_SURFACE_CREATED,this);           // 注册：Surface创建事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_SURFACE_DESTROYED,this);         // 注册：Surface销毁事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_CCT,this);               // 注册：显示色温事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_DIAPLAY_WAVE_BRIGHTNESS,this);   // 注册：波形亮度事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_MODE,this);              // 注册：显示模式事件观察者
    }                                                                                       // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 参数初始化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化XY参数
     * 设置XY显示的默认参数值
     *
     * <p><b>初始化参数：</b></p>
     * <ul>
     *   <li>宽度：从ScopeBase获取XY宽度</li>
     *   <li>高度：从ScopeBase获取XY高度</li>
     *   <li>X/Y方向垂直缩放值：1.0</li>
     *   <li>标志位：1</li>
     *   <li>ADC最大值：从HwConfig获取</li>
     *   <li>X/Y偏移：0</li>
     *   <li>前景色：青色（0xFF00FFFF）</li>
     *   <li>背景色：黑色（0）</li>
     *   <li>亮度：80</li>
     * </ul>
     */
    private void InitParam() {                                                            // 私有方法：初始化XY参数
        xyParam.setWidth(ScopeBase.getXYWidth());                                         // 设置宽度：从ScopeBase获取XY宽度
        xyParam.setHeight(ScopeBase.getXYHeight());                                       // 设置高度：从ScopeBase获取XY高度
        xyParam.setXDstVScaleVal(1.0);                                                    // 设置X方向垂直缩放值：1.0
        xyParam.setYDstVScaleVal(1.0);                                                    // 设置Y方向垂直缩放值：1.0
        xyParam.setFlags(1);                                                              // 设置标志位：1
        xyParam.setADMaxVal(HwConfig.getInstance().getAdMaxVal());                        // 设置ADC最大值：从HwConfig获取
        setX(0);                                                                          // 设置X偏移：0
        setY(0);                                                                          // 设置Y偏移：0
        setForegroundColor(0xFF00FFFF);                                                   // 设置前景色：青色（ARGB格式）
        setBackgroundColor(0);                                                            // 设置背景色：黑色
        setBrightness(80);                                                                // 设置亮度：80
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // XY模式判断方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断当前是否为XY模式
     * 从Display获取当前显示模式
     *
     * @return true=XY模式，false=非XY模式
     */
    private boolean isXY(){                                                               // 私有方法：判断是否为XY模式
        return Display.getInstance().isXYMode();                                          // 返回Display中的XY模式状态
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 参数设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置X偏移
     * 设置XY显示的X轴偏移量
     *
     * @param x X偏移值（像素）
     */
    public void setX(int x) {                                                             // 公有方法：设置X偏移
        xyParam.setX(x);                                                                  // 设置XY参数中的X偏移
        if(isXY()) {                                                                      // 如果当前是XY模式
            run();                                                                        // 触发线程运行，立即绘制
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    /**
     * 设置Y偏移
     * 设置XY显示的Y轴偏移量
     *
     * @param y Y偏移值（像素）
     */
    public void setY(int y) {                                                             // 公有方法：设置Y偏移
        xyParam.setY(y);                                                                  // 设置XY参数中的Y偏移
        if(isXY()) {                                                                      // 如果当前是XY模式
            run();                                                                        // 触发线程运行，立即绘制
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    /**
     * 设置前景色（波形颜色）
     *
     * @param color 前景色（ARGB格式）
     */
    public void setForegroundColor(int color) {                                           // 公有方法：设置前景色
        xyParam.setForegroundColor(color);                                                // 设置XY参数中的前景色
    }                                                                                       // 方法结束

    /**
     * 设置背景色
     *
     * @param color 背景色（ARGB格式）
     */
    public void setBackgroundColor(int color) {                                           // 公有方法：设置背景色
        xyParam.setBackgroundColor(color);                                                // 设置XY参数中的背景色
    }                                                                                       // 方法结束

    /**
     * 设置亮度
     * 设置XY波形的显示亮度
     *
     * @param val 亮度值（0-100）
     */
    public void setBrightness(int val) {                                                  // 公有方法：设置亮度
        xyParam.setBrightness(val);                                                       // 设置XY参数中的亮度
        if(isXY()) {                                                                      // 如果当前是XY模式
            run();                                                                        // 触发线程运行，立即绘制
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    /**
     * 设置色温使能
     * 设置是否启用色温调节
     *
     * @param cctEnable 色温使能（1=启用，0=禁用）
     */
    public void setCCTEnable(int cctEnable){                                              // 公有方法：设置色温使能
        xyParam.setCCTEnable(cctEnable);                                                  // 设置XY参数中的色温使能
        if(isXY()) {                                                                      // 如果当前是XY模式
            run();                                                                        // 触发线程运行，立即绘制
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // XY波形绘制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制XY波形
     * 从CH1和CH2通道获取数据，绘制XY合成波形
     *
     * <p><b>绘制流程：</b></p>
     * <ol>
     *   <li>检查波形有效标志</li>
     *   <li>获取CH1和CH2通道的数据缓冲区</li>
     *   <li>调用本地方法绘制XY波形</li>
     *   <li>回收数据缓冲区</li>
     *   <li>如果波形无效，清除XY显示</li>
     * </ol>
     */
    public void drawXY() {                                                                // 公有方法：绘制XY波形

        boolean bClear = false;                                                           // 局部变量：清除标志，初始值为false
        synchronized (this) {                                                             // 同步块：锁定当前对象
            if (bWaveVaild) {                                                             // 如果波形有效
                Channel ch1 = ChannelFactory.getDynamicChannel(ChannelFactory.CH1);       // 获取CH1通道动态实例
                Channel ch2 = ChannelFactory.getDynamicChannel(ChannelFactory.CH2);       // 获取CH2通道动态实例
                IDataBuffer dataBuffer1 = ch1.obtain();                                   // 从CH1通道获取数据缓冲区
                IDataBuffer dataBuffer2 = ch2.obtain();                                   // 从CH2通道获取数据缓冲区
                if (dataBuffer1 != null && dataBuffer2 != null) {                         // 如果两个缓冲区都不为null
                    drawXY(dataBuffer1.getByteBuffer(), dataBuffer2.getByteBuffer());     // 调用本地方法绘制XY波形
                }                                                                           // if语句结束
                if (dataBuffer1 != null) {                                                // 如果CH1缓冲区不为null
                    ch1.recycle(dataBuffer1);                                             // 回收CH1数据缓冲区
                }                                                                           // if语句结束
                if (dataBuffer2 != null) {                                                // 如果CH2缓冲区不为null
                    ch2.recycle(dataBuffer2);                                             // 回收CH2数据缓冲区
                }                                                                           // if语句结束
            } else {                                                                       // 否则（波形无效）
                bClear = true;                                                            // 设置清除标志为true
            }                                                                               // if-else语句结束
        }                                                                                   // 同步块结束
        if(bClear){                                                                        // 如果需要清除
            ClearXY();                                                                     // 清除XY波形
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    /**
     * 清除XY波形
     * 清除XY显示，重置波形有效标志
     *
     * <p><b>清除流程：</b></p>
     * <ol>
     *   <li>设置波形有效标志为false</li>
     *   <li>设置CH1和CH2通道波形无效</li>
     *   <li>调用SurfaceNative清除显示</li>
     * </ol>
     */
    public void ClearXY(){                                                                // 公有方法：清除XY波形

        synchronized (this){                                                              // 同步块：锁定当前对象
            bWaveVaild = false;                                                           // 设置波形有效标志为false
            Channel ch1 = ChannelFactory.getDynamicChannel(ChannelFactory.CH1);           // 获取CH1通道动态实例
            Channel ch2 = ChannelFactory.getDynamicChannel(ChannelFactory.CH2);           // 获取CH2通道动态实例
            ch1.setWaveValid(false);                                                      // 设置CH1通道波形无效
            ch2.setWaveValid(false);                                                      // 设置CH2通道波形无效
            if (surfaceNative != null) {                                                  // 如果SurfaceNative不为null
                surfaceNative.clearSurface();                                             // 清除Surface显示
            }                                                                               // if语句结束
        }                                                                                   // 同步块结束
    }                                                                                       // 方法结束

    /**
     * 绘制XY波形（使用指定的字节缓冲区）
     * 调用SurfaceNative本地方法绘制XY波形
     *
     * @param xByteBuffer X轴数据缓冲区（CH1数据）
     * @param yByteBuffer Y轴数据缓冲区（CH2数据）
     */
    public void drawXY(ByteBuffer xByteBuffer, ByteBuffer yByteBuffer) {                  // 公有方法：绘制XY波形（指定缓冲区）
        synchronized (this) {                                                             // 同步块：锁定当前对象
            if (surfaceNative != null) {                                                  // 如果SurfaceNative不为null
                surfaceNative.drawXYSurface(xByteBuffer, yByteBuffer, xyParam.getDirectBuffer()); // 调用本地方法绘制XY波形
            }                                                                               // if语句结束
        }                                                                                   // 同步块结束
    }                                                                                       // 方法结束

    /**
     * 清除显示
     * 调用SurfaceNative清除Surface显示
     */
    public void clear(){                                                                  // 公有方法：清除显示
        surfaceNative.clearSurface();                                                     // 调用SurfaceNative清除Surface
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // Observer接口实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 事件更新回调方法
     * 实现Observer接口，处理各种事件
     *
     * <p><b>处理的事件：</b></p>
     * <ul>
     *   <li>EVENT_CH_WAVE_UPDATE: 通道波形更新事件</li>
     *   <li>EVENT_SURFACE_CREATED: Surface创建事件</li>
     *   <li>EVENT_SURFACE_DESTROYED: Surface销毁事件</li>
     *   <li>EVENT_DISPLAY_CCT: 显示色温事件</li>
     *   <li>EVENT_DIAPLAY_WAVE_BRIGHTNESS: 波形亮度事件</li>
     *   <li>EVENT_DISPLAY_MODE: 显示模式事件</li>
     * </ul>
     *
     * @param observable 被观察者对象
     * @param data 事件数据（EventBase对象）
     */
    @Override                                                                             // 注解：重写update方法
    public void update(Observable observable, Object data) {                              // 公有方法：事件更新回调
        EventBase eventBase = (EventBase) data;                                           // 将数据转换为EventBase对象

        // ─────────────────────────────────────────────────────────────────────────
        // 处理通道波形更新事件
        // ─────────────────────────────────────────────────────────────────────────
        if (eventBase.getId() == EventFactory.EVENT_CH_WAVE_UPDATE) {                     // 如果是通道波形更新事件
            if(Display.getInstance().isXYMode()) {                                        // 如果当前是XY模式
                run();                                                                    // 触发线程运行，绘制XY波形
            }                                                                               // if语句结束
            if(Display.getInstance().isXYMode()){                                         // 如果当前是XY模式
                if(Scope.getInstance().isRun()){                                          // 如果示波器处于运行状态
                    bWaveVaild = true;                                                    // 设置波形有效标志为true
                }                                                                           // if语句结束
            }else{                                                                         // 否则（非XY模式）
                bWaveVaild = false;                                                       // 设置波形有效标志为false
            }                                                                               // if-else语句结束

        // ─────────────────────────────────────────────────────────────────────────
        // 处理Surface创建事件
        // ─────────────────────────────────────────────────────────────────────────
        }else if(eventBase.getId() == EventFactory.EVENT_SURFACE_CREATED){                // 如果是Surface创建事件
            bWaveVaild = false;                                                           // 设置波形有效标志为false
            SurfaceDataRecv surface = (SurfaceDataRecv)eventBase.getData();               // 从事件数据中获取SurfaceDataRecv对象
            if(surface != null){                                                          // 如果SurfaceDataRecv不为null
                synchronized (this) {                                                     // 同步块：锁定当前对象
                    surfaceNative = surface.getSurfaceNative(SurfacePreview.LAYER_XY);    // 从Surface获取XY层的SurfaceNative
                    surfaceNative.acquireSurface(ScopeBase.getXYWidth(), ScopeBase.getXYHeight()); // 获取Surface资源
                    surfaceNative.clearSurface();                                         // 清除Surface显示
                }                                                                           // 同步块结束
            }                                                                               // if语句结束

        // ─────────────────────────────────────────────────────────────────────────
        // 处理Surface销毁事件
        // ─────────────────────────────────────────────────────────────────────────
        }else if(eventBase.getId() == EventFactory.EVENT_SURFACE_DESTROYED){              // 如果是Surface销毁事件
            SurfaceDataRecv surface = (SurfaceDataRecv)eventBase.getData();               // 从事件数据中获取SurfaceDataRecv对象
            if(surface != null) {                                                         // 如果SurfaceDataRecv不为null
                synchronized (this) {                                                     // 同步块：锁定当前对象
                    if(surfaceNative !=  null) {                                          // 如果SurfaceNative不为null
                        surfaceNative.releaseSurface();                                   // 释放Surface资源
                        surfaceNative = null;                                             // 设置SurfaceNative为null
                    }                                                                       // if语句结束
                }                                                                           // 同步块结束
            }                                                                               // if语句结束

        // ─────────────────────────────────────────────────────────────────────────
        // 处理显示色温事件
        // ─────────────────────────────────────────────────────────────────────────
        }else if(eventBase.getId() == EventFactory.EVENT_DISPLAY_CCT){                    // 如果是显示色温事件
            setCCTEnable(Display.getInstance().isCCT() ? 1:0);                            // 设置色温使能（根据Display中的CCT状态）

        // ─────────────────────────────────────────────────────────────────────────
        // 处理波形亮度事件
        // ─────────────────────────────────────────────────────────────────────────
        }else if(eventBase.getId() == EventFactory.EVENT_DIAPLAY_WAVE_BRIGHTNESS){        // 如果是波形亮度事件
            setBrightness(Display.getInstance().getBrightness());                         // 设置亮度（从Display获取亮度值）

        // ─────────────────────────────────────────────────────────────────────────
        // 处理显示模式事件
        // ─────────────────────────────────────────────────────────────────────────
        }else if(eventBase.getId() == EventFactory.EVENT_DISPLAY_MODE){                   // 如果是显示模式事件

            ClearXY();                                                                    // 清除XY波形
            bWaveVaild = false;                                                           // 设置波形有效标志为false
        }                                                                                   // if-else if语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 内部类：XYParam - XY参数类
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * XY参数内部类
     * 继承自BaseDirectBuffer，用于管理XY绘制的参数
     *
     * <p><b>参数内存布局：</b></p>
     * <pre>
     * 偏移量  │  参数名称         │  类型    │  说明
     * ───────┼──────────────────┼─────────┼───────────────────────────
     *    0    │  perPixelByte    │  int    │  每像素字节数
     *    4    │  x               │  int    │  X偏移
     *    8    │  y               │  int    │  Y偏移
     *   12    │  width           │  int    │  宽度
     *   16    │  height          │  int    │  高度
     *   20    │  foregroundColor │  int    │  前景色（波形颜色）
     *   24    │  backgroundColor │  int    │  背景色
     *   28    │  brightness      │  int    │  亮度
     *   32    │  flags           │  int    │  标志位
     *   36    │  xDstVScaleVal   │  double │  X方向垂直缩放值
     *   44    │  yDstVScaleVal   │  double │  Y方向垂直缩放值
     *   52    │  cctEnable       │  int    │  色温使能
     *   56    │  adMaxVal        │  int    │  ADC最大值
     * </pre>
     *
     * @see BaseDirectBuffer
     */
    private class XYParam extends BaseDirectBuffer {                                      // 内部类：XY参数类，继承BaseDirectBuffer

        /**
         * 构造函数
         * 创建128字节的直接缓冲区
         */
        public XYParam() {                                                                // 构造方法：初始化XY参数
            super(128);                                                                   // 调用父类构造函数，创建128字节的直接缓冲区
        }                                                                                   // 构造方法结束

        /**
         * 设置每像素字节数
         *
         * @param val 每像素字节数
         */
        public void setPerPixelByte(int val) {                                            // 公有方法：设置每像素字节数
            setVal(0, val);                                                               // 在偏移量0处设置值
        }                                                                                   // 方法结束

        /**
         * 设置X偏移
         *
         * @param x X偏移值（像素）
         */
        public void setX(int x) {                                                         // 公有方法：设置X偏移
            setVal(4, x);                                                                 // 在偏移量4处设置值
        }                                                                                   // 方法结束

        /**
         * 设置Y偏移
         *
         * @param y Y偏移值（像素）
         */
        public void setY(int y) {                                                         // 公有方法：设置Y偏移
            setVal(8, y);                                                                 // 在偏移量8处设置值
        }                                                                                   // 方法结束

        /**
         * 设置宽度
         *
         * @param w 宽度（像素）
         */
        public void setWidth(int w) {                                                     // 公有方法：设置宽度
            setVal(12, w);                                                                // 在偏移量12处设置值
        }                                                                                   // 方法结束

        /**
         * 设置高度
         *
         * @param h 高度（像素）
         */
        public void setHeight(int h) {                                                    // 公有方法：设置高度
            setVal(16, h);                                                                // 在偏移量16处设置值
        }                                                                                   // 方法结束

        /**
         * 设置前景色（波形颜色）
         *
         * @param val 前景色（ARGB格式）
         */
        public void setForegroundColor(int val) {                                         // 公有方法：设置前景色
            setVal(20, val);                                                              // 在偏移量20处设置值
        }                                                                                   // 方法结束

        /**
         * 设置背景色
         *
         * @param val 背景色（ARGB格式）
         */
        public void setBackgroundColor(int val) {                                         // 公有方法：设置背景色
            setVal(24, val);                                                              // 在偏移量24处设置值
        }                                                                                   // 方法结束

        /**
         * 设置亮度
         *
         * @param val 亮度值（0-100）
         */
        public void setBrightness(int val) {                                              // 公有方法：设置亮度
            setVal(28, val);                                                              // 在偏移量28处设置值
        }                                                                                   // 方法结束

        /**
         * 设置标志位
         *
         * @param val 标志位
         */
        public void setFlags(int val) {                                                   // 公有方法：设置标志位
            setVal(32, val);                                                              // 在偏移量32处设置值
        }                                                                                   // 方法结束

        /**
         * 设置X方向垂直缩放值
         *
         * @param val X方向垂直缩放值
         */
        public void setXDstVScaleVal(double val) {                                        // 公有方法：设置X方向垂直缩放值
            setVal(36, val);                                                              // 在偏移量36处设置值（double占8字节）
        }                                                                                   // 方法结束

        /**
         * 设置Y方向垂直缩放值
         *
         * @param val Y方向垂直缩放值
         */
        public void setYDstVScaleVal(double val) {                                        // 公有方法：设置Y方向垂直缩放值
            setVal(44, val);                                                              // 在偏移量44处设置值（double占8字节）
        }                                                                                   // 方法结束

        /**
         * 设置色温使能
         *
         * @param cctEnable 色温使能（1=启用，0=禁用）
         */
        public void setCCTEnable(int cctEnable){                                          // 公有方法：设置色温使能
            setVal(52,cctEnable);                                                         // 在偏移量52处设置值
        }                                                                                   // 方法结束

        /**
         * 设置ADC最大值
         *
         * @param val ADC最大值
         */
        public void setADMaxVal(int val){                                                 // 公有方法：设置ADC最大值
            setVal(56,val);                                                               // 在偏移量56处设置值
        }                                                                                   // 方法结束

    }                                                                                       // 内部类结束

}                                                                                           // 类结束
