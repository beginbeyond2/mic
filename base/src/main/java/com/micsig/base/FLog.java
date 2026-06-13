package com.micsig.base; // 定义基础工具类包

import android.content.Context; // 导入Android上下文类
import android.content.Intent; // 导入Android意图类
import android.content.pm.PackageInfo; // 导入包信息类
import android.content.pm.PackageManager; // 导入包管理器类
import android.net.Uri; // 导入URI类
import android.os.Environment; // 导入环境类，用于获取存储路径
import android.util.Log; // 导入Android日志类

import java.io.File; // 导入文件类
import java.io.FileWriter; // 导入文件写入器类
import java.io.IOException; // 导入IO异常类
import java.text.DateFormat; // 导入日期格式化类
import java.text.SimpleDateFormat; // 导入简单日期格式化类
import java.util.Date; // 导入日期类

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                                FLog 日志工具类                               │
 * │                            文件日志记录与调试工具                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   MHO示波器基础工具模块 - 日志记录核心组件                                     │
 * │   提供持久化日志记录功能，支持将运行日志写入文件系统                            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 提供文件日志记录功能，支持追加写入                                        │
 * │   2. 自动记录应用版本信息到日志文件                                            │
 * │   3. 支持U盘/外部存储日志文件刷新                                              │
 * │   4. 提供日志开关控制，生产环境可关闭日志                                       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   采用单例模式设计，全局唯一实例                                               │
 * │   日志文件存储在 /sdcard/Alarms/ 目录下                                       │
 * │   文件命名格式：bugName-yyyy-MM-dd_HH-mm-ss-timestamp.log                    │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   日志内容 → FileWriter → 文件系统 → MediaScanner刷新 → 可被外部设备识别      │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖: android.content.Context (Android上下文)                              │
 * │   依赖: Logger (控制台日志输出)                                               │
 * │   依赖: android.os.Environment (存储路径获取)                                 │
 * │   被依赖: 示波器各功能模块的调试日志记录                                       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   FLog.getInstance().init(context, false);  // 初始化，开启日志              │
 * │   FLog.getInstance().Append("TAG", "日志内容"); // 写入日志                   │
 * │   String path = FLog.getInstance().getFilePathName(); // 获取日志文件路径     │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author Micsig R&D Team
 * @version 1.0
 * @since MHO Series Oscilloscope Software
 */
public class FLog { // 文件日志工具类
    
    // ==================== 常量定义 ====================
    private static final String TAG = "FLog"; // 日志标签，用于控制台输出识别

    // ==================== 单例模式 ====================
    //region 单例
    private static FLog INSTANCE = new FLog(); // 单例实例，类加载时创建

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 获取单例实例                                                            │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   获取FLog的全局唯一实例                                                 │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return FLog单例实例                                                  │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static FLog getInstance() { // 获取单例实例的静态方法
        return INSTANCE; // 返回全局唯一实例
    }
    //endregion

    // ==================== 成员变量 ====================
    private boolean bIsNoLog; // 日志开关标志，true表示关闭日志
    private Context context; // Android上下文对象，用于获取包信息和发送广播
    private String bugName = ""; // 日志文件基础名称
    private String filePath = ""; // 日志文件所在目录路径
    private String fileName = ""; // 日志文件完整名称
    private String pathName = ""; // 日志文件完整路径（目录+文件名）
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 日期格式化器，用于生成时间戳

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 初始化日志系统（使用Context）                                            │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   初始化文件日志系统，创建日志文件并写入版本信息                            │
     * │   日志文件存储在 /sdcard/Alarms/ 目录下                                  │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param context  Android上下文对象                                     │
     * │   @param isNoLog  日志开关，true表示关闭日志功能                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   FLog.getInstance().init(context, false); // 开启日志                  │
     * │   FLog.getInstance().init(context, true);  // 关闭日志                  │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public void init(Context context, boolean isNoLog) { // 初始化方法（带Context参数）
        this.context = context; // 保存上下文引用
        this.bIsNoLog = isNoLog; // 设置日志开关状态
        if (isNoLog) return; // 如果关闭日志，直接返回

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Alarms/"; // 设置日志文件目录为外部存储的Alarms文件夹
        long timestamp = System.currentTimeMillis(); // 获取当前时间戳
        String time = formatter.format(new Date()); // 格式化当前时间为字符串
        bugName = "bugFlog"; // 设置默认日志文件基础名称
        fileName = bugName + "-" + time + "-" + timestamp + ".log"; // 组合生成完整文件名
        pathName = filePath + fileName; // 组合生成完整文件路径

        Append(TAG, getVersionInfo()); // 在日志文件开头写入版本信息
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 初始化日志系统（自定义名称）                                             │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   使用自定义名称初始化文件日志系统                                        │
     * │   适用于需要区分不同模块日志的场景                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param name     日志文件基础名称                                      │
     * │   @param isNoLog  日志开关，true表示关闭日志功能                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   FLog.getInstance().init("measure", false); // 测量模块日志             │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public void init(String name, boolean isNoLog) { // 初始化方法（带自定义名称参数）
        this.bIsNoLog = isNoLog; // 设置日志开关状态
        if (isNoLog) return; // 如果关闭日志，直接返回
        if (bugName.isEmpty()) this.bIsNoLog = true; // 如果名称为空，强制关闭日志

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Alarms/"; // 设置日志文件目录
        long timestamp = System.currentTimeMillis(); // 获取当前时间戳
        String time = formatter.format(new Date()); // 格式化当前时间
        bugName = name; // 设置自定义日志文件基础名称
        fileName = bugName + "-" + time + "-" + timestamp + ".log"; // 组合生成完整文件名
        pathName = filePath + fileName; // 组合生成完整文件路径

        Append(TAG, getVersionInfo()); // 在日志文件开头写入版本信息
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 追加日志内容                                                            │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   向日志文件追加一条日志记录                                             │
     * │   同时输出到控制台和文件系统                                             │
     * │   写入完成后刷新媒体扫描器，使文件可被外部设备识别                         │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param TAG     日志标签，用于分类识别                                  │
     * │   @param content 日志内容                                               │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   FLog.getInstance().Append("Measure", "测量值: 3.14V");                 │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public void Append(String TAG, String content) { // 追加日志内容方法
        if (bIsNoLog) return; // 如果日志已关闭，直接返回
        Logger.i(TAG, content + "< " + bugName + " debug >"); // 输出到控制台，附加调试标识
        FileWriter writer = null; // 声明文件写入器
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(pathName, true); // 创建FileWriter，追加模式
            writer.write(TAG + ":" + content + "\r\n"); // 写入日志内容，格式：TAG:内容+换行
        } catch (IOException e) { // 捕获IO异常
            e.printStackTrace(); // 打印异常堆栈
        } finally { // 最终执行块
            try {
                if (writer != null) { // 检查写入器是否已创建
                    writer.flush(); // 刷新缓冲区
                    writer.close(); // 关闭写入器
                }
            } catch (IOException e) { // 捕获关闭时的IO异常
                e.printStackTrace(); // 打印异常堆栈
            }
        }
        if(uDiskUpdate(pathName).contains("failture")) { // 刷新媒体扫描器，检查是否成功
            Logger.i(TAG, "******Refresh data in file is Failture!!******"); // 刷新失败时输出警告
        }
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 设置上下文对象                                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   设置或更新Android上下文对象                                            │
     * │   用于后续获取包信息和发送广播                                           │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param context Android上下文对象                                      │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public void setContext(Context context) { // 设置上下文方法
        this.context = context; // 更新上下文引用
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 获取版本信息                                                            │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   获取当前应用的版本名称和版本号                                          │
     * │   用于在日志文件开头记录版本信息                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return 版本信息字符串，格式：versionName:xxx versionCode:xxx          │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    private String getVersionInfo() { // 获取版本信息方法
        String title = ""; // 初始化版本信息字符串
        try {
            if (this.context != null) { // 检查上下文是否可用
                PackageManager pm = this.context.getPackageManager(); // 获取包管理器
                PackageInfo pi = pm.getPackageInfo(this.context.getPackageName(), // 获取包信息
                        PackageManager.GET_ACTIVITIES); // 包含Activity信息
                if (pi != null) { // 检查包信息是否获取成功
                    String versionName = pi.versionName == null ? "null" // 获取版本名称，为空则显示"null"
                            : pi.versionName; // 使用实际版本名称
                    String versionCode = pi.versionCode + ""; // 获取版本号并转为字符串
                    title = "versionName:" + versionName + "    versionCode:" + versionCode; // 组合版本信息字符串
                }
            }
        } catch (PackageManager.NameNotFoundException e) { // 捕获包名未找到异常
            Log.e(TAG, "an error occured when collect package info", e); // 输出错误日志
        }
        return title; // 返回版本信息字符串
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 获取日志文件完整路径                                                    │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   获取当前日志文件的完整路径                                             │
     * │   用于外部程序读取或分享日志文件                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return 日志文件完整路径，日志关闭时返回空字符串                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   String path = FLog.getInstance().getFilePathName();                   │
     * │   // 返回: /sdcard/Alarms/bugFlog-2024-01-01_12-00-00-1704096000000.log │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public String getFilePathName() { // 获取日志文件路径方法
        if(bugName.isEmpty() || bIsNoLog) return ""; // 日志名称为空或日志关闭时返回空字符串
        return pathName; // 返回完整文件路径
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 刷新U盘/外部存储文件                                                    │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   通知媒体扫描器刷新指定文件，使其可被外部设备（如U盘）识别                 │
     * │   用于解决Android设备写入文件后外部设备无法立即看到的问题                   │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param path 需要刷新的文件路径                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return 操作结果字符串，包含"success"或"failture"标识                  │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   String result = FLog.getInstance().uDiskUpdate("/sdcard/Alarms/test.log"); │
     * │   // 成功: "Refresh Log file[...]is successfull !!success"              │
     * │   // 失败: "MainActiveContext is null !!failture"                       │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public String uDiskUpdate(String path) { // 刷新媒体文件方法
        if (this.context == null) { // 检查上下文是否可用
            return "MainActiveContext is null !!failture"; // 上下文为空，返回失败信息
        }
        if (bIsNoLog) { // 检查日志是否关闭
            return "Flag bIsNoLog is set true !!failture"; // 日志已关闭，返回失败信息
        }
        if (path.isEmpty()) { // 检查路径是否为空
            return "There is not defined debug log file !!failture"; // 路径为空，返回失败信息
        }
        File f = new File(path); // 创建文件对象
        if (f.exists()) { // 检查文件是否存在
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); // 创建媒体扫描意图
            Uri contentUri = Uri.parse("file://" + path); // 构建文件URI
            mediaScanIntent.setData(contentUri); // 设置意图数据
            this.context.sendBroadcast(mediaScanIntent); // 发送广播通知媒体扫描器
            return "Refresh Log file["+pathName+"]"+"is successfull !!success"; // 返回成功信息
        } else { // 文件不存在
            return "File["+pathName+"]"+" is not exists! Check it !!failture"; // 返回失败信息
        }
    }

}
