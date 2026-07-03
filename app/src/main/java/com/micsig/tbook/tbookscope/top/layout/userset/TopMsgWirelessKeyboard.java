package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 无线键盘 → 消息载体                                   │
 * │ 核心职责：封装无线键盘的状态数据（序列号、电量、心跳时间戳）                │
 * │ 架构设计：消息数据类，作为RxBus事件载体传递无线键盘状态                     │
 * │ 数据流向：ExternalKeysOnBindService → TopMsgWirelessKeyboard(本类) → RxBus │
 * │ 依赖关系：无外部依赖，纯数据载体                                           │
 * │ 使用场景：无线键盘状态变化时，通过RxBus通知UI更新                          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 无线键盘消息类，封装无线键盘的序列号、电量级别和心跳时间戳。
 * <p>
 * 当无线键盘状态变化时，通过RxBus发送此消息通知相关模块。
 */
public class TopMsgWirelessKeyboard {
    /** 无线键盘序列号 */
    private long sn; // 无线键盘序列号
    /** 电池电量级别 */
    private int batterylevel; // 电池电量级别

    /** 无线键盘电池心跳时间戳 */
    private long wirelessBatteryHeartbeat; // 无线键盘电池心跳时间戳

    /**
     * 构造方法，创建无线键盘消息。
     *
     * @param sn                       无线键盘序列号
     * @param level                    电池电量级别
     * @param wirelessBatteryHeartbeat 电池心跳时间戳
     */
    public TopMsgWirelessKeyboard(long sn, int level, long wirelessBatteryHeartbeat){ // 构造方法
        this.sn = sn; // 赋值序列号
        this.batterylevel = level; // 赋值电量级别
        this.wirelessBatteryHeartbeat = wirelessBatteryHeartbeat; // 赋值心跳时间戳
    }

    /**
     * 获取无线键盘序列号。
     *
     * @return 序列号
     */
    public long getSn() { // 获取序列号
        return sn; // 返回序列号
    }

    /**
     * 获取电池电量级别。
     *
     * @return 电量级别
     */
    public int getBatterylevel() { // 获取电量级别
        return batterylevel; // 返回电量级别
    }
}
