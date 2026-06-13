package com.micsig.tbook.ui.top.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.micsig.tbook.ui.R;

import org.w3c.dom.Text;

/**
 * Created by Administrator on 2017/4/6.
 */

public class TopViewSeekBar extends LinearLayout {
    private Context context;
    private String seekbarTitle;

    private TextView triggerLow,triggerHigh;
    private int seekbarMax;
    private int seekbarInit;
    private Button show;
    private int headWidth, seekbarWidth;
    private SeekBar seekBar;
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener;

    public TopViewSeekBar(Context context) {
        this(context, null);
    }

    public TopViewSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.view_seekbarwithhead, this);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewSeekBar);
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewSeekBar_headWidth, 100);
        seekbarWidth = ta.getDimensionPixelSize(R.styleable.TopViewSeekBar_seekBarWidth, 480);
        ta.recycle();
    }

    public void setData(String seekbarTitle, int seekbarMax, int seekbarInit, SeekBar.OnSeekBarChangeListener seekBarChangeListener) {
        this.seekbarTitle = seekbarTitle;
        this.seekbarMax = seekbarMax;
        this.seekbarInit = seekbarInit;
        this.seekBarChangeListener = seekBarChangeListener;
        updateView();
    }

    public void setData(int seekbarTitleResId, int seekbarMax, int seekbarInit, SeekBar.OnSeekBarChangeListener seekBarChangeListener) {
        setData(context.getString(seekbarTitleResId), seekbarMax, seekbarInit, seekBarChangeListener);
    }

    private void updateView() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(seekbarTitle);
        LinearLayout.LayoutParams lpTitle = (LayoutParams) title.getLayoutParams();
        lpTitle.width = headWidth;
        title.setLayoutParams(lpTitle);

        show = (Button) findViewById(R.id.show);
        show.setText(seekbarInit * 100 / seekbarMax + "%");
        show.setOnClickListener(onClickListener);
        show.setEnabled(false);

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(seekbarMax);
        seekBar.setProgress(seekbarInit);
        LinearLayout.LayoutParams lpSeekbar = (LayoutParams) seekBar.getLayoutParams();
        lpSeekbar.width = seekbarWidth;
        seekBar.setLayoutParams(lpSeekbar);
        seekBar.setOnSeekBarChangeListener(mChangeListener);
        seekBar.setOnClickListener(null);//去掉此行，则会在屏幕上点击其他地方时，seekBar会有响应
    }

    public int getProgress() {
        return seekBar.getProgress();
    }

    public void setProgress(int progress) {
        seekBar.setProgress(progress);
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            seekBar.setProgress(seekBar.getProgress() + 10);
        }
    };
    public void setTriggerSeekBar(boolean isTriggerSeekBar){
        if(isTriggerSeekBar){
            triggerLow = findViewById(R.id.triggerSensitivityLow);
            triggerHigh = findViewById(R.id.triggerSensitivityHigh);
            show = (Button) findViewById(R.id.show);
            triggerLow.setVisibility(View.VISIBLE);
            triggerHigh.setVisibility(View.VISIBLE);
            show.setVisibility(View.GONE);
        }
    }
    private SeekBar.OnSeekBarChangeListener mChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            show.setText(progress + "%");
            if (seekBarChangeListener != null) {
                seekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (seekBarChangeListener != null) {
                seekBarChangeListener.onStartTrackingTouch(seekBar);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (seekBarChangeListener != null) {
                seekBarChangeListener.onStopTrackingTouch(seekBar);
            }
        }
    };
}
