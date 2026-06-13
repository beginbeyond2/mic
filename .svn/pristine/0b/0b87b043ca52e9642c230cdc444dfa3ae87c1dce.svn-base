package com.micsig.tbook.hardware;

import android.content.Context;
import android.hardware.OtherManager;
import android.os.Build;
import android.util.Log;

/**
 * Created by zhuzh on 2018/3/9.
 */

public class HwManager {

    private static final String TAG = "HwManager";
    private Context mContext;
    private OtherManager mOtherManager;


    private int mSysVersion = -1;
    private long mHwVersion = 0;

    private static volatile HwManager instance = null;

    public static HwManager getInstance(){
        return instance;
    }
    public static HwManager getInstance(Context context) {
        if (instance == null) {
            synchronized (HwManager.class) {
                if (instance == null && context != null) {
                    instance = new HwManager(context);
                }
            }
        }
        return instance;
    }

    private HwManager(Context context){
        mContext = context;
        mOtherManager = (OtherManager)mContext.getSystemService(HwServiceName.OTHER_SERVICE);
        mHwVersion = mOtherManager.getHardwareVersion();
        mSysVersion = getSysVesion();
        Log.d(TAG,"mHwVersion:" + mHwVersion + ",mSysVersion:" + mSysVersion);
    }
    public int readE2PROM(int addr, byte[] byteArray){
        return mOtherManager.eepromRead(addr,byteArray);
    }
    public int writeE2PROM(int addr,byte[] byteArray){
        return mOtherManager.eppromWrite(addr,byteArray);
    }
    public void setFpgaDna(String dna){
        mOtherManager.setFpgaDna(dna);
    }

    public String getMachineUUID(){
        return  mOtherManager.getMachineUuid();
    }

    public int getTemperature(){
        return mOtherManager.getSysTemperature();
    }

    public int getCpuTemperature(){
        return mOtherManager.getCpuTemperature();
    }
    public void setTemperature(int val){
        mOtherManager.setSysTemperature(val);
    }

    public void setFanSpeed(int val){
        mOtherManager.setSysFanSpeed(val);
    }

    private int getSysVesion(){
        mSysVersion = 0;
        String ver = OtherManager.getString("ro.product.version");
        if(ver!=null && !ver.isEmpty()){
            mSysVersion = Integer.parseInt(ver);
        }
        return mSysVersion;

    }
    public String getSysId(){
        String ver = OtherManager.getString("ro.product.id");
        if(ver != null && ver.length() > 0){
            return ver;
        }
        return "";
    }
    public int sysVersion(){
        return mSysVersion;
    }
    public int getHwVersion(){
        return (int)mHwVersion;
    }
    public void setUsbInfo(String product,String serial,String ver){

        Log.d(TAG, "setUsbInfo() called with: product = [" + product + "], serial = [" + serial + "]");
        product = product.replace("\r\n","").trim();
        serial = serial.replace("\r\n","").trim();
        ver = ver.replace("\r\n","").trim();
        if(product.isEmpty()){
            product = Build.MODEL;
        }
        if(!product.equals(OtherManager.getString("persist.usb.product"))) {
            OtherManager.setString("persist.usb.product", product);
        }
        if(!serial.equals(OtherManager.getString("persist.usb.serialno"))) {
            OtherManager.setString("persist.usb.serialno", serial);
        }
        if(!ver.equals(OtherManager.getString("persist.firmwarerevision"))) {
            OtherManager.setString("persist.firmwarerevision", ver);
        }
    }


    public int getChProbeVal(int chIdx){
        int val = 0;
        if(chIdx >= 0 &&  chIdx < 4) {
            int []chArray={7,6,5,4};
            for(int i=0;i<10;i++)
            {
                val += mOtherManager.getAdcVal(chArray[chIdx]);
            }
            val /= 10;
            val = val * 1800/1024;
        }
//        Log.d(TAG,"chIdx:" + chIdx + ",val:" + val);
        return val;
    }

    public static String getString(String property) {
        return OtherManager.getString(property);
    }

    public static void setString(String key, String val) {
        OtherManager.setString(key,val);
    }

}
