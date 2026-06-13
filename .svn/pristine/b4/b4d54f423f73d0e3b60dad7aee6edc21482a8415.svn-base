package com.micsig.tbook.ui.top.view.frequency;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.micsig.base.Logger;
import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.TBookUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by yangj on 2017/6/19.
 */

public class TopViewBandWidthHzSmall extends View {
    private Context context;

    private Bitmap lineBitmap;
    private Bitmap indicatorBitmap;
    private Paint paint;
    private Scroller scroller;
    private int maxVelocity;
    private DecimalFormat df2;
    private VelocityTracker velocityTracker;

    private long minNs = TopUtilBandWidthHz.DEFAULT_MIN_TIME;
    private long maxNs = TopUtilBandWidthHz.DEFAULT_MAX_TIME;

    private int width;
    private int height;
    private float twoLinePx;
    private int bgColor;
    private int lineColor;
    private int textColor;
    private int indicatorColor;
    private int indicatorLength;
    private int textPadding;
    private double curValue;
    private double itemValue;
    private String curUnit;
    private float moveOffset;
    private float startX;
    private float lastX;
    float lastOffset = 0;
    double startValue = 0;

    private int pointerId;
    private OnRulerChangedListener onRulerChangedListener;

    public interface OnRulerChangedListener {
        void rulerChanged(String value, String unit, double curItem);
    }

    public OnRulerChangedListener getOnRulerChangedListener() {
        return onRulerChangedListener;
    }

    public void setOnRulerChangedListener(OnRulerChangedListener onRulerChangedListener) {
        this.onRulerChangedListener = onRulerChangedListener;
    }

    public TopViewBandWidthHzSmall(Context context) {
        this(context, null);
    }

    public TopViewBandWidthHzSmall(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewBandWidthHzSmall(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        twoLinePx = 12.5f;
        width = (int) (twoLinePx * 40);
        height = 90;
        bgColor = getResources().getColor(R.color.bg_slip_backcolor);
        lineColor = getResources().getColor(R.color.scaleDivider);
        textColor = getResources().getColor(R.color.textColor);
        indicatorColor = Color.RED;
        indicatorLength = 10;

        textPadding = 10;
        curValue = 5.5;
        itemValue = 0.01;
        curUnit = TopUtilBandWidthHz.UNIT_US;
        moveOffset = 0;

        lineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.small_scale);
        indicatorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.indicator_small_scale);
        Matrix matrix = new Matrix();
//        matrix.postScale(width * 3.0f / lineBitmap.getWidth(), height * 1.0f / lineBitmap.getHeight());
        matrix.postScale(1, 1);
        lineBitmap = Bitmap.createBitmap(lineBitmap, 0, 0, lineBitmap.getWidth(), lineBitmap.getHeight(), matrix, true);

        paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
        paint.setTextSize(20);

        scroller = new Scroller(context);
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        df2 = new DecimalFormat("###0.00", new DecimalFormatSymbols(Locale.CHINA));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS) && curValue < minNs) {
            curValue = minNs;
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS)) {
            curValue = 1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS;
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS))) {
            curValue = 1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS);
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS))) {
            curValue = 1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS);
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
        }
        else if (curUnit.equals(TopUtilBandWidthHz.UNIT_S) && curValue > (1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS)) {
            curValue = 1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS;
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
        }

        drawBg(canvas);
        drawRuler(canvas);
        drawIndicator(canvas);
    }

    private void drawIndicator(Canvas canvas) {
//        paint.setColor(indicatorColor);
//        Path path = new Path();
//        path.moveTo(width / 2 + indicatorLength, 0);
//        path.lineTo(width / 2 + indicatorLength, indicatorLength);
//        path.lineTo(width / 2, indicatorLength * 2);
//        path.lineTo(width / 2 - indicatorLength, indicatorLength);
//        path.lineTo(width / 2 - indicatorLength, 0);
//        path.close();
//        canvas.drawPath(path, paint);
        canvas.drawBitmap(indicatorBitmap, width / 2 - indicatorBitmap.getWidth() / 2, 1, paint);
    }

    private void drawRuler(Canvas canvas) {
        changeUnit();

        String left;
        String right;
        int leftOff = 0;
        int rightOff = 0;
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS)) {
            left = ((int) (curValue - itemValue * 20)) + curUnit;
            right = ((int) (curValue + itemValue * 20)) + curUnit;
            if (((int) (curValue - itemValue * 20)) < 0) {
                Logger.i("drawRuler,zeroOff:" + (int) (curValue - itemValue * 20) + "," + lastOffset);
                leftOff = (int) (((int) (itemValue * 20 - curValue)) * twoLinePx);
                left = 0 + curUnit;
                canvas.drawBitmap(lineBitmap, leftOff, 1, paint);
                leftOff = leftOff - getTextWidth(left) / 2;
            } else {
                Logger.i("drawRuler,zeroUnOff:" + (int) (curValue - itemValue * 20) + "," + lastOffset);
                canvas.drawBitmap(lineBitmap, -width + lastOffset, 1, paint);
            }
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_S)) {
            left = TBookUtil.getD3FromD(curValue - itemValue * 20) + curUnit;
            double maxS = 1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS;
            if (curValue + itemValue * 20 < maxS) {
                right = TBookUtil.getD3FromD(curValue + itemValue * 20) + curUnit;
                canvas.drawBitmap(lineBitmap, -width + lastOffset, 1, paint);
            } else {
                double rightLineValue = curValue + itemValue * 20;
                int offPx = (int) ((rightLineValue - maxS) / itemValue * twoLinePx+1);
                right = TBookUtil.getD3FromD(maxS) + "s";
                rightOff = -offPx;
                canvas.drawBitmap(lineBitmap, -width * 2 - offPx, 1, paint);
            }
        } else {
            left = TBookUtil.getD3FromD(curValue - itemValue * 20) + curUnit;
            right = TBookUtil.getD3FromD(curValue + itemValue * 20) + curUnit;
            canvas.drawBitmap(lineBitmap, -width + lastOffset, 1, paint);
        }

        paint.setColor(textColor);
        left = TopUtilBandWidthHz.getHzFromS(left);
        right = TopUtilBandWidthHz.getHzFromS(right);
        canvas.drawText(left, textPadding + leftOff, height - textPadding, paint);
        canvas.drawText(right, width - textPadding - getTextWidth(right) + rightOff, height - textPadding, paint);
    }

    private void drawBg(Canvas canvas) {
        paint.setColor(bgColor);
        canvas.drawRect(0, 0, width, height, paint);
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(1, 1, width - 1, height - 1, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null == velocityTracker) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
        final VelocityTracker verTracker = velocityTracker;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startValue = curValue;
                startX = event.getX();
                lastX = event.getX();
                //获取第一个触点
                pointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                moveOffset = event.getX() - lastX;
                lastX = event.getX();
                if (!checkCanMove()) {
                    return true;
                }
                curValue = getCurValueFromMoveOffset(event.getX() - startX);
                lastOffset = getLastOffsetFromMoveOffset(moveOffset);
                invalidate();
                onListener();
                break;
            case MotionEvent.ACTION_UP:
                if (!checkCanMove()) {
                    return true;
                }
                verTracker.computeCurrentVelocity(1000, maxVelocity);
                float velocityX = verTracker.getXVelocity(pointerId);
                if (Math.abs(velocityX) > 100) {//手指离开时大于一定速度的才会发生惯性滑动
                    velocityX = velocityX > 1000 ? 1000 : velocityX;
                    velocityX = velocityX < -1000 ? -1000 : velocityX;
                    startValue = curValue;
                    scroller.fling((int) lastX, (int) event.getY(), (int) velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                } else {
                    drawLastState();
                }
                releaseVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }

    boolean isLast = false;

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            isLast = true;
            moveOffset = scroller.getCurrX() - scroller.getStartX();
            curValue = getCurValueFromMoveOffset(moveOffset);
            lastOffset = getLastOffsetFromMoveOffset(moveOffset);
            invalidate();
            onListener();
        } else {
            if (isLast) {
                isLast = false;
                drawLastState();
            }
        }
        super.computeScroll();
    }

    private boolean checkCanMove() {
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS) && curValue < minNs && moveOffset > 0) {
            curValue = minNs;
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
            invalidate();
            onListener();
            return false;
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS) && moveOffset > 0) {
            curValue = 1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS;
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
            invalidate();
            onListener();
            return false;
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS)) && moveOffset > 0) {
            curValue = 1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS);
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
            invalidate();
            onListener();
            return false;
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS)) && moveOffset > 0) {
            curValue = 1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS);
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
            invalidate();
            onListener();
            return false;
        }else if (curUnit.equals(TopUtilBandWidthHz.UNIT_MS) && curValue < (1.0 * minNs / (1000 * TopUtilBandWidthHz.TIME_US2NS)) && moveOffset > 0) {
            curValue = 1.0 * minNs / (1000 * TopUtilBandWidthHz.TIME_US2NS);
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
            invalidate();
            onListener();
            return false;
        }
        else if (curUnit.equals(TopUtilBandWidthHz.UNIT_S) && curValue > (1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS) && moveOffset < 0) {
            curValue = 1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS;
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
            invalidate();
            onListener();
            return false;
        } else {
            return true;
        }
    }

    private void drawLastState() {
        if (checkCanMove()) {
            moveOffset = 0;
            lastOffset = getLastOffsetFromCurValue();
            invalidate();
            onListener();
        }
    }

    private double getCurValueFromMoveOffset(float moveOffset) {
        curValue = startValue + (-moveOffset / twoLinePx * itemValue);
        curValue = Double.parseDouble(df2.format(curValue));
//        Logger.i("getCurValueFromMoveOffset:" + moveOffset + "," + itemValue + "," + curValue);
        return curValue;
    }

    private float getLastOffsetFromMoveOffset(float moveOffset) {
        lastOffset += moveOffset;
        while (lastOffset >= twoLinePx * 5) {
            lastOffset -= (twoLinePx * 10);
        }
        while (lastOffset <= -twoLinePx * 5) {
            lastOffset += (twoLinePx * 10);
        }
        return lastOffset;
    }

    private int getLastOffsetFromCurValue() {
        int curValue = (int) (this.curValue * 100);
        int itemValue = (int) (this.itemValue * 100);
        int offsetNumber = curValue % (itemValue * 10) / itemValue;
        return (int) (-offsetNumber * twoLinePx);
    }

    private void releaseVelocityTracker() {
        if (null != velocityTracker) {
            velocityTracker.clear();
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void onListener() {
        if (onRulerChangedListener != null) {
            if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS)) {
                onRulerChangedListener.rulerChanged(String.valueOf((int) curValue), curUnit, itemValue);
            } else {
                onRulerChangedListener.rulerChanged(TBookUtil.getD3FromD(curValue), curUnit, itemValue);
            }
        }
    }

    public void setTimeRange(long minNs, long maxNs) {
        this.minNs = minNs;
        this.maxNs = maxNs;
    }

    public void setValue(double curNumber, String curUnit, double curItem) {
        this.curValue = curNumber;
        this.curUnit = curUnit;
        this.itemValue = curItem;
        moveOffset = 0;
        lastOffset = getLastOffsetFromCurValue();
        invalidate();
    }

    public void moveLeftOneStep() {
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS) && (curValue - itemValue) < minNs
        && minNs < TopUtilBandWidthHz.TIME_US2NS) {
            return;
        }
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && (curValue - itemValue) < 1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS
                && (minNs > TopUtilBandWidthHz.TIME_US2NS && minNs < 10 * TopUtilBandWidthHz.TIME_US2NS)) {
            return;
        }
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && (curValue - itemValue) < 1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS)
                && (minNs > 10 * TopUtilBandWidthHz.TIME_US2NS && minNs < 100 * TopUtilBandWidthHz.TIME_US2NS)) {
            return;
        }
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && (curValue - itemValue) < 1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS)
                && (minNs > 100 * TopUtilBandWidthHz.TIME_US2NS && minNs < TopUtilBandWidthHz.TIME_MS2NS)) {
            return;
        }
        curValue = curValue - itemValue;
        moveOffset = 0;
        lastOffset = getLastOffsetFromCurValue();
        changeUnit();
        invalidate();
        onListener();
    }

    public void moveRightOneStep() {
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_S) && (curValue + itemValue) > (1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS)) {
            return;
        }
        curValue = curValue + itemValue;
        moveOffset = 0;
        lastOffset = getLastOffsetFromCurValue();
        changeUnit();
        invalidate();
        onListener();
    }

    //改变当前的数据单位
    private void changeUnit() {
        long ns = TopUtilBandWidthHz.getNSFromValue(curValue + curUnit);
        TopUtilBandWidthHz.ScaleValue scaleValue = new TopUtilBandWidthHz().createScaleValue();
        TopUtilBandWidthHz.getValueFromNS(ns, scaleValue);
        if (itemValue != scaleValue.itemValue / 100) {
            this.curValue = scaleValue.value;
            this.curUnit = scaleValue.itemUnit;
            this.itemValue = scaleValue.itemValue / 100;
            startValue = curValue;
        }
    }

    //获取该字符串的宽度px
    private int getTextWidth(String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }
}
