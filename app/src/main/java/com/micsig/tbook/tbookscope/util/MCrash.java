package com.micsig.tbook.tbookscope.util; // 工具包，存放示波器应用的各类工具类

import android.content.Context; // Android上下文，用于获取应用环境信息

import com.micsig.tbook.tbookscope.tools.Tools; // 工具类，提供SMART_PATH和SCOPE_PATH等路径常量

import java.io.File; // 文件操作类，用于路径判断和文件创建

import xcrash.ICrashCallback; // XCrash崩溃回调接口，崩溃发生时触发
import xcrash.TombstoneManager; // XCrash墓碑管理器，用于删除原始崩溃日志
import xcrash.XCrash; // XCrash核心类，负责Java/ANR/Native崩溃捕获

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                            MCrash - 崩溃管理类                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：MHO示波器Android应用 → 工具模块(util) → 崩溃管理                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                    │
 * │   1. 封装XCrash库，统一管理Java/ANR/Native三种崩溃的捕获                      │
 * │   2. Debug模式：仅记录崩溃日志到本地，方便开发调试                              │
 * │   3. Release模式：崩溃日志加密（RSA+AES混合加密）后删除原始日志，保护用户隐私    │
 * │   4. 自动选择日志存储路径（smart或MicsigTBook目录）                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                    │
 * │   - 饿汉式单例模式，类加载时即创建实例，线程安全                                │
 * │   - 通过ICrashCallback回调在崩溃日志写入后执行加密逻辑                         │
 * │   - 加密流程：原始崩溃日志 → HybridCryptoManager(RSA+AES) → .Mcrash加密文件  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：                                                                    │
 * │   崩溃发生 → XCrash捕获并写入.log文件 → callback回调触发                      │
 * │   → HybridCryptoManager加密 → 生成.Mcrash文件 → 删除原始.log文件              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：                                                                    │
 * │   - XCrash：第三方崩溃捕获库                                                   │
 * │   - HybridCryptoManager：RSA+AES混合加密管理器（同包）                         │
 * │   - Tools：路径常量提供（SMART_PATH/SCOPE_PATH）                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例：                                                                    │
 * │   MCrash.getInstance().init(getApplicationContext(), BuildConfig.DEBUG);      │
 * │   // 在Application.onCreate()中调用，传入是否Debug模式                         │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MCrash {

    /** RSA密钥算法名称，用于HybridCryptoManager密钥导入时指定算法类型 */
    private static final String RSA_KEY_ALGORITHM = "RSA"; // RSA非对称加密算法标识

    /** RSA密钥长度（位），2048位提供足够的安全强度 */
    private static final int KEY_SIZE = 2048; // 密钥长度，单位：bit

    /**
     * 崩溃回调处理器，仅在Release模式下注册。
     * 当崩溃日志写入完成后触发，负责：
     * 1. 导入内嵌的RSA公钥
     * 2. 使用HybridCryptoManager对崩溃日志进行RSA+AES混合加密
     * 3. 删除原始未加密的崩溃日志文件
     *
     * 加密文件命名规则：原始文件名去掉扩展名后追加"Mcrash"
     * 例如：crash_2024.log → crash_2024.Mcrash
     */
    private static final ICrashCallback callback = new ICrashCallback() { // 崩溃回调实例
        /**
         * 崩溃发生后的回调方法
         * @param logPath   崩溃日志文件的绝对路径（XCrash写入的原始.log文件）
         * @param emergency 紧急信息（XCrash在极度危险情况下传递的附加信息）
         * @throws Exception 加密或文件操作可能抛出的异常
         */
        @Override
        public void onCrash(String logPath, String emergency) throws Exception {
            HybridCryptoManager cryptoManager = new HybridCryptoManager(); // 创建混合加密管理器实例
            cryptoManager.importPublicKeyFromPem("-----BEGIN PUBLIC KEY-----" + // 导入内嵌的RSA公钥（PEM格式）
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsGTluAC7SPIxSx01vCFN" + // 公钥Base64内容第1行
                    "2g5vOVdikZKzpqcxZ5Mxyv9BqnSWQ1BiYdC4C098V4KuztKYUDaKkTVs/HdHr3dc" + // 公钥Base64内容第2行
                    "2D9cb4erdshK/k9GI1QXgIcDHeTAWWHJHPPX56xoTAIZWVEm+JJyxBnwrdZfrOoi" + // 公钥Base64内容第3行
                    "CWoQjWodGe8hOG+IMGr3JIjCH1O73AogVlkmSIv1oPNCo8I19+ie6kvfPKhaNZzx" + // 公钥Base64内容第4行
                    "9cdBrqnNxpJsuOZZ1GAAUfCYu+YOJLe60S4nHqEsgsQNnueHfDk2IkbqF5s9w6SA" + // 公钥Base64内容第5行
                    "QlAnaSc5HbI3011QKnZOPjYiJTUW+UZHu/3HXBS6KWfKDfs6xpwwPYF4bJyYbPj2" + // 公钥Base64内容第6行
                    "HwIDAQAB" + // 公钥Base64内容最后一行
                    "-----END PUBLIC KEY-----"); // PEM格式结束标记
            File input = new File(logPath); // 创建原始崩溃日志文件对象
            File outputPath = new File(logPath.substring(0, logPath.lastIndexOf(".") + 1) + "Mcrash"); // 加密输出路径：替换扩展名为.Mcrash

            // 使用RSA+AES混合加密对崩溃日志进行加密
            cryptoManager.encryptFile(input, outputPath); // 加密原始日志文件，输出为.Mcrash加密文件
            TombstoneManager.deleteTombstone(logPath); // 删除原始未加密的崩溃日志，防止敏感信息泄露
//            //解密 需要私钥（仅开发环境使用，Release版本不包含私钥）
//            cryptoManager.decryptFile(outputPath, deoutput); // 解密操作需要私钥，此处仅作注释保留
        }
    };

    /** 饿汉式单例实例，类加载时即创建，保证线程安全 */
    private static MCrash instance = new MCrash(); // 饿汉式单例，JVM保证线程安全

    /**
     * 获取MCrash单例实例
     * @return MCrash唯一实例
     */
    public static MCrash getInstance() { // 饿汉式单例获取方法
        return instance; // 返回预先创建的实例
    }

    /**
     * 初始化崩溃捕获系统。
     * 根据isDebug参数决定是否注册加密回调：
     * - Debug模式：仅记录崩溃日志到指定目录，不加密，方便开发调试
     * - Release模式：注册Java/ANR/Native三种崩溃回调，崩溃日志自动加密后删除原始文件
     *
     * 日志存储路径自动选择：
     * - 若 /storage/emulated/0/smart 目录存在 → 使用 smart/log
     * - 否则 → 使用 MicsigTBook/log（兼容旧版路径）
     *
     * @param context 应用上下文，XCrash初始化需要
     * @param isDebug 是否为Debug模式。true=仅记录日志；false=加密后删除原始日志
     * @throws Exception XCrash初始化失败时抛出
     */
    public void init(Context context, boolean isDebug) throws Exception {
        // 检测smart目录是否存在，决定日志存储的基础路径
        File f = new File("/storage/emulated/0/smart"); // 检测smart目录
        String scopePath; // 示波器存储基础路径名
        if (f.exists()) { // smart目录存在（新版设备）
            scopePath = Tools.SMART_PATH; // 使用"smart"作为路径名
        } else { // smart目录不存在（旧版设备）
            scopePath = Tools.SCOPE_PATH; // 使用"Oscilloscope"作为路径名（旧版兼容）
        }
        String logPath = "/storage/emulated/0/" + scopePath + "/log"; // 拼接完整的日志存储路径

        // 根据Debug/Release模式初始化XCrash
        if (isDebug) { // Debug模式：仅记录日志，不注册加密回调
            XCrash.InitParameters parameters = new XCrash.InitParameters() // 创建初始化参数
                    .setLogDir(logPath); // 仅设置日志目录
            XCrash.init(context, parameters); // 初始化XCrash（不注册回调）
        } else { // Release模式：注册加密回调，崩溃日志自动加密
            XCrash.InitParameters parameters = new XCrash.InitParameters() // 创建初始化参数
                    .setJavaCallback(callback) // 注册Java崩溃回调（加密处理）
                    .setAnrCallback(callback) // 注册ANR崩溃回调（加密处理）
                    .setNativeCallback(callback) // 注册Native崩溃回调（加密处理）
                    .setLogDir(logPath); // 设置日志目录
            XCrash.init(context, parameters); // 初始化XCrash（注册所有回调）
        }

    }

}
