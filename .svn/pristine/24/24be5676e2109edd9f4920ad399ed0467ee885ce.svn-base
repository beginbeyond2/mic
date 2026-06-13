package com.micsig.tbook.tbookscope.top.layout.trigger;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.Trigger.TriggerNEdge;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel;
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogCount;
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
public class TopLayoutTriggerNEdge extends Fragment {
    private Context context;
    private TopViewRadioGroup rgSource;
    private TopViewRadioGroup rgNEdgeSlope;
    private TextView tvNEdgeTime, tvNEdgeDetail;
    private TopDialogScale scaleDialog;
    private TopDialogCount countDialog;

    private TopMsgTriggerNEdge msgTriggerDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    private TriggerNEdge triggerNEdge;
    private ViewGroup rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_triggernedge, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        this.rootView = (ViewGroup) view;
        initView(view);
        initData();
        initControl();
    }

    private void initView(View view) {
        rgSource = (TopViewRadioGroup) view.findViewById(R.id.triggerSource);
        rgSource.setData(getResources().getString(R.string.triggerSource), GlobalVar.get().getChannelsName(), onCheckChangedListener);
        rgNEdgeSlope = (TopViewRadioGroup) view.findViewById(R.id.nEdgeSlope);
        rgNEdgeSlope.setData(R.string.triggerNEdgeSlope, R.array.triggerNEdgeSlope, onCheckChangedListener);
        tvNEdgeTime = (TextView) view.findViewById(R.id.nEdgeTime);
        tvNEdgeTime.setOnClickListener(onClickListener);
        tvNEdgeDetail = (TextView) view.findViewById(R.id.nEdgeDetail);
        tvNEdgeDetail.setOnClickListener(onClickListener);
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale);
        countDialog = (TopDialogCount) ((MainActivity) context).findViewById(R.id.dialogTopCount);

        triggerNEdge = (TriggerNEdge) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_NEDGE);
    }

    private void initData() {
        msgTriggerDetail = new TopMsgTriggerNEdge();
        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setnEdgeSlope(rgNEdgeSlope.getSelected());
        msgTriggerDetail.setnEdgeTime(tvNEdgeTime.getText().toString());
        msgTriggerDetail.setnEdgeDetail(tvNEdgeDetail.getText().toString());
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
        int slope = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_SLOPE);
        String time = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_IDLE);
        int edge = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_EDGE);

        rgSource.setSelectedIndex(source);
        rgNEdgeSlope.setSelectedIndex(slope);
        tvNEdgeTime.setText(time);
        tvNEdgeDetail.setText(String.valueOf(edge));

        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setnEdgeSlope(rgNEdgeSlope.getSelected());
        msgTriggerDetail.setnEdgeTime(tvNEdgeTime.getText().toString());
        msgTriggerDetail.setnEdgeDetail(tvNEdgeDetail.getText().toString());
        sendMsg(false);

        Command.get().getTrigger_nedge().Source(source, false);
        Command.get().getTrigger_nedge().Idle(TBookUtil.getSFromTime(time), false);
        Command.get().getTrigger_nedge().Slope(slope, false);
        Command.get().getTrigger_nedge().Edge(edge, false);

        triggerNEdge.setTriggerSource(source);
        triggerNEdge.setSlope(slope);
        triggerNEdge.setIdleTime(TBookUtil.getPsFromTime(time) / 1000);
        triggerNEdge.setEdge(edge);
    }

    public TopMsgTriggerNEdge getMsgTriggerDetail() {
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
            if(TriggerFactory.getTriggerType() == triggerNEdge.getTriggerType()){
                int idx = rgNEdgeSlope.getSelected().getIndex() + 1;
                if(idx >= rgNEdgeSlope.getArray().length){
                    idx = 0;
                }
                rgNEdgeSlope.setSelectedIndex(idx);
                onCheckChanged(rgNEdgeSlope, rgNEdgeSlope.getSelected(), false);
            }
        }
    };
    private Consumer<TopMsgTriggerChannel> consumerTriggerChannel = new Consumer<TopMsgTriggerChannel>() {
        @Override
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception {
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_NEDGE) {
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) {
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber());
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber()));
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected());
                    Command.get().getTrigger_nedge().Source(rgSource.getSelected().getIndex(),false);
                }
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerNEdge, true);
        }
    };

    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception {
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            if (triggerIndex == TopLayoutTrigger.DETAIL_NEDGE) {
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) {
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect());
                    onCheckChanged(rgSource, rgSource.getSelected(), false);
                }
                if ((msgLevel.getTopSelect() == 0) != (rgNEdgeSlope.getSelected().getIndex() == 0)) {
                    rgNEdgeSlope.setSelectedIndex(msgLevel.getTopSelect() == 0 ? 0 : 1);
                    onCheckChanged(rgNEdgeSlope, rgNEdgeSlope.getSelected(), false);
                }
            }
        }
    };

    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() {
        @Override
        public void accept(@NonNull MainMsgTriggerLevel msgTriggerLevel) throws Exception {
            if (msgTriggerLevel.isOnlyModifyNumber()) return;
            if (MainHolderTriggerLevel.LEVEL_TRIGGER_NEDGE.equals(msgTriggerLevel.getCurLevel())) {
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
                case CommandMsgToUI.FLAG_TRIGGERNEDGE_SOURCE:
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgSource, rgSource.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERNEDGE_SLOPE:
//                    if (rgNEdgeSlope.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgNEdgeSlope.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgNEdgeSlope, rgNEdgeSlope.getSelected(), false);
//                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERNEDGE_IDLE:
                    double checkBefore = Double.parseDouble(commandMsgToUI.getParam());
                    long checkTime = TopUtilScale.checkTime((long) (checkBefore * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_NEDGE_MIN, TopUtilScale.TIME_NEDGE_MAX);
                    String time = TBookUtil.getTimeFromS(checkTime * 1.0 / TopUtilScale.TIME_S2NS);
                    if (!tvNEdgeTime.getText().toString().equals(time)) {
                        onTextChanged(tvNEdgeTime, time, false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERNEDGE_EDGE:
                    if (!tvNEdgeDetail.getText().toString().equals(commandMsgToUI.getParam())) {
                        onTextChanged(tvNEdgeDetail, commandMsgToUI.getParam(), false);
                    }
                    break;
            }
        }
    };

    private EventUIObserver eventUITriggerParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_TRIGGER_PARAM) {
                int source = triggerNEdge.getTriggerSource();
                if (source != rgSource.getSelected().getIndex()) {
                    rgSource.setSelectedIndex(source);
                    onCheckChanged(rgSource, rgSource.getSelected(), true);
                }
                int slope = triggerNEdge.getSlope();
                if (slope != rgNEdgeSlope.getSelected().getIndex()) {
                    rgNEdgeSlope.setSelectedIndex(slope);
                    onCheckChanged(rgNEdgeSlope, rgNEdgeSlope.getSelected(), true);
                }
                long checkTime = TopUtilScale.checkTime(triggerNEdge.getIdleTime()
                        , TopUtilScale.TIME_NEDGE_MIN, TopUtilScale.TIME_NEDGE_MAX);
                String time = TBookUtil.getTime3FromPs(checkTime * 1000 * 10);
                if (!tvNEdgeTime.getText().toString().equals(time)) {
                    tvNEdgeTime.setText(time);
                    onTextChanged(tvNEdgeTime, time, true);
                }
                String edge = String.valueOf(triggerNEdge.getEdge());
                if (!tvNEdgeDetail.getText().toString().equals(edge)) {
                    tvNEdgeDetail.setText(edge);
                    onTextChanged(tvNEdgeDetail, edge, true);
                }
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            ScreenUtil.getViewLocation(v);
            int i = v.getId();
            if (i == R.id.nEdgeTime) {
                scaleDialog.setValue(context.getString(R.string.triggernedge_time), tvNEdgeTime.getText().toString()
                        , TopUtilScale.TIME_NEDGE_MIN, TopUtilScale.TIME_NEDGE_MAX, onScaleDismissListener);
            } else if (i == R.id.nEdgeDetail) {
                countDialog.setData(context.getString(R.string.triggerEdge), Integer.parseInt(tvNEdgeDetail.getText().toString())
                        , 65535, onCountDismissListener);
            }
        }
    };

    private TopDialogScale.OnDismissListener onScaleDismissListener = new TopDialogScale.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvNEdgeTime, result, false);
        }
    };

    private TopDialogCount.OnDismissListener onCountDismissListener = new TopDialogCount.OnDismissListener() {
        @Override
        public void onDismiss(int result) {
            onTextChanged(tvNEdgeDetail, String.valueOf(result), false);
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
        Tools.PrintControlsLocation("TopLayoutTriggerNEdge", rootView);
        if (view.getId() == R.id.triggerSource) {
            Command.get().getTrigger_nedge().Source(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerNEdge.setTriggerSource(item.getIndex());
            }
            msgTriggerDetail.setTriggerSource(item);
            sendMsg(isFromEventBus);
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL,
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_NEDGE, item.getIndex()));
        } else if (view.getId() == R.id.nEdgeSlope) {
            Command.get().getTrigger_nedge().Slope(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_SLOPE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerNEdge.setSlope(item.getIndex());
            }
            msgTriggerDetail.setnEdgeSlope(item);
            sendMsg(isFromEventBus);
        }
    }

    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) {
        if (tv.getId() == R.id.nEdgeTime) {
            Command.get().getTrigger_nedge().Idle(TBookUtil.getSFromTime(result), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_IDLE, result);
            if (!isFromEventBus) {
                triggerNEdge.setIdleTime(TBookUtil.getPsFromTime(result) / 1000);
            }
            tvNEdgeTime.setText(result);
            msgTriggerDetail.setnEdgeTime(result);
            sendMsg(isFromEventBus);
        } else if (tv.getId() == R.id.nEdgeDetail) {
            Command.get().getTrigger_nedge().Edge(Integer.parseInt(result), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_EDGE, result);
            if (!isFromEventBus) {
                triggerNEdge.setEdge(Integer.parseInt(result));
            }
            tvNEdgeDetail.setText(result);
            msgTriggerDetail.setnEdgeDetail(result);
            sendMsg(isFromEventBus);
        }
    }
}
