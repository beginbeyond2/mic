package com.micsig.tbook.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

//import com.micsig.base.Logger;

/**
 * Created by liwb on 2017/3/31.
 */

public class MButton_CheckBox extends View {
    private final static String TAG="MButton_CheckBox";
    private TextPaint textPaint = null;
    StaticLayout layout = null;

    private Bitmap checkBitmap = null, unCheckBitmap = null;
    String text = null;
    int checkTextColor, unCheckTextColor;
    float textSize;

    private boolean enable=true;
    protected boolean checked = false;
    private int text_x = -1, text_y = -1;
    private boolean textCenterX = false;
    private OnClickListener onClickListener = null;

    public MButton_CheckBox(Context context) {
        this(context, null);
    }

    public MButton_CheckBox(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MButton_CheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MButton_CheckBox, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.MButton_CheckBox_checkedBitmap) {
                checkBitmap = ((BitmapDrawable) a.getDrawable(attr)).getBitmap();
            } else if (attr == R.styleable.MButton_CheckBox_unCheckedBitmap) {
                unCheckBitmap = ((BitmapDrawable) a.getDrawable(attr)).getBitmap();
            } else if (attr == R.styleable.MButton_android_text) {
                text = a.getString(attr);
            } else if (attr == R.styleable.MButton_android_textColor) {
                unCheckTextColor = a.getColor(attr, 0xFFFFFF);
            } else if (attr == R.styleable.MButton_CheckBox_checkedTextColor) {
                checkTextColor = a.getColor(attr, 0xff000000);
            } else if (attr == R.styleable.MButton_CheckBox_unCheckedTextColor) {
                unCheckTextColor = a.getColor(attr, 0xFFFFFF);
            } else if (attr == R.styleable.MButton_android_textSize) {
                textSize = a.getDimension(attr, 16);
            } else if (attr == R.styleable.MButton_CheckBox_text_x) {
                text_x = (int) a.getDimension(attr, -1);
            } else if (attr == R.styleable.MButton_CheckBox_text_y) {
                text_y = (int) a.getDimension(attr, -1);
            } else if (attr == R.styleable.MButton_CheckBox_textCenterX) {
                textCenterX = a.getBoolean(attr, false);
            }
        }
        a.recycle();

        textPaint = new TextPaint();
        textPaint.setColor(unCheckTextColor);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);

        initLayout();
    }

    private void initLayout() {
        if (unCheckBitmap != null && text != null) {
            if (checked) {
                textPaint.setColor(checkTextColor);
            } else {
                textPaint.setColor(unCheckTextColor);
            }
            layout = new StaticLayout(text, textPaint, unCheckBitmap.getWidth(),
                    Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
            if (text_x == -1) {
                text_x = (unCheckBitmap.getWidth() - layout.getWidth()) / 2;

            }
            if (text_y == -1) {
                text_y = (unCheckBitmap.getHeight() - layout.getHeight()) / 2;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (checkBitmap != null || unCheckBitmap != null) {
            Rect des, src;
            int center=(this.getWidth()-checkBitmap.getWidth())/2;
            src=new Rect(0, 0, checkBitmap.getWidth(), checkBitmap.getHeight());
            des = new Rect(center, 0, checkBitmap.getWidth()+center, checkBitmap.getHeight());

            if (checked) {
                canvas.drawBitmap(checkBitmap, src, des, null);
            } else {
                canvas.drawBitmap(unCheckBitmap, src, des, null);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                //处理点击事件
                if (getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY()) && enable) {
                    onSingleClick();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }

    protected void onSingleClick() {
        checked = !checked;
        if (onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        enable=enabled;
    }

    private Rect getViewInScreen() {
        int[] location = new int[2];
        this.getLocationOnScreen(location);
        return new Rect(location[0], location[1], this.getWidth() + location[0], this.getHeight() + location[1]);
    }

    Rect rect = new Rect();

    private int getTextWidth(String text) {
        textPaint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        if (!enable) return;
        this.checked = checked;
        invalidate();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        initLayout();
        invalidate();
    }

    public void setTextSize(int size) {
        textPaint.setTextSize(size);
        initLayout();
        invalidate();
    }

    public void setText_y(int text_y) {
        this.text_y = text_y;
        initLayout();
        invalidate();
    }

    public void setText_x(int text_x) {
        this.text_x = text_x;
        initLayout();
        invalidate();
    }

    public void setCheckBitmap(Bitmap checkBitmap) {
        this.checkBitmap = checkBitmap;
        invalidate();
    }

    public void setUnCheckBitmap(Bitmap unCheckBitmap) {
        this.unCheckBitmap = unCheckBitmap;
        invalidate();
    }
}
