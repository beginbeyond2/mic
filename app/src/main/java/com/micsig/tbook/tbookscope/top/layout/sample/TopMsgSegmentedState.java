// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/TopMsgSegmentedState.java
//  核心职责：分段存储状态消息数据类，携带工作模式/滚动/串行总线状态标志
//  架构设计：消息数据类，作为RxBus消息载体，传递分段存储的可用性状态
//  数据流向：时基/触发变更 → TopMsgSegmentedState → TopLayoutSampleSegmented（控制开关可用性）
//  依赖关系：无外部依赖
//  使用场景：时基或触发模式变更时，通知分段存储页面更新开关可用性
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

/**
 * 分段存储状态消息类 - 携带工作模式、滚动模式、串行总线文本状态标志
 */
public class TopMsgSegmentedState { // 分段存储状态消息类
    /** 是否来自EventBus事件 */
    private boolean isFromEventBus; // 事件来源标志
    /** 是否为YT模式 */
    private boolean isYtMode; // YT模式标志
    /** 是否为滚动模式 */
    private boolean isRoll; // 滚动模式标志
    /** 是否为串行总线文本解码模式 */
    private boolean isTxtSerial; // 串行总线文本标志

    /**
     * 构造函数
     * @param isFromEventBus 是否来自EventBus
     */
    public TopMsgSegmentedState(boolean isFromEventBus) { // 构造函数
        this.isFromEventBus = isFromEventBus; // 保存事件来源标志
    }

    /**
     * 判断消息是否来自EventBus
     * @return true表示来自EventBus，false表示来自用户操作
     */
    public boolean isFromEventBus() { // 判断是否来自EventBus
        return isFromEventBus; // 返回事件来源标志
    }

    /**
     * 设置消息来源标志
     * @param fromEventBus true表示来自EventBus，false表示来自用户操作
     */
    public void setFromEventBus(boolean fromEventBus) { // 设置事件来源标志
        isFromEventBus = fromEventBus; // 保存事件来源标志
    }

    /**
     * 判断是否为YT模式
     * @return true表示YT模式
     */
    public boolean isYtMode() { // 判断是否为YT模式
        return isYtMode; // 返回YT模式标志
    }

    /**
     * 设置YT模式标志
     * @param ytMode true表示YT模式
     */
    public void setYtMode(boolean ytMode) { // 设置YT模式标志
        isYtMode = ytMode; // 保存YT模式标志
    }

    /**
     * 判断是否为滚动模式
     * @return true表示滚动模式
     */
    public boolean isRoll() { // 判断是否为滚动模式
        return isRoll; // 返回滚动模式标志
    }

    /**
     * 设置滚动模式标志
     * @param roll true表示滚动模式
     */
    public void setRoll(boolean roll) { // 设置滚动模式标志
        isRoll = roll; // 保存滚动模式标志
    }

    /**
     * 判断是否为串行总线文本解码模式
     * @return true表示串行总线文本模式
     */
    public boolean isTxtSerial() { // 判断是否为串行总线文本模式
        return isTxtSerial; // 返回串行总线文本标志
    }

    /**
     * 设置串行总线文本解码模式标志
     * @param txtSerial true表示串行总线文本模式
     */
    public void setTxtSerial(boolean txtSerial) { // 设置串行总线文本标志
        isTxtSerial = txtSerial; // 保存串行总线文本标志
    }
}
