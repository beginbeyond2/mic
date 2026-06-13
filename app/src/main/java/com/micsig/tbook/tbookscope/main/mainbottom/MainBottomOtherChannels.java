package com.micsig.tbook.tbookscope.main.mainbottom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainHolderRightOthers;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * @Description: bottom other channel view (Math Ref Serials)
 * @Author: lmh
 * @CreateDate: 2024/3/11 15:53
 */
public class MainBottomOtherChannels extends RecyclerView.ViewHolder {

    private static final String TAG = "MainBottomChannelOthers";
    private Context context;
    private ImageView imgLeft, imgRight;
    private Button btnOtherChannel;
    private int slideMathIndex = MainViewGroup.RIGHTSLIP_MATH1;
    private int slideRefIndex = MainViewGroup.RIGHTSLIP_REF1;
    private int slideSerialsIndex = MainViewGroup.RIGHTSLIP_S1;
    private int currentMathChan = TChan.Math1;
    private int currentRefChan = TChan.R1;
    private int currentSerialsChan = TChan.S1;
    private HorizontalScrollView scrollView;
    private MainViewGroup mainViewGroup;
    private LinearLayout llBottomOtherChannels;
    private boolean isSerialsWordShow = false;//文本页面是否显示

    public MainBottomOtherChannels(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        mainViewGroup = (MainViewGroup) itemView;
        new MainHolderRightOthers(itemView);
        intiView(itemView);
    }

    private void intiView(View itemView) {
        llBottomOtherChannels = itemView.findViewById(R.id.ll_bottom_other);
        scrollView = itemView.findViewById(R.id.scroll_view);
        imgLeft = itemView.findViewById(R.id.img_left);
        imgRight = itemView.findViewById(R.id.img_right);
        btnOtherChannel = itemView.findViewById(R.id.btn_other_channel);

        imgLeft.setOnClickListener(onClickListener);
        imgRight.setOnClickListener(onClickListener);
        btnOtherChannel.setOnClickListener(onClickListener);
        SpannableString spannableString = new SpannableString("Math\nRef\nBus");
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.color_Math)), 0, 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.color_R_text)), 5, 8, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.color_S1)), 9, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        btnOtherChannel.setText(spannableString);
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL).subscribe(consumerShowOtherChannel);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_BOTTOM_OTHER_CHANNEL_EDGE_IMG).subscribe(consumerShowEdgeImg);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);

        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialsWordVisible);

        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OPEN_ADD_CHANNEL_MENU).subscribe(consumerOpenAddChannelMenu);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.img_left:
                    scrollView.smoothScrollBy(-152, 0);
                    break;
                case R.id.img_right:
                    scrollView.smoothScrollBy(152, 0);
                    break;
                case R.id.btn_other_channel:
//                    if (checkMaxAvailableChannel()) {//判断Math/Ref/Serials 是否达到最大限制
                    if (checkUsableAvailableChannel()) {//判断Math/Ref/Serials 是否可添加
                        RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true);
                        setNormalState();
                    } else {
                        DToast.get().show(R.string.mathRefSerials_can_not_add);
                    }
                    break;
            }
        }
    };

    boolean serialsAvailable = false;
    boolean refAvailable = false;
    boolean mathAvailable = false;
    private boolean checkMaxAvailableChannel() {
        //串口通道
        int lastSerialChan = getMaxSerialChannelNumber();
        Logger.d(TAG, "lastSerialChan = " + lastSerialChan);
        if (TChan.isSerial(lastSerialChan + 1)) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(lastSerialChan + 1));
            if (serialChannel == null) {
                serialsAvailable = false;
                DToast.get().show(R.string.serials_can_not_add);
            } else {
                currentSerialsChan = lastSerialChan + 1;
                updateSlideSerialIndex(currentSerialsChan);
                serialsAvailable = true;
            }
        } else {
            serialsAvailable = false;
            DToast.get().show(R.string.serials_channel_already_max);
        }

        //参考通道
//        int lastRefChan = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MAX_CHANNEL_NUMBER_REF);
        int lastRefChan = getMaxRefChannelNumber();
        Logger.d(TAG, "lastRefChan = " + lastRefChan);
        if (TChan.isRef(lastRefChan + 1)) {
            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(lastRefChan + 1));
            if (refChannel == null) {
                refAvailable = false;
                DToast.get().show(R.string.ref_can_not_add);
            } else {
                currentRefChan = lastRefChan + 1;
                updateSlideRefIndex(currentRefChan);
                refAvailable = true;
            }
        } else {
            refAvailable = false;
            DToast.get().show(R.string.ref_channel_already_max);
        }

        //数学通道
        int lastMathChan = getMaxMathChannelNumber();
        Logger.d(TAG, "lastMathChan = " + lastMathChan);
        if (TChan.isMath(lastMathChan + 1)) {
            MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(lastMathChan + 1));
            if (mathChannel == null) {
                mathAvailable = false;
                DToast.get().show(R.string.math_can_not_add);
            } else {
                currentMathChan = lastMathChan + 1;
                updateSlideMathIndex(currentMathChan);
                mathAvailable = true;
            }
        } else {
            mathAvailable = false;
            DToast.get().show(R.string.math_channel_already_max);
        }

        //如果各个通道都占满了，不能再做增加操作了
        return serialsAvailable || refAvailable || mathAvailable;
    }

    //检查当前是否可以添加数学通道
    private boolean checkAddMathAvailable(boolean showSingleToast) {
        //数学通道
        int lastMathChan = getUsableMathChannelNumber();
        Logger.d(TAG, "lastMathChan = " + lastMathChan);
        if (TChan.isMath(lastMathChan)) {
            MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(lastMathChan));
            if (mathChannel == null) {
                mathAvailable = false;
                DToast.get().show(R.string.math_can_not_add);
            } else {
                currentMathChan = lastMathChan;
                updateSlideMathIndex(currentMathChan);
                mathAvailable = true;
            }
        } else {
            mathAvailable = false;
            if(showSingleToast) {
                DToast.get().show(R.string.math_channel_already_max);
            }
        }
        return mathAvailable;
    }

    //检查当前是否可以添加参考通道
    private boolean checkAddRefAvailable(boolean showSingleToast) {
        //参考通道
        int lastRefChan = getUsableRefChannelNumber();
        Logger.d(TAG, "lastRefChan = " + lastRefChan);
        if (TChan.isRef(lastRefChan)) {
            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(lastRefChan));
            if (refChannel == null) {
                refAvailable = false;
                DToast.get().show(R.string.ref_can_not_add);
            } else {
                currentRefChan = lastRefChan;
                updateSlideRefIndex(currentRefChan);
                refAvailable = true;
            }
        } else {
            refAvailable = false;
            if(showSingleToast) {
                DToast.get().show(R.string.ref_channel_already_max);
            }
        }
        return refAvailable;
    }

    //检查当前是否可以添加串口通道
    private boolean checkAddSerialAvailable(boolean showSingleToast) {
        //串口通道
        int lastSerialChan = getUsableSerialChannelNumber();
        Logger.d(TAG, "lastSerialChan = " + lastSerialChan);
        if (TChan.isSerial(lastSerialChan)) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(lastSerialChan));
            if (serialChannel == null) {
                serialsAvailable = false;
                DToast.get().show(R.string.serials_can_not_add);
            } else {
                currentSerialsChan = lastSerialChan;
                updateSlideSerialIndex(currentSerialsChan);
                serialsAvailable = true;
            }
        } else {
            serialsAvailable = false;
            if(showSingleToast) {
                DToast.get().show(R.string.serials_channel_already_max);
            }
        }
        return serialsAvailable;
    }

    private boolean checkUsableAvailableChannel() {
        //串口通道
        checkAddSerialAvailable(false);
        //参考通道
        checkAddRefAvailable(false);
        //数学通道
        checkAddMathAvailable(false);
        //如果各个通道都占满了，不能再做增加操作了
        return serialsAvailable || refAvailable || mathAvailable;
    }

    private void updateSlideMathIndex(int currentMathChan) {
        switch (currentMathChan) {
            case TChan.Math1:
                slideMathIndex = MainViewGroup.RIGHTSLIP_MATH1;
                break;
            case TChan.Math2:
                slideMathIndex = MainViewGroup.RIGHTSLIP_MATH2;
                break;
            case TChan.Math3:
                slideMathIndex = MainViewGroup.RIGHTSLIP_MATH3;
                break;
            case TChan.Math4:
                slideMathIndex = MainViewGroup.RIGHTSLIP_MATH4;
                break;
            case TChan.Math5:
                slideMathIndex = MainViewGroup.RIGHTSLIP_MATH5;
                break;
            case TChan.Math6:
                slideMathIndex = MainViewGroup.RIGHTSLIP_MATH6;
                break;
            case TChan.Math7:
                slideMathIndex = MainViewGroup.RIGHTSLIP_MATH7;
                break;
            case TChan.Math8:
                slideMathIndex = MainViewGroup.RIGHTSLIP_MATH8;
                break;
            default:
                slideMathIndex = MainViewGroup.RIGHTSLIP_MATH1;
                break;
        }
    }

    private void updateSlideRefIndex(int currentRefChan) {
        switch (currentRefChan) {
            case TChan.R1:
                slideRefIndex = MainViewGroup.RIGHTSLIP_REF1;
                break;
            case TChan.R2:
                slideRefIndex = MainViewGroup.RIGHTSLIP_REF2;
                break;
            case TChan.R3:
                slideRefIndex = MainViewGroup.RIGHTSLIP_REF3;
                break;
            case TChan.R4:
                slideRefIndex = MainViewGroup.RIGHTSLIP_REF4;
                break;
            case TChan.R5:
                slideRefIndex = MainViewGroup.RIGHTSLIP_REF5;
                break;
            case TChan.R6:
                slideRefIndex = MainViewGroup.RIGHTSLIP_REF6;
                break;
            case TChan.R7:
                slideRefIndex = MainViewGroup.RIGHTSLIP_REF7;
                break;
            case TChan.R8:
                slideRefIndex = MainViewGroup.RIGHTSLIP_REF8;
                break;
            default:
                slideRefIndex = MainViewGroup.RIGHTSLIP_REF1;
                break;
        }
    }

    private void updateSlideSerialIndex(int currentSerialsChan) {
        switch (currentSerialsChan) {
            case TChan.S1:
                slideSerialsIndex = MainViewGroup.RIGHTSLIP_S1;
                break;
            case TChan.S2:
                slideSerialsIndex = MainViewGroup.RIGHTSLIP_S2;
                break;
            case TChan.S3:
                slideSerialsIndex = MainViewGroup.RIGHTSLIP_S3;
                break;
            case TChan.S4:
                slideSerialsIndex = MainViewGroup.RIGHTSLIP_S4;
                break;
            default:
                slideSerialsIndex = MainViewGroup.RIGHTSLIP_S1;
                break;
        }
    }

    private Consumer<Integer> consumerShowOtherChannel = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Throwable {
//            checkMaxAvailableChannel();
            checkUsableAvailableChannel();
            switch (integer) {
                case 0: //显示数学通道设置页面
                    if (mathAvailable) {
                        resetOpenState();
                        isMathOpen = true;
                        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slideMathIndex, true, false));
                    }
                    break;
                case 1: //显示参考通道设置页面
                    if (refAvailable) {
                        resetOpenState();
                        isRefOpen = true;
                        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slideRefIndex, true, false));
                    }
                    break;
                case 2: //显示串口通道设置页面
                    if (serialsAvailable) {
                        resetOpenState();
                        isSerialsOpen = true;
                        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slideSerialsIndex, true, false));
                    }
                    break;
            }
        }
    };

    private Consumer<Boolean> consumerShowEdgeImg = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean isShow) throws Throwable {
//            Tools.PrintControlsLocation("MainBottomChannelOthers", llBottomOtherChannels);
            int isVisible = imgLeft.getVisibility();
            if (isShow) {//显示
                if (isVisible == View.VISIBLE) return;
                imgLeft.setVisibility(View.VISIBLE);
                imgRight.setVisibility(View.VISIBLE);
            } else {//不显示
                if (isVisible != View.VISIBLE) return;
                imgLeft.setVisibility(View.INVISIBLE);
                imgRight.setVisibility(View.INVISIBLE);
            }
        }
    };

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            switch (workModeBean.getNextWorkMode()) {
                case IWorkMode.WorkMode_YT:
                case IWorkMode.WorkMode_YTZOOM:
                    llBottomOtherChannels.setVisibility(View.VISIBLE);
                    break;
                case IWorkMode.WorkMode_XY:
                    llBottomOtherChannels.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    private Consumer<Boolean> consumerSerialsWordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            isSerialsWordShow = aBoolean;
        }
    };

    //这个三个值用来处理  按键打开菜单，再按 关闭对应菜单。
    private boolean isMathOpen = false;
    private boolean isRefOpen = false;
    private boolean isSerialsOpen = false;
    private Consumer<Integer> consumerOpenAddChannelMenu = new Consumer<Integer>() {
        @Override
        public void accept(Integer tabIndex) {
            try {
                if (!mainViewGroup.isRightSlipOtherShow()) {
                    resetOpenState();//先重置，以防其他情况关闭菜单没更新状态
                }
                checkUsableAvailableChannel();//更新状态
                switch (tabIndex) {
                    case 0://Math
                        if (isMathOpen) {
                            mainViewGroup.hideAllSlip();
                            resetOpenState();
                            break;
                        }
                        if (checkAddMathAvailable(true)) {
                            consumerShowOtherChannel.accept(tabIndex);
                            String available = mathAvailable + CommandMsgToUI.PARAM_SPLIT + refAvailable + CommandMsgToUI.PARAM_SPLIT + serialsAvailable;
                            RxBus.getInstance().post(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_MATH, slideMathIndex + CommandMsgToUI.PARAM_SPLIT + available);
                        }
                        break;
                    case 1://Ref
                        if (isRefOpen) {
                            mainViewGroup.hideAllSlip();
                            resetOpenState();
                            break;
                        }
                        if (checkAddRefAvailable(true)) {
                            consumerShowOtherChannel.accept(tabIndex);
                            String available = mathAvailable + CommandMsgToUI.PARAM_SPLIT + refAvailable + CommandMsgToUI.PARAM_SPLIT + serialsAvailable;
                            RxBus.getInstance().post(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_REF, slideRefIndex + CommandMsgToUI.PARAM_SPLIT + available);
                        }
                        break;
                    case 2://Serials
                        if (isSerialsOpen) {
                            mainViewGroup.hideAllSlip();
                            resetOpenState();
                            break;
                        }
                        if (checkAddSerialAvailable(true)) {
                            consumerShowOtherChannel.accept(tabIndex);
                            String available = mathAvailable + CommandMsgToUI.PARAM_SPLIT + refAvailable + CommandMsgToUI.PARAM_SPLIT + serialsAvailable;
                            RxBus.getInstance().post(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_SERIALS, slideSerialsIndex + CommandMsgToUI.PARAM_SPLIT + available);
                        }
                        break;
                    default:
                        break;
                }
            } catch (Throwable e) {
                Logger.e("Open add channel menu failed with tabIndex=" + tabIndex);
            }
        }
    };

    //重置菜单那打开状态的记录值
    private void resetOpenState() {
        isMathOpen = false;
        isRefOpen = false;
        isSerialsOpen = false;
    }

    private void setNormalState() {
        Logger.d(TAG, "curMath=" + currentMathChan + " curRef=" + currentRefChan + " curSerials=" + currentSerialsChan);
        Logger.d(TAG, "mathAvailable = " + mathAvailable + " refAvailable = " + refAvailable + " serialsAvailable = " + serialsAvailable);
        String available = mathAvailable + CommandMsgToUI.PARAM_SPLIT + refAvailable + CommandMsgToUI.PARAM_SPLIT + serialsAvailable;

        RxBus.getInstance().post(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_MATH, slideMathIndex + CommandMsgToUI.PARAM_SPLIT + available);
        RxBus.getInstance().post(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_REF, slideRefIndex + CommandMsgToUI.PARAM_SPLIT + available);
        RxBus.getInstance().post(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_SERIALS, slideSerialsIndex + CommandMsgToUI.PARAM_SPLIT + available);
        if (isSerialsWordShow) {//文本页面 只显示Serials
            if (serialsAvailable) {
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slideSerialsIndex, true, false));
            } else {
                DToast.get().show(R.string.mathRefSerials_can_not_add);
            }
        } else {
            //显示优先级 Math > Ref > Serials
            if (mathAvailable) {
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slideMathIndex, true, false));
            } else {
                if (refAvailable) {
                    RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slideRefIndex, true, false));
                } else {
                    if (serialsAvailable) {
                        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slideSerialsIndex, true, false));
                    }
                }
            }
        }
    }


    private int getMaxRefChannelNumber() {
        final int[] maxRefChan = {TChan.R1 - TChan.Ch1};
        TChan.foreachRef(refChan -> {
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan);
            boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan);
//            Logger.d("limh", "refChan = " + refChan + " refCheck = " + refCheck + " refAddByUser = " + refAddByUser);
//            if (refCheck) {
            if (refAddByUser) {
                maxRefChan[0] = Math.max(maxRefChan[0], refChan);
            }
        });
        return maxRefChan[0];
    }

    private int getUsableRefChannelNumber() {
        final int[] minRefChan = {TChan.R8 + TChan.Ch1};
        TChan.foreachRef(refChan -> {
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan);
            boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan);
            if (!refAddByUser) {
                minRefChan[0] = Math.min(minRefChan[0], refChan);
            }
        });
        return minRefChan[0];
    }

    public int getMaxMathChannelNumber() {//获取可添加的最大的Math通道
        final int[] maxMathChan = {TChan.Math1 - TChan.Ch1};
        TChan.foreachMath(mathChan -> {
//            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan);
            boolean mathAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan);
//            Logger.d("limh", "mathChan = " + mathChan + " mathCheck = " + mathCheck + " mathAddByUser = " + mathAddByUser);
//            if (mathCheck) {
            if (mathAddByUser) {
                maxMathChan[0] = Math.max(maxMathChan[0], mathChan);
            }
        });
        return maxMathChan[0];
    }

    public int getUsableMathChannelNumber() { //获取可添加的最小Math通道
        final int[] minMathChan = {TChan.Math8 + TChan.Ch1};
        TChan.foreachMath(mathChan -> {
//            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan);
            boolean mathAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan);
            if (!mathAddByUser) {//还没添加过的
                minMathChan[0] = Math.min(minMathChan[0], mathChan);
            }
        });
        return minMathChan[0];
    }

    public int getMaxSerialChannelNumber() {
        final int[] maxSerialChan = {TChan.S1 - TChan.Ch1};
        TChan.foreachSerial(serialChan -> {
            boolean serialCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + TChan.toSerialNumber(serialChan));
            boolean serialsAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(serialChan));
//            Logger.d("limh", "serialChan = " + serialChan + " serialCheck = " + serialCheck + " serialsAddByUser = " + serialsAddByUser);
//            if (serialCheck) {
            if (serialsAddByUser) {
                maxSerialChan[0] = Math.max(maxSerialChan[0], serialChan);
            }
        });
        return maxSerialChan[0];
    }

    public int getUsableSerialChannelNumber() {
        final int[] minSerialChan = {TChan.S4 + TChan.Ch1};
        TChan.foreachSerial(serialChan -> {
            boolean serialCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + TChan.toSerialNumber(serialChan));
            boolean serialsAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(serialChan));
            if (!serialsAddByUser) {
                minSerialChan[0] = Math.min(minSerialChan[0], serialChan);
            }
        });
        return minSerialChan[0];
    }

    public int getCurrentRefChan() {
        return currentRefChan;
    }

}