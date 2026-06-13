package com.micsig.tbook.tbookscope.tools;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class LockScreenUtils {
    private static final String TAG = "LockScreenUtils";

    public static boolean isLockScreen(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Class<?> cls = wm.getClass();
        Method method;
        boolean isLockScreen = false;
        try {
            method = cls.getMethod("isLockScreen");
            isLockScreen = (boolean) method.invoke(wm);
        } catch (Exception ex) {
            Log.e(TAG,"Invoke method error. " + ex.getMessage());
        }

        return isLockScreen;
    }

    public static boolean isSystemSupportLockScreen(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Class<?> cls = wm.getClass();
        Method method;
        boolean isSystemSupportLockScreen = false;
        try {
            method = cls.getMethod("isSystemSupportLockScreen");
            isSystemSupportLockScreen = (boolean) method.invoke(wm);
        } catch (Exception ex) {
            Log.e(TAG,"Invoke method error. " + ex.getMessage());
        }

        return isSystemSupportLockScreen;
    }

    public static void setLockScreen(Context context,boolean lockScreen){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Class<?> cls = wm.getClass();
        Method method;
        try {
            method = cls.getMethod("setLockScreen",new Class[]{boolean.class});
            method.invoke(wm,lockScreen);
        } catch (Exception ex) {
            Log.e(TAG,"Invoke method error. " + ex.getMessage());
        }
    }
}
