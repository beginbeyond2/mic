package com.micsig.tbook.tbookscope.wavezone.wave; // 定义波形管理类的包路径


import android.graphics.Rect; // 导入矩形类，用于处理波形标签的矩形区域
import android.os.Handler; // 导入Handler类，用于处理消息队列和延时操作
import android.os.Message; // 导入Message类，用于Handler消息传递
import android.util.Log; // 导入Log类，用于日志输出

import com.chillingvan.canvasgl.ICanvasGL; // 导入OpenGL画布接口，用于波形绘制
import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口，用于串行总线通信
import com.micsig.tbook.scope.Display.DisplayXYService; // 导入XY显示服务，用于XY模式显示
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类，用于事件管理
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入UI事件观察者类
import com.micsig.tbook.scope.Scope; // 导入示波器核心类
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基类
import com.micsig.tbook.scope.Trigger.Trigger; // 导入触发器类
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 导入触发器工厂类
import com.micsig.tbook.scope.Trigger.TriggerLevel; // 导入触发电平类
import com.micsig.tbook.scope.channel.Channel; // 导入通道类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.channel.MathChannel; // 导入数学通道类
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道类
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串行通道类
import com.micsig.tbook.scope.math.MathDualWave; // 导入双波形数学运算类
import com.micsig.tbook.scope.math.MathExprWave; // 导入表达式数学运算类
import com.micsig.tbook.scope.math.MathFFTWave; // 导入FFT数学运算类
import com.micsig.tbook.scope.surface.PreviewTextureView; // 导入预览纹理视图类
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.main.mainright.MainRightLayoutItemChannelBranch; // 导入右侧布局通道分支项类
import com.micsig.tbook.tbookscope.main.mainright.MainRightLayoutItemChannelMaster; // 导入右侧布局通道主项类
import com.micsig.tbook.tbookscope.middleware.Tag; // 导入标签类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials; // 导入右侧串行布局类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 导入右侧串行消息类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava总线类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava枚举类
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean; // 导入工作模式Bean类
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理类
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 导入光标管理类
import com.micsig.tbook.tbookscope.wavezone.display.WaveGridManage; // 导入波形网格管理类
import com.micsig.tbook.tbookscope.wavezone.display.WaveMaskLayer_YTZoom; // 导入YT缩放遮罩层类
import com.micsig.tbook.tbookscope.wavezone.trigger.ITriggerLine; // 导入触发线接口
import com.micsig.tbook.tbookscope.wavezone.trigger.VoltageLineManage; // 导入电压线管理类
import com.micsig.tbook.ui.main.LeftPositionView; // 导入左侧位置视图类
import com.micsig.tbook.ui.util.TBookUtil; // 导入TBook工具类
import com.micsig.tbook.ui.wavezone.IWave; // 导入波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道枚举类

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
 * ┃                           WaveManage - 波形管理核心类                                 ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：波形显示管理模块                                                            │
 * │ 所属系统：MHO系列示波器软件 - 波形显示子系统                                          │
 * │ 核心职责：统一管理YT模式和XY模式下的波形显示、位置、偏移、选择等操作                    │
 * └─────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────┐
 * │ 架构设计：                                                                            │
 * │                                                                                      │
 * │     ┌──────────────────────────────────────────────────────────────────────┐       │
 * │     │                         WaveManage (单例)                             │       │
 * │     │                    [IWorkMode, IWaveManage接口实现]                   │       │
 * │     └──────────────────────────────────────────────────────────────────────┘       │
 * │                                    │                                                │
 * │                    ┌───────────────┴───────────────┐                              │
 * │                    │                               │                              │
 * │     ┌──────────────▼──────────────┐  ┌────────────▼─────────────┐               │
 * │     │    WaveManage_YT            │  │    WaveManage_XY         │               │
 * │     │  (YT/YTZOOM模式管理)        │  │  (XY模式管理)            │               │
 * │     └─────────────────────────────┘  └──────────────────────────┘               │
 * │                    │                               │                              │
 * │     ┌──────────────▼──────────────┐  ┌────────────▼─────────────┐               │
 * │     │  - 普通通道(CH1-CH8)        │  │  - X轴通道(CH1/3/5/7)   │               │
 * │     │  - 数学通道(Math1-8)        │  │  - Y轴通道(CH2/4/6/8)   │               │
 * │     │  - 参考通道(Ref1-8)         │  │  - XY显示服务           │               │
 * │     │  - 串行通道(S1-S4)          │  │                         │               │
 * │     └─────────────────────────────┘  └──────────────────────────┘               │
 * │                                                                                      │
 * │     ┌──────────────────────────────────────────────────────────────────────┐       │
 * │     │                      外部依赖模块                                      │       │
 * │     ├──────────────────────────────────────────────────────────────────────┤       │
 * │     │  - WorkModeManage: 工作模式管理                                        │       │
 * │     │  - ChannelFactory: 通道工厂                                            │       │
 * │     │  - CursorManage: 光标管理                                              │       │
 * │     │  - VoltageLineManage: 电压线管理                                       │       │
 * │     │  - SerialBusManage: 串行总线管理                                       │       │
 * │     │  - DisplayXYService: XY显示服务                                        │       │
 * │     │  - RxBus: RxJava事件总线                                                │       │
 * │     │  - EventFactory: 事件工厂                                              │       │
 * │     └──────────────────────────────────────────────────────────────────────┘       │
 * └─────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────┐
 * │ 数据流向：                                                                            │
 * │                                                                                      │
 * │     用户操作 ──▶ WaveManage ──▶ WaveManage_YT/XY ──▶ 波形显示                      │
 * │         │              │                │                                          │
 * │         │              │                ▼                                          │
 * │         │              │         位置/偏移更新                                      │
 * │         │              │                │                                          │
 * │         │              ▼                ▼                                          │
 * │         │         触发线更新 ──▶ VoltageLineManage                                  │
 * │         │              │                                                          │
 * │         │              ▼                                                          │
 * │         │         缓存更新 ──▶ CacheUtil                                           │
 * │         │                                                                         │
 * │         ▼                                                                          │
 * │     RxBus事件 ──▶ consumer处理 ──▶ 数据同步                                        │
 * │                                                                                      │
 * │     EventFactory事件 ──▶ eventUIObserver ──▶ 位置同步                              │
 * └─────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────┐
 * │ 核心功能：                                                                            │
 * │                                                                                      │
 * │  1. 工作模式管理：                                                                    │
 * │     - YT模式：普通时域波形显示                                                        │
 * │     - YTZOOM模式：YT缩放模式                                                         │
 * │     - XY模式：X-Y波形显示                                                            │
 * │                                                                                      │
 * │  2. 波形位置管理：                                                                    │
 * │     - 设置/获取波形Y轴位置                                                           │
 * │     - 处理波形移动事件                                                               │
 * │     - 同步通道位置到底层硬件                                                         │
 * │                                                                                      │
 * │  3. 通道管理：                                                                        │
 * │     - 普通通道(CH1-CH8)                                                              │
 * │     - 数学通道(Math1-8)                                                              │
 * │     - 参考通道(Ref1-8)                                                               │
 * │     - 串行通道(S1-S4)                                                                │
 * │                                                                                      │
 * │  4. 触发线联动：                                                                      │
 * │     - 波形移动时自动更新触发线位置                                                   │
 * │     - 支持双触发线模式                                                               │
 * │                                                                                      │
 * │  5. 串行总线支持：                                                                    │
 * │     - UART/LIN/CAN/SPI/I2C/M429/M1553B                                              │
 * │     - 波形移动时更新串行总线电平                                                     │
 * │                                                                                      │
 * │  6. 缓存管理：                                                                        │
 * │     - 加载缓存时恢复波形位置                                                         │
 * │     - 保存波形位置到缓存                                                             │
 * └─────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────┐
 * │ 依赖关系：                                                                            │
 * │                                                                                      │
 * │  上游依赖：                                                                           │
 * │    - MainViewGroup: 主视图容器                                                       │
 * │    - PreviewTextureView: 波形纹理视图                                               │
 * │    - WorkModeManage: 工作模式管理                                                   │
 * │                                                                                      │
 * │  下游依赖：                                                                           │
 * │    - WaveManage_YT: YT模式波形管理                                                  │
 * │    - WaveManage_XY: XY模式波形管理                                                  │
 * │    - ChannelFactory: 通道工厂                                                       │
 * │    - VoltageLineManage: 电压线管理                                                  │
 * │    - SerialBusManage: 串行总线管理                                                  │
 * │    - CursorManage: 光标管理                                                         │
 * │                                                                                      │
 * │  横向依赖：                                                                           │
 * │    - RxBus: RxJava事件总线                                                          │
 * │    - EventFactory: 事件工厂                                                         │
 * │    - CacheUtil: 缓存工具                                                            │
 * │    - Tools: 工具类                                                                  │
 * └─────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────┐
 * │ 线程安全说明：                                                                        │
 * │                                                                                      │
 * │  1. 单例模式：使用静态内部类Holder实现，保证线程安全的延迟初始化                       │
 * │                                                                                      │
 * │  2. Handler使用：                                                                    │
 * │     - handler对象在主线程创建，用于处理触发线和值线的延时隐藏                       │
 * │     - 所有Handler操作都在主线程执行，保证线程安全                                    │
 * │                                                                                      │
 * │  3. RxBus订阅：                                                                      │
 * │     - 所有RxJava订阅默认在主线程执行，保证UI操作安全                                 │
 * │                                                                                      │
 * │  4. EventFactory观察者：                                                             │
 * │     - 事件观察者在主线程处理，保证UI更新安全                                         │
 * │                                                                                      │
 * │  5. 注意事项：                                                                        │
 * │     - 避免在非主线程直接调用UI相关方法                                               │
 * │     - 波形位置更新需要通过RxBus或EventFactory进行跨线程通信                          │
 * │     - 缓存操作需要确保线程同步                                                       │
 * └─────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────┐
 * │ 使用示例：                                                                            │
 * │                                                                                      │
 * │  // 获取单例实例                                                                      │
 * │  WaveManage waveManage = WaveManage.get();                                          │
 * │                                                                                      │
 * │  // 初始化                                                                            │
 * │  waveManage.init(mainViewGroup);                                                    │
 * │                                                                                      │
 * │  // 设置波形位置                                                                      │
 * │  waveManage.setPositionY(TChan.Ch1, 100.0);                                         │
 * │                                                                                      │
 * │  // 获取波形位置                                                                      │
 * │  double position = waveManage.getPositionY(TChan.Ch1);                              │
 * │                                                                                      │
 * │  // 切换工作模式                                                                      │
 * │  waveManage.switchWorkMode(IWorkMode.WorkMode_YT);                                  │
 * │                                                                                      │
 * │  // 绘制波形                                                                          │
 * │  waveManage.draw(canvas);                                                           │
 * └─────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author liwb // 作者：liwb
 * @date 2017/11/7 // 创建日期：2017年11月7日
 * @version 2.0 // 版本号：2.0
 * @since MHO系列示波器软件V1.0 // 软件版本：MHO系列示波器软件V1.0开始引入
 */

public class WaveManage implements IWorkMode, IWaveManage { // 波形管理类，实现工作模式接口和波形管理接口
    
    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 常量定义区域                                                                      │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    private static final String TAG = "WaveManage"; // 日志标签，用于标识波形管理类的日志输出

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 单例模式实现区域                                                                  │
    // │ 使用静态内部类Holder实现线程安全的延迟初始化单例模式                              │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    //region 单例 // 单例模式区域标记
    private static final class WaveManageHolder { // 静态内部类Holder，用于持有单例实例
        public static final WaveManage instance = new WaveManage(); // 静态常量实例，类加载时创建，保证线程安全
    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ get() - 获取WaveManage单例实例                                                ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 获取波形管理器的单例实例。
     * 使用静态内部类Holder实现，保证线程安全的延迟初始化。
     * 
     * @return WaveManage单例实例，永远不会为null
     */
    public static WaveManage get() { // 获取单例实例的静态方法
        return WaveManageHolder.instance; // 返回静态内部类持有的单例实例
    }
    //endregion // 单例模式区域结束

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 成员变量定义区域                                                                  │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    private WaveManage_XY waveManage_xy; // XY模式波形管理器，负责XY模式下的波形显示和管理
    private WaveManage_YT waveManage_yt; // YT模式波形管理器，负责YT和YTZOOM模式下的波形显示和管理
    private MainViewGroup mainViewGroup; // 主视图组，用于获取UI组件引用

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 构造函数区域                                                                      │
    // │ 初始化波形管理器，创建XY和YT管理器，订阅RxBus事件，注册EventFactory观察者        │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ WaveManage() - 私有构造函数                                                    ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 私有构造函数，防止外部直接创建实例，保证单例模式的完整性。
     * 初始化XY和YT波形管理器，订阅RxBus事件，注册EventFactory观察者。
     * 
     * 初始化流程：
     * 1. 创建WaveManage_XY实例，传入波形移动和选择事件回调
     * 2. 创建WaveManage_YT实例，传入波形移动和选择事件回调
     * 3. 订阅MAIN_LOAD_CACHE事件，用于加载缓存时恢复波形位置
     * 4. 订阅MAIN_LOAD_CACHE_EX事件，用于扩展缓存加载
     * 5. 订阅WAVEZONE_WORKMODE_CHANGE事件，用于工作模式切换时刷新波形
     * 6. 订阅RIGHTLAYOUT_SERIALS事件，用于串行总线配置变化时更新电平
     * 7. 注册EVENT_CHANNEL_POS事件观察者，用于通道位置变化时同步波形位置
     * 8. 注册EVENT_MATH_VPOS事件观察者，用于数学通道位置变化时同步波形位置
     * 9. 注册EVENT_REF_VPOS事件观察者，用于参考通道位置变化时同步波形位置
     * 10. 注册EVENT_WAVE_LABEL_SELECT事件观察者，用于波形标签选择事件
     * 11. 注册EVENT_WAVE_LABEL_MOVE事件观察者，用于波形标签移动事件
     */
    private WaveManage() { // 私有构造函数，防止外部直接创建实例
        waveManage_xy = new WaveManage_XY(onMovingWaveEvent, onSelectChangeEvent); // 创建XY模式波形管理器，传入波形移动和选择事件回调
        waveManage_yt = new WaveManage_YT(onMovingWaveEvent, onSelectChangeEvent); // 创建YT模式波形管理器，传入波形移动和选择事件回调
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅主缓存加载事件，用于加载缓存时恢复波形位置
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCacheEx); // 订阅扩展缓存加载事件，用于扩展缓存加载
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange); // 订阅工作模式切换事件，用于工作模式切换时刷新波形
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials); // 订阅右侧串行布局事件，用于串行总线配置变化时更新电平
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_POS, eventUIObserver); // 注册通道位置变化事件观察者，用于通道位置变化时同步波形位置
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_VPOS, eventUIObserver); // 注册数学通道位置变化事件观察者，用于数学通道位置变化时同步波形位置
        EventFactory.addEventObserver(EventFactory.EVENT_REF_VPOS, eventUIObserver); // 注册参考通道位置变化事件观察者，用于参考通道位置变化时同步波形位置
        EventFactory.addEventObserver(EventFactory.EVENT_WAVE_LABEL_SELECT,eventUIObserver); // 注册波形标签选择事件观察者，用于波形标签选择事件
        EventFactory.addEventObserver(EventFactory.EVENT_WAVE_LABEL_MOVE, eventUIObserver); // 注册波形标签移动事件观察者，用于波形标签移动事件

    }

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ RxJava消费者定义区域                                                              │
    // │ 定义各种RxBus事件的消费者，用于响应不同的事件                                      │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ consumerRightSerials - 右侧串行布局事件消费者                                  ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 右侧串行布局事件消费者，用于处理串行总线配置变化事件。
     * 当串行总线配置发生变化时，遍历所有通道，更新串行总线电平。
     * 
     * 处理逻辑：
     * 1. 检查事件是否来自EventBus（避免重复处理）
     * 2. 如果不是来自EventBus，遍历所有通道
     * 3. 对每个通道调用changeSerialTrigVol_channel方法，更新串行总线电平
     */
    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() { // 定义右侧串行布局事件消费者

        /**
         * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
         * ┃ accept() - 接收并处理右侧串行布局事件                                        ┃
         * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
         * 
         * 接收并处理右侧串行布局事件。
         * 当串行总线配置发生变化时，更新所有通道的串行总线电平。
         * 
         * @param rightMsgSerials 右侧串行布局消息对象
         * @throws Exception 可能抛出的异常
         */
        @Override
        public void accept(RightMsgSerials rightMsgSerials) throws Exception { // 接收并处理右侧串行布局事件
            if(!rightMsgSerials.isFromEventBus()) { // 检查事件是否来自EventBus，避免重复处理
                ChannelFactory.forEachCh(channel -> { // 遍历所有通道
                    changeSerialTrigVol_channel(channel.getChId()); // 对每个通道调用changeSerialTrigVol_channel方法，更新串行总线电平
                });
            }
        }
    };

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 公共方法区域                                                                      │
    // │ 提供波形管理器的各种公共操作方法                                                  │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ setWaveTextureView() - 设置波形纹理视图                                       ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 设置波形纹理视图，用于YT模式的波形显示。
     * 将PreviewTextureView传递给YT模式波形管理器。
     * 
     * @param waveTextureView 波形纹理视图对象，用于渲染波形
     */
    public void setWaveTextureView(PreviewTextureView waveTextureView) { // 设置波形纹理视图
        waveManage_yt.setWaveTextureView(waveTextureView); // 将波形纹理视图传递给YT模式波形管理器
    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ setClickSelectEnable() - 设置点选功能使能                                     ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 设置点选功能的使能状态。
     * 控制YT模式下是否允许通过点击选择波形。
     * 
     * @param clickSelectEnable true表示启用点选功能，false表示禁用点选功能
     */
    public void setClickSelectEnable(boolean clickSelectEnable) { // 设置点选功能使能
        waveManage_yt.setClickSelectEnable(clickSelectEnable); // 将点选功能使能状态传递给YT模式波形管理器
    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ setWaveChange() - 设置波形高度变化                                            ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 设置波形高度变化，用于调整波形显示区域的高度。
     * 同时更新YT缩放遮罩层的高度，并重新计算所有通道的偏移。
     * 
     * 处理流程：
     * 1. 更新YT缩放遮罩层的高度（高度的1/4）
     * 2. 更新YT模式波形管理器的波形高度
     * 3. 遍历所有普通通道，重新设置偏移
     * 4. 遍历所有参考通道，重新设置偏移
     * 5. 遍历所有数学通道，重新设置偏移
     * 
     * @param height 新的波形高度（像素值）
     */
    public void setWaveChange(int height) { // 设置波形高度变化
        WaveMaskLayer_YTZoom.getInstance().changeZoomH(height >> 2); // 更新YT缩放遮罩层的高度，高度的1/4（右移2位相当于除以4）
        waveManage_yt.setWaveChange(height); // 更新YT模式波形管理器的波形高度
//        waveManage_xy.setWaveChange(height); // 注释掉的代码：XY模式波形管理器的波形高度更新（已禁用）
        TChan.foreachChan(this::setOffset); // 遍历所有普通通道，重新设置偏移
        TChan.foreachRef(this::setRefOffset); // 遍历所有参考通道，重新设置偏移
        TChan.foreachMath(this::setMathOffset); // 遍历所有数学通道，重新设置偏移

    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ changeChannelColor() - 改变通道颜色                                            ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 改变指定通道的波形颜色。
     * 将颜色变化传递给YT模式波形管理器进行更新。
     * 
     * @param chIndex 通道索引（1-8）
     * @param colorStr 颜色字符串（如"#FF0000"）
     */
    public void changeChannelColor(int chIndex, String colorStr) { // 改变通道颜色
        waveManage_yt.changeColor(chIndex, colorStr); // 将颜色变化传递给YT模式波形管理器进行更新

    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ init() - 初始化波形管理器                                                      ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 初始化波形管理器，设置主视图组引用。
     * 主视图组用于获取UI组件引用，如右侧布局通道主项、左侧位置视图等。
     * 
     * @param mainViewGroup 主视图组对象，包含所有UI组件
     */
    public void init(MainViewGroup mainViewGroup) { // 初始化波形管理器
        this.mainViewGroup = mainViewGroup; // 设置主视图组引用
    }

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ RxJava消费者定义区域（续）                                                        │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ consumerLoadCache - 主缓存加载事件消费者                                       ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 主缓存加载事件消费者，用于处理缓存加载事件。
     * 当加载缓存时，恢复波形位置，并标记WaveManage缓存已加载。
     * 
     * 处理逻辑：
     * 1. 调用setCache方法，恢复波形位置
     * 2. 标记WaveManage缓存已加载
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 定义主缓存加载事件消费者
        /**
         * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
         * ┃ accept() - 接收并处理主缓存加载事件                                        ┃
         * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
         * 
         * 接收并处理主缓存加载事件。
         * 当加载缓存时，恢复波形位置，并标记WaveManage缓存已加载。
         * 
         * @param loadCache 缓存加载对象
         * @throws Exception 可能抛出的异常
         */
        @Override
        public void accept(LoadCache loadCache) throws Exception { // 接收并处理主缓存加载事件
            setCache(); // 调用setCache方法，恢复波形位置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_WaveManage, true); // 标记WaveManage缓存已加载
        }
    };

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ consumerLoadCacheEx - 扩展缓存加载事件消费者                                   ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 扩展缓存加载事件消费者，用于处理扩展缓存加载事件。
     * 当加载扩展缓存时，恢复波形位置，并重新设置所有通道的偏移。
     * 
     * 处理逻辑：
     * 1. 调用setCache方法，恢复波形位置
     * 2. 遍历所有普通通道，重新设置偏移
     * 3. 遍历所有参考通道，重新设置偏移
     * 4. 遍历所有数学通道，重新设置偏移
     */
    private Consumer<LoadCache> consumerLoadCacheEx = new Consumer<LoadCache>() { // 定义扩展缓存加载事件消费者
        /**
         * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
         * ┃ accept() - 接收并处理扩展缓存加载事件                                      ┃
         * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
         * 
         * 接收并处理扩展缓存加载事件。
         * 当加载扩展缓存时，恢复波形位置，并重新设置所有通道的偏移。
         * 
         * @param loadCache 缓存加载对象
         * @throws Exception 可能抛出的异常
         */
        @Override
        public void accept(LoadCache loadCache) throws Exception { // 接收并处理扩展缓存加载事件
            setCache(); // 调用setCache方法，恢复波形位置
            TChan.foreachChan(chan -> { // 遍历所有普通通道
                setOffset(chan); // 重新设置通道偏移
            });
            TChan.foreachRef(refChan -> { // 遍历所有参考通道
                setRefOffset(refChan); // 重新设置参考通道偏移
            });
            TChan.foreachMath(mathChan -> { // 遍历所有数学通道
                setMathOffset(mathChan); // 重新设置数学通道偏移
            });
        }
    };

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 缓存管理方法区域                                                                  │
    // │ 处理缓存加载和波形位置恢复                                                        │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ setCache() - 设置缓存，恢复波形位置                                            ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 设置缓存，恢复波形位置。
     * 根据当前工作模式，恢复YT模式或XY模式的波形位置。
     * 
     * 处理流程：
     * 1. 获取当前工作模式
     * 2. 如果不是XY模式，恢复YT模式的波形位置：
     *    - 遍历所有普通通道，设置垂直范围和位置
     *    - 遍历所有参考通道，设置位置
     *    - 遍历所有数学通道，设置位置
     * 3. 临时切换到XY模式，恢复XY模式的波形位置：
     *    - 设置CH1和CH2的XY位置
     *    - 更新DisplayXYService的X和Y偏移
     * 4. 恢复原始工作模式
     */
    private void setCache() { // 设置缓存，恢复波形位置

        int curWorkMode = WorkModeManage.getInstance().getmWorkMode(); // 获取当前工作模式
        if(curWorkMode != IWorkMode.WorkMode_XY) { // 如果当前不是XY模式
            TChan.foreachChan((chNo)->{ // 遍历所有普通通道
                Channel ch = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(chNo)); // 获取动态通道对象
                if (ch != null) { // 如果通道对象不为空
                    int val = Scope.vSpanOfView(ch.getResistanceType(),ch.getVScaleVal() / ch.getProbeRate()); // 计算垂直范围值
                    ch.setVRange(-val, val); // 设置通道的垂直范围
                }

            });
            TChan.foreachRef((chan)->{ // 遍历所有参考通道
                setPositionY(chan, Tools.getYTChannelPositionUI(chan)); // 设置参考通道的Y位置
            });
            TChan.foreachChan((chNo)->{ // 遍历所有普通通道
                Channel ch = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(chNo)); // 获取动态通道对象
                if (ch!=null){ // 如果通道对象不为空
                    ch.setPos(Tools.getYTChannelPosition(chNo)); // 设置通道位置
                }
                setPositionY(chNo,Tools.getYTChannelPositionUI(chNo)); // 设置通道的Y位置
            });
            TChan.foreachMath((mathNo)->{ // 遍历所有数学通道
                MathChannel math = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathNo)); // 获取数学通道对象
                if (math!=null && math.getPosUI()!=Tools.getYTChannelPosition(mathNo)){ // 如果数学通道对象不为空且位置不匹配
                    math.setPos(Tools.getYTChannelPosition(mathNo)); // 设置数学通道位置
                }
                setPositionY(mathNo,Tools.getYTChannelPositionUI(mathNo)); // 设置数学通道的Y位置
            });
            TChan.foreachRef((refNo)->{ // 遍历所有参考通道
                RefChannel ref = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refNo)); // 获取参考通道对象
                if (ref!=null){ // 如果参考通道对象不为空
                    ref.setPos(Tools.getYTChannelPosition(refNo)); // 设置参考通道位置
                }
            });
        }

        WorkModeManage.getInstance().setWorkModeOnlyChangeData(WorkMode_XY); // 临时切换到XY模式（仅改变数据）
        waveManage_xy.setPositionY(TChan.Ch1, CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_XY_POSITION + TChan.Ch1)); // 设置CH1的XY位置
        DisplayXYService.getInstance().setX(ScopeBase.getXYWidth() / 2 - (int) waveManage_xy.getPositionY(TChan.Ch1)); // 设置DisplayXYService的X偏移
        waveManage_xy.setPositionY(TChan.Ch2, CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_XY_POSITION + TChan.Ch2)); // 设置CH2的XY位置
        DisplayXYService.getInstance().setY(ScopeBase.getXYWidth() / 2 - (int) waveManage_xy.getPositionY(TChan.Ch2)); // 设置DisplayXYService的Y偏移
        WorkModeManage.getInstance().setWorkModeOnlyChangeData(curWorkMode); // 恢复原始工作模式
    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ setMathWaveVScaleId() - 设置数学波形垂直刻度ID                                ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 设置数学波形的垂直刻度ID。
     * 根据数学通道的类型（AXB、DW、AM、FFT），设置相应的垂直刻度ID。
     * 
     * 处理逻辑：
     * 1. 获取数学通道类型
     * 2. 根据类型获取对应的数学波形对象
     * 3. 从缓存中获取垂直刻度ID并设置
     * 
     * @param mathChan 数学通道索引（Math1-8）
     */
    private void setMathWaveVScaleId(int mathChan) { // 设置数学波形垂直刻度ID
        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChan); // 获取数学通道类型
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan)); // 获取数学通道对象
        if (mathChannel == null) return; // 如果数学通道对象为空，直接返回
        if (mathType == CacheUtil.MATHTYPE_AXB) { // 如果是AXB类型（表达式运算）
            MathExprWave exprWave = mathChannel.getMathExprWave(); // 获取表达式数学波形对象
            exprWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_AXB_VSCALE_ID + mathChan)); // 设置AXB垂直刻度ID
        } else if (mathType == CacheUtil.MATHTYPE_DW) { // 如果是DW类型（双波形运算）
            MathDualWave dualWave = mathChannel.getMathDualWave(); // 获取双波形数学运算对象
            dualWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_DW_VSCALE_ID + mathChan)); // 设置DW垂直刻度ID
        } else if (mathType == CacheUtil.MATHTYPE_AM) { // 如果是AM类型（高级数学运算）
            MathExprWave exprWave = mathChannel.getMathExprWave(); // 获取表达式数学波形对象
            exprWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_AM_VSCALE_ID + mathChan)); // 设置AM垂直刻度ID
        } else { // 其他类型（FFT）
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathChan) == 1) { // 如果FFT类型为DB
                MathFFTWave mathFFTWave = mathChannel.getMathFFTWave(); // 获取FFT数学波形对象
                mathFFTWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + mathChan)); // 设置FFT DB垂直刻度ID
            } else { // 如果FFT类型为RMS
                MathFFTWave mathFFTWave = mathChannel.getMathFFTWave(); // 获取FFT数学波形对象
                mathFFTWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + mathChan)); // 设置FFT RMS垂直刻度ID
            }
        }
    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ consumerWorkModeChange - 工作模式切换事件消费者                                ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 工作模式切换事件消费者，用于处理工作模式切换事件。
     * 当工作模式切换到YT或YTZOOM时，刷新波形显示，并重新设置所有通道的偏移。
     * 
     * 处理逻辑：
     * 1. 检查下一个工作模式
     * 2. 如果是YT或YTZOOM模式：
     *    - 刷新YT模式波形管理器
     *    - 刷新波形网格管理器
     *    - 遍历所有通道，重新设置偏移
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() { // 定义工作模式切换事件消费者
        /**
         * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
         * ┃ accept() - 接收并处理工作模式切换事件                                      ┃
         * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
         * 
         * 接收并处理工作模式切换事件。
         * 当工作模式切换到YT或YTZOOM时，刷新波形显示，并重新设置所有通道的偏移。
         * 
         * @param workModeBean 工作模式Bean对象
         * @throws Exception 可能抛出的异常
         */
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception { // 接收并处理工作模式切换事件
            switch (workModeBean.getNextWorkMode()) { // 根据下一个工作模式进行分支处理
                case IWorkMode.WorkMode_YT: // YT模式
                case IWorkMode.WorkMode_YTZOOM: // YTZOOM模式
                    waveManage_yt.refresh(); // 刷新YT模式波形管理器
                    WaveGridManage.getInstance().refresh(); // 刷新波形网格管理器
                    TChan.foreachChan(chan -> setOffset(chan)); // 遍历所有普通通道，重新设置偏移
                    TChan.foreachRef(refChan -> setRefOffset(refChan)); // 遍历所有参考通道，重新设置偏移
                    TChan.foreachMath(mathChan -> setMathOffset(mathChan)); // 遍历所有数学通道，重新设置偏移
                    break; // 结束分支
            }
        }
    };

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 波形标签管理变量区域                                                              │
    // │ 用于管理波形标签的选择和移动                                                      │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    private boolean isSelectLabel = false; // 是否选中波形标签，true表示选中，false表示未选中
    private int labelSelectChan = -1; // 选中的波形标签对应的通道索引，-1表示未选中
    private int isLabelTouchX = -1; // Label点击位置的X坐标，-1表示未点击
    private int labelTouchLeft; // 开始点击时的Label左边界位置

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ EventFactory事件观察者区域                                                       │
    // │ 用于监听和处理各种事件                                                          │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ eventUIObserver - UI事件观察者                                                ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * UI事件观察者，用于监听和处理各种UI事件。
     * 包括通道位置变化、数学通道位置变化、参考通道位置变化、波形标签选择和移动等事件。
     * 
     * 处理的事件类型：
     * 1. EVENT_CHANNEL_POS：通道位置变化事件，同步所有普通通道的位置
     * 2. EVENT_MATH_VPOS：数学通道位置变化事件，同步所有数学通道的位置
     * 3. EVENT_REF_VPOS：参考通道位置变化事件，同步所有参考通道的位置
     * 4. EVENT_WAVE_LABEL_SELECT：波形标签选择事件，处理标签点击
     * 5. EVENT_WAVE_LABEL_MOVE：波形标签移动事件，处理标签拖动
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 定义UI事件观察者
        /**
         * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
         * ┃ update() - 更新事件处理                                                      ┃
         * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
         * 
         * 更新事件处理，根据事件类型执行相应的处理逻辑。
         * 
         * @param data 事件数据对象
         */
        @Override
        public void update(Object data) { // 更新事件处理
            EventBase eventBase = (EventBase) data; // 将数据转换为EventBase对象
            int id = eventBase.getId(); // 获取事件ID
            if (id == EventFactory.EVENT_CHANNEL_POS) { // 如果是通道位置变化事件
                for (int i = ChannelFactory.CH1; i < ChannelFactory.CH_CNT; i++) { // 遍历所有通道（从CH1到CH_CNT）
//                    if (!ChannelFactory.isChOpen(i)) continue; // 注释掉的代码：如果通道未打开则跳过（已禁用）
                    Channel channel = ChannelFactory.getDynamicChannel(i); // 获取动态通道对象
                    if (channel == null) continue; // 如果通道对象为空，跳过
                    double y = ScopeBase.getNewHeight() / 2.0 - channel.getPosUI(); // 计算Y位置（屏幕中心减去UI位置）
                    y = Tools.YT2Zoom(y); // 将YT坐标转换为Zoom坐标
//                    Log.d(Tag.Debug, String.format("WaveManage.update CHAN newHeight:%s,PosUI:%s,y:%s,channel:%s", ScopeBase.getNewHeight(), channel.getPosUI(), y, channel.getName())); // 注释掉的代码：日志输出（已禁用）
//                    Log.d(Tag.Debug, String.format("WaveManage.update: %s,%s",y,getPositionY(TChan.toUiChNo(i)) )); // 注释掉的代码：日志输出（已禁用）
                    if (y != getPositionY(TChan.toUiChNo(i))) { // 如果计算的位置与当前位置不同
                        setPositionYFromEventBus(TChan.toUiChNo(i), y); // 从EventBus设置位置
                    }
                }
            } else if (id == EventFactory.EVENT_MATH_VPOS) { // 如果是数学通道位置变化事件
//                int chIdx = (int) eventBase.getData(); // 注释掉的代码：获取通道索引（已禁用）
//                if (ChannelFactory.isChOpen(chIdx)) { // 注释掉的代码：如果通道打开（已禁用）
                TChan.foreachMath(mathCh -> { // 遍历所有数学通道
                    MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathCh)); // 获取数学通道对象
                    if (mathChannel != null) { // 如果数学通道对象不为空
                        double y = ScopeBase.getNewHeight() / 2.0 - mathChannel.getPosUI(); // 计算Y位置（屏幕中心减去UI位置）
                        y = Tools.YT2Zoom(y); // 将YT坐标转换为Zoom坐标
//                        Log.d(Tag.Debug, String.format("WaveManage.update newHeight:%s,PosUI:%s,y:%s,channel:%s\"", ScopeBase.getNewHeight(), mathChannel.getPosUI(), y, mathChannel.getName())); // 注释掉的代码：日志输出（已禁用）
                        if (y != getPositionY(mathCh)) { // 如果计算的位置与当前位置不同
                            setPositionYFromEventBus(mathCh, y); // 从EventBus设置位置
                        }
                    }
                });
//                } // 注释掉的代码：if语句结束（已禁用）
            } else if (id == EventFactory.EVENT_REF_VPOS) { // 如果是参考通道位置变化事件
                TChan.foreachRef((ref)->{ // 遍历所有参考通道
                    int fpgaRef=TChan.toFpgaChNo(ref); // 将UI通道号转换为FPGA通道号
//                    if (ChannelFactory.isChOpen(fpgaRef)) { // 注释掉的代码：如果通道打开（已禁用）
                        double y = ScopeBase.getNewHeight() / 2.0 - ChannelFactory.getRefChannel(fpgaRef).getPosUI(); // 计算Y位置（屏幕中心减去UI位置）
                        y = Tools.YT2Zoom(y); // 将YT坐标转换为Zoom坐标
                        if (y != getPositionY(ref)) { // 如果计算的位置与当前位置不同
                            setPositionYFromEventBus(ref, y); // 从EventBus设置位置
                        }
//                    } // 注释掉的代码：if语句结束（已禁用）
                });
            } else if (id == EventFactory.EVENT_WAVE_LABEL_SELECT) { // 如果是波形标签选择事件（点击选中label）
                String point = (String) eventBase.getData(); // 获取点击坐标字符串
                String[] temp = point.split(";"); // 分割坐标字符串（格式：x;y）
                isSelectLabel = waveManage_yt.isLabelContains(Integer.parseInt(temp[0]), Integer.parseInt(temp[1])); // 检查点击位置是否在标签范围内
                labelSelectChan = waveManage_yt.getLabelSelectChan(); // 获取选中的标签对应的通道
                isLabelTouchX = Integer.parseInt(temp[0]); // 记录点击的X坐标
                labelTouchLeft = waveManage_yt.getLabelRect(labelSelectChan).left; // 记录标签的左边界位置
                Logger.i("WaveManage touch x= " + Integer.parseInt(temp[0]) + " ,y= " + Integer.parseInt(temp[1]) + " ,isLabelContains= " + isSelectLabel); // 输出日志信息
            } else if (id == EventFactory.EVENT_WAVE_LABEL_MOVE) { // 如果是波形标签移动事件（左右滑动label）
                String point = (String) eventBase.getData(); // 获取移动坐标字符串
                String[] temp = point.split(";"); // 分割坐标字符串（格式：x;y）
                if (isSelectLabel && isLabelTouchX != Integer.parseInt(temp[0])) { // 如果标签已选中且X坐标发生变化
                    Logger.i("WaveManage move deltaX= " + (Integer.parseInt(temp[0]) - isLabelTouchX) + " ,labelRect.left= " + labelTouchLeft + " ,isLabelTouchX= " + isLabelTouchX); // 输出日志信息
                    waveManage_yt.changeLabelPos(labelSelectChan, labelTouchLeft + Integer.parseInt(temp[0]) - isLabelTouchX); // 改变标签位置（原始左边界 + X坐标变化量）
                }
            }
        }
    };

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ UI组件引用变量区域                                                                │
    // │ 用于引用右侧布局和左侧位置视图                                                    │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    MainRightLayoutItemChannelBranch ch1Branch, ch2Branch, ch3Branch, ch4Branch, mathBranch, refBranch, s1Branch, s2Branch; // 右侧布局通道分支项引用（CH1-4、Math、Ref、S1-2）
    MainRightLayoutItemChannelMaster ch1Master, ch2Master, ch3Master, ch4Master,ch5Master,ch6Master,ch7Master,ch8Master; // 右侧布局通道主项引用（CH1-8）
    MainRightLayoutItemChannelMaster ref1Master, ref2Master, ref3Master, ref4Master, ref5Master, ref6Master, ref7Master, ref8Master; // 右侧布局参考通道主项引用（Ref1-8）
    MainRightLayoutItemChannelMaster math1Master, math2Master, math3Master, math4Master, math5Master, math6Master, math7Master, math8Master; // 右侧布局数学通道主项引用（Math1-8）

    LeftPositionView leftPositionView; // 左侧位置视图引用，用于显示通道位置信息

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 波形移动事件回调区域                                                              │
    // │ 处理波形移动时的各种联动操作                                                      │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ onMovingWaveEvent - 波形移动事件回调                                          ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 波形移动事件回调，用于处理波形移动时的各种联动操作。
     * 包括XY模式下的X/Y轴偏移更新、YT模式下的通道位置更新、触发线联动、串行总线电平更新等。
     * 
     * 处理逻辑：
     * 1. XY模式：
     *    - CH1/3/5/7控制X轴偏移
     *    - CH2/4/6/8控制Y轴偏移
     *    - 更新DisplayXYService的X/Y值
     *    - 保存位置到缓存
     *    - 更新光标位置
     * 
     * 2. YT/YTZOOM模式：
     *    - 更新通道位置到Command
     *    - 保存位置到缓存
     *    - 更新通道对象的Pos值
     *    - 更新UI显示的偏移值
     *    - 联动触发线位置
     *    - 联动串行总线电平
     *    - 更新光标位置
     */
    private IWave.OnMovingWaveEvent onMovingWaveEvent = new IWave.OnMovingWaveEvent() { // 定义波形移动事件回调
        /**
         * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
         * ┃ OnMovingWave() - 波形移动事件处理                                            ┃
         * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
         * 
         * 波形移动事件处理，根据工作模式和通道类型执行相应的联动操作。
         * 
         * @param iWave 波形接口对象
         * @param x X坐标（用于XY模式的X轴偏移）
         * @param y Y坐标（用于YT模式的Y轴位置和XY模式的Y轴偏移）
         * @param isSwitchWorkMode 是否正在切换工作模式
         * @param isFromEventBus 是否来自EventBus事件
         */
        @Override
        public void OnMovingWave(IWave iWave, long x, double y, boolean isSwitchWorkMode, boolean isFromEventBus) { // 波形移动事件处理

            switch (WorkModeManage.getInstance().getmWorkMode()) { // 根据当前工作模式进行分支处理
                case IWorkMode.WorkMode_XY: { // XY模式
                    switch (iWave.getLineNameID()) { // 根据波形通道ID进行分支处理
                        case TChan.Ch1: // CH1通道（X轴）
                        case TChan.Ch3: // CH3通道（X轴）
                        case TChan.Ch5: // CH5通道（X轴）
                        case TChan.Ch7: // CH7通道（X轴）
                            if (!isFromEventBus) { // 如果不是来自EventBus
                                DisplayXYService.getInstance().setX(ScopeBase.getXYWidth() / 2 - (int) x); // 设置DisplayXYService的X偏移（屏幕中心减去X坐标）
                            }
                            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_XY_POSITION + iWave.getLineNameID(), String.valueOf(x)); // 保存X位置到缓存
                            break; // 结束分支
                        case TChan.Ch2: // CH2通道（Y轴）
                        case TChan.Ch4: // CH4通道（Y轴）
                        case TChan.Ch6: // CH6通道（Y轴）
                        case TChan.Ch8: // CH8通道（Y轴）
                            if (!isFromEventBus) { // 如果不是来自EventBus
                                DisplayXYService.getInstance().setY(ScopeBase.getXYHeight() / 2 - (int) Math.round(y)); // 设置DisplayXYService的Y偏移（屏幕中心减去Y坐标）
                            }
                            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_XY_POSITION + iWave.getLineNameID(), String.valueOf(y)); // 保存Y位置到缓存
                            break; // 结束分支
//                        case IWave.Ch3: // 注释掉的代码：CH3通道处理（已禁用）
//                            if (!isFromEventBus) { // 注释掉的代码：如果不是来自EventBus（已禁用）
//                                DisplayXYService.getInstance().setX(ScopeBase.getXYWidth() / 2 - (int) x); // 注释掉的代码：设置X偏移（已禁用）
//                            } // 注释掉的代码：if语句结束（已禁用）
//                            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_XY_POSITION + iWave.getLineNameID(), String.valueOf(x)); // 注释掉的代码：保存X位置（已禁用）
//                            break; // 注释掉的代码：结束分支（已禁用）
//                        case IWave.Ch4: // 注释掉的代码：CH4通道处理（已禁用）
//                            if (!isFromEventBus) { // 注释掉的代码：如果不是来自EventBus（已禁用）
//                                DisplayXYService.getInstance().setY(ScopeBase.getXYHeight() / 2 - y); // 注释掉的代码：设置Y偏移（已禁用）
//                            } // 注释掉的代码：if语句结束（已禁用）
//                            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_XY_POSITION + iWave.getLineNameID(), String.valueOf(y)); // 注释掉的代码：保存Y位置（已禁用）
//                            break; // 注释掉的代码：结束分支（已禁用）
                    }
                    if (!isSwitchWorkMode) { // 如果不是正在切换工作模式
                        CursorManage.getInstance().curChannelMove(); // 更新光标位置（当前通道移动）
                    }
                }
                break; // 结束XY模式分支
                case IWorkMode.WorkMode_YT: // YT模式
                case IWorkMode.WorkMode_YTZOOM: // YTZOOM模式
                    if (leftPositionView == null) { // 如果左侧位置视图引用为空
                        leftPositionView = (LeftPositionView) mainViewGroup.findViewById(R.id.leftPositionView); // 从主视图组获取左侧位置视图引用
                    }
                    int chIdx = iWave.getLineNameID(); // 获取波形通道索引
                    Command.get().getChannel().setPosition(chIdx - 1, y); // 设置Command中的通道位置（通道索引-1转换为FPGA索引）
                    Tools.putChannelPosition(chIdx, y); // 保存通道位置到缓存（这里是保存的像素位置）
                    y = Tools.getYTChannelPosition(chIdx); // 将像素位置转换为正负偏移量
                    int idx = chIdx -1; // 计算FPGA通道索引（通道索引-1）
                    if(ChannelFactory.isDynamicCh(idx)){ // 如果是动态通道（普通通道）
                        if (!isFromEventBus) { // 如果不是来自EventBus
                            Channel channel = ChannelFactory.getDynamicChannel(idx); // 获取动态通道对象
                            if (channel != null) { // 如果通道对象不为空
                                channel.setPos(y, true); // 设置通道位置（第二个参数true表示需要同步）
                            }
                        }
                        setOffset(chIdx); // 设置通道偏移（更新UI显示）
                    }else if(ChannelFactory.isMathCh(idx)){ // 如果是数学通道
                        if (!isFromEventBus) { // 如果不是来自EventBus
                            MathChannel mathChannel = ChannelFactory.getMathChannel(idx); // 获取数学通道对象
                            if(mathChannel != null){ // 如果数学通道对象不为空
                                if (mathChannel.getPosUI() != y) { // 如果UI位置与新位置不同
                                    mathChannel.setPos(y); // 设置数学通道位置
                                }
                            }
                        }
//                        if (mathBranch == null) { // 注释掉的代码：如果数学分支引用为空（已禁用）
//                            mathBranch = (MainRightLayoutItemChannelBranch) mainViewGroup.findViewById(R.id.rightMathBranch); // 注释掉的代码：获取数学分支引用（已禁用）
//                        } // 注释掉的代码：if语句结束（已禁用）
                        setMathOffset(TChan.toUiChNo(idx)); // 设置数学通道偏移（更新UI显示）
                    }else if(ChannelFactory.isRefCh(idx)){ // 如果是参考通道
                        if (!isFromEventBus) { // 如果不是来自EventBus
                            RefChannel refChannel = ChannelFactory.getRefChannel(idx); // 获取参考通道对象
                            if (refChannel != null && refChannel.getPosUI() != y) { // 如果参考通道对象不为空且UI位置与新位置不同
                                refChannel.setPos(y); // 设置参考通道位置
                            }
                        }
//                        if (refBranch == null) { // 注释掉的代码：如果参考分支引用为空（已禁用）
//                            refBranch = (MainRightLayoutItemChannelBranch) mainViewGroup.findViewById(R.id.rightRefBranch); // 注释掉的代码：获取参考分支引用（已禁用）
//                        } // 注释掉的代码：if语句结束（已禁用）
//                        setRefOffset(refBranch, idx); // 注释掉的代码：设置参考通道偏移（已禁用）
                        setRefOffset(TChan.toUiChNo(idx)); // 设置参考通道偏移（更新UI显示）
                    }




                    ITriggerLine triggerLine = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger); // 获取触发线对象
                    int curCh = iWave.getLineNameID(); // 获取当前通道索引（1~4）

                    if (ChannelFactory.isChActivate(TChan.toFpgaChNo(curCh)) && !isSwitchWorkMode) // 如果通道激活且不是正在切换工作模式
                        CursorManage.getInstance().curChannelMove(); // 更新光标位置（当前通道移动）

                    if (!TChan.isChan(curCh)) return; // 如果不是普通通道，直接返回

                    double triggerChannelTempH = Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP_H + curCh); // 获取触发通道高电平临时缓存值
                    double triggerChannelTemp = Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + curCh); // 获取触发通道低电平临时缓存值


                    if (triggerLine.getShowMode() == ITriggerLine.ShowMode_Two) { // 如果触发线显示模式为双线模式
                        if (iWave.getLineNameID() == triggerLine.getChannelId()) { // 如果当前通道是触发线关联的通道
                            triggerLine.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - triggerChannelTempH); // 设置触发线高电平Y位置（当前波形位置减去高电平临时值）
                            triggerLine.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - triggerChannelTemp); // 设置触发线低电平Y位置（当前波形位置减去低电平临时值）
                            Tools.putLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh, // 保存触发通道高电平缓存值
                                    getPositionY(curCh) - triggerLine.getOtherY(ITriggerLine.VoltageLine_High)); // 当前波形位置减去触发线高电平位置
                            Tools.putLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh, // 保存触发通道低电平缓存值
                                   getPositionY(curCh) - triggerLine.getOtherY(ITriggerLine.VoltageLine_Low)); // 当前波形位置减去触发线低电平位置
                            sendMsgWaveToLevel(MsgWaveToLevel.LEVELTYPE_TRIGGER, curCh); // 发送波形到电平消息（触发类型）
                            Trigger trigger = TriggerFactory.getInstance().getTrigger(); // 获取触发器对象
                            if (trigger.isTriggerSource( TChan.toFpgaChNo(curCh))) { // 如果当前通道是触发源
                                trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, TChan.toFpgaChNo(curCh)) // 获取触发低电平对象
                                        .setPos(Tools.getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh)); // 设置触发低电平位置
                                trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_HIGH, TChan.toFpgaChNo(curCh)) // 获取触发高电平对象
                                        .setPos(Tools.getYTLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh)); // 设置触发高电平位置
                            }
                        }
                    } else { // 如果触发线显示模式为单线模式
                        triggerLine.setOtherY(curCh, getPositionY(curCh) - triggerChannelTemp); // 设置触发线Y位置（当前波形位置减去临时值）

                        Tools.putLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh, getPositionY(curCh) - triggerLine.getOtherY(curCh)); // 保存触发通道缓存值（当前波形位置减去触发线位置）
                        sendMsgWaveToLevel(MsgWaveToLevel.LEVELTYPE_TRIGGER, curCh); // 发送波形到电平消息（触发类型）
                        Trigger trigger = TriggerFactory.getInstance().getTrigger(); // 获取触发器对象
                        if (trigger.isTriggerSource(TChan.toFpgaChNo(curCh))) { // 如果当前通道是触发源
                            if(!isFromEventBus) { // 如果不是来自EventBus
                                trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, TChan.toFpgaChNo(curCh)) // 获取触发低电平对象
                                        .setPos(Tools.getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh)); // 设置触发低电平位置
                            }
                        }
                    }
                    int serials = 0; // 初始化串行总线编号（0表示无串行总线）
                    boolean s1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1); // 获取S1串行总线是否启用
                    boolean s2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2); // 获取S2串行总线是否启用
                    boolean s3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3); // 获取S3串行总线是否启用
                    boolean s4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4); // 获取S4串行总线是否启用
                    if (s1 || s2 || s3 || s4) { // 如果有任意串行总线启用
                        SerialChannel serialChannel; // 定义串行通道变量
                        int maxIdx = ChannelFactory.getMaxSerialIdx(); // 获取最大串行索引
                        for (int i = ChannelFactory.S1; i < maxIdx; i++) { // 遍历所有串行通道（从S1到最大索引）
                            serialChannel = ChannelFactory.getSerialChannel(i); // 获取串行通道对象
                            if (serialChannel != null && serialChannel.isOpen()) { // 如果串行通道对象不为空且已打开
                                IBus bus = serialChannel.getBus(); // 获取总线对象
                                if (bus != null && bus.isChInSample(TChan.toFpgaChNo(curCh))) { // 如果总线对象不为空且当前通道在采样中
                                    serials = CacheUtil.S1 + (i - ChannelFactory.S1); // 计算串行总线编号（S1 + 偏移量）
                                    break; // 找到匹配的串行总线，跳出循环
                                }
                            }
                        }
                    }
//                    Logger.e("limh", "serials= " + serials); // 注释掉的代码：日志输出（已禁用）
                    if(serials == 0) break; // 如果没有匹配的串行总线，跳出switch
                    if (serials == CacheUtil.S1) { // 如果是S1串行总线
                        CacheUtil.get().setValueLevelSerials(CacheUtil.S1); // 设置值电平串行总线为S1
                        double valueChannelTempH = Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + curCh); // 获取值通道高电平临时缓存值
                        double valueChannelTemp = Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + curCh); // 获取值通道低电平临时缓存值
                        int i = 0; // 初始化索引变量（未使用）
                        ITriggerLine valueLine = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1); // 获取值线1对象
                        ITriggerLine valueLine2 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2); // 获取值线2对象
                        ITriggerLine valueLine3 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3); // 获取值线3对象
                        ITriggerLine valueLine4 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4); // 获取值线4对象
                        boolean s2M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2) == CacheUtil.M429; // 判断S2是否为M429类型
                        boolean s3M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3) == CacheUtil.M429; // 判断S3是否为M429类型
                        boolean s4M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4) == CacheUtil.M429; // 判断S4是否为M429类型
                        int levelType = MsgWaveToLevel.LEVELTYPE_VALUE1; // 设置电平类型为值线1
                        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1) == CacheUtil.M429) { // 如果S1是M429类型
                            valueLine.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线1显示模式为双线模式
                            if (s2M429) { // 如果S2是M429类型
                                valueLine2.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线2显示模式为双线模式
                            } else { // 如果S2不是M429类型
                                valueLine2.setShowMode(ITriggerLine.ShowMode_One); // 设置值线2显示模式为单线模式
                            }
                            if (s3M429) { // 如果S3是M429类型
                                valueLine3.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线3显示模式为双线模式
                            } else { // 如果S3不是M429类型
                                valueLine3.setShowMode(ITriggerLine.ShowMode_One); // 设置值线3显示模式为单线模式
                            }
                            if (s4M429) { // 如果S4是M429类型
                                valueLine4.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线4显示模式为双线模式
                            } else { // 如果S4不是M429类型
                                valueLine4.setShowMode(ITriggerLine.ShowMode_One); // 设置值线4显示模式为单线模式
                            }
                            if (iWave.getLineNameID() == valueLine.getChannelId()) { // 如果当前通道是值线1关联的通道
                                if (s2M429) { // 如果S2是M429类型
                                    if(valueLine2.getChannelId() == curCh) { // 如果值线2关联的通道是当前通道
                                        valueLine2.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线2高电平Y位置
                                        valueLine2.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线2低电平Y位置
                                    }
                                } else { // 如果S2不是M429类型
                                    valueLine2.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线2Y位置（单线模式）
                                }
                                if (s3M429) { // 如果S3是M429类型
                                    if(valueLine3.getChannelId() == curCh) { // 如果值线3关联的通道是当前通道
                                        valueLine3.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线3高电平Y位置
                                        valueLine3.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线3低电平Y位置
                                    }
                                } else { // 如果S3不是M429类型
                                    valueLine3.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线3Y位置（单线模式）
                                }
                                if (s4M429) { // 如果S4是M429类型
                                    if(valueLine4.getChannelId() == curCh) { // 如果值线4关联的通道是当前通道
                                        valueLine4.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线4高电平Y位置
                                        valueLine4.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线4低电平Y位置
                                    }
                                } else { // 如果S4不是M429类型
                                    valueLine4.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线4Y位置（单线模式）
                                }

                                valueLine.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线1高电平Y位置
                                valueLine.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线1低电平Y位置
                                Tools.putLevelCache(CacheUtil.VALUE_CHANNEL_H + curCh, // 保存值通道高电平缓存值
                                        getPositionY(curCh) - valueLine.getOtherY(ITriggerLine.VoltageLine_High)); // 当前波形位置减去值线高电平位置
                                Tools.putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, // 保存值通道低电平缓存值
                                       getPositionY(curCh) - valueLine.getOtherY(ITriggerLine.VoltageLine_Low)); // 当前波形位置减去值线低电平位置
                                sendMsgWaveToLevel(levelType, curCh); // 发送波形到电平消息（值线1类型）
                            }
                        } else { // 如果S1不是M429类型
                            valueLine.setShowMode(ITriggerLine.ShowMode_One); // 设置值线1显示模式为单线模式
                            if (s2M429) { // 如果S2是M429类型
                                if(valueLine2.getChannelId() == curCh) { // 如果值线2关联的通道是当前通道
                                    valueLine2.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线2高电平Y位置
                                    valueLine2.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线2低电平Y位置
                                }
                            } else { // 如果S2不是M429类型
                                valueLine2.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线2Y位置（单线模式）
                            }
                            if (s3M429) { // 如果S3是M429类型
                                if(valueLine3.getChannelId() == curCh) { // 如果值线3关联的通道是当前通道
                                    valueLine3.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线3高电平Y位置
                                    valueLine3.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线3低电平Y位置
                                }
                            } else { // 如果S3不是M429类型
                                valueLine3.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线3Y位置（单线模式）
                            }
                            if (s4M429) { // 如果S4是M429类型
                                if(valueLine4.getChannelId() == curCh) { // 如果值线4关联的通道是当前通道
                                    valueLine4.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线4高电平Y位置
                                    valueLine4.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线4低电平Y位置
                                }
                            } else { // 如果S4不是M429类型
                                valueLine4.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线4Y位置（单线模式）
                            }


                            valueLine.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线1Y位置（单线模式）
                            Tools.putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, getPositionY(curCh) - valueLine.getOtherY(curCh)); // 保存值通道缓存值（当前波形位置减去值线位置）
                            sendMsgWaveToLevel(levelType, curCh); // 发送波形到电平消息（值线1类型）
                        }
                        CacheUtil.get().setValueLevelSerials(0); // 重置值电平串行总线为0
                    } else if (serials == CacheUtil.S2) { // 如果是S2串行总线
                        CacheUtil.get().setValueLevelSerials(CacheUtil.S2); // 设置值电平串行总线为S2
                        double valueChannelTempH = Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + curCh); // 获取值通道高电平临时缓存值
                        double valueChannelTemp = Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + curCh); // 获取值通道低电平临时缓存值
                        int i = 1; // 初始化索引变量（未使用）
                        ITriggerLine valueLine = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2); // 获取值线2对象
                        ITriggerLine valueLine1 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1); // 获取值线1对象
                        ITriggerLine valueLine3 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3); // 获取值线3对象
                        ITriggerLine valueLine4 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4); // 获取值线4对象
                        boolean s1M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1) == CacheUtil.M429; // 判断S1是否为M429类型
                        boolean s3M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3) == CacheUtil.M429; // 判断S3是否为M429类型
                        boolean s4M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4) == CacheUtil.M429; // 判断S4是否为M429类型
                        int levelType = MsgWaveToLevel.LEVELTYPE_VALUE2; // 设置电平类型为值线2
                        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2) == CacheUtil.M429) { // 如果S2是M429类型
                            valueLine.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线2显示模式为双线模式
                            if (s1M429) { // 如果S1是M429类型
                                valueLine1.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线1显示模式为双线模式
                            } else { // 如果S1不是M429类型
                                valueLine1.setShowMode(ITriggerLine.ShowMode_One); // 设置值线1显示模式为单线模式
                            }
                            if (s3M429) { // 如果S3是M429类型
                                valueLine3.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线3显示模式为双线模式
                            } else { // 如果S3不是M429类型
                                valueLine3.setShowMode(ITriggerLine.ShowMode_One); // 设置值线3显示模式为单线模式
                            }
                            if (s4M429) { // 如果S4是M429类型
                                valueLine4.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线4显示模式为双线模式
                            } else { // 如果S4不是M429类型
                                valueLine4.setShowMode(ITriggerLine.ShowMode_One); // 设置值线4显示模式为单线模式
                            }

                            if (iWave.getLineNameID() == valueLine.getChannelId()) { // 如果当前通道是值线2关联的通道
                                if (s1M429) { // 如果S1是M429类型
                                    if(valueLine1.getChannelId() == curCh) { // 如果值线1关联的通道是当前通道
                                        valueLine1.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线1高电平Y位置
                                        valueLine1.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线1低电平Y位置
                                    }
                                } else { // 如果S1不是M429类型
                                    valueLine1.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线1Y位置（单线模式）
                                }
                                if (s3M429) { // 如果S3是M429类型
                                    if(valueLine3.getChannelId() == curCh) { // 如果值线3关联的通道是当前通道
                                        valueLine3.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线3高电平Y位置
                                        valueLine3.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线3低电平Y位置
                                    }
                                } else { // 如果S3不是M429类型
                                    valueLine3.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线3Y位置（单线模式）
                                }
                                if (s4M429) { // 如果S4是M429类型
                                    if(valueLine4.getChannelId() == curCh) { // 如果值线4关联的通道是当前通道
                                        valueLine4.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线4高电平Y位置
                                        valueLine4.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线4低电平Y位置
                                    }
                                } else { // 如果S4不是M429类型
                                    valueLine4.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线4Y位置（单线模式）
                                }

                                valueLine.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线2高电平Y位置
                                valueLine.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线2低电平Y位置
                                Tools.putLevelCache(CacheUtil.VALUE_CHANNEL_H + curCh, // 保存值通道高电平缓存值
                                        getPositionY(curCh) - valueLine.getOtherY(ITriggerLine.VoltageLine_High)); // 当前波形位置减去值线高电平位置
                                Tools.putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, // 保存值通道低电平缓存值
                                        getPositionY(curCh) - valueLine.getOtherY(ITriggerLine.VoltageLine_Low)); // 当前波形位置减去值线低电平位置
                                sendMsgWaveToLevel(levelType, curCh); // 发送波形到电平消息（值线2类型）
                            }
                        } else { // 如果S2不是M429类型
                            valueLine.setShowMode(ITriggerLine.ShowMode_One); // 设置值线2显示模式为单线模式
                            if (s1M429) { // 如果S1是M429类型
                                if(valueLine1.getChannelId() == curCh) { // 如果值线1关联的通道是当前通道
                                    valueLine1.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线1高电平Y位置
                                    valueLine1.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线1低电平Y位置
                                }
                            } else { // 如果S1不是M429类型
                                valueLine1.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线1Y位置（单线模式）
                            }
                            if (s3M429) { // 如果S3是M429类型
                                if(valueLine3.getChannelId() == curCh) { // 如果值线3关联的通道是当前通道
                                    valueLine3.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线3高电平Y位置
                                    valueLine3.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线3低电平Y位置
                                }
                            } else { // 如果S3不是M429类型
                                valueLine3.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线3Y位置（单线模式）
                            }
                            if (s4M429) { // 如果S4是M429类型
                                if(valueLine4.getChannelId() == curCh) { // 如果值线4关联的通道是当前通道
                                    valueLine4.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线4高电平Y位置
                                    valueLine4.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线4低电平Y位置
                                }
                            } else { // 如果S4不是M429类型
                                valueLine4.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线4Y位置（单线模式）
                            }
                            valueLine.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线2Y位置（单线模式）
                            Tools.putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, // 保存值通道缓存值
                                   getPositionY(curCh) - valueLine.getOtherY(curCh)); // 当前波形位置减去值线位置
                            sendMsgWaveToLevel(levelType, curCh); // 发送波形到电平消息（值线2类型）
                        }
                        CacheUtil.get().setValueLevelSerials(0); // 重置值电平串行总线为0
                    } else if (serials == CacheUtil.S3) { // 如果是S3串行总线
                        CacheUtil.get().setValueLevelSerials(CacheUtil.S3); // 设置值电平串行总线为S3
                        double valueChannelTempH = Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + curCh); // 获取值通道高电平临时缓存值
                        double valueChannelTemp = Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + curCh); // 获取值通道低电平临时缓存值
                        int i = 1; // 初始化索引变量（未使用）
                        ITriggerLine valueLine = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3); // 获取值线3对象
                        ITriggerLine valueLine1 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1); // 获取值线1对象
                        ITriggerLine valueLine2 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2); // 获取值线2对象
                        ITriggerLine valueLine4 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4); // 获取值线4对象
                        boolean s1M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1) == CacheUtil.M429; // 判断S1是否为M429类型
                        boolean s2M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2) == CacheUtil.M429; // 判断S2是否为M429类型
                        boolean s4M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4) == CacheUtil.M429; // 判断S4是否为M429类型
                        int levelType = MsgWaveToLevel.LEVELTYPE_VALUE3; // 设置电平类型为值线3
                        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3) == CacheUtil.M429) { // 如果S3是M429类型
                            valueLine.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线3显示模式为双线模式
                            if (s1M429) { // 如果S1是M429类型
                                valueLine1.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线1显示模式为双线模式
                            } else { // 如果S1不是M429类型
                                valueLine1.setShowMode(ITriggerLine.ShowMode_One); // 设置值线1显示模式为单线模式
                            }
                            if (s2M429) { // 如果S2是M429类型
                                valueLine2.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线2显示模式为双线模式
                            } else { // 如果S2不是M429类型
                                valueLine2.setShowMode(ITriggerLine.ShowMode_One); // 设置值线2显示模式为单线模式
                            }
                            if (s4M429) { // 如果S4是M429类型
                                valueLine4.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线4显示模式为双线模式
                            } else { // 如果S4不是M429类型
                                valueLine4.setShowMode(ITriggerLine.ShowMode_One); // 设置值线4显示模式为单线模式
                            }

                            if (iWave.getLineNameID() == valueLine.getChannelId()) { // 如果当前通道是值线3关联的通道
                                if (s1M429) { // 如果S1是M429类型
                                    if(valueLine1.getChannelId() == curCh) { // 如果值线1关联的通道是当前通道
                                        valueLine1.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线1高电平Y位置
                                        valueLine1.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线1低电平Y位置
                                    }
                                } else { // 如果S1不是M429类型
                                    valueLine1.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线1Y位置（单线模式）
                                }
                                if (s2M429) { // 如果S2是M429类型
                                    if(valueLine2.getChannelId() == curCh) { // 如果值线2关联的通道是当前通道
                                        valueLine2.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线2高电平Y位置
                                        valueLine2.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线2低电平Y位置
                                    }
                                } else { // 如果S2不是M429类型
                                    valueLine2.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线2Y位置（单线模式）
                                }
                                if (s4M429) { // 如果S4是M429类型
                                    if(valueLine4.getChannelId() == curCh) { // 如果值线4关联的通道是当前通道
                                        valueLine4.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线4高电平Y位置
                                        valueLine4.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线4低电平Y位置
                                    }
                                } else { // 如果S4不是M429类型
                                    valueLine4.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线4Y位置（单线模式）
                                }

                                valueLine.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线3高电平Y位置
                                valueLine.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线3低电平Y位置
                                Tools.putLevelCache(CacheUtil.VALUE_CHANNEL_H + curCh, // 保存值通道高电平缓存值
                                      getPositionY(curCh) - valueLine.getOtherY(ITriggerLine.VoltageLine_High)); // 当前波形位置减去值线高电平位置
                                Tools.putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, // 保存值通道低电平缓存值
                                       getPositionY(curCh) - valueLine.getOtherY(ITriggerLine.VoltageLine_Low)); // 当前波形位置减去值线低电平位置
                                sendMsgWaveToLevel(levelType, curCh); // 发送波形到电平消息（值线3类型）
                            }
                        } else { // 如果S3不是M429类型
                            valueLine.setShowMode(ITriggerLine.ShowMode_One); // 设置值线3显示模式为单线模式
                            if (s1M429) { // 如果S1是M429类型
                                if(valueLine1.getChannelId() == curCh) { // 如果值线1关联的通道是当前通道
                                    valueLine1.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线1高电平Y位置
                                    valueLine1.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线1低电平Y位置
                                }
                            } else { // 如果S1不是M429类型
                                valueLine1.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线1Y位置（单线模式）
                            }
                            if (s2M429) { // 如果S2是M429类型
                                if(valueLine2.getChannelId() == curCh) { // 如果值线2关联的通道是当前通道
                                    valueLine2.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线2高电平Y位置
                                    valueLine2.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线2低电平Y位置
                                }
                            } else { // 如果S2不是M429类型
                                valueLine2.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线2Y位置（单线模式）
                            }
                            if (s4M429) { // 如果S4是M429类型
                                if(valueLine4.getChannelId() == curCh) { // 如果值线4关联的通道是当前通道
                                    valueLine4.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线4高电平Y位置
                                    valueLine4.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线4低电平Y位置
                                }
                            } else { // 如果S4不是M429类型
                                valueLine4.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线4Y位置（单线模式）
                            }
                            valueLine.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线3Y位置（单线模式）
                            Tools.putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, // 保存值通道缓存值
                                    getPositionY(curCh) - valueLine.getOtherY(curCh)); // 当前波形位置减去值线位置
                            sendMsgWaveToLevel(levelType, curCh); // 发送波形到电平消息（值线3类型）
                        }
                        CacheUtil.get().setValueLevelSerials(0); // 重置值电平串行总线为0
                    } else if (serials == CacheUtil.S4) { // 如果是S4串行总线
                        CacheUtil.get().setValueLevelSerials(CacheUtil.S4); // 设置值电平串行总线为S4
                        double valueChannelTempH = Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + curCh); // 获取值通道高电平临时缓存值
                        double valueChannelTemp = Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + curCh); // 获取值通道低电平临时缓存值
                        int i = 1; // 初始化索引变量（未使用）
                        ITriggerLine valueLine = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4); // 获取值线4对象
                        ITriggerLine valueLine1 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1); // 获取值线1对象
                        ITriggerLine valueLine2 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2); // 获取值线2对象
                        ITriggerLine valueLine3 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3); // 获取值线3对象
                        boolean s1M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1) == CacheUtil.M429; // 判断S1是否为M429类型
                        boolean s2M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2) == CacheUtil.M429; // 判断S2是否为M429类型
                        boolean s3M429 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3) == CacheUtil.M429; // 判断S3是否为M429类型
                        int levelType = MsgWaveToLevel.LEVELTYPE_VALUE4; // 设置电平类型为值线4
                        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4) == CacheUtil.M429) { // 如果S4是M429类型
                            valueLine.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线4显示模式为双线模式
                            if (s1M429) { // 如果S1是M429类型
                                valueLine1.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线1显示模式为双线模式
                            } else { // 如果S1不是M429类型
                                valueLine1.setShowMode(ITriggerLine.ShowMode_One); // 设置值线1显示模式为单线模式
                            }
                            if (s2M429) { // 如果S2是M429类型
                                valueLine2.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线2显示模式为双线模式
                            } else { // 如果S2不是M429类型
                                valueLine2.setShowMode(ITriggerLine.ShowMode_One); // 设置值线2显示模式为单线模式
                            }
                            if (s3M429) { // 如果S3是M429类型
                                valueLine3.setShowMode(ITriggerLine.ShowMode_Two); // 设置值线3显示模式为双线模式
                            } else { // 如果S3不是M429类型
                                valueLine3.setShowMode(ITriggerLine.ShowMode_One); // 设置值线3显示模式为单线模式
                            }

                            if (iWave.getLineNameID() == valueLine.getChannelId()) { // 如果当前通道是值线4关联的通道
                                if (s1M429) { // 如果S1是M429类型
                                    if(valueLine1.getChannelId() == curCh) { // 如果值线1关联的通道是当前通道
                                        valueLine1.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线1高电平Y位置
                                        valueLine1.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线1低电平Y位置
                                    }
                                } else { // 如果S1不是M429类型
                                    valueLine1.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线1Y位置（单线模式）
                                }
                                if (s2M429) { // 如果S2是M429类型
                                    if(valueLine2.getChannelId() == curCh) { // 如果值线2关联的通道是当前通道
                                        valueLine2.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线2高电平Y位置
                                        valueLine2.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线2低电平Y位置
                                    }
                                } else { // 如果S2不是M429类型
                                    valueLine2.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线2Y位置（单线模式）
                                }
                                if (s3M429) { // 如果S3是M429类型
                                    if(valueLine3.getChannelId() == curCh) { // 如果值线3关联的通道是当前通道
                                        valueLine3.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线3高电平Y位置
                                        valueLine3.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线3低电平Y位置
                                    }
                                } else { // 如果S3不是M429类型
                                    valueLine3.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线3Y位置（单线模式）
                                }

                                valueLine.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线4高电平Y位置
                                valueLine.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线4低电平Y位置
                                Tools.putLevelCache(CacheUtil.VALUE_CHANNEL_H + curCh, // 保存值通道高电平缓存值
                                        getPositionY(curCh) - valueLine.getOtherY(ITriggerLine.VoltageLine_High)); // 当前波形位置减去值线高电平位置
                                Tools.putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, // 保存值通道低电平缓存值
                                        getPositionY(curCh) - valueLine.getOtherY(ITriggerLine.VoltageLine_Low)); // 当前波形位置减去值线低电平位置
                                sendMsgWaveToLevel(levelType, curCh); // 发送波形到电平消息（值线4类型）
                            }
                        } else { // 如果S4不是M429类型
                            valueLine.setShowMode(ITriggerLine.ShowMode_One); // 设置值线4显示模式为单线模式
                            if (s1M429) { // 如果S1是M429类型
                                if(valueLine1.getChannelId() == curCh) { // 如果值线1关联的通道是当前通道
                                    valueLine1.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线1高电平Y位置
                                    valueLine1.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线1低电平Y位置
                                }
                            } else { // 如果S1不是M429类型
                                valueLine1.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线1Y位置（单线模式）
                            }
                            if (s2M429) { // 如果S2是M429类型
                                if(valueLine2.getChannelId() == curCh) { // 如果值线2关联的通道是当前通道
                                    valueLine2.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线2高电平Y位置
                                    valueLine2.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线2低电平Y位置
                                }
                            } else { // 如果S2不是M429类型
                                valueLine2.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线2Y位置（单线模式）
                            }
                            if (s3M429) { // 如果S3是M429类型
                                if(valueLine3.getChannelId() == curCh) { // 如果值线3关联的通道是当前通道
                                    valueLine3.setOtherY(ITriggerLine.VoltageLine_High, getPositionY(curCh) - valueChannelTempH); // 设置值线3高电平Y位置
                                    valueLine3.setOtherY(ITriggerLine.VoltageLine_Low, getPositionY(curCh) - valueChannelTemp); // 设置值线3低电平Y位置
                                }
                            } else { // 如果S3不是M429类型
                                valueLine3.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线3Y位置（单线模式）
                            }
                            valueLine.setOtherY(curCh, getPositionY(curCh) - valueChannelTemp); // 设置值线4Y位置（单线模式）
                            Tools.putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, // 保存值通道缓存值
                                   getPositionY(curCh) - valueLine.getOtherY(curCh)); // 当前波形位置减去值线位置
                            sendMsgWaveToLevel(levelType, curCh); // 发送波形到电平消息（值线4类型）
                        }
                        CacheUtil.get().setValueLevelSerials(0); // 重置值电平串行总线为0
                    }

                    if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1) // 如果S1串行总线启用
                            || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2) // 或者S2串行总线启用
                            || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3) // 或者S3串行总线启用
                            || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4) // 或者S4串行总线启用
                    ) {
                        if (!isFromEventBus) { // 如果不是来自EventBus
                            changeSerialTrigVol_channel(curCh - 1); // 更新串行总线电平（通道索引-1转换为FPGA索引）
                        }
                    }
                    break; // 结束YT/YTZOOM模式分支
            }
        }
    };



    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 偏移设置方法区域                                                                  │
    // │ 用于设置各种通道的偏移值                                                          │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ setRefOffset() - 设置参考通道偏移                                              ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 设置参考通道的偏移值，更新右侧布局显示。
     * 计算参考通道的实际位置和偏移值，并更新对应的UI组件。
     * 
     * 处理流程：
     * 1. 获取参考通道对象
     * 2. 计算左侧位置（像素位置）
     * 3. 计算缩放高度和偏移值
     * 4. 获取单位（V或A）
     * 5. 计算数值（位置 * 每像素垂直值）
     * 6. 根据通道索引更新对应的右侧布局主项
     * 
     * @param chIndex 参考通道索引（R1-8）
     */
    public void setRefOffset(int chIndex) { // 设置参考通道偏移
        RefChannel refChannel = (RefChannel) ChannelFactory.getRefChannel(TChan.toFpgaChNo(chIndex)); // 获取参考通道对象
        if (refChannel==null) return; // 如果参考通道对象为空，直接返回
        double leftPos = Tools.getChannelPositionUI(chIndex); // 获取左侧位置（像素位置，如541）
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75); // 计算缩放高度（新高度的75%）
        double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos; // 计算偏移值（中心位置减去左侧位置）
        String unit = ChannelFactory.getProbeType(TChan.toFpgaChNo(chIndex)); // 获取单位（V或A）
        int startPos = ScopeBase.getHeight() - zoomHeight; // 计算起始位置（屏幕高度减去缩放高度）
        leftPos = Tools.isZoom() ? (leftPos + startPos) : leftPos; // 如果是缩放模式，左侧位置加上起始位置
        startPos = Tools.isZoom() ? startPos : 0; // 如果是缩放模式，使用起始位置，否则为0
        String number = TBookUtil.getFourFromD_Trim0(pos * refChannel.getVerticalPerPix()); // 计算数值（偏移值 * 每像素垂直值，保留4位小数）
        switch (chIndex) { // 根据通道索引进行分支处理
            case TChan.R1: // 参考通道1
                if (ref1Master == null) { // 如果ref1Master引用为空
                    ref1Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.ref1Master); // 从主视图组获取ref1Master引用
                }
                ref1Master.setLeftPosition(number + unit); // 设置ref1Master的左侧位置显示
                break; // 结束分支
            case TChan.R2: // 参考通道2
                if (ref2Master == null) { // 如果ref2Master引用为空
                    ref2Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.ref2Master); // 从主视图组获取ref2Master引用
                }
                ref2Master.setLeftPosition(number + unit); // 设置ref2Master的左侧位置显示
                break; // 结束分支
            case TChan.R3: // 参考通道3
                if (ref3Master == null) { // 如果ref3Master引用为空
                    ref3Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.ref3Master); // 从主视图组获取ref3Master引用
                }
                ref3Master.setLeftPosition(number + unit); // 设置ref3Master的左侧位置显示
                break; // 结束分支
            case TChan.R4: // 参考通道4
                if (ref4Master == null) { // 如果ref4Master引用为空
                    ref4Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.ref4Master); // 从主视图组获取ref4Master引用
                }
                ref4Master.setLeftPosition(number + unit); // 设置ref4Master的左侧位置显示
                break; // 结束分支
            case TChan.R5: // 参考通道5
                if (ref5Master == null) { // 如果ref5Master引用为空
                    ref5Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.ref5Master); // 从主视图组获取ref5Master引用
                }
                ref5Master.setLeftPosition(number + unit); // 设置ref5Master的左侧位置显示
                break; // 结束分支
            case TChan.R6: // 参考通道6
                if (ref6Master == null) { // 如果ref6Master引用为空
                    ref6Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.ref6Master); // 从主视图组获取ref6Master引用
                }
                ref6Master.setLeftPosition(number + unit); // 设置ref6Master的左侧位置显示
                break; // 结束分支
            case TChan.R7: // 参考通道7
                if (ref7Master == null) { // 如果ref7Master引用为空
                    ref7Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.ref7Master); // 从主视图组获取ref7Master引用
                }
                ref7Master.setLeftPosition(number + unit); // 设置ref7Master的左侧位置显示
                break; // 结束分支
            case TChan.R8: // 参考通道8
                if (ref8Master == null) { // 如果ref8Master引用为空
                    ref8Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.ref8Master); // 从主视图组获取ref8Master引用
                }
                ref8Master.setLeftPosition(number + unit); // 设置ref8Master的左侧位置显示
                break; // 结束分支
        }
    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ setOffset() - 设置普通通道偏移                                                  ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 设置普通通道的偏移值，更新左侧位置视图和右侧布局显示。
     * 计算普通通道的实际位置和偏移值，并更新对应的UI组件。
     * 
     * 处理流程：
     * 1. 获取通道对象
     * 2. 计算左侧位置（像素位置）
     * 3. 计算缩放高度和偏移值
     * 4. 获取单位（V或A）
     * 5. 计算数值（位置 * 每像素垂直值）
     * 6. 更新左侧位置视图
     * 7. 根据通道数量（8通道、4通道、2通道）更新对应的右侧布局主项
     * 
     * @param chIndex 通道索引（Ch1-8）
     */
    public void setOffset(int chIndex) { // 设置普通通道偏移
        Channel channel = ChannelFactory.getDynamicChannel(chIndex - 1); // 获取动态通道对象（通道索引-1转换为FPGA索引）
        if (channel==null) return; // 如果通道对象为空，直接返回
        double leftPos = Tools.getChannelPositionUI(chIndex); // 获取左侧位置（像素位置，如541）
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75); // 计算缩放高度（新高度的75%）
        double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos; // 计算偏移值（中心位置减去左侧位置）
        String unit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + chIndex) == 0 ? "V" : "A"; // 获取单位（探头类型为0表示V，否则为A）
//        layoutBranch.setOffset(TBookUtil.getFourFromD_Trim0(pos * channel.getVerticalPerPix()) + unit); // 注释掉的代码：设置分支偏移（已禁用）
        int startPos = ScopeBase.getHeight() - zoomHeight; // 计算起始位置（屏幕高度减去缩放高度）
        leftPos = Tools.isZoom() ? (leftPos + startPos) : leftPos; // 如果是缩放模式，左侧位置加上起始位置
        startPos = Tools.isZoom() ? startPos : 0; // 如果是缩放模式，使用起始位置，否则为0
        String number = TBookUtil.getFourFromD_Trim0(pos * channel.getVerticalPerPix()); // 计算数值（偏移值 * 每像素垂直值，保留4位小数）
//        RxBus.getInstance().post(RxEnum.MSG_CHANNEL_SLIP_POSITION,number+unit+";"+chIndex); // 注释掉的代码：发送通道滑块位置消息（已禁用）
        leftPositionView.setData(chIndex - 1, leftPos // 设置左侧位置视图数据（通道索引-1，左侧位置）
                , number // 数值
                , unit, startPos); // 单位，起始位置
        if (GlobalVar.get().getChannelsCount()==GlobalVar.CHANNEL_COUNT_8){ // 如果通道数量为8
            if (ch1Master == null || ch2Master == null || ch3Master == null || ch4Master == null // 如果任意主项引用为空
                    || ch5Master == null || ch6Master == null || ch7Master == null || ch8Master == null) {
                ch1Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh1MasterEight); // 从主视图组获取ch1Master引用（8通道版本）
                ch2Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh2MasterEight); // 从主视图组获取ch2Master引用（8通道版本）
                ch3Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh3MasterEight); // 从主视图组获取ch3Master引用（8通道版本）
                ch4Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh4MasterEight); // 从主视图组获取ch4Master引用（8通道版本）
                ch5Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh5MasterEight); // 从主视图组获取ch5Master引用（8通道版本）
                ch6Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh6MasterEight); // 从主视图组获取ch6Master引用（8通道版本）
                ch7Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh7MasterEight); // 从主视图组获取ch7Master引用（8通道版本）
                ch8Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh8MasterEight); // 从主视图组获取ch8Master引用（8通道版本）
            }
            switch (chIndex){ // 根据通道索引进行分支处理
                case TChan.Ch1:ch1Master.setLeftPosition(number+unit); break; // CH1：设置ch1Master的左侧位置显示
                case TChan.Ch2:ch2Master.setLeftPosition(number+unit); break; // CH2：设置ch2Master的左侧位置显示
                case TChan.Ch3:ch3Master.setLeftPosition(number+unit); break; // CH3：设置ch3Master的左侧位置显示
                case TChan.Ch4:ch4Master.setLeftPosition(number+unit); break; // CH4：设置ch4Master的左侧位置显示
                case TChan.Ch5:ch5Master.setLeftPosition(number+unit); break; // CH5：设置ch5Master的左侧位置显示
                case TChan.Ch6:ch6Master.setLeftPosition(number+unit); break; // CH6：设置ch6Master的左侧位置显示
                case TChan.Ch7:ch7Master.setLeftPosition(number+unit); break; // CH7：设置ch7Master的左侧位置显示
                case TChan.Ch8:ch8Master.setLeftPosition(number+unit); break; // CH8：设置ch8Master的左侧位置显示
            }
        }else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) { // 如果通道数量为4
            if (ch1Master == null || ch2Master == null || ch3Master == null || ch4Master == null) { // 如果任意主项引用为空
                ch1Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh1Master); // 从主视图组获取ch1Master引用（4通道版本）
                ch2Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh2Master); // 从主视图组获取ch2Master引用（4通道版本）
                ch3Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh3Master); // 从主视图组获取ch3Master引用（4通道版本）
                ch4Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh4Master); // 从主视图组获取ch4Master引用（4通道版本）
            }
            if (chIndex == TChan.Ch1) { // 如果是CH1通道
                ch1Master.setLeftPosition(number + unit); // 设置ch1Master的左侧位置显示
            } else if (chIndex == TChan.Ch2) { // 如果是CH2通道
                ch2Master.setLeftPosition(number + unit); // 设置ch2Master的左侧位置显示
            } else if (chIndex == TChan.Ch3) { // 如果是CH3通道
                ch3Master.setLeftPosition(number + unit); // 设置ch3Master的左侧位置显示
            } else if (chIndex == TChan.Ch4) { // 如果是CH4通道
                ch4Master.setLeftPosition(number + unit); // 设置ch4Master的左侧位置显示
            }
        } else { // 其他通道数量（2通道）
            if (ch1Master == null || ch2Master == null) { // 如果任意主项引用为空
                ch1Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh1MasterDouble); // 从主视图组获取ch1Master引用（2通道版本）
                ch2Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.rightCh2MasterDouble); // 从主视图组获取ch2Master引用（2通道版本）
            }
            if (chIndex == TChan.Ch1) { // 如果是CH1通道
                ch1Master.setLeftPosition(number + unit); // 设置ch1Master的左侧位置显示
            } else if (chIndex == TChan.Ch2) { // 如果是CH2通道
                ch2Master.setLeftPosition(number + unit); // 设置ch2Master的左侧位置显示
            }
        }
    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ setMathOffset() - 设置数学通道偏移                                              ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 设置数学通道的偏移值，更新右侧布局显示。
     * 根据数学通道的类型（DW、FFT、AXB、AM），计算偏移值并更新对应的UI组件。
     * 
     * 处理流程：
     * 1. 检查是否为数学通道
     * 2. 获取数学通道对象
     * 3. 根据数学类型获取位置缓存值
     * 4. 计算偏移值
     * 5. 获取单位
     * 6. 计算数值（位置 * AD每像素垂直值）
     * 7. 根据通道索引更新对应的右侧布局主项
     * 
     * @param mathIndex 数学通道索引（Math1-8）
     */
    public void setMathOffset(int mathIndex) { // 设置数学通道偏移
        if (!ChannelFactory.isMathCh(TChan.toFpgaChNo(mathIndex))) return; // 如果不是数学通道，直接返回
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathIndex)); // 获取数学通道对象
        double pos; // 定义位置变量
        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathIndex)) { // 根据数学类型进行分支处理
            case CacheUtil.MATHTYPE_DW: // 双波形运算类型
                pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION + mathIndex); // 获取DW Y位置缓存值
                break; // 结束分支
            case CacheUtil.MATHTYPE_FFT: // FFT运算类型
                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathIndex) == 0) { // 如果FFT类型为RMS
                    pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + mathIndex); // 获取FFT RMS Y位置缓存值
                } else { // 如果FFT类型为DB
                    pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION + mathIndex); // 获取FFT DB Y位置缓存值
                }
                break; // 结束分支
            case CacheUtil.MATHTYPE_AXB: // AXB表达式运算类型
                pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION + mathIndex); // 获取AXB Y位置缓存值
                break; // 结束分支
            default: // 默认类型（AM高级数学运算）
                pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION + mathIndex); // 获取AM Y位置缓存值
                break; // 结束分支
        }
        pos = GlobalVar.get().getMainWave().y / 2 - pos; // 计算偏移值（主波形Y中心减去位置）
        String unit = ChannelFactory.getProbeType(TChan.toFpgaChNo(mathIndex)); // 获取单位
        String number = TBookUtil.getFourFromD_Trim0(pos * mathChannel.getADVerticalPerPix()); // 计算数值（偏移值 * AD每像素垂直值，保留4位小数）

        switch (mathIndex) { // 根据数学通道索引进行分支处理
            case TChan.Math1: // 数学通道1
                if (math1Master == null) { // 如果math1Master引用为空
                    math1Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.math1Master); // 从主视图组获取math1Master引用
                }
                math1Master.setLeftPosition(number + unit); // 设置math1Master的左侧位置显示
                break; // 结束分支
            case TChan.Math2: // 数学通道2
                if (math2Master == null) { // 如果math2Master引用为空
                    math2Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.math2Master); // 从主视图组获取math2Master引用
                }
                math2Master.setLeftPosition(number + unit); // 设置math2Master的左侧位置显示
                break; // 结束分支
            case TChan.Math3: // 数学通道3
                if (math3Master == null) { // 如果math3Master引用为空
                    math3Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.math3Master); // 从主视图组获取math3Master引用
                }
                math3Master.setLeftPosition(number + unit); // 设置math3Master的左侧位置显示
                break; // 结束分支
            case TChan.Math4: // 数学通道4
                if (math4Master == null) { // 如果math4Master引用为空
                    math4Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.math4Master); // 从主视图组获取math4Master引用
                }
                math4Master.setLeftPosition(number + unit); // 设置math4Master的左侧位置显示
                break; // 结束分支
            case TChan.Math5: // 数学通道5
                if (math5Master == null) { // 如果math5Master引用为空
                    math5Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.math5Master); // 从主视图组获取math5Master引用
                }
                math5Master.setLeftPosition(number + unit); // 设置math5Master的左侧位置显示
                break; // 结束分支
            case TChan.Math6: // 数学通道6
                if (math6Master == null) { // 如果math6Master引用为空
                    math6Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.math6Master); // 从主视图组获取math6Master引用
                }
                math6Master.setLeftPosition(number + unit); // 设置math6Master的左侧位置显示
                break; // 结束分支
            case TChan.Math7: // 数学通道7
                if (math7Master == null) { // 如果math7Master引用为空
                    math7Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.math7Master); // 从主视图组获取math7Master引用
                }
                math7Master.setLeftPosition(number + unit); // 设置math7Master的左侧位置显示
                break; // 结束分支
            case TChan.Math8: // 数学通道8
                if (math8Master == null) { // 如果math8Master引用为空
                    math8Master = (MainRightLayoutItemChannelMaster) mainViewGroup.findViewById(R.id.math8Master); // 从主视图组获取math8Master引用
                }
                math8Master.setLeftPosition(number + unit); // 设置math8Master的左侧位置显示
                break; // 结束分支
        }
    }

    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ setRefOffset() - 设置参考通道偏移（分支版本）                                  ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 设置参考通道的偏移值，更新右侧布局分支显示。
     * 计算参考通道的偏移值，并更新对应的分支UI组件。
     * 
     * @param layoutBranch 右侧布局通道分支项
     * @param refIndex 参考通道索引（FPGA索引）
     */
    private void setRefOffset(MainRightLayoutItemChannelBranch layoutBranch, int refIndex) { // 设置参考通道偏移（分支版本）
        Log.d(Tag.Debug, String.format("WaveManage.setRefOffset: %d",refIndex )); // 输出日志信息
        RefChannel channel = ChannelFactory.getRefChannel(refIndex); // 获取参考通道对象
        double pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_REF_Y_POSITION + (TChan.toUiChNo(refIndex) )); // 获取参考通道Y位置缓存值
        pos = GlobalVar.get().getMainWave().y / 2 - pos; // 计算偏移值（主波形Y中心减去位置）
        String unit = ChannelFactory.getProbeType(refIndex); // 获取单位
        layoutBranch.setMathRefOffset(TBookUtil.getFourFromD_Trim0(pos * channel.getVerticalPerPix()) + unit); // 设置分支的数学/参考偏移显示
    }

    // ┌─────────────────────────────────────────────────────────────────────────────────┐
    // │ 串行总线电平管理方法区域                                                          │
    // │ 用于处理串行总线电平的更新                                                        │
    // └─────────────────────────────────────────────────────────────────────────────────┘
    
    /**
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ changeSerialTrigVol_channel() - 改变串行总线电平                                ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     * 
     * 改变串行总线电平，根据通道索引和串行总线类型更新通道的串行总线电平。
     * 
     * 处理流程：
     * 1. 检查通道是否匹配各个串行总线（S1-S4）
     * 2. 如果匹配且串行总线已打开，设置值电平串行总线
     * 3. 获取通道对象
     * 4. 根据M429类型设置主/次电平
     * 5. 重置值电平串行总线
     * 
     * @param chIdx 通道索引（IWave.Ch1-4，即0-3）
     */
    private void changeSerialTrigVol_channel(int chIdx) { // 改变串行总线电平（这里chIdx=IWave.ch1~ch4，即0-3）
        if (isCurChannelEquals(CacheUtil.S1, chIdx)) { // 如果当前通道匹配S1串行总线
            if (ChannelFactory.isChOpen(ChannelFactory.S1)) { // 如果S1串行总线已打开
                CacheUtil.get().setValueLevelSerials(CacheUtil.S1); // 设置值电平串行总线为S1
            } else { // 如果S1串行总线未打开
                return; // 直接返回
            }
        } else if (isCurChannelEquals(CacheUtil.S2, chIdx)) { // 如果当前通道匹配S2串行总线
