package com.micsig.tbook.tbookscope.util;


import android.content.Context;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;


/**
 * Toast统一管理类
 */
public class DToast {
    public static final int MSG_TOAST_SHOW = 0xab;
    public static final int MSG_TOAST_HIDE = 0xac;
    public static final int MSG_TOAST_SHOW_DELAY = 0xad;

    private static final int msgShowTime = 500;//ms
    private static final int msgShowTimeDelay = 1000;//ms
    private static DToast toast;
    private TextView tvToast;
    private int startX;

    private DToast() {
    }

    public static DToast get() {
        if (toast == null) {
            toast = new DToast();
        }
        return toast;
    }


    public void init(Context context) {
        tvToast = (TextView) ((MainActivity) context).findViewById(R.id.toast);
        startX = (int) context.getResources().getDimension(R.dimen.leftBarWidth);
    }

    public void show(String msg) {
        if (TextUtils.isEmpty(msg)) return;

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
        int x = GlobalVar.get().getMainWave().x / 2 + startX - width / 2;
        int y = GlobalVar.get().getMainWave().y / 2 + GlobalVar.get().getMainTop().y - height / 2;
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) tvToast.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.x = x;
        layoutParams.y = y;

        tvToast.setLayoutParams(layoutParams);
        tvToast.setText(msg);
        handler.sendEmptyMessage(MSG_TOAST_SHOW);
    }


    public void showBottom(String msg) {
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
        int x = GlobalVar.get().getMainWave().x / 2 + startX - width / 2;
        int y = GlobalVar.get().getMainWave().y / 2 + GlobalVar.get().getMainTop().y + height / 2;
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) tvToast.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.x = x;
        layoutParams.y = 1000;
//        tvToast.setLayoutParams(layoutParams);
        tvToast.setText(msg);
        handler.sendEmptyMessage(MSG_TOAST_SHOW);
    }

    public void show(int resId) {
        show(App.get().getResources().getString(resId));
    }

    public void showDelay(String msg) {
        Message message = new Message();
        message.obj = msg;
        message.what = MSG_TOAST_SHOW_DELAY;
        handler.sendMessageDelayed(message, msgShowTimeDelay);
    }

    public void showDelay(int resId) {
        showDelay(App.get().getResources().getString(resId));
    }

    public void hide() {
        handler.sendEmptyMessage(MSG_TOAST_HIDE);
    }

    private int getTextWidth(String text) {
        Paint paint = new Paint();
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.width();
    }

    private int getTextHeight(String text) {
        Paint paint = new Paint();
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TOAST_SHOW:
                    tvToast.setVisibility(View.VISIBLE);
//                    tvToast.setBackground(RenderScriptGaussianBlur.getInstance().getDrawable());
//                    RenderScriptGaussianBlur.getInstance().getWaveBmp(tvToast);

                    if (handler.hasMessages(MSG_TOAST_HIDE)) {
                        handler.removeMessages(MSG_TOAST_HIDE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_TOAST_HIDE, msgShowTime);
                    break;
                case MSG_TOAST_HIDE:
                    if (handler.hasMessages(MSG_TOAST_SHOW_DELAY)) {
                        handler.removeMessages(MSG_TOAST_SHOW_DELAY);
                    }
                    tvToast.setVisibility(View.GONE);
                    break;
                case MSG_TOAST_SHOW_DELAY:
                    show(String.valueOf(msg.obj));
                    break;
            }
        }
    };
}
