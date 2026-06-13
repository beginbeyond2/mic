package com.micsig.tbook.tbookscope.top.layout.frequencymeter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Auto.FreqCounter;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.layout.measure.IMeasureDetail;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutFrequencyMeter extends Fragment {
    private Context context;
    private TopViewRadioGroup rgFrequency;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_frequencymeter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initView(View view) {
        rgFrequency = (TopViewRadioGroup) view.findViewById(R.id.frequencymeter);
        String[] frequencyMeter1 = getResources().getStringArray(R.array.frequencymeter);
        String[] frequencyMeter2 = GlobalVar.get().getChannelsName();
        String[] frequencyMeter = StrUtil.add(frequencyMeter1, frequencyMeter2);
        rgFrequency.setData(getString(R.string.frequencymeter), frequencyMeter, onCheckChangedListener);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.TOP_USER_SELFADJUST).subscribe(consumerUserSelfAdjust);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_FREQ_COUNTER, eventFreqCounter);
    }

    private void setCache() {
        boolean enableFreqCounter = isEnableFreqCounter();
        if (enableFreqCounter) {
            int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_FREQUENCY_METER);
            rgFrequency.setSelectedIndex(index);
            setFreqMeasure(index);
            Command.get().getFrequency().setFrequency(index, false);
            FreqCounter.getInstance().setChIdx(index - 1);
        } else {
            rgFrequency.setEnabled(false);
        }
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    public IMeasureDetail getMeasureDetail() {
        return null;
    }

    private Consumer<Integer> consumerUserSelfAdjust = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            if (isEnableFreqCounter()) {
                if (rgFrequency.getSelected().getIndex() != 0) {
                    rgFrequency.setSelectedIndex(0);
                    onCheckChangedListener.onClick(rgFrequency, rgFrequency.getSelected());
                }
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutFrequencyMeter, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_MEASURE_COUNT_SOURCE:
                case CommandMsgToUI.FLAG_MENU_COUNTER:
                    if (isEnableFreqCounter()) {
                        int index = Integer.parseInt(commandMsgToUI.getParam());
                        rgFrequency.setSelectedIndex(index);
                        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_FREQUENCY_METER, String.valueOf(index));
                        setFreqMeasure(index);
                        FreqCounter.getInstance().setChIdx(index - 1);
                        MeasureManage.getInstance().getFrequencyMeterMeasure().setData("");
                    }
                    break;
            }
        }
    };

    private EventUIObserver eventFreqCounter = new EventUIObserver() {
        @Override
        public void update(Object data) {
            if (((EventBase) data).getId() == EventFactory.EVENT_FREQ_COUNTER) {
                if (isEnableFreqCounter()) {
                    String hz = "";
                    if (FreqCounter.getInstance().IsVaild()) {
                        hz = TBookUtil.getHzFromHz(FreqCounter.getInstance().getFreqVal());
                    }
                    MeasureManage.getInstance().getFrequencyMeterMeasure().setData(hz);
                }
            }
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
            if (isEnableFreqCounter()) {
                Command.get().getFrequency().setFrequency(item.getIndex(), false);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_FREQUENCY_METER, String.valueOf(item.getIndex()));
                setFreqMeasure(item.getIndex());
                FreqCounter.getInstance().setChIdx(item.getIndex() - 1);
                MeasureManage.getInstance().getFrequencyMeterMeasure().setData("");
            }
        }
    };

    private boolean isEnableFreqCounter() {
        return ScopeConfig.getConfig().isEnableFreqCounter() || App.IsDebug();
    }

    private void setFreqMeasure(int index) {
        if (index == 0) {
            MeasureManage.getInstance().getFrequencyMeterMeasure().setVisible(false);
        } else {
            MeasureManage.getInstance().getFrequencyMeterMeasure().setVisible(true);
            MeasureManage.getInstance().getFrequencyMeterMeasure().setChannelId(index);
        }
    }
}
