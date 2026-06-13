package com.micsig.tbook.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class MCheckBox extends RelativeLayout {
    private Context context;

    public MCheckBox(Context context) {
        this(context, null);
    }

    public MCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        inflate(context, R.layout.layout_checkbox, this);
    }
}
