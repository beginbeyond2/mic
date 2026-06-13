package com.micsig.tbook.scope.channel;  // 定义包名：示波器通道管理模块

import android.util.Log;  // 导入Log类：Android日志工具

import com.micsig.base.DoubleUtil;  // 导入DoubleUtil类：双精度浮点数工具
import com.micsig.base.FilterThread;  // 导入FilterThread类：防抖线程，避免频繁调用
import com.micsig.base.Logger;  // 导入Logger类：基础日志工具
import com.micsig.tbook.scope.Action.UiMessage;  // 导入UiMessage类：UI消息常量
import com.micsig.tbook.scope.Data.DataFactory;  // 导入DataFactory类：数据缓冲区工厂
import com.micsig.tbook.scope.Data.IDataBuffer;  // 导入IDataBuffer类：数据缓冲区接口
import com.micsig.tbook.scope.Data.ImageParam;  // 导入ImageParam类：图像渲染参数
import com.micsig.tbook.scope.Data.WaveData;  // 导入WaveData类：波形数据封装
import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理
import com.micsig.tbook.scope.ScopeBase;  // 导入ScopeBase类：示波器基类常量
import com.micsig.tbook.scope.ScopeMessage;  // 导入ScopeMessage类：消息处理中心
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入HorizontalAxis类：水平轴管理
import com.micsig.tbook.scope.horizontal.HorizontalAxisRef;  // 导入HorizontalAxisRef类：参考通道水平轴
import com.micsig.tbook.scope.surface.SurfaceDataRecv;  // 导入SurfaceDataRecv类：Surface数据接收
import com.micsig.tbook.scope.surface.SurfaceNative;  // 导入SurfaceNative类：Native层Surface渲染
import com.micsig.tbook.scope.surface.SurfacePreview;  // 导入SurfacePreview类：Surface预览层定义
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入VerticalAxis类：垂直轴管理
import com.micsig.tbook.scope.vertical.VerticalAxisMathDual;  // 导入VerticalAxisMathDual类：数学双波形垂直轴
import com.micsig.tbook.scope.vertical.VerticalAxisMathFft;  // 导入VerticalAxisMathFft类：FFT垂直轴

import java.io.File;  // 导入File类：文件操作
import java.nio.channels.FileChannel;  // 导入FileChannel类：文件通道，用于高效文件读取
import java.util.Observable;  // 导入Observable类：被观察者基类
import java.util.Observer;  // 导入Observer类：观察者接口

/**
 * 参考通道类 - 存储和显示历史波形数据的通道实现
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道管理模块）</li>
 *   <li>架构层级：数据层 - 参考通道实现</li>
 *   <li>设计模式：观察者模式 + 组合模式</li>
 *   <li>职责类型：波形存储、波形显示、时基管理、档位管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>存储历史波形数据（从文件加载或从其他通道保存）</li>
 *   <li>支持独立的时基和垂直档位设置</li>
 *   <li>支持三种波形类型（动态通道/数学双波形/FFT）</li>
 *   <li>实现防抖绘制机制，避免频繁刷新</li>
 *   <li>监听Surface生命周期事件</li>
 * </ul>
 * 
 * <p><b>参考通道架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   RefChannel - 参考通道                                                  │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   波形类型（refType）                                             │   │
 * │   │                                                                   │   │
 * │   │   refType=0: 动态通道波形                                         │   │
 * │   │       ├── 来源：CH1-CH8任意通道                                   │   │
 * │   │       ├── 档位：使用VerticalAxis档位表                            │   │
 * │   │       └── 探头：支持探头衰减系数                                   │   │
 * │   │                                                                   │   │
 * │   │   refType=1: 数学通道-双波形运算                                  │   │
 * │   │       ├── 来源：MathChannel双波形运算结果                         │   │
 * │   │       ├── 档位：使用VerticalAxisMathDual档位表                    │   │
 * │   │       └── 探头：固定为1.0                                         │   │
 * │   │                                                                   │   │
 * │   │   refType=2: 数学通道-FFT运算                                     │   │
 * │   │       ├── 来源：MathChannel FFT运算结果                           │   │
 * │   │       ├── 档位：使用VerticalAxisMathFft档位表                     │   │
 * │   │       ├── 显示：支持线性(V)和对数(dB)模式                         │   │
 * │   │       └── 探头：固定为1.0                                         │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   时基管理                                                        │   │
 * │   │                                                                   │   │
 * │   │   refTimeScaleVal_original: 原始时基（保存时的时基）              │   │
 * │   │   refTimeScaleVal: 当前显示时基（可独立调整）                     │   │
 * │   │   refXPos_original: 原始X位置（保存时的位置）                     │   │
 * │   │   refXPos: 当前X偏移（可独立调整）                                │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   绘制机制                                                        │   │
 * │   │                                                                   │   │
 * │   │   FilterThread ───→ 防抖绘制（避免频繁刷新）                      │   │
 * │   │        │                                                          │   │
 * │   │        └──→ drawRef() ───→ SurfaceNative.drawSurface()           │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>参考通道索引分配：</b>
 * <pre>
 *   REF1 = 16（ChannelFactory.REF1）
 *   REF2 = 17
 *   REF3 = 18
 *   ...
 *   REF8 = 23（ChannelFactory.REF8）
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   BaseChannel（通道基类）
 *       │
 *       └── RefChannel（参考通道）
 *               │
 *               ├── 实现：Observer（观察者接口）
 *               ├── 组合：RefChannelAction（动作代理）
 *               ├── 组合：FilterThread（防抖线程）
 *               ├── 组合：HorizontalAxisRef（水平轴管理）
 *               └── 组合：SurfaceNative（Native渲染）
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户保存当前波形到参考通道，用于后续对比分析</li>
 *   <li>用户从文件加载历史波形数据到参考通道</li>
 *   <li>参考通道可独立调整时基和垂直档位，不影响其他通道</li>
 *   <li>支持与实时波形叠加显示，便于对比分析</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：BaseChannel（通道基类）</li>
 *   <li>实现：Observer（观察者接口，监听Surface事件）</li>
 *   <li>组合：RefChannelAction（动作代理）</li>
 *   <li>组合：FilterThread（防抖线程）</li>
 *   <li>组合：HorizontalAxisRef（水平轴管理）</li>
 *   <li>组合：SurfaceNative（Native渲染）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 *   <li>依赖：ScopeMessage（消息处理中心）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/14
 * @see BaseChannel 通道基类
 * @see RefChannelAction 参考通道动作代理
 * @see HorizontalAxisRef 参考通道水平轴
 * @see SurfaceNative Native渲染
 */
public class RefChannel extends BaseChannel implements Observer {  // 继承BaseChannel，实现Observer观察者接口

    /** 日志标签 */
    private static final String TAG = "RefChannel";  // 日志输出标签
    
    /** 防抖线程：避免频繁调用drawRef() */
    private FilterThread refThread;  // 防抖线程，延迟执行绘制操作
    
    /** 图像渲染参数：包含绘制所需的各种参数 */
    private ImageParam refParam = new ImageParam();  // 图像渲染参数封装
    
    /** Native层Surface渲染器：用于绘制波形 */
    private SurfaceNative surfaceNative = null;  // Native层渲染器，初始化为null
    
    /** 参考通道动作代理：处理状态变化消息发送 */
    private RefChannelAction refChannelAction;  // 动作代理实例
    
    /** 参考通道水平轴管理器：管理时基档位 */
    private HorizontalAxisRef horizontalAxisRef;  // 水平轴管理器

    /** 参考波形类型：0=动态通道，1=数学双波形，2=数学FFT */
    private int refType;  // 波形类型标识
    
    /** 当前X位置偏移（像素）：相对于原始触发位置的移动量 */
    private long refXPos = 0;  // X位置偏移，初始为0
    
    /** 原始X位置（保存时的位置）：用于计算相对偏移 */
    private long refXPos_original;  // 原始X位置
    
    /** 延迟位置：用于延迟触发显示 */
    private long refDelayPos;  // 延迟位置
    
    /** 波形起始X坐标：波形数据的起始位置 */
    private int refXStart;  // 起始X坐标
    
    /** 波形结束X坐标：波形数据的结束位置 */
    private int refXEnd;  // 结束X坐标
    
    /** Y位置：波形的垂直位置 */
    private double refYPos;  // Y位置
    
    /** 原始时基值：保存波形时的时基档位值 */
    private double refTimeScaleVal_original;  // 原始时基值
    
    /** 当前时基值：显示时的时基档位值，可独立调整 */
    private double refTimeScaleVal;  // 当前时基值
    
    /** 垂直档位值：每格的电压值 */
    private double refVScaleVal;  // 垂直档位值
    
    /** 微调系数：档位微调系数，范围0.1~2.5 */
    private double refFineVal = 1.0;  // 微调系数，默认1.0
    
    /** 探头类型：线性或对数(dB) */
    private int refProbeType;  // 探头类型
    
    /** 探头字符串：探头描述信息 */
    private String refProbeStr;  // 探头字符串
    
    /** 探头衰减系数：如1X、10X等 */
    private double refProbeRate;  // 探头衰减系数
    
    /** 采样率：波形数据的采样率 */
    private double refSampleRate;  // 采样率
    
    /** 波形长度：波形数据点数 */
    private int refWaveLength;  // 波形长度

    /**
     * 构造方法：初始化参考通道
     * 
     * <p>执行以下初始化操作：
     * <ol>
     *   <li>调用父类构造方法，设置通道名称</li>
     *   <li>创建动作代理实例</li>
     *   <li>创建防抖线程</li>
     *   <li>初始化渲染参数</li>
     *   <li>注册Surface事件观察者</li>
     *   <li>创建水平轴管理器</li>
     *   <li>分配数据缓冲区</li>
     * </ol>
     * 
     * @param chIdx 通道索引（ChannelFactory.REF1~REF8）
     */
    public RefChannel(int chIdx) {
        super(chIdx,"REF" + (chIdx - ChannelFactory.REF1 + 1));  // 调用父类构造方法，设置通道名称为REF1~REF8
        refChannelAction = new RefChannelAction(this);  // 创建动作代理实例
        refThread = new FilterThread(TAG);  // 创建防抖线程
        refThread.setRunnable(new Runnable() {  // 设置防抖线程的执行任务
            @Override
            public void run() {
                drawRef();  // 执行绘制操作
            }
        });
        InitRefParam();  // 初始化渲染参数
        EventFactory.addEventObserver(EventFactory.EVENT_SURFACE_CREATED,this);  // 注册Surface创建事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_SURFACE_DESTROYED,this);  // 注册Surface销毁事件观察者

        horizontalAxisRef = new HorizontalAxisRef();  // 创建水平轴管理器
        setBuffer(DataFactory.allocateBufferQueue(true));  // 分配数据缓冲区队列，true表示参考通道
    }

    /**
     * 初始化参考通道渲染参数
     * 
     * <p>设置默认的渲染参数：
     * <ul>
     *   <li>每像素字节数：4（ARGB格式）</li>
     *   <li>背景色：透明（0）</li>
     *   <li>前景色：粉红色（0xFFE17D7D）</li>
     *   <li>宽高：使用ScopeBase定义的屏幕尺寸</li>
     * </ul>
     */
    private void InitRefParam(){

        refParam.setPerPixelByte(4);  // 设置每像素4字节（ARGB格式）
        refParam.setBackgroundColor(0);  // 设置背景色为透明
        refParam.setForegroundColor(0xFFE17D7D);  // 设置前景色为粉红色
        refParam.setWidth(ScopeBase.getWidth());  // 设置宽度为屏幕宽度
        refParam.setHeight(ScopeBase.getHeight());  // 设置高度为屏幕高度
        refParam.setXOffset(0);  // 设置X偏移为0
        refParam.setYOffset((int) Math.round(this.getPos()));  // 设置Y偏移为当前通道位置
    }

    /**
     * 设置前景色
     * 
     * <p>将RGB颜色值转换为BGR格式（用于Native渲染），并触发重绘。
     * 
     * @param colorValue ARGB颜色值
     */
    public void setForegroundColor(int colorValue) {
        colorValue = (colorValue & 0xFF000000)  // 保留Alpha通道
                | ((colorValue & 0xFF) << 16)  // R通道移到B位置
                | ((colorValue & 0xFF00))  // G通道保持不变
                | ((colorValue & 0xFF0000) >> 16);  // B通道移到R位置
        refParam.setForegroundColor(colorValue);  // 设置前景色
        drawRef();  // 触发重绘
    }
    
    /**
     * 打开通道
     * 
     * <p>设置打开状态，发送采样频率UI消息，发送通道打开事件。
     */
    @Override
    public void open() {
        setOpen(true);  // 设置打开状态
        ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);  // 发送采样频率UI消息
        sendEvent(EventFactory.EVENT_CHANNEL_OPEN, true);  // 发送通道打开事件
    }

    /**
     * 关闭通道
     * 
     * <p>设置关闭状态，发送采样频率UI消息，发送通道关闭事件。
     */
    @Override
    public void close() {
        setOpen(false);  // 设置关闭状态
        ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);  // 发送采样频率UI消息
        sendEvent(EventFactory.EVENT_CHANNEL_CLOSE);  // 发送通道关闭事件
    }

    /**
     * 激活通道
     * 
     * <p>发送激活事件和档位事件，如果通道已打开则发送采样频率和窗口显示UI消息。
     */
    @Override
    public void activate() {
        sendEvent(EventFactory.EVENT_CHANNEL_ACTIVE,true);  // 发送激活事件
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE,true);  // 发送档位事件
        if (isOpen()) {  // 检查通道是否已打开
            ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);  // 发送采样频率UI消息
            ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_SIMPLE_WIN_DIS);  // 发送窗口显示UI消息
        }
    }

    /**
     * 获取每像素垂直单位
     * 
     * <p>计算每像素对应的电压值，考虑Zoom模式。
     * 
     * @return 每像素垂直单位值
     */
    @Override
    public double getVerticalPerPix() {
        double val = refVScaleVal * refFineVal;  // 计算实际档位值
        double h = ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff();  // 获取每格像素数
        if (Scope.getInstance().isZoom()) {  // 检查是否为Zoom模式
            h = ScopeBase.getZoomVerticalPerGridPixels() * ScopeBase.getToUICoff();  // 使用Zoom模式像素数
        }
        return val / h;  // 返回每像素垂直单位
    }

    /**
     * 获取AD每像素对应幅值
     * 
     * <p>计算AD采样每像素对应的电压值。
     * 
     * @return AD每像素幅值
     */
    @Override
    public double getADVerticalPerPix() {
        double val = refVScaleVal * refFineVal;  // 计算实际档位值
        double h = ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff();  // 获取每格像素数
        return val / h;  // 返回AD每像素幅值
    }

    /**
     * 获取采样率
     * 
     * <p>从波形数据缓冲区获取采样率。
     * 
     * @return 采样率，默认返回1.0
     */
    @Override
    public double getSampleRate() {
        double fs = 1.0;  // 默认采样率
        WaveData dataBuffer = (WaveData)obtain();  // 获取波形数据缓冲区
        if(dataBuffer != null){
            fs = dataBuffer.getSampRate();  // 获取采样率
            recycle(dataBuffer);  // 回收缓冲区
        }
        return fs;  // 返回采样率
    }

    /**
     * 获取显示用采样率
     * 
     * <p>从波形数据缓冲区获取显示用采样率。
     * 
     * @return 显示用采样率，默认返回1.0
     */
    @Override
    public double getSampleRate2display() {
        double fs = 1.0;  // 默认采样率
        WaveData dataBuffer = (WaveData) obtain();  // 获取波形数据缓冲区
        if (dataBuffer != null) {
            fs = dataBuffer.getSampRate2display();  // 获取显示用采样率
            recycle(dataBuffer);  // 回收缓冲区
        }
        return fs;  // 返回显示用采样率
    }

    /**
     * 获取参考波形类型
     * 
     * @return 波形类型：0=动态通道，1=数学双波形，2=数学FFT
     */
    public int getRefType(){
        return refType;  // 返回波形类型
    }
    
    /**
     * 获取探头衰减系数
     * 
     * @return 探头衰减系数
     */
    public double getRefProbeRate(){
        return refProbeRate;  // 返回探头衰减系数
    }
    
    /**
     * 获取档位ID
     * 
     * <p>根据波形类型使用不同的档位表获取档位ID。
     * 
     * @return 档位ID
     */
    public int getVScaleId() {
        int VScaleId = 0;  // 档位ID
        switch (refType) {  // 根据波形类型选择档位表
            default:
            case 0://dynamic ch - 动态通道
            {
                double val = refVScaleVal / refProbeRate;  // 计算实际电压值（考虑探头衰减）
                VScaleId = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVScaleId(val);  // 使用动态通道档位表
                break;
            }
            case 1://Math ch:Dual - 数学双波形
                VScaleId = VerticalAxisMathDual.getScaleIdByValue(refVScaleVal);  // 使用数学双波形档位表
                break;
            case 2://Math ch:FFT - 数学FFT
                VScaleId = refProbeType == VerticalAxis.PROBE_TYPE_DB ?  // 检查是否为dB模式
                        VerticalAxisMathFft.getScaleIdInDb(refVScaleVal) :  // dB模式档位表
                        VerticalAxisMathFft.getScaleIdInRms(refVScaleVal);  // RMS模式档位表
                break;
        }
        return VScaleId;  // 返回档位ID
    }

    /**
     * 获取最大档位ID
     * 
     * <p>根据波形类型返回对应的最大档位ID。
     * 
     * @return 最大档位ID
     */
    public int getVScaleIdMax() {
        int VScaleIdMax = VerticalAxis.getMaxGear();  // 默认使用动态通道最大档位
        switch (refType) {  // 根据波形类型选择
            case 0:  // 动态通道
                VScaleIdMax = VerticalAxis.getMaxGear();  // 动态通道最大档位
                break;
            case 1:  // 数学双波形
                VScaleIdMax =VerticalAxisMathDual.DANG_DUAL_MAX;  // 数学双波形最大档位
                break;
            case 2:  // 数学FFT
                VScaleIdMax = refProbeType == VerticalAxis.PROBE_TYPE_DB ?  // 检查是否为dB模式
                        VerticalAxisMathFft.DANG_DBV_MAX :  // dB模式最大档位
                        VerticalAxisMathFft.DANG_RMS_MAX;  // RMS模式最大档位
                break;
        }
        return VScaleIdMax;  // 返回最大档位ID
    }

    /**
     * 获取最小档位ID
     * 
     * <p>根据波形类型返回对应的最小档位ID。
     * 
     * @return 最小档位ID
     */
    public int getVScaleIdMin() {
        int VScaleIdMin = VerticalAxis.getMinGear();  // 默认使用动态通道最小档位
        switch (refType) {  // 根据波形类型选择
            case 0:  // 动态通道
                VScaleIdMin = VerticalAxis.getMinGear();  // 动态通道最小档位
                break;
            case 1:  // 数学双波形
                VScaleIdMin =VerticalAxisMathDual.DANG_DUAL_MIN;  // 数学双波形最小档位
                break;
            case 2:  // 数学FFT
                VScaleIdMin = refProbeType == VerticalAxis.PROBE_TYPE_DB ?  // 检查是否为dB模式
                        VerticalAxisMathFft.DANG_DBV_MIN :  // dB模式最小档位
                        VerticalAxisMathFft.DANG_RMS_MIN;  // RMS模式最小档位
                break;
        }
        return VScaleIdMin;  // 返回最小档位ID
    }

    /**
     * 设置档位ID
     * 
     * <p>根据波形类型使用不同的档位表设置档位值，并触发重绘。
     * 
     * @param scaleId 档位ID
     */
    public void setVScaleId(int scaleId) {
        switch (refType) {  // 根据波形类型选择档位表
            case 0:  // 动态通道
                refFineVal = 1.0;  // 重置微调系数
                refVScaleVal = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVScaleIdVal(scaleId);  // 获取档位值
                refVScaleVal *= refProbeRate;  // 乘以探头衰减系数
                break;
            case 1:  // 数学双波形
                refFineVal = 1.0;  // 重置微调系数
                refVScaleVal = VerticalAxisMathDual.getScaleIdValById(scaleId);  // 获取数学双波形档位值
                break;
            case 2:  // 数学FFT
                refFineVal = 1.0;  // 重置微调系数
                refVScaleVal = refProbeType == VerticalAxis.PROBE_TYPE_DB ?  // 检查是否为dB模式
                        VerticalAxisMathFft.dbvIdToValue(scaleId) :  // dB模式档位值
                        VerticalAxisMathFft.rmsIdToValue(scaleId);  // RMS模式档位值

                break;
        }
        drawRef();  // 触发重绘
        super.setVScaleId(scaleId);  // 调用父类方法
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE,true);  // 发送档位变化事件
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
        double fineVal = refFineVal;  // 获取微调系数
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
     * 设置档位值
     * 
     * <p>根据波形类型查找最接近的档位ID并设置，支持微调系数。
     * 
     * @param vScaleVal 档位值
     */
    public void setVScaleVal(double vScaleVal){

        vScaleVal /= refProbeRate;  // 除以探头衰减系数
        int scaleId = -1;  // 档位ID
        switch (refType){  // 根据波形类型选择档位表
            case 0: {  // 动态通道
                double v;
                scaleId = VerticalAxis.getMinGear();  // 获取最小档位ID
                v = VerticalAxis.getScaleIdValById(scaleId);  // 获取档位值
                for (int i = VerticalAxis.getMinGear(); i <= VerticalAxis.getMaxGear(); i++) {  // 遍历档位
                    scaleId = i;  // 更新档位ID
                    v = VerticalAxis.getScaleIdValById(scaleId);  // 获取档位值
                    if (vScaleVal <= v || DoubleUtil.FuzzyCompare(vScaleVal, v)) {  // 检查是否匹配
                        break;  // 找到后退出循环
                    }
                }
                refFineVal = vScaleVal / v;  // 计算微调系数
                refVScaleVal = v * refProbeRate;  // 计算实际档位值
            }

            break;
            case 1: {  // 数学双波形
                    double v;
                    scaleId = VerticalAxisMathDual.DANG_DUAL_MIN;  // 获取最小档位ID
                    v = VerticalAxisMathDual.getScaleIdValById(scaleId);  // 获取档位值
                    int i = scaleId;
                    int m = VerticalAxisMathDual.DANG_DUAL_MAX;  // 获取最大档位ID
                    for (; i <= m; i++) {  // 遍历档位
                        scaleId = i;  // 更新档位ID
                        v = VerticalAxisMathDual.getScaleIdValById(scaleId);  // 获取档位值
                        if (vScaleVal <= v || DoubleUtil.FuzzyCompare(vScaleVal, v)) {  // 检查是否匹配
                            break;  // 找到后退出循环
                        }
                    }
                    refFineVal = vScaleVal / v;  // 计算微调系数
                    refVScaleVal = v * refProbeRate;  // 计算实际档位值
            }
                break;
            case 2: {  // 数学FFT

                double v;
                int i,m;
                if(refProbeType == VerticalAxis.PROBE_TYPE_DB){  // 检查是否为dB模式
                    scaleId = VerticalAxisMathFft.DANG_DBV_MIN;  // dB模式最小档位ID
                    m = VerticalAxisMathFft.DANG_DBV_MAX;  // dB模式最大档位ID
                    v = VerticalAxisMathFft.dbvIdToValue(scaleId);  // 获取档位值
                }else{  // RMS模式
                    scaleId = VerticalAxisMathFft.DANG_RMS_MIN;  // RMS模式最小档位ID
                    m = VerticalAxisMathFft.DANG_RMS_MAX;  // RMS模式最大档位ID
                    v = VerticalAxisMathFft.rmsIdToValue(scaleId);  // 获取档位值
                }
                i = scaleId;
                for (; i <= m; i++) {  // 遍历档位
                    scaleId = i;  // 更新档位ID
                    v = refProbeType == VerticalAxis.PROBE_TYPE_DB ?  // 检查是否为dB模式
                            VerticalAxisMathFft.dbvIdToValue(scaleId) :  // dB模式档位值
                            VerticalAxisMathFft.rmsIdToValue(scaleId);  // RMS模式档位值
                    if (vScaleVal <= v || DoubleUtil.FuzzyCompare(vScaleVal, v)) {  // 检查是否匹配
                        break;  // 找到后退出循环
                    }
                }
                refFineVal = vScaleVal / v;  // 计算微调系数
                refVScaleVal = v * refProbeRate;  // 计算实际档位值
            }
            break;
        }
        drawRef();  // 触发重绘
        if(scaleId > 0) {  // 检查档位ID是否有效
            super.setVScaleId(scaleId);  // 调用父类方法
        }
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE,true);  // 发送档位变化事件
    }

    /**
     * 检查档位ID是否有效
     * 
     * <p>参考通道档位ID有效性检查，当前始终返回false。
     * 
     * @param vScaleId 档位ID
     * @return 始终返回false
     */
    @Override
    public boolean isVScaleIdValid(int vScaleId) {
        return false;  // 参考通道不进行档位ID有效性检查
    }

    /**
     * 获取档位值
     * 
     * <p>返回实际档位值（档位值 * 微调系数）。
     * 
     * @return 档位值
     */
    public double getVScaleVal() {
        return refVScaleVal * refFineVal;  // 返回实际档位值
    }
    
    /**
     * 获取微调系数
     * 
     * <p>返回考虑探头衰减的微调系数。
     * 
     * @return 微调系数
     */
    public double getFineScale(){
        return refFineVal * 1/ refProbeRate;  // 返回微调系数（考虑探头衰减）
    }
    
    /**
     * 获取档位值
     * 
     * <p>与getVScaleVal()相同，返回实际档位值。
     * 
     * @return 档位值
     */
    public double getVScaleIdVal() {
        return getVScaleVal();  // 返回档位值
    }
    
    /**
     * 获取探头类型
     * 
     * @return 探头类型（线性或对数）
     */
    public int getProbeType() {
        return refProbeType;  // 返回探头类型
    }
    
    /**
     * 获取探头字符串
     * 
     * @return 探头描述字符串
     */
    public String getProbeStr(){
        return refProbeStr;  // 返回探头字符串
    }
    
    /**
     * 获取参考时基ID（绝对坐标）
     * 
     * <p>使用当前时基值获取时基ID。
     * 
     * @return 时基ID
     */
    public int getRefTimeScaleId() {
        return horizontalAxisRef.timeScaleId(refTimeScaleVal,refType);  // 返回时基ID
    }
    
    /**
     * 获取参考时基ID（相对坐标/界面index）
     * 
     * <p>使用原始时基和当前时基获取时基ID。
     * 
     * @return 时基ID
     */
    public int getRefTimeScaleId_ui() {
        return horizontalAxisRef.timeScaleId_ui(refTimeScaleVal_original,refTimeScaleVal,refType);  // 返回时基ID
    }
    
    /**
     * 获取参考时基值
     * 
     * @return 当前时基值
     */
    public double getRefTimeScaleVal() {
        return refTimeScaleVal;  // 返回当前时基值
    }
    
    /**
     * 获取每像素时间值
     * 
     * <p>根据是否跟随通道时基，返回对应的每像素时间值。
     * 
     * @return 每像素时间值
     */
    public double getRefTimePerPix(){
        if (HorizontalAxis.getInstance().getScaleFollowingCh()) {  // 检查是否跟随通道时基
            return HorizontalAxis.getInstance().getTimesPrePix();  // 返回主时基每像素时间
        } else {
            return refTimeScaleVal/ScopeBase.getHorizonPerGridPixels();  // 返回参考通道每像素时间
        }
    }

    /**
     * 设置通道位置
     * 
     * <p>将UI坐标转换为FPGA坐标，触发重绘，发送位置变化事件。
     * 
     * @param pos 位置值（UI坐标）
     */
    @Override
    public void setPos(double pos) {
        pos = pos * ScopeBase.getToFPGACoff();  // 转换为FPGA坐标
        super.setPos(pos);  // 调用父类方法
        drawRef();  // 触发重绘
        sendEvent(EventFactory.EVENT_REF_VPOS,true);  // 发送参考通道位置事件
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
        EventFactory.sendEvent(new EventBase(eventId, getChId()), async);  // 发送事件
    }

    /**
     * 设置视图X位置（像素）
     * 
     * <p>根据视图像素位置计算参考通道的X偏移。
     * 
     * @param pos 视图像素位置
     */
    public void setXPosOfViewPix(long pos){
        long pos1 = HorizontalAxis.getInstance().getTimePoseOfGrid(refTimeScaleVal, refXPos_original);  // 获取原始位置
        refXPos = pos1-pos;  // 计算偏移
        Logger.i("RefChannel= ", getName() + " ,ref now pix=" + pos + ", original pix=" + pos1 + ", mov pix=" + refXPos);  // 输出日志
        drawRef();  // 触发重绘
        ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_SIMPLE_WIN_DIS);  // 发送窗口显示UI消息
    }

    /**
     * 获取延迟视图像素位置
     * 
     * @return 延迟视图像素位置
     */
    public long getTimeDelayOfViewPix() {
        return HorizontalAxis.getInstance().getTimePoseOfGrid(refTimeScaleVal, refDelayPos);  // 返回延迟视图像素位置
    }

    /**
     * 获取时间位置视图像素
     * 
     * @param timePos 时间位置
     * @return 视图像素位置
     */
    public long getTimePoseOfViewPix(long timePos) {
        return HorizontalAxis.getInstance().getTimePoseOfGrid(refTimeScaleVal, timePos);  // 返回视图像素位置
    }

    /**
     * 获取当前时间位置视图像素
     * 
     * @return 当前视图像素位置
     */
    public long getTimePoseOfViewPix(){
        return HorizontalAxis.getInstance().getTimePoseOfGrid(refTimeScaleVal, refXPos_original) - refXPos;  // 返回当前视图像素位置
    }

    /**
     * 获取原始时间位置视图像素（原始时基）
     * 
     * @return 原始视图像素位置
     */
    public long getTimePoseOfViewPix_original_originalScale(){
        return HorizontalAxis.getInstance().getTimePoseOfGrid(refTimeScaleVal_original, refXPos_original);  // 返回原始视图像素位置
    }

    /**
     * 获取原始时间位置视图像素（当前时基）
     * 
     * @return 原始视图像素位置（当前时基）
     */
    public long getTimePoseOfViewPix_original_nowScale(){
        return HorizontalAxis.getInstance().getTimePoseOfGrid(refTimeScaleVal, refXPos_original);  // 返回原始视图像素位置
    }

    /**
     * 获取时间位置（时间值）
     * 
     * @return 时间位置
     */
    public long getTimePosOfView(){
        return refXPos_original-HorizontalAxis.getInstance().getTimePose(refTimeScaleVal, refXPos);  // 返回时间位置
    }

    /**
     * 获取时间位置（指定像素）
     * 
     * @param posPix 像素位置
     * @return 时间位置
     */
    public long getTimePosOfView(int posPix) {
        return HorizontalAxis.getInstance().getTimePose(refTimeScaleVal, posPix);  // 返回时间位置
    }

    /**
     * 获取参考移动像素
     * 
     * @return X偏移像素值
     */
    public  long getRefMovPix(){
        return refXPos;  // 返回X偏移像素值
    }


    /**
     * 校正时间位置
     * 
     * <p>当时基变化时，校正X位置，确保波形不会超出显示范围。
     */
    public void correctTimePose(){
        double radio=refTimeScaleVal_original/refTimeScaleVal;  // 计算时基比例
        if(refXPos < 0){  // 左移
            int leftMosPix=refXEnd-ScopeBase.getWidth()/2;  // 计算左边界
            leftMosPix *= radio;  // 按比例调整
            if(-refXPos > leftMosPix){  // 检查是否超出边界
                refXPos = -leftMosPix;  // 限制到边界
            }
        }
        else{  // 右移
            int rightMosPix=ScopeBase.getWidth()/2-refXStart;  // 计算右边界
            rightMosPix *= radio;  // 按比例调整
            if(rightMosPix > 0) {  // 检查右边界是否有效
                if (refXPos > rightMosPix) {  // 检查是否超出边界
                    refXPos = rightMosPix;  // 限制到边界
                }
            }
        }
    }

    /**
     * 获取原始X位置像素
     * 
     * @return 原始X位置像素
     */
    public long getXPos_pix_original(){
        return HorizontalAxis.getInstance().getTimePoseOfGrid(refTimeScaleVal_original, refXPos_original);  // 返回原始X位置像素
    }

    /**
     * 获取波形长度
     * 
     * @return 波形长度（点数）
     */
    public int getWaveLen(){
        int len=0;  // 波形长度
        WaveData dataBuffer = (WaveData)obtain();  // 获取波形数据缓冲区
        if(dataBuffer != null){
            len = dataBuffer.getWaveLength();  // 获取波形长度
            recycle(dataBuffer);  // 回收缓冲区
        }
        return len;  // 返回波形长度
    }

    /**
     * 获取水平轴管理器
     * 
     * @return HorizontalAxisRef实例
     */
    public HorizontalAxisRef getHorizontalAxisRef() {
        return horizontalAxisRef;  // 返回水平轴管理器
    }


    /**
     * 获取屏幕数
     * 
     * <p>计算原始时基与当前时基的比例，表示波形占用的屏幕数。
     * 
     * @return 屏幕数
     */
    public double getScreenNum(){
        return (double)(refTimeScaleVal_original/refTimeScaleVal);  // 返回屏幕数
    }

    /**
     * 设置参考时基
     * 
     * @param timeScale 时基值
     */
    public void setRefTimeScale(double timeScale) {
        setRefTimeScale(timeScale,0);  // 调用双参数版本
    }

    /**
     * 设置参考时基（带位置偏移）
     * 
     * <p>更新时基值，计算新的X位置偏移，触发重绘。
     * 
     * @param timeScale 时基值
     * @param pos 位置偏移
     */
    public void setRefTimeScale(double timeScale, int pos) {
        refXPos *= refTimeScaleVal / timeScale;  // 按比例调整X位置
        refXPos += pos;  // 添加位置偏移
        double tempScaleVal = refTimeScaleVal;  // 保存旧时基值
        refTimeScaleVal = timeScale;  // 更新时基值
        long temp = getTimePoseOfViewPix_original_nowScale();  // 计算原始位置
        boolean needFix = Math.abs(temp) < 1000 && Math.abs(refXPos - temp) <= 1;  // 检查是否需要修正
        if (pos == 0 && temp != 0 && (Math.abs(refXPos - temp) * 1.0 / Math.abs(temp) <= 1E-3 || needFix) && tempScaleVal != timeScale) {  // 检查是否需要修正
            refXPos = temp;  // 修正X位置
        }
        drawRef();  // 触发重绘
        ScopeMessage.getInstance().sendUiMsg(UiMessage.UI_MESSAGE_SIMPLE_WIN_DIS);  // 发送窗口显示UI消息
    }

    /**
     * 从文件通道加载波形
     * 
     * @param fileChannel 文件通道
     * @return true表示加载成功，false表示加载失败
     */
    public boolean loadWave(FileChannel fileChannel){
        boolean bret = false;  // 返回值
        IDataBuffer dataBuffer = obtain();  // 获取数据缓冲区
        if(dataBuffer != null){
            bret = dataBuffer.load(fileChannel);  // 从文件通道加载数据
            recycle(dataBuffer);  // 回收缓冲区
        }
        if(bret){
            loadWave();  // 加载成功后执行后续处理
        }
        return bret;  // 返回结果
    }
    
    /**
     * 加载波形完成处理
     * 
     * <p>调用动作代理加载波形，加载配置，触发重绘。
     */
    public void loadWave(){
        Log.d(TAG, "loadWave() called");  // 输出日志
        refChannelAction.loadWave();  // 调用动作代理加载波形
        LoadRefConfig();  // 加载参考通道配置
        drawRef();  // 触发重绘
    }

    /**
     * 从文件路径加载波形
     * 
     * @param pathName 文件路径
     * @return true表示加载成功，false表示加载失败
     */
    public boolean loadWave(String pathName){
        Log.d(TAG, "loadWave() called with: pathName = [" + pathName + "]");  // 输出日志

        boolean bret = false;  // 返回值
        File file = new File(pathName);  // 创建文件对象
        if(!file.exists()) {  // 检查文件是否存在
            return bret;  // 文件不存在返回false
        }
        IDataBuffer dataBuffer = obtain();  // 获取数据缓冲区
        if(dataBuffer != null){
            bret = dataBuffer.load(pathName);  // 从文件加载数据
            recycle(dataBuffer);  // 回收缓冲区
        }
        if(bret){
            loadWave();  // 加载成功后执行后续处理
        }
        return bret;  // 返回结果
    }

    /**
     * 加载参考通道配置
     * 
     * <p>从波形数据中读取配置信息，包括：
     * <ul>
     *   <li>波形类型</li>
     *   <li>X位置和范围</li>
     *   <li>Y位置</li>
     *   <li>档位值</li>
     *   <li>探头信息</li>
     *   <li>采样率和波形长度</li>
     *   <li>时基值</li>
     * </ul>
     */
    public void LoadRefConfig(){
        WaveData dataBuffer = (WaveData)obtain();  // 获取波形数据缓冲区
        if(dataBuffer != null){
            refType = dataBuffer.getWaveType();  // 获取波形类型
            refXPos_original = dataBuffer.getXPos();  // 获取原始X位置
            refXPos = 0;  // 重置X偏移
            refXStart = dataBuffer.getStartX();  // 获取起始X坐标
            refXEnd = dataBuffer.getEndX();  // 获取结束X坐标
            refYPos = dataBuffer.getYPosition();  // 获取Y位置
            Log.d("LoadRefConfig", "channel= " + getName() + " ,refXStart:" + refXStart + ",refXEnd:" + refXEnd + ",refXPos_original:" + refXPos_original + " ,refYPos= " + refYPos);  // 输出日志
            refVScaleVal = dataBuffer.getVScaleVal();  // 获取档位值
            refProbeType = dataBuffer.getProbeType();  // 获取探头类型
            refProbeStr = dataBuffer.getProbeStr();  // 获取探头字符串
            Logger.i("LoadRefConfig channel= " + getName() + " ,refProbeType= " + refProbeType + " ,refProbeStr= " + refProbeStr + " ,label= " + dataBuffer.getLabel());  // 输出日志
            refProbeRate = dataBuffer.getProbeRate();  // 获取探头衰减系数
            refSampleRate = dataBuffer.getSampRate2display();  // 获取显示用采样率
            refWaveLength = dataBuffer.getWaveLength();  // 获取波形长度
            int memDepthSet = dataBuffer.getMemDepthSet();  // 获取存储深度设置
            int memDepthItemIdx = dataBuffer.getMemDepthItemIdx();  // 获取存储深度索引
            super.setLabel(dataBuffer.getLabel());  // 设置标签
            recycle(dataBuffer);  // 回收缓冲区
            setPos(refYPos);  // 设置Y位置（同步到MathRefWave）
            super.setPos(refYPos);  // 调用父类方法
            horizontalAxisRef.setRefType(refType);  // 设置水平轴类型
            horizontalAxisRef.setMemDepthSet(memDepthSet);  // 设置存储深度
            horizontalAxisRef.setMemDepthItemIdx(memDepthItemIdx);  // 设置存储深度索引
            horizontalAxisRef.setSampleRate(refSampleRate);  // 设置采样率
            horizontalAxisRef.setWaveLength(refWaveLength);  // 设置波形长度

            refTimeScaleVal_original = dataBuffer.getTimeScaleVal();  // 获取原始时基值
            horizontalAxisRef.generateHorizontalAxis(refTimeScaleVal_original);  // 生成水平轴列表
            if(refType == 2) {  // FFT类型
                refTimeScaleVal = horizontalAxisRef.getxAxis().get(0);  // 使用第一个时基值
            }
            else
                refTimeScaleVal = refTimeScaleVal_original;  // 使用原始时基值
            refFineVal = 1.0;  // 重置微调系数
            if(refType > 0){  // 数学通道
                refProbeRate = 1.0;  // 探头衰减系数固定为1.0
            }
            Log.d(TAG, "LoadRefConfig() called refVScaleVal:" + refVScaleVal
                    + ",refType:" + refType
                    + ",refProbeRate:" + refProbeRate);  // 输出日志
            setVScaleVal(refVScaleVal);  // 设置档位值
            setDelay(0);  // 重置延迟
            refChannelAction.changeChTimeBase();  // 通知时基变化
        }
    }

    /**
     * 计算X偏移
     * 
     * <p>根据时基比例计算波形数据的起始和结束X坐标。
     * 
     * @param waveData 波形数据对象
     */
    public void calcXOffset(WaveData waveData){

        int w = ScopeBase.getWidth()/2;  // 屏幕中心X坐标
        int s = refXStart;  // 起始X坐标
        int e = refXEnd;  // 结束X坐标
        double r = refTimeScaleVal_original/refTimeScaleVal;  // 时基比例

        s = (int)Math.floor(w - (w - s) * r + refXPos);  // 计算新的起始X坐标
        e = (int)Math.floor(w + (e - w) * r + refXPos);  // 计算新的结束X坐标

        waveData.setStartX(s);  // 设置起始X坐标
        waveData.setEndX(e);  // 设置结束X坐标
    }


    /**
     * 绘制参考波形
     * 
     * <p>使用防抖线程绘制参考波形，支持Zoom模式。
     * 如果当前线程不是防抖线程，则将绘制任务提交到防抖线程。
     */
    public void drawRef(){
        if(!isOpen()) {  // 检查通道是否打开
            return;  // 未打开则不绘制
        }

        if(refThread.isSelfThread()) {  // 检查是否在防抖线程中
            WaveData dataBuffer = (WaveData)obtain();  // 获取波形数据缓冲区
            if (dataBuffer != null) {
                refChannelAction.loadWave();  // 调用动作代理加载波形
                refParam.setTimeScaleVal(refTimeScaleVal);  // 设置时基值
                double v = refVScaleVal * refFineVal/(ScopeBase.getVerticalPerGridPixels());  // 计算每像素垂直值

                if(refType == 2  // FFT类型
                        && refProbeType == VerticalAxis.PROBE_TYPE_DB) {  // dB模式
                    v *= dataBuffer.getVScaleVal()*1e4;  // 调整比例
                }
                refParam.setVerticalPerPix(v);  // 设置每像素垂直值


                if (refType != WaveData.FFT_WAVE) {  // 非FFT类型
                    refParam.setXOffset((int) (refXPos + getTimeDelayOfViewPix()));  // 设置X偏移（含延迟）
                } else {  // FFT类型
                    refParam.setXOffset((int) refXPos);  // 设置X偏移
                }
                Logger.i(TAG, "refXPos= " + refXPos + " ,delayPos= " + getTimeDelayOfViewPix() + " ,delay= " + delay);  // 输出日志
                if(Scope.getInstance().isZoom()) {  // Zoom模式
                    refParam.setYOffset((int) Math.round(this.getPos() * ScopeBase.getZoomHeight() / ScopeBase.getHeight()));  // 设置Y偏移（Zoom模式）
                    refParam.setHeight(ScopeBase.getZoomHeight());  // 设置高度（Zoom模式）
                } else {  // 普通模式
                    refParam.setYOffset((int) Math.round(this.getPos()));  // 设置Y偏移
                    refParam.setHeight(ScopeBase.getHeight());  // 设置高度
                }
                synchronized (surfaceLock) {  // 同步锁
                    if (surfaceNative != null) {  // 检查Surface是否有效
                        refParam.setStartX(refXStart);  // 设置起始X坐标
                        refParam.setEndX(refXEnd);  // 设置结束X坐标
                        surfaceNative.drawSurface(dataBuffer.getByteBuffer(), refParam.getDirectBuffer());  // 调用Native层绘制
                    }
                }
                recycle(dataBuffer);  // 回收缓冲区
            }
        }else
        {
            refThread.run();  // 提交到防抖线程执行
        }
    }

    /** Surface同步锁对象 */
    public final Object surfaceLock = new Object();  // 用于同步Surface操作
    
    /**
     * 观察者更新方法
     * 
     * <p>处理Surface创建和销毁事件。
     * 
     * @param observable 被观察者对象
     * @param data 事件数据
     */
    @Override
    public void update(Observable observable, Object data) {
        EventBase eventBase = (EventBase)data;  // 转换为事件对象
        if(eventBase != null){
            if(eventBase.getId() == EventFactory.EVENT_SURFACE_CREATED){  // Surface创建事件
                SurfaceDataRecv surface = (SurfaceDataRecv)eventBase.getData();  // 获取Surface数据
                if(surface != null){
                    synchronized (surfaceLock) {  // 同步锁
                        surfaceNative = surface.getSurfaceNative(SurfacePreview.LAYER_REF1 + getChId() - ChannelFactory.REF1);  // 获取对应层的Surface
                        surfaceNative.acquireSurface(ScopeBase.getWidth(), ScopeBase.getHeight());  // 获取Surface
                        surfaceNative.clearSurface();  // 清空Surface
                    }
                }
            }else if(eventBase.getId() == EventFactory.EVENT_SURFACE_DESTROYED){  // Surface销毁事件
                SurfaceDataRecv surface = (SurfaceDataRecv)eventBase.getData();  // 获取Surface数据
                if(surface != null) {
                    synchronized (surfaceLock) {  // 同步锁
                        if(surfaceNative != null) {
                            surfaceNative.releaseSurface();  // 释放Surface
                            surfaceNative = null;  // 置空引用
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取原始时基值
     * 
     * @return 原始时基值
     */
    public double getRefTimeScaleVal_original() {
        return refTimeScaleVal_original;  // 返回原始时基值
    }

    /**
     * 获取原始X位置
     * 
     * @return 原始X位置
     */
    public long getRefXPos_original() {
        return refXPos_original;  // 返回原始X位置
    }

    /** 延迟值（秒） */
    private double delay = 0;  // 延迟值，初始为0

    /**
     * 设置延迟
     * 
     * <p>设置延迟值并计算延迟位置，触发重绘。
     * 
     * @param delay 延迟值（秒）
     */
    public void setDelay(double delay) {
        this.delay = delay;  // 设置延迟值
        this.refDelayPos = (long) (delay / 1e-13);  // 计算延迟位置（单位：1e-13秒）
        drawRef();  // 触发重绘
    }

    /**
     * 获取延迟
     * 
     * @return 延迟值（秒）
     */
    public double getDelay() {
        return this.delay;  // 返回延迟值
    }

}
