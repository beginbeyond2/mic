package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.tbook.tbookscope.R;

public class MainRightLayoutItemChannelBranch extends LinearLayout {
    private Context context;
    private boolean checked = true;
    private String text;

    private ConstraintLayout bgBranch;
    private ConstraintLayout layoutBranch;
    private TextView tvChannelText;
    private TextView tvInvert;
    private TextView tvBandWidth;
    private ImageView tvCouple;
    private TextView tvImped;
    private TextView tvProbeDetailText;
    private TextView tvOffset;
    private TextView textBranch;
    private OnClickListener onClickListener;
    private boolean fromExternalKey;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public boolean isFromExternalKey() {
        return fromExternalKey;
    }

    public void setFromExternalKey(boolean fromExternalKey) {
        this.fromExternalKey = fromExternalKey;
    }

    public MainRightLayoutItemChannelBranch(Context context) {
        this(context, null);
    }

    public MainRightLayoutItemChannelBranch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainRightLayoutItemChannelBranch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        inflate(context, R.layout.layout_mainright_channel_branch, this);
        text = "1";
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainRightLayoutItemChannelBranch);
        checked = ta.getBoolean(R.styleable.MainRightLayoutItemChannelBranch_checked, true);
        text = ta.getString(R.styleable.MainRightLayoutItemChannelBranch_text);

        ta.recycle();

        initView();
        setChecked(checked);
    }

    private void initView() {
        setBackgroundColor(getResources().getColor(R.color.bg_main_outside));

        bgBranch = (ConstraintLayout) findViewById(R.id.bgBranch);
        layoutBranch = (ConstraintLayout) findViewById(R.id.layoutBranch);
        tvChannelText = (TextView) findViewById(R.id.channelText);
        tvInvert = (TextView) findViewById(R.id.invert);
        tvBandWidth = (TextView) findViewById(R.id.bandWidth);
        tvCouple = (ImageView) findViewById(R.id.couple);
        tvImped = (TextView) findViewById(R.id.Imped);
        tvProbeDetailText = (TextView) findViewById(R.id.probeDetailText);
        tvOffset = (TextView) findViewById(R.id.offset);
        textBranch = (TextView) findViewById(R.id.textBranch);

        ((Button) findViewById(R.id.btnBranch)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(MainRightLayoutItemChannelBranch.this);
            }
        });

        tvChannelText.setText(text);
        textBranch.setText(text);
        tvInvert.setVisibility(INVISIBLE);
        tvBandWidth.setText("");
        tvCouple.setImageResource(0);
        tvImped.setText("");
        tvProbeDetailText.setText("");
        tvOffset.setText("");

        setBackground();
    }

    private void setBackground() {
        if ("1".equals(text)) {
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Ch1));
            textBranch.setTextColor(getResources().getColor(R.color.color_Ch1));
            if (checked) {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch1_have);
            } else {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch1_nothing);
            }
        } else if ("2".equals(text)) {
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Ch2));
            textBranch.setTextColor(getResources().getColor(R.color.color_Ch2));
            if (checked) {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have);
            } else {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing);
            }
        } else if ("3".equals(text)) {
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Ch3));
            textBranch.setTextColor(getResources().getColor(R.color.color_Ch3));
            if (checked) {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have);
            } else {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing);
            }
        } else if ("4".equals(text)) {
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Ch4));
            textBranch.setTextColor(getResources().getColor(R.color.color_Ch4));
            if (checked) {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch4_have);
            } else {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch4_nothing);
            }
        } else if ("M1".equals(text)) {
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Math));
            textBranch.setTextColor(getResources().getColor(R.color.color_Math));
            textBranch.setText("MATH");
            if (checked) {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch1_have);
            } else {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch1_nothing);
            }
        } else if ("R1".equals(text)) {
            tvChannelText.setTextColor(getResources().getColor(R.color.color_R1));
            textBranch.setTextColor(getResources().getColor(R.color.color_R1));
            textBranch.setText("REF");
            if (checked) {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have);
            } else {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing);
            }
        } else if ("S1".equals(text)) {
            tvChannelText.setTextColor(getResources().getColor(R.color.color_S1));
            textBranch.setTextColor(getResources().getColor(R.color.color_S1));
            if (checked) {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have);
            } else {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing);
            }
        } else if ("S2".equals(text)) {
            tvChannelText.setTextColor(getResources().getColor(R.color.color_S2));
            textBranch.setTextColor(getResources().getColor(R.color.color_S2));
            if (checked) {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch4_have);
            } else {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch4_nothing);
            }
        } else {
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Math));
            textBranch.setTextColor(getResources().getColor(R.color.color_Math));
            if (checked) {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have);
            } else {
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing);
            }
        }
    }

    public void clickOnClick() {
        onClickListener.onClick(MainRightLayoutItemChannelBranch.this);
    }

    public String getText() {
        return text;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (checked) {
            layoutBranch.setVisibility(VISIBLE);
            textBranch.setVisibility(INVISIBLE);
        } else {
            layoutBranch.setVisibility(INVISIBLE);
            textBranch.setVisibility(VISIBLE);
        }
        setBackground();
    }

    public boolean isChecked() {
        return checked;
    }

    public void setInvert(boolean invert) {
        tvInvert.setVisibility(invert ? VISIBLE : INVISIBLE);
    }

    public void setBandWidth(String bandWidth) {
        tvBandWidth.setText(bandWidth);
    }

    public void setCoupleResId(int coupleResId) {
        tvCouple.setImageResource(coupleResId);
    }

    public void setImped(String imped) {
        tvImped.setText(imped);
    }

    public void setProbeDetail(String probeDetailText) {
        tvProbeDetailText.setText(probeDetailText);
    }

    public void setOffset(String offset) {
        tvOffset.setText(offset);
    }


    public void setMathRefOffset(String timebase) {
        tvOffset.setText(timebase);
    }

    public void setMathRefMsg(String msg, boolean textSmall) {
        setMathRefMsgTextSize(textSmall);
        tvImped.setText(msg);
    }

    public void setMathRefMsg(SpannableStringBuilder msg, boolean textSmall) {
        setMathRefMsgTextSize(textSmall);
        tvImped.setText(msg);
    }

    private void setMathRefMsgTextSize(boolean textSmall) {
        if (textSmall) {
            tvImped.setMaxLines(3);
            tvImped.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9);
            tvOffset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9);
        } else {
            tvImped.setMaxLines(1);
            tvImped.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
            tvOffset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 13);
        }
    }

    public void setSerialsTitle(String title) {
        tvBandWidth.setText(title);
    }

    public void setSerialsMsgMiddle(String msg) {
        tvImped.setText(msg);
    }

    public void setSerialsMsgBottom(String msg) {
        if (("S1".equals(text) || "S2".equals(text)) && "UART".contentEquals(tvBandWidth.getText())) {
            tvOffset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12);
        } else {
            tvOffset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 13);
        }
        tvOffset.setText(msg);
    }

    public void setSerialsMsgMiddle(SpannableStringBuilder msg) {
        tvImped.setText(msg);
    }

    public void setSerialsMsgBottom(SpannableStringBuilder msg) {
        tvOffset.setText(msg);
    }
}
