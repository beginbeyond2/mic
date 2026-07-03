package com.micsig.tbook.tbookscope.main;

/**
 * Created by yangj on 2018/8/15.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      外部按键时基消息实体类                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * 【模块定位】                                                                  │
 *   示波器外部按键消息系统 - 时基（时间基准）调节消息封装                           │
 * 【核心职责】                                                                  │
 *   1. 封装时基调节相关参数（增加/减少方向、调节步数）                              │
 *   2. 作为消息载体在按键事件与时基控制模块之间传递数据                             │
 * 【架构设计】                                                                  │
 *   简单POJO类，采用私有字段+公共getter/setter的JavaBean模式                       │
 * 【数据流向】                                                                  │
 *   外部按键事件 → 消息封装 → 事件总线 → 时基控制层 → 时基调节执行                  │
 * 【依赖关系】                                                                  │
 *   被依赖：ExternalKeysMsg相关处理类、事件总线、时基控制层                         │
 * 【使用场景】                                                                  │
 *   当用户通过外部按键调节时基（时间/格）时，创建此消息对象并分发                   │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class ExternalKeysMsgTimeBase {
    /** 是否为增加时基操作（true=增加时基, false=减少时基） */ // 时基调节方向标识
    private boolean isAdd; // 是否为增加时基操作

    /** 调节步数/计数 */ // 调节步数字段
    private int count; // 调节步数计数

    /**
     * 构造函数：创建外部按键时基消息
     *
     * @param isAdd 是否为增加时基操作（true=增加时基, false=减少时基）
     * @param count 调节步数（单次调节的步进数量）
     */
    public ExternalKeysMsgTimeBase(boolean isAdd, int count) {
        this.isAdd = isAdd; // 初始化时基调节方向标识
        this.count = count; // 初始化调节步数
    }

    /**
     * 获取是否为增加时基操作
     *
     * @return true表示增加时基，false表示减少时基
     */
    public boolean isAdd() {
        return isAdd; // 返回时基调节方向标识
    }

    /**
     * 设置是否为增加时基操作
     *
     * @param add true表示增加时基，false表示减少时基
     */
    public void setAdd(boolean add) {
        isAdd = add; // 更新时基调节方向标识
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