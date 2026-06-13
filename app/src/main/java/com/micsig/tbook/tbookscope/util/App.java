package com.micsig.tbook.tbookscope.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.chillingvan.canvasgl.util.Loggers;
import com.micsig.base.Logger;
import com.micsig.base.Utils;
import com.micsig.smart.Property;
import com.micsig.smart.PropertyManage;
import com.micsig.tbook.hardware.Hardware;
import com.micsig.tbook.hardware.HwManager;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.tbookscope.BuildConfig;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.config.IConfig;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.tools.SaveManage;




/**
 * Created by Administrator on 2017/4/5.
 */

public class App extends Application {
    private static final String TAG = "App";

    private static boolean isDebug = false;
    public static String LANGUAGE = "language";
    private static App app;
    private boolean isInit = false;

    private String getSN(){
        PropertyManage propertyManage = PropertyManage.getInstance();
        propertyManage.update();
        Property property = propertyManage.getProperty();
        return property.getDisplaySN();
    }

    private void initUsbInfo(Scope scope){
        PropertyManage propertyManage = PropertyManage.getInstance();
        propertyManage.update();
        Property property = propertyManage.getProperty();
        String product = "";
        String serial = "";
        product = property.getType();
        serial = property.getDisplaySN();
        if(product == null
                || product.isEmpty() ){
            product = Build.MODEL;
        }
        if(serial ==  null
                || serial.isEmpty()){
            serial = property.getSN();
        }
        if(serial ==  null
                || serial.isEmpty()){
            serial = "12345678ABCDEF";
        }
        String ver = getFwVersion(property);
        scope.setUsbInfo(product,serial,ver);

    }

    public static String getFwVersion(Property property){
        String ver = property.getHwVersion();
        if (ver != null && ver.length()>0){
            ver = ver + "." + BuildConfig.VERSION_CODE + "." + Scope.fpgaVer;
        }
        else {
            ver = "1." + BuildConfig.VERSION_CODE + "." + Scope.fpgaVer;
        }
        return ver;
    }

    public static void RefreshFwVersion(){
        PropertyManage propertyManage = PropertyManage.getInstance();
        propertyManage.update();
        Property property = propertyManage.getProperty();
        Scope scope = Scope.getInstance();
        scope.setUsbInfo(scope.getProduct(),scope.getSn(),getFwVersion(property));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HwManager.setString("app.micsig","1");
        App.setDropDownBoxVisiable(this,false);


        app = this;
        isDebug = BuildConfig.LOG_DEBUG;
        try {
            MCrash.getInstance().init(this,isDebug);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Logger.DEBUG = isDebug;
        Loggers.DEBUG = isDebug;
        Scope.getInstance(getApplicationContext());

        PropertyManage.getInstance().init(getApplicationContext(),String.format("%016x",Scope.fpgaDna));


        Screen.init(this);
        // PrefUtil.setProgress(this);
        GlobalVar.get().init(this);
        IConfig config = ScopeConfig.getConfig();
        if(config.isValidProduct()) {
            initUsbInfo(Scope.getInstance());
            Scope.getInstance().initScope();
            SaveManage.getInstance().init();
            CacheUtil.get().initStateCacheLoad();
            Utils.InitSignal();
        }else{
            Toast.makeText(this, R.string.app_system_supported,Toast.LENGTH_LONG).show();
            finish();
        }
    }
    @Override
    public void onTerminate() {
        Scope.getInstance().standby();
        super.onTerminate();
    }

    public static void finish() {
        if(mainActivity != null){
            mainActivity.finish();
            mainActivity = null;
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public boolean isInit() {
        return isInit;
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    public static App get() {
        return app;
    }

    public static boolean IsDebug() {
        return isDebug;
    }

    private volatile static boolean bMainActivityAlive = false;
    public static void setMainActivityAlive(boolean mainActivityAlive){
        bMainActivityAlive = mainActivityAlive;
    }
    public static boolean isMainActivity(){
        return bMainActivityAlive;
    }

    private static MainActivity mainActivity;
    public static void setMainActivity(MainActivity activity){
        mainActivity = activity;
    }

    public static final String ACTION_HIDE_NAVIGATION = "action.ACTION_HIDE_NAVIGATION";
    public static void setDropDownBoxVisiable(Context context,boolean bVisiable){
        Intent intent = new Intent(ACTION_HIDE_NAVIGATION);
        intent.putExtra("state", bVisiable ? "false" : "true");  //允许下拉框
        context.sendBroadcast(intent);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
