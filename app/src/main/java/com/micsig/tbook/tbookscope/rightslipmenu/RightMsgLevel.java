package com.micsig.tbook.tbookscope.rightslipmenu;

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           RightMsgLevel                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：右侧滑出菜单 - 触发电平消息封装类                                   ║
 * ║ 核心职责：封装触发电平面板的三级选择状态（顶部/中间/底部），用于UI与业务层通信   ║
 * ║ 架构设计：纯数据消息Bean，配合RightLayoutLevel使用，通过RxBus事件总线传递       ║
 * ║ 数据流向：RightLayoutLevel → RxBus → 订阅方                                 ║
 * ║ 依赖关系：无外部依赖                                                         ║
 * ║ 使用场景：触发模式选择（边沿/脉宽/N边沿等）时，传递用户选择结果                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 触发电平面板消息封装类
 * <p>
 * 封装触发电平面板的三级选择状态：
 * - 顶部：触发类型子选项（如上升沿/下降沿/双边沿）
 * - 中间：自动/普通触发模式
 * - 底部：触发源通道选择（CH1-CH8/外部触发）
 * </p>
 */
public class RightMsgLevel {
    /** 顶部选项数量（如边沿类型数量） */
    private int topCount;
    /** 中间选项数量（自动/普通 = 2） */
    private int middleCount;
    /** 底部选项数量（通道数 + 外部触发） */
    private int bottomCount;
    /** 顶部当前选中项索引 */
    private int topSelect;
    /** 中间当前选中项索引（0=自动, 1=普通） */
    private int middleSelect;
    /** 底部当前选中项索引，值为channelIndex，0-3... */
    private int bottomCount;//值为channelIndex，0-3...

    /** 是否来自EventBus事件（true表示由内部事件触发，false表示由用户操作触发） */
    private boolean isFromEventBus;

    /**
     * 默认构造方法
     */
    public RightMsgLevel() {
    }

    /**
     * 获取顶部选项数量
     * @return 顶部选项数量
     */
    public int getTopCount() {
        return topCount;
    }

    /**
     * 设置顶部选项数量
     * @param topCount 顶部选项数量
     */
    public void setTopCount(int topCount) {
        this.topCount = topCount;
    }

    /**
     * 获取中间选项数量
     * @return 中间选项数量
     */
    public int getMiddleCount() {
        return middleCount;
    }

    /**
     * 设置中间选项数量
     * @param middleCount 中间选项数量
     */
    public void setMiddleCount(int middleCount) {
        this.middleCount = middleCount;
    }

    /**
     * 获取底部选项数量
     * @return 底部选项数量
     */
    public int getBottomCount() {
        return bottomCount;
    }

    /**
     * 设置底部选项数量
     * @param bottomCount 底部选项数量
     */
    public void setBottomCount(int bottomCount) {
        this.bottomCount = bottomCount;
    }

    /**
     * 获取顶部当前选中项索引
     * @return 顶部选中项索引
     */
    public int getTopSelect() {
        return topSelect;
    }

    /**
     * 设置顶部当前选中项索引
     * @param topSelect 顶部选中项索引
     */
    public void setTopSelect(int topSelect) {
        this.topSelect = topSelect;
    }

    /**
     * 获取中间当前选中项索引
     * @return 中间选中项索引（0=自动, 1=普通）
     */
    public int getMiddleSelect() {
        return middleSelect;
    }

    /**
     * 设置中间当前选中项索引
     * @param middleSelect 中间选中项索引
     */
    public void setMiddleSelect(int middleSelect) {
        this.middleSelect = middleSelect;
    }

    /**
     * 获取底部当前选中项索引
     * @return 底部选中项索引（通道索引）
     */
    public int getBottomSelect() {
        return bottomSelect;
    }

    /**
     * 设置底部当前选中项索引
     * @param bottomSelect 底部选中项索引
     */
    public void setBottomSelect(int bottomSelect) {
        this.bottomSelect = bottomSelect;
    }

    /**
     * 判断是否来自EventBus事件
     * @return true=来自事件总线, false=来自用户操作
     */
    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    /**
     * 设置是否来自EventBus事件
     * @param fromEventBus 是否来自事件总线
     */
    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    /**
     * 返回对象的字符串表示
     * @return 包含各选择状态和事件来源的字符串
     */
    @Override
    public String toString() {
        return "RightMsgLevel{" +
                "topSelect=" + topSelect +
                ", middleSelect=" + middleSelect +
                ", channelSelect=" + bottomSelect +
                ", isFromEventBus=" + isFromEventBus +
                '}';
    }
}
