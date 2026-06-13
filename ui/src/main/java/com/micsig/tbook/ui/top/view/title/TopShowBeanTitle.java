package com.micsig.tbook.ui.top.view.title;

import com.micsig.tbook.ui.bean.RxMsgSelect;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopShowBeanTitle extends RxMsgSelect implements Cloneable {
    private int indexShow;
    private int indexAll;
    private String text;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public TopShowBeanTitle(int indexShow, int indexAll, String text) {
        this.indexShow = indexShow;
        this.indexAll = indexAll;
        this.text = text;
    }

    public int getIndexShow() {
        return indexShow;
    }

    public void setIndexShow(int indexShow) {
        this.indexShow = indexShow;
    }

    public int getIndexAll() {
        return indexAll;
    }

    public void setIndexAll(int indexAll) {
        this.indexAll = indexAll;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TopShowBeanTitle{" +
                "indexShow=" + indexShow +
                ", indexAll=" + indexAll +
                ", text='" + text + '\'' +
                '}';
    }
}
