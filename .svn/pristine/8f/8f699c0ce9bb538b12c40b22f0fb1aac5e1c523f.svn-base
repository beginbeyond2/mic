package com.micsig.tbook.scope.Bus;

/**
 * Created by zhuzh on 2018-5-29.
 */

public class I2CBus extends IBus {
    public static final int I2C_TRIGGER_START_CONDITION = 0;
    public static final int I2C_TRIGGER_STOP_CONDITION = 1;
    public static final int I2C_TRIGGER_ACK_LOST = 2;
    public static final int I2C_TRIGGER_ADDRESS_NO_ACK = 3;
    public static final int I2C_TRIGGER_RESTART = 4;
    public static final int I2C_TRIGGER_EEPROM_READ_DATA = 5;
    public static final int I2C_TRIGGER_FRAME1 = 6;
    public static final int I2C_TRIGGER_FRAME2 = 7;
    public static final int I2C_TRIGGER_WRITE_FRAME = 8;
    public static final int I2C_TRIGGER_TYPE_MAX = 9;
    private int sdaChIdx = 0;
    private int sclChIdx = 0;
    private int triggerType = I2C_TRIGGER_START_CONDITION;
    private int triggerRelation = TRIGGER_RELATION_EQUAL;
    private int []triggerAddrs = new int[I2C_TRIGGER_TYPE_MAX];
    private int []triggerData1s = new int[I2C_TRIGGER_TYPE_MAX];
    private int triggerData2 = 0;
    public static boolean isTriggerTypeVaild(int triggerType){
        return triggerType>= I2C_TRIGGER_START_CONDITION && triggerType<I2C_TRIGGER_TYPE_MAX;
    }
    public I2CBus(int busIdx) {
        super(busIdx,I2C);
    }

    public int getSdaChIdx() {
        return sdaChIdx;
    }

    public void setSdaChIdx(int sdaChIdx) {
        this.sdaChIdx = sdaChIdx;
        chChange();
    }

    public int getSclChIdx() {
        return sclChIdx;
    }

    public void setSclChIdx(int sclChIdx) {
        this.sclChIdx = sclChIdx;
        chChange();
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

    public int getTriggerRelation() {
        return triggerRelation;
    }

    public void setTriggerRelation(int triggerRelation) {
        this.triggerRelation = triggerRelation;
        busChange();
    }

    public int getTriggerData2() {
        return triggerData2;
    }

    public void setTriggerData2(int triggerData2) {
        this.triggerData2 = triggerData2;
        busChange();
    }
    public int getTriggerData1() {
        return triggerData1s[triggerType];
    }

    public void setTriggerData1(int triggerData1) {
        this.triggerData1s[triggerType] = triggerData1;
        busChange();
    }

    public int getTriggerData1(int triggerType) {
        if(isTriggerTypeVaild(triggerType))
            return triggerData1s[triggerType];
        return 0;
    }

    public void setTriggerData1(int triggerType,int triggerData1) {
        if(isTriggerTypeVaild(triggerType)) {
            this.triggerData1s[triggerType] = triggerData1;
            busChange();
        }
    }

    public int getTriggerAddr(int triggerType) {

        if(isTriggerTypeVaild(triggerType))
            return triggerAddrs[triggerType];
        return 0;
    }

    public void setTriggerAddrs(int triggerType,int triggerAddr) {

        if(isTriggerTypeVaild(triggerType)) {
            this.triggerAddrs[triggerType] = triggerAddr;
            busChange();
        }
    }
    @Override
    public int getChSampleCnt() {
        return 2;
    }

    @Override
    public boolean isChInSample(int chIdx) {
        return sdaChIdx == chIdx || sclChIdx == chIdx;
    }
}
