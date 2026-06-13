package com.micsig.tbook.tbookscope.main;

/**
 * Created by yangj on 2018/8/15.
 */

public class ExKeysMsgRightCanPercent {
    private boolean isS1;
    private boolean isAdd;
    private boolean isTop;
    private int count;

    public ExKeysMsgRightCanPercent(boolean isS1, boolean isAdd, boolean isTop, int count) {
        this.isS1 = isS1;
        this.isAdd = isAdd;
        this.isTop = isTop;
        this.count = count;
    }

    public boolean isS1() {
        return isS1;
    }

    public void setS1(boolean s1) {
        isS1 = s1;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean add) {
        isAdd = add;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
