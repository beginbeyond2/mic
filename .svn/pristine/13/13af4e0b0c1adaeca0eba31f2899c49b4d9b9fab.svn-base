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
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogLoadRefCsvWave;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogRefRecallBean;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
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

public class TopLayoutInvokeSession extends Fragment {

    private static final String TAG = "TopLayoutInvokeSession";
    private static final int BROWSE_SESSION = 3;//会话

    public Context context;
    private TopViewSpinner sessionSpinner;
    private Button btnSessionInvoke, btnSessionBrowse;
    private CheckBox chIsFilesShowOnly;
    private final FixedSizeHashSet<FileBean> sessionPathSet = new FixedSizeHashSet<>(10);
    private DialogLoadRefCsvWave dialogLoadRefCsvWave;

    private boolean isFilesShowOnly = true;
    private DToastDialog dToastDialog = new DToastDialog();
    private OnDetailSendMsgListener onDetailSendMsgListener;
    private DialogOk dialogOnlyOk;
    private FileSelector fileSelector;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_invoke_session, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view, savedInstanceState);
        initControl();
    }

    private void initView(View view, Bundle savedInstanceState) {
        dialogLoadRefCsvWave = (DialogLoadRefCsvWave) ((MainActivity) context).findViewById(R.id.dialogLoadRefCsv);

        chIsFilesShowOnly = view.findViewById(R.id.check_file_only_show);
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all);
        chIsFilesShowOnly.setBackground(null);
        chIsFilesShowOnly.setButtonDrawable(null);
        drawable.setBounds(0, 0, 22, 22);
        chIsFilesShowOnly.setCompoundDrawables(drawable, null, null, null);
        chIsFilesShowOnly.setOnCheckedChangeListener(onCheckBoxChangedListener);

        sessionSpinner = view.findViewById(R.id.session_spinner);
        sessionSpinner.setData(context.getResources().getString(R.string.top_save_wave_directory),
                getSessionFileList(), R.layout.layout_item_for_save_directory, onSessionPathItemSelectListener);
        btnSessionBrowse = view.findViewById(R.id.btn_session_browse);
        btnSessionBrowse.setOnClickListener(onClickListener);
        btnSessionInvoke = view.findViewById(R.id.btn_session_invoke);
        btnSessionInvoke.setOnClickListener(onClickListener);

        dialogOnlyOk = (DialogOk) ((MainActivity) context).getMainViewGroup().getDialog(MainViewGroup.DIALOG_OK);
        fileSelector = new FileSelector(context, (selectedPath) -> {
            addSelectToPathSet(selectedPath);
        });
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private void addSelectToPathSet(FileBean pathStr) {
        addPathToSessionSet(pathStr);
    }
    private void addPathToSessionSet(FileBean pathStr) {
        handleAddPath(pathStr);
        sessionPathSet.add(pathStr);
        sessionSpinner.updateDataList(getSessionFileList(), null);
        saveSessionPathToCache();
    }

    private boolean handleAddPath(FileBean pathStr) {
        boolean canAdd = true;
        FileBean temp = null;
        for (FileBean fileBean : sessionPathSet) {
            if (fileBean.getPath().equals(pathStr.getPath())) {
                canAdd = false;
                temp = fileBean;
                break;
            }
        }
        if(temp != null) {
            sessionPathSet.remove(temp);
        }
        return canAdd;
    }


    private ArrayList<FileBean> getSessionFileList() {
        return sessionPathSet.getReverseList();
//        return sessionPathSet.getPositiveList();
    }

    private void saveSessionPathToCache() {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SESSION_FILE_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(sessionPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SESSION_FILE_ABSOLUTE_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(sessionPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SESSION_FILE_PATH_CURRENT, sessionSpinner.getSelectItem());
    }

    TopViewSpinner.onItemSelectListener onSessionPathItemSelectListener = str -> {
        if (Tools.fileIsExists(str.getPath())) {
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SESSION_FILE_PATH_CURRENT, str.getDisplayName());
            addPathToSessionSet(str);
        } else {
            DToast.get().show(R.string.select_flie_not_exist);
            deleteEmptyItem(str);
        }
    };

    @SuppressLint("NonConstantResourceId")
    private View.OnClickListener onClickListener = v -> {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.btn_session_browse:
                handleBrowse();
                break;
            case R.id.btn_session_invoke:
                loadSessionFile();
                break;
        }
    };

    private void deleteEmptyItem(FileBean filePath) {
        deleteEmptySessionItem(filePath);
    }

    private void deleteEmptySessionItem(FileBean filePath) {
        boolean deleteSuccess = sessionPathSet.remove(filePath);
        if (deleteSuccess) {
            sessionSpinner.updateDataList(getSessionFileList(), null);
            saveSessionPathToCache();
        }
    }


    private void handleBrowse() {
        handleBrowseClick();
    }

    private void handleBrowseClick() {
        String spinnerSelectPath= sessionSpinner.getSelectItem();
        String disPlay = sessionSpinner.getDisPlaySelectItem();
        File file = new File(spinnerSelectPath);

        if(!file.exists()){
            spinnerSelectPath = "/storage/emulated/0";
            disPlay = context.getResources().getString(R.string.internal_storage);
        }else{
            spinnerSelectPath = file.getParent();
        }
        String[] sessionFileType = {"mss"};
        fileSelector.buildInvokeFileSelector(spinnerSelectPath, disPlay, this, context, isFilesShowOnly, sessionFileType);
    }

    String toastStr = "";

    boolean bloadSession = false;
    synchronized boolean isLoadSession(){
        return bloadSession;
    }
    synchronized void setLoadSession(boolean bSave){
        this.bloadSession = bSave;
    }
    private void loadSessionFile() {
        if(isLoadSession()){
            return;
        }
        final String path = sessionSpinner.getSelectItem();
        if (StrUtil.isEmpty(path)) {
            DToast.get().show(R.string.top_slip_select_file_first);
            return;
        }
        if(!Tools.fileIsExists(path)) {
            DToast.get().show(R.string.select_flie_not_exist);
            return;
        }
        setLoadSession(true);
        Scope scope = Scope.getInstance();
        boolean oldIsRun = scope.isRun();
        if (scope.isRun()) {
//            scope.setRun(false);
            Command.get().getFunctionMenu().Stop(true);
        }else{
            Command.get().getSample().SegmentedStop(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ms_sleep(1000);
                File file = new File(path);
                if (file.exists()) {
                    HashMap<String, HashMap<String, String>> map = new HashMap<>();
                    SaveRecoverySession saveRecoverySession = SaveRecoverySession.getInstance();
                    saveRecoverySession.restore(map, path);

                    while (!saveRecoverySession.isDone()) {
                        ms_sleep(100);
                        Log.i(TAG, "LoadSession progress:" + saveRecoverySession.getSaveRecoveryProgress());
                        showLoadProgress(saveRecoverySession.getSaveRecoveryProgress());
                    }
                    toastStr = context.getResources().getString(R.string.top_slip_recovery_session_success);
                    if (saveRecoverySession.getStatus() == SaveRecoverySession.S_FAIL) {
                        Logger.i(TAG, "LoadSession errcode= " + saveRecoverySession.getErrCode());
                        if (saveRecoverySession.getErrCode() == SaveRecoverySession.ERR_PRODUCT) {
                            loadFileNotSupport(scope, oldIsRun);
                            return;
                        }
                        toastStr = context.getResources().getString(R.string.top_slip_recovery_session_failed);
                        showLoadProgress(100);
                    }
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Scope.getInstance().enableCommand(false);
                            CacheUtil.get().initStateCacheLoad();

                            ((MainActivity) context).preMainLoadCahceProcess();

                            HashMap<String, String> curMap = map.get(CacheUtil.DefaultSaveName);
                            HashMap<String, String> dstCurMap = CacheUtil.get().getCurrMap();
//                            HashMap<String, String> dstOtherMap = CacheUtil.get().getCurrOtherMap();
                            dstCurMap.clear();
//                            dstOtherMap.clear();
                            if (curMap != null) {
                                dstCurMap.putAll(curMap);
                            }
                            CacheUtil.get().clearTempSaveParam(dstCurMap);
                            CacheUtil.get().checkMSSStoreMap();

                            CacheUtil.get().putMapInForce(CacheUtil.MAIN_LEFT_RUNSTOP, String.valueOf(false));
                            ((MainActivity) context).updateMainLoadCaheProcess(false);
                            ((MainActivity) context).postMainLoadCacheProcess();

                            int recoverySelect = CacheUtil.get().getInt(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + "-1");
                            RxBus.getInstance().post(RxEnum.MQ_MSG_RECOVERY_SELECT, recoverySelect);

                            Log.d("SaveRecoverySession", toastStr);
                            DToast.get().show(toastStr);
                            setLoadSession(false);
                        }
                    });
                }else{
                    setLoadSession(false);
                }
            }
        }).start();
    }


    private void showLoadProgress(int progress) {
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

    private void loadFileNotSupport(Scope scope, boolean isRun) {
        requireActivity().runOnUiThread(() -> {
            ScreenControls screenControls = ScreenControls.getInstance();
            if (screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);
            }
            screenControls.setProgressValue(0);
            dialogOnlyOk.setData(context.getResources().getString(R.string.file_not_support), null, null);
            setLoadSession(false);
            if (scope.isRun() == isRun) return;
            if (isRun) {
                Command.get().getFunctionMenu().Run(true);
            } else {
                Command.get().getFunctionMenu().Stop(true);
            }
        });
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
        restoreSessionPath();
    }

    private void restoreSessionPath() {
        sessionPathSet.clear();
        String sessionCurrentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SESSION_FILE_PATH_CURRENT);
        String sessionPathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SESSION_FILE_PATH);
        String abSessionPathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SESSION_FILE_ABSOLUTE_PATH);

        ArrayList<String> sessionPathList = StrUtil.getListFromString(sessionPathListStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        ArrayList<String> abSessionPathList = StrUtil.getListFromString(abSessionPathListStr, CacheUtil.WAVE_STORE_PATH_SLIP);

        if (!Tools.fileIsExists(sessionCurrentPath)) {
            sessionCurrentPath = null;
        }


        ArrayList<FileBean> dataList = new ArrayList<>();
        FileBean currentBean = new FileBean();
        for (int i = 0; i < abSessionPathList.size(); i++) {
            if (!Tools.fileIsExists(abSessionPathList.get(i))) continue;
            FileBean fileBean = new FileBean();
            fileBean.setPath(abSessionPathList.get(i));
            fileBean.setDisplayName(sessionPathList.get(i));
            if(abSessionPathList.get(i).equals(sessionCurrentPath)) {
                currentBean.setPath(abSessionPathList.get(i));
                currentBean.setDisplayName(sessionPathList.get(i));
            }
            dataList.add(fileBean);
        }


        sessionPathSet.addAll(dataList);
        sessionSpinner.updateDataList(getSessionFileList(), null);

        isFilesShowOnly = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_INVOKE_SESSION_FILE_FILTER);
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
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_INVOKE_SESSION_FILE_FILTER, String.valueOf(isChecked));
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
