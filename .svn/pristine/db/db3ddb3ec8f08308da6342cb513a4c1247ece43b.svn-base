package com.micsig.tbook.tbookscope.top.layout.display;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleSegmented;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.display.RulerManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.top.view.TopViewSeekBar;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/6.
 */

public class TopLayoutDisplayCommon extends Fragment {
    private static final String TAG = "TopLayoutDisplayCommon";
    private Context context;
    private TopViewRadioGroup rgHorRef;
    private TopViewRadioGroup rgDisplayMode;
    private MSwitchBox sbRoll;
    private MSwitchBox sbCCT;
    private MSwitchBox sbScale;
    private TextView editPosition;
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;

    private TopMsgDisplayCommon displayDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;
    private boolean sampleSegmentedOpen = false;
    private boolean isAutoSaveStart = false;

    private TopViewSeekBar displayTransparency;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_displaycommon, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initData() {
        displayDetail = new TopMsgDisplayCommon();
        displayDetail.setHorRef(rgHorRef.getSelected());
        displayDetail.setTimeBase(rgDisplayMode.getSelected());
        displayDetail.setRoll(sbRoll.isState());
        displayDetail.setCct(sbCCT.isState());
        displayDetail.setAlpha(0.8f);
    }

    private void initView(View view) {
        rgHorRef = (TopViewRadioGroup) view.findViewById(R.id.displayHorRef);
        rgHorRef.setData(R.string.displayHorRef, R.array.displayHorRef, onCheckChangedListener);
        rgDisplayMode = (TopViewRadioGroup) view.findViewById(R.id.displayTimebase);
        rgDisplayMode.setData(R.string.displayTimebase, R.array.displayTimebase, onCheckChangedListener);
        sbRoll = (MSwitchBox) view.findViewById(R.id.displayRoll);
        sbRoll.setOnToggleStateChangedListener(onToggleStateChangedListener);
        sbCCT = (MSwitchBox) view.findViewById(R.id.displayCCT);
        sbCCT.setOnToggleStateChangedListener(onToggleStateChangedListener);
        displayTransparency = view.findViewById(R.id.transparency);
        sbScale = (MSwitchBox) view.findViewById(R.id.displayScale);
        sbScale.setOnToggleStateChangedListener(onToggleStateChangedListener);

        displayTransparency.setData(R.string.view_display_transparency, 100, 30, seekBarChangeListener);
        displayTransparency.setVisibility(View.GONE);

        editPosition = view.findViewById(R.id.edit_time_pos);
        editPosition.setOnClickListener(onClickListener);
        dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { //波形亮度调节
            displayDetail.setAlpha(progress/100.0f);
            sendMsg(false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ALPHA, String.valueOf(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerSampleSegment);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_TIMEBASE).subscribe(consumerUpdateTimeBase);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION).subscribe(consumerMouseClick);
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE).subscribe(consumerAutoSaveTaskState);
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_MODE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_HORREF, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_CCT, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_SET_CH_TIMEPOS, eventUIObserver);
    }

    private void setCache() {
        int horRef = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_HORREF);
        int displayMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE);
        int roll = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL);
        int cct = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_CCT);
        int scale=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_SCALE);
        int alpha = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ALPHA);
        alpha=0;

        rgHorRef.setSelectedIndex(horRef);
        rgDisplayMode.setSelectedIndex(displayMode);
        sbRoll.setState(roll == 0);
        sbCCT.setState(cct == 0);
        sbScale.setState(scale==0);
        displayTransparency.setProgress(alpha);

        Command.get().getDisplay().HorRef(horRef, false);
        Command.get().getDisplay().setRoutineTimeBaseMode(displayMode, false);
        Command.get().getDisplay().setRoutineRollingScreen(roll, false);
        Command.get().getDisplay().Cct(cct==0, false);

        int mode = 1;
        if (roll != 0) {
            mode = displayMode == 0 ? 0 : 2;
        }
        Command.get().getTimebase().Mode(mode, false);

        Display.getInstance().setHorRef(horRef);
        Display.getInstance().setDisplayMode(displayMode);
        Display.getInstance().setRoll(roll == 0);
        Display.getInstance().setCCT(cct == 0);
        RulerManage.getIns().setShow(scale==0);

        WaveManage.get().setClickSelectEnable(cct != 0);

        displayDetail.setHorRef(rgHorRef.getSelected());
        displayDetail.setTimeBase(rgDisplayMode.getSelected());
        displayDetail.setRoll(sbRoll.isState());
        displayDetail.setCct(sbCCT.isState());
        displayDetail.setAlpha(alpha/100.0f);
        sendMsg(false);

        CommandMsgToUI msgToUI_H = new CommandMsgToUI();
        CommandMsgToUI msgToUI_V = new CommandMsgToUI();
        if (displayMode == 0) {
            boolean isZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
            if (!isZoom) {
                //YT
                WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, false);
                CursorManage.getInstance().initXY();

                boolean cursorH = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORH_VISIBLE);
                boolean cursorV = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORV_VISIBLE);
                msgToUI_H.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL);
                msgToUI_H.setParam(String.valueOf(cursorH));
                msgToUI_V.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL);
                msgToUI_V.setParam(String.valueOf(cursorV));
            }
        } else {
            //XY
            WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_XY, false);
            CursorManage.getInstance().initXY();
            boolean cursorH_xy = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORH_VISIBLE);
            boolean cursorV_xy = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORV_VISIBLE);
            msgToUI_H.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL);
            msgToUI_H.setParam(String.valueOf(cursorH_xy));
            msgToUI_V.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL);
            msgToUI_V.setParam(String.valueOf(cursorV_xy));
        }
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI_H);
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI_V);
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE, new TopMsgSegmentedState(false));
    }

    public TopMsgDisplayCommon getDisplayDetail() {
        return displayDetail;
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutDisplayCommon, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_DISPLAY_HORREF:
                    rgHorRef.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                    onCheckChanged(rgHorRef, rgHorRef.getSelected(), false);
                    break;
                case CommandMsgToUI.FLAG_DISPLAY_TIMEBASE:
                    rgDisplayMode.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                    onCheckChanged(rgDisplayMode, rgDisplayMode.getSelected(), false);
                    break;
                case CommandMsgToUI.FLAG_TIMEBASE_MODE:
                    int mode = Integer.parseInt(commandMsgToUI.getParam());
                    switch (mode) {
                        case 0:
                        case 2: {
                            int selectIndex = mode == 0 ? 0 : 1;
                            rgDisplayMode.setSelectedIndex(selectIndex);
                            onCheckChanged(rgDisplayMode, rgDisplayMode.getSelected(), false);
                        }
                        break;
                        case 1: {
                            sbRoll.setState(mode == 1);
                            onStateChanged(sbRoll, sbRoll.isState(), false);
                        }
                        break;

                    }
                    break;
                case CommandMsgToUI.FLAG_DISPLAY_CCT:
                    sbCCT.setState(Boolean.parseBoolean(commandMsgToUI.getParam()));
                    onStateChanged(sbCCT, sbCCT.isState(), false);
                    break;
                case CommandMsgToUI.FLAG_TIMEBASE_ROLL:{
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    sbRoll.setState(b);
                    onStateChanged(sbRoll, sbRoll.isState(), false);
                }break;
            }
        }
    };
    private Consumer<TopMsgSampleSegmented> consumerSampleSegment = new Consumer<TopMsgSampleSegmented>() {
        @Override
        public void accept(TopMsgSampleSegmented msgSampleSegmented) throws Exception {
            boolean isOpen = msgSampleSegmented.getState().isValue();
            sampleSegmentedOpen = isOpen;
            rgDisplayMode.setEnabled(!sampleSegmentedOpen && !isAutoSaveStart);
            sbRoll.setEnabled(!isOpen);
            sendMsg(msgSampleSegmented.isFromEventBus());
        }
    };


    private MainTopMsgRightGone msgTopGone = new MainTopMsgRightGone();

    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            onStateChanged(view, state, false);
        }
    };

    private void onStateChanged(MSwitchBox view, boolean state, boolean isFromEventBus) {
        if (view.getId() == R.id.displayRoll) {
            if (Tools.isSlowTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE))) {
                if (state) {
                    msgTopGone.setVisible(false);
                    RxBus.getInstance().post(RxEnum.MAIN_TOPRIGHT_GONE, msgTopGone);
                    if (ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate())
                            || ChannelFactory.isRefCh(ChannelFactory.getChActivate())) {
                        msgTopGone.setVisible(true);
                        RxBus.getInstance().post(RxEnum.MAIN_TOPCENTER_TEXT_GONE, msgTopGone);
                    }
                } else {
                    msgTopGone.setVisible(true);
                    RxBus.getInstance().post(RxEnum.MAIN_TOPRIGHT_GONE, msgTopGone);
                }
            } else {
                msgTopGone.setVisible(true);
                RxBus.getInstance().post(RxEnum.MAIN_TOPRIGHT_GONE, msgTopGone);
            }

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL, String.valueOf(state ? 0 : 1));
            displayDetail.setRoll(state);
            sendMsg(isFromEventBus);
            Command.get().getDisplay().setRoutineRollingScreen(state ? 0 : 1, false);
            if (!isFromEventBus) {
                if(!state) {
                    long l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL);
                    HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - l);
                }
                Display.getInstance().setRoll(state);
            }
            if (Scope.getInstance().isZoom() && Scope.getInstance().isRun() && Scope.getInstance().isInScrollMode()) {
                RxBus.getInstance().post(RxEnum.ROLL_RUN_ZOOM, false);
            }
            RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE, new TopMsgSegmentedState(isFromEventBus));
            Command.get().getTimebase().Roll_Display(sbRoll.isState(),false);
        } else if (view.getId() == R.id.displayCCT) {
            WaveManage.get().setClickSelectEnable(!state);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_CCT, String.valueOf(state ? 0 : 1));
            displayDetail.setCct(state);
            sendMsg(isFromEventBus);
            Command.get().getDisplay().Cct(state, false);
            if (!isFromEventBus) {
                Display.getInstance().setCCT(state);
            }
        }else if (view.getId() == R.id.displayScale){
            RulerManage.getIns().setShow(state);
            WorkModeManage.getInstance().refresh();
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_SCALE, String.valueOf(state ? 0 : 1));

        }

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
            onCheckChanged(view, item, false);
        }
    };

    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        if (view.getId() == R.id.displayHorRef) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_HORREF, String.valueOf(item.getIndex()));
            displayDetail.setHorRef(item);
            sendMsg(isFromEventBus);
            Command.get().getDisplay().HorRef(item.getIndex(), false);
            if (!isFromEventBus) {
                Display.getInstance().setHorRef(item.getIndex());
            }
        } else if (view.getId() == R.id.displayTimebase) {
            if (item.getIndex() == displayDetail.getTimeBase().getIndex()) {
                return;
            }
            if (item.getIndex() == 1) {
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch1, String.valueOf(CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION + TChan.Ch1)));
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch2, String.valueOf(CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION + TChan.Ch2)));
                WaveManage.get().setCenterChY(TChan.Ch1);
                WaveManage.get().setCenterChY(TChan.Ch2);
            }
            Message msg = new Message();
            msg.what = MSG_CHANGE_PREV;
            msg.obj = item;
            msg.arg1 = isFromEventBus ? 1 : 0;
            handler.sendMessageDelayed(msg, MSG_DELAY);
        }
    }

    private static final int MSG_CHANGE_PREV = 888;
    private static final int MSG_CHANGE_NEXT = 889;
    private static final int MSG_DELAY = 10;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CHANGE_PREV:
                    TopBeanChannel item = (TopBeanChannel) msg.obj;
                    boolean isFromEventBus = msg.arg1 == 1;
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE, String.valueOf(item.getIndex()));
                    displayDetail.setTimeBase(item);
                    sendMsg(isFromEventBus);
                    CommandMsgToUI msgToUI_H = new CommandMsgToUI();
                    CommandMsgToUI msgToUI_V = new CommandMsgToUI();
                    if (item.getIndex() == 0) {
                        //YT
//                    WorkModeManage.getInstance().setmWorkMode(IWorkMode.WorkMode_YT);
                        WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, isFromEventBus);
                        CursorManage.getInstance().initXY();
                        if (!isFromEventBus) {
                            Display.getInstance().setDisplayMode(Display.DISPLAY_YT);
                        }

                        boolean cursorH = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORH_VISIBLE);
                        boolean cursorV = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORV_VISIBLE);
                        msgToUI_H.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL);
                        msgToUI_H.setParam(String.valueOf(cursorH));
                        msgToUI_V.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL);
                        msgToUI_V.setParam(String.valueOf(cursorV));
                    } else {
                        //XY
//                    WorkModeManage.getInstance().setmWorkMode(IWorkMode.WorkMode_XY);
                        WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_XY, isFromEventBus);
                        CursorManage.getInstance().initXY();
                        if (!isFromEventBus) {
                            Display.getInstance().setDisplayMode(Display.DISPLAY_XY);
                            Scope scope = Scope.getInstance();
                            if(scope.isRun()){
                                scope.setRun(true);
                            }
                        }
                        boolean cursorH_xy = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORH_VISIBLE);
                        boolean cursorV_xy = CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORV_VISIBLE);
                        msgToUI_H.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL);
                        msgToUI_H.setParam(String.valueOf(cursorH_xy));
                        msgToUI_V.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL);
                        msgToUI_V.setParam(String.valueOf(cursorV_xy));
                    }
                    RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI_H);
                    RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI_V);
                    Command.get().getDisplay().setRoutineTimeBaseMode(item.getIndex(), false);
                    if (item.getIndex() == 0) {
                        handler.sendEmptyMessageDelayed(MSG_CHANGE_NEXT, MSG_DELAY);
                    }
                    RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE, new TopMsgSegmentedState(isFromEventBus));
                    Command.get().getTimebase().Mode(item.getIndex(),false);
                    break;
                case MSG_CHANGE_NEXT:
                    WaveManage.get().setPositionY(TChan.Ch1, CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch1));
                    WaveManage.get().setPositionY(TChan.Ch2, CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch2));
                    break;
            }
        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            if (((EventBase) data).getId() == EventFactory.EVENT_DISPLAY_MODE) {
                if (rgDisplayMode.getSelected().getIndex() != Display.getInstance().getDisplayMode()) {
                    rgDisplayMode.setSelectedIndex(Display.getInstance().getDisplayMode());
                    onCheckChanged(rgDisplayMode, rgDisplayMode.getSelected(), true);
                    int mode = Display.DISPLAY_YT == Display.getInstance().getDisplayMode() ? 0 : 2;
                    Command.get().getTimebase().Mode(mode, false);
                }
            } else if (((EventBase) data).getId() == EventFactory.EVENT_HORREF) {
                if (rgHorRef.getSelected().getIndex() != Display.getInstance().getHorRef()) {
                    rgHorRef.setSelectedIndex(Display.getInstance().getHorRef());
                    onCheckChanged(rgHorRef, rgHorRef.getSelected(), true);
                }
            } else if (((EventBase) data).getId() == EventFactory.EVENT_DISPLAY_CCT) {
                if ((sbCCT.isState()) != Display.getInstance().isCCT()) {
                    sbCCT.setState(Display.getInstance().isCCT());
                    onStateChanged(sbCCT, sbCCT.isState(), true);
                }
            } else if (((EventBase) data).getId() == EventFactory.EVENT_SET_CH_TIMEPOS) {
                int chActive = ChannelFactory.getChActivate();
                double timePos = (double) ((EventBase) data).getData();
                Command.get().getTimebase().Position(timePos, true);//CH

                TChan.foreachMath(mathChan -> {//FFT
                    if(ChannelFactory.isChOpen(TChan.toFpgaChNo(mathChan)) && ChannelFactory.isMath_FFT_Ch(TChan.toFpgaChNo(mathChan))){
                        Command.get().getMath_fft().Position(TChan.toFpgaChNo(mathChan), timePos, true);
                        ((MainActivity) context).getSlider().updateUI();
                    }
                });

                TChan.foreachRef(refChan -> {
                    int refIndex = TChan.toFpgaChNo(refChan);
                    RefChannel refChannel = ChannelFactory.getRefChannel(refIndex);
                    if (refChannel == null || ChannelFactory.isChOpen(refIndex)) return;
                    if (refChannel.getRefType() != WaveData.FFT_WAVE) {
                        Command.get().getReference().Timebase_Position(refIndex, timePos, true);
                        ((MainActivity) context).getSlider().updateUI();
                    }
                });

                if (ChannelFactory.isRefCh(chActive)) {//最后原来的Ref
                    RefChannel refChannel = ChannelFactory.getRefChannel(chActive);
                    if (refChannel == null) return;
                    if (refChannel.getRefType() != WaveData.FFT_WAVE) {
                        Command.get().getReference().Timebase_Position(chActive, timePos, true);
                        ((MainActivity) context).getSlider().updateUI();
                    }
                }
            }
        }
    };

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.edit_time_pos:
                    clickTimepos();
                    break;
                default:
                    break;
            }
        }
    };

    private void clickTimepos() {
        if(rgDisplayMode.getSelected().getIndex() == 1) return;
        String txt = editPosition.getText().toString().replaceAll("(?:s|\\s)", "");
        dialogFloatKeyBoard.setFloatData(txt, editPosition, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                double val = TBookUtil.getBigDoubleFromM(show);
                Logger.d(TAG, "result= " + show + " ,val= " + val);
                setTimePos(val);
            }
        });
    }


    private void setTimePos(Double timePos) {
        int chActive = ChannelFactory.getChActivate();
        Logger.d(TAG, "setTimePos= " + timePos + " ,chActive= " + chActive);
        if (ChannelFactory.isMath_FFT_Ch(chActive)) {
            Command.get().getMath_fft().Position(chActive, timePos, true);
            ((MainActivity) context).getSlider().updateUI();
        }
        if (ChannelFactory.isDynamic_or_math_Ch(chActive) || ChannelFactory.isSerialCh(chActive) || ChannelFactory.isRefCh(chActive)) {
            Command.get().getTimebase().Position(timePos, true);
            ((MainActivity) context).getSlider().updateUI();
        }
    }

    private final Consumer<String> consumerUpdateTimeBase = new Consumer<String>() {
        @Override
        public void accept(String triggerTimeBaseInfo) throws Throwable {
            Logger.d(TAG, "chActive= " + ChannelFactory.getChActivate() + " ,timePosInfo =" + triggerTimeBaseInfo);
            int refTimeBaseIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
            String timePos = triggerTimeBaseInfo.split(";")[0];
            int chIdx = Integer.parseInt(triggerTimeBaseInfo.split(";")[1]);
            if (refTimeBaseIndex == 0) { //跟随
                editPosition.setText(timePos);
            } else {
//                if (chIdx != ChannelFactory.getChActivate() || !ChannelFactory.isDynamic_or_math_Ch(chIdx)) return;
                if (chIdx != ChannelFactory.getChActivate()) return;
                editPosition.setText(timePos);
            }
        }
    };


    private Consumer<String> consumerMouseClick = new Consumer<String>() {
        @Override
        public void accept(String clickInfo) throws Throwable {
            String[] info = clickInfo.split(";");
            int chIdx = Integer.parseInt(info[0]);
            int clickPos = Integer.parseInt(info[1]);//0垂直档位  1垂直位置  2水平挡位  3水平位置 4点击的是触发位置
            Logger.d(TAG, "editPosition ClickInfo chidx= " + chIdx + " ,clickPos= " + clickPos);
            if (clickPos == 4) { //顶部触发位置
                clickTimepos();
            }
        }
    };

    private final Consumer<Boolean> consumerAutoSaveTaskState = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean isAutoSaveStop) throws Throwable {
            requireActivity().runOnUiThread(() -> {
                Logger.i("limh isAutoSaveStop= " + isAutoSaveStop);
                isAutoSaveStart = !isAutoSaveStop;
                rgDisplayMode.setEnabled(!isAutoSaveStart && !sampleSegmentedOpen);
            });
        }
    };

}
