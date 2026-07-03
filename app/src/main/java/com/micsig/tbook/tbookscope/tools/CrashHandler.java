package com.micsig.tbook.tbookscope.tools; // 崩溃处理工具类所在包

import android.content.Context; // 导入上下文类，用于获取应用环境信息
import android.content.pm.PackageInfo; // 导入包信息类，用于获取版本号等
import android.content.pm.PackageManager; // 导入包管理器，用于查询应用包信息
import android.os.Build; // 导入Build类，用于获取设备硬件/系统属性
import android.os.Environment; // 导入环境类，用于获取外部存储路径
import android.util.Log; // 导入日志类，用于输出日志信息

import java.io.File; // 导入文件类，用于创建目录和文件操作
import java.io.FileOutputStream; // 导入文件输出流，用于写入崩溃日志到文件
import java.io.PrintWriter; // 导入打印写入器，用于格式化输出异常堆栈
import java.io.StringWriter; // 导入字符串写入器，用于将堆栈信息转为字符串
import java.io.Writer; // 导入Writer基类，用于抽象字符输出流
import java.lang.reflect.Field; // 导入反射字段类，用于遍历Build类的所有属性
import java.text.DateFormat; // 导入日期格式化基类
import java.text.SimpleDateFormat; // 导入简单日期格式化类，用于生成日志文件名中的时间戳
import java.util.Date; // 导入日期类，用于获取当前时间
import java.util.HashMap; // 导入哈希映射类，用于存储设备信息和异常信息键值对
import java.util.Map; // 导入Map接口，用于声明infos的类型

/**
 * +-----------------------------------------------------------------------------+
 * |                         CrashHandler - 全局崩溃捕获器                        |
 * +-----------------------------------------------------------------------------+
 * |                                                                             |
 * | 【模块定位】                                                                |
 * |   属于 tbookscope.tools 工具模块，是应用级别的全局未捕获异常处理器。           |
 * |                                                                             |
 * | 【核心职责】                                                                |
 * |   1. 拦截应用中所有未捕获的异常（UncaughtException）                          |
 * |   2. 收集崩溃时的设备参数信息（版本号、设备型号、系统版本等）                    |
 * |   3. 将崩溃信息（设备信息 + 异常堆栈）写入本地日志文件                         |
 * |   4. 通过U盘同步机制将崩溃日志上传                                           |
 * |   5. 在无法处理异常时交由系统默认处理器兜底                                    |
 * |                                                                             |
 * | 【架构设计】                                                                |
 * |   采用单例模式（饿汉式），实现 Thread.UncaughtExceptionHandler 接口。          |
 * |   通过 Thread.setDefaultUncaughtExceptionHandler() 替换系统默认处理器，        |
 * |   在异常发生时优先执行自定义处理逻辑，处理失败则回退到系统默认行为。              |
 * |                                                                             |
 * | 【数据流向】                                                                |
 * |   未捕获异常 → uncaughtException() → handleException()                       |
 * |     → collectDeviceInfo() 收集设备信息到 infos Map                           |
 * |     → saveCrashInfo2File() 拼接信息并写入外部存储日志文件                      |
 * |     → Tools.uDiskUpdate() 通过U盘同步崩溃日志                                |
 * |                                                                             |
 * | 【依赖关系】                                                                |
 * |   - android.os.Build：反射获取设备硬件与系统属性                              |
 * |   - android.content.pm.PackageManager / PackageInfo：获取应用版本信息          |
 * |   - android.os.Environment：获取外部存储目录路径                               |
 * |   - Tools.uDiskUpdate()：U盘同步工具方法                                     |
 * |   - Thread.UncaughtExceptionHandler：系统未捕获异常处理接口                    |
 * |                                                                             |
 * | 【使用场景】                                                                |
 * |   在 Application.onCreate() 中调用 CrashHandler.getInstance().init(context)   |
 * |   完成初始化，之后应用中任何线程发生未捕获异常都会被此处理器拦截并记录。          |
 * |                                                                             |
 * +-----------------------------------------------------------------------------+
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler { // 实现系统未捕获异常处理接口
    public static final String TAG = "CrashHandler"; // 日志标签，用于Logcat过滤

    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler; // 保存系统默认异常处理器引用，用于兜底处理
    // CrashHandler实例
    private static CrashHandler INSTANCE = new CrashHandler(); // 饿汉式单例，类加载时即创建唯一实例
    // 程序的Context对象
    private Context mContext; // 应用上下文，用于获取PackageManager等系统服务
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>(); // 键值对存储设备属性与应用版本信息

    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 日期格式化器，生成文件名中的时间部分
    private String nameString; // 日志文件名前缀标识，用于区分不同设备或用户的日志

    //保存crash文件的路径
    private String crashPath; // 崩溃日志文件保存的目录路径

    /** 保证只有一个CrashHandler实例 */
    private CrashHandler() { // 私有构造函数，禁止外部实例化，保证单例
    }

    /**
     * 获取CrashHandler实例，单例模式。
     * <p>
     * 全局唯一入口，通过此方法获取CrashHandler单例对象。
     * </p>
     *
     * @return CrashHandler的唯一实例
     */
    public static CrashHandler getInstance() { // 静态工厂方法，返回单例实例
        return INSTANCE; // 返回饿汉式创建的唯一实例
    }

    /**
     * 初始化崩溃处理器。
     * <p>
     * 保存应用上下文，获取系统默认异常处理器，并将当前CrashHandler
     * 设置为全局未捕获异常处理器，同时初始化日志文件名前缀和保存路径。
     * </p>
     *
     * @param context 应用上下文对象，通常传入Application的Context
     */
    public void init(Context context) { // 初始化方法，在Application.onCreate()中调用
        mContext = context; // 保存应用上下文引用
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler(); // 获取并保存系统默认异常处理器，用于兜底
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this); // 将当前实例注册为全局未捕获异常处理器
        nameString = "liwb"; // 设置日志文件名前缀标识
        crashPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Alarms/"; // 设置崩溃日志保存目录为外部存储的Alarms目录

    }

    /**
     * 当UncaughtException发生时会转入该函数来处理。
     * <p>
     * 这是 Thread.UncaughtExceptionHandler 接口的回调方法。当任何线程发生
     * 未捕获异常时，系统会调用此方法。优先使用自定义逻辑处理异常，若自定义
     * 处理失败则回退到系统默认处理器；若自定义处理成功则延迟3秒后杀死进程退出程序。
     * </p>
     *
     * @param thread 发生未捕获异常的线程
     * @param ex     未捕获的异常对象
     */
    @Override // 标注覆盖接口方法
    public void uncaughtException(Thread thread, Throwable ex) { // 系统回调：未捕获异常处理入口
        if (!handleException(ex) && mDefaultHandler != null) { // 如果自定义处理失败且系统默认处理器存在
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex); // 委托给系统默认处理器处理异常
        } else { // 自定义处理成功或无系统默认处理器
            try { // 尝试延迟退出，给日志写入留出时间
                Thread.sleep(3000); // 休眠3秒，确保日志文件写入完成
            } catch (InterruptedException e) { // 休眠被中断
                Log.e(TAG, "error : ", e); // 记录休眠中断错误日志
            }
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid()); // 杀死当前应用进程
            System.exit(1); // 非正常退出，状态码1表示异常终止
        }
    }

    /**
     * 自定义错误处理，收集错误信息、发送错误报告等操作均在此完成。
     * <p>
     * 核心处理逻辑：先收集设备信息，再将崩溃信息写入日志文件，
     * 最后通过U盘同步机制上传日志文件。
     * </p>
     *
     * @param ex 未捕获的异常对象
     * @return true: 如果处理了该异常信息; 否则返回false
     */
    private boolean handleException(Throwable ex) { // 自定义异常处理核心方法
        if (ex == null) { // 异常对象为空，无法处理
            return false; // 返回false表示未处理
        }
        // 收集设备参数信息
        collectDeviceInfo(mContext); // 收集设备硬件属性和应用版本信息到infos Map
        // 保存日志文件
        String fileName = saveCrashInfo2File(ex); // 将设备信息与异常堆栈写入本地日志文件，返回文件名
        Tools.uDiskUpdate(fileName,mContext); // 通过U盘同步机制上传崩溃日志文件
        return true; // 返回true表示已处理该异常
    }

    /**
     * 收集设备参数信息。
     * <p>
     * 分两步收集：首先通过PackageManager获取应用版本信息（versionName、versionCode），
     * 然后通过反射遍历 Build 类的所有字段获取设备硬件与系统属性（如型号、厂商、SDK版本等），
     * 所有信息以键值对形式存入 infos Map。
     * </p>
     *
     * @param ctx 上下文对象，用于获取PackageManager
     */
    public void collectDeviceInfo(Context ctx) { // 收集设备与应用信息
        try { // 尝试获取应用包信息
            PackageManager pm = ctx.getPackageManager(); // 获取包管理器实例
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), // 获取当前应用的包信息
                    PackageManager.GET_ACTIVITIES); // 请求包含Activity信息的标志位
            if (pi != null) { // 包信息获取成功
                String versionName = pi.versionName == null ? "null" // 版本名为空则用"null"字符串替代
                        : pi.versionName; // 否则使用实际版本名
                String versionCode = pi.versionCode + ""; // 将版本号转为字符串
                infos.put("versionName", versionName); // 存储应用版本名到infos
                infos.put("versionCode", versionCode); // 存储应用版本号到infos
            }
        } catch (PackageManager.NameNotFoundException e) { // 包名未找到异常
            Log.e(TAG, "an error occured when collect package info", e); // 记录获取包信息失败的错误日志
        }
        Field[] fields = Build.class.getDeclaredFields(); // 通过反射获取Build类的所有声明字段
        for (Field field : fields) { // 遍历Build类的每个字段
            try { // 尝试读取字段值
                field.setAccessible(true); // 设置字段可访问（包括private字段）
                infos.put(field.getName(), field.get(null).toString()); // 将字段名和值存入infos，null表示读取静态字段
//                Log.d(TAG, field.getName() + " : " + field.get(null)); // 已注释的调试日志
            } catch (Exception e) { // 反射访问字段异常
                Log.e(TAG, "an error occured when collect crash info", e); // 记录收集设备信息失败的错误日志
            }
        }
    }

    /**
     * 保存错误信息到文件中。
     * <p>
     * 将 infos Map 中的设备信息与异常堆栈信息拼接后写入外部存储的日志文件。
     * 文件名格式为：{nameString}-{日期时间}-{时间戳}.log。
     * 日志文件保存路径由 crashPath 指定（默认为外部存储/Alarms/目录）。
     * </p>
     *
     * @param ex 未捕获的异常对象，用于提取堆栈信息
     * @return 返回日志文件完整路径名称，便于将文件传送到服务器；写入失败返回null
     */
    private String saveCrashInfo2File(Throwable ex) { // 将崩溃信息保存到本地日志文件

        StringBuffer sb = new StringBuffer(); // 创建字符串缓冲区，用于拼接日志内容
        for (Map.Entry<String, String> entry : infos.entrySet()) { // 遍历infos中的所有键值对
            String key = entry.getKey(); // 获取信息项的键名
            String value = entry.getValue(); // 获取信息项的值
            sb.append(key + "=" + value + "\n"); // 以"key=value\n"格式追加到缓冲区
        }

        Writer writer = new StringWriter(); // 创建字符串写入器，用于接收堆栈信息
        PrintWriter printWriter = new PrintWriter(writer); // 创建打印写入器，包装字符串写入器
        ex.printStackTrace(printWriter); // 将主异常的堆栈信息输出到printWriter
        Throwable cause = ex.getCause(); // 获取异常的根本原因（Cause）
        while (cause != null) { // 循环遍历异常链中的所有Cause
            cause.printStackTrace(printWriter); // 将当前Cause的堆栈信息输出到printWriter
            cause = cause.getCause(); // 获取下一层Cause
        }
        printWriter.close(); // 关闭打印写入器，刷新缓冲区
        String result = writer.toString(); // 将写入器中的堆栈信息转为字符串
        sb.append(result); // 将异常堆栈信息追加到设备信息之后
        try { // 尝试将日志写入文件
            long timestamp = System.currentTimeMillis(); // 获取当前时间戳（毫秒）
            String time = formatter.format(new Date()); // 将当前日期格式化为文件名中的时间部分
            String fileName = nameString + "-" + time + "-" + timestamp // 拼接日志文件名：前缀-时间-时间戳
                    + ".log"; // 文件扩展名为.log
            if (Environment.getExternalStorageState().equals( // 检查外部存储是否可用（已挂载）
                    Environment.MEDIA_MOUNTED)) { // 比较外部存储状态为已挂载
                String path =crashPath;  /*WMapConstants.CrashLogDir;*/ // 使用crashPath作为日志保存目录
                File dir = new File(path); // 创建目录文件对象
                if (!dir.exists()) { // 如果目录不存在
                    dir.mkdirs(); // 创建目录（含父目录）
                }
                fileName=path+fileName; // 拼接完整文件路径：目录路径+文件名
                FileOutputStream fos = new FileOutputStream(fileName); // 创建文件输出流，打开文件用于写入
                fos.write(sb.toString().getBytes()); // 将日志内容转为字节数组并写入文件
                fos.close(); // 关闭文件输出流，释放资源
            }
            return fileName; // 返回日志文件的完整路径名
        } catch (Exception e) { // 文件写入异常
            Log.e(TAG, "an error occured while writing file...", e); // 记录文件写入失败的错误日志
        }
        return null; // 写入失败返回null
    }
}
