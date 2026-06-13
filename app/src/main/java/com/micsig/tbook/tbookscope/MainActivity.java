package com.micsig.tbook.tbookscope; // 示波器主应用包名

// ==================== Android SDK 导入 ====================
import android.content.Intent; // Intent意图，用于Activity间通信和广播
import android.graphics.Bitmap; // 位图，用于指针图标创建
import android.graphics.Canvas; // 画布，用于绘制指针图标
import android.graphics.drawable.Drawable; // 可绘制对象，用于加载图标资源
import android.os.Build; // 系统版本信息
import android.os.Bundle; // Bundle，用于保存和恢复Activity状态
import android.os.Environment; // 环境变量，用于获取存储路径
import android.os.Handler; // Handler，用于线程间消息通信
import android.os.Looper; // Looper，消息循环器
import android.os.Message; // Message，消息对象
import android.os.MessageQueue; // MessageQueue，消息队列
import android.util.Log; // Log，日志输出工具
import android.view.InputDevice; // InputDevice，输入设备信息
import android.view.KeyEvent; // KeyEvent，按键事件
import android.view.MotionEvent; // MotionEvent，触摸和运动事件
import android.view.PointerIcon; // PointerIcon，鼠标指针图标
import android.view.TextureView; // TextureView，硬件加速的纹理视图，用于波形绘制
import android.view.View; // View，视图基类
import android.view.Window; // Window，窗口对象
import android.view.WindowManager; // WindowManager，窗口管理器

// ==================== AndroidX 支持库导入 ====================
import androidx.annotation.NonNull; // NonNull注解，用于参数非空检查
import androidx.appcompat.app.AppCompatActivity; // AppCompatActivity，兼容性Activity基类

// ==================== 基础模块导入 ====================
import com.micsig.base.keyevent.KeyEventUtil; // KeyEventUtil，按键事件工具类
import com.micsig.base.Logger; // Logger，日志工具类

// ==================== 硬件抽象层导入 ====================
import com.micsig.tbook.hardware.HwManager; // HwManager，硬件管理器（LED、截图参数等）

// ==================== 示波器核心模块导入 ====================
import com.micsig.tbook.scope.Data.AutoSave; // AutoSave，自动保存功能
import com.micsig.tbook.scope.Data.SaveBin; // SaveBin，二进制数据保存
import com.micsig.tbook.scope.Display.Display; // Display，显示模式管理（YT/XY）
import com.micsig.tbook.scope.Display.DisplayAction; // DisplayAction，显示动作标志
import com.micsig.tbook.scope.Scope; // Scope，示波器核心实例
import com.micsig.tbook.scope.ScopeBase; // ScopeBase，示波器基础参数
import com.micsig.tbook.scope.ScopeMessage; // ScopeMessage，示波器消息处理
import com.micsig.tbook.scope.channel.Channel; // Channel，通道基类
import com.micsig.tbook.scope.channel.ChannelFactory; // ChannelFactory，通道工厂（创建CH/Math/Ref通道）
import com.micsig.tbook.scope.channel.MathChannel; // MathChannel，数学运算通道
import com.micsig.tbook.scope.channel.RefChannel; // RefChannel，参考波形通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // HorizontalAxis，水平轴（时基）管理
import com.micsig.tbook.scope.surface.PreviewTextureView; // PreviewTextureView，波形预览纹理视图
import com.micsig.tbook.scope.vertical.VerticalAxis; // VerticalAxis，垂直轴（电压档位）管理

// ==================== 主界面UI组件导入 ====================
import com.micsig.tbook.tbookscope.broadcastreceiver.BroadcastManager; // BroadcastManager，广播管理器
import com.micsig.tbook.tbookscope.main.StatusBar; // StatusBar，状态栏（USB/WiFi/电池/时间）
import com.micsig.tbook.tbookscope.main.dialog.DialogManage; // DialogManage，对话框管理器
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel; // DialogOkCancel，确认取消对话框
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom; // MainHolderBottom，底部控制栏持有者
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottomQuick; // MainHolderBottomQuick，底部快捷控制栏
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomOtherChannels; // MainBottomOtherChannels，其他通道面板
import com.micsig.tbook.tbookscope.main.maincenter.MainHolderLeftMenu; // MainHolderLeftMenu，左侧菜单持有者
import com.micsig.tbook.tbookscope.main.maincenter.MainLeftMsgMenuRunStop; // MainLeftMsgMenuRunStop，左侧Run/Stop消息
import com.micsig.tbook.tbookscope.main.mainright.MainHolderRightChannels; // MainHolderRightChannels，右侧通道面板持有者
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel; // MainHolderTriggerLevel，触发电平控制持有者

// ==================== 命令中间件导入 ====================
import com.micsig.tbook.tbookscope.middleware.command.Command; // Command，命令系统入口
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // CommandMsgToUI，命令消息转UI
import com.micsig.tbook.tbookscope.middleware.command.Command_Display; // Command_Display，显示命令

// ==================== RxJava事件总线导入 ====================
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxBus，RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister; // RxBusRegister，RxBus注册管理器
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxEnum，事件类型枚举

// ==================== SCPI远程控制导入 ====================
import com.micsig.tbook.tbookscope.scpi.SCPICommandDeal; // SCPICommandDeal，SCPI命令处理器

// ==================== 外部按键服务导入 ====================
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysOnBindService; // ExternalKeysOnBindService，外部按键绑定服务
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol; // ExternalKeysProtocol，外部按键协议
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ExternalKeysManager; // ExternalKeysManager，多功能旋钮管理器

// ==================== SCPI服务导入 ====================
import com.micsig.tbook.tbookscope.services.SCPI.client.ScpiOnBindService; // ScpiOnBindService，SCPI绑定服务

// ==================== 数据结构导入 ====================
import com.micsig.tbook.tbookscope.struct.ExternalKeysMsg_ToMCU; // ExternalKeysMsg_ToMCU，外部按键消息（发送到MCU）
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand; // ExternalKeysCommand，外部按键命令
import com.micsig.tbook.tbookscope.structdata.ExternalKeysUI; // ExternalKeysUI，外部按键UI

// ==================== 工具类导入 ====================
import com.micsig.tbook.tbookscope.tools.FileUtils; // FileUtils，文件工具类
import com.micsig.tbook.tbookscope.tools.PlaySound; // PlaySound，声音播放工具
import com.micsig.tbook.tbookscope.tools.RecoveryManage; // RecoveryManage，恢复管理器
import com.micsig.tbook.tbookscope.tools.SaveManage; // SaveManage，保存管理器
import com.micsig.tbook.tbookscope.tools.ScreenControls; // ScreenControls，屏幕控制（锁屏等）
import com.micsig.tbook.tbookscope.tools.Tools; // Tools，通用工具类

// ==================== UI组件导入 ====================
import com.micsig.tbook.tbookscope.top.popwindow.TopLayoutPopWindow; // TopLayoutPopWindow，顶部弹出菜单
import com.micsig.tbook.tbookscope.util.App; // App，应用全局类
import com.micsig.tbook.tbookscope.util.CacheUtil; // CacheUtil，缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // DToast，自定义Toast提示

// ==================== 波形显示区域模块导入 ====================
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // IWorkMode，工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // WorkModeManage，工作模式管理（YT/XY/Zoom）
import com.micsig.tbook.tbookscope.wavezone.bean.YTZoomMsgDisplay; // YTZoomMsgDisplay，YT Zoom显示消息
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // CursorManage，光标管理器
import com.micsig.tbook.tbookscope.wavezone.display.RulerManage; // RulerManage，标尺管理器
import com.micsig.tbook.tbookscope.wavezone.display.WaveGridManage; // WaveGridManage，波形网格管理器
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // MeasureManage，测量管理器
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase; // TriggerTimebase，触发时基
import com.micsig.tbook.tbookscope.wavezone.trigger.VoltageLineManage; // VoltageLineManage，电压线管理器
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage; // SerialBusManage，串行总线管理器
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // WaveManage，波形管理器

// ==================== UI控件导入 ====================
import com.micsig.tbook.ui.MTriggerStateBar; // MTriggerStateBar，触发状态栏控件
import com.micsig.tbook.ui.MTriggerTime; // MTriggerTime，触发时间滑块控件
import com.micsig.tbook.ui.util.StrUtil; // StrUtil，字符串工具类
import com.micsig.tbook.ui.util.TBookUtil; // TBookUtil，TBook工具类
import com.micsig.tbook.ui.util.svg.SvgNodeInfo; // SvgNodeInfo，SVG节点信息（通道颜色）
import com.micsig.tbook.ui.wavezone.TChan; // TChan，通道枚举工具

// ==================== 第三方库导入 ====================
import com.molihuan.pathselector.utils.BundleUtil; // BundleUtil，Bundle工具类

// ==================== Java标准库导入 ====================
import java.io.File; // File，文件操作
import java.util.ArrayList; // ArrayList，动态数组列表
import java.util.HashMap; // HashMap，哈希映射
import java.util.List; // List，列表接口

/**
 * 示波器主界面Activity
 * 
 * <p><b>所属模块：</b>app/tbookscope - 应用层主界面模块
 * 
 * <p><b>模块职责：</b>
 * <ul>
 *   <li>作为示波器应用的主入口Activity</li>
 *   <li>负责主界面的初始化、布局管理和生命周期控制</li>
 *   <li>协调各子模块（波形显示、通道控制、触发设置等）的交互</li>
 *   <li>处理用户输入事件（触摸、滚轮、按键）</li>
 *   <li>管理配置参数的加载和恢复</li>
 * </ul>
 * 
 * <p><b>核心依赖：</b>
 * <ul>
 *   <li>scope模块：核心示波器功能（Scope、ChannelFactory、HorizontalAxis等）</li>
 *   <li>wavezone模块：波形显示区域管理（WaveManage、WorkModeManage等）</li>
 *   <li>middleware模块：命令中间件（Command、SCPI命令处理）</li>
 *   <li>services模块：外部按键服务、SCPI远程控制服务</li>
 *   <li>main子模块：状态栏、底部栏、右侧面板、左侧菜单等UI组件</li>
 * </ul>
 * 
 * <p><b>界面布局结构：</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │                      StatusBar (状态栏)                      │
 * │   [USB连接] [U盘] [WiFi] [有线网络] [电池] [时间]            │
 * ├─────────────────────────────────────────────────────────────┤
 * │                  TopLayoutPopWindow (顶部快捷菜单)           │
 * │   [Auto] [Single] [Run/Stop] [Zoom] [Measure] [Trigger]     │
 * ├────────┬────────────────────────────────────┬───────────────┤
 * │        │                                    │               │
 * │ 左侧   │         波形显示区域                │   右侧通道    │
 * │ 菜单   │    (TextureView + PreviewTexture)  │   控制面板    │
 * │        │         YT/XY/Zoom模式             │  [CH1-CH8]    │
 * │RunStop │                                    │  [Math1-4]    │
 * │ 按钮   │                                    │  [Ref1-8]     │
 * │        │                                    │  触发电平线    │
 * ├────────┴────────────────────────────────────┴───────────────┤
 * │                  MainHolderBottom (底部控制栏)               │
 * │   [时基选择] [通道列表] [测量项] [菜单] [其他通道]           │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>生命周期流程：</b>
 * <pre>
 * onCreate() → 初始化窗口、创建视图、绑定服务、初始化单例、加载配置
 *     ↓
 * onResume() → 恢复示波器运行、启用按键处理、刷新波形、更新LED状态
 *     ↓
 * [运行状态] → 处理用户交互、波形显示、测量计算
 *     ↓
 * onPause() → 暂停示波器、禁用按键、保存状态
 *     ↓
 * onDestroy() → 待机模式、解绑服务、释放资源
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>应用启动后进入主界面</li>
 *   <li>用户进行波形观测、参数调整、测量分析等操作</li>
 *   <li>接收外部按键和SCPI远程控制命令</li>
 *   <li>截图保存、自动保存、配置恢复等功能</li>
 * </ul>
 * 
 * <p><b>关键功能点：</b>
 * <ul>
 *   <li>全屏沉浸式显示，保持屏幕常亮</li>
 *   <li>异步加载配置参数，避免阻塞UI线程</li>
 *   <li>滚轮缩放时基，触摸事件分发</li>
 *   <li>截图功能支持时间戳、反色、缩略图选项</li>
 *   <li>Zoom模式、XY模式、YT模式切换</li>
 *   <li>通道颜色、位置、零点等配置恢复</li>
 * </ul>
 * 
 * @author Micsig Development Team
 * @version 2.0
 * @since 2023-10-07
 */
public class MainActivity extends AppCompatActivity {
    
    /**
     * 日志标签
     */
    private static final String TAG = "MainActivity";

    /**
     * Handler消息：启动完成，关闭Splash界面
     */
    public static final int MSG_SPLASH = 0xaa;
    
    /**
     * Handler消息：重绘参考波形
     */
    public static final int MSG_REDRAWREF = 0x01;
    
    /**
     * Handler消息：刷新波形显示
     */
    public static final int MSG_REFRESH_WAVE = 0x02;
    
    /**
     * Handler消息：保存截图
     */
    public static final int MSG_SAVE_PICTURE = 0x03;

    /**
     * 禁用展开标志（用于系统UI控制）
     */
    public static final int DISABLE_EXPAND = 0x00010000;
    
    /**
     * 无禁用标志（用于系统UI控制）
     */
    public static final int DISABLE_NONE = 0x00000000;

    /**
     * 按键码：Run/Stop按键
     */
    private static final int KEYCODE_RUNSTOP = 121;
    
    /**
     * 按键码：Single按键
     */
    private static final int KEYCODE_SINGLE = 0;
    
    /**
     * 按键码：Auto按键
     */
    private static final int KEYCODE_AUTO = 131;
    
    /**
     * 按键码：Half按键
     */
    private static final int KEYCODE_HALF = 132;
    
    /**
     * 按键码：Measure按键
     */
    private static final int KEYCODE_MEASURE = 82;
    
    /**
     * 按键码：Trigger按键
     */
    private static final int KEYCODE_TRIGGER = 122;
    
    /**
     * 按键码：Home按键
     */
    private static final int KEYCODE_HOME = 4;

    /**
     * 触发状态栏控件（右上角触发信息显示）
     */
    private MTriggerStateBar detail;
    
    /**
     * 触发时间滑块控件（底部时基调节）
     */
    private MTriggerTime slider;
    
    /**
     * 主视图容器，包含所有子视图
     */
    private MainViewGroup mainViewGroup;
    
    /**
     * RxBus事件注册管理器
     */
    private RxBusRegister rxBusRegister = null;
    
    /**
     * 底部控制栏持有者（时基、通道列表、测量项等）
     */
    private MainHolderBottom mainHolderBottom;
    
    /**
     * 状态栏（USB、WiFi、电池、时间等状态显示）
     */
    private StatusBar statusBar;

    /**
     * 顶部快捷菜单弹出窗口
     */
    private TopLayoutPopWindow topSlipMenuBar_Quick;

    /**
     * 波形预览TextureView包装器
     */
    private PreviewTextureView previewTextureView;

    /**
     * 外部按键绑定服务（与MCU通信）
     */
    private ExternalKeysOnBindService keyService;
    
    /**
     * SCPI远程控制绑定服务
     */
    private ScpiOnBindService scpiService;

    /**
     * 示波器核心实例
     */
    private Scope mScope;
    
    /**
     * 主线程Handler，用于处理异步消息
     */
    private MainHandler mainHandler = new MainHandler();
    
    /**
     * 波形绘制TextureView
     */
    private TextureView tv;

    /**
     * 设置鼠标指针图标
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从资源文件加载指针图标</li>
     *   <li>转换为Bitmap并创建PointerIcon对象</li>
     *   <li>设置到窗口DecorView上</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>在onCreate()中调用，用于自定义鼠标指针外观
     */
    private void setPointerIcon() {
        Drawable drawable = getDrawable(R.drawable.pointer_icon); // 从资源文件加载指针图标
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888); // 创建ARGB_8888格式的位图
        Canvas canvas = new Canvas(bitmap); // 创建画布，绑定到位图
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight()); // 设置可绘制对象的边界
        drawable.draw(canvas); // 将图标绘制到画布上
        float v = App.get().getResources().getDimension(R.dimen.dp_1); // 获取1dp的像素值作为热点偏移
        PointerIcon pointerIcon = PointerIcon.create(bitmap, v, v); // 创建PointerIcon对象，设置热点位置
        getWindow().getDecorView().setPointerIcon(pointerIcon); // 设置窗口的指针图标
    }

    /**
     * Activity创建回调
     * 
     * <p><b>初始化流程：</b>
     * <ol>
     *   <li>获取Scope实例和硬件信息</li>
     *   <li>设置全屏沉浸式窗口</li>
     *   <li>创建MainViewGroup并设置为内容视图</li>
     *   <li>绑定外部按键服务和SCPI服务</li>
     *   <li>初始化所有单例组件</li>
     *   <li>初始化波形显示区域</li>
     *   <li>加载配置参数</li>
     *   <li>初始化波形绘制TextureView</li>
     *   <li>初始化状态栏</li>
     * </ol>
     * 
     * @param savedInstanceState 保存的实例状态，用于Activity重建时恢复
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 获取示波器实例和硬件信息
        mScope = Scope.getInstance(); // 获取Scope单例实例
        mScope.getChNum(); // 获取通道数量
        mScope.getHwVersion(); // 获取硬件版本信息
        rxBusRegister = new RxBusRegister(); // 创建RxBus注册管理器
        
        // 设置无标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 请求无标题栏窗口特性
        Window window = getWindow(); // 获取窗口对象

        // 设置全屏和保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 添加全屏标志
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 添加保持屏幕常亮标志
        WindowManager.LayoutParams params = window.getAttributes(); // 获取窗口属性
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE; // 设置系统UI可见性：隐藏导航栏、沉浸式模式
        window.setAttributes(params); // 应用窗口属性
        super.onCreate(savedInstanceState); // 调用父类onCreate方法

        // 创建主视图容器并设置为内容视图
        mainViewGroup = new MainViewGroup(MainActivity.this); // 创建MainViewGroup实例
        setContentView(mainViewGroup); // 设置为Activity的内容视图

        // 绑定外部按键服务（与MCU通信）
        keyService = new ExternalKeysOnBindService(MainActivity.this, mainViewGroup); // 创建外部按键绑定服务
        keyService.bind(); // 绑定服务
        keyService.isExistServices(); // 检查服务是否存在
        keyService.setDealKeys(true); // 设置处理按键标志
        
        // 绑定SCPI远程控制服务
        scpiService = new ScpiOnBindService(MainActivity.this, mainViewGroup); // 创建SCPI绑定服务
        scpiService.bind(); // 绑定服务

        // 初始化所有单例组件
        initSington(); // 调用单例初始化方法

        // 设置沉浸式粘性模式
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE // 设置低调模式（状态栏变暗）
                | View.SYSTEM_UI_FLAG_FULLSCREEN // 全屏模式
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE // 布局稳定
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // 沉浸式粘性模式（用户交互后自动恢复）
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // 布局时隐藏导航栏
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); // 隐藏导航栏

        // 初始化波形显示区域（YT/XY/Zoom模式）
        WorkModeManage.getInstance().initWaveZoneDisplay(MainActivity.this, mainViewGroup); // 初始化工作模式管理器

        getWindow().getDecorView(); // 获取DecorView（无实际作用，可能是遗留代码）
        // 初始化外部按键UI和命令处理
        ExternalKeysUI.getInstance().setMainViewGroup(mainViewGroup); // 设置外部按键UI的主视图容器
        ExternalKeysCommand.get().init(this, mainViewGroup); // 初始化外部按键命令处理器
        initParam(); // 初始化默认参数
        loadParam(savedInstanceState); // 加载配置参数
        setPointerIcon(); // 设置鼠标指针图标

        // 打印最大可用内存（调试用）
        Runtime rt = Runtime.getRuntime(); // 获取Runtime实例
        long maxMemory = rt.maxMemory(); // 获取最大可用内存
        Log.i("maxMemory:", Long.toString(maxMemory / (1024 * 1024))); // 打印最大内存（MB单位）

        // 播放启动音效
        PlaySound.getInstance().playStartUp(); // 播放启动音效
        
        // 初始化波形绘制TextureView
        tv = (TextureView) findViewById(R.id.textureView); // 查找TextureView控件
        tv.setRotationX(180);  // 垂直翻转（X轴旋转180度）
        previewTextureView = new PreviewTextureView(getBaseContext(), tv); // 创建波形预览纹理视图包装器
        WaveManage.get().setWaveTextureView(previewTextureView); // 设置波形管理器的纹理视图

        // 初始化触发状态栏和时基滑块
        detail = (MTriggerStateBar) findViewById(R.id.maintopright_detail); // 查找触发状态栏控件
        slider = (MTriggerTime) findViewById(R.id.slider); // 查找触发时间滑块控件
        
        // 初始化状态栏
        statusBar=new StatusBar(MainActivity.this); // 创建状态栏实例
        statusBar.setBroadcastReceiver(MainActivity.this); // 设置广播接收器
        Log.d(TAG, "onCreate()  called with: end"); // 打印日志：onCreate结束

        // 获取TextureView在窗口中的位置（用于滚轮缩放计算）
        int [] point = {0,0}; // 创建坐标数组
        tv.getLocationInWindow(point); // 获取TextureView在窗口中的位置
        previewTextureViewX = point[0]; // 保存X坐标
    }
    
    /**
     * TextureView在窗口中的X坐标
     * 用于滚轮缩放时计算相对位置
     */
    int previewTextureViewX = 0;
    
    /**
     * 底部快捷控制栏持有者
     */
    private MainHolderBottomQuick mMainHolderBottomQuick;
    
    /**
     * 加载界面参数和创建子组件
     * 
     * <p><b>初始化内容：</b>
     * <ul>
     *   <li>初始化MainViewGroup布局</li>
     *   <li>创建触发电平控制组件</li>
     *   <li>创建底部快捷控制栏</li>
     *   <li>创建底部主控制栏</li>
     *   <li>创建右侧通道面板</li>
     *   <li>创建左侧菜单</li>
     *   <li>创建其他通道组件</li>
     *   <li>创建顶部快捷菜单</li>
     *   <li>注册空闲处理器异步加载配置</li>
     * </ul>
     * 
     * @param savedInstanceState 保存的实例状态
     */
    private void loadParam(Bundle savedInstanceState) {
        mainViewGroup.initLayout(); // 初始化MainViewGroup布局
        
        // 创建各个UI组件持有者
        new MainHolderTriggerLevel(mainViewGroup); // 创建触发电平控制组件持有者
        mMainHolderBottomQuick = new MainHolderBottomQuick(mainViewGroup, savedInstanceState); // 创建底部快捷控制栏持有者
        mMainHolderBottomQuick.setMainActivity(MainActivity.this); // 设置MainActivity引用
        mainHolderBottom = new MainHolderBottom(mainViewGroup, MainActivity.this); // 创建底部主控制栏持有者
        new MainHolderRightChannels(mainViewGroup); // 创建右侧通道面板持有者
        new MainHolderLeftMenu(mainViewGroup); // 创建左侧菜单持有者
        new MainBottomOtherChannels(mainViewGroup); // 创建其他通道组件持有者
        topSlipMenuBar_Quick = (TopLayoutPopWindow) mainViewGroup.findViewById(R.id.topSlipMenuBar_Quick); // 查找顶部快捷菜单弹出窗口
        topSlipMenuBar_Quick.setSavedInstanceState(savedInstanceState); // 设置保存的实例状态

        // 注册空闲处理器，在UI线程空闲时异步加载配置
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() { // 添加空闲处理器到消息队列
            @Override
            public boolean queueIdle() { // 空闲时回调
                Logger.i(TAG,"loadparam"); // 打印日志：开始加载参数
                try {
                    // 禁用命令，设置为运行状态
                    mScope.enableCommand(false); // 禁用命令处理
                    mScope.setRun(true); // 设置示波器为运行状态
                    // 加载缓存配置
                    CacheUtil.get().getCacheMap(); // 获取缓存映射
                    CacheUtil.get().startUp(); // 启动缓存工具
                    // 更新配置到界面
                    updateMainLoadCaheProcess(false); // 更新主界面加载缓存过程

                }catch (Exception e){
                    // 异常处理：清除缓存并重新加载
                    e.printStackTrace(); // 打印异常堆栈
                    CacheUtil.get().clearCacheMap(); // 清除缓存映射
                    CacheUtil.get().clearOtherMap(); // 清除其他映射
                    updateMainLoadCaheProcess(false); // 重新加载配置
                }finally {
                    // 隐藏加载动画
                    findViewById(R.id.loading).setVisibility(View.GONE); // 隐藏加载动画视图
                }
                return false; // 返回false，表示只执行一次
            }
        });

    }

    /**
     * 获取主视图容器
     * 
     * @return MainViewGroup实例
     */
    public MainViewGroup getMainViewGroup() {
        return mainViewGroup;
    }

    /**
     * 初始化所有单例组件
     * 
     * <p><b>初始化组件列表：</b>
     * <ul>
     *   <li>DToast：自定义Toast提示</li>
     *   <li>ScreenControls：屏幕控制（锁屏等）</li>
     *   <li>RecoveryManage：恢复管理</li>
     *   <li>PlaySound：声音播放</li>
     *   <li>RxBus：RxJava事件总线</li>
     *   <li>WorkModeManage：工作模式管理（YT/XY/Zoom）</li>
     *   <li>SerialBusManage：串行总线管理</li>
     *   <li>TriggerTimebase：触发时基</li>
     *   <li>VoltageLineManage：电压线管理</li>
     *   <li>MeasureManage：测量管理</li>
     *   <li>CursorManage：光标管理</li>
     *   <li>WaveGridManage：波形网格管理</li>
     *   <li>RulerManage：标尺管理</li>
     *   <li>Command：命令系统</li>
     *   <li>SCPICommandDeal：SCPI命令处理</li>
     *   <li>WaveManage：波形管理</li>
     *   <li>ExternalKeysManager：外部按键管理</li>
     *   <li>ExternalKeysProtocol：外部按键协议</li>
     *   <li>SaveManage：保存管理</li>
     *   <li>DialogManage：对话框管理</li>
     * </ul>
     */
    private void initSington() {
        DToast.get().init(this); // 初始化自定义Toast提示
        ScreenControls.getInstance().init(mainViewGroup, this, new ScreenControls.IScreenLockListener() { // 初始化屏幕控制，设置锁屏监听器
            @Override
            public void onLockScreen(boolean bLockScreen) { // 锁屏回调
                sendAutoSave(bLockScreen); // 发送自动保存命令
            }
        });

        RecoveryManage.getIns().init(this); // 初始化恢复管理器
        PlaySound.getInstance().init(); // 初始化声音播放工具
        RxBus.getInstance().init(); // 初始化RxJava事件总线
        WorkModeManage.getInstance().init(); // 初始化工作模式管理器（YT/XY/Zoom）
        SerialBusManage.getInstance().init(); // 初始化串行总线管理器

        TriggerTimebase.getInstance().init(); // 初始化触发时基
        VoltageLineManage.getInstance().init(); // 初始化电压线管理器
        MeasureManage.getInstance().init(); // 初始化测量管理器
        CursorManage.getInstance().init(); // 初始化光标管理器
        WaveGridManage.getInstance().init(); // 初始化波形网格管理器
        RulerManage.getIns().init(); // 初始化标尺管理器
        Command.get().init(); // 初始化命令系统
        SCPICommandDeal.getInstance().init(scpiService); // 初始化SCPI命令处理器
        WaveManage.get().init(mainViewGroup); // 初始化波形管理器
        ExternalKeysManager.get().init(mainViewGroup); // 初始化外部按键管理器
        ExternalKeysProtocol.init(); // 初始化外部按键协议
        SaveManage.getInstance().init(this); // 初始化保存管理器
        initMathRef(); // 初始化Math和Ref通道
        PlaySound.getInstance().init(); // 再次初始化声音播放工具（可能是冗余调用）
        DialogManage.getIns().init(this); // 初始化对话框管理器
    }

    /**
     * 触摸事件分发
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>重写dispatchTouchEvent处理触摸事件分发</li>
     *   <li>在ACTION_UP时重置用户触摸状态</li>
     * </ul>
     * 
     * @param ev 触摸事件
     * @return 是否消费事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean r = super.dispatchTouchEvent(ev);
        if(ev.getAction() == MotionEvent.ACTION_UP){
            if(mainViewGroup.isUserTouch()){
                mainViewGroup.setUserTouch(false);
            }
        }
        return r;
    }

    /**
     * 通用运动事件处理（滚轮、鼠标等）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>处理鼠标滚轮事件</li>
     *   <li>实现时基缩放功能</li>
     *   <li>检查自动保存状态，避免冲突</li>
     * </ul>
     * 
     * <p><b>缩放逻辑：</b>
     * <ul>
     *   <li>获取滚轮事件位置</li>
     *   <li>计算相对于波形显示区域的位置</li>
     *   <li>调用Scope.horizontalAxisScale进行时基缩放</li>
     * </ul>
     * 
     * @param event 运动事件
     * @return 是否消费事件
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event){

        if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) { // 检查事件来源是否为指针设备（鼠标等）

            switch (event.getAction()) { // 根据事件动作类型分发
                case MotionEvent.ACTION_SCROLL: // 滚轮滚动事件
                    // 检查自动保存状态
                    AutoSave autoSave = AutoSave.getInstance(); // 获取自动保存实例
                    if(autoSave.isRun() && autoSave.isSaving()){ // 如果自动保存正在运行且正在保存中
                        break; // 跳出，不处理滚轮事件
                    }
                    autoSave.setUserInput(true); // 设置用户输入标志
                    // 计算滚轮位置相对于波形显示区域
                    int x = Math.round(event.getX() - previewTextureViewX); // 计算滚轮位置相对于波形显示区域的X坐标
                    if(x > 0 &&  x < ScopeBase.getWidth()) { // 如果X坐标在波形显示区域内
                        // 调用时基缩放（滚轮向上放大，向下缩小）
                        mScope.horizontalAxisScale(x // 调用时基缩放方法，传入X坐标
                                , event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0 ? 1 : -1); // 根据滚轮方向传入缩放方向（1为放大，-1为缩小）
                    }
                    return true; // 返回true，表示事件已消费
            }
        }
        return super.onGenericMotionEvent(event); // 调用父类方法处理其他事件
    }

    /**
     * 初始化Math和Ref通道
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取Ref通道颜色数组</li>
     *   <li>获取Math通道颜色数组</li>
     *   <li>初始化ChannelFactory中的Ref和Math通道</li>
     * </ul>
     */
    public void initMathRef() {
        int[] refColors = SvgNodeInfo.getRefColorsInt();
        int[] mathColors = SvgNodeInfo.getMathColorsInt();
        ChannelFactory.initRef(refColors);
        ChannelFactory.initMath(mathColors);
    }

    /**
     * 更改通道颜色
     * 
     * @param chIndex 通道索引
     */
    public void changeColor(int chIndex) {
        initMathRef();
        ChannelFactory.getInstance().changeChannelColor(chIndex);
    }

    /**
     * 初始化参数
     * 
     * <p><b>功能说明：</b>设置默认的Run/Stop状态为运行
     */
    private void initParam() {
        CacheUtil.get().putMapInForce(CacheUtil.MAIN_LEFT_RUNSTOP, String.valueOf(true));
    }

    /**
     * Activity启动回调
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

    }

    /**
     * Activity重启回调
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");

    }

    /**
     * 恢复实例状态回调
     * 
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    /**
     * 设置截图参数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从缓存中读取截图设置（时间戳、反色、缩略图）</li>
     *   <li>发送广播通知系统截图服务</li>
     *   <li>写入硬件管理器配置</li>
     * </ul>
     */
    public void setScreenshotParam() {
        Intent i = new Intent("android.intent.action.setScreenshotParam");
        boolean bScreenshotTimeStamp = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP);
        boolean bScreenshotInvert = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT);
        boolean bThumbnail = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SAVETHUMBNAIL);

        i.putExtra("timestamp", bScreenshotTimeStamp);
        i.putExtra("invert", bScreenshotInvert);
        i.putExtra("filePath", CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_CURRENT));
        i.putExtra("fileName", getPictureName());
        i.putExtra("display", bThumbnail);

        HwManager.setString("ScreenshotTimeStamp",String.valueOf(bScreenshotTimeStamp));
        HwManager.setString("ScreenshotInvert",String.valueOf(bScreenshotInvert));
        HwManager.setString("ScreenshotDisplay", String.valueOf(bThumbnail));

        sendBroadcast(i);
    }
    
    /**
     * Activity恢复回调
     * 
     * <p><b>恢复流程：</b>
     * <ol>
     *   <li>调用Scope.resume()恢复示波器</li>
     *   <li>调用WorkModeManage.onResume()恢复工作模式</li>
     *   <li>启用外部按键处理</li>
     *   <li>刷新波形显示</li>
     *   <li>更新外部按键LED状态</li>
     *   <li>重绘参考波形</li>
     *   <li>刷新U盘图标状态</li>
     * </ol>
     */
    @Override
    protected void onResume() {
        super.onResume(); // 调用父类onResume方法
        Log.d(TAG, "activity onResume"); // 打印日志：onResume调用

        // 恢复示波器和工作模式
        mScope.resume(); // 恢复示波器运行状态
        WorkModeManage.getInstance().onResume(); // 恢复工作模式管理器
        App.setDropDownBoxVisiable(this,false); // 设置下拉框不可见

        HwManager.setString("im.emoji", "0"); // 设置硬件管理器参数：禁用emoji输入

        // 启用外部按键处理
        keyService.setDealKeys(true); // 启用外部按键处理
        boolean runStop = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP); // 获取Run/Stop状态

        // 如果是停止状态，定时刷新波形
        if (!runStop) { // 如果示波器处于停止状态
            drawRefresh = 0; // 重置刷新计数器
            mainHandler.sendEmptyMessageDelayed(MSG_REFRESH_WAVE, 1000); // 发送延迟消息，定时刷新波形
        }
        
        // 关闭Splash界面
        if(!App.isMainActivity()) { // 如果MainActivity不是当前Activity
            closeSplashActivity(); // 关闭Splash界面
        }

        // 更新外部按键LED状态
        RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU, new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_ZOOM, // 发送Zoom按键LED状态消息
                CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM)? ExternalKeysMsg_ToMCU.STATE_LED_ON : ExternalKeysMsg_ToMCU.STATE_LED_OFF) ); // 根据Zoom状态设置LED亮灭

        RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU, new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_RUNSTOP, // 发送Run/Stop按键LED状态消息
                CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP) ? ExternalKeysMsg_ToMCU.STATE_LED_GREEN : ExternalKeysMsg_ToMCU.STATE_LED_RED)); // 根据Run/Stop状态设置LED颜色（绿色或红色）

        // 延迟重绘参考波形
        mainHandler.sendEmptyMessageDelayed(MSG_REDRAWREF, 200); // 发送延迟消息，重绘参考波形

        // 刷新U盘图标状态
        statusBar.refreshUdiskIcon(); // 刷新状态栏的U盘图标
        App.setMainActivityAlive(true); // 设置MainActivity存活标志为true
        App.setMainActivity(this); // 设置当前MainActivity实例
    }

    /**
     * 改变波形区域（根据测量项数量调整）
     */
    private void changeWaveArea() {
        // 获取测量项列表
        String channelStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL); // 从缓存获取测量项列表字符串
        Logger.d(TAG, "measureItem channelStrings= " + channelStrings); // 打印日志：测量项列表
        ArrayList<String> channelList = StrUtil.getListFromString(channelStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP); // 解析字符串为列表
        int measureCount = channelList.size(); // 获取测量项数量
        String measureStr; // 测量项字符串
        if (measureCount == 0) { // 如果没有测量项
            measureStr = false + CommandMsgToUI.PARAM_SPLIT + 0; // 设置为false，参数为0
        } else if (measureCount <= 10) { // 如果测量项数量小于等于10
            measureStr = true + CommandMsgToUI.PARAM_SPLIT + 1; // 设置为true，参数为1（单行显示）
        } else { // 如果测量项数量大于10
            measureStr = true + CommandMsgToUI.PARAM_SPLIT + 11; // 设置为true，参数为11（多行显示）
        }
        Logger.d(TAG, "measureItem measureStr= " + measureStr); // 打印日志：测量项字符串
        RxBus.getInstance().post(RxEnum.MQ_MSG_MEASURE_ITEM_COUNT, measureStr); // 发送测量项数量消息
    }

    /**
     * 关闭Splash界面（异步执行）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>等待配置加载完成</li>
     *   <li>设置触发时基位置</li>
     *   <li>发送MSG_SPLASH消息关闭Splash界面</li>
     *   <li>启用命令处理</li>
     *   <li>激活当前Math通道</li>
     * </ul>
     */
    private void closeSplashActivity() {

        new Thread(new Runnable() { // 创建新线程
            @Override
            public void run() { // 线程执行方法
                Thread.currentThread().setName("Main_CloseSplashActivity"); // 设置线程名称

                CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_WaveZoneDisplayManage, false); // 设置加载菜单状态为未完成
                int count = 0; // 计数器
                // 等待配置加载完成
                while (CacheUtil.get().isLoadParamComplete() == false && count++ < 100) { // 循环等待配置加载完成，最多等待10秒
                    Tools.sleep(100); // 等待100毫秒
                }

                // 设置触发时基位置
                TriggerTimebase.getInstance().setCache(); // 设置触发时基的缓存值
                Tools.sleep(1000); // 等待1秒
                
                // 发送消息关闭Splash界面
                if(!mainHandler.hasMessages(MSG_SPLASH)) { // 如果Handler没有MSG_SPLASH消息
                    mainHandler.sendEmptyMessage(MSG_SPLASH); // 发送MSG_SPLASH消息
                }
                Tools.sleep(100); // 等待100毫秒
                Logger.i(TAG, "loadComplete sendemptyMessage:" ); // 打印日志：加载完成
                
                // 启用命令处理
                mScope.enableCommand(true); // 启用命令处理

                // 激活当前Math通道
                int chIdx = ChannelFactory.getChActivate(); // 获取当前活动通道索引
                if(ChannelFactory.isMathCh(chIdx)){ // 如果是Math通道
                    MathChannel mathChannel = ChannelFactory.getMathChannel(chIdx); // 获取Math通道实例
                    mathChannel.activate();; // 激活Math通道
                }
            }
        }).start(); // 启动线程

    }

    /**
     * Activity暂停回调
     * 
     * <p><b>暂停流程：</b>
     * <ol>
     *   <li>停止SaveBin保存</li>
     *   <li>保存当前活动通道</li>
     *   <li>暂停工作模式</li>
     *   <li>关闭Splash对话框</li>
     *   <li>禁用外部按键处理</li>
     *   <li>暂停示波器</li>
     *   <li>暂停缓存</li>
     * </ol>
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "MainActivity onPause!");
        super.onPause(); // 调用父类onPause方法

        // 停止保存
        SaveBin.getInstance().stop(); // 停止SaveBin保存

        // 保存当前活动通道
        CacheUtil.get().putMap(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + "-1", String.valueOf(ChannelFactory.getChActivate()), true); // 保存当前活动通道索引到缓存
        WorkModeManage.getInstance().onPause(); // 暂停工作模式管理器
        SplashDialog.get().closeDialog(); // 关闭Splash对话框

        App.setDropDownBoxVisiable(this,true); // 设置下拉框可见
        // 禁用外部按键处理
        keyService.setDealKeys(false); // 禁用外部按键处理
        // 暂停示波器和缓存
        mScope.pause(); // 暂停示波器
        CacheUtil.get().pause(); // 暂停缓存工具

        HwManager.setString("im.emoji", "1"); // 设置硬件管理器参数：启用emoji输入
    }

    /**
     * Activity停止回调
     */
    @Override
    protected void onStop() {
        Log.i(TAG, "onStop"); // 打印日志：onStop调用
        super.onStop(); // 调用父类onStop方法
        RxBus.getInstance().post(RxEnum.ACTIVITY_ACTIVITYONSTOP, new ActivityMsgOnStop()); // 发送Activity停止消息
    }

    /**
     * 保存实例状态回调
     * 
     * @param outState 输出的状态Bundle
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        long bundleSize = BundleUtil.getBundleSize(outState);
        Log.w("MainActivity", "getBundleSize= " + bundleSize);
    }

    /**
     * Activity结果回调
     * 
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ActivityMsgResult result = new ActivityMsgResult(requestCode, resultCode, data);
        RxBus.getInstance().post(RxEnum.ACTIVITY_ACTIVITYRESULT, result);
    }

    /**
     * Activity销毁回调
     * 
     * <p><b>销毁流程：</b>
     * <ol>
     *   <li>设置示波器为待机状态</li>
     *   <li>取消注册RxBus</li>
     *   <li>取消注册广播接收器</li>
     *   <li>解绑外部按键服务</li>
     *   <li>解绑SCPI服务</li>
     *   <li>设置MainActivity存活标志为false</li>
     *   <li>调用App.finish()结束应用</li>
     * </ol>
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestory"); // 打印日志：onDestroy调用
        // 设置待机状态
        mScope.standby(); // 设置示波器为待机状态

        // 取消注册和解除绑定
        if (rxBusRegister != null) rxBusRegister.unRegister(); // 取消注册RxBus
        BroadcastManager.getInstance().unregisterReceiver(); // 取消注册广播接收器
        keyService.unBind(); // 解绑外部按键服务
        scpiService.unBind(); // 解绑SCPI服务
        App.setMainActivityAlive(false); // 设置MainActivity存活标志为false
        App.finish(); // 调用App.finish()结束应用
        Log.d(TAG, "onDestory end"); // 打印日志：onDestroy结束
        super.onDestroy(); // 调用父类onDestroy方法
    }

    /**
     * 在CacheUtil重新载入配置前清理环境为标准状态
     * 
     * <p><b>清理内容：</b>
     * <ul>
     *   <li>停止示波器运行</li>
     *   <li>关闭所有通道</li>
     *   <li>清除波形</li>
     *   <li>重置Zoom标志</li>
     *   <li>关闭Zoom模式</li>
     *   <li>切换到YT模式</li>
     *   <li>隐藏所有滑出菜单</li>
     * </ul>
     */
    public void preMainLoadCahceProcess() {
        mScope.setRun(false);
        for(int i=0;i<ChannelFactory.CHANNEL_CNT;i++){
            ChannelFactory.chClose(i);
        }
        mScope.clearWave();
        mScope.setZoomFlags(DisplayAction.FLAGS_RESET_BAKSCALEID);
        mScope.setZoom(false);
        WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, false);
        mainViewGroup.hideAllSlip();
    }

    /**
     * 载入CacheUtil内的配置
     * 
     * <p><b>功能说明：</b>如果CacheUtil内为空配置，则载入默认设置信息
     * 
     * <p><b>加载流程：</b>
     * <ol>
     *   <li>保存并恢复运行状态</li>
     *   <li>检查Zoom状态并保存时基值</li>
     *   <li>初始化外部按键管理器</li>
     *   <li>设置时基档位范围</li>
     *   <li>发送MAIN_LOAD_CACHE事件通知各组件加载配置</li>
     *   <li>恢复Zoom模式（如果之前是Zoom状态）</li>
     *   <li>恢复各通道零点位置</li>
     *   <li>恢复Math/Ref通道颜色</li>
     *   <li>恢复活动通道</li>
     *   <li>设置时基位置</li>
     * </ol>
     * 
     * @param isFromRecovery 是否从恢复模式启动
     */
    public void updateMainLoadCaheProcess(boolean isFromRecovery) {
        // 保存Zoom状态和时基值
        double normalScale = 0; // 普通时基值
        double zoomLargeScale = 0; // Zoom大时基值
        double zoomLargeScaleFull = 0; // Zoom大时基值（全屏）
        boolean bRun = mScope.isRun(); // 获取当前运行状态
        mScope.setRun(true); // 设置为运行状态
        CacheUtil.get().checkCacheParam(); // 检查缓存参数
        mScope.setUsersetReset(true); // 设置用户重置标志
        mScope.setRun(bRun); // 恢复运行状态
        boolean bTmpZoom = false; // Zoom状态临时变量

        bTmpZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM); // 获取Zoom状态
        if(bTmpZoom){ // 如果是Zoom状态
            normalScale = TBookUtil.getSFromTime(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE)); // 获取普通时基值
            zoomLargeScale = TBookUtil.getSFromTime(CacheUtil.get().getString(CacheUtil.ZOOM_BOTTOM_TIMEBASE_LARGE_SCALE)); // 获取Zoom大时基值
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM, String.valueOf(false),true); // 暂时关闭Zoom状态
        }
        long x =  CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL); // 获取时基位置
        int channelsSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT); // 获取通道选择
        
        // 初始化外部按键管理器
        ExternalKeysManager.get().init(mainViewGroup); // 初始化外部按键管理器

        // 设置时基档位范围
        HorizontalAxis.setMaxGear(CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_TIMEBASE_MAX)); // 设置最大时基档位
        HorizontalAxis.setMinGear(CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_TIMEBASE_MIN)); // 设置最小时基档位
        HorizontalAxis.getInstance().initXAxis(); // 初始化X轴

        // 发送加载配置事件
        RxBus.getInstance().post(RxEnum.MAIN_LOAD_CACHE, new LoadCache()); // 发送加载缓存事件
        CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT,String.valueOf(channelsSelect),true); // 保存通道选择
        CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_WILL_NULL,String.valueOf(channelsSelect),true); // 保存通道选择（空值）
        RxBus.getInstance().post(RxEnum.MAIN_LOAD_CACHE_EX,new LoadCache()); // 发送加载缓存扩展事件
        CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL, String.valueOf(x),true); // 保存时基位置
        
        // 恢复Zoom模式
        if(bTmpZoom){ // 如果之前是Zoom状态
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM, String.valueOf(bTmpZoom),true); // 恢复Zoom状态

            Command.get().getDisplay().setRoutineTimeBaseMode(Command_Display.RoutineTimeBaseMode_YT, true); // 设置为YT模式
            Command.get().getDisplay().Zoom(true, false); // 开启Zoom
            Scope.getInstance().setZoom(true); // 设置Zoom标志
            RxBus.getInstance().post(RxEnum.WAVEZONE_DISPLAY_YTZOOM, new YTZoomMsgDisplay(true)); // 发送YT Zoom显示消息
            WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YTZOOM, false); // 设置工作模式为YT Zoom

            HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴实例
            horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, horizontalAxis.timeValtoTimeScaleId(normalScale)); // 设置普通时基档位
            horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_LARGE, horizontalAxis.timeValtoTimeScaleId(zoomLargeScale)); // 设置Zoom大时基档位
        }

        // 恢复各通道零点位置
        TChan.foreachChan((i) -> { // 遍历所有通道
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(i)); // 获取通道实例
            if (channel != null) { // 如果通道不为空
                for (int j = VerticalAxis.getMinGear(); j <= VerticalAxis.getMaxGear(); j++) { // 遍历所有档位
                    int pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_CH_Y_ZERO_POSITION + i + j); // 获取零点位置
                    channel.setZero(j, pos); // 设置零点位置
                }
            }
        });

        // 恢复Math/Ref通道颜色
        TChan.foreachMath1ToR8(chan -> { // 遍历所有Math和Ref通道
            String selectColor = CacheUtil.get().getString(CacheUtil.MAIN_CHANNEL_COLOR + chan); // 获取通道颜色
            SvgNodeInfo.setChannelColor(chan, selectColor); // 设置通道颜色
            RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR, chan + ";" + selectColor); // 发送通道颜色消息
        });
        
        // 恢复活动通道
        if (!isFromRecovery) { // 如果不是从恢复模式启动
            int recoverySelect = CacheUtil.get().getInt(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + "-1"); // 获取恢复通道选择
            RxBus.getInstance().post(RxEnum.MQ_MSG_RECOVERY_SELECT, recoverySelect); // 发送恢复通道选择消息
        }

        // 设置时基位置
        long l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL); // 获取时基位置
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴实例
        horizontalAxis.setTimePosOfView( // 设置时基位置
                HorizontalAxis.WPI_STANDARD, // 普通时基
                horizontalAxis.getTimePose(HorizontalAxis.WPI_STANDARD, ScopeBase.getWidth() / 2 - l) // 计算时基位置
        );
        mScope.setUsersetReset(false); // 重置用户设置标志

    }

    /**
     * 等待CacheUtil重新载入后执行功能
     * 
     * <p><b>功能说明：</b>启动示波器为运行状态，启动zoom状态
     */
    public void postMainLoadCacheProcess() {
        closeSplashActivity();
    }

    /**
     * 波形刷新计数器
     */
    private int drawRefresh = 0;

    /**
     * 主线程Handler
     * 
     * <p><b>处理消息类型：</b>
     * <ul>
     *   <li>MSG_SPLASH：启动完成，关闭Splash界面，初始化Zoom等</li>
     *   <li>MSG_REDRAWREF：重绘参考波形</li>
     *   <li>MSG_REFRESH_WAVE：刷新波形显示</li>
     *   <li>MSG_SAVE_PICTURE：保存截图</li>
     * </ul>
     */
    class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SPLASH: { // 启动完成消息
                    {
                        // 关闭Splash对话框
                        SplashDialog.get().closeDialog(); // 关闭Splash对话框
                        boolean runStop = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP); // 获取Run/Stop状态

                        // 设置通道位置（屏幕中心模式）
                        ChannelFactory.forEachCh(channel -> { // 遍历所有通道
                            if(channel.getVerticalMode() == Channel.VERTICAL_MODE_SCREEN_CENTER){ // 如果是屏幕中心模式
                                channel.setPos(Tools.getYTChannelPosition((channel.getChId() - ChannelFactory.CH1)  + 1)); // 设置通道位置
                            }
                        });
                        
                        // 重新设置Math类型
                        ChannelFactory.forEachMath(mathChannel -> mathChannel.setMathType(mathChannel.getMathType())); // 遍历所有Math通道，重新设置Math类型

                        // 激活当前通道
                        ChannelFactory.chActivate(ChannelFactory.getChActivate()); // 激活当前通道
                        
                        // 恢复XY显示模式
                        if(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1){ // 如果是XY模式
                            Display.getInstance().setDisplayMode(Display.DISPLAY_XY); // 设置显示模式为XY
                        }

                        // 如果是滚动模式，关闭Zoom
                        if (mScope.isRun() && mScope.isInScrollMode()) { // 如果是运行状态且是滚动模式
                            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM, String.valueOf(false)); // 关闭Zoom
                            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU, // 发送Zoom按键LED状态消息
                                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_ZOOM, // Zoom按键类型
                                            ExternalKeysMsg_ToMCU.STATE_LED_OFF)); // LED关闭状态
                        }

                        // 恢复Zoom模式
                        boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM); // 获取Zoom状态
                        if (zoom) { // 如果是Zoom状态
                            double x =  TBookUtil.getSFromTime(CacheUtil.get().getString(CacheUtil.ZOOM_BOTTOM_TIMEBASE_LARGE_SCALE)); // 获取Zoom大时基值
                            HorizontalAxis.getInstance().setTimePoseOfViewPix(0); // 设置时基位置像素为0
                            HorizontalAxis.getInstance().correctTimePose_poseMove(); // 校正时基位置移动
                            YTZoomMsgDisplay ytZoomMsgDisplay = new YTZoomMsgDisplay(true); // 创建YT Zoom显示消息
                            ytZoomMsgDisplay.setReloadLargeTimeScale(true); // 设置重新加载大时基标志
                            ytZoomMsgDisplay.setPlaySound(false); // 设置不播放音效
                            RxBus.getInstance().post(RxEnum.WAVEZONE_DISPLAY_YTZOOM, ytZoomMsgDisplay); // 发送YT Zoom显示消息
                            HorizontalAxis.getInstance().setTimePosOfView( // 设置时基位置
                                    HorizontalAxis.WPI_LARGE, // Zoom大时基
                                    HorizontalAxis.getInstance().getTimePose(HorizontalAxis.WPI_LARGE, (long) (ScopeBase.getWidth() / 2 - x)) // 计算时基位置
                            );
                            HorizontalAxis.getInstance().correctTimePose_poseMove(); // 校正时基位置移动
                            TriggerTimebase.getInstance().movePix(0); // 移动触发时基像素为0
                        }
                        
                        // 恢复显示设置
                        Display display = Display.getInstance(); // 获取显示实例
                        display.setCCT(display.isCCT()); // 设置CCT状态
                        display.setPersistType(display.getPersistType()); // 设置持久化类型
                        mScope.setRun(runStop); // 设置运行状态
                        mScope.setUI(true); // 设置UI标志
                        setScreenshotParam(); // 设置截图参数
                        
                        // 更新串行触发电压
                        ChannelFactory.forEachCh((channel)->{ // 遍历所有通道
                            MainHolderTriggerLevel.changeSerialTrigVol_channel(TChan.toUiChNo(channel.getChId())); // 更新串行触发电压
                        });
                        HwManager.setString("app.micsig","2"); // 设置硬件管理器参数：应用版本
                    }
                }
                break; // 跳出MSG_SPLASH case
                case MSG_REDRAWREF: { // 重绘参考波形消息
                    // 重绘参考波形
                    if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_REF)) { // 如果右侧有Ref通道
                        RefChannel refChannel; // Ref通道实例

                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch1)) { // 如果CH1打开
                            ChannelFactory.getRefChannel(ChannelFactory.REF1).setPos(Tools.getYTChannelPosition(TChan.R1)); // 设置REF1位置
                        }
                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch2)) { // 如果CH2打开
                            ChannelFactory.getRefChannel(ChannelFactory.REF2).setPos(Tools.getYTChannelPosition(TChan.R2)); // 设置REF2位置
                        }
                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch3)) { // 如果CH3打开
                            ChannelFactory.getRefChannel(ChannelFactory.REF3).setPos(Tools.getYTChannelPosition(TChan.R3)); // 设置REF3位置
                        }
                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch4)) { // 如果CH4打开
                            ChannelFactory.getRefChannel(ChannelFactory.REF4).setPos(Tools.getYTChannelPosition(TChan.R4)); // 设置REF4位置
                        }
                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch5)) { // 如果CH5打开
                            refChannel = ChannelFactory.getRefChannel(ChannelFactory.REF5); // 获取REF5实例
                            if (refChannel == null) return; // 如果为空，返回
                            refChannel.setPos(Tools.getYTChannelPosition(TChan.R5)); // 设置REF5位置
                        }
                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch6)) { // 如果CH6打开
                            refChannel = ChannelFactory.getRefChannel(ChannelFactory.REF6); // 获取REF6实例
                            if (refChannel == null) return; // 如果为空，返回
                            refChannel.setPos(Tools.getYTChannelPosition(TChan.R6)); // 设置REF6位置
                        }
                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch7)) { // 如果CH7打开
                            refChannel = ChannelFactory.getRefChannel(ChannelFactory.REF7); // 获取REF7实例
                            if (refChannel == null) return; // 如果为空，返回
                            refChannel.setPos(Tools.getYTChannelPosition(TChan.R7)); // 设置REF7位置
                        }
                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch8)) { // 如果CH8打开
                            refChannel = ChannelFactory.getRefChannel(ChannelFactory.REF8); // 获取REF8实例
                            if (refChannel == null) return; // 如果为空，返回
                            refChannel.setPos(Tools.getYTChannelPosition(TChan.R8)); // 设置REF8位置
                        }

                    }
                }
                break; // 跳出MSG_REDRAWREF case
                case MSG_REFRESH_WAVE: { // 刷新波形消息
                    // 刷新波形（停止状态时定时刷新）
                    if (drawRefresh < 3) { // 如果刷新次数小于3
                        mainHandler.sendEmptyMessageDelayed(MSG_REFRESH_WAVE, 150); // 发送延迟消息，继续刷新
                        drawRefresh++; // 增加刷新计数器
                    }
                    mScope.Refresh(); // 刷新示波器波形
                }
                break; // 跳出MSG_REFRESH_WAVE case
                case MSG_SAVE_PICTURE:{ // 保存截图消息
                    // 保存截图
                    String picturePath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_CURRENT); // 获取截图路径
                    String pictureName = getPictureName(); // 获取截图文件名
                    doSavePicture(picturePath, pictureName); // 执行保存截图
                }
                break; // 跳出MSG_SAVE_PICTURE case
            }
        }
    }

    /**
     * 获取截图文件名
     * 
     * <p><b>命名规则：</b>
     * <ul>
     *   <li>从缓存获取用户设置的文件名</li>
     *   <li>如果为空则自动生成</li>
     *   <li>根据设置添加序号后缀</li>
     *   <li>添加.png扩展名</li>
     * </ul>
     * 
     * @return 截图文件名
     */
    private String getPictureName() {
        String pictureName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_NAME);
        if (pictureName.isEmpty()) {
            pictureName = Tools.generateName();
        }
        boolean isFileNumAddCheck = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK);
        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK_NUM);
        if (isFileNumAddCheck) {
            pictureName = pictureName + "_" + suffixNum + ".png";
        } else {
            pictureName = pictureName + ".png";
        }
        return pictureName;
    }

    /**
     * 自动保存截图
     * 
     * <p><b>功能说明：</b>从缓存获取自动保存路径和文件名，执行截图保存
     */
    public void autoSaveScreenShot() {
        String picturePath = CacheUtil.get().getString(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_AUTO_SAVE); // 获取自动保存路径
        String pictureName = CacheUtil.get().getString(CacheUtil.TOP_SLIP_SAVE_PICTURE_AUTO_SAVE_NAME); // 获取自动保存文件名
        Logger.i(TAG, "screenShot filePath= " + picturePath + " ,fileName= " + pictureName); // 打印日志：截图路径和文件名
        doAutoSavePicture(picturePath, pictureName); // 执行自动保存截图
    }

    /**
     * 手动截图
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从缓存获取截图路径和文件名</li>
     *   <li>检查文件是否存在</li>
     *   <li>如果存在则显示确认对话框</li>
     *   <li>如果不存在则直接保存</li>
     * </ul>
     */
    public void screenShot() {
        String picturePath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_CURRENT); // 获取当前截图路径
        String pictureName = getPictureName(); // 获取截图文件名
        Logger.i(TAG, "screenShot filePath= " + picturePath + " ,fileName= " + pictureName); // 打印日志：截图路径和文件名

        if (SaveManage.getInstance().checkFileExists(picturePath + File.separator + pictureName)) { // 检查文件是否存在
            ((DialogOkCancel) mainViewGroup.getDialog(MainViewGroup.DIALOG_OKCANCEL)) // 获取确认取消对话框
                    .setData(null, R.string.top_slip_save_file_exists, null, null, onOkCancelClickListener); // 设置对话框数据：文件已存在
        } else { // 如果文件不存在
            doSavePicture(picturePath, pictureName); // 直接保存截图
        }
    }

    /**
     * 执行保存截图
     * 
     * @param path 保存路径
     * @param name 文件名
     */
    private void doSavePicture(String path, String name) {
        keyService.screenshot(CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP), // 调用截图服务，传入时间戳设置
                CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT), path, name); // 传入反色设置、路径和文件名

        RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_CAPTURE_SUCCESS, true); // 发送截图保存成功消息
    }

    /**
     * 执行自动保存截图
     * 
     * @param path 保存路径
     * @param name 文件名
     */
    private void doAutoSavePicture(String path, String name) {
        keyService.screenshot(CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP), // 调用截图服务，传入时间戳设置
                CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT), path, name,true); // 传入反色设置、路径、文件名和自动保存标志

    }

    /**
     * 截图确认对话框点击监听器
     */
    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener = new DialogOkCancel.OnOkCancelClickListener() {
        @Override
        public void onOkClick(View v, Object data) {
            String picturePath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_CURRENT);
            String pictureName = getPictureName();
            if (FileUtils.deleteFile(picturePath + File.separator + pictureName)) {
                mainHandler.sendEmptyMessageDelayed(MSG_SAVE_PICTURE, 50);
            }
        }

        @Override
        public void onCancelClick(View v, Object data) {
            Logger.i("Click cancel");
        }

        @Override
        public void onDialogClose(View view) {
        }
    };

    /**
     * 解绑无线设备ID
     */
    public void unbindWirelessId(){
        keyService.unbindWirelessId();
    }

    /**
     * 发送自动保存命令
     * 
     * @param bAutoSave 是否自动保存
     */
    public void sendAutoSave(boolean bAutoSave){
        keyService.sendAutoSave(bAutoSave);
    }

    /**
     * 按键按下事件处理
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>过滤忽略的按键</li>
     *   <li>处理探头事件（扫描码194）</li>
     * </ul>
     * 
     * @param keyCode 按键码
     * @param event 按键事件
     * @return 是否消费事件
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEventUtil.isIgnoreKey(event)) return true; // 如果是忽略的按键，直接返回true
        if(event.getAction() == KeyEvent.ACTION_DOWN){ // 如果是按键按下事件
            if(event.getScanCode() == 194){ // 如果扫描码是194（探头事件）
                if(!Scope.getInstance().isStandby()) { // 如果示波器不是待机状态
                    ScopeMessage.getInstance().probeEvent(); // 处理探头事件
                }
            }
        }
        return super.onKeyDown(keyCode, event); // 调用父类方法处理其他按键
    }

    /**
     * 获取触发时间滑块控件
     * 
     * @return MTriggerTime实例
     */
    public MTriggerTime getSlider() {
        return slider;
    }

}
