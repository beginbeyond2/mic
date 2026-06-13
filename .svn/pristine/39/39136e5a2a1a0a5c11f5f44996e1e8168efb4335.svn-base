package com.micsig.tbook.tbookscope.broadcastreceiver;

import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.ui.main.BatteryView;

/**
 * @auother Liwb
 * @description:
 * @data:2022-4-14 9:40
 */
public class BroadcastManager {
    private static BroadcastManager instance=null;
    public static BroadcastManager getInstance(){
        if (instance==null){
            instance=new BroadcastManager();
        }
        return instance;
    }


    private BatteryChangedReceiver batteryChangedReceiver;
    private UDiskChangedReceiver uDiskChangedReceiver;
    private UsbChangedReceiver usbChangedReceiver;
    private WifiChangedReceiver wifiChangedReceiver;

    private MainActivity mainActivity;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public BroadcastManager(){

    }

    public void init(MainActivity mainActivity){
        this.mainActivity=mainActivity;


        batteryChangedReceiver = new BatteryChangedReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);


        mainActivity.registerReceiver(batteryChangedReceiver, intentFilter);


        usbChangedReceiver = new UsbChangedReceiver();
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction(UsbChangedReceiver.ACTION);
        mainActivity.registerReceiver(usbChangedReceiver, intentFilter3);

        uDiskChangedReceiver = new UDiskChangedReceiver();
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDeviceStateFilter.addAction(ACTION_USB_PERMISSION);
        usbDeviceStateFilter.addDataScheme("file");
        mainActivity.registerReceiver(uDiskChangedReceiver, usbDeviceStateFilter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        wifiChangedReceiver = new WifiChangedReceiver();
        mainActivity.registerReceiver(wifiChangedReceiver, filter);
    }


    public void unregisterReceiver(){

        mainActivity.unregisterReceiver(batteryChangedReceiver);

        mainActivity.unregisterReceiver(usbChangedReceiver);
        mainActivity.unregisterReceiver(wifiChangedReceiver);
        mainActivity.unregisterReceiver(uDiskChangedReceiver);
    }

    public void setBatteryControl(BatteryView tvBattery, TextView tvTime, DialogOk dialogOk){

        batteryChangedReceiver.setBatteryControl(tvBattery);
        batteryChangedReceiver.setTimeControl(tvTime);
        batteryChangedReceiver.setDialogOk(dialogOk);

    }
    public void setUsbControl(ImageView tvUsbPcLink){
        usbChangedReceiver.setUsbControl(tvUsbPcLink);
    }
    public void setWifiControl(ImageView tvWifi){
        wifiChangedReceiver.setWifiControl(tvWifi);
    }
    public void setInternetControl(ImageView tvInternet){
        wifiChangedReceiver.setInternetControl(tvInternet);
    }
    public void setUDiskControl(ImageView tvUDisk){
        uDiskChangedReceiver.setUDiskControl(tvUDisk);
    }



}
