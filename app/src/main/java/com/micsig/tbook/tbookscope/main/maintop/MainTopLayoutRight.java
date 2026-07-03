package com.micsig.tbook.tbookscope.main.maintop; // 定义主顶部右侧布局类的包路径

import android.content.Context; // 导入Android上下文类
import android.graphics.Rect; // 导入矩形类
import android.util.AttributeSet; // 导入属性集类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewConfiguration; // 导入视图配置类
import android.widget.ImageView; // 导入图像视图类
import android.widget.LinearLayout; // 导入线性布局类
import android.widget.TextView; // 导入文本视图类

import com.micsig.base.Logger; // 导入基础日志工具
import com.micsig.tbook.scope.Trigger.Trigger; // 导入触发器类
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 导入触发器工厂类
import com.micsig.tbook.tbookscope.LoadCache; // 导入加载缓存类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone; // 导入顶部右侧隐藏消息类
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel; // 导入触发电平持有者类
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel; // 导入触发电平消息类
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令到UI消息类
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgChannel; // 导入右侧滑块通道消息类
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel; // 导入右侧滑块电平消息类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava枚举
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入顶部触发布局类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTrigger; // 导入触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerCommon; // 导入通用触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerEdge; // 导入边沿触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerLogic; // 导入逻辑触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerNEdge; // 导入N边沿触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerPulsewidth; // 导入脉宽触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerRunt; // 导入欠幅触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerSlope; // 导入斜率触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerTimeout; // 导入超时触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerVideo; // 导入视频触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.TopMsgTriggerSerials; // 导入串行触发消息类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Data; // 导入ARINC429数据详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Label; // 导入ARINC429标签详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelData; // 导入ARINC429标签数据详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSdi; // 导入ARINC429标签SDI详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSsm; // 导入ARINC429标签SSM详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Sdi; // 导入ARINC429 SDI详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Ssm; // 导入ARINC429 SSM详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanDataId; // 导入CAN数据ID详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanIdData; // 导入CAN ID数据详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRdId; // 导入CAN RD ID详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRemoteId; // 导入CAN远程ID详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2c10WriteFrame; // 导入I2C 10位写帧详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame1; // 导入I2C帧1详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame2; // 导入I2C帧2详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cNoAckInAdr; // 导入I2C地址无ACK详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cRomData; // 导入I2C ROM数据详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinFrameId; // 导入LIN帧ID详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinIdData; // 导入LIN ID数据详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bCsWord; // 导入M1553B CS字详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bDataWord; // 导入M1553B数据字详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bRtAddr; // 导入M1553B RT地址详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailSpiData; // 导入SPI数据详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart0Data; // 导入UART0数据详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart1Data; // 导入UART1数据详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartData; // 导入UART数据详情类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartxData; // 导入UARTx数据详情类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean; // 导入工作模式数据类
import com.micsig.tbook.ui.MTriggerStateBar; // 导入触发状态栏控件
import com.micsig.tbook.ui.main.MainBeanTopRight; // 导入顶部右侧数据类
import com.micsig.tbook.ui.top.view.scale.TopUtilScale; // 导入顶部工具缩放类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.micsig.tbook.ui.util.TBookUtil; // 导入示波器工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道工具类

import java.util.ArrayList; // 导入数组列表类

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口

/**
 * Created by yangj on 2017/5/8.
 */

/*
+===================================================================================+
|                              MainTopLayoutRight                                   |
+===================================================================================+
| 模块定位：示波器主界面顶部右侧区域布局组件                                        |
| 核心职责：显示触发类型图标、触发状态、触发电平信息，处理触发参数变更              |
| 架构设计：继承LinearLayout，采用RxJava响应式编程处理触发相关消息                  |
| 数据流向：接收触发参数变更 → 更新触发图标和状态 → 显示触发电平列表                |
| 依赖关系：依赖RxBus消息总线、TriggerFactory触发器工厂、MTriggerStateBar状态栏      |
|           各种触发类型消息类（边沿、脉宽、逻辑、串行等）                          |
| 使用场景：在示波器运行时显示当前触发类型、触发模式和各通道触发电平信息            |
+===================================================================================+
*/

/**
 * 示波器顶部右侧布局类，负责触发状态和电平信息显示
 *
 * 该类继承自LinearLayout，作为示波器主界面顶部右侧区域的容器布局。
 * 主要功能包括：
 * 1. 显示触发类型图标（边沿、脉宽、逻辑、串行等）
 * 2. 显示触发模式（自动Auto/正常Normal）
 * 3. 显示各通道的触发电平信息列表
 * 4. 响应触发参数变更事件并更新UI
 * 5. 处理用户的触摸交互事件
 * 6. 支持多种触发类型（边沿、脉宽、逻辑、N边沿、欠幅、斜率、超时、视频、串行）
 *
 * 支持的串行触发类型包括：
 * - UART、LIN、CAN、SPI、I2C、ARINC429、M1553B等
 *
 * @author yangj
 * @version 1.0
 * @since 2017/5/8
 */
public class MainTopLayoutRight extends LinearLayout {
    private static final String TAG = "MainTopLayoutRight"; // 日志标签常量，用于标识本类

    // 触发类型图标资源ID常量定义
    public static final int DRAWABLEID_BUSTRIGGER = R.drawable.bus_trigger; // 总线触发图标
    public static final int DRAWABLEID_CAN = R.drawable.can; // CAN触发图标
    public static final int DRAWABLEID_DPULSE = R.drawable.dpulse; // 双脉冲触发图标
    public static final int DRAWABLEID_HOLD = R.drawable.hold; // 保持触发图标
    public static final int DRAWABLEID_LIN = R.drawable.lin; // LIN触发图标
    public static final int DRAWABLEID_M429 = R.drawable.m429; // ARINC429触发图标
    public static final int DRAWABLEID_M1553B = R.drawable.m1553b; // M1553B触发图标
    public static final int DRAWABLEID_NTH = R.drawable.nth; // N边沿触发图标
    public static final int DRAWABLEID_AND = R.drawable.logic_and; // 逻辑AND触发图标
    public static final int DRAWABLEID_OR = R.drawable.logic_or; // 逻辑OR触发图标
    public static final int DRAWABLEID_NAND = R.drawable.logic_nand; // 逻辑NAND触发图标
    public static final int DRAWABLEID_NOR = R.drawable.logic_nor; // 逻辑NOR触发图标
    public static final int DRAWABLEID_PULSE = R.drawable.pulse; // 正脉冲触发图标
    public static final int DRAWABLEID_PULSE_N = R.drawable.pulse_n; // 负脉冲触发图标
    public static final int DRAWABLEID_SLOPE = R.drawable.slope; // 正斜率触发图标
    public static final int DRAWABLEID_SLOPE_N = R.drawable.slope_n; // 负斜率触发图标
    public static final int DRAWABLEID_SLOPE_D = R.drawable.slope_d; // 双斜率触发图标
    public static final int DRAWABLEID_SPI = R.drawable.spi; // SPI触发图标
    public static final int DRAWABLEID_TIMEOUT = R.drawable.timeout; // 正超时触发图标
    public static final int DRAWABLEID_TIMEOUT_N = R.drawable.timeout_n; // 负超时触发图标
    public static final int DRAWABLEID_TIMEOUT_D = R.drawable.timeout_d; // 双超时触发图标
    public static final int DRAWABLEID_TRIGGERD = R.drawable.trigger_d; // 双边沿触发图标
    public static final int DRAWABLEID_TRIGGERS = R.drawable.trigger_s; // 上升沿触发图标
    public static final int DRAWABLEID_TRIGGERX = R.drawable.trigger_x; // 下降沿触发图标
    public static final int DRAWABLEID_UART = R.drawable.uart; // UART触发图标
    public static final int DRAWABLEID_VIDEOH = R.drawable.video_h; // 视频行触发图标
    public static final int DRAWABLEID_VIDEOL = R.drawable.video_l; // 视频场触发图标

    private Context context; // Android上下文对象，用于访问资源和系统服务
    private TextView lblTrig, an; // 触发标签文本、自动/正常模式文本
    private ImageView icon; // 触发类型图标视图
    //    private MainViewTopRight detail;
    private MTriggerStateBar detail; // 触发状态栏控件，显示各通道触发电平列表
    private int drawableId; // 当前显示的触发类型图标资源ID
    private View parentView; // 父视图引用
    private ArrayList<MainBeanTopRight> list; // 触发电平数据列表，存储各通道的触发电平信息

    /**
     * 单参数构造方法
     *
     * @param context Android上下文对象
     */
    public MainTopLayoutRight(Context context) {
        this(context, null); // 调用双参数构造方法，传入null属性集
    }

    /**
     * 双参数构造方法
     *
     * @param context Android上下文对象
     * @param attrs XML属性集，从布局文件中解析的属性
     */
    public MainTopLayoutRight(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 调用三参数构造方法，传入默认样式属性
    }

    /**
     * 三参数构造方法（完整构造方法）
     *
     * 初始化布局视图和事件监听器，设置默认触发图标为逻辑AND。
     *
     * @param context Android上下文对象，用于访问资源和系统服务
     * @param attrs XML属性集，从布局文件中解析的属性
     * @param defStyleAttr 默认样式属性，用于定义视图的默认样式
     */
    public MainTopLayoutRight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类LinearLayout的构造方法
        this.context = context; // 保存上下文对象到成员变量
        drawableId = DRAWABLEID_AND; // 设置默认触发图标为逻辑AND
        initView(); // 初始化视图布局和控件
        initControl(); // 初始化事件监听器和消息订阅
    }

    /**
     * 初始化事件监听器和消息订阅
     *
     * 通过RxBus订阅各种触发相关消息事件，包括缓存加载、
     * 触发参数变更、触发电平变更、通道变更、可见性变更、工作模式变更等。
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerMainCache); // 订阅缓存加载消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTopTrigger); // 订阅触发参数变更消息
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel); // 订阅触发电平变更消息
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_CHANNEL).subscribe(consumerRightChannel); // 订阅通道变更消息
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerMainTopRightGone); // 订阅顶部右侧隐藏消息
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange); // 订阅工作模式变更消息
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL)
//                .debounce(20, TimeUnit.MILLISECONDS)
                .subscribe(consumerTriggerLevel); // 订阅触发电平触发通道消息

        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI消息
    }

    /**
     * 初始化视图布局和控件
     *
     * 从XML布局文件加载视图，并初始化各种控件。
     * 创建默认的触发电平数据列表，并设置触摸监听器。
     */
    private void initView() {
        parentView = View.inflate(context, R.layout.layout_maintop_right, this); // 从布局文件加载视图到本布局
        setLayoutDirection(View.LAYOUT_DIRECTION_LTR); // 设置布局方向为从左到右
        setOrientation(HORIZONTAL); // 设置布局方向为水平
        lblTrig = (TextView) findViewById(R.id.lblTrig); // 查找并初始化触发标签文本
        an = (TextView) findViewById(R.id.maintopright_an); // 查找并初始化自动/正常模式文本
        icon = (ImageView) findViewById(R.id.maintopright_icon); // 查找并初始化触发类型图标视图
//        detail = (MainViewTopRight) findViewById(R.id.detail);
        detail = (MTriggerStateBar) findViewById(R.id.maintopright_detail); // 查找并初始化触发状态栏控件
        detail.setOnClickListener(onClickListener); // 设置触发状态栏点击监听器
        icon.setImageResource(drawableId); // 设置默认触发图标

        list = new ArrayList<MainBeanTopRight>(); // 创建触发电平数据列表
        list.add(new MainBeanTopRight(TChan.Ch1, "-70.6224mV", MainBeanTopRight.LINE_TOP)); // 添加通道1触发电平数据（默认值）
        list.add(new MainBeanTopRight(TChan.Ch2, "-10uV", MainBeanTopRight.LINE_TOP)); // 添加通道2触发电平数据（默认值）
        list.add(new MainBeanTopRight(TChan.Ch3, "-70.64mV", MainBeanTopRight.LINE_TOP)); // 添加通道3触发电平数据（默认值）
        list.add(new MainBeanTopRight(TChan.Ch4, "-70.64mV", MainBeanTopRight.LINE_TOP)); // 添加通道4触发电平数据（默认值）
        list.add(new MainBeanTopRight(TChan.Ch5, "-70.64mV", MainBeanTopRight.LINE_TOP)); // 添加通道5触发电平数据（默认值）
        list.add(new MainBeanTopRight(TChan.Ch6, "-70.64mV", MainBeanTopRight.LINE_TOP)); // 添加通道6触发电平数据（默认值）
        list.add(new MainBeanTopRight(TChan.Ch7, "-70.64mV", MainBeanTopRight.LINE_TOP)); // 添加通道7触发电平数据（默认值）
        list.add(new MainBeanTopRight(TChan.Ch8, "-70.64mV", MainBeanTopRight.LINE_TOP)); // 添加通道8触发电平数据（默认值）
        list.add(new MainBeanTopRight(TChan.Ch8 + 1, "2.5V", MainBeanTopRight.LINE_TOP)); // 添加额外通道触发电平数据（默认值）
        list.add(new MainBeanTopRight("-70.6224mV", MainBeanTopRight.LINE_TOP, R.color.color_S1)); // 添加特殊触发电平数据（默认值）
        detail.setData(list); // 设置触发状态栏的数据列表

        this.setOnTouchListener(this::onTouchEvent); // 设置触摸监听器，使用Lambda表达式引用方法

    }

    float oldDown,oldDownY; // 记录触摸按下时的X和Y坐标，用于判断是否为点击操作
    boolean mScrolling; // 标记是否正在滚动，用于控制触摸事件响应
    public Consumer<View> OnClickEvent; // 点击事件消费者接口，用于外部订阅点击事件

    /**
     * 处理触摸事件
     *
     * 判断用户的触摸操作是点击还是滑动，并触发OnClickEvent回调。
     *
     * @param view 触摸的视图对象
     * @param event 触摸事件对象，包含动作类型和坐标信息
     * @return 如果是点击操作返回true，否则返回false
     */
    private boolean onTouchEvent(View view, MotionEvent event) {

        switch (event.getAction()){ // 根据触摸动作类型进行分支处理
            case MotionEvent.ACTION_DOWN:{ // 触摸按下事件
                oldDown=event.getX(); // 记录按下时的X坐标
                oldDownY=event.getY(); // 记录按下时的Y坐标
                mScrolling=false; // 初始化滚动标记为false
            }break; // 结束ACTION_DOWN分支

            case MotionEvent.ACTION_UP:{ // 触摸抬起事件
                int min= ViewConfiguration.get(getContext()).getScaledTouchSlop(); // 获取最小滑动距离阈值
                if (Math.abs(oldDown - event.getX()) <=min  &&  Math.abs(oldDownY-event.getY())<=min ) { // 判断是否为点击操作（移动距离小于阈值）
                    if (OnClickEvent!=null) { // 如果点击事件消费者不为空
                        try {
                            OnClickEvent.accept(null); // 触发点击事件，传递null参数
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
            if (r1.contains(x,y) && this.getVisibility()==View.VISIBLE ){ // 如果坐标在矩形内且布局可见
                return true; // 返回true，表示触摸在区域内
            }else {
                return false; // 返回false，表示触摸不在区域内
            }
        }else {
            return false; // 对于非抬起事件返回false
        }
    }

    /**
     * 缓存加载消息消费者
     *
     * 当收到缓存加载消息时，从缓存获取触发模式配置，
     * 并更新自动/正常模式文本显示。
     */
    private Consumer<LoadCache> consumerMainCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            int mode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE); // 获取触发模式配置（0=自动，1=正常）
            an.setText(mode == 0 ? "A" : "N"); // 设置自动/正常模式文本（A=Auto，N=Normal）
        }
    };

    /**
     * 右侧滑块通道消息消费者
     *
     * 当收到通道变更消息时，更新触发电平列表中的单位（V或A）。
     * 根据通道的探头类型（电压或电流）确定单位。
     */
    private Consumer<RightMsgChannel> consumerRightChannel = new Consumer<RightMsgChannel>() {
        @Override
        public void accept(RightMsgChannel rightMsgChannel) throws Exception {
//            if (rightMsgChannel.getProbeType().isRxMsgSelect()) {
            int channelNumber = rightMsgChannel.getChannelNumber(); // 获取通道号
            String unit = Tools.getChanProbeTypeUnit(channelNumber - 1) == 0 ? "V" : "A"; // 获取探头类型单位（0=电压V，1=电流A）
//                String unit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber) == 0 ? "V" : "A";

            if (TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_SERIAL1 // 如果触发类型为串行1
                    || TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_SERIAL2 // 或串行2
                    || TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_SERIAL3 // 或串行3
                    || TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_SERIAL4 // 或串行4
            ) {
                return; // 对于串行触发类型直接返回，不更新单位
            }
            for (int i = 0; i < list.size(); i++) { // 遍历触发电平数据列表
                if (list.get(i).getChannel() == channelNumber && list.get(i).isVisible() // 如果通道号匹配且可见
                        || list.get(i).getChannel() == 0 && list.get(i).isVisible()) { // 或特殊通道（通道0）且可见
                    if (list.get(i).getText().endsWith("A") || list.get(i).getText().endsWith("V")) { // 如果文本以A或V结尾
                        list.get(i).setText(list.get(i).getText().replace("A", unit).replace("V", unit)); // 替换单位
                    }
                }
            }
            detail.setData(list); // 更新触发状态栏数据
        }
//        }
    };

    /**
     * 触发电平消息消费者
     *
     * 当收到触发电平变更消息时，根据触发类型更新各通道的触发电平显示。
     * 支持边沿、脉宽、逻辑、N边沿、欠幅、斜率、超时等触发类型的电平显示。
     */
    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() {
        @Override
        public void accept(MainMsgTriggerLevel msgTriggerLevel) throws Exception {
            if (msgTriggerLevel == null || msgTriggerLevel.getCurLevel() == null) return; // 如果消息或电平为空则直接返回
            if (!msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_EDGE) // 如果不是边沿触发
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_PULSEWIDTH) // 且不是脉宽触发
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_LOGIC) // 且不是逻辑触发
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_NEDGE) // 且不是N边沿触发
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_RUNT) // 且不是欠幅触发
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_SLOPE) // 且不是斜率触发
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_TIMEOUT)) { // 且不是超时触发
                return; // 对于不支持的触发类型直接返回
            }
            switch (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER)) { // 根据触发详情配置分支处理
                case TopLayoutTrigger.DETAIL_EDGE: // 边沿触发
                case TopLayoutTrigger.DETAIL_PULSEWIDTH: // 脉宽触发
                case TopLayoutTrigger.DETAIL_LOGIC: // 逻辑触发
                case TopLayoutTrigger.DETAIL_NEDGE: // N边沿触发
                case TopLayoutTrigger.DETAIL_RUNT: // 欠幅触发
                case TopLayoutTrigger.DETAIL_SLOPE: // 斜率触发
                case TopLayoutTrigger.DETAIL_TIMEOUT: // 超时触发
                    break; // 支持的触发类型，继续处理
                default:
                    return; // 不支持的触发类型，直接返回
            }
            int colorResId = R.color.color_S1; // 定义颜色资源ID变量
            for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                if (MainHolderTriggerLevel.LEVEL_TRIGGER_LOGIC.equals(msgTriggerLevel.getCurLevel())) { // 如果是逻辑触发
                    if (bean.getChannel() != 0) { // 如果不是特殊通道（通道0）
                        //触发电平的逻辑
                        bean.setText(getChannelLevel(bean.getChannel(), Tools.LevelType_Normal, Tools.LevelMode_Normal)); // 设置通道电平文本
                    }
                } else if (bean.getChannel() == msgTriggerLevel.getCurCh()) { // 如果通道号匹配当前触发电平通道
                    if (MainHolderTriggerLevel.LEVEL_TRIGGER_SLOPE.equals(msgTriggerLevel.getCurLevel())) { // 如果是斜率触发
                        //触发电平的斜率低电平
                        bean.setText(getChannelLevel(bean.getChannel(), Tools.LevelType_Normal, Tools.LevelMode_Normal)); // 设置斜率低电平文本
                        bean.setLine(MainBeanTopRight.LINE_BOTTOM); // 设置为底线位置
                        colorResId = bean.getColorResId(); // 获取通道颜色资源ID
                    } else if (MainHolderTriggerLevel.LEVEL_TRIGGER_RUNT.equals(msgTriggerLevel.getCurLevel())) { // 如果是欠幅触发
                        //触发电平的欠幅低电平
                        bean.setText(getChannelLevel(bean.getChannel(), Tools.LevelType_Normal, Tools.LevelMode_Normal)); // 设置欠幅低电平文本
                        bean.setLine(MainBeanTopRight.LINE_BOTTOM); // 设置为底线位置
                        colorResId = bean.getColorResId(); // 获取通道颜色资源ID
                    } else { // 其他触发类型
                        //触发电平的其他
                        bean.setText(getChannelLevel(bean.getChannel(), Tools.LevelType_Normal, Tools.LevelMode_Normal)); // 设置电平文本
                        break; // 跳出循环
                    }
                }
                if (bean.getChannel() == 0) { // 如果是特殊通道（通道0）
                    if (MainHolderTriggerLevel.LEVEL_TRIGGER_SLOPE.equals(msgTriggerLevel.getCurLevel())) { // 如果是斜率触发
                        //触发电平的斜率高电平
                        bean.setText(getChannelLevel(msgTriggerLevel.getCurCh(), Tools.LevelType_High, Tools.LevelMode_Normal)); // 设置斜率高电平文本
                        bean.setColorResId(colorResId); // 设置颜色资源ID
                        bean.setLine(MainBeanTopRight.LINE_TOP); // 设置为顶线位置
                        bean.setVisible(true); // 设置可见
                    } else if (MainHolderTriggerLevel.LEVEL_TRIGGER_RUNT.equals(msgTriggerLevel.getCurLevel())) { // 如果是欠幅触发
                        //触发电平的欠幅低电平
                        bean.setText(getChannelLevel(msgTriggerLevel.getCurCh(), Tools.LevelType_High, Tools.LevelMode_Normal)); // 设置欠幅高电平文本
                        bean.setColorResId(colorResId); // 设置颜色资源ID
                        bean.setLine(MainBeanTopRight.LINE_TOP); // 设置为顶线位置
                        bean.setVisible(true); // 设置可见
                    } else { // 其他触发类型
                        bean.setVisible(false); // 隐藏特殊通道
                    }
                }
            }
            detail.setData(list); // 更新触发状态栏数据
        }
    };

    /**
     * 获取通道触发电平文本
     *
     * 根据通道号、电平类型和电平模式获取触发电平文本。
     *
     * @param channel 通道号（1-8）
     * @param levelType 电平类型（正常、高电平等）
     * @param levelMode 电平模式
     * @return 触发电平文本字符串
     */
    private String getChannelLevel(int channel, int levelType, int levelMode) {
        return Tools.getChannelLevel(channel, levelType, levelMode); // 调用工具类获取通道电平文本
    }

    private boolean visibleTimeBase = true; // 时基可见性标志
    private boolean visibleXYMode = true; // XY模式可见性标志

    /**
     * 顶部右侧隐藏消息消费者
     *
     * 当收到顶部右侧隐藏消息时，根据可见性标志更新本布局的可见性。
     */
    private Consumer<MainTopMsgRightGone> consumerMainTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone mainTopMsgRightGone) throws Exception {
            visibleTimeBase = mainTopMsgRightGone.isVisible(); // 获取时基可见性标志
            if (visibleTimeBase && visibleXYMode) { // 如果时基可见且XY模式可见
                lblTrig.setVisibility(VISIBLE); // 设置触发标签可见
                an.setVisibility(View.VISIBLE); // 设置自动/正常模式文本可见
                icon.setVisibility(View.VISIBLE); // 设置触发图标可见
                detail.setVisibility(View.VISIBLE); // 设置触发状态栏可见
                parentView.setVisibility(View.VISIBLE); // 设置父视图可见
            } else { // 如果不可见
                lblTrig.setVisibility(INVISIBLE); // 设置触发标签不可见
                an.setVisibility(View.INVISIBLE); // 设置自动/正常模式文本不可见
                icon.setVisibility(View.INVISIBLE); // 设置触发图标不可见
                detail.setVisibility(View.INVISIBLE); // 设置触发状态栏不可见
                parentView.setVisibility(View.INVISIBLE); // 设置父视图不可见
            }
        }
    };

    /**
     * 工作模式变更消息消费者
     *
     * 当工作模式变更时，根据是否为XY模式更新本布局的可见性。
     * XY模式时隐藏，YT模式时显示。
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            visibleXYMode = workModeBean.getNextWorkMode() != IWorkMode.WorkMode_XY; // 计算XY模式可见性（非XY模式为true）
            if (visibleTimeBase && visibleXYMode) { // 如果时基可见且XY模式可见
                lblTrig.setVisibility(VISIBLE); // 设置触发标签可见
                an.setVisibility(View.VISIBLE); // 设置自动/正常模式文本可见
                icon.setVisibility(View.VISIBLE); // 设置触发图标可见
                detail.setVisibility(View.VISIBLE); // 设置触发状态栏可见
                parentView.setVisibility(View.VISIBLE); // 设置父视图可见
            } else { // 如果不可见
                lblTrig.setVisibility(INVISIBLE); // 设置触发标签不可见
                an.setVisibility(View.INVISIBLE); // 设置自动/正常模式文本不可见
                icon.setVisibility(View.INVISIBLE); // 设置触发图标不可见
                detail.setVisibility(View.INVISIBLE); // 设置触发状态栏不可见
                parentView.setVisibility(View.INVISIBLE); // 设置父视图不可见
            }
        }
    };

    /**
     * 右侧滑块电平消息消费者
     *
     * 当收到右侧滑块电平变更消息时，更新自动/正常模式文本显示。
     */
    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(RightMsgLevel msgLevel) throws Exception {
            an.setText(msgLevel.getMiddleSelect() == 0 ? "A" : "N"); // 设置自动/正常模式文本（0=Auto，其他=Normal）
        }
    };

    /**
     * 触发参数变更消息消费者（核心处理逻辑）
     *
     * 当收到触发参数变更消息时，根据触发类型更新触发图标、触发电平列表、
     * 自动/正常模式文本等。
     * 支持多种触发类型：通用、边沿、脉宽、逻辑、N边沿、欠幅、斜率、超时、视频、串行等。
     * 串行触发支持UART、LIN、CAN、SPI、I2C、ARINC429、M1553B等协议。
     */
    private Consumer<TopMsgTrigger> consumerTopTrigger = new Consumer<TopMsgTrigger>() {
        @Override
        public void accept(TopMsgTrigger topMsgTrigger) throws Exception {
            if(topMsgTrigger.getTriggerDetail() == null) return; // 如果触发详情为空则直接返回
            for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                bean.setShowNumber(true); // 设置显示通道号
            }
            switch (topMsgTrigger.getTriggerTitle().getIndex()) { // 根据触发类型索引分支处理
                case TopLayoutTrigger.DETAIL_COMMON: // 通用触发
                    TopMsgTriggerCommon common = ((TopMsgTriggerCommon) topMsgTrigger.getTriggerDetail()); // 获取通用触发详情
                    an.setText(common.getMode().getIndex() == 0 ? "A" : "N"); // 设置自动/正常模式文本
                    break;
                case TopLayoutTrigger.DETAIL_EDGE: // 边沿触发
                    TopMsgTriggerEdge edge = (TopMsgTriggerEdge) topMsgTrigger.getTriggerDetail(); // 获取边沿触发详情
                    switch (edge.getTriggerEdge().getIndex()) { // 根据边沿类型分支处理
                        case 0: // 上升沿
                            icon.setImageResource(DRAWABLEID_TRIGGERS); // 设置上升沿触发图标
                            break;
                        case 1: // 下降沿
                            icon.setImageResource(DRAWABLEID_TRIGGERX); // 设置下降沿触发图标
                            break;
                        case 2: // 双边沿
                            icon.setImageResource(DRAWABLEID_TRIGGERD); // 设置双边沿触发图标
                            break;
                    }
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        bean.setVisible(bean.getChannel() == (edge.getTriggerSource().getIndex() + 1)); // 设置可见性（仅触发源通道可见）
                        if (bean.getChannel() == (edge.getTriggerSource().getIndex() + 1)) { // 如果是触发源通道
                            bean.setLine(MainBeanTopRight.LINE_NULL); // 设置为无特殊线位置
                        }
                    }
                    detail.setData(list); // 更新触发状态栏数据
                    break;
                case TopLayoutTrigger.DETAIL_PULSEWIDTH: // 脉宽触发
                    TopMsgTriggerPulsewidth pulsewidth = (TopMsgTriggerPulsewidth) topMsgTrigger.getTriggerDetail(); // 获取脉宽触发详情
                    if (pulsewidth.getPolar().getIndex() == 0) { // 如果极性为正脉冲
                        icon.setImageResource(DRAWABLEID_PULSE); // 设置正脉冲触发图标
                    } else { // 如果极性为负脉冲
                        icon.setImageResource(DRAWABLEID_PULSE_N); // 设置负脉冲触发图标
                    }
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        bean.setVisible(bean.getChannel() == (pulsewidth.getTriggerSource().getIndex() + 1)); // 设置可见性（仅触发源通道可见）
                        if (bean.getChannel() == (pulsewidth.getTriggerSource().getIndex() + 1)) { // 如果是触发源通道
                            bean.setLine(pulsewidth.getPolar().getIndex() == 0 ? MainBeanTopRight.LINE_BOTTOM : MainBeanTopRight.LINE_TOP); // 设置线位置（正脉冲为底线，负脉冲为顶线）
                        }
                    }
                    detail.setData(list); // 更新触发状态栏数据
                    break;
                case TopLayoutTrigger.DETAIL_LOGIC: // 逻辑触发
                    TopMsgTriggerLogic logic = (TopMsgTriggerLogic) topMsgTrigger.getTriggerDetail(); // 获取逻辑触发详情
                    switch (logic.getTriggerLogic().getIndex()) { // 根据逻辑类型分支处理
                        case 0: // AND逻辑
                            icon.setImageResource(DRAWABLEID_AND); // 设置AND触发图标
                            break;
                        case 1: // OR逻辑
                            icon.setImageResource(DRAWABLEID_OR); // 设置OR触发图标
                            break;
                        case 2: // NAND逻辑
                            icon.setImageResource(DRAWABLEID_NAND); // 设置NAND触发图标
                            break;
                        case 3: // NOR逻辑
                            icon.setImageResource(DRAWABLEID_NOR); // 设置NOR触发图标
                            break;
                    }
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        switch (bean.getChannel()) { // 根据通道号分支处理
                            case 0: // 特殊通道
                                bean.setVisible(false); // 隐藏特殊通道
                                break;
                            case TChan.Ch1: // 通道1
                                bean.setVisible(logic.getCh1().getIndex() != 2); // 设置可见性（非忽略状态）
                                bean.setLine(logic.getCh1().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM); // 设置线位置（高电平为顶线，低电平为底线）
                                break;
                            case TChan.Ch2: // 通道2
                                bean.setVisible(logic.getCh2().getIndex() != 2); // 设置可见性（非忽略状态）
                                bean.setLine(logic.getCh2().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM); // 设置线位置
                                break;
                            case TChan.Ch3: // 通道3
                                bean.setVisible(logic.getCh3().getIndex() != 2); // 设置可见性
                                bean.setLine(logic.getCh3().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM); // 设置线位置
                                break;
                            case TChan.Ch4: // 通道4
                                bean.setVisible(logic.getCh4().getIndex() != 2); // 设置可见性
                                bean.setLine(logic.getCh4().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM); // 设置线位置
                                break;
                            case TChan.Ch5: // 通道5
                                bean.setVisible(logic.getCh5().getIndex() != 2); // 设置可见性
                                bean.setLine(logic.getCh5().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM); // 设置线位置
                                break;
                            case TChan.Ch6: // 通道6
                                bean.setVisible(logic.getCh6().getIndex() != 2); // 设置可见性
                                bean.setLine(logic.getCh6().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM); // 设置线位置
                                break;
                            case TChan.Ch7: // 通道7
                                bean.setVisible(logic.getCh7().getIndex() != 2); // 设置可见性
                                bean.setLine(logic.getCh7().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM); // 设置线位置
                                break;
                            case TChan.Ch8: // 通道8
                                bean.setVisible(logic.getCh8().getIndex() != 2); // 设置可见性
                                bean.setLine(logic.getCh8().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM); // 设置线位置
                                break;
                        }
                    }
                    detail.setData(list); // 更新触发状态栏数据
                    break;
                case TopLayoutTrigger.DETAIL_NEDGE: // N边沿触发
                    TopMsgTriggerNEdge nEdge = (TopMsgTriggerNEdge) topMsgTrigger.getTriggerDetail(); // 获取N边沿触发详情
                    icon.setImageResource(DRAWABLEID_NTH); // 设置N边沿触发图标
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        bean.setVisible(bean.getChannel() == (nEdge.getTriggerSource().getIndex() + 1)); // 设置可见性（仅触发源通道可见）
                        if (bean.getChannel() == (nEdge.getTriggerSource().getIndex() + 1)) { // 如果是触发源通道
                            bean.setLine(MainBeanTopRight.LINE_NULL); // 设置为无特殊线位置
                        }
                    }
                    detail.setData(list); // 更新触发状态栏数据
                    break;
                case TopLayoutTrigger.DETAIL_RUNT: // 欠幅触发
                    TopMsgTriggerRunt runt = (TopMsgTriggerRunt) topMsgTrigger.getTriggerDetail(); // 获取欠幅触发详情
                    icon.setImageResource(DRAWABLEID_DPULSE); // 设置欠幅触发图标
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        bean.setVisible(bean.getChannel() == (runt.getTriggerSource().getIndex() + 1)); // 设置可见性（仅触发源通道可见）
                        if (bean.getChannel() == (runt.getTriggerSource().getIndex() + 1)) { // 如果是触发源通道
                            bean.setLine(MainBeanTopRight.LINE_NULL); // 设置为无特殊线位置
                        }
                    }
                    detail.setData(list); // 更新触发状态栏数据
                    break;
                case TopLayoutTrigger.DETAIL_SLOPE: // 斜率触发
                    TopMsgTriggerSlope slope = (TopMsgTriggerSlope) topMsgTrigger.getTriggerDetail(); // 获取斜率触发详情
                    if (slope.getEdge().getIndex() == 0) { // 如果边沿为上升
                        icon.setImageResource(DRAWABLEID_SLOPE); // 设置上升斜率触发图标
                    } else if (slope.getEdge().getIndex() == 1) { // 如果边沿为下降
                        icon.setImageResource(DRAWABLEID_SLOPE_N); // 设置下降斜率触发图标
                    } else { // 如果边沿为双边沿
                        icon.setImageResource(DRAWABLEID_SLOPE_D); // 设置双边沿斜率触发图标
                    }
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        bean.setVisible(bean.getChannel() == (slope.getTriggerSource().getIndex() + 1)); // 设置可见性
                        if (bean.getChannel() == (slope.getTriggerSource().getIndex() + 1)) { // 如果是触发源通道
                            bean.setLine(MainBeanTopRight.LINE_NULL); // 设置为无特殊线位置
                        }
                    }
                    detail.setData(list); // 更新触发状态栏数据
                    break;
                case TopLayoutTrigger.DETAIL_TIMEOUT: // 超时触发
                    TopMsgTriggerTimeout timeout = (TopMsgTriggerTimeout) topMsgTrigger.getTriggerDetail(); // 获取超时触发详情
                    if (timeout.getPolar().getIndex() == 0) { // 如果极性为正
                        icon.setImageResource(DRAWABLEID_TIMEOUT); // 设置正超时触发图标
                    } else if (timeout.getPolar().getIndex() == 1) { // 如果极性为负
                        icon.setImageResource(DRAWABLEID_TIMEOUT_N); // 设置负超时触发图标
                    } else { // 如果极性为双边沿
                        icon.setImageResource(DRAWABLEID_TIMEOUT_D); // 设置双边沿超时触发图标
                    }
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        bean.setVisible(bean.getChannel() == (timeout.getTriggerSource().getIndex() + 1)); // 设置可见性
                        if (bean.getChannel() == (timeout.getTriggerSource().getIndex() + 1)) { // 如果是触发源通道
                            switch (timeout.getPolar().getIndex()) { // 根据极性分支处理
                                case 0: // 正极性
                                    bean.setLine(MainBeanTopRight.LINE_BOTTOM); // 设置为底线位置
                                    break;
                                case 1: // 负极性
                                    bean.setLine(MainBeanTopRight.LINE_TOP); // 设置为顶线位置
                                    break;
                                case 2: // 双边沿
                                    bean.setLine(MainBeanTopRight.LINE_NULL); // 设置为无特殊线位置
                                    break;
                            }
                        }
                    }
                    detail.setData(list); // 更新触发状态栏数据
                    break;
                case TopLayoutTrigger.DETAIL_VIDEO: // 视频触发
                    TopMsgTriggerVideo video = (TopMsgTriggerVideo) topMsgTrigger.getTriggerDetail(); // 获取视频触发详情
                    if (video.getPolar().getIndex() == 0) { // 如果极性为行
                        icon.setImageResource(DRAWABLEID_VIDEOH); // 设置行触发图标
                    } else { // 如果极性为场
                        icon.setImageResource(DRAWABLEID_VIDEOL); // 设置场触发图标
                    }
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        bean.setVisible(bean.getChannel() == (video.getTriggerSource().getIndex() + 1)); // 设置可见性
                    }
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        if (bean.getChannel() == video.getTriggerSource().getIndex() + 1) { // 如果是触发源通道
                            bean.setLine(video.getPolar().getIndex() == 0 ? MainBeanTopRight.LINE_BOTTOM : MainBeanTopRight.LINE_TOP); // 设置线位置（行为底线，场为顶线）
                            String s; // 定义文本变量
                            s = video.getStandard().getText() + " " + video.getTrigger().getText(); // 组合视频标准和触发类型文本
                            if (video.isTriggerLine()) { // 如果触发特定行号
                                s += " " + video.getLineDetail().getValue(); // 添加行号详情
                            }
                            bean.setText(s.replaceAll("\n", "")); // 设置文本（去除换行符）
                            bean.setShowNumber(false); // 不显示通道号
                            icon.setImageResource(video.getPolar().getIndex() == 0 ? DRAWABLEID_VIDEOH : DRAWABLEID_VIDEOL); // 再次设置触发图标（确保一致性）
                            break; // 跳出循环
                        }
                    }
                    detail.setData(list); // 更新触发状态栏数据
                    break;
                case TopLayoutTrigger.DETAIL_S1: // 串行触发S1
                case TopLayoutTrigger.DETAIL_S2: // 串行触发S2
                case TopLayoutTrigger.DETAIL_S3: // 串行触发S3
                case TopLayoutTrigger.DETAIL_S4: // 串行触发S4
                    TopMsgTriggerSerials s = (TopMsgTriggerSerials) topMsgTrigger.getTriggerDetail(); // 获取串行触发详情
                    icon.setImageResource(DRAWABLEID_BUSTRIGGER); // 设置总线触发图标
                    String text1 = topMsgTrigger.getTriggerTitle().getText().replace(" ", ":"); // 获取触发标题文本（替换空格为冒号）
                    String text2 = s.getSerials().getName(); // 获取串行协议名称
                    String text3 = ""; // 初始化详情文本变量
                    Logger.d("MainTopLayoutRight:" + topMsgTrigger); // 打印调试日志
                    if (s.getSerialsDetail() != null) { // 如果串行详情不为空
                        // 根据串行详情类型分支处理，设置详情文本
                        if (s.getSerialsDetail() instanceof SerialsDetailUartData) { // UART数据触发
                            SerialsDetailUartData data = (SerialsDetailUartData) s.getSerialsDetail(); // 获取UART数据详情
                            text3 = data.getUartDataEditTitle() + data.getUartDataCondition().getText() + data.getUartDataEdit().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailUart0Data) { // UART0数据触发
                            SerialsDetailUart0Data data = (SerialsDetailUart0Data) s.getSerialsDetail(); // 获取UART0数据详情
                            text3 = data.getUart0DataEditTitle() + data.getUart0DataCondition().getText() + data.getUart0DataEdit().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailUart1Data) { // UART1数据触发
                            SerialsDetailUart1Data data = (SerialsDetailUart1Data) s.getSerialsDetail(); // 获取UART1数据详情
                            text3 = data.getUart1DataEditTitle() + data.getUart1DataCondition().getText() + data.getUart1DataEdit().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailUartxData) { // UARTx数据触发
                            SerialsDetailUartxData data = (SerialsDetailUartxData) s.getSerialsDetail(); // 获取UARTx数据详情
                            text3 = data.getUartxDataEditTitle() + data.getUartxDataCondition().getText() + data.getUartxDataEdit().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailLinFrameId) { // LIN帧ID触发
                            SerialsDetailLinFrameId data = (SerialsDetailLinFrameId) s.getSerialsDetail(); // 获取LIN帧ID详情
                            text3 = data.getLinFrameIdEditEditTitle() + "=" + data.getLinFrameIdEditEdit().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailLinIdData) { // LIN ID数据触发
                            SerialsDetailLinIdData data = (SerialsDetailLinIdData) s.getSerialsDetail(); // 获取LIN ID数据详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getLinIdDataIdTitle() + "=" + data.getLinIdDataId().getValue()
                                    + " " + data.getLinIdDataDataTitle() + "=" + data.getLinIdDataData().getValue(); // 组合ID和数据文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailCanRemoteId) { // CAN远程ID触发
                            SerialsDetailCanRemoteId data = (SerialsDetailCanRemoteId) s.getSerialsDetail(); // 获取CAN远程ID详情
                            text3 = data.getCanRemoteIdEditTitle() + "=" + data.getCanRemoteIdEdit().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailCanDataId) { // CAN数据ID触发
                            SerialsDetailCanDataId data = (SerialsDetailCanDataId) s.getSerialsDetail(); // 获取CAN数据ID详情
                            text3 = data.getCanDataIdEditTitle() + "=" + data.getCanDataIdEdit().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailCanRdId) { // CAN RD ID触发
                            SerialsDetailCanRdId data = (SerialsDetailCanRdId) s.getSerialsDetail(); // 获取CAN RD ID详情
                            text3 = data.getCanRdIdEditTitle() + "=" + data.getCanRdIdEdit().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailCanIdData) { // CAN ID数据触发
                            SerialsDetailCanIdData data = (SerialsDetailCanIdData) s.getSerialsDetail(); // 获取CAN ID数据详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getCanIdDataId().getValue() + "/" + data.getCanIdDataDlc().getValue() + "/" + data.getCanIdDataData().getValue(); // 组合ID/DLC/数据文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailSpiData) { // SPI数据触发
                            text2 = ""; // 清空协议名称
                            SerialsDetailSpiData data = (SerialsDetailSpiData) s.getSerialsDetail(); // 获取SPI数据详情
                            text3 = data.getSpiDataData().getValue(); // 设置数据文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2cNoAckInAdr) { // I2C地址无ACK触发
                            text2 = ""; // 清空协议名称
                            SerialsDetailI2cNoAckInAdr data = (SerialsDetailI2cNoAckInAdr) s.getSerialsDetail(); // 获取I2C地址无ACK详情
                            text3 = data.getI2cNoAckInAdrDataTitle() + "=" + data.getI2cNoAckInAdrData().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2cFrame1) { // I2C帧1触发
                            text2 = ""; // 清空协议名称
                            SerialsDetailI2cFrame1 data = (SerialsDetailI2cFrame1) s.getSerialsDetail(); // 获取I2C帧1详情
                            text3 = data.getI2cFrame1AddrTitle() + "=" + data.getI2cFrame1Addr().getValue()
                                    + " " + data.getI2cFrame1DataTitle() + "=" + data.getI2cFrame1Data().getValue(); // 组合地址和数据文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2cFrame2) { // I2C帧2触发
                            text2 = ""; // 清空协议名称
                            SerialsDetailI2cFrame2 data = (SerialsDetailI2cFrame2) s.getSerialsDetail(); // 获取I2C帧2详情
                            text3 = data.getI2cFrame2AddrTitle() + "=" + data.getI2cFrame2Addr().getValue()
                                    + " " + data.getI2cFrame2Data1Title() + "=" + data.getI2cFrame2Data1().getValue()
                                    + " " + data.getI2cFrame2Data2Title() + "=" + data.getI2cFrame2Data2().getValue(); // 组合地址和数据1、数据2文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2cRomData) { // I2C ROM数据触发
                            text2 = ""; // 清空协议名称
                            SerialsDetailI2cRomData data = (SerialsDetailI2cRomData) s.getSerialsDetail(); // 获取I2C ROM数据详情
                            text3 = data.getI2cRomDataDataTitle() + data.getI2cRomDataCondition().getText() + data.getI2cRomDataData().getValue(); // 组合文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2c10WriteFrame) { // I2C 10位写帧触发
                            text2 = ""; // 清空协议名称
                            SerialsDetailI2c10WriteFrame data = (SerialsDetailI2c10WriteFrame) s.getSerialsDetail(); // 获取I2C 10位写帧详情
                            text3 = data.getI2c10WriteFrameAddrTitle() + "=" + data.getI2c10WriteFrameAddr().getValue()
                                    + " " + data.getI2c10WriteFrameDataTitle() + "=" + data.getI2c10WriteFrameData().getValue(); // 组合地址和数据文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429Label) { // ARINC429标签触发
                            SerialsDetailArinc429Label data = (SerialsDetailArinc429Label) s.getSerialsDetail(); // 获取ARINC429标签详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getArinc429LabelLabelTitle() + "=" + data.getArinc429LabelLabel().getValue(); // 组合标签文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429Sdi) { // ARINC429 SDI触发
                            SerialsDetailArinc429Sdi data = (SerialsDetailArinc429Sdi) s.getSerialsDetail(); // 获取ARINC429 SDI详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getArinc429SdiLabelTitle() + "=" + data.getArinc429SdiLabel().getValue(); // 组合SDI文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429Data) { // ARINC429数据触发
                            SerialsDetailArinc429Data data = (SerialsDetailArinc429Data) s.getSerialsDetail(); // 获取ARINC429数据详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getArinc429DataDataTitle() + "=" + data.getArinc429DataData().getValue(); // 组合数据文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429Ssm) { // ARINC429 SSM触发
                            SerialsDetailArinc429Ssm data = (SerialsDetailArinc429Ssm) s.getSerialsDetail(); // 获取ARINC429 SSM详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getArinc429SsmLabelTitle() + "=" + data.getArinc429SsmLabel().getValue(); // 组合SSM文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429LabelSdi) { // ARINC429标签SDI触发
                            SerialsDetailArinc429LabelSdi data = (SerialsDetailArinc429LabelSdi) s.getSerialsDetail(); // 获取ARINC429标签SDI详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getArinc429LabelSdiLabelTitle() + "=" + data.getArinc429LabelSdiLabel().getValue()
                                    + " " + data.getArinc429LabelSdiSdiTitle() + "=" + data.getArinc429LabelSdiSdi().getValue(); // 组合标签和SDI文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429LabelData) { // ARINC429标签数据触发
                            SerialsDetailArinc429LabelData data = (SerialsDetailArinc429LabelData) s.getSerialsDetail(); // 获取ARINC429标签数据详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getArinc429LabelDataLabelTitle() + "=" + data.getArinc429LabelDataLabel().getValue()
                                    + " " + data.getArinc429LabelDataDataTitle() + "=" + data.getArinc429LabelDataData().getValue(); // 组合标签和数据文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429LabelSsm) { // ARINC429标签SSM触发
                            SerialsDetailArinc429LabelSsm data = (SerialsDetailArinc429LabelSsm) s.getSerialsDetail(); // 获取ARINC429标签SSM详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getArinc429LabelSsmLabelTitle() + "=" + data.getArinc429LabelSsmLabel().getValue()
                                    + " " + data.getArinc429LabelSsmSsmTitle() + "=" + data.getArinc429LabelSsmSsm().getValue(); // 组合标签和SSM文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailM1553bCsWord) { // M1553B CS字触发
                            SerialsDetailM1553bCsWord data = (SerialsDetailM1553bCsWord) s.getSerialsDetail(); // 获取M1553B CS字详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getM1553bCsWordCsWordTitle() + "=" + data.getM1553bCsWordCsWord().getValue(); // 组合CS字文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailM1553bRtAddr) { // M1553B RT地址触发
                            SerialsDetailM1553bRtAddr data = (SerialsDetailM1553bRtAddr) s.getSerialsDetail(); // 获取M1553B RT地址详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getM1553bRtAddrRtAddrTitle() + "=" + data.getM1553bRtAddrRtAddr().getValue(); // 组合RT地址文本
                        } else if (s.getSerialsDetail() instanceof SerialsDetailM1553bDataWord) { // M1553B数据字触发
                            SerialsDetailM1553bDataWord data = (SerialsDetailM1553bDataWord) s.getSerialsDetail(); // 获取M1553B数据字详情
                            text2 = ""; // 清空协议名称
                            text3 = data.getM1553bDataWordDataTitle() + "=" + data.getM1553bDataWordData().getValue(); // 组合数据字文本
                        }
                    }
                    String text = text1
                            + (StrUtil.isEmpty(text2) ? "" : ("\n" + text2))
                            + (StrUtil.isEmpty(text3) ? "" : ("\n" + text3)); // 组合完整触发文本
                    Logger.i(TAG, "text:" + text); // 打印信息日志
                    for (MainBeanTopRight bean : list) { // 遍历触发电平数据列表
                        bean.setShowNumber(false); // 不显示通道号
                        if (bean.getChannel() == 0) { // 如果是特殊通道（通道0）
                            int colorResourceId = R.color.color_S1; // 定义颜色资源ID变量，默认为S1颜色
                            int triggerIndex = topMsgTrigger.getTriggerTitle().getIndex(); // 获取触发索引
                            if (triggerIndex == TopLayoutTrigger.DETAIL_S4) { // 如果是串行触发S4
                                colorResourceId = R.color.color_S4; // 设置为S4颜色
                            } else if (triggerIndex == TopLayoutTrigger.DETAIL_S3) { // 如果是串行触发S3
                                colorResourceId = R.color.color_S3; // 设置为S3颜色
                            } else if (triggerIndex == TopLayoutTrigger.DETAIL_S2) { // 如果是串行触发S2
                                colorResourceId = R.color.color_S2; // 设置为S2颜色
                            }
                            bean.setColorResId(colorResourceId); // 设置颜色资源ID
                            bean.setText(text); // 设置触发文本
                            bean.setLine(MainBeanTopRight.LINE_NULL); // 设置为无特殊线位置
                            bean.setVisible(true); // 设置可见
                        } else { // 其他通道
                            bean.setVisible(false); // 隐藏其他通道
                        }
                    }
                    detail.setData(list); // 更新触发状态栏数据
                    break;
            }
        }
    };

    /**
     * 触发状态栏点击监听器
     *
     * 当用户点击触发状态栏中的某项时，显示Toast提示该项的详细信息。
     */
    private MTriggerStateBar.OnClickListener onClickListener = new MTriggerStateBar.OnClickListener() {
        @Override
        public void onClick(MTriggerStateBar view, MainBeanTopRight item) {
            DToast.get().show(item.toString()); // 显示Toast提示该项的字符串表示
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
        return true; // 返回true，消费触摸事件
    }

    /**
     * 命令到UI消息消费者
     *
     * 当收到命令到UI消息时，根据命令标志更新自动/正常模式文本显示。
     * FLAG_TRIGGER_MODE标志用于更新触发模式。
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分支处理
                case CommandMsgToUI.FLAG_TRIGGER_MODE: // 触发模式标志
                    int mode = Integer.parseInt(commandMsgToUI.getParam()); // 解析触发模式参数
                    an.setText(mode == 0 ? "A" : "N"); // 设置自动/正常模式文本（0=Auto，其他=Normal）
                    break;
            }
        }
    };

}