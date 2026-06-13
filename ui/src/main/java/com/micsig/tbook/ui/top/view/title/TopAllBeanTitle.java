package com.micsig.tbook.ui.top.view.title;

import com.micsig.tbook.ui.bean.RxMsgSelect;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopAllBeanTitle extends RxMsgSelect implements Cloneable {
    private int index;
    private String text;
    private boolean visible;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public TopAllBeanTitle(int index, String text, boolean visible) {
        this.index = index;
        this.text = text;
        this.visible = visible;
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "TopAllBeanTitle{" +
                "index=" + index +
                ", text='" + text + '\'' +
                ", visible=" + visible +
                '}';
    }
}
