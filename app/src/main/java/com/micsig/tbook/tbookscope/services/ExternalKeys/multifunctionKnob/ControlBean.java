package com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob;

import android.graphics.Rect;

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-28 14:54
 */
public class ControlBean {
    private Rect rect;
    private boolean visible;
    private String name;
    public ControlBean(String name,boolean visible,Rect rect){
        this.name=name;
        this.visible=visible;
        this.rect=rect;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ControlBean{");
        sb.append("rect=").append(rect);
        sb.append(", visible=").append(visible);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
