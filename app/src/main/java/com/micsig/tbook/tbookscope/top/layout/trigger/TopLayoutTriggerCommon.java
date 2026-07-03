package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import android.content.Context; // 上下文 //
import android.os.Bundle; // Fragment参数包 //
import android.util.Log; // 日志 //
import android.view.LayoutInflater; // 布局填充器 //
import android.view.View; // 视图基类 //
import android.view.ViewGroup; // 视图组 //
import android.widget.SeekBar; // 拖动条 //
import android.widget.TextView; // 文本视图 //

import androidx.annotation.Nullable; // 可空注解 //
import androidx.fragment.app.Fragment; // Fragment基类 //

import com.micsig.tbook.scope.Event.EventBase; // 事件基类 //
import com.micsig.tbook.scope.Event.EventFactory; // 事件工厂 //
import com.micsig.tbook.scope.Event.EventUIObserver; // 事件UI观察者 //
import com.micsig.tbook.scope.Trigger.TriggerCommon; // 常规触发器硬件数据 //
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 触发器工厂 //
import com.micsig.tbook.tbookscope.LoadCache; // 缓存加载消息 //
import com.micsig.tbook.tbookscope.MainActivity; // 主Activity //
import com.micsig.tbook.tbookscope.R; // 资源ID //
import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件 //
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 命令到UI消息 //
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel; // 右侧面板等级消息 //
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxBus事件总线 //
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // Rx枚举定义 //
import com.micsig.tbook.tbookscope.tools.PlaySound; // 播放声音工具 //
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 详情发送消息监听器 //
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogScale; // 刻度对话框 //
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具 //
import com.micsig.tbook.ui.top.view.TopViewSeekBar; // 自定义拖动条 //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean //
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 自定义单选按钮组 //
import com.micsig.tbook.ui.top.view.scale.TopUtilScale; // 刻度工具 //
import com.micsig.tbook.ui.util.TBookUtil; // 通用工具类 //

import io.reactivex.rxjava3.annotations.NonNull; // 非空注解 //
import io.reactivex.rxjava3.functions.Consumer; // RxJava消费者接口 //


/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   常规触发器配置Fragment                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopLayoutTriggerCommon                                             ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/4/10                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 提供常规触发器的抑制时间和触发模式配置界面                                  ║
 * ║  2. 监听EventBus事件和Command回调，同步硬件状态到UI                            ║
 * ║  3. 用户操作后通过Command中间件向硬件发送配置命令                              ║
 * ║  4. 通过RxBus与右侧面板、主菜单等模块通信                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                    ║
 * ║  用户操作 → UI变更 → Command → 硬件                                           ║
 * ║  硬件事件 → EventFactory → eventUIObserver → UI更新                           ║
 * ║  右侧面板 → RxBus → consumerRightLevel → UI更新                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: Fragment, RxBus, Command, EventFactory, TriggerFactory,           ║
 * ║            CacheUtil, TopDialogScale, TopViewRadioGroup, TopViewSeekBar      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopLayoutTriggerCommon extends Fragment { // 常规触发器配置Fragment //
    /** 上下文对象 */ // 上下文 //
    private Context context; // 上下文 //
    /** 抑制时间显示文本 */ // 抑制时间文本 //
    private TextView tvHoldOff; // 抑制时间显示 //
    /** 触发模式单选按钮组 */ // 触发模式 //
    private TopViewRadioGroup rgMode; // 触发模式单选组 //
    /** 刻度对话框 */ // 刻度对话框 //
    private TopDialogScale scaleDialog; // 刻度对话框 //

    /** 触发灵敏度拖动条 */ // 触发灵敏度 //
    private TopViewSeekBar triggerSensitivitySeekBar; // 触发灵敏度拖动条 //
    /** 常规触发器详情数据 */ // 触发器详情 //
    private TopMsgTriggerCommon triggerDetail; // 触发器详情数据 //
    /** 详情发送消息监听器 */ // 发送消息监听 //
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情发送消息监听器 //

    /** 常规触发器硬件数据 */ // 硬件数据 //
    private TriggerCommon triggerCommon; // 常规触发器硬件数据 //
    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器
     * @param savedInstanceState 保存的实例状态
     * @return View 填充后的视图
     */
    @Nullable // 可空注解 //
    @Override // 重写Fragment方法 //
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图 //
        return inflater.inflate(R.layout.layout_triggercommon, container, false); // 填充常规触发器布局 //
    } // 方法结束 //

    /**
     * 视图创建完成后的初始化
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 重写Fragment方法 //
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成 //
        this.context = getActivity(); // 获取Activity上下文 //
        initView(view); // 初始化视图控件 //
        initData(); // 初始化数据 //
        initControl(); // 初始化事件监听 //
    } // 方法结束 //

    /**
     * 初始化视图控件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图 //
        tvHoldOff = (TextView) view.findViewById(R.id.holdoffTimeValue); // 获取抑制时间文本 //
        tvHoldOff.setOnClickListener(onClickListener); // 设置点击监听 //
        rgMode = (TopViewRadioGroup) view.findViewById(R.id.mode); // 获取触发模式单选组 //
        rgMode.setData(R.string.triggerMode, R.array.triggerMode, onSelectListener); // 设置模式数据 //
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale); // 获取刻度对话框 //
        triggerSensitivitySeekBar = view.findViewById(R.id.triggerSensitivity); // 获取灵敏度拖动条 //
        triggerSensitivitySeekBar.setTriggerSeekBar(true); // 设置为触发器拖动条模式 //
        triggerSensitivitySeekBar.setData(R.string.TriggerSensitivity,90,45,seekBarChangeListener); // 设置灵敏度数据 //
        triggerCommon = TriggerFactory.getInstance().getTriggerCommon(); // 获取常规触发器硬件数据 //

    } // 方法结束 //

    /**
     * 初始化数据
     */
    private void initData() { // 初始化数据 //
        triggerDetail = new TopMsgTriggerCommon(); // 创建触发器详情对象 //
        triggerDetail.setHoldoffTime(tvHoldOff.getText().toString()); // 设置抑制时间 //
        triggerDetail.setMode(rgMode.getSelected()); // 设置触发模式 //
    } // 方法结束 //

    /**
     * 初始化事件监听，订阅RxBus和EventFactory
     */
    private void initControl() { // 初始化事件监听 //
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件 //
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel); // 订阅右侧面板等级事件 //
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI事件 //
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_AUTO).subscribe(consumerMainLeftMenuAuto); // 订阅自动菜单事件 //
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_COMMON_MODE, eventUIObserver); // 监听触发模式事件 //
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_COMMON_HOLDOFFTIME, eventUIObserver); // 监听抑制时间事件 //
    } // 方法结束 //

    /**
     * 从缓存恢复触发器配置
     */
    private void setCache() { // 从缓存恢复配置 //
        String time = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_COMMON_TIME); // 读取抑制时间缓存 //
        int mode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE); // 读取触发模式缓存 //
        int progress = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SENSITIVITY); // 读取灵敏度缓存 //
        triggerSensitivitySeekBar.setProgress(progress); // 设置灵敏度进度 //
        double triggerSensitivity = 0.9-(progress/90.0) *0.9; // 计算灵敏度值 //
        triggerSensitivity += 0.1; // 加上基础灵敏度 //
        triggerCommon.setTriggerSensitivity(triggerSensitivity); // 设置硬件灵敏度 //
        tvHoldOff.setText(time); // 显示抑制时间 //
        this.rgMode.setSelectedIndex(mode); // 设置模式选中项 //
        triggerDetail.setHoldoffTime(tvHoldOff.getText().toString()); // 更新详情抑制时间 //
        triggerDetail.setMode(this.rgMode.getSelected()); // 更新详情触发模式 //

        Command.get().getTrigger().HoldOff(TBookUtil.getSFromTime(time), false); // 发送抑制时间命令 //
        Command.get().getTrigger().Mode(mode, false); // 发送触发模式命令 //

        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon(); // 获取硬件数据 //
        triggerCommon.setTriggerHoldOffTime(TBookUtil.get_nsFromTime(time)); // 设置硬件抑制时间 //
        triggerCommon.setTriggerMode(mode); // 设置硬件触发模式 //
    } // 方法结束 //

    /**
     * 获取触发器详情数据
     * @return TopMsgTriggerCommon 触发器详情
     */
    public TopMsgTriggerCommon getTriggerDetail() { // 获取触发器详情 //
        return triggerDetail; // 返回详情数据 //
    } // 方法结束 //

    /**
     * 设置详情发送消息监听器
     * @param onDetailSendMsgListener 消息监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息监听器 //
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 保存监听器 //
    } // 方法结束 //

    /**
     * 发送触发器配置变更消息
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) { // 发送变更消息 //
        if (onDetailSendMsgListener != null) { // 如果监听器不为空 //
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 回调监听器 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 检查时间值是否在合法范围内
     * @param ns 纳秒值
     * @return long 修正后的纳秒值
     */
    private long checkTime(long ns) { // 检查时间范围 //
        long timeMin = TopUtilScale.TIME_COMMON_MIN; // 获取最小时间 //
        long timeMax = TopUtilScale.TIME_COMMON_MAX; // 获取最大时间 //
        ns = ns < timeMin ? timeMin : ns; // 不小于最小值 //
        ns = ns > timeMax ? timeMax : ns; // 不大于最大值 //
        return ns; // 返回修正后的值 //
    } // 方法结束 //

    /** 缓存加载消费者，从缓存恢复配置 */ // 缓存加载消费者 //
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者 //
        @Override // 重写accept方法 //
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件 //
            setCache(); // 从缓存恢复配置 //
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerCommon, true); // 标记已加载 //
        } // 方法结束 //
    }; // 消费者结束 //

    /** 右侧面板等级消费者，同步右侧面板模式选择 */ // 右侧面板消费者 //
    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() { // 右侧面板消费者 //
        @Override // 重写accept方法 //
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception { // 接收右侧面板事件 //
            if (msgLevel.getMiddleSelect() != rgMode.getSelected().getIndex()) { // 如果模式索引不同 //
                rgMode.setSelectedIndex(msgLevel.getMiddleSelect()); // 设置模式选中项 //
                onCheckChanged(rgMode, rgMode.getSelected(), msgLevel.isFromEventBus()); // 触发模式变更 //
            } // 判断结束 //
        } // 方法结束 //
    }; // 消费者结束 //

    /** 命令到UI消费者，处理硬件返回的配置变更 */ // 命令到UI消费者 //
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令到UI消费者 //
        @Override // 重写accept方法 //
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令到UI事件 //
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发 //
                case CommandMsgToUI.FLAG_TRIGGER_HOLDOFF: // 抑制时间变更 //
                    double checkBefore = Double.parseDouble(commandMsgToUI.getParam()); // 解析参数 //
                    long checkTime = TopUtilScale.checkTime((long) (checkBefore * TopUtilScale.TIME_S2NS) // 检查时间范围 //
                            , TopUtilScale.TIME_COMMON_MIN, TopUtilScale.TIME_COMMON_MAX); // 传入最小最大值 //
                    String time = TBookUtil.getTimeFromS(checkTime * 1.0 / TopUtilScale.TIME_S2NS); // 转换为时间字符串 //
//                    if (!tvHoldOff.getText().toString().equals(time)) { // 被注释掉的判断 //
                        onTextChanged(tvHoldOff, time, false); // 触发文本变更 //
//                    } // 被注释掉的判断结束 //
                    break; // 结束抑制时间分支 //
                case CommandMsgToUI.FLAG_TRIGGER_MODE: // 触发模式变更 //
                    if (rgMode.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 如果模式不同 //
                        rgMode.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 设置模式选中项 //
                        onCheckChanged(rgMode, rgMode.getSelected(), false); // 触发模式变更 //
                    } // 判断结束 //
                    break; // 结束触发模式分支 //
            } // switch结束 //
        } // 方法结束 //
    }; // 消费者结束 //
    /** 灵敏度拖动条变更监听器 */ // 灵敏度拖动条监听 //
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() { // 灵敏度拖动条监听 //
        @Override // 重写进度变更方法 //
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { // 进度变更回调 // //波形亮度调节
            double triggerSensitivity = 0.9-(progress/90.0) *0.9; // 计算灵敏度值 //
            triggerSensitivity += 0.1; // 加上基础灵敏度 //
            Log.d("TAG", "onProgressChanged: " + triggerSensitivity); // 打印日志 //
            triggerCommon.setTriggerSensitivity(triggerSensitivity); // 设置硬件灵敏度 //
            sendMsg(false); // 发送变更消息 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SENSITIVITY, String.valueOf(progress)); // 保存灵敏度缓存 //
        } // 方法结束 //

        @Override // 重写开始拖动方法 //
        public void onStartTrackingTouch(SeekBar seekBar) { // 开始拖动回调 //

        } // 方法结束 //

        @Override // 重写停止拖动方法 //
        public void onStopTrackingTouch(SeekBar seekBar) { // 停止拖动回调 //

        } // 方法结束 //
    }; // 监听器结束 //

    /** 自动菜单消费者，自动设置时切换到自动模式 */ // 自动菜单消费者 //
    private Consumer<Boolean> consumerMainLeftMenuAuto = new Consumer<Boolean>() { // 自动菜单消费者 //
        @Override // 重写accept方法 //
        public void accept(Boolean aBoolean) throws Exception { // 接收自动菜单事件 //
            if (aBoolean) { // 如果启用自动 //
                if (rgMode.getSelected().getIndex() != 0) { // 如果当前不是自动模式 //
                    rgMode.setSelectedIndex(0); // 切换到自动模式 //
                    onCheckChanged(rgMode, rgMode.getSelected(), true); // 触发模式变更 //
                } // 判断结束 //
            } // 判断结束 //
        } // 方法结束 //
    }; // 消费者结束 //

    /** 刻度对话框关闭监听器 */ // 对话框关闭监听 //
    private TopDialogScale.OnDismissListener onDismissListener = new TopDialogScale.OnDismissListener() { // 对话框关闭监听 //
        @Override // 重写onDismiss方法 //
        public void onDismiss(String result) { // 对话框关闭回调 //
            onTextChanged(tvHoldOff, result, false); // 更新抑制时间 //
        } // 方法结束 //
    }; // 监听器结束 //

    /** 抑制时间点击监听器 */ // 点击监听器 //
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 点击监听器 //
        @Override // 重写onClick方法 //
        public void onClick(View v) { // 点击回调 //
            PlaySound.getInstance().playButton(); // 播放按钮音效 //
            scaleDialog.setValue(context.getString(R.string.view_trigger_inhibitiontimetitle), tvHoldOff.getText().toString() // 设置对话框标题和当前值 //
                    , TopUtilScale.TIME_COMMON_MIN, TopUtilScale.TIME_COMMON_MAX, onDismissListener); // 设置时间范围和关闭监听 //
        } // 方法结束 //
    }; // 监听器结束 //


    /** 触发模式单选监听器 */ // 模式选择监听 //
    private TopViewRadioGroup.OnCheckChangedListener onSelectListener = new TopViewRadioGroup.OnCheckChangedListener() { // 模式选择监听 //
        @Override // 重写音效回调 //
        public void onClickSound(boolean isCheckedSuccess) { // 选中音效回调 //
            PlaySound.getInstance().playButton(); // 播放按钮音效 //
        } // 方法结束 //

        @Override // 重写提示回调 //
        public void onPrompt(TopViewRadioGroup view) { // 提示回调 //

        } // 方法结束 //

        @Override // 重写点击回调 //
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中项变更回调 //
            onCheckChanged(view, item, false); // 触发模式变更 //
        } // 方法结束 //
    }; // 监听器结束 //

    /**
     * 触发模式变更处理
     * @param view 单选按钮组
     * @param item 选中的项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 模式变更处理 //
        if (view.getId() == rgMode.getId()) { // 如果是触发模式组 //
            Command.get().getTrigger().Mode(item.getIndex(), false); // 发送模式命令 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE, String.valueOf(item.getIndex())); // 保存模式缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                TriggerFactory.getInstance().getTriggerCommon().setTriggerMode(item.getIndex()); // 更新硬件数据 //
            } // 判断结束 //
            triggerDetail.setMode(item); // 更新详情模式 //
            sendMsg(isFromEventBus); // 发送变更消息 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 文本变更处理（抑制时间）
     * @param textView 文本视图
     * @param result 新文本值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onTextChanged(TextView textView, String result, boolean isFromEventBus) { // 文本变更处理 //
        if (textView.getId() == tvHoldOff.getId()) { // 如果是抑制时间文本 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_COMMON_TIME, result); // 保存抑制时间缓存 //
            Command.get().getTrigger().HoldOff(TBookUtil.getSFromTime(result), false); // 发送抑制时间命令 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                TriggerFactory.getInstance().getTriggerCommon().setTriggerHoldOffTime(TBookUtil.get_nsFromTime(result)); // 更新硬件数据 //
            } // 判断结束 //
            tvHoldOff.setText(result); // 更新显示文本 //
            triggerDetail.setHoldoffTime(result); // 更新详情抑制时间 //
            sendMsg(isFromEventBus); // 发送变更消息 //
        } // 判断结束 //
    } // 方法结束 //

    /** 事件UI观察者，监听硬件触发器配置变更 */ // 事件UI观察者 //
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 事件UI观察者 //
        @Override // 重写update方法 //
        public void update(Object data) { // 事件更新回调 //
            TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon(); // 获取硬件数据 //
            if (((EventBase) data).getId() == EventFactory.EVENT_TRIGGER_COMMON_MODE) { // 如果是触发模式事件 //
                if (rgMode.getSelected().getIndex() != triggerCommon.getTriggerMode()) { // 如果模式不同 //
                    rgMode.setSelectedIndex(triggerCommon.getTriggerMode()); // 设置模式选中项 //
                    onCheckChanged(rgMode, rgMode.getSelected(), true); // 触发模式变更 //
                } // 判断结束 //
            } else if (((EventBase) data).getId() == EventFactory.EVENT_TRIGGER_COMMON_HOLDOFFTIME) { // 如果是抑制时间事件 //
                long checkTime = TopUtilScale.checkTime(triggerCommon.getTriggerHoldOffTime() // 检查时间范围 //
                        , TopUtilScale.TIME_COMMON_MIN, TopUtilScale.TIME_COMMON_MAX); // 传入最小最大值 //
                String time = TBookUtil.getTime3FromPs(checkTime * 1000 * 10); // 转换为时间字符串 //
                if (!tvHoldOff.getText().toString().equals(time)) { // 如果时间不同 //
                    tvHoldOff.setText(time); // 更新显示文本 //
                    onTextChanged(tvHoldOff, tvHoldOff.getText().toString(), true); // 触发文本变更 //
                } // 判断结束 //
            } // 判断结束 //
        } // 方法结束 //
    }; // 观察者结束 //

} // 类结束 //
