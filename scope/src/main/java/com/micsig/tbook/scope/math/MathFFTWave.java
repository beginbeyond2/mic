package com.micsig.tbook.scope.math;


import androidx.annotation.IntDef;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.vertical.VerticalAxisMathFft;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                MathFFTWave - FFT频谱分析波形类                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Math模块的FFT频谱分析波形类，位于math包下，                                 ║
 * ║   提供示波器的快速傅里叶变换（FFT）频谱分析功能。                              ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 执行FFT变换，将时域信号转换为频域信号                                   ║
 * ║   2. 管理FFT显示类型（RMS有效值、dB分贝）                                    ║
 * ║   3. 管理FFT窗函数（矩形窗、汉明窗等）                                       ║
 * ║   4. 管理FFT源通道                                                          ║
 * ║   5. 提供FFT结果访问接口（DC值、最大值、最大值索引）                         ║
 * ║                                                                              ║
 * ║ 【FFT类型】                                                                  ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        FFT类型说明                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【RMS有效值】FFT_TYPE_RMS = 0                                       │ ║
 * ║   │   - 显示频谱的有效值（RMS）                                           │ ║
 * ║   │   - 单位：V（电压）或 A（电流）                                       │ ║
 * ║   │   - 适用于观察频谱幅度的绝对值                                        │ ║
 * ║   │                                                                      │ ║
 * ║   │   【dB分贝】FFT_TYPE_DB = 1                                           │ ║
 * ║   │   - 显示频谱的分贝值（dBV）                                           │ ║
 * ║   │   - 单位：dB                                                         │ ║
 * ║   │   - 适用于观察频谱的相对幅度                                          │ ║
 * ║   │   - 计算公式：dB = 20 × log10(V/Vref)                                │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【FFT窗函数】                                                                ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        FFT窗函数说明                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【矩形窗】FFT_WINDOW_RECTANGLE = 0                                  │ ║
 * ║   │   - 也称为均匀窗或Dirichlet窗                                         │ ║
 * ║   │   - 特点：主瓣窄，旁瓣高                                              │ ║
 * ║   │   - 适用：瞬态信号、周期信号                                          │ ║
 * ║   │                                                                      │ ║
 * ║   │   【汉明窗】FFT_WINDOW_HAMMING = 1                                    │ ║
 * ║   │   - 特点：主瓣较宽，旁瓣较低                                          │ ║
 * ║   │   - 适用：一般用途的频谱分析                                          │ ║
 * ║   │                                                                      │ ║
 * ║   │   【布莱克曼窗】FFT_WINDOW_BLACKMAN = 2                                │ ║
 * ║   │   - 特点：主瓣最宽，旁瓣最低                                          │ ║
 * ║   │   - 适用：需要高动态范围的频谱分析                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【汉宁窗】FFT_WINDOW_HANNING = 3                                    │ ║
 * ║   │   - 也称为Hann窗或升余弦窗                                            │ ║
 * ║   │   - 特点：主瓣宽度适中，旁瓣衰减快                                    │ ║
 * ║   │   - 适用：大多数频谱分析应用                                          │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【数据流】                                                                   ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 设置源通道  │───▶│ FFT变换     │───▶│ 频谱显示    │                   ║
 * ║   │ setSource   │    │ MathNative  │    │ RMS/dB      │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 频谱分析：分析信号的频率成分                                           ║
 * ║   2. 谐波分析：检测信号的谐波分量                                           ║
 * ║   3. 噪声分析：分析信号的噪声频谱                                           ║
 * ║   4. 失真分析：检测信号的失真情况                                           ║
 * ║   5. 调制分析：分析调制信号的频谱                                           ║
 * ║                                                                              ║
 * ║ 【继承关系】                                                                 ║
 * ║   MathWave (数学运算基类)                                                   ║
 * ║      └── MathFFTWave (FFT频谱分析波形类)                                    ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - MathWave: 数学运算基类                                                 ║
 * ║   - MathChannel: 数学通道类                                                ║
 * ║   - MathNative: 数学运算Native方法                                         ║
 * ║   - VerticalAxisMathFft: FFT垂直轴管理类                                   ║
 * ║   - ChannelFactory: 通道工厂                                               ║
 * ║   - EventFactory: 事件工厂                                                 ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * FFT频谱分析波形类
 * 继承自MathWave，提供快速傅里叶变换（FFT）频谱分析功能
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>FFT变换：将时域信号转换为频域信号</li>
 *   <li>类型管理：设置和获取FFT显示类型（RMS/dB）</li>
 *   <li>窗函数管理：设置和获取FFT窗函数类型</li>
 *   <li>源通道管理：设置和获取FFT源通道</li>
 *   <li>结果访问：获取DC值、最大值、最大值索引</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 创建FFT波形对象
 * MathFFTWave fftWave = new MathFFTWave(mathChannel);
 *
 * // 设置源通道
 * fftWave.setSource(ChannelFactory.CH1);
 *
 * // 设置FFT类型为dB
 * fftWave.setFFTType(MathFFTWave.FFT_TYPE_DB);
 *
 * // 设置窗函数为汉明窗
 * fftWave.setFFTWindow(MathFFTWave.FFT_WINDOW_HAMMING);
 *
 * // 获取FFT最大值
 * double maxVal = fftWave.getFFTMaxVal(1.0);
 * </pre>
 *
 * @see MathWave
 * @see MathChannel
 * @see MathNative
 * @see VerticalAxisMathFft
 */
public class MathFFTWave extends MathWave{

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签 */
    public static final String TAG = "MathFFTWave";

    // ═══════════════════════════════════════════════════════════════════════════════
    // FFT类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * FFT类型注解
     * 用于限制参数只能是定义的FFT类型常量
     */
    @IntDef({FFT_TYPE_RMS,FFT_TYPE_DB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FFT_TYPE {}

    /** RMS有效值类型，显示频谱的有效值 */
    public static final int FFT_TYPE_RMS = 0;

    /** dB分贝类型，显示频谱的分贝值 */
    public static final int FFT_TYPE_DB = 1;

    // ═══════════════════════════════════════════════════════════════════════════════
    // FFT窗函数常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * FFT窗函数注解
     * 用于限制参数只能是定义的窗函数常量
     */
    @IntDef({FFT_WINDOW_RECTANGLE,FFT_WINDOW_HAMMING,FFT_WINDOW_BLACKMAN,FFT_WINDOW_HANNING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FFT_WINDOW {}

    /** 矩形窗（均匀窗），主瓣窄，旁瓣高 */
    public static final int FFT_WINDOW_RECTANGLE = 0;

    /** 汉明窗，主瓣较宽，旁瓣较低 */
    public static final int FFT_WINDOW_HAMMING = 1;

    /** 布莱克曼窗，主瓣最宽，旁瓣最低 */
    public static final int FFT_WINDOW_BLACKMAN = 2;

    /** 汉宁窗（升余弦窗），主瓣宽度适中，旁瓣衰减快 */
    public static final int FFT_WINDOW_HANNING = 3;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 关联的数学通道对象 */
    private MathChannel mathChannel;

    /** FFT源通道索引 */
    private int srcChIdx = ChannelFactory.CH1;

    /** FFT显示类型（RMS或dB） */
    private int fftType = FFT_TYPE_RMS;

    /** FFT窗函数类型 */
    private int fftWindow = FFT_WINDOW_RECTANGLE;

    /** FFT垂直轴管理对象 */
    private VerticalAxisMathFft verticalAxisMathFft;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造MathFFTWave实例
     *
     * @param channel 关联的数学通道对象
     */
    public MathFFTWave(MathChannel channel){
        super(MATH_FFTWAVE);                                                         // 调用父类构造函数，设置类型为FFT类型
        mathChannel = channel;                                                       // 保存数学通道引用
        verticalAxisMathFft = new VerticalAxisMathFft();                             // 创建FFT垂直轴管理对象
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 源通道管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置FFT源通道
     * 设置后会触发通道采样变化和强制刷新
     *
     * @param chIdx 源通道索引
     */
    public void setSource(int chIdx){
        srcChIdx = chIdx;                                                            // 设置源通道索引
        mathChannel.chSampleChange();                                                // 通知通道采样变化
        mathChannel.forceRefresh();                                                  // 强制刷新数学通道
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_CHANNEL_VSCALE, chIdx, mathChannel.getChId()), true); // 发送垂直档位事件
        sendEvent(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR);                          // 清除余辉
        sendEvent(EventFactory.EVENT_MATH_SOURCE,true);                              // 发送数学源事件
    }

    /**
     * 获取FFT源通道索引
     *
     * @return 源通道索引
     */
    public int getSource(){
        return srcChIdx;                                                             // 返回源通道索引
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // FFT类型管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置FFT显示类型
     * 设置后会强制刷新数学通道并清除余辉
     *
     * @param fftType FFT类型
     *                 FFT_TYPE_RMS: RMS有效值
     *                 FFT_TYPE_DB: dB分贝
     */
    public void setFFTType(@FFT_TYPE int fftType){
        this.fftType = fftType;                                                      // 设置FFT类型
        mathChannel.forceRefresh();                                                  // 强制刷新数学通道
        mathChannel.mathVChange();                                                   // 通知数学垂直轴变化
        sendEvent(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR);                          // 清除余辉
    }

    /**
     * 获取FFT显示类型
     *
     * @return FFT类型
     */
    public int getFFTType(){
        return fftType;                                                              // 返回FFT类型
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // FFT窗函数管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取FFT窗函数类型
     *
     * @return 窗函数类型
     */
    public int getFFTWindow() {
        return fftWindow;                                                            // 返回窗函数类型
    }

    /**
     * 设置FFT窗函数类型
     * 设置后会强制刷新数学通道并清除余辉
     *
     * @param fftWindow 窗函数类型
     *                  FFT_WINDOW_RECTANGLE: 矩形窗
     *                  FFT_WINDOW_HAMMING: 汉明窗
     *                  FFT_WINDOW_BLACKMAN: 布莱克曼窗
     *                  FFT_WINDOW_HANNING: 汉宁窗
     */
    public void setFFTWindow(@FFT_WINDOW int fftWindow) {
        this.fftWindow = fftWindow;                                                  // 设置窗函数类型
        mathChannel.forceRefresh();                                                  // 强制刷新数学通道
        sendEvent(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR);                          // 清除余辉
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件发送方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 发送事件（同步）
     *
     * @param eventId 事件ID
     */
    public void sendEvent(int eventId){
        EventFactory.sendEvent(new EventBase(eventId, mathChannel.getChId()), false); // 同步发送事件
    }

    /**
     * 发送事件
     *
     * @param eventId 事件ID
     * @param async true: 异步发送
     *              false: 同步发送
     */
    public void sendEvent(int eventId,boolean async){
        EventFactory.sendEvent(new EventBase(eventId, mathChannel.getChId()), async); // 发送事件
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 通道采样检查方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 检查指定通道是否参与数学运算
     *
     * @param chIdx 通道索引
     * @return true: 该通道参与运算
     *         false: 该通道不参与运算
     */
    @Override
    public boolean isChInSample(int chIdx) {
        return srcChIdx == chIdx;                                                    // 检查是否为源通道
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 垂直轴管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取垂直档位值
     * 根据FFT类型返回不同的档位值
     *
     * @return 垂直档位值
     */
    public double getVScaleVal() {
        if (fftType == FFT_TYPE_DB) {                                                // dB类型
            return verticalAxisMathFft.getScaleVal();                                // 返回dB档位值
        } else {                                                                     // RMS类型
            Channel channel=ChannelFactory.getDynamicChannel(srcChIdx);              // 获取源通道
            double probeRate = 1.0;                                                  // 探头比率默认值
            if (channel != null) {                                                   // 通道有效
                probeRate = channel.getProbeRate();                                  // 获取探头比率
            }
            return verticalAxisMathFft.getScaleValInRms() * probeRate;               // 返回RMS档位值 × 探头比率
        }
    }

    /**
     * 获取当前档位ID对应的档位值
     * 根据FFT类型返回不同的档位值
     *
     * @return 档位值
     */
    public double getVScaleIdVal() {
        if (fftType == FFT_TYPE_DB) {                                                // dB类型
            return verticalAxisMathFft.getScaleIdVal();                              // 返回dB档位值
        } else {                                                                     // RMS类型
            return verticalAxisMathFft.getScaleIdValInRms();                         // 返回RMS档位值
        }
    }

    /**
     * 根据档位ID和FFT类型获取档位值
     *
     * @param scaleId 档位ID
     * @param fftType FFT类型
     * @return 档位值
     */
    public double getVScaleIdVal(int scaleId, int fftType) {
        if (fftType == FFT_TYPE_DB) {                                                // dB类型
            return verticalAxisMathFft.getScaleIdVal(scaleId);                       // 返回dB档位值
        } else {                                                                     // RMS类型
            return verticalAxisMathFft.getScaleIdValInRms(scaleId);                  // 返回RMS档位值
        }
    }

    /**
     * 根据档位值和FFT类型获取档位ID
     *
     * @param val 档位值
     * @param fftType FFT类型
     * @return 档位ID
     */
    public int getVScaleId(double val, int fftType) {
        if (fftType == FFT_TYPE_DB) {                                                // dB类型
            return verticalAxisMathFft.getScaleId(val);                              // 返回dB档位ID
        } else {                                                                     // RMS类型
            return verticalAxisMathFft.getScaleIdInRms(val);                         // 返回RMS档位ID
        }
    }

    /**
     * 获取当前档位ID
     * 根据FFT类型返回不同的档位ID
     *
     * @return 档位ID
     */
    public int getVScaleId() {
        if (fftType == FFT_TYPE_DB) {                                                // dB类型
            return verticalAxisMathFft.getScaleId();                                 // 返回dB档位ID
        } else {                                                                     // RMS类型
            return verticalAxisMathFft.getScaleIdInRms();                            // 返回RMS档位ID
        }
    }

    /**
     * 获取微调比例
     * 根据FFT类型返回不同的微调比例
     *
     * @return 微调比例
     */
    public double getFineScale() {
        if (fftType == FFT_TYPE_DB) {                                                // dB类型
            return verticalAxisMathFft.getFineScale();                               // 返回dB微调比例
        } else {                                                                     // RMS类型
            return verticalAxisMathFft.getFineScaleInRms();                          // 返回RMS微调比例
        }
    }

    /**
     * 获取最大档位ID
     * 根据FFT类型返回不同的最大档位ID
     *
     * @return 最大档位ID
     */
    public int getVScaleIdMax() {
        if(fftType == FFT_TYPE_DB) {                                                 // dB类型
            return verticalAxisMathFft.DANG_DBV_MAX;                                 // 返回dB最大档位ID
        } else {                                                                     // RMS类型
            return verticalAxisMathFft.DANG_RMS_MAX;                                 // 返回RMS最大档位ID
        }
    }

    /**
     * 获取最小档位ID
     * 根据FFT类型返回不同的最小档位ID
     *
     * @return 最小档位ID
     */
    public  int getVScaleIdMin() {
        if(fftType == FFT_TYPE_DB) {                                                 // dB类型
            return verticalAxisMathFft.DANG_DBV_MIN;                                 // 返回dB最小档位ID
        } else {                                                                     // RMS类型
            return verticalAxisMathFft.DANG_RMS_MIN;                                 // 返回RMS最小档位ID
        }
    }

    /**
     * 设置档位ID
     * 根据FFT类型设置不同的档位ID
     *
     * @param scaleId 档位ID
     */
    public void setVScaleId(int scaleId) {
        if(fftType == FFT_TYPE_DB) {                                                 // dB类型
            verticalAxisMathFft.setScaleId(scaleId);                                 // 设置dB档位ID
        } else {                                                                     // RMS类型
            verticalAxisMathFft.setScaleIdInRms(scaleId);                            // 设置RMS档位ID
        }
        mathChannel.forceRefresh();                                                  // 强制刷新数学通道

    }

    /**
     * 根据档位值设置档位ID
     * 根据FFT类型设置不同的档位ID
     *
     * @param scaleIdVal 档位值
     */
    public void setVScaleVal(double scaleIdVal){
        setVScaleId(fftType == FFT_TYPE_DB? verticalAxisMathFft.getScaleId(scaleIdVal) : verticalAxisMathFft.getScaleIdInRms(scaleIdVal)); // 设置档位ID
    }

    /**
     * 设置微调比例
     * 根据FFT类型设置不同的微调比例
     *
     * @param fineScale 微调比例
     */
    public void setFineScale(double fineScale){
        if(fftType == FFT_TYPE_DB) {                                                 // dB类型
            verticalAxisMathFft.setFineScale(fineScale);                             // 设置dB微调比例
        } else {                                                                     // RMS类型
            verticalAxisMathFft.setFineScaleInRms(fineScale);                        // 设置RMS微调比例
        }
    }

    /**
     * 根据档位ID获取档位值（重写父类方法）
     *
     * @param scaleId 档位ID
     * @return 档位值（未实现，返回0）
     */
    @Override
    public double getVScaleIdVal(int scaleId) {
        return 0;                                                                    // 未实现，返回0
    }

    /**
     * 根据档位值获取档位ID（重写父类方法）
     *
     * @param scaleVal 档位值
     * @return 档位ID（未实现，返回0）
     */
    @Override
    public int getVScaleId(double scaleVal) {
        return 0;                                                                    // 未实现，返回0
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 探头类型生成方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 生成探头类型
     * 根据FFT类型和源通道探头类型生成结果单位
     *
     * <p><b>生成规则：</b></p>
     * <ul>
     *   <li>RMS类型：V（电压）或 A（电流）</li>
     *   <li>dB类型：dB（分贝）</li>
     * </ul>
     *
     * @return 探头类型
     */
    public int generateProbeType() {
        int probeType = 255;                                                         // 默认探头类型
        if (fftType == FFT_TYPE_RMS) {//RMS                                          // RMS类型
            int isCur = ChannelFactory.getDynamicChannel(srcChIdx).getProbeType();   // 获取源通道探头类型
            if (isCur == 0) {                                                        // 电压探头
                probeType = verticalAxisMathFft.PROBE_TYPE_VOL;//"V"                 // 返回V
            } else {                                                                 // 电流探头
                probeType = verticalAxisMathFft.PROBE_TYPE_CUR;//"A"                 // 返回A
            }
        } else {//DBV                                                                // dB类型
            probeType = verticalAxisMathFft.PROBE_TYPE_DB;//"db"                     // 返回dB
        }
        return probeType;                                                            // 返回探头类型
    }

    /**
     * 获取探头类型
     *
     * @return 探头类型
     */
    public int getProbeType() {
        return verticalAxisMathFft.getProbeType();                                   // 返回探头类型
    }

    /**
     * 设置探头类型
     *
     * @param probeType 探头类型
     */
    public void setProbeType(int probeType) {
        verticalAxisMathFft.setProbeType(probeType);                                 // 设置探头类型
    }

    /**
     * 设置探头字符串
     *
     * @param probeStr 探头字符串
     */
    @Override
    public void setProbeStr(String probeStr) {
        verticalAxisMathFft.setProbeStr(probeStr);                                   // 设置探头字符串
    }

    /**
     * 获取探头字符串
     *
     * @return 探头字符串
     */
    @Override
    public String getProbeStr() {
        return verticalAxisMathFft.getProbeStr();                                    // 返回探头字符串
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // FFT结果访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取数学通道索引
     * 用于访问Native层的FFT结果
     *
     * @return 数学通道索引（0-3）
     */
    private int getMathChIdx(){
        return mathChannel.getChId() - ChannelFactory.MATH1;                         // 计算相对索引
    }

    /**
     * 获取FFT的DC值（直流分量）
     * 根据FFT类型返回不同的值
     *
     * @param val 转换系数（RMS类型使用）
     * @return DC值
     */
    public double getFFTDCVal(double val) {

        if(fftType == FFT_TYPE_DB)                                                   // dB类型
            return MathNative.getFFTDCVal(getMathChIdx());                           // 返回dB值
        else                                                                         // RMS类型
            return MathNative.getFFTDCVal(getMathChIdx())*val;                       // 返回RMS值 × 转换系数
    }

    /**
     * 获取FFT的最大值
     * 根据FFT类型返回不同的值
     *
     * @param val 转换系数（RMS类型使用）
     * @return 最大值
     */
    public double getFFTMaxVal(double val) {
        if(fftType == FFT_TYPE_DB)                                                   // dB类型
            return MathNative.getFFTMaxVal(getMathChIdx());                          // 返回dB值
        else                                                                         // RMS类型
            return MathNative.getFFTMaxVal(getMathChIdx())*val;                      // 返回RMS值 × 转换系数
    }

    /**
     * 检查FFT类型是否为dB
     *
     * @return true: dB类型
     *         false: RMS类型
     */
    public boolean isTypeDb(){
        return fftType==FFT_TYPE_DB;                                                 // 返回是否为dB类型
    }

    /**
     * 获取FFT最大值对应的频率索引
     *
     * @return 频率索引
     */
    public int getFFTMaxIdx() {
        return MathNative.getFFTMaxIdx(getMathChIdx());                              // 返回最大值索引
    }
}
