/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：Micsig MHO系列示波器 - 触发系统 - 欠幅(Runt)触发配置界面        ║
 * ║  文件名称：TopLayoutTriggerRunt.java                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责：                                                              ║
 * ║    1. 提供欠幅(Runt)触发模式的参数配置UI（触发源、极性、条件、高/低时间）    ║
 * ║    2. 将用户操作同步到硬件Command、缓存CacheUtil、触发模型TriggerRunt       ║
 * ║    3. 响应外部事件（RxBus/EventBus）更新UI状态                            ║
 * ║    4. 维护高时间/低时间的联动约束（高时间必须小于低时间）                    ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计：                                                              ║
 * ║    - Fragment子类，由TopLayoutTrigger通过FragmentTransaction管理显示/隐藏  ║
 * ║    - 采用观察者模式：RxBus订阅 + EventFactory事件监听                     ║
 * ║    - 数据流向：UI → Command(硬件) + CacheUtil(缓存) + Trigger(模型)      ║
 * ║    - 反向同步：EventBus/RxBus → UI更新                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系：                                                              ║
 * ║    - TriggerRunt : 欠幅触发数据模型                                       ║
 * ║    - Command : 硬件指令发送中间件                                          ║
 * ║    - CacheUtil : 参数缓存持久化                                           ║
 * ║    - RxBus/EventFactory : 事件总线                                        ║
 * ║    - TopDialogScale : 时间刻度弹窗                                        ║
 * ║    - TopViewRadioGroup : 自定义单选按钮组                                 ║
 * ║    - TopMatchTrigger : 触发条件映射工具                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景：                                                              ║
 * ║    用户在顶部触发菜单选择"欠幅"触发类型时，显示此Fragment进行参数配置        ║
 * ║    欠幅触发用于检测脉冲幅度未达到高低阈值之间的信号                         ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发布局包

import android.content.Context; // 上下文类
import android.os.Bundle; // Fragment参数Bundle
import android.view.LayoutInflater; // 布局填充器
import android.view.View; // 视图基类
import android.view.ViewGroup; // 视图组基类
import android.widget.LinearLayout; // 线性布局控件
import android.widget.TextView; // 文本视图控件

import androidx.annotation.Nullable; // 可空注解
import androidx.fragment.app.Fragment; // Fragment基类

import com.micsig.tbook.scope.Event.EventBase; // 事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 事件UI观察者
import com.micsig.tbook.scope.Trigger.Trigger; // 触发器基类
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 触发器工厂
import com.micsig.tbook.scope.Trigger.TriggerRunt; // 欠幅触发器模型
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.LoadCache; // 缓存加载消息
import com.micsig.tbook.tbookscope.MainActivity; // 主Activity
import com.micsig.tbook.tbookscope.MainViewGroup; // 主视图组
import com.micsig.tbook.tbookscope.R; // 资源ID
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel; // 触发电平持有者
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel; // 触发电平消息
import com.micsig.tbook.tbookscope.middleware.command.Command; // 硬件指令中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 指令到UI的消息
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel; // 右侧滑菜单电平消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 按键音效工具
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 详情发送消息监听器
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogScale; // 刻度弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 自定义单选按钮组
import com.micsig.tbook.ui.top.view.scale.TopUtilScale; // 刻度工具
import com.micsig.tbook.ui.util.ScreenUtil; // 屏幕工具
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类

import io.reactivex.rxjava3.annotations.NonNull; // 非空注解
import io.reactivex.rxjava3.functions.Consumer; // RxJava消费者接口


/**
 * 欠幅(Runt)触发配置Fragment
 * <p>
 * 提供欠幅触发模式的参数配置界面，包括触发源选择、极性方向、触发条件（大于/小于/范围内/范围外）
 * 以及高时间和低时间参数。当信号脉冲幅度未穿过两个阈值时触发采集。
 * </p>
 * <p>Created by Administrator on 2017/4/10.</p>
 */
public class TopLayoutTriggerRunt extends Fragment { // 欠幅触发配置Fragment
    private Context context; // 上下文引用
    private TopViewRadioGroup rgSource; // 触发源单选按钮组
    private TopViewRadioGroup rgPolar; // 极性单选按钮组
    private TopViewRadioGroup rgCondition; // 触发条件单选按钮组
    private TextView tvTimeHigh; // 高时间文本视图
    private TextView tvTimeLow; // 低时间文本视图
    private LinearLayout layoutTimeHigh; // 高时间布局容器
    private LinearLayout layoutTimeLow; // 低时间布局容器
    private TopDialogScale scaleDialog; // 时间刻度弹窗

    private TopMsgTriggerRunt msgTriggerDetail; // 欠幅触发详情消息
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器

    private TriggerRunt triggerRunt; // 欠幅触发器数据模型

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图回调
        return inflater.inflate(R.layout.layout_triggerrunt, container, false); // 填充欠幅触发布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图控件
        initData(); // 初始化数据
        initControl(); // 初始化事件监听
    }

    /**
     * 初始化所有视图控件并绑定数据
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图
        rgSource = (TopViewRadioGroup) view.findViewById(R.id.triggerSource); // 查找触发源按钮组
        rgSource.setData(getResources().getString(R.string.triggerSource), GlobalVar.get().getChannelsName(), onCheckChangedListener); // 设置触发源数据
        rgPolar = (TopViewRadioGroup) view.findViewById(R.id.polar); // 查找极性按钮组
        rgPolar.setData(R.string.triggerRuntPolar, R.array.triggerRuntPolar, onCheckChangedListener); // 设置极性数据
        rgCondition = (TopViewRadioGroup) view.findViewById(R.id.condition); // 查找触发条件按钮组
        rgCondition.setData(R.string.triggerRuntCondition, R.array.triggerRuntCondition, onCheckChangedListener); // 设置触发条件数据
        tvTimeHigh = (TextView) view.findViewById(R.id.timeHighDetail); // 查找高时间文本视图
        tvTimeLow = (TextView) view.findViewById(R.id.timeLowDetail); // 查找低时间文本视图
        layoutTimeHigh = (LinearLayout) view.findViewById(R.id.layoutRuntTimeHigh); // 查找高时间布局容器
        layoutTimeLow = (LinearLayout) view.findViewById(R.id.layoutRuntTimeLow); // 查找低时间布局容器
        tvTimeHigh.setOnClickListener(onClickListener); // 设置高时间点击监听
        tvTimeLow.setOnClickListener(onClickListener); // 设置低时间点击监听
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale); // 从MainActivity获取刻度弹窗

        triggerRunt = (TriggerRunt) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_LOW_PULSE); // 从触发器工厂获取欠幅触发器实例
    }

    /**
     * 初始化触发详情消息对象，从UI控件读取当前值
     */
    private void initData() { // 初始化数据
        msgTriggerDetail = new TopMsgTriggerRunt(); // 创建欠幅触发消息对象
        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 设置触发源
        msgTriggerDetail.setPolar(rgPolar.getSelected()); // 设置极性
        msgTriggerDetail.setCondition(rgCondition.getSelected()); // 设置触发条件
        msgTriggerDetail.setTimeHighDetail(tvTimeHigh.getText().toString()); // 设置高时间
        msgTriggerDetail.setTimeLowDetail(tvTimeLow.getText().toString()); // 设置低时间
    }

    /**
     * 注册RxBus事件订阅和EventFactory事件观察者
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.TOPTRIGGER_CHANNEL).subscribe(consumerTriggerChannel); // 订阅触发通道变更事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel); // 订阅右侧电平变更事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL).subscribe(consumerTriggerLevel); // 订阅触发电平通道变更事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅指令到UI事件

        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_EDGE).subscribe(consumerExternalkeysEdge); // 订阅外部按键边沿事件
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_PARAM, eventUITriggerParam); // 注册触发参数事件观察者
    }

    /**
     * 从缓存恢复欠幅触发参数，并同步到硬件和模型
     */
    private void setCache() { // 设置缓存数据
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE); // 从缓存读取触发源索引
        int polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_RUNT_POLAR); // 从缓存读取极性索引
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_RUNT_CONDITION); // 从缓存读取触发条件索引
        String timeHigh = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_HIGH); // 从缓存读取高时间
        String timeLow = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_LOW); // 从缓存读取低时间

        rgSource.setSelectedIndex(source); // 设置触发源选中项
        rgPolar.setSelectedIndex(polar); // 设置极性选中项
        rgCondition.setSelectedIndex(condition); // 设置触发条件选中项
        tvTimeHigh.setText(timeHigh); // 设置高时间文本
        tvTimeLow.setText(timeLow); // 设置低时间文本
        switch (condition) { // 根据触发条件控制时间布局可见性
            case 0: // 条件0：大于
                layoutTimeHigh.setVisibility(View.VISIBLE); // 显示高时间
                layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏低时间
                break; // 跳出
            case 1: // 条件1：小于
                layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏高时间
                layoutTimeLow.setVisibility(View.VISIBLE); // 显示低时间
                break; // 跳出
            case 2: // 条件2：范围内
                layoutTimeHigh.setVisibility(View.VISIBLE); // 显示高时间
                layoutTimeLow.setVisibility(View.VISIBLE); // 显示低时间
                break; // 跳出
            case 3: // 条件3：范围外
                layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏高时间
                layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏低时间
                break; // 跳出
        }

        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 更新消息中的触发源
        msgTriggerDetail.setPolar(this.rgPolar.getSelected()); // 更新消息中的极性
        msgTriggerDetail.setCondition(this.rgCondition.getSelected()); // 更新消息中的触发条件
        msgTriggerDetail.setTimeHighDetail(timeHigh); // 更新消息中的高时间
        msgTriggerDetail.setTimeLowDetail(timeLow); // 更新消息中的低时间
        sendMsg(false); // 发送消息（非EventBus来源）

        Command.get().getTrigger_dwart().Source(source, false); // 发送触发源指令到硬件
        Command.get().getTrigger_dwart().Polarity(polar, false); // 发送极性指令到硬件
        Command.get().getTrigger_dwart().Condition(condition, false); // 发送触发条件指令到硬件
        Command.get().getTrigger_dwart().HTime(TBookUtil.getSFromTime(timeHigh), false); // 发送高时间指令到硬件
        Command.get().getTrigger_dwart().LTime(TBookUtil.getSFromTime(timeLow), false); // 发送低时间指令到硬件

        triggerRunt.setTriggerSource(source); // 更新模型中的触发源
        triggerRunt.setPolarity(polar); // 更新模型中的极性
        triggerRunt.setCondition(TopMatchTrigger.triggerConditionToScope(condition)); // 更新模型中的触发条件（UI索引转示波器索引）
        triggerRunt.setTimeHigh(TBookUtil.getPsFromTime(timeHigh) / 1000); // 更新模型中的高时间（ps转ns）
        triggerRunt.setTimeLow(TBookUtil.getPsFromTime(timeLow) / 1000); // 更新模型中的低时间（ps转ns）
    }

    /**
     * 获取欠幅触发详情消息
     * @return 欠幅触发详情消息对象
     */
    public TopMsgTriggerRunt getMsgTriggerDetail() { // 获取触发详情消息
        return msgTriggerDetail; // 返回消息对象
    }

    /**
     * 设置详情消息发送监听器
     * @param onDetailSendMsgListener 详情消息发送监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息发送监听器
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 保存监听器引用
    }

    /**
     * 发送触发详情消息给父Fragment
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) { // 发送消息
        if (onDetailSendMsgListener != null) { // 监听器不为空时
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用监听器回调
        }
    }

    /**
     * 外部按键边沿事件消费者，循环切换极性方向
     */
    private Consumer<Boolean> consumerExternalkeysEdge = new Consumer<Boolean>() { // 外部按键边沿消费者
        @Override
        public void accept(Boolean aBoolean) throws Exception { // 接收事件
            if(TriggerFactory.getTriggerType() == triggerRunt.getTriggerType()){ // 当前触发类型为欠幅时
                int idx = rgPolar.getSelected().getIndex() + 1; // 获取下一个极性索引
                if(idx >= rgPolar.getArray().length){ // 超出数组范围时
                    idx = 0; // 回到第一个
                }
                rgPolar.setSelectedIndex(idx); // 设置选中索引
                onCheckChanged(rgPolar, rgPolar.getSelected(), false); // 触发选中变更处理
            }
        }
    };

    /**
     * 触发通道变更事件消费者，同步触发源选择
     */
    private Consumer<TopMsgTriggerChannel> consumerTriggerChannel = new Consumer<TopMsgTriggerChannel>() { // 触发通道消费者
        @Override
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception { // 接收事件
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_RUNT) { // 非本Fragment触发的通道变更
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) { // 触发源索引不同时
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber()); // 更新触发源选中项
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber())); // 更新缓存
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 更新消息中的触发源
                    Command.get().getTrigger_dwart().Source(rgSource.getSelected().getIndex(),false); // 发送触发源指令
                }
            }
        }
    };

    /**
     * 缓存加载事件消费者，恢复缓存参数
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收事件
            setCache(); // 从缓存恢复参数
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerRunt, true); // 标记本Fragment缓存已加载
        }
    };

    /**
     * 右侧电平变更事件消费者，同步触发源和极性
     */
    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() { // 右侧电平消费者
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception { // 接收事件
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发类型索引
            if (triggerIndex == TopLayoutTrigger.DETAIL_RUNT) { // 当前为欠幅触发时
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) { // 触发源不同时
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect()); // 更新触发源
                    onCheckChanged(rgSource, rgSource.getSelected(), false); // 触发变更处理
                }
                if (msgLevel.getTopSelect() != rgPolar.getSelected().getIndex()) { // 极性不同时
                    rgPolar.setSelectedIndex(msgLevel.getTopSelect()); // 更新极性
                    onCheckChanged(rgPolar, rgPolar.getSelected(), false); // 触发变更处理
                }
            }
        }
    };

    /**
     * 触发电平通道变更事件消费者，同步触发源
     */
    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() { // 触发电平消费者
        @Override
        public void accept(@NonNull MainMsgTriggerLevel msgTriggerLevel) throws Exception { // 接收事件
            if (msgTriggerLevel.isOnlyModifyNumber()) return; // 仅修改数值时不处理
            if (MainHolderTriggerLevel.LEVEL_TRIGGER_RUNT.equals(msgTriggerLevel.getCurLevel())) { // 当前为欠幅触发电平时
                if (rgSource.getSelected().getIndex() != msgTriggerLevel.getCurCh() - 1) { // 通道不同时
                    rgSource.setSelectedIndex(msgTriggerLevel.getCurCh() - 1); // 更新触发源（通道号从1开始）
                    onCheckChanged(rgSource, rgSource.getSelected(), msgTriggerLevel.isFromEventBus()); // 触发变更处理
                }
            }
        }
    };

    /**
     * 指令到UI事件消费者，处理远程指令同步UI
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 指令到UI消费者
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收事件
            switch (commandMsgToUI.getFlag()) { // 根据指令标志分发
                case CommandMsgToUI.FLAG_TRIGGERRUNT_SOURCE: // 触发源变更指令
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 触发源不同时
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新触发源
                        onCheckChanged(rgSource, rgSource.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                case CommandMsgToUI.FLAG_TRIGGERRUNT_POLAR: // 极性变更指令
                    if (rgPolar.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 极性不同时
                        rgPolar.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新极性
                        onCheckChanged(rgPolar, rgPolar.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                case CommandMsgToUI.FLAG_TRIGGERRUNT_CONDITION: // 触发条件变更指令
                    if (rgCondition.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 触发条件不同时
                        rgCondition.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新触发条件
                        onCheckChanged(rgCondition, rgCondition.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                case CommandMsgToUI.FLAG_TRIGGERRUNT_HTIME: // 高时间变更指令
                    double checkBeforeHigh = Double.parseDouble(commandMsgToUI.getParam()); // 解析参数为秒数
                    long checkTimeHigh = TopUtilScale.checkTime((long) (checkBeforeHigh * TopUtilScale.TIME_S2NS) // 转换为纳秒并检查范围
                            , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX); // 限制在欠幅时间范围内
                    String timeHigh = TBookUtil.getTimeFromS(checkTimeHigh * 1.0 / TopUtilScale.TIME_S2NS); // 转换为时间字符串
                    onTextChanged(tvTimeHigh, timeHigh, false); // 触发文本变更处理
                    break; // 跳出
                case CommandMsgToUI.FLAG_TRIGGERRUNT_LOWTIME: // 低时间变更指令
                    double checkBeforeLow = Double.parseDouble(commandMsgToUI.getParam()); // 解析参数为秒数
                    long checkTimeLow = TopUtilScale.checkTime((long) (checkBeforeLow * TopUtilScale.TIME_S2NS) // 转换为纳秒并检查范围
                            , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX); // 限制在欠幅时间范围内
                    String timeLow = TBookUtil.getTimeFromS(checkTimeLow * 1.0 / TopUtilScale.TIME_S2NS); // 转换为时间字符串
                    onTextChanged(tvTimeLow, timeLow, false); // 触发文本变更处理
                    break; // 跳出
                case CommandMsgToUI.FLAG_TRIGGERRUNT_BTIME: // 高低时间同时变更指令
                    String[] param = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 拆分参数（高时间;低时间）
                    double checkBeforeBHigh = Double.parseDouble(param[0]); // 解析高时间秒数
                    long checkTimeBHigh = TopUtilScale.checkTime((long) (checkBeforeBHigh * TopUtilScale.TIME_S2NS) // 转换为纳秒并检查范围
                            , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX); // 限制在欠幅时间范围内
                    String timeBHigh = TBookUtil.getTimeFromS(checkTimeBHigh * 1.0 / TopUtilScale.TIME_S2NS); // 转换为时间字符串
                    double checkBeforeBLow = Double.parseDouble(param[1]); // 解析低时间秒数
                    long checkTimeBLow = TopUtilScale.checkTime((long) (checkBeforeBLow * TopUtilScale.TIME_S2NS) // 转换为纳秒并检查范围
                            , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX); // 限制在欠幅时间范围内
                    String timeBLow = TBookUtil.getTimeFromS(checkTimeBLow * 1.0 / TopUtilScale.TIME_S2NS); // 转换为时间字符串
                    onTextChanged(tvTimeHigh, timeBHigh, false); // 触发高时间文本变更处理
                    onTextChanged(tvTimeLow, timeBLow, false); // 触发低时间文本变更处理
                    break; // 跳出
            }
        }
    };

    /**
     * 触发参数事件观察者，从EventBus同步触发模型参数到UI
     */
    private EventUIObserver eventUITriggerParam = new EventUIObserver() { // 触发参数事件观察者
        @Override
        public void update(Object data) { // 事件更新回调
            EventBase eventBase = (EventBase) data; // 转换为事件基类
            if (eventBase.getId() == EventFactory.EVENT_TRIGGER_PARAM) { // 触发参数事件时
                int source = triggerRunt.getTriggerSource(); // 从模型获取触发源
                if (rgSource.getSelected().getIndex() != source) { // 触发源不同时
                    rgSource.setSelectedIndex(source); // 更新触发源
                    onCheckChanged(rgSource, rgSource.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                int polar = triggerRunt.getPolarity(); // 从模型获取极性
                if (rgPolar.getSelected().getIndex() != polar) { // 极性不同时
                    rgPolar.setSelectedIndex(polar); // 更新极性
                    onCheckChanged(rgPolar, rgPolar.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                int condition = TopMatchTrigger.triggerConditionFromScope(triggerRunt.getCondition()); // 从模型获取触发条件（示波器索引转UI索引）
                if (rgCondition.getSelected().getIndex() != condition) { // 触发条件不同时
                    rgCondition.setSelectedIndex(condition); // 更新触发条件
                    onCheckChanged(rgCondition, rgCondition.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                long checkTimeHigh = TopUtilScale.checkTime(triggerRunt.getTimeHigh() // 从模型获取高时间并检查范围
                        , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX); // 限制在欠幅时间范围内
                String timeHigh = TBookUtil.getTime3FromPs(checkTimeHigh * 1000 * 10); // 转换为时间字符串
                if (!tvTimeHigh.getText().toString().equals(timeHigh)) { // 高时间不同时
                    onTextChanged(tvTimeHigh, timeHigh, true); // 触发文本变更处理（来自EventBus）
                }
                long checkTimeLow = TopUtilScale.checkTime(triggerRunt.getTimeLow() // 从模型获取低时间并检查范围
                        , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX); // 限制在欠幅时间范围内
                String timeLow = TBookUtil.getTime3FromPs(checkTimeLow * 1000 * 10); // 转换为时间字符串
                if (!tvTimeLow.getText().toString().equals(timeLow)) { // 低时间不同时
                    onTextChanged(tvTimeLow, timeLow, true); // 触发文本变更处理（来自EventBus）
                }
            }
        }
    };

    /**
     * 点击事件监听器，处理高时间和低时间的点击弹窗
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            ScreenUtil.getViewLocation(v); // 获取视图屏幕位置
            switch (v.getId()) { // 根据点击视图ID分发
                case R.id.timeHighDetail: // 点击高时间时
                    scaleDialog.setValue(context.getString(R.string.triggerrunt_timehigh), tvTimeHigh.getText().toString(), // 设置刻度弹窗标题和当前值
                            TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX, onHighDismissListener); // 设置时间范围和关闭监听
                    break; // 跳出
                case R.id.timeLowDetail: // 点击低时间时
                    scaleDialog.setValue(context.getString(R.string.triggerrunt_timelow), tvTimeLow.getText().toString(), // 设置刻度弹窗标题和当前值
                            TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX, onLowDismissListener); // 设置时间范围和关闭监听
                    break; // 跳出
            }
        }
    };

    /**
     * 高时间刻度弹窗关闭监听器，将选择结果应用到高时间
     */
    private TopDialogScale.OnDismissListener onHighDismissListener = new TopDialogScale.OnDismissListener() { // 高时间弹窗关闭监听
        @Override
        public void onDismiss(String result) { // 弹窗关闭回调
            onTextChanged(tvTimeHigh, result, false); // 更新高时间
        }
    };

    /**
     * 低时间刻度弹窗关闭监听器，将选择结果应用到低时间
     */
    private TopDialogScale.OnDismissListener onLowDismissListener = new TopDialogScale.OnDismissListener() { // 低时间弹窗关闭监听
        @Override
        public void onDismiss(String result) { // 弹窗关闭回调
            onTextChanged(tvTimeLow, result, false); // 更新低时间
        }
    };


    /**
     * 单选按钮组变更监听器，处理选中项变更的音效和回调
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选变更监听器
        @Override
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { // 提示回调（空实现）

        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 点击回调
            onCheckChanged(view, item, false); // 触发选中变更处理（非EventBus来源）
        }
    };

    /**
     * 处理单选按钮组选中项变更，同步到硬件/缓存/模型
     * @param view 变更的单选按钮组
     * @param item 选中的项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 选中变更处理
        if (view.getId() == R.id.triggerSource) { // 触发源变更时
            Command.get().getTrigger_dwart().Source(item.getIndex(), false); // 发送触发源指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex())); // 更新缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerRunt.setTriggerSource(item.getIndex()); // 更新模型中的触发源
            }
            msgTriggerDetail.setTriggerSource(item); // 更新消息中的触发源
            sendMsg(isFromEventBus); // 发送消息
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL, // 发送触发通道变更事件
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_RUNT, item.getIndex())); // 携带欠幅类型和通道索引
        } else if (view.getId() == R.id.polar) { // 极性变更时
            Command.get().getTrigger_dwart().Polarity(item.getIndex(), false); // 发送极性指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_POLAR, String.valueOf(item.getIndex())); // 更新缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerRunt.setPolarity(item.getIndex()); // 更新模型中的极性
            }
            msgTriggerDetail.setPolar(item); // 更新消息中的极性
            sendMsg(isFromEventBus); // 发送消息
        } else if (view.getId() == R.id.condition) { // 触发条件变更时
            Command.get().getTrigger_dwart().Condition(item.getIndex(), false); // 发送触发条件指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_CONDITION, String.valueOf(item.getIndex())); // 更新缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerRunt.setCondition(TopMatchTrigger.triggerConditionToScope(item.getIndex())); // 更新模型中的触发条件（UI索引转示波器索引）
            }
            msgTriggerDetail.setCondition(item); // 更新消息中的触发条件
            sendMsg(isFromEventBus); // 发送消息
            switch (item.getIndex()) { // 根据触发条件控制时间布局可见性
                case 0: // 条件0：大于
                    layoutTimeHigh.setVisibility(View.VISIBLE); // 显示高时间
                    layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏低时间
                    break; // 跳出
                case 1: // 条件1：小于
                    layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏高时间
                    layoutTimeLow.setVisibility(View.VISIBLE); // 显示低时间
                    break; // 跳出
                case 2: // 条件2：范围内
                    layoutTimeHigh.setVisibility(View.VISIBLE); // 显示高时间
                    layoutTimeLow.setVisibility(View.VISIBLE); // 显示低时间
                    break; // 跳出
                case 3: // 条件3：范围外
                    layoutTimeHigh.setVisibility(View.INVISIBLE); // 隐藏高时间
                    layoutTimeLow.setVisibility(View.INVISIBLE); // 隐藏低时间
                    break; // 跳出
            }
            RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPTRIGGER_RUNT_HIGH); // 发送控件可见性变更事件
        }
    }

    /**
     * 处理文本变更，同步高时间或低时间到硬件/缓存/模型
     * 维护高时间必须小于低时间的约束
     * @param tv 变更的文本视图
     * @param result 新的文本值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) { // 文本变更处理
        if (tv.getId() == tvTimeHigh.getId()) { // 高时间变更时
            long ht = TBookUtil.getPsFromTime(result) / 1000; // 将高时间转换为ns
            long lt = TBookUtil.getPsFromTime(tvTimeLow.getText().toString()) / 1000; // 将低时间转换为ns
            if (ht < lt) { // 高时间小于低时间时，需要联动调整低时间
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_LOW, result); // 更新低时间缓存
                if (!isFromEventBus) { // 非EventBus来源时
                    triggerRunt.setTimeLow(ht); // 更新模型中的低时间
                }
                tvTimeLow.setText(result); // 更新低时间UI文本
                Command.get().getTrigger_dwart().LTime(TBookUtil.getSFromTime(result), false); // 发送低时间指令到硬件
                msgTriggerDetail.setTimeLowDetail(result); // 更新消息中的低时间
            }
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_HIGH, result); // 更新高时间缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerRunt.setTimeHigh(ht); // 更新模型中的高时间
            }
            tvTimeHigh.setText(result); // 更新高时间UI文本
            Command.get().getTrigger_dwart().HTime(TBookUtil.getSFromTime(result), false); // 发送高时间指令到硬件
            msgTriggerDetail.setTimeHighDetail(result); // 更新消息中的高时间
            sendMsg(isFromEventBus); // 发送消息
        } else if (tv.getId() == tvTimeLow.getId()) { // 低时间变更时
            long lt = TBookUtil.getPsFromTime(result) / 1000; // 将低时间转换为ns
            long ht = TBookUtil.getPsFromTime(tvTimeHigh.getText().toString()) / 1000; // 将高时间转换为ns
            if (ht < lt) { // 高时间小于低时间时，需要联动调整高时间
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_HIGH, result); // 更新高时间缓存
                if (!isFromEventBus) { // 非EventBus来源时
                    triggerRunt.setTimeHigh(ht); // 更新模型中的高时间
                }
                tvTimeHigh.setText(result); // 更新高时间UI文本
                Command.get().getTrigger_dwart().HTime(TBookUtil.getSFromTime(result), false); // 发送高时间指令到硬件
                msgTriggerDetail.setTimeHighDetail(result); // 更新消息中的高时间
            }
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_LOW, result); // 更新低时间缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerRunt.setTimeLow(TBookUtil.getPsFromTime(result) / 1000); // 更新模型中的低时间
            }
            tvTimeLow.setText(result); // 更新低时间UI文本
            Command.get().getTrigger_dwart().LTime(TBookUtil.getSFromTime(result), false); // 发送低时间指令到硬件
            msgTriggerDetail.setTimeLowDetail(result); // 更新消息中的低时间
            sendMsg(isFromEventBus); // 发送消息
        }
    }
}
