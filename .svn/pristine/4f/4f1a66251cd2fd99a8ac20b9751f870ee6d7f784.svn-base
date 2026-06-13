package com.micsig.tbook.tbookscope.main.mainright;

/**
 * Created by yangj on 2017/8/28.
 */

public class MainMsgTriggerLevel {
    /**
     * 该消息是否是来自于eventBus的发送
     */
    private boolean isFromEventBus = false;
    /**
     * 该消息一般情况下是同时发送给触发电平值、阈值电平值、触发源的修改，
     * 但是当移动通道时，不做触发源的修改
     */
    private boolean isOnlyModifyNumber = false;
    private String curLevel;
    private int curCh;

    public MainMsgTriggerLevel() {
    }

    public MainMsgTriggerLevel(MainMsgTriggerLevel msgTriggerLevel) {
        this.curLevel = msgTriggerLevel.curLevel;
        this.curCh = msgTriggerLevel.curCh;
        this.isOnlyModifyNumber = msgTriggerLevel.isOnlyModifyNumber;
        this.isFromEventBus = msgTriggerLevel.isFromEventBus;
    }

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public boolean isOnlyModifyNumber() {
        return isOnlyModifyNumber;
    }

    public void setOnlyModifyNumber(boolean onlyModifyNumber) {
        isOnlyModifyNumber = onlyModifyNumber;
    }

    public String getCurLevel() {
        return curLevel;
    }

    public void setCurLevel(String curLevel) {
        this.curLevel = curLevel;
    }

    public int getCurCh() {
        return curCh;
    }

    public void setCurCh(int curCh) {
        this.curCh = curCh;
    }

    @Override
    public String toString() {
        return "MainMsgTriggerLevel{" +
                "isFromEventBus=" + isFromEventBus +
                ", isOnlyModifyNumber=" + isOnlyModifyNumber +
                ", curLevel='" + curLevel + '\'' +
                ", curCh=" + curCh +
                '}';
    }
}
