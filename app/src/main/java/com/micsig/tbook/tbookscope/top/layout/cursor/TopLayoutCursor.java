package com.micsig.tbook.tbookscope.top.layout.cursor;

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
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * @auother Liwb
 * @description:
 * @data:2023-9-5 18:20
 */
public class TopLayoutCursor extends Fragment {
    public static final int DETAIL_COMMON = 0;
    public static final int DETAIL_SETTING= 1;
    private Context context;
    private TopViewTitleWithScroll measureTitle;
    private TopLayoutCursorCommon cursorCommonLayout;             //常规
    private TopLayoutCursorSetting cursorSettingLayout;           //设置
    private String[] tags = {"CommonLayout","SettingLayout"};
    private Fragment[] fragments = new Fragment[2];
    private TopMsgCursor msgCursor;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_cursor, container, false);
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

        cursorCommonLayout = fragments[0] == null ? new TopLayoutCursorCommon() : (TopLayoutCursorCommon) fragments[0];
        cursorSettingLayout=fragments[1]==null? new TopLayoutCursorSetting():(TopLayoutCursorSetting) fragments[1];

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.measureDetail, cursorCommonLayout, tags[0])
                    .add(R.id.measureDetail,cursorSettingLayout,tags[1])
                    .hide(cursorSettingLayout)
                    .commitAllowingStateLoss();
        }


        measureTitle = (TopViewTitleWithScroll) view.findViewById(R.id.measureTitle);
        String[] array = context.getResources().getStringArray(R.array.cursor);
        boolean[] arrayVisible = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            if (i == 0 || i==1 || i==2 || i == 3) {
                arrayVisible[i] = true;
            } else {
                arrayVisible[i] = false;
            }
        }
        measureTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener);

        cursorCommonLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);


        msgCursor = new TopMsgCursor();
        msgCursor.setCursorTitle(measureTitle.getSelected());
        msgCursor.setCursorDetail(cursorCommonLayout.getCursorDetail());
        msgCursor.setFromEventBus(false);
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
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_CURSOR, msgCursor);
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
            if (msgCursor.getCursorTitle() == null) {
                msgCursor.setCursorTitle(measureTitle.getSelected());
            }
            if (fragment.equals(cursorCommonLayout)) {
                msgCursor.setCursorDetail(cursorCommonLayout.getCursorDetail());
                msgCursor.getCursorTitle().setRxMsgSelect(false);
                msgCursor.setFromEventBus(isFromEventBus);
                sendMsg();
            }else if (fragment.equals(cursorSettingLayout)){
                msgCursor.setCursorDetail(cursorSettingLayout.getCursorDetail());
                msgCursor.getCursorTitle().setRxMsgSelect(false);
                msgCursor.setFromEventBus(isFromEventBus);
                sendMsg();
            }
        }
    };

    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        if (view.getId() == measureTitle.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE, String.valueOf(item.getIndex()));
            getChildFragmentManager().beginTransaction()
                    .hide(cursorCommonLayout)
                    .commitAllowingStateLoss();
            switch (item.getIndex()) {
                case DETAIL_COMMON:
                    getChildFragmentManager().beginTransaction()
                            .show(cursorCommonLayout)
                            .hide(cursorSettingLayout)
                            .commitAllowingStateLoss();
                    msgCursor.setCursorTitle(item);
                    msgCursor.setCursorDetail(cursorCommonLayout.getCursorDetail());
                    msgCursor.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_SETTING:
                    getChildFragmentManager().beginTransaction()
                            .hide(cursorCommonLayout)
                            .show(cursorSettingLayout).commitAllowingStateLoss();
                    msgCursor.setCursorTitle(item);
                    msgCursor.setCursorDetail(cursorSettingLayout.getCursorDetail());
                    msgCursor.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
            }
        }
    }
    public int getCursorIdx(){
        return measureTitle.getSelected().getIndex();
    }

}
