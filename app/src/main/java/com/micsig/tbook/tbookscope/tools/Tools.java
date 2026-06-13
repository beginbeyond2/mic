package com.micsig.tbook.tbookscope.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.micsig.base.DoubleUtil;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.math.MathWave;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.scope.mem.Memory;
import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by liwb on 2017/9/26.
 */

public class Tools {
    private static final String TAG = "Tools";
    //region  全局
    public static final int SaveType_LOCAL = 0x00;
    public static final int SaveType_UDISK = 0x01;

    @IntDef({SaveType_LOCAL, SaveType_UDISK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SaveType {
    }

    public static final String SaveDir_DEFAULT = "default/";
    public static final String SaveDir_USERSET = "userset/";
    public static final String SaveDir_REFWAVE = "refwave/";
    public static final String SaveDir_REFDEFAULT = "refdefault/";
    public static final String SaveDir_CSVWAVE = "csvwave/";
    public static final String SaveDir_CSVSBT = "csvsbt/";
    public static final String SaveDir_BINWAVE = "binwave/";
    public static final String SaveDir_DCIM = "DCIM/";

    //endregion

    //region 静态变量
    private static Rect rectText = new Rect();

    private static long beginTime, endTime;
    //endregion

    public static Rect getTextRect(String text, Paint paint) {
        Rect rectText = new Rect();
        paint.getTextBounds(text, 0, text.length(), rectText);
        return rectText;
    }

    public static String getTextFormat(String text) {
        return text.replaceAll("[0-9]", "0");
    }

    public static void sleep(int delayTime_ms) {
        try {
            Thread.sleep(delayTime_ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static final String SCOPE_PATH = "Oscilloscope";
    public static final String SMART_PATH = "smart";
    /***
     * 返回默认存储路径
     * @param SaveType  存储类型
     * @return
     */
    public static String resultSavePath(@SaveType int SaveType, String SaveDir, Activity activity) {
        if (SaveDir == null || SaveDir.equals("")) SaveDir = "";
        String path = "";
        switch (SaveType) {
            case SaveType_LOCAL: {
                path = Environment.getExternalStorageDirectory().getAbsolutePath();
                Logger.i(TAG, "Path:" + path);
            }
            break;
            case SaveType_UDISK: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    path=getUDiskPath(App.get().getApplicationContext());
                }else {
                    path=Environment.getExternalStorageDirectory().toString();
                }
            }
            break;
            default: {
                path = Environment.getExternalStorageDirectory().toString();
            }
            break;
        }
        File f = new File(path + File.separator + SMART_PATH + File.separator);
        if(f.exists()){
            path += File.separator + SMART_PATH + File.separator;
        }else{
            path += File.separator + SCOPE_PATH + File.separator;
        }

        File file = new File(path);
        if (!file.exists()) {
            boolean b = file.mkdir();
            uDiskUpdate(path, activity);
            Logger.i(TAG, "mkdir :" + b);
        }
        path = path + SaveDir;
        file = new File(path);
        if (!file.exists()) {
            boolean b = file.mkdir();
            uDiskUpdate(path, activity);
            Logger.i(TAG, "mkdir saveDir:" + path);
        }
        return path;
    }

    public static String getSaveDataPath(){
        String path = "";
        path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File f = new File(path + File.separator + SCOPE_PATH + File.separator + "tmpdata"+ File.separator);
        if(!f.exists()){
            f.mkdirs();
        }
        return f.getAbsolutePath();
    }

    public static String resultDefaultSavePath(String saveDir, Activity activity) {
        String path = resultSavePath(Tools.SaveType_LOCAL, saveDir, activity);
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (path.startsWith(rootPath)) {
            path = path.substring(rootPath.length());
        }
        return path;
    }

    public static String resultAbDefaultSavePath(String saveDir, Activity activity) {
        return resultSavePath(Tools.SaveType_LOCAL, saveDir, activity);
    }


    public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";

    public static boolean uDiskUpdate(String path, Activity mainActivity) {
        if (mainActivity == null) return false;
        Intent mediaScanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);
        File f = new File(path);
        if (f.isFile()) {
            Uri uri = FileProvider.getUriForFile(mainActivity, "com.micsig.tbook.fileprovider", f);
            mediaScanIntent.setData(uri);
            mainActivity.sendBroadcast(mediaScanIntent);
        }
        return true;
    }

    public static boolean uDiskUpdate(String path, Context context) {
        if (context == null) return false;
        Intent mediaScanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);
        File f = new File(path);
        if (f.isFile()) {
            Uri uri = FileProvider.getUriForFile(context, "com.micsig.tbook.fileprovider", f);
            mediaScanIntent.setData(uri);
            context.sendBroadcast(mediaScanIntent);
        }
        return true;
    }

    public static boolean uDiskUpdateFile(String path, Context context) {
        if (context == null) return false;
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(new File(path)));
        context.sendBroadcast(mediaScanIntent);
        return true;
    }

    /***
     *返回当前U盘的挂载路径
     * @return
     */
    public static List<String> getAllExternalSdcardPath() {

        List<String> list = new ArrayList<>();
        for (String path : mapUdisk.values()) {
            list.add(path);
        }

        return list;
    }


    /**
     * UI 转 channelFactory
     *
     * @param channel
     * @return
     */
    public static int UIChannelToChannelFactory(int channel) {
        switch (channel) {
            case 0:
                return ChannelFactory.CH1;
            case 1:
                return ChannelFactory.CH2;
            case 2:
                return ChannelFactory.CH3;
            case 3:
                return ChannelFactory.CH4;
            case 4:
                return ChannelFactory.MATH1;
            case 5:
                return ChannelFactory.REF1;
            case 6:
                return ChannelFactory.REF2;
            case 7:
                return ChannelFactory.REF3;
            case 8:
                return ChannelFactory.REF4;
        }
        return ChannelFactory.CH1;
    }

    public static final int LevelType_Normal = 0;
    public static final int LevelType_High = 1;
    public static final int LevelType_Low = 2;

    public static final int LevelMode_Normal = 0;
    public static final int LevelMode_Bus = 1;

    /**
     * 输入当前通道和当前电平的距离view顶部的px像素值，得到当前通道电平的V/A值
     *
     * @param ch        1-4
     * @param levelType 0，普通，1搞，2地
     * @param levelMode 0，普通，1 总线
     */
    public static String getChannelLevel(int ch, int levelType, int levelMode) {
        Channel channel = ChannelFactory.getDynamicChannel(ch - 1);
        String key = CacheUtil.TRIGGER_CHANNEL;
        if (levelMode == LevelMode_Bus) {
            key = CacheUtil.VALUE_CHANNEL;
        }
        if (levelType == LevelType_High) {
            key += CacheUtil.HIGH;
        }
        key += ch;
        double val = getYTLevelCache(key);
        double chEvery = 1;
//        if (ch == CacheUtil.CH1) {
//            chEvery = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                    .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH1_VSCALEID));
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CacheUtil.CH1));
//            chEvery /= ScopeBase.getVerticalPerGridPixels();
//        } else if (ch == CacheUtil.CH2) {
//            chEvery = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                    .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH2_VSCALEID));
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CacheUtil.CH2));
//            chEvery /= ScopeBase.getVerticalPerGridPixels();
//        } else if (ch == CacheUtil.CH3) {
//            chEvery = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                    .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH3_VSCALEID));
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CacheUtil.CH3));
//            chEvery /= ScopeBase.getVerticalPerGridPixels();
//        } else if (ch == CacheUtil.CH4) {
//            chEvery = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                    .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH4_VSCALEID));
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CacheUtil.CH4));
//            chEvery /= ScopeBase.getVerticalPerGridPixels();
//        }
        String unit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + ch) == 0 ? "V" : "A";
        unit = Tools.getChanProbeTypeUnit(ch - 1) == 0 ? "V" : "A";
        if (TChan.isChan(ch)) {
            Channel chan = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ch));
            if (chan == null) return "";
//            chEvery = chan.getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_SCALE_ID + ch)) *;
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + ch));
            chEvery = chan.getVScaleVal();
            chEvery /= ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff();
//            return TBookUtil.getFourFromD_Trim0(ScopeBase.changeAccuracy(chEvery * val)) + unit;
            return TBookUtil.getFourFromD_Trim0(chEvery * val) + unit;
        } else if (ch == TChan.Ch8 + 1) {
            double temp = getExtTriggerValCache(key);
            unit = "V";
            return TBookUtil.getFourFromD_Trim0(temp) + unit;
        } else {
            return TBookUtil.getFourFromD_Trim0(chEvery * val) + unit;
        }
    }

    public static double ScaleToPix(int chIdx,double val){
        switch (chIdx){
            case ChannelFactory.CH1:
            case ChannelFactory.CH2:
            case ChannelFactory.CH3:
            case ChannelFactory.CH4:
            case ChannelFactory.CH5:
            case ChannelFactory.CH6:
            case ChannelFactory.CH7:
            case ChannelFactory.CH8:
            {
                Channel channel = ChannelFactory.getDynamicChannel(chIdx);
                double curPos =  (DoubleUtil.divide( val , channel.getVerticalPerPix()));
                double chPos=( GlobalVar.get().getMainWave().y/2- WaveManage.get().getPositionY(TChan.toUiChNo(chIdx)));
                double pix=GlobalVar.get().getMainWave().y/2-curPos-chPos;
//                Log.d("Tag.Debug", String.format("Tools.ScaleToPix: height/2:%s, val:%s,verPerPix:%s, curPos:%s,chPos:%s pix:%s",GlobalVar.get().getMainWave().y/2, val,channel.getVerticalPerPix(),curPos,chPos,pix ));
                return pix;
            }
            case ChannelFactory.MATH1:
            case ChannelFactory.MATH2:
            case ChannelFactory.MATH3:
            case ChannelFactory.MATH4:
            case ChannelFactory.MATH5:
            case ChannelFactory.MATH6:
            case ChannelFactory.MATH7:
            case ChannelFactory.MATH8:
            {
                MathChannel math = ChannelFactory.getMathChannel(chIdx);
                double curPos =(val / math.getVerticalPerPix());
                double chPos= ( GlobalVar.get().getMainWave().y/2- WaveManage.get().getPositionY(TChan.toUiChNo(chIdx)));
                double pix=GlobalVar.get().getMainWave().y/2-curPos-chPos;
//                Log.d("Tag.Debug", String.format("Tools.ScaleToPix: curPos:%s, chPos:%s ,pix:%s",curPos,chPos,pix ));
                return pix;
            }
            case ChannelFactory.REF1:
            case ChannelFactory.REF2:
            case ChannelFactory.REF3:
            case ChannelFactory.REF4:
            case ChannelFactory.REF5:
            case ChannelFactory.REF6:
            case ChannelFactory.REF7:
            case ChannelFactory.REF8:
            {
                RefChannel ref=ChannelFactory.getRefChannel(chIdx);
                double curPos=(val/ ref.getVerticalPerPix());
                double chPos= ( GlobalVar.get().getMainWave().y/2- WaveManage.get().getPositionY(TChan.toUiChNo(chIdx)));
                double pix=GlobalVar.get().getMainWave().y/2-curPos-chPos;
//                Log.d("Tag.Debug", String.format("Tools.ScaleToPix: curPos:%s, chPos:%s ,pix:%s",curPos,chPos,pix ));
                return pix;
            }
            default:  //S1,S2 就保存原来不变
                return 0;

        }
    }

    public static long TimebaseToPix(int chIdx,double val)
    {
        switch (chIdx){
            case ChannelFactory.CH1:
            case ChannelFactory.CH2:
            case ChannelFactory.CH3:
            case ChannelFactory.CH4:
            case ChannelFactory.CH5:
            case ChannelFactory.CH6:
            case ChannelFactory.CH7:
            case ChannelFactory.CH8:
            case ChannelFactory.S1:
            case ChannelFactory.S2:
            case ChannelFactory.S3:
            case ChannelFactory.S4:
            {
                return TimebaseToPix(val);
            }
            case ChannelFactory.MATH1:
            case ChannelFactory.MATH2:
            case ChannelFactory.MATH3:
            case ChannelFactory.MATH4:
            case ChannelFactory.MATH5:
            case ChannelFactory.MATH6:
            case ChannelFactory.MATH7:
            case ChannelFactory.MATH8:
            {
                MathChannel math=ChannelFactory.getMathChannel(chIdx);
                if  (math.getMathType()== MathWave.MATH_FFTWAVE){
                    long pix= math.getHorizontalAxisMathFFT().SCPIQueryPixInScreenFromTImePosVal(val);
                    long dd= math.getHorizontalAxisMathFFT().getXPosOfView();
                    long pos=GlobalVar.get().getMainWave().x/2+pix-dd;
                    return pos;
                }else {
                    return TimebaseToPix(val);
                }
            }
            case ChannelFactory.REF1:
            case ChannelFactory.REF2:
            case ChannelFactory.REF3:
            case ChannelFactory.REF4:
            case ChannelFactory.REF5:
            case ChannelFactory.REF6:
            case ChannelFactory.REF7:
            case ChannelFactory.REF8:
            default:
            {
                RefChannel ref= ChannelFactory.getRefChannel(chIdx);
//                if (ref.getRefType()==2){ //fft
////                    Log.d("Tag.Debug", String.format("Tools.TimebaseToPix: %s,%s",val,ref.getRefTimePerPix() ));
//                    long pix=(long)( val/ ref.getRefTimePerPix());
////                    long tt= ref.getRefMovPix();
//                    long dd= ref.getTimePoseOfViewPix();
//                    long pos=GlobalVar.get().getMainWave().x/2+pix-dd;
//                    return pos;
//                }else {
                if(ref!=null){
                    long pix=(long)Math.round(val/ref.getRefTimePerPix());
                    long tt=ref.getTimePoseOfViewPix();
//                    Log.d("Tag.Debug", String.format("Tools.TimebaseToPix: %s,%s",pix,tt ));
                    long pos= GlobalVar.get().getMainWave().x/2+pix-tt;
                    return pos;
                }else {
                    return 0;
                }

//                }
            }
        }

    }

    public static long TimebaseToPix(double val){
        int WPIID=1;
        if (Command.get().getDisplay().ZoomQ()==false){
            WPIID=HorizontalAxis.WPI_STANDARD;
        }
        long l=HorizontalAxis.TimebaseToPix(WPIID,val);
        long dd= HorizontalAxis.getInstance().getTimePoseOfViewPix();
        //逻辑通道，中心展开
//        int verBase= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_HORREF);
//        if (verBase==0 ){
//            dd=0;
//        }
        l=GlobalVar.get().getMainWave().x/2+l-dd;
//        Logger.i(Command.TAG,"TimebaseToPix pix:"+l+",time:"+val);
        return l;
    }

    public static double PixToTimebase(long offsetX){
        offsetX=GlobalVar.get().getMainWave().x/2-offsetX;
        int WPIID=1;
        if (Command.get().getDisplay().ZoomQ()==false){
            WPIID=HorizontalAxis.WPI_STANDARD;
        }
        double d=HorizontalAxis.PixToTimebase(WPIID,offsetX);
//        Logger.i(Command.TAG,"PixToTimebase val:"+d+",pix:"+offsetX+",isYT:"+(WPIID==0));

        return d;
    }



    /**
     * 返回阈值电平值
     *
     * @param ch   当前通道
     * @param pxY  当前的像素位置
     * @param dang 档位
     * @return 返回阈值电平值
     */
    public static int getChannelDiscreetVoltageLevel(int ch, long pxY, int dang) {
        double chEvery = ChannelFactory.getDynamicChannel(ch - 1).getAdPix();
        int Level = (int) ((127 - pxY) / chEvery);
        Logger.i(TAG, "discreetVoltageLevel:" + Level + "=ChEvery:" + chEvery + "/pxY:" + pxY);
        return Level;
    }

    public static boolean memcmp(byte[] data1, byte[] data2, int len) {
        if (data1 == null && data2 == null) {
            return true;
        }
        if (data1 == null || data2 == null) {
            return false;
        }
        if (data1 == data2) {
            return true;
        }

        boolean bEquals = true;
        int i;
        for (i = 0; i < data1.length && i < data2.length && i < len; i++) {
            if (data1[i] != data2[i]) {
                bEquals = false;
                break;
            }
        }

        return bEquals;
    }


    /**
     * 是否是慢时基
     * 不管当前，只根据模拟通道判断
     */
    public static boolean isSlowTimeBase() {
        String timeBaseScale;
//        if (ChannelFactory.REF1 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF2 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF3 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF4 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF5 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF6 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF7 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF8 == ChannelFactory.getChActivate()
//                || ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate())
//        ) {
//            return false;
//        } else {
            timeBaseScale = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE);
            //大于等于200ms时，为慢时基状态
            return TBookUtil.getPsFromTime(timeBaseScale) / 1000 >= 100 * 1000 * 1000;
//        }
    }

    public static boolean isSlowTimeBase(String timeBaseScale) {
        return TBookUtil.getPsFromTime(timeBaseScale) / 1000 >= 100 * 1000 * 1000;
    }

    public static boolean isChaZhiDang() {

        Scope scope  = Scope.getInstance();
        int cnt = scope.getChannelSampOnCnt(true);
        String timeBaseScale = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE);
        long timeBaseNs = TBookUtil.getPsFromTime(timeBaseScale) / 1000;
        if(cnt < 8){
            return timeBaseNs <= 10;
        }else{
            return timeBaseNs <= 20;
        }
    }

    public static void __printLine() {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
        StackTraceElement tmp = trace[1];
        System.out.println(tmp.getClassName() + "." + tmp.getMethodName()
                + "(" + tmp.getFileName() + ":" + tmp.getLineNumber() + ")");
    }

    public static boolean saveImg(Bitmap bitmap, String name, Context context) {
        try {
            String sdcardPath = System.getenv("EXTERNAL_STORAGE");      //获得sd卡路径
            String dir = sdcardPath + "/Pictures/";                    //图片保存的文件夹名
            File file = new File(dir);                                 //已File来构建
            if (!file.exists()) {                                     //如果不存在  就mkdirs()创建此文件夹
                file.mkdirs();
            }
            //Log.i("SaveImg", "file uri==>" + dir);
            File mFile = new File(dir + name);                        //将要保存的图片文件
            if (mFile.exists()) {
                //Toast.makeText(context, "该图片已存在!", Toast.LENGTH_SHORT).show();
                return false;
            }

            FileOutputStream outputStream = new FileOutputStream(mFile);     //构建输出流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);  //compress到输出outputStream
            Uri uri = Uri.fromFile(mFile);                                  //获得图片的uri
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)); //发送广播通知更新图库，这样系统图库可以找到这张图片
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean saveImgOverlap(Bitmap bitmap, String name, Context context) {
        try {
            String sdcardPath = System.getenv("EXTERNAL_STORAGE");      //获得sd卡路径
            String dir = sdcardPath + "/Pictures/";                    //图片保存的文件夹名
            File file = new File(dir);                                 //已File来构建
            if (!file.exists()) {                                     //如果不存在  就mkdirs()创建此文件夹
                file.mkdirs();
            }
            //Log.i("SaveImg", "file uri==>" + dir);
            File mFile = new File(dir + name);                        //将要保存的图片文件
            if (mFile.exists()) {
                //Toast.makeText(context, "该图片已存在!", Toast.LENGTH_SHORT).show();
                mFile.delete();
                //return false;
            }

            FileOutputStream outputStream = new FileOutputStream(mFile);     //构建输出流
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);  //compress到输出outputStream

            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri uri = Uri.fromFile(mFile);                                  //获得图片的uri
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)); //发送广播通知更新图库，这样系统图库可以找到这张图片
            Memory.Sync();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String genNameByDateTime() {
        SimpleDateFormat sTimeFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
        String date = sTimeFormat.format(new Date());
        return date;
    }

    public static void beginTime() {
        beginTime = System.currentTimeMillis();
    }

    public static long endTime() {
        endTime = System.currentTimeMillis();
        return endTime - beginTime;
    }

    public interface CallBack {
        void doSomething();
    }

    @SuppressLint("DefaultLocale")
    public static void countTime(CallBack callBack) {
        long beginTime = System.currentTimeMillis();
        callBack.doSomething();
        long endTime = System.currentTimeMillis();
        Logger.i(TAG, String.format("耗时：%d ms", endTime - beginTime));
    }


    //判断文件是否存在
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String getASCIIFromInt(int num, String replace) {
        char s1 = ((num >= 32 && num <= 126) || (num >= 0xA1 & num <= 0xFF && num != 0xAD)) ? (char) num : '\0';
        if (s1 == '\0') {
            return replace;
        } else {
            return String.valueOf(s1);
        }
    }

    /**
     * 深度复制
     *
     * @param src
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<T> dest = (List<T>) in.readObject();
        return dest;
    }

    public static <T> T deepCopy(T object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        bos.close();
        byte[] byteArray = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (T) ois.readObject();
    }


    public static <T> ArrayList<T> jsonToArrayList(List<T> sourceList, Class<T> clazz) {
        Gson gson = new Gson();
        String json = gson.toJson(sourceList);

        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();
        ArrayList<JsonObject> jsonObjects = new Gson().fromJson(json, type);

        ArrayList<T> arrayList = new ArrayList<>();
        for (JsonObject jsonObject : jsonObjects) {
            arrayList.add(new Gson().fromJson(jsonObject, clazz));
        }
        return arrayList;
    }

    private static String[] Hexs = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static StringBuilder hex = new StringBuilder();

    public static synchronized String ByteToHexString(byte b) {
        hex.delete(0, hex.length());
        int i32 = b & 0x00FF;
        hex.append(Hexs[i32 / 16]);
        hex.append(Hexs[i32 % 16]);
        return hex.toString();
    }

    public static String ByteToHexString(byte b, int bit) {
        StringBuilder hex = new StringBuilder();
        int i32 = b & 0x00FF;
        if (bit > 4) hex.append(Hexs[i32 / 16]);
        hex.append(Hexs[i32 % 16]);
        return hex.toString();
    }

    /**
     *
     * @param hexString
     * @return
     */
    public static int HexStringToInt(String hexString){
        int result=0;
        try {
            result=Integer.parseInt(hexString.trim(),16);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static long HexStringToLong(String hexString){
        long result=0;
        try {
            result=Long.parseLong(hexString.trim(),16);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static void AppendContentToFile(String pathName, String content) {
//        if (isDebug) return;

        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(pathName, true);
            writer.write(content + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveStringBuild(String pathName, StringBuilder sb) {
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(pathName);
//            fos.write(sb.toString().getBytes());
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(pathName, true));
            bw.write(sb.toString());
            bw.flush();
            bw.close();
            Memory.Sync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> mapUdisk = new HashMap<>();

    public static boolean checkUdiskState() {
        return mapUdisk.size() > 0;
    }

    public static boolean isExistUDisk(Context context){
//        Log.d("Tag.Debug", String.format("isExistUDisk: %b", Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)));
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            StorageManager storageManager=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<StorageVolume> storageVolumes= storageManager.getStorageVolumes();
                for(StorageVolume volume :storageVolumes){
//                    Log.d("Tag.Debug", String.format("isExistUDisk: %s",volume.toString() ));
                    if (!volume.isEmulated() && !volume.isPrimary() ){
                        return true;
                    }
                }
            }
        }else {
            return false;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static String getUDiskPath(Context context){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            StorageManager storageManager=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
                List<StorageVolume> storageVolumes= storageManager.getStorageVolumes();
                for(StorageVolume volume :storageVolumes){
                    if (!volume.isEmulated() && !volume.isPrimary() ){
                        String path=volume.getDirectory().getAbsolutePath();
//                        Log.d("Tag.Debug", String.format("getUDiskPath: %s",path ));
                        return path;
                    }
                }

        }
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }


    public static String getUdiskPath() {
        String udiskPath = null;
        for (String upath : mapUdisk.values()) {
            udiskPath = upath;
            break;
        }
        return udiskPath;
    }

    public static double getYTChannelPositionUI(int chIdx) {
        double y = 0;
        if (TChan.isChan(chIdx)) {
            y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION + chIdx);
        } else if (TChan.isMath(chIdx)) {
            int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + chIdx);
            if (mathType == CacheUtil.MATHTYPE_DW) {
                y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION + chIdx);
            } else if (mathType == CacheUtil.MATHTYPE_AXB) {
                y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION + chIdx);
            } else if (mathType == CacheUtil.MATHTYPE_AM) {
                y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION + chIdx);
            } else {
                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + chIdx) == 1) {
                    y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION + chIdx);
                } else {
                    y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + chIdx);
                }
            }
        } else if (TChan.isRef(chIdx)) {
            y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_REF_Y_POSITION + chIdx);
        }
        return y;
    }

    public static double getZoomChannelPositionUI(int chIdx) {
        return ScopeBase.changeAccuracy(getYTChannelPositionUI(chIdx) * ScopeBase.getZoomHeight() / ScopeBase.getHeight());
    }

    // 250 到 -250 坐标系
    public static double getYTChannelPosition(int chIdx) {
        return ScopeBase.getNewHeight() * 1.0 / 2 - getYTChannelPositionUI(chIdx);
    }

    //
    public static double getChannelPositionUI(int chIdx) {
        return YT2Zoom(getYTChannelPositionUI(chIdx));
    }

    public static boolean isZoom() {
        return CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
    }

    public static double Zoom2YT(double y) {

        if (isZoom()) {
            y = ScopeBase.changeAccuracy(y * ScopeBase.getHeight() / ScopeBase.getZoomHeight());
        }
        return y;
    }

    public static double YT2Zoom(double y) {
        if (isZoom()) {
            y = ScopeBase.changeAccuracy(y * ScopeBase.getZoomHeight() / ScopeBase.getHeight());
        }
        return y;
    }

    //修改通道位置 , 转换到YT坐标 UI pos (0 - height)
    public static void putChannelPosition(int chIdx, double pos) {
        //保存的是 屏幕上像素位置
        if (getChannelPositionUI(chIdx) != pos) {
            putYTChannelPosition(chIdx, Zoom2YT(pos));
        }
    }

    //UI 坐标
    public static void putYTChannelPosition(int chIdx, double pos) {
        if (TChan.isChan(chIdx)) {
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_Y_POSITION + chIdx, String.valueOf(pos));
        } else if (TChan.isMath(chIdx)) {
            int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + chIdx);
            if (mathType == CacheUtil.MATHTYPE_DW) {
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION + chIdx, String.valueOf(pos));
            } else if (mathType == CacheUtil.MATHTYPE_AXB) {
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION + chIdx, String.valueOf(pos));
            } else if (mathType == CacheUtil.MATHTYPE_AM) {
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION + chIdx, String.valueOf(pos));
            } else {
                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + chIdx) == 1) {
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION + chIdx, String.valueOf(pos));
                } else {
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + chIdx, String.valueOf(pos));
                }
            }
        } else if (TChan.isRef(chIdx)) {
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_REF_Y_POSITION + chIdx, String.valueOf(pos));
        }
    }


    //YT模式触发电平
    public static double getYTLevelCache(String key) {
        return ScopeBase.changeAccuracy(CacheUtil.get().getDouble(key) * ScopeBase.getToUICoff());
    }

    /**
     * 获得电平的cache值
     */
    public static double getLevelCache(String key) {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {
            return getYTLevelCache(key) * 3 / 4;
        } else {
            return getYTLevelCache(key) ;
        }
    }

    /**
     * 转换成外部触发对应的像素位置 底部0---顶部1000
     */
    private static double transFormTriggerInput(double pos) {
        double temp = pos;
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {
            temp = Math.round(ScopeBase.getNewZoomHeight() - temp * GlobalVar.get().toZoomCoef());
        } else {
            temp = ScopeBase.getNewHeight() - temp;
        }
        return temp;
    }

    /**
     * 获取当前外部触发的幅度值
     */
    public static double getExtTriggerValCache(String key) {
        double temp = transFormTriggerInput(Tools.getYTLevelCache(key));
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {
            return 5.0f * temp / ScopeBase.getNewZoomHeight();
        } else {
            return 5.0f * temp / ScopeBase.getNewHeight();
        }
    }

    /**
     * 保存电平的cache值
     */
    public static void putLevelCache(String key, double value) {
        if (getLevelCache(key) != value) {
            if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {
                CacheUtil.get().putMap(key, String.valueOf(((value * 4 / 3) * ScopeBase.getToFPGACoff())));
            } else {
                CacheUtil.get().putMap(key, String.valueOf((value * ScopeBase.getToFPGACoff())));
            }
        }


    }


    /**
     * @param ch IWave.CH1 - IWave.CH4
     * @return 返回该通道当前的可滑动的上下限。从屏幕正中间开始向上或向下可移动的像素px值
     */
    public static int getChRange(int ch) {
        Channel channel = ChannelFactory.getDynamicChannel(ch - TChan.Ch1 + ChannelFactory.CH1);
        if(channel != null){
            return Scope.vSpanOfView(channel.getResistanceType(),channel.getVScaleVal()/channel.getProbeRate());
        }
        return 0;
    }

    public static boolean is24HourFormat() {
        String value = Settings.System.getString(App.get().getContentResolver(), Settings.System.TIME_12_24);
        if (value == null) {
            Locale locale = App.get().getResources().getConfiguration().locale;
            DateFormat natural = DateFormat.getTimeInstance(DateFormat.LONG, locale);
            if (natural instanceof SimpleDateFormat) {
                SimpleDateFormat sdf = (SimpleDateFormat) natural;
                String pattern = sdf.toPattern();

                if (pattern.indexOf('H') >= 0) {
                    value = "24";
                } else {
                    value = "12";
                }
            } else {
                value = "12";
            }
            return value.equals("24");
        }
        return value.equals("24");
    }

    public static String getCurTime() {
        Calendar calendar = Calendar.getInstance();
        int hourInt = Tools.is24HourFormat()
                ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR);
        int minInt = calendar.get(Calendar.MINUTE);
        String hourStr = String.valueOf(hourInt < 10 ? "0" + hourInt : hourInt);
        String minStr = String.valueOf(minInt < 10 ? "0" + minInt : minInt);
        return hourStr + ":" + minStr;
    }

    /**
     * 相对父的位置
     * @param layoutView
     */
    public static void PrintControlsLocation(ViewGroup layoutView){
        if (ExternalKeysProtocol.isPrintDebugLocation) {
            String title="";
            new Handler().postDelayed(() -> {
                Log.d(TAG, String.format("============== location %s===============",title));
                getViewRectAndSubView(layoutView);
                Log.d(TAG, "=================================================");
            }, 1000);
        }
    }

    /**
     * 全屏位置
     * @param title
     * @param layoutView
     */
    public static void PrintControlsLocation(String title,ViewGroup layoutView){
        if (ExternalKeysProtocol.isPrintDebugLocation) {
            new Handler().postDelayed(() -> {
                Log.d(TAG, String.format("============== location %s===============",title));
                getViewRectAndSubView(title, layoutView);
                Log.d(TAG, "=================================================");
            }, 1000);
        }
    }

    //父控件中的位置
    private static void getViewRectAndSubView(ViewGroup layoutView){
        int viewCount=layoutView.getChildCount();
        for(int i=0;i<viewCount;i++){
            View view=layoutView.getChildAt(i);

            if (view instanceof ViewGroup){
//                Log.d(TAG, String.format("----------enter subview %s------------ ",view.getClass().getSimpleName()));
                getViewRectAndSubView ((ViewGroup)view);
            }else if (view instanceof RecyclerView){
                int count= ((RecyclerView)(view)).getChildCount();
                for(int j=0;j<count;j++){
                    View v= ((RecyclerView)(view)).getChildAt(j);
//                    Rect r= getViewRect(view);
                    Rect r=new Rect(view.getLeft(),view.getTop(),view.getRight(),view.getBottom());
                    if (r.width()==0 || r.height()==0)continue;
                    String rect=String.format("(%d,%d - %d,%d)",r.left,r.top,r.width(),r.height());
                    String name=view.getClass().getSimpleName();
                    Log.d(TAG, String.format("getViewRectAndSubView: name:%s ,rect:%s",name,rect));
                }
            }
            else{
//                Rect r= getViewRect(view);
                Rect r=new Rect(view.getLeft(),view.getTop(),view.getRight(),view.getBottom());


                if (r.width()==0 || r.height()==0) continue;
                String rect=String.format("%d,%d - %d,%d",(int)r.left,(int)r.top,(int)r.width(),(int)r.height());
                String name=view.getClass().getSimpleName();
                if (view instanceof TextView){
                    name=((TextView) view).getText().toString();
                }
                Log.d(TAG, String.format("getViewRectAndSubView: name:%s ,rect:%s",name,rect));
            }

        }
    }
    private static void getViewRectAndSubView(String title,ViewGroup layoutView){

        int viewCount=layoutView.getChildCount();
        for(int i=0;i<viewCount;i++){
            View view=layoutView.getChildAt(i);

            if (view instanceof ViewGroup){
//                Log.d(TAG, String.format("----------enter subview %s------------ ",view.getClass().getSimpleName()));
                getViewRectAndSubView (title,(ViewGroup)view);
            }else if (view instanceof RecyclerView){
                int count= ((RecyclerView)(view)).getChildCount();
                for(int j=0;j<count;j++){
                    View v= ((RecyclerView)(view)).getChildAt(j);
                    Rect r= getViewRect(view);
                    if (r.width()==0 || r.height()==0)continue;
                    String rect=String.format("(%d,%d - %d,%d)",r.left,r.top,r.width(),r.height());
                    String name=view.getClass().getSimpleName();
                    Log.d(TAG, String.format("getViewRectAndSubView: name:%s ,rect:%s",name,rect));
                }
            }
            else{
                Rect r= getViewRect(view);
                if (r.width()==0 || r.height()==0) continue;
                String rect=String.format("%d,%d - %d,%d",r.left,r.top,r.width(),r.height());
                String name=view.getClass().getSimpleName();
                if (view instanceof TextView){
                    name=((TextView) view).getText().toString();
                }
                Log.d(TAG, String.format("getViewRectAndSubView: name:%s ,rect:%s",name,rect));
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void getViewRectByButton(ViewGroup layoutView, BiConsumer<Rect,String> OnConsumer){
        int viewCount=layoutView.getChildCount();
        for(int i=0;i<viewCount;i++){
            View view=layoutView.getChildAt(i);
            if (view instanceof  ViewGroup){
                getViewRectByButton((ViewGroup)view,OnConsumer);

            }
            else {

                Rect r= getViewRect(view);
                if (r.width()==0 || r.height()==0 || view.getVisibility()==View.GONE || view.isEnabled()==false ) continue;
                String rect=String.format("%d,%d - %d,%d",r.left,r.top,r.width(),r.height());
                String name=view.getClass().getSimpleName();
                if (view instanceof Button){
                    name=((Button)view).getText().toString();
                    OnConsumer.accept(r,name);
                }

            }
        }
    }

    public static Rect getViewRect(View v) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        Rect rect = new Rect(x, y, x + v.getWidth(), y + v.getHeight());
//        Log.d("Debug", String.format("getViewRect: %s", rect.toString() ));
        return rect;
    }



    public static boolean isEnableAutoRange() {
        boolean isSerials = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1) || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        return (ScopeConfig.getConfig().isEnableAutoRange() || App.IsDebug()) && !isSerials;
    }


    public static < T > int indexOf(List< T > list, Predicate<? super T> predicate) {
        int idx = 0;
        for (Iterator< T > iter = list.iterator(); iter.hasNext(); idx++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (predicate.test(iter.next())) {
                    return idx;
                }
            }
        }

        return -1;
    }

    public static <T> int indexOf(T[] list,Predicate<? super T> predicate){
        for(int i=0;i<list.length;i++){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (predicate.test((list[i]))){
                    return i;
                }
            }
        }
        return -1;
    }

    public static <T> void insertElement(List<T> list, int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= list.size()
                || toIndex < 0 || toIndex >= list.size()
                || fromIndex == toIndex) {
            return;
        }
        T element = list.remove(fromIndex);
        int newIndex = (fromIndex < toIndex) ? toIndex - 1 : toIndex;
        list.add(newIndex, element);
    }

    public static <T> void swapElement(List<T> list, int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= list.size()
                || toIndex < 0 || toIndex >= list.size()
                || fromIndex == toIndex) {
            return;
        }
        T element = list.get(fromIndex);
        list.set(fromIndex, list.get(toIndex));
        list.set(toIndex, element);
    }


    private static final Pattern NUMBER_PATTERN=Pattern.compile("-?\\d+(\\.\\d+)?");
    public static boolean isNumeric(String s){
        return s!=null & NUMBER_PATTERN.matcher(s).matches();
    }

    public static Bitmap readNineBmp(int resId){
        BitmapFactory.Options options=new BitmapFactory.Options();
        TypedValue value=new TypedValue();
        App.get().getResources().openRawResource(resId,value);
        options.inTargetDensity=value.density;
        options.inScaled=false;
        Bitmap b= BitmapFactory.decodeResource(App.get().getResources(), resId,options );

       return b;
    }
    public static Bitmap readSvgBmp(int resId) {
        Context context = App.get().getApplicationContext();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return BitmapFactory.decodeResource(context.getResources(), resId);
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    public static   Bitmap getNineBmp(Bitmap bmp,int width,int height,float rotate,int alpha){

        if (NinePatch.isNinePatchChunk(bmp.getNinePatchChunk())){
            NinePatchDrawable ninePatchDrawable=new NinePatchDrawable(App.get().getResources(),bmp,bmp.getNinePatchChunk(),new Rect(),null);
            if (width==0){
                width=1;
            }
            if (height==0){
                height=1;
            }
            Bitmap b=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            Canvas canvas=new Canvas(b);
            canvas.save();
            ninePatchDrawable.setBounds(0, 0, canvas.getWidth(),canvas.getHeight());
            ninePatchDrawable.setAlpha(alpha);
            ninePatchDrawable.draw(canvas);
            canvas.rotate(rotate,b.getWidth()/2,b.getHeight()/2);
            canvas.restore();

            return b;
        }
        return null;
    }

    //region 更新测量数据
    public static String updateMeasureData(int chIdx,int measureType, float val){
        Measure measure = getHardwareMeasure(chIdx);
        String value= addUnit(chIdx,measureType,val);
        String result= clippingProcess(value,measureType+Measure.MeasureType.MEASURE_PERIOD,measure.getClipping());
        return result;
    }
    private static Measure getHardwareMeasure(int chId) {
        BaseChannel baseChannel = null;
        if (ChannelFactory.isDynamicCh(chId)) {
            baseChannel = ChannelFactory.getDynamicChannel(chId);
        } else if (ChannelFactory.isMathCh(chId)) {
            baseChannel = ChannelFactory.getMathChannel(chId);
        } else if (ChannelFactory.isRefCh(chId)) {
            baseChannel = ChannelFactory.getRefChannel(chId);
        }
        if (baseChannel != null) {
            return baseChannel.getMeasure();
        }
        return null;
    }
    private static final String MEASURE_DATA_INIT = "----";
    private static String clippingProcess(String sData, int itemId, int clipping) {
        switch (clipping) {
            case 1://正削
                switch (itemId) {
                    case Measure.MeasureType.MEASURE_NEGATIVE_OVERSHOOT:
                    case Measure.MeasureType.MEASURE_LOW:
                    case Measure.MeasureType.MEASURE_MIN:
                        break;
                    default:
                        if (!sData.equals(MEASURE_DATA_INIT)) sData += "?";
                        break;
                }
                break;
            case 2://负削
                switch (itemId) {
                    case Measure.MeasureType.MEASURE_POSITIVE_OVERSHOOT:
                    case Measure.MeasureType.MEASURE_HIGH:
                    case Measure.MeasureType.MEASURE_MAX:
                        break;
                    default:
                        if (!sData.equals(MEASURE_DATA_INIT)) sData += "?";
                        break;
                }
                break;
            case 3://双削
                if (!sData.equals(MEASURE_DATA_INIT)) sData += "?";
                break;
            case 4://无波
                if (!sData.equals(MEASURE_DATA_INIT)) sData += "?";
                break;
            case 0://正常
            default:
                break;
        }
        return sData;
    }
    public static String addUnit(int ch, int measureType, float val) {
        switch (measureType) {
            case MeasureManage.IMeasure.MeasureId_Freq:
                return TBookUtil.getFourFromD_(val) + "Hz";
            case MeasureManage.IMeasure.MeasureId_DutyAdd:
            case MeasureManage.IMeasure.MeasureId_DutySub:
            case MeasureManage.IMeasure.MeasureId_ROV:
            case MeasureManage.IMeasure.MeasureId_FOV:
                //%不显示m、k、M等前缀，保留2位小数
                return TBookUtil.getPoint2FromD_noscale(val * 100) + "%";
            case MeasureManage.IMeasure.MeasureId_Phase:
                String d = TBookUtil.getFourFromD_(val);
                if ("-0f".equals(d) || "0f".equals(d)) {
                    d = "0";
                }
                return d + "°";
            case MeasureManage.IMeasure.MeasureId_Period:
            case MeasureManage.IMeasure.MeasureId_RiseTime:
            case MeasureManage.IMeasure.MeasureId_FallTime:
            case MeasureManage.IMeasure.MeasureId_Delay:
            case MeasureManage.IMeasure.MeasureId_WidthAdd:
            case MeasureManage.IMeasure.MeasureId_WidthSub:
            case MeasureManage.IMeasure.MeasureId_BurstW:
            case MeasureManage.IMeasure.MeasureId_TVALUE:
                return TBookUtil.getFourFromD_(val) + "s";
            case MeasureManage.IMeasure.MeasureId_PKPK:
            case MeasureManage.IMeasure.MeasureId_Amp:
            case MeasureManage.IMeasure.MeasureId_High:
            case MeasureManage.IMeasure.MeasureId_Low:
            case MeasureManage.IMeasure.MeasureId_Max:
            case MeasureManage.IMeasure.MeasureId_Min:
            case MeasureManage.IMeasure.MeasureId_RMS:
            case MeasureManage.IMeasure.MeasureId_CRMS:
            case MeasureManage.IMeasure.MeasureId_Mean:
            case MeasureManage.IMeasure.MeasureId_CMean:
            case MeasureManage.IMeasure.MeasureId_ACRMS:
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch);
            case MeasureManage.IMeasure.MeasureId_PostitiveRate:
            case MeasureManage.IMeasure.MeasureId_NegativeRate:
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch) + "/s";
        }
        return "";
    }
    //endregion


    public static void drawText(Canvas canvas,String text,float left,float y,int width,Paint paint){
        float textWidth=paint.measureText(text);
        String info=text;
        if (width<textWidth){
            info= TextUtils.ellipsize(text,new TextPaint(paint),width,TextUtils.TruncateAt.END).toString();
            if (left<0){
                left=0;
            }
        }
        canvas.drawText(info,left,y,paint);
    }
    public static void drawText(Canvas canvas,String text,float left,float y,int width,Paint paint,TextPaint textPaint){
        float textWidth=paint.measureText(text);
        String info=text;
        if (width<textWidth){
            info= TextUtils.ellipsize(text,new TextPaint(paint),width,TextUtils.TruncateAt.END).toString();
            if (left<0){
                left=0;
            }
        }
        canvas.drawText(info,left,y,textPaint);
        canvas.drawText(info,left,y,paint);
    }

    //region probes
    public static int getChanProbeTypeUnit(int chIdx){
        return getChanProbeTypeUnit(ChannelFactory.getDynamicChannel(chIdx));
    }
    public static   int getChanProbeTypeUnit(Channel channel){
        if (channel!=null) {
            BaseProbe baseProbe = channel.getProbe();
            if (isProbeInterface(channel) && baseProbe != null) {
                return channel.getProbe().getProbeType();
            } else {
                return channel.getProbeType();
            }
        }
        return 0;
    }


    public static boolean isProbeInterface(Channel channel){
        if (channel.getProbe()!=null) {
            //Log.d("Tag.Debug", String.format("isProbeInterface: %b,%s", channel.isAutoProbe(), channel.getProbe().getSN()));
            return channel != null && channel.isAutoProbe() && !StrUtil.isEmpty(channel.getProbe().getSN());
        }
        return false;
    }

    public static String generateName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String str_time = sdf.format(date);
        return str_time;
    }





    //endregion
}