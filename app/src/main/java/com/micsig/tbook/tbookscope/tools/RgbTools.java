package com.micsig.tbook.tbookscope.tools;

import android.graphics.Color;
/**
 * Created by liwb on 2019/4/3.
 */
public class RgbTools {
    public static final int H_BlackOrWhite=-1;
    public static final int H_Yellow=60;
    public static final int H_Red=0;
    public static final int H_Green=120;
    public static final int H_Blue=240;
    public static final int H_Cyan=180;
    public static final int H_Magenta=300;


    public static int rgb2Hsv_H(int color) {
        int imax, imin, diff;

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        imax = rgb2Hsv_max(color);
        imin = rgb2Hsv_min(color);
        diff = imax - imin;
        int v = imax;
        int s = 0;
        int h = 0;
        if (imax == 0) {
            s = 0;
        } else {
            s = diff;
        }
        if (diff != 0) {
            if (r == imax) {
                h = 60 * (g - b) / diff;
            } else if (g == imax) {
                h = 60 * (b - r) / diff + 120;
            } else {
                h = 60 * (r - g) / diff + 240;
            }

            if (h < 0) {
                h = h + 360;
            }
        } else {
            h = -1;
        }
        return h;

    }

    private static int rgb2Hsv_max(int color) {
        int a, b, c;
        a = Color.red(color);
        b = Color.green(color);
        c = Color.blue(color);
        int max = a;
        if (b > max) max = b;
        if (c > max) max = c;
        return max;
    }

    private static int rgb2Hsv_min(int color) {
        int a, b, c;
        a = Color.red(color);
        b = Color.green(color);
        c = Color.blue(color);
        int min = a;
        if (b < min) min = b;
        if (c < min) min = c;
        return min;
    }
}
