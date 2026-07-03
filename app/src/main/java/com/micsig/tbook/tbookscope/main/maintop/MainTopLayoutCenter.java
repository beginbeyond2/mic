package com.micsig.tbook.tbookscope.main.maintop; // 定义主顶部中心布局类的包路径

import android.content.Context; // 导入Android上下文类
import android.graphics.Color; // 导入颜色类
import android.graphics.Rect; // 导入矩形类
import android.util.AttributeSet; // 导入属性集类
import android.util.Log; // 导入日志工具类
import android.view.InputDevice; // 导入输入设备类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewConfiguration; // 导入视图配置类
import android.widget.RelativeLayout; // 导入相对布局类

import com.micsig.base.Logger; // 导入基础日志工具
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入事件UI观察者
import com.micsig.tbook.scope.Sample.SegmentSample; // 导入分段采样类
import com.micsig.tbook.scope.Scope; // 导入示波器核心类
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基类
import com.micsig.tbook.scope.ScopeFrozen; // 导入冻结示波器类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.channel.IChannel; // 导入通道接口
import com.micsig.tbook.scope.channel.MathChannel; // 导入数学通道类
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道类
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone; // 导入顶部右侧隐藏消息类
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase; // 导入触发时基类
import com.micsig.tbook.ui.util.svg.SvgNodeInfo; // 导入SVG节点信息类
import com.micsig.tbook.ui.wavezone.IChan; // 导入通道接口
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令类
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum; // 导入消息队列枚举
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChActiveChange; // 导入通道激活变更消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava总线
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister; // 导入RxJava注册类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava枚举
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleMode; // 导入采样模式消息
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleSegmented; // 导入分段采样消息
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState; // 导入分段状态消息
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean; // 导入工作模式数据类
import com.micsig.tbook.tbookscope.wavezone.trigger.MainWaveMsgTriggerTimeBase; // 导入触发时基消息
import com.micsig.tbook.ui.MTriggerTime; // 导入触发时间控件
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.micsig.tbook.ui.util.TBookUtil; // 导入示波器工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道工具类

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * Created by yangj on 2017/5/8.
 * 2018.1.29  liwb   修改触发时刻控件，重新连接到MTriggerTime控件上。原因：占用CPU过高。
 */

/*
+===================================================================================+
|                              MainTopLayoutCenter                                  |
+===================================================================================+
| 模块定位：示波器主界面顶部中心区域布局组件                                        |
| 核心职责：显示触发时间位置、采样率和存储深度信息，处理触发时间滑块交互            |
| 架构设计：继承RelativeLayout，采用RxJava响应式编程处理消息总线事件                |
| 数据流向：接收触发时基变更消息 → 更新UI显示 → 用户触摸交互 → 触发时间调整        |
| 依赖关系：依赖RxBus消息总线、Scope核心示波器类、HorizontalAxis水平轴类            |
|           MTriggerTime触发时间控件、ChannelFactory通道工厂                        |
| 使用场景：在示波器运行时显示触发时间位置，用户可通过滑块调整触发时间            |
+===================================================================================+
*/

/**
 * 示波器顶部中心布局类，负责触发时间显示和交互控制
 *
 * 该类继承自RelativeLayout，作为示波器主界面顶部中心区域的容器布局。
 * 主要功能包括：
 * 1. 显示触发时间位置滑块控件
 * 2. 显示当前通道的采样率和存储深度
 * 3. 响应触发时基变更事件并更新UI
 * 4. 处理用户的触摸交互事件
 * 5. 支持双击触发区域快速调整
 *
 * @author yangj
 * @author liwb (2018.1.29 修改触发时刻控件)
 * @version 1.0
 * @since 2017/5/8
 */
public class MainTopLayoutCenter extends RelativeLayout {

    private static final String TAG = "MainTopLayoutCenter"; // 日志标签常量，用于标识本类
    private Context context; // Android上下文对象，用于访问资源和系统服务
    private MTriggerTime slider; // 触发时间滑块控件，显示和调整触发时间位置

    /**
     * 单参数构造方法
     *
     * @param context Android上下文对象
     */
    public MainTopLayoutCenter(Context context) {
        this(context, null); // 调用双参数构造方法，传入null属性集
    }

    /**
     * 双参数构造方法
     *
     * @param context Android上下文对象
     * @param attrs XML属性集，从布局文件中解析的属性
     */
    public MainTopLayoutCenter(Context context, AttributeSet attrs) {
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
    public MainTopLayoutCenter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类RelativeLayout的构造方法
        this.context = context; // 保存上下文对象到成员变量
        initView(context, attrs, defStyleAttr); // 初始化视图布局和控件
        initControl(); // 初始化事件监听器和消息订阅
    }

    /**
     * 初始化视图布局和控件
     *
     * 从XML布局文件加载视图，并初始化触发时间滑块控件。
     * 设置触摸监听器以处理用户交互。
     *
     * @param context Android上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_maintop_center, this); // 从布局文件加载视图到本布局
        slider = (MTriggerTime) findViewById(R.id.slider); // 查找并初始化触发时间滑块控件
        this.setOnTouchListener(this::onTouchEvent); // 设置触摸监听器，使用Lambda表达式引用方法
    }

    float oldDown,oldDownY; // 记录触摸按下时的X和Y坐标，用于判断是否为点击操作
    boolean mScrolling; // 标记是否正在滚动，用于控制触摸事件响应
    public Consumer<Boolean> OnClickEvent; // 点击事件消费者接口，用于外部订阅点击事件
    private long lastClickTime = 0; // 记录上次点击时间，用于判断双击事件
    private boolean isDoubleClick = false; // 标记是否为双击事件

    /**
     * 处理触摸事件
     *
     * 判断用户的触摸操作是点击还是滑动，并处理双击事件。
     * 如果是点击操作，则触发OnClickEvent回调；
     * 如果是双击且在触发区域内，则传递true参数，否则传递false。
     *
     * @param view 触摸的视图对象
     * @param event 触摸事件对象，包含动作类型和坐标信息
     * @return 如果是点击操作返回true，否则返回false
     */
    private boolean onTouchEvent(View view, MotionEvent event) {
        boolean isFromMouse = event.isFromSource(InputDevice.SOURCE_MOUSE); // 判断是否来自鼠标点击
        switch (event.getAction()){ // 根据触摸动作类型进行分支处理
            case MotionEvent.ACTION_DOWN:{ // 触摸按下事件
                oldDown=event.getX(); // 记录按下时的X坐标
                oldDownY=event.getY(); // 记录按下时的Y坐标
                mScrolling=false; // 初始化滚动标记为false
                long clickTime = System.currentTimeMillis(); // 获取当前时间戳
                if (clickTime - lastClickTime < 300) { // 如果距离上次点击小于300毫秒
                    //双击事件
                    isDoubleClick = true; // 标记为双击事件
                } else {
                    lastClickTime = clickTime; // 更新上次点击时间
                }
            }break; // 结束ACTION_DOWN分支

            case MotionEvent.ACTION_UP:{ // 触摸抬起事件
                double min= ViewConfiguration.get(getContext()).getScaledTouchSlop(); // 获取最小滑动距离阈值
                if (Math.abs(oldDown - event.getX()) <=min && Math.abs(oldDownY-event.getY())<=min ) { // 判断是否为点击操作（移动距离小于阈值）
                    if (OnClickEvent!=null) { // 如果点击事件消费者不为空
                        try {
                            if (isDoubleClick && isFromMouse && checkDoubleClick((int) event.getX(), (int) event.getY())) { // 如果是双击、来自鼠标且在触发区域内
                                OnClickEvent.accept(true); // 触发双击事件，传递true参数
                                isDoubleClick = false; // 重置双击标记
                            } else {
                                OnClickEvent.accept(false); // 触发单击事件，传递false参数
                            }
                        } catch (Exception e) {
                            e.printStackTrace(); // 打印异常堆栈
                        } catch (Throwable e) {
                            e.printStackTrace(); // 打印错误堆栈
                        }
                    }
                    mScrolling = true; // 标记为点击操作
                } else {
                    mScrolling = false; // 标记为滑动操作
                }

            }break; // 结束ACTION_UP分支
        }
        return mScrolling; // 返回是否为点击操作
    }

    /**
     * 检查双击是否在触发区域内
     *
     * 判断双击事件的坐标是否落在触发时间滑块的矩形区域内，
     * 用于确定是否触发双击调整功能。
     *
     * @param x 双击事件的X坐标
     * @param y 双击事件的Y坐标
     * @return 如果双击在触发区域内返回true，否则返回false
     */
    private boolean checkDoubleClick(int x, int y) {
        Rect rect = slider.getTriggerRect(); // 获取触发时间滑块的矩形区域
        Logger.d(TAG, "handleDoubleClick x = " + x + " ,y= " + y + " ,rect =" + rect); // 记录日志信息
        return rect.contains(x, y); // 判断坐标是否在矩形区域内
    }

    /**
     * 初始化事件监听器和消息订阅
     *
     * 通过RxBus订阅各种消息事件，包括触发时基变更、工作模式切换、
     * 采样模式变更、分段采样状态、通道激活变更等。
     * 同时注册EventFactory的事件观察者，监听分段帧数和采样图表更新。
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAINWAVE_TRIGGERTIMEBASE).subscribe(consumerMainWaveTriggerTimeBase); // 订阅触发时基变更消息
        //RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase);

        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerTopRightGone); // 订阅顶部右侧隐藏消息
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPCENTER_TEXT_GONE).subscribe(consumerTopRightGone); // 订阅顶部中心文本隐藏消息
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange); // 订阅工作模式变更消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE).subscribe(consumerSegmentedState); // 订阅分段采样状态消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLEMODE).subscribe(consumerTopLayoutSampleMode); // 订阅采样模式消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerSampleSegment); // 订阅分段采样消息
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange); // 订阅通道激活变更消息
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅通道颜色选择消息

        EventFactory.addEventObserver(EventFactory.EVENT_SEGMENT_FRAMES, eventUIObserver); // 注册分段帧数事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_UI_SAMPLE_GRAPH, eventUIObserver); // 注册采样图表事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_UI_DEPTH_SAMPFRE_REFLASH, uiObserver); // 注册存储深度采样率刷新事件观察者
    }



    /**
     * 检查触摸事件是否在布局矩形区域内
     *
     * 当触摸抬起时，判断触摸点是否在本布局的可见矩形区域内，
     * 用于确定触摸事件是否应该由本布局处理。
     *
     * @param event 触摸事件对象
     * @return 如果触摸在布局可见区域内返回true，否则返回false
     */
    public boolean containsRect(MotionEvent event){
        if (MotionEvent.ACTION_UP==event.getAction()){ // 仅处理触摸抬起事件
            int x=(int)event.getRawX(); // 获取触摸点的原始X坐标
            int y=(int)event.getRawY(); // 获取触摸点的原始Y坐标
            Rect r1=new Rect(); // 创建矩形对象

            this.getGlobalVisibleRect(r1); // 获取本布局的全局可见矩形区域
            if (r1.contains(x,y) && this.getVisibility()==View.VISIBLE){ // 如果坐标在矩形内且布局可见
                return true; // 返回true，表示触摸在区域内
            }else {
                return false; // 返回false，表示触摸不在区域内
            }
        }else {
            return false; // 对于非抬起事件返回false
        }
    }

    /**
     * 触发时基变更消息消费者
     *
     * 当收到触发时基变更消息时，更新触发时间滑块的位置和显示时间。
     * 计算触发时间百分比位置，并设置滑块图标位置和触发时间文本。
     */
    private Consumer<MainWaveMsgTriggerTimeBase> consumerMainWaveTriggerTimeBase = new Consumer<MainWaveMsgTriggerTimeBase>() {
        @Override
        public void accept(MainWaveMsgTriggerTimeBase msgTimeBase) throws Exception {
            long x = msgTimeBase.getX(); // 获取触发时基的X坐标值
            double iconTPercent = x * 1.0 / 7; // 计算触发图标位置的百分比
            long ps = HorizontalAxis.getInstance().getTimePosOfView(); // 获取水平轴的视图时间位置（单位：1e-13s）
            slider.setIconTPercent((float) iconTPercent); // 设置滑块的图标位置百分比
//                        .setTipPercent(40)
            slider.setTrigger(TBookUtil.getTime3FromPs(ps)) // 设置触发时间文本
                    .updateUI(); // 更新UI显示
        }
    };


/*
    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() {
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception {
            if (!StrUtil.isEmpty(msgTimeBase.getTimeBase())) {
                long ns = TBookUtil.getPsFromTime(msgTimeBase.getTimeBase()) / 1000;
                slider.setMultiple(2).updateUI();
            }
//            if (msgTimeBase.isLeft()) {
//                slider.setMultiple(2).updateUI();
//            } else if (msgTimeBase.isRight()) {
//                slider.setMultiple(3).updateUI();
//            }
        }
    };
    */

    /**
     * 处理通道激活变更事件
     *
     * 当通道激活状态变更时，更新触发时间滑块的通道颜色，
     * 并显示当前通道的采样率和存储深度信息。
     *
     * @param obj 消息对象，包含通道激活变更信息
     */
    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum = RxBusRegister.parseMqEnum(obj); // 解析消息队列枚举类型
        if (mqEnum != MQEnum.CH_ACTIVE) return; // 如果不是通道激活消息则直接返回

        IChan ch = ((MsgChActiveChange) obj).getChan(); // 获取激活的通道对象
        int color; // 定义颜色变量
        if (ch == IChan.CH_NULL) { // 如果是空通道
            color = Color.TRANSPARENT; // 设置为透明色
        }  else {
            color = TChan.getChannelColor(context, TChan.toUiChNo(ch.getValue())); // 根据通道号获取通道颜色
        }
        slider.setPassagewayColor(color).updateUI(); // 设置滑块通道颜色并更新UI
        displayRateLength(ch.getValue() + 1); // 显示采样率和存储深度信息

//        TriggerTimebase.getInstance().setVisible(!IChan.isRef(ch));
    }



    /**
     * 显示采样率和存储深度信息
     *
     * 根据通道类型决定是否显示采样率和存储深度信息。
     * 对于串行通道或空通道不显示，对于其他通道显示当前的存储深度和采样率。
     *
     * @param ch 通道索引（1-based）
     */
    private void displayRateLength(int ch) {
        if (TChan.isSerial(ch) || ch == TChan.NULL) { // 如果是串行通道或空通道
//            slider.setMemoryDepth("");
//            slider.setSampleRate("");
            slider.setMemoryDepthVisible(false); // 隐藏存储深度显示
            slider.setSampleRateVisible(false); // 隐藏采样率显示
//            tvLength.setText("");
//            tvRate.setText("");
        } else { // 对于其他通道类型
            slider.setMemoryDepthVisible(true); // 显示存储深度
            slider.setSampleRateVisible(true); // 显示采样率
            int len = getDepthLen(); // 获取存储深度长度
            slider.setMemoryDepth(TBookUtil.getFourFromD((double) len)); // 设置存储深度文本（格式化为4位有效数字）
            double dx = getSampRate(); // 获取采样率
            Log.d("HH","displayRateLength--------------------------------------"); // 打印调试日志
         //  dx=zhSampGate(dx);
            slider.setSampleRate(TBookUtil.getFourFromD(dx) + "Sa/s"); // 设置采样率文本（格式化并添加单位）
        }
        slider.updateUI(); // 更新滑块UI显示
    }

    /**
     * 设置分段显示状态
     *
     * 根据配置决定是否显示分段存储信息。
     * 需要同时满足时基显示开启且分段存储状态开启的条件。
     */
    private void setSegmentShowState() {
        boolean isYT = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 0; // 获取时基显示状态配置
        boolean isNoSegment = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE) == 0; // 获取分段存储状态配置
        slider.setSegmentVisible(isYT && isNoSegment); // 设置分段信息是否可见
        slider.updateUI(); // 更新滑块UI
    }

    /**
     * 分段采样消息消费者
     *
     * 当收到分段采样消息时，调用setSegmentShowState方法更新分段显示状态。
     */
    private Consumer<TopMsgSampleSegmented> consumerSampleSegment = new Consumer<TopMsgSampleSegmented>() {
        @Override
        public void accept(TopMsgSampleSegmented msgSampleSegmented) throws Exception {
            setSegmentShowState(); // 更新分段显示状态
        }
    };

    /**
     * 顶部右侧隐藏消息消费者
     *
     * 当收到顶部右侧隐藏消息时，根据可见性设置触发时间滑块的可移动状态。
     */
    private Consumer<MainTopMsgRightGone> consumerTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone mainTopMsgRightGone) throws Exception {
            boolean visible = mainTopMsgRightGone.isVisible(); // 获取可见性标志
            slider.setCanMove(visible); // 设置滑块是否可移动
            slider.updateUI(); // 更新滑块UI
        }
    };

    /**
     * 工作模式变更消息消费者
     *
     * 当工作模式变更时，根据新的工作模式设置本布局的可见性。
     * 在YT模式和YT缩放模式时显示，在XY模式时隐藏。
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            switch (workModeBean.getNextWorkMode()) { // 根据新的工作模式分支处理
                case IWorkMode.WorkMode_YT: // YT模式
                case IWorkMode.WorkMode_YTZoom: // YT缩放模式
                    MainTopLayoutCenter.this.setVisibility(VISIBLE); // 设置本布局可见
                    break;
                case IWorkMode.WorkMode_XY: // XY模式
                    MainTopLayoutCenter.this.setVisibility(INVISIBLE); // 设置本布局不可见
                    break;
            }
        }
    };

    /**
     * 分段状态消息消费者
     *
     * 当收到分段状态消息时，检查串行文本模式是否开启，
     * 并根据串行文本状态设置本布局的可用状态。
     */
    private Consumer<TopMsgSegmentedState> consumerSegmentedState = new Consumer<TopMsgSegmentedState>() {
        @Override
        public void accept(TopMsgSegmentedState msgSegmentedState) throws Exception {
            // 串型文本打开与关闭，是通过分段存储消息过来的
            boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT); // 获取串行文本开关状态
            MainTopLayoutCenter.this.setEnabled(!isSerialsTxt); // 根据串行文本状态设置布局可用状态
        }
    };

    /**
     * 采样模式消息消费者
     *
     * 当收到采样模式变更消息时，根据采样模式类型更新触发时间滑块的采样类型文本显示。
     * 不同采样模式索引对应不同的文本格式。
     */
    private Consumer<TopMsgSampleMode> consumerTopLayoutSampleMode = new Consumer<TopMsgSampleMode>() {
        @Override
        public void accept(TopMsgSampleMode msgSample) throws Exception {
            switch (msgSample.getSample().getIndex()) { // 根据采样模式索引分支处理
                case 0: // 等效采样模式
                case 3: // 其他采样模式
                    slider.setSampleType(msgSample.getSample().getText()).updateUI(); // 设置采样类型文本
                    break;
                case 1: // 实时采样模式
                case 2: // 其他实时采样模式
                    if (!StrUtil.isEmpty(msgSample.getSample().getSimpleText())) { // 如果有简短文本
                        slider.setSampleType(msgSample.getDetail() + msgSample.getSample().getSimpleText()).updateUI(); // 使用详细文本加简短文本
                    } else {
                        slider.setSampleType(msgSample.getDetail() + msgSample.getSample().getText()).updateUI(); // 使用详细文本加完整文本
                    }
                    break;
            }

        }
    };


    /**
     * 事件UI观察者
     *
     * 监听EventFactory的分段帧数事件和采样图表事件。
     * 当分段帧数变更时，更新分段缓冲区显示；
     * 当采样图表变更时，更新屏幕数、触发位置、中心位置等信息。
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 获取事件基类对象
            if (eventBase.getId() == EventFactory.EVENT_SEGMENT_FRAMES) { // 如果是分段帧数事件
                SegmentSample segmentSample = SegmentSample.getInstance(); // 获取分段采样实例
                String strTxt = ""; // 初始化分段文本变量
                if (Scope.getInstance().isRun()) { // 如果示波器正在运行
                    int nums = segmentSample.getSegmentNums(); // 获取分段总数
                    if (nums > segmentSample.getMaxSegmentNums()) // 如果分段数超过最大值
                        nums = segmentSample.getMaxSegmentNums(); // 使用最大分段数
                    strTxt = "" + segmentSample.getSegmentFrames() + "/" + nums; // 格式化为"当前帧数/总帧数"
                } else { // 如果示波器停止运行
                    Scope scope = Scope.getInstance(); // 获取示波器实例
                    int nums = scope.getSegmentFrameNums(); // 获取分段帧总数
                    int val = (scope.getSegmentFrameNo() + 1); // 获取当前分段帧号（+1转为1-based）
                    if (SegmentSample.getInstance().getSegmentDisplayType()==SegmentSample.SEGMENT_DISPLAY_FITTING){ // 如果是拟合显示类型
                        val = scope.getSegmentFrameNo() ; // 使用0-based帧号
                    }
                    if (val>nums) val=nums; // 如果帧号超过总数，使用总数
                    if (nums <= 0) val = 0; // 如果总数为0，帧号也设为0
                    strTxt = "" + val + "/" + nums; // 格式化为"当前帧号/总帧数"
                }
                if (slider.getSegmentedBuffer().equalsIgnoreCase(strTxt) == false) { // 如果文本与当前显示不同
                    slider.setSegmentedBuffer(strTxt).updateUI(); // 更新分段缓冲区文本
                }
            } else if (eventBase.getId() == EventFactory.EVENT_UI_SAMPLE_GRAPH) { // 如果是采样图表事件
                int idx = ChannelFactory.getChActivate(); // 获取当前激活的通道索引
                int temp = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 获取参考时基设置
                if (ChannelFactory.isDynamic_or_math_Ch(idx) || ChannelFactory.isSerialCh(idx) || temp == 0) { // 如果是动态通道、数学通道、串行通道或参考时基关闭
                    HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴实例
                    Scope scope = Scope.getInstance(); // 获取示波器实例
                    ScopeFrozen scopeFrozen = ScopeFrozen.getInstance(); // 获取冻结示波器实例

                    //屏数
                    float screenNum; // 定义屏幕数变量
                    if (scope.isZoom()) { // 如果是缩放模式
                        screenNum = (float) scope.screenNum_zoom(); // 获取缩放屏幕数
                        slider.setNormalMultiple((float) scope.screenNum_Main()); // 设置正常屏幕倍数//#2796
                    } else { // 如果是正常模式
                        screenNum = (float) scope.screenNum_Main(); // 获取主屏幕数
                        slider.setNormalMultiple(1); // 设置正常屏幕倍数为1
                    }
                    slider.setMultiple(screenNum); // 设置滑块屏幕倍数

                    //触发位置
                    long timePosMain; // 定义主触发位置变量
                    if (scope.isRun()) // 如果正在运行
                        timePosMain = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD); // 获取标准视图时间位置
                    else // 如果停止运行
                        timePosMain = scopeFrozen.getTimePosOfView(); // 获取冻结视图时间位置
                    float percent; // 定义百分比变量
                    if (scope.isZoom()) { // 如果是缩放模式
                        percent = 0.5f - (float) timePosMain / (scope.timeOneScreen_main()); // 计算触发位置百分比（相对于一屏）
                    } else { // 如果是正常模式
                        percent = 0.5f - (float) timePosMain / (scope.timeOneScreen_main() * screenNum); // 计算触发位置百分比（相对于多屏）
                    }
                    slider.setIconTPercent(percent); // 设置触发图标位置百分比
                    long timePos = horizontalAxis.getTimePosOfView(); // 获取当前视图时间位置
                    //slider.setText(timePos + "s");
                    Command.get().getTimebase().Position(TBookUtil.getSFromTime(TBookUtil.getSFrom100Fs(timePos)), false); // 设置时基位置命令
                    slider.setTrigger(TBookUtil.getSFrom100Fs(timePos)); // 设置触发时间文本

                    //中心位置
                    float fx; // 定义中心位置百分比变量
                    if (scope.isRun() && !scope.isZoom()) { // 如果正在运行且非缩放模式
                        fx = 0.5f - (float) timePos / (scope.timeOneScreen_main() * screenNum); // 计算中心位置
                        if (fx >= 1.0) // 如果超过1.0
                            fx = 1.5f - fx; // 反向计算中心位置
                        else
                            fx = 0.5f; // 使用默认中心位置
                    } else { // 如果停止运行或缩放模式
                        timePos = timePos - timePosMain; // 计算相对时间位置
                        if (scope.isZoom()) // 如果是缩放模式
                            fx = 0.5f + (float) timePos / (scope.timeOneScreen_main()); // 计算中心位置（相对于一屏）
                        else // 如果是正常模式
                            fx = 0.5f + (float) timePos / (scope.timeOneScreen_main() * screenNum); // 计算中心位置（相对于多屏）
                    }
                    slider.setTipPercent(fx); // 设置中心位置百分比
                    slider.updateUI(); // 更新滑块UI
                } else if (ChannelFactory.isMathCh(idx)) { // 如果是数学通道
                    slider.updateUI(); // 仅更新UI
                } else if (ChannelFactory.isRefCh(idx)) { // 如果是参考通道
//                    HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
//                    Scope scope = Scope.getInstance();
//                    ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();

                    RefChannel refChannel = ChannelFactory.getRefChannel(idx); // 获取参考通道实例
                    //屏数
                    double screenNum = refChannel.getScreenNum(); // 获取参考通道屏幕数
                    slider.setMultiple((float) screenNum); // 设置滑块屏幕倍数

                    //触发位置
                    if (refChannel.getRefType() != 2) { // 如果参考类型不是2
                        long timePosPix = refChannel.getTimePoseOfViewPix_original_originalScale(); // 获取原始缩尺的视图时间位置像素值
                        float percent = 0.5f - (float) timePosPix / ScopeBase.getWidth(); // 计算触发位置百分比
                        slider.setIconTPercent(percent); // 设置触发图标位置百分比
                        long timePos = refChannel.getTimePosOfView(); // 获取视图时间位置
                        //slider.setText(timePos + "s");
                        Command.get().getTimebase().Position(TBookUtil.getSFromTime(TBookUtil.getSFrom100Fs(timePos)), false); // 设置时基位置命令
                        slider.setTrigger(TBookUtil.getSFrom100Fs(timePos)); // 设置触发时间文本
                    } else {
                        //骚操作
                        slider.setTrigger("  "); // 设置触发时间为空格（特殊处理）
                    }


                    //中心位置
                    double fx = refChannel.getRefMovPix() / screenNum; // 计算参考通道移动像素相对于屏幕数的比例
                    fx = 0.5f - fx / ScopeBase.getWidth(); // 计算中心位置百分比
                    slider.setTipPercent((float) fx); // 设置中心位置百分比

                    slider.updateUI(); // 更新滑块UI
                }
                RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_TIMEBASE, slider.getTrigger() + ";" + idx); // 发送时基更新消息
            }
        }
    };

    /**
     * 存储深度采样率刷新事件观察者
     *
     * 监听EventFactory的存储深度和采样率刷新事件，
     * 更新滑块显示的存储深度和采样率信息。
     */
    EventUIObserver uiObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 获取事件基类对象
            if (eventBase.getId() == EventFactory.EVENT_UI_DEPTH_SAMPFRE_REFLASH) { // 如果是存储深度采样率刷新事件
                int len = getDepthLen(); // 获取存储深度长度
                slider.setMemoryDepth(TBookUtil.getFourFromD((double) len)); // 设置存储深度文本
                double dx = getSampRate(); // 获取采样率
              //  dx=zhSampGate(dx);
                //Log.d("HH","uiObserver--------------------------------------dx="+ dx);
                slider.setSampleRate(TBookUtil.getFourFromD(dx) + "Sa/s"); // 设置采样率文本
//                displayRateLength(ChannelFactory.getChActivate()+1);
            }
        }
    };

//    public  double zhSampGate(double dx ){
//        if (dx==6.0e9){
//            dx=6.4e9;
//        }else if (dx==3.0e9){
//            dx=3.2e9;
//        }else if (dx==1.5e9){
//            dx=1.6e9;
//        }else if (dx==7.5e8){
//            dx=8.0e8;
//        }else if (dx==3.0e8){
//            dx=4.0e8;
//        }else if (dx==1.5e8){
//            dx=2.0e8;
//        }else if (dx==7.5e7){
//            dx=1.0e8;
//        }else if (dx==3.0e7){
//            dx=5.0e7;
//        }else if (dx==1.5e7){
//            dx=2.5e7;
//        }else if (dx==7.5e6){
//            dx=1.2e7;
//        }else if (dx==3.0e6){
//            dx=6.0e6;
//        }else if (dx==1.5e6){
//            dx=3.0e6;
//        }else if (dx==7.5e5){
//            dx=1.5e6;
//        }else if (dx==3.0e5){
//            dx=7.5e5;
//        }else if (dx==1.5e5){
//            dx=3.0e5;
//        }
//        return dx;
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
     * 通道颜色选择消息消费者
     *
     * 当收到通道颜色选择消息时，如果颜色信息对应的通道是当前激活通道，
     * 则更新触发时间滑块的通道颜色。
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return; // 如果颜色信息为空则直接返回
            Logger.i(TAG, "selectColorInfo= " + colorInfo + " ,ChActivate= " + ChannelFactory.getChActivate()); // 记录日志信息
            String[] info = colorInfo.split(";"); // 分割颜色信息字符串（格式：通道索引;颜色值）
            int chIndex = Integer.parseInt(info[0]); // 解析通道索引
            String colorStr = info[1]; // 获取颜色字符串
            if (TChan.toFpgaChNo(chIndex) == ChannelFactory.getChActivate()) { // 如果颜色信息对应当前激活通道
                slider.setPassagewayColor(Color.parseColor(colorStr)).updateUI(); // 更新滑块通道颜色
            }
        }
    };


}