package com.micsig.tbook.scope.horizontal;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Sample.IMemDepth;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.ScopeFrozen;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                HorizontalAxis - 示波器水平轴管理类                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   horizontal模块的水平轴管理类，位于horizontal包下，                           ║
 * ║   提供示波器的时基（时间/格）管理功能。                                         ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理时基档位（从1KS到250pS共39档）                                       ║
 * ║   2. 管理时间位置（触发位置偏移）                                              ║
 * ║   3. 提供时间和像素之间的转换                                                 ║
 * ║   4. 管理主窗口和缩放窗口的时基参数                                           ║
 * ║   5. 响应示波器事件更新时基                                                   ║
 * ║                                                                              ║
 * ║ 【时基档位】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        时基档位列表                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【秒级】1KS, 500S, 200S, 100S, 50S, 20S, 10S, 5S, 2S, 1S           │ ║
 * ║   │                                                                      │ ║
 * ║   │   【毫秒级】500mS, 200mS, 100mS, 50mS, 20mS, 10mS, 5mS, 2mS, 1mS    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【微秒级】500uS, 200uS, 100uS, 50uS, 20uS, 10uS, 5uS, 2uS, 1uS    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【纳秒级】500nS, 200nS, 100nS, 50nS, 20nS, 10nS, 5nS, 2nS, 1nS    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【皮秒级】500pS, 250pS                                             │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【窗口模式】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        窗口模式说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【标准窗口】WPI_STANDARD (0)                                        │ ║
 * ║   │   - 未启用缩放时的标准窗口                                            │ ║
 * ║   │   - 缩放模式下的缩略窗口                                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【放大窗口】WPI_LARGE (1)                                           │ ║
 * ║   │   - 缩放模式下的放大窗口                                              │ ║
 * ║   │   - 显示缩略窗口中选定区域的细节                                      │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【时间单位】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        时间单位说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【内部单位】0.1皮秒（100飞秒）                                       │ ║
 * ║   │   - timePoseMain、timePoseZoom使用此单位                             │ ║
 * ║   │   - 便于高精度时间计算                                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   【显示单位】秒（S）                                                  │ ║
 * ║   │   - getTimeScaleIdVal()返回值使用此单位                              │ ║
 * ║   │   - 便于用户理解和显示                                                │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【数据流】                                                                   ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 设置时基    │───▶│ 更新X轴     │───▶│ 刷新显示    │                   ║
 * ║   │ setTimeScale│    │ initXAxis   │    │ EventFactory│                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 时基调节：用户调节时基档位                                             ║
 * ║   2. 波形缩放：缩放模式下的时基管理                                         ║
 * ║   3. 触发位置：调整触发位置偏移                                             ║
 * ║   4. 时间测量：时间和像素之间的转换                                         ║
 * ║   5. 参考波形：参考波形的时基同步                                           ║
 * ║                                                                              ║
 * ║ 【单例模式】                                                                 ║
 * ║   - 双重检查锁定单例模式                                                    ║
 * ║   - 延迟初始化，线程安全                                                    ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 使用synchronized保护共享数据                                            ║
 * ║   - 使用volatile保证可见性                                                  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - Scope: 示波器主类                                                      ║
 * ║   - ScopeBase: 示波器基础参数                                              ║
 * ║   - ScopeFrozen: 示波器冻结状态                                            ║
 * ║   - Display: 显示管理类                                                    ║
 * ║   - EventFactory: 事件工厂                                                 ║
 * ║   - MemDepthFactory: 存储深度工厂                                          ║
 * ║   - HorizontalAxisAction: 水平轴动作类                                     ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器水平轴管理类
 * 实现Observer接口，监听示波器事件
 * 负责管理示波器的时基档位和时间位置
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>时基管理：管理39个时基档位（1KS ~ 250pS）</li>
 *   <li>时间位置：管理触发位置偏移</li>
 *   <li>窗口模式：支持标准窗口和缩放窗口</li>
 *   <li>时间转换：时间和像素之间的转换</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 获取HorizontalAxis实例
 * HorizontalAxis hAxis = HorizontalAxis.getInstance();
 *
 * // 设置时基档位
 * hAxis.setTimeScaleIdOfView(HorizontalAxis.TSI_1mS);
 *
 * // 获取时基值
 * double timeScale = hAxis.getTimeScaleIdVal();
 *
 * // 设置时间位置
 * hAxis.setTimePosOfView(1000000000000L); // 100us
 * </pre>
 *
 * @see Scope
 * @see ScopeBase
 * @see HorizontalAxisAction
 */
public class HorizontalAxis implements Observer {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签 */
    private static final String TAG = "HorizontalAxis";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例实例
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 单例实例，使用volatile保证可见性 */
    private static volatile HorizontalAxis instance = null;

    /**
     * 获取HorizontalAxis单例实例
     * 使用双重检查锁定模式，线程安全且高效
     *
     * @return HorizontalAxis实例
     */
    public static HorizontalAxis getInstance() {
        if (instance == null) {                                                        // 第一次检查，避免不必要同步
            synchronized (HorizontalAxis.class) {                                      // 同步块
                if (instance == null) {                                                // 第二次检查
                    instance = new HorizontalAxis();                                    // 创建实例
                }
            }
        }
        return instance;                                                               // 返回实例
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 窗口模式常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 标准窗口模式（未启用缩放时的标准窗口，或缩放模式下的缩略窗口） */
    public static final int WPI_STANDARD = 0;

    /** 小窗口模式（缩放窗口中尺寸较小的一个，与标准窗口相同） */
    public static final int WPI_SMALL = WPI_STANDARD;

    /** 大窗口模式（缩放窗口中尺寸较大的一个，即放大窗口） */
    public static final int WPI_LARGE = 1;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位常量定义（秒级）
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 最小时基档位索引 */
    private static final int TSI_MIN = 0;

    /** 1000秒/格 */
    public static final int TSI_1KS = TSI_MIN;

    /** 500秒/格 */
    public static final int TSI_500S = 1;

    /** 200秒/格 */
    public static final int TSI_200S = 2;

    /** 100秒/格 */
    public static final int TSI_100S = 3;

    /** 50秒/格 */
    public static final int TSI_50S = 4;

    /** 20秒/格 */
    public static final int TSI_20S = 5;

    /** 10秒/格 */
    public static final int TSI_10S = 6;

    /** 5秒/格 */
    public static final int TSI_5S = 7;

    /** 2秒/格 */
    public static final int TSI_2S = 8;

    /** 1秒/格 */
    public static final int TSI_1S = 9;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位常量定义（毫秒级）
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 500毫秒/格 */
    public static final int TSI_500mS = 10;

    /** 200毫秒/格 */
    public static final int TSI_200mS = 11;

    /** 100毫秒/格 */
    public static final int TSI_100mS = 12;

    /** 50毫秒/格 */
    public static final int TSI_50mS = 13;

    /** 20毫秒/格 */
    public static final int TSI_20mS = 14;

    /** 10毫秒/格 */
    public static final int TSI_10mS = 15;

    /** 5毫秒/格 */
    public static final int TSI_5mS = 16;

    /** 2毫秒/格 */
    public static final int TSI_2mS = 17;

    /** 1毫秒/格 */
    public static final int TSI_1mS = 18;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位常量定义（微秒级）
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 500微秒/格 */
    public static final int TSI_500uS = 19;

    /** 200微秒/格 */
    public static final int TSI_200uS = 20;

    /** 100微秒/格 */
    public static final int TSI_100uS = 21;

    /** 50微秒/格 */
    public static final int TSI_50uS = 22;

    /** 20微秒/格 */
    public static final int TSI_20uS = 23;

    /** 10微秒/格 */
    public static final int TSI_10uS = 24;

    /** 5微秒/格 */
    public static final int TSI_5uS = 25;

    /** 2微秒/格 */
    public static final int TSI_2uS = 26;

    /** 1微秒/格 */
    public static final int TSI_1uS = 27;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位常量定义（纳秒级）
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 500纳秒/格 */
    public static final int TSI_500nS = 28;

    /** 200纳秒/格 */
    public static final int TSI_200nS = 29;

    /** 100纳秒/格 */
    public static final int TSI_100nS = 30;

    /** 50纳秒/格 */
    public static final int TSI_50nS = 31;

    /** 20纳秒/格 */
    public static final int TSI_20nS = 32;

    /** 10纳秒/格 */
    public static final int TSI_10nS = 33;

    /** 5纳秒/格 */
    public static final int TSI_5nS = 34;

    /** 2纳秒/格 */
    public static final int TSI_2nS = 35;

    /** 1纳秒/格 */
    public static final int TSI_1nS = 36;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位常量定义（皮秒级）
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 500皮秒/格 */
    public static final int TSI_500pS = 37;

    /** 250皮秒/格 */
    public static final int TSI_250pS = 38;

    /** 最大时基档位索引 */
    private static final int TSI_MAX = TSI_250pS;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 主窗口（或放大模式缩略窗口）时间位置，单位0.1ps，中心点相对触发位置的偏移量 */
    private long timePoseMain = 0;

    /** 放大模式放大窗口时间位置，单位0.1ps，屏幕中心点相对触发位置的偏移量 */
    private long timePoseZoom = 0;

    /** 主窗口时基档位ID */
    private int timeScaleIdMain = TSI_5mS;

    /** 缩放窗口时基档位ID */
    private int timeScaleIdZoom = TSI_5mS;

    /** 波形起始X坐标 */
    private int startX = 0;

    /** 波形结束X坐标 */
    private int endX = 0;

    /** X轴时基值列表（秒/格） */
    private List<Double> xAxis = new ArrayList<>();

    /** 水平轴动作处理对象 */
    private HorizontalAxisAction horizontalAxisAction;

    /** 时基跟随通道标志，true表示时基跟随参考波形变化 */
    private boolean scaleFollowingCh = true;

    /** 最小时基档位 */
    private static int minGear = TSI_MIN;

    /** 最大时基档位 */
    private static int maxGear = TSI_MAX;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数
     * 初始化水平轴，创建动作处理对象，注册事件监听器
     */
    public HorizontalAxis() {
        horizontalAxisAction = new HorizontalAxisAction(this);                          // 创建水平轴动作处理对象
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OPEN, this);          // 注册通道打开事件
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_CLOSE, this);         // 注册通道关闭事件
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_ZOOM, this);          // 注册显示缩放事件
        EventFactory.addEventObserver(EventFactory.EVENT_MEM_DEPTH, this);             // 注册存储深度事件
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_STATE, this);           // 注册示波器状态事件
        EventFactory.addEventObserver(EventFactory.EVENT_FORCE_MEM_DEPTH, this);       // 注册强制存储深度事件
        EventFactory.addEventObserver(EventFactory.EVENT_CHANGE_CH_TIMEBASE, this);    // 注册通道时基变化事件
        InitStdXAxis();                                                                 // 初始化标准X轴
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位验证方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 验证时基档位ID是否有效
     *
     * @param timeScaleId 时基档位ID
     * @return true: 有效
     *         false: 无效
     */
    public static boolean isValidScaleId(int timeScaleId){
        return minGear <=  timeScaleId && maxGear >= timeScaleId;                       // 检查是否在有效范围内
    }

    /**
     * 判断是否为Roll模式（滚动模式）
     * 当主窗口时基档位大于100mS时为Roll模式
     *
     * @param timeScale 时基档位ID
     * @return true: Roll模式
     *         false: 非Roll模式
     */
    public static boolean isRoolScale(int timeScale) {
        return timeScale <= TSI_100mS;                                                  // 大于100mS为Roll模式
    }

    /**
     * 判断当前是否为Roll模式（滚动模式）
     *
     * @return true: Roll模式
     *         false: 非Roll模式
     */
    public static boolean isRoolScale() {
        return HorizontalAxis.getInstance().timeScaleIdMain <= TSI_100mS;               // 检查主窗口时基档位
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 档位范围管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取最小时基档位
     *
     * @return 最小时基档位ID
     */
    public static int getMinGear() {
        return minGear;                                                                // 返回最小档位
    }

    /**
     * 设置最小时基档位
     *
     * @param gear 时基档位ID
     */
    public static void setMinGear(int gear) {
        minGear = gear;                                                                // 设置最小档位
    }

    /**
     * 获取最大时基档位
     *
     * @return 最大时基档位ID
     */
    public static int getMaxGear() {
        return maxGear;                                                                // 返回最大档位
    }

    /**
     * 设置最大时基档位
     *
     * @param gear 时基档位ID
     */
    public static void setMaxGear(int gear) {
        maxGear = gear;                                                                // 设置最大档位
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // X轴初始化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化标准X轴
     * 使用标准时基值填充X轴列表
     */
    public void InitStdXAxis() {
        xAxis.clear();                                                                 // 清空X轴列表
        for (int i = minGear; i <= maxGear; i++) {                                     // 遍历所有档位
            xAxis.add(stdTimeScaleIdVal(i));                                           // 添加标准时基值
        }
    }

    /**
     * 初始化X轴
     * 根据当前存储深度计算实际时基值
     * 如果列表长度变化，发送事件通知
     */
    public void initXAxis() {
        boolean bChange = false;                                                       // 变化标志
        synchronized (this) {                                                          // 同步保护
            int l = xAxis.size();                                                      // 保存原列表长度
            xAxis.clear();                                                             // 清空X轴列表
            for (int i = minGear; i <= maxGear; i++) {                                 // 遍历所有档位
                xAxis.add(timeScaleIdVal(i));                                          // 添加实际时基值
            }
            if( l != xAxis.size()){                                                    // 列表长度变化
                bChange = true;                                                        // 设置变化标志
            }
        }
        if(bChange) {                                                                  // 发生变化
            EventFactory.sendEvent(EventFactory.EVENT_TIME_SCALE_LIST);               // 发送时基列表事件
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基值转换方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 根据时基值获取时基档位ID
     *
     * @param val 时基值（秒/格）
     * @return 时基档位ID
     */
    public int timeValtoTimeScaleId(double val) {
        synchronized (this) {                                                          // 同步保护
            for (int i = maxGear; i > minGear; i--) {                                  // 从最大档位开始遍历
                if (i < xAxis.size()) {                                                // 索引有效
                    if (val < ((xAxis.get(i) + xAxis.get(i - 1)) / 2))                 // 值小于中间值
                        return i;                                                      // 返回当前档位
                }
            }
        }
        return minGear;                                                                // 返回最小档位
    }

    /**
     * 根据时基档位ID获取实际时基值
     * 考虑存储深度的影响，可能对时基值进行调整
     *
     * @param timeScaleId 时基档位ID
     * @return 实际时基值（秒/格）
     */
    private double timeScaleIdVal(int timeScaleId) {

        IMemDepth memDepth = MemDepthFactory.getMemDepth();                            // 获取存储深度对象
        double val = stdTimeScaleIdVal(timeScaleId);                                   // 获取标准时基值

        if (memDepth.isSpecialTimeScale()) {                                           // 特殊时基模式
            int stdTimeScaleId;                                                         // 标准时基档位
            Scope scope = Scope.getInstance();                                          // 获取Scope实例
            double timeScaleRatio = memDepth.getTimeScaleRatio(timeScaleId);           // 获取时基比率
            val *= timeScaleRatio;                                                      // 应用时基比率

            if (scope.isRun()) {                                                        // 运行状态
                if (!scope.isZoom()) {                                                  // 非缩放模式
                    return val;                                                         // 直接返回
                }
                stdTimeScaleId = getTimeScaleIdOfView(WPI_STANDARD);                   // 获取标准窗口时基档位
            } else {                                                                   // 停止状态
                stdTimeScaleId = ScopeFrozen.getInstance().getTimeScaleId();           // 获取冻结状态时基档位
            }

            //小于等于标准时基档时，上述计算的肯定正确，只有展开时才可能会修改时基档
            if (timeScaleId <= stdTimeScaleId)                                          // 时基档位小于等于标准档位
                return val;                                                             // 直接返回

            //zoom且时基大于缩略图、静态，只有这两种情况才有可能需要对时基特殊处理
            //注意，对于参考波形，时基不用特殊处理
            double timeScaleStd = stdTimeScaleIdVal(stdTimeScaleId) * memDepth.getTimeScaleRatio(stdTimeScaleId); // 标准时基值
            double timeScaleNow = stdTimeScaleIdVal(timeScaleId) * timeScaleRatio;     // 当前时基值
            double iScreenNum = timeScaleStd / timeScaleNow;//屏数计算                 // 计算屏数
            double iLieNum = (float) (iScreenNum * ScopeBase.getWidth());//总列数       // 计算总列数
            double tmp = iLieNum / scope.zunMemDepth();//插值倍数                      // 计算插值倍数

            if (tmp > 1.01) {//插值                                                     // 插值模式
                if ((tmp > 62.5 - 0.01 && tmp < 62.5 + 0.01)//temp == 62.5             // 插值倍数为62.5
                        || (tmp > 31.25 - 0.01 && tmp < 31.25 + 0.01))//temp == 31.25  // 或插值倍数为31.25
                {
                    val = val * 5.0 / 4;                                               // 调整时基值
                } else if ((tmp > 1.6 - 0.01 && tmp < 1.6 + 0.01)) {                   // 插值倍数为1.6
                    val = val * 4 / 5;                                                 // 调整时基值
                }
            } else {//抽样                                                             // 抽样模式
                tmp = 500 / tmp;//500列对应多少数据                                     // 计算每列数据量
                int x = (int) (tmp + 0.1);                                             // 四舍五入
                if (x % 500 != 0) {//每列个数为小数                                     // 每列数据量不是整数
                    if (x % 100 == 0) {//每列个数乘以5为整数（如1.6，3.2）              // 每列数据量乘以5为整数
                        val = val * 5.0 / 4;                                           // 调整时基值
                    }
                }
            }
        }

        return val;                                                                    // 返回时基值
    }

    /**
     * 根据时基档位ID获取标准时基值
     * 标准时基值不考虑存储深度的影响
     *
     * @param timeScaleId 时基档位ID
     * @return 标准时基值（秒/格）
     */
    public static double stdTimeScaleIdVal(int timeScaleId) {
        double ret = 1e-9d;                                                            // 默认值1ns
        switch (timeScaleId) {                                                         // 根据时基档位ID
            case TSI_1KS:                                                              // 1000秒
                ret = 1000;
                break;
            case TSI_500S:                                                             // 500秒
                ret = 500;
                break;
            case TSI_200S:                                                             // 200秒
                ret = 200;
                break;
            case TSI_100S:                                                             // 100秒
                ret = 100;
                break;
            case TSI_50S:                                                              // 50秒
                ret = 50;
                break;
            case TSI_20S:                                                              // 20秒
                ret = 20;
                break;
            case TSI_10S:                                                              // 10秒
                ret = 10;
                break;
            case TSI_5S:                                                               // 5秒
                ret = 5;
                break;
            case TSI_2S:                                                               // 2秒
                ret = 2;
                break;
            case TSI_1S:                                                               // 1秒
                ret = 1;
                break;
            case TSI_500mS:                                                            // 500毫秒
                ret = 0.5;
                break;
            case TSI_200mS:                                                            // 200毫秒
                ret = 0.2;
                break;
            case TSI_100mS:                                                            // 100毫秒
                ret = 0.1;
                break;
            case TSI_50mS:                                                             // 50毫秒
                ret = 0.05;
                break;
            case TSI_20mS:                                                             // 20毫秒
                ret = 0.02;
                break;
            case TSI_10mS:                                                             // 10毫秒
                ret = 0.01;
                break;
            case TSI_5mS:                                                              // 5毫秒
                ret = 0.005;
                break;
            case TSI_2mS:                                                              // 2毫秒
                ret = 0.002;
                break;
            case TSI_1mS:                                                              // 1毫秒
                ret = 0.001;
                break;
            case TSI_500uS:                                                            // 500微秒
                ret = 500e-6;
                break;
            case TSI_200uS:                                                            // 200微秒
                ret = 200e-6;
                break;
            case TSI_100uS:                                                            // 100微秒
                ret = 100e-6;
                break;
            case TSI_50uS:                                                             // 50微秒
                ret = 50e-6;
                break;
            case TSI_20uS:                                                             // 20微秒
                ret = 20e-6;
                break;
            case TSI_10uS:                                                             // 10微秒
                ret = 10e-6;
                break;
            case TSI_5uS:                                                              // 5微秒
                ret = 5e-6;
                break;
            case TSI_2uS:                                                              // 2微秒
                ret = 2e-6;
                break;
            case TSI_1uS:                                                              // 1微秒
                ret = 1e-6;
                break;
            case TSI_500nS:                                                            // 500纳秒
                ret = 500e-9;
                break;
            case TSI_200nS:                                                            // 200纳秒
                ret = 200e-9;
                break;
            case TSI_100nS:                                                            // 100纳秒
                ret = 100e-9;
                break;
            case TSI_50nS:                                                             // 50纳秒
                ret = 50e-9;
                break;
            case TSI_20nS:                                                             // 20纳秒
                ret = 20e-9;
                break;
            case TSI_10nS:                                                             // 10纳秒
                ret = 10e-9;
                break;
            case TSI_5nS:                                                              // 5纳秒
                ret = 5e-9;
                break;
            case TSI_2nS:                                                              // 2纳秒
                ret = 2e-9;
                break;
            case TSI_1nS:                                                              // 1纳秒
                ret = 1e-9;
                break;
            case TSI_500pS:                                                            // 500皮秒
                ret = 500e-12;
                break;
            case TSI_250pS:                                                            // 250皮秒
            default:                                                                   // 默认
                ret = 250e-12;
                break;
        }
        return ret;                                                                    // 返回时基值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // X轴访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取X轴时基值列表
     * 返回列表的副本，避免外部修改
     *
     * @return X轴时基值列表（秒/格）
     */
    public List<Double> getxAxis() {
        synchronized (this) {                                                          // 同步保护
            return new ArrayList<>(xAxis);                                             // 返回副本
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 窗口模式判断方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前窗口模式ID
     *
     * @return 窗口模式ID
     *         WPI_STANDARD: 标准窗口
     *         WPI_LARGE: 放大窗口
     */
    public int getWPIId() {
        int id = WPI_STANDARD;                                                         // 默认标准窗口
        if (Display.getInstance().isZoom())                                            // 缩放模式
            id = WPI_LARGE;                                                            // 放大窗口
        return id;                                                                     // 返回窗口模式ID
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基值获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前视图的时基值
     *
     * @return 时基值（秒/格）
     */
    public double getTimeScaleIdVal() {
        return getTimeScaleIdVal(getTimeScaleIdOfView());                              // 获取当前视图的时基值
    }

    /**
     * 根据时基档位ID获取时基值
     *
     * @param timeScaleId 时基档位ID
     * @return 时基值（秒/格）
     * @throws IllegalArgumentException 时基档位ID无效时抛出异常
     */
    public double getTimeScaleIdVal(int timeScaleId) {

        double val = 0;                                                                // 初始化返回值
        synchronized (this) {                                                          // 同步保护
            if (timeScaleId >= minGear && timeScaleId <= maxGear) {                    // 档位ID有效
                int idx = timeScaleId - minGear;                                       // 计算索引
                if(idx < xAxis.size()) {                                               // 索引有效
                    val = xAxis.get(idx);                                              // 获取时基值
                }
            } else {                                                                   // 档位ID无效
                throw new IllegalArgumentException();                                   // 抛出异常
            }
        }

        return val;                                                                    // 返回时基值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前视图的时基档位ID
     *
     * @return 时基档位ID
     */
    public int getTimeScaleIdOfView() {
        return getTimeScaleIdOfView(getWPIId());                                       // 获取当前视图的时基档位
    }

    /**
     * 设置当前视图的时基档位ID
     *
     * @param timeScaleId 时基档位ID
     */
    public void setTimeScaleIdOfView(int timeScaleId) {
        setTimeScaleIdOfView(getWPIId(), timeScaleId);                                 // 设置当前视图的时基档位
    }

    /**
     * 设置指定窗口的时基档位ID
     *
     * @param WPIId 窗口模式ID
     * @param timeScaleId 时基档位ID
     */
    public void setTimeScaleIdOfView(int WPIId, int timeScaleId) {
        if (WPIId == WPI_LARGE) {                                                      // 放大窗口
            timeScaleIdZoom = timeScaleId;                                             // 设置缩放窗口时基档位
            horizontalAxisAction.timeScaleIdChange();                                  // 通知时基变化
        } else {                                                                       // 标准窗口
            timeScaleIdMain = timeScaleId;                                             // 设置主窗口时基档位
            horizontalAxisAction.timeScaleIdChange();                                  // 通知时基变化
        }

    }

    /**
     * 获取指定窗口的时基档位ID
     *
     * @param WPIId 窗口模式ID
     * @return 时基档位ID
     */
    public int getTimeScaleIdOfView(int WPIId) {
        if (WPIId == WPI_LARGE) return timeScaleIdZoom;                                // 返回缩放窗口时基档位
        return timeScaleIdMain;                                                        // 返回主窗口时基档位
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时间位置获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前视图的时间位置
     *
     * @return 时间位置（单位0.1ps）
     */
    public long getTimePosOfView() {
        return getTimePosOfView(getWPIId());                                           // 获取当前视图的时间位置
    }

    /**
     * 设置当前视图的时间位置
     *
     * @param timePos 时间位置（单位0.1ps）
     */
    public void setTimePosOfView(long timePos) {
        setTimePosOfView(getWPIId(), timePos);                                         // 设置当前视图的时间位置
    }

    /**
     * 获取指定窗口的时间位置
     *
     * @param WPIId 窗口模式ID
     * @return 时间位置（单位0.1ps）
     */
    public long getTimePosOfView(int WPIId) {
        if (WPIId == WPI_LARGE) return timePoseZoom;                                   // 返回缩放窗口时间位置
        return timePoseMain;                                                           // 返回主窗口时间位置
    }

    /**
     * 设置指定窗口的时间位置
     *
     * @param WPIId 窗口模式ID
     * @param timePos 时间位置（单位0.1ps）
     */
    public void setTimePosOfView(int WPIId, long timePos) {
        if (WPIId == WPI_LARGE) {                                                      // 放大窗口
            timePoseZoom = timePos;                                                    // 设置缩放窗口时间位置
            horizontalAxisAction.timePosChange();                                      // 通知时间位置变化
        } else {                                                                       // 标准窗口
            timePoseMain = timePos;                                                    // 设置主窗口时间位置
            horizontalAxisAction.timePosChange();                                      // 通知时间位置变化
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Roll模式判断方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断主窗口时基是否大于100ms
     * 用于判断是否为Roll模式
     *
     * @return true: 大于100ms（Roll模式）
     *         false: 小于等于100ms（非Roll模式）
     */
    public boolean isGreater100ms() {
        return (timeScaleIdMain <= TSI_100mS);                                         // 检查主窗口时基档位
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时间和像素转换方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 根据时基和时间位置计算触发时刻在屏幕上的像素位置
     *
     * @param timeScale 时基值（秒/格）
     * @param timePos 时间位置（单位0.1ps）
     * @return 触发时刻在屏幕上的像素位置
     */
    public long getTimePoseOfGrid(double timeScale, long timePos) {
        long offset;                                                                   // 偏移量
        timeScale = (double) ScopeBase.getHorizonPerGridPixels() * timePos / (timeScale * 1e13); // 计算像素位置
//        if (timeScale < 0)
//            offset = (long) (timeScale - 0.5);
//        else
//            offset = (long) (timeScale + 0.5);

        return Math.round(timeScale);                                                  // 四舍五入返回
    }

    /**
     * 将时间值转换为像素值
     *
     * @param WPIId 窗口模式ID
     * @param val 时间值（秒）
     * @return 像素值
     */
    public static long TimebaseToPix(int WPIId,double val) {
        Scope scope = Scope.getInstance();                                             // 获取Scope实例
        double timeScale;                                                              // 时基值
        if (WPIId == WPI_STANDARD) {                                                   // 标准窗口
            timeScale = scope.timeScale_mainBoard()/ScopeBase.getHorizonPerGridPixels(); // 获取主窗口每格像素时间
        } else {                                                                       // 放大窗口
            timeScale = scope.timeScale_zoomBoard()/ScopeBase.getHorizonPerGridPixels(); // 获取缩放窗口每格像素时间
        }

        return (long)(val>0?val/timeScale + 0.5:val/timeScale-0.5);                    // 计算像素值并四舍五入
    }

    /**
     * 将像素值转换为时间值
     *
     * @param WPIId 窗口模式ID
     * @param pix 像素值
     * @return 时间值（秒）
     */
    public static double PixToTimebase(int WPIId, long pix){
        Scope scope = Scope.getInstance();                                             // 获取Scope实例
        double timeScale;                                                              // 时基值
        if (WPIId == WPI_STANDARD) {                                                   // 标准窗口
            timeScale = scope.timeScale_mainBoard()/ScopeBase.getHorizonPerGridPixels(); // 获取主窗口每格像素时间
        } else {                                                                       // 放大窗口
            timeScale = scope.timeScale_zoomBoard()/ScopeBase.getHorizonPerGridPixels(); // 获取缩放窗口每格像素时间
        }
        return pix*timeScale;                                                          // 计算时间值
    }

    /**
     * 获取触发时刻在屏幕上的位置
     *
     * @param WPIId 窗口模式ID
     * @return 触发时刻在屏幕上的像素位置
     */
    public long getTimePoseOfGrid(int WPIId) {
        Scope scope = Scope.getInstance();                                             // 获取Scope实例
        double timeScale;                                                              // 时基值
        if (WPIId == WPI_STANDARD) {                                                   // 标准窗口
            timeScale = scope.timeScale_mainBoard();                                   // 获取主窗口时基
        } else {                                                                       // 放大窗口
            timeScale = scope.timeScale_zoomBoard();                                   // 获取缩放窗口时基
        }
        return getTimePoseOfGrid(timeScale, getTimePosOfView(WPIId));                  // 计算触发时刻位置
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时间位置校正方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 校正时间位置
     * 确保时间位置在有效范围内
     */
    public void correctTimePose() {
        long pos = getTimePosOfView(WPI_STANDARD);                                     // 获取主窗口时间位置
        long pos1 = correctTimePose(pos);                                              // 校正时间位置
        if (pos != pos1)                                                               // 位置发生变化
            setTimePosOfView(WPI_STANDARD, pos1);                                      // 设置校正后的位置
    }

    /**
     * 校正时间位置
     * 根据示波器状态和显示模式限制时间位置范围
     *
     * @param pos 时间位置（单位0.1ps）
     * @return 校正后的时间位置
     */
    public long correctTimePose(long pos) {
        Scope scope = Scope.getInstance();                                             // 获取Scope实例
        if (scope.isZoom())                                                            // 缩放模式
            return pos;                                                                // 不校正
        if (scope.isUsersetReset()){                                                   // 用户设置重置
            return pos;                                                                // 不校正
        }

        long timeScreen_n = -(long) (scope.timeOneScreen_main() * scope.screenNum_Main() + 0.1); // 计算一屏时间的负值
//        Logger.i(TAG, "correctTimePose() ==>[0]"
//                +" pos:"+pos+"["+(pos/1e13)+"]"
//                +" timeOneScreen_main:"+(scope.timeOneScreen_main()/1e13)
//                +" screenNum_Main:"+scope.screenNum_Main()
//                +" timeScreen_n:"+(timeScreen_n/1e13)
//        + "run:" + scope.isRun());
        if (scope.isRun()) {                                                           // 运行状态
            if (pos < timeScreen_n) {                                                  // 位置超出范围
                pos = timeScreen_n;                                                    // 限制到边界
            }
        } else {                                                                       // 停止状态

            long half_timeScreen_n = timeScreen_n/2;                                   // 半屏时间
            long xpos = ScopeFrozen.getInstance().getTimePosOfView();                  // 获取冻结状态时间位置
            if (pos < 0) {                                                             // 位置为负
                if(xpos < half_timeScreen_n) {                                         // 冻结位置在左半屏
                    if(pos < timeScreen_n) {                                           // 位置超出范围
                        pos = timeScreen_n;                                            // 限制到边界
                    }
                } else {                                                               // 冻结位置在右半屏
                    half_timeScreen_n += xpos;                                         // 调整半屏时间
                    if (pos < half_timeScreen_n) {                                     // 位置超出范围
                        pos = half_timeScreen_n;                                       // 限制到边界
                    }
                }
            } else {                                                                   // 位置为正
                if(xpos > -half_timeScreen_n) {                                        // 冻结位置在右半屏
                    if(pos < xpos+half_timeScreen_n) {                                 // 位置超出范围
                        pos = xpos+half_timeScreen_n;                                  // 限制到边界
                    }
                    if(pos > xpos-half_timeScreen_n) {                                 // 位置超出范围
                        pos = xpos-half_timeScreen_n;                                  // 限制到边界
                    }
                } else if(xpos < half_timeScreen_n) {                                  // 冻结位置在左半屏
                    if(pos > 0) {                                                      // 位置为正
                        pos = 0;                                                       // 限制到0
                    }
                } else {                                                               // 其他情况
                    half_timeScreen_n -= xpos;                                         // 调整半屏时间
                    if (pos > -half_timeScreen_n) {                                    // 位置超出范围
                        pos = -half_timeScreen_n;                                      // 限制到边界
                    }
                }
            }
        }
        return pos;                                                                    // 返回校正后的位置
    }

    /**
     * 根据时基和像素位置计算时间位置
     *
     * @param timeScale 时基值（秒/格）
     * @param pos 像素位置
     * @return 时间位置（单位0.1ps）
     */
    public long getTimePose(double timeScale, long pos) {
        timeScale = timeScale * 1e13 * pos / ScopeBase.getHorizonPerGridPixels();      // 计算时间位置
        long offset;                                                                   // 偏移量
        if (timeScale < 0)                                                             // 负值
            offset = (long) (timeScale - 0.5);                                         // 向下取整
        else                                                                           // 正值
            offset = (long) (timeScale + 0.5);                                         // 向上取整
        return offset;                                                                 // 返回时间位置
    }

    /**
     * 校正时间位置（移动触发位置时）
     * 确保触发位置在数据范围内
     */
    public void correctTimePose_poseMove() {
        Scope scope = Scope.getInstance();                                             // 获取Scope实例
        boolean zoom = scope.isZoom();                                                 // 是否缩放模式

        long posMain = getTimePosOfView(WPI_STANDARD);                                 // 主窗口时间位置
        long pos = getTimePosOfView();                                                 // 当前视图时间位置
        long halfTime = scope.timeOneScreen_main() / 2;                                // 半屏时间
        if (zoom) {                                                                    // 缩放模式
            long lx1 = halfTime - posMain;                                             // 左边界
            long lx2 = halfTime + posMain;                                             // 右边界
            if (-pos > lx1) {//判断数据头是否在屏幕中心偏右                            // 数据头超出左边界
                setTimePosOfView(-lx1);                                                // 限制到左边界
            } else if (pos > lx2) {//判断数据尾是否在屏幕中心偏左                     // 数据尾超出右边界
                setTimePosOfView(lx2);                                                 // 限制到右边界
            }
        }
    }

    /**
     * 根据窗口模式和像素位置计算时间位置
     *
     * @param WPIId 窗口模式ID
     * @param pos 像素位置
     * @return 时间位置（单位0.1ps）
     */
    public long getTimePose(int WPIId, long pos) {
        Scope scope = Scope.getInstance();                                             // 获取Scope实例
        double timeScale;                                                              // 时基值
        if (WPIId == WPI_STANDARD) {                                                   // 标准窗口
            timeScale = scope.timeScale_mainBoard();                                   // 获取主窗口时基
        } else {                                                                       // 放大窗口
            timeScale = scope.timeScale_zoomBoard();                                   // 获取缩放窗口时基
        }
        return getTimePose(timeScale, pos);                                            // 计算时间位置
    }

    /**
     * 根据像素位置计算时间位置
     *
     * @param pos 像素位置
     * @return 时间位置（单位0.1ps）
     */
    public long getTimePose(long pos) {
        return getTimePose(getWPIId(), pos);                                           // 计算时间位置
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 100飞秒常量（10万亿） */
    public static final long TIME_FS_100 = 10_000_000_000_000L;

    /** 最大时间位置（12ks，单位0.1ps） */
    private static final long MAX_TIME_POS = (long) ScopeBase.getHorizonGridCnt() * 1000 * 1000 * 1000 * 1000 * 1000 * 10;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时间位置变化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 根据时基、时间位置和偏移像素计算新的时间位置
     *
     * @param timeScale 时基值（秒/格）
     * @param timePos 时间位置（单位0.1ps）
     * @param offset 偏移像素
     * @return 新的时间位置（单位0.1ps）
     */
    public long timePoseOfViewChangeEx(double timeScale, long timePos, long offset) {

        long gridTime = (long) (timeScale * 1e13 + 0.1);//0.1ps                         // 计算每格时间（单位0.1ps）
        gridTime /= ScopeBase.getHorizonPerGridPixels();                               // 计算每像素时间
        offset = timePos / gridTime - offset;                                          // 计算偏移量
        long pos = offset * gridTime;                                                  // 计算新位置
        if (pos > MAX_TIME_POS)                                                        // 超出最大值
            pos = MAX_TIME_POS;                                                        // 限制到最大值
        timePos = pos;                                                                 // 更新时间位置
        return timePos;                                                                // 返回新位置
    }

    /**
     * 获取触发时刻在屏幕上的像素位置
     *
     * @return 触发时刻在屏幕上的像素位置
     */
    public long getTimePoseOfViewPix() {
        return getTimePoseOfGrid(getWPIId());                                          // 获取触发时刻位置
    }

    /**
     * 设置触发时刻在屏幕上的像素位置
     *
     * @param pos 像素位置
     */
    public void setTimePoseOfViewPix(long pos) {

        pos = getTimePose(pos);                                                        // 转换为时间位置
//        Logger.i(TAG, "setTimePoseOfViewPix() ==>"+" [2]pos:"+pos+" - "+(1.0*pos/1e13));
        if (pos > MAX_TIME_POS) {                                                      // 超出最大值
            pos = MAX_TIME_POS;                                                        // 限制到最大值
        }
        pos = correctTimePose(pos);                                                    // 校正时间位置
//        Logger.i(TAG, "setTimePoseOfViewPix() ==>"+" [3]pos:"+pos+" - "+(1.0*pos/1e13));
        setTimePosOfView(pos);                                                         // 设置时间位置
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 每像素时间获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取每像素对应的时间（秒）
     *
     * @return 每像素时间（秒）
     */
    public double getTimesPrePix() {
        double timeScale = getTimeScaleIdVal();                                        // 获取时基值
        timeScale /= ScopeBase.getHorizonPerGridPixels();                              // 计算每像素时间
        return timeScale;                                                              // 返回每像素时间
    }

    /**
     * 将秒转换为100飞秒（0.1ps）
     *
     * @param s 秒值
     * @return 100飞秒值
     */
    public static long Sto100FS(double s) {
        return (long) (s * 1000 * 1000 * 1000 * 1000 * 10 + 0.01);                     // 转换并四舍五入
    }

    /**
     * 获取每像素对应的时间（100飞秒单位）
     *
     * @return 每像素时间（单位0.1ps）
     */
    public long getTimesPrePixExt() {
        return Sto100FS(getTimesPrePix());                                             // 转换并返回
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 波形起始和结束X坐标方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取波形起始X坐标
     *
     * @return 起始X坐标
     */
    public synchronized int getStartX() {
        return startX;                                                                 // 返回起始X坐标
    }

    /**
     * 设置波形起始X坐标
     *
     * @param startX 起始X坐标
     */
    public synchronized void setStartX(int startX) {
        this.startX = startX;                                                          // 设置起始X坐标
    }

    /**
     * 获取波形结束X坐标
     *
     * @return 结束X坐标
     */
    public synchronized int getEndX() {
        return endX;                                                                   // 返回结束X坐标
    }

    /**
     * 设置波形结束X坐标
     *
     * @param endX 结束X坐标
     */
    public synchronized void setEndX(int endX) {
        this.endX = endX;                                                              // 设置结束X坐标
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件观察者方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 事件观察者更新方法
     * 处理各类示波器事件
     *
     * @param observable 事件源
     * @param data 事件数据
     */
    @Override
    public void update(Observable observable, Object data) {
        EventBase eventBase = (EventBase) data;                                        // 转换为事件基类
        if (eventBase != null) {                                                       // 事件有效
            switch (eventBase.getId()) {                                               // 根据事件ID处理
                case EventFactory.EVENT_CHANNEL_OPEN:                                  // 通道打开事件
                case EventFactory.EVENT_CHANNEL_CLOSE:                                 // 通道关闭事件
                case EventFactory.EVENT_DISPLAY_ZOOM:                                  // 显示缩放事件
                case EventFactory.EVENT_MEM_DEPTH:                                     // 存储深度事件
                case EventFactory.EVENT_SCOPE_STATE:                                   // 示波器状态事件
                case EventFactory.EVENT_FORCE_MEM_DEPTH:                               // 强制存储深度事件
                    initXAxis();                                                        // 初始化X轴
                    break;
                case EventFactory.EVENT_CHANGE_CH_TIMEBASE:                            // 通道时基变化事件
                    double timeBase = (double) eventBase.getData();                    // 获取时基值
                    Logger.i(TAG, " timeBase= " + timeBase + " ,scaleFollowingCh= " + scaleFollowingCh); // 打印日志
                    int chIdx = eventBase.getChannelIndex();                           // 获取通道索引
                    if (scaleFollowingCh) {                                            // 时基跟随通道
                        changeScaleByRef(timeBase);                                    // 根据参考波形调整时基
                        //调整完时基之后，进行触发位置同步
                        if (ChannelFactory.isRefCh(chIdx)) {                           // 参考通道
                            RefChannel refChannel = ChannelFactory.getRefChannel(chIdx); // 获取参考通道
                            if (refChannel != null && refChannel.getRefType() != WaveData.FFT_WAVE) { // 非FFT波形
                                EventFactory.sendEvent(new EventBase(EventFactory.EVENT_SET_CH_TIMEPOS, refChannel.getRefXPos_original() * 1E-13)); // 发送时间位置事件
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 根据参考波形时基调整时基档位
     *
     * @param timeBase 参考波形时基值
     */
    private void changeScaleByRef(double timeBase) {
        Logger.d(TAG, "changeScaleByRef timeBase= " + timeBase);                       // 打印日志
        int scaleIsd = -1;                                                             // 时基档位ID
        for (int i = 0; i < xAxis.size(); i++) {                                       // 遍历X轴列表
            if (xAxis.get(i) == timeBase) {                                            // 找到匹配的时基值
                scaleIsd = i;                                                          // 保存档位ID
                break;                                                                 // 跳出循环
            }
        }
        if (scaleIsd >= 0) {                                                           // 找到匹配的档位
            setTimeScaleIdOfView(scaleIsd);                                            // 设置时基档位
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 波特率转换方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 根据波特率计算时基档位
     * 用于串口协议解码
     *
     * @param baudrate 波特率
     * @return 时基档位ID
     */
    public static int BaudrateToTimeScale(int baudrate) {
        if (baudrate <= 0) {                                                           // 波特率无效
            return TSI_50mS;                                                           // 返回默认值
        }
        double tsis[] = {1000, 500, 200, 100, 50, 20, 10, 5, 2, 1,                     // 时基值数组
                0.5, 0.2, 0.1, 0.05, 0.02, 0.01, 0.005, 0.002, 0.001,
                500e-6, 200e-6, 100e-6, 50e-6, 20e-6, 10e-6, 5e-6, 2e-6, 1e-6,
                500e-9, 200e-9, 100e-9, 50e-9, 20e-9, 10e-9, 5e-9, 2e-9, 1e-9};
        double ctsi = 9.6 / baudrate;                                                  // 计算位时间
        int ret = TSI_50mS;                                                            // 默认返回值
        int max = tsis.length;                                                         // 数组长度
        for (int i = 0; i < max - 1; i++) {                                            // 遍历时基数组
            if (tsis[i] > ctsi && ctsi >= tsis[i + 1]) {                               // 找到合适的时基
                ctsi = tsis[i + 1];                                                    // 保存时基值
                if ((i + 1) > max) i = 18;                                             // 索引超出范围
                ret = (i + 1);                                                         // 保存档位ID
                break;                                                                 // 跳出循环
            }
        }
        return ret;                                                                    // 返回时基档位ID
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // SCPI接口方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * SCPI接口：查询给定触发时刻的时间值对应在屏幕上的位置信息
     *
     * @param timePosVal 给定触发时刻的时间值（秒）
     * @return 对应屏幕上的位置信息，中间为0，范围从[-350 ~ 349]
     */
    public long SCPIQueryPixInScreenFromTImePosVal(double timePosVal) {
        double pix = timePosVal * ScopeBase.getHorizonPerGridPixels() / getTimeScaleIdVal(); // 计算像素位置
//        if (pix < -350) pix = -350;
//        if(pix > 349) pix = 349;
        return Math.round(pix);                                                        // 四舍五入返回
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基跟随通道方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置时基跟随通道标志
     *
     * @param followingCh true: 时基跟随参考波形变化
     *                    false: 时基不跟随参考波形变化
     */
    public void setScaleFollowingCh(boolean followingCh) {
        this.scaleFollowingCh = followingCh;                                           // 设置跟随标志
    }

    /**
     * 获取时基跟随通道标志
     *
     * @return true: 时基跟随参考波形变化
     *         false: 时基不跟随参考波形变化
     */
    public boolean getScaleFollowingCh() {
        return this.scaleFollowingCh;                                                  // 返回跟随标志
    }

}
