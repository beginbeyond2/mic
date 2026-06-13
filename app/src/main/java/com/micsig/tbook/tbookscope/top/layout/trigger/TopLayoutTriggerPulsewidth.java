package com.micsig.tbook.tbookscope.top.layout.trigger; // 脉宽触发器配置界面包路径

import android.content.Context; // Android上下文类
import android.os.Bundle; // Android Bundle类，用于保存和恢复状态
import android.view.LayoutInflater; // Android布局填充器
import android.view.View; // Android视图基类
import android.view.ViewGroup; // Android视图容器基类
import android.widget.LinearLayout; // Android线性布局
import android.widget.TextView; // Android文本视图

import androidx.annotation.Nullable; // Android可空注解
import androidx.fragment.app.Fragment; // Android Fragment基类

import com.micsig.tbook.scope.Event.EventBase; // 事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 事件工厂类
import com.micsig.tbook.scope.Event.EventUIObserver; // 事件UI观察者接口
import com.micsig.tbook.scope.Trigger.Trigger; // 触发器基类
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 触发器工厂类
import com.micsig.tbook.scope.Trigger.TriggerPulseWidth; // 脉宽触发器核心类
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量管理类
import com.micsig.tbook.tbookscope.LoadCache; // 缓存加载消息类
import com.micsig.tbook.tbookscope.MainActivity; // 主Activity类
import com.micsig.tbook.tbookscope.R; // 资源ID类
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel; // 触发电平持有者类
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel; // 触发电平消息类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令发送中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 命令到UI消息类
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel; // 右侧滑菜单电平消息类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 音效播放工具类
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 详情发送消息监听器接口
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogScale; // 顶部刻度对话框
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道数据Bean类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 自定义单选按钮组视图
import com.micsig.tbook.ui.top.view.scale.TopUtilScale; // 刻度工具类
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类

import io.reactivex.rxjava3.annotations.NonNull; // RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // RxJava消费者接口


/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        脉宽触发器配置Fragment                                  ║
 * ║                    TopLayoutTriggerPulsewidth.java                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块定位：触发系统 > 触发类型配置 > 脉宽触发器配置界面                         ║
 * ║  核心职责：提供脉宽触发器参数配置的UI交互界面，包括触发源、极性、条件、脉宽时间等 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                              架构设计                                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║    ┌─────────────────────────────────────────────────────────────────┐       ║
 * ║    │                    TopLayoutTriggerPulsewidth                  │       ║
 * ║    │                      (Fragment UI层)                           │       ║
 * ║    └───────────────────────┬─────────────────────────────────────────┘       ║
 * ║                            │                                                  ║
 * ║              ┌─────────────┼─────────────┬──────────────┐                    ║
 * ║              │             │             │              │                     ║
 * ║              ▼             ▼             ▼              ▼                     ║
 * ║    ┌──────────────┐ ┌──────────────┐ ┌────────────┐ ┌──────────────┐        ║
 * ║    │  RxBus事件   │ │  EventFactory │ │  Command   │ │  CacheUtil   │        ║
 * ║    │  订阅中心    │ │  事件观察者   │ │  命令发送  │ │  缓存管理    │        ║
 * ║    └──────────────┘ └──────────────┘ └────────────┘ └──────────────┘        ║
 * ║              │             │             │              │                     ║
 * ║              └─────────────┼─────────────┴──────────────┘                    ║
 * ║                            │                                                  ║
 * ║                            ▼                                                  ║
 * ║    ┌─────────────────────────────────────────────────────────────────┐       ║
 * ║    │              TriggerPulseWidth (触发器核心数据模型)              │       ║
 * ║    └─────────────────────────────────────────────────────────────────┘       ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                              数据流向                                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  用户交互 → UI控件 → onCheckChanged/onTextChanged → Command发送 → 硬件      ║
 * ║     ↓                    ↓                                                   ║
 * ║  CacheUtil ← ← ← ← ← ← ← ←┘                                                   ║
 * ║     ↓                                                                         ║
 * ║  msgTriggerDetail → sendMsg → onDetailSendMsgListener → 父Fragment           ║
 * ║                                                                              ║
 * ║  硬件状态变化 → EventFactory → eventUITriggerParam → UI更新                   ║
 * ║  外部命令     → CommandMsgToUI → consumerCommandToUI → UI更新                ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                            核心功能模块                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  1. 触发源配置 (Trigger Source)                                              ║
 * ║     - 支持多通道选择（CH1/CH2/CH3/CH4等）                                     ║
 * ║     - 与触发通道联动                                                         ║
 * ║                                                                              ║
 * ║  2. 极性配置 (Polarity)                                                       ║
 * ║     - 正极性（Positive）                                                     ║
 * ║     - 负极性（Negative）                                                     ║
 * ║     - 支持外部按键快速切换                                                   ║
 * ║                                                                              ║
 * ║  3. 条件配置 (Condition)                                                     ║
 * ║     - 条件0：时间高（Time High）                                             ║
 * ║     - 条件1：时间低（Time Low）                                              ║
 * ║     - 条件2：脉宽（Pulse Width）                                             ║
 * ║     - 条件3：时间高+时间低（Time High + Time Low）                           ║
 * ║     - 不同条件显示不同的时间输入控件                                         ║
 * ║                                                                              ║
 * ║  4. 时间参数配置                                                             ║
 * ║     - 脉宽时间（Pulse Width）                                                ║
 * ║     - 时间高（Time High）                                                    ║
 * ║     - 时间低（Time Low）                                                     ║
 * ║     - 时间高与时间低存在联动约束关系                                         ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                            依赖关系                                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  内部依赖：                                                                   ║
 * ║    - TriggerPulseWidth: 脉宽触发器核心数据模型                               ║
 * ║    - TopMsgTriggerPulsewidth: 脉宽触发器消息数据类                           ║
 * ║    - TopDialogScale: 时间参数输入对话框                                      ║
 * ║    - TopViewRadioGroup: 自定义单选按钮组控件                                 ║
 * ║                                                                              ║
 * ║  外部依赖：                                                                   ║
 * ║    - RxBus: 事件总线，用于组件间通信                                          ║
 * ║    - EventFactory: 事件工厂，用于硬件状态监听                                ║
 * ║    - Command: 命令中间件，用于向硬件发送配置命令                             ║
 * ║    - CacheUtil: 缓存工具，用于保存/恢复配置状态                               ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                          线程安全说明                                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  1. UI操作：所有UI操作均在主线程执行，通过Fragment生命周期保证               ║
 * ║  2. 事件订阅：RxBus和EventFactory的回调在主线程执行，可安全操作UI             ║
 * ║  3. 数据同步：通过CacheUtil和triggerPulseWidth进行状态同步                  ║
 * ║  4. 非线程安全：成员变量msgTriggerDetail、triggerPulseWidth等未做同步保护    ║
 * ║     但由于仅在主线程访问，在Fragment生命周期内是安全的                        ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                          使用示例                                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  // 在父Fragment或Activity中使用                                              ║
 * ║  TopLayoutTriggerPulsewidth fragment = new TopLayoutTriggerPulsewidth();    ║
 * ║  fragment.setOnDetailSendMsgListener(new OnDetailSendMsgListener() {        ║
 * ║      @Override                                                               ║
 * ║      public void onClick(Object sender, boolean isFromEventBus) {           ║
 * ║          // 处理配置变化通知                                                  ║
 * ║      }                                                                       ║
 * ║  });                                                                         ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                          修改历史                                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  2017-04-10  Administrator  创建类                                            ║
 * ║  YYYY-MM-DD  XXX            添加功能说明                                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * @author Administrator
 * @version 1.0
 * @since 2017-04-10
 */
public class TopLayoutTriggerPulsewidth extends Fragment { // 继承Fragment基类，实现脉宽触发器配置界面
    
    // ==================== 成员变量定义 ====================
    
    /** Android上下文对象，用于访问资源和系统服务 */
    private Context context; // Fragment关联的Activity上下文
    
    /** 触发源单选按钮组，用于选择触发信号来源（CH1/CH2/CH3/CH4等） */
    private TopViewRadioGroup rgSource; // 触发源选择控件
    
    /** 极性单选按钮组，用于选择触发极性（正极性/负极性） */
    private TopViewRadioGroup rgPolar; // 极性选择控件
    
    /** 条件单选按钮组，用于选择触发条件（时间高/时间低/脉宽/时间高+时间低） */
    private TopViewRadioGroup rgCondition; // 条件选择控件
    
    /** 时间刻度对话框，用于输入脉宽时间参数 */
    private TopDialogScale scaleDialog; // 时间参数输入对话框
    
    /** 脉宽时间显示文本框，显示当前设置的脉宽时间值 */
    private TextView tvPulsewidth; // 脉宽时间显示控件
    
    /** 时间高显示文本框，显示当前设置的时间高值 */
    private TextView tvTimeHigh; // 时间高显示控件
    
    /** 时间低显示文本框，显示当前设置的时间低值 */
    private TextView tvTimeLow; // 时间低显示控件
    
    /** 时间高布局容器，包含时间高标签和值显示 */
    private LinearLayout layoutTimeHigh; // 时间高布局容器
    
    /** 时间低布局容器，包含时间低标签和值显示 */
    private LinearLayout layoutTimeLow; // 时间低布局容器
    
    /** 脉宽触发器配置消息数据对象，用于向父Fragment传递配置信息 */
    private TopMsgTriggerPulsewidth msgTriggerDetail; // 配置消息数据对象
    
    /** 详情发送消息监听器，用于向父Fragment通知配置变化 */
    private OnDetailSendMsgListener onDetailSendMsgListener; // 配置变化回调监听器
    
    /** 脉宽触发器核心数据模型，用于与底层硬件交互 */
    private TriggerPulseWidth triggerPulseWidth; // 触发器核心数据模型
    
    /** 根视图容器，用于调试时打印控件位置信息 */
    private ViewGroup rootView; // Fragment根视图容器
    
    // ==================== 生命周期方法 ====================
    
    /**
     * 创建Fragment视图
     * <p>
     * 该方法是Fragment生命周期的一部分，在Fragment首次显示时调用。
     * 负责加载布局资源文件，创建视图层次结构。
     * </p>
     *
     * @param inflater 布局填充器，用于将XML布局转换为View对象
     * @param container 父视图容器，用于包含Fragment的视图
     * @param savedInstanceState 保存的实例状态，用于恢复Fragment状态
     * @return 创建的Fragment根视图对象
     */
    @Nullable // 参数和返回值可为空
    @Override // 覆盖父类方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图方法
        return inflater.inflate(R.layout.layout_triggerpulsewidth, container, false); // 加载脉宽触发器配置布局文件
    } // 方法结束

    /**
     * 视图创建完成回调
     * <p>
     * 该方法在onCreateView返回后立即调用，此时视图已创建完成。
     * 负责初始化视图控件、数据和事件监听器。
     * </p>
     *
     * @param view 已创建的Fragment根视图
     * @param savedInstanceState 保存的实例状态，用于恢复Fragment状态
     */
    @Override // 覆盖父类方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成方法
        this.context = getActivity(); // 获取关联的Activity上下文
        this.rootView = (ViewGroup) view; // 保存根视图引用，用于调试
        initView(view); // 初始化视图控件
        initData(); // 初始化数据对象
        initControl(); // 初始化事件监听器
    } // 方法结束
    
    // ==================== 初始化方法 ====================
    
    /**
     * 初始化视图控件
     * <p>
     * 该方法负责查找并初始化布局中的所有视图控件，
     * 设置控件的初始数据和监听器。
     * </p>
     *
     * <h3>初始化的控件包括：</h3>
     * <ul>
     *   <li>触发源单选按钮组（rgSource）</li>
     *   <li>极性单选按钮组（rgPolar）</li>
     *   <li>条件单选按钮组（rgCondition）</li>
     *   <li>脉宽时间文本框（tvPulsewidth）</li>
     *   <li>时间高文本框（tvTimeHigh）</li>
     *   <li>时间低文本框（tvTimeLow）</li>
     *   <li>时间刻度对话框（scaleDialog）</li>
     * </ul>
     *
     * @param view Fragment根视图，用于查找子视图
     */
    private void initView(View view) { // 初始化视图方法
        // 初始化触发源单选按钮组
        rgSource = (TopViewRadioGroup) view.findViewById(R.id.triggerSource); // 查找触发源控件
        rgSource.setData(getResources().getString(R.string.triggerSource), GlobalVar.get().getChannelsName(), onCheckChangedListener); // 设置触发源标题和通道名称列表
        
        // 初始化极性单选按钮组
        rgPolar = (TopViewRadioGroup) view.findViewById(R.id.polar); // 查找极性控件
        rgPolar.setData(R.string.triggerPulsewidthPolar, R.array.triggerPulsewidthPolar, onCheckChangedListener); // 设置极性标题和选项数组
        
        // 初始化条件单选按钮组
        rgCondition = (TopViewRadioGroup) view.findViewById(R.id.condition); // 查找条件控件
        rgCondition.setData(R.string.triggerPulsewidthCondition, R.array.triggerPulsewidthCondition, onCheckChangedListener); // 设置条件标题和选项数组
        
        // 初始化脉宽时间文本框
        tvPulsewidth = (TextView) view.findViewById(R.id.pulsewidth); // 查找脉宽时间控件
        tvPulsewidth.setOnClickListener(onClickListener); // 设置点击监听器，点击时弹出时间输入对话框
        
        // 初始化时间高和时间低文本框
        tvTimeHigh = (TextView) view.findViewById(R.id.timeHighDetail); // 查找时间高控件
        tvTimeLow = (TextView) view.findViewById(R.id.timeLowDetail); // 查找时间低控件
        
        // 初始化时间高和时间低布局容器
        layoutTimeHigh = (LinearLayout) view.findViewById(R.id.layoutPulseWidthTimeHigh); // 查找时间高布局容器
        layoutTimeLow = (LinearLayout) view.findViewById(R.id.layoutPulseWidthTimeLow); // 查找时间低布局容器
        
        // 设置时间高和时间低的点击监听器
        tvTimeHigh.setOnClickListener(onClickListener); // 设置时间高点击监听器
        tvTimeLow.setOnClickListener(onClickListener); // 设置时间低点击监听器
        
        // 获取时间刻度对话框引用
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale); // 从MainActivity获取对话框引用
        
        // 获取脉宽触发器核心数据模型
        triggerPulseWidth = (TriggerPulseWidth) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_PULSE); // 从触发器工厂获取脉宽触发器实例
    } // 方法结束
    
    /**
     * 初始化数据对象
     * <p>
     * 该方法负责创建并初始化脉宽触发器配置消息数据对象，
     * 从当前UI控件状态读取初始值。
     * </p>
     */
    private void initData() { // 初始化数据方法
        // 创建脉宽触发器配置消息对象
        msgTriggerDetail = new TopMsgTriggerPulsewidth(); // 实例化配置消息对象
        
        // 从UI控件读取初始值并设置到消息对象
        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 设置触发源
        msgTriggerDetail.setPolar(rgPolar.getSelected()); // 设置极性
        msgTriggerDetail.setCondition(rgCondition.getSelected()); // 设置条件
        msgTriggerDetail.setPulsewidth(tvPulsewidth.getText().toString()); // 设置脉宽时间
        msgTriggerDetail.setTimeHighDetail(tvTimeHigh.getText().toString()); // 设置时间高
        msgTriggerDetail.setTimeLowDetail(tvTimeLow.getText().toString()); // 设置时间低
    } // 方法结束
    
    /**
     * 初始化事件监听器
     * <p>
     * 该方法负责订阅RxBus事件和注册EventFactory观察者，
     * 监听各种配置变化和硬件状态更新事件。
     * </p>
     *
     * <h3>订阅的事件包括：</h3>
     * <ul>
     *   <li>TOPTRIGGER_CHANNEL: 触发通道变化事件</li>
     *   <li>MAIN_LOAD_CACHE: 加载缓存事件</li>
     *   <li>RIGHTLAYOUT_LEVEL: 右侧滑菜单电平变化事件</li>
     *   <li>MAIN_TRIGGERLEVEL_TRIGGERCHANNEL: 主界面触发电平触发通道变化事件</li>
     *   <li>COMMAND_TO_UI: 命令到UI事件</li>
     *   <li>EXTERNALKEYS_EDGE: 外部按键边沿事件</li>
     *   <li>EVENT_TRIGGER_PARAM: 触发参数变化事件</li>
     * </ul>
     */
    private void initControl() { // 初始化事件监听器方法
        // 订阅RxBus事件
        RxBus.getInstance().getObservable(RxEnum.TOPTRIGGER_CHANNEL).subscribe(consumerTriggerChannel); // 订阅触发通道变化事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅加载缓存事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel); // 订阅右侧滑菜单电平变化事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL).subscribe(consumerTriggerLevel); // 订阅触发电平触发通道变化事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI事件
        
        // 订阅外部按键事件
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_EDGE).subscribe(consumerExternalkeysEdge); // 订阅外部按键边沿事件
        
        // 注册EventFactory观察者
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_PARAM, eventUITriggerParam); // 注册触发参数变化事件观察者
    } // 方法结束
    
    // ==================== 缓存加载方法 ====================
    
    /**
     * 从缓存加载配置
     * <p>
     * 该方法负责从CacheUtil读取之前保存的脉宽触发器配置，
     * 并将配置应用到UI控件和底层硬件。
     * </p>
     *
     * <h3>加载的配置项包括：</h3>
     * <ul>
     *   <li>触发源（Trigger Source）</li>
     *   <li>极性（Polarity）</li>
     *   <li>条件（Condition）</li>
     *   <li>脉宽时间（Pulse Width）</li>
     *   <li>时间高（Time High）</li>
     *   <li>时间低（Time Low）</li>
     * </ul>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>从CacheUtil读取各项配置值</li>
     *   <li>将配置值设置到UI控件</li>
     *   <li>根据条件值显示/隐藏相应的时间输入控件</li>
     *   <li>更新消息数据对象</li>
     *   <li>向硬件发送配置命令</li>
     *   <li>更新触发器核心数据模型</li>
     * </ol>
     */
    private void setCache() { // 从缓存加载配置方法
        // 从缓存读取各项配置值
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE); // 读取触发源索引
        int polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_POLAR); // 读取极性索引
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_CONDITION); // 读取条件索引
        String pulsewidth = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_PULSEWIDTH); // 读取脉宽时间字符串
        String timeHigh = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_HIGH); // 读取时间高字符串
        String timeLow = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_LOW); // 读取时间低字符串
        
        // 将配置值设置到UI控件
        rgSource.setSelectedIndex(source); // 设置触发源选中项
        rgPolar.setSelectedIndex(polar); // 设置极性选中项
        rgCondition.setSelectedIndex(condition); // 设置条件选中项
        tvPulsewidth.setText(pulsewidth); // 设置脉宽时间文本
        tvTimeHigh.setText(timeHigh); // 设置时间高文本
        tvTimeLow.setText(timeLow); // 设置时间低文本
        
        // 根据条件值显示/隐藏相应的时间输入控件
        switch (condition) { // 条件分支判断
            case 0: // 条件0：时间高
                tvPulsewidth.setVisibility(View.INVISIBLE); // 隐藏脉宽时间控件
                layoutTimeHigh.setVisibility(View.VISIBLE); // 显示时间高控件
                layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低控件
                break; // 跳出分支
            case 1: // 条件1：时间低
                tvPulsewidth.setVisibility(View.INVISIBLE); // 隐藏脉宽时间控件
                layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高控件
                layoutTimeLow.setVisibility(View.VISIBLE); // 显示时间低控件
                break; // 跳出分支
            case 2: // 条件2：脉宽
                tvPulsewidth.setVisibility(View.VISIBLE); // 显示脉宽时间控件
                layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高控件
                layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低控件
                break; // 跳出分支
            case 3: // 条件3：时间高+时间低
                tvPulsewidth.setVisibility(View.INVISIBLE); // 隐藏脉宽时间控件
                layoutTimeHigh.setVisibility(View.VISIBLE); // 显示时间高控件
                layoutTimeLow.setVisibility(View.VISIBLE); // 显示时间低控件
                break; // 跳出分支
        } // switch结束
        
        // 更新消息数据对象
        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 设置触发源
        msgTriggerDetail.setPolar(this.rgPolar.getSelected()); // 设置极性
        msgTriggerDetail.setCondition(this.rgCondition.getSelected()); // 设置条件
        msgTriggerDetail.setPulsewidth(pulsewidth); // 设置脉宽时间
        msgTriggerDetail.setTimeHighDetail(timeHigh); // 设置时间高
        msgTriggerDetail.setTimeLowDetail(timeLow); // 设置时间低
        
        // 通知父Fragment配置已更新
        sendMsg(false); // 发送配置更新消息，非EventBus来源
        
        // 向硬件发送配置命令
        Command.get().getTrigger_pulse().Source(source, false); // 发送触发源配置命令
        Command.get().getTrigger_pulse().Polarity(polar, false); // 发送极性配置命令
        Command.get().getTrigger_pulse().Condition(condition, false); // 发送条件配置命令
        Command.get().getTrigger_pulse().Width(TBookUtil.getSFromTime(pulsewidth), false); // 发送脉宽时间配置命令
        Command.get().getTrigger_pulse().HTime(TBookUtil.getSFromTime(timeHigh), false); // 发送时间高配置命令
        Command.get().getTrigger_pulse().LTime(TBookUtil.getSFromTime(timeLow), false); // 发送时间低配置命令
        
        // 更新触发器核心数据模型
        triggerPulseWidth.setTriggerSource(source); // 设置触发源
        triggerPulseWidth.setPolarity(polar); // 设置极性
        triggerPulseWidth.setCondition(TopMatchTrigger.triggerConditionToScope(condition)); // 设置条件（转换为示波器内部格式）
        triggerPulseWidth.setPwTime(TBookUtil.getPsFromTime(pulsewidth) / 1000); // 设置脉宽时间（转换为纳秒）
        triggerPulseWidth.setTimeHigh(TBookUtil.getPsFromTime(timeHigh) / 1000); // 设置时间高（转换为纳秒）
        triggerPulseWidth.setTimeLow(TBookUtil.getPsFromTime(timeLow) / 1000); // 设置时间低（转换为纳秒）
    } // 方法结束
    
    // ==================== Getter/Setter方法 ====================
    
    /**
     * 获取脉宽触发器配置消息对象
     * <p>
     * 该方法用于获取当前脉宽触发器的配置信息，
     * 供父Fragment或其他组件读取配置状态。
     * </p>
     *
     * @return 脉宽触发器配置消息对象，包含当前所有配置参数
     */
    public TopMsgTriggerPulsewidth getMsgTriggerDetail() { // 获取配置消息方法
        return msgTriggerDetail; // 返回配置消息对象
    } // 方法结束
    
    /**
     * 设置详情发送消息监听器
     * <p>
     * 该方法用于设置配置变化回调监听器，
     * 当配置发生变化时，通过该监听器通知父Fragment。
     * </p>
     *
     * @param onDetailSendMsgListener 详情发送消息监听器接口实例
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置监听器方法
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 保存监听器引用
    } // 方法结束
    
    // ==================== 消息发送方法 ====================
    
    /**
     * 发送配置更新消息
     * <p>
     * 该方法用于向父Fragment通知配置发生变化，
     * 通过onDetailSendMsgListener回调接口实现。
     * </p>
     *
     * @param isFromEventBus 是否来自EventBus事件，
     *                       true表示由硬件状态变化触发，
     *                       false表示由用户操作触发
     */
    private void sendMsg(boolean isFromEventBus) { // 发送消息方法
        if (onDetailSendMsgListener != null) { // 检查监听器是否已设置
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用监听器回调方法
        } // if结束
    } // 方法结束
    
    // ==================== RxBus消费者回调 ====================
    
    /**
     * 外部按键边沿事件消费者
     * <p>
     * 该消费者用于处理外部按键边沿事件，
     * 当外部按键按下时，切换极性选择（正极性↔负极性）。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>检查当前触发器类型是否为脉宽触发器</li>
     *   <li>获取当前极性索引并加1</li>
     *   <li>如果索引超出范围，则循环回到0</li>
     *   <li>设置新的极性选中项</li>
     *   <li>触发极性变化处理</li>
     * </ol>
     */
    private Consumer<Boolean> consumerExternalkeysEdge = new Consumer<Boolean>() { // 外部按键边沿消费者定义
        @Override // 覆盖接口方法
        public void accept(Boolean aBoolean) throws Exception { // 接受事件方法
            if(TriggerFactory.getTriggerType() == triggerPulseWidth.getTriggerType()){ // 检查当前触发器类型是否为脉宽触发器
                int idx = rgPolar.getSelected().getIndex() + 1; // 获取当前极性索引并加1
                if(idx >= rgPolar.getArray().length){ // 检查索引是否超出范围
                    idx = 0; // 循环回到第一个选项
                } // if结束
                rgPolar.setSelectedIndex(idx); // 设置新的极性选中项
                onCheckChanged(rgPolar, rgPolar.getSelected(), false); // 触发极性变化处理，非EventBus来源
            } // if结束
        } // 方法结束
    }; // 消费者定义结束
    
    /**
     * 触发通道变化事件消费者
     * <p>
     * 该消费者用于处理触发通道变化事件，
     * 当其他触发类型界面切换触发通道时，同步更新本界面的触发源选择。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>检查事件来源是否为脉宽触发器界面</li>
     *   <li>如果不是，则检查当前触发源是否与事件中的通道一致</li>
     *   <li>如果不一致，则更新触发源选择</li>
     *   <li>保存配置到缓存</li>
     *   <li>更新消息数据对象</li>
     *   <li>向硬件发送配置命令</li>
     * </ol>
     */
    private Consumer<TopMsgTriggerChannel> consumerTriggerChannel = new Consumer<TopMsgTriggerChannel>() { // 触发通道变化消费者定义
        @Override // 覆盖接口方法
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception { // 接受事件方法
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_PULSEWIDTH) { // 检查事件来源是否为脉宽触发器界面
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) { // 检查当前触发源是否与事件通道一致
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber()); // 更新触发源选择
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber())); // 保存到缓存
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 更新消息数据对象
                    Command.get().getTrigger_pulse().Source(rgSource.getSelected().getIndex(),false); // 向硬件发送配置命令
                } // if结束
            } // if结束
        } // 方法结束
    }; // 消费者定义结束
    
    /**
     * 加载缓存事件消费者
     * <p>
     * 该消费者用于处理加载缓存事件，
     * 当系统需要恢复配置状态时，从缓存加载配置。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>调用setCache方法从缓存加载配置</li>
     *   <li>标记本Fragment缓存已加载</li>
     * </ol>
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 加载缓存消费者定义
        @Override // 覆盖接口方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接受事件方法
            setCache(); // 从缓存加载配置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerPulsewidth, true); // 标记缓存已加载
        } // 方法结束
    }; // 消费者定义结束
    
    /**
     * 右侧滑菜单电平变化事件消费者
     * <p>
     * 该消费者用于处理右侧滑菜单电平变化事件，
     * 当用户通过右侧滑菜单调整触发电平时，同步更新本界面的触发源和极性选择。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>检查当前触发器类型是否为脉宽触发器</li>
     *   <li>如果是，则检查触发源是否需要更新</li>
     *   <li>检查极性是否需要更新</li>
     *   <li>触发相应的变化处理</li>
     * </ol>
     */
    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() { // 右侧滑菜单电平变化消费者定义
        @Override // 覆盖接口方法
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception { // 接受事件方法
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发器类型索引
            if (triggerIndex == TopLayoutTrigger.DETAIL_PULSEWIDTH) { // 检查是否为脉宽触发器
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) { // 检查触发源是否需要更新
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect()); // 更新触发源选择
                    onCheckChanged(rgSource, rgSource.getSelected(), false); // 触发触发源变化处理
                } // if结束
                if ((msgLevel.getTopSelect() == 0) != (rgPolar.getSelected().getIndex() == 0)) { // 检查极性是否需要更新
                    rgPolar.setSelectedIndex(msgLevel.getTopSelect() == 0 ? 0 : 1); // 更新极性选择
                    onCheckChanged(rgPolar, rgPolar.getSelected(), false); // 触发极性变化处理
                } // if结束
            } // if结束
        } // 方法结束
    }; // 消费者定义结束
    
    /**
     * 主界面触发电平触发通道变化事件消费者
     * <p>
     * 该消费者用于处理主界面触发电平触发通道变化事件，
     * 当用户通过主界面调整触发电平通道时，同步更新本界面的触发源选择。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>检查是否仅修改数值（如果是则不处理）</li>
     *   <li>检查当前触发电平类型是否为脉宽触发器</li>
     *   <li>检查触发源是否需要更新</li>
     *   <li>触发相应的变化处理</li>
     * </ol>
     */
    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() { // 主界面触发电平触发通道变化消费者定义
        @Override // 覆盖接口方法
        public void accept(@NonNull MainMsgTriggerLevel msgTriggerLevel) throws Exception { // 接受事件方法
            if (msgTriggerLevel.isOnlyModifyNumber()) return; // 如果仅修改数值则不处理
            if (MainHolderTriggerLevel.LEVEL_TRIGGER_PULSEWIDTH.equals(msgTriggerLevel.getCurLevel())) { // 检查是否为脉宽触发器
                if (rgSource.getSelected().getIndex() != msgTriggerLevel.getCurCh() - 1) { // 检查触发源是否需要更新
                    rgSource.setSelectedIndex(msgTriggerLevel.getCurCh() - 1); // 更新触发源选择
                    onCheckChanged(rgSource, rgSource.getSelected(), msgTriggerLevel.isFromEventBus()); // 触发触发源变化处理
                } // if结束
            } // if结束
        } // 方法结束
    }; // 消费者定义结束
    
    /**
     * 命令到UI事件消费者
     * <p>
     * 该消费者用于处理来自硬件或其他模块的命令事件，
     * 根据命令标志更新相应的UI控件状态。
     * </p>
     *
     * <h3>支持的命令标志：</h3>
     * <ul>
     *   <li>FLAG_TRIGGERPULSE_SOURCE: 触发源变化命令</li>
     *   <li>FLAG_TRIGGERPULSE_POLAR: 极性变化命令</li>
     *   <li>FLAG_TRIGGERPULSE_WIDTH: 脉宽时间变化命令</li>
     *   <li>FLAG_TRIGGERPULSE_HTIME: 时间高变化命令</li>
     *   <li>FLAG_TRIGGERPULSE_LTIME: 时间低变化命令</li>
     *   <li>FLAG_TRIGGERPULSE_CONDITION: 条件变化命令</li>
     * </ul>
     *
     * <h3>时间参数处理：</h3>
     * <p>
     * 对于时间参数命令，会进行以下处理：
     * <ol>
     *   <li>将秒转换为纳秒</li>
     *   <li>检查时间是否在有效范围内</li>
     *   <li>将时间转换为显示格式</li>
     *   <li>更新UI控件</li>
     * </ol>
     * </p>
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令到UI消费者定义
        @Override // 覆盖接口方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接受事件方法
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分支处理
                case CommandMsgToUI.FLAG_TRIGGERPULSE_SOURCE: // 触发源变化命令
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 检查是否需要更新
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新触发源选择
                        onCheckChanged(rgSource, rgSource.getSelected(), false); // 触发触发源变化处理
                    } // if结束
                    break; // 跳出分支
                case CommandMsgToUI.FLAG_TRIGGERPULSE_POLAR: // 极性变化命令
                    if (rgPolar.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 检查是否需要更新
                        rgPolar.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新极性选择
                        onCheckChanged(rgPolar, rgPolar.getSelected(), false); // 触发极性变化处理
                    } // if结束
                    break; // 跳出分支
                case CommandMsgToUI.FLAG_TRIGGERPULSE_WIDTH: // 脉宽时间变化命令
                    double checkBefore = Double.parseDouble(commandMsgToUI.getParam()); // 解析时间参数（秒）
                    long checkTime = TopUtilScale.checkTime((long) (checkBefore * TopUtilScale.TIME_S2NS) // 转换为纳秒并检查范围
                            , TopUtilScale.TIME_PULSEWIDTH_MIN, TopUtilScale.TIME_PULSEWIDTH_MAX); // 检查时间范围
                    String time = TBookUtil.getTimeFromS(checkTime * 1.0 / TopUtilScale.TIME_S2NS); // 转换为显示格式
//                    if (!tvPulsewidth.getText().toString().equals(time)) { // 注释掉的检查逻辑
                        onTextChanged(tvPulsewidth, time, false); // 触发脉宽时间变化处理
//                    } // if结束
                    break; // 跳出分支
                case CommandMsgToUI.FLAG_TRIGGERPULSE_HTIME: // 时间高变化命令
                    double checkBeforeHigh = Double.parseDouble(commandMsgToUI.getParam()); // 解析时间参数（秒）
                    long checkTimeHigh = TopUtilScale.checkTime((long) (checkBeforeHigh * TopUtilScale.TIME_S2NS) // 转换为纳秒并检查范围
                            , TopUtilScale.TIME_PULSEWIDTH_MIN, TopUtilScale.TIME_PULSEWIDTH_MAX); // 检查时间范围
                    String timeHigh = TBookUtil.getTimeFromS(checkTimeHigh * 1.0 / TopUtilScale.TIME_S2NS); // 转换为显示格式
                    onTextChanged(tvTimeHigh, timeHigh, false); // 触发时间高变化处理
                    break; // 跳出分支
                case CommandMsgToUI.FLAG_TRIGGERPULSE_LTIME: // 时间低变化命令
                    double checkBeforeLow = Double.parseDouble(commandMsgToUI.getParam()); // 解析时间参数（秒）
                    long checkTimeLow = TopUtilScale.checkTime((long) (checkBeforeLow * TopUtilScale.TIME_S2NS) // 转换为纳秒并检查范围
                            , TopUtilScale.TIME_PULSEWIDTH_MIN, TopUtilScale.TIME_PULSEWIDTH_MAX); // 检查时间范围
                    String timeLow = TBookUtil.getTimeFromS(checkTimeLow * 1.0 / TopUtilScale.TIME_S2NS); // 转换为显示格式
                    onTextChanged(tvTimeLow, timeLow, false); // 触发时间低变化处理
                    break; // 跳出分支
                case CommandMsgToUI.FLAG_TRIGGERPULSE_CONDITION: // 条件变化命令
                    if (rgCondition.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 检查是否需要更新
                        rgCondition.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新条件选择
                        onCheckChanged(rgCondition, rgCondition.getSelected(), false); // 触发条件变化处理
                    } // if结束
                    break; // 跳出分支
            } // switch结束
        } // 方法结束
    }; // 消费者定义结束
    
    // ==================== EventFactory观察者 ====================
    
    /**
     * 触发参数变化事件观察者
     * <p>
     * 该观察者用于监听硬件触发参数变化事件，
     * 当硬件触发参数发生变化时，同步更新UI控件状态。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>检查事件ID是否为触发参数变化事件</li>
     *   <li>检查并更新触发源</li>
     *   <li>检查并更新极性</li>
     *   <li>检查并更新条件</li>
     *   <li>检查并更新脉宽时间</li>
     *   <li>检查并更新时间高</li>
     *   <li>检查并更新时间低</li>
     * </ol>
     *
     * <h3>时间参数处理：</h3>
     * <p>
     * 对于时间参数，会进行以下处理：
     * <ol>
     *   <li>检查时间是否在有效范围内</li>
     *   <li>将时间转换为显示格式</li>
     *   <li>检查是否与当前值不同</li>
     *   <li>更新UI控件</li>
     * </ol>
     * </p>
     */
    private EventUIObserver eventUITriggerParam = new EventUIObserver() { // 触发参数变化观察者定义
        @Override // 覆盖接口方法
        public void update(Object data) { // 更新方法
            EventBase eventBase = (EventBase) data; // 转换事件数据类型
            if (eventBase.getId() == EventFactory.EVENT_TRIGGER_PARAM) { // 检查事件ID
                // 检查并更新触发源
                int source = triggerPulseWidth.getTriggerSource(); // 获取触发源
                if (rgSource.getSelected().getIndex() != source) { // 检查是否需要更新
                    rgSource.setSelectedIndex(source); // 更新触发源选择
                    onCheckChanged(rgSource, rgSource.getSelected(), true); // 触发触发源变化处理，来自EventBus
                } // if结束
                
                // 检查并更新极性
                int polarity = triggerPulseWidth.getPolarity(); // 获取极性
                if (rgPolar.getSelected().getIndex() != polarity) { // 检查是否需要更新
                    rgPolar.setSelectedIndex(polarity); // 更新极性选择
                    onCheckChanged(rgPolar, rgPolar.getSelected(), true); // 触发极性变化处理，来自EventBus
                } // if结束
                
                // 检查并更新条件
                int condition = TopMatchTrigger.triggerConditionFromScope(triggerPulseWidth.getCondition()); // 获取条件并转换格式
                if (rgCondition.getSelected().getIndex() != condition) { // 检查是否需要更新
                    rgCondition.setSelectedIndex(condition); // 更新条件选择
                    onCheckChanged(rgCondition, rgCondition.getSelected(), true); // 触发条件变化处理，来自EventBus
                } // if结束
                
                // 检查并更新脉宽时间
                long checkTime = TopUtilScale.checkTime(triggerPulseWidth.getPwTime() // 获取脉宽时间并检查范围
                        , TopUtilScale.TIME_PULSEWIDTH_MIN, TopUtilScale.TIME_PULSEWIDTH_MAX); // 检查时间范围
                String time = TBookUtil.getTime3FromPs(checkTime * 1000 * 10); // 转换为显示格式
                if (!tvPulsewidth.getText().toString().equals(time)) { // 检查是否需要更新
                    tvPulsewidth.setText(time); // 更新脉宽时间文本
                    onTextChanged(tvPulsewidth, time, true); // 触发脉宽时间变化处理，来自EventBus
                } // if结束
                
                // 检查并更新时间高
                long checkTimeHigh = TopUtilScale.checkTime(triggerPulseWidth.getTimeHigh() // 获取时间高并检查范围
                        , TopUtilScale.TIME_PULSEWIDTH_MIN, TopUtilScale.TIME_PULSEWIDTH_MAX); // 检查时间范围
                String timeHigh = TBookUtil.getTime3FromPs(checkTimeHigh * 1000 * 10); // 转换为显示格式
                if (!tvTimeHigh.getText().toString().equals(timeHigh)) { // 检查是否需要更新
                    onTextChanged(tvTimeHigh, timeHigh, true); // 触发时间高变化处理，来自EventBus
                } // if结束
                
                // 检查并更新时间低
                long checkTimeLow = TopUtilScale.checkTime(triggerPulseWidth.getTimeLow() // 获取时间低并检查范围
                        , TopUtilScale.TIME_PULSEWIDTH_MIN, TopUtilScale.TIME_PULSEWIDTH_MAX); // 检查时间范围
                String timeLow = TBookUtil.getTime3FromPs(checkTimeLow * 1000 * 10); // 转换为显示格式
                if (!tvTimeLow.getText().toString().equals(timeLow)) { // 检查是否需要更新
                    onTextChanged(tvTimeLow, timeLow, true); // 触发时间低变化处理，来自EventBus
                } // if结束
            } // if结束
        } // 方法结束
    }; // 观察者定义结束
    
    // ==================== 对话框监听器 ====================
    
    /**
     * 时间高对话框关闭监听器
     * <p>
     * 该监听器用于处理时间高对话框关闭事件，
     * 当用户完成时间高输入后，更新UI和硬件配置。
     * </p>
     */
    private TopDialogScale.OnDismissListener onHighDismissListener = new TopDialogScale.OnDismissListener() { // 时间高对话框关闭监听器定义
        @Override // 覆盖接口方法
        public void onDismiss(String result) { // 对话框关闭方法
            onTextChanged(tvTimeHigh, result, false); // 触发时间高变化处理，非EventBus来源
        } // 方法结束
    }; // 监听器定义结束
    
    /**
     * 时间低对话框关闭监听器
     * <p>
     * 该监听器用于处理时间低对话框关闭事件，
     * 当用户完成时间低输入后，更新UI和硬件配置。
     * </p>
     */
    private TopDialogScale.OnDismissListener onLowDismissListener = new TopDialogScale.OnDismissListener() { // 时间低对话框关闭监听器定义
        @Override // 覆盖接口方法
        public void onDismiss(String result) { // 对话框关闭方法
            onTextChanged(tvTimeLow, result, false); // 触发时间低变化处理，非EventBus来源
        } // 方法结束
    }; // 监听器定义结束
    
    /**
     * 脉宽时间对话框关闭监听器
     * <p>
     * 该监听器用于处理脉宽时间对话框关闭事件，
     * 当用户完成脉宽时间输入后，更新UI和硬件配置。
     * </p>
     */
    private TopDialogScale.OnDismissListener onDismissListener = new TopDialogScale.OnDismissListener() { // 脉宽时间对话框关闭监听器定义
        @Override // 覆盖接口方法
        public void onDismiss(String result) { // 对话框关闭方法
            onTextChanged(tvPulsewidth, result, false); // 触发脉宽时间变化处理，非EventBus来源
        } // 方法结束
    }; // 监听器定义结束
    
    // ==================== 点击监听器 ====================
    
    /**
     * 视图点击监听器
     * <p>
     * 该监听器用于处理时间参数文本框的点击事件，
     * 点击时弹出时间刻度对话框供用户输入时间参数。
     * </p>
     *
     * <h3>支持的控件：</h3>
     * <ul>
     *   <li>timeHighDetail: 时间高文本框</li>
     *   <li>timeLowDetail: 时间低文本框</li>
     *   <li>pulsewidth: 脉宽时间文本框</li>
     * </ul>
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 点击监听器定义
        @Override // 覆盖接口方法
        public void onClick(View v) { // 点击事件方法
            PlaySound.getInstance().playButton(); // 播放按钮音效
            switch (v.getId()) { // 根据控件ID分支处理
                case R.id.timeHighDetail: // 时间高文本框
                    scaleDialog.setValue(context.getString(R.string.triggerpulsewidth_timehigh), tvTimeHigh.getText().toString(), // 设置对话框标题和当前值
                            TopUtilScale.TIME_PULSEWIDTH_MIN, TopUtilScale.TIME_PULSEWIDTH_MAX, onHighDismissListener); // 设置时间范围和关闭监听器
                    break; // 跳出分支
                case R.id.timeLowDetail: // 时间低文本框
                    scaleDialog.setValue(context.getString(R.string.triggerpulsewidth_timelow), tvTimeLow.getText().toString(), // 设置对话框标题和当前值
                            TopUtilScale.TIME_PULSEWIDTH_MIN, TopUtilScale.TIME_PULSEWIDTH_MAX, onLowDismissListener); // 设置时间范围和关闭监听器
                    break; // 跳出分支
                case R.id.pulsewidth: // 脉宽时间文本框
                    scaleDialog.setValue(context.getString(R.string.trigger_pulsewidth), tvPulsewidth.getText().toString() // 设置对话框标题和当前值
                            , TopUtilScale.TIME_PULSEWIDTH_MIN, TopUtilScale.TIME_PULSEWIDTH_MAX, onDismissListener); // 设置时间范围和关闭监听器
                    break; // 跳出分支
            } // switch结束
        } // 方法结束
    }; // 监听器定义结束
    
    
    // ==================== 单选按钮监听器 ====================
    
    /**
     * 单选按钮变化监听器
     * <p>
     * 该监听器用于处理单选按钮组的点击和变化事件，
     * 当用户点击单选按钮时，播放音效并触发变化处理。
     * </p>
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选按钮变化监听器定义
        @Override // 覆盖接口方法
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效方法
            PlaySound.getInstance().playButton(); // 播放按钮音效
        } // 方法结束

        @Override // 覆盖接口方法
        public void onPrompt(TopViewRadioGroup view) { // 提示方法
            // 空实现，暂无提示功能
        } // 方法结束

        @Override // 覆盖接口方法
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 点击事件方法
            onCheckChanged(view, item, false); // 触发变化处理，非EventBus来源
        } // 方法结束
    }; // 监听器定义结束
    
    // ==================== 业务逻辑方法 ====================
    
    /**
     * 处理单选按钮变化事件
     * <p>
     * 该方法用于处理触发源、极性、条件等单选按钮的变化事件，
     * 根据变化的控件类型执行相应的配置更新操作。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>判断变化的控件类型（触发源/极性/条件）</li>
     *   <li>向硬件发送配置命令</li>
     *   <li>保存配置到缓存</li>
     *   <li>更新触发器核心数据模型（如果不是来自EventBus）</li>
     *   <li>更新消息数据对象</li>
     *   <li>通知父Fragment配置变化</li>
     *   <li>如果是条件变化，更新时间控件的显示/隐藏状态</li>
     * </ol>
     *
     * <h3>条件与时间控件显示关系：</h3>
     * <ul>
     *   <li>条件0（时间高）：显示时间高控件，隐藏脉宽和时间低控件</li>
     *   <li>条件1（时间低）：显示时间低控件，隐藏脉宽和时间高控件</li>
     *   <li>条件2（脉宽）：显示脉宽控件，隐藏时间高和时间低控件</li>
     *   <li>条件3（时间高+时间低）：显示时间高和时间低控件，隐藏脉宽控件</li>
     * </ul>
     *
     * @param view 发生变化的单选按钮组控件
     * @param item 选中的选项数据
     * @param isFromEventBus 是否来自EventBus事件，
     *                        true表示由硬件状态变化触发，
     *                        false表示由用户操作触发
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 单选按钮变化处理方法
        if (view.getId() == R.id.triggerSource) { // 检查是否为触发源变化
            // 处理触发源变化
            Command.get().getTrigger_pulse().Source(item.getIndex(), false); // 向硬件发送触发源配置命令
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex())); // 保存触发源到缓存
            if (!isFromEventBus) { // 如果不是来自EventBus
                triggerPulseWidth.setTriggerSource(item.getIndex()); // 更新触发器核心数据模型
            } // if结束
            msgTriggerDetail.setTriggerSource(item); // 更新消息数据对象
            sendMsg(isFromEventBus); // 通知父Fragment配置变化
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL, // 发送触发通道变化事件
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_PULSEWIDTH, item.getIndex())); // 创建并发送事件消息
        } else if (view.getId() == R.id.polar) { // 检查是否为极性变化
            // 处理极性变化
            Command.get().getTrigger_pulse().Polarity(item.getIndex(), false); // 向硬件发送极性配置命令
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_POLAR, String.valueOf(item.getIndex())); // 保存极性到缓存
            if (!isFromEventBus) { // 如果不是来自EventBus
                triggerPulseWidth.setPolarity(item.getIndex()); // 更新触发器核心数据模型
            } // if结束
            msgTriggerDetail.setPolar(item); // 更新消息数据对象
            sendMsg(isFromEventBus); // 通知父Fragment配置变化
        } else if (view.getId() == R.id.condition) { // 检查是否为条件变化
            // 处理条件变化
            Command.get().getTrigger_pulse().Condition(item.getIndex(), false); // 向硬件发送条件配置命令
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_CONDITION, String.valueOf(item.getIndex())); // 保存条件到缓存
            if (!isFromEventBus) { // 如果不是来自EventBus
                triggerPulseWidth.setCondition(TopMatchTrigger.triggerConditionToScope(item.getIndex())); // 更新触发器核心数据模型（转换为示波器内部格式）
            } // if结束
            msgTriggerDetail.setCondition(item); // 更新消息数据对象
            sendMsg(isFromEventBus); // 通知父Fragment配置变化
            
            // 根据条件更新时间控件的显示/隐藏状态
            switch (item.getIndex()) { // 根据条件索引分支处理
                case 0: // 条件0：时间高
                    tvPulsewidth.setVisibility(View.INVISIBLE); // 隐藏脉宽时间控件
                    layoutTimeHigh.setVisibility(View.VISIBLE); // 显示时间高控件
                    layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低控件
                    break; // 跳出分支
                case 1: // 条件1：时间低
                    tvPulsewidth.setVisibility(View.INVISIBLE); // 隐藏脉宽时间控件
                    layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高控件
                    layoutTimeLow.setVisibility(View.VISIBLE); // 显示时间低控件
                    break; // 跳出分支
                case 2: // 条件2：脉宽
                    tvPulsewidth.setVisibility(View.VISIBLE); // 显示脉宽时间控件
                    layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高控件
                    layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低控件
                    break; // 跳出分支
                case 3: // 条件3：时间高+时间低
                    tvPulsewidth.setVisibility(View.INVISIBLE); // 隐藏脉宽时间控件
                    layoutTimeHigh.setVisibility(View.VISIBLE); // 显示时间高控件
                    layoutTimeLow.setVisibility(View.VISIBLE); // 显示时间低控件
                    break; // 跳出分支
            } // switch结束
        } // if-else结束
    } // 方法结束
    
    /**
     * 处理文本变化事件
     * <p>
     * 该方法用于处理脉宽时间、时间高、时间低等文本框的变化事件，
     * 根据变化的文本框类型执行相应的配置更新操作。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>判断变化的文本框类型（脉宽时间/时间高/时间低）</li>
     *   <li>向硬件发送配置命令</li>
     *   <li>保存配置到缓存</li>
     *   <li>更新触发器核心数据模型（如果不是来自EventBus）</li>
     *   <li>更新UI文本显示</li>
     *   <li>更新消息数据对象</li>
     *   <li>通知父Fragment配置变化</li>
     *   <li>如果是时间高或时间低变化，检查并处理联动约束</li>
     * </ol>
     *
     * <h3>时间高与时间低的联动约束：</h3>
     * <p>
     * 当设置时间高时，如果时间高小于时间低，则自动将时间低更新为时间高的值。
     * 当设置时间低时，如果时间低大于时间高，则自动将时间高更新为时间低的值。
     * 这样可以保证时间高始终大于等于时间低。
     * </p>
     *
     * @param tv 发生变化的文本框控件
     * @param result 新的文本值
     * @param isFromEventBus 是否来自EventBus事件，
     *                       true表示由硬件状态变化触发，
     *                       false表示由用户操作触发
     */
    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) { // 文本变化处理方法
        Tools.PrintControlsLocation("TopLayoutTriggerPulsewidth", rootView); // 打印控件位置信息（调试用）
        
        if (tv.getId() == tvPulsewidth.getId()) { // 检查是否为脉宽时间变化
            // 处理脉宽时间变化
            Command.get().getTrigger_pulse().Width(TBookUtil.getSFromTime(result), false); // 向硬件发送脉宽时间配置命令
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_PULSEWIDTH, result); // 保存脉宽时间到缓存
            if (!isFromEventBus) { // 如果不是来自EventBus
                triggerPulseWidth.setPwTime(TBookUtil.getPsFromTime(result) / 1000); // 更新触发器核心数据模型（转换为纳秒）
            } // if结束
            tvPulsewidth.setText(result); // 更新UI文本显示
            msgTriggerDetail.setPulsewidth(result); // 更新消息数据对象
            sendMsg(isFromEventBus); // 通知父Fragment配置变化
            
        } else if (tv.getId() == tvTimeHigh.getId()) { // 检查是否为时间高变化
            // 处理时间高变化
            long ht = TBookUtil.getPsFromTime(result) / 1000; // 将时间高转换为纳秒
            long lt = TBookUtil.getPsFromTime(tvTimeLow.getText().toString()) / 1000; // 将时间低转换为纳秒
            if (ht < lt) { // 检查时间高是否小于时间低
                // 时间高小于时间低，需要同步更新时间低
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_LOW, result); // 保存时间低到缓存
                if (!isFromEventBus) { // 如果不是来自EventBus
                    triggerPulseWidth.setTimeLow(ht); // 更新触发器核心数据模型的时间低
                } // if结束
                tvTimeLow.setText(result); // 更新时间低UI文本显示
                Command.get().getTrigger_pulse().LTime(TBookUtil.getSFromTime(result), false); // 向硬件发送时间低配置命令
                msgTriggerDetail.setTimeLowDetail(result); // 更新消息数据对象的时间低
            } // if结束
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_HIGH, result); // 保存时间高到缓存
            if (!isFromEventBus) { // 如果不是来自EventBus
                triggerPulseWidth.setTimeHigh(ht); // 更新触发器核心数据模型的时间高
            } // if结束
            tvTimeHigh.setText(result); // 更新时间高UI文本显示
            Command.get().getTrigger_pulse().HTime(TBookUtil.getSFromTime(result), false); // 向硬件发送时间高配置命令
            msgTriggerDetail.setTimeHighDetail(result); // 更新消息数据对象的时间高
            sendMsg(isFromEventBus); // 通知父Fragment配置变化
            
        } else if (tv.getId() == tvTimeLow.getId()) { // 检查是否为时间低变化
            // 处理时间低变化
            long lt = TBookUtil.getPsFromTime(result) / 1000; // 将时间低转换为纳秒
            long ht = TBookUtil.getPsFromTime(tvTimeHigh.getText().toString()) / 1000; // 将时间高转换为纳秒
            if (ht < lt) { // 检查时间高是否小于时间低
                // 时间高小于时间低，需要同步更新时间高
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_HIGH, result); // 保存时间高到缓存
                if (!isFromEventBus) { // 如果不是来自EventBus
                    triggerPulseWidth.setTimeHigh(ht); // 更新触发器核心数据模型的时间高
                } // if结束
                tvTimeHigh.setText(result); // 更新时间高UI文本显示
                Command.get().getTrigger_pulse().HTime(TBookUtil.getSFromTime(result), false); // 向硬件发送时间高配置命令
                msgTriggerDetail.setTimeHighDetail(result); // 更新消息数据对象的时间高
            } // if结束
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_LOW, result); // 保存时间低到缓存
            if (!isFromEventBus) { // 如果不是来自EventBus
                triggerPulseWidth.setTimeLow(TBookUtil.getPsFromTime(result) / 1000); // 更新触发器核心数据模型的时间低
            } // if结束
            tvTimeLow.setText(result); // 更新时间低UI文本显示
            Command.get().getTrigger_pulse().LTime(TBookUtil.getSFromTime(result), false); // 向硬件发送时间低配置命令
            msgTriggerDetail.setTimeLowDetail(result); // 更新消息数据对象的时间低
            sendMsg(isFromEventBus); // 通知父Fragment配置变化
        } // if-else结束
    } // 方法结束
} // 类结束