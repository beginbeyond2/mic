package com.micsig.tbook.tbookscope;   // 示波器主应用包，包含启动画面对话框类

import android.content.Context;   // 导入Android Context类，用于访问应用资源
import android.graphics.PixelFormat;   // 导入Android PixelFormat类，用于设置窗口像素格式
import android.view.Gravity;   // 导入Android Gravity类，用于设置窗口位置
import android.view.LayoutInflater;   // 导入Android LayoutInflater类，用于加载布局
import android.view.View;   // 导入Android View类，用于视图操作
import android.view.WindowManager;   // 导入Android WindowManager类，用于窗口管理

import com.micsig.base.Logger;   // 导入日志工具类，用于调试日志输出
import com.micsig.tbook.tbookscope.first.SplashScreenSurfaceView;   // 导入启动画面SurfaceView类，用于显示启动动画
import com.micsig.tbook.tbookscope.util.App;   // 导入应用工具类，用于获取应用上下文

/**
 * 启动画面对话框 - 应用启动时显示的欢迎画面（Splash Screen）
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    启动画面显示流程架构                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           FirstActivity                      │               │
 * │  │         (启动引导Activity)                    │               │
 * │  │                                             │               │
 * │  │  Step 1: 检查悬浮窗权限                       │               │
 * │  │    └─ Settings.canDrawOverlays()            │               │
 * │  │                                             │               │
 * │  │  Step 2: 显示启动画面                         │               │
 * │  │    └─ SplashDialog.get().showDialog()       │               │
 * │  │                                             │               │
 * │  │  Step 3: 启动MainActivity                    │               │
 * │  │    └─ onShowStartListener.onShowStart()     │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       │ showDialog()                           │
 * │                       │ 显示启动画面                           │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  SplashDialog        │                             │
 * │           │  (启动画面对话框)     │                             │
 * │           │                     │                             │
 * │           │  ┌───────────────┐  │                             │
 * │           │  │ WindowManager │  │                             │
 * │           │  │  (窗口管理器)  │  │                             │
 * │           │  └───────────────┘  │                             │
 * │           │                     │                             │
 * │           │  ┌───────────────┐  │                             │
 * │           │  │  dialogView   │  │                             │
 * │           │  │ (对话框视图)   │  │                             │
 * │           │  │               │  │                             │
 * │           │  │ ┌───────────┐ │  │                             │
 * │           │  │ │  splash   │ │  │                             │
 * │           │  │ │ (启动动画) │ │  │                             │
 * │           │  │ │SurfaceView│ │  │                             │
 * │           │  │ └───────────┘ │  │                             │
 * │           │  └───────────────┘  │                             │
 * │           │                     │                             │
 * │           │  状态管理：          │                             │
 * │           │    isFirst = true   │                             │
 * │           │    (已显示标志)      │                             │
 * │           └─────────────────────┘                             │
 * │                       │                                       │
 * │                       │ onShowStart()                          │
 * │                       │ 启动MainActivity                       │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  MainActivity       │                             │
 * │           │  (主Activity)       │                             │
 * │           │                     │                             │
 * │           │  Step 1: 初始化界面  │                             │
 * │           │  Step 2: 加载配置   │                             │
 * │           │  Step 3: 启动波形   │                             │
 * │           │                     │                             │
 * │           │  Step 4: 关闭启动画面│                             │
 * │           │    └─ SplashDialog  │                             │
 * │           │      .get()         │                             │
 * │           │      .closeDialog() │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是示波器应用的启动画面对话框，负责在应用启动时显示欢迎画面（Splash Screen）。
 * 通过WindowManager实现悬浮窗效果，在MainActivity初始化完成后关闭启动画面。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>启动画面显示</b>：在应用启动时显示欢迎画面，提升用户体验</li>
 *   <li><b>悬浮窗管理</b>：通过WindowManager实现悬浮窗效果，覆盖整个屏幕</li>
 *   <li><b>动画播放</b>：通过SplashScreenSurfaceView播放启动动画</li>
 *   <li><b>状态管理</b>：管理启动画面的显示状态（isFirst标志）</li>
 *   <li><b>回调通知</b>：通过OnShowStartListener通知启动完成，启动MainActivity</li>
 * </ul>
 * 
 * <h3>启动画面显示流程说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              启动画面显示流程详解                              │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Step 1: FirstActivity检查悬浮窗权限                           │
 * │    └─ Settings.canDrawOverlays()                              │
 * │    └─ 如果没有权限，请求悬浮窗权限                              │
 * │    └─ 如果有权限，显示启动画面                                  │
 * │                                                               │
 * │  Step 2: FirstActivity调用SplashDialog.showDialog()           │
 * │    └─ 获取WindowManager服务                                    │
 * │    └─ 加载dialog_splash布局                                    │
 * │    └─ 创建WindowManager.LayoutParams                           │
 * │    └─ 设置窗口参数（全屏、隐藏导航栏、沉浸式）                  │
 * │    └─ 设置窗口类型（paramsType）                               │
 * │    └─ 添加视图到WindowManager                                  │
 * │    └─ 设置isFirst = true                                       │
 * │    └─ 调用onShowStartListener.onShowStart()                    │
 * │                                                               │
 * │  Step 3: onShowStartListener回调启动MainActivity               │
 * │    └─ 启动MainActivity                                         │
 * │    └─ MainActivity初始化界面                                   │
 * │    └─ MainActivity加载配置                                     │
 * │    └─ MainActivity启动波形显示                                 │
 * │                                                               │
 * │  Step 4: MainActivity调用SplashDialog.closeDialog()           │
 * │    └─ 检查isFirst标志                                          │
 * │    └─ 设置isFirst = false                                      │
 * │    └─ 调用splash.stop()停止动画                                │
 * │    └─ 调用windowManager.removeViewImmediate()移除视图          │
 * │    └─ 启动画面关闭，显示主界面                                  │
 * │                                                               │
 * │  窗口参数说明：                                                 │
 * │    ┌─────────────────────────────────────────┐               │
 * │    │  WindowManager.LayoutParams参数          │               │
 * │    ├─────────────────────────────────────────┤               │
 * │    │  height = MATCH_PARENT                   │               │
 * │    │    - 全屏高度                            │               │
 * │    │  width = MATCH_PARENT                    │               │
 * │    │    - 全屏宽度                            │               │
 * │    │  format = RGBA_8888                      │               │
 * │    │    - 32位像素格式，支持透明度             │               │
 * │    │  systemUiVisibility =                    │               │
 * │    │    HIDE_NAVIGATION | IMMERSIVE           │               │
 * │    │    - 隐藏导航栏，沉浸式模式               │               │
 * │    │  flags = FULLSCREEN | LAYOUT_IN_SCREEN   │               │
 * │    │    - 全屏模式，布局在屏幕内               │               │
 * │    │  gravity = CENTER                        │               │
 * │    │    - 居中显示                            │               │
 * │    │  type = paramsType                       │               │
 * │    │    - 窗口类型（由FirstActivity传入）      │               │
 * │    │      - TYPE_APPLICATION (普通应用窗口)   │               │
 * │    │      - TYPE_APPLICATION_OVERLAY (悬浮窗) │               │
 * │    └─────────────────────────────────────────┘               │
 * │                                                               │
 * │  悬浮窗权限说明：                                               │
 * │    ┌─────────────────────────────────────────┐               │
 * │    │  Android 6.0+ 需要悬浮窗权限              │               │
 * │    ├─────────────────────────────────────────┤               │
 * │    │  权限名称：SYSTEM_ALERT_WINDOW            │               │
 * │    │  检查方法：Settings.canDrawOverlays()    │               │
 * │    │  请求方法：Settings.ACTION_MANAGE_OVERLAY_PERMISSION │ │
 * │    │  窗口类型：TYPE_APPLICATION_OVERLAY      │               │
 * │    │    - Android 8.0+ 必须使用此类型         │               │
 * │    │  窗口类型：TYPE_PHONE (已废弃)           │               │
 * │    │    - Android 8.0以下可以使用             │               │
 * │    └─────────────────────────────────────────┘               │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>应用启动</b>：在FirstActivity中显示启动画面，提升用户体验</li>
 *   <li><b>初始化等待</b>：在MainActivity初始化期间显示启动画面，避免空白界面</li>
 *   <li><b>品牌展示</b>：通过启动动画展示品牌形象</li>
 *   <li><b>加载提示</b>：提示用户应用正在加载中</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link WindowManager} - Android窗口管理器，用于添加/移除悬浮窗</li>
 *   <li>{@link SplashScreenSurfaceView} - 启动画面SurfaceView，用于播放启动动画</li>
 *   <li>{@link com.micsig.tbook.tbookscope.first.FirstActivity} - 启动引导Activity，调用showDialog()显示启动画面</li>
 *   <li>{@link MainActivity} - 主Activity，调用closeDialog()关闭启动画面</li>
 *   <li>{@link com.micsig.tbook.tbookscope.util.App} - 应用工具类，提供应用上下文</li>
 *   <li>{@link com.micsig.base.Logger} - 日志工具类，用于调试日志输出</li>
 * </ul>
 * 
 * <h3>设计模式</h3>
 * <p>本类采用单例模式（Singleton Pattern），确保全局只有一个启动画面对话框实例。
 * 使用懒汉式实现方式，在第一次调用get()方法时创建实例。</p>
 * 
 * <h3>注意事项</h3>
 * <ul>
 *   <li><b>悬浮窗权限</b>：Android 6.0+需要悬浮窗权限才能显示启动画面</li>
 *   <li><b>窗口类型</b>：Android 8.0+必须使用TYPE_APPLICATION_OVERLAY窗口类型</li>
 *   <li><b>线程安全</b>：showDialog()和closeDialog()方法使用synchronized关键字，确保线程安全</li>
 *   <li><b>状态管理</b>：isFirst标志用于管理启动画面的显示状态，避免重复关闭</li>
 *   <li><b>动画停止</b>：关闭启动画面时必须调用splash.stop()停止动画，释放资源</li>
 *   <li><b>视图移除</b>：使用removeViewImmediate()立即移除视图，避免延迟</li>
 * </ul>
 * 
 * @see WindowManager
 * @see SplashScreenSurfaceView
 * @see com.micsig.tbook.tbookscope.first.FirstActivity
 * @see MainActivity
 * @see OnShowStartListener
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class SplashDialog {   // 启动画面对话框类：应用启动时显示的欢迎画面（Splash Screen）

    /**
     * 日志标签 - 用于调试日志输出
     * 
     * <p>TAG常量，用于Logger日志输出，标识日志来源为SplashDialog类。</p>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>在closeDialog()方法中输出调试日志</li>
     *   <li>标识日志来源，便于调试和问题排查</li>
     * </ul>
     */
    private static final String TAG = "SplashDialog";   // 日志标签：用于调试日志输出，标识日志来源为SplashDialog类
    
    //region 单例模式实现
    /**
     * 单例实例 - 全局唯一的启动画面对话框实例
     * 
     * <p>splashDialog静态变量，保存SplashDialog的单例实例。
     * 使用懒汉式实现方式，在第一次调用get()方法时创建实例。</p>
     * 
     * <h4>实现方式</h4>
     * <ul>
     *   <li><b>懒汉式</b>：延迟加载，在第一次使用时创建实例</li>
     *   <li><b>线程不安全</b>：未使用同步机制，多线程环境下可能创建多个实例</li>
     *   <li><b>适用场景</b>：单线程环境，或确保只在单线程中调用get()方法</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>FirstActivity调用SplashDialog.get()获取实例</li>
     *   <li>MainActivity调用SplashDialog.get()获取实例</li>
     * </ul>
     */
    private static SplashDialog splashDialog;   // 单例实例：全局唯一的启动画面对话框实例，使用懒汉式实现

    /**
     * 获取单例实例 - 获取全局唯一的启动画面对话框实例
     * 
     * <p>获取SplashDialog的单例实例。使用懒汉式实现方式，
     * 在第一次调用get()方法时创建实例，后续调用返回已创建的实例。</p>
     * 
     * <h4>实现逻辑</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  获取单例实例流程                        │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  Step 1: 检查实例是否已创建              │
     * │    └─ if (splashDialog == null)         │
     * │                                         │
     * │  Step 2: 如果未创建，创建新实例          │
     * │    └─ splashDialog = new SplashDialog() │
     * │                                         │
     * │  Step 3: 返回实例                        │
     * │    └─ return splashDialog               │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>FirstActivity调用SplashDialog.get().showDialog()显示启动画面</li>
     *   <li>MainActivity调用SplashDialog.get().closeDialog()关闭启动画面</li>
     *   <li>FirstActivity调用SplashDialog.get().isVisible()检查启动画面是否可见</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * SplashDialog splashDialog = SplashDialog.get();
     * splashDialog.showDialog(paramsType, onShowStartListener);
     * </pre>
     * 
     * <h4>注意事项</h4>
     * <ul>
     *   <li><b>线程不安全</b>：未使用同步机制，多线程环境下可能创建多个实例</li>
     *   <li><b>延迟加载</b>：在第一次使用时创建实例，节省资源</li>
     * </ul>
     * 
     * @return SplashDialog单例实例
     */
    public static SplashDialog get() {   // 方法：获取单例实例，使用懒汉式实现
        if (splashDialog == null) {   // 判断：实例是否已创建
            splashDialog = new SplashDialog();   // 创建新实例：如果未创建，创建新的SplashDialog实例
        }   // if判断结束
        return splashDialog;   // 返回实例：返回SplashDialog单例实例
    }   // get方法结束

    /**
     * 私有构造函数 - 防止外部直接创建实例
     * 
     * <p>私有构造函数，防止外部直接创建SplashDialog实例，
     * 确保只能通过get()方法获取单例实例。</p>
     * 
     * <h4>设计目的</h4>
     * <ul>
     *   <li><b>单例模式</b>：确保全局只有一个SplashDialog实例</li>
     *   <li><b>私有构造</b>：防止外部直接创建实例，破坏单例模式</li>
     *   <li><b>延迟加载</b>：在第一次使用时创建实例，节省资源</li>
     * </ul>
     */
    private SplashDialog() {   // 私有构造函数：防止外部直接创建实例，确保单例模式

    }   // 构造函数结束
    //endregion

    /**
     * 显示开始监听器 - 启动画面显示完成后的回调接口
     * 
     * <p>OnShowStartListener接口，定义启动画面显示完成后的回调方法。
     * 在showDialog()方法中注册监听器，启动画面显示完成后调用onShowStart()方法，
     * 通常用于启动MainActivity。</p>
     * 
     * <h4>接口方法</h4>
     * <table border="1">
     *   <tr><th>方法</th><th>说明</th></tr>
     *   <tr><td>onShowStart()</td><td>启动画面显示完成后调用，通常用于启动MainActivity</td></tr>
     * </table>
     * 
     * <h4>实现示例</h4>
     * <pre>
     * OnShowStartListener onShowStartListener = new OnShowStartListener() {
     *     @Override
     *     public void onShowStart() {
     *         // 启动MainActivity
     *         Intent intent = new Intent(FirstActivity.this, MainActivity.class);
     *         startActivity(intent);
     *     }
     * };
     * SplashDialog.get().showDialog(paramsType, onShowStartListener);
     * </pre>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>FirstActivity实现监听器接口，在onShowStart()中启动MainActivity</li>
     *   <li>启动画面显示完成后，通知FirstActivity启动主界面</li>
     * </ul>
     * 
     * @see SplashDialog#showDialog(int, OnShowStartListener)
     */
    private OnShowStartListener onShowStartListener;   // 显示开始监听器：启动画面显示完成后的回调接口

    /**
     * 显示开始监听器接口 - 定义启动画面显示完成后的回调方法
     * 
     * <p>OnShowStartListener接口，定义启动画面显示完成后的回调方法。
     * 在showDialog()方法中注册监听器，启动画面显示完成后调用onShowStart()方法。</p>
     * 
     * <h4>接口方法</h4>
     * <table border="1">
     *   <tr><th>方法</th><th>说明</th></tr>
     *   <tr><td>onShowStart()</td><td>启动画面显示完成后调用，通常用于启动MainActivity</td></tr>
     * </table>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>FirstActivity实现监听器接口，在onShowStart()中启动MainActivity</li>
     *   <li>启动画面显示完成后，通知FirstActivity启动主界面</li>
     * </ul>
     */
    public interface OnShowStartListener {   // 显示开始监听器接口：定义启动画面显示完成后的回调方法
        
        /**
         * 启动画面显示完成回调 - 启动画面显示完成后调用
         * 
         * <p>启动画面显示完成后调用此方法。通常用于启动MainActivity，
         * 开始主界面的初始化工作。</p>
         * 
         * <h4>调用时机</h4>
         * <ul>
         *   <li>在showDialog()方法中，启动画面显示完成后调用</li>
         *   <li>在WindowManager.addView()完成后调用</li>
         * </ul>
         * 
         * <h4>实现示例</h4>
         * <pre>
         * @Override
         * public void onShowStart() {
         *     // 启动MainActivity
         *     Intent intent = new Intent(FirstActivity.this, MainActivity.class);
         *     startActivity(intent);
         * }
         * </pre>
         */
        void onShowStart();   // 方法：启动画面显示完成回调，通常用于启动MainActivity
    }   // OnShowStartListener接口结束

    /**
     * 窗口管理器 - 用于添加/移除悬浮窗
     * 
     * <p>WindowManager对象，用于管理启动画面的悬浮窗。
     * 在showDialog()方法中获取WindowManager服务，添加启动画面视图。
     * 在closeDialog()方法中移除启动画面视图。</p>
     * 
     * <h4>获取方式</h4>
     * <pre>
     * windowManager = (WindowManager) App.get().getSystemService(Context.WINDOW_SERVICE);
     * </pre>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>showDialog()方法中添加启动画面视图</li>
     *   <li>closeDialog()方法中移除启动画面视图</li>
     * </ul>
     */
    private WindowManager windowManager;   // 窗口管理器：用于添加/移除悬浮窗，管理启动画面的显示和关闭
    
    /**
     * 对话框视图 - 启动画面的布局视图
     * 
     * <p>View对象，保存启动画面的布局视图。
     * 在showDialog()方法中通过LayoutInflater加载dialog_splash布局。
     * 在closeDialog()方法中通过WindowManager移除视图。</p>
     * 
     * <h4>布局内容</h4>
     * <ul>
     *   <li>{@link SplashScreenSurfaceView} - 启动画面SurfaceView，播放启动动画</li>
     *   <li>其他布局元素 - 品牌Logo、加载提示等</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>showDialog()方法中加载布局</li>
     *   <li>closeDialog()方法中移除视图</li>
     * </ul>
     */
    private View dialogView;   // 对话框视图：启动画面的布局视图，包含启动动画和其他布局元素
    
    /**
     * 启动画面SurfaceView - 播放启动动画
     * 
     * <p>SplashScreenSurfaceView对象，用于播放启动动画。
     * 在showDialog()方法中从dialogView中获取splash控件。
     * 在closeDialog()方法中调用splash.stop()停止动画。</p>
     * 
     * <h4>功能说明</h4>
     * <ul>
     *   <li><b>动画播放</b>：播放启动动画，展示品牌形象</li>
     *   <li><b>SurfaceView</b>：使用SurfaceView实现高性能动画渲染</li>
     *   <li><b>资源释放</b>：关闭时调用stop()方法释放资源</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>showDialog()方法中获取splash控件</li>
     *   <li>closeDialog()方法中调用splash.stop()停止动画</li>
     * </ul>
     * 
     * @see SplashScreenSurfaceView
     */
    private SplashScreenSurfaceView splash;   // 启动画面SurfaceView：播放启动动画，展示品牌形象

    /**
     * 显示启动画面对话框 - 在应用启动时显示欢迎画面
     * 
     * <p>显示启动画面对话框，通过WindowManager实现悬浮窗效果。
     * 加载dialog_splash布局，设置窗口参数（全屏、隐藏导航栏、沉浸式），
     * 添加视图到WindowManager，调用onShowStartListener回调启动MainActivity。</p>
     * 
     * <h4>显示流程</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  显示启动画面流程                        │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  Step 1: 获取WindowManager服务          │
     * │    └─ App.get().getSystemService()      │
     * │                                         │
     * │  Step 2: 加载dialog_splash布局          │
     * │    └─ LayoutInflater.from().inflate()   │
     * │                                         │
     * │  Step 3: 获取splash控件                 │
     * │    └─ dialogView.findViewById(R.id.splash)│
     * │                                         │
     * │  Step 4: 创建WindowManager.LayoutParams │
     * │    └─ 设置窗口参数                      │
     * │      - height = MATCH_PARENT (全屏高度) │
     * │      - width = MATCH_PARENT (全屏宽度)  │
     * │      - format = RGBA_8888 (32位像素格式)│
     * │      - systemUiVisibility =             │
     * │        HIDE_NAVIGATION | IMMERSIVE      │
     * │      - flags = FULLSCREEN | LAYOUT_IN_SCREEN│
     * │      - gravity = CENTER (居中显示)      │
     * │      - type = paramsType (窗口类型)     │
     * │                                         │
     * │  Step 5: 添加视图到WindowManager        │
     * │    └─ windowManager.addView(dialogView, params)│
     * │                                         │
     * │  Step 6: 设置显示标志                   │
     * │    └─ isFirst = true                    │
     * │                                         │
     * │  Step 7: 调用回调方法                   │
     * │    └─ onShowStartListener.onShowStart() │
     * │    └─ 启动MainActivity                  │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>paramsType</td><td>int</td><td>窗口类型，由FirstActivity传入（TYPE_APPLICATION或TYPE_APPLICATION_OVERLAY）</td></tr>
     *   <tr><td>onShowStartListener</td><td>OnShowStartListener</td><td>显示开始监听器，启动画面显示完成后调用</td></tr>
     * </table>
     * 
     * <h4>窗口类型说明</h4>
     * <table border="1">
     *   <tr><th>窗口类型</th><th>说明</th><th>适用版本</th></tr>
     *   <tr><td>TYPE_APPLICATION</td><td>普通应用窗口</td><td>Android 8.0以下</td></tr>
     *   <tr><td>TYPE_APPLICATION_OVERLAY</td><td>悬浮窗窗口</td><td>Android 8.0+</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>FirstActivity中调用，显示启动画面</li>
     *   <li>在检查悬浮窗权限后调用</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * SplashDialog.get().showDialog(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, new OnShowStartListener() {
     *     @Override
     *     public void onShowStart() {
     *         // 启动MainActivity
     *         Intent intent = new Intent(FirstActivity.this, MainActivity.class);
     *         startActivity(intent);
     *     }
     * });
     * </pre>
     * 
     * <h4>注意事项</h4>
     * <ul>
     *   <li><b>线程安全</b>：使用synchronized关键字，确保线程安全</li>
     *   <li><b>悬浮窗权限</b>：Android 6.0+需要悬浮窗权限</li>
     *   <li><b>窗口类型</b>：Android 8.0+必须使用TYPE_APPLICATION_OVERLAY</li>
     * </ul>
     * 
     * @param paramsType 窗口类型（TYPE_APPLICATION或TYPE_APPLICATION_OVERLAY）
     * @param onShowStartListener 显示开始监听器，启动画面显示完成后调用
     * @see OnShowStartListener
     */
    public synchronized void showDialog(int paramsType, OnShowStartListener onShowStartListener) {   // 方法：显示启动画面对话框，使用synchronized确保线程安全
        windowManager = (WindowManager) App.get().getSystemService(Context.WINDOW_SERVICE);   // 获取WindowManager服务：通过App.get()获取应用上下文，再获取WINDOW_SERVICE系统服务
        dialogView = LayoutInflater.from(App.get()).inflate(R.layout.dialog_splash, null);   // 加载布局：通过LayoutInflater加载dialog_splash布局，创建启动画面视图
        splash = dialogView.findViewById(R.id.splash);   // 获取splash控件：从dialogView中获取SplashScreenSurfaceView控件，用于播放启动动画

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();   // 创建窗口参数：创建WindowManager.LayoutParams对象，用于设置窗口属性
        params.height = WindowManager.LayoutParams.MATCH_PARENT;   // 设置窗口高度：MATCH_PARENT表示全屏高度，覆盖整个屏幕
        params.width = WindowManager.LayoutParams.MATCH_PARENT;   // 设置窗口宽度：MATCH_PARENT表示全屏宽度，覆盖整个屏幕
        params.format = PixelFormat.RGBA_8888;   // 设置像素格式：RGBA_8888表示32位像素格式，支持透明度，适合动画显示
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;   // 设置系统UI可见性：隐藏导航栏，启用沉浸式模式，提供更好的用户体验
        params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;   // 设置窗口标志：全屏模式，布局在屏幕内，覆盖状态栏和导航栏
        params.gravity = Gravity.CENTER;   // 设置窗口位置：居中显示，启动画面在屏幕中央显示

        params.type = paramsType;   // 设置窗口类型：由FirstActivity传入，Android 8.0+使用TYPE_APPLICATION_OVERLAY，Android 8.0以下使用TYPE_APPLICATION

        windowManager.addView(dialogView, params);   // 添加视图到WindowManager：将启动画面视图添加到窗口管理器，显示启动画面
        isFirst = true;   // 设置显示标志：isFirst = true表示启动画面已显示，用于状态管理
        onShowStartListener.onShowStart();   // 调用回调方法：通知FirstActivity启动画面已显示完成，通常用于启动MainActivity
    }   // showDialog方法结束

    /**
     * 是否首次显示标志 - 标识启动画面是否已显示
     * 
     * <p>isFirst布尔变量，标识启动画面是否已显示。
     * 在showDialog()方法中设置为true，表示启动画面已显示。
     * 在closeDialog()方法中设置为false，表示启动画面已关闭。</p>
     * 
     * <h4>状态说明</h4>
     * <table border="1">
     *   <tr><th>状态</th><th>值</th><th>说明</th></tr>
     *   <tr><td>未显示</td><td>false</td><td>启动画面未显示或已关闭</td></tr>
     *   <tr><td>已显示</td><td>true</td><td>启动画面正在显示</td></tr>
     * </table>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>showDialog()方法中设置为true</li>
     *   <li>closeDialog()方法中检查并设置为false</li>
     *   <li>isVisible()方法中返回状态</li>
     * </ul>
     */
    private boolean isFirst = false;   // 是否首次显示标志：标识启动画面是否已显示，true=已显示，false=未显示或已关闭

    /**
     * 检查启动画面是否可见 - 检查启动画面是否正在显示
     * 
     * <p>检查启动画面是否正在显示。返回isFirst标志的值，
     * true表示启动画面正在显示，false表示启动画面已关闭。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 启动画面正在显示</li>
     *   <li><b>false</b>: 启动画面已关闭</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>FirstActivity中调用，检查启动画面是否已显示</li>
     *   <li>避免重复显示启动画面</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * if (!SplashDialog.get().isVisible()) {
     *     SplashDialog.get().showDialog(paramsType, onShowStartListener);
     * }
     * </pre>
     * 
     * @return 是否可见，true=正在显示，false=已关闭
     */
    public boolean isVisible() {   // 方法：检查启动画面是否可见
        return isFirst;   // 返回isFirst标志：true表示启动画面正在显示，false表示启动画面已关闭
    }   // isVisible方法结束

    /**
     * 关闭启动画面对话框 - 移除启动画面视图，释放资源
     * 
     * <p>关闭启动画面对话框，移除启动画面视图，释放资源。
     * 检查isFirst标志，如果为true则关闭启动画面：
     * 设置isFirst为false，调用splash.stop()停止动画，
     * 调用windowManager.removeViewImmediate()移除视图。</p>
     * 
     * <h4>关闭流程</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  关闭启动画面流程                        │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  Step 1: 输出调试日志                    │
     * │    └─ Logger.d(TAG, "closeDialog:       │
     * │        isFirst = " + isFirst)           │
     * │                                         │
     * │  Step 2: 检查isFirst标志                 │
     * │    └─ if (isFirst)                      │
     * │                                         │
     * │  Step 3: 设置关闭标志                    │
     * │    └─ isFirst = false                   │
     * │                                         │
     * │  Step 4: 检查windowManager是否有效       │
     * │    └─ if (windowManager != null)        │
     * │                                         │
     * │  Step 5: 停止启动动画                    │
     * │    └─ splash.stop()                     │
     * │    └─ 释放动画资源                      │
     * │                                         │
     * │  Step 6: 移除视图                        │
     * │    └─ windowManager.removeViewImmediate │
     * │        (dialogView)                     │
     * │    └─ 立即移除视图，避免延迟            │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>MainActivity中调用，关闭启动画面</li>
     *   <li>MainActivity初始化完成后调用</li>
     *   <li>Handler消息处理中调用（MSG_SPLASH消息）</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * SplashDialog.get().closeDialog();
     * </pre>
     * 
     * <h4>注意事项</h4>
     * <ul>
     *   <li><b>线程安全</b>：使用synchronized关键字，确保线程安全</li>
     *   <li><b>状态检查</b>：检查isFirst标志，避免重复关闭</li>
     *   <li><b>动画停止</b>：必须调用splash.stop()停止动画，释放资源</li>
     *   <li><b>立即移除</b>：使用removeViewImmediate()立即移除视图，避免延迟</li>
     * </ul>
     */
    public synchronized void closeDialog() {   // 方法：关闭启动画面对话框，使用synchronized确保线程安全
        Logger.d(TAG, "closeDialog: isFirst = " + isFirst);   // 输出调试日志：记录关闭启动画面的调用和isFirst状态，便于调试和问题排查
        if (isFirst) {   // 判断：检查isFirst标志，是否需要关闭启动画面
            isFirst = false;   // 设置关闭标志：isFirst = false表示启动画面已关闭，避免重复关闭
            if (windowManager != null) {   // 判断：检查windowManager是否有效，避免空指针异常
                splash.stop();   // 停止启动动画：调用splash.stop()停止动画播放，释放动画资源
//                dialogView.setVisibility(View.GONE);   // 注释代码：设置视图不可见（已废弃，使用removeViewImmediate代替）
//                windowManager.removeView(dialogView);   // 注释代码：移除视图（已废弃，使用removeViewImmediate代替）
                windowManager.removeViewImmediate(dialogView);   // 立即移除视图：调用removeViewImmediate()立即移除启动画面视图，避免延迟，释放窗口资源
            }   // windowManager判断结束
        }   // isFirst判断结束
    }   // closeDialog方法结束
}   // SplashDialog类结束