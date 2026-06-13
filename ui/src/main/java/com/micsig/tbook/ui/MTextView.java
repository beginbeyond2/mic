package com.micsig.tbook.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.micsig.tbook.ui.util.ScreenUtil;

public class MTextView extends AppCompatTextView {
    public MTextView(Context context) {
        this(context, null);
    }

    public MTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        String text = getText().toString();
        TextPaint paint = getPaint();
        paint.setColor(getTextColors().getDefaultColor());
        int measureWidth = getMeasuredWidth();
        int measureHeight = getMeasuredHeight();
        int textWidth = ScreenUtil.getTextWidth3(paint, text);
        int textHeight = ScreenUtil.getTextHeight(paint, text);
        if (text.contains("\n")) {
            String[] strings = text.split("\n");
            int itemHeight = ScreenUtil.getTextHeight(paint, "Sp");
            for (int i = 0; i < strings.length; i++) {
                textWidth = ScreenUtil.getTextWidth3(paint, strings[i]);
                canvas.drawText(strings[i], measureWidth - textWidth, measureHeight / 2 + itemHeight / 2 * (-1 * strings.length + (i + 1) * 2) + i * 5 - 5, paint);
            }
        } else {
            canvas.drawText(text, measureWidth - textWidth, measureHeight / 2 + textHeight / 2, paint);
        }
    }
}
