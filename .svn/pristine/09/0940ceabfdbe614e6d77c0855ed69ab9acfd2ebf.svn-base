package com.micsig.tbook.tbookscope.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ImageView;

/**
 * @auother Liwb
 * @description:
 * @data:2022-4-14 9:29
 */
public class WifiChangedReceiver extends BroadcastReceiver {
    private ImageView tvInternet;
    private ImageView tvWifi;
    public void setInternetControl(ImageView tvInternet){
        this.tvInternet=tvInternet;
    }
    public void setWifiControl(ImageView tvWifi){
        this.tvWifi=tvWifi;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                || intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            switch (isNetworkAvailable(context)) {
                case 1:
                    tvInternet.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    tvWifi.setVisibility(View.VISIBLE);
                    break;
                case 0:
                    tvInternet.setVisibility(View.GONE);
                    tvWifi.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public static int isNetworkAvailable(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ethNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (ethNetInfo != null && ethNetInfo.isConnected()) {
            return 1;
        } else if (wifiNetInfo != null && wifiNetInfo.isConnected()) {
            return 2;
        } else {
            return 0;
        }
    }
}
