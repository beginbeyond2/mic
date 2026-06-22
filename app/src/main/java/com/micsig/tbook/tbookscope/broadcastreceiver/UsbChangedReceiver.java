package com.micsig.tbook.tbookscope.broadcastreceiver; // USB状态广播接收器所属包

import android.content.BroadcastReceiver; // 导入广播接收器基类
import android.content.Context;            // 导入上下文类，用于访问应用环境信息
import android.content.Intent;             // 导入意图类，用于传递广播动作与数据
import android.view.View;                  // 导入视图基类，用于控制可见性常量
import android.widget.ImageView;           // 导入图像视图控件，用于显示USB连接图标

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          UsbChangedReceiver                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: broadcastreceiver 包 │ MHO系列示波器Android应用                    ║
 * ║ 核心职责: 监听USB PC连接状态变化，并驱动UI层更新USB连接图标显示               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 架构设计:                                                                   ║
 * ║   - 继承自 Android BroadcastReceiver，采用观察者模式                         ║
 * ║   - 通过 setUsbControl() 注入 ImageView 引用，实现广播接收器与UI控件的解耦   ║
 * ║   - onReceive() 在主线程回调，可直接操作UI                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 数据流向:                                                                   ║
 * ║   Android系统(USB_STATE广播) → onReceive() → ImageView可见性切换            ║
 * ║   ┌──────────────┐    广播Intent     ┌──────────────────────┐               ║
 * ║   │ Android系统   │ ──────────────→  │ UsbChangedReceiver   │               ║
 * ║   │ USB_STATE事件 │  action+extras   │   onReceive()        │               ║
 * ║   └──────────────┘                   └──────────┬───────────┘               ║
 * ║                                                  │                          ║
 * ║                                     VISIBLE / GOTO                          ║
 * ║                                                  ▼                          ║
 * ║                                      ┌────────────────────┐                 ║
 * ║                                      │ ImageView          │                 ║
 * ║                                      │ (tvUsbPcLink图标)  │                 ║
 * ║                                      └────────────────────┘                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 依赖关系:                                                                   ║
 * ║   - Android SDK: BroadcastReceiver, Context, Intent, View, ImageView        ║
 * ║   - 外部注入: 通过 setUsbControl() 由调用方提供 ImageView 实例              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 使用示例:                                                                   ║
 * ║   UsbChangedReceiver receiver = new UsbChangedReceiver();                  ║
 * ║   receiver.setUsbControl(usbIconImageView);                                ║
 * ║   registerReceiver(receiver, new IntentFilter(UsbChangedReceiver.ACTION)); ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ @author Liwb                                                                ║
 * ║ @since  2022-4-14                                                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class UsbChangedReceiver extends BroadcastReceiver { // 继承BroadcastReceiver，注册后可接收系统USB状态广播

    /**
     * USB状态变化的广播Action常量
     * <p>
     * 对应Android系统广播：android.hardware.usb.action.USB_STATE
     * 当设备的USB连接状态发生变化时（如插入/拔出USB线），系统会发送此广播。
     * Intent中携带的extras：
     *   - "connected" (boolean): USB是否已连接
     *   - "configured" (boolean): USB是否已配置
     * </p>
     */
    public final static String ACTION = "android.hardware.usb.action.USB_STATE"; // 系统USB状态广播Action

    /**
     * USB PC连接状态图标控件引用
     * <p>
     * 用于在UI上显示/隐藏USB PC连接图标。
     * 当USB连接到PC时设为VISIBLE，断开时设为GONE。
     * 通过 setUsbControl() 方法由外部注入。
     * </p>
     */
    private ImageView tvUsbPcLink; // USB PC连接指示图标

    /**
     * 注入USB PC连接图标的ImageView引用
     * <p>
     * 调用方在注册广播接收器之前，必须调用此方法注入ImageView实例，
     * 以便 onReceive() 回调中能够更新图标的可见性。
     * </p>
     *
     * @param tvUsbPcLink 用于显示USB PC连接状态的ImageView控件，
     *                    连接时显示，断开时隐藏
     */
    public void setUsbControl(ImageView tvUsbPcLink) { // 设置USB图标控件引用
        this.tvUsbPcLink = tvUsbPcLink;               // 将外部传入的ImageView引用保存到成员变量
    }

    /**
     * 广播接收回调，当接收到USB状态变化广播时由系统调用
     * <p>
     * 处理流程：
     *   1. 获取广播的Action字符串
     *   2. 空值防御：Action为null时直接返回
     *   3. 匹配USB_STATE Action
     *   4. 从Intent中读取"connected"布尔值
     *   5. 根据连接状态设置ImageView的可见性
     * </p>
     *
     * @param context 广播上下文，由系统传入，本方法中未使用
     * @param intent  携带广播动作和数据的Intent，包含"connected"布尔额外数据
     */
    @Override
    public void onReceive(Context context, Intent intent) {        // 系统回调：接收到广播时执行
        String action = intent.getAction();                         // 从Intent中提取广播Action
        if (action == null) return;                                 // 防御性检查：Action为null则直接返回，避免NPE
        if (action.equalsIgnoreCase(ACTION)) {                      // 忽略大小写匹配USB_STATE广播Action
            boolean connected = intent.getBooleanExtra("connected", false); // 读取USB连接状态，默认false（未连接）
            tvUsbPcLink.setVisibility(connected ? View.VISIBLE : View.GONE); // 已连接→显示图标；未连接→隐藏图标（不占布局空间）
        }
    }
}
