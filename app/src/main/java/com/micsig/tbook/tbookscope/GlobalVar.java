package com.micsig.tbook.tbookscope; // 定义包路径，属于示波器核心模块

import static com.micsig.tbook.tbookscope.wavezone.IWorkMode.WorkMode_XY; // 导入XY工作模式常量
import static com.micsig.tbook.tbookscope.wavezone.IWorkMode.WorkMode_YT; // 导入YT工作模式常量
import static com.micsig.tbook.tbookscope.wavezone.IWorkMode.WorkMode_YTZOOM; // 导入YT缩放工作模式常量

import android.content.Context; // 导入Android上下文类，用于获取资源和系统服务
import android.content.res.Resources; // 导入资源类，用于访问应用资源
import android.graphics.Point; // 导入点类，用于表示二维坐标点
import android.graphics.Rect; // 导入矩形类，用于表示屏幕区域

import com.micsig.tbook.scope.Scope; // 导入示波器核心类，提供硬件信息
import com.micsig.tbook.tbookscope.config.ScopeConfig; // 导入示波器配置类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.ui.top.view.scale.TopUtilScale; // 导入顶部工具栏缩放工具类

import java.util.HashMap; // 导入哈希映射类，用于存储测量行高度映射

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                           GlobalVar - 全局变量管理类                          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 * 
 * 【架构图】
 * 
 *      ┌──────────────────────────────────────────────────────────────────┐
 *      │                        Application Layer                         │
 *      │                    (应用层 - UI/业务逻辑)                          │
 *      └───────────────────────────┬──────────────────────────────────────┘
 *                                  │
 *                                  │ 获取全局配置参数
 *                                  ▼
 *      ┌──────────────────────────────────────────────────────────────────┐
 *      │                         GlobalVar                                │
 *      │                    (全局变量管理类)                                │
 *      │  ┌─────────────────────────────────────────────────────────────┐ │
 *      │  │  单例模式 (静态内部类)                                        │ │
 *      │  │  - GlobalVarHolder.instance                                 │ │
 *      │  └─────────────────────────────────────────────────────────────┘ │
 *      │  ┌─────────────────────────────────────────────────────────────┐ │
 *      │  │  三种工作模式参数管理                                         │ │
 *      │  │  - YT模式 (常规时域模式)                                     │ │
 *      │  │  - YTZOOM模式 (缩放模式)                                     │ │
 *      │  │  - XY模式 (李萨如图模式)                                     │ │
 *      │  └─────────────────────────────────────────────────────────────┘ │
 *      │  ┌─────────────────────────────────────────────────────────────┐ │
 *      │  │  屏幕尺寸参数管理                                            │ │
 *      │  │  - screen: 屏幕完整区域                                      │ │
 *      │  │  - mainWave: 主波形区域                                      │ │
 *      │  │  - mainTop: 顶部区域                                         │ │
 *      │  │  - bottomSlip: 底部滑动区域                                   │ │
 *      │  │  - rectZoom: 缩放区域                                        │ │
 *      │  └─────────────────────────────────────────────────────────────┘ │
 *      └───────────────────────────┬──────────────────────────────────────┘
 *                                  │
 *                                  │ 读取配置/硬件信息
 *                                  ▼
 *      ┌──────────────────────────────────────────────────────────────────┐
 *      │                      Hardware/Config Layer                       │
 *      │                  (硬件/配置层 - Scope/ScopeConfig)                │
 *      └──────────────────────────────────────────────────────────────────┘
 * 
 * 【模块定位】
 * - 所属模块: tbookscope (示波器核心模块)
 * - 模块类型: 基础设施层 (Infrastructure Layer)
 * - 职责范围: 全局配置参数的统一管理和分发
 * - 设计模式: 单例模式 (静态内部类实现)
 * 
 * 【核心职责】
 * 1. 单例管理: 通过静态内部类实现线程安全的单例模式
 * 2. 屏幕尺寸管理: 管理示波器屏幕各区域的尺寸参数
 * 3. 工作模式适配: 根据不同工作模式(YT/YTZOOM/XY)提供相应的参数
 * 4. 通道配置管理: 管理2/4/8通道的配置信息
 * 5. 测量参数管理: 管理测量项的位置、数量等参数
 * 
 * 【三种工作模式说明】
 * ┌──────────────┬──────────────────────────────────────────────────────────┐
 * │   工作模式    │                         说明                              │
 * ├──────────────┼──────────────────────────────────────────────────────────┤
 * │ WorkMode_YT  │ YT模式 - 常规时域模式，显示电压随时间变化的波形              │
 * │              │ - 波形区域: mainWave.x × mainWave.y                       │
 * │              │ - 用于常规信号测量和分析                                    │
 * │              │ - 支持多通道同时显示                                        │
 * ├──────────────┼──────────────────────────────────────────────────────────┤
 * │ WorkMode_YTZOOM│ YTZOOM模式 - YT缩放模式，在YT基础上增加缩放窗口            │
 * │              │ - 波形区域: mainWave.x × (mainWave.y * ytToZoomCoef)       │
 * │              │ - 缩放窗口高度: zoomH (主波形的1/4)                         │
 * │              │ - 用于查看波形的细节部分                                    │
 * │              │ - 缩放系数: ytToZoomCoef = (mainWave.y - zoomH) / mainWave.y│
 * ├──────────────┼──────────────────────────────────────────────────────────┤
 * │ WorkMode_XY  │ XY模式 - 李萨如图模式，显示两个通道信号的相位关系           │
 * │              │ - 固定波形区域: 800 × 800                                  │
 * │              │ - 用于测量频率比和相位差                                   │
 * │              │ - 不支持常规测量功能                                        │
 * └──────────────┴──────────────────────────────────────────────────────────┘
 * 
 * 【屏幕尺寸参数说明】
 * ┌─────────────────┬──────────────────────────────────────────────────────┐
 * │      参数        │                      说明                              │
 * ├─────────────────┼──────────────────────────────────────────────────────┤
 * │ screen          │ 屏幕完整区域，包含所有UI元素                             │
 * │                 │ - 来源: R.dimen.screenWidth/screenHeight                │
 * ├─────────────────┼──────────────────────────────────────────────────────┤
 * │ mainWave        │ 主波形显示区域                                           │
 * │                 │ - 宽度: R.dimen.mainCenterWidth                         │
 * │                 │ - 高度: R.dimen.mainCenterHeightMeasureRow0             │
 * │                 │ - 用于YT/YTZOOM模式的波形显示                            │
 * ├─────────────────┼──────────────────────────────────────────────────────┤
 * │ mainTop         │ 顶部工具栏区域                                           │
 * │                 │ - 宽度: R.dimen.mainTopWidth                             │
 * │                 │ - 高度: R.dimen.mainTopHeight                            │
 * ├─────────────────┼──────────────────────────────────────────────────────┤
 * │ bottomSlip      │ 底部快捷操作区域                                          │
 * │                 │ - 宽度: R.dimen.mainBottomQuickWidth                     │
 * │                 │ - 高度: R.dimen.mainBottomQuickHeight                    │
 * ├─────────────────┼──────────────────────────────────────────────────────┤
 * │ rectZoom        │ 缩放窗口区域 (YTZOOM模式使用)                             │
 * │                 │ - 宽度: mainWave.x                                        │
 * │                 │ - 高度: zoomH (默认为mainWave.y的1/4)                     │
 * ├─────────────────┼──────────────────────────────────────────────────────┤
 * │ ytToZoomCoef    │ YT模式到YTZOOM模式的缩放系数                              │
 * │                 │ - 计算公式: (mainWave.y - zoomH) / mainWave.y            │
 * │                 │ - 用于将YT模式的位置参数转换为YTZOOM模式                   │
 * ├─────────────────┼──────────────────────────────────────────────────────┤
 * │ zoomToYtCoef    │ YTZOOM模式到YT模式的缩放系数                              │
 * │                 │ - 计算公式: mainWave.y / (mainWave.y - zoomH)            │
 * │                 │ - 用于将YTZOOM模式的位置参数转换为YT模式                   │
 * ├─────────────────┼──────────────────────────────────────────────────────┤
 * │ measureRowToHeightMap│ 测量行高度映射表                                     │
 * │                 │ - Key: 测量行索引 (0-4)                                   │
 * │                 │ - Value: 对应行的高度像素值                               │
 * │                 │ - 用于不同测量行数的界面适配                              │
 * └─────────────────┴──────────────────────────────────────────────────────┘
 * 
 * 【依赖关系】
 * - 依赖类:
 *   - Scope: 示波器硬件信息类，提供通道数、硬件配置等
 *   - ScopeConfig: 示波器配置类，提供出厂日期等配置信息
 *   - CacheUtil: 缓存工具类，用于持久化配置参数
 *   - IWorkMode: 工作模式接口，定义三种工作模式常量
 *   - TopUtilScale: 顶部工具栏缩放工具类
 * - 被依赖:
 *   - 整个示波器应用的UI层和业务逻辑层
 *   - 所有需要获取屏幕尺寸、工作模式参数的组件
 * 
 * 【单例模式说明】
 * - 实现方式: 静态内部类 (Initialization-on-demand holder idiom)
 * - 线程安全: 由JVM类加载机制保证
 * - 延迟加载: 首次调用get()方法时才初始化
 * - 优点: 
 *   - 无需同步锁，性能优异
 *   - 实现简单，代码清晰
 *   - 自动支持序列化机制
 * 
 * 【使用示例】
 * <pre>
 * // 获取单例实例
 * GlobalVar globalVar = GlobalVar.get();
 * 
 * // 初始化 (在Application.onCreate中调用)
 * globalVar.init(context);
 * 
 * // 获取屏幕尺寸
 * Rect screen = globalVar.getScreen();
 * 
 * // 根据工作模式获取波形区域尺寸
 * int width = globalVar.getWaveZoneWidth_Pix(WorkMode_YT);
 * int height = globalVar.getWaveZoneHeight_Pix(WorkMode_YT);
 * </pre>
 * 
 * 【注意事项】
 * 1. 必须在Application启动时调用init()方法进行初始化
 * 2. 屏幕尺寸参数从资源文件读取，支持不同屏幕尺寸的适配
 * 3. 工作模式切换时，相关参数会自动适配
 * 4. changeMainWaveH()方法会触发相关参数的重新计算
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/5/8
 * @see IWorkMode 工作模式接口
 * @see Scope 示波器核心类
 * @see ScopeConfig 示波器配置类
 */
public class GlobalVar {
    
    //region 单例创建
    
    /**
     * 静态内部类持有者 - 实现线程安全的单例模式
     * 
     * 【设计原理】
     * - 利用JVM类加载机制保证线程安全
     * - 静态内部类在首次访问时才加载，实现延迟初始化
     * - 无需同步锁，性能最优
     */
    private static class GlobalVarHolder {
        
        /** 单例实例 - 静态final保证唯一性和不可变性 */
        private static final GlobalVar instance = new GlobalVar(); // 创建唯一的GlobalVar实例
    }

    /**
     * 获取GlobalVar单例实例
     * 
     * 【功能说明】
     * - 提供全局唯一的GlobalVar实例访问点
     * - 线程安全，无需同步
     * 
     * 【调用时机】
     * - 任何需要访问全局变量时调用
     * - 通常在UI组件初始化、业务逻辑处理时调用
     * 
     * 【使用示例】
     * <pre>
     * GlobalVar globalVar = GlobalVar.get();
     * int channelCount = globalVar.getChannelsCount();
     * </pre>
     * 
     * @return GlobalVar单例实例，永不返回null
     */
    public static final GlobalVar get() {
        return GlobalVarHolder.instance; // 返回静态内部类持有的单例实例
    }

    /** Android应用上下文 - 用于获取资源和系统服务 */
    private Context context; // 存储Application Context，用于访问资源

    /**
     * 初始化GlobalVar
     * 
     * 【功能说明】
     * - 设置应用上下文
     * - 初始化屏幕尺寸参数
     * - 必须在Application.onCreate()中调用
     * 
     * 【参数说明】
     * @param context Android应用上下文 (Application Context)，不能为null
     * 
     * 【调用时机】
     * - 在Application.onCreate()方法中调用
     * - 在任何其他方法调用前必须先调用此方法
     * 
     * 【注意事项】
     * - 只需调用一次，多次调用会覆盖之前的配置
     * - 传入的context应该是Application Context，避免内存泄漏
     * 
     * 【使用示例】
     * <pre>
     * public class MyApplication extends Application {
     *     public void onCreate() {
     *         super.onCreate();
     *         GlobalVar.get().init(this);
     *     }
     * }
     * </pre>
     */
    public void init(Context context) {
        this.context = context; // 保存应用上下文引用
        initScreen(); // 初始化屏幕尺寸参数
    }
    //endregion



    //region YT模式 参数定义
    
    /**
     * 获取波形区域的宽度(像素)
     * 
     * 【功能说明】
     * - 根据不同工作模式返回波形区域的宽度
     * - 不同模式的宽度可能不同
     * 
     * 【参数说明】
     * @param workMode 工作模式，取值范围:
     *                  - WorkMode_XY: XY模式
     *                  - WorkMode_YT: YT模式
     *                  - WorkMode_YTZOOM: YT缩放模式
     * 
     * 【返回值说明】
     * @return 波形区域宽度(像素)
     *         - WorkMode_XY: 固定返回800
     *         - WorkMode_YT: 返回mainWave.x
     *         - WorkMode_YTZOOM: 返回mainWave.x
     * 
     * 【调用时机】
     * - 波形绘制时确定绘制区域宽度
     * - 测量功能计算位置时
     * - UI布局计算时
     * 
     * 【使用示例】
     * <pre>
     * int width = GlobalVar.get().getWaveZoneWidth_Pix(WorkMode_YT);
     * </pre>
     */
    public int getWaveZoneWidth_Pix(@IWorkMode.WorkMode int workMode) {
        switch (workMode) { // 根据工作模式选择对应的宽度
            case WorkMode_XY: // XY模式
                return 800; // XY模式固定宽度800像素
            case WorkMode_YT: // YT模式
                return mainWave.x; // 返回主波形区域的宽度
            case WorkMode_YTZOOM: // YT缩放模式
                return mainWave.x; // 返回主波形区域的宽度(与YT模式相同)
        }
        return mainWave.x; // 默认返回主波形区域的宽度
    }

    /**
     * 获取波形区域的高度(像素)
     * 
     * 【功能说明】
     * - 根据不同工作模式返回波形区域的高度
     * - YTZOOM模式的高度会根据缩放系数调整
     * 
     * 【参数说明】
     * @param workMode 工作模式，取值范围:
     *                  - WorkMode_XY: XY模式
     *                  - WorkMode_YT: YT模式
     *                  - WorkMode_YTZOOM: YT缩放模式
     * 
     * 【返回值说明】
     * @return 波形区域高度(像素)
     *         - WorkMode_XY: 固定返回800
     *         - WorkMode_YT: 返回mainWave.y
     *         - WorkMode_YTZOOM: 返回(int)(mainWave.y * ytToZoomCoef)
     * 
     * 【调用时机】
     * - 波形绘制时确定绘制区域高度
     * - 测量功能计算位置时
     * - UI布局计算时
     * 
     * 【使用示例】
     * <pre>
     * int height = GlobalVar.get().getWaveZoneHeight_Pix(WorkMode_YTZOOM);
     * </pre>
     */
    public int getWaveZoneHeight_Pix(@IWorkMode.WorkMode int workMode) {
        switch (workMode) { // 根据工作模式选择对应的高度
            case WorkMode_XY: // XY模式
                return 800; // XY模式固定高度800像素
            case WorkMode_YT: // YT模式
                return mainWave.y; // 返回主波形区域的高度
            case WorkMode_YTZOOM: // YT缩放模式
                return (int) (mainWave.y * ytToZoomCoef); // 返回缩放后的高度(主波形高度乘以缩放系数)
        }
        return mainWave.y; // 默认返回主波形区域的高度
    }

    /**
     * 获取光标测量结果的显示位置
     * 
     * 【功能说明】
     * - 返回光标测量结果文本的显示位置坐标
     * - 不同工作模式下的位置不同
     * 
     * 【参数说明】
     * @param workMode 工作模式，取值范围:
     *                  - WorkMode_XY: XY模式
     *                  - WorkMode_YT: YT模式
     *                  - WorkMode_YTZOOM: YT缩放模式
     * 
     * 【返回值说明】
     * @return 光标测量结果的显示位置(Point)，XY模式返回null
     *         - WorkMode_XY: null (XY模式不支持光标测量)
     *         - WorkMode_YT: Point(1460, 60)
     *         - WorkMode_YTZOOM: Point(1460, 60 * toZoomCoef)
     * 
     * 【调用时机】
     * - 光标测量功能显示测量结果时
     * - UI布局计算测量结果显示位置时
     * 
     * 【使用示例】
     * <pre>
     * Point position = GlobalVar.get().getMeasureCursorPosition(WorkMode_YT);
     * if (position != null) {
     *     textView.setX(position.x);
     *     textView.setY(position.y);
     * }
     * </pre>
     */
    public Point getMeasureCursorPosition(@IWorkMode.WorkMode int workMode) {
        switch (workMode) { // 根据工作模式选择对应的位置
            case WorkMode_XY: // XY模式
                return new Point(500, 40); // XY模式返回固定位置(500, 40)
            case WorkMode_YT: // YT模式
                return new Point(1460, 60); // YT模式返回固定位置(1460, 60)
            case WorkMode_YTZOOM: // YT缩放模式
                return new Point(1460, ((int) (60 * toZoomCoef()))); // YTZOOM模式返回缩放后的位置
        }
        return new Point(1460, 60); // 默认返回YT模式的位置
    }

    /**
     * 获取全部测量项的显示位置
     * 
     * 【功能说明】
     * - 返回全部测量项列表的显示位置坐标
     * - 不同工作模式下的位置不同
     * 
     * 【参数说明】
     * @param workMode 工作模式，取值范围:
     *                  - WorkMode_XY: XY模式
     *                  - WorkMode_YT: YT模式
     *                  - WorkMode_YTZOOM: YT缩放模式
     * 
     * 【返回值说明】
     * @return 全部测量项的显示位置(Point)，XY模式返回null
     *         - WorkMode_XY: null (XY模式不支持全部测量)
     *         - WorkMode_YT: Point(0, 700)
     *         - WorkMode_YTZOOM: Point(0, 660 * toZoomCoef)
     * 
     * 【调用时机】
     * - 显示全部测量项列表时
     * - UI布局计算测量项列表位置时
     * 
     * 【使用示例】
     * <pre>
     * Point position = GlobalVar.get().getMeasureAllPosition(WorkMode_YT);
     * </pre>
     */
    public Point getMeasureAllPosition(@IWorkMode.WorkMode int workMode) {
        switch (workMode) { // 根据工作模式选择对应的位置
            case WorkMode_XY: // XY模式
                return null; // XY模式不支持全部测量，返回null
            case WorkMode_YT: // YT模式
                return new Point(0, 700); // YT模式返回固定位置(0, 700)
            case WorkMode_YTZOOM: // YT缩放模式
                return new Point(0, (int)(660*toZoomCoef())); // YTZOOM模式返回缩放后的位置
            //return new Point(0, ((int) (330 * 0.8))); // 注释掉的旧代码
        }
        return new Point(0, 790); // 默认返回位置(0, 790)
    }
    
    /**
     * 获取测量项的显示位置
     * 
     * 【功能说明】
     * - 返回单个测量项的显示位置坐标
     * - 不同工作模式下的位置不同
     * 
     * 【参数说明】
     * @param workMode 工作模式，取值范围:
     *                  - WorkMode_XY: XY模式
     *                  - WorkMode_YT: YT模式
     *                  - WorkMode_YTZOOM: YT缩放模式
     * 
     * 【返回值说明】
     * @return 测量项的显示位置(Point)，XY模式返回null
     *         - WorkMode_XY: null (XY模式不支持测量项)
     *         - WorkMode_YT: Point(0, 910)
     *         - WorkMode_YTZOOM: Point(0, 890 * toZoomCoef)
     * 
     * 【调用时机】
     * - 显示单个测量项时
     * - UI布局计算测量项位置时
     * 
     * 【使用示例】
     * <pre>
     * Point position = GlobalVar.get().getMeasureItemPosition(WorkMode_YT);
     * </pre>
     */
    public Point getMeasureItemPosition(@IWorkMode.WorkMode int workMode) {
        switch (workMode) { // 根据工作模式选择对应的位置
            case WorkMode_XY: // XY模式
                return null; // XY模式不支持测量项，返回null
            case WorkMode_YT: // YT模式
//                return new Point(0, 450); // 注释掉的旧代码
                return new Point(0, 910); // YT模式返回固定位置(0, 910)
            case WorkMode_YTZOOM: // YT缩放模式
//                return new Point(0,349); // 注释掉的旧代码
                return new Point(0, (int)(890*toZoomCoef())); // YTZOOM模式返回缩放后的位置
            //return new Point(0, ((int) (450 * 0.8))); // 注释掉的旧代码
        }
        return new Point(0, 910); // 默认返回YT模式的位置
    }
    
    /**
     * 获取频率计的显示位置
     * 
     * 【功能说明】
     * - 返回频率计测量结果的显示位置坐标
     * - 不同工作模式下的位置不同
     * 
     * 【参数说明】
     * @param workMode 工作模式，取值范围:
     *                  - WorkMode_XY: XY模式
     *                  - WorkMode_YT: YT模式
     *                  - WorkMode_YTZOOM: YT缩放模式
     * 
     * 【返回值说明】
     * @return 频率计的显示位置(Point)，XY模式返回null
     *         - WorkMode_XY: null (XY模式不支持频率计)
     *         - WorkMode_YT: Point(100, 60)
     *         - WorkMode_YTZOOM: Point(100, 60 * toZoomCoef)
     * 
     * 【调用时机】
     * - 显示频率计测量结果时
     * - UI布局计算频率计位置时
     * 
     * 【使用示例】
     * <pre>
     * Point position = GlobalVar.get().getMeasureFrequencyMeterPosition(WorkMode_YT);
     * </pre>
     */
    public Point getMeasureFrequencyMeterPosition(@IWorkMode.WorkMode int workMode) {
        switch (workMode) { // 根据工作模式选择对应的位置
            case WorkMode_XY: // XY模式
                return null; // XY模式不支持频率计，返回null
            case WorkMode_YT: // YT模式
                return new Point(100, 60); // YT模式返回固定位置(100, 60)
            case WorkMode_YTZOOM: // YT缩放模式
                return new Point(100, ((int) (60 * toZoomCoef()))); // YTZOOM模式返回缩放后的位置
        }
        return new Point(100, 60); // 默认返回YT模式的位置
    }

    /**
     * 获取FFT测量结果的显示位置
     * 
     * 【功能说明】
     * - 返回FFT(快速傅里叶变换)测量结果的显示位置坐标
     * - 不同工作模式下的位置不同
     * 
     * 【参数说明】
     * @param workMode 工作模式，取值范围:
     *                  - WorkMode_XY: XY模式
     *                  - WorkMode_YT: YT模式
     *                  - WorkMode_YTZOOM: YT缩放模式
     * 
     * 【返回值说明】
     * @return FFT测量结果的显示位置(Point)，XY模式返回null
     *         - WorkMode_XY: null (XY模式不支持FFT)
     *         - WorkMode_YT: Point(100, 116)
     *         - WorkMode_YTZOOM: Point(100, 116 * toZoomCoef)
     * 
     * 【调用时机】
     * - 显示FFT测量结果时
     * - UI布局计算FFT结果显示位置时
     * 
     * 【使用示例】
     * <pre>
     * Point position = GlobalVar.get().getMeasureFFTPosition(WorkMode_YT);
     * </pre>
     */
    public Point getMeasureFFTPosition(@IWorkMode.WorkMode int workMode) {
        switch (workMode) { // 根据工作模式选择对应的位置
            case WorkMode_XY: // XY模式
                return null; // XY模式不支持FFT，返回null
            case WorkMode_YT: // YT模式
                return new Point(100, 116); // YT模式返回固定位置(100, 116)
            case WorkMode_YTZOOM: // YT缩放模式
                return new Point(100, (int) (116 * toZoomCoef())); // YTZOOM模式返回缩放后的位置
        }
        return new Point(100, 116); // 默认返回YT模式的位置
    }


    /**
     * 获取分段测量的显示位置
     * 
     * 【功能说明】
     * - 返回分段测量功能的显示位置坐标
     * - 不同工作模式下的位置不同
     * 
     * 【参数说明】
     * @param workMode 工作模式，取值范围:
     *                  - WorkMode_XY: XY模式
     *                  - WorkMode_YT: YT模式
     *                  - WorkMode_YTZOOM: YT缩放模式
     * 
     * 【返回值说明】
     * @return 分段测量的显示位置(Point)，XY模式返回null
     *         - WorkMode_XY: null (XY模式不支持分段测量)
     *         - WorkMode_YT: Point(30, 5)
     *         - WorkMode_YTZOOM: Point(30, 5 * toZoomCoef)
     * 
     * 【调用时机】
     * - 显示分段测量UI时
     * - UI布局计算分段测量位置时
     * 
     * 【使用示例】
     * <pre>
     * Point position = GlobalVar.get().getMeasureSegmentPosition(WorkMode_YT);
     * </pre>
     */
    public Point getMeasureSegmentPosition(@IWorkMode.WorkMode int workMode) {
        switch (workMode) { // 根据工作模式选择对应的位置
            case WorkMode_XY: // XY模式
                return null; // XY模式不支持分段测量，返回null
            case WorkMode_YT: // YT模式
                return new Point(30, 5); // YT模式返回固定位置(30, 5)
            case WorkMode_YTZOOM: // YT缩放模式
                return new Point(30, (int) (5 * toZoomCoef())); // YTZOOM模式返回缩放后的位置
        }
        return new Point(30, 5); // 默认返回YT模式的位置
    }


    //endregion

    //region 屏幕尺寸
    
    /** 屏幕完整区域 - 包含所有UI元素的矩形区域 */
    private Rect screen; // 存储屏幕完整区域的矩形对象
    
    /** 主波形显示区域 - 用于YT/YTZOOM模式的波形显示 */
    private Point mainWave; // 存储主波形区域的宽度和高度
    
    /** 顶部工具栏区域 - 显示时间、触发等信息 */
    private Point mainTop; // 存储顶部区域的宽度和高度
    
    /** 底部快捷操作区域 - 显示快捷按钮等 */
    private Point bottomSlip; // 存储底部区域的宽度和高度
    
    /** 双通道名称数组 - 存储2通道示波器的通道名称 */
    private String[] channelsNameDouble; // 存储2通道示波器的通道名称列表
    
    /** 四通道名称数组 - 存储4通道示波器的通道名称 */
    private String[] channelsNameFour; // 存储4通道示波器的通道名称列表
    
    /** 八通道名称数组 - 存储8通道示波器的通道名称 */
    private String[] channelsNameEight; // 存储8通道示波器的通道名称列表
    
    /** YT模式到YTZOOM模式的缩放系数 - 用于位置转换 */
    private float ytToZoomCoef; // 存储YT到YTZOOM的缩放系数
    
    /** YTZOOM模式到YT模式的缩放系数 - 用于位置转换 */
    private float zoomToYtCoef; // 存储YTZOOM到YT的缩放系数
    
    /** 缩放窗口区域 - YTZOOM模式下的缩放窗口 */
    private Rect rectZoom; // 存储缩放窗口的矩形区域
    
    /** 缩放窗口高度 - 默认为主波形高度的1/4 */
    private float zoomH = 1000.0f / 4; // 存储缩放窗口的高度，初始值为250
    
    /** 测量行高度映射表 - 存储不同测量行数对应的高度 */
    private final HashMap<Integer, Integer> measureRowToHeightMap = new HashMap<>(); // 存储测量行索引到高度的映射

    /**
     * 初始化屏幕尺寸参数
     * 
     * 【功能说明】
     * - 从资源文件读取屏幕尺寸参数
     * - 初始化所有屏幕区域参数
     * - 计算缩放系数
     * - 初始化通道名称数组
     * - 初始化测量行高度映射表
     * 
     * 【调用时机】
     * - 在init()方法中被调用
     * - Application启动时执行一次
     * 
     * 【注意事项】
     * - 必须在context初始化后调用
     * - 资源文件中的尺寸值必须已定义
     * - 会覆盖之前的所有参数值
     */
    private void initScreen() {
        Resources res = context.getResources(); // 获取资源对象
        
        // 初始化屏幕完整区域
        screen = new Rect(0, 0, (int) res.getDimension(R.dimen.screenWidth), (int) res.getDimension(R.dimen.screenHeight)); // 创建屏幕矩形区域
        
        // 初始化主波形区域
        mainWave = new Point((int) res.getDimension(R.dimen.mainCenterWidth), (int) res.getDimension(R.dimen.mainCenterHeightMeasureRow0)); // 创建主波形区域点对象
        
        // 初始化顶部区域
        mainTop = new Point(((int) res.getDimension(R.dimen.mainTopWidth)), (int) res.getDimension(R.dimen.mainTopHeight)); // 创建顶部区域点对象
        
        // 初始化底部区域
        bottomSlip = new Point(((int) res.getDimension(R.dimen.mainBottomQuickWidth)), (int) res.getDimension(R.dimen.mainBottomQuickHeight)); // 创建底部区域点对象
        
        // 初始化缩放窗口区域
        rectZoom = new Rect(0, 0, mainWave.x, (int) zoomH); // 创建缩放窗口矩形区域
        
        // 初始化通道名称数组
        channelsNameDouble = res.getStringArray(R.array.channelsNameDouble); // 获取双通道名称数组
        channelsNameFour = res.getStringArray(R.array.channelsNameFour); // 获取四通道名称数组
        channelsNameEight = res.getStringArray(R.array.channelsNameEight); // 获取八通道名称数组
        
        // 计算缩放系数
        ytToZoomCoef = (mainWave.y - zoomH) / mainWave.y; // 计算YT到YTZOOM的缩放系数
        zoomToYtCoef = mainWave.y / (mainWave.y - zoomH); // 计算YTZOOM到YT的缩放系数

        // 初始化测量行高度映射表
        measureRowToHeightMap.put(0, (int) res.getDimension(R.dimen.mainCenterHeightMeasureRow0)); // 添加0行对应的高度
        measureRowToHeightMap.put(1, (int) res.getDimension(R.dimen.mainCenterHeightMeasureRow1)); // 添加1行对应的高度
        measureRowToHeightMap.put(2, (int) res.getDimension(R.dimen.mainCenterHeightMeasureRow2)); // 添加2行对应的高度
        measureRowToHeightMap.put(3, (int) res.getDimension(R.dimen.mainCenterHeightMeasureRow3)); // 添加3行对应的高度
        measureRowToHeightMap.put(4, (int) res.getDimension(R.dimen.mainCenterHeightMeasureRow4)); // 添加4行对应的高度
    }

    /**
     * 获取屏幕完整区域
     * 
     * 【功能说明】
     * - 返回屏幕完整区域的矩形对象
     * - 包含所有UI元素的区域
     * 
     * 【返回值说明】
     * @return 屏幕完整区域的Rect对象，包含left、top、right、bottom四个值
     *         - left: 0
     *         - top: 0
     *         - right: 屏幕宽度
     *         - bottom: 屏幕高度
     * 
     * 【调用时机】
     * - 需要获取屏幕尺寸时
     * - UI布局计算时
     * - 坐标转换时
     * 
     * 【使用示例】
     * <pre>
     * Rect screen = GlobalVar.get().getScreen();
     * int screenWidth = screen.width();
     * int screenHeight = screen.height();
     * </pre>
     */
    public Rect getScreen() {
        return screen; // 返回屏幕完整区域的矩形对象
    }
    
    /**
     * 获取缩放窗口区域
     * 
     * 【功能说明】
     * - 返回YTZOOM模式下的缩放窗口区域
     * - 用于显示波形的缩放视图
     * 
     * 【返回值说明】
     * @return 缩放窗口区域的Rect对象
     *         - left: 0
     *         - top: 0
     *         - right: mainWave.x
     *         - bottom: zoomH
     * 
     * 【调用时机】
     * - YTZOOM模式下绘制缩放窗口时
     * - 计算缩放窗口位置时
     * 
     * 【使用示例】
     * <pre>
     * Rect zoomRect = GlobalVar.get().getRectZoom();
     * </pre>
     */
    public Rect getRectZoom(){return rectZoom;} // 返回缩放窗口区域的矩形对象
    
    /**
     * 获取主波形显示区域
     * 
     * 【功能说明】
     * - 返回主波形显示区域的尺寸
     * - 用于YT/YTZOOM模式的波形显示
     * 
     * 【返回值说明】
     * @return 主波形区域的Point对象
     *         - x: 主波形区域宽度
     *         - y: 主波形区域高度
     * 
     * 【调用时机】
     * - 波形绘制时确定绘制区域
     * - 测量功能计算位置时
     * - UI布局计算时
     * 
     * 【使用示例】
     * <pre>
     * Point mainWave = GlobalVar.get().getMainWave();
     * int width = mainWave.x;
     * int height = mainWave.y;
     * </pre>
     */
    public Point getMainWave() {
        return mainWave; // 返回主波形区域的点对象
    }
    
    /**
     * 判断x坐标是否在主波形区域内
     * 
     * 【功能说明】
     * - 判断单个x坐标是否在主波形区域的x范围内
     * - 主波形区域的x范围: [0, mainWave.x)
     * 
     * 【参数说明】
     * @param x 要判断的x坐标值
     * 
     * 【返回值说明】
     * @return true: x坐标在主波形区域内
     *         false: x坐标不在主波形区域内
     * 
     * 【调用时机】
     * - 触摸事件处理时判断是否点击在波形区域
     * - 坐标有效性验证时
     * 
     * 【使用示例】
     * <pre>
     * if (GlobalVar.get().isContainMainWaveX(touchX)) {
     *     // 处理波形区域内的点击事件
     * }
     * </pre>
     */
    public boolean isContainMainWaveX(int x){
        return x>=0 && x<mainWave.x; // 判断x是否在[0, mainWave.x)范围内
    }
    
    /**
     * 判断两个x坐标是否都在主波形区域内
     * 
     * 【功能说明】
     * - 判断两个x坐标是否都在主波形区域的x范围内
     * - 用于判断一个区间是否完全在主波形区域内
     * 
     * 【参数说明】
     * @param x1 第一个x坐标值
     * @param x2 第二个x坐标值
     * 
     * 【返回值说明】
     * @return true: 两个x坐标都在主波形区域内
     *         false: 至少一个x坐标不在主波形区域内
     * 
     * 【调用时机】
     * - 判断线段是否完全在波形区域内
     * - 判断区间是否有效时
     * 
     * 【使用示例】
     * <pre>
     * if (GlobalVar.get().isContainMainWaveX(startX, endX)) {
     *     // 处理完全在波形区域内的线段
     * }
     * </pre>
     */
    public boolean isContainMainWaveX(int x1,int x2){
        return isContainMainWaveX(x1) && isContainMainWaveX(x2); // 判断两个x坐标是否都在主波形区域内
    }
    
    /**
     * 判断y坐标是否在主波形区域内
     * 
     * 【功能说明】
     * - 判断单个y坐标是否在主波形区域的y范围内
     * - 主波形区域的y范围: [0, mainWave.y)
     * 
     * 【参数说明】
     * @param y 要判断的y坐标值
     * 
     * 【返回值说明】
     * @return true: y坐标在主波形区域内
     *         false: y坐标不在主波形区域内
     * 
     * 【调用时机】
     * - 触摸事件处理时判断是否点击在波形区域
     * - 坐标有效性验证时
     * 
     * 【使用示例】
     * <pre>
     * if (GlobalVar.get().isContainMainWaveY(touchY)) {
     *     // 处理波形区域内的点击事件
     * }
     * </pre>
     */
    public boolean isContainMainWaveY(int y){
        return y>=0 && y< mainWave.y; // 判断y是否在[0, mainWave.y)范围内
    }
    
    /**
     * 判断两个y坐标是否都在主波形区域内
     * 
     * 【功能说明】
     * - 判断两个y坐标是否都在主波形区域的y范围内
     * - 用于判断一个区间是否完全在主波形区域内
     * 
     * 【参数说明】
     * @param y1 第一个y坐标值
     * @param y2 第二个y坐标值
     * 
     * 【返回值说明】
     * @return true: 两个y坐标都在主波形区域内
     *         false: 至少一个y坐标不在主波形区域内
     * 
     * 【调用时机】
     * - 判断线段是否完全在波形区域内
     * - 判断区间是否有效时
     * 
     * 【使用示例】
     * <pre>
     * if (GlobalVar.get().isContainMainWaveY(startY, endY)) {
     *     // 处理完全在波形区域内的线段
     * }
     * </pre>
     */
    public boolean isContainMainWaveY(int y1,int y2){
        return isContainMainWaveY(y1) && isContainMainWaveY(y2); // 判断两个y坐标是否都在主波形区域内
    }
    
    /**
     * 获取顶部工具栏区域
     * 
     * 【功能说明】
     * - 返回顶部工具栏区域的尺寸
     * - 用于显示时间、触发等信息
     * 
     * 【返回值说明】
     * @return 顶部区域的Point对象
     *         - x: 顶部区域宽度
     *         - y: 顶部区域高度
     * 
     * 【调用时机】
     * - 顶部工具栏布局计算时
     * - UI组件定位时
     * 
     * 【使用示例】
     * <pre>
     * Point mainTop = GlobalVar.get().getMainTop();
     * int topWidth = mainTop.x;
     * int topHeight = mainTop.y;
     * </pre>
     */
    public Point getMainTop() {
        return mainTop; // 返回顶部区域的点对象
    }

    /**
     * 获取底部快捷操作区域
     * 
     * 【功能说明】
     * - 返回底部快捷操作区域的尺寸
     * - 用于显示快捷按钮等
     * 
     * 【返回值说明】
     * @return 底部区域的Point对象
     *         - x: 底部区域宽度
     *         - y: 底部区域高度
     * 
     * 【调用时机】
     * - 底部快捷操作区域布局计算时
     * - UI组件定位时
     * 
     * 【使用示例】
     * <pre>
     * Point bottomSlip = GlobalVar.get().getBottomSlip();
     * int bottomWidth = bottomSlip.x;
     * int bottomHeight = bottomSlip.y;
     * </pre>
     */
    public Point getBottomSlip() {
        return bottomSlip; // 返回底部区域的点对象
    }

    /**
     * 获取YT模式到YTZOOM模式的缩放系数
     * 
     * 【功能说明】
     * - 返回从YT模式位置转换到YTZOOM模式位置的缩放系数
     * - 用于位置坐标的转换
     * 
     * 【返回值说明】
     * @return YT到YTZOOM的缩放系数
     *         - 计算公式: (mainWave.y - zoomH) / mainWave.y
     *         - 典型值: 约0.75 (当zoomH为mainWave.y的1/4时)
     * 
     * 【调用时机】
     * - YTZOOM模式下计算UI组件位置时
     * - 从YT模式切换到YTZOOM模式时
     * 
     * 【使用示例】
     * <pre>
     * float coef = GlobalVar.get().toZoomCoef();
     * int zoomY = (int) (ytY * coef); // 将YT模式的y坐标转换为YTZOOM模式的y坐标
     * </pre>
     */
    public float toZoomCoef(){
       return ytToZoomCoef; // 返回YT到YTZOOM的缩放系数
    }
    
    /**
     * 获取YTZOOM模式到YT模式的缩放系数
     * 
     * 【功能说明】
     * - 返回从YTZOOM模式位置转换到YT模式位置的缩放系数
     * - 用于位置坐标的转换
     * 
     * 【返回值说明】
     * @return YTZOOM到YT的缩放系数
     *         - 计算公式: mainWave.y / (mainWave.y - zoomH)
     *         - 典型值: 约1.33 (当zoomH为mainWave.y的1/4时)
     *         - 是toZoomCoef()的倒数
     * 
     * 【调用时机】
     * - 从YTZOOM模式切换到YT模式时
     * - 需要将YTZOOM模式的位置转换为YT模式位置时
     * 
     * 【使用示例】
     * <pre>
     * float coef = GlobalVar.get().toYTCoef();
     * int ytY = (int) (zoomY * coef); // 将YTZOOM模式的y坐标转换为YT模式的y坐标
     * </pre>
     */
    public float toYTCoef(){
        return zoomToYtCoef; // 返回YTZOOM到YT的缩放系数
    }
    //endregion

    //region 界面配置

    /** 双通道常量 - 表示示波器有2个通道 */
    public static final int CHANNEL_COUNT_2 = 2; // 定义双通道常量值为2
    
    /** 四通道常量 - 表示示波器有4个通道 */
    public static final int CHANNEL_COUNT_4 = 4; // 定义四通道常量值为4
    
    /** 八通道常量 - 表示示波器有8个通道 */
    public static final int CHANNEL_COUNT_8 = 8; // 定义八通道常量值为8

    /**
     * 获取当前示波器的通道数
     * 
     * 【功能说明】
     * - 返回当前示波器硬件支持的通道数量
     * - 通道数由硬件配置决定
     * 
     * 【返回值说明】
     * @return 通道数量，可能的值:
     *         - CHANNEL_COUNT_2 (2): 双通道示波器
     *         - CHANNEL_COUNT_4 (4): 四通道示波器
     *         - CHANNEL_COUNT_8 (8): 八通道示波器
     * 
     * 【调用时机】
     * - 需要根据通道数进行UI适配时
     * - 初始化通道列表时
     * - 验证通道操作有效性时
     * 
     * 【使用示例】
     * <pre>
     * int channelCount = GlobalVar.get().getChannelsCount();
     * if (channelCount == GlobalVar.CHANNEL_COUNT_4) {
     *     // 四通道示波器的特殊处理
     * }
     * </pre>
     */
    public int getChannelsCount() {
        return Scope.getInstance().getChNum(); // 从Scope硬件类获取通道数量
    }

    /**
     * 修改主波形区域高度
     * 
     * 【功能说明】
     * - 动态修改主波形区域的高度
     * - 重新计算相关的缩放系数和区域参数
     * - 将新高度持久化到缓存
     * 
     * 【参数说明】
     * @param h 新的主波形区域高度(像素)，必须为正整数
     * 
     * 【副作用】
     * - 修改mainWave.y的值
     * - 重新计算zoomH (h >> 2)
     * - 重新计算ytToZoomCoef和zoomToYtCoef
     * - 更新rectZoom的高度
     * - 将新高度保存到缓存
     * 
     * 【调用时机】
     * - 用户调整波形显示区域高度时
     * - 切换测量行数时
     * - 需要动态调整界面布局时
     * 
     * 【注意事项】
     * - 调用此方法会触发多个参数的重新计算
     * - 新高度会持久化，下次启动时仍有效
     * 
     * 【使用示例】
     * <pre>
     * GlobalVar.get().changeMainWaveH(800); // 将主波形高度设置为800像素
     * </pre>
     */
    public void changeMainWaveH(int h) {
        mainWave.y = h; // 更新主波形区域高度
        zoomH = h >> 2; // 更新缩放窗口高度 (主波形高度的1/4)
        ytToZoomCoef = (mainWave.y - zoomH) / mainWave.y; // 重新计算YT到YTZOOM的缩放系数
        zoomToYtCoef = mainWave.y / (mainWave.y - zoomH); // 重新计算YTZOOM到YT的缩放系数
        rectZoom.set(rectZoom.left, rectZoom.top, rectZoom.right, (int) zoomH); // 更新缩放窗口矩形的高度
        CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_ZONE_HEIGHT, String.valueOf(h)); // 将新高度持久化到缓存
    }

    /**
     * 获取通道名称数组
     * 
     * 【功能说明】
     * - 根据当前示波器的通道数返回对应的通道名称数组
     * - 通道名称从资源文件加载
     * 
     * 【返回值说明】
     * @return 通道名称数组
     *         - 2通道: 返回channelsNameDouble (如: ["CH1", "CH2"])
     *         - 4通道: 返回channelsNameFour (如: ["CH1", "CH2", "CH3", "CH4"])
     *         - 8通道: 返回channelsNameEight (如: ["CH1", "CH2", ..., "CH8"])
     * 
     * 【调用时机】
     * - 显示通道列表时
     * - 初始化通道选择器时
     * - 需要获取通道名称时
     * 
     * 【使用示例】
     * <pre>
     * String[] channelNames = GlobalVar.get().getChannelsName();
     * for (String name : channelNames) {
     *     System.out.println(name); // 输出通道名称
     * }
     * </pre>
     */
    public String[] getChannelsName() {
        if (getChannelsCount() == CHANNEL_COUNT_2) { // 判断是否为双通道示波器
            return channelsNameDouble; // 返回双通道名称数组
        } else if (getChannelsCount() == CHANNEL_COUNT_4) { // 判断是否为四通道示波器
            return channelsNameFour; // 返回四通道名称数组
        } else { // 其他情况(八通道示波器)
            return channelsNameEight; // 返回八通道名称数组
        }
    }

    /**
     * 获取测量项的总数量
     * 
     * 【功能说明】
     * - 返回示波器支持的测量项总数量
     * - 固定返回40个测量项
     * 
     * 【返回值说明】
     * @return 测量项总数量，固定为40
     * 
     * 【调用时机】
     * - 初始化测量项列表时
     * - 计算测量项分页时
     * - 验证测量项索引有效性时
     * 
     * 【使用示例】
     * <pre>
     * int totalMeasureItems = GlobalVar.get().getMeasureItemCount(); // 返回40
     * </pre>
     */
    public int getMeasureItemCount() {
        return 40; // 返回测量项总数量，固定为40
    }

    /**
     * 获取每行显示的测量项数量
     * 
     * 【功能说明】
     * - 返回每行显示的测量项数量
     * - 固定返回10个测量项
     * 
     * 【返回值说明】
     * @return 每行显示的测量项数量，固定为10
     * 
     * 【调用时机】
     * - 计算测量项布局时
     * - 计算测量项分页时
     * - UI布局计算时
     * 
     * 【使用示例】
     * <pre>
     * int itemsPerRow = GlobalVar.get().getMeasureItemPerRowCount(); // 返回10
     * int totalRows = GlobalVar.get().getMeasureItemCount() / itemsPerRow; // 计算总行数
     * </pre>
     */
    public int getMeasureItemPerRowCount() {
        return 10; // 返回每行显示的测量项数量，固定为10
    }

    /**
     * 判断是否显示工厂校准页面
     * 
     * 【功能说明】
     * - 判断是否应该显示工厂校准页面
     * - 根据出厂日期配置决定
     * 
     * 【返回值说明】
     * @return true: 显示工厂校准页面 (未设置出厂日期)
     *         false: 不显示工厂校准页面 (已设置出厂日期)
     * 
     * 【调用时机】
     * - 判断是否允许进入工厂校准页面时
     * - UI菜单显示控制时
     * 
     * 【使用示例】
     * <pre>
     * if (GlobalVar.get().isFactoryCalibration()) {
     *     // 显示工厂校准菜单项
     * }
     * </pre>
     */
    public boolean isFactoryCalibration() {
        return !ScopeConfig.getConfig().isDeliveryDate(); // 返回是否未设置出厂日期
    }

    /**
     * 获取触发电平时间项的最小识别间隔
     * 
     * 【功能说明】
     * - 返回触发电平中时间项的最小识别间隔
     * - 用于时间参数的精度控制
     * 
     * 【返回值说明】
     * @return 时间最小识别间隔，单位: 纳秒(ns)
     * 
     * 【调用时机】
     * - 设置触发电平时间参数时
     * - 验证时间参数有效性时
     * - 时间参数精度控制时
     * 
     * 【使用示例】
     * <pre>
     * long minInterval = GlobalVar.get().getTimeMinInterval(); // 获取最小时间间隔
     * if (Math.abs(time1 - time2) < minInterval) {
     *     // 两个时间值被认为相等
     * }
     * </pre>
     */
    public int getTimeMinInterval() {
        return TopUtilScale.TIME_MIN_INTERVAL; // 返回时间最小识别间隔
    }

    /**
     * 判断数学运算A×B功能是否可见
     * 
     * 【功能说明】
     * - 判断数学运算中的A×B功能是否应该显示
     * - 当前固定返回true，表示始终可见
     * 
     * 【返回值说明】
     * @return true: A×B功能可见
     *         false: A×B功能不可见
     * 
     * 【调用时机】
     * - 显示数学运算菜单时
     * - 判断是否允许使用A×B功能时
     * 
     * 【使用示例】
     * <pre>
     * if (GlobalVar.get().isMathAxbVisible()) {
     *     // 显示A×B数学运算选项
     * }
     * </pre>
     */
    public boolean isMathAxbVisible() {
        return true; // 固定返回true，表示A×B功能始终可见
    }



    /**
     * 判断是否启用通道零
     * 
     * 【功能说明】
     * - 判断是否启用了特殊的通道零功能
     * - 通道零通常用于特殊测量或参考信号
     * 
     * 【返回值说明】
     * @return true: 通道零已启用
     *         false: 通道零未启用
     * 
     * 【调用时机】
     * - 判断是否显示通道零选项时
     * - 验证通道操作有效性时
     * - 通道列表显示控制时
     * 
     * 【使用示例】
     * <pre>
     * if (GlobalVar.get().isEnableChannelZero()) {
     *     // 显示通道零相关功能
     * }
     * </pre>
     */
    public boolean isEnableChannelZero() {

        return Scope.getInstance().isEnableChannelZero(); // 从Scope硬件类获取通道零启用状态
    }

    /**
     * 设置是否启用通道零
     * 
     * 【功能说明】
     * - 设置通道零的启用状态
     * - 会影响硬件配置和UI显示
     * 
     * 【参数说明】
     * @param enableChannelZero 是否启用通道零
     *                          - true: 启用通道零
     *                          - false: 禁用通道零
     * 
     * 【调用时机】
     * - 用户切换通道零开关时
     * - 加载配置时
     * 
     * 【注意事项】
     * - 会影响硬件配置，需谨慎调用
     * - 设置后会立即生效
     * 
     * 【使用示例】
     * <pre>
     * GlobalVar.get().setEnableChannelZero(true); // 启用通道零
     * </pre>
     */
    public void setEnableChannelZero(boolean enableChannelZero) {
        Scope.getInstance().setEnableChannelZero(enableChannelZero); // 设置Scope硬件类的通道零启用状态
    }

    /**
     * 获取测量行高度映射表
     * 
     * 【功能说明】
     * - 返回测量行索引到高度的映射表
     * - 用于不同测量行数配置下的界面适配
     * 
     * 【返回值说明】
     * @return 测量行高度映射表
     *         - Key: 测量行索引 (0-4)
     *         - Value: 对应行的高度像素值
     * 
     * 【调用时机】
     * - 根据测量行数计算界面高度时
     * - UI布局适配时
     * 
     * 【使用示例】
     * <pre>
     * HashMap&lt;Integer, Integer&gt; map = GlobalVar.get().getMeasureRowToHeightMap();
     * int height = map.get(0); // 获取0行对应的高度
     * </pre>
     */
    public HashMap<Integer, Integer> getMeasureRowToHeightMap() {
        return measureRowToHeightMap; // 返回测量行高度映射表
    }

    //endregion
}