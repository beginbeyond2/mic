package com.micsig.tbook.tbookscope.middleware.command;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import java.lang.reflect.Method;

public class PowerManagerUtils {
    private static final String TAG = "PowerManagerUtils";

    //关机
    public static void shutdown(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        Class<?> cls = pm.getClass();
        Method method;
        try {
            method = cls.getMethod("shutdown");
            method.invoke(pm);
        } catch (Exception ex) {
            Log.e(TAG,"Invoke method error. " + ex.getMessage());
        }
    }

    //重启
    public static void reboot(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        Class<?> cls = pm.getClass();
        Method method;
        try {
            method = cls.getMethod("reboot");
            method.invoke(pm);
        } catch (Exception ex) {
            Log.e(TAG,"Invoke method error. " + ex.getMessage());
        }
    }

    //待机
    public static void enterStandby(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        Class<?> cls = pm.getClass();
        Method method;
        try {
            method = cls.getMethod("enterStandby");
            method.invoke(pm);
        } catch (Exception ex) {
            Log.e(TAG,"Invoke method error. " + ex.getMessage());
        }
    }

    //唤醒
    public static void exitStandby(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        Class<?> cls = pm.getClass();
        Method method;
        try {
            method = cls.getMethod("exitStandby");
            method.invoke(pm);
        } catch (Exception ex) {
            Log.e(TAG,"Invoke method error. " + ex.getMessage());
        }
    }
}
