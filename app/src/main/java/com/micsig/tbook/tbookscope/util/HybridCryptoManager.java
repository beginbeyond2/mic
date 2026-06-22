package com.micsig.tbook.tbookscope.util; // // 工具类包，存放应用级工具组件

/*
 * =====================================================================
 * |  _   _  ___  ____ _____ _____   ____                              |
 * | | | | |/ _ \/ ___|_   _|  ___| |  _ \ ___  ___ _ __   ___         |
 * | | |_| | | | \___ \ | | | |_    | |_) / _ \/ __| '_ \ / __|        |
 * | |  _  | |_| |___) || | |  _|   |  _ <  __/\__ \ |_) | (__         |
 * | |_| |_|\___/|____/ |_| |_|     |_| \_\___||___/ .__/ \___|        |
 * |                                                |_|                  |
 * |  模块名称: HybridCryptoManager (混合加密管理器)                        |
 * |  所属层级: com.micsig.tbook.tbookscope.util                         |
 * |  核心职责: 实现RSA+AES混合加密，提供文件加密/解密与密钥管理功能          |
 * |                                                                     |
 * |  架构设计:                                                           |
 * |    采用信封加密（Envelope Encryption）模式：                           |
 * |    - 数据加密：每次加密生成随机AES-256密钥，用AES-GCM加密数据           |
 * |    - 密钥保护：用RSA-2048公钥加密AES密钥                              |
 * |    - 解密时：先用RSA私钥解密AES密钥，再用AES密钥解密数据               |
 * |                                                                     |
 * |  自定义二进制文件格式:                                                |
 * |    [魔数4B][版本1B][预留3B][RSA密钥长度4B][RSA加密AES密钥]             |
 * |    [IV 12B][AES加密数据][认证标签16B]                                 |
 * |                                                                     |
 * |  数据流向:                                                           |
 * |    加密:                                                             |
 * |      plaintext → AES-GCM encrypt → ciphertext                       |
 * |      AES key   → RSA-OAEP encrypt  → encrypted AES key              |
 * |      → 写入自定义格式文件                                            |
 * |                                                                     |
 * |    解密:                                                             |
 * |      读取文件头 → 验证魔数+版本                                       |
 * |      → RSA-OAEP decrypt → AES key                                   |
 * |      → AES-GCM decrypt → plaintext                                  |
 * |      → 验证GCM认证标签                                               |
 * |                                                                     |
 * |  依赖关系:                                                           |
 * |    - java.security.*     : RSA密钥对生成与操作                        |
 * |    - javax.crypto.*      : AES-GCM加密/解密                          |
 * |    - android.util.Base64 : 密钥Base64编解码                          |
 * |                                                                     |
 * |  使用示例:                                                           |
 * |    HybridCryptoManager mgr = new HybridCryptoManager();              |
 * |    mgr.generateAndSaveKeyPairToFile(pubFile, priFile);              |
 * |    mgr.encryptFile(inputFile, outputFile);  // 加密文件              |
 * |    mgr.loadKeyPairFromFiles(pubFile, priFile);                      |
 * |    mgr.decryptFile(encryptedFile, decryptedFile); // 解密文件        |
 * |                                                                     |
 * =====================================================================
 */

import android.content.Context; // // Android上下文接口
import android.content.SharedPreferences; // // 轻量级存储（已弃用）
import android.util.Base64; // // Base64编解码工具
import android.util.Log; // // Android日志工具

import java.io.BufferedReader; // // 缓冲读取器，用于PEM文件读取
import java.io.ByteArrayInputStream; // // 字节数组输入流
import java.io.File; // // 文件操作类
import java.io.FileInputStream; // // 文件输入流
import java.io.FileOutputStream; // // 文件输出流
import java.io.FileReader; // // 文件字符读取器
import java.io.FileWriter; // // 文件字符写入器
import java.io.IOException; // // IO异常类
import java.io.InputStream; // // 输入流基类
import java.io.InputStreamReader; // // 输入流读取器
import java.io.OutputStream; // // 输出流基类
import java.nio.ByteBuffer; // // 字节缓冲区（未使用）
import java.security.KeyFactory; // // 密钥工厂，用于重建密钥对象
import java.security.KeyPair; // // RSA密钥对
import java.security.KeyPairGenerator; // // RSA密钥对生成器
import java.security.PrivateKey; // // RSA私钥接口
import java.security.PublicKey; // // RSA公钥接口
import java.security.SecureRandom; // // 安全随机数生成器
import java.security.spec.PKCS8EncodedKeySpec; // // PKCS#8私钥编码规范
import java.security.spec.X509EncodedKeySpec; // // X.509公钥编码规范

import javax.crypto.Cipher; // // 加密/解密器
import javax.crypto.KeyGenerator; // // 对称密钥生成器
import javax.crypto.SecretKey; // // 对称密钥接口
import javax.crypto.spec.GCMParameterSpec; // // GCM模式参数规范
import javax.crypto.spec.SecretKeySpec; // // 对称密钥规格

/**
 * 混合加密管理器。
 * 实现RSA+AES混合加密方案：
 * - RSA 2048位密钥对用于加密AES密钥（密钥保护）
 * - AES 256位 GCM模式用于加密实际数据（高效加密）
 * - 支持PEM格式密钥导入/导出
 * - 支持自定义二进制格式的文件加密/解密
 */
public class HybridCryptoManager {

    /** SharedPreferences文件名（已弃用，密钥现保存到文件） */
    private static final String PREFS_NAME = "hybrid_crypto_prefs"; // // SP文件名

    /** SharedPreferences公钥键名（已弃用） */
    private static final String PREF_PUBLIC_KEY = "public_key"; // // SP公钥键名

    /** SharedPreferences私钥键名（已弃用） */
    private static final String PREF_PRIVATE_KEY = "private_key"; // // SP私钥键名

    // ==================== 加密参数 ====================

    /** RSA密钥长度（位），2048位提供足够的安全性 */
    private static final int RSA_KEY_SIZE = 2048; // // RSA密钥长度

    /** AES密钥长度（位），256位提供最高安全级别 */
    private static final int AES_KEY_SIZE = 256; // // AES密钥长度

    /** GCM认证标签长度（位），128位提供强认证保证 */
    private static final int GCM_TAG_LENGTH = 128; // // GCM认证标签长度

    /** GCM初始化向量长度（字节），12字节是GCM推荐值 */
    private static final int GCM_IV_LENGTH = 12; // // GCM IV长度

    /** 文件读写缓冲区大小（字节） */
    private static final int BUFFER_SIZE = 8192; // // 缓冲区大小

    // ==================== 文件格式 ====================

    /** 文件格式魔数，"MCSG"（Micsig缩写），用于标识加密文件 */
    private static final byte[] MAGIC = new byte[] {0x4d, 0x43, 0x53, 0x47}; // "MCSG" // // 文件魔数

    /** 文件格式版本号，当前为1 */
    private static final int VERSION = 1; // // 格式版本号

//    private final Context context; // // 已弃用：上下文引用
//    private final SharedPreferences prefs; // // 已弃用：SharedPreferences

    /** 内存中的RSA公钥（优先使用内存中的密钥） */
    private PublicKey publicKey = null; // // RSA公钥

    /** 内存中的RSA私钥（优先使用内存中的密钥） */
    private PrivateKey privateKey = null; // // RSA私钥

    /**
     * 默认构造函数。
     * 密钥管理已从SharedPreferences迁移到文件方式，
     * 构造时无需传入Context。
     */
    public HybridCryptoManager() { // // 默认构造函数
//        this.context = context.getApplicationContext(); // // 已弃用
//        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); // // 已弃用
    }

    // ==================== 密钥生成与文件保存 ====================

    /**
     * 生成新的RSA密钥对并保存到指定文件。
     * 生成RSA 2048位密钥对，保存到内存，同时以PEM格式写入文件。
     *
     * @param publicKeyFile  保存公钥的文件（PEM格式）
     * @param privateKeyFile 保存私钥的文件（PEM格式）
     * @throws Exception 密钥生成或文件写入失败时抛出
     */
    public void generateAndSaveKeyPairToFile(File publicKeyFile, File privateKeyFile) throws Exception { // // 生成密钥对并保存到文件
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); // // 获取RSA密钥对生成器
        keyGen.initialize(RSA_KEY_SIZE); // // 初始化为2048位
        KeyPair keyPair = keyGen.generateKeyPair(); // // 生成密钥对

        // 保存到内存
        this.publicKey = keyPair.getPublic(); // // 保存公钥到内存
        this.privateKey = keyPair.getPrivate(); // // 保存私钥到内存

//        // 保存到SharedPreferences（已弃用）
//        String publicKeyBase64 = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.NO_WRAP);
//        String privateKeyBase64 = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.NO_WRAP);
//
//        prefs.edit()
//                .putString(PREF_PUBLIC_KEY, publicKeyBase64)
//                .putString(PREF_PRIVATE_KEY, privateKeyBase64)
//                .apply();

        // 保存到文件
        saveStringToFile(exportPublicKeyPem(), publicKeyFile); // // 公钥以PEM格式写入文件
        saveStringToFile(exportPrivateKeyPem(), privateKeyFile); // // 私钥以PEM格式写入文件
    }

//    /**
//     * 仅生成并保存公钥到文件（用于只加密场景）（已弃用）
//     */
//    public void generateAndSavePublicKeyToFile(File publicKeyFile) throws Exception {
//        generateAndSaveKeyPairToFile(publicKeyFile, new File(context.getCacheDir(), "temp_private.pem"));
//    }

    // ==================== 从文件加载密钥 ====================

    /**
     * 从PEM文件加载公钥。
     * 读取文件内容，解析PEM格式，重建RSA公钥对象。
     *
     * @param publicKeyFile 公钥PEM文件
     * @throws Exception 文件读取或密钥解析失败时抛出
     */
    public void loadPublicKeyFromFile(File publicKeyFile) throws Exception { // // 从文件加载公钥
        String pem = readStringFromFile(publicKeyFile); // // 读取PEM字符串
        importPublicKeyFromPem(pem); // // 导入PEM格式公钥
    }

    /**
     * 从InputStream加载公钥（用于assets/raw资源）。
     *
     * @param inputStream 输入流
     * @throws Exception 流读取或密钥解析失败时抛出
     */
    public void loadPublicKeyFromStream(InputStream inputStream) throws Exception { // // 从流加载公钥
        String pem = readStringFromStream(inputStream); // // 从流读取PEM字符串
        importPublicKeyFromPem(pem); // // 导入PEM格式公钥
    }

    /**
     * 从PEM文件加载私钥。
     *
     * @param privateKeyFile 私钥PEM文件
     * @throws Exception 文件读取或密钥解析失败时抛出
     */
    public void loadPrivateKeyFromFile(File privateKeyFile) throws Exception { // // 从文件加载私钥
        String pem = readStringFromFile(privateKeyFile); // // 读取PEM字符串
        importPrivateKeyFromPem(pem); // // 导入PEM格式私钥
    }

    /**
     * 从InputStream加载私钥。
     *
     * @param inputStream 输入流
     * @throws Exception 流读取或密钥解析失败时抛出
     */
    public void loadPrivateKeyFromStream(InputStream inputStream) throws Exception { // // 从流加载私钥
        String pem = readStringFromStream(inputStream); // // 从流读取PEM字符串
        importPrivateKeyFromPem(pem); // // 导入PEM格式私钥
    }

    /**
     * 同时从文件加载密钥对。
     *
     * @param publicKeyFile  公钥PEM文件
     * @param privateKeyFile 私钥PEM文件
     * @throws Exception 文件读取或密钥解析失败时抛出
     */
    public void loadKeyPairFromFiles(File publicKeyFile, File privateKeyFile) throws Exception { // // 从文件加载密钥对
        loadPublicKeyFromFile(publicKeyFile); // // 加载公钥
        loadPrivateKeyFromFile(privateKeyFile); // // 加载私钥
    }

//    /**
//     * 从assets目录加载密钥对（已弃用）
//     */
//    public void loadKeyPairFromAssets(String publicKeyAsset, String privateKeyAsset) throws Exception {
//        loadPublicKeyFromStream(context.getAssets().open(publicKeyAsset));
//        loadPrivateKeyFromStream(context.getAssets().open(privateKeyAsset));
//    }

    // ==================== 保存密钥到文件 ====================

    /**
     * 将当前公钥保存到文件（PEM格式）。
     *
     * @param publicKeyFile 目标文件
     * @throws Exception 导出或写入失败时抛出
     */
    public void savePublicKeyToFile(File publicKeyFile) throws Exception { // // 保存公钥到文件
        saveStringToFile(exportPublicKeyPem(), publicKeyFile); // // 导出PEM并写入文件
    }

    /**
     * 将当前私钥保存到文件（PEM格式）。
     *
     * @param privateKeyFile 目标文件
     * @throws Exception 导出或写入失败时抛出
     */
    public void savePrivateKeyToFile(File privateKeyFile) throws Exception { // // 保存私钥到文件
        saveStringToFile(exportPrivateKeyPem(), privateKeyFile); // // 导出PEM并写入文件
    }

    /**
     * 将密钥对保存到文件。
     *
     * @param publicKeyFile  公钥目标文件
     * @param privateKeyFile 私钥目标文件
     * @throws Exception 导出或写入失败时抛出
     */
    public void saveKeyPairToFiles(File publicKeyFile, File privateKeyFile) throws Exception { // // 保存密钥对到文件
        savePublicKeyToFile(publicKeyFile); // // 保存公钥
        savePrivateKeyToFile(privateKeyFile); // // 保存私钥
    }

    // ==================== 字符串/内存导入导出 ====================

    /**
     * 从Base64字符串导入公钥。
     * Base64字符串为X.509编码的公钥字节。
     *
     * @param publicKeyBase64 Base64编码的公钥
     * @throws Exception 解码或密钥重建失败时抛出
     */
    public void importPublicKey(String publicKeyBase64) throws Exception { // // 从Base64导入公钥
        byte[] decoded = Base64.decode(publicKeyBase64, Base64.NO_WRAP); // // Base64解码
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded); // // 创建X.509公钥规范
        KeyFactory kf = KeyFactory.getInstance("RSA"); // // 获取RSA密钥工厂
        this.publicKey = kf.generatePublic(spec); // // 重建公钥对象
//        prefs.edit().putString(PREF_PUBLIC_KEY, publicKeyBase64).apply(); // // 已弃用：保存到SP
    }

    /**
     * 从PEM格式字符串导入公钥。
     * 去除PEM头尾标记和空白字符后，调用importPublicKey()。
     *
     * @param pemString PEM格式公钥字符串
     * @throws Exception 解析或导入失败时抛出
     */
    public void importPublicKeyFromPem(String pemString) throws Exception { // // 从PEM导入公钥
        String base64 = pemString // // 提取Base64部分
                .replace("-----BEGIN PUBLIC KEY-----", "") // // 去除PEM头
                .replace("-----END PUBLIC KEY-----", "") // // 去除PEM尾
                .replaceAll("\\s", ""); // // 去除所有空白
        importPublicKey(base64); // // 调用Base64导入
    }

    /**
     * 从Base64字符串导入私钥。
     * Base64字符串为PKCS#8编码的私钥字节。
     *
     * @param privateKeyBase64 Base64编码的私钥
     * @throws Exception 解码或密钥重建失败时抛出
     */
    public void importPrivateKey(String privateKeyBase64) throws Exception { // // 从Base64导入私钥
        byte[] decoded = Base64.decode(privateKeyBase64, Base64.NO_WRAP); // // Base64解码
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded); // // 创建PKCS#8私钥规范
        KeyFactory kf = KeyFactory.getInstance("RSA"); // // 获取RSA密钥工厂
        this.privateKey = kf.generatePrivate(spec); // // 重建私钥对象

//        prefs.edit().putString(PREF_PRIVATE_KEY, privateKeyBase64).apply(); // // 已弃用：保存到SP
    }

    /**
     * 从PEM格式字符串导入私钥。
     * 支持两种PEM头："BEGIN RSA PRIVATE KEY"和"BEGIN PRIVATE KEY"。
     *
     * @param pemString PEM格式私钥字符串
     * @throws Exception 解析或导入失败时抛出
     */
    public void importPrivateKeyFromPem(String pemString) throws Exception { // // 从PEM导入私钥
        String base64 = pemString // // 提取Base64部分
                .replace("-----BEGIN RSA PRIVATE KEY-----", "") // // 去除传统PEM头
                .replace("-----END RSA PRIVATE KEY-----", "") // // 去除传统PEM尾
                .replace("-----BEGIN PRIVATE KEY-----", "") // // 去除PKCS#8 PEM头
                .replace("-----END PRIVATE KEY-----", "") // // 去除PKCS#8 PEM尾
                .replaceAll("\\s", ""); // // 去除所有空白
        importPrivateKey(base64); // // 调用Base64导入
    }

    /**
     * 同时导入密钥对（Base64格式）。
     *
     * @param publicKeyBase64  Base64编码的公钥
     * @param privateKeyBase64 Base64编码的私钥
     * @throws Exception 导入失败时抛出
     */
    public void importKeyPair(String publicKeyBase64, String privateKeyBase64) throws Exception { // // 导入密钥对（Base64）
        importPublicKey(publicKeyBase64); // // 导入公钥
        importPrivateKey(privateKeyBase64); // // 导入私钥
    }

    /**
     * 同时导入密钥对（PEM格式）。
     *
     * @param publicKeyPem  PEM格式公钥
     * @param privateKeyPem PEM格式私钥
     * @throws Exception 导入失败时抛出
     */
    public void importKeyPairFromPem(String publicKeyPem, String privateKeyPem) throws Exception { // // 导入密钥对（PEM）
        importPublicKeyFromPem(publicKeyPem); // // 导入PEM公钥
        importPrivateKeyFromPem(privateKeyPem); // // 导入PEM私钥
    }

    // ==================== 密钥导出为字符串 ====================

    /**
     * 导出公钥为Base64字符串。
     *
     * @return Base64编码的X.509公钥
     * @throws IllegalStateException 公钥未设置时抛出
     */
    public String exportPublicKeyBase64() throws Exception { // // 导出公钥Base64
        PublicKey pk = getPublicKey(); // // 获取公钥
        if (pk == null) throw new IllegalStateException("公钥未设置"); // // 公钥为空则抛异常
        return Base64.encodeToString(pk.getEncoded(), Base64.NO_WRAP); // // 编码为Base64
    }

    /**
     * 导出公钥为PEM格式字符串。
     *
     * @return PEM格式公钥
     * @throws IllegalStateException 公钥未设置时抛出
     */
    public String exportPublicKeyPem() throws Exception { // // 导出公钥PEM
        String base64 = exportPublicKeyBase64(); // // 获取Base64编码
        return wrapPem(base64, "PUBLIC KEY"); // // 包装为PEM格式
    }

    /**
     * 导出私钥为Base64字符串。
     *
     * @return Base64编码的PKCS#8私钥
     * @throws IllegalStateException 私钥未设置时抛出
     */
    public String exportPrivateKeyBase64() throws Exception { // // 导出私钥Base64
        PrivateKey pk = getPrivateKey(); // // 获取私钥
        if (pk == null) throw new IllegalStateException("私钥未设置"); // // 私钥为空则抛异常
        return Base64.encodeToString(pk.getEncoded(), Base64.NO_WRAP); // // 编码为Base64
    }

    /**
     * 导出私钥为PEM格式字符串。
     *
     * @return PEM格式私钥
     * @throws IllegalStateException 私钥未设置时抛出
     */
    public String exportPrivateKeyPem() throws Exception { // // 导出私钥PEM
        String base64 = exportPrivateKeyBase64(); // // 获取Base64编码
        return wrapPem(base64, "RSA PRIVATE KEY"); // // 包装为PEM格式
    }

    /**
     * 将Base64字符串包装为PEM格式。
     * PEM格式：头部标记 + 每64字符换行的Base64 + 尾部标记。
     *
     * @param base64 Base64编码内容
     * @param type   PEM类型标记，如"PUBLIC KEY"、"RSA PRIVATE KEY"
     * @return PEM格式字符串
     */
    private String wrapPem(String base64, String type) { // // 包装为PEM格式
        StringBuilder pem = new StringBuilder(); // // 构建PEM字符串
        pem.append("-----BEGIN ").append(type).append("-----\n"); // // 写入PEM头
        for (int i = 0; i < base64.length(); i += 64) { // // 每64字符换行
            pem.append(base64.substring(i, Math.min(i + 64, base64.length()))); // // 取64字符子串
            pem.append("\n"); // // 换行
        }
        pem.append("-----END ").append(type).append("-----"); // // 写入PEM尾
        return pem.toString(); // // 返回PEM字符串
    }

    // ==================== 文件读写工具 ====================

    /**
     * 将字符串内容写入文件。
     * 自动创建父目录。
     *
     * @param content 要写入的内容
     * @param file    目标文件
     * @throws IOException 写入失败时抛出
     */
    private void saveStringToFile(String content, File file) throws IOException { // // 字符串写入文件
        File parent = file.getParentFile(); // // 获取父目录
        if (parent != null && !parent.exists()) { // // 父目录不存在
            parent.mkdirs(); // // 创建父目录
        }

        FileWriter writer = new FileWriter(file); // // 创建文件写入器
        writer.write(content); // // 写入内容
        writer.close(); // // 关闭写入器
    }

    /**
     * 从文件读取全部内容为字符串。
     *
     * @param file 源文件
     * @return 文件内容字符串
     * @throws IOException 读取失败时抛出
     */
    private String readStringFromFile(File file) throws IOException { // // 从文件读取字符串
        StringBuilder sb = new StringBuilder(); // // 构建字符串
        BufferedReader reader = new BufferedReader(new FileReader(file)); // // 创建缓冲读取器
        String line; // // 行变量
        while ((line = reader.readLine()) != null) { // // 逐行读取
            sb.append(line).append("\n"); // // 拼接行并添加换行
        }
        reader.close(); // // 关闭读取器
        return sb.toString(); // // 返回完整内容
    }

    /**
     * 从输入流读取全部内容为字符串。
     * 读取完毕后关闭流。
     *
     * @param inputStream 输入流
     * @return 流内容字符串
     * @throws IOException 读取失败时抛出
     */
    private String readStringFromStream(InputStream inputStream) throws IOException { // // 从流读取字符串
        StringBuilder sb = new StringBuilder(); // // 构建字符串
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); // // 创建缓冲读取器
        String line; // // 行变量
        while ((line = reader.readLine()) != null) { // // 逐行读取
            sb.append(line).append("\n"); // // 拼接行并添加换行
        }
        reader.close(); // // 关闭读取器
        inputStream.close(); // // 关闭输入流
        return sb.toString(); // // 返回完整内容
    }

    // ==================== 密钥查询 ====================

    /**
     * 检查是否同时拥有公钥和私钥。
     * @return true表示密钥对完整
     */
    public boolean hasKeyPair() { // // 检查密钥对是否完整
        return (publicKey != null && privateKey != null); // // 公钥和私钥都不为空
//        (prefs.contains(PREF_PUBLIC_KEY) && prefs.contains(PREF_PRIVATE_KEY) // // 已弃用
    }

    /**
     * 检查是否拥有公钥。
     * @return true表示公钥已设置
     */
    public boolean hasPublicKey() { // // 检查是否有公钥
        return getPublicKey() != null; // // 公钥不为空
    }

    /**
     * 检查是否拥有私钥。
     * @return true表示私钥已设置
     */
    public boolean hasPrivateKey() { // // 检查是否有私钥
        return getPrivateKey() != null; // // 私钥不为空
    }

    /**
     * 清除内存中的所有密钥。
     * 注意：不会删除已保存到文件的密钥文件。
     */
    public void clearKeys() { // // 清除密钥
        publicKey = null; // // 清空公钥
        privateKey = null; // // 清空私钥
//        prefs.edit() // // 已弃用：清除SP
//                .remove(PREF_PUBLIC_KEY)
//                .remove(PREF_PRIVATE_KEY)
//                .apply();
    }

    // ==================== 内部密钥获取 ====================

    /**
     * 获取内存中的公钥。
     * @return 公钥对象，未设置则返回null
     */
    private PublicKey getPublicKey() { // // 获取公钥
        if (publicKey != null) return publicKey; // // 内存中有则直接返回
        return null; // // 未设置返回null
//        String base64 = prefs.getString(PREF_PUBLIC_KEY, null); // // 已弃用：从SP读取
//        if (base64 == null) return null;
//
//        try {
//            byte[] decoded = Base64.decode(base64, Base64.NO_WRAP);
//            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
//            KeyFactory kf = KeyFactory.getInstance("RSA");
//            publicKey = kf.generatePublic(spec);
//            return publicKey;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
    }

    /**
     * 获取内存中的私钥。
     * @return 私钥对象，未设置则返回null
     */
    private PrivateKey getPrivateKey() { // // 获取私钥
        if (privateKey != null) return privateKey; // // 内存中有则直接返回
        return null; // // 未设置返回null
//        String base64 = prefs.getString(PREF_PRIVATE_KEY, null); // // 已弃用：从SP读取
//        if (base64 == null) return null;
//
//        try {
//            byte[] decoded = Base64.decode(base64, Base64.NO_WRAP);
//            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
//            KeyFactory kf = KeyFactory.getInstance("RSA");
//            privateKey = kf.generatePrivate(spec);
//            return privateKey;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
    }

    // ==================== 文件加密解密 ====================

    /**
     * 加密文件到自定义二进制格式。
     *
     * 加密流程：
     *   1. 生成随机AES-256密钥
     *   2. 用RSA公钥加密AES密钥
     *   3. 生成随机GCM IV
     *   4. 用AES-GCM加密文件数据
     *   5. 按自定义格式写入输出文件
     *
     * 文件格式：
     *   [魔数4字节][版本1字节][预留3字节]
     *   [RSA加密AES密钥长度4字节][RSA加密AES密钥]
     *   [IV 12字节][AES加密数据][认证标签16字节]
     *
     * @param inputFile  明文输入文件
     * @param outputFile 密文输出文件
     * @throws IllegalStateException 公钥未设置时抛出
     * @throws Exception 加密过程出错时抛出
     */
    public void encryptFile(File inputFile, File outputFile) throws Exception { // // 加密文件
        PublicKey pk = getPublicKey(); // // 获取公钥
        if (pk == null) { // // 公钥未设置
            throw new IllegalStateException("公钥未设置"); // // 抛出异常
        }

        KeyGenerator keyGen = KeyGenerator.getInstance("AES"); // // 获取AES密钥生成器
        keyGen.init(AES_KEY_SIZE); // // 初始化为256位
        SecretKey aesKey = keyGen.generateKey(); // // 生成随机AES密钥

        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"); // // RSA-OAEP加密器
        rsaCipher.init(Cipher.ENCRYPT_MODE, pk); // // 初始化为加密模式
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded()); // // 用RSA公钥加密AES密钥

        byte[] iv = new byte[GCM_IV_LENGTH]; // // 创建IV字节数组
        new SecureRandom().nextBytes(iv); // // 生成随机IV

        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding"); // // AES-GCM加密器
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv); // // GCM参数（含认证标签长度和IV）
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec); // // 初始化为加密模式

        try (FileOutputStream fos = new FileOutputStream(outputFile); // // 创建输出流
             FileInputStream fis = new FileInputStream(inputFile)) { // // 创建输入流

            fos.write(MAGIC); // // 写入魔数（4字节）
            fos.write(VERSION); // // 写入版本号（1字节）
            fos.write(new byte[3]); // // 写入预留字段（3字节）

            fos.write(intToBytes(encryptedAesKey.length)); // // 写入RSA加密AES密钥长度（4字节）
            fos.write(encryptedAesKey); // // 写入RSA加密的AES密钥
            fos.write(iv); // // 写入GCM IV（12字节）

            byte[] buffer = new byte[BUFFER_SIZE]; // // 创建读写缓冲区
            int bytesRead; // // 实际读取字节数
            while ((bytesRead = fis.read(buffer)) != -1) { // // 循环读取输入文件
                byte[] encrypted = aesCipher.update(buffer, 0, bytesRead); // // AES-GCM增量加密
                if (encrypted != null) { // // 有加密输出
                    fos.write(encrypted); // // 写入加密数据
                }
            }

            byte[] finalBlock = aesCipher.doFinal(); // // 完成加密（含GCM认证标签）
            fos.write(finalBlock); // // 写入最后的加密块和认证标签
        }
    }

    /**
     * 解密自定义二进制格式的加密文件。
     *
     * 解密流程：
     *   1. 读取并验证文件头（魔数+版本）
     *   2. 读取RSA加密的AES密钥
     *   3. 用RSA私钥解密AES密钥
     *   4. 读取GCM IV
     *   5. 用AES-GCM解密文件数据
     *   6. 验证GCM认证标签（防篡改）
     *
     * @param inputFile  密文输入文件
     * @param outputFile 明文输出文件
     * @throws IllegalStateException 私钥未设置时抛出
     * @throws IllegalArgumentException 文件格式无效或版本不支持时抛出
     * @throws SecurityException GCM认证失败（文件被篡改或密钥错误）时抛出
     * @throws Exception 解密过程出错时抛出
     */
    public void decryptFile(File inputFile, File outputFile) throws Exception { // // 解密文件
        PrivateKey pk = getPrivateKey(); // // 获取私钥
        if (pk == null) { // // 私钥未设置
            throw new IllegalStateException("私钥未设置，无法解密"); // // 抛出异常
        }

        try (FileInputStream fis = new FileInputStream(inputFile); // // 创建输入流
             FileOutputStream fos = new FileOutputStream(outputFile)) { // // 创建输出流

            byte[] magic = new byte[4]; // // 魔数缓冲区
            if (fis.read(magic) != 4 || !java.util.Arrays.equals(magic, MAGIC)) { // // 读取并验证魔数
                throw new IllegalArgumentException("无效的文件格式"); // // 魔数不匹配
            }

            int version = fis.read(); // // 读取版本号
            if (version != VERSION) { // // 版本号不匹配
                throw new IllegalArgumentException("不支持的文件版本: " + version); // // 抛出异常
            }

            fis.skip(3); // // 跳过预留字段（3字节）

            byte[] keyLengthBytes = new byte[4]; // // 密钥长度缓冲区
            fis.read(keyLengthBytes); // // 读取RSA加密AES密钥长度
            int encryptedKeyLength = bytesToInt(keyLengthBytes); // // 转换为int

            byte[] encryptedAesKey = new byte[encryptedKeyLength]; // // 创建AES密钥缓冲区
            fis.read(encryptedAesKey); // // 读取RSA加密的AES密钥

            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"); // // RSA-OAEP解密器
            rsaCipher.init(Cipher.DECRYPT_MODE, pk); // // 初始化为解密模式
            byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey); // // 用RSA私钥解密AES密钥
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES"); // // 重建AES密钥对象

            byte[] iv = new byte[GCM_IV_LENGTH]; // // IV缓冲区
            fis.read(iv); // // 读取GCM IV

            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding"); // // AES-GCM解密器
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv); // // GCM参数
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec); // // 初始化为解密模式

            long fileSize = inputFile.length(); // // 获取文件总大小
            long headerSize = 4 + 1 + 3 + 4 + encryptedKeyLength + GCM_IV_LENGTH; // // 计算文件头大小
            long dataSize = fileSize - headerSize - 16; // // 计算加密数据大小（减去16字节认证标签）

            byte[] buffer = new byte[BUFFER_SIZE]; // // 读写缓冲区
            long remaining = dataSize; // // 剩余待解密字节数

            while (remaining > 0) { // // 循环解密
                int toRead = (int) Math.min(buffer.length, remaining); // // 计算本次读取量
                int bytesRead = fis.read(buffer, 0, toRead); // // 读取数据
                if (bytesRead <= 0) break; // // 读取结束

                byte[] decrypted = aesCipher.update(buffer, 0, bytesRead); // // AES-GCM增量解密
                if (decrypted != null) { // // 有解密输出
                    fos.write(decrypted); // // 写入解密数据
                }
                remaining -= bytesRead; // // 更新剩余量
            }

            byte[] tag = new byte[16]; // // GCM认证标签缓冲区
            fis.read(tag); // // 读取认证标签

            try { // // 尝试验证认证标签
                byte[] finalBlock = aesCipher.doFinal(tag); // // 完成解密并验证标签
                if (finalBlock != null && finalBlock.length > 0) { // // 有最终解密输出
                    fos.write(finalBlock); // // 写入
                }
            } catch (javax.crypto.AEADBadTagException e) { // // GCM认证失败
                throw new SecurityException("认证失败：文件可能被篡改或密钥错误", e); // // 抛出安全异常
            }
        }
    }

    /**
     * 将int转换为大端序字节数组（4字节）。
     *
     * @param value 整数值
     * @return 大端序字节数组
     */
    private byte[] intToBytes(int value) { // // int转大端序字节数组
        return new byte[] { // // 返回4字节大端序
                (byte) (value >>> 24), // // 最高字节
                (byte) (value >>> 16), // // 次高字节
                (byte) (value >>> 8), // // 次低字节
                (byte) value // // 最低字节
        };
    }

    /**
     * 将大端序字节数组转换为int。
     *
     * @param bytes 大端序字节数组（4字节）
     * @return 整数值
     */
    private int bytesToInt(byte[] bytes) { // // 大端序字节数组转int
        return ((bytes[0] & 0xFF) << 24) | // // 最高字节左移24位
                ((bytes[1] & 0xFF) << 16) | // // 次高字节左移16位
                ((bytes[2] & 0xFF) << 8) | // // 次低字节左移8位
                (bytes[3] & 0xFF); // // 最低字节
    }
}
