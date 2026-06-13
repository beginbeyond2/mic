package com.micsig.tbook.tbookscope.top.layout.display;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/6.
 */
public class TopLayoutDisplay extends Fragment {
    public static final int DETAIL_COMMON = 0;
    public static final int DETAIL_WAVEFORM = 1;
    public static final int DETAIL_GRATICULE = 2;
    public static final int DETAIL_PERSIST = 3;
    public static final int DETAIL_FFT_INFO = 4;
    public static final int DETAIL_TXT_MIX = 5;

    private Context context;
    private TopViewTitleWithScroll titleWithHead;
    private RelativeLayout displayDetail;
    private TopViewTitleWithScroll displayTitle;
    private TopLayoutDisplayCommon displayCommonLayout;             //常规
    private TopLayoutDisplayWaveform displayWaveformLayout;         //波形
    private TopLayoutDisplayGraticule displayGraticuleLayout;       //方格图
    private TopLayoutDisplayPersist displayPersistLayout;           //余辉
    private TopLayoutDisplayFftInfo displayFftInfoLayout;           //FFT Info
    private TopLayoutDisplayTxtMix displayTxtMixLayout;             //TXT Mix

    private TopMsgDisplay msgDisplay;

    private String[] tags = {"CommonLayout", "WaveformLayout", "GraticuleLayout", "PersistLayout", "FftInfoLayout", "TxtMixLayout"};
    private Fragment[] fragments = new Fragment[tags.length];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_display, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view, savedInstanceState);
        initControl();
    }

    private void initView(View view, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            for (int i = 0; i < tags.length; i++) {
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]);
            }
        }

        displayCommonLayout = fragments[0] == null ? new TopLayoutDisplayCommon() : (TopLayoutDisplayCommon) fragments[0];
        displayWaveformLayout = fragments[1] == null ? new TopLayoutDisplayWaveform() : (TopLayoutDisplayWaveform) fragments[1];
        displayGraticuleLayout = fragments[2] == null ? new TopLayoutDisplayGraticule() : (TopLayoutDisplayGraticule) fragments[2];
        displayPersistLayout = fragments[3] == null ? new TopLayoutDisplayPersist() : (TopLayoutDisplayPersist) fragments[3];
        displayFftInfoLayout = fragments[4] == null ? new TopLayoutDisplayFftInfo() : (TopLayoutDisplayFftInfo) fragments[4];
        displayTxtMixLayout = fragments[5] == null ? new TopLayoutDisplayTxtMix() : (TopLayoutDisplayTxtMix) fragments[5];
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.displayDetail, displayCommonLayout, tags[0])
                    .add(R.id.displayDetail, displayWaveformLayout, tags[1])
                    .add(R.id.displayDetail, displayGraticuleLayout, tags[2])
                    .add(R.id.displayDetail, displayPersistLayout, tags[3])
                    .add(R.id.displayDetail, displayFftInfoLayout, tags[4])
                    .add(R.id.displayDetail, displayTxtMixLayout, tags[5])
                    .hide(displayWaveformLayout)
                    .hide(displayGraticuleLayout)
                    .hide(displayPersistLayout)
                    .hide(displayFftInfoLayout)
                    .hide(displayTxtMixLayout)
                    .commitAllowingStateLoss();
        }

        displayDetail = (RelativeLayout) view.findViewById(R.id.displayDetail);
        displayTitle = (TopViewTitleWithScroll) view.findViewById(R.id.displayTitle);
        displayTitle.setData(R.array.display, onCheckChangedTitleListener, onItemClickListener);

        titleWithHead = (TopViewTitleWithScroll) view.findViewById(R.id.displayTitle);

        displayCommonLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        displayWaveformLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        displayGraticuleLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        displayPersistLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        displayFftInfoLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        displayTxtMixLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);

        msgDisplay = new TopMsgDisplay();
        msgDisplay.setDisplayTitle(displayTitle.getSelected());
        msgDisplay.setDisplayDetail(displayCommonLayout.getDisplayDetail());
        msgDisplay.setFromEventBus(false);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private void setCache() {
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY);
        displayTitle.setSelected(index);
        onCheckChanged(displayTitle, displayTitle.getSelected(), false);
    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_DISPLAY, msgDisplay);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutDisplay, true);
        }
    };

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
        }
    };

    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() {
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) {
            onCheckChanged(view, item, false);
        }
    };

    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() {
        @Override
        public void onClick(Fragment fragment, boolean isFromEventBus) {
            if (msgDisplay.getDisplayTitle() == null) {
                msgDisplay.setDisplayTitle(displayTitle.getSelected());
            }
            if (fragment.equals(displayCommonLayout)) {
                msgDisplay.setDisplayDetail(displayCommonLayout.getDisplayDetail());
                msgDisplay.getDisplayTitle().setRxMsgSelect(false);
                msgDisplay.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(displayWaveformLayout)) {
                msgDisplay.setDisplayDetail(displayWaveformLayout.getDisplayDetail());
                msgDisplay.getDisplayTitle().setRxMsgSelect(false);
                msgDisplay.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(displayGraticuleLayout)) {
                msgDisplay.setDisplayDetail(displayGraticuleLayout.getDisplayDetail());
                msgDisplay.getDisplayTitle().setRxMsgSelect(false);
                msgDisplay.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(displayPersistLayout)) {
                msgDisplay.setDisplayDetail(displayPersistLayout.getDisplayDetail());
                msgDisplay.getDisplayTitle().setRxMsgSelect(false);
                msgDisplay.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(displayFftInfoLayout)) {
                msgDisplay.setDisplayDetail(displayFftInfoLayout.getDisplayDetail());
                msgDisplay.getDisplayTitle().setRxMsgSelect(false);
                msgDisplay.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(displayTxtMixLayout)) {
                msgDisplay.setDisplayDetail(displayTxtMixLayout.getDisplayDetail());
                msgDisplay.getDisplayTitle().setRxMsgSelect(false);
                msgDisplay.setFromEventBus(isFromEventBus);
                sendMsg();
            }
        }
    };

    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        if (view.getId() == displayTitle.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY, String.valueOf(item.getIndex()));
            getChildFragmentManager().beginTransaction()
                    .hide(displayCommonLayout)
                    .hide(displayWaveformLayout)
                    .hide(displayGraticuleLayout)
                    .hide(displayPersistLayout)
                    .hide(displayFftInfoLayout)
                    .hide(displayTxtMixLayout)
                    .commitAllowingStateLoss();
            switch (item.getIndex()) {
                case DETAIL_COMMON:             //常规
                    getChildFragmentManager().beginTransaction()
                            .show(displayCommonLayout).commitAllowingStateLoss();
                    msgDisplay.setDisplayTitle(item);
                    msgDisplay.setDisplayDetail(displayCommonLayout.getDisplayDetail());
                    msgDisplay.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_WAVEFORM:             //波形
                    getChildFragmentManager().beginTransaction()
                            .show(displayWaveformLayout).commitAllowingStateLoss();
                    msgDisplay.setDisplayTitle(item);
                    msgDisplay.setDisplayDetail(displayWaveformLayout.getDisplayDetail());
                    msgDisplay.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_GRATICULE:             //方格图
                    getChildFragmentManager().beginTransaction()
                            .show(displayGraticuleLayout).commitAllowingStateLoss();
                    msgDisplay.setDisplayTitle(item);
                    msgDisplay.setDisplayDetail(displayGraticuleLayout.getDisplayDetail());
                    msgDisplay.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_PERSIST:             //余辉
                    getChildFragmentManager().beginTransaction()
                            .show(displayPersistLayout).commitAllowingStateLoss();
                    msgDisplay.setDisplayTitle(item);
                    msgDisplay.setDisplayDetail(displayPersistLayout.getDisplayDetail());
                    msgDisplay.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_FFT_INFO:             //FFT Info
                    getChildFragmentManager().beginTransaction()
                            .show(displayFftInfoLayout).commitAllowingStateLoss();
                    msgDisplay.setDisplayTitle(item);
                    msgDisplay.setDisplayDetail(displayFftInfoLayout.getDisplayDetail());
                    msgDisplay.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_TXT_MIX:             //TXT Mix
                    getChildFragmentManager().beginTransaction()
                            .show(displayTxtMixLayout).commitAllowingStateLoss();
                    msgDisplay.setDisplayTitle(item);
                    msgDisplay.setDisplayDetail(displayTxtMixLayout.getDisplayDetail());
                    msgDisplay.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
            }
        }
    }

    public int getDisplayIdx(){
        return displayTitle.getSelected().getIndex();
    }


    /**
     * Serials word 显现消失的时候，设置Display标签下的所有标签的可点击性
     */
    public void setSerialsWordVisible(boolean serialsWordVisible) {
        displayTitle.setEnable(DETAIL_COMMON, !serialsWordVisible);
        displayTitle.setEnable(DETAIL_WAVEFORM, !serialsWordVisible);
        displayTitle.setEnable(DETAIL_GRATICULE, !serialsWordVisible);
        displayTitle.setEnable(DETAIL_PERSIST, !serialsWordVisible);
        displayTitle.setEnable(DETAIL_FFT_INFO, !serialsWordVisible);
        displayTitle.setEnable(DETAIL_TXT_MIX, true);

        if (serialsWordVisible && displayTitle.getSelected().getIndex() != DETAIL_TXT_MIX) {
            displayTitle.moveOnlyScroll(DETAIL_TXT_MIX);
            displayTitle.setSelected(DETAIL_TXT_MIX);
            onCheckChanged(displayTitle, displayTitle.getSelected(), false);
        }

    }

    public boolean isShowLayoutCommom() {
        return DETAIL_COMMON == titleWithHead.getSelected().getIndex();
    }

    public void showLayoutCommon() {
        titleWithHead.setSelected(DETAIL_COMMON);
        onCheckChanged(titleWithHead, titleWithHead.getSelected(), false);
    }

    
}
