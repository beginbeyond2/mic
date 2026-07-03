/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：Micsig MHO系列示波器 - 触发系统 - N边沿触发配置界面              ║
 * ║  文件名称：TopLayoutTriggerNEdge.java                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责：                                                              ║
 * ║    1. 提供N边沿(NEdge)触发模式的参数配置UI（触发源、斜率、空闲时间、边沿数） ║
 * ║    2. 将用户操作同步到硬件Command、缓存CacheUtil、触发模型TriggerNEdge      ║
 * ║    3. 响应外部事件（RxBus/EventBus）更新UI状态                            ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计：                                                              ║
 * ║    - Fragment子类，由TopLayoutTrigger通过FragmentTransaction管理显示/隐藏  ║
 * ║    - 采用观察者模式：RxBus订阅 + EventFactory事件监听                     ║
 * ║    - 数据流向：UI → Command(硬件) + CacheUtil(缓存) + Trigger(模型)      ║
 * ║    - 反向同步：EventBus/RxBus → UI更新                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系：                                                              ║
 * ║    - TriggerNEdge : N边沿触发数据模型                                     ║
 * ║    - Command : 硬件指令发送中间件                                          ║
 * ║    - CacheUtil : 参数缓存持久化                                           ║
 * ║    - RxBus/EventFactory : 事件总线                                        ║
 * ║    - TopDialogScale/TopDialogCount : 时间/计数弹窗                        ║
 * ║    - TopViewRadioGroup : 自定义单选按钮组                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景：                                                              ║
 * ║    用户在顶部触发菜单选择"N边沿"触发类型时，显示此Fragment进行参数配置       ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发布局包

import android.content.Context; // 上下文类
import android.os.Bundle; // Fragment参数Bundle
import android.view.LayoutInflater; // 布局填充器
import android.view.View; // 视图基类
import android.view.ViewGroup; // 视图组基类
import android.widget.TextView; // 文本视图控件

import androidx.annotation.Nullable; // 可空注解
import androidx.fragment.app.Fragment; // Fragment基类

import com.micsig.tbook.scope.Event.EventBase; // 事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 事件UI观察者
import com.micsig.tbook.scope.Trigger.Trigger; // 触发器基类
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 触发器工厂
import com.micsig.tbook.scope.Trigger.TriggerNEdge; // N边沿触发器模型
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.LoadCache; // 缓存加载消息
import com.micsig.tbook.tbookscope.MainActivity; // 主Activity
import com.micsig.tbook.tbookscope.R; // 资源ID
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel; // 触发电平持有者
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel; // 触发电平消息
import com.micsig.tbook.tbookscope.middleware.command.Command; // 硬件指令中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 指令到UI的消息
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel; // 右侧滑菜单电平消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 按键音效工具
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 详情发送消息监听器
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogCount; // 计数弹窗
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
 * N边沿触发配置Fragment
 * <p>
 * 提供N边沿触发模式的参数配置界面，包括触发源选择、斜率方向、空闲时间和边沿计数。
 * 当示波器需要在指定空闲时间后检测到N个边沿时触发采集时使用此模式。
 * </p>
 * <p>Created by Administrator on 2017/4/10.</p>
 */
public class TopLayoutTriggerNEdge extends Fragment { // N边沿触发配置Fragment
    private Context context; // 上下文引用
    private TopViewRadioGroup rgSource; // 触发源单选按钮组
    private TopViewRadioGroup rgNEdgeSlope; // N边沿斜率单选按钮组
    private TextView tvNEdgeTime, tvNEdgeDetail; // 空闲时间文本视图、边沿数文本视图
    private TopDialogScale scaleDialog; // 时间刻度弹窗
    private TopDialogCount countDialog; // 计数弹窗

    private TopMsgTriggerNEdge msgTriggerDetail; // N边沿触发详情消息
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器

    private TriggerNEdge triggerNEdge; // N边沿触发器数据模型
    private ViewGroup rootView; // 根视图组

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
        return inflater.inflate(R.layout.layout_triggernedge, container, false); // 填充N边沿触发布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        this.rootView = (ViewGroup) view; // 保存根视图引用
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
        rgSource.setData(getResources().getString(R.string.triggerSource), GlobalVar.get().getChannelsName(), onCheckChangedListener); // 设置触发源数据（标签+通道名列表+监听器）
        rgNEdgeSlope = (TopViewRadioGroup) view.findViewById(R.id.nEdgeSlope); // 查找N边沿斜率按钮组
        rgNEdgeSlope.setData(R.string.triggerNEdgeSlope, R.array.triggerNEdgeSlope, onCheckChangedListener); // 设置斜率数据（标签+数组资源+监听器）
        tvNEdgeTime = (TextView) view.findViewById(R.id.nEdgeTime); // 查找空闲时间文本视图
        tvNEdgeTime.setOnClickListener(onClickListener); // 设置空闲时间点击监听
        tvNEdgeDetail = (TextView) view.findViewById(R.id.nEdgeDetail); // 查找边沿数文本视图
        tvNEdgeDetail.setOnClickListener(onClickListener); // 设置边沿数点击监听
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale); // 从MainActivity获取刻度弹窗
        countDialog = (TopDialogCount) ((MainActivity) context).findViewById(R.id.dialogTopCount); // 从MainActivity获取计数弹窗

        triggerNEdge = (TriggerNEdge) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_NEDGE); // 从触发器工厂获取N边沿触发器实例
    }

    /**
     * 初始化触发详情消息对象，从UI控件读取当前值
     */
    private void initData() { // 初始化数据
        msgTriggerDetail = new TopMsgTriggerNEdge(); // 创建N边沿触发消息对象
        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 设置触发源
        msgTriggerDetail.setnEdgeSlope(rgNEdgeSlope.getSelected()); // 设置斜率
        msgTriggerDetail.setnEdgeTime(tvNEdgeTime.getText().toString()); // 设置空闲时间
        msgTriggerDetail.setnEdgeDetail(tvNEdgeDetail.getText().toString()); // 设置边沿数
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
     * 从缓存恢复N边沿触发参数，并同步到硬件和模型
     */
    private void setCache() { // 设置缓存数据
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE); // 从缓存读取触发源索引
        int slope = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_SLOPE); // 从缓存读取斜率索引
        String time = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_IDLE); // 从缓存读取空闲时间
        int edge = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_EDGE); // 从缓存读取边沿数

        rgSource.setSelectedIndex(source); // 设置触发源选中项
        rgNEdgeSlope.setSelectedIndex(slope); // 设置斜率选中项
        tvNEdgeTime.setText(time); // 设置空闲时间文本
        tvNEdgeDetail.setText(String.valueOf(edge)); // 设置边沿数文本

        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 更新消息中的触发源
        msgTriggerDetail.setnEdgeSlope(rgNEdgeSlope.getSelected()); // 更新消息中的斜率
        msgTriggerDetail.setnEdgeTime(tvNEdgeTime.getText().toString()); // 更新消息中的空闲时间
        msgTriggerDetail.setnEdgeDetail(tvNEdgeDetail.getText().toString()); // 更新消息中的边沿数
        sendMsg(false); // 发送消息（非EventBus来源）

        Command.get().getTrigger_nedge().Source(source, false); // 发送触发源指令到硬件
        Command.get().getTrigger_nedge().Idle(TBookUtil.getSFromTime(time), false); // 发送空闲时间指令到硬件
        Command.get().getTrigger_nedge().Slope(slope, false); // 发送斜率指令到硬件
        Command.get().getTrigger_nedge().Edge(edge, false); // 发送边沿数指令到硬件

        triggerNEdge.setTriggerSource(source); // 更新模型中的触发源
        triggerNEdge.setSlope(slope); // 更新模型中的斜率
        triggerNEdge.setIdleTime(TBookUtil.getPsFromTime(time) / 1000); // 更新模型中的空闲时间（ps转ns）
        triggerNEdge.setEdge(edge); // 更新模型中的边沿数
    }

    /**
     * 获取N边沿触发详情消息
     * @return N边沿触发详情消息对象
     */
    public TopMsgTriggerNEdge getMsgTriggerDetail() { // 获取触发详情消息
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
     * 外部按键边沿事件消费者，循环切换斜率方向
     */
    private Consumer<Boolean> consumerExternalkeysEdge = new Consumer<Boolean>() { // 外部按键边沿消费者
        @Override
        public void accept(Boolean aBoolean) throws Exception { // 接收事件
            if(TriggerFactory.getTriggerType() == triggerNEdge.getTriggerType()){ // 当前触发类型为N边沿时
                int idx = rgNEdgeSlope.getSelected().getIndex() + 1; // 获取下一个斜率索引
                if(idx >= rgNEdgeSlope.getArray().length){ // 超出数组范围时
                    idx = 0; // 回到第一个
                }
                rgNEdgeSlope.setSelectedIndex(idx); // 设置选中索引
                onCheckChanged(rgNEdgeSlope, rgNEdgeSlope.getSelected(), false); // 触发选中变更处理
            }
        }
    };

    /**
     * 触发通道变更事件消费者，同步触发源选择
     */
    private Consumer<TopMsgTriggerChannel> consumerTriggerChannel = new Consumer<TopMsgTriggerChannel>() { // 触发通道消费者
        @Override
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception { // 接收事件
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_NEDGE) { // 非本Fragment触发的通道变更
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) { // 触发源索引不同时
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber()); // 更新触发源选中项
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber())); // 更新缓存
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 更新消息中的触发源
                    Command.get().getTrigger_nedge().Source(rgSource.getSelected().getIndex(),false); // 发送触发源指令
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
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerNEdge, true); // 标记本Fragment缓存已加载
        }
    };

    /**
     * 右侧电平变更事件消费者，同步触发源和斜率
     */
    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() { // 右侧电平消费者
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception { // 接收事件
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发类型索引
            if (triggerIndex == TopLayoutTrigger.DETAIL_NEDGE) { // 当前为N边沿触发时
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) { // 触发源不同时
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect()); // 更新触发源
                    onCheckChanged(rgSource, rgSource.getSelected(), false); // 触发变更处理
                }
                if ((msgLevel.getTopSelect() == 0) != (rgNEdgeSlope.getSelected().getIndex() == 0)) { // 斜率方向不同时
                    rgNEdgeSlope.setSelectedIndex(msgLevel.getTopSelect() == 0 ? 0 : 1); // 更新斜率
                    onCheckChanged(rgNEdgeSlope, rgNEdgeSlope.getSelected(), false); // 触发变更处理
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
            if (MainHolderTriggerLevel.LEVEL_TRIGGER_NEDGE.equals(msgTriggerLevel.getCurLevel())) { // 当前为N边沿触发电平时
                if (rgSource.getSelected().getIndex() != msgTriggerLevel.getCurCh() - 1) { // 通道不同时
                    rgSource.setSelectedIndex(msgTriggerLevel.getCurCh() - 1); // 更新触发源（通道号从1开始，索引从0开始）
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
                case CommandMsgToUI.FLAG_TRIGGERNEDGE_SOURCE: // 触发源变更指令
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 触发源不同时
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新触发源
                        onCheckChanged(rgSource, rgSource.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                case CommandMsgToUI.FLAG_TRIGGERNEDGE_SLOPE: // 斜率变更指令
//                    if (rgNEdgeSlope.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgNEdgeSlope.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 直接更新斜率
                        onCheckChanged(rgNEdgeSlope, rgNEdgeSlope.getSelected(), false); // 触发变更处理
//                    }
                    break; // 跳出
                case CommandMsgToUI.FLAG_TRIGGERNEDGE_IDLE: // 空闲时间变更指令
                    double checkBefore = Double.parseDouble(commandMsgToUI.getParam()); // 解析参数为秒数
                    long checkTime = TopUtilScale.checkTime((long) (checkBefore * TopUtilScale.TIME_S2NS) // 转换为纳秒并检查范围
                            , TopUtilScale.TIME_NEDGE_MIN, TopUtilScale.TIME_NEDGE_MAX); // 限制在N边沿时间范围内
                    String time = TBookUtil.getTimeFromS(checkTime * 1.0 / TopUtilScale.TIME_S2NS); // 转换为时间字符串
                    if (!tvNEdgeTime.getText().toString().equals(time)) { // 时间不同时
                        onTextChanged(tvNEdgeTime, time, false); // 触发文本变更处理
                    }
                    break; // 跳出
                case CommandMsgToUI.FLAG_TRIGGERNEDGE_EDGE: // 边沿数变更指令
                    if (!tvNEdgeDetail.getText().toString().equals(commandMsgToUI.getParam())) { // 边沿数不同时
                        onTextChanged(tvNEdgeDetail, commandMsgToUI.getParam(), false); // 触发文本变更处理
                    }
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
                int source = triggerNEdge.getTriggerSource(); // 从模型获取触发源
                if (source != rgSource.getSelected().getIndex()) { // 触发源不同时
                    rgSource.setSelectedIndex(source); // 更新触发源
                    onCheckChanged(rgSource, rgSource.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                int slope = triggerNEdge.getSlope(); // 从模型获取斜率
                if (slope != rgNEdgeSlope.getSelected().getIndex()) { // 斜率不同时
                    rgNEdgeSlope.setSelectedIndex(slope); // 更新斜率
                    onCheckChanged(rgNEdgeSlope, rgNEdgeSlope.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                long checkTime = TopUtilScale.checkTime(triggerNEdge.getIdleTime() // 从模型获取空闲时间并检查范围
                        , TopUtilScale.TIME_NEDGE_MIN, TopUtilScale.TIME_NEDGE_MAX); // 限制在N边沿时间范围内
                String time = TBookUtil.getTime3FromPs(checkTime * 1000 * 10); // 转换为时间字符串
                if (!tvNEdgeTime.getText().toString().equals(time)) { // 时间不同时
                    tvNEdgeTime.setText(time); // 更新空闲时间文本
                    onTextChanged(tvNEdgeTime, time, true); // 触发文本变更处理（来自EventBus）
                }
                String edge = String.valueOf(triggerNEdge.getEdge()); // 从模型获取边沿数
                if (!tvNEdgeDetail.getText().toString().equals(edge)) { // 边沿数不同时
                    tvNEdgeDetail.setText(edge); // 更新边沿数文本
                    onTextChanged(tvNEdgeDetail, edge, true); // 触发文本变更处理（来自EventBus）
                }
            }
        }
    };

    /**
     * 点击事件监听器，处理空闲时间和边沿数的点击弹窗
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            ScreenUtil.getViewLocation(v); // 获取视图屏幕位置
            int i = v.getId(); // 获取点击视图ID
            if (i == R.id.nEdgeTime) { // 点击空闲时间时
                scaleDialog.setValue(context.getString(R.string.triggernedge_time), tvNEdgeTime.getText().toString() // 设置刻度弹窗标题和当前值
                        , TopUtilScale.TIME_NEDGE_MIN, TopUtilScale.TIME_NEDGE_MAX, onScaleDismissListener); // 设置时间范围和关闭监听
            } else if (i == R.id.nEdgeDetail) { // 点击边沿数时
                countDialog.setData(context.getString(R.string.triggerEdge), Integer.parseInt(tvNEdgeDetail.getText().toString()) // 设置计数弹窗标题和当前值
                        , 65535, onCountDismissListener); // 设置最大值和关闭监听
            }
        }
    };

    /**
     * 刻度弹窗关闭监听器，将选择结果应用到空闲时间
     */
    private TopDialogScale.OnDismissListener onScaleDismissListener = new TopDialogScale.OnDismissListener() { // 刻度弹窗关闭监听
        @Override
        public void onDismiss(String result) { // 弹窗关闭回调
            onTextChanged(tvNEdgeTime, result, false); // 更新空闲时间
        }
    };

    /**
     * 计数弹窗关闭监听器，将选择结果应用到边沿数
     */
    private TopDialogCount.OnDismissListener onCountDismissListener = new TopDialogCount.OnDismissListener() { // 计数弹窗关闭监听
        @Override
        public void onDismiss(int result) { // 弹窗关闭回调
            onTextChanged(tvNEdgeDetail, String.valueOf(result), false); // 更新边沿数
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
        Tools.PrintControlsLocation("TopLayoutTriggerNEdge", rootView); // 打印控件位置调试信息
        if (view.getId() == R.id.triggerSource) { // 触发源变更时
            Command.get().getTrigger_nedge().Source(item.getIndex(), false); // 发送触发源指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex())); // 更新缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerNEdge.setTriggerSource(item.getIndex()); // 更新模型中的触发源
            }
            msgTriggerDetail.setTriggerSource(item); // 更新消息中的触发源
            sendMsg(isFromEventBus); // 发送消息
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL, // 发送触发通道变更事件
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_NEDGE, item.getIndex())); // 携带N边沿类型和通道索引
        } else if (view.getId() == R.id.nEdgeSlope) { // 斜率变更时
            Command.get().getTrigger_nedge().Slope(item.getIndex(), false); // 发送斜率指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_SLOPE, String.valueOf(item.getIndex())); // 更新缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerNEdge.setSlope(item.getIndex()); // 更新模型中的斜率
            }
            msgTriggerDetail.setnEdgeSlope(item); // 更新消息中的斜率
            sendMsg(isFromEventBus); // 发送消息
        }
    }

    /**
     * 处理文本变更，同步空闲时间或边沿数到硬件/缓存/模型
     * @param tv 变更的文本视图
     * @param result 新的文本值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) { // 文本变更处理
        if (tv.getId() == R.id.nEdgeTime) { // 空闲时间变更时
            Command.get().getTrigger_nedge().Idle(TBookUtil.getSFromTime(result), false); // 发送空闲时间指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_IDLE, result); // 更新缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerNEdge.setIdleTime(TBookUtil.getPsFromTime(result) / 1000); // 更新模型中的空闲时间
            }
            tvNEdgeTime.setText(result); // 更新UI文本
            msgTriggerDetail.setnEdgeTime(result); // 更新消息中的空闲时间
            sendMsg(isFromEventBus); // 发送消息
        } else if (tv.getId() == R.id.nEdgeDetail) { // 边沿数变更时
            Command.get().getTrigger_nedge().Edge(Integer.parseInt(result), false); // 发送边沿数指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_EDGE, result); // 更新缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerNEdge.setEdge(Integer.parseInt(result)); // 更新模型中的边沿数
            }
            tvNEdgeDetail.setText(result); // 更新UI文本
            msgTriggerDetail.setnEdgeDetail(result); // 更新消息中的边沿数
            sendMsg(isFromEventBus); // 发送消息
        }
    }
}
