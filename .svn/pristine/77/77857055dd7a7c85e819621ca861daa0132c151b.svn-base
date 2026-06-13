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
import com.micsig.tbook.scope.Trigger.TriggerTimeOut;
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
public class TopLayoutTriggerTimeout extends Fragment {
    private Context context;
    private TopViewRadioGroup rgSource;
    private TopViewRadioGroup rgPolar;
    private TextView tvTime;
    private TopDialogScale scaleDialog;

    private TopMsgTriggerTimeout msgTriggerDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    private TriggerTimeOut triggerTimeOut;
    private ViewGroup rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_triggerovertime, container, false);
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
        rgPolar = (TopViewRadioGroup) view.findViewById(R.id.polar);
        rgPolar.setData(R.string.triggerTimeoutPolar, R.array.triggerTimeoutPolar, onCheckChangedListener);
        tvTime = (TextView) view.findViewById(R.id.timeoutTimeDetail);
        tvTime.setOnClickListener(onClickListener);
        scaleDialog = (TopDialogScale) ((MainActivity) context).findViewById(R.id.dialogTopScale);

        triggerTimeOut = (TriggerTimeOut) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_TIMEOUT);
    }

    private void initData() {
        msgTriggerDetail = new TopMsgTriggerTimeout();
        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setPolar(rgPolar.getSelected());
        msgTriggerDetail.setOverTimeTimeDetail(tvTime.getText().toString());
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
        int polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_TIMEOUT_POLAR);
        String overTime = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_TIMEOUT_OVERTIME);
        rgSource.setSelectedIndex(source);
        rgPolar.setSelectedIndex(polar);
        tvTime.setText(overTime);
        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setPolar(this.rgPolar.getSelected());
        msgTriggerDetail.setOverTimeTimeDetail(overTime);
        sendMsg(false);

        Command.get().getTrigger_timeout().Source(source, false);
        Command.get().getTrigger_timeout().Polarity(polar, false);
        Command.get().getTrigger_timeout().Time(TBookUtil.getSFromTime(overTime), false);

        triggerTimeOut.setTriggerSource(source);
        triggerTimeOut.setPolarity(polar);
        triggerTimeOut.setTimeOutTime(TBookUtil.getPsFromTime(overTime) / 1000);

    }

    public TopMsgTriggerTimeout getMsgTriggerDetail() {
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
            if(TriggerFactory.getTriggerType() == triggerTimeOut.getTriggerType()){
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
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_TIMEOUT) {
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) {
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber());
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber()));
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected());
                    Command.get().getTrigger_timeout().Source(rgSource.getSelected().getIndex(),false);
                }
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerTimeout, true);
        }
    };

    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception {
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            if (triggerIndex == TopLayoutTrigger.DETAIL_TIMEOUT) {
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
            if (MainHolderTriggerLevel.LEVEL_TRIGGER_TIMEOUT.equals(msgTriggerLevel.getCurLevel())) {
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
                case CommandMsgToUI.FLAG_TRIGGERTIMEOUT_SOURCE:
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgSource, rgSource.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERTIMEOUT_POLAR:
                    if (rgPolar.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgPolar.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgPolar, rgPolar.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_TRIGGERTIMEOUT_TIME:
                    double checkBefore = Double.parseDouble(commandMsgToUI.getParam());
                    long checkTime = TopUtilScale.checkTime((long) (checkBefore * TopUtilScale.TIME_S2NS)
                            , TopUtilScale.TIME_TIMEOUT_MIN, TopUtilScale.TIME_TIMEOUT_MAX);
                    String time = TBookUtil.getTimeFromS(checkTime * 1.0 / TopUtilScale.TIME_S2NS);
                    if (!tvTime.getText().toString().equals(time)) {
                        onTextChanged(tvTime, time, false);
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
                int source = triggerTimeOut.getTriggerSource();
                if (rgSource.getSelected().getIndex() != source) {
                    rgSource.setSelectedIndex(source);
                    onCheckChanged(rgSource, rgSource.getSelected(), true);
                }
                int polarity = triggerTimeOut.getPolarity();
                if (rgPolar.getSelected().getIndex() != polarity) {
                    rgPolar.setSelectedIndex(polarity);
                    onCheckChanged(rgPolar, rgPolar.getSelected(), true);
                }
                long checkBefore = triggerTimeOut.getTimeOutTime();
                String time = TBookUtil.getTime3FromPs(TopUtilScale.checkTime(checkBefore
                        , TopUtilScale.TIME_TIMEOUT_MIN, TopUtilScale.TIME_TIMEOUT_MAX) * 1000 * 10);
                if (!tvTime.getText().toString().equals(time)) {
                    onTextChanged(tvTime, time, true);
                }
            }
        }
    };

    private TopDialogScale.OnDismissListener onDismissListener = new TopDialogScale.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvTime, result, false);
        }
    };
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            ScreenUtil.getViewLocation(v);
            scaleDialog.setValue(context.getString(R.string.trigger_overTime), tvTime.getText().toString(),
                    TopUtilScale.TIME_TIMEOUT_MIN, TopUtilScale.TIME_TIMEOUT_MAX, onDismissListener);
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
        Tools.PrintControlsLocation("TopLayoutTriggerTimeout", rootView);
        if (view.getId() == R.id.triggerSource) {
            Command.get().getTrigger_timeout().Source(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerTimeOut.setTriggerSource(item.getIndex());
            }
            msgTriggerDetail.setTriggerSource(item);
            sendMsg(isFromEventBus);
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL,
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_TIMEOUT, item.getIndex()));
        } else if (view.getId() == R.id.polar) {
            Command.get().getTrigger_timeout().Polarity(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_TIMEOUT_POLAR, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerTimeOut.setPolarity(item.getIndex());
            }
            msgTriggerDetail.setPolar(item);
            sendMsg(isFromEventBus);
        }
    }

    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) {
        if (tv.getId() == tvTime.getId()) {
            Command.get().getTrigger_timeout().Time(TBookUtil.getSFromTime(result), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_TIMEOUT_OVERTIME, result);
            if (!isFromEventBus) {
                triggerTimeOut.setTimeOutTime(TBookUtil.getPsFromTime(result) / 1000);
            }
            tvTime.setText(result);
            msgTriggerDetail.setOverTimeTimeDetail(result);
            sendMsg(isFromEventBus);
        }
    }
}
