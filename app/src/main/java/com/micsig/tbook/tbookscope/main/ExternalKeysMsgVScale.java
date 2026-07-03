package com.micsig.tbook.tbookscope.main;

/**
 * Created by yangj on 2018/8/15.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      外部按键垂直缩放消息实体类                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * 【模块定位】                                                                  │
 *   示波器外部按键消息系统 - 垂直缩放（伏/格）调节消息封装                          │
 * 【核心职责】                                                                  │
 *   1. 封装垂直缩放调节相关参数（通道索引、增加/减少方向、调节步数）                 │
 *   2. 作为消息载体在按键事件与垂直缩放控制模块之间传递数据                          │
 * 【架构设计】                                                                  │
 *   简单POJO类，采用私有字段+公共getter/setter的JavaBean模式                       │
 * 【数据流向】                                                                  │
 *   外部按键事件 → 消息封装 → 事件总线 → 垂直缩放控制层 → 垂直缩放执行               │
 * 【依赖关系】                                                                  │
 *   被依赖：ExternalKeysMsg相关处理类、事件总线、垂直缩放控制层                      │
 * 【使用场景】                                                                  │
 *   当用户通过外部按键调节通道垂直缩放（伏/格）时，创建此消息对象并分发              │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class ExternalKeysMsgVScale {
    /** 通道索引 */ // 通道索引字段
    private int chIndex; // 通道索引

    /** 是否为增加垂直缩放操作（true=增加伏/格, false=减少伏/格） */ // 垂直缩放调节方向标识
    private boolean isAdd; // 是否为增加垂直缩放操作

    /** 调节步数/计数 */ // 调节步数字段
    private int count; // 调节步数计数

    /**
     * 获取通道索引
     *
     * @return 通道索引值
     */
    public int getChIndex() {
        return chIndex; // 返回通道索引
    }

    /**
     * 设置通道索引
     *
     * @param chIndex 通道索引值
     */
    public void setChIndex(int chIndex) {
        this.chIndex = chIndex; // 更新通道索引
    }

    /**
     * 获取是否为增加垂直缩放操作
     *
     * @return true表示增加伏/格，false表示减少伏/格
     */
    public boolean isAdd() {
        return isAdd; // 返回垂直缩放调节方向标识
    }

    /**
     * 设置是否为增加垂直缩放操作
     *
     * @param add true表示增加伏/格，false表示减少伏/格
     */
    public void setAdd(boolean add) {
        isAdd = add; // 更新垂直缩放调节方向标识
    }

    /**
     * 获取调节步数
     *
     * @return 调节步数（单次调节的步进数量）
     */
    public int getCount() {
        return count; // 返回调节步数
    }

    /**
     * 设置调节步数
     *
     * @param count 调节步数（单次调节的步进数量）
     */
    public void setCount(int count) {
        this.count = count; // 更新调节步数
    }
}