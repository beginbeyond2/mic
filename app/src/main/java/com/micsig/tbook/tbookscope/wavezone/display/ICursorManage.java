package com.micsig.tbook.tbookscope.wavezone.display;  // 光标显示管理接口所在包

/*
 * +=============================================================================+
 * |                        ICursorManage — 光标管理接口                          |
 * +=============================================================================+
 * | 模块定位：tbookscope.wavezone.display 显示层                                 |
 * | 核心职责：定义光标（Cursor）的选中、移动、可见性控制等管理行为契约             |
 * | 架构设计：纯接口，由 CursorManage 实现类提供具体逻辑                          |
 * | 数据流向：上层UI/事件 → ICursorManage → Cursor_impIWave（具体光标实例）       |
 * | 依赖关系：无外部依赖，被 CursorManage / CursorLabel 等类引用                  |
 * | 使用场景：示波器光标选择、移动、跟踪、可见性切换等交互操作                    |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2017/11/6.
 */

public interface ICursorManage {

    /**
     * 设置水平光标（行光标）的可见性
     *
     * @param visible true表示显示行光标，false表示隐藏行光标
     */
    public void setRowVisible(boolean visible);  // 设置行光标可见性

    /**
     * 设置垂直光标（列光标）的可见性
     *
     * @param visible true表示显示列光标，false表示隐藏列光标
     */
    public void setColVisible(boolean visible);  // 设置列光标可见性

    /**
     * 设置光标所关联通道的颜色
     *
     * @param ChNo 通道编号
     */
    public void setCursorChannelColor(int ChNo);  // 设置光标通道颜色

    /**
     * 根据坐标位置选中对应的光标
     *
     * @param x 水平像素坐标
     * @param y 垂直像素坐标（double精度）
     * @return 被选中的光标类型索引，未选中返回-1
     */
    public int selectCursor(int x, double y);  // 根据坐标选中光标

    /**
     * 设置当前选中的光标索引
     *
     * @param index 光标类型索引
     */
    public void setSelectCursor(int index);  // 设置选中光标索引

    /**
     * 移动当前选中的光标到指定位置
     *
     * @param x 水平偏移量（像素）
     * @param y 垂直偏移量（像素，double精度）
     */
    public void moveSelectCursor(int x, double y);  // 移动选中光标

    /**
     * 以整数像素偏移量移动多个选中的光标
     *
     * @param x 水平偏移量（像素）
     * @param y 垂直偏移量（像素）
     */
    public void moveMultiSelectCursor(int x, int y);  // 移动多个选中光标

    /**
     * 光标移动结束回调，用于收尾处理（如保存位置缓存）
     */
    public void moveFinish();  // 光标移动结束

    /**
     * 设置光标跟踪模式，使光标跟随指定通道波形移动
     *
     * @param ChNo 要跟踪的通道编号
     */
    public void setCursorTracking(int ChNo);  // 设置光标跟踪通道

    /**
     * 获取当前选中的光标类型索引
     *
     * @return 当前选中光标的类型索引
     */
    public int getCurrSelectCursor();  // 获取当前选中光标索引
}
