package com.micsig.tbook.tbookscope.top.layout.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase;
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterSegmented;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


public class TopLayoutSampleSegmented extends Fragment {
    private static final int TEMP_MAX_SEGMENT_NUMS = 30;

    private Context context;
    private MSwitchBox rgState;
    private TopViewRadioGroup rgNumber;
    private TextView tvUserDefine;
    private TopViewRadioGroup rgDisplay;
    private TextView tvFitStart;
    private TextView tvFitEnd;
    private TopViewRadioGroup rgOrder;

    private TopDialogNumberKeyBoard dialogNumberKeyBoard;

    private TopMsgSampleSegmented msgSampleSegmented;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_sample_segmented, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initView(View view) {
        rgState = (MSwitchBox) view.findViewById(R.id.segmentedState);
        rgNumber = (TopViewRadioGroup) view.findViewById(R.id.segmentedNumber);
        tvUserDefine = (TextView) view.findViewById(R.id.segmentedUsersetNumber);
        rgDisplay = (TopViewRadioGroup) view.findViewById(R.id.segmentedDisplay);
        tvFitStart = (TextView) view.findViewById(R.id.segmentedFitStart);
        tvFitEnd = (TextView) view.findViewById(R.id.segmentedFitEnd);
        rgOrder = (TopViewRadioGroup) view.findViewById(R.id.segmentedOrder);

        dialogNumberKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);

        rgState.setOnToggleStateChangedListener(onToggleStateChangedListener);

        rgNumber.setData(R.string.sampleSegmentedNumber, R.array.sampleSegmentedNumber, onCheckChangedListener);
        tvUserDefine.setOnClickListener(onClickListener);
        rgDisplay.setData(R.string.sampleSegmentedDisplay, R.array.sampleSegmentedDisplay, onCheckChangedListener);
        tvFitStart.setOnClickListener(onClickListener);
        tvFitEnd.setOnClickListener(onClickListener);
        rgOrder.setData(R.string.sampleSegmentedOrder, R.array.sampleSegmentedOrder, onCheckChangedListener);
    }

    private void initData() {
        msgSampleSegmented = new TopMsgSampleSegmented();
        msgSampleSegmented.setState(rgState.isState());
        msgSampleSegmented.setNumber(rgNumber.getSelected());
        msgSampleSegmented.setUserDefine(tvUserDefine.getText().toString());
        msgSampleSegmented.setDisplay(rgDisplay.getSelected());
        msgSampleSegmented.setStart(tvFitStart.getText().toString());
        msgSampleSegmented.setEnd(tvFitEnd.getText().toString());
        msgSampleSegmented.setOrder(rgOrder.getSelected());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_SEGMENTED).subscribe(consumerCenterLayoutSegmented);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLEDEPTH).subscribe(consumerSampleDepth);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE).subscribe(consumerSegmentedState);
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase);
        EventFactory.addEventObserver(EventFactory.EVENT_SEGMENT_FRAMES, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_SEGMENT_TIMESTAMP, eventUIObserver);
    }

    private void setCache() {
        int state = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE);
        int number = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER);
        int userDefine = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE);
        int display = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY);
        int fitStart = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_START);
        int fitEnd = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_END);
        int order = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_ORDER);

        rgState.setState(state==0);
        rgNumber.setSelectedIndex(number);
        rgDisplay.setSelectedIndex(display);
        tvFitStart.setText(String.valueOf(fitStart));
        tvFitEnd.setText(String.valueOf(fitEnd));
        rgOrder.setSelectedIndex(order);
        setNumberDetail();
        setDisplayDetailVisible();

        SegmentSample segment = SegmentSample.getInstance();
        segment.setSegmentEnable(rgState.isState());

        segment.setSegmentNums(getSegmentNums());
        segment.setSegmentDisplayType(rgDisplay.getSelected().getIndex());
        segment.setFittingBegingFrame(fitStart - 1);
        segment.setFittingEndFrame(fitEnd - 1);

        msgSampleSegmented.setState(rgState.isState());
        msgSampleSegmented.setNumber(rgNumber.getSelected());
        msgSampleSegmented.setUserDefine(tvUserDefine.getText().toString());
        msgSampleSegmented.setDisplay(rgDisplay.getSelected());
        msgSampleSegmented.setStart(tvFitStart.getText().toString());
        msgSampleSegmented.setEnd(tvFitEnd.getText().toString());
        msgSampleSegmented.setOrder(rgOrder.getSelected());
        sendMsg(false);

//        RxBus.getInstance().post(RxEnum.MAINCENTER_SEGMENTED_VISIBLE, rgState.getSelected().getIndex() == 0);
        showSegment();
    }

    private void showSegment() {
        MeasureManage.SegmentMeasure segmentMeasure = MeasureManage.getInstance().getSegmentMeasure();
        segmentMeasure.setVisible(rgState.isState());
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void update(Object data) {
            int eventId = ((EventBase) data).getId();
            if (eventId == EventFactory.EVENT_SEGMENT_FRAMES) {
                MeasureManage.SegmentMeasure segmentMeasure = MeasureManage.getInstance().getSegmentMeasure();
                SegmentSample segmentSample = SegmentSample.getInstance();
                String strTxt = "";
                if (Scope.getInstance().isRun()) {
                    int nums = segmentSample.getSegmentNums();
                    if (nums > segmentSample.getMaxSegmentNums())
                        nums = segmentSample.getMaxSegmentNums();
                    strTxt = "" + segmentSample.getSegmentFrames() + "/" + nums;
                } else {
                    Scope scope = Scope.getInstance();
                    int nums = scope.getSegmentFrameNums();
                    int val = (scope.getSegmentFrameNo() + 1);
                    if (nums <= 0) val = 0;
                    strTxt = "" + val + "/" + nums;
                }
                segmentMeasure.setText(strTxt);
            } else if (eventId == EventFactory.EVENT_SEGMENT_TIMESTAMP) {
//                Logger.i(Command.TAG,"nums:"+Scope.getInstance().getSegmentFrameNums());
                Scope scope = Scope.getInstance();

                setBeginFrame(0);
                setEndFrame(scope.getSegmentFrameNums());
                sendMsg(true);
            }
        }
    };


    private int getSegmentNums() {
        int segmentNum = 100;
        SegmentSample segmentSample = SegmentSample.getInstance();
        segmentSample.setEnableMaxSegment(false);
        switch (rgNumber.getSelected().getIndex()) {
            case 0:
                segmentNum = 100;
                break;
            case 1:
                segmentNum = 1000;
                break;
            case 2:
                segmentSample.setEnableMaxSegment(true);
                segmentNum = segmentSample.getMaxSegmentNums();
                break;
            case 3:
                segmentNum = Integer.parseInt(tvUserDefine.getText().toString());
                break;
        }
        return segmentNum;
    }

    private void setNumberDetail() {
        int userDefine = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE);
        if (rgNumber.getSelected().getIndex() == 3) {
            tvUserDefine.setText(String.valueOf(userDefine));
            tvUserDefine.setEnabled(true);
        } else {
            tvUserDefine.setText("---");
            tvUserDefine.setEnabled(false);
        }
    }

    private void setDisplayDetailVisible() {
        if (rgDisplay.getSelected().getIndex() == 1) {
            tvFitStart.setVisibility(View.VISIBLE);
            tvFitEnd.setVisibility(View.VISIBLE);
            rgOrder.setVisibility(View.INVISIBLE);
        } else {
            tvFitStart.setVisibility(View.INVISIBLE);
            tvFitEnd.setVisibility(View.INVISIBLE);
            rgOrder.setVisibility(View.VISIBLE);
        }
    }

    private void sendMsg(boolean isFromEventBus) {
        msgSampleSegmented.setFromEventBus(isFromEventBus);
//        Logger.d("sendMsg() called with: TOPLAYOUT_SAMPLESEGMENTED = [" + msgSampleSegmented + "]");
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED, msgSampleSegmented);
    }

    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            onCheckChanged(view, state, false);
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSampleSegmented, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_SAMPLE_IsOpenSegMent:{
                    boolean isOpen = Boolean.parseBoolean(commandMsgToUI.getParam());
                    rgState.setState(isOpen);
                    onCheckChanged(rgState, isOpen, false);
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedQTY:{
                    int i=Integer.parseInt(commandMsgToUI.getParam());
                    if (i==100){
                        rgNumber.setSelectedIndex(0);
                        onCheckChanged(rgNumber,rgNumber.getSelected(),true,false);
                    }else if (i==1000){
                        rgNumber.setSelectedIndex(1);
                        onCheckChanged(rgNumber,rgNumber.getSelected(),true,false);
                    }else{
                        rgNumber.setSelectedIndex(3);
                        if (i>10000) i=10000;
                        tvUserDefine.setText(String.valueOf(i));
                        onCheckChanged(rgNumber,rgNumber.getSelected(),true,false);
                        onTextChanged(tvUserDefine,String.valueOf(i),true,false);
                    }

                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedDisplayType:{
                    int index=Integer.parseInt(commandMsgToUI.getParam());
                    rgDisplay.setSelectedIndex(index);
                    onCheckChanged(rgDisplay,rgDisplay.getSelected(),true,false);
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedOrder:{
                    int index=Integer.parseInt(commandMsgToUI.getParam());
                    rgOrder.setSelectedIndex(index);
                    onCheckChanged(rgOrder,rgOrder.getSelected(),true,false);
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedFra2:{
                    if (tvFitStart.getVisibility()==View.VISIBLE){
                        int i=Integer.parseInt(commandMsgToUI.getParam());
                        tvFitStart.setText(String.valueOf(i));
                        onTextChanged(tvFitStart,String.valueOf(i),true,false);
                    }
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedFra3:{
                    if (tvFitEnd.getVisibility()==View.VISIBLE){
                        int i=Integer.parseInt(commandMsgToUI.getParam());
                        tvFitEnd.setText(String.valueOf(i));
                        onTextChanged(tvFitEnd,String.valueOf(i),true,false);
                    }
                }break;
            }
        }
    };

    private Consumer<MainMsgCenterSegmented> consumerCenterLayoutSegmented = new Consumer<MainMsgCenterSegmented>() {
        @Override
        public void accept(MainMsgCenterSegmented msgCenterSegmented) throws Exception {

            int display = msgCenterSegmented.isDisplay().isValue() ? 1 : 0;
            int order = msgCenterSegmented.isSingleOrder().isValue() ? 0 : 1;
            int fitStart = msgCenterSegmented.getFitStart().getFrameId();
            int fitEnd = msgCenterSegmented.getFitEnd().getFrameId();
            boolean changed = false;
            int maxSegmentNums = Scope.getInstance().getSegmentFrameNums();
            //int maxSegmentNums = TEMP_MAX_SEGMENT_NUMS;
            if (maxSegmentNums < 1) maxSegmentNums = 1;
            if (fitStart > maxSegmentNums) fitStart = maxSegmentNums;
            if (fitEnd > maxSegmentNums) fitEnd = maxSegmentNums;
            if (rgDisplay.getSelected().getIndex() != display) {
                changed = true;
                rgDisplay.setSelectedIndex(display);
                onCheckChanged(rgDisplay, rgDisplay.getSelected(), false, false);
            }
            if (rgOrder.getSelected().getIndex() != order) {
//                Logger.d("accept() rgOrder.getSelected().getIndex() = [" + rgOrder.getSelected().getIndex() + "]");
                changed = true;
                rgOrder.setSelectedIndex(order);
                onCheckChanged(rgOrder, rgOrder.getSelected(), false, false);
            }
            if (StrUtil.isEmpty(tvFitStart.getText().toString()) || Integer.parseInt(tvFitStart.getText().toString()) != fitStart) {
                changed = true;
                tvFitStart.setText(String.valueOf(fitStart));
                onTextChanged(tvFitStart, tvFitStart.getText().toString(), false, false);
            }
            if (StrUtil.isEmpty(tvFitEnd.getText().toString()) || Integer.parseInt(tvFitEnd.getText().toString()) != fitEnd) {
                changed = true;
                tvFitEnd.setText(String.valueOf(fitEnd));
                onTextChanged(tvFitEnd, tvFitEnd.getText().toString(), false, false);
            }
            if (changed) {
                sendMsg(false);
            }
        }
    };

    private Consumer<TopMsgSampleDepth> consumerSampleDepth = new Consumer<TopMsgSampleDepth>() {
        @Override
        public void accept(TopMsgSampleDepth msgSampleDepth) throws Exception {
            setNumberEnable(msgSampleDepth.isFromEventBus());
        }
    };

    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() {
        @Override
        public void accept(MainRightMsgChannels mainRightMsgChannels) throws Exception {
            if (mainRightMsgChannels.isChangeChState()) {
                setNumberEnable(mainRightMsgChannels.isFromEventBus());
            }
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
            //TODO 数学多通道
            if (mainRightMsgOthers.getMath1().isRxMsgSelect()) {
                setNumberEnable(false);
            }
        }
    };

    private Consumer<RightMsgMath> consumerRightMath = new Consumer<RightMsgMath>() {
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception {
            if (rightMsgMath.getMathType().isRxMsgSelect()) {
                setNumberEnable(false);
            }
        }
    };

    private void setNumberEnable(boolean isFromEventBus) {
        if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_DEPTH) == 0) {
            rgNumber.setEnabled(true);
            sendMsg(isFromEventBus);
            return;
        }

        int maxSegmentNums = SegmentSample.getInstance().getMaxSegmentNums();
        if (maxSegmentNums < 1) maxSegmentNums = 1;//保证段数最少为1
        //int maxSegmentNums = TEMP_MAX_SEGMENT_NUMS;
        String[] array = context.getResources().getStringArray(R.array.sampleSegmentedNumber);
        if (maxSegmentNums < Integer.parseInt(array[0])) {
            if (rgNumber.getSelected().getIndex() == 0) {
                rgNumber.setSelectedIndex(2);
                onCheckChanged(rgNumber, rgNumber.getSelected(), false, isFromEventBus);
            }
            rgNumber.setEnabled(0, false);
        } else {
            rgNumber.setEnabled(0, true);
        }
        if (maxSegmentNums < Integer.parseInt(array[1])) {
            if (rgNumber.getSelected().getIndex() == 1) {
                rgNumber.setSelectedIndex(2);
                onCheckChanged(rgNumber, rgNumber.getSelected(), false, isFromEventBus);
            }
            rgNumber.setEnabled(1, false);
        } else {
            rgNumber.setEnabled(1, true);
        }
        if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE) > maxSegmentNums) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE, String.valueOf(maxSegmentNums));
            setNumberDetail();
//                onTextChanged(tvUserDefine, String.valueOf(maxSegmentNums), false, msgSampleDepth.isFromEventBus());
        }

        if(!isFromEventBus) {
            SegmentSample segmentSample = SegmentSample.getInstance();
            if (segmentSample.isSegmentEnable()) {
                segmentSample.setSegmentNums(getSegmentNums());
            }
        }
        sendMsg(isFromEventBus);
    }

    private Consumer<TopMsgSegmentedState> consumerSegmentedState = new Consumer<TopMsgSegmentedState>() {
        @Override
        public void accept(TopMsgSegmentedState msgSegmentedState) throws Exception {
            boolean isXYMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1;
            boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
            boolean isRoll = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL) == 0
                    && Tools.isSlowTimeBase();
            if (!isXYMode && !isSerialsTxt && !isRoll) {
                rgState.setEnabled(true);
                sendMsg(msgSegmentedState.isFromEventBus());
            } else {
                if (rgState.isState()) {
                    rgState.setState(false);
                    onCheckChanged(rgState, rgState.isState(), msgSegmentedState.isFromEventBus());
                }
                rgState.setEnabled(false);
                sendMsg(msgSegmentedState.isFromEventBus());
            }
        }
    };

    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() {
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception {
            setNumberEnable(msgTimeBase.isFromEventBus());
            RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE, new TopMsgSegmentedState(msgTimeBase.isFromEventBus()));
        }
    };

    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            onCheckChanged(view, item, true, false);
        }

        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {

        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            Screen.getViewLocation(v);
            if (v.getId() == tvUserDefine.getId()) {
                dialogNumberKeyBoard.setDecimalData(tvUserDefine.getText().toString(), 8, IDigits.DIGITS_10
                        , new TopDialogNumberKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(String result) {
                                result = TBookUtil.getNumRemovePreZero(result);
                                onTextChanged(tvUserDefine, result, true, false);
                            }
                        });
            } else if (v.getId() == tvFitStart.getId()) {
                dialogNumberKeyBoard.setDecimalData(tvFitStart.getText().toString(), 8, IDigits.DIGITS_10
                        , new TopDialogNumberKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(String result) {
                                result = TBookUtil.getNumRemovePreZero(result);
                                onTextChanged(tvFitStart, result, true, false);
                            }
                        });
            } else if (v.getId() == tvFitEnd.getId()) {
                dialogNumberKeyBoard.setDecimalData(tvFitEnd.getText().toString(), 8, IDigits.DIGITS_10
                        , new TopDialogNumberKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(String result) {
                                result = TBookUtil.getNumRemovePreZero(result);
                                onTextChanged(tvFitEnd, result, true, false);
                            }
                        });
            }
        }
    };

    private void onCheckChanged(MSwitchBox view, boolean state, boolean isFromEventBus)
    {
        if (view.getId() == rgState.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE, String.valueOf(state?0:1));

            SegmentSample.getInstance().setSegmentEnable(state);
            msgSampleSegmented.setState(state);
            sendMsg(isFromEventBus);
            showSegment();
        }
    }
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isSendMsg, boolean isFromEventBus) {
        if (view.getId() == rgNumber.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER, String.valueOf(item.getIndex()));
            setNumberDetail();

            SegmentSample.getInstance().setSegmentNums(getSegmentNums());
            msgSampleSegmented.setNumber(item);
            if (isSendMsg) {
                sendMsg(isFromEventBus);
            }
        } else if (view.getId() == rgDisplay.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY, String.valueOf(item.getIndex()));

            SegmentSample.getInstance().setSegmentDisplayType(rgDisplay.getSelected().getIndex());
            setDisplayDetailVisible();
            msgSampleSegmented.setDisplay(item);
            if (isSendMsg) {
                sendMsg(isFromEventBus);
            }
        } else if (view.getId() == rgOrder.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_ORDER, String.valueOf(item.getIndex()));
            msgSampleSegmented.setOrder(item);
            if (isSendMsg) {
                sendMsg(isFromEventBus);
            }
        }
    }

    private void onTextChanged(TextView textView, String result, boolean isSendMsg, boolean isFromEventBus) {

        int maxSegmentNums = 0;

        if (textView.getId() == tvUserDefine.getId()) {
            maxSegmentNums = SegmentSample.getInstance().getMaxSegmentNums();
            if (Integer.parseInt(result) < 1) {
                result = String.valueOf(1);
            }
        } else {
            maxSegmentNums = Scope.getInstance().getSegmentFrameNums();
            if (maxSegmentNums < 1) maxSegmentNums = 1;
        }

        if (Integer.parseInt(result) > maxSegmentNums) {
            result = String.valueOf(maxSegmentNums);
        }

        if (textView.getId() == tvUserDefine.getId()) {
            tvUserDefine.setText(result);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE, result);

            SegmentSample segmentSample = SegmentSample.getInstance();
            segmentSample.setEnableMaxSegment(false);
            segmentSample.setSegmentNums(Integer.parseInt(result));

            msgSampleSegmented.setUserDefine(result);
            if (isSendMsg) {
                sendMsg(isFromEventBus);
            }
        } else if (textView.getId() == tvFitStart.getId()) {
            int startFrame = Integer.parseInt(result);
            int endFrame = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_END);
            if (endFrame < startFrame) {
                endFrame = startFrame;
                setEndFrame(endFrame);
            }
            setBeginFrame(startFrame);
            if (isSendMsg) {
                sendMsg(isFromEventBus);
            }
        } else if (textView.getId() == tvFitEnd.getId()) {
            int endFrame = Integer.parseInt(result);
            int startFrame = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_START);
            if (startFrame > endFrame) {
                startFrame = endFrame;
                setBeginFrame(startFrame);
            }
            setEndFrame(endFrame);
            if (isSendMsg) {
                sendMsg(isFromEventBus);
            }
        }
    }

    private void setBeginFrame(int beginFrame) {
        beginFrame -= 1;
        if (beginFrame < 0) beginFrame = 0;
        String result = "" + (beginFrame + 1);
//        Logger.d("setBeginFrame:"+tvFitStart.getText() + "," + beginFrame);
        tvFitStart.setText(result);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_START, result);

        SegmentSample.getInstance().setFittingBegingFrame(beginFrame);
        msgSampleSegmented.setStart(result);
    }

    private void setEndFrame(int endFrame) {
//        Logger.i(Command.TAG,"endFrame:"+endFrame);
        endFrame -= 1;
        if (endFrame < 0) endFrame = 0;
        String result = "" + (endFrame + 1);
        tvFitEnd.setText(result);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_END, result);

        SegmentSample.getInstance().setFittingEndFrame(endFrame);
        msgSampleSegmented.setEnd(result);
    }
}