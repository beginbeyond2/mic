package com.micsig.tbook.tbookscope.rightslipmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.LoadCsv;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.horizontal.HorizontalAxisRef;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.middleware.command.Command_Channel;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogChannelLabel;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogLoadRefCsvWave;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogSelectColor;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.ForEightRefRecallAdapter;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogRefRecallBean;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ExternalKeysNode;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.save.TopMsgSaveRef;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.util.FileSelector;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.FixedSizeHashSet;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.TopViewSpinner;
import com.micsig.tbook.ui.util.FileBeanToStr;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.svg.SelectorUtil;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;
import com.molihuan.pathselector.entity.FileBean;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * @Description: 适配8通道Ref设置页面
 * @Author: lmh
 * @CreateDate: 2024/3/28 9:45
 */

public class RightLayoutRef extends LinearLayout {

    private static final String TAG = "RightLayoutRef";
    private Context context;
    private RightMsgRefForEight msgRef;
    private ViewGroup rootView;

    private Button btnBrowse;
    private TopViewSpinner spinner;

    private RecyclerView recyclerView;
    private ForEightRefRecallAdapter adapter;
    private ArrayList<DialogRefRecallBean> list;
    private LinearLayout llDisplaySwitch;
    private MSwitchBox switchBox;
    private int refChannelNumber;//TChan.R1--TChan.R8
    private ConstraintLayout topChannelTitle, clTopChannelGroup;
    private TextView channelTitle;
    private TextView btnDeleteChannel, btnAddChannel;
    private RadioButton channelMath, channelRef, channelSerials;
    private RightViewSelect refLoadType;
    private boolean needUpdateMasterLocation = false;//是否需要更新refMaster位置

    private ConstraintLayout clImgBtn;
    private Button btnTop, btnBottom;
    private ImageView ivBackground, imgBottom, imgUp;
    private MyHandler myHandler;
    private boolean isLoadSuccess = false;
    private DialogLoadRefCsvWave dialogLoadRefCsvWave;
    private TopViewEdit chLabel, selectColor, phaseDelay, forDoubleClick;
    private DialogChannelLabel dialogChannelLabel;
    private DialogSelectColor dialogSelectColor;
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;

    private final FixedSizeHashSet<FileBean> pathSet = new FixedSizeHashSet<>(10);

    public RightLayoutRef(Context context) {
        this(context, null);
    }

    public RightLayoutRef(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutRef(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
        initControl();
        myHandler = new MyHandler(RightLayoutRef.this);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.TOPSLIP_SAVE_REF).subscribe(consumerSaveRef);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_NORMAL_STATE).subscribe(consumerShowNormalState);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_REF).subscribe(consumerOtherChannelCanAdd);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_VERTICAL_SCALE).subscribe(consumerChannelVscale);
        RxBus.getInstance().getObservable(RxEnum.DIALOG_REFRECALL_CHANGED).subscribe(consumerRefRecallChanged);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST).subscribe(consumerUpdateRefDataList);
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE, this::OnChanActiveChange);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL).subscribe(consumerDeleteChannel);
        RxBus.getInstance().getObservable(RxEnum.UDISK_RESPONSE).subscribe(consumerUDiskResponse);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_TIMEBASE).subscribe(consumerUpdateTimeBase);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_USER_SET_TIMEBASE).subscribe(consumerRefTimeBase);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION).subscribe(consumerMouseClick);

        EventFactory.addEventObserver(EventFactory.EVENT_LOADCSV_RUN, eventLoadCsvObserver);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);

//        recyclerView.addOnScrollListener(scrollListener);
    }

    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum = RxBusRegister.parseMqEnum(obj);
        if (mqEnum == MQEnum.CH_OPEN || mqEnum == MQEnum.CH_CLOSE) {

        }
    }

    @SuppressLint("SetTextI18n")
    private void initView(AttributeSet attrs, int defStyleAttr) {
        rootView = (ViewGroup) View.inflate(context, R.layout.layout_right_ref_for_eight, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightLayoutRef);
        int refNumber = ta.getInt(R.styleable.RightLayoutRef_refChannelNumber, 1);
        ta.recycle();
        refChannelNumber = TChan.toRefTChan(refNumber);
        topChannelTitle = findViewById(R.id.top_channel_title);
        btnDeleteChannel = findViewById(R.id.btn_delete_channel);
        channelTitle = findViewById(R.id.channel_title);
        llDisplaySwitch = findViewById(R.id.ll_display_switch);
        switchBox = findViewById(R.id.channel_switch);
        clImgBtn = findViewById(R.id.cl_img_btn);
        btnTop = findViewById(R.id.btnTop);
        btnBottom = findViewById(R.id.btnBottom);
        ivBackground = findViewById(R.id.img_back_src);
        refLoadType = (RightViewSelect) findViewById(R.id.ref_load_type);
        imgBottom = findViewById(R.id.img_bottom);
        imgUp = findViewById(R.id.img_up);
        chLabel = findViewById(R.id.chLabel);
        selectColor = findViewById(R.id.select_color);
        phaseDelay = findViewById(R.id.phase_delay);
        forDoubleClick = findViewById(R.id.for_doubleClick);
        imgBottom.setOnTouchListener(onTouchListener);
        imgUp.setOnTouchListener(onTouchListener);
        btnBrowse = findViewById(R.id.btn_browse);
        btnBrowse.setOnClickListener(onClickListener);
        spinner = findViewById(R.id.spinner);
        spinner.setData(context.getResources().getString(R.string.top_save_wave_directory), getPreviousDirectory(),R.layout.layout_item_for_ref_directory, onItemSelectListener);

        clTopChannelGroup = findViewById(R.id.cl_top_channel_group);
        channelMath = findViewById(R.id.channelMath);
        channelRef = findViewById(R.id.channelRef);
        channelSerials = findViewById(R.id.channelSerials);
        btnAddChannel = findViewById(R.id.btn_add_channel);
        channelMath.setOnClickListener(onClickListener);
        channelRef.setOnClickListener(onClickListener);
        channelSerials.setOnClickListener(onClickListener);
        btnAddChannel.setOnClickListener(onClickListener);
        btnTop.setOnClickListener(onClickListener);
        btnBottom.setOnClickListener(onClickListener);
        refLoadType.setOnItemClickListener(onRightViewSelectItemClickListener);

        recyclerView = findViewById(R.id.rcv_recall_data);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        list = getList(spinner.getSelectItem());
        adapter = new ForEightRefRecallAdapter(context, list, refChannelNumber);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        channelTitle.setText("R" + TChan.toRefNumber(refChannelNumber));
        btnDeleteChannel.setOnClickListener(onClickListener);
        switchBox.setOnToggleStateChangedListener(onToggleStateChangedListener);
        chLabel.setOnClickEditListener(onClickEditListener);
        phaseDelay.setOnClickEditListener(onClickEditListener);
        forDoubleClick.setOnClickEditListener(onClickEditListener);
        selectColor.setOnClickEditListener(onClickEditListener);

        dialogLoadRefCsvWave = (DialogLoadRefCsvWave) ((MainActivity) context).findViewById(R.id.dialogLoadRefCsv);

        initData();
        setControlColorByChIdx(refChannelNumber);
        showUpBottomImg();
    }

    /**
     * 根据RecyclerView处理topImg bottomImg显示隐藏逻辑
     */
    private void showUpBottomImg() {
        int firstItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        int lastItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        if (list.size() < 9) {
            imgUp.setVisibility(View.INVISIBLE);
            imgBottom.setVisibility(View.INVISIBLE);
        } else {
            if (firstItemPosition > 0) {
                imgUp.setVisibility(View.VISIBLE);
            } else {
                imgUp.setVisibility(View.INVISIBLE);
            }

            if (lastItemPosition < (adapter.getItemCount() - 1)) {
                imgBottom.setVisibility(View.VISIBLE);
            } else {
                imgBottom.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void initData() {
        msgRef = new RightMsgRefForEight();
        msgRef.setRefChannelNumber(refChannelNumber);
        msgRef.setRefChecked(false);
        msgRef.setLabel("");
    }

//    @Override
//    protected void onVisibilityChanged(@android.support.annotation.NonNull View changedView, int visibility) {
//        super.onVisibilityChanged(changedView, visibility);
//        if(visibility == VISIBLE) {
//            fillSelectData();
//        } else {
//            clearSelect();
//        }
//    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_REF, msgRef);
    }


    private Consumer consumerLoadCache = new Consumer() {
        @Override
        public void accept(@NonNull Object o) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutRef, true);
        }
    };

    private void setCache() {
        //获取参考通道cache
        boolean isAddByUser = isAddByUser(refChannelNumber);
        boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber);
        String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + refChannelNumber);
        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber);
        Command.get().getChannel().Display(TChan.toFpgaChNo(refChannelNumber), refCheck && isAddByUser, false);
        switchBox.setState(refCheck);
//        String delay = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_DELAY + refChannelNumber);
        resetDelayUI();
        restorePath();
        //TODO


//        if (SaveRecoverySession.MSS_REF_TAG.equals(refFilePath)) { //这里用一个乱码匹配，
//            rEditFilePath.setText("");
//        } else {
//            rEditFilePath.setText(refFilePath);
//        }
        chLabel.setText(label);
        String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber);
        if (isAddByUser) {
            if (!StrUtil.isEmpty(selectFile)) {
                if (selectFile.endsWith(".csv")) { //从csv中读


                    ScreenControls.getInstance().lockScreen(ScreenControls.LOCK_LOADCSV << (refChannelNumber - TChan.R1));
                    new Thread(loadCsRunnable).start();
                    //loadCsRunnable.run();
                } else if (selectFile.endsWith(".wav") || selectFile.endsWith(".mwav")) {//从wav中读
                    isLoadSuccess = SaveManage.getInstance().readRef(TChan.toFpgaChNo(refChannelNumber), selectFile);
                }
            }
        }
        Command.get().getReference().Enable(TChan.toFpgaChNo(refChannelNumber), refCheck, false);
        //获取要显示的Ref通道
        msgRef.setRefChecked(refCheck);
        msgRef.setLabel(label);
        updateSelect();
        refLoadType.setSelectIndex(refType);

        WaveManage.get().setChannelLabel(refChannelNumber, label);
        setChannelLabel(TChan.toFpgaChNo(refChannelNumber), label);

        String colorStr = CacheUtil.get().getString(CacheUtil.MAIN_CHANNEL_COLOR + refChannelNumber);
        selectColor.setEditColor(colorStr);

        sendMsg();
    }


    //TODO 当存储参考时，如果参考没有打开，则先关闭所有参考
    private void closeAllRef_thenSaveRef() {
        if (!CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber)) {
            if (switchBox.isState()) {
                switchBox.setState(false);
                onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState());
            }
//            if (r2Switch.isState()) {
//                r2Switch.setState(false);
//                onToggleStateChangedListener.onToggleStateChanged(r2Switch, false);
//            }
//            if (r3Switch.isState()) {
//                r3Switch.setState(false);
//                onToggleStateChangedListener.onToggleStateChanged(r3Switch, false);
//            }
//            if (r4Switch.isState()) {
//                r4Switch.setState(false);
//                onToggleStateChangedListener.onToggleStateChanged(r4Switch, false);
//            }
        }
    }

    private Consumer<TopMsgSaveRef> consumerSaveRef = new Consumer<TopMsgSaveRef>() {
        @SuppressLint("SetTextI18n")
        @Override
        public void accept(TopMsgSaveRef msgSaveRef) throws Exception {
            int ch = msgSaveRef.getFromIdChannelFactory();
            if (msgSaveRef.getSaveToRefId() == TChan.toRefNumber(refChannelNumber)) {
                String refPath = SaveManage.getInstance().saveRef(ch, Tools.SaveType_LOCAL, TChan.toFpgaChNo(refChannelNumber), null);
                int refIndex = TChan.toRefNumber(refChannelNumber);
                if (StrUtil.isEmpty(refPath)) {
                    DToast.get().show(String.format(App.get().getString(R.string.msgTopSaveBinFailed), "REF" + TChan.toRefNumber(refChannelNumber)));
                    if (switchBox.isState()) {
                        switchBox.setState(false);
                        msgRef.setRefChecked(false);
                        onToggleStateChangedListener.onToggleStateChanged(switchBox, false);
                    }
                } else {
                    ChannelFactory.chActivate(ch);
                    refLoadType.setSelectIndex(0);//WAV格式
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber, String.valueOf(refLoadType.getSelectIndex()));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber, refPath);

                    String defaultStr = Tools.resultSavePath(Tools.SaveType_LOCAL, Tools.SaveDir_REFDEFAULT, (MainActivity) context);
                    FileBean fileBean = new FileBean();
                    fileBean.setPath(defaultStr);
                    fileBean.setDisplayName(defaultStr); //TODO

                    addPathToPathSet(fileBean);
                    spinner.updateDataList(getPreviousDirectory(), null);
                    updateList();
                    DToast.get().show(String.format(App.get().getString(R.string.msgTopSaveBinSuccess), "REF" + TChan.toRefNumber(refChannelNumber)));
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + refChannelNumber, "");
                    closeAllRef_thenSaveRef();
                    switchBox.setState(true);
                    msgRef.setRefChecked(true);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChannelNumber, String.valueOf(true));
                    needUpdateMasterLocation = true;
                    onToggleStateChangedListener.onToggleStateChanged(switchBox, true);
                    RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber));
                    if (refChannel != null) {
                        setLabel(refChannel.getLabel().trim());
                        ChannelFactory.chActivate(TChan.toFpgaChNo(refChannelNumber));
                    }
                }
            }
        }
    };

    private void setLabel(String label) {
        chLabel.setText(label);
        msgRef.setLabel(label);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + refChannelNumber, label);
        WaveManage.get().setChannelLabel(refChannelNumber, label);
        setChannelLabel(TChan.toFpgaChNo(refChannelNumber), label);
    }

    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception {
            boolean value = msgOthers.getRef(refChannelNumber).isValue();
            boolean isAddByUser = isAddByUser(refChannelNumber);
            Command.get().getChannel().Display(TChan.toFpgaChNo(refChannelNumber), value && isAddByUser, false);
            if (value == switchBox.isState()) {
                return;
            }
            if (!CacheUtil.get().isLoadParamComplete()) {
                return;
            }
            int refIndex = TChan.toRefNumber(refChannelNumber);
            if (isAddByUser) {
                switchBox.setState(value);
                Command.get().getReference().Enable(TChan.toFpgaChNo(refChannelNumber), value, false);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(value));
                ChannelFactory.chEnable(TChan.toFpgaChNo(refChannelNumber), value);
                msgRef.setRefChecked(value);
            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_CHANNEL_DISPLAY: {
                    List<Command_Channel.ChannelAttribute> list = (List<Command_Channel.ChannelAttribute>) commandMsgToUI.getObject();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    boolean isOpen = Boolean.parseBoolean(params[1]);
                    if (chIndex != TChan.toFpgaChNo(refChannelNumber)) return;
                    if (TChan.isRef(TChan.toUiChNo(chIndex)) /*&& isOpen==false*/) {
                        switchBox.setState(isOpen);
                        onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState());
                    }
                }
                break;
                case CommandMsgToUI.FLAG_REF_ENABLE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int refSource = Integer.parseInt(params[0]);
                    boolean display = Boolean.parseBoolean(params[1]);
                    if (refSource == TChan.toFpgaChNo(refChannelNumber)) { //是当前ref通道
                        switchBox.setState(display);
                        onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState());
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_STOTAGE_LOAD: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int refSource = Integer.parseInt(params[0]);
                    String fileName = params[1];
                    boolean display = Boolean.parseBoolean(params[2]);
                    if (refChannelNumber == refSource) {
                        loadRef(refSource, fileName, display);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_REF_LABEL: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String result = params[1];
                    if (chIndex != TChan.toFpgaChNo(refChannelNumber)) return;
                    setLabel(result);
                }
                break;
                case CommandMsgToUI.FLAG_REF_LABEL_CLEAR: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String result = "";
//                    Logger.i(Command.TAG, "label clear");
                    if (chIndex != TChan.toFpgaChNo(refChannelNumber)) return;
                    setLabel(result);
                }
                break;
            }
        }
    };

    private Consumer<String> consumerShowNormalState = new Consumer<String>() {
        @Override
        public void accept(String String) throws Exception {
            String[] params = String.split(CommandMsgToUI.PARAM_SPLIT);
            int channelNumber = Integer.parseInt(params[0]);
            boolean isNormal = Boolean.parseBoolean(params[1]);
            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber));

            if (channelNumber != refChannelNumber) return;
            if (isNormal) {//R1...R8布局样式
                llDisplaySwitch.setVisibility(View.VISIBLE);//switch 显示
                topChannelTitle.setVisibility(View.VISIBLE);//R1 delete按钮 显示
                clTopChannelGroup.setVisibility(View.GONE);//Math/Ref/Serial 不显示
                clImgBtn.setVisibility(View.VISIBLE);
                if (refChannel != null && refChannel.getRefType() == WaveData.FFT_WAVE) {
                    phaseDelay.setVisibility(View.GONE);
                } else {
                    phaseDelay.setVisibility(View.VISIBLE);
                }
            } else {//Math/Ref/Serials共同显示布局样式
                llDisplaySwitch.setVisibility(View.INVISIBLE);//switch 不显示
                topChannelTitle.setVisibility(View.INVISIBLE);//R1 delete按钮 不显示
                clTopChannelGroup.setVisibility(View.VISIBLE);//Math/Ref/Serial 显示
                clImgBtn.setVisibility(View.INVISIBLE);
                phaseDelay.setVisibility(View.GONE);
            }

            setRefLoadTypeEnable();
            int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber);
            String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + refChannelNumber);
            refLoadType.setSelectIndex(refType);
            chLabel.setText(label);
            updateList();
            setControlColorByChIdx(refChannelNumber);
        }
    };

    private Consumer<String> consumerOtherChannelCanAdd = new Consumer<String>() {
        @Override
        public void accept(String available) throws Throwable {
            if (channelMath == null || channelRef == null || channelSerials == null) return;
            String[] params = available.split(CommandMsgToUI.PARAM_SPLIT);
            int refSlideIndex = Integer.parseInt(params[0]);
            boolean mathAvailable = Boolean.parseBoolean(params[1]);
            boolean refAvailable = Boolean.parseBoolean(params[2]);
            boolean serialAvailable = Boolean.parseBoolean(params[3]);
            int refChanNumber = TChan.R1;
            switch (refSlideIndex) {
                case MainViewGroup.RIGHTSLIP_REF1:
                    refChanNumber = TChan.R1;
                    break;
                case MainViewGroup.RIGHTSLIP_REF2:
                    refChanNumber = TChan.R2;
                    break;
                case MainViewGroup.RIGHTSLIP_REF3:
                    refChanNumber = TChan.R3;
                    break;
                case MainViewGroup.RIGHTSLIP_REF4:
                    refChanNumber = TChan.R4;
                    break;
                case MainViewGroup.RIGHTSLIP_REF5:
                    refChanNumber = TChan.R5;
                    break;
                case MainViewGroup.RIGHTSLIP_REF6:
                    refChanNumber = TChan.R6;
                    break;
                case MainViewGroup.RIGHTSLIP_REF7:
                    refChanNumber = TChan.R7;
                    break;
                case MainViewGroup.RIGHTSLIP_REF8:
                    refChanNumber = TChan.R8;
                    break;
            }
            if (refChanNumber == refChannelNumber) {
                channelMath.setEnabled(mathAvailable);
                channelSerials.setEnabled(serialAvailable);
            } else {
                channelMath.setEnabled(true);
                channelSerials.setEnabled(true);
            }
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
            boolean slowTimeBase = Tools.isSlowTimeBase();
            if (zoom && slowTimeBase) {
                channelSerials.setEnabled(false);
            }
        }
    };

    private Consumer<String> consumerChannelVscale = new Consumer<String>() {

        @Override
        public void accept(String adjustStr) throws Throwable {
            String[] param = adjustStr.split(CommandMsgToUI.PARAM_SPLIT);
            boolean isClickTop = Boolean.parseBoolean(param[0]);
            int chan = Integer.parseInt(param[1]);
            if (chan != refChannelNumber) return;
            if (isClickTop) {
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174);
                msgRef.setUpClick(true);
                postChange();
            } else {
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174);
                msgRef.setUpClick(false);
                postChange();
            }
        }
    };

    private Consumer<String> consumerRefRecallChanged = new Consumer<String>() {
        @Override
        public void accept(String recallInfo) throws Exception {
            String[] params = recallInfo.split(CommandMsgToUI.PARAM_SPLIT);
            int integer = Integer.parseInt(params[0]);
            int channelNumber = Integer.parseInt(params[1]);
            if (channelNumber != refChannelNumber) return;
            switch (integer) {
                case ExternalKeysNode.ACTION_REFRECALL_FINISH:
                    DialogRefRecallBean bean = null;
                    for (DialogRefRecallBean item : list) {
                        if (item.isSelect()) {
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber, item.getPathFile());
                            confirm();
                            break;
                        }
                    }
                    break;
                case ExternalKeysNode.ACTION_REFRECALL_UP:
                    moveOnlyScroll(true);
                    break;
                case ExternalKeysNode.ACTION_REFRECALL_DOWN:
                    moveOnlyScroll(false);
                    break;
            }
        }
    };

    private Consumer<Integer> consumerUpdateRefDataList = new Consumer<Integer>() {
        @Override
        public void accept(Integer refChan) throws Throwable {
            if (refChan == refChannelNumber) {
                updateList();
            }
        }
    };


    private void updateList() {
        list.clear();
        list = getList(spinner.getSelectItem());
        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber);
        String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber);
        for (DialogRefRecallBean bean : list) {
            boolean isSelect = bean.getPathFile().equals(selectFile);
            bean.setSelect(isSelect);
        }
        adapter.setList(list);
        adapter.notifyDataSetChanged();

    }


    private Consumer<Integer> consumerDeleteChannel = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Throwable {
            if (integer == refChannelNumber) {
                btnDeleteChannel.performClick();
            }
        }
    };

    private Consumer<Boolean> consumerUDiskResponse = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            //通知刷新参考数据
            RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, refChannelNumber);
        }
    };

    public void moveOnlyScroll(boolean isUp) {
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            DialogRefRecallBean bean = list.get(i);
            if (bean.isSelect()) {
                index = bean.getIndex();
                break;
            }
        }
/*        if (isUp) {
            if (index != 0) {
                index -= 1;
            } else {
                index = list.size() - 1;
            }
        } else {
            if (index != list.size() - 1) {
                index += 1;
            } else {
                index = 0;
            }
        }*/

        if (isUp) {
            index -= 1;
            index = Math.max(index, 0);
        } else {
            index += 1;
            index = Math.min(index, list.size() - 1);
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstCompletely = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastCompletely = layoutManager.findLastCompletelyVisibleItemPosition();
        if (firstCompletely > index) {
            layoutManager.scrollToPositionWithOffset(index, 0);
        } else if (lastCompletely < index) {
            layoutManager.scrollToPositionWithOffset(index - 9, -30);
        }
        for (int i = 0; i < list.size(); i++) {
            DialogRefRecallBean bean = list.get(i);
            bean.setSelect(bean.getIndex() == index);
            if (bean.isSelect()) {
                scrollToPos(bean.getIndex());
            }
        }
        adapter.notifyDataSetChanged();
        confirm();
        showUpBottomImg();
    }

    private void scrollToPos(int pos) {
        if (list.size() > 9 && pos > 4 && pos < list.size() - 5) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            linearLayoutManager.scrollToPositionWithOffset(pos, 225);
        }
    }

    private void loadRef(int refSource, String fileName, boolean display) {
        File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_REFWAVE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Optional<File> any = Arrays.stream(files).filter(s -> s.getName().replace(".mwav", "").replace(".wav", "").equals(fileName.trim())).findAny();
            if (any.isPresent() == false) return;
            File file = any.get();

            long time = file.lastModified();
            String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
            String title = file.getName().replace(".mwav", "").replace(".wav", "");
            loafRefX(refSource, file.getAbsolutePath(), title, ctime, display);
        }
    }


    private void loafRefX(int refSource, String absolutePath, String title, String ctime, boolean display) {
        isLoadSuccess = SaveManage.getInstance().readRef(TChan.toFpgaChNo(refSource), absolutePath);
        if (isLoadSuccess) {
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refSource, String.valueOf(display));
            double scaleVal = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refSource)).getRefTimeScaleVal();
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + refSource, getStringRefScale(TChan.toFpgaChNo(refSource), scaleVal));

            msgRef.setRefChecked(display);
            sendMsg();
            SaveManage.getInstance().readRef(TChan.toFpgaChNo(refSource), absolutePath);
        }
    }

    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            switch (view.getId()) {
                case R.id.channel_switch:
                    if (!switchBox.isState()) {
                        ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
                    }
                    if (state) {
                        isLoadSuccess = SaveManage.getInstance().readRef(TChan.toFpgaChNo(refChannelNumber), adapter.getSelectItem().getPathFile());
                        RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber));
                        if (refChannel != null) {
                            setLabel(refChannel.getLabel());
                        }
                        if (isLoadSuccess) {
                            resetDelayUI();
                        }
                    }
                    msgRef.setRefChecked(state);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(state));
                    ChannelFactory.chEnable(TChan.toFpgaChNo(refChannelNumber), switchBox.isState());
                    Command.get().getChannel().Display(TChan.toFpgaChNo(refChannelNumber), state, false);
                    Command.get().getReference().Enable(TChan.toFpgaChNo(refChannelNumber), state, false);
                    sendMsg();
                    if (!state) {
                        //关闭垂直调节按钮
                        RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true);
                    }
                    break;
            }
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber);
            int refIndex = TChan.toRefNumber(refChannelNumber);
            if (refCheck) {
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + refChannelNumber, String.valueOf(refIndex));
            } else {
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + refChannelNumber, "");
            }
        }
    };

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


    private ForEightRefRecallAdapter.OnItemClickListener onItemClickListener = new ForEightRefRecallAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(DialogRefRecallBean item) {
//            DToast.get().show("选中的Index= " + item.getIndex());
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber, item.getPathFile());

            PlaySound.getInstance().playButton();
            for (DialogRefRecallBean bean : list) {
                if (getSelectedIndex() == 0) {
                    bean.setSelect(bean.getIndex() == item.getIndex());
                    if (bean.isSelect()) {
                        confirm();
                    }
                }
            }
//            adapter.setList(list);
            adapter.notifyDataSetChanged();

            if (getSelectedIndex() == 1) {//CSV
                loadCsvFile(item);
            }
        }
    };

    private void loadCsvFile(DialogRefRecallBean item) {
        LoadCsv loadCsv = new LoadCsv();
        boolean loadSuccess = false;
        try {
            loadSuccess = loadCsv.load(item.getPathFile());
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
        dialogLoadRefCsvWave.setData(channelInCsv, new DialogLoadRefCsvWave.OnDismissListener() {
            @Override
            public void onDismiss(ConcurrentHashMap<Integer, Integer> channelToRef) {
                if (channelToRef.size() <= 0) return;
                loadCsvInBackGround(channelToRef, loadCsv, item);
            }
        });
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
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            post(() -> {
                channelToRef.forEach((key, value) -> {
                    ChannelFactory.chOpen(key);
                    setCacheMapValue(key, item);
                });
            });
        }).start();
    }

    private void setCacheMapValue(int key, DialogRefRecallBean item) {
        if (item == null) return;
        int chanId = TChan.toUiChNo(key);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chanId, String.valueOf(true));
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + chanId, String.valueOf(true));
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_TYPE + chanId, String.valueOf(refLoadType.getSelectIndex()));
        double scaleVal = ChannelFactory.getRefChannel(TChan.toFpgaChNo(chanId)).getRefTimeScaleVal();
        CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + chanId, getStringRefScale(TChan.toFpgaChNo(chanId), scaleVal));
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + chanId, item.getPathFile());
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + chanId, "0 ns");

        ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
        String label = getLabelFromChannel(key);
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + chanId, label);
        resetDelayUI();
        WaveManage.get().setChannelLabel(chanId, label);
        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_STATE, chanId);
    }

    public void confirm() {
//        for (DialogRefRecallBean item : list) {
//            if (item.isSelect()) {
////                rEditFilePath.setText(item.getPathFile());
//                break;
//            }
//        }
        //如果当前是 R1……R8单独的状态，需要更新当前通道的数据
        if (llDisplaySwitch.getVisibility() == View.VISIBLE) {
            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber));
            if (refChannel == null) return;
            msgRef.setRefChecked(true);
            msgRef.setLabel(chLabel.getText());
            //保存新增加的RefChannel 信息
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(true));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + refChannelNumber, chLabel.getText());
//            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + refChannelNumber, "0 ns");
            double scaleVal = refChannel.getRefTimeScaleVal();
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + refChannelNumber, getStringRefScale(TChan.toFpgaChNo(refChannelNumber), scaleVal));
            String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber);
            if (getSelectedIndex() == 0) {
                isLoadSuccess = SaveManage.getInstance().readRef(TChan.toFpgaChNo(refChannelNumber), selectFile);
                setLabel(refChannel.getLabel());
                if (isLoadSuccess) {
                    resetDelayUI();
                }
            }
        }
    }


    public int getSelectedIndex() {
        return refLoadType.getSelectIndex();
    }


    public ArrayList<File> filterFileList(ArrayList<File> oldList, String... suffix) {
        String[] suffixS = suffix;
        ArrayList<File> files = new ArrayList<File>();
        if (oldList == null || oldList.size() <= 0) return files;
        for (int i = 0; i < oldList.size(); i++) {
            if (suffixS != null && suffixS.length > 0) {
                for (String s : suffixS) {
                    if (oldList.get(i).getAbsolutePath().endsWith(s)) {
                        files.add(oldList.get(i));
                        break;
                    }
                }
            }
        }
        return files;
    }


    private ArrayList<DialogRefRecallBean> getList(String path) {
        ArrayList<DialogRefRecallBean> list = new ArrayList<DialogRefRecallBean>();
        if (path == null || path.isEmpty()) return list;
        int refTypeIndex = getSelectedIndex();
        ArrayList<File> files = SaveManage.getInstance().getFilesFromCur(path);
        List<File> filterFiles = new ArrayList<File>();
        if (refTypeIndex == 1) { //CSV
            filterFiles = filterFileList(files ,".csv");
        } else if (refTypeIndex == 0) {//WAV
            filterFiles = filterFileList(files, ".wav", ".mwav");
        } else {
            filterFiles = filterFileList(files);
        }

        if (filterFiles == null || filterFiles.size() == 0) return list;
        String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber);
        for (int i = 0; i < filterFiles.size(); i++) {
            long time = filterFiles.get(i).lastModified();
            String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
            DialogRefRecallBean item = new DialogRefRecallBean();
            item.setIndex(i);
            item.setLastModifyTime(time);
            item.setPathFile(filterFiles.get(i).getAbsolutePath());
            if (filterFiles.get(i).getAbsolutePath().equals(selectFile)) {
                item.setSelect(true);
            } else {
                item.setSelect(false);
            }
            item.setTime(ctime);
            item.setTitle(filterFiles.get(i).getName());
            list.add(item);
        }
        list.sort((o1, o2) -> {
            long i = o2.getLastModifyTime() - o1.getLastModifyTime();
            if (i == 0) {
                return o2.getTitle().compareTo(o1.getTitle());
            } else {
                return Long.compare(o2.getLastModifyTime(), o1.getLastModifyTime());
            }
        });
        for (int i = 0; i < list.size(); i++) {
            DialogRefRecallBean item = list.get(i);
            item.setIndex(i);
        }
        return list;
    }

    public void clearSelect() { //清除选中
        if (list.isEmpty()) return;
        for (DialogRefRecallBean bean : list) {
            if (bean.isSelect()) bean.setSelect(false);
            adapter.notifyItemChanged(list.indexOf(bean));
        }
    }

    private void updateSelect() {
        if (list.isEmpty()) return;
        int index = -1;
        String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber);
        for (DialogRefRecallBean bean : list) {
            boolean isSelect = bean.getPathFile().equals(selectFile);
            bean.setSelect(isSelect);
            if (isSelect) {
                index = bean.getIndex();
            }
            adapter.notifyDataSetChanged();
        }
        if (index > -1) { //由于recyclerView高度固定（一屏显示11条item），要想使选中的滚动到中间，这里选中index+5，视觉上即在中间位置
            if (list.size() <= 12) return;//一屏能展示完，无需滚动
            int targetIndex = index + 6;
            if (targetIndex >= list.size()) targetIndex = list.size() - 1;
            recyclerView.smoothScrollToPosition(targetIndex);//滚动到选中的item
        }
    }

    private boolean isLongPressUp;
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            moveOnlyScroll(isLongPressUp);
            myHandler.postDelayed(this, 100);
        }
    };

    private View.OnTouchListener onTouchListener = new OnTouchListener() {
        private long startTime;

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.img_up:
                    isLongPressUp = true;
                    break;
                case R.id.img_bottom:
                    isLongPressUp = false;
                    break;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startTime = System.currentTimeMillis();
                    myHandler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());//默认400ms之后开启持续长按事件
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    long duration = System.currentTimeMillis() - startTime; //触摸开始到结束时间
                    if (duration < ViewConfiguration.getLongPressTimeout()) {//处理单击事件
                        moveOnlyScroll(isLongPressUp);
                    }
                    myHandler.removeCallbacks(longPressRunnable); //停止持续长按事件
                    break;
            }
            return true;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_delete_channel:
                    CacheUtil.get().putMapInForce(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChannelNumber, String.valueOf(false));
                    CacheUtil.get().putMapInForce(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(false));
                    switchBox.setState(false);
                    onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState());
//                    resetDefaultColor(refChannelNumber);
                    break;
                case R.id.channelMath:
                    setChannelState();
                    hideSlip();
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 0);
                    break;
                case R.id.channelRef:
                    setChannelState();
                    break;
                case R.id.channelSerials:
                    setChannelState();
                    hideSlip();
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 2);
                    break;
                case R.id.btnTop:
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174);
                    msgRef.setUpClick(true);
                    postChange();
                    break;
                case R.id.btnBottom:
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174);
                    msgRef.setUpClick(false);
                    postChange();
                    break;
                case R.id.btn_add_channel://TODO check文件存在性
                    if (adapter.getSelectItem() == null || adapter.getSelectItem().getPathFile().isEmpty()) {
                        DToast.get().show(context.getResources().getString(R.string.select_ref_data_first));
                    } else {
                        if (!Tools.fileIsExists(adapter.getSelectItem().getPathFile())) {
                            DToast.get().show(context.getResources().getString(R.string.select_flie_not_exist));
                            return;
                        }
                        if(getSelectedIndex() == 1) { //csv
                            loadCsvFile(adapter.getSelectItem());
                        } else {
                            needUpdateMasterLocation = true;
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChannelNumber, String.valueOf(true));
                            switchBox.setState(true);
                            onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState());
                            hideSlip();
                        }
                    }
                    break;
                case R.id.btn_browse:
                    handleBrowseClick();
                    break;
            }
        }
    };

    private void handleBrowseClick() {
        String spinnerSelectPath = spinner.getSelectItem();
        String disPlay = spinner.getDisPlaySelectItem();
        File file = new File(spinnerSelectPath);

        if (!file.exists() || !file.isDirectory()) {
            spinnerSelectPath = "/storage/emulated/0";
            disPlay = context.getResources().getString(R.string.internal_storage);
        }

        FileSelector fileSelector = new FileSelector((selectedPath) -> {
            addPathToPathSet(selectedPath);
            updateList();
        });
        fileSelector.buildSaveFileRefSelector(spinnerSelectPath, disPlay, null, context);
    }


    TopViewSpinner.onItemSelectListener onItemSelectListener = new TopViewSpinner.onItemSelectListener() {
        @Override
        public void onItemSelected(FileBean str) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_BIN, str.getPath());
            addPathToPathSet(str);
            updateList();
        }
    };

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
                canAdd = false;
                temp = fileBean;
                break;
            }
        }
        if (temp != null) {
            pathSet.remove(temp);
        }
        return canAdd;
    }

    public void savePathToCache() {
        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber);

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_PATH + refType + refChannelNumber,
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + refType + refChannelNumber,
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_PATH_CURRENT + refType + refChannelNumber, spinner.getSelectItem());
    }

    public void restorePath() {
        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber);
        pathSet.clear();
        String pathCacheStr = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_PATH + refType + refChannelNumber);
        String abPathCacheStr = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + refType + refChannelNumber);
        String currentPath = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_PATH_CURRENT + refType + refChannelNumber);
        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);
        if (!Tools.fileIsExists(currentPath)) {
            currentPath = null;
        }

        ArrayList<FileBean> dataList = new ArrayList<>();
        FileBean currentBean = new FileBean();
        for (int i = 0; i < abPathCacheList.size(); i++) {
            if (!Tools.fileIsExists(abPathCacheList.get(i))) continue;
            FileBean fileBean = new FileBean();
            fileBean.setPath(abPathCacheList.get(i));
            fileBean.setDisplayName(pathCacheList.get(i));
            if(abPathCacheList.get(i).equals(currentPath)) {
                currentBean.setPath(abPathCacheList.get(i));
                currentBean.setDisplayName(pathCacheList.get(i));
            }
            dataList.add(fileBean);
        }

        pathSet.addAll(dataList);
        spinner.updateDataList(getPreviousDirectory(), null);
    }


    private RightViewSelect.OnItemClickListener onRightViewSelectItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            RightLayoutRef.this.onItemClick(viewId, item, false);
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    @SuppressLint("NonConstantResourceId")
    private void onItemClick(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        switch (viewId) {
            case R.id.ref_load_type:
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber, String.valueOf(refLoadType.getSelectIndex()));
//                if (refChan == refChannelNumber) {
                restorePath();
                updateList();
//                }
                break;
        }
    }


    private void setChannelState() {
        channelMath.setChecked(false);
        channelRef.setChecked(true);
        channelSerials.setChecked(false);
    }

    private void setControlColorByChIdx(int chIdx) {
        //textColorNewTopTitleUnSelect
        channelTitle.setTextColor(TChan.getChannelColor(context, refChannelNumber));
        switchBox.setControlColorByChIdx(chIdx);
        channelRef.setTextColor(TChan.getChannelColor(context, refChannelNumber));

        refLoadType.setControlColorByChIdx(chIdx);
        setRefLoadTypeEnable();
        adapter.notifyItemChanged(list.indexOf(adapter.getSelectItem()));
//        selectColor.setEditColor(SvgNodeInfo.getAllBaseColor(refChannelNumber));

        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber);
        refLoadType.setSelectIndex(refType);
    }

    private void setRefLoadTypeEnable() {//目前只是CSV WAV可选，其他置灰
        if (refLoadType == null) return;
        this.refLoadType.setEnabled(0, true);//WAV
        this.refLoadType.setEnabled(1, true);//CSV
        this.refLoadType.setEnabled(2, false);
        this.refLoadType.setEnabled(3, false);
    }

    private void hideSlip() {
        int slipIndex = MainViewGroup.RIGHTSLIP_REF1;
        switch (refChannelNumber) {
            case TChan.R1:
                slipIndex = MainViewGroup.RIGHTSLIP_REF1;
                break;
            case TChan.R2:
                slipIndex = MainViewGroup.RIGHTSLIP_REF2;
                break;
            case TChan.R3:
                slipIndex = MainViewGroup.RIGHTSLIP_REF3;
                break;
            case TChan.R4:
                slipIndex = MainViewGroup.RIGHTSLIP_REF4;
                break;
            case TChan.R5:
                slipIndex = MainViewGroup.RIGHTSLIP_REF5;
                break;
            case TChan.R6:
                slipIndex = MainViewGroup.RIGHTSLIP_REF6;
                break;
            case TChan.R7:
                slipIndex = MainViewGroup.RIGHTSLIP_REF7;
                break;
            case TChan.R8:
                slipIndex = MainViewGroup.RIGHTSLIP_REF8;
                break;
        }
        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slipIndex, false));
    }

    private void updateMaxChannelNumber() {
        AtomicInteger maxChan = new AtomicInteger(TChan.R1 - 1);
        TChan.foreachRef(refChan -> {
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan);
//            if (refCheck) {
            if (isAddByUser(refChan)) {
                maxChan.set(Math.max(maxChan.get(), refChan));
            }
        });
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MAX_CHANNEL_NUMBER_REF, maxChan.toString());
    }

    private void postChange() {
        if (myHandler.hasMessages(HANDLE_MSG)) {
            myHandler.removeMessages(HANDLE_MSG);
        }
        myHandler.sendEmptyMessageDelayed(HANDLE_MSG, 200);
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_REFX, msgRef);
    }


    private static final int HANDLE_MSG = 1;

    public static class MyHandler extends Handler {
        private final WeakReference<RightLayoutRef> rightLayoutHandler;

        public MyHandler(RightLayoutRef layoutRef) {
            rightLayoutHandler = new WeakReference<RightLayoutRef>(layoutRef);
        }

        @Override
        public void handleMessage(Message msg) {
            if (rightLayoutHandler.get() != null) {
                if (msg.what == HANDLE_MSG) {
                    RightLayoutRef layoutRef = (RightLayoutRef) rightLayoutHandler.get();
                    layoutRef.ivBackground.setImageResource(R.drawable.svg_right_chx_button_88x174);
                }
            }
        }
    }

//    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
//
//        @Override
//        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//            if (newState == RecyclerView.SCROLL_STATE_IDLE) { //当前未滑动
//                showUpBottomImg();
//            }
//        }
//
//        @Override
//        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//            // dy大于0表示正在向上滑动，小于等于0表示停止或向下滑动
//        }
//    };

    private boolean isAddByUser(int refChan) {
        return CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan);
    }

    private void updateLoadProgress(int progress) {
        EventBase eventBase = new EventBase(EventFactory.EVENT_LOADCSV_RUN);
        Logger.i("LoadCsv progress=" + progress + " ,ThreadName= " + Thread.currentThread().getName());
        eventBase.setData(progress);
        EventFactory.sendEvent(eventBase, true);

    }

    public void setChannelLabel(int chNo, String label) {
        RefChannel refChannel = ChannelFactory.getRefChannel(chNo);
        if (refChannel != null) {
            refChannel.setLabel(label);
        }
    }

    public String getLabelFromChannel(int chNo) {
        String label = "";
        RefChannel refChannel = ChannelFactory.getRefChannel(chNo);
        if (refChannel != null) {
            label = refChannel.getLabel();
        }
        return label;
    }

    @SuppressLint("NonConstantResourceId")
    private final TopViewEdit.OnClickEditListener onClickEditListener = (v, text) -> {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.chLabel:
                if (dialogChannelLabel == null) {
                    dialogChannelLabel = (DialogChannelLabel) ((MainActivity) context).findViewById(R.id.dialogChannelLabel);
                }
                dialogChannelLabel.setData(refChannelNumber, chLabel.getText()
                        , CacheUtil.RIGHT_SLIP_CH_LABEL_USERDEFINE + refChannelNumber
                        , DialogChannelLabel.FROM_MATHREF
                        , result -> {
                            PlaySound.getInstance().playButton();
                            setLabel(result);
                        }
                );
                break;
            case R.id.select_color:
                if (dialogSelectColor == null) {
                    dialogSelectColor = (DialogSelectColor) ((MainActivity)context).findViewById(R.id.dialogSelectColor);
                }
                dialogSelectColor.setData(DialogSelectColor.FROM_MATHREF, refChannelNumber, (chIndex, colorStr) -> {
                    if (refChannelNumber == chIndex) {
                        Logger.d(TAG, "选中的颜色值为：" + colorStr + " channelNum= " + chIndex);
                        selectColor.setEditColor(colorStr);
                    }
                });
                break;
            case R.id.phase_delay:
                if(dialogFloatKeyBoard == null) {
                    dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
                }

                String txt= phaseDelay.getText().replace("s", "").replace(" ","");
                dialogFloatKeyBoard.setRefFloatData(txt, phaseDelay, new TopDialogFloatKeyBoard.OnDismissListener() {
                    @Override
                    public void onDismiss(View fromView, String show) {
                        PlaySound.getInstance().playButton();
                        double delay = TBookUtil.getBigDoubleFromM(show);
                        if ("0".equals(show.trim())) {
                            show = "0 ns";
                        } else {
                            show = show + "s";
                        }
                        RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber));
                        double original = 0;
                        if(refChannel != null) {
                            original = refChannel.getRefXPos_original() / 1.0E13;
                            refChannel.setDelay(delay);
                        }
                        Logger.d(TAG, "result= " + show + " ,delay= " + delay + " ,original= " + original);
//                        setTimePos(delay);
//                        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + refChannelNumber, show);
                        phaseDelay.setText(show);

                    }
                });
                break;
            default:
                break;
        }
    };

    private void setTimePos(Double delay) {
        RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber));
        if (refChannel == null) return;
        if (refChannel.getRefType() != WaveData.FFT_WAVE) {
            Command.get().getReference().Timebase_Position(TChan.toFpgaChNo(refChannelNumber), delay, true);
        }
        ((MainActivity) context).getSlider().updateUI();
    }


    private Runnable loadCsRunnable = new Runnable() {
        @Override
        public void run() {

            int flag = ScreenControls.LOCK_LOADCSV << (refChannelNumber - TChan.R1);

            int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber);
            String refFilePath = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber);
            String tempString = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_FROM + refChannelNumber);
            String[] channelToRef = tempString.split(";");
            if (channelToRef.length > 0 && channelToRef[0].equals(refFilePath)) {
                LoadCsv loadCsv = new LoadCsv();
                boolean loadSuccess = false;
                try {
                    loadSuccess = loadCsv.load(refFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "chNums:" + loadCsv.getChNums() + ",b:" + loadSuccess);
                if (!loadSuccess) return;//这里是冷启动加载Cache中记录的文件，解析失败暂不做处理。
                HashMap<Integer, Integer> map = new HashMap<>();
                map.put(TChan.toFpgaChNo(refChannelNumber), TChan.toFpgaChNo(Integer.parseInt(channelToRef[1])));
                loadCsv.loadToRef(map);
                loadCsv.setLoadCsvProgress(new LoadCsv.ILoadCsvProgress() {
                    @Override
                    public void onProgress(int val) {
                        ScreenControls.getInstance().csvupdate(flag,val);
                    }
                });

                while (!loadCsv.isFinish()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                post(()->{
                    DialogRefRecallBean item = null;
                    for (DialogRefRecallBean bean : getList(spinner.getSelectItem())) {
                        if (bean.getPathFile().equals(refFilePath)) {
                            item = bean;
                            break;
                        }
                    }
                    ChannelFactory.chOpen(TChan.toFpgaChNo(refChannelNumber));
                    setCacheMapValue(TChan.toFpgaChNo(refChannelNumber), item);
                    ScreenControls.getInstance().unLockScreen(flag);
                });
            }
        }
    };

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }


    public void scrollToPositionAndGetView(int targetPosition) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        layoutManager.scrollToPosition(targetPosition);
        recyclerView.post(() -> {
            View targetView = layoutManager.findViewByPosition(targetPosition);
            if (targetView != null) {
                int[] local = new int[2];
                targetView.getLocationOnScreen(local);
                RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_CSV_ITEM_POS,
                        refChannelNumber + CommandMsgToUI.PARAM_SPLIT + local[1] + CommandMsgToUI.PARAM_SPLIT + targetPosition);
            }
        });
    }

    private ArrayList<Integer> channelInCsv = new ArrayList<>();

    public ArrayList<Integer> getChannelInCsv() {
        return channelInCsv;
    }

    EventUIObserver eventLoadCsvObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase base = (EventBase) data;
            if (base == null) return;
            int progress = 0;
            switch (base.getId()) {
                case EventFactory.EVENT_LOADCSV_RUN:
                    progress = (int) ((EventBase) data).getData();
                    break;
            }
            ScreenControls screenControls = ScreenControls.getInstance();
            if (progress < 0 || progress >= 100) {
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);
            } else {
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);
                }
                screenControls.setProgressValue(progress);
            }
        }
    };

    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return;
            Logger.i(TAG, "selectColorInfo= " + colorInfo);
            String[] info = colorInfo.split(";");
            int chIndex = Integer.parseInt(info[0]);
            String colorStr = info[1];
            setControlColorByChIdx(chIndex);
        }
    };

    private void resetDefaultColor(int chIndex) {
        String colorDefault = SvgNodeInfo.getDefaultColor(chIndex);
        SvgNodeInfo.setChannelColor(chIndex, colorDefault);//改变颜色值
        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_COLOR + chIndex, colorDefault);
        selectColor.setEditColor(colorDefault);
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR, chIndex + ";" + colorDefault);//发消息通知颜色值改变了
    }



    private final Consumer<String> consumerUpdateTimeBase = new Consumer<String>() {
        @Override
        public void accept(String triggerTimeBaseInfo) throws Throwable {
//            Logger.d(TAG, "chActive= " + ChannelFactory.getChActivate() + " ,timePosInfo =" + triggerTimeBaseInfo);
//            String timePos = triggerTimeBaseInfo.split(";")[0];
//            int chIdx = Integer.parseInt(triggerTimeBaseInfo.split(";")[1]);
//            if (chIdx != TChan.toFpgaChNo(refChannelNumber)) return;
//            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber));
//            double original = 0;
//            double delay = 0;
//            if (refChannel != null) {
//                original = refChannel.getRefXPos_original() / 1.0E13;
//                delay = refChannel.getDelay();
//            }
//            double val = TBookUtil.getBigDoubleFromM(timePos.replace("s", "").replace(" ", ""));
//            double finalVal = val - original;
//            Logger.d(TAG, "oriainal= " + original + " ,val= " + val + " final= " + finalVal + " text= " + TBookUtil.getFourFromD(finalVal));
//            phaseDelay.setText(TBookUtil.getFourFromD(finalVal) + "s");
        }
    };


    private final Consumer<Integer> consumerRefTimeBase = new Consumer<Integer>() {
        @Override
        public void accept(Integer refTbIndex) throws Throwable {
            if (refTbIndex == 1) {//独立调节
                resetDelayUI();
            }
        }
    };

    private void resetDelayUI() {
        phaseDelay.setText("0 ns");
    }


    private Consumer<String> consumerMouseClick = new Consumer<String>() {
        @Override
        public void accept(String clickInfo) throws Throwable {
            String[] info = clickInfo.split(";");
            int chIdx = Integer.parseInt(info[0]);
            int clickPos = Integer.parseInt(info[1]);//0垂直档位  1垂直位置  2水平挡位  3水平位置
            Logger.d(TAG, "ClickInfo chidx= " + chIdx + " ,clickPos= " + clickPos);
            if (dialogFloatKeyBoard == null) {
                dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
            }
            if (TChan.toUiChNo(chIdx) != refChannelNumber) return;
            String unit = ChannelFactory.getProbeType(chIdx);
            RefChannel refChannel = ChannelFactory.getRefChannel(chIdx);
            if (refChannel == null) return;
            if (clickPos == 0) { //垂直档位
                double extent = refChannel.getVScaleIdVal();
                setChVScale(TBookUtil.getMFromDouble(extent) + unit, chIdx);
            }
            if (clickPos == 1) { //垂直位置
                double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));
                int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);
                double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos;
                String number = TBookUtil.getFourFromD_Trim0(pos * refChannel.getVerticalPerPix());
                setChVPosition(number + unit, chIdx);

            }
            if (clickPos == 3) { //水平位置
                int followCh = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
                String timePosStr = "";
                if (refChannel.getRefType() != WaveData.FFT_WAVE) {
                    long timePos;
                    if (followCh == 0) { //跟随模拟通道
                        timePos = HorizontalAxis.getInstance().getTimePosOfView();
                    } else {
                        timePos = refChannel.getTimePosOfView();
                    }
                    timePosStr = TBookUtil.getSFrom100Fs(timePos);
                    setChHPosition(timePosStr, chIdx);
                }

            }
        }
    };

    //水平位置
    public void setChHPosition(String nowTxt, int chIdx) {
        dialogFloatKeyBoard.bringToFront();

        String unit = ChannelFactory.getProbeType(chIdx);
        String txt = nowTxt.toString().replaceAll("(?:s|\\s)", "");
        dialogFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                double val = TBookUtil.getBigDoubleFromM(show);
                Command.get().getTimebase().Position(val, true);
            }
        });
    }


    //垂直位置
    private void setChVPosition(String nowTxt, int chIdx) {
        dialogFloatKeyBoard.bringToFront();

        String unit = ChannelFactory.getProbeType(chIdx);
        String txt = nowTxt.replace(unit, "").replace(" ", "");
        dialogFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                Log.d(TAG, "onDismiss: " + chIdx);
                RefChannel channel = ChannelFactory.getRefChannel(chIdx);
                if(channel!=null){
                    PlaySound.getInstance().playButton();
                    double val = TBookUtil.getDoubleFromM(show);
                    Command.get().getReference().Position(chIdx, val, true);
                }

            }
        });
    }


    //垂直档位
    private void setChVScale(String nowTxt, int chIdx) {
        dialogFloatKeyBoard.bringToFront();

        String unit = ChannelFactory.getProbeType(chIdx);
        String txt = nowTxt.replace(unit, "").replace(" ", "");
        dialogFloatKeyBoard.setFloatData_Extent(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                double d = TBookUtil.getDoubleFromM(show);
                Command.get().getReference().Vscale(chIdx, d, true);
            }
        });
    }


}
