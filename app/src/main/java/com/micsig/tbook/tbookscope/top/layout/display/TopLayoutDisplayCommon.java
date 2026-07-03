package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.os.Handler; // 导入Handler类，用于延迟消息处理
import android.os.Message; // 导入Message类，用于Handler消息传递
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.SeekBar; // 导入滑动条控件
import android.widget.TextView; // 导入文本视图控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具
import com.micsig.tbook.scope.Data.WaveData; // 导入波形数据类
import com.micsig.tbook.scope.Display.Display; // 导入显示控制单例
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入事件UI观察者
import com.micsig.tbook.scope.Scope; // 导入示波器单例
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道类
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴控制类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone; // 导入顶部右侧隐藏消息
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令发送单例
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息转UI消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleSegmented; // 导入采样分段消息
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState; // 导入分段状态消息
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard; // 导入浮动键盘对话框
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理器
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 导入光标管理器
import com.micsig.tbook.tbookscope.wavezone.display.RulerManage; // 导入标尺管理器
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 导入波形管理器
import com.micsig.tbook.ui.MSwitchBox; // 导入自定义开关控件
import com.micsig.tbook.ui.top.view.TopViewSeekBar; // 导入自定义滑动条视图
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件
import com.micsig.tbook.ui.util.TBookUtil; // 导入TBook工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道枚举

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 常规(Common)子页面Fragment                     ║
 * ║  核心职责: 管理Display-常规页面的UI交互、命令下发和状态同步                   ║
 * ║  架构设计: 子Fragment，通过OnDetailSendMsgListener向父Fragment传递数据       ║
 * ║  数据流向: UI控件 → onCheckChanged/onStateChanged → Command → 底层硬件     ║
 * ║  依赖关系: Command、Display、RxBus、CacheUtil、WorkModeManage等            ║
 * ║  使用场景: 用户在Display-常规页面操作水平参考/时基/滚动/CCT/透明度等          ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by Administrator on 2017/4/6.
 */

public class TopLayoutDisplayCommon extends Fragment { // 常规显示子页面Fragment，管理水平参考/时基/滚动/CCT等配置

    private static final String TAG = "TopLayoutDisplayCommon"; // 日志标签
    private Context context; // Fragment关联的Activity上下文
    private TopViewRadioGroup rgHorRef; // 水平参考单选组
    private TopViewRadioGroup rgDisplayMode; // 时基模式单选组（YT/XY）
    private MSwitchBox sbRoll; // 滚动屏幕开关
    private MSwitchBox sbCCT; // CCT（点击选择）开关
    private MSwitchBox sbScale; // 标尺显示开关
    private TextView editPosition; // 时间位置编辑框
    private TopDialogFloatKeyBoard dialogFloatKeyBoard; // 浮动键盘对话框

    private TopMsgDisplayCommon displayDetail; // 常规显示详情数据模型
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器
    private boolean sampleSegmentedOpen = false; // 采样分段是否开启
    private boolean isAutoSaveStart = false; // 自动保存是否启动

    private TopViewSeekBar displayTransparency; // 波形透明度滑动条

    /**
     * 创建Fragment视图，加载布局文件
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的视图
     */
    @Nullable // 标记返回值可为空
    @Override // 重写onCreateView方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图
        return inflater.inflate(R.layout.layout_displaycommon, container, false); // 加载常规显示布局文件
    }

    /**
     * 视图创建完成后的初始化操作
     * @param view Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 重写onViewCreated方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取关联的Activity上下文
        initView(view); // 初始化视图
        initData(); // 初始化数据
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化数据，从UI控件读取当前值并填充到详情数据模型
     */
    private void initData() { // 初始化数据
        displayDetail = new TopMsgDisplayCommon(); // 创建常规显示详情对象
        displayDetail.setHorRef(rgHorRef.getSelected()); // 设置水平参考通道
        displayDetail.setTimeBase(rgDisplayMode.getSelected()); // 设置时基模式
        displayDetail.setRoll(sbRoll.isState()); // 设置滚动开关状态
        displayDetail.setCct(sbCCT.isState()); // 设置CCT开关状态
        displayDetail.setAlpha(0.8f); // 设置默认透明度
    }

    /**
     * 初始化视图组件，绑定控件和监听器
     * @param view Fragment的根视图
     */
    private void initView(View view) { // 初始化视图
        rgHorRef = (TopViewRadioGroup) view.findViewById(R.id.displayHorRef); // 获取水平参考单选组
        rgHorRef.setData(R.string.displayHorRef, R.array.displayHorRef, onCheckChangedListener); // 设置数据和监听器
        rgDisplayMode = (TopViewRadioGroup) view.findViewById(R.id.displayTimebase); // 获取时基模式单选组
        rgDisplayMode.setData(R.string.displayTimebase, R.array.displayTimebase, onCheckChangedListener); // 设置数据和监听器
        sbRoll = (MSwitchBox) view.findViewById(R.id.displayRoll); // 获取滚动开关
        sbRoll.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置开关状态变化监听器
        sbCCT = (MSwitchBox) view.findViewById(R.id.displayCCT); // 获取CCT开关
        sbCCT.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置开关状态变化监听器
        displayTransparency = view.findViewById(R.id.transparency); // 获取透明度滑动条
        sbScale = (MSwitchBox) view.findViewById(R.id.displayScale); // 获取标尺显示开关
        sbScale.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置开关状态变化监听器

        displayTransparency.setData(R.string.view_display_transparency, 100, 30, seekBarChangeListener); // 设置透明度滑动条数据
        displayTransparency.setVisibility(View.GONE); // 隐藏透明度滑动条

        editPosition = view.findViewById(R.id.edit_time_pos); // 获取时间位置编辑框
        editPosition.setOnClickListener(onClickListener); // 设置点击监听器
        dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard); // 获取浮动键盘对话框
    }

    /**
     * 透明度滑动条变化监听器，实时更新透明度并发送命令
     */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() { // 滑动条变化监听器
        @Override // 重写onProgressChanged方法
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { // 波形亮度调节
            displayDetail.setAlpha(progress/100.0f); // 更新透明度值
            sendMsg(false); // 发送消息
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ALPHA, String.valueOf(progress)); // 缓存透明度值
        }

        @Override // 重写onStartTrackingTouch方法
        public void onStartTrackingTouch(SeekBar seekBar) { // 开始触摸滑动条

        }

        @Override // 重写onStopTrackingTouch方法
        public void onStopTrackingTouch(SeekBar seekBar) { // 停止触摸滑动条

        }
    };

    /**
     * 初始化事件控制，订阅RxBus事件和EventBus观察者
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令转UI事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerSampleSegment); // 订阅采样分段事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_TIMEBASE).subscribe(consumerUpdateTimeBase); // 订阅时基更新事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION).subscribe(consumerMouseClick); // 订阅鼠标点击位置事件
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE).subscribe(consumerAutoSaveTaskState); // 订阅自动保存状态事件
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_MODE, eventUIObserver); // 注册显示模式事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_HORREF, eventUIObserver); // 注册水平参考事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_CCT, eventUIObserver); // 注册CCT事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_SET_CH_TIMEPOS, eventUIObserver); // 注册通道时间位置事件观察者
    }

    /**
     * 从缓存恢复常规显示页面的所有配置状态
     */
    private void setCache() { // 设置缓存
        int horRef = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_HORREF); // 获取缓存的水平参考索引
        int displayMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE); // 获取缓存的时基模式索引
        int roll = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL); // 获取缓存的滚动开关状态
        int cct = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_CCT); // 获取缓存的CCT开关状态
        int scale=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_SCALE); // 获取缓存的标尺开关状态
        int alpha = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ALPHA); // 获取缓存的透明度值
        alpha=0; // 强制透明度重置为0（暂不使用缓存值）

        rgHorRef.setSelectedIndex(horRef); // 设置水平参考选中项
        rgDisplayMode.setSelectedIndex(displayMode); // 设置时基模式选中项
        sbRoll.setState(roll == 0); // 设置滚动开关状态（0=开启）
        sbCCT.setState(cct == 0); // 设置CCT开关状态（0=开启）
        sbScale.setState(scale==0); // 设置标尺开关状态（0=开启）
        displayTransparency.setProgress(alpha); // 设置透明度滑动条进度

        Command.get().getDisplay().HorRef(horRef, false); // 下发水平参考命令
        Command.get().getDisplay().setRoutineTimeBaseMode(displayMode, false); // 下发时基模式命令
        Command.get().getDisplay().setRoutineRollingScreen(roll, false); // 下发滚动屏幕命令
        Command.get().getDisplay().Cct(cct==0, false); // 下发CCT命令

        int mode = 1; // 时基模式默认值（YT+非滚动）
        if (roll != 0) { // 滚动模式关闭时
            mode = displayMode == 0 ? 0 : 2; // YT模式=0，XY模式=2
        }
        Command.get().getTimebase().Mode(mode, false); // 下发时基模式命令

        Display.getInstance().setHorRef(horRef); // 更新Display单例的水平参考
        Display.getInstance().setDisplayMode(displayMode); // 更新Display单例的显示模式
        Display.getInstance().setRoll(roll == 0); // 更新Display单例的滚动状态
        Display.getInstance().setCCT(cct == 0); // 更新Display单例的CCT状态
        RulerManage.getIns().setShow(scale==0); // 设置标尺显示状态

        WaveManage.get().setClickSelectEnable(cct != 0); // 设置波形点击选择使能

        displayDetail.setHorRef(rgHorRef.getSelected()); // 更新详情数据-水平参考
        displayDetail.setTimeBase(rgDisplayMode.getSelected()); // 更新详情数据-时基模式
        displayDetail.setRoll(sbRoll.isState()); // 更新详情数据-滚动状态
        displayDetail.setCct(sbCCT.isState()); // 更新详情数据-CCT状态
        displayDetail.setAlpha(alpha/100.0f); // 更新详情数据-透明度
        sendMsg(false); // 发送消息通知主界面

        CommandMsgToUI msgToUI_H = new CommandMsgToUI(); // 创建水平光标消息
        CommandMsgToUI msgToUI_V = new CommandMsgToUI(); // 创建垂直光标消息
        if (displayMode == 0) { // YT模式
            boolean isZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM); // 获取缩放状态
            if (!isZoom) { // 非缩放模式
                //YT
                WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, false); // 设置YT工作模式
                CursorManage.getInstance().initXY(); // 初始化光标

                boolean cursorH = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORH_VISIBLE); // 获取YT水平光标可见性
                boolean cursorV = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORV_VISIBLE); // 获取YT垂直光标可见性
                msgToUI_H.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL); // 设置水平光标标志
                msgToUI_H.setParam(String.valueOf(cursorH)); // 设置水平光标参数
                msgToUI_V.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL); // 设置垂直光标标志
                msgToUI_V.setParam(String.valueOf(cursorV)); // 设置垂直光标参数
            }
        } else { // XY模式
            //XY
            WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_XY, false); // 设置XY工作模式
            CursorManage.getInstance().initXY(); // 初始化光标
            boolean cursorH_xy = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORH_VISIBLE); // 获取XY水平光标可见性
            boolean cursorV_xy = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORV_VISIBLE); // 获取XY垂直光标可见性
            msgToUI_H.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL); // 设置水平光标标志
            msgToUI_H.setParam(String.valueOf(cursorH_xy)); // 设置水平光标参数
            msgToUI_V.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL); // 设置垂直光标标志
            msgToUI_V.setParam(String.valueOf(cursorV_xy)); // 设置垂直光标参数
        }
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI_H); // 发送水平光标消息
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI_V); // 发送垂直光标消息
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE, new TopMsgSegmentedState(false)); // 发送采样分段状态
    }

    /**
     * 获取常规显示详情数据
     * @return 常规显示详情数据模型
     */
    public TopMsgDisplayCommon getDisplayDetail() { // 获取详情数据
        return displayDetail; // 返回详情数据
    }

    /**
     * 设置详情消息发送监听器
     * @param onDetailSendMsgListener 监听器实例
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息发送监听器
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 赋值监听器
    }

    /**
     * 通过监听器发送消息到父Fragment
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) { // 发送消息
        if (onDetailSendMsgListener != null) { // 监听器不为空
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用监听器回调
        }
    }

    /**
     * 缓存加载事件消费者，恢复缓存状态并标记加载完成
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override // 重写accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 处理缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutDisplayCommon, true); // 标记常规页面缓存已加载
        }
    };

    /**
     * 命令转UI事件消费者，处理底层命令回传的UI更新
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令转UI消费者
        @Override // 重写accept方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 处理命令转UI事件
            switch (commandMsgToUI.getFlag()) { // 根据消息标志分发处理
                case CommandMsgToUI.FLAG_DISPLAY_HORREF: // 水平参考命令
                    rgHorRef.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 设置水平参考选中项
                    onCheckChanged(rgHorRef, rgHorRef.getSelected(), false); // 触发切换处理
                    break; // 退出case
                case CommandMsgToUI.FLAG_DISPLAY_TIMEBASE: // 时基模式命令
                    rgDisplayMode.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 设置时基模式选中项
                    onCheckChanged(rgDisplayMode, rgDisplayMode.getSelected(), false); // 触发切换处理
                    break; // 退出case
                case CommandMsgToUI.FLAG_TIMEBASE_MODE: // 时基模式命令（含滚动）
                    int mode = Integer.parseInt(commandMsgToUI.getParam()); // 获取模式值
                    switch (mode) { // 根据模式值分发
                        case 0: // YT+滚动
                        case 2: { // XY+滚动
                            int selectIndex = mode == 0 ? 0 : 1; // 映射为显示模式索引
                            rgDisplayMode.setSelectedIndex(selectIndex); // 设置选中项
                            onCheckChanged(rgDisplayMode, rgDisplayMode.getSelected(), false); // 触发切换处理
                        }
                        break; // 退出case
                        case 1: { // YT+非滚动
                            sbRoll.setState(mode == 1); // 设置滚动开关状态
                            onStateChanged(sbRoll, sbRoll.isState(), false); // 触发状态变化处理
                        }
                        break; // 退出case

                    }
                    break; // 退出case
                case CommandMsgToUI.FLAG_DISPLAY_CCT: // CCT命令
                    sbCCT.setState(Boolean.parseBoolean(commandMsgToUI.getParam())); // 设置CCT开关状态
                    onStateChanged(sbCCT, sbCCT.isState(), false); // 触发状态变化处理
                    break; // 退出case
                case CommandMsgToUI.FLAG_TIMEBASE_ROLL:{ // 时基滚动命令
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析布尔值
                    sbRoll.setState(b); // 设置滚动开关状态
                    onStateChanged(sbRoll, sbRoll.isState(), false); // 触发状态变化处理
                }break; // 退出case
            }
        }
    };

    /**
     * 采样分段事件消费者，根据分段状态更新UI使能
     */
    private Consumer<TopMsgSampleSegmented> consumerSampleSegment = new Consumer<TopMsgSampleSegmented>() { // 采样分段消费者
        @Override // 重写accept方法
        public void accept(TopMsgSampleSegmented msgSampleSegmented) throws Exception { // 处理采样分段事件
            boolean isOpen = msgSampleSegmented.getState().isValue(); // 获取分段开启状态
            sampleSegmentedOpen = isOpen; // 更新分段开启标记
            rgDisplayMode.setEnabled(!sampleSegmentedOpen && !isAutoSaveStart); // 更新时基模式使能状态
            sbRoll.setEnabled(!isOpen); // 更新滚动开关使能状态
            sendMsg(msgSampleSegmented.isFromEventBus()); // 发送消息
        }
    };


    private MainTopMsgRightGone msgTopGone = new MainTopMsgRightGone(); // 顶部右侧隐藏消息对象

    /**
     * 开关状态变化监听器，播放音效并触发状态变化处理
     */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() { // 开关状态变化监听器
        @Override // 重写onToggleStateChanged方法
        public void onToggleStateChanged(MSwitchBox view, boolean state) { // 处理开关状态变化
            PlaySound.getInstance().playButton(); // 播放按键音效
            onStateChanged(view, state, false); // 触发状态变化处理
        }
    };

    /**
     * 处理开关状态变化，根据不同开关ID执行对应逻辑
     * @param view 触发变化的开关视图
     * @param state 新的开关状态
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onStateChanged(MSwitchBox view, boolean state, boolean isFromEventBus) { // 处理开关状态变化
        if (view.getId() == R.id.displayRoll) { // 滚动开关变化
            if (Tools.isSlowTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE))) { // 慢时基模式
                if (state) { // 开启滚动
                    msgTopGone.setVisible(false); // 设置右侧不可见
                    RxBus.getInstance().post(RxEnum.MAIN_TOPRIGHT_GONE, msgTopGone); // 发送右侧隐藏消息
                    if (ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate()) // 当前激活通道是FFT
                            || ChannelFactory.isRefCh(ChannelFactory.getChActivate())) { // 或是参考通道
                        msgTopGone.setVisible(true); // 设置右侧可见
                        RxBus.getInstance().post(RxEnum.MAIN_TOPCENTER_TEXT_GONE, msgTopGone); // 发送中间文本隐藏消息
                    }
                } else { // 关闭滚动
                    msgTopGone.setVisible(true); // 设置右侧可见
                    RxBus.getInstance().post(RxEnum.MAIN_TOPRIGHT_GONE, msgTopGone); // 发送右侧显示消息
                }
            } else { // 非慢时基模式
                msgTopGone.setVisible(true); // 设置右侧可见
                RxBus.getInstance().post(RxEnum.MAIN_TOPRIGHT_GONE, msgTopGone); // 发送右侧显示消息
            }

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL, String.valueOf(state ? 0 : 1)); // 缓存滚动状态
            displayDetail.setRoll(state); // 更新详情数据
            sendMsg(isFromEventBus); // 发送消息
            Command.get().getDisplay().setRoutineRollingScreen(state ? 0 : 1, false); // 下发滚动屏幕命令
            if (!isFromEventBus) { // 非EventBus来源
                if(!state) { // 关闭滚动时
                    long l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL); // 获取缓存的时间位置
                    HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - l); // 设置水平轴时间位置
                }
                Display.getInstance().setRoll(state); // 更新Display单例滚动状态
            }
            if (Scope.getInstance().isZoom() && Scope.getInstance().isRun() && Scope.getInstance().isInScrollMode()) { // 缩放+运行+滚动模式
                RxBus.getInstance().post(RxEnum.ROLL_RUN_ZOOM, false); // 发送滚动运行缩放消息
            }
            RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE, new TopMsgSegmentedState(isFromEventBus)); // 发送采样分段状态
            Command.get().getTimebase().Roll_Display(sbRoll.isState(),false); // 下发滚动显示命令
        } else if (view.getId() == R.id.displayCCT) { // CCT开关变化
            WaveManage.get().setClickSelectEnable(!state); // 设置波形点击选择使能（CCT开启时禁用点击选择）
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_CCT, String.valueOf(state ? 0 : 1)); // 缓存CCT状态
            displayDetail.setCct(state); // 更新详情数据
            sendMsg(isFromEventBus); // 发送消息
            Command.get().getDisplay().Cct(state, false); // 下发CCT命令
            if (!isFromEventBus) { // 非EventBus来源
                Display.getInstance().setCCT(state); // 更新Display单例CCT状态
            }
        }else if (view.getId() == R.id.displayScale){ // 标尺开关变化
            RulerManage.getIns().setShow(state); // 设置标尺显示状态
            WorkModeManage.getInstance().refresh(); // 刷新工作模式
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_SCALE, String.valueOf(state ? 0 : 1)); // 缓存标尺状态

        }

    }

    /**
     * 单选组选中变化监听器，播放音效并触发选中变化处理
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组变化监听器
        @Override // 重写onClickSound方法
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override // 重写onPrompt方法
        public void onPrompt(TopViewRadioGroup view) { // 提示回调

        }

        @Override // 重写onClick方法
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 点击回调
            onCheckChanged(view, item, false); // 触发选中变化处理
        }
    };

    /**
     * 处理单选组选中变化，根据不同单选组ID执行对应逻辑
     * @param view 触发变化的单选组视图
     * @param item 选中的通道数据Bean
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 处理选中变化
        if (view.getId() == R.id.displayHorRef) { // 水平参考变化
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_HORREF, String.valueOf(item.getIndex())); // 缓存水平参考索引
            displayDetail.setHorRef(item); // 更新详情数据
            sendMsg(isFromEventBus); // 发送消息
            Command.get().getDisplay().HorRef(item.getIndex(), false); // 下发水平参考命令
            if (!isFromEventBus) { // 非EventBus来源
                Display.getInstance().setHorRef(item.getIndex()); // 更新Display单例水平参考
            }
        } else if (view.getId() == R.id.displayTimebase) { // 时基模式变化
            if (item.getIndex() == displayDetail.getTimeBase().getIndex()) { // 模式未变化
                return; // 直接返回
            }
            if (item.getIndex() == 1) { // 切换到XY模式
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch1, String.valueOf(CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION + TChan.Ch1))); // 缓存CH1的YT位置
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch2, String.valueOf(CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION + TChan.Ch2))); // 缓存CH2的YT位置
                WaveManage.get().setCenterChY(TChan.Ch1); // CH1居中
                WaveManage.get().setCenterChY(TChan.Ch2); // CH2居中
            }
            Message msg = new Message(); // 创建Handler消息
            msg.what = MSG_CHANGE_PREV; // 设置消息类型为切换预览
            msg.obj = item; // 携带选中项数据
            msg.arg1 = isFromEventBus ? 1 : 0; // 携带EventBus标记
            handler.sendMessageDelayed(msg, MSG_DELAY); // 延迟发送消息
        }
    }

    private static final int MSG_CHANGE_PREV = 888; // Handler消息类型：切换预览
    private static final int MSG_CHANGE_NEXT = 889; // Handler消息类型：切换下一步
    private static final int MSG_DELAY = 10; // Handler延迟时间（毫秒）

    /**
     * Handler处理延迟消息，用于时基模式切换的异步处理
     */
    private Handler handler = new Handler() { // Handler实例
        @Override // 重写handleMessage方法
        public void handleMessage(@NonNull Message msg) { // 处理消息
            super.handleMessage(msg); // 调用父类处理
            switch (msg.what) { // 根据消息类型分发
                case MSG_CHANGE_PREV: // 切换预览
                    TopBeanChannel item = (TopBeanChannel) msg.obj; // 获取选中项
                    boolean isFromEventBus = msg.arg1 == 1; // 获取EventBus标记
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE, String.valueOf(item.getIndex())); // 缓存时基模式索引
                    displayDetail.setTimeBase(item); // 更新详情数据
                    sendMsg(isFromEventBus); // 发送消息
                    CommandMsgToUI msgToUI_H = new CommandMsgToUI(); // 创建水平光标消息
                    CommandMsgToUI msgToUI_V = new CommandMsgToUI(); // 创建垂直光标消息
                    if (item.getIndex() == 0) { // YT模式
                        //YT
//                    WorkModeManage.getInstance().setmWorkMode(IWorkMode.WorkMode_YT);
                        WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, isFromEventBus); // 设置YT工作模式
                        CursorManage.getInstance().initXY(); // 初始化光标
                        if (!isFromEventBus) { // 非EventBus来源
                            Display.getInstance().setDisplayMode(Display.DISPLAY_YT); // 更新Display单例显示模式为YT
                        }

                        boolean cursorH = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORH_VISIBLE); // 获取YT水平光标可见性
                        boolean cursorV = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORV_VISIBLE); // 获取YT垂直光标可见性
                        msgToUI_H.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL); // 设置水平光标标志
                        msgToUI_H.setParam(String.valueOf(cursorH)); // 设置水平光标参数
                        msgToUI_V.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL); // 设置垂直光标标志
                        msgToUI_V.setParam(String.valueOf(cursorV)); // 设置垂直光标参数
                    } else { // XY模式
                        //XY
//                    WorkModeManage.getInstance().setmWorkMode(IWorkMode.WorkMode_XY);
                        WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_XY, isFromEventBus); // 设置XY工作模式
                        CursorManage.getInstance().initXY(); // 初始化光标
                        if (!isFromEventBus) { // 非EventBus来源
                            Display.getInstance().setDisplayMode(Display.DISPLAY_XY); // 更新Display单例显示模式为XY
                            Scope scope = Scope.getInstance(); // 获取示波器实例
                            if(scope.isRun()){ // 示波器正在运行
                                scope.setRun(true); // 重新启动运行
                            }
                        }
                        boolean cursorH_xy = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORH_VISIBLE); // 获取XY水平光标可见性
                        boolean cursorV_xy = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORV_VISIBLE); // 获取XY垂直光标可见性
                        msgToUI_H.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL); // 设置水平光标标志
                        msgToUI_H.setParam(String.valueOf(cursorH_xy)); // 设置水平光标参数
                        msgToUI_V.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL); // 设置垂直光标标志
                        msgToUI_V.setParam(String.valueOf(cursorV_xy)); // 设置垂直光标参数
                    }
                    RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI_H); // 发送水平光标消息
                    RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI_V); // 发送垂直光标消息
                    Command.get().getDisplay().setRoutineTimeBaseMode(item.getIndex(), false); // 下发时基模式命令
                    if (item.getIndex() == 0) { // YT模式下需要恢复通道Y位置
                        handler.sendEmptyMessageDelayed(MSG_CHANGE_NEXT, MSG_DELAY); // 延迟发送下一步消息
                    }
                    RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE, new TopMsgSegmentedState(isFromEventBus)); // 发送采样分段状态
                    Command.get().getTimebase().Mode(item.getIndex(),false); // 下发时基模式命令
                    break; // 退出case
                case MSG_CHANGE_NEXT: // 切换下一步（恢复YT模式通道Y位置）
                    WaveManage.get().setPositionY(TChan.Ch1, CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch1)); // 恢复CH1的Y位置
                    WaveManage.get().setPositionY(TChan.Ch2, CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch2)); // 恢复CH2的Y位置
                    break; // 退出case
            }
        }
    };

    /**
     * 事件UI观察者，监听底层事件并同步更新UI状态
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 事件UI观察者
        @Override // 重写update方法
        public void update(Object data) { // 处理事件更新
            if (((EventBase) data).getId() == EventFactory.EVENT_DISPLAY_MODE) { // 显示模式事件
                if (rgDisplayMode.getSelected().getIndex() != Display.getInstance().getDisplayMode()) { // UI与底层状态不一致
                    rgDisplayMode.setSelectedIndex(Display.getInstance().getDisplayMode()); // 同步UI选中项
                    onCheckChanged(rgDisplayMode, rgDisplayMode.getSelected(), true); // 触发切换处理（EventBus来源）
                    int mode = Display.DISPLAY_YT == Display.getInstance().getDisplayMode() ? 0 : 2; // 映射模式值
                    Command.get().getTimebase().Mode(mode, false); // 下发时基模式命令
                }
            } else if (((EventBase) data).getId() == EventFactory.EVENT_HORREF) { // 水平参考事件
                if (rgHorRef.getSelected().getIndex() != Display.getInstance().getHorRef()) { // UI与底层状态不一致
                    rgHorRef.setSelectedIndex(Display.getInstance().getHorRef()); // 同步UI选中项
                    onCheckChanged(rgHorRef, rgHorRef.getSelected(), true); // 触发切换处理（EventBus来源）
                }
            } else if (((EventBase) data).getId() == EventFactory.EVENT_DISPLAY_CCT) { // CCT事件
                if ((sbCCT.isState()) != Display.getInstance().isCCT()) { // UI与底层状态不一致
                    sbCCT.setState(Display.getInstance().isCCT()); // 同步UI开关状态
                    onStateChanged(sbCCT, sbCCT.isState(), true); // 触发状态变化处理（EventBus来源）
                }
            } else if (((EventBase) data).getId() == EventFactory.EVENT_SET_CH_TIMEPOS) { // 通道时间位置事件
                int chActive = ChannelFactory.getChActivate(); // 获取当前激活通道
                double timePos = (double) ((EventBase) data).getData(); // 获取时间位置值
                Command.get().getTimebase().Position(timePos, true);//CH // 下发CH通道时间位置命令

                TChan.foreachMath(mathChan -> {//FFT // 遍历数学通道
                    if(ChannelFactory.isChOpen(TChan.toFpgaChNo(mathChan)) && ChannelFactory.isMath_FFT_Ch(TChan.toFpgaChNo(mathChan))){ // FFT通道打开且为FFT类型
                        Command.get().getMath_fft().Position(TChan.toFpgaChNo(mathChan), timePos, true); // 下发FFT时间位置命令
                        ((MainActivity) context).getSlider().updateUI(); // 更新滑块UI
                    }
                });

                TChan.foreachRef(refChan -> { // 遍历参考通道
                    int refIndex = TChan.toFpgaChNo(refChan); // 获取FPGA通道号
                    RefChannel refChannel = ChannelFactory.getRefChannel(refIndex); // 获取参考通道对象
                    if (refChannel == null || ChannelFactory.isChOpen(refIndex)) return; // 通道为空或已打开则跳过
                    if (refChannel.getRefType() != WaveData.FFT_WAVE) { // 非FFT波形参考通道
                        Command.get().getReference().Timebase_Position(refIndex, timePos, true); // 下发参考通道时间位置命令
                        ((MainActivity) context).getSlider().updateUI(); // 更新滑块UI
                    }
                });

                if (ChannelFactory.isRefCh(chActive)) {//最后原来的Ref // 当前激活通道是参考通道
                    RefChannel refChannel = ChannelFactory.getRefChannel(chActive); // 获取参考通道对象
                    if (refChannel == null) return; // 通道为空则返回
                    if (refChannel.getRefType() != WaveData.FFT_WAVE) { // 非FFT波形参考通道
                        Command.get().getReference().Timebase_Position(chActive, timePos, true); // 下发参考通道时间位置命令
                        ((MainActivity) context).getSlider().updateUI(); // 更新滑块UI
                    }
                }
            }
        }
    };

    /**
     * 视图点击监听器，处理时间位置编辑框点击
     */
    private final View.OnClickListener onClickListener = new View.OnClickListener() { // 点击监听器
        @Override // 重写onClick方法
        public void onClick(View v) { // 处理点击事件
            switch (v.getId()) { // 根据视图ID分发
                case R.id.edit_time_pos: // 时间位置编辑框
                    clickTimepos(); // 调用时间位置点击处理
                    break; // 退出case
                default: // 其他视图
                    break; // 退出case
            }
        }
    };

    /**
     * 处理时间位置编辑框点击，弹出浮动键盘输入时间位置值
     */
    private void clickTimepos() { // 点击时间位置
        if(rgDisplayMode.getSelected().getIndex() == 1) return; // XY模式下不允许编辑时间位置
        String txt = editPosition.getText().toString().replaceAll("(?:s|\\s)", ""); // 移除单位s和空格
        dialogFloatKeyBoard.setFloatData(txt, editPosition, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置浮动键盘数据
            @Override // 重写onDismiss方法
            public void onDismiss(View fromView, String show) { // 键盘关闭回调
                PlaySound.getInstance().playButton(); // 播放按键音效
                double val = TBookUtil.getBigDoubleFromM(show); // 将输入字符串转为数值
                Logger.d(TAG, "result= " + show + " ,val= " + val); // 打印日志
                setTimePos(val); // 设置时间位置
            }
        });
    }


    /**
     * 设置时间位置值，根据当前激活通道类型下发对应命令
     * @param timePos 时间位置值
     */
    private void setTimePos(Double timePos) { // 设置时间位置
        int chActive = ChannelFactory.getChActivate(); // 获取当前激活通道
        Logger.d(TAG, "setTimePos= " + timePos + " ,chActive= " + chActive); // 打印日志
        if (ChannelFactory.isMath_FFT_Ch(chActive)) { // FFT通道
            Command.get().getMath_fft().Position(chActive, timePos, true); // 下发FFT时间位置命令
            ((MainActivity) context).getSlider().updateUI(); // 更新滑块UI
        }
        if (ChannelFactory.isDynamic_or_math_Ch(chActive) || ChannelFactory.isSerialCh(chActive) || ChannelFactory.isRefCh(chActive)) { // 动态/数学/串口/参考通道
            Command.get().getTimebase().Position(timePos, true); // 下发CH时间位置命令
            ((MainActivity) context).getSlider().updateUI(); // 更新滑块UI
        }
    }

    /**
     * 时基更新事件消费者，更新时间位置编辑框显示
     */
    private final Consumer<String> consumerUpdateTimeBase = new Consumer<String>() { // 时基更新消费者
        @Override // 重写accept方法
        public void accept(String triggerTimeBaseInfo) throws Throwable { // 处理时基更新事件
            Logger.d(TAG, "chActive= " + ChannelFactory.getChActivate() + " ,timePosInfo =" + triggerTimeBaseInfo); // 打印日志
            int refTimeBaseIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 获取参考时基索引
            String timePos = triggerTimeBaseInfo.split(";")[0]; // 解析时间位置字符串
            int chIdx = Integer.parseInt(triggerTimeBaseInfo.split(";")[1]); // 解析通道索引
            if (refTimeBaseIndex == 0) { // 跟随模式
                editPosition.setText(timePos); // 更新编辑框文本
            } else { // 独立模式
//                if (chIdx != ChannelFactory.getChActivate() || !ChannelFactory.isDynamic_or_math_Ch(chIdx)) return;
                if (chIdx != ChannelFactory.getChActivate()) return; // 非当前激活通道则跳过
                editPosition.setText(timePos); // 更新编辑框文本
            }
        }
    };


    /**
     * 鼠标点击位置事件消费者，处理顶部触发位置点击
     */
    private Consumer<String> consumerMouseClick = new Consumer<String>() { // 鼠标点击位置消费者
        @Override // 重写accept方法
        public void accept(String clickInfo) throws Throwable { // 处理鼠标点击事件
            String[] info = clickInfo.split(";"); // 解析点击信息
            int chIdx = Integer.parseInt(info[0]); // 获取通道索引
            int clickPos = Integer.parseInt(info[1]);//0垂直档位  1垂直位置  2水平挡位  3水平位置 4点击的是触发位置 // 获取点击位置类型
            Logger.d(TAG, "editPosition ClickInfo chidx= " + chIdx + " ,clickPos= " + clickPos); // 打印日志
            if (clickPos == 4) { //顶部触发位置
                clickTimepos(); // 触发时间位置编辑
            }
        }
    };

    /**
     * 自动保存状态事件消费者，更新时基模式使能状态
     */
    private final Consumer<Boolean> consumerAutoSaveTaskState = new Consumer<Boolean>() { // 自动保存状态消费者
        @Override // 重写accept方法
        public void accept(Boolean isAutoSaveStop) throws Throwable { // 处理自动保存状态事件
            requireActivity().runOnUiThread(() -> { // 切换到UI线程
                Logger.i("limh isAutoSaveStop= " + isAutoSaveStop); // 打印日志
                isAutoSaveStart = !isAutoSaveStop; // 更新自动保存启动标记
                rgDisplayMode.setEnabled(!isAutoSaveStart && !sampleSegmentedOpen); // 更新时基模式使能状态
            });
        }
    };

}
