package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.ui.MButton;
import com.micsig.tbook.ui.MButton_CheckBox_ThreeClick;
import com.micsig.tbook.ui.util.TBookUtil;

/**
 * Created by yangj on 2017/5/11.
 */

public class MainRightLayoutItemChannel extends AbsoluteLayout {
    private Context context;
    private MButton_CheckBox_ThreeClick channelButton;
    private RelativeLayout showLayout;
    private TextView tvInvert;
    private TextView tvChannelText;
    private ImageView tvCouple;
    private TextView tvProbeTypeNum, tvProbeTypeUtil;
    private TextView tvBandWidth;
    private MButton btnMV, btnV;
    private TextView tvProbeDetailText;
    private View tvProbeDetailBg;

    private boolean checked = false;

    private int chIndex;
    private String text;
    private boolean isInvert;
    private int coupleResId;
    private String strBandWidth;
    private String strProbeDetail;
    private OnButtonClickListener onButtonClickListener;

    /**
     * 是否来自外部按键
     */
    private boolean isFromExternalKey = false;

    public interface OnButtonClickListener {
        /**
         * @param before 此check是否是此次操作改变之前的状态，
         *               即如果为true则此check代表此次操作还未执行切换，需要在该接口实现里进行切换状态
         * @param sound  是否播放声音
         * @return check的此次操作之后的最终状态
         */
        boolean onNameClick(MainRightLayoutItemChannel layout, boolean checked, boolean before, boolean sound);

        void onMVClick(MainRightLayoutItemChannel layout);

        void onVClick(MainRightLayoutItemChannel layout);
    }

    public MainRightLayoutItemChannel(Context context) {
        this(context, null);
    }

    public MainRightLayoutItemChannel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainRightLayoutItemChannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        text = "CH1";
        isInvert = true;
        coupleResId = R.drawable.coupling_dc;
        strBandWidth = "20M";
        strProbeDetail = "1mX";
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_mainright_channel, this);
//        setBackgroundResource(R.color.color_Backcolor_black);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainLayoutRightChannel);
        checked = ta.getBoolean(R.styleable.MainLayoutRightChannel_checked, false);
        text = ta.getString(R.styleable.MainLayoutRightChannel_text);
        chIndex = Integer.parseInt(text.replace("CH", "")) - 1;
        int showLayoutMarginTop = ta.getDimensionPixelSize(R.styleable.MainLayoutRightChannel_showLayoutMarginTop, 32);
        int bgLeftChecked = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgLeftChecked, R.drawable.l_ch1);
        int bgLeftUnChecked = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgLeftUnChecked, R.drawable.l_unclick);
        int bgRightTopTouchDown = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgRightTopTouchDown, R.drawable.ch1_mv_press);
        int bgRightTopTouchUp = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgRightTopTouchUp, R.drawable.ch1_mv);
        int bgRightBottomTouchDown = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgRightBottomTouchDown, R.drawable.ch1_v_press);
        int bgRightBottomTouchUp = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgRightBottomTouchUp, R.drawable.ch1_v);
        int bgProbeDetail = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgProbeDetail, R.drawable.ch1_10x);
        ta.recycle();

        channelButton = (MButton_CheckBox_ThreeClick) findViewById(R.id.channelButton);
        showLayout = (RelativeLayout) findViewById(R.id.showLayout);
        tvInvert = (TextView) findViewById(R.id.invert);
        tvChannelText = (TextView) findViewById(R.id.channelText);
        tvCouple = (ImageView) findViewById(R.id.couple);
        tvProbeTypeNum = (TextView) findViewById(R.id.probeTypeNum);
        tvProbeTypeUtil = (TextView) findViewById(R.id.probeTypeUnit);
        tvBandWidth = (TextView) findViewById(R.id.bandWidth);
        btnMV = (MButton) findViewById(R.id.mV);
        btnV = (MButton) findViewById(R.id.v);
        tvProbeDetailText = (TextView) findViewById(R.id.probeDetailText);
        tvProbeDetailBg = findViewById(R.id.probeDetailBg);

        AbsoluteLayout.LayoutParams lpShow = (LayoutParams) showLayout.getLayoutParams();
        lpShow.y = showLayoutMarginTop - 4;
        showLayout.setLayoutParams(lpShow);
        setChecked();
        AbsoluteLayout.LayoutParams lpChannel = (LayoutParams) channelButton.getLayoutParams();
        lpChannel.y = showLayoutMarginTop - 2;
        channelButton.setLayoutParams(lpChannel);
        channelButton.setText_y(7);
        channelButton.setCheckBitmap(BitmapFactory.decodeResource(getResources(), bgLeftChecked));
        channelButton.setUnCheckBitmap(BitmapFactory.decodeResource(getResources(), bgLeftUnChecked));
        btnMV.setTouchDownBitmap(BitmapFactory.decodeResource(getResources(), bgRightTopTouchDown));
        btnMV.setTouchUpBitmap(BitmapFactory.decodeResource(getResources(), bgRightTopTouchUp));
        btnV.setTouchDownBitmap(BitmapFactory.decodeResource(getResources(), bgRightBottomTouchDown));
        btnV.setTouchUpBitmap(BitmapFactory.decodeResource(getResources(), bgRightBottomTouchUp));
        tvProbeDetailBg.setBackgroundResource(bgProbeDetail);

        channelButton.setOnThreeClickListener(onThreeClickListener);
        btnMV.setOnClickListener(onCheckedChangeListener);
        btnV.setOnClickListener(onCheckedChangeListener);
    }

    private MButton_CheckBox_ThreeClick.OnThreeClickListener onThreeClickListener = new MButton_CheckBox_ThreeClick.OnThreeClickListener() {
        @Override
        public boolean onThreeClick(boolean checked) {
            return MainRightLayoutItemChannel.this.onThreeClick(checked);
        }
    };

    public boolean onThreeClick(boolean checked) {
        if (!isEnabled()) return isChecked();
        if (onButtonClickListener != null) {
            checked = onButtonClickListener.onNameClick(MainRightLayoutItemChannel.this, checked, true, true);
        } else {
            checked = !checked;
        }
        setChecked(checked);
        return checked;
    }

    private View.OnClickListener onCheckedChangeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isEnabled()) return;
            if (v.getId() == btnMV.getId()) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onMVClick(MainRightLayoutItemChannel.this);
                }
            } else if (v.getId() == btnV.getId()) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onVClick(MainRightLayoutItemChannel.this);
                }
            }
        }
    };

    private void setChecked() {
        if (!isEnabled()) return;
        if (checked) {
            channelButton.setText("");
            tvChannelText.setText(text);
            channelButton.setChecked(true);
            showLayout.setVisibility(VISIBLE);
            btnMV.setVisibility(VISIBLE);
            btnV.setVisibility(VISIBLE);
            tvProbeDetailText.setVisibility(VISIBLE);
            tvProbeDetailBg.setVisibility(VISIBLE);
        } else {
            channelButton.setText(text);
            channelButton.setChecked(false);
            showLayout.setVisibility(INVISIBLE);
            btnMV.setVisibility(INVISIBLE);
            btnV.setVisibility(INVISIBLE);
            tvProbeDetailText.setVisibility(INVISIBLE);
            tvProbeDetailBg.setVisibility(INVISIBLE);
        }
    }

    public boolean isFromExternalKey() {
        return isFromExternalKey;
    }

    public void setFromExternalKey(boolean fromExternalKey) {
        isFromExternalKey = fromExternalKey;
    }

    public void setVScale(double vScale) {
        Channel channel = ChannelFactory.getDynamicChannel(chIndex);
        channel.setVScaleVal(vScale);
        String probeEvery = TBookUtil.getMFromDouble(channel.getVScaleVal());
        setStrProbeTypeNum(probeEvery);
//        return probeEvery;
    }

    public String getText() {
        return text;
    }

    public void setChecked(boolean checked) {
        if (!isEnabled()) return;
        this.checked = checked;
        setChecked();
    }

    public boolean isChecked() {
        return checked;
    }

    public void setInvert(boolean invert) {
        isInvert = invert;
        tvInvert.setVisibility(isInvert ? VISIBLE : INVISIBLE);
    }

    public void setCoupleResId(int coupleResId) {
        this.coupleResId = coupleResId;
        tvCouple.setImageResource(this.coupleResId);
    }

    public void setStrProbeTypeUnit(String strProbeTypeUnit) {
        tvProbeTypeUtil.setText(strProbeTypeUnit);
    }

    public void setStrProbeTypeNum(String strProbeTypeNum) {
        tvProbeTypeNum.setText(strProbeTypeNum);
    }

    public void setStrBandWidth(String strBandWidth) {
        this.strBandWidth = strBandWidth;
        tvBandWidth.setText(this.strBandWidth);
    }

    public void setStrProbeDetail(String strProbeDetail) {
        this.strProbeDetail = strProbeDetail;
        tvProbeDetailText.setText(this.strProbeDetail);
    }

    public String getStrProbeType() {
        return tvProbeTypeNum.getText().toString() + tvProbeTypeUtil.getText().toString();
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }
}
