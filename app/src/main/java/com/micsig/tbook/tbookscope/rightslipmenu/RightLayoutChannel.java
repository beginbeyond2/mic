package com.micsig.tbook.tbookscope.rightslipmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Calibrate.ProbeCalibrate;
import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.scope.probe.ProbeNotifyInfo;
import com.micsig.tbook.scope.probe.ProbeUpgradeInfo;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels;
import com.micsig.tbook.tbookscope.middleware.MiddleMain;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChOpenClose;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogBandWidthHz;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogChannelLabel;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogProbeInterface;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogProbeMultiple;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.ui.wavezone.TChan;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/9.
 */

public class RightLayoutChannel extends RelativeLayout {
    private static final String TAG = "RightLayoutChannel";

    private Context context;
    private MSwitchBox btnchCheck, btnInvert, btnFineSwitch;
    private RightViewSelect rgCouple, rgProbeType, rgBandWidth;
    private RightViewSelect rgVerBase, rgImped;
    private TextView channelVisibleHead, probeMultiple, bandWidthEdit, probeTypeModel, probeTypeSN, probeTypeX, probeTypeCall;
    private TopViewEdit chLabel, chDelay, chOffset, chPosition,chFineExtent;
    private DialogProbeMultiple dialogProbeMultiple;
    private DialogBandWidthHz dialogBandWidthHz;
    private DialogChannelLabel dialogChannelLabel;
    private DialogProbeInterface dialogProbeInterface;
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;
    private DialogOk dialogOk;
    private Button btnTop, btnBottom;
    private ImageView ivBackground, ivInvertLight;
    private MyHandler myHandler;

    /**
     * value 1-8
     * TChan.Ch1
     * TChan.Ch2
     * TChan.Ch3
     * TChan.Ch4
     * TChan.Ch5
     * TChan.Ch6
     * TChan.Ch7
     * TChan.Ch8
     */
    private int channelNumber;
    private RightMsgChannel msgChannel;

    private String preProbeMul;
    private ViewGroup rootViewGroup;

    private ConstraintLayout mdp700Layout,commonLayout,msp500Layout,mrcpLayout;
    private RightViewSelect mdp700Select;
    private Button mdp700Info;
    private TextView mdp700Model,mrcpProbeTypeX,mrcpProbeTypeCall;
    private TextView mrcpMode;

    public RightLayoutChannel(Context context) {
        this(context, null);
    }

    public RightLayoutChannel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutChannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
        initControl();
        myHandler = new MyHandler(RightLayoutChannel.this);
    }
    public String positionString;
    private void initView(AttributeSet attrs, int defStyleAttr) {
        rootViewGroup = (ViewGroup) View.inflate(context, R.layout.layout_right_channel, this);

        setClickable(true);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightLayoutChannel);
        channelNumber = ta.getInt(R.styleable.RightLayoutChannel_channelNumber, TChan.Ch1);
        ta.recycle();
        RelativeLayout chanelLayout = (RelativeLayout) findViewById(R.id.chanelLayout);
        channelVisibleHead = (TextView) findViewById(R.id.channelVisibleHead);
        btnchCheck = (MSwitchBox) findViewById(R.id.channelVisibleDetail);
        btnInvert = (MSwitchBox) findViewById(R.id.invertDetail);
        btnFineSwitch=(MSwitchBox)findViewById(R.id.chFineSwitch);
        rgCouple = (RightViewSelect) findViewById(R.id.ch1Couple);
        rgProbeType = (RightViewSelect) findViewById(R.id.ch1ProbeType);
        rgBandWidth = (RightViewSelect) findViewById(R.id.bandWidth);
        rgVerBase = (RightViewSelect) findViewById(R.id.chVerticalBaseDetail);
        rgImped = (RightViewSelect) findViewById(R.id.chImpedDetail);
        chLabel = (TopViewEdit) findViewById(R.id.chLabel);
        chDelay = (TopViewEdit) findViewById(R.id.chDelay);
        chPosition = (TopViewEdit) findViewById(R.id.chPosition);
        chOffset = (TopViewEdit) findViewById(R.id.chOffset);
        chFineExtent=(TopViewEdit)findViewById(R.id.chFineExtent);
        btnTop = (Button) findViewById(R.id.btnTop);
        btnBottom = (Button) findViewById(R.id.btnBottom);
        ivBackground = findViewById(R.id.img_back_src);
        ivInvertLight = findViewById(R.id.img_invert_light);

        probeMultiple = (TextView) findViewById(R.id.probeMultiple);
        bandWidthEdit = (TextView) findViewById(R.id.bandWidthEdit);
        dialogProbeMultiple = (DialogProbeMultiple) ((MainActivity) context).findViewById(R.id.dialogProbeMultiple);
        dialogBandWidthHz = (DialogBandWidthHz) ((MainActivity) context).findViewById(R.id.dialogBandWidthHz);

        dialogOk = (DialogOk) ((MainActivity) context).findViewById(R.id.dialogOk);
        dialogProbeInterface = (DialogProbeInterface) ((MainActivity) context).findViewById(R.id.dialogProbeInterface);


        probeTypeModel = findViewById(R.id.probeTypeModel);
        probeTypeSN = findViewById(R.id.probeTypeSN);
        probeTypeX = findViewById(R.id.probeTypeX);
        probeTypeCall = findViewById(R.id.probeTypeCall);
        mrcpMode=findViewById(R.id.mrcpMode);

        btnchCheck.setOnToggleStateChangedListener(onToggleStateChangedListener);
        btnInvert.setOnToggleStateChangedListener(onToggleStateChangedListener);
        btnFineSwitch.setOnToggleStateChangedListener(onToggleStateChangedListener);
        rgCouple.setOnItemClickListener(onItemClickListener);
        rgProbeType.setOnItemClickListener(onItemClickListener);
        probeMultiple.setOnClickListener(onClickListener);
        rgBandWidth.setOnItemClickListener(onItemClickListener);
        bandWidthEdit.setOnClickListener(onClickListener);
        rgVerBase.setOnItemClickListener(onItemClickListener);
        rgImped.setOnItemClickListener(onItemClickListener);
        chLabel.setOnClickEditListener(onClickEditListener);
        chDelay.setOnClickEditListener(onClickEditListener);
        chPosition.setOnClickEditListener(onClickEditListener);
        chOffset.setOnClickEditListener(onClickEditListener);
        chFineExtent.setOnClickEditListener(onClickEditListener);
        btnTop.setOnClickListener(onClickListener);
        btnBottom.setOnClickListener(onClickListener);

        probeTypeCall.setOnClickListener(onClickListener);

        bandWidthEdit.setEnabled(false);

        chanelLayout.setBackgroundColor(getResources().getColor(R.color.frame_color));

        commonLayout=findViewById(R.id.commonLayout);
        msp500Layout=findViewById(R.id.msp500Layout);
        mrcpLayout=findViewById(R.id.mrcpLayout);

        mdp700Layout =findViewById(R.id.mdp700layout);
        mdp700Select =findViewById(R.id.mdp700ProbeSelect);
        mdp700Model=findViewById(R.id.mdp700Model);
        mdp700Info =findViewById(R.id.mdp700Info);
        mdp700Select.setOnItemClickListener(onItemClickListener);
        mdp700Info.setOnClickListener(onClickListener);

        mrcpProbeTypeX=findViewById(R.id.mrcpProbeTypeX);
        mrcpProbeTypeCall=findViewById(R.id.mrcpProbeTypeCall);
        mrcpProbeTypeCall.setOnClickListener(onClickListener);

        initData();
        msp500Layout.setVisibility(GONE);
        mdp700Layout.setVisibility(GONE);
        mrcpLayout.setVisibility(GONE);

        channelVisibleHead.setText("Ch" + channelNumber);
        setControlColorByChIdx(channelNumber);
    }
    private void setControlColorByChIdx(int chIdx){
        btnchCheck.setControlColorByChIdx(chIdx);
        btnInvert.setControlColorByChIdx(chIdx);
        btnFineSwitch.setControlColorByChIdx(chIdx);
        rgCouple.setControlColorByChIdx(chIdx);
        rgProbeType.setControlColorByChIdx(chIdx);
        rgBandWidth.setControlColorByChIdx(chIdx);
        rgVerBase.setControlColorByChIdx(chIdx);
        rgImped.setControlColorByChIdx(chIdx);
        mdp700Select.setControlColorByChIdx(chIdx);
    }

    private void initControl() {

        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCacheEx);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_CHANNEL_BANDWIDTH).subscribe(consumerChannelBandWidth);
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_VERNIER).subscribe(consumerExternalkeysVernier);
        RxBus.getInstance().getObservable(RxEnum.DIALOG_CLOSE).subscribe(onDialogClose);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_VERTICAL_SCALE).subscribe(consumerChannelVscale);
        RxBus.getInstance().getObservable(RxEnum.MSG_CHANNEL_SLIP_POSITION).subscribe(consumerChannelPosition);

        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_RESISTANCETYPE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_BANDWIDTH, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_INVERT, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_COUPLE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_VERTICAL_MODE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_START, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_STOP, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_MODE, eventObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_EVENT, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE_USER,OnChannelVscalUserChange);
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_ZERO, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_UPGRADE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_CALIBRATE_END, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_ALARM, eventUIObserver);
//        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OPEN,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OFFSET,eventUIObserver);

        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_PLUG,this::OnProbePlug);
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_UNPLUG,this::OnProbeUnPlug);

        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION).subscribe(consumerMouseClick);
    }




    private void initData() {
        msgChannel = new RightMsgChannel();
        msgChannel.setChannelNumber(channelNumber);
        msgChannel.setChCheck(true);
        boolean invert = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_INVERT + channelNumber);
        msgChannel.setInvert(invert);
        msgChannel.setCouple(rgCouple.getSelectItem());
//        msgChannel.setImped(rgCouple.getSelectItem());//阻抗设置放到耦合方式里
        msgChannel.setProbeType(rgProbeType.getSelectItem());
        msgChannel.setProbeMultiple(probeMultiple.getText().toString());
        msgChannel.setBandWidth(rgBandWidth.getSelectItem());
        msgChannel.setBandWidthEdit(bandWidthEdit.getText().toString());
        msgChannel.setImped(rgImped.getSelectItem());
        msgChannel.setLabel("");
        msgChannel.setDelay("");
        msgChannel.setFineExtent("");
        msgChannel.setFineSwitch(false);
        setInvertLight(invert);
    }

    private void setInvertLight(boolean invert) {
        ivInvertLight.setVisibility(invert ? View.VISIBLE : View.INVISIBLE);
        switch (channelNumber) {
            case TChan.Ch1:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch1_light);
                break;
            case TChan.Ch2:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch2_light);
                break;
            case TChan.Ch3:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch3_light);
                break;
            case TChan.Ch4:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch4_light);
                break;
            case TChan.Ch5:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch5_light);
                break;
            case TChan.Ch6:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch6_light);
                break;
            case TChan.Ch7:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch7_light);
                break;
            case TChan.Ch8:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch8_light);
                break;
            default:
                ivInvertLight.setBackground(null);
                break;
        }
    }

    private void setCacheEx() {
        int verBase = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_VERTICALBASE + channelNumber);
        int imped = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);
        this.rgVerBase.setSelectIndex(verBase);
        this.rgImped.setSelectIndex(imped);
        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
        if (channel == null) return;
        channel.setVerticalMode(verBase);

        String fineExtent=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+channelNumber);
        channel.setVScaleVal(TBookUtil.getDoubleFromM(fineExtent));

        updateProbeUI(channel);

    }

    boolean isLoadCache=false;
    private void setCache() {
        isLoadCache=true;
//        setProbeTypeLayout();

        boolean enableHighLowFilter = isEnableHighLowFilter();

        boolean invert = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_INVERT + channelNumber);
        int couple = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber);
        int probeType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber);
        String probeMultiple = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber);
        int bandWidth = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber);
        String bandWidthHighEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber);
        String bandWidthLowEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber);
        int verBase = 1;
        String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + channelNumber);
        String delay = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_DELAY + channelNumber);
        String offset = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_OFFSET + channelNumber);
//        String position = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_POSITION +channelNumber);
        int impedance = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);
        String fineExtent=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+channelNumber);
        fineExtent+=probeType==0?"V":"A";
        boolean fineSwitch= CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE + channelNumber);

//        offset=offset.replace("A","").replace("V","").replace(" ","");
        preProbeMul = probeMultiple;

        this.btnInvert.setState(invert);
        this.rgCouple.setSelectIndex(couple);
        this.rgProbeType.setSelectIndex(probeType);
        this.probeMultiple.setText(probeMultiple);
        this.rgVerBase.setSelectIndex(verBase);
        this.rgImped.setSelectIndex(impedance);
        chLabel.setText(label);
        chDelay.setText(delay);
//        chOffset.setText(offset);
//        chOffset.setText(position);
        btnFineSwitch.setState(fineSwitch);
        chFineExtent.setText(fineExtent);
//        chFineExtent.setEnabled(fineSwitch);
        if (!enableHighLowFilter) {
            if (bandWidth == 3 || bandWidth == 4) {
                this.rgBandWidth.setSelectIndex(0);
            }
            this.rgBandWidth.setEnabled(3, false);
            this.rgBandWidth.setEnabled(4, false);
        } else {
            this.rgBandWidth.setSelectIndex(bandWidth);
        }
        this.bandWidthEdit.setEnabled(bandWidth == 3 || bandWidth == 4);
        this.bandWidthEdit.setText(this.bandWidthEdit.isEnabled() ? (bandWidth == 3 ? bandWidthHighEdit : bandWidthLowEdit) : "");

        boolean channelVisible = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + channelNumber);
        btnchCheck.setState(channelVisible);


        Command.get().getChannel().Display(TChan.toFpgaChNo(channelNumber), channelVisible, false);
        Command.get().getChannel().Inverse(TChan.toFpgaChNo(channelNumber), invert, false);
        Command.get().getChannel().Couple(TChan.toFpgaChNo(channelNumber), couple, false);
        Command.get().getChannel().Prty(TChan.toFpgaChNo(channelNumber), probeType, false);
        Command.get().getChannel().Probe(TChan.toFpgaChNo(channelNumber), (float) TBookUtil.getDoubleFromX(probeMultiple), false);
        if (bandWidth == 1) {
            Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), bandWidth, (float) (TBookUtil.getMHzFromHz(bandWidthHighEdit) * 1000 * 1000), false);
        } else if (bandWidth == 2) {
            Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), bandWidth, (float) (TBookUtil.getMHzFromHz(bandWidthLowEdit) * 1000 * 1000), false);
        } else {
            Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), bandWidth, (float) (TBookUtil.getMHzFromHz(bandWidthHighEdit) * 1000 * 1000), false);
        }
        Command.get().getChannel().Vref(TChan.toFpgaChNo(channelNumber), verBase, false);
        Command.get().getChannel().Inputres(TChan.toFpgaChNo(channelNumber), impedance, false);
        Command.get().getChannel().Vernier(TChan.toFpgaChNo(channelNumber),fineSwitch,false);
        double _offset= TBookUtil.getDoubleFromM(offset.replace("A", "").replace("V", "").replace(" ", ""));
        Command.get().getChannel().Offset(TChan.toFpgaChNo(channelNumber),_offset,false);

        double _position= TBookUtil.getDoubleFromM(positionString.replace("A", "").replace("V", "").replace(" ", ""));
        Command.get().getChannel().Position(TChan.toFpgaChNo(channelNumber),_position,false);

        double _delay=TBookUtil.getDoubleFromM(delay.replace("s", ""))  + 0.1;
        Command.get().getChannel().Delay(TChan.toFpgaChNo(channelNumber),_delay,false);
        Command.get().getChannel().Label(TChan.toFpgaChNo(channelNumber), label, false);

        WaveManage.get().setChannelLabel(channelNumber, label);
        setChannelLabel(TChan.toFpgaChNo(channelNumber), label);


        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
        if (channel == null) return;
        channel.setVerticalMode(verBase);

        channel.setInvert(invert);
        channel.setCoupleType(matchCouple(couple));
        channel.setProbeType(probeType);
        channel.setProbeRate(TBookUtil.getDoubleFromX(probeMultiple));
        channel.setResistanceType(impedance);
        channel.setVerticalMode(verBase);
        fineExtent=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+channelNumber);
        channel.setVScaleVal(TBookUtil.getDoubleFromM(fineExtent));
        switch (bandWidth) {
            case 0:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, Channel.getMaxBandWidth());
                break;
            case 1:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_200M, (TBookUtil.getMHzFromHz("200" + TBookUtil.UNIT_MHZ) * 1000 * 1000));
                break;
            case 2:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_20M, (TBookUtil.getMHzFromHz("20" + TBookUtil.UNIT_MHZ) * 1000 * 1000));
                break;
            case 3:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_HIGHPASS, (TBookUtil.getMHzFromHz(bandWidthHighEdit) * 1000 * 1000));
                break;
            case 4:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, (TBookUtil.getMHzFromHz(bandWidthLowEdit) * 1000 * 1000));
                break;
            default:
                break;
        }

//        channel.setPos(TBookUtil.getDoubleFromM(positionString.replace("A","").replace("V","").replace(" ","")));
        channel.setChOffsetVal(TBookUtil.getDoubleFromM(offset.replace("A","").replace("V","").replace(" ","")));
        channel.setDelay((int)(TBookUtil.getDoubleFromM(delay.replace("s", "")) * 1e12 + 0.1));
        boolean xy = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1;
        btnchCheck.setEnabled(!xy);
        updateProbeUI(channel);

        msgChannel.setChCheck(channelVisible);
        msgChannel.setInvert(invert);
        msgChannel.setCouple(this.rgCouple.getSelectItem());
//        msgChannel.setImped(this.rgCouple.getSelectItem());
        msgChannel.setProbeType(this.rgProbeType.getSelectItem());
        msgChannel.setProbeMultiple(probeMultiple);
        msgChannel.setBandWidth(this.rgBandWidth.getSelectItem());
        msgChannel.setBandWidthEdit(this.bandWidthEdit.getText().toString());
        msgChannel.setImped(this.rgImped.getSelectItem());
        msgChannel.setLabel(label);
        msgChannel.setDelay(delay);
        msgChannel.setFineExtent(fineExtent);
        msgChannel.setFineSwitch(fineSwitch);
        sendMsgChannel(false);
        isLoadCache=false;
        setInvertLight(invert);
    }

    HashMap<String,String> beforeMap=new HashMap<>();
    private void saveParam() {

        String probeTypeIdx = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber);
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, probeTypeIdx);
        String probeMultiple = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber);
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber, probeMultiple);
        String bandWidthIdx = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber);
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber, bandWidthIdx);
        String bandWidthHighEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber);
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber, bandWidthHighEdit);
        String bandWidthLowEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber);
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, bandWidthLowEdit);
        String coupleIdx = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber);
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber, coupleIdx);
        String impedanceIdx = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber, impedanceIdx);

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+channelNumber,"");
    }
    private void recoverParam(){
        isLoadCache=true;
        String probeTypeIdx= beforeMap.get(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber);
        if (StrUtil.isEmpty(probeTypeIdx)) probeTypeIdx=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber,probeTypeIdx);

        String probeMultiple = beforeMap.get(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber);
        if (StrUtil.isEmpty(probeMultiple)) probeMultiple= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber,probeMultiple);

//        String bandWidthIdx =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber );
//        if (StrUtil.isEmpty(bandWidthIdx)) bandWidthIdx= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber);
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber,bandWidthIdx);
//
//        String bandWidthHighEdit =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber );
//        if (StrUtil.isEmpty(bandWidthHighEdit)) bandWidthHighEdit= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber);
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber,bandWidthHighEdit);
//
//        String bandWidthLowEdit =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber );
//        if (StrUtil.isEmpty(bandWidthLowEdit)) bandWidthLowEdit= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber);
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber,bandWidthLowEdit);

        String coupleIdx =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber );
        if (StrUtil.isEmpty(coupleIdx)) coupleIdx= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber,coupleIdx);

        String impedanceIdx =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);
        if (StrUtil.isEmpty(impedanceIdx)) impedanceIdx= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber,impedanceIdx);

        //recreate
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber-1);

        double d= channel.getVScaleVal()/channel.getProbeRate();
        double dResult = TBookUtil.getDoubleFromX(probeMultiple);
        String recoverFineExtent= TBookUtil.getMFromDouble(d*dResult);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+channelNumber,recoverFineExtent);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+channelNumber,"");
        btnchCheck.post(()->{
            setCache();
        });


    }

    public static final int ProbeType_NONE=0;
    public static final int ProbeType_MSP =1;
    public static final int probeType_MDP =2;
    public static final int ProbeType_MRCP=3;
    public static final int ProbeType_MOIP =4;


    private int getProbeType(Channel channel) {
        if (channel == null || !channel.isOpen()) return 0;
        BaseProbe baseProbe = channel.getProbe();
        if (isProbeInterface() && baseProbe != null) {
//            Log.d("Tag.Debug", String.format("RightLayoutChannel.getProbeType: %s",baseProbe.getModeName() ));
            if (baseProbe.getModeName().equals("MSP")) {
                return ProbeType_MSP;
            } else if (baseProbe.getModeName().equals("MDP")) {
                return probeType_MDP;
            } else if (baseProbe.getModeName().equals("MRCP")) {
                return ProbeType_MRCP;
            } else if (baseProbe.getModeName().equals("MOIP")) {
                return ProbeType_MOIP;
            }
        }
        return ProbeType_NONE;
    }

    public static int getProbeType(int chIdx) {
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if (channel == null || !channel.isOpen()) return 0;
        BaseProbe baseProbe = channel.getProbe();
        if (RightLayoutChannel.isProbeInterface(chIdx) && baseProbe != null) {
            if (baseProbe.getModeName().equals("MSP")) {
                return ProbeType_MSP;
            } else if (baseProbe.getModeName().equals("MDP")) {
                return probeType_MDP;
            } else if (baseProbe.getModeName().equals("MRCP")) {
                return ProbeType_MRCP;
            } else if (baseProbe.getModeName().equals("MOIP")) {
                return ProbeType_MOIP;
            }
        }
        return ProbeType_NONE;
    }

    private void setMdp700SelectVisible(){
        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
        int probeType=getProbeType(channel);
        if (probeType== probeType_MDP || probeType== ProbeType_MOIP) {
            BaseProbe bp = channel.getProbe();
            if (bp != null && bp.isAutoRateCtrl()) {
                mdp700Select.setVisibility(GONE);
            } else {
                mdp700Select.setVisibility(VISIBLE);
            }
        }
    }
    private void refreshProbeUI(Channel channel){
        int probeType=getProbeType(channel);
        if (probeType== ProbeType_MSP || probeType==ProbeType_MRCP){
            String sx= TBookUtil.getXFromDouble(channel.getProbe().getProbeRate());
            probeTypeX.setText(sx);
        }else if (probeType== probeType_MDP || probeType== ProbeType_MOIP){
            String s= TBookUtil.getXFromDouble(channel.getProbe().getProbeRate());
            mdp700Select.setSelectText(s);
            setMdp700SelectVisible();
        }else {
            //none
        }
    }
    private void updateProbeUI(Channel channel){
        updateProbeUI(channel,false);
    }
    private void updateProbeUI(Channel channel, boolean isFromEventBus){
        try {
            if (!ChannelFactory.isDynamicCh(channel.getChId())) {
                return;
            }
            if (!channel.isOpen() || channel.getChId() != channelNumber - 1) return;

            setProbeTypeLayout(channel);
            BaseProbe baseProbe = channel.getProbe();
            int probeType = getProbeType(channel);

//        if (isProbeInterface() && baseProbe != null) {
            if (probeType == ProbeType_MSP) {
                String model = baseProbe.getProbeName();
                String sn = baseProbe.getSN();
                String version = baseProbe.getVersion();
                List<String> probeX = baseProbe.getProbeX();
                double probeRate = baseProbe.getProbeRate();
                baseProbe.setProbeRate(probeRate);


                probeTypeModel.setText(model);
                probeTypeSN.setText(sn);
                String sx = TBookUtil.getXFromDouble(channel.getProbe().getProbeRate());
                probeTypeX.setText(sx);
//            probeTypeCall.setText(R.string.Information);
                if (channel.getResistanceType() != Channel.RESISTANCE_1M) {
                    channel.setResistanceType(Channel.RESISTANCE_1M);
                }
                msgChannel.setImped(rgImped.getSelectItem());
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(baseProbe.getProbeType()));
                sendMsgChannel(true);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + channelNumber, "");
                int impIdx = baseProbe.isImped50() ? 1 : 0;
                rgImped.setSelectIndex(impIdx);
                if (baseProbe.isImped50()) {
                    rgImped.setEnabled(0, false);
                    rgImped.setEnabled(1, true);
                } else {
                    rgImped.setEnabled(0, true);
                    rgImped.setEnabled(1, false);
                }
                ChannelFactory.chActivate(channel.getChId());
            } else if (probeType == ProbeType_MRCP) {
                String model = baseProbe.getProbeName();
                String sn = baseProbe.getSN();
                String version = baseProbe.getVersion();
                List<String> probeX = baseProbe.getProbeX();
                double probeRate = baseProbe.getProbeRate();
                baseProbe.setProbeRate(probeRate);

                String sx = TBookUtil.getXFromDouble(channel.getProbe().getProbeRate());
                mrcpProbeTypeX.setText(sx);
                mrcpMode.setText("Model:" +model);
                if (channel.getResistanceType() != Channel.RESISTANCE_1M) {
                    channel.setResistanceType(Channel.RESISTANCE_1M);
                }
//                msgChannel.setImped(rgCouple.getSelectItem());
                msgChannel.setImped(rgImped.getSelectItem());
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(baseProbe.getProbeType()));
                sendMsgChannel(true);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + channelNumber, "");
                int impIdx = baseProbe.isImped50() ? 1 : 0;
                rgImped.setSelectIndex(impIdx);
                if (baseProbe.isImped50()) {
                    rgImped.setEnabled(0, false);
                    rgImped.setEnabled(1, true);
                } else {
                    rgImped.setEnabled(0, true);
                    rgImped.setEnabled(1, false);
                }
                ChannelFactory.chActivate(channel.getChId());
            } else if (probeType == probeType_MDP || ProbeType_MOIP == probeType) {

                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(baseProbe.getProbeType()));
                mdp700Model.setText("Model:" + baseProbe.getProbeName());
                String sx = TBookUtil.getXFromDouble(baseProbe.getProbeRate());

                String[] s = baseProbe.getScaleNames().toArray(new String[0]);
                mdp700Select.setArray(s);
                int index = Tools.indexOf(s, i -> i.equals(sx));
                if (index >= 0 && index <= 1) {
                    mdp700Select.setSelectIndex(index);
                }
                setMdp700SelectVisible();

                int impIdx = baseProbe.isImped50() ? 1 : 0;
                rgImped.setSelectIndex(impIdx);
                if (baseProbe.isImped50()) {
                    rgImped.setEnabled(0, false);
                    rgImped.setEnabled(1, true);
                } else {
                    rgImped.setEnabled(0, true);
                    rgImped.setEnabled(1, false);
                }
                // set bandwidth
                setProbeBandWidth(baseProbe);

                msgChannel.setBandWidth(rgBandWidth.getSelectItem());
                msgChannel.setBandWidthEdit(TBookUtil.getHz3FromHz(baseProbe.getBandWidth()));
                msgChannel.setProbeMultiple(sx);
//                msgChannel.setImped(rgCouple.getSelectItem());
                msgChannel.setImped(rgImped.getSelectItem());
                sendMsgChannel(true);
                ChannelFactory.chActivate(channel.getChId());
            } else {
                dialogProbeInterface.hide();
                int unit = channel.getProbeType();
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(unit));
                msgChannel.setChCheck(channel.isOpen());
                sendMsgChannel(true);
                rgImped.setEnabled(true);
                unLockScreen();
            }
            if(!isFromEventBus) {
//                channel.setResistanceType(rgCouple.getSelectIndex());
                channel.setResistanceType(rgImped.getSelectIndex());
            }
//            RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_SELECT, new MainCenterMsgChannels(channel.getChId() + IWave.Ch1, false, true));
            MiddleMain.getIns().getChanSelectorManage().setActivityChannel(IChan.toIChan(channel.getChId()),isFromEventBus);
            refreshOffset();
        }catch (Exception e){

        }

        int impedance = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);
        int couple = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber);
        this.rgImped.setEnabled(1, matchCouple(couple) != Channel.COUPLE_TYPE_AC);//耦合方式选择AC时，阻抗50Ω置灰
        this.rgCouple.setEnabled(1, impedance != Channel.RESISTANCE_50);//阻抗选择50Ω时， 耦合方式AC置灰
    }

    private void setProbeBandWidth(BaseProbe baseProbe) {
        if (baseProbe == null) return;
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);
        if (channel == null) return;
        if (rgBandWidth.getSelectIndex() == 0 || rgBandWidth.getSelectIndex() == 3) {
            rgBandWidth.setSelectIndex(0);
            bandWidthEdit.setEnabled(false);
            bandWidthEdit.setText("");
            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, getMaxBandWidth());

        } else if (rgBandWidth.getSelectIndex() == 4) {
            long probeHz = baseProbe.getBandWidth();
            String probeBandwidth = TBookUtil.getHz3FromHz(probeHz);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH + channelNumber, probeBandwidth);

            double Hz = getBandWidthCurScopeMaxProbe();
            String showBandwidth = TBookUtil.getHz3FromHz(Hz);

            bandWidthEdit.setText(showBandwidth);
            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, Hz);
        }
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber, String.valueOf(rgBandWidth.getSelectIndex()));
    }

    private void setProbeBandWidth_Change(BaseProbe baseProbe){
        if (baseProbe == null) return;
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);
        if (channel == null) return;
        if (rgBandWidth.getSelectIndex() == 0 /*|| rgBandWidth.getSelectIndex()==1*/) {
            rgBandWidth.setSelectIndex(0);
            bandWidthEdit.setEnabled(false);
            bandWidthEdit.setText("");
            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, getMaxBandWidth());

        } else if (rgBandWidth.getSelectIndex() == 4) {
            long probeHz = baseProbe.getBandWidth();
            long scopeHz = getScopeBandWidth();
            long Hz = Math.min(scopeHz, probeHz);
            String showBandwidth = TBookUtil.getHz3FromHz(Hz);
//            Log.d("Tag.Debug", String.format("RightLayoutChannel.setProbeBandWidth_Change: %s",showBandwidth ));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, showBandwidth);
            bandWidthEdit.setText(showBandwidth);
            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, Hz);
        }
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber, String.valueOf(rgBandWidth.getSelectIndex()));
    }

    private long getMaxBandWidth(){
        long scopeBandWidth = (long) Channel.getMaxBandWidth();
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);
        if (channel == null) return scopeBandWidth;
        int probeType = getProbeType(channel);
        long probeBandWidth = 0;
        if (probeType != ProbeType_NONE && probeType != ProbeType_MSP) {
            probeBandWidth = channel.getProbe().getBandWidth();

        }
        if (probeType == ProbeType_NONE || probeType == ProbeType_MSP) {
            return scopeBandWidth;
        } else {
            return Math.min(scopeBandWidth, probeBandWidth);
        }
    }

    private void refreshOffset(){
        Channel channel= ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
        String text=chOffset.getText();
        String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";
        text= text.replace("A",unit).replace("V",unit);
        chOffset.setText(text);
    }

    private int matchCouple(int couple) {
        if (couple == 0) return Channel.COUPLE_TYPE_DC;
        if (couple == 1) return Channel.COUPLE_TYPE_AC;
        return Channel.COUPLE_TYPE_GND;
    }

    private int unMatchCouple(int scopeCouple) {
        if (scopeCouple == Channel.COUPLE_TYPE_DC) return 0;
        if (scopeCouple == Channel.COUPLE_TYPE_AC) return 1;
        return 2;
    }

    private boolean isEnableHighLowFilter() {
        return ScopeConfig.getConfig().isEnableHighLowFilter() || App.IsDebug();
    }

    private void sendMsgChannel(boolean isFromEventBus) {
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
            if (channelNumber == TChan.Ch3 || channelNumber == TChan.Ch4
                    || channelNumber == TChan.Ch5 || channelNumber == TChan.Ch6
                    || channelNumber == TChan.Ch7 || channelNumber == TChan.Ch8)
            {
                return;
            }
        } else if(GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {
            if (channelNumber == TChan.Ch5 || channelNumber == TChan.Ch6
                    || channelNumber == TChan.Ch7 || channelNumber == TChan.Ch8)
            {
                return;
            }
        }
        msgChannel.setFromEventBus(isFromEventBus);
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_CHANNEL, msgChannel);
    }

    private void setProbeTypeLayout(Channel channel) {
        int probeType = getProbeType(channel);
        switch (probeType) {
            case ProbeType_NONE: {
                commonLayout.setVisibility(VISIBLE);
                msp500Layout.setVisibility(GONE);
                mdp700Layout.setVisibility(GONE);
                mrcpLayout.setVisibility(GONE);
                this.rgBandWidth.setEnabled(0, true);//full
                this.rgBandWidth.setEnabled(3, true);//highpass
            }
            break;
            case ProbeType_MSP: {
                commonLayout.setVisibility(GONE);
                msp500Layout.setVisibility(VISIBLE);
                mdp700Layout.setVisibility(GONE);
                mrcpLayout.setVisibility(GONE);
                this.rgBandWidth.setEnabled(0,true);
                this.rgBandWidth.setEnabled(3, true);
            }
            break;
            case ProbeType_MRCP:{
                commonLayout.setVisibility(GONE);
                msp500Layout.setVisibility(GONE);
                mdp700Layout.setVisibility(GONE);
                mrcpLayout.setVisibility(VISIBLE);
                this.rgBandWidth.setEnabled(0,true);
                this.rgBandWidth.setEnabled(3, false);
            }break;
            case ProbeType_MOIP:
            case probeType_MDP: {
                commonLayout.setVisibility(GONE);
                msp500Layout.setVisibility(GONE);
                mdp700Layout.setVisibility(VISIBLE);
                mrcpLayout.setVisibility(GONE);
                this.rgBandWidth.setEnabled(0,true);
                this.rgBandWidth.setEnabled(3, false);
            }
            break;
        }
    }

    private boolean isProbeInterface() {
        return isProbeInterface(TChan.toFpgaChNo(channelNumber));
    }

    public static boolean isProbeInterface(int idx) {
        Channel channel = ChannelFactory.getDynamicChannel(idx);
        return channel != null && channel.isAutoProbe() && !StrUtil.isEmpty(channel.getProbe().getSN());
//        return true;
    }

    private Consumer<Integer> onDialogClose=(i)->{
        if (i== MainViewGroup.DIALOG_PROBE_INTERFACE ){
           setMdp700SelectVisible();
        }
    };

    private Consumer<Boolean> consumerExternalkeysVernier = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {

            int chidx = ChannelFactory.getChActivate();
            if(ChannelFactory.isDynamicCh(chidx)){
                if(chidx == channelNumber - 1){
                    PlaySound.getInstance().playButton();
                    boolean state = !btnFineSwitch.isState();
                    btnFineSwitch.setState(state);
//                    chFineExtent.setEnabled(state);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE + channelNumber, String.valueOf(state));
                    Command.get().getChannel().Vernier(chidx,state,false);
                    msgChannel.setFineSwitch(state);
                    sendMsgChannel(false);
                }
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutChannel, true);
        }
    };

    private Consumer<LoadCache> consumerLoadCacheEx = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCacheEx();
        }
    };

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            boolean isNoXY = workModeBean.getNextWorkMode() != IWorkMode.WorkMode_XY;
            if (!isNoXY) {
                btnchCheck.setState(true);
                btnchCheck.setEnabled(false);
                rgVerBase.setEnabled(false);
                chLabel.setEnabled(false);
                chDelay.setEnabled(false);
                chOffset.setEnabled(false);
                chPosition.setEnabled(false);
                chFineExtent.setEnabled(false);
                btnFineSwitch.setEnabled(false);
            } else {
                btnchCheck.setEnabled(true);
                rgVerBase.setEnabled(true);
                chLabel.setEnabled(true);
                chDelay.setEnabled(true);
                chOffset.setEnabled(true);
                chPosition.setEnabled(true);
                chFineExtent.setEnabled(true);
                btnFineSwitch.setEnabled(true);
            }
        }
    };

    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() {
        @Override
        public void accept(MainRightMsgChannels msgChannels)  {
            switch (channelNumber) {
                case TChan.Ch1:
                    if (msgChannels.getCh1().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh1().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch1, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch2:
                    if (msgChannels.getCh2().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh2().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch2, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch3:
                    if (msgChannels.getCh3().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh3().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch3, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch4:
                    if (msgChannels.getCh4().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh4().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch4, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch5:
                    if (msgChannels.getCh5().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh5().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch5, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch6:
                    if (msgChannels.getCh6().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh6().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch6, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch7:
                    if (msgChannels.getCh7().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh7().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch7, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch8:
                    if (msgChannels.getCh8().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh8().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch8, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
            }
        }
    };

    private Consumer<String> consumerChannelBandWidth=new Consumer<String>() {
        @Override
        public void accept(String s) throws Exception {
            String[] param= s.split(",");
            String fb=param[0];
            int chIdx=Integer.parseInt(param[1]);
            if (chIdx!=channelNumber-1) return;
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+channelNumber,fb);

            double bandWidth= getBandWidthCurScopeCurProbe();
//            Log.d("Tag.Debug", String.format("RightLayoutChannel.accept: %f",bandWidth ));
            sendChannelBandWidth(bandWidth);
        }
    };

    private Consumer<String> consumerChannelVscale = new Consumer<String>() {

        //接收垂直档位调整消息
        @Override
        public void accept(String adjustStr) throws Throwable {

            String[] param = adjustStr.split(CommandMsgToUI.PARAM_SPLIT);
            boolean isClickTop = Boolean.parseBoolean(param[0]); // 解析参数，判断是向上还是向下调整
            int chan = Integer.parseInt(param[1]);
            if (chan != channelNumber) return;

            if (isClickTop) {
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174);
                msgChannel.setUpClick(true);
                postChange();// 调用postChange()进行档位切换
            } else {
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174);
                msgChannel.setUpClick(false);
                postChange();// 调用postChange()进行档位切换
            }
        }
    };

    private Consumer<String> consumerChannelPosition = new Consumer<String>() {

        @Override
        public void accept(String messageStr) throws Throwable {
            String [] parts = messageStr.split(";",2);
            String positionStr = parts[0].trim();
            int chId = Integer.parseInt(parts[1].trim());
            if(chId+1 == channelNumber){
                chPosition.setText(positionStr);
                positionString = positionStr;
            }
        }
    };
    /**
     * 当前探头选择带宽与示波器最大带宽比较
     * @return
     */
    private double getBandWidthCurScopeCurProbe(){
        long probeBandWidth= getProbeBandWidth();
        long scopeBandWidth= getScopeBandWidth();
        //        Log.d("Tag.Debug", String.format("RightLayoutChannel.getFinalBandWidth probe: %d ,scope: %d",probeBandWidth,scopeBandWidth ));
        return Math.min(probeBandWidth, scopeBandWidth);
    }

    private double getBandWidthCurScopeMaxProbe(){
        long scopeBandWidth= getScopeBandWidth();
        BaseProbe baseProbe= ChannelFactory.getDynamicChannel(channelNumber-1).getProbe();
        long probeBandWidth=Long.MAX_VALUE;
        if (baseProbe!=null){
            probeBandWidth=baseProbe.getBandWidth();
        }
        return Math.min(scopeBandWidth, probeBandWidth);
    }

    private long getProbeBandWidth(){
        Channel channel=ChannelFactory.getDynamicChannel(channelNumber-1);
        String fb= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+channelNumber);
        if (StrUtil.isEmpty(fb) || channel.getProbe()==null ) return Long.MAX_VALUE;
        long probeBandWidth=  (long) (TBookUtil.getMHzFromHz(fb) * 1000 * 1000);
        return probeBandWidth;
    }

    private long getScopeBandWidth() {
        long scopeBandWidth = (long) 20e6;
        switch (rgBandWidth.getSelectIndex()) {
            case 0://full
                scopeBandWidth = getMaxBandWidth();
                break;
            case 1://200M
                scopeBandWidth = (long) 200e6;
                break;
            case 2://20M
                scopeBandWidth = (long) 20e6;
                break;
            case 3://highpass
                String bandWidthHighEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber);
                scopeBandWidth = (long) (TBookUtil.getMHzFromHz(bandWidthHighEdit) * 1000 * 1000);
                break;
            case 4://lowpass
                String bandWidthLowEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber);
                scopeBandWidth = (long) (TBookUtil.getMHzFromHz(bandWidthLowEdit) * 1000 * 1000);
                break;
        }
        return scopeBandWidth;
    }

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_CHANNEL_DISPLAY: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    boolean isOpen = Boolean.parseBoolean(params[1]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    if (!btnchCheck.isEnabled()) {
                        return;
                    }
                    btnchCheck.setState(isOpen);
                    operationUI_ToggleState(btnchCheck, isOpen, false);
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_INVERSE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    boolean isOpen = Boolean.parseBoolean(params[1]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    btnInvert.setState(isOpen);
                    operationUI_ToggleState(btnInvert, isOpen, false);
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_BANDWIDTH: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int bandIndex = Integer.parseInt(params[1]);
                    double bandDetail = Double.parseDouble(params[2]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    if (!isEnableHighLowFilter() && (bandIndex == 3 || bandIndex == 4)) {
                        return;
                    }
                    rgBandWidth.setSelectIndex(bandIndex);
                    setBandWidth(bandDetail);
//                    if (bandIndex == 3) {//highpass
//                        bandWidthEdit.setEnabled(true);
//                        bandWidthEdit.setText(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber));
//                    } else if (bandIndex == 4) {//lowpass
//                        bandWidthEdit.setEnabled(true);
//                        bandWidthEdit.setText(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber));
//                    }
                    onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), false);
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_PROBETYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int probeTypeIndex = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    rgProbeType.setSelectIndex(probeTypeIndex);
                    onItemClick(rgProbeType.getId(), rgProbeType.getSelectItem(), false);
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_PROBEMULTIPLE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    double multiple = Double.parseDouble(params[1]);
                    String probeRate = TBookUtil.getXFromDouble(multiple);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    if (!dialogProbeMultiple.isExistProbeRate(probeRate)) return;
                    onTextClick(probeRate, false);
                    dialogProbeMultiple.hide();
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_COUPLE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int coupleIndex = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    rgCouple.setSelectIndex(coupleIndex);
                    onItemClick(rgCouple.getId(), rgCouple.getSelectItem(), false);
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_INPUTRES: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int inp = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    rgImped.setSelectIndex(inp);
                    onItemClick(rgImped.getId(), rgImped.getSelectItem(), false);
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_VREF: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int vref = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    rgVerBase.setSelectIndex(vref);
                    onItemClick(rgVerBase.getId(), rgVerBase.getSelectItem(), false);
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_LABEL: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String result = params[1];
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    chLabel.setText(result);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + channelNumber, result);
                    WaveManage.get().setChannelLabel(channelNumber, result);
                    setChannelLabel(chIndex, result);
                }
                break;
                case CommandMsgToUI.FLAG_CHANNEL_LABEL_CLEAR: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String result = "";
//                    Logger.i(Command.TAG, "label clear");
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    chLabel.setText(result);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + channelNumber, result);
                    WaveManage.get().setChannelLabel(channelNumber, result);
                    setChannelLabel(chIndex, result);
                }
                break;
                case CommandMsgToUI.FLAG_CHANNEL_DELAY:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    double result = Double.parseDouble( params[1]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;
                    String show= TBookUtil.getTimeFromS(result);
                    int delay= (int)(TBookUtil.getDoubleFromM(show.replace("s", "")) * 1e12 + 0.1);
                    if(-Channel.MAX_DELAY_PS > delay || delay > Channel.MAX_DELAY_PS) {
                       // show="100 ns";
                        show="500 ns";
                    }

                    chDelay.setText(show);
                    msgChannel.setDelay(show);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + channelNumber, show);
                    Channel channel = ChannelFactory.getDynamicChannel(chIndex);
                    if(channel != null){
                        channel.setDelay((int)(TBookUtil.getDoubleFromM(show.replace("s", "")) * 1e12 + 0.1));
                    }
                }break;
                case CommandMsgToUI.FLAG_CHANNEL_OFFSET:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    double result = Double.parseDouble( params[1]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;

                    Channel channel = ChannelFactory.getDynamicChannel(chIndex);
                    String unit = ChannelFactory.getProbeType(channel.getChId());
                    String show = TBookUtil.getMFromDouble(Math.abs(result));
                    if(result < 0) show = "-" + show;
                    if (show.equals("")){
                        show="0 "+unit;
                    }else {
                        show+=unit;
                    }
                    chOffset.setText(show);
                    if (channel!=null) {
                        channel.setChOffsetVal(result);
                    }
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_OFFSET + channelNumber, show);

                }break;
                case CommandMsgToUI.FLAG_CHANNEL_VERNIER:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    boolean state = Boolean.parseBoolean( params[1]);
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;

                    btnFineSwitch.setState(state);
                    chFineExtent.setEnabled(state);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE + channelNumber, String.valueOf(state));
                    msgChannel.setFineSwitch(state);
                    sendMsgChannel(false);

                }break;
            }
        }
    };

    public void setChannelLabel(int chNo, String label) {
        Channel channel = ChannelFactory.getDynamicChannel(chNo);
        if(channel != null){
            channel.setLabel(label);
        }
    }

    private void operationUI_ToggleState(MSwitchBox view, boolean state, boolean isFromEventBus) {
        if (view.getId() == btnInvert.getId()) {
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_INVERT + channelNumber, String.valueOf(state));
            Command.get().getChannel().Inverse(TChan.toFpgaChNo(channelNumber), state, false);
            if (!isFromEventBus) {
                ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber)).setInvert(state);
            }
            msgChannel.setInvert(state);
            setInvertLight(state);
            sendMsgChannel(isFromEventBus);
        } else if (view.getId() == btnchCheck.getId()) {
            if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE + channelNumber, String.valueOf(btnchCheck.isState()));
            }
            if (!btnchCheck.isState()) {
                ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
            }
            msgChannel.setChCheck(btnchCheck.isState());
            sendMsgChannel(isFromEventBus);
        }else if (view.getId()==btnFineSwitch.getId()){
//            chFineExtent.setEnabled(state);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE + channelNumber, String.valueOf(state));
            Command.get().getChannel().Vernier(TChan.toFpgaChNo(channelNumber),state,false);
            msgChannel.setFineSwitch(state);
            sendMsgChannel(isFromEventBus);
        }
    }

    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            operationUI_ToggleState(view, state, false);
        }
    };

    private final OnClickListener onClickListener = new OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            ScreenUtil.getViewLocation(v);
            switch (v.getId()) {
                case R.id.probeMultiple:
                    dialogProbeMultiple.setData(channelNumber, probeMultiple.getText().toString(), CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + channelNumber, onProbeMultipleDismissListener);
                    break;
                case R.id.bandWidthEdit:
                    long maxBandWidth = getMaxBandWidth();
                    String s = rgBandWidth.getSelectItem().getText();
                    dialogBandWidthHz.setValue(s, bandWidthEdit.getText().toString()
                            , 30, maxBandWidth
                            , onBandWidthDismissListener);
                    break;
                case R.id.probeTypeCall:
                    //Logger.d(TAG,"ch:" + channelNumber);
                    double[] param = {channelNumber - 1, VerticalAxis.DANG_50mV};
                    BaseProbe baseProbe = ChannelFactory.getDynamicChannel(channelNumber - 1).getProbe();
                    if (baseProbe != null && baseProbe.isDa()) {
                        ProbeCalibrate probeCalibrate = ProbeCalibrate.getInstance();
                        if (!probeCalibrate.isCalibrate()) {
                            ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
                            workMode = WorkModeManage.getInstance().getmWorkMode();
                            if (workMode != IWorkMode.WorkMode_YT) {
                                WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, false);
                            }
                            ProbeCalibrate.getInstance().begin(param);
                            lockScreen();
                        }
                    }
                    break;
                case R.id.mdp700Info:
                case R.id.mrcpProbeTypeCall:
                    Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);
                    if (channel == null) return;
                    int type = getProbeType(channel);
                    dialogProbeInterface.show(channel, type);
                    break;
                case R.id.btnTop:
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174);
                    msgChannel.setUpClick(true);
                    postChange();
                    break;
                case R.id.btnBottom:
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174);
                    msgChannel.setUpClick(false);
                    postChange();
                    break;
            }
        }
    };

    private void postChange() {
        if (myHandler.hasMessages(HANDLE_MSG)) {
            myHandler.removeMessages(HANDLE_MSG);
        }
        myHandler.sendEmptyMessageDelayed(HANDLE_MSG, 200);
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_CHX, msgChannel); // 发送消息到消息队列
    }

    int workMode = IWorkMode.WorkMode_YT;
    private void lockScreen() {

        ScreenControls screenControls = ScreenControls.getInstance();
        screenControls.lockScreen(ScreenControls.LOCK_PROBE << (channelNumber-1));
    }

    private void unLockScreen() {
        ScreenControls screenControls = ScreenControls.getInstance();
        screenControls.unLockScreen(ScreenControls.LOCK_PROBE << (channelNumber-1));
    }
    private DialogBandWidthHz.OnDismissListener onBandWidthDismissListener = new DialogBandWidthHz.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
//            PlaySound.getInstance().playButton();
            if (!isEnableHighLowFilter() && (rgBandWidth.getSelectIndex() == 3 || rgBandWidth.getSelectIndex() == 4)) {
                return;
            }
            bandSwitchChange(result);
        }

        public void bandSwitchChange(String fb) {
            long probeBandWidth= getProbeBandWidth();
            long scopeBandwidth=(long) (TBookUtil.getMHzFromHz(fb)*1e6);
            double bandWidth= scopeBandwidth>=probeBandWidth?probeBandWidth:scopeBandwidth;
            setBandWidth(fb);
            sendChannelBandWidth(bandWidth);
        }
    };

    private void onItemClick(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        Tools.PrintControlsLocation("RightLayoutChannel", rootViewGroup);
        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
        if(channel == null) return;
        if (viewId == rgCouple.getId()) {
            //设置耦合方式
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber, String.valueOf(item.getIndex()));
            Command.get().getChannel().Couple(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);
            if (!isFromEventBus) {
//                int resistanceType = Channel.RESISTANCE_1M;
//                int coupleType = Channel.COUPLE_TYPE_DC;
//                switch(item.getIndex()){
//                    case 1:
//                        coupleType = Channel.COUPLE_TYPE_AC;
//                        break;
//                    case 2:
//                        resistanceType = Channel.RESISTANCE_50;
//                        break;
//                }
                channel.setCoupleType(matchCouple(item.getIndex()));
//                channel.setResistanceType(resistanceType);
            }
            this.rgImped.setEnabled(1, matchCouple(item.getIndex()) != Channel.COUPLE_TYPE_AC);//选择AC时50Ω置灰
            msgChannel.setCouple(item);
//            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber, String.valueOf(item.getIndex()));
//            Command.get().getChannel().Inputres(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);
//            msgChannel.setImped(item);

            sendMsgChannel(isFromEventBus);
        } else if (viewId == rgProbeType.getId()) {
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(item.getIndex()));
            Command.get().getChannel().Prty(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);
            if (!isFromEventBus) {
                channel.setProbeType(item.getIndex());
            }
            msgChannel.setProbeType(item);
            sendMsgChannel(isFromEventBus);
            if (chOffset.getText().equals("")!=true){
                String text=chOffset.getText();
                String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";
                text= text.replace("A",unit).replace("V",unit);
                chOffset.setText(text);
            }
            if (chPosition.getText().equals("")!=true){
                String text=chPosition.getText();
                String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";
                text= text.replace("A",unit).replace("V",unit);
                chPosition.setText(text);
            }
            refreshFineExtent();
        } else if (viewId == rgBandWidth.getId()) {
            if (!isEnableHighLowFilter() && (item.getIndex() == 3 || item.getIndex() == 4)) {
                return;
            }
            setProbeBandWidth_Change(channel.getProbe());

            double hz = TBookUtil.getMHzFromHz(bandWidthEdit.getText().toString());
            Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), item.getIndex(), hz, false);

            switch (item.getIndex()) {
                case 0://Full
                    bandWidthEdit.setEnabled(false);
                    bandWidthEdit.setText("");
                    if (!isFromEventBus) {
                        double d = getBandWidthCurScopeCurProbe();
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, d);
                    }
                    break;
                case 1://200M
                    bandWidthEdit.setEnabled(false);
                    bandWidthEdit.setText("");
                    if (!isFromEventBus) {
                        double d = getBandWidthCurScopeCurProbe();
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_200M, d);
                    }
                    break;
                case 2://20M
                    bandWidthEdit.setEnabled(false);
                    bandWidthEdit.setText("");
                    if (!isFromEventBus) {
                        double d = getBandWidthCurScopeCurProbe();
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_20M, d);
                    }
                    break;
                case 3://Highpass
                    bandWidthEdit.setEnabled(true);
                    bandWidthEdit.setText(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber, bandWidthEdit.getText().toString());
                    if (!isFromEventBus) {
                        double d = getBandWidthCurScopeCurProbe();
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_HIGHPASS, d);
                    }
                    break;
                case 4://Lowpass
                    bandWidthEdit.setEnabled(true);
                    bandWidthEdit.setText(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, bandWidthEdit.getText().toString());
                    if (!isFromEventBus) {
                        double d = getBandWidthCurScopeCurProbe();
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, d);
                    }
                    break;
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber, String.valueOf(item.getIndex()));
            msgChannel.setBandWidthEdit(bandWidthEdit.getText().toString());
            msgChannel.setBandWidth(item);
            sendMsgChannel(isFromEventBus);
        } else if (viewId == rgVerBase.getId()) {
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_VERTICALBASE + channelNumber, String.valueOf(item.getIndex()));
            Command.get().getChannel().Vref(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);
            if (!isFromEventBus) {
                channel.setVerticalMode(item.getIndex());
            }
        } else if (viewId == rgImped.getId()) {
            int resistanceType = Channel.RESISTANCE_1M;
            if (!isFromEventBus) {
                if (item.getIndex() == 0) {
                    resistanceType = Channel.RESISTANCE_1M;
                } else {
                    resistanceType = Channel.RESISTANCE_50;
                }
                Log.d(TAG,"调整----------------------------------resistanceType:"+resistanceType);
                channel.setResistanceType(resistanceType);
            }
            this.rgCouple.setEnabled(1, resistanceType != Channel.RESISTANCE_50);//选择50Ω时AC置灰
            item = rgImped.getSelectItem();
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber, String.valueOf(item.getIndex()));
            Command.get().getChannel().Inputres(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);
            msgChannel.setImped(item);
            sendMsgChannel(isFromEventBus);
        } else if (viewId == mdp700Select.getId()) {
            String result= mdp700Select.getSelectItem().getText().toString();
            if (StrUtil.isEmpty(result)) return;
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber, result);
            probeMultiple.setText(result);
            double dResult = TBookUtil.getDoubleFromX(result);
            Command.get().getChannel().Probe(TChan.toFpgaChNo(channelNumber), dResult, false);
            if (!isFromEventBus) {
                channel.setProbeRate(TBookUtil.getDoubleFromX(result));
            }
            msgChannel.setProbeMultiple(probeMultiple.getText().toString());
            msgChannel.setProbeMulScale(TBookUtil.getDoubleFromX(preProbeMul) / TBookUtil.getDoubleFromX(result));
            sendMsgChannel(isFromEventBus);
            preProbeMul = result;

            refreshFineExtent();
        }
    }

    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            RightLayoutChannel.this.onItemClick(viewId, item, false);
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    private void onTextClick(String result, boolean isFromEventBus) {
        result = result.replace("X", "");
        if (result.contains(".")) {
            while (result.endsWith("0")) {
                result = result.substring(0, result.length() - 1);
            }
            if (result.endsWith(".")) {
                result = result.substring(0, result.length() - 1);
            }
        }
        result = result + "X";

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber, result);
        probeMultiple.setText(result);
        double dResult = TBookUtil.getDoubleFromX(result);
        Command.get().getChannel().Probe(TChan.toFpgaChNo(channelNumber), dResult, false);
        if (!isFromEventBus) {
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
            channel.setProbeRate(TBookUtil.getDoubleFromX(result));
        }
        msgChannel.setProbeMultiple(probeMultiple.getText().toString());
        msgChannel.setProbeMulScale(TBookUtil.getDoubleFromX(preProbeMul) / TBookUtil.getDoubleFromX(result));
        sendMsgChannel(isFromEventBus);
        preProbeMul = result;

        refreshFineExtent();

    }

    private void sendChannelBandWidth(double d) {
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);
        if (channel == null) return;
        switch (rgBandWidth.getSelectIndex()) {
            case 0:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, d);
                break;
            case 1:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_200M, d);
                break;
            case 2:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_20M, d);
                break;
            case 3:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_HIGHPASS, d);
                break;
            case 4:
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, d);
                break;
        }
    }

    private void refreshFineExtent(){
        Channel channel = ChannelFactory.getDynamicChannel(msgChannel.getChannelNumber() - 1);
        String s=TBookUtil.getMFromDouble(channel.getVScaleVal());
        String unit= channel.getProbeType()==0?"V":"A";
        chFineExtent.setText(s+unit);
    }

    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();
            if (v.getId() == chLabel.getId()) {
                if (dialogChannelLabel == null) {
                    dialogChannelLabel = (DialogChannelLabel) ((MainActivity) context).findViewById(R.id.dialogChannelLabel);
                }
                dialogChannelLabel.setData(channelNumber, chLabel.getText()
                        , CacheUtil.RIGHT_SLIP_CH_LABEL_USERDEFINE + channelNumber
                        , DialogChannelLabel.FROM_CHANNEL
                        , result -> {
                            PlaySound.getInstance().playButton();
                            chLabel.setText(result);
                            msgChannel.setLabel(result);
                            Command.get().getChannel().Label(channelNumber - 1, result, false);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + channelNumber, result);
                            WaveManage.get().setChannelLabel(channelNumber, result);
                            setChannelLabel(TChan.toFpgaChNo(channelNumber), result);
                        });
            } else if (v.getId() == chDelay.getId()) {
                if (dialogFloatKeyBoard == null) {
                    dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
                }
                dialogFloatKeyBoard.setFloatpnData(chDelay.getText().replace("s", "").replace(" ",""), chDelay, new TopDialogFloatKeyBoard.OnDismissListener() {
                    @Override
                    public void onDismiss(View fromView, String show) {
//                        boolean isP = chDelay.getText().contains("p");
                        PlaySound.getInstance().playButton();
                        if ("0".equals(show.trim())) {
//                            show = isP ? "0 ps" : "0 ns";
                            show = "0 ns";
                        } else {
                            show = show + "s";
                        }
                        int delay= (int)(TBookUtil.getDoubleFromM(show.replace("s", "")) * 1e12 + 0.1);
                        if (delay > Channel.MAX_DELAY_PS) {
                            //show="100 ns";
                            show="500 ns";
                        }
                        if (-Channel.MAX_DELAY_PS > delay) {
                            //show="-100 ns";
                            show="-500 ns";
                        }
                        chDelay.setText(show);
                        msgChannel.setDelay(show);
                        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + channelNumber, show);
                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
                        if(channel != null){
                            channel.setDelay((int)(TBookUtil.getDoubleFromM(show.replace("s", "")) * 1e12 + 0.1));
                        }
                        double _delay=TBookUtil.getDoubleFromM(show.replace("s",""));
                        Command.get().getChannel().Delay(TChan.toFpgaChNo(channelNumber),_delay,false);


                    }
                });
            } else if (v.getId() == chOffset.getId()) {
                if (WorkModeManage.getInstance().isXyMode()) return;
                setChOffset(chOffset);

            }else if (v.getId()==chFineExtent.getId()){
                fineExtentDialog(chFineExtent);
            } else if (v.getId() == chPosition.getId()){
                if (WorkModeManage.getInstance().isXyMode()) return;
                setChPosition(chPosition);
            }
        }
    };

    private void setChOffset(TopViewEdit topViewEdit) {
        if (dialogFloatKeyBoard == null) {
            dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
        }
        dialogFloatKeyBoard.setFloatData(chOffset.getText().replace("A","").replace("V","").replace(" ",""), chOffset, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
                if(channel != null){
                    String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";
                    double val = TBookUtil.getDoubleFromM(show);
                    channel.setChOffsetVal(val);
                    onRefreshOffset(channel);
                    Command.get().getChannel().Offset(TChan.toFpgaChNo(channelNumber),val,false);
                }
            }
        });
    }

    private void setChPosition(TopViewEdit topViewEdit) {
        if (dialogFloatKeyBoard == null) {
            dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
        }
        dialogFloatKeyBoard.setFloatData_Offset(topViewEdit.getText().replace("A", "").replace("V", "").replace(" ", ""), true, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
                if(channel != null){
                    String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";
                    double d = channel.getVerticalPerPix();
                    double val = TBookUtil.getDoubleFromM(show);
                    double offsetPix = (Tools.isZoom() ? ScopeBase.getNewZoomHeight() : ScopeBase.getNewHeight()) / 2.0 - (val / d);
//                    Tools.putYTChannelPosition(channelNumber,offsetPix);
//                    double y = Tools.getYTChannelPosition((channelNumber));
//                    channel.setPos(y);

                    CursorManage.getInstance().setScpiChanIdx(channelNumber);
                    CursorManage.getInstance().setCursorTrace(true);
                    WaveManage.get().setPositionY(channelNumber,offsetPix);
                    CursorManage.setCursorByScaleTrace();
                    CursorManage.getInstance().setCursorTrace(false);
//                    Command.get().getChannel().Position(TChan.toFpgaChNo(channelNumber),offsetPix,false);

                    onRefreshPosition(channel);
                }
            }
        });

    }


    private void fineExtentDialog(TopViewEdit topViewEdit){
        if (dialogFloatKeyBoard == null) {
            dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
        }
        String txt=topViewEdit.getText().replace("V", "").replace("A","").replace(" ","");
        dialogFloatKeyBoard.setFloatData_Extent(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
                String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";
                topViewEdit.setText(show+unit);
                msgChannel.setFineExtent(show+unit);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT + channelNumber, show);
                double d=TBookUtil.getDoubleFromM(show);
                d= getVerticalRange(d,channel);
                channel.setVScaleVal(d);

//                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
//                msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_EXTENT);
//                String param = String.valueOf(channel.getChId()) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(d);
//                msgToUI.setParam(param);
//                RxBus.getInstance().post(RxSendBean.COMMAND_TO_UI, msgToUI);
            }
        });
    }
    /** 返回有效档位值 */
    private double getVerticalRange(double input,Channel chan){
        double min=0;
        double max=0;

        min= VerticalAxis.getScaleIdValById(VerticalAxis.getMinGear())*chan.getProbeRate();
        max=VerticalAxis.getScaleIdValById(VerticalAxis.getMaxGear());
        if (chan.getResistanceType()==Channel.RESISTANCE_50){
            max=VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1V);
        }
        max*=chan.getProbeRate();

        if (input<min){
            input=min;
        }
        if (input>max){
            input=max;
        }
        return input;

    }
    private DialogProbeMultiple.OnDismissListener onProbeMultipleDismissListener = new DialogProbeMultiple.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            PlaySound.getInstance().playButton();
            onTextClick(result, false);
        }
    };


    private void setBandWidth(String fb) { //DialogBandWidthHz dismiss时候设置值用，所以只会有highpass 和 lowpass
        double hz = TBookUtil.getMHzFromHz(bandWidthEdit.getText().toString());
        Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), rgBandWidth.getSelectIndex(), hz, false);
        switch (rgBandWidth.getSelectIndex()) {
            case 0:
            case 1:
            case 2:
                break;
            case 3:
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber, fb);
                break;
            case 4:
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, fb);
                break;
        }
        bandWidthEdit.setText(fb);
        msgChannel.setBandWidthEdit(bandWidthEdit.getText().toString());
        sendMsgChannel(false);
    }

    private void setBandWidth(double Hz) {
        String fb = TBookUtil.getHz3FromHz(Hz);
        int bandWidthType = Channel.BANDWIDTH_TYPE_FULL;
        switch (rgBandWidth.getSelectIndex()) {
            case 0:
                bandWidthType = Channel.BANDWIDTH_TYPE_FULL;
                break;
            case 1:
                bandWidthType = Channel.BANDWIDTH_TYPE_200M;
                break;
            case 2:
                bandWidthType = Channel.BANDWIDTH_TYPE_20M;
                break;
            case 3:
                bandWidthEdit.setEnabled(true);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber, fb);
                break;
            case 4:
                bandWidthEdit.setEnabled(true);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, fb);
                break;
        }
        bandWidthEdit.setText(fb);
        msgChannel.setBandWidthEdit(bandWidthEdit.getText().toString());
        sendMsgChannel(false);
        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
        if (channel != null) {
            channel.setBandWidthType(bandWidthType, Hz);
        }
    }

    private Observer eventObserver = new Observer() {
        @Override
        public void update(Observable observable, Object data) {

            EventBase eventBase = (EventBase)(data);
            int evId = eventBase.getId();

            // Logger.d(TAG,"evId:" + evId);
            if (evId == EventFactory.EVENT_DISPLAY_MODE) {
                if (eventBase.getData() != null) {
                    int chIdx = (int) eventBase.getData();
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;
                }
                rgVerBase.post(new Runnable() {
                    @Override
                    public void run() {
                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
                        if (Display.getInstance().isXYMode()) {
                            channel.setVerticalMode(Channel.VERTICAL_MODE_CH_ZERO);
                            if (rgVerBase.getSelectIndex() != channel.getVerticalMode()) {
                                rgVerBase.setSelectIndex(channel.getVerticalMode());
                                onItemClick(rgVerBase.getId(), rgVerBase.getSelectItem(), true);
                            }
                            rgVerBase.setEnabled(false);
                        } else {
                            rgVerBase.setEnabled(true);
                        }
                    }
                });
            }
        }
    };

    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum= RxBusRegister.parseMqEnum(obj);
        if (mqEnum==MQEnum.CH_OPEN){
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
            int chIdx = ((MsgChOpenClose)obj).getChan().getValue();
            if(chIdx == channel.getChId()) {
                updateProbeUI(channel,true);
            }
        }
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
            if(channel == null) return;
            EventBase eventBase = (EventBase) data;
            int evId = eventBase.getId();
            if (evId == EventFactory.EVENT_CHANNEL_RESISTANCETYPE) {
                if (eventBase.getData() != null) {
                    int chIdx = (int) eventBase.getData();
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;
                }
                if (rgImped.getSelectItem().getIndex() != channel.getResistanceType() && channel.getProbe() == null) {
                    rgImped.setSelectIndex(channel.getResistanceType());
                    onItemClick(rgImped.getId(), rgImped.getSelectItem(), true);
                } else {
                    updateProbeUI(channel, true);
                }
            } else if (evId == EventFactory.EVENT_CHANNEL_BANDWIDTH) {
                if (eventBase.getData() != null) {
                    int chIdx = (int) eventBase.getData();
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;
                }
                switch (channel.getBandWidthType()) {
                    case Channel.BANDWIDTH_TYPE_FULL:
                        if (rgBandWidth.getSelectIndex() != 0) {
                            rgBandWidth.setSelectIndex(0);
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);
                        }
                        break;
                    case Channel.BANDWIDTH_TYPE_200M:
                        if (rgBandWidth.getSelectIndex() != 1) {
                            rgBandWidth.setSelectIndex(1);
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);
                        }
                        break;
                    case Channel.BANDWIDTH_TYPE_20M:
                        if (rgBandWidth.getSelectIndex() != 2) {
                            rgBandWidth.setSelectIndex(2);
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);
                        }
                        break;
                    case Channel.BANDWIDTH_TYPE_HIGHPASS:
                        if (rgBandWidth.getSelectIndex() != 3) {
                            rgBandWidth.setSelectIndex(3);
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);
                        }
                        break;
                    case Channel.BANDWIDTH_TYPE_LOWPASS:
                        if (rgBandWidth.getSelectIndex() != 4) {
                            rgBandWidth.setSelectIndex(4);
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);
                        }
                        break;
                }
            } else if (((EventBase) data).getId() == EventFactory.EVENT_CHANNEL_INVERT) {
                if (eventBase.getData() != null) {
                    int chIdx = (int) eventBase.getData();
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;
                }
                if (btnInvert.isState() != channel.isInvert()) {
                    btnInvert.setState(channel.isInvert());
                    operationUI_ToggleState(btnInvert, btnInvert.isState(), true);
                }
            } else if (evId == EventFactory.EVENT_CHANNEL_COUPLE) {
                if (eventBase.getData() != null) {
                    int chIdx = (int) eventBase.getData();
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;
                }
                if (rgCouple.getSelectIndex() != unMatchCouple(channel.getCoupleType())) {
                    rgCouple.setSelectIndex(unMatchCouple(channel.getCoupleType()));
                    onItemClick(rgCouple.getId(), rgCouple.getSelectItem(), true);
                }
            }
            else if(evId == EventFactory.EVENT_CHANNEL_OFFSET ){
                if (eventBase.getData() != null) {
                    int chIdx = (int) eventBase.getData();
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;
                }
                onRefreshOffset(channel);
            }
            else if (evId == EventFactory.EVENT_CHANNEL_VSCALE) {
                if (eventBase.getData() != null) {
                    int chIdx = (int) eventBase.getData();
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;
                }
                if (channel.isOpen() && isLoadCache==false) {
                    String probeRate = TBookUtil.getXFromDouble(channel.getProbeRate());
//                Logger.d(TAG, probeRate);
                    if (!probeMultiple.getText().toString().equals(probeRate)) {
                        probeMultiple.setText(probeRate);
                        onTextClick(probeRate, true);
                        refreshProbeUI(channel);
                        preProbeMul = probeRate;
                    }
                    onRefreshOffset(channel);
                }
            } else if (evId == EventFactory.EVENT_VERTICAL_MODE) {
                if (eventBase.getData() != null) {
                    int chIdx = (int) eventBase.getData();
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;
                }
                if (rgVerBase.getSelectIndex() != channel.getVerticalMode()) {
                    rgVerBase.setSelectIndex(channel.getVerticalMode());
                    onItemClick(rgVerBase.getId(), rgVerBase.getSelectItem(), true);
                }
            } else if (evId == EventFactory.EVENT_AUTO_START) {
                if (rgVerBase.getSelectIndex() != channel.getVerticalMode()) {
                    rgVerBase.setSelectIndex(channel.getVerticalMode());
                    onItemClick(rgVerBase.getId(), rgVerBase.getSelectItem(), true);
                }
                rgVerBase.setEnabled(false);
            } else if (evId == EventFactory.EVENT_AUTO_STOP) {
                rgVerBase.setEnabled(true);
            }else if (evId == EventFactory.EVENT_PROBE_EVENT) {
                if (channel.isOpen()) {
                    int chIdx = (int) ((EventBase) data).getData();
                    if (chIdx != channel.getChId()) return;
                    updateProbeUI(channel,true);
                }
            } else if (evId == EventFactory.EVENT_PROBE_CALIBRATE_END) {
                ProbeCalibrate probeCalibrate = ProbeCalibrate.getInstance();
                int code = probeCalibrate.getErrorCode();
                switch (code) {
                    case 0:
                        dialogOk.setData(R.string.probeCalibrationSuccessful, null, null);
                        break;
                    case 1001: //超时
                        dialogOk.setData(R.string.probeAbnormal, null, null);
                        break;
                    case 705:
                        dialogOk.setData(R.string.probeSignalAbnormal, null, null);
                        break;
                    default:
                        dialogOk.setData(R.string.probeCalibrationFailed, null, null);
                        break;
                }

                if(workMode != IWorkMode.WorkMode_YT){
                    WorkModeManage.getInstance().setWorkMode(workMode, false);
                    if(workMode == IWorkMode.WorkMode_YTZOOM){
                        Scope.getInstance().setZoom(true);
                    }else if(workMode == IWorkMode.WorkMode_XY){
                        Display.getInstance().setDisplayMode(Display.DISPLAY_XY);
                    }
                }
                Scope.getInstance().setRun(true);
                unLockScreen();
            }else if (evId == EventFactory.EVENT_PROBE_ZERO){


                ProbeNotifyInfo probeNotifyInfo = (ProbeNotifyInfo) eventBase.getData();
                if (probeNotifyInfo != null && probeNotifyInfo.chIdx == (channelNumber-1)) {

                    if (probeNotifyInfo.Id != ProbeNotifyInfo.ZERO_ING) {
                        unLockScreen();
                        int code = probeNotifyInfo.Id;
                        switch (code) {
                            case ProbeNotifyInfo.ZERO_SUCCESS:
                                dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeCalibrationSuccessful), null, null);
                                break;
                            case ProbeNotifyInfo.ZERO_FAIL1:
                            case ProbeNotifyInfo.ZERO_FAIL2:
                                dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeCalibrationFailed), null, null);
                                break;
                        }
                    }else{
                        lockScreen();
                    }
                }
            }else if(evId == EventFactory.EVENT_PROBE_UPGRADE){

                ProbeUpgradeInfo probeUpgradeInfo = (ProbeUpgradeInfo) eventBase.getData();
                if(probeUpgradeInfo != null){
                    switch (probeUpgradeInfo.getState()){
                        case ProbeUpgradeInfo.UPGRADE_BEGIN:
                            if(channel.getChId() == probeUpgradeInfo.getChIdx()) {
                                dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeUpgrade), null, null);
                                lockScreen();
                            }
                            break;
                        case ProbeUpgradeInfo.UPGRADE_END:
                            if(channel.getChId() == probeUpgradeInfo.getChIdx()) {
                                if(probeUpgradeInfo.getProgress() == 100) {
                                    dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeUpgradeSuccessful), null, null);
                                }else{
                                    dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeUpgradeAbort), null, null);
                                }
                                unLockScreen();
                            }
                            break;
                    }
                }
            }else if (evId==EventFactory.EVENT_CHANNEL_OPEN){
//                EventBase eventBase = (EventBase) data;
//                int chIdx = (int)eventBase.getData();
//                if(chIdx == channel.getChId()) {
//                    updateProbeUI(channel,true);
//                }
            }else if (evId == EventFactory.EVENT_PROBE_ALARM){

                ProbeNotifyInfo probeNotifyInfo = (ProbeNotifyInfo) eventBase.getData();
                if (probeNotifyInfo != null && probeNotifyInfo.chIdx == (channelNumber -1)) {
                    int chIdx = probeNotifyInfo.chIdx;
                    String s = "";
                    switch (chIdx) {
                        case ChannelFactory.CH1:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh1);
                            break;
                        case ChannelFactory.CH2:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh2);
                            break;
                        case ChannelFactory.CH3:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh3);
                            break;
                        case ChannelFactory.CH4:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh4);
                            break;
                        case ChannelFactory.CH5:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh5);
                            break;
                        case ChannelFactory.CH6:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh6);
                            break;
                        case ChannelFactory.CH7:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh7);
                            break;
                        case ChannelFactory.CH8:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh8);
                            break;
                    }
                    int msgResId = 0;
                    switch (probeNotifyInfo.Id) {
                        case ProbeNotifyInfo.ALARM_RANGE_OUT:
                            msgResId = R.string.channelProbeAlarmMsg1RangeOut;
                            break;
                        case ProbeNotifyInfo.ALARM_BATTERY_LOW:
                            msgResId = R.string.channelProbeAlarmMsg2BatteryLow;
                            break;
                        case ProbeNotifyInfo.ALARM_COMM_ABNORMAL:
                            msgResId = R.string.channelProbeAlarmMsg3CommAbnormal;
                            break;
                        case ProbeNotifyInfo.ALARM_50_SWITCH:
                            msgResId = R.string.channelProbeAlarmMsg4FiftySwitch;
                            break;
                        case ProbeNotifyInfo.ALARM_CONV_HIGH_TEMP:
                            msgResId = R.string.channelProbeAlarmMsg5ConvHighTemp;
                            break;
                        case ProbeNotifyInfo.ALARM_COMP_HIGH_TEMP:
                            msgResId = R.string.channelProbeAlarmMsg6CompHighTemp;
                            break;
                        case ProbeNotifyInfo.ALARM_ACDC_ABNORMAL:
                            msgResId = R.string.channelProbeAlarmMsg7AcDcAbnormal;
                            break;
                        case ProbeNotifyInfo.ALARM_REPLACE_BATTERY:
                            msgResId = R.string.channelProbeAlarmMsgAreplaceBattery;
                            break;
                        case ProbeNotifyInfo.ALARM_MISMATCH:
                            msgResId = R.string.channelProbeAlarmMsgBMismatch;
                            break;
                        case ProbeNotifyInfo.ALARM_ATTENUATOR_ERROR:
                            msgResId = R.string.channelProbeAlarmMsgCAttenuator;
                            break;
                    }

                    if (msgResId != 0) {
                        dialogOk.setData(s + " " + getResources().getString(msgResId), null, null);
                    }
                }
            }
        }
    };

    private EventUIObserver OnChannelVscalUserChange=new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getData() != null) {
                int chIdx = (int) eventBase.getData();
                if (chIdx != TChan.toFpgaChNo(channelNumber)) return;
            }
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));
            if (channel == null) return;
            String s = TBookUtil.getMFromDouble(channel.getVScaleVal());
            String unit = channel.getProbeType() == VerticalAxis.PROBE_TYPE_VOL ? "V" : "A";
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT + channelNumber, s);
            s += unit;
            chFineExtent.setText(s);
            onRefreshPosition(channel);
        }
    };

    private void OnProbePlug(Observable observable, Object data) {
        int evId = ((EventBase) data).getId();
        int chIdx=(int)((EventBase) data).getData();
        if (chIdx==channelNumber-1) {
            saveParam();
        }
    }
    private void OnProbeUnPlug(Observable observable, Object data) {
        int evId = ((EventBase) data).getId();
        int chIdx=(int)((EventBase) data).getData();
        if (chIdx==channelNumber-1) {
            recoverParam();

        }
    }


    private void onRefreshOffset(Channel channel){
        double val = channel.getChOffsetVal();
        String unit = ChannelFactory.getProbeType(channel.getChId());

        String show = TBookUtil.getMFromDouble(Math.abs(val));
        if(val < 0) show = "-" + show;
        if (show.equals("")){
            show="0 "+unit;
        }else {
            show+=unit;
        }
//        Log.d("00112233","val:" + val + ",show:" + show);
        chOffset.setText(show);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_OFFSET + channelNumber, show);
    }

    private void onRefreshPosition(Channel channel){
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);
        double leftPos = Tools.getChannelPositionUI(channel.getChId());
        double val = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight())/2 - leftPos;
        String unit = ChannelFactory.getProbeType(channel.getChId());
        String show = TBookUtil.getFourFromD_Trim0(val * channel.getVerticalPerPix());
        if (show.equals("")){
            show="0 "+unit;
        }else {
            show+=unit;
        }
        if(positionString != show){
            chPosition.setText(positionString);
        }else {
            chPosition.setText(show);
        }
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_OFFSET + channelNumber, show);
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_POSITION + channelNumber, show);
    }

    private static final int HANDLE_MSG = 1;

    public static class MyHandler extends Handler {
        private final WeakReference<RightLayoutChannel> rightLayoutHandler;

        public MyHandler(RightLayoutChannel layoutChannel) {
            rightLayoutHandler = new WeakReference<RightLayoutChannel>(layoutChannel);
        }

        @Override
        public void handleMessage(Message msg) {
            if (rightLayoutHandler.get() != null) {
                if (msg.what == HANDLE_MSG) {
                    RightLayoutChannel layoutChannel = (RightLayoutChannel) rightLayoutHandler.get();
                    layoutChannel.ivBackground.setImageResource(R.drawable.svg_right_chx_button_88x174);
                }
            }
        }
    }

    private Consumer<String> consumerMouseClick = new Consumer<String>() {
        @Override
        public void accept(String clickInfo) throws Throwable {
            String[] info = clickInfo.split(";");
            int chIdx = Integer.parseInt(info[0]);
            int clickPos = Integer.parseInt(info[1]);//0垂直档位  1垂直位置  2水平挡位  3水平位置
            Logger.d(TAG, "ClickInfo chidx= " + chIdx + " ,clickPos= " + clickPos);
            if (TChan.toUiChNo(chIdx) != channelNumber) return;
            if (clickPos == 0) { //垂直档位
                fineExtentDialog(chFineExtent);
            }
            if (clickPos == 1) { //垂直位置
                setChPosition(chPosition);
            }
        }
    };

}
