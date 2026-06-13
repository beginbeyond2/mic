package com.micsig.tbook.tbookscope.top.popwindow;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;

/**
 * Created by yangj on 2018/8/21.
 */

public class TopMsgPopWindow {
    private int checkIndex;
    private RxBooleanWithSelect ytMode = new RxBooleanWithSelect(true);//是否是yt模式
    private RxBooleanWithSelect serialWord = new RxBooleanWithSelect(false);//是否是serial word模式

    public TopMsgPopWindow(int checkIndex, boolean ytMode, boolean serialWord) {
        this.checkIndex = checkIndex;
        this.ytMode = new RxBooleanWithSelect(ytMode);
        this.serialWord = new RxBooleanWithSelect(serialWord);
    }

    public int getCheckIndex() {
        return checkIndex;
    }

    public void setCheckIndex(int checkIndex) {
        this.checkIndex = checkIndex;
    }

    public RxBooleanWithSelect getYtMode() {
        return ytMode;
    }

    public void setYtMode(boolean ytMode) {
        if (this.ytMode.isValue() != ytMode) {
            this.ytMode.setValue(ytMode);
            setAllUnSelect();
            this.ytMode.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getSerialWord() {
        return serialWord;
    }

    public void setSerialWord(boolean serialWord) {
        if (this.serialWord.isValue() != serialWord) {
            this.serialWord.setValue(serialWord);
            setAllUnSelect();
            this.serialWord.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        ytMode.setRxMsgSelect(false);
        serialWord.setRxMsgSelect(false);
    }
}
