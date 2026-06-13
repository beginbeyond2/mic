package com.micsig.tbook.tbookscope.top.layout.userset;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Calibrate.CalibrateService;
import com.micsig.tbook.scope.Calibrate.SelfCalibrate;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutUsersetSelfAdjust extends Fragment {
    private static final String SAVE_CURSET_KEY = "TopLayoutUsersetSelfAdjust";

    private Context context;
    private Button btnCalibration;
    private TextView tipsSelfAdjust,tipsAdjustZero;
    private DialogOk dialogOk;
    private boolean isCalibration;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_usersetselfadjust, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_SELF_CALIBRATE_BEGIN, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_SELF_CALIBRATE_END, eventUIObserver);
    }

    private void initView(View view) {
        btnCalibration = (Button) view.findViewById(R.id.adjust);
        tipsSelfAdjust = (TextView) getActivity().findViewById(R.id.tipsSelfAdjust);
        tipsAdjustZero=(TextView)getActivity().findViewById(R.id.tipsAdjustZero);
        dialogOk = (DialogOk) getActivity().findViewById(R.id.dialogOk);
        btnCalibration.setOnClickListener(onClickListener);
    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOP_USER_SELFADJUST, 1);
    }

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_USERSET_SELFADJUST:
                    startCalibration(false);
                    break;
                case CommandMsgToUI.FLAG_USERSET_AutoZero:
                    int chIdx = ChannelFactory.getChActivate();
                    if(ChannelFactory.isDynamicCh(chIdx)) {
                        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
                        activateChIdx = chIdx;
                        vScaleVal = channel.getVScaleVal()/channel.getProbeRate();
                        startCalibration(true);
                    }
                    break;
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            startCalibration(false);
        }
    };


    private int activateChIdx = -1;
    private double vScaleVal = 0;
    private boolean bSelfZeroCalibration = false;
    public void startCalibration(boolean selfZeroCalibration) {
        if (!isCalibration) {
            try {
                SaveManage.getInstance().saveUserSet(SAVE_CURSET_KEY, CacheUtil.get().getCacheMap(), null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Command.get().getUserset().setSelfAdjust(false);
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.TOPSLIP, false));//关闭TOPSLIP菜单
            RxBus.getInstance().post(RxEnum.MAINLEFT_TO_MENU_AUTO, false);//关闭Auto
            if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_YT) {//切换为YT模式
                WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, false);
            }
            this.isCalibration = true;
            CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_IS_CALIBRATION,String.valueOf(isCalibration));
            //启动校准
            //启动自校准需要锁屏和锁按键
            Logger.i("启动自校准");
            sendMsg();
            if (selfZeroCalibration){
                tipsAdjustZero.setVisibility(View.VISIBLE);
                tipsSelfAdjust.setVisibility(View.GONE);
            }else {
                tipsSelfAdjust.setVisibility(View.VISIBLE);
                tipsAdjustZero.setVisibility(View.GONE);
            }
            ScreenControls screenControls = ScreenControls.getInstance();
            screenControls.lockScreen(ScreenControls.LOCK_SELF_ADJUST);//锁定屏幕
            bSelfZeroCalibration = selfZeroCalibration;

            SelfCalibrate.getInstance().setSelfZeroCalibrate(selfZeroCalibration,activateChIdx,vScaleVal);
            SelfCalibrate.getInstance().begin();
        }
    }
    private volatile boolean bLockScreen = false;
    private volatile boolean bLockExternalKey = false;
    public void finishedSelfCalibration(final EventBase eventBase) {
        getView().post(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = (Bundle) eventBase.getData();
                int err = bundle.getInt(SelfCalibrate.ERROR_KEY);
                if (bSelfZeroCalibration) {
                    tipsAdjustZero.setVisibility(View.GONE);
                    tipsSelfAdjust.setVisibility(View.GONE);
                    if (err==0){
                        dialogOk.setData(getResources().getString(R.string.msgAutoZeroSuccess),null,onOkClickListener);
                    }else{
                        dialogOk.setData(getResources().getString(R.string.msgAutoZeroFailed),null,onOkClickListener);
                    }
                }
                else
                {
                    tipsAdjustZero.setVisibility(View.GONE);
                    tipsSelfAdjust.setVisibility(View.GONE);
                    if (err == 0) {
                        Logger.i("finished self cablibrate, sucessful!");
                        dialogOk.setData(R.string.msgSelfAdjustSuccess, null, onOkClickListener);
                    } else {
                        Logger.i("finished self cablibrate, err!!!!, errcode=" + err);
                        dialogOk.setData(String.format(getString(R.string.msgSelfAdjustFail), String.valueOf(err)), null, onOkClickListener);
                    }
                }
                ScreenControls screenControls = ScreenControls.getInstance();
                screenControls.unLockScreen(ScreenControls.LOCK_SELF_ADJUST);
                //ScreenControls.getInstance().handler.sendEmptyMessage(ScreenControls.getInstance().MSG_UnLOCKSCREEN);
                isCalibration = false;
                CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_IS_CALIBRATION,String.valueOf(isCalibration));
                Scope.getInstance().enableCommand(false);
                CacheUtil.get().initStateCacheLoad();
                try {
                    ((MainActivity) context).preMainLoadCahceProcess();
                    if (!SaveManage.getInstance().loadUserSet(SAVE_CURSET_KEY, CacheUtil.get().getCacheMap())) {
                        //配置载入失败则清空配置载入默认配置值
                        CacheUtil.get().clearCacheMap();
                        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
                        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, HorizontalAxis.TSI_2mS);
                        horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, 0);
                        horizontalAxis.correctTimePose();
//                        ExternalKeysProtocol.closeShift();
                        DToast.get().show("File is not exist!Load default config!!");
                    }

                    //刷新界面
                    TChan.foreachAllChan((chan)->{
                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(chan));
                        if(channel != null) {
                            for (int j = VerticalAxis.getMinGear(); j <= VerticalAxis.getMaxGear(); j++) {
                                CacheUtil.get().putMapInForce(CacheUtil.MAIN_WAVE_CH_Y_ZERO_POSITION + chan + j,String.valueOf(channel.getZero()));
                            }
                        }
                    });
//                    int pos = 0;
//                    Channel channel;
//                    for(int i = IWave.Ch1; i<=IWave.Ch4; i++){
//                        channel = ChannelFactory.getDynamicChannel(i-IWave.Ch1);
//                        if(channel != null) {
//                            for (int j = VerticalAxis.getMinGear(); j <= VerticalAxis.getMaxGear(); j++) {
//                                CacheUtil.get().putMapInForce(CacheUtil.MAIN_WAVE_CH_Y_ZERO_POSITION + i + j,String.valueOf(channel.getZero()));
//                            }
//                        }
//                    }

                    ((MainActivity) context).updateMainLoadCaheProcess(false);
                    ((MainActivity) context).postMainLoadCacheProcess();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    Scope.getInstance().enableCommand(true);
                }

                for (int i = 0; i < SelfCalibrate.getJIaoZhunCnt(); i++) {
                    int id = SelfCalibrate.getCablicationID(i);
                    ArrayList<String> stringList =
                            bundle.getStringArrayList(CalibrateService.getInstance().getCalibrate(id).getTAG());
                    Logger.i("<<<========================================================================================");
                    if (stringList != null) {
                        for (String x : stringList
                        ) {
                            Logger.i(x);
                        }
                    }
                }
                Logger.i("========================================================================================>>>");
            }
        });
    }

    private DialogOk.OnOkClickListener onOkClickListener = new DialogOk.OnOkClickListener() {
        @Override
        public void onClick(View v, Object data) {

        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_SELF_CALIBRATE_BEGIN) {
                Command.get().getMeasure().factoryCalibration();
                startCalibration(bSelfZeroCalibration);
            } else if (eventBase.getId() == EventFactory.EVENT_SELF_CALIBRATE_END) {
                finishedSelfCalibration(eventBase);
                isCalibration = false;
            }
        }
    };
}