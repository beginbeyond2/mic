package com.micsig.tbook.tbookscope.top.layout.trigger; // 逻辑触发器配置Fragment所在包 //

import android.content.Context; // Android上下文类，用于访问系统资源和服务 //
import android.os.Bundle; // Android Bundle类，用于保存和恢复Fragment状态 //
import android.view.LayoutInflater; // 布局填充器，用于将XML布局转换为View对象 //
import android.view.View; // Android视图基类 //
import android.view.ViewGroup; // Android视图组基类 //
import android.widget.LinearLayout; // 线性布局控件 //
import android.widget.TextView; // 文本视图控件 //

import androidx.annotation.Nullable; // AndroidX可空注解 //
import androidx.fragment.app.Fragment; // AndroidX Fragment基类 //

import com.micsig.tbook.scope.Event.EventBase; // 事件基类 //
import com.micsig.tbook.scope.Event.EventFactory; // 事件工厂类 //
import com.micsig.tbook.scope.Event.EventUIObserver; // 事件UI观察者接口 //
import com.micsig.tbook.scope.Trigger.Trigger; // 触发器基类 //
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 触发器工厂类 //
import com.micsig.tbook.scope.Trigger.TriggerLogic; // 逻辑触发器类 //
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量管理类 //
import com.micsig.tbook.tbookscope.LoadCache; // 缓存加载事件类 //
import com.micsig.tbook.tbookscope.MainActivity; // 主Activity类 //
import com.micsig.tbook.tbookscope.R; // 资源文件R类 //
import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件类 //
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // UI命令消息类 //
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线 //
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义 //
import com.micsig.tbook.tbookscope.tools.PlaySound; // 声音播放工具类 //
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类 //
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 详情消息发送监听器接口 //
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogScale; // 刻度对话框类 //
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类 //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean类 //
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 自定义单选组控件 //
import com.micsig.tbook.ui.top.view.scale.TopUtilScale; // 刻度工具类 //
import com.micsig.tbook.ui.util.ScreenUtil; // 屏幕工具类 //
import com.micsig.tbook.ui.util.TBookUtil; // TBook通用工具类 //

import io.reactivex.rxjava3.annotations.NonNull; // RxJava非空注解 //
import io.reactivex.rxjava3.functions.Consumer; // RxJava消费者接口 //


/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        逻辑触发器配置Fragment                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopLayoutTriggerLogic                                             ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/4/10                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 提供逻辑触发器的参数配置界面                                               ║
 * ║  2. 支持1-8通道的逻辑状态配置（高/低/忽略）                                    ║
 * ║  3. 支持逻辑关系配置（AND/OR/NAND/NOR等）                                     ║
 * ║  4. 支持触发条件配置（时间高/时间低/时间/组合）                                ║
 * ║  5. 实现UI与硬件参数的双向同步                                                ║
 * ║  6. 管理配置参数的缓存与恢复                                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构图:                                                                      ║
 * ║                                                                              ║
 * ║    ┌─────────────────────────────────────────────────────────────────┐       ║
 * ║    │                    TopLayoutTriggerLogic                        │       ║
 * ║    │                         (Fragment)                             │       ║
 * ║    └─────────────────────────────────────────────────────────────────┘       ║
 * ║                                   │                                          ║
 * ║              ┌────────────────────┼────────────────────┐                     ║
 * ║              │                    │                    │                     ║
 * ║              ▼                    ▼                    ▼                     ║
 * ║    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                 ║
 * ║    │  UI控件层     │    │  数据层       │    │  通信层      │                 ║
 * ║    ├──────────────┤    ├──────────────┤    ├──────────────┤                 ║
 * ║    │ rgCh1~8      │    │triggerDetail │    │ Command      │                 ║
 * ║    │ rgTriggerLogic│   │ triggerLogic │    │ RxBus        │                 ║
 * ║    │ rgCondition  │    │ CacheUtil    │    │ EventBus     │                 ║
 * ║    │ tvTime       │    │ GlobalVar    │    │              │                 ║
 * ║    │ tvTimeHigh   │    │              │    │              │                 ║
 * ║    │ tvTimeLow    │    │              │    │              │                 ║
 * ║    └──────────────┘    └──────────────┘    └──────────────┘                 ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                    ║
 * ║                                                                              ║
 * ║  ┌──────────┐    用户操作    ┌──────────────────┐    参数变更    ┌────────┐ ║
 * ║  │  用户    │ ───────────▶ │  UI控件监听器     │ ───────────▶ │ Command│ ║
 * ║  └──────────┘              └──────────────────┘               └────────┘ ║
 * ║                                     │                              │        ║
 * ║                                     │ 更新UI                       │ 发送   ║
 * ║                                     ▼                              ▼        ║
 * ║                            ┌──────────────────┐              ┌────────┐   ║
 * ║                            │  triggerDetail   │              │ 硬件   │   ║
 * ║                            │  triggerLogic    │◀─────────────│ FPGA   │   ║
 * ║                            └──────────────────┘  参数同步    └────────┘   ║
 * ║                                     │                              │        ║
 * ║                                     │ 缓存                         │ 事件   ║
 * ║                                     ▼                              ▼        ║
 * ║                            ┌──────────────────┐              ┌────────┐   ║
 * ║                            │   CacheUtil      │◀─────────────│RxBus/ │   ║
 * ║                            └──────────────────┘              │EventBus│   ║
 * ║                                                              └────────┘   ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系:                                                                    ║
 * ║                                                                              ║
 * ║  ┌─────────────────────────────────────────────────────────────────────┐    ║
 * ║  │                        外部依赖                                       │    ║
 * ║  ├─────────────────────────────────────────────────────────────────────┤    ║
 * ║  │ • Fragment (AndroidX)              - Fragment基类                   │    ║
 * ║  │ • RxBus                            - 事件总线                        │    ║
 * ║  │ • Command                          - 硬件命令中间件                  │    ║
 * ║  │ • CacheUtil                        - 缓存管理工具                    │    ║
 * ║  │ • TriggerFactory/TriggerLogic      - 触发器工厂/逻辑触发器          │    ║
 * ║  │ • EventFactory/EventUIObserver     - 事件工厂/UI观察者               │    ║
 * ║  │ • TopDialogScale                   - 刻度对话框                      │    ║
 * ║  │ • TopViewRadioGroup                - 自定义单选组控件                 │    ║
 * ║  │ • TopMatchTrigger                  - 触发器参数匹配工具               │    ║
 * ║  └─────────────────────────────────────────────────────────────────────┘    ║
 * ║                                                                              ║
 * ║  ┌─────────────────────────────────────────────────────────────────────┐    ║
 * ║  │                        内部依赖                                       │    ║
 * ║  ├─────────────────────────────────────────────────────────────────────┤    ║
 * ║  │ • TopMsgTriggerLogic               - 触发器配置数据模型             │    ║
 * ║  │ • OnDetailSendMsgListener          - 消息发送监听器接口             │    ║
 * ║  └─────────────────────────────────────────────────────────────────────┘    ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  线程安全说明:                                                                ║
 * ║                                                                              ║
 * ║  1. 本类运行于UI线程，所有UI操作均在主线程执行                                ║
 * ║  2. RxBus订阅默认在主线程回调，确保UI操作安全                                ║
 * ║  3. EventBus事件回调在主线程执行                                            ║
 * ║  4. triggerLogic对象非线程安全，仅限主线程访问                               ║
 * ║  5. CacheUtil操作已做线程同步处理                                            ║
 * ║  6. Command命令发送已做线程同步处理                                          ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用示例:                                                                    ║
 * ║                                                                              ║
 * ║  // 在Activity中创建Fragment实例                                             ║
 * ║  TopLayoutTriggerLogic fragment = new TopLayoutTriggerLogic();              ║
 * ║  fragment.setOnDetailSendMsgListener(listener);                             ║
 * ║  getSupportFragmentManager()                                                ║
 * ║      .beginTransaction()                                                    ║
 * ║      .replace(R.id.container, fragment)                                     ║
 * ║      .commit();                                                             ║
 * ║                                                                              ║
 * ║  // 获取当前配置                                                             ║
 * ║  TopMsgTriggerLogic detail = fragment.getTriggerDetail();                   ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  注意事项:                                                                    ║
 * ║                                                                              ║
 * ║  1. 通道数量由GlobalVar.get().getChannelsCount()决定，支持2/4/8通道         ║
 * ║  2. 时间参数有范围限制，由TopUtilScale.TIME_LOGIC_MIN/MAX定义               ║
 * ║  3. 条件类型决定显示的时间参数控件（时间高/时间低/时间）                     ║
 * ║  4. 所有参数变更会自动缓存到CacheUtil                                       ║
 * ║  5. 硬件参数变更会通过EventBus同步到UI                                       ║
 * ║                                                                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopLayoutTriggerLogic extends Fragment { // 逻辑触发器配置Fragment类，继承自Fragment //

    // ==================== 成员变量定义区域 ==================== //

    /** Fragment上下文对象，用于访问系统资源和启动组件 */ // Fragment上下文对象 //
    private Context context; // 上下文对象 //

    /** 根布局视图组，用于控制布局可见性和查找子视图 */ // 根布局视图组 //
    private ViewGroup layout; // 根布局 //

    /** 刻度对话框，用于时间参数的精确调节 */ // 刻度对话框 //
    private TopDialogScale scaleDialog; // 刻度对话框 //

    /** 通道1逻辑状态单选组（高/低/忽略） */ // 通道1单选组 //
    private TopViewRadioGroup rgCh1; // 通道1单选组 //

    /** 通道2逻辑状态单选组（高/低/忽略） */ // 通道2单选组 //
    private TopViewRadioGroup rgCh2; // 通道2单选组 //

    /** 通道3逻辑状态单选组（高/低/忽略） */ // 通道3单选组 //
    private TopViewRadioGroup rgCh3; // 通道3单选组 //

    /** 通道4逻辑状态单选组（高/低/忽略） */ // 通道4单选组 //
    private TopViewRadioGroup rgCh4; // 通道4单选组 //

    /** 通道5逻辑状态单选组（高/低/忽略） */ // 通道5单选组 //
    private TopViewRadioGroup rgCh5; // 通道5单选组 //

    /** 通道6逻辑状态单选组（高/低/忽略） */ // 通道6单选组 //
    private TopViewRadioGroup rgCh6; // 通道6单选组 //

    /** 通道7逻辑状态单选组（高/低/忽略） */ // 通道7单选组 //
    private TopViewRadioGroup rgCh7; // 通道7单选组 //

    /** 通道8逻辑状态单选组（高/低/忽略） */ // 通道8单选组 //
    private TopViewRadioGroup rgCh8; // 通道8单选组 //

    /** 逻辑关系单选组（AND/OR/NAND/NOR等） */ // 逻辑关系单选组 //
    private TopViewRadioGroup rgTriggerLogic; // 逻辑关系单选组 //

    /** 触发条件单选组（时间高/时间低/时间/组合） */ // 触发条件单选组 //
    private TopViewRadioGroup rgCondition; // 触发条件单选组 //

    /** 时间参数显示文本框（用于时间条件） */ // 时间参数文本框 //
    private TextView tvTime; // 时间参数文本框 //

    /** 时间高参数显示文本框（用于时间高条件） */ // 时间高参数文本框 //
    private TextView tvTimeHigh; // 时间高参数文本框 //

    /** 时间低参数显示文本框（用于时间低条件） */ // 时间低参数文本框 //
    private TextView tvTimeLow; // 时间低参数文本框 //

    /** 时间高参数布局容器 */ // 时间高布局 //
    private LinearLayout layoutTimeHigh; // 时间高布局 //

    /** 时间低参数布局容器 */ // 时间低布局 //
    private LinearLayout layoutTimeLow; // 时间低布局 //

    /** 触发器配置详情数据模型，存储所有配置参数 */ // 触发器详情数据 //
    private TopMsgTriggerLogic triggerDetail; // 触发器详情 //

    /** 详情消息发送监听器，用于通知外部配置变更 */ // 消息发送监听器 //
    private OnDetailSendMsgListener onDetailSendMsgListener; // 消息发送监听器 //

    /** 逻辑触发器核心对象，与硬件层交互 */ // 逻辑触发器对象 //
    private TriggerLogic triggerLogic; // 逻辑触发器对象 //

    /** 当前设备支持的通道数量（2/4/8） */ // 通道数量 //
    private final int channelCount = GlobalVar.get().getChannelsCount(); // 通道数量 //

    // ==================== 生命周期方法区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: onCreateView
     * 方法描述: Fragment生命周期方法，创建并返回Fragment的根视图
     *
     * @param inflater           布局填充器，用于将XML布局转换为View
     * @param container          父容器，Fragment将被插入到此容器中
     * @param savedInstanceState 保存的实例状态，用于恢复Fragment状态
     * @return View              Fragment的根视图
     *
     * 功能说明:
     * 1. 加载R.layout.layout_triggerlogic布局文件
     * 2. 该布局包含8个通道配置、逻辑关系配置、条件配置等UI控件
     *
     * 调用时机: Fragment被创建时由系统自动调用
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Nullable // 可空注解 //
    @Override // 重写注解 //
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图 //
        return inflater.inflate(R.layout.layout_triggerlogic, container, false); // 加载逻辑触发器布局 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: onViewCreated
     * 方法描述: Fragment生命周期方法，视图创建完成后的初始化
     *
     * @param view               创建的视图
     * @param savedInstanceState 保存的实例状态
     *
     * 功能说明:
     * 1. 获取Activity上下文
     * 2. 保存根布局引用
     * 3. 初始化UI控件
     * 4. 初始化数据模型
     * 5. 初始化事件监听和订阅
     *
     * 调用时机: onCreateView之后由系统自动调用
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Override // 重写注解 //
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调 //
        this.context = getActivity(); // 获取Activity上下文 //
        layout = (ViewGroup) view; // 保存根布局引用 //
        initView(view); // 初始化UI控件 //
        initData(); // 初始化数据 //
        initControl(); // 初始化控制逻辑 //
    } // 方法结束 //

    // ==================== 初始化方法区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: initView
     * 方法描述: 初始化所有UI控件并设置数据
     *
     * @param view Fragment的根视图
     *
     * 功能说明:
     * 1. 查找并初始化所有通道单选组控件（rgCh1~rgCh8）
     * 2. 查找并初始化逻辑关系和条件单选组控件
     * 3. 查找并初始化时间参数相关控件
     * 4. 获取刻度对话框引用
     * 5. 根据通道数量隐藏不可用的通道控件
     * 6. 获取逻辑触发器实例
     *
     * UI控件说明:
     * - rgCh1~rgCh8: 8个通道的逻辑状态选择（高/低/忽略）
     * - rgTriggerLogic: 逻辑关系选择（AND/OR/NAND/NOR等）
     * - rgCondition: 触发条件选择（时间高/时间低/时间/组合）
     * - tvTime/tvTimeHigh/tvTimeLow: 时间参数显示
     * - scaleDialog: 时间参数调节对话框
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void initView(View view) { // 初始化视图控件 //
        // 初始化通道1单选组，设置标题和选项数组 //
        rgCh1 = (TopViewRadioGroup) view.findViewById(R.id.ch1); // 查找通道1单选组 //
        rgCh1.setData(R.string.triggerCh1, R.array.triggerCh, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化通道2单选组，设置标题和选项数组 //
        rgCh2 = (TopViewRadioGroup) view.findViewById(R.id.ch2); // 查找通道2单选组 //
        rgCh2.setData(R.string.triggerCh2, R.array.triggerCh, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化通道3单选组，设置标题和选项数组 //
        rgCh3 = (TopViewRadioGroup) view.findViewById(R.id.ch3); // 查找通道3单选组 //
        rgCh3.setData(R.string.triggerCh3, R.array.triggerCh, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化通道4单选组，设置标题和选项数组 //
        rgCh4 = (TopViewRadioGroup) view.findViewById(R.id.ch4); // 查找通道4单选组 //
        rgCh4.setData(R.string.triggerCh4, R.array.triggerCh, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化通道5单选组，设置标题和选项数组 //
        rgCh5 = (TopViewRadioGroup) view.findViewById(R.id.ch5); // 查找通道5单选组 //
        rgCh5.setData(R.string.triggerCh5, R.array.triggerCh, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化通道6单选组，设置标题和选项数组 //
        rgCh6 = (TopViewRadioGroup) view.findViewById(R.id.ch6); // 查找通道6单选组 //
        rgCh6.setData(R.string.triggerCh6, R.array.triggerCh, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化通道7单选组，设置标题和选项数组 //
        rgCh7 = (TopViewRadioGroup) view.findViewById(R.id.ch7); // 查找通道7单选组 //
        rgCh7.setData(R.string.triggerCh7, R.array.triggerCh, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化通道8单选组，设置标题和选项数组 //
        rgCh8 = (TopViewRadioGroup) view.findViewById(R.id.ch8); // 查找通道8单选组 //
        rgCh8.setData(R.string.triggerCh8, R.array.triggerCh, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化逻辑关系单选组 //
        rgTriggerLogic = (TopViewRadioGroup) view.findViewById(R.id.triggerLogic); // 查找逻辑关系单选组 //
        rgTriggerLogic.setData(R.string.triggerLogic, R.array.triggerLogic, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化触发条件单选组 //
        rgCondition = (TopViewRadioGroup) view.findViewById(R.id.condition); // 查找触发条件单选组 //
        rgCondition.setData(R.string.triggerLogicCondition, R.array.triggerLogicCondition, onSelectChangedListener); // 设置数据和监听器 //

        // 初始化时间参数文本框 //
        tvTime = (TextView) view.findViewById(R.id.tvLogic); // 查找时间参数文本框 //
        tvTime.setOnClickListener(onClickListener); // 设置点击监听器 //

        // 初始化时间高参数文本框 //
        tvTimeHigh = (TextView) view.findViewById(R.id.timeHighDetail); // 查找时间高参数文本框 //
        tvTimeLow = (TextView) view.findViewById(R.id.timeLowDetail); // 查找时间低参数文本框 //

        // 初始化时间参数布局容器 //
        layoutTimeHigh = (LinearLayout) view.findViewById(R.id.layoutLogicTimeHigh); // 查找时间高布局 //
        layoutTimeLow = (LinearLayout) view.findViewById(R.id.layoutLogicTimeLow); // 查找时间低布局 //

        // 设置时间参数点击监听器 //
        tvTimeHigh.setOnClickListener(onClickListener); // 设置时间高点击监听器 //
        tvTimeLow.setOnClickListener(onClickListener); // 设置时间低点击监听器 //

        // 获取主Activity中的刻度对话框引用 //
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale); // 获取刻度对话框 //

        // 根据通道数量设置不可用通道的可见性 //
        if (channelCount == GlobalVar.CHANNEL_COUNT_2) { // 如果是2通道设备 //
            // 2通道设备：隐藏通道3-8 //
            TopViewRadioGroup[] count2Views = {rgCh3, rgCh4, rgCh5, rgCh6, rgCh7, rgCh8}; // 创建不可用通道数组 //
            setVisibilityAndSelectedIndex(count2Views, false, 2); // 隐藏并设置为忽略状态 //
        } else if (channelCount == GlobalVar.CHANNEL_COUNT_4) { // 如果是4通道设备 //
            // 4通道设备：显示通道3-4，隐藏通道5-8 //
            TopViewRadioGroup[] count4Views = {rgCh5, rgCh6, rgCh7, rgCh8}; // 创建不可用通道数组 //
            rgCh3.setVisibility(View.VISIBLE); // 显示通道3 //
            rgCh4.setVisibility(View.VISIBLE); // 显示通道4 //
            setVisibilityAndSelectedIndex(count4Views, false, 2); // 隐藏通道5-8并设置为忽略状态 //
        } // 通道数量判断结束 //

        // 获取逻辑触发器实例 //
        triggerLogic = (TriggerLogic) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_LOGIC); // 从触发器工厂获取逻辑触发器 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: setVisibilityAndSelectedIndex
     * 方法描述: 批量设置单选组的可见性和选中状态
     *
     * @param views        单选组数组
     * @param isVisible    是否可见
     * @param selectedIndex 选中的索引
     *
     * 功能说明:
     * 1. 遍历所有单选组，设置可见性
     * 2. 设置选中索引
     * 3. 如果不可见，移除监听器以避免无效回调
     *
     * 使用场景: 根据设备通道数量隐藏不可用的通道控件
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void setVisibilityAndSelectedIndex(TopViewRadioGroup[] views, boolean isVisible, int selectedIndex) { // 设置可见性和选中索引 //
        for (TopViewRadioGroup view : views) { // 遍历所有单选组 //
            view.setVisibility(isVisible ? View.VISIBLE : View.GONE); // 设置可见性 //
            view.setSelectedIndex(selectedIndex); // 设置选中索引 //
            if (!isVisible) { // 如果不可见 //
                view.setOnListener(null); // 移除监听器 //
            } // 可见性判断结束 //
        } // 遍历结束 //
    } // 方法结束 //


    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: initData
     * 方法描述: 初始化触发器配置数据模型
     *
     * 功能说明:
     * 1. 创建TopMsgTriggerLogic数据模型实例
     * 2. 从UI控件读取当前配置值
     * 3. 将配置值设置到数据模型中
     *
     * 数据模型内容:
     * - 通道1-8的逻辑状态
     * - 逻辑关系类型
     * - 触发条件类型
     * - 时间参数（时间高/时间低/时间）
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void initData() { // 初始化数据 //
        // 创建触发器详情数据模型 //
        triggerDetail = new TopMsgTriggerLogic(); // 创建触发器详情实例 //

        // 从UI控件读取各通道状态并设置到数据模型 //
        triggerDetail.setCh1(rgCh1.getSelected()); // 设置通道1状态 //
        triggerDetail.setCh2(rgCh2.getSelected()); // 设置通道2状态 //
        triggerDetail.setCh3(rgCh3.getSelected()); // 设置通道3状态 //
        triggerDetail.setCh4(rgCh4.getSelected()); // 设置通道4状态 //
        triggerDetail.setCh5(rgCh5.getSelected()); // 设置通道5状态 //
        triggerDetail.setCh6(rgCh6.getSelected()); // 设置通道6状态 //
        triggerDetail.setCh7(rgCh7.getSelected()); // 设置通道7状态 //
        triggerDetail.setCh8(rgCh8.getSelected()); // 设置通道8状态 //

        // 设置逻辑关系和触发条件 //
        triggerDetail.setTriggerLogic(rgTriggerLogic.getSelected()); // 设置逻辑关系 //
        triggerDetail.setCondition(rgCondition.getSelected()); // 设置触发条件 //

        // 设置时间参数 //
        triggerDetail.setTimeHighDetail(tvTimeHigh.getText().toString()); // 设置时间高参数 //
        triggerDetail.setTimeLowDetail(tvTimeLow.getText().toString()); // 设置时间低参数 //
        triggerDetail.setLogic(tvTime.getText().toString()); // 设置时间参数 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: initControl
     * 方法描述: 初始化事件监听和订阅
     *
     * 功能说明:
     * 1. 订阅RxBus的缓存加载事件
     * 2. 订阅RxBus的UI命令事件
     * 3. 注册EventBus的触发器参数变更事件观察者
     *
     * 事件说明:
     * - MAIN_LOAD_CACHE: 缓存加载事件，用于恢复配置
     * - COMMAND_TO_UI: UI命令事件，用于接收硬件参数变更
     * - EVENT_TRIGGER_PARAM: 触发器参数变更事件，用于同步硬件参数
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void initControl() { // 初始化控制逻辑 //
        // 订阅缓存加载事件 //
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件 //

        // 订阅UI命令事件 //
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅UI命令事件 //

        // 注册触发器参数变更事件观察者 //
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_PARAM, eventUITriggerParam); // 添加事件观察者 //
    } // 方法结束 //

    // ==================== 缓存加载方法区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: setCache
     * 方法描述: 从缓存加载配置并同步到UI和硬件
     *
     * 功能说明:
     * 1. 从CacheUtil读取所有配置参数
     * 2. 更新UI控件显示
     * 3. 根据条件类型显示/隐藏时间参数控件
     * 4. 更新数据模型
     * 5. 发送配置到硬件
     * 6. 同步到TriggerLogic对象
     *
     * 缓存参数:
     * - TOP_SLIP_TRIGGER_LOGIC_CH1~CH8: 通道1-8的逻辑状态
     * - TOP_SLIP_TRIGGER_LOGIC_LOGIC: 逻辑关系类型
     * - TOP_SLIP_TRIGGER_LOGIC_CONDITION: 触发条件类型
     * - TOP_SLIP_TRIGGER_LOGIC_TVLOGIC: 时间参数
     * - TOP_SLIP_TRIGGER_LOGIC_TIME_HIGH: 时间高参数
     * - TOP_SLIP_TRIGGER_LOGIC_TIME_LOW: 时间低参数
     *
     * 条件类型说明:
     * - 0: 时间高条件，显示时间高参数
     * - 1: 时间低条件，显示时间低参数
     * - 2: 时间条件，显示时间参数
     * - 3: 组合条件，显示时间高和时间低参数
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void setCache() { // 从缓存加载配置 //
        // 从缓存读取各通道状态 //
        int ch1 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH1); // 读取通道1状态 //
        int ch2 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH2); // 读取通道2状态 //
        int ch3 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH3); // 读取通道3状态 //
        int ch4 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH4); // 读取通道4状态 //
        int ch5 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH5); // 读取通道5状态 //
        int ch6 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH6); // 读取通道6状态 //
        int ch7 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH7); // 读取通道7状态 //
        int ch8 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH8); // 读取通道8状态 //

        // 从缓存读取逻辑关系和触发条件 //
        int logic = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_LOGIC); // 读取逻辑关系 //
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CONDITION); // 读取触发条件 //

        // 从缓存读取时间参数 //
        String tvLogic = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_TVLOGIC); // 读取时间参数 //
        String timeHigh = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_TIME_HIGH); // 读取时间高参数 //
        String timeLow = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_TIME_LOW); // 读取时间低参数 //

        // 更新通道1-2的UI显示 //
        this.rgCh1.setSelectedIndex(ch1); // 设置通道1选中状态 //
        this.rgCh2.setSelectedIndex(ch2); // 设置通道2选中状态 //

        // 根据通道数量更新通道3-8的UI显示 //
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) { // 如果是8通道设备 //
            // 8通道设备：设置所有通道状态 //
            this.rgCh3.setSelectedIndex(ch3); // 设置通道3选中状态 //
            this.rgCh4.setSelectedIndex(ch4); // 设置通道4选中状态 //
            this.rgCh5.setSelectedIndex(ch5); // 设置通道5选中状态 //
            this.rgCh6.setSelectedIndex(ch6); // 设置通道6选中状态 //
            this.rgCh7.setSelectedIndex(ch7); // 设置通道7选中状态 //
            this.rgCh8.setSelectedIndex(ch8); // 设置通道8选中状态 //
        } else if (channelCount == GlobalVar.CHANNEL_COUNT_4) { // 如果是4通道设备 //
            // 4通道设备：设置通道3-4，通道5-8设为忽略 //
            this.rgCh3.setSelectedIndex(ch3); // 设置通道3选中状态 //
            this.rgCh4.setSelectedIndex(ch4); // 设置通道4选中状态 //
            this.rgCh5.setSelectedIndex(2); // 设置通道5为忽略 //
            this.rgCh6.setSelectedIndex(2); // 设置通道6为忽略 //
            this.rgCh7.setSelectedIndex(2); // 设置通道7为忽略 //
            this.rgCh8.setSelectedIndex(2); // 设置通道8为忽略 //
        } else { // 其他情况（2通道设备） //
            // 2通道设备：通道3-8都设为忽略 //
            this.rgCh3.setSelectedIndex(2); // 设置通道3为忽略 //
            this.rgCh4.setSelectedIndex(2); // 设置通道4为忽略 //
            this.rgCh5.setSelectedIndex(2); // 设置通道5为忽略 //
            this.rgCh6.setSelectedIndex(2); // 设置通道6为忽略 //
            this.rgCh7.setSelectedIndex(2); // 设置通道7为忽略 //
            this.rgCh8.setSelectedIndex(2); // 设置通道8为忽略 //
        } // 通道数量判断结束 //

        // 更新逻辑关系和触发条件的UI显示 //
        this.rgTriggerLogic.setSelectedIndex(logic); // 设置逻辑关系选中状态 //
        this.rgCondition.setSelectedIndex(condition); // 设置触发条件选中状态 //

        // 更新时间参数的UI显示 //
        this.tvTime.setText(tvLogic); // 设置时间参数文本 //
        this.tvTimeHigh.setText(timeHigh); // 设置时间高参数文本 //
        this.tvTimeLow.setText(timeLow); // 设置时间低参数文本 //

        // 根据触发条件类型显示/隐藏时间参数控件 //
        switch (condition) { // 条件判断 //
            case 0: // 时间高条件 //
                tvTime.setVisibility(View.INVISIBLE); // 隐藏时间参数 //
                layoutTimeHigh.setVisibility(View.VISIBLE); // 显示时间高参数 //
                layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低参数 //
                break; // 跳出 //
            case 1: // 时间低条件 //
                tvTime.setVisibility(View.INVISIBLE); // 隐藏时间参数 //
                layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高参数 //
                layoutTimeLow.setVisibility(View.VISIBLE); // 显示时间低参数 //
                break; // 跳出 //
            case 2: // 时间条件 //
                tvTime.setVisibility(View.VISIBLE); // 显示时间参数 //
                layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高参数 //
                layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低参数 //
                break; // 跳出 //
            case 3: // 组合条件 //
                tvTime.setVisibility(View.INVISIBLE); // 隐藏时间参数 //
                layoutTimeHigh.setVisibility(View.VISIBLE); // 显示时间高参数 //
                layoutTimeLow.setVisibility(View.VISIBLE); // 显示时间低参数 //
                break; // 跳出 //
            default: // 默认情况 //
                tvTime.setVisibility(View.INVISIBLE); // 隐藏时间参数 //
                layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高参数 //
                layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低参数 //
                break; // 跳出 //
        } // 条件判断结束 //

        // 更新数据模型 //
        triggerDetail.setCh1(this.rgCh1.getSelected()); // 设置通道1状态到数据模型 //
        triggerDetail.setCh2(this.rgCh2.getSelected()); // 设置通道2状态到数据模型 //
        triggerDetail.setCh3(this.rgCh3.getSelected()); // 设置通道3状态到数据模型 //
        triggerDetail.setCh4(this.rgCh4.getSelected()); // 设置通道4状态到数据模型 //
        triggerDetail.setCh5(this.rgCh5.getSelected()); // 设置通道5状态到数据模型 //
        triggerDetail.setCh6(this.rgCh6.getSelected()); // 设置通道6状态到数据模型 //
        triggerDetail.setCh7(this.rgCh7.getSelected()); // 设置通道7状态到数据模型 //
        triggerDetail.setCh8(this.rgCh8.getSelected()); // 设置通道8状态到数据模型 //
        triggerDetail.setTriggerLogic(rgTriggerLogic.getSelected()); // 设置逻辑关系到数据模型 //
        triggerDetail.setCondition(this.rgCondition.getSelected()); // 设置触发条件到数据模型 //
        triggerDetail.setLogic(tvLogic); // 设置时间参数到数据模型 //
        triggerDetail.setTimeHighDetail(timeHigh); // 设置时间高参数到数据模型 //
        triggerDetail.setTimeLowDetail(timeLow); // 设置时间低参数到数据模型 //

        // 发送消息通知外部 //
        sendMsg(false); // 发送配置变更消息 //

        // 发送命令到硬件 //
        Command.get().getTrigger_logic().Status(0, ch1, false); // 发送通道1状态命令 //
        Command.get().getTrigger_logic().Status(1, ch2, false); // 发送通道2状态命令 //
        Command.get().getTrigger_logic().Status(2, ch3, false); // 发送通道3状态命令 //
        Command.get().getTrigger_logic().Status(3, ch4, false); // 发送通道4状态命令 //
        Command.get().getTrigger_logic().Status(4, ch5, false); // 发送通道5状态命令 //
        Command.get().getTrigger_logic().Status(5, ch6, false); // 发送通道6状态命令 //
        Command.get().getTrigger_logic().Status(6, ch7, false); // 发送通道7状态命令 //
        Command.get().getTrigger_logic().Status(7, ch8, false); // 发送通道8状态命令 //
        Command.get().getTrigger_logic().Function(logic, false); // 发送逻辑关系命令 //
        Command.get().getTrigger_logic().Condition(condition, false); // 发送触发条件命令 //
        Command.get().getTrigger_logic().HTime(TBookUtil.getSFromTime(timeHigh), false); // 发送时间高参数命令 //
        Command.get().getTrigger_logic().LTime(TBookUtil.getSFromTime(timeLow), false); // 发送时间低参数命令 //
        Command.get().getTrigger_logic().Time(TBookUtil.getSFromTime(tvLogic), false); // 发送时间参数命令 //

        // 同步到TriggerLogic对象 //
        triggerLogic.setLogicValids(0, TopMatchTrigger.triggerLogicChToScope(ch1)); // 设置通道1逻辑有效值 //
        triggerLogic.setLogicValids(1, TopMatchTrigger.triggerLogicChToScope(ch2)); // 设置通道2逻辑有效值 //

        // 根据通道数量设置通道3-8的逻辑有效值 //
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) { // 如果是8通道设备 //
            triggerLogic.setLogicValids(2, TopMatchTrigger.triggerLogicChToScope(ch3)); // 设置通道3逻辑有效值 //
            triggerLogic.setLogicValids(3, TopMatchTrigger.triggerLogicChToScope(ch4)); // 设置通道4逻辑有效值 //
            triggerLogic.setLogicValids(4, TopMatchTrigger.triggerLogicChToScope(ch5)); // 设置通道5逻辑有效值 //
            triggerLogic.setLogicValids(5, TopMatchTrigger.triggerLogicChToScope(ch6)); // 设置通道6逻辑有效值 //
            triggerLogic.setLogicValids(6, TopMatchTrigger.triggerLogicChToScope(ch7)); // 设置通道7逻辑有效值 //
            triggerLogic.setLogicValids(7, TopMatchTrigger.triggerLogicChToScope(ch8)); // 设置通道8逻辑有效值 //
        } else if (channelCount == GlobalVar.CHANNEL_COUNT_4) { // 如果是4通道设备 //
            triggerLogic.setLogicValids(2, TopMatchTrigger.triggerLogicChToScope(ch3)); // 设置通道3逻辑有效值 //
            triggerLogic.setLogicValids(3, TopMatchTrigger.triggerLogicChToScope(ch4)); // 设置通道4逻辑有效值 //
            triggerLogic.setLogicValids(4, TriggerLogic.LOGIC_NONE); // 设置通道5为无 //
            triggerLogic.setLogicValids(5, TriggerLogic.LOGIC_NONE); // 设置通道6为无 //
            triggerLogic.setLogicValids(6, TriggerLogic.LOGIC_NONE); // 设置通道7为无 //
            triggerLogic.setLogicValids(7, TriggerLogic.LOGIC_NONE); // 设置通道8为无 //
        } else { // 其他情况（2通道设备） //
            triggerLogic.setLogicValids(2, TriggerLogic.LOGIC_NONE); // 设置通道3为无 //
            triggerLogic.setLogicValids(3, TriggerLogic.LOGIC_NONE); // 设置通道4为无 //
            triggerLogic.setLogicValids(4, TriggerLogic.LOGIC_NONE); // 设置通道5为无 //
            triggerLogic.setLogicValids(5, TriggerLogic.LOGIC_NONE); // 设置通道6为无 //
            triggerLogic.setLogicValids(6, TriggerLogic.LOGIC_NONE); // 设置通道7为无 //
            triggerLogic.setLogicValids(7, TriggerLogic.LOGIC_NONE); // 设置通道8为无 //
        } // 通道数量判断结束 //

        // 设置其他参数到TriggerLogic对象 //
        triggerLogic.setLogic(logic); // 设置逻辑关系 //
        triggerLogic.setTimeHigh(TBookUtil.getPsFromTime(timeHigh) / 1000); // 设置时间高参数（纳秒） //
        triggerLogic.setTimeLow(TBookUtil.getPsFromTime(timeLow) / 1000); // 设置时间低参数（纳秒） //
        triggerLogic.setCondition(TopMatchTrigger.triggerConditionToScope(condition)); // 设置触发条件 //
        triggerLogic.setLogicTime(TBookUtil.getPsFromTime(tvLogic) / 1000); // 设置时间参数（纳秒） //
    } // 方法结束 //

    // ==================== 公共方法区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: getTriggerDetail
     * 方法描述: 获取触发器配置详情数据模型
     *
     * @return TopMsgTriggerLogic 触发器配置详情对象
     *
     * 功能说明:
     * 返回当前逻辑触发器的所有配置参数，包括：
     * - 通道1-8的逻辑状态
     * - 逻辑关系类型
     * - 触发条件类型
     * - 时间参数
     *
     * 使用场景: 外部需要读取当前配置时调用
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public TopMsgTriggerLogic getTriggerDetail() { // 获取触发器详情 //
        return triggerDetail; // 返回触发器详情对象 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: setOnDetailSendMsgListener
     * 方法描述: 设置详情消息发送监听器
     *
     * @param onDetailSendMsgListener 消息发送监听器
     *
     * 功能说明:
     * 设置监听器后，当配置参数变更时会通过此监听器通知外部
     *
     * 使用场景: Activity或父Fragment需要监听配置变更时调用
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息发送监听器 //
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 保存监听器引用 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: sendMsg
     * 方法描述: 发送配置变更消息到外部监听器
     *
     * @param isFromEventBus 是否来自EventBus事件
     *
     * 功能说明:
     * 当配置参数变更时，通过监听器通知外部
     *
     * 参数说明:
     * - isFromEventBus=true: 表示配置来自硬件同步，不需要再发送到硬件
     * - isFromEventBus=false: 表示配置来自用户操作，需要发送到硬件
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void sendMsg(boolean isFromEventBus) { // 发送消息 //
        if (onDetailSendMsgListener != null) { // 如果监听器不为空 //
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用监听器回调 //
        } // 监听器判断结束 //
    } // 方法结束 //

    // ==================== RxJava消费者区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 消费者名称: consumerLoadCache
     * 消费者描述: 缓存加载事件消费者
     *
     * 功能说明:
     * 监听RxBus的MAIN_LOAD_CACHE事件，当收到缓存加载事件时：
     * 1. 从缓存加载配置参数
     * 2. 标记本Fragment缓存已加载
     *
     * 触发时机: 应用启动或配置恢复时
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者 //
        @Override // 重写注解 //
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收事件 //
            setCache(); // 从缓存加载配置 //
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerLogic, true); // 标记缓存已加载 //
        } // 方法结束 //
    }; // 消费者结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 消费者名称: consumerCommandToUI
     * 消费者描述: UI命令事件消费者
     *
     * 功能说明:
     * 监听RxBus的COMMAND_TO_UI事件，处理硬件发送的参数变更命令：
     * - FLAG_TRIGGERLOGIC_STATUS: 通道状态变更
     * - FLAG_TRIGGERLOGIC_FUNCTION: 逻辑关系变更
     * - FLAG_TRIGGERLOGIC_CONDITION: 触发条件变更
     * - FLAG_TRIGGERLOGIC_HTIME: 时间高参数变更
     * - FLAG_TRIGGERLOGIC_LOWTIME: 时间低参数变更
     * - FLAG_TRIGGERLOGIC_BTIME: 组合时间参数变更
     * - FLAG_TRIGGERLOGIC_TIME: 时间参数变更
     *
     * 处理流程:
     * 1. 解析命令参数
     * 2. 更新UI控件显示
     * 3. 触发选择变更回调
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // UI命令消费者 //
        @Override // 重写注解 //
        public void accept(@NonNull CommandMsgToUI commandMsgToUI) throws Exception { // 接收事件 //
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发 //
                case CommandMsgToUI.FLAG_TRIGGERLOGIC_STATUS: { // 通道状态变更命令 //
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 分割参数 //
                    switch (Integer.parseInt(params[0])) { // 根据通道号分发 //
                        case 0: // 通道1 //
                            if (rgCh1.getSelected().getIndex() != Integer.parseInt(params[1])) { // 如果状态有变化 //
                                rgCh1.setSelectedIndex(Integer.parseInt(params[1])); // 更新UI //
                                onSelectChanged(rgCh1, rgCh1.getSelected(), false); // 触发回调 //
                            } // 状态判断结束 //
                            break; // 跳出 //
                        case 1: // 通道2 //
                            if (rgCh2.getSelected().getIndex() != Integer.parseInt(params[1])) { // 如果状态有变化 //
                                rgCh2.setSelectedIndex(Integer.parseInt(params[1])); // 更新UI //
                                onSelectChanged(rgCh2, rgCh2.getSelected(), false); // 触发回调 //
                            } // 状态判断结束 //
                            break; // 跳出 //
                        case 2: // 通道3 //
                            if (rgCh3.getSelected().getIndex() != Integer.parseInt(params[1])) { // 如果状态有变化 //
                                rgCh3.setSelectedIndex(Integer.parseInt(params[1])); // 更新UI //
                                onSelectChanged(rgCh3, rgCh3.getSelected(), false); // 触发回调 //
                            } // 状态判断结束 //
                            break; // 跳出 //
                        case 3: // 通道4 //
                            if (rgCh4.getSelected().getIndex() != Integer.parseInt(params[1])) { // 如果状态有变化 //
                                rgCh4.setSelectedIndex(Integer.parseInt(params[1])); // 更新UI //
                                onSelectChanged(rgCh4, rgCh4.getSelected(), false); // 触发回调 //
                            } // 状态判断结束 //
                            break; // 跳出 //
                        case 4: // 通道5 //
                            if (rgCh5.getSelected().getIndex() != Integer.parseInt(params[1])) { // 如果状态有变化 //
                                rgCh5.setSelectedIndex(Integer.parseInt(params[1])); // 更新UI //
                                onSelectChanged(rgCh5, rgCh5.getSelected(), false); // 触发回调 //
                            } // 状态判断结束 //
                            break; // 跳出 //
                        case 5: // 通道6 //
                            if (rgCh6.getSelected().getIndex() != Integer.parseInt(params[1])) { // 如果状态有变化 //
                                rgCh6.setSelectedIndex(Integer.parseInt(params[1])); // 更新UI //
                                onSelectChanged(rgCh6, rgCh6.getSelected(), false); // 触发回调 //
                            } // 状态判断结束 //
                            break; // 跳出 //
                        case 6: // 通道7 //
                            if (rgCh7.getSelected().getIndex() != Integer.parseInt(params[1])) { // 如果状态有变化 //
                                rgCh7.setSelectedIndex(Integer.parseInt(params[1])); // 更新UI //
                                onSelectChanged(rgCh7, rgCh7.getSelected(), false); // 触发回调 //
                            } // 状态判断结束 //
                            break; // 跳出 //
                        case 7: // 通道8 //
                            if (rgCh8.getSelected().getIndex() != Integer.parseInt(params[1])) { // 如果状态有变化 //
                                rgCh8.setSelectedIndex(Integer.parseInt(params[1])); // 更新UI //
                                onSelectChanged(rgCh8, rgCh8.getSelected(), false); // 触发回调 //
                            } // 状态判断结束 //
                            break; // 跳出 //
                    } // 通道号判断结束 //
                    break; // 跳出 //
                } // 通道状态命令结束 //
                case CommandMsgToUI.FLAG_TRIGGERLOGIC_FUNCTION: // 逻辑关系变更命令 //
                    if (rgTriggerLogic.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 如果状态有变化 //
                        rgTriggerLogic.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新UI //
                        onSelectChanged(rgTriggerLogic, rgTriggerLogic.getSelected(), false); // 触发回调 //
                    } // 状态判断结束 //
                    break; // 跳出 //
                case CommandMsgToUI.FLAG_TRIGGERLOGIC_CONDITION: // 触发条件变更命令 //
                    if (rgCondition.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 如果状态有变化 //
                        rgCondition.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新UI //
                        onSelectChanged(rgCondition, rgCondition.getSelected(), false); // 触发回调 //
                    } // 状态判断结束 //
                    break; // 跳出 //
                case CommandMsgToUI.FLAG_TRIGGERLOGIC_HTIME: { // 时间高参数变更命令 //
                    double checkBeforeHigh = Double.parseDouble(commandMsgToUI.getParam()); // 解析参数 //
                    long checkTimeHigh = TopUtilScale.checkTime((long) (checkBeforeHigh * TopUtilScale.TIME_S2NS) // 检查时间范围 //
                            , TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX); // 检查时间范围 //
                    String timeHigh = TBookUtil.getTimeFromS(checkTimeHigh * 1.0 / TopUtilScale.TIME_S2NS); // 转换时间格式 //
                    onTextChanged(tvTimeHigh, timeHigh, false); // 触发文本变更 //
                    break; // 跳出 //
                } // 时间高命令结束 //
                case CommandMsgToUI.FLAG_TRIGGERLOGIC_LOWTIME: { // 时间低参数变更命令 //
                    double checkBeforeLow = Double.parseDouble(commandMsgToUI.getParam()); // 解析参数 //
                    long checkTimeLow = TopUtilScale.checkTime((long) (checkBeforeLow * TopUtilScale.TIME_S2NS) // 检查时间范围 //
                            , TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX); // 检查时间范围 //
                    String timeLow = TBookUtil.getTimeFromS(checkTimeLow * 1.0 / TopUtilScale.TIME_S2NS); // 转换时间格式 //
                    onTextChanged(tvTimeLow, timeLow, false); // 触发文本变更 //
                    break; // 跳出 //
                } // 时间低命令结束 //
                case CommandMsgToUI.FLAG_TRIGGERLOGIC_BTIME: { // 组合时间参数变更命令 //
                    String[] param = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 分割参数 //
                    double checkBeforeBHigh = Double.parseDouble(param[0]); // 解析时间高参数 //
                    long checkTimeBHigh = TopUtilScale.checkTime((long) (checkBeforeBHigh * TopUtilScale.TIME_S2NS) // 检查时间范围 //
                            , TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX); // 检查时间范围 //
                    String timeBHigh = TBookUtil.getTimeFromS(checkTimeBHigh * 1.0 / TopUtilScale.TIME_S2NS); // 转换时间格式 //
                    double checkBeforeBLow = Double.parseDouble(param[1]); // 解析时间低参数 //
                    long checkTimeBLow = TopUtilScale.checkTime((long) (checkBeforeBLow * TopUtilScale.TIME_S2NS) // 检查时间范围 //
                            , TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX); // 检查时间范围 //
                    String timeBLow = TBookUtil.getTimeFromS(checkTimeBLow * 1.0 / TopUtilScale.TIME_S2NS); // 转换时间格式 //
                    onTextChanged(tvTimeHigh, timeBHigh, false); // 触发时间高变更 //
                    onTextChanged(tvTimeLow, timeBLow, false); // 触发时间低变更 //
                    break; // 跳出 //
                } // 组合时间命令结束 //
                case CommandMsgToUI.FLAG_TRIGGERLOGIC_TIME: { // 时间参数变更命令 //
                    double checkBefore = Double.parseDouble(commandMsgToUI.getParam()); // 解析参数 //
                    long checkTime = TopUtilScale.checkTime((long) (checkBefore * TopUtilScale.TIME_S2NS) // 检查时间范围 //
                            , TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX); // 检查时间范围 //
                    String time = TBookUtil.getTimeFromS(checkTime * 1.0 / TopUtilScale.TIME_S2NS); // 转换时间格式 //
                    // 注释掉的判断：总是更新时间参数 //
//                    if (!tvTime.getText().toString().equals(time)) { // 如果时间有变化 //
                        onTextChanged(time, false); // 触发文本变更 //
//                    } // 时间判断结束 //
                    break; // 跳出 //
                } // 时间命令结束 //
            } // 命令标志判断结束 //
        } // 方法结束 //
    }; // 消费者结束 //

    // ==================== EventBus观察者区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 观察者名称: eventUITriggerParam
     * 观察者描述: 触发器参数变更事件观察者
     *
     * 功能说明:
     * 监听EventBus的EVENT_TRIGGER_PARAM事件，当硬件参数变更时同步到UI：
     * - 通道1-8的逻辑状态
     * - 逻辑关系类型
     * - 触发条件类型
     * - 时间参数（时间高/时间低/时间）
     *
     * 处理流程:
     * 1. 从TriggerLogic对象读取硬件参数
     * 2. 转换参数格式
     * 3. 更新UI控件显示
     * 4. 触发选择变更回调
     *
     * 注意事项:
     * - 回调参数isFromEventBus=true，避免循环发送命令
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private EventUIObserver eventUITriggerParam = new EventUIObserver() { // 触发器参数事件观察者 //
        @Override // 重写注解 //
        public void update(Object data) { // 更新方法 //
            EventBase eventBase = (EventBase) data; // 转换事件对象 //
            if (eventBase.getId() == EventFactory.EVENT_TRIGGER_PARAM) { // 如果是触发器参数事件 //
                // 同步通道1状态 //
                int ch1Index = TopMatchTrigger.triggerLogicChFromScope(triggerLogic.getLogicValid(0)); // 转换通道状态 //
                if (ch1Index != rgCh1.getSelected().getIndex()) { // 如果状态有变化 //
                    rgCh1.setSelectedIndex(ch1Index); // 更新UI //
                    onSelectChanged(rgCh1, rgCh1.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步通道2状态 //
                int ch2Index = TopMatchTrigger.triggerLogicChFromScope(triggerLogic.getLogicValid(1)); // 转换通道状态 //
                if (ch2Index != rgCh2.getSelected().getIndex()) { // 如果状态有变化 //
                    rgCh2.setSelectedIndex(ch2Index); // 更新UI //
                    onSelectChanged(rgCh2, rgCh2.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步通道3状态 //
                int ch3Index = TopMatchTrigger.triggerLogicChFromScope(triggerLogic.getLogicValid(2)); // 转换通道状态 //
                if (ch3Index != rgCh3.getSelected().getIndex()) { // 如果状态有变化 //
                    rgCh3.setSelectedIndex(ch3Index); // 更新UI //
                    onSelectChanged(rgCh3, rgCh3.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步通道4状态 //
                int ch4Index = TopMatchTrigger.triggerLogicChFromScope(triggerLogic.getLogicValid(3)); // 转换通道状态 //
                if (ch4Index != rgCh4.getSelected().getIndex()) { // 如果状态有变化 //
                    rgCh4.setSelectedIndex(ch4Index); // 更新UI //
                    onSelectChanged(rgCh4, rgCh4.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步通道5状态 //
                int ch5Index = TopMatchTrigger.triggerLogicChFromScope(triggerLogic.getLogicValid(4)); // 转换通道状态 //
                if (ch5Index != rgCh5.getSelected().getIndex()) { // 如果状态有变化 //
                    rgCh5.setSelectedIndex(ch5Index); // 更新UI //
                    onSelectChanged(rgCh5, rgCh5.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步通道6状态 //
                int ch6Index = TopMatchTrigger.triggerLogicChFromScope(triggerLogic.getLogicValid(5)); // 转换通道状态 //
                if (ch6Index != rgCh6.getSelected().getIndex()) { // 如果状态有变化 //
                    rgCh6.setSelectedIndex(ch6Index); // 更新UI //
                    onSelectChanged(rgCh6, rgCh6.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步通道7状态 //
                int ch7Index = TopMatchTrigger.triggerLogicChFromScope(triggerLogic.getLogicValid(6)); // 转换通道状态 //
                if (ch7Index != rgCh7.getSelected().getIndex()) { // 如果状态有变化 //
                    rgCh7.setSelectedIndex(ch7Index); // 更新UI //
                    onSelectChanged(rgCh7, rgCh7.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步通道8状态 //
                int ch8Index = TopMatchTrigger.triggerLogicChFromScope(triggerLogic.getLogicValid(7)); // 转换通道状态 //
                if (ch8Index != rgCh8.getSelected().getIndex()) { // 如果状态有变化 //
                    rgCh8.setSelectedIndex(ch8Index); // 更新UI //
                    onSelectChanged(rgCh8, rgCh8.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步逻辑关系 //
                int logic = triggerLogic.getLogic(); // 获取逻辑关系 //
                if (logic != rgTriggerLogic.getSelected().getIndex()) { // 如果状态有变化 //
                    rgTriggerLogic.setSelectedIndex(logic); // 更新UI //
                    onSelectChanged(rgTriggerLogic, rgTriggerLogic.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步触发条件 //
                int condition = TopMatchTrigger.triggerConditionFromScope(triggerLogic.getCondition()); // 转换触发条件 //
                if (condition != rgCondition.getSelected().getIndex()) { // 如果状态有变化 //
                    rgCondition.setSelectedIndex(condition); // 更新UI //
                    onSelectChanged(rgCondition, rgCondition.getSelected(), true); // 触发回调（来自EventBus） //
                } // 状态判断结束 //

                // 同步时间参数 //
                long checkTime = TopUtilScale.checkTime(triggerLogic.getLogicTime() // 检查时间范围 //
                        , TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX); // 检查时间范围 //
                String time = TBookUtil.getTime3FromPs(checkTime * 1000 * 10); // 转换时间格式 //
                if (!tvTime.getText().toString().equals(time)) { // 如果时间有变化 //
                    tvTime.setText(time); // 更新UI //
                    onTextChanged(time, true); // 触发回调（来自EventBus） //
                } // 时间判断结束 //

                // 同步时间高参数 //
                long checkTimeHigh = TopUtilScale.checkTime(triggerLogic.getTimeHigh() // 检查时间范围 //
                        , TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX); // 检查时间范围 //
                String timeHigh = TBookUtil.getTime3FromPs(checkTimeHigh * 1000 * 10); // 转换时间格式 //
                if (!tvTimeHigh.getText().toString().equals(timeHigh)) { // 如果时间有变化 //
                    onTextChanged(tvTimeHigh, timeHigh, true); // 触发回调（来自EventBus） //
                } // 时间判断结束 //

                // 同步时间低参数 //
                long checkTimeLow = TopUtilScale.checkTime(triggerLogic.getTimeLow() // 检查时间范围 //
                        , TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX); // 检查时间范围 //
                String timeLow = TBookUtil.getTime3FromPs(checkTimeLow * 1000 * 10); // 转换时间格式 //
                if (!tvTimeLow.getText().toString().equals(timeLow)) { // 如果时间有变化 //
                    onTextChanged(tvTimeLow, timeLow, true); // 触发回调（来自EventBus） //
                } // 时间判断结束 //
            } // 事件ID判断结束 //
        } // 方法结束 //
    }; // 观察者结束 //

    // ==================== 对话框监听器区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 监听器名称: onDismissListener
     * 监听器描述: 时间参数对话框关闭监听器
     *
     * 功能说明:
     * 当用户通过刻度对话框调节时间参数后，接收结果并更新UI
     *
     * 使用场景: tvTime控件点击后弹出的刻度对话框关闭时
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private TopDialogScale.OnDismissListener onDismissListener = new TopDialogScale.OnDismissListener() { // 时间参数对话框关闭监听器 //
        @Override // 重写注解 //
        public void onDismiss(String result) { // 对话框关闭回调 //
            onTextChanged(result, false); // 触发文本变更 //
        } // 方法结束 //
    }; // 监听器结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 监听器名称: onHighDismissListener
     * 监听器描述: 时间高参数对话框关闭监听器
     *
     * 功能说明:
     * 当用户通过刻度对话框调节时间高参数后，接收结果并更新UI
     *
     * 使用场景: tvTimeHigh控件点击后弹出的刻度对话框关闭时
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private TopDialogScale.OnDismissListener onHighDismissListener = new TopDialogScale.OnDismissListener() { // 时间高参数对话框关闭监听器 //
        @Override // 重写注解 //
        public void onDismiss(String result) { // 对话框关闭回调 //
            onTextChanged(tvTimeHigh, result, false); // 触发文本变更 //
        } // 方法结束 //
    }; // 监听器结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 监听器名称: onLowDismissListener
     * 监听器描述: 时间低参数对话框关闭监听器
     *
     * 功能说明:
     * 当用户通过刻度对话框调节时间低参数后，接收结果并更新UI
     *
     * 使用场景: tvTimeLow控件点击后弹出的刻度对话框关闭时
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private TopDialogScale.OnDismissListener onLowDismissListener = new TopDialogScale.OnDismissListener() { // 时间低参数对话框关闭监听器 //
        @Override // 重写注解 //
        public void onDismiss(String result) { // 对话框关闭回调 //
            onTextChanged(tvTimeLow, result, false); // 触发文本变更 //
        } // 方法结束 //
    }; // 监听器结束 //


    // ==================== 点击监听器区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 监听器名称: onClickListener
     * 监听器描述: 时间参数控件点击监听器
     *
     * 功能说明:
     * 处理时间参数控件的点击事件，弹出刻度对话框供用户调节参数
     *
     * 处理控件:
     * - tvTimeHigh: 时间高参数
     * - tvTimeLow: 时间低参数
     * - tvTime: 时间参数
     *
     * 处理流程:
     * 1. 播放按钮音效
     * 2. 获取控件位置
     * 3. 设置刻度对话框参数
     * 4. 显示对话框
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 点击监听器 //
        @Override // 重写注解 //
        public void onClick(View v) { // 点击回调 //
            PlaySound.getInstance().playButton(); // 播放按钮音效 //
            ScreenUtil.getViewLocation(v); // 获取控件位置 //
            switch (v.getId()) { // 根据控件ID分发 //
                case R.id.timeHighDetail: // 时间高参数控件 //
                    scaleDialog.setValue(context.getString(R.string.triggerlogic_timehigh), tvTimeHigh.getText().toString(), // 设置对话框参数 //
                            TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX, onHighDismissListener); // 设置对话框参数 //
                    break; // 跳出 //
                case R.id.timeLowDetail: // 时间低参数控件 //
                    scaleDialog.setValue(context.getString(R.string.triggerlogic_timelow), tvTimeLow.getText().toString(), // 设置对话框参数 //
                            TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX, onLowDismissListener); // 设置对话框参数 //
                    break; // 跳出 //
                case R.id.tvLogic: // 时间参数控件 //
                    scaleDialog.setValue(context.getString(R.string.triggerLogic), tvTime.getText().toString() // 设置对话框参数 //
                            , TopUtilScale.TIME_LOGIC_MIN, TopUtilScale.TIME_LOGIC_MAX, onDismissListener); // 设置对话框参数 //
                    break; // 跳出 //
            } // 控件ID判断结束 //
        } // 方法结束 //
    }; // 监听器结束 //


    // ==================== 单选组监听器区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 监听器名称: onSelectChangedListener
     * 监听器描述: 单选组选择变更监听器
     *
     * 功能说明:
     * 处理所有单选组控件的选择变更事件，包括：
     * - 通道1-8的逻辑状态选择
     * - 逻辑关系选择
     * - 触发条件选择
     *
     * 接口方法:
     * - onClickSound: 播放选择音效
     * - onPrompt: 提示回调（未使用）
     * - onClick: 选择变更回调
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private TopViewRadioGroup.OnCheckChangedListener onSelectChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组选择变更监听器 //
        @Override // 重写注解 //
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调 //
            PlaySound.getInstance().playButton(); // 播放按钮音效 //
        } // 方法结束 //

        @Override // 重写注解 //
        public void onPrompt(TopViewRadioGroup view) { // 提示回调 //
            // 未实现 //
        } // 方法结束 //

        @Override // 重写注解 //
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选择变更回调 //
            onSelectChanged(view, item, false); // 触发选择变更处理 //
        } // 方法结束 //
    }; // 监听器结束 //

    // ==================== 选择变更处理方法区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: onSelectChanged
     * 方法描述: 处理单选组选择变更事件
     *
     * @param view          发生变更的单选组控件
     * @param item          选中的选项
     * @param isFromEventBus 是否来自EventBus事件
     *
     * 功能说明:
     * 根据不同的控件ID处理选择变更：
     * - 通道1-8: 发送状态命令、更新缓存、同步到TriggerLogic
     * - 逻辑关系: 发送功能命令、更新缓存、同步到TriggerLogic
     * - 触发条件: 发送条件命令、更新缓存、同步到TriggerLogic、更新时间参数控件可见性
     *
     * 参数说明:
     * - isFromEventBus=true: 表示配置来自硬件同步，不需要再发送到硬件
     * - isFromEventBus=false: 表示配置来自用户操作，需要发送到硬件
     *
     * 时间参数控件可见性规则:
     * - 条件0(时间高): 显示时间高参数
     * - 条件1(时间低): 显示时间低参数
     * - 条件2(时间): 显示时间参数
     * - 条件3(组合): 显示时间高和时间低参数
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void onSelectChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 选择变更处理 //
        Tools.PrintControlsLocation("TopLayoutTriggerLogic", layout); // 打印控件位置日志 //

        // 处理通道1选择变更 //
        if (view.getId() == R.id.ch1) { // 如果是通道1 //
            Command.get().getTrigger_logic().Status(0, item.getIndex(), false); // 发送状态命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH1, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setLogicValids(0, TopMatchTrigger.triggerLogicChToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setCh1(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (view.getId() == R.id.ch2) { // 如果是通道2 //
            Command.get().getTrigger_logic().Status(1, item.getIndex(), false); // 发送状态命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH2, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setLogicValids(1, TopMatchTrigger.triggerLogicChToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setCh2(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (view.getId() == R.id.ch3) { // 如果是通道3 //
            Command.get().getTrigger_logic().Status(2, item.getIndex(), false); // 发送状态命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH3, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setLogicValids(2, TopMatchTrigger.triggerLogicChToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setCh3(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (view.getId() == R.id.ch4) { // 如果是通道4 //
            Command.get().getTrigger_logic().Status(3, item.getIndex(), false); // 发送状态命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH4, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setLogicValids(3, TopMatchTrigger.triggerLogicChToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setCh4(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (view.getId() == R.id.ch5) { // 如果是通道5 //
            Command.get().getTrigger_logic().Status(4, item.getIndex(), false); // 发送状态命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH5, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setLogicValids(4, TopMatchTrigger.triggerLogicChToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setCh5(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (view.getId() == R.id.ch6) { // 如果是通道6 //
            Command.get().getTrigger_logic().Status(5, item.getIndex(), false); // 发送状态命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH6, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setLogicValids(5, TopMatchTrigger.triggerLogicChToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setCh6(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (view.getId() == R.id.ch7) { // 如果是通道7 //
            Command.get().getTrigger_logic().Status(6, item.getIndex(), false); // 发送状态命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH7, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setLogicValids(6, TopMatchTrigger.triggerLogicChToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setCh7(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (view.getId() == R.id.ch8) { // 如果是通道8 //
            Command.get().getTrigger_logic().Status(7, item.getIndex(), false); // 发送状态命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH8, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setLogicValids(7, TopMatchTrigger.triggerLogicChToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setCh8(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (view.getId() == R.id.triggerLogic) { // 如果是逻辑关系 //
            Command.get().getTrigger_logic().Function(item.getIndex(), false); // 发送功能命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_LOGIC, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setLogic(TopMatchTrigger.triggerLogicRelationToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setTriggerLogic(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (view.getId() == R.id.condition) { // 如果是触发条件 //
            Command.get().getTrigger_logic().Condition(item.getIndex(), false); // 发送条件命令到硬件 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CONDITION, String.valueOf(item.getIndex())); // 保存到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setCondition(TopMatchTrigger.triggerConditionToScope(item.getIndex())); // 同步到TriggerLogic //
            } // 来源判断结束 //
            triggerDetail.setCondition(item); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //

            // 根据触发条件类型更新时间参数控件可见性 //
            switch (item.getIndex()) { // 条件判断 //
                case 0: // 时间高条件 //
                    tvTime.setVisibility(View.INVISIBLE); // 隐藏时间参数 //
                    layoutTimeHigh.setVisibility(View.VISIBLE); // 显示时间高参数 //
                    layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低参数 //
                    break; // 跳出 //
                case 1: // 时间低条件 //
                    tvTime.setVisibility(View.INVISIBLE); // 隐藏时间参数 //
                    layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高参数 //
                    layoutTimeLow.setVisibility(View.VISIBLE); // 显示时间低参数 //
                    break; // 跳出 //
                case 2: // 时间条件 //
                    tvTime.setVisibility(View.VISIBLE); // 显示时间参数 //
                    layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高参数 //
                    layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低参数 //
                    break; // 跳出 //
                case 3: // 组合条件 //
                    tvTime.setVisibility(View.INVISIBLE); // 隐藏时间参数 //
                    layoutTimeHigh.setVisibility(View.VISIBLE); // 显示时间高参数 //
                    layoutTimeLow.setVisibility(View.VISIBLE); // 显示时间低参数 //
                    break; // 跳出 //
                default: // 默认情况 //
                    tvTime.setVisibility(View.INVISIBLE); // 隐藏时间参数 //
                    layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏时间高参数 //
                    layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏时间低参数 //
                    break; // 跳出 //
            } // 条件判断结束 //

        } // 控件ID判断结束 //
    } // 方法结束 //

    // ==================== 文本变更处理方法区域 ==================== //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: onTextChanged (时间参数)
     * 方法描述: 处理时间参数文本变更事件
     *
     * @param result         新的时间值
     * @param isFromEventBus 是否来自EventBus事件
     *
     * 功能说明:
     * 当时间参数变更时：
     * 1. 发送时间命令到硬件
     * 2. 保存到缓存
     * 3. 同步到TriggerLogic对象
     * 4. 更新UI显示
     * 5. 更新数据模型
     * 6. 发送消息通知外部
     *
     * 参数说明:
     * - isFromEventBus=true: 表示配置来自硬件同步，不需要再发送到硬件
     * - isFromEventBus=false: 表示配置来自用户操作，需要发送到硬件
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void onTextChanged(String result, boolean isFromEventBus) { // 时间参数文本变更处理 //
        Command.get().getTrigger_logic().Time(TBookUtil.getSFromTime(result), false); // 发送时间命令到硬件 //
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_TVLOGIC, result); // 保存到缓存 //
        if (!isFromEventBus) { // 如果不是来自EventBus //
            triggerLogic.setLogicTime(TBookUtil.getPsFromTime(result) / 1000); // 同步到TriggerLogic //
        } // 来源判断结束 //
        tvTime.setText(result); // 更新UI显示 //
        triggerDetail.setLogic(result); // 更新数据模型 //
        sendMsg(isFromEventBus); // 发送消息通知外部 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: onTextChanged (时间高/低参数)
     * 方法描述: 处理时间高/低参数文本变更事件
     *
     * @param tv             文本框控件
     * @param result          新的时间值
     * @param isFromEventBus 是否来自EventBus事件
     *
     * 功能说明:
     * 当时间高/低参数变更时：
     * 1. 检查时间高/低参数的约束关系（时间高必须大于时间低）
     * 2. 如果违反约束，自动调整另一个参数
     * 3. 发送命令到硬件
     * 4. 保存到缓存
     * 5. 同步到TriggerLogic对象
     * 6. 更新UI显示
     * 7. 更新数据模型
     * 8. 发送消息通知外部
     *
     * 约束规则:
     * - 时间高参数必须大于时间低参数
     * - 如果时间高 < 时间低，自动将时间低调整为时间高
     * - 如果时间低 > 时间高，自动将时间高调整为时间低
     *
     * 参数说明:
     * - isFromEventBus=true: 表示配置来自硬件同步，不需要再发送到硬件
     * - isFromEventBus=false: 表示配置来自用户操作，需要发送到硬件
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) { // 时间高/低参数文本变更处理 //
        if (tv.getId() == tvTimeHigh.getId()) { // 如果是时间高参数 //
            long ht = TBookUtil.getPsFromTime(result) / 1000; // 转换时间高参数为纳秒 //
            long lt = TBookUtil.getPsFromTime(tvTimeLow.getText().toString()) / 1000; // 获取时间低参数 //
            if (ht < lt) { // 如果时间高小于时间低，违反约束 //
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_TIME_LOW, result); // 保存时间低到缓存 //
                if (!isFromEventBus) { // 如果不是来自EventBus //
                    triggerLogic.setTimeLow(ht); // 同步时间低到TriggerLogic //
                } // 来源判断结束 //
                tvTimeLow.setText(result); // 更新时间低UI //
                Command.get().getTrigger_logic().LTime(TBookUtil.getSFromTime(result), false); // 发送时间低命令 //
                triggerDetail.setTimeLowDetail(result); // 更新数据模型 //
            } // 约束判断结束 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_TIME_HIGH, result); // 保存时间高到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setTimeHigh(ht); // 同步时间高到TriggerLogic //
            } // 来源判断结束 //
            tvTimeHigh.setText(result); // 更新时间高UI //
            Command.get().getTrigger_logic().HTime(TBookUtil.getSFromTime(result), false); // 发送时间高命令 //
            triggerDetail.setTimeHighDetail(result); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } else if (tv.getId() == tvTimeLow.getId()) { // 如果是时间低参数 //
            long lt = TBookUtil.getPsFromTime(result) / 1000; // 转换时间低参数为纳秒 //
            long ht = TBookUtil.getPsFromTime(tvTimeHigh.getText().toString()) / 1000; // 获取时间高参数 //
            if (ht < lt) { // 如果时间高小于时间低，违反约束 //
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_TIME_HIGH, result); // 保存时间高到缓存 //
                if (!isFromEventBus) { // 如果不是来自EventBus //
                    triggerLogic.setTimeHigh(ht); // 同步时间高到TriggerLogic //
                } // 来源判断结束 //
                tvTimeHigh.setText(result); // 更新时间高UI //
                Command.get().getTrigger_logic().HTime(TBookUtil.getSFromTime(result), false); // 发送时间高命令 //
                triggerDetail.setTimeHighDetail(result); // 更新数据模型 //
            } // 约束判断结束 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_TIME_LOW, result); // 保存时间低到缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerLogic.setTimeLow(TBookUtil.getPsFromTime(result) / 1000); // 同步时间低到TriggerLogic //
            } // 来源判断结束 //
            tvTimeLow.setText(result); // 更新时间低UI //
            Command.get().getTrigger_logic().LTime(TBookUtil.getSFromTime(result), false); // 发送时间低命令 //
            triggerDetail.setTimeLowDetail(result); // 更新数据模型 //
            sendMsg(isFromEventBus); // 发送消息通知外部 //
        } // 控件ID判断结束 //
    } // 方法结束 //
} // 类结束 //