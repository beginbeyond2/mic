package com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob;

import android.graphics.Point;

import java.util.List;

/**
 * Created by yangj on 2018/5/22.
 */

public class ExternalKeysNode {
    public static final String TYPE_NO_CLICK = "noClick";//不可点击类型，相对于一般的按键是直接模拟点击来说的
    public static final String TYPE_CLICK_IS_SUREBACK = "clickIsBack";//点击确定返回上一级类型
    public static final String TYPE_BRIGHTNESS_PROGRESS = "brightnessProgress";//波形亮度
    public static final String TYPE_TRIGGER_SENSITIVITY_PROGRESS = "triggerSensitivity";//波形亮度
    public static final String TYPE_INTENSITY_PROGRESS = "intensityProgress";//网格亮度
    public static final String TYPE_TOPCOUNT_PROGRESS = "topCount";//topDialogScale进度条
    public static final String TYPE_RIGHT_SLIP_CH_DELAY = "rightSlipChDelay";//模拟通道设置菜单delay弹窗中进度条
    public static final String TYPE_RIGHT_SLIP_CH_DELAY_UNIT = "rightSlipChDelayUnit";//模拟通道设置菜单delay弹窗中 移动光标位置
    public static final String TYPE_ALPHA_PROGRESS="TYPE_ALPHA_PROGRESS"; //显示透明度
    public static final String TYPE_PERSIST_ADJUST = "persistAdjust";//余辉时间
    public static final String TYPE_TRIGGER_TITLE = "triggerTitle";//触发菜单的滑动
    public static final String TYPE_MAIN_MENU_CHANNEL_MEASURE_COMMON="TYPE_MAIN_MENU_CHANNEL_MEASURE_COMMON"; //主菜单测量common通道
    public static final String TYPE_MAIN_MENU_CHANNEL_MEASURE_SETTING="TYPE_MAIN_MENU_CHANNEL_MEASURE_SETTING";
    public static final String TYPE_MAIN_MENU_CHANNEL_SAVE_WAVE="TYPE_MAIN_MENU_CHANNEL_SAVE_WAVE";
    public static final String TYPE_MAIN_MENU_CHANNEL_SAVE_CSV="TYPE_MAIN_MENU_CHANNEL_SAVE_CSV";
    public static final String TYPE_MAIN_MENU_CHANNEL_AUTO_SAVE_CHANNEL="TYPE_MAIN_MENU_CHANNEL_AUTO_SAVE_CHANNEL";

    public static final String TYPE_MAIN_MENU_CHANNEL_SAVE_BIN="TYPE_MAIN_MENU_CHANNEL_SAVE_BIN";
    public static final String TYPE_MAIN_MENU_CHANNEL_CURSOR_COMMON="TYPE_MAIN_MENU_CHANNEL_CURSOR_COMMON";
    public static final String TYPE_TRIGGER_VIDEO_LINE = "triggerVideoLine";//视频触发的line的显示
    public static final String TYPE_TRIGGER_VIDEO_STANDARD_FIRST = "triggerVideoStandardFirst";//视频触发的line的显示
    public static final String TYPE_RECEIVE_MSG = "receiveMsg";
    public static final String TYPE_REFRECALL_PROGRESS = "refRecallProgress";//参考通道选择列表
    public static final String TYPE_BACK_TO_BACK = "backToBack";//rightSerials中，uart下，baudRate的小键盘自定义数据确定之后的连续返回
    public static final String TYPE_CENTER_SEGMENT_PLAY = "centerSegmentPlay";//分段存储导航条的播放按键
    public static final String TYPE_CSV_LIST = "typeCsvList";//Ref设置页面CSV列表
    public static final String TYPE_SPINNER_LIST = "typeSpinnerList";// 文件/文件夹 选择列表

    public static final String TYPE_CAN_PERCENT = "canPercent";//rightSerials中，can下，baudRate的percent的外部按键旋转点击

    public static final String DIALOG_MEASUREDELAY = "DIALOG_MEASUREDELAY";
    public static final String DIALOG_MEASUREPHASE = "DIALOG_MEASUREPHASE";
    public static final String DIALOG_TOPCOUNT = "DIALOG_TOPCOUNT";
    public static final String DIALOG_TOPSCALE = "DIALOG_TOPSCALE";
    public static final String DIALOG_TEXTKEYBOARD = "DIALOG_TEXTKEYBOARD";
    public static final String DIALOG_NUMBERKEYBOARD = "DIALOG_NUMBERKEYBOARD";
    public static final String DIALOG_BAUDRATE = "DIALOG_BAUDRATE";
    public static final String DIALOG_CHANNELLABEL = "DIALOG_CHANNELLABEL";
    public static final String DIALOG_BANDWIDTH = "DIALOG_BANDWIDTH";
    public static final String DIALOG_PROBEMULTIPLE = "DIALOG_PROBEMULTIPLE";
    public static final String DIALOG_REFRECALL = "DIALOG_REFRECALL";
    public static final String DIALOG_AFTERGLOW = "DIALOG_AFTERGLOW";
    public static final String DIALOG_PROBE_INTERFACE="DIALOG_PROBE_INTERFACE";
    public static final String DIALOG_MATH_PERSISTENCE="DIALOG_MATH_PERSISTENCE";
    public static final String DIALOG_LOAD_CSV="DIALOG_LOAD_CSV";
    public static final String DIALOG_SELECT_COLOR = "DIALOG_SELECT_COLOR";
    public static final String DIALOG_MEASURETVALUE = "DIALOG_MEASURETVALUE";

    public static final int ACTION_REFRECALL_UP = -1;
    public static final int ACTION_REFRECALL_FINISH = 0;
    public static final int ACTION_REFRECALL_DOWN = 1;

    private String name;
    private int index;
    private int x, y, w, h;
    private String type;
    private String dialog;
    private ExternalKeysNode parentNode;
    private List<ExternalKeysNode> parentNodes;
    private List<ExternalKeysNode> childNodes;

    /**
     * 当前列表中的选中项的保存，只在list的第一个选项中存取
     */
    private int curListSelect;
    /**
     * 当前item的显现性
     */
    private boolean visible = true;

    public ExternalKeysNode() {
    }

    public ExternalKeysNode setName(String name) {
        this.name = name;
        return this;
    }

    public void setPlace(int index, int x, int y, int w, int h) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    public Point getCenterPoint(){
        return new Point(x+w/2,y+h/2);
    }

    public boolean isContain(int px, int py) {
        return px >= x && px <= (x + w) && py >= y && py <= (y + h);
    }

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public void setParentNode(ExternalKeysNode parentNode) {
        this.parentNode = parentNode;
    }

    public void setParentNodes(List<ExternalKeysNode> parentNodes) {
        this.parentNodes = parentNodes;
    }

    public void setChildNodes(List<ExternalKeysNode> childNodes) {
        this.childNodes = childNodes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setW(int w) {
        this.w = w;
    }

    public void setH(int h) {
        this.h = h;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setCurListSelect(int curListSelect) {
        this.curListSelect = curListSelect;
    }

    public int getCurListSelect() {
        return curListSelect;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public ExternalKeysNode getParentNode() {
        return parentNode;
    }

    public List<ExternalKeysNode> getParentNodes() {
        return parentNodes;
    }

    public List<ExternalKeysNode> getChildNodes() {
        return childNodes;
    }

    @Override
    public String toString() {
        return "ExternalKeysNode{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", visible=" + visible +
                ", curListSelect="+ curListSelect+
                ", type = " + type +
                ", dialog = " + dialog +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                '}';
    }
}
