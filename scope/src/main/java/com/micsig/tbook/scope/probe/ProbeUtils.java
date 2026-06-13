package com.micsig.tbook.scope.probe;

import android.content.Context;

import com.micsig.base.FileValidateUtil;
import com.micsig.base.HttpUtil;
import com.micsig.base.Utils;
import com.micsig.base.ZipUtils;
import com.micsig.tbook.scope.BuildConfig;
import com.micsig.tbook.scope.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                ProbeUtils - 探头工具类                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Probe模块的探头工具类，位于probe包下，                                        ║
 * ║   提供探头配置管理、固件下载、固件读取等工具方法。                               ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理探头配置文件路径                                                     ║
 * ║   2. 解压和加载探头配置                                                       ║
 * ║   3. 下载探头固件更新                                                         ║
 * ║   4. 读取固件二进制文件                                                       ║
 * ║   5. 查找固件版本信息                                                         ║
 * ║                                                                              ║
 * ║ 【探头配置管理架构】                                                         ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                    探头配置文件管理                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【存储路径】                                                        │ ║
 * ║   │   PROBE_PATH       = /storage/emulated/0/Android/data/.../files     │ ║
 * ║   │   PROBE_CACHE_PATH = /storage/emulated/0/Android/data/.../cache     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【配置文件结构】                                                    │ ║
 * ║   │   PROBE_PATH/                                                        │ ║
 * ║   │   ├── probezip.json          (配置信息文件)                         │ ║
 * ║   │   └── probe/                                                        │ ║
 * ║   │       ├── probe.json         (探头配置JSON)                         │ ║
 * ║   │       ├── app_MSP500_001_01.bin  (MSP500固件)                       │ ║
 * ║   │       ├── app_MDP_001_01.bin     (MDP固件)                          │ ║
 * ║   │       └── ...                                                       │ ║
 * ║   │                                                                      │ ║
 * ║   │   【配置加载流程】                                                    │ ║
 * ║   │   1. 检查是否需要解压（版本比对）                                     │ ║
 * ║   │   2. 从APK资源解压probe.zip                                          │ ║
 * ║   │   3. 读取probe.json配置                                              │ ║
 * ║   │   4. 检查服务器是否有新版本                                           │ ║
 * ║   │   5. 下载并更新固件                                                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【固件下载流程】                                                             ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 检查服务器   │───▶│ 比对版本号   │───▶│ 下载新版本   │                   ║
 * ║   │ probezip.json│    │ (version)   │    │ probe.zip   │                   ║
 * ║   └─────────────┘    └─────────────┘    └──────┬──────┘                   ║
 * ║                                                 │                          ║
 * ║                                                 ▼                          ║
 * ║                                        ┌─────────────┐                     ║
 * ║                                        │ MD5校验     │                     ║
 * ║                                        │ 解压更新     │                     ║
 * ║                                        └─────────────┘                     ║
 * ║                                                                              ║
 * ║ 【固件文件命名规则】                                                         ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │   app_{hw}_{vercode}_{mcuidx}.bin                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   - hw:       硬件版本（如MSP500、MDP）                              │ ║
 * ║   │   - vercode:  版本代码（3位数字，如001、002）                         │ ║
 * ║   │   - mcuidx:   MCU索引（2位数字，如01、02）                            │ ║
 * ║   │                                                                      │ ║
 * ║   │   示例：                                                              │ ║
 * ║   │   app_MSP500_001_01.bin  - MSP500探头，版本1，MCU1                   │ ║
 * ║   │   app_MDP_002_01.bin     - MDP探头，版本2，MCU1                      │ ║
 * ║   │   app_MDP_002_02.bin     - MDP探头，版本2，MCU2                      │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 应用启动时加载探头配置                                                  ║
 * ║   2. 检查并下载探头固件更新                                                  ║
 * ║   3. 探头升级时读取固件文件                                                  ║
 * ║   4. 查找可用的固件版本                                                     ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 文件操作使用synchronized保护                                            ║
 * ║   - 静态方法，无实例状态                                                    ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - ProbeUpgrade: 调用readBin读取固件文件                                   ║
 * ║   - ProbeFactory: 调用loadProbe加载配置                                    ║
 * ║   - HttpUtil: 下载固件文件                                                  ║
 * ║   - ZipUtils: 解压配置文件                                                 ║
 * ║   - FileValidateUtil: MD5校验                                              ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class ProbeUtils {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 路径常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 探头配置文件存储路径
     * 指向应用的外部存储目录
     * 示例：/storage/emulated/0/Android/data/com.micsig.tbook.scope/files
     */
    public static String PROBE_PATH = "";

    /**
     * 探头缓存文件存储路径
     * 指向应用的外部缓存目录
     * 示例：/storage/emulated/0/Android/data/com.micsig.tbook.scope/cache
     */
    public static String PROBE_CACHE_PATH = "";

    /**
     * 探头配置信息文件名
     * 存储探头配置的MD5和版本信息
     */
    public static final String PROBE_INFO = "probezip.json";

    /**
     * 探头配置压缩包文件名
     * 包含探头配置JSON和固件文件
     */
    public static final String PROBE_ZIP = "probe.zip";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 内部类 - 探头信息Bean
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 探头信息Bean类
     * 用于存储探头配置的MD5校验值和版本号
     */
    public static class ProbeInfoBean{
        /**
         * MD5校验值
         * 用于验证文件完整性
         */
        private String md5;

        /**
         * 版本号
         * 用于版本比对，判断是否需要更新
         */
        private int version;

        /**
         * 默认构造方法
         */
        public ProbeInfoBean(){

        }

        /**
         * 获取MD5校验值
         *
         * @return MD5字符串
         */
        public String getMd5() {
            return md5;                                                              // 返回MD5值
        }

        /**
         * 设置MD5校验值
         *
         * @param md5 MD5字符串
         */
        public void setMd5(String md5) {
            this.md5 = md5;                                                          // 设置MD5值
        }

        /**
         * 获取版本号
         *
         * @return 版本号
         */
        public int getVersion() {
            return version;                                                          // 返回版本号
        }

        /**
         * 设置版本号
         *
         * @param version 版本号
         */
        public void setVersion(int version) {
            this.version = version;                                                  // 设置版本号
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 配置解压方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 解压APK资源中的探头配置
     * 从APK的res/raw目录解压probe.zip和probezip.json到指定路径
     * 
     * <p><b>解压流程：</b></p>
     * <ol>
     *   <li>删除旧的probe目录</li>
     *   <li>解压probe.zip到指定路径</li>
     *   <li>复制probezip.json配置信息文件</li>
     * </ol>
     *
     * @param context 应用上下文
     * @param path 目标路径
     */
    public static void unRawProbeZip(Context context,String path){

        File f = new File(path,"probe");                                            // 创建probe目录文件对象
        if(f.exists()){                                                              // 目录已存在
            Utils.delFile(f);                                                        // 删除旧目录
        }
        InputStream is = context.getResources().openRawResource(R.raw.probe);        // 打开probe.zip资源
        ZipUtils.UnZipFolder(is,path);                                               // 解压到指定路径
        is = context.getResources().openRawResource(R.raw.probezip);                 // 打开probezip.json资源
        Utils.saveFile(is,path + File.separator + PROBE_INFO);                       // 保存配置信息文件
    }

    /**
     * 获取APK资源中探头配置的MD5信息
     * 从APK的res/raw目录读取probezip.json并解析
     *
     * @param context 应用上下文
     * @return ProbeInfoBean对象，解析失败返回null
     */
    public static ProbeInfoBean getRawMD5(Context context){
        InputStream is = context.getResources().openRawResource(R.raw.probezip);     // 打开probezip.json资源
        return getProbeMD5(Utils.readAll(is));                                       // 解析并返回MD5信息
    }

    /**
     * 检查是否需要解压APK资源中的探头配置
     * 通过版本比对判断是否需要更新
     * 
     * <p><b>判断条件：</b></p>
     * <ul>
     *   <li>probezip.json文件不存在 → 需要解压</li>
     *   <li>probe.json文件不存在 → 需要解压</li>
     *   <li>APK资源版本号 > 已解压版本号 → 需要解压</li>
     *   <li>DEBUG模式 → 强制解压</li>
     * </ul>
     *
     * @param rawbean APK资源中的探头信息
     * @return true: 需要解压
     *         false: 不需要解压
     */
    public static boolean isUnRawProbeZip(ProbeInfoBean rawbean){
        File f = new File(PROBE_PATH, PROBE_INFO);                                  // 检查probezip.json文件
        if (!f.exists()){                                                            // 文件不存在
            return true;                                                             // 需要解压
        }

        f = new File(PROBE_PATH + File.separator + "probe" + File.separator + "probe.json"); // 检查probe.json文件
        if(!f.exists()){                                                             // 文件不存在
            return true;                                                             // 需要解压
        }

        ProbeInfoBean bean = getProbeMD5(Utils.readAll(PROBE_PATH + File.separator + PROBE_INFO)); // 读取已解压的版本信息
        if(bean == null){                                                            // 解析失败
            return true;                                                             // 需要解压
        }
        if(rawbean != null){                                                         // APK资源信息有效
            if(rawbean.getVersion() > bean.getVersion()){                            // APK版本更高
                return true;                                                         // 需要解压
            }
        }
        if(BuildConfig.DEBUG){                                                      // DEBUG模式
            return true;                                                             // 强制解压
        }
        return false;                                                                // 不需要解压
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 配置加载方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 加载探头配置
     * 初始化路径，检查并解压配置文件，读取探头配置JSON
     * 
     * <p><b>加载流程：</b></p>
     * <ol>
     *   <li>初始化PROBE_PATH和PROBE_CACHE_PATH</li>
     *   <li>检查是否需要解压APK资源</li>
     *   <li>解压probe.zip（如需要）</li>
     *   <li>读取probe.json配置内容</li>
     * </ol>
     *
     * @param context 应用上下文
     * @return probe.json文件内容字符串
     */
    public static String loadProbe(Context context) {
        PROBE_PATH = context.getExternalFilesDir(null).getAbsolutePath();            // 初始化配置存储路径
        PROBE_CACHE_PATH = context.getExternalCacheDir().getAbsolutePath();          // 初始化缓存存储路径

        if(isUnRawProbeZip(getRawMD5(context))){                                    // 检查是否需要解压
            unRawProbeZip(context,PROBE_PATH);                                       // 解压APK资源
        }

        return Utils.readAll(PROBE_PATH + File.separator + "probe" + File.separator + "probe.json"); // 返回配置JSON内容
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 固件下载方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 下载探头固件（指定MD5）
     * 从服务器下载probe.zip，校验MD5后解压更新
     * 
     * <p><b>下载流程：</b></p>
     * <ol>
     *   <li>从服务器下载probe.zip到缓存目录</li>
     *   <li>校验下载文件的MD5值</li>
     *   <li>校验通过后删除旧配置</li>
     *   <li>解压新配置到PROBE_PATH</li>
     *   <li>复制probezip.json配置信息文件</li>
     * </ol>
     *
     * @param md5 预期的MD5校验值
     */
    public static void downloadProbe(final String md5){
        String url = getBaseUrl() + "/download/probe/probe.zip";                     // 构建下载URL
        HttpUtil.get().download(url, PROBE_CACHE_PATH, PROBE_ZIP, new HttpUtil.OnDownloadListener() { // 开始下载
            @Override
            public void onDownloadSuccess(File file) {                              // 下载成功
                if(FileValidateUtil.validateFile(FileValidateUtil.TypeEnum.MD5,md5,file)){ // MD5校验
                    synchronized (ProbeUtils.class) {                                 // 同步保护
                        Utils.delFile(new File(PROBE_PATH + File.separator + PROBE_INFO)); // 删除旧配置信息
                        Utils.delFile(new File(PROBE_PATH + File.separator + "probe")); // 删除旧probe目录
                        ZipUtils.UnZipFolder(PROBE_CACHE_PATH + File.separator + PROBE_ZIP, PROBE_PATH); // 解压新配置
                        Utils.copyFile(PROBE_CACHE_PATH + File.separator + PROBE_INFO, // 复制配置信息文件
                                PROBE_PATH + File.separator + PROBE_INFO);
                    }
                }
            }

            @Override
            public void onDownloading(int progress) {                               // 下载进度
                                                                                  // 暂未处理
            }

            @Override
            public void onDownloadFailed(Exception e) {                             // 下载失败
                                                                                  // 暂未处理
            }
        });
    }

    /**
     * 下载探头固件（自动检查更新）
     * 从服务器下载probezip.json，比对版本后决定是否更新
     * 
     * <p><b>检查流程：</b></p>
     * <ol>
     *   <li>删除缓存中的旧文件</li>
     *   <li>下载服务器上的probezip.json</li>
     *   <li>比对服务器版本和本地版本</li>
     *   <li>服务器版本更高且MD5不同 → 下载新版本</li>
     * </ol>
     */
    public static void downloadProbe(){
        String url = getBaseUrl() + "/download/probe/probezip.json";                 // 构建下载URL

        Utils.delFile(new File(PROBE_CACHE_PATH + File.separator + PROBE_INFO));     // 删除缓存中的配置信息
        Utils.delFile(new File(PROBE_CACHE_PATH + File.separator + PROBE_ZIP));      // 删除缓存中的压缩包

        HttpUtil.get().download(url, PROBE_CACHE_PATH, PROBE_INFO, new HttpUtil.OnDownloadListener() { // 下载配置信息
            @Override
            public void onDownloadSuccess(File file) {                              // 下载成功
                ProbeInfoBean bean1 = getProbeMD5(Utils.readAll(PROBE_CACHE_PATH + File.separator + PROBE_INFO)); // 解析服务器版本
                ProbeInfoBean bean2 = getProbeMD5(Utils.readAll(PROBE_PATH + File.separator + PROBE_INFO)); // 解析本地版本
                if(bean1 != null && bean2 != null){                                  // 两个版本都有效
                    if(bean1.getVersion() > bean2.getVersion()){                    // 服务器版本更高
                        if(!bean1.getMd5().equalsIgnoreCase(bean2.getMd5())){       // MD5不同
                            downloadProbe(bean1.getMd5());                           // 下载新版本
                        }
                    }
                }
            }

            @Override
            public void onDownloading(int progress) {                               // 下载进度
                                                                                  // 暂未处理
            }

            @Override
            public void onDownloadFailed(Exception e) {                             // 下载失败
                                                                                  // 暂未处理
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // JSON解析方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 解析探头配置JSON
     * 从JSON字符串中提取MD5和版本号
     * 
     * <p><b>JSON格式：</b></p>
     * <pre>
     * {
     *   "md5": "a1b2c3d4e5f6...",
     *   "version": 1
     * }
     * </pre>
     *
     * @param str JSON字符串
     * @return ProbeInfoBean对象，解析失败返回null
     */
    private static ProbeInfoBean getProbeMD5(String str){
        try {
            if(str != null && !str.isEmpty()) {                                      // 字符串有效
                ProbeInfoBean bean = new ProbeInfoBean();                            // 创建Bean对象
                JSONObject jsonObject = new JSONObject(str);                         // 解析JSON
                String md5 = jsonObject.getString("md5");                            // 获取MD5值
                int ver = jsonObject.getInt("version");                              // 获取版本号
                bean.setVersion(ver);                                                // 设置版本号
                bean.setMd5(md5.toLowerCase());                                      // 设置MD5（小写）
                return bean;                                                         // 返回Bean对象
            }
        } catch (JSONException e) {
            e.printStackTrace();                                                    // 打印异常堆栈
        }
        return null;                                                                // 返回null
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 语言和URL方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取系统语言代码
     * 格式：语言_国家（如zh_cn、en_us）
     *
     * @return 语言代码字符串
     */
    private static String getLanguage() {
        Locale l = Locale.getDefault();                                              // 获取系统默认语言
        return l.getLanguage().toLowerCase() +"_"+ l.getCountry().toLowerCase();     // 返回格式化的语言代码
    }

    /**
     * 获取服务器基础URL
     * 根据系统语言返回对应的服务器地址
     * 
     * <p><b>URL规则：</b></p>
     * <ul>
     *   <li>中文（zh_cn）: http://www.micsig.com.cn</li>
     *   <li>其他语言: http://www.micsig.com</li>
     * </ul>
     *
     * @return 服务器基础URL
     */
    public static String getBaseUrl(){
        String str = getLanguage();                                                  // 获取系统语言
        if(str.startsWith("zh_cn")){                                                // 中文环境
            return "http://www.micsig.com.cn";                                      // 返回国内服务器
        }
        return "http://www.micsig.com";                                             // 返回国际服务器
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 固件读取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 读取固件二进制文件
     * 根据硬件版本、版本代码和MCU索引读取对应的固件文件
     * 
     * <p><b>文件命名规则：</b></p>
     * <pre>
     * app_{hw}_{vercode}_{mcuidx}.bin
     * </pre>
     * 
     * <p><b>数据对齐：</b></p>
     * <ul>
     *   <li>固件数据按128字节对齐</li>
     *   <li>不足部分填充0xFF</li>
     * </ul>
     * 
     * <p><b>示例：</b></p>
     * <pre>
     * readBin("MSP500", 1, 1) → 读取 app_MSP500_001_01.bin
     * readBin("MDP", 2, 1)    → 读取 app_MDP_002_01.bin
     * </pre>
     *
     * @param hw 硬件版本（如MSP500、MDP）
     * @param vercode 版本代码
     * @param mcuidx MCU索引
     * @return 固件数据字节数组，读取失败返回null
     */
    public static byte [] readBin(String hw,int vercode,int mcuidx){
        synchronized (ProbeUtils.class) {                                            // 同步保护
            final String s = "app_" + hw + "_" + String.format("%03d_%02d.bin", vercode, mcuidx); // 构建文件名
            File f = new File(PROBE_PATH + File.separator + "probe");               // probe目录
            if (f.exists()) {                                                        // 目录存在
                File[] list = f.listFiles(new FilenameFilter() {                     // 查找匹配的文件
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.equalsIgnoreCase(s);                              // 文件名匹配（忽略大小写）
                    }
                });
                if (list != null && list.length > 0) {                               // 找到文件
                    f = list[0];                                                     // 获取文件对象
                    int size = (int) f.length();                                     // 获取文件大小
                    int n = size % 128;                                              // 计算128字节对齐余数
                    if (n > 0) {                                                     // 不对齐
                        size += 128 - n;                                             // 补齐到128字节
                    }
                    byte[] bytes = new byte[size];                                   // 创建字节数组

                    FileInputStream fileInputStream = null;                          // 文件输入流

                    try {
                        fileInputStream = new FileInputStream(f);                    // 打开文件
                        int r = 0;                                                   // 每次读取的字节数
                        n = 0;                                                       // 已读取的总字节数
                        size = (int) f.length();                                     // 实际文件大小
                        while (n < size) {                                           // 循环读取
                            r = fileInputStream.read(bytes, n, size - n);            // 读取数据
                            if (r <= 0) {                                            // 读取结束
                                break;                                               // 跳出循环
                            }
                            n += r;                                                  // 累加已读取字节数
                        }
                    } catch (IOException e) {
                        e.printStackTrace();                                        // 打印异常堆栈
                    } finally {
                        if (fileInputStream != null) {                               // 输入流不为空
                            try {
                                fileInputStream.close();                             // 关闭输入流
                            } catch (IOException e) {
                                e.printStackTrace();                                // 打印异常堆栈
                            }
                        }
                    }
                    if (n == size) {                                                 // 读取成功
                        for (int i = n; i < bytes.length; i++) {                     // 填充对齐字节
                            bytes[i] = (byte) 0xFF;                                  // 填充0xFF
                        }
                        return bytes;                                                // 返回固件数据
                    }
                }
            }
            return null;                                                            // 返回null
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 版本查找方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 查找指定硬件版本的最新固件版本号
     * 扫描probe目录下的固件文件，找出最新版本
     * 
     * <p><b>查找规则：</b></p>
     * <ul>
     *   <li>扫描所有app_*.bin文件</li>
     *   <li>匹配指定的硬件版本</li>
     *   <li>返回最大的版本号</li>
     * </ul>
     * 
     * <p><b>示例：</b></p>
     * <pre>
     * probe目录下有：
     *   app_MSP500_001_01.bin
     *   app_MSP500_002_01.bin
     *   app_MSP500_003_01.bin
     * 
     * findBinVerCode("MSP500", 0) → 返回3
     * </pre>
     *
     * @param hw 硬件版本
     * @param vercode 初始版本号（通常传0）
     * @return 最新版本号，未找到返回初始值
     */
    public static int findBinVerCode(String hw,int vercode){
        synchronized (ProbeUtils.class) {                                            // 同步保护
            File f = new File(PROBE_PATH + File.separator + "probe");               // probe目录
            if (f.exists()) {                                                        // 目录存在
                String[] list = f.list(new FilenameFilter() {                        // 列出所有固件文件
                    @Override
                    public boolean accept(File dir, String name) {
                        String s = name.toLowerCase();                                // 转小写
                        return s.startsWith("app_") && s.endsWith(".bin");           // 匹配固件文件
                    }
                });
                if (list != null && list.length > 0) {                               // 找到文件
                    for (String s : list) {                                          // 遍历文件列表
                        String[] v = s.split("\\.|_");                               // 分割文件名
                        if (v != null && v.length > 0) {                             // 分割成功
                            if (hw.equalsIgnoreCase(v[1])) {                         // 硬件版本匹配
                                int c = 0;                                           // 版本号
                                try {
                                    c = Integer.parseInt(v[2]);                      // 解析版本号
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();                            // 打印异常堆栈
                                }
                                if (c > vercode) {                                   // 版本号更大
                                    vercode = c;                                     // 更新最大版本号
                                }
                            }
                        }
                    }
                }
            }
            return vercode;                                                          // 返回最新版本号
        }
    }
}
