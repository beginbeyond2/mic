package com.micsig.smart;

import android.content.Context;
import android.hardware.OtherManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class HardwareCtrl {
    private static final String TAG = "HardwareCtrl";


    private OtherManager mOtherManager;
    private Context mContext;
    public static final String OTHER_SERVICE = "other";



    private static volatile HardwareCtrl instance = null;
    public static HardwareCtrl getInstance(){
        return instance;
    }
    public static HardwareCtrl getInstance(Context context) {
        if (instance == null) {
            synchronized (HardwareCtrl.class) {
                if (instance == null && context != null) {
                    instance = new HardwareCtrl(context);
                }
            }
        }
        return instance;
    }
    private HardwareCtrl(Context context){
        mContext = context;
        mOtherManager = (OtherManager) mContext.getSystemService(OTHER_SERVICE);
        checkHwMagic();
        if(!isHwMagic()){
            byte [] bytes = new byte[4];
            readE2PROM(HW_MAGIC_ADDR0,bytes);
            if(bytes[0] == 0x20
                    && bytes[1] == 0x23
                    && bytes[2] == 0x02
                    && bytes[3] == 0x21){
                writeHwMagic();
            }
        }
        OPT_ID = readOptId();
    }

    private static final int HW_MAGIC_ADDR0 = 0x7FF0;
    private static final int HW_MAGIC_ADDR = 0x55F0;
    private volatile boolean bHwMagic = false;
    private synchronized void checkHwMagic(){
        bHwMagic = false;
        byte [] bytes = new byte[4];
        readE2PROM(HW_MAGIC_ADDR,bytes);
        if(bytes[0] == 0x20
                && bytes[1] == 0x23
                && bytes[2] == 0x02
                && bytes[3] == 0x21){
            bHwMagic = true;
        }
    }
    public synchronized boolean isHwMagic(){
        return bHwMagic;
    }

    public void writeHwMagic(){
        byte [] bytes = {0x20,0x23,0x02,0x21};
        writeE2PROM(HW_MAGIC_ADDR,bytes);
        checkHwMagic();
    }

    public int readE2PROM(int addr, byte[] byteArray){
        return mOtherManager.eepromRead(addr,byteArray);
    }
    public int writeE2PROM(int addr,byte[] byteArray){
        int len = 0;
        len = mOtherManager.eppromWrite(addr,byteArray);
        return len;
    }
    public boolean isE2PROM(){
        String str = OtherManager.getString("ro.product.eeprom");
        if(str == null || str.trim().isEmpty() || "true".equalsIgnoreCase(str)){
            return true;
        }
        return false;
    }

    public void setFpgaDna(String dna){
        mOtherManager.setFpgaDna(dna);
    }
    public String getMachineUUID(){
        if(isE2PROM()){
            return mOtherManager.getFpgaDna();
        }else {
            //无核心板
            if(isHwMagic()){
                return OPT_ID;
            }else{
                return mOtherManager.getMachineUuid();
            }
        }
    }
    private String OPT_ID = "";
    private String readOptId(){
        String optId = "";
        File file  = new File("/proc/cpuinfo");
        if(file.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null){
                    String []ss = line.split(":");
                    if(ss.length == 2){
                        String key = ss[0].trim();
                        String v = ss[1].trim();
                        if("Serial".equals(key)){
                            optId = v;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return optId;
    }
}
