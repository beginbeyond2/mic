package com.micsig.tbook.tbookscope.top.layout.save;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

public class TopLayoutSave extends Fragment {

    public static final int DETAIL_STORE = 0;
    public static final int DETAIL_INVOKE = 1;

    public static final int DETAIL_AUTOSAVE = 2;
    private Context context;
    private RelativeLayout saveDetail;
    private TopViewTitleWithScroll saveTitle;

    private TopLayoutSaveStore storeLayout;
    private TopLayoutSaveInvoke invokeLayout;

    private TopLayoutAutoSave autoSaveLayout;

    private final String[] tags = {"StoreLayout", "InvokeLayout","AutoSave"};
    private final Fragment[] fragments = new Fragment[3];

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save, container, false);
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
        storeLayout = fragments[0] == null ? new TopLayoutSaveStore() : (TopLayoutSaveStore) fragments[0];
        invokeLayout = fragments[1] == null ? new TopLayoutSaveInvoke() : (TopLayoutSaveInvoke) fragments[1];
        autoSaveLayout = fragments[2] == null ? new TopLayoutAutoSave() : (TopLayoutAutoSave) fragments[2];
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.saveDetail, storeLayout, tags[0])
                    .add(R.id.saveDetail, invokeLayout, tags[1])
                    .add(R.id.saveDetail, autoSaveLayout, tags[2])
                    .hide(invokeLayout)
                    .commitAllowingStateLoss();
        }
        saveDetail = (RelativeLayout) view.findViewById(R.id.saveDetail);
        saveTitle = (TopViewTitleWithScroll) view.findViewById(R.id.saveTitle);
        String[] array = context.getResources().getStringArray(R.array.save);
        boolean[] arrayVisible = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            arrayVisible[i] = true;
        }
        saveTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE).subscribe(consumerAutoSaveTaskState);

    }

    private void setCache() {
        int index = CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_SAVE);
        saveTitle.setSelected(index);
        onCheckChanged(saveTitle, saveTitle.getSelected(), false);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSave, true);
        }
    };

    private Consumer<Boolean> consumerAutoSaveTaskState = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean setState) throws Throwable {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("TAGTest", "run: " +setState);
                    if(setState){
                        saveTitle.setEnable(DETAIL_STORE,true);
                        saveTitle.setEnable(DETAIL_INVOKE,true);
                    }else {
                        saveTitle.setEnable(DETAIL_STORE,false);
                        saveTitle.setEnable(DETAIL_INVOKE,false);

                    }
                }
            });

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

    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        if (view.getId() == saveTitle.getId()) {
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE, String.valueOf(item.getIndex()));
            getChildFragmentManager().beginTransaction()
                    .hide(storeLayout)
                    .hide(invokeLayout)
                    .hide(autoSaveLayout)
                    .commitAllowingStateLoss();
            switch (item.getIndex()) {
                case DETAIL_STORE:
                    getChildFragmentManager().beginTransaction()
                            .show(storeLayout).commitAllowingStateLoss();
                    break;
                case DETAIL_INVOKE:
                    getChildFragmentManager().beginTransaction()
                            .show(invokeLayout).commitAllowingStateLoss();
                    break;
                case DETAIL_AUTOSAVE:
                    getChildFragmentManager().beginTransaction()
                            .show(autoSaveLayout).commitAllowingStateLoss();
                    break;
            }
        }
    }

    public int getSaveIdx(){
        return saveTitle.getSelected().getIndex();
    }

}
