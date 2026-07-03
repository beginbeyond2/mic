package com.micsig.tbook.tbookscope.tools;

import android.graphics.Color;
/**
 * Created by liwb on 2019/4/3.
 */

/**
 * +-----------------------------------------------------------------------------+
 * |                        RGB颜色工具类 (RgbTools)                              |
 * +-----------------------------------------------------------------------------+
 * | 模块定位 : tbookscope.tools 通用工具层                                      |
 * | 核心职责 : 将 RGB 颜色值转换为 HSV 色彩空间中的 H（色相）分量                |
 * | 架构设计 : 纯静态工具类，无状态，所有方法均为 static                          |
 * | 数据流向 : RGB整型颜色值 → 提取R/G/B分量 → 计算max/min/diff → 求H色相值    |
 * | 依赖关系 : android.graphics.Color                                            |
 * | 使用场景 : 颜色识别、色相判断、颜色分类等需要从RGB转换到HSV的场景             |
 * +-----------------------------------------------------------------------------+
 */
public class RgbTools {
    /** 黑或白的色相值（无色彩信息时返回-1） */ // 黑白色的H值标记
    public static final int H_BlackOrWhite=-1; // 黑白颜色的色相值，-1表示无色相
    /** 黄色的色相值 */ // 黄色H值
    public static final int H_Yellow=60; // 黄色在HSV中的色相值
    /** 红色的色相值 */ // 红色H值
    public static final int H_Red=0; // 红色在HSV中的色相值
    /** 绿色的色相值 */ // 绿色H值
    public static final int H_Green=120; // 绿色在HSV中的色相值
    /** 蓝色的色相值 */ // 蓝色H值
    public static final int H_Blue=240; // 蓝色在HSV中的色相值
    /** 青色的色相值 */ // 青色H值
    public static final int H_Cyan=180; // 青色在HSV中的色相值
    /** 品红色的色相值 */ // 品红色H值
    public static final int H_Magenta=300; // 品红色在HSV中的色相值


    /**
     * 将 RGB 颜色值转换为 HSV 色彩空间中的 H（色相）分量
     * <p>色相范围 0~360，当颜色为黑白灰（diff=0）时返回 -1</p>
     *
     * @param color RGB 颜色整型值（如 Color.RED）
     * @return H 色相值（0~360），黑白灰返回 -1
     */
    public static int rgb2Hsv_H(int color) { // 将RGB颜色转换为HSV的H分量
        int imax, imin, diff; // 声明最大值、最小值、差值变量

        int r = Color.red(color); // 提取RGB中的红色分量
        int g = Color.green(color); // 提取RGB中的绿色分量
        int b = Color.blue(color); // 提取RGB中的蓝色分量
        imax = rgb2Hsv_max(color); // 计算RGB三分量中的最大值
        imin = rgb2Hsv_min(color); // 计算RGB三分量中的最小值
        diff = imax - imin; // 计算最大值与最小值的差值
        int v = imax; // V（明度）等于最大值
        int s = 0; // 初始化 S（饱和度）
        int h = 0; // 初始化 H（色相）
        if (imax == 0) { // 最大值为0，即纯黑
            s = 0; // 饱和度为0
        } else { // 最大值不为0
            s = diff; // 饱和度等于差值（简化计算，未归一化）
        }
        if (diff != 0) { // 差值不为0，即有色彩信息
            if (r == imax) { // 红色分量最大
                h = 60 * (g - b) / diff; // 根据红色最大计算色相
            } else if (g == imax) { // 绿色分量最大
                h = 60 * (b - r) / diff + 120; // 根据绿色最大计算色相，偏移120度
            } else { // 蓝色分量最大
                h = 60 * (r - g) / diff + 240; // 根据蓝色最大计算色相，偏移240度
            }

            if (h < 0) { // 色相值为负数
                h = h + 360; // 加360度转换为正值
            }
        } else { // 差值为0，即黑白灰色
            h = -1; // 色相设为-1表示无色彩信息
        }
        return h; // 返回色相值

    }

    /**
     * 获取 RGB 颜色值中 R/G/B 三个分量的最大值
     *
     * @param color RGB 颜色整型值
     * @return R/G/B 三个分量中的最大值
     */
    private static int rgb2Hsv_max(int color) { // 计算RGB三分量的最大值
        int a, b, c; // 声明三个分量变量
        a = Color.red(color); // 提取红色分量
        b = Color.green(color); // 提取绿色分量
        c = Color.blue(color); // 提取蓝色分量
        int max = a; // 默认最大值为红色分量
        if (b > max) max = b; // 绿色分量更大则更新最大值
        if (c > max) max = c; // 蓝色分量更大则更新最大值
        return max; // 返回最大值
    }

    /**
     * 获取 RGB 颜色值中 R/G/B 三个分量的最小值
     *
     * @param color RGB 颜色整型值
     * @return R/G/B 三个分量中的最小值
     */
    private static int rgb2Hsv_min(int color) { // 计算RGB三分量的最小值
        int a, b, c; // 声明三个分量变量
        a = Color.red(color); // 提取红色分量
        b = Color.green(color); // 提取绿色分量
        c = Color.blue(color); // 提取蓝色分量
        int min = a; // 默认最小值为红色分量
        if (b < min) min = b; // 绿色分量更小则更新最小值
        if (c < min) min = c; // 蓝色分量更小则更新最小值
        return min; // 返回最小值
    }
}
