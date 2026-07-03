package com.micsig.tbook.tbookscope.wavezone.trigger;

/*
 * +=============================================================================+
 * |                       MainWaveMsgTriggerTimeBase                            |
 * +=============================================================================+
 * | 模块定位 : 主波形触发时基消息数据载体                                          |
 * | 核心职责 : 封装触发时基的X轴位置值，作为触发时间基准信息在模块间传递              |
 * | 架构设计 : 纯数据Bean，无业务逻辑，仅持有一个long型X坐标                        |
 * | 数据流向 : 底层硬件/FPGA数据 → 本Bean → 主波形模块读取                        |
 * | 依赖关系 : 无外部依赖                                                        |
 * | 使用场景 : 触发事件发生时，记录和传递触发点的时基位置                           |
 * +=============================================================================+
 */

/**
 * Created by yangj on 2017/5/25.
 * 主波形触发时基消息
 */
public class MainWaveMsgTriggerTimeBase {
    /** 触发时基的X轴位置值 */
    private long x; // // 触发时基的X轴坐标，对应时间轴上的位置

    /**
     * 构造方法，初始化触发时基X坐标
     * @param x 触发时基的X轴位置值
     */
    public MainWaveMsgTriggerTimeBase(long x) { // // 构造方法，传入X轴坐标
        this.x = x; // // 将参数赋值给成员变量
    }

    /**
     * 获取触发时基的X轴位置值
     * @return X轴位置值
     */
    public long getX() { // // 获取X轴坐标
        return x; // // 返回X轴位置值
    }

    /**
     * 设置触发时基的X轴位置值
     * @param x X轴位置值
     */
    public void setX(long x) { // // 设置X轴坐标
        this.x = x; // // 将参数赋值给成员变量
    }

    /**
     * 返回Bean的字符串描述，用于调试日志输出
     * @return 包含X值的字符串
     */
    @Override
    public String toString() { // // 重写toString，输出X值便于调试
        return "MainWaveMsgTriggerTimeBase{" + // // 拼接类名
                "x=" + x + // // 拼接X轴位置值
                '}'; // // 结束大括号
    }
}
