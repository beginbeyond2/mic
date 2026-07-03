package com.micsig.tbook.tbookscope.middleware.mq.msg;

import com.micsig.tbook.tbookscope.middleware.mq.MQBase;

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                   MsgChOpenClose — 通道开关消息                          ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：middleware层 · MQ消息子系统 · msg子包                          ║
 * ║ 核心职责：封装通道开关事件的消息载体，通过RxBus广播通道打开/关闭状态变化    ║
 * ║ 架构设计：继承MQBase抽象基类，复用rxType/mqType/chan公共字段，             ║
 * ║          mqType区分CH_OPEN（打开）与CH_CLOSE（关闭）语义                 ║
 * ║ 数据流向：MQChanSelectorManage.eventUIObserver() →                       ║
 * ║          MsgChOpenClose → RxBus.post() → UI层订阅处理                    ║
 * ║ 依赖关系：MQBase（抽象基类）                                              ║
 * ║ 使用场景：当通道打开或关闭时，通过此消息通知UI层更新通道显示状态           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * @auother Liwb
 * @description: 通道开关消息，继承MQBase，用于RxBus广播通道打开/关闭状态变化
 * @data:2024-2-29 11:07
 */
public class MsgChOpenClose extends MQBase { // 继承MQBase，复用rxType/mqType/chan字段

    /**
     * 默认构造方法
     * <p>调用父类MQBase的无参构造方法，字段保持默认值</p>
     */
    public MsgChOpenClose(){ // 无参构造方法
        super(); // 调用父类MQBase的无参构造方法
    }
}
