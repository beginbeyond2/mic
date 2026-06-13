package com.micsig.tbook.tbookscope.main.maincenter;

import android.graphics.Point;

import com.micsig.tbook.tbookscope.util.CacheUtil;

/**
 * Created by yangj on 2017/6/30.
 */

public class MainCenterMsgChannels {
    public static final int CH_NULL = -1;

//    public static final int CH1 = CacheUtil.CH1;
//    public static final int CH2 = CacheUtil.CH2;
//    public static final int CH3 = CacheUtil.CH3;
//    public static final int CH4 = CacheUtil.CH4;
//    public static final int CH5 = CacheUtil.CH5;
//    public static final int CH6 = CacheUtil.CH6;
//    public static final int CH7 = CacheUtil.CH7;
//    public static final int CH8 = CacheUtil.CH8;
//    public static final int MATH = CacheUtil.MATH;
//    public static final int REF1 = CacheUtil.REF1;
//    public static final int REF2 = CacheUtil.REF2;
//    public static final int REF3 = CacheUtil.REF3;
//    public static final int REF4 = CacheUtil.REF4;
//    public static final int S1 = CacheUtil.REF4 + 1;
//    public static final int S2 = CacheUtil.REF4 + 2;

    /**
     * 从1到9是当前通道号,CH_NULL表示没有当前选中项
     */
    private int chNO;
    private boolean isFromEventBus = false;
    private Point position = new Point();
    private int chCount;
    /**
     * 此消息发送者是否是channelsLayout本身
     */
    private boolean self = false;

    public MainCenterMsgChannels(int chNO, boolean self, boolean isFromEventBus) {
        this.self = self;
        this.chNO = chNO;
        this.isFromEventBus = isFromEventBus;
    }

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public MainCenterMsgChannels(int chNO) {
        this.chNO = chNO;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    public int getChNO() {
        return chNO;
    }

    public void setChNO(int chNO) {
        this.chNO = chNO;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getChCount() {
        return chCount;
    }

    public void setChCount(int chCount) {
        this.chCount = chCount;
    }
}
