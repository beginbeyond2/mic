package com.micsig.tbook.tbookscope.top.layout.sample;

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
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


public class TopLayoutSample extends Fragment {
    public static final int DETAIL_MODE = 0;
    public static final int DETAIL_DEPTH = 1;
    public static final int DETAIL_SEGMENTED = 2;

    private Context context;
    private RelativeLayout sampleDetail;
    private TopViewTitleWithScroll sampleTitle;
    private TopLayoutSampleMode sampleModeLayout;                   //模式
    private TopLayoutSampleDepth sampleDepthLayout;                 //记录长度
    private TopLayoutSampleSegmented sampleSegmentedLayout;         //分段存储

    private TopMsgSample msgSample;

    private String[] tags = {"sampleModeLayout", "sampleDepthLayout", "sampleSegmentedLayout"};
    private Fragment[] fragments = new Fragment[3];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_sample, container, false);
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

        sampleModeLayout = fragments[0] == null ? new TopLayoutSampleMode() : (TopLayoutSampleMode) fragments[0];
        sampleDepthLayout = fragments[1] == null ? new TopLayoutSampleDepth() : (TopLayoutSampleDepth) fragments[1];
        sampleSegmentedLayout = fragments[2] == null ? new TopLayoutSampleSegmented() : (TopLayoutSampleSegmented) fragments[2];
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.sampleDetail, sampleModeLayout, tags[0])
                    .add(R.id.sampleDetail, sampleDepthLayout, tags[1])
                    .add(R.id.sampleDetail, sampleSegmentedLayout, tags[2])
                    .hide(sampleDepthLayout)
                    .hide(sampleSegmentedLayout)
                    .commitAllowingStateLoss();
        }

        sampleDetail = (RelativeLayout) view.findViewById(R.id.sampleDetail);
        sampleTitle = (TopViewTitleWithScroll) view.findViewById(R.id.sampleTitle);
        sampleTitle.setData(R.array.sample, R.array.sampleVisible, onCheckChangedTitleListener, onItemClickListener);

//        sampleModeLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
//        sampleDepthLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
//        sampleSegmentedLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);

        msgSample = new TopMsgSample();
        msgSample.setSampleTitle(sampleTitle.getSelected());
//        msgSample.setSampleDetail(sampleModeLayout.getSampleDetail());
        msgSample.setFromEventBus(false);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private void setCache() {
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE);
        sampleTitle.setSelected(index);
        onCheckChanged(sampleTitle, sampleTitle.getSelected(), false);
    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLE, msgSample);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSample, true);
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

//    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() {
//        @Override
//        public void onClick(Fragment fragment, boolean isFromEventBus) {
//            if (msgSample.getSampleTitle() == null) {
//                msgSample.setSampleTitle(sampleTitle.getSelected());
//            }
//            if (fragment.equals(sampleModeLayout)) {
//                msgSample.setSampleDetail(sampleModeLayout.getSampleDetail());
//                msgSample.getSampleTitle().setRxMsgSelect(false);
//                msgSample.setFromEventBus(isFromEventBus);
//                sendMsg();
//            } else if (fragment.equals(sampleDepthLayout)) {
//                msgSample.setSampleDetail(sampleDepthLayout.getSampleDetail());
//                msgSample.getSampleTitle().setRxMsgSelect(false);
//                msgSample.setFromEventBus(isFromEventBus);
//                sendMsg();
//            } else if (fragment.equals(sampleSegmentedLayout)) {
//                msgSample.setSampleDetail(sampleSegmentedLayout.getSampleDetail());
//                msgSample.getSampleTitle().setRxMsgSelect(false);
//                msgSample.setFromEventBus(isFromEventBus);
//                sendMsg();
//            }
//        }
//    };

    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        if (view.getId() == sampleTitle.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE, String.valueOf(item.getIndex()));
            getChildFragmentManager().beginTransaction()
                    .hide(sampleModeLayout)
                    .hide(sampleDepthLayout)
                    .hide(sampleSegmentedLayout)
                    .commitAllowingStateLoss();
            switch (item.getIndex()) {
                case DETAIL_MODE:             //模式
                    getChildFragmentManager().beginTransaction()
                            .show(sampleModeLayout).commitAllowingStateLoss();
                    msgSample.setSampleTitle(item);
//                    msgSample.setSampleDetail(sampleModeLayout.getSampleDetail());
                    msgSample.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_DEPTH:             //记录长度
                    getChildFragmentManager().beginTransaction()
                            .show(sampleDepthLayout).commitAllowingStateLoss();
                    msgSample.setSampleTitle(item);
//                    msgSample.setSampleDetail(sampleDepthLayout.getSampleDetail());
                    msgSample.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_SEGMENTED:             //分段存储
                    getChildFragmentManager().beginTransaction()
                            .show(sampleSegmentedLayout).commitAllowingStateLoss();
                    msgSample.setSampleTitle(item);
//                    msgSample.setSampleDetail(sampleSegmentedLayout.getSampleDetail());
                    msgSample.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
            }
        }
    }

    public int getSampleIdx(){
        return sampleTitle.getSelected().getIndex();
    }
}
