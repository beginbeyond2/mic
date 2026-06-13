package com.micsig.tbook.tbookscope.main.maintop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.base.OEM;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.broadcastreceiver.BroadcastManager;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.main.maincenter.MainLeftMsgMenuRunStop;
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterMenuCommand;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChActiveChange;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleMode;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/24.
 */

public class MainTopLayoutLeft extends AbsoluteLayout {
    private static final String TAG = "MainTopLayoutLeft";
    private Context context;
    private RelativeLayout dataLayout;
    private TextView tvState, tvHigh, tvSd, tvLength, tvRate, tvSample;
    private CheckBox mainTopMeasureMenu;
    private ImageView ivLogo;
    private MainViewGroup mainViewGroup;
//
//    private ImageView tvUsbPcLink, tvUDisk, tvWifi, tvInternet;
//    private BatteryView tvBattery;
//    private TextView tvTime;
    private DialogOk dialogOk;

    public MainTopLayoutLeft(Context context) {
        this(context, null);
    }

    public MainTopLayoutLeft(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainTopLayoutLeft(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
        initControl();
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_maintop_left, this);
        dataLayout = findViewById(R.id.dataLayout);
        ivLogo = (ImageView) findViewById(R.id.logo);
        tvState = (TextView) findViewById(R.id.state);
        tvHigh = (TextView) findViewById(R.id.high);
        tvLength = (TextView) findViewById(R.id.length);
        tvRate = (TextView) findViewById(R.id.rate);
        tvSample = (TextView) findViewById(R.id.peak);

        mainTopMeasureMenu=findViewById(R.id.mainTopMeasureMenu);
//        tvUsbPcLink = (ImageView) findViewById(R.id.usbPcLink);
//        tvUDisk = (ImageView) findViewById(R.id.uDisk);
//        tvBattery = (BatteryView) findViewById(R.id.battery);
//        tvTime = (TextView) findViewById(R.id.time);
//        tvWifi = findViewById(R.id.wifi);
//        tvInternet = findViewById(R.id.lan);
//
        Bitmap bitmap = OEM.getLogo();
        if (bitmap != null) {
            ivLogo.setImageBitmap(bitmap);
        }

        ivLogo.setOnClickListener(this::onImageLogoClickEvent);
    }

    public void setMainViewGroup(MainViewGroup mainViewGroup){
        this.mainViewGroup=mainViewGroup;
    }
    private void onImageLogoClickEvent(View view) {
        MainMsgCenterMenuCommand command= new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandReturnHome);
        RxBus.getInstance().post(RxEnum.MainLeft_To_Menu_Command,command);
    }

    public void setBroadcastReceiver(MainActivity mainActivity) {
//        BroadcastManager.getInstance().init(mainActivity);
//        dialogOk = mainActivity.getMainViewGroup().findViewById(R.id.dialogOk);
//        BroadcastManager.getInstance().setBatteryControl(tvBattery, tvTime, dialogOk);
//        BroadcastManager.getInstance().setInternetControl(tvInternet);
//        BroadcastManager.getInstance().setUDiskControl(tvUDisk);
//        BroadcastManager.getInstance().setWifiControl(tvWifi);
//        BroadcastManager.getInstance().setUsbControl(tvUsbPcLink);
    }

    public void unBroadcastReceiver() {
        BroadcastManager.getInstance().unregisterReceiver();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_RUNSTOP).subscribe(consumerMainLeftMenu);
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_AUTO).subscribe(consumerMainLeftMenu_Auto);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLEMODE).subscribe(consumerTopLayoutSampleMode);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE).subscribe(consumerSegmentedState);
        RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_TO_OTHER).subscribe(consumerSlipMenuChange);
        RxBus.getInstance().getObservable(RxEnum.TOPSLIP_TITLE).subscribe(consumerTopSlipTitle);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_MEASURE).subscribe(consumerTopSlipTitle);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAVE).subscribe(consumerTopSlipTitle);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_CURSOR).subscribe(consumerTopSlipTitle);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLE).subscribe(consumerTopSlipTitle);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopSlipTitle);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTopSlipTitle);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_AUTO).subscribe(consumerTopSlipTitle);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_USERSET).subscribe(consumerTopSlipTitle);
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange);
        //RxBus.getInstance().getObservable(RxEnum.BOTTOMLAYOUT_HIGHREFRESH).subscribe(consumerBottomHighRefresh);
        EventFactory.addEventObserver(EventFactory.EVENT_UI_DEPTH_SAMPFRE_REFLASH, uiObserver);
    }



    public boolean containsRect(MotionEvent event){
        if (MotionEvent.ACTION_UP==event.getAction()){
            int x=(int)event.getRawX();
            int y=(int)event.getRawY();
            Rect r1=new Rect();

            mainTopMeasureMenu.getGlobalVisibleRect(r1);
            if (r1.contains(x,y) && mainTopMeasureMenu.getVisibility()==View.VISIBLE){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }

    }

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

    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum= RxBusRegister.parseMqEnum(obj);
        if (mqEnum!=MQEnum.CH_ACTIVE)return;

        IChan ch = ((MsgChActiveChange)obj).getChan();
        int color;
        if (ch == IChan.CH_NULL) {
            color = Color.TRANSPARENT;
        } else if (IChan.isR1ToR8(ch)) {
            color = TChan.getChannelColor(context,TChan.RefActive);
        } else {
            color = TChan.getChannelColor(context,TChan.toUiChNo(ch.getValue()));
        }

        tvLength.setTextColor(color);
        tvRate.setTextColor(color);
        tvSample.setTextColor(color);
        displayRateLength(ch.getValue()+1);
    }

    private void displayRateLength(int ch) {
        if (TChan.isSerial(ch) ) {
            tvLength.setText("");
            tvRate.setText("");
        } else {
            int len = getDepthLen();
            tvLength.setText(TBookUtil.getFourFromD((double) len));
            double dx = getSampRate();
            tvRate.setText(TBookUtil.getFourFromD(dx) + "Sa/s");
        }
    }

    private int getDepthLen() {
        IChannel channel = ChannelFactory.getInstance().getWaveChannel();
        int len = 0;
        if (channel != null) {
            int chId = channel.getChId();
            //Logger.i("current ch=ch"+(chId+1));
            if (ChannelFactory.isDynamicCh(chId))
                len = (int) Scope.getInstance().zunMemDepth();
            else if (ChannelFactory.isMathCh(chId))
                len = ((MathChannel) channel).getWaveLen();
            else
                len = ((RefChannel) channel).getWaveLen();
        }
        return len;
    }

    private double getSampRate() {
        IChannel channel = ChannelFactory.getInstance().getWaveChannel();
        if (channel != null) {
            return channel.getSampleRate2display();
        }
        return 0;
    }


    private Consumer<MainLeftMsgMenuRunStop> consumerMainLeftMenu = new Consumer<MainLeftMsgMenuRunStop>() {
        @Override
        public void accept(MainLeftMsgMenuRunStop mainLeftMsgMenuRunStop) throws Exception {

            if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.RUN) {
                if (Scope.getInstance().isAuto()) {
//                    Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.AUTO);
                    return;
                }
                Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.RUN);
                tvState.setText(getResources().getString(R.string.mainTopLeftStateRun));
                tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));
            } else if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.WAIT) {
                if (Scope.getInstance().isAuto()) {
//                    Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.AUTO);
                    return;
                }
                if (Scope.getInstance().isRun()) {
                    Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.WAIT);
                    tvState.setText(getResources().getString(R.string.mainTopLeftStateWait));
                    tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_wait));
                } else {
                    Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.STOP);
                    tvState.setText(getResources().getString(R.string.mainTopLeftStateStop));
                    tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));
                }
            } else if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.STOP) {
                Command.get().getTrigger().Status(MainLeftMsgMenuRunStop.STOP);
                tvState.setText(getResources().getString(R.string.mainTopLeftStateStop));
                tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));
            }
        }
    };

    private Consumer<Boolean> consumerMainLeftMenu_Auto = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean b) throws Exception {
            if (b) {
                tvState.setText(getResources().getString(R.string.mainTopLeftStateAuto));
                tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_auto));
            } else {
//                tvState.setText(getResources().getString(R.string.mainTopLeftStateRun));
//                tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));
                if (Scope.getInstance().isRun()) {
                    tvState.setText(getResources().getString(R.string.mainTopLeftStateRun));
                    tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));
                } else {
                    tvState.setText(getResources().getString(R.string.mainTopLeftStateStop));
                    tvState.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));
                }
            }
        }
    };

    private Consumer<TopMsgSampleMode> consumerTopLayoutSampleMode = new Consumer<TopMsgSampleMode>() {
        @Override
        public void accept(TopMsgSampleMode msgSample) throws Exception {
            switch (msgSample.getSample().getIndex()) {
                case 0:
                case 3:
                    tvSample.setText(msgSample.getSample().getText());
                    break;
                case 1:
                case 2:
                    if (!StrUtil.isEmpty(msgSample.getSample().getSimpleText())) {
                        tvSample.setText(msgSample.getDetail() + msgSample.getSample().getSimpleText());
                    } else {
                        tvSample.setText(msgSample.getDetail() + msgSample.getSample().getText());
                    }
                    break;
            }

        }
    };

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            setMainTopMeasureMenuVisible();
        }
    };
    private Consumer<TopMsgSegmentedState> consumerSegmentedState = new Consumer<TopMsgSegmentedState>() {
        @Override
        public void accept(TopMsgSegmentedState msgSegmentedState) throws Exception {
            // 串型文本打开与关闭，是通过分段存储消息过来的
            setMainTopMeasureMenuVisible();
        }
    };
    private void setCheckMeasureState(){
        boolean isOpenMainMenu=mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP);
        boolean isMeasure= mainViewGroup.getMainMenu().isShowLayoutMeasureCommon();
        mainTopMeasureMenu.setChecked( isOpenMainMenu&& isMeasure );
    }
    private Consumer<MainMsgSlip> consumerSlipMenuChange = new Consumer<MainMsgSlip>() {
        @Override
        public void accept(MainMsgSlip mainMsgSlip) throws Exception {
            setCheckMeasureState();
        }
    };
    private Consumer<Object> consumerTopSlipTitle =new Consumer<Object>() {
        @Override
        public void accept(Object obj) throws Exception {
            setCheckMeasureState();
        }
    };

    private void setMainTopMeasureMenuVisible(){
        boolean visible = WorkModeManage.getInstance().isXyMode();
        boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
        boolean result= visible || isSerialsTxt;
        mainTopMeasureMenu.setVisibility(result ? GONE : VISIBLE);
        mainTopMeasureMenu.setEnabled(!result);

    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    EventUIObserver uiObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_UI_DEPTH_SAMPFRE_REFLASH) {
                int len = getDepthLen();
                tvLength.setText(TBookUtil.getFourFromD((double) len));
                double dx = getSampRate();
                tvRate.setText(TBookUtil.getFourFromD(dx) + "Sa/s");
                displayRateLength(ChannelFactory.getChActivate() + 1);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}