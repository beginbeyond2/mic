package com.micsig.tbook.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.base.Logger;
import com.micsig.tbook.ui.util.BitmapUtil;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.svg.SvgManager;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2017/4/11.
 */

public class MSwitchBox extends View {
    private static final String TAG = "MSwitchBox";
    private Bitmap openBitmap, closeBitmap, disableBitmap;
    private int lastX, lastY, switchWidth, switchHeight;
    //region
    private boolean currState;
    private boolean enable;
    private Context context;
    private OnToggleStateChangedListener onToggleStateChangedListener = null;

    public void setOnToggleStateChangedListener(OnToggleStateChangedListener onToggleStateChangedListener) {
        this.onToggleStateChangedListener = onToggleStateChangedListener;
    }
    //endregion

    public MSwitchBox(Context context) {
        this(context, null);
    }

    public MSwitchBox(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSwitchBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MSwitchBox);
        switchWidth = ta.getDimensionPixelSize(R.styleable.MSwitchBox_switchWidth, 72);
        switchHeight = ta.getDimensionPixelSize(R.styleable.MSwitchBox_switchHeight, 36);
        openBitmap = BitmapUtil.getBitmapFromDrawable(getContext(), R.drawable.svg_switch_open, switchWidth, switchHeight);
        closeBitmap = BitmapUtil.getBitmapFromDrawable(getContext(), R.drawable.svg_switch_close, switchWidth, switchHeight);
        disableBitmap = BitmapUtil.getBitmapFromDrawable(getContext(), R.drawable.svg_switch_disable, switchWidth, switchHeight);
        ta.recycle();
        enable = true;
    }

    public void setControlColorByChIdx(int chId){
        if (TChan.isCh1ToS4(chId)){
            int color = TChan.getChannelColor(context, chId);
//            Logger.i(TAG, "Switch color = " + SvgNodeInfo.getAllBaseColor(chId) + " ,chIndex= " + chId + " ,switchWidth= " + switchWidth + " ,switchHeight= " + switchHeight);
            openBitmap = SvgManager.createScaleSvg(SvgNodeInfo.getSwitchOpenPaths(), SvgNodeInfo.getSwitchOpenColors(chId), SvgNodeInfo.SWITCH_WIDTH, SvgNodeInfo.SWITCH_HEIGHT, switchWidth, switchHeight);
        }else {
            openBitmap = BitmapUtil.getBitmapFromDrawable(getContext(), R.drawable.svg_switch_open, switchWidth, switchHeight);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint=new Paint();
        int top = getMeasuredHeight() / 2 - openBitmap.getHeight() / 2;
        if (!enable) {
            canvas.drawBitmap(disableBitmap, 0, top, paint);
        } else if (currState) {
            canvas.drawBitmap(openBitmap, 0, top, paint);
        } else {
            canvas.drawBitmap(closeBitmap, 0, top, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (getViewRect(this).contains((int) event.getX(), (int) event.getY()) &&
                        getViewRect(this).contains(lastX, lastY)) {
                    if (!enable) {
                        break;
                    }
                    currState = !currState;
                    if (onToggleStateChangedListener != null) {
                        Logger.d(TAG, "onToggleStateChangedListener," + ScreenUtil.getViewLocation(MSwitchBox.this));
                        onToggleStateChangedListener.onToggleStateChanged(MSwitchBox.this, currState);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        invalidate();
        return true;
    }

    private static Rect getViewRect(View view) {
        return new Rect(0, 0, view.getWidth(), view.getHeight());
    }

    public boolean isState() {
        return currState;
    }

    public void setState(boolean state) {
        currState = state;
        invalidate();
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enable = enabled;
        invalidate();
    }

    public boolean isEnabled() {
        return enable;
    }

    /**
     * @author liwb
     */
    public interface OnToggleStateChangedListener {

        public void onToggleStateChanged(MSwitchBox view, boolean state);

    }
}
