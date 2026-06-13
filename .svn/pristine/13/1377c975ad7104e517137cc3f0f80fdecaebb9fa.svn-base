package com.micsig.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FLog {
    private static final String TAG = "FLog";

    //region 单例
    private static FLog INSTANCE = new FLog();

    public static FLog getInstance() {
        return INSTANCE;
    }
    //endregion

    private boolean bIsNoLog;
    private Context context;
    private String bugName = "";
    private String filePath = "";
    private String fileName = "";
    private String pathName = "";
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public void init(Context context, boolean isNoLog) {
        this.context = context;
        this.bIsNoLog = isNoLog;
        if (isNoLog) return;

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Alarms/";
        long timestamp = System.currentTimeMillis();
        String time = formatter.format(new Date());
        bugName = "bugFlog";
        fileName = bugName + "-" + time + "-" + timestamp + ".log";
        pathName = filePath + fileName;

        Append(TAG, getVersionInfo());
    }

    public void init(String name, boolean isNoLog) {
        this.bIsNoLog = isNoLog;
        if (isNoLog) return;
        if (bugName.isEmpty()) this.bIsNoLog = true;

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Alarms/";
        long timestamp = System.currentTimeMillis();
        String time = formatter.format(new Date());
        bugName = name;
        fileName = bugName + "-" + time + "-" + timestamp + ".log";
        pathName = filePath + fileName;

        Append(TAG, getVersionInfo());
    }

    public void Append(String TAG, String content) {
        if (bIsNoLog) return;
        Logger.i(TAG, content + "< " + bugName + " debug >");
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(pathName, true);
            writer.write(TAG + ":" + content + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(uDiskUpdate(pathName).contains("failture")) {
            Logger.i(TAG, "******Refresh data in file is Failture!!******");
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private String getVersionInfo() {
        String title = "";
        try {
            if (this.context != null) {
                PackageManager pm = this.context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(this.context.getPackageName(),
                        PackageManager.GET_ACTIVITIES);
                if (pi != null) {
                    String versionName = pi.versionName == null ? "null"
                            : pi.versionName;
                    String versionCode = pi.versionCode + "";
                    title = "versionName:" + versionName + "    versionCode:" + versionCode;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        return title;
    }

    public String getFilePathName() {
        if(bugName.isEmpty() || bIsNoLog) return "";
        return pathName;
    }

    public String uDiskUpdate(String path) {
        if (this.context == null) {
            return "MainActiveContext is null !!failture";
        }
        if (bIsNoLog) {
            return "Flag bIsNoLog is set true !!failture";
        }
        if (path.isEmpty()) {
            return "There is not defined debug log file !!failture";
        }
        File f = new File(path);
        if (f.exists()) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.parse("file://" + path);
            mediaScanIntent.setData(contentUri);
            this.context.sendBroadcast(mediaScanIntent);
            return "Refresh Log file["+pathName+"]"+"is successfull !!success";
        } else {
            return "File["+pathName+"]"+" is not exists! Check it !!failture";
        }
    }

}

