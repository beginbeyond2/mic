package com.micsig.tbook.tbookscope.middleware.mq;

import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                        MQBase — MQ消息抽象基类                         ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：middleware层 · 消息队列(MQ)子系统 · 消息基类                    ║
 * ║ 核心职责：定义所有MQ消息的公共字段（rxType / mqType / chan），            ║
 * ║          为RxBus消息传递提供统一数据载体                                  ║
 * ║ 架构设计：抽象基类模式，子类（MsgChOpenClose / MsgChActiveChange等）       ║
 * ║          继承本类获得消息公共属性，通过RxBus进行进程内事件广播              ║
 * ║ 数据流向：Command层 → MQBase子类封装 → RxBus.post() → UI层订阅消费       ║
 * ║ 依赖关系：RxEnum（RxBus消息路由枚举）、MQEnum（MQ消息类型枚举）、          ║
 * ║          IChan（通道标识接口）                                            ║
 * ║ 使用场景：任何需要通过RxBus在middleware与UI之间传递消息的场景，            ║
 * ║          如通道开关、通道激活变更、触发器参数变更等                        ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * @auother Liwb
 * @description: MQ消息抽象基类，定义消息的公共字段与访问方法
 * @data:2024-2-29 10:52
 */
public abstract class MQBase {

    /** RxBus消息路由类型，用于确定消息在哪个RxBus通道上广播 */ // RxBus路由类型字段
    protected RxEnum rxType; // RxBus路由枚举，标识消息所属的事件通道

    /** MQ消息类型，标识具体的消息语义（如CH_OPEN、CH_CLOSE、CH_ACTIVE等） */ // MQ消息类型字段
    protected MQEnum mqType; // MQ消息类型枚举，标识消息的具体语义

    /** 通道标识，标识消息关联的通道（如CH1~CH8、Math1~Math8、R1~R8、S1~S4） */ // 通道标识字段
    protected IChan chan; // 通道标识，指向消息所关联的具体通道

    /** 是否来自EventBus事件的标志位，默认false表示非EventBus来源 */ // EventBus来源标志
    public boolean isFromEventBus = false; // 标识此消息是否由EventBus事件触发，true表示来自EventBus

    /**
     * 默认无参构造方法
     * <p>子类实例化时调用，字段保持默认值（null / false）</p>
     */
    public MQBase(){ // 无参构造方法，子类通过super()调用

    }

    /**
     * 带参构造方法，初始化消息类型与通道
     * @param type MQ消息类型枚举值
     * @param chan 通道标识
     */
    public MQBase(MQEnum type, IChan chan){ // 带参构造方法，指定消息类型和关联通道
        this.mqType =type; // 设置MQ消息类型
        this.chan=chan; // 设置关联通道标识
    }

    /**
     * 获取MQ消息类型
     * @return 当前消息的MQEnum类型
     */
    public MQEnum getMqType() { // 获取MQ消息类型枚举值
        return mqType; // 返回mqType字段
    }

    /**
     * 获取RxBus路由类型
     * @return 当前消息的RxEnum路由类型
     */
    public RxEnum getRxType() { // 获取RxBus路由枚举值
        return rxType; // 返回rxType字段
    }

    /**
     * 设置RxBus路由类型
     * @param rxType RxBus路由类型枚举值
     */
    public void setRxType(RxEnum rxType) { // 设置RxBus路由类型
        this.rxType = rxType; // 赋值rxType字段
    }

    /**
     * 设置MQ消息类型
     * @param mqType MQ消息类型枚举值
     */
    public void setMqType(MQEnum mqType) { // 设置MQ消息类型
        this.mqType = mqType; // 赋值mqType字段
    }

    /**
     * 获取关联通道标识
     * @return 当前消息关联的IChan通道
     */
    public IChan getChan() { // 获取关联通道标识
        return chan; // 返回chan字段
    }

    /**
     * 设置关联通道标识
     * @param chan 通道标识
     */
    public void setChan(IChan chan) { // 设置关联通道标识
        this.chan = chan; // 赋值chan字段
    }

    /**
     * 重写toString方法，输出消息的调试信息
     * @return 包含rxType、mqType、chan的字符串表示
     */
    @Override
    public String toString() { // 重写toString，便于日志调试
        final StringBuilder sb = new StringBuilder("MQBase{"); // 创建StringBuilder，起始为"MQBase{"
        sb.append("rxType=").append(rxType); // 拼接rxType字段
        sb.append(", mqType=").append(mqType); // 拼接mqType字段
        sb.append(", chan=").append(chan); // 拼接chan字段
        sb.append('}'); // 拼接右花括号
        return sb.toString(); // 返回完整字符串
    }
}
