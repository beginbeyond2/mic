package com.micsig.tbook.ui.main;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.wavezone.TChan;


/**
 * Created by yangj on 2017/5/31.
 */

public class MainBeanTopRight  {
    public static final int LINE_NULL = 0;
    public static final int LINE_BOTTOM = 1;
    public static final int LINE_TOP = 2;

    /**
     * 1 ~ 8 通道1-8<p/>
     * 0 第五项，比如：显示高低通道的另一个通道项、Serials的显示项
     */
    private int channel;
    private String text;
    private int line;
    private int colorResId;
    private int clickStart;
    private int clickEnd;
    private boolean visible;
    private boolean showNumber;
    private boolean changed;

    public MainBeanTopRight(String text, int line, int colorResId) {
        this.text = text;
        this.line = line;
        this.colorResId = colorResId;
        this.channel = 0;
        showNumber = true;
        visible = false;
        changed = false;
    }


    public MainBeanTopRight(int channel, String text, int line) {
        this.channel = channel;
        this.text = text;
        this.line = line;
        showNumber = true;
        visible = false;
        changed = false;
        switch (channel) {
            case TChan.Ch1:
                colorResId = R.color.colorCh1;
                break;
            case TChan.Ch2:
                colorResId = R.color.colorCh2;
                break;
            case TChan.Ch3:
                colorResId = R.color.colorCh3;
                break;
            case TChan.Ch4:
                colorResId = R.color.colorCh4;
                break;
            case TChan.Ch5:
                colorResId = R.color.colorCh5;
                break;
            case TChan.Ch6:
                colorResId = R.color.colorCh6;
                break;
            case TChan.Ch7:
                colorResId = R.color.colorCh7;
                break;
            case TChan.Ch8:
                colorResId = R.color.colorCh8;
                break;
            default:
                colorResId = R.color.colorChCommon;
                break;
        }
    }


    public MainBeanTopRight clone() {
        MainBeanTopRight m = new MainBeanTopRight(this.getText(), this.line, this.colorResId);
        m.text = this.text;
        m.line = this.line;
        m.colorResId = this.colorResId;
        m.channel = this.channel;
        m.showNumber = showNumber;
        m.visible = visible;
        m.changed = changed;
        return m;
    }

    public boolean isShowNumber() {
        return showNumber;
    }

    public void setShowNumber(boolean showNumber) {
        this.showNumber = showNumber;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColorResId() {
        return colorResId;
    }

    public void setColorResId(int colorResId) {
        this.colorResId = colorResId;
    }

    public int getClickStart() {
        return clickStart;
    }

    public void setClickStart(int clickStart) {
        this.clickStart = clickStart;
    }

    public int getClickEnd() {
        return clickEnd;
    }

    public void setClickEnd(int clickEnd) {
        this.clickEnd = clickEnd;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return "MainBeanTopRight{" +
                "channel=" + channel +
                ", text='" + text + '\'' +
                ", line=" + line +
                ", colorResId=" + colorResId +
                ", clickStart=" + clickStart +
                ", clickEnd=" + clickEnd +
                ", visible=" + visible +
                ", showNumber=" + showNumber +
                ", changed=" + changed +
                '}';
    }
}
