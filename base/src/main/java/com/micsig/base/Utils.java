package com.micsig.base; // 定义基础工具类包

import android.os.StatFs; // 导入文件系统状态类
import android.util.Log; // 导入Android日志类

import java.io.BufferedReader; // 导入缓冲读取器类
import java.io.File; // 导入文件类
import java.io.FileInputStream; // 导入文件输入流类
import java.io.FileNotFoundException; // 导入文件未找到异常类
import java.io.FileOutputStream; // 导入文件输出流类
import java.io.IOException; // 导入IO异常类
import java.io.InputStream; // 导入输入流接口
import java.io.InputStreamReader; // 导入输入流读取器类

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                              Utils 系统工具类                               │
 * │                          文件操作与系统功能封装                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   MHO示波器基础工具模块 - 系统工具组件                                        │
 * │   提供文件操作、CRC校验、磁盘检测等系统级功能                                 │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 提供CRC16校验功能（JNI实现）                                           │
 * │   2. 提供磁盘空间检测功能                                                  │
 * │   3. 提供文件读写操作工具                                                  │
 * │   4. 提供文件删除、复制等操作                                              │
 * │   5. 提供数值格式化工具                                                   │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   静态方法设计，无需实例化                                                  │
 * │   部分功能通过JNI调用本地库（libbaselib.so）                                 │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   文件/数据 → 工具方法 → 处理结果                                          │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖: android.os.StatFs (文件系统状态)                                    │
 * │   依赖: libbaselib.so (JNI本地库)                                          │
 * │   被依赖: 文件管理、数据存储、波形保存、固件升级等模块                         │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   short crc = Utils.CRC16(data, 0, len);  // 计算CRC16                    │
 * │   long space = Utils.getDiskAvaiableSize(file);  // 获取磁盘可用空间        │
 * │   Utils.saveFile(inputStream, outputPath);  // 保存文件                    │
 * │   Utils.delFile(directory);  // 删除目录                                   │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-7-5
 */
public class Utils { // 系统工具类
    
    // ==================== 常量定义 ====================
    private static String TAG = "Utils"; // 日志标签
    
    // ==================== 静态初始化块 ====================
    static {
        System.loadLibrary("baselib"); // 加载JNI本地库
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 计算CRC16校验值                                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   计算指定字节数组的CRC16校验值                                          │
     * │   通过JNI调用本地库实现，性能优于Java实现                                 │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param buf    待计算的字节数组                                        │
     * │   @param offset 起始偏移量                                              │
     * │   @param len    计算长度                                                │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return CRC16校验值（16位短整型）                                      │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   byte[] data = {0x01, 0x02, 0x03, 0x04};                              │
     * │   short crc = Utils.CRC16(data, 0, data.length);                       │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static short CRC16(byte [] buf, int offset, int len){ // CRC16计算方法
        return crc16(buf, offset, len); // 调用JNI本地方法
    }
    
    /**
     * CRC16计算的JNI本地方法
     * @param buf 字节数组
     * @param offset 偏移量
     * @param len 长度
     * @return CRC16值
     */
    private static native short crc16(byte[] buf, int offset, int len); // JNI本地方法声明

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 初始化信号处理                                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   初始化本地信号处理器，用于捕获和处理系统信号                              │
     * │   通过JNI调用本地库实现                                                 │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   Utils.InitSignal();  // 在应用启动时调用                              │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static void InitSignal(){ // 初始化信号处理方法
        initSignal(); // 调用JNI本地方法
    }
    
    /**
     * 信号初始化的JNI本地方法
     */
    private static native void initSignal(); // JNI本地方法声明

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 获取磁盘可用空间                                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   获取指定文件所在磁盘分区的可用空间大小                                   │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param path 文件对象，用于确定所在分区                                  │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return 可用空间大小（字节），失败返回0                                 │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   File storage = new File("/sdcard");                                  │
     * │   long freeSpace = Utils.getDiskAvaiableSize(storage);                 │
     * │   // 返回: 可用字节数                                                   │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static long getDiskAvaiableSize(File path){ // 获取磁盘可用空间方法
        try {
            StatFs stat = new StatFs(path.getParent()); // 获取父目录的文件系统状态
            return stat.getBlockSizeLong() * stat.getAvailableBlocksLong(); // 计算可用空间 = 块大小 * 可用块数
        }catch (IllegalArgumentException e){ // 捕获非法参数异常
            e.printStackTrace(); // 打印异常堆栈
        }
        return 0; // 失败返回0
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 检查磁盘是否有足够空间                                                  │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   检查指定位置是否有足够的磁盘空间                                        │
     * │   预留1GB作为安全缓冲区                                                 │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param path     文件对象，用于确定所在分区                              │
     * │   @param useSize  需要的空间大小（字节）                                  │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return true表示空间充足，false表示空间不足                             │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   if (Utils.isDiskAvaiable(file, 100 * 1024 * 1024)) {                 │
     * │       // 有100MB以上可用空间（含1GB缓冲）                                 │
     * │       saveLargeFile();                                                 │
     * │   }                                                                    │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static boolean isDiskAvaiable(File path, long useSize){ // 检查磁盘空间方法
        long avaiableSize = getDiskAvaiableSize(path) - (1L << 30); // 计算可用空间，预留1GB缓冲
//        Log.d(TAG,"avaiableSize:" + avaiableSize + ",useSize:" + useSize +"," + path.getAbsolutePath());
        return avaiableSize > useSize; // 返回是否有足够空间
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 将double值格式化为X单位字符串                                           │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   将数值转换为带单位的字符串表示                                          │
     * │   支持mX（毫）、X（基本单位）、kX（千）三种单位                            │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param x 数值（基本单位）                                              │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return 格式化后的字符串，如 "100mX"、"1.5kX"                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   String s1 = Utils.getXFromDouble(0.5);   // "500mX"                  │
     * │   String s2 = Utils.getXFromDouble(1.5);   // "1X"                     │
     * │   String s3 = Utils.getXFromDouble(1500);  // "1kX"                    │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static String getXFromDouble(double x) { // 数值格式化方法
        if (x < 1) { // 小于1，使用毫单位
            x = x * 1000; // 转换为毫单位
            x += 0.1; // 加0.1用于四舍五入
            return String.valueOf((int) x) + "mX"; // 返回毫单位字符串
        } else if (x >= 1000) { // 大于等于1000，使用千单位
            x = x / 1000; // 转换为千单位
            x += 0.1; // 加0.1用于四舍五入
            return String.valueOf(((int) x)) + "kX"; // 返回千单位字符串
        } else { // 1到1000之间，使用基本单位
            x += 0.1; // 加0.1用于四舍五入
            return String.valueOf(((int) x)) + "X"; // 返回基本单位字符串
        }
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 递归删除文件或目录                                                      │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   递归删除指定文件或目录及其所有内容                                      │
     * │   如果是目录，会先删除目录内所有文件和子目录                               │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param index 要删除的文件或目录对象                                    │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   File dir = new File("/sdcard/temp");                                 │
     * │   Utils.delFile(dir);  // 删除整个temp目录                              │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static void delFile(File index) { // 递归删除文件方法
        if(index.exists()) { // 检查文件是否存在
            File[] files = index.listFiles(); // 获取目录下所有文件
            if(files != null) { // 检查是否有子文件
                for (File file : files) { // 遍历所有子文件
                    if (file.isDirectory()) // 如果是目录
                        delFile(file); // 递归删除子目录
                    file.delete(); // 删除文件
                }
            }
            index.delete(); // 删除当前目录或文件
        }
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 保存输入流到文件                                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   将输入流数据保存到指定文件                                              │
     * │   如果文件已存在，会先删除再创建                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param is       输入流                                                │
     * │   @param outpath  输出文件路径                                           │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   InputStream is = assetManager.open("config.bin");                    │
     * │   Utils.saveFile(is, "/sdcard/config.bin");                            │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static void saveFile(InputStream is, String outpath){ // 保存文件方法
        File f = new File(outpath); // 创建文件对象
        if(f.exists()){ // 检查文件是否存在
            f.delete(); // 删除已存在的文件
        }
        FileOutputStream fos = null; // 声明文件输出流
        try {
            byte [] buffer = new byte[4096]; // 创建4KB缓冲区
            fos = new FileOutputStream(f); // 创建文件输出流
            int len; // 声明读取长度变量
            while ((len = is.read(buffer)) > 0){ // 循环读取数据
                fos.write(buffer, 0, len); // 写入文件
            }
            fos.flush(); // 刷新缓冲区
        }catch (IOException e){ // 捕获IO异常
            e.printStackTrace(); // 打印异常堆栈
        }finally { // 最终执行块
            if(fos != null){ // 检查输出流是否创建
                try {
                    fos.close(); // 关闭输出流
                } catch (IOException e) { // 捕获关闭异常
                    e.printStackTrace(); // 打印异常堆栈
                }
            }
        }
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 读取输入流全部内容                                                      │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   读取输入流的所有内容并返回字符串                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param is 输入流                                                     │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return 输入流内容的字符串表示                                         │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   InputStream is = assetManager.open("config.txt");                    │
     * │   String content = Utils.readAll(is);                                  │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static String readAll(InputStream is){ // 读取输入流方法
        StringBuilder sb = new StringBuilder(); // 创建字符串构建器
        InputStreamReader sr = null; // 声明输入流读取器
        BufferedReader br = null; // 声明缓冲读取器
        String line; // 声明行字符串变量
        try {
            sr = new InputStreamReader(is); // 创建输入流读取器
            br = new BufferedReader(sr); // 创建缓冲读取器
            while ((line = br.readLine()) != null) { // 逐行读取
                sb.append(line + "\n"); // 追加到构建器，保留换行
            }
        }catch (IOException e) { // 捕获IO异常
            e.printStackTrace(); // 打印异常堆栈
        }finally { // 最终执行块
            try {
                if(br != null) br.close(); // 关闭缓冲读取器
                if(sr != null) sr.close(); // 关闭输入流读取器
            } catch (IOException e) { // 捕获关闭异常
                e.printStackTrace(); // 打印异常堆栈
            }
        }
        return sb.toString(); // 返回读取的内容
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 读取文件全部内容                                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   读取指定文件的所有内容并返回字符串                                      │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param fileName 文件路径                                              │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return 文件内容的字符串表示，文件不存在返回空字符串                     │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   String content = Utils.readAll("/sdcard/config.txt");                │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static String readAll(String fileName){ // 读取文件方法
        StringBuilder sb = new StringBuilder(); // 创建字符串构建器
        File f = new File(fileName); // 创建文件对象
        if(f.exists()){ // 检查文件是否存在
            FileInputStream fis = null; // 声明文件输入流
            InputStreamReader sr = null; // 声明输入流读取器
            BufferedReader br = null; // 声明缓冲读取器

            String line; // 声明行字符串变量
            try {
                fis = new FileInputStream(f); // 创建文件输入流
                sr = new InputStreamReader(fis); // 创建输入流读取器
                br = new BufferedReader(sr); // 创建缓冲读取器
                while ((line = br.readLine()) != null) { // 逐行读取
                    sb.append(line + "\n"); // 追加到构建器，保留换行
                }
            }catch (IOException e) { // 捕获IO异常
                e.printStackTrace(); // 打印异常堆栈
            }finally { // 最终执行块
                try {
                    if(fis != null) fis.close(); // 关闭文件输入流
                    if(br != null) br.close(); // 关闭缓冲读取器
                    if(sr != null) sr.close(); // 关闭输入流读取器
                } catch (IOException e) { // 捕获关闭异常
                    e.printStackTrace(); // 打印异常堆栈
                }
            }
        }
        return sb.toString(); // 返回读取的内容
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 复制文件（路径参数）                                                    │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   复制源文件到目标路径                                                  │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param srcFilePath 源文件路径                                         │
     * │   @param dstFilePath 目标文件路径                                       │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return true表示复制成功，false表示失败                                │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   boolean success = Utils.copyFile("/sdcard/a.txt", "/sdcard/b.txt");  │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static boolean copyFile(String srcFilePath, String dstFilePath){ // 复制文件方法（路径参数）
        return copyFile(new File(srcFilePath), new File(dstFilePath)); // 调用文件对象版本
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 复制文件（File对象参数）                                                │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   复制源文件到目标文件                                                  │
     * │   如果目标文件已存在，会先删除再复制                                      │
     * │   自动创建目标文件的父目录                                               │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param srcFile 源文件对象                                             │
     * │   @param dstFile 目标文件对象                                           │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return true表示复制成功，false表示失败                                │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   File src = new File("/sdcard/source.bin");                           │
     * │   File dst = new File("/sdcard/backup/source.bin");                    │
     * │   boolean success = Utils.copyFile(src, dst);                          │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static boolean copyFile(File srcFile, File dstFile){ // 复制文件方法（File参数）
        boolean bRet = false; // 初始化返回值为false
        if(dstFile.exists()){ // 检查目标文件是否存在
            dstFile.delete(); // 删除已存在的目标文件
        }
        if(srcFile.exists()){ // 检查源文件是否存在
            int byteread = 0; // 声明读取字节数变量
            InputStream inputStream = null; // 声明输入流
            try {
                inputStream = new FileInputStream(srcFile); // 创建文件输入流
                dstFile.getParentFile().mkdirs(); // 创建目标文件的父目录
                try {
                    dstFile.createNewFile(); // 创建目标文件
                    FileOutputStream outputStream = new FileOutputStream(dstFile); // 创建文件输出流
                    byte[] buffer = new byte[4096]; // 创建4KB缓冲区
                    while ((byteread = inputStream.read(buffer)) != -1){ // 循环读取数据
                        outputStream.write(buffer, 0, byteread); // 写入目标文件
                    }
                    outputStream.flush(); // 刷新缓冲区
                    inputStream.close(); // 关闭输入流
                    outputStream.close(); // 关闭输出流
                    bRet = true; // 设置返回值为true
                } catch (IOException e) { // 捕获IO异常
                    e.printStackTrace(); // 打印异常堆栈
                }
            } catch (FileNotFoundException e) { // 捕获文件未找到异常
                e.printStackTrace(); // 打印异常堆栈
            }
        }
        return bRet; // 返回操作结果
    }
}
