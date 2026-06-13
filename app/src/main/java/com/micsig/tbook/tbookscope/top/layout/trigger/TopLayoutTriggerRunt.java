package com.micsig.tbook.tbookscope.top.layout.trigger;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.Trigger.TriggerRunt;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel;
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogScale;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.top.view.scale.TopUtilScale;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.TBookUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/10.
 */
public class TopLayoutTriggerRunt extends Fragment {
    private Context context;
    private TopViewRadioGroup rgSource;
    private TopViewRadioGroup rgPolar;
    private TopViewRadioGroup rgCondition;
    private TextView tvTimeHigh;
    private TextView tvTimeLow;
    private LinearLayout layoutTimeHigh;
    private LinearLayout layoutTimeLow;
    private TopDialogScale scaleDialog;

    private TopMsgTriggerRunt msgTriggerDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    private TriggerRunt triggerRunt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_triggerrunt, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initView(View view) {
        rgSource = (TopViewRadioGroup) view.findViewById(R.id.triggerSource);
        rgSource.setData(getResources().getString(R.string.triggerSource), GlobalVar.get().getChannelsName(), onCheckChangedListener);
        rgPolar = (TopViewRadioGroup) view.findViewById(R.id.polar);
        rgPolar.setData(R.string.triggerRuntPolar, R.array.triggerRuntPolar, onCheckChangedListener);
        rgCondition = (TopViewRadioGroup) view.findViewById(R.id.condition);
        rgCondition.setData(R.string.triggerRuntCondition, R.array.triggerRuntCondition, onCheckChangedListener);
        tvTimeHigh = (TextView) view.findViewById(R.id.timeHighDetail);
        tvTimeLow = (TextView) view.findViewById(R.id.timeLowDetail);
        layoutTimeHigh = (LinearLayout) view.findViewById(R.id.layoutRuntTimeHigh);
        layoutTimeLow = (LinearLayout) view.findViewById(R.id.layoutRuntTimeLow);
        tvTimeHigh.setOnClickListener(onClickListener);
        tvTimeLow.setOnClickListener(onClickListener);
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale);

        triggerRunt = (TriggerRunt) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_LOW_PULSE);
    }

    private void initData() {
        msgTriggerDetail = new TopMsgTriggerRunt();
        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setPolar(rgPolar.getSelected());
        msgTriggerDetail.setCondition(rgCondition.getSelected());
        msgTriggerDetail.setTimeHighDetail(tvTimeHigh.getText().toString());
        msgTriggerDetail.setTimeLowDetail(tvTimeLow.getText().toString());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.TOPTRIGGER_CHANNEL).subscribe(consumerTriggerChannel);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL).subscribe(consumerTriggerLevel);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);

        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_EDGE).subscribe(consumerExternalkeysEdge);
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_PARAM, eventUITriggerParam);
    }

    private void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE);
        int polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_RUNT_POLAR);
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_RUNT_CONDITION);
        String timeHigh = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_HIGH);
        String timeLow = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_LOW);

        rgSource.setSelectedIndex(source);
        rgPolar.setSelectedIndex(polar);
        rgCondition.setSelectedIndex(condition);
        tvTimeHigh.setText(timeHigh);
        tvTimeLow.setText(timeLow);
        switch (condition) {
            case 0:
                layoutTimeHigh.setVisibility(View.VISIBLE);
                layoutTimeLow.setVisibility(View.INVISIBLE);
                break;
            case 1:
                layoutTimeHigh.setVisibility(View.INVISIBLE);
                layoutTimeLow.setVisibility(View.VISIBLE);
                break;
            case 2:
                layoutTimeHigh.setVisibility(View.VISIBLE);
                layoutTimeLow.setVisibility(View.VISIBLE);
                break;
            case 3:
                layoutTimeHigh.setVisibility(View.INVISIBLE);
                layoutTimeLow.setVisibility(View.INVISIBLE);
                break;
        }

        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setPolar(this.rgPolar.getSelected());
        msgTriggerDetail.setCondition(this.rgCondition.getSelected());
        msgTriggerDetail.setTimeHighDetail(timeHigh);
        msgTriggerDetail.setTimeLowDetail(timeLow);
        sendMsg(false);

        Command.get().getTrigger_dwart().Source(source, false);
        Command.get().getTrigger_dwart().Polarity(polar, false);
        Command.get().getTrigger_dwart().Condition(condition, false);
        Command.get().getTrigger_dwart().HTime(TBookUtil.getSFromTime(timeHigh), false);
        Command.get().getTrigger_dwart().LTime(TBookUtil.getSFromTime(timeLow), false);

        triggerRunt.setTriggerSource(source);
        triggerRunt.setPolarity(polar);
        triggerRunt.setCondition(TopMatchTrigger.triggerConditionToScope(condition));
        triggerRunt.setTimeHigh(TBookUtil.getPsFromTime(timeHigh) / 1000);
        triggerRunt.setTimeLow(TBookUtil.getPsFromTime(timeLow) / 1000);
    }

    public TopMsgTriggerRunt getMsgTriggerDetail() {
        return msgTriggerDetail;
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, isFromEventBus);
        }
    }

    private Consumer<Boolean> consumerExternalkeysEdge = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if(TriggerFactory.getTriggerType() == triggerRunt.getTriggerType()){
                int idx = rgPolar.getSelected().getIndex() + 1;
                if(idx >= rgPolar.getArray().length){
                    idx = 0;
                }
                rgPolar.setSelectedIndex(idx);
                onCheckChanged(rgPolar, rgPolar.getSelected(), false);
            }
        }
    };

    private Consumer<TopMsgTriggerChannel> consumerTriggerChannel = new Consumer<TopMsgTriggerChannel>() {
        @Override
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception {
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_RUNT) {
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) {
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber());
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber()));
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected());
                    Command.get().getTrigger_dwart().Source(rgSource.getSelected().getIndex(),false);
                }
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerRunt, true);
        }
    };

    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception {
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            if (triggerIndex == TopLayoutTrigger.DETAIL_RUNT) {
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) {
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect());
                    onCheckChanged(rgSource, rgSource.getSelected(), false);
                }
                if (msgLevel.getTopSelect() != rgPolar.getSelected().getIndex()) {
                    rgPolar.setSelectedIndex(msgLevel.getTopSelect());
                    onCheckChanged(rgPolar, rgPolar.getSelected(), false);
                }
            }
        }
    };

    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() {
        @Override
        public void accept(@NonNull MainMsgTriggerLevel msgTriggerLevel) throws Exception {
            if (msgTriggerLevel.isOnlyModifyNumber()) return;
            if (MainHolderTriggerLevel.LEVEL_TRIGGER_RUNT.equals(msgTriggerLevel.getCurLevel())) {
                if (rgSource.getSelected().getIndex() != msgTriggerLevel.getCurCh() - 1) {
                    rgSource.setSelectedIndex(msgTriggerLevel.getCurCh() - 1);
                    onCheckChanged(rgSource, rgSource.getSelected(), msgTriggerLevel.isFromEventBus());
                }
            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERRUNT_SOURCE:
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgSource, rgSource.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERRUNT_POLAR:
                    if (rgPolar.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgPolar.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgPolar, rgPolar.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERRUNT_CONDITION:
                    if (rgCondition.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgCondition.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgCondition, rgCondition.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERRUNT_HTIME:
                    double checkBeforeHigh = Double.parseDouble(commandMsgToUI.getParam());
                    long checkTimeHigh = TopUtilScale.checkTime((long) (checkBeforeHigh * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX);
                    String timeHigh = TBookUtil.getTimeFromS(checkTimeHigh * 1.0 / TopUtilScale.TIME_S2NS);
                    onTextChanged(tvTimeHigh, timeHigh, false);
                    break;
                case CommandMsgToUI.FLAG_TRIGGERRUNT_LOWTIME:
                    double checkBeforeLow = Double.parseDouble(commandMsgToUI.getParam());
                    long checkTimeLow = TopUtilScale.checkTime((long) (checkBeforeLow * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX);
                    String timeLow = TBookUtil.getTimeFromS(checkTimeLow * 1.0 / TopUtilScale.TIME_S2NS);
                    onTextChanged(tvTimeLow, timeLow, false);
                    break;
                case CommandMsgToUI.FLAG_TRIGGERRUNT_BTIME:
                    String[] param = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    double checkBeforeBHigh = Double.parseDouble(param[0]);
                    long checkTimeBHigh = TopUtilScale.checkTime((long) (checkBeforeBHigh * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX);
                    String timeBHigh = TBookUtil.getTimeFromS(checkTimeBHigh * 1.0 / TopUtilScale.TIME_S2NS);
                    double checkBeforeBLow = Double.parseDouble(param[1]);
                    long checkTimeBLow = TopUtilScale.checkTime((long) (checkBeforeBLow * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX);
                    String timeBLow = TBookUtil.getTimeFromS(checkTimeBLow * 1.0 / TopUtilScale.TIME_S2NS);
                    onTextChanged(tvTimeHigh, timeBHigh, false);
                    onTextChanged(tvTimeLow, timeBLow, false);
                    break;
            }
        }
    };

    private EventUIObserver eventUITriggerParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_TRIGGER_PARAM) {
                int source = triggerRunt.getTriggerSource();
                if (rgSource.getSelected().getIndex() != source) {
                    rgSource.setSelectedIndex(source);
                    onCheckChanged(rgSource, rgSource.getSelected(), true);
                }
                int polar = triggerRunt.getPolarity();
                if (rgPolar.getSelected().getIndex() != polar) {
                    rgPolar.setSelectedIndex(polar);
                    onCheckChanged(rgPolar, rgPolar.getSelected(), true);
                }
                int condition = TopMatchTrigger.triggerConditionFromScope(triggerRunt.getCondition());
                if (rgCondition.getSelected().getIndex() != condition) {
                    rgCondition.setSelectedIndex(condition);
                    onCheckChanged(rgCondition, rgCondition.getSelected(), true);
                }
                long checkTimeHigh = TopUtilScale.checkTime(triggerRunt.getTimeHigh()
                        , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX);
                String timeHigh = TBookUtil.getTime3FromPs(checkTimeHigh * 1000 * 10);
                if (!tvTimeHigh.getText().toString().equals(timeHigh)) {
                    onTextChanged(tvTimeHigh, timeHigh, true);
                }
                long checkTimeLow = TopUtilScale.checkTime(triggerRunt.getTimeLow()
                        , TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX);
                String timeLow = TBookUtil.getTime3FromPs(checkTimeLow * 1000 * 10);
                if (!tvTimeLow.getText().toString().equals(timeLow)) {
                    onTextChanged(tvTimeLow, timeLow, true);
                }
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            ScreenUtil.getViewLocation(v);
            switch (v.getId()) {
                case R.id.timeHighDetail:
                    scaleDialog.setValue(context.getString(R.string.triggerrunt_timehigh), tvTimeHigh.getText().toString(),
                            TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX, onHighDismissListener);
                    break;
                case R.id.timeLowDetail:
                    scaleDialog.setValue(context.getString(R.string.triggerrunt_timelow), tvTimeLow.getText().toString(),
                            TopUtilScale.TIME_RUNT_MIN, TopUtilScale.TIME_RUNT_MAX, onLowDismissListener);
                    break;
            }
        }
    };

    private TopDialogScale.OnDismissListener onHighDismissListener = new TopDialogScale.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvTimeHigh, result, false);
        }
    };

    private TopDialogScale.OnDismissListener onLowDismissListener = new TopDialogScale.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvTimeLow, result, false);
        }
    };


    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {
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
        if (view.getId() == R.id.triggerSource) {
            Command.get().getTrigger_dwart().Source(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerRunt.setTriggerSource(item.getIndex());
            }
            msgTriggerDetail.setTriggerSource(item);
            sendMsg(isFromEventBus);
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL,
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_RUNT, item.getIndex()));
        } else if (view.getId() == R.id.polar) {
            Command.get().getTrigger_dwart().Polarity(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_POLAR, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerRunt.setPolarity(item.getIndex());
            }
            msgTriggerDetail.setPolar(item);
            sendMsg(isFromEventBus);
        } else if (view.getId() == R.id.condition) {
            Command.get().getTrigger_dwart().Condition(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_CONDITION, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerRunt.setCondition(TopMatchTrigger.triggerConditionToScope(item.getIndex()));
            }
            msgTriggerDetail.setCondition(item);
            sendMsg(isFromEventBus);
            switch (item.getIndex()) {
                case 0:
                    layoutTimeHigh.setVisibility(View.VISIBLE);
                    layoutTimeLow.setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    layoutTimeHigh.setVisibility(View.INVISIBLE);
                    layoutTimeLow.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    layoutTimeHigh.setVisibility(View.VISIBLE);
                    layoutTimeLow.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    layoutTimeHigh.setVisibility(View.INVISIBLE);
                    layoutTimeLow.setVisibility(View.INVISIBLE);
                    break;
            }
            RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPTRIGGER_RUNT_HIGH);
        }
    }

    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) {
        if (tv.getId() == tvTimeHigh.getId()) {
            long ht = TBookUtil.getPsFromTime(result) / 1000;
            long lt = TBookUtil.getPsFromTime(tvTimeLow.getText().toString()) / 1000;
            if (ht < lt) {
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_LOW, result);
                if (!isFromEventBus) {
                    triggerRunt.setTimeLow(ht);
                }
                tvTimeLow.setText(result);
                Command.get().getTrigger_dwart().LTime(TBookUtil.getSFromTime(result), false);
                msgTriggerDetail.setTimeLowDetail(result);
            }
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_HIGH, result);
            if (!isFromEventBus) {
                triggerRunt.setTimeHigh(ht);
            }
            tvTimeHigh.setText(result);
            Command.get().getTrigger_dwart().HTime(TBookUtil.getSFromTime(result), false);
            msgTriggerDetail.setTimeHighDetail(result);
            sendMsg(isFromEventBus);
        } else if (tv.getId() == tvTimeLow.getId()) {
            long lt = TBookUtil.getPsFromTime(result) / 1000;
            long ht = TBookUtil.getPsFromTime(tvTimeHigh.getText().toString()) / 1000;
            if (ht < lt) {
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_HIGH, result);
                if (!isFromEventBus) {
                    triggerRunt.setTimeHigh(ht);
                }
                tvTimeHigh.setText(result);
                Command.get().getTrigger_dwart().HTime(TBookUtil.getSFromTime(result), false);
                msgTriggerDetail.setTimeHighDetail(result);
            }
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_RUNT_TIME_LOW, result);
            if (!isFromEventBus) {
                triggerRunt.setTimeLow(TBookUtil.getPsFromTime(result) / 1000);
            }
            tvTimeLow.setText(result);
            Command.get().getTrigger_dwart().LTime(TBookUtil.getSFromTime(result), false);
            msgTriggerDetail.setTimeLowDetail(result);
            sendMsg(isFromEventBus);
        }
    }
}
