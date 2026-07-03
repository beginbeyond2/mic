package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

import android.content.Context; // Android上下文
import android.os.PowerManager; // Android电源管理
import android.util.Log; // Android日志工具

import java.lang.reflect.Method; // 反射方法类

/*
 * +-----------------------------------------------------------------------------+
 * |                         PowerManagerUtils                                   |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 电源管理工具类                                                     |
 * | 核心职责: 提供关机、重启、待机、唤醒等电源操作的静态方法，通过反射调用        |
 * |          PowerManager的隐藏API实现                                           |
 * | 架构设计: 工具类，提供静态方法，不持有状态                                   |
 * | 数据流向: 调用方 → 本类(反射调用) → PowerManager隐藏API → 系统电源管理      |
 * | 依赖关系: PowerManager, Context                                              |
 * | 使用场景: Command_Production等模块需要执行关机/重启/待机/唤醒操作时调用       |
 * +-----------------------------------------------------------------------------+
 */
public class PowerManagerUtils {
    private static final String TAG = "PowerManagerUtils"; // 日志标签

    /**
     * 关机操作
     *
     * @param context Android上下文
     */
    //关机
    public static void shutdown(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE); // 获取电源管理器

        Class<?> cls = pm.getClass(); // 获取PowerManager的Class对象
        Method method; // 反射方法引用
        try {
            method = cls.getMethod("shutdown"); // 获取隐藏的shutdown方法
            method.invoke(pm); // 反射调用关机方法
        } catch (Exception ex) { // 反射调用异常
            Log.e(TAG,"Invoke method error. " + ex.getMessage()); // 记录错误日志
        }
    }

    /**
     * 重启操作
     *
     * @param context Android上下文
     */
    //重启
    public static void reboot(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE); // 获取电源管理器

        Class<?> cls = pm.getClass(); // 获取PowerManager的Class对象
        Method method; // 反射方法引用
        try {
            method = cls.getMethod("reboot"); // 获取隐藏的reboot方法
            method.invoke(pm); // 反射调用重启方法
        } catch (Exception ex) { // 反射调用异常
            Log.e(TAG,"Invoke method error. " + ex.getMessage()); // 记录错误日志
        }
    }

    /**
     * 进入待机模式
     *
     * @param context Android上下文
     */
    //待机
    public static void enterStandby(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE); // 获取电源管理器

        Class<?> cls = pm.getClass(); // 获取PowerManager的Class对象
        Method method; // 反射方法引用
        try {
            method = cls.getMethod("enterStandby"); // 获取隐藏的enterStandby方法
            method.invoke(pm); // 反射调用待机方法
        } catch (Exception ex) { // 反射调用异常
            Log.e(TAG,"Invoke method error. " + ex.getMessage()); // 记录错误日志
        }
    }

    /**
     * 唤醒（退出待机模式）
     *
     * @param context Android上下文
     */
    //唤醒
    public static void exitStandby(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE); // 获取电源管理器

        Class<?> cls = pm.getClass(); // 获取PowerManager的Class对象
        Method method; // 反射方法引用
        try {
            method = cls.getMethod("exitStandby"); // 获取隐藏的exitStandby方法
            method.invoke(pm); // 反射调用唤醒方法
        } catch (Exception ex) { // 反射调用异常
            Log.e(TAG,"Invoke method error. " + ex.getMessage()); // 记录错误日志
        }
    }
}
