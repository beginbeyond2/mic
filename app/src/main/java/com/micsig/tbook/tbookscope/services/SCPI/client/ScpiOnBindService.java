package com.micsig.tbook.tbookscope.services.SCPI.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.AutoSave;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.scpi.SCPICommandDeal;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * Created by liwb on 2018/8/8.
 */

public class ScpiOnBindService {
    private static final String TAG = "ScpiOnBindService";

    private static final int SEND_MESSAGE_CODE = 0x0001;
    private static final int SEND_MESSAGE_SCPI = 0x0003;

    private static final int RECEIVE_MESSAGE_CODE = 0x0002;
    private static final int RECIEVE_MESSAGE_SCPI = 0x0004;

    private static final String KEY_MESSAGE="message";
    private static final String KEY_SHARED_MEMORY="shared_memory";
    private static final String KEY_MESSAGE_TYPE="message_type";
    private static final String KEY_TYPE_WAVEFORM="waveform";

    private Context context = null;
    private MainViewGroup mainViewGroup = null;

    private boolean isBound = false;

    //用于启动MyService的Intent对应的action
    private final String SERVICE_ACTION = "com.micsig.coreservice";
    private final String SERVICE_CLASS = "com.micsig.coreservice.SCPIService.SCPIService";

    private Messenger serviceMessenger = null;
    private Messenger clientMessenger = new Messenger(new ClientHandler());

    public ScpiOnBindService(Context context, MainViewGroup mainViewGroup) {
        this.context = context;
        this.mainViewGroup = mainViewGroup;
//        initControl();
    }


    public void sendMessage(String str) {
        Message msg = Message.obtain();
        msg.what = SEND_MESSAGE_SCPI;
        Bundle data = new Bundle();
        data.putString("message", str);
        msg.setData(data);
        try {
//            Log.i(TAG, "发送信息");
            if (serviceMessenger == null) {
                Log.i(TAG, "serviceMessenger null");
                return;
            }
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String str,boolean isContainWaveForm) {
        Message msg = Message.obtain();
        msg.what = SEND_MESSAGE_SCPI;
        Bundle data = new Bundle();
        data.putString(KEY_MESSAGE, str);
        if (isContainWaveForm){
            data.putString(KEY_MESSAGE_TYPE, KEY_TYPE_WAVEFORM);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                data.putParcelable(KEY_SHARED_MEMORY,SCPICommandDeal.getInstance().getSharedMem());
            }
        }
        msg.setData(data);
        try {
//            Log.i(TAG, "发送信息");
            if (serviceMessenger == null) {
                Log.i(TAG, "serviceMessenger null");
                return;
            }
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //客户端与Service建立连接
            Log.i(TAG, "客户端 onServiceConnected");

            //我们可以通过从Service的onBind方法中返回的IBinder初始化一个指向Service端的Messenger
            serviceMessenger = new Messenger(binder);
            isBound = true;

            Message msg = Message.obtain();
            msg.what = SEND_MESSAGE_CODE;

            //此处跨进程Message通信不能将msg.obj设置为non-Parcelable的对象，应该使用Bundle
            //msg.obj = "你好，MyService，我是客户端";
            Bundle data = new Bundle();
            data.putString("msg", "你好，MyService，我是客户端");
            data.putString("AppPackage",context.getPackageName());
            msg.setData(data);
            //需要将Message的replyTo设置为客户端的clientMessenger，
            //以便Service可以通过它向客户端发送消息
            msg.replyTo = clientMessenger;
            try {
                Log.i(TAG, "客户端向service发送信息");
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.i(TAG, "客户端向service发送消息失败: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //客户端与Service失去连接
            serviceMessenger = null;
            isBound = false;
            Log.i(TAG, "客户端 onServiceDisconnected");
            bind();
        }
    };

    private static class ClientHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
//            Logger.i(TAG, "ClientHandler -> handleMessage");
            if (msg.what == RECEIVE_MESSAGE_CODE) {
                Bundle data = msg.getData();
                if (data != null) {
                    String str = data.getString("msg");
                    Logger.i(TAG, "客户端收到Service的消息: " + str);
                }
            } else if (msg.what == RECIEVE_MESSAGE_SCPI) {
                //接收到信息转到SCPI的COMMAND上
                Bundle data = msg.getData();
                String s = data.getString("message");
                if(!StrUtil.isEmpty(s)) {
                    AutoSave autoSave = AutoSave.getInstance();
                    if(autoSave.isRun()
                            && autoSave.isSaving()){
                        return;
                    }
                    autoSave.setUserInput(true);
                    Log.d(TAG,"s:" + s);
                    SCPICommandDeal.getInstance().scpiParser(s);
                    if(!s.startsWith("*IDN?")){
//                        Command.get().getProduction().Lock();
                    }
                }
            }
        }
    }


    public void bind() {
        Logger.i("开始绑定服务！");
        if (!isBound) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName(SERVICE_ACTION, SERVICE_CLASS);
            intent.setComponent(cn);
            try {
                context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
                Logger.i("绑定服务完成 ！");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("DemoLog", e.getMessage());
            }
            Log.i(TAG, "client onbind!");
        }

    }

    public void unBind() {
//        if (isBound) {
            Log.i(TAG, "客户端调用unbindService方法");
            context.unbindService(conn);

    }


}
