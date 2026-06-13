package com.micsig.tbook.tbookscope.top.popwindow;

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
import com.micsig.tbook.ui.util.ScreenUtil;

/**
 * Created by yangj on 2017/4/24.
 */

public class TopDialogCount extends AbsoluteLayout {
    private int curCount = 321;
    private int maxCount = 65535;

    private Context context;
    private TextView title, show;
    private RadioButton x1, x100;
    private Button subtract, add;
    private SeekBar seekBar;
    private OnDismissListener onDismissListener;
    private boolean isFromUser;

    private ViewGroup rootViewGroup;

    public interface OnDismissListener {
        void onDismiss(int result);
    }

    public TopDialogCount(Context context) {
        this(context, null);
    }

    public TopDialogCount(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogCount(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    //[50, 387]	608	125
    private void init() {
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_count, this);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (onDismissListener != null) {
                    onDismissListener.onDismiss(Integer.parseInt(show.getText().toString()));
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
        x1 = (RadioButton) view.findViewById(R.id.x1);
        x100 = (RadioButton) view.findViewById(R.id.x100);
        subtract = (Button) view.findViewById(R.id.subtract);
        add = (Button) view.findViewById(R.id.add);
        seekBar = (SeekBar) view.findViewById(R.id.progress);

        x1.setOnClickListener(onCheckedListener);
        x100.setOnClickListener(onCheckedListener);
        subtract.setOnClickListener(onCheckedListener);
        add.setOnClickListener(onCheckedListener);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_TOPCOUNT);
        Tools.PrintControlsLocation("TopDialogCount",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_TOPCOUNT);
    }

    public void setProgress(int i) {
        isFromUser = true;
        seekBar.setProgress(i);
    }

    public int getProgress() {
        return seekBar.getProgress();
    }

    public void setData(String titleString, int curCount, int maxCount, OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        this.curCount = curCount;
        this.maxCount = maxCount;
        title.setText(titleString);
        show.setText(String.valueOf(curCount));
        isFromUser = false;
        if (x1.isChecked()) {
            seekBar.setMax(curCount < 100 ? 98 : 99);
            seekBar.setProgress(curCount < 100 ? curCount - 1 : curCount % 100);
        } else if (x100.isChecked()) {
            seekBar.setMax(maxCount / 100);
            seekBar.setProgress(curCount / 100);
        }
        show();
    }

    private View.OnClickListener onCheckedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            ScreenUtil.getViewLocation(v);
            if (x1.isChecked() && x100.getId() == v.getId()) {
                x100.setChecked(true);
                x1.setChecked(false);
                isFromUser = false;
                seekBar.setMax(maxCount / 100);
                seekBar.setProgress(curCount / 100);
            } else if (x100.isChecked() && x1.getId() == v.getId()) {
                x1.setChecked(true);
                x100.setChecked(false);
                isFromUser = false;
                seekBar.setMax(curCount < 100 ? 98 : 99);
                seekBar.setProgress(curCount < 100 ? curCount - 1 : curCount % 100);
            } else if (add.getId() == v.getId()) {
                isFromUser = true;
                if (x1.isChecked()) {
                    seekBar.setProgress(seekBar.getProgress() + 1);
                } else if (x100.isChecked()) {
                    seekBar.setProgress(seekBar.getProgress() + 1);
                }
            } else if (subtract.getId() == v.getId()) {
                isFromUser = true;
                if (x1.isChecked()) {
                    seekBar.setProgress(seekBar.getProgress() - 1);
                } else if (x100.isChecked()) {
                    seekBar.setProgress(seekBar.getProgress() - 1);
                }
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ScreenUtil.getViewLocation(seekBar);
            if (isFromUser || fromUser) {
                if (x1.isChecked()) {
                    if (curCount < 100) {
                        curCount = progress + 1;
                    } else {
                        curCount = curCount / 100 * 100 + progress;
                    }
                } else if (x100.isChecked()) {
                    curCount = curCount % 100 + progress * 100;
                }
            }
            if (curCount > maxCount) {
                curCount = maxCount;
            }
            if (curCount == 0) {
                curCount = 1;
            }
            show.setText(String.valueOf(curCount));

            if (onDismissListener != null) {
                onDismissListener.onDismiss(Integer.parseInt(show.getText().toString()));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
