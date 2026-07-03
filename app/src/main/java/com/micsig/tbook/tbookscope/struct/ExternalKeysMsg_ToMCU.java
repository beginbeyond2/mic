package com.micsig.tbook.tbookscope.struct; // 外部按键消息结构体所在包

/**
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │            ExternalKeysMsg_ToMCU                                    │
 * │          外部按键→MCU LED控制消息封装类                               │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：struct → 消息结构体层                                      │
 * │ 核心职责：封装LED类型和状态，用于向MCU发送LED控制指令                   │
 * │ 架构设计：POJO消息类，由ExternalKeysManager构建并通过串口发送给MCU     │
 * │ 数据流向：ExternalKeysManager → 串口 → MCU固件 → LED硬件             │
 * │ 依赖关系：无外部依赖，纯数据载体                                      │
 * │ 使用场景：示波器前面板按键LED灯状态同步（运行/停止、自动、缩放等）      │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ LED类型常量：TYPE_CH/MATH/REF/SERIAL等 → 对应前面板各按键LED          │
 * │ LED状态常量：STATE_LED_RED/GREEN/ON/OFF → LED颜色和开关               │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * Created by liwb on 2018/1/3.
 */

public class ExternalKeysMsg_ToMCU { // 外部按键→MCU LED控制消息封装类
    public static final String TYPE_CH = "TYPE_CH"; // 通道按键LED类型

    public static final String TYPE_MATH = "TYPE_MATH"; // Math运算按键LED类型
    public static final String TYPE_RUNSTOP = "TYPE_RUNSTOP"; // 运行/停止按键LED类型
    public static final String TYPE_AUTO = "TYPE_AUTO"; // Auto自动按键LED类型
    public static final String TYPE_SEQ = "TYPE_SEQ"; // Seq单次按键LED类型
    public static final String TYPE_FINE = "TYPE_FINE"; // Fine微调按键LED类型
    public static final String TYPE_ZOOM = "TYPE_ZOOM"; // Zoom缩放按键LED类型
    public static final String TYPE_SEARCH = "TYPE_SEARCH"; // Search搜索按键LED类型
    public static final String TYPE_REF = "TYPE_REF"; // Ref参考按键LED类型
    public static final String TYPE_SERIAL = "TYPE_SERIAL"; // Serial串口按键LED类型
    public static final String TYPE_DIGITAL = "TYPE_DIGITAL"; // Digital数字按键LED类型
    public static final String TYPE_WAVEFORM = "TYPE_WAVEFORM"; // Waveform波形按键LED类型
    public static final String TYPE_POWER = "TYPE_POWER"; // Power电源按键LED类型
    public static final String TYPE_TOUCH_OFF = "TYPE_TOUCH_OFF"; // 触摸关闭按键LED类型

    public static final String TYPE_S1 = "TYPE_S1"; // 串口1按键LED类型
    public static final String TYPE_S2 = "TYPE_S2"; // 串口2按键LED类型
    public static final String TYPE_S3 = "TYPE_S3"; // 串口3按键LED类型
    public static final String TYPE_S4 = "TYPE_S4"; // 串口4按键LED类型

    public static final String TYPE_VCURSOR = "TYPE_VCURSOR"; // 垂直光标按键LED类型
    public static final String TYPE_HCURSOR = "TYPE_HCURSOR"; // 水平光标按键LED类型
    public static final String TYPE_ALL = "TYPE_ALL"; // 全部LED类型（批量操作）

    public static final String STATE_LED_RED = "STATE_LED_RED"; // LED状态：红色
    public static final String STATE_LED_GREEN = "STATE_LED_GREEN"; // LED状态：绿色
    public static final String STATE_LED_ON = "STATE_LED_ON"; // LED状态：点亮
    public static final String STATE_LED_OFF = "STATE_LED_OFF"; // LED状态：熄灭

    /**
     * LED类型
     */
    private String ledType; // LED类型，对应前面板具体按键
    /**
     * LED状态
     */
    private String ledState; // LED状态，对应LED颜色和开关

    /** 获取LED类型 */
    public String getLedType() { // 获取LED类型
        return ledType; // 返回LED类型
    }

    /** 设置LED类型 */
    public void setLedType(String ledType) { // 设置LED类型
        this.ledType = ledType; // 赋值LED类型
    }

    /** 获取LED状态 */
    public String getLedState() { // 获取LED状态
        return ledState; // 返回LED状态
    }

    /** 设置LED状态 */
    public void setLedState(String ledState) { // 设置LED状态
        this.ledState = ledState; // 赋值LED状态
    }

    /** 构造方法，同时设置LED类型和状态 */
    public ExternalKeysMsg_ToMCU(String ledType, String ledState) { // 全参构造方法
        this.ledType = ledType; // 赋值LED类型
        this.ledState = ledState; // 赋值LED状态
    }

    /** 返回消息对象的字符串表示，用于调试日志 */
    @Override
    public String toString() { // 重写toString方法
        return "ExternalKeysMsg_ToMCU{" + // 返回消息对象字符串
                "ledType=" + ledType + // 拼接LED类型
                ", ledState=" + ledState + // 拼接LED状态
                '}'; // 字符串结束
    }
}
