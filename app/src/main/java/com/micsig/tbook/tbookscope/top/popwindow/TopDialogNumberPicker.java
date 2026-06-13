package com.micsig.tbook.tbookscope.top.popwindow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.TopViewNumberPicker;

public class TopDialogNumberPicker extends RelativeLayout {
    private static final String TAG = "TopDialogNumberPicker";
    private Context context;
    private TopViewNumberPicker numberPickerView;
    private OnDismissListener onDismissListener;
    private String number, power;
    private ViewGroup rootViewGroup;

    public interface OnDismissListener {
        void onDismiss(String number, String power);
    }

    public TopDialogNumberPicker(Context context) {
        this(context, null);
    }

    public TopDialogNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        setClickable(true);
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_numberpicker, this);

        numberPickerView = (TopViewNumberPicker) rootViewGroup.findViewById(R.id.numberPickerView);
        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onDismissListener.onDismiss(number, power);
                hide();
                return false;
            }
        });
        hide();
    }

    public void setData(String number, String power, OnDismissListener onDismissListener) {
        this.number = number;
        this.power = power;
        this.onDismissListener = onDismissListener;
        show();
        numberPickerView.setData(this.number, this.power, onNumberPickerListener);
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_FORMULAKEYBOARD);
        Tools.PrintControlsLocation("TopDialogNumBerPicker",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_FORMULAKEYBOARD);
    }

    private TopViewNumberPicker.OnNumberPickerListener onNumberPickerListener = new TopViewNumberPicker.OnNumberPickerListener() {
        @Override
        public void onChangedShow(String s1, String s2) {
            PlaySound.getInstance().playButton();
            TopDialogNumberPicker.this.number = s1;
            TopDialogNumberPicker.this.power = s2;
        }
    };


    public void openExpand(int index) {
        numberPickerView.openExpand(index);
    }

    public void addOne(int index, boolean isAdd) {
        numberPickerView.addOne(index, isAdd);
    }
}
