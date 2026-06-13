package com.micsig.tbook.tbookscope.main.mainbottom;

/**
 * Created by yangj on 2017/5/25.
 */

public class MainBottomMsgTimeBase {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FFT = 1;
    public static final int TYPE_REF = 2;

    private boolean isFromEventBus;
    private int type;
    private String timeBase;

    public MainBottomMsgTimeBase() {
        timeBase = "";
        type = TYPE_NORMAL;
    }

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(String timeBase) {
        this.timeBase = timeBase;
    }

    @Override
    public String toString() {
        return "MainBottomMsgTimeBase{" +
                "type=" + type +
                ", timeBase='" + timeBase + '\'' +
                '}';
    }
}
