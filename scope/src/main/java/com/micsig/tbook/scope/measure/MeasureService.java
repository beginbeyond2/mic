package com.micsig.tbook.scope.measure;

import android.os.SystemClock;
import android.util.Log;

import com.micsig.base.FilterThread;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.math.MathService;
import com.micsig.tbook.scope.math.MathWave;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                MeasureService - 示波器测量服务类                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Measure模块的测量服务类，位于measure包下，                                  ║
 * ║   提供示波器的自动测量功能调度和管理。                                         ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理测量计算线程（FilterThread）                                        ║
 * ║   2. 调度通道、参考通道、数学通道的测量计算                                   ║
 * ║   3. 处理相位测量和延迟测量                                                  ║
 * ║   4. 管理测量统计信息                                                       ║
 * ║   5. 响应示波器事件触发测量刷新                                             ║
 * ║                                                                              ║
 * ║ 【测量类型分类】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        测量类型                                        │ ║
 * ║   │                                                                      │ ║
 * ║   │   【通道测量】CH_MEASURE (bit 0)                                      │ ║
 * ║   │   - 动态通道（CH1-CH4）的自动测量                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【参考通道测量】REF_MEASURE (bit 1)                                 │ ║
 * ║   │   - 参考通道（REF1-REF4）的自动测量                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【数学通道测量】MATH_MEASURE (bit 2)                                │ ║
 * ║   │   - 数学通道（MATH1-MATH4）的自动测量                                 │ ║
 * ║   │   - 包括FFT数学运算                                                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【事件驱动机制】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        事件监听列表                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   EVENT_CH_WAVE_UPDATE       通道波形更新                            │ ║
 * ║   │   EVENT_REF_WAVE_UPDATE      参考通道波形更新                        │ ║
 * ║   │   EVENT_MATH_WAVE_UPDATE     数学通道波形更新                        │ ║
 * ║   │   EVENT_CHANNEL_CLOSE        通道关闭                                │ ║
 * ║   │   EVENT_CHANNEL_OPEN         通道打开                                │ ║
 * ║   │   EVENT_CHANNEL_VSCALE       通道垂直档位变化                        │ ║
 * ║   │   EVENT_CHANNEL_POS          通道位置变化                            │ ║
 * ║   │   EVENT_DISPLAY_MODE        显示模式变化                            │ ║
 * ║   │   EVENT_DISPLAY_ZOOM        显示缩放变化                            │ ║
 * ║   │   EVENT_TIME_SCALE          时基变化                                │ ║
 * ║   │   EVENT_TIME_POS            时基位置变化                            │ ║
 * ║   │   EVENT_SCOPE_STATE         示波器状态变化                          │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【测量流程】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 事件触发    │───▶│ 测量计算    │───▶│ 统计计算    │                   ║
 * ║   │ measureFlag │    │ MeasureCalc │    │ MeasureStatics                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 使用volatile保证可见性                                                   ║
 * ║   - 测量标志位使用位运算操作                                                ║
 * ║   - 关键操作使用synchronized保护                                            ║
 * ║                                                                              ║
 * ║ 【单例模式】                                                                 ║
 * ║   - 双重检查锁定单例模式                                                     ║
 * ║   - 延迟初始化，线程安全                                                    ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - FilterThread: 测量计算线程基类                                          ║
 * ║   - ChannelFactory: 通道工厂，获取各类型通道                                ║
 * ║   - EventFactory: 事件工厂，发送/接收示波器事件                             ║
 * ║   - Measure: 测量类，执行具体测量计算                                        ║
 * ║   - MathService: 数学服务，FFT刷新                                          ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器测量服务类
 * 负责管理和调度示波器的自动测量功能
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>通道自动测量：动态通道、参考通道、数学通道</li>
 *   <li>相位测量：计算两个通道之间的相位差</li>
 *   <li>延迟测量：计算两个通道之间的时间延迟</li>
 *   <li>测量统计：最小值、最大值、平均值统计</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 刷新所有测量
 * MeasureService.forceMeasureRefresh();
 *
 * // 刷新指定通道测量
 * MeasureService.forceChMeasureRefresh();
 *
 * // 立即测量指定通道
 * MeasureService.forceImmedMeasure(chIdx);
 * </pre>
 *
 * @see Measure
 * @see MeasureStatics
 * @see FilterThread
 */
public class MeasureService extends FilterThread implements Observer{
    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签 */
    private static final String TAG = "MeasureService";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例实例
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 单例实例，使用volatile保证可见性 */
    private static volatile MeasureService instance = null;

    /**
     * 获取MeasureService单例实例
     * 使用双重检查锁定模式，线程安全且高效
     *
     * @return MeasureService实例
     */
    public static MeasureService getInstance() {
        if (instance == null) {                                                        // 第一次检查，避免不必要同步
            synchronized (MeasureService.class) {                                      // 同步块
                if (instance == null) {                                                // 第二次检查
                    instance = new MeasureService();                                   // 创建实例
                }
            }
        }
        return instance;                                                               // 返回实例
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量类型标志位定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 通道测量标志位 (bit 0) */
    private static final int CH_MEASURE = 1<<0;

    /** 参考通道测量标志位 (bit 1) */
    private static final int REF_MEASURE = 1<<1;

    /** 数学通道测量标志位 (bit 2) */
    private static final int MATH_MEASURE = 1<<2;

    /** 统计重置标志位 (bit 8) */
    private static final int STATICS_RESET = 1<<8;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 测量标志位，使用位图表示各类型测量的待处理状态 */
    private volatile int measureFlag = 0;

    /** 统计重置标志位，记录需要重置统计的通道 */
    private volatile int statics_flag = 0;

    /** 通道索引数组，存储所有通道和数学通道的索引 */
    int[] chArray = new int[ChannelFactory.getChNums() + ChannelFactory.getMathChNums()];

    /** 备份的通道数量，用于检测通道变化 */
    int bakChNums = 0;

    /** 当前通道数量 */
    int chNums = 0;

    /** 光标范围标志，是否使用光标测量范围 */
    private static boolean bCursorRang = false;

    /** 同步对象 */
    Object xo = new Object();

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 私有构造函数
     * 初始化测量服务，注册事件监听器
     */
    private MeasureService(){
        super(TAG);                                                                    // 调用父类构造函数，设置线程名
        this.setRunnable(new Runnable() {                                             // 设置测量计算任务
            @Override
            public void run() {                                                        // 测量计算入口
                MeasureCalc();                                                        // 调用测量计算方法
            }
        });
        setBeforeRun(false);                                                          // 设置运行前不等待
        setDelayMillis(100);                                                          // 设置100ms延迟

        // 注册通道波形更新事件监听
        EventFactory.addEventObserver(EventFactory.EVENT_CH_WAVE_UPDATE,this);       // 通道波形更新
        EventFactory.addEventObserver(EventFactory.EVENT_REF_WAVE_UPDATE,this);       // 参考通道波形更新
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_WAVE_UPDATE,this);     // 数学通道波形更新
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_CLOSE,this);        // 通道关闭事件

        // 注册通道参数变化事件监听
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE,this);        // 通道垂直档位变化
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_POS,this);          // 通道位置变化

        // 注册显示和时基事件监听
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_MODE,this);         // 显示模式变化
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_ZOOM,this);         // 显示缩放变化
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_SCALE,this);           // 时基变化
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_POS,this);             // 时基位置变化
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_STATE,this);         // 示波器状态变化

        // 初始化通道索引数组
        AtomicInteger j = new AtomicInteger(0);                                       // 原子计数器
        ChannelFactory.forEachCh(channel -> {                                        // 遍历所有动态通道
            chArray[j.getAndIncrement()] = channel.getChId();                        // 存储通道ID
        });
        ChannelFactory.forEachMath(mathChannel -> {                                   // 遍历所有数学通道
            chArray[j.getAndIncrement()] = mathChannel.getChId();                   // 存储通道ID
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取指定通道的Measure对象
     *
     * @param chIdx 通道索引
     * @return Measure对象，如果通道无效返回null
     */
    private Measure getMeasure(int chIdx){
        IChannel channel = ChannelFactory.getValidChannel(chIdx);                    // 获取有效通道
        if(channel != null){                                                          // 通道有效
            return channel.getMeasure();                                             // 返回测量对象
        }
        return null;                                                                   // 通道无效返回null
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 通道测量计算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行通道测量计算
     * 遍历所有动态通道，执行启用的测量
     *
     * @return true: 任意通道测量成功
     *         false: 所有通道测量失败
     */
    private boolean ChMeasureCalc(){

        boolean []bMeasure = {false};                                                 // 测量结果数组
        ChannelFactory.forEachCh(channel -> {                                         // 遍历所有动态通道
            Measure measure = null;                                                   // 测量对象
            measure = channel.getMeasure();                                          // 获取测量对象
            if(measure != null                                                       // 测量对象有效
                    && measure.isEnable()){                                          // 测量已启用
                bMeasure[0] = measure.MeasureCalc() || bMeasure[0];                // 执行测量计算
            }
        });
        return bMeasure[0];                                                          // 返回测量结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 参考通道测量计算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行参考通道测量计算
     * 遍历所有参考通道，执行启用的测量
     *
     * @return true: 任意参考通道测量成功
     *         false: 所有参考通道测量失败
     */
    private boolean RefMeasureCalc(){
        boolean []bMeasure = {false};                                                 // 测量结果数组
        ChannelFactory.forEachRef(refChannel -> {                                     // 遍历所有参考通道
            Measure measure = refChannel.getMeasure();                                // 获取测量对象
            if(measure != null                                                       // 测量对象有效
                    && measure.isEnable()){                                          // 测量已启用
                bMeasure[0] = measure.MeasureCalc() || bMeasure[0];                // 执行测量计算
            }
        });
        return bMeasure[0];                                                          // 返回测量结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数学通道测量计算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行数学通道测量计算
     * 遍历所有数学通道，执行启用的测量
     * 特殊处理FFT通道的光标测量
     *
     * @return true: 任意数学通道测量成功
     *         false: 所有数学通道测量失败
     */
    private boolean MathMeasureCalc() {
        boolean[] bMeasure = {false};                                                 // 测量结果数组
        boolean[] bForceRefresh = {false};                                            // 强制刷新标志
        ChannelFactory.forEachMath(mathChannel -> {                                    // 遍历所有数学通道
            Measure measure = mathChannel.getMeasure();                              // 获取测量对象
            if (measure != null                                                      // 测量对象有效
                    && measure.isEnable()) {                                         // 测量已启用
                bMeasure[0] = measure.MeasureCalc() || bMeasure[0];                // 执行测量计算

                // FFT通道特殊处理：光标测量需要强制刷新
                if (mathChannel.getMathType() == MathWave.MATH_FFTWAVE) {            // FFT数学通道
                    if (measure.isMeasureItemEnable(Measure.MeasureType.MEASURE_CURSOR_X1)  // 光标X1启用
                            || measure.isMeasureItemEnable(Measure.MeasureType.MEASURE_CURSOR_X2)) { // 光标X2启用
                        bForceRefresh[0] = true;                                     // 设置强制刷新标志
                    }
                }
            }
        });

        if(bForceRefresh[0]){                                                         // 需要强制刷新
            MathService.forceMathRefresh();                                          // 强制刷新数学通道
        }
        return bMeasure[0];                                                          // 返回测量结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量统计方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行测量统计计算
     * 对所有通道执行统计计算，统计测量值的最小/最大/平均值
     */
    private void MeasureStatics(){

        Measure measure;                                                               // 测量对象
        for(int i=0;i<chArray.length;i++) {                                           // 遍历所有通道索引
            measure = getMeasure(chArray[i]);                                         // 获取测量对象
            if(measure != null){                                                      // 测量对象有效
                if(ChannelFactory.isChOpen(chArray[i])) {                            // 通道已打开
                    measure.calcStatics();                                            // 执行统计计算
                }else{                                                               // 通道已关闭
                    measure.getMeasureStatics().reset();                             // 重置统计
                }
            }
        }
    }

    /**
     * 重置指定通道的测量统计
     *
     * @param chIdx 通道索引
     */
    private void MeasureStaticsReset(int chIdx)
    {
        Measure measure = getMeasure(chIdx);                                         // 获取测量对象
        if (measure != null) {                                                       // 测量对象有效
            measure.getMeasureStatics().reset();                                     // 重置统计
        }
    }

    /**
     * 设置指定通道的统计重置标志
     * 线程安全方法
     *
     * @param chIdx 通道索引
     */
    private synchronized void setMeasureStaticsReset(int chIdx){
        statics_flag |= 1 << chIdx;                                                  // 设置重置标志
    }

    /**
     * 重置所有通道的测量统计
     * 线程安全方法
     */
    private synchronized void MeasureStaticsReset(){

        for (int j : chArray) {                                                      // 遍历所有通道
            statics_flag |= 1 << j;                                                  // 设置重置标志
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 波形高度设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置通道的波形高度比率
     * 用于缩放模式下的测量
     *
     * @param channel 通道对象
     */
    private void setWaveHeight(IChannel channel){
        if(channel != null){                                                          // 通道有效
            channel.getMeasure().WaveHeightRate(                                     // 设置波形高度比率
                    Scope.getInstance().isZoom()                                     // 判断是否缩放模式
                            ?(float) ScopeBase.getZoomHeight()/ScopeBase.getHeight() // 缩放模式使用缩放高度
                            :1.0f                                                    // 正常模式使用原始高度
            );
        }
    }

    /**
     * 更新所有通道的波形高度
     * 包括动态通道、数学通道、参考通道
     */
    private void MeasureWaveHeight(){
        ChannelFactory.forEachCh(this::setWaveHeight);                               // 更新动态通道
        ChannelFactory.forEachMath(this::setWaveHeight);                            // 更新数学通道
        ChannelFactory.forEachRef(this::setWaveHeight);                             // 更新参考通道
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 相位和延迟测量方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行相位和延迟测量
     * 遍历所有通道，处理相位测量和延迟测量
     */
    private void MeaseureDelayORPhase(){

        Measure measure,refMeasure;                                                   // 测量对象和参考测量对象
        boolean bValid = false;                                                       // 有效标志

        for (int k : chArray) {                                                      // 遍历所有通道
            measure = getMeasure(k);                                                  // 获取测量对象
            if (measure != null) {                                                   // 测量对象有效

                // 延迟测量处理
                if (measure.isMeasureDelay()) {                                      // 启用延迟测量
                    bValid = false;                                                   // 重置有效标志
                    int idx = measure.getDelayRefChIdx();                            // 获取延迟参考通道
                    if (Scope.getInstance().isChannelInSample(idx)                   // 参考通道正在采样
                            || (ChannelFactory.isMathCh(idx)                         // 或数学通道
                            && ChannelFactory.getMathChannel(idx).isOpen())) {      // 且已打开
                        refMeasure = getMeasure(idx);                                // 获取参考测量对象
                        if (refMeasure != null) {                                   // 参考测量对象有效
                            if (refMeasure.isMeasureItemEnable(Measure.MeasureType.MEASURE_DELAY)) { // 参考通道延迟测量已启用
                                bValid = measure.MeasureDelay(refMeasure);          // 执行延迟测量
                            } else {                                                 // 参考通道延迟测量未启用
                                refMeasure.MeasureItemEnable(Measure.MeasureType.MEASURE_DELAY, true); // 启用参考通道延迟测量
                            }
                        }
                    }
                    measure.setMeasureItemValid(Measure.MeasureType.MEASURE_DELAY, bValid); // 设置延迟测量有效状态
                }

                // 相位测量处理
                if (measure.isMeasurePhase()) {                                       // 启用相位测量
                    bValid = false;                                                   // 重置有效标志
                    int idx = measure.getPhaseRefChIdx();                            // 获取相位参考通道
                    if (Scope.getInstance().isChannelInSample(idx)                   // 参考通道正在采样
                            || (ChannelFactory.isMathCh(idx)                         // 或数学通道
                            && ChannelFactory.getMathChannel(idx).isOpen())) {      // 且已打开
                        refMeasure = getMeasure(idx);                                // 获取参考测量对象
                        if (refMeasure != null) {                                   // 参考测量对象有效
                            if (refMeasure.isMeasureItemEnable(Measure.MeasureType.MEASURE_PHASE)) { // 参考通道相位测量已启用
                                bValid = measure.MeasurePhase(refMeasure);          // 执行相位测量
                            } else {                                                 // 参考通道相位测量未启用
                                refMeasure.MeasureItemEnable(Measure.MeasureType.MEASURE_PHASE, true); // 启用参考通道相位测量
                            }
                        }
                    }
                    measure.setMeasureItemValid(Measure.MeasureType.MEASURE_PHASE, bValid); // 设置相位测量有效状态
                }

            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量计算主方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 测量计算主方法
     * 调度各类型测量计算，处理统计重置，发送测量更新事件
     */
    private void MeasureCalc(){
        boolean bChMeasure = false;                                                   // 通道测量结果
        boolean bRefMeasure = false;                                                 // 参考通道测量结果
        boolean bMathMeasure = false;                                                // 数学通道测量结果

        // 处理统计重置标志
        synchronized (this) {                                                         // 同步保护
            if (statics_flag != 0) {                                                 // 有待重置的统计
                for (int i = 0; i < 32; i++) {                                       // 遍历32个可能的通道
                    if ((statics_flag & (1<< i)) != 0) {                             // 该通道需要重置
                        MeasureStaticsReset(i);                                      // 重置该通道统计
                    }
                }
                statics_flag = 0;                                                     // 清除重置标志
            }
        }

        // 执行测量计算
        if(measureFlag != 0) {                                                       // 有待执行的测量

            // 通道测量
            if ((measureFlag & CH_MEASURE) != 0) {                                   // 通道测量标志
                synchronized (this) {                                                 // 同步保护
                    bakChNums = chNums;                                              // 备份通道数量
                    measureFlag &= ~(CH_MEASURE);                                    // 清除测量标志
                }
                bChMeasure = ChMeasureCalc();                                         // 执行通道测量
            }

            // 参考通道测量
            if ((measureFlag & REF_MEASURE) != 0) {                                  // 参考通道测量标志
                measureFlag &= ~(REF_MEASURE);                                        // 清除测量标志
                bRefMeasure = RefMeasureCalc();                                      // 执行参考通道测量
            }

            // 数学通道测量
            if ((measureFlag & MATH_MEASURE) != 0) {                                  // 数学通道测量标志
                measureFlag &= ~(MATH_MEASURE);                                       // 清除测量标志
                bMathMeasure = MathMeasureCalc();                                    // 执行数学通道测量

            }

            // 执行相位和延迟测量
            MeaseureDelayORPhase();

            // 执行测量统计
            MeasureStatics();

        }

        // 发送测量更新事件
        if(bChMeasure || isChMeasureEnable()){                                        // 通道测量成功或已启用
            EventFactory.sendEvent(EventFactory.EVENT_CH_MEASURE_UPDATE);             // 发送通道测量更新事件
        }
        if(bRefMeasure){                                                             // 参考通道测量成功
            EventFactory.sendEvent(EventFactory.EVENT_REF_MEASURE_UPDATE);            // 发送参考通道测量更新事件
        }
        if(bMathMeasure){                                                            // 数学通道测量成功
            EventFactory.sendEvent(EventFactory.EVENT_MATH_MEASURE_UPDATE);            // 发送数学通道测量更新事件
        }

        // 检查通道数量变化
        synchronized (this){                                                          // 同步保护
            if(chNums != bakChNums){                                                 // 通道数量变化
                measureFlag |= CH_MEASURE;                                           // 设置通道测量标志
                run();                                                               // 重新执行测量
            }else{                                                                   // 通道数量未变化
                chNums = bakChNums = 0;                                              // 重置通道数量
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量启用状态检查方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 检查指定范围内的通道是否有启用的测量
     *
     * @param beginIdx 起始通道索引
     * @param endIdx 结束通道索引
     * @return true: 有启用的测量
     *         false: 没有启用的测量
     */
    private boolean isMeasureEnable(int beginIdx,int endIdx){
        Measure measure;                                                               // 测量对象
        for(int i=beginIdx;i<endIdx;i++) {                                           // 遍历通道范围
            measure = getMeasure(i);                                                  // 获取测量对象
            if(measure != null                                                       // 测量对象有效
                    && measure.isEnable()){                                          // 测量已启用
                return true;                                                         // 返回true
            }
        }
        return false;                                                                 // 没有启用的测量
    }

    /**
     * 检查通道测量是否启用
     *
     * @return true: 有启用的通道测量
     *         false: 没有启用的通道测量
     */
    private boolean isChMeasureEnable(){
        return isMeasureEnable(ChannelFactory.CH1,ChannelFactory.getMaxChIdx());    // 检查通道范围
    }

    /**
     * 检查参考通道测量是否启用
     *
     * @return true: 有启用的参考通道测量
     *         false: 没有启用的参考通道测量
     */
    private boolean isRefMeasureEnable(){
        return isMeasureEnable(ChannelFactory.REF1,ChannelFactory.getMaxRefIdx());  // 检查参考通道范围
    }

    /**
     * 检查数学通道测量是否启用
     *
     * @return true: 有启用的数学通道测量
     *         false: 没有启用的数学通道测量
     */
    private boolean isMathMeasureEnable(){
        return isMeasureEnable(ChannelFactory.MATH1,ChannelFactory.getMaxMathIdx()); // 检查数学通道范围
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量刷新方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 刷新通道测量
     * 设置通道测量标志并触发测量计算
     */
    public void ChMeasureRefresh(){
        if(isChMeasureEnable()) {                                                    // 通道测量已启用
            synchronized (this) {                                                     // 同步保护
                measureFlag |= CH_MEASURE;                                           // 设置通道测量标志
                chNums++;                                                            // 增加通道计数
            }
            run();                                                                    // 触发测量计算
        }
    }

    /**
     * 刷新参考通道测量
     * 设置参考通道测量标志并触发测量计算
     */
    public void RefMeasureRefresh(){
        if(isRefMeasureEnable()) {                                                  // 参考通道测量已启用
            measureFlag |= REF_MEASURE;                                              // 设置参考通道测量标志
            run();                                                                    // 触发测量计算
        }
    }

    /**
     * 刷新数学通道测量
     * 设置数学通道测量标志并触发测量计算
     */
    public void MathMeasureRefresh(){
        if(isMathMeasureEnable()) {                                                  // 数学通道测量已启用
            measureFlag |= MATH_MEASURE;                                              // 设置数学通道测量标志
            run();                                                                    // 触发测量计算
        }
    }

    /**
     * 刷新所有测量
     * 设置所有类型测量标志并触发测量计算
     */
    public void MeasureRefresh(){
        measureFlag = MATH_MEASURE | REF_MEASURE | CH_MEASURE;                        // 设置所有测量标志
        run();                                                                        // 触发测量计算
    }

    /**
     * 立即执行指定通道的测量
     * 异步执行，不等待测量完成
     *
     * @param chIdx 通道索引
     */
    public void immedMeasure(int chIdx){

        run(()->{                                                                     // 异步执行
            if(ChannelFactory.isDynamicCh(chIdx)){                                   // 动态通道
                measureFlag |= CH_MEASURE;                                           // 设置通道测量标志
            }
            if(ChannelFactory.isRefCh(chIdx)){                                      // 参考通道
                measureFlag |= REF_MEASURE;                                          // 设置参考通道测量标志
            }
            if(ChannelFactory.isMathCh(chIdx)){                                     // 数学通道
                measureFlag |= MATH_MEASURE;                                         // 设置数学通道测量标志
            }
            this.MeasureCalc();                                                      // 执行测量计算
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件观察者方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 事件观察者更新方法
     * 处理各类示波器事件，触发相应的测量刷新
     *
     * @param observable 事件源
     * @param data 事件数据
     */
    @Override
    public void update(Observable observable, Object data) {
        EventBase eventBase = (EventBase)data;                                       // 转换为事件基类
        int chIdx = 0;                                                               // 通道索引
        switch (eventBase.getId()){                                                  // 根据事件ID处理
            case EventFactory.EVENT_CHANNEL_CLOSE:                                   // 通道关闭事件
                chIdx = (int) eventBase.getData();                                   // 获取通道索引
                setMeasureStaticsReset(chIdx);                                       // 设置统计重置标志
                // 注意：此处缺少break，会继续执行通道测量刷新
            case EventFactory.EVENT_CH_WAVE_UPDATE:                                  // 通道波形更新事件
                ChMeasureRefresh();                                                  // 刷新通道测量
                break;
            case EventFactory.EVENT_REF_WAVE_UPDATE:                                 // 参考通道波形更新事件
                RefMeasureRefresh();                                                 // 刷新参考通道测量
                break;
            case EventFactory.EVENT_MATH_WAVE_UPDATE:                                // 数学通道波形更新事件
                MathMeasureRefresh();                                                // 刷新数学通道测量
                break;
            case EventFactory.EVENT_CHANNEL_OPEN:                                    // 通道打开事件
            case EventFactory.EVENT_CHANNEL_VSCALE:                                  // 通道垂直档位变化事件
            case EventFactory.EVENT_CHANNEL_POS:                                    // 通道位置变化事件
                chIdx = (int) eventBase.getData();                                   // 获取通道索引
                setMeasureStaticsReset(chIdx);                                       // 设置统计重置标志
                break;
            case EventFactory.EVENT_DISPLAY_ZOOM:                                   // 显示缩放变化事件
                MeasureWaveHeight();                                                 // 更新波形高度
                // 注意：此处缺少break，会继续执行统计重置
            case EventFactory.EVENT_DISPLAY_MODE:                                   // 显示模式变化事件
            case EventFactory.EVENT_TIME_SCALE:                                      // 时基变化事件
            case EventFactory.EVENT_TIME_POS:                                       // 时基位置变化事件
                MeasureStaticsReset();                                               // 重置所有统计
                break;
            case EventFactory.EVENT_SCOPE_STATE:                                    // 示波器状态变化事件
                if(Scope.getInstance().isRun()){                                    // 示波器运行状态
                    MeasureStaticsReset();                                           // 重置所有统计
                }
                break;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 静态刷新方法（对外接口）
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 强制刷新所有测量
     * 静态方法，供外部调用
     */
    public static void forceMeasureRefresh(){
        getInstance().MeasureRefresh();                                              // 调用实例方法
    }

    /**
     * 强制刷新通道测量
     * 静态方法，供外部调用
     */
    public static void forceChMeasureRefresh(){
        getInstance().ChMeasureRefresh();                                            // 调用实例方法
    }

    /**
     * 强制刷新参考通道测量
     * 静态方法，供外部调用
     */
    public static void forceRefMeasureRefresh(){
        getInstance().RefMeasureRefresh();                                           // 调用实例方法
    }

    /**
     * 强制刷新数学通道测量
     * 静态方法，供外部调用
     */
    public static void forceMathMeasureRefresh(){
        getInstance().MathMeasureRefresh();                                          // 调用实例方法
    }

    /**
     * 强制立即测量指定通道
     * 静态方法，供外部调用
     *
     * @param chIdx 通道索引
     */
    public static void forceImmedMeasure(int chIdx){
        getInstance().immedMeasure(chIdx);                                           // 调用实例方法
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量范围设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置通道的测量范围
     *
     * @param channel 通道对象
     * @param begin 开始位置
     * @param end 结束位置
     */
    private static void setMeasureRange(IChannel channel,int begin,int end){
        Measure measure = channel.getMeasure();                                      // 获取测量对象
        if(measure != null){                                                          // 测量对象有效
            measure.MeasureBegin(begin);                                             // 设置开始位置
            measure.MeasureEnd(end);                                                 // 设置结束位置
            measure.setCursorMeasureRange(isCursorRang());                           // 设置光标测量范围
        }
    }

    /**
     * 设置测量范围
     * 静态方法，供外部调用
     *
     * @param begin 开始位置
     * @param end 结束位置
     */
    public static void setMeasureRange(int begin,int end){

        if(end < begin){                                                              // 结束位置小于开始位置
            int tmp = begin;                                                         // 交换位置
            begin = end;
            end = tmp;
        }
        if(!isCursorRang()){                                                         // 非光标范围模式
            begin = 0;                                                               // 使用全屏范围
            end = ScopeBase.getWidth()-1;
        }
        int finalBegin = begin;                                                     // 最终开始位置
        int finalEnd = end;                                                         // 最终结束位置

        // 设置所有通道的测量范围
        ChannelFactory.forEachRef(refChannel -> {                                    // 参考通道
            setMeasureRange(refChannel, finalBegin, finalEnd);
        });
        ChannelFactory.forEachCh(channel -> {                                        // 动态通道
            setMeasureRange(channel,finalBegin, finalEnd);
        });
        ChannelFactory.forEachMath(mathChannel -> {                                   // 数学通道
            setMeasureRange(mathChannel,finalBegin, finalEnd);
        });
        forceMeasureRefresh();                                                       // 刷新测量
    }

    /**
     * 设置光标范围模式
     *
     * @param b 是否启用光标范围
     */
    public static void setCursorRang(boolean b){
        synchronized (MeasureService.class) {                                         // 类级别同步
            bCursorRang = b;                                                         // 设置光标范围标志
            EventFactory.sendEvent(EventFactory.EVENT_MEASURE_RANGE,true);           // 发送测量范围事件
        }
    }

    /**
     * 检查是否使用光标范围
     *
     * @return true: 使用光标范围
     *         false: 使用全屏范围
     */
    public static boolean isCursorRang(){
        synchronized (MeasureService.class) {                                         // 类级别同步
            return bCursorRang;                                                      // 返回光标范围标志
        }
    }
}
