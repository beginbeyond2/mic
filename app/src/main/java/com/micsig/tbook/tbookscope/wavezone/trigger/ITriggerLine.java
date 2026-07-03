package com.micsig.tbook.tbookscope.wavezone.trigger;

import android.graphics.Canvas;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;

import java.util.ArrayList;

/*
 * +=============================================================================+
 * |                            ITriggerLine                                     |
 * +=============================================================================+
 * | 模块定位 : 触发电平线接口定义                                                 |
 * | 核心职责 : 定义触发电平线的通用行为契约（激活、显示、位置、绘制等）               |
 * | 架构设计 : 接口层，继承IWorkMode，由DiscreetVoltageLine等实现类具体实现          |
 * | 数据流向 : 上层控制逻辑 → ITriggerLine接口 → 实现类执行绘制/状态变更            |
 * | 依赖关系 : IWorkMode(工作模式)、ICanvasGL(OpenGL画布)、Canvas(系统画布)、       |
 * |            DiscreetVoltageLineInfoBean(信息载体)                             |
 * | 使用场景 : 所有触发电平线的统一抽象，支持多种显示模式与通道操作                   |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2017/5/12
 * 触发电平线 接口.
 */
public interface ITriggerLine extends IWorkMode {

    /***
     * 显示一个箭头
     */
    int ShowMode_One = 0x01; // // 单箭头显示模式（普通触发电平）

    /**
     * 显示二个箭头，高低电平
     */
    int ShowMode_Two = 0x02; // // 双箭头显示模式（高低电平触发）

    /***
     * 显示三个箭头，logic触发模式
     */
    int ShowMode_Three = 0x03; // // 三箭头显示模式（逻辑触发）

    /***
     * 显示模式：普通
     */
    int VoltageLine_Normal = 0; // // 电平线索引：普通（单箭头模式下）

    /***
     * 显示模式2：高
     */
    int VoltageLine_High = 1; // // 电平线索引：高电平（双箭头模式下）

    /***
     * 显示模式2：低
     */
    int VoltageLine_Low = 2; // // 电平线索引：低电平（双箭头模式下）


    /**
     * 设置电平线的激活状态，激活时显示前置图标样式
     * @param bActive true=激活（前置样式），false=非激活
     */
    void setActive(boolean bActive); // // 设置电平线是否处于激活（前置/选中）状态

    /**
     * 查询电平线是否处于激活状态
     * @return true=激活，false=非激活
     */
    boolean isActive(); // // 查询当前电平线是否激活


    /***
     * 设置通道就是设置画线颜色
     * @param channelId 通道号
     */
    void setChannelId(int channelId); // // 设置当前所属通道，同时决定绘制颜色

    /***
     * 返回当前通道
     * @return 1~4
     */
    int getChannelId(); // // 获取当前所属通道号

    /**
     * 获取电平线名称ID
     * @return 通道序列号ID
     */
    int getNameId(); // // 获取电平线对应的名称ID（用于标识阈值序列号）

    /**
     * 获取电平线名称
     * @return 电平线类型名称字符串
     */
    String getName(); // // 获取电平线类型名称（如Value1/Value2等）
    /**
     *
     */

    /***
     * 设置显示线是否显示
     * @param visibleLine true=显示电平线，false=只显示图标
     */
    void setVisibleLine(boolean visibleLine); // // 设置电平线横线是否可见

    /***
     * 查询电平线是否可见
     * @return true=可见，false=不可见
     */
    boolean getVisibleLine(); // // 查询电平线横线是否可见

    /**
     * 设置整个控件的显现性
     * @param visible true=可见，false=不可见
     */
    void setVisible(boolean visible); // // 设置整个电平线控件是否可见

    /**
     * 查询整个控件是否可见
     * @return true=可见，false=不可见
     */
    boolean getVisible(); // // 查询整个电平线控件是否可见

    /***
     * 设置当前操作的线
     * @param currYIndex 通道号/电平线索引
     */
    void setCurrYIndex(int currYIndex); // // 设置当前操作的电平线索引号

    /***
     * 返回当前操作线的序号
     * @return 当前电平线索引号
     */
    int getCurrYIndex(); // // 获取当前操作的电平线索引号

    /***
     * \设置显示模式
     * 显示模式：触发电平分为三种，预值电平分为二种
     * @param showMode ShowMode_One、 ShowMode_Two、ShowMode_Three
     */
    void setShowMode(int showMode); // // 设置电平线显示模式（单箭头/双箭头/三箭头）

    /***
     * 返回显示模式
     * @return 显示模式常量
     */
    int getShowMode(); // // 获取当前电平线显示模式

    /***
     * 偏移Y
     * @return 设置的值是否超过边界
     */
    boolean setOffsetY(double offsetY); // // 根据偏移量调整当前电平线Y位置，返回是否越界

    /***
     * 设置Y坐标 传过来的是屏幕实际位置,保存的是1000对应的位置
     * @return 设置的值是否超过边界
     */
    boolean setCurrY(double currY); // // 直接设置当前索引电平线的Y坐标（屏幕像素值）

    /***
     * 设置Y坐标
     * @return 设置的值是否超过边界
     */
    boolean setOtherY(int ch, double y); // // 设置指定通道电平线的Y坐标（屏幕像素值）

    /**
     * 设置电平线旁边的文本内容
     * @param text 要显示的文本
     */
    void setText(String text); // // 设置电平线旁显示的数值文本

    /**
     * 获取电平线旁边的文本内容
     * @return 当前文本
     */
    String getText(); // // 获取电平线旁显示的数值文本

    /**
     * get Y坐标 当前屏幕实际位置
     * @return 当前索引电平线的Y屏幕坐标
     */
    double getCurrY(); // // 获取当前索引电平线的Y坐标（屏幕像素值）

    /**
     * 获取指定通道电平线的Y坐标
     * @param ch 通道号
     * @return Y屏幕坐标
     */
    double getOtherY(int ch); // // 获取指定通道电平线的Y坐标（屏幕像素值）

    /**
     * 获取所有通道电平线的Y坐标数组
     * @return Y坐标数组
     */
    double[] getCurrYAll(); // // 获取所有通道电平线的Y坐标数组（屏幕像素值）

    /***
     * 绘制到系统Canvas
     * @param canvas 系统画布
     */
    void draw(Canvas canvas); // // 在系统Canvas上绘制电平线（已弃用，保留接口）

    /**
     * 绘制到OpenGL画布
     * @param canvas OpenGL画布
     */
    void draw(ICanvasGL canvas); // // 在ICanvasGL上绘制电平线（主要绘制入口）

    /***
     * 设置是否显示
     * @param show true=显示，false=隐藏
     */
    void setShowState(boolean show); // // 设置电平线是否参与绘制显示

    /**
     * 查询电平线是否参与绘制显示
     * @return true=显示，false=隐藏
     */
    boolean getShowState(); // // 查询电平线是否参与绘制显示

    /**
     * 返回通道显示信息
     * @return 通道显示信息列表
     */
    ArrayList<DiscreetVoltageLineInfoBean> getShowChannelInfo(); // // 获取所有需要显示的通道电平线信息列表

    /**
     * 刷新电平线（重新初始化Bitmap并重绘）
     */
    void refresh(); // // 刷新电平线资源与绘制
}
