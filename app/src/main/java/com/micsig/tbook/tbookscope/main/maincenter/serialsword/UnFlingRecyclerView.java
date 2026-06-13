package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class UnFlingRecyclerView extends RecyclerView {
    public UnFlingRecyclerView(Context context) {
        super(context);
    }

    public UnFlingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UnFlingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        return false;
    }
}
