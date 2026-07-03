package com.micsig.tbook.tbookscope.main;

/**
 * Created by yangj on 2018/8/15.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      外部按键右侧CAN百分比消息实体类                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * 【模块定位】                                                                  │
 *   示波器外部按键消息系统 - 右侧CAN总线百分比调节消息封装                         │
 * 【核心职责】                                                                  │
 *   1. 封装右侧CAN百分比调节相关参数（通道、方向、位置、步数）                      │
 *   2. 作为消息载体在按键事件与UI控制层之间传递数据                                │
 * 【架构设计】                                                                  │
 *   简单POJO类，采用私有字段+公共getter/setter的JavaBean模式                       │
 * 【数据流向】                                                                  │
 *   外部按键事件 → 消息封装 → 事件总线 → UI控制层 → 调节执行                        │
 * 【依赖关系】                                                                  │
 *   被依赖：ExternalKeysMsg相关处理类、事件总线、UI控制层                           │
 * 【使用场景】                                                                  │
 *   当用户通过外部按键调节右侧CAN百分比时，创建此消息对象并分发                       │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class ExKeysMsgRightCanPercent {
    /** 是否为S1通道（true=S1, false=S2） */ // 是否为S1通道标识
    private boolean isS1; // 是否为S1通道

    /** 是否为增加操作（true=增加, false=减少） */ // 是否为增加操作标识
    private boolean isAdd; // 是否为增加操作

    /** 是否为顶部位置（true=顶部, false=底部） */ // 是否为顶部位置标识
    private boolean isTop; // 是否为顶部位置

    /** 调节步数/计数 */ // 调节步数值
    private int count; // 调节步数计数

    /**
     * 构造函数：创建右侧CAN百分比调节消息
     *
     * @param isS1   是否为S1通道（true=S1通道, false=S2通道）
     * @param isAdd  是否为增加操作（true=增加百分比, false=减少百分比）
     * @param isTop  是否为顶部位置（true=顶部位置, false=底部位置）
     * @param count  调节步数（单次调节的步进数量）
     */
    public ExKeysMsgRightCanPercent(boolean isS1, boolean isAdd, boolean isTop, int count) {
        this.isS1 = isS1; // 初始化S1通道标识
        this.isAdd = isAdd; // 初始化增加操作标识
        this.isTop = isTop; // 初始化顶部位置标识
        this.count = count; // 初始化调节步数
    }

    /**
     * 获取是否为S1通道
     *
     * @return true表示S1通道，false表示S2通道
     */
    public boolean isS1() {
        return isS1; // 返回S1通道标识
    }

    /**
     * 设置是否为S1通道
     *
     * @param s1 true表示S1通道，false表示S2通道
     */
    public void setS1(boolean s1) {
        isS1 = s1; // 更新S1通道标识
    }

    /**
     * 获取是否为增加操作
     *
     * @return true表示增加操作，false表示减少操作
     */
    public boolean isAdd() {
        return isAdd; // 返回增加操作标识
    }

    /**
     * 设置是否为增加操作
     *
     * @param add true表示增加操作，false表示减少操作
     */
    public void setAdd(boolean add) {
        isAdd = add; // 更新增加操作标识
    }

    /**
     * 获取是否为顶部位置
     *
     * @return true表示顶部位置，false表示底部位置
     */
    public boolean isTop() {
        return isTop; // 返回顶部位置标识
    }

    /**
     * 设置是否为顶部位置
     *
     * @param top true表示顶部位置，false表示底部位置
     */
    public void setTop(boolean top) {
        isTop = top; // 更新顶部位置标识
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