package com.micsig.tbook.tbookscope.top.layout.auto;

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
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutAuto extends Fragment {
    public static final int DETAIL_SET = 0;
    public static final int DETAIL_RANGE = 1;

    private Context context;
    private TopViewTitleWithScroll autoTitle;
    private RelativeLayout autoDetail;
    private TopLayoutAutoSet layoutAutoSet;         //自动设置
    private TopLayoutAutoRange layoutAutoRange;     //自动量程

    private TopMsgAuto msgAuto;

    private String[] tags = {"AutoSet", "AutoRange"};
    private Fragment[] fragments = new Fragment[2];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_auto, container, false);
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
        layoutAutoSet = fragments[0] == null ? new TopLayoutAutoSet() : (TopLayoutAutoSet) fragments[0];
        layoutAutoRange = fragments[1] == null ? new TopLayoutAutoRange() : (TopLayoutAutoRange) fragments[1];
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.autoDetail, layoutAutoSet, tags[0])
                    .add(R.id.autoDetail, layoutAutoRange, tags[1])
                    .hide(layoutAutoRange)
                    .commitAllowingStateLoss();
        }

        autoDetail = (RelativeLayout) view.findViewById(R.id.autoDetail);
        autoTitle = (TopViewTitleWithScroll) view.findViewById(R.id.autoTitle);
        autoTitle.setData(R.array.auto, onCheckChangedTitleListener, onItemClickListener);

        layoutAutoSet.setOnDetailSendMsgListener(onDetailSendMsgListener);
        layoutAutoRange.setOnDetailSendMsgListener(onDetailSendMsgListener);

        msgAuto = new TopMsgAuto();
        msgAuto.setAutoTitle(autoTitle.getSelected());
        msgAuto.setAutoDetail(layoutAutoSet.getMsgAutoDetail());
        msgAuto.setFromEventBus(false);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private void setCache() {
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO);
        autoTitle.setSelected(index);
        onCheckChanged(autoTitle, autoTitle.getSelected(), false);
    }

    private void detailLayoutSetGone() {
        getChildFragmentManager().beginTransaction()
                .hide(layoutAutoSet)
                .hide(layoutAutoRange)
                .commitAllowingStateLoss();
    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_AUTO, msgAuto);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutAuto, true);
        }
    };

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
        }
    };

    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO, String.valueOf(item.getIndex()));
        detailLayoutSetGone();
        switch (item.getIndex()) {
            case DETAIL_SET:
                getChildFragmentManager().beginTransaction()
                        .show(layoutAutoSet).commitAllowingStateLoss();
                msgAuto.setAutoTitle(item);
                msgAuto.setAutoDetail(layoutAutoSet.getMsgAutoDetail());
                msgAuto.setFromEventBus(false);
                sendMsg();
                break;
            case DETAIL_RANGE:
                getChildFragmentManager().beginTransaction()
                        .show(layoutAutoRange).commitAllowingStateLoss();
                msgAuto.setAutoTitle(item);
                msgAuto.setAutoDetail(layoutAutoRange.getAutoDetail());
                msgAuto.setFromEventBus(false);
                sendMsg();
                break;
        }
    }

    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() {
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) {
            onCheckChanged(view, item, false);
        }
    };

    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() {
        @Override
        public void onClick(Fragment fragment, boolean isFromEventBus) {
            if (msgAuto.getAutoTitle() == null) {
                msgAuto.setAutoTitle(autoTitle.getSelected());
            }
            if (fragment.equals(layoutAutoSet)) {
                msgAuto.setAutoDetail(layoutAutoSet.getMsgAutoDetail());
                msgAuto.getAutoTitle().setRxMsgSelect(false);
                msgAuto.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(layoutAutoRange)) {
                msgAuto.setAutoDetail(layoutAutoRange.getAutoDetail());
                msgAuto.getAutoTitle().setRxMsgSelect(false);
                msgAuto.setFromEventBus(isFromEventBus);
                sendMsg();
            }
        }
    };

    public int getAutoIdx(){
        return autoTitle.getSelected().getIndex();
    }
}
