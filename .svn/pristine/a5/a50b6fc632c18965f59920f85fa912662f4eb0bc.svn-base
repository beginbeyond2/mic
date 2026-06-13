package com.micsig.tbook.tbookscope.broadcastreceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.micsig.base.Logger;
import com.micsig.smart.Property;
import com.micsig.smart.PropertyManage;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @auother Liwb
 * @description:
 * @data:2022-4-14 9:24
 */
public  class UDiskChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "UDiskChangedReceiver";
    private static final String MOUNTS_FILE = "/proc/mounts";
    private StorageManager mStorageManager;

    private ImageView tvUDisk;
    public void setUDiskControl(ImageView tvUDisk){
        this.tvUDisk=tvUDisk;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
        String action = intent.getAction();
        Logger.d(TAG, "action:" + action);
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            String mountPath = intent.getData().getPath();
            Uri data = intent.getData();
            if ("/storage/emulated/0".equalsIgnoreCase(mountPath)) {
                return;
            }
            Logger.d(TAG, "mountPath = " + mountPath);
            if (!TextUtils.isEmpty(mountPath)) {
                //读取到U盘路径再做其他业务逻辑
                CacheUtil.get().putOtherMapAndSave(CacheUtil.MAIN_BOTTOM_USB_PATH, mountPath);
                boolean mounted = isMounted(mountPath);
                tvUDisk.setVisibility(View.VISIBLE);
                if (!Tools.mapUdisk.containsKey(mountPath)) {
                    Tools.mapUdisk.put(mountPath, mountPath);
                }
                RxBus.getInstance().post(RxEnum.UDISK_RESPONSE, true);
                //saveSysInfo(mountPath);
            }
        } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {
            Logger.d(TAG, "onReceive: " + "U盘移除了");
            CacheUtil.get().putOtherMapAndSave(CacheUtil.MAIN_BOTTOM_USB_PATH, "");
            String path = intent.getData().getPath();
            Tools.mapUdisk.remove(path);
            tvUDisk.setVisibility(View.GONE);
            RxBus.getInstance().post(RxEnum.UDISK_RESPONSE, false);
        } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            Logger.d(TAG, "onReceive: " + "BOOT_COMPLETED");
            //如果是开机完成，则需要调用另外的方法获取U盘的路径
        } else if (action.equals(Intent.ACTION_MEDIA_REMOVED)) {
            Logger.d(TAG, "onReceive: " + "ACTION_MEDIA_REMOVED");
        }
    }

    /**
     * 判断是否有U盘插入,当U盘开机之前插入使用该方法.
     *
     * @param path
     * @return
     */
    public static boolean isMounted(String path) {
        boolean blnRet = false;
        String strLine = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));
            while ((strLine = reader.readLine()) != null) {
                if (strLine.contains(path)) {
                    blnRet = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }
        return blnRet;
    }


    private static void saveSysInfo(String path) {
        PropertyManage propertyManage = PropertyManage.getInstance();
        propertyManage.update();
        Property property = propertyManage.getProperty();
        String uuid = property.getUUID();
        String sn = property.getSN();
        String displaySn = property.getDisplaySN();
        if (sn != null) sn = sn.trim();
        if (displaySn != null) displaySn = displaySn.trim();
        if (displaySn == null || displaySn.isEmpty()) {
            displaySn = sn;
        }
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += displaySn + ".txt";
        File file = new File(path);
        if (!file.exists()) {
            try {
                FileWriter fw = new FileWriter(file);
                BufferedWriter bufw = new BufferedWriter(fw);
                bufw.write("SN:" + displaySn + "( " + sn + ")");
                bufw.newLine();
                bufw.write("Code:" + uuid);
                bufw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
