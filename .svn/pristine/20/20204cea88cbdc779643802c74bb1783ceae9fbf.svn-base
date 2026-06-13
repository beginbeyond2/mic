package com.micsig.tbook.tbookscope.top.layout.display;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.display.WaveGridManage;
import com.micsig.tbook.ui.top.view.TopViewSeekBar;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/6.
 */

public class TopLayoutDisplayGraticule extends Fragment {
    private Context context;
    private TopViewRadioGroup displayMode;
    private TopViewSeekBar displayIntensity;

    private TopMsgDisplayGraticule displayDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_displaygraticule, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initData() {
        displayDetail = new TopMsgDisplayGraticule();
        displayDetail.setDisplayMode(displayMode.getSelected());
        displayDetail.setIntensity(displayIntensity.getProgress());
    }

    private void initView(View view) {
        displayMode = (TopViewRadioGroup) view.findViewById(R.id.displayMode);
        displayMode.setData(R.string.displayMode, R.array.displayMode, onCheckChangedListener);
        displayIntensity = (TopViewSeekBar) view.findViewById(R.id.chartStrength);
        displayIntensity.setData(R.string.view_display_intensity, 100, 60, seekBarChangeListener);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
    }

    private void setCache() {
        int mode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_GRATICULE_MODE);
        int intensity = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_GRATICULE_INTENSITY);

        displayMode.setSelectedIndex(mode);
        displayIntensity.setProgress(intensity);

        onCheckChanged(displayMode, displayMode.getSelected(), false);
        onProgressChanged(displayIntensity, intensity, false);
    }

    public TopMsgDisplayGraticule getDisplayDetail() {
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
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutDisplayGraticule, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_DISPLAY_GRATICULE:
                    int modeIndex = Integer.parseInt(commandMsgToUI.getParam());
                    if (displayMode.getSelected().getIndex() != modeIndex) {
                        displayMode.setSelectedIndex(modeIndex);
                        onCheckChanged(displayMode, displayMode.getSelected(), false);
                    }
                    break;
                case CommandMsgToUI.FLAG_DISPLAY_INTENSITY:
                    int intensity = Integer.parseInt(commandMsgToUI.getParam());
                    if (displayIntensity.getProgress() != intensity) {
                        displayIntensity.setProgress(intensity);
                        onProgressChanged(displayIntensity, displayIntensity.getProgress(), false);
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
            onCheckChanged(view, item, false);
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TopLayoutDisplayGraticule.this.onProgressChanged(displayIntensity, progress, false);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        if (view.getId() == R.id.displayMode) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_GRATICULE_MODE, String.valueOf(item.getIndex()));
            switch (item.getIndex()) {
                case 0:
                    WaveGridManage.getInstance().setGridLine_Attr(WaveGridManage.GridAttr_CrossLine | WaveGridManage.GridAttr_ALLPoint |
                            WaveGridManage.GridAttr_CrossPoint | WaveGridManage.GridAttr_Frame);
                    break;
                case 1:
                    WaveGridManage.getInstance().setGridLine_Attr(WaveGridManage.GridAttr_CrossPoint | WaveGridManage.GridAttr_Frame);
                    break;
                case 2:
                    WaveGridManage.getInstance().setGridLine_Attr(WaveGridManage.GridAttr_CrossPoint | WaveGridManage.GridAttr_CrossLine
                            | WaveGridManage.GridAttr_Frame);
                    break;
                case 3:
                    WaveGridManage.getInstance().setGridLine_Attr(WaveGridManage.GridAttr_Frame);
                    break;
            }
            WorkModeManage.getInstance().refresh();
            Command.get().getDisplay().Graticule(item.getIndex(), false);
            displayDetail.setDisplayMode(item);
            sendMsg(isFromEventBus);
        }
    }

    private void onProgressChanged(TopViewSeekBar seekBar, int progress, boolean isFromEventBus) {
        if (seekBar.getId() == displayIntensity.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_GRATICULE_INTENSITY, String.valueOf(progress));
            WaveGridManage.getInstance().setGridLine_Bright(progress);
            Command.get().getDisplay().Intensity(progress, false);
        }
    }
}
