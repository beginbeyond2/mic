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
import com.micsig.tbook.scope.Trigger.TriggerSlope;
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
public class TopLayoutTriggerSlope extends Fragment {
    private Context context;
    private TopViewRadioGroup rgSource;
    private TopViewRadioGroup rgEdge;
    private TopViewRadioGroup rgCondition;
    private TextView tvTimeHigh;
    private TextView tvTimeLow;
    private LinearLayout layoutTimeHigh;
    private LinearLayout layoutTimeLow;
    private TopDialogScale scaleDialog;

    private TopMsgTriggerSlope msgTriggerDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    private TriggerSlope triggerSlope;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_triggerslope, container, false);
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
        rgEdge = (TopViewRadioGroup) view.findViewById(R.id.edge);
        rgEdge.setData(R.string.triggerSlopeEdge, R.array.triggerSlopeEdge, onCheckChangedListener);
        rgCondition = (TopViewRadioGroup) view.findViewById(R.id.condition);
        rgCondition.setData(R.string.triggerSlopeCondition, R.array.triggerSlopeCondition, onCheckChangedListener);
        tvTimeHigh = (TextView) view.findViewById(R.id.slopeTimeHighDetail);
        tvTimeLow = (TextView) view.findViewById(R.id.slopeTimeLowDetail);
        layoutTimeHigh = (LinearLayout) view.findViewById(R.id.layoutSlopeTimeHigh);
        layoutTimeLow = (LinearLayout) view.findViewById(R.id.layoutSlopeTimeLow);
        tvTimeHigh.setOnClickListener(onClickListener);
        tvTimeLow.setOnClickListener(onClickListener);
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale);

        triggerSlope = (TriggerSlope) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_SLOPE);
    }

    private void initData() {
        msgTriggerDetail = new TopMsgTriggerSlope();
        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setEdge(rgEdge.getSelected());
        msgTriggerDetail.setCondition(rgCondition.getSelected());
        msgTriggerDetail.setSlopeTimeHighDetail(tvTimeHigh.getText().toString());
        msgTriggerDetail.setSlopeTimeLowDetail(tvTimeLow.getText().toString());
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
        int edge = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_EDGE);
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_CONDITION);
        String timeHigh = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_TIME_HIGH);
        String timeLow = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_TIME_LOW);

        rgSource.setSelectedIndex(source);
        rgEdge.setSelectedIndex(edge);
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
        }

        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setEdge(this.rgEdge.getSelected());
        msgTriggerDetail.setCondition(this.rgCondition.getSelected());
        msgTriggerDetail.setSlopeTimeHighDetail(timeHigh);
        msgTriggerDetail.setSlopeTimeLowDetail(timeLow);
        sendMsg(false);

        Command.get().getTrigger_slope().Source(source, false);
        Command.get().getTrigger_slope().Edge(edge, false);
        Command.get().getTrigger_slope().Condition(condition, false);
        Command.get().getTrigger_slope().HTime(TBookUtil.getSFromTime(timeHigh), false);
        Command.get().getTrigger_dwart().LTime(TBookUtil.getSFromTime(timeLow), false);

        triggerSlope.setTriggerSource(source);
        triggerSlope.setEdge(edge);
        triggerSlope.setCondition(TopMatchTrigger.triggerConditionToScope(condition));
        triggerSlope.setTimeHigh(TBookUtil.getPsFromTime(timeHigh) / 1000);
        triggerSlope.setTimeLow(TBookUtil.getPsFromTime(timeLow) / 1000);
    }

    public TopMsgTriggerSlope getMsgTriggerDetail() {
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
            if(TriggerFactory.getTriggerType() == triggerSlope.getTriggerType()){
                int idx = rgEdge.getSelected().getIndex() + 1;
                if(idx >= rgEdge.getArray().length){
                    idx = 0;
                }
                rgEdge.setSelectedIndex(idx);
                onCheckChanged(rgEdge, rgEdge.getSelected(), false);
            }
        }
    };
    private Consumer<TopMsgTriggerChannel> consumerTriggerChannel = new Consumer<TopMsgTriggerChannel>() {
        @Override
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception {
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_SLOPE) {
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) {
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber());
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber()));
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected());
                    Command.get().getTrigger_slope().Source(rgSource.getSelected().getIndex(),false);
                }
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerSlope, true);
        }
    };

    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception {
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            if (triggerIndex == TopLayoutTrigger.DETAIL_SLOPE) {
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) {
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect());
                    onCheckChanged(rgSource, rgSource.getSelected(), false);
                }
                if (msgLevel.getTopSelect() != rgEdge.getSelected().getIndex()) {
                    rgEdge.setSelectedIndex(msgLevel.getTopSelect());
                    onCheckChanged(rgEdge, rgEdge.getSelected(), false);
                }
            }
        }
    };

    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() {
        @Override
        public void accept(@NonNull MainMsgTriggerLevel msgTriggerLevel) throws Exception {
            if (msgTriggerLevel.isOnlyModifyNumber()) return;
            if (MainHolderTriggerLevel.LEVEL_TRIGGER_SLOPE.equals(msgTriggerLevel.getCurLevel())) {
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
                case CommandMsgToUI.FLAG_TRIGGERSLOPE_SOURCE:
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgSource, rgSource.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERSLOPE_EDGE:
                    if (rgEdge.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgEdge.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgEdge, rgEdge.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERSLOPE_CONDITION:
                    if (rgCondition.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgCondition.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgCondition, rgCondition.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERSLOPE_HTIME:
                    double checkBeforeHigh = Double.parseDouble(commandMsgToUI.getParam());
                    long checkTimeHigh = TopUtilScale.checkTime((long) (checkBeforeHigh * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_SLOPE_MIN, TopUtilScale.TIME_SLOPE_MAX);
                    String timeHigh = TBookUtil.getTimeFromS(checkTimeHigh * 1.0 / TopUtilScale.TIME_S2NS);
                    onTextChanged(tvTimeHigh, timeHigh, false);
                    break;
                case CommandMsgToUI.FLAG_TRIGGERSLOPE_LTIME:
                    double checkBeforeLow = Double.parseDouble(commandMsgToUI.getParam());
                    long checkTimeLow = TopUtilScale.checkTime((long) (checkBeforeLow * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_SLOPE_MIN, TopUtilScale.TIME_SLOPE_MAX);
                    String timeLow = TBookUtil.getTimeFromS(checkTimeLow * 1.0 / TopUtilScale.TIME_S2NS);
                    onTextChanged(tvTimeLow, timeLow, false);
                    break;
                case CommandMsgToUI.FLAG_TRIGGERSLOPE_BTIME:
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    double checkBeforeBHigh = Double.parseDouble(params[0]);
                    long checkTimeBHigh = TopUtilScale.checkTime((long) (checkBeforeBHigh * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_SLOPE_MIN, TopUtilScale.TIME_SLOPE_MAX);
                    String timeBHigh = TBookUtil.getTimeFromS(checkTimeBHigh * 1.0 / TopUtilScale.TIME_S2NS);
                    double checkBeforeBLow = Double.parseDouble(params[1]);
                    long checkTimeBLow = TopUtilScale.checkTime((long) (checkBeforeBLow * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_SLOPE_MIN, TopUtilScale.TIME_SLOPE_MAX);
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
                int source = triggerSlope.getTriggerSource();
                if (rgSource.getSelected().getIndex() != source) {
                    rgSource.setSelectedIndex(source);
                    onCheckChanged(rgSource, rgSource.getSelected(), true);
                }
                int edge = triggerSlope.getEdge();
                if (rgEdge.getSelected().getIndex() != edge) {
                    rgEdge.setSelectedIndex(edge);
                    onCheckChanged(rgEdge, rgEdge.getSelected(), true);
                }
                int condition = TopMatchTrigger.triggerConditionFromScope(triggerSlope.getCondition());
                if (rgCondition.getSelected().getIndex() != condition) {
                    rgCondition.setSelectedIndex(condition);
                    onCheckChanged(rgCondition, rgCondition.getSelected(), true);
                }
                long checkTimeHigh = TopUtilScale.checkTime(triggerSlope.getTimeHigh()
                        , TopUtilScale.TIME_SLOPE_MIN, TopUtilScale.TIME_SLOPE_MAX);
                String timeHigh = TBookUtil.getTime3FromPs(checkTimeHigh * 1000 * 10);
                if (!tvTimeHigh.getText().toString().equals(timeHigh)) {
                    onTextChanged(tvTimeHigh, timeHigh, true);
                }
                long checkTimeLow = TopUtilScale.checkTime(triggerSlope.getTimeLow()
                        , TopUtilScale.TIME_SLOPE_MIN, TopUtilScale.TIME_SLOPE_MAX);
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
                case R.id.slopeTimeHighDetail:
                    scaleDialog.setValue(context.getString(R.string.triggerslope_timehighhead), tvTimeHigh.getText().toString(),
                            TopUtilScale.TIME_SLOPE_MIN, TopUtilScale.TIME_SLOPE_MAX, onHighDismissListener);
                    break;
                case R.id.slopeTimeLowDetail:
                    scaleDialog.setValue(context.getString(R.string.triggerslope_timelowhead), tvTimeLow.getText().toString(),
                            TopUtilScale.TIME_SLOPE_MIN, TopUtilScale.TIME_SLOPE_MAX, onLowDismissListener);
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
            Command.get().getTrigger_slope().Source(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerSlope.setTriggerSource(item.getIndex());
            }
            msgTriggerDetail.setTriggerSource(item);
            sendMsg(isFromEventBus);
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL,
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_SLOPE, item.getIndex()));
        } else if (view.getId() == R.id.edge) {
            Command.get().getTrigger_slope().Edge(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_EDGE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerSlope.setEdge(item.getIndex());
            }
            msgTriggerDetail.setEdge(item);
            sendMsg(isFromEventBus);
        } else if (view.getId() == R.id.condition) {
            Command.get().getTrigger_slope().Condition(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_CONDITION, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerSlope.setCondition(TopMatchTrigger.triggerConditionToScope(item.getIndex()));
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
            }
            RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPTRIGGER_SLOPE_HIGH);
        }
    }

    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) {
        if (tv.getId() == tvTimeHigh.getId()) {
            long ht = TBookUtil.getPsFromTime(result) / 1000;
            long lt = TBookUtil.getPsFromTime(tvTimeLow.getText().toString()) / 1000;
            if (ht < lt) {
                Command.get().getTrigger_dwart().LTime(TBookUtil.getSFromTime(result), false);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_TIME_LOW, result);
                if (!isFromEventBus) {
                    triggerSlope.setTimeLow(ht);
                }
                tvTimeLow.setText(result);
                msgTriggerDetail.setSlopeTimeLowDetail(result);
            }
            Command.get().getTrigger_slope().HTime(TBookUtil.getSFromTime(result), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_TIME_HIGH, result);
            if (!isFromEventBus) {
                triggerSlope.setTimeHigh(TBookUtil.getPsFromTime(result) / 1000);
            }
            tvTimeHigh.setText(result);
            msgTriggerDetail.setSlopeTimeHighDetail(result);
            sendMsg(isFromEventBus);
        } else if (tv.getId() == tvTimeLow.getId()) {
            long lt = TBookUtil.getPsFromTime(result) / 1000;
            long ht = TBookUtil.getPsFromTime(tvTimeHigh.getText().toString()) / 1000;
            if (ht < lt) {
                Command.get().getTrigger_dwart().HTime(TBookUtil.getSFromTime(result), false);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_TIME_HIGH, result);
                if (!isFromEventBus) {
                    triggerSlope.setTimeHigh(ht);
                }
                tvTimeHigh.setText(result);
                msgTriggerDetail.setSlopeTimeHighDetail(result);
            }
            Command.get().getTrigger_slope().LTime(TBookUtil.getSFromTime(result), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_TIME_LOW, result);
            if (!isFromEventBus) {
                triggerSlope.setTimeLow(TBookUtil.getPsFromTime(result) / 1000);
            }
            tvTimeLow.setText(result);
            msgTriggerDetail.setSlopeTimeLowDetail(result);
            sendMsg(isFromEventBus);
        }
    }
}
