package com.micsig.tbook.tbookscope.top.layout.userset;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.Trigger.TriggerCommon;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.fpga.FPGACommand;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * @auother limh
 * @description: 触发输入/输出 时钟输入/输出
 * @data:2024-11-22 14:40
 */
public class TopLayoutUserSetAuxOut extends Fragment {

    private Context context;
    private TopViewRadioGroup groupTrigger, groupClock, groupImped;
    private ViewGroup rootViewGroup;
    private DialogOkCancel dialogOk;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private boolean isRunning = false;
    private static final int clockInDetectionTime = 2000;//2秒一检测，10秒一弹窗
    private static final int clockOutDetectionTime = 30;//30毫秒

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_usersetauxout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initView(View view) {
        rootViewGroup = (ViewGroup) view;

        groupTrigger = view.findViewById(R.id.trigger);
        groupClock = view.findViewById(R.id.clock);
        groupImped = view.findViewById(R.id.input_imped);

        groupTrigger.setOnListener(onCheckChangedListener);
        groupClock.setOnListener(onCheckChangedListener);
        groupImped.setOnListener(onCheckChangedListener);

        groupTrigger.setRadioButtonOnPromptState(1, true);
        groupClock.setRadioButtonOnPromptState(1, true);

        dialogOk = getParentFragment().getActivity().findViewById(R.id.dialogOkCancel);

        groupImped.setVisibility(HardwareProduct.isMHO68V1() ? View.GONE : View.VISIBLE);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE).subscribe(consumerSyncExternalTriggerState);
    }

    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            onSelectChanged(view, item, false);
            if (view.getId() == R.id.clock) {
                if (item.getIndex() == 0) num = 0;//时钟切换到输出时
            }
        }

        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { //点击操作 有拦截
            if (view.getId() == groupTrigger.getId()) {
                showTriggerDialog();
            } else if (view.getId() == groupClock.getId()) {
                checkClockInStatus();
            }
        }
    };


    int cacheRunStop = 0;

    private void setCache() {
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_TRIGGER);
        groupTrigger.setSelectedIndex(triggerIndex);
        onCheckChangedListener.onClick(groupTrigger, groupTrigger.getSelected());

        int clockIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_CLOCK);
        //时钟启动后不保持之前状态，默认输出
        cacheRunStop  = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP) ? 1 : 2;
        groupClock.setSelectedIndex(0);
        onCheckChangedListener.onClick(groupClock, groupClock.getSelected());

        Command.get().getMenu().aux_trigger(groupTrigger.getSelected().getIndex(), false);
        Command.get().getMenu().aux_clock(groupClock.getSelected().getIndex(), false);
        Command.get().getMenu().aux_inputres(groupImped.getSelected().getIndex(),false);
        cacheRunStop = 0;
    }

    @SuppressLint("NonConstantResourceId")
    private void onSelectChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        Tools.PrintControlsLocation("TopLayoutUserSetAuxOut", rootViewGroup);
        switch (view.getId()) {
            case R.id.trigger: {
                boolean isChecked = item.getIndex() == 1;
                Sample.getInstance().setTriggerInOut(isChecked);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_TRIGGER, String.valueOf(item.getIndex()));
                RxBus.getInstance().post(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE, isChecked);//同步外部触发状态
                Command.get().getMenu().aux_trigger(item.getIndex(), false);
                updateInputImpedState(item.getIndex());
            }
            break;
            case R.id.clock: {
                stopDetection();
                boolean isChecked = item.getIndex() == 1;
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_CLOCK, String.valueOf(item.getIndex()));
                if(!isChecked) num = 0;
                setClockInOut(isChecked);
                Command.get().getMenu().aux_clock(item.getIndex(), false);
            }
            break;
            case R.id.input_imped: {
                TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();
                triggerCommon.setExtTriggerInputRes(item.getIndex());
                Command.get().getMenu().aux_inputres(item.getIndex(),false);
            }
            break;
            default:
                break;
        }
    }

    private void updateInputImpedState(int index) {
        groupImped.setEnabled(index == 1);
        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();
        if (index == 1) {
            triggerCommon.setExtTriggerInputRes(groupImped.getSelected().getIndex());
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_MENU_AUX_TRIGGER: {
                    int idx = Integer.parseInt(commandMsgToUI.getParam());
                    groupTrigger.setSelectedIndex(idx);
                    //onCheckChangedListener.onClick(groupTrigger, groupTrigger.getSelected());
                    onSelectChanged(groupTrigger,new TopBeanChannel(idx,groupTrigger.getSelectedString()),false);
                }
                break;
                case CommandMsgToUI.FLAG_MENU_AUX_CLOCK: {
                    int idx = Integer.parseInt(commandMsgToUI.getParam());
                    if (idx==groupClock.getSelected().getIndex()) return;
                    groupClock.setSelectedIndex(idx);
                    switchClock(idx==1);

//                    onCheckChangedListener.onPrompt(groupClock);
//                    onCheckChangedListener.onClick(groupClock, groupClock.getSelected());
//                    onSelectChanged(groupClock,new TopBeanChannel(idx,groupClock.getSelectedString()),false);
                }
                break;
                case CommandMsgToUI.FLAG_MENU_AUX_INPUTRES:{
                    if (groupTrigger.getSelected().getIndex()!=1) return;
                    int idx = Integer.parseInt(commandMsgToUI.getParam());
                    groupImped.setSelectedIndex(idx);
                    onSelectChanged(groupImped,new TopBeanChannel(idx,groupImped.getSelectedString()),false);

                }break;
                default:
                    break;
            }
        }
    };

    private void switchClock(boolean isIn){
        stopDetection();
        setScopeState(false);
        Sample.getInstance().setClkInOut(isIn);
        Tools.sleep(200);
        startScheduledDetection(isIn);
    }
    private Consumer<Boolean> consumerSyncExternalTriggerState = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Throwable {
            int newIndex = aBoolean ? 1 : 0;
            if (newIndex == groupTrigger.getSelected().getIndex()) return;
            groupTrigger.setSelectedIndex(newIndex);
            onCheckChangedListener.onClick(groupTrigger, groupTrigger.getSelected());
        }
    };

    private void changeClockSelect(boolean isIn) {
        int selectIndex = groupClock.getSelected().getIndex();
        TopBeanChannel itemOut = new TopBeanChannel(0, ((RadioButton) (groupClock.getRadioGroup().getChildAt(0))).getText().toString());
        TopBeanChannel itemIn = new TopBeanChannel(1, ((RadioButton) (groupClock.getRadioGroup().getChildAt(1))).getText().toString());
        if (isIn && selectIndex != 1) {
            groupClock.setSelectedIndex(1);
            onSelectChanged(groupClock, itemIn, false);
        } else if (!isIn && selectIndex != 0) {
            groupClock.setSelectedIndex(0);
            onSelectChanged(groupClock, itemOut, false);
        }
    }

    private int num = 0;

    private void checkClockInStatus() {
        boolean inStatus = Scope.getInstance().getFpgaClockInOutStatus(true);
        Logger.d("limh", "checkInStatus= " + inStatus + ",nums= " + num + ",clockDialogIsShow= " + isClockDialogShow());
        if(inStatus){
            if(!isScopeRun()){
                setScopeState(true);
            }
            hideClockDialog();
        }else{
            if(!isClockDialogShow() && num % 4 == 0){
                setScopeState(false);
                showClockDialog();
            }
        }
        startScheduledDetection(true);
        num++;
    }

    private void checkClockOutStatus() {
        boolean outStatus = Scope.getInstance().getFpgaClockInOutStatus(false);
        if (!outStatus) {
            startScheduledDetection(false);
        } else {
            setScopeState(true);
            stopDetection();
        }
    }

    private static final int DIALOG_HIDE = 0;
    private static final int DIALOG_TRIGGER_SHOW = 1;
    private static final int DIALOG_CLOCK_SHOW = 2;
    private int dialogIsShow = DIALOG_HIDE;

    private boolean isClockDialogShow(){
        return dialogIsShow == DIALOG_CLOCK_SHOW;
    }
    private void showClockDialog() {
        if(dialogIsShow == DIALOG_HIDE) {
            TopBeanChannel item0 = new TopBeanChannel(0, ((RadioButton) (groupClock.getRadioGroup().getChildAt(0))).getText().toString());
            TopBeanChannel item1 = new TopBeanChannel(1, ((RadioButton) (groupClock.getRadioGroup().getChildAt(1))).getText().toString());
            dialogOk.setData(groupClock, R.string.top_user_auxout_clock_tips, item1, item0, onOkCancelClickListener);
            dialogIsShow = DIALOG_CLOCK_SHOW;
        }
    }

    private void showTriggerDialog() {
        if(dialogIsShow == DIALOG_HIDE) {
            TopBeanChannel item0 = new TopBeanChannel(0, ((RadioButton) (groupTrigger.getRadioGroup().getChildAt(0))).getText().toString());
            TopBeanChannel item1 = new TopBeanChannel(1, ((RadioButton) (groupTrigger.getRadioGroup().getChildAt(1))).getText().toString());
            dialogOk.setData(groupTrigger, R.string.top_user_auxout_trigger_tips, item1, item0, onOkCancelClickListener);
            dialogIsShow = DIALOG_TRIGGER_SHOW;
        }
    }

    private void hideClockDialog(){
        if(dialogIsShow == DIALOG_CLOCK_SHOW) {
            hideDialog();
        }
    }

    private void hideDialog(){
        if(dialogIsShow != DIALOG_HIDE) {
            dialogIsShow = DIALOG_HIDE;
            if (dialogOk.isShow()) {
                dialogOk.hide();
            }
        }
    }

    private void startScheduledDetection(boolean isIn) {
        isRunning = true;
        runnable = () -> {
            if (isRunning) {
                if (isIn) {
                    checkClockInStatus();
                } else {
                    checkClockOutStatus();
                }
            }
        };
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(runnable, isIn ? clockInDetectionTime : clockOutDetectionTime);
    }

    private void stopDetection() {
        Log.d("limh", "stopDetection() called");
        hideClockDialog();
        isRunning = false;
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopDetection();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (groupClock.getSelected().getIndex() == 1) {
            isRunning = true;
            checkClockInStatus();
        }
    }

    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener = new DialogOkCancel.OnOkCancelClickListener() {
        @Override
        public void onOkClick(View view, Object okData) {
            if (okData == null || view == null) return;
            if (view.getId() == R.id.trigger) {
                onSelectChanged(groupTrigger, (TopBeanChannel) okData, false);
            } else if (view.getId() == R.id.clock) {
                setScopeState(false);
                changeClockSelect(true);
                startScheduledDetection(true);
            }
        }

        @Override
        public void onCancelClick(View view, Object cancelData) {
            if (cancelData == null || view == null) return;
            if (view.getId() == R.id.trigger) {
                //do nothing
            } else if (view.getId() == R.id.clock) {
                changeClockSelect(false);
                stopDetection();
                setClockInOut(false);
            }
        }

        @Override
        public void onDialogClose(View view) {
            if (view == null) return;
            if (view.getId() == R.id.clock
                    || view.getId() == R.id.trigger) {
                hideDialog();
            }
        }
    };

    boolean bScopeState = false;
    boolean bCache = false;
    private void setScopeState(boolean b){
        Scope scope = Scope.getInstance();
        if(b){
            if(!scope.isRun()) {
                if(cacheRunStop  != 2) {
                    FPGACommand.getInstance().resume();
                    Command.get().getFunctionMenu().Run(true);
                }
            }
        }else{
            if(scope.isRun()) {
                Command.get().getFunctionMenu().Stop(true);
            }
        }
        bScopeState = b;
    }

    private boolean isScopeRun(){
        return  bScopeState;
    }

    private void setClockInOut(boolean isIn) {
        setScopeState(false);
        Sample.getInstance().setClkInOut(isIn);
        ms_sleep(200);
        boolean isInOutStatus = Scope.getInstance().getFpgaClockInOutStatus(isIn);
        Logger.d("limh", "setClockIsIn= " + isIn + ",getInOutStatus= " + isInOutStatus);
        if (isInOutStatus) {
            if(!isScopeRun()){
                setScopeState(true);
            }
            if (!isIn) stopDetection();
        } else {
            startScheduledDetection(isIn);
        }
    }
    void ms_sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
