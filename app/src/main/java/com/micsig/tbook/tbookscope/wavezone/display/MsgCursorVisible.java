package com.micsig.tbook.tbookscope.wavezone.display;  // 光标显示消息类所在包

/*
 * +=============================================================================+
 * |                     MsgCursorVisible — 光标可见性消息                        |
 * +=============================================================================+
 * | 模块定位：tbookscope.wavezone.display 显示层                                 |
 * | 核心职责：封装光标可见性状态消息，用于在不同组件间传递光标显示/隐藏信息         |
 * | 架构设计：简单POJO消息类，承载YT模式标志、竖直光标标志、可见性标志             |
 * | 数据流向：光标管理模块 → MsgCursorVisible → UI/事件总线                      |
 * | 依赖关系：无外部依赖，被光标相关模块使用                                      |
 * | 使用场景：光标可见性切换时的消息传递，区分YT/XY模式和行/列光标                 |
 * +=============================================================================+
 */

public class MsgCursorVisible {

    /** 是否为YT模式 */  // YT模式标志
    private boolean isYt;  // YT模式标志

    /** 是否为竖直光标（列光标） */  // 竖直光标标志
    private boolean isShu;  // 竖直（列光标）标志

    /** 光标是否可见 */  // 可见性标志
    private boolean visible;  // 可见性标志

    /**
     * 构造光标可见性消息
     *
     * @param isYt    是否为YT模式
     * @param isShu   是否为竖直光标（列光标）
     * @param visible 光标是否可见
     */
    public MsgCursorVisible(boolean isYt, boolean isShu, boolean visible) {  // 构造函数
        this.isYt = isYt;  // 赋值YT模式标志
        this.isShu = isShu;  // 赋值竖直光标标志
        this.visible = visible;  // 赋值可见性标志
    }

    /**
     * 判断是否为YT模式
     *
     * @return true表示YT模式，false表示XY模式
     */
    public boolean isYt() {  // 获取YT模式标志
        return isYt;  // 返回YT模式标志
    }

    /**
     * 设置YT模式标志
     *
     * @param yt true表示YT模式，false表示XY模式
     */
    public void setYt(boolean yt) {  // 设置YT模式标志
        isYt = yt;  // 赋值YT模式标志
    }

    /**
     * 判断是否为竖直光标（列光标）
     *
     * @return true表示竖直光标，false表示水平光标
     */
    public boolean isShu() {  // 获取竖直光标标志
        return isShu;  // 返回竖直光标标志
    }

    /**
     * 设置竖直光标标志
     *
     * @param shu true表示竖直光标，false表示水平光标
     */
    public void setShu(boolean shu) {  // 设置竖直光标标志
        isShu = shu;  // 赋值竖直光标标志
    }

    /**
     * 判断光标是否可见
     *
     * @return true表示可见，false表示隐藏
     */
    public boolean isVisible() {  // 获取可见性标志
        return visible;  // 返回可见性标志
    }

    /**
     * 设置光标可见性
     *
     * @param visible true表示可见，false表示隐藏
     */
    public void setVisible(boolean visible) {  // 设置可见性标志
        this.visible = visible;  // 赋值可见性标志
    }

    /**
     * 返回消息的字符串表示，用于调试日志
     *
     * @return 包含isYt、isShu、visible的字符串
     */
    @Override  // 覆写toString方法
    public String toString() {  // 转为字符串
        return "CursorVisibleMsg{" +  // 返回格式化字符串
                "isYt=" + isYt +  // 拼接YT模式标志
                ", isShu=" + isShu +  // 拼接竖直光标标志
                ", visible=" + visible +  // 拼接可见性标志
                '}';  // 结束大括号
    }
}
