package com.micsig.tbook.tbookscope.top.layout.measure;

import android.content.Context;
import android.content.res.Configuration;
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
import com.micsig.tbook.tbookscope.top.layout.frequencymeter.TopLayoutFrequencyMeter;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/6.
 */
public class TopLayoutMeasure extends Fragment {
    public static final int DETAIL_COMMON = 0;
    public static final int DETAIL_STATICS = 1;
    public static final int DETAIL_FREQUENCY = 2;
    public static final int DETAIL_SETTING = 3;

    private Context context;
    private RelativeLayout measureDetail;
    private TopViewTitleWithScroll measureTitle;
    private TopLayoutMeasureCommon measureCommonLayout;             //常规
    private TopLayoutMeasureSetting measureSettingLayout;           //Setting
    private TopLayoutMeasureStatics measureStaticsLayout;           //Statics
//    private TopLayoutMeasureCounter measureCounterLayout;           //Counter
    private TopLayoutFrequencyMeter measureCounterLayout;           //Counter

    private TopMsgMeasure msgMeasure;

    private String[] tags = {"CommonLayout", "StaticsLayout", "CounterLayout","SettingLayout"};
    private Fragment[] fragments = new Fragment[4];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_measure, container, false);
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

        measureCommonLayout = fragments[0] == null ? new TopLayoutMeasureCommon() : (TopLayoutMeasureCommon) fragments[0];
        measureStaticsLayout = fragments[1] == null ? new TopLayoutMeasureStatics() : (TopLayoutMeasureStatics) fragments[1];
//        measureCounterLayout = fragments[3] == null ? new TopLayoutMeasureCounter() : (TopLayoutMeasureCounter) fragments[3];
        measureCounterLayout = fragments[2] == null ? new TopLayoutFrequencyMeter() : (TopLayoutFrequencyMeter) fragments[2];
        measureSettingLayout = fragments[3] == null ? new TopLayoutMeasureSetting() : (TopLayoutMeasureSetting) fragments[3];
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.measureDetail, measureCommonLayout, tags[0])
                    .add(R.id.measureDetail, measureStaticsLayout, tags[1])
                    .add(R.id.measureDetail, measureCounterLayout, tags[2])
                    .add(R.id.measureDetail, measureSettingLayout, tags[3])
                    .hide(measureCommonLayout)
                    .hide(measureSettingLayout)
                    .hide(measureStaticsLayout)
                    .hide(measureCounterLayout)
                    .commitAllowingStateLoss();
        }

        measureDetail = (RelativeLayout) view.findViewById(R.id.measureDetail);
        measureTitle = (TopViewTitleWithScroll) view.findViewById(R.id.measureTitle);
        String[] array = context.getResources().getStringArray(R.array.measure);
        boolean[] arrayVisible = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            if (i == 0 || i==1 || i==2 || i == 3) {
                arrayVisible[i] = true;
            } else {
                arrayVisible[i] = false;
            }
        }
        measureTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener);

        measureCommonLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        measureSettingLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        measureStaticsLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        measureCounterLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);

        msgMeasure = new TopMsgMeasure();
        msgMeasure.setMeasureTitle(measureTitle.getSelected());
        msgMeasure.setMeasureDetail(measureCommonLayout.getMeasureDetail());
        msgMeasure.setFromEventBus(false);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private void setCache() {
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE);
        measureTitle.setSelected(index);
        onCheckChanged(measureTitle, measureTitle.getSelected(), false);
    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_MEASURE, msgMeasure);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasure, true);
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
            if (msgMeasure.getMeasureTitle() == null) {
                msgMeasure.setMeasureTitle(measureTitle.getSelected());
            }
            if (fragment.equals(measureCommonLayout)) {
                msgMeasure.setMeasureDetail(measureCommonLayout.getMeasureDetail());
                msgMeasure.getMeasureTitle().setRxMsgSelect(false);
                msgMeasure.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(measureSettingLayout)) {
                msgMeasure.setMeasureDetail(measureSettingLayout.getMeasureDetail());
                msgMeasure.getMeasureTitle().setRxMsgSelect(false);
                msgMeasure.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(measureStaticsLayout)) {
                msgMeasure.setMeasureDetail(measureStaticsLayout.getMeasureDetail());
                msgMeasure.getMeasureTitle().setRxMsgSelect(false);
                msgMeasure.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(measureCounterLayout)) {
                msgMeasure.setMeasureDetail(measureCounterLayout.getMeasureDetail());
                msgMeasure.getMeasureTitle().setRxMsgSelect(false);
                msgMeasure.setFromEventBus(isFromEventBus);
                sendMsg();
            }
        }
    };

    public void showMeasureCommon(){
        measureTitle.setSelected(0);
        onCheckChanged(measureTitle, measureTitle.getSelected(), false);
    }
    public int getMeasureIdx(){
        if(measureTitle != null
                && measureTitle.getSelected() != null) {
            return measureTitle.getSelected().getIndex();
        }else{
            return 0;
        }
    }
    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        if (view.getId() == measureTitle.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE, String.valueOf(item.getIndex()));
            getChildFragmentManager().beginTransaction()
                    .hide(measureCommonLayout)
                    .hide(measureSettingLayout)
                    .hide(measureStaticsLayout)
                    .hide(measureCounterLayout)
                    .commitAllowingStateLoss();
            switch (item.getIndex()) {
                case DETAIL_COMMON:
                    getChildFragmentManager().beginTransaction()
                            .show(measureCommonLayout).commitAllowingStateLoss();
                    msgMeasure.setMeasureTitle(item);
                    msgMeasure.setMeasureDetail(measureCommonLayout.getMeasureDetail());
                    msgMeasure.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_SETTING:
                    getChildFragmentManager().beginTransaction()
                            .show(measureSettingLayout).commitAllowingStateLoss();
                    msgMeasure.setMeasureTitle(item);
                    msgMeasure.setMeasureDetail(measureSettingLayout.getMeasureDetail());
                    msgMeasure.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_STATICS:
                    getChildFragmentManager().beginTransaction()
                            .show(measureStaticsLayout).commitAllowingStateLoss();
                    msgMeasure.setMeasureTitle(item);
                    msgMeasure.setMeasureDetail(measureStaticsLayout.getMeasureDetail());
                    msgMeasure.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_FREQUENCY:
                    getChildFragmentManager().beginTransaction()
                            .show(measureCounterLayout).commitAllowingStateLoss();
                    msgMeasure.setMeasureTitle(item);
                    msgMeasure.setMeasureDetail(measureCounterLayout.getMeasureDetail());
                    msgMeasure.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
            }
        }
    }
}
