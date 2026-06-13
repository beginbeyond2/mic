package com.micsig.tbook.tbookscope.wavezone.display;

/**
 * Created by liwb on 2017/11/6.
 */

public interface IWaveControl {
    /***
     * 线的微调，加一个像素
     */
    public void addPixMove();

    /**
     * 线的微调， 减一个像素
     */
    public void subPixMove();

    /**
     * 初始化的位置 X坐标，也就是50%的时候使用到
     */
    public void initCursorX();

    /**
     * 初始化位置 Y坐标，也就是50%的时候使用到
     */
    public void initCursorY();

    public void setCursor(int cursorType, double position);

    public double getCursor(int cursorType);
}
