package com.micsig.tbook.tbookscope.middleware.command;


import android.os.Build;
import android.os.Environment;
import android.os.SharedMemory;
import android.util.Log;

import com.micsig.tbook.scope.Data.AutoSave;
import com.micsig.tbook.scope.Data.SingleSaveData;
import com.micsig.tbook.scope.Data.Waveform;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.SCPICommandDeal;
import com.micsig.tbook.tbookscope.scpi.SCPIParam;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Storage {
//     new SCPICommandStruct(":STORage:SAVE","SCPI_Storage","Save"),//存储指定通道的波形到指定位置
//            new SCPICommandStruct(":STORage:LOAD","SCPI_Storage","Load"),//载入ref
//            new SCPICommandStruct(":STORage:CAPTure","SCPI_Storage","Capture"),//屏幕截图
//            new SCPICommandStruct(":STORage:DEPTh","SCPI_Storage","Depth"),//设置示波器存储深度
//            new SCPICommandStruct(":STORage:DEPTh?","SCPI_Storage","DepthQ"),//查询示波器存储深度
//            new SCPICommandStruct(":STORage:CONSave","SCPI_Storage","ConSave"),//存储示波器设置
//            new SCPICommandStruct(":STORage:CONLoad","SCPI_Storage","ConLoad"),//调用示波器设置
//            new SCPICommandStruct(":STORage:RECord","SCPI_Storage","Record"),//设置示波器录制功能的打开与关闭
//            new SCPICommandStruct(":STORage:RECord?","SCPI_Storage","RecordQ"),//查询示波器录制功能的打开与关闭
//            new SCPICommandStruct(":STORage:PLAY","SCPI_Storage","Play"),//设置示波器回放功能的打开和关闭
//            new SCPICommandStruct(":STORage:PLAY?","SCPI_Storage","PlayQ"),//查询示波器回放功能的打开和关闭
//            new SCPICommandStruct(":STORage:PLAY:SPEed","SCPI_Storage","Play_Speed"),//设置示波器回放快进选项
//            new SCPICommandStruct(":STORage:PLAY:SPEed?","SCPI_Storage","Play_SpeedQ"),//查询示波器回放快进选项
//            new SCPICommandStruct(":STORage:PLAY:BACK","SCPI_Storage","Play_Back"),//设置示波器回放后退选项
//            new SCPICommandStruct(":STORage:PLAY:BACK?","SCPI_Storage","Play_backQ"),//查询示波器回放后退选项
//            new SCPICommandStruct(":STORage:SAVE:SOURce", "SCPI_Storage","Save_Source"),
//            new SCPICommandStruct(":STORage:SAVE:SOURce?", "SCPI_Storage","Save_SourceQ"),
//            new SCPICommandStruct(":STORage:SAVE:LOCAtion", "SCPI_Storage","Save_Location"),
//            new SCPICommandStruct(":STORage:SAVE:LOCAtion?", "SCPI_Storage","Save_LocationQ"),
//            new SCPICommandStruct(":STORage:SAVE:TYPE", "SCPI_Storage","Save_Type"),
//            new SCPICommandStruct(":STORage:SAVE:TYPE?", "SCPI_Storage","Save_TypeQ"),
//            new SCPICommandStruct(":STORage:SAVE:FILename","SCPI_Storage","Save_Filename"),
//            new SCPICommandStruct(":STORage:SAVE:FILename?", "SCPI_Storage","Save_FilenameQ"),
//            new SCPICommandStruct(":STORage:SAVE:START", "SCPI_Storage","Save_Start"),

    private int depth;
    /**
     * const char * stor_vedioState[] = {
     * "RECOrd",
     * "STOP",
     * NULL
     * };
     */
    private int record;
    private int play;
    private int play_Speed;
    private int play_back;
    private int ch;
    private int location;
    private int saveType;
    private String fileName;
    private boolean isAllSegments;
    private boolean isTime;
    private boolean isIncolor;
    private boolean isThumbnail;

    /**
     * 存储指定通道的波形到指定位置
     *
     * @param channel {CH1|CH2|CH3|CH4|MATH}
     * @param type    {LOCal|UDISk}
     */
    public void Save(int channel, int type, boolean isUpdateUI) {
        this.ch = channel;
        this.location = type;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE);
            String params = String.valueOf(channel) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type);
            msgToUI.setParam(params);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 载入ref
     *
     * @param source {REF1| REF 2| REF 3| REF4 }
     */
    //private String[] list={ "CH1","CH2","CH3","CH4","MATH","REF1","REF2","REF3","REF4","OFF"};
    private String[] list = {"R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8"};

    public void Load(int source, String fileName, boolean isOpen, boolean isUpdateUI) {
        if (isUpdateUI) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (Arrays.stream(list).filter(s->s.equals(ToolsSCPI.getChAll(source).trim())).findAny().isPresent() ){
                    CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                    msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_LOAD);
                    String params = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT +
                            String.valueOf(fileName.trim())+ CommandMsgToUI.PARAM_SPLIT+
                            String.valueOf(isOpen);
                    msgToUI.setParam(params);
                    RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
                }
            }
        }
    }

    /**
     * 屏幕截图
     */
    public void Capture(boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }


    public  void Capture_Time(boolean isTime,boolean isUpdateUI) {
        this.isTime=isTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE_TIME);
            msgToUI.setParam(String.valueOf(isTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  boolean Capture_TimeQ() {
        return isTime;
    }
    public  void Capture_Incolor(boolean isIncolor,boolean isUpdateUI) {
        this.isIncolor=isIncolor;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE_INCOLOR);
            msgToUI.setParam(String.valueOf(isIncolor));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  boolean Capture_IncolorQ() {
        return isIncolor;
    }

    public void Capture_Thumbnail(boolean isThumbnail, boolean isUpdateUI) {
        this.isThumbnail = isThumbnail;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE_THUMBNAIL);
            msgToUI.setParam(String.valueOf(isThumbnail));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean Capture_ThumbnailQ() {
        return isThumbnail;
    }

    public  void Capture_Start(boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }


    /**
     * 设置示波器存储深度
     */
    public void Depth(int depth, boolean isUpdateUI) {
        if (this.depth == depth) return;
        this.depth = depth;
        if (isUpdateUI) {
        }
    }

    /**
     * 查询示波器存储深度
     */
    public int DepthQ() {
        return depth;
    }

    /**
     * 存储示波器设置
     */
    public void ConSave(String fileName, boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CONSAVE);
            msgToUI.setParam(fileName.trim());
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public void ConSave_start( boolean isUpdateUI){
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CONSAVE_START);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    /**
     * 调用示波器设置
     */
    public void ConLoad(String fileName, boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CONLOAD);
            msgToUI.setParam(fileName);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置示波器录制功能的打开与关闭
     */
    public void Record(int record, boolean isUpdateUI) {
        if (this.record == record) return;
        this.record = record;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_RECORD);
            msgToUI.setParam(String.valueOf(record));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询示波器录制功能的打开与关闭
     */
    public int RecordQ() {
        return record;
    }

    /**
     * 设置示波器回放功能的打开和关闭
     */
    public void Play(int index, boolean isUpdateUI) {
    }

    /**
     * 查询示波器回放功能的打开和关闭
     */
    public int PlayQ() {
        return 0;
    }

    /**
     * 设置示波器回放快进选项
     */
    public void Play_Speed(int index, boolean isUpdateUI) {
    }

    /**
     * 查询示波器回放快进选项
     */
    public int Play_SpeedQ() {
        return 0;
    }

    /**
     * 设置示波器回放后退选项
     */
    public void Play_Back(int index, boolean isUpdateUI) {
    }

    /**
     * 查询示波器回放后退选项
     */
    public int Play_backQ() {
        return 0;
    }

    public void Save_Source(int ch, boolean isUpdateUI) {
        this.ch = ch;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_SOURCE);
            msgToUI.setParam(String.valueOf(ch));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int Save_SourceQ() {
        return this.ch;
    }

    public void Save_Location(int location, boolean isUpdateUI) {
        this.location=location;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_LOCATION);
            msgToUI.setParam(String.valueOf(location));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int Save_LocationQ() {
        return location;
    }

    public void Save_Type(int saveType, boolean isUpdateUI) {
        this.saveType=saveType;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_TYPE);
            msgToUI.setParam(String.valueOf(saveType));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int Save_TypeQ() {
        return saveType;
    }

    public void Save_Filename(String fileName, boolean isUpdateUI) {
        this.fileName=fileName.trim();
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_FILENAME);
            msgToUI.setParam(String.valueOf(fileName.trim()));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public String Save_FilenameQ() {
        return fileName;
    }

    public void Save_Start() {
        CommandMsgToUI msgToUI = Command.get().getMsgToUI();
        msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_START);
        msgToUI.setParam(String.valueOf(record));
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
    }

    public  void Save_ALLSegments(boolean isOpen,boolean isUpdateUI){
        this.isAllSegments=isOpen;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_ALLSEGMENTS);
            msgToUI.setParam(String.valueOf(isOpen));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public  boolean Save_ALLSegmentsQ(){
        return isAllSegments;
    }

    SingleSaveData.ISaveDataListener saveDataListener = new SingleSaveData.ISaveDataListener() {
        private ScreenControls screenControls = null;
        private int lockFlag = 0;

        private AtomicBoolean atomicInteger = new AtomicBoolean(false);
        @Override
        public void onBegin() {
            if(screenControls == null){
                screenControls = ScreenControls.getInstance();
            }
            screenControls.onUI(()->{
                Scope scope = Scope.getInstance();
                boolean b = scope.isRun();
                if(b){
                    Command.get().getFunctionMenu().Stop(true);
                }
                atomicInteger.set(b);
            });

        }

        @Override
        public void onEnd() {
            if(screenControls.isLockScreen(lockFlag)) {
                screenControls.unLockScreen(lockFlag);
            }
            lockFlag = 0;
            screenControls.onUI(()->{
                if(atomicInteger.get()){
                    Command.get().getFunctionMenu().Run(true);
                }
                atomicInteger.set(false);
            });
        }

        @Override
        public void onSaveBefore(boolean bProgress) {
            lockFlag = ScreenControls.LOCK_SCREEN;
            if(bProgress){
                lockFlag |= ScreenControls.LOCK_PROGRESS;
            }
            if(!screenControls.isLockScreen(lockFlag)) {
                screenControls.lockScreen(lockFlag);
            }
        }

        @Override
        public void onProgress(int val) {
            screenControls.onUI(()-> {
                screenControls.setProgressValue(val);
            });
        }

        @Override
        public void onSaveAfter(boolean bProgress) {
            int flag = lockFlag;
            if(bProgress){
                flag = ScreenControls.LOCK_PROGRESS;
                lockFlag &= ~flag;
            }
            if(screenControls.isLockScreen(flag)) {
                screenControls.unLockScreen(flag);
            }
        }

        @Override
        public void onPicture(String filePath, String fileName) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_AUTO_SAVE,filePath);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_AUTO_SAVE_NAME,fileName);
            RxBus.getInstance().post(RxEnum.MSG_AUTO_SAVE_INVOKE_SCREENSHOT,true);
        }

        @Override
        public HashMap<String, HashMap<String, String>> onCurCache() {
            HashMap<String, HashMap<String, String>> map = new HashMap<>();
            map.put(CacheUtil.DefaultSaveName, CacheUtil.get().getCurrMap());
            map.put(CacheUtil.OtherDefaultSaveName, CacheUtil.get().getCurrOtherMap());
            return map;
        }
    };
    public boolean Save_DataType(String [] strArray){
        SingleSaveData singleSaveData = SingleSaveData.getInstance();
        boolean b = false;
        if(!singleSaveData.isRun()) {
            singleSaveData.setAutoSaveListener(saveDataListener);
            int type = 0;
            for(String s:strArray){
                if(s.equalsIgnoreCase("csv")){
                    type |= SingleSaveData.AUTO_SAVE_CSV;
                }else if(s.equalsIgnoreCase("png")){
                    type |= SingleSaveData.AUTO_SAVE_IMAGE;
                }else if(s.equalsIgnoreCase("mss")){
                    type |= SingleSaveData.AUTO_SAVE_SESSION;
                }
            }
            if(type != 0) {
                singleSaveData.setSaveType(type);
                String path = Tools.getSaveDataPath();
                singleSaveData.setFilePath(path);
                singleSaveData.start();
                b = true;
            }
        }
        return b;
    }

    public boolean Save_DataStatusQ(){
        SingleSaveData singleSaveData = SingleSaveData.getInstance();
        return !singleSaveData.isRun();
    }


    public String Save_DataCSVQ(){
        SharedMemory shm = SCPICommandDeal.getInstance().getSharedMem();
        SingleSaveData.getInstance().writeCsvToShm(shm);
        return "";
    }

    public String Save_DataPNGQ(){
        SharedMemory shm = SCPICommandDeal.getInstance().getSharedMem();
        SingleSaveData.getInstance().writePngToShm(shm);
        return "";
    }

    public String Save_DataMSSQ(){
        SharedMemory shm = SCPICommandDeal.getInstance().getSharedMem();
        SingleSaveData.getInstance().writeMssToShm(shm);
        return "";
    }
}
