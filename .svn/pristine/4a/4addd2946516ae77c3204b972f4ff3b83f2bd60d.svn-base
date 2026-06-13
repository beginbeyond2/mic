package com.micsig.tbook.tbookscope.top.layout.save;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight;
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
import com.micsig.tbook.ui.MMainMenuChannel;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.TopViewSpinner;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.channel.TopViewChannel;
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.FileBeanToStr;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.wavezone.TChan;
import com.molihuan.pathselector.entity.FileBean;
import com.molihuan.pathselector.fragment.impl.PathSelectFragment;
import com.molihuan.pathselector.utils.DToastDialog;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/5.
 */

public class TopLayoutSaveWav extends Fragment {
    private static final String TAG = "TopLayoutSave";
    public static final int SAVEINLOCAL = 0;// Tools.SaveType_LOCAL
    public static final int SAVEINUDISK = 1;// Tools.SaveType_UDISK

    private Context context;
    private TopViewRadioGroup saveTo;
    private TopViewEdit saveNameEdit;
    private MMainMenuChannel viewChannel;
    private TopDialogTextKeyBoard layoutTextKeyBoard, fileSelectorTextKeyBoard;
    private TopMsgSaveWave topMsgSaveWave;
    private Button btnSave;
    private TopViewSpinner spinner;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    private Button saveTest;
    private PathSelectFragment selector;

    private Button btnBrowse;
    private CheckBox checkFileNameAdd;
    private TextView txtSuffixNum;
    private final FixedSizeHashSet<FileBean> pathSet = new FixedSizeHashSet<>(10);
    protected TopDialogNumberKeyBoard dialogKeyBoard;

    public WindowManager windowManager;

    public DToastDialog dToastdialog = new DToastDialog();
    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //S1--S4
    private boolean[] channelShow = {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false
    };

    private TopMsgSaveSegments msgSaveSegments = new TopMsgSaveSegments();
    private DialogOkCancel dialogOk;

    private FileSelector fileSelector ;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_wav, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getContext();
        initView(view);
        initControl();
    }

    private void initView(View view) {
        viewChannel = view.findViewById(R.id.chanSaveWave);
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener);

        saveNameEdit = (TopViewEdit) view.findViewById(R.id.saveName);
        saveNameEdit.setOnClickEditListener(onClickEditListener);
        btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(onClickListener);
        saveTo = (TopViewRadioGroup) view.findViewById(R.id.saveTo);
        saveTo.setData(R.string.save_saveto, R.array.saveTo, onCheckChangedListener);

        spinner = view.findViewById(R.id.topSpinner);
        spinner.setData(context.getResources().getString(R.string.top_save_wave_directory),
                getPreviousDirectory(), R.layout.layout_item_for_save_directory, onItemSelectListener);
        btnBrowse = view.findViewById(R.id.btn_browse);
        btnBrowse.setOnClickListener(onClickListener);
        txtSuffixNum = view.findViewById(R.id.txt_index_num);
        txtSuffixNum.setOnClickListener(onClickListener);
        checkFileNameAdd = view.findViewById(R.id.check_file_name_add);
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all);
        checkFileNameAdd.setBackground(null);
        checkFileNameAdd.setButtonDrawable(null);
        drawable.setBounds(0, 0, 22, 22);
        checkFileNameAdd.setCompoundDrawables(drawable, null, null, null);
        checkFileNameAdd.setOnCheckedChangeListener(onCheckBoxChangedListener);


        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);

        topMsgSaveWave = new TopMsgSaveWave(new boolean[]{true, true, true});

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
        dialogOk = (DialogOkCancel) ((MainActivity) context).getMainViewGroup().getDialog(MainViewGroup.DIALOG_OKCANCEL);
        fileSelector = new FileSelector(context,(selectedPath) -> {
//            test(selectedPath);
            addPathToPathSet(selectedPath);
        });
    }

    private ArrayList<FileBean> getPreviousDirectory() {
        return pathSet.getReverseList();
    }

    private void addPathToPathSet(FileBean pathStr) {
        handleAddPath(pathStr);
        pathSet.add(pathStr);
        spinner.updateDataList(getPreviousDirectory(), null);
        savePathToCache();
    }

    private boolean handleAddPath(FileBean pathStr) {
        boolean canAdd = true;
        FileBean temp = null;
        for (FileBean fileBean : pathSet) {
            if (fileBean.getPath().equals(pathStr.getPath())) {
                temp = fileBean;
                canAdd = false;
                break;
            }
        }
        if (temp != null) {
            pathSet.remove(temp);
        }
        return canAdd;
    }

    public void savePathToCache() {

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH + CacheUtil.WAVE_TYPE_WAV,
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_WAV,
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_WAV, spinner.getSelectItem());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_QUICKSAVE).subscribe(consumerMainBottomQuickSave);
//        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_KEYBOARD).subscribe(consumerHideKeyboard);
        EventFactory.addEventObserver(EventFactory.EVENT_SAVEBIN_RUN, eventSaveBinObserver);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);
//        setChannelShow();
    }

    private void setCache() {
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT);

        String waveName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_NAME + CacheUtil.WAVE_TYPE_WAV);
        if (waveName.isEmpty()) {
            waveName = Tools.generateName();
        }
        saveNameEdit.setText(waveName);
        channelShowChange();
        Command.get().getStorage().Save(channelSelect, SAVEINLOCAL, false);
        Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false);
        Command.get().getStorage().Save_Type(0, false);
        Command.get().getStorage().Save_ALLSegments(false, false);

        viewChannel.setChangeListener(null, null);
        viewChannel.setChecked(channelSelect);
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener);
        saveTo.clearCheck();
        boolean ch1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch1);
        boolean ch2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch2);
        boolean ch3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch3);
        boolean ch4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch4);
        boolean ch5 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch5);
        boolean ch6 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch6);
        boolean ch7 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch7);
        boolean ch8 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch8);

        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
            channelShow[0] = ch1;
            channelShow[1] = ch2;
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {
            channelShow[0] = ch1;
            channelShow[1] = ch2;
            channelShow[2] = ch3;
            channelShow[3] = ch4;
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {
            channelShow[0] = ch1;
            channelShow[1] = ch2;
            channelShow[2] = ch3;
            channelShow[3] = ch4;
            channelShow[4] = ch5;
            channelShow[5] = ch6;
            channelShow[6] = ch7;
            channelShow[7] = ch8;
        }
        TChan.foreachCh1ToR8(chan -> {
            if (TChan.isMath(chan)) {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            }
            if (TChan.isRef(chan)) {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            }
        });
        setChannelShow();
        viewChannel.getViewChannelMultipleChoice().unCheckAll();

        restorePath();

        boolean isFileNumAddCheck = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK + CacheUtil.WAVE_TYPE_WAV);
        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_WAV);
        txtSuffixNum.setEnabled(isFileNumAddCheck);
        txtSuffixNum.setText(suffixNum);
        if (checkFileNameAdd.isChecked() != isFileNumAddCheck) {
            checkFileNameAdd.setChecked(isFileNumAddCheck);
        } else {
            onCheckBoxChangedListener.onCheckedChanged(checkFileNameAdd, isFileNumAddCheck);
        }
    }

    private void restorePath() {
        pathSet.clear();
        String pathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH + CacheUtil.WAVE_TYPE_WAV);//显示路径
        String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_WAV);//绝对路径

        String currentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_WAV);

        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);

//        if (!Tools.fileIsExists(currentPath)) {
//            currentPath = null;
//        }


        ArrayList<FileBean> dataList = new ArrayList<>();
        FileBean currentBean = new FileBean();
        for (int i = 0; i < abPathCacheList.size(); i++) {
            if (!Tools.fileIsExists(abPathCacheList.get(i))) continue;
            FileBean fileBean = new FileBean();
            fileBean.setPath(abPathCacheList.get(i));
            fileBean.setDisplayName(pathCacheList.get(i));
            Logger.i(TAG, "currentPath= " + currentPath + " ,pathCacheList= " + pathCacheList.get(i));
            if(abPathCacheList.get(i).equals(currentPath)) {
                currentBean.setPath(abPathCacheList.get(i));
                currentBean.setDisplayName(pathCacheList.get(i));
            }
            dataList.add(fileBean);
        }

        pathSet.addAll(dataList);
        spinner.updateDataList(getPreviousDirectory(), null);

//        for (FileBean pathStr : dataList) {
//            addPathToPathSet(pathStr);
//        }
    }

    private void sendMsg() {
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, false);
        }
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    public ISaveDetail getSaveDetail() {
        return topMsgSaveWave;
    }

    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() {
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception {
            TChan.foreachChan(chan -> {
                boolean isOpen = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + chan);
                channelShow[TChan.toFpgaChNo(chan)] = msgChannels.getCh(TChan.toFpgaChNo(chan)).isValue();
            });
            setChannelShow();
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception {
            TChan.foreachMath(chan -> {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            });
            TChan.foreachRef(chan -> {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            });
            setChannelShow();
        }
    };

    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() {
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception {
            //哪个通道变化 设置哪个通道
            boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + msgRef.getRefChannelNumber());
            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = msgRef.getRefChecked().isValue() && isAddByUser;
            setChannelShow();
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSaveWave, true);
        }
    };

    private void setChannelShow() {
        viewChannel.setItemVisible(channelShow, true);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL);
    }

    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return;
            Logger.i(TAG, "selectColorInfo= " + colorInfo);
            String[] info = colorInfo.split(";");
            int chIndex = Integer.parseInt(info[0]);
            String colorStr = info[1];
            viewChannel.setChannelColor(chIndex, colorStr);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_STOTAGE_SAVE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int channelIndex = Integer.parseInt(params[0]);
                    int saveIndex = Integer.parseInt(params[1]);
                    if (!ChannelFactory.isChOpen(channelIndex)) {
                        return;
                    }
                    viewChannel.setChecked(channelIndex);
                    break;
                }
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_SOURCE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int channelIndex = Integer.parseInt(params[0]);
                    if (!ChannelFactory.isChOpen(channelIndex)) {
                        return;
                    }
                    viewChannel.setChecked(channelIndex);
                }
                break;
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_LOCATION: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int saveIndex = Integer.parseInt(params[0]);
                }
                break;
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_FILENAME: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    String fileName = (params[0]);
                    saveNameEdit.setText(fileName);
                }
                break;
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_START: {
                    onClickListener.onClick(btnSave);
                }
                break;
            }
        }
    };

    private Consumer<Boolean> consumerMainBottomQuickSave = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false);
            if (aBoolean) {//快速保存成功
                autoAddSuffixNum();
            }
        }
    };


    private TopMsgSaveRef msgSaveRef = new TopMsgSaveRef();

    TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() {
        @Override
        public void checkChanged(int viewId, int checkedIndex, RadioButton radioButton) {
            PlaySound.getInstance().playButton();
            Command.get().getStorage().Save_Source(checkedIndex, false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT, String.valueOf(checkedIndex));
        }
    };

    TopViewChannelMultipleChoice.onTestListener onChannelItemTestListener = new TopViewChannelMultipleChoice.onTestListener() {
        @Override
        public void onTest(CheckBox checkBox) {
//            List<Integer> lisSelects = viewChannel.getViewChannelMultipleChoice().getSelectChannel();
//            boolean showBin = true;
//            for (Integer chanIdx : lisSelects) {
//                if (ChannelFactory.isMathCh(chanIdx) || ChannelFactory.isRefCh(chanIdx)) {
//                    showBin = false;
//                    break;
//                }
//            }
//            saveType.setEnabled(2, showBin);
//            topMsgSaveWave.setSaveTypeEnable(2, showBin);
//            updateSaveToState();
        }
    };

    TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {

        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {
            DToast.get().show(R.string.topSaveSaveInToast);
        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            onCheckChanged(view, item);
        }
    };

    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();
            if (v.getId() == saveNameEdit.getId()) {
                String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_WAV);
                String suffix = "_" + suffixNum;
                if (text.endsWith(suffix)) {
                    text = text.substring(0, text.length() - suffix.length());
                }
                layoutTextKeyBoard.setData(text, TopDialogTextKeyBoard.HANDLE_TYPE_SAVE_SESSION, TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() {
                    @Override
                    public void onDismiss(String result) {
                        saveNameEdit.setText(result);
                        Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false);
                        txtSuffixNum.setText("000");
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_NAME + CacheUtil.WAVE_TYPE_WAV, result);
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_WAV, "000");
                    }
                });
            }
        }
    };

    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item) {
        if (view.getId() == R.id.saveTo) {
            msgSaveRef.setFromIdChannelFactory(viewChannel.getSelectChannel());
            msgSaveRef.setSaveToRefId(item.getIndex() + 1);
            RxBus.getInstance().post(RxEnum.TOPSLIP_SAVE_REF, msgSaveRef);
            handler.sendEmptyMessageDelayed(1, 500);
        }
    }

    public void channelShowChange() {
        viewChannel.setAllSelectShow(false);//csv时显示多选控件
        setChannelShow();
//        updateSaveToState();
    }

//    private void updateSaveToState() {
//        int type = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_TYPE);
//        int selectCount = viewChannel.getViewChannelMultipleChoice().getSelectCount();
//        boolean disAble = type == 1 && selectCount > 1;//只有此时需要disable
//        saveTo.setEnabled(!disAble);
//    }

    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == checkFileNameAdd.getId()) {
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK + CacheUtil.WAVE_TYPE_WAV, String.valueOf(isChecked));
                txtSuffixNum.setEnabled(isChecked);
                RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE, CacheUtil.WAVE_TYPE_WAV + CacheUtil.WAVE_STORE_PATH_SLIP + isChecked);
            }
        }
    };


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    saveTo.clearCheck();
                    break;
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            if (v.getId() == btnSave.getId()) {
                String finalInput = getFinaleName();
                String filePath = spinner.getSelectItem() + File.separator + finalInput + ".mwav";

                if (FileUtils.checkFileExists(filePath)) {
//                    DToast.get().show(String.format(getString(R.string.msgTopSaveNameExisted), finalInput + suffix));
//                    autoAddSuffixNum();
                    dialogOk.setData(btnSave, R.string.top_slip_save_file_exists, filePath, null, onOkCancelClickListener);
                } else {
                    doSaveWave(filePath);
                }
            } else if (v.getId() == btnBrowse.getId()) {
                handleBrowseClick();
            } else if (v.getId() == txtSuffixNum.getId()) {
                dialogKeyBoard.setDecimalData(3, IDigits.DIGITS_10, onNumSubFixListener);
            }
        }
    };

    private String getFinaleName() {
        String finalName = saveNameEdit.getText();
        if(checkFileNameAdd.isChecked()) {
            finalName = finalName + "_" + txtSuffixNum.getText();
        }
        return finalName;
    }


    private void doSaveWave(String filePath) {
        String finalInput = getFinaleName();
        int ch = viewChannel.getSelectChannel();

        if (StrUtil.isEmpty(spinner.getSelectItem())) {
            DToast.get().show(R.string.top_slip_directory_save_to);
            return;
        }

        if (!FileUtils.checkFolderExists(spinner.getSelectItem(), context.getResources().getString(R.string.internal_storage))) {
            DToast.get().show(R.string.top_slip_save_wave_path_unable);
            return;
        }

        if (!ChannelFactory.isChOpen(ch)) {
            DToast.get().show(R.string.msgTopSaveCsvNotSelect);
            return;
        }

        ScreenControls screenControls = ScreenControls.getInstance();
        screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);
        SaveManage.getInstance().allSaveEntrance(ch, 0, spinner.getSelectItem(), finalInput, null, new SaveManage.SaveCallBack() {
            @Override
            public void onResult(boolean success, String msg) {
                ScreenControls screenControls = ScreenControls.getInstance();
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);
                if (success) {
                    SaveManage.getInstance().putCacheName(finalInput);
                    FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                    autoAddSuffixNum();
                } else {
                    FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                }
                DToast.get().show(msg);
            }
        });
        Command.get().getStorage().Save_Filename(finalInput, false);
    }

    private void autoAddSuffixNum() {//文件名序号递增
        if (!checkFileNameAdd.isChecked()) return;
        int oldSuffixNum = Integer.parseInt(txtSuffixNum.getText().toString().trim());
        String tempNum = KeyBoardNumberUtil.toBits((oldSuffixNum + 1) + "", 3);
        if (onNumSubFixListener != null) {
            onNumSubFixListener.onDismiss(tempNum);
        }
    }


    private TopDialogNumberKeyBoard.OnDismissListener onNumSubFixListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result);
        }
    };

    private void onTextListener(String text) {
        txtSuffixNum.setText(text);
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_WAV, text);
    }

    private void handleBrowseClick() {
        String spinnerSelectPath= spinner.getSelectItem();
        String disPlay = spinner.getDisPlaySelectItem();
        File file = new File(spinnerSelectPath);

        if(!file.exists() || !file.isDirectory()){
            spinnerSelectPath = "/storage/emulated/0";
            disPlay = context.getResources().getString(R.string.internal_storage);
        }
        this.selector = fileSelector.buildSaveFileSelector(spinnerSelectPath, disPlay, this, context);
    }
    public void test(FileBean fileBean){
        String display = fileBean.getDisplayName();
        String path = fileBean.getPath();
    }
    TopViewSpinner.onItemSelectListener onItemSelectListener = new TopViewSpinner.onItemSelectListener() {
        @Override
        public void onItemSelected(FileBean str) {
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_WAV, str.getPath());
            //选中的记录置顶
            addPathToPathSet(str);
        }
    };

    EventUIObserver eventSaveBinObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase base = (EventBase) data;
            if (base == null) return;
            int progress = 0;
            switch (base.getId()) {
                case EventFactory.EVENT_SAVEBIN_RUN:
                    progress = (int) ((EventBase) data).getData();
                    break;
            }
            ScreenControls screenControls = ScreenControls.getInstance();
            if (progress < 0 || progress >= 100) {
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);
                } else {
                    screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);
                }
            } else {
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);
                }
                screenControls.setProgressValue(progress);
            }
        }
    };


    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener = new DialogOkCancel.OnOkCancelClickListener() {
        @Override
        public void onOkClick(View v, Object data) {
            Logger.i("Click ok");
            if (v == null || data == null) return;
            FileUtils.createBakFile((String) data);
            doSaveWave((String) data);
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
