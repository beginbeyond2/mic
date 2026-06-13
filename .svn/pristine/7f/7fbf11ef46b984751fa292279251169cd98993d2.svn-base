package com.micsig.tbook.ui.top.view.title;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by yangj on 2017/4/13.
 */

public class TopViewHorScroll extends HorizontalScrollView {

    private static final int MinSliderRange=20;

    private OnScrollListener onScrollListener;

    public TopViewHorScroll(Context context) {
        super(context);
    }

    public TopViewHorScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopViewHorScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface OnScrollListener {
        void onScrollChanged(TopViewHorScroll scrollView, int x, int y, int oldx, int oldy);

        void onStop();
    }

    public OnScrollListener getOnScrollListener() {
        return onScrollListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    int l, oldl,t,oldt,downX;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (onScrollListener != null && Math.abs(downX-(int) ev.getX())>MinSliderRange) {
                onScrollListener.onStop();
                return true;
            }
            return super.onInterceptTouchEvent(ev);
        } else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            oldl = (int) ev.getX();
            oldt=(int)ev.getY();
            downX=oldl;
            return false;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            l=(int) ev.getX();
            t=(int) ev.getY();
            if (onScrollListener != null && Math.abs(downX-(int) ev.getX())>MinSliderRange) {
                onScrollListener.onScrollChanged(this, l, 0, oldl, 0);
            }
            oldl=(int) ev.getX();
            return false;
        } else {
            return false;
        }
    }

////
//    @Override
//    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//        super.onScrollChanged(l, t, oldl, oldt);
//        if (onScrollListener != null) {
//            onScrollListener.onScrollChanged(this, l, t, oldl, oldt);
//        }
//    }

}
