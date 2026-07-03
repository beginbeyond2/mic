package com.micsig.tbook.tbookscope.tools;

import android.os.Environment;

import com.micsig.base.Logger;

import java.io.File;

/**
 * +-----------------------------------------------------------------------------+
 * |                           文件工具类 (FileUtils)                             |
 * +-----------------------------------------------------------------------------+
 * | 模块定位 : tbookscope.tools 通用工具层                                      |
 * | 核心职责 : 提供文件与目录的增删查、备份与恢复等操作                           |
 * | 架构设计 : 纯静态工具类，无状态，所有方法均为 static                          |
 * | 数据流向 : 外部传入路径字符串 → 内部构造 File 对象 → 执行文件系统操作        |
 * | 依赖关系 : android.os.Environment, com.micsig.base.Logger, java.io.File      |
 * | 使用场景 : 配置文件备份/恢复、临时文件清理、文件存在性校验                     |
 * +-----------------------------------------------------------------------------+
 */
public class FileUtils {

    /** 备份文件的后缀名 */ // 备份文件后缀常量
    public static final String BACKUP_FILE_SUFFIX = ".bak"; // 备份文件后缀为 .bak
    /** 日志标签 */ // 日志标签常量
    public static final String TAG = "FileUtils"; // 日志TAG为 FileUtils

    //删除文件夹和文件夹里面的文件
    /**
     * 删除指定路径的目录及其内部所有文件和子目录
     *
     * @param pPath 要删除的目录路径
     */
    public static void deleteDir(final String pPath) { // 根据路径字符串删除整个目录
        File dir = new File(pPath); // 将路径字符串构造为 File 对象
        deleteDirWihtFile(dir); // 调用递归删除方法
    }

    /**
     * 递归删除目录及其内部所有文件和子目录
     *
     * @param dir 要删除的目录对象
     */
    public static void deleteDirWihtFile(File dir) { // 递归删除目录及子文件
        if (dir == null || !dir.exists() || !dir.isDirectory()) // 目录为空、不存在或不是目录则直接返回
            return; // 退出方法
        for (File file : dir.listFiles()) { // 遍历目录下的所有文件和子目录
            if (file.isFile()) // 如果是文件
                file.delete(); // 删除所有文件
            else if (file.isDirectory()) // 如果是子目录
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    /**
     * 检查指定路径的文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件存在返回 true，否则返回 false；路径为空也返回 false
     */
    public static boolean checkFileExists(String filePath) { // 检查文件是否存在
        if (filePath == null || filePath.trim().isEmpty()) return false; // 路径为空或空白则返回 false
        File file = new File(filePath); // 根据路径构造 File 对象
        Logger.i(TAG, "文件是否存在 filePath= " + file.getAbsolutePath() + " ,isExists= " + file.exists()); // 记录文件存在性日志
        return file.exists(); // 返回文件是否存在的布尔值
    }

    /**
     * 检查指定路径的文件夹是否存在，支持路径前缀替换为外部存储根路径
     *
     * @param path 文件夹路径
     * @param pre  路径前缀标识，用于拼接外部存储根路径
     * @return 文件夹存在且是目录返回 true，否则返回 false
     */
    public static boolean checkFolderExists(String path, String pre) { // 检查文件夹是否存在
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath(); // 获取外部存储根路径
//        Logger.i(TAG, "文件夹是否存在: " + path + " rootPath= " + rootPath + " ,pre= " + pre);
        if (path.startsWith(File.separator + pre)) { // 如果路径以 "/前缀" 开头
            path = path.substring(pre.length() + 1); // 截取去掉前缀部分的路径
            path = rootPath + path; // 拼接外部存储根路径
        }
        if (path.trim().isEmpty()) { // 路径为空白
            return false; // 返回 false
        }
        Logger.i(TAG, "文件夹是否存在 finalPath: " + path); // 记录最终路径日志
        File folder = new File(path); // 根据路径构造 File 对象
        return folder.isDirectory() && folder.exists(); // 判断是否为目录且存在
    }

    /**
     * 将原始文件重命名为备份文件（添加 .bak 后缀）
     *
     * @param originalFilePath 原始文件路径
     */
    public static void createBakFile(String originalFilePath) { // 创建备份文件
        File originFile = new File(originalFilePath); // 构造原始文件对象
        if (!originFile.exists()) return; // 原始文件不存在则直接返回
        String backUpFilePath = originalFilePath + BACKUP_FILE_SUFFIX; // 拼接备份文件路径
        File bakFile = new File(backUpFilePath); // 构造备份文件对象
        if (bakFile.exists()) bakFile.delete(); // 备份文件已存在则先删除
        if (originFile.renameTo(bakFile)) { // 将原始文件重命名为备份文件
            Logger.i(TAG, "文件备份成功： " + backUpFilePath); // 记录备份成功日志
        }
    }

    /**
     * 删除指定路径的文件
     *
     * @param filePath 文件路径
     * @return 文件删除成功或文件不存在返回 true，删除失败返回 false
     */
    public static boolean deleteFile(String filePath) { // 删除文件
        if (filePath == null || filePath.trim().isEmpty()) return true; // 路径为空视为删除成功
        File file = new File(filePath); // 构造文件对象
        if (!file.exists()) { // 文件不存在
            Logger.i(TAG, "要删除的文件不存在： " + filePath); // 记录文件不存在日志
            return true; // 文件不存在视为删除成功
        }
        boolean isDelete = file.delete(); // 执行删除操作
        if (isDelete) { // 删除成功
            Logger.i(TAG, "文件删除成功： " + filePath); // 记录删除成功日志
        } else { // 删除失败
            Logger.i(TAG, "文件删除失败： " + filePath); // 记录删除失败日志
        }
        return isDelete; // 返回删除结果
    }

    /**
     * 从备份文件恢复原始文件（将 .bak 后缀去掉重命名回原文件名）
     *
     * @param backUpFilePath 备份文件路径（必须以 .bak 结尾）
     */
    public static void restoreFromBakFile(String backUpFilePath) { // 从备份文件恢复
        if (backUpFilePath == null || backUpFilePath.trim().isEmpty()) return; // 路径为空则直接返回
        if (!backUpFilePath.endsWith(BACKUP_FILE_SUFFIX)) { // 路径不以 .bak 结尾
            Logger.i(TAG, "不符合备份文件要求： " + backUpFilePath); // 记录不符合要求日志
            return; // 退出方法
        }
        File backUpFile = new File(backUpFilePath); // 构造备份文件对象
        if (!backUpFile.exists()) { // 备份文件不存在
            Logger.i(TAG, "备份文件不存在，无法恢复= " + backUpFilePath); // 记录备份文件不存在日志
            return; // 退出方法
        }
        File originalFile = new File(backUpFilePath.replace(BACKUP_FILE_SUFFIX, "")); // 去掉 .bak 后缀得到原文件路径
        if (originalFile.exists()) { // 原文件已存在
            Logger.i(TAG, "原文件已存在，无需恢复！"); // 记录原文件已存在日志
            backUpFile.delete(); // 删除多余的备份文件
        }
        if (backUpFile.renameTo(originalFile)) { // 将备份文件重命名为原文件
            Logger.i(TAG, "从备份文件恢复成功： " + originalFile.getAbsolutePath()); // 记录恢复成功日志
        } else { // 恢复失败
            Logger.i(TAG, "从备份文件恢复失败： " + backUpFilePath); // 记录恢复失败日志
        }
    }

}
