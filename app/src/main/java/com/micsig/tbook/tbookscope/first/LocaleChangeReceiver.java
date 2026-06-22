package com.micsig.tbook.tbookscope.first;   // 启动模块包：负责开机启动、权限申请、语言切换和启动画面显示

import android.content.BroadcastReceiver;   // 导入Android广播接收器基类
import android.content.Context;   // 导入Android上下文类，用于访问应用环境和资源
import android.content.Intent;   // 导入Android意图类，用于获取广播Action

import com.micsig.base.Logger;   // 导入自定义日志工具类，封装日志输出功能
import com.micsig.tbook.tbookscope.util.App;   // 导入应用工具类，提供应用退出功能


/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                                                                              │
 * │  LocaleChangeReceiver - 语言切换广播接收器                                   │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   所属模块: first（启动模块）                                                │
 * │   所在层级: 系统广播监听层                                                   │
 * │   作用范围: 系统语言变更事件监听                                             │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   监听系统语言变更广播（ACTION_LOCALE_CHANGED），当系统语言改变时            │
 * │   调用App.finish()强制退出应用，确保应用重新启动后加载正确的语言资源。       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   继承自Android BroadcastReceiver，在AndroidManifest.xml中静态注册，         │
 * │   监听ACTION_LOCALE_CHANGED系统广播。                                       │
 * │                                                                              │
 * │   设计理念：                                                                  │
 * │   - 示波器应用的UI文本和资源配置与系统语言强相关                             │
 * │   - 语言切换后如果不重启应用，可能导致资源加载不一致                         │
 * │   - 采用"退出重启"策略，确保语言切换后资源完全重新加载                       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   系统语言变更 → ACTION_LOCALE_CHANGED广播 → onReceive()                    │
 * │     → Logger.e("ACTION_LOCALE_CHANGED") → App.finish() → 应用退出          │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖: BroadcastReceiver (Android广播接收器基类)                            │
 * │   依赖: Logger (自定义日志工具：记录语言变更事件)                            │
 * │   依赖: App (应用工具类：提供finish()强制退出方法)                           │
 * │   被依赖: Android系统 (通过AndroidManifest.xml静态注册)                      │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   本类通过AndroidManifest.xml静态注册，无需手动实例化：                      │
 * │   &lt;receiver android:name=".first.LocaleChangeReceiver"&gt;                  │
 * │       &lt;intent-filter&gt;                                                      │
 * │           &lt;action android:name="android.intent.action.LOCALE_CHANGED"/&gt;  │
 * │       &lt;/intent-filter&gt;                                                    │
 * │   &lt;/receiver&gt;                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【注意事项】                                                                 │
 * │   1. App.finish()会强制终止应用进程（Process.killProcess + System.exit）     │
 * │   2. 语言变更后应用不会自动重启，需要配合BootCompletedReceiver开机自启动     │
 * │   3. 使用Logger.e()而非Logger.i()记录，因为语言变更属于需要关注的异常事件   │
 * │   4. 此广播只有在系统语言真正改变时才会触发，不会重复触发                    │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * @see BroadcastReceiver
 * @see App#finish()
 * @author Micsig智能示波器团队
 * @version 1.0
 * @since MHO Series Oscilloscope Software
 */
public class LocaleChangeReceiver extends BroadcastReceiver {   // 语言切换广播接收器：监听系统语言变更，强制退出应用以重新加载语言资源

    /**
     * 广播接收回调方法 - 处理系统语言变更事件
     *
     * <p>当收到ACTION_LOCALE_CHANGED广播时，记录日志并调用App.finish()
     * 强制退出应用进程。应用退出后，配合BootCompletedReceiver的开机自启动
     * 机制，用户下次操作设备时应用将自动重启并加载新的语言资源。</p>
     *
     * <h4>处理流程</h4>
     * <pre>
     * onReceive(context, intent)
     *   │
     *   ├─ 判断：intent.getAction() == ACTION_LOCALE_CHANGED
     *   │
     *   ├─ Logger.e("ACTION_LOCALE_CHANGED")  // 记录语言变更事件
     *   │
     *   └─ App.finish()  // 强制退出应用
     *       ├─ MainActivity.finish()  // 关闭主界面
     *       ├─ Process.killProcess(myPid())  // 终止进程
     *       └─ System.exit(0)  // 退出JVM
     * </pre>
     *
     * <h4>注意事项</h4>
     * <ul>
     *   <li>使用Logger.e()而非Logger.i()，因为语言变更属于需要关注的异常事件</li>
     *   <li>App.finish()会强制终止进程，所有未保存的数据将丢失</li>
     *   <li>语言变更后应用不会自动重启，需要用户操作或开机自启动触发</li>
     * </ul>
     *
     * @param context 上下文对象，提供访问应用环境的接口
     * @param intent 广播意图对象，包含ACTION_LOCALE_CHANGED Action
     */
    @Override
    public void onReceive(Context context, Intent intent) {   // 广播接收回调：处理系统语言变更事件
        if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {   // 判断：是否为系统语言变更广播
            Logger.e("ACTION_LOCALE_CHANGED");   // 记录日志：使用error级别记录语言变更事件，便于排查语言相关问题
            App.finish();   // 强制退出应用：关闭MainActivity、终止进程并退出JVM，确保下次启动时加载正确的语言资源
        }   // 语言变更广播判断结束
    }   // onReceive方法结束
}   // LocaleChangeReceiver类结束
