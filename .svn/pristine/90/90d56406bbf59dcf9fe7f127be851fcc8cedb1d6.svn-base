package com.micsig.tbook.tbookscope.top.layout.display;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewSeekBar;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/6.
 */

public class TopLayoutDisplayWaveform extends Fragment {
    private Context context;
    private TopViewSeekBar displayBrightness;
    private TopViewRadioGroup rgDrawType;
    private TopViewRadioGroup rgBackground;
    private TopMsgDisplayWaveform displayDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_displaywaveform, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initData() {
        displayDetail = new TopMsgDisplayWaveform();
        displayDetail.setDrawType(rgDrawType.getSelected());
        displayDetail.setBackground(rgBackground.getSelected());
        displayDetail.setBrightness(displayBrightness.getProgress());
    }

    private void initView(View view) {
        rgDrawType = (TopViewRadioGroup) view.findViewById(R.id.displayDrawType);
        rgDrawType.setData(R.string.displayDrawType, R.array.displayDrawType, onCheckChangedListener);

        rgBackground = (TopViewRadioGroup) view.findViewById(R.id.displayBackground);
        rgBackground.setData(R.string.displayBackground,R.array.displayBackground,onCheckChangedListener);
        displayBrightness = (TopViewSeekBar) view.findViewById(R.id.brightness);
        displayBrightness.setData(R.string.view_display_brightness, 100, 48, seekBarChangeListener);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_DRAW_TYPE, eventUIObserver);
    }

    private void setCache() {
        int drawType = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_WAVEFORM_DRAWTYPE);
        int bright = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_WAVEFORM_BRIGHT);
        int bg = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_WAVEFORM_BACKGROUND);

        rgDrawType.setSelectedIndex(drawType);
        rgBackground.setSelectedIndex(bg);
        displayBrightness.setProgress(bright);

        Command.get().getDisplay().WaveForm(drawType, false);
        Command.get().getDisplay().Background(bg, false);
        Command.get().getDisplay().Brightness(bright, false);

        Display.getInstance().setDrawType(drawType);
        Display.getInstance().setWaveBackground( bg == 1 ? Color.WHITE : Color.TRANSPARENT);
        Display.getInstance().setBrightness(bright);

        displayDetail.setDrawType(rgDrawType.getSelected());
        displayDetail.setBackground(rgBackground.getSelected());
        displayDetail.setBrightness(displayBrightness.getProgress());
        sendMsg(false);
    }

    public TopMsgDisplayWaveform getDisplayDetail() {
        return displayDetail;
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutDisplayWaveform, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_DISPLAY_WAVEFORM:
                    int index = Integer.parseInt(commandMsgToUI.getParam());
                    if (rgDrawType.getSelected().getIndex() != index) {
                        rgDrawType.setSelectedIndex(index);
                        onCheckChanged(rgDrawType, rgDrawType.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_DISPLAY_BRIGHTNESS:
                    int progress = Integer.parseInt(commandMsgToUI.getParam());
                    if (displayBrightness.getProgress() != progress) {
                        displayBrightness.setProgress(progress);
                        onProgressChanged(displayBrightness, displayBrightness.getProgress(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_DISPLAY_BACKGROUND:
                    int bg = Integer.parseInt(commandMsgToUI.getParam());
                    if(rgBackground.getSelected().getIndex() != bg){
                        rgBackground.setSelectedIndex(bg);
                        onCheckChanged(rgBackground,rgBackground.getSelected(),false);
                    }
                    break;
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
            onCheckChanged(view, item, false);
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { //波形亮度调节
            TopLayoutDisplayWaveform.this.onProgressChanged(displayBrightness, progress, false);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        if (view.getId() == R.id.displayDrawType) { //点模式和线模式选择
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_WAVEFORM_DRAWTYPE, String.valueOf(item.getIndex()));
            Command.get().getDisplay().WaveForm(item.getIndex(), false);
            if (!isFromEventBus) {
                Display.getInstance().setDrawType(item.getIndex());
            }
            displayDetail.setDrawType(item);
            sendMsg(isFromEventBus);
        }else if(view.getId() == R.id.displayBackground){
            int idx = item.getIndex();
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_WAVEFORM_BACKGROUND,String.valueOf(idx));
            Command.get().getDisplay().WaveForm(idx,false);
            if(!isFromEventBus){
                Display.getInstance().setWaveBackground( idx == 1 ? Color.WHITE : Color.TRANSPARENT);
            }
            displayDetail.setBackground(item);
            sendMsg(isFromEventBus);
        }
    }

    private void onProgressChanged(TopViewSeekBar seekBar, int progress, boolean isFromEventBus) {
        if (seekBar.getId() == displayBrightness.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_WAVEFORM_BRIGHT, String.valueOf(progress));
            Command.get().getDisplay().Brightness(progress, false);
            if (!isFromEventBus) {
                Display.getInstance().setBrightness(progress);
            }
            displayDetail.setBrightness(progress);
            sendMsg(isFromEventBus);
        }
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            if (((EventBase) data).getId() == EventFactory.EVENT_DRAW_TYPE) {
                if (rgDrawType.getSelected().getIndex() != Display.getInstance().getDrawType()) {
                    rgDrawType.setSelectedIndex(Display.getInstance().getDrawType());
                    onCheckChanged(rgDrawType, rgDrawType.getSelected(), true);
                }
            }
        }
    };
}
