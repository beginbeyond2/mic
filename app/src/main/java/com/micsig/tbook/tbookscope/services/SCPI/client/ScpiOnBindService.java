package com.micsig.tbook.tbookscope.services.SCPI.client; // SCPI客户端模块包

import android.content.ComponentName; // 导入组件名，用于标识Service组件
import android.content.Context; // 导入上下文，用于绑定Service
import android.content.Intent; // 导入意图，用于启动Service
import android.content.ServiceConnection; // 导入Service连接接口
import android.os.Build; // 导入Build类，用于获取SDK版本
import android.os.Bundle; // 导入数据包裹，用于跨进程传递数据
import android.os.Handler; // 导入Handler，用于线程间消息通信
import android.os.IBinder; // 导入Binder接口，用于跨进程通信
import android.os.Message; // 导入消息对象
import android.os.Messenger; // 导入信使，用于跨进程消息传递
import android.os.RemoteException; // 导入远程异常
import android.util.Log; // 导入日志工具

import com.micsig.base.Logger; // 导入自定义日志工具
import com.micsig.tbook.scope.Data.AutoSave; // 导入自动保存数据管理类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件
import com.micsig.tbook.tbookscope.scpi.SCPICommandDeal; // 导入SCPI命令处理器
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类

/**
 * Created by liwb on 2018/8/8.
 */

/**
 * +----------------------------------------------------------------------+
 * |                     SCPI服务绑定客户端                                 |
 * +----------------------------------------------------------------------+
 * | 模块定位：SCPI模块的客户端侧，负责与SCPI远程服务建立Messenger          |
 * |           跨进程连接，收发SCPI指令                                     |
 * +----------------------------------------------------------------------+
 * | 核心职责：                                                            |
 * |   1. 绑定/解绑核心服务(SCPIService)                                   |
 * |   2. 向Service发送SCPI指令字符串                                      |
 * |   3. 接收Service转发的SCPI指令并交由SCPICommandDeal解析执行            |
 * |   4. 支持波形数据的共享内存传输                                       |
 * |   5. 自动保存运行时拒绝SCPI指令处理                                   |
 * +----------------------------------------------------------------------+
 * | 架构设计：                                                            |
 * |   基于Android Messenger机制的跨进程通信(C/S模式)                     |
 * |   本类为Client端，通过ServiceConnection绑定远程SCPIService            |
 * |   使用静态内部类ClientHandler处理来自Service的消息                    |
 * +----------------------------------------------------------------------+
 * | 数据流向：                                                            |
 * |   外部SCPI命令 → sendMessage → Service → SCPIService处理              |
 * |   Service → ClientHandler → SCPICommandDeal.scpiParser → 执行          |
 * +----------------------------------------------------------------------+
 * | 依赖关系：                                                            |
 * |   - SCPICommandDeal      (SCPI命令解析执行)                           |
 * |   - AutoSave             (自动保存状态判断)                           |
 * |   - MainViewGroup        (主视图引用)                                  |
 * |   - StrUtil              (字符串工具)                                  |
 * +----------------------------------------------------------------------+
 * | 使用场景：                                                            |
 * |   远程SCPI客户端（如上位机）通过核心服务发送SCPI指令控制示波器         |
 * +----------------------------------------------------------------------+
 */
public class ScpiOnBindService {
    private static final String TAG = "ScpiOnBindService"; // 日志标签

    private static final int SEND_MESSAGE_CODE = 0x0001; // 发送消息类型：握手消息
    private static final int SEND_MESSAGE_SCPI = 0x0003; // 发送消息类型：SCPI指令

    private static final int RECEIVE_MESSAGE_CODE = 0x0002; // 接收消息类型：Service通用消息
    private static final int RECIEVE_MESSAGE_SCPI = 0x0004; // 接收消息类型：SCPI指令

    private static final String KEY_MESSAGE="message"; // Bundle键：消息内容
    private static final String KEY_SHARED_MEMORY="shared_memory"; // Bundle键：共享内存
    private static final String KEY_MESSAGE_TYPE="message_type"; // Bundle键：消息类型
    private static final String KEY_TYPE_WAVEFORM="waveform"; // 消息类型值：波形数据

    private Context context = null; // 应用上下文引用
    private MainViewGroup mainViewGroup = null; // 主视图组引用

    private boolean isBound = false; // 是否已绑定Service

    //用于启动MyService的Intent对应的action
    private final String SERVICE_ACTION = "com.micsig.coreservice"; // 核心服务的Action
    private final String SERVICE_CLASS = "com.micsig.coreservice.SCPIService.SCPIService"; // 核心SCPI服务的类名

    private Messenger serviceMessenger = null; // Service端的信使，用于向Service发送消息
    private Messenger clientMessenger = new Messenger(new ClientHandler()); // 客户端信使，供Service回复消息

    /**
     * 构造函数：初始化SCPI服务绑定客户端
     *
     * @param context       应用上下文
     * @param mainViewGroup 主视图组引用
     */
    public ScpiOnBindService(Context context, MainViewGroup mainViewGroup) {
        this.context = context; // 保存上下文引用
        this.mainViewGroup = mainViewGroup; // 保存主视图组引用
//        initControl();
    }


    /**
     * 发送SCPI指令消息（不含波形数据）
     *
     * @param str SCPI指令字符串
     */
    public void sendMessage(String str) {
        Message msg = Message.obtain(); // 获取消息对象
        msg.what = SEND_MESSAGE_SCPI; // 设置消息类型为SCPI指令
        Bundle data = new Bundle(); // 创建数据包裹
        data.putString("message", str); // 放入SCPI指令字符串
        msg.setData(data); // 设置数据包裹
        try {
//            Log.i(TAG, "发送信息");
            if (serviceMessenger == null) { // Service信使为空
                Log.i(TAG, "serviceMessenger null"); // 打印警告
                return; // 返回
            }
            serviceMessenger.send(msg); // 发送SCPI消息给Service
        } catch (RemoteException e) { // 捕获远程异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * 发送SCPI指令消息（可选携带波形数据）
     * 当isContainWaveForm为true时，附带共享内存中的波形数据
     *
     * @param str               SCPI指令字符串
     * @param isContainWaveForm 是否携带波形数据
     */
    public void sendMessage(String str,boolean isContainWaveForm) {
        Message msg = Message.obtain(); // 获取消息对象
        msg.what = SEND_MESSAGE_SCPI; // 设置消息类型为SCPI指令
        Bundle data = new Bundle(); // 创建数据包裹
        data.putString(KEY_MESSAGE, str); // 放入SCPI指令字符串
        if (isContainWaveForm){ // 如果需要携带波形数据
            data.putString(KEY_MESSAGE_TYPE, KEY_TYPE_WAVEFORM); // 设置消息类型为波形
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) { // Android 8.1以上
                data.putParcelable(KEY_SHARED_MEMORY,SCPICommandDeal.getInstance().getSharedMem()); // 放入共享内存对象
            }
        }
        msg.setData(data); // 设置数据包裹
        try {
//            Log.i(TAG, "发送信息");
            if (serviceMessenger == null) { // Service信使为空
                Log.i(TAG, "serviceMessenger null"); // 打印警告
                return; // 返回
            }
            serviceMessenger.send(msg); // 发送SCPI消息给Service
        } catch (RemoteException e) { // 捕获远程异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * Service连接回调
     * 处理与SCPI Service的连接和断开事件
     */
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //客户端与Service建立连接
            Log.i(TAG, "客户端 onServiceConnected"); // 打印连接成功日志

            //我们可以通过从Service的onBind方法中返回的IBinder初始化一个指向Service端的Messenger
            serviceMessenger = new Messenger(binder); // 通过Binder创建Service端信使
            isBound = true; // 标记已绑定

            Message msg = Message.obtain(); // 获取消息对象
            msg.what = SEND_MESSAGE_CODE; // 设置消息类型为握手

            //此处跨进程Message通信不能将msg.obj设置为non-Parcelable的对象，应该使用Bundle
            //msg.obj = "你好，MyService，我是客户端";
            Bundle data = new Bundle(); // 创建数据包裹
            data.putString("msg", "你好，MyService，我是客户端"); // 放入握手消息
            data.putString("AppPackage",context.getPackageName()); // 放入App包名
            msg.setData(data); // 设置数据包裹
            //需要将Message的replyTo设置为客户端的clientMessenger，
            //以便Service可以通过它向客户端发送消息
            msg.replyTo = clientMessenger; // 设置回复信使
            try {
                Log.i(TAG, "客户端向service发送信息"); // 打印发送日志
                serviceMessenger.send(msg); // 发送握手消息
            } catch (RemoteException e) { // 捕获远程异常
                e.printStackTrace(); // 打印异常堆栈
                Log.i(TAG, "客户端向service发送消息失败: " + e.getMessage()); // 打印失败信息
            }
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
     * 客户端消息处理器（静态内部类，避免持有外部类引用导致内存泄漏）
     * 处理来自Service的SCPI指令消息
     */
    private static class ClientHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
//            Logger.i(TAG, "ClientHandler -> handleMessage");
            if (msg.what == RECEIVE_MESSAGE_CODE) { // Service通用消息
                Bundle data = msg.getData(); // 获取数据包裹
                if (data != null) { // 数据不为空
                    String str = data.getString("msg"); // 获取消息字符串
                    Logger.i(TAG, "客户端收到Service的消息: " + str); // 打印收到的消息
                }
            } else if (msg.what == RECIEVE_MESSAGE_SCPI) { // SCPI指令消息
                //接收到信息转到SCPI的COMMAND上
                Bundle data = msg.getData(); // 获取数据包裹
                String s = data.getString("message"); // 获取SCPI指令字符串
                if(!StrUtil.isEmpty(s)) { // 指令非空
                    AutoSave autoSave = AutoSave.getInstance(); // 获取自动保存实例
                    if(autoSave.isRun() // 自动保存正在运行
                            && autoSave.isSaving()){ // 且正在保存中
                        return; // 拒绝处理SCPI指令，避免保存期间干扰
                    }
                    autoSave.setUserInput(true); // 标记有用户输入
                    Log.d(TAG,"s:" + s); // 打印SCPI指令
                    SCPICommandDeal.getInstance().scpiParser(s); // 解析并执行SCPI指令
                    if(!s.startsWith("*IDN?")){ // 非IDN查询指令
//                        Command.get().getProduction().Lock(); // 锁定生产状态（已注释）
                    }
                }
            }
        }
    }


    /**
     * 绑定SCPI核心服务
     * 使用显式Intent绑定SCPIService
     */
    public void bind() {
        Logger.i("开始绑定服务！"); // 打印开始绑定日志
        if (!isBound) { // 尚未绑定
            Intent intent = new Intent(); // 创建Intent
            ComponentName cn = new ComponentName(SERVICE_ACTION, SERVICE_CLASS); // 创建组件名
            intent.setComponent(cn); // 设置组件
            try {
                context.bindService(intent, conn, Context.BIND_AUTO_CREATE); // 绑定Service（自动创建）
                Logger.i("绑定服务完成 ！"); // 打印绑定完成日志
            } catch (Exception e) { // 捕获异常
                e.printStackTrace(); // 打印异常堆栈
                Log.e("DemoLog", e.getMessage()); // 打印错误消息
            }
            Log.i(TAG, "client onbind!"); // 打印绑定日志
        }

    }

    /**
     * 解绑SCPI核心服务
     */
    public void unBind() {
//        if (isBound) {
            Log.i(TAG, "客户端调用unbindService方法"); // 打印解绑日志
            context.unbindService(conn); // 解绑Service

    }


}
