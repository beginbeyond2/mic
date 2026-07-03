
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
import com.micsig.tbook.scope.Trigger.TriggerVideo; // 视频触发器模型
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.LoadCache; // 缓存加载消息
import com.micsig.tbook.tbookscope.MainActivity; // 主Activity
import com.micsig.tbook.tbookscope.MainViewGroup; // 主视图组
import com.micsig.tbook.tbookscope.R; // 资源ID
import com.micsig.tbook.tbookscope.middleware.command.Command; // 硬件指令中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 指令到UI的消息
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel; // 右侧滑菜单电平消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 按键音效工具
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 详情发送消息监听器
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogCount; // 计数弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 自定义单选按钮组
import com.micsig.tbook.ui.util.ScreenUtil; // 屏幕工具

import io.reactivex.rxjava3.annotations.NonNull; // 非空注解
import io.reactivex.rxjava3.functions.Consumer; // RxJava消费者接口

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：Micsig MHO系列示波器 - 触发系统 - 视频(Video)触发配置界面        ║
 * ║  文件名称：TopLayoutTriggerVideo.java                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责：                                                              ║
 * ║    1. 提供视频(Video)触发模式的参数配置UI（触发源、极性、标准、触发点、     ║
 * ║       频率、行号）                                                        ║
 * ║    2. 将用户操作同步到硬件Command、缓存CacheUtil、触发模型TriggerVideo      ║
 * ║    3. 根据视频标准动态切换触发点和频率选项                                 ║
 * ║    4. 响应外部事件（RxBus/EventBus）更新UI状态                            ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计：                                                              ║
 * ║    - Fragment子类，由TopLayoutTrigger通过FragmentTransaction管理显示/隐藏  ║
 * ║    - 采用观察者模式：RxBus订阅 + EventFactory事件监听                     ║
 * ║    - 数据流向：UI → Command(硬件) + CacheUtil(缓存) + Trigger(模型)      ║
 * ║    - 反向同步：EventBus/RxBus → UI更新                                   ║
 * ║    - 视频标准切换时动态更新触发点/频率数组和行号范围                        ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系：                                                              ║
 * ║    - TriggerVideo : 视频触发数据模型                                      ║
 * ║    - Command : 硬件指令发送中间件                                          ║
 * ║    - CacheUtil : 参数缓存持久化                                           ║
 * ║    - RxBus/EventFactory : 事件总线                                        ║
 * ║    - TopDialogCount : 计数弹窗（行号输入）                                ║
 * ║    - TopViewRadioGroup : 自定义单选按钮组                                 ║
 * ║    - TopMatchTrigger : 视频触发模式/频率映射工具                          ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景：                                                              ║
 * ║    用户在顶部触发菜单选择"视频"触发类型时，显示此Fragment进行参数配置        ║
 * ║    视频触发用于检测特定视频标准（PAL/NTSC/SECAM/720P/1080I等）的信号       ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
/**
 * 视频(Video)触发配置Fragment
 * <p>
 * 提供视频触发模式的参数配置界面，包括触发源选择、极性方向、视频标准（PAL/NTSC/SECAM/720P/1080I等）、
 * 触发点、频率和行号。根据视频标准动态切换触发点和频率选项。
 * </p>
 * <p>Created by Administrator on 2017/4/10.</p>
 */
public class TopLayoutTriggerVideo extends Fragment { // 视频触发配置Fragment
    private static final int TRIGGERCHOICEMORE = 0; // 触发点选项多模式标记
    private static final int TRIGGERCHOICELESS = 1; // 触发点选项少模式标记
    private static final int FREQUENCYCHOICEMORE = 0; // 频率选项多模式标记
    private static final int FREQUENCYCHOICELESS = 1; // 频率选项少模式标记

    private Context context; // 上下文引用
    private TopViewRadioGroup rgSource; // 触发源单选按钮组
    private TopViewRadioGroup rgPolar; // 极性单选按钮组
    private TopViewRadioGroup rgStandard; // 视频标准单选按钮组
    private TopViewRadioGroup rgTrigger; // 触发点单选按钮组
    private TopViewRadioGroup rgFrequency; // 频率单选按钮组
    private TextView tvLine; // 行号文本视图
    private LinearLayout layoutLine; // 行号布局容器
    private int triggerChoice, frequencyChoice; // 触发点选项模式、频率选项模式
    private TopDialogCount countDialog; // 计数弹窗（行号输入）

    private TopMsgTriggerVideo msgTriggerDetail; // 视频触发详情消息
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器

    private TriggerVideo triggerVideo; // 视频触发器数据模型
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
        return inflater.inflate(R.layout.layout_triggervideo, container, false); // 填充视频触发布局
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
        rgSource.setData(getResources().getString(R.string.triggerSource), GlobalVar.get().getChannelsName(), onCheckChangedListener); // 设置触发源数据
        rgPolar = (TopViewRadioGroup) view.findViewById(R.id.polar); // 查找极性按钮组
        rgPolar.setData(R.string.triggerVideoPolar, R.array.triggerVideoPolar, onCheckChangedListener); // 设置极性数据
        rgStandard = (TopViewRadioGroup) view.findViewById(R.id.standard); // 查找视频标准按钮组
        rgStandard.setData(R.string.triggerVideoStandard, R.array.triggerVideoStandard, onCheckChangedListener); // 设置视频标准数据
        rgTrigger = (TopViewRadioGroup) view.findViewById(R.id.trigger); // 查找触发点按钮组
        triggerChoice = TRIGGERCHOICEMORE; // 初始化触发点选项为多模式
        rgTrigger.setData(R.string.triggerVideotrigger, getPointArrayResId(), onCheckChangedListener); // 设置触发点数据
        rgFrequency = (TopViewRadioGroup) view.findViewById(R.id.frequency); // 查找频率按钮组
        frequencyChoice = FREQUENCYCHOICELESS; // 初始化频率选项为少模式
        rgFrequency.setData(R.string.triggerVideoFrequency, getFrequencyArrayResId(), onCheckChangedListener); // 设置频率数据
        rgFrequency.setVisibility(View.GONE); // 默认隐藏频率选项

        layoutLine = (LinearLayout) view.findViewById(R.id.lineLayout); // 查找行号布局容器
        layoutLine.setVisibility(View.GONE); // 默认隐藏行号布局
        tvLine = (TextView) view.findViewById(R.id.lineDetail); // 查找行号文本视图
        tvLine.setOnClickListener(onClickListener); // 设置行号点击监听
        countDialog = (TopDialogCount) ((MainActivity) context).findViewById(R.id.dialogTopCount); // 从MainActivity获取计数弹窗

        triggerVideo = (TriggerVideo) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_VIDEO); // 从触发器工厂获取视频触发器实例
    }

    /**
     * 初始化触发详情消息对象，从UI控件读取当前值
     */
    private void initData() { // 初始化数据
        msgTriggerDetail = new TopMsgTriggerVideo(); // 创建视频触发消息对象
        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 设置触发源
        msgTriggerDetail.setPolar(rgPolar.getSelected()); // 设置极性
        msgTriggerDetail.setStandard(rgStandard.getSelected()); // 设置视频标准
        msgTriggerDetail.setTrigger(rgTrigger.getSelected()); // 设置触发点
        msgTriggerDetail.setFrequency(rgFrequency.getSelected()); // 设置频率
        msgTriggerDetail.setLineDetail(tvLine.getText().toString()); // 设置行号
    }

    /**
     * 注册RxBus事件订阅和EventFactory事件观察者
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.TOPTRIGGER_CHANNEL).subscribe(consumerTriggerChannel); // 订阅触发通道变更事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel); // 订阅右侧电平变更事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅指令到UI事件

        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_EDGE).subscribe(consumerExternalkeysEdge); // 订阅外部按键边沿事件
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_PARAM, eventUITriggerParam); // 注册触发参数事件观察者
    }

    /**
     * 从缓存恢复视频触发参数，并同步到硬件和模型
     */
    private void setCache() { // 设置缓存数据
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE); // 从缓存读取触发源索引
        int polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_POLAR); // 从缓存读取极性索引
        int standard = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_STANDARD); // 从缓存读取视频标准索引
        int trigger = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_TRIGGER + standard); // 从缓存读取触发点索引（按标准区分）
        int frequency = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + standard); // 从缓存读取频率索引（按标准区分）
        int line = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_LINE + standard); // 从缓存读取行号（按标准区分）

        switch (standard) { // 根据视频标准设置触发点和频率选项模式
            case 0: // PAL
            case 1: // PAL-M
            case 2: // NTSC
                triggerChoice = TRIGGERCHOICEMORE; // 触发点多选项
                rgFrequency.setVisibility(View.GONE); // 隐藏频率选项
                break; // 跳出
            case 3: // SECAM
                triggerChoice = TRIGGERCHOICELESS; // 触发点少选项
                frequencyChoice = FREQUENCYCHOICELESS; // 频率少选项
                rgFrequency.setVisibility(View.VISIBLE); // 显示频率选项
                break; // 跳出
            case 4: // 720P
                triggerChoice = TRIGGERCHOICEMORE; // 触发点多选项
                frequencyChoice = FREQUENCYCHOICELESS; // 频率少选项
                rgFrequency.setVisibility(View.VISIBLE); // 显示频率选项
                break; // 跳出
            case 5: // 1080I
                triggerChoice = TRIGGERCHOICELESS; // 触发点少选项
                frequencyChoice = FREQUENCYCHOICEMORE; // 频率多选项
                rgFrequency.setVisibility(View.VISIBLE); // 显示频率选项
                break; // 跳出
        }
        rgTrigger.setData(R.string.triggerVideotrigger, getPointArrayResId(), onCheckChangedListener); // 更新触发点数据
        rgFrequency.setData(R.string.triggerVideoFrequency, getFrequencyArrayResId(), onCheckChangedListener); // 更新频率数据

        rgSource.setSelectedIndex(source); // 设置触发源选中项
        rgPolar.setSelectedIndex(polar); // 设置极性选中项
        rgStandard.setSelectedIndex(standard); // 设置视频标准选中项
        rgTrigger.setSelectedIndex(trigger); // 设置触发点选中项
        rgFrequency.setSelectedIndex(frequency); // 设置频率选中项
        tvLine.setText(String.valueOf(line)); // 设置行号文本
        String[] point = getResources().getStringArray(getPointArrayResId()); // 获取当前触发点选项数组
        layoutLine.setVisibility(point.length - 1 == trigger ? View.VISIBLE : View.GONE); // 最后一个触发点选项（自定义行号）时显示行号

        msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 更新消息中的触发源
        msgTriggerDetail.setPolar(rgPolar.getSelected()); // 更新消息中的极性
        msgTriggerDetail.setStandard(rgStandard.getSelected()); // 更新消息中的视频标准
        msgTriggerDetail.setTrigger(rgTrigger.getSelected()); // 更新消息中的触发点
        msgTriggerDetail.setFrequency(rgFrequency.getSelected()); // 更新消息中的频率
        msgTriggerDetail.setLineDetail(String.valueOf(line)); // 更新消息中的行号
        sendMsg(false); // 发送消息（非EventBus来源）

        Command.get().getTrigger_video().Source(source, false); // 发送触发源指令到硬件
        Command.get().getTrigger_video().Polarity(polar, false); // 发送极性指令到硬件
        Command.get().getTrigger_video().Standard(standard, false); // 发送视频标准指令到硬件
        Command.get().getTrigger_video().Line(line, false); // 发送行号指令到硬件
        if (standard == 3 || standard == 5) { // SECAM或1080I使用B模式
            Command.get().getTrigger_video().Bmode(trigger, false); // 发送B模式触发点指令
        } else { // 其他标准使用A模式
            Command.get().getTrigger_video().Amode(trigger, false); // 发送A模式触发点指令
        }
        if (standard == 3 || standard == 4) { // SECAM或720P使用A频率
            Command.get().getTrigger_video().Afrequence(frequency, false); // 发送A频率指令
        } else if (standard == 5) { // 1080I使用B频率
            Command.get().getTrigger_video().Bfrequence(frequency, false); // 发送B频率指令
        }

        triggerVideo.setTriggerSource(source); // 更新模型中的触发源
        triggerVideo.setPolarity(polar); // 更新模型中的极性
        triggerVideo.setStandard(standard); // 更新模型中的视频标准
        triggerVideo.setVideoTrigger(TopMatchTrigger.triggerVideoTriggerToScope(standard, trigger)); // 更新模型中的触发点（UI索引转示波器索引）
        triggerVideo.setVideoFrequency(TopMatchTrigger.triggerVideoFrequencyToScope(standard, frequency)); // 更新模型中的频率（UI索引转示波器索引）
        triggerVideo.setLine(line); // 更新模型中的行号
    }

    /**
     * 获取触发点选项数组资源ID
     * @return 触发点选项数组资源ID
     */
    private int getPointArrayResId() { // 获取触发点数组资源ID
        return triggerChoice == TRIGGERCHOICEMORE ? R.array.triggerVideoTriggerMore : R.array.triggerVideoTriggerLess; // 多选项或少选项
    }

    /**
     * 获取频率选项数组资源ID
     * @return 频率选项数组资源ID
     */
    private int getFrequencyArrayResId() { // 获取频率数组资源ID
        return frequencyChoice == FREQUENCYCHOICEMORE ? R.array.triggerVideoFrequencyMore : R.array.triggerVideoFrequencyLess; // 多选项或少选项
    }

    /**
     * 获取视频触发详情消息
     * @return 视频触发详情消息对象
     */
    public TopMsgTriggerVideo getMsgTriggerDetail() { // 获取触发详情消息
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
            if(TriggerFactory.getTriggerType() == triggerVideo.getTriggerType()){ // 当前触发类型为视频时
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
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_VIDEO) { // 非本Fragment触发的通道变更
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) { // 触发源索引不同时
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber()); // 更新触发源选中项
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber())); // 更新缓存
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected()); // 更新消息中的触发源
                    Command.get().getTrigger_video().Source(rgSource.getSelected().getIndex(),false); // 发送触发源指令
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
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerVideo, true); // 标记本Fragment缓存已加载
        }
    };

    /**
     * 右侧电平变更事件消费者，同步触发源和极性
     */
    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() { // 右侧电平消费者
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception { // 接收事件
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发类型索引
            if (triggerIndex == TopLayoutTrigger.DETAIL_VIDEO) { // 当前为视频触发时
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) { // 触发源不同时
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect()); // 更新触发源
                    onCheckChanged(rgSource, rgSource.getSelected(), false); // 触发变更处理
                }
                if ((msgLevel.getTopSelect() == 0) != (rgPolar.getSelected().getIndex() == 0)) { // 极性方向不同时
                    rgPolar.setSelectedIndex(msgLevel.getTopSelect() == 0 ? 0 : 1); // 更新极性
                    onCheckChanged(rgPolar, rgPolar.getSelected(), false); // 触发变更处理
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
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_SOURCE: { // 触发源变更指令
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 触发源不同时
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新触发源
                        onCheckChanged(rgSource, rgSource.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_POLAR: { // 极性变更指令
                    if (rgPolar.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 极性不同时
                        rgPolar.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新极性
                        onCheckChanged(rgPolar, rgPolar.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_STANDARD: { // 视频标准变更指令
                    if (rgStandard.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 视频标准不同时
                        rgStandard.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新视频标准
                        onCheckChanged(rgStandard, rgStandard.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_AMODE: { // A模式触发点变更指令
                    int param= Integer.parseInt(commandMsgToUI.getParam()); // 解析参数
                    if (triggerChoice==TRIGGERCHOICELESS){ // 当前为少选项模式时
                        param-=2; //因为合并模式，5个变3个，还是前面的两个消逝，下面的频率不需要，是因为频率消失的是后面3个，不影响索引
                    }
                        rgTrigger.setSelectedIndex(param); // 更新触发点
                        onCheckChanged(rgTrigger, rgTrigger.getSelected(), false); // 触发变更处理
                    break; // 跳出
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_BMODE: { // B模式触发点变更指令
                    if (rgTrigger.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())
                            /*&& triggerChoice == TRIGGERCHOICELESS*/) { // 触发点不同时
                        rgTrigger.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新触发点
                        onCheckChanged(rgTrigger, rgTrigger.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_AFREQUENCE: { // A频率变更指令
                    if (rgFrequency.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())
                            /*&& frequencyChoice == FREQUENCYCHOICELESS*/) { // 频率不同时
                        rgFrequency.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新频率
                        onCheckChanged(rgFrequency, rgFrequency.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_BFREQUENCE: { // B频率变更指令
                    if (rgFrequency.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())
                            /*&& frequencyChoice == FREQUENCYCHOICEMORE*/) { // 频率不同时
                        rgFrequency.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新频率
                        onCheckChanged(rgFrequency, rgFrequency.getSelected(), false); // 触发变更处理
                    }
                    break; // 跳出
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_LINE: { // 行号变更指令
                    if (!tvLine.getText().toString().equals(commandMsgToUI.getParam())
                            && layoutLine.getVisibility() == View.VISIBLE) { // 行号不同且行号布局可见时
                        onTextChanged(tvLine, commandMsgToUI.getParam(), false); // 触发文本变更处理
                    }
                    break; // 跳出
                }
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
                int source = triggerVideo.getTriggerSource(); // 从模型获取触发源
                if (rgSource.getSelected().getIndex() != source) { // 触发源不同时
                    rgSource.setSelectedIndex(source); // 更新触发源
                    onCheckChanged(rgSource, rgSource.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                int polarity = triggerVideo.getPolarity(); // 从模型获取极性
                if (rgPolar.getSelected().getIndex() != polarity) { // 极性不同时
                    rgPolar.setSelectedIndex(polarity); // 更新极性
                    onCheckChanged(rgPolar, rgPolar.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                int standard = triggerVideo.getStandard(); // 从模型获取视频标准
                if (rgStandard.getSelected().getIndex() != standard) { // 视频标准不同时
                    rgStandard.setSelectedIndex(standard); // 更新视频标准
                    onCheckChanged(rgStandard, rgStandard.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                int trigger = TopMatchTrigger.triggerVideoTriggerFromScope(standard, triggerVideo.getVideoTrigger()); // 从模型获取触发点（示波器索引转UI索引）
                if (rgTrigger.getSelected().getIndex() != trigger) { // 触发点不同时
                    rgTrigger.setSelectedIndex(trigger); // 更新触发点
                    onCheckChanged(rgTrigger, rgTrigger.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                int frequency = TopMatchTrigger.triggerVideoFrequencyFromScope(standard, triggerVideo.getVideoFrequency()); // 从模型获取频率（示波器索引转UI索引）
                if (rgFrequency.getSelected().getIndex() != frequency) { // 频率不同时
                    rgFrequency.setSelectedIndex(frequency); // 更新频率
                    onCheckChanged(rgFrequency, rgFrequency.getSelected(), true); // 触发变更处理（来自EventBus）
                }
                String line = String.valueOf(triggerVideo.getLine()); // 从模型获取行号
                if (!tvLine.getText().toString().equals(line)) { // 行号不同时
                    onTextChanged(tvLine, line, true); // 触发文本变更处理（来自EventBus）
                }
            }
        }
    };

    /**
     * 点击事件监听器，处理行号的点击弹窗
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            ScreenUtil.getViewLocation(v); // 获取视图屏幕位置
            int maxCount = 625; // 默认最大行号
            int index = rgStandard.getSelected().getIndex(); // 获取当前视频标准索引
            if (index == 0 || index == 1) { // PAL/PAL-M
                maxCount = 625; // 最大行号625

            } else if (index == 2) { // NTSC
                maxCount = 525; // 最大行号525

            } else if (index == 3) { // SECAM
                maxCount = 750; // 最大行号750

            } else if (index == 4 || index == 5) { // 720P/1080I
                maxCount = 1125; // 最大行号1125

            }
            countDialog.setData(context.getString(R.string.triggervideo_linehead), // 设置计数弹窗标题
                    Integer.parseInt(tvLine.getText().toString()), maxCount, onCountDismissListener); // 设置当前值、最大值和关闭监听
        }
    };

    /**
     * 计数弹窗关闭监听器，将选择结果应用到行号
     */
    private TopDialogCount.OnDismissListener onCountDismissListener = new TopDialogCount.OnDismissListener() { // 计数弹窗关闭监听
        @Override
        public void onDismiss(int result) { // 弹窗关闭回调
            onTextChanged(tvLine, String.valueOf(result), false); // 更新行号
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
        Tools.PrintControlsLocation("TopLayoutTriggerVideo", rootView); // 打印控件位置调试信息
        if (view.getId() == R.id.triggerSource) { // 触发源变更时
            Command.get().getTrigger_video().Source(item.getIndex(), false); // 发送触发源指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex())); // 更新缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerVideo.setTriggerSource(item.getIndex()); // 更新模型中的触发源
            }
            msgTriggerDetail.setTriggerSource(item); // 更新消息中的触发源
            sendMsg(isFromEventBus); // 发送消息
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL, // 发送触发通道变更事件
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_VIDEO, item.getIndex(),isFromEventBus)); // 携带视频类型、通道索引和EventBus标记
        } else if (view.getId() == R.id.polar) { // 极性变更时
            Command.get().getTrigger_video().Polarity(item.getIndex(), false); // 发送极性指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_POLAR, String.valueOf(item.getIndex())); // 更新缓存
            if (!isFromEventBus) { // 非EventBus来源时
                triggerVideo.setPolarity(item.getIndex()); // 更新模型中的极性
            }
            msgTriggerDetail.setPolar(item); // 更新消息中的极性
            sendMsg(isFromEventBus); // 发送消息
        } else if (view.getId() == R.id.standard) { // 视频标准变更时
            Command.get().getTrigger_video().Standard(item.getIndex(), false); // 发送视频标准指令到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_STANDARD, String.valueOf(item.getIndex())); // 更新缓存
            switch (item.getIndex()) { // 根据视频标准设置触发点和频率选项模式
                case 0: // PAL
                case 1: // PAL-M
                case 2: // NTSC
                    triggerChoice = TRIGGERCHOICEMORE; // 触发点多选项
                    rgFrequency.setVisibility(View.GONE); // 隐藏频率选项
                    break; // 跳出
                case 3: // SECAM
                    triggerChoice = TRIGGERCHOICELESS; // 触发点少选项
                    frequencyChoice = FREQUENCYCHOICELESS; // 频率少选项
                    rgFrequency.setVisibility(View.VISIBLE); // 显示频率选项
                    break; // 跳出
                case 4: // 720P
                    triggerChoice = TRIGGERCHOICEMORE; // 触发点多选项
                    frequencyChoice = FREQUENCYCHOICELESS; // 频率少选项
                    rgFrequency.setVisibility(View.VISIBLE); // 显示频率选项
                    break; // 跳出
                case 5: // 1080I
                    triggerChoice = TRIGGERCHOICELESS; // 触发点少选项
                    frequencyChoice = FREQUENCYCHOICEMORE; // 频率多选项
                    rgFrequency.setVisibility(View.VISIBLE); // 显示频率选项
                    break; // 跳出
            }
            rgTrigger.setData(R.string.triggerVideotrigger, getPointArrayResId(), onCheckChangedListener); // 更新触发点数据
            rgFrequency.setData(R.string.triggerVideoFrequency, getFrequencyArrayResId(), onCheckChangedListener); // 更新频率数据
            int trigger = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_TRIGGER + item.getIndex()); // 从缓存读取该标准的触发点
            int frequency = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + item.getIndex()); // 从缓存读取该标准的频率
            int line = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_LINE + item.getIndex()); // 从缓存读取该标准的行号
            rgTrigger.setSelectedIndex(trigger); // 设置触发点选中项
            rgFrequency.setSelectedIndex(frequency); // 设置频率选中项
            tvLine.setText(String.valueOf(line)); // 设置行号文本
            layoutLine.setVisibility(rgTrigger.getArray().length - 1 == rgTrigger.getSelected().getIndex() ? View.VISIBLE : View.GONE); // 最后一个触发点选项时显示行号

            Command.get().getTrigger_video().Amode(trigger,false); // 发送A模式触发点指令
            Command.get().getTrigger_video().Afrequence(frequency,false); // 发送A频率指令

            if (!isFromEventBus) { // 非EventBus来源时
                triggerVideo.setStandard(item.getIndex()); // 更新模型中的视频标准
                triggerVideo.setVideoTrigger(TopMatchTrigger.triggerVideoTriggerToScope(item.getIndex(), rgTrigger.getSelected().getIndex())); // 更新模型中的触发点
                triggerVideo.setVideoFrequency(TopMatchTrigger.triggerVideoFrequencyToScope(item.getIndex(), rgFrequency.getSelected().getIndex())); // 更新模型中的频率
                triggerVideo.setLine(line); // 更新模型中的行号
            }
            RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPTRIGGER_VIDEO); // 发送控件可见性变更事件
            msgTriggerDetail.setStandard(item); // 更新消息中的视频标准
            msgTriggerDetail.setTrigger(rgTrigger.getSelected()); // 更新消息中的触发点
            msgTriggerDetail.setFrequency(rgFrequency.getSelected()); // 更新消息中的频率
            msgTriggerDetail.setLineDetail(tvLine.getText().toString()); // 更新消息中的行号
            sendMsg(isFromEventBus); // 发送消息
        } else if (view.getId() == R.id.trigger) { // 触发点变更时
            Command.get().getTrigger_video().Amode(item.getIndex(),false); // 发送A模式触发点指令

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_TRIGGER + rgStandard.getSelected().getIndex(), String.valueOf(item.getIndex())); // 更新缓存（按标准区分）
            String[] point = getResources().getStringArray(getPointArrayResId()); // 获取当前触发点选项数组
            layoutLine.setVisibility(point.length - 1 == item.getIndex() ? View.VISIBLE : View.GONE); // 最后一个触发点选项时显示行号
            if (!isFromEventBus) { // 非EventBus来源时
                triggerVideo.setVideoTrigger(TopMatchTrigger.triggerVideoTriggerToScope(rgStandard.getSelected().getIndex(), rgTrigger.getSelected().getIndex())); // 更新模型中的触发点
            }
            RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPTRIGGER_VIDEO); // 发送控件可见性变更事件
            msgTriggerDetail.setTrigger(item); // 更新消息中的触发点
            sendMsg(isFromEventBus); // 发送消息
        } else if (view.getId() == R.id.frequency) { // 频率变更时
            Command.get().getTrigger_video().Afrequence(item.getIndex(), false); // 发送A频率指令

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + rgStandard.getSelected().getIndex(), String.valueOf(item.getIndex())); // 更新缓存（按标准区分）
            if (!isFromEventBus) { // 非EventBus来源时
                triggerVideo.setVideoFrequency(TopMatchTrigger.triggerVideoFrequencyToScope(rgStandard.getSelected().getIndex(), rgFrequency.getSelected().getIndex())); // 更新模型中的频率
            }
            msgTriggerDetail.setFrequency(item); // 更新消息中的频率
            sendMsg(isFromEventBus); // 发送消息
        }
    }

    /**
     * 处理文本变更，同步行号到硬件/缓存/模型
     * @param tv 变更的文本视图
     * @param result 新的文本值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) { // 文本变更处理
        if (tv.getId() == tvLine.getId()) { // 行号变更时
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_LINE + rgStandard.getSelected().getIndex(), result); // 更新缓存（按标准区分）
            if (!isFromEventBus) { // 非EventBus来源时
                triggerVideo.setLine(Integer.parseInt(result)); // 更新模型中的行号
            }
            tvLine.setText(result); // 更新UI文本
            msgTriggerDetail.setLineDetail(result); // 更新消息中的行号
            sendMsg(isFromEventBus); // 发送消息
            Command.get().getTrigger_video().Line(Integer.parseInt(result), false); // 发送行号指令到硬件
        }
    }
}
