package com.micsig.tbook.tbookscope.top.layout.auto;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgAutoRange implements IAutoDetail {
    private RxBooleanWithSelect range;
    private RxBooleanWithSelect vertical;
    private RxBooleanWithSelect horizontal;
    private RxBooleanWithSelect level;

    public RxBooleanWithSelect getRange() {
        return range;
    }

    public void setRange(boolean range) {
        if (this.range == null) {
            this.range = new RxBooleanWithSelect(range);
        } else {
            this.range.setValue(range);
            setAllUnSelect();
            this.range.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        if (this.vertical == null) {
            this.vertical = new RxBooleanWithSelect(vertical);
        } else {
            this.vertical.setValue(vertical);
            setAllUnSelect();
            this.vertical.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(boolean horizontal) {
        if (this.horizontal == null) {
            this.horizontal = new RxBooleanWithSelect(horizontal);
        } else {
            this.horizontal.setValue(horizontal);
            setAllUnSelect();
            this.horizontal.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getLevel() {
        return level;
    }

    public void setLevel(boolean level) {
        if (this.level == null) {
            this.level = new RxBooleanWithSelect(level);
        } else {
            this.level.setValue(level);
            setAllUnSelect();
            this.level.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        range.setRxMsgSelect(false);
        vertical.setRxMsgSelect(false);
        horizontal.setRxMsgSelect(false);
        level.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgAutoRange{" +
                "range=" + range +
                ", vertical=" + vertical +
                ", horizontal=" + horizontal +
                ", level=" + level +
                '}';
    }
}
