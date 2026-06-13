package com.micsig.tbook.ui.rightslipmenu;

import android.content.Context;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.bean.RxMsgSelect;



/**
 * Created by yangj on 2017/5/3.
 */

public class RightBeanSelect extends RxMsgSelect  {
    private int index;
    private String text;
    private boolean check;
    private boolean enable;
    private String simpleText;

    public RightBeanSelect(int index, String text, boolean check) {
        this.index = index;
        this.text = text;
        this.check = check;
        this.enable = true;
    }

    public boolean isUserDefine(Context context) {
        return context.getString(R.string.serialsUserDefine).equals(text);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getSimpleText() {
        return simpleText;
    }

    public void setSimpleText(String simpleText) {
        this.simpleText = simpleText;
    }

    @Override
    public String toString() {
        return "RightBeanSelect{" +
                "index=" + index +
                ", text='" + text + '\'' +
                ", check=" + check +
                ", enable=" + enable +
                '}';
    }
}
