package com.micsig.tbook.tbookscope.main.maintop; // 定义主顶部右侧按钮布局类的包路径

import android.content.Context; // 导入Android上下文类
import android.graphics.Bitmap; // 导入位图类
import androidx.constraintlayout.widget.ConstraintLayout; // 导入约束布局类
import android.util.AttributeSet; // 导入属性集类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.widget.Button; // 导入按钮类
import android.widget.ImageView; // 导入图像视图类
import android.widget.TextView; // 导入文本视图类

import com.micsig.base.Logger; // 导入基础日志工具
import com.micsig.base.OEM; // 导入OEM配置类
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入事件UI观察者
import com.micsig.tbook.scope.Scope; // 导入示波器核心类
import com.micsig.tbook.tbookscope.LoadCache; // 导入加载缓存类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.main.maincenter.MainLeftMsgMenuRunStop; // 导入左侧菜单运行停止消息类
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterMenuCommand; // 导入中心菜单命令消息类
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令到UI消息类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava枚举
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState; // 导入分段状态消息类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean; // 导入工作模式数据类
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理类

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * @auother Liwb
 * @description:
 * @data:2022-1-11 15:25
 */

/*
+===================================================================================+
|                          MainTopLayoutRightButton                                 |
+===================================================================================+
| 模块定位：示波器主界面顶部右侧按钮区域布局组件                                    |
| 核心职责：显示运行/停止、自动、序列按钮，处理按钮点击和状态更新                    |
| 架构设计：继承ConstraintLayout，采用RxJava响应式编程处理按钮状态变更              |
| 数据流向：接收运行状态变更 → 更新按钮文本颜色 → 用户点击按钮 → 发送命令消息        |
| 依赖关系：依赖RxBus消息总线、Scope示波器核心类、OEM品牌配置                       |
|           MainMsgCenterMenuCommand命令消息类                                      |
| 使用场景：在示波器运行时显示运行/停止/自动/序列按钮，用户通过按钮控制示波器状态    |
+===================================================================================+
*/

/**
 * 示波器顶部右侧按钮布局类，负责运行/停止/自动/序列按钮显示和控制
 *
 * 该类继承自ConstraintLayout，作为示波器主界面顶部右侧按钮区域的容器布局。
 * 主要功能包括：
 * 1. 显示Logo品牌图标
 * 2. 显示运行/停止按钮（RunStop）
 * 3. 显示自动设置按钮（Auto/AutoRange）
 * 4. 显示序列按钮（SEQ）
 * 5. 响应运行状态变更并更新按钮文本和颜色
 * 6. 处理按钮点击事件并发送相应命令
 * 7. 根据工作模式和串行文本模式调整按钮可见性
 *
 * @author Liwb
 * @version 1.0
 * @since 2022-1-11 15:25
 */
public class MainTopLayoutRightButton extends ConstraintLayout {
    private static final String TAG=MainTopLayoutRightButton.class.getSimpleName(); // 日志标签常量，使用类名作为标签

    private Context context; // Android上下文对象，用于访问资源和系统服务
    private Button checkRunStop, checkAuto, checkSeq; // 运行停止按钮、自动按钮、序列按钮
    private ImageView ivLogo; // Logo图标视图
    private TextView txRunStop; // 运行停止状态文本视图

    private int colorGreen= getResources().getColor(R.color.color_maintopleft_state_run); // 绿色常量，用于运行状态和序列选中
    private int colorBlue= getResources().getColor(R.color.main_text_color); // 蓝色常量，用于停止状态和序列未选中

    /**
     * 单参数构造方法
     *
     * @param context Android上下文对象
     */
    public MainTopLayoutRightButton(Context context) {
        this(context, null); // 调用双参数构造方法，传入null属性集
    }

    /**
     * 双参数构造方法
     *
     * @param context Android上下文对象
     * @param attrs XML属性集，从布局文件中解析的属性
     */
    public MainTopLayoutRightButton(Context context, AttributeSet attrs) {
        this(context,attrs, 0); // 调用三参数构造方法，传入默认样式属性
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
    public MainTopLayoutRightButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类ConstraintLayout的构造方法
        this.context = context; // 保存上下文对象到成员变量
        initView(context, attrs, defStyleAttr); // 初始化视图布局和控件
        initControl(); // 初始化事件监听器和消息订阅
    }

    /**
     * 初始化视图布局和控件
     *
     * 从XML布局文件加载视图，并初始化各种控件。
     * 设置Logo图标、按钮点击监听器。
     *
     * @param context Android上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        View view= View.inflate(context, R.layout.layout_maintop_right_button, this); // 从布局文件加载视图到本布局
        ivLogo = (ImageView) view.findViewById(R.id.logo); // 查找并初始化Logo图标视图
        checkRunStop=view.findViewById(R.id.RunStop); // 查找并初始化运行停止按钮
        checkAuto =view.findViewById(R.id.Auto); // 查找并初始化自动按钮
        checkSeq =view.findViewById(R.id.SEQ); // 查找并初始化序列按钮
        txRunStop = view.findViewById(R.id.tx_runstop); // 查找并初始化运行停止状态文本视图


        checkRunStop.setOnClickListener(Onclick); // 设置运行停止按钮点击监听器
        checkAuto.setOnClickListener(Onclick); // 设置自动按钮点击监听器
        checkSeq.setOnClickListener(Onclick); // 设置序列按钮点击监听器

        Bitmap bitmap = OEM.getLogo(); // 从OEM配置获取Logo位图
        if (bitmap != null) { // 如果Logo位图不为空
            ivLogo.setImageBitmap(bitmap); // 设置Logo图标
        }

        ivLogo.setOnClickListener(this::onImageLogoClickEvent); // 设置Logo点击监听器，使用Lambda表达式引用方法
    }

    /**
     * 按钮点击监听器
     *
     * 处理运行停止、自动、序列三个按钮的点击事件。
     * 点击按钮时，发送相应的命令消息到RxJava总线。
     */
    private OnClickListener Onclick=new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){ // 根据按钮ID分支处理
                case R.id.RunStop: // 运行停止按钮
                    sendMsg(new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandRunStop));break; // 发送运行停止命令消息
                case R.id.Auto: // 自动按钮
                    sendMsg(new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandAuto));break; // 发送自动命令消息
                case R.id.SEQ: // 序列按钮
                    checkSeq.setTextColor(colorGreen); // 设置序列按钮文本为绿色（选中状态）
                    sendMsg(new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandSEQ));break; // 发送序列命令消息
            }
        }
    };

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
     * 发送命令消息
     *
     * 通过RxJava总线发送中心菜单命令消息。
     *
     * @param command 命令消息对象
     */
    private void sendMsg(MainMsgCenterMenuCommand command){
        RxBus.getInstance().post(RxEnum.MainLeft_To_Menu_Command,command); // 发送命令消息到RxJava总线
    }


    /**
     * 初始化事件监听器和消息订阅
     *
     * 通过RxBus订阅各种消息事件，包括缓存加载、运行停止状态变更、
     * 自动模式变更、命令到UI、分段状态变更、工作模式变更等。
     * 同时注册EventFactory的事件观察者，监听自动开始/停止、示波器状态、采样有效、单次触发等事件。
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载消息
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCacheEx); // 订阅扩展缓存加载消息
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_RUNSTOP).subscribe(consumerMainLeftMenu); // 订阅运行停止菜单消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_AUTO_CHANGED).subscribe(consumerAutoChanged); // 订阅自动模式变更消息
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE).subscribe(consumerSegmentedState); // 订阅分段状态消息
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange); // 订阅工作模式变更消息
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_START, eventUIObserver); // 注册自动开始事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_STOP, eventUIObserver); // 注册自动停止事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_STATE, eventUIObserver); // 注册示波器状态事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_SAMPLE_VALID, eventUIObserver); // 注册采样有效事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_SINGLE,eventUIObserver); // 注册单次触发事件观察者
    }

    /**
     * 检查触摸事件是否在按钮矩形区域内
     *
     * 目前已全部注释，返回false。
     * 原本用于判断触摸点是否落在三个按钮的矩形区域内。
     *
     * @param event 触摸事件对象
     * @return 总是返回false
     */
    public boolean containsRect(MotionEvent event){
//        if (MotionEvent.ACTION_DOWN==event.getAction()){
//            int x=(int)event.getRawX();
//            int y=(int)event.getRawY();
//            Rect r1=new Rect();
//            Rect r2=new Rect();
//            Rect r3=new Rect();
//            Rect r4=new Rect();
//            Rect r5=new Rect();
//            checkSeq.getGlobalVisibleRect(r1);
//            checkAuto.getGlobalVisibleRect(r2);
//            checkRunStop.getGlobalVisibleRect(r3);
//            checkZoom.getGlobalVisibleRect(r4);
//            if (r1.contains(x,y) ||r2.contains(x,y) || r3.contains(x,y) || r4.contains(x,y)|| r5.contains(x,y) ){
//                return true;
//            }else {
//                return false;
//            }
//        }else {
//            return false;
//        }
        return false; // 返回false，表示不在按钮区域内
    }

    /**
     * 设置缓存配置
     *
     * 从缓存获取运行停止、序列、自动按钮的初始状态。
     * 强制设置序列和自动按钮为关闭状态（false）。
     * 根据自动范围配置设置自动按钮文本（Auto或AutoRange）。
     */
    private void setCache(){
        boolean runStop = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP); // 获取运行停止按钮状态缓存
        boolean seq = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_SEQ); // 获取序列按钮状态缓存
        boolean auto = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_AUTO); // 获取自动按钮状态缓存

        seq = false; // 强制设置序列状态为关闭
        CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_SEQ, "false"); // 保存序列状态到缓存映射
        auto = false; // 强制设置自动状态为关闭
        CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_AUTO, "false"); // 保存自动状态到缓存映射

        if (seq) { // 如果序列状态为开启（实际不会执行，因为上面强制为false）
            this.checkSeq.setTextColor(colorGreen); // 设置序列按钮文本为绿色
        } else { // 如果序列状态为关闭
            this.checkSeq.setTextColor(colorBlue); // 设置序列按钮文本为蓝色
        }
//        if (auto) {
//            this.checkAuto.setTextColor(colorGreen);
//        } else {
//            this.checkAuto.setTextColor(colorBlue);
//        }
        this.checkAuto.setTextColor(colorBlue); // 强制设置自动按钮文本为蓝色

        boolean autoRange= (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_RANGE))==0?true:false; // 获取自动范围配置（0=AutoRange，其他=Auto）
        if (autoRange){ // 如果自动范围开启
            checkAuto.setText(getResources().getString(R.string.mainCenterMenuAutoRange)); // 设置自动按钮文本为"AutoRange"
        }else { // 如果自动范围关闭
            checkAuto.setText(getResources().getString(R.string.mainCenterMenuAuto)); // 设置自动按钮文本为"Auto"
        }
    }

    /**
     * 缓存加载消息消费者
     *
     * 当收到缓存加载消息时，调用setCache方法更新按钮状态配置。
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache o) throws Exception {
            setCache(); // 更新缓存配置
        }
    };

    /**
     * 扩展缓存加载消息消费者
     *
     * 当收到扩展缓存加载消息时，暂不处理。
     */
    private Consumer consumerLoadCacheEx=new Consumer() {
        @Override
        public void accept(Object o) throws Exception {

        }
    };

    /**
     * 设置按钮可见性
     *
     * 根据串行文本模式和XY模式决定自动和序列按钮是否显示。
     * 在串行文本模式或XY模式时隐藏自动和序列按钮。
     */
    private void setBtnVisible(){
        boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT); // 获取串行文本模式开关
        boolean isXy=WorkModeManage.getInstance().getmWorkMode()==IWorkMode.WorkMode_XY; // 获取是否为XY模式

        if (isXy || isSerialsTxt){ // 如果XY模式或串行文本模式
            checkAuto.setVisibility(View.GONE); // 隐藏自动按钮
            checkSeq.setVisibility(View.GONE); // 隐藏序列按钮
        }else { // 如果非XY模式且非串行文本模式
            checkAuto.setVisibility(View.VISIBLE); // 显示自动按钮
            checkSeq.setVisibility(View.VISIBLE); // 显示序列按钮
        }

        if (!isSerialsTxt){ // 如果非串行文本模式
            checkSeq.setVisibility(VISIBLE); // 显示序列按钮
        }

    }

    /**
     * 分段状态消息消费者
     *
     * 当收到分段状态消息时，检查串行文本模式是否开启，
     * 并调用setBtnVisible方法更新按钮可见性。
     */
    private Consumer<TopMsgSegmentedState> consumerSegmentedState = new Consumer<TopMsgSegmentedState>() {
        @Override
        public void accept(TopMsgSegmentedState msgSegmentedState) throws Exception {
            // 串型文本打开与关闭，是通过分段存储消息过来的
            setBtnVisible(); // 更新按钮可见性
        }
    };

    /**
     * 工作模式变更消息消费者
     *
     * 当工作模式变更时，调用setBtnVisible方法更新按钮可见性。
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            setBtnVisible(); // 更新按钮可见性
        }
    };

    /**
     * 命令到UI消息消费者
     *
     * 当收到命令到UI消息时，根据命令标志更新序列按钮文本颜色。
     * FLAG_MENU_SINGLE标志设置序列按钮为绿色（选中），
     * FLAG_MENU_STOP和FLAG_MENU_RUN标志设置序列按钮为蓝色（未选中）。
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI=new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()){ // 根据命令标志分支处理
                case CommandMsgToUI.FLAG_MENU_SINGLE:{ // 单次触发标志
                    checkSeq.setTextColor(colorGreen); // 设置序列按钮文本为绿色（选中状态）
                }break;
                case CommandMsgToUI.FLAG_MENU_STOP: // 停止标志
                case CommandMsgToUI.FLAG_MENU_RUN: { // 运行标志
                    checkSeq.setTextColor(colorBlue); // 设置序列按钮文本为蓝色（未选中状态）
                    break;
                }
            }
        }
    };

    /**
     * 自动模式变更消息消费者
     *
     * 当收到自动模式变更消息时，根据自动模式开启状态更新自动按钮文本。
     * 自动模式开启时显示"AutoRange"，关闭时显示"Auto"。
     */
    private Consumer<Boolean> consumerAutoChanged=new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean){ // 如果自动模式开启
                checkAuto.setText(getResources().getString(R.string.mainCenterMenuAutoRange)); // 设置自动按钮文本为"AutoRange"
            }else { // 如果自动模式关闭
                checkAuto.setText(getResources().getString(R.string.mainCenterMenuAuto)); // 设置自动按钮文本为"Auto"
            }
        }
    };

    /**
     * 运行停止菜单消息消费者
     *
     * 当收到运行停止状态变更消息时，根据状态更新运行停止按钮和状态文本的颜色和内容。
     * 状态包括RUN、WAIT、STOP三种。
     * 同时根据示波器的单次触发状态更新序列按钮颜色。
     */
    private Consumer<MainLeftMsgMenuRunStop> consumerMainLeftMenu = new Consumer<MainLeftMsgMenuRunStop>() {
        @Override
        public void accept(MainLeftMsgMenuRunStop mainLeftMsgMenuRunStop) throws Exception {
            Logger.i(TAG,"state:"+mainLeftMsgMenuRunStop.getRunState()); // 记录状态信息日志
            if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.RUN) { // 如果是RUN状态
                Logger.i(TAG,"scope state Run:"+ Scope.getInstance().isAuto()); // 记录示波器自动状态日志
//                if (Scope.getInstance().isAuto()){
//                    return;
//                }
                checkRunStop.setText(getResources().getString(R.string.mainTopLeftStateRun)); // 设置运行停止按钮文本为"Run"
                checkRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run)); // 设置运行停止按钮文本颜色为绿色

                txRunStop.setText(getResources().getString(R.string.mainTopLeftStateRun)); // 设置状态文本为"Run"
                txRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run)); // 设置状态文本颜色为绿色

            } else if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.WAIT) { // 如果是WAIT状态
//                if (Scope.getInstance().isAuto()) {
//                    return;
//                }

                Scope scope = Scope.getInstance(); // 获取示波器实例
                if(!scope.isTouchFine()){ // 如果非精细触摸模式
                    if (scope.isRun()) { // 如果示波器正在运行
                        checkRunStop.setText(getResources().getString(R.string.mainTopLeftStateWait)); // 设置运行停止按钮文本为"Wait"
                        checkRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_wait)); // 设置运行停止按钮文本颜色为黄色

                        txRunStop.setText(getResources().getString(R.string.mainTopLeftStateWait)); // 设置状态文本为"Wait"
                        txRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_wait)); // 设置状态文本颜色为黄色
                        checkSeq.setTextColor(scope.isSingle()?colorGreen:colorBlue); // 设置序列按钮颜色（单次触发为绿色，否则为蓝色）

                    } else { // 如果示波器停止运行
                        checkRunStop.setText(getResources().getString(R.string.mainTopLeftStateStop)); // 设置运行停止按钮文本为"Stop"
                        checkRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop)); // 设置运行停止按钮文本颜色为红色

                        txRunStop.setText(getResources().getString(R.string.mainTopLeftStateStop)); // 设置状态文本为"Stop"
                        txRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop)); // 设置状态文本颜色为红色
                        checkSeq.setTextColor(colorBlue); // 设置序列按钮颜色为蓝色
                    }
                }
            } else if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.STOP) { // 如果是STOP状态
                checkRunStop.setText(getResources().getString(R.string.mainTopLeftStateStop)); // 设置运行停止按钮文本为"Stop"
                checkRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop)); // 设置运行停止按钮文本颜色为红色

                txRunStop.setText(getResources().getString(R.string.mainTopLeftStateStop)); // 设置状态文本为"Stop"
                txRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop)); // 设置状态文本颜色为红色
                checkSeq.setTextColor(colorBlue); // 设置序列按钮颜色为蓝色
            }
        }
    };

    /**
     * 事件UI观察者
     *
     * 监听EventFactory的自动开始/停止、示波器状态、采样有效、单次触发事件。
     * 根据事件类型更新自动和序列按钮的颜色状态。
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 获取事件基类对象
            Scope scope = Scope.getInstance(); // 获取示波器实例
            if (eventBase.getId() == EventFactory.EVENT_AUTO_START) { // 如果是自动开始事件
                    checkAuto.setTextColor(colorGreen); // 设置自动按钮文本为绿色（选中状态）
                    checkSeq.setTextColor(colorBlue); // 设置序列按钮文本为蓝色（未选中状态）
            } else if (eventBase.getId() == EventFactory.EVENT_AUTO_STOP) { // 如果是自动停止事件
                    Logger.i(TAG,"AUTO STOP!"); // 记录自动停止日志
                    checkAuto.setTextColor(colorBlue); // 设置自动按钮文本为蓝色（未选中状态）
                    checkSeq.setTextColor(colorBlue); // 设置序列按钮文本为蓝色（未选中状态）
            } else if (eventBase.getId() == EventFactory.EVENT_SCOPE_STATE) { // 如果是示波器状态事件

            } else if (eventBase.getId() == EventFactory.EVENT_SCOPE_SINGLE){ // 如果是单次触发事件
                checkSeq.setTextColor(scope.isSingle()? colorGreen:colorBlue); // 设置序列按钮颜色（单次触发为绿色，否则为蓝色）
            }
        }
    };

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
//        if (containsRect(event)){
//            return false;
//        }
        return true; // 返回true，消费触摸事件
    }
}