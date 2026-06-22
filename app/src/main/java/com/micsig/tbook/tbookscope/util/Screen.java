package com.micsig.tbook.tbookscope.util; // 工具包，存放示波器应用的各类工具类

import android.app.Application; // Application基类，用于获取全局上下文
import android.content.Context; // Android上下文，用于获取系统服务
import android.graphics.Rect; // 矩形类，用于描述视图位置和尺寸
import android.util.DisplayMetrics; // 屏幕显示度量信息，包含宽高像素和密度
import android.view.Display; // 显示器对象，用于获取屏幕尺寸
import android.view.View; // 视图基类，用于获取视图位置信息
import android.view.WindowManager; // 窗口管理器，用于获取屏幕Display

import com.micsig.base.Logger; // 日志工具类

import java.util.Arrays; // 数组工具类（当前未使用，保留）

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                           Screen - 屏幕工具类                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：MHO示波器Android应用 → 工具模块(util) → 屏幕与度量                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                    │
 * │   1. 获取并缓存屏幕尺寸（像素px和dp）                                         │
 * │   2. 百分比计算（整数/浮点数→百分比字符串）                                    │
 * │   3. 时间格式化（毫秒→MM:SS格式）                                             │
 * │   4. 文件大小格式化（Byte→MB/KB）                                             │
 * │   5. dp/px互转（适配不同屏幕密度）                                             │
 * │   6. 视图屏幕位置获取（View→Rect）                                            │
 * │   7. Dimen资源值获取                                                          │
 * │   8. 字符串数组转逗号分隔字符串                                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                    │
 * │   - 静态工具类，所有方法均为static                                              │
 * │   - 屏幕尺寸在init()中一次性缓存到静态变量，避免重复获取                         │
 * │   - init()必须在Application.onCreate()中调用                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：                                                                    │
 * │   Application.onCreate() → Screen.init() → 缓存屏幕尺寸到静态变量             │
 * │   其他模块 → Screen.SCREEN_WIDTH_PX / SCREEN_HEIGHT_DP 等读取缓存值           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：                                                                    │
 * │   - WindowManager：获取屏幕Display                                            │
 * │   - DisplayMetrics：屏幕度量信息                                               │
 * │   - Logger：日志输出（当前仅注释代码使用）                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例：                                                                    │
 * │   // 初始化（在Application.onCreate中）                                        │
 * │   Screen.init(application);                                                   │
 * │   // 获取屏幕宽度（像素）                                                      │
 * │   int widthPx = Screen.SCREEN_WIDTH_PX;                                       │
 * │   // dp转px                                                                   │
 * │   float px = Screen.dip2px(context, 16f);                                     │
 * │   // 格式化时间                                                                │
 * │   String time = Screen.formatSecondTime(125000); // "02:05"                  │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class Screen {
    /** 屏幕宽度（dp单位），在init()中初始化 */
    public static float SCREEN_WIDTH_DP; // 屏幕宽度dp值

    /** 屏幕高度（dp单位），在init()中初始化 */
    public static float SCREEN_HEIGHT_DP; // 屏幕高度dp值

    /** 屏幕宽度（像素px单位），在init()中初始化 */
    public static int SCREEN_WIDTH_PX; // 屏幕宽度像素值

    /** 屏幕高度（像素px单位），在init()中初始化 */
    public static int SCREEN_HEIGHT_PX; // 屏幕高度像素值

    /**
     * 初始化屏幕尺寸信息。
     * 获取屏幕实际像素尺寸，并转换为dp值缓存到静态变量。
     * 必须在Application.onCreate()中调用。
     *
     * @param application 应用Application对象，用于获取WindowManager
     */
    public static void init(Application application) { // 初始化屏幕尺寸
        DisplayMetrics dm = getScreen(application); // 获取屏幕DisplayMetrics
        SCREEN_WIDTH_PX = dm.widthPixels; // 缓存屏幕宽度（像素）
        SCREEN_HEIGHT_PX = dm.heightPixels; // 缓存屏幕高度（像素）
        SCREEN_WIDTH_DP = px2dip(application, SCREEN_WIDTH_PX); // 将像素宽度转换为dp并缓存
        SCREEN_HEIGHT_DP = px2dip(application, SCREEN_HEIGHT_PX); // 将像素高度转换为dp并缓存
    }

    /**
     * 获取屏幕的DisplayMetrics信息。
     * 通过WindowManager获取默认Display，读取宽高像素值。
     *
     * @param context 上下文，用于获取WindowManager系统服务
     * @return DisplayMetrics对象，包含widthPixels和heightPixels
     *         widthPixels：屏幕宽度（像素）[索引0语义]
     *         heightPixels：屏幕高度（像素）[索引1语义]
     */
    public static DisplayMetrics getScreen(Context context) { // 获取屏幕度量信息
        WindowManager windowManager = (WindowManager) context // 获取窗口管理器
                .getSystemService(Context.WINDOW_SERVICE); // 通过系统服务获取
        Display display = windowManager.getDefaultDisplay(); // 获取默认显示器
        DisplayMetrics outMetrics = new DisplayMetrics(); // 创建DisplayMetrics对象
        display.getMetrics(outMetrics); // 将屏幕信息填充到outMetrics
        int width = outMetrics.widthPixels; // 读取屏幕宽度（像素），未使用但保留
        int height = outMetrics.heightPixels; // 读取屏幕高度（像素），未使用但保留
        return outMetrics; // 返回完整的DisplayMetrics对象
    }

    /**
     * 计算百分比。
     * 将n占total的比例转换为百分比字符串。
     * - 若结果为整数（如50.0），返回"50"
     * - 若结果有小数（如33.3），保留一位小数返回"33.3"
     *
     * @param n     子数量
     * @param total 总数量
     * @return 百分比字符串，如"50"或"33.3"
     */
    public static String getPercent(int n, float total) { // 计算百分比
        float rs = (n / total) * 100; // 计算百分比值
        // 判断是否是正整数（即小数部分为.0）
        if (String.valueOf(rs).indexOf(".0") != -1) { // 结果包含".0"，说明是整数
            return String.valueOf((int) rs); // 返回整数格式，如"50"
        } else { // 结果有小数
            return String.format("%.1f", rs); // 保留一位小数，如"33.3"
        }
    }

    /**
     * 格式化毫秒时间为MM:SS格式。
     * 将毫秒数转换为"分:秒"格式的字符串，分和秒均两位显示。
     *
     * @param millisecond 毫秒数，若为0则返回"00:00"
     * @return 格式化后的时间字符串，如"02:05"、"00:30"
     */
    public static String formatSecondTime(int millisecond) { // 格式化毫秒→MM:SS
        if (millisecond == 0) { // 毫秒数为0
            return "00:00"; // 直接返回零值格式
        }
        millisecond = millisecond / 1000; // 毫秒转秒（整除，丢弃毫秒部分）
        int m = millisecond / 60 % 60; // 计算分钟数（取余60防止超过60分钟时显示错误）
        int s = millisecond % 60; // 计算秒数（0-59）
        return (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s); // 拼接为MM:SS格式，不足两位补零
    }

    /**
     * 格式化文件大小：Byte → MB。
     * 将字节数转换为兆字节(MB)，保留两位小数。
     *
     * @param size 文件大小（字节）
     * @return 格式化后的MB字符串，如"1.50"、"1024.00"
     */
    public static String formatByteToMB(int size) { // Byte→MB格式化
        float mb = size / 1024f / 1024f; // 先转KB再转MB（除以1024两次）
        return String.format("%.2f", mb); // 保留两位小数
    }

    /**
     * 格式化文件大小：Byte → KB。
     * 将字节数转换为千字节(KB)，保留两位小数。
     *
     * @param size 文件大小（字节）
     * @return 格式化后的KB字符串，如"512.00"、"1024.50"
     */
    public static String formatByteToKB(int size) { // Byte→KB格式化
        float kb = size / 1024f; // 字节转KB（除以1024）
        return String.format("%.2f", kb); // 保留两位小数
    }

    /**
     * 获取dimen资源对应的像素尺寸值。
     * 注意：此方法返回的是经过密度换算后的像素值（getDimensionPixelSize），
     * 会进行四舍五入处理，适合用于布局尺寸。
     *
     * @param context 上下文，用于获取Resources
     * @param resId   dimen资源ID，如R.dimen.xxx
     * @return dimen值对应的像素大小（px）
     */
    public static int getDimen(Context context, int resId) { // 获取dimen像素尺寸
        return context.getResources() // 获取Resources对象
                .getDimensionPixelSize(resId); // 返回密度换算后的像素值（四舍五入）
    }

    /**
     * 获取dimen资源对应的像素偏移值。
     * 注意：此方法返回的是经过密度换算后的像素值（getDimensionPixelOffset），
     * 会进行截断（向下取整）处理，适合用于偏移量。
     *
     * @param context 上下文，用于获取Resources
     * @param resId   dimen资源ID，如R.dimen.xxx
     * @return dimen值对应的像素偏移量（px，向下取整）
     */
    public static int getDimenOffset(Context context, int resId) { // 获取dimen像素偏移
        return context.getResources().getDimensionPixelOffset(resId); // 返回密度换算后的像素偏移（截断）
    }

    /**
     * 字符串数组转逗号分隔字符串。
     * 将String[]中的元素用逗号连接为一个字符串。
     * 例如：["CH1","CH2","CH3"] → "CH1,CH2,CH3"
     *
     * @param strs 字符串数组
     * @return 逗号分隔的字符串，若数组为空则返回""
     */
    public static String arrToStr(String[] strs) { // 字符串数组→逗号分隔字符串
        StringBuilder sb = new StringBuilder(); // 创建StringBuilder
        if (strs.length < 1) { // 数组为空
            return ""; // 返回空字符串
        }
        for (String str : strs) { // 遍历数组
            sb.append(str + ","); // 每个元素后追加逗号
        }
        sb.deleteCharAt(sb.length() - 1); // 删除最后一个多余的逗号
        return sb.toString(); // 返回结果字符串
    }

    /**
     * 将dp值转换为像素值（px）。
     * 基于公式：px = dp × (dpi / 160)，其中160是Android基准密度(mdpi)。
     * 注意：此方法使用densityDpi直接计算，结果为截断的int值。
     * 推荐使用dip2px()方法，其使用density属性并做0.5f四舍五入，精度更高。
     *
     * @param context 上下文，用于获取屏幕密度信息
     * @param dp      dp值
     * @return 转换后的像素值（px，截断取整）
     */
    public static int convertDpToPixel(Context context, float dp) { // dp→px（截断版本）
        DisplayMetrics metrics = context.getResources().getDisplayMetrics(); // 获取显示度量
        float px = dp * (metrics.densityDpi / 160f); // dp × (dpi/160) 转换为像素
        return (int) px; // 截断为整数
    }

    /**
     * 将dp值转换为像素值（px），带0.5f偏移四舍五入。
     * 基于公式：px = dp × density + 0.5f。
     * 使用density属性（= densityDpi / 160），加0.5f实现四舍五入效果，
     * 避免精度丢失导致的1像素偏差。
     *
     * @param context 上下文，用于获取屏幕密度信息
     * @param dpValue dp值
     * @return 转换后的像素值（px，四舍五入）
     */
    public static float dip2px(Context context, float dpValue) { // dp→px（四舍五入版本）
        final float scale = context.getResources().getDisplayMetrics().density; // 获取屏幕密度比例（densityDpi/160）
        return (dpValue * scale + 0.5f); // dp × density + 0.5f，实现四舍五入
    }

    /**
     * 将像素值（px）转换为dp值，带0.5f偏移四舍五入。
     * 基于公式：dp = px / density + 0.5f。
     * 与dip2px互为逆运算，加0.5f实现四舍五入效果。
     *
     * @param context 上下文，用于获取屏幕密度信息
     * @param pxValue 像素值（px）
     * @return 转换后的dp值（四舍五入）
     */
    public static float px2dip(Context context, float pxValue) { // px→dp（四舍五入版本）
        final float scale = context.getResources().getDisplayMetrics().density; // 获取屏幕密度比例
        return (pxValue / scale + 0.5f); // px / density + 0.5f，实现四舍五入
    }

    /**
     * 获取视图在屏幕上的位置矩形。
     * 通过View.getLocationOnScreen获取左上角坐标，结合视图宽高计算完整矩形。
     *
     * @param view 目标视图
     * @return Rect对象，left/top为视图左上角屏幕坐标，right/bottom为右下角屏幕坐标
     */
    public static Rect getViewLocation(View view) { // 获取视图屏幕位置矩形
        int[] ints = new int[2]; // 创建二维数组存储左上角坐标[x, y]
        view.getLocationOnScreen(ints); // 获取视图左上角在屏幕上的坐标
        //Logger.i("view:" + Arrays.toString(ints) + "\t" + view.getWidth() + "\t" + view.getHeight()); // 调试日志（已注释）
        return new Rect(ints[0], ints[1], ints[0] + view.getWidth(), ints[1] + view.getHeight()); // 构造矩形：left, top, right, bottom
    }
}
