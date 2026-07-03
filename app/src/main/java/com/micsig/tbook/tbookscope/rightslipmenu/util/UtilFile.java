package com.micsig.tbook.tbookscope.rightslipmenu.util; // 工具子包，提供文件读写与加解密功能

import android.os.Environment;  // Android环境信息，用于获取SD卡路径
import android.util.Base64;     // Base64编解码工具，用于加解密中的编码转换

import java.io.BufferedReader;       // 缓冲读取器，逐行读取文本
import java.io.File;                  // 文件抽象类，用于文件路径判断
import java.io.FileInputStream;       // 文件输入流，读取文件字节
import java.io.FileNotFoundException; // 文件未找到异常
import java.io.IOException;           // 通用IO异常
import java.io.InputStream;           // 输入流基类
import java.io.InputStreamReader;     // 字节流转字符流的桥梁
import java.util.ArrayList;           // 动态数组，存储DataBean列表

import javax.crypto.Cipher;           // 加解密核心类
import javax.crypto.SecretKey;        // 密钥接口
import javax.crypto.SecretKeyFactory; // 密钥工厂，生成密钥
import javax.crypto.spec.DESKeySpec;  // DES密钥规范

/**
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │                         UtilFile 文件工具类                          │
 * ├──────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】右侧滑菜单(rightslipmenu)工具子包(util)中的文件操作工具类  │
 * │ 【核心职责】提供SD卡路径获取、加密数据文件读取、DES加解密三项能力       │
 * │ 【架构设计】全静态方法的无状态工具类，DES加解密为私有方法，对外仅暴露    │
 * │            getSDPath()和readTranFile()两个公共接口                    │
 * │ 【数据流向】SD卡文件(DES加密+Base64) → 解密 → CSV解析 → DataBean列表 │
 * │            → 调用方获取数据                                           │
 * │ 【依赖关系】依赖 DataBean 作为数据载体；依赖 MyConstant 提供DES密钥；  │
 * │            依赖 Android SDK(Environment/Base64)                      │
 * │ 【使用场景】右滑菜单加载历史测量数据时，从SD卡读取加密的传输文件并解析  │
 * └──────────────────────────────────────────────────────────────────────┘
 */
public class UtilFile {
    public static final String TAG = "UtilFile"; // 日志标签，用于Logcat过滤

    /**
     * String fileName = getSDPath() +"/" + name;//以name存在目录中
     *
     * @return
     */
    /**
     * 获取SD卡根路径。
     * <p>先检测SD卡是否已挂载(MEDIA_MOUNTED)，若已挂载则返回SD卡根目录的字符串路径，
     * 否则返回null。调用方可拼接具体文件名来构造完整文件路径。</p>
     *
     * @return SD卡根路径字符串；若SD卡不可用则返回null
     */
    public static String getSDPath() { // 获取SD卡根目录路径
        File sdDir = null; // SD卡目录对象，初始为空
        boolean sdCardExist = Environment.getExternalStorageState() // 获取SD卡当前状态字符串
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在 // 判断状态是否为"已挂载"
        if (sdCardExist) { // SD卡已挂载
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录 // 获取SD卡根目录File对象
        }
        return sdDir != null ? sdDir.toString() : null; // 若目录对象非空则返回路径字符串，否则返回null
    }

    /**
     * 从指定路径读取DES加密的CSV数据文件，解密后解析为DataBean列表。
     * <p>文件每行为一条DES加密+Base64编码的记录，解密后为逗号分隔的CSV格式，
     * 依次解析为DataBean的六个字段(G, b, c, d, e, f)。</p>
     *
     * @param desFile 要读取的加密数据文件的完整路径
     * @return 解析成功的DataBean列表；若文件不存在则返回null；若解析过程中单行出错则跳过该行
     */
    public static ArrayList<DataBean> readTranFile(String desFile) { // 读取加密传输文件并解析为DataBean列表
        ArrayList<DataBean> list = new ArrayList<>(); // 创建结果列表，用于存放解析后的DataBean
        File file = new File(desFile); // 根据路径构造File对象
        if (file.exists() == false) { // 文件不存在
            return null; // 返回null标识文件缺失
        }
        try { // 开始文件读取主流程
            InputStream instream = new FileInputStream(file); // 创建文件字节输入流
            InputStreamReader inputreader = new InputStreamReader(instream); // 将字节流转换为字符流
            BufferedReader buffreader = new BufferedReader(inputreader); // 包装为缓冲读取器，支持逐行读取
            String line = ""; // 当前读取行的内容
            while ((line = buffreader.readLine()) != null) { // 逐行读取，直到文件末尾
                line = decryptPassword(line); // 对当前行进行DES解密，还原为明文CSV
                String[] s = line.split(","); // 按逗号分割CSV字段
                try { // 尝试解析单行数据为DataBean
                    DataBean bean = new DataBean(Float.valueOf(s[0]), Float.valueOf(s[1]), Float.valueOf(s[2]), s[3], Float.valueOf(s[4]), Float.valueOf(s[5])); // 用六个字段构造DataBean实例
                    list.add(bean); // 将解析成功的DataBean加入列表
                } catch (Exception e) { // 单行解析异常（如字段数不足、类型转换失败）
                    e.printStackTrace(); // 打印异常堆栈，跳过该行继续处理
                }
            }
            instream.close();//关闭输入流 // 关闭输入流，释放资源


        } catch (FileNotFoundException e) { // 文件未找到异常
            e.printStackTrace(); // 打印异常堆栈
        } catch (IOException e) { // IO读写异常
            e.printStackTrace(); // 打印异常堆栈
        } catch (Exception e) { // 其他未预料异常
            e.printStackTrace(); // 打印异常堆栈
        }


        return list; // 返回解析后的DataBean列表（可能为空列表）
    }

    //region 字符串的加密与解密

    /**
     * 加密
     **/
    /**
     * 使用DES算法对明文字符串进行加密。
     * <p>加密流程：明文UTF-8字节 → DES加密 → Base64编码 → 密文字符串。
     * 密钥来源于{@link MyConstant#PASSWORD_ENC_SECRET}。</p>
     *
     * @param clearText 待加密的明文字符串
     * @return 加密并Base64编码后的密文字符串；若加密过程异常则返回原始明文
     */
    private static String encryptPassword(String clearText) { // DES加密方法
        try { // 加密流程
            DESKeySpec keySpec = new DESKeySpec( // 创建DES密钥规范
                    MyConstant.PASSWORD_ENC_SECRET.getBytes("UTF-8")); // 将密钥字符串转为UTF-8字节数组
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES"); // 获取DES密钥工厂实例
            SecretKey key = keyFactory.generateSecret(keySpec); // 根据密钥规范生成SecretKey对象

            Cipher cipher = Cipher.getInstance("DES"); // 获取DES加密器实例
            cipher.init(Cipher.ENCRYPT_MODE, key); // 初始化为加密模式，并传入密钥
            String encrypedPwd = Base64.encodeToString(cipher.doFinal(clearText // 对明文UTF-8字节执行DES加密
                    .getBytes("UTF-8")), Base64.DEFAULT); // 将加密结果进行Base64编码为字符串
            return encrypedPwd; // 返回加密后的Base64密文字符串
        } catch (Exception e) { // 加密过程发生异常
        }
        return clearText; // 异常时返回原始明文（降级处理）
    }


    /**
     * DES加密密钥常量持有类。
     * <p>集中管理加密密钥字符串，供DES加解密方法统一引用。</p>
     */
    public class MyConstant { // 密钥常量内部类
        public static final String PASSWORD_ENC_SECRET = "mythmayor"; // DES加密密钥常量
    }

    /**
     * 解密
     **/
    /**
     * 使用DES算法对密文字符串进行解密。
     * <p>解密流程：Base64密文 → Base64解码 → DES解密 → 明文字符串。
     * 密钥与{@link #encryptPassword(String)}使用相同的{@link MyConstant#PASSWORD_ENC_SECRET}。</p>
     *
     * @param encryptedPwd 待解密的Base64编码密文字符串
     * @return 解密后的明文字符串；若解密过程异常则返回原始密文
     */
    private static String decryptPassword(String encryptedPwd) { // DES解密方法
        try { // 解密流程
            DESKeySpec keySpec = new DESKeySpec(MyConstant.PASSWORD_ENC_SECRET.getBytes("UTF-8")); // 用密钥字符串构造DES密钥规范
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES"); // 获取DES密钥工厂实例
            SecretKey key = keyFactory.generateSecret(keySpec); // 根据密钥规范生成SecretKey对象

            byte[] encryptedWithoutB64 = Base64.decode(encryptedPwd, Base64.DEFAULT); // 对Base64密文进行解码，得到原始加密字节
            Cipher cipher = Cipher.getInstance("DES"); // 获取DES加密器实例
            cipher.init(Cipher.DECRYPT_MODE, key); // 初始化为解密模式，并传入密钥
            byte[] plainTextPwdBytes = cipher.doFinal(encryptedWithoutB64); // 对加密字节执行DES解密，得到明文字节
            return new String(plainTextPwdBytes); // 将明文字节转为字符串并返回
        } catch (Exception e) { // 解密过程发生异常
        }
        return encryptedPwd; // 异常时返回原始密文（降级处理）
    }

    //endregion


}
