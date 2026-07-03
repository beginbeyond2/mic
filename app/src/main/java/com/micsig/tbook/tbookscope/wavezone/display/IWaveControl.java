package com.micsig.tbook.tbookscope.wavezone.display;  // 波形控制接口所在包

/*
 * +=============================================================================+
 * |                        IWaveControl — 波形控制接口                           |
 * +=============================================================================+
 * | 模块定位：tbookscope.wavezone.display 显示层                                 |
 * | 核心职责：定义光标线的微调移动与位置初始化行为契约                             |
 * | 架构设计：纯接口，由 CursorManage 等实现类提供光标控制的具体逻辑               |
 * | 数据流向：上层UI微调按钮 → IWaveControl → 光标位置变更 → 界面刷新            |
 * | 依赖关系：无外部依赖，被 CursorManage 引用                                    |
 * | 使用场景：光标线单像素微调移动、光标位置重置到50%中心位置                      |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2017/11/6.
 */

public interface IWaveControl {

    /***
     * 线的微调，加一个像素
     */
    public void addPixMove();  // 光标线正向微调（+1像素）

    /**
     * 线的微调， 减一个像素
     */
    public void subPixMove();  // 光标线反向微调（-1像素）

    /**
     * 初始化的位置 X坐标，也就是50%的时候使用到
     */
    public void initCursorX();  // 初始化列光标X坐标到50%位置

    /**
     * 初始化位置 Y坐标，也就是50%的时候使用到
     */
    public void initCursorY();  // 初始化行光标Y坐标到50%位置

    /**
     * 设置指定类型光标的位置
     *
     * @param cursorType 光标类型（如TChan.Cursor_col_1等）
     * @param position   光标位置值（像素）
     */
    public void setCursor(int cursorType, double position);  // 设置光标位置

    /**
     * 获取指定类型光标的位置
     *
     * @param cursorType 光标类型（如TChan.Cursor_col_1等）
     * @return 光标位置值（像素）
     */
    public double getCursor(int cursorType);  // 获取光标位置
}
