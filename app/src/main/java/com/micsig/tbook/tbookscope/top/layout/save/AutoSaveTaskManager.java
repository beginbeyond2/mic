package com.micsig.tbook.tbookscope.top.layout.save;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.micsig.base.Utils;
import com.micsig.tbook.scope.Data.SaveRecoverySession;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.FileUtils;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.ui.wavezone.TChan;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AutoSaveTaskManager {

    private static volatile AutoSaveTaskManager instance;
    private AutoSaveTaskCondition autoSaveTaskCondition;

    private ScheduledExecutorService scheduler;

    private CountDownLatch latch;
    private TaskSuffixNumModel taskSuffixNumModel;

    public AutoSaveTaskManager(AutoSaveTaskCondition autoSaveTaskCondition, TaskSuffixNumModel txtSuffixNum) {
        this.autoSaveTaskCondition = autoSaveTaskCondition;
        this.taskSuffixNumModel = txtSuffixNum;
    }
     public  static AutoSaveTaskManager createOrUpdate(AutoSaveTaskCondition autoSaveTaskCondition, TaskSuffixNumModel txtSuffixNum){
        if(instance==null){
            synchronized (AutoSaveTaskManager.class){
                if(instance==null){
                    instance = new AutoSaveTaskManager(autoSaveTaskCondition,txtSuffixNum);
                }
            }
        }
        instance.updateCondition(autoSaveTaskCondition);
        return instance;
     }

     public static AutoSaveTaskManager getInstance(){
        return instance;
     }

     public void updateCondition(AutoSaveTaskCondition autoSaveTaskCondition){
        instance.autoSaveTaskCondition = autoSaveTaskCondition;
     }

    String originSuffixNum;

    private volatile boolean started = false;


    public void start() {
        if (this.scheduler != null && !this.scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newScheduledThreadPool(1);
        AtomicInteger batchCounter = new AtomicInteger(1);
        started = true;
        originSuffixNum = autoSaveTaskCondition.getSuffixNum();
        long intervalTime = autoSaveTaskCondition.getIntervalTime().getTime();
        List<AutoSaveTaskCondition.SaveType> saveTypes = autoSaveTaskCondition.getSaveType();
        int chanNum = autoSaveTaskCondition.getSelectedChannel().size();
        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,false);
        int totalTasks = 0;
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.CSV)) {
            totalTasks += 1;
        }
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.WAV)) {
            totalTasks += chanNum;
        }
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.BIN)) {
            totalTasks += chanNum;
        }
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.PICTURE)) {
            totalTasks += 1;
        }
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.SESSION)) {
            totalTasks += 1;
        }
        int finalTotalTasks = totalTasks;
        scheduler.scheduleWithFixedDelay(() -> {
            boolean userTouch = CacheUtil.get().getBoolean(CacheUtil.USER_TOUCH);
            Log.d("TAG", "start: "+userTouch);
            if(userTouch){
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            if(now.isBefore(autoSaveTaskCondition.getStartTime())){
                return;
            }
            ScreenControls screenControls = ScreenControls.getInstance();
            screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);
            if (!started) return;

            File taskDir = new File(autoSaveTaskCondition.getSavePath() +"/"+ autoSaveTaskCondition.getSaveFileName());
            taskDir.mkdirs();
            latch = new CountDownLatch(finalTotalTasks);
            if (saveTypes.contains(AutoSaveTaskCondition.SaveType.CSV)) {
                File csvDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString()
                        .replace("-","_")
                        .replace(":","")+"_csv") ;
                csvDir.mkdirs();
                doSaveWaveCSV(csvDir.getPath());
            }
            if (saveTypes.contains(AutoSaveTaskCondition.SaveType.WAV)) {
                File wavDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString()
                        .replace("-","_")
                        .replace(":","")+"_wav");
                wavDir.mkdirs();
                doSaveWaveWav(wavDir.getPath());
            }
            if (saveTypes.contains(AutoSaveTaskCondition.SaveType.BIN)) {
                File binDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString()
                        .replace("-","_")
                        .replace(":","")+"_bin");
                binDir.mkdirs();
                doSaveWaveBin(binDir.getPath());
            }
            if (saveTypes.contains(AutoSaveTaskCondition.SaveType.SESSION)) {
                File sessionDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString()
                        .replace("-","_")
                        .replace(":","")+"_session" );
                sessionDir.mkdirs();
                doSaveSession(sessionDir.getPath());
            }
            if(saveTypes.contains(AutoSaveTaskCondition.SaveType.PICTURE)){
                File pictureDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString()
                        .replace("-","_")
                        .replace(":","")+"_picture" );
                pictureDir.mkdirs();
                doSavePicture(pictureDir.getPath());
            }
            try {
                latch.await();
                autoAddSuffixNum();
                StopCondition stopCondition = autoSaveTaskCondition.getStopCondition();
                RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_STOP_BUTTON_STATE,true);
                if(stopCondition.getType().equals(StopCondition.StopConditionType.TIME)){
                    if(now.isAfter(LocalDateTime.parse(stopCondition.getValue()))){
                        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true);
                        stop();
                    }
                }else if(stopCondition.getType().equals(StopCondition.StopConditionType.AFTER_N_FRAME)){
                    if(batchCounter.get()==Integer.parseInt(stopCondition.getValue())){
                        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true);
                        stop();
                    }
                }
                if(autoSaveTaskCondition.getSaveMode().equals(AutoSaveTaskCondition.SaveMode.FULL_WHEN_STOP)){
                    if(noEnoughSpace()){
                        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true);
                        stop();
                    }
                }
                batchCounter.getAndIncrement();
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }, 0, intervalTime, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        started = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    this.scheduler.shutdown();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        taskSuffixNumModel.updateText("0000000");
        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true);
        scheduler = null;
    }

    private void doSaveWaveWav(String filePath) {
        String input = autoSaveTaskCondition.getSaveFileName();
        List<Integer> selectList = autoSaveTaskCondition.getSelectedChannel();//保存CSV时选中的channel
        ScreenControls screenControls = ScreenControls.getInstance();
        screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);

        for (int i = 0; i < selectList.size(); i++) {
            final int ch = selectList.get(i);
            String finalInput = input + "_" + originSuffixNum + "_" + getChName(ch);
            Log.d("SaveWav", "doSaveWaveWav: " + ch);
            SaveManage.getInstance().allSaveEntrance(selectList.get(i), 0, filePath, finalInput, null, new SaveManage.SaveCallBack() {
                @Override
                public void onResult(boolean success, String msg) {
                    if (success) {
                        SaveManage.getInstance().putCacheName(finalInput);
                        FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                    } else {
                        FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                    }
                    latch.countDown();
                    if(noEnoughSpace()){
                        File wavDir = new File(filePath);
                        deleteOldstFile(wavDir);
                    }
                }
            });
            Command.get().getStorage().Save_Filename(finalInput, false);
        }
    }

    private void doSaveWaveBin(String filePath) {
        String input = autoSaveTaskCondition.getSaveFileName();
        List<Integer> selectList = autoSaveTaskCondition.getSelectedChannel();//保存CSV时选中的channel
        ScreenControls screenControls = ScreenControls.getInstance();
        screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);

        for (int i = 0; i < selectList.size(); i++) {
            final int ch = selectList.get(i);
            Log.d("SaveWav", "doSaveWaveWav: " + ch);

            if(ch>=TChan.Ch8){
                latch.countDown();
                continue;
            }
            String finalInput = input+"_" + originSuffixNum +"_" + getChName(ch);
            Log.d("SaveWav", "doSaveWaveWav: " + finalInput);
            SaveManage.getInstance().allSaveEntrance(selectList.get(i), 2, filePath, finalInput, null, new SaveManage.SaveCallBack() {
                @Override
                public void onResult(boolean success, String msg) {
                    if (success) {
                        SaveManage.getInstance().putCacheName(finalInput);
                        FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                    } else {
                        FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                    }
                    latch.countDown();
                    if(noEnoughSpace()){
                        File wavDir = new File(filePath);
                        deleteOldstFile(wavDir);
                    }
                }
            });
            Command.get().getStorage().Save_Filename(finalInput, false);
        }
    }


    private void doSaveWaveCSV(String filePath) {
        String finalInput = autoSaveTaskCondition.getSaveFileName() + "_" + originSuffixNum;
        List<Integer> selectList = autoSaveTaskCondition.getSelectedChannel();//保存CSV时选中的channel

        ScreenControls screenControls = ScreenControls.getInstance();
        screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);

        SaveManage.getInstance().allSaveEntrance(0, 1, filePath, finalInput, selectList, new SaveManage.SaveCallBack() {
            @Override
            public void onResult(boolean success, String msg) {
                if (success) {
                    SaveManage.getInstance().putCacheName(finalInput);
                    FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                } else {
                    FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
                }
                latch.countDown();
                if(noEnoughSpace()){
                    File wavDir = new File(filePath);
                    deleteOldstFile(wavDir);
                }
            }
        });
    }


    private void doSavePicture(String filePath) {
        String finalInput = autoSaveTaskCondition.getSaveFileName() + "_" + originSuffixNum;
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_AUTO_SAVE,filePath);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_AUTO_SAVE_NAME,finalInput);
        RxBus.getInstance().post(RxEnum.MSG_AUTO_SAVE_INVOKE_SCREENSHOT,true);
        latch.countDown();
    }


    private void autoAddSuffixNum() {//文件名序号递增
        int oldSuffixNum = Integer.parseInt(originSuffixNum.trim());
        String tempNum = KeyBoardNumberUtil.toBits((oldSuffixNum + 1) + "", 7);
        originSuffixNum = tempNum;
        taskSuffixNumModel.updateText(tempNum);
    }


    private void doSaveSession(String filePath) {
        boolean selectIsFast32 = false;

        Scope scope = Scope.getInstance();
        boolean oldIsRun = scope.isRun();
        if (scope.isRun()) {
            Command.get().getFunctionMenu().Stop(true);
        } else {
            Command.get().getSample().SegmentedStop(true);
        }
        int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
        CacheUtil.get().putMapInForce(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + "-1", String.valueOf(channelSelect));
        AtomicReference<String> toastStr = new AtomicReference<>("");

        new Thread(() -> {
            ms_sleep(1000);
            SaveRecoverySession saveRecoverySession = SaveRecoverySession.getInstance();
            long needSize = saveRecoverySession.estimateStorage();
            boolean canSave = Utils.isDiskAvaiable(new File(filePath), needSize);
            if (selectIsFast32) {
                if (needSize >= 4 * 1024 * 1024 * 1024L) {
                    noEnoughSpace(true, scope, oldIsRun, filePath);
                    return;
                }
            }
            if (!canSave) {
                noEnoughSpace(false, scope, oldIsRun, filePath);
                return;
            }
            HashMap<String, HashMap<String, String>> map = new HashMap<>();
            map.put(CacheUtil.DefaultSaveName, CacheUtil.get().getCurrMap());
            map.put(CacheUtil.OtherDefaultSaveName, CacheUtil.get().getCurrOtherMap());
            saveRecoverySession.store(map, filePath+"/"+autoSaveTaskCondition.getSaveFileName()+"_"+originSuffixNum+".mss");
            while (!saveRecoverySession.isDone()) {
                ms_sleep(100);
                Log.d("SaveRecoverySession", "store progress:" + saveRecoverySession.getSaveRecoveryProgress());
                showSaveProgress(saveRecoverySession.getSaveRecoveryProgress());
            }
//            toastStr = context.getResources().getString(R.string.top_slip_save_session_success);
            boolean saveSuccess = true;
            if (saveRecoverySession.getStatus() == SaveRecoverySession.S_FAIL) {
                saveSuccess = false;
                showSaveProgress(100);
            }
            boolean finalSaveSuccess = saveSuccess;
            boolean finalOldState = oldIsRun;

            Log.d("SaveRecoverySession", toastStr.get());
            ScreenControls screenControls = ScreenControls.getInstance();
            if (screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);
            }
            if (scope.isRun() != finalOldState) {
                //scope.setRun(finalOldState);
                if (finalOldState) {
                    Command.get().getFunctionMenu().Run(true);
                } else {
                    Command.get().getFunctionMenu().Stop(true);
                }
            }
            if (finalSaveSuccess) { //保存成功
                FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
            } else { //保存失败
                FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
            }
        }).start();
//        DToast.get().show(toastStr.get());
        latch.countDown();
        if(noEnoughSpace()){
            File wavDir = new File(filePath);
            deleteOldstFile(wavDir);
        }
    }

    private void showSaveProgress(int progress) {

        ScreenControls screenControls = ScreenControls.getInstance();
        if (progress < 0 || progress >= 100) {
            if (screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);
            }
        } else {
            if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {
                screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);
            }
            screenControls.setProgressValue(progress);
        }
        ;
    }

    private void ms_sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void recoveryScopeState(Scope scope, boolean finalOldState) {
        if (scope.isRun() == finalOldState) return;
        if (finalOldState) {
            Command.get().getFunctionMenu().Run(true);
        } else {
            Command.get().getFunctionMenu().Stop(true);
        }
    }

    private void noEnoughSpace(boolean isCausedByFast32, Scope scope, boolean oldIsRun, String filePath) {
        recoveryScopeState(scope, oldIsRun);
        FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX);
    }

    public boolean noEnoughSpace(){
        File saveDir = new File(autoSaveTaskCondition.getSavePath());
        long freeSpace = saveDir.getFreeSpace();
//        Log.d("TAG", "noEnoughSpace: "+freeSpace);
        long estimatedSize = 10 * 1024 * 1024L *1024;
        return freeSpace < estimatedSize;
    }
    private boolean deleteOldstFile(File storageDir){
        File[] files = storageDir.listFiles();
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        return files[0].delete();
    }

    private String getChName(int ch){
        ch = ch+1;
        if(ch >= TChan.Ch1 && ch<=TChan.Ch8){
            return "CH" + ch;
        }
        else if(ch>= TChan.Math1 && ch<= TChan.Math8){
            return "Math"+ (ch-8);
        }else if(ch>=TChan.R1 && ch<= TChan.R8){
            return "Ref" +(ch-16);
        }
        return "";
    }
}



