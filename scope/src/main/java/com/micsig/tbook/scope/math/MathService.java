package com.micsig.tbook.scope.math;

import android.os.SystemClock;
import android.util.Log;

import com.micsig.base.DoubleUtil;
import com.micsig.base.FilterThread;
import com.micsig.base.Logger;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Data.IDataBuffer;
import com.micsig.tbook.scope.Data.ImageParam;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.scope.surface.SlideFinger;
import com.micsig.tbook.scope.surface.SurfaceDataRecv;
import com.micsig.tbook.scope.surface.SurfaceNative;
import com.micsig.tbook.scope.surface.SurfacePreview;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                MathService - 数学运算服务类                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Math模块的数学运算服务类，位于math包下，                                    ║
 * ║   提供示波器数学运算的调度、计算和绘制功能。                                   ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理数学通道的计算线程                                                  ║
 * ║   2. 调度双通道运算、FFT变换、表达式运算                                     ║
 * ║   3. 管理数学波形的绘制                                                      ║
 * ║   4. 响应示波器事件触发数学刷新                                              ║
 * ║   5. 管理Surface生命周期                                                    ║
 * ║                                                                              ║
 * ║ 【数学运算类型】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        数学运算类型                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【双通道运算】MATH_DUALWAVE                                         │ ║
 * ║   │   - 加法：CH1 + CH2                                                  │ ║
 * ║   │   - 减法：CH1 - CH2                                                  │ ║
 * ║   │   - 乘法：CH1 × CH2                                                  │ ║
 * ║   │   - 除法：CH1 ÷ CH2                                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【FFT变换】MATH_FFTWAVE                                             │ ║
 * ║   │   - 时域信号转换为频域信号                                            │ ║
 * ║   │   - 支持RMS和dB显示                                                  │ ║
 * ║   │   - 支持多种窗函数                                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【表达式运算】MATH_EXPR                                              │ ║
 * ║   │   - 自定义数学表达式                                                  │ ║
 * ║   │   - 支持三角函数、对数等高级运算                                      │ ║
 * ║   │   - 支持用户自定义变量                                                │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【计算流程】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 获取源数据  │───▶│ 数学计算    │───▶│ 波形绘制    │                   ║
 * ║   │ obtain()    │    │ MathNative  │    │ drawSurface │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【线程模型】                                                                 ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        线程模型                                      │ ║
 * ║   │                                                                     │ ║
 * ║   │   FilterThread (主线程)                                             │ ║
 * ║   │   └── Runnable (计算任务)                                           │ ║
 * ║   │       └── fixedThreadPool (线程池)                                  │ ║
 * ║   │           ├── MathChannel1 计算任务                                 │ ║
 * ║   │           ├── MathChannel2 计算任务                                 │ ║
 * ║   │           ├── MathChannel3 计算任务                                 │ ║
 * ║   │           └── MathChannel4 计算任务                                 │ ║
 * ║   └─────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【事件监听】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        事件监听列表                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   EVENT_MATH_VPOS          数学通道垂直位置变化                     │ ║
 * ║   │   EVENT_CH_WAVE_UPDATE      通道波形更新                             │ ║
 * ║   │   EVENT_SURFACE_CREATED     Surface创建                              │ ║
 * ║   │   EVENT_SURFACE_DESTROYED   Surface销毁                              │ ║
 * ║   │   EVENT_MATH_REFRESH        数学刷新请求                             │ ║
 * ║   │   EVENT_DISPLAY_CCT         显示CCT变化                              │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 实时数学运算：通道加减乘除、FFT变换                                    ║
 * ║   2. 自定义表达式：用户输入的数学表达式计算                                 ║
 * ║   3. 波形显示：数学运算结果的波形绘制                                       ║
 * ║   4. 光标测量：FFT频谱的光标测量                                           ║
 * ║                                                                              ║
 * ║ 【单例模式】                                                                 ║
 * ║   - 双重检查锁定单例模式                                                    ║
 * ║   - 延迟初始化，线程安全                                                    ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 使用synchronized保护Surface操作                                        ║
 * ║   - 使用线程池并行计算                                                      ║
 * ║   - 使用volatile保证可见性                                                  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - FilterThread: 过滤线程基类                                             ║
 * ║   - MathChannel: 数学通道类                                                ║
 * ║   - MathNative: 数学运算Native方法                                         ║
 * ║   - MathDualWave: 双通道数学运算类                                         ║
 * ║   - MathFFTWave: FFT频谱分析类                                             ║
 * ║   - MathExprWave: 数学表达式类                                             ║
 * ║   - ChannelFactory: 通道工厂                                               ║
 * ║   - EventFactory: 事件工厂                                                 ║
 * ║   - SurfaceNative: Surface绘制类                                           ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 数学运算服务类
 * 继承自FilterThread，实现Observer接口
 * 负责管理和调度示波器的数学运算功能
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>数学计算：双通道运算、FFT变换、表达式运算</li>
 *   <li>波形绘制：数学运算结果的波形绘制</li>
 *   <li>事件响应：响应示波器事件触发数学刷新</li>
 *   <li>Surface管理：管理数学通道的Surface生命周期</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 获取MathService实例
 * MathService mathService = MathService.getInstance();
 *
 * // 刷新数学运算
 * MathService.forceMathRefresh();
 * </pre>
 *
 * @see FilterThread
 * @see MathChannel
 * @see MathNative
 * @see MathDualWave
 * @see MathFFTWave
 * @see MathExprWave
 */
public class MathService extends FilterThread implements Observer {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签 */
    private static final String TAG = "MathService";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例实例
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 单例实例，使用volatile保证可见性 */
    private static volatile MathService instance = null;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 数学通道图像参数数组 */
    private ImageParam [] mathParam = new ImageParam[ChannelFactory.MATH_CNT];

    /** 数学通道Surface绘制对象数组 */
    private SurfaceNative [] surfaceNative = new SurfaceNative[ChannelFactory.MATH_CNT];

    /** 固定大小线程池，用于并行计算多个数学通道 */
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(ChannelFactory.MATH_CNT);

    /** Surface操作锁数组，每个数学通道一个锁 */
    private final Object []surfaceLock = new Object[ChannelFactory.MATH_CNT];

    /** 当前计算的数学类型数组，用于检测数学类型变化 */
    private final int [] calcMathType = new int[ChannelFactory.MATH_CNT];

    /** 当前采样率，用于检测采样率变化 */
    private double sampleRate = 0;

    /** 表达式字符串数组，用于检测表达式变化 */
    private  String [] exprString = { ")",")",")",")",")",")",")",")",")",")"};

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取MathService单例实例
     * 使用双重检查锁定模式，线程安全且高效
     *
     * @return MathService实例
     */
    public static MathService getInstance() {
        if (instance == null) {                                                        // 第一次检查，避免不必要同步
            synchronized (MathService.class) {                                         // 同步块
                if (instance == null) {                                                // 第二次检查
                    instance = new MathService();                                      // 创建实例
                }
            }
        }
        return instance;                                                               // 返回实例
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 私有构造函数
     * 初始化数学服务，创建线程池，注册事件监听器
     */
    private MathService() {
        super(TAG);                                                                    // 调用父类构造函数，设置线程名
        for(int i=0;i<surfaceLock.length;i++){                                         // 初始化Surface锁
            surfaceLock[i] = new Object();                                             // 创建锁对象
        }
        InitMathParam();                                                               // 初始化数学参数
        this.setRunnable(new Runnable() {                                              // 设置计算任务
            @Override
            public void run() {                                                        // 计算任务入口
                List<Callable<Boolean>> list = new ArrayList<>();                      // 创建任务列表
                ChannelFactory.forEachMath(mathChannel -> {                            // 遍历所有数学通道
                    list.add(()->{                                                     // 添加计算任务
                        if(mathChannel.isOpen()) {                                     // 数学通道已打开
                            if (MathCalc(mathChannel)) {                               // 数学计算成功
                                drawMath(mathChannel);                                 // 绘制数学波形
                            } else {                                                   // 数学计算失败
                                if(!SlideFinger.getInstance().isSlide()) {             // 非滑动状态
                                    clearMath(mathChannel);                            // 清除数学波形
                                }
                            }
                        }
                        return true;                                                   // 返回成功
                    });
                });
                try {
                    List<Future<Boolean>>futures = fixedThreadPool.invokeAll(list);    // 并行执行所有任务
//                    futures.forEach( future->{
//                    });
                } catch (InterruptedException e) {                                     // 线程中断异常
                    e.printStackTrace();                                               // 打印异常堆栈
                }
            }
        });
        setBeforeRun(false);                                                           // 设置运行前不等待
        this.setDelayMillis(40);                                                       // 设置40ms延迟

        // 注册事件监听器
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_VPOS,this);              // 数学通道垂直位置变化
        EventFactory.addEventObserver(EventFactory.EVENT_CH_WAVE_UPDATE,this);         // 通道波形更新
        EventFactory.addEventObserver(EventFactory.EVENT_SURFACE_CREATED,this);        // Surface创建
        EventFactory.addEventObserver(EventFactory.EVENT_SURFACE_DESTROYED,this);      // Surface销毁
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_REFRESH,this);           // 数学刷新请求
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_CCT,this);            // 显示CCT变化
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 初始化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化数学参数
     * 为每个数学通道创建ImageParam对象并设置默认参数
     */
    private void InitMathParam(){
        for(int i=0;i<mathParam.length;i++) {                                          // 遍历所有数学通道
            mathParam[i] = new ImageParam();                                           // 创建ImageParam对象
            mathParam[i].setPerPixelByte(4);                                           // 设置每像素字节数为4（ARGB）
            mathParam[i].setBackgroundColor(0);                                        // 设置背景色为透明
            mathParam[i].setForegroundColor(0xFF0000FF);                               // 设置前景色为蓝色
            mathParam[i].setWidth(ScopeBase.getWidth());                               // 设置宽度为屏幕宽度
            mathParam[i].setHeight(ScopeBase.getHeight());                             // 设置高度为屏幕高度
            mathParam[i].setXOffset(0);                                                // 设置X偏移为0
            mathParam[i].setYOffset(0);                                                // 设置Y偏移为0
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 波形绘制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制数学波形
     * 根据数学通道类型和参数绘制波形
     *
     * @param mathChannel 数学通道对象
     */
    private void drawMath(MathChannel mathChannel){
        if(mathChannel.isOpen()){                                                      // 数学通道已打开
            int idx = mathChannel.getChId() - ChannelFactory.MATH1;                    // 计算索引
            ImageParam imageParam = mathParam[idx];                                    // 获取图像参数
            imageParam.setForegroundColor(mathChannel.getForegroundColor());           // 设置前景色
            imageParam.setVerticalPerPix(ScopeBase.changeAccuracy(mathChannel.getADVerticalPerPix()  * ScopeBase.getToUICoff())); // 设置垂直每像素值
            // horizontal - 水平轴参数设置
            if(mathChannel.getMathType() == MathWave.MATH_FFTWAVE) {                   // FFT类型
                //转化为绝对坐标
                HorizontalAxisMath horizontalAxisFFT=mathChannel.getHorizontalAxisMathFFT(); // 获取FFT水平轴
                imageParam.setXOffset((int)(-horizontalAxisFFT.getXPosOfView()));      // 设置X偏移
                imageParam.setTimeScaleVal(horizontalAxisFFT.fftXScaleIdVal());        // 设置时基值

            } else {                                                                   // 非FFT类型
                imageParam.setXOffset(0);                                              // 设置X偏移为0
                imageParam.setTimeScaleVal(HorizontalAxis.getInstance().getTimeScaleIdVal()); // 设置时基值
            }
            // vertical - 垂直轴参数设置
            if(Scope.getInstance().isZoom()) {                                         // 缩放模式
                imageParam.setYOffset((int) Math.round(mathChannel.getPos() * ScopeBase.getZoomHeight()/ScopeBase.getHeight())); // 设置Y偏移
                imageParam.setHeight(ScopeBase.getZoomHeight());                       // 设置高度为缩放高度
            } else {                                                                   // 正常模式
                imageParam.setYOffset((int) Math.round(mathChannel.getPos()));         // 设置Y偏移
                imageParam.setHeight(ScopeBase.getHeight());                           // 设置高度为屏幕高度
            }
            IDataBuffer dataBuffer = mathChannel.obtain();                             // 获取数据缓冲区
            if(dataBuffer != null) {                                                   // 数据缓冲区有效
                boolean bCursorEnable = false;                                         // 光标启用标志
                if (mathChannel.getMathType() == MathWave.MATH_FFTWAVE) {              // FFT类型
                    WaveData wd = (WaveData) dataBuffer;                               // 转换为WaveData
                    int len = wd.getWaveLength();                                      // 获取波形长度

                    wd.setTimeScaleVal(wd.getSampRate() * (len - 1) / 2 / len / ScopeBase.getHorizonGridCnt()); // 设置时基值

                    wd.setXPos(0);                                                     // 设置X位置为0
                    wd.setStartX(ScopeBase.getWidth() / 2);//归零参数，方便计算        // 设置起始X为屏幕中心
                    wd.setEndX(ScopeBase.getWidth() / 2 + ScopeBase.getWidth() - 1);//头尾长度控制在标准屏长度width // 设置结束X

                    MathFFTWave mathFFTWave = mathChannel.getMathFFTWave();             // 获取FFT对象
                    if(mathFFTWave.getFFTType() == MathFFTWave.FFT_TYPE_DB){           // dB类型
                        imageParam.setVerticalPerPix(ScopeBase.changeAccuracy(wd.getVScaleVal()*mathChannel.getADVerticalPerPix() * ScopeBase.getToUICoff()*1e4)); // 设置垂直每像素值
                    }
                    Measure measure = mathChannel.getMeasure();                        // 获取测量对象
                    if(measure.isMeasureItemEnable(Measure.MeasureType.MEASURE_CURSOR_X1) // 光标X1启用
                            ||measure.isMeasureItemEnable(Measure.MeasureType.MEASURE_CURSOR_X2)) { // 光标X2启用
                        imageParam.setCursor(measure.getCursorX1(), measure.getCursorX2()); // 设置光标位置
                        bCursorEnable = true;                                          // 设置光标启用标志
                    }
                }
                synchronized (surfaceLock[idx]) {                                      // 同步保护Surface操作

                    if (surfaceNative[idx] != null) {                                  // Surface有效
                        if(mathChannel.getMathType() == getCalcMathType(mathChannel.getChId() - ChannelFactory.MATH1)) { // 数学类型匹配
                            if(!SlideFinger.getInstance().isSlide()) {                 // 非滑动状态
                                imageParam.setStartX(((WaveData)dataBuffer).getStartX()); // 设置起始X
                                imageParam.setEndX(((WaveData)dataBuffer).getEndX()); // 设置结束X
                                surfaceNative[idx].drawSurface(dataBuffer.getByteBuffer(), imageParam.getDirectBuffer()); // 绘制波形
                                if(bCursorEnable){                                     // 光标启用
                                    mathChannel.setCursorValue(imageParam.getCursorX1Value(),imageParam.getCursorX2Value()); // 设置光标值
                                    mathChannel.setCursorValid(imageParam.isCursorX1Valid(), imageParam.isCursorX2Valid()); // 设置光标有效状态
                                    if(ChannelFactory.getChActivate() == mathChannel.getChId()) { // 当前通道激活
                                        EventFactory.sendEvent(EventFactory.EVENT_MATH_MEASURE_UPDATE); // 发送数学测量更新事件
                                    }
                                }else{                                                 // 光标未启用
                                    mathChannel.setCursorValid(false,false);           // 设置光标无效
                                }
                            }
                        }
                    }
                }
                mathChannel.recycle(dataBuffer);                                       // 回收数据缓冲区
            }
        }
    }

    /**
     * 清除数学波形
     * 清除指定数学通道的波形显示
     *
     * @param mathChannel 数学通道对象
     */
    public void clearMath(MathChannel mathChannel) {
        if (mathChannel.isOpen()                                                       // 数学通道已打开
                && !Scope.getInstance().isRun()) {                                     // 示波器非运行状态
            int idx = mathChannel.getChId() - ChannelFactory.MATH1;                    // 计算索引
            synchronized (surfaceLock[idx]){                                           // 同步保护Surface操作
                if(surfaceNative[idx] != null){                                        // Surface有效
                    surfaceNative[idx].clearSurface();                                 // 清除Surface
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数学类型管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置当前计算的数学类型
     *
     * @param idx 数学通道索引
     * @param mathType 数学类型
     */
    private void setCalcMathType(int idx,int mathType){
        this.calcMathType[idx] = mathType;                                             // 设置数学类型
    }

    /**
     * 获取当前计算的数学类型
     *
     * @param idx 数学通道索引
     * @return 数学类型
     */
    private int getCalcMathType(int idx){
        return this.calcMathType[idx];                                                 // 返回数学类型
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数学计算主方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行数学计算
     * 根据数学通道类型调用相应的计算方法
     *
     * @param mathChannel 数学通道对象
     * @return true: 计算成功
     *         false: 计算失败
     */
    private boolean MathCalc(MathChannel mathChannel){
        boolean bMathCalc = false;                                                     // 初始化计算结果
        boolean bSlide = SlideFinger.getInstance().isSlide();                          // 获取滑动状态
        int idx = mathChannel.getChId() - ChannelFactory.MATH1;                        // 计算索引
        if(mathChannel.isOpen() && !bSlide){                                           // 数学通道打开且非滑动状态

            // 检测数学类型或采样率变化
            if(mathChannel.getMathType() != getCalcMathType(idx)                       // 数学类型变化
                    || !DoubleUtil.FuzzyCompare(sampleRate , mathChannel.getSampleRate())){; // 采样率变化
                EventFactory.sendEvent(new EventBase(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR),true,500); // 发送余辉清除事件
                setCalcMathType(idx,mathChannel.getMathType());                        // 更新数学类型
                sampleRate = mathChannel.getSampleRate();                              // 更新采样率
            }

            // 根据数学类型执行相应计算
            switch (getCalcMathType(idx)){                                             // 根据数学类型
                case MathWave.MATH_DUALWAVE:                                           // 双通道运算
                    bMathCalc = MathDualWaveCalc(mathChannel);                         // 执行双通道计算
                    break;
                case MathWave.MATH_FFTWAVE:                                            // FFT变换
                    bMathCalc = MathFFTWaveCalc(mathChannel);                          // 执行FFT计算
                    break;
                case MathWave.MATH_EXPR:                                               // 表达式运算
                    bMathCalc = MathExprWaveCalc(mathChannel);                         // 执行表达式计算
                    break;
            }
            // 检查数学类型是否在计算过程中发生变化
            if(getCalcMathType(idx) != mathChannel.getMathType()){                     // 数学类型变化
                bMathCalc = false;                                                     // 设置计算失败
            }
        }
        if(bMathCalc){                                                                 // 计算成功
            mathChannel.setWaveValid(true);                                            // 设置波形有效
            EventFactory.sendEvent(new EventBase(EventFactory.EVENT_MATH_WAVE_UPDATE, mathChannel.getChId())); // 发送数学波形更新事件
        }
        return bMathCalc;                                                              // 返回计算结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 通道数据序列化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 序列化通道数据（单源）
     * 从通道数据数组中选择第一个有效的源进行序列化
     *
     * @param mathChannel 数学通道对象
     * @param dstWave 目标波形数据
     * @param ch 通道数据数组
     */
    private void serialzationChannel(MathChannel mathChannel,WaveData dstWave,
                                     IDataBuffer [] ch){
        IDataBuffer src = null;                                                        // 源数据缓冲区
        for(int i=0;i<ch.length;i++){                                                  // 遍历通道数据数组
            if(ch[i] != null){                                                         // 数据有效
                src = ch[i];                                                           // 保存源数据
                break;                                                                 // 跳出循环
            }
        }
        if(src != null) {                                                              // 源数据有效
            serialzationChannel(mathChannel,dstWave, (WaveData) src, (WaveData) src);  // 调用双源序列化方法
        }
    }

    /**
     * 序列化通道数据（双源）
     * 将源通道数据参数复制到目标波形数据
     *
     * @param mathChannel 数学通道对象
     * @param dstWave 目标波形数据
     * @param src1 源波形数据1
     * @param src2 源波形数据2
     */
    private void serialzationChannel(MathChannel mathChannel,WaveData dstWave, WaveData src1, WaveData src2) {

        WaveData src = src1;                                                           // 选择源1
        if (src.getWaveLength() > src2.getWaveLength())                                // 源2长度更小
            src = src2;                                                                // 选择源2
        dstWave.setTimeScaleVal(src.getTimeScaleVal());                                // 复制时基值
        dstWave.setXPos(src.getXPos());                                                // 复制X位置
        dstWave.setYPosition(mathChannel.getPos());                                    // 设置Y位置
        dstWave.setStartX(src.getStartX());                                            // 复制起始X
        dstWave.setEndX(src.getEndX());                                                // 复制结束X
        mathChannel.setWaveLen(src.getWaveLength());                                   // 设置波形长度
        double _vScale = src.getVScaleVal();                                           // 获取垂直档位值
        double vPrePix = ScopeBase.changeAccuracy(mathChannel.getADVerticalPerPix() * ScopeBase.getToUICoff()); // 计算垂直每像素值
        double wavFactor = HwConfig.getInstance().getWavFactor();                      // 获取波形因子
        int mathType = mathChannel.getMathType();                                      // 获取数学类型
        int waveType = WaveData.DYNAMIC_WAVE;                                          // 默认波形类型为动态波形
        switch (mathType){                                                             // 根据数学类型
            case MathWave.MATH_DUALWAVE:                                               // 双通道运算
            case MathWave.MATH_EXPR:                                                   // 表达式运算
                _vScale = mathChannel.getVScaleVal();                                  // 使用数学通道的垂直档位值
                vPrePix *= wavFactor;                                                  // 应用波形因子
                break;
            case MathWave.MATH_FFTWAVE:                                                // FFT变换
                if(mathChannel.getMathFFTWave().getFFTType() == MathFFTWave.FFT_TYPE_DB){ // dB类型
                    vPrePix = _vScale = mathChannel.getVScaleVal();                    // 使用数学通道的垂直档位值
                }else {                                                                // RMS类型
                    vPrePix = src.getVerticalPerPix();                                  // 使用源通道的垂直每像素值
                }
                waveType = WaveData.FFT_WAVE;                                          // 设置波形类型为FFT波形
                break;
        }

        dstWave.setVScaleVal(_vScale);                                                 // 设置垂直档位值
        dstWave.setVerticalPerPix(vPrePix);                                            // 设置垂直每像素值
        dstWave.setChIdx(mathChannel.getChId());                                       // 设置通道索引
        dstWave.setSampRate(src.getSampRate());                                        // 设置采样率
        dstWave.setSampRate2display(src.getSampRate2display());                        // 设置显示采样率
        dstWave.setWaveType(waveType);                                                 // 设置波形类型
        dstWave.setProbeType(mathChannel.getProbeType());                              // 设置探头类型
        dstWave.setProbeStr(mathChannel.getProbeStr());                                // 设置探头字符串
        dstWave.setProbeRate(1.0);                                                     // 设置探头比率为1.0
        dstWave.setBinType( WaveData.WAVE_BIN);                                        // 设置二进制类型
        dstWave.setPlaceVal(0);                                                        // 设置位置值为0
        dstWave.setSegmentNums(1);                                                     // 设置段数为1
        dstWave.setSegmentLen(dstWave.getWaveLength());                                // 设置段长度
        dstWave.setVerticalPerGridPixels(ScopeBase.getVerticalPerGridPixels());        // 设置每格垂直像素数
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 双通道运算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行双通道数学运算
     * 从源通道获取数据，执行运算，存储结果
     *
     * @param mathChannel 数学通道对象
     * @return true: 计算成功
     *         false: 计算失败
     */
    private boolean MathDualWaveCalc(MathChannel mathChannel ){
        boolean bMathCalc = false;                                                     // 初始化计算结果
        Scope scope = Scope.getInstance();                                             // 获取Scope实例
        MathDualWave mathDualWave = mathChannel.getMathDualWave();                     // 获取双通道运算对象
        IChannel src1 = ChannelFactory.getDynamicChannel(mathDualWave.getSource1());   // 获取源通道1
        IChannel src2 = ChannelFactory.getDynamicChannel(mathDualWave.getSource2());   // 获取源通道2

        if(src1 != null &&  src2 != null){                                             // 两个源通道都有效
            if(scope.isChannelInSample(src1.getChId())                                 // 源通道1正在采样
                    && scope.isChannelInSample(src2.getChId())) {                      // 源通道2正在采样
                IDataBuffer dstBuffer = mathChannel.dequeue();                         // 获取目标缓冲区
                if (dstBuffer != null) {                                               // 目标缓冲区有效
                    IDataBuffer srcBuffer1 = src1.obtain();                            // 获取源通道1数据
                    if (srcBuffer1 != null) {                                          // 源数据1有效
                        IDataBuffer srcBuffer2 = src2.obtain();                        // 获取源通道2数据
                        if (srcBuffer2 != null) {                                      // 源数据2有效
                            serialzationChannel(mathChannel,(WaveData) dstBuffer, (WaveData) srcBuffer1, (WaveData) srcBuffer2); // 序列化通道数据
                            bMathCalc = MathNative.CalcDual(mathDualWave.getOperator(), // 执行双通道运算
                                    dstBuffer.getByteBuffer(),
                                    srcBuffer1.getByteBuffer(),
                                    srcBuffer2.getByteBuffer());
                            src2.recycle(srcBuffer2);                                  // 回收源数据2
                        }
                        src1.recycle(srcBuffer1);                                      // 回收源数据1
                    }
                    mathChannel.enqueue(dstBuffer);                                    // 入队目标缓冲区
                }
            }
        }
        return bMathCalc;                                                              // 返回计算结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // FFT变换方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行FFT变换
     * 从源通道获取数据，执行FFT变换，存储结果
     *
     * @param mathChannel 数学通道对象
     * @return true: 计算成功
     *         false: 计算失败
     */
    private boolean MathFFTWaveCalc(MathChannel mathChannel ){
        int mathIdx = mathChannel.getMathChIdx();                                      // 获取数学通道索引
        boolean bMathCalc = false;                                                     // 初始化计算结果
        Scope scope = Scope.getInstance();                                             // 获取Scope实例
        MathFFTWave mathFFTWave = mathChannel.getMathFFTWave();                        // 获取FFT对象
        IChannel src = ChannelFactory.getDynamicChannel(mathFFTWave.getSource());      // 获取源通道
        if(src != null                                                                 // 源通道有效
                && scope.isChannelInSample(src.getChId())){                            // 源通道正在采样
            IDataBuffer dstBuffer = mathChannel.dequeue();                             // 获取目标缓冲区

            if(dstBuffer != null ){                                                    // 目标缓冲区有效
                WaveData srcBuffer = (WaveData) src.obtain();                          // 获取源数据
                if(srcBuffer != null){                                                 // 源数据有效
                    MathNative.setVAd(mathIdx,srcBuffer.getVerticalPerPix());          // 设置垂直每像素值
                    bMathCalc = MathNative.CalcFFT(mathIdx,mathFFTWave.getFFTType(),   // 执行FFT变换
                            mathFFTWave.getFFTWindow(),
                            dstBuffer.getByteBuffer(),
                            srcBuffer.getByteBuffer());
                    //放CalcFFT后面 - 在FFT计算后序列化通道数据
                    serialzationChannel(mathChannel,(WaveData)dstBuffer,(WaveData)srcBuffer,(WaveData)srcBuffer); // 序列化通道数据
                    WaveData waveData = (WaveData)srcBuffer;                           // 获取波形数据
                    mathChannel.setFFTDCVal(mathFFTWave.getFFTDCVal((double)waveData.getVerticalPerPix())); // 设置DC值
                    mathChannel.setFFTMaxVal(mathFFTWave.getFFTMaxVal((double)waveData.getVerticalPerPix())); // 设置最大值
                    mathChannel.setFFTMaxFreq((double)(mathFFTWave.getFFTMaxIdx() *    // 设置最大值频率
                            mathChannel.getSampleRate()/MathNative.CalFFTPointNum(waveData.getWaveLength())));
                    src.recycle(srcBuffer);                                            // 回收源数据
                }
                mathChannel.enqueue(dstBuffer);                                        // 入队目标缓冲区
            }
        }
        return bMathCalc;                                                              // 返回计算结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 表达式运算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行数学表达式运算
     * 从多个源通道获取数据，执行表达式运算，存储结果
     *
     * @param mathChannel 数学通道对象
     * @return true: 计算成功
     *         false: 计算失败
     */
    private boolean MathExprWaveCalc(MathChannel mathChannel){
        int mathIdx = mathChannel.getMathChIdx();                                      // 获取数学通道索引
        boolean bMathCalc = false;                                                     // 初始化计算结果
        boolean bExprChange = false;                                                   // 表达式变化标志
        Scope scope = Scope.getInstance();                                             // 获取Scope实例
        MathExprWave mathExprWave = mathChannel.getMathExprWave();                     // 获取表达式对象
        String exprString = mathExprWave.getExprString();                              // 获取表达式字符串
        bExprChange = mathExprWave.isExprChange();                                     // 获取表达式变化标志
        if(!this.exprString[mathIdx].equals(exprString)){                              // 表达式字符串变化

            if(MathNative.setCalcExpr(mathIdx,exprString)){                            // 设置表达式成功
                this.exprString[mathIdx] = exprString;                                 // 更新表达式字符串
                bExprChange = true;                                                    // 设置表达式变化标志
            }else{                                                                     // 设置表达式失败
                return false;                                                          // 返回失败
            }
        }


        Channel [] chArray = new Channel[ChannelFactory.CH_CNT];                       // 通道数组
        IDataBuffer[] chBuffer = new IDataBuffer[ChannelFactory.CH_CNT];               // 数据缓冲区数组
        ByteBuffer[] chBufArray = new ByteBuffer[ChannelFactory.CH_CNT];               // ByteBuffer数组
        boolean bSample = false;                                                       // 采样标志
        int maxIdx = ChannelFactory.getMaxChIdx();                                     // 获取最大通道索引
        for(int i = ChannelFactory.CH1;i < maxIdx;i++){                                // 遍历所有动态通道
            chArray[i] = null;                                                         // 初始化为null
            chBuffer[i] = null;                                                        // 初始化为null
            chBufArray[i] = null;                                                      // 初始化为null
            if(scope.isChannelInSample(i)){                                            // 通道正在采样
                chArray[i] = ChannelFactory.getDynamicChannel(i);                      // 获取通道对象
                chBuffer[i] = chArray[i].obtain();                                     // 获取数据缓冲区
                if(chBuffer[i] != null)                                                // 数据有效
                    bSample = true;                                                    // 设置采样标志
                chBufArray[i] = chBuffer[i] == null ? null : chBuffer[i].getByteBuffer(); // 获取ByteBuffer
            }
        }

        double t = HorizontalAxis.getInstance().getTimeScaleIdVal();                   // 获取时基值

        if(bSample){                                                                   // 有通道正在采样
            double val = 0;                                                            // 计算结果值
            IDataBuffer dstBuffer = mathChannel.dequeue();                             // 获取目标缓冲区
            if(dstBuffer != null) {                                                    // 目标缓冲区有效
                serialzationChannel(mathChannel,(WaveData) dstBuffer, chBuffer);       // 序列化通道数据
                val = MathNative.CalcExpr(mathIdx,dstBuffer.getByteBuffer(),           // 执行表达式计算
                        chBufArray, t,
                        mathExprWave.getVar1(), mathExprWave.getVar2()
                );
                mathChannel.enqueue(dstBuffer);                                        // 入队目标缓冲区
            }
            bMathCalc = true;                                                          // 设置计算成功

            if(bExprChange){                                                           // 表达式变化
                mathChannel.setMaxVal(val);                                            // 设置最大值
                mathExprWave.setExprChange(false);                                     // 清除表达式变化标志
            }
        }

        // 回收所有源通道数据
        for(int i = ChannelFactory.CH1;i<maxIdx;i++){                                  // 遍历所有动态通道
            if(chBuffer[i] != null){                                                   // 数据有效
                chArray[i].recycle(chBuffer[i]);                                       // 回收数据
                chBuffer[i] = null;                                                    // 设置为null
            }
        }
        return bMathCalc;                                                              // 返回计算结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数学刷新方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 刷新数学运算
     * 触发数学计算线程运行
     */
    public void MathRefresh(){
        if(ChannelFactory.isMathEnable()){                                             // 数学功能启用
            run();                                                                     // 运行计算线程
        }
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
        EventBase eventBase = (EventBase)data;                                         // 转换为事件基类
        switch (eventBase.getId()){                                                    // 根据事件ID处理
            case EventFactory.EVENT_MATH_VPOS:                                         // 数学通道垂直位置变化
            case EventFactory.EVENT_CH_WAVE_UPDATE:                                    // 通道波形更新
            case EventFactory.EVENT_MATH_REFRESH:                                      // 数学刷新请求
            case EventFactory.EVENT_DISPLAY_CCT:                                       // 显示CCT变化
                if(!SlideFinger.getInstance().isSlide()) {                             // 非滑动状态
                    MathRefresh();                                                     // 刷新数学运算
                }
                break;
            case EventFactory.EVENT_SURFACE_CREATED: {                                 // Surface创建事件
                SurfaceDataRecv surface = (SurfaceDataRecv) eventBase.getData();       // 获取Surface数据
                if (surface != null) {                                                 // Surface有效

                        ChannelFactory.forEachMath(mathChannel -> {                    // 遍历所有数学通道
                            int idx = mathChannel.getChId() - ChannelFactory.MATH1;    // 计算索引
                            synchronized (surfaceLock[idx]) {                          // 同步保护
                                surfaceNative[idx] = surface.getSurfaceNative(SurfacePreview.LAYER_MATH1 + idx); // 获取Surface
                                surfaceNative[idx].acquireSurface(ScopeBase.getWidth(), ScopeBase.getHeight()); // 获取Surface资源
                                surfaceNative[idx].clearSurface();                     // 清除Surface
                            }
                        });


                }
            }
                break;
            case EventFactory.EVENT_SURFACE_DESTROYED: {                               // Surface销毁事件
                SurfaceDataRecv surface = (SurfaceDataRecv) eventBase.getData();       // 获取Surface数据
                if (surface != null) {                                                 // Surface有效
                    ChannelFactory.forEachMath(mathChannel -> {                        // 遍历所有数学通道
                        int idx = mathChannel.getChId() - ChannelFactory.MATH1;        // 计算索引
                        synchronized (surfaceLock[idx]) {                              // 同步保护
                            if (surfaceNative[idx] != null) {                          // Surface有效
                                surfaceNative[idx].releaseSurface();                   // 释放Surface资源
                                surfaceNative[idx] = null;                             // 设置为null
                            }
                        }
                    });
                }
            }
                break;
        }

    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 静态刷新方法（对外接口）
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 强制刷新数学运算
     * 静态方法，供外部调用
     */
    public static void forceMathRefresh(){
        getInstance().MathRefresh();                                                   // 调用实例方法
    }
}
