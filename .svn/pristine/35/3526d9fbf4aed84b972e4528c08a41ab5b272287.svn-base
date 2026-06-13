package com.micsig.tbook.ui.util;

import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.micsig.base.Logger;

import java.util.Arrays;

public class ScreenUtil {
    public static Rect getViewLocation(View view) {
        int[] ints = new int[2];
        view.getLocationOnScreen(ints);
        Logger.i("view:" + Arrays.toString(ints) + "\t" + view.getWidth() + "\t" + view.getHeight());
        return new Rect(ints[0], ints[1], view.getWidth(), view.getHeight());
    }

    static Rect rect = new Rect();

    //计算文字所在矩形，可以得到宽高
    public static int getTextWidth(Paint paint, String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }

    //精确计算文字宽度
    public static int getTextWidth2(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    // 粗略计算文字宽度
    public static int getTextWidth3(Paint paint, String text) {
        return (int) paint.measureText(text);
    }

    public static int getTextHeight(Paint paint, String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return h;
    }
}
