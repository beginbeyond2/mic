package com.micsig.tbook.tbookscope.services.ExternalKeys.client;

import static android.content.Context.BIND_AUTO_CREATE;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.struct.ExternalKeysMsg_ToMCU;
import com.micsig.tbook.tbookscope.top.layout.userset.TopMsgWirelessKeyboard;
import com.micsig.tbook.tbookscope.util.CacheUtil;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by liwb on 2017/12/4.
 */

public class ExternalKeysOnBindService {
    private static String TAG = "ExternalKeys";
    private static final int SEND_MESSAGE_CODE = 0x0001;
    private static final int RECEIVE_MESSAGE_CODE = 0x0002;
    private static final int RECEIVE_MESSAGE_MCUTOARM = 0x0003;
    private static final int RECEIVE_MESSAGE_EDITOR = 0x0004;
    private static final int RECEIVE_MESSAGE_BatteryProtection=0x0007;

    private static final int SEND_MESSAGE_ARMTOMCU = 0x0005;
    private static final int SEND_MESSAGE_BOOTPROTECTION=0x0006;
    private static final int SEND_MESSAGE_SCREENSHOT = 0x0008;

    private static final int RECEIVE_MESSAGE_WIRELESS_ID = 0x0009;
    private static final int RECEIVE_MESSAGE_WIRELESS_BATTERY = 0x000A;
    private static final int SEND_MESSAGE_WIRELESS_ID = 0x000B;

    private static final int RECEIVE_TIME_OUT = 0x1001;

    private static final int SEND_SHAKEHANDS = 0x1002;

    private static final int SEND_KEEPLIVE = 0x1003;

    private static final int SEND_SCOPE_AUTOSAVE = 0x1005;

    private Context context = null;
    private MainViewGroup mainViewGroup = null;
    private boolean isBound = false;
    private static boolean dealKeys=false;

    public static long wirelessId = 0;
    public static int wirelessBattery = 0;
    public static long wirelessBatteryHeartbeat = 0;

    //用于启动MyService的Intent对应的action
//    private final String SERVICE_ACTION = "com.micsig.tbook.tbookscope";
//    private final String SERVICE_PACKAGE = "com.micsig.tbook.tbookscope.debug";
//    private final String SERVICE_CLASS = "com.micsig.tbook.tbookscope.services.ExternalKeys.service.ExternalKeysService";
    private final String SERVICE_ACTION = "com.micsig.coreservice";
    private final String SERVICE_CLASS = "com.micsig.coreservice.ExternalKeysService.ExternalKeysService";

    private Messenger serviceMessenger = null;
    private ClientHandler clientHandler = new ClientHandler();
    private Messenger clientMessenger = null;

    public ExternalKeysOnBindService(Context context, MainViewGroup mainViewGroup) {
        this.context = context;
        this.mainViewGroup = mainViewGroup;
        initControl();
        clientMessenger = new Messenger(clientHandler);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEY_TOMCU).subscribe(consumerToMCU);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerMainLoadCache);
    }

    private Consumer<ExternalKeysMsg_ToMCU> consumerToMCU = new Consumer<ExternalKeysMsg_ToMCU>() {
        @Override
        public void accept(@NonNull ExternalKeysMsg_ToMCU externalKeysMsg_toMCU) throws Exception {

            byte[] b = ExternalKeysProtocol.parseToMCU(externalKeysMsg_toMCU);
            if (b == null) return;
            if(dealKeys) {
                sendMessage(b);
            }else{
                byte[] x = new byte[b.length];
                for (int i=0;i<b.length;i++){
                    x[i] = 0;
                }
                sendMessage(x);
            }
        }
    };

    private Consumer<LoadCache> consumerMainLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("ExtKeyBindServer");
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    byte[] b = ExternalKeysProtocol.parseToMCU(null);
                    if (b == null) return;
                    sendMessage(b);
                }
            }).start();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_ExternalKeysOnBindService,true);
            sendTimeout();
        }
    };

    public void sendTimeout(){
        clientHandler.sendEmptyMessageDelayed(RECEIVE_TIME_OUT,25);
    }

    private void keeplive(){
        if(clientHandler.hasMessages(SEND_KEEPLIVE)) {
            clientHandler.removeMessages(SEND_KEEPLIVE);
        }
        clientHandler.sendEmptyMessageDelayed(SEND_KEEPLIVE, 10000);
    }

    private class ClientHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what!=RECEIVE_TIME_OUT) {
                 //Log.d("Tag.Debug", String.format("ClientHandler.handleMessage: %s",msg ));
            }


            if (!CacheUtil.get().isLoadComplete()) {
                if(msg.what == SEND_SHAKEHANDS){
                    clientHandler.sendEmptyMessageDelayed(SEND_SHAKEHANDS,100);
                }else{
                    Log.d(TAG,"msg.what:" + msg.what);
                }
                return;
            }
            if(msg.what == SEND_SHAKEHANDS){
                sendShakehands();
                keeplive();
            }else if (msg.what == RECEIVE_MESSAGE_CODE) {
                Bundle data = msg.getData();
                if (data != null) {
                    String str = data.getString("msg");
                    Logger.i(TAG, "客户端收到Service的消息: " + str);
                }
            } else if (msg.what == RECEIVE_MESSAGE_MCUTOARM) {
                if (dealKeys==false) {
                    Log.i(TAG,"msg.what = RECEIVE_MESSAGE_MCUTOARM dealKeys=false!!!");
                    return;
                }
                //按键
                Bundle data = msg.getData();
                byte[] b = data.getByteArray("mcuToArm");
                //Log.d("Tag.Debug", String.format("ClientHandler.handleMessage: %s", Arrays.toString(b) ));
                boolean bValid = false;
                for(int i=0;i<b.length;i++){

                    if(b[i] != 0){
                        bValid = true;
                    }
                }

                if (b != null && bValid) {
                    ExternalKeysProtocol.parseMCUTOARM(b);
                }else{
                    Log.i(TAG,"bValid:" + bValid);
                }
            } else if (msg.what == RECEIVE_MESSAGE_EDITOR) {
                if (dealKeys==false) {
                    Logger.i(TAG,"msg.what = RECEIVE_MESSAGE_EDITOR dealKeys=false!!!");
                    return;
                }
                //旋钮
                Bundle data = msg.getData();
                byte[] b1 = data.getByteArray("editor");

                if (b1 != null && !(b1[0] == 0 && b1[1] == 0)) {
                    ExternalKeysProtocol.parseEditor(b1);
                }
            }else if (msg.what==RECEIVE_MESSAGE_BatteryProtection){
                if (dealKeys==false){
                    Logger.i(TAG,"msg.what==RECEIVE_MESSAGE_BatteryProtection=false!!!");
                    return;
                }
                //电池保护
                Bundle data = msg.getData();
                byte[] b1 = data.getByteArray("batteryProtection");
                if (b1 != null && !(b1[0] == 0 )) {
//                    ExternalKeysProtocol.parseBatteryProtection(b1);
                }
            }else if(msg.what == RECEIVE_TIME_OUT){
                ExternalKeysProtocol.moveValueLevel();
                sendTimeout();

            }else if(msg.what == RECEIVE_MESSAGE_WIRELESS_ID){
                Bundle data = msg.getData();
                wirelessId = data.getLong("WirelessPairID");
                RxBus.getInstance().post(RxEnum.WIRELESS_KEYBOARD_STAT,new TopMsgWirelessKeyboard(wirelessId,wirelessBattery,wirelessBatteryHeartbeat));
            }else if(msg.what == RECEIVE_MESSAGE_WIRELESS_BATTERY){
                Bundle data = msg.getData();
                wirelessBattery = data.getInt("WirelessPairBattery");
                wirelessBatteryHeartbeat = data.getLong("WirelessBatteryHeartbeat");
                RxBus.getInstance().post(RxEnum.WIRELESS_KEYBOARD_STAT,new TopMsgWirelessKeyboard(wirelessId,wirelessBattery,wirelessBatteryHeartbeat));
            }else if(msg.what == SEND_KEEPLIVE){
                sendKeeplive();
                keeplive();
            }
        }
    }

    public void sendAutoSave(boolean bAutoSave){
        Message msg = Message.obtain();
        msg.what = SEND_SCOPE_AUTOSAVE;
        Bundle data = new Bundle();
        data.putBoolean("SCOPE_AUTOSAVE", bAutoSave);
        msg.setData(data);
        try {
            if (serviceMessenger == null) {
                Log.i(TAG, "serviceMessenger null");
                return;
            }
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendKeeplive(){
        Message msg = Message.obtain();
        msg.what = SEND_KEEPLIVE;
        msg.replyTo = clientMessenger;
        try {
            if (serviceMessenger == null) {
                Log.i(TAG, "serviceMessenger null");
                return;
            }
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void screenshot(boolean bTimestamp, boolean bInvert, String filePath, String fileName){
        screenshot(bTimestamp,bInvert,filePath,fileName,false);
    }
    public void screenshot(boolean bTimestamp, boolean bInvert, String filePath, String fileName,boolean bAutosave) {
        boolean bThumbnail = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SAVETHUMBNAIL);
        Message msg = Message.obtain();
        msg.what = SEND_MESSAGE_SCREENSHOT;
        msg.arg1 = bTimestamp ? 1 : 0;
        msg.arg2 = (bInvert ? 1 : 0) | (bAutosave | !bThumbnail ? 2 : 0);
        Bundle data = new Bundle();
        data.putString("filePath", filePath);
        data.putString("fileName", fileName);
        msg.setData(data);
        msg.replyTo = clientMessenger;
        try {
            if (serviceMessenger == null) {
                Log.i(TAG, "serviceMessenger null");
                return;
            }
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unbindWirelessId(){
        wirelessId = 0;
        Message msg = Message.obtain();
        msg.what = SEND_MESSAGE_WIRELESS_ID;
        msg.arg1 = 0;
        msg.arg2 = 0;
        try {
            if (serviceMessenger == null) {
                Log.i(TAG, "serviceMessenger null");
                return;
            }
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息，还未进行封装，在调用前应该有一个转换的功能。
     *
     * @param b 根据协议进行转换。
     */
    public void sendMessage(byte[] b) {

        Message msg = Message.obtain();
        msg.what = SEND_MESSAGE_ARMTOMCU;
        Bundle data = new Bundle();
        data.putByteArray("armToMcu", b);
        msg.setData(data);
        msg.replyTo = clientMessenger;
        try {

            if (serviceMessenger == null) {
                Log.i(TAG, "serviceMessenger null");
                return;
            }
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void sendShakehands(){
        Message msg = Message.obtain();
        msg.what = SEND_MESSAGE_CODE;

        Bundle data = new Bundle();
        data.putString("msg", "你好，MyService，我是客户端");
        data.putString("AppPackage",context.getPackageName());
        msg.setData(data);
        msg.replyTo = clientMessenger;
        try {
            Log.i(TAG, "客户端向service发送信息");
            if(serviceMessenger != null) {
                serviceMessenger.send(msg);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.i(TAG, "客户端向service发送消息失败: " + e.getMessage());
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

            clientHandler.sendEmptyMessage(SEND_SHAKEHANDS);
            keeplive();
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

    public boolean isExistServices() {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.toString().equals(SERVICE_CLASS)) {
                Log.i(TAG, "find package:" + runningService.get(i).service.getClassName());
                return true;
            }
            Log.i(TAG, "running package:" + runningService.get(i).service.getClassName());
        }
        Log.i(TAG, "No Service!");
        return false;
    }

    public void bind() {
        Logger.i("开始绑定服务！");
        if (!isBound) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName(SERVICE_ACTION, SERVICE_CLASS);
            intent.setComponent(cn);
            try {
                context.bindService(intent, conn, BIND_AUTO_CREATE);
                Logger.i("绑定服务完成 ！");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("DemoLog", e.getMessage());
            }
            Log.i(TAG, "client onbind!");
        }
    }

    public void unBind() {
        if (isBound) {
            Log.i(TAG, "客户端调用unbindService方法");
            context.unbindService(conn);
        }
    }

    public void setDealKeys(boolean dealKeys){
        //Log.d("Tag.Debug", String.format("ExternalKeysOnBindService.setDealKeys: %s",dealKeys ));
        this.dealKeys=dealKeys;
        if(!dealKeys){
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU, new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_ALL, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
        }
    }
    public boolean isDealKeys(){
        return this.dealKeys;
    }
}
