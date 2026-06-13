package com.micsig.tbook.ui.top.view.channel;

import com.micsig.tbook.ui.bean.RxMsgSelect;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopBeanChannel extends RxMsgSelect implements Cloneable {
    private int index;
    private String text;
    private String simpleText;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSimpleText() {
        return simpleText;
    }

    public void setSimpleText(String simpleText) {
        this.simpleText = simpleText;
    }

    public TopBeanChannel(int index, String text) {
        this.index = index;
        this.text = text;
    }

    @Override
    public String toString() {
        return "TopBeanChannel{" +
                "index=" + index +
                ", text='" + text + '\'' +
                ", rxMsgSelect='" + rxMsgSelect + '\'' +
                '}';
    }
}
