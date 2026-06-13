package com.micsig.tbook.tbookscope.main;

/**
 * Created by yangj on 2018/8/15.
 */

public class ExternalKeysMsgCursor {
    public static final int TYPE_CHANGE = 0;//改变一次
    public static final int TYPE_OPEN = 1;//状态改为打开
    public static final int TYPE_CLOSE = 2;//状态改为关闭

    private boolean isHor;//是水平光标么
    private int type;

    public ExternalKeysMsgCursor(boolean isHor, int type) {
        this.isHor = isHor;
        this.type = type;
    }

    public boolean isHor() {
        return isHor;
    }

    public void setHor(boolean hor) {
        isHor = hor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
