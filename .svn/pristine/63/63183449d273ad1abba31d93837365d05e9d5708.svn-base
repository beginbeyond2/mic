package com.micsig.tbook.tbookscope.top.layout.save;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.LoadCsv;
import com.micsig.tbook.scope.Data.SaveRecoverySession;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.horizontal.HorizontalAxisRef;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogLoadRefCsvWave;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogRefRecallBean;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.FileUtils;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.util.FileSelector;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.FixedSizeHashSet;
import com.micsig.tbook.ui.top.view.TopViewSpinner;
import com.micsig.tbook.ui.util.FileBeanToStr;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;
import com.molihuan.pathselector.PathSelector;
import com.molihuan.pathselector.dao.SelectConfigData;
import com.molihuan.pathselector.entity.FileBean;
import com.molihuan.pathselector.entity.FontBean;
import com.molihuan.pathselector.fragment.BasePathSelectFragment;
import com.molihuan.pathselector.listener.CommonItemListener;
import com.molihuan.pathselector.service.impl.ConfigDataBuilderImpl;
import com.molihuan.pathselector.utils.DToastDialog;
import com.molihuan.pathselector.utils.MConstants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;

public class TopLayoutInvokeCsv extends Fragment {

    private static final String TAG = "TopLayoutInvokeCsv";
    public Context context;
    private TopViewSpinner waveSpinner;
    private Button btnWaveInvoke, btnWaveBrowse;
    private CheckBox chIsFilesShowOnly;
    private final FixedSizeHashSet<FileBean> wavePathSet = new FixedSizeHashSet<>(10);
    private DialogLoadRefCsvWave dialogLoadRefCsvWave;

    private boolean isFilesShowOnly = true;
    private DToastDialog dToastDialog = new DToastDialog();
    private OnDetailSendMsgListener onDetailSendMsgListener;
    private FileSelector fileSelector ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_invoke_wav, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view, savedInstanceState);
        initControl();
    }

    private void initView(View view, Bundle savedInstanceState) {
        dialogLoadRefCsvWave = (DialogLoadRefCsvWave) ((MainActivity) context).findViewById(R.id.dialogLoadRefCsv);
        waveSpinner = view.findViewById(R.id.wave_spinner);
        waveSpinner.setData(context.getResources().getString(R.string.top_save_wave_directory),
                getWaveFileList(), R.layout.layout_item_for_save_directory, onWavePathItemSelectListener);
        btnWaveBrowse = view.findViewById(R.id.btn_wave_browse);
        btnWaveBrowse.setOnClickListener(onClickListener);
        btnWaveInvoke = view.findViewById(R.id.btn_wave_invoke);
        btnWaveInvoke.setOnClickListener(onClickListener);

        chIsFilesShowOnly = view.findViewById(R.id.check_file_only_show);
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all);
        chIsFilesShowOnly.setBackground(null);
        chIsFilesShowOnly.setButtonDrawable(null);
        drawable.setBounds(0, 0, 22, 22);
        chIsFilesShowOnly.setCompoundDrawables(drawable, null, null, null);
        chIsFilesShowOnly.setOnCheckedChangeListener(onCheckBoxChangedListener);

        fileSelector = new FileSelector(context, (selectedPath) -> {
            addSelectToPathSet(selectedPath);
        });
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private void addSelectToPathSet(FileBean pathStr) {
        addPathToWaveSet(pathStr);
    }

    private void addPathToWaveSet(FileBean pathStr) {
        handleAddPath(pathStr);
        wavePathSet.add(pathStr);
        waveSpinner.updateDataList(getWaveFileList(), null);
        saveWavePathToCache();
    }

    private boolean handleAddPath(FileBean pathStr) {
        boolean canAdd = true;
        FileBean temp = null;
        for (FileBean fileBean : wavePathSet) {
            if (fileBean.getPath().equals(pathStr.getPath())) {
                canAdd = false;
                temp = fileBean;
                break;
            }
        }
        if (temp != null) {
            wavePathSet.remove(temp);
        }
        return canAdd;
    }

    private ArrayList<FileBean> getWaveFileList() {
        return wavePathSet.getReverseList();
    }

    public void saveWavePathToCache() {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_PATH + CacheUtil.WAVE_TYPE_CSV,
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(wavePathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_CSV,
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(wavePathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_PATH_CURRENT + CacheUtil.WAVE_TYPE_CSV, waveSpinner.getSelectItem());

    }


    TopViewSpinner.onItemSelectListener onWavePathItemSelectListener = str -> {
        if (Tools.fileIsExists(str.getPath())) {
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_PATH_CURRENT + CacheUtil.WAVE_TYPE_CSV, str.getDisplayName());
            addPathToWaveSet(str);
        } else {
            DToast.get().show(R.string.select_flie_not_exist);
            deleteEmptyItem(str);
        }
    };

    @SuppressLint("NonConstantResourceId")
    private View.OnClickListener onClickListener = v -> {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.btn_wave_browse:
                handleBrowse();
                break;
            case R.id.btn_wave_invoke:
                loadRefFromFile();
                break;
        }
    };

    private void deleteEmptyItem(FileBean filePath) {
        deleteEmptyWaveItem(filePath);
    }

    private void deleteEmptyWaveItem(FileBean filePath) {
        boolean deleteSuccess = wavePathSet.remove(filePath);
        if (deleteSuccess) {
            waveSpinner.updateDataList(getWaveFileList(), null);
            saveWavePathToCache();
        }
    }


    private void handleBrowse() {
        handleBrowseClick();
    }

    private void handleBrowseClick() {
        String spinnerSelectPath= waveSpinner.getSelectItem();
        String disPlay = waveSpinner.getDisPlaySelectItem();
        File file = new File(spinnerSelectPath);

        if(!file.exists()){
            spinnerSelectPath = "/storage/emulated/0";
            disPlay = context.getResources().getString(R.string.internal_storage);
        }else{
            spinnerSelectPath = file.getParent();
        }
        String[] waveFileType = {"csv"};
        fileSelector.buildInvokeFileSelector(spinnerSelectPath, disPlay, this, context, isFilesShowOnly, waveFileType);
    }

    private void loadRefFromFile() {
        String wavePath = waveSpinner.getSelectItem();
        if (StrUtil.isEmpty(wavePath)) {
            DToast.get().show(R.string.top_slip_select_file_first);
            return;
        }
        if(!Tools.fileIsExists(wavePath)) {
            DToast.get().show(R.string.select_flie_not_exist);
            return;
        }
        loadCsvChannelFromFile(wavePath);
    }

    private ArrayList<Integer> channelInCsv = new ArrayList<>();

    private void loadCsvChannelFromFile(String csvPath) {
        LoadCsv loadCsv = new LoadCsv();
        boolean loadSuccess = false;
        try {
            loadSuccess = loadCsv.load(csvPath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!loadSuccess) {
                DToast.get().show(context.getResources().getString(R.string.file_load_fail));
            }
        }
        Log.d(TAG, "chNums:" + loadCsv.getChNums() + ",b:" + loadSuccess);
        if (!loadSuccess) return;
        channelInCsv.clear();
        channelInCsv = loadCsv.getCsvInfos();
        for (int c : channelInCsv) {
            Log.d(TAG, "ch" + c);
        }
        DialogRefRecallBean item = createRecallBean(csvPath);
        dialogLoadRefCsvWave.setData(channelInCsv, new DialogLoadRefCsvWave.OnDismissListener() {
            @Override
            public void onDismiss(ConcurrentHashMap<Integer, Integer> channelToRef) {
                if (channelToRef.size() <= 0) return;
                loadCsvInBackGround(channelToRef, loadCsv, item);
            }
        });
    }

    private DialogRefRecallBean createRecallBean(String csvPath) {
        File file = new File(csvPath);
        long time = file.lastModified();
        @SuppressLint("SimpleDateFormat") String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
        DialogRefRecallBean item = new DialogRefRecallBean();
        item.setLastModifyTime(time);
        item.setPathFile(csvPath);
        item.setTime(ctime);
        item.setTitle(file.getName());
        return item;
    }

    public void loadCsvInBackGround(ConcurrentHashMap<Integer, Integer> channelToRef, LoadCsv loadCsv, DialogRefRecallBean item) {
        new Thread(() -> {
            Logger.d(TAG, "channelToRef= " + channelToRef.toString());
            for (Map.Entry<Integer, Integer> entry : channelToRef.entrySet()) {
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_FROM + TChan.toUiChNo(entry.getKey()),
                        item.getPathFile() + ";" + TChan.toUiChNo(entry.getValue()));
            }
            loadCsv.setLoadCsvProgress(val -> updateLoadProgress(val));
            loadCsv.loadToRef(channelToRef);
            while (!loadCsv.isFinish()) {
                ms_sleep(100);
            }
            requireActivity().runOnUiThread(() -> {
                channelToRef.forEach((key, value) -> {
                    ChannelFactory.chOpen(key);
                    setCacheMapValue(key, item);
                });
            });
        }).start();
    }

    private void updateLoadProgress(int progress) {
        requireActivity().runOnUiThread(() -> {
            ScreenControls screenControls = ScreenControls.getInstance();
            if (progress < 0 || progress >= 100) {
                if (screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                    screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);
                }
            } else {
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);
                }
                screenControls.setProgressValue(progress);
            }
        });
    }


    private void setCacheMapValue(int key, DialogRefRecallBean item) {
        if (item == null) return;
        int chanId = TChan.toUiChNo(key);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chanId, String.valueOf(true));
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + chanId, String.valueOf(true));
        int rightRefSlipType = 1;//csv
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_TYPE + chanId, String.valueOf(rightRefSlipType));
        double scaleVal = ChannelFactory.getRefChannel(TChan.toFpgaChNo(chanId)).getRefTimeScaleVal();
        CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + chanId, getStringRefScale(TChan.toFpgaChNo(chanId), scaleVal));
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + chanId, item.getPathFile());
        ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
        String label = getLabelFromChannel(key);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + chanId, label);
        WaveManage.get().setChannelLabel(chanId, label);
        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_STATE, chanId);
    }

    public static String getStringRefScale(int refIndex, double scale) {
        RefChannel refChannel = ChannelFactory.getRefChannel(refIndex);
        if (refChannel == null) return "";
        HorizontalAxisRef horizontalAxisRef = refChannel.getHorizontalAxisRef();
        String tail = "s";
        if (horizontalAxisRef.getRefType() == HorizontalAxisRef.REFTYPE_MATHFFT) {
            tail = "Hz";
        }
        String s = TBookUtil.getMFromDouble(scale);
        if (TextUtils.isEmpty(s)) return "";
        return s + tail;
    }

    public String getLabelFromChannel(int chNo) {
        String label = "";
        RefChannel refChannel = ChannelFactory.getRefChannel(chNo);
        if (refChannel != null) {
            label = refChannel.getLabel();
        }
        return label;
    }


    private void ms_sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private void setCache() {
        restoreWavePath();
    }

    private void restoreWavePath() {
        wavePathSet.clear();
        String currentWavePath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_WAVE_FILE_PATH_CURRENT + CacheUtil.WAVE_TYPE_CSV);
        String wavePathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_WAVE_FILE_PATH + CacheUtil.WAVE_TYPE_CSV);
        String abWavePathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_CSV);
        ArrayList<String> wavePathList = StrUtil.getListFromString(wavePathListStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        ArrayList<String> abWavePathList = StrUtil.getListFromString(abWavePathListStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        if (!Tools.fileIsExists(currentWavePath)) {
            currentWavePath = null;
        }

        ArrayList<FileBean> dataList = new ArrayList<>();
        FileBean currentBean = new FileBean();
        for (int i = 0; i < abWavePathList.size(); i++) {
            if (!Tools.fileIsExists(abWavePathList.get(i))) continue;
            FileBean fileBean = new FileBean();
            fileBean.setPath(abWavePathList.get(i));
            fileBean.setDisplayName(wavePathList.get(i));
            if(abWavePathList.get(i).equals(currentWavePath)) {
                currentBean.setPath(abWavePathList.get(i));
                currentBean.setDisplayName(wavePathList.get(i));
            }
            dataList.add(fileBean);
        }

        wavePathSet.addAll(dataList);
        waveSpinner.updateDataList(getWaveFileList(), null);

        isFilesShowOnly = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_INVOKE_WAVE_FILE_FILTER + CacheUtil.WAVE_TYPE_CSV);
        chIsFilesShowOnly.setChecked(isFilesShowOnly);
    }

    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == chIsFilesShowOnly.getId()) {
                if (isChecked) {
                    chIsFilesShowOnly.setTextColor(getResources().getColor(R.color.color_Text_white));
                    isFilesShowOnly = true;
                } else {
                    chIsFilesShowOnly.setTextColor(getResources().getColor(R.color.textColorNewTopViewEnable));
                    isFilesShowOnly = false;
                }
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_INVOKE_WAVE_FILE_FILTER + CacheUtil.WAVE_TYPE_CSV, String.valueOf(isChecked));
            }
        }
    };

    private void sendMsg() {
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, false);
        }
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    public ISaveDetail getSaveDetail() {
        return null;
    }

}
