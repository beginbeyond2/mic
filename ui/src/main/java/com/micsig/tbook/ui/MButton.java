package com.micsig.tbook.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;


/**
 * Created by liwb on 2017/3/30.
 */

public class MButton extends View {
    StaticLayout layout = null;
    TextPaint textPaint = null;
    Bitmap touchDownBitmap = null;
    Bitmap touchUpBitmap = null;
    String text = null;
    int textColor;
    float textSize;
    boolean mState = false;
    private int text_x, text_y;
    private boolean textCenterX = false;
    private OnClickListener onClickListener = null;


    public MButton(Context context) {
        this(context, null);
    }

    public MButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MButton, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.MButton_touchDownBitmap) {
                touchDownBitmap = ((BitmapDrawable) a.getDrawable(attr)).getBitmap();
            } else if (attr == R.styleable.MButton_touchUpBitmap) {
                touchUpBitmap = ((BitmapDrawable) a.getDrawable(attr)).getBitmap();
            } else if (attr == R.styleable.MButton_android_text) {
                text = a.getString(attr);
            } else if (attr == R.styleable.MButton_android_textColor) {
                textColor = a.getColor(attr, 0xFFFFFF);
            } else if (attr == R.styleable.MButton_android_textSize) {
                textSize = a.getDimension(attr, 16);
            } else if (attr == R.styleable.MButton_text_x) {
                text_x = (int) a.getDimension(attr, 16);
            } else if (attr == R.styleable.MButton_text_y) {
                text_y = (int) a.getDimension(attr, 16);
            } else if (attr == R.styleable.MButton_textCenterX) {
                textCenterX = a.getBoolean(attr, false);
            }

        }
        a.recycle();

        textPaint = new TextPaint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
    }

    private void initLayout() {
        if (touchUpBitmap != null && text != null)
            layout = new StaticLayout(text, textPaint, touchUpBitmap.getWidth(),
                    Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    private Rect src = new Rect();
    private Rect des;

    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
    private Paint paint=new Paint();
    @Override
    protected void onDraw(Canvas canvas) {
        if (touchDownBitmap != null || touchUpBitmap != null) {
//            Rect des, src;
//            des = new Rect(0, 0, touchDownBitmap.getWidth(), touchDownBitmap.getHeight());
            paint.setXfermode(clearMode);
            canvas.drawPaint(paint);
            paint.setXfermode(srcMode);
            src.set(0, 0, touchDownBitmap.getWidth(), touchDownBitmap.getHeight());
            des = src;
            if (mState) {
                canvas.drawBitmap(touchDownBitmap, src, des, paint);
            } else {
                canvas.drawBitmap(touchUpBitmap, src, des, paint);
            }

            if (text != null) {
                initLayout();
                if (textCenterX) {
                    if (layout.getParagraphDirection(0) == Layout.DIR_RIGHT_TO_LEFT) {
                        text_x = (getTextWidth(text) - getMeasuredWidth()) / 2;
                    } else {
                        text_x = (getMeasuredWidth() - getTextWidth(text)) / 2;
                    }
                }
                canvas.save();
                canvas.translate(text_x, text_y);
                layout.draw(canvas);
                canvas.restore();
            }
        }
    }

    private int downx, downy;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downx = (int) event.getRawX();
                downy = (int) event.getRawY();
                mState = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mState = false;
                invalidate();
                //处理点击事件
                if (getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY())) {
                    if (onClickListener != null) {
                        onClickListener.onClick(this);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mState = false;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY())
                        || (Math.abs(downx - (int) event.getRawX()) > 20 || Math.abs(downy - (int) event.getRawY()) > 20)
                ) {
                    mState = false;
                    invalidate();
                }
                break;
        }
        return true;
    }

    public void setTextSize(int textSize) {
        textPaint.setTextSize(textSize);
        initLayout();
        invalidate();
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private Rect getViewInScreen() {
        int[] location = new int[2];
        this.getLocationOnScreen(location);
        return new Rect(location[0], location[1], this.getWidth() + location[0], this.getHeight() + location[1]);
    }

    Rect rect = new Rect();

    private int getTextWidth(String text) {
        String[] split = text.split("\\n");
        int w = 0;
        for (String s : split) {
            textPaint.getTextBounds(s, 0, s.length(), rect);
            w = Math.max(w, rect.width());
        }
        return w;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void setTouchDownBitmap(Bitmap touchDownBitmap) {
        this.touchDownBitmap = touchDownBitmap;
    }

    public void setTouchUpBitmap(Bitmap touchUpBitmap) {
        this.touchUpBitmap = touchUpBitmap;
    }
}
