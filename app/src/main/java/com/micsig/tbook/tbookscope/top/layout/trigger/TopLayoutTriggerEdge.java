package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import android.content.Context; // 上下文 //
import android.os.Bundle; // Fragment参数包 //
import android.view.LayoutInflater; // 布局填充器 //
import android.view.View; // 视图基类 //
import android.view.ViewGroup; // 视图组 //

import androidx.annotation.Nullable; // 可空注解 //
import androidx.fragment.app.Fragment; // Fragment基类 //

import com.micsig.base.Logger; // 日志工具 //
import com.micsig.tbook.scope.Event.EventBase; // 事件基类 //
import com.micsig.tbook.scope.Event.EventFactory; // 事件工厂 //
import com.micsig.tbook.scope.Event.EventUIObserver; // 事件UI观察者 //
import com.micsig.tbook.scope.Sample.Sample; // 采样硬件数据 //
import com.micsig.tbook.scope.Trigger.Trigger; // 触发器基类 //
import com.micsig.tbook.scope.Trigger.TriggerEdge; // 边沿触发器硬件数据 //
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 触发器工厂 //
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量 //
import com.micsig.tbook.tbookscope.LoadCache; // 缓存加载消息 //
import com.micsig.tbook.tbookscope.R; // 资源ID //
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel; // 触发电平持有者 //
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel; // 触发电平消息 //
import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件 //
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 命令到UI消息 //
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel; // 右侧面板等级消息 //
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxBus事件总线 //
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // Rx枚举定义 //
import com.micsig.tbook.tbookscope.tools.PlaySound; // 播放声音工具 //
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具 //
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 详情发送消息监听器 //
import com.micsig.tbook.tbookscope.util.App; // 应用工具 //
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具 //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean //
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 自定义单选按钮组 //
import com.micsig.tbook.ui.util.StrUtil; // 字符串工具 //
import com.micsig.tbook.ui.wavezone.TChan; // 通道映射工具 //

import io.reactivex.rxjava3.annotations.NonNull; // 非空注解 //
import io.reactivex.rxjava3.functions.Consumer; // RxJava消费者接口 //


/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   边沿触发器配置Fragment                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopLayoutTriggerEdge                                               ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/4/10                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 提供边沿触发器的触发源/边沿类型/耦合方式配置界面                            ║
 * ║  2. 监听EventBus事件和Command回调，同步硬件状态到UI                            ║
 * ║  3. 用户操作后通过Command中间件向硬件发送配置命令                              ║
 * ║  4. 通过RxBus与右侧面板、触发电平等模块通信                                   ║
 * ║  5. 处理外部触发源切换和同步状态                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                    ║
 * ║  用户操作 → UI变更 → Command → 硬件                                           ║
 * ║  硬件事件 → EventFactory → eventUITriggerParam → UI更新                      ║
 * ║  右侧面板 → RxBus → consumerRightLevel → UI更新                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: Fragment, RxBus, Command, EventFactory, TriggerFactory,           ║
 * ║            CacheUtil, TopViewRadioGroup, TChan, Sample                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopLayoutTriggerEdge extends Fragment { // 边沿触发器配置Fragment //
    /** 上下文对象 */ // 上下文 //
    private Context context; // 上下文 //
    /** 根布局 */ // 根布局 //
    private ViewGroup layout; // 根布局 //
    /** 触发源单选按钮组 */ // 触发源 //
    private TopViewRadioGroup rgSource; // 触发源单选组 //
    /** 触发边沿单选按钮组 */ // 触发边沿 //
    private TopViewRadioGroup rgEdge; // 触发边沿单选组 //
    /** 触发耦合单选按钮组 */ // 触发耦合 //
    private TopViewRadioGroup rgCouple; // 触发耦合单选组 //

    /** 边沿触发器详情数据 */ // 触发器详情 //
    private TopMsgTriggerEdge msgTriggerDetail; // 边沿触发器详情 //
    /** 详情发送消息监听器 */ // 发送消息监听 //
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情发送消息监听器 //

    /** 边沿触发器硬件数据 */ // 硬件数据 //
    private TriggerEdge triggerEdge; // 边沿触发器硬件数据 //
    /** 通道名称数组（含外部触发源） */ // 通道名称 //
    private String[] channels; // 通道名称数组 //

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
        return inflater.inflate(R.layout.layout_triggeredge, container, false); // 填充边沿触发器布局 //
    } // 方法结束 //

    /**
     * 视图创建完成后的初始化
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 重写Fragment方法 //
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成 //
        this.context = getActivity(); // 获取Activity上下文 //
        layout = (ViewGroup) view; // 保存根布局 //
        initView(view); // 初始化视图控件 //
        initData(); // 初始化数据 //
        initControl(); // 初始化事件监听 //
    } // 方法结束 //

    /**
     * 初始化视图控件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图 //
        rgSource = (TopViewRadioGroup) view.findViewById(R.id.triggerSource); // 获取触发源单选组 //
        String[] channels1 = GlobalVar.get().getChannelsName(); // 获取通道名称 //
        String[] channels2 = context.getResources().getStringArray(com.micsig.tbook.ui.R.array.edgeExternalTrigger); // 获取外部触发源名称 //
        channels = StrUtil.add(channels1, channels2); // 合并通道和外部触发源 //
        rgSource.setData(context.getResources().getString(R.string.triggerSource), channels, onSelectListener); // 设置触发源数据 //
        rgEdge = (TopViewRadioGroup) view.findViewById(R.id.triggerEdge); // 获取触发边沿单选组 //
        rgEdge.setData(R.string.triggerEdge, R.array.triggerEdge, onSelectListener); // 设置边沿数据 //
        rgCouple = (TopViewRadioGroup) view.findViewById(R.id.triggerCouple); // 获取触发耦合单选组 //
        rgCouple.setData(R.string.triggerCouple, R.array.triggerCouple, onSelectListener); // 设置耦合数据 //

        triggerEdge = (TriggerEdge) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_EDGE); // 获取边沿触发器硬件数据 //
    } // 方法结束 //

    /**
     * 初始化数据
     */
    private void initData() { // 初始化数据 //
        msgTriggerDetail = new TopMsgTriggerEdge(); // 创建边沿触发器详情对象 //
        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 设置触发源 //
        msgTriggerDetail.setTriggerEdge(rgEdge.getSelected()); // 设置触发边沿 //
        msgTriggerDetail.setTriggerCouple(rgCouple.getSelected()); // 设置触发耦合 //
    } // 方法结束 //

    /**
     * 初始化事件监听，订阅RxBus和EventFactory
     */
    private void initControl() { // 初始化事件监听 //
        RxBus.getInstance().getObservable(RxEnum.TOPTRIGGER_CHANNEL).subscribe(consumerTriggerChannel); // 订阅触发器通道事件 //
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件 //
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel); // 订阅右侧面板等级事件 //
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL).subscribe(consumerTriggerLevel); // 订阅触发电平通道事件 //
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI事件 //
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_EDGE).subscribe(consumerExternalkeysEdge); // 订阅外部按键边沿事件 //
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_PARAM, eventUITriggerParam); // 监听触发参数事件 //
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE).subscribe(consumerSyncExternalTriggerState); // 订阅外部触发状态同步事件 //
    } // 方法结束 //

    /**
     * 从缓存恢复边沿触发器配置
     */
    private void setCache() { // 从缓存恢复配置 //
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE); // 读取触发源缓存 //
        int edge = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_EDGE); // 读取触发边沿缓存 //
        int couple = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_COUPLE); // 读取触发耦合缓存 //

        triggerEdge.setTriggerSource(source); // 设置硬件触发源 //
        SyncExTriggerState(source); // 同步外部触发状态 //
        triggerEdge.setTriggerEdge(edge); // 设置硬件触发边沿 //
        triggerEdge.setTriggerCouple(couple); // 设置硬件触发耦合 //

        rgSource.setSelectedIndex(source); // 设置UI触发源选中项 //
        rgEdge.setSelectedIndex(edge); // 设置UI触发边沿选中项 //
        rgCouple.setSelectedIndex(couple); // 设置UI触发耦合选中项 //

        source=TChan.ChToExtChOfScpi(source); // 转换为SCPI外部触发通道索引 //
        Command.get().getTrigger_edge().Source(source, false); // 发送触发源命令 //
        Command.get().getTrigger_edge().Slope(edge, false); // 发送触发边沿命令 //
        Command.get().getTrigger_edge().Couple(couple, false); // 发送触发耦合命令 //

        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 更新详情触发源 //
        msgTriggerDetail.setTriggerEdge(rgEdge.getSelected()); // 更新详情触发边沿 //
        msgTriggerDetail.setTriggerCouple(rgCouple.getSelected()); // 更新详情触发耦合 //
        sendMsg(false); // 发送变更消息 //
    } // 方法结束 //

    /**
     * 同步外部触发状态
     * @param source 触发源索引
     */
    private void SyncExTriggerState(int source) { // 同步外部触发状态 //
        Sample.getInstance().setTriggerInOut(source == channels.length - 1); // 设置采样模块的外部触发标志 //
//        Logger.d("同步触发状态 edge=" + (source == channels.length - 1)); // 被注释掉的日志 //
        RxBus.getInstance().post(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE, source == channels.length - 1);//同步外部触发状态 // // 发送外部触发状态事件 //
    } // 方法结束 //

    /**
     * 获取边沿触发器详情数据
     * @return TopMsgTriggerEdge 边沿触发器详情
     */
    public TopMsgTriggerEdge getMsgTriggerDetail() { // 获取触发器详情 //
        return msgTriggerDetail; // 返回详情数据 //
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

    /** 外部按键边沿消费者，物理按键切换边沿类型 */ // 外部按键消费者 //
    private Consumer<Boolean> consumerExternalkeysEdge = new Consumer<Boolean>() { // 外部按键消费者 //
        @Override // 重写accept方法 //
        public void accept(Boolean aBoolean) throws Exception { // 接收外部按键事件 //
            if(TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_EDGE){ // 如果当前是边沿触发器 //
                int idx = rgEdge.getSelected().getIndex() + 1; // 获取下一个边沿索引 //
                if(idx >= rgEdge.getArray().length){ // 如果超出范围 //
                    idx = 0; // 回到第一个 //
                } // 判断结束 //
                rgEdge.setSelectedIndex(idx); // 设置边沿选中项 //
                onSelectChanged(rgEdge, rgEdge.getSelected(), false); // 触发边沿变更 //
            } // 判断结束 //
        } // 方法结束 //
    }; // 消费者结束 //

    /** 触发器通道消费者，同步其他触发器的通道选择 */ // 触发器通道消费者 //
    private Consumer<TopMsgTriggerChannel> consumerTriggerChannel = new Consumer<TopMsgTriggerChannel>() { // 触发器通道消费者 //
        @Override // 重写accept方法 //
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception { // 接收通道变更事件 //
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_EDGE) { // 如果不是边沿触发器发出的 //
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) { // 如果通道不同 //
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber()); // 设置触发源选中项 //
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber())); // 保存触发源缓存 //
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 更新详情触发源 //
                    int idx=TChan.ChToExtChOfScpi(rgSource.getSelected().getIndex()); // 转换为SCPI通道索引 //
                    Command.get().getTrigger_edge().Source(idx,false); // 发送触发源命令 //
                } // 判断结束 //
            } // 判断结束 //
        } // 方法结束 //
    }; // 消费者结束 //

    /** 缓存加载消费者 */ // 缓存加载消费者 //
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者 //
        @Override // 重写accept方法 //
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件 //
            setCache(); // 从缓存恢复配置 //
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerEdge, true); // 标记已加载 //
        } // 方法结束 //
    }; // 消费者结束 //

    /** 右侧面板等级消费者 */ // 右侧面板消费者 //
    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() { // 右侧面板消费者 //
        @Override // 重写accept方法 //
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception { // 接收右侧面板事件 //
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发器类型 //
            if (triggerIndex == TopLayoutTrigger.DETAIL_EDGE) { // 如果是边沿触发器 //
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) { // 如果触发源不同 //
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect()); // 设置触发源选中项 //
                    onSelectChanged(rgSource, rgSource.getSelected(), msgLevel.isFromEventBus()); // 触发源变更 //
                } // 判断结束 //
                if (msgLevel.getTopSelect() != rgEdge.getSelected().getIndex()) { // 如果触发边沿不同 //
                    rgEdge.setSelectedIndex(msgLevel.getTopSelect()); // 设置触发边沿选中项 //
                    onSelectChanged(rgEdge, rgEdge.getSelected(), msgLevel.isFromEventBus()); // 触发边沿变更 //
                } // 判断结束 //
            } // 判断结束 //
        } // 方法结束 //
    }; // 消费者结束 //

    /** 触发电平消费者，同步触发电平的通道选择 */ // 触发电平消费者 //
    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() { // 触发电平消费者 //
        @Override // 重写accept方法 //
        public void accept(@NonNull MainMsgTriggerLevel msgTriggerLevel) throws Exception { // 接收触发电平事件 //
            if (msgTriggerLevel.isOnlyModifyNumber()) return; // 如果仅修改数值则忽略 //
            if (MainHolderTriggerLevel.LEVEL_TRIGGER_EDGE.equals(msgTriggerLevel.getCurLevel())) { // 如果是边沿触发电平 //
                if (rgSource.getSelected().getIndex() != msgTriggerLevel.getCurCh() - 1) { // 如果通道不同 //
                    rgSource.setSelectedIndex(msgTriggerLevel.getCurCh() - 1); // 设置触发源选中项 //
                    onSelectChanged(rgSource, rgSource.getSelected(), msgTriggerLevel.isFromEventBus()); // 触发源变更 //
                } // 判断结束 //
            } // 判断结束 //
        } // 方法结束 //
    }; // 消费者结束 //

    /** 命令到UI消费者，处理硬件返回的配置变更 */ // 命令到UI消费者 //
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令到UI消费者 //
        @Override // 重写accept方法 //
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令到UI事件 //
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发 //
                case CommandMsgToUI.FLAG_TRIGGEREDGE_SOURCE: // 触发源变更 //
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 如果触发源不同 //
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 设置触发源选中项 //
                        onSelectChanged(rgSource, rgSource.getSelected(), false); // 触发源变更 //
                    } // 判断结束 //
                    break; // 结束触发源分支 //
                case CommandMsgToUI.FLAG_TRIGGEREDGE_SLOPE: // 触发边沿变更 //
//                    if (rgEdge.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 被注释掉的判断 //
                        rgEdge.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 设置触发边沿选中项 //
                        onSelectChanged(rgEdge, rgEdge.getSelected(), false); // 触发边沿变更 //
//                    } // 被注释掉的判断结束 //
                    break; // 结束触发边沿分支 //
                case CommandMsgToUI.FLAG_TRIGGEREDGE_COUPLE: // 触发耦合变更 //
                    if (rgCouple.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 如果触发耦合不同 //
                        rgCouple.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 设置触发耦合选中项 //
                        onSelectChanged(rgCouple, rgCouple.getSelected(), false); // 触发耦合变更 //
                    } // 判断结束 //
                    break; // 结束触发耦合分支 //
            } // switch结束 //
        } // 方法结束 //
    }; // 消费者结束 //

    /** 触发参数事件UI观察者，监听硬件触发参数变更 */ // 触发参数观察者 //
    private EventUIObserver eventUITriggerParam = new EventUIObserver() { // 触发参数观察者 //
        @Override // 重写update方法 //
        public void update(Object data) { // 事件更新回调 //
            EventBase eventBase = (EventBase) data; // 获取事件基类 //
            if (eventBase.getId() == EventFactory.EVENT_TRIGGER_PARAM) { // 如果是触发参数事件 //
                int source = triggerEdge.getTriggerSource(); // 获取硬件触发源 //
                int edge = triggerEdge.getTriggerEdge(); // 获取硬件触发边沿 //
                int couple = triggerEdge.getTriggerCouple(); // 获取硬件触发耦合 //

                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE) != source) { // 如果触发源与缓存不同 //
                    rgSource.setSelectedIndex(source); // 设置触发源选中项 //
                    onSelectChanged(rgSource, rgSource.getSelected(), true); // 触发源变更 //
                } // 判断结束 //
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_EDGE) != edge) { // 如果触发边沿与缓存不同 //
                    rgEdge.setSelectedIndex(edge); // 设置触发边沿选中项 //
                    onSelectChanged(rgEdge, rgEdge.getSelected(), true); // 触发边沿变更 //
                } // 判断结束 //
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_COUPLE) != couple) { // 如果触发耦合与缓存不同 //
                    rgCouple.setSelectedIndex(couple); // 设置触发耦合选中项 //
                    onSelectChanged(rgCouple, rgCouple.getSelected(), true); // 触发耦合变更 //
                } // 判断结束 //
            } // 判断结束 //
        } // 方法结束 //
    }; // 观察者结束 //

    /** 单选按钮变更监听器 */ // 单选按钮监听 //
    private TopViewRadioGroup.OnCheckChangedListener onSelectListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选按钮监听 //
        @Override // 重写音效回调 //
        public void onClickSound(boolean isCheckedSuccess) { // 选中音效回调 //
            PlaySound.getInstance().playButton(); // 播放按钮音效 //
        } // 方法结束 //

        @Override // 重写提示回调 //
        public void onPrompt(TopViewRadioGroup view) { // 提示回调 //

        } // 方法结束 //

        @Override // 重写点击回调 //
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中项变更回调 //
            onSelectChanged(view, item, false); // 触发选中项变更 //
        } // 方法结束 //
    }; // 监听器结束 //

    /**
     * 选中项变更处理
     * @param view 单选按钮组
     * @param item 选中的项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onSelectChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 选中项变更处理 //
        Tools.PrintControlsLocation("TopLayoutTriggerEdge", layout); // 打印控件位置 //
        if (view.getId() == R.id.triggerSource) { // 如果是触发源 //
            int index=TChan.ChToExtChOfScpi(item.getIndex()); // 转换为SCPI通道索引 //
            Command.get().getTrigger_edge().Source(index, false); // 发送触发源命令 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex())); // 保存触发源缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerEdge.setTriggerSource(item.getIndex()); // 更新硬件数据 //
                SyncExTriggerState(item.getIndex()); // 同步外部触发状态 //
            } // 判断结束 //
            msgTriggerDetail.setTriggerSource(item); // 更新详情触发源 //
            sendMsg(isFromEventBus); // 发送变更消息 //
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL, // 发送通道变更事件 //
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_EDGE, item.getIndex())); // 创建通道变更消息 //
        } else if (view.getId() == R.id.triggerEdge) { // 如果是触发边沿 //
            Command.get().getTrigger_edge().Slope(item.getIndex(), false); // 发送触发边沿命令 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_EDGE_EDGE, String.valueOf(item.getIndex())); // 保存触发边沿缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerEdge.setTriggerEdge(item.getIndex()); // 更新硬件数据 //
            } // 判断结束 //
            msgTriggerDetail.setTriggerEdge(item); // 更新详情触发边沿 //
            sendMsg(isFromEventBus); // 发送变更消息 //
        } else if (view.getId() == R.id.triggerCouple) { // 如果是触发耦合 //
            Command.get().getTrigger_edge().Couple(item.getIndex(), false); // 发送触发耦合命令 //
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_EDGE_COUPLE, String.valueOf(item.getIndex())); // 保存触发耦合缓存 //
            if (!isFromEventBus) { // 如果不是来自EventBus //
                triggerEdge.setTriggerCouple(item.getIndex()); // 更新硬件数据 //
            } // 判断结束 //
            msgTriggerDetail.setTriggerCouple(item); // 更新详情触发耦合 //
            sendMsg(isFromEventBus); // 发送变更消息 //
        } // 判断结束 //
    } // 方法结束 //

    /** 外部触发状态同步消费者 */ // 外部触发状态消费者 //
    private Consumer<Boolean> consumerSyncExternalTriggerState = new Consumer<Boolean>() { // 外部触发状态消费者 //
        @Override // 重写accept方法 //
        public void accept(Boolean aBoolean) throws Throwable { // 接收外部触发状态事件 //
            int newIndex = aBoolean ? channels.length - 1 : triggerEdge.getPreTriggerSource(); // 计算新的触发源索引 //
            int source = rgSource.getSelected().getIndex(); // 获取当前触发源 //
            rgCouple.setEnabled(newIndex != channels.length - 1);//外部触发时触发耦合置灰 // // 外部触发时禁用耦合 //
            if(source == newIndex) return; // 如果触发源相同则忽略 //
            rgSource.setSelectedIndex(newIndex); // 设置触发源选中项 //
            onSelectChanged(rgSource, rgSource.getSelected(), false); // 触发源变更 //
        } // 方法结束 //
    }; // 消费者结束 //

} // 类结束 //
