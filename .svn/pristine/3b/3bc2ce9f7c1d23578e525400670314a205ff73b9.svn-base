package com.micsig.tbook.ui.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.VectorDrawable;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.wavezone.TChan;

public class BitmapUtil {
    /**
     * 根据给定的宽和高进行拉伸
     *
     * @param origin    原图
     * @param newWidth  新图的宽
     * @param newHeight 新图的高
     * @return new Bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 裁剪
     *
     * @param bitmap 原图
     * @return 裁剪后的图像
     */
    public static Bitmap cropBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int cropWidth = w >= h ? h : w;// 裁切后所取的正方形区域边长
        cropWidth /= 2;
        int cropHeight = (int) (cropWidth / 1.2);
        return Bitmap.createBitmap(bitmap, w / 3, 0, cropWidth, cropHeight, null, false);
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 偏移效果
     * @param origin 原图
     * @return 偏移后的bitmap
     */
    public static Bitmap skewBitmap(Bitmap origin) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.postSkew(-0.6f, -0.3f);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 根据drawable获得bitmap...
     * @param context
     * @param drawableId
     * @return
     */
    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable
//                || drawable instanceof VectorDrawableCompat
        ) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId, int width, int height) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable
//                || drawable instanceof VectorDrawableCompat
        ) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    public static Bitmap getBitmapFromDrawable(Context context,@DrawableRes int resId,int width,int height,int color){
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable
//                || drawable instanceof VectorDrawableCompat
        ) {
            ((VectorDrawable)drawable).setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
    public static Drawable getSvgDrawable(Context context,@DrawableRes int resId,int color){
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        ((VectorDrawable)drawable).setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        return drawable;
    }
    /**
     * 根据drawable获得bitmap...
     * @param context
     * @return
     */
    public static Bitmap getBitmapFromDrawable(Context context, Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable
//                || drawable instanceof VectorDrawableCompat
        ) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
    public static Drawable getDrawable(Context context,int resId,int color){
        Drawable drawable= context.getResources().getDrawable(resId);
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        return drawable;
    }


    public static ColorStateList genSelectorColor(Context context,int chIdx){
        int[][] states=new int[4][];
        states[0]=new int[]{-android.R.attr.state_enabled};
        states[1]=new int[]{android.R.attr.state_checked};
        states[2]=new int[]{android.R.attr.state_pressed};
        states[3]=new int[]{};

        int checkedColor= TChan.getChannelColor(context,chIdx);
        int enableColor=context.getResources().getColor(R.color.textColorNewRightViewDisable);
        int normalColor=context.getResources().getColor(R.color.textColorNewRightViewEnable);
        int[] colors=new int[]{enableColor,checkedColor,checkedColor,normalColor};
        ColorStateList itemTextColorResId=new ColorStateList(states,colors);
        return itemTextColorResId;
    }
    public static StateListDrawable genSelectorDrawable(Context context,int chIdx){
        int checkedColor= TChan.getChannelColor(context,chIdx);

        Drawable drawable= context.getResources().getDrawable(R.drawable.layer_button_select_s2);
        Drawable d1= ((LayerDrawable)drawable).getDrawable(0);
        d1.setTint(checkedColor);

         StateListDrawable  itemBgViewResId=new StateListDrawable();
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked, -android.R.attr.state_enabled},context.getResources().getDrawable(R.drawable.layer_button_disable));
        itemBgViewResId.addState(new int[]{-android.R.attr.state_enabled}, context.getResources().getDrawable(R.drawable.layer_button_disable));
        itemBgViewResId.addState(new int[]{android.R.attr.state_selected},drawable);
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked },drawable);
        itemBgViewResId.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_window_focused},drawable);
        itemBgViewResId.addState(new int[0],context.getResources().getDrawable(R.drawable.layer_button_normal));
        return itemBgViewResId;
    }

    public static StateListDrawable genSelectorLeftDrawable(Context context,int chIdx){
        int checkedColor= TChan.getChannelColor(context,chIdx);
        Drawable drawable =getSvgDrawable(context,R.drawable.svg_semicircle_left_select_ch1,checkedColor);

        StateListDrawable  itemBgViewResId=new StateListDrawable();
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked, -android.R.attr.state_enabled},context.getResources().getDrawable(R.drawable.svg_semicircle_left_disable));
        itemBgViewResId.addState(new int[]{-android.R.attr.state_enabled}, context.getResources().getDrawable(R.drawable.svg_semicircle_left_disable));
        itemBgViewResId.addState(new int[]{android.R.attr.state_selected},drawable);
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked },drawable);
        itemBgViewResId.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_window_focused},drawable);
        itemBgViewResId.addState(new int[0],context.getResources().getDrawable(R.drawable.svg_semicircle_left));
        return itemBgViewResId;
    }

    public static StateListDrawable genSelectorRightDrawable(Context context,int chIdx){
        int checkedColor= TChan.getChannelColor(context,chIdx);
        Drawable drawable =getSvgDrawable(context,R.drawable.svg_semicircle_right_select_ch1,checkedColor);

        StateListDrawable  itemBgViewResId=new StateListDrawable();
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked, -android.R.attr.state_enabled},context.getResources().getDrawable(R.drawable.svg_semicircle_right_disable));
        itemBgViewResId.addState(new int[]{-android.R.attr.state_enabled}, context.getResources().getDrawable(R.drawable.svg_semicircle_right_disable));
        itemBgViewResId.addState(new int[]{android.R.attr.state_selected},drawable);
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked },drawable);
        itemBgViewResId.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_window_focused},drawable);
        itemBgViewResId.addState(new int[0],context.getResources().getDrawable(R.drawable.svg_semicircle_right));
        return itemBgViewResId;
    }


}
