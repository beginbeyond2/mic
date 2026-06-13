package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.util.TBookUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by yangj on 2017/4/24.
 */

public class DialogBandWidth extends AbsoluteLayout {
    public static final String UNIT_MHZ = TBookUtil.UNIT_MHZ;
    public static final String UNIT_KHZ = TBookUtil.UNIT_KHZ;

    private int integerPart;
    private int decimalPart;
    private String curUnit;

    private Context context;
    private TextView title, show;
    private RadioButton mhz, khz;
    private Button subtract, add;
    private SeekBar seekBar;
    private OnDismissListener onDismissListener;
    private boolean isFromUser;

    private ViewGroup rootViewGroup;

    public interface OnDismissListener {
        void onDismiss(String result);
        void bandSwitchChange(String fb);
    }

    public DialogBandWidth(Context context) {
        this(context, null);
    }

    public DialogBandWidth(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogBandWidth(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    //[50, 387]	608	125
    private void init() {
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_bandwidth, this);

        findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (onDismissListener != null) {
                    String showString;
                    if (integerPart >= 1) {
                        showString = getShowString();
                    } else {
                        decimalPart = Math.max(decimalPart, 30);
                        showString = decimalPart + UNIT_KHZ;
                    }
                    onDismissListener.onDismiss(showString);
                }
                hide();
                return false;
            }
        });
        initView(rootViewGroup);
        hide();
    }

    private void initView(View view) {
        title = (TextView) view.findViewById(R.id.title);
        show = (TextView) view.findViewById(R.id.show);
        mhz = (RadioButton) view.findViewById(R.id.mhz);
        khz = (RadioButton) view.findViewById(R.id.khz);
        subtract = (Button) view.findViewById(R.id.subtract);
        add = (Button) view.findViewById(R.id.add);
        seekBar = (SeekBar) view.findViewById(R.id.progress);

        mhz.setOnClickListener(onCheckedListener);
        khz.setOnClickListener(onCheckedListener);
        subtract.setOnClickListener(onCheckedListener);
        add.setOnClickListener(onCheckedListener);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_BANDWIDTH);
        Tools.PrintControlsLocation("DialogBandWidth",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_BANDWIDTH);
    }

    public void setProgress(int i) {
        isFromUser = true;
        seekBar.setProgress(i);
    }

    public int getProgress() {
        return seekBar.getProgress();
    }

    public void setData(String titleString, String numberString, OnDismissListener onDismissListener) {
        if (numberString.endsWith(UNIT_KHZ)) {
            curUnit = UNIT_MHZ;
            integerPart = 0;
            decimalPart = Integer.valueOf(numberString.replace(UNIT_KHZ, ""));
        } else if (numberString.endsWith(UNIT_MHZ)) {
            double curNumber = Double.valueOf(numberString.replace(UNIT_MHZ, ""));
            curUnit = UNIT_MHZ;
            integerPart = (int) curNumber;
            decimalPart = (int) (curNumber * 1000 % 1000);
        } else {
            curUnit = UNIT_MHZ;
            integerPart = 0;
            decimalPart = 0;
        }
        this.onDismissListener = onDismissListener;
        title.setText(titleString);
        show.setText(getShowString());
        isFromUser = false;
        if (mhz.isChecked()) {
            seekBar.setMax(100);
            seekBar.setProgress(integerPart);
        } else if (khz.isChecked()) {
            seekBar.setMax(1000);
            seekBar.setProgress(decimalPart);
        }
        show();
    }

    private String getShowString() {
        double curNumber = integerPart + decimalPart * 1.0 / 1000;
        curNumber = Math.min(curNumber, 100);
        DecimalFormat df = new DecimalFormat("###0.000", new DecimalFormatSymbols(Locale.CHINA));
        return TBookUtil.getDMinus0(Double.parseDouble(df.format(curNumber))) + curUnit;
    }

    private View.OnClickListener onCheckedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            if (mhz.isChecked() && khz.getId() == v.getId()) {
                khz.setChecked(true);
                mhz.setChecked(false);
                isFromUser = false;
                seekBar.setMax(1000);
                seekBar.setProgress(decimalPart);
            } else if (khz.isChecked() && mhz.getId() == v.getId()) {
                mhz.setChecked(true);
                khz.setChecked(false);
                isFromUser = false;
                seekBar.setMax(100);
                seekBar.setProgress(integerPart);
            } else if (add.getId() == v.getId()) {
                isFromUser = true;
                seekBar.setProgress(seekBar.getProgress() + 1);
            } else if (subtract.getId() == v.getId()) {
                isFromUser = true;
                seekBar.setProgress(seekBar.getProgress() - 1);
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isFromUser || fromUser) {
                if (mhz.isChecked()) {
                    integerPart = progress;
                } else if (khz.isChecked()) {
                    decimalPart = progress;
                }
            }

            if (integerPart < 1 && decimalPart < 30) {
                decimalPart = Math.max(decimalPart, 30);
                seekBar.setProgress(30);
            }
            String str=getShowString();
            onDismissListener.bandSwitchChange(str);
            show.setText(str);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
