package com.micsig.tbook.tbookscope.main.maincenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.AutoSave;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel;
import com.micsig.tbook.tbookscope.main.dialog.MainDialogMenuHalf;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.struct.ExternalKeysMsg_ToMCU;
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/10.
 */

public class MainLayoutCenterMenu extends LinearLayout {
    private static final String TAG = "MainLayoutCenterMenu";

    private static final int DATA_TIME_OUT = 0;
    private static final int DATA_TIME_WAIT = 1;

    private Context context;

    private Button centerRunStop;
    private Switch centerSeq;
    private Switch centerAuto;
    private ImageButton centerHome;
    private Button centerHalf;
    private Button centerZero;

    private DialogOkCancel dialogOk;

    private MainDialogMenuHalf dialogMenuHalf;
    private MainLeftMsgMenuRunStop msgMenuRunStop;

    public MainLayoutCenterMenu(Context context) {
        this(context, null);
    }

    public MainLayoutCenterMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainLayoutCenterMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);

    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.layout_maincenter_centermenu, this);
        setOrientation(VERTICAL);
        setClickable(true);

        centerRunStop = (Button) findViewById(R.id.centerRunStop);
        centerSeq = (Switch) findViewById(R.id.centerSeq);
        centerAuto = (Switch) findViewById(R.id.centerAuto);
        centerHome = (ImageButton) findViewById(R.id.centerHome);
        centerHalf = (Button) findViewById(R.id.centerHalf);
        centerZero = (Button) findViewById(R.id.zeroCalibrate);



        centerRunStop.setOnClickListener(onClickListener);
        centerSeq.setOnClickListener(onClickListener);
        centerAuto.setOnClickListener(onClickListener);
        centerHalf.setOnClickListener(onClickListener);
        centerHome.setOnClickListener(onClickListener);
        centerZero.setOnClickListener(onClickListener);



        msgMenuRunStop = new MainLeftMsgMenuRunStop(MainLeftMsgMenuRunStop.RUN);

        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MCUTOARM).subscribe(consumerMcuToArm);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_TO_MENU_AUTO).subscribe(consumerToMenuAuto);
        RxBus.getInstance().getObservable(RxEnum.MainLeft_To_Menu_Command).subscribe(consumerToMenuLeftCommand);
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_START, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_STOP, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_STATE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_SAMPLE_VALID, eventUIObserver);
    }

    private void setCache() {
        dialogMenuHalf = (MainDialogMenuHalf) ((MainViewGroup) getParent()).getDialog(MainViewGroup.DIALOG_MENUHALF);
        dialogMenuHalf.setOnDismissListener(onDismissListener);

        dialogOk = (DialogOkCancel) ((MainViewGroup) getParent()).getDialog(MainViewGroup.DIALOG_OKCANCEL);


        boolean runStop = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);
        boolean seq = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_SEQ);
        boolean auto = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_AUTO);

        seq = false;
        CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_SEQ, "false");
        auto = false;
        CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_AUTO, "false");

        if (runStop) {
            centerRunStop.setText(R.string.mainTopLeftStateRun);
            centerRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));
            msgMenuRunStop.setRunState(MainLeftMsgMenuRunStop.RUN);
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_RUNSTOP, ExternalKeysMsg_ToMCU.STATE_LED_GREEN));
            handler.sendEmptyMessage(DATA_TIME_WAIT);
        } else {
            centerRunStop.setText(R.string.mainTopLeftStateStop);
            centerRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));
            msgMenuRunStop.setRunState(MainLeftMsgMenuRunStop.STOP);
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_RUNSTOP, ExternalKeysMsg_ToMCU.STATE_LED_RED));
        }
        if (seq) {
            this.centerSeq.setChecked(true);
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_SEQ, ExternalKeysMsg_ToMCU.STATE_LED_ON));
        } else {
            this.centerSeq.setChecked(false);
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_SEQ, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
        }
        if (auto) {
            this.centerAuto.setChecked(true);
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_AUTO, ExternalKeysMsg_ToMCU.STATE_LED_ON));
        } else {
            this.centerAuto.setChecked(false);
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_AUTO, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
        }
        sendRunStopMsg();
    }

    private void sendRunStopMsg() {
        RxBus.getInstance().post(RxEnum.MAINLEFT_MENU_RUNSTOP, msgMenuRunStop);
    }

    private void sendAutoMsg(boolean auto) {
        RxBus.getInstance().post(RxEnum.MAINLEFT_MENU_AUTO, auto);
    }

    private boolean isAutoOpen() {
        return centerAuto.isChecked();
    }

    private boolean isSingleOpen() {
        return centerSeq.isChecked();
    }

    private boolean isRunStopOpen() {
        return centerRunStop.getText().toString().equals(getResources().getString(R.string.mainTopLeftStateRun));
    }

    private Consumer consumerLoadCache = new Consumer() {
        @Override
        public void accept(Object o) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_MainHolderLeftMenu, true);
        }
    };

    private Consumer<Integer> consumerMcuToArm = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            switch (integer) {
                case ExternalKeysCommand.MCUTOARM_RUNSTOP:
                    onClickRunStop(!isRunStopOpen());
                    break;
                case ExternalKeysCommand.MCUTOARM_SEQ:
                    onClickSeq(true);
                    break;
                case ExternalKeysCommand.MCUTOARM_AUTO:
                    onClickAuto(!isAutoOpen());
                    break;
                case ExternalKeysCommand.MCUTOARM_HOME:
                    onClickHome();
                    break;
            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_MENU_AUTO: {
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    onClickAuto(b);
                    break;
                }
                case CommandMsgToUI.FLAG_MENU_RUN: {
                    onClickRunStop(true);
                    break;
                }
                case CommandMsgToUI.FLAG_MENU_STOP: {
                    onClickRunStop(false);
                    break;
                }
                case CommandMsgToUI.FLAG_MENU_SINGLE: {
                    onClickSeq(true);
                    break;
                }
                case CommandMsgToUI.FLAG_MENU_HOMEPAGE:
                case CommandMsgToUI.FLAG_MENU_RETURN: {
                    onClickHome();
                    break;
                }
            }
        }
    };

    private Consumer<Boolean> consumerToMenuAuto = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean open) throws Exception {
            if (isAutoOpen() != open) {
                onClickAuto(open);
            }
        }
    };
    private Consumer<MainMsgCenterMenuCommand> consumerToMenuLeftCommand=new Consumer<MainMsgCenterMenuCommand>() {
        @Override
        public void accept(MainMsgCenterMenuCommand mainMsgCenterMenuCommand) throws Exception {
            switch (mainMsgCenterMenuCommand.getCommand()){
                case MainMsgCenterMenuCommand.CommandAuto:
                    centerAuto.setChecked(!isAutoOpen());
                    onClickAuto(isAutoOpen());break;
                case MainMsgCenterMenuCommand.CommandRunStop:
                    onClickRunStop(!isRunStopOpen());break;
                case MainMsgCenterMenuCommand.CommandSEQ:
                    onClickSeq(true);break;
                case MainMsgCenterMenuCommand.Command50Percent:{
                    if (mainMsgCenterMenuCommand.isOpenPercent50){
                        dialogMenuHalf.show();
                    }else {
                        dialogMenuHalf.hide();
                    }

                }break;
                case MainMsgCenterMenuCommand.CommandCalibrationZero:onZeroCalibrate(null);break;
                case MainMsgCenterMenuCommand.CommandReturnHome: onClickHome();break;
            }
        }
    };

    private MainDialogMenuHalf.OnDismissListener onDismissListener = new MainDialogMenuHalf.OnDismissListener() {
        @Override
        public void onDismiss() {

        }
    };
    private int chCapval = 50;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            switch (v.getId()) {
                case R.id.centerRunStop:
                    onClickRunStop(!isRunStopOpen());
                    break;
                case R.id.centerSeq:
                    onClickSeq(true);
                    break;
                case R.id.centerAuto:
                    onClickAuto(isAutoOpen());
                    break;
                case R.id.centerHalf:
                    dialogMenuHalf.show();
                    break;
                case R.id.zeroCalibrate:
                    onZeroCalibrate(v);
                    break;
                case R.id.centerHome:
                    onClickHome();
                    break;
            }
        }
    };
    private DialogOk.OnOkClickListener onOkClickListener = new DialogOk.OnOkClickListener() {
        @Override
        public void onClick(View v, Object data) {
            Command.get().getUserset().setAutoZero(true);
        }
    };
    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener=new DialogOkCancel.OnOkCancelClickListener() {
        @Override
        public void onOkClick(View v, Object data) {
            Command.get().getUserset().setAutoZero(true);
        }

        @Override
        public void onCancelClick(View v, Object data) {

        }

        @Override
        public void onDialogClose(View view) {}
    };

    private void onZeroCalibrate(View view){
        int chIdx = ChannelFactory.getChActivate();
        if(ChannelFactory.isDynamicCh(chIdx)) {
//            dialogOk.setData(R.string.settingcalibration_pleasesure, null, onOkClickListener);
            dialogOk.setData(view, R.string.settingcalibration_pleasesure, null, null, onOkCancelClickListener);
        }
    }

    private void onClickHome() {
        if(AutoSave.getInstance().isRun()){
            return;
        }
        if (isAutoOpen()) {
            onClickAuto(false);
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer();
        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM)) {
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_ZOOM, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
        }
        RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_RUNSTOP, ExternalKeysMsg_ToMCU.STATE_LED_RED));
        if (isSingleOpen()) {
            onClickSeq(false);
        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);
    }

    private void onClickAuto(boolean isOpen) {
        if (Scope.getInstance().isInXYMode()) {
            return;
        }
        if (isOpen) {
            if (!isRunStopOpen()) {
                onClickRunStop(true);
            }
            if (isSingleOpen()) {
                onClickSeq(false);
            }
            RxBus.getInstance().post(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE, false);//同步外部触发状态
            //关闭所有弹框和菜单
            ((MainViewGroup) getParent()).hideAllDialogSlip();
            //设置所有阈值电平初始标记位为已使用�?
            for (int serials = CacheUtil.S1; serials <= CacheUtil.S4; serials++) {
                for (int busType = CacheUtil.UART; busType <= CacheUtil.M1553B; busType++) {
                    for (int ch = TChan.Ch1; ch <= TChan.Ch8; ch++) {
                        CacheUtil.get().putMap(CacheUtil.VALUE_INIT + serials + busType + ch, String.valueOf(false));
                    }

                }
            }
            centerAuto.setChecked(true);
            CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_AUTO, "true");
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_AUTO, ExternalKeysMsg_ToMCU.STATE_LED_ON));
            Scope.getInstance().Auto(true);
            TriggerTimebase.getInstance().rstX_50percentt();
        } else {
            centerAuto.setChecked(false);
            CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_AUTO, "false");
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_AUTO, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
            Scope.getInstance().Auto(false);
        }
        sendAutoMsg(isAutoOpen());
        Command.get().getMenu().Auto(isOpen,false);
    }

    private void onClickSeq(boolean isRun) {
        if (isRun) {
            centerSeq.setChecked(true);
            CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_SEQ, "true");
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_SEQ, ExternalKeysMsg_ToMCU.STATE_LED_ON));
            if (!isRunStopOpen()) {
                centerRunStop.setText(R.string.mainTopLeftStateRun);
                centerRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));
                msgMenuRunStop.setRunState(MainLeftMsgMenuRunStop.RUN);
                CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_RUNSTOP, "true");
                RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                        new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_RUNSTOP, ExternalKeysMsg_ToMCU.STATE_LED_GREEN));
                Command.get().getFunctionMenu().Stop(false);
                //Scope.getInstance().setRun(true); //bug 0006053
                if (Scope.getInstance().isInScrollMode() && Scope.getInstance().isZoom()) {
                    RxBus.getInstance().post(RxEnum.ROLL_RUN_ZOOM, false);
                }
                sendRunStopMsg();
                handler.sendEmptyMessage(DATA_TIME_WAIT);
            }
            Scope.getInstance().setSingle(true);
        } else {
            centerSeq.setChecked(false);
            CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_SEQ, "false");
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_SEQ, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
            Command.get().getFunctionMenu().Single(false);
        }
    }

    private void onClickRunStop(boolean isRun) {
        if (!isRun) {
            if (isAutoOpen()) {
                onClickAuto(false);
            }
            if (isSingleOpen()) {
                onClickSeq(false);
            }
            Scope.getInstance().setRun(false);
            centerRunStop.setText(R.string.mainTopLeftStateStop);
            centerRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));
            msgMenuRunStop.setRunState(MainLeftMsgMenuRunStop.STOP);
            CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_RUNSTOP, "false");
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_RUNSTOP, ExternalKeysMsg_ToMCU.STATE_LED_RED));
            Command.get().getFunctionMenu().Stop(false);

            if (handler.hasMessages(DATA_TIME_WAIT)){
                handler.removeMessages(DATA_TIME_WAIT);
//                Logger.i(Command.TAG,"remove message Data_TIME_Wait");
            }
            if (handler.hasMessages(DATA_TIME_OUT)) {
                handler.removeMessages(DATA_TIME_OUT);
//                Logger.i(Command.TAG,"remove message Data_TIME_OUT");
            }
        } else {
            Log.d("liwb", String.format("onClickRunStop: "));

            centerRunStop.setText(R.string.mainTopLeftStateRun);
            centerRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));
            msgMenuRunStop.setRunState(MainLeftMsgMenuRunStop.RUN);
            CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_RUNSTOP, "true");
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_RUNSTOP, ExternalKeysMsg_ToMCU.STATE_LED_GREEN));
            Command.get().getFunctionMenu().Run(false);
            Scope.getInstance().setRun(true);
//            Log.d("liwb", String.format("onClickRunStop: isScroll:%s, isZoom:%s ,isZoom:%s",
//                    Scope.getInstance().isInScrollMode(),Scope.getInstance().isZoom(), WorkModeManage.getInstance().isZoom()));
            if (Scope.getInstance().isInScrollMode() && (Scope.getInstance().isZoom() || WorkModeManage.getInstance().isZoom()) ) {
                RxBus.getInstance().post(RxEnum.ROLL_RUN_ZOOM, false);
            }
            handler.sendEmptyMessage(DATA_TIME_WAIT);
        }
        sendRunStopMsg();
    }

    private void setSendAuto(boolean auto){
        Log.d(TAG, "setSendAuto() called with: auto = [" + auto + "]");
        ((MainActivity)context).sendAutoSave(auto);
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            Scope scope = Scope.getInstance();
            if (eventBase.getId() == EventFactory.EVENT_AUTO_START) {
                Logger.i(TAG, "eventUIObserver() ==>" + " EVENT_AUTO_START");
                setSendAuto(true);
                if (!isAutoOpen()) {
                    Logger.i(TAG, "set [centerAuto] Button MENU_RUN!!!");
                    centerAuto.setChecked(true);
                    CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_AUTO, "true");
                    RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                            new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_AUTO, ExternalKeysMsg_ToMCU.STATE_LED_ON));
                    sendAutoMsg(true);
                    Command.get().getFunctionMenu().Auto(true);
                }
            } else if (eventBase.getId() == EventFactory.EVENT_AUTO_STOP) {
                Logger.i(TAG, "eventUIObserver() ==>" + " EVENT_AUTO_STOP");
                setSendAuto(false);
                if (isAutoOpen()) {
                    Logger.i(TAG, "set [centerAuto] Button MENU_STOP!!!");
                    centerAuto.setChecked(false);
                    CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_AUTO, "false");
                    RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                            new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_AUTO, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
                    sendAutoMsg(false);
                    Command.get().getFunctionMenu().Auto(false);
                }
            } else if (eventBase.getId() == EventFactory.EVENT_SCOPE_STATE) {
                Logger.i(TAG, "eventUIObserver() ==>" + " EVENT_SCOPE_STATE"
                        + " isRun:" + scope.isRun() + " isRunStopOpen:" + isRunStopOpen());
                if (scope.isRun()) {
                    if (!isRunStopOpen()) {
                        Logger.i(TAG, "set [centerRunStop] Button MENU_RUN!!!");
                        centerRunStop.setText(R.string.mainTopLeftStateRun);
                        centerRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));
                        msgMenuRunStop.setRunState(MainLeftMsgMenuRunStop.RUN);
                        CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_RUNSTOP, "true");
                        if (Scope.getInstance().isInScrollMode() && Scope.getInstance().isZoom()) {
                            RxBus.getInstance().post(RxEnum.ROLL_RUN_ZOOM, false);
                        }
                        RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                                new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_RUNSTOP, ExternalKeysMsg_ToMCU.STATE_LED_GREEN));
                        Command.get().getFunctionMenu().Run(false);
                        sendRunStopMsg();
                        handler.sendEmptyMessage(DATA_TIME_WAIT);
                    }
                } else {
                    if (isRunStopOpen()) {
                        Logger.i(TAG, "set [centerRunStop] Button MENU_STOP!!!");
                        centerRunStop.setText(R.string.mainTopLeftStateStop);
                        centerRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));
                        msgMenuRunStop.setRunState(MainLeftMsgMenuRunStop.STOP);
                        CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_RUNSTOP, "false");
                        RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                                new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_RUNSTOP, ExternalKeysMsg_ToMCU.STATE_LED_RED));
                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_SEQ)) {
                            centerSeq.setChecked(false);
                            CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_SEQ, "false");
                            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_SEQ, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
                        }
                        Command.get().getFunctionMenu().Stop(false);
                        sendRunStopMsg();
                    }
                    if (handler.hasMessages(DATA_TIME_OUT)) {
                        handler.removeMessages(DATA_TIME_OUT);
                    }
                }
            } else if (eventBase.getId() == EventFactory.EVENT_SAMPLE_VALID) {
                if (scope.isRun()) {
                    boolean [] bSample = (boolean []) eventBase.getData();
                    if(bSample != null && bSample[0]) {
                        handler.sendEmptyMessage(DATA_TIME_WAIT);
                    }
                }
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DATA_TIME_OUT:
                    if (msgMenuRunStop.getRunState() != MainLeftMsgMenuRunStop.WAIT) {
                        msgMenuRunStop.setRunState(MainLeftMsgMenuRunStop.WAIT);
                        sendRunStopMsg();
                    }
                    break;
                case DATA_TIME_WAIT:
                    if (msgMenuRunStop.getRunState() != MainLeftMsgMenuRunStop.RUN) {
                        msgMenuRunStop.setRunState(MainLeftMsgMenuRunStop.RUN);
                        sendRunStopMsg();
                    }

                    Scope scope = Scope.getInstance();
                    double dTime = scope.timeScale_mainBoard();
                    long time = (long) (dTime * 1000) * ScopeBase.getHorizonGridCnt();
                    if (handler.hasMessages(DATA_TIME_OUT)) {
                        handler.removeMessages(DATA_TIME_OUT);
                    }
                    handler.sendEmptyMessageDelayed(DATA_TIME_OUT, (long) (time + 1500 + time * 0.1));
                    break;
            }
        }
    };

    //region 屏幕拖动控件相关
    float downX, downY;
    float moveX, moveY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                downY = ev.getRawY();
                left = (int) this.getX();
                top = (int) this.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = ev.getRawX();
                moveY = ev.getRawY();
                if (Math.abs(moveX - downX) > 5 || Math.abs(moveY - downY) > 5) {
                    return true;//如果是滑动，就自己处理，也就是交给onTouchEvent；其他情况就传下去
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    int left, top;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getRawX();
                moveY = event.getRawY();

                Rect screen = GlobalVar.get().getScreen();
                float tmpX = getX() + (moveX - downX);
                if (tmpX < screen.left) {
                    tmpX = screen.left;
                }
                if (tmpX + getWidth() > screen.right) {
                    tmpX = screen.right - getWidth();
                }

                float tmpY = getY() + (moveY - downY);
                if (tmpY < screen.top) {
                    tmpY = screen.top;
                }
                if (tmpY + getHeight() > screen.bottom) {
                    tmpY = screen.bottom - getHeight();
                }
                this.setX(tmpX);
                this.setY(tmpY);
                downX = moveX;
                downY = moveY;
                RxBus.getInstance().post(RxEnum.MAINCENTER_MENU_MOVE, 0);
                break;
            case MotionEvent.ACTION_UP:
                if (left != this.getX() || top != this.getY()) {
                    CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_MENU_X, String.valueOf((int) this.getX()));
                    CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_MENU_Y, String.valueOf((int) this.getY()));
                }
                break;
        }
        return true;
    }
    //endregion

    //region 屏幕位置相关
    public void setLocation(int x, int y) {
        setX(x);
        setY(y);
        RxBus.getInstance().post(RxEnum.MAINCENTER_MENU_MOVE, 0);
    }

    private Rect r = new Rect();

    public boolean containsPoint(int x, int y) {
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());
        return x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE;
    }

    public boolean containsDownPoint(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());
        boolean b = x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE && event.getAction() == MotionEvent.ACTION_DOWN;
        return b;
    }
    //endregion
}
