package com.micsig.tbook.tbookscope.tools;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-14 9:29
 */
public class UsbUtils {
    private static final UsbUtils mUsbUtils=new UsbUtils();
    public static UsbUtils getInstance(){
        return mUsbUtils;
    }

    private static StorageVolume[] getVolumeList(StorageManager storageManager){
        try {
            Class clz=StorageManager.class;
            Method getVolumeList=clz.getMethod("getVolumeList");
            StorageVolume[] result=(StorageVolume[])getVolumeList.invoke(storageManager);
            return result;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static String getVolumeState(StorageManager storageManager,String path){
        String result="";
        if (null==storageManager || TextUtils.isEmpty(path)){
            return result;
        }
        try{
            Class clz=StorageManager.class;
            Method getVolumeList=clz.getMethod("getVolumeState",String.class);
            result=(String)getVolumeList.invoke(storageManager,path);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static boolean UdiskExist(Context context){
        StorageManager storageManager=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        if (storageManager==null){
            return false;
        }
        String usbPath=getUsbPath(context,storageManager);
        if (UsbUtils.getVolumeState(storageManager,usbPath).equals(Environment.MEDIA_MOUNTED)){
            Tools.mapUdisk.put(usbPath, usbPath);
            return true;
        }
        return false;

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static String getUsbPath(Context context, StorageManager storageManager){
        String usb=null;
        StorageVolume[] volumes=UsbUtils.getVolumeList(storageManager);
        for(int i=0;i<volumes.length;i++){
            //Logger.i(Command.TAG,"usb description:"+volumes[i].getDescription(context)+", isRemovalbel:"+volumes[i].isRemovable());
            if (volumes[i].isRemovable() /*&& volumes[i].getDescription(context).contains("USB")*/){
                File file=volumes[i].getDirectory();
                if(file != null) {
                    usb = file.getPath();
                    break;
                }

//                if (file.getPath().equals("/storage/udisk")){
//                    break;
//                }

            }
        }
        return usb;
    }

}
