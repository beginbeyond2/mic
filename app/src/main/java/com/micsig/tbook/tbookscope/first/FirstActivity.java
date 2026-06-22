package com.micsig.tbook.tbookscope.first;   // 启动模块包：负责开机启动、权限申请、语言切换和启动画面显示

import android.content.Intent;   // 导入Android意图类，用于组件间通信和启动Activity
import android.net.Uri;   // 导入Android Uri类，用于构建权限设置页面的Uri
import android.os.Build;   // 导入Android Build类，用于获取SDK版本号
import android.os.Bundle;   // 导入Android Bundle类，用于保存和恢复Activity状态
import android.provider.Settings;   // 导入Android Settings类，用于访问系统设置（悬浮窗权限）
import android.util.Log;   // 导入Android日志类，用于调试日志输出
import android.view.Window;   // 导入Android Window类，用于设置窗口属性（无标题）
import android.view.WindowManager;   // 导入Android WindowManager类，用于设置悬浮窗类型
import android.widget.Toast;   // 导入Android Toast类，用于显示提示信息

import androidx.annotation.Nullable;   // 导入Nullable注解，标记参数可为空
import androidx.appcompat.app.AppCompatActivity;   // 导入AppCompatActivity基类，提供兼容性支持

import com.hjq.permissions.OnPermissionCallback;   // 导入XXPermissions权限回调接口
import com.hjq.permissions.Permission;   // 导入XXPermissions权限常量类
import com.hjq.permissions.XXPermissions;   // 导入XXPermissions权限请求框架
import com.micsig.tbook.tbookscope.MainActivity;   // 导入主Activity类，启动完成后的跳转目标
import com.micsig.tbook.tbookscope.R;   // 导入资源类，访问布局和字符串资源
import com.micsig.tbook.tbookscope.SplashDialog;   // 导入启动画面对话框类，显示启动画面
import com.micsig.tbook.tbookscope.config.IConfig;   // 导入配置接口，定义产品校验方法
import com.micsig.tbook.tbookscope.config.ScopeConfig;   // 导入示波器配置类，获取当前产品配置
import com.micsig.tbook.tbookscope.util.App;   // 导入应用工具类，提供全局应用上下文和状态查询
import com.micsig.tbook.tbookscope.util.Screen;   // 导入屏幕工具类，获取屏幕尺寸信息

import java.util.List;   // 导入List集合类，用于权限列表


/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                                                                              │
 * │  FirstActivity - 启动Activity                                                │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   所属模块: first（启动模块）                                                │
 * │   所在层级: 应用启动入口层                                                   │
 * │   作用范围: 应用启动流程控制                                                 │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 产品校验：验证当前设备是否为合法产品（isValidProduct）                  │
 * │   2. 权限申请：申请存储权限（XXPermissions）                                 │
 * │   3. 悬浮窗权限：申请悬浮窗权限（canDrawOverlays）                           │
 * │   4. 启动画面：显示SplashDialog启动对话框                                    │
 * │   5. 跳转主页：启动完成后跳转到MainActivity                                  │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   继承自AppCompatActivity，作为应用的启动入口Activity。                      │
 * │   启动流程采用链式设计：                                                     │
 * │   产品校验 → 权限申请 → 悬浮窗权限 → 启动画面 → 跳转MainActivity           │
 * │                                                                              │
 * │   关键设计决策：                                                              │
 * │   - 使用SplashDialog悬浮窗显示启动画面，而非Activity主题方式                │
 * │   - 悬浮窗类型根据SDK版本动态选择（TYPE_APPLICATION_OVERLAY vs TYPE_TOAST） │
 * │   - 产品校验失败时显示Toast提示并finish()退出                                │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   onCreate()                                                                 │
 * │     │                                                                        │
 * │     ├─ isValidProduct() == false → Toast提示 → finish()退出                 │
 * │     │                                                                        │
 * │     └─ isValidProduct() == true                                              │
 * │         ├─ requestPermissions() → XXPermissions申请存储权限                  │
 * │         ├─ App.isMainActivity() == true → 直接启动MainActivity              │
 * │         └─ App.isMainActivity() == false                                     │
 * │             ├─ canDrawOverlays == false → 跳转悬浮窗权限设置页              │
 * │             └─ canDrawOverlays == true → SplashDialog.showDialog()          │
 * │                 └─ onShowStart() → 启动MainActivity → finish()              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖: AppCompatActivity (Android兼容Activity基类)                          │
 * │   依赖: XXPermissions (权限请求框架：存储权限申请)                           │
 * │   依赖: SplashDialog (启动画面对话框：显示启动画面)                          │
 * │   依赖: ScopeConfig/IConfig (配置管理：产品校验)                             │
 * │   依赖: App (应用工具：MainActivity状态查询)                                 │
 * │   依赖: Screen (屏幕工具：获取屏幕尺寸)                                     │
 * │   依赖: MainActivity (主Activity：启动完成后的跳转目标)                      │
 * │   被依赖: BootCompletedReceiver (开机广播接收器：开机自启动)                 │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   // 通过Intent启动FirstActivity                                             │
 * │   Intent intent = new Intent(context, FirstActivity.class);                  │
 * │   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);                            │
 * │   context.startActivity(intent);                                             │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【注意事项】                                                                 │
 * │   1. 本Activity无标题栏（requestWindowFeature(FEATURE_NO_TITLE)）            │
 * │   2. 悬浮窗权限为启动画面的前置条件，缺失时跳转系统设置页                   │
 * │   3. Android 8.0+必须使用TYPE_APPLICATION_OVERLAY窗口类型                   │
 * │   4. 产品校验失败时应用直接退出，无法进入主界面                              │
 * │   5. 若MainActivity已存活（App.isMainActivity()），跳过启动画面直接启动     │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * @see AppCompatActivity
 * @see SplashDialog
 * @see MainActivity
 * @see BootCompletedReceiver
 * @see ScopeConfig
 * @author Micsig智能示波器团队
 * @version 1.0
 * @since MHO Series Oscilloscope Software
 */
public class FirstActivity extends AppCompatActivity {   // 启动Activity：负责产品校验、权限申请、启动画面显示和跳转MainActivity

    /**
     * 日志标签 - 用于调试日志输出
     *
     * <p>TAG常量，用于Log日志输出，标识日志来源为FirstActivity类。</p>
     */
    private static final String TAG = "FirstActivity";   // 日志标签：标识日志来源为FirstActivity类

    /**
     * 悬浮窗权限请求码 - 用于onActivityResult中识别悬浮窗权限请求的返回结果
     *
     * <p>当用户从系统悬浮窗权限设置页返回时，onActivityResult()通过此请求码
     * 识别是悬浮窗权限请求的返回，从而继续显示启动画面。</p>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>startActivityForResult()中作为请求码传入</li>
     *   <li>onActivityResult()中用于匹配返回结果</li>
     * </ul>
     */
    private static final int REQUEST_CODE_OVERLAY = 10;   // 悬浮窗权限请求码：用于识别悬浮窗权限请求的返回结果

    /**
     * Activity创建回调 - 执行启动流程的核心逻辑
     *
     * <p>在Activity创建时执行以下启动流程：</p>
     * <ol>
     *   <li>设置无标题栏窗口</li>
     *   <li>调用父类onCreate()</li>
     *   <li>产品校验（isValidProduct）</li>
     *   <li>校验通过：设置布局 → 申请权限 → 显示启动画面/跳转MainActivity</li>
     *   <li>校验失败：显示Toast提示 → 退出Activity</li>
     * </ol>
     *
     * <h4>启动流程分支</h4>
     * <pre>
     * onCreate()
     *   │
     *   ├─ isValidProduct() == false
     *   │   └─ Toast提示"系统不支持" → finish()
     *   │
     *   └─ isValidProduct() == true
     *       ├─ setContentView(layout_first)
     *       ├─ requestPermissions()
     *       │
     *       ├─ App.isMainActivity() == true（主界面已存活）
     *       │   └─ 直接调用onShowStart()跳转MainActivity
     *       │
     *       └─ App.isMainActivity() == false（首次启动）
     *           ├─ SplashDialog不可见时：
     *           │   ├─ 无悬浮窗权限 → 跳转系统设置页
     *           │   └─ 有悬浮窗权限 → 显示SplashDialog
     *           └─ 获取屏幕尺寸信息
     * </pre>
     *
     * @param savedInstanceState 保存的实例状态，Activity被系统回收后恢复时使用，正常启动时为null
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {   // Activity创建回调：执行产品校验、权限申请和启动画面显示
        requestWindowFeature(Window.FEATURE_NO_TITLE);   // 设置无标题栏：移除Activity标题栏，实现全屏显示效果

        super.onCreate(savedInstanceState);   // 调用父类onCreate：执行AppCompatActivity的初始化逻辑
        if(isValidProduct()) {   // 判断：当前设备是否为合法产品
            setContentView(R.layout.layout_first);   // 设置布局：加载layout_first布局文件
            requestPermissions();   // 申请权限：请求存储权限

            if(App.isMainActivity()){   // 判断：MainActivity是否已存活（应用已在运行中）
                onShowStartListener.onShowStart();   // 直接启动MainActivity：无需显示启动画面，直接跳转
            }else {   // MainActivity未存活，需要显示启动画面
                if (!SplashDialog.get().isVisible()) {   // 判断：启动画面是否尚未显示
                    if (!Settings.canDrawOverlays(FirstActivity.this)) {   // 判断：是否缺少悬浮窗权限
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,   // 创建意图：跳转到悬浮窗权限设置页
                                Uri.parse("package:" + getPackageName()));   // 构建Uri：指定当前应用的包名，定位到本应用的权限设置
                        startActivityForResult(intent, REQUEST_CODE_OVERLAY);   // 启动权限设置页：等待用户授权后返回
                    } else {   // 已有悬浮窗权限
                        SplashDialog.get().showDialog(getParamsType(), onShowStartListener);   // 显示启动画面：传入窗口类型和启动完成回调
                    }   // 悬浮窗权限判断结束
                }   // 启动画面可见性判断结束
                Screen.getScreen(App.get());   // 获取屏幕尺寸：初始化屏幕信息，用于后续界面布局计算
            }   // MainActivity存活判断结束
        }else{   // 产品校验失败
            Toast.makeText(this,R.string.app_system_supported,Toast.LENGTH_LONG).show();   // 显示提示：告知用户当前系统不支持此应用

            finish();   // 退出Activity：产品校验失败，直接退出启动流程
        }   // 产品校验判断结束
    }   // onCreate方法结束

    /**
     * Activity结果回调 - 处理悬浮窗权限请求的返回结果
     *
     * <p>当用户从系统悬浮窗权限设置页返回时，此方法被调用。
     * 无论用户是否授权悬浮窗权限，都尝试显示启动画面。
     * 如果用户未授权，SplashDialog将使用TYPE_TOAST窗口类型（兼容旧版本）。</p>
     *
     * <h4>处理逻辑</h4>
     * <ul>
     *   <li>匹配REQUEST_CODE_OVERLAY请求码</li>
     *   <li>直接调用SplashDialog.showDialog()显示启动画面</li>
     *   <li>不检查授权结果，因为即使未授权也尝试显示</li>
     * </ul>
     *
     * @param requestCode 请求码，用于识别是哪个startActivityForResult的返回
     * @param resultCode  结果码，一般为RESULT_OK或RESULT_CANCELED
     * @param data 返回数据，本场景中不使用
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {   // Activity结果回调：处理悬浮窗权限请求的返回结果
        super.onActivityResult(requestCode, resultCode, data);   // 调用父类方法：执行默认的结果处理逻辑
        if (requestCode == REQUEST_CODE_OVERLAY) {   // 判断：是否为悬浮窗权限请求的返回
            SplashDialog.get().showDialog(getParamsType(), onShowStartListener);   // 显示启动画面：无论授权与否都尝试显示
        }   // 请求码判断结束
    }   // onActivityResult方法结束

    /**
     * 启动完成回调监听器 - 启动画面显示完成后启动MainActivity
     *
     * <p>当SplashDialog启动画面显示完成后，通过此监听器回调通知FirstActivity，
     * FirstActivity在回调中启动MainActivity并关闭自身。</p>
     *
     * <h4>回调流程</h4>
     * <ul>
     *   <li>创建指向MainActivity的Intent</li>
     *   <li>启动MainActivity</li>
     *   <li>设置无动画过渡（overridePendingTransition(0,0)）</li>
     *   <li>关闭FirstActivity</li>
     * </ul>
     *
     * <h4>注意事项</h4>
     * <ul>
     *   <li>使用overridePendingTransition(0,0)取消Activity切换动画，实现无缝过渡</li>
     *   <li>finish()后FirstActivity从任务栈中移除</li>
     * </ul>
     */
    private SplashDialog.OnShowStartListener onShowStartListener = new SplashDialog.OnShowStartListener() {   // 启动完成回调监听器：启动画面显示完成后启动MainActivity
        @Override
        public void onShowStart() {   // 启动画面显示完成回调
            Intent intent = new Intent(FirstActivity.this, MainActivity.class);   // 创建意图：目标为MainActivity
            startActivity(intent);   // 启动MainActivity：进入应用主界面
            overridePendingTransition(0, 0);   // 取消Activity切换动画：实现无缝过渡，避免闪烁
            finish();   // 关闭FirstActivity：从任务栈中移除，用户按返回键不会回到此页面
        }   // onShowStart方法结束
    };   // onShowStartListener定义结束

    /**
     * 获取悬浮窗类型参数 - 根据SDK版本选择合适的窗口类型
     *
     * <p>根据Android SDK版本和悬浮窗权限状态，选择合适的WindowManager.LayoutParams类型：</p>
     * <ul>
     *   <li>Android 8.0+且有悬浮窗权限：使用TYPE_APPLICATION_OVERLAY（系统悬浮窗）</li>
     *   <li>其他情况：使用TYPE_TOAST（Toast类型窗口，不需要悬浮窗权限）</li>
     * </ul>
     *
     * <h4>窗口类型说明</h4>
     * <table border="1">
     *   <tr><th>窗口类型</th><th>值</th><th>说明</th><th>适用版本</th></tr>
     *   <tr><td>TYPE_APPLICATION_OVERLAY</td><td>2038</td><td>系统悬浮窗，需要悬浮窗权限</td><td>Android 8.0+</td></tr>
     *   <tr><td>TYPE_TOAST</td><td>2005</td><td>Toast类型窗口，不需要悬浮窗权限</td><td>Android 8.0以下</td></tr>
     * </table>
     *
     * <h4>注意事项</h4>
     * <ul>
     *   <li>Android 8.0+使用TYPE_SYSTEM_ALERT已被废弃，必须使用TYPE_APPLICATION_OVERLAY</li>
     *   <li>TYPE_TOAST在Android 7.1+可能无法正常显示，需要悬浮窗权限</li>
     * </ul>
     *
     * @return 窗口类型参数，TYPE_APPLICATION_OVERLAY或TYPE_TOAST
     */
    private int getParamsType() {   // 获取悬浮窗类型参数：根据SDK版本选择合适的窗口类型
        int paramsType;   // 声明窗口类型变量
        if (Settings.canDrawOverlays(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {   // 判断：是否有悬浮窗权限且SDK版本>=8.0
            paramsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;   // 使用TYPE_APPLICATION_OVERLAY：Android 8.0+的悬浮窗类型
        } else {   // 无悬浮窗权限或SDK版本<8.0
            paramsType = WindowManager.LayoutParams.TYPE_TOAST;   // 使用TYPE_TOAST：兼容旧版本的窗口类型
        }   // SDK版本判断结束
        return paramsType;   // 返回窗口类型参数
    }   // getParamsType方法结束

    /**
     * 申请存储权限 - 使用XXPermissions框架申请存储权限
     *
     * <p>使用XXPermissions权限请求框架申请存储权限（READ_EXTERNAL_STORAGE +
     * WRITE_EXTERNAL_STORAGE），确保应用能够读写外部存储中的文件。</p>
     *
     * <h4>权限申请流程</h4>
     * <pre>
     * requestPermissions()
     *   │
     *   ├─ XXPermissions.with(this)
     *   │   └─ .permission(Permission.Group.STORAGE)
     *   │       └─ .request(callback)
     *   │
     *   ├─ onGranted() - 权限授予
     *   │   └─ all==true → 日志"获取存储权限成功"
     *   │
     *   └─ onDenied() - 权限拒绝
     *       ├─ never==true → 日志"被永久拒绝" → 跳转系统权限设置页
     *       └─ never==false → 日志"获取存储权限失败"
     * </pre>
     *
     * <h4>注意事项</h4>
     * <ul>
     *   <li>存储权限用于保存/加载示波器设置和波形数据</li>
     *   <li>被永久拒绝时自动跳转到系统权限设置页，引导用户手动授权</li>
     *   <li>权限被拒绝不会阻止应用启动，但可能影响部分功能</li>
     * </ul>
     */
    private void requestPermissions(){   // 申请存储权限：使用XXPermissions框架申请存储权限
        XXPermissions.with(this)   // 创建权限请求：绑定当前Activity
            .permission(Permission.Group.STORAGE)   // 申请存储权限组：包含READ_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE
            .request(new OnPermissionCallback() {   // 发起权限请求：设置权限回调监听器

                @Override
                public void onGranted(List<String> permissions, boolean all) {   // 权限授予回调
                    if (all) {   // 判断：是否所有权限都已授予
                        Log.d(TAG,"获取存储权限成功");   // 记录日志：所有存储权限已成功获取
                    }   // all判断结束
                }   // onGranted方法结束

                @Override
                public void onDenied(List<String> permissions, boolean never) {   // 权限拒绝回调
                    if (never) {   // 判断：是否被永久拒绝（用户勾选"不再询问"）
                        Log.d(TAG,"被永久拒绝授权，请手动授予存储权限");   // 记录日志：权限被永久拒绝
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(FirstActivity.this, permissions);   // 跳转权限设置页：引导用户手动授权
                    } else {   // 非永久拒绝（用户本次拒绝但未勾选"不再询问"）
                        Log.d(TAG,"获取存储权限失败");   // 记录日志：权限获取失败
                    }   // never判断结束
                }   // onDenied方法结束
            });   // 权限回调结束
    }   // requestPermissions方法结束

    /**
     * 产品校验 - 验证当前设备是否为合法的MHO系列示波器产品
     *
     * <p>通过ScopeConfig获取当前产品配置，调用isValidProduct()方法验证
     * 当前设备是否为合法的MHO系列示波器产品。校验失败时，应用将无法进入主界面。</p>
     *
     * <h4>校验逻辑</h4>
     * <ul>
     *   <li>获取ScopeConfig配置实例</li>
     *   <li>调用IConfig.isValidProduct()进行产品校验</li>
     *   <li>校验依据包括：设备型号、FPGA DNA、硬件版本等</li>
     * </ul>
     *
     * <h4>校验失败处理</h4>
     * <ul>
     *   <li>在onCreate()中，校验失败会显示Toast提示并finish()退出</li>
     * </ul>
     *
     * @return true-合法产品，允许启动；false-非法产品，拒绝启动
     */
    private boolean isValidProduct(){   // 产品校验：验证当前设备是否为合法的MHO系列示波器产品
        IConfig config = ScopeConfig.getConfig();   // 获取配置实例：获取当前产品的ScopeConfig配置
        return config.isValidProduct();   // 返回校验结果：调用isValidProduct()判断产品合法性
    }   // isValidProduct方法结束
}   // FirstActivity类结束
