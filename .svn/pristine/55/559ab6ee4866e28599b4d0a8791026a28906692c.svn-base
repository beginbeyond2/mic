package com.micsig.tbook.scope.Bus;

/**
 * Created by zhuzh on 2018-5-29.
 */

public class MILSTD1553BBus extends IBus {
    public static final int MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC = 1;
    public static final int MILSTD1553B_TRIGGER_DATA_WORD_SYNC = 2;
    public static final int MILSTD1553B_TRIGGER_COMMAND_STATUS_WORD = 3;
    public static final int MILSTD1553B_TRIGGER_DATA_WORD = 4;
    public static final int MILSTD1553B_TRIGGER_RT_ADDRESS = 5;
    public static final int MILSTD1553B_TRIGGER_ODD_PARITY_ERROR = 6;
    public static final int MILSTD1553B_TRIGGER_MANCHESTER_ERROR = 7;
    public static final int MILSTD1553B_TRIGGER_ALL_ERROR = 8;

    private int srcChIdx = 0;
    private int displayFormat = DISPLAY_HEX_DISPLAY;
    private int triggerType = MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC;
    private int cmdStatus = 0;
    private int data;
    private int addr;

    public MILSTD1553BBus(int busIdx) {
        super(busIdx,MILSTD1553B);
    }

    public int getSrcChIdx() {
        return srcChIdx;
    }

    public void setSrcChIdx(int srcChIdx) {
        this.srcChIdx = srcChIdx;
        chChange();
    }

    public int getDisplayFormat() {
        return displayFormat;
    }

    public void setDisplayFormat(int displayFormat) {
        this.displayFormat = displayFormat;
    }

    public int getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(int triggerType) {
        this.triggerType = triggerType;
        busChange();
    }

    public int getCmdStatus() {
        return cmdStatus;
    }

    public void setCmdStatus(int cmdStatus) {
        this.cmdStatus = cmdStatus;
        busChange();
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
        busChange();
    }

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
        busChange();
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
