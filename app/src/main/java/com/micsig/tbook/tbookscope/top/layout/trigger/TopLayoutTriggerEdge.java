package com.micsig.tbook.tbookscope.top.layout.trigger;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerEdge;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
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
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/10.
 */
public class TopLayoutTriggerEdge extends Fragment {
    private Context context;
    private ViewGroup layout;
    private TopViewRadioGroup rgSource;
    private TopViewRadioGroup rgEdge;
    private TopViewRadioGroup rgCouple;

    private TopMsgTriggerEdge msgTriggerDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    private TriggerEdge triggerEdge;
    private String[] channels;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_triggeredge, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        layout = (ViewGroup) view;
        initView(view);
        initData();
        initControl();
    }

    private void initView(View view) {
        rgSource = (TopViewRadioGroup) view.findViewById(R.id.triggerSource);
        String[] channels1 = GlobalVar.get().getChannelsName();
        String[] channels2 = context.getResources().getStringArray(com.micsig.tbook.ui.R.array.edgeExternalTrigger);
        channels = StrUtil.add(channels1, channels2);
        rgSource.setData(context.getResources().getString(R.string.triggerSource), channels, onSelectListener);
        rgEdge = (TopViewRadioGroup) view.findViewById(R.id.triggerEdge);
        rgEdge.setData(R.string.triggerEdge, R.array.triggerEdge, onSelectListener);
        rgCouple = (TopViewRadioGroup) view.findViewById(R.id.triggerCouple);
        rgCouple.setData(R.string.triggerCouple, R.array.triggerCouple, onSelectListener);

        triggerEdge = (TriggerEdge) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_EDGE);
    }

    private void initData() {
        msgTriggerDetail = new TopMsgTriggerEdge();
        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setTriggerEdge(rgEdge.getSelected());
        msgTriggerDetail.setTriggerCouple(rgCouple.getSelected());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.TOPTRIGGER_CHANNEL).subscribe(consumerTriggerChannel);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL).subscribe(consumerTriggerLevel);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_EDGE).subscribe(consumerExternalkeysEdge);
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_PARAM, eventUITriggerParam);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE).subscribe(consumerSyncExternalTriggerState);
    }

    private void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE);
        int edge = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_EDGE);
        int couple = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_COUPLE);

        triggerEdge.setTriggerSource(source);
        SyncExTriggerState(source);
        triggerEdge.setTriggerEdge(edge);
        triggerEdge.setTriggerCouple(couple);

        rgSource.setSelectedIndex(source);
        rgEdge.setSelectedIndex(edge);
        rgCouple.setSelectedIndex(couple);

        source=TChan.ChToExtChOfScpi(source);
        Command.get().getTrigger_edge().Source(source, false);
        Command.get().getTrigger_edge().Slope(edge, false);
        Command.get().getTrigger_edge().Couple(couple, false);

        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setTriggerEdge(rgEdge.getSelected());
        msgTriggerDetail.setTriggerCouple(rgCouple.getSelected());
        sendMsg(false);
    }

    private void SyncExTriggerState(int source) {
        Sample.getInstance().setTriggerInOut(source == channels.length - 1);
//        Logger.d("同步触发状态 edge=" + (source == channels.length - 1));
        RxBus.getInstance().post(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE, source == channels.length - 1);//同步外部触发状态
    }

    public TopMsgTriggerEdge getMsgTriggerDetail() {
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
            if(TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_EDGE){
                int idx = rgEdge.getSelected().getIndex() + 1;
                if(idx >= rgEdge.getArray().length){
                    idx = 0;
                }
                rgEdge.setSelectedIndex(idx);
                onSelectChanged(rgEdge, rgEdge.getSelected(), false);
            }
        }
    };

    private Consumer<TopMsgTriggerChannel> consumerTriggerChannel = new Consumer<TopMsgTriggerChannel>() {
        @Override
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception {
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_EDGE) {
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) {
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber());
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber()));
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected());
                    int idx=TChan.ChToExtChOfScpi(rgSource.getSelected().getIndex());
                    Command.get().getTrigger_edge().Source(idx,false);
                }
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerEdge, true);
        }
    };

    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception {
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            if (triggerIndex == TopLayoutTrigger.DETAIL_EDGE) {
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) {
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect());
                    onSelectChanged(rgSource, rgSource.getSelected(), msgLevel.isFromEventBus());
                }
                if (msgLevel.getTopSelect() != rgEdge.getSelected().getIndex()) {
                    rgEdge.setSelectedIndex(msgLevel.getTopSelect());
                    onSelectChanged(rgEdge, rgEdge.getSelected(), msgLevel.isFromEventBus());
                }
            }
        }
    };

    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() {
        @Override
        public void accept(@NonNull MainMsgTriggerLevel msgTriggerLevel) throws Exception {
            if (msgTriggerLevel.isOnlyModifyNumber()) return;
            if (MainHolderTriggerLevel.LEVEL_TRIGGER_EDGE.equals(msgTriggerLevel.getCurLevel())) {
                if (rgSource.getSelected().getIndex() != msgTriggerLevel.getCurCh() - 1) {
                    rgSource.setSelectedIndex(msgTriggerLevel.getCurCh() - 1);
                    onSelectChanged(rgSource, rgSource.getSelected(), msgTriggerLevel.isFromEventBus());
                }
            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGEREDGE_SOURCE:
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onSelectChanged(rgSource, rgSource.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGEREDGE_SLOPE:
//                    if (rgEdge.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgEdge.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onSelectChanged(rgEdge, rgEdge.getSelected(), false);
//                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGEREDGE_COUPLE:
                    if (rgCouple.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgCouple.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onSelectChanged(rgCouple, rgCouple.getSelected(), false);
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
                int source = triggerEdge.getTriggerSource();
                int edge = triggerEdge.getTriggerEdge();
                int couple = triggerEdge.getTriggerCouple();

                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE) != source) {
                    rgSource.setSelectedIndex(source);
                    onSelectChanged(rgSource, rgSource.getSelected(), true);
                }
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_EDGE) != edge) {
                    rgEdge.setSelectedIndex(edge);
                    onSelectChanged(rgEdge, rgEdge.getSelected(), true);
                }
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_COUPLE) != couple) {
                    rgCouple.setSelectedIndex(couple);
                    onSelectChanged(rgCouple, rgCouple.getSelected(), true);
                }
            }
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
            onSelectChanged(view, item, false);
        }
    };

    private void onSelectChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        Tools.PrintControlsLocation("TopLayoutTriggerEdge", layout);
        if (view.getId() == R.id.triggerSource) {
            int index=TChan.ChToExtChOfScpi(item.getIndex());
            Command.get().getTrigger_edge().Source(index, false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerEdge.setTriggerSource(item.getIndex());
                SyncExTriggerState(item.getIndex());
            }
            msgTriggerDetail.setTriggerSource(item);
            sendMsg(isFromEventBus);
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL,
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_EDGE, item.getIndex()));
        } else if (view.getId() == R.id.triggerEdge) {
            Command.get().getTrigger_edge().Slope(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_EDGE_EDGE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerEdge.setTriggerEdge(item.getIndex());
            }
            msgTriggerDetail.setTriggerEdge(item);
            sendMsg(isFromEventBus);
        } else if (view.getId() == R.id.triggerCouple) {
            Command.get().getTrigger_edge().Couple(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_EDGE_COUPLE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerEdge.setTriggerCouple(item.getIndex());
            }
            msgTriggerDetail.setTriggerCouple(item);
            sendMsg(isFromEventBus);
        }
    }

    private Consumer<Boolean> consumerSyncExternalTriggerState = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Throwable {
            int newIndex = aBoolean ? channels.length - 1 : triggerEdge.getPreTriggerSource();
            int source = rgSource.getSelected().getIndex();
            rgCouple.setEnabled(newIndex != channels.length - 1);//外部触发时触发耦合置灰
            if(source == newIndex) return;
            rgSource.setSelectedIndex(newIndex);
            onSelectChanged(rgSource, rgSource.getSelected(), false);
        }
    };

}
