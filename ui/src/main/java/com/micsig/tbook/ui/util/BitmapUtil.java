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

/**
 * 位图工具类
 * 
 * <p>提供位图处理和Drawable转换的核心功能，包括：</p>
 * <ul>
 *   <li>位图缩放：按尺寸或比例缩放</li>
 *   <li>位图裁剪：裁剪为指定区域</li>
 *   <li>位图旋转：按角度旋转图像</li>
 *   <li>位图偏移：应用倾斜变换</li>
 *   <li>Drawable转换：将Drawable转换为Bitmap</li>
 *   <li>选择器生成：生成带通道颜色的状态选择器</li>
 * </ul>
 * 
 * <p>该类主要用于示波器UI界面中的图形处理，支持VectorDrawable和BitmapDrawable两种类型。</p>
 * 
 * @author Micsig Technology
 * @version 1.0
 * @since 2024
 */
public class BitmapUtil {
    
    /**
     * 根据给定的宽和高进行拉伸
     * 
     * <p>将原始位图缩放到指定的宽度和高度，使用Matrix进行变换。</p>
     * <p>注意：此方法会回收原始位图资源。</p>
     *
     * @param origin    原始位图，如果为null则返回null
     * @param newWidth  新位图的宽度（像素）
     * @param newHeight 新位图的高度（像素）
     * @return 缩放后的新位图，如果原始位图为null则返回null
     * 
     * @see Matrix#postScale(float, float) 使用后乘方式进行缩放变换
     */
    public static Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        // 空值检查，避免空指针异常
        if (origin == null) {
            return null;
        }
        
        // 获取原始位图的尺寸
        int height = origin.getHeight();  // 原始高度
        int width = origin.getWidth();    // 原始宽度
        
        // 计算宽度和高度的缩放比例
        float scaleWidth = ((float) newWidth) / width;   // 宽度缩放比例
        float scaleHeight = ((float) newHeight) / height; // 高度缩放比例
        
        // 创建变换矩阵并应用缩放
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight); // 使用后乘方式应用缩放变换
        
        // 根据变换矩阵创建新的位图
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        
        // 回收原始位图资源，释放内存
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        
        return newBM;
    }

    /**
     * 按比例缩放图片
     * 
     * <p>使用相同的比例缩放宽度和高度，保持图像的宽高比。</p>
     * <p>注意：如果新位图与原始位图相同，则不会回收原始位图。</p>
     *
     * @param origin 原始位图，如果为null则返回null
     * @param ratio  缩放比例，1.0表示原始大小，小于1缩小，大于1放大
     * @return 缩放后的新位图，如果原始位图为null则返回null
     * 
     * @see Matrix#preScale(float, float) 使用前乘方式进行缩放变换
     */
    public static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        // 空值检查，避免空指针异常
        if (origin == null) {
            return null;
        }
        
        // 获取原始位图的尺寸
        int width = origin.getWidth();   // 原始宽度
        int height = origin.getHeight(); // 原始高度
        
        // 创建变换矩阵并应用等比例缩放
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio); // 使用前乘方式应用等比例缩放
        
        // 根据变换矩阵创建新的位图
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        
        // 如果新位图与原始位图相同，直接返回（避免回收）
        if (newBM.equals(origin)) {
            return newBM;
        }
        
        // 回收原始位图资源
        origin.recycle();
        
        return newBM;
    }

    /**
     * 裁剪位图
     * 
     * <p>从原始位图中裁剪出一个矩形区域。</p>
     * <p>裁剪区域的位置和大小根据原始位图的尺寸自动计算：</p>
     * <ul>
     *   <li>裁剪宽度：取宽高中的较小值的一半</li>
     *   <li>裁剪高度：裁剪宽度的1/1.2</li>
     *   <li>起始X位置：宽度的1/3处</li>
     *   <li>起始Y位置：0</li>
     * </ul>
     *
     * @param bitmap 原始位图
     * @return 裁剪后的位图
     */
    public static Bitmap cropBitmap(Bitmap bitmap) {
        // 获取原始位图的宽度和高度
        int w = bitmap.getWidth();  // 位图宽度
        int h = bitmap.getHeight(); // 位图高度
        
        // 计算裁切后所取的正方形区域边长（取宽高中的较小值）
        int cropWidth = w >= h ? h : w;
        cropWidth /= 2; // 裁剪宽度为较小值的一半
        
        // 计算裁剪高度（按1.2的比例）
        int cropHeight = (int) (cropWidth / 1.2);
        
        // 从指定位置裁剪位图：起始位置为(w/3, 0)
        return Bitmap.createBitmap(bitmap, w / 3, 0, cropWidth, cropHeight, null, false);
    }

    /**
     * 旋转变换
     * 
     * <p>将原始位图围绕原点旋转指定角度。</p>
     * <p>注意：如果新位图与原始位图相同，则不会回收原始位图。</p>
     *
     * @param origin 原始位图，如果为null则返回null
     * @param alpha  旋转角度（度），可正可负，正值顺时针旋转，负值逆时针旋转
     * @return 旋转后的位图，如果原始位图为null则返回null
     * 
     * @see Matrix#setRotate(float) 设置旋转变换
     */
    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        // 空值检查，避免空指针异常
        if (origin == null) {
            return null;
        }
        
        // 获取原始位图的尺寸
        int width = origin.getWidth();   // 原始宽度
        int height = origin.getHeight(); // 原始高度
        
        // 创建变换矩阵并设置旋转角度
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha); // 围绕原点旋转指定角度
        
        // 根据变换矩阵创建新的位图
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        
        // 如果新位图与原始位图相同，直接返回（避免回收）
        if (newBM.equals(origin)) {
            return newBM;
        }
        
        // 回收原始位图资源
        origin.recycle();
        
        return newBM;
    }

    /**
     * 偏移效果（倾斜变换）
     * 
     * <p>对位图应用倾斜变换，产生视觉上的偏移效果。</p>
     * <p>默认应用固定的倾斜参数：X方向-0.6，Y方向-0.3。</p>
     * <p>注意：如果新位图与原始位图相同，则不会回收原始位图。</p>
     *
     * @param origin 原始位图，如果为null则返回null
     * @return 偏移后的位图，如果原始位图为null则返回null
     * 
     * @see Matrix#postSkew(float, float) 使用后乘方式应用倾斜变换
     */
    public static Bitmap skewBitmap(Bitmap origin) {
        // 空值检查，避免空指针异常
        if (origin == null) {
            return null;
        }
        
        // 获取原始位图的尺寸
        int width = origin.getWidth();   // 原始宽度
        int height = origin.getHeight(); // 原始高度
        
        // 创建变换矩阵并应用倾斜变换
        Matrix matrix = new Matrix();
        matrix.postSkew(-0.6f, -0.3f); // X方向倾斜-0.6，Y方向倾斜-0.3
        
        // 根据变换矩阵创建新的位图
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        
        // 如果新位图与原始位图相同，直接返回（避免回收）
        if (newBM.equals(origin)) {
            return newBM;
        }
        
        // 回收原始位图资源
        origin.recycle();
        
        return newBM;
    }

    /**
     * 根据Drawable资源ID获取位图
     * 
     * <p>支持BitmapDrawable和VectorDrawable两种类型的转换。</p>
     * <p>对于VectorDrawable，会创建与原始尺寸相同的ARGB_8888格式的位图。</p>
     *
     * @param context    上下文对象，用于获取资源
     * @param drawableId Drawable资源ID
     * @return 转换后的位图
     * @throws IllegalArgumentException 如果Drawable类型不支持（非BitmapDrawable或VectorDrawable）
     */
    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        // 获取Drawable对象
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        // 如果是BitmapDrawable，直接获取其位图
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } 
        // 如果是VectorDrawable，需要绘制到位图上
        else if (drawable instanceof VectorDrawable
//                || drawable instanceof VectorDrawableCompat
        ) {
            // 创建与Drawable原始尺寸相同的ARGB_8888位图
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap); // 创建画布
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight()); // 设置绘制边界
            drawable.draw(canvas); // 将Drawable绘制到位图上

            return bitmap;
        } else {
            // 不支持的Drawable类型，抛出异常
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
    
    /**
     * 根据Drawable资源ID获取指定位图
     * 
     * <p>支持BitmapDrawable和VectorDrawable两种类型的转换。</p>
     * <p>对于VectorDrawable，会创建指定尺寸的ARGB_8888格式的位图。</p>
     *
     * @param context    上下文对象，用于获取资源
     * @param drawableId Drawable资源ID
     * @param width      目标位图宽度（像素）
     * @param height     目标位图高度（像素）
     * @return 转换后的位图
     * @throws IllegalArgumentException 如果Drawable类型不支持
     */
    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId, int width, int height) {
        // 获取Drawable对象
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        
        // 如果是BitmapDrawable，直接获取其位图
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } 
        // 如果是VectorDrawable，创建指定尺寸的位图并绘制
        else if (drawable instanceof VectorDrawable
//                || drawable instanceof VectorDrawableCompat
        ) {
            // 创建指定尺寸的ARGB_8888位图
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap); // 创建画布
            drawable.setBounds(0, 0, width, height); // 设置绘制边界为指定尺寸
            drawable.draw(canvas); // 将Drawable绘制到位图上
            return bitmap;
        } else {
            // 不支持的Drawable类型，抛出异常
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    /**
     * 根据Drawable资源ID获取带颜色的位图
     * 
     * <p>支持BitmapDrawable和VectorDrawable两种类型的转换。</p>
     * <p>对于VectorDrawable，会应用指定的颜色滤镜后创建位图。</p>
     *
     * @param context 上下文对象，用于获取资源
     * @param resId   Drawable资源ID
     * @param width   目标位图宽度（像素）
     * @param height  目标位图高度（像素）
     * @param color   要应用的颜色值（ARGB格式）
     * @return 转换后带颜色的位图
     * @throws IllegalArgumentException 如果Drawable类型不支持
     */
    public static Bitmap getBitmapFromDrawable(Context context,@DrawableRes int resId,int width,int height,int color){
        // 获取Drawable对象
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        
        // 如果是BitmapDrawable，直接获取其位图
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } 
        // 如果是VectorDrawable，应用颜色滤镜后创建位图
        else if (drawable instanceof VectorDrawable
//                || drawable instanceof VectorDrawableCompat
        ) {
            // 为VectorDrawable设置颜色滤镜
            ((VectorDrawable)drawable).setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
            // 创建指定尺寸的ARGB_8888位图
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap); // 创建画布
            drawable.setBounds(0, 0, width, height); // 设置绘制边界
            drawable.draw(canvas); // 将带颜色的Drawable绘制到位图上
            return bitmap;
        } else {
            // 不支持的Drawable类型，抛出异常
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
    
    /**
     * 获取带颜色的SVG Drawable
     * 
     * <p>为VectorDrawable应用指定的颜色滤镜，用于动态改变SVG图形的颜色。</p>
     *
     * @param context 上下文对象，用于获取资源
     * @param resId   Drawable资源ID（应为VectorDrawable）
     * @param color   要应用的颜色值（ARGB格式）
     * @return 应用了颜色滤镜的Drawable对象
     */
    public static Drawable getSvgDrawable(Context context,@DrawableRes int resId,int color){
        // 获取Drawable对象
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        // 为VectorDrawable设置颜色滤镜
        ((VectorDrawable)drawable).setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        return drawable;
    }
    
    /**
     * 根据Drawable对象获取位图
     * 
     * <p>支持BitmapDrawable和VectorDrawable两种类型的转换。</p>
     * <p>对于VectorDrawable，会创建与原始尺寸相同的ARGB_8888格式的位图。</p>
     *
     * @param context  上下文对象（当前未使用，保留用于扩展）
     * @param drawable Drawable对象
     * @return 转换后的位图
     * @throws IllegalArgumentException 如果Drawable类型不支持
     */
    public static Bitmap getBitmapFromDrawable(Context context, Drawable drawable) {
        // 如果是BitmapDrawable，直接获取其位图
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } 
        // 如果是VectorDrawable，创建位图并绘制
        else if (drawable instanceof VectorDrawable
//                || drawable instanceof VectorDrawableCompat
        ) {
            // 创建与Drawable原始尺寸相同的ARGB_8888位图
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap); // 创建画布
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight()); // 设置绘制边界
            drawable.draw(canvas); // 将Drawable绘制到位图上

            return bitmap;
        } else {
            // 不支持的Drawable类型，抛出异常
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
    
    /**
     * 获取带颜色的Drawable
     * 
     * <p>从资源ID获取Drawable并应用指定的颜色滤镜。</p>
     *
     * @param context 上下文对象，用于获取资源
     * @param resId   Drawable资源ID
     * @param color   要应用的颜色值（ARGB格式）
     * @return 应用了颜色滤镜的Drawable对象
     */
    public static Drawable getDrawable(Context context,int resId,int color){
        // 从资源获取Drawable
        Drawable drawable= context.getResources().getDrawable(resId);
        // 设置颜色滤镜
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        return drawable;
    }

    /**
     * 生成通道选择器颜色状态列表
     * 
     * <p>根据通道索引生成ColorStateList，用于控件在不同状态下显示不同颜色。</p>
     * <p>状态包括：禁用、选中、按下、正常。</p>
     * <p>选中状态下使用通道对应的颜色。</p>
     *
     * @param context 上下文对象，用于获取资源和通道颜色
     * @param chIdx   通道索引（0-7对应CH1-CH8等）
     * @return 包含四种状态颜色的ColorStateList
     * 
     * @see TChan#getChannelColor(Context, int) 获取通道对应的颜色
     */
    public static ColorStateList genSelectorColor(Context context,int chIdx){
        // 定义四种状态数组
        int[][] states=new int[4][];
        states[0]=new int[]{-android.R.attr.state_enabled};        // 禁用状态
        states[1]=new int[]{android.R.attr.state_checked};         // 选中状态
        states[2]=new int[]{android.R.attr.state_pressed};         // 按下状态
        states[3]=new int[]{};                                      // 默认状态

        // 获取各状态对应的颜色
        int checkedColor= TChan.getChannelColor(context,chIdx);    // 选中颜色（通道颜色）
        int enableColor=context.getResources().getColor(R.color.textColorNewRightViewDisable);  // 禁用颜色
        int normalColor=context.getResources().getColor(R.color.textColorNewRightViewEnable);   // 正常颜色
        
        // 创建颜色数组（与状态数组一一对应）
        int[] colors=new int[]{enableColor,checkedColor,checkedColor,normalColor};
        
        // 创建并返回ColorStateList
        ColorStateList itemTextColorResId=new ColorStateList(states,colors);
        return itemTextColorResId;
    }
    
    /**
     * 生成通道选择器Drawable
     * 
     * <p>根据通道索引生成StateListDrawable，用于按钮在不同状态下显示不同背景。</p>
     * <p>选中状态下背景使用通道对应的颜色着色。</p>
     * <p>支持的状态：禁用、选中、按下、正常。</p>
     *
     * @param context 上下文对象，用于获取资源和通道颜色
     * @param chIdx   通道索引（0-7对应CH1-CH8等）
     * @return 包含多种状态的StateListDrawable
     * 
     * @see TChan#getChannelColor(Context, int) 获取通道对应的颜色
     */
    public static StateListDrawable genSelectorDrawable(Context context,int chIdx){
        // 获取通道对应的颜色
        int checkedColor= TChan.getChannelColor(context,chIdx);

        // 获取选中状态的层叠Drawable并设置颜色
        Drawable drawable= context.getResources().getDrawable(R.drawable.layer_button_select_s2);
        Drawable d1= ((LayerDrawable)drawable).getDrawable(0); // 获取层叠Drawable的第一层
        d1.setTint(checkedColor); // 应用通道颜色着色

        // 创建状态列表Drawable
        StateListDrawable itemBgViewResId=new StateListDrawable();
        
        // 添加各种状态对应的Drawable
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked, -android.R.attr.state_enabled},context.getResources().getDrawable(R.drawable.layer_button_disable)); // 禁用且选中
        itemBgViewResId.addState(new int[]{-android.R.attr.state_enabled}, context.getResources().getDrawable(R.drawable.layer_button_disable)); // 禁用
        itemBgViewResId.addState(new int[]{android.R.attr.state_selected},drawable); // 选中
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked },drawable); // 勾选
        itemBgViewResId.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_window_focused},drawable); // 按下且窗口聚焦
        itemBgViewResId.addState(new int[0],context.getResources().getDrawable(R.drawable.layer_button_normal)); // 默认状态
        
        return itemBgViewResId;
    }

    /**
     * 生成左侧半圆形选择器Drawable
     * 
     * <p>生成用于左侧按钮的半圆形状态选择器。</p>
     * <p>选中状态下使用通道对应的颜色着色。</p>
     *
     * @param context 上下文对象，用于获取资源和通道颜色
     * @param chIdx   通道索引（0-7对应CH1-CH8等）
     * @return 左侧半圆形的StateListDrawable
     * 
     * @see #getSvgDrawable(Context, int, int) 获取带颜色的SVG Drawable
     */
    public static StateListDrawable genSelectorLeftDrawable(Context context,int chIdx){
        // 获取通道对应的颜色
        int checkedColor= TChan.getChannelColor(context,chIdx);
        
        // 获取带通道颜色的左侧半圆形SVG Drawable
        Drawable drawable =getSvgDrawable(context,R.drawable.svg_semicircle_left_select_ch1,checkedColor);

        // 创建状态列表Drawable
        StateListDrawable itemBgViewResId=new StateListDrawable();
        
        // 添加各种状态对应的Drawable
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked, -android.R.attr.state_enabled},context.getResources().getDrawable(R.drawable.svg_semicircle_left_disable)); // 禁用且选中
        itemBgViewResId.addState(new int[]{-android.R.attr.state_enabled}, context.getResources().getDrawable(R.drawable.svg_semicircle_left_disable)); // 禁用
        itemBgViewResId.addState(new int[]{android.R.attr.state_selected},drawable); // 选中
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked },drawable); // 勾选
        itemBgViewResId.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_window_focused},drawable); // 按下且窗口聚焦
        itemBgViewResId.addState(new int[0],context.getResources().getDrawable(R.drawable.svg_semicircle_left)); // 默认状态
        
        return itemBgViewResId;
    }

    /**
     * 生成右侧半圆形选择器Drawable
     * 
     * <p>生成用于右侧按钮的半圆形状态选择器。</p>
     * <p>选中状态下使用通道对应的颜色着色。</p>
     *
     * @param context 上下文对象，用于获取资源和通道颜色
     * @param chIdx   通道索引（0-7对应CH1-CH8等）
     * @return 右侧半圆形的StateListDrawable
     * 
     * @see #getSvgDrawable(Context, int, int) 获取带颜色的SVG Drawable
     */
    public static StateListDrawable genSelectorRightDrawable(Context context,int chIdx){
        // 获取通道对应的颜色
        int checkedColor= TChan.getChannelColor(context,chIdx);
        
        // 获取带通道颜色的右侧半圆形SVG Drawable
        Drawable drawable =getSvgDrawable(context,R.drawable.svg_semicircle_right_select_ch1,checkedColor);

        // 创建状态列表Drawable
        StateListDrawable itemBgViewResId=new StateListDrawable();
        
        // 添加各种状态对应的Drawable
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked, -android.R.attr.state_enabled},context.getResources().getDrawable(R.drawable.svg_semicircle_right_disable)); // 禁用且选中
        itemBgViewResId.addState(new int[]{-android.R.attr.state_enabled}, context.getResources().getDrawable(R.drawable.svg_semicircle_right_disable)); // 禁用
        itemBgViewResId.addState(new int[]{android.R.attr.state_selected},drawable); // 选中
        itemBgViewResId.addState(new int[]{android.R.attr.state_checked },drawable); // 勾选
        itemBgViewResId.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_window_focused},drawable); // 按下且窗口聚焦
        itemBgViewResId.addState(new int[0],context.getResources().getDrawable(R.drawable.svg_semicircle_right)); // 默认状态
        
        return itemBgViewResId;
    }

}
