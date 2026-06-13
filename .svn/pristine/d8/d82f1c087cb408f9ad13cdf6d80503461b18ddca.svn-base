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
import com.micsig.tbook.hardware.HwManager;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
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
import com.micsig.tbook.ui.MSwitchBox;
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


public class TopLayoutSavePicture extends Fragment {

    private static final String TAG = "TopLayoutSavePicture";
    private Context context;
    private MSwitchBox saveTimestamp;
    private MSwitchBox saveInverseColor;
    private MSwitchBox saveThumbnail;
    private TopViewSpinner spinner;
    private TopViewEdit saveNameEdit;
    private CheckBox fileNameAdd;
    private TextView txtSuffixNum;
    private Button btnSave, btnBrowse;
    private final FixedSizeHashSet<FileBean> picturePathSet = new FixedSizeHashSet<>(10);
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
        return inflater.inflate(R.layout.layout_save_picture, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initView(View view) {
        saveTimestamp = (MSwitchBox) view.findViewById(R.id.savePictureTimestampDetail);
        saveInverseColor = (MSwitchBox) view.findViewById(R.id.savePictureInverseColorDetail);
        saveThumbnail = (MSwitchBox) view.findViewById(R.id.savePictureThumbnailDetail);

        saveTimestamp.setOnToggleStateChangedListener(onToggleStateChangedListener);
        saveInverseColor.setOnToggleStateChangedListener(onToggleStateChangedListener);
        saveThumbnail.setOnToggleStateChangedListener(onToggleStateChangedListener);

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
                getPicturePathList(), R.layout.layout_item_for_save_directory, onItemSelectListener);
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

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SAVE_CAPTURE_SUCCESS).subscribe(consumerCaptureState);
    }

    private ArrayList<FileBean> getPicturePathList() {
        return picturePathSet.getReverseList();
    }

    private void addSelectToPathSet(FileBean pathStr) {
        handleAddPath(pathStr);
        picturePathSet.add(pathStr);
        spinner.updateDataList(getPicturePathList(), null);
        savePicturePathToCache();
        ((MainActivity) (context)).setScreenshotParam();
    }

    private boolean handleAddPath(FileBean pathStr) {
        boolean canAdd = true;
        FileBean temp = null;
        for (FileBean fileBean : picturePathSet) {
            if (fileBean.getPath().equals(pathStr.getPath())) {
                canAdd = false;
                temp = fileBean;
                break;
            }
        }
        if (temp != null) {
            picturePathSet.remove(temp);
        }
        return canAdd;
    }

    public void savePicturePathToCache() {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(picturePathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_ABSOLUTE_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(picturePathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_CURRENT, spinner.getSelectItem());
    }

    TopViewSpinner.onItemSelectListener onItemSelectListener = str -> {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_CURRENT, str.getPath());
        addSelectToPathSet(str);
    };

    private void setCache() {
        boolean timestamp = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP);
        saveTimestamp.setState(timestamp);
        boolean screenInvert = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT);
        saveInverseColor.setState(screenInvert);
        boolean thumbnail = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SAVETHUMBNAIL);
        saveThumbnail.setState(thumbnail);

        Command.get().getStorage().Capture_Incolor(screenInvert,false);
        Command.get().getStorage().Capture_Time(timestamp,false);
        Command.get().getStorage().Capture_Thumbnail(thumbnail, false);

        picturePathSet.clear();
        String pathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH);
        String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_ABSOLUTE_PATH);
        String currentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_CURRENT);
        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);

        ArrayList<FileBean> dataList = new ArrayList<>();
        FileBean currentBean = new FileBean();
        for (int i = 0; i < abPathCacheList.size(); i++) {
            FileBean fileBean = new FileBean();
            fileBean.setPath(abPathCacheList.get(i));
            fileBean.setDisplayName(pathCacheList.get(i));
            if (abPathCacheList.get(i).equals(currentPath)) {
                currentBean.setPath(abPathCacheList.get(i));
                currentBean.setDisplayName(pathCacheList.get(i));
            }
            dataList.add(fileBean);
        }

        picturePathSet.addAll(dataList);
        spinner.updateDataList(getPicturePathList(), null);

        String pictureName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_NAME);
        if (pictureName.isEmpty()) {
            pictureName = Tools.generateName();
        }
        saveNameEdit.setText(pictureName);


        boolean isFileNumAddCheck = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK);
        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK_NUM);
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
                savePicture();
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
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK_NUM, text);
    }


    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI=new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()){
                case CommandMsgToUI.FLAG_STOTAGE_CAPTURE_INCOLOR:{
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    saveInverseColor.setState(b);
                    onToggleStateChangedListener.onToggleStateChanged(saveInverseColor,b);
                }break;
                case CommandMsgToUI.FLAG_STOTAGE_CAPTURE_TIME:{
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    saveTimestamp.setState(b);
                    onToggleStateChangedListener.onToggleStateChanged(saveTimestamp,b);
                }break;
                case CommandMsgToUI.FLAG_STOTAGE_CAPTURE_THUMBNAIL:{
                    boolean b = Boolean.parseBoolean(commandMsgToUI.getParam());
                    saveThumbnail.setState(b);
                    onToggleStateChangedListener.onToggleStateChanged(saveThumbnail,b);
                }break;
            }
        }
    };

    MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            if (view.getId() == R.id.savePictureTimestampDetail) {
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP, String.valueOf(state));
                Command.get().getStorage().Capture_Time(state,false);
            } else if (view.getId() == R.id.savePictureInverseColorDetail) {
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT, String.valueOf(state));
                Command.get().getStorage().Capture_Incolor(state,false);
            } else if (view.getId() == R.id.savePictureThumbnailDetail) {
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_SAVETHUMBNAIL, String.valueOf(state));
                Command.get().getStorage().Capture_Thumbnail(state, false);
            }
            ((MainActivity) (context)).setScreenshotParam();
        }
    };

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    public ISaveDetail getSaveDetail() {
        return null;
    }

    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();
            if (v.getId() == saveNameEdit.getId()) {
                String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK_NUM);
                String suffix = "_" + suffixNum;
                if (text.endsWith(suffix)) {
                    text = text.substring(0, text.length() - suffix.length());
                }
                Logger.i("TopLayoutSavePicture", "EditName= " + saveNameEdit.getText() + " ,handleText= " + text);
                layoutTextKeyBoard.setData(text, TopDialogTextKeyBoard.HANDLE_TYPE_SAVE_SESSION, TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() {
                    @Override
                    public void onDismiss(String result) {
                        if (result.isEmpty()) {
                            DToast.get().show(R.string.file_name_not_null);
                            return;
                        }
                        saveNameEdit.setText(result);
                        txtSuffixNum.setText("000");
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_NAME, result);
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK_NUM, "000");
                        ((MainActivity) (context)).setScreenshotParam();
                    }
                });
            }
        }
    };


    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == fileNameAdd.getId()) {
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK, String.valueOf(isChecked));
                txtSuffixNum.setEnabled(isChecked);
                RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE, CacheUtil.SAVE_TYPE_PICTURE + CacheUtil.WAVE_STORE_PATH_SLIP + isChecked);
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


    private void savePicture() {
        if (StrUtil.isEmpty(spinner.getSelectItem())) {
            DToast.get().show(R.string.top_slip_directory_save_to);
            return;
        }
//        String fileName = getFinalName();
//        final String filePath = spinner.getSelectItem() + File.separator + fileName + ".png";
//        if (SaveManage.getInstance().checkFileExists(filePath)) {
//            dialogOk.setData(btnSave, R.string.top_slip_save_file_exists, filePath, null, onOkCancelClickListener);
//        } else {
            doSavePicture();
//        }
    }

    private void doSavePicture() {
        if (!FileUtils.checkFolderExists(spinner.getSelectItem(),context.getResources().getString(R.string.internal_storage))) {
            DToast.get().show(R.string.top_slip_save_wave_path_unable);
            return;
        }
        //保存图片
        ((MainActivity) getActivity()).screenShot();
//        autoAddSuffixNum();
    }

    private String getFinalName() {
        String finalName = saveNameEdit.getText();
        if(fileNameAdd.isChecked()) {
            finalName = finalName + "_" + txtSuffixNum.getText();
        }
        return finalName;
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

    private Consumer<Boolean> consumerHideKeyboard = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if(windowManager!=null && fileSelectorTextKeyBoard !=null){
                windowManager.removeView(fileSelectorTextKeyBoard);
                fileSelectorTextKeyBoard=null;
                windowManager=null;
            }
        }

    };

    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener = new DialogOkCancel.OnOkCancelClickListener() {
        @Override
        public void onOkClick(View v, Object data) {
            if (v == null || data == null) return;
            Logger.i(TAG, "savePicture data = " + data);
            doSavePicture();
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

    private Consumer<Boolean> consumerCaptureState = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Throwable {
            if (aBoolean) {
                autoAddSuffixNum();
            }
        }
    };


}
