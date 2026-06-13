package com.micsig.tbook.ui.util.svg;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import androidx.core.content.res.ColorStateListInflaterCompat;

import com.micsig.tbook.ui.wavezone.TChan;

public class SelectorUtil {

    public static int STATE_PRESSED = android.R.attr.state_pressed;
    public static int STATE_FOCUSED = android.R.attr.state_focused;
    public static int STATE_ENABLED = android.R.attr.state_enabled;
    public static int STATE_CHECKED = android.R.attr.state_checked;
    private static int WIDTH_TOP_CHANNEL = 20;
    private static int HEIGHT_TOP_CHANNEL = 20;

    //通道单选 按钮
    public static Drawable createCheckedDrawable(int chIndex) {
        StateListDrawable drawable = new StateListDrawable();
        Bitmap checked = SvgManager.createSvg(SvgNodeInfo.getSCPaths(), SvgNodeInfo.getSCColors(chIndex), WIDTH_TOP_CHANNEL, HEIGHT_TOP_CHANNEL);
        Bitmap unchecked = SvgManager.createSvg(SvgNodeInfo.getSCUncheckPaths(), SvgNodeInfo.getSCUncheckColors(), WIDTH_TOP_CHANNEL, HEIGHT_TOP_CHANNEL);
        drawable.addState(new int[]{STATE_CHECKED}, new BitmapDrawable(Resources.getSystem(), checked));
        drawable.addState(new int[]{-STATE_CHECKED}, new BitmapDrawable(Resources.getSystem(), unchecked));
        return drawable;
    }

    //通道多选 按钮
    public static Drawable createMuChoiceDrawable(int chIndex) {
        StateListDrawable drawable = new StateListDrawable();
        Bitmap checked = SvgManager.createDuiHaoSvg(chIndex, WIDTH_TOP_CHANNEL, HEIGHT_TOP_CHANNEL, false);
        Bitmap unchecked = SvgManager.createDuiHaoSvg(chIndex, WIDTH_TOP_CHANNEL, HEIGHT_TOP_CHANNEL, true);
        drawable.addState(new int[]{STATE_CHECKED}, new BitmapDrawable(Resources.getSystem(), checked));
        drawable.addState(new int[]{-STATE_CHECKED}, new BitmapDrawable(Resources.getSystem(), unchecked));
        return drawable;
    }

    private static final int[][] RadioButtonStateList = new int[][]{
            new int[]{-SelectorUtil.STATE_ENABLED},
            new int[]{SelectorUtil.STATE_PRESSED},
            new int[]{SelectorUtil.STATE_CHECKED},
            new int[]{}
    };

    public static ColorStateList createColorStateList(int chIndex) {
        int[] colors = new int[]{
                Color.parseColor(SvgNodeInfo.getColorViewDisable()),
                SvgNodeInfo.getAllBaseColorInt(chIndex),
                SvgNodeInfo.getAllBaseColorInt(chIndex),
                SvgNodeInfo.getAllBaseColorInt(TChan.NULL),
        };
        return new ColorStateList(RadioButtonStateList, colors);
    }


}
