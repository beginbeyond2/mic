package com.micsig.tbook.scope.channel;  // 定义包名：示波器通道管理模块

import com.micsig.base.Logger;  // 导入Logger类：基础日志工具
import com.micsig.tbook.scope.Action.UiMessage;  // 导入UiMessage类：UI消息常量
import com.micsig.tbook.scope.Data.DataFactory;  // 导入DataFactory类：数据缓冲区工厂
import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理
import com.micsig.tbook.scope.ScopeBase;  // 导入ScopeBase类：示波器基类
import com.micsig.tbook.scope.ScopeMessage;  // 导入ScopeMessage类：示波器消息管理
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;  // 导入HorizontalAxisMath类：数学通道水平轴
import com.micsig.tbook.scope.math.MathDualWave;  // 导入MathDualWave类：双波形数学运算
import com.micsig.tbook.scope.math.MathExprWave;  // 导入MathExprWave类：表达式数学运算
import com.micsig.tbook.scope.math.MathFFTWave;  // 导入MathFFTWave类：FFT数学运算
import com.micsig.tbook.scope.math.MathWave;  // 导入MathWave类：数学波形基类

/**
 * 数学通道类 - 数学运算结果通道实现
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道管理模块）</li>
 *   <li>架构层级：数据层 - 数学通道实现</li>
 *   <li>设计模式：策略模式 + 组合模式</li>
 *   <li>职责类型：数学运算、FFT分析、档位管理、采样率计算</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理三种数学运算类型（双波形/FFT/表达式）</li>
 *   <li>提供数学运算结果的档位管理</li>
 *   <li>支持FFT频率轴生成和显示</li>
 *   <li>管理数学通道的采样率和插值</li>
 * </ul>
 * 
 * <p><b>数学运算类型架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   MathChannel - 数学通道                                                 │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │                    MathWave[] 数学运算数组                       │   │
 * │   │                                                                   │   │
 * │   │   [0] MathDualWave    双波形运算                                  │   │
 * │   │       ├── 支持运算：加(+)、减(-)、乘(*)、除(/)                    │   │
 * │   │       ├── 源通道：CH1-CH8任意两个通道                            │   │
 * │   │       └── 示例：CH1+CH2, CH1-CH2, CH1*CH2, CH1/CH2              │   │
 * │   │                                                                   │   │
 * │   │   [1] MathFFTWave    FFT频谱分析                                  │   │
 * │   │       ├── 支持类型：幅度谱、功率谱、相位谱                        │   │
 * │   │       ├── 显示单位：线性(V)、对数(dB)                            │   │
 * │   │       ├── 窗函数：矩形窗、汉宁窗、汉明窗等                        │   │
 * │   │       └── 源通道：CH1-CH8任意一个通道                            │   │
 * │   │                                                                   │   │
 * │   │   [2] MathExprWave   表达式运算                                   │   │
 * │   │       ├── 支持函数：sin、cos、tan、log、exp、sqrt、diff等        │   │
 * │   │       ├── 支持运算：+、-、*、/、^、()等                          │   │
 * │   │       ├── 源通道：CH1-CH8任意通道                                │   │
 * │   │       └── 示例：sin(CH1), log(CH1*CH2), diff(CH1)               │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   当前运算类型：MathType（默认为MATH_EXPR）                              │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>FFT频率轴架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   FFT频率轴生成（HorizontalAxisMath）                                     │
 * │                                                                          │
 * │   频率分辨率 = 采样率 / FFT点数                                           │
 * │   频率范围 = 0 ~ (采样率/2)                                               │
 * │                                                                          │
 * │   示例：                                                                  │
 * │   采样率 = 1GHz, FFT点数 = 1024                                          │
 * │   频率分辨率 = 1GHz / 1024 ≈ 976.56kHz                                   │
 * │   频率范围 = 0 ~ 500MHz                                                  │
 * │                                                                          │
 * │   FFT显示：                                                               │
 * │   ├── X轴：频率（Hz）                                                    │
 * │   ├── Y轴：幅度（V或dB）                                                 │
 * │   └── 水平轴刻度由HorizontalAxisMath生成                                 │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   BaseChannel（通道基类）
 *       │
 *       └── MathChannel（数学通道）
 *               │
 *               ├── 组合：MathWave[]（数学运算数组）
 *               ├── 组合：HorizontalAxisMath（FFT水平轴）
 *               └── 继承：open/close/activate/setPos等方法
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：BaseChannel（通道基类）</li>
 *   <li>组合：MathWave、MathDualWave、MathFFTWave、MathExprWave（数学运算）</li>
 *   <li>组合：HorizontalAxisMath（FFT水平轴）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see BaseChannel 通道基类
 * @see MathWave 数学波形基类
 * @see MathDualWave 双波形运算
 * @see MathFFTWave FFT运算
 * @see MathExprWave 表达式运算
 */
public class MathChannel extends BaseChannel {  // 继承BaseChannel通道基类

    /** 日志标签 */
    private static final String TAG = "MathChannel";  // 日志输出标签
    
    /** 当前数学运算类型：使用volatile保证多线程可见性 */
    private volatile int MathType = MathWave.MATH_EXPR;  // 默认为表达式运算
    
    /** 数学运算实例数组：存储三种数学运算的实现 */
    private MathWave[] mathWave = new MathWave[MathWave.MATH_TYPE_MAX];  // 数学运算数组

    /** 采样率：用于FFT频率计算 */
    private double sampleRate = 0;  // 采样率
    
    /** 插值系数：采样率乘数 */
    private double chaZhiNum = 1;  // 插值系数
    
    /** 波形长度：用于FFT点数计算 */
    private int waveLen = 0;  // 波形长度
    
    /** FFT水平轴管理器 */
    private HorizontalAxisMath horizontalAxisMath;  // FFT水平轴管理器

    /**
     * 构造方法：初始化数学通道
     * 
     * <p>创建三种数学运算实例，初始化水平轴管理器。
     * 
     * @param chIdx 通道索引
     */
    public MathChannel(int chIdx) {
        super(chIdx,"MATH" + (chIdx - ChannelFactory.MATH1 + 1) );  // 调用父类构造方法，设置通道名称
        horizontalAxisMath = new HorizontalAxisMath(this);  // 创建FFT水平轴管理器
        mathWave[MathWave.MATH_DUALWAVE] = new MathDualWave(this);  // 创建双波形运算实例
        mathWave[MathWave.MATH_FFTWAVE] = new MathFFTWave(this);  // 创建FFT运算实例
        mathWave[MathWave.MATH_EXPR] = new MathExprWave(this);  // 创建表达式运算实例
        setBuffer(DataFactory.allocateBufferQueue(false));  // 分配数据缓冲区队列
    }

    /**
     * 检查指定通道是否在当前数学运算的采样中
     * 
     * @param chIdx 通道索引
     * @return true表示在采样中，false表示不在
     */
    public boolean isChInSample(int chIdx){
        if(MathWave.isMathTypeVaild(MathType)){  // 检查数学类型是否有效
            return mathWave[MathType].isChInSample(chIdx);  // 返回源通道采样状态
        }
        return  false;  // 无效类型返回false
    }

    /**
     * 检查FFT是否为dB显示模式
     * 
     * @return true表示dB模式，false表示线性模式
     */
    public boolean isFFTDb(){
        if (MathType==MathWave.MATH_FFTWAVE){  // 检查是否为FFT类型
            MathFFTWave fft= ((MathFFTWave)mathWave[MathType]);  // 获取FFT实例
            return fft.isTypeDb();  // 返回dB模式状态
        }
        return false;  // 非FFT类型返回false
    }

    /**
     * 设置通道位置
     * 
     * <p>将UI坐标转换为FPGA坐标，发送位置变化事件。
     * 
     * @param pos 位置值（UI坐标）
     */
    @Override
    public void setPos(double pos) {
        pos = pos * ScopeBase.getToFPGACoff();  // 转换为FPGA坐标
        super.setPos(pos);  // 调用父类方法
        sendEvent(EventFactory.EVENT_MATH_VPOS, true);  // 发送数学通道位置事件
        sendEvent(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR,true);  // 发送余辉清除事件
    }

//    @Override
//    protected void setPos(double pos) {
//        pos = pos * ScopeBase.getToFPGACoff();
//        super.setPos(pos);
//        sendEvent(EventFactory.EVENT_MATH_VPOS, true);
//        sendEvent(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR,true);
//    }

    /**
     * 打开通道
     * 
     * <p>设置打开状态，更新采样状态，发送打开事件。
     */
    @Override
    public void open() {
        setOpen(true);  // 设置打开状态
        chSampleChange();  // 更新通道采样状态
        if (MathType == MathWave.MATH_FFTWAVE)  // 检查是否为FFT类型
            horizontalAxisMath.genFFT_Axis(true);  // 生成FFT频率轴
        sendEvent(EventFactory.EVENT_MATH_VPOS);  // 发送数学通道位置事件
        sendEvent(EventFactory.EVENT_CHANNEL_OPEN, true);  // 发送通道打开事件

    }

    /**
     * 关闭通道
     * 
     * <p>设置关闭状态，更新采样状态，发送关闭事件。
     */
    @Override
    public void close() {
        setOpen(false);  // 设置关闭状态
        chSampleChange();  // 更新通道采样状态
        sendEvent(EventFactory.EVENT_MATH_VPOS);  // 发送数学通道位置事件
        sendEvent(EventFactory.EVENT_CHANNEL_CLOSE);  // 发送通道关闭事件
    }

    /**
     * 发送事件（同步）
     * 
     * @param eventId 事件ID
     */
    public void sendEvent(int eventId){
        EventFactory.sendEvent(new EventBase(eventId, getChId()), false);  // 同步发送事件
    }

    /**
     * 发送事件
     * 
     * @param eventId 事件ID
     * @param async true表示异步发送，false表示同步发送
     */
    public void sendEvent(int eventId,boolean async){
        EventFactory.sendEvent(new EventBase(eventId, getChId(), getChId()), async);  // 发送事件
    }

    /**
     * 激活通道
     * 
     * <p>如果是FFT类型，重新设置数学类型以刷新频率轴。
     */
    @Override
    public void activate() {

        if (MathType == MathWave.MATH_FFTWAVE) {  // 检查是否为FFT类型
            setMathType(MathWave.MATH_FFTWAVE);  // 重新设置FFT类型
        }
        sendEvent(EventFactory.EVENT_CHANNEL_ACTIVE);  // 发送激活事件

        ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);  // 发送采样频率UI消息
        ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_SIMPLE_WIN_DIS);  // 发送窗口显示UI消息
        sendEvent(EventFactory.EVENT_AFTERGLOW_MATH);  // 发送余辉事件
    }

    /** 前景色值：用于通道显示颜色 */
    private int colorValue = 0xFF0000FF;  // 默认蓝色

    /**
     * 设置前景色
     * 
     * <p>将RGB颜色值转换为BGR格式（用于显示）。
     * 
     * @param colorValue ARGB颜色值
     */
    public void setForegroundColor(int colorValue) {
        this.colorValue = (colorValue & 0xFF000000)  // 保留Alpha通道
                | ((colorValue & 0xFF) << 16)  // R通道移到B位置
                | ((colorValue & 0xFF00))  // G通道保持不变
                | ((colorValue & 0xFF0000) >> 16);  // B通道移到R位置
    }

    /**
     * 获取前景色
     * 
     * @return ARGB颜色值
     */
    public int getForegroundColor() {
        return colorValue;  // 返回前景色
    }

    /**
     * 获取当前数学运算类型
     * 
     * @return 数学运算类型ID
     */
    public int getMathType() {
        synchronized (this) {  // 同步锁
            return MathType;  // 返回数学类型
        }
    }

    /**
     * 设置数学运算类型
     * 
     * <p>切换数学运算类型，更新采样状态，刷新显示。
     * 
     * @param mathType 数学运算类型ID
     */
    public void setMathType(int mathType) {

        if (MathWave.isMathTypeVaild(mathType)) {  // 检查数学类型是否有效
            synchronized (this) {  // 同步锁
                MathType = mathType;  // 设置数学类型
            }
            if (mathType == MathWave.MATH_FFTWAVE)  // 检查是否为FFT类型
                horizontalAxisMath.genFFT_Axis();  // 生成FFT频率轴
            chSampleChange();  // 更新通道采样状态

            mathVChange();  // 刷新数学通道显示
            sendEvent(EventFactory.EVENT_MATH_SOURCE,true);  // 发送数学源事件
        }
    }

    /**
     * 数学通道变化处理
     * 
     * <p>发送通道打开事件和余辉相关事件。
     */
    public void mathVChange() {
        sendEvent(EventFactory.EVENT_CHANNEL_OPEN, true);  // 发送通道打开事件
        sendEvent(EventFactory.EVENT_AFTERGLOW_MATH);  // 发送余辉事件
        sendEvent(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR);  // 发送余辉清除事件
    }


    /**
     * 获取当前数学运算实例
     * 
     * @return MathWave实例
     */
    public MathWave getMathWave() {
        synchronized (this) {  // 同步锁
            return mathWave[MathType];  // 返回当前类型的数学运算实例
        }
    }

    /**
     * 获取双波形运算实例
     * 
     * @return MathDualWave实例
     */
    public MathDualWave getMathDualWave() {
        return (MathDualWave) (mathWave[MathWave.MATH_DUALWAVE]);  // 返回双波形运算实例
    }

    /**
     * 获取FFT运算实例
     * 
     * @return MathFFTWave实例
     */
    public MathFFTWave getMathFFTWave() {
        return (MathFFTWave) (mathWave[MathWave.MATH_FFTWAVE]);  // 返回FFT运算实例
    }

    /**
     * 获取表达式运算实例
     * 
     * @return MathExprWave实例
     */
    public MathExprWave getMathExprWave() {
        return (MathExprWave) (mathWave[MathWave.MATH_EXPR]);  // 返回表达式运算实例
    }

    /**
     * 获取指定类型的数学运算实例
     * 
     * @param mathType 数学运算类型
     * @return MathWave实例，无效类型返回null
     */
    public MathWave getMathWave(int mathType) {
        if (MathWave.isMathTypeVaild(mathType)) {  // 检查数学类型是否有效
            return mathWave[mathType];  // 返回指定类型的实例
        }
        return null;  // 无效类型返回null
    }


    /**
     * 获取探头类型
     * 
     * @return 探头类型ID
     */
    public int getProbeType() {
        return getMathWave().getProbeType();  // 返回当前数学运算的探头类型
    }

    /**
     * 设置探头类型
     * 
     * @param probeType 探头类型ID
     */
    public void setProbeType(int probeType) {
        getMathWave().setProbeType(probeType);  // 设置当前数学运算的探头类型
    }

    /**
     * 获取探头字符串
     * 
     * @return 探头字符串
     */
    public String getProbeStr() {
        return getMathWave().getProbeStr();  // 返回当前数学运算的探头字符串
    }

    /**
     * 设置探头字符串
     * 
     * @param probeStr 探头字符串
     */
    public void setProbeStr(String probeStr) {
        getMathWave().setProbeStr(probeStr);  // 设置当前数学运算的探头字符串
    }

    /**
     * 获取档位值
     * 
     * @return 档位值
     */
    public double getVScaleVal() {
        return getMathWave().getVScaleVal();  // 返回当前数学运算的档位值
    }

    /**
     * 生成探头类型
     * 
     * @return 探头类型ID
     */
    public int generateProbeType() {
        return getMathWave().generateProbeType();  // 返回当前数学运算生成的探头类型
    }

    /**
     * 获取指定档位ID对应的值
     * 
     * @param scaleId 档位ID
     * @return 档位值
     */
    public double getVScaleIdVal(int scaleId) {
        int isInRms = 0;  // RMS类型标志
        if (getMathType() == MathWave.MATH_FFTWAVE) {  // 检查是否为FFT类型
            isInRms = (getMathFFTWave().getFFTType());  // 获取FFT类型
        }
        return getVScaleIdVal(scaleId, getMathType(), isInRms);  // 返回档位值
    }

    /**
     * 获取指定档位ID对应的值（指定数学类型）
     * 
     * @param scaleId 档位ID
     * @param mathType 数学运算类型
     * @param isInRms FFT类型标志
     * @return 档位值
     */
    public double getVScaleIdVal(int scaleId, int mathType, int isInRms) {

        if (mathType == MathWave.MATH_FFTWAVE) {  // 检查是否为FFT类型
            return getMathFFTWave().getVScaleIdVal(scaleId, isInRms);  // 返回FFT档位值
        } else {
            return getMathWave().getVScaleIdVal(scaleId);  // 返回普通档位值
        }
    }

    /**
     * 获取当前档位值
     * 
     * @return 档位值
     */
    public double getVScaleIdVal() {
        return getMathWave().getVScaleIdVal();  // 返回当前档位值
    }

    /**
     * 根据值获取档位ID（指定数学类型）
     * 
     * @param val 档位值
     * @param mathType 数学运算类型
     * @param isInRms FFT类型标志
     * @return 档位ID
     */
    public int getVScaleId(double val, int mathType, int isInRms) {
        if (mathType == MathWave.MATH_FFTWAVE) {  // 检查是否为FFT类型
            return getMathFFTWave().getVScaleId(val, isInRms);  // 返回FFT档位ID
        } else {
            return getMathWave().getVScaleId(val);  // 返回普通档位ID
        }
    }

    /**
     * 根据值获取档位ID（指定数学类型）
     * 
     * @param val 档位值
     * @param mathType 数学运算类型
     * @return 档位ID
     */
    public int getVScaleId(double val, int mathType) {
        return getVScaleId(val, mathType, getMathFFTWave().getFFTType());  // 返回档位ID
    }

    /**
     * 根据值获取档位ID
     * 
     * @param val 档位值
     * @return 档位ID
     */
    public int getVScaleId(double val) {
        return getVScaleId(val, getMathType());  // 返回档位ID
    }

    /**
     * 获取当前档位ID
     * 
     * @return 档位ID
     */
    public int getVScaleId() {

        return getMathWave().getVScaleId();  // 返回当前档位ID

    }

    /**
     * 获取指定数学类型的最大档位ID
     * 
     * @param mathType 数学运算类型
     * @return 最大档位ID
     */
    public int getVScaleIdMax(int mathType) {
        return getMathWave(mathType).getVScaleIdMax();  // 返回最大档位ID
    }

    /**
     * 获取当前数学类型的最大档位ID
     * 
     * @return 最大档位ID
     */
    public int getVScaleIdMax() {
        return getVScaleIdMax(getMathType());  // 返回最大档位ID
    }

    /**
     * 获取指定数学类型的最小档位ID
     * 
     * @param mathType 数学运算类型
     * @return 最小档位ID
     */
    public int getVScaleIdMin(int mathType) {
        return getMathWave(mathType).getVScaleIdMin();  // 返回最小档位ID
    }

    /**
     * 获取当前数学类型的最小档位ID
     * 
     * @return 最小档位ID
     */
    public int getVScaleIdMin() {
        return getVScaleIdMin(getMathType());  // 返回最小档位ID
    }

    /**
     * 获取微调系数
     * 
     * @return 微调系数
     */
    public double getFineScale() {
        return getMathWave().getFineScale();  // 返回微调系数

    }

    /**
     * 设置档位ID
     * 
     * @param scaleId 档位ID
     */
    public void setVScaleId(int scaleId) {

        setVScaleId(scaleId,1.0);  // 设置档位ID，微调系数为1.0
    }

    /**
     * 设置档位ID（带微调系数）
     * 
     * @param scaleId 档位ID
     * @param fine 微调系数
     */
    private void setVScaleId(int scaleId,double fine){
        getMathWave().setVScaleId(scaleId);  // 设置数学运算的档位ID
        setFineScale(fine);  // 设置微调系数
        super.setVScaleId(scaleId);  // 调用父类方法
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE, true);  // 发送档位变化事件
        sendEvent(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR);  // 发送余辉清除事件
    }

    /**
     * 计算档位ID
     * 
     * <p>根据微调系数和调整值计算新的档位ID。
     * 
     * @param val 调整值（正数增加档位，负数减少档位）
     * @return 计算后的档位ID
     */
    public int calcVScaleId(int val){
        int scaleId = getVScaleId();  // 获取当前档位ID
        double fineVal = getFineScale();  // 获取微调系数
        if(val > 0){  // 增加档位
            if(fineVal >= 1.0){  // 微调系数大于等于1.0时才能增加
                scaleId += val;  // 增加档位
            }
        }else if(val < 0){  // 减少档位
            if(fineVal <= 1.0){  // 微调系数小于等于1.0时才能减少
                scaleId += val;  // 减少档位
            }
        }
        return scaleId;  // 返回计算后的档位ID
    }

    /**
     * 检查档位ID是否有效
     * 
     * @param vScaleId 档位ID
     * @return true表示有效，false表示无效
     */
    @Override
    public boolean isVScaleIdValid(int vScaleId) {

        if (vScaleId <= getVScaleIdMax() && vScaleId >= getVScaleIdMin()) {  // 检查档位ID范围
            double max = Integer.MAX_VALUE;  // 最大值
            if (getVerticalMode() == VERTICAL_MODE_SCREEN_CENTER) {  // 屏幕中心模式
                double val = centerVal / getVScaleIdVal(vScaleId);  // 计算值
                return (val < max) && (val > -max);  // 检查值是否在有效范围内
            } else {
                return true;  // 其他模式直接返回有效
            }
        }
        return false;  // 超出范围返回无效
    }

    /**
     * 根据档位值设置档位ID
     * 
     * <p>查找最接近的档位ID并设置。
     * 
     * @param scaleIdVal 档位值
     */
    public void setVScaleVal(double scaleIdVal) {
        int scaleId = getVScaleIdMin();  // 获取最小档位ID
        int m = getVScaleIdMax();  // 获取最大档位ID
        double v1 = getVScaleIdVal(scaleId);  // 获取最小档位值
        double v2 = getVScaleIdVal(m);  // 获取最大档位值
        if(scaleIdVal < v1){  // 小于最小值
            scaleIdVal = v1;  // 限制为最小值
        }else if(scaleIdVal > v2){  // 大于最大值
            scaleIdVal = v2;  // 限制为最大值
        }
        for(int i= scaleId;i < m;i++){  // 遍历档位
            v1 = getVScaleIdVal(i);  // 获取当前档位值
            v2 = getVScaleIdVal(i + 1);  // 获取下一档位值
            if(scaleIdVal>=v1 && scaleIdVal<=v2){  // 检查是否在范围内
                if(Math.abs(scaleIdVal-v1) < Math.abs(scaleIdVal-v2)){  // 比较距离
                    scaleId = i;  // 选择较近的档位
                }else{
                    scaleId = i + 1;  // 选择较近的档位
                }
                break;  // 找到后退出循环
            }
        }
        setVScaleId(scaleId,1.0);  // 设置档位ID
    }

    /**
     * 获取FFT水平轴管理器
     * 
     * @return HorizontalAxisMath实例
     */
    public HorizontalAxisMath getHorizontalAxisMathFFT() {
        return horizontalAxisMath;  // 返回FFT水平轴管理器
    }

    /**
     * 设置微调系数
     * 
     * @param fineScale 微调系数
     */
    public void setFineScale(double fineScale) {
        getMathWave().setFineScale(fineScale);  // 设置数学运算的微调系数
    }

    /**
     * 获取每像素垂直单位
     * 
     * @return 每像素垂直单位值
     */
    @Override
    public double getVerticalPerPix() {
        double val = getVScaleVal();  // 获取档位值
        double h =  ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff();  // 获取每格像素数
        if (Scope.getInstance().isZoom()) {  // 检查是否为Zoom模式
            h = ScopeBase.getZoomVerticalPerGridPixels() * ScopeBase.getToUICoff();  // 使用Zoom模式像素数
        }
        return val / h;  // 返回每像素垂直单位
    }

    /**
     * 获取AD每像素对应幅值
     * 
     * @return AD每像素幅值
     */
    public double getADVerticalPerPix() {  // AD每像素对应幅值
        double val = getVScaleVal();  // 获取档位值
        double h =  ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff();  // 获取每格像素数

        return val / h;  // 返回AD每像素幅值
    }

    /**
     * 获取采样率
     * 
     * <p>返回实际采样率（采样率 * 插值系数）。
     * 
     * @return 采样率
     */
    @Override
    public double getSampleRate() {
        return sampleRate * chaZhiNum;  // 返回实际采样率
    }

    /**
     * 获取显示用采样率
     * 
     * @return 显示用采样率
     */
    @Override
    public double getSampleRate2display() {
        return sampleRate;  // 返回显示用采样率
    }

    /**
     * 获取波形长度
     * 
     * @return 波形长度
     */
    public synchronized int getWaveLen() {
        return waveLen;  // 返回波形长度
    }

    /**
     * 设置波形长度
     * 
     * <p>波形长度变化时发送UI消息更新显示。
     * 
     * @param len 波形长度
     */
    public void setWaveLen(int len) {
        if (waveLen != len) {  // 检查是否变化
            ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);  // 发送采样频率UI消息
        }
        synchronized (this) {  // 同步锁
            waveLen = len;  // 设置波形长度
        }
    }

    /**
     * 设置采样率和插值系数
     * 
     * <p>采样率变化时更新FFT频率轴。
     * 
     * @param sampleRate 采样率
     * @param chaZhiNum 插值系数
     */
    public void setSampleRate(double sampleRate, int chaZhiNum) {
        if (this.sampleRate != sampleRate || this.chaZhiNum != chaZhiNum) {  // 检查是否变化
            this.sampleRate = sampleRate;  // 设置采样率
            this.chaZhiNum = chaZhiNum;  // 设置插值系数

            if (MathType == MathWave.MATH_FFTWAVE) {  // 检查是否为FFT类型
                // 采样率变化，发出通知
                horizontalAxisMath.genFFT_Axis();  // 生成FFT频率轴
                horizontalAxisMath.correctXPose();  // 校正X轴位置

                //ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);
            }
        }
    }

    /**
     * 通道采样变化处理
     * 
     * <p>发送通道采样消息和UI消息。
     */
    public void chSampleChange() {
        ScopeMessage.getInstance().sendChSample();  // 发送通道采样消息
        ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);  // 发送采样频率UI消息
    }

    /**
     * 计算范围
     * 
     * @param pointNum 点数
     * @param freqSolution 频率分辨率
     * @param tgtFreqScale 目标频率刻度
     * @return 范围值
     */
    private long GetRange(int pointNum, double freqSolution, double tgtFreqScale) {
        Logger.i(TAG, "freq:" + freqSolution + " tgtScale:" + tgtFreqScale + " pointNum:" + pointNum);  // 输出日志
        return (long) (pointNum / 2 * ScopeBase.getHorizonPerGridPixels() * freqSolution / tgtFreqScale);  // 计算范围
    }

    /**
     * 校正时间位置
     */
    public void correctTimePose() {
//        double radio=refTimeScaleVal_original/refTimeScaleVal;
//        if(refXPos < 0) {
//            //左移
//            int leftMosPix=refXEnd-ScopeBase.getWidth()/2;
//            leftMosPix *= radio;
//            if(-refXPos > leftMosPix){
//                refXPos = -leftMosPix;
//            }
//        } else {
//            //右移
//            int rightMosPix=ScopeBase.getWidth()/2-refXStart;
//            rightMosPix *= radio;
//            if(refXPos > rightMosPix){
//                refXPos = rightMosPix;
//            }
//        }
    }

    /** FFT直流分量值 */
    private double fftDCVal = 0;  // FFT直流分量
    
    /** FFT最大值 */
    private double fftMaxVal = 0;  // FFT最大值
    
    /** FFT最大频率 */
    private double fftMaxFreq = 0;  // FFT最大频率

    /**
     * 设置FFT直流分量值
     * 
     * @param fftDCVal 直流分量值
     */
    public void setFFTDCVal(double fftDCVal) {
        this.fftDCVal = fftDCVal;  // 设置直流分量值
    }

    /**
     * 设置FFT最大值
     * 
     * @param fftMaxVal 最大值
     */
    public void setFFTMaxVal(double fftMaxVal) {
        this.fftMaxVal = fftMaxVal;  // 设置最大值
    }

    /**
     * 设置FFT最大频率
     * 
     * @param fftMaxFreq 最大频率
     */
    public void setFFTMaxFreq(double fftMaxFreq) {
        this.fftMaxFreq = fftMaxFreq;  // 设置最大频率
    }

    /**
     * 获取FFT直流分量值
     * 
     * @return 直流分量值
     */
    public double getFFTDCVal() {
        return fftDCVal;  // 返回直流分量值
    }

    /**
     * 获取FFT最大值
     * 
     * @return 最大值
     */
    public double getFFTMaxVal() {
        return fftMaxVal;  // 返回最大值
    }

    /**
     * 获取FFT最大频率
     * 
     * @return 最大频率
     */
    public double getFFTMaxIdxFreq() {
        return fftMaxFreq;  // 返回最大频率
    }
//    if (getMathType() == MathWave.MATH_DUALWAVE) return -1;
//        return getFFTMaxIdx()*getSampleRate()/MathNative.CalFFTPointNum(getWaveLen());

    /**
     * 强制刷新
     */
    public void forceRefresh() {
        sendEvent(EventFactory.EVENT_MATH_REFRESH);  // 发送数学刷新事件
    }

    /**
     * 设置最大值
     * 
     * <p>根据最大值自动调整档位。
     * 
     * @param val 最大值
     */
    public void setMaxVal(double val) {
        int id = getMathExprWave().maxVal2VScaleId(val / (ScopeBase.getVerticalGridCnt() / 2 - 1));  // 计算档位ID
        if (isVScaleIdValid(id)) {  // 检查档位ID是否有效
            setVScaleId(id);  // 设置档位ID
        } else {
            Logger.d(TAG, "isVScaleIdValid id" + id + ",");  // 输出日志
        }
    }

    /**
     * 检查是否为微分运算
     * 
     * <p>检查表达式是否包含diff函数。
     * 
     * @return true表示是微分运算，false表示不是
     */
    public boolean isDifferential() {
        boolean bRet = false;  // 结果变量
        if (getMathType() == MathWave.MATH_EXPR) {  // 检查是否为表达式类型
            String exprString = getMathExprWave().getExprString();  // 获取表达式字符串
            for (int i = 0; i < ChannelFactory.getChNums(); i++) {  // 遍历通道
                if (exprString.contains("diff(ch" + (i + 1) + ")")) {  // 检查是否包含diff函数
                    bRet = true;  // 设置为微分运算
                    break;  // 退出循环
                }
            }
        }
        return bRet;  // 返回结果
    }

    /**
     * 获取数学通道索引
     * 
     * @return 数学通道索引（0-based）
     */
    public int getMathChIdx(){
        return getChId() - ChannelFactory.MATH1;  // 返回数学通道索引
    }


    /** 光标X1值 */
    private double x1Val,x2Val;  // 光标X1、X2值
    
    /** 光标X1有效标志 */
    private boolean bX1Valid = false;  // 光标X1有效标志
    
    /** 光标X2有效标志 */
    private boolean bX2Valid = false;  // 光标X2有效标志

    /**
     * 设置光标值
     * 
     * @param x1Val X1值
     * @param x2Val X2值
     */
    public synchronized void setCursorValue(double x1Val,double x2Val){
        this.x1Val = x1Val;  // 设置X1值
        this.x2Val = x2Val;  // 设置X2值
    }

    /**
     * 设置光标有效标志
     * 
     * @param bX1Valid X1有效标志
     * @param bX2Valid X2有效标志
     */
    public synchronized void setCursorValid(boolean bX1Valid,boolean bX2Valid){
        this.bX1Valid = bX1Valid;  // 设置X1有效标志
        this.bX2Valid = bX2Valid;  // 设置X2有效标志
    }

    /**
     * 获取光标X1值
     * 
     * @return X1值
     */
    public synchronized double getCursorX1Value(){
        return x1Val;  // 返回X1值
    }

    /**
     * 获取光标X2值
     * 
     * @return X2值
     */
    public synchronized double getCursorX2Value(){
        return x2Val;  // 返回X2值
    }

    /**
     * 检查光标X1是否有效
     * 
     * @return true表示有效，false表示无效
     */
    public synchronized boolean isCursorX1Valid(){
        return bX1Valid;  // 返回X1有效标志
    }

    /**
     * 检查光标X2是否有效
     * 
     * @return true表示有效，false表示无效
     */
    public synchronized boolean isCursorX2Valid(){
        return bX2Valid;  // 返回X2有效标志
    }
}
