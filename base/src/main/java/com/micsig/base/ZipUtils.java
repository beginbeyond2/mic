package com.micsig.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    private final static String TAG = "ZipUtils";
    private final static int MAX_BUFFER = 4096;

    public static void UnZipFolder(InputStream is,String outPath){
        ZipInputStream inZip = null;
        inZip = new ZipInputStream(is);
        UnZipFolder(inZip,outPath);
        try {
            inZip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void UnZipFolder(ZipInputStream inZip,String outPath){
        ZipEntry zipEntry;
        String szName = "";
        FileOutputStream out = null;
        byte[] buffer = new byte[MAX_BUFFER];
        int len = 0;
        try {
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPath , szName);
                    folder.mkdirs();
                } else {

                    File file = new File(outPath , szName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }

                    out = new FileOutputStream(file);

                    while ((len = inZip.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                    out.close();
                    out = null;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void UnZipFolder(String zipFile, String outPath) {
        ZipInputStream inZip = null;
        try {
            inZip = new ZipInputStream(new FileInputStream(zipFile));
            UnZipFolder(inZip,outPath);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(inZip != null) {
                try {
                    inZip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
