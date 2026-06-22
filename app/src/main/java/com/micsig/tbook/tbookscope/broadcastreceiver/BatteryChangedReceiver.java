package com.micsig.tbook.tbookscope.broadcastreceiver; // 包声明：电池变化广播接收器所属包路径

import android.content.BroadcastReceiver; // 导入广播接收器基类，用于接收系统广播
import android.content.Context;           // 导入上下文类，用于访问应用环境和资源
import android.content.Intent;            // 导入意图类，用于携带广播数据和动作标识
import android.os.BatteryManager;         // 导入电池管理器，提供电池状态常量和查询接口
import android.os.Handler;                // 导入Handler类，用于线程间消息传递和UI更新
import android.os.Message;                // 导入Message类，用于封装Handler传递的消息数据
import android.view.View;                 // 导入View类，用于控制视图的可见性
import android.widget.TextView;           // 导入TextView类，用于显示时间文本

import com.micsig.tbook.hardware.HardwareProduct;                // 导入硬件产品类，判断设备是否支持电池
import com.micsig.tbook.tbookscope.R;                            // 导入资源类，访问字符串等资源ID
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;         // 导入确认对话框类，用于低电量弹窗提示
import com.micsig.tbook.tbookscope.tools.Tools;                  // 导入工具类，提供时间格式判断等方法
import com.micsig.tbook.tbookscope.util.DToast;                  // 导入自定义Toast工具类，用于低电量Toast提示
import com.micsig.tbook.ui.main.BatteryView;                     // 导入电池视图控件，用于显示电池图标和电量

import java.util.Calendar;   // 导入日历类，用于获取当前系统时间
import java.util.Objects;    // 导入对象工具类，用于安全地比较字符串

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    BatteryChangedReceiver 类说明文档                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                             ║
 * ║ 【模块定位】                                                                ║
 * ║   电池状态广播接收器 - MHO系列示波器Android应用电池监控组件                  ║
 * ║   位于 broadcastreceiver 包，负责监听系统电池状态变化广播                    ║
 * ║   并将电池信息转发至UI层进行可视化展示                                       ║
 * ║                                                                             ║
 * ║ 【核心职责】                                                                ║
 * ║   1. 监听电池电量变化（充电/放电/充满状态切换）                              ║
 * ║   2. 更新BatteryView电池视图（电量百分比、充电图标切换）                     ║
 * ║   3. 低电量弹窗提示（电量低于20%时弹出DialogOk警告）                        ║
 * ║   4. 时间显示更新（每20秒刷新一次状态栏时间）                                ║
 * ║   5. Handler消息处理机制（将广播事件转化为UI更新操作）                       ║
 * ║                                                                             ║
 * ║ 【架构设计】                                                                ║
 * ║   ┌─────────────────────────────────────────────────────────────────┐       ║
 * ║   │                  BatteryChangedReceiver                        │       ║
 * ║   │  ┌──────────────────────────────────────────────────────────┐   │       ║
 * ║   │  │                  外部接口层                               │   │       ║
 * ║   │  │  setBatteryControl() │ setDialogOk() │ setTimeControl()  │   │       ║
 * ║   │  └──────────────────────────────────────────────────────────┘   │       ║
 * ║   │                            ↓                                   │       ║
 * ║   │  ┌──────────────────────────────────────────────────────────┐   │       ║
 * ║   │  │                  广播接收层                               │   │       ║
 * ║   │  │  onReceive() → 解析电池Intent → 构造Message → 发送      │   │       ║
 * ║   │  └──────────────────────────────────────────────────────────┘   │       ║
 * ║   │                            ↓                                   │       ║
 * ║   │  ┌──────────────────────────────────────────────────────────┐   │       ║
 * ║   │  │                  消息处理层（Handler）                    │   │       ║
 * ║   │  │  MSG_UPDATETIME │ MSG_BATTERY_CHARGE │ MSG_BATTERY_     │   │       ║
 * ║   │  │  DISCHARGE │ MSG_BATTERY_SELF_UPDATE │ MSG_TIP_DISPLAY  │   │       ║
 * ║   │  └──────────────────────────────────────────────────────────┘   │       ║
 * ║   │                            ↓                                   │       ║
 * ║   │  ┌──────────────────────────────────────────────────────────┐   │       ║
 * ║   │  │                  UI更新层                                 │   │       ║
 * ║   │  │  BatteryView.setLevel() │ BatteryView.setIcon() │        │   │       ║
 * ║   │  │  DialogOk.setData() │ TextView.setText()                 │   │       ║
 * ║   │  └──────────────────────────────────────────────────────────┘   │       ║
 * ║   └─────────────────────────────────────────────────────────────────┘       ║
 * ║                                                                             ║
 * ║ 【数据流向】                                                                ║
 * ║   系统广播(ACTION_BATTERY_CHANGED)                                          ║
 * ║       → onReceive() 解析Intent中的电量/状态/充电方式                        ║
 * ║       → Handler.sendMessage() 将数据投递到主线程                            ║
 * ║       → handleMessage() 根据消息类型执行UI更新                              ║
 * ║       → BatteryView/DialogOk/TextView 执行界面刷新                          ║
 * ║                                                                             ║
 * ║ 【消息处理流程图】                                                          ║
 * ║   ┌───────────────┐                                                         ║
 * ║   │  onReceive()  │                                                         ║
 * ║   └───────┬───────┘                                                         ║
 * ║           │ 解析电池状态                                                     ║
 * ║     ┌─────┴──────┐                                                          ║
 * ║     ↓            ↓                                                          ║
 * ║  充电状态     放电状态                                                       ║
 * ║     │            │                                                          ║
 * ║     ↓            ↓                                                          ║
 * ║  MSG_BATTERY  MSG_BATTERY                                                   ║
 * ║  _CHARGE      _DISCHARGE                                                    ║
 * ║     │            │                                                          ║
 * ║     ↓            ↓                                                          ║
 * ║  设置充电图标  设置放电图标                                                  ║
 * ║  更新电量      更新电量                                                      ║
 * ║  关闭低电量    电量<20%弹出                                                  ║
 * ║     弹窗        低电量弹窗                                                   ║
 * ║                                                                             ║
 * ║ 【依赖关系】                                                                ║
 * ║   ┌──────────────────────┐     ┌──────────────────────┐                     ║
 * ║   │ BatteryView          │←────│ BatteryChangedRecv   │                     ║
 * ║   │ 电池视图控件         │     │ 电池变化接收器       │                     ║
 * ║   └──────────────────────┘     └──────────────────────┘                     ║
 * ║   ┌──────────────────────┐     ┌──────────────────────┐                     ║
 * ║   │ DialogOk             │←────│ BatteryChangedRecv   │                     ║
 * ║   │ 确认对话框           │     │ 电池变化接收器       │                     ║
 * ║   └──────────────────────┘     └──────────────────────┘                     ║
 * ║   ┌──────────────────────┐     ┌──────────────────────┐                     ║
 * ║   │ HardwareProduct      │←────│ BatteryChangedRecv   │                     ║
 * ║   │ 硬件产品特性判断     │     │ 电池变化接收器       │                     ║
 * ║   └──────────────────────┘     └──────────────────────┘                     ║
 * ║   ┌──────────────────────┐     ┌──────────────────────┐                     ║
 * ║   │ DToast               │←────│ BatteryChangedRecv   │                     ║
 * ║   │ 自定义Toast提示      │     │ 电池变化接收器       │                     ║
 * ║   └──────────────────────┘     └──────────────────────┘                     ║
 * ║                                                                             ║
 * ║ 【使用示例】                                                                ║
 * ║   // 1. 创建并注册广播接收器                                                ║
 * ║   BatteryChangedReceiver receiver = new BatteryChangedReceiver();           ║
 * ║   receiver.setBatteryControl(batteryView);                                  ║
 * ║   receiver.setDialogOk(dialogOk);                                           ║
 * ║   receiver.setTimeControl(tvTime);                                          ║
 * ║   // 2. 在Activity中注册广播                                               ║
 * ║   registerReceiver(receiver,                                                ║
 * ║       new IntentFilter(Intent.ACTION_BATTERY_CHANGED));                     ║
 * ║                                                                             ║
 * ║ 【注意事项】                                                                ║
 * ║   1. ACTION_BATTERY_CHANGED为粘性广播，注册后立即收到一次                   ║
 * ║   2. HardwareProduct.isBattery()返回false时，电池视图隐藏且不处理电池广播   ║
 * ║   3. 低电量阈值为20%，低于此值会弹出DialogOk警告对话框                      ║
 * ║   4. 时间更新线程每20秒发送一次MSG_UPDATETIME消息                           ║
 * ║   5. 部分消息类型（MSG_TIP_DISPLAY_TB、MSG_TIMEBASE等）当前已注释停用       ║
 * ║                                                                             ║
 * ║ 【作者信息】                                                                ║
 * ║   @author Liwb                                                              ║
 * ║   @since 2022-4-14                                                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class BatteryChangedReceiver extends BroadcastReceiver {

    // ═════════════════════════════════════════════════════════════════════════════
    // 消息类型常量定义 - Handler消息的what字段标识
    // ═════════════════════════════════════════════════════════════════════════════

    /** 时间显示更新消息标识，触发状态栏时间文本刷新 */
    private static final int MSG_UPDATETIME = 31; // 时间显示更新

    /** 时基改变提示显示消息标识（当前已停用，代码已注释） */
    private static final int MSG_TIP_DISPLAY_TB = 32; // 时基改变提示显示

    /** 时基改变提示消失消息标识（当前已停用，代码已注释） */
    private static final int MSG_TIP_DISPLAY_TB_GONE = 33; // 时基改变提示消失

    /** 电池进入充电状态消息标识，触发充电图标显示和低电量弹窗关闭 */
    private static final int MSG_BATTERY_CHARGE = 34; // 电池进入充电状态

    /** 电池进入非充电状态（放电）消息标识，触发放电图标显示和低电量检测 */
    private static final int MSG_BATTERY_DISCHARGE = 35; // 电池进入非充电状态

    /** 电池电量充电时的自动更新消息标识（当前已停用，代码已注释） */
    private static final int MSG_BATTERY_SELF_UPDATE = 36; // 电池电量充电时的自动更新

    /** 时基消息标识（当前已停用，代码已注释） */
    private static final int MSG_TIMEBASE = 37; // 时基消息

    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量 - UI控件引用
    // ═════════════════════════════════════════════════════════════════════════════

    /** 确认对话框引用，用于显示低电量警告弹窗，可通过setDialogOk()设置 */
    private DialogOk dialogOk; // 低电量弹窗对话框

    /** 应用上下文引用，用于访问字符串资源，在onReceive()中赋值 */
    private Context context; // 上下文对象

    /** 电池视图控件引用，用于显示电池图标和电量百分比，可通过setBatteryControl()设置 */
    private BatteryView tvBattery; // 电池视图控件

    /** 时间文本视图引用，用于显示当前时间，可通过setTimeControl()设置 */
    private TextView tvTime; // 时间显示文本视图

    /** 时间更新线程引用，负责周期性发送时间更新消息，懒加载启动 */
    private TimeThread timeThread; // 时间更新线程

    // ═════════════════════════════════════════════════════════════════════════════
    // 公共接口方法 - 外部设置UI控件引用
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置电池视图控件引用
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 将外部的BatteryView控件引用绑定到本接收器，使接收器能够更新电池显示。
     * 同时根据硬件产品特性判断是否支持电池，不支持则隐藏电池视图。
     *
     * 【参数说明】
     * @param tvBattery 电池视图控件引用，用于显示电池图标和电量
     *
     * 【处理逻辑】
     * 1. 保存BatteryView引用到成员变量
     * 2. 调用HardwareProduct.isBattery()判断设备是否支持电池
     * 3. 不支持电池时，将BatteryView设置为GONE（隐藏且不占位）
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setBatteryControl(BatteryView tvBattery){ // 设置电池视图控件
        this.tvBattery=tvBattery; // 保存电池视图引用
        if(!HardwareProduct.isBattery()){ // 判断设备是否支持电池功能
            tvBattery.setVisibility(View.GONE); // 不支持电池则隐藏电池视图（不占布局空间）
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置确认对话框引用
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 将外部的DialogOk对话框引用绑定到本接收器，使接收器能够在低电量时
     * 弹出警告对话框，或在充电时关闭低电量警告对话框。
     *
     * 【参数说明】
     * @param dialogOk 确认对话框引用，用于低电量弹窗提示
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setDialogOk(DialogOk dialogOk){ // 设置确认对话框引用
        this.dialogOk=dialogOk; // 保存对话框引用
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置时间文本视图引用
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 将外部的TextView控件引用绑定到本接收器，使接收器能够更新状态栏时间显示。
     * 首次调用时会创建并启动时间更新线程（TimeThread），线程每20秒发送一次
     * 时间更新消息，驱动Handler刷新时间文本。
     *
     * 【参数说明】
     * @param tvTime 时间文本视图引用，用于显示当前系统时间
     *
     * 【线程启动逻辑】
     * 采用懒加载模式，仅在timeThread为null时创建并启动线程，
     * 避免重复调用setTimeControl()导致多个时间线程同时运行。
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setTimeControl(TextView tvTime){ // 设置时间文本视图
        this.tvTime=tvTime; // 保存时间文本视图引用
        if (timeThread==null){ // 判断时间线程是否已创建（懒加载）
            timeThread= new TimeThread(); // 创建时间更新线程实例
            timeThread.start(); // 启动时间更新线程
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 内部类 - 时间更新线程
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 时间更新线程内部类
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 后台线程，周期性地向Handler发送时间更新消息（MSG_UPDATETIME），
     * 驱动主线程刷新状态栏的时间显示文本。
     *
     * 【运行机制】
     * 线程启动后进入无限循环，每次循环：
     * 1. 从Handler消息池获取一个Message对象
     * 2. 设置消息类型为MSG_UPDATETIME
     * 3. 通过Handler发送到主线程消息队列
     * 4. 休眠20秒（1000ms × 20 = 20000ms）
     *
     * 【线程安全】
     * handler.obtainMessage()从消息池复用Message，避免频繁创建对象。
     * handler.sendMessage()将消息投递到主线程，保证UI操作在主线程执行。
     *
     * 【注意事项】
     * 此线程为守护性质，无退出条件（while(true)），随应用进程生命周期结束。
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private class TimeThread extends Thread { // 时间更新线程内部类
        /** 复用的Message对象引用，用于向Handler发送时间更新消息 */
        Message msg; // 消息对象

        /**
         * ═════════════════════════════════════════════════════════════════════
         * 构造方法
         * ═════════════════════════════════════════════════════════════════════
         * 【功能说明】
         * 创建TimeThread实例，初始化一个空的Message对象。
         * 注意：此Message仅作占位，实际使用时通过obtainMessage()重新获取。
         * ═════════════════════════════════════════════════════════════════════
         */
        public TimeThread() { // 构造方法
            msg = new Message(); // 创建初始Message对象（占位，后续会被obtainMessage覆盖）
        }

        /**
         * ═════════════════════════════════════════════════════════════════════
         * 线程运行方法
         * ═════════════════════════════════════════════════════════════════════
         * 【功能说明】
         * 线程的主执行逻辑，无限循环地每20秒发送一次时间更新消息。
         *
         * 【执行流程】
         * 1. 从Handler消息池获取Message → 2. 设置what为MSG_UPDATETIME
         * → 3. 发送消息到主线程 → 4. 休眠20秒 → 5. 回到步骤1
         *
         * 【异常处理】
         * InterruptedException：线程休眠被中断时打印堆栈跟踪，不中断循环
         * ═════════════════════════════════════════════════════════════════════
         */
        @Override
        public void run() { // 线程运行方法
            super.run(); // 调用父类Thread的run()方法
            do { // 进入无限循环
                try { // 尝试执行消息发送和休眠
                    msg = handler.obtainMessage(); // 从Handler消息池获取复用的Message对象
                    msg.what = MSG_UPDATETIME; // 设置消息类型为时间更新
                    handler.sendMessage(msg); // 将消息发送到主线程消息队列
                    Thread.sleep(1000 * 20); // 休眠20秒（1000ms × 20）
                } catch (InterruptedException e) { // 捕获线程中断异常
                    e.printStackTrace(); // 打印中断异常堆栈信息
                }
            } while (true); // 无限循环，持续发送时间更新消息
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // Handler消息处理器 - 处理电池状态变化和时间更新
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * Handler消息处理器
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 主线程Handler，接收并处理来自广播接收器和时间线程的消息，
     * 根据消息类型执行相应的UI更新操作。
     *
     * 【消息处理映射】
     * ┌─────────────────────────┬──────────────────────────────────────────┐
     * │ 消息类型                │ 处理操作                                 │
     * ├─────────────────────────┼──────────────────────────────────────────┤
     * │ MSG_UPDATETIME (31)     │ 更新状态栏时间显示                       │
     * │ MSG_TIP_DISPLAY_TB (32) │ 时基提示显示（已停用）                   │
     * │ MSG_TIP_DISPLAY_TB_GONE │ 时基提示消失（已停用）                   │
     * │ MSG_BATTERY_CHARGE (34) │ 设置充电图标、更新电量、关闭低电量弹窗   │
     * │ MSG_BATTERY_DISCHARGE   │ 设置放电图标、更新电量、低电量弹窗检测   │
     * │ MSG_BATTERY_SELF_UPDATE │ 充电自动更新（已停用）                   │
     * │ MSG_TIMEBASE (37)       │ 时基UI更新（已停用）                     │
     * └─────────────────────────┴──────────────────────────────────────────┘
     *
     * 【线程模型】
     * Handler在主线程创建，所有handleMessage()回调在主线程执行，
     * 保证UI操作的线程安全性。
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private  Handler handler = new Handler() { // 创建主线程Handler实例
        /**
         * ═════════════════════════════════════════════════════════════════════
         * 消息处理回调方法
         * ═════════════════════════════════════════════════════════════════════
         * 【功能说明】
         * 接收并分发处理各类消息，根据msg.what执行对应的UI更新操作。
         *
         * 【参数说明】
         * @param msg 待处理的消息对象，包含消息类型(what)和附加数据(arg1)
         * ═════════════════════════════════════════════════════════════════════
         */
        @Override
        public void handleMessage(Message msg) { // 消息处理回调
            super.handleMessage(msg); // 调用父类Handler的handleMessage()
            switch (msg.what) { // 根据消息类型分发处理
                case MSG_UPDATETIME: // 时间更新消息
                    Calendar calendar = Calendar.getInstance(); // 获取日历实例（当前时间）
                    int hourInt = Tools.is24HourFormat() // 判断系统是否使用24小时制
                            ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR); // 24小时制取HOUR_OF_DAY，12小时制取HOUR
                    int minInt = calendar.get(Calendar.MINUTE); // 获取当前分钟数
                    String hourStr = String.valueOf(hourInt < 10 ? "0" + hourInt : hourInt); // 小时数格式化：不足10补前导零
                    String minStr = String.valueOf(minInt < 10 ? "0" + minInt : minInt); // 分钟数格式化：不足10补前导零
                    tvTime.setText(hourStr + ":" + minStr); // 设置时间文本，格式为"HH:mm"
                    break; // 结束MSG_UPDATETIME处理

                case MSG_TIP_DISPLAY_TB: // 时基改变提示显示（当前已停用）
                    // 以下代码已注释，保留供未来功能恢复参考
//                    tvBriefDisplayTB.setVisibility(View.VISIBLE); // 显示时基提示文本
//                    String str; // 时基文本字符串
//                    if (btnCenterTimeBase.getText().toString().contains("\n")) { // 判断时基按钮文本是否包含换行
//                        str = btnCenterTimeBase.getText().toString().split("\n")[1]; // 取换行后第二行
//                    } else {
//                        str = btnCenterTimeBase.getText().toString(); // 取完整文本
//                    }
//                    tvBriefDisplayTB.setText(str); // 设置时基提示文本
//                    if (handler.hasMessages(MSG_TIP_DISPLAY_TB_GONE)) { // 检查是否有待处理的消失消息
//                        handler.removeMessages(MSG_TIP_DISPLAY_TB_GONE); // 移除旧的消失消息
//                    }
//                    handler.sendEmptyMessageDelayed(MSG_TIP_DISPLAY_TB_GONE, 2000); // 延迟2秒后发送消失消息
                    break; // 结束MSG_TIP_DISPLAY_TB处理

                case MSG_TIP_DISPLAY_TB_GONE: // 时基改变提示消失（当前已停用）
                    // 以下代码已注释，保留供未来功能恢复参考
//                    tvBriefDisplayTB.setVisibility(View.GONE); // 隐藏时基提示文本
                    break; // 结束MSG_TIP_DISPLAY_TB_GONE处理

                case MSG_BATTERY_CHARGE: // 电池充电状态消息
                    // 以下日志已注释，调试时可恢复
//                    Logger.i(Command.TAG,"dialogOK:"+dialogOk+",tvBattery:"+tvBattery); // 打印dialogOk和tvBattery引用
                    if (dialogOk==null || tvBattery==null)break; // 空值保护：dialogOk或tvBattery未设置时跳过处理
                    if (dialogOk.isShow() && Objects.equals(dialogOk.getText(), context.getResources().getString(R.string.msgBatteryLow))) { // 判断低电量弹窗是否正在显示且文本为低电量提示
                        dialogOk.hide(); // 充电状态下关闭低电量警告弹窗
                    }
                    tvBattery.setLevel(msg.arg1); // 更新电池视图的电量百分比（arg1携带电量值）
                    tvBattery.setIcon(true); // 设置电池图标为充电状态（显示充电图标）
                    // 以下充电自动更新代码已注释
//                    tvBattery.setText(msg.arg1 + "%"); // 设置电量百分比文本
//                    if (handler.hasMessages(MSG_BATTERY_SELF_UPDATE)) { // 检查是否有待处理的自动更新消息
//                        handler.removeMessages(MSG_BATTERY_SELF_UPDATE); // 移除旧的自动更新消息
//                    }
//                    handler.sendEmptyMessageDelayed(MSG_BATTERY_SELF_UPDATE, 800); // 延迟800ms后发送自动更新消息
                    break; // 结束MSG_BATTERY_CHARGE处理

                case MSG_BATTERY_DISCHARGE: // 电池放电状态消息
                    // 以下日志已注释，调试时可恢复
//                    Logger.i(Command.TAG,"dialogOK:"+dialogOk+",tvBattery:"+tvBattery); // 打印dialogOk和tvBattery引用
                    if (dialogOk==null || tvBattery==null)break; // 空值保护：dialogOk或tvBattery未设置时跳过处理
                    if (tvBattery.getLevel() >= 20 && msg.arg1 < 20) { // 判断电量是否从≥20%降至<20%（低电量阈值检测）
                        dialogOk.setData(R.string.msgBatteryLow, null, null); // 弹出低电量警告对话框，无附加数据和回调
                    }
                    tvBattery.setLevel(msg.arg1); // 更新电池视图的电量百分比（arg1携带电量值）
                    tvBattery.setIcon(false); // 设置电池图标为放电状态（显示空电池图标）
                    // 以下放电自动更新代码已注释
//                    if (handler.hasMessages(MSG_BATTERY_SELF_UPDATE)) { // 检查是否有待处理的自动更新消息
//                        handler.removeMessages(MSG_BATTERY_SELF_UPDATE); // 移除旧的自动更新消息
//                    }
                    break; // 结束MSG_BATTERY_DISCHARGE处理

                case MSG_BATTERY_SELF_UPDATE: // 电池充电自动更新消息（当前已停用）
                    // 以下代码已注释，保留供未来充电动画功能恢复参考
//                    tvBattery.selfUpdate(); // 调用BatteryView自更新方法（充电动画效果）
//                    handler.sendEmptyMessageDelayed(MSG_BATTERY_SELF_UPDATE, 800); // 延迟800ms后再次发送自动更新消息（循环动画）
                    break; // 结束MSG_BATTERY_SELF_UPDATE处理

                case MSG_TIMEBASE: // 时基消息（当前已停用）
                    // 以下代码已注释，保留供未来时基UI更新功能恢复参考
//                    if (Scope.getInstance().isZoom()) { // 判断是否处于缩放模式
//                        bgTimeBase.setBackgroundResource(R.drawable.ic_rectangle_6_zoom); // 缩放模式背景
//                    } else {
//                        bgTimeBase.setBackgroundResource(R.drawable.ic_rectangle_6); // 普通模式背景
//                    }
//                    btnLeftTimeBase.setBackgroundResource(R.drawable.ic_timebase_left); // 左时基按钮背景
//                    btnRightTimeBase.setBackgroundResource(R.drawable.ic_timebase_right); // 右时基按钮背景
                    break; // 结束MSG_TIMEBASE处理
            } // switch结束
        } // handleMessage结束
    }; // handler结束

    // ═════════════════════════════════════════════════════════════════════════════
    // 广播接收方法 - 核心入口
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 广播接收回调方法
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 当系统发送电池相关广播时，此方法被回调执行。
     * 解析Intent中的电池状态数据，根据充电/放电状态构造不同的Handler消息，
     * 将电池信息投递到主线程进行UI更新。
     *
     * 【参数说明】
     * @param context 广播接收时的上下文对象，用于访问资源
     * @param intent  携带电池状态数据的意图对象
     *
     * 【处理流程】
     * 1. 保存上下文引用
     * 2. 获取广播Action，空值则直接返回
     * 3. 判断设备是否支持电池功能，不支持则直接返回
     * 4. 根据Action类型分发处理：
     *    a. ACTION_BATTERY_CHANGED：解析电量、状态、充电方式，发送充电/放电消息
     *    b. ACTION_BATTERY_LOW：显示低电量Toast提示
     *    c. ACTION_BATTERY_OKAY：电池恢复正常（当前无操作）
     *
     * 【电池状态判断逻辑】
     * ┌─────────────────────────────────────────────────────────────────┐
     * │ 第一层判断：BATTERY_STATUS（充电状态）                          │
     * │  CHARGING(2) → MSG_BATTERY_CHARGE                              │
     * │  FULL(5) / NOT_CHARGING(4) / DISCHARGING(3) / 其他             │
     * │    → MSG_BATTERY_DISCHARGE                                     │
     * ├─────────────────────────────────────────────────────────────────┤
     * │ 第二层判断：BATTERY_PLUGGED（充电方式，仅非充满状态时执行）     │
     * │  PLUGGED_AC(1) / PLUGGED_USB(2) → MSG_BATTERY_CHARGE          │
     * │  PLUGGED_WIRELESS(4) / 其他 → MSG_BATTERY_DISCHARGE           │
     * └─────────────────────────────────────────────────────────────────┘
     *
     * 【注意事项】
     * 1. ACTION_BATTERY_CHANGED是粘性广播，注册后立即触发一次
     * 2. 第二层判断（PLUGGED）在非充满状态下额外判断充电方式，
     *    可能覆盖第一层判断的结果（如NOT_CHARGING但连接了USB）
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Override
    public void onReceive(Context context, Intent intent) { // 广播接收回调方法
        this.context =context; // 保存上下文引用（后续用于访问字符串资源）
        String action = intent.getAction(); // 获取广播的Action标识
        if (action == null) return; // Action为空则直接返回，避免NPE
        if(!HardwareProduct.isBattery()) return; // 设备不支持电池功能则直接返回，不处理电池广播
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) { // 判断是否为电池状态变化广播
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0); // 获取当前电量值（0~scale），默认0
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100); // 获取电量最大值（通常为100），默认100
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0); // 获取充电状态常量，默认0
            Message message = new Message(); // 创建消息对象，用于传递到Handler
            message.arg1 = level; // 将电量值存入消息的arg1字段
            switch (status) { // 根据充电状态分发
                case BatteryManager.BATTERY_STATUS_CHARGING: // 正在充电（状态值=2）
                    message.what = MSG_BATTERY_CHARGE; // 设置消息类型为充电状态
                    handler.sendMessage(message); // 发送充电消息到主线程
                    break; // 结束CHARGING分支
                case BatteryManager.BATTERY_STATUS_FULL: // 电池充满（状态值=5）
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING: // 未充电（状态值=4，连接电源但未充电）
                case BatteryManager.BATTERY_STATUS_DISCHARGING: // 放电中（状态值=3）
                default: // 其他未知状态
                    message.what = MSG_BATTERY_DISCHARGE; // 设置消息类型为放电状态
                    handler.sendMessage(message); // 发送放电消息到主线程
                    break; // 结束DISCHARGE分支
            } // switch(status)结束
            if (status != BatteryManager.BATTERY_STATUS_FULL) { // 非充满状态下，进一步判断充电连接方式
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0); // 获取充电连接方式常量，默认0
                Message message2 = new Message(); // 创建第二个消息对象，用于充电方式判断
                message2.arg1 = level; // 将电量值存入消息的arg1字段
                switch (plugged) { // 根据充电连接方式分发
                    case BatteryManager.BATTERY_PLUGGED_AC: // 连接交流充电器（方式值=1）
                    case BatteryManager.BATTERY_PLUGGED_USB: // 连接USB充电（方式值=2）
                        message2.what = MSG_BATTERY_CHARGE; // 设置消息类型为充电状态
                        handler.sendMessage(message2); // 发送充电消息到主线程
                        break; // 结束AC/USB分支
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS: // 连接无线充电（方式值=4）
                    default: // 其他未知充电方式（含未连接=0）
                        message.what = MSG_BATTERY_DISCHARGE; // 设置消息类型为放电状态（复用第一个message）
                        //handler.sendMessage(message); // 发送放电消息（已注释，当前不发送）
                        break; // 结束WIRELESS/default分支
                } // switch(plugged)结束
            } // 非充满状态判断结束
        } else if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_LOW)) { // 判断是否为低电量广播
            // 表示当前电池电量低
            DToast.get().show(R.string.msgBatteryLow); // 显示低电量Toast提示
        } else if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_OKAY)) { // 判断是否为电量恢复正常广播
            // 表示当前电池已经从电量低恢复为正常（当前无额外操作）
        } // Action类型判断结束
    } // onReceive方法结束

} // BatteryChangedReceiver类结束
