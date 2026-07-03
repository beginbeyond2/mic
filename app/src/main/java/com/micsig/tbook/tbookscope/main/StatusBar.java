package com.micsig.tbook.tbookscope.main;

import android.content.Context; // 导入Android上下文类
import android.view.View; // 导入Android视图基类
import android.widget.ImageView; // 导入Android图像视图组件
import android.widget.TextView; // 导入Android文本视图组件

import com.micsig.tbook.tbookscope.LoadCache; // 导入加载缓存类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主活动类
import com.micsig.tbook.tbookscope.R; // 导入资源文件引用类
import com.micsig.tbook.tbookscope.broadcastreceiver.BroadcastManager; // 导入广播管理器类
import com.micsig.tbook.tbookscope.broadcastreceiver.WifiChangedReceiver; // 导入WiFi状态变化接收器类
import com.micsig.tbook.tbookscope.main.dialog.DialogOk; // 导入确认对话框类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava枚举类
import com.micsig.tbook.tbookscope.tools.UsbUtils; // 导入USB工具类
import com.micsig.tbook.ui.main.BatteryView; // 导入电池视图组件类

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava3非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava3消费者接口


/**
 * @auother Liwb
 * @description:
 * @data:2023-10-7 9:59
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      状态栏管理类                                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * 【模块定位】                                                                  │
 *   示波器UI系统 - 顶部状态栏控制器                                               │
 * 【核心职责】                                                                  │
 *   1. 管理状态栏图标显示（USB连接、U盘、WiFi、有线网络、电池、时间）               │
 *   2. 监听系统广播更新状态栏图标                                                  │
 *   3. 响应RxJava事件总线更新缓存数据                                              │
 * 【架构设计】                                                                  │
 *   UI控制器类，持有主活动引用和各状态图标的视图引用，通过广播接收器监听系统状态     │
 * 【数据流向】                                                                  │
 *   系统广播 → BroadcastManager → 状态栏图标更新                                   │
 *   LoadCache事件 → RxBus → setCache() → WiFi/网络图标更新                         │
 * 【依赖关系】                                                                  │
 *   依赖：MainActivity、BroadcastManager、RxBus、LoadCache、WifiChangedReceiver    │
 *   被依赖：MainActivity                                                            │
 * 【使用场景】                                                                  │
 *   主活动初始化时创建StatusBar实例，注册广播接收器，实时显示设备连接和电量状态     │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class StatusBar {
    /** USB PC连接状态图标 */ // USB连接状态图标视图
    private ImageView tvUsbPcLink, tvUDisk, tvWifi, tvInternet; // 状态图标视图控件

    /** 电池电量视图 */ // 电池电量显示视图
    private BatteryView tvBattery; // 电池电量视图控件

    /** 时间显示文本 */ // 时间显示文本视图
    private TextView tvTime; // 时间显示文本控件

    /** 确认对话框 */ // 确认对话框引用
    private DialogOk dialogOk; // 确认对话框控件

    /** 应用上下文 */ // 应用程序上下文引用
    private Context context; // 上下文对象

    /**
     * 构造函数：初始化状态栏控制器
     *
     * @param mainActivity 主活动实例，用于获取上下文和查找视图控件
     */
    public StatusBar(MainActivity mainActivity){
        this.context=mainActivity.getApplicationContext(); // 获取应用上下文
        tvUsbPcLink = (ImageView) mainActivity.findViewById(R.id.usbPcLink); // 查找USB连接图标
        tvUDisk = (ImageView) mainActivity.findViewById(R.id.uDisk); // 查找U盘图标
        tvBattery = (BatteryView) mainActivity.findViewById(R.id.battery); // 查找电池视图
        tvTime = (TextView) mainActivity.findViewById(R.id.time); // 查找时间文本
        tvWifi = mainActivity.findViewById(R.id.wifi); // 查找WiFi图标
        tvInternet = mainActivity.findViewById(R.id.lan); // 查找有线网络图标
        initControl(); // 初始化控件监听
    }

    /**
     * 初始化控件监听
     * 订阅RxJava事件总线，监听LoadCache事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅加载缓存事件

    }

    /** LoadCache事件消费者 */ // RxJava消费者对象
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 创建LoadCache消费者
        /**
         * 接收LoadCache事件回调
         *
         * @param loadCache 加载缓存数据对象
         * @throws Exception 可能抛出的异常
         */
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收事件处理
            setCache(); // 更新缓存状态
        }
    };

    /**
     * 设置缓存状态
     * 根据网络可用性更新WiFi和有线网络图标显示状态
     */
    private void setCache() {
        tvInternet.setVisibility(View.GONE); // 隐藏有线网络图标
        tvWifi.setVisibility(View.GONE); // 隐藏WiFi图标
        switch (WifiChangedReceiver.isNetworkAvailable(context)) { // 检查网络可用性
            case 1: // 有线网络可用
                tvInternet.setVisibility(View.VISIBLE); // 显示有线网络图标
                break; // 跳出switch
            case 2: // WiFi网络可用
                tvWifi.setVisibility(View.VISIBLE); // 显示WiFi图标
                break; // 跳出switch
            case 0: // 无网络连接
                tvInternet.setVisibility(View.GONE); // 隐藏有线网络图标
                tvWifi.setVisibility(View.GONE); // 隐藏WiFi图标
                break; // 跳出switch
        }

    }

    /**
     * 设置广播接收器
     * 注册各类系统状态广播接收器，监听电池、网络、U盘、USB连接状态变化
     *
     * @param mainActivity 主活动实例，用于初始化广播管理器
     */
    public void setBroadcastReceiver(MainActivity mainActivity) {
        BroadcastManager.getInstance().init(mainActivity); // 初始化广播管理器
        dialogOk = mainActivity.getMainViewGroup().findViewById(R.id.dialogOk); // 查找确认对话框
        BroadcastManager.getInstance().setBatteryControl(tvBattery, tvTime, dialogOk); // 设置电池状态监听
        BroadcastManager.getInstance().setInternetControl(tvInternet); // 设置有线网络状态监听
        BroadcastManager.getInstance().setUDiskControl(tvUDisk); // 设置U盘状态监听
        BroadcastManager.getInstance().setWifiControl(tvWifi); // 设置WiFi状态监听
        BroadcastManager.getInstance().setUsbControl(tvUsbPcLink); // 设置USB连接状态监听
    }

    /**
     * 刷新U盘状态
     * 根据当前系统版本检查U盘是否存在，并更新图标显示状态
     * BUG id:7880
     */
    public void refreshUdiskIcon() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // 检查Android版本是否>=R(30)
            boolean b = UsbUtils.UdiskExist(context); // 检查U盘是否存在
//            String  s = "usb isExist:"+ b; // 调试日志字符串
//            Logger.i(Command.TAG,s); // 输出调试日志
            tvUDisk.setVisibility(b ? View.VISIBLE : View.GONE); // 根据U盘存在状态设置图标可见性

        }
    }
}