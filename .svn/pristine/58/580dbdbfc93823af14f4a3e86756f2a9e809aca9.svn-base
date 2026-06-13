package com.micsig.tbook.tbookscope.util;

import android.content.Context;

import com.micsig.tbook.tbookscope.tools.Tools;

import java.io.File;

import xcrash.ICrashCallback;
import xcrash.TombstoneManager;
import xcrash.XCrash;
public class MCrash {
    private static final String RSA_KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final ICrashCallback callback=new ICrashCallback() {
        @Override
        public void onCrash(String logPath, String emergency) throws Exception {
            HybridCryptoManager cryptoManager=new HybridCryptoManager();
            cryptoManager.importPublicKeyFromPem("-----BEGIN PUBLIC KEY-----" +
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsGTluAC7SPIxSx01vCFN" +
                    "2g5vOVdikZKzpqcxZ5Mxyv9BqnSWQ1BiYdC4C098V4KuztKYUDaKkTVs/HdHr3dc" +
                    "2D9cb4erdshK/k9GI1QXgIcDHeTAWWHJHPPX56xoTAIZWVEm+JJyxBnwrdZfrOoi" +
                    "CWoQjWodGe8hOG+IMGr3JIjCH1O73AogVlkmSIv1oPNCo8I19+ie6kvfPKhaNZzx" +
                    "9cdBrqnNxpJsuOZZ1GAAUfCYu+YOJLe60S4nHqEsgsQNnueHfDk2IkbqF5s9w6SA" +
                    "QlAnaSc5HbI3011QKnZOPjYiJTUW+UZHu/3HXBS6KWfKDfs6xpwwPYF4bJyYbPj2" +
                    "HwIDAQAB" +
                    "-----END PUBLIC KEY-----");
            File input=new File(logPath);
            File outputPath= new File(logPath.substring(0, logPath.lastIndexOf(".") + 1)+"Mcrash");

            //加密
            cryptoManager.encryptFile(input,outputPath);
            TombstoneManager.deleteTombstone(logPath);
//            //解密 需要私钥
//            cryptoManager.decryptFile(outputPath,deoutput);
        }
    };
    private static MCrash instance = new MCrash();;
    public static MCrash getInstance(){
        return instance;
    }
    public void init(Context context,boolean isDebug) throws Exception {
        //存储路径
        File f = new File("/storage/emulated/0/smart");
        String scopePath;
        if(f.exists()){
            scopePath= Tools.SMART_PATH;
        }else{
            scopePath= Tools.SCOPE_PATH;
        }
        String logPath="/storage/emulated/0/"+scopePath+"/log";
        //初始化XCrash
        if(isDebug){
            XCrash.InitParameters parameters=new XCrash.InitParameters()
                    .setLogDir(logPath);
            XCrash.init(context,parameters);
        }else{
            XCrash.InitParameters parameters=new XCrash.InitParameters()
                    .setJavaCallback(callback)
                    .setAnrCallback(callback)
                    .setNativeCallback(callback)
                    .setLogDir(logPath);
            XCrash.init(context,parameters);
        }

    }

}