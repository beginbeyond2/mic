package com.micsig.tbook.ui.top.view.scale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.micsig.tbook.ui.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by yangj on 2017/4/14.
 */

public class TopViewScaleLarge extends View {
    private static final String[] RULERNAME = {"0", "1μs", "10μs", "100μs", "1ms", "10ms", "100ms", "1s", "10s"};

    private Context context;
    private Paint paint;
    private int bgColor;
    private int lineColor;
    private int textColor;
    private int height;
    private int width;

    private int strokeWidth;
    private int bottomPadding;
    private int longLineHeight;
    private int middleLineHeight;
    private int shortLineHeight;
    private float twoLineInterval;
    private float pointX;

    private long minNs = TopUtilScale.DEFAULT_MIN_TIME;
    private long maxNs = TopUtilScale.DEFAULT_MAX_TIME;

    private OnRulerChangedListener onRulerChangedListener;

    public interface OnRulerChangedListener {
        void rulerChanged(double value, String unit, double item);
    }

    public OnRulerChangedListener getOnRulerChangedListener() {
        return onRulerChangedListener;
    }

    public void setOnRulerChangedListener(OnRulerChangedListener onRulerChangedListener) {
        this.onRulerChangedListener = onRulerChangedListener;
    }

    public TopViewScaleLarge(Context context) {
        this(context, null);
    }

    public TopViewScaleLarge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewScaleLarge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        bgColor = getResources().getColor(R.color.bg_slip_backcolor);
        lineColor = getResources().getColor(R.color.scaleDivider);
        textColor = getResources().getColor(R.color.textColorTop);

        strokeWidth = 1;
        bottomPadding = 10;
        longLineHeight = 60;
        middleLineHeight = 30;
        shortLineHeight = 20;
        twoLineInterval = 11f;

        paint = new Paint();
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setTextSize(20);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
        int result = (int) (twoLineInterval * 73);
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, measureSize);
                break;
        }
        width = result;
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int measureMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureSize = MeasureSpec.getSize(heightMeasureSpec);
        int result = longLineHeight * 2;
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, measureSize);
                break;
        }
        height = result;
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        drawRuler(canvas);
        drawProgress(canvas);
    }

    private void drawProgress(Canvas canvas) {
        paint.setColor(Color.argb(0x99, Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
        canvas.drawRect(1, 1, pointX == 0 ? 2 : pointX, height, paint);
    }

    private void drawRuler(Canvas canvas) {
        paint.setColor(lineColor);
        for (int i = 0; i < 72; i++) {
            float x = twoLineInterval * (i + 1);
            if (i % 9 == 0 && i != 0) {
                canvas.drawLine(x, 1, x, longLineHeight, paint);
            } else if (i % 9 == 4) {
                canvas.drawLine(x, 1, x, middleLineHeight, paint);
            } else {
                canvas.drawLine(x, 1, x, shortLineHeight, paint);
            }
        }
        paint.setColor(textColor);
        int j = 0;
        for (int i = 0; i < 72; i++) {
            float x = twoLineInterval * (i + 1);
            if (i % 9 == 0 && i != 0) {
                String text = RULERNAME[++j];
                canvas.drawText(text, x - getTextWidth(text) / 2, height - bottomPadding, paint);
            }
        }
        canvas.drawText(RULERNAME[0], bottomPadding, height - bottomPadding, paint);
        String lastText = RULERNAME[RULERNAME.length - 1];
        canvas.drawText(lastText, width - getTextWidth(lastText) - bottomPadding, height - bottomPadding, paint);
    }


    private int getTextWidth(String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                pointX = checkRange(event.getX());
                onListener();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                pointX = checkRange(event.getX() - event.getX() % (twoLineInterval / 2));
                onListener();
                invalidate();
                break;
        }
        return true;
    }

    private void onListener() {
        if (onRulerChangedListener != null) {
            float px = pointX - pointX % (twoLineInterval / 2);
            if (px < 0) {

            } else if (px < twoLineInterval * 10) {
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 0, 50, 0), TopUtilScale.UNIT_NS, 100);
            } else if (px < twoLineInterval * 19) {
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 10, 0.5, 1), TopUtilScale.UNIT_US, 1);
            } else if (px < twoLineInterval * 28) {
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 19, 5, 10), TopUtilScale.UNIT_US, 10);
            } else if (px < twoLineInterval * 37) {
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 28, 50, 100), TopUtilScale.UNIT_US, 100);
            } else if (px < twoLineInterval * 46) {
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 37, 0.5, 1), TopUtilScale.UNIT_MS, 1);
            } else if (px < twoLineInterval * 55) {
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 46, 5, 10), TopUtilScale.UNIT_MS, 10);
            } else if (px < twoLineInterval * 64) {
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 55, 50, 100), TopUtilScale.UNIT_MS, 100);
            } else if (px <= twoLineInterval * 73) {
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 64, 0.5, 1), TopUtilScale.UNIT_S, 1);
            }
        }
    }

    public void moveLeftOneStep() {
        pointX = checkRange(pointX - twoLineInterval / 2);
        onListener();
        invalidate();
    }

    public void moveRightOneStep() {
        pointX = checkRange(pointX + twoLineInterval / 2);
        onListener();
        invalidate();
    }

    public void setTimeRange(long minNs, long maxNs) {
        this.minNs = minNs;
        this.maxNs = maxNs;
    }

    public void setValue(double curValue, String itemUnit, double itemValue) {
        float px = 0;
        if (TopUtilScale.UNIT_NS.equals(itemUnit) && itemValue == 100) {
            px = getPxFromValue(curValue, 0, 50, 0);
        } else if (TopUtilScale.UNIT_US.equals(itemUnit) && itemValue == 1) {
            px = getPxFromValue(curValue, 1, 0.5, 10);
        } else if (TopUtilScale.UNIT_US.equals(itemUnit) && itemValue == 10) {
            px = getPxFromValue(curValue, 10, 5, 19);
        } else if (TopUtilScale.UNIT_US.equals(itemUnit) && itemValue == 100) {
            px = getPxFromValue(curValue, 100, 50, 28);
        } else if (TopUtilScale.UNIT_MS.equals(itemUnit) && itemValue == 1) {
            px = getPxFromValue(curValue, 1, 0.5, 37);
        } else if (TopUtilScale.UNIT_MS.equals(itemUnit) && itemValue == 10) {
            px = getPxFromValue(curValue, 10, 5, 46);
        } else if (TopUtilScale.UNIT_MS.equals(itemUnit) && itemValue == 100) {
            px = getPxFromValue(curValue, 100, 50, 55);
        } else if (TopUtilScale.UNIT_S.equals(itemUnit) && itemValue == 1) {
            px = getPxFromValue(curValue, 1, 0.5, 64);
        }
        if (px != 0) {
            pointX = checkRange(px - px % (twoLineInterval / 2));
            invalidate();
        }
    }

    private float checkRange(float willPointX) {
        if (minNs < TopUtilScale.TIME_US2NS) {//最小值小于1us
            long minPointX = (long) (minNs / 50 * (twoLineInterval / 2));
            pointX = Math.max(willPointX, minPointX);
        }
        if (maxNs > TopUtilScale.TIME_S2NS) {//最大值大于1s
            long maxPointX = (long) ((maxNs / (TopUtilScale.TIME_S2NS / 2) - 2 + 64 * 2) * (twoLineInterval / 2));
            pointX = Math.min(pointX, maxPointX);
        }
        return pointX;
    }

    private float getPxFromValue(double value, int headValue, double halfItemValue, int beforeHeadCount) {
        return (float) (((value - headValue) / halfItemValue + beforeHeadCount * 2) * (twoLineInterval / 2));
    }

    DecimalFormat df = new DecimalFormat("###0.00", new DecimalFormatSymbols(Locale.CHINA));

    private double getValueFromPx(float px, int beforeHeadCount, double haifItemValue, int headValue) {
        return Double.parseDouble(df.format((px / (twoLineInterval / 2) - beforeHeadCount * 2) * haifItemValue + headValue));
    }
}
