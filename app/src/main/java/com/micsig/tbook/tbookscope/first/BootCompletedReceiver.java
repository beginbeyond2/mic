package com.micsig.tbook.tbookscope.first;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.hardware.Hardware;
import com.micsig.tbook.scope.Auto.Auto;
import com.micsig.tbook.scope.Data.AutoSave;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.struct.ExternalKeysMsg_ToMCU;
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.util.App;


public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";
    /**
     * 开机广播
     */
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    /**
     * 关机广播
     */
    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

    private static final String ACTION_STANDBY = "android.intent.action.STANDBY";

    private static final String ACTION_STANDBY_OUT = "android.intent.action.STANDBY_OUT";

    private static boolean bAutoRange = false;
    private static boolean bAuto = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG,"BootCompletedReceiver:onReceive() :" +App.get()  +"," + intent.getAction());
        if (ACTION.equals(intent.getAction())) {

            App.setDropDownBoxVisiable(context,false);


            //开机自启动app
            Intent intent1 = new Intent(context, FirstActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        } else if (ACTION_SHUTDOWN.equals(intent.getAction())) {

            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU, new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_ALL, ExternalKeysMsg_ToMCU.STATE_LED_OFF));
            SaveManage.getInstance().saveToDefaultSaveName();
            SaveManage.getInstance().saveToOtherSaveName();
        }else if(ACTION_STANDBY.equals(intent.getAction())){
            AutoSave.getInstance().setUserInput(true);
            if(App.isMainActivity()){
                Scope scope = Scope.getInstance(context);
                if(scope != null){
                    Auto auto = Auto.getInstance();
                    bAuto = auto.isAuto();
                    bAutoRange = auto.isAutoRangeEnable();
                    scope.setStandby(true);
                    scope.setRun(false);
                    scope.clearWave();
                }
                ms_sleep(300);
            }

            Hardware.getInstance(context).standby();
            SaveManage.getInstance().saveToDefaultSaveName();
            SaveManage.getInstance().saveToOtherSaveName();
        }else if(ACTION_STANDBY_OUT.equals(intent.getAction())){

            AutoSave.getInstance().setUserInput(true);
            Hardware.getInstance(context).resume();
            ScreenControls.getInstance().unLockScreen(0);
            if(App.isMainActivity()) {
                Scope scope = Scope.getInstance(context);
                if (scope != null) {
                    scope.resume(true);
                    scope.setRun(true);
                    scope.setStandby(false);
                    if(bAuto && bAutoRange) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!Auto.getInstance().isAuto()){
                                    ExternalKeysCommand.get().clickAuto();
                                    handler.postDelayed(this,1000);
                                }
                            }
                        }, 5000);
                    }
                }
            }
        }
    }

    static void ms_sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
