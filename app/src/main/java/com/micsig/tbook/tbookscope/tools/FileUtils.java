package com.micsig.tbook.tbookscope.tools;

import android.os.Environment;

import com.micsig.base.Logger;

import java.io.File;

public class FileUtils {

    public static final String BACKUP_FILE_SUFFIX = ".bak";
    public static final String TAG = "FileUtils";

    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    public static boolean checkFileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return false;
        File file = new File(filePath);
        Logger.i(TAG, "文件是否存在 filePath= " + file.getAbsolutePath() + " ,isExists= " + file.exists());
        return file.exists();
    }

    public static boolean checkFolderExists(String path, String pre) {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        Logger.i(TAG, "文件夹是否存在: " + path + " rootPath= " + rootPath + " ,pre= " + pre);
        if (path.startsWith(File.separator + pre)) {
            path = path.substring(pre.length() + 1);
            path = rootPath + path;
        }
        if (path.trim().isEmpty()) {
            return false;
        }
        Logger.i(TAG, "文件夹是否存在 finalPath: " + path);
        File folder = new File(path);
        return folder.isDirectory() && folder.exists();
    }

    public static void createBakFile(String originalFilePath) {
        File originFile = new File(originalFilePath);
        if (!originFile.exists()) return;
        String backUpFilePath = originalFilePath + BACKUP_FILE_SUFFIX;
        File bakFile = new File(backUpFilePath);
        if (bakFile.exists()) bakFile.delete();
        if (originFile.renameTo(bakFile)) {
            Logger.i(TAG, "文件备份成功： " + backUpFilePath);
        }
    }

    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return true;
        File file = new File(filePath);
        if (!file.exists()) {
            Logger.i(TAG, "要删除的文件不存在： " + filePath);
            return true;
        }
        boolean isDelete = file.delete();
        if (isDelete) {
            Logger.i(TAG, "文件删除成功： " + filePath);
        } else {
            Logger.i(TAG, "文件删除失败： " + filePath);
        }
        return isDelete;
    }

    public static void restoreFromBakFile(String backUpFilePath) {
        if (backUpFilePath == null || backUpFilePath.trim().isEmpty()) return;
        if (!backUpFilePath.endsWith(BACKUP_FILE_SUFFIX)) {
            Logger.i(TAG, "不符合备份文件要求： " + backUpFilePath);
            return;
        }
        File backUpFile = new File(backUpFilePath);
        if (!backUpFile.exists()) {
            Logger.i(TAG, "备份文件不存在，无法恢复= " + backUpFilePath);
            return;
        }
        File originalFile = new File(backUpFilePath.replace(BACKUP_FILE_SUFFIX, ""));
        if (originalFile.exists()) {
            Logger.i(TAG, "原文件已存在，无需恢复！");
            backUpFile.delete();
        }
        if (backUpFile.renameTo(originalFile)) {
            Logger.i(TAG, "从备份文件恢复成功： " + originalFile.getAbsolutePath());
        } else {
            Logger.i(TAG, "从备份文件恢复失败： " + backUpFilePath);
        }
    }

}
