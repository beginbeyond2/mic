package com.micsig.tbook.tbookscope.top.layout.trigger;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Trigger.TriggerCommon;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogScale;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewSeekBar;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.top.view.scale.TopUtilScale;
import com.micsig.tbook.ui.util.TBookUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/10.
 */
public class TopLayoutTriggerCommon extends Fragment {
    private Context context;
    private TextView tvHoldOff;
    private TopViewRadioGroup rgMode;
    private TopDialogScale scaleDialog;

    private TopViewSeekBar triggerSensitivitySeekBar;
    private TopMsgTriggerCommon triggerDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    private TriggerCommon triggerCommon;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_triggercommon, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initView(View view) {
        tvHoldOff = (TextView) view.findViewById(R.id.holdoffTimeValue);
        tvHoldOff.setOnClickListener(onClickListener);
        rgMode = (TopViewRadioGroup) view.findViewById(R.id.mode);
        rgMode.setData(R.string.triggerMode, R.array.triggerMode, onSelectListener);
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale);
        triggerSensitivitySeekBar = view.findViewById(R.id.triggerSensitivity);
        triggerSensitivitySeekBar.setTriggerSeekBar(true);
        triggerSensitivitySeekBar.setData(R.string.TriggerSensitivity,90,45,seekBarChangeListener);
        triggerCommon = TriggerFactory.getInstance().getTriggerCommon();

    }

    private void initData() {
        triggerDetail = new TopMsgTriggerCommon();
        triggerDetail.setHoldoffTime(tvHoldOff.getText().toString());
        triggerDetail.setMode(rgMode.getSelected());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_AUTO).subscribe(consumerMainLeftMenuAuto);
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_COMMON_MODE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_COMMON_HOLDOFFTIME, eventUIObserver);
    }

    private void setCache() {
        String time = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_COMMON_TIME);
        int mode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE);
        int progress = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SENSITIVITY);
        triggerSensitivitySeekBar.setProgress(progress);
        double triggerSensitivity = 0.9-(progress/90.0) *0.9;
        triggerSensitivity += 0.1;
        triggerCommon.setTriggerSensitivity(triggerSensitivity);
        tvHoldOff.setText(time);
        this.rgMode.setSelectedIndex(mode);
        triggerDetail.setHoldoffTime(tvHoldOff.getText().toString());
        triggerDetail.setMode(this.rgMode.getSelected());

        Command.get().getTrigger().HoldOff(TBookUtil.getSFromTime(time), false);
        Command.get().getTrigger().Mode(mode, false);

        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();
        triggerCommon.setTriggerHoldOffTime(TBookUtil.get_nsFromTime(time));
        triggerCommon.setTriggerMode(mode);
    }

    public TopMsgTriggerCommon getTriggerDetail() {
        return triggerDetail;
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, isFromEventBus);
        }
    }

    private long checkTime(long ns) {
        long timeMin = TopUtilScale.TIME_COMMON_MIN;
        long timeMax = TopUtilScale.TIME_COMMON_MAX;
        ns = ns < timeMin ? timeMin : ns;
        ns = ns > timeMax ? timeMax : ns;
        return ns;
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerCommon, true);
        }
    };

    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception {
            if (msgLevel.getMiddleSelect() != rgMode.getSelected().getIndex()) {
                rgMode.setSelectedIndex(msgLevel.getMiddleSelect());
                onCheckChanged(rgMode, rgMode.getSelected(), msgLevel.isFromEventBus());
            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGER_HOLDOFF:
                    double checkBefore = Double.parseDouble(commandMsgToUI.getParam());
                    long checkTime = TopUtilScale.checkTime((long) (checkBefore * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_COMMON_MIN, TopUtilScale.TIME_COMMON_MAX);
                    String time = TBookUtil.getTimeFromS(checkTime * 1.0 / TopUtilScale.TIME_S2NS);
//                    if (!tvHoldOff.getText().toString().equals(time)) {
                        onTextChanged(tvHoldOff, time, false);
//                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGER_MODE:
                    if (rgMode.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgMode.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgMode, rgMode.getSelected(), false);
                    }
                    break;
            }
        }
    };
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { //波形亮度调节
            double triggerSensitivity = 0.9-(progress/90.0) *0.9;
            triggerSensitivity += 0.1;
            Log.d("TAG", "onProgressChanged: " + triggerSensitivity);
            triggerCommon.setTriggerSensitivity(triggerSensitivity);
            sendMsg(false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SENSITIVITY, String.valueOf(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private Consumer<Boolean> consumerMainLeftMenuAuto = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) {
                if (rgMode.getSelected().getIndex() != 0) {
                    rgMode.setSelectedIndex(0);
                    onCheckChanged(rgMode, rgMode.getSelected(), true);
                }
            }
        }
    };

    private TopDialogScale.OnDismissListener onDismissListener = new TopDialogScale.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvHoldOff, result, false);
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            scaleDialog.setValue(context.getString(R.string.view_trigger_inhibitiontimetitle), tvHoldOff.getText().toString()
                    , TopUtilScale.TIME_COMMON_MIN, TopUtilScale.TIME_COMMON_MAX, onDismissListener);
        }
    };


    private TopViewRadioGroup.OnCheckChangedListener onSelectListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {

        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            onCheckChanged(view, item, false);
        }
    };

    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        if (view.getId() == rgMode.getId()) {
            Command.get().getTrigger().Mode(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                TriggerFactory.getInstance().getTriggerCommon().setTriggerMode(item.getIndex());
            }
            triggerDetail.setMode(item);
            sendMsg(isFromEventBus);
        }
    }

    private void onTextChanged(TextView textView, String result, boolean isFromEventBus) {
        if (textView.getId() == tvHoldOff.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_COMMON_TIME, result);
            Command.get().getTrigger().HoldOff(TBookUtil.getSFromTime(result), false);
            if (!isFromEventBus) {
                TriggerFactory.getInstance().getTriggerCommon().setTriggerHoldOffTime(TBookUtil.get_nsFromTime(result));
            }
            tvHoldOff.setText(result);
            triggerDetail.setHoldoffTime(result);
            sendMsg(isFromEventBus);
        }
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();
            if (((EventBase) data).getId() == EventFactory.EVENT_TRIGGER_COMMON_MODE) {
                if (rgMode.getSelected().getIndex() != triggerCommon.getTriggerMode()) {
                    rgMode.setSelectedIndex(triggerCommon.getTriggerMode());
                    onCheckChanged(rgMode, rgMode.getSelected(), true);
                }
            } else if (((EventBase) data).getId() == EventFactory.EVENT_TRIGGER_COMMON_HOLDOFFTIME) {
                long checkTime = TopUtilScale.checkTime(triggerCommon.getTriggerHoldOffTime()
                        , TopUtilScale.TIME_COMMON_MIN, TopUtilScale.TIME_COMMON_MAX);
                String time = TBookUtil.getTime3FromPs(checkTime * 1000 * 10);
                if (!tvHoldOff.getText().toString().equals(time)) {
                    tvHoldOff.setText(time);
                    onTextChanged(tvHoldOff, tvHoldOff.getText().toString(), true);
                }
            }
        }
    };

}
