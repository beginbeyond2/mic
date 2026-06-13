package com.micsig.tbook.tbookscope.main.maintop;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone;
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel;
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgChannel;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerCommon;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerEdge;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerLogic;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerNEdge;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerPulsewidth;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerRunt;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerSlope;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerTimeout;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerVideo;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.TopMsgTriggerSerials;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Data;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Label;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSdi;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSsm;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Sdi;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Ssm;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanDataId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanIdData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRdId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRemoteId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2c10WriteFrame;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame1;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame2;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cNoAckInAdr;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cRomData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinFrameId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinIdData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bCsWord;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bDataWord;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bRtAddr;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailSpiData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart0Data;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart1Data;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartxData;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.ui.MTriggerStateBar;
import com.micsig.tbook.ui.main.MainBeanTopRight;
import com.micsig.tbook.ui.top.view.scale.TopUtilScale;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * Created by yangj on 2017/5/8.
 */

public class MainTopLayoutRight extends LinearLayout {
    private static final String TAG = "MainTopLayoutRight";
    public static final int DRAWABLEID_BUSTRIGGER = R.drawable.bus_trigger;
    public static final int DRAWABLEID_CAN = R.drawable.can;
    public static final int DRAWABLEID_DPULSE = R.drawable.dpulse;
    public static final int DRAWABLEID_HOLD = R.drawable.hold;
    public static final int DRAWABLEID_LIN = R.drawable.lin;
    public static final int DRAWABLEID_M429 = R.drawable.m429;
    public static final int DRAWABLEID_M1553B = R.drawable.m1553b;
    public static final int DRAWABLEID_NTH = R.drawable.nth;
    public static final int DRAWABLEID_AND = R.drawable.logic_and;
    public static final int DRAWABLEID_OR = R.drawable.logic_or;
    public static final int DRAWABLEID_NAND = R.drawable.logic_nand;
    public static final int DRAWABLEID_NOR = R.drawable.logic_nor;
    public static final int DRAWABLEID_PULSE = R.drawable.pulse;
    public static final int DRAWABLEID_PULSE_N = R.drawable.pulse_n;
    public static final int DRAWABLEID_SLOPE = R.drawable.slope;
    public static final int DRAWABLEID_SLOPE_N = R.drawable.slope_n;
    public static final int DRAWABLEID_SLOPE_D = R.drawable.slope_d;
    public static final int DRAWABLEID_SPI = R.drawable.spi;
    public static final int DRAWABLEID_TIMEOUT = R.drawable.timeout;
    public static final int DRAWABLEID_TIMEOUT_N = R.drawable.timeout_n;
    public static final int DRAWABLEID_TIMEOUT_D = R.drawable.timeout_d;
    public static final int DRAWABLEID_TRIGGERD = R.drawable.trigger_d;
    public static final int DRAWABLEID_TRIGGERS = R.drawable.trigger_s;
    public static final int DRAWABLEID_TRIGGERX = R.drawable.trigger_x;
    public static final int DRAWABLEID_UART = R.drawable.uart;
    public static final int DRAWABLEID_VIDEOH = R.drawable.video_h;
    public static final int DRAWABLEID_VIDEOL = R.drawable.video_l;

    private Context context;
    private TextView lblTrig, an;
    private ImageView icon;
    //    private MainViewTopRight detail;
    private MTriggerStateBar detail;
    private int drawableId;
    private View parentView;
    private ArrayList<MainBeanTopRight> list;

    public MainTopLayoutRight(Context context) {
        this(context, null);
    }

    public MainTopLayoutRight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainTopLayoutRight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        drawableId = DRAWABLEID_AND;
        initView();
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerMainCache);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTopTrigger);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_CHANNEL).subscribe(consumerRightChannel);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerMainTopRightGone);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL)
//                .debounce(20, TimeUnit.MILLISECONDS)
                .subscribe(consumerTriggerLevel);

        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
    }

    private void initView() {
        parentView = View.inflate(context, R.layout.layout_maintop_right, this);
        setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        setOrientation(HORIZONTAL);
        lblTrig = (TextView) findViewById(R.id.lblTrig);
        an = (TextView) findViewById(R.id.maintopright_an);
        icon = (ImageView) findViewById(R.id.maintopright_icon);
//        detail = (MainViewTopRight) findViewById(R.id.detail);
        detail = (MTriggerStateBar) findViewById(R.id.maintopright_detail);
        detail.setOnClickListener(onClickListener);
        icon.setImageResource(drawableId);

        list = new ArrayList<MainBeanTopRight>();
        list.add(new MainBeanTopRight(TChan.Ch1, "-70.6224mV", MainBeanTopRight.LINE_TOP));
        list.add(new MainBeanTopRight(TChan.Ch2, "-10uV", MainBeanTopRight.LINE_TOP));
        list.add(new MainBeanTopRight(TChan.Ch3, "-70.64mV", MainBeanTopRight.LINE_TOP));
        list.add(new MainBeanTopRight(TChan.Ch4, "-70.64mV", MainBeanTopRight.LINE_TOP));
        list.add(new MainBeanTopRight(TChan.Ch5, "-70.64mV", MainBeanTopRight.LINE_TOP));
        list.add(new MainBeanTopRight(TChan.Ch6, "-70.64mV", MainBeanTopRight.LINE_TOP));
        list.add(new MainBeanTopRight(TChan.Ch7, "-70.64mV", MainBeanTopRight.LINE_TOP));
        list.add(new MainBeanTopRight(TChan.Ch8, "-70.64mV", MainBeanTopRight.LINE_TOP));
        list.add(new MainBeanTopRight(TChan.Ch8 + 1, "2.5V", MainBeanTopRight.LINE_TOP));
        list.add(new MainBeanTopRight("-70.6224mV", MainBeanTopRight.LINE_TOP, R.color.color_S1));
        detail.setData(list);

        this.setOnTouchListener(this::onTouchEvent);

    }

    float oldDown,oldDownY;
    boolean mScrolling;
    public Consumer<View> OnClickEvent;

    private boolean onTouchEvent(View view, MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                oldDown=event.getX();
                oldDownY=event.getY();
                mScrolling=false;
            }break;

            case MotionEvent.ACTION_UP:{
                int min= ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (Math.abs(oldDown - event.getX()) <=min  &&  Math.abs(oldDownY-event.getY())<=min ) {
                    if (OnClickEvent!=null) {
                        try {
                            OnClickEvent.accept(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    mScrolling = true;
                } else {
                    mScrolling = false;
                }

            }break;
        }
        return mScrolling;
    }

    public boolean containsRect(MotionEvent event){
        if (MotionEvent.ACTION_UP==event.getAction()){
            int x=(int)event.getRawX();
            int y=(int)event.getRawY();
            Rect r1=new Rect();

            this.getGlobalVisibleRect(r1);
            if (r1.contains(x,y) && this.getVisibility()==View.VISIBLE ){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    private Consumer<LoadCache> consumerMainCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            int mode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE);
            an.setText(mode == 0 ? "A" : "N");
        }
    };

    private Consumer<RightMsgChannel> consumerRightChannel = new Consumer<RightMsgChannel>() {
        @Override
        public void accept(RightMsgChannel rightMsgChannel) throws Exception {
//            if (rightMsgChannel.getProbeType().isRxMsgSelect()) {
            int channelNumber = rightMsgChannel.getChannelNumber();
            String unit = Tools.getChanProbeTypeUnit(channelNumber - 1) == 0 ? "V" : "A";
//                String unit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber) == 0 ? "V" : "A";

            if (TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_SERIAL1
                    || TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_SERIAL2
                    || TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_SERIAL3
                    || TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_SERIAL4
            ) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getChannel() == channelNumber && list.get(i).isVisible()
                        || list.get(i).getChannel() == 0 && list.get(i).isVisible()) {
                    if (list.get(i).getText().endsWith("A") || list.get(i).getText().endsWith("V")) {
                        list.get(i).setText(list.get(i).getText().replace("A", unit).replace("V", unit));
                    }
                }
            }
            detail.setData(list);
        }
//        }
    };

    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() {
        @Override
        public void accept(MainMsgTriggerLevel msgTriggerLevel) throws Exception {
            if (msgTriggerLevel == null || msgTriggerLevel.getCurLevel() == null) return;
            if (!msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_EDGE)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_PULSEWIDTH)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_LOGIC)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_NEDGE)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_RUNT)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_SLOPE)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_TIMEOUT)) {
                return;
            }
            switch (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER)) {
                case TopLayoutTrigger.DETAIL_EDGE:
                case TopLayoutTrigger.DETAIL_PULSEWIDTH:
                case TopLayoutTrigger.DETAIL_LOGIC:
                case TopLayoutTrigger.DETAIL_NEDGE:
                case TopLayoutTrigger.DETAIL_RUNT:
                case TopLayoutTrigger.DETAIL_SLOPE:
                case TopLayoutTrigger.DETAIL_TIMEOUT:
                    break;
                default:
                    return;
            }
            int colorResId = R.color.color_S1;
            for (MainBeanTopRight bean : list) {
                if (MainHolderTriggerLevel.LEVEL_TRIGGER_LOGIC.equals(msgTriggerLevel.getCurLevel())) {
                    if (bean.getChannel() != 0) {
                        //触发电平的逻辑
                        bean.setText(getChannelLevel(bean.getChannel(), Tools.LevelType_Normal, Tools.LevelMode_Normal));
                    }
                } else if (bean.getChannel() == msgTriggerLevel.getCurCh()) {
                    if (MainHolderTriggerLevel.LEVEL_TRIGGER_SLOPE.equals(msgTriggerLevel.getCurLevel())) {
                        //触发电平的斜率低电平
                        bean.setText(getChannelLevel(bean.getChannel(), Tools.LevelType_Normal, Tools.LevelMode_Normal));
                        bean.setLine(MainBeanTopRight.LINE_BOTTOM);
                        colorResId = bean.getColorResId();
                    } else if (MainHolderTriggerLevel.LEVEL_TRIGGER_RUNT.equals(msgTriggerLevel.getCurLevel())) {
                        //触发电平的欠幅低电平
                        bean.setText(getChannelLevel(bean.getChannel(), Tools.LevelType_Normal, Tools.LevelMode_Normal));
                        bean.setLine(MainBeanTopRight.LINE_BOTTOM);
                        colorResId = bean.getColorResId();
                    } else {
                        //触发电平的其他
                        bean.setText(getChannelLevel(bean.getChannel(), Tools.LevelType_Normal, Tools.LevelMode_Normal));
                        break;
                    }
                }
                if (bean.getChannel() == 0) {
                    if (MainHolderTriggerLevel.LEVEL_TRIGGER_SLOPE.equals(msgTriggerLevel.getCurLevel())) {
                        //触发电平的斜率高电平
                        bean.setText(getChannelLevel(msgTriggerLevel.getCurCh(), Tools.LevelType_High, Tools.LevelMode_Normal));
                        bean.setColorResId(colorResId);
                        bean.setLine(MainBeanTopRight.LINE_TOP);
                        bean.setVisible(true);
                    } else if (MainHolderTriggerLevel.LEVEL_TRIGGER_RUNT.equals(msgTriggerLevel.getCurLevel())) {
                        //触发电平的欠幅低电平
                        bean.setText(getChannelLevel(msgTriggerLevel.getCurCh(), Tools.LevelType_High, Tools.LevelMode_Normal));
                        bean.setColorResId(colorResId);
                        bean.setLine(MainBeanTopRight.LINE_TOP);
                        bean.setVisible(true);
                    } else {
                        bean.setVisible(false);
                    }
                }
            }
            detail.setData(list);
        }
    };

    /**
     * @param channel 1 - 4
     */
    private String getChannelLevel(int channel, int levelType, int levelMode) {
        return Tools.getChannelLevel(channel, levelType, levelMode);
    }

    private boolean visibleTimeBase = true;
    private boolean visibleXYMode = true;

    private Consumer<MainTopMsgRightGone> consumerMainTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone mainTopMsgRightGone) throws Exception {
            visibleTimeBase = mainTopMsgRightGone.isVisible();
            if (visibleTimeBase && visibleXYMode) {
                lblTrig.setVisibility(VISIBLE);
                an.setVisibility(View.VISIBLE);
                icon.setVisibility(View.VISIBLE);
                detail.setVisibility(View.VISIBLE);
                parentView.setVisibility(View.VISIBLE);
            } else {
                lblTrig.setVisibility(INVISIBLE);
                an.setVisibility(View.INVISIBLE);
                icon.setVisibility(View.INVISIBLE);
                detail.setVisibility(View.INVISIBLE);
                parentView.setVisibility(View.INVISIBLE);
            }
        }
    };

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            visibleXYMode = workModeBean.getNextWorkMode() != IWorkMode.WorkMode_XY;
            if (visibleTimeBase && visibleXYMode) {
                lblTrig.setVisibility(VISIBLE);
                an.setVisibility(View.VISIBLE);
                icon.setVisibility(View.VISIBLE);
                detail.setVisibility(View.VISIBLE);
                parentView.setVisibility(View.VISIBLE);
            } else {
                lblTrig.setVisibility(INVISIBLE);
                an.setVisibility(View.INVISIBLE);
                icon.setVisibility(View.INVISIBLE);
                detail.setVisibility(View.INVISIBLE);
                parentView.setVisibility(View.INVISIBLE);
            }
        }
    };

    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(RightMsgLevel msgLevel) throws Exception {
            an.setText(msgLevel.getMiddleSelect() == 0 ? "A" : "N");
        }
    };

    private Consumer<TopMsgTrigger> consumerTopTrigger = new Consumer<TopMsgTrigger>() {
        @Override
        public void accept(TopMsgTrigger topMsgTrigger) throws Exception {
            if(topMsgTrigger.getTriggerDetail() == null) return;
            for (MainBeanTopRight bean : list) {
                bean.setShowNumber(true);
            }
            switch (topMsgTrigger.getTriggerTitle().getIndex()) {
                case TopLayoutTrigger.DETAIL_COMMON:
                    TopMsgTriggerCommon common = ((TopMsgTriggerCommon) topMsgTrigger.getTriggerDetail());
                    an.setText(common.getMode().getIndex() == 0 ? "A" : "N");
                    break;
                case TopLayoutTrigger.DETAIL_EDGE:
                    TopMsgTriggerEdge edge = (TopMsgTriggerEdge) topMsgTrigger.getTriggerDetail();
                    switch (edge.getTriggerEdge().getIndex()) {
                        case 0:
                            icon.setImageResource(DRAWABLEID_TRIGGERS);
                            break;
                        case 1:
                            icon.setImageResource(DRAWABLEID_TRIGGERX);
                            break;
                        case 2:
                            icon.setImageResource(DRAWABLEID_TRIGGERD);
                            break;
                    }
                    for (MainBeanTopRight bean : list) {
                        bean.setVisible(bean.getChannel() == (edge.getTriggerSource().getIndex() + 1));
                        if (bean.getChannel() == (edge.getTriggerSource().getIndex() + 1)) {
                            bean.setLine(MainBeanTopRight.LINE_NULL);
                        }
                    }
                    detail.setData(list);
                    break;
                case TopLayoutTrigger.DETAIL_PULSEWIDTH:
                    TopMsgTriggerPulsewidth pulsewidth = (TopMsgTriggerPulsewidth) topMsgTrigger.getTriggerDetail();
                    if (pulsewidth.getPolar().getIndex() == 0) {
                        icon.setImageResource(DRAWABLEID_PULSE);
                    } else {
                        icon.setImageResource(DRAWABLEID_PULSE_N);
                    }
                    for (MainBeanTopRight bean : list) {
                        bean.setVisible(bean.getChannel() == (pulsewidth.getTriggerSource().getIndex() + 1));
                        if (bean.getChannel() == (pulsewidth.getTriggerSource().getIndex() + 1)) {
                            bean.setLine(pulsewidth.getPolar().getIndex() == 0 ? MainBeanTopRight.LINE_BOTTOM : MainBeanTopRight.LINE_TOP);
                        }
                    }
                    detail.setData(list);
                    break;
                case TopLayoutTrigger.DETAIL_LOGIC:
                    TopMsgTriggerLogic logic = (TopMsgTriggerLogic) topMsgTrigger.getTriggerDetail();
                    switch (logic.getTriggerLogic().getIndex()) {
                        case 0:
                            icon.setImageResource(DRAWABLEID_AND);
                            break;
                        case 1:
                            icon.setImageResource(DRAWABLEID_OR);
                            break;
                        case 2:
                            icon.setImageResource(DRAWABLEID_NAND);
                            break;
                        case 3:
                            icon.setImageResource(DRAWABLEID_NOR);
                            break;
                    }
                    for (MainBeanTopRight bean : list) {
                        switch (bean.getChannel()) {
                            case 0:
                                bean.setVisible(false);
                                break;
                            case TChan.Ch1:
                                bean.setVisible(logic.getCh1().getIndex() != 2);
                                bean.setLine(logic.getCh1().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM);
                                break;
                            case TChan.Ch2:
                                bean.setVisible(logic.getCh2().getIndex() != 2);
                                bean.setLine(logic.getCh2().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM);
                                break;
                            case TChan.Ch3:
                                bean.setVisible(logic.getCh3().getIndex() != 2);
                                bean.setLine(logic.getCh3().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM);
                                break;
                            case TChan.Ch4:
                                bean.setVisible(logic.getCh4().getIndex() != 2);
                                bean.setLine(logic.getCh4().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM);
                                break;
                            case TChan.Ch5:
                                bean.setVisible(logic.getCh5().getIndex() != 2);
                                bean.setLine(logic.getCh5().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM);
                                break;
                            case TChan.Ch6:
                                bean.setVisible(logic.getCh6().getIndex() != 2);
                                bean.setLine(logic.getCh6().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM);
                                break;
                            case TChan.Ch7:
                                bean.setVisible(logic.getCh7().getIndex() != 2);
                                bean.setLine(logic.getCh7().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM);
                                break;
                            case TChan.Ch8:
                                bean.setVisible(logic.getCh8().getIndex() != 2);
                                bean.setLine(logic.getCh8().getIndex() == 0 ? MainBeanTopRight.LINE_TOP : MainBeanTopRight.LINE_BOTTOM);
                                break;
                        }
                    }
                    detail.setData(list);
                    break;
                case TopLayoutTrigger.DETAIL_NEDGE:
                    TopMsgTriggerNEdge nEdge = (TopMsgTriggerNEdge) topMsgTrigger.getTriggerDetail();
                    icon.setImageResource(DRAWABLEID_NTH);
                    for (MainBeanTopRight bean : list) {
                        bean.setVisible(bean.getChannel() == (nEdge.getTriggerSource().getIndex() + 1));
                        if (bean.getChannel() == (nEdge.getTriggerSource().getIndex() + 1)) {
                            bean.setLine(MainBeanTopRight.LINE_NULL);
                        }
                    }
                    detail.setData(list);
                    break;
                case TopLayoutTrigger.DETAIL_RUNT:
                    TopMsgTriggerRunt runt = (TopMsgTriggerRunt) topMsgTrigger.getTriggerDetail();
                    icon.setImageResource(DRAWABLEID_DPULSE);
                    for (MainBeanTopRight bean : list) {
                        bean.setVisible(bean.getChannel() == (runt.getTriggerSource().getIndex() + 1));
                        if (bean.getChannel() == (runt.getTriggerSource().getIndex() + 1)) {
                            bean.setLine(MainBeanTopRight.LINE_NULL);
                        }
                    }
                    detail.setData(list);
                    break;
                case TopLayoutTrigger.DETAIL_SLOPE:
                    TopMsgTriggerSlope slope = (TopMsgTriggerSlope) topMsgTrigger.getTriggerDetail();
                    if (slope.getEdge().getIndex() == 0) {
                        icon.setImageResource(DRAWABLEID_SLOPE);
                    } else if (slope.getEdge().getIndex() == 1) {
                        icon.setImageResource(DRAWABLEID_SLOPE_N);
                    } else {
                        icon.setImageResource(DRAWABLEID_SLOPE_D);
                    }
                    for (MainBeanTopRight bean : list) {
                        bean.setVisible(bean.getChannel() == (slope.getTriggerSource().getIndex() + 1));
                        if (bean.getChannel() == (slope.getTriggerSource().getIndex() + 1)) {
                            bean.setLine(MainBeanTopRight.LINE_NULL);
                        }
                    }
                    detail.setData(list);
                    break;
                case TopLayoutTrigger.DETAIL_TIMEOUT:
                    TopMsgTriggerTimeout timeout = (TopMsgTriggerTimeout) topMsgTrigger.getTriggerDetail();
                    if (timeout.getPolar().getIndex() == 0) {
                        icon.setImageResource(DRAWABLEID_TIMEOUT);
                    } else if (timeout.getPolar().getIndex() == 1) {
                        icon.setImageResource(DRAWABLEID_TIMEOUT_N);
                    } else {
                        icon.setImageResource(DRAWABLEID_TIMEOUT_D);
                    }
                    for (MainBeanTopRight bean : list) {
                        bean.setVisible(bean.getChannel() == (timeout.getTriggerSource().getIndex() + 1));
                        if (bean.getChannel() == (timeout.getTriggerSource().getIndex() + 1)) {
                            switch (timeout.getPolar().getIndex()) {
                                case 0:
                                    bean.setLine(MainBeanTopRight.LINE_BOTTOM);
                                    break;
                                case 1:
                                    bean.setLine(MainBeanTopRight.LINE_TOP);
                                    break;
                                case 2:
                                    bean.setLine(MainBeanTopRight.LINE_NULL);
                                    break;
                            }
                        }
                    }
                    detail.setData(list);
                    break;
                case TopLayoutTrigger.DETAIL_VIDEO:
                    TopMsgTriggerVideo video = (TopMsgTriggerVideo) topMsgTrigger.getTriggerDetail();
                    if (video.getPolar().getIndex() == 0) {
                        icon.setImageResource(DRAWABLEID_VIDEOH);
                    } else {
                        icon.setImageResource(DRAWABLEID_VIDEOL);
                    }
                    for (MainBeanTopRight bean : list) {
                        bean.setVisible(bean.getChannel() == (video.getTriggerSource().getIndex() + 1));
                    }
                    for (MainBeanTopRight bean : list) {
                        if (bean.getChannel() == video.getTriggerSource().getIndex() + 1) {
                            bean.setLine(video.getPolar().getIndex() == 0 ? MainBeanTopRight.LINE_BOTTOM : MainBeanTopRight.LINE_TOP);
                            String s;
                            s = video.getStandard().getText() + " " + video.getTrigger().getText();
                            if (video.isTriggerLine()) {
                                s += " " + video.getLineDetail().getValue();
                            }
                            bean.setText(s.replaceAll("\n", ""));
                            bean.setShowNumber(false);
                            icon.setImageResource(video.getPolar().getIndex() == 0 ? DRAWABLEID_VIDEOH : DRAWABLEID_VIDEOL);
                            break;
                        }
                    }
                    detail.setData(list);
                    break;
                case TopLayoutTrigger.DETAIL_S1:
                case TopLayoutTrigger.DETAIL_S2:
                case TopLayoutTrigger.DETAIL_S3:
                case TopLayoutTrigger.DETAIL_S4:
                    TopMsgTriggerSerials s = (TopMsgTriggerSerials) topMsgTrigger.getTriggerDetail();
                    icon.setImageResource(DRAWABLEID_BUSTRIGGER);
                    String text1 = topMsgTrigger.getTriggerTitle().getText().replace(" ", ":");
                    String text2 = s.getSerials().getName();
                    String text3 = "";
                    Logger.d("MainTopLayoutRight:" + topMsgTrigger);
                    if (s.getSerialsDetail() != null) {
                        if (s.getSerialsDetail() instanceof SerialsDetailUartData) {
                            SerialsDetailUartData data = (SerialsDetailUartData) s.getSerialsDetail();
                            text3 = data.getUartDataEditTitle() + data.getUartDataCondition().getText() + data.getUartDataEdit().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailUart0Data) {
                            SerialsDetailUart0Data data = (SerialsDetailUart0Data) s.getSerialsDetail();
                            text3 = data.getUart0DataEditTitle() + data.getUart0DataCondition().getText() + data.getUart0DataEdit().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailUart1Data) {
                            SerialsDetailUart1Data data = (SerialsDetailUart1Data) s.getSerialsDetail();
                            text3 = data.getUart1DataEditTitle() + data.getUart1DataCondition().getText() + data.getUart1DataEdit().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailUartxData) {
                            SerialsDetailUartxData data = (SerialsDetailUartxData) s.getSerialsDetail();
                            text3 = data.getUartxDataEditTitle() + data.getUartxDataCondition().getText() + data.getUartxDataEdit().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailLinFrameId) {
                            SerialsDetailLinFrameId data = (SerialsDetailLinFrameId) s.getSerialsDetail();
                            text3 = data.getLinFrameIdEditEditTitle() + "=" + data.getLinFrameIdEditEdit().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailLinIdData) {
                            SerialsDetailLinIdData data = (SerialsDetailLinIdData) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getLinIdDataIdTitle() + "=" + data.getLinIdDataId().getValue()
                                    + " " + data.getLinIdDataDataTitle() + "=" + data.getLinIdDataData().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailCanRemoteId) {
                            SerialsDetailCanRemoteId data = (SerialsDetailCanRemoteId) s.getSerialsDetail();
                            text3 = data.getCanRemoteIdEditTitle() + "=" + data.getCanRemoteIdEdit().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailCanDataId) {
                            SerialsDetailCanDataId data = (SerialsDetailCanDataId) s.getSerialsDetail();
                            text3 = data.getCanDataIdEditTitle() + "=" + data.getCanDataIdEdit().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailCanRdId) {
                            SerialsDetailCanRdId data = (SerialsDetailCanRdId) s.getSerialsDetail();
                            text3 = data.getCanRdIdEditTitle() + "=" + data.getCanRdIdEdit().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailCanIdData) {
                            SerialsDetailCanIdData data = (SerialsDetailCanIdData) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getCanIdDataId().getValue() + "/" + data.getCanIdDataDlc().getValue() + "/" + data.getCanIdDataData().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailSpiData) {
                            text2 = "";
                            SerialsDetailSpiData data = (SerialsDetailSpiData) s.getSerialsDetail();
                            text3 = data.getSpiDataData().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2cNoAckInAdr) {
                            text2 = "";
                            SerialsDetailI2cNoAckInAdr data = (SerialsDetailI2cNoAckInAdr) s.getSerialsDetail();
                            text3 = data.getI2cNoAckInAdrDataTitle() + "=" + data.getI2cNoAckInAdrData().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2cFrame1) {
                            text2 = "";
                            SerialsDetailI2cFrame1 data = (SerialsDetailI2cFrame1) s.getSerialsDetail();
                            text3 = data.getI2cFrame1AddrTitle() + "=" + data.getI2cFrame1Addr().getValue()
                                    + " " + data.getI2cFrame1DataTitle() + "=" + data.getI2cFrame1Data().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2cFrame2) {
                            text2 = "";
                            SerialsDetailI2cFrame2 data = (SerialsDetailI2cFrame2) s.getSerialsDetail();
                            text3 = data.getI2cFrame2AddrTitle() + "=" + data.getI2cFrame2Addr().getValue()
                                    + " " + data.getI2cFrame2Data1Title() + "=" + data.getI2cFrame2Data1().getValue()
                                    + " " + data.getI2cFrame2Data2Title() + "=" + data.getI2cFrame2Data2().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2cRomData) {
                            text2 = "";
                            SerialsDetailI2cRomData data = (SerialsDetailI2cRomData) s.getSerialsDetail();
                            text3 = data.getI2cRomDataDataTitle() + data.getI2cRomDataCondition().getText() + data.getI2cRomDataData().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailI2c10WriteFrame) {
                            text2 = "";
                            SerialsDetailI2c10WriteFrame data = (SerialsDetailI2c10WriteFrame) s.getSerialsDetail();
                            text3 = data.getI2c10WriteFrameAddrTitle() + "=" + data.getI2c10WriteFrameAddr().getValue()
                                    + " " + data.getI2c10WriteFrameDataTitle() + "=" + data.getI2c10WriteFrameData().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429Label) {
                            SerialsDetailArinc429Label data = (SerialsDetailArinc429Label) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getArinc429LabelLabelTitle() + "=" + data.getArinc429LabelLabel().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429Sdi) {
                            SerialsDetailArinc429Sdi data = (SerialsDetailArinc429Sdi) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getArinc429SdiLabelTitle() + "=" + data.getArinc429SdiLabel().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429Data) {
                            SerialsDetailArinc429Data data = (SerialsDetailArinc429Data) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getArinc429DataDataTitle() + "=" + data.getArinc429DataData().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429Ssm) {
                            SerialsDetailArinc429Ssm data = (SerialsDetailArinc429Ssm) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getArinc429SsmLabelTitle() + "=" + data.getArinc429SsmLabel().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429LabelSdi) {
                            SerialsDetailArinc429LabelSdi data = (SerialsDetailArinc429LabelSdi) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getArinc429LabelSdiLabelTitle() + "=" + data.getArinc429LabelSdiLabel().getValue()
                                    + " " + data.getArinc429LabelSdiSdiTitle() + "=" + data.getArinc429LabelSdiSdi().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429LabelData) {
                            SerialsDetailArinc429LabelData data = (SerialsDetailArinc429LabelData) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getArinc429LabelDataLabelTitle() + "=" + data.getArinc429LabelDataLabel().getValue()
                                    + " " + data.getArinc429LabelDataDataTitle() + "=" + data.getArinc429LabelDataData().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailArinc429LabelSsm) {
                            SerialsDetailArinc429LabelSsm data = (SerialsDetailArinc429LabelSsm) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getArinc429LabelSsmLabelTitle() + "=" + data.getArinc429LabelSsmLabel().getValue()
                                    + " " + data.getArinc429LabelSsmSsmTitle() + "=" + data.getArinc429LabelSsmSsm().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailM1553bCsWord) {
                            SerialsDetailM1553bCsWord data = (SerialsDetailM1553bCsWord) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getM1553bCsWordCsWordTitle() + "=" + data.getM1553bCsWordCsWord().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailM1553bRtAddr) {
                            SerialsDetailM1553bRtAddr data = (SerialsDetailM1553bRtAddr) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getM1553bRtAddrRtAddrTitle() + "=" + data.getM1553bRtAddrRtAddr().getValue();
                        } else if (s.getSerialsDetail() instanceof SerialsDetailM1553bDataWord) {
                            SerialsDetailM1553bDataWord data = (SerialsDetailM1553bDataWord) s.getSerialsDetail();
                            text2 = "";
                            text3 = data.getM1553bDataWordDataTitle() + "=" + data.getM1553bDataWordData().getValue();
                        }
                    }
                    String text = text1
                            + (StrUtil.isEmpty(text2) ? "" : ("\n" + text2))
                            + (StrUtil.isEmpty(text3) ? "" : ("\n" + text3));
                    Logger.i(TAG, "text:" + text);
                    for (MainBeanTopRight bean : list) {
                        bean.setShowNumber(false);
                        if (bean.getChannel() == 0) {
                            int colorResourceId = R.color.color_S1;
                            int triggerIndex = topMsgTrigger.getTriggerTitle().getIndex();
                            if (triggerIndex == TopLayoutTrigger.DETAIL_S4) {
                                colorResourceId = R.color.color_S4;
                            } else if (triggerIndex == TopLayoutTrigger.DETAIL_S3) {
                                colorResourceId = R.color.color_S3;
                            } else if (triggerIndex == TopLayoutTrigger.DETAIL_S2) {
                                colorResourceId = R.color.color_S2;
                            }
                            bean.setColorResId(colorResourceId);
                            bean.setText(text);
                            bean.setLine(MainBeanTopRight.LINE_NULL);
                            bean.setVisible(true);
                        } else {
                            bean.setVisible(false);
                        }
                    }
                    detail.setData(list);
                    break;
            }
        }
    };

    private MTriggerStateBar.OnClickListener onClickListener = new MTriggerStateBar.OnClickListener() {
        @Override
        public void onClick(MTriggerStateBar view, MainBeanTopRight item) {
            DToast.get().show(item.toString());
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGER_MODE:
                    int mode = Integer.parseInt(commandMsgToUI.getParam());
                    an.setText(mode == 0 ? "A" : "N");
                    break;
            }
        }
    };

}
