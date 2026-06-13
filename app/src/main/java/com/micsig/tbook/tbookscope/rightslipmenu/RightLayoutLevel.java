package com.micsig.tbook.tbookscope.rightslipmenu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.micsig.base.Logger;
import com.micsig.base.widget.MyRadioGroup;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Trigger.TriggerCommon;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel;
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerChannel;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.util.TimerUtils;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


public class RightLayoutLevel extends RelativeLayout {
    private static final String TAG = RightLayoutLevel.class.getSimpleName();
    private Context context;

    private RadioGroup rgTopLayout;
    private LinearLayout llShowTopLayout;
    private RadioGroup rgMiddleLayout;
    private MyRadioGroup rgBottomLayout;
    private RadioButton rbTop1;
    private RadioButton rbTop2;
    private RadioButton rbTop3;
    private ImageView ivShowTop1;
    private ImageView ivShowTop2;
    private ImageView ivShowTop3;
    private RadioButton rbAuto;
    private RadioButton rbNormal;
    private RadioButton rbBottom1, rbBottom2, rbBottom3, rbBottom4, rbBottom5, rbBottom6, rbBottom7, rbBottom8, rbBottom9;

    private final List<RadioButton> rbBottoms = new ArrayList<>();
    private RightMsgLevel msgLevel;
    private OnButtonClickListener onButtonClickListener;
    private TimerUtils timer;
    private Drawable drawableTop, drawableMiddle, drawableBottom;

    private final int channelCount = GlobalVar.get().getChannelsCount();

    private final List<Integer> tChanChX = Arrays.asList(
            TChan.Ch1, TChan.Ch2, TChan.Ch3, TChan.Ch4,
            TChan.Ch5, TChan.Ch6, TChan.Ch7, TChan.Ch8
    );

    public interface OnButtonClickListener {
        void onClick(RadioGroup group, RadioButton radioButton);
    }

    public OnButtonClickListener getOnButtonClickListener() {
        return onButtonClickListener;
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public RightLayoutLevel(Context context) {
        this(context, null);
    }

    public RightLayoutLevel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutLevel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initDrawable();
        initView();
        initMsg();
        initControl();
    }

    private void initDrawable() {
        drawableTop = context.getResources().getDrawable(R.drawable.bg_right_level_click_top);
        drawableMiddle = context.getResources().getDrawable(R.drawable.bg_right_level_click_middle);
        drawableBottom = context.getResources().getDrawable(R.drawable.bg_right_level_click_bottom);
    }

    private void initView() {
        View.inflate(context, R.layout.layout_right_level, this);
        rgTopLayout = (RadioGroup) findViewById(R.id.rightLevelTopLayout);
        rgMiddleLayout = (RadioGroup) findViewById(R.id.rightLevelMiddleLayout);
        rgBottomLayout = (MyRadioGroup) findViewById(R.id.rightLevelBottomLayout);

        rbTop1 = (RadioButton) findViewById(R.id.rightLevelTop1);
        rbTop2 = (RadioButton) findViewById(R.id.rightLevelTop2);
        rbTop3 = (RadioButton) findViewById(R.id.rightLevelTop3);
        rbAuto = (RadioButton) findViewById(R.id.rightLevelAuto);
        rbNormal = (RadioButton) findViewById(R.id.rightLevelNormal);
        rbBottom1 = (RadioButton) findViewById(R.id.rightLevelBottom1);
        rbBottom2 = (RadioButton) findViewById(R.id.rightLevelBottom2);
        rbBottom3 = (RadioButton) findViewById(R.id.rightLevelBottom3);
        rbBottom4 = (RadioButton) findViewById(R.id.rightLevelBottom4);
        rbBottom5 = (RadioButton) findViewById(R.id.rightLevelBottom5);
        rbBottom6 = (RadioButton) findViewById(R.id.rightLevelBottom6);
        rbBottom7 = (RadioButton) findViewById(R.id.rightLevelBottom7);
        rbBottom8 = (RadioButton) findViewById(R.id.rightLevelBottom8);
        rbBottom9 = (RadioButton) findViewById(R.id.rightLevelBottom9);
        rbBottoms.add(rbBottom1);
        rbBottoms.add(rbBottom2);
        rbBottoms.add(rbBottom3);
        rbBottoms.add(rbBottom4);
        rbBottoms.add(rbBottom5);
        rbBottoms.add(rbBottom6);
        rbBottoms.add(rbBottom7);
        rbBottoms.add(rbBottom8);
        rbBottoms.add(rbBottom9);

        llShowTopLayout = (LinearLayout) findViewById(R.id.rightLevelShowTopLayout);
        ivShowTop1 = (ImageView) findViewById(R.id.rightLevelShowTop1);
        ivShowTop2 = (ImageView) findViewById(R.id.rightLevelShowTop2);
        ivShowTop3 = (ImageView) findViewById(R.id.rightLevelShowTop3);

        rgTopLayout.setOnCheckedChangeListener(onCheckedChangeListener);
        rgMiddleLayout.setOnCheckedChangeListener(onCheckedChangeListener);
        rgBottomLayout.setOnCheckedChangeListener(onCheckedChangeListener);
        rgBottomLayout.setChildBackGround(drawableTop, drawableMiddle, drawableBottom);
    }

    private void initMsg() {
        msgLevel = new RightMsgLevel();
        msgLevel.setTopCount(3);
        msgLevel.setMiddleCount(2);
//        msgLevel.setBottomCount(channelCount);
        msgLevel.setTopSelect(0);
        msgLevel.setMiddleSelect(0);
        msgLevel.setBottomSelect(0);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTopTrigger);
        RxBus.getInstance().getObservable(RxEnum.TOPTRIGGER_CHANNEL).subscribe(consumerTopTriggerChannel);
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_AUTO).subscribe(consumerMainLeftMenuAuto);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL).subscribe(consumerTriggerLevel);
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_MODE).subscribe(consumerModeChange);
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_COMMON_MODE, eventUIObserver);

        timer = new TimerUtils(timeOut);
        timer.setIntervalMs(4000);

    }

    private TimerUtils.TimeOutEvent timeOut = new TimerUtils.TimeOutEvent() {
        @Override
        public void onTimeOut() {
            RightLayoutLevel.this.post(new Runnable() {
                @Override
                public void run() {
                    RightLayoutLevel.this.setVisibility(GONE);
                }
            });

        }
    };

    private void setCache() {
        int triggerMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE);

        if (triggerMode == 0) {
            onlyCheck(rgMiddleLayout, rbAuto.getId());
        } else {
            onlyCheck(rgMiddleLayout, rbNormal.getId());
        }
        msgLevel.setMiddleSelect(triggerMode);
        setTriggerIndex(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER));
    }

    private void setBottomViewVisible(boolean isAllVisible, ArrayList<Boolean> chxVisible) {
        for (int i = 0; i < rbBottoms.size(); i++) {
            if (i < chxVisible.size() - 1) {
                rbBottoms.get(i).setEnabled(isAllVisible || chxVisible.get(i));//是否可点击
                rbBottoms.get(i).setText(TChan.getChannelName(i + 1));//名称
                boolean type = isAllVisible ? true : chxVisible.get(i);
                rbBottoms.get(i).setTextColor(type
                        ? TChan.getChannelColor(context, i + 1)
                        : context.getResources().getColor(R.color.main_text_color_disable));//颜色值
            }
            if (i == chxVisible.size() - 1) {//最后一个外部触发是否显示
                ((RadioButton) rbBottoms.get(i)).setVisibility(chxVisible.get(i) ? View.VISIBLE : View.GONE);
                rbBottoms.get(i).setText(context.getResources().getString(R.string.top_edge_external_trigger));//名称
                rbBottoms.get(i).setTextColor(context.getResources().getColor(R.color.colorChCommon));//颜色值
            }
        }
//        updateBackGround();
    }

    private void updateBackGround() {
        int maxRows = rgBottomLayout.getMaxRows();
        for (int i = 0; i < rbBottoms.size(); i++) {
            if (1 % maxRows == 0) {
                rbBottoms.get(i).setBackgroundDrawable(drawableTop);
            } else if (1 % maxRows == maxRows - 1 || i == rbBottoms.size() - 1) {
                rbBottoms.get(i).setBackgroundDrawable(drawableBottom);
            } else {
                rbBottoms.get(i).setBackgroundDrawable(drawableMiddle);
            }
        }
        rgBottomLayout.requestLayout();
    }

    private void setBottomTextAndColor() {
        for (int i = 0; i < rbBottoms.size(); i++) {
            rbBottoms.get(i).setText(TChan.getChannelName(i + 1));
            rbBottoms.get(i).setTextColor(TChan.getChannelColor(context, i + 1));
        }
    }

    private void setTriggerIndex(int triggerIndex) {
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE);
        onlyCheck(rgBottomLayout, rbBottoms.get(source).getId());
        msgLevel.setBottomSelect(source);
        int triggerMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE);
        if (triggerMode == 0) {
            onlyCheck(rgMiddleLayout, rbAuto.getId());
        } else {
            onlyCheck(rgMiddleLayout, rbNormal.getId());
        }
        msgLevel.setMiddleSelect(triggerMode);
        msgLevel.setTopCount(0);

        ArrayList<Boolean> chXVisible = new ArrayList<>();
        boolean triggerCh1Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH1) != 2;
        boolean triggerCh2Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH2) != 2;
        boolean triggerCh3Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH3) != 2;
        boolean triggerCh4Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH4) != 2;
        boolean triggerCh5Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH5) != 2;
        boolean triggerCh6Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH6) != 2;
        boolean triggerCh7Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH7) != 2;
        boolean triggerCh8Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH8) != 2;
        chXVisible.add(triggerCh1Visible);
        chXVisible.add(triggerCh2Visible);
        chXVisible.add(triggerCh3Visible);
        chXVisible.add(triggerCh4Visible);
        chXVisible.add(triggerCh5Visible);
        chXVisible.add(triggerCh6Visible);
        chXVisible.add(triggerCh7Visible);
        chXVisible.add(triggerCh8Visible);
        chXVisible.add(false);
        if(triggerIndex == TopLayoutTrigger.DETAIL_EDGE) {
            chXVisible.set(chXVisible.size() - 1, true);
        }
//        setBottomTextAndColor();
        setBottomViewVisible(triggerIndex != TopLayoutTrigger.DETAIL_LOGIC, chXVisible);
        //其他触发的时候，CH8是显示的最后一个，所以更新下CH8的背景
        rgBottomLayout.getChildAt(rgBottomLayout.getChildCount() - 2).setBackground(drawableBottom);
        switch (triggerIndex) {
//            case TopLayoutTrigger.DETAIL_COMMON:
//                rgTopLayout.setVisibility(GONE);
//                llShowTopLayout.setVisibility(GONE);
//
////                int triggerMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE);
////                if (triggerMode == 0) {
////                    onlyCheck(rgMiddleLayout, rbAuto.getId());
////                } else {
////                    onlyCheck(rgMiddleLayout, rbNormal.getId());
////                }
////                msgLevel.setTopCount(0);
//                break;
            case TopLayoutTrigger.DETAIL_EDGE:
                rgTopLayout.setVisibility(VISIBLE);
                rbTop1.setVisibility(VISIBLE);
                rbTop2.setVisibility(VISIBLE);
                rbTop3.setVisibility(VISIBLE);
                llShowTopLayout.setVisibility(VISIBLE);
                ivShowTop1.setVisibility(VISIBLE);
                ivShowTop2.setVisibility(VISIBLE);
                ivShowTop3.setVisibility(VISIBLE);
                ivShowTop1.setImageResource(R.drawable.bg_right_level_rising);
                ivShowTop2.setImageResource(R.drawable.bg_right_level_falling);
                ivShowTop3.setImageResource(R.drawable.bg_right_level_double);
                //N边沿的时候，CH8不是最后一个，所以更新下CH8的背景
                rgBottomLayout.getChildAt(rgBottomLayout.getChildCount() - 2).setBackground(drawableMiddle);

                int edge = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_EDGE);
                if (edge == 0) {
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else if (edge == 1) {
                    onlyCheck(rgTopLayout, rbTop2.getId());
                } else {
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(3);
                msgLevel.setTopSelect(edge);
                break;
            case TopLayoutTrigger.DETAIL_PULSEWIDTH:
                rgTopLayout.setVisibility(VISIBLE);
                rbTop1.setVisibility(VISIBLE);
                rbTop2.setVisibility(GONE);
                rbTop3.setVisibility(VISIBLE);
                llShowTopLayout.setVisibility(VISIBLE);
                ivShowTop1.setVisibility(VISIBLE);
                ivShowTop2.setVisibility(GONE);
                ivShowTop3.setVisibility(VISIBLE);
                ivShowTop1.setImageResource(R.drawable.bg_right_level_positive);
                ivShowTop3.setImageResource(R.drawable.bg_right_level_negative);

                int polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_POLAR);
                if (polar == 0) {
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else {
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(2);
                msgLevel.setTopSelect(polar);
                break;
            case TopLayoutTrigger.DETAIL_NEDGE:
                rgTopLayout.setVisibility(VISIBLE);
                rbTop1.setVisibility(VISIBLE);
                rbTop2.setVisibility(GONE);
                rbTop3.setVisibility(VISIBLE);
                llShowTopLayout.setVisibility(VISIBLE);
                ivShowTop1.setVisibility(VISIBLE);
                ivShowTop2.setVisibility(GONE);
                ivShowTop3.setVisibility(VISIBLE);
                ivShowTop1.setImageResource(R.drawable.bg_right_level_rising);
                ivShowTop3.setImageResource(R.drawable.bg_right_level_falling);

                int slope = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_SLOPE);
                if (slope == 0) {
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else {
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(2);
                msgLevel.setTopSelect(slope);
                break;
            case TopLayoutTrigger.DETAIL_RUNT:
                rgTopLayout.setVisibility(VISIBLE);
                rbTop1.setVisibility(VISIBLE);
                rbTop2.setVisibility(VISIBLE);
                rbTop3.setVisibility(VISIBLE);
                llShowTopLayout.setVisibility(VISIBLE);
                ivShowTop1.setVisibility(VISIBLE);
                ivShowTop2.setVisibility(VISIBLE);
                ivShowTop3.setVisibility(VISIBLE);
                ivShowTop1.setImageResource(R.drawable.bg_right_level_positive);
                ivShowTop2.setImageResource(R.drawable.bg_right_level_negative);
                ivShowTop3.setImageResource(R.drawable.bg_right_level_either);

                polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_RUNT_POLAR);
                if (polar == 0) {
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else if (polar == 1) {
                    onlyCheck(rgTopLayout, rbTop2.getId());
                } else {
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(3);
                msgLevel.setTopSelect(polar);
                break;
            case TopLayoutTrigger.DETAIL_SLOPE:
                rgTopLayout.setVisibility(VISIBLE);
                rbTop1.setVisibility(VISIBLE);
                rbTop2.setVisibility(VISIBLE);
                rbTop3.setVisibility(VISIBLE);
                llShowTopLayout.setVisibility(VISIBLE);
                ivShowTop1.setVisibility(VISIBLE);
                ivShowTop2.setVisibility(VISIBLE);
                ivShowTop3.setVisibility(VISIBLE);
                ivShowTop1.setImageResource(R.drawable.bg_right_level_rising);
                ivShowTop2.setImageResource(R.drawable.bg_right_level_falling);
                ivShowTop3.setImageResource(R.drawable.bg_right_level_double);

                edge = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_EDGE);
                if (edge == 0) {
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else if (edge == 1) {
                    onlyCheck(rgTopLayout, rbTop2.getId());
                } else {
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(3);
                msgLevel.setTopSelect(edge);
                break;
            case TopLayoutTrigger.DETAIL_TIMEOUT:
                rgTopLayout.setVisibility(VISIBLE);
                rbTop1.setVisibility(VISIBLE);
                rbTop2.setVisibility(VISIBLE);
                rbTop3.setVisibility(VISIBLE);
                llShowTopLayout.setVisibility(VISIBLE);
                ivShowTop1.setVisibility(VISIBLE);
                ivShowTop2.setVisibility(VISIBLE);
                ivShowTop3.setVisibility(VISIBLE);
                ivShowTop1.setImageResource(R.drawable.bg_right_level_positive);
                ivShowTop2.setImageResource(R.drawable.bg_right_level_negative);
                ivShowTop3.setImageResource(R.drawable.bg_right_level_either);

                polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_TIMEOUT_POLAR);
                if (polar == 0) {
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else if (polar == 1) {
                    onlyCheck(rgTopLayout, rbTop2.getId());
                } else {
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(3);
                msgLevel.setTopSelect(polar);
                break;
            case TopLayoutTrigger.DETAIL_VIDEO:
                rgTopLayout.setVisibility(VISIBLE);
                rbTop1.setVisibility(VISIBLE);
                rbTop2.setVisibility(GONE);
                rbTop3.setVisibility(VISIBLE);
                llShowTopLayout.setVisibility(VISIBLE);
                ivShowTop1.setVisibility(VISIBLE);
                ivShowTop2.setVisibility(GONE);
                ivShowTop3.setVisibility(VISIBLE);
                ivShowTop1.setImageResource(R.drawable.bg_right_level_positive);
                ivShowTop3.setImageResource(R.drawable.bg_right_level_negative);

                polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_POLAR);
                if (polar == 0) {
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else {
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(2);
                msgLevel.setTopSelect(polar);
                break;
            case TopLayoutTrigger.DETAIL_LOGIC:
            case TopLayoutTrigger.DETAIL_S1:
            case TopLayoutTrigger.DETAIL_S2:
            case TopLayoutTrigger.DETAIL_S3:
            case TopLayoutTrigger.DETAIL_S4:
                msgLevel.setTopCount(0);
                rgTopLayout.setVisibility(GONE);
                llShowTopLayout.setVisibility(GONE);
                break;
        }
        rgBottomLayout.setChildBackGround(drawableTop, drawableMiddle, drawableBottom);
    }

//    private int getChTextResId(int channel) {
//        if (channel == IWave.Ch1) {
//            return R.string.mainCenterRadioButtonCh1;
//        } else if (channel == IWave.Ch2) {
//            return R.string.mainCenterRadioButtonCh2;
//        } else if (channel == IWave.Ch3) {
//            return R.string.mainCenterRadioButtonCh3;
//        } else if (channel == IWave.Ch4) {
//            return R.string.mainCenterRadioButtonCh4;
//        } else {
//            return R.string.mainCenterRadioButtonCh1;
//        }
//    }

    private void onlyCheck(RadioGroup radioGroup, int radioButtonId) {
        radioGroup.setOnCheckedChangeListener(null);
        radioGroup.check(radioButtonId);
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    private int getBottomCheckIndex() {
        if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom1.getId()) {
            return 0;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom2.getId()) {
            return 1;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom3.getId()) {
            return 2;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom4.getId()) {
            return 3;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom5.getId()) {
            return 4;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom6.getId()) {
            return 5;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom7.getId()) {
            return 6;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom8.getId()) {
            return 7;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom9.getId()) {
            return 8;
        } else {
            return -1;
        }
    }

    private int getMiddleCheckIndex() {
        if (rgMiddleLayout.getCheckedRadioButtonId() == rbAuto.getId()) {
            return 0;
        } else {
            return 1;
        }
    }


    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            timer.start();
        }
        super.setVisibility(visibility);
    }

    private void sendMsg(boolean isFromEventBus) {
        msgLevel.setFromEventBus(isFromEventBus);
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_LEVEL, msgLevel);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private Consumer<TopMsgTrigger> consumerTopTrigger = new Consumer<TopMsgTrigger>() {
        @Override
        public void accept(@NonNull TopMsgTrigger topMsgTrigger) throws Exception {
            setTriggerIndex(topMsgTrigger.getTriggerTitle().getIndex());
        }
    };

    private Consumer<TopMsgTriggerChannel> consumerTopTriggerChannel = new Consumer<TopMsgTriggerChannel>() {
        @Override
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception {
            setTriggerIndex(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER));
            sendMsg(topMsgTriggerChannel.isFromEventBus());
        }
    };

    private Consumer<Boolean> consumerMainLeftMenuAuto = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) {
                if (getMiddleCheckIndex() != 0) {
                    onlyCheck(rgMiddleLayout, rbAuto.getId());
                    msgLevel.setMiddleSelect(0);
                    sendMsg(true);
                }
            }
        }
    };

    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() {
        @Override
        public void accept(MainMsgTriggerLevel msgTriggerLevel) throws Exception {
            if (msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_LOGIC)) {
                if (msgTriggerLevel.getCurCh() == getBottomCheckIndex()) {
                    return;
                }
                String strCh1 = getResources().getString(R.string.mainCenterRadioButtonCh1);
                String strCh2 = getResources().getString(R.string.mainCenterRadioButtonCh2);
                String strCh3 = getResources().getString(R.string.mainCenterRadioButtonCh3);
                String strCh4 = getResources().getString(R.string.mainCenterRadioButtonCh4);
                String strCh5 = getResources().getString(R.string.mainCenterRadioButtonCh5);
                String strCh6 = getResources().getString(R.string.mainCenterRadioButtonCh6);
                String strCh7 = getResources().getString(R.string.mainCenterRadioButtonCh7);
                String strCh8 = getResources().getString(R.string.mainCenterRadioButtonCh8);
                List<String> strChx = Arrays.asList(
                        strCh1, strCh2, strCh3, strCh4, strCh5, strCh6, strCh7, strCh8
                );
                for (int i = 0; i < strChx.size(); i++) {
                    if (msgTriggerLevel.getCurCh() == i + 1) {
                        if (strChx.get(i).equals(rbBottoms.get(i).getText().toString())) {
                            onlyCheck(rgBottomLayout, rbBottoms.get(i).getId());
                        }
                    }
                }
                msgLevel.setBottomSelect(msgTriggerLevel.getCurCh() - 1);
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            PlaySound.getInstance().playButton();
            timer.start();
            if (group.getId() == rgTopLayout.getId()) {
                if (checkedId == rbTop1.getId()) {
                    msgLevel.setTopSelect(0);
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbTop1);
                    }
                } else if (checkedId == rbTop2.getId()) {
                    msgLevel.setTopSelect(1);
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbTop2);
                    }
                } else if (checkedId == rbTop3.getId()) {
                    msgLevel.setTopSelect(2);
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbTop3);
                    }
                }
            } else if (group.getId() == rgMiddleLayout.getId()) {
                if (checkedId == rbAuto.getId()) {
                    msgLevel.setMiddleSelect(0);
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbAuto);
                    }
                } else if (checkedId == rbNormal.getId()) {
                    msgLevel.setMiddleSelect(1);
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbNormal);
                    }
                }
            } else if (group.getId() == rgBottomLayout.getId()) {
                if (checkedId == rbBottom1.getId()) {
                    msgLevel.setBottomSelect(getChIndex(rbBottom1));
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom1);
                    }
                } else if (checkedId == rbBottom2.getId()) {
                    msgLevel.setBottomSelect(getChIndex(rbBottom2));
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom2);
                    }
                } else if (checkedId == rbBottom3.getId()) {
                    msgLevel.setBottomSelect(getChIndex(rbBottom3));
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom3);
                    }
                } else if (checkedId == rbBottom4.getId()) {
                    msgLevel.setBottomSelect(getChIndex(rbBottom4));
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom4);
                    }
                } else if (checkedId == rbBottom5.getId()) {
                    msgLevel.setBottomSelect(getChIndex(rbBottom5));
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom5);
                    }
                } else if (checkedId == rbBottom6.getId()) {
                    msgLevel.setBottomSelect(getChIndex(rbBottom6));
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom6);
                    }
                } else if (checkedId == rbBottom7.getId()) {
                    msgLevel.setBottomSelect(getChIndex(rbBottom7));
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom7);
                    }
                } else if (checkedId == rbBottom8.getId()) {
                    msgLevel.setBottomSelect(getChIndex(rbBottom8));
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom8);
                    }
                } else if (checkedId == rbBottom9.getId()) {
                    msgLevel.setBottomSelect(getChIndex(rbBottom9));
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom9);
                    }
                }
            }
            sendMsg(false);
        }
    };

    private int getChIndex(RadioButton rb) {
        List<String> strChX = Arrays.asList(
                getResources().getString(R.string.mainCenterRadioButtonCh1), getResources().getString(R.string.mainCenterRadioButtonCh2),
                getResources().getString(R.string.mainCenterRadioButtonCh3), getResources().getString(R.string.mainCenterRadioButtonCh4),
                getResources().getString(R.string.mainCenterRadioButtonCh5), getResources().getString(R.string.mainCenterRadioButtonCh6),
                getResources().getString(R.string.mainCenterRadioButtonCh7), getResources().getString(R.string.mainCenterRadioButtonCh8)
        );
        int chIndex = 8;
        for (int i = 0; i < strChX.size(); i++) {
            if (strChX.get(i).equals(rb.getText().toString())) {
                chIndex = i;
            }
        }
        return chIndex;
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();
            if (((EventBase) data).getId() == EventFactory.EVENT_TRIGGER_COMMON_MODE) {
                boolean auto = rgMiddleLayout.getCheckedRadioButtonId() == rbAuto.getId();
                if (auto != (triggerCommon.getTriggerMode() == TriggerCommon.TM_AUTO)) {
                    if (triggerCommon.getTriggerMode() == TriggerCommon.TM_AUTO) {
                        onlyCheck(rgMiddleLayout, rbAuto.getId());
                        msgLevel.setMiddleSelect(0);
                    } else {
                        onlyCheck(rgMiddleLayout, rbNormal.getId());
                        msgLevel.setMiddleSelect(1);
                    }
                    sendMsg(true);
                }
            }
        }
    };

    private Consumer<Object> consumerModeChange = new Consumer<Object>() {
        @Override
        public void accept(Object object) throws Exception {
            boolean auto = rgMiddleLayout.getCheckedRadioButtonId() == rbAuto.getId();
            if (auto) {
                onlyCheck(rgMiddleLayout, rbNormal.getId());
                msgLevel.setMiddleSelect(1);
            } else {
                onlyCheck(rgMiddleLayout, rbAuto.getId());
                msgLevel.setMiddleSelect(0);
            }
            sendMsg(false);
        }
    };


}