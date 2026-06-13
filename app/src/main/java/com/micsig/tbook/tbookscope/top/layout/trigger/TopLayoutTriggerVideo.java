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
import com.micsig.tbook.scope.Trigger.TriggerVideo;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogCount;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.ScreenUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/10.
 */
public class TopLayoutTriggerVideo extends Fragment {
    private static final int TRIGGERCHOICEMORE = 0;
    private static final int TRIGGERCHOICELESS = 1;
    private static final int FREQUENCYCHOICEMORE = 0;
    private static final int FREQUENCYCHOICELESS = 1;

    private Context context;
    private TopViewRadioGroup rgSource;
    private TopViewRadioGroup rgPolar;
    private TopViewRadioGroup rgStandard;
    private TopViewRadioGroup rgTrigger;
    private TopViewRadioGroup rgFrequency;
    private TextView tvLine;
    private LinearLayout layoutLine;
    private int triggerChoice, frequencyChoice;
    private TopDialogCount countDialog;

    private TopMsgTriggerVideo msgTriggerDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    private TriggerVideo triggerVideo;
    private ViewGroup rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_triggervideo, container, false);
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
        rgPolar.setData(R.string.triggerVideoPolar, R.array.triggerVideoPolar, onCheckChangedListener);
        rgStandard = (TopViewRadioGroup) view.findViewById(R.id.standard);
        rgStandard.setData(R.string.triggerVideoStandard, R.array.triggerVideoStandard, onCheckChangedListener);
        rgTrigger = (TopViewRadioGroup) view.findViewById(R.id.trigger);
        triggerChoice = TRIGGERCHOICEMORE;
        rgTrigger.setData(R.string.triggerVideotrigger, getPointArrayResId(), onCheckChangedListener);
        rgFrequency = (TopViewRadioGroup) view.findViewById(R.id.frequency);
        frequencyChoice = FREQUENCYCHOICELESS;
        rgFrequency.setData(R.string.triggerVideoFrequency, getFrequencyArrayResId(), onCheckChangedListener);
        rgFrequency.setVisibility(View.GONE);

        layoutLine = (LinearLayout) view.findViewById(R.id.lineLayout);
        layoutLine.setVisibility(View.GONE);
        tvLine = (TextView) view.findViewById(R.id.lineDetail);
        tvLine.setOnClickListener(onClickListener);
        countDialog = (TopDialogCount) ((MainActivity) context).findViewById(R.id.dialogTopCount);

        triggerVideo = (TriggerVideo) TriggerFactory.getInstance().getTrigger(Trigger.TRIG_TYPE_VIDEO);
    }

    private void initData() {
        msgTriggerDetail = new TopMsgTriggerVideo();
        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setPolar(rgPolar.getSelected());
        msgTriggerDetail.setStandard(rgStandard.getSelected());
        msgTriggerDetail.setTrigger(rgTrigger.getSelected());
        msgTriggerDetail.setFrequency(rgFrequency.getSelected());
        msgTriggerDetail.setLineDetail(tvLine.getText().toString());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.TOPTRIGGER_CHANNEL).subscribe(consumerTriggerChannel);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);

        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_EDGE).subscribe(consumerExternalkeysEdge);
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_PARAM, eventUITriggerParam);
    }

    private void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE);
        int polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_POLAR);
        int standard = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_STANDARD);
        int trigger = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_TRIGGER + standard);
        int frequency = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + standard);
        int line = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_LINE + standard);

        switch (standard) {
            case 0:
            case 1:
            case 2:
                triggerChoice = TRIGGERCHOICEMORE;
                rgFrequency.setVisibility(View.GONE);
                break;
            case 3:
                triggerChoice = TRIGGERCHOICELESS;
                frequencyChoice = FREQUENCYCHOICELESS;
                rgFrequency.setVisibility(View.VISIBLE);
                break;
            case 4:
                triggerChoice = TRIGGERCHOICEMORE;
                frequencyChoice = FREQUENCYCHOICELESS;
                rgFrequency.setVisibility(View.VISIBLE);
                break;
            case 5:
                triggerChoice = TRIGGERCHOICELESS;
                frequencyChoice = FREQUENCYCHOICEMORE;
                rgFrequency.setVisibility(View.VISIBLE);
                break;
        }
        rgTrigger.setData(R.string.triggerVideotrigger, getPointArrayResId(), onCheckChangedListener);
        rgFrequency.setData(R.string.triggerVideoFrequency, getFrequencyArrayResId(), onCheckChangedListener);

        rgSource.setSelectedIndex(source);
        rgPolar.setSelectedIndex(polar);
        rgStandard.setSelectedIndex(standard);
        rgTrigger.setSelectedIndex(trigger);
        rgFrequency.setSelectedIndex(frequency);
        tvLine.setText(String.valueOf(line));
        String[] point = getResources().getStringArray(getPointArrayResId());
        layoutLine.setVisibility(point.length - 1 == trigger ? View.VISIBLE : View.GONE);

        msgTriggerDetail.setTriggerSource(rgSource.getSelected());
        msgTriggerDetail.setPolar(rgPolar.getSelected());
        msgTriggerDetail.setStandard(rgStandard.getSelected());
        msgTriggerDetail.setTrigger(rgTrigger.getSelected());
        msgTriggerDetail.setFrequency(rgFrequency.getSelected());
        msgTriggerDetail.setLineDetail(String.valueOf(line));
        sendMsg(false);

        Command.get().getTrigger_video().Source(source, false);
        Command.get().getTrigger_video().Polarity(polar, false);
        Command.get().getTrigger_video().Standard(standard, false);
        Command.get().getTrigger_video().Line(line, false);
        if (standard == 3 || standard == 5) {
            Command.get().getTrigger_video().Bmode(trigger, false);
        } else {
            Command.get().getTrigger_video().Amode(trigger, false);
        }
        if (standard == 3 || standard == 4) {
            Command.get().getTrigger_video().Afrequence(frequency, false);
        } else if (standard == 5) {
            Command.get().getTrigger_video().Bfrequence(frequency, false);
        }

        triggerVideo.setTriggerSource(source);
        triggerVideo.setPolarity(polar);
        triggerVideo.setStandard(standard);
        triggerVideo.setVideoTrigger(TopMatchTrigger.triggerVideoTriggerToScope(standard, trigger));
        triggerVideo.setVideoFrequency(TopMatchTrigger.triggerVideoFrequencyToScope(standard, frequency));
        triggerVideo.setLine(line);
    }

    private int getPointArrayResId() {
        return triggerChoice == TRIGGERCHOICEMORE ? R.array.triggerVideoTriggerMore : R.array.triggerVideoTriggerLess;
    }

    private int getFrequencyArrayResId() {
        return frequencyChoice == FREQUENCYCHOICEMORE ? R.array.triggerVideoFrequencyMore : R.array.triggerVideoFrequencyLess;
    }

    public TopMsgTriggerVideo getMsgTriggerDetail() {
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
            if(TriggerFactory.getTriggerType() == triggerVideo.getTriggerType()){
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
            if (topMsgTriggerChannel.getTriggerType() != TopLayoutTrigger.DETAIL_VIDEO) {
                if (rgSource.getSelected().getIndex() != topMsgTriggerChannel.getChNumber()) {
                    rgSource.setSelectedIndex(topMsgTriggerChannel.getChNumber());
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(topMsgTriggerChannel.getChNumber()));
                    msgTriggerDetail.setTriggerSource(rgSource.getSelected());
                    Command.get().getTrigger_video().Source(rgSource.getSelected().getIndex(),false);
                }
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerVideo, true);
        }
    };

    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception {
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            if (triggerIndex == TopLayoutTrigger.DETAIL_VIDEO) {
                if (msgLevel.getBottomSelect() != rgSource.getSelected().getIndex()) {
                    rgSource.setSelectedIndex(msgLevel.getBottomSelect());
                    onCheckChanged(rgSource, rgSource.getSelected(), false);
                }
                if ((msgLevel.getTopSelect() == 0) != (rgPolar.getSelected().getIndex() == 0)) {
                    rgPolar.setSelectedIndex(msgLevel.getTopSelect() == 0 ? 0 : 1);
                    onCheckChanged(rgPolar, rgPolar.getSelected(), false);
                }
            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_SOURCE: {
                    if (rgSource.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgSource.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgSource, rgSource.getSelected(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_POLAR: {
                    if (rgPolar.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgPolar.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgPolar, rgPolar.getSelected(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_STANDARD: {
                    if (rgStandard.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        rgStandard.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgStandard, rgStandard.getSelected(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_AMODE: {
                    int param= Integer.parseInt(commandMsgToUI.getParam());
                    if (triggerChoice==TRIGGERCHOICELESS){
                        param-=2; //因为合并模式，5个变3个，还是前面的两个消逝，下面的频率不需要，是因为频率消失的是后面3个，不影响索引
                    }
                        rgTrigger.setSelectedIndex(param);
                        onCheckChanged(rgTrigger, rgTrigger.getSelected(), false);
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_BMODE: {
                    if (rgTrigger.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())
                            /*&& triggerChoice == TRIGGERCHOICELESS*/) {
                        rgTrigger.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgTrigger, rgTrigger.getSelected(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_AFREQUENCE: {
                    if (rgFrequency.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())
                            /*&& frequencyChoice == FREQUENCYCHOICELESS*/) {
                        rgFrequency.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgFrequency, rgFrequency.getSelected(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_BFREQUENCE: {
                    if (rgFrequency.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())
                            /*&& frequencyChoice == FREQUENCYCHOICEMORE*/) {
                        rgFrequency.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(rgFrequency, rgFrequency.getSelected(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERVIDEO_LINE: {
                    if (!tvLine.getText().toString().equals(commandMsgToUI.getParam())
                            && layoutLine.getVisibility() == View.VISIBLE) {
                        onTextChanged(tvLine, commandMsgToUI.getParam(), false);
                    }
                    break;
                }
            }
        }
    };

    private EventUIObserver eventUITriggerParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_TRIGGER_PARAM) {
                int source = triggerVideo.getTriggerSource();
                if (rgSource.getSelected().getIndex() != source) {
                    rgSource.setSelectedIndex(source);
                    onCheckChanged(rgSource, rgSource.getSelected(), true);
                }
                int polarity = triggerVideo.getPolarity();
                if (rgPolar.getSelected().getIndex() != polarity) {
                    rgPolar.setSelectedIndex(polarity);
                    onCheckChanged(rgPolar, rgPolar.getSelected(), true);
                }
                int standard = triggerVideo.getStandard();
                if (rgStandard.getSelected().getIndex() != standard) {
                    rgStandard.setSelectedIndex(standard);
                    onCheckChanged(rgStandard, rgStandard.getSelected(), true);
                }
                int trigger = TopMatchTrigger.triggerVideoTriggerFromScope(standard, triggerVideo.getVideoTrigger());
                if (rgTrigger.getSelected().getIndex() != trigger) {
                    rgTrigger.setSelectedIndex(trigger);
                    onCheckChanged(rgTrigger, rgTrigger.getSelected(), true);
                }
                int frequency = TopMatchTrigger.triggerVideoFrequencyFromScope(standard, triggerVideo.getVideoFrequency());
                if (rgFrequency.getSelected().getIndex() != frequency) {
                    rgFrequency.setSelectedIndex(frequency);
                    onCheckChanged(rgFrequency, rgFrequency.getSelected(), true);
                }
                String line = String.valueOf(triggerVideo.getLine());
                if (!tvLine.getText().toString().equals(line)) {
                    onTextChanged(tvLine, line, true);
                }
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            ScreenUtil.getViewLocation(v);
            int maxCount = 625;
            int index = rgStandard.getSelected().getIndex();
            if (index == 0 || index == 1) {
                maxCount = 625;

            } else if (index == 2) {
                maxCount = 525;

            } else if (index == 3) {
                maxCount = 750;

            } else if (index == 4 || index == 5) {
                maxCount = 1125;

            }
            countDialog.setData(context.getString(R.string.triggervideo_linehead),
                    Integer.parseInt(tvLine.getText().toString()), maxCount, onCountDismissListener);
        }
    };

    private TopDialogCount.OnDismissListener onCountDismissListener = new TopDialogCount.OnDismissListener() {
        @Override
        public void onDismiss(int result) {
            onTextChanged(tvLine, String.valueOf(result), false);
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
        Tools.PrintControlsLocation("TopLayoutTriggerVideo", rootView);
        if (view.getId() == R.id.triggerSource) {
            Command.get().getTrigger_video().Source(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SOURCE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerVideo.setTriggerSource(item.getIndex());
            }
            msgTriggerDetail.setTriggerSource(item);
            sendMsg(isFromEventBus);
            RxBus.getInstance().post(RxEnum.TOPTRIGGER_CHANNEL,
                    new TopMsgTriggerChannel(TopLayoutTrigger.DETAIL_VIDEO, item.getIndex(),isFromEventBus));
        } else if (view.getId() == R.id.polar) {
            Command.get().getTrigger_video().Polarity(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_POLAR, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerVideo.setPolarity(item.getIndex());
            }
            msgTriggerDetail.setPolar(item);
            sendMsg(isFromEventBus);
        } else if (view.getId() == R.id.standard) {
            Command.get().getTrigger_video().Standard(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_STANDARD, String.valueOf(item.getIndex()));
            switch (item.getIndex()) {
                case 0:
                case 1:
                case 2:
                    triggerChoice = TRIGGERCHOICEMORE;
                    rgFrequency.setVisibility(View.GONE);
                    break;
                case 3:
                    triggerChoice = TRIGGERCHOICELESS;
                    frequencyChoice = FREQUENCYCHOICELESS;
                    rgFrequency.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    triggerChoice = TRIGGERCHOICEMORE;
                    frequencyChoice = FREQUENCYCHOICELESS;
                    rgFrequency.setVisibility(View.VISIBLE);
                    break;
                case 5:
                    triggerChoice = TRIGGERCHOICELESS;
                    frequencyChoice = FREQUENCYCHOICEMORE;
                    rgFrequency.setVisibility(View.VISIBLE);
                    break;
            }
            rgTrigger.setData(R.string.triggerVideotrigger, getPointArrayResId(), onCheckChangedListener);
            rgFrequency.setData(R.string.triggerVideoFrequency, getFrequencyArrayResId(), onCheckChangedListener);
            int trigger = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_TRIGGER + item.getIndex());
            int frequency = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + item.getIndex());
            int line = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_LINE + item.getIndex());
            rgTrigger.setSelectedIndex(trigger);
            rgFrequency.setSelectedIndex(frequency);
            tvLine.setText(String.valueOf(line));
            layoutLine.setVisibility(rgTrigger.getArray().length - 1 == rgTrigger.getSelected().getIndex() ? View.VISIBLE : View.GONE);

            Command.get().getTrigger_video().Amode(trigger,false);
            Command.get().getTrigger_video().Afrequence(frequency,false);

            if (!isFromEventBus) {
                triggerVideo.setStandard(item.getIndex());
                triggerVideo.setVideoTrigger(TopMatchTrigger.triggerVideoTriggerToScope(item.getIndex(), rgTrigger.getSelected().getIndex()));
                triggerVideo.setVideoFrequency(TopMatchTrigger.triggerVideoFrequencyToScope(item.getIndex(), rgFrequency.getSelected().getIndex()));
                triggerVideo.setLine(line);
            }
            RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPTRIGGER_VIDEO);
            msgTriggerDetail.setStandard(item);
            msgTriggerDetail.setTrigger(rgTrigger.getSelected());
            msgTriggerDetail.setFrequency(rgFrequency.getSelected());
            msgTriggerDetail.setLineDetail(tvLine.getText().toString());
            sendMsg(isFromEventBus);
        } else if (view.getId() == R.id.trigger) {
            Command.get().getTrigger_video().Amode(item.getIndex(),false);

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_TRIGGER + rgStandard.getSelected().getIndex(), String.valueOf(item.getIndex()));
            String[] point = getResources().getStringArray(getPointArrayResId());
            layoutLine.setVisibility(point.length - 1 == item.getIndex() ? View.VISIBLE : View.GONE);
            if (!isFromEventBus) {
                triggerVideo.setVideoTrigger(TopMatchTrigger.triggerVideoTriggerToScope(rgStandard.getSelected().getIndex(), rgTrigger.getSelected().getIndex()));
            }
            RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPTRIGGER_VIDEO);
            msgTriggerDetail.setTrigger(item);
            sendMsg(isFromEventBus);
        } else if (view.getId() == R.id.frequency) {
            Command.get().getTrigger_video().Afrequence(item.getIndex(), false);

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + rgStandard.getSelected().getIndex(), String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                triggerVideo.setVideoFrequency(TopMatchTrigger.triggerVideoFrequencyToScope(rgStandard.getSelected().getIndex(), rgFrequency.getSelected().getIndex()));
            }
            msgTriggerDetail.setFrequency(item);
            sendMsg(isFromEventBus);
        }
    }

    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) {
        if (tv.getId() == tvLine.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_LINE + rgStandard.getSelected().getIndex(), result);
            if (!isFromEventBus) {
                triggerVideo.setLine(Integer.parseInt(result));
            }
            tvLine.setText(result);
            msgTriggerDetail.setLineDetail(result);
            sendMsg(isFromEventBus);
            Command.get().getTrigger_video().Line(Integer.parseInt(result), false);
        }
    }
}
