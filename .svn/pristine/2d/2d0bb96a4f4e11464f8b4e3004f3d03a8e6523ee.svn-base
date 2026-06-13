package com.micsig.tbook.tbookscope.top.layout.trigger;

/**
 * Created by yangj on 2018/6/19.
 */

public class TopMsgTriggerChannel {
    /**
     * TopLayoutTrigger.DETAIL_EDGE
     * TopLayoutTrigger.DETAIL_PULSEWIDTH
     * TopLayoutTrigger.DETAIL_NEDGE
     * TopLayoutTrigger.DETAIL_RUNT
     * TopLayoutTrigger.DETAIL_SLOPE
     * TopLayoutTrigger.DETAIL_TIMEOUT
     * TopLayoutTrigger.DETAIL_VIDEO
     */
    private int triggerType;
    /**
     * 0,1,2,3
     */
    private int chNumber;

    public TopMsgTriggerChannel(int triggerType, int chNumber) {
        this(triggerType,chNumber,false);
    }
    public TopMsgTriggerChannel(int triggerType, int chNumber,boolean isFromEventBus) {
        this.triggerType = triggerType;
        this.chNumber = chNumber;
        this.isFromEventBus = isFromEventBus;
    }

    public int getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(int triggerType) {
        this.triggerType = triggerType;
    }

    public int getChNumber() {
        return chNumber;
    }

    public void setChNumber(int chNumber) {
        this.chNumber = chNumber;
    }

    @Override
    public String toString() {
        return "TopMsgTriggerChannel{" +
                "triggerType=" + triggerType +
                ", chNumber=" + chNumber +
                '}';
    }

    private boolean isFromEventBus;

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }
}
