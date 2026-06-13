package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.MButton;
import com.micsig.tbook.ui.MButton_CheckBox_ThreeClick;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * Created by yangj on 2017/5/11.
 */

public class MainRightLayoutItemOthers extends AbsoluteLayout {
    private Context context;
    private MButton_CheckBox_ThreeClick tvNameUnCheck;
    private LinearLayout leftLayout;
    private AbsoluteLayout mathRefRightLayout;
    private LinearLayout serialsRightLayout;
    private TextView tvNameCheck, tvScale, tvTimebase;
    private MButton btnMV, btnV;
    private TextView tv1, tv2, tv3, tv4;

    private int serialsType = RightLayoutSerials.SERIALS_UART;
    private int text1Ch = 1;//1-4
    private int text2Ch = 1;
    private int text3Ch = 1;
    private boolean isSerials;
    private boolean checked;
    private boolean rightEnabled = true;
    private String strNameUnCheck, strNameCheck;
    private String strTimeBase;
    private SpannableStringBuilder strSerialsMsg;
    private OnButtonClickListener onButtonClickListener;

    /**
     * 是否来自外部按键
     */
    private boolean isFromExternalKey = false;

    public interface OnButtonClickListener {
        /**
         * @param before 此check是否是此次操作改变之前的状态，
         *               即如果为true则此check代表此次操作还未执行切换，需要在该接口实现里进行切换状态
         * @return check的此次操作之后的最终状态
         */
        boolean onNameClick(MainRightLayoutItemOthers layout, boolean checked, boolean before);

        void onMVClick(MainRightLayoutItemOthers layout);

        void onVClick(MainRightLayoutItemOthers layout);

        void onSerialsClick(MainRightLayoutItemOthers layout);

        /**
         * 当当前控件enable属性为false时，点击控件所使用的回调
         */
        void onEnableFalseClick(MainRightLayoutItemOthers layout);
    }

    public MainRightLayoutItemOthers(Context context) {
        this(context, null);
    }

    public MainRightLayoutItemOthers(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainRightLayoutItemOthers(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_mainright, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainLayoutRight);
        checked = ta.getBoolean(R.styleable.MainLayoutRight_checked, false);
        isSerials = ta.getBoolean(R.styleable.MainLayoutRight_isSerials, false);
        strNameUnCheck = ta.getString(R.styleable.MainLayoutRight_text);
        if (isSerials) {
            strNameCheck = strNameUnCheck + "\nUART";
        } else {
            strNameCheck = strNameUnCheck;
        }
        int bgLeftChecked = ta.getResourceId(R.styleable.MainLayoutRight_bgLeftChecked, R.drawable.l_math);
        int bgLeftUnChecked = ta.getResourceId(R.styleable.MainLayoutRight_bgLeftUnChecked, R.drawable.l_unclick);
        int bgRightTopTouchDown = ta.getResourceId(R.styleable.MainLayoutRight_bgRightTopTouchDown, R.drawable.math_mv_1);
        int bgRightTopTouchUp = ta.getResourceId(R.styleable.MainLayoutRight_bgRightTopTouchUp, R.drawable.math_mv);
        int bgRightBottomTouchDown = ta.getResourceId(R.styleable.MainLayoutRight_bgRightBottomTouchDown, R.drawable.math_v_1);
        int bgRightBottomTouchUp = ta.getResourceId(R.styleable.MainLayoutRight_bgRightBottomTouchUp, R.drawable.math_v);
        int text_x = ta.getDimensionPixelSize(R.styleable.MainLayoutRight_text_x, 5);
        ta.recycle();

        tvNameUnCheck = (MButton_CheckBox_ThreeClick) findViewById(R.id.nameUnCheck);
        leftLayout = (LinearLayout) findViewById(R.id.leftLayout);
        mathRefRightLayout = (AbsoluteLayout) findViewById(R.id.mathRefRightLayout);
        serialsRightLayout = (LinearLayout) findViewById(R.id.serialsRightLayout);
        tvNameCheck = (TextView) findViewById(R.id.nameCheck);
        tvScale = (TextView) findViewById(R.id.vertical);
        tvTimebase = (TextView) findViewById(R.id.timebase);
        btnMV = (MButton) findViewById(R.id.btnmV);
        btnV = (MButton) findViewById(R.id.btnV);
        tv1 = (TextView) findViewById(R.id.text1);
        tv2 = (TextView) findViewById(R.id.text2);
        tv3 = (TextView) findViewById(R.id.text3);
        tv4 = (TextView) findViewById(R.id.text4);
        tv1.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
        tv2.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
        tv3.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
        tv4.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);

//        tvNameUnCheck.setText_x(text_x);
        tvNameUnCheck.setCheckBitmap(BitmapFactory.decodeResource(getResources(), bgLeftChecked));
        tvNameUnCheck.setUnCheckBitmap(BitmapFactory.decodeResource(getResources(), bgLeftUnChecked));
        btnMV.setTouchDownBitmap(BitmapFactory.decodeResource(getResources(), bgRightTopTouchDown));
        btnMV.setTouchUpBitmap(BitmapFactory.decodeResource(getResources(), bgRightTopTouchUp));
        btnV.setTouchDownBitmap(BitmapFactory.decodeResource(getResources(), bgRightBottomTouchDown));
        btnV.setTouchUpBitmap(BitmapFactory.decodeResource(getResources(), bgRightBottomTouchUp));
        if (!StrUtil.isEmpty(strNameUnCheck)) {
            if (strNameUnCheck.contains("S1")) {
                serialsRightLayout.setBackgroundColor(getResources().getColor(R.color.color_S1));
            } else if (strNameUnCheck.contains("S2")) {
                serialsRightLayout.setBackgroundColor(getResources().getColor(R.color.color_S2));
            }
        }

        setChecked();
        tvNameUnCheck.setOnThreeClickListener(onThreeClickListener);
        btnMV.setOnClickListener(onCheckedChangeListener);
        btnV.setOnClickListener(onCheckedChangeListener);
        serialsRightLayout.setOnClickListener(onCheckedChangeListener);
    }

    public boolean isFromExternalKey() {
        return isFromExternalKey;
    }

    public void setFromExternalKey(boolean fromExternalKey) {
        isFromExternalKey = fromExternalKey;
    }

    private MButton_CheckBox_ThreeClick.OnThreeClickListener onThreeClickListener = new MButton_CheckBox_ThreeClick.OnThreeClickListener() {
        @Override
        public boolean onThreeClick(boolean check) {
            return MainRightLayoutItemOthers.this.onThreeClick(check);
        }
    };

    public boolean onThreeClick(boolean check) {
        if (!isEnabled()) {
            if (onButtonClickListener != null) {
                onButtonClickListener.onEnableFalseClick(MainRightLayoutItemOthers.this);
            }
            return false;
        }
        if (onButtonClickListener != null) {
            check = onButtonClickListener.onNameClick(MainRightLayoutItemOthers.this, check, true);
        } else {
            check = !check;
        }
        setChecked(check);
        return check;
    }

    private View.OnClickListener onCheckedChangeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isEnabled() || !rightEnabled) return;
            if (v.getId() == btnMV.getId()) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onMVClick(MainRightLayoutItemOthers.this);
                }
            } else if (v.getId() == btnV.getId()) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onVClick(MainRightLayoutItemOthers.this);
                }
            } else if (v.getId() == serialsRightLayout.getId()) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onSerialsClick(MainRightLayoutItemOthers.this);
                }
            }
        }
    };

    public void setRightEnabled(boolean enabled) {
        this.rightEnabled = enabled;
    }

    public void setTvScale(String scale) {
        Paint paint = new Paint();
        paint.setTextSize(14);
        Rect textRect = Tools.getTextRect(scale, paint);
        if (textRect.width() >= 59) {
            tvScale.setTextSize(TypedValue.COMPLEX_UNIT_PX, 11);
        } else if (textRect.width() >= 48) {
            tvScale.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12);
        } else {
            tvScale.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
        }
        tvScale.setText(scale);
    }

    public void setTvTimebase(String timeBase) {
        tvTimebase.setText(timeBase);
    }

    public String getTvScale() {
        return tvScale.getText().toString();
    }

    private void setChecked() {
        if (checked) {
            tvNameUnCheck.setText("");
            tvNameUnCheck.setChecked(true);
            leftLayout.setVisibility(VISIBLE);
            tvNameCheck.setText(strNameCheck);
            if (isSerials) {
                tvScale.setVisibility(GONE);
                tvTimebase.setVisibility(GONE);
                serialsRightLayout.setVisibility(VISIBLE);
                mathRefRightLayout.setVisibility(GONE);
            } else {
                tvScale.setVisibility(VISIBLE);
                tvTimebase.setVisibility(VISIBLE);
                mathRefRightLayout.setVisibility(VISIBLE);
                serialsRightLayout.setVisibility(GONE);
            }
        } else {
            tvNameUnCheck.setText(strNameUnCheck);
            tvNameUnCheck.setChecked(false);
            leftLayout.setVisibility(GONE);
            serialsRightLayout.setVisibility(GONE);
            mathRefRightLayout.setVisibility(GONE);
        }
    }


    /**
     * 设置阈值电平值的显示，只有当当前显示的channel与当前改变的channel相同时，才会修改
     *
     * @param curCh     本次修改的channel
     * @param levelType 本次修改channel的px值
     */
    public void setCommonValueLevel(int curCh, int levelType, int levelMode) {
        String val = Tools.getChannelLevel(curCh, levelType, levelMode);
        switch (serialsType) {
            case RightLayoutSerials.SERIALS_UART:
            case RightLayoutSerials.SERIALS_LIN:
            case RightLayoutSerials.SERIALS_CAN:
            case RightLayoutSerials.SERIALS_M1553B:
                if (text1Ch == curCh) {
                    setSerialsTextLine1(val);
                }
                break;
            case RightLayoutSerials.SERIALS_SPI:
                if (text1Ch == curCh) {
                    setSerialsTextLine1(val);
                } else if (text2Ch == curCh) {
                    setSerialsTextLine2(val);
                } else if (text3Ch == curCh && tv3.getVisibility() == VISIBLE) {
                    setSerialsTextLine3(val);
                }
                break;
            case RightLayoutSerials.SERIALS_I2C:
                if (text1Ch == curCh) {
                    setSerialsTextLine1(val);
                } else if (text2Ch == curCh) {
                    setSerialsTextLine2(val);
                }
                break;
            case RightLayoutSerials.SERIALS_M429:
                if (text1Ch == curCh) {
//                    Logger.i("setCommonValueLevel:" + curCh +"\t" + levelType + "\t" + val);
//                    try {
//                        throw new IllegalAccessException();
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
                    if (levelType == Tools.LevelType_High) {
                        setSerialsTextLine1(val);
                    } else {
                        setSerialsTextLine2(val);
                    }
                }
                break;
        }
    }

    /**
     * @return 结果为：1u、1m、1等不带单位的数据
     */
//    public String addRefVScale() {
//        RefChannel channel = ChannelFactory.getRefChannel(ChannelFactory.getInstance().getTopRefChannel().getChId());
//        channel.setVScaleId(channel.getVScaleId() - 1);
//        double vScale = channel.getVScaleIdVal();
//        int iUnit = channel.getProbeType();
//        setTvScale(TBookUtil.getMFromDouble(vScale) + ChannelFactory.getProbeString(iUnit));
//        return tvScale.getText().toString();
//    }
//
//    /**
//     * @return 结果为：1u、1m、1等不带单位的数据
//     */
//    public String subRefVScale() {
//        RefChannel channel = ChannelFactory.getRefChannel(ChannelFactory.getInstance().getTopRefChannel().getChId());
//        channel.setVScaleId(channel.getVScaleId() + 1);
//        double vScale = channel.getVScaleIdVal();
//        int iUnit = channel.getProbeType();
//        setTvScale(TBookUtil.getMFromDouble(vScale) + ChannelFactory.getProbeString(iUnit));
//        return tvScale.getText().toString();
//    }

    /**
     * 设置当前的总线类型
     */
    public void setSerialsType(int serialsType) {
        this.serialsType = serialsType;
    }

    /**
     * 设置除了spi、i2c模式以外的模式下，两个通道的通道号
     */
    public void setCommonCh(int ch) {
        this.text1Ch = ch;
    }

    /**
     * 设置i2c模式下，两个通道的通道号
     */
    public void setI2cCh(int text1Ch, int text2Ch) {
        this.text1Ch = text1Ch;
        this.text2Ch = text2Ch;
    }

    /**
     * 设置spi模式下，两个通道的通道号
     */
    public void setSpiCh(int text1Ch, int text2Ch, int text3Ch) {
        this.text1Ch = text1Ch;
        this.text2Ch = text2Ch;
        this.text3Ch = text3Ch;
    }

    public void setChecked(boolean checked) {
        if (!isEnabled()) return;
        if (this.checked != checked) {
            this.checked = checked;
            setChecked();
        }
    }

    public boolean isChecked() {
        return checked;
    }

    public String getName() {
        return strNameCheck;
    }

    public void setName(String strNameCheck) {
        this.strNameCheck = strNameCheck;
        tvNameCheck.setText(strNameCheck);
    }

    public void setSerialsTextLine1(String text) {
        if (!StrUtil.isEmpty(text)) {
            text = text.replace("b/s", "");
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9);
            Rect textRect = Tools.getTextRect(text, tv1.getPaint());
            if (textRect.width() >= 45) {
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9);
            } else {
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9);
            }
            tv1.setVisibility(View.VISIBLE);
            tv1.setText(text);
        } else {
            tv1.setVisibility(View.GONE);
        }
    }

    public void setSerialsTextLine2(String text) {
        if (!StrUtil.isEmpty(text)) {
            text = text.replace("b/s", "");
            tv2.setVisibility(View.VISIBLE);
            tv2.setText(text);
        } else {
            tv2.setVisibility(View.GONE);
        }
    }

    public void setSerialsTextLine3(String text) {
        if (!StrUtil.isEmpty(text)) {
            text = text.replace("b/s", "");
            tv3.setVisibility(View.VISIBLE);
            tv3.setText(text);
        } else {
            tv3.setVisibility(View.GONE);
        }
    }

    public int getSerialsTextLine3Visible() {
        return tv3.getVisibility();
    }

    public String getSerialsTextLine1() {
        return tv1.getText().toString();
    }

    public String getSerialsTextLine2() {
        return tv2.getText().toString();
    }

    public String getSerialsTextLine3() {
        return tv3.getText().toString();
    }

    public void setSerialsText(String text1, String text2, String text3, String text4
            , int color1, int color2, int color3, int color4
            , boolean line1, boolean line2, boolean line3, boolean line4) {
        if (!StrUtil.isEmpty(text1)) {
            text1 = text1.replace("b/s", "");
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9);
            Rect textRect = Tools.getTextRect(text1, tv1.getPaint());
            if (textRect.width() >= 45) {
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9);
            } else {
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9);
            }
            if (line1) {
                tv1.setText(text1);
                tv1.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            } else {
                tv1.setText(text1);
                tv1.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            }
            tv1.setTextColor(color1);
            tv1.setVisibility(View.VISIBLE);
        } else {
            tv1.setVisibility(View.GONE);
        }
        if (!StrUtil.isEmpty(text2)) {
            text2 = text2.replace("b/s", "");
            if (line2) {
                tv2.setText(text2);
                tv2.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            } else {
                tv2.setText(text2);
                tv2.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            }
            tv2.setTextColor(color2);
            tv2.setVisibility(View.VISIBLE);
        } else {
            tv2.setVisibility(View.GONE);
        }
        if (!StrUtil.isEmpty(text3)) {
            text3 = text3.replace("b/s", "");
            if (line3) {
                tv3.setText(text3);
                tv3.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            } else {
                tv3.setText(text3);
                tv3.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            }
            tv3.setTextColor(color3);
            tv3.setVisibility(View.VISIBLE);
        } else {
            tv3.setVisibility(View.GONE);
        }
        if (!StrUtil.isEmpty(text4)) {
            text4 = text4.replace("b/s", "");
            if (line4) {
                tv4.setText(Html.fromHtml("<u>" + text4 + "</u>"));
            } else {
                tv4.setText(text4);
            }
            tv4.setTextColor(color4);
            tv4.setVisibility(View.VISIBLE);
        } else {
            tv4.setVisibility(View.GONE);
        }

    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }
}
