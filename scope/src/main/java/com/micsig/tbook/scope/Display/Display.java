package com.micsig.tbook.scope.Display;                                                   // 包声明：显示模块

import android.graphics.Color;                                                            // 导入：颜色类

import com.micsig.tbook.scope.Event.EventFactory;                                         // 导入：事件工厂类

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              Display - 示波器显示管理类                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Display模块的显示管理类，位于Display包下，                                    ║
 * ║   是示波器显示设置的核心管理类。                                               ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理显示模式（YT模式、XY模式）                                           ║
 * ║   2. 管理余辉类型和余辉时间                                                   ║
 * ║   3. 管理缩放模式                                                            ║
 * ║   4. 管理波形绘制类型（点、线）                                               ║
 * ║   5. 管理显示亮度、背景色、色温等                                             ║
 * ║   6. 管理Roll模式                                                            ║
 * ║                                                                              ║
 * ║ 【单例模式】                                                                 ║
 * ║   采用双重检查锁定（Double-Check Locking）实现单例模式                        ║
 * ║   - volatile保证instance变量的可见性                                         ║
 * ║   - synchronized保证线程安全                                                 ║
 * ║                                                                              ║
 * ║ 【显示模式说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        显示模式说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【YT模式（Y-T Mode）】                                               │ ║
 * ║   │   - 横轴：时间                                                        │ ║
 * ║   │   - 纵轴：电压                                                        │ ║
 * ║   │   - 最常用的显示模式                                                  │ ║
 * ║   │   - 显示信号随时间的变化                                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【XY模式（X-Y Mode）】                                               │ ║
 * ║   │   - 横轴：通道1电压                                                   │ ║
 * ║   │   - 纵轴：通道2电压                                                   │ ║
 * ║   │   - 用于李萨如图形、相位比较等                                        │ ║
 * ║   │   - 显示两个信号的相互关系                                            │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【余辉类型说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        余辉类型说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【PERSIST_TYPE_NONE】无余辉                                         │ ║
 * ║   │   - 只显示当前波形                                                   │ ║
 * ║   │   - 不保留历史波形                                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【PERSIST_TYPE_AUTO】自动余辉                                       │ ║
 * ║   │   - 根据时基自动调整余辉时间                                         │ ║
 * ║   │   - 默认模式                                                         │ ║
 * ║   │                                                                      │ ║
 * ║   │   【PERSIST_TYPE_NORMAL】普通余辉                                     │ ║
 * ║   │   - 固定余辉时间                                                     │ ║
 * ║   │   - 用户可调整余辉时间                                               │ ║
 * ║   │                                                                      │ ║
 * ║   │   【PERSIST_TYPE_INFINITE】无限余辉                                   │ ║
 * ║   │   - 永久保留所有波形                                                 │ ║
 * ║   │   - 直到手动清除                                                     │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【绘制类型说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        绘制类型说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【DRAWTYPE_DOTS】点模式                                             │ ║
 * ║   │   - 只绘制采样点                                                     │ ║
 * ║   │   - 适用于高采样率信号                                               │ ║
 * ║   │                                                                      │ ║
 * ║   │   【DRAWTYPE_LINE】线模式                                             │ ║
 * ║   │   - 连接采样点形成连续波形                                           │ ║
 * ║   │   - 默认模式                                                         │ ║
 * ║   │   - 更直观显示波形形状                                               │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【水平参考说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        水平参考说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【HORREF_CENTER】中心参考                                           │ ║
 * ║   │   - 水平参考线在屏幕中心                                             │ ║
 * ║   │   - 默认模式                                                         │ ║
 * ║   │                                                                      │ ║
 * ║   │   【HORREF_TRIGPOS】触发位置参考                                      │ ║
 * ║   │   - 水平参考线在触发位置                                             │ ║
 * ║   │   - 方便观察触发点                                                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【缩放模式说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        缩放模式说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【zoom = false】正常模式                                            │ ║
 * ║   │   - 显示主窗口                                                       │ ║
 * ║   │   - 全屏显示波形                                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【zoom = true】缩放模式                                             │ ║
 * ║   │   - 显示主窗口和放大窗口                                             │ ║
 * ║   │   - 主窗口显示完整波形                                               │ ║
 * ║   │   - 放大窗口显示局部细节                                             │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【Roll模式说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        Roll模式说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【roll = true】滚动模式                                             │ ║
 * ║   │   - 波形从右向左滚动                                                 │ ║
 * ║   │   - 适用于慢速信号                                                   │ ║
 * ║   │   - 实时显示信号变化                                                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   【roll = false】触发模式                                            │ ║
 * ║   │   - 波形稳定显示                                                     │ ║
 * ║   │   - 由触发控制波形显示                                               │ ║
 * ║   │   - 适用于周期信号                                                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 显示模式切换：YT模式和XY模式切换                                       ║
 * ║   2. 余辉设置：设置余辉类型和时间                                          ║
 * ║   3. 缩放操作：放大波形细节                                                ║
 * ║   4. 波形绘制：点模式和线模式切换                                          ║
 * ║   5. 显示调整：亮度、背景色、色温设置                                      ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 单例模式：保证全局只有一个Display实例                                   ║
 * ║   - 动作模式：使用DisplayAction处理显示变化动作                            ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 单例使用双重检查锁定保证线程安全                                        ║
 * ║   - 关键成员变量使用volatile修饰                                           ║
 * ║   - 部分方法使用synchronized保护                                           ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - DisplayAction: 显示动作处理类                                         ║
 * ║   - EventFactory: 事件工厂                                                ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器显示管理类
 * 单例模式，管理示波器的显示设置
 * 包括显示模式、余辉、缩放、绘制类型等
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>显示模式管理：YT模式、XY模式</li>
 *   <li>余辉管理：余辉类型和余辉时间</li>
 *   <li>缩放管理：正常模式和缩放模式</li>
 *   <li>绘制类型管理：点模式和线模式</li>
 *   <li>显示设置：亮度、背景色、色温</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 获取Display实例
 * Display display = Display.getInstance();
 *
 * // 设置显示模式为YT模式
 * display.setDisplayMode(Display.DISPLAY_YT);
 *
 * // 设置余辉类型为自动余辉
 * display.setPersistType(Display.PERSIST_TYPE_AUTO);
 *
 * // 开启缩放模式
 * display.setZoom(true);
 *
 * // 设置波形亮度
 * display.setBrightness(80);
 * </pre>
 *
 * @see DisplayAction
 * @see EventFactory
 */
public class Display {                                                                     // 类声明：显示管理类

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签和单例实例
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签，用于日志输出时标识此类 */
    private static final String TAG = "Display";                                           // 静态常量：日志标签

    /** 单例实例，使用volatile保证多线程可见性 */
    private static volatile Display instance = null;                                       // 静态变量：单例实例，使用volatile修饰

    /**
     * 获取单例实例
     * 使用双重检查锁定（Double-Check Locking）保证线程安全
     *
     * @return Display单例实例
     */
    public static Display getInstance() {                                                  // 静态方法：获取单例实例
        if (instance == null) {                                                            // 第一次检查：实例是否为null
            synchronized (Display.class) {                                                 // 同步块：锁定类对象
                if (instance == null) {                                                    // 第二次检查：实例是否为null
                    instance = new Display();                                              // 创建单例实例
                }                                                                           // if语句结束
            }                                                                               // 同步块结束
        }                                                                                   // if语句结束
        return instance;                                                                   // 返回单例实例
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 显示模式常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** YT模式（Y-T Mode）：横轴为时间，纵轴为电压 */
    public static final int DISPLAY_YT = 0;                                                // 静态常量：YT模式，值为0

    /** XY模式（X-Y Mode）：横轴为通道1电压，纵轴为通道2电压 */
    public static final int DISPLAY_XY = 1;                                                // 静态常量：XY模式，值为1

    // ═══════════════════════════════════════════════════════════════════════════════
    // 水平参考常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 水平参考：中心参考 */
    public static final int HORREF_CENTER = 0;                                             // 静态常量：中心参考，值为0

    /** 水平参考：触发位置参考 */
    public static final int HORREF_TRIGPOS = 1;                                            // 静态常量：触发位置参考，值为1

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绘制类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 绘制类型：点模式 */
    public static final int DRAWTYPE_DOTS = 0;                                             // 静态常量：点模式，值为0

    /** 绘制类型：线模式 */
    public static final int DRAWTYPE_LINE = 1;                                             // 静态常量：线模式，值为1

    // ═══════════════════════════════════════════════════════════════════════════════
    // 余辉类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 余辉类型：无余辉 */
    public static final int PERSIST_TYPE_NONE = 0;                                         // 静态常量：无余辉，值为0

    /** 余辉类型：自动余辉 */
    public static final int PERSIST_TYPE_AUTO = 1;                                         // 静态常量：自动余辉，值为1

    /** 余辉类型：普通余辉 */
    public static final int PERSIST_TYPE_NORMAL = 2;                                       // 静态常量：普通余辉，值为2

    /** 余辉类型：无限余辉 */
    public static final int PERSIST_TYPE_INFINITE = 3;                                     // 静态常量：无限余辉，值为3

    // ═══════════════════════════════════════════════════════════════════════════════
    // 高刷新计数器
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 高刷新计数器 */
    private static int HIGH_REFRESH_COUNTER = 0;                                           // 静态变量：高刷新计数器，初始值为0

    /**
     * 设置高刷新计数器
     *
     * @param counter 计数值
     */
    public static void setHighRefreshCounter(int counter) {                                // 静态方法：设置高刷新计数器
        HIGH_REFRESH_COUNTER = counter;                                                    // 更新高刷新计数器
    }                                                                                       // 方法结束

    /**
     * 获取高刷新计数器
     *
     * @return 高刷新计数器值
     */
    public static int getHighRefreshCounter() {                                            // 静态方法：获取高刷新计数器
        return HIGH_REFRESH_COUNTER;                                                       // 返回高刷新计数器值
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 余辉类型，使用volatile保证多线程可见性 */
    private volatile int persistType = PERSIST_TYPE_AUTO;                                  // 成员变量：余辉类型，初始值为自动余辉

    /** FFT余辉类型，使用volatile保证多线程可见性 */
    private volatile int fftPersistType = PERSIST_TYPE_AUTO;                               // 成员变量：FFT余辉类型，初始值为自动余辉

    /** 水平参考类型 */
    private int horRef = HORREF_CENTER;                                                    // 成员变量：水平参考类型，初始值为中心参考

    /** 显示模式 */
    private int displayMode = DISPLAY_YT;                                                  // 成员变量：显示模式，初始值为YT模式

    /** 绘制类型 */
    private int drawType = DRAWTYPE_LINE;                                                  // 成员变量：绘制类型，初始值为线模式

    /** 显示动作处理对象 */
    private DisplayAction displayAction;                                                   // 成员变量：显示动作处理对象

    /** 缩放模式标识，使用volatile保证多线程可见性 */
    private volatile boolean zoom = false;                                                 // 成员变量：缩放模式标识，初始值为false（正常模式）

    /** Roll模式标识，使用volatile保证多线程可见性 */
    private volatile boolean roll = true;                                                  // 成员变量：Roll模式标识，初始值为true（滚动模式）

    /** 色温标识，使用volatile保证多线程可见性 */
    private volatile boolean cct = false;                                                  // 成员变量：色温标识，初始值为false

    /** 波形亮度，使用volatile保证多线程可见性 */
    private volatile int brightness = 50;                                                  // 成员变量：波形亮度，初始值为50

    /** 波形背景色，使用volatile保证多线程可见性 */
    private volatile int background = Color.TRANSPARENT;                                   // 成员变量：波形背景色，初始值为透明

    /** 余辉调整时间（毫秒），使用volatile保证多线程可见性 */
    private volatile int persistAdjustTime = 200;                                          // 成员变量：余辉调整时间，初始值为200ms

    /** FFT余辉调整时间（毫秒），使用volatile保证多线程可见性 */
    private volatile int fftPersistAdjustTime = 200;                                       // 成员变量：FFT余辉调整时间，初始值为200ms

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数
     * 初始化显示动作处理对象
     */
    public Display() {                                                                     // 构造方法：初始化显示管理类
        displayAction = new DisplayAction(this);                                           // 创建显示动作处理对象
    }                                                                                       // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 余辉清除方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 清除余辉
     * 清除所有通道的余辉波形
     */
    public void clearPersist() {                                                           // 公有方法：清除余辉
        displayAction.clearPersist();                                                      // 调用DisplayAction清除余辉
    }                                                                                       // 方法结束

    /**
     * 清除FFT余辉
     * 清除FFT通道的余辉波形
     */
    public void clearFftPersist() {                                                        // 公有方法：清除FFT余辉
        displayAction.clearFftPersist();                                                   // 调用DisplayAction清除FFT余辉
    }                                                                                       // 方法结束

    /**
     * 清除波形
     * 清除所有波形显示
     */
    public void clearWave() {                                                              // 公有方法：清除波形
        displayAction.clearWave();                                                         // 调用DisplayAction清除波形
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 余辉时间获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取余辉调整时间
     *
     * @return 余辉调整时间（毫秒）
     */
    public int getPersistAdjustTime() {                                                    // 公有方法：获取余辉调整时间
        return persistAdjustTime;                                                          // 返回余辉调整时间
    }                                                                                       // 方法结束

    /**
     * 获取FFT余辉调整时间
     *
     * @return FFT余辉调整时间（毫秒）
     */
    public int getFftPersistAdjustTime() {                                                 // 公有方法：获取FFT余辉调整时间
        return fftPersistAdjustTime;                                                       // 返回FFT余辉调整时间
    }                                                                                       // 方法结束

    /**
     * 获取FFT余辉类型
     *
     * @return FFT余辉类型
     */
    public int getFftPersistType() {                                                       // 公有方法：获取FFT余辉类型
        return fftPersistType;                                                             // 返回FFT余辉类型
    }                                                                                       // 方法结束

    /**
     * 设置余辉调整时间
     *
     * @param persistAdjustTime 余辉调整时间（毫秒）
     */
    public void setPersistAdjustTime(int persistAdjustTime) {                              // 公有方法：设置余辉调整时间
        this.persistAdjustTime = persistAdjustTime;                                        // 更新余辉调整时间
        displayAction.persistAdjustTimeChange();                                           // 调用DisplayAction处理余辉时间变化
    }                                                                                       // 方法结束

    /**
     * 设置FFT余辉调整时间
     *
     * @param persistAdjustTime FFT余辉调整时间（毫秒）
     */
    public void setFftPersistAdjustTime(int persistAdjustTime) {                           // 公有方法：设置FFT余辉调整时间
        synchronized (this) {                                                              // 同步块：保护fftPersistAdjustTime的访问
            this.fftPersistAdjustTime = persistAdjustTime;                                 // 更新FFT余辉调整时间
        }                                                                                   // 同步块结束
        EventFactory.sendEvent(EventFactory.EVENT_AFTERGLOW_MATH, true);                   // 发送数学余辉事件（异步）
    }                                                                                       // 方法结束

    /**
     * 设置FFT余辉类型
     *
     * @param persistType FFT余辉类型
     */
    public void setFftPersistType(int persistType) {                                       // 公有方法：设置FFT余辉类型
        synchronized (this) {                                                              // 同步块：保护fftPersistType的访问
            this.fftPersistType = persistType;                                             // 更新FFT余辉类型
        }                                                                                   // 同步块结束
        EventFactory.sendEvent(EventFactory.EVENT_AFTERGLOW_MATH, true);                   // 发送数学余辉事件（异步）
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 余辉类型获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取余辉类型
     *
     * @return 余辉类型
     */
    public int getPersistType() {                                                          // 公有方法：获取余辉类型
        synchronized (this) {                                                              // 同步块：保护persistType的访问
            return persistType;                                                            // 返回余辉类型
        }                                                                                   // 同步块结束
    }                                                                                       // 方法结束

    /**
     * 设置余辉类型
     *
     * @param persistType 余辉类型
     */
    public void setPersistType(int persistType) {                                          // 公有方法：设置余辉类型
        synchronized (this) {                                                              // 同步块：保护persistType的访问
            this.persistType = persistType;                                                // 更新余辉类型
        }                                                                                   // 同步块结束
        displayAction.persistTypeChange();                                                 // 调用DisplayAction处理余辉类型变化
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 亮度获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取波形亮度
     *
     * @return 波形亮度（0-100）
     */
    public int getBrightness() {                                                           // 公有方法：获取波形亮度
        return brightness;                                                                 // 返回波形亮度
    }                                                                                       // 方法结束

    /**
     * 设置波形亮度
     *
     * @param brightness 波形亮度（0-100）
     */
    public void setBrightness(int brightness) {                                            // 公有方法：设置波形亮度
        this.brightness = brightness;                                                      // 更新波形亮度
        displayAction.brightnessChange();                                                  // 调用DisplayAction处理亮度变化
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绘制类型获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取绘制类型
     *
     * @return 绘制类型
     */
    public int getDrawType() {                                                             // 公有方法：获取绘制类型
        return drawType;                                                                   // 返回绘制类型
    }                                                                                       // 方法结束

    /**
     * 设置绘制类型
     *
     * @param drawType 绘制类型
     */
    public void setDrawType(int drawType) {                                                // 公有方法：设置绘制类型
        this.drawType = drawType;                                                          // 更新绘制类型
        displayAction.drawTypeChange();                                                    // 调用DisplayAction处理绘制类型变化
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 水平参考获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取水平参考类型
     *
     * @return 水平参考类型
     */
    public int getHorRef() {                                                               // 公有方法：获取水平参考类型
        return horRef;                                                                     // 返回水平参考类型
    }                                                                                       // 方法结束

    /**
     * 设置水平参考类型
     *
     * @param horRef 水平参考类型
     */
    public void setHorRef(int horRef) {                                                    // 公有方法：设置水平参考类型
        this.horRef = horRef;                                                              // 更新水平参考类型
        displayAction.horRefChange();                                                      // 调用DisplayAction处理水平参考变化
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 色温获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置色温
     *
     * @param bEnable true=开启色温，false=关闭色温
     */
    public void setCCT(boolean bEnable) {                                                  // 公有方法：设置色温
        synchronized (this) {                                                              // 同步块：保护cct的访问
            this.cct = bEnable;                                                            // 更新色温标识
        }                                                                                   // 同步块结束
        displayAction.cctChange();                                                         // 调用DisplayAction处理色温变化
    }                                                                                       // 方法结束

    /**
     * 判断是否开启色温
     *
     * @return true=开启色温，false=关闭色温
     */
    public boolean isCCT() {                                                               // 公有方法：判断是否开启色温
        synchronized (this) {                                                              // 同步块：保护cct的访问
            return this.cct;                                                               // 返回色温标识
        }                                                                                   // 同步块结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 背景色获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置波形背景色
     *
     * @param background 背景色（Color值）
     */
    public void setWaveBackground(int background) {                                        // 公有方法：设置波形背景色
        this.background = background;                                                      // 更新波形背景色
        displayAction.backgroundChange();                                                  // 调用DisplayAction处理背景色变化
    }                                                                                       // 方法结束

    /**
     * 获取波形背景色
     *
     * @return 背景色（Color值）
     */
    public int getWaveBackground() {                                                       // 公有方法：获取波形背景色
        return this.background;                                                            // 返回波形背景色
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 缩放模式方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断是否为缩放模式
     *
     * @return true=缩放模式，false=正常模式
     */
    public boolean isZoom() {                                                              // 公有方法：判断是否为缩放模式
        return zoom;                                                                       // 返回缩放模式标识
    }                                                                                       // 方法结束

    /**
     * 设置缩放变化标志
     *
     * @param flags 缩放变化标志
     */
    public void setZoomFlags(int flags) {                                                  // 公有方法：设置缩放变化标志
        displayAction.setZoomChangeFlags(flags);                                           // 调用DisplayAction设置缩放变化标志
    }                                                                                       // 方法结束

    /**
     * 设置缩放模式（默认修改配置）
     *
     * @param bEnable true=缩放模式，false=正常模式
     */
    public void setZoom(boolean bEnable) {                                                 // 公有方法：设置缩放模式
        setZoom(bEnable, true);                                                            // 调用重载方法，默认修改配置
    }                                                                                       // 方法结束

    /**
     * 设置缩放模式
     *
     * @param bEnable  true=缩放模式，false=正常模式
     * @param bModify true=修改配置，false=不修改配置
     */
    public void setZoom(boolean bEnable, boolean bModify) {                                // 公有方法：设置缩放模式
        zoom = bEnable;                                                                    // 更新缩放模式标识
        if (bModify) {                                                                     // 如果需要修改配置
            displayAction.zoomChange(false);                                               // 调用DisplayAction处理缩放变化（不强制更新）
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    /**
     * 缩放变化处理
     * 如果当前为缩放模式，则触发缩放变化
     */
    public void zoomChange() {                                                             // 公有方法：缩放变化处理
        if (isZoom()) {                                                                    // 如果当前为缩放模式
            displayAction.zoomChange(true);                                                // 调用DisplayAction处理缩放变化（强制更新）
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 显示模式方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断是否为XY模式
     *
     * @return true=XY模式，false=YT模式
     */
    public synchronized boolean isXYMode() {                                               // 同步方法：判断是否为XY模式
        return displayMode == DISPLAY_XY;                                                  // 返回是否为XY模式
    }                                                                                       // 方法结束

    /**
     * 判断是否为YT模式
     *
     * @return true=YT模式，false=XY模式
     */
    public boolean isYTMode() {                                                            // 公有方法：判断是否为YT模式
        return displayMode == DISPLAY_YT;                                                  // 返回是否为YT模式
    }                                                                                       // 方法结束

    /**
     * 获取显示模式
     *
     * @return 显示模式
     */
    public int getDisplayMode() {                                                          // 公有方法：获取显示模式
        return displayMode;                                                                // 返回显示模式
    }                                                                                       // 方法结束

    /**
     * 设置显示模式
     *
     * @param dispMode 显示模式
     */
    public void setDisplayMode(int dispMode) {                                             // 公有方法：设置显示模式
        synchronized (this) {                                                              // 同步块：保护displayMode的访问
            displayMode = dispMode;                                                        // 更新显示模式
        }                                                                                   // 同步块结束
        displayAction.backgroundChange();                                                  // 调用DisplayAction处理背景变化
        displayAction.displayModeChange();                                                 // 调用DisplayAction处理显示模式变化
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // Roll模式方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断是否为Roll模式
     *
     * @return true=Roll模式，false=触发模式
     */
    public boolean isRoll() {                                                              // 公有方法：判断是否为Roll模式
        return roll;                                                                       // 返回Roll模式标识
    }                                                                                       // 方法结束

    /**
     * 设置Roll模式
     * 只有当模式发生变化时才触发变化处理
     *
     * @param bEnable true=Roll模式，false=触发模式
     */
    public void setRoll(boolean bEnable) {                                                 // 公有方法：设置Roll模式
        if (roll != bEnable) {                                                             // 如果模式发生变化
            roll = bEnable;                                                                // 更新Roll模式标识
            displayAction.rollChange();                                                    // 调用DisplayAction处理Roll模式变化
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束
}                                                                                           // 类结束
