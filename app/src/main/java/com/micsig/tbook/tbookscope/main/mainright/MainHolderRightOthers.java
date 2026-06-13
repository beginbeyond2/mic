package com.micsig.tbook.tbookscope.main.mainright;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.SaveRecoverySession;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;
import com.micsig.tbook.scope.horizontal.HorizontalAxisRef;
import com.micsig.tbook.scope.math.MathDualWave;
import com.micsig.tbook.scope.math.MathExprWave;
import com.micsig.tbook.scope.math.MathFFTWave;
import com.micsig.tbook.scope.math.MathWave;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgChannel;
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgVScale;
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase;
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterChannel;
import com.micsig.tbook.tbookscope.menu.MainMsgSliderZone;
import com.micsig.tbook.tbookscope.middleware.MiddleMain;
import com.micsig.tbook.tbookscope.middleware.mq.MQBase;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgChannel;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsCan;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsI2c;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsLin;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsM1553b;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsM429;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsSpi;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsUart;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.struct.ExternalKeysMsg_ToMCU;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YT;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * Created by yangj on 2017/5/12.
 */

public class MainHolderRightOthers extends RecyclerView.ViewHolder {
    private static final String TAG = "MainHolderRightOthers";

    private Context context;

    private HorizontalScrollView scrollView;
    private LinearLayout llChannelMaster;
    private MainRightLayoutItemChannelMaster math1Master, math2Master, math3Master, math4Master, math5Master, math6Master, math7Master, math8Master;

    private final List<MainRightLayoutItemChannelMaster> mathMasters = new ArrayList<>();
    private MainRightLayoutItemChannelMaster ref1Master, ref2Master, ref3Master, ref4Master, ref5Master, ref6Master, ref7Master, ref8Master;
    private final List<MainRightLayoutItemChannelMaster> refMasters = new ArrayList<>();
    private MainRightLayoutItemSerialsMaster s1Master, s2Master, s3Master, s4Master;
    private final List<MainRightLayoutItemSerialsMaster> sMasters = new ArrayList<>();
    private MainRightMsgOthers msgOthers;
    private MainRightMsgSerialsDetail msgSerialsDetail;
    private MainLayoutCenterChannel channelLayout;
    private TextView briefDisplayMath, briefDisplayRef;
    private RightMsgSerials msgSerials1 = new RightMsgSerials();
    private RightMsgSerials msgSerials2 = new RightMsgSerials();
    private RightMsgSerials msgSerials3 = new RightMsgSerials();
    private RightMsgSerials msgSerials4 = new RightMsgSerials();
    private boolean isSerialsWordShow = false;//是否显示文本页面

    public MainHolderRightOthers(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        initView(itemView);
        initData();
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_CHANNEL).subscribe(consumerExternalkeysChannel);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_CHANNEL).subscribe(consumerRightChannel);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL).subscribe(consumerTriggerLevel);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCacheEx);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerTopRightGone);
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase);
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_VSCALE).subscribe(consumerExternalKeysVScale);
        RxBus.getInstance().getObservable(RxEnum.TOP_USER_SELFADJUST).subscribe(consumerUserSelfAdjust);
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialswordVisible);
        RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_TO_OTHER).subscribe(consumerMainSlipToOther);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_MATHX).subscribe(consumerRightMathMsg);//RightLayoutMath 垂直灵敏都调整
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_REFX).subscribe(consumerRightRefMsg);//RightLayoutRef 垂直灵敏都调整
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_MASTER_LOCATION).subscribe(consumerUpdateMasterLocation);

        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_RECOVERY_SELECT).subscribe(consumerRecoverySelect);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_REF_STATE).subscribe(consumerUpdateRefState);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_TIMEBASE).subscribe(consumerUpdateTimeBase);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_USER_SET_TIMEBASE).subscribe(consumerRefTimeBase);

        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_FFT_SCALE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_SOURCE,eventUIObserver);

        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange);
    }



    private void initView(View itemView) {
        scrollView = itemView.findViewById(R.id.scroll_view);
        llChannelMaster = itemView.findViewById(R.id.ll_channel_master);
        math1Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.math1Master);
        math2Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.math2Master);
        math3Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.math3Master);
        math4Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.math4Master);
        math5Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.math5Master);
        math6Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.math6Master);
        math7Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.math7Master);
        math8Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.math8Master);
        mathMasters.add(math1Master);
        mathMasters.add(math2Master);
        mathMasters.add(math3Master);
        mathMasters.add(math4Master);
        mathMasters.add(math5Master);
        mathMasters.add(math6Master);
        mathMasters.add(math7Master);
        mathMasters.add(math8Master);

        ref1Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.ref1Master);
        ref2Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.ref2Master);
        ref3Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.ref3Master);
        ref4Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.ref4Master);
        ref5Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.ref5Master);
        ref6Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.ref6Master);
        ref7Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.ref7Master);
        ref8Master = (MainRightLayoutItemChannelMaster) itemView.findViewById(R.id.ref8Master);
        refMasters.add(ref1Master);
        refMasters.add(ref2Master);
        refMasters.add(ref3Master);
        refMasters.add(ref4Master);
        refMasters.add(ref5Master);
        refMasters.add(ref6Master);
        refMasters.add(ref7Master);
        refMasters.add(ref8Master);

        s1Master = (MainRightLayoutItemSerialsMaster) itemView.findViewById(R.id.rightS1Master);
        s2Master = (MainRightLayoutItemSerialsMaster) itemView.findViewById(R.id.rightS2Master);
        s3Master = (MainRightLayoutItemSerialsMaster) itemView.findViewById(R.id.rightS3Master);
        s4Master = (MainRightLayoutItemSerialsMaster) itemView.findViewById(R.id.rightS4Master);
        sMasters.add(s1Master);
        sMasters.add(s2Master);
        sMasters.add(s3Master);
        sMasters.add(s4Master);

        channelLayout = (MainLayoutCenterChannel) itemView.findViewById(R.id.mainLayoutCenterChannels);
        briefDisplayMath = (TextView) itemView.findViewById(R.id.briefDisplayTextMath);
        briefDisplayRef = (TextView) itemView.findViewById(R.id.briefDisplayTextRef);

        setListeners();

        setMasterVisible();//更新显示状态
//        changeViewLocation();//更新位置状态
    }

    private void setListeners() {
        mathMasters.forEach(mathMaster -> {
            mathMaster.setOnRightMasterListener(onMathRefMasterListener);
            mathMaster.setOnRightSmallListener(onMathRefSmallListener);
            mathMaster.setOnSlidCloseChannelListener(mathRefCloseListener);
        });
        refMasters.forEach(refMaster -> {
            refMaster.setOnRightMasterListener(onMathRefMasterListener);
            refMaster.setOnRightSmallListener(onMathRefSmallListener);
            refMaster.setOnSlidCloseChannelListener(mathRefCloseListener);
        });

        sMasters.forEach(serialsMaster -> {
            serialsMaster.setOnAllClickListener(onSerialsClickListener);
            serialsMaster.setOnSlidCloseChannelListener(serialsCloseListener);
        });
    }

    private void initData() {
        msgOthers = new MainRightMsgOthers();
        msgOthers.setMath1(false);
        msgOthers.setMath2(false);
        msgOthers.setMath3(false);
        msgOthers.setMath4(false);
        msgOthers.setMath5(false);
        msgOthers.setMath6(false);
        msgOthers.setMath7(false);
        msgOthers.setMath8(false);
        msgOthers.setRef1(false);
        msgOthers.setRef2(false);
        msgOthers.setRef3(false);
        msgOthers.setRef4(false);
        msgOthers.setRef5(false);
        msgOthers.setRef6(false);
        msgOthers.setRef7(false);
        msgOthers.setRef8(false);
        msgOthers.setS1(false);
        msgOthers.setS2(false);
        msgOthers.setS3(false);
        msgOthers.setS4(false);
        msgSerialsDetail = new MainRightMsgSerialsDetail();
    }

    private void setCache() {

        Log.d(TAG, "setCache() called");
        TChan.foreachRef(this::checkRefWaveExists);

        boolean math1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math1);
        boolean math2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math2);
        boolean math3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math3);
        boolean math4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math4);
        boolean math5 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math5);
        boolean math6 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math6);
        boolean math7 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math7);
        boolean math8 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math8);
        boolean m1AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math1);
        boolean m2AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math2);
        boolean m3AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math3);
        boolean m4AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math4);
        boolean m5AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math5);
        boolean m6AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math6);
        boolean m7AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math7);
        boolean m8AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math8);

        boolean s1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        boolean s1AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S1);
        boolean s2AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S2);
        boolean s3AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S3);
        boolean s4AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S4);

        boolean r1 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R1);
        boolean r2 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R2);
        boolean r3 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R3);
        boolean r4 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R4);
        boolean r5 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R5);
        boolean r6 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R6);
        boolean r7 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R7);
        boolean r8 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R8);
        boolean r1AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R1);
        boolean r2AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R2);
        boolean r3AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R3);
        boolean r4AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R4);
        boolean r5AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R5);
        boolean r6AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R6);
        boolean r7AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R7);
        boolean r8AddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R8);

        int channelsSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
        math1Master.setChecked(math1);
        math2Master.setChecked(math2);
        math3Master.setChecked(math3);
        math4Master.setChecked(math4);
        math5Master.setChecked(math5);
        math6Master.setChecked(math6);
        math7Master.setChecked(math7);
        math8Master.setChecked(math8);
        ref1Master.setChecked(r1);
        ref2Master.setChecked(r2);
        ref3Master.setChecked(r3);
        ref4Master.setChecked(r4);
        ref5Master.setChecked(r5);
        ref6Master.setChecked(r6);
        ref7Master.setChecked(r7);
        ref8Master.setChecked(r8);
        s1Master.setChecked(s1);
        s2Master.setChecked(s2);
        s3Master.setChecked(s3);
        s4Master.setChecked(s4);

//        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH, math));
//        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF, true));
//        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_S1, s1));
//        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_S2, s2));

        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S1);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S2);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S3);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S4);

        checkAndUpdateMathKeyState();
        checkAndUpdateRefKeyState();
        checkAndUpdateSerialKeyState();

        SerialBusManage.getInstance().setVisible(TChan.S1, s1 & s1AddByUser);
        SerialBusManage.getInstance().setVisible(TChan.S2, s2 & s2AddByUser);
        SerialBusManage.getInstance().setVisible(TChan.S3, s3 & s3AddByUser);
        SerialBusManage.getInstance().setVisible(TChan.S4, s4 & s4AddByUser);

//        SerialBusManage.getInstance().setVisible(TChan.S1, s1 && s1AddByUser);
//        SerialBusManage.getInstance().setVisible(TChan.S2, s2 && s2AddByUser);
//        SerialBusManage.getInstance().setVisible(TChan.S3, s3 && s3AddByUser);
//        SerialBusManage.getInstance().setVisible(TChan.S4, s4 && s4AddByUser);

        ChannelFactory.chEnable(ChannelFactory.MATH1, math1 && m1AddByUser);
        ChannelFactory.chEnable(ChannelFactory.MATH2, math2 && m2AddByUser);
        ChannelFactory.chEnable(ChannelFactory.MATH3, math3 && m3AddByUser);
        ChannelFactory.chEnable(ChannelFactory.MATH4, math4 && m4AddByUser);
        ChannelFactory.chEnable(ChannelFactory.MATH5, math5 && m5AddByUser);
        ChannelFactory.chEnable(ChannelFactory.MATH6, math6 && m6AddByUser);
        ChannelFactory.chEnable(ChannelFactory.MATH7, math7 && m7AddByUser);
        ChannelFactory.chEnable(ChannelFactory.MATH8, math8 && m8AddByUser);
        ChannelFactory.chEnable(ChannelFactory.REF1, r1 && r1AddByUser);
        ChannelFactory.chEnable(ChannelFactory.REF2, r2 && r2AddByUser);
        ChannelFactory.chEnable(ChannelFactory.REF3, r3 && r3AddByUser);
        ChannelFactory.chEnable(ChannelFactory.REF4, r4 && r4AddByUser);
        ChannelFactory.chEnable(ChannelFactory.REF5, r5 && r5AddByUser);
        ChannelFactory.chEnable(ChannelFactory.REF6, r6 && r6AddByUser);
        ChannelFactory.chEnable(ChannelFactory.REF7, r7 && r7AddByUser);
        ChannelFactory.chEnable(ChannelFactory.REF8, r8 && r8AddByUser);
        ChannelFactory.chEnable(ChannelFactory.S1, s1 && s1AddByUser);
        ChannelFactory.chEnable(ChannelFactory.S2, s2 && s2AddByUser);
        ChannelFactory.chEnable(ChannelFactory.S3, s3 && s3AddByUser);
        ChannelFactory.chEnable(ChannelFactory.S4, s4 && s4AddByUser);
        ChannelFactory.chActivate(channelsSelect);

        TChan.foreachMath(mathChan->{
            setMathWaveVScaleId(mathChan, CacheUtil.MATHTYPE_DW);
            setMathWaveVScaleId(mathChan, CacheUtil.MATHTYPE_FFT);
            setMathWaveVScaleId(mathChan, CacheUtil.MATHTYPE_AXB);
            setMathWaveVScaleId(mathChan, CacheUtil.MATHTYPE_AM);
            int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChan);
            if (mathType == CacheUtil.MATHTYPE_DW) {
                setMathWaveVScaleId(mathChan, CacheUtil.MATHTYPE_DW);
            } else if (mathType == CacheUtil.MATHTYPE_FFT) {
                setMathWaveVScaleId(mathChan, CacheUtil.MATHTYPE_FFT);
            } else if (mathType == CacheUtil.MATHTYPE_AXB) {
                setMathWaveVScaleId(mathChan, CacheUtil.MATHTYPE_AXB);
            } else if (mathType == CacheUtil.MATHTYPE_AM) {
                setMathWaveVScaleId(mathChan, CacheUtil.MATHTYPE_AM);
            }
            MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
            if(mathChannel  != null) {
                mathChannel.setProbeType(mathChannel.generateProbeType());
                if (mathType == CacheUtil.MATHTYPE_AXB || mathType == CacheUtil.MATHTYPE_AM) {
                    setMathWaveAxbUnit(mathChan, mathChannel);
                }
                setChScale(TChan.toFpgaChNo(mathChan), mathChannel.getVScaleIdVal(), mathChannel.getProbeType(), false);
            }
        });
        int chIndex = ChannelFactory.getInstance().getTopRefChannel().getChId();
        RefChannel refChannel = ChannelFactory.getRefChannel(chIndex);
        if (refChannel != null && ChannelFactory.isRefCh(chIndex)) {
            String rvScaleId = CacheUtil.get().getString(CacheUtil.MAIN_CHAN_REF_VSCALE_ID + TChan.toUiChNo(chIndex));
            int tempId = refChannel.getVScaleId();
            try {
                if (Integer.parseInt(rvScaleId) != refChannel.getVScaleId()) {
                    tempId = Integer.parseInt(rvScaleId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                refChannel.setVScaleId(tempId);
            }
            setChScale(chIndex, refChannel.getVScaleIdVal(), refChannel.getProbeType(), false);
        }

        msgOthers.setMath1(math1);
        msgOthers.setMath2(math2);
        msgOthers.setMath3(math3);
        msgOthers.setMath4(math4);
        msgOthers.setMath5(math5);
        msgOthers.setMath6(math6);
        msgOthers.setMath7(math7);
        msgOthers.setMath8(math8);
        msgOthers.setRef1(r1);
        msgOthers.setRef2(r2);
        msgOthers.setRef3(r3);
        msgOthers.setRef4(r4);
        msgOthers.setRef5(r5);
        msgOthers.setRef6(r6);
        msgOthers.setRef7(r7);
        msgOthers.setRef8(r8);
        msgOthers.setS1(s1 & s1AddByUser);
        msgOthers.setS2(s2 & s2AddByUser);
        msgOthers.setS3(s3 & s3AddByUser);
        msgOthers.setS4(s4 & s4AddByUser);
        msgOthers.setAllUnSelect();
        sendMsg();

        setEnableSlip();

        refreshSerialValue(s1Master);
        refreshSerialValue(s2Master);
        refreshSerialValue(s3Master);
        refreshSerialValue(s4Master);
        refreshMathValue();

        setMasterVisible();
    }

    private void setEnableSlip() {
        boolean isXy = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1;
        boolean isSerialTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
//        boolean isMath = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH);
//        boolean isRef = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_REF);
//        boolean isS1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
//        boolean isS2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
//        boolean isS3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
//        boolean isS4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        boolean math1Enable = !isXy && !isSerialTxt/* && isMath*/;
        boolean math2Enable = !isXy && !isSerialTxt/* && isMath*/;
        boolean math3Enable = !isXy && !isSerialTxt/* && isMath*/;
        boolean math4Enable = !isXy && !isSerialTxt/* && isMath*/;
        boolean math5Enable = !isXy && !isSerialTxt/* && isMath*/;
        boolean math6Enable = !isXy && !isSerialTxt/* && isMath*/;
        boolean math7Enable = !isXy && !isSerialTxt/* && isMath*/;
        boolean math8Enable = !isXy && !isSerialTxt/* && isMath*/;
        boolean ref1Enable = !isXy && !isSerialTxt /*&& isRef*/;
        boolean ref2Enable = !isXy && !isSerialTxt/* && isRef*/;
        boolean ref3Enable = !isXy && !isSerialTxt/* && isRef*/;
        boolean ref4Enable = !isXy && !isSerialTxt/* && isRef*/;
        boolean ref5Enable = !isXy && !isSerialTxt/* && isRef*/;
        boolean ref6Enable = !isXy && !isSerialTxt/* && isRef*/;
        boolean ref7Enable = !isXy && !isSerialTxt/* && isRef*/;
        boolean ref8Enable = !isXy && !isSerialTxt/* && isRef*/;
        boolean s1Enable = !isXy /*&& isS1*/;
        boolean s2Enable = !isXy /*&& isS2*/;
        boolean s3Enable = !isXy /*&& isS3*/;
        boolean s4Enable = !isXy /*&& isS4*/;
        MainViewGroup viewGroup = (MainViewGroup) itemView;
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_MATH1) != math1Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH1, math1Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_MATH2) != math2Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH2, math2Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_MATH3) != math3Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH3, math3Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_MATH4) != math4Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH4, math4Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_MATH5) != math5Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH5, math5Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_MATH6) != math6Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH6, math6Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_MATH7) != math7Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH7, math7Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_MATH8) != math8Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH8, math8Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_REF1) != ref1Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF1, ref1Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_REF2) != ref2Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF2, ref2Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_REF3) != ref3Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF3, ref3Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_REF4) != ref4Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF4, ref4Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_REF5) != ref5Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF5, ref5Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_REF6) != ref6Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF6, ref6Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_REF7) != ref7Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF7, ref7Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_REF8) != ref8Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF8, ref8Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_S1) != s1Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_S1, s1Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_S2) != s2Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_S2, s2Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_S3) != s3Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_S3, s3Enable));
        }
        if (viewGroup.isSlipEnable(MainViewGroup.RIGHTSLIP_S4) != s4Enable) {
            RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_S4, s4Enable));
        }
    }

    private void setBtnEnable(boolean enable) {
        math1Master.setEnabled(enable);
        math2Master.setEnabled(enable);
        math3Master.setEnabled(enable);
        math4Master.setEnabled(enable);
        math5Master.setEnabled(enable);
        math6Master.setEnabled(enable);
        math7Master.setEnabled(enable);
        math8Master.setEnabled(enable);
        ref1Master.setEnabled(enable);
        ref2Master.setEnabled(enable);
        ref3Master.setEnabled(enable);
        ref4Master.setEnabled(enable);
        ref5Master.setEnabled(enable);
        ref6Master.setEnabled(enable);
        ref7Master.setEnabled(enable);
        ref8Master.setEnabled(enable);
        s1Master.setEnabled(enable);
        s2Master.setEnabled(enable);
        s3Master.setEnabled(enable);
        s4Master.setEnabled(enable);
        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF1, enable));
        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF2, enable));
        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF3, enable));
        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF4, enable));
        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF5, enable));
        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF6, enable));
        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF7, enable));
        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_REF8, enable));
//        boolean math = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH);
//        boolean s1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
//        boolean s2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
//        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_MATH, math));
//        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_S1, s1));
//        RxBus.getInstance().post(RxEnum.MAIN_MENU_ENABLESLIP, new MainMsgSliderZone(MainMsgSliderZone.MENUSLIP_S2, s2));
    }

    private void setMathWaveAxbUnit(int mathChan, MathChannel mathChannel) {
        int mathTypeCache = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChan);
        if (mathTypeCache == CacheUtil.MATHTYPE_AXB) {
            String axbUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + mathChan);
            int axbSource = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_AXB_SOURCE + mathChan);
            mathChannel.setProbeStr(axbUnit);
        } else if (mathTypeCache == CacheUtil.MATHTYPE_AM) {
            String amUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_UNIT + mathChan);
            mathChannel.setProbeStr(amUnit);
        }
    }

    private Consumer consumerLoadCache = new Consumer() {
        @Override
        public void accept(@NonNull Object o) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_MainHolderRightOthers, true);
        }
    };

    private Consumer consumerLoadCacheEx = new Consumer() {
        @Override
        public void accept(Object o) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_MainHolderRightOthers, true);
        }
    };

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            MainViewGroup viewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
            switch (workModeBean.getNextWorkMode()) {
                case IWorkMode.WorkMode_YT:
                case IWorkMode.WorkMode_YTZOOM:
                    setBtnEnable(true);
                    if (workModeBean.getPreWorkMode() == IWorkMode.WorkMode_XY) {
                        TChan.foreachMath(mathChan -> {
                            MainRightLayoutItemChannelMaster layoutMaster = null;
                            switch (mathChan) {
                                case TChan.Math1: layoutMaster = math1Master;break;
                                case TChan.Math2: layoutMaster = math2Master;break;
                                case TChan.Math3: layoutMaster = math3Master;break;
                                case TChan.Math4: layoutMaster = math4Master;break;
                                case TChan.Math5: layoutMaster = math5Master;break;
                                case TChan.Math6: layoutMaster = math6Master;break;
                                case TChan.Math7: layoutMaster = math7Master;break;
                                case TChan.Math8: layoutMaster = math8Master;break;
                            }
                            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan);
                            boolean mathAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan);
                            if (layoutMaster != null && mathCheck && mathAddByUser) {
                                layoutMaster.setChecked(true);
                                Command.get().getMath().Display(TChan.toFpgaChNo(mathChan), true, false);
                                ChannelFactory.chEnable(TChan.toFpgaChNo(mathChan), true);
                                msgOthers.setMath(mathChan, true);
                                sendMsg();
                            }
                        });
                        checkAndUpdateMathKeyState();

                        TChan.foreachRef(refChan -> {
                            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan);
                            boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan);
                            refMasters.get(TChan.toRefNumber(refChan) - 1).setChecked(refCheck);
                            Command.get().getReference().Display(TChan.toFpgaChNo(refChan), refCheck && refAddByUser, false);
                            ChannelFactory.chEnable(TChan.toFpgaChNo(refChan), refCheck && refAddByUser);
                            msgOthers.setRef(refChan, refCheck);
                            sendMsg();
                        });
                        checkAndUpdateRefKeyState();

                        TChan.foreachSerial(serialChan -> {
                            int serialsNumber = TChan.toSerialNumber(serialChan);
                            boolean serialsCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + serialsNumber);
                            boolean serialsAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + serialsNumber);
                            sMasters.get(serialsNumber - 1).setChecked(serialsCheck);
                            if (serialsCheck && serialsAddByUser) {
                                SerialBusManage.getInstance().setVisible(serialChan, true);
                                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S1 - 1 + serialsNumber);
                                ChannelFactory.chEnable(TChan.toFpgaChNo(serialChan), true);
                                msgOthers.setSerialsDraw(serialsNumber, true);
                                sendMsg();
                            }
                        });
                        checkAndUpdateSerialKeyState();
                    }
                    break;
                case IWorkMode.WorkMode_XY:
                    TChan.foreachMath(mathChan -> {
                        int mathIndex = TChan.toMathNumber(mathChan);
                        MainRightLayoutItemChannelMaster mathMaster = mathMasters.get(mathIndex - 1);
                        if (mathMaster.isChecked()) {
                            mathMaster.setChecked(false);
                            Command.get().getMath().Display(TChan.toFpgaChNo(mathChan), false, false);
                            msgOthers.setMath(mathChan, false);
                            sendMsg();
                        }
                    });
                    RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                            new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_MATH, ExternalKeysMsg_ToMCU.STATE_LED_OFF));

                    TChan.foreachRef(refChan -> {
                        int refIndex = TChan.toRefNumber(refChan);
                        int slipIndex = MainViewGroup.RIGHTSLIP_REF1 - 1 + refIndex;
                        MainRightLayoutItemChannelMaster refMaster = refMasters.get(refIndex - 1);
                        if (refMaster.isChecked()) {
                            refMaster.setChecked(false);
                            viewGroup.hideSlip(slipIndex);
                            Command.get().getReference().Display(TChan.toFpgaChNo(refChan), false, false);
                            ChannelFactory.chEnable(TChan.toFpgaChNo(refChan), false);
                            msgOthers.setRef(refChan, false);
                            sendMsg();
                        }
                    });
                    RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                            new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_REF, ExternalKeysMsg_ToMCU.STATE_LED_OFF));


                    TChan.foreachSerial(serialsChan -> {
                        int serialsNumber = TChan.toSerialNumber(serialsChan);
                        int tageLineIndex = MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S1 - 1 + serialsNumber;
                        MainRightLayoutItemSerialsMaster serialsMaster = sMasters.get(serialsNumber - 1);
                        serialsMaster.setChecked(false);
                        SerialBusManage.getInstance().setVisible(serialsChan, false);
                        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, tageLineIndex);
                        ChannelFactory.chEnable(TChan.toFpgaChNo(serialsChan), false);
                        msgOthers.setSerials(serialsNumber, false);
                        sendMsg();
                    });
                    RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                            new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_SERIAL, ExternalKeysMsg_ToMCU.STATE_LED_OFF));

                    setBtnEnable(false);
                    break;
            }
            setEnableSlip();
        }
    };


    private void setIsOpenSerials(int chIdx, boolean isOpen) {
        if (chIdx == ChannelFactory.S1) {
            setIsOpenS1(isOpen);
        }
        if (chIdx == ChannelFactory.S2) {
            setIsOpenS2(isOpen);
        }
        if (chIdx == ChannelFactory.S3) {
            setIsOpenS3(isOpen);
        }
        if (chIdx == ChannelFactory.S4) {
            setIsOpenS4(isOpen);
        }
    }
    private void setIsOpenS1(boolean isOpen){
            s1Master.setChecked(isOpen);
            SerialBusManage.getInstance().setVisible(TChan.S1, isOpen);
            RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S1);
            ChannelFactory.chEnable(ChannelFactory.S1, isOpen);
            checkAndUpdateSerialKeyState();
            msgOthers.setS1(isOpen);
            sendMsg();
    }
    private void setIsOpenS2(boolean isOpen){
        s2Master.setChecked(isOpen);
        SerialBusManage.getInstance().setVisible(TChan.S2, isOpen);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S2);
        ChannelFactory.chEnable(ChannelFactory.S2, isOpen);
        checkAndUpdateSerialKeyState();
        msgOthers.setS2(isOpen);
        sendMsg();
    }

    private void setIsOpenS3(boolean isOpen){
        s3Master.setChecked(isOpen);
        SerialBusManage.getInstance().setVisible(TChan.S3, isOpen);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S3);
        ChannelFactory.chEnable(ChannelFactory.S3, isOpen);
        checkAndUpdateSerialKeyState();
        msgOthers.setS3(isOpen);
        sendMsg();
    }

    private void setIsOpenS4(boolean isOpen){
        s4Master.setChecked(isOpen);
        SerialBusManage.getInstance().setVisible(TChan.S4, isOpen);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S4);
        ChannelFactory.chEnable(ChannelFactory.S4, isOpen);
        checkAndUpdateSerialKeyState();
        msgOthers.setS4(isOpen);
        sendMsg();
    }

    private Consumer<MainTopMsgRightGone> consumerTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone mainTopMsgRightGone) throws Exception {
            boolean sendMsg = false;
            if (mainTopMsgRightGone.isVisible()) return;
            if (s1Master.isChecked()) {
                s1Master.setChecked(false);
                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1, String.valueOf(false));
                SerialBusManage.getInstance().setVisible(TChan.S1, false);
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S1);
                ChannelFactory.chEnable(ChannelFactory.S1, false);
//                postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S1, false);
                checkAndUpdateSerialKeyState();
                msgOthers.setS1(false);
                sendMsg = true;
            }
            if (s2Master.isChecked()) {
                s2Master.setChecked(false);
                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2, String.valueOf(false));
                SerialBusManage.getInstance().setVisible(TChan.S2, false);
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S2);
                ChannelFactory.chEnable(ChannelFactory.S2, false);
//                postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S2, false);
                checkAndUpdateSerialKeyState();
                msgOthers.setS2(false);
                sendMsg = true;
            }
            if (s3Master.isChecked()) {
                s3Master.setChecked(false);
                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3, String.valueOf(false));
                SerialBusManage.getInstance().setVisible(TChan.S3, false);
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S3);
                ChannelFactory.chEnable(ChannelFactory.S3, false);
//                postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S3, false);
                checkAndUpdateSerialKeyState();
                msgOthers.setS3(false);
                sendMsg = true;
            }
            if (s4Master.isChecked()) {
                s4Master.setChecked(false);
                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4, String.valueOf(false));
                SerialBusManage.getInstance().setVisible(TChan.S4, false);
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S4);
                ChannelFactory.chEnable(ChannelFactory.S4, false);
//                postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S4, false);
                checkAndUpdateSerialKeyState();
                msgOthers.setS4(false);
                sendMsg = true;
            }
            if (sendMsg) {
                sendMsg();
            }
        }
    };

    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() {
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception {
            if (Tools.isSlowTimeBase()) {
                boolean sendMsg = false;
                if (s1Master.isChecked() && s1Master.getVisibility() == View.VISIBLE) {
                    s1Master.setChecked(false);
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1, String.valueOf(false));
                    SerialBusManage.getInstance().setVisible(TChan.S1, false);
                    RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S1);
                    ChannelFactory.chEnable(ChannelFactory.S1, false);
//                    postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S1, false);
                    checkAndUpdateSerialKeyState();
                    msgOthers.setS1(false);
                    sendMsg = true;
                }
                if (s2Master.isChecked() && s2Master.getVisibility() == View.VISIBLE) {
                    s2Master.setChecked(false);
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2, String.valueOf(false));
                    SerialBusManage.getInstance().setVisible(TChan.S2, false);
                    RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S2);
                    ChannelFactory.chEnable(ChannelFactory.S2, false);
//                    postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S2, false);
                    checkAndUpdateSerialKeyState();
                    msgOthers.setS2(false);
                    sendMsg = true;
                }
                if (s3Master.isChecked() && s3Master.getVisibility() == View.VISIBLE) {
                    s3Master.setChecked(false);
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3, String.valueOf(false));
                    SerialBusManage.getInstance().setVisible(TChan.S3, false);
                    RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S3);
                    ChannelFactory.chEnable(ChannelFactory.S3, false);
//                    postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S3, false);
                    checkAndUpdateSerialKeyState();
                    msgOthers.setS3(false);
                    sendMsg = true;
                }
                if (s4Master.isChecked() && s4Master.getVisibility() == View.VISIBLE) {
                    s4Master.setChecked(false);
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4, String.valueOf(false));
                    SerialBusManage.getInstance().setVisible(TChan.S4, false);
                    RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S4);
                    ChannelFactory.chEnable(ChannelFactory.S4, false);
//                    postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S4, false);
                    checkAndUpdateSerialKeyState();
                    msgOthers.setS4(false);
                    sendMsg = true;
                }
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM)) {
                    s1Master.setEnabled(false);
                    s2Master.setEnabled(false);
                    s3Master.setEnabled(false);
                    s4Master.setEnabled(false);
                }
                if (sendMsg) {
                    sendMsg();
                }
            } else {
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM)) {
                    s1Master.setEnabled(true);
                    s2Master.setEnabled(true);
                    s3Master.setEnabled(true);
                    s4Master.setEnabled(true);
                }
            }
            handleRefTimeBase(msgTimeBase);
            handleMathTimeBase(msgTimeBase);
        }
    };

    private void handleMathTimeBase(MainBottomMsgTimeBase msgTimeBase) {
        String timeBase = msgTimeBase.getTimeBase();
        int refTimeBaseIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
        if (refTimeBaseIndex == 0 && !timeBase.contains("Hz")) {
            final String finalTimeBse = timeBase;
            TChan.foreachMath(mathChan -> {
                MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
                if (mathChannel != null && mathChannel.isOpen() && mathChannel.getMathType() != MathWave.MATH_FFTWAVE) {
                    List<Double> mathxAxis = HorizontalAxis.getInstance().getxAxis();
                    double scaleTime = TBookUtil.getDoubleFromM(finalTimeBse.replace("s", ""));
                    if (scaleTime > mathxAxis.get(0)) {
                        String unit = "s";
                        mathMasters.get(TChan.toMathNumber(mathChan) - 1).setTimeBase(TBookUtil.getMFromDouble(mathxAxis.get(0)) + unit);
                    } else {
                        mathMasters.get(TChan.toMathNumber(mathChan) - 1).setTimeBase(finalTimeBse);
                    }
                }
            });
        } else {
            int chActivate = ChannelFactory.getChActivate();
            boolean syncMath = false;
            if (ChannelFactory.isMathCh(chActivate)) {
                MathChannel mathChannel = ChannelFactory.getMathChannel(chActivate);
                if (mathChannel.getMathType() != MathWave.MATH_FFTWAVE) {
                    syncMath = true;
                }
            }
            if (ChannelFactory.isDynamicCh(chActivate) || syncMath) {
                final String finalTimeBse = timeBase;
                TChan.foreachMath(mathChan -> {
                    MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
                    if (mathChannel != null && mathChannel.isOpen() && mathChannel.getMathType() != MathWave.MATH_FFTWAVE) {
                        List<Double> mathxAxis = HorizontalAxis.getInstance().getxAxis();
                        double scaleTime = TBookUtil.getDoubleFromM(finalTimeBse.replace("s", ""));
                        if (scaleTime > mathxAxis.get(0)) {
                            String unit = "s";
                            mathMasters.get(TChan.toMathNumber(mathChan) - 1).setTimeBase(TBookUtil.getMFromDouble(mathxAxis.get(0)) + unit);
                        } else {
                            mathMasters.get(TChan.toMathNumber(mathChan) - 1).setTimeBase(finalTimeBse);
                        }
                    }
                });
            } else {
                if (ChannelFactory.isMathCh(ChannelFactory.getChActivate())) {
                    int mathIndex = TChan.toMathNumber(TChan.toUiChNo(ChannelFactory.getChActivate()));
                    mathMasters.get(mathIndex - 1).setTimeBase(timeBase);
                }
            }
        }
    }

    private void handleRefTimeBase(MainBottomMsgTimeBase msgTimeBase) {
        String timeBase = msgTimeBase.getTimeBase();
        int refTimeBaseIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
//        Logger.i(TAG, "msgTimeBase= " + msgTimeBase.toString());
//        if (refTimeBaseIndex == 0 && !timeBase.contains("Hz")) {
        if (refTimeBaseIndex == 0 && !timeBase.contains("Hz")) {
            final String finalTimeBse = timeBase;
            TChan.foreachRef(refChan -> {
                RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChan));
                if (refChannel != null && refChannel.isOpen() && refChannel.getRefType() != WaveData.FFT_WAVE) {
                    List<Double> refxAxis = refChannel.getHorizontalAxisRef().getxAxis();
                    if (refTimeBaseIndex == 0) {
                        refxAxis = HorizontalAxis.getInstance().getxAxis();
                    }
                    boolean isHz = finalTimeBse.contains("Hz");
                    double scaleTime = TBookUtil.getDoubleFromM(finalTimeBse.replace("s", "").replace("Hz", ""));
                    if (scaleTime > refxAxis.get(0)) {
                        String unit = "s";
                        if (isHz) {
                            unit = "Hz";
                        }
                        refMasters.get(TChan.toRefNumber(refChan) - 1).setTimeBase(TBookUtil.getMFromDouble(refxAxis.get(0)) + unit);
                    } else {
                        refMasters.get(TChan.toRefNumber(refChan) - 1).setTimeBase(finalTimeBse);
                    }
                }
            });
        } else {
            if (ChannelFactory.isRefCh(ChannelFactory.getChActivate())) {
                int refIndex = TChan.toRefNumber(TChan.toUiChNo(ChannelFactory.getChActivate()));
                refMasters.get(refIndex - 1).setTimeBase(timeBase);
            }
        }
    }

    private int serialsSpiLine1 = 1;
    private int serialsSpiLine2 = 2;
    private int serialsSpiLine3 = 3;
    private int serialsI2cLine1 = 1;
    private int serialsI2cLine2 = 2;

    private Consumer<ExternalKeysMsgChannel> consumerExternalkeysChannel = new Consumer<ExternalKeysMsgChannel>() {
        @Override
        public void accept(ExternalKeysMsgChannel msgChannel) throws Exception {
            if (Scope.getInstance().isInXYMode()) {
                return;
            }
            PlaySound.getInstance().playButton();
            int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT) + 1;
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
            boolean slowTimeBase = Tools.isSlowTimeBase();
            if (msgChannel.getChIndex() == ExternalKeysMsgChannel.MATH1) {
//                if (channelSelect != TChan.Math1 && math1Master.isChecked()) {
//
//                } else {
                    math1Master.setChecked(!math1Master.isChecked());
                    updateMathMaster(TChan.Math1, math1Master);
                    setMathScope(ChannelFactory.MATH1, math1Master.isChecked());
//                }
                msgOthers.setMath1(math1Master.isChecked());
                sendMsg();
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.MATH2) {
                if (channelSelect != TChan.Math2 && math2Master.isChecked()) {

                } else {
                    math2Master.setChecked(!math2Master.isChecked());
                    updateMathMaster(TChan.Math2, math2Master);
                    setMathScope(ChannelFactory.MATH2, math2Master.isChecked());
                }
                msgOthers.setMath2(math2Master.isChecked());
                sendMsg();
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.MATH3) {
                if (channelSelect != TChan.Math3 && math3Master.isChecked()) {

                } else {
                    math3Master.setChecked(!math3Master.isChecked());
                    updateMathMaster(TChan.Math3, math3Master);
                    setMathScope(ChannelFactory.MATH3, math3Master.isChecked());
                }
                msgOthers.setMath3(math3Master.isChecked());
                sendMsg();
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.MATH4) {
                if (channelSelect != TChan.Math4 && math4Master.isChecked()) {

                } else {
                    math4Master.setChecked(!math4Master.isChecked());
                    updateMathMaster(TChan.Math4, math4Master);
                    setMathScope(ChannelFactory.MATH4, math4Master.isChecked());
                }
                msgOthers.setMath4(math4Master.isChecked());
                sendMsg();
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.MATH5) {
                if (channelSelect != TChan.Math5 && math5Master.isChecked()) {

                } else {
                    math5Master.setChecked(!math5Master.isChecked());
                    updateMathMaster(TChan.Math5, math5Master);
                    setMathScope(ChannelFactory.MATH5, math5Master.isChecked());
                }
                msgOthers.setMath5(math5Master.isChecked());
                sendMsg();
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.MATH6) {
                if (channelSelect != TChan.Math6 && math6Master.isChecked()) {

                } else {
                    math6Master.setChecked(!math6Master.isChecked());
                    updateMathMaster(TChan.Math6, math6Master);
                    setMathScope(ChannelFactory.MATH6, math6Master.isChecked());
                }
                msgOthers.setMath6(math6Master.isChecked());
                sendMsg();
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.MATH7) {
                if (channelSelect != TChan.Math7 && math7Master.isChecked()) {

                } else {
                    math7Master.setChecked(!math7Master.isChecked());
                    updateMathMaster(TChan.Math7, math7Master);
                    setMathScope(ChannelFactory.MATH7, math7Master.isChecked());
                }
                msgOthers.setMath7(math7Master.isChecked());
                sendMsg();
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.MATH8) {
                if (channelSelect != TChan.Math8 && math8Master.isChecked()) {

                } else {
                    math8Master.setChecked(!math8Master.isChecked());
                    updateMathMaster(TChan.Math8, math8Master);
                    setMathScope(ChannelFactory.MATH8, math8Master.isChecked());
                }
                msgOthers.setMath8(math8Master.isChecked());
                sendMsg();
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.REF1) {
                boolean r1Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R1);
                if (!ref1Master.isChecked()) {
                    if (r1Check || !StrUtil.isEmpty(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R1))) {
                        ref1Master.setChecked(true);
                    } else {
                        MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) {
                            mainViewGroup.hideAllDialogSlip();
                        } else {
                            mainViewGroup.hideAllDialogSlip();
                            mainViewGroup.openSlip(MainViewGroup.RIGHTSLIP_REF1);
                        }
                        return;
                    }
                } else {
                    if ((channelSelect != TChan.R1 && ref1Master.isChecked())) {

                    } else {
                        ref1Master.setChecked(false);
                    }
                }
                updateRefMaster(TChan.R1, ref1Master);
                setRefScope(TChan.toFpgaChNo(TChan.R1), ref1Master.isChecked());
                msgOthers.setRef1(ref1Master.isChecked());
                sendMsg();
            }
            else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.REF2) {
                boolean r2Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R2);
                if (!ref2Master.isChecked()) {
                    if (r2Check || !StrUtil.isEmpty(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R2))) {
                        ref2Master.setChecked(true);
                    } else {
                        MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) {
                            mainViewGroup.hideAllDialogSlip();
                        } else {
                            mainViewGroup.hideAllDialogSlip();
                            mainViewGroup.openSlip(MainViewGroup.RIGHTSLIP_REF2);
                        }
                        return;
                    }
                } else {
                    if ((channelSelect != TChan.R2 && ref2Master.isChecked())) {

                    } else {
                        ref2Master.setChecked(false);
                    }
                }
                updateRefMaster(TChan.R2, ref2Master);
                setRefScope(TChan.toFpgaChNo(TChan.R2),ref2Master.isChecked());
                msgOthers.setRef2(ref2Master.isChecked());
                sendMsg();
            }

            else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.REF3) {
                boolean r3Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R3);
                if (!ref3Master.isChecked()) {
                    if (r3Check || !StrUtil.isEmpty(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R3))) {
                        ref3Master.setChecked(true);
                    } else {
                        MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) {
                            mainViewGroup.hideAllDialogSlip();
                        } else {
                            mainViewGroup.hideAllDialogSlip();
                            mainViewGroup.openSlip(MainViewGroup.RIGHTSLIP_REF3);
                        }
                        return;
                    }
                } else {
                    if ((channelSelect != TChan.R3 && ref3Master.isChecked())) {

                    } else {
                        ref3Master.setChecked(false);
                    }
                }
                updateRefMaster(TChan.R3, ref3Master);
                setRefScope(TChan.toFpgaChNo(TChan.R3),ref3Master.isChecked());
                msgOthers.setRef3(ref3Master.isChecked());
                sendMsg();
            }

            else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.REF4) {
                boolean r4Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R4);
                if (!ref4Master.isChecked()) {
                    if (r4Check || !StrUtil.isEmpty(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R4))) {
                        ref4Master.setChecked(true);
                    } else {
                        MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) {
                            mainViewGroup.hideAllDialogSlip();
                        } else {
                            mainViewGroup.hideAllDialogSlip();
                            mainViewGroup.openSlip(MainViewGroup.RIGHTSLIP_REF4);
                        }
                        return;
                    }
                } else {
                    if ((channelSelect != TChan.R4 && ref4Master.isChecked())) {

                    } else {
                        ref4Master.setChecked(false);
                    }
                }
                updateRefMaster(TChan.R4, ref4Master);
                setRefScope(TChan.toFpgaChNo(TChan.R4),ref4Master.isChecked());
                msgOthers.setRef4(ref4Master.isChecked());
                sendMsg();
            }

            else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.REF5) {
                boolean r5Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R5);
                if (!ref5Master.isChecked()) {
                    if (r5Check || !StrUtil.isEmpty(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R5))) {
                        ref5Master.setChecked(true);
                    } else {
                        MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) {
                            mainViewGroup.hideAllDialogSlip();
                        } else {
                            mainViewGroup.hideAllDialogSlip();
                            mainViewGroup.openSlip(MainViewGroup.RIGHTSLIP_REF5);
                        }
                        return;
                    }
                } else {
                    if ((channelSelect != TChan.R5 && ref5Master.isChecked())) {

                    } else {
                        ref5Master.setChecked(false);
                    }
                }
                updateRefMaster(TChan.R5, ref5Master);
                setRefScope(TChan.toFpgaChNo(TChan.R5),ref5Master.isChecked());
                msgOthers.setRef5(ref5Master.isChecked());
                sendMsg();
            }

            else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.REF6) {
                boolean r6Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R6);
                if (!ref6Master.isChecked()) {
                    if (r6Check || !StrUtil.isEmpty(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R6))) {
                        ref6Master.setChecked(true);
                    } else {
                        MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) {
                            mainViewGroup.hideAllDialogSlip();
                        } else {
                            mainViewGroup.hideAllDialogSlip();
                            mainViewGroup.openSlip(MainViewGroup.RIGHTSLIP_REF6);
                        }
                        return;
                    }
                } else {
                    if ((channelSelect != TChan.R6 && ref6Master.isChecked())) {

                    } else {
                        ref6Master.setChecked(false);
                    }
                }
                updateRefMaster(TChan.R6, ref6Master);
                setRefScope(TChan.toFpgaChNo(TChan.R6),ref6Master.isChecked());
                msgOthers.setRef6(ref6Master.isChecked());
                sendMsg();
            }

            else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.REF7) {
                boolean r7Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R7);
                if (!ref7Master.isChecked()) {
                    if (r7Check || !StrUtil.isEmpty(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R7))) {
                        ref7Master.setChecked(true);
                    } else {
                        MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) {
                            mainViewGroup.hideAllDialogSlip();
                        } else {
                            mainViewGroup.hideAllDialogSlip();
                            mainViewGroup.openSlip(MainViewGroup.RIGHTSLIP_REF7);
                        }
                        return;
                    }
                } else {
                    if ((channelSelect != TChan.R7 && ref7Master.isChecked())) {

                    } else {
                        ref7Master.setChecked(false);
                    }
                }
                updateRefMaster(TChan.R7, ref7Master);
                setRefScope(TChan.toFpgaChNo(TChan.R7),ref7Master.isChecked());
                msgOthers.setRef7(ref7Master.isChecked());
                sendMsg();
            }

            else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.REF8) {
                boolean r8Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R8);
                if (!ref8Master.isChecked()) {
                    if (r8Check || !StrUtil.isEmpty(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R8))) {
                        ref8Master.setChecked(true);
                    } else {
                        MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) {
                            mainViewGroup.hideAllDialogSlip();
                        } else {
                            mainViewGroup.hideAllDialogSlip();
                            mainViewGroup.openSlip(MainViewGroup.RIGHTSLIP_REF8);
                        }
                        return;
                    }
                } else {
                    if ((channelSelect != TChan.R8 && ref8Master.isChecked())) {

                    } else {
                        ref8Master.setChecked(false);
                    }
                }
                updateRefMaster(TChan.R8, ref8Master);
                setRefScope(TChan.toFpgaChNo(TChan.R8), ref8Master.isChecked());
                msgOthers.setRef8(ref8Master.isChecked());
                sendMsg();
            }
            else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.S1) {
//                if (channelSelect != TChan.S1 && s1Master.isChecked()) {
//
//                } else {
                    if (!s1Master.isChecked() && (!zoom || !slowTimeBase)) {
                        s1Master.setChecked(!s1Master.isChecked());
                        updateSerialsMaster(TChan.S1, s1Master);
                        setS1Scope(s1Master.isChecked());
                        SerialBusManage.getInstance().setVisible(TChan.S1, s1Master.isChecked());
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1, String.valueOf(s1Master.isChecked()));
                    } else if (s1Master.isChecked()) {
                        s1Master.setChecked(!s1Master.isChecked());
                        updateSerialsMaster(TChan.S1, s1Master);
                        setS1Scope(s1Master.isChecked());
                        SerialBusManage.getInstance().setVisible(TChan.S1, s1Master.isChecked());
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1, String.valueOf(s1Master.isChecked()));
                    }
//                }
                Log.d(TAG, "S1Master: "+s1Master.isChecked());
                msgOthers.setS1(s1Master.isChecked());
                sendMsg();
                sendMsgSerialsDetail(true);
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.S2) {
                if (channelSelect != TChan.S2 && s2Master.isChecked()) {

                } else {
                    if (!s2Master.isChecked() && (!zoom || !slowTimeBase)) {
                        s2Master.setChecked(!s2Master.isChecked());
                        updateSerialsMaster(TChan.S2, s2Master);
                        setS2Scope(s2Master.isChecked());
                        SerialBusManage.getInstance().setVisible(TChan.S2, s2Master.isChecked());
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2, String.valueOf(s2Master.isChecked()));
                    } else if (s2Master.isChecked()) {
                        s2Master.setChecked(!s2Master.isChecked());
                        updateSerialsMaster(TChan.S2, s2Master);
                        setS2Scope(s2Master.isChecked());
                        SerialBusManage.getInstance().setVisible(TChan.S2, s2Master.isChecked());
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2, String.valueOf(s2Master.isChecked()));
                    }
                }
                msgOthers.setS2(s2Master.isChecked());
                sendMsg();
                sendMsgSerialsDetail(true);
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.S3) {
                if (channelSelect != TChan.S3 && s3Master.isChecked()) {

                } else {
                    if (!s3Master.isChecked() && (!zoom || !slowTimeBase)) {
                        s3Master.setChecked(!s3Master.isChecked());
                        updateSerialsMaster(TChan.S3, s3Master);
                        setS3Scope(s3Master.isChecked());
                        SerialBusManage.getInstance().setVisible(TChan.S3, s3Master.isChecked());
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3, String.valueOf(s3Master.isChecked()));
                    } else if (s3Master.isChecked()) {
                        s3Master.setChecked(!s3Master.isChecked());
                        updateSerialsMaster(TChan.S3, s3Master);
                        setS3Scope(s3Master.isChecked());
                        SerialBusManage.getInstance().setVisible(TChan.S3, s3Master.isChecked());
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3, String.valueOf(s3Master.isChecked()));
                    }
                }
                msgOthers.setS3(s3Master.isChecked());
                sendMsg();
                sendMsgSerialsDetail(true);
            } else if (msgChannel.getChIndex() == ExternalKeysMsgChannel.S4) {
                if (channelSelect != TChan.S4 && s4Master.isChecked()) {

                } else {
                    if (!s4Master.isChecked() && (!zoom || !slowTimeBase)) {
                        s4Master.setChecked(!s4Master.isChecked());
                        updateSerialsMaster(TChan.S4, s4Master);
                        setS4Scope(s4Master.isChecked());
                        SerialBusManage.getInstance().setVisible(TChan.S4, s4Master.isChecked());
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4, String.valueOf(s4Master.isChecked()));
                    } else if (s4Master.isChecked()) {
                        s4Master.setChecked(!s4Master.isChecked());
                        updateSerialsMaster(TChan.S4, s4Master);
                        setS4Scope(s4Master.isChecked());
                        SerialBusManage.getInstance().setVisible(TChan.S4, s4Master.isChecked());
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4, String.valueOf(s4Master.isChecked()));
                    }
                }
                msgOthers.setS4(s4Master.isChecked());
                sendMsg();
                sendMsgSerialsDetail(true);
            }

        }
    };

    private void updateMathMaster(int mathChan, MainRightLayoutItemChannelMaster mathMaster) {
        if (mathMaster.isChecked()) {
            boolean mathAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan);
            if (!mathAddByUser) {//之前没添加过Math信号
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan, String.valueOf(true));
                try {
//                    consumerUpdateMasterLocation.accept(mathChan);
//                    setMasterVisible();
                    setMathMasterVisible(mathChan, false);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateRefMaster(int refChan, MainRightLayoutItemChannelMaster refMaster) {
        if (refMaster.isChecked()) {
            boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan);
            if (!refAddByUser) {//之前没添加过Math信号
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan, String.valueOf(true));
                try {
//                    consumerUpdateMasterLocation.accept(refChan);
//                    setMasterVisible();
                    setRefMasterVisible(refChan, false);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateSerialsMaster(int serialsChan, MainRightLayoutItemSerialsMaster serialsMaster) {
        if (serialsMaster.isChecked()) {
            boolean serialsAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(serialsChan));
            if (!serialsAddByUser) {//之前没添加过Math信号
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(serialsChan), String.valueOf(true));
                try {
//                    consumerUpdateMasterLocation.accept(serialsChan);
//                    setMasterVisible();
                    setSerialsMasterVisible(serialsChan, false);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private Consumer<RightMsgChannel> consumerRightChannel = new Consumer<RightMsgChannel>() {
        @Override
        public void accept(RightMsgChannel rightMsgChannel) throws Throwable {
            if (rightMsgChannel.getProbeType().isRxMsgSelect()) {//当前是修改的探针类型
                TChan.foreachMath(mathChan -> {
                    MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
                    if (mathChannel != null) {
                        setMathWaveVScaleId(mathChan, mathChannel.getMathType());
                        if (mathChannel.getMathType() == MathWave.MATH_EXPR) {
                            setMathWaveAxbUnit(mathChan, mathChannel);
                        }
                        mathChannel.setProbeType(mathChannel.generateProbeType());
                        setChScale(TChan.toFpgaChNo(mathChan), mathChannel.getVScaleIdVal(), mathChannel.getProbeType(), false);
                    }
                });

                msgSerials1.setOpenLevel(false);
                msgSerials1.setFromEventBus(rightMsgChannel.isFromEventBus());
                msgSerials2.setOpenLevel(false);
                msgSerials2.setFromEventBus(rightMsgChannel.isFromEventBus());
                msgSerials3.setOpenLevel(false);
                msgSerials3.setFromEventBus(rightMsgChannel.isFromEventBus());
                msgSerials4.setOpenLevel(false);
                msgSerials4.setFromEventBus(rightMsgChannel.isFromEventBus());
                consumerRightSerials.accept(msgSerials1);
                consumerRightSerials.accept(msgSerials2);
                consumerRightSerials.accept(msgSerials3);
                consumerRightSerials.accept(msgSerials4);
            }
            refreshSerialValue(s1Master);
            refreshSerialValue(s2Master);
            refreshSerialValue(s3Master);
            refreshSerialValue(s4Master);
            refreshMathValue();
        }
    };

    private Consumer<RightMsgMath> consumerRightMath = new Consumer<RightMsgMath>() {
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception {
            int mathChannelNumber = rightMsgMath.getMathChannelNumber();
            MainRightLayoutItemChannelMaster layoutMaster = null;
            switch (mathChannelNumber) {
                case TChan.Math1: layoutMaster = math1Master;break;
                case TChan.Math2: layoutMaster = math2Master;break;
                case TChan.Math3: layoutMaster = math3Master;break;
                case TChan.Math4: layoutMaster = math4Master;break;
                case TChan.Math5: layoutMaster = math5Master;break;
                case TChan.Math6: layoutMaster = math6Master;break;
                case TChan.Math7: layoutMaster = math7Master;break;
                case TChan.Math8: layoutMaster = math8Master;break;
            }
            if (layoutMaster != null) {
                boolean change = layoutMaster.isChecked() != rightMsgMath.getMathCheck().isValue();
                int chSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
                if (!change) {
                    change = chSelect != TChan.toFpgaChNo(mathChannelNumber);
                }
                layoutMaster.setChecked(rightMsgMath.getMathCheck().isValue());
                MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber));
                if (mathChannel == null) return;
                setMathScope(TChan.toFpgaChNo(mathChannelNumber), layoutMaster.isChecked());
                mathChannel.setProbeType(mathChannel.generateProbeType());

                if (rightMsgMath.getMathType().getValue() == CacheUtil.MATHTYPE_AXB
                        || rightMsgMath.getMathType().getValue() == CacheUtil.MATHTYPE_AM) {
                    setMathWaveAxbUnit(mathChannelNumber, mathChannel);
                }
                setMathWaveVScaleId(mathChannelNumber, rightMsgMath.getMathType().getValue());
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber);
                if (mathType == CacheUtil.MATHTYPE_FFT) {
                    if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathChannelNumber) != 1) {
                        //line fft
                        double coff = ChannelFactory.getDynamicChannel(mathChannel.getMathFFTWave().getSource()).getProbeRate();
                        double extent = mathChannel.getVScaleIdVal() * coff;
                        setChScale(TChan.toFpgaChNo(mathChannelNumber), extent, mathChannel.getProbeType(), false);
                    } else {
                        setChScale(TChan.toFpgaChNo(mathChannelNumber), mathChannel.getVScaleIdVal(), mathChannel.getProbeType(), false);
                    }
                }
                setRightMasterSmall(mathChannelNumber, layoutMaster.isChecked());
                if (change) {
                    msgOthers.setMath(mathChannelNumber, rightMsgMath.getMathCheck().isValue());
                    sendMsg();
                }
            }
            setMathMasterVisible(mathChannelNumber, false);
//            setMasterVisible();
            refreshMathValue();
        }
    };

    private void setMathScope(int mathIndex, boolean check) {
        boolean mathAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.toUiChNo(mathIndex));
        Command.get().getMath().Display(mathIndex, check && mathAddByUser, false);
        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.toUiChNo(mathIndex), String.valueOf(check));
        ChannelFactory.chEnable(mathIndex, check && mathAddByUser);
//        postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_MATH + TChan.toUiChNo(mathIndex), check);
        checkAndUpdateMathKeyState();
    }

    private void setRefScope(int refChannelNumber, boolean check) {
        if (!TChan.isRef(TChan.toUiChNo(refChannelNumber))) return;
        boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.toUiChNo(refChannelNumber));
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.toUiChNo(refChannelNumber), String.valueOf(check));
        Command.get().getReference().Display(refChannelNumber, check && refAddByUser, false);
        ChannelFactory.chEnable(refChannelNumber, check && refAddByUser);
//        postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_REF, check);
        checkAndUpdateRefKeyState();
    }

    private void setS1Scope(boolean check) {
        boolean serialAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S1);
        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1, String.valueOf(check));
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S1);
        ChannelFactory.chEnable(ChannelFactory.S1, check && serialAddByUser);
//        postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S1, check);
        checkAndUpdateSerialKeyState();
        if(check && serialAddByUser) {
            updateSelectChan(TChan.S1);
        }
    }

    private void setS2Scope(boolean check) {
        boolean serialAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S2);
        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2, String.valueOf(check));
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S2);
        ChannelFactory.chEnable(ChannelFactory.S2, check && serialAddByUser);
//        postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S2, check);
        checkAndUpdateSerialKeyState();
        if(check && serialAddByUser) {
            updateSelectChan(TChan.S2);
        }
    }

    private void setS3Scope(boolean check) {
        boolean serialAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S3);
        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3, String.valueOf(check));
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S3);
        ChannelFactory.chEnable(ChannelFactory.S3, check && serialAddByUser);
//        postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S3, check);
        checkAndUpdateSerialKeyState();
        if(check && serialAddByUser) {
            updateSelectChan(TChan.S3);
        }
    }

    private void setS4Scope(boolean check) {
        boolean serialAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + CacheUtil.S4);
        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4, String.valueOf(check));
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_DISCREETVOLTAGELINE_S4);
        ChannelFactory.chEnable(ChannelFactory.S4, check && serialAddByUser);
//        postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_S4, check);
        checkAndUpdateSerialKeyState();
        if(check && serialAddByUser) {
            updateSelectChan(TChan.S4);
        }
    }

    private void setMathWaveVScaleId(int machChan, int mathType) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(machChan));
        if (mathChannel == null) return;
        if (mathType == CacheUtil.MATHTYPE_AXB) {
            MathExprWave exprWave = mathChannel.getMathExprWave();
            exprWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_AXB_VSCALE_ID + machChan));
//            Logger.d(TAG, "setMathWaveVScaleId() called with: mathType = [" + mathType + "],VScaleId:" + exprWave.getVScaleId());
        } else if (mathType == CacheUtil.MATHTYPE_DW) {
            MathDualWave dualWave = mathChannel.getMathDualWave();
            dualWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_DW_VSCALE_ID + machChan));
//            Logger.d(TAG, "setMathWaveVScaleId() called with: mathType = [" + mathType + "],VScaleId:" + dualWave.getVScaleId());
        } else if (mathType == CacheUtil.MATHTYPE_AM) {
            MathExprWave exprWave = mathChannel.getMathExprWave();
            exprWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_AM_VSCALE_ID + machChan));
//            Logger.d(TAG, "setMathWaveVScaleId() called with: mathType = [" + mathType + "],VScaleId:" + exprWave.getVScaleId());
        } else {
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + machChan) == 1) {
                MathFFTWave mathFFTWave = mathChannel.getMathFFTWave();
                mathFFTWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + machChan));
//                Logger.d(TAG, "setMathWaveVScaleId() called with: mathType = [" + mathType + "],VScaleId:" + mathFFTWave.getVScaleId());
            } else {
                MathFFTWave mathFFTWave = mathChannel.getMathFFTWave();
                mathFFTWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + machChan));
//                Logger.d(TAG, "setMathWaveVScaleId() called with: mathType = [" + mathType + "],VScaleId:" + mathFFTWave.getVScaleId());
            }
        }
//        WaveManage.get().setMathOffset(machChan);
    }

    private void setCacheVScaleId(int vScaleId, int mathChan) {
        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChan);
        if (mathType == CacheUtil.MATHTYPE_AXB) {
            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH_AXB_VSCALE_ID + mathChan, String.valueOf(vScaleId));
        } else if (mathType == CacheUtil.MATHTYPE_DW) {
            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH_DW_VSCALE_ID + mathChan, String.valueOf(vScaleId));
        } else if (mathType == CacheUtil.MATHTYPE_AM) {
            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH_AM_VSCALE_ID + mathChan, String.valueOf(vScaleId));
        } else {
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathChan) == 1) {
                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + mathChan, String.valueOf(vScaleId));
            } else {
                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + mathChan, String.valueOf(vScaleId));
            }
        }
    }

    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() {
        @Override
        public void accept(RightMsgRefForEight rightMsgRef) throws Exception {
            int refChannelNumber = rightMsgRef.getRefChannelNumber();
            MainRightLayoutItemChannelMaster refMaster = null;
            switch (refChannelNumber) {
                case TChan.R1: refMaster = ref1Master;break;
                case TChan.R2: refMaster = ref2Master;break;
                case TChan.R3: refMaster = ref3Master;break;
                case TChan.R4: refMaster = ref4Master;break;
                case TChan.R5: refMaster = ref5Master;break;
                case TChan.R6: refMaster = ref6Master;break;
                case TChan.R7: refMaster = ref7Master;break;
                case TChan.R8: refMaster = ref8Master;break;
            }
            if (refMaster != null) {
                if (rightMsgRef.getRefChecked().isValue() != refMaster.isChecked()) {
                    refMaster.setChecked(rightMsgRef.getRefChecked().isValue());
                }
                if (rightMsgRef.getRefChecked().isValue()) {
                    refMaster.setChecked(true);
                    msgOthers.setRef(refChannelNumber, true);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(true));
                    checkAndUpdateRefKeyState();
                    RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber));
                    if (refChannel == null) return;
                    HorizontalAxisRef horizontalAxisRef = refChannel.getHorizontalAxisRef();
                    String tail = "s";
                    if (horizontalAxisRef.getRefType() == HorizontalAxisRef.REFTYPE_MATHFFT) {
                        tail = "Hz";
                    }
                    String s = TBookUtil.getMFromDouble(refChannel.getRefTimeScaleVal());
                    setChScale(TChan.toFpgaChNo(refChannelNumber), refChannel.getVScaleIdVal(), refChannel.getProbeType(), false);
                } else {
                    refMaster.setChecked(false);
                    msgOthers.setRef(refChannelNumber, false);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(false));
                    RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                            new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_REF + refChannelNumber, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
                }
                setRefScope(TChan.toFpgaChNo(refChannelNumber), refMaster.isChecked());
                msgOthers.setRef(refChannelNumber, refMaster.isChecked());
                sendMsg();
//                setMasterVisible();
                setRefMasterVisible(refChannelNumber, false);
            }
        }
    };

    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() {
        @Override
        public void accept(RightMsgSerials msgSerials) throws Exception {
            MainRightLayoutItemSerialsMaster layout = null;
            if (msgSerials.getSerialsNumber() == RightMsgSerials.SERIALS_S1) {
                msgSerials1 = msgSerials;
                layout = s1Master;
                s1Master.setName("S1");
                s1Master.setSerialsType(msgSerials.getSerialsType().getText().toUpperCase());
                s1Master.setSerialsType(msgSerials.getSerialsType().getIndex());
                CacheUtil.get().setValueLevelSerials(CacheUtil.S1);
                if (msgSerials.getSerials1Check() != null && s1Master.isChecked() != msgSerials.getSerials1Check().isValue()) {
                    s1Master.setChecked(msgSerials.getSerials1Check().isValue());
                    setS1Scope(s1Master.isChecked());
                    msgOthers.setS1(s1Master.isChecked());
                    sendMsg();
                }
            } else if (msgSerials.getSerialsNumber() == RightMsgSerials.SERIALS_S2) {
                msgSerials2 = msgSerials;
                layout = s2Master;
                s2Master.setName("S2");
                s2Master.setSerialsType(msgSerials.getSerialsType().getText().toUpperCase());
                s2Master.setSerialsType(msgSerials.getSerialsType().getIndex());
                CacheUtil.get().setValueLevelSerials(CacheUtil.S2);
                if (msgSerials.getSerials2Check() != null && s2Master.isChecked() != msgSerials.getSerials2Check().isValue()) {
                    s2Master.setChecked(msgSerials.getSerials2Check().isValue());
                    setS2Scope(s2Master.isChecked());
                    msgOthers.setS2(s2Master.isChecked());
                    sendMsg();
                }
            } else if (msgSerials.getSerialsNumber() == RightMsgSerials.SERIALS_S3) {
                msgSerials3 = msgSerials;
                layout = s3Master;
                s3Master.setName("S3");
                s3Master.setSerialsType(msgSerials.getSerialsType().getText().toUpperCase());
                s3Master.setSerialsType(msgSerials.getSerialsType().getIndex());
                CacheUtil.get().setValueLevelSerials(CacheUtil.S3);
                if (msgSerials.getSerials3Check() != null && s3Master.isChecked() != msgSerials.getSerials3Check().isValue()) {
                    s3Master.setChecked(msgSerials.getSerials3Check().isValue());
                    setS3Scope(s3Master.isChecked());
                    msgOthers.setS3(s3Master.isChecked());
                    sendMsg();
                }
            } else if (msgSerials.getSerialsNumber() == RightMsgSerials.SERIALS_S4) {
                msgSerials4 = msgSerials;
                layout = s4Master;
                s4Master.setName("S4");
                s4Master.setSerialsType(msgSerials.getSerialsType().getText().toUpperCase());
                s4Master.setSerialsType(msgSerials.getSerialsType().getIndex());
                CacheUtil.get().setValueLevelSerials(CacheUtil.S4);
                if (msgSerials.getSerials4Check() != null && s4Master.isChecked() != msgSerials.getSerials4Check().isValue()) {
                    s4Master.setChecked(msgSerials.getSerials4Check().isValue());
                    setS4Scope(s4Master.isChecked());
                    msgOthers.setS4(s4Master.isChecked());
                    sendMsg();
                }
            }
            if (msgSerials.getSerialsType() == null) return;
            int serialId = CacheUtil.get().getValueLevelSerials();
            CacheUtil.get().setValueLevelSerials(msgSerials.getSerialsNumber());
            switch (msgSerials.getSerialsType().getIndex()) {
                case RightLayoutSerials.SERIALS_UART: {
                    RightMsgSerialsUart serialsUart = (RightMsgSerialsUart) msgSerials.getSerialsDetails();
                    int ch = serialsUart.getRx().getIndex() + 1;
                    int color = TChan.getChannelColor(context,ch);
                    layout.setCommonCh(ch);
                    layout.setSerialsText(Tools.getChannelLevel(ch, Tools.LevelType_Normal, Tools.LevelMode_Bus),
                            null,//serialsUart.getCheck().getText(),
                            null,//serialsUart.getBits().getText(),
                            null,//serialsUart.getBaudRate().getValue(),
                            color, color, color, color,
                            serialsUart.getIdleLevel().getIndex() == 1, false, false, false);
                    Command.get().getBus_uart().Rx(msgSerials.getSerialsNumber()-1,ch-1,false);
                    break;
                }
                case RightLayoutSerials.SERIALS_LIN: {
                    RightMsgSerialsLin serialsLin = (RightMsgSerialsLin) msgSerials.getSerialsDetails();
                    int ch = serialsLin.getSource().getIndex() + 1;
                    int color = TChan.getChannelColor(context,ch);
                    layout.setCommonCh(ch);
                    layout.setSerialsText(Tools.getChannelLevel(ch, Tools.LevelType_Normal, Tools.LevelMode_Bus),
                            null,//serialsLin.getBaudRate().getText(),
                            null, null, color, color, color, color,
                            serialsLin.getIdleLevel().getIndex() == 1, false, false, false);
                    Command.get().getBus_lin().Channel(msgSerials.getSerialsNumber()-1,ch-1,false);
                    break;
                }
                case RightLayoutSerials.SERIALS_CAN: {
                    RightMsgSerialsCan serialsCan = (RightMsgSerialsCan) msgSerials.getSerialsDetails();
                    int ch = serialsCan.getSource().getIndex() + 1;
                    int color = TChan.getChannelColor(context,ch);
                    layout.setCommonCh(ch);
                    layout.setSerialsText(Tools.getChannelLevel(ch, Tools.LevelType_Normal, Tools.LevelMode_Bus),
                            null,//serialsCan.getSignal().getText(),
                            null,//serialsCan.getBaudRate().getText(),
                            null, color, color, color, color, false, false, false, false);

                    Command.get().getBus_can().Channel(msgSerials.getSerialsNumber()-1,ch-1,false);
                    break;
                }
                case RightLayoutSerials.SERIALS_SPI: {
                    RightMsgSerialsSpi serialsSpi = (RightMsgSerialsSpi) msgSerials.getSerialsDetails();
                    serialsSpiLine1 = serialsSpi.getClk().getIndex() + 1;
                    serialsSpiLine2 = serialsSpi.getData().getIndex() + 1;
                    serialsSpiLine3 = serialsSpi.getCs().getIndex() + 1;
                    int color1 = TChan.getChannelColor(context,serialsSpiLine1);
                    int color2 = TChan.getChannelColor(context,serialsSpiLine2);
                    int color3 = TChan.getChannelColor(context,serialsSpiLine3);
                    layout.setSpiCh(serialsSpiLine1, serialsSpiLine2, serialsSpiLine3);
                    int channelCount = GlobalVar.get().getChannelsCount();
                    layout.setSerialsText(
                            Tools.getChannelLevel(serialsSpiLine1, Tools.LevelType_Normal, Tools.LevelMode_Bus),
                            Tools.getChannelLevel(serialsSpiLine2, Tools.LevelType_Normal, Tools.LevelMode_Bus),
                            (channelCount == GlobalVar.CHANNEL_COUNT_4 || channelCount == GlobalVar.CHANNEL_COUNT_8)
                                    && serialsSpi.getCsSwitch().isValue() ? Tools.getChannelLevel(serialsSpiLine3,
                                    Tools.LevelType_Normal, Tools.LevelMode_Bus) : null,
                            null,//serialsSpi.getBit().getText(),
                            color1, color2, color3, Color.WHITE,
                            serialsSpi.getClkLow().isValue(),
                            serialsSpi.getDataLow().isValue(),
                            serialsSpi.getCsLow().isValue(),
                            false);
                    Command.get().getBus_spi().setClock(msgSerials.getSerialsNumber()-1,serialsSpiLine1-1,false);
                    Command.get().getBus_spi().setData(msgSerials.getSerialsNumber()-1,serialsSpiLine2-1,false);
                    Command.get().getBus_spi().setCs(msgSerials.getSerialsNumber()-1,serialsSpiLine3-1,false);
                    break;
                }
                case RightLayoutSerials.SERIALS_I2C: {
                    RightMsgSerialsI2c serialsI2c = (RightMsgSerialsI2c) msgSerials.getSerialsDetails();
                    serialsI2cLine1 = serialsI2c.getSda().getIndex() + 1;
                    serialsI2cLine2 = serialsI2c.getScl().getIndex() + 1;
                    int color1 = TChan.getChannelColor(context,serialsI2cLine1);
                    int color2 = TChan.getChannelColor(context,serialsI2cLine2);
                    layout.setI2cCh(serialsI2cLine1, serialsI2cLine2);
                    layout.setSerialsText(
                            Tools.getChannelLevel(serialsI2cLine1, Tools.LevelType_Normal, Tools.LevelMode_Bus),
                            Tools.getChannelLevel(serialsI2cLine2, Tools.LevelType_Normal, Tools.LevelMode_Bus),
                            null, null, color1, color2, color1, color1, false, false, false, false);


                    Command.get().getBus_iic().SDA(msgSerials.getSerialsNumber()-1,serialsI2cLine1-1,false);
                    Command.get().getBus_iic().SCL(msgSerials.getSerialsNumber()-1,serialsI2cLine2-1,false);
//                    int sda= Command.get().getBus_iic().SDAQ(msgSerials.getSerialsNumber()-1);
//                    int scl=Command.get().getBus_iic().SCLQ(msgSerials.getSerialsNumber()-1);
//                    Logger.i(Command.TAG,"sda:"+sda+",scl:"+scl);
                    break;
                }
                case RightLayoutSerials.SERIALS_M429: {
                    RightMsgSerialsM429 serialsM429 = (RightMsgSerialsM429) msgSerials.getSerialsDetails();
                    int ch = serialsM429.getSource().getIndex() + 1;
                    int color = TChan.getChannelColor(context,ch);
                    layout.setCommonCh(ch);
                    layout.setSerialsText(
                            Tools.getChannelLevel(ch, Tools.LevelType_High, Tools.LevelMode_Bus),
                            Tools.getChannelLevel(ch, Tools.LevelType_Normal, Tools.LevelMode_Bus),
                            null,//serialsM429.getFormatSimple(),
                            null,//serialsM429.getBaudRate().getText(),
                            color, color, color, color, false, false, false, false);
                    Command.get().getBus_429().Source(msgSerials.getSerialsNumber()-1,ch-1,false);
                    break;
                }
                case RightLayoutSerials.SERIALS_M1553B: {
                    RightMsgSerialsM1553b serialsM1553B = (RightMsgSerialsM1553b) msgSerials.getSerialsDetails();
                    int ch = serialsM1553B.getSource().getIndex() + 1;
                    int color = TChan.getChannelColor(context,ch);
                    layout.setCommonCh(ch);
                    layout.setSerialsText(
                            Tools.getChannelLevel(ch, Tools.LevelType_Normal, Tools.LevelMode_Bus),
                            null, null, null, color, color, color, color, false, false, false, false);
                    Command.get().getBus_1553B().Channel(msgSerials.getSerialsNumber()-1,ch-1,false);
                    break;
                }
            }
            CacheUtil.get().setValueLevelSerials(0);
            msgSerialsDetail.setUnOpenTriggerLevel(true);
            msgSerialsDetail.setRightMsgSerials(msgSerials);
            sendMsgSerialsDetail(msgSerials.isFromEventBus());

//            setMasterVisible();
            setSerialsMasterVisible(TChan.toSerialTChan(msgSerials.getSerialsNumber()), false);
        }
    };

    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() {
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception {
            if (msgChannels.isChangeChState()) return;
            TChan.foreachMath(mathChan -> {
                MathChannel channel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
                if (channel != null && channel.isOpen()) {
                    double vScale = channel.getVScaleVal();
                    if (channel.getMathType() == MathWave.MATH_DUALWAVE) {
                        MathDualWave dualWave = channel.getMathDualWave();
                        if (msgChannels.isChangeChScaleState(dualWave.getSource1())
                                || msgChannels.isChangeChScaleState(dualWave.getSource2())) {
                            vScale = dualWave.getVScaleIdVal(dualWave.getDefaultVScaleId());
                            //dualWave.setVScaleVal(vScale);
                            channel.setVScaleVal(vScale);
                        }
                    } else if (channel.getMathType() == MathWave.MATH_EXPR) {
                        MathExprWave exprWave = channel.getMathExprWave();
                        List<Integer> sources = exprWave.getMathSources();
                        int chIdx = -1;
                        for (int i = 0; i < sources.size(); i++) {
                            chIdx = sources.get(i);
                            if (msgChannels.isChangeChScaleState(chIdx)) {
                                vScale = exprWave.getVScaleIdVal(exprWave.getDefaultVScaleId());
                                //exprWave.setVScaleVal(vScale);
                                //channel.setVScaleVal(vScale);
                                exprWave.setExprChange(true);
                                break;
                            }
                        }

                    } else {
//                    TODO FFT的数据源通道档位发生变化导致FFT档位的相应处理
//                    MathFFTWave fftWave = channel.getMathFFTWave();
//                    Channel src = ChannelFactory.getDynamicChannel(fftWave.getSource());
                    }
                    setChScale(TChan.toFpgaChNo(mathChan), vScale, channel.getProbeType(), false);
                }
            });
        }
    };

    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() {
        @Override
        public void accept(MainMsgTriggerLevel msgTriggerLevel) throws Exception {
            if (msgTriggerLevel == null || msgTriggerLevel.getCurLevel() == null) return;
            if (!msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_VALUE_UART)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_VALUE_LIN)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_VALUE_CAN)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_VALUE_SPI)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_VALUE_I2C)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_VALUE_M429)
                    && !msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_VALUE_M1553B)) {
                return;
            }

            //这里同步更新下S1/S2/S3/S4对应的显示值
            int curCh = msgTriggerLevel.getCurCh();
            TChan.foreachSerial(serialsChan -> {
                int serialsType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + TChan.toSerialNumber(serialsChan));
                MainRightLayoutItemSerialsMaster layoutMaster = sMasters.get(TChan.toSerialNumber(serialsChan) - 1);
                int layoutType = layoutMaster.getSerialsType();
                switch (layoutType) {
                    case RightLayoutSerials.SERIALS_UART:
                    case RightLayoutSerials.SERIALS_LIN:
                    case RightLayoutSerials.SERIALS_CAN:
                    case RightLayoutSerials.SERIALS_M1553B:
                        layoutMaster.setCommonValueLevel(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                        break;
                    case RightLayoutSerials.SERIALS_SPI:
                        int clk = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + TChan.toSerialNumber(serialsChan));
                        int data = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + TChan.toSerialNumber(serialsChan));
                        int cs = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + TChan.toSerialNumber(serialsChan));
                        layoutMaster.setCommonValueLevel(clk + 1, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                        layoutMaster.setCommonValueLevel(data + 1, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                        layoutMaster.setCommonValueLevel(cs + 1, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                        break;
                    case RightLayoutSerials.SERIALS_M429:
                        layoutMaster.setCommonValueLevel(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                        if (serialsType == CacheUtil.M429) {
                            layoutMaster.setCommonValueLevel(curCh, Tools.LevelType_High, Tools.LevelMode_Bus);
                        }
                        break;
                    case RightLayoutSerials.SERIALS_I2C:
                        int sda = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + TChan.toSerialNumber(serialsChan));
                        int scl = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + TChan.toSerialNumber(serialsChan));
                        layoutMaster.setCommonValueLevel(sda + 1, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                        layoutMaster.setCommonValueLevel(scl + 1, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                        break;
                }
            });
        }
    };

    private void refreshSerialValue(MainRightLayoutItemSerialsMaster busMaster){
        switch (busMaster.getSerialsType()) {
            case RightLayoutSerials.SERIALS_M429:
                busMaster.setCommonValueLevel(busMaster.getCommonCh(), Tools.LevelType_Normal, Tools.LevelMode_Bus);
                busMaster.setCommonValueLevel(busMaster.getCommonCh(), Tools.LevelType_High, Tools.LevelMode_Bus);
                break;
            case RightLayoutSerials.SERIALS_SPI:
                busMaster.setCommonValueLevel(serialsSpiLine1, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                busMaster.setCommonValueLevel(serialsSpiLine2, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                busMaster.setCommonValueLevel(serialsSpiLine3, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                break;
            case RightLayoutSerials.SERIALS_I2C:
                busMaster.setCommonValueLevel(serialsI2cLine1, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                busMaster.setCommonValueLevel(serialsI2cLine2, Tools.LevelType_Normal, Tools.LevelMode_Bus);
                break;
            case RightLayoutSerials.SERIALS_UART:
            case RightLayoutSerials.SERIALS_LIN:
            case RightLayoutSerials.SERIALS_CAN:
            case RightLayoutSerials.SERIALS_M1553B:
                busMaster.setCommonValueLevel(busMaster.getCommonCh(), Tools.LevelType_Normal, Tools.LevelMode_Bus);
                break;
        }
    }

    private void refreshMathValue(){
        TChan.foreachMath(mathChan -> {
            int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChan);
            MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
            if(mathChannel != null) {
                mathChannel.setProbeType(mathChannel.generateProbeType());
                if (mathType == CacheUtil.MATHTYPE_AXB || mathType == CacheUtil.MATHTYPE_AM) {
                    setMathWaveAxbUnit(mathChan, mathChannel);
                }
                setChScale(TChan.toFpgaChNo(mathChan), mathChannel.getVScaleIdVal(), mathChannel.getProbeType(), false);
            }
        });
    }

    private int getColor(int chIndex) {
        return TChan.getChannelColor(context,chIndex + 1);
    }

    private void sendMsgSerialsDetail(boolean isFromEventBus) {
        msgSerialsDetail.setFromEventBus(isFromEventBus);
        RxBus.getInstance().post(RxEnum.MAINRIGHT_SERIALSDETAIL, msgSerialsDetail);
    }

    private void sendMsg() {
        setEnableSlip();
        RxBus.getInstance().post(RxEnum.MAINRIGHT_OTHERS_PREV, msgOthers);
    }

    private boolean getCheckedStatue(boolean checked, @MainViewGroup.Slip int slip) {
        MainViewGroup viewGroup = (MainViewGroup) itemView;
        if (!checked && !viewGroup.isSlipShow(slip)) {
            checked = true;
            viewGroup.hideAllDialogSlip();
        } else if (checked && viewGroup.isSlipShow(slip)) {
            checked = false;
            viewGroup.hideSlip(slip);
        } else {
            checked = true;
            viewGroup.openSlip(slip);
            switch (slip) {
                case MainViewGroup.RIGHTSLIP_S1:
                    //rightSlipSerials 1打开
                    break;
                case MainViewGroup.RIGHTSLIP_S2:
                    //rightSlipSerials 2打开
                    break;
            }
        }
        return checked;
    }

    private static final int MSG_BRIEF_DISPLAY_MATH1 = 21;
    private static final int MSG_BRIEF_DISPLAY_MATH1_GONE = MSG_BRIEF_DISPLAY_MATH1 + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH2 = MSG_BRIEF_DISPLAY_MATH1_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH2_GONE = MSG_BRIEF_DISPLAY_MATH2 + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH3 = MSG_BRIEF_DISPLAY_MATH2_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH3_GONE = MSG_BRIEF_DISPLAY_MATH3 + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH4 = MSG_BRIEF_DISPLAY_MATH3_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH4_GONE = MSG_BRIEF_DISPLAY_MATH4 + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH5 = MSG_BRIEF_DISPLAY_MATH4_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH5_GONE = MSG_BRIEF_DISPLAY_MATH5 + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH6 = MSG_BRIEF_DISPLAY_MATH5_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH6_GONE = MSG_BRIEF_DISPLAY_MATH6 + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH7 = MSG_BRIEF_DISPLAY_MATH6_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH7_GONE = MSG_BRIEF_DISPLAY_MATH7 + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH8 = MSG_BRIEF_DISPLAY_MATH7_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_MATH8_GONE = MSG_BRIEF_DISPLAY_MATH8 + 1;
    private static final int MSG_BRIEF_DISPLAY_REF1 = MSG_BRIEF_DISPLAY_MATH8_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_REF1_GONE = MSG_BRIEF_DISPLAY_REF1 + 1;
    private static final int MSG_BRIEF_DISPLAY_REF2 = MSG_BRIEF_DISPLAY_REF1_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_REF2_GONE = MSG_BRIEF_DISPLAY_REF2 + 1;
    private static final int MSG_BRIEF_DISPLAY_REF3 = MSG_BRIEF_DISPLAY_REF2_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_REF3_GONE = MSG_BRIEF_DISPLAY_REF3 + 1;
    private static final int MSG_BRIEF_DISPLAY_REF4 = MSG_BRIEF_DISPLAY_REF3_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_REF4_GONE = MSG_BRIEF_DISPLAY_REF4 + 1;
    private static final int MSG_BRIEF_DISPLAY_REF5 = MSG_BRIEF_DISPLAY_REF4_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_REF5_GONE = MSG_BRIEF_DISPLAY_REF5 + 1;
    private static final int MSG_BRIEF_DISPLAY_REF6 = MSG_BRIEF_DISPLAY_REF5_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_REF6_GONE = MSG_BRIEF_DISPLAY_REF6 + 1;
    private static final int MSG_BRIEF_DISPLAY_REF7 = MSG_BRIEF_DISPLAY_REF6_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_REF7_GONE = MSG_BRIEF_DISPLAY_REF7 + 1;
    private static final int MSG_BRIEF_DISPLAY_REF8 = MSG_BRIEF_DISPLAY_REF7_GONE + 1;
    private static final int MSG_BRIEF_DISPLAY_REF8_GONE = MSG_BRIEF_DISPLAY_REF8 + 1;

    private static final int MSG_PROBE_TYPE_MATH1 = MSG_BRIEF_DISPLAY_REF8_GONE + 1;
    private static final int MSG_PROBE_TYPE_MATH2 = MSG_PROBE_TYPE_MATH1 + 1;
    private static final int MSG_PROBE_TYPE_MATH3 = MSG_PROBE_TYPE_MATH2 + 1;
    private static final int MSG_PROBE_TYPE_MATH4 = MSG_PROBE_TYPE_MATH3 + 1;
    private static final int MSG_PROBE_TYPE_MATH5 = MSG_PROBE_TYPE_MATH4 + 1;
    private static final int MSG_PROBE_TYPE_MATH6 = MSG_PROBE_TYPE_MATH5 + 1;
    private static final int MSG_PROBE_TYPE_MATH7 = MSG_PROBE_TYPE_MATH6 + 1;
    private static final int MSG_PROBE_TYPE_MATH8 = MSG_PROBE_TYPE_MATH7 + 1;
    private static final int MSG_PROBE_TYPE_REF1 = MSG_PROBE_TYPE_MATH8 + 1;
    private static final int MSG_PROBE_TYPE_REF2 = MSG_PROBE_TYPE_REF1 + 1;
    private static final int MSG_PROBE_TYPE_REF3 = MSG_PROBE_TYPE_REF2 + 1;
    private static final int MSG_PROBE_TYPE_REF4 = MSG_PROBE_TYPE_REF3 + 1;
    private static final int MSG_PROBE_TYPE_REF5 = MSG_PROBE_TYPE_REF4 + 1;
    private static final int MSG_PROBE_TYPE_REF6 = MSG_PROBE_TYPE_REF5 + 1;
    private static final int MSG_PROBE_TYPE_REF7 = MSG_PROBE_TYPE_REF6 + 1;
    private static final int MSG_PROBE_TYPE_REF8 = MSG_PROBE_TYPE_REF7 + 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_BRIEF_DISPLAY_MATH1:
                    briefDisplayMath.setVisibility(View.GONE);
                    briefDisplayMath.setText(math1Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_MATH1_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_MATH1_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH1_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_MATH2:
                    briefDisplayMath.setVisibility(View.GONE);
                    briefDisplayMath.setText(math2Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_MATH2_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_MATH2_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH2_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_MATH3:
                    briefDisplayMath.setVisibility(View.GONE);
                    briefDisplayMath.setText(math3Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_MATH3_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_MATH3_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH3_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_MATH4:
                    briefDisplayMath.setVisibility(View.GONE);
                    briefDisplayMath.setText(math4Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_MATH4_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_MATH4_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH4_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_MATH5:
                    briefDisplayMath.setVisibility(View.GONE);
                    briefDisplayMath.setText(math5Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_MATH5_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_MATH5_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH5_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_MATH6:
                    briefDisplayMath.setVisibility(View.GONE);
                    briefDisplayMath.setText(math6Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_MATH6_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_MATH6_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH6_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_MATH7:
                    briefDisplayMath.setVisibility(View.GONE);
                    briefDisplayMath.setText(math7Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_MATH7_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_MATH7_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH7_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_MATH8:
                    briefDisplayMath.setVisibility(View.GONE);
                    briefDisplayMath.setText(math8Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_MATH8_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_MATH8_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH8_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_MATH1_GONE:
                case MSG_BRIEF_DISPLAY_MATH2_GONE:
                case MSG_BRIEF_DISPLAY_MATH3_GONE:
                case MSG_BRIEF_DISPLAY_MATH4_GONE:
                case MSG_BRIEF_DISPLAY_MATH5_GONE:
                case MSG_BRIEF_DISPLAY_MATH6_GONE:
                case MSG_BRIEF_DISPLAY_MATH7_GONE:
                case MSG_BRIEF_DISPLAY_MATH8_GONE:
                    briefDisplayMath.setVisibility(View.GONE);
                    break;
                case MSG_BRIEF_DISPLAY_REF1:
                    briefDisplayRef.setVisibility(View.GONE);
                    briefDisplayRef.setText(ref1Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_REF1_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_REF1_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF1_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_REF2:
                    briefDisplayRef.setVisibility(View.GONE);
                    briefDisplayRef.setText(ref2Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_REF2_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_REF2_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF2_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_REF3:
                    briefDisplayRef.setVisibility(View.GONE);
                    briefDisplayRef.setText(ref3Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_REF3_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_REF3_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF3_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_REF4:
                    briefDisplayRef.setVisibility(View.GONE);
                    briefDisplayRef.setText(ref4Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_REF4_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_REF4_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF4_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_REF5:
                    briefDisplayRef.setVisibility(View.GONE);
                    briefDisplayRef.setText(ref5Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_REF5_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_REF5_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF5_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_REF6:
                    briefDisplayRef.setVisibility(View.GONE);
                    briefDisplayRef.setText(ref6Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_REF6_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_REF6_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF6_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_REF7:
                    briefDisplayRef.setVisibility(View.GONE);
                    briefDisplayRef.setText(ref7Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_REF7_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_REF7_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF7_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_REF8:
                    briefDisplayRef.setVisibility(View.GONE);
                    briefDisplayRef.setText(ref8Master.getProbeType());
                    if (handler.hasMessages(MSG_BRIEF_DISPLAY_REF8_GONE)) {
                        handler.removeMessages(MSG_BRIEF_DISPLAY_REF8_GONE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF8_GONE, 2000);
                    break;
                case MSG_BRIEF_DISPLAY_REF1_GONE:
                case MSG_BRIEF_DISPLAY_REF2_GONE:
                case MSG_BRIEF_DISPLAY_REF3_GONE:
                case MSG_BRIEF_DISPLAY_REF4_GONE:
                case MSG_BRIEF_DISPLAY_REF5_GONE:
                case MSG_BRIEF_DISPLAY_REF6_GONE:
                case MSG_BRIEF_DISPLAY_REF7_GONE:
                case MSG_BRIEF_DISPLAY_REF8_GONE:
                    briefDisplayRef.setVisibility(View.GONE);
                    break;
                case MSG_PROBE_TYPE_MATH1: {
                    math1Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    math1Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math1);
                    if (mathType == CacheUtil.MATHTYPE_FFT) {
                        math1Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math1));
                    } else {
                        ref1Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
                    }
                    WaveManage.get().setMathOffset(TChan.Math1);
                }
                break;
                case MSG_PROBE_TYPE_MATH2:{
                    math2Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    math2Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math2);
                    if (mathType == CacheUtil.MATHTYPE_FFT) {
                        math2Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math2));
                    } else {
                        math2Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
                    }
                    WaveManage.get().setMathOffset(TChan.Math2);
                }
                    break;
                case MSG_PROBE_TYPE_MATH3: {
                    math3Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    math3Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math3);
                    Log.d(TAG,"-------------- mathType:" + mathType);
                    if (mathType == CacheUtil.MATHTYPE_FFT) {
                        math3Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math3));
                    } else {
                        math3Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
                    }
                    WaveManage.get().setMathOffset(TChan.Math3);
                }
                break;
                case MSG_PROBE_TYPE_MATH4: {
                    math4Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    math4Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math4);
                    if (mathType == CacheUtil.MATHTYPE_FFT) {
                        math4Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math4));
                    } else {
                        math4Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
                    }
                    WaveManage.get().setMathOffset(TChan.Math4);
                }
                break;
                case MSG_PROBE_TYPE_MATH5: {
                    math5Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    math5Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math5);
                    if (mathType == CacheUtil.MATHTYPE_FFT) {
                        math5Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math5));
                    } else {
                        math5Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
                    }
                    WaveManage.get().setMathOffset(TChan.Math5);
                }
                break;
                case MSG_PROBE_TYPE_MATH6: {
                    math6Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    math6Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math6);
                    if (mathType == CacheUtil.MATHTYPE_FFT) {
                        math6Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math6));
                    } else {
                        math6Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
                    }
                    WaveManage.get().setMathOffset(TChan.Math6);
                }
                break;
                case MSG_PROBE_TYPE_MATH7: {
                    math7Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    math7Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math7);
                    if (mathType == CacheUtil.MATHTYPE_FFT) {
                        math7Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math7));
                    } else {
                        math7Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
                    }
                    WaveManage.get().setMathOffset(TChan.Math7);
                }
                break;
                case MSG_PROBE_TYPE_MATH8: {
                    math8Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    math8Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math8);
                    if (mathType == CacheUtil.MATHTYPE_FFT) {
                        math8Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math8));
                    } else {
                        math8Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
                    }
                    WaveManage.get().setMathOffset(TChan.Math8);
                }
                break;
                case MSG_PROBE_TYPE_REF1:
                    ref1Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    ref1Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    ref1Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R1));
                    Log.d(TAG, "handleMessage value: " + (Double) msg.obj);
                    WaveManage.get().setRefOffset(TChan.R1);
                    break;
                case MSG_PROBE_TYPE_REF2:
                    ref2Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    ref2Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    ref2Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R2));
                    WaveManage.get().setRefOffset(TChan.R2);
                    break;
                case MSG_PROBE_TYPE_REF3:
                    ref3Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    ref3Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    ref3Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R3));
                    WaveManage.get().setRefOffset(TChan.R3);
                    break;
                case MSG_PROBE_TYPE_REF4:
                    ref4Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    ref4Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    ref4Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R4));
                    WaveManage.get().setRefOffset(TChan.R4);
                    break;
                case MSG_PROBE_TYPE_REF5:
                    ref5Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    ref5Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    ref5Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R5));
                    WaveManage.get().setRefOffset(TChan.R5);
                    break;
                case MSG_PROBE_TYPE_REF6:
                    ref6Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    ref6Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    ref6Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R6));
                    WaveManage.get().setRefOffset(TChan.R6);
                    break;
                case MSG_PROBE_TYPE_REF7:
                    ref7Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    ref7Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    ref7Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R7));
                    WaveManage.get().setRefOffset(TChan.R7);
                    break;
                case MSG_PROBE_TYPE_REF8:
                    ref8Master.setProbeTypeNum(TBookUtil.getMFromDouble((Double) msg.obj));
                    ref8Master.setProbeTypeUnit(ChannelFactory.getProbeType(msg.arg1));
                    ref8Master.setTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R8));
                    WaveManage.get().setRefOffset(TChan.R8);
                    break;
            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_MATH_DISPLAY: {
                    boolean isCheck = Boolean.parseBoolean(commandMsgToUI.getParam());
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex =TChan.toUiChNo(Integer.parseInt(params[0]));
                    MainRightLayoutItemChannelMaster layoutMaster = null;
                    switch (chIndex){
                        case TChan.Math1: layoutMaster=math1Master; break;
                        case TChan.Math2: layoutMaster=math2Master; break;
                        case TChan.Math3: layoutMaster=math3Master; break;
                        case TChan.Math4: layoutMaster=math4Master; break;
                        case TChan.Math5: layoutMaster=math5Master; break;
                        case TChan.Math6: layoutMaster=math6Master; break;
                        case TChan.Math7: layoutMaster=math7Master; break;
                        case TChan.Math8: layoutMaster=math8Master; break;
                    }
                    if (layoutMaster != null) {
                        layoutMaster.setChecked(isCheck);
                        onMathRefSmall(layoutMaster);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_REF_DISPLAY: {
                    boolean isCheck = Boolean.parseBoolean(commandMsgToUI.getParam());
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex =TChan.toUiChNo(Integer.parseInt(params[0]));
                    MainRightLayoutItemChannelMaster layoutMaster = null;
                    switch (chIndex){
                        case TChan.R1: layoutMaster=ref1Master; break;
                        case TChan.R2: layoutMaster=ref2Master; break;
                        case TChan.R3: layoutMaster=ref3Master; break;
                        case TChan.R4: layoutMaster=ref4Master; break;
                        case TChan.R5: layoutMaster=ref5Master; break;
                        case TChan.R6: layoutMaster=ref6Master; break;
                        case TChan.R7: layoutMaster=ref7Master; break;
                        case TChan.R8: layoutMaster=ref8Master; break;
                    }
                    if (layoutMaster != null) {
                        layoutMaster.setChecked(isCheck);
                        onMathRefSmall(layoutMaster);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_REF_Vscal :

                    String[] param= commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int ch=Integer.parseInt(param[0]);
                    double vs=Double.parseDouble(param[1]);
                    double src=Double.parseDouble(param[1]);
                    //边界处理
                    if (VerticalAxis.getScaleIdByValue(vs)==-1){
                        double min= VerticalAxis.getScaleIdValById(VerticalAxis.getMinGear());
                        if (Double.compare(vs,min)>0){
                            vs=VerticalAxis.getScaleIdValById(VerticalAxis.getMaxGear());
                        }else{
                            vs=VerticalAxis.getScaleIdValById(VerticalAxis.getMinGear());
                        }
                    }

                    int refIndex = ChannelFactory.getInstance().getChannel(ch).getChId();
                    RefChannel refChannel = ChannelFactory.getRefChannel(refIndex);
                    if (refChannel.getRefType()==2){ //ref fft
                        Integer[] ints=new Integer[]{1,2,5,10,20,50,100,200,500}; //db档位
                        if (Tools.indexOf(ints,s->s==(int)src)==-1) return;
                        refChannel.setVScaleVal(src);
                    }else {
                        refChannel.setVScaleId(VerticalAxis.getScaleIdByValue(vs));
                    }
                    MainRightLayoutItemChannelMaster refMaster = null;
                    switch (TChan.toUiChNo(refIndex)) {
                        case TChan.R1: refMaster=ref1Master; break;
                        case TChan.R2: refMaster=ref2Master; break;
                        case TChan.R3: refMaster=ref3Master; break;
                        case TChan.R4: refMaster=ref4Master; break;
                        case TChan.R5: refMaster=ref5Master; break;
                        case TChan.R6: refMaster=ref6Master; break;
                        case TChan.R7: refMaster=ref7Master; break;
                        case TChan.R8: refMaster=ref8Master; break;
                    }
                    setChScale(refIndex, refChannel.getVScaleIdVal(), refChannel.getProbeType(), false);
//                    WaveManage.get().setRefOffset(TChan.toUiChNo(refIndex));
                    break;
                case CommandMsgToUI.FLAG_MATH_BASE_EXTENT: {
//                    if (Command.get().getMath().ModeQ() != CacheUtil.MATHTYPE_DW) return;
                    String[] strings = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int mathIndex = Integer.parseInt(strings[0]);
                    double extent = Double.parseDouble(strings[1]);
                    MathChannel mathChannel = ChannelFactory.getMathChannel(mathIndex);
                    if(mathChannel == null) return;
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        int i = mathChannel.getVScaleId(extent);
                        //getVscaledid()找不到会返回最小值，这样就错了。所以下面问是不是在有效档位，就会为true,就会有漏洞。
                        if ((i == 0 && extent != 1e-15) || !mathChannel.isVScaleIdValid(i)) {
                            DToast.get().show(R.string.msgParameterLimited);
                        } else {
                            //mathChannel.setVScaleId(mathChannel.getVScaleId(extent));
//                            setCacheVScaleId(mathChannel.getVScaleId());
                            setCacheVScaleId(mathChannel.getVScaleId(extent), TChan.toUiChNo(mathIndex));
                            setMathWaveVScaleId(TChan.toUiChNo(mathIndex), CacheUtil.MATHTYPE_DW);
                            setChScale(mathIndex, mathChannel.getVScaleVal(), mathChannel.getProbeType(), true);
                            if (ChannelFactory.isChActivate(mathIndex))
                                CursorManage.getInstance().curChannelMove();
                        }
                    }
                    double extent1 = mathChannel.getVScaleVal();
                    Command.get().getMath_base().Extent(mathIndex, extent1, false);
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFT_Extent: {
//                    if (Command.get().getMath().ModeQ() != CacheUtil.MATHTYPE_FFT) return;
                    String[] strings = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int mathIndex = Integer.parseInt(strings[0]);
                    double extent = Double.parseDouble(strings[1]);
                    MathChannel mathChannel = ChannelFactory.getMathChannel(mathIndex);
                    if (mathChannel == null) return;
                    int type = mathChannel.getMathFFTWave().getFFTType();
                    if (type == 0) {
                        int math = Command.get().getMath_fft().SourceQ();
                        double prob = Command.get().getChannel().ProbeQ(math);
                        extent = extent / prob;
                        if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                            int i = mathChannel.getVScaleId(extent);
                            if ((i == 0 && extent != 1e-15) || !mathChannel.isVScaleIdValid(mathChannel.getVScaleId(extent))) {
                                DToast.get().show(R.string.msgParameterLimited);
                            } else {
                                setCacheVScaleId(mathChannel.getVScaleId(extent), TChan.toUiChNo(mathIndex));
                                setMathWaveVScaleId(TChan.toUiChNo(mathIndex), CacheUtil.MATHTYPE_FFT);
                                setChScale(mathIndex, mathChannel.getVScaleVal(), mathChannel.getProbeType(), true);
                                if (ChannelFactory.isChActivate(mathIndex))
                                    CursorManage.getInstance().curChannelMove();
                            }
                        }
                        double extent1 = mathChannel.getVScaleVal();
                        Command.get().getMath_fft().Extent(mathIndex, extent1, false);
                    } else if (type == 1) { //db
                        if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                            int extentId = mathChannel.getMathFFTWave().getVScaleId(extent, type);
                            if (!mathChannel.isVScaleIdValid(extentId)) {
                                DToast.get().show(R.string.msgParameterLimited);
                            } else {
                                mathChannel.setVScaleId(extentId);
                                setCacheVScaleId(mathChannel.getVScaleId(), TChan.toUiChNo(mathIndex));
                                setChScale(mathIndex, mathChannel.getVScaleVal(), mathChannel.getProbeType(), true);
                                if (ChannelFactory.isChActivate(mathIndex))
                                    CursorManage.getInstance().curChannelMove();
                                setCommandExtent(mathIndex, extent);
                            }

                        }
                    }
                }
                break;
                case CommandMsgToUI.FLAG_MATH_AXB_Extent:{
//                    if (Command.get().getMath().ModeQ()!=CacheUtil.MATHTYPE_AXB) return;
                    String[] strings = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int mathIndex = Integer.parseInt(strings[0]);
                    double extent = Double.parseDouble(strings[1]);
                    MathChannel mathChannel = ChannelFactory.getMathChannel(mathIndex);
                    if (mathChannel == null) return;
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY){
                        int i=mathChannel.getVScaleId(extent);
                        if ((i==0 && extent!=1e-15) || !mathChannel.isVScaleIdValid(mathChannel.getVScaleId(extent))) {
                            DToast.get().show(R.string.msgParameterLimited);
                        } else {
                            setCacheVScaleId(mathChannel.getVScaleId(extent), TChan.toUiChNo(mathIndex));
                            setMathWaveVScaleId(TChan.toUiChNo(mathIndex), CacheUtil.MATHTYPE_AXB);
                            setChScale(mathIndex, mathChannel.getVScaleVal(), mathChannel.getProbeType(), true);
                            if (ChannelFactory.isChActivate(mathIndex))
                                CursorManage.getInstance().curChannelMove();
                        }
                    }
                    double extent1 = mathChannel.getVScaleVal();
                    Command.get().getMath_axb().Extent(mathIndex, extent1, false);
                }break;
                case CommandMsgToUI.FLAG_MATH_ADV_Extent:{
//                    if (Command.get().getMath().ModeQ()!=CacheUtil.MATHTYPE_AM) return;
                    String[] strings = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int mathIndex = Integer.parseInt(strings[0]);
                    double extent = Double.parseDouble(strings[1]);
                    MathChannel mathChannel = ChannelFactory.getMathChannel(mathIndex);
                    if (mathChannel == null) return;
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY){
                        int i=mathChannel.getVScaleId(extent);
                        if ((i==0 && extent!=1e-15) || !mathChannel.isVScaleIdValid(mathChannel.getVScaleId(extent))) {
                            DToast.get().show(R.string.msgParameterLimited);
                        } else {
                            setCacheVScaleId(mathChannel.getVScaleId(extent), TChan.toUiChNo(mathIndex));
                            setMathWaveVScaleId(TChan.toUiChNo(mathIndex), CacheUtil.MATHTYPE_AM);
                            setChScale(mathIndex, mathChannel.getVScaleVal(), mathChannel.getProbeType(), true);
                            if (ChannelFactory.isChActivate(mathIndex))
                                CursorManage.getInstance().curChannelMove();
                        }
                    }
                    double extent1 = mathChannel.getVScaleVal();
                    Command.get().getMath_advanced().Extent(mathIndex, extent1, false);
                }break;

                case CommandMsgToUI.FLAG_MATH_BASE_OFFSET:
                case CommandMsgToUI.FLAG_MATH_FFT_Offset:
                case CommandMsgToUI.FLAG_MATH_AXB_Offset:
                case CommandMsgToUI.FLAG_MATH_ADV_Offset: {
                    String[] strings = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int mathIndex = Integer.parseInt(strings[0]);
                    double offsetY = Double.parseDouble(strings[1]);
                    MathChannel mathChannel = ChannelFactory.getMathChannel(mathIndex);
                    if (mathChannel == null) return;
                    double pixY = offsetY / mathChannel.getVerticalPerPix();
                    pixY= GlobalVar.get().getMainWave().y/2-pixY;

                    CursorManage.getInstance().setScpiChanIdx(TChan.toUiChNo(mathIndex));
                    CursorManage.getInstance().setCursorTrace(true);
                    WaveManage.get().setPositionY(TChan.toUiChNo(mathIndex), (int) pixY);

                    CursorManage.setCursorByScaleTrace();
                    CursorManage.getInstance().setCursorTrace(false);
//                    int pix = (int) Tools.TimebaseToPix(offsetX);
//                    TriggerTimebase.getInstance().setX(pix);
                }
                break;


                case CommandMsgToUI.FLAG_MATH_FFTSOURCE: {
                    Logger.i(TAG, "flag_math_fftsource");
                    int index = Integer.parseInt(commandMsgToUI.getParam());
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFTTYPE: {
                    Logger.i(TAG, "flag_math_ffttype");
                    int index = Integer.parseInt(commandMsgToUI.getParam());
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFTWINDOW: {
                    Logger.i(TAG, "flag_math_fftwindow");
                    int index = Integer.parseInt(commandMsgToUI.getParam());
                }
                break;
            }
        }
    };

    private Consumer<ExternalKeysMsgVScale> consumerExternalKeysVScale = new Consumer<ExternalKeysMsgVScale>() {
        @Override
        public void accept(ExternalKeysMsgVScale msgVScale) throws Exception {
            MainRightLayoutItemChannelMaster layoutMaster = null;
            switch (msgVScale.getChIndex()) {
                case ChannelFactory.MATH1:
                    layoutMaster = math1Master;
                    break;
                case ChannelFactory.MATH2:
                    layoutMaster = math2Master;
                    break;
                case ChannelFactory.MATH3:
                    layoutMaster = math3Master;
                    break;
                case ChannelFactory.MATH4:
                    layoutMaster = math4Master;
                    break;
                case ChannelFactory.MATH5:
                    layoutMaster = math5Master;
                    break;
                case ChannelFactory.MATH6:
                    layoutMaster = math6Master;
                    break;
                case ChannelFactory.MATH7:
                    layoutMaster = math7Master;
                    break;
                case ChannelFactory.MATH8:
                    layoutMaster = math8Master;
                    break;
                case ChannelFactory.REF1:
                    layoutMaster = ref1Master;
                    break;
                case ChannelFactory.REF2:
                    layoutMaster = ref2Master;
                    break;
                case ChannelFactory.REF3:
                    layoutMaster = ref3Master;
                    break;
                case ChannelFactory.REF4:
                    layoutMaster = ref4Master;
                    break;
                case ChannelFactory.REF5:
                    layoutMaster = ref5Master;
                    break;
                case ChannelFactory.REF6:
                    layoutMaster = ref6Master;
                    break;
                case ChannelFactory.REF7:
                    layoutMaster = ref7Master;
                    break;
                case ChannelFactory.REF8:
                    layoutMaster = ref8Master;
                    break;
            }
            if (layoutMaster != null) {
                if (msgVScale.isAdd()) {
                    layoutMaster.onBtnBottomClick();
                } else {
                    layoutMaster.onBtnTopClick();
                }
            }
        }
    };

    private Consumer<Integer> consumerUserSelfAdjust = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            if (math1Master.isChecked()) {
                math1Master.setChecked(false);
                onMathRefSmall(math1Master);
            }
            if (math2Master.isChecked()) {
                math2Master.setChecked(false);
                onMathRefSmall(math2Master);
            }
            if (math3Master.isChecked()) {
                math3Master.setChecked(false);
                onMathRefSmall(math3Master);
            }
            if (math4Master.isChecked()) {
                math4Master.setChecked(false);
                onMathRefSmall(math4Master);
            }
            if (math5Master.isChecked()) {
                math5Master.setChecked(false);
                onMathRefSmall(math5Master);
            }
            if (math6Master.isChecked()) {
                math6Master.setChecked(false);
                onMathRefSmall(math6Master);
            }
            if (math7Master.isChecked()) {
                math7Master.setChecked(false);
                onMathRefSmall(math7Master);
            }
            if (math8Master.isChecked()) {
                math8Master.setChecked(false);
                onMathRefSmall(math8Master);
            }
            if (ref1Master.isChecked()) {
                ref1Master.setChecked(false);
                onMathRefSmall(ref1Master);
            }
            if (ref2Master.isChecked()) {
                ref2Master.setChecked(false);
                onMathRefSmall(ref2Master);
            }
            if (ref3Master.isChecked()) {
                ref3Master.setChecked(false);
                onMathRefSmall(ref3Master);
            }
            if (ref4Master.isChecked()) {
                ref4Master.setChecked(false);
                onMathRefSmall(ref4Master);
            }
            if (ref5Master.isChecked()) {
                ref5Master.setChecked(false);
                onMathRefSmall(ref5Master);
            }
            if (ref6Master.isChecked()) {
                ref6Master.setChecked(false);
                onMathRefSmall(ref6Master);
            }
            if (ref7Master.isChecked()) {
                ref7Master.setChecked(false);
                onMathRefSmall(ref7Master);
            }
            if (ref8Master.isChecked()) {
                ref8Master.setChecked(false);
                onMathRefSmall(ref8Master);
            }
            if (s1Master.isChecked()) {
                onSerialsClickListener.onClick(s1Master);
            }
            if (s2Master.isChecked()) {
                onSerialsClickListener.onClick(s2Master);
            }
            if (s3Master.isChecked()) {
                onSerialsClickListener.onClick(s3Master);
            }
            if (s4Master.isChecked()) {
                onSerialsClickListener.onClick(s4Master);
            }
        }
    };

    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            isSerialsWordShow = aBoolean;
//            mathMasters.forEach(mathMaster -> {
//                mathMaster.setDisable(aBoolean);
////                mathMaster.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
//            });
//            refMasters.forEach(refMaster -> {
//                refMaster.setDisable(aBoolean);
////                refMaster.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
//            });
            setEnableSlip();

            //文本页面不显示数学/参考按钮
            TChan.foreachMath(mathChan -> {
                setMathMasterVisible(mathChan, false);
            });
            TChan.foreachRef(refChan -> {
                setRefMasterVisible(refChan, false);
            });

        }
    };

    private Consumer<MainMsgSlip> consumerMainSlipToOther = new Consumer<MainMsgSlip>() {
        @Override
        public void accept(MainMsgSlip mainMsgSlip) throws Exception {
            if (mainMsgSlip.isOpen()) {
                int chSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
                switch (mainMsgSlip.getSlip()) {
                    case MainViewGroup.RIGHTSLIP_MATH1:
                        setRightMasterSmall(TChan.Math1);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch1, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.Math1
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math1)
                                && isAddByUser(TChan.Math1)
                        ) {
                            math1Master.setChecked(true);
                            onMathRefSmall(math1Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_MATH2:
                        setRightMasterSmall(TChan.Math2);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch2, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.Math2
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math2)
                                && isAddByUser(TChan.Math2)
                        ) {
                            math2Master.setChecked(true);
                            onMathRefSmall(math2Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_MATH3:
                        setRightMasterSmall(TChan.Math3);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch3, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.Math3
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math3)
                                && isAddByUser(TChan.Math3)
                        ) {
                            math3Master.setChecked(true);
                            onMathRefSmall(math3Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_MATH4:
                        setRightMasterSmall(TChan.Math4);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch4, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.Math4
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math4)
                                && isAddByUser(TChan.Math4)
                        ) {
                            math4Master.setChecked(true);
                            onMathRefSmall(math4Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_MATH5:
                        setRightMasterSmall(TChan.Math5);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch5, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.Math5
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math5)
                                && isAddByUser(TChan.Math5)
                        ) {
                            math5Master.setChecked(true);
                            onMathRefSmall(math5Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_MATH6:
                        setRightMasterSmall(TChan.Math6);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch6, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.Math6
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math6)
                                && isAddByUser(TChan.Math6)
                        ) {
                            math6Master.setChecked(true);
                            onMathRefSmall(math6Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_MATH7:
                        setRightMasterSmall(TChan.Math7);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch7, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.Math7
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math7)
                                && isAddByUser(TChan.Math7)
                        ) {
                            math7Master.setChecked(true);
                            onMathRefSmall(math7Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_MATH8:
                        setRightMasterSmall(TChan.Math8);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch8, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.Math8
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Math8)
                                && isAddByUser(TChan.Math8)
                        ) {
                            math8Master.setChecked(true);
                            onMathRefSmall(math8Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_REF1:
                        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, TChan.R1);
                        String key1 = CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R1;
                        CacheUtil.get().putMap(key1, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.R1
                                && CacheUtil.get().getBoolean(key1)
                                && isAddByUser(TChan.R1)
                        ) {
                            ref1Master.setChecked(true);
                            onMathRefSmall(ref1Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_REF2:
                        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, TChan.R2);
                        String key2 = CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R2;
                        CacheUtil.get().putMap(key2, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.R2
                                && CacheUtil.get().getBoolean(key2)
                                && isAddByUser(TChan.R2)
                        ) {
                            ref2Master.setChecked(true);
                            onMathRefSmall(ref2Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_REF3:
                        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, TChan.R3);
                        String key3 = CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R3;
                        CacheUtil.get().putMap(key3, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.R3
                                && CacheUtil.get().getBoolean(key3)
                                && isAddByUser(TChan.R3)
                        ) {
                            ref3Master.setChecked(true);
                            onMathRefSmall(ref3Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_REF4:
                        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, TChan.R4);
                        String key4 = CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R4;
                        CacheUtil.get().putMap(key4, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.R4
                                && CacheUtil.get().getBoolean(key4)
                                && isAddByUser(TChan.R4)
                        ) {
                            ref4Master.setChecked(true);
                            onMathRefSmall(ref4Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_REF5:
                        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, TChan.R5);
                        String key5 = CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R5;
                        CacheUtil.get().putMap(key5, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.R5
                                && CacheUtil.get().getBoolean(key5)
                                && isAddByUser(TChan.R5)
                        ) {
                            ref5Master.setChecked(true);
                            onMathRefSmall(ref5Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_REF6:
                        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, TChan.R6);
                        String key6 = CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R6;
                        CacheUtil.get().putMap(key6, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.R6
                                && CacheUtil.get().getBoolean(key6)
                                && isAddByUser(TChan.R6)
                        ) {
                            ref6Master.setChecked(true);
                            onMathRefSmall(ref6Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_REF7:
                        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, TChan.R7);
                        String key7 = CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R7;
                        CacheUtil.get().putMap(key7, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.R7
                                && CacheUtil.get().getBoolean(key7)
                                && isAddByUser(TChan.R7)
                        ) {
                            ref7Master.setChecked(true);
                            onMathRefSmall(ref7Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_REF8:
                        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, TChan.R8);
                        String key8 = CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R8;
                        CacheUtil.get().putMap(key8, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.R8
                                && CacheUtil.get().getBoolean(key8)
                                && isAddByUser(TChan.R8)
                        ) {
                            ref8Master.setChecked(true);
                            onMathRefSmall(ref8Master);
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_S1:
                        setRightMasterSmall(CacheUtil.S1);
//                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.S1
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1)
                                && isAddByUser(TChan.S1)
                        ) {
                            s1Master.setChecked(true);
                            setS1Scope(true);
                            SerialBusManage.getInstance().setVisible(TChan.S1, true);
                            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1, String.valueOf(true));
                            msgOthers.setS1(s1Master.isChecked());
                            sendMsg();
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_S2:
                        setRightMasterSmall(CacheUtil.S2);
//                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.S2
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)
                                && isAddByUser(TChan.S2)
                        ) {
                            s2Master.setChecked(true);
                            setS2Scope(true);
                            SerialBusManage.getInstance().setVisible(TChan.S2, true);
                            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2, String.valueOf(true));
                            msgOthers.setS2(s2Master.isChecked());
                            sendMsg();
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_S3:
                        setRightMasterSmall(CacheUtil.S3);
//                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.S3
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3)
                                && isAddByUser(TChan.S3)
                        ) {
                            s3Master.setChecked(true);
                            setS3Scope(true);
                            SerialBusManage.getInstance().setVisible(TChan.S3, true);
                            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3, String.valueOf(true));
                            msgOthers.setS3(s3Master.isChecked());
                            sendMsg();
                        }
                        break;
                    case MainViewGroup.RIGHTSLIP_S4:
                        setRightMasterSmall(CacheUtil.S4);
//                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4, String.valueOf(true));
                        if (TChan.toUiChNo(chSelect) != TChan.S4
                                && CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4)
                                && isAddByUser(TChan.S4)
                        ) {
                            s4Master.setChecked(true);
                            setS4Scope(true);
                            SerialBusManage.getInstance().setVisible(TChan.S4, true);
                            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4, String.valueOf(true));
                            msgOthers.setS4(s4Master.isChecked());
                            sendMsg();
                        }
                        break;
                }
            }
        }
    };


    private void setRightMasterSmall(int indexLarge) {
        setRightMasterSmall(indexLarge, true);
    }

    private void setRightMasterSmall(int indexLarge, boolean check) {
        if (indexLarge == TChan.Math1 || indexLarge == TChan.Math2
                || indexLarge == TChan.Math3 || indexLarge == TChan.Math4
                || indexLarge == TChan.Math5 || indexLarge == TChan.Math6
                || indexLarge == TChan.Math7 || indexLarge == TChan.Math8
        ) {
            MainRightLayoutItemChannelMaster mathMaster = null;
            switch (indexLarge) {
                case TChan.Math1: mathMaster = math1Master;break;
                case TChan.Math2: mathMaster = math2Master;break;
                case TChan.Math3: mathMaster = math3Master;break;
                case TChan.Math4: mathMaster = math4Master;break;
                case TChan.Math5: mathMaster = math5Master;break;
                case TChan.Math6: mathMaster = math6Master;break;
                case TChan.Math7: mathMaster = math7Master;break;
                case TChan.Math8: mathMaster = math8Master;break;
            }
            if (mathMaster == null) return;
            if (!check) {
                mathMaster.setChecked(false);
            } else {
                mathMaster.setState(true, true);
            }
            msgOthers.setMath(indexLarge, check);
        } else if (indexLarge == TChan.R1 || indexLarge == TChan.R2
                || indexLarge == TChan.R3 || indexLarge == TChan.R4
                || indexLarge == TChan.R5 || indexLarge == TChan.R6
                || indexLarge == TChan.R7 || indexLarge == TChan.R8
        ) {
            MainRightLayoutItemChannelMaster refMaster = null;
            switch (indexLarge) {
                case TChan.R1: refMaster = ref1Master;break;
                case TChan.R2: refMaster = ref2Master;break;
                case TChan.R3: refMaster = ref3Master;break;
                case TChan.R4: refMaster = ref4Master;break;
                case TChan.R5: refMaster = ref5Master;break;
                case TChan.R6: refMaster = ref6Master;break;
                case TChan.R7: refMaster = ref7Master;break;
                case TChan.R8: refMaster = ref8Master;break;
            }
            if (refMaster == null) return;
            if (check) {
                refMaster.setState(true, true);
            } else {
                refMaster.setChecked(false);
            }
            msgOthers.setRef(indexLarge, check);
        }
    }

    private void setCommandExtent(int mathIndex, double extent) {
        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(mathIndex))) {
            case CacheUtil.MATHTYPE_DW:
                Command.get().getMath_base().Extent(mathIndex, extent, false);
                break;
            case CacheUtil.MATHTYPE_FFT:
                Command.get().getMath_fft().Extent(mathIndex, extent, false);
                break;
            case CacheUtil.MATHTYPE_AXB:
                Command.get().getMath_axb().Extent(mathIndex, extent, false);
                break;
            default:
                Command.get().getMath_advanced().Extent(mathIndex, extent, false);
                break;
        }
    }

    private MainRightLayoutItemChannelMaster.OnRightMasterListener onMathRefMasterListener = new MainRightLayoutItemChannelMaster.OnRightMasterListener() {
        @Override
        public void onTopClick(MainRightLayoutItemChannelMaster v) {
            CursorManage.getInstance().setCursorTrace(true);

            PlaySound.getInstance().playButton();
            if (v.getId() == R.id.math1Master || v.getId() == R.id.math2Master
                    || v.getId() == R.id.math3Master || v.getId() == R.id.math4Master
                    || v.getId() == R.id.math5Master || v.getId() == R.id.math6Master
                    || v.getId() == R.id.math7Master || v.getId() == R.id.math8Master

            ) {
                MainRightLayoutItemChannelMaster mathMaster = null;
                int mathIndex = ChannelFactory.MATH1;
                switch (v.getId()) {
                    case R.id.math1Master:
                        mathIndex = ChannelFactory.MATH1;
                        mathMaster = math1Master;
                        break;
                    case R.id.math2Master:
                        mathIndex = ChannelFactory.MATH2;
                        mathMaster = math2Master;
                        break;
                    case R.id.math3Master:
                        mathIndex = ChannelFactory.MATH3;
                        mathMaster = math3Master;
                        break;
                    case R.id.math4Master:
                        mathIndex = ChannelFactory.MATH4;
                        mathMaster = math4Master;
                        break;
                    case R.id.math5Master:
                        mathIndex = ChannelFactory.MATH5;
                        mathMaster = math5Master;
                        break;
                    case R.id.math6Master:
                        mathIndex = ChannelFactory.MATH6;
                        mathMaster = math6Master;
                        break;
                    case R.id.math7Master:
                        mathIndex = ChannelFactory.MATH7;
                        mathMaster = math7Master;
                        break;
                    case R.id.math8Master:
                        mathIndex = ChannelFactory.MATH8;
                        mathMaster = math8Master;
                        break;
                }
                if (mathMaster != null) {
                    MathChannel channel = ChannelFactory.getMathChannel(mathIndex);
                    int scaleId = channel.calcVScaleId(-1);
                    if (!channel.isVScaleIdValid(scaleId)) {
                        DToast.get().show(R.string.msgParameterLimited);
                    } else {
                        channel.setVScaleId(scaleId);
                        setCacheVScaleId(channel.getVScaleId(), TChan.toUiChNo(mathIndex));
                        double extent = channel.getVScaleVal();
                        setChScale(mathIndex, extent, channel.getProbeType(), true);
                        if (ChannelFactory.isChActivate(mathIndex))
                            CursorManage.getInstance().curChannelMove();

                        setCommandExtent(mathIndex, extent);
                    }
                    msgOthers.setMath(TChan.toUiChNo(mathIndex), mathMaster.isChecked());
                    sendMsg();
                }
            } else if (v.getId() == R.id.ref1Master || v.getId() == R.id.ref2Master
                    || v.getId() == R.id.ref3Master || v.getId() == R.id.ref4Master
                    || v.getId() == R.id.ref5Master || v.getId() == R.id.ref6Master
                    || v.getId() == R.id.ref7Master || v.getId() == R.id.ref8Master) {
                MainRightLayoutItemChannelMaster refMaster = null;
                int refIndex = ChannelFactory.REF1;
                switch (v.getId()) {
                    case R.id.ref1Master:
                        refMaster = ref1Master;
                        refIndex = ChannelFactory.REF1;
                        break;
                    case R.id.ref2Master:
                        refMaster = ref2Master;
                        refIndex = ChannelFactory.REF2;
                        break;
                    case R.id.ref3Master:
                        refMaster = ref3Master;
                        refIndex = ChannelFactory.REF3;
                        break;
                    case R.id.ref4Master:
                        refMaster = ref4Master;
                        refIndex = ChannelFactory.REF4;
                        break;
                    case R.id.ref5Master:
                        refMaster = ref5Master;
                        refIndex = ChannelFactory.REF5;
                        break;
                    case R.id.ref6Master:
                        refMaster = ref6Master;
                        refIndex = ChannelFactory.REF6;
                        break;
                    case R.id.ref7Master:
                        refMaster = ref7Master;
                        refIndex = ChannelFactory.REF7;
                        break;
                    case R.id.ref8Master:
                        refMaster = ref8Master;
                        refIndex = ChannelFactory.REF8;
                        break;
                }
                if (refMaster == null) return;
                RefChannel channel = ChannelFactory.getRefChannel(refIndex);
                int scaleId = channel.calcVScaleId(-1);
                if (scaleId < channel.getVScaleIdMin()) {
                    DToast.get().show(R.string.msgParameterLimited);
                } else {
                    channel.setVScaleId(scaleId);
                    double extent = channel.getVScaleIdVal();
                    setChScale(refIndex, extent, channel.getProbeType(), true);
                    if (ChannelFactory.isChActivate(refIndex))
                        CursorManage.getInstance().curChannelMove();
                    CacheUtil.get().putMap(CacheUtil.MAIN_CHAN_REF_VSCALE_ID + TChan.toUiChNo(refIndex), String.valueOf(scaleId));
                }
                msgOthers.setRef(TChan.toUiChNo(refIndex), refMaster.isChecked());
                sendMsg();
            }
            CursorManage.setCursorByScaleTrace();
            CursorManage.getInstance().setCursorTrace(false);
        }

        @Override
        public void onBottomClick(MainRightLayoutItemChannelMaster v) {
            CursorManage.getInstance().setCursorTrace(true);
            PlaySound.getInstance().playButton();
            if (v.getId() == R.id.math1Master || v.getId() == R.id.math2Master
                    || v.getId() == R.id.math3Master || v.getId() == R.id.math4Master
                    || v.getId() == R.id.math5Master || v.getId() == R.id.math6Master
                    || v.getId() == R.id.math7Master || v.getId() == R.id.math8Master

            ) {
                MainRightLayoutItemChannelMaster mathMaster = null;
                int mathIndex = ChannelFactory.MATH1;
                switch (v.getId()) {
                    case R.id.math1Master:
                        mathIndex = ChannelFactory.MATH1;
                        mathMaster = math1Master;
                        break;
                    case R.id.math2Master:
                        mathIndex = ChannelFactory.MATH2;
                        mathMaster = math2Master;
                        break;
                    case R.id.math3Master:
                        mathIndex = ChannelFactory.MATH3;
                        mathMaster = math3Master;
                        break;
                    case R.id.math4Master:
                        mathIndex = ChannelFactory.MATH4;
                        mathMaster = math4Master;
                        break;
                    case R.id.math5Master:
                        mathIndex = ChannelFactory.MATH5;
                        mathMaster = math5Master;
                        break;
                    case R.id.math6Master:
                        mathIndex = ChannelFactory.MATH6;
                        mathMaster = math6Master;
                        break;
                    case R.id.math7Master:
                        mathIndex = ChannelFactory.MATH7;
                        mathMaster = math7Master;
                        break;
                    case R.id.math8Master:
                        mathIndex = ChannelFactory.MATH8;
                        mathMaster = math8Master;
                        break;
                }
                if (mathMaster != null) {
                    MathChannel channel = ChannelFactory.getMathChannel(mathIndex);
                    if (channel == null) return;
                    int scaleId = channel.calcVScaleId(1);
                    if (!channel.isVScaleIdValid(scaleId)) {
                        DToast.get().show(R.string.msgParameterLimited);
                    } else {
                        channel.setVScaleId(scaleId);
                        setCacheVScaleId(channel.getVScaleId(), TChan.toUiChNo(mathIndex));
                        double extent = channel.getVScaleVal();
                        setChScale(mathIndex, extent, channel.getProbeType(), true);
                        if (ChannelFactory.isChActivate(mathIndex))
                            CursorManage.getInstance().curChannelMove();
                        setCommandExtent(mathIndex, extent);
                    }
                    msgOthers.setMath(TChan.toUiChNo(mathIndex), mathMaster.isChecked());
                    sendMsg();
                }
            } else if (v.getId() == R.id.ref1Master || v.getId() == R.id.ref2Master
                    || v.getId() == R.id.ref3Master || v.getId() == R.id.ref4Master
                    || v.getId() == R.id.ref5Master || v.getId() == R.id.ref6Master
                    || v.getId() == R.id.ref7Master || v.getId() == R.id.ref8Master)  {
                MainRightLayoutItemChannelMaster refMaster = null;
                switch (v.getId()) {
                    case R.id.ref1Master: refMaster = ref1Master;break;
                    case R.id.ref2Master: refMaster = ref2Master;break;
                    case R.id.ref3Master: refMaster = ref3Master;break;
                    case R.id.ref4Master: refMaster = ref4Master;break;
                    case R.id.ref5Master: refMaster = ref5Master;break;
                    case R.id.ref6Master: refMaster = ref6Master;break;
                    case R.id.ref7Master: refMaster = ref7Master;break;
                    case R.id.ref8Master: refMaster = ref8Master;break;
                }
                if (refMaster == null) return;
                int refIndex = refMaster.getChIndex();
                RefChannel channel = ChannelFactory.getRefChannel(refMaster.getChIndex());
                if (channel == null) return;
                int scaleId = channel.calcVScaleId(1);
                if (scaleId > channel.getVScaleIdMax()) {
                    DToast.get().show(R.string.msgParameterLimited);
                } else {
                    channel.setVScaleId(scaleId);
                    double extent = channel.getVScaleIdVal();
                    setChScale(refIndex, extent, channel.getProbeType(), true);
                    if (ChannelFactory.isChActivate(refIndex))
                        CursorManage.getInstance().curChannelMove();
                    CacheUtil.get().putMap(CacheUtil.MAIN_CHAN_REF_VSCALE_ID + TChan.toUiChNo(refIndex), String.valueOf(scaleId));
                }
                msgOthers.setRef(TChan.toUiChNo(refIndex), refMaster.isChecked());
                sendMsg();
            }
            CursorManage.setCursorByScaleTrace();
            CursorManage.getInstance().setCursorTrace(false);
        }
    };

    private MainRightLayoutItemChannelMaster.OnRightSmallListener onMathRefSmallListener = new MainRightLayoutItemChannelMaster.OnRightSmallListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onSmallClick(MainRightLayoutItemChannelMaster v) {
            CursorManage.getInstance().setCursorTrace(true);

            PlaySound.getInstance().playButton();
            MainRightLayoutItemChannelMaster refMaster = null;
            switch (v.getId()) {
                case R.id.ref1Master: refMaster = ref1Master;break;
                case R.id.ref2Master: refMaster = ref2Master;break;
                case R.id.ref3Master: refMaster = ref3Master;break;
                case R.id.ref4Master: refMaster = ref4Master;break;
                case R.id.ref5Master: refMaster = ref5Master;break;
                case R.id.ref6Master: refMaster = ref6Master;break;
                case R.id.ref7Master: refMaster = ref7Master;break;
                case R.id.ref8Master: refMaster = ref8Master;break;
            }
            if (refMaster != null) {
                refMaster.setChecked(true);
            }
            onMathRefSmall(v);
            CursorManage.setCursorByScaleTrace();
            CursorManage.getInstance().setCursorTrace(false);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onSmallDoubleClick(MainRightLayoutItemChannelMaster v) {
            //双击打开对应通道
            switch (v.getId()) {
                case R.id.ref1Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_REF1, true));
                    break;
                case R.id.ref2Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_REF2, true));
                    break;
                case R.id.ref3Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_REF3, true));
                    break;
                case R.id.ref4Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_REF4, true));
                    break;
                case R.id.ref5Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_REF5, true));
                    break;
                case R.id.ref6Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_REF6, true));
                    break;
                case R.id.ref7Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_REF7, true));
                    break;
                case R.id.ref8Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_REF8, true));
                    break;
                case R.id.math1Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_MATH1, true));
                    break;
                case R.id.math2Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_MATH2, true));
                    break;
                case R.id.math3Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_MATH3, true));
                    break;
                case R.id.math4Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_MATH4, true));
                    break;
                case R.id.math5Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_MATH5, true));
                    break;
                case R.id.math6Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_MATH6, true));
                    break;
                case R.id.math7Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_MATH7, true));
                    break;
                case R.id.math8Master:
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_MATH8, true));
                    break;

            }
        }
    };

    //下滑关闭通道
    private MainRightLayoutItemChannelMaster.SlidCloseChannelListener mathRefCloseListener = new MainRightLayoutItemChannelMaster.SlidCloseChannelListener() {

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onSlidCloseChannel(MainRightLayoutItemChannelMaster v) {
            switch (v.getId()) {
                case R.id.ref1Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R1, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R1, String.valueOf(false));
                    break;
                case R.id.ref2Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R2, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R2, String.valueOf(false));
                    break;
                case R.id.ref3Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R3, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R3, String.valueOf(false));
                    break;
                case R.id.ref4Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R4, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R4, String.valueOf(false));
                    break;
                case R.id.ref5Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R5, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R5, String.valueOf(false));
                    break;
                case R.id.ref6Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R6, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R6, String.valueOf(false));
                    break;
                case R.id.ref7Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R7, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R7, String.valueOf(false));
                    break;
                case R.id.ref8Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + TChan.R8, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R8, String.valueOf(false));
                    break;
                case R.id.math1Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math1, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Math1, String.valueOf(false));
                    break;
                case R.id.math2Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math2, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Math2, String.valueOf(false));
                    break;
                case R.id.math3Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math3, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Math3, String.valueOf(false));
                    break;
                case R.id.math4Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math4, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Math4, String.valueOf(false));
                    break;
                case R.id.math5Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math5, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Math5, String.valueOf(false));
                    break;
                case R.id.math6Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math6, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Math6, String.valueOf(false));
                    break;
                case R.id.math7Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math7, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Math7, String.valueOf(false));
                    break;
                case R.id.math8Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math8, String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + TChan.Math8, String.valueOf(false));
                    break;

            }
        }
    };

    //下滑关闭通道
    private MainRightLayoutItemSerialsMaster.SlidCloseChannelListener serialsCloseListener = new MainRightLayoutItemSerialsMaster.SlidCloseChannelListener() {

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onSlidCloseChannel(MainRightLayoutItemSerialsMaster v) {
            switch (v.getId()) {
                case R.id.rightS1Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(TChan.S1), String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + TChan.toSerialNumber(TChan.S1), String.valueOf(false));
                    break;
                case R.id.rightS2Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(TChan.S2), String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + TChan.toSerialNumber(TChan.S2), String.valueOf(false));
                    break;
                case R.id.rightS3Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(TChan.S3), String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + TChan.toSerialNumber(TChan.S3), String.valueOf(false));
                    break;
                case R.id.rightS4Master:
//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(TChan.S4), String.valueOf(false));
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + TChan.toSerialNumber(TChan.S4), String.valueOf(false));
                    break;
            }
        }
    };

    private void onMathRefSmall(MainRightLayoutItemChannelMaster v) {
        if (v.getId() == R.id.math1Master || v.getId() == R.id.math2Master
                || v.getId() == R.id.math3Master || v.getId() == R.id.math4Master
                || v.getId() == R.id.math5Master || v.getId() == R.id.math6Master
                || v.getId() == R.id.math7Master || v.getId() == R.id.math8Master

        ) {
            MainRightLayoutItemChannelMaster mathMaster = null;
            int mathIndex = ChannelFactory.MATH1;
            switch (v.getId()) {
                case R.id.math1Master:
                    mathIndex = ChannelFactory.MATH1;
                    mathMaster = math1Master;
                    break;
                case R.id.math2Master:
                    mathIndex = ChannelFactory.MATH2;
                    mathMaster = math2Master;
                    break;
                case R.id.math3Master:
                    mathIndex = ChannelFactory.MATH3;
                    mathMaster = math3Master;
                    break;
                case R.id.math4Master:
                    mathIndex = ChannelFactory.MATH4;
                    mathMaster = math4Master;
                    break;
                case R.id.math5Master:
                    mathIndex = ChannelFactory.MATH5;
                    mathMaster = math5Master;
                    break;
                case R.id.math6Master:
                    mathIndex = ChannelFactory.MATH6;
                    mathMaster = math6Master;
                    break;
                case R.id.math7Master:
                    mathIndex = ChannelFactory.MATH7;
                    mathMaster = math7Master;
                    break;
                case R.id.math8Master:
                    mathIndex = ChannelFactory.MATH8;
                    mathMaster = math8Master;
                    break;
            }
            if (mathMaster == null) return;
            setRightMasterSmall(TChan.toUiChNo(mathIndex));
            setMathScope(mathIndex, mathMaster.isChecked());
            updateSelectChan(TChan.toUiChNo(mathIndex));
            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.toUiChNo(mathIndex));
            msgOthers.setMath(TChan.toUiChNo(mathIndex), mathMaster.isChecked());
            sendMsg();

            //显示垂直调节按钮
            MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
            if (!mainViewGroup.isRightSlipOtherShow()) {
                RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_VERTICAL_SCALE, v);
            }

        } else if (v.getId() == R.id.ref1Master || v.getId() == R.id.ref2Master
                || v.getId() == R.id.ref3Master || v.getId() == R.id.ref4Master
                || v.getId() == R.id.ref5Master || v.getId() == R.id.ref6Master
                || v.getId() == R.id.ref7Master || v.getId() == R.id.ref8Master
        ) {
            boolean r1Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R1);
            boolean r2Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R2);
            boolean r3Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R3);
            boolean r4Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R4);
            boolean r5Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R5);
            boolean r6Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R6);
            boolean r7Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R7);
            boolean r8Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R8);
            MainRightLayoutItemChannelMaster refMaster = null;
            int refChan = -1;
            switch (v.getId()) {
                case R.id.ref1Master:
                    refChan = TChan.R1;
                    break;
                case R.id.ref2Master:
                    refChan = TChan.R2;
                    break;
                case R.id.ref3Master:
                    refChan = TChan.R3;
                    break;
                case R.id.ref4Master:
                    refChan = TChan.R4;
                    break;
                case R.id.ref5Master:
                    refChan = TChan.R5;
                    break;
                case R.id.ref6Master:
                    refChan = TChan.R6;
                    break;
                case R.id.ref7Master:
                    refChan = TChan.R7;
                    break;
                case R.id.ref8Master:
                    refChan = TChan.R8;
                    break;
            }
            if (refChan < 0) return;
            setRightMasterSmall(refChan);
            setRefScope(TChan.toFpgaChNo(refChan), true);
            updateSelectChan(refChan);
            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, refChan);
            msgOthers.setRef(refChan, true);
            sendMsg();

            //显示垂直调节按钮
            MainViewGroup mainViewGroup = (MainViewGroup) MainHolderRightOthers.this.itemView;
            if (!mainViewGroup.isRightSlipOtherShow()) {
                RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_VERTICAL_SCALE, v);
            }
        /*    if (r1Check) {
                setRightMasterSmall(TChan.R1, true);
                setRefScope(TChan.toFpgaChNo(TChan.R1), true);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.R1);
                msgOthers.setRef1(true);
                sendMsg();
            } else if (r2Check) {
                setRightMasterSmall(TChan.R2, true);
                setRefScope(TChan.toFpgaChNo(TChan.R2), true);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.R2);
                msgOthers.setRef2(true);
                sendMsg();
            } else if (r3Check) {
                setRightMasterSmall(TChan.R3, true);
                setRefScope(TChan.toFpgaChNo(TChan.R3), true);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.R3);
                msgOthers.setRef3(true);
                sendMsg();
            } else if (r4Check) {
                setRightMasterSmall(TChan.R4, true);
                setRefScope(TChan.toFpgaChNo(TChan.R4), true);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.R4);
                msgOthers.setRef4(true);
                sendMsg();
            } else if (r5Check) {
                setRightMasterSmall(TChan.R5, true);
                setRefScope(TChan.toFpgaChNo(TChan.R5), true);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.R5);
                msgOthers.setRef5(true);
                sendMsg();
            } else if (r6Check) {
                setRightMasterSmall(TChan.R6, true);
                setRefScope(TChan.toFpgaChNo(TChan.R6), true);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.R6);
                msgOthers.setRef6(true);
                sendMsg();
            } else if (r7Check) {
                setRightMasterSmall(TChan.R7, true);
                setRefScope(TChan.toFpgaChNo(TChan.R7), true);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.R7);
                msgOthers.setRef7(true);
                sendMsg();
            } else if (r8Check) {
                setRightMasterSmall(TChan.R8, true);
                setRefScope(TChan.toFpgaChNo(TChan.R8), true);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.R8);
                msgOthers.setRef8(true);
                sendMsg();
            } else {
                if (refMaster != null) {
                    String closeSave = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + refChan);
                    if (StrUtil.isEmpty(closeSave)) {
                        refMaster.setChecked(false);
                        MainViewGroup mainViewGroup = (MainViewGroup) this.itemView;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)
                                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)
                                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)
                                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)
                                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)
                                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)
                                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)
                                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)
                        ) {
                            mainViewGroup.hideAllDialogSlip();
                        } else {
                            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slipIndex, true));
                        }
                    } else {
                        setRightMasterSmall(refChan, true);
                        setRefScope(TChan.toFpgaChNo(refChan), true);
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, refChan);
                        msgOthers.setRef(refChan, true);
                        sendMsg();
                    }
                }
            }*/
        }
        setEnableSlip();
    }

    private MainRightLayoutItemSerialsMaster.OnAllClickListener onSerialsClickListener = new MainRightLayoutItemSerialsMaster.OnAllClickListener() {
        @Override
        public void onClick(MainRightLayoutItemSerialsMaster v) {
            PlaySound.getInstance().playButton();
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
            boolean slowTimeBase = Tools.isSlowTimeBase();
            boolean serialBusTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
            switch (v.getChIndex()) {
                case ChannelFactory.S1:
                    if (zoom && slowTimeBase) {
                        break;
                    }
                    if (!s1Master.isChecked()) {
                        s1Master.setChecked(true);
                        setS1Scope(true);
                        SerialBusManage.getInstance().setVisible(TChan.S1, true);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1, String.valueOf(true));
                        msgOthers.setS1(s1Master.isChecked());
                        sendMsg();
                        if (!serialBusTxt) {
                            msgSerialsDetail.setUnOpenTriggerLevel(true);
                            msgSerialsDetail.setRightMsgSerials(msgSerials1);
                            sendMsgSerialsDetail(false);
                        }
                    } else {
                        if (!serialBusTxt) {
                            msgSerialsDetail.setUnOpenTriggerLevel(false);
                            msgSerialsDetail.setRightMsgSerials(msgSerials1);
                            sendMsgSerialsDetail(false);
                        }
                    }
                    updateSelectChan(TChan.S1);
                    break;
                case ChannelFactory.S2:
                    if (zoom && slowTimeBase) {
                        break;
                    }
                    if (!s2Master.isChecked()) {
                        s2Master.setChecked(true);
                        setS2Scope(true);
                        SerialBusManage.getInstance().setVisible(TChan.S2, true);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2, String.valueOf(true));
                        msgOthers.setS2(s2Master.isChecked());
                        sendMsg();
                        if (!serialBusTxt) {
                            msgSerialsDetail.setUnOpenTriggerLevel(true);
                            msgSerialsDetail.setRightMsgSerials(msgSerials2);
                            sendMsgSerialsDetail(false);
                        }
                    } else {
                        if (!serialBusTxt) {
                            msgSerialsDetail.setUnOpenTriggerLevel(false);
                            msgSerialsDetail.setRightMsgSerials(msgSerials2);
                            sendMsgSerialsDetail(false);
                        }
                    }
                    updateSelectChan(TChan.S2);
                    break;
                case ChannelFactory.S3:
                    if (zoom && slowTimeBase) {
                        break;
                    }
                    if (!s3Master.isChecked()) {
                        s3Master.setChecked(true);
                        setS3Scope(true);
                        SerialBusManage.getInstance().setVisible(TChan.S3, true);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3, String.valueOf(true));
                        msgOthers.setS3(s3Master.isChecked());
                        sendMsg();
                        if (!serialBusTxt) {
                            msgSerialsDetail.setUnOpenTriggerLevel(true);
                            msgSerialsDetail.setRightMsgSerials(msgSerials3);
                            sendMsgSerialsDetail(false);
                        }
                    } else {
                        if (!serialBusTxt) {
                            msgSerialsDetail.setUnOpenTriggerLevel(false);
                            msgSerialsDetail.setRightMsgSerials(msgSerials3);
                            sendMsgSerialsDetail(false);
                        }
                    }
                    updateSelectChan(TChan.S3);
                    break;
                case ChannelFactory.S4:
                    if (zoom && slowTimeBase) {
                        break;
                    }
                    if (!s4Master.isChecked()) {
                        s4Master.setChecked(true);
                        setS4Scope(true);
                        SerialBusManage.getInstance().setVisible(TChan.S4, true);
                        CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4, String.valueOf(true));
                        msgOthers.setS4(s4Master.isChecked());
                        sendMsg();
                        if (!serialBusTxt) {
                            msgSerialsDetail.setUnOpenTriggerLevel(true);
                            msgSerialsDetail.setRightMsgSerials(msgSerials4);
                            sendMsgSerialsDetail(false);
                        }
                    } else {
                        if (!serialBusTxt) {
                            msgSerialsDetail.setUnOpenTriggerLevel(false);
                            msgSerialsDetail.setRightMsgSerials(msgSerials4);
                            sendMsgSerialsDetail(false);
                        }
                    }
                    updateSelectChan(TChan.S4);
                    break;

            }
            setEnableSlip();
        }
    };


    public void updateSelectChan(int chanNumber) {
        IChan chan = MiddleMain.getIns().getChanSelectorManage().getActivityChannel();
        if (chan != IChan.toIChan(TChan.toFpgaChNo(chanNumber)) && isAddByUser(chanNumber)) {
            CursorManage.getInstance().setCursorTrace(true);
            WaveManage.get().setSelectCursor(chanNumber);
            MiddleMain.getIns().getChanSelectorManage().setActivityChannel(IChan.toIChan(TChan.toFpgaChNo(chanNumber)));
            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chanNumber);
            CursorManage.getInstance().setCursorByTimebaseTrace();
            CursorManage.getInstance().setCursorTrace(false);
        }
    }

    private boolean isAddByUser(int chanNum) {
        if (TChan.isMath(chanNum)) {
            return CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chanNum);
        } else if (TChan.isRef(chanNum)) {
            return CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chanNum);
        } else if (TChan.isSerial(chanNum)) {
            return CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(chanNum));
        } else {
            return false;
        }
    }

    private MainRightLayoutItemOthers.OnButtonClickListener onButtonClickListener = new MainRightLayoutItemOthers.OnButtonClickListener() {

        @Override
        public void onEnableFalseClick(MainRightLayoutItemOthers layout) {
            if (layout.getId() == R.id.mathLayout) {
                if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) {
                    DToast.get().show(R.string.msgOpenMathInModeXY);
                }
            }
        }

        @Override
        public boolean onNameClick(MainRightLayoutItemOthers layout, boolean checked, boolean before) {
            PlaySound.getInstance().playButton();

            return checked;
        }

        @Override
        public void onMVClick(MainRightLayoutItemOthers layout) {

        }

        @Override
        public void onVClick(MainRightLayoutItemOthers layout) {

        }

        @Override
        public void onSerialsClick(MainRightLayoutItemOthers layout) { //点击屏幕右侧串行通道按钮右边的电平框

        }
    };

    private void setChScale(int chIndex, double extent, int iUnit, boolean isShowBrief) {
        Message msg = new Message();
        switch (chIndex) {
            case ChannelFactory.MATH1:
                msg.what = MSG_PROBE_TYPE_MATH1;
                break;
            case ChannelFactory.MATH2:
                msg.what = MSG_PROBE_TYPE_MATH2;
                break;
            case ChannelFactory.MATH3:
                msg.what = MSG_PROBE_TYPE_MATH3;
                break;
            case ChannelFactory.MATH4:
                msg.what = MSG_PROBE_TYPE_MATH4;
                break;
            case ChannelFactory.MATH5:
                msg.what = MSG_PROBE_TYPE_MATH5;
                break;
            case ChannelFactory.MATH6:
                msg.what = MSG_PROBE_TYPE_MATH6;
                break;
            case ChannelFactory.MATH7:
                msg.what = MSG_PROBE_TYPE_MATH7;
                break;
            case ChannelFactory.MATH8:
                msg.what = MSG_PROBE_TYPE_MATH8;
                break;
            case ChannelFactory.REF1:
                msg.what = MSG_PROBE_TYPE_REF1;
                break;
            case ChannelFactory.REF2:
                msg.what = MSG_PROBE_TYPE_REF2;
                break;
            case ChannelFactory.REF3:
                msg.what = MSG_PROBE_TYPE_REF3;
                break;
            case ChannelFactory.REF4:
                msg.what = MSG_PROBE_TYPE_REF4;
                break;
            case ChannelFactory.REF5:
                msg.what = MSG_PROBE_TYPE_REF5;
                break;
            case ChannelFactory.REF6:
                msg.what = MSG_PROBE_TYPE_REF6;
                break;
            case ChannelFactory.REF7:
                msg.what = MSG_PROBE_TYPE_REF7;
                break;
            case ChannelFactory.REF8:
                msg.what = MSG_PROBE_TYPE_REF8;
                break;
        }
        msg.arg1 = chIndex;
        msg.obj = extent;
        if (handler.hasMessages(msg.what)) {
            handler.removeMessages(msg.what);
        }
        handler.sendMessageDelayed(msg, 200);
        //Logger.i("MainHolderRightOther," + layout.getName() + ",setChScale:" + layout.getTvScale());

        Command.get().getChannel().Extent(chIndex, extent, false);
        if (isShowBrief) {
            switch (chIndex) {
                case ChannelFactory.MATH1:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH1, 250);
                    break;
                case ChannelFactory.MATH2:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH2, 250);
                    break;
                case ChannelFactory.MATH3:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH3, 250);
                    break;
                case ChannelFactory.MATH4:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH4, 250);
                    break;
                case ChannelFactory.MATH5:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH5, 250);
                    break;
                case ChannelFactory.MATH6:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH6, 250);
                    break;
                case ChannelFactory.MATH7:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH7, 250);
                    break;
                case ChannelFactory.MATH8:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_MATH8, 250);
                    break;
                case ChannelFactory.REF1:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF1, 250);
                    break;
                case ChannelFactory.REF2:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF2, 250);
                    break;
                case ChannelFactory.REF3:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF3, 250);
                    break;
                case ChannelFactory.REF4:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF4, 250);
                    break;
                case ChannelFactory.REF5:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF5, 250);
                    break;
                case ChannelFactory.REF6:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF6, 250);
                    break;
                case ChannelFactory.REF7:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF7, 250);
                    break;
                case ChannelFactory.REF8:
                    handler.sendEmptyMessageDelayed(MSG_BRIEF_DISPLAY_REF8, 250);
                    break;
            }
        }
    }

    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum = RxBusRegister.parseMqEnum(obj);
        IChan chan = ((MQBase) obj).getChan();
        if (mqEnum == MQEnum.CH_OPEN || mqEnum == MQEnum.CH_CLOSE) {
            int chIdx = chan.getValue();
            TChan.foreachMath(mathChan -> {
                boolean math = ChannelFactory.isChOpen(TChan.toFpgaChNo(mathChan));
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan) != math) {
                    //mathMaster.setChecked(math); 改变UI和点击状态
                    Command.get().getMath().Display(TChan.toFpgaChNo(mathChan), math, false);
                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + mathChan, String.valueOf(math));
                    if (msgOthers.getMath(mathChan).isValue() != math) {
                        msgOthers.setMath(mathChan, math);
                        sendMsg();
                    }
//                    postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_MATH + mathChan, math);
                }
            });
            checkAndUpdateMathKeyState();
            boolean isOpen = mqEnum == MQEnum.CH_OPEN;
            setIsOpenSerials(chIdx, isOpen);
            setRefScope(chIdx, isOpen);
        } else if (mqEnum == MQEnum.CH_ACTIVE) {
            int chActivate = chan.getValue();
//            if (IChan.isMathToS4(chan)) {
//                if (channelLayout.getChannelSelectIndex() != chActivate) {
                    channelLayout.updateSelect(true);
//                }
//            }
//            setRightMasterSmall(chActivate + 1);
            updateSelectBackground(chan.getValue());
        }
    }

    private int lastChActivate = -1;
    private void updateSelectBackground(int chActivate) {
        if(lastChActivate == chActivate) return;
        lastChActivate = chActivate;
        View masterView = null;
        switch (chActivate) {
            case ChannelFactory.MATH1:
                masterView = math1Master;
                break;
            case ChannelFactory.MATH2:
                masterView = math2Master;
                break;
            case ChannelFactory.MATH3:
                masterView = math3Master;
                break;
            case ChannelFactory.MATH4:
                masterView = math4Master;
                break;
            case ChannelFactory.MATH5:
                masterView = math5Master;
                break;
            case ChannelFactory.MATH6:
                masterView = math6Master;
                break;
            case ChannelFactory.MATH7:
                masterView = math7Master;
                break;
            case ChannelFactory.MATH8:
                masterView = math8Master;
                break;
            case ChannelFactory.REF1:
                masterView = ref1Master;
                break;
            case ChannelFactory.REF2:
                masterView = ref2Master;
                break;
            case ChannelFactory.REF3:
                masterView = ref3Master;
                break;
            case ChannelFactory.REF4:
                masterView = ref4Master;
                break;
            case ChannelFactory.REF5:
                masterView = ref5Master;
                break;
            case ChannelFactory.REF6:
                masterView = ref6Master;
                break;
            case ChannelFactory.REF7:
                masterView = ref7Master;
                break;
            case ChannelFactory.REF8:
                masterView = ref8Master;
                break;
            case ChannelFactory.S1:
                masterView = s1Master;
                break;
            case ChannelFactory.S2:
                masterView = s2Master;
                break;
            case ChannelFactory.S3:
                masterView = s3Master;
                break;
            case ChannelFactory.S4:
                masterView = s4Master;
                break;
        }

        for (int i = 0; i < mathMasters.size(); i++) {
            mathMasters.get(i).updateBackground(masterView == mathMasters.get(i));
        }
        for (int i = 0; i < refMasters.size(); i++) {
            refMasters.get(i).updateBackground(masterView == refMasters.get(i));
        }
        for (int i = 0; i < sMasters.size(); i++) {
            sMasters.get(i).updateBackground(masterView == sMasters.get(i));
        }

        if (TChan.isMath(TChan.toUiChNo(chActivate))) {
            setMathMasterVisible(TChan.toUiChNo(chActivate), true);
        }
        if (TChan.isRef(TChan.toUiChNo(chActivate))) {
            setRefMasterVisible(TChan.toUiChNo(chActivate), true);
        }
        if (TChan.isSerial(TChan.toUiChNo(chActivate))) {
            setSerialsMasterVisible(TChan.toUiChNo(chActivate), true);
        }
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_CHANNEL_OPEN
                    || eventBase.getId() == EventFactory.EVENT_CHANNEL_CLOSE) {
//                int chIdx= (int) eventBase.getData();
//                boolean math = ChannelFactory.isChOpen(ChannelFactory.MATH_CH);
//                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH) != math) {
//                    mathMaster.setChecked(math);
//                    mathBranch.setChecked(math);
//
//                    Command.get().getMath().Display(math, false);
//                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH, String.valueOf(math));
//                    if (msgOthers.getMath().isValue() != math) {
//                        msgOthers.setMath(math);
//                        sendMsg();
//                    }
//                    postEXTERNALKEY_TOMCU(ExternalKeysMsg_ToMCU.TYPE_MATH, math);
////                    RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
////                            new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_MATH,
////                                    math ? ExternalKeysMsg_ToMCU.STATE_LED_ON : ExternalKeysMsg_ToMCU.STATE_LED_OFF));
//                }
//                boolean isOpen=eventBase.getId()==EventFactory.EVENT_CHANNEL_OPEN?true:false;
//                if (chIdx==ChannelFactory.S1){
//                    setIsOpenS1(isOpen);
//                }
//                if (chIdx==ChannelFactory.S2){
//                    setIsOpenS2(isOpen);
//                }


//                boolean refCache = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_REF);
//                if (refCache) {
//                    boolean ref1 = ChannelFactory.isChOpen(ChannelFactory.REF1);
//                    boolean ref2 = ChannelFactory.isChOpen(ChannelFactory.REF2);
//                    boolean ref3 = ChannelFactory.isChOpen(ChannelFactory.REF3);
//                    boolean ref4 = ChannelFactory.isChOpen(ChannelFactory.REF4);
//                    boolean ref = ref1 || ref2 || ref3 || ref4;
//                    msgRef.setR1Checked(ref1);
//                    msgRef.setR2Checked(ref2);
//                    msgRef.setR3Checked(ref3);
//                    msgRef.setR4Checked(ref4);
//                    refLayout.setChecked(ref);
//
//                    Command.get().getReference().Display(ref, false);
//                    CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_REF, String.valueOf(ref));
//                    if (ref) {
//                        msgOthers.setRef1(msgRef.getR1Checked().isValue());
//                        msgOthers.setRef2(msgRef.getR2Checked().isValue());
//                        msgOthers.setRef3(msgRef.getR3Checked().isValue());
//                        msgOthers.setRef4(msgRef.getR4Checked().isValue());
//                        msgOthers.getRef1().setRxMsgSelect(msgOthers.getRef1().isValue());
//                        msgOthers.getRef2().setRxMsgSelect(msgOthers.getRef2().isValue());
//                        msgOthers.getRef3().setRxMsgSelect(msgOthers.getRef3().isValue());
//                        msgOthers.getRef4().setRxMsgSelect(msgOthers.getRef4().isValue());
//                    } else {
//                        msgOthers.setRef1(false);
//                        msgOthers.setRef2(false);
//                        msgOthers.setRef3(false);
//                        msgOthers.setRef4(false);
//                    }
//                    RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
//                            new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_REF,
//                                    ref ? ExternalKeysMsg_ToMCU.STATE_LED_ON : ExternalKeysMsg_ToMCU.STATE_LED_OFF));
//                }
            } else if (eventBase.getId() == EventFactory.EVENT_CHANNEL_ACTIVE) {
//                int chActivate = ChannelFactory.getChActivate();
//                if (chActivate >= ChannelFactory.MATH_CH && chActivate <= ChannelFactory.S2) {
//                    if (channelLayout.getChannelSelectIndex() != chActivate) {
//                        channelLayout.setChannelSelectIndex(chActivate);
//                        channelLayout.updateSelect(true);
//                    }
//                }
            } else if (eventBase.getId() == EventFactory.EVENT_CHANNEL_VSCALE) {
                int chIdx = (int) eventBase.getData();
                int mathIndex = eventBase.getChannelIndex();
                if (ChannelFactory.isDynamicCh(chIdx)) {
                    if (!ChannelFactory.isMathCh(mathIndex)) return;
                    MathChannel mathChannel = ChannelFactory.getMathChannel(mathIndex);
                    if (mathChannel == null) return;
                    double vScale = mathChannel.getVScaleIdVal();
                    if (mathChannel.getMathType() == MathWave.MATH_DUALWAVE) {
                        MathDualWave dualWave = mathChannel.getMathDualWave();
                        vScale = dualWave.getVScaleIdVal(dualWave.getDefaultVScaleId());
                        if (ChannelFactory.isDynamicCh(chIdx)) {
                            //dualWave.setVScaleVal(vScale);
                            mathChannel.setVScaleVal(vScale);
                            setChScale(mathIndex, vScale, mathChannel.getProbeType(), false);
                        }
                    } else if (mathChannel.getMathType() == MathExprWave.MATH_EXPR) {
                        MathExprWave exprWave = mathChannel.getMathExprWave();
                        if (ChannelFactory.isDynamicCh(chIdx) && exprWave.isChInSample(chIdx)) {
                            vScale = exprWave.getVScaleIdVal(exprWave.getDefaultVScaleId());
                            //channel.setVScaleVal(vScale);
                            exprWave.setExprChange(true);
                            //setChScale(mathLayout, 4, vScale, channel.getProbeType(), false);
                        }
                    } else {
                        // TODO FFT的数据源通道档位发生变化导致FFT档位的相应处理
                        MathFFTWave fftWave = mathChannel.getMathFFTWave();
                        vScale = fftWave.getVScaleVal();
                        setChScale(mathIndex, vScale, mathChannel.getProbeType(), false);
                    }
                    setCacheVScaleId(mathChannel.getVScaleId(), TChan.toUiChNo(mathIndex));
                    if (mathChannel.getMathType() == MathExprWave.MATH_EXPR) {
                        MathExprWave exprWave = mathChannel.getMathExprWave();
                        vScale = exprWave.getVScaleIdVal();

                        setChScale(mathIndex, vScale, mathChannel.getProbeType(), false);
                    }
                } else if (ChannelFactory.isRefCh(chIdx)) {
                    RefChannel refChannel = ChannelFactory.getRefChannel(chIdx);
                    if (refChannel != null) {
                        MainRightLayoutItemChannelMaster refMaster = null;
                        switch (TChan.toUiChNo(chIdx)) {
                            case TChan.R1: refMaster=ref1Master; break;
                            case TChan.R2: refMaster=ref2Master; break;
                            case TChan.R3: refMaster=ref3Master; break;
                            case TChan.R4: refMaster=ref4Master; break;
                            case TChan.R5: refMaster=ref5Master; break;
                            case TChan.R6: refMaster=ref6Master; break;
                            case TChan.R7: refMaster=ref7Master; break;
                            case TChan.R8: refMaster=ref8Master; break;
                        }
                        if (refMaster != null)
                            setChScale(chIdx, refChannel.getVScaleVal(), refChannel.getProbeType(), false);
                    }
                }else if(ChannelFactory.isMathCh(chIdx)){
                    MathChannel channel = ChannelFactory.getMathChannel(chIdx);
                    double vScale = channel.getVScaleVal();
                    if (channel.isFFTDb()) return;
                    setChScale(chIdx, vScale, channel.getProbeType(), false);
                    setCacheVScaleId(channel.getVScaleId(), TChan.toUiChNo(chIdx));
                }
            } else if (eventBase.getId() == EventFactory.EVENT_MATH_FFT_SCALE) {
                int chIdx = (int)eventBase.getData();
                MathChannel mathChannel = ChannelFactory.getMathChannel(chIdx);
                if(mathChannel != null) {
                    if (mathChannel.isOpen() && !ChannelFactory.isChActivate(chIdx)
                            && ChannelFactory.isMath_FFT_Ch(chIdx)) {
                        HorizontalAxisMath horizontalAxisMath = mathChannel.getHorizontalAxisMathFFT();
                        //String fftNum = TBookUtil.getMFromDouble(horizontalAxisMath.getMathFftScaleDefault());
                        String fftNum = TBookUtil.getMFromDouble(horizontalAxisMath.fftXScaleIdVal());
//                    mathBranch.setMathRefTvTimebase(fftNum + "Hz");
                    }
                }
            }else if(eventBase.getId() == EventFactory.EVENT_MATH_SOURCE){
                int chIdx = (int)eventBase.getData();
                MathChannel channel = ChannelFactory.getMathChannel(chIdx);
                if(channel != null) {
                    double vScale = channel.getVScaleIdVal();
                    if (channel.getMathType() == MathWave.MATH_DUALWAVE) {
                        MathDualWave dualWave = channel.getMathDualWave();
                        vScale = dualWave.getVScaleIdVal(dualWave.getDefaultVScaleId());
                        channel.setVScaleVal(vScale);
                        setCacheVScaleId(channel.getVScaleId(), TChan.toUiChNo(chIdx));
                        setChScale(chIdx, vScale, channel.getProbeType(), false);
                    }
                }
            }
        }
    };

    private Consumer<RightMsgMath> consumerRightMathMsg = new Consumer<RightMsgMath>() {
        @Override
        public void accept(RightMsgMath msgMath) throws Throwable {
            PlaySound.getInstance().playButton();
            int mathChannel = msgMath.getMathChannelNumber();
            MainRightLayoutItemChannelMaster layoutMaster = null;
            switch (mathChannel) {
                case TChan.Math1: layoutMaster = math1Master;break;
                case TChan.Math2: layoutMaster = math2Master;break;
                case TChan.Math3: layoutMaster = math3Master;break;
                case TChan.Math4: layoutMaster = math4Master;break;
                case TChan.Math5: layoutMaster = math5Master;break;
                case TChan.Math6: layoutMaster = math6Master;break;
                case TChan.Math7: layoutMaster = math7Master;break;
                case TChan.Math8: layoutMaster = math8Master;break;
            }
            if (layoutMaster == null) return;
            if (msgMath.isUpClick()) {
                layoutMaster.onBtnTopClick();
            } else {
                layoutMaster.onBtnBottomClick();
            }
        }
    };

    private Consumer<RightMsgRefForEight> consumerRightRefMsg = new Consumer<RightMsgRefForEight>() {
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Throwable {
            PlaySound.getInstance().playButton();
            int refChannel = msgRef.getRefChannelNumber();
            MainRightLayoutItemChannelMaster layoutMaster = null;
            switch (refChannel) {
                case TChan.R1: layoutMaster = ref1Master;break;
                case TChan.R2: layoutMaster = ref2Master;break;
                case TChan.R3: layoutMaster = ref3Master;break;
                case TChan.R4: layoutMaster = ref4Master;break;
                case TChan.R5: layoutMaster = ref5Master;break;
                case TChan.R6: layoutMaster = ref6Master;break;
                case TChan.R7: layoutMaster = ref7Master;break;
                case TChan.R8: layoutMaster = ref8Master;break;
            }
            if (layoutMaster == null) return;
            if (msgRef.isUpClick()) {
                layoutMaster.onBtnTopClick();
            } else {
                layoutMaster.onBtnBottomClick();
            }
        }
    };

    private Consumer<Integer> consumerUpdateMasterLocation = new Consumer<Integer>() {
        @Override
        public void accept(Integer channelNumber) throws Throwable {
            View layoutMaster = null;
            switch (channelNumber) {
                case TChan.Math1: layoutMaster = math1Master;break;
                case TChan.Math2: layoutMaster = math2Master;break;
                case TChan.Math3: layoutMaster = math3Master;break;
                case TChan.Math4: layoutMaster = math4Master;break;
                case TChan.Math5: layoutMaster = math5Master;break;
                case TChan.Math6: layoutMaster = math6Master;break;
                case TChan.Math7: layoutMaster = math7Master;break;
                case TChan.Math8: layoutMaster = math8Master;break;
                case TChan.R1: layoutMaster = ref1Master;break;
                case TChan.R2: layoutMaster = ref2Master;break;
                case TChan.R3: layoutMaster = ref3Master;break;
                case TChan.R4: layoutMaster = ref4Master;break;
                case TChan.R5: layoutMaster = ref5Master;break;
                case TChan.R6: layoutMaster = ref6Master;break;
                case TChan.R7: layoutMaster = ref7Master;break;
                case TChan.R8: layoutMaster = ref8Master;break;
                case TChan.S1: layoutMaster = s1Master;break;
                case TChan.S2: layoutMaster = s2Master;break;
                case TChan.S3: layoutMaster = s3Master;break;
                case TChan.S4: layoutMaster = s4Master;break;
            }
            Logger.d(TAG, "masterCount = " + llChannelMaster.getChildCount());
            if (layoutMaster != null && llChannelMaster.getChildCount() > 0) {
                llChannelMaster.removeView(layoutMaster);
                llChannelMaster.addView(layoutMaster, llChannelMaster.getChildCount());//移动到最后
                scrollView.post(() -> {//新添加的MasterView显示出来
                    scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    needShowEdgeImg();
                });
                updateMasterCacheIndex();//这里更新保存的位置信息用以重启加载之后显示之前添加的顺序
            }
        }
    };

    private void updateMasterCacheIndex() {//更新master所在位置的cache值
        for (int i = 0; i < llChannelMaster.getChildCount(); i++) {
            View view = llChannelMaster.getChildAt(i);
            if (view instanceof MainRightLayoutItemChannelMaster) {
                MainRightLayoutItemChannelMaster mathRefMaster = (MainRightLayoutItemChannelMaster) view;
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.toUiChNo(mathRefMaster.getChIndex()), String.valueOf(i));
            } else if (view instanceof MainRightLayoutItemSerialsMaster) {
                MainRightLayoutItemSerialsMaster serialsMaster = (MainRightLayoutItemSerialsMaster) view;
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.toUiChNo(serialsMaster.getChIndex()), String.valueOf(i));
            }
        }
    }

    private void changeViewLocation() { //初始化时更新位置
        TChan.foreachMath(mathChan -> {
            int viewIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_OTHERS_CHANNEL_ORDER + mathChan);
//            Logger.d("limh", "mathChan= " + mathChan + " viewIndex= " + viewIndex);
            moveViewToIndex(llChannelMaster, mathMasters.get(TChan.toMathNumber(mathChan) - 1), viewIndex);
        });
        TChan.foreachRef(refChan -> {
            int viewIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_OTHERS_CHANNEL_ORDER + refChan);
//            Logger.d("limh", "refChan= " + refChan + " viewIndex= " + viewIndex);
            moveViewToIndex(llChannelMaster, refMasters.get(TChan.toRefNumber(refChan) - 1), viewIndex);
        });
        TChan.foreachSerial(serialChan -> {
            int viewIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_OTHERS_CHANNEL_ORDER + serialChan);
//            Logger.d("limh", "serialChan= " + serialChan + " viewIndex= " + viewIndex);
            moveViewToIndex(llChannelMaster, sMasters.get(TChan.toSerialNumber(serialChan) - 1), viewIndex);
        });
        needShowEdgeImg();
    }

    //具体移动操作
    public void moveViewToIndex(ViewGroup viewGroup, View view, int targetIndex) {
        // 先从ViewGroup中移除该View
        int currentIndex = viewGroup.indexOfChild(view);
        if (currentIndex == targetIndex) {
            // 如果已经在目标位置，则无需移动
            return;
        }
        viewGroup.removeViewAt(currentIndex);
        // 插入到新的位置
        if (targetIndex >= viewGroup.getChildCount()) {
            // 如果目标位置超过当前子View数量，直接添加到最后
            viewGroup.addView(view);
        } else {
            // 否则，在特定索引处插入
            viewGroup.addView(view, targetIndex);
        }
    }

    /**
     * 是否显示底部Math/Ref/Serials masterView两边的图片
     */
    private void needShowEdgeImg() {
        boolean needShow = llChannelMaster.getWidth() > scrollView.getWidth();
        RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_BOTTOM_OTHER_CHANNEL_EDGE_IMG, needShow);
    }

    /**
     * 当前选中的View滚动到中间
     */
    int lastMasterWidth = 0;
    private void needScroll() {
        int leftWidth = 0;
        int lastLeftWidth = 0;
        boolean needScroll = false;
        for (int i = 0; i < llChannelMaster.getChildCount(); i++) {
            View view = llChannelMaster.getChildAt(i);
            if (view instanceof MainRightLayoutItemChannelMaster) {
                MainRightLayoutItemChannelMaster mathRefMaster = (MainRightLayoutItemChannelMaster) view;
                if (mathRefMaster.getVisibility() == View.VISIBLE) {
                    lastLeftWidth = leftWidth;
                    leftWidth += (mathRefMaster.getWidth() + 4);
                }
                if (mathRefMaster.isSelect()) {
                    needScroll = true;
                    break;
                }
            } else if (view instanceof MainRightLayoutItemSerialsMaster) {
                MainRightLayoutItemSerialsMaster serialsMaster = (MainRightLayoutItemSerialsMaster) view;
                if (serialsMaster.getVisibility() == View.VISIBLE) {
                    lastLeftWidth = leftWidth;
                    leftWidth += (serialsMaster.getWidth() + 4);
                }
                if (serialsMaster.isSelect()) {
                    needScroll = true;
                    break;
                }
            }
        }
        if (!needScroll) return;
//        Logger.d(TAG, "lastLeftWidth= " + lastLeftWidth);
        int deltaX = lastLeftWidth - scrollView.getWidth() / 2 + 34;
        if (isLastChecked()) {
            deltaX += lastMasterWidth;
        }
        scrollView.smoothScrollTo(deltaX, 0);

        scrollView.setOnScrollChangeListener(new HorizontalScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //如果垂直挡位显示，还是跟着移动位置
                RxBus.getInstance().post(RxEnum.MQ_MSG_VERTICAL_SCALE_MOVE);
//                Logger.d("limh", "scrollX= " + scrollX);
            }
        });
    }

    private boolean isLastChecked() {
        boolean isLast = false;
        for (int i = llChannelMaster.getChildCount() - 1; i >= 0; i--) {
            View view = llChannelMaster.getChildAt(i);
            if (view instanceof MainRightLayoutItemChannelMaster) {
                MainRightLayoutItemChannelMaster mathRefMaster = (MainRightLayoutItemChannelMaster) view;
                if (mathRefMaster.isSelect() && mathRefMaster.getChIndex() == ChannelFactory.getChActivate()) {
                    isLast = true;
                    lastMasterWidth = mathRefMaster.getWidth();
                    break;
                }
            } else if (view instanceof MainRightLayoutItemSerialsMaster) {
                MainRightLayoutItemSerialsMaster serialsMaster = (MainRightLayoutItemSerialsMaster) view;
                if (serialsMaster.isSelect() && serialsMaster.getChIndex() == ChannelFactory.getChActivate()) {
                    isLast = true;
                    lastMasterWidth = serialsMaster.getWidth();
                    break;
                }
            }
        }
        Logger.i(TAG, "isLastCheck= " + isLast);
        return isLast;
    }


    private void setMasterVisible() {
        TChan.foreachMath(mathChan -> {
            setMathMasterVisible(mathChan, true);
        });
        TChan.foreachRef(refChan -> {
            setRefMasterVisible(refChan, true);
        });
        TChan.foreachSerial(serialChan -> {
            setSerialsMasterVisible(serialChan, true);
        });
        scrollView.post(this::needShowEdgeImg);
        scrollView.post(this::needScroll);
    }

    //Check Ref data file exists
    private boolean checkRefWaveExists(int refChan) {
        boolean isExists = false;
        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChan);
        String refFilePath = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChan);
        File file = new File(refFilePath);
        if (file.exists() || SaveRecoverySession.MSS_REF_TAG.equals(refFilePath)) {
            isExists = true;
        } else {
            CacheUtil.get().putMapInForce(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan, String.valueOf(false));
            CacheUtil.get().putMapInForce(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan, String.valueOf(false));
            WaveManage.get().setVisible(refChan, false);
        }
//        Logger.d("limh", "Ref" + TChan.toRefNumber(refChan) + " filePath= " + refFilePath + " isExists= " + isExists);
        return isExists;
    }

    private void setRefMasterVisible(int refChan, boolean needMove) {
//        boolean refWaveExists = checkRefWaveExists(refChan);
        boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan);
//        Logger.d("limh", "Ref" + TChan.toRefNumber(refChan) + " addByUser= " + refAddByUser);
        if (refAddByUser /*&& refWaveExists*/ && !isSerialsWordShow) {//非文本才可能显示Ref按钮
            refMasters.get(TChan.toRefNumber(refChan) - 1).setVisibility(View.VISIBLE);
        } else {
            refMasters.get(TChan.toRefNumber(refChan) - 1).setVisibility(View.GONE);
            if(!isSerialsWordShow) {
                clearRefMathLabel(refChan);
            }
        }
        scrollView.post(this::needShowEdgeImg);
        if(needMove) {
            scrollView.post(this::needScroll);
        }
    }


    private void clearRefMathLabel(int chan) {
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + chan, "");
        WaveManage.get().setChannelLabel(chan, "");
        setChannelLabel(TChan.toFpgaChNo(chan), "");
    }

    public void setChannelLabel(int chNo, String label) {
        IChannel channel = ChannelFactory.getValidChannel(chNo);
        if (channel != null) {
            channel.setLabel(label);
        }
    }

    private void setMathMasterVisible(int mathChan, boolean needMove) {
        boolean mathAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan);
//            Logger.d("limh", "Math" + TChan.toMathNumber(mathChan) + " addByUser= " + mathAddByUser);
        if (mathAddByUser && !isSerialsWordShow) { //非文本才可能显示数学按钮
            mathMasters.get(TChan.toMathNumber(mathChan) - 1).setVisibility(View.VISIBLE);
        } else {
            mathMasters.get(TChan.toMathNumber(mathChan) - 1).setVisibility(View.GONE);
            if(!isSerialsWordShow) {
                clearRefMathLabel(mathChan);
            }
        }
        scrollView.post(this::needShowEdgeImg);
        if(needMove) {
            scrollView.post(this::needScroll);
        }
    }

    private void setSerialsMasterVisible(int serialChan, boolean needMove) {
        boolean serialCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + TChan.toSerialNumber(serialChan));
        boolean serialAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(serialChan));
//            Logger.d("limh", "S" + TChan.toSerialNumber(serialChan) + " addByUser= " + serialAddByUser);
        if (serialAddByUser) {
            sMasters.get(TChan.toSerialNumber(serialChan) - 1).setVisibility(View.VISIBLE);
        } else {
            sMasters.get(TChan.toSerialNumber(serialChan) - 1).setVisibility(View.GONE);
        }
        scrollView.post(this::needShowEdgeImg);
        if (needMove) {
            scrollView.post(this::needScroll);
        }
    }

    private Consumer<Integer> consumerRecoverySelect = new Consumer<Integer>() {
        @Override
        public void accept(Integer recoverySelect) throws Throwable {
            if (recoverySelect == null) return;
            CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(recoverySelect));
            MiddleMain.getIns().getChanSelectorManage().setActivityChannel(IChan.toIChan(recoverySelect));
        }
    };

    private Consumer<Integer> consumerUpdateRefState = new Consumer<Integer>() {
        @Override
        public void accept(Integer channelId) throws Throwable {
            setRefMasterVisible(channelId, true);
            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(channelId));
            if (refChannel != null && ChannelFactory.isRefCh(TChan.toFpgaChNo(channelId))) {
                refChannel.setVScaleId(refChannel.getVScaleId());
                setChScale(TChan.toFpgaChNo(channelId), refChannel.getVScaleIdVal(), refChannel.getProbeType(), false);
                refMasters.get(TChan.toRefNumber(channelId) - 1).setChecked(true);
            }
            sendMsg();
        }
    };


    private void checkAndUpdateRefKeyState() {//R1--R8有打开的就亮灯 Ref不区分哪个通道打开，有开的就亮灯
        AtomicBoolean isRefLedOn = new AtomicBoolean(false);
        TChan.foreachRef(refChan -> {//添加 并且是 打开状态
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan);
            boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan);
            if (refAddByUser && refCheck) {
                isRefLedOn.set(true);
            }
        });
        RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_REF, isRefLedOn.get()
                        ? ExternalKeysMsg_ToMCU.STATE_LED_ON : ExternalKeysMsg_ToMCU.STATE_LED_OFF));
    }

    private void checkAndUpdateMathKeyState() {//Math不区分哪个通道打开，有开的就亮灯
        AtomicBoolean isMathLedOn = new AtomicBoolean(false);
        TChan.foreachMath(mathChan -> {
            boolean mathAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan);
            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan);
            if (mathAddByUser && mathCheck) {
                isMathLedOn.set(true);
            }
        });

        RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_MATH, isMathLedOn.get()
                        ? ExternalKeysMsg_ToMCU.STATE_LED_ON : ExternalKeysMsg_ToMCU.STATE_LED_OFF));
    }

    private void checkAndUpdateSerialKeyState() {//Serials不区分哪个通道打开，有开的就亮灯
        AtomicBoolean isSerialLedOn = new AtomicBoolean(false);
        TChan.foreachSerial(serialChan -> {
            boolean serialsAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(serialChan));
            boolean serialCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + TChan.toSerialNumber(serialChan));
            if (serialsAddByUser && serialCheck) {
                isSerialLedOn.set(true);
            }
        });
        RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_SERIAL, isSerialLedOn.get()
                        ? ExternalKeysMsg_ToMCU.STATE_LED_ON : ExternalKeysMsg_ToMCU.STATE_LED_OFF));
    }

    private final Consumer<String> consumerUpdateTimeBase = new Consumer<String>() {
        @Override
        public void accept(String triggerTimeBaseInfo) throws Throwable {
            Logger.d(TAG, "chActive= " + ChannelFactory.getChActivate() + " ,timePosInfo =" + triggerTimeBaseInfo);
            String timePos = triggerTimeBaseInfo.split(";")[0];
            int chIdx = Integer.parseInt(triggerTimeBaseInfo.split(";")[1]) + 1;
            setRefTimePos(chIdx, timePos);
            setMathTimePos(chIdx, timePos);
        }
    };

    private final Consumer<Integer> consumerRefTimeBase = new Consumer<Integer>() {
        @Override
        public void accept(Integer refTbIndex) throws Throwable {
            if (refTbIndex == 1) {//独立调节
                TChan.foreachRef(refChan -> {
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + refChan, "0 ns");
                    RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChan));
                    if (refChannel != null) {
                        refChannel.setDelay(0);
                    }
                });
            }
        }
    };

    private void setMathTimePos(int chanIndex, String timePos) {
        int timeBaseIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
        int index = TChan.toFpgaChNo(chanIndex);
        if (ChannelFactory.isDynamicCh(index) || (ChannelFactory.isRefCh(index) && timeBaseIndex == 0) || ChannelFactory.isMathCh(index)) {
            TChan.foreachMath(mathChan -> {
                MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
                MainRightLayoutItemChannelMaster temp = mathMasters.get(TChan.toMathNumber(mathChan) - 1);
                if (temp != null) {
                    if (mathChannel != null && mathChannel.getMathType() != MathWave.MATH_FFTWAVE) {
                        temp.setTriggerTime(timePos);
                    } else {
                        temp.setTriggerTime("");
                    }
                }
            });
        }
    }


    private void setRefTimePos(int chIdx, String timePos) {
        int timeBaseIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
        if (timeBaseIndex == 0) { //跟随
            TChan.foreachRef(refChan -> {
                RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChan));
                MainRightLayoutItemChannelMaster temp = refMasters.get(TChan.toRefNumber(refChan) - 1);
                if (temp != null) {
                    if (refChannel != null && refChannel.getRefType() != WaveData.FFT_WAVE) {
                        temp.setTriggerTime(timePos);
                    } else {
                        temp.setTriggerTime("");
                    }
                }
            });
        } else {
            MainRightLayoutItemChannelMaster refMaster = null;
            switch (chIdx) {
                case TChan.R1:
                    refMaster = ref1Master;
                    break;
                case TChan.R2:
                    refMaster = ref2Master;
                    break;
                case TChan.R3:
                    refMaster = ref3Master;
                    break;
                case TChan.R4:
                    refMaster = ref4Master;
                    break;
                case TChan.R5:
                    refMaster = ref5Master;
                    break;
                case TChan.R6:
                    refMaster = ref6Master;
                    break;
                case TChan.R7:
                    refMaster = ref7Master;
                    break;
                case TChan.R8:
                    refMaster = ref8Master;
                    break;
            }
            if (refMaster != null) {
                refMaster.setTriggerTime(timePos);
                RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(chIdx));
                if (refChannel != null && refChannel.getRefType() == WaveData.FFT_WAVE) {
                    refMaster.setTriggerTime("");
                }
            }
        }
    }


}
