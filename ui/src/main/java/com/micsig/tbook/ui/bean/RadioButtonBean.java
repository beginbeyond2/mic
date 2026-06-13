package com.micsig.tbook.ui.bean;

import android.content.Context;
import android.view.View;

import com.micsig.tbook.ui.R;

import java.util.function.BiConsumer;

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-27 11:13
 */
public class RadioButtonBean {
    private int index;
    private String text;
    private boolean check;
    private boolean enable;
    private int visible;
    private String simpleText;
    private int ResIdBackGround;
    private boolean enableBeforeColor;
    private int beforeColor;

    private BiConsumer<View,RadioButtonBean> onClick=null;

    private int itemLeftMargin,itemTopMargin,itemRightMargin,itemBottomMargin;

    public RadioButtonBean(int index,String text,boolean check,boolean enableBeforeColor,int beforeColor,int resIdBackGround,BiConsumer<View,RadioButtonBean> onClick){
        this.index=index;
        this.text=text;
        this.check=check;
        this.enable=true;
        this.enableBeforeColor=enableBeforeColor;
        this.beforeColor=beforeColor;
        this.ResIdBackGround=resIdBackGround;
        this.onClick=onClick;
    }
    public void setItemMargin(int itemLeftMargin,int itemTopMargin,int itemRightMargin,int itemBottomMargin){
        this.itemLeftMargin=itemLeftMargin;
        this.itemTopMargin=itemTopMargin;
        this.itemRightMargin=itemRightMargin;
        this.itemBottomMargin=itemBottomMargin;
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

    public int getResIdBackGround() {
        return ResIdBackGround;
    }

    public void setResIdBackGround(int resIdBackGround) {
        ResIdBackGround = resIdBackGround;
    }

    public BiConsumer<View, RadioButtonBean> getOnClick() {
        return onClick;
    }

    public void setOnClick(BiConsumer<View, RadioButtonBean> onClick) {
        this.onClick = onClick;
    }

    public int getItemLeftMargin() {
        return itemLeftMargin;
    }

    public int getItemTopMargin() {
        return itemTopMargin;
    }

    public int getItemRightMargin() {
        return itemRightMargin;
    }

    public int getItemBottomMargin() {
        return itemBottomMargin;
    }

    public boolean isEnableBeforeColor() {
        return enableBeforeColor;
    }

    public void setEnableBeforeColor(boolean enableBeforeColor) {
        this.enableBeforeColor = enableBeforeColor;
    }

    public int getBeforeColor() {
        return beforeColor;
    }

    public void setBeforeColor(int beforeColor) {
        this.beforeColor = beforeColor;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RadioButtonBean{");
        sb.append("index=").append(index);
        sb.append(", text='").append(text).append('\'');
        sb.append(", check=").append(check);
        sb.append(", enable=").append(enable);
        sb.append(", visible=").append(visible);
        sb.append(", simpleText='").append(simpleText).append('\'');
        sb.append(", beforeColor=").append(beforeColor);
        sb.append('}');
        return sb.toString();
    }
}
