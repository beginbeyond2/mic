package com.micsig.tbook.tbookscope.top.layout.save;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.FileUtils;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
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
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;

public class TopLayoutSaveSetting extends Fragment {
    private Context context;


    private TopViewSpinner spinner;
    private TopViewEdit saveNameEdit;
    private CheckBox fileNameAdd;
    private TextView txtSuffixNum;
    private Button btnSave, btnBrowse;
    private final FixedSizeHashSet<FileBean> settingPathSet = new FixedSizeHashSet<>(10);
    private TopDialogTextKeyBoard layoutTextKeyBoard,fileSelectorTextKeyBoard;
    private String toastStr = "";
    protected TopDialogNumberKeyBoard dialogKeyBoard;

    private WindowManager windowManager;

    public DToastDialog dToastdialog = new DToastDialog();
    private DialogOkCancel dialogOk;

    private FileSelector fileSelector ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_setting, container, false);
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
                getSettingPathList(), R.layout.layout_item_for_save_directory, onItemSelectListener);
        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);

        btnBrowse.setOnClickListener(onClickListener);
        btnSave.setOnClickListener(onClickListener);
        txtSuffixNum.setOnClickListener(onClickListener);
        saveNameEdit.setOnClickEditListener(onClickEditListener);
        fileNameAdd.setOnCheckedChangeListener(onCheckBoxChangedListener);

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
        dialogOk = (DialogOkCancel) ((MainActivity) context).getMainViewGroup().getDialog(MainViewGroup.DIALOG_OKCANCEL);
        fileSelector = new FileSelector(context,(selectedPath) -> {
            addSelectToPathSet(selectedPath);
        });
    }


    private ArrayList<FileBean> getSettingPathList() {
        return settingPathSet.getReverseList();
    }

    private void addSelectToPathSet(FileBean pathStr) {
        handleAddPath(pathStr);
        settingPathSet.add(pathStr);
        spinner.updateDataList(getSettingPathList(), null);
        saveSettingPathToCache();
    }

    private boolean handleAddPath(FileBean pathStr) {
        boolean canAdd = true;
        FileBean temp = null;
        for (FileBean fileBean : settingPathSet) {
            if (fileBean.getPath().equals(pathStr.getPath())) {
                canAdd = false;
                temp = fileBean;
                break;
            }
        }
        if (temp != null) {
            settingPathSet.remove(temp);
        }
        return canAdd;
    }

    public void saveSettingPathToCache() {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(settingPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_ABSOLUTE_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(settingPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH_CURRENT, spinner.getSelectItem());
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    public ISaveDetail getSaveDetail() {
        return null;
    }

    TopViewSpinner.onItemSelectListener onItemSelectListener = str -> {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH_CURRENT, str.getPath());
//        DToast.get().show(str);
        addSelectToPathSet(str);
    };


    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };


    private void setCache() {
        settingPathSet.clear();
        String pathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH);
        String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_ABSOLUTE_PATH);
        String currentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH_CURRENT);
        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        ArrayList<String> adPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);

        ArrayList<FileBean> dataList = new ArrayList<>();
        FileBean currentBean = new FileBean();
        for (int i = 0; i < adPathCacheList.size(); i++) {
            FileBean fileBean = new FileBean();
            fileBean.setPath(adPathCacheList.get(i));
            fileBean.setDisplayName(pathCacheList.get(i));
            if(adPathCacheList.get(i).equals(currentPath)) {
                currentBean.setPath(adPathCacheList.get(i));
                currentBean.setDisplayName(pathCacheList.get(i));
            }
            dataList.add(fileBean);
        }

        settingPathSet.addAll(dataList);
        spinner.updateDataList(getSettingPathList(), null);

        String settingName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_NAME);
        if (settingName.isEmpty()) {
            settingName = Tools.generateName();
        }
        saveNameEdit.setText(settingName);


        boolean isFileNumAddCheck = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK);
        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM);
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
                saveSetting();
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
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM, text);

    }


    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();
            if (v.getId() == saveNameEdit.getId()) {
                String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM);
                String suffix = "_" + suffixNum;
                if (text.endsWith(suffix)) {
                    text = text.substring(0, text.length() - suffix.length());
                }
                layoutTextKeyBoard.setData(text, TopDialogTextKeyBoard.HANDLE_TYPE_SAVE_SESSION, TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() {
                    @Override
                    public void onDismiss(String result) {
                        saveNameEdit.setText(result);
                        txtSuffixNum.setText("000");
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_NAME, result);
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM, "000");
                    }
                });
            }
        }
    };


    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == fileNameAdd.getId()) {
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK, String.valueOf(isChecked));
                txtSuffixNum.setEnabled(isChecked);
                RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE, CacheUtil.SAVE_TYPE_SETTING + CacheUtil.WAVE_STORE_PATH_SLIP + isChecked);
            }
        }
    };

    private void handleBrowseClick() {
        String spinnerSelectedPath = spinner.getSelectItem();
        String disPlay = spinner.getDisPlaySelectItem();
        File file = new File(spinnerSelectedPath);

        if(!file.exists() || !file.isDirectory()){
            spinnerSelectedPath = "/storage/emulated/0";
            disPlay = context.getResources().getString(R.string.internal_storage);
        }

        fileSelector.buildSaveFileSelector(spinnerSelectedPath, disPlay, this, context);
    }


    private void saveSetting() {
        if (StrUtil.isEmpty(spinner.getSelectItem())) {
            DToast.get().show(R.string.top_slip_directory_save_to);
            return;
        }
        final String filePath = spinner.getSelectItem() + File.separator + getFinaleName() + ".SaveRecovery";
        if (SaveManage.getInstance().checkFileExists(filePath)) {
//            DToast.get().show(String.format(getString(R.string.msgTopSaveNameExisted), saveNameEdit.getText() + ".SaveRecovery"));
//            autoAddSuffixNum();
            dialogOk.setData(btnSave, R.string.top_slip_save_file_exists, filePath, null, onOkCancelClickListener);
        } else {
            doSaveSetting(filePath);
        }
    }

    private String getFinaleName() {
        String finalName = saveNameEdit.getText();
        if(fileNameAdd.isChecked()) {
            finalName = finalName + "_" + txtSuffixNum.getText();
        }
        return finalName;
    }


    private void doSaveSetting(String filePath) {
        if (!FileUtils.checkFolderExists(spinner.getSelectItem(),context.getResources().getString(R.string.internal_storage))) {
            DToast.get().show(R.string.top_slip_save_wave_path_unable);
            return;
        }

        try {
            int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
            CacheUtil.get().putMapInForce(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + "-1", String.valueOf(channelSelect));
            SaveManage.getInstance().saveUserSetToPath(filePath, CacheUtil.get().getCacheMap(), new SaveManage.SaveCallBack() {
                @Override
                public void onResult(boolean success, String msg) {
                    DToast.get().show(msg);
                    Logger.i("SaveSetting isSuccess= " + success);
                    if (success) {
                        autoAddSuffixNum();
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void autoAddSuffixNum() {//文件名序号递增
        if (!fileNameAdd.isChecked()) return;
        int oldSuffixNum = Integer.parseInt(txtSuffixNum.getText().toString().trim());
        String tempNum = KeyBoardNumberUtil.toBits((oldSuffixNum + 1) + "", 3);
        if (onNumSubFixListener != null) {
            onNumSubFixListener.onDismiss(tempNum);
        }
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
            if (v == null || data == null) return;
            doSaveSetting((String) data);
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
