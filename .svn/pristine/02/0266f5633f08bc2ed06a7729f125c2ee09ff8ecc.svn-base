package com.micsig.base;

import android.util.Log;

public class Logger {
    public static boolean DEBUG;
    static final String DEBUG_TAG = "Logger";

    public static void i(String msg) {
        if (DEBUG) {
            Log.i(DEBUG_TAG, msg + "");
        }
    }

    public static void d(String msg) {
        if (DEBUG) {
            Log.d(DEBUG_TAG, msg + "");
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e(DEBUG_TAG, msg + "");
        }
    }

    public static void w(String msg) {
        if (DEBUG) {
            Log.w(DEBUG_TAG, msg + "");
        }
    }

    public static void v(String msg) {
        if (DEBUG) {
            Log.v(DEBUG_TAG, msg + "");
        }
    }


    public static void i(String tag, String msg) {
        if (DEBUG){
            Log.i(tag,msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG){
            Log.d(tag,msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG){
            Log.e(tag,msg);
        }
    }

    public static void w(String tag, String msg) {
        if(DEBUG){
            Log.w(tag,msg);
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG){
            Log.v(tag,msg);
        }
    }

    public static void i(Object o, String msg) {
        i(o.getClass().getSimpleName() + ":" + msg);
    }

    public static void d(Object o, String msg) {
        d(o.getClass().getSimpleName() + ":" + msg);
    }

    public static void e(Object o, String msg) {
        e(o.getClass().getSimpleName() + ":" + msg);
    }

    public static void w(Object o, String msg) {
        w(o.getClass().getSimpleName() + ":" + msg);
    }

    public static void v(Object o, String msg) {
        v(o.getClass().getSimpleName() + ":" + msg);
    }

    public static void i(String tag, String info, Throwable e) {
        i(tag + ":" + info + ":" + Log.getStackTraceString(e));
    }

    public static void d(String tag, String info, Throwable e) {
        d(tag + ":" + info + ":" + Log.getStackTraceString(e));
    }

    public static void e(String tag, String info, Throwable e) {
        e(tag + ":" + info + ":" + Log.getStackTraceString(e));
    }

    public static void v(String tag, String info, Throwable e) {
        v(tag + ":" + info + ":" + Log.getStackTraceString(e));
    }

    public static void w(String tag, String info, Throwable e) {
        w(tag + ":" + info + ":" + Log.getStackTraceString(e));
    }

    public static void i(String tag, Throwable e) {
        i(tag + ":" + Log.getStackTraceString(e));
    }

    public static void d(String tag, Throwable e) {
        d(tag + ":" + Log.getStackTraceString(e));
    }

    public static void e(String tag, Throwable e) {
        e(tag + ":" + Log.getStackTraceString(e));
    }

    public static void v(String tag, Throwable e) {
        v(tag + ":" + Log.getStackTraceString(e));
    }

    public static void w(String tag, Throwable e) {
        w(tag + ":" + Log.getStackTraceString(e));
    }

    public static void vLarg(String sb) {
        if (sb.length() > 4000) {
            v("log.length = " + sb.length());
            int chunkCount = sb.length() / 4000;     // integer division
            for (int i = 0; i <= chunkCount; i++) {
                int max = 4000 * (i + 1);
                if (max >= sb.length()) {
                    v("chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i));
                } else {
                    v("chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i, max));
                }
            }
        } else {
            v(sb.toString());
        }
    }

//    public static void errDebug(){
//        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//        e("micsig","TODO:需要支持8通道,数学8通道,参考8通道,总线4通道"
//                + ",Name:" + stackTraceElements[3].getFileName()
//                + "," + stackTraceElements[3].getLineNumber()
//        );
//    }
}

