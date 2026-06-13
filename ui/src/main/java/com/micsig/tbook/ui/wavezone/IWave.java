package com.micsig.tbook.ui.wavezone;

import android.graphics.Canvas;

import com.chillingvan.canvasgl.ICanvasGL;

import java.util.function.Consumer;

/**
 * Created by liwb on 2017/5/5.
 */

public interface IWave {
    /**
     * 偏移量
     *
     * @param offsetX x方向偏移
     * @param offsetY y方向偏移
     */
    void moveLine(int offsetX, int offsetY);

    /**
     * 画图函数
     */
    void draw(Canvas canvas);

    /**
     * openGL 画图函数
     *
     * @param canvas
     */
    void draw(ICanvasGL canvas);

    /**
     * 初始位置 50%位置
     */
    void resultIniRect();

    /**
     * 设置线的名称：
     *
     * @param nameId 如：
     */
    void setLineNameId(int nameId);

    int getLineNameID();

    long getX();

    double getY();

    void setX(long x);

    void setY(double y);

    void setColor(int color);

    int getColor();

    void setVisible(boolean visible);

    boolean getVisible();

    void setSelected(boolean selected);

    boolean isSelected();

    void movePix(double px);

    void setOnSelectChangeEvent(OnSelectChangeEvent onSelectChangeEvent);

    void setOnMovingWaveEvent(OnMovingWaveEvent onMovingWaveEvent);

    /***
     *@deprecated 改变时触发
     */
    interface OnSelectChangeEvent {
        void OnSelectChange(IWave iWave, boolean isSelect);
    }

    /***
     * @deprecated 波形移动时
     */
    interface OnMovingWaveEvent {
        void OnMovingWave(IWave iWave, long x, double y, boolean isSwitchWorkMode, boolean isFromEventBus);
    }
}
