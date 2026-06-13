package com.micsig.tbook.scope.Data;

import android.os.SystemClock;
import android.util.Log;

import com.micsig.base.Utils;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeMessage;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.scope.mem.Memory;

import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoSave implements Observer {
    private static final String TAG = "AutoSave";

    public static final int AUTO_SAVE_WAV = 1<<0;
    public static final int AUTO_SAVE_CSV = 1<<1;
    public static final int AUTO_SAVE_BIN = 1<<2;
    public static final int AUTO_SAVE_IMAGE = 1<<3;
    public static final int AUTO_SAVE_SESSION = 1<<4;
    public static final int STOP_CONDITION_NONE = 0;
    public static final int STOP_CONDITION_TIME = 1;
    public static final int STOP_CONDITION_FRAMES = 2;
    public static final int SAVE_MODE_FULL_STOP = 0;
    public static final int SAVE_MODE_LOOP = 1;

    public static final int MAX_SUFFIXCODE = 1000000;
    public LocalDateTime startZonedDateTime;
    public LocalDateTime stopZonedDateTime;
    public int stopCondition = STOP_CONDITION_NONE;
    private List<IChannel> channels = new ArrayList<>();
    private int nFrames = 0;
    private int sotpFrames = 0;
    private long frameInterval = 0;
    private int saveMode = SAVE_MODE_FULL_STOP;
    private int autoSaveType = 0;

    private long saveTimestamp = -1;

    ExecutorService executorService = Executors.newFixedThreadPool(2);


    private String filePath;

    private String prefixName;

    private int suffixCode;

    private boolean bAutoSave = false;
    private boolean bSaveRun = false;

    private final AtomicBoolean bSaving = new AtomicBoolean(false);

    public boolean isSaving(){
        return bSaving.get();
    }

    private AtomicBoolean bTriggerState = new AtomicBoolean(false);


    public interface IAutoSaveListener{
        void onBegin();
        void onEnd();
        void onSaveBefore(boolean bProgress);

        void onProgress(int val);
        void onSaveAfter(boolean bProgress);

        void onPicture(String filePath,String fileName);
        HashMap<String, HashMap<String, String>> onCurCache();
    }

    private IAutoSaveListener autoSaveListener;

    private static volatile AutoSave instance = null;

    public static AutoSave getInstance() {
        if (instance == null) {
            synchronized (AutoSave.class) {
                if (instance == null) {
                    instance = new AutoSave();
                }
            }
        }
        return instance;
    }
    private AutoSave(){
        EventFactory.addEventObserver(EventFactory.EVENT_SAMPLE_VALID,this);
    }

    public synchronized void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public synchronized void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }

    public synchronized void setSuffixCode(int suffixCode) {
        this.suffixCode = suffixCode;
    }
    public void setChannels(List<IChannel> channels){
        this.channels.clear();
        this.channels.addAll(channels);
    }
    public synchronized void setSaveMode(int saveMode){
        this.saveMode = saveMode;
    }
    private synchronized int getSaveMode(){
        return this.saveMode;
    }
    public synchronized void setFrameInterval(long frameInterval){
        this.frameInterval = frameInterval;
    }
    public synchronized void setStopCondition(int condition){
        this.stopCondition = condition;
    }
    private synchronized int getStopCondition(){
        return this.stopCondition;
    }
    public synchronized void setStartDateTime(LocalDateTime zonedDateTime){
        this.startZonedDateTime = zonedDateTime;
    }
    private synchronized LocalDateTime getStartDateTime(){
        return this.startZonedDateTime;
    }
    public synchronized void setStopConditionTime(LocalDateTime zonedDateTime){
        this.stopZonedDateTime = zonedDateTime;
    }
    private synchronized LocalDateTime getStopDateTime(){
        return this.stopZonedDateTime;
    }
    public synchronized void setStopConditionFrames(int nums){
        this.sotpFrames = nums;
    }
    private synchronized int getStopConditionFrames(){
        return this.sotpFrames;
    }
    private synchronized boolean isAutoSave(){
        return bAutoSave;
    }
    public synchronized boolean isRun(){
        return this.bSaveRun;
    }

    public synchronized void start(){
        Log.d(TAG, "start() called");
        if(!this.bSaveRun){
            directory = new File(this.filePath);
            if(isDirectory()){
                errCode = nFrames = groupCode = 0;
                saveTimestamp = userInputTimestamp = Long.MIN_VALUE;
                this.bAutoSave = bSaveRun = true;
                if(saveMode == SAVE_MODE_LOOP) {
                    bLoopDelete.set(true);
                    executorService.execute(this::onLoopDelete);
                }else {
                    bLoopDelete.set(false);
                }
                executorService.execute(this::run);
            }
        }
    }
    public synchronized void stop(){
        Log.d(TAG, "stop() called");
        this.bAutoSave = false;
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

    AtomicInteger atomicInteger = new AtomicInteger(0);
    private boolean isTriggered(){
        return bTriggerState.get();
    }
    private void setTriggerState(boolean bTriggerState){
        Log.d(TAG, "setTriggerState() called with: bTriggerState = [" + bTriggerState + "]");
        if(bTriggerState){
            synchronized (this) {
                triggerTimestamp = SystemClock.elapsedRealtime();
            }
        }
        atomicInteger.set(0);
        this.bTriggerState.set(bTriggerState);

    }
    private long triggerTimestamp = 0;



    public void setAutoSaveListener(IAutoSaveListener autoSaveListener){
        this.autoSaveListener = autoSaveListener;
    }
    public synchronized void setSaveType(int autoSaveType){
        this.autoSaveType = autoSaveType;
    }
    public synchronized int getAutoSaveType(){
        return this.autoSaveType;
    }
    private synchronized void setFrames(int nFrames){
        this.nFrames = nFrames;
    }
    public synchronized int getFrames(){
        return nFrames;
    }
    public synchronized int  getSuffixCode(){
        return suffixCode;
    }
    private long userInputTimestamp = 0;
    public synchronized void setUserInput(boolean bUserInput){
        userInputTimestamp = SystemClock.elapsedRealtime();
    }
    private synchronized void setSaveTimestamp(long timestamp){
        Log.d(TAG, "setSaveTimestamp() called with: timestamp = [" + timestamp + "]");
        this.saveTimestamp = timestamp;
    }
    String wavePath;
    String csvPath;
    String binPath;
    String imagePath;
    String sessionPath;

    String groupPath;

    int groupCode = 0;
    int deleteCode = 0;
    int deleteSuffixCode = 0;

    int [] fileNums = new int[5];

    private void addFile(int type){
        switch (type){
            case AUTO_SAVE_WAV:
                fileNums[0]++;
                break;
            case AUTO_SAVE_CSV:
                fileNums[1]++;
                break;
            case AUTO_SAVE_BIN:
                fileNums[2]++;
                break;
            case AUTO_SAVE_IMAGE:
                fileNums[3]++;
                break;
            case AUTO_SAVE_SESSION:
                fileNums[4]++;
                break;
        }
    }
    private static final int MAX_FILE_NUMS = 1000;
    private boolean isMaxFileNums(){
        for(int v:fileNums){
            if(v >= MAX_FILE_NUMS){
                return true;
            }
        }
        return false;
    }
    private synchronized void genDir(boolean Init){
        String fileName = filePath;
        if(Init) {
            if (!fileName.endsWith(File.separator)) {
                fileName += File.separator;
            }
            fileName += prefixName + "_" + ZonedDateTime.now().format(formatter) + File.separator;
            groupPath = fileName;
            deleteCode = groupCode = 0;
            deleteSuffixCode = suffixCode;
            directory = new File(groupPath);
        }else{
            fileName = groupPath;
            groupCode++;
        }

        String groupName = String.format("_M%07d",groupCode);
        wavePath = fileName + prefixName + groupName + "_Wav" + File.separator;
        csvPath = fileName + prefixName + groupName + "_Csv" + File.separator;
        binPath = fileName + prefixName + groupName + "_Bin" + File.separator;
        imagePath = fileName + prefixName + groupName + "_Picture" + File.separator;
        sessionPath = fileName + prefixName + groupName + "_Session" + File.separator;

        String [] paths = {wavePath,csvPath,binPath,imagePath,sessionPath};
        int type = getAutoSaveType();
        for(int i=0;i<paths.length;i++){
            if((type & (1<<i)) != 0){
                File file = new File(paths[i]);
                if(!file.exists()){
                    file.mkdirs();
                }
            }
        }
        Arrays.fill(fileNums,0);
    }
    private long saveSize = 0;
    private long saveItemSize = 0;
    private static final int WAIT_TIME = 100;
    private AtomicBoolean bLoopDelete = new AtomicBoolean(false);
    private void onLoopDelete(){
        Log.d(TAG, "onLoopDelete() called in");
        while (isAutoSave()){
            onDelete();
            ms_sleep(WAIT_TIME);
        }
        bLoopDelete.set(false);
        Log.d(TAG, "onLoopDelete() called out");
    }
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
        Scope scope = Scope.getInstance();
        ScopeMessage scopeMessage = ScopeMessage.getInstance();
        genDir(true);
        onBegin();
        while (isAutoSave()
                && !isStart()){
            ms_sleep(10);
        }

        Log.d(TAG, "run() called  : " + frameInterval);
        while (isAutoSave()
                && isDirectory()){

            if(scope.isStandby()
                    || !scopeMessage.messageFinished()){

                ms_sleep(WAIT_TIME);
                continue;
            }

            if(isInterval()){
                setTriggerState(false);
                scope.setSingle(true);

                long singleNum = 0;
                while (isAutoSave()
                        && isHold()){
                    if(isTriggered()){
                        bSaving.set(true);
                        onSaveBefore(false);
                        estimateStorage();
                        onDelete();
                        int type = getAutoSaveType();
                        saveSize = saveItemSize = 0;

                        if((type & (AUTO_SAVE_WAV
                                | AUTO_SAVE_CSV
                                | AUTO_SAVE_BIN
                                | AUTO_SAVE_SESSION)) != 0) {
                            onSaveBefore(true);
                            onProgress();
                            if ((type & (AUTO_SAVE_WAV | AUTO_SAVE_CSV)) != 0
                                    && !scope.isStandby()) {
                                onSaveWave();
                                onProgress();
                            }
                            if ((type & AUTO_SAVE_BIN) != 0
                                    && !scope.isStandby()) {
                                onSaveBin();
                                onProgress();
                            }
                            if ((type & AUTO_SAVE_SESSION) != 0
                                    && !scope.isStandby()) {
                                onSaveSession();
                                onProgress();
                            }
                            onSaveAfter(true);
                        }
                        if((type & AUTO_SAVE_IMAGE) != 0
                            && !scope.isStandby()){
                            onSaveImage();
                            saveSize += IMAGE_SIZE;
                        }
                        synchronized (this) {
                            suffixCode++;
                            if(suffixCode >= MAX_SUFFIXCODE){
                                suffixCode = 0;
                            }
                        }
                        setFrames(getFrames()+1);
                        onSaveAfter(false);
                        setTriggerState(false);
                        bSaving.set(false);
                        break;
                    }else{
                        ms_sleep(WAIT_TIME);
                        if(!isTriggered()) {
                            if (!scope.isRun()) {
                                singleNum++;
                                if (singleNum >= 3) {
                                    break;
                                }
                            }
                        }
                    }
                    if(isStop()){
                        break;
                    }
                }
                setSaveTimestamp(SystemClock.elapsedRealtime());
            }else {
                ms_sleep(WAIT_TIME);
            }
            if(isStop()){
                setErrCode(ERR_STOP);
                break;
            }
            if(isFull()){
                setErrCode(ERR_FULL);
                break;
            }
            if(isMaxFileNums()){
                genDir(false);
            }
        }
        if(isDirectory()){
            setErrCode(ERR_DIR);
        }
        stop();
        while (bLoopDelete.get()){
            ms_sleep(WAIT_TIME);
        }
        onEnd();
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private String genFileName(int type,String name,int code){
        String suffixName = String.format("%07d",code);
        switch (type){
            case AUTO_SAVE_WAV:
                return prefixName + "_" + suffixName + "_"+ name + ".mwav";
            case AUTO_SAVE_CSV:
                return  prefixName + "_" + suffixName + name + ".csv";
            case AUTO_SAVE_BIN:
                return  prefixName + "_" + suffixName + "_" + name + ".bin";
            case AUTO_SAVE_IMAGE:
                return prefixName + "_" + suffixName + ".png";
            case AUTO_SAVE_SESSION:
                return  prefixName + "_" + suffixName + ".mss";
        }

        return "";
    }
    private String genFileName(int type,String name){
        return genFileName(type,name,getSuffixCode());
    }

    private int curFrameNo = 0;
    private synchronized void setFrameNo(int curFrameNo){
        Log.d(TAG, "setFrameNo() called with: curFrameNo = [" + curFrameNo + "]");
        this.curFrameNo = curFrameNo;
        atomicInteger.set(0);
    }
    private synchronized int getFrameNo(){
        return this.curFrameNo;
    }
    SaveCsv saveCsv = SaveCsv.getInstance();

    private synchronized void sendFrameNo(SegmentSample segmentSample,int no,int type){
        atomicInteger.set(0);
        segmentSample.setFrameNo(no,SegmentSample.SEGMENT_DISPLAY_SFRAME);
    }
    private void onSaveWave(){
        SegmentSample segmentSample = SegmentSample.getInstance();
        Scope scope = Scope.getInstance();
        String name = "";
        boolean bSegment = false;//segmentSample.isSegmentEnable();
        int num = 0;
        long frameNums = 1;
        if(bSegment){
            frameNums = scope.getSegmentFrameNums();
            if(frameNums < 1) frameNums = 1;
            setFrameNo(-1);
        }
        int type = getAutoSaveType();
        long baksize = saveSize;
        long bakFrameN = segmentSample.getFrameNo();
        int bakFrameType = segmentSample.getSegmentDisplayType();

        do {
            if(bSegment){
                name = String.format("_%07d",num+1);
                Log.d(TAG,"name:" + name);
                long sendTimestamp = SystemClock.elapsedRealtime();
                sendFrameNo(segmentSample,num,SegmentSample.SEGMENT_DISPLAY_SFRAME);
                while (getFrameNo() != num){
                    ms_sleep(10);
                    long t = SystemClock.elapsedRealtime();
                    if(t - sendTimestamp > 1000){
                        sendFrameNo(segmentSample,num,SegmentSample.SEGMENT_DISPLAY_SFRAME);
                        sendTimestamp = t;
                    }
                }
            }

            if((type & AUTO_SAVE_WAV) != 0) {
                saveItemSize = 0;
                for (IChannel channel : channels) {
                    if (channel.isOpen()
                            && !scope.isStandby()) {
                        String fileName = genFileName(AUTO_SAVE_WAV, channel.getName() + name);
                        channel.save(wavePath + fileName);
                        addFile(AUTO_SAVE_WAV);
                        saveItemSize += WaveData.BUFFER_SIZE;
                        onProgress();
                    }
                }
            }

            saveSize += saveItemSize;
            saveItemSize = 0;
            onProgress();
            if((type & AUTO_SAVE_CSV) != 0
                    && !scope.isStandby()) {
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
                String fileName = genFileName(AUTO_SAVE_CSV,name);
                saveCsv.save(csvPath + fileName);
                while (!saveCsv.isFinish()){
                    ms_sleep(10);
                    if(s != saveItemSize){
                        onProgress();
                        s = saveItemSize;
                    }
                }
                addFile(AUTO_SAVE_CSV);
                saveSize += saveItemSize;
                saveItemSize = 0;
                onProgress();
            }
            num++;
        }while (bSegment && num < frameNums);
        saveItemSize = 0;
        saveSize = baksize + frameWaveSize + frameCsvSize;
        if(bSegment) {
            segmentSample.setFrameNo(bakFrameN, bakFrameType);
        }
        ms_sleep(100);
    }
    private void onSaveBin(){
        ms_sleep(100);
        saveItemSize = 0;
        SaveBin saveBin = SaveBin.getInstance();
        boolean bSegment = SegmentSample.getInstance().isSegmentEnable();
        SaveBin.ISaveBinListener saveBinListener = saveBin.getSaveBinListener();
        saveBin.setSaveBinListener(val -> {
            if(val > 0 && val < 100) {
                saveItemSize = chBinSize * val / 100;
                onProgress();
            }
        });
        Scope scope = Scope.getInstance();
        for (IChannel channel : channels) {
            if(ChannelFactory.isDynamicCh(channel.getChId())
                && channel.isOpen()
                && !scope.isStandby()) {
                String fileName = genFileName(AUTO_SAVE_BIN, channel.getName());
                saveBin.save((Channel) channel, binPath + fileName,bSegment,false);
                addFile(AUTO_SAVE_BIN);
                saveItemSize = 0;
                saveSize += chBinSize;
                onProgress();
            }
        }
        saveBin.setSaveBinListener(saveBinListener);

    }
    private void onSaveImage(){
        String fileName = genFileName(AUTO_SAVE_IMAGE,"");
        File f = new File(imagePath + fileName);
        if(f.exists()){
            f.delete();
        }
        onPicture(imagePath,fileName);
        addFile(AUTO_SAVE_IMAGE);
        ms_sleep(WAIT_TIME);
    }
    SaveRecoverySession saveRecoverySession = SaveRecoverySession.getInstance();
    private void onSaveSession(){
        Log.d(TAG,"onSaveSession Enter");
        saveItemSize = 0;
        HashMap<String, HashMap<String, String>> map = onCurCache();
        if(map != null) {

            long bak = 0;
            String fileName = genFileName(AUTO_SAVE_SESSION, "");
            saveRecoverySession.store(map, sessionPath + fileName);
            while (!saveRecoverySession.isDone()) {
                ms_sleep(100);
                int v = saveRecoverySession.getSaveRecoveryProgress();
                if( v != bak){
                    saveItemSize = frameSessionSize * v / 100;
                    onProgress();
                    bak = v;
                }
            }
            addFile(AUTO_SAVE_SESSION);
            saveItemSize = 0;
            saveSize += frameSessionSize;
            onProgress();
        }
        Log.d(TAG,"onSaveSession Leave");
    }

    private String genTypePath(int type, int code){
        String str = groupPath + prefixName + String.format("_M%07d",code);
        switch (type){
            case AUTO_SAVE_WAV:
                return str + "_Wav" + File.separator ;
            case AUTO_SAVE_CSV:
                return str + "_Csv" + File.separator ;
            case AUTO_SAVE_BIN:
                return str + "_Bin" + File.separator ;
            case AUTO_SAVE_IMAGE:
                return str + "_Picture" + File.separator ;
            case AUTO_SAVE_SESSION:
                return str + "_Session" + File.separator;
        }
        return "";
    }
    private void onDelete(){
        if(saveMode == SAVE_MODE_LOOP){
            while (isAutoSave()
                    && isDelete()){

                Log.d(TAG,"onDelete 1 frameSize:" + this.frameSize + ",disk:" + Utils.getDiskAvaiableSize(directory) );
                synchronized (this) {
                    if(suffixCode - deleteSuffixCode > 1) {
                        String suffixName = prefixName + String.format("_%07d", deleteSuffixCode);
                        int type = getAutoSaveType();
                        for (int i = 0; i < fileNums.length; i++) {
                            if ((type & (1 << i)) != 0) {
                                boolean bDir = false;
                                String xpath = genTypePath(1 << i, deleteCode);
                                if (!xpath.isEmpty()) {
                                    File dir = new File(xpath);
                                    if (dir.exists()) {
                                        bDir = true;
                                        File[] allFiles = dir.listFiles(new FilenameFilter() {
                                            @Override
                                            public boolean accept(File dir, String name) {
                                                return name.startsWith(suffixName);
                                            }
                                        });

                                        if (allFiles != null) {
                                            for (File file : allFiles) {
                                                if (file.exists()) {
                                                    file.delete();
                                                    Log.d(TAG, "delete file:" + file.getName());
                                                }
                                            }
                                        }
                                        if(deleteCode < groupCode) {
                                            allFiles = dir.listFiles();
                                            if (allFiles == null
                                                    || allFiles.length == 0) {
                                                dir.delete();
                                                Log.d(TAG, "delete dir:" + dir.getName());
                                                bDir = false;
                                            }
                                        }
                                    }
                                }
                                if (!bDir) {
                                    type &= ~(1 << i);
                                }
                            }
                        }
                        if (type == 0) {
                            deleteCode++;
                            if (deleteCode > groupCode) {
                                deleteCode = groupCode;
                            }
                        }
                        deleteSuffixCode++;
                    }else {
                        break;
                    }
                }

                Log.d(TAG,"onDelete 2 frameSize:" + this.frameSize
                        + ",disk:"+ Utils.getDiskAvaiableSize(directory)
                        + ",deleteCode:" + deleteCode +",deleteSuffixCode:" + deleteSuffixCode);
            }
        }
    }

    private boolean isDelete(){
        return !Utils.isDiskAvaiable(directory, this.frameSize);
    }
    private  boolean isFull(){
        if(getSaveMode() == SAVE_MODE_FULL_STOP){
            return isDelete();
        }
        return false;
    }

    private void ms_sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private  boolean isStart(){
        LocalDateTime dt = LocalDateTime.now();
        return dt.isAfter(getStartDateTime());
    }
    private boolean isStop(){
        int s = getStopCondition();
        Log.d(TAG,"s:" + s + ",time:" + getStopDateTime() + ",stopframes:" + getStopConditionFrames());
        if(s == STOP_CONDITION_TIME){
            LocalDateTime dt = LocalDateTime.now();
            return dt.isAfter(getStopDateTime());
        }else if(s == STOP_CONDITION_FRAMES){
            return getFrames() >= getStopConditionFrames();
        }
        return false;
    }
    private static final int HOLD_TIME = 200;
    private synchronized boolean isHold(){
        long t = SystemClock.elapsedRealtime();
        return userInputTimestamp + HOLD_TIME <= t;
    }
    private synchronized boolean isInterval(){
        long t = SystemClock.elapsedRealtime();
        if(userInputTimestamp + HOLD_TIME <= t) {
            return saveTimestamp + frameInterval <= t;
        }
        return false;
    }

    private int getDelayTime(){
        int delay = 1;
        for(IChannel channel: channels){
            if(channel.isOpen()){
                boolean bMath = false;
                if(ChannelFactory.isMath_FFT_Ch(channel.getChId())){
                    delay = 5;
                    bMath = true;
                }else if(ChannelFactory.isMathCh(channel.getChId())){
                    delay = Math.max(delay,3);
                    bMath = true;
                }
                Measure measure = channel.getMeasure();
                if(measure.isEnable()){
                    delay = Math.max(delay,bMath ? 5 : 3);
                }
            }
        }
        return delay;
    }

    private void onSaveBefore(boolean bProgress){
        Log.d(TAG, "onSaveBefore() called :" + bProgress);

        if(null != autoSaveListener){
            autoSaveListener.onSaveBefore( bProgress);
        }
        if(!bProgress) {

            long m = 0;
            synchronized (this){
                m = SystemClock.elapsedRealtime() - triggerTimestamp;
            }
            long k = getDelayTime();
            SegmentSample segmentSample = SegmentSample.getInstance();
            if (segmentSample.isSegmentEnable()) {
                m = k * WAIT_TIME - m;
            }else{
                m = k * WAIT_TIME - m;
            }
            if(m > 0) {
                ms_sleep(m);
            }
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
        Log.d(TAG, "onSaveAfter() called bProgress:" + bProgress);
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

    @Override
    public void update(Observable o, Object arg) {
        EventBase eventBase = (EventBase) arg;
        if(eventBase.getId() == EventFactory.EVENT_SAMPLE_VALID){

            if(!isRun()){
                return;
            }

            boolean [] bData = (boolean[])eventBase.getData();
            int idx = 0;
            if(bData != null){
                idx = bData[1] ? 0 : 1;
            }
            SyncHeader header = SyncHeader.getInstance(idx);

            if(header.isValid()) {
                int v = atomicInteger.incrementAndGet();
//                Log.d(TAG,"v:" + v);
                if(v >= 2) {

                    if (!header.isSample()) {
                        if (header.isLastSample()) {
                            setTriggerState(true);
                        }else {
                            SegmentSample segmentSample = SegmentSample.getInstance();
                            if (segmentSample.isSegmentEnable()) {
//                                int frameNo = header.getFrameNo() - 1;
                                setFrameNo(getFrameNo() + 1);
                            }
                        }
                    }
                }
            }
        }
    }
}
