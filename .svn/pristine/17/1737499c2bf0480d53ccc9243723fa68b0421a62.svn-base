package com.micsig.tbook.scope.Data;

import android.os.Build;
import android.os.SharedMemory;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.mem.Memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SingleSaveData {
    private static final String TAG = "SingleSaveData";
    public static final int AUTO_SAVE_WAV = 1<<0;
    public static final int AUTO_SAVE_CSV = 1<<1;
    public static final int AUTO_SAVE_BIN = 1<<2;
    public static final int AUTO_SAVE_IMAGE = 1<<3;
    public static final int AUTO_SAVE_SESSION = 1<<4;
    private List<IChannel> channels = new ArrayList<>();
    private int autoSaveType = 0;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String filePath;

    private boolean bSaveRun = false;



    public interface ISaveDataListener {
        void onBegin();
        void onEnd();
        void onSaveBefore(boolean bProgress);

        void onProgress(int val);
        void onSaveAfter(boolean bProgress);

        void onPicture(String filePath,String fileName);
        HashMap<String, HashMap<String, String>> onCurCache();
    }

    private ISaveDataListener autoSaveListener;

    private static volatile SingleSaveData instance = null;

    public static SingleSaveData getInstance() {
        if (instance == null) {
            synchronized (SingleSaveData.class) {
                if (instance == null) {
                    instance = new SingleSaveData();
                }
            }
        }
        return instance;
    }
    private SingleSaveData(){
    }

    public synchronized void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public synchronized boolean isRun(){
        return this.bSaveRun;
    }
    private void close(RandomAccessFile raf){
        if(raf != null){
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public synchronized void start(){
        Log.d(TAG, "start() called :" + filePath);
        if(!this.bSaveRun){
            if(!filePath.endsWith(File.separator)){
                filePath += File.separator;
            }
            directory = new File(this.filePath);
            if(isDirectory()){
                errCode = 0;
                bSaveRun = true;
                this.channels.clear();
                for(int i=ChannelFactory.CH1;i<ChannelFactory.REF_MAX;i++){
                    if(ChannelFactory.isChOpen(i)){
                        channels.add(ChannelFactory.getValidChannel(i));
                    }
                }

                csvFileName = filePath + "savedata.csv";
                imageFileName = filePath + "savedata.png";
                sessionFileName = filePath + "savedata.mss";
                close(mssraf);
                close(pngraf);
                close(csvraf);
                mssraf = pngraf = csvraf = null;
                executorService.execute(this::run);
            }else{
                Log.e(TAG, "start() called isDirectory :" + this.isDirectory());
            }
        }else{
            Log.e(TAG, "start() called bSaveRun :" + this.bSaveRun);
        }
    }
    public synchronized void stop(){
        Log.d(TAG, "stop() called");
        this.bSaveRun = false;
    }
    private boolean isDirectory(){
        return directory.exists();
    }
    private File directory;

    private long frameSize = 0;
    private long frameWaveSize = 0;
    private long frameCsvSize = 0;
    private long frameBinSize = 0;
    private long frameSessionSize = 0;
    private long chBinSize = 0;
    private long onceCsvSize = 0;

    private static final int IMAGE_SIZE = 1024*1024;

    public long estimateStorage(){

        long s = 0;
        int type = getAutoSaveType();
        long n1,n2;
        n1 = n2 = 0;
        SegmentSample segmentSample = SegmentSample.getInstance();
        int frameNums = 1;
        if(segmentSample.isSegmentEnable()){
            frameNums = Scope.getInstance().getSegmentFrameNums();
        }
        if(frameNums < 1) frameNums = 1;
        Log.d(TAG,"frameNums:" + frameNums);
        //n2为模拟通道，n1是所有通道号，bin只存储模拟通道
        for(IChannel channel:channels){
            if(channel.isOpen()) {
                if (ChannelFactory.isDynamicCh(channel.getChId())) {
                    n2++;
                }
                n1++;
            }
        }
        frameSize = 0;
        frameWaveSize = 0;
        frameCsvSize = 0;
        frameBinSize = 0;
        frameSessionSize = 0;
        if(0 != (type & AUTO_SAVE_WAV)){
            frameWaveSize = n1 * WaveData.BUFFER_SIZE ;//* frameNums;
        }
        if(0 != (type & AUTO_SAVE_CSV)){
            onceCsvSize = saveCsv.calcStorageSize();
            frameCsvSize = onceCsvSize ;//* frameNums;
        }
        if(0 != (type & AUTO_SAVE_BIN)){
            chBinSize = SaveBin.getInstance().estimateStorage();
            frameBinSize = chBinSize * n2;
        }
        if(0 != (type & AUTO_SAVE_IMAGE)){
            s = IMAGE_SIZE;
        }
        if(0 != (type & AUTO_SAVE_SESSION)){
            frameSessionSize = saveRecoverySession.estimateStorage();
        }
        frameSize = frameWaveSize + frameCsvSize + frameBinSize + frameSessionSize + s;
        return frameSize;
    }


    public void setAutoSaveListener(ISaveDataListener autoSaveListener){
        this.autoSaveListener = autoSaveListener;
    }
    public synchronized void setSaveType(int autoSaveType){
        this.autoSaveType = autoSaveType;
    }
    public synchronized int getAutoSaveType(){
        return this.autoSaveType;
    }
    String csvFileName;
    String imageFileName;
    String sessionFileName;
    private long saveSize = 0;
    private long saveItemSize = 0;
    private static final int WAIT_TIME = 100;
    private int errCode = 0;
    private synchronized void setErrCode(int errCode){
        this.errCode = errCode;
    }
    public synchronized int getErrCode(){
        return this.errCode;
    }
    private static final int ERR_STOP = 1;      //停止条件满足退出
    private static final int ERR_FULL = 2;      //磁盘满退出
    private static final int ERR_DIR = 3;       //存储目录异常退出
    private void run() {
        boolean b = false;
        Scope scope = Scope.getInstance();
        b = scope.isRun();
        onBegin();
        onSaveBefore(false);
        estimateStorage();
        saveSize = 0;
        saveItemSize = 0;
        int type = getAutoSaveType();
        if(b) {
            if ((type & (AUTO_SAVE_CSV | AUTO_SAVE_SESSION)) != 0) {
                ms_sleep(200);
            }
        }
        onSaveBefore(true);
        if((type & AUTO_SAVE_CSV) != 0) {
            onSaveWave();
        }
        if((type & AUTO_SAVE_SESSION) != 0) {
            onSaveSession();
        }
        onSaveAfter(true);
        if((type & AUTO_SAVE_IMAGE) != 0) {
            onSaveImage();
        }
        onSaveAfter(false);
        onEnd();
    }


    private void onSaveWave(){
        int num = 0;
        long frameNums = 1;

        int type = getAutoSaveType();
        long baksize = saveSize;
        do {
            saveSize += saveItemSize;
            saveItemSize = 0;
            onProgress();

            if((type & AUTO_SAVE_CSV) != 0) {
                long s = 0;
                saveCsv.clear();
                for (IChannel channel : channels) {
                    if (channel.isOpen()) {
                        saveCsv.add(channel);
                    }
                }
                saveCsv.setSaveCsvProgress(val -> {
                    if(val > 0 && val < 100) {
                        saveItemSize = onceCsvSize * val / 100;
                    }
                });
                File f = new File(csvFileName);
                if(f.exists()){
                    f.delete();
                }
                saveCsv.save(csvFileName);
                while (!saveCsv.isFinish()){
                    ms_sleep(10);
                    if(s != saveItemSize){
                        onProgress();
                        s = saveItemSize;
                    }
                }
                saveSize += saveItemSize;
                saveItemSize = 0;
                onProgress();
            }
            num++;
        }while (num < frameNums);
        saveItemSize = 0;
        saveSize = baksize + frameWaveSize + frameCsvSize;
        ms_sleep(100);
    }

    SaveCsv saveCsv = SaveCsv.getInstance();
    private void onSaveImage(){
        File f = new File(imageFileName);
        if(f.exists()){
            f.delete();
        }
        onPicture(filePath,"savedata.png");
        ms_sleep(WAIT_TIME);
    }
    SaveRecoverySession saveRecoverySession = SaveRecoverySession.getInstance();
    private void onSaveSession(){
        saveItemSize = 0;
        HashMap<String, HashMap<String, String>> map = onCurCache();
        if(map != null) {
            long bak = 0;
            File f = new File(sessionFileName);
            if(f.exists()){
                f.delete();
            }
            saveRecoverySession.store(map, sessionFileName);
            while (!saveRecoverySession.isDone()) {
                ms_sleep(100);
                int v = saveRecoverySession.getSaveRecoveryProgress();
                if( v != bak){
                    saveItemSize = frameSessionSize * v / 100;
                    onProgress();
                    bak = v;
                }
            }
            saveItemSize = 0;
            saveSize += frameSessionSize;
            onProgress();
        }
    }

    private void ms_sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onSaveBefore(boolean bProgress){
        Log.d(TAG, "onSaveBefore() called :" + bProgress);
        if(null != autoSaveListener){
            autoSaveListener.onSaveBefore( bProgress);
        }
    }

    private void onProgress(){
        long val = 0;
        synchronized (this) {
            val = (saveSize + saveItemSize) * 100 / frameSize;
        }
        if(null != autoSaveListener){
            autoSaveListener.onProgress((int) val);
        }
    }
    private void onSaveAfter(boolean bProgress){
        Log.d(TAG, "onSaveAfter() called");
        if(null != autoSaveListener){
            autoSaveListener.onSaveAfter(bProgress);
            if(bProgress){
                ms_sleep(WAIT_TIME);
            }
        }
    }
    private void onBegin(){
        Log.d(TAG, "onBegin() called");
        if(null != autoSaveListener){
            autoSaveListener.onBegin();
        }
    }
    private synchronized void onEnd(){
        Log.d(TAG, "onEnd() called");
        Memory.Sync();
        if(null != autoSaveListener){
            autoSaveListener.onEnd();
        }
        bSaveRun = false;
    }
    private HashMap<String, HashMap<String, String>> onCurCache(){
        Log.d(TAG, "onCurCache() called");
        HashMap<String, HashMap<String, String>> map = null;
        if(null != autoSaveListener){
            map = autoSaveListener.onCurCache();
        }
        return map;
    }
    private void onPicture(String filePath,String fileName){
        Log.d(TAG, "onPicture() called with: filePath = [" + filePath + "], fileName = [" + fileName + "]");
        if(null != autoSaveListener){
            autoSaveListener.onPicture(filePath,fileName);
        }
    }

    RandomAccessFile csvraf;
    RandomAccessFile pngraf;
    RandomAccessFile mssraf;
    private static final int READ_BUF_SIZE = 1 * 1024 * 1024;
    byte[] data;

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    private void writeToShm(SharedMemory shm,RandomAccessFile raf){
        if(data == null){
            data = new byte[READ_BUF_SIZE];
        }
        int len = 0;
        ByteBuffer destBuf = null;
        try {
            destBuf = shm.mapReadWrite();
            if(raf != null){
                try {
                    len = raf.read(data);
                    Log.d(TAG,"read len:" + len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(len < 0){
                len = 0;
            }
            String end="\r\n";
            String head = String.format("#9%09d", len);
            destBuf.putInt(len + 13);
            destBuf.put(head.getBytes());
            destBuf.put(data,0,len);
            destBuf.put(end.getBytes());
            shm.unmap(destBuf);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    public void writeCsvToShm(SharedMemory shm){
        if(csvraf == null){
            File file = new File(csvFileName);
            if(file.exists()) {
                try {
                    csvraf = new RandomAccessFile(file,"r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        writeToShm(shm,csvraf);
    }
    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    public void writePngToShm(SharedMemory shm){

        if(pngraf == null){
            File file = new File(imageFileName);
            if(file.exists()) {
                try {
                    pngraf = new RandomAccessFile(file,"r");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        writeToShm(shm,pngraf);
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    public void writeMssToShm(SharedMemory shm){

        if(mssraf == null){
            File file = new File(sessionFileName);
            if(file.exists()) {
                try {
                    mssraf = new RandomAccessFile(file,"r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        writeToShm(shm,mssraf);
    }

}
