package com.micsig.tbook.tbookscope.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HybridCryptoManager {

    private static final String PREFS_NAME = "hybrid_crypto_prefs";
    private static final String PREF_PUBLIC_KEY = "public_key";
    private static final String PREF_PRIVATE_KEY = "private_key";

    // 加密参数
    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int BUFFER_SIZE = 8192;

    // 文件格式魔数
    private static final byte[] MAGIC = new byte[] {0x4d, 0x43, 0x53, 0x47}; // "MCSG"
    private static final int VERSION = 1;

//    private final Context context;
//    private final SharedPreferences prefs;

    // 内存中的密钥（优先使用）
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;

    public HybridCryptoManager() {
//        this.context = context.getApplicationContext();
//        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ==================== 密钥生成与文件保存 ====================

    /**
     * 生成新的RSA密钥对并保存到指定文件
     * @param publicKeyFile 保存公钥的文件（PEM格式）
     * @param privateKeyFile 保存私钥的文件（PEM格式）
     */
    public void generateAndSaveKeyPairToFile(File publicKeyFile, File privateKeyFile) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();

        // 保存到内存
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();

//        // 保存到SharedPreferences
//        String publicKeyBase64 = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.NO_WRAP);
//        String privateKeyBase64 = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.NO_WRAP);
//
//        prefs.edit()
//                .putString(PREF_PUBLIC_KEY, publicKeyBase64)
//                .putString(PREF_PRIVATE_KEY, privateKeyBase64)
//                .apply();

        // 保存到文件
        saveStringToFile(exportPublicKeyPem(), publicKeyFile);
        saveStringToFile(exportPrivateKeyPem(), privateKeyFile);
    }

//    /**
//     * 仅生成并保存公钥到文件（用于只加密场景）
//     */
//    public void generateAndSavePublicKeyToFile(File publicKeyFile) throws Exception {
//        generateAndSaveKeyPairToFile(publicKeyFile, new File(context.getCacheDir(), "temp_private.pem"));
//    }

    // ==================== 从文件加载密钥 ====================

    /**
     * 从PEM文件加载公钥
     */
    public void loadPublicKeyFromFile(File publicKeyFile) throws Exception {
        String pem = readStringFromFile(publicKeyFile);
        importPublicKeyFromPem(pem);
    }

    /**
     * 从InputStream加载公钥（用于assets/raw）
     */
    public void loadPublicKeyFromStream(InputStream inputStream) throws Exception {
        String pem = readStringFromStream(inputStream);
        importPublicKeyFromPem(pem);
    }

    /**
     * 从PEM文件加载私钥
     */
    public void loadPrivateKeyFromFile(File privateKeyFile) throws Exception {
        String pem = readStringFromFile(privateKeyFile);
        importPrivateKeyFromPem(pem);
    }

    /**
     * 从InputStream加载私钥
     */
    public void loadPrivateKeyFromStream(InputStream inputStream) throws Exception {
        String pem = readStringFromStream(inputStream);
        importPrivateKeyFromPem(pem);
    }

    /**
     * 同时从文件加载密钥对
     */
    public void loadKeyPairFromFiles(File publicKeyFile, File privateKeyFile) throws Exception {
        loadPublicKeyFromFile(publicKeyFile);
        loadPrivateKeyFromFile(privateKeyFile);
    }

//    /**
//     * 从assets目录加载密钥对
//     */
//    public void loadKeyPairFromAssets(String publicKeyAsset, String privateKeyAsset) throws Exception {
//        loadPublicKeyFromStream(context.getAssets().open(publicKeyAsset));
//        loadPrivateKeyFromStream(context.getAssets().open(privateKeyAsset));
//    }

    // ==================== 保存密钥到文件 ====================

    /**
     * 将当前公钥保存到文件（PEM格式）
     */
    public void savePublicKeyToFile(File publicKeyFile) throws Exception {
        saveStringToFile(exportPublicKeyPem(), publicKeyFile);
    }

    /**
     * 将当前私钥保存到文件（PEM格式）
     */
    public void savePrivateKeyToFile(File privateKeyFile) throws Exception {
        saveStringToFile(exportPrivateKeyPem(), privateKeyFile);
    }

    /**
     * 将密钥对保存到文件
     */
    public void saveKeyPairToFiles(File publicKeyFile, File privateKeyFile) throws Exception {
        savePublicKeyToFile(publicKeyFile);
        savePrivateKeyToFile(privateKeyFile);
    }

    // ==================== 字符串/内存导入导出 ====================

    /**
     * 从Base64字符串导入公钥
     */
    public void importPublicKey(String publicKeyBase64) throws Exception {
        byte[] decoded = Base64.decode(publicKeyBase64, Base64.NO_WRAP);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.publicKey = kf.generatePublic(spec);
//        prefs.edit().putString(PREF_PUBLIC_KEY, publicKeyBase64).apply();
    }

    /**
     * 从PEM格式字符串导入公钥
     */
    public void importPublicKeyFromPem(String pemString) throws Exception {
        String base64 = pemString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        importPublicKey(base64);
    }

    /**
     * 从Base64字符串导入私钥
     */
    public void importPrivateKey(String privateKeyBase64) throws Exception {
        byte[] decoded = Base64.decode(privateKeyBase64, Base64.NO_WRAP);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.privateKey = kf.generatePrivate(spec);

//        prefs.edit().putString(PREF_PRIVATE_KEY, privateKeyBase64).apply();
    }

    /**
     * 从PEM格式字符串导入私钥
     */
    public void importPrivateKeyFromPem(String pemString) throws Exception {
        String base64 = pemString
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        importPrivateKey(base64);
    }

    /**
     * 同时导入密钥对
     */
    public void importKeyPair(String publicKeyBase64, String privateKeyBase64) throws Exception {
        importPublicKey(publicKeyBase64);
        importPrivateKey(privateKeyBase64);
    }

    /**
     * 同时导入密钥对（PEM格式）
     */
    public void importKeyPairFromPem(String publicKeyPem, String privateKeyPem) throws Exception {
        importPublicKeyFromPem(publicKeyPem);
        importPrivateKeyFromPem(privateKeyPem);
    }

    // ==================== 密钥导出为字符串 ====================

    public String exportPublicKeyBase64() throws Exception {
        PublicKey pk = getPublicKey();
        if (pk == null) throw new IllegalStateException("公钥未设置");
        return Base64.encodeToString(pk.getEncoded(), Base64.NO_WRAP);
    }

    public String exportPublicKeyPem() throws Exception {
        String base64 = exportPublicKeyBase64();
        return wrapPem(base64, "PUBLIC KEY");
    }

    public String exportPrivateKeyBase64() throws Exception {
        PrivateKey pk = getPrivateKey();
        if (pk == null) throw new IllegalStateException("私钥未设置");
        return Base64.encodeToString(pk.getEncoded(), Base64.NO_WRAP);
    }

    public String exportPrivateKeyPem() throws Exception {
        String base64 = exportPrivateKeyBase64();
        return wrapPem(base64, "RSA PRIVATE KEY");
    }

    private String wrapPem(String base64, String type) {
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN ").append(type).append("-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64.substring(i, Math.min(i + 64, base64.length())));
            pem.append("\n");
        }
        pem.append("-----END ").append(type).append("-----");
        return pem.toString();
    }

    // ==================== 文件读写工具 ====================

    private void saveStringToFile(String content, File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
    }

    private String readStringFromFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private String readStringFromStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        inputStream.close();
        return sb.toString();
    }

    // ==================== 密钥查询 ====================

    public boolean hasKeyPair() {
        return (publicKey != null && privateKey != null);
//        (prefs.contains(PREF_PUBLIC_KEY) && prefs.contains(PREF_PRIVATE_KEY)
    }

    public boolean hasPublicKey() {
        return getPublicKey() != null;
    }

    public boolean hasPrivateKey() {
        return getPrivateKey() != null;
    }

    public void clearKeys() {
        publicKey = null;
        privateKey = null;
//        prefs.edit()
//                .remove(PREF_PUBLIC_KEY)
//                .remove(PREF_PRIVATE_KEY)
//                .apply();
    }

    // ==================== 内部密钥获取 ====================

    private PublicKey getPublicKey() {
        if (publicKey != null) return publicKey;
        return null;
//        String base64 = prefs.getString(PREF_PUBLIC_KEY, null);
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

    private PrivateKey getPrivateKey() {
        if (privateKey != null) return privateKey;
        return null;
//        String base64 = prefs.getString(PREF_PRIVATE_KEY, null);
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
     * 加密文件到二进制格式
     * 格式: [魔数4字节][版本1字节][预留3字节][RSA加密AES密钥长度4字节][RSA加密AES密钥][IV12字节][AES加密数据][认证标签16字节]
     */
    public void encryptFile(File inputFile, File outputFile) throws Exception {
        PublicKey pk = getPublicKey();
        if (pk == null) {
            throw new IllegalStateException("公钥未设置");
        }

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        SecretKey aesKey = keyGen.generateKey();

        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, pk);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

        try (FileOutputStream fos = new FileOutputStream(outputFile);
             FileInputStream fis = new FileInputStream(inputFile)) {

            fos.write(MAGIC);
            fos.write(VERSION);
            fos.write(new byte[3]);

            fos.write(intToBytes(encryptedAesKey.length));
            fos.write(encryptedAesKey);
            fos.write(iv);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] encrypted = aesCipher.update(buffer, 0, bytesRead);
                if (encrypted != null) {
                    fos.write(encrypted);
                }
            }

            byte[] finalBlock = aesCipher.doFinal();
            fos.write(finalBlock);
        }
    }

    public void decryptFile(File inputFile, File outputFile) throws Exception {
        PrivateKey pk = getPrivateKey();
        if (pk == null) {
            throw new IllegalStateException("私钥未设置，无法解密");
        }

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] magic = new byte[4];
            if (fis.read(magic) != 4 || !java.util.Arrays.equals(magic, MAGIC)) {
                throw new IllegalArgumentException("无效的文件格式");
            }

            int version = fis.read();
            if (version != VERSION) {
                throw new IllegalArgumentException("不支持的文件版本: " + version);
            }

            fis.skip(3);

            byte[] keyLengthBytes = new byte[4];
            fis.read(keyLengthBytes);
            int encryptedKeyLength = bytesToInt(keyLengthBytes);

            byte[] encryptedAesKey = new byte[encryptedKeyLength];
            fis.read(encryptedAesKey);

            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, pk);
            byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

            byte[] iv = new byte[GCM_IV_LENGTH];
            fis.read(iv);

            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

            long fileSize = inputFile.length();
            long headerSize = 4 + 1 + 3 + 4 + encryptedKeyLength + GCM_IV_LENGTH;
            long dataSize = fileSize - headerSize - 16;

            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = dataSize;

            while (remaining > 0) {
                int toRead = (int) Math.min(buffer.length, remaining);
                int bytesRead = fis.read(buffer, 0, toRead);
                if (bytesRead <= 0) break;

                byte[] decrypted = aesCipher.update(buffer, 0, bytesRead);
                if (decrypted != null) {
                    fos.write(decrypted);
                }
                remaining -= bytesRead;
            }

            byte[] tag = new byte[16];
            fis.read(tag);

            try {
                byte[] finalBlock = aesCipher.doFinal(tag);
                if (finalBlock != null && finalBlock.length > 0) {
                    fos.write(finalBlock);
                }
            } catch (javax.crypto.AEADBadTagException e) {
                throw new SecurityException("认证失败：文件可能被篡改或密钥错误", e);
            }
        }
    }

    private byte[] intToBytes(int value) {
        return new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

    private int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }
}