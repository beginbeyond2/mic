package com.micsig.tbook.tbookscope.wavezone.trigger;

import android.graphics.Canvas;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;

import java.util.ArrayList;

/**
 * Created by liwb on 2017/5/12
 * 触发电平线 接口.
 */

public interface ITriggerLine extends IWorkMode {

    /***
     * 显示一个箭头
     */
    int ShowMode_One = 0x01;
    /**
     * 显示二个箭头，高低电平
     */
    int ShowMode_Two = 0x02;
    /***
     * 显示三个箭头，logic触发模式
     */
    int ShowMode_Three = 0x03;

    /***
     * 显示模式：普通
     */
    int VoltageLine_Normal = 0;
    /***
     * 显示模式2：高
     */
    int VoltageLine_High = 1;
    /***
     * 显示模式2：低
     */
    int VoltageLine_Low = 2;


    void setActive(boolean bActive);
    boolean isActive();


    /***
     * 设置通道就是设置画线颜色
     * @param channelId
     */
    void setChannelId(int channelId);

    /***
     * 返回当前通道
     * @return 1~4
     */
    int getChannelId();

    int getNameId();

    String getName();
    /**
     *
     */

    /***
     * 设置显示线是否显示
     * @param visibleLine
     */
    void setVisibleLine(boolean visibleLine);

    /***
     *
     * @return
     */
    boolean getVisibleLine();

    /**
     * 设置整个控件的显现性
     */
    void setVisible(boolean visible);

    boolean getVisible();

    /***
     * 设置当前操作的线
     * @param currYIndex 通道号
     */
    void setCurrYIndex(int currYIndex);

    /***
     * 返回当前操作线的序号
     * @return
     */
    int getCurrYIndex();

    /***
     * \设置显示模式
     * 显示模式：触发电平分为三种，预值电平分为二种
     * @param showMode ShowMode_One、 ShowMode_Two、ShowMode_Three
     */
    void setShowMode(int showMode);

    /***
     * 返回显示模式
     * @return
     */
    int getShowMode();

    /***
     * 偏移Y
     * @return 设置的值是否超过边界
     */
    boolean setOffsetY(double offsetY);

    /***
     * 设置Y坐标 传过来的是屏幕实际位置,保存的是1000对应的位置
     * @return 设置的值是否超过边界
     */
    boolean setCurrY(double currY);

    /***
     * 设置Y坐标
     * @return 设置的值是否超过边界
     */
    boolean setOtherY(int ch, double y);

    void setText(String text);

    String getText();

    /**
     * get Y坐标 当前屏幕实际位置
     */
    double getCurrY();

    double getOtherY(int ch);

    double[] getCurrYAll();

    /***
     * 绘制
     */
    void draw(Canvas canvas);

    void draw(ICanvasGL canvas);

    /***
     * 设置是否显示
     */
    void setShowState(boolean show);

    boolean getShowState();

    /**
     * 返回通道显示信息
     */
    ArrayList<DiscreetVoltageLineInfoBean> getShowChannelInfo();

    void refresh();
}
