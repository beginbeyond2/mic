package com.micsig.tbook.ui.rightslipmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by yangj on 2017/5/5.
 */

public class InterceptTouchRelativeLayout extends RelativeLayout {
    private OnInterceptTouchListener onInterceptTouchListener;

    public interface OnInterceptTouchListener {
        void onTouch(MotionEvent event,View view);
    }

    public void setOnInterceptTouchListener(OnInterceptTouchListener onInterceptTouchListener) {
        this.onInterceptTouchListener = onInterceptTouchListener;
    }

    public InterceptTouchRelativeLayout(Context context) {
        super(context);
    }

    public InterceptTouchRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptTouchRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        return super.onInterceptTouchEvent(ev);
    }

    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (onInterceptTouchListener != null) {
            onInterceptTouchListener.onTouch(event,InterceptTouchRelativeLayout.this);
        }
        super.onTouchEvent(event);
        return true;
    }
}
