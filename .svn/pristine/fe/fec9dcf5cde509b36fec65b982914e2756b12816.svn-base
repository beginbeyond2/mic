package com.micsig.tbook.tbookscope.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * @Description: java类作用描述
 * @Author: micsig
 * @CreateDate: 2024/4/29 9:26
 */
public class ViewUtils {
    private static final long diffTime = 500;
    private long lastClickTime;
    private int lastClickViewId;
    private static volatile ViewUtils instance;
    private static int dialogFloatOffset = 0;
    private static int dialogFormulaOffset = 0;
    private static int dialogNumberOffset = 0;
    private static int mathRefBusOffset = -3;

    public static ViewUtils getInstance() {
        if (instance == null) {
            synchronized (ViewUtils.class) {
                if (instance == null) {
                    instance = new ViewUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 判断两次点击的间隔，如果两次点击小于500毫秒，则认为是多次无效点击
     */
    public boolean isFastDoubleClick(int viewId) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < lastClickTime && 0 < timeD && lastClickViewId == viewId && timeD < diffTime) {
            return true;
        } else {
            lastClickTime = time;
            lastClickViewId = viewId;
            return false;
        }
    }

    /**
     * 在视图布局完成后执行一次性操作
     * @param view     目标视图
     * @param runnable 布局完成后的回调
     */
    public static void doAfterLayout(final View view, final Runnable runnable) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 确保移除监听器
                removeListener(observer, this);
                // 执行回调
                runnable.run();
            }
        });
    }

    /**
     * 移除监听器（兼容不同 API 版本）
     */
    private static void removeListener(ViewTreeObserver observer, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (observer.isAlive()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                observer.removeOnGlobalLayoutListener(listener);
            } else {
                observer.removeGlobalOnLayoutListener(listener);
            }
        }
    }

    public static Bitmap createBitmapFromView(View view) {
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        view.draw(canvas);
        return bitmap;
    }


    public static int getDialogFloatOffset() {
        return dialogFloatOffset;
    }

    public static void setDialogFloatOffset(int dialogFloatOffset) {
        ViewUtils.dialogFloatOffset = dialogFloatOffset;
    }

    public static int getDialogFormulaOffset() {
        return dialogFormulaOffset;
    }

    public static void setDialogFormulaOffset(int dialogFormulaOffset) {
        ViewUtils.dialogFormulaOffset = dialogFormulaOffset;
    }

    public static int getDialogNumberOffset() {
        return dialogNumberOffset;
    }

    public static void setDialogNumberOffset(int dialogNumberOffset) {
        ViewUtils.dialogNumberOffset = dialogNumberOffset;
    }

    public static int getMathRefBusOffset() {
        return mathRefBusOffset;
    }

    public static void setMathRefBusOffset(int mathRefBusOffset) {
        ViewUtils.mathRefBusOffset = mathRefBusOffset;
    }
}