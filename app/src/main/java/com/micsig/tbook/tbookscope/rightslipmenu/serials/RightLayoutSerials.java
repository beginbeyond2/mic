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


/**
 * Created by yangj on 2017/5/4.
 */

public class RightLayoutSerials extends RelativeLayout {
    private static final String TAG = RightLayoutSerials.class.getSimpleName();

    public static final int SERIALS_UART = 0;
    public static final int SERIALS_LIN = 1;
    public static final int SERIALS_CAN = 2;
    public static final int SERIALS_SPI = 3;
    public static final int SERIALS_I2C = 4;
    public static final int SERIALS_M429 = 5;
    public static final int SERIALS_M1553B = 6;
    private LinearLayout llDisplaySwitch;
    public static int getPropertyIndex(int SERIALS) {
        int idx = -1;
        switch (SERIALS) {
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

    private Context context;
    private MSwitchBox serialsCheck;
    private RightViewSelect serials;
    private ConstraintLayout detail;
    private RightLayoutSerialsUart uart;
    private RightLayoutSerialsLin lin;
    private RightLayoutSerialsCan can;
    private RightLayoutSerialsSpi spi;
    private RightLayoutSerialsI2c i2c;
    private RightLayoutSerialsM429 m429;
    private RightLayoutSerialsM1553B m1553b;

    private RightMsgSerials msgSerials;
    /**
     * value 1,2,3,4
     */
    private int serialsNumber;
    private ConstraintLayout topChannelTitle, clTopChannelGroup;
    private TextView channelTitle;
    private View space;
    private TextView btnDeleteChannel, btnAddChannel;
    private RadioButton channelMath, channelRef, channelSerials;
    private boolean needUpdateMasterLocation = false;
    private ViewGroup rootView;
    private boolean isSerialsWordShow = false;//文本页面是否显示

    public RightLayoutSerials(Context context) {
        this(context, null);
    }

    public RightLayoutSerials(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutSerials(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
        initControl();
    }

    @SuppressLint("SetTextI18n")
    private void initView(AttributeSet attrs, int defStyleAttr) {
        rootView = (ViewGroup) View.inflate(context, R.layout.layout_right_serials, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightLayoutSerials);
        serialsNumber = ta.getInt(R.styleable.RightLayoutSerials_serialsNumber, CacheUtil.S1);
        ta.recycle();
        space = findViewById(R.id.space);
        topChannelTitle = findViewById(R.id.top_channel_title);
        channelTitle = findViewById(R.id.channel_title);
        btnDeleteChannel = findViewById(R.id.btn_delete_channel);
        llDisplaySwitch = findViewById(R.id.ll_display_switch);
        serialsCheck = (MSwitchBox) findViewById(R.id.serialsCheckDetail);
        serials = (RightViewSelect) findViewById(R.id.serialsType);
        detail = findViewById(R.id.detail);
        uart = (RightLayoutSerialsUart) findViewById(R.id.uartRightSerials);
        lin = (RightLayoutSerialsLin) findViewById(R.id.linRightSerials);
        can = (RightLayoutSerialsCan) findViewById(R.id.canRightSerials);
        spi = (RightLayoutSerialsSpi) findViewById(R.id.spiRightSerials);
        i2c = (RightLayoutSerialsI2c) findViewById(R.id.i2cRightSerials);
        m429 = (RightLayoutSerialsM429) findViewById(R.id.m429RightSerials);
        m1553b = (RightLayoutSerialsM1553B) findViewById(R.id.m1553bRightSerials);

        SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)));
        if (serialChannel == null) return;
        uart.setSerialsNumber(serialsNumber);
        lin.setSerialsNumber(serialsNumber);
        can.setSerialsNumber(serialsNumber);
        spi.setSerialsNumber(serialsNumber);
        i2c.setSerialsNumber(serialsNumber);
        m429.setSerialsNumber(serialsNumber);
        m1553b.setSerialsNumber(serialsNumber);

        clTopChannelGroup = findViewById(R.id.cl_top_channel_group);
        channelMath = findViewById(R.id.channelMath);
        channelRef = findViewById(R.id.channelRef);
        channelSerials = findViewById(R.id.channelSerials);
        btnAddChannel = findViewById(R.id.btn_add_channel);
        channelMath.setOnClickListener(onClickListener);
        channelRef.setOnClickListener(onClickListener);
        channelSerials.setOnClickListener(onClickListener);
        btnAddChannel.setOnClickListener(onClickListener);

        btnDeleteChannel.setOnClickListener(onClickListener);
        serials.setOnItemClickListener(onSerialsItemClickListener);
        serialsCheck.setOnToggleStateChangedListener(onToggleStateChangedListener);
        uart.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        lin.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        can.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        spi.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        i2c.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        m429.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        m1553b.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);

        initData();

        setControlColorByChIdx(TChan.toSerialTChan(serialsNumber));
        channelTitle.setText("S" + serialsNumber);
    }
    private void setControlColorByChIdx(int chIdx){
        serialsCheck.setControlColorByChIdx(chIdx);
        serials.setControlColorByChIdx(chIdx);
        channelTitle.setTextColor(TChan.getChannelColor(context, chIdx));
        channelSerials.setTextColor(TChan.getChannelColor(context, chIdx));
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS_CLOSE).subscribe(consumerRightSerials);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_NORMAL_STATE).subscribe(consumerShowNormalState);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_SERIALS).subscribe(consumerOtherChannelCanAdd);

        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialsWordVisible);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL).subscribe(consumerDeleteChannel);


    }

    private void initData() {
        String[] ss = context.getResources().getStringArray(R.array.serialsType);
        msgSerials = new RightMsgSerials();
        msgSerials.setSerialsNumber(serialsNumber);
        msgSerials.setSerialsCheck(serialsNumber, serialsCheck.isState());
        msgSerials.setSerialsType(new RightBeanSelect(SERIALS_UART, ss[SERIALS_UART], true));
    }

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
        TChan.foreachChan((uiCh)->{
            int chVScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_SCALE_ID +uiCh);
            Channel chan= ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(uiCh));
            if (chan!=null) {
                chan.setVScaleId(chVScaleId);
            }
        });

        for (int i = 0; i < detail.getChildCount(); i++) {
            detail.getChildAt(i).setVisibility(GONE);
        }
        String[] ss = context.getResources().getStringArray(R.array.serialsType);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)));
        if (serialChannel == null) return;
        boolean checkKey = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + serialsNumber);
        serialsCheck.setState(checkKey);
        msgSerials.setSerialsCheck(serialsNumber, checkKey);
        int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber);
        SerialBusManage serialBusManage = SerialBusManage.getInstance();
        SerialBus serialBus = serialBusManage.getSerialBus(TChan.toSerialTChan(serialsNumber));
        boolean isAddByUser = isAddByUser(serialsNumber);
        switch (index) {
            case SERIALS_UART:
                uart.setVisibility(VISIBLE);
                serialChannel.setBusType(IBus.UART);
                if (isAddByUser) {
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) {
                        serialBus.setVisible(true);
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_UART);
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_UART, ss[SERIALS_UART], true));
                msgSerials.setSerialsDetails(uart.getMsgDetailsUart());
                break;
            case SERIALS_LIN:
                lin.setVisibility(VISIBLE);
                serialChannel.setBusType(IBus.LIN);
                if (isAddByUser) {
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) {
                        serialBus.setVisible(true);
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_LIN);
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_LIN, ss[SERIALS_LIN], true));
                msgSerials.setSerialsDetails(lin.getMsgDetailsLin());
                break;
            case SERIALS_CAN:
                can.setVisibility(VISIBLE);
                serialChannel.setBusType(IBus.CAN);
                if (isAddByUser) {
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) {
                        serialBus.setVisible(true);
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_CAN);
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_CAN, ss[SERIALS_CAN], true));
                msgSerials.setSerialsDetails(can.getMsgDetailsCan());
                break;
            case SERIALS_SPI:
                spi.setVisibility(VISIBLE);
                serialChannel.setBusType(IBus.SPI);
                if (isAddByUser) {
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) {
                        serialBus.setVisible(true);
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_SPI);
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_SPI, ss[SERIALS_SPI], true));
                msgSerials.setSerialsDetails(spi.getMsgDetailsSpi());
                break;
            case SERIALS_I2C:
                i2c.setVisibility(VISIBLE);
                serialChannel.setBusType(IBus.I2C);
                if (isAddByUser) {
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) {
                        serialBus.setVisible(true);
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_I2C);
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_I2C, ss[SERIALS_I2C], true));
                msgSerials.setSerialsDetails(i2c.getMsgDetailsI2c());
                break;
            case SERIALS_M429:
                m429.setVisibility(VISIBLE);
                serialChannel.setBusType(IBus.ARINC429);
                if (isAddByUser) {
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) {
                        serialBus.setVisible(true);
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_429);
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_M429, ss[SERIALS_M429], true));
                msgSerials.setSerialsDetails(m429.getMsgDetailsM429());
                break;
            case SERIALS_M1553B:
                m1553b.setVisibility(VISIBLE);
                serialChannel.setBusType(IBus.MILSTD1553B);
                if (isAddByUser) {
                    if (!serialBus.getVisible() && serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) {
                        serialBus.setVisible(true);
                    }
                }
                serialBusManage.OnTitleChange(TChan.toSerialTChan(serialsNumber), SerialBusStruct.SerialBusType_1553B);
                msgSerials.setSerialsType(new RightBeanSelect(SERIALS_M1553B, ss[SERIALS_M1553B], true));
                msgSerials.setSerialsDetails(m1553b.getMsgDetailsM1553B());
                break;
        }
        serials.setSelectIndex(index);
        Command.get().getChannel().Display(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)), checkKey && isAddByUser, false);
        Command.get().getBus().Type(serialsNumber - 1, index, false);
    }

    private void sendMsg(boolean isFromEventBus) {
        msgSerials.setFromEventBus(isFromEventBus);
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_SERIALS, msgSerials);
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_SERIALS_FOLLOW, msgSerials);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerials, true);
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
            if(msgSerials == null) return;
            serialsCheck.setState(mainRightMsgOthers.getSerial(serialsNumber).isValue());
            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + serialsNumber, String.valueOf(serialsCheck.isState()));
            msgSerials.setSerialsCheck(serialsNumber, serialsCheck.isState());

            if (mainRightMsgOthers.getSerial(serialsNumber).isValue() && isAddByUser(serialsNumber)) {
                SerialBusManage.getInstance().getSerialBus(TChan.toSerialTChan(serialsNumber)).forceDrawLastData(TChan.toSerialTChan(serialsNumber));
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
            if (serialsNumber == serialNo && serialsCheck.isState()) {
                boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
                boolean slowTimeBase = Tools.isSlowTimeBase();
                if (zoom && slowTimeBase) {
                    return;
                }
                serialsCheck.setState(!serialsCheck.isState());
                onToggleStateChangedListener.onToggleStateChanged(serialsCheck, serialsCheck.isState());
            }
        }
    };
    private Consumer<CommandMsgToUI> consumerCommandToUI=new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()){
                case CommandMsgToUI.FLAG_CHANNEL_DISPLAY:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    boolean isOpen = Boolean.parseBoolean(params[1]);
                    if (TChan.toSerialTChan(serialsNumber) == TChan.toUiChNo(chIndex)) {
                        if (!serialsCheck.isEnabled()){
                            return;
                        }
                        serialsCheck.setState(isOpen);
                        onToggleStateChangedListener.onToggleStateChanged(serialsCheck,isOpen);
                    }
                }break;
                case CommandMsgToUI.FLAG_Bus_Type: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]) + 1;
                    int type = Integer.parseInt(params[1]);
                    if (serialsNumber == chIndex) {
                        serials.setSelectIndex(type);
                        onSerialsItemClickListener.onItemClick(serials.getId(), new RightBeanSelect(type, ToolsSCPI.getBusType(type), serialsCheck.isState()));
                    }

                }break;
            }
        }
    };

    private Consumer<String> consumerShowNormalState = new Consumer<String>() {
        @Override
        public void accept(String String) throws Exception {
            String[] params = String.split(CommandMsgToUI.PARAM_SPLIT);
            int channelNumber = Integer.parseInt(params[0]);
            boolean isNormal = Boolean.parseBoolean(params[1]);
            if (channelNumber != TChan.toSerialTChan(serialsNumber)) return;
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

    private Consumer<String> consumerOtherChannelCanAdd = new Consumer<String>() {
        @Override
        public void accept(String available) throws Throwable {
            if (channelMath == null || channelRef == null || channelSerials == null) return;
            String[] params = available.split(CommandMsgToUI.PARAM_SPLIT);
            int serialsSlideIndex = Integer.parseInt(params[0]);
            boolean mathAvailable = Boolean.parseBoolean(params[1]);
            boolean refAvailable = Boolean.parseBoolean(params[2]);
            boolean serialAvailable = Boolean.parseBoolean(params[3]);
            int curSerialsNumber = CacheUtil.S1;
            switch (serialsSlideIndex) {
                case MainViewGroup.RIGHTSLIP_S1:
                    curSerialsNumber = CacheUtil.S1;
                    break;
                case MainViewGroup.RIGHTSLIP_S2:
                    curSerialsNumber = CacheUtil.S2;
                    break;
                case MainViewGroup.RIGHTSLIP_S3:
                    curSerialsNumber = CacheUtil.S3;
                    break;
                case MainViewGroup.RIGHTSLIP_S4:
                    curSerialsNumber = CacheUtil.S4;
                    break;
            }
            if (curSerialsNumber == serialsNumber) {
                channelMath.setEnabled(mathAvailable && !isSerialsWordShow);
                channelRef.setEnabled(refAvailable && !isSerialsWordShow);
            } else {
                channelMath.setEnabled(!isSerialsWordShow);
                channelRef.setEnabled(!isSerialsWordShow);
            }
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
            boolean slowTimeBase = Tools.isSlowTimeBase();
            if (zoom && slowTimeBase) {
                channelSerials.setEnabled(false);
            }
        }
    };


    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
            boolean slowTimeBase = Tools.isSlowTimeBase();
            if (zoom && slowTimeBase) {
                serialsCheck.setState(false);
                Command.get().getChannel().Display(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)), false,false);
                return;
            }
            if (!serialsCheck.isState()) {
                ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
            }
            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + serialsNumber, String.valueOf(serialsCheck.isState()));
            msgSerials.setSerialsCheck(serialsNumber, serialsCheck.isState());
            sendMsg(false);
            Command.get().getChannel().Display(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)), state && isAddByUser(serialsNumber), false);
//            这里暂时不需要移动MasterView位置
//            updateMaxChannelNumber();
//            if (needUpdateMasterLocation) {
//                needUpdateMasterLocation = false;
//                RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_MASTER_LOCATION, TChan.toSerialTChan(serialsNumber));
//            }
            //若没显示，根据状态显示串口通道
            SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(TChan.toSerialTChan(serialsNumber));
            if (serialBus.getLineNameID() == TChan.toSerialTChan(serialsNumber)) {
                serialBus.setVisible(state && isAddByUser(serialsNumber));
            }

        }
    };

    private RightViewSelect.OnItemClickListener onSerialsItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            Tools.PrintControlsLocation("RightLayoutSerials", rootView);
            CursorManage.getInstance().setCursorTrace(true);
            for (int i = 0; i < detail.getChildCount(); i++) {
                detail.getChildAt(i).setVisibility(GONE);
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber, String.valueOf(item.getIndex()));
            int ch = TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber));
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(ch);
            int serialsChNo = TChan.toSerialTChan(serialsNumber);
            Command.get().getBus().Type(serialsNumber - 1, item.getIndex(), false);
            switch (item.getIndex()) {
                case SERIALS_UART:
                    uart.setVisibility(VISIBLE);
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_UART);
                    if (serialChannel != null) {
                        serialChannel.setBusType(IBus.UART);
                    }
                    msgSerials.setSerialsType(item);
                    msgSerials.setSerialsDetails(uart.getMsgDetailsUart());
                    break;
                case SERIALS_LIN:
                    lin.setVisibility(VISIBLE);
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_LIN);
                    if (serialChannel != null) {
                        serialChannel.setBusType(IBus.LIN);
                    }
                    msgSerials.setSerialsType(item);
                    msgSerials.setSerialsDetails(lin.getMsgDetailsLin());
                    break;
                case SERIALS_CAN:
                    can.setVisibility(VISIBLE);
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_CAN);
                    if (serialChannel != null) {
                        serialChannel.setBusType(IBus.CAN);
                    }
                    msgSerials.setSerialsType(item);
                    msgSerials.setSerialsDetails(can.getMsgDetailsCan());
                    break;
                case SERIALS_SPI:
                    spi.setVisibility(VISIBLE);
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_SPI);
                    if (serialChannel != null) {
                        serialChannel.setBusType(IBus.SPI);
                    }
                    msgSerials.setSerialsType(item);
                    msgSerials.setSerialsDetails(spi.getMsgDetailsSpi());
                    break;
                case SERIALS_I2C:
                    i2c.setVisibility(VISIBLE);
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_I2C);
                    if (serialChannel != null) {
                        serialChannel.setBusType(IBus.I2C);
                    }
                    msgSerials.setSerialsType(item);
                    msgSerials.setSerialsDetails(i2c.getMsgDetailsI2c());
                    break;
                case SERIALS_M429:
                    m429.setVisibility(VISIBLE);
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_429);
                    if (serialChannel != null) {
                        serialChannel.setBusType(IBus.ARINC429);
                    }
                    msgSerials.setSerialsType(item);
                    msgSerials.setSerialsDetails(m429.getMsgDetailsM429());
                    break;
                case SERIALS_M1553B:
                    m1553b.setVisibility(VISIBLE);
                    SerialBusManage.getInstance().OnTitleChange(serialsChNo, SerialBusStruct.SerialBusType_1553B);
                    if (serialChannel != null) {
                        serialChannel.setBusType(IBus.MILSTD1553B);
                    }
                    msgSerials.setSerialsType(item);
                    msgSerials.setSerialsDetails(m1553b.getMsgDetailsM1553B());
                    break;
            }
//            if (isAddByUser(serialsNumber)) {
                sendMsg(false);
//            }
            CursorManage.setCursorByTimebaseTrace();
            CursorManage.getInstance().setCursorTrace(false);
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };



    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener = new OnSerialsDetailSendMsgListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View detailView, boolean isFromEventBus) {
            String[] ss = context.getResources().getStringArray(R.array.serialsType);
            switch (detailView.getId()) {
                case R.id.uartRightSerials:
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_UART, ss[SERIALS_UART], true));
                    msgSerials.setSerialsDetails(uart.getMsgDetailsUart());
                    msgSerials.getSerialsType().setRxMsgSelect(false);
                    break;
                case R.id.linRightSerials:
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_LIN, ss[SERIALS_LIN], true));
                    msgSerials.setSerialsDetails(lin.getMsgDetailsLin());
                    msgSerials.getSerialsType().setRxMsgSelect(false);
                    break;
                case R.id.canRightSerials:
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_CAN, ss[SERIALS_CAN], true));
                    msgSerials.setSerialsDetails(can.getMsgDetailsCan());
                    msgSerials.getSerialsType().setRxMsgSelect(false);
                    break;
                case R.id.spiRightSerials:
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_SPI, ss[SERIALS_SPI], true));
                    msgSerials.setSerialsDetails(spi.getMsgDetailsSpi());
                    msgSerials.getSerialsType().setRxMsgSelect(false);
                    break;
                case R.id.i2cRightSerials:
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_I2C, ss[SERIALS_I2C], true));
                    msgSerials.setSerialsDetails(i2c.getMsgDetailsI2c());
                    msgSerials.getSerialsType().setRxMsgSelect(false);
                    break;
                case R.id.m429RightSerials:
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_M429, ss[SERIALS_M429], true));
                    msgSerials.setSerialsDetails(m429.getMsgDetailsM429());
                    msgSerials.getSerialsType().setRxMsgSelect(false);
                    break;
                case R.id.m1553bRightSerials:
                    msgSerials.setSerialsType(new RightBeanSelect(SERIALS_M1553B, ss[SERIALS_M1553B], true));
                    msgSerials.setSerialsDetails(m1553b.getMsgDetailsM1553B());
                    msgSerials.getSerialsType().setRxMsgSelect(false);
                    break;

            }
            if (isAddByUser(msgSerials.getSerialsNumber())) {
                sendMsg(isFromEventBus);
            }
        }
    };

    public void setRightMsgSerials(RightMsgSerials msgSerials) {
        this.msgSerials = msgSerials;
    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_delete_channel:
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + serialsNumber, String.valueOf(false));
                    serialsCheck.setState(false);
                    onToggleStateChangedListener.onToggleStateChanged(serialsCheck, serialsCheck.isState());
                    break;
                case R.id.channelMath:
                    setChannelState();
                    hideSlip();
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 0);
                    break;
                case R.id.channelRef:
                    setChannelState();
                    hideSlip();
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 1);
                    break;
                case R.id.channelSerials:
                    setChannelState();
                    break;
                case R.id.btn_add_channel:
                    needUpdateMasterLocation = true;
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + serialsNumber, String.valueOf(true));
                    serialsCheck.setState(true);
                    onToggleStateChangedListener.onToggleStateChanged(serialsCheck, serialsCheck.isState());
                    hideSlip();
                    break;

            }
        }
    };

    private void hideSlip() {
        int slipIndex = MainViewGroup.RIGHTSLIP_S1;
        switch(TChan.toSerialTChan(serialsNumber)) {
            case TChan.S1: slipIndex = MainViewGroup.RIGHTSLIP_S1;break;
            case TChan.S2: slipIndex = MainViewGroup.RIGHTSLIP_S2;break;
            case TChan.S3: slipIndex = MainViewGroup.RIGHTSLIP_S3;break;
            case TChan.S4: slipIndex = MainViewGroup.RIGHTSLIP_S4;break;
        }
        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slipIndex, false));
    }

    private void setChannelState() {
        channelMath.setChecked(false);
        channelRef.setChecked(false);
        channelSerials.setChecked(true);
    }

    private void updateMaxChannelNumber() {
        AtomicInteger maxChan = new AtomicInteger(TChan.S1 - 1);
        TChan.foreachSerial(serialChan -> {
            int serialsIndex = TChan.toSerialNumber(serialChan);
            boolean serialsCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + serialsIndex);
//            if (serialsCheck) {
            if (isAddByUser(serialsIndex)) {
                maxChan.set(Math.max(maxChan.get(), serialChan));
            }
        });
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MAX_CHANNEL_NUMBER_SERIALS, maxChan.toString());
    }


    private Consumer<Boolean> consumerSerialsWordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            isSerialsWordShow = aBoolean;
        }
    };

    private Consumer<Integer> consumerDeleteChannel = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Throwable {
            if (integer == TChan.toSerialTChan(serialsNumber)) {
                btnDeleteChannel.performClick();
            }
        }
    };

    //是否为已添加过的通道
    private boolean isAddByUser(int serialsNumber) {
        return CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + serialsNumber);
    }
}
