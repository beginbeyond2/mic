package com.micsig.tbook.scope.Bus;

/**
 * Created by zhuzh on 2018-5-29.
 */

public class CanBus extends IBus {
    //trigger Type
    public static final int CAN_TRIGGER_FRAME_START = 0;
    public static final int CAN_TRIGGER_REMOTE_FRAME_ID = 1;
    public static final int CAN_TRIGGER_DATA_FRAME_ID = 2;
    public static final int CAN_TRIGGER_REMOTE_DATA_ID = 3;
    public static final int CAN_TRIGGER_ID_AND_DATA = 4;
    public static final int CAN_TRIGGER_WRONG_FRAME = 5;
    public static final int CAN_TRIGGER_ALL_ERROR = 6;
    public static final int CAN_TRIGGER_ACK_ERROR = 7;
    public static final int CAN_TRIGGER_OVERLOAD_FRAME = 8;
    public static final int CAN_TRIGGER_TYPE_MAX = 9;
    //signal
    public static final int CAN_H = 0;
    public static final int CAN_L = 1;
    public static final int CAN_H_L = 2;
    public static final int CAN_L_H = 3;
    public static final int CAN_Rx = 4;
    public static final int CAN_Tx = 5;
    private int srcChIdx = 0;
    private int signal = CAN_H;
    private int baudRate = 100*1000;
    private int FDBandRate = 2*1000*100;
    private double samplePlace1 = 0.65;
    private double samplePlace2 = 0.65;
    private int triggerType = CAN_TRIGGER_FRAME_START;
    private int []frameIds = new int[CAN_TRIGGER_TYPE_MAX];
    private int dlc = 0;
    private long data = 0;
    private boolean bCanFDEnable = false;
    private boolean bISO = true;

    public boolean isISO() {
        return bISO;
    }

    public void setISO(boolean bISO) {
        this.bISO = bISO;
        busChange();
    }

    public boolean isCanFDEnable() {
        return bCanFDEnable;
    }

    public void setCanFDEnable(boolean bCanFDEnable) {
        this.bCanFDEnable = bCanFDEnable;
        busTypeChange();
    }

    public int getFDBandRate(){
        return FDBandRate;
    }
    public void setFDBandRate(int bandRate){
        FDBandRate = bandRate;
        setCanFDEnable(FDBandRate > 1);
        busChange();
    }

    public static boolean isTriggerTypeVaild(int triggerType){
        return triggerType>=CAN_TRIGGER_FRAME_START && triggerType<CAN_TRIGGER_TYPE_MAX;
    }
    public CanBus(int busIdx) {
        super(busIdx,CAN);
    }
    public void setSrcChIdx(int srcChIdx){
        this.srcChIdx = srcChIdx;
        chChange();
    }
    public int getSrcChIdx(){
        return srcChIdx;
    }
    public void setSignal(int signal){
        this.signal = signal;
        busChange();
    }
    public int getSignal(){
        return signal;
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
        if(isTriggerTypeVaild(triggerType)) {
            this.triggerType = triggerType;
            busChange();
        }
    }
    public void setSamplePlace1(double samplePlace){
        this.samplePlace1 = samplePlace;
        busChange();
    }

    public double getSamplePlace1(){
        return samplePlace1;
    }

    public void setSamplePlace2(double samplePlace){
        this.samplePlace2 = samplePlace;
        busChange();
    }

    public double getSamplePlace2(){
        return samplePlace2;
    }
    public int getDlc() {
        return dlc;
    }

    public void setDlc(int dlc) {
        this.dlc = dlc;
        busChange();
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
        busChange();
    }

    public int getFrameId(int triggerType) {
        if(isTriggerTypeVaild(triggerType)){
            return frameIds[triggerType];
        }
        return 0;
    }

    public void setFrameId(int triggerType,int frameId) {
        if(isTriggerTypeVaild(triggerType)){
            frameIds[triggerType] = frameId;
            busChange();
        }
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
