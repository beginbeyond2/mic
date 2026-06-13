package com.micsig.tbook.tbookscope.main.mainbottom;


/**
 * Created by yangj on 2017/7/31.
 */

public class MainTopMsgRightGone {
    private boolean visible;

    public MainTopMsgRightGone() {
        this.visible = true;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "MainTopMsgRightGone{" +
                "visible=" + visible +
                '}';
    }
}
