package com.micsig.tbook.tbookscope.wavezone;   // 波形显示区域包，包含波形显示相关的类

import android.content.Context;   // 导入Android上下文类，用于获取资源和系统服务
import android.graphics.Color;   // 导入Android颜色类，用于设置颜色常量
import android.os.SystemClock;   // 导入Android系统时钟类，用于获取精确时间戳
import android.view.MotionEvent;   // 导入Android触摸事件类，用于处理手势操作
import android.view.ViewGroup;   // 导入Android视图组类，用于判断父视图可见性

import com.chillingvan.canvasgl.ICanvasGL;   // 导入GL画布接口，用于OpenGL绘制
import com.chillingvan.canvasgl.glview.texture.GLTextureView;   // 导入GL纹理视图类，用于OpenGL渲染
import com.micsig.base.Logger;   // 导入日志类，用于调试输出
import com.micsig.tbook.scope.Scope;   // 导入示波器主类，用于双击处理
import com.micsig.tbook.tbookscope.util.CacheUtil;   // 导入缓存工具类，用于状态缓存
import com.micsig.tbook.tbookscope.wavezone.display.WaveGridManage;   // 导入网格管理器类，用于绘制网格
import com.micsig.tbook.tbookscope.wavezone.display.WaveMaskLayer_YTZoom;   // 导入遮罩层管理器类，用于绘制遮罩层
import com.micsig.tbook.tbookscope.wavezone.display.WaveMaskLayer_YTZoomAction;   // 导入遮罩层动作管理器类，用于处理遮罩层交互

/**
 * YT模式缩放窗口显示类 - 放大显示波形细节的专用窗口
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    YT缩放窗口显示架构                            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────┐                                           │
 * │  │ WaveZoneDisplay │                                           │
 * │  │     _YT         │                                           │
 * │  │ (YT波形显示)     │                                           │
 * │  │                 │                                           │
 * │  │  双指缩放手势   │                                           │
 * │  │  ────────────→ │                                           │
 * │  │                 │                                           │
 * │  │  RxBus事件      │                                           │
 * │  │  YTZoomMsgDisplay│                                           │
 * │  └────────┬────────┘                                           │
 * │           │                                                     │
 * │           │ 控制显示/隐藏                                       │
 * │           ▼                                                     │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │       WaveZoneDisplay_YTZoom                │               │
 * │  │         (YT缩放窗口)                         │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  GL绘制组件                          │   │               │
 * │  │  │  - WaveGridManage: 绘制网格          │   │               │
 * │  │  │  - WaveMaskLayer_YTZoom: 绘制遮罩层  │   │               │
 * │  │  │  - ICanvasGL: OpenGL画布             │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  触摸事件处理                        │   │               │
 * │  │  │  - 双击：关闭缩放窗口                │   │               │
 * │  │  │  - 拖动：移动缩放窗口位置            │   │               │
 * │  │  │  - layerX_move: 移动遮罩层          │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  WaveMaskLayer      │                             │
 * │           │    _YTZoom          │                             │
 * │           │  (遮罩层管理)        │                             │
 * │           │  layerX_move()      │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是YT模式缩放显示系统的核心组件，继承GLTextureView，用于显示放大后的波形细节。
 * 当用户在主波形窗口（WaveZoneDisplay_YT）上执行双指缩放手势时，此窗口会被显示，
 * 用于放大显示选中区域的波形，便于观察波形细节。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>缩放窗口渲染</b>：使用OpenGL渲染缩放窗口的网格和遮罩层</li>
 *   <li><b>遮罩层绘制</b>：绘制缩放窗口的遮罩层，标识缩放区域</li>
 *   <li><b>触摸事件处理</b>：处理双击关闭窗口和拖动移动窗口位置</li>
 *   <li><b>显示状态控制</b>：根据YTZoomMsgDisplay消息控制窗口显示/隐藏</li>
 * </ul>
 * 
 * <h3>YT缩放窗口功能说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │                    YT缩放窗口功能详解                          │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  YT缩放窗口是示波器的重要功能，用于放大显示波形细节：           │
 * │                                                               │
 * │  ┌─────────────────────────────────────────────┐             │
 * │  │              主波形窗口                      │             │
 * │  │  (WaveZoneDisplay_YT)                       │             │
 * │  │                                             │             │
 * │  │  ┌───────────────────────────────────────┐ │             │
 * │  │  │  正常波形显示                         │ │             │
 * │  │  │                                       │ │             │
 * │  │  │  ┌───────────────┐                   │ │             │
 * │  │  │  │ 缩放区域      │                   │ │             │
 * │  │  │  │ (选中的区域)  │                   │ │             │
 * │  │  │  └───────────────┘                   │ │             │
 * │  │  │                                       │ │             │
 * │  │  └───────────────────────────────────────┘ │             │
 * │  └─────────────────────────────────────────────┘             │
 * │                                                               │
 * │  ┌─────────────────────────────────────────────┐             │
 * │  │              缩放窗口                        │             │
 * │  │  (WaveZoneDisplay_YTZoom)                   │             │
 * │  │                                             │             │
 * │  │  ┌───────────────────────────────────────┐ │             │
 * │  │  │  放大显示选中区域的波形               │ │             │
 * │  │  │                                       │ │             │
 * │  │  │  时间刻度放大                         │ │             │
 * │  │  │  波形细节清晰可见                     │ │             │
 * │  │  │                                       │ │             │
 * │  │  │  ┌───────────────────────────────┐   │ │             │
 * │  │  │  │  遮罩层                        │   │ │             │
 * │  │  │  │  (WaveMaskLayer_YTZoom)       │   │ │             │
 * │  │  │  │  标识缩放区域                  │   │ │             │
 * │  │  │  └───────────────────────────────┘   │ │             │
 * │  │  │                                       │ │             │
 * │  │  └───────────────────────────────────────┘ │             │
 * │  └─────────────────────────────────────────────┘             │
 * │                                                               │
 * │  触发方式：                                                    │
 * │    - 双指缩放手势：在主波形窗口上双指捏合/展开                 │
 * │    - RxBus事件：YTZoomMsgDisplay消息控制显示/隐藏             │
 * │                                                               │
 * │  用户交互：                                                    │
 * │    - 双击：关闭缩放窗口，恢复正常显示                          │
 * │    - 拖动：移动缩放窗口的位置                                  │
 * │                                                               │
 * │  功能特点：                                                    │
 * │    - 时间放大：只放大时间轴，保持电压轴不变                    │
 * │    - 细节观察：用于观察波形的细节部分                          │
 * │    - 遮罩层：标识缩放区域，提供视觉反馈                        │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>显示控制流程</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              YT缩放窗口显示控制流程                            │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Step 1: 用户双指缩放手势                                     │
 * │    └─ 在WaveZoneDisplay_YT上双指操作                          │
 * │                                                               │
 * │  Step 2: 状态判断                                             │
 * │    └─ 根据手势状态判断缩放窗口显示/隐藏                        │
 * │      - state=0: 显示缩放窗口                                  │
 * │      - state=1: 隐藏缩放窗口                                  │
 * │                                                               │
 * │  Step 3: 发送RxBus事件                                        │
 * │    └─ RxBus.getInstance().post(                               │
 * │         RxEnum.WAVEZONE_DISPLAY_YTZOOM,                       │
 * │         new YTZoomMsgDisplay(true/false))                     │
 * │                                                               │
 * │  Step 4: 接收事件                                             │
 * │    └─ MainViewGroup订阅事件                                   │
 * │    └─ 根据isDisplay设置WaveZoneDisplay_YTZoom可见性           │
 * │      - isDisplay=true: View.VISIBLE                           │
 * │      - isDisplay=false: View.GONE                             │
 * │                                                               │
 * │  Step 5: GL绘制                                               │
 * │    └─ WaveZoneDisplay_YTZoom.onGLDraw()                       │
 * │    └─ 绘制网格和遮罩层                                        │
 * │                                                               │
 * │  Step 6: 用户交互                                             │
 * │    └─ 双击：关闭缩放窗口                                      │
 * │    └─ 拖动：移动缩放窗口位置                                  │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>双击检测机制</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              双击检测机制详解                                  │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  双击检测原理：                                                │
 * │    - 记录第一次点击的时间戳（ClickTS）和坐标（X, Y）           │
 * │    - 第二次点击时判断时间差和位置差                            │
 * │    - 时间差 < 500ms 且位置差 < 50像素 → 双击成功              │
 * │                                                               │
 * │  检测流程：                                                    │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  第一次点击                                  │           │
 * │    │    - ClickTS = 当前时间戳                    │           │
 * │    │    - X, Y = 点击坐标                         │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                       │                                       │
 * │                       ▼                                       │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  第二次点击                                  │           │
 * │    │    - ts = 当前时间戳                         │           │
 * │    │    - x, y = 点击坐标                         │           │
 * │    │                                             │           │
 * │    │  判断条件：                                  │           │
 * │    │    if ((ts - ClickTS) < 500) {               │           │
 * │    │      if (Math.abs(x - X) < 50 &&             │           │
 * │    │          Math.abs(y - Y) < 50) {             │           │
 * │    │        // 双击成功                           │           │
 * │    │        Scope.getInstance().doubleClicked(x)  │           │
 * │    │      }                                       │           │
 * │    │      ClickTS = 0; // 重置                    │           │
 * │    │    } else {                                  │           │
 * │    │      ClickTS = ts; // 更新为第一次点击       │           │
 * │    │    }                                         │           │
 * │    │                                             │           │
 * │    │  更新坐标：                                  │           │
 * │    │    X = x;                                    │           │
 * │    │    Y = y;                                    │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  双击成功后：                                                  │
 * │    - 调用Scope.getInstance().doubleClicked(x)                 │
 * │    - 关闭缩放窗口                                              │
 * │    - 恢复正常显示                                              │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>波形细节观察</b>：放大显示波形细节，观察信号特征</li>
 *   <li><b>时间轴放大</b>：只放大时间轴，保持电压轴不变</li>
 *   <li><b>双击关闭</b>：双击缩放窗口关闭并恢复正常显示</li>
 *   <li><b>拖动移动</b>：拖动缩放窗口查看不同区域的波形</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link GLTextureView} - 父类，提供OpenGL渲染能力</li>
 *   <li>{@link WaveGridManage} - 网格管理器，绘制缩放窗口网格</li>
 *   <li>{@link WaveMaskLayer_YTZoom} - 遮罩层管理器，绘制缩放区域标识</li>
 *   <li>{@link WaveMaskLayer_YTZoomAction} - 遮罩层动作管理器，处理遮罩层交互</li>
 *   <li>{@link WaveZoneDisplay_YT} - YT波形显示区域，发送YTZoomMsgDisplay消息</li>
 *   <li>{@link YTZoomMsgDisplay} - 缩放显示消息，控制窗口显示/隐藏</li>
 *   <li>{@link Scope} - 示波器主类，处理双击事件</li>
 *   <li>{@link CacheUtil} - 缓存工具类，记录加载状态</li>
 * </ul>
 * 
 * <h3>设计模式</h3>
 * <p>本类采用OpenGL渲染模式，继承GLTextureView实现高效的波形绘制。
 * 配合RxBus事件总线，实现观察者模式的解耦通信。</p>
 * 
 * @see GLTextureView
 * @see WaveGridManage
 * @see WaveMaskLayer_YTZoom
 * @see WaveMaskLayer_YTZoomAction
 * @see WaveZoneDisplay_YT
 * @see YTZoomMsgDisplay
 * @see Scope
 * @see CacheUtil
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class WaveZoneDisplay_YTZoom extends GLTextureView {   // 继承GLTextureView，获得OpenGL渲染能力
    
    /**
     * 日志标签 - 用于调试输出
     * 
     * <p>定义日志输出的标签，方便在日志中识别YT缩放窗口的相关信息。</p>
     */
    private static final String TAG = "WaveZoneDisplay_YTZoom";   // 日志标签：用于调试输出

    /**
     * Android上下文对象
     * 
     * <p>用于获取资源和系统服务，在构造函数中初始化。</p>
     */
    private Context context;   // Android上下文：用于获取资源和系统服务
    
    /**
     * GL画布对象
     * 
     * <p>用于OpenGL绘制，在onGLDraw方法中初始化。
     * 提供绘制网格和遮罩层的画布接口。</p>
     */
    private ICanvasGL canvasGL;   // GL画布：用于OpenGL绘制
    
    /**
     * 开始时间、结束时间、时间差
     * 
     * <p>用于性能监控和调试，记录绘制时间。</p>
     */
    private long startTime, endTime, dt;   // 时间变量：用于性能监控


    //region 属性


    //endregion

    /**
     * 获取GL画布对象
     * 
     * <p>返回当前GL画布对象，供外部使用。</p>
     * 
     * <h4>返回值说明</h4>
     * <p>返回ICanvasGL对象，用于OpenGL绘制。</p>
     * 
     * <h4>调用时机</h4>
     * <p>外部组件需要访问GL画布时调用此方法。</p>
     * 
     * @return GL画布对象（ICanvasGL）
     */
    public ICanvasGL getCanvasGL() {   // 方法：获取GL画布对象
        return canvasGL;   // 返回canvasGL属性值
    }   // getCanvasGL方法结束

    /**
     * 构造函数 - 创建YT缩放窗口显示实例
     * 
     * <p>创建WaveZoneDisplay_YTZoom实例，初始化缩放窗口的基本属性。
     * 设置透明背景、非透明模式，并创建遮罩层动作管理器。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>context</td><td>Context</td><td>Android上下文，用于获取资源和系统服务</td></tr>
     * </table>
     * 
     * <h4>初始化流程</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  初始化流程                               │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  Step 1: 调用父类构造函数                │
     * │    └─ super(context)                    │
     * │                                         │
     * │  Step 2: 保存上下文对象                  │
     * │    └─ this.context = context            │
     * │                                         │
     * │  Step 3: 设置透明背景                    │
     * │    └─ setOpaque(false)                  │
     * │                                         │
     * │  Step 4: 记录开始时间                    │
     * │    └─ startTime = System.currentTimeMillis() │
     * │                                         │
     * │  Step 5: 创建遮罩层动作管理器            │
     * │    └─ new WaveMaskLayer_YTZoomAction(context) │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>MainViewGroup初始化时创建实例</li>
     *   <li>WorkModeManage初始化YT缩放模式时创建</li>
     * </ul>
     * 
     * @param context Android上下文，用于获取资源和系统服务
     * @see WaveMaskLayer_YTZoomAction
     */
    public WaveZoneDisplay_YTZoom(Context context) {   // 构造函数：创建YT缩放窗口显示实例
        super(context);   // 调用父类GLTextureView的构造函数，传入context
        this.context = context;   // 保存上下文对象到成员变量
        this.setOpaque(false);   // 设置非透明模式：允许透明背景
        startTime = System.currentTimeMillis();   // 记录开始时间：用于性能监控
        new WaveMaskLayer_YTZoomAction(context);   // 创建遮罩层动作管理器：处理遮罩层交互
    }   // 构造函数结束

    //    public WaveZoneDisplay_YTZoom(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.context = context;
//        this.setOpaque(false);
//    }


    /**
     * 初始化方法 - GL视图初始化
     * 
     * <p>在GL视图创建时调用，执行初始化操作。
     * 调用父类的init方法完成基础初始化。</p>
     * 
     * <h4>调用时机</h4>
     * <p>GL视图创建时自动调用，由GLTextureView框架管理。</p>
     * 
     * @see GLTextureView#init()
     */
    @Override   // 重写父类GLTextureView的init方法
    protected void init() {   // 方法：GL视图初始化
        super.init();   // 调用父类的init方法，完成基础初始化
    }   // init方法结束

    /**
     * GL绘制方法 - 绘制缩放窗口的网格和遮罩层
     * 
     * <p>此方法在每一帧绘制时调用，负责绘制缩放窗口的网格和遮罩层。
     * 使用OpenGL进行高效渲染，支持25帧刷新率。</p>
     * 
     * <h4>绘制流程</h4>
     * <pre>
     * ┌───────────────────────────────────────────────────────────────┐
     * │                    onGLDraw执行流程                           │
     * ├───────────────────────────────────────────────────────────────┤
     * │                                                               │
     * │  Step 1: 保存GL画布对象                                       │
     * │    └─ this.canvasGL = canvas                                  │
     * │                                                               │
     * │  Step 2: 检查父视图可见性                                     │
     * │    └─ if (((ViewGroup)(this.getParent())).getVisibility() == GONE) │
     * │      - 父视图不可见时：清屏并返回                             │
     * │      - 父视图可见时：继续绘制                                 │
     * │                                                               │
     * │  Step 3: 清屏                                                 │
     * │    └─ canvas.clearBuffer(Color.TRANSPARENT)                   │
     * │                                                               │
     * │  Step 4: 绘制网格                                             │
     * │    └─ WaveGridManage.getInstance().draw(                      │
     * │         IWorkMode.WorkMode_YTZOOM, canvas)                    │
     * │                                                               │
     * │  Step 5: 绘制遮罩层                                           │
     * │    └─ WaveMaskLayer_YTZoom.getInstance().draw(canvas)         │
     * │                                                               │
     * │  Step 6: 设置加载状态                                         │
     * │    └─ CacheUtil.get().setLoadMenuState(                       │
     * │         CacheUtil.LOAD_WaveZoneDisplayManage, true)           │
     * │                                                               │
     * └───────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>绘制组件说明</h4>
     * <table border="1">
     *   <tr><th>组件</th><th>功能</th><th>绘制内容</th></tr>
     *   <tr><td>WaveGridManage</td><td>网格管理器</td><td>绘制缩放窗口网格（背景网格线）</td></tr>
     *   <tr><td>WaveMaskLayer_YTZoom</td><td>遮罩层管理器</td><td>绘制遮罩层（标识缩放区域）</td></tr>
     * </table>
     * 
     * <h4>性能优化</h4>
     * <ul>
     *   <li>父视图不可见时直接返回，避免无效绘制</li>
     *   <li>使用透明背景，减少绘制开销</li>
     *   <li>25帧刷新率，平衡流畅度和性能</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>GL视图每一帧绘制时自动调用，由GLTextureView框架管理。</p>
     * 
     * @param canvas GL画布对象，用于OpenGL绘制
     * @see WaveGridManage#draw(int, ICanvasGL)
     * @see WaveMaskLayer_YTZoom#draw(ICanvasGL)
     * @see CacheUtil#setLoadMenuState(int, boolean)
     */
    @Override   // 重写父类GLTextureView的onGLDraw方法
    protected void onGLDraw(ICanvasGL canvas) {   // 方法：GL绘制，绘制缩放窗口的网格和遮罩层
        //25帧   // 注释：目标刷新率为25帧


        this.canvasGL = canvas;   // 保存GL画布对象到成员变量
        
        if (((ViewGroup) (this.getParent())).getVisibility() == GONE) {   // 判断：父视图是否不可见
            canvas.clearBuffer();   // 清屏：父视图不可见时清空画布
            return;   // 返回：父视图不可见时不绘制，直接返回
        }   // 父视图可见性判断结束
        
        canvas.clearBuffer(Color.TRANSPARENT);   // 清屏：使用透明颜色清空画布
        WaveGridManage.getInstance().draw(IWorkMode.WorkMode_YTZOOM, canvas);   // 绘制网格：调用网格管理器绘制缩放窗口网格
        WaveMaskLayer_YTZoom.getInstance().draw(canvas);   // 绘制遮罩层：调用遮罩层管理器绘制缩放区域标识
        CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_WaveZoneDisplayManage, true);   // 设置加载状态：标记缩放窗口已加载完成
    }   // onGLDraw方法结束


    /**
     * 触摸事件坐标变量
     * 
     * <p>用于记录触摸事件的坐标，处理拖动和双击操作。</p>
     * 
     * <h4>变量说明</h4>
     * <ul>
     *   <li><b>downX, downY</b>: 触摸按下时的坐标</li>
     *   <li><b>oldX, oldY</b>: 上一次触摸事件的坐标</li>
     * </ul>
     */
    int downX, downY, oldX, oldY;   // 触摸事件坐标：记录按下和上一次的坐标
    
    /**
     * 空隔宽度
     * 
     * <p>用于计算缩放窗口的间隔宽度，目前未使用。</p>
     */
    int LayerWidth = 0;  // 空隔宽度：用于计算缩放窗口的间隔，目前未使用
    
    /**
     * 双击检测时间戳
     * 
     * <p>记录第一次点击的时间戳，用于双击检测。
     * 双击检测条件：两次点击时间差 < 500ms。</p>
     */
    private long ClickTS = 0;   // 双击检测时间戳：记录第一次点击的时间
    
    /**
     * 双击检测坐标
     * 
     * <p>记录第一次点击的坐标，用于双击检测。
     * 双击检测条件：两次点击位置差 < 50像素。</p>
     */
    private int X = 0;   // 双击检测X坐标：记录第一次点击的X坐标
    private int Y = 0;   // 双击检测Y坐标：记录第一次点击的Y坐标
    
    /**
     * 双击检测和处理方法
     * 
     * <p>检测用户是否执行了双击操作，并在双击成功时关闭缩放窗口。
     * 双击检测条件：时间差 < 500ms 且位置差 < 50像素。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>x</td><td>int</td><td>当前点击的X坐标</td></tr>
     *   <tr><td>y</td><td>int</td><td>当前点击的Y坐标</td></tr>
     * </table>
     * 
     * <h4>检测逻辑</h4>
     * <pre>
     * ┌───────────────────────────────────────────────────────────────┐
     * │              双击检测逻辑                                      │
     * ├───────────────────────────────────────────────────────────────┤
     * │                                                               │
     * │  Step 1: 获取当前时间戳                                       │
     * │    └─ ts = SystemClock.elapsedRealtime()                      │
     * │                                                               │
     * │  Step 2: 判断时间差                                           │
     * │    └─ if ((ts - ClickTS) < 500)                               │
     * │      - 时间差 < 500ms：可能是双击                             │
     * │      - 时间差 >= 500ms：不是双击，更新ClickTS                 │
     * │                                                               │
     * │  Step 3: 判断位置差                                           │
     * │    └─ if (Math.abs(x - X) < 50 && Math.abs(y - Y) < 50)       │
     * │      - 位置差 < 50像素：双击成功                              │
     * │      - 位置差 >= 50像素：不是双击                             │
     * │                                                               │
     * │  Step 4: 双击成功处理                                         │
     * │    └─ Scope.getInstance().doubleClicked(x)                    │
     * │    └─ 关闭缩放窗口                                            │
     * │    └─ ClickTS = 0; // 重置时间戳                              │
     * │                                                               │
     * │  Step 5: 更新坐标                                             │
     * │    └─ X = x;                                                  │
     * │    └─ Y = y;                                                  │
     * │                                                               │
     * └───────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>MotionEvent.ACTION_UP：单指抬起时检测双击</li>
     *   <li>MotionEvent.ACTION_POINTER_UP：多指抬起时检测双击</li>
     * </ul>
     * 
     * @param x 当前点击的X坐标
     * @param y 当前点击的Y坐标
     * @see Scope#doubleClicked(int)
     */
    private void doubleClick(int x,int y){   // 方法：双击检测和处理
        long ts = SystemClock.elapsedRealtime();   // 获取当前时间戳：用于计算时间差

        if((ts - ClickTS) < 500){   // 判断：时间差是否小于500ms（双击时间窗口）
            if(Math.abs(x - X) < 50 && Math.abs(y - Y) < 50){   // 判断：位置差是否小于50像素（双击位置窗口）

                Scope.getInstance().doubleClicked(x);   // 双击成功：调用示波器主类的doubleClicked方法，关闭缩放窗口
            }   // 位置差判断结束
            ClickTS = 0;   // 重置时间戳：双击检测完成，重置为0
        }else {   // 时间差判断结束，时间差大于500ms
            ClickTS = ts;   // 更新时间戳：记录为第一次点击的时间
        }   // 时间差判断结束
        
        this.X = x;   // 更新X坐标：记录当前点击的X坐标
        this.Y = y;   // 更新Y坐标：记录当前点击的Y坐标
    }   // doubleClick方法结束
    
    /**
     * 触摸事件处理方法 - 处理缩放窗口的触摸交互
     * 
     * <p>处理缩放窗口的触摸事件，包括单指拖动、双击关闭、多指操作等。
     * 所有触摸事件都会被处理并返回true。</p>
     * 
     * <h4>事件类型说明</h4>
     * <table border="1">
     *   <tr><th>事件类型</th><th>触发条件</th><th>处理逻辑</th></tr>
     *   <tr><td>ACTION_DOWN</td><td>单指按下</td><td>记录按下坐标</td></tr>
     *   <tr><td>ACTION_MOVE</td><td>单指移动</td><td>移动缩放窗口位置</td></tr>
     *   <tr><td>ACTION_UP</td><td>单指抬起</td><td>检测双击关闭窗口</td></tr>
     *   <tr><td>ACTION_POINTER_DOWN</td><td>多指按下</td><td>记录第一个手指坐标</td></tr>
     *   <tr><td>ACTION_POINTER_UP</td><td>多指抬起</td><td>更新剩余手指坐标，检测双击</td></tr>
     * </table>
     * 
     * <h4>触摸事件处理流程</h4>
     * <pre>
     * ┌───────────────────────────────────────────────────────────────┐
     * │              onTouchEvent执行流程                             │
     * ├───────────────────────────────────────────────────────────────┤
     * │                                                               │
     * │  Step 1: 获取触摸坐标                                         │
     * │    └─ x = (int) event.getX()                                  │
     * │    └─ y = (int) event.getY()                                  │
     * │                                                               │
     * │  Step 2: 判断事件类型                                         │
     * │    └─ switch (event.getAction() & MotionEvent.ACTION_MASK)    │
     * │                                                               │
     * │  Step 3: 处理ACTION_DOWN                                      │
     * │    └─ 记录按下坐标：downX = oldX = (int) event.getX()          │
     * │                                                               │
     * │  Step 4: 处理ACTION_MOVE                                      │
     * │    └─ 调用zoomChangeOffset(event)移动缩放窗口                 │
     * │                                                               │
     * │  Step 5: 处理ACTION_UP                                        │
     * │    └─ 调用doubleClick(x, y)检测双击                           │
     * │                                                               │
     * │  Step 6: 处理ACTION_POINTER_UP                                │
     * │    └─ 更新剩余手指坐标                                        │
     * │    └─ 调用doubleClick(x, y)检测双击                           │
     * │                                                               │
     * │  Step 7: 处理ACTION_POINTER_DOWN                              │
     * │    └─ 记录第一个手指坐标                                       │
     * │                                                               │
     * │  Step 8: 返回true                                             │
     * │    └─ 表示事件已处理                                          │
     * │                                                               │
     * └───────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <p>用户触摸缩放窗口时自动调用，由Android系统分发触摸事件。</p>
     * 
     * @param event 触摸事件对象，包含触摸信息
     * @return true表示事件已处理，false表示未处理
     * @see MotionEvent
     * @see doubleClick(int, int)
     * @see zoomChangeOffset(MotionEvent)
     */
    @Override   // 重写父类GLTextureView的onTouchEvent方法
    public boolean onTouchEvent(MotionEvent event) {   // 方法：触摸事件处理，处理缩放窗口的触摸交互
        int x = (int) event.getX();   // 获取触摸X坐标：转换为整数
        int y = (int) event.getY();   // 获取触摸Y坐标：转换为整数
        
        switch (event.getAction() & MotionEvent.ACTION_MASK) {   // switch语句：判断触摸事件类型
            case MotionEvent.ACTION_DOWN: {   // case：单指按下事件
                downX = oldX = (int) event.getX();   // 记录按下坐标：保存到downX和oldX
                //downY = oldY = (int) event.getY();   // 已注释：不记录Y坐标
            }   // ACTION_DOWN处理结束
            break;   // 跳出case
            
            case MotionEvent.ACTION_MOVE: {   // case：单指移动事件
                zoomChangeOffset(event);   // 调用zoomChangeOffset方法：移动缩放窗口位置
            }   // ACTION_MOVE处理结束
            break;   // 跳出case
            
            case MotionEvent.ACTION_UP:{   // case：单指抬起事件
                doubleClick(x,y);   // 调用doubleClick方法：检测双击关闭窗口
            }   // ACTION_UP处理结束
            break;   // 跳出case
            
            case MotionEvent.ACTION_POINTER_UP: {   // case：多指抬起事件（非第一个手指抬起）
                int id = event.getActionIndex();   // 获取抬起的手指索引：判断是哪个手指抬起
                
                if (id == 0) {   // 判断：第一个手指抬起
                    downX = oldX = (int) event.getX(1);   // 更新坐标：使用第二个手指的坐标
                    downY = oldY = (int) event.getY(1);   // 更新坐标：使用第二个手指的Y坐标
                } else {   // 第一个手指判断结束，非第一个手指抬起
                    downX = oldX = (int) event.getX(0);   // 更新坐标：使用第一个手指的坐标
                    downY = oldY = (int) event.getY(0);   // 更新坐标：使用第一个手指的Y坐标
                }   // 手指索引判断结束
                
                doubleClick(x,y);   // 调用doubleClick方法：检测双击关闭窗口
            }   // ACTION_POINTER_UP处理结束
            break;   // 跳出case
            
            case MotionEvent.ACTION_POINTER_DOWN: {   // case：多指按下事件（非第一个手指按下）
                downX = oldX = (int) event.getX(0);   // 更新坐标：使用第一个手指的坐标
                downY = oldY = (int) event.getY(0);   // 更新坐标：使用第一个手指的Y坐标
            }   // ACTION_POINTER_DOWN处理结束
            break;   // 跳出case
        }   // switch语句结束
        
        return true;   // 返回true：表示触摸事件已处理
        //return super.onTouchEvent(event);   // 已注释：不调用父类方法
    }   // onTouchEvent方法结束

    /**
     * 改变X偏移方法 - 移动缩放窗口的位置
     * 
     * <p>根据触摸移动的偏移量，移动缩放窗口的遮罩层位置。
     * 用于实现拖动缩放窗口查看不同区域的波形。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>event</td><td>MotionEvent</td><td>触摸事件对象，包含当前触摸坐标</td></tr>
     * </table>
     * 
     * <h4>移动逻辑</h4>
     * <pre>
     * ┌───────────────────────────────────────────────────────────────┐
     * │              zoomChangeOffset执行逻辑                         │
     * ├───────────────────────────────────────────────────────────────┤
     * │                                                               │
     * │  Step 1: 计算偏移量                                           │
     * │    └─ offset = (int) event.getX() - downX                     │
     * │      - 当前X坐标 - 按下X坐标 = 移动偏移量                     │
     * │                                                               │
     * │  Step 2: 判断偏移量                                           │
     * │    └─ if (offset != 0)                                        │
     * │      - 偏移量 != 0：有移动，执行移动操作                      │
     * │      - 偏移量 == 0：无移动，不执行                            │
     * │                                                               │
     * │  Step 3: 更新按下坐标                                         │
     * │    └─ downX = (int) event.getX()                              │
     * │      - 更新为当前坐标，用于下次计算偏移                       │
     * │                                                               │
     * │  Step 4: 移动遮罩层                                           │
     * │    └─ WaveMaskLayer_YTZoom.getInstance().layerX_move(offset)  │
     * │      - 调用遮罩层管理器移动方法                               │
     * │      - 移动遮罩层的X位置                                      │
     * │                                                               │
     * └───────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>MotionEvent.ACTION_MOVE：单指移动时调用</li>
     *   <li>用户拖动缩放窗口时触发</li>
     * </ul>
     * 
     * @param event 触摸事件对象，包含当前触摸坐标
     * @see WaveMaskLayer_YTZoom#layerX_move(int)
     */
    //改变X的偏移   // 注释：此方法用于改变缩放窗口的X偏移，移动窗口位置
    private void zoomChangeOffset(MotionEvent event) {   // 方法：改变X偏移，移动缩放窗口位置
        int offset = (int) event.getX() - downX;   // 计算偏移量：当前X坐标 - 按下X坐标
        
        if(offset != 0) {   // 判断：偏移量是否不为0（有移动）
            downX = (int) event.getX();   // 更新按下坐标：记录为当前X坐标，用于下次计算偏移
            WaveMaskLayer_YTZoom.getInstance().layerX_move(offset);   // 移动遮罩层：调用遮罩层管理器的移动方法，传入偏移量
        }   // 偏移量判断结束
    }   // zoomChangeOffset方法结束

}   // WaveZoneDisplay_YTZoom类结束