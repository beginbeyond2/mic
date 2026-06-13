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
public class TopLayoutSaveStore extends Fragment {
    public static final int SAVEINLOCAL = 0;// Tools.SaveType_LOCAL
    public static final int SAVEINUDISK = 1;// Tools.SaveType_UDISK

    public static final int DETAIL_WAV = 0;
    public static final int DETAIL_CSV = 1;
    public static final int DETAIL_BIN = 2;
    public static final int DETAIL_SETTING = 3;
    public static final int DETAIL_PICTURE = 4;
    public static final int DETAIL_SESSION = 5;

    private Context context;
    private RelativeLayout saveDetail;
    private TopViewTitleWithScroll saveTitle;
    private TopLayoutSaveWav saveWaveLayout;               //save wave
    private TopLayoutSaveCsv saveCsvLayout;                //save csv
    private TopLayoutSaveBin saveBinLayout;                //save bin
    private TopLayoutSaveSetting saveSettingLayout;        //setting
    private TopLayoutSavePicture savePictureLayout;        //picture
    private TopLayoutSaveSession sessionLayout;            //state

    private TopMsgSaveStore msgSave;

    private final String[] tags = {"WavLayout", "CsvLayout", "BinLayout", "SettingLayout", "PictureLayout", "StateLayout"};
    private Fragment[] fragments = new Fragment[6];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_store, container, false);
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

        saveWaveLayout = fragments[0] == null ? new TopLayoutSaveWav() : (TopLayoutSaveWav) fragments[0];
        saveCsvLayout = fragments[1] == null ? new TopLayoutSaveCsv() : (TopLayoutSaveCsv) fragments[1];
        saveBinLayout = fragments[2] == null ? new TopLayoutSaveBin() : (TopLayoutSaveBin) fragments[2];
        saveSettingLayout = fragments[3] == null ? new TopLayoutSaveSetting() : (TopLayoutSaveSetting) fragments[3];
        savePictureLayout = fragments[4] == null ? new TopLayoutSavePicture() : (TopLayoutSavePicture) fragments[4];
        sessionLayout = fragments[5] == null ? new TopLayoutSaveSession() : (TopLayoutSaveSession) fragments[5];
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.saveDetail, saveWaveLayout, tags[0])
                    .add(R.id.saveDetail, saveCsvLayout, tags[1])
                    .add(R.id.saveDetail, saveBinLayout, tags[2])
                    .add(R.id.saveDetail, saveSettingLayout, tags[3])
                    .add(R.id.saveDetail, savePictureLayout, tags[4])
                    .add(R.id.saveDetail, sessionLayout, tags[5])
                    .hide(saveCsvLayout)
                    .hide(saveBinLayout)
                    .hide(saveSettingLayout)
                    .hide(savePictureLayout)
                    .hide(sessionLayout)
                    .commitAllowingStateLoss();
        }

        saveDetail = (RelativeLayout) view.findViewById(R.id.saveDetail);
        saveTitle = (TopViewTitleWithScroll) view.findViewById(R.id.saveTitle);
        String[] array = context.getResources().getStringArray(R.array.topSaveType);
        boolean[] arrayVisible = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            arrayVisible[i] = true;
        }
        saveTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener);

        saveWaveLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        saveCsvLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        saveBinLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        saveSettingLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        savePictureLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        sessionLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);

        msgSave = new TopMsgSaveStore();
        msgSave.setSaveTitle(saveTitle.getSelected());
        msgSave.setSaveDetail(saveWaveLayout.getSaveDetail());
        msgSave.setFromEventBus(false);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private void setCache() {
        int index = CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_SAVE_STORE);
        saveTitle.setSelected(index);
        onCheckChanged(saveTitle, saveTitle.getSelected(), false);
    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAVE, msgSave);
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
            if (msgSave.getSaveTitle() == null) {
                msgSave.setSaveTitle(saveTitle.getSelected());
            }
            if (fragment.equals(saveWaveLayout)) {
                msgSave.setSaveDetail(saveWaveLayout.getSaveDetail());
                msgSave.getSaveTitle().setRxMsgSelect(false);
                msgSave.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(saveCsvLayout)) {
                msgSave.setSaveDetail(saveCsvLayout.getSaveDetail());
                msgSave.getSaveTitle().setRxMsgSelect(false);
                msgSave.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(saveBinLayout)) {
                msgSave.setSaveDetail(saveBinLayout.getSaveDetail());
                msgSave.getSaveTitle().setRxMsgSelect(false);
                msgSave.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(saveSettingLayout)) {
                msgSave.setSaveDetail(saveSettingLayout.getSaveDetail());
                msgSave.getSaveTitle().setRxMsgSelect(false);
                msgSave.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(savePictureLayout)) {
                msgSave.setSaveDetail(savePictureLayout.getSaveDetail());
                msgSave.getSaveTitle().setRxMsgSelect(false);
                msgSave.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if(fragment.equals(sessionLayout)) {
                msgSave.setSaveDetail(sessionLayout.getSaveDetail());
                msgSave.getSaveTitle().setRxMsgSelect(false);
                msgSave.setFromEventBus(isFromEventBus);
                sendMsg();
            }
        }
    };

    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        if (view.getId() == saveTitle.getId()) {
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_STORE, String.valueOf(item.getIndex()));
            getChildFragmentManager().beginTransaction()
                    .hide(saveWaveLayout)
                    .hide(saveCsvLayout)
                    .hide(saveBinLayout)
                    .hide(saveSettingLayout)
                    .hide(savePictureLayout)
                    .hide(sessionLayout)
                    .commitAllowingStateLoss();
            switch (item.getIndex()) {
                case DETAIL_WAV:
                    getChildFragmentManager().beginTransaction()
                            .show(saveWaveLayout).commitAllowingStateLoss();
                    msgSave.setSaveTitle(item);
                    msgSave.setSaveDetail(saveWaveLayout.getSaveDetail());
                    msgSave.setFromEventBus(isFromEventBus);
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_TYPE, String.valueOf(0));
                    sendMsg();
                    break;
                case DETAIL_CSV:
                    getChildFragmentManager().beginTransaction()
                            .show(saveCsvLayout).commitAllowingStateLoss();
                    msgSave.setSaveTitle(item);
                    msgSave.setSaveDetail(saveCsvLayout.getSaveDetail());
                    msgSave.setFromEventBus(isFromEventBus);
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_TYPE, String.valueOf(1));
                    sendMsg();
                    break;
                case DETAIL_BIN:
                    getChildFragmentManager().beginTransaction()
                            .show(saveBinLayout).commitAllowingStateLoss();
                    msgSave.setSaveTitle(item);
                    msgSave.setSaveDetail(saveBinLayout.getSaveDetail());
                    msgSave.setFromEventBus(isFromEventBus);
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_TYPE, String.valueOf(2));
                    sendMsg();
                    break;
                case DETAIL_SETTING:
                    getChildFragmentManager().beginTransaction()
                            .show(saveSettingLayout).commitAllowingStateLoss();
                    msgSave.setSaveTitle(item);
                    msgSave.setSaveDetail(saveSettingLayout.getSaveDetail());
                    msgSave.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_PICTURE:
                    getChildFragmentManager().beginTransaction()
                            .show(savePictureLayout).commitAllowingStateLoss();
                    msgSave.setSaveTitle(item);
                    msgSave.setSaveDetail(savePictureLayout.getSaveDetail());
                    msgSave.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
                case DETAIL_SESSION:
                    getChildFragmentManager().beginTransaction()
                            .show(sessionLayout).commitAllowingStateLoss();
                    msgSave.setSaveTitle(item);
                    msgSave.setSaveDetail(sessionLayout.getSaveDetail());
                    msgSave.setFromEventBus(isFromEventBus);
                    sendMsg();
                    break;
            }
        }
    }

    public int getSaveIdx(){
        return saveTitle.getSelected().getIndex();
    }
}
