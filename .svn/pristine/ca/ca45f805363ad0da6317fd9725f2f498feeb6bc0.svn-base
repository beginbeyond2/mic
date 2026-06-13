package com.micsig.tbook.tbookscope.top.layout.sample;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTrigger;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.top.view.selectHorList.TopBeanHorizontal;
import com.micsig.tbook.ui.top.view.selectHorList.TopViewSelectHorListToHead;
import com.micsig.tbook.ui.top.view.selectHorList.TopViewSelectHorListToList;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutSampleMode extends Fragment {
    private static final int MATH_DIFF_SAMPLE_TYPE_INDEX = 1;
    private static final int MATH_DIFF_SAMPLE_DETAIL_INDEX = 1;

    private Context context;
    private TopViewRadioGroup rgSample;
    private TopViewSelectHorListToHead tvSampleDetail;
    private TopViewSelectHorListToList selectListToList;

    private TopMsgSampleMode msgSample;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_sample_mode, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initView(View view) {
        rgSample = (TopViewRadioGroup) view.findViewById(R.id.topViewSample);
        rgSample.setData(R.string.sampleMode, R.array.sampleMode, onCheckChangedListener);
        tvSampleDetail = (TopViewSelectHorListToHead) view.findViewById(R.id.sampleSelectListToHead);
        tvSampleDetail.setData("", "", selectListToHeadListener);
        selectListToList = (TopViewSelectHorListToList) ((MainActivity) context).findViewById(R.id.selectListToList);
        selectListToList.setData(R.id.sampleSelectListToHead, R.array.sampleDetail, selectListToListListener);

        msgSample = new TopMsgSampleMode();
        msgSample.setSample(rgSample.getSelected());
        msgSample.setDetail(tvSampleDetail.getText());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTopTrigger);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerSampleSegmented);
//        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_CHANNEL_SELECT).subscribe(consumerChannelSelect);

        EventFactory.addEventObserver(EventFactory.EVENT_SAMPLE_TYPE, eventUIObserver);
    }

    private void setCache() {
//        isWorkModeXY = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1;
//        isSerials = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1) || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)
//                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S1
//                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S2;
//        isSegment = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE) == 0;
//        int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
//        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE);
//        if ((channelSelect >= 0 && channelSelect <= 3)
//                || (channelSelect == 4 && mathType != CacheUtil.MATHTYPE_FFT)) {
//            //当当前通道为普通通道或数学的非fft通道时，判断是否为慢时基状态
//            isSlow = Tools.isSlowTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
//        }
//        boolean visible = !(isWorkModeXY || isSerials || isSlow || isSegment);
//        rgSample.setEnabled(1, visible);
//        rgSample.setEnabled(2, visible);
//        rgSample.setEnabled(3, !isWorkModeXY);
        setSampleEnable(false);
        int sampleIndex;
        int detailIndex;
        if (isMathAmDiff() && rgSample.isEnabled(MATH_DIFF_SAMPLE_TYPE_INDEX)) {
            sampleIndex = MATH_DIFF_SAMPLE_TYPE_INDEX;
            detailIndex = MATH_DIFF_SAMPLE_DETAIL_INDEX;
        } else {
            sampleIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE);
            detailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
//            if (!rgSample.getEnabled(sampleIndex)) {
//                sampleIndex = 0;
//            }
        }
        rgSample.setSelectedIndex(sampleIndex);
        setDetailEnable(sampleIndex);

        Command.get().getSample().Type(sampleIndex, false);
        Command.get().getSample().Mean(detailIndex, false);
        Command.get().getSample().Envelop(detailIndex, false);

        Sample.getInstance().setSampleType(matchSample(sampleIndex));
        Sample.getInstance().setSampleNum(getSampleNum());

        msgSample.setSample(rgSample.getSelected());
        msgSample.setDetail(tvSampleDetail.getText());
        msgSample.setSampleDetailIndex(detailIndex);
        sendMsgSample(false);
    }

    private int getSampleNum() {
        String s = tvSampleDetail.getText().replace("∞", "512");
        if (StrUtil.isEmpty(s)) {
            return 2;
        } else {
            return Integer.parseInt(s);
        }
    }

    private int matchSample(int sample) {
        if (sample == 1) return Sample.SAMPLE_TYPE_AVERAGE;
        if (sample == 2) return Sample.SAMPLE_TYPE_ENVEL;
        if (sample == 3) return Sample.SAMPLE_TYPE_PEAK;
        return Sample.SAMPLE_TYPE_NORMAL;
    }

    private int unMatchSample(int scopeSample) {
        if (scopeSample == Sample.SAMPLE_TYPE_AVERAGE) return 1;
        if (scopeSample == Sample.SAMPLE_TYPE_ENVEL) return 2;
        if (scopeSample == Sample.SAMPLE_TYPE_PEAK) return 3;
        return 0;
    }

    private void setDetailEnable(int sampleIndex) {
        if (sampleIndex != 1 && sampleIndex != 2) {
            tvSampleDetail.setText("");
            tvSampleDetail.setEnabled(false);
        } else {
            int detailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
            String[] sampleDetailArray = getEnvelList();
            tvSampleDetail.setText(sampleDetailArray[detailIndex]);
            tvSampleDetail.setEnabled(true);
        }
    }

    private String[] getAveList() {
        String[] sampleDetailArray = getEnvelList();
        String[] strings = new String[sampleDetailArray.length - 1];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = sampleDetailArray[i];
        }
        return strings;
    }

    private String[] getEnvelList() {
        return context.getResources().getStringArray(R.array.sampleDetail);
    }

    private void sendMsgSample(boolean isFromEventBus) {
        msgSample.setFromEventBus(isFromEventBus);
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLEMODE, msgSample);
    }

    /**
     * 当前选中了数学菜单下，高级数学中的，微分项，且重置标志还在....
     */
    private boolean isMathAmDiff() {
        final boolean[] isMathAmDiff = {false};
        TChan.foreachMath(mathChan -> {
            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan);
            boolean isMathAm = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChan) == CacheUtil.MATHTYPE_AM;
            boolean amFormulaHave = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + mathChan);
            boolean amFormulaReset = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + mathChan);
            boolean amFormula = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA).contains("ch");
            isMathAmDiff[0] = (mathCheck && isMathAm && amFormulaHave && amFormulaReset && amFormula) || isMathAmDiff[0];
        });
        return isMathAmDiff[0];
//        return
//                CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch1)
//                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE) == CacheUtil.MATHTYPE_AM
//                && CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE)
//                && CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET)
//                && CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA).contains("ch");
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSampleMode, true);
        }
    };


    //region 设置sample中后三个选项的可点击性
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            switch (workModeBean.getNextWorkMode()) {
                case IWorkMode.WorkMode_YT:
                case IWorkMode.WorkMode_YTZOOM:
                    isWorkModeXY = false;
                    setSampleEnable(workModeBean.isFromEventBus());
                    break;
                case IWorkMode.WorkMode_XY:
                    isWorkModeXY = true;
                    setSampleEnable(workModeBean.isFromEventBus());
                    break;
            }
        }
    };

    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() {
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception {
            if (msgTimeBase.getType() == MainBottomMsgTimeBase.TYPE_NORMAL) {
                isSlow = Tools.isSlowTimeBase();
            }
            setSampleEnable(msgTimeBase.isFromEventBus());
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception {
            boolean btnS1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
            boolean btnS2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
            boolean btnS3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
            boolean btnS4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
            int trigger = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            isSerials = btnS1 || btnS2 || btnS3 || btnS4
                    || trigger == TopLayoutTrigger.DETAIL_S1 || trigger == TopLayoutTrigger.DETAIL_S2
                    || trigger == TopLayoutTrigger.DETAIL_S3 || trigger == TopLayoutTrigger.DETAIL_S4;
            setSampleEnable(false);
            if (isMathAmDiff() && rgSample.isEnabled(MATH_DIFF_SAMPLE_TYPE_INDEX)) {
                if (rgSample.getSelected().getIndex() != MATH_DIFF_SAMPLE_TYPE_INDEX) {
                    rgSample.setSelectedIndex(MATH_DIFF_SAMPLE_TYPE_INDEX);
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, false);
                }
                String[] envelList = getEnvelList();
                if (!tvSampleDetail.getText().equals(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX])) {
                    tvSampleDetail.setText(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]);
                    onTextChanged(new TopBeanHorizontal(MATH_DIFF_SAMPLE_DETAIL_INDEX
                            , envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]), false, false);
                }
            } else {
                int indexSample = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE);
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
                if (rgSample.getSelected().getIndex() != indexSample) {
                    rgSample.setSelectedIndex(indexSample);
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, true);
                }
                String[] envelList = getEnvelList();
                if (!tvSampleDetail.getText().equals(envelList[sampleDetailIndex])) {
                    tvSampleDetail.setText(envelList[sampleDetailIndex]);
                    onTextChanged(new TopBeanHorizontal(sampleDetailIndex
                            , envelList[sampleDetailIndex]), false, true);
                }
            }
        }
    };

    private Consumer<TopMsgTrigger> consumerTopTrigger = new Consumer<TopMsgTrigger>() {
        @Override
        public void accept(TopMsgTrigger topMsgTrigger) throws Exception {
            if (topMsgTrigger.getTriggerTitle().isRxMsgSelect()) {
                boolean btnS1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
                boolean btnS2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
                boolean btnS3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
                boolean btnS4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
                int trigger = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
                isSerials = btnS1 || btnS2 || btnS3 || btnS4
                        || trigger == TopLayoutTrigger.DETAIL_S1 || trigger == TopLayoutTrigger.DETAIL_S2
                        || trigger == TopLayoutTrigger.DETAIL_S3 || trigger == TopLayoutTrigger.DETAIL_S4;;
                setSampleEnable(topMsgTrigger.isFromEventBus());
            }
        }
    };

    private Consumer<RightMsgMath> consumerRightMath = new Consumer<RightMsgMath>() {
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception {
            if (isMathAmDiff() && rgSample.isEnabled(MATH_DIFF_SAMPLE_TYPE_INDEX)) {
                if (rgSample.getSelected().getIndex() != MATH_DIFF_SAMPLE_TYPE_INDEX) {
                    rgSample.setSelectedIndex(MATH_DIFF_SAMPLE_TYPE_INDEX);
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, false);
                }
                String[] envelList = getEnvelList();
                if (!tvSampleDetail.getText().equals(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX])) {
                    tvSampleDetail.setText(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]);
                    onTextChanged(new TopBeanHorizontal(MATH_DIFF_SAMPLE_DETAIL_INDEX
                            , envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]), false, false);
                }
            } else {
                int indexSample = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE);
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
                if (rgSample.getSelected().getIndex() != indexSample) {
                    rgSample.setSelectedIndex(indexSample);
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, true);
                }
                String[] envelList = getEnvelList();
                if (!tvSampleDetail.getText().equals(envelList[sampleDetailIndex])) {
                    tvSampleDetail.setText(envelList[sampleDetailIndex]);
                    onTextChanged(new TopBeanHorizontal(sampleDetailIndex
                            , envelList[sampleDetailIndex]), false, true);
                }
            }
        }
    };

    private Consumer<TopMsgSampleSegmented> consumerSampleSegmented = new Consumer<TopMsgSampleSegmented>() {
        @Override
        public void accept(TopMsgSampleSegmented msgSampleSegmented) throws Exception {
            isSegment = msgSampleSegmented.getState().isValue();
            setSampleEnable(msgSampleSegmented.isFromEventBus());
        }
    };

//    private Consumer<MainCenterMsgChannels> consumerChannelSelect = new Consumer<MainCenterMsgChannels>() {
//        @Override
//        public void accept(MainCenterMsgChannels msgChannels) throws Exception {
//            setSampleEnable(msgChannels.isFromEventBus());
//        }
//    };

    /**
     * 是否是xy模式, 为true时，123置灰...
     */
    private boolean isWorkModeXY = false;
    /**
     * 总线解码是否开启，为true时，12置灰...
     */
    private boolean isSerials = false;
    /**
     * 是否是慢时基模式，为true时，12置灰...
     */
    private boolean isSlow = false;
    /**
     * 是否是分段存储开启模式，为true时，12置灰...
     */
    private boolean isSegment = false;
    /**
     * 是否是插值档，为true时，2置灰...
     */
    private boolean isChaZhiDang = false;

    /**
     * 设置sample的可点击性
     */
    private void setSampleEnable(boolean isFromEventBus) {
        isWorkModeXY = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1;
        isSerials = ChannelFactory.isChOpen(ChannelFactory.S1)
                || ChannelFactory.isChOpen(ChannelFactory.S2)
                || ChannelFactory.isChOpen(ChannelFactory.S3)
                || ChannelFactory.isChOpen(ChannelFactory.S4)
                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S1
                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S2
                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S3
                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S4;

        isSlow = Tools.isSlowTimeBase();
        isSegment = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE) == 0;
        isChaZhiDang = Tools.isChaZhiDang();
        boolean changeEnable = false;
        boolean b1 = msgSample.setSampleEnable(true, true, true, true);
        if (b1) {
            changeEnable = true;
        }
        if (isWorkModeXY) {
            boolean b = msgSample.setSampleEnable(false, false, false);
            if (b) {
                changeEnable = true;
            }
        }
        if (isSerials) {
            boolean b = msgSample.setSampleEnable(false, false);
            if (b) {
                changeEnable = true;
            }
        }
        if (isSlow) {
            boolean b = msgSample.setSampleEnable(false, false);
            if (b) {
                changeEnable = true;
            }
        }
        if (isSegment) {
            boolean b = msgSample.setSampleEnable(false, false);
            if (b) {
                changeEnable = true;
            }
        }
        if (isChaZhiDang) {
            boolean b = msgSample.setSampleEnable(2, false);
            if (b) {
                changeEnable = true;
            }
        }
        //由于sample的可点击性改变，发送消息
        if (isMathAmDiff() && msgSample.getSampleEnable()[MATH_DIFF_SAMPLE_TYPE_INDEX]) {
            boolean msgSend = false;
            if (rgSample.getSelected().getIndex() != MATH_DIFF_SAMPLE_TYPE_INDEX) {
                rgSample.setSelectedIndex(MATH_DIFF_SAMPLE_TYPE_INDEX);
                onCheckChanged(rgSample, rgSample.getSelected(), isFromEventBus, false, false);
                msgSend = true;
            }
            String[] envelList = getEnvelList();
            if (!tvSampleDetail.getText().equals(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX])) {
                tvSampleDetail.setText(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]);
                onTextChanged(new TopBeanHorizontal(MATH_DIFF_SAMPLE_DETAIL_INDEX
                        , envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]), isFromEventBus, false);
                msgSend = true;
            }
            if (!msgSend) {
                sendMsgSample(isFromEventBus);
            }
        } else {
            int indexSample = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE);
            if (msgSample.getSampleEnable()[indexSample]) {
                boolean msgSend = false;
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
                if (rgSample.getSelected().getIndex() != indexSample) {
                    rgSample.setSelectedIndex(indexSample);
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, true);
                    msgSend = true;
                }
                String[] envelList = getEnvelList();
                if (!tvSampleDetail.getText().equals(envelList[sampleDetailIndex])) {
                    tvSampleDetail.setText(envelList[sampleDetailIndex]);
                    onTextChanged(new TopBeanHorizontal(sampleDetailIndex
                            , envelList[sampleDetailIndex]), isFromEventBus, true);
                    msgSend = true;
                }
                if (!msgSend) {
                    sendMsgSample(isFromEventBus);
                }
            } else if (!msgSample.getSampleEnable()[rgSample.getSelected().getIndex()]) {
                rgSample.setSelectedIndex(0);
                onCheckChanged(rgSample, rgSample.getSelected(), isFromEventBus, false, true);
            } else {
                if (changeEnable) {
                    sendMsgSample(isFromEventBus);
                }
            }
        }
        rgSample.setEnabled(1, msgSample.getSampleEnable()[1]);
        rgSample.setEnabled(2, msgSample.getSampleEnable()[2]);
        rgSample.setEnabled(3, msgSample.getSampleEnable()[3]);

//        boolean visible = !(isWorkModeXY || isSerials || isSlow || isSegment);
//        msgSample.setSampleEnable(visible, visible, !isWorkModeXY);
//        //当总可见性为不可见，且不是非xy模式下的3，则改变选项...
//        if (!visible && !(rgSample.getSelected().getIndex() == 3 && !isWorkModeXY)) {
//            if (rgSample.getSelected().getIndex() != 0) {
//                rgSample.setSelectedIndex(0);
//                onCheckChanged(rgSample, rgSample.getSelected(), isFromEventBus, false, true);
//            } else {
//                sendMsgSample(isFromEventBus);
//            }
//        } else {
//            //由于sample的可点击性改变，发送消息
//            if (isMathAmDiff() && visible) {
//                boolean msgSend = false;
//                if (rgSample.getSelected().getIndex() != MATH_DIFF_SAMPLE_TYPE_INDEX) {
//                    rgSample.setSelectedIndex(MATH_DIFF_SAMPLE_TYPE_INDEX);
//                    onCheckChanged(rgSample, rgSample.getSelected(), isFromEventBus, false, false);
//                    msgSend = true;
//                }
//                String[] envelList = getEnvelList();
//                if (!tvSampleDetail.getText().equals(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX])) {
//                    tvSampleDetail.setText(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]);
//                    onTextChanged(new TopBeanHorizontal(MATH_DIFF_SAMPLE_DETAIL_INDEX
//                            , envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]), isFromEventBus, false);
//                    msgSend = true;
//                }
//                if (!msgSend) {
//                    sendMsgSample(isFromEventBus);
//                }
//            } else {
//                boolean msgSend = false;
//                int indexSample = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE);
//                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
//                if (rgSample.getSelected().getIndex() != indexSample) {
//                    rgSample.setSelectedIndex(indexSample);
//                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, true);
//                    msgSend = true;
//                }
//                String[] envelList = getEnvelList();
//                if (!tvSampleDetail.getText().equals(envelList[sampleDetailIndex])) {
//                    tvSampleDetail.setText(envelList[sampleDetailIndex]);
//                    onTextChanged(new TopBeanHorizontal(sampleDetailIndex
//                            , envelList[sampleDetailIndex]), isFromEventBus, true);
//                    msgSend = true;
//                }
//                if (!msgSend) {
//                    sendMsgSample(isFromEventBus);
//                }
//            }
//        }
//        boolean aveChange = rgSample.setEnabled(1, visible);
//        boolean envelChange = rgSample.setEnabled(2, visible);
//        boolean peakChange = rgSample.setEnabled(3, !isWorkModeXY);
//        rgSample.setEnabled(1, msgSample.getSampleEnable()[1]);
//        rgSample.setEnabled(2, msgSample.getSampleEnable()[2]);
//        rgSample.setEnabled(3, msgSample.getSampleEnable()[3]);
    }
    //endregion

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_SAMPLE_TYPE: {
                    rgSample.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                    onCheckChanged(rgSample, rgSample.getSelected(), false, true, true);
                    break;
                }
                case CommandMsgToUI.FLAG_SAMPLE_MEAN: {
                    if (rgSample.getSelected().getIndex() == 1) {
                        int index = Integer.parseInt(commandMsgToUI.getParam());
                        String[] sampleDetail = getEnvelList();
                        if (index >= 0 && index <= sampleDetail.length - 2) {
                            tvSampleDetail.setText(sampleDetail[index]);
                            onTextChanged(new TopBeanHorizontal(index
                                    , sampleDetail[index]), false, true);
                            break;
                        }
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_SAMPLE_ENVELOP: {
                    if (rgSample.getSelected().getIndex() == 2) {
                        int index = Integer.parseInt(commandMsgToUI.getParam());
                        String[] sampleDetail = getEnvelList();
                        if (index >= 0 && index <= sampleDetail.length - 1) {
                            tvSampleDetail.setText(sampleDetail[index]);
                            onTextChanged(new TopBeanHorizontal(index
                                    , sampleDetail[index]), false, true);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    };

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

    private TopViewSelectHorListToHead.OnClickListener selectListToHeadListener = new TopViewSelectHorListToHead.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            String[] sampleDetail = getEnvelList();
            if (rgSample.getSelected().getIndex() == 1) {
                selectListToList.setData(R.id.sampleSelectListToHead, getAveList(), selectListToListListener);
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
                selectListToList.setSelected(sampleDetailIndex);
                selectListToList.show(306);
            } else if (rgSample.getSelected().getIndex() == 2) {
                selectListToList.setData(R.id.sampleSelectListToHead, getEnvelList(), selectListToListListener);
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
                selectListToList.setSelected(sampleDetailIndex);
                selectListToList.show(306);
            }
        }
    };

    private TopViewSelectHorListToList.OnDialogChangedListener selectListToListListener = new TopViewSelectHorListToList.OnDialogChangedListener() {
        @Override
        public void checkChanged(int headViewId, TopBeanHorizontal item) {
            PlaySound.getInstance().playButton();
            onTextChanged(item, false, true);
        }

        @Override
        public void onShow() {
            RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_AFTERGLOW);
        }

        @Override
        public void onHide() {
            RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_AFTERGLOW);
        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            if (((EventBase) data).getId() == EventFactory.EVENT_SAMPLE_TYPE) {
                if (rgSample.getSelected().getIndex() != unMatchSample(Sample.getInstance().getSampleType())) {
                    rgSample.setSelectedIndex(unMatchSample(Sample.getInstance().getSampleType()));
                    onCheckChanged(rgSample, rgSample.getSelected(), true, false, true);
                }
            }
        }
    };

    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus, boolean isUser, boolean setCache) {
        if (view.getId() == R.id.topViewSample) {
            if (isUser) {
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET, String.valueOf(false));
            }
            int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
            String[] sampleDetailArray = getEnvelList();
            if (item.getIndex() == 1 && sampleDetailIndex == sampleDetailArray.length - 1) {
                sampleDetailIndex--;
                tvSampleDetail.setText(selectListToList.getSelected().getText());
                onTextChanged(new TopBeanHorizontal(sampleDetailIndex
                        , sampleDetailArray[sampleDetailIndex]), isFromEventBus, setCache);
            }
            setDetailEnable(item.getIndex());
            if (setCache) {
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_MODE, String.valueOf(item.getIndex()));
            }
            Command.get().getSample().Type(item.getIndex(), false);
            if (!isFromEventBus) {
                Sample.getInstance().setSampleType(matchSample(item.getIndex()));
            }
            if (item.getIndex() == 1) {//平均
                if (!isFromEventBus) {
                    Sample.getInstance().setSampleNum(getSampleNum());
                }
                Command.get().getSample().Mean(sampleDetailIndex, false);
            } else if (item.getIndex() == 2) {//包絡
                if (!isFromEventBus) {
                    Sample.getInstance().setSampleNum(getSampleNum());
                }
                Command.get().getSample().Envelop(sampleDetailIndex, false);
            }
            msgSample.setSample(item);
            msgSample.setDetail(tvSampleDetail.getText());
            msgSample.setSampleDetailIndex(sampleDetailIndex);
            sendMsgSample(isFromEventBus);
        }
    }

    private void onTextChanged(TopBeanHorizontal item, boolean isFromEventBus, boolean setCache) {
        if (rgSample.getSelected().getIndex() != 1 && rgSample.getSelected().getIndex() != 2) {
            tvSampleDetail.setText("");
            sendMsgSample(isFromEventBus);
            Command.get().getSample().Mean(0,false);
            Command.get().getSample().Envelop(0,false);
            return;
        } else {
            tvSampleDetail.setText(item.getText());
            if (rgSample.getSelected().getIndex()==1) {
                Command.get().getSample().Mean(item.getIndex(), false);
            }else if (rgSample.getSelected().getIndex()==2) {
                Command.get().getSample().Envelop(item.getIndex(), false);
            }
        }
        if (setCache) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX, String.valueOf(item.getIndex()));
        }
        Sample.getInstance().setSampleNum(getSampleNum());
        msgSample.setDetail(tvSampleDetail.getText());
        msgSample.setSampleDetailIndex(item.getIndex());
        sendMsgSample(isFromEventBus);
    }
}
