package com.micsig.tbook.tbookscope.tools;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * +-----------------------------------------------------------------------------+
 * |                        锁屏工具类 (LockScreenUtils)                          |
 * +-----------------------------------------------------------------------------+
 * | 模块定位 : tbookscope.tools 通用工具层                                      |
 * | 核心职责 : 通过反射调用 WindowManager 的自定义扩展方法，实现锁屏状态查询与控制 |
 * | 架构设计 : 纯静态工具类，无状态，所有方法均为 static                          |
 * | 数据流向 : Context → WindowManager → 反射调用系统扩展方法 → 返回结果        |
 * | 依赖关系 : android.content.Context, android.view.WindowManager,              |
 * |            java.lang.reflect.Method                                          |
 * | 使用场景 : 查询当前是否处于锁屏状态、查询系统是否支持锁屏、设置锁屏开关       |
 * +-----------------------------------------------------------------------------+
 */
public class LockScreenUtils {
    /** 日志标签 */ // 日志标签常量
    private static final String TAG = "LockScreenUtils"; // 日志TAG为 LockScreenUtils

    /**
     * 查询当前是否处于锁屏状态
     * <p>通过反射调用 WindowManager 的 isLockScreen() 扩展方法</p>
     *
     * @param context 上下文对象
     * @return true 表示当前处于锁屏状态，false 表示未锁屏或调用失败
     */
    public static boolean isLockScreen(Context context){ // 查询当前是否锁屏
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); // 获取 WindowManager 系统服务

        Class<?> cls = wm.getClass(); // 获取 WindowManager 的运行时类
        Method method; // 声明反射方法引用
        boolean isLockScreen = false; // 默认未锁屏
        try { // 尝试反射调用
            method = cls.getMethod("isLockScreen"); // 获取 isLockScreen 方法
            isLockScreen = (boolean) method.invoke(wm); // 反射调用并获取返回值
        } catch (Exception ex) { // 捕获反射异常
            Log.e(TAG,"Invoke method error. " + ex.getMessage()); // 记录调用错误日志
        }

        return isLockScreen; // 返回锁屏状态
    }

    /**
     * 查询系统是否支持锁屏功能
     * <p>通过反射调用 WindowManager 的 isSystemSupportLockScreen() 扩展方法</p>
     *
     * @param context 上下文对象
     * @return true 表示系统支持锁屏，false 表示不支持或调用失败
     */
    public static boolean isSystemSupportLockScreen(Context context){ // 查询系统是否支持锁屏
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); // 获取 WindowManager 系统服务

        Class<?> cls = wm.getClass(); // 获取 WindowManager 的运行时类
        Method method; // 声明反射方法引用
        boolean isSystemSupportLockScreen = false; // 默认不支持锁屏
        try { // 尝试反射调用
            method = cls.getMethod("isSystemSupportLockScreen"); // 获取 isSystemSupportLockScreen 方法
            isSystemSupportLockScreen = (boolean) method.invoke(wm); // 反射调用并获取返回值
        } catch (Exception ex) { // 捕获反射异常
            Log.e(TAG,"Invoke method error. " + ex.getMessage()); // 记录调用错误日志
        }

        return isSystemSupportLockScreen; // 返回系统是否支持锁屏
    }

    /**
     * 设置锁屏开关状态
     * <p>通过反射调用 WindowManager 的 setLockScreen(boolean) 扩展方法</p>
     *
     * @param context    上下文对象
     * @param lockScreen true 启用锁屏，false 关闭锁屏
     */
    public static void setLockScreen(Context context,boolean lockScreen){ // 设置锁屏开关
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); // 获取 WindowManager 系统服务

        Class<?> cls = wm.getClass(); // 获取 WindowManager 的运行时类
        Method method; // 声明反射方法引用
        try { // 尝试反射调用
            method = cls.getMethod("setLockScreen",new Class[]{boolean.class}); // 获取 setLockScreen 方法，参数为 boolean
            method.invoke(wm,lockScreen); // 反射调用设置锁屏状态
        } catch (Exception ex) { // 捕获反射异常
            Log.e(TAG,"Invoke method error. " + ex.getMessage()); // 记录调用错误日志
        }
    }
}
