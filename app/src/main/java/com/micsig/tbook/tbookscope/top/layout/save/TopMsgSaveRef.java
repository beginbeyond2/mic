package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → 消息对象 → Ref通道保存消息（Ref Msg）    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：作为Ref通道保存的消息载体，封装目标Ref通道ID和源通道工厂ID，          │
 * │          用于在WAV调用时将波形数据加载到指定的Ref通道                           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：POJO消息类，包含saveToRefId（目标Ref通道ID）和                      │
 * │          fromIdChannelFactory（源通道工厂ID），用于建立源通道到Ref通道的映射     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：TopLayoutInvokeWav(用户选择Ref通道) → TopMsgSaveRef(封装) →        │
 * │          ChannelFactory(根据ID加载Ref波形数据)                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：ChannelFactory                                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在WAV调用页面选择将WAV文件加载到Ref1-Ref8通道时，                │
 * │          通过此消息对象传递目标Ref通道和源通道的ID映射                          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopMsgSaveRef {
    /** 目标Ref通道ID，对应Ref1-Ref8的通道编号 */
    private int saveToRefId; // 保存到Ref通道的ID

    /** 源通道工厂ID，用于从ChannelFactory获取源通道数据 */
    private int fromIdChannelFactory; // 来自通道工厂的ID

    /**
     * 获取目标Ref通道ID
     * @return Ref通道ID
     */
    public int getSaveToRefId() {
        return saveToRefId; // 返回目标Ref通道ID
    }

    /**
     * 设置目标Ref通道ID
     * @param saveToRefId 要设置的Ref通道ID
     */
    public void setSaveToRefId(int saveToRefId) {
        this.saveToRefId = saveToRefId; // 设置目标Ref通道ID
    }

    /**
     * 获取源通道工厂ID
     * @return 通道工厂ID
     */
    public int getFromIdChannelFactory() {
        return fromIdChannelFactory; // 返回源通道工厂ID
    }

    /**
     * 设置源通道工厂ID
     * @param fromIdChannelFactory 要设置的通道工厂ID
     */
    public void setFromIdChannelFactory(int fromIdChannelFactory) {
        this.fromIdChannelFactory = fromIdChannelFactory; // 设置源通道工厂ID
    }

    /**
     * 返回消息对象的字符串表示，用于日志调试
     * @return 包含saveToRefId和fromIdChannelFactory的字符串
     */
    @Override
    public String toString() {
        return "TopMsgSaveRef{" + // 消息类名
                "saveToRefId=" + saveToRefId + // 目标Ref通道ID
                ", fromIdChannelFactory=" + fromIdChannelFactory + // 源通道工厂ID
                '}'; // 结束大括号
    }
}
