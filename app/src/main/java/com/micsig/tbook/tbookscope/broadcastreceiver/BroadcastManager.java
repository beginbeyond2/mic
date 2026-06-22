package com.micsig.tbook.tbookscope.broadcastreceiver; // 广播接收器包，集中管理示波器所有系统广播的注册与分发

import android.content.Intent;                    // Android意图对象，用于携带广播动作与数据
import android.content.IntentFilter;              // Android意图过滤器，用于指定接收器监听的广播动作
import android.hardware.usb.UsbManager;           // Android USB管理器，提供USB设备相关动作常量
import android.net.ConnectivityManager;           // Android网络连接管理器，提供网络状态变化动作常量
import android.net.wifi.WifiManager;              // Android WiFi管理器，提供WiFi状态变化动作常量
import android.widget.ImageView;                  // Android图片视图，用于显示USB/WiFi/U盘等状态图标
import android.widget.TextView;                   // Android文本视图，用于显示时间等信息

import com.micsig.tbook.hardware.HardwareProduct; // 硬件产品信息工具类，判断设备硬件能力（如是否有电池）
import com.micsig.tbook.tbookscope.MainActivity;  // 示波器主活动，作为广播注册/注销的Context宿主
import com.micsig.tbook.tbookscope.main.dialog.DialogOk; // 通用确认对话框，用于电池低电量弹窗提醒
import com.micsig.tbook.ui.main.BatteryView;      // 自定义电池视图控件，显示电池电量与充电图标

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                         BroadcastManager                                    │
 * │                      系统广播管理器（单例）                                    │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：com.micsig.tbook.tbookscope.broadcastreceiver                    │
 * │ 所属层级：示波器应用 → 系统事件层 → 广播管理中心                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                   │
 * │   1. 以懒汉式单例统一管理示波器应用中所有 BroadcastReceiver 的注册与注销      │
 * │   2. 将系统广播（电池/USB/U盘/WiFi）与 UI 控件进行解耦绑定                    │
 * │   3. 提供各接收器 UI 控件引用的注入入口，实现广播接收与界面刷新的分离          │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                   │
 * │   · 设计模式：懒汉式单例（Lazy Singleton），延迟初始化，线程不安全            │
 * │   · 管理策略：集中注册 + 集中注销，确保生命周期与 MainActivity 同步            │
 * │   · 控件注入：通过 setter 方法将 UI 控件引用注入各 Receiver，避免直接耦合     │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：                                                                   │
 * │   系统广播 → BroadcastReceiver.onReceive() → Handler/UI控件 → 界面刷新       │
 * │       ↑                        ↑                                            │
 * │   BroadcastManager.init()  注册接收器                                        │
 * │   BroadcastManager.setXxxControl()  注入UI控件                               │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 管理的接收器：                                                                │
 * │   · BatteryChangedReceiver  — 电池电量/充电状态/低电量广播                    │
 * │   · UsbChangedReceiver      — USB连接/断开状态广播（PC联机）                  │
 * │   · UDiskChangedReceiver    — U盘挂载/卸载/弹出广播                          │
 * │   · WifiChangedReceiver     — WiFi/以太网连接状态变化广播                     │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：                                                                   │
 * │   ← MainActivity          （Context宿主，提供注册/注销能力）                   │
 * │   ← BatteryChangedReceiver（电池广播接收器）                                  │
 * │   ← UsbChangedReceiver    （USB状态广播接收器）                               │
 * │   ← UDiskChangedReceiver  （U盘挂载广播接收器）                               │
 * │   ← WifiChangedReceiver   （WiFi/网络状态广播接收器）                         │
 * │   ← HardwareProduct       （硬件能力判断）                                    │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例：                                                                   │
 * │   // 在 MainActivity.onCreate() 中初始化                                     │
 * │   BroadcastManager.getInstance().init(this);                                 │
 * │   // 在 UI 控件初始化后注入引用                                               │
 * │   BroadcastManager.getInstance().setBatteryControl(batteryView, tvTime, dlg);│
 * │   BroadcastManager.getInstance().setUsbControl(ivUsbLink);                   │
 * │   BroadcastManager.getInstance().setWifiControl(ivWifi);                     │
 * │   BroadcastManager.getInstance().setInternetControl(ivInternet);             │
 * │   BroadcastManager.getInstance().setUDiskControl(ivUDisk);                   │
 * │   // 在 MainActivity.onDestroy() 中注销                                      │
 * │   BroadcastManager.getInstance().unregisterReceiver();                       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 注意事项：                                                                   │
 * │   · 懒汉式单例非线程安全，若多线程访问需加同步（当前仅在主线程调用，无风险）    │
 * │   · unregisterReceiver() 必须在 Activity 销毁时调用，否则将引发内存泄漏       │
 * │   · init() 与 unregisterReceiver() 必须成对调用                              │
 * │   · setXxxControl() 必须在 init() 之后调用，否则接收器实例为 null            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ @author  Liwb                                                                │
 * │ @since   2022-4-14                                                          │
 * │ @version 1.0.0                                                              │
 * └──────────────────────────────────────────────────────────────────────────────┘
 */
public class BroadcastManager {

    // ==================== 单例实例 ====================

    /** 单例实例引用，初始为null，首次调用getInstance()时延迟创建 */
    private static BroadcastManager instance = null; // 懒汉式单例，延迟初始化

    /**
     * 获取 BroadcastManager 单例实例（懒汉式）。
     * <p>
     * 线程安全说明：当前仅在主线程调用，无需同步；
     * 若需多线程访问，应改为 DCL 或静态内部类方式。
     * </p>
     *
     * @return BroadcastManager 全局唯一实例
     */
    public static BroadcastManager getInstance() { // 静态工厂方法，获取单例
        if (instance == null) {                    // 判断实例是否已创建
            instance = new BroadcastManager();     // 首次调用时创建实例
        }
        return instance;                           // 返回单例实例
    }

    // ==================== 广播接收器成员 ====================

    /** 电池状态变化接收器，监听电量变化、低电量、充电状态等广播 */
    private BatteryChangedReceiver batteryChangedReceiver; // 电池广播接收器实例

    /** U盘挂载/卸载接收器，监听U盘插入、弹出、USB设备附着/分离等广播 */
    private UDiskChangedReceiver uDiskChangedReceiver;     // U盘广播接收器实例

    /** USB连接状态接收器，监听USB与PC联机/断开状态广播 */
    private UsbChangedReceiver usbChangedReceiver;         // USB状态广播接收器实例

    /** WiFi/网络状态接收器，监听WiFi开关、网络连接/断开等广播 */
    private WifiChangedReceiver wifiChangedReceiver;       // WiFi网络广播接收器实例

    // ==================== 其他成员 ====================

    /** 示波器主活动引用，作为广播注册/注销的Context宿主 */
    private MainActivity mainActivity;                     // 主活动实例，提供registerReceiver/unregisterReceiver能力

    /** USB权限请求动作常量，用于U盘接收器过滤USB权限授权广播 */
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"; // USB权限授权动作

    /**
     * 默认构造函数。
     * <p>
     * 仅由 getInstance() 内部调用，外部不应直接 new。
     * 懒汉式单例要求构造函数为 public（或 protected），但实际通过 getInstance() 控制访问。
     * </p>
     */
    public BroadcastManager() { // 默认构造函数，单例模式下由getInstance()内部调用

    }

    /**
     * 初始化广播管理器，创建并注册所有广播接收器。
     * <p>
     * 必须在 MainActivity.onCreate() 中调用，且必须在 UI 控件初始化之前完成。
     * 调用此方法后，各接收器已注册但尚未绑定 UI 控件，
     * 需再调用对应的 setXxxControl() 方法完成控件注入。
     * </p>
     * <p>
     * 注册的广播及对应动作如下：
     * <ul>
     *   <li>BatteryChangedReceiver — ACTION_BATTERY_CHANGED / ACTION_BATTERY_LOW / ACTION_BATTERY_OKAY</li>
     *   <li>UsbChangedReceiver — USB_STATE（自定义动作）</li>
     *   <li>UDiskChangedReceiver — USB_DEVICE_ATTACHED / MEDIA_MOUNTED / MEDIA_UNMOUNTED / MEDIA_EJECT / USB_DEVICE_DETACHED / USB_PERMISSION</li>
     *   <li>WifiChangedReceiver — NETWORK_STATE_CHANGED / WIFI_STATE_CHANGED / CONNECTIVITY_ACTION / SUPPLICANT_CONNECTION_CHANGE / SUPPLICANT_STATE_CHANGED</li>
     * </ul>
     * </p>
     *
     * @param mainActivity 示波器主活动实例，作为广播注册的Context宿主，
     *                     不能为null，否则将抛出NullPointerException
     */
    public void init(MainActivity mainActivity) { // 初始化方法，注册所有广播接收器
        this.mainActivity = mainActivity;         // 保存主活动引用，后续注销时需要使用


        // ---------- 1. 注册电池状态变化接收器 ----------
        batteryChangedReceiver = new BatteryChangedReceiver(); // 创建电池状态变化接收器实例
        IntentFilter intentFilter = new IntentFilter();        // 创建意图过滤器，用于指定监听的广播动作
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED); // 添加：电池电量变化广播（粘性广播，可立即获取当前电量）
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);     // 添加：电池电量低广播
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);    // 添加：电池电量从低恢复正常广播


        mainActivity.registerReceiver(batteryChangedReceiver, intentFilter); // 向系统注册电池接收器


        // ---------- 2. 注册USB连接状态接收器 ----------
        usbChangedReceiver = new UsbChangedReceiver();         // 创建USB连接状态接收器实例
        IntentFilter intentFilter3 = new IntentFilter();       // 创建意图过滤器（命名intentFilter3为历史遗留）
        intentFilter3.addAction(UsbChangedReceiver.ACTION);    // 添加：USB状态变化动作（android.hardware.usb.action.USB_STATE）
        mainActivity.registerReceiver(usbChangedReceiver, intentFilter3); // 向系统注册USB状态接收器

        // ---------- 3. 注册U盘挂载/卸载接收器 ----------
        uDiskChangedReceiver = new UDiskChangedReceiver();     // 创建U盘变化接收器实例
        IntentFilter usbDeviceStateFilter = new IntentFilter(); // 创建意图过滤器，用于USB设备和存储介质状态
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); // 添加：USB设备附着广播
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);           // 添加：外部存储介质挂载广播
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);         // 添加：外部存储介质卸载广播
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_EJECT);             // 添加：外部存储介质弹出广播
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED); // 添加：USB设备分离广播
        usbDeviceStateFilter.addAction(ACTION_USB_PERMISSION);                 // 添加：USB权限授权结果广播
        usbDeviceStateFilter.addDataScheme("file");    // 设置数据方案为"file"，过滤仅包含文件URI的广播
        mainActivity.registerReceiver(uDiskChangedReceiver, usbDeviceStateFilter); // 向系统注册U盘接收器

        // ---------- 4. 注册WiFi/网络状态接收器 ----------
        IntentFilter filter = new IntentFilter();                              // 创建意图过滤器，用于网络状态相关广播
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);            // 添加：网络连接状态变化广播
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);               // 添加：WiFi开关状态变化广播
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);              // 添加：网络连接性变化广播
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);     // 添加：WPA supplicant连接变化广播
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);         // 添加：WPA supplicant状态变化广播
        wifiChangedReceiver = new WifiChangedReceiver();                       // 创建WiFi状态接收器实例
        mainActivity.registerReceiver(wifiChangedReceiver, filter);            // 向系统注册WiFi接收器
    }


    /**
     * 注销所有已注册的广播接收器。
     * <p>
     * 必须在 MainActivity.onDestroy() 中调用，防止内存泄漏。
     * 如果未调用此方法，已注册的 BroadcastReceiver 将持有 Activity 引用，
     * 导致 Activity 无法被 GC 回收，引发严重的内存泄漏问题。
     * </p>
     * <p>
     * 注意：必须在 init() 之后调用，否则各接收器引用为 null 将抛出异常。
     * </p>
     */
    public void unregisterReceiver() {                              // 注销所有广播接收器

        mainActivity.unregisterReceiver(batteryChangedReceiver);    // 注销电池状态变化接收器

        mainActivity.unregisterReceiver(usbChangedReceiver);        // 注销USB连接状态接收器
        mainActivity.unregisterReceiver(wifiChangedReceiver);       // 注销WiFi/网络状态接收器
        mainActivity.unregisterReceiver(uDiskChangedReceiver);      // 注销U盘挂载/卸载接收器
    }

    /**
     * 为电池状态接收器注入 UI 控件引用。
     * <p>
     * 调用此方法后，BatteryChangedReceiver 将能够：
     * <ul>
     *   <li>通过 BatteryView 显示电池电量与充电图标</li>
     *   <li>通过 TextView 显示当前系统时间（每20秒刷新）</li>
     *   <li>通过 DialogOk 在低电量时弹出提醒对话框</li>
     * </ul>
     * </p>
     *
     * @param tvBattery 自定义电池视图控件，显示电池电量百分比与充电图标；
     *                  若设备无电池硬件（HardwareProduct.isBattery()=false），该控件将被隐藏
     * @param tvTime    时间文本视图，用于显示当前系统时间（HH:mm格式）
     * @param dialogOk  通用确认对话框，用于电池低电量时弹出提醒；
     *                  充电后自动关闭低电量弹窗
     */
    public void setBatteryControl(BatteryView tvBattery, TextView tvTime, DialogOk dialogOk) { // 注入电池相关UI控件

        batteryChangedReceiver.setBatteryControl(tvBattery);   // 设置电池视图控件引用
        batteryChangedReceiver.setTimeControl(tvTime);         // 设置时间文本控件引用，同时启动时间刷新线程
        batteryChangedReceiver.setDialogOk(dialogOk);          // 设置低电量弹窗对话框引用

    }

    /**
     * 为USB连接状态接收器注入 UI 控件引用。
     * <p>
     * 调用此方法后，UsbChangedReceiver 将根据USB与PC的连接状态
     * 控制图标的显示/隐藏：连接时显示，断开时隐藏。
     * </p>
     *
     * @param tvUsbPcLink USB联机状态图标，USB与PC连接时显示，断开时隐藏
     */
    public void setUsbControl(ImageView tvUsbPcLink) {         // 注入USB联机状态图标
        usbChangedReceiver.setUsbControl(tvUsbPcLink);         // 将ImageView引用传递给USB接收器
    }

    /**
     * 为WiFi状态接收器注入 WiFi 图标控件引用。
     * <p>
     * 调用此方法后，WifiChangedReceiver 将根据WiFi连接状态
     * 控制WiFi图标的显示/隐藏：WiFi已连接时显示，否则隐藏。
     * </p>
     *
     * @param tvWifi WiFi连接状态图标，WiFi已连接时显示，否则隐藏
     */
    public void setWifiControl(ImageView tvWifi) {             // 注入WiFi状态图标
        wifiChangedReceiver.setWifiControl(tvWifi);            // 将ImageView引用传递给WiFi接收器
    }

    /**
     * 为WiFi状态接收器注入 互联网连接图标控件引用。
     * <p>
     * 调用此方法后，WifiChangedReceiver 将根据以太网连接状态
     * 控制互联网图标的显示/隐藏：以太网已连接时显示，否则隐藏。
     * </p>
     *
     * @param tvInternet 互联网连接状态图标，以太网已连接时显示，否则隐藏
     */
    public void setInternetControl(ImageView tvInternet) {     // 注入互联网连接状态图标
        wifiChangedReceiver.setInternetControl(tvInternet);    // 将ImageView引用传递给WiFi接收器（同时管理以太网图标）
    }

    /**
     * 为U盘状态接收器注入 UI 控件引用。
     * <p>
     * 调用此方法后，UDiskChangedReceiver 将根据U盘挂载/卸载状态
     * 控制U盘图标的显示/隐藏：U盘已挂载时显示，卸载时隐藏。
     * </p>
     *
     * @param tvUDisk U盘状态图标，U盘已挂载时显示，卸载/弹出时隐藏
     */
    public void setUDiskControl(ImageView tvUDisk) {           // 注入U盘状态图标
        uDiskChangedReceiver.setUDiskControl(tvUDisk);         // 将ImageView引用传递给U盘接收器
    }



}
