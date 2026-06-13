package com.micsig.tbook.tbookscope.top.layout.save;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.base.Utils;
import com.micsig.tbook.scope.Data.SaveRecoverySession;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.Command_Sample;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.FileUtils;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.util.FileSelector;
import com.micsig.tbook.ui.FixedSizeHashSet;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.TopViewSpinner;
import com.micsig.tbook.ui.util.FileBeanToStr;
import com.micsig.tbook.ui.util.StrUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;

public class TopLayoutSaveSession extends Fragment {
    private static final String TAG = "TopLayoutSaveSession";
    private Context context;
    private TopViewSpinner spinner;
    private TopViewEdit saveNameEdit;
    private CheckBox fileNameAdd;
    private TextView txtSuffixNum;
    private Button btnSave, btnBrowse;
    private final FixedSizeHashSet<FileBean> sessionPathSet = new FixedSizeHashSet<>(10);
    private TopDialogTextKeyBoard layoutTextKeyBoard,fileSelectorTextKeyBoard;
    private String toastStr = "";
    protected TopDialogNumberKeyBoard dialogKeyBoard;
    private DialogOkCancel dialogOk;
    private DialogOk dialogOnlyOk;
    private boolean selectIsFast32 = false;

    public DToastDialog dToastdialog = new DToastDialog();

    private WindowManager windowManager;

    private FileSelector fileSelector ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_session, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);

    }

    private void initView(View view) {
        spinner = view.findViewById(R.id.topSpinner);
        saveNameEdit = view.findViewById(R.id.saveName);
        fileNameAdd = view.findViewById(R.id.check_file_name_add);
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all);
        fileNameAdd.setBackground(null);
        fileNameAdd.setButtonDrawable(null);
        drawable.setBounds(0, 0, 22, 22);
        fileNameAdd.setCompoundDrawables(drawable, null, null, null);

        txtSuffixNum = view.findViewById(R.id.txt_index_num);
        btnSave = view.findViewById(R.id.btn_save);
        btnBrowse = view.findViewById(R.id.btn_browse);

        spinner.setData(context.getResources().getString(R.string.top_save_wave_directory),
                getSessionPathList(), R.layout.layout_item_for_save_directory, onItemSelectListener);
        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);

        btnBrowse.setOnClickListener(onClickListener);
        btnSave.setOnClickListener(onClickListener);
        txtSuffixNum.setOnClickListener(onClickListener);
        saveNameEdit.setOnClickEditListener(onClickEditListener);
        fileNameAdd.setOnCheckedChangeListener(onCheckBoxChangedListener);

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
        dialogOk = (DialogOkCancel) ((MainActivity) context).getMainViewGroup().getDialog(MainViewGroup.DIALOG_OKCANCEL);
        dialogOnlyOk = (DialogOk) ((MainActivity) context).getMainViewGroup().getDialog(MainViewGroup.DIALOG_OK);
        fileSelector = new FileSelector(context,(selectedPath) -> {
            addSelectToPathSet(selectedPath);
        });
    }


    private ArrayList<FileBean> getSessionPathList() {
        return sessionPathSet.getReverseList();
    }

    private void addSelectToPathSet(FileBean pathStr) {
        handleAddPath(pathStr);
        sessionPathSet.add(pathStr);
        spinner.updateDataList(getSessionPathList(), null);
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
        if (temp != null) {
            sessionPathSet.remove(temp);
        }
        return canAdd;
    }


    public void saveSessionPathToCache() {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SESSION_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(sessionPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SESSION_ABSOLUTE_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(sessionPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SESSION_PATH_CURRENT, spinner.getSelectItem());
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    public ISaveDetail getSaveDetail() {
        return null;
    }

    TopViewSpinner.onItemSelectListener onItemSelectListener = fileBean -> {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SESSION_PATH_CURRENT, fileBean.getPath());
//        DToast.get().show(str);
        addSelectToPathSet(fileBean);
    };


    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };


    private void setCache() {
        sessionPathSet.clear();
        String pathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SESSION_PATH);
        String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SESSION_ABSOLUTE_PATH);
        String currentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SESSION_PATH_CURRENT);
        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);

        ArrayList<FileBean> dataList = new ArrayList<>();
        FileBean currentBean = new FileBean();
        for (int i = 0; i < abPathCacheList.size(); i++) {
            FileBean fileBean = new FileBean();
            fileBean.setPath(abPathCacheList.get(i));
            fileBean.setDisplayName(pathCacheList.get(i));
            if(abPathCacheList.get(i).equals(currentPath)) {
                currentBean.setPath(abPathCacheList.get(i));
                currentBean.setDisplayName(pathCacheList.get(i));
            }
            dataList.add(fileBean);
        }

        sessionPathSet.addAll(dataList);
        spinner.updateDataList(getSessionPathList(), null);

        String sessionName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SESSION_NAME);
        if (sessionName.isEmpty()) {
            sessionName = Tools.generateName();
        }
        saveNameEdit.setText(sessionName);

        boolean isFileNumAddCheck = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK);
        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK_NUM);
        txtSuffixNum.setEnabled(isFileNumAddCheck);
        txtSuffixNum.setText(suffixNum);
        if (fileNameAdd.isChecked() != isFileNumAddCheck) {
            fileNameAdd.setChecked(isFileNumAddCheck);
        } else {
            onCheckBoxChangedListener.onCheckedChanged(fileNameAdd, isFileNumAddCheck);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            if (v.getId() == btnBrowse.getId()) {
                handleBrowseClick();
            } else if (v.getId() == btnSave.getId()) {
                String spinnerSelectPath = spinner.getSelectItem();
                selectIsFast32 = fileSelector.isFAT32(spinnerSelectPath);
                saveState();
            } else if (v.getId() == txtSuffixNum.getId()) {
                dialogKeyBoard.setDecimalData(3, IDigits.DIGITS_10, onNumSubFixListener);
            }
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onNumSubFixListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result);
        }
    };


    private void onTextListener(String text) {
        txtSuffixNum.setText(text);
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK_NUM, text);
    }



    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();
            if (v.getId() == saveNameEdit.getId()) {
                String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK_NUM);
                String suffix = "_" + suffixNum;
                if (text.endsWith(suffix)) {
                    text = text.substring(0, text.length() - suffix.length());
                }
                layoutTextKeyBoard.setData(text, TopDialogTextKeyBoard.HANDLE_TYPE_SAVE_SESSION, TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() {
                    @Override
                    public void onDismiss(String result) {
                        saveNameEdit.setText(result);
                        txtSuffixNum.setText("000");
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SESSION_NAME, result);
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK_NUM, "000");
                    }
                });
            }
        }
    };


    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == fileNameAdd.getId()) {
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK, String.valueOf(isChecked));
                txtSuffixNum.setEnabled(isChecked);
                RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE, CacheUtil.SAVE_TYPE_SESSION + CacheUtil.WAVE_STORE_PATH_SLIP + isChecked);
            }
        }
    };

    private void handleBrowseClick() {
        String spinnerSelectPath= spinner.getSelectItem();
        String disPlay = spinner.getDisPlaySelectItem();
        File file = new File(spinnerSelectPath);

        if(!file.exists() || !file.isDirectory()){
            spinnerSelectPath = "/storage/emulated/0";
            disPlay = context.getResources().getString(R.string.internal_storage);
        }

        fileSelector.buildSaveFileSelector(spinnerSelectPath, disPlay, this, context);
    }


    private void saveState() {

        if (StrUtil.isEmpty(spinner.getSelectItem())) {
            DToast.get().show(R.string.top_slip_directory_save_to);
            return;
        }

        final String path = spinner.getSelectItem() + File.separator + getFinaleName() + ".mss";
        if (SaveManage.getInstance().checkFileExists(path)) {
//            DToast.get().show(String.format(getString(R.string.msgTopSaveNameExisted), saveNameEdit.getText() + ".mss"));
//            autoAddSuffixNum();
            dialogOk.setData(btnSave, R.string.top_slip_save_file_exists, path, null, onOkCancelClickListener);
        } else {
            doSaveSession(path);
        }
    }

    private String getFinaleName() {
        String finalName = saveNameEdit.getText();
        if(fileNameAdd.isChecked()) {
            finalName = finalName + "_" + txtSuffixNum.getText();
        }
        return finalName;
    }


    boolean bSaveSession = false;
    synchronized boolean isSaveSession(){
        return bSaveSession;
    }
    synchronized void setSaveSession(boolean bSave){
        this.bSaveSession = bSave;
    }
    private void doSaveSession(String filePath) {
        if(isSaveSession()){
            return;
        }
        if (!FileUtils.checkFolderExists(spinner.getSelectItem(),context.getResources().getString(R.string.internal_storage))) {
            DToast.get().show(R.string.top_slip_save_wave_path_unable);
            return;
        }
        setSaveSession(true);

        Scope scope = Scope.getInstance();
        boolean oldIsRun = scope.isRun();
        if (scope.isRun()) {
            //scope.setRun(false);
            Command.get().getFunctionMenu().Stop(true);
        }else{
            Command.get().getSample().SegmentedStop(true);
        }
        int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
        CacheUtil.get().putMapInForce(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + "-1", String.valueOf(channelSelect));
        new Thread(() -> {
            ms_sleep(1000);
            SaveRecoverySession saveRecoverySession = SaveRecoverySession.getInstance();
            long needSize = saveRecoverySession.estimateStorage();
            boolean canSave = Utils.isDiskAvaiable(new File(filePath), needSize);
            Logger.i(TAG, "SaveSessionNeedSize= " + needSize + " ,canSave= " + canSave + " ,selectIsFast32= " + selectIsFast32);
            if (selectIsFast32) {
                if (needSize >= 4 * 1024 * 1024 * 1024L) {
                    noEnoughSpace(true, scope, oldIsRun, filePath);
                    return;
                }
            }
            if (!canSave) {
                noEnoughSpace(false, scope, oldIsRun, filePath);
                return;
            }
            HashMap<String, HashMap<String, String>> map = new HashMap<>();
            map.put(CacheUtil.DefaultSaveName, CacheUtil.get().getCurrMap());
            map.put(CacheUtil.OtherDefaultSaveName, CacheUtil.get().getCurrOtherMap());
            saveRecoverySession.store(map, filePath);
            while (!saveRecoverySession.isDone()) {
                ms_sleep(100);
                Log.d("SaveRecoverySession", "store progress:" + saveRecoverySession.getSaveRecoveryProgress());
                showSaveProgress(saveRecoverySession.getSaveRecoveryProgress());
            }

            toastStr = context.getResources().getString(R.string.top_slip_save_session_success);
            boolean saveSuccess = true;
            if (saveRecoverySession.getStatus() == SaveRecoverySession.S_FAIL) {
                toastStr = context.getResources().getString(R.string.top_slip_save_session_failed);
                saveSuccess = false;
                showSaveProgress(100);
            }

            boolean finalSaveSuccess = saveSuccess;
            boolean finalOldState = oldIsRun;
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("SaveRecoverySession", toastStr);
                    ScreenControls screenControls = ScreenControls.getInstance();
                    if (screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                        screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);
                    }
//                    if (!scope.isRun()) {
//                        scope.setRun(true);
//                    }
                    if (scope.isRun() != finalOldState) {
                        //scope.setRun(finalOldState);
                        if(finalOldState){
                            Command.get().getFunctionMenu().Run(true);
                        }else{
                            Command.get().getFunctionMenu().Stop(true);
                        }
                    }
                    if (finalSaveSuccess) { //保存成功
                        autoAddSuffixNum();
                        FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                        toastStr = "保存成功";
                    } else { //保存失败
                        FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                        toastStr = "保存失败";
                    }
                    DToast.get().show(toastStr);
                    setSaveSession(false);
                }
            });
        }).start();
    }

    private void noEnoughSpace(boolean isCausedByFast32, Scope scope, boolean oldIsRun, String filePath) {
        requireActivity().runOnUiThread(() -> {
            String msg = "";
            if (isCausedByFast32) {
                msg = context.getResources().getString(R.string.storage_type_not_avaiable);
            } else {
                msg = context.getResources().getString(R.string.no_storage_space);
            }
            dialogOnlyOk.setData(msg, null, null);
            recoveryScopeState(scope, oldIsRun);
            FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
        });
    }

    private void recoveryScopeState(Scope scope, boolean finalOldState) {
        if (isSaveSession()) {
            setSaveSession(false);
        }
        if (scope.isRun() == finalOldState) return;
        if (finalOldState) {
            Command.get().getFunctionMenu().Run(true);
        } else {
            Command.get().getFunctionMenu().Stop(true);
        }
    }

    private void autoAddSuffixNum() { //文件名序号递增
        if (!fileNameAdd.isChecked()) return;
        int oldSuffixNum = Integer.parseInt(txtSuffixNum.getText().toString().trim());
        String tempNum = KeyBoardNumberUtil.toBits((oldSuffixNum + 1) + "", 3);
        if (onNumSubFixListener != null) {
            onNumSubFixListener.onDismiss(tempNum);
        }
    }

    private void showSaveProgress(int progress) {
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


    private void ms_sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener = new DialogOkCancel.OnOkCancelClickListener() {
        @Override
        public void onOkClick(View v, Object data) {
            Logger.i("Click ok");
            if (v == null || data == null) return;
            FileUtils.createBakFile((String) data);
            doSaveSession((String) data);
        }

        @Override
        public void onCancelClick(View v, Object data) {
            //Do nothing
            Logger.i("Click cancel");
        }

        @Override
        public void onDialogClose(View view) {
        }
    };

}