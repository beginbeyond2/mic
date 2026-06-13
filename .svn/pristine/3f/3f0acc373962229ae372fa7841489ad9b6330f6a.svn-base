package com.micsig.base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class OEM {
    private static final String Splash_PATH = "/private/oem/smart_oscilloscope.png";
    private static final String Logo_PATH = "/private/oem/logo.png";
    private static final String OEM_PATH = "/private/oem/oem.txt";
    private static Bitmap getBitmap(String path){
        Bitmap bitmap = null;
        File f = new File(path);
        if(f.exists()){
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;
            op.inScaled = false;
            bitmap = BitmapFactory.decodeFile(path,op);
        }
        return bitmap;
    }
    public static Bitmap getSplashScreen(){
        return getBitmap(Splash_PATH);
    }

    public static Bitmap getLogo(){
        return getBitmap(Logo_PATH);
    }
    public static String getOEMName(){
        String oemName = "";
        try {
            FileReader fr = new FileReader(OEM_PATH);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            while ((str = bf.readLine()) != null) {
                oemName = str;
                break;
            }
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return oemName;
    }
    public static boolean isMicsig(){
        String oemName = getOEMName();
        if(oemName == null || oemName.isEmpty() || "micsig".equals(oemName)){
            return true;
        }
        return false;
    }
}
