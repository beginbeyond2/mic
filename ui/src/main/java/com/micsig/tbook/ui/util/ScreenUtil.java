package com.micsig.tbook.ui.util;

import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.micsig.base.Logger;

import java.util.Arrays;

/**
 * 屏幕和文本尺寸工具类
 * 
 * <p>提供视图位置获取和文本尺寸计算功能。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>视图位置：获取视图在屏幕上的位置和尺寸</li>
 *   <li>文本宽度：提供三种不同精度的文本宽度计算方法</li>
 *   <li>文本高度：计算文本的绘制高度</li>
 * </ul>
 * 
 * <p>文本宽度计算方法对比：</p>
 * <ul>
 *   <li>getTextWidth：基于边界框计算，返回整数宽度</li>
 *   <li>getTextWidth2：逐字符精确计算，返回向上取整的宽度</li>
 *   <li>getTextWidth3：使用measureText快速测量，性能最好</li>
 * </ul>
 * 
 * @author Micsig Technology
 * @version 1.0
 * @since 2024
 */
public class ScreenUtil {
    
    /** 复用的矩形对象，用于文本边界计算，避免频繁创建对象 */
    static Rect rect = new Rect();
    
    /**
     * 获取视图在屏幕上的位置和尺寸
     * 
     * <p>获取视图相对于屏幕左上角的位置，并结合视图的宽高返回矩形区域。</p>
     * <p>返回的Rect包含：left（X坐标）、top（Y坐标）、right（宽度）、bottom（高度）。</p>
     * <p>注意：此方法会输出日志信息。</p>
     *
     * @param view 要获取位置的视图对象
     * @return 包含位置和尺寸的Rect对象
     *         <ul>
     *           <li>rect.left：视图左边缘的X坐标</li>
     *           <li>rect.top：视图上边缘的Y坐标</li>
     *           <li>rect.right：视图的宽度</li>
     *           <li>rect.bottom：视图的高度</li>
     *         </ul>
     * 
     * @see View#getLocationOnScreen(int[]) 获取视图在屏幕上的位置
     */
    public static Rect getViewLocation(View view) {
        // 创建数组存储视图位置（x, y坐标）
        int[] ints = new int[2];
        
        // 获取视图在屏幕上的位置
        view.getLocationOnScreen(ints);
        
        // 输出日志：位置、宽度、高度
        Logger.i("view:" + Arrays.toString(ints) + "\t" + view.getWidth() + "\t" + view.getHeight());
        
        // 返回包含位置和尺寸的矩形
        // 注意：这里right和bottom存储的是宽度和高度，不是右下角坐标
        return new Rect(ints[0], ints[1], view.getWidth(), view.getHeight());
    }

    /**
     * 计算文本宽度（基于边界框）
     * 
     * <p>使用Paint的getTextBounds方法计算文本的边界框，返回边界框的宽度。</p>
     * <p>此方法返回整数宽度，适合需要整数像素值的场景。</p>
     * <p>注意：此方法会修改类级别的rect对象。</p>
     *
     * @param paint 绘制文本使用的Paint对象
     * @param text  要计算宽度的文本字符串
     * @return 文本的宽度（像素），整数类型
     * 
     * @see Paint#getTextBounds(String, int, int, Rect) 获取文本边界框
     */
    public static int getTextWidth(Paint paint, String text) {
        // 计算文本边界框
        paint.getTextBounds(text, 0, text.length(), rect);
        
        // 获取边界框的宽度和高度
        int w = rect.width();  // 文本宽度
        int h = rect.height(); // 文本高度（当前未使用）
        
        return w;
    }

    /**
     * 精确计算文本宽度（逐字符累加）
     * 
     * <p>使用Paint的getTextWidths方法获取每个字符的精确宽度，然后累加。</p>
     * <p>此方法对每个字符的宽度向上取整后累加，精度最高。</p>
     * <p>适合需要精确控制文本布局的场景。</p>
     *
     * @param paint 绘制文本使用的Paint对象
     * @param str   要计算宽度的文本字符串，如果为null或空字符串则返回0
     * @return 文本的精确宽度（像素），每个字符宽度向上取整后累加
     * 
     * @see Paint#getTextWidths(String, float[]) 获取每个字符的宽度
     */
    public static int getTextWidth2(Paint paint, String str) {
        int iRet = 0; // 累计宽度
        
        // 检查字符串是否有效
        if (str != null && str.length() > 0) {
            int len = str.length();        // 字符串长度
            float[] widths = new float[len]; // 存储每个字符宽度的数组
            
            // 获取每个字符的宽度
            paint.getTextWidths(str, widths);
            
            // 累加每个字符的宽度（向上取整）
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]); // 向上取整后累加
            }
        }
        
        return iRet;
    }

    /**
     * 粗略计算文本宽度（快速测量）
     * 
     * <p>使用Paint的measureText方法快速测量文本宽度。</p>
     * <p>此方法性能最好，但返回的是浮点数转整数，可能有精度损失。</p>
     * <p>适合对精度要求不高、需要快速计算的场景。</p>
     *
     * @param paint 绘制文本使用的Paint对象
     * @param text  要计算宽度的文本字符串
     * @return 文本的宽度（像素），整数类型
     * 
     * @see Paint#measureText(String) 测量文本宽度
     */
    public static int getTextWidth3(Paint paint, String text) {
        // 使用measureText快速测量并转换为整数
        return (int) paint.measureText(text);
    }

    /**
     * 计算文本高度
     * 
     * <p>使用Paint的getTextBounds方法计算文本的边界框，返回边界框的高度。</p>
     * <p>注意：此方法会修改类级别的rect对象。</p>
     *
     * @param paint 绘制文本使用的Paint对象
     * @param text  要计算高度的文本字符串
     * @return 文本的高度（像素），整数类型
     * 
     * @see Paint#getTextBounds(String, int, int, Rect) 获取文本边界框
     */
    public static int getTextHeight(Paint paint, String text) {
        // 计算文本边界框
        paint.getTextBounds(text, 0, text.length(), rect);
        
        // 获取边界框的宽度和高度
        int w = rect.width();  // 文本宽度（当前未使用）
        int h = rect.height(); // 文本高度
        
        return h;
    }
}
