package com.micsig.smart;

import android.content.Context;
import android.util.Log;


/**
 * Created by zhuzh on 2018-7-6.
 */

public class PropertyManage {

    private static final String TAG = "PropertyManage";
    static {
        System.loadLibrary("property-lib");
    }


    HardwareCtrl mHw;
    private Context mContext;


    private Property property;
    private String machineUUID;
    private boolean bValid = false;

    private static final int CONF_ADDR = 0x0000;
    private static PropertyManage instance = null;

    byte [] xBytes = null;


    public static PropertyManage getInstance() {
        synchronized (PropertyManage.class) {
            if (instance == null) {
                instance = new PropertyManage();
            }
            return instance;
        }
    }
    private PropertyManage(){


        property = new Property();
        xBytes = new byte[property.getBytes().length];
    }



    public boolean isValid(){
        return bValid;
    }

    public void init(Context context,String fpgaDna){
        Log.d(TAG, "init() called with: context = [" + context + "], fpgaDna = [" + fpgaDna + "]");
        mContext = context;
        mHw = HardwareCtrl.getInstance(mContext);

        if (fpgaDna != null && !fpgaDna.isEmpty()) {
            mHw.setFpgaDna(fpgaDna);
        }
        machineUUID = mHw.getMachineUUID();
        readProperty();
        if(isBlank(property.getSN())
                && isBlank(property.getType())){
            if(!mHw.isE2PROM()) {
                if (!mHw.isHwMagic()) {
                    mHw.writeHwMagic();
                    machineUUID = mHw.getMachineUUID();
                    readProperty();
                }
            }
        }
        Log.d(TAG,"machineUUID:" + machineUUID);
    }

    public static boolean isBlank(String s) {
        if (s == null || s.trim().length() == 0) {
            return true;
        }
        return false;
    }
    private void readProperty(){

        int len = mHw.readE2PROM(CONF_ADDR,xBytes);
        if(len == xBytes.length){
            property.setUUID(machineUUID);
            if(property.initProperty(xBytes)){
                String hwVer = property.getHwVersion();
                if(hwVer == null || hwVer.isEmpty()){
                    property.setHwVersion("1");
                }
                bValid = true;
            }
        }

    }

    private void saveProperty(){

        byte[] bytes = property.getBytes();
        if (bytes != null) {
            mHw.writeE2PROM(CONF_ADDR,bytes);
        }
    }
    public void clear(){
        property.clear();
        saveProperty();
        readProperty();
    }
    //在每次读取前更新数据
    public void update(){
        readProperty();
    }
    //在每次设置收提交数据，n个设置 commit
    public void commit(){
        saveProperty();
    }
    public Property getProperty(){
        return property;
    }

}
