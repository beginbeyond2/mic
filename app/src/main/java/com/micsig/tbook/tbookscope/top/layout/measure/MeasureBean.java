package com.micsig.tbook.tbookscope.top.layout.measure;

import com.micsig.tbook.ui.bean.RxMsgSelect;


/**
 * Created by yangj on 2017/4/27.
 */

public class MeasureBean extends RxMsgSelect  {
    private int no = -1;
    private int index;
    private String name;
    /**
     *  1 - 9
     */
    private int channel;
    private int drawableResId;
    private boolean isSelect;

    public MeasureBean(int MeasureIndex, String name, int channel, int drawableResId) {
        this.index = MeasureIndex;
        this.name = name;
        this.channel = channel;
        this.drawableResId = drawableResId;
    }

    public MeasureBean(int MeasureIndex, String name, int channel, int drawableResId, boolean isSelect) {
        this.index = MeasureIndex;
        this.name = name;
        this.channel = channel;
        this.drawableResId = drawableResId;
        this.isSelect = isSelect;
    }

    public int getNo(){return no;}
    public void setNo(int no){
        this.no = no;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public void setDrawableResId(int drawableResId) {
        this.drawableResId = drawableResId;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeasureBean measureBean = (MeasureBean) o;

        if (index != measureBean.index) return false;
        return channel == measureBean.channel;

    }

    @Override
    public String toString() {
        return "MeasureBean{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", channel=" + channel +
                ", isSelect=" + isSelect +
                ", drawableResId=" + drawableResId +
                '}';
    }
}
