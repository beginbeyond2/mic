package com.micsig.tbook.tbookscope.top.layout.display;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.top.view.selectHorList.TopBeanHorizontal;
import com.micsig.tbook.ui.top.view.selectHorList.TopViewSelectHorListToHead;
import com.micsig.tbook.ui.top.view.selectHorList.TopViewSelectHorListToList;
import com.micsig.tbook.ui.util.TBookUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/6.
 */

public class TopLayoutDisplayPersist extends Fragment {
    private Context context;
    private TopViewRadioGroup displayPersist, fftDisplayPersist;
    private TopViewSelectHorListToHead selectListToHead, fftSelectListToHead;
    private TopViewSelectHorListToList selectListToList, fftSelectListToList;//这里用两个实例避免设置值相互影响，不必要添加新的逻辑区分。
    private Button clear, fftClear;

    private TopMsgDisplayPersist displayDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;
    private ViewGroup rootView;

    private boolean isCache = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_displaypersist, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        rootView = (ViewGroup) view;
        initView(view);
        initData();
        initControl();
    }

    private void initData() {
        displayDetail = new TopMsgDisplayPersist();

        displayDetail.setPersist(displayPersist.getSelected());
        displayDetail.setClear(false);
        int persistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT);
        selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener);
        selectListToList.setSelected(persistSelect);
        displayDetail.setAdjust(selectListToList.getSelected());

        displayDetail.setFftPersist(fftDisplayPersist.getSelected());
        displayDetail.setFftClear(false);
        int fftPersistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT);
        fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener);
        fftSelectListToList.setSelected(fftPersistSelect);
        displayDetail.setFftAdjust(fftSelectListToList.getSelected());
    }

    private void initView(View view) {
        displayPersist = (TopViewRadioGroup) view.findViewById(R.id.displayPersist);
        displayPersist.setData(R.string.displayPersist, R.array.displayPersist, onPersistChangeListener);
        clear = (Button) view.findViewById(R.id.clear_persist);
        clear.setOnClickListener(onClickListener);
        selectListToHead = (TopViewSelectHorListToHead) view.findViewById(R.id.selectListToHead);
        selectListToHead.setData(R.string.view_display_adjust, R.string.view_horizontallist_show, selectListToHeadListener);
        selectListToList = (TopViewSelectHorListToList) ((MainActivity) context).findViewById(R.id.selectListToList);
        selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener);

        fftDisplayPersist = (TopViewRadioGroup) view.findViewById(R.id.fft_persist);
        fftDisplayPersist.setData("FFT", R.array.displayPersist, onPersistChangeListener);
        fftClear = (Button) view.findViewById(R.id.fft_clear_persist);
        fftClear.setOnClickListener(onClickListener);
        fftSelectListToHead = (TopViewSelectHorListToHead) view.findViewById(R.id.fft_selectListToHead);
        fftSelectListToHead.setData(R.string.view_display_adjust, R.string.view_horizontallist_show, fftSelectListToHeadListener);
        fftSelectListToList = (TopViewSelectHorListToList) ((MainActivity) context).findViewById(R.id.fft_selectListToList);
        fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
    }

    private void setCache() {
        isCache = true;
        Display display = Display.getInstance();

        int persist = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_PERSIST);
        int select = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT);
        displayPersist.setSelectedIndex(persist);
        selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener);
        selectListToList.setSelected(select);
        if (persist != 2) {
            selectListToHead.setText("");
            selectListToHead.setEnabled(false);
        } else {
            selectListToHead.setText(selectListToList.getSelected().getText());
            selectListToHead.setEnabled(true);
        }
        Command.get().getDisplay().Persist_Mode(persist, false);
        Command.get().getDisplay().Persist_Adjust(select, false);
        display.setPersistType(persist);
        display.setPersistAdjustTime((int) (TBookUtil.getSFromTime(selectListToList.getSelected().getText()) * 1000));
        displayDetail.setPersist(displayPersist.getSelected());
        displayDetail.setAdjust(selectListToList.getSelected());

        int fftPersist = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_PERSIST);
        int fftSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT);
        fftDisplayPersist.setSelectedIndex(fftPersist);
        fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener);
        fftSelectListToList.setSelected(fftSelect);
        if(fftPersist != 2) {
            fftSelectListToHead.setText("");
            fftSelectListToHead.setEnabled(false);
        } else {
            fftSelectListToHead.setText(fftSelectListToList.getSelected().getText());
            fftSelectListToHead.setEnabled(true);
        }
        Command.get().getDisplay().FftPersist_Mode(fftPersist, false);
        Command.get().getDisplay().FftPersist_Adjust(select, false);
        display.setFftPersistType(fftPersist);
        display.setFftPersistAdjustTime((int) (TBookUtil.getSFromTime(fftSelectListToList.getSelected().getText()) * 1000));
        displayDetail.setFftPersist(fftDisplayPersist.getSelected());
        displayDetail.setFftAdjust(fftSelectListToList.getSelected());

        sendMsg(false);
    }

    public TopMsgDisplayPersist getDisplayDetail() {
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
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutDisplayPersist, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_DISPLAY_PERSISTMODE: {
                    int index = Integer.parseInt(commandMsgToUI.getParam());
                    if (displayPersist.getSelected().getIndex() != index) {
                        displayPersist.setSelectedIndex(index);
                        onCheckChanged(displayPersist, displayPersist.getSelected(), false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_DISPLAY_FFT_PERSISTMODE: {
                    int index = Integer.parseInt(commandMsgToUI.getParam());
                    if (fftDisplayPersist.getSelected().getIndex() != index) {
                        fftDisplayPersist.setSelectedIndex(index);
                        onCheckChanged(fftDisplayPersist, fftDisplayPersist.getSelected(), false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_DISPLAY_PERSISTADJUST: {
                    int time = Integer.parseInt(commandMsgToUI.getParam());
                    int persistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT);
                    selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener);
                    selectListToList.setSelected(persistSelect);
                    TopBeanHorizontal bean = selectListToList.getBean(time);
                    if (displayPersist.getSelected().getIndex() != 2) {
                        return;
                    }
                    if (bean.getIndex() != selectListToList.getSelected().getIndex()) {
                        onDialogChanged(R.id.selectListToHead, bean, false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_DISPLAY_FFT_PERSISTADJUST: {
                    int time = Integer.parseInt(commandMsgToUI.getParam());
                    int fftPersistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT);
                    fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener);
                    fftSelectListToList.setSelected(fftPersistSelect);
                    TopBeanHorizontal bean = fftSelectListToList.getBean(time);
                    if (fftDisplayPersist.getSelected().getIndex() != 2) {
                        return;
                    }
                    if (bean.getIndex() != fftSelectListToList.getSelected().getIndex()) {
                        onDialogChanged(R.id.fft_selectListToHead, bean, false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_DISPLAY_PERSIST_CLEAR: {
                    onClickListener.onClick(clear);
                }
                break;
                case CommandMsgToUI.FLAG_DISPLAY_FFT_PERSIST_CLEAR: {
                    onClickListener.onClick(fftClear);
                }
                break;
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            switch (v.getId()) {
                case R.id.clear_persist:
                    Command.get().getDisplay().Persist_Clear(false);
                    Display.getInstance().clearPersist();
                    break;
                case R.id.fft_clear_persist:
                    Command.get().getDisplay().FftPersist_Clear(false);
                    Display.getInstance().clearFftPersist();
                    break;
            }
        }
    };


    private TopViewRadioGroup.OnCheckChangedListener onPersistChangeListener = new TopViewRadioGroup.OnCheckChangedListener() {
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

    private TopViewSelectHorListToHead.OnClickListener selectListToHeadListener = new TopViewSelectHorListToHead.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            int persistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT);
            selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener);
            selectListToList.setSelected(persistSelect);
            selectListToList.show(376);
        }
    };

    private TopViewSelectHorListToHead.OnClickListener fftSelectListToHeadListener = new TopViewSelectHorListToHead.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            int fftPersistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT);
            fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener);
            fftSelectListToList.setSelected(fftPersistSelect);
            fftSelectListToList.show(376);
        }
    };

    private TopViewSelectHorListToList.OnDialogChangedListener selectListToListListener = new TopViewSelectHorListToList.OnDialogChangedListener() {
        @Override
        public void checkChanged(int headViewId, TopBeanHorizontal item) {
            onDialogChanged(headViewId, item, false);
        }

        @Override
        public void onShow() {
            RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_AFTERGLOW);
        }

        @Override
        public void onHide() {
            RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_AFTERGLOW);
        }
    };

    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        Tools.PrintControlsLocation("TopLayoutDisplayPersist", rootView);
        if (view.getId() == R.id.displayPersist) { //余辉选择
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_PERSIST, String.valueOf(item.getIndex()));
            if (item.getIndex() != 2) {
                selectListToHead.setText("");
                selectListToHead.setEnabled(false);
            } else {
                int persistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT);
                selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener);
                selectListToList.setSelected(persistSelect);
                selectListToHead.setText(selectListToList.getSelected().getText());
                selectListToHead.setEnabled(true);
            }
            Command.get().getDisplay().Persist_Mode(item.getIndex(), false);
            if (!isFromEventBus) {
                Display display = Display.getInstance();
                display.setPersistType(item.getIndex());
                if (item.getIndex() == 2) {
                    display.setPersistAdjustTime((int) (TBookUtil.getSFromTime(selectListToList.getSelected().getText()) * 1000));
                }
            }
            displayDetail.setPersist(item);
            if (item.getIndex() == 2) {
                displayDetail.setAdjust(selectListToList.getSelected());
            }
            sendMsg(isFromEventBus);
        } else if (view.getId() == R.id.fft_persist) { //FFT 余辉选择
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_PERSIST, String.valueOf(item.getIndex()));
            if (item.getIndex() != 2) {
                fftSelectListToHead.setText("");
                fftSelectListToHead.setEnabled(false);
            } else {
                int fftPersistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT);
                fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener);
                fftSelectListToList.setSelected(fftPersistSelect);
                fftSelectListToHead.setText(fftSelectListToList.getSelected().getText());
                fftSelectListToHead.setEnabled(true);
            }
            Command.get().getDisplay().FftPersist_Mode(item.getIndex(), false);
            if (!isFromEventBus) {
                Display display = Display.getInstance();
                display.setFftPersistType(item.getIndex());
                if (item.getIndex() == 2) {
                    display.setFftPersistAdjustTime((int) (TBookUtil.getSFromTime(fftSelectListToList.getSelected().getText()) * 1000));
                }
            }
            displayDetail.setFftPersist(item);
            if (item.getIndex() == 2) {
                displayDetail.setFftAdjust(fftSelectListToList.getSelected());
            }
            sendMsg(isFromEventBus);
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void onDialogChanged(int headViewId, TopBeanHorizontal item, boolean isFromEventBus) {
        switch (headViewId) {
            case R.id.selectListToHead:
                if (!isCache) {
                    PlaySound.getInstance().playButton();
                }
                isCache = false;
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT, String.valueOf(item.getIndex()));
                Command.get().getDisplay().Persist_Adjust(TBookUtil.getMsFromTime(item.getText()), false);
                if (!isFromEventBus) {
                    Display.getInstance().setPersistAdjustTime((int) (TBookUtil.getSFromTime(item.getText()) * 1000));
                }
                selectListToHead.setText(item.getText());
                displayDetail.setAdjust(item);
                sendMsg(isFromEventBus);
                break;
            case R.id.fft_selectListToHead:
                if (!isCache) {
                    PlaySound.getInstance().playButton();
                }
                isCache = false;
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT, String.valueOf(item.getIndex()));
                Command.get().getDisplay().FftPersist_Adjust(TBookUtil.getMsFromTime(item.getText()), false);
                if (!isFromEventBus) {
                    Display.getInstance().setFftPersistAdjustTime((int) (TBookUtil.getSFromTime(item.getText()) * 1000));
                }
                fftSelectListToHead.setText(item.getText());
                displayDetail.setFftAdjust(item);
                sendMsg(isFromEventBus);
                break;
        }
    }
}
