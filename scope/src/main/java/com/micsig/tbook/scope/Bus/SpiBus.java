package com.micsig.tbook.scope.Bus;

/**
 * Created by zhuzh on 2018-5-29.
 */

public class SpiBus extends IBus {
    //trigger type
    public static final int SPI_TRIGGER_FRAME_CS = 1;
    public static final int SPI_TRIGGER_FRAME_DATA = 2;
    public static final int SPI_TRIGGER_FRAME_X_DATA = 3;
    //clk sample edge
    public static final int SPI_CLK_RISE_EDGE = 0;
    public static final int SPI_CLK_FALL_EDGE = 1;
    private int clkChIdx = 0;
    private int dataChIdx = 1;
    private int csChIdx = 2;
    private int bits = 8;
    private int clkSample = SPI_CLK_RISE_EDGE;
    private int dataIdleLevel = IDLE_LEVEL_HIGH;
    private boolean csValid = false;
    private int csIdleLevel = IDLE_LEVEL_HIGH;
    private int triggerType = SPI_TRIGGER_FRAME_CS;
    private int triggerMask = 0;
    private int triggerData = 0;

    public SpiBus(int busIdx) {
        super(busIdx,SPI);
    }

    public int getClkChIdx() {
        return clkChIdx;
    }

    public void setClkChIdx(int clkChIdx) {
        this.clkChIdx = clkChIdx;
        chChange();
    }

    public int getDataChIdx() {
        return dataChIdx;
    }

    public void setDataChIdx(int dataChIdx) {
        this.dataChIdx = dataChIdx;
        chChange();
    }

    public int getCsChIdx() {
        return csChIdx;
    }

    public void setCsChIdx(int csChIdx) {
        this.csChIdx = csChIdx;
        chChange();
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
        busChange();
    }

    public int getClkSample() {
        return clkSample;
    }

    public void setClkSample(int clkSample) {
        this.clkSample = clkSample;
        busChange();
    }

    public int getDataIdleLevel() {
        return dataIdleLevel;
    }

    public void setDataIdleLevel(int dataIdleLevel) {
        this.dataIdleLevel = dataIdleLevel;
        busChange();
    }

    public boolean isCsValid() {
        return csValid;
    }

    public void setCsValid(boolean csValid) {
        this.csValid = csValid;
        chChange();
    }

    public int getCsIdleLevel() {
        return csIdleLevel;
    }

    public void setCsIdleLevel(int csIdleLevel) {
        this.csIdleLevel = csIdleLevel;
        busChange();
    }

    public int getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(int triggerType) {
        this.triggerType = triggerType;
        busChange();
    }

    public int getTriggerMask() {
        return triggerMask;
    }

    public void setTriggerMask(int triggerMask) {
        this.triggerMask = triggerMask;
        busChange();
    }

    public int getTriggerData() {
        return triggerData;
    }

    public void setTriggerData(int triggerData) {
        this.triggerData = triggerData;
        busChange();
    }

    @Override
    public int getChSampleCnt() {
        if(csValid)
            return 3;
        return 2;
    }

    @Override
    public boolean isChInSample(int chIdx) {
        boolean bSample = (clkChIdx == chIdx) || (dataChIdx == chIdx);
        if(csValid) {
            bSample = (bSample) || (csChIdx == chIdx);
        }
        return bSample;
    }
}
