package com.micsig.tbook.tbookscope.top.layout.display;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.StrUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by limh on 2024/8/6.
 */

public class TopLayoutDisplayFftInfo extends Fragment {
    private Context context;
    private TopViewRadioGroup rgFftInfo;
//    private MSwitchBox switchBox;
    private OnDetailSendMsgListener onDetailSendMsgListener;
    private TopMsgDisplayFftInfo displayDetail;
    private ViewGroup rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_displayfftinfo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initView(View view) {
        rootView = (ViewGroup) view;
        rgFftInfo = (TopViewRadioGroup) view.findViewById(R.id.displayFftInfo);
        String[] fftInfoPre = getResources().getStringArray(R.array.frequencymeter);
        String[] fftInfo = getResources().getStringArray(R.array.topLayoutDisplayFftInfo);
        String[] fftInfoArray = StrUtil.add(fftInfoPre, fftInfo);
        rgFftInfo.setData(getString(R.string.topLayoutDisplayFftInfo), fftInfoArray, onCheckChangedListener);

//        switchBox = (MSwitchBox) view.findViewById(R.id.switchFftInfo);
//        switchBox.setOnToggleStateChangedListener(onToggleStateChangedListener);
    }

    private void initData() {
        displayDetail = new TopMsgDisplayFftInfo();
        displayDetail.setFftInfoIndex(rgFftInfo.getSelected().getIndex());
//        displayDetail.setShowFftInfo(switchBox.isState());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);

    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    public TopMsgDisplayFftInfo getDisplayDetail() {
        return displayDetail;
    }

    private void setCache() {
        int index = CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY);
        boolean showFftInfo = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY_SWITCH);
        rgFftInfo.setSelectedIndex(index);
//        switchBox.setState(showFftInfo);
//        rgFftInfo.setEnabled(showFftInfo);

        displayDetail.setFftInfoIndex(rgFftInfo.getSelected().getIndex());
//        displayDetail.setShowFftInfo(switchBox.isState());

        sendMsg(false);
    }

    private void sendMsg(boolean isFromEventBus) {
        Tools.PrintControlsLocation("TopLayoutDisplayFftInfo", rootView);
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, isFromEventBus);
        }
    }


    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
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
            int index = CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY);
            if (item.getIndex() == index) return;
            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY, String.valueOf(item.getIndex()));
            displayDetail.setFftInfoIndex(item.getIndex());
            sendMsg(false);
        }
    };

//    //switch box hide
//    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
//        @Override
//        public void onToggleStateChanged(MSwitchBox view, boolean state) {
//            PlaySound.getInstance().playButton();
//            rgFftInfo.setEnabled(state);
//            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY_SWITCH, String.valueOf(state));
//            displayDetail.setShowFftInfo(state);
//            sendMsg(false);
//        }
//    };
}
