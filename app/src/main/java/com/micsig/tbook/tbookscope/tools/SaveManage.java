package com.micsig.tbook.tbookscope.tools;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.IntRange;

import com.micsig.base.Logger;
import com.micsig.base.Utils;
import com.micsig.tbook.scope.Data.SaveBin;
import com.micsig.tbook.scope.Data.SaveCsv;
import com.micsig.tbook.scope.Data.SaveRecoverySession;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.ScopeFrozen;
import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.mem.Memory;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.maincenter.serialsword.ISerialsWord;
import com.micsig.tbook.tbookscope.rightslipmenu.RightLayoutRef;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by liwb on 2018/5/28.
 * <pre class="prettyprint">
 * SaveManage:是一个存储文件管理类，是一个单例类
 * 保存失败的几个原因：
 * 1.文件已存在
 * 2.mainActivity未创建
 * <p>
 * <p>
 * </pre>
 */

public class SaveManage {
    private static final String TAG = "SaveManage";

    public static final String SAVE_WAVE_DEFAULT = "refwave";
    public static final String SAVE_CSV_DEFAULT = "csvwave";
    public static final String SAVE_BIN_DEFAULT = "binwave";
    public static final String SAVE_SETTING_DEFAULT = "default";
    public static final String SAVE_SESSION_DEFAULT = "session";
    public static final String SAVE_PICTURE_DEFAULT = "picture";

    public static final String SAVE_AUTOSAVE_DEFAULT= "autosave";

    //region  单例
    private static class SaveManageHolder {
        public static final SaveManage instance = new SaveManage();
    }

    public static SaveManage getInstance() {
        return SaveManage.SaveManageHolder.instance;
    }
    //endregion

    private static final int Handler_UDiskUpdate = 0x01;
    private static final int Handler_SaveCompleteSuccess = 0x02;
    private static final int Handler_SaveCompleteFailed = 0x03;


    private static final int ThreadPool_WHAT_SaveRecord = 0x01;
    private static final int ThreadPool_WHAT_LoadRecord = 0x02;
    private static final int ThreadPool_WHAT_saveWave = 0x03;
    private static final int ThreadPool_WHAT_saveCSV = 0x04;
    private static final int ThreadPool_WHAT_saveBin = 0x05;
    private static final int ThreadPool_WHAT_saveRef = 0x06;
    private static final int ThreadPool_WHAT_readRef = 0x07;
    private static final int ThreadPool_WHAT_saveSerialCSV = 0x08;

    //region 变量
    private MainActivity mainActivity;
    private static ExecutorService mSingleThreadExecutor = null;

    private static ExecutorService mMultiThreadExecutor = null;
    private static SaveSetRunnable mSaveSetRunable = null;

    private UIHandler uiHandler;

    //endregion

    public void init() {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        ThreadPoolExecutor multiThreadPool = new ThreadPoolExecutor(3, Integer.MAX_VALUE,
                1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        multiThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        mSingleThreadExecutor = threadPool;
        mMultiThreadExecutor = multiThreadPool;
        mSaveSetRunable = new SaveSetRunnable();
        uiHandler = new UIHandler();
    }

    public void init(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

    }

    //region  存储功能接口

    /**
     * 保存到默认名称
     *
     * @return
     */
    public SaveManage saveToDefaultSaveName() {
        try {
            saveUserSet(CacheUtil.DefaultSaveName, CacheUtil.get().getCurrMap(), null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SaveManage saveToOtherSaveName() {
        try {
            saveUserSet(CacheUtil.OtherDefaultSaveName, CacheUtil.get().getCurrOtherMap(), null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }


    public boolean saveIsComplete() {
        return mSaveSetRunable.isComplete;
    }

    /***
     * 存储用户设置
     * @param fileName
     */
    public SaveManage saveUserSet(String fileName, HashMap<String, String> map, SaveCallBack saveCallBack) throws InterruptedException {
        if (mSaveSetRunable.isComplete == false) return this;
        String defaultSuffix = ".SaveRecovery";
        if(fileName.equals(CacheUtil.OtherDefaultSaveName)) {
            defaultSuffix = ".SR";
        }
        String path = Tools.resultSavePath(Tools.SaveType_LOCAL, Tools.SaveDir_DEFAULT, mainActivity) + fileName + defaultSuffix;
        if(Utils.isDiskAvaiable(new File(path),1)) {
            mSaveSetRunable.isComplete = false;
            mSaveSetRunable.map = map;
            mSaveSetRunable.what = ThreadPool_WHAT_SaveRecord;
            mSaveSetRunable.PathFile = path;
            mSaveSetRunable.saveCallBack = saveCallBack;
            mSingleThreadExecutor.execute(mSaveSetRunable);
            while (true) {
                if (mSaveSetRunable.isComplete) break;
            }
            Logger.i(TAG, "saveUserSet:" + path);
        }
        return this;
    }

    public SaveManage saveUserSetToPath(String filePath, HashMap<String, String> map, SaveCallBack saveCallBack) throws InterruptedException {
        if (!mSaveSetRunable.isComplete) return this;
        mSaveSetRunable.isComplete = false;
        mSaveSetRunable.map = map;
        mSaveSetRunable.what = ThreadPool_WHAT_SaveRecord;
        mSaveSetRunable.PathFile = filePath;
        mSaveSetRunable.saveCallBack = saveCallBack;
        mSingleThreadExecutor.execute(mSaveSetRunable);
        while (true) {
            if (mSaveSetRunable.isComplete) break;
        }
        Logger.i(TAG, "saveUserSetTopath:" + filePath);
        return this;
    }


    public boolean loadUserSet(String fileName, HashMap<String, String> map) throws InterruptedException {
        if (!mSaveSetRunable.isComplete) return false;
        String defaultSuffix = ".SaveRecovery";
        if(fileName.equals(CacheUtil.OtherDefaultSaveName)) {
            defaultSuffix = ".SR";
        }
        String path = Tools.resultSavePath(Tools.SaveType_LOCAL, Tools.SaveDir_DEFAULT, mainActivity) + fileName + defaultSuffix;
        File file = new File(path);
        if (!file.exists()) return false;
        mSaveSetRunable.map = map;
        mSaveSetRunable.what = ThreadPool_WHAT_LoadRecord;
        mSaveSetRunable.PathFile = path;
        mSaveSetRunable.saveCallBack = null;
        mSaveSetRunable.isComplete = false;
        mSingleThreadExecutor.execute(mSaveSetRunable);
        while (true) {
            if (mSaveSetRunable.isComplete) break;
        }
        Logger.i(TAG, "loadUserSet() ==>" + " LoadUserSetName:" + path);
        return true;
    }

    public boolean loadUserSetFromFilePath(String filePath, HashMap<String, String> map) throws InterruptedException {
        if (!mSaveSetRunable.isComplete) return false;
        File file = new File(filePath);
        if (!file.exists()) return false;
        mSaveSetRunable.map = map;
        mSaveSetRunable.what = ThreadPool_WHAT_LoadRecord;
        mSaveSetRunable.PathFile = filePath;
        mSaveSetRunable.saveCallBack = null;
        mSaveSetRunable.isComplete = false;
        mSingleThreadExecutor.execute(mSaveSetRunable);
        while (true) {
            if (mSaveSetRunable.isComplete) break;
        }
        Logger.i(TAG, "loadUserSetFromFilePath() ==>" + " LoadUserSetName:" + filePath);
        return true;
    }

    public SaveManage allSaveEntrance(@IntRange(from = 0, to = 10) int channel, int fileType, String savePath, String fileName, List<Integer> selectList, SaveCallBack saveCallBack) {
//        if (!mSaveSetRunable.isComplete) return this;
        String suffix;
        SaveSetRunnable mSaveRunnable = new SaveSetRunnable();
        switch (fileType) {
            default:
            case 0:
                mSaveRunnable.what = ThreadPool_WHAT_saveWave;
                suffix = ".mwav";
                break;
            case 1:
                mSaveRunnable.what = ThreadPool_WHAT_saveCSV;
                suffix = ".csv";
                break;
            case 2:
                mSaveRunnable.what = ThreadPool_WHAT_saveBin;
                suffix = ".bin";
                break;
        }
        mSaveRunnable.ChannelID = channel;
        mSaveRunnable.saveCallBack = saveCallBack;
        mSaveRunnable.PathFile = savePath + File.separator + fileName + suffix;
        Logger.i("SaveManage allSaveEntrance PathFile= " + mSaveRunnable.PathFile);
        mSaveRunnable.selectList = selectList;
        if (fileExistResultField(mSaveRunnable.PathFile, mSaveRunnable.saveCallBack)) return this;
        mSaveRunnable.isComplete = false;
        mSingleThreadExecutor.execute(mSaveRunnable);
        Logger.i("SaveManage allSaveEntrance PathFile= " + mSaveRunnable.PathFile);
        return this;
    }


    /***
     * 保存为WAV文件
     * @param Channel  保存的通道
     * @param saveType  保存的类型，本地 U盘
     * @param fileName  保存的文件名（不带扩展名）
     * @return
     */
    public SaveManage saveWave(@IntRange(from = 0, to = 10) int Channel, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) {
        int chMax = GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8 ? ChannelFactory.CH8 : ChannelFactory.CH4;
        if (Channel >= ChannelFactory.CH1 && Channel <= chMax && !ChannelFactory.isChOpen(Channel)) {
//            String resultMsg = String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinFailed), ChannelFactory.getChannelName(Channel));
            String resultMsg = "";
            saveCallBack.onResult(false, resultMsg);
            return this;
        }
        mSaveSetRunable.what = ThreadPool_WHAT_saveWave;
        mSaveSetRunable.ChannelID = Channel;
        mSaveSetRunable.saveCallBack = saveCallBack;
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_REFWAVE, mainActivity) + fileName + ".mwav";
        if (fileExistResultField(mSaveSetRunable.PathFile, saveCallBack)) return this;
        mSaveSetRunable.isComplete = false;
        mSingleThreadExecutor.execute(mSaveSetRunable);
        while (true) {
            if (mSaveSetRunable.isComplete) break;
        }
        return this;
    }

    /***
     * 保存为CSV文件
     * @param Channel   保存的通道
     * @param saveType  保存的类型   本地   U盘
     * @param fileName  保存的文件名（不带扩展名）
     * @return
     */
    public SaveManage saveCSV(@IntRange(from = 0, to = 10) int Channel, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) throws InterruptedException {
        if (mSaveSetRunable.isComplete == false) return this;
        mSaveSetRunable.what = ThreadPool_WHAT_saveCSV;
        mSaveSetRunable.ChannelID = Channel;
        mSaveSetRunable.saveCallBack = saveCallBack;
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_CSVWAVE, mainActivity) + fileName + ".csv";
        if (fileExistResultField(mSaveSetRunable.PathFile, mSaveSetRunable.saveCallBack))
            return this;
        mSaveSetRunable.isComplete = false;
        mSingleThreadExecutor.execute(mSaveSetRunable);
//        while (true) {
//            if (mSaveSetRunable.isComplete) break;
//        }
        return this;
    }

    /***
     * 保存为CSV文件
     * @param selectList   选中要保存的通道集合
     * @param saveType  保存的类型   本地   U盘
     * @param fileName  保存的文件名（不带扩展名）
     * @return
     */
    public SaveManage saveCSV(List<Integer> selectList, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) throws InterruptedException {
        if (mSaveSetRunable.isComplete == false) return this;
        mSaveSetRunable.what = ThreadPool_WHAT_saveCSV;
//        mSaveSetRunable.ChannelID = Channel;
        mSaveSetRunable.selectList = selectList;
        mSaveSetRunable.saveCallBack = saveCallBack;
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_CSVWAVE, mainActivity) + fileName + ".csv";
        if (fileExistResultField(mSaveSetRunable.PathFile, mSaveSetRunable.saveCallBack))
            return this;
        mSaveSetRunable.isComplete = false;
        mSingleThreadExecutor.execute(mSaveSetRunable);
        return this;
    }



    /***
     * 保存为Bin文件
     *
     * @param Channel    保存的通道
     * @param saveType   保存的类型  本地  U盘
     * @param fileName   保存的文件名（不带扩展名）
     * @return
     */
    public SaveManage saveBin(@IntRange(from = 0, to = 10) int Channel, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) throws InterruptedException {
        if (mSaveSetRunable.isComplete == false) return this;
        mSaveSetRunable.what = ThreadPool_WHAT_saveBin;
        mSaveSetRunable.ChannelID = Channel;
        mSaveSetRunable.saveCallBack = saveCallBack;
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_BINWAVE, mainActivity) + fileName + ".bin";
        if (fileExistResultField(mSaveSetRunable.PathFile, mSaveSetRunable.saveCallBack))
            return this;
        mSaveSetRunable.isComplete = false;
        mSingleThreadExecutor.execute(mSaveSetRunable);
//        while (true) {
//            if (mSaveSetRunable.isComplete) break;
//        }
        return this;
    }

    public SaveManage saveSerialCSV(int Channel, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) {
        if (mSaveSetRunable.isComplete == false) return this;
        mSaveSetRunable.isComplete = false;
        mSaveSetRunable.what = ThreadPool_WHAT_saveSerialCSV;
        mSaveSetRunable.ChannelID = Channel;
        mSaveSetRunable.saveCallBack = saveCallBack;
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_CSVSBT, mainActivity) + fileName + ".csv";
        mSingleThreadExecutor.execute(mSaveSetRunable);

        return this;
    }

    public SaveManage saveSerialCSV(int Channel, int allSerialsBus, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) {
        if (mSaveSetRunable.isComplete == false) return this;
        mSaveSetRunable.isComplete = false;
        mSaveSetRunable.what = ThreadPool_WHAT_saveSerialCSV;
        mSaveSetRunable.ChannelID = Channel;
        mSaveSetRunable.allSerialsBus = allSerialsBus;
        mSaveSetRunable.saveCallBack = saveCallBack;
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_CSVSBT, mainActivity) + fileName + ".csv";
        mSingleThreadExecutor.execute(mSaveSetRunable);

        return this;
    }

    /***
     * 保存数据到参考通道
     * @param Channel     保存的通道 Ch1--Ch4
     * @param saveType   保存的类型  本地  U盘
     * @param ref   保存的文件名（参考的文件名为固定名称 Ref1.wav  Ref2.wav等）
     * @return 保存的文件位置
     */
    public String saveRef(int Channel, @Tools.SaveType int saveType, int ref, SaveCallBack saveCallBack) {
        if (!mSaveSetRunable.isComplete) return "";
        mSaveSetRunable.isComplete = false;
        String fileName = "Ref1";
        switch (ref) {
            case ChannelFactory.REF1: {
                fileName = "Ref1";
            }
            break;
            case ChannelFactory.REF2: {
                fileName = "Ref2";
            }
            break;
            case ChannelFactory.REF3: {
                fileName = "Ref3";
            }
            break;
            case ChannelFactory.REF4: {
                fileName = "Ref4";
            }
            break;
            case ChannelFactory.REF5: {
                fileName = "Ref5";
            }
            break;
            case ChannelFactory.REF6: {
                fileName = "Ref6";
            }
            break;
            case ChannelFactory.REF7: {
                fileName = "Ref7";
            }
            break;
            case ChannelFactory.REF8: {
                fileName = "Ref8";
            }
            break;
        }
        mSaveSetRunable.what = ThreadPool_WHAT_saveRef;
        mSaveSetRunable.ChannelID = Channel;
        mSaveSetRunable.saveCallBack = saveCallBack;
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_REFDEFAULT, mainActivity) + fileName + ".mwav";
        File file = new File(mSaveSetRunable.PathFile);
        if (file.exists()) {
            file.delete();
        }
        mSingleThreadExecutor.execute(mSaveSetRunable);
        while (true) {
            if (mSaveSetRunable.isComplete) break;
        }
        if (file.exists()) {
            readRef(ref, mSaveSetRunable.PathFile);
        } else {
            mSaveSetRunable.PathFile = "";
        }

        return mSaveSetRunable.PathFile;
    }

    public boolean readRef(int Channel, String pathFile) {
        Log.d(TAG, "readRef() called with: Channel = [" + Channel + "], pathFile = [" + pathFile + "]");
        RefChannel refChannel = ChannelFactory.getRefChannel(Channel /*ChannelFactory.REF1*/);
        if (refChannel != null) {
            if (SaveRecoverySession.MSS_REF_TAG.equals(pathFile)
                    || refChannel.loadWave(pathFile)) {
                Logger.d(TAG, "Ref" + (Channel - ChannelFactory.REF1 + 1) + ": load " + pathFile + " success!");

                if(!SaveRecoverySession.MSS_REF_TAG.equals(pathFile)){
                    //SaveRecoverySession.MSS_REF_TAG.equals(pathFile)

                    CacheUtil.get().putMap(CacheUtil.MAIN_CHAN_REF_VSCALE_ID + TChan.toUiChNo(Channel),
                            String.valueOf(refChannel.getVScaleId()));
                    //修改REF的触发时刻;
                    TriggerTimebase.getInstance().putCacheForTimeBasePosition(ScopeBase.getWidth() / 2 - refChannel.getXPos_pix_original(), Channel);
                    //修改ref的y偏移
                    int index = TChan.toUiChNo(Channel);
                    double offset = ScopeBase.getNewHeight() / 2 - refChannel.getPosUI();
                    Tools.putYTChannelPosition(index, offset);

                    //CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_REF_Y_POSITION + index, String.valueOf(offset));
                    WaveManage.get().setPositionY(index, Tools.getChannelPositionUI(index));
                    double scaleVal = ChannelFactory.getRefChannel(Channel).getRefTimeScaleVal();
                    CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + index
                            , RightLayoutRef.getStringRefScale(TChan.toFpgaChNo(index), scaleVal));

//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + index, "0 ns");//重新加载Ref时，将Delay归零
                    refChannel.setDelay(0);
//                    String delay = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_DELAY + TChan.toUiChNo(Channel));
//                    if (refChannel.getRefType() != WaveData.FFT_WAVE) {
//                        refChannel.setDelay(TBookUtil.getBigDoubleFromM(delay.replace("s", "")));
//                    }
                    return true;
                }
            }
        }
        return false;
    }

    //endregion

    class SaveSetRunnable implements Runnable {
        private static final String TAG = "SaveSetRunnalbe";
        public volatile int what;
        public volatile int ChannelID;
        public volatile List<Integer> selectList;
        public String PathFile;
        public volatile boolean isComplete = true;
        public HashMap<String, String> map;
        public SaveCallBack saveCallBack;
        public volatile int allSerialsBus = -1;

        @Override
        public void run() {
//            Thread.currentThread().setName("SaveManage");
            Logger.i(TAG, "thread pool begin! name= " + Thread.currentThread().getName());
            switch (what) {
                case ThreadPool_WHAT_SaveRecord: {
                    HashMap<String, String> hashMap = (HashMap<String, String>) (map.clone());
                    // 写
                    FileOutputStream fos = null;
                    BufferedOutputStream bos = null;
                    ObjectOutputStream oos = null;
                    try {
                        fos = new FileOutputStream(PathFile);
                        bos = new BufferedOutputStream(fos);
                        oos = new ObjectOutputStream(bos);
                        oos.writeObject(hashMap);
                        oos.flush();
                        oos.close();
                        bos.flush();
                        bos.close();
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                case ThreadPool_WHAT_LoadRecord: {
                    // 读
                    FileInputStream fis = null;
                    BufferedInputStream bis = null;
                    ObjectInputStream ois = null;
                    try {
                        fis = new FileInputStream(PathFile);
                        bis = new BufferedInputStream(fis);
                        ois = new ObjectInputStream(bis);
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap = (HashMap<String, String>) ois.readObject();
                        ois.close();
                        bis.close();
                        fis.close();
                        //CacheUtil.get().putMapAll(hashMap);
                        if (!hashMap.isEmpty()) {
                            map.clear();
                            map.putAll(hashMap);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (OptionalDataException e) {
                        e.printStackTrace();
                    } catch (StreamCorruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    isComplete = true;
                    Tools.sleep(10);
                    CacheUtil.get().clearTempSaveParam(map);
                }
                return;
/*                case ThreadPool_WHAT_saveWave: {
                    boolean bSave = true;
                    if (!Scope.getInstance().isRun()
                            && !ScopeFrozen.getInstance().isValid()) {
                        bSave = false;
                    }
                    if(ChannelFactory.isMathCh(ChannelID)){
                        MathChannel mathChannel = ChannelFactory.getMathChannel(ChannelID);
                        if(mathChannel != null && bSave){
                            mathChannel.save(PathFile);
                        }
                    }else if(ChannelFactory.isDynamicCh(ChannelID)){
                        Channel channel = ChannelFactory.getDynamicChannel(ChannelID);
                        if(channel != null && bSave){
                            channel.save(PathFile);
                        }
                    }
                }
                break;*/
                case ThreadPool_WHAT_saveCSV: {
                    boolean bSave = true;
                    if (!Scope.getInstance().isRun()
                            && !ScopeFrozen.getInstance().isValid()) {
                        bSave = false;
                    }
                    SaveCsv saveCsv = SaveCsv.getInstance();
                    saveCsv.clear();
                    for (int i = 0; i < selectList.size(); i++) {
                        IChannel channel = ChannelFactory.getValidChannel(selectList.get(i));
                        if(channel == null) continue;
                        saveCsv.add(channel);
                    }
                    long needUse = saveCsv.calcStorageSize();
                    File file = new File(PathFile);
                    if (!Utils.isDiskAvaiable(file, needUse)) {
                        bSave = false;
                    }
                    if (bSave) {
                        saveCsv.save(PathFile);
                        saveCsv.setSaveCsvProgress(val -> {//进度条
                            EventBase eventBase = new EventBase(EventFactory.EVENT_SAVECSV_RUN);
                            Logger.i("SaveCsv progress=" + val);
                            eventBase.setData(val);
                            EventFactory.sendEvent(eventBase, false);
                        });
                        while (!saveCsv.isFinish()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
                case ThreadPool_WHAT_saveBin: {
                    Channel channel = ChannelFactory.getDynamicChannel(ChannelID);
                    if (channel != null) {
                        boolean bAllSegments = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_SAVE_ALL_SEGMENT_VISIBLE)
                                && CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_SAVE_ALL_SEGMENT_CHECK);
                        SaveBin.getInstance().save(channel, PathFile,bAllSegments);
                    }
                }
                break;
                case ThreadPool_WHAT_saveWave:
                case ThreadPool_WHAT_saveRef: {
                    boolean bSave = true;
                    if (!Scope.getInstance().isRun()
                            && !ScopeFrozen.getInstance().isValid()) {
                        bSave = false;
                    }
                    //com.micsig.tbook.scope.channel.Channel channel = ChannelFactory.getDynamicChannel(ChannelID);
                    BaseChannel channel = null;
                    if (ChannelFactory.isDynamicCh(ChannelID))
                        channel = ChannelFactory.getDynamicChannel(ChannelID);
                    else if (ChannelFactory.isMathCh(ChannelID)) {
                        channel = ChannelFactory.getMathChannel(ChannelID);
                    } else if (ChannelFactory.isRefCh(ChannelID)) {
                        channel = ChannelFactory.getRefChannel(ChannelID);
                    }

                    Logger.i(TAG, "channel= " + channel.getName() + " ,bsave= " + bSave + " ,isOPen= " + channel.isOpen());
                    if (channel != null && channel.isOpen() && bSave) {
                        channel.save(PathFile);
                    }
                }
                break;
                case ThreadPool_WHAT_readRef: {
                }
                break;
                case ThreadPool_WHAT_saveSerialCSV: { //串型文本解码保存
                    int serialbus = 0;
                    if (ChannelID == ISerialsWord.TYPE_S1) {
                        serialbus = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1);
                    } else if (ChannelID == ISerialsWord.TYPE_S2) {
                        serialbus = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2);
                    } else if (ChannelID == ISerialsWord.TYPE_S3) {
                        serialbus = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3);
                    } else if (ChannelID == ISerialsWord.TYPE_S4) {
                        serialbus = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4);
                    } else if (ChannelID == ISerialsWord.TYPE_S12) {
                        serialbus = allSerialsBus;
                    }

                    File file = new File(PathFile);
                    if (file.exists()) file.delete();
                    StringBuilder sb = new StringBuilder();

                    //region 各种串形解码
                    switch (serialbus) {
                        case RightLayoutSerials.SERIALS_UART: {

                            LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListTotal = SerialBusManage.getInstance()
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);
                            sb.append("SerialsBusTextUART " + Tools.genNameByDateTime() + "\n");
                            sb.append(SerialBusTxtStruct.toCSVTitleUart() + "\n");
                            for (Iterator iter = uartListTotal.iterator(); iter.hasNext(); ) {
                                SerialBusTxtStruct.UartStruct uart = (SerialBusTxtStruct.UartStruct) iter.next();
                                sb.append(uart.toCSV() + "\n");
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_LIN: {  //ConcurrentLinkedQueue  BlockingQueue
                            LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linListTotal = SerialBusManage.getInstance()
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);
                            sb.append("SerialsBusTextLIN " + Tools.genNameByDateTime() + "\n");
                            sb.append(SerialBusTxtStruct.toCSVTitleLin() + "\n");
                            for (Iterator iter = linListTotal.iterator(); iter.hasNext(); ) {
                                SerialBusTxtStruct.LinStruct lin = (SerialBusTxtStruct.LinStruct) iter.next();
                                sb.append(lin.toCSV() + "\n");
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_CAN: {
                            LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal = SerialBusManage.getInstance()
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);
                            sb.append("SerialsBusTextCAN " + Tools.genNameByDateTime() + "\n");
                            sb.append(SerialBusTxtStruct.toCSVTitleCan() + "\n");
                            for (Iterator iter = canListTotal.iterator(); iter.hasNext(); ) {
                                SerialBusTxtStruct.CanStruct can = (SerialBusTxtStruct.CanStruct) iter.next();
                                sb.append(can.toCSV() + "\n");
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_SPI: {
                            LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiListTotal = SerialBusManage.getInstance()
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);
                            sb.append("SerialsBusTextSPI " + Tools.genNameByDateTime() + "\n");
                            sb.append(SerialBusTxtStruct.toCSVTitleSpi() + "\n");
                            for (Iterator iter = spiListTotal.iterator(); iter.hasNext(); ) {
                                SerialBusTxtStruct.SpiStruct spi = (SerialBusTxtStruct.SpiStruct) iter.next();
                                sb.append(spi.toCSV() + "\n");
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_I2C: {
                            LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cListTotal = SerialBusManage.getInstance()
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);
                            sb.append("SerialsBusTextI2C " + Tools.genNameByDateTime() + "\n");
                            sb.append(SerialBusTxtStruct.toCSVTitleI2c() + "\n");
                            for (Iterator iter = i2cListTotal.iterator(); iter.hasNext(); ) {
                                SerialBusTxtStruct.I2cStruct i2c = (SerialBusTxtStruct.I2cStruct) iter.next();
                                sb.append(i2c.toCSV() + "\n");
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_M429: {
                            LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429ListTotal = SerialBusManage.getInstance()
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);
                            sb.append("SerialsBusText429 " + Tools.genNameByDateTime() + "\n");
                            sb.append(SerialBusTxtStruct.toCSVTitleArinc492() + "\n");
                            for (Iterator iter = arinc429ListTotal.iterator(); iter.hasNext(); ) {
                                SerialBusTxtStruct.Arinc429Struct a429 = (SerialBusTxtStruct.Arinc429Struct) iter.next();
                                sb.append(a429.toCSV() + "\n");
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_M1553B: {
                            LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> m1553bListTotal = SerialBusManage.getInstance()
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);
                            sb.append("SerialsBusText1553B " + Tools.genNameByDateTime() + "\n");
                            sb.append(SerialBusTxtStruct.toCSVTitleM1553b() + "\n");
                            for (Iterator iter = m1553bListTotal.iterator(); iter.hasNext(); ) {
                                SerialBusTxtStruct.MilSTD1553bStruct m1553b = (SerialBusTxtStruct.MilSTD1553bStruct) iter.next();
                                sb.append(m1553b.toCSV() + "\n");
                            }
                        }
                        break;
                    }
                    Tools.saveStringBuild(PathFile, sb);
//                    Tools.sleep(3000);
                    //endregion
                }
                break;
            }

            if (mainActivity == null) {
                isComplete = true;
                Message msg = new Message();
                msg.what = Handler_SaveCompleteFailed;
                msg.obj = new UiHandlerMsg(this.PathFile, saveCallBack);
                uiHandler.sendMessage(msg);
                return;
            }
            Memory.Sync();
            boolean b = uDiskUpdate(this.PathFile);
            if (b) {

                Message msg = new Message();
                msg.what = Handler_SaveCompleteSuccess;
                msg.obj = new UiHandlerMsg(this.PathFile, saveCallBack);
                msg.arg1 = 1;
                uiHandler.sendMessage(msg);
            } else {
                Message msg = new Message();
                msg.what = Handler_SaveCompleteFailed;
                msg.obj = new UiHandlerMsg(this.PathFile, saveCallBack);
                msg.arg1 = 0;
                uiHandler.sendMessage(msg);
            }

            isComplete = true;
            Logger.i(TAG, "thread pool end!");
        }
    }

    class UiHandlerMsg {
        String pathFile;
        SaveCallBack saveCallBack;

        public UiHandlerMsg(String pathFile, SaveCallBack saveCallBack) {
            this.pathFile = pathFile;
            this.saveCallBack = saveCallBack;
        }
    }

    public interface SaveCallBack {
        void onResult(boolean success, String msg);
    }

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Handler_SaveCompleteSuccess: {
                    UiHandlerMsg handlerMsg = (UiHandlerMsg) msg.obj;
                    saveCompleteSuccess(handlerMsg.pathFile);
                    uDiskUpdate(handlerMsg.pathFile);
                    if (handlerMsg.saveCallBack != null) {
                        String[] paths = handlerMsg.pathFile.split("/");
                        String resultMsg = String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinSuccess), paths[paths.length - 1]);
                        handlerMsg.saveCallBack.onResult(true, resultMsg);
                    }
                    Logger.i(TAG, "Handler_SaveCompleteSuccess["
                            + Thread.currentThread().getName() + "]" + handlerMsg.pathFile);
                }
                break;
                case Handler_SaveCompleteFailed: {
                    UiHandlerMsg handlerMsg = (UiHandlerMsg) msg.obj;
                    saveCompleteFailed(handlerMsg.pathFile);
//                    int spaceAvailable = msg.arg1;
                    uDiskUpdate(handlerMsg.pathFile);
                    if (handlerMsg.saveCallBack != null) {
                        String[] paths = handlerMsg.pathFile.split("/");
                        String resultMsg = String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinFailed), paths[paths.length - 1]);
//                        if(spaceAvailable == 0) {
//                            resultMsg = mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveCsvNotSpace);
//                        }
                        handlerMsg.saveCallBack.onResult(false, resultMsg);
                    }
                    Logger.i(TAG, "Handler_SaveCompleteFailed["
                            + Thread.currentThread().getName() + "]" + handlerMsg.pathFile);
                }
                break;
                case Handler_UDiskUpdate: {
                    uDiskUpdate((String) msg.obj);
                    Logger.i(TAG, "Handler_UDiskUpdate["
                            + Thread.currentThread().getName() + "]" + (String) msg.obj);
                }
                break;

            }
        }
    }

    public boolean uDiskUpdate(String path) {
        if (mainActivity == null) return false;
        if (!Tools.fileIsExists(path)) return false;
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse("file://" + path);
        mediaScanIntent.setData(contentUri);
        mainActivity.sendBroadcast(mediaScanIntent);

        return Tools.fileIsExists(path);
    }

    private void saveCompleteSuccess(String path) {
        //提示保存的信息
        if (path == null) return;
        Logger.i(TAG, "send saveCompleteSuccess!" + path);
        String[] s = path.split("/");
        if (s[s.length - 1].contains("SaveRecovery")) return;
//        DToast.get().show(String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinSuccess), s[s.length - 1]));
    }

    private void saveCompleteFailed(String path) {
        //提示储存信息失败
        if (path == null) return;
        Logger.i(TAG, "send saveCompleteFailed!" + path);
        String[] s = path.split("/");
        if (s[s.length - 1].contains("SaveRecovery")) return;
//        DToast.get().show(String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinFailed), s[s.length - 1]));
    }

    private boolean fileExistResultField(String pathFile, SaveCallBack saveCallBack) {
        boolean exist = false;
        File file = new File(pathFile);
        exist = file.exists();
        if (exist) {
            Message msg = new Message();
            msg.what = Handler_SaveCompleteFailed;
            msg.obj = new UiHandlerMsg(pathFile, saveCallBack);
            uiHandler.sendMessage(msg);
        }
        return exist;
    }

    /**
     * 返回保存的ref文件列表，同时返回u盘和本地目录下的列表
     */
    public File[] getFliesFromCurRef(String saveDir) {
        List<String> uDisk = Tools.getAllExternalSdcardPath();
        String uDiskPath = "", localPath = "";
        if (uDisk.size() > 0) {
            uDiskPath = Tools.resultSavePath(Tools.SaveType_UDISK, saveDir, mainActivity);
        }
        localPath = Tools.resultSavePath(Tools.SaveType_LOCAL, saveDir, mainActivity);

        File[] uDiskFileList = new File(uDiskPath).listFiles();
        File[] localFileList = new File(localPath).listFiles();

        if (uDiskFileList == null) uDiskFileList = new File[0];
        if (localFileList == null) localFileList = new File[0];

        File[] result = new File[uDiskFileList.length + localFileList.length];
        System.arraycopy(uDiskFileList, 0, result, 0, uDiskFileList.length);
        System.arraycopy(localFileList, 0, result, uDiskFileList.length, localFileList.length);
        return result;
    }

    public ArrayList<File> getFilesFromCur(String saveDir) {
        ArrayList<File> arrFiles = new ArrayList<>();
        File[] files = new File(saveDir).listFiles();
        if (files == null || files.length == 0) return arrFiles;
        for (File file : files) {
            if (!file.isDirectory()) arrFiles.add(file);
        }
        return arrFiles;
    }


    /**
     * 生成新的序列号名字，不保存序列号到cache
     */
    public String generateName() {
        boolean sbt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
        int saveType = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_TYPE);
        String cacheData;
        String cacheIndex;
        if (!sbt && saveType == 0) {//wav
            cacheData = CacheUtil.GENNAME_INDEXDATE_WAV;
            cacheIndex = CacheUtil.GENNAME_INDEX_WAV;
        } else if (!sbt && saveType == 1) {//csv
            cacheData = CacheUtil.GENNAME_INDEXDATE_CSV;
            cacheIndex = CacheUtil.GENNAME_INDEX_CSV;
        } else if (!sbt && saveType == 2) {//bin
            cacheData = CacheUtil.GENNAME_INDEXDATE_BIN;
            cacheIndex = CacheUtil.GENNAME_INDEX_BIN;
        } else {//serialsBusText
            cacheData = CacheUtil.GENNAME_INDEXDATE_SBT;
            cacheIndex = CacheUtil.GENNAME_INDEX_SBT;
        }
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String str_time = sdf.format(date);
        if (CacheUtil.get().getOtherMapValue(cacheData).equals(str_time)) {
            int index = Integer.parseInt(CacheUtil.get().getOtherMapValue(cacheIndex)) + 1;
            String sIndex = String.format("%04d", index);
            return str_time + sIndex;
        } else {
            String sIndex = String.format("%04d", 1);
            return str_time + sIndex;
        }
    }

//    public String generateName(String oldName) {
//
//    }


    /**
     * 检查名字是否存在
     */
    public boolean checkName(String saveDir, String name) {
        File[] files = getFliesFromCurRef(saveDir);
        if (files == null) {
            return false;
        }
        for (File file : files) {
            if (file.getName()
                    .replace(".mwav", "")
                    .replace(".wav", "")
                    .replace(".bin", "")
                    .replace(".csv", "")
                    .equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查文件是否存在
     */
    public boolean checkFileExists(String filePath) {
        File file = new File(filePath);
        Logger.i("SaveManage checkFileExists name= " + file.getAbsolutePath() + " ,isExists= " + file.exists());
        return file.exists();
    }

    /**
     * 检查名字，如果是序列号名字，则保存序列号到cache
     */
    public void putCacheName(String name) {
        String _name=name;
        if (name.contains("_") && name.split("_").length>=2){
            _name=name.split("_")[1];
        }
        boolean sbt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
        int saveType = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_TYPE);
        String cacheData;
        String cacheIndex;
        if (!sbt && saveType == 0) {//wav
            cacheData = CacheUtil.GENNAME_INDEXDATE_WAV;
            cacheIndex = CacheUtil.GENNAME_INDEX_WAV;
        } else if (!sbt && saveType == 1) {//csv
            cacheData = CacheUtil.GENNAME_INDEXDATE_CSV;
            cacheIndex = CacheUtil.GENNAME_INDEX_CSV;
        } else if (!sbt && saveType == 2) {//bin
            cacheData = CacheUtil.GENNAME_INDEXDATE_BIN;
            cacheIndex = CacheUtil.GENNAME_INDEX_BIN;
        } else {//serialsBusText
            cacheData = CacheUtil.GENNAME_INDEXDATE_SBT;
            cacheIndex = CacheUtil.GENNAME_INDEX_SBT;
        }
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String str_time = sdf.format(date);
        String uName;
        String sIndex;
        if (CacheUtil.get().getOtherMapValue(cacheData).equals(str_time)) {
            int index = Integer.parseInt(CacheUtil.get().getOtherMapValue(cacheIndex)) + 1;
            sIndex = String.format("%04d", index);
            uName = str_time + sIndex;
        } else {
            CacheUtil.get().putOtherMap(cacheData, str_time);
            sIndex = String.format("%04d", 1);
            CacheUtil.get().putOtherMapAndSave(cacheIndex, sIndex);
            uName = str_time + sIndex;
        }

        if (uName.equals(_name)) {
            CacheUtil.get().putOtherMapAndSave(cacheIndex, sIndex);
        }
    }

    public String getDefaultPath(String saveDir) {
        String pre = mainActivity.getString(R.string.internal_storage_for_display);
//        if (pre.equals("内部存储空间")) {
//            pre = "Internal shared storage";
//        }
        return File.separator + pre + Tools.resultDefaultSavePath(saveDir, mainActivity);
    }

    public String getAbDefaultPath(String saveDir) {
        return Tools.resultAbDefaultSavePath(saveDir, mainActivity);
    }
}
