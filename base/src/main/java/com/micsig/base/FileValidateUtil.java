package com.micsig.base; // 定义基础工具类包

import android.util.Log; // 导入Android日志类

import java.io.File; // 导入文件类
import java.io.FileInputStream; // 导入文件输入流类
import java.io.FileNotFoundException; // 导入文件未找到异常类
import java.io.IOException; // 导入IO异常类
import java.io.InputStream; // 导入输入流接口
import java.math.BigInteger; // 导入大整数类
import java.security.MessageDigest; // 导入消息摘要类
import java.security.NoSuchAlgorithmException; // 导入算法未找到异常类

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                         FileValidateUtil 文件校验工具                        │
 * │                          文件完整性验证与哈希计算                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   MHO示波器基础工具模块 - 文件安全组件                                        │
 * │   提供文件完整性验证功能，支持MD5/SHA1/SHA256哈希计算                         │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 计算文件的哈希值（MD5/SHA1/SHA256）                                     │
 * │   2. 验证文件与预期哈希值是否匹配                                            │
 * │   3. 支持固件升级包、配置文件的完整性校验                                     │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   基于Java MessageDigest实现哈希计算                                        │
 * │   采用流式读取方式处理大文件，避免内存溢出                                    │
 * │   提供TypeEnum枚举支持多种哈希算法                                           │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   文件 → InputStream → MessageDigest → 哈希值 → 字符串输出                   │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖: java.security.MessageDigest (哈希算法核心)                           │
 * │   依赖: java.math.BigInteger (十六进制转换)                                  │
 * │   被依赖: 固件升级模块、文件下载模块、配置验证模块                            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   // 验证文件MD5                                                           │
 * │   boolean valid = FileValidateUtil.validateFile(                           │
 * │       TypeEnum.MD5, "d41d8cd98f00b204e9800998ecf8427e", file);             │
 * │   // 验证固件SHA256                                                         │
 * │   boolean valid = FileValidateUtil.validateFile(                           │
 * │       TypeEnum.SHA256, expectedHash, firmwareFile);                        │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author Micsig R&D Team
 * @version 1.0
 * @since MHO Series Oscilloscope Software
 */
public class FileValidateUtil { // 文件校验工具类
    
    // ==================== 常量定义 ====================
    private static final String TAG = "FileValidateUtil"; // 日志标签

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 哈希算法类型枚举                                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   定义支持的哈希算法类型                                                 │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【枚举值】                                                              │
     * │   MD5    - 128位哈希，速度快，用于一般校验                               │
     * │   SHA1   - 160位哈希，安全性高于MD5                                      │
     * │   SHA256 - 256位哈希，安全性最高，用于固件验证                            │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public enum TypeEnum { // 哈希算法类型枚举
        MD5,    // MD5哈希算法，128位输出
        SHA1,   // SHA-1哈希算法，160位输出
        SHA256  // SHA-256哈希算法，256位输出
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 检查字符串是否为空                                                      │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   判断字符串是否为null或空字符串                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param str 待检查的字符串                                              │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return true表示为空，false表示非空                                    │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    private static boolean isEmpty(String str){ // 私有方法，检查字符串是否为空
        return str == null || str.isEmpty(); // 返回null检查或空字符串检查结果
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 验证文件哈希值                                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   计算文件的哈希值并与预期值比较，验证文件完整性                           │
     * │   用于固件升级、文件下载等场景的完整性校验                                 │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param typeEnum      哈希算法类型（MD5/SHA1/SHA256）                   │
     * │   @param standardStr   预期的哈希值（十六进制字符串）                     │
     * │   @param fileToCheck   待校验的文件对象                                  │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return true表示文件哈希匹配，验证通过                                  │
     * │           false表示验证失败（哈希不匹配或计算错误）                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   File file = new File("/sdcard/firmware.bin");                        │
     * │   boolean valid = FileValidateUtil.validateFile(                       │
     * │       TypeEnum.SHA256, "abc123...", file);                             │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static boolean validateFile(TypeEnum typeEnum, String standardStr, File fileToCheck) { // 验证文件哈希方法
        if (isEmpty(standardStr) || fileToCheck == null) { // 检查参数有效性
            Log.e(TAG, "MD5 string empty or updateFile null"); // 输出错误日志
            return false; // 参数无效，返回false
        }

        String calculatedDigest = getFileSignature(fileToCheck, typeEnum); // 计算文件哈希值

        if (isEmpty(calculatedDigest)) { // 检查计算结果是否有效
            Log.d(TAG, "calculatedDigest null"); // 输出调试日志
            return false; // 计算失败，返回false
        }
        return calculatedDigest.equalsIgnoreCase(standardStr); // 比较计算值与预期值（忽略大小写）
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 计算文件哈希签名                                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   使用指定算法计算文件的哈希值                                           │
     * │   采用流式读取方式，支持大文件处理                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param file     待计算的文件对象                                       │
     * │   @param typeEnum 哈希算法类型                                           │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return 文件的十六进制哈希字符串，失败返回null                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【算法说明】                                                            │
     * │   1. 根据类型创建MessageDigest实例                                       │
     * │   2. 以8KB缓冲区流式读取文件                                             │
     * │   3. 更新摘要并计算最终哈希                                              │
     * │   4. 转换为32位十六进制字符串输出                                        │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    private static String getFileSignature(File file, TypeEnum typeEnum) { // 计算文件哈希签名方法
        MessageDigest digest; // 声明消息摘要对象
        String type = ""; // 初始化算法类型字符串
        switch (typeEnum) { // 根据枚举类型选择算法
            case MD5: // MD5算法
                type = "MD5"; // 设置算法名称
                break; // 跳出switch
            case SHA1: // SHA-1算法
                type = "SHA-1"; // 设置算法名称
                break; // 跳出switch
            case SHA256: // SHA-256算法
                type = "SHA-256"; // 设置算法名称
                break; // 跳出switch
        }
        if (isEmpty(type)) { // 检查算法类型是否有效
            Log.e(TAG, "type undefined"); // 输出错误日志
            return null; // 返回null表示失败
        }
        try {
            digest = MessageDigest.getInstance(type); // 获取指定算法的MessageDigest实例
        } catch (NoSuchAlgorithmException e) { // 捕获算法不存在异常
            e.printStackTrace(); // 打印异常堆栈
            return null; // 返回null表示失败
        }

        InputStream is; // 声明输入流
        try {
            is = new FileInputStream(file); // 创建文件输入流
        } catch (FileNotFoundException e) { // 捕获文件未找到异常
            e.printStackTrace(); // 打印异常堆栈
            Log.e(TAG, "Exception while getting FileInputStream"); // 输出错误日志
            return null; // 返回null表示失败
        }

        byte[] buffer = new byte[8192]; // 创建8KB缓冲区，用于流式读取
        int read; // 声明读取字节数变量
        try {
            while ((read = is.read(buffer)) > 0) { // 循环读取文件内容
                digest.update(buffer, 0, read); // 更新消息摘要
            }
            byte[] md5sum = digest.digest(); // 计算最终哈希值
            BigInteger bigInt = new BigInteger(1, md5sum); // 转换为大整数
            String output = bigInt.toString(16); // 转换为十六进制字符串
            output = String.format("%32s", output).replace(' ', '0'); // 格式化为32位，不足补0
            return output; // 返回哈希字符串
        } catch (IOException e) { // 捕获IO异常
            e.printStackTrace(); // 打印异常堆栈
            Log.e(TAG,"Unable to process file for "); // 输出错误日志
            return null; // 返回null表示失败
        } finally { // 最终执行块
            try {
                is.close(); // 关闭输入流
            } catch (IOException e) { // 捕获关闭异常
                e.printStackTrace(); // 打印异常堆栈
                Log.e(TAG, "Exception on closing inputstream:" ); // 输出错误日志
            }
        }
    }

}
