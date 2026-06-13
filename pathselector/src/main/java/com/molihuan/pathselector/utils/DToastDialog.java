package com.molihuan.pathselector.utils;


import static com.blankj.molihuan.utilcode.util.ColorUtils.getColor;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.molihuan.pathselector.R;

import org.w3c.dom.Text;


/**
 * Toast统一管理类
 */
public class DToastDialog {
    public static final int MSG_TOAST_SHOW = 0xab;
    public static final int MSG_TOAST_HIDE = 0xac;
    public static final int MSG_TOAST_SHOW_DELAY = 0xad;

    private static final int msgShowTime = 500;//ms
    private static final int msgShowTimeDelay = 1000;//ms
    private TextView tvToast;
    private int startX;

    public DToastDialog() {
    }


    public  void show(Dialog dialog, String msg) {
        ViewGroup decorView = (ViewGroup) dialog.getWindow().getDecorView();
        Context context = dialog.getContext();
        View customToastView = LayoutInflater.from(context).inflate(R.layout.utils_toast_view,null);
        TextView textView = customToastView.findViewById(R.id.message);
        textView.setTextColor(getColor(R.color.folder_text_color));
        String[] strings;
        if (msg.contains("\n")) {
            strings = msg.split("\n");
        } else {
            strings = new String[1];
            strings[0] = msg;
        }
        int width = 0, height = 50;
        for (String string : strings) {
            width = Math.max(width, getTextWidth(string));
            height = height + (int) (getTextHeight(string) * 1.5);
        }
        width = 200 + width;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;


        textView.setText(msg);
        decorView.addView(customToastView,layoutParams);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(customToastView.getParent()!=null){
                    decorView.removeView(customToastView);
                }
            }
        },1000);

    }


    public TextView showBottom(String msg) {
//        if (TextUtils.isEmpty(msg)) return;\
        String[] strings;
        if (msg.contains("\n")) {
            strings = msg.split("\n");
        } else {
            strings = new String[1];
            strings[0] = msg;
        }
        int width = 0, height = 50;
        for (String string : strings) {
            width = Math.max(width, getTextWidth(string));
            height = height + (int) (getTextHeight(string) * 1.5);
        }
        width = 200 + width;

        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) tvToast.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.x = 1000;
        layoutParams.y = 1000;
//        tvToast.setLayoutParams(layoutParams);
        tvToast.setText(msg);
        return tvToast;

    }



    private static int getTextWidth(String text) {
        Paint paint = new Paint();
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.width();
    }

    private static int getTextHeight(String text) {
        Paint paint = new Paint();
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }


}
