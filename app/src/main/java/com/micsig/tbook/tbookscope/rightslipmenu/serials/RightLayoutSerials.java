package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.smart.Property;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBus;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusStruct;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/*
 * +--------------------------------------------------------------------------+
 * |                       串口总线配置右侧滑出菜单布局                         |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— 串口总线配置的总入口布局          |
 * | 核心职责: 管理S1~S4串口通道的开关、协议类型选择(UART/CAN/LIN/SPI/         |
 * |          I2C/M429/M1553B)及各协议子布局的显示切换，                       |
 * |          通过RxBus广播配置变更消息                                        |
 * | 架构设计: RelativeLayout子类，包含7种协议子布局(Uart/Lin/Can/Spi/         |
 * |          I2c/M429/M1553B)的容器，通过setVisibility切换显示，             |
 * |          采用观察者模式监听RxBus事件和EventBus总线参数变更               |
 * | 数据流向: 用户UI操作 → RightLayoutSerials → RightMsgSerials → RxBus     |
 * |          → 消费方(电平设置页面等)                                        |
 * |          外部事件(RxBus/EventBus/SCPI) → RightLayoutSerials → UI更新    |
 * | 依赖关系: RightLayoutSerialsXxx(7种协议子布局), RightMsgSerials,         |
 * |          RxBus, Command(SCPI), SerialBusManage, ChannelFactory,          |
 * |          CacheUtil, TChan                                                |
 * | 使用场景: 示波器右侧菜单中串口总线配置面板，用户在此选择协议、            |
 * |          开关通道、添加/删除串口通道                                      |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/4.
 */

/**
 * 串口总线配置右侧滑出菜单布局
 * <p>
 * 管理S1~S4四个串口通道的开关状态、协议类型选择及各协议参数子布局。
 * 当用户切换协议类型时，显示对应的子布局并隐藏其他子布局。
 * 通过RxBus广播配置变更消息，同时监听外部事件更新UI。
 * </p>
 */
public class RightLayoutSerials extends RelativeLayout {
    /** 日志标签 */
    private static final String TAG = RightLayoutSerials.class.getSimpleName(); // 日志标签

    /** UART协议类型常量 */
    public static final int SERIALS_UART = 0; // UART协议类型索引
    /** LIN协议类型常量 */
    public static final int SERIALS_LIN = 1; // LIN协议类型索引
    /** CAN协议类型常量 */
    public static final int SERIALS_CAN = 2; // CAN协议类型索引
    /** SPI协议类型常量 */
    public static final int SERIALS_SPI = 3; // SPI协议类型索引
    /** I2C协议类型常量 */
    public static final int SERIALS_I2C = 4; // I2C协议类型索引
    /** ARINC429协议类型常量 */
    public static final int SERIALS_M429 = 5; // ARINC429协议类型索引
    /** MIL-STD-1553B协议类型常量 */
    public static final int SERIALS_M1553B = 6; // MIL-STD-1553B协议类型索引
    /** 显示开关布局 */
    private LinearLayout llDisplaySwitch; // 显示开关布局
    /**
     * 根据串口协议类型常量获取对应的Property索引
     *
     * @param SERIALS 串口协议类型常量（SERIALS_UART等）
     * @return 对应的Property索引，未匹配返回-1
     */
    public static int getPropertyIndex(int SERIALS) {
        int idx = -1; // 默认返回-1表示未匹配
        switch (SERIALS) { // 根据协议类型匹配Property索引
            case SERIALS_UART:
                idx = Property.BUS_UART;
                break;
            case SERIALS_LIN:
                idx = Property.BUS_LIN;
                break;
            case SERIALS_CAN:
                idx = Property.BUS_CAN;
                break;
            case SERIALS_SPI:
                idx = Property.BUS_SPI;
                break;
            case SERIALS_I2C:
                idx = Property.BUS_I2C;
                break;
            case SERIALS_M429:
                idx = Property.BUS_429;
                break;
            case SERIALS_M1553B:
                idx = Property.BUS_1553B;
                break;
        }
        return idx;
    }

    /** 上下文引用 */
    private Context context; // 上下文引用
    /** 串口开关控件 */
    private MSwitchBox serialsCheck; // 串口开关控件
    /** 协议类型选择控件 */
    private RightViewSelect serials; // 协议类型选择控件
    /** 协议详情容器布局 */
    private ConstraintLayout detail; // 协议详情容器布局
    /** UART协议子布局 */
    private RightLayoutSerialsUart uart; // UART协议子布局
    /** LIN协议子布局 */
    private RightLayoutSerialsLin lin; // LIN协议子布局
    /** CAN协议子布局 */
    private RightLayoutSerialsCan can; // CAN协议子布局
    /** SPI协议子布局 */
    private RightLayoutSerialsSpi spi; // SPI协议子布局
    /** I2C协议子布局 */
    private RightLayoutSerialsI2c i2c; // I2C协议子布局
    /** ARINC429协议子布局 */
    private RightLayoutSerialsM429 m429; // ARINC429协议子布局
    /** MIL-STD-1553B协议子布局 */
    private RightLayoutSerialsM1553B m1553b; // MIL-STD-1553B协议子布局

    /** 串口配置消息体 */
    private RightMsgSerials msgSerials; // 串口配置消息体
    /**
     * value 1,2,3,4
     */
    private int serialsNumber; // 串口通道编号(1=S1,2=S2,3=S3,4=S4)
    /** 通道标题栏布局 */
    private ConstraintLayout topChannelTitle, clTopChannelGroup; // 通道标题栏和通道组布局
    /** 通道标题文本 */
    private TextView channelTitle; // 通道标题文本(如"S1")
    /** 间距控件 */
    private View space; // 间距控件
    /** 删除/添加通道按钮 */
    private TextView btnDeleteChannel, btnAddChannel; // 删除通道和添加通道按钮
    /** Math/Ref/Serials单选按钮 */
    private RadioButton channelMath, channelRef, channelSerials; // Math/Ref/Serials通道类型单选按钮
    /** 是否需要更新MasterView位置 */
    private boolean needUpdateMasterLocation = false; // 是否需要更新MasterView位置
    /** 根视图 */
    private ViewGroup rootView; // 根视图
    /** 文本页面是否显示 */
    private boolean isSerialsWordShow = false;//文本页面是否显示

    /**
     * 单参数构造函数
     *
     * @param context 上下文
     */
    public RightLayoutSerials(Context context) {
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public RightLayoutSerials(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数，初始化视图和控件
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutSerials(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文引用
        initView(attrs, defStyleAttr); // 初始化视图
        initControl(); // 初始化控件（RxBus订阅等）
    }

    /**
     * 初始化视图控件，加载布局并绑定各子布局和事件监听器
     *
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    @SuppressLint("SetTextI18n")
    private void initView(AttributeSet attrs, int defStyleAttr) {
        rootView = (ViewGroup) View.inflate(context, R.layout.layout_right_serials, this); // 加载串口配置布局
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightLayoutSerials); // 获取自定义属性集
        serialsNumber = ta.getInt(R.styleable.RightLayoutSerials_serialsNumber, CacheUtil.S1); // 读取串口编号属性，默认S1
        ta.recycle(); // 回收TypedArray
        space = findViewById(R.id.space); // 间距控件
        topChannelTitle = findViewById(R.id.top_channel_title); // 通道标题栏布局
        channelTitle = findViewById(R.id.channel_title); // 通道标题文本
        btnDeleteChannel = findViewById(R.id.btn_delete_channel); // 删除通道按钮
        llDisplaySwitch = findViewById(R.id.ll_display_switch); // 显示开关布局
        serialsCheck = (MSwitchBox) findViewById(R.id.serialsCheckDetail); // 串口开关控件
        serials = (RightViewSelect) findViewById(R.id.serialsType); // 协议类型选择控件
        detail = findViewById(R.id.detail); // 协议详情容器布局
        uart = (RightLayoutSerialsUart) findViewById(R.id.uartRightSerials); // UART子布局
        lin = (RightLayoutSerialsLin) findViewById(R.id.linRightSerials); // LIN子布局
        can = (RightLayoutSerialsCan) findViewById(R.id.canRightSerials); // CAN子布局
        spi = (RightLayoutSerialsSpi) findViewById(R.id.spiRightSerials); // SPI子布局
        i2c = (RightLayoutSerialsI2c) findViewById(R.id.i2cRightSerials); // I2C子布局
        m429 = (RightLayoutSerialsM429) findViewById(R.id.m429RightSerials); // M429子布局
        m1553b = (RightLayoutSerialsM1553B) findViewById(R.id.m1553bRightSerials); // M1553B子布局

        SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber))); // 获取串口通道对象
        if (serialChannel == null) return; // 通道为空则直接返回
        uart.setSerialsNumber(serialsNumber); // 设置UART子布局的串口编号
        lin.setSerialsNumber(serialsNumber); // 设置LIN子布局的串口编号
        can.setSerialsNumber(serialsNumber); // 设置CAN子布局的串口编号
        spi.setSerialsNumber(serialsNumber); // 设置SPI子布局的串口编号
        i2c.setSerialsNumber(serialsNumber); // 设置I2C子布局的串口编号
        m429.setSerialsNumber(serialsNumber); // 设置M429子布局的串口编号
        m1553b.setSerialsNumber(serialsNumber); // 设置M1553B子布局的串口编号

        clTopChannelGroup = findViewById(R.id.cl_top_channel_group); // 通道组布局
        channelMath = findViewById(R.id.channelMath); // Math单选按钮
        channelRef = findViewById(R.id.channelRef); // Ref单选按钮
        channelSerials = findViewById(R.id.channelSerials); // Serials单选按钮
        btnAddChannel = findViewById(R.id.btn_add_channel); // 添加通道按钮
        channelMath.setOnClickListener(onClickListener); // 绑定Math点击监听
        channelRef.setOnClickListener(onClickListener); // 绑定Ref点击监听
        channelSerials.setOnClickListener(onClickListener); // 绑定Serials点击监听
        btnAddChannel.setOnClickListener(onClickListener); // 绑定添加通道点击监听

        btnDeleteChannel.setOnClickListener(onClickListener); // 绑定删除通道点击监听
        serials.setOnItemClickListener(onSerialsItemClickListener); // 绑定协议类型选择监听
        serialsCheck.setOnToggleStateChangedListener(onToggleStateChangedListener); // 绑定开关状态变更监听
        uart.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 绑定UART详情消息监听
        lin.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 绑定LIN详情消息监听
        can.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 绑定CAN详情消息监听
        spi.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 绑定SPI详情消息监听
        i2c.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 绑定I2C详情消息监听
        m429.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 绑定M429详情消息监听
        m1553b.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 绑定M1553B详情消息监听

        initData(); // 初始化默认数据

        setControlColorByChIdx(TChan.toSerialTChan(serialsNumber)); // 根据通道索引设置控件颜色
        channelTitle.setText("S" + serialsNumber); // 设置通道标题（如"S1"）
    }
    /**
     * 根据通道索引设置控件颜色
     *
     * @param chIdx UI通道索引
     */
    private void setControlColorByChIdx(int chIdx){
        serialsCheck.setControlColorByChIdx(chIdx); // 设置开关控件颜色
        serials.setControlColorByChIdx(chIdx); // 设置协议选择控件颜色
        channelTitle.setTextColor(TChan.getChannelColor(context, chIdx)); // 设置通道标题文字颜色
        channelSerials.setTextColor(TChan.getChannelColor(context, chIdx)); // 设置Serials单选按钮文字颜色
    }

    /**
     * 初始化控件，订阅RxBus事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅右侧菜单其他消息
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS_CLOSE).subscribe(consumerRightSerials); // 订阅串口关闭事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅SCPI命令回传事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_NORMAL_STATE).subscribe(consumerShowNormalState); // 订阅正常/共享模式状态事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_SERIALS).subscribe(consumerOtherChannelCanAdd); // 订阅其他通道可添加状态事件

        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialsWordVisible); // 订阅串口文本页面可见性事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL).subscribe(consumerDeleteChannel); // 订阅删除其他通道事件


    }

    /**
     * 初始化默认数据，创建消息体并设置默认值
     */
    private void initData() {
        String[] ss = context.getResources().getStringArray(R.array.serialsType); // 获取协议类型字符串数组
        msgSerials = new RightMsgSerials(); // 创建串口配置消息对象
        msgSerials.setSerialsNumber(serialsNumber); // 设置消息体的串口编号
        msgSerials.setSerialsCheck(serialsNumber, serialsCheck.isState()); // 设置消息体的串口开关状态
        msgSerials.setSerialsType(new RightBeanSelect(SERIALS_UART, ss[SERIALS_UART], true)); // 设置消息体的协议类型（默认UART）
    }

    /**
     * 从缓存恢复配置状态，设置各控件的选中值和可见性，
     * 并向硬件发送对应的SCPI命令
     */
    private void setCache() {
        //因为此处初始化之后会发送消息到显示阈值电平的数值的地方，所以需要先把各个通道的垂直档位先设置下以便于计算阈值电平时使用
//        int ch1VScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_EXTENT_ID+TChan.Ch1);
//        int ch2VScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_EXTENT_ID+TChan.Ch2);
//        int ch3VScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_EXTENT_ID+TChan.Ch3);
//        int ch4VScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_EXTENT_ID+TChan.Ch4);
//        int ch5VScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_EXTENT_ID+TChan.Ch5);
//        int ch6VScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_EXTENT_ID+TChan.Ch6);
//        int ch7VScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_EXTENT_ID+TChan.Ch7);
//        int ch8VScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_EXTENT_ID+TChan.Ch8);
//
//        ChannelFactory.getDynamicChannel(ChannelFactory.CH1).setVScaleId(ch1VScaleId);
//        ChannelFactory.getDynamicChannel(ChannelFactory.CH2).setVScaleId(ch2VScaleId);
//        ChannelFactory.getDynamicChannel(ChannelFactory.CH3).setVScaleId(ch3VScaleId);
//        ChannelFactory.getDynamicChannel(ChannelFactory.CH4).setVScaleId(ch4VScaleId);
//        ChannelFactory.getDynamicChannel(ChannelFactory.CH5).setVScaleId(ch5VScaleId);
//        ChannelFactory.getDynamicChannel(ChannelFactory.CH6).setVScaleId(ch6VScaleId);
//        ChannelFactory.getDynamicChannel(ChannelFactory.CH7).setVScaleId(ch7VScaleId);
//        ChannelFactory.getDynamicChannel(ChannelFactory.CH8).setVScaleId(ch8VScaleId);
        TChan.foreachChan((uiCh)->{ // 遍历所有通道
            int chVScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_SCALE_ID +uiCh); // 获取该通道的垂直档位ID
            Channel chan= ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(uiCh)); // 获取通道对象
            if (chan!=null) { // 通道非空时
                chan.setVScaleId(chVScaleId); // 设置垂直档位（用于阈值电平计算）
            }
        });

        for (int i = 0; i < detail.getChildCount(); i++) { // 遍历详情容器所有子布局
            detail.getChildAt(i).setVisibility(GONE); // 先隐藏所有协议子布局
        }
        String[] ss = context.getResources().getStringArray(R.array.serialsType); // 获取协议类型字符串数组
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber))); // 获取当前串口通道对象
        if (serialChannel == null) return; // 通道为空则直接返回
        boolean checkKey = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + serialsNumber); // 读取缓存中的串口开关状态
        serialsCheck.setState(checkKey); // 设置开关控件状态
        msgSerials.setSerialsCheck(serialsNumber, checkKey); // 同步消息体的开关状态
        int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber); // 读取缓存中的协议类型索引
        SerialBusManage serialBusManage = SerialBusManage.getInstance(); // 获取串口总线管理器单例
        SerialBus serialBus = serialBusManage.getSerialBus(TChan.toSerialTChan(serialsNumber)); // 获取当前串口的串口总线对象
        boolean isAddByUser = isAddByUser(serialsNumber); // 判断该通道是否已被用户添加
        switch (index) { // 根据协议类型索引恢复对应子布局
            case SERIALS_UART: // UART协议
                uart.setVisibility(VISIBLE); // 显示UART子布局
                serialChannel.setBusType(IBus.UART); // 设置通道总线类型为UART
                if (isAddByUser) { // 用户已添加该通道
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) { // 总线不可见且归属当前通道
                        serialBus.setVisible(true); // 设置总线可见
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_UART); // 通知标题变更
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_UART, ss[SERIALS_UART], true)); // 设置消息体协议类型
                msgSerials.setSerialsDetails(uart.getMsgDetailsUart()); // 设置消息体UART详情数据
                break;
            case SERIALS_LIN: // LIN协议
                lin.setVisibility(VISIBLE); // 显示LIN子布局
                serialChannel.setBusType(IBus.LIN); // 设置通道总线类型为LIN
                if (isAddByUser) { // 用户已添加该通道
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) { // 总线不可见且归属当前通道
                        serialBus.setVisible(true); // 设置总线可见
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_LIN); // 通知标题变更
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_LIN, ss[SERIALS_LIN], true)); // 设置消息体协议类型
                msgSerials.setSerialsDetails(lin.getMsgDetailsLin()); // 设置消息体LIN详情数据
                break;
            case SERIALS_CAN: // CAN协议
                can.setVisibility(VISIBLE); // 显示CAN子布局
                serialChannel.setBusType(IBus.CAN); // 设置通道总线类型为CAN
                if (isAddByUser) { // 用户已添加该通道
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) { // 总线不可见且归属当前通道
                        serialBus.setVisible(true); // 设置总线可见
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_CAN); // 通知标题变更
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_CAN, ss[SERIALS_CAN], true)); // 设置消息体协议类型
                msgSerials.setSerialsDetails(can.getMsgDetailsCan()); // 设置消息体CAN详情数据
                break;
            case SERIALS_SPI: // SPI协议
                spi.setVisibility(VISIBLE); // 显示SPI子布局
                serialChannel.setBusType(IBus.SPI); // 设置通道总线类型为SPI
                if (isAddByUser) { // 用户已添加该通道
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) { // 总线不可见且归属当前通道
                        serialBus.setVisible(true); // 设置总线可见
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_SPI); // 通知标题变更
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_SPI, ss[SERIALS_SPI], true)); // 设置消息体协议类型
                msgSerials.setSerialsDetails(spi.getMsgDetailsSpi()); // 设置消息体SPI详情数据
                break;
            case SERIALS_I2C: // I2C协议
                i2c.setVisibility(VISIBLE); // 显示I2C子布局
                serialChannel.setBusType(IBus.I2C); // 设置通道总线类型为I2C
                if (isAddByUser) { // 用户已添加该通道
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) { // 总线不可见且归属当前通道
                        serialBus.setVisible(true); // 设置总线可见
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_I2C); // 通知标题变更
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_I2C, ss[SERIALS_I2C], true)); // 设置消息体协议类型
                msgSerials.setSerialsDetails(i2c.getMsgDetailsI2c()); // 设置消息体I2C详情数据
                break;
            case SERIALS_M429: // ARINC429协议
                m429.setVisibility(VISIBLE); // 显示M429子布局
                serialChannel.setBusType(IBus.ARINC429); // 设置通道总线类型为ARINC429
                if (isAddByUser) { // 用户已添加该通道
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) { // 总线不可见且归属当前通道
                        serialBus.setVisible(true); // 设置总线可见
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_429); // 通知标题变更
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_M429, ss[SERIALS_M429], true)); // 设置消息体协议类型
                msgSerials.setSerialsDetails(m429.getMsgDetailsM429()); // 设置消息体M429详情数据
                break;
            case SERIALS_M1553B: // MIL-STD-1553B协议
                m1553b.setVisibility(VISIBLE); // 显示M1553B子布局
                serialChannel.setBusType(IBus.MILSTD1553B); // 设置通道总线类型为MILSTD1553B
                if (isAddByUser) { // 用户已添加该通道
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) { // 总线不可见且归属当前通道
                        serialBus.setVisible(true); // 设置总线可见
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_1553B); // 通知标题变更
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_M1553B, ss[SERIALS_M1553B], true)); // 设置消息体协议类型
                msgSerials.setSerialsDetails(m1553b.getMsgDetailsM1553B()); // 设置消息体M1553B详情数据
                break;
        }
        serials.setSelectIndex(index); // 设置协议类型选择控件的选中项
        Command.get().getChannel().Display(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)), checkKey && isAddByUser, false); // 下发通道显示SCPI命令
        Command.get().getBus().Type(serialsNumber - 1, index, false); // 下发总线类型SCPI命令
    }

    /**
     * 通过RxBus发送串口配置变更消息
     *
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) {
        msgSerials.setFromEventBus(isFromEventBus); // 设置消息来源标记
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_SERIALS, msgSerials); // 广播串口配置变更消息
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_SERIALS_FOLLOW, msgSerials); // 广播串口跟随消息
    }

    /**
     * 缓存加载事件消费者，从缓存恢复配置并标记加载完成
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache(); // 从缓存恢复配置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerials, true); // 标记串口菜单已加载
        }
    };

    /**
     * 右侧菜单其他消息消费者，同步串口开关状态并强制绘制最新数据
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
            if(msgSerials == null) return; // 消息体为空则跳过
            serialsCheck.setState(mainRightMsgOthers.getSerial(serialsNumber).isValue()); // 同步串口开关状态
            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + serialsNumber, String.valueOf(serialsCheck.isState())); // 保存开关状态到缓存
            msgSerials.setSerialsCheck(serialsNumber, serialsCheck.isState()); // 同步消息体开关状态

            if (mainRightMsgOthers.getSerial(serialsNumber).isValue() && isAddByUser(serialsNumber)) { // 开关开启且用户已添加
                SerialBusManage.getInstance().getSerialBus(TChan.toSerialTChan(serialsNumber)).forceDrawLastData(TChan.toSerialTChan(serialsNumber)); // 强制绘制最新串口数据
            }

//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                serialsCheck.setState(mainRightMsgOthers.getS1().isValue());
//                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1, String.valueOf(serialsCheck.isState()));
//                msgSerials.setSerialsCheck(serialsNumber,serialsCheck.isState());
//            } else if (serialsNumber == RightMsgSerials.SERIALS_S2) {
//                serialsCheck.setState(mainRightMsgOthers.getS2().isValue());
//                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2, String.valueOf(serialsCheck.isState()));
//                msgSerials.setSerialsCheck(serialsNumber,serialsCheck.isState());
//            } else if (serialsNumber == RightMsgSerials.SERIALS_S3) {
//                serialsCheck.setState(mainRightMsgOthers.getS3().isValue());
//                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3, String.valueOf(serialsCheck.isState()));
//                msgSerials.setSerialsCheck(serialsNumber,serialsCheck.isState());
//            } else if (serialsNumber == RightMsgSerials.SERIALS_S4) {
//                serialsCheck.setState(mainRightMsgOthers.getS4().isValue());
//                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4, String.valueOf(serialsCheck.isState()));
//                msgSerials.setSerialsCheck(serialsNumber,serialsCheck.isState());
//            }

//            boolean s1AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S1);
//            boolean s2AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S2);
//            boolean s3AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S3);
//            boolean s4AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S4);
//
//            if (mainRightMsgOthers.getS1().isValue() && s1AddByUser) {
//                SerialBusManage.getInstance().getSerialBus(TChan.S1).forceDrawLastData(TChan.S1);
//            }
//            if (mainRightMsgOthers.getS2().isValue() && s2AddByUser) {
//                SerialBusManage.getInstance().getSerialBus(TChan.S2).forceDrawLastData(TChan.S2);
//            }
//            if (mainRightMsgOthers.getS3().isValue() && s3AddByUser) {
//                SerialBusManage.getInstance().getSerialBus(TChan.S3).forceDrawLastData(TChan.S3);
//            }
//            if (mainRightMsgOthers.getS4().isValue() && s4AddByUser) {
//                SerialBusManage.getInstance().getSerialBus(TChan.S4).forceDrawLastData(TChan.S4);
//            }
        }
    };

    /**
     * 只处理关闭S1，S2.这个关闭最好是SCPI指令，不过SCPI没有这条指令，先用这个Key传信息
     */
    private Consumer<Integer> consumerRightSerials = new Consumer<Integer>() {
        @Override
        public void accept(Integer serialNo) throws Exception {
            if (serialsNumber == serialNo && serialsCheck.isState()) { // 匹配当前串口编号且开关已打开
                boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM); // 读取缩放状态
                boolean slowTimeBase = Tools.isSlowTimeBase(); // 判断是否慢时基模式
                if (zoom && slowTimeBase) { // 缩放且慢时基模式下不允许关闭
                    return; // 直接返回
                }
                serialsCheck.setState(!serialsCheck.isState()); // 切换开关状态（关闭）
                onToggleStateChangedListener.onToggleStateChanged(serialsCheck, serialsCheck.isState()); // 触发开关变更回调
            }
        }
    };
    /**
     * SCPI命令回传消费者，处理通道显示和总线类型的远程指令
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI=new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()){ // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_CHANNEL_DISPLAY:{ // 通道显示命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]); // 通道索引
                    boolean isOpen = Boolean.parseBoolean(params[1]); // 是否打开
                    if (TChan.toSerialTChan(serialsNumber) == TChan.toUiChNo(chIndex)) { // 匹配当前串口通道
                        if (!serialsCheck.isEnabled()){ // 开关控件不可用
                            return; // 直接返回
                        }
                        serialsCheck.setState(isOpen); // 设置开关状态
                        onToggleStateChangedListener.onToggleStateChanged(serialsCheck,isOpen); // 触发开关变更回调
                    }
                }break;
                case CommandMsgToUI.FLAG_Bus_Type: { // 总线类型命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]) + 1; // 通道索引（0起始+1转为1起始）
                    int type = Integer.parseInt(params[1]); // 总线类型
                    if (serialsNumber == chIndex) { // 匹配当前串口通道
                        serials.setSelectIndex(type); // 设置协议选择控件选中项
                        onSerialsItemClickListener.onItemClick(serials.getId(), new RightBeanSelect(type, ToolsSCPI.getBusType(type), serialsCheck.isState())); // 触发协议选择回调
                    }

                }break;
            }
        }
    };

    /**
     * 正常/共享模式状态消费者，切换S1~S4布局和Math/Ref/Serials共享布局样式
     */
    private Consumer<String> consumerShowNormalState = new Consumer<String>() {
        @Override
        public void accept(String String) throws Exception {
            String[] params = String.split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
            int channelNumber = Integer.parseInt(params[0]); // 通道编号
            boolean isNormal = Boolean.parseBoolean(params[1]); // 是否为正常模式
            if (channelNumber != TChan.toSerialTChan(serialsNumber)) return; // 非当前通道则跳过
            if (isNormal) {//S1...S4布局样式
                llDisplaySwitch.setVisibility(View.VISIBLE);//switch 显示
                topChannelTitle.setVisibility(View.VISIBLE);//Sx delete按钮 显示
                space.setVisibility(View.GONE);//空占位
                clTopChannelGroup.setVisibility(View.GONE);//Math/Ref/Serial 不显示
            } else {//Math/Ref/Serials共同显示布局样式
                llDisplaySwitch.setVisibility(View.INVISIBLE);//switch 不显示
                topChannelTitle.setVisibility(View.INVISIBLE);//Sx delete按钮 不显示
                space.setVisibility(View.GONE);//空占位
                clTopChannelGroup.setVisibility(View.VISIBLE);//Math/Ref/Serial 显示
            }
        }
    };

    /**
     * 其他通道可添加状态消费者，控制Math/Ref/Serials按钮的可用性
     */
    private Consumer<String> consumerOtherChannelCanAdd = new Consumer<String>() {
        @Override
        public void accept(String available) throws Throwable {
            if (channelMath == null || channelRef == null || channelSerials == null) return; // 控件未初始化则跳过
            String[] params = available.split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
            int serialsSlideIndex = Integer.parseInt(params[0]); // 串口滑出菜单索引
            boolean mathAvailable = Boolean.parseBoolean(params[1]); // Math通道是否可用
            boolean refAvailable = Boolean.parseBoolean(params[2]); // Ref通道是否可用
            boolean serialAvailable = Boolean.parseBoolean(params[3]); // Serial通道是否可用
            int curSerialsNumber = CacheUtil.S1; // 当前串口编号默认S1
            switch (serialsSlideIndex) { // 根据滑出菜单索引确定串口编号
                case MainViewGroup.RIGHTSLIP_S1:
                    curSerialsNumber = CacheUtil.S1; // S1
                    break;
                case MainViewGroup.RIGHTSLIP_S2:
                    curSerialsNumber = CacheUtil.S2; // S2
                    break;
                case MainViewGroup.RIGHTSLIP_S3:
                    curSerialsNumber = CacheUtil.S3; // S3
                    break;
                case MainViewGroup.RIGHTSLIP_S4:
                    curSerialsNumber = CacheUtil.S4; // S4
                    break;
            }
            if (curSerialsNumber == serialsNumber) { // 匹配当前串口编号
                channelMath.setEnabled(mathAvailable && !isSerialsWordShow); // Math可用性=外部可用且文本页面不显示
                channelRef.setEnabled(refAvailable && !isSerialsWordShow); // Ref可用性=外部可用且文本页面不显示
            } else { // 非当前串口
                channelMath.setEnabled(!isSerialsWordShow); // Math可用性=文本页面不显示
                channelRef.setEnabled(!isSerialsWordShow); // Ref可用性=文本页面不显示
            }
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM); // 读取缩放状态
            boolean slowTimeBase = Tools.isSlowTimeBase(); // 判断是否慢时基模式
            if (zoom && slowTimeBase) { // 缩放且慢时基模式下
                channelSerials.setEnabled(false); // 禁用Serials按钮
            }
        }
    };


    /**
     * 串口开关状态变更监听器，处理通道显示/隐藏逻辑
     */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM); // 读取缩放状态
            boolean slowTimeBase = Tools.isSlowTimeBase(); // 判断是否慢时基模式
            if (zoom && slowTimeBase) { // 缩放且慢时基模式下不允许打开
                serialsCheck.setState(false); // 强制关闭开关
                Command.get().getChannel().Display(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)), false,false); // 下发关闭显示命令
                return; // 直接返回
            }
            if (!serialsCheck.isState()) { // 开关切换为关闭状态
                ((MainActivity) context).getMainViewGroup().hideAllDialogSlip(); // 隐藏所有滑出对话框
            }
            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + serialsNumber, String.valueOf(serialsCheck.isState())); // 保存开关状态到缓存
            msgSerials.setSerialsCheck(serialsNumber, serialsCheck.isState()); // 同步消息体开关状态
            sendMsg(false); // 广播配置变更消息
            Command.get().getChannel().Display(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)), state && isAddByUser(serialsNumber), false); // 下发通道显示SCPI命令（开关打开且已添加时才显示）
//            这里暂时不需要移动MasterView位置
//            updateMaxChannelNumber();
//            if (needUpdateMasterLocation) {
//                needUpdateMasterLocation = false;
//                RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_MASTER_LOCATION, TChan.toSerialTChan(serialsNumber));
//            }
            //若没显示，根据状态显示串口通道
            SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(TChan.toSerialTChan(serialsNumber)); // 获取串口总线对象
            if (serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) { // 总线归属当前通道
                serialBus.setVisible(state && isAddByUser(serialsNumber)); // 设置总线可见性（开关打开且已添加）
            }

        }
    };

    /**
     * 协议类型选择项点击监听器，切换协议子布局并下发SCPI命令
     */
    private RightViewSelect.OnItemClickListener onSerialsItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            Tools.PrintControlsLocation("RightLayoutSerials", rootView); // 打印控件位置调试信息
            CursorManage.getInstance().setCursorTrace(true); // 开启光标追踪
            for (int i = 0; i < detail.getChildCount(); i++) { // 遍历详情容器所有子布局
                detail.getChildAt(i).setVisibility(GONE); // 先隐藏所有协议子布局
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber, String.valueOf(item.getIndex())); // 保存协议类型到缓存
            int ch = TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)); // 转换为FPGA通道编号
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(ch); // 获取串口通道对象
            int serialsChNo = TChan.toSerialTChan(serialsNumber); // 转换为TChan通道编号
            Command.get().getBus().Type(serialsNumber - 1, item.getIndex(), false); // 下发总线类型SCPI命令
            switch (item.getIndex()) { // 根据选中的协议类型显示对应子布局
                case SERIALS_UART: // UART协议
                    uart.setVisibility(VISIBLE); // 显示UART子布局
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_UART); // 通知标题变更
                    if (serialChannel != null) { // 通道非空
                        serialChannel.setBusType(IBus.UART); // 设置总线类型为UART
                    }
                    msgSerials.setSerialsType(item); // 设置消息体协议类型
                    msgSerials.setSerialsDetails(uart.getMsgDetailsUart()); // 设置消息体UART详情数据
                    break;
                case SERIALS_LIN: // LIN协议
                    lin.setVisibility(VISIBLE); // 显示LIN子布局
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_LIN); // 通知标题变更
                    if (serialChannel != null) { // 通道非空
                        serialChannel.setBusType(IBus.LIN); // 设置总线类型为LIN
                    }
                    msgSerials.setSerialsType(item); // 设置消息体协议类型
                    msgSerials.setSerialsDetails(lin.getMsgDetailsLin()); // 设置消息体LIN详情数据
                    break;
                case SERIALS_CAN: // CAN协议
                    can.setVisibility(VISIBLE); // 显示CAN子布局
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_CAN); // 通知标题变更
                    if (serialChannel != null) { // 通道非空
                        serialChannel.setBusType(IBus.CAN); // 设置总线类型为CAN
                    }
                    msgSerials.setSerialsType(item); // 设置消息体协议类型
                    msgSerials.setSerialsDetails(can.getMsgDetailsCan()); // 设置消息体CAN详情数据
                    break;
                case SERIALS_SPI: // SPI协议
                    spi.setVisibility(VISIBLE); // 显示SPI子布局
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_SPI); // 通知标题变更
                    if (serialChannel != null) { // 通道非空
                        serialChannel.setBusType(IBus.SPI); // 设置总线类型为SPI
                    }
                    msgSerials.setSerialsType(item); // 设置消息体协议类型
                    msgSerials.setSerialsDetails(spi.getMsgDetailsSpi()); // 设置消息体SPI详情数据
                    break;
                case SERIALS_I2C: // I2C协议
                    i2c.setVisibility(VISIBLE); // 显示I2C子布局
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_I2C); // 通知标题变更
                    if (serialChannel != null) { // 通道非空
                        serialChannel.setBusType(IBus.I2C); // 设置总线类型为I2C
                    }
                    msgSerials.setSerialsType(item); // 设置消息体协议类型
                    msgSerials.setSerialsDetails(i2c.getMsgDetailsI2c()); // 设置消息体I2C详情数据
                    break;
                case SERIALS_M429: // ARINC429协议
                    m429.setVisibility(VISIBLE); // 显示M429子布局
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_429); // 通知标题变更
                    if (serialChannel != null) { // 通道非空
                        serialChannel.setBusType(IBus.ARINC429); // 设置总线类型为ARINC429
                    }
                    msgSerials.setSerialsType(item); // 设置消息体协议类型
                    msgSerials.setSerialsDetails(m429.getMsgDetailsM429()); // 设置消息体M429详情数据
                    break;
                case SERIALS_M1553B: // MIL-STD-1553B协议
                    m1553b.setVisibility(VISIBLE); // 显示M1553B子布局
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_1553B); // 通知标题变更
                    if (serialChannel != null) { // 通道非空
                        serialChannel.setBusType(IBus.MILSTD1553B); // 设置总线类型为MILSTD1553B
                    }
                    msgSerials.setSerialsType(item); // 设置消息体协议类型
                    msgSerials.setSerialsDetails(m1553b.getMsgDetailsM1553B()); // 设置消息体M1553B详情数据
                    break;
            }
//            if (isAddByUser(serialsNumber)) {
                sendMsg(false); // 广播配置变更消息
//            }
            CursorManage.setCursorByTimebaseTrace(); // 根据时基设置光标追踪
            CursorManage.getInstance().setCursorTrace(false); // 关闭光标追踪
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {
            // 点击后刷新UI回调（空实现）
        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {
            // 点击前刷新UI回调（空实现）
        }
    };



    /**
     * 协议详情消息发送监听器，子布局参数变更时更新消息体并广播
     */
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener = new OnSerialsDetailSendMsgListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View detailView, boolean isFromEventBus) {
            String[] ss = context.getResources().getStringArray(R.array.serialsType); // 获取协议类型字符串数组
            switch (detailView.getId()) { // 根据详情视图ID分发处理
                case R.id.uartRightSerials: // UART详情变更
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_UART, ss[SERIALS_UART], true)); // 设置消息体协议类型为UART
                    msgSerials.setSerialsDetails(uart.getMsgDetailsUart()); // 设置消息体UART详情数据
                    msgSerials.getSerialsType().setRxMsgSelect(false); // 标记非Rx消息选中
                    break;
                case R.id.linRightSerials: // LIN详情变更
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_LIN, ss[SERIALS_LIN], true)); // 设置消息体协议类型为LIN
                    msgSerials.setSerialsDetails(lin.getMsgDetailsLin()); // 设置消息体LIN详情数据
                    msgSerials.getSerialsType().setRxMsgSelect(false); // 标记非Rx消息选中
                    break;
                case R.id.canRightSerials: // CAN详情变更
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_CAN, ss[SERIALS_CAN], true)); // 设置消息体协议类型为CAN
                    msgSerials.setSerialsDetails(can.getMsgDetailsCan()); // 设置消息体CAN详情数据
                    msgSerials.getSerialsType().setRxMsgSelect(false); // 标记非Rx消息选中
                    break;
                case R.id.spiRightSerials: // SPI详情变更
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_SPI, ss[SERIALS_SPI], true)); // 设置消息体协议类型为SPI
                    msgSerials.setSerialsDetails(spi.getMsgDetailsSpi()); // 设置消息体SPI详情数据
                    msgSerials.getSerialsType().setRxMsgSelect(false); // 标记非Rx消息选中
                    break;
                case R.id.i2cRightSerials: // I2C详情变更
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_I2C, ss[SERIALS_I2C], true)); // 设置消息体协议类型为I2C
                    msgSerials.setSerialsDetails(i2c.getMsgDetailsI2c()); // 设置消息体I2C详情数据
                    msgSerials.getSerialsType().setRxMsgSelect(false); // 标记非Rx消息选中
                    break;
                case R.id.m429RightSerials: // M429详情变更
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_M429, ss[SERIALS_M429], true)); // 设置消息体协议类型为M429
                    msgSerials.setSerialsDetails(m429.getMsgDetailsM429()); // 设置消息体M429详情数据
                    msgSerials.getSerialsType().setRxMsgSelect(false); // 标记非Rx消息选中
                    break;
                case R.id.m1553bRightSerials: // M1553B详情变更
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_M1553B, ss[SERIALS_M1553B], true)); // 设置消息体协议类型为M1553B
                    msgSerials.setSerialsDetails(m1553b.getMsgDetailsM1553B()); // 设置消息体M1553B详情数据
                    msgSerials.getSerialsType().setRxMsgSelect(false); // 标记非Rx消息选中
                    break;

            }
            if (isAddByUser(msgSerials.getSerialsNumber())) { // 通道已被用户添加
                sendMsg(isFromEventBus); // 广播配置变更消息
            }
        }
    };

    /**
     * 设置串口配置消息体
     *
     * @param msgSerials 消息体对象
     */
    public void setRightMsgSerials(RightMsgSerials msgSerials) {
        this.msgSerials = msgSerials; // 设置串口配置消息体
    }


    /**
     * 通用点击监听器，处理删除/添加通道和Math/Ref/Serials切换
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) { // 根据点击的视图ID分发处理
                case R.id.btn_delete_channel: // 删除通道按钮
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + serialsNumber, String.valueOf(false)); // 标记通道未添加
                    serialsCheck.setState(false); // 关闭串口开关
                    onToggleStateChangedListener.onToggleStateChanged(serialsCheck, serialsCheck.isState()); // 触发开关变更回调
                    break;
                case R.id.channelMath: // Math通道按钮
                    setChannelState(); // 设置Serials选中状态
                    hideSlip(); // 隐藏滑出菜单
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 0); // 广播显示Math通道消息
                    break;
                case R.id.channelRef: // Ref通道按钮
                    setChannelState(); // 设置Serials选中状态
                    hideSlip(); // 隐藏滑出菜单
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 1); // 广播显示Ref通道消息
                    break;
                case R.id.channelSerials: // Serials通道按钮
                    setChannelState(); // 设置Serials选中状态
                    break;
                case R.id.btn_add_channel: // 添加通道按钮
                    needUpdateMasterLocation = true; // 标记需要更新MasterView位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + serialsNumber, String.valueOf(true)); // 标记通道已添加
                    serialsCheck.setState(true); // 打开串口开关
                    onToggleStateChangedListener.onToggleStateChanged(serialsCheck, serialsCheck.isState()); // 触发开关变更回调
                    hideSlip(); // 隐藏滑出菜单
                    break;

            }
        }
    };

    /**
     * 隐藏滑出菜单
     */
    private void hideSlip() {
        int slipIndex = MainViewGroup.RIGHTSLIP_S1; // 滑出菜单索引默认S1
        switch(TChan.toSerialTChan(serialsNumber)) { // 根据通道编号确定滑出菜单索引
            case TChan.S1: slipIndex = MainViewGroup.RIGHTSLIP_S1;break; // S1滑出菜单
            case TChan.S2: slipIndex = MainViewGroup.RIGHTSLIP_S2;break; // S2滑出菜单
            case TChan.S3: slipIndex = MainViewGroup.RIGHTSLIP_S3;break; // S3滑出菜单
            case TChan.S4: slipIndex = MainViewGroup.RIGHTSLIP_S4;break; // S4滑出菜单
        }
        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slipIndex, false)); // 广播隐藏滑出菜单消息
    }

    /**
     * 设置通道状态为Serials选中
     */
    private void setChannelState() {
        channelMath.setChecked(false); // 取消Math选中
        channelRef.setChecked(false); // 取消Ref选中
        channelSerials.setChecked(true); // 选中Serials
    }

    /**
     * 更新最大通道编号缓存
     */
    private void updateMaxChannelNumber() {
        AtomicInteger maxChan = new AtomicInteger(TChan.S1 - 1); // 最大通道编号初始值（S1-1）
        TChan.foreachSerial(serialChan -> { // 遍历所有串口通道
            int serialsIndex = TChan.toSerialNumber(serialChan); // 转换为串口编号
            boolean serialsCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + serialsIndex); // 读取开关状态
//            if (serialsCheck) {
            if (isAddByUser(serialsIndex)) { // 通道已被用户添加
                maxChan.set(Math.max(maxChan.get(), serialChan)); // 更新最大通道编号
            }
        });
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MAX_CHANNEL_NUMBER_SERIALS, maxChan.toString()); // 保存最大通道编号到缓存
    }


    /**
     * 串口文本页面可见性消费者，更新文本页面显示状态标记
     */
    private Consumer<Boolean> consumerSerialsWordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            isSerialsWordShow = aBoolean; // 更新文本页面显示标记
        }
    };

    /**
     * 删除通道消费者，收到删除指令时模拟点击删除按钮
     */
    private Consumer<Integer> consumerDeleteChannel = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Throwable {
            if (integer == TChan.toSerialTChan(serialsNumber)) { // 匹配当前串口通道
                btnDeleteChannel.performClick(); // 模拟点击删除通道按钮
            }
        }
    };

    //是否为已添加过的通道
    /**
     * 判断指定串口通道是否已被用户添加
     *
     * @param serialsNumber 串口编号(1~4)
     * @return true=已添加，false=未添加
     */
    private boolean isAddByUser(int serialsNumber) {
        return CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + serialsNumber); // 从缓存读取通道添加标记
    }
}
