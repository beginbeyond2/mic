package com.micsig.tbook.tbookscope.util; // // 工具类包，存放应用级工具组件

/*
 * =====================================================================
 * |  __  __  ___  ___    ____              _                          |
 * | |  \/  |/ _ \| _ \  |  _ \  ___   ___ | |_  ___  _ __  ___        |
 * | | |\/| | | | | | | | | |_) |/ _ \ / _ \| __|/ _ \| '__|/ __|      |
 * | | |  | | |_| | |_| | |  _ <|  __/|  __/| |_|  __/| |   \__ \      |
 * | |_|  |_|\___/|___/  |_| \__\\___| \___| \__|\___||_|   |___/      |
 * |                                                                     |
 * |  模块名称: App (Application子类)                                     |
 * |  所属层级: com.micsig.tbook.tbookscope.util                         |
 * |  核心职责: 应用程序入口，负责全局初始化与生命周期管理                      |
 * |                                                                     |
 * |  架构设计:                                                           |
 * |    App作为Android Application子类，是整个MHO示波器应用的启动入口。      |
 * |    在onCreate()中完成所有核心模块的初始化，包括硬件抽象层、              |
 * |    属性管理、屏幕适配、全局变量、产品校验等。                           |
 * |                                                                     |
 * |  数据流向:                                                           |
 * |    App.onCreate()                                                    |
 * |      ├─> HwManager.setString()        // 硬件层初始化标记             |
 * |      ├─> MCrash.init()                // 崩溃捕获初始化               |
 * |      ├─> Scope.getInstance()          // 示波器核心单例初始化          |
 * |      ├─> PropertyManage.init()        // 属性管理器初始化              |
 * |      ├─> Screen.init()                // 屏幕参数初始化               |
 * |      ├─> GlobalVar.init()             // 全局变量初始化               |
 * |      ├─> ScopeConfig.isValidProduct() // 产品合法性校验               |
 * |      ├─> initUsbInfo()                // USB设备信息初始化             |
 * |      ├─> Scope.initScope()            // 示波器硬件初始化             |
 * |      ├─> SaveManage.init()            // 存储管理初始化               |
 * |      └─> CacheUtil.initStateCacheLoad()// 缓存状态加载               |
 * |                                                                     |
 * |  依赖关系:                                                           |
 * |    - Scope          : 示波器核心控制单例                               |
 * |    - PropertyManage : 硬件属性管理（SN/型号/固件版本）                  |
 * |    - Screen         : 屏幕尺寸与适配                                   |
 * |    - GlobalVar      : 全局运行时变量                                    |
 * |    - ScopeConfig    : 产品配置与校验                                    |
 * |    - SaveManage     : 持久化存储管理                                    |
 * |    - CacheUtil      : 内存缓存管理                                     |
 * |    - MCrash         : 崩溃日志捕获                                     |
 * |    - HwManager      : 硬件管理器                                       |
 * |                                                                     |
 * |  使用示例:                                                           |
 * |    App.get()              // 获取Application上下文                    |
 * |    App.IsDebug()          // 判断是否调试模式                          |
 * |    App.setMainActivity()  // 注册MainActivity引用                     |
 * |    App.finish()           // 强制退出应用                              |
 * |    App.RefreshFwVersion() // 刷新固件版本信息                          |
 * |                                                                     |
 * =====================================================================
 */

import android.app.Application; // // Android Application基类
import android.content.Context; // // Android上下文接口
import android.content.Intent; // // Android意图，用于发送广播
import android.os.Build; // // Android构建信息（型号等）
import android.util.Log; // // Android日志工具
import android.widget.Toast; // // Android Toast提示

import com.chillingvan.canvasgl.util.Loggers; // // OpenGL Canvas日志工具
import com.micsig.base.Logger; // // 自定义日志工具
import com.micsig.base.Utils; // // 基础工具类
import com.micsig.smart.Property; // // 硬件属性数据类
import com.micsig.smart.PropertyManage; // // 硬件属性管理器
import com.micsig.tbook.hardware.Hardware; // // 硬件抽象接口
import com.micsig.tbook.hardware.HwManager; // // 硬件管理器
import com.micsig.tbook.scope.Scope; // // 示波器核心控制单例
import com.micsig.tbook.tbookscope.BuildConfig; // // Gradle构建配置（含调试标志与版本号）
import com.micsig.tbook.tbookscope.GlobalVar; // // 全局运行时变量
import com.micsig.tbook.tbookscope.MainActivity; // // 主界面Activity
import com.micsig.tbook.tbookscope.R; // // 资源ID常量
import com.micsig.tbook.tbookscope.config.IConfig; // // 配置接口
import com.micsig.tbook.tbookscope.config.ScopeConfig; // // 示波器配置实现
import com.micsig.tbook.tbookscope.tools.SaveManage; // // 持久化存储管理


/**
 * Created by Administrator on 2017/4/5.
 *
 * MHO示波器应用入口类。
 * 继承Android Application，负责应用启动时的全局初始化、
 * 产品校验、USB信息配置、调试模式管理以及MainActivity生命周期控制。
 */
public class App extends Application {

    /** 日志标签，用于Logcat过滤 */
    private static final String TAG = "App"; // // 日志标签

    /** 调试模式标志，由BuildConfig.LOG_DEBUG决定，控制日志输出级别 */
    private static boolean isDebug = false; // // 调试模式标志，默认关闭

    /** 语言设置的SharedPreferences键名 */
    public static String LANGUAGE = "language"; // // 语言偏好键名

    /** Application单例引用，供全局获取上下文 */
    private static App app; // // Application单例

    /** 初始化完成标志，标记应用核心模块是否初始化完毕 */
    private boolean isInit = false; // // 初始化完成标志

    /**
     * 获取设备序列号（SN）。
     * 通过PropertyManage读取硬件属性中的显示用序列号。
     *
     * @return 设备序列号字符串
     */
    private String getSN(){ // // 获取设备序列号
        PropertyManage propertyManage = PropertyManage.getInstance(); // // 获取属性管理器单例
        propertyManage.update(); // // 刷新属性数据
        Property property = propertyManage.getProperty(); // // 获取属性对象
        return property.getDisplaySN(); // // 返回显示用序列号
    }

    /**
     * 初始化USB设备信息。
     * 从PropertyManage读取产品型号、序列号，组装固件版本号，
     * 并通过Scope.setUsbInfo()写入USB设备描述符，供USB主机识别。
     *
     * 优先级：
     *   产品名：Property.type > Build.MODEL（Android设备型号）
     *   序列号：Property.displaySN > Property.SN > "12345678ABCDEF"（硬编码默认值）
     *
     * @param scope 示波器核心单例，用于设置USB信息
     */
    private void initUsbInfo(Scope scope){ // // 初始化USB设备信息
        PropertyManage propertyManage = PropertyManage.getInstance(); // // 获取属性管理器单例
        propertyManage.update(); // // 刷新属性数据
        Property property = propertyManage.getProperty(); // // 获取属性对象
        String product = ""; // // 产品名称
        String serial = ""; // // 序列号
        product = property.getType(); // // 从属性中读取产品型号
        serial = property.getDisplaySN(); // // 从属性中读取显示用序列号
        if(product == null // // 产品名为空
                || product.isEmpty() ){ // // 或产品名为空字符串
            product = Build.MODEL; // // 回退使用Android设备型号
        }
        if(serial ==  null // // 序列号为空
                || serial.isEmpty()){ // // 或序列号为空字符串
            serial = property.getSN(); // // 回退使用原始序列号
        }
        if(serial ==  null // // 序列号仍为空
                || serial.isEmpty()){ // // 或序列号仍为空字符串
            serial = "12345678ABCDEF"; // // 使用硬编码默认序列号
        }
        String ver = getFwVersion(property); // // 获取固件版本号
        scope.setUsbInfo(product,serial,ver); // // 将产品名、序列号、版本号写入Scope

    }

    /**
     * 根据硬件属性构建固件版本字符串。
     * 格式：[硬件版本].[应用版本号].[FPGA版本号]
     * 若硬件版本为空，则使用"1"作为主版本号。
     *
     * @param property 硬件属性对象
     * @return 固件版本字符串，如 "2.102.5"
     */
    public static String getFwVersion(Property property){ // // 构建固件版本字符串
        String ver = property.getHwVersion(); // // 读取硬件版本号
        if (ver != null && ver.length()>0){ // // 硬件版本号非空
            ver = ver + "." + BuildConfig.VERSION_CODE + "." + Scope.fpgaVer; // // 拼接：硬件版本.应用版本.FPGA版本
        }
        else { // // 硬件版本号为空
            ver = "1." + BuildConfig.VERSION_CODE + "." + Scope.fpgaVer; // // 使用默认主版本1.应用版本.FPGA版本
        }
        return ver; // // 返回版本字符串
    }

    /**
     * 刷新固件版本信息。
     * 重新从PropertyManage读取最新属性，重新计算版本号，
     * 并更新Scope中的USB信息。通常在固件升级后调用。
     */
    public static void RefreshFwVersion(){ // // 刷新固件版本信息
        PropertyManage propertyManage = PropertyManage.getInstance(); // // 获取属性管理器单例
        propertyManage.update(); // // 刷新属性数据
        Property property = propertyManage.getProperty(); // // 获取属性对象
        Scope scope = Scope.getInstance(); // // 获取示波器单例
        scope.setUsbInfo(scope.getProduct(),scope.getSn(),getFwVersion(property)); // // 更新USB信息中的版本号
    }

    /**
     * Application创建回调，应用启动时的入口方法。
     * 执行顺序：
     *   1. 设置硬件标记（app.micsig=1）
     *   2. 隐藏系统下拉框
     *   3. 保存Application引用
     *   4. 初始化调试模式
     *   5. 初始化崩溃捕获
     *   6. 初始化Scope、PropertyManage、Screen、GlobalVar
     *   7. 产品校验
     *   8. 合法则初始化USB信息、示波器、存储、缓存
     *   9. 不合法则提示并退出
     */
    @Override
    public void onCreate() { // // Application创建回调
        super.onCreate(); // // 调用父类onCreate
        HwManager.setString("app.micsig","1"); // // 向硬件层写入应用启动标记
        App.setDropDownBoxVisiable(this,false); // // 隐藏系统下拉框（状态栏下拉）


        app = this; // // 保存Application单例引用
        isDebug = BuildConfig.LOG_DEBUG; // // 从构建配置读取调试标志
        try { // // 尝试初始化崩溃捕获
            MCrash.getInstance().init(this,isDebug); // // 初始化MCrash崩溃捕获器
        } catch (Exception e) { // // 初始化异常
            throw new RuntimeException(e); // // 抛出运行时异常终止应用
        }
        Logger.DEBUG = isDebug; // // 设置自定义Logger调试模式
        Loggers.DEBUG = isDebug; // // 设置OpenGL Canvas日志调试模式
        Scope.getInstance(getApplicationContext()); // // 初始化Scope示波器核心单例

        PropertyManage.getInstance().init(getApplicationContext(),String.format("%016x",Scope.fpgaDna)); // // 初始化属性管理器，传入FPGA DNA作为唯一标识


        Screen.init(this); // // 初始化屏幕参数
        // PrefUtil.setProgress(this); // // 已注释：进度偏好初始化
        GlobalVar.get().init(this); // // 初始化全局变量
        IConfig config = ScopeConfig.getConfig(); // // 获取产品配置
        if(config.isValidProduct()) { // // 产品合法性校验通过
            initUsbInfo(Scope.getInstance()); // // 初始化USB设备信息
            Scope.getInstance().initScope(); // // 初始化示波器硬件
            SaveManage.getInstance().init(); // // 初始化存储管理
            CacheUtil.get().initStateCacheLoad(); // // 初始化缓存状态加载标记
            Utils.InitSignal(); // // 初始化信号量
        }else{ // // 产品校验不通过
            Toast.makeText(this, R.string.app_system_supported,Toast.LENGTH_LONG).show(); // // 显示不支持提示
            finish(); // // 强制退出应用
        }
    }

    /**
     * Application终止回调。
     * 在应用进程结束前将示波器置于待机状态。
     */
    @Override
    public void onTerminate() { // // Application终止回调
        Scope.getInstance().standby(); // // 示波器进入待机模式
        super.onTerminate(); // // 调用父类onTerminate
    }

    /**
     * 强制结束应用。
     * 先关闭MainActivity，再杀死应用进程并退出。
     * 用于产品校验失败等不可恢复场景。
     */
    public static void finish() { // // 强制结束应用
        if(mainActivity != null){ // // MainActivity引用存在
            mainActivity.finish(); // // 关闭MainActivity
            mainActivity = null; // // 清空引用
        }
        android.os.Process.killProcess(android.os.Process.myPid()); // // 杀死当前进程
        System.exit(0); // // 退出JVM
    }

    /**
     * 获取初始化完成标志。
     * @return true表示核心模块已初始化完毕
     */
    public boolean isInit() { // // 获取初始化完成标志
        return isInit; // // 返回初始化标志
    }

    /**
     * 设置初始化完成标志。
     * @param init true表示初始化完毕
     */
    public void setInit(boolean init) { // // 设置初始化完成标志
        isInit = init; // // 更新初始化标志
    }

    /**
     * 获取Application单例。
     * @return App实例，可用于获取ApplicationContext
     */
    public static App get() { // // 获取Application单例
        return app; // // 返回单例引用
    }

    /**
     * 判断当前是否为调试模式。
     * 由BuildConfig.LOG_DEBUG决定，影响日志输出级别。
     * @return true表示调试模式
     */
    public static boolean IsDebug() { // // 判断是否调试模式
        return isDebug; // // 返回调试模式标志
    }

    /** MainActivity存活标志（volatile保证线程可见性） */
    private volatile static boolean bMainActivityAlive = false; // // MainActivity存活标志

    /**
     * 设置MainActivity存活状态。
     * 在MainActivity的onCreate/onDestroy中调用。
     * @param mainActivityAlive true表示MainActivity正在运行
     */
    public static void setMainActivityAlive(boolean mainActivityAlive){ // // 设置MainActivity存活状态
        bMainActivityAlive = mainActivityAlive; // // 更新存活标志
    }

    /**
     * 查询MainActivity是否存活。
     * @return true表示MainActivity正在运行
     */
    public static boolean isMainActivity(){ // // 查询MainActivity是否存活
        return bMainActivityAlive; // // 返回存活标志
    }

    /** MainActivity引用，用于强制关闭主界面 */
    private static MainActivity mainActivity; // // MainActivity引用

    /**
     * 注册MainActivity引用。
     * 在MainActivity.onCreate()中调用。
     * @param activity MainActivity实例
     */
    public static void setMainActivity(MainActivity activity){ // // 注册MainActivity引用
        mainActivity = activity; // // 保存引用
    }

    /** 隐藏/显示导航栏的广播Action */
    public static final String ACTION_HIDE_NAVIGATION = "action.ACTION_HIDE_NAVIGATION"; // // 导航栏控制广播Action

    /**
     * 控制系统下拉框（状态栏下拉）的可见性。
     * 通过发送广播通知系统UI控制组件，并等待500ms确保生效。
     *
     * @param context  上下文
     * @param bVisiable true表示允许下拉，false表示禁止下拉
     */
    public static void setDropDownBoxVisiable(Context context,boolean bVisiable){ // // 控制下拉框可见性
        Intent intent = new Intent(ACTION_HIDE_NAVIGATION); // // 创建导航栏控制广播
        intent.putExtra("state", bVisiable ? "false" : "true");  // 允许下拉框（注意逻辑反转：bVisiable=true时state="false"表示不隐藏）
        context.sendBroadcast(intent); // // 发送广播
        try { // // 等待广播生效
            Thread.sleep(500); // // 等待500ms确保系统UI更新
        } catch (InterruptedException e) { // // 线程被中断
            throw new RuntimeException(e); // // 转换为运行时异常
        }
    }
}
