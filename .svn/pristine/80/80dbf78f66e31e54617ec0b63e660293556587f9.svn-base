package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.math.MathWave;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.menu.SliderZone;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogSetChannelInfo;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

public class MainRightLayoutItemChannelMaster extends LinearLayout implements SliderZone.ISliderZone {
    private static final String TAG = MainRightLayoutItemChannelMaster.class.getSimpleName();
    private Context context;
    private boolean isNormalStyle = true;
    private boolean checked = false;
    private boolean large = false;
    private String text;
    private int chIndex;
    private boolean invert = false;
    private boolean disable = false;

    private ConstraintLayout itemMaster;
    private TextView tvChannelText;
    private ConstraintLayout dataLayout;
    private TextView tvTimeBase;
    private TextView tvTriggerTime;
    private LinearLayout llProbe;
    private TextView tvProbeTypeNum;
    private TextView tvProbeTypeUnit;
    private ImageView ivCouple;
    private TextView tvBandWidth;
    private TextView tvImpedance;
    private TextView tvProbe;
    private TextView tvLeftPosition;
    private ImageView ivBackground, ivSmallLight;
    private Button btnSmall;
    private OnRightMasterListener onRightMasterListener;
    private OnRightSmallListener onRightSmallListener;
    private SlidCloseChannelListener mSlideCloseListener;
    private boolean isSelect = false;
    private DialogSetChannelInfo dialogSetChannelInfo;

    //region interface
    private Rect availableSliderRect = null;

    @Override
    public Rect getAvailableSliderRect() {

        //10008只有一种状态，范围左右固定 left=1800 ,right=1920
        availableSliderRect = Tools.getViewRect(this);
//            availableSliderRect.left = GlobalVar.get().getMainWave().x;
        availableSliderRect.right = GlobalVar.get().getScreen().right;

        return availableSliderRect;
    }
    //endregion


    public interface OnRightMasterListener {
        void onTopClick(MainRightLayoutItemChannelMaster v);

        void onBottomClick(MainRightLayoutItemChannelMaster v);
    }

    public interface OnRightSmallListener {
        void onSmallClick(MainRightLayoutItemChannelMaster v);

        void onSmallDoubleClick(MainRightLayoutItemChannelMaster v);
    }

    public interface SlidCloseChannelListener {
        void onSlidCloseChannel(MainRightLayoutItemChannelMaster v);
    }

    public void setOnRightMasterListener(OnRightMasterListener onRightMasterListener) {
        this.onRightMasterListener = onRightMasterListener;
    }

    public void setOnRightSmallListener(OnRightSmallListener onRightSmallListener) {
        this.onRightSmallListener = onRightSmallListener;
    }

    public void setOnSlidCloseChannelListener(SlidCloseChannelListener mSlideCloseListener) {
        this.mSlideCloseListener = mSlideCloseListener;
    }

    public MainRightLayoutItemChannelMaster(Context context) {
        this(context, null);
    }

    public MainRightLayoutItemChannelMaster(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainRightLayoutItemChannelMaster(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private String[] probeList;
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        text = "1";
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainRightLayoutItemChannelMaster);
        checked = ta.getBoolean(R.styleable.MainRightLayoutItemChannelMaster_checked, false);
        large = ta.getBoolean(R.styleable.MainRightLayoutItemChannelMaster_large, false);
        text = ta.getString(R.styleable.MainRightLayoutItemChannelMaster_text);
        isNormalStyle = ta.getBoolean(R.styleable.MainRightLayoutItemChannelMaster_isNormal, true);
        ta.recycle();
        Logger.i("text= " + text);
        if (!isNormalStyle) {//底部Math/Ref对应的布局
//            if (text.contains("R")) {
                inflate(context, R.layout.layout_item_mainright_ref, this);
//            } else {
//                inflate(context, R.layout.layout_item_mainright_math_and_ref, this);
//            }
        } else {
            if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {//右侧CH 8通道对应布局
                inflate(context, R.layout.layout_item_mainright_channel, this);
            } else {
                inflate(context, R.layout.layout_mainright_channel_master, this);
            }
        }
        probeList = getResources().getStringArray(R.array.channelProbeTypeMultiple);
        initView();
        setState(checked, large);
    }

    private void initView() {
        itemMaster = findViewById(R.id.item_channel_master);
        tvChannelText = (TextView) findViewById(R.id.channelText);
        dataLayout = (ConstraintLayout) findViewById(R.id.dataLayout);
        tvTimeBase = findViewById(R.id.refTimebase);
        tvTriggerTime = findViewById(R.id.refTriggerTime);
        llProbe = findViewById(R.id.ll_probe);
        tvProbeTypeNum = (TextView) findViewById(R.id.probeTypeNum);
        tvProbeTypeUnit = (TextView) findViewById(R.id.probeTypeUnit);
        ivCouple = (ImageView) findViewById(R.id.rightCouple);
        tvBandWidth = (TextView) findViewById(R.id.rightBandWidth);
        tvImpedance = findViewById(R.id.impedance);
        tvProbe = findViewById(R.id.prob);
        tvLeftPosition = findViewById(R.id.leftPosition);
        ivBackground = findViewById(R.id.background);
        ivSmallLight = (ImageView) findViewById(R.id.smallLight);
        btnSmall = (Button) findViewById(R.id.btnSmall);

        btnSmall.setOnTouchListener(onTouchListener);

        dialogSetChannelInfo = (DialogSetChannelInfo) ((MainActivity) context).findViewById(R.id.dialogSetChannelInfo);
        tvChannelText.setText(text);
        chIndex = getChIndex();
        setContentColor(chIndex);
    }

    public void setContentColor(int chIndex) {

        int[] chColors = SvgNodeInfo.getColorsIntForView();
        int[] drawableResources = {
                R.drawable.ic_bg_ch1_light, R.drawable.ic_bg_ch2_light,
                R.drawable.ic_bg_ch3_light, R.drawable.ic_bg_ch4_light,
                R.drawable.ic_bg_ch5_light, R.drawable.ic_bg_ch6_light,
                R.drawable.ic_bg_ch7_light, R.drawable.ic_bg_ch8_light
        };

        switch (chIndex) {
            case ChannelFactory.CH1:
            case ChannelFactory.CH2:
            case ChannelFactory.CH3:
            case ChannelFactory.CH4:
            case ChannelFactory.CH5:
            case ChannelFactory.CH6:
            case ChannelFactory.CH7:
            case ChannelFactory.CH8:
                ivSmallLight.setImageResource(drawableResources[chIndex]);
                tvChannelText.setTextColor(chColors[chIndex]);
                tvProbeTypeNum.setTextColor(chColors[chIndex]);
                tvProbeTypeUnit.setTextColor(chColors[chIndex]);
                tvTimeBase.setTextColor(chColors[chIndex]);
                tvLeftPosition.setTextColor(chColors[chIndex]);
                tvTimeBase.setVisibility(View.GONE);
                tvTriggerTime.setVisibility(View.GONE);
                tvLeftPosition.setVisibility(View.VISIBLE);
                setOtherTextVisible(true);
                break;
            case ChannelFactory.MATH1:
            case ChannelFactory.MATH2:
            case ChannelFactory.MATH3:
            case ChannelFactory.MATH4:
            case ChannelFactory.MATH5:
            case ChannelFactory.MATH6:
            case ChannelFactory.MATH7:
            case ChannelFactory.MATH8:
                ivSmallLight.setImageResource(R.drawable.ic_bg_math_light);
                tvChannelText.setVisibility(VISIBLE);
                tvChannelText.setTextColor(chColors[chIndex]);
                tvProbeTypeNum.setTextColor(chColors[chIndex]);
                tvProbeTypeUnit.setTextColor(chColors[chIndex]);
                tvTimeBase.setTextColor(chColors[chIndex]);
                tvTriggerTime.setTextColor(chColors[chIndex]);
                tvLeftPosition.setTextColor(chColors[chIndex]);

                tvTimeBase.setVisibility(View.VISIBLE);
                tvTriggerTime.setVisibility(View.VISIBLE);
                tvLeftPosition.setVisibility(View.VISIBLE);
                setOtherTextVisible(false);
                break;
            case ChannelFactory.REF1:
            case ChannelFactory.REF2:
            case ChannelFactory.REF3:
            case ChannelFactory.REF4:
            case ChannelFactory.REF5:
            case ChannelFactory.REF6:
            case ChannelFactory.REF7:
            case ChannelFactory.REF8:
                ivSmallLight.setImageResource(R.drawable.ic_bg_ref_light);
                tvChannelText.setVisibility(VISIBLE);
                tvChannelText.setTextColor(chColors[chIndex]);
                tvProbeTypeNum.setTextColor(chColors[chIndex]);
                tvProbeTypeUnit.setTextColor(chColors[chIndex]);
                tvTimeBase.setTextColor(chColors[chIndex]);
                tvTriggerTime.setTextColor(chColors[chIndex]);
                tvLeftPosition.setTextColor(chColors[chIndex]);

                tvTimeBase.setVisibility(View.VISIBLE);
                tvTriggerTime.setVisibility(View.VISIBLE);
                tvLeftPosition.setVisibility(View.VISIBLE);
                setOtherTextVisible(false);
                break;
            default:
                tvChannelText.setTextColor(chColors[chIndex]);
                tvChannelText.setVisibility(VISIBLE);
                ivSmallLight.setImageResource(R.drawable.ic_bg_math_light);
                tvLeftPosition.setVisibility(View.GONE);
                setOtherTextVisible(false);
                break;
        }

        if (ChannelFactory.isRefCh(chIndex)) { //改变颜色之后的处理
            RefChannel refChannel = ChannelFactory.getRefChannel(chIndex);
            if (refChannel != null && refChannel.getRefType() == WaveData.FFT_WAVE) {
                tvTriggerTime.setVisibility(View.GONE);
            }
        }

        if (ChannelFactory.isMath_FFT_Ch(chIndex)) { //改变颜色之后的处理
            MathChannel mathChannel = ChannelFactory.getMathChannel(chIndex);
            if (mathChannel != null && mathChannel.getMathType() == MathWave.MATH_FFTWAVE) {
                tvTriggerTime.setVisibility(View.GONE);
            }
        }

    }


    private void setOtherTextVisible(boolean b) {
        tvBandWidth.setVisibility(b ? VISIBLE : GONE);
        tvImpedance.setVisibility(b ? VISIBLE : GONE);
        tvProbe.setVisibility(b ? VISIBLE : GONE);
//        tvLeftPosition.setVisibility(b ? VISIBLE : GONE);
        ivCouple.setVisibility(b ? VISIBLE : GONE);
        if (!b) {
            ivCouple.setImageResource(0);
        }
    }

    /**
     * 设置布局右对齐到边齐。
     * 解决：tvProbeTypeUnit在xml布局中，右边控件ivCouple动态隐藏后，不能对齐的问题。
     *
     * @param parent 父类
     * @param view   要操作的控件
     */
    private void setRightToRight(ConstraintLayout parent, View view) {
        ConstraintSet set = new ConstraintSet();
        set.clone(parent);
        set.connect(view.getId(), ConstraintSet.RIGHT, 0, ConstraintSet.LEFT, 0);
        set.connect(view.getId(), ConstraintSet.RIGHT, parent.getId(), ConstraintSet.RIGHT, 0);
        set.applyTo(parent);
    }
    private void setLeftRightCenter(ConstraintLayout parent, View leftView,View rightView){
        ConstraintSet set = new ConstraintSet();
        set.clone(parent);
        set.connect(leftView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
        set.connect(leftView.getId(), ConstraintSet.RIGHT, rightView.getId(), ConstraintSet.LEFT, 0);
        set.setHorizontalChainStyle(leftView.getId(),ConstraintSet.CHAIN_PACKED);

        set.connect(rightView.getId(), ConstraintSet.LEFT, leftView.getId(), ConstraintSet.RIGHT, 0);
        set.connect(rightView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);

        set.applyTo(parent);
    }


    private void setMarginTop(ConstraintLayout parent, View view) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        lp.setMargins(0, 50, 0, 0);
        view.setLayoutParams(lp);
    }

    public int getChIndex() {
        int chIndex;
        switch (tvChannelText.getText().toString()) {
            case "1": chIndex = ChannelFactory.CH1;break;
            case "2": chIndex = ChannelFactory.CH2;break;
            case "3": chIndex = ChannelFactory.CH3;break;
            case "4": chIndex = ChannelFactory.CH4;break;
            case "5": chIndex = ChannelFactory.CH5;break;
            case "6": chIndex = ChannelFactory.CH6;break;
            case "7": chIndex = ChannelFactory.CH7;break;
            case "8": chIndex = ChannelFactory.CH8;break;
            case "M1": chIndex = ChannelFactory.MATH1;break;
            case "M2": chIndex = ChannelFactory.MATH2;break;
            case "M3": chIndex = ChannelFactory.MATH3;break;
            case "M4": chIndex = ChannelFactory.MATH4;break;
            case "M5": chIndex = ChannelFactory.MATH5;break;
            case "M6": chIndex = ChannelFactory.MATH6;break;
            case "M7": chIndex = ChannelFactory.MATH7;break;
            case "M8": chIndex = ChannelFactory.MATH8;break;
            case "R1": chIndex = ChannelFactory.REF1;break;
            case "R2": chIndex = ChannelFactory.REF2;break;
            case "R3": chIndex = ChannelFactory.REF3;break;
            case "R4": chIndex = ChannelFactory.REF4;break;
            case "R5": chIndex = ChannelFactory.REF5;break;
            case "R6": chIndex = ChannelFactory.REF6;break;
            case "R7": chIndex = ChannelFactory.REF7;break;
            case "R8": chIndex = ChannelFactory.REF8;break;
            default: chIndex = ChannelFactory.CH1;break;
        }
        return chIndex;
    }

    private void setBgResId() {
//        ivLight.setVisibility(INVISIBLE);
        ivSmallLight.setVisibility(INVISIBLE);
//        ivLightInvert.setVisibility(INVISIBLE);
        setShowTextVisible(checked, large);
        if (checked) {
            switch (chIndex) {
                case ChannelFactory.CH1:
                case ChannelFactory.CH2:
                case ChannelFactory.CH3:
                case ChannelFactory.CH4:
                case ChannelFactory.CH5:
                case ChannelFactory.CH6:
                case ChannelFactory.CH7:
                case ChannelFactory.CH8:
                    if (invert) {
                        ivSmallLight.setVisibility(VISIBLE);
                    } else {
                        ivSmallLight.setVisibility(INVISIBLE);
                    }
                    break;
                case ChannelFactory.MATH1:
                case ChannelFactory.MATH2:
                case ChannelFactory.MATH3:
                case ChannelFactory.MATH4:
                case ChannelFactory.MATH5:
                case ChannelFactory.MATH6:
                case ChannelFactory.MATH7:
                case ChannelFactory.MATH8:
                case ChannelFactory.REF1:
                case ChannelFactory.REF2:
                case ChannelFactory.REF3:
                case ChannelFactory.REF4:
                case ChannelFactory.REF5:
                case ChannelFactory.REF6:
                case ChannelFactory.REF7:
                case ChannelFactory.REF8:
                default:
                    ivSmallLight.setVisibility(INVISIBLE);
                    break;
            }
        }
    }

    private void setShowTextVisible(boolean checked, boolean large) {
        if (disable) {
            return;
        }
        if (checked) {
            if (chIndex == ChannelFactory.MATH1 || chIndex == ChannelFactory.MATH2
                    || chIndex == ChannelFactory.MATH3 || chIndex == ChannelFactory.MATH4
                    || chIndex == ChannelFactory.MATH5 || chIndex == ChannelFactory.MATH6
                    || chIndex == ChannelFactory.MATH7 || chIndex == ChannelFactory.MATH8
                    || chIndex == ChannelFactory.REF1 || chIndex == ChannelFactory.REF2
                    || chIndex == ChannelFactory.REF3 || chIndex == ChannelFactory.REF4
                    || chIndex == ChannelFactory.REF5 || chIndex == ChannelFactory.REF6
                    || chIndex == ChannelFactory.REF7 || chIndex == ChannelFactory.REF8
            ) {
                tvChannelText.setVisibility(VISIBLE);
            } else {
                tvChannelText.setVisibility(GONE);
            }
            dataLayout.setVisibility(VISIBLE);
        } else {
            //通道 关闭的显示位置
            tvChannelText.setVisibility(VISIBLE);
            dataLayout.setVisibility(GONE);
        }

        if (!isNormalStyle) {
            if (checked) {
                tvChannelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18);
            } else {
                tvChannelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 26);
            }
        } else {
            tvChannelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 26);
        }

//        Rect probeTypeRect = Tools.getTextRect(tvProbeTypeNum.getText().toString() + tvProbeTypeUnit.getText().toString(), tvProbeTypeNum.getPaint());
//        int rightMargin = (layout.getMeasuredWidth() - probeTypeRect.width()) / 2;
        // 显示一堆的参数
//        if (checked && large) {
            // 选中且展开形式
//            if (chIndex == MATH || chIndex == REF) {
//                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//                layoutParams.setMargins(0, 85, 0, 0);
//                dataLayout.setLayoutParams(layoutParams);
//            } else {
//                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//                layoutParams.setMargins(0, 90, 0, 0);
//                dataLayout.setLayoutParams(layoutParams);
//            }
//        } else if (checked) {
//            if (chIndex == MATH || chIndex == REF) {
//                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//                layoutParams.setMargins(0, 85, 0, 0);
//                dataLayout.setLayoutParams(layoutParams);
//            } else {
//                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//                layoutParams.setMargins(0, 90, 0, 0);
//                dataLayout.setLayoutParams(layoutParams);
//            }
//        } else {
//            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//            layoutParams.setMargins(0, 90, 0, 0);
//            dataLayout.setLayoutParams(layoutParams);
//        }
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }


    public void setState(boolean checked, boolean large) {
        if (disable) {
            return;
        }
        this.checked = checked;
        this.large = large;
        btnSmall.setVisibility(VISIBLE);
        btnSmall.setClickable(true);
        setBgResId();
//        updateBackground(large);
    }

    public void updateBackground(boolean isSelect) {
        this.isSelect = isSelect;
//        new RuntimeException("limh item" + getChIndex() + " isSelect=" + isSelect).printStackTrace();
        ivBackground.setVisibility(View.VISIBLE);
        if (isSelect) {
            if (TChan.isChan(TChan.toUiChNo(chIndex))) {
                ivBackground.setImageResource(R.drawable.svg_right_ch1234_select);
            } else if (TChan.isRef(TChan.toUiChNo(chIndex)) || TChan.isMath(TChan.toUiChNo(chIndex))) {
                ivBackground.setVisibility(View.GONE);
                itemMaster.setBackground(context.getResources().getDrawable(R.drawable.layer_button_select));
            } else {
                ivBackground.setImageResource(R.drawable.layer_button_select);
            }
        } else {
            if (TChan.isChan(TChan.toUiChNo(chIndex))) {
                ivBackground.setImageResource(R.drawable.svg_right_ch1234_close);
            }  else if (TChan.isRef(TChan.toUiChNo(chIndex)) || TChan.isMath(TChan.toUiChNo(chIndex))) {
                ivBackground.setVisibility(View.GONE);
                itemMaster.setBackground(context.getResources().getDrawable(R.drawable.layer_button_disable));
            } else {
                ivBackground.setImageResource(R.drawable.layer_button_disable);
            }
        }
    }

    public boolean isSelect() {
        return isSelect;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        if (disable) {
            return;
        }
        setState(checked, true);
    }

    public boolean isLarge() {
        return large;
    }

    public void setLarge(boolean large) {
        if (disable) {
            return;
        }
        setState(checked, large);
    }

    public void setVScale(double vScale) {
        if (disable) {
            return;
        }
        Channel channel = ChannelFactory.getDynamicChannel(Integer.parseInt(text.replace("CH", "")) - 1);
        channel.setVScaleVal(vScale);
        String probeEvery = TBookUtil.getMFromDouble(channel.getVScaleVal());
        setProbeTypeNum(probeEvery);
    }

    public void setProbeTypeNum(String probeTypeNum) {
        Logger.d("index= " + getChIndex() + " TypeNum= " + probeTypeNum);
        if (disable) {
            return;
        }
        if (StrUtil.isEmpty(probeTypeNum)) {
            tvProbeTypeNum.setVisibility(GONE);
            tvProbeTypeUnit.setVisibility(GONE);
        } else {
            tvProbeTypeNum.setVisibility(VISIBLE);
            tvProbeTypeUnit.setVisibility(VISIBLE);
        }
        tvProbeTypeNum.setText(probeTypeNum);
        changeProbeTypeTextSize();
    }

    public void setProbeTypeUnit(String probeTypeUnit) {
        Logger.d("index= " + getChIndex() + " TypeUnit= " + probeTypeUnit);
        if (disable) {
            return;
        }
        tvProbeTypeUnit.setText(probeTypeUnit);
        changeProbeTypeTextSize();
    }

    //Math/Ref触发位置
    public void setTriggerTime(String triggerTime) {
        Logger.d(TAG, "index= " + getChIndex() + " triggerTime= " + triggerTime);
        if (disable) return;
        if (StrUtil.isEmpty(triggerTime)) {
            tvTriggerTime.setVisibility(View.GONE);
            return;
        }
        tvTriggerTime.setText(triggerTime);
        tvTriggerTime.setVisibility(View.VISIBLE);
    }

    //Math/Ref显示时基
    public void setTimeBase(String timeBase) {
        Logger.d(TAG, "index= " + getChIndex() + " TimeBase= " + timeBase);
        if (disable) return;
        if (StrUtil.isEmpty(timeBase)) {
            tvTimeBase.setVisibility(View.GONE);
            return;
        }
        tvTimeBase.setText(timeBase);
        tvTimeBase.setVisibility(View.VISIBLE);
    }

    public String getProbeType() {
        return tvProbeTypeNum.getText().toString() + tvProbeTypeUnit.getText().toString();
    }

    private void changeProbeTypeTextSize() {
        if (disable) {
            return;
        }
        String probeType = getProbeType();
        tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
        tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
        Rect typeNumRect = Tools.getTextRect(probeType, tvProbeTypeNum.getPaint());
        Rect typeUnitRect = Tools.getTextRect(probeType, tvProbeTypeUnit.getPaint());
        if (!isNormalStyle) {
            if (typeNumRect.width() + typeUnitRect.width() > 120) {
                tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
                tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
                tvTimeBase.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
            } else {
                tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
                tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
                tvTimeBase.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
            }
        } else {
            if (typeNumRect.width() > 100) {
                tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
                tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
            } else {
                tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24);
                tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24);
            }
        }
        setShowTextVisible(checked, large);
    }

    public void setCoupleResId(int resId) {
        if (disable) {
            return;
        }
        ivCouple.setImageResource(resId);
    }

    public void setBandWidth(String bandWidth) {
        if (disable) {
            return;
        }
        tvBandWidth.setText(bandWidth);
    }

    public void setTvImpedance(String impedance) {
        if (disable) {
            return;
        }
        tvImpedance.setText(impedance);
    }

    public void setProbeMultiple(String probeMultiple) {
//        if (probeMultiple.contains("500mX")) {
//            probeMultiple = probeMultiple.replace("500mX", "0.5X");
//        } else if (probeMultiple.contains("200mX")) {
//            probeMultiple = probeMultiple.replace("200mX", "0.2X");
//        } else if (probeMultiple.contains("100mX")) {
//            probeMultiple = probeMultiple.replace("100mX", "0.1X");
//        }
//        Log.d(Command.TAG, String.format("setProbeMultiple: %s ,list:%s",probeMultiple , Arrays.toString(probeList)));
        int index= Tools.indexOf(probeList,s->s.equals(probeMultiple));
        if (index>0){
            //如果是标准的，就选择标准的
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.toUiChNo(chIndex), "");
        }
        tvProbe.setText(probeMultiple);
    }

    public void setLeftPosition(String leftPosition) {
        Logger.d("channelIndex= " + getChIndex() + " leftPosition= " + leftPosition);
        tvLeftPosition.setText(leftPosition);
        RxBus.getInstance().post(RxEnum.MSG_CHANNEL_SLIP_POSITION,leftPosition +";" + chIndex);
    }

    public void setLeftPositionVisible(boolean visible) {
        tvLeftPosition.setVisibility(visible ? VISIBLE : INVISIBLE);
    }

    public void setInvert(boolean invert) {
        if (disable) {
            return;
        }
        this.invert = invert;
        setBgResId();
    }

    public void onBtnTopClick() {
        if (disable) {
            return;
        }
        if (handler.hasMessages(HANDLE_MSG)) {
            handler.removeMessages(HANDLE_MSG);
        }
        handler.sendEmptyMessageDelayed(HANDLE_MSG, 500);//改背景
        if (onRightMasterListener != null) {
            onRightMasterListener.onTopClick(MainRightLayoutItemChannelMaster.this);
        }
    }

    public void onBtnBottomClick() {
        if (disable) {
            return;
        }
        if (handler.hasMessages(HANDLE_MSG)) {
            handler.removeMessages(HANDLE_MSG);
        }
        handler.sendEmptyMessageDelayed(HANDLE_MSG, 500);
        if (onRightMasterListener != null) {
            onRightMasterListener.onBottomClick(MainRightLayoutItemChannelMaster.this);
        }
    }

    public void onSmallClick() {
        if (disable) {
            return;
        }
        if (onRightSmallListener != null) {
            onRightSmallListener.onSmallClick(MainRightLayoutItemChannelMaster.this);
        }
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        private View curView;
        private int oldX, oldY;
        private int clickCount = 0;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean isFromMouse = motionEvent.isFromSource(InputDevice.SOURCE_MOUSE);//判断是否来自鼠标点击
//            Log.d(Tag.Debug, String.format("MainRightLayoutItemChannelMaster.onTouch: %b",disable ));
            if (disable) return false;
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    this.oldX = (int) motionEvent.getX();
                    this.oldY = (int) motionEvent.getY();
                    this.curView = view;
                    break;
                case MotionEvent.ACTION_UP:
                    float offsetX = (float) (motionEvent.getX() - oldX);
                    float offsetY = (float) (motionEvent.getY() - oldY);
                    int i = getChIndex();
                    if (TChan.isChan(TChan.toUiChNo(i))) {
                        offsetY = Math.abs(offsetY);
                        if (offsetX > 10 && (offsetY <= 5 || offsetX / offsetY >= 1.0f)) {
                            CloseChannel(view);
                            break;
                        }
                    } else {
                        if (offsetY > 10 && (offsetX <= 5 || offsetY / offsetX >= 1.0f)) {
//                            CloseChannel(view);//关闭通道
                            RxBus.getInstance().post(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL, TChan.toUiChNo(i));//删除通道
                            break;
                        }
                    }
                    clickCount++;
                    int timeout = 300;
                    Rect rect = Tools.getViewRect(view);
                    if (curView != null && curView == view && (Math.abs(offsetX) < 10 && Math.abs(offsetY) < 10) /*rect.contains((int)motionEvent.getRawX(), (int) motionEvent.getRawY())*/) {
                        OnMouseUp(view);
                        handler.postDelayed(() -> {
                            if (clickCount == 1) {
                            } else if (clickCount > 1) {
                                Point point = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
                                onDoubleMouseUp(view, isFromMouse, point);
                            }
                            handler.removeCallbacksAndMessages(null);//清空handler延时
                            clickCount = 0;//计数清零
                        }, timeout);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }

            return false;
        }

        private void CloseChannel(View view) {
            //Log.d(Tag.Debug, String.format("MainRightLayoutItemChannelMaster.CloseChannel: %d",getChIndex()));
            if (disable) {
                return;
            }
            PlaySound.getInstance().playSlide();
            if (mSlideCloseListener != null) {
                mSlideCloseListener.onSlidCloseChannel(MainRightLayoutItemChannelMaster.this);
            }
            int i = getChIndex();
            Command.get().getChannel().Display(i, false, true);
        }

//        private void OnMouseDown(View v) {
//            if (v.getId() == btnTop.getId()) {
//                if (invert) {
//                    ivBackground.setImageResource(R.drawable.svg_right_channel_up);
//                } else {
//                    ivBackground.setImageResource(R.drawable.svg_right_channel_up);
//                }
//            } else if (v.getId() == btnBottom.getId()) {
//                if (invert) {
//                    ivBackground.setImageResource(R.drawable.svg_right_channel_down);
//                } else {
//                    ivBackground.setImageResource(R.drawable.svg_right_channel_down);
//                }
//            } else if (v.getId() == btnSmall.getId()) {
//
//            }
//
//        }

//        private void OnMouseCancel(View v){
//            if (v.getId()==btnTop.getId() || v.getId()==btnBottom.getId()) {
//                if (large) {
//                    if (invert) {
//                        ivBackground.setImageResource(R.drawable.svg_right_channel);
//                    } else {
//                        ivBackground.setImageResource(R.drawable.svg_right_channel);
//                    }
//                    ivSmallLight.setVisibility(INVISIBLE);
//                } else {
//                    ivBackground.setImageResource(R.drawable.svg_right_ch1234_close);
//                    if (invert) {
//                        ivSmallLight.setVisibility(VISIBLE);
//                    } else {
//                        ivSmallLight.setVisibility(INVISIBLE);
//                    }
//                }
//            }
//        }

        private void OnMouseUp(View v) {
//            DToast.get().show("单击");
            if (disable) {
                return;
            }
            if (v.getId() == btnSmall.getId()) {
                if (onRightSmallListener != null) {
                    onRightSmallListener.onSmallClick(MainRightLayoutItemChannelMaster.this);
                }
            }
        }

        private void onDoubleMouseUp(View v, boolean isFromMouse, Point point) {
//            DToast.get().show("双击");
            if (disable) {
                return;
            }
            if (v.getId() == btnSmall.getId()) {
                if (onRightSmallListener == null) return;
                if (isFromMouse) {
                    checkClickPosition(point);
                } else {
                    openSlideZone();
                }
            }
        }
    };


    private void openSlideZone() {
        //关闭垂直调节按钮
        RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true);
        onRightSmallListener.onSmallDoubleClick(MainRightLayoutItemChannelMaster.this);
    }

    private Rect expandPosition(Rect rect) {
        rect.left = rect.left - 10;
        rect.top = rect.top - 3;
        rect.right = rect.right + 10;
        rect.bottom = rect.bottom + 3;
        return rect;
    }


    private void checkClickPosition(Point point) {
        Rect llProbeRect = Screen.getViewLocation(llProbe);
        Rect leftPositionRect = Screen.getViewLocation(tvLeftPosition);
        Rect refTimeBaseRect = Screen.getViewLocation(tvTimeBase);
        Rect refTriggerTimeRect = Screen.getViewLocation(tvTriggerTime);
        String clickInfo = "";
        if (llProbeRect.contains(point.x, point.y)) {//垂直挡位
            clickInfo = getChIndex() + ";" + 0;
        }
        if (leftPositionRect.contains(point.x, point.y)) {//垂直位置
            clickInfo = getChIndex() + ";" + 1;
        }
        if (refTimeBaseRect.contains(point.x, point.y)) {//水平档位
            clickInfo = getChIndex() + ";" + 2;
        }
        if (refTriggerTimeRect.contains(point.x, point.y)) {//水平位置
            clickInfo = getChIndex() + ";" + 3;
        }
        Logger.i(TAG, "MouseDoubleClickInfo= " + clickInfo);

        if (!clickInfo.isEmpty() && !WorkModeManage.getInstance().isXyMode()) {
            RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true);
            RxBus.getInstance().post(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION, clickInfo);
//            if (dialogSetChannelInfo == null) {//这是弹窗中统一处理的方案
//                dialogSetChannelInfo = (DialogSetChannelInfo) ((MainActivity) context).findViewById(R.id.dialogSetChannelInfo);
//            }
//            dialogSetChannelInfo.setData(getChIndex(), new DialogSetChannelInfo.OnDismissListener() {
//                @Override
//                public void onDismiss(int chIdx, HashMap<Integer, String> infoMap) {
//                }
//            });
        } else {
          openSlideZone();
        }
    }


//    private OnClickListener onClickListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (disable) {
//                return;
//            }
//            if (v.getId() == btnSmall.getId()) {
//                onSmallClick();
//            }
//        }
//    };

    private static final int HANDLE_MSG = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLE_MSG:
                    setBgResId();
                    break;
            }
        }
    };

}
