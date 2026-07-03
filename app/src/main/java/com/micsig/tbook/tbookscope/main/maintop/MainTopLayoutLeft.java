package com.micsig.tbook.tbookscope.main.maintop; // 定义主顶部左侧布局类的包路径

import android.content.Context; // 导入Android上下文类
import android.graphics.Bitmap; // 导入位图类
import android.graphics.Color; // 导入颜色类
import android.graphics.Rect; // 导入矩形类
import android.util.AttributeSet; // 导入属性集类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.widget.AbsoluteLayout; // 导入绝对布局类
import android.widget.CheckBox; // 导入复选框类
import android.widget.ImageView; // 导入图像视图类
import android.widget.RelativeLayout; // 导入相对布局类
import android.widget.TextView; // 导入文本视图类

import com.micsig.base.OEM; // 导入OEM配置类
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入事件UI观察者
import com.micsig.tbook.scope.Scope; // 导入示波器核心类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.channel.IChannel; // 导入通道接口
import com.micsig.tbook.scope.channel.MathChannel; // 导入数学通道类
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道类
import com.micsig.tbook.tbookscope.LoadCache; // 导入加载缓存类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主活动类
import com.micsig.tbook.tbookscope.MainMsgSlip; // 导入主滑块消息类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.broadcastreceiver.BroadcastManager; // 导入广播管理器类
import com.micsig.tbook.tbookscope.main.dialog.DialogOk; // 导入确认对话框类
import com.micsig.tbook.tbookscope.main.maincenter.MainLeftMsgMenuRunStop; // 导入左侧菜单运行停止消息类
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterMenuCommand; // 导入中心菜单命令消息类
import com.micsig.tbook.ui.wavezone.IChan; // 导入通道接口
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令类
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum; // 导入消息队列枚举
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChActiveChange; // 导入通道激活变更消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava总线
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister; // 导入RxJava注册类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava枚举
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleMode; // 导入采样模式消息
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState; // 导入分段状态消息
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean; // 导入工作模式数据类
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.micsig.tbook.ui.util.TBookUtil; // 导入示波器工具类
import com.micsig.tbook.ui.wavezone.IWave; // 导入波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道工具类

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * Created by yangj on 2017/5/24.
 */

/*
+===================================================================================+
|                              MainTopLayoutLeft                                    |
+===================================================================================+
| 模块定位：示波器主界面顶部左侧区域布局组件                                        |
| 核心职责：显示运行状态、采样信息、Logo图标、测量菜单入口                          |
| 架构设计：继承AbsoluteLayout，采用RxJava响应式编程处理消息总线事件                |
| 数据流向：接收运行状态变更 → 更新状态文本颜色 → 接收采样信息 → 更新显示数据        |
| 依赖关系：依赖RxBus消息总线、Scope示波器核心类、ChannelFactory通道工厂            |
|           OEM品牌配置、MainViewGroup主视图组                                      |
| 使用场景：在示波器运行时显示当前运行/停止/自动状态、存储深度、采样率等信息        |
+===================================================================================+
*/

/**
 * 示波器顶部左侧布局类，负责运行状态和采样信息显示
 *
 * 该类继承自AbsoluteLayout，作为示波器主界面顶部左侧区域的容器布局。
 * 主要功能包括：
 * 1. 显示Logo品牌图标
 * 2. 显示示波器运行/停止/自动状态
 * 3. 显示当前通道的存储深度和采样率
 * 4. 显示采样模式类型
 * 5. 提供测量菜单入口复选框
 * 6. 处理广播接收器（电池、网络等状态，已注释）
 *
 * @author yangj
 * @version 1.0
 * @since 2017/5/24
 */
public class MainTopLayoutLeft extends AbsoluteLayout {
    private static final String TAG = "MainTopLayoutLeft"; // 日志标签常量，用于标识本类
    private Context context; // Android上下文对象，用于访问资源和系统服务
    private RelativeLayout dataLayout; // 数据布局容器，包含存储深度和采样率显示
    private TextView tvState, tvHigh, tvSd, tvLength, tvRate, tvSample; // 状态、高刷新率、SD卡、存储深度、采样率、采样模式文本视图
    private CheckBox mainTopMeasureMenu; // 测量菜单入口复选框
    private ImageView ivLogo; // Logo图标视图
    private MainViewGroup mainViewGroup; // 主视图组引用，用于获取滑块菜单状态
//
//    private ImageView tvUsbPcLink, tvUDisk, tvWifi, tvInternet;
//    private BatteryView tvBattery;
//    private TextView tvTime;
    private DialogOk dialogOk; // 确认对话框引用（已注释）

    /**
     * 单参数构造方法
     *
     * @param context Android上下文对象
     */
    public MainTopLayoutLeft(Context context) {
        this(context, null); // 调用双参数构造方法，传入null属性集
    }

    /**
     * 双参数构造方法
     *
     * @param context Android上下文对象
     * @param attrs XML属性集，从布局文件中解析的属性
     */
    public MainTopLayoutLeft(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 调用三参数构造方法，传入默认样式属性
    }

    /**
     * 三参数构造方法（完整构造方法）
     *
     * 初始化布局视图和事件监听器，是类的核心初始化入口
     *
     * @param context Android上下文对象，用于访问资源和系统服务
     * @param attrs XML属性集，从布局文件中解析的属性
     * @param defStyleAttr 默认样式属性，用于定义视图的默认样式
     */
    public MainTopLayoutLeft(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类AbsoluteLayout的构造方法
        this.context = context; // 保存上下文对象到成员变量
        initView(attrs, defStyleAttr); // 初始化视图布局和控件
        initControl(); // 初始化事件监听器和消息订阅
    }

    /**
     * 初始化视图布局和控件
     *
     * 从XML布局文件加载视图，并初始化各种控件。
     * 设置Logo图标和点击监听器。
     *
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_maintop_left, this); // 从布局文件加载视图到本布局
        dataLayout = findViewById(R.id.dataLayout); // 查找并初始化数据布局容器
        ivLogo = (ImageView) findViewById(R.id.logo); // 查找并初始化Logo图标视图
        tvState = (TextView) findViewById(R.id.state); // 查找并初始化状态文本视图
        tvHigh = (TextView) findViewById(R.id.high); // 查找并初始化高刷新率文本视图（未使用）
        tvLength = (TextView) findViewById(R.id.length); // 查找并初始化存储深度文本视图
        tvRate = (TextView) findViewById(R.id.rate); // 查找并初始化采样率文本视图
        tvSample = (TextView) findViewById(R.id.peak); // 查找并初始化采样模式文本视图

        mainTopMeasureMenu=findViewById(R.id.mainTopMeasureMenu); // 查找并初始化测量菜单复选框
//        tvUsbPcLink = (ImageView) findViewById(R.id.usbPcLink);
//        tvUDisk = (ImageView) findViewById(R.id.uDisk);
//        tvBattery = (BatteryView) findViewById(R.id.battery);
//        tvTime = (TextView) findViewById(R.id.time);
//        tvWifi = findViewById(R.id.wifi);
//        tvInternet = findViewById(R.id.lan);
//
        Bitmap bitmap = OEM.getLogo(); // 从OEM配置获取Logo位图
        if (bitmap != null) { // 如果Logo位图不为空
            ivLogo.setImageBitmap(bitmap); // 设置Logo图标
        }

        ivLogo.setOnClickListener(this::onImageLogoClickEvent); // 设置Logo点击监听器，使用Lambda表达式引用方法
    }

    /**
     * 设置主视图组引用
     *
     * 由MainActivity调用，传入主视图组实例，
     * 用于后续获取滑块菜单显示状态。
     *
     * @param mainViewGroup 主视图组对象
     */
    public void setMainViewGroup(MainViewGroup mainViewGroup){
        this.mainViewGroup=mainViewGroup; // 保存主视图组引用到成员变量
    }

    /**
     * 处理Logo图标点击事件
     *
     * 当用户点击Logo图标时，发送返回主页命令消息，
     * 通过RxBus通知主菜单返回主页。
     *
     * @param view 点击的视图对象
     */
    private void onImageLogoClickEvent(View view) {
        MainMsgCenterMenuCommand command= new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandReturnHome); // 创建返回主页命令消息
        RxBus.getInstance().post(RxEnum.MainLeft_To_Menu_Command,command); // 发送命令消息到RxJava总线
    }

    /**
     * 设置广播接收器
     *
     * 初始化各种状态指示器的广播接收器（电池、网络、U盘、WiFi、USB）。
     * 目前已全部注释，不再使用广播接收器。
     *
     * @param mainActivity 主活动对象
     */
    public void setBroadcastReceiver(MainActivity mainActivity) {
//        BroadcastManager.getInstance().init(mainActivity);
//        dialogOk = mainActivity.getMainViewGroup().findViewById(R.id.dialogOk);
//        BroadcastManager.getInstance().setBatteryControl(tvBattery, tvTime, dialogOk);
//        BroadcastManager.getInstance().setInternetControl(tvInternet);
//        BroadcastManager.getInstance().setUDiskControl(tvUDisk);
//        BroadcastManager.getInstance().setWifiControl(tvWifi);
//        BroadcastManager.getInstance().setUsbControl(tvUsbPcLink);
    }

    /**
     * 取消注册广播接收器
     *
     * 在活动销毁时调用，取消注册所有广播接收器。
     * 目前已全部注释。
     */
    public void unBroadcastReceiver() {
        BroadcastManager.getInstance().unregisterReceiver(); // 取消注册广播接收器
    }

    /**
     * 初始化事件监听器和消息订阅
     *
     * 通过RxBus订阅各种消息事件，包括运行停止状态变更、
     * 自动模式变更、采样模式变更、缓存加载、工作模式切换、
     * 滑块菜单变更、通道激活变更等。
     * 同时注册EventFactory的事件观察者。
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_RUNSTOP).subscribe(consumerMainLeftMenu); // 订阅运行停止菜单消息
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_AUTO).subscribe(consumerMainLeftMenu_Auto); // 订阅自动模式菜单消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLEMODE).subscribe(consumerTopLayoutSampleMode); // 订阅采样模式消息
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载消息
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange); // 订阅工作模式变更消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE).subscribe(consumerSegmentedState); // 订阅分段状态消息
        RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_TO_OTHER).subscribe(consumerSlipMenuChange); // 订阅滑块菜单变更消息
        RxBus.getInstance().getObservable(RxEnum.TOPSLIP_TITLE).subscribe(consumerTopSlipTitle); // 订阅滑块标题消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_MEASURE).subscribe(consumerTopSlipTitle); // 订阅测量布局消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAVE).subscribe(consumerTopSlipTitle); // 订阅保存布局消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_CURSOR).subscribe(consumerTopSlipTitle); // 订阅光标布局消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLE).subscribe(consumerTopSlipTitle); // 订阅采样布局消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopSlipTitle); // 订阅显示布局消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTopSlipTitle); // 订阅触发布局消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_AUTO).subscribe(consumerTopSlipTitle); // 订阅自动布局消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_USERSET).subscribe(consumerTopSlipTitle); // 订阅用户设置布局消息
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange); // 订阅通道激活变更消息
        //RxBus.getInstance().getObservable(RxEnum.BOTTOMLAYOUT_HIGHREFRESH).subscribe(consumerBottomHighRefresh);
        EventFactory.addEventObserver(EventFactory.EVENT_UI_DEPTH_SAMPFRE_REFLASH, uiObserver); // 注册存储深度采样率刷新事件观察者
    }



    /**
     * 检查触摸事件是否在测量菜单复选框矩形区域内
     *
     * 当触摸抬起时，判断触摸点是否在测量菜单复选框的可见矩形区域内，
     * 用于确定触摸事件是否应该由复选框处理。
     *
     * @param event 触摸事件对象
     * @return 如果触摸在测量菜单复选框可见区域内返回true，否则返回false
     */
    public boolean containsRect(MotionEvent event){
        if (MotionEvent.ACTION_UP==event.getAction()){ // 仅处理触摸抬起事件
            int x=(int)event.getRawX(); // 获取触摸点的原始X坐标
            int y=(int)event.getRawY(); // 获取触摸点的原始Y坐标
            Rect r1=new Rect(); // 创建矩形对象

            mainTopMeasureMenu.getGlobalVisibleRect(r1); // 获取测量菜单复选框的全局可见矩形区域
            if (r1.contains(x,y) && mainTopMeasureMenu.getVisibility()==View.VISIBLE){ // 如果坐标在矩形内且复选框可见
                return true; // 返回true，表示触摸在区域内
            }else {
                return false; // 返回false，表示触摸不在区域内
            }
        }else {
            return false; // 对于非抬起事件返回false
        }

    }

    /**
     * 设置缓存配置
     *
     * 根据缓存配置更新UI显示状态。
     * 目前已全部注释，不再更新网络状态指示器。
     */
    private void setCache() {
//        tvInternet.setVisibility(View.GONE);
//        tvWifi.setVisibility(View.GONE);
//        switch (WifiChangedReceiver.isNetworkAvailable(context)) {
//            case 1:
//                tvInternet.setVisibility(View.VISIBLE);
//                break;
//            case 2:
//                tvWifi.setVisibility(View.VISIBLE);
//                break;
//            case 0:
//                tvInternet.setVisibility(View.GONE);
//                tvWifi.setVisibility(View.GONE);
//                break;
//        }
//        boolean visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 0;
//        dataLayout.setVisibility(visible ? VISIBLE : INVISIBLE);
    }

    /**
     * 处理通道激活变更事件
     *
     * 当通道激活状态变更时，更新存储深度、采样率和采样模式文本的颜色，
     * 并显示当前通道的采样率和存储深度信息。
     *
     * @param obj 消息对象，包含通道激活变更信息
     */
    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum= RxBusRegister.parseMqEnum(obj); // 解析消息队列枚举类型
        if (mqEnum!=MQEnum.CH_ACTIVE)return; // 如果不是通道激活消息则直接返回

        IChan ch = ((MsgChActiveChange)obj).getChan(); // 获取激活的通道对象
        int color; // 定义颜色变量
        if (ch == IChan.CH_NULL) { // 如果是空通道
            color = Color.TRANSPARENT; // 设置为透明色
        } else if (IChan.isR1ToR8(ch)) { // 如果是参考通道R1-R8
            color = TChan.getChannelColor(context,TChan.RefActive); // 获取参考通道激活颜色
        } else { // 如果是普通通道
            color = TChan.getChannelColor(context,TChan.toUiChNo(ch.getValue())); // 根据通道号获取通道颜色
        }

        tvLength.setTextColor(color); // 设置存储深度文本颜色
        tvRate.setTextColor(color); // 设置采样率文本颜色
        tvSample.setTextColor(color); // 设置采样模式文本颜色
        displayRateLength(ch.getValue()+1); // 显示采样率和存储深度信息
    }

    /**
     * 显示采样率和存储深度信息
     *
     * 根据通道类型决定是否显示采样率和存储深度信息。
     * 对于串行通道不显示，对于其他通道显示当前的存储深度和采样率。
     *
     * @param ch 通道索引（1-based）
     */
    private void displayRateLength(int ch) {
        if (TChan.isSerial(ch) ) { // 如果是串行通道
            tvLength.setText(""); // 清空存储深度文本
            tvRate.setText(""); // 清空采样率文本
        } else { // 对于其他通道类型
            int len = getDepthLen(); // 获取存储深度长度
            tvLength.setText(TBookUtil.getFourFromD((double) len)); // 设置存储深度文本（格式化为4位有效数字）
            double dx = getSampRate(); // 获取采样率
            tvRate.setText(TBookUtil.getFourFromD(dx) + "Sa/s"); // 设置采样率文本（格式化并添加单位）
        }
    }

    /**
     * 获取存储深度长度
     *
     * 根据当前激活的通道类型获取存储深度（波形长度）。
     * 对于动态通道使用示波器的存储深度，对于数学通道和参考通道使用通道的波形长度。
     *
     * @return 存储深度长度，如果通道为空返回0
     */
    private int getDepthLen() {
        IChannel channel = ChannelFactory.getInstance().getWaveChannel(); // 获取当前波形通道
        int len = 0; // 初始化长度为0
        if (channel != null) { // 如果通道不为空
            int chId = channel.getChId(); // 获取通道ID
            //Logger.i("current ch=ch"+(chId+1));
            if (ChannelFactory.isDynamicCh(chId)) // 如果是动态通道
                len = (int) Scope.getInstance().zunMemDepth(); // 使用示波器的存储深度
            else if (ChannelFactory.isMathCh(chId)) // 如果是数学通道
                len = ((MathChannel) channel).getWaveLen(); // 使用数学通道的波形长度
            else // 如果是参考通道
                len = ((RefChannel) channel).getWaveLen(); // 使用参考通道的波形长度
        }
        return len; // 返回存储深度长度
    }

    /**
     * 获取采样率
     *
     * 从当前激活的通道获取采样率。
     *
     * @return 采样率，如果通道为空返回0
     */
    private double getSampRate() {
        IChannel channel = ChannelFactory.getInstance().getWaveChannel(); // 获取当前波形通道
        if (channel != null) { // 如果通道不为空
            return channel.getSampleRate2display(); // 返回通道的显示采样率
        }
        return 0; // 如果通道为空返回0
    }


    /**
     * 运行停止菜单消息消费者
     *
     * 当收到运行停止状态变更消息时，根据状态更新状态文本和颜色，
     * 并发送相应的触发器状态命令。
     * 状态包括RUN、WAIT、STOP三种。
     */
    private Consumer<MainLeftMsgMenuRunStop> consumerMainLeftMenu = new Consumer<MainLeftMsgMenuRunStop>() {
        @Override
        public void accept(MainLeftMsgMenuRunStop mainLeftMsgMenuRunStop) throws Exception {

            if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.RUN) { // 如果是RUN状态
                if (Scope.getInstance().isAuto()) { // 如果示波器处于自动模式
//                    Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.AUTO);
                    return; // 直接返回，不处理
                }
                Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.RUN); // 发送RUN状态命令到触发器
                tvState.setText(getResources().getString(R.string.mainTopLeftStateRun)); // 设置状态文本为"Run"
                tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run)); // 设置状态文本颜色为绿色
            } else if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.WAIT) { // 如果是WAIT状态
                if (Scope.getInstance().isAuto()) { // 如果示波器处于自动模式
//                    Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.AUTO);
                    return; // 直接返回，不处理
                }
                if (Scope.getInstance().isRun()) { // 如果示波器正在运行
                    Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.WAIT); // 发送WAIT状态命令到触发器
                    tvState.setText(getResources().getString(R.string.mainTopLeftStateWait)); // 设置状态文本为"Wait"
                    tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_wait)); // 设置状态文本颜色为黄色
                } else { // 如果示波器停止运行
                    Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.STOP); // 发送STOP状态命令到触发器
                    tvState.setText(getResources().getString(R.string.mainTopLeftStateStop)); // 设置状态文本为"Stop"
                    tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop)); // 设置状态文本颜色为红色
                }
            } else if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.STOP) { // 如果是STOP状态
                Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.STOP); // 发送STOP状态命令到触发器
                tvState.setText(getResources().getString(R.string.mainTopLeftStateStop)); // 设置状态文本为"Stop"
                tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop)); // 设置状态文本颜色为红色
            }
        }
    };

    /**
     * 自动模式菜单消息消费者
     *
     * 当收到自动模式变更消息时，根据自动模式开启状态更新状态文本和颜色。
     * 自动模式开启时显示"Auto"和蓝色，关闭时显示"Run"或"Stop"。
     */
    private Consumer<Boolean> consumerMainLeftMenu_Auto = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean b) throws Exception {
            if (b) { // 如果自动模式开启
                tvState.setText(getResources().getString(R.string.mainTopLeftStateAuto)); // 设置状态文本为"Auto"
                tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_auto)); // 设置状态文本颜色为蓝色
            } else { // 如果自动模式关闭
//                tvState.setText(getResources().getString(R.string.mainTopLeftStateRun));
//                tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));
                if (Scope.getInstance().isRun()) { // 如果示波器正在运行
                    tvState.setText(getResources().getString(R.string.mainTopLeftStateRun)); // 设置状态文本为"Run"
                    tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run)); // 设置状态文本颜色为绿色
                } else { // 如果示波器停止运行
                    tvState.setText(getResources().getString(R.string.mainTopLeftStateStop)); // 设置状态文本为"Stop"
                    tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop)); // 设置状态文本颜色为红色
                }
            }
        }
    };

    /**
     * 采样模式消息消费者
     *
     * 当收到采样模式变更消息时，根据采样模式类型更新采样模式文本显示。
     * 不同采样模式索引对应不同的文本格式。
     */
    private Consumer<TopMsgSampleMode> consumerTopLayoutSampleMode = new Consumer<TopMsgSampleMode>() {
        @Override
        public void accept(TopMsgSampleMode msgSample) throws Exception {
            switch (msgSample.getSample().getIndex()) { // 根据采样模式索引分支处理
                case 0: // 等效采样模式
                case 3: // 其他采样模式
                    tvSample.setText(msgSample.getSample().getText()); // 设置采样模式文本
                    break;
                case 1: // 实时采样模式
                case 2: // 其他实时采样模式
                    if (!StrUtil.isEmpty(msgSample.getSample().getSimpleText())) { // 如果有简短文本
                        tvSample.setText(msgSample.getDetail() + msgSample.getSample().getSimpleText()); // 使用详细文本加简短文本
                    } else {
                        tvSample.setText(msgSample.getDetail() + msgSample.getSample().getText()); // 使用详细文本加完整文本
                    }
                    break;
            }

        }
    };

    /**
     * 工作模式变更消息消费者
     *
     * 当工作模式变更时，更新测量菜单复选框的可见性。
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            setMainTopMeasureMenuVisible(); // 更新测量菜单复选框可见性
        }
    };

    /**
     * 分段状态消息消费者
     *
     * 当收到分段状态消息时，检查串行文本模式是否开启，
     * 并更新测量菜单复选框的可见性。
     */
    private Consumer<TopMsgSegmentedState> consumerSegmentedState = new Consumer<TopMsgSegmentedState>() {
        @Override
        public void accept(TopMsgSegmentedState msgSegmentedState) throws Exception {
            // 串型文本打开与关闭，是通过分段存储消息过来的
            setMainTopMeasureMenuVisible(); // 更新测量菜单复选框可见性
        }
    };

    /**
     * 设置测量菜单复选框的选中状态
     *
     * 检查主滑块菜单是否显示以及测量布局是否显示，
     * 如果都显示则设置复选框为选中状态。
     */
    private void setCheckMeasureState(){
        boolean isOpenMainMenu=mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP); // 获取顶部滑块菜单是否显示
        boolean isMeasure= mainViewGroup.getMainMenu().isShowLayoutMeasureCommon(); // 获取测量布局是否显示
        mainTopMeasureMenu.setChecked( isOpenMainMenu&& isMeasure ); // 设置复选框选中状态
    }

    /**
     * 滑块菜单变更消息消费者
     *
     * 当收到滑块菜单变更消息时，更新测量菜单复选框的选中状态。
     */
    private Consumer<MainMsgSlip> consumerSlipMenuChange = new Consumer<MainMsgSlip>() {
        @Override
        public void accept(MainMsgSlip mainMsgSlip) throws Exception {
            setCheckMeasureState(); // 更新测量菜单复选框选中状态
        }
    };

    /**
     * 顶部滑块标题消息消费者
     *
     * 当收到顶部滑块标题消息时，更新测量菜单复选框的选中状态。
     * 用于监听各种布局变更（测量、保存、光标、采样、显示、触发、自动、用户设置）。
     */
    private Consumer<Object> consumerTopSlipTitle =new Consumer<Object>() {
        @Override
        public void accept(Object obj) throws Exception {
            setCheckMeasureState(); // 更新测量菜单复选框选中状态
        }
    };

    /**
     * 设置测量菜单复选框的可见性和可用状态
     *
     * 根据工作模式和串行文本模式决定测量菜单复选框是否显示。
     * 在XY模式或串行文本模式时隐藏测量菜单复选框。
     */
    private void setMainTopMeasureMenuVisible(){
        boolean visible = WorkModeManage.getInstance().isXyMode(); // 获取是否为XY模式
        boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT); // 获取串行文本模式开关
        boolean result= visible || isSerialsTxt; // 计算结果：如果XY模式或串行文本模式
        mainTopMeasureMenu.setVisibility(result ? GONE : VISIBLE); // 如果结果为true则隐藏，否则显示
        mainTopMeasureMenu.setEnabled(!result); // 如果结果为true则禁用，否则启用

    }

    /**
     * 缓存加载消息消费者
     *
     * 当收到缓存加载消息时，调用setCache方法更新UI配置。
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache(); // 更新缓存配置
        }
    };

    /**
     * 存储深度采样率刷新事件观察者
     *
     * 监听EventFactory的存储深度和采样率刷新事件，
     * 更新存储深度和采样率文本显示。
     */
    EventUIObserver uiObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 获取事件基类对象
            if (eventBase.getId() == EventFactory.EVENT_UI_DEPTH_SAMPFRE_REFLASH) { // 如果是存储深度采样率刷新事件
                int len = getDepthLen(); // 获取存储深度长度
                tvLength.setText(TBookUtil.getFourFromD((double) len)); // 设置存储深度文本
                double dx = getSampRate(); // 获取采样率
                tvRate.setText(TBookUtil.getFourFromD(dx) + "Sa/s"); // 设置采样率文本
                displayRateLength(ChannelFactory.getChActivate() + 1); // 显示采样率和存储深度信息
            }
        }
    };


/*
    private Consumer<MainBottomMsgQuickHighRefresh> consumerBottomHighRefresh = new Consumer<MainBottomMsgQuickHighRefresh>() {
        @Override
        public void accept(MainBottomMsgQuickHighRefresh msgQuickHighRefresh) throws Exception {
            tvHigh.setVisibility(msgQuickHighRefresh.isRefresh() ? View.VISIBLE : View.GONE);
        }
    };
    */

    /**
     * 刷新U盘状态
     * BUG id:7880
     */
//    public void refreshUdiskIcon() {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//            boolean b = UsbUtils.UdiskExist(context);
////            String  s = "usb isExist:"+ b;
////            Logger.i(Command.TAG,s);
//            tvUDisk.setVisibility(b ? View.VISIBLE : View.GONE);
//
//        }
//    }

    /**
     * 重写触摸事件处理方法
     *
     * 消费所有触摸事件，防止事件传递到父布局。
     *
     * @param event 触摸事件对象
     * @return 总是返回true，表示事件被消费
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true; // 返回true，消费触摸事件
    }
}