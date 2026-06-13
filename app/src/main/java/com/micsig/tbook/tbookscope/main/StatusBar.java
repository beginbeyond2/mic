package com.micsig.tbook.tbookscope.main;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.broadcastreceiver.BroadcastManager;
import com.micsig.tbook.tbookscope.broadcastreceiver.WifiChangedReceiver;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.UsbUtils;
import com.micsig.tbook.ui.main.BatteryView;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * @auother Liwb
 * @description:
 * @data:2023-10-7 9:59
 */
public class StatusBar {
    private ImageView tvUsbPcLink, tvUDisk, tvWifi, tvInternet;
    private BatteryView tvBattery;
    private TextView tvTime;
    private DialogOk dialogOk;

    private Context context;
    public StatusBar(MainActivity mainActivity){
        this.context=mainActivity.getApplicationContext();
        tvUsbPcLink = (ImageView) mainActivity.findViewById(R.id.usbPcLink);
        tvUDisk = (ImageView) mainActivity.findViewById(R.id.uDisk);
        tvBattery = (BatteryView) mainActivity.findViewById(R.id.battery);
        tvTime = (TextView) mainActivity.findViewById(R.id.time);
        tvWifi = mainActivity.findViewById(R.id.wifi);
        tvInternet = mainActivity.findViewById(R.id.lan);
        initControl();
    }
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);

    }
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private void setCache() {
        tvInternet.setVisibility(View.GONE);
        tvWifi.setVisibility(View.GONE);
        switch (WifiChangedReceiver.isNetworkAvailable(context)) {
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

    public void setBroadcastReceiver(MainActivity mainActivity) {
        BroadcastManager.getInstance().init(mainActivity);
        dialogOk = mainActivity.getMainViewGroup().findViewById(R.id.dialogOk);
        BroadcastManager.getInstance().setBatteryControl(tvBattery, tvTime, dialogOk);
        BroadcastManager.getInstance().setInternetControl(tvInternet);
        BroadcastManager.getInstance().setUDiskControl(tvUDisk);
        BroadcastManager.getInstance().setWifiControl(tvWifi);
        BroadcastManager.getInstance().setUsbControl(tvUsbPcLink);
    }
    /**
     * 刷新U盘状态
     * BUG id:7880
     */
    public void refreshUdiskIcon() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            boolean b = UsbUtils.UdiskExist(context);
//            String  s = "usb isExist:"+ b;
//            Logger.i(Command.TAG,s);
            tvUDisk.setVisibility(b ? View.VISIBLE : View.GONE);

        }
    }
}
