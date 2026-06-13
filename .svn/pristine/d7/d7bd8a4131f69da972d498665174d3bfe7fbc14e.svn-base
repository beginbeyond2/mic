package com.micsig.base;

import android.os.StatFs;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zhuzh on 2018-7-5.
 */

public class Utils {
    private static String TAG = "Utils";
    static {
        System.loadLibrary("baselib");
    }
    public static short CRC16(byte [] buf,int offset,int len){
        return crc16(buf,offset,len);
    }
    private static native short crc16(byte[] buf, int offset, int len);

    public static void InitSignal(){
        initSignal();
    }
    private static native void initSignal();
    public static long getDiskAvaiableSize(File path){
        try {
            StatFs stat = new StatFs(path.getParent());
            return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        return 0;
    }
    public static boolean isDiskAvaiable(File path,long useSize){
        long avaiableSize = getDiskAvaiableSize(path)-(1L<<30);
//        Log.d(TAG,"avaiableSize:" + avaiableSize + ",useSize:" + useSize +"," + path.getAbsolutePath());
        return avaiableSize > useSize;
    }
    public static String getXFromDouble(double x) {
        if (x < 1) {
            x = x * 1000;
            x += 0.1;
            return String.valueOf((int) x) + "mX";
        } else if (x >= 1000) {
            x = x / 1000;
            x += 0.1;
            return String.valueOf(((int) x)) + "kX";
        } else {
            x += 0.1;
            return String.valueOf(((int) x)) + "X";
        }
    }

    public static void delFile(File index) {
        if(index.exists()) {
            File[] files = index.listFiles();
            if(files != null) {
                for (File file : files) {
                    if (file.isDirectory())
                        delFile(file);
                    file.delete();
                }
            }
            index.delete();
        }
    }

    public static void saveFile(InputStream is,String outpath){
        File f = new File(outpath);
        if(f.exists()){
            f.delete();
        }
        FileOutputStream fos = null;
        try {
            byte [] buffer = new byte[4096];
            fos = new FileOutputStream(f);
            int len;
            while ((len = is.read(buffer))> 0){
                fos.write(buffer,0,len);
            }
            fos.flush();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readAll(InputStream is){
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = null;
        BufferedReader br = null;
        String line;
        try {
            sr = new InputStreamReader(is);
            br = new BufferedReader(sr);
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(br != null) br.close();
                if(sr != null) sr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }




    public static String readAll(String fileName){
        StringBuilder sb = new StringBuilder();
        File f = new File(fileName);
        if(f.exists()){
            FileInputStream fis = null;
            InputStreamReader sr = null;
            BufferedReader br = null;


            String line;
            try {
                fis = new FileInputStream(f);
                sr = new InputStreamReader(fis);
                br = new BufferedReader(sr);
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
            }catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(fis != null) fis.close();
                    if(br != null) br.close();
                    if(sr != null) sr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
    public static boolean copyFile(String srcFilePath,String dstFilePath){
        return copyFile(new File(srcFilePath),new File(dstFilePath));
    }
    public static boolean copyFile(File srcFile,File dstFile){
        boolean bRet = false;
        if(dstFile.exists()){
            dstFile.delete();
        }
        if(srcFile.exists()){
            int byteread = 0;
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(srcFile);
                dstFile.getParentFile().mkdirs();
                try {
                    dstFile.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(dstFile);
                    byte[] buffer = new byte[4096];
                    while ((byteread = inputStream.read(buffer)) != -1){
                        outputStream.write(buffer, 0, byteread);
                    }
                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();
                    bRet = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bRet;
    }
}
