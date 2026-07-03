package com.micsig.tbook.tbookscope.services.ExternalKeys.client; // 外部按键客户端模块包

import static android.content.Context.BIND_AUTO_CREATE; // 导入自动创建绑定常量

import android.app.ActivityManager; // 导入活动管理器，用于查询运行中的服务
import android.content.ComponentName; // 导入组件名，用于标识Service组件
import android.content.Context; // 导入上下文，用于绑定Service
import android.content.Intent; // 导入意图，用于启动Service
import android.content.ServiceConnection; // 导入Service连接接口
import android.os.Bundle; // 导入数据包裹，用于跨进程传递数据
import android.os.Handler; // 导入Handler，用于线程间消息通信
import android.os.IBinder; // 导入Binder接口，用于跨进程通信
import android.os.Message; // 导入消息对象
import android.os.Messenger; // 导入信使，用于跨进程消息传递
import android.os.RemoteException; // 导入远程异常
import android.util.Log; // 导入日志工具

import com.micsig.base.Logger; // 导入自定义日志工具
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.struct.ExternalKeysMsg_ToMCU; // 导入发送给MCU的按键消息结构体
import com.micsig.tbook.tbookscope.top.layout.userset.TopMsgWirelessKeyboard; // 导入无线键盘消息类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类

import java.util.ArrayList; // 导入动态数组
import java.util.Arrays; // 导入数组工具

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * Created by liwb on 2017/12/4.
 */

/**
 * +----------------------------------------------------------------------+
 * |                      外部按键服务绑定客户端                           |
 * +----------------------------------------------------------------------+
 * | 模块定位：ExternalKeys模块的客户端侧，负责与外部按键Service建立        |
 * |           Messenger跨进程连接，收发MCU按键/旋钮数据                   |
 * +----------------------------------------------------------------------+
 * | 核心职责：                                                            |
 * |   1. 绑定/解绑核心服务(ExternalKeysService)                          |
 * |   2. 向Service发送ARM→MCU指令（LED控制、握手、保活、截图等）          |
 * |   3. 接收Service转发的MCU→ARM按键数据和旋钮数据                      |
 * |   4. 管理无线键盘配对ID和电量信息                                     |
 * |   5. 控制按键处理开关(dealKeys)                                       |
 * +----------------------------------------------------------------------+
 * | 架构设计：                                                            |
 * |   基于Android Messenger机制的跨进程通信(C/S模式)                     |
 * |   本类为Client端，通过ServiceConnection绑定远程Service               |
 * |   使用Handler处理来自Service的消息                                    |
 * |   使用RxBus订阅/发布本地事件                                          |
 * +----------------------------------------------------------------------+
 * | 数据流向：                                                            |
 * |   MCU → Service → ClientHandler → ExternalKeysProtocol → UI          |
 * |   UI → RxBus → consumerToMCU → Service → MCU                         |
 * +----------------------------------------------------------------------+
 * | 依赖关系：                                                            |
 * |   - ExternalKeysProtocol  (协议解析)                                  |
 * |   - RxBus / RxEnum        (本地事件总线)                              |
 * |   - CacheUtil             (缓存状态)                                  |
 * |   - MainViewGroup         (主视图引用)                                |
 * +----------------------------------------------------------------------+
 * | 使用场景：                                                            |
 * |   示波器App启动时绑定核心服务，处理物理按键和旋钮的输入               |
 * +----------------------------------------------------------------------+
 */
public class ExternalKeysOnBindService {
    private static String TAG = "ExternalKeys"; // 日志标签
    private static final int SEND_MESSAGE_CODE = 0x0001; // 发送消息类型：客户端→Service握手
    private static final int RECEIVE_MESSAGE_CODE = 0x0002; // 接收消息类型：Service→客户端通用消息
    private static final int RECEIVE_MESSAGE_MCUTOARM = 0x0003; // 接收消息类型：MCU→ARM按键数据
    private static final int RECEIVE_MESSAGE_EDITOR = 0x0004; // 接收消息类型：旋钮编码器数据
    private static final int RECEIVE_MESSAGE_BatteryProtection=0x0007; // 接收消息类型：电池保护消息

    private static final int SEND_MESSAGE_ARMTOMCU = 0x0005; // 发送消息类型：ARM→MCU数据
    private static final int SEND_MESSAGE_BOOTPROTECTION=0x0006; // 发送消息类型：启动保护
    private static final int SEND_MESSAGE_SCREENSHOT = 0x0008; // 发送消息类型：截图请求

    private static final int RECEIVE_MESSAGE_WIRELESS_ID = 0x0009; // 接收消息类型：无线键盘配对ID
    private static final int RECEIVE_MESSAGE_WIRELESS_BATTERY = 0x000A; // 接收消息类型：无线键盘电量
    private static final int SEND_MESSAGE_WIRELESS_ID = 0x000B; // 发送消息类型：解绑无线键盘ID

    private static final int RECEIVE_TIME_OUT = 0x1001; // 内部消息类型：超时触发值电平线移动

    private static final int SEND_SHAKEHANDS = 0x1002; // 内部消息类型：触发握手

    private static final int SEND_KEEPLIVE = 0x1003; // 内部消息类型：触发保活

    private static final int SEND_SCOPE_AUTOSAVE = 0x1005; // 内部消息类型：触发自动保存

    private Context context = null; // 应用上下文引用
    private MainViewGroup mainViewGroup = null; // 主视图组引用
    private boolean isBound = false; // 是否已绑定Service
    private static boolean dealKeys=false; // 是否处理按键事件的开关

    public static long wirelessId = 0; // 无线键盘配对ID
    public static int wirelessBattery = 0; // 无线键盘电量
    public static long wirelessBatteryHeartbeat = 0; // 无线键盘电量心跳时间戳

    //用于启动MyService的Intent对应的action
//    private final String SERVICE_ACTION = "com.micsig.tbook.tbookscope";
//    private final String SERVICE_PACKAGE = "com.micsig.tbook.tbookscope.debug";
//    private final String SERVICE_CLASS = "com.micsig.tbook.tbookscope.services.ExternalKeys.service.ExternalKeysService";
    private final String SERVICE_ACTION = "com.micsig.coreservice"; // 核心服务的Action
    private final String SERVICE_CLASS = "com.micsig.coreservice.ExternalKeysService.ExternalKeysService"; // 核心服务的类名

    private Messenger serviceMessenger = null; // Service端的信使，用于向Service发送消息
    private ClientHandler clientHandler = new ClientHandler(); // 客户端消息处理器
    private Messenger clientMessenger = null; // 客户端信使，供Service回复消息

    /**
     * 构造函数：初始化外部按键服务绑定客户端
     *
     * @param context       应用上下文
     * @param mainViewGroup 主视图组引用
     */
    public ExternalKeysOnBindService(Context context, MainViewGroup mainViewGroup) {
        this.context = context; // 保存上下文引用
        this.mainViewGroup = mainViewGroup; // 保存主视图组引用
        initControl(); // 初始化RxBus订阅
        clientMessenger = new Messenger(clientHandler); // 创建客户端信使
    }

    /**
     * 初始化控件：订阅RxBus事件
     * 订阅发送给MCU的消息事件和主界面缓存加载完成事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEY_TOMCU).subscribe(consumerToMCU); // 订阅发送MCU消息事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerMainLoadCache); // 订阅主界面缓存加载事件
    }

    /**
     * RxBus消费者：处理发送给MCU的按键消息
     * 将按键消息通过协议解析后发送给Service，如果dealKeys为false则发送全零数据
     */
    private Consumer<ExternalKeysMsg_ToMCU> consumerToMCU = new Consumer<ExternalKeysMsg_ToMCU>() {
        @Override
        public void accept(@NonNull ExternalKeysMsg_ToMCU externalKeysMsg_toMCU) throws Exception {

            byte[] b = ExternalKeysProtocol.parseToMCU(externalKeysMsg_toMCU); // 协议解析为MCU字节数组
            if (b == null) return; // 解析失败则返回
            if(dealKeys) { // 如果允许处理按键
                sendMessage(b); // 发送实际按键数据
            }else{ // 如果不允许处理按键
                byte[] x = new byte[b.length]; // 创建等长的全零数组
                for (int i=0;i<b.length;i++){ // 遍历每个字节
                    x[i] = 0; // 置零
                }
                sendMessage(x); // 发送全零数据（屏蔽按键）
            }
        }
    };

    /**
     * RxBus消费者：处理主界面缓存加载完成事件
     * 延迟200ms后发送初始化MCU指令，并设置缓存加载状态
     */
    private Consumer<LoadCache> consumerMainLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            new Thread(new Runnable() { // 启动新线程
                @Override
                public void run() {
                    Thread.currentThread().setName("ExtKeyBindServer"); // 设置线程名称
                    try {
                        Thread.sleep(200); // 延迟200ms等待系统初始化
                    } catch (InterruptedException e) {
                        e.printStackTrace(); // 打印中断异常
                    }
                    byte[] b = ExternalKeysProtocol.parseToMCU(null); // 解析空消息获取初始化指令
                    if (b == null) return; // 解析失败则返回
                    sendMessage(b); // 发送初始化指令到Service
                }
            }).start(); // 启动线程
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_ExternalKeysOnBindService,true); // 标记本模块缓存加载完成
            sendTimeout(); // 启动超时触发值电平线移动定时器
        }
    };

    /**
     * 发送超时消息，触发值电平线移动
     * 延迟25ms后发送超时消息
     */
    public void sendTimeout(){
        clientHandler.sendEmptyMessageDelayed(RECEIVE_TIME_OUT,25); // 延迟25ms发送超时消息
    }

    /**
     * 保活机制：移除旧的保活消息并重新发送
     * 每10秒发送一次保活消息，维持与Service的连接
     */
    private void keeplive(){
        if(clientHandler.hasMessages(SEND_KEEPLIVE)) { // 如果已有保活消息在队列中
            clientHandler.removeMessages(SEND_KEEPLIVE); // 移除旧的保活消息
        }
        clientHandler.sendEmptyMessageDelayed(SEND_KEEPLIVE, 10000); // 10秒后发送保活消息
    }

    /**
     * 客户端消息处理器
     * 处理来自Service的消息以及内部定时消息
     */
    private class ClientHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what!=RECEIVE_TIME_OUT) { // 非超时消息
                 //Log.d("Tag.Debug", String.format("ClientHandler.handleMessage: %s",msg ));
            }


            if (!CacheUtil.get().isLoadComplete()) { // 如果缓存尚未加载完成
                if(msg.what == SEND_SHAKEHANDS){ // 握手消息
                    clientHandler.sendEmptyMessageDelayed(SEND_SHAKEHANDS,100); // 延迟100ms重试握手
                }else{ // 其他消息
                    Log.d(TAG,"msg.what:" + msg.what); // 打印消息类型
                }
                return; // 缓存未加载完成时不处理消息
            }
            if(msg.what == SEND_SHAKEHANDS){ // 握手消息
                sendShakehands(); // 发送握手消息
                keeplive(); // 启动保活机制
            }else if (msg.what == RECEIVE_MESSAGE_CODE) { // Service通用消息
                Bundle data = msg.getData(); // 获取数据包裹
                if (data != null) { // 数据不为空
                    String str = data.getString("msg"); // 获取消息字符串
                    Logger.i(TAG, "客户端收到Service的消息: " + str); // 打印收到的消息
                }
            } else if (msg.what == RECEIVE_MESSAGE_MCUTOARM) { // MCU→ARM按键数据
                if (dealKeys==false) { // 不允许处理按键
                    Log.i(TAG,"msg.what = RECEIVE_MESSAGE_MCUTOARM dealKeys=false!!!"); // 打印警告
                    return; // 直接返回
                }
                //按键
                Bundle data = msg.getData(); // 获取数据包裹
                byte[] b = data.getByteArray("mcuToArm"); // 获取MCU到ARM的字节数据
                //Log.d("Tag.Debug", String.format("ClientHandler.handleMessage: %s", Arrays.toString(b) ));
                boolean bValid = false; // 数据有效性标志
                for(int i=0;i<b.length;i++){ // 遍历字节数组

                    if(b[i] != 0){ // 如果存在非零字节
                        bValid = true; // 标记数据有效
                    }
                }

                if (b != null && bValid) { // 数据非空且有效
                    ExternalKeysProtocol.parseMCUTOARM(b); // 解析MCU到ARM的按键数据
                }else{ // 数据无效
                    Log.i(TAG,"bValid:" + bValid); // 打印有效性状态
                }
            } else if (msg.what == RECEIVE_MESSAGE_EDITOR) { // 旋钮编码器数据
                if (dealKeys==false) { // 不允许处理按键
                    Logger.i(TAG,"msg.what = RECEIVE_MESSAGE_EDITOR dealKeys=false!!!"); // 打印警告
                    return; // 直接返回
                }
                //旋钮
                Bundle data = msg.getData(); // 获取数据包裹
                byte[] b1 = data.getByteArray("editor"); // 获取旋钮字节数据

                if (b1 != null && !(b1[0] == 0 && b1[1] == 0)) { // 数据非空且旋钮值不为零
                    ExternalKeysProtocol.parseEditor(b1); // 解析旋钮数据
                }
            }else if (msg.what==RECEIVE_MESSAGE_BatteryProtection){ // 电池保护消息
                if (dealKeys==false){ // 不允许处理按键
                    Logger.i(TAG,"msg.what==RECEIVE_MESSAGE_BatteryProtection=false!!!"); // 打印警告
                    return; // 直接返回
                }
                //电池保护
                Bundle data = msg.getData(); // 获取数据包裹
                byte[] b1 = data.getByteArray("batteryProtection"); // 获取电池保护字节数据
                if (b1 != null && !(b1[0] == 0 )) { // 数据非空且值不为零
//                    ExternalKeysProtocol.parseBatteryProtection(b1); // 解析电池保护数据（已注释）
                }
            }else if(msg.what == RECEIVE_TIME_OUT){ // 超时触发值电平线移动
                ExternalKeysProtocol.moveValueLevel(); // 移动值电平线
                sendTimeout(); // 重新启动超时定时器

            }else if(msg.what == RECEIVE_MESSAGE_WIRELESS_ID){ // 无线键盘配对ID
                Bundle data = msg.getData(); // 获取数据包裹
                wirelessId = data.getLong("WirelessPairID"); // 获取无线键盘配对ID
                RxBus.getInstance().post(RxEnum.WIRELESS_KEYBOARD_STAT,new TopMsgWirelessKeyboard(wirelessId,wirelessBattery,wirelessBatteryHeartbeat)); // 发布无线键盘状态事件
            }else if(msg.what == RECEIVE_MESSAGE_WIRELESS_BATTERY){ // 无线键盘电量
                Bundle data = msg.getData(); // 获取数据包裹
                wirelessBattery = data.getInt("WirelessPairBattery"); // 获取无线键盘电量
                wirelessBatteryHeartbeat = data.getLong("WirelessBatteryHeartbeat"); // 获取电量心跳时间戳
                RxBus.getInstance().post(RxEnum.WIRELESS_KEYBOARD_STAT,new TopMsgWirelessKeyboard(wirelessId,wirelessBattery,wirelessBatteryHeartbeat)); // 发布无线键盘状态事件
            }else if(msg.what == SEND_KEEPLIVE){ // 保活消息
                sendKeeplive(); // 发送保活消息给Service
                keeplive(); // 重新启动保活定时器
            }
        }
    }

    /**
     * 发送自动保存指令
     *
     * @param bAutoSave 是否自动保存
     */
    public void sendAutoSave(boolean bAutoSave){
        Message msg = Message.obtain(); // 获取消息对象
        msg.what = SEND_SCOPE_AUTOSAVE; // 设置消息类型为自动保存
        Bundle data = new Bundle(); // 创建数据包裹
        data.putBoolean("SCOPE_AUTOSAVE", bAutoSave); // 放入自动保存标志
        msg.setData(data); // 设置数据包裹
        try {
            if (serviceMessenger == null) { // Service信使为空
                Log.i(TAG, "serviceMessenger null"); // 打印警告
                return; // 返回
            }
            serviceMessenger.send(msg); // 发送消息给Service
        } catch (RemoteException e) { // 捕获远程异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * 发送保活消息给Service
     * 维持客户端与Service之间的连接
     */
    private void sendKeeplive(){
        Message msg = Message.obtain(); // 获取消息对象
        msg.what = SEND_KEEPLIVE; // 设置消息类型为保活
        msg.replyTo = clientMessenger; // 设置回复信使为客户端信使
        try {
            if (serviceMessenger == null) { // Service信使为空
                Log.i(TAG, "serviceMessenger null"); // 打印警告
                return; // 返回
            }
            serviceMessenger.send(msg); // 发送保活消息给Service
        } catch (RemoteException e) { // 捕获远程异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * 截图请求（不含自动保存标志）
     *
     * @param bTimestamp 是否添加时间戳水印
     * @param bInvert    是否反色
     * @param filePath   文件保存路径
     * @param fileName   文件名
     */
    public void screenshot(boolean bTimestamp, boolean bInvert, String filePath, String fileName){
        screenshot(bTimestamp,bInvert,filePath,fileName,false); // 默认不自动保存
    }

    /**
     * 截图请求（含自动保存标志）
     *
     * @param bTimestamp 是否添加时间戳水印
     * @param bInvert    是否反色
     * @param filePath   文件保存路径
     * @param fileName   文件名
     * @param bAutosave  是否自动保存
     */
    public void screenshot(boolean bTimestamp, boolean bInvert, String filePath, String fileName,boolean bAutosave) {
        boolean bThumbnail = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SAVETHUMBNAIL); // 获取是否保存缩略图设置
        Message msg = Message.obtain(); // 获取消息对象
        msg.what = SEND_MESSAGE_SCREENSHOT; // 设置消息类型为截图
        msg.arg1 = bTimestamp ? 1 : 0; // arg1存储时间戳标志
        msg.arg2 = (bInvert ? 1 : 0) | (bAutosave | !bThumbnail ? 2 : 0); // arg2存储反色标志和缩略图/自动保存标志的组合
        Bundle data = new Bundle(); // 创建数据包裹
        data.putString("filePath", filePath); // 放入文件路径
        data.putString("fileName", fileName); // 放入文件名
        msg.setData(data); // 设置数据包裹
        msg.replyTo = clientMessenger; // 设置回复信使
        try {
            if (serviceMessenger == null) { // Service信使为空
                Log.i(TAG, "serviceMessenger null"); // 打印警告
                return; // 返回
            }
            serviceMessenger.send(msg); // 发送截图消息给Service
        } catch (RemoteException e) { // 捕获远程异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * 解绑无线键盘ID
     * 将无线键盘配对ID清零并通知Service
     */
    public void unbindWirelessId(){
        wirelessId = 0; // 清零无线键盘配对ID
        Message msg = Message.obtain(); // 获取消息对象
        msg.what = SEND_MESSAGE_WIRELESS_ID; // 设置消息类型为无线键盘ID
        msg.arg1 = 0; // 参数1置零
        msg.arg2 = 0; // 参数2置零
        try {
            if (serviceMessenger == null) { // Service信使为空
                Log.i(TAG, "serviceMessenger null"); // 打印警告
                return; // 返回
            }
            serviceMessenger.send(msg); // 发送解绑消息给Service
        } catch (RemoteException e) { // 捕获远程异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * 发送消息，还未进行封装，在调用前应该有一个转换的功能。
     *
     * @param b 根据协议进行转换。
     */
    public void sendMessage(byte[] b) {

        Message msg = Message.obtain(); // 获取消息对象
        msg.what = SEND_MESSAGE_ARMTOMCU; // 设置消息类型为ARM→MCU
        Bundle data = new Bundle(); // 创建数据包裹
        data.putByteArray("armToMcu", b); // 放入ARM到MCU的字节数据
        msg.setData(data); // 设置数据包裹
        msg.replyTo = clientMessenger; // 设置回复信使
        try {

            if (serviceMessenger == null) { // Service信使为空
                Log.i(TAG, "serviceMessenger null"); // 打印警告
                return; // 返回
            }
            serviceMessenger.send(msg); // 发送消息给Service
        } catch (RemoteException e) { // 捕获远程异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * 发送握手消息给Service
     * 包含App包名信息，用于Service识别客户端
     */
    public void sendShakehands(){
        Message msg = Message.obtain(); // 获取消息对象
        msg.what = SEND_MESSAGE_CODE; // 设置消息类型为握手

        Bundle data = new Bundle(); // 创建数据包裹
        data.putString("msg", "你好，MyService，我是客户端"); // 放入握手消息
        data.putString("AppPackage",context.getPackageName()); // 放入App包名
        msg.setData(data); // 设置数据包裹
        msg.replyTo = clientMessenger; // 设置回复信使
        try {
            Log.i(TAG, "客户端向service发送信息"); // 打印发送日志
            if(serviceMessenger != null) { // Service信使不为空
                serviceMessenger.send(msg); // 发送握手消息
            }
        } catch (RemoteException e) { // 捕获远程异常
            e.printStackTrace(); // 打印异常堆栈
            Log.i(TAG, "客户端向service发送消息失败: " + e.getMessage()); // 打印失败信息
        }
    }

    /**
     * Service连接回调
     * 处理与Service的连接和断开事件
     */
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //客户端与Service建立连接
            Log.i(TAG, "客户端 onServiceConnected"); // 打印连接成功日志

            //我们可以通过从Service的onBind方法中返回的IBinder初始化一个指向Service端的Messenger
            serviceMessenger = new Messenger(binder); // 通过Binder创建Service端信使
            isBound = true; // 标记已绑定

            clientHandler.sendEmptyMessage(SEND_SHAKEHANDS); // 发送握手消息
            keeplive(); // 启动保活机制
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            //客户端与Service失去连接
            serviceMessenger = null; // 清空Service信使
            isBound = false; // 标记未绑定
            Log.i(TAG, "客户端 onServiceDisconnected"); // 打印断开连接日志
            bind(); // 重新尝试绑定Service
        }
    };

    /**
     * 检查核心服务是否正在运行
     *
     * @return true=服务正在运行，false=服务未运行
     */
    public boolean isExistServices() {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE); // 获取活动管理器
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30); // 获取最近30个运行中的服务
        for (int i = 0; i < runningService.size(); i++) { // 遍历运行中的服务
            if (runningService.get(i).service.toString().equals(SERVICE_CLASS)) { // 找到目标服务
                Log.i(TAG, "find package:" + runningService.get(i).service.getClassName()); // 打印找到的服务
                return true; // 服务存在
            }
            Log.i(TAG, "running package:" + runningService.get(i).service.getClassName()); // 打印遍历到的服务
        }
        Log.i(TAG, "No Service!"); // 未找到服务
        return false; // 服务不存在
    }

    /**
     * 绑定核心服务
     * 使用显式Intent绑定ExternalKeysService
     */
    public void bind() {
        Logger.i("开始绑定服务！"); // 打印开始绑定日志
        if (!isBound) { // 尚未绑定
            Intent intent = new Intent(); // 创建Intent
            ComponentName cn = new ComponentName(SERVICE_ACTION, SERVICE_CLASS); // 创建组件名
            intent.setComponent(cn); // 设置组件
            try {
                context.bindService(intent, conn, BIND_AUTO_CREATE); // 绑定Service（自动创建）
                Logger.i("绑定服务完成 ！"); // 打印绑定完成日志
            } catch (Exception e) { // 捕获异常
                e.printStackTrace(); // 打印异常堆栈
                Log.e("DemoLog", e.getMessage()); // 打印错误消息
            }
            Log.i(TAG, "client onbind!"); // 打印绑定日志
        }
    }

    /**
     * 解绑核心服务
     */
    public void unBind() {
        if (isBound) { // 已绑定
            Log.i(TAG, "客户端调用unbindService方法"); // 打印解绑日志
            context.unbindService(conn); // 解绑Service
        }
    }

    /**
     * 设置按键处理开关
     * 关闭按键处理时，发送LED关闭指令
     *
     * @param dealKeys true=允许处理按键，false=禁止处理按键
     */
    public void setDealKeys(boolean dealKeys){
        //Log.d("Tag.Debug", String.format("ExternalKeysOnBindService.setDealKeys: %s",dealKeys ));
        this.dealKeys=dealKeys; // 设置按键处理开关
        if(!dealKeys){ // 如果关闭按键处理
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU, new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_ALL, ExternalKeysMsg_ToMCU.STATE_LED_OFF)); // 发送LED关闭指令
        }
    }

    /**
     * 获取按键处理开关状态
     *
     * @return true=允许处理按键，false=禁止处理按键
     */
    public boolean isDealKeys(){
        return this.dealKeys; // 返回按键处理开关状态
    }
}
