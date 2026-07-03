package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → 消息对象 → WAV保存消息（Wave Msg）      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：作为WAV波形保存功能的消息载体，实现ISaveDetail接口，                 │
 * │          封装WAV保存类型（WAV/CSV/BIN）的启用状态数组，                        │
 * │          用于控制保存页面中各保存类型的可用性                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：POJO消息类，实现ISaveDetail接口，包含saveTypeEnable布尔数组，       │
 * │          数组索引对应保存类型（0=WAV, 1=CSV, 2=BIN），                        │
 * │          通过TopMsgSave在RxBus事件中传递                                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：TopLayoutSaveWav(通道状态变化) → TopMsgSaveWave(封装) →            │
 * │          TopMsgSave(传递) → TopLayoutSave(判断保存类型可用性)                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：ISaveDetail                                                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：在WAV保存页面中，根据当前通道的分段状态，                             │
 * │          通过此消息对象控制WAV/CSV/BIN三种保存类型的启用/禁用状态               │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopMsgSaveWave implements ISaveDetail { // 实现保存详情接口的WAV保存消息类
    /** 保存类型启用状态数组，索引0=WAV, 1=CSV, 2=BIN */
    private boolean[] saveTypeEnable; // 保存类型启用状态数组

    /**
     * 构造方法，初始化保存类型启用状态
     * @param saveTypeEnable 保存类型启用状态数组，索引0=WAV, 1=CSV, 2=BIN
     */
    public TopMsgSaveWave(boolean[] saveTypeEnable) {
        this.saveTypeEnable = saveTypeEnable; // 保存类型启用状态数组
    }

    /**
     * 获取保存类型启用状态数组
     * @return 保存类型启用状态数组
     */
    public boolean[] getSaveTypeEnable() {
        return saveTypeEnable; // 返回保存类型启用状态数组
    }

    /**
     * 按索引设置单个保存类型的启用状态
     * @param index 保存类型索引（0=WAV, 1=CSV, 2=BIN）
     * @param saveTypeEnable true表示启用，false表示禁用
     */
    public void setSaveTypeEnable(int index, boolean saveTypeEnable) {
        this.saveTypeEnable[index] = saveTypeEnable; // 设置指定索引的启用状态
    }

    /**
     * 设置整个保存类型启用状态数组
     * @param saveTypeEnable 保存类型启用状态数组
     */
    public void setSaveTypeEnable(boolean[] saveTypeEnable) {
        this.saveTypeEnable = saveTypeEnable; // 替换整个启用状态数组
    }
}
