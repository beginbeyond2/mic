package com.micsig.tbook.tbookscope.top.layout.save;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.AutoSave;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.FileUtils;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.layout.measure.IMeasureDetail;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.util.FileSelector;
import com.micsig.tbook.tbookscope.util.TopViewTimeSelector;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
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

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/6.
 */
public class TopLayoutAutoSave extends Fragment {
    private static final String TAG = "TopLayoutCursorCommon";
    private Context context;
    private MMainMenuChannel viewChannel;

    private List<Integer> saveType = new ArrayList<>();
    private TopViewTimeSelector topViewStartTimeSelector, topViewStopTimeSelector;
    private boolean[] channelShow = {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false
    };

    private TopViewSpinner spinner;

    private TopViewRadioGroup rgStopCondition,intervalGroup,saveModeGroup;

    private CheckBox checkWAV,checkCSV,checkBIN,checkPicture,checkSession;

    private TextView frameStopText;
    private final FixedSizeHashSet<FileBean> pathSet = new FixedSizeHashSet<>(10);

    private Button startBtn, stopBtn, btnBrowse;

    private TopViewEdit saveNameEdit;
    private CheckBox checkFileNameAdd;

    private FileSelector fileSelector;

    private TopDialogTextKeyBoard layoutTextKeyBoard;

    private TextView txtSuffixNum;

    protected TopDialogNumberKeyBoard dialogKeyBoard;

    private TaskSuffixNumModel taskSuffixNumModel;

    private RelativeLayout multiSaveType;
    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //default
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_autosave, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initView(View view) {
        viewChannel = view.findViewById(R.id.chanAutoSave);
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener);
        spinner = view.findViewById(R.id.topSpinner);
        spinner.setData(context.getResources().getString(R.string.top_save_wave_directory),
                getPreviousDirectory(), R.layout.layout_item_for_save_directory, onItemSelectListener);
        topViewStartTimeSelector = view.findViewById(R.id.startCondition);
        topViewStartTimeSelector.setNowTime();
        topViewStopTimeSelector = view.findViewById(R.id.stopTimeCondition);
        topViewStopTimeSelector.setNowTime();
        rgStopCondition = view.findViewById(R.id.stopRadioGroup);
        frameStopText = view.findViewById(R.id.stopFrameCondition);
        startBtn = view.findViewById(R.id.btnStart);
        stopBtn = view.findViewById(R.id.btnStop);
        intervalGroup = view.findViewById(R.id.interval);
        saveModeGroup = view.findViewById(R.id.saveMode);
//        saveTypeGroup = view.findViewById(R.id.saveType);
        multiSaveType = view.findViewById(R.id.saveTypeMulti);
        checkWAV = view.findViewById(R.id.check_wav);
        checkCSV = view.findViewById(R.id.check_csv);
        checkBIN = view.findViewById(R.id.check_bin);
        checkPicture = view.findViewById(R.id.check_picture);
        checkSession = view.findViewById(R.id.check_session);
        checkWAV.setOnClickListener(onClickListener);
        checkCSV.setOnClickListener(onClickListener);
        checkBIN.setOnClickListener(onClickListener);
        checkPicture.setOnClickListener(onClickListener);
        checkSession.setOnClickListener(onClickListener);
        rgStopCondition.setData(R.string.stopCondition, R.array.stopCondition, onCheckChangedListener);
        intervalGroup.setData(R.string.interval,R.array.intervalTime,onCheckChangedListener);
        saveModeGroup.setData(R.string.autoSaveMode,R.array.autoSaveMode,onCheckChangedListener);
        btnBrowse = view.findViewById(R.id.btn_browse);
        btnBrowse.setOnClickListener(onClickListener);
        startBtn.setOnClickListener(onClickListener);
        stopBtn.setOnClickListener(onClickListener);
        checkFileNameAdd = view.findViewById(R.id.check_file_name_add);
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_grey);
        checkFileNameAdd.setBackground(null);
        checkFileNameAdd.setButtonDrawable(null);
        drawable.setBounds(0, 0, 22, 22);
        checkFileNameAdd.setCompoundDrawables(drawable, null, null, null);
        checkFileNameAdd.setOnCheckedChangeListener(onCheckBoxChangedListener);
        fileSelector = new FileSelector(context,(selectedPath) -> {
            addSelectToPathSet(selectedPath);
        });
        saveNameEdit = (TopViewEdit) view.findViewById(R.id.saveName);
        saveNameEdit.setOnClickEditListener(onClickEditListener);
        txtSuffixNum = view.findViewById(R.id.txt_index_num);
        txtSuffixNum.setOnClickListener(onClickListener);
        frameStopText.setOnClickListener(onClickListener);
        checkFileNameAdd.setEnabled(false);
        checkFileNameAdd.setTextColor(context.getResources().getColor(com.micsig.tbook.ui.R.color.textColorNewTopViewDisable));
        checkFileNameAdd.setChecked(true);

        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);
        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
        taskSuffixNumModel = new ViewModelProvider(this).get(TaskSuffixNumModel.class);
        taskSuffixNumModel.getTextLiveData().observe(getViewLifecycleOwner(),text-> {
            txtSuffixNum.setText(text);
        });
    }


    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
//        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
//        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_QUICKSAVE).subscribe(consumerMainBottomQuickSave);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE).subscribe(consumerAutoSaveTask);
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_STOP_BUTTON_STATE).subscribe(consumerAutoSaveTaskStopButton);
        RxBus.getInstance().getObservable(RxEnum.MSG_AUTO_SAVE_INVOKE_SCREENSHOT).subscribe(consumerAutoSaveScreenShot);

    }


    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSaveWave, true);
        }
    };

    private void setCache() {
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT);

        channelShowChange();
        boolean wavSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_WAV);
        boolean csvSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_CSV);
        boolean binSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_BIN);
        boolean pictureSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.SAVE_TYPE_PICTURE);
        boolean sessionSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.SAVE_TYPE_SESSION);

        checkWAV.setChecked(wavSelect);
        checkCSV.setChecked(csvSelect);
        checkBIN.setChecked(binSelect);
        checkPicture.setChecked(pictureSelect);
        checkSession.setChecked(sessionSelect);

        viewChannel.setChangeListener(null, null);
        viewChannel.setChecked(channelSelect);
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener);
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
        viewChannel.getViewChannelMultipleChoice().unCheckAll();

        setChannelShow();
        restorePath();
        String fileName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_NAME);
        if (fileName.isEmpty()) {
            fileName = Tools.generateName();
        }
        saveNameEdit.setText(fileName);
//        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK_NUM);
        txtSuffixNum.setEnabled(true);
        checkFileNameAdd.setChecked(true);
        int stopCondition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_CONDITION);
        rgStopCondition.setSelectedIndex(stopCondition);
        onCheckChangedListener.onClick(rgStopCondition,rgStopCondition.getSelected());
        int interval = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SAVE_Interval);
        intervalGroup.setSelectedIndex(interval);
        int saveMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_MODE);
        saveModeGroup.setSelectedIndex(saveMode);
        String stopTime = CacheUtil.get().getString(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME);
        if(!stopTime.equals("")){
            topViewStopTimeSelector.setTimeSelectorByString(stopTime);
        }else {
            topViewStopTimeSelector.setNowTime();
        }
        String stopFrame = CacheUtil.get().getString(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_FRAME);
        if(!stopFrame.equals("")){
            frameStopText.setText(stopFrame);
        }else {
            frameStopText.setText("0000001");
        }
    }

    private void setSource(int sourceIndex) {
        int ch = sourceIndex;
        if (sourceIndex == 24) {
            ch = ChannelFactory.getChActivate();
            if (!ChannelFactory.isChOpen(ch)) {
                ch = -1;
            }
        }
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE, String.valueOf(sourceIndex));
        CursorManage.getInstance().setCursorChannelColor(TChan.toUiChNo(ch));
        MeasureManage.getInstance().setCursorChannelColor(TChan.toUiChNo(ch));
    }

    TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() {
        @Override
        public void checkChanged(int viewId, int checkedIndex, RadioButton radioButton) {
            PlaySound.getInstance().playButton();
//            Command.get().getStorage().Save_Source(checkedIndex, false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT, String.valueOf(checkedIndex));
        }
    };

    TopViewChannelMultipleChoice.onTestListener onChannelItemTestListener = new TopViewChannelMultipleChoice.onTestListener() {
        @Override
        public void onTest(CheckBox checkBox) {
            List<Integer> lisSelects = viewChannel.getViewChannelMultipleChoice().getSelectChannel();
            boolean showBin = true;
            for (Integer chanIdx : lisSelects) {
                if (ChannelFactory.isMathCh(chanIdx) || ChannelFactory.isRefCh(chanIdx)) {
                    showBin = false;
                    break;
                }
            }
        }
    };


    private void setChannelShow() {
        viewChannel.setItemVisible(channelShow, true);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL);
    }


    public IMeasureDetail getCursorDetail() {
        return null;
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

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


    private Consumer<Boolean> consumerAutoSaveTask = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean setState) throws Throwable {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(setState){
                        onStopRefresh();
                    }
                }
            });

        }
    };
    private void onStopRefresh(){
        viewChannel.setReadOnly(true);
        topViewStartTimeSelector.setReadOnly(true);
        rgStopCondition.setReadOnly(true);
        topViewStopTimeSelector.setReadOnly(true);
        rgStopCondition.setReadOnly(true);
        intervalGroup.setReadOnly(true);
        saveModeGroup.setReadOnly(true);
//        saveTypeGroup.setEnabled(true);
        for(int i=0;i<multiSaveType.getChildCount();i++){
            View  child = multiSaveType.getChildAt(i);
            if(child instanceof  CheckBox){
                CheckBox cb = (CheckBox) child;
                if(cb.isChecked()){
                    cb.setClickable(true);
                }else{
                    cb.setEnabled(true);
                }
            }
        }
        btnBrowse.setEnabled(true);
        txtSuffixNum.setEnabled(true);
        spinner.setReadOnly(true);
        saveNameEdit.setEnabled(true);
        saveNameEdit.setClickable(true);
        startBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.GONE);
        topViewStartTimeSelector.setNowTime();
        startBtn.setEnabled(true);
    }

    private Consumer<Boolean> consumerAutoSaveTaskStopButton = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean setState) throws Throwable {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "runAutoTaskSaveButtonState: " +setState);
                    if(setState){
                        stopBtn.setEnabled(true);
                    }else {
                        stopBtn.setEnabled(false);
                    }
                }
            });

        }
    };

    private Consumer<Boolean> consumerAutoSaveScreenShot = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean setState) throws Throwable {
            ((MainActivity) getActivity()).autoSaveScreenShot();

        }
    };

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

    public void channelShowChange() {
        viewChannel.setAllSelectShow(true);//csv时显示多选控件
        setChannelShow();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            if (v.getId() == startBtn.getId()) {
                boolean startResult = validAutoSaveTaskParameter();
                if (startResult) {
                    viewChannel.setReadOnly(false);
                    topViewStartTimeSelector.setReadOnly(false);
                    topViewStopTimeSelector.setReadOnly(false);
                    rgStopCondition.setReadOnly(false);
                    intervalGroup.setReadOnly(false);
                    saveModeGroup.setReadOnly(false);
                    btnBrowse.setEnabled(false);
                    txtSuffixNum.setEnabled(false);
                    spinner.setReadOnly(false);
                    saveNameEdit.setEnabled(false);
                    startBtn.setVisibility(View.GONE);
                    stopBtn.setVisibility(View.VISIBLE);
                    stopBtn.setEnabled(true);
                    for(int i=0;i<multiSaveType.getChildCount();i++){
                        View  child = multiSaveType.getChildAt(i);
                        if(child instanceof  CheckBox){
                            CheckBox cb = (CheckBox) child;
                            if(cb.isChecked()){
                                cb.setClickable(false);
                            }else{
                                cb.setEnabled(false);
                            }
                        }
                    }
                }
            } else if ((v.getId() == stopBtn.getId())) {
                viewChannel.setReadOnly(true);
                topViewStartTimeSelector.setReadOnly(true);
                rgStopCondition.setReadOnly(true);
                topViewStopTimeSelector.setReadOnly(true);
                rgStopCondition.setReadOnly(true);
                intervalGroup.setReadOnly(true);
                saveModeGroup.setReadOnly(true);
                btnBrowse.setEnabled(true);
                txtSuffixNum.setEnabled(true);
                spinner.setReadOnly(true);
                saveNameEdit.setEnabled(true);
                saveNameEdit.setClickable(true);
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                AutoSave.getInstance().stop();
                topViewStartTimeSelector.setNowTime();
                for(int i=0;i<multiSaveType.getChildCount();i++){
                    View  child = multiSaveType.getChildAt(i);
                    if(child instanceof  CheckBox){
                        CheckBox cb = (CheckBox) child;
                        if(cb.isChecked()){
                            cb.setClickable(true);
                        }else{
                            cb.setEnabled(true);
                        }
                    }
                }
            }else if (v.getId() == btnBrowse.getId()) {
                handleBrowseClick();
            }else if (v.getId() == txtSuffixNum.getId()) {
                dialogKeyBoard.setDecimalData(7, IDigits.DIGITS_10, onNumSubFixListener);
            }else if (v.getId() == frameStopText.getId()) {
                dialogKeyBoard.setDecimalData(7, IDigits.DIGITS_10, onNumFrameListener);
            }else if(v.getId() == checkWAV.getId()){
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_WAV, String.valueOf(checkWAV.isChecked()));
            }else if(v.getId() == checkCSV.getId()){
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_CSV, String.valueOf(checkCSV.isChecked()));
            }else if(v.getId() == checkBIN.getId()){
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_BIN, String.valueOf(checkBIN.isChecked()));
            }else if(v.getId() == checkPicture.getId()){
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.SAVE_TYPE_PICTURE, String.valueOf(checkPicture.isChecked()));
            }else if(v.getId() == checkSession.getId()){
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.SAVE_TYPE_SESSION, String.valueOf(checkSession.isChecked()));
            }
        }
    };

    private ArrayList<FileBean> getPreviousDirectory() {
        return pathSet.getReverseList();
    }

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
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_ABSOLUTE_PATH,
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH_CURRENT, spinner.getSelectItem());
    }

    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {
        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            onCheckChanged(view, item, false, true, true);
        }
    };

    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus, boolean isUser, boolean setCache) {
        if (view.getId() == R.id.stopRadioGroup) {
            if (item.getIndex() == 1) {
                topViewStopTimeSelector.setVisibility(View.VISIBLE);
                frameStopText.setVisibility(View.GONE);
            } else if (item.getIndex() == 2) {
                frameStopText.setVisibility(View.VISIBLE);
                topViewStopTimeSelector.setVisibility(View.GONE);
            } else if (item.getIndex() == 0) {
                frameStopText.setVisibility(View.GONE);
                topViewStopTimeSelector.setVisibility(View.GONE);
            }
            RxBus.getInstance().post(RxEnum.MQ_MSG_SYNC_EXTERNAL_AUTOSAVE_STATE,item.getIndex());
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_CONDITION,String.valueOf(item.getIndex()));
        }else if(view.getId() == R.id.interval){
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_Interval,String.valueOf(item.getIndex()));
        }else if(view.getId() == R.id.saveMode){
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_MODE,String.valueOf(item.getIndex()));
        }
    }

    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();
            if (v.getId() == saveNameEdit.getId()) {
                layoutTextKeyBoard.setData(text, TopDialogTextKeyBoard.HANDLE_TYPE_SAVE_SESSION, TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() {
                    @Override
                    public void onDismiss(String result) {
                        saveNameEdit.setText(result);
                        txtSuffixNum.setText("0000000");
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK_NUM + CacheUtil.SAVE_TYPE_AUTOSAVE, "0000000");
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_NAME, result);
                    }
                });
            }
        }
    };
    private AutoSave.IAutoSaveListener autoSaveListener = new AutoSave.IAutoSaveListener() {

        private ScreenControls screenControls = null;
        private int lockFlag = 0;
        @Override
        public void onBegin() {
            if(screenControls == null){
                screenControls = ScreenControls.getInstance();
            }
            RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,false);
            getActivity().runOnUiThread(()->{
                ((MainActivity)getActivity()).sendAutoSave(true);
            });
        }

        @Override
        public void onEnd() {
            int errCode = AutoSave.getInstance().getErrCode();
            getActivity().runOnUiThread(()->{
                if(errCode != 0){
                    Log.i(TAG,"errCode:" + errCode);
                }
                if(screenControls.isLockScreen(lockFlag)) {
                    screenControls.unLockScreen(lockFlag);
                }
                lockFlag = 0;
                ((MainActivity)getActivity()).sendAutoSave(false);
            });
            RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true);

        }

        @Override
        public void onSaveBefore(boolean bProgress) {
            lockFlag = ScreenControls.LOCK_SCREEN;
            if(bProgress){
                lockFlag |= ScreenControls.LOCK_PROGRESS;
            }
            if(!screenControls.isLockScreen(lockFlag)) {
                screenControls.lockScreen(lockFlag);
            }
        }

        @Override
        public void onProgress(int val) {
            getActivity().runOnUiThread(()->{
                screenControls.setProgressValue(val);
            });
        }

        @Override
        public void onSaveAfter(boolean bProgress) {
            int flag = lockFlag;
            if(bProgress){
                flag = ScreenControls.LOCK_PROGRESS;
                lockFlag &= ~flag;
            }else{
                getActivity().runOnUiThread(()->{
                    AutoSave autoSave = AutoSave.getInstance();
                    onTextListener(String.format("%07d",autoSave.getSuffixCode()));
                });
            }
            if(screenControls.isLockScreen(flag)) {
                screenControls.unLockScreen(flag);
            }
        }

        @Override
        public void onPicture(String filePath, String fileName) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_AUTO_SAVE,filePath);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_AUTO_SAVE_NAME,fileName);
            RxBus.getInstance().post(RxEnum.MSG_AUTO_SAVE_INVOKE_SCREENSHOT,true);
        }

        @Override
        public HashMap<String, HashMap<String, String>> onCurCache() {
            HashMap<String, HashMap<String, String>> map = new HashMap<>();
            map.put(CacheUtil.DefaultSaveName, CacheUtil.get().getCurrMap());
            map.put(CacheUtil.OtherDefaultSaveName, CacheUtil.get().getCurrOtherMap());
            return map;
        }
    };

    public boolean validAutoSaveTaskParameter() {
        if (!FileUtils.checkFolderExists(spinner.getSelectItem(), context.getResources().getString(R.string.internal_storage))) {
            DToast.get().show(R.string.top_slip_save_wave_path_unable);
            return false;
        }
        LocalDateTime startTime = topViewStartTimeSelector.getTime();
        LocalDateTime stopTime = topViewStopTimeSelector.getTime();
        int stopConditionType = rgStopCondition.getSelected().getIndex();
        List<Integer> selectList = viewChannel.getAllSelectChannel();//保存CSV时选中的channel
        if(checkWAV.isChecked()){
            saveType.add(AutoSaveTaskCondition.SaveType.WAV.getCode());
        }
        if(checkCSV.isChecked()){
            saveType.add(AutoSaveTaskCondition.SaveType.CSV.getCode());
        }
        if(checkBIN.isChecked()){
            saveType.add(AutoSaveTaskCondition.SaveType.BIN.getCode());
        }
        if(checkPicture.isChecked()){
            saveType.add(AutoSaveTaskCondition.SaveType.PICTURE.getCode());
        }
        if(checkSession.isChecked()){
            saveType.add(AutoSaveTaskCondition.SaveType.SESSION.getCode());
        }
        List<AutoSaveTaskCondition.SaveType> typeList = saveType.stream()
                .map(AutoSaveTaskCondition.SaveType::fromCode)
                .collect(Collectors.toList());
        saveType.clear();
        if (typeList == null || typeList.size() <= 0) {
            DToast.get().show(R.string.msgTopSaveTypeNotSelect);
            return false;
        }
        if(typeList.contains(AutoSaveTaskCondition.SaveType.WAV)|| typeList.contains(AutoSaveTaskCondition.SaveType.BIN)|| typeList.contains(AutoSaveTaskCondition.SaveType.CSV)){
            if (selectList == null || selectList.size() <= 0) {
                DToast.get().show(R.string.msgTopSaveCsvNotSelect);
                return false;
            }
        }
        if(spinner.getSelectItem().isEmpty()){
            DToast.get().show(R.string.top_slip_directory_save_to);
            return false;
        }
        if(!ifEnoughSpace()){
            return false;
        }
        if ((startTime.isAfter(stopTime) || startTime.isEqual(stopTime))&& rgStopCondition.getSelected().getIndex()==1) {
            DToast.get().show(R.string.top_slip_stop_must_later_start);
            return false;
        } else if((startTime.isBefore(stopTime)&& stopConditionType==1) || stopConditionType==2 || stopConditionType == 0){
            String stopValue = "";
            if(stopConditionType == 1){
                stopValue = topViewStopTimeSelector.getTime().toString();
            }else if(stopConditionType==2){
                stopValue  = frameStopText.getText().toString();
            }
            AutoSave autoSave = AutoSave.getInstance();
            List<IChannel> channels = new ArrayList<>();
            for(Integer integer:selectList){
                channels.add(ChannelFactory.getValidChannel(integer));
            }
            autoSave.setChannels(channels);
            autoSave.setStartDateTime(topViewStartTimeSelector.getTime());
            autoSave.setStopCondition(stopConditionType);
            if(stopConditionType == AutoSave.STOP_CONDITION_TIME){
                autoSave.setStopConditionTime(LocalDateTime.parse(stopValue));
            }else if(stopConditionType == AutoSave.STOP_CONDITION_FRAMES){
                autoSave.setStopConditionFrames(Integer.parseInt(stopValue));
            }
            autoSave.setFrameInterval(AutoSaveTaskCondition.TimeInterval.fromCode(intervalGroup.getSelected().getIndex()).getTime());
            autoSave.setSaveMode(saveModeGroup.getSelected().getIndex());
            int type = 0;
            for(AutoSaveTaskCondition.SaveType t:typeList){
                type |= 1 << t.getCode();
            }
            autoSave.setSaveType(type);
            autoSave.setFilePath(spinner.getSelectItem().trim());
            autoSave.setPrefixName(saveNameEdit.getText().trim());
            autoSave.setSuffixCode(Integer.parseInt(txtSuffixNum.getText().toString().trim()));
            autoSave.setAutoSaveListener(autoSaveListener);
            if(!autoSave.isRun()) {
                ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
                autoSave.start();
            }
        }
        return true;
    }

    private TopDialogNumberKeyBoard.OnDismissListener  onNumSubFixListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result);
        }
    };

    private void onTextListener(String text) {
        int inputSuffix = Integer.parseInt(text.trim());
        if(inputSuffix>AutoSave.MAX_SUFFIXCODE){
            inputSuffix = AutoSave.MAX_SUFFIXCODE;
        }
        text = String.valueOf(inputSuffix);
        text = KeyBoardNumberUtil.toBits(text,7);
        txtSuffixNum.setText(text);
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK_NUM + CacheUtil.SAVE_TYPE_AUTOSAVE, text);
    }

    private TopDialogNumberKeyBoard.OnDismissListener  onNumFrameListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onFrameTextListener(result);
        }
    };

    private void onFrameTextListener(String text) {
        if(text.equals("0000000")){
            DToast.get().show(R.string.inputCorrectNframe);
            return;
        }
        int inputStopCount = Integer.parseInt(text.trim());
        if(inputStopCount>AutoSave.MAX_SUFFIXCODE){
            inputStopCount = AutoSave.MAX_SUFFIXCODE;
        }
        text = String.valueOf(inputStopCount);
        text = KeyBoardNumberUtil.toBits(text,7);
        Log.d(TAG, "onFrameTextListener: "+text);
        frameStopText.setText(text);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_FRAME, text);
    }

    private String getFinaleName() {
        String finalName = saveNameEdit.getText();
        if(checkFileNameAdd.isChecked()) {
            finalName = finalName + "_" + txtSuffixNum.getText();
        }
        return finalName;
    }

    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == checkFileNameAdd.getId()) {
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK + CacheUtil.SAVE_TYPE_AUTOSAVE, String.valueOf(isChecked));
                txtSuffixNum.setEnabled(isChecked);
                RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE, CacheUtil.SAVE_TYPE_AUTOSAVE + CacheUtil.WAVE_STORE_PATH_SLIP + isChecked);
            }
        }
    };

    private void restorePath() {
        pathSet.clear();
        String pathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH);
        String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_ABSOLUTE_PATH);
        String currentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH_CURRENT);
        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);

        ArrayList<FileBean> dataList = new ArrayList<>();
        FileBean currentBean = new FileBean();
        for (int i = 0; i < abPathCacheList.size(); i++) {
            if (!Tools.fileIsExists(abPathCacheList.get(i))) continue;
            FileBean fileBean = new FileBean();
            fileBean.setPath(abPathCacheList.get(i));
            fileBean.setDisplayName(pathCacheList.get(i));
            Logger.i(TAG, "abPath= " + abPathCacheList.get(i) + " ,disPath= " + pathCacheList.get(i) + " ,currnt= " + currentPath);
            if(abPathCacheList.get(i).equals(currentPath)) {
                currentBean.setPath(abPathCacheList.get(i));
                currentBean.setDisplayName(pathCacheList.get(i));
            }
            dataList.add(fileBean);
        }
        pathSet.addAll(dataList);
        spinner.updateDataList(getPreviousDirectory(), null);
    }

    public boolean ifEnoughSpace(){
        File saveDir = new File(spinner.getSelectItem());
        long freeSpace = saveDir.getFreeSpace();
        long estimatedSize = 10 * 1024 * 1024L *1024;
        if(freeSpace < estimatedSize){
            DToast.get().show(R.string.no_storage_space);
            return false;
        }else {
            return true;
        }
    }

    TopViewSpinner.onItemSelectListener onItemSelectListener = fileBean -> {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH_CURRENT, fileBean.getPath());
//        DToast.get().show(str);
        addSelectToPathSet(fileBean);
    };

    private void addSelectToPathSet(FileBean pathStr) {
        handleAddPath(pathStr);
        pathSet.add(pathStr);
        spinner.updateDataList(getPreviousDirectory(), null);
        savePathToCache();
    }
}
