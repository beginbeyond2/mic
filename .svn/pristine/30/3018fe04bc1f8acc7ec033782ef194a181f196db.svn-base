package com.micsig.tbook.tbookscope.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

/**
 * @auother Liwb
 * @description:
 * @data:2022-4-14 9:31
 */
public class UsbChangedReceiver extends BroadcastReceiver {
    public final static String ACTION = "android.hardware.usb.action.USB_STATE";

    private ImageView tvUsbPcLink;
    public void setUsbControl(ImageView tvUsbPcLink){
        this.tvUsbPcLink=tvUsbPcLink;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        if (action.equalsIgnoreCase(ACTION)) {
            boolean connected = intent.getBooleanExtra("connected", false);
            tvUsbPcLink.setVisibility(connected ? View.VISIBLE : View.GONE);
        }
    }
}
