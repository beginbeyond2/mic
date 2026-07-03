package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-5 11:09
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → 消息对象 → 分段保存消息（Segments Msg）  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：作为分段保存功能的消息载体，实现ISaveDetail接口，                    │
 * │          封装分段保存的可见性和各保存类型（WAV/CSV/BIN）的启用状态              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：POJO消息类，实现ISaveDetail接口，包含isVisibleSegments（分段可见）、 │
 * │          isEnableTypeWav（WAV启用）、isEnableTypeCsv（CSV启用）、              │
 * │          isEnableTypeBin（BIN启用）四个布尔状态字段                             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：TopLayoutSaveWav(通道状态变化) → TopMsgSaveSegments(封装) →        │
 * │          TopMsgSave(传递) → TopLayoutSave(判断分段保存可用性)                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：ISaveDetail                                                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：在WAV/CSV/BIN保存页面中，根据当前通道的分段状态，                     │
 * │          通过此消息对象控制分段保存相关UI控件的可见性和启用状态                  │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopMsgSaveSegments implements ISaveDetail { // 实现保存详情接口的分段保存消息类
    /** 分段保存选项是否可见，默认可见 */
    private boolean isVisibleSegments = true; // 分段可见性标志
    /** WAV类型保存是否启用，默认启用 */
    private boolean isEnableTypeWav = true; // WAV类型启用标志
    /** CSV类型保存是否启用，默认启用 */
    private boolean isEnableTypeCsv = true; // CSV类型启用标志
    /** BIN类型保存是否启用，默认启用 */
    private boolean isEnableTypeBin = true; // BIN类型启用标志

    /**
     * 判断分段保存选项是否可见
     * @return true表示可见，false表示不可见
     */
    public boolean isVisibleSegments() {
        return isVisibleSegments; // 返回分段可见性
    }

    /**
     * 设置分段保存选项的可见性
     * @param visibleSegments true表示可见，false表示不可见
     */
    public void setVisibleSegments(boolean visibleSegments) {
        isVisibleSegments = visibleSegments; // 设置分段可见性
    }

    /**
     * 判断WAV类型保存是否启用
     * @return true表示启用，false表示禁用
     */
    public boolean isEnableTypeWav() {
        return isEnableTypeWav; // 返回WAV类型启用状态
    }

    /**
     * 设置WAV类型保存的启用状态
     * @param enableTypeWav true表示启用，false表示禁用
     */
    public void setEnableTypeWav(boolean enableTypeWav) {
        isEnableTypeWav = enableTypeWav; // 设置WAV类型启用状态
    }

    /**
     * 判断CSV类型保存是否启用
     * @return true表示启用，false表示禁用
     */
    public boolean isEnableTypeCsv() {
        return isEnableTypeCsv; // 返回CSV类型启用状态
    }

    /**
     * 设置CSV类型保存的启用状态
     * @param enableTypeCsv true表示启用，false表示禁用
     */
    public void setEnableTypeCsv(boolean enableTypeCsv) {
        isEnableTypeCsv = enableTypeCsv; // 设置CSV类型启用状态
    }

    /**
     * 判断BIN类型保存是否启用
     * @return true表示启用，false表示禁用
     */
    public boolean isEnableTypeBin() {
        return isEnableTypeBin; // 返回BIN类型启用状态
    }

    /**
     * 设置BIN类型保存的启用状态
     * @param enableTypeBin true表示启用，false表示禁用
     */
    public void setEnableTypeBin(boolean enableTypeBin) {
        isEnableTypeBin = enableTypeBin; // 设置BIN类型启用状态
    }

    /**
     * 返回消息对象的字符串表示，用于日志调试
     * @return 包含所有分段保存状态的字符串
     */
    @Override
    public String toString() {
        return "TopMsgSaveSegments{" + // 消息类名
                "isVisibleSegments=" + isVisibleSegments + // 分段可见性
                ", isEnableTypeWav=" + isEnableTypeWav + // WAV启用状态
                ", isEnableTypeCsv=" + isEnableTypeCsv + // CSV启用状态
                ", isEnableTypeBin=" + isEnableTypeBin + // BIN启用状态
                '}'; // 结束大括号
    }
}
