package com.micsig.tbook.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

/**
 * Created by yangj on 2017/6/28.
 */

public class MButton_CheckBox_ThreeClick extends MButton_CheckBox {
    private OnThreeClickListener onThreeClickListener;

    public interface OnThreeClickListener {
        boolean onThreeClick(boolean check);
    }

    public OnThreeClickListener getOnThreeClickListener() {
        return onThreeClickListener;
    }

    public void setOnThreeClickListener(OnThreeClickListener onThreeClickListener) {
        this.onThreeClickListener = onThreeClickListener;
    }

    public MButton_CheckBox_ThreeClick(Context context) {
        super(context);
    }

    public MButton_CheckBox_ThreeClick(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MButton_CheckBox_ThreeClick(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSingleClick() {
        if (onThreeClickListener != null) {
            checked = onThreeClickListener.onThreeClick(checked);
        } else {
            checked = !checked;
        }
    }
}
