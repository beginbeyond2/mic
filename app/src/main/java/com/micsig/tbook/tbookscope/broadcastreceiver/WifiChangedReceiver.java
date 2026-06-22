package com.micsig.tbook.tbookscope.broadcastreceiver; // 网络状态广播接收器所在包

import android.content.BroadcastReceiver; // 广播接收器基类，用于接收系统或应用发出的广播意图
import android.content.Context;            // Android上下文，提供系统服务访问和环境信息
import android.content.Intent;             // 意图对象，携带广播的动作和数据
import android.net.ConnectivityManager;    // 连接管理器，用于查询网络连接状态
import android.net.NetworkInfo;            // 网络信息类，描述当前网络连接的详细状态
import android.view.View;                  // 视图基类，提供可见性常量（VISIBLE/GONE等）
import android.widget.ImageView;           // 图像视图控件，用于显示网络状态图标

/*
 * ╔════════════════════════════════════════════════════════════════════════════════╗
 * ║                         WifiChangedReceiver                                  ║
 * ║                    WiFi / 网络状态变化广播接收器                               ║
 * ╠════════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  【模块定位】                                                                ║
 * ║    所属模块：tbookscope → broadcastreceiver                                 ║
 * ║    模块职责：监听系统网络连接状态变化广播，驱动UI层更新网络状态图标显示        ║
 * ║                                                                              ║
 * ║  【核心职责】                                                                ║
 * ║    1. 注册并接收系统网络连接变化广播（CONNECTIVITY_ACTION）                   ║
 * ║    2. 区分当前网络类型：以太网(1) / WiFi(2) / 无连接(0)                      ║
 * ║    3. 根据网络类型控制 ImageView 图标的可见性，反映实时网络状态               ║
 * ║                                                                              ║
 * ║  【架构设计】                                                                ║
 * ║    继承体系：BroadcastReceiver → WifiChangedReceiver                         ║
 * ║    设计模式：观察者模式（Observer Pattern）                                   ║
 * ║      - 被观察者：Android 系统ConnectivityService                             ║
 * ║      - 观察者：本接收器（WifiChangedReceiver）                                ║
 * ║      - 通知方式：系统发送广播 → onReceive() 回调 → 更新UI图标                ║
 * ║    耦合方式：通过 setter 注入 ImageView 控件引用，实现与Activity/Fragment     ║
 * ║              的松耦合绑定                                                    ║
 * ║                                                                              ║
 * ║  【数据流向】                                                                ║
 * ║    Android系统                                                               ║
 * ║      │ (网络状态变化)                                                        ║
 * ║      ▼                                                                       ║
 * ║    发送CONNECTIVITY_ACTION广播                                               ║
 * ║      │                                                                       ║
 * ║      ▼                                                                       ║
 * ║    WifiChangedReceiver.onReceive()                                           ║
 * ║      │                                                                       ║
 * ║      ├─→ isNetworkAvailable() 查询当前网络类型                               ║
 * ║      │       │                                                               ║
 * ║      │       ▼                                                               ║
 * ║      │   返回类型码：1(以太网) / 2(WiFi) / 0(无网络)                         ║
 * ║      │                                                                       ║
 * ║      ▼                                                                       ║
 * ║    更新ImageView可见性：tvInternet / tvWifi                                   ║
 * ║      │                                                                       ║
 * ║      ▼                                                                       ║
 * ║    用户界面刷新（网络图标显示/隐藏）                                          ║
 * ║                                                                              ║
 * ║  【返回值编码】                                                              ║
 * ║    ┌──────────┬──────────┬──────────────────────────────┐                   ║
 * ║    │ 返回值   │ 含义     │ UI行为                       │                   ║
 * ║    ├──────────┼──────────┼──────────────────────────────┤                   ║
 * ║    │ 0        │ 无网络   │ 隐藏以太网图标和WiFi图标     │                   ║
 * ║    │ 1        │ 以太网   │ 显示以太网图标               │                   ║
 * ║    │ 2        │ WiFi     │ 显示WiFi图标                 │                   ║
 * ║    └──────────┴──────────┴──────────────────────────────┘                   ║
 * ║                                                                              ║
 * ║  【依赖关系】                                                                ║
 * ║    - Android SDK：BroadcastReceiver, ConnectivityManager, NetworkInfo        ║
 * ║    - UI层：ImageView（通过setter注入，由宿主Activity/Fragment提供）          ║
 * ║    - 系统服务：Context.CONNECTIVITY_SERVICE                                  ║
 * ║                                                                              ║
 * ║  【使用示例】                                                                ║
 * ║    // 在Activity中注册和使用                                                 ║
 * ║    WifiChangedReceiver receiver = new WifiChangedReceiver();                 ║
 * ║    receiver.setInternetControl(imgInternet);  // 注入以太网图标控件          ║
 * ║    receiver.setWifiControl(imgWifi);           // 注入WiFi图标控件           ║
 * ║    IntentFilter filter = new IntentFilter(                                   ║
 * ║        ConnectivityManager.CONNECTIVITY_ACTION);                             ║
 * ║    registerReceiver(receiver, filter);  // 注册广播接收器                    ║
 * ║    // ... 在onDestroy中反注册                                                ║
 * ║    unregisterReceiver(receiver);                                             ║
 * ║                                                                              ║
 * ║  【注意事项】                                                                ║
 * ║    - Android 7.0+ 已废弃 CONNECTIVITY_ACTION 广播的静态注册，                ║
 * ║      必须使用动态注册（代码中注册）                                          ║
 * ║    - NetworkInfo 自 Android 10 起已废弃，建议迁移至                          ║
 * ║      NetworkCapabilities API                                                 ║
 * ║    - isNetworkAvailable() 为静态方法，可独立调用，不依赖接收器实例           ║
 * ║                                                                              ║
 * ║  【作者】Liwb    【创建日期】2022-4-14                                       ║
 * ╚════════════════════════════════════════════════════════════════════════════════╝
 */
public class WifiChangedReceiver extends BroadcastReceiver { // 继承BroadcastReceiver，成为系统广播的监听者

    /** 以太网网络状态图标控件，用于显示/隐藏以太网连接指示图标 */
    private ImageView tvInternet; // 以太网图标ImageView引用，由外部通过setInternetControl注入

    /** WiFi网络状态图标控件，用于显示/隐藏WiFi连接指示图标 */
    private ImageView tvWifi; // WiFi图标ImageView引用，由外部通过setWifiControl注入

    /**
     * 设置以太网网络状态图标控件引用。
     * <p>
     * 通过此方法将宿主Activity/Fragment中的ImageView控件注入到接收器中，
     * 以便在接收到网络状态变化广播时，能够直接控制该图标的可见性。
     * </p>
     *
     * @param tvInternet 以太网状态图标对应的ImageView控件，由宿主UI提供
     */
    public void setInternetControl(ImageView tvInternet) { // setter方法：注入以太网图标控件
        this.tvInternet = tvInternet; // 将外部传入的ImageView引用赋值给成员变量
    }

    /**
     * 设置WiFi网络状态图标控件引用。
     * <p>
     * 通过此方法将宿主Activity/Fragment中的ImageView控件注入到接收器中，
     * 以便在接收到网络状态变化广播时，能够直接控制该图标的可见性。
     * </p>
     *
     * @param tvWifi WiFi状态图标对应的ImageView控件，由宿主UI提供
     */
    public void setWifiControl(ImageView tvWifi) { // setter方法：注入WiFi图标控件
        this.tvWifi = tvWifi; // 将外部传入的ImageView引用赋值给成员变量
    }

    /**
     * 广播接收回调方法，当系统网络连接状态发生变化时由Android框架调用。
     * <p>
     * 处理流程：
     * <ol>
     *   <li>判断广播Action是否为网络连接变化相关（CONNECTIVITY_ACTION）</li>
     *   <li>调用 {@link #isNetworkAvailable(Context)} 查询当前网络类型</li>
     *   <li>根据返回的网络类型码，更新对应图标的可见性：
     *     <ul>
     *       <li>1（以太网）→ 显示以太网图标</li>
     *       <li>2（WiFi）→ 显示WiFi图标</li>
     *       <li>0（无网络）→ 隐藏所有网络图标</li>
     *     </ul>
     *   </li>
     * </ol>
     * </p>
     *
     * @param context 接收广播时的上下文环境，用于获取系统服务
     * @param intent  携带广播动作和数据的意图对象
     */
    @Override // 重写BroadcastReceiver的onReceive方法
    public void onReceive(Context context, Intent intent) { // 系统广播回调入口
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) // 判断广播Action是否为系统定义的网络连接变化Action
                || intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) { // 兼容旧版Action字符串（部分系统使用此值）
            switch (isNetworkAvailable(context)) { // 根据当前网络可用性类型进行分支处理
                case 1: // 返回值1：当前为以太网连接
                    tvInternet.setVisibility(View.VISIBLE); // 显示以太网网络状态图标
                    break; // 跳出switch
                case 2: // 返回值2：当前为WiFi连接
                    tvWifi.setVisibility(View.VISIBLE); // 显示WiFi网络状态图标
                    break; // 跳出switch
                case 0: // 返回值0：当前无网络连接
                    tvInternet.setVisibility(View.GONE); // 隐藏以太网图标（不占布局空间）
                    tvWifi.setVisibility(View.GONE); // 隐藏WiFi图标（不占布局空间）
                    break; // 跳出switch
            } // switch结束
        } // if结束：仅处理网络连接变化相关的广播
    } // onReceive方法结束

    /**
     * 检测当前设备的网络连接状态及类型。
     * <p>
     * 通过 {@link ConnectivityManager} 分别查询以太网和WiFi的连接状态，
     * 并返回对应的类型编码。该方法是静态方法，可独立于接收器实例使用。
     * </p>
     * <p>
     * 注意：{@link NetworkInfo} 自 Android API 29 起已废弃，
     * 建议后续迁移至 {@code NetworkCapabilities} + {@code NetworkCallback} 方案。
     * </p>
     *
     * @param context 应用上下文，用于获取ConnectivityManager系统服务
     * @return 网络类型编码：
     *         <ul>
     *           <li>1 - 以太网已连接</li>
     *           <li>2 - WiFi已连接</li>
     *           <li>0 - 无可用网络连接</li>
     *         </ul>
     *         优先级：以太网 > WiFi > 无网络（以太网连接时不再检查WiFi）
     */
    public static int isNetworkAvailable(Context context) { // 静态工具方法：查询当前网络连接类型
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); // 获取系统连接管理器服务实例
        NetworkInfo ethNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET); // 查询以太网类型的网络连接信息
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI); // 查询WiFi类型的网络连接信息

        if (ethNetInfo != null && ethNetInfo.isConnected()) { // 以太网信息非空且已连接
            return 1; // 返回1：以太网已连接（优先级最高）
        } else if (wifiNetInfo != null && wifiNetInfo.isConnected()) { // WiFi信息非空且已连接
            return 2; // 返回2：WiFi已连接
        } else { // 既无以太网也无WiFi连接
            return 0; // 返回0：无可用网络连接
        } // 网络类型判断结束
    } // isNetworkAvailable方法结束
} // WifiChangedReceiver类结束
