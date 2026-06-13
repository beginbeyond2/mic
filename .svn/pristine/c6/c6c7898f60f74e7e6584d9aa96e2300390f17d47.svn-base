package com.micsig.tbook.tbookscope.top.layout.save;

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
public class TopLayoutSaveInvoke extends Fragment {
    public static final int DETAIL_INVOKE_WAV = 0;
    public static final int DETAIL_INVOKE_CSV = 1;
    public static final int DETAIL_INVOKE_RECOVERY = 2;
    public static final int DETAIL_INVOKE_SESSION = 3;

    private Context context;
    private RelativeLayout invokeDetail;
    private TopViewTitleWithScroll invokeTitle;
    private TopLayoutInvokeWav wavLayout;               //invoke wave
    private TopLayoutInvokeCsv csvLayout;                //invoke csv
    private TopLayoutInvokeSetting settingLayout;        //invoke setting
    private TopLayoutInvokeSession sessionLayout;        //invoke session

    private TopMsgSaveInvoke msgInvoke;

    private final String[] tags = {"WavLayout", "CsvLayout", "SettingLayout", "SessionLayout"};
    private Fragment[] fragments = new Fragment[4];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_invoke, container, false);
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

        wavLayout = fragments[0] == null ? new TopLayoutInvokeWav() : (TopLayoutInvokeWav) fragments[0];
        csvLayout = fragments[1] == null ? new TopLayoutInvokeCsv() : (TopLayoutInvokeCsv) fragments[1];
        settingLayout = fragments[2] == null ? new TopLayoutInvokeSetting() : (TopLayoutInvokeSetting) fragments[2];
        sessionLayout = fragments[3] == null ? new TopLayoutInvokeSession() : (TopLayoutInvokeSession) fragments[3];
       
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.invokeDetail, wavLayout, tags[0])
                    .add(R.id.invokeDetail, csvLayout, tags[1])
                    .add(R.id.invokeDetail, settingLayout, tags[2])
                    .add(R.id.invokeDetail, sessionLayout, tags[3])
                    .hide(csvLayout)
                    .hide(settingLayout)
                    .hide(sessionLayout)
                    .commitAllowingStateLoss();
        }

        invokeDetail = (RelativeLayout) view.findViewById(R.id.invokeDetail);
        invokeTitle = (TopViewTitleWithScroll) view.findViewById(R.id.invokeTitle);
        String[] array = context.getResources().getStringArray(R.array.topInvokeType);
        boolean[] arrayVisible = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            arrayVisible[i] = true;
        }
        invokeTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener);

        wavLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        csvLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        settingLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        sessionLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);

        msgInvoke = new TopMsgSaveInvoke();
        msgInvoke.setSaveTitle(invokeTitle.getSelected());
        msgInvoke.setSaveDetail(wavLayout.getSaveDetail());
        msgInvoke.setFromEventBus(false);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private void setCache() {
        int index = CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_INVOKE_STORE);
        invokeTitle.setSelected(index);
        onCheckChanged(invokeTitle, invokeTitle.getSelected(), false);
    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_INVOKE, msgInvoke);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSave, true);
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
            if (msgInvoke.getSaveTitle() == null) {
                msgInvoke.setSaveTitle(invokeTitle.getSelected());
            }
            if (fragment.equals(wavLayout)) {
                msgInvoke.setSaveDetail(wavLayout.getSaveDetail());
                msgInvoke.getSaveTitle().setRxMsgSelect(false);
                msgInvoke.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(csvLayout)) {
                msgInvoke.setSaveDetail(csvLayout.getSaveDetail());
                msgInvoke.getSaveTitle().setRxMsgSelect(false);
                msgInvoke.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(settingLayout)) {
                msgInvoke.setSaveDetail(settingLayout.getSaveDetail());
                msgInvoke.getSaveTitle().setRxMsgSelect(false);
                msgInvoke.setFromEventBus(isFromEventBus);
                sendMsg();
            }else if(fragment.equals(sessionLayout)) {
                msgInvoke.setSaveDetail(sessionLayout.getSaveDetail());
                msgInvoke.getSaveTitle().setRxMsgSelect(false);
                msgInvoke.setFromEventBus(isFromEventBus);
                sendMsg();
            }
        }
    };

    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        if (view.getId() == invokeTitle.getId()) {
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_INVOKE_STORE, String.valueOf(item.getIndex()));
            getChildFragmentManager().beginTransaction()
                    .hide(wavLayout)
                    .hide(csvLayout)
                    .hide(settingLayout)
                    .hide(sessionLayout)
                    .commitAllowingStateLoss();
            switch (item.getIndex()) {
                case DETAIL_INVOKE_WAV:
                    getChildFragmentManager().beginTransaction()
                            .show(wavLayout).commitAllowingStateLoss();
                    msgInvoke.setSaveTitle(item);
                    msgInvoke.setSaveDetail(wavLayout.getSaveDetail());
                    msgInvoke.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_INVOKE_CSV:
                    getChildFragmentManager().beginTransaction()
                            .show(csvLayout).commitAllowingStateLoss();
                    msgInvoke.setSaveTitle(item);
                    msgInvoke.setSaveDetail(csvLayout.getSaveDetail());
                    msgInvoke.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_INVOKE_RECOVERY:
                    getChildFragmentManager().beginTransaction()
                            .show(settingLayout).commitAllowingStateLoss();
                    msgInvoke.setSaveTitle(item);
                    msgInvoke.setSaveDetail(settingLayout.getSaveDetail());
                    msgInvoke.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_INVOKE_SESSION:
                    getChildFragmentManager().beginTransaction()
                            .show(sessionLayout).commitAllowingStateLoss();
                    msgInvoke.setSaveTitle(item);
                    msgInvoke.setSaveDetail(sessionLayout.getSaveDetail());
                    msgInvoke.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
            }
        }
    }

    public int getSaveIdx(){
        return invokeTitle.getSelected().getIndex();
    }
}
