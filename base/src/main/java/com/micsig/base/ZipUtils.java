package com.micsig.base; // 定义基础工具类包

import java.io.File; // 导入文件类
import java.io.FileInputStream; // 导入文件输入流类
import java.io.FileOutputStream; // 导入文件输出流类
import java.io.IOException; // 导入IO异常类
import java.io.InputStream; // 导入输入流接口
import java.util.zip.ZipEntry; // 导入ZIP条目类
import java.util.zip.ZipInputStream; // 导入ZIP输入流类

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                             ZipUtils ZIP工具类                              │
 * │                          ZIP文件解压缩处理器                                 │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   MHO示波器基础工具模块 - 文件压缩组件                                        │
 * │   提供ZIP文件解压缩功能                                                     │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 解压ZIP文件到指定目录                                                 │
 * │   2. 支持从文件路径解压                                                   │
 * │   3. 支持从输入流解压                                                     │
 * │   4. 自动创建目录结构                                                     │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   静态方法设计，无需实例化                                                  │
 * │   使用Java标准库ZipInputStream实现解压                                      │
 * │   支持大文件处理（4KB缓冲区）                                               │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   ZIP文件/流 → ZipInputStream → 解压 → 文件系统                            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖: java.util.zip.ZipInputStream (ZIP解压)                             │
 * │   被依赖: 固件升级、资源包解压、数据导入等模块                               │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   // 从文件路径解压                                                       │
 * │   ZipUtils.UnZipFolder("/sdcard/update.zip", "/sdcard/update/");          │
 * │   // 从输入流解压                                                         │
 * *   ZipUtils.UnZipFolder(inputStream, "/sdcard/output/");                    │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author Micsig R&D Team
 * @version 1.0
 * @since MHO Series Oscilloscope Software
 */
public class ZipUtils { // ZIP工具类
    
    // ==================== 常量定义 ====================
    private final static String TAG = "ZipUtils"; // 日志标签
    private final static int MAX_BUFFER = 4096; // 缓冲区大小：4KB

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 从输入流解压ZIP文件                                                     │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   从输入流解压ZIP内容到指定目录                                          │
     * │   适用于从网络下载或资源文件中解压                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param is       ZIP文件输入流                                         │
     * │   @param outPath  解压输出目录                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   InputStream is = assetManager.open("resources.zip");                 │
     * │   ZipUtils.UnZipFolder(is, "/sdcard/resources/");                      │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static void UnZipFolder(InputStream is, String outPath){ // 从输入流解压方法
        ZipInputStream inZip = null; // 声明ZIP输入流
        inZip = new ZipInputStream(is); // 创建ZIP输入流
        UnZipFolder(inZip, outPath); // 调用通用解压方法
        try {
            inZip.close(); // 关闭ZIP输入流
        } catch (IOException e) { // 捕获IO异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 从ZipInputStream解压                                                   │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   核心解压方法，从ZipInputStream解压所有条目                              │
     * │   自动处理目录和文件，创建必要的目录结构                                   │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param inZip    ZIP输入流                                             │
     * │   @param outPath  解压输出目录                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【算法说明】                                                            │
     * │   1. 遍历ZIP中的每个条目                                                │
     * │   2. 如果是目录，创建目录                                               │
     * │   3. 如果是文件，读取并写入到目标位置                                    │
     * │   4. 使用4KB缓冲区提高效率                                              │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static void UnZipFolder(ZipInputStream inZip, String outPath){ // 核心解压方法
        ZipEntry zipEntry; // 声明ZIP条目
        String szName = ""; // 条目名称
        FileOutputStream out = null; // 声明文件输出流
        byte[] buffer = new byte[MAX_BUFFER]; // 创建缓冲区
        int len = 0; // 读取长度
        try {
            while ((zipEntry = inZip.getNextEntry()) != null) { // 遍历ZIP条目
                szName = zipEntry.getName(); // 获取条目名称
                if (zipEntry.isDirectory()) { // 如果是目录
                    szName = szName.substring(0, szName.length() - 1); // 去除末尾的斜杠
                    File folder = new File(outPath, szName); // 创建目录文件对象
                    folder.mkdirs(); // 创建目录
                } else { // 如果是文件
                    File file = new File(outPath, szName); // 创建目标文件对象
                    if (!file.exists()) { // 检查文件是否存在
                        file.getParentFile().mkdirs(); // 创建父目录
                        file.createNewFile(); // 创建文件
                    }

                    out = new FileOutputStream(file); // 创建文件输出流

                    while ((len = inZip.read(buffer)) != -1) { // 循环读取数据
                        out.write(buffer, 0, len); // 写入文件
                    }
                    out.flush(); // 刷新缓冲区
                    out.close(); // 关闭输出流
                    out = null; // 重置输出流引用
                }
            }
        }catch (IOException e){ // 捕获IO异常
            e.printStackTrace(); // 打印异常堆栈
        }finally { // 最终执行块
            if(out != null){ // 检查输出流是否关闭
                try {
                    out.close(); // 关闭输出流
                } catch (IOException e) { // 捕获关闭异常
                    e.printStackTrace(); // 打印异常堆栈
                }
            }
        }
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 从文件路径解压ZIP文件                                                   │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   从指定路径解压ZIP文件到目标目录                                         │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param zipFile  ZIP文件路径                                           │
     * │   @param outPath  解压输出目录                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   ZipUtils.UnZipFolder("/sdcard/update.zip", "/sdcard/update/");       │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static void UnZipFolder(String zipFile, String outPath) { // 从文件路径解压方法
        ZipInputStream inZip = null; // 声明ZIP输入流
        try {
            inZip = new ZipInputStream(new FileInputStream(zipFile)); // 创建ZIP输入流
            UnZipFolder(inZip, outPath); // 调用通用解压方法
        }catch (IOException e){ // 捕获IO异常
            e.printStackTrace(); // 打印异常堆栈
        }finally { // 最终执行块
            if(inZip != null) { // 检查输入流是否创建
                try {
                    inZip.close(); // 关闭输入流
                } catch (IOException e) { // 捕获关闭异常
                    e.printStackTrace(); // 打印异常堆栈
                }
            }
        }
    }
}
