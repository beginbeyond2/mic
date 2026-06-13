package com.micsig.tbook.scope.Bus;

/**
 * Created by zhuzh on 2018-5-29.
 */

public class LinBus extends IBus {
    public static final int LIN_TRIGGER_SYNC_RISING_EDGE = 0;
    public static final int LIN_TRIGGER_FRAME_ID = LIN_TRIGGER_SYNC_RISING_EDGE + 1;
    public static final int LIN_TRIGGER_ID_AND_DATA = LIN_TRIGGER_FRAME_ID + 1;
    public static final int LIN_TRIGGER_DATA = LIN_TRIGGER_ID_AND_DATA + 1;
    public static final int LIN_TRIGGER_PARITY_ERROR = LIN_TRIGGER_DATA + 1;
    public static final int LIN_TRIGGER_CHECKSUM_ERROR = LIN_TRIGGER_PARITY_ERROR + 1;
    public static final int LIN_TRIGGER_TYPE_MAX = LIN_TRIGGER_CHECKSUM_ERROR + 1;

    public static final int LIN_TYPE_1_3 = 0;
    public static final int LIN_TYPE_1_3_LONG = LIN_TYPE_1_3 + 1;
    public static final int LIN_TYPE_2_0 = LIN_TYPE_1_3_LONG + 1;
    private int srcChIdx = 0;
    private int idleLevel = IDLE_LEVEL_HIGH;
    private int baudRate = 9600;
    private int triggerType = LIN_TRIGGER_SYNC_RISING_EDGE;
    private int []frameIds = new int[LIN_TRIGGER_TYPE_MAX];

    private int linType = LIN_TYPE_1_3;
    private long []data = new long[LIN_TRIGGER_TYPE_MAX];
    private int dataLength = 16;//数据触发输入的有效位数

    public static boolean isTriggerTypeValid(int triggerType){
        return triggerType>=LIN_TRIGGER_SYNC_RISING_EDGE && triggerType<LIN_TRIGGER_TYPE_MAX;
    }
    public LinBus(int busIdx) {
        super(busIdx,LIN);
    }

    public void setLinType(int linType){
        this.linType = linType;
        busTypeChange();
    }
    public int getLinType(){
        return linType;
    }
    public void setSrcChIdx(int srcChIdx){
        this.srcChIdx = srcChIdx;
        chChange();
    }
    public int getSrcChIdx(){
        return srcChIdx;
    }
    public void setIdleLevel(int idleLevel){
        this.idleLevel = idleLevel;
        busChange();
    }
    public int getIdleLevel(){
        return idleLevel;
    }
    public void setBaudRate(int baudRate){
        this.baudRate = baudRate;
        busChange();
    }
    public int getBaudRate(){
        return baudRate;
    }

    public int getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(int triggerType) {
        if(isTriggerTypeValid(triggerType)) {
            this.triggerType = triggerType;
            busChange();
        }
    }

    public int getFrameId(int triggerType) {
        if(isTriggerTypeValid(triggerType))
            return frameIds[triggerType];
        return 0;
    }

    public void setFrameId(int triggerType,int frameId) {
        if(isTriggerTypeValid(triggerType)) {
            frameIds[triggerType] = frameId;
            busChange();
        }
    }

    public long getData() {
        return data[triggerType];
    }

    public void setData(long data) {
        this.data[triggerType] = data;
        busChange();
    }
    public void setData(int triggerType,int data){
        this.data[triggerType] = data;
        busChange();
    }
    public long getData(int triggerType){
        return data[triggerType];
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    @Override
    public int getChSampleCnt() {
        return 1;
    }

    @Override
    public boolean isChInSample(int chIdx) {
        return srcChIdx == chIdx;
    }
}
