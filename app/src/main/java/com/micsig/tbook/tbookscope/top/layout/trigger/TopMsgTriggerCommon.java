package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.ui.bean.RxStringWithSelect; // RxJava字符串选择Bean //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean类 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   常规触发器配置消息数据类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTriggerCommon                                               ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/5/17                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装常规触发器的抑制时间和触发模式参数                                     ║
 * ║  2. 实现ITriggerDetail接口，支持克隆操作                                      ║
 * ║  3. 管理RxMsg选中状态，用于右侧面板识别变更参数                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: ITriggerDetail, RxStringWithSelect, TopBeanChannel                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTriggerCommon implements ITriggerDetail { // 常规触发器配置消息数据类 //
    /** 抑制时间参数 */ // 抑制时间 //
    private RxStringWithSelect holdoffTime; // 抑制时间 //
    /** 触发模式参数 */ // 触发模式 //
    private TopBeanChannel mode; // 触发模式 //

    /**
     * 克隆当前对象，创建深拷贝
     * @return Object 克隆后的对象
     * @throws CloneNotSupportedException 如果不支持克隆
     */
    @Override // 重写clone方法 //
    public Object clone() throws CloneNotSupportedException { // 克隆方法 //
        TopMsgTriggerCommon topMsgTriggerCommon = (TopMsgTriggerCommon) super.clone(); // 浅拷贝 //
        topMsgTriggerCommon.holdoffTime = (RxStringWithSelect) topMsgTriggerCommon.holdoffTime.clone(); // 深拷贝抑制时间 //
        topMsgTriggerCommon.mode = (TopBeanChannel) topMsgTriggerCommon.mode.clone(); // 深拷贝触发模式 //
        return topMsgTriggerCommon; // 返回深拷贝对象 //
    } // 方法结束 //

    /**
     * 获取抑制时间
     * @return RxStringWithSelect 抑制时间
     */
    public RxStringWithSelect getHoldoffTime() { // 获取抑制时间 //
        return holdoffTime; // 返回抑制时间 //
    } // 方法结束 //

    /**
     * 获取触发源（常规触发器无触发源，返回null）
     * @return TopBeanChannel null
     */
    @Override // 重写接口方法 //
    public TopBeanChannel getTriggerSource() { // 获取触发源 //
        return null; // 常规触发器无触发源 //
    } // 方法结束 //

    /**
     * 设置抑制时间
     * @param holdoffTime 抑制时间字符串
     */
    public void setHoldoffTime(String holdoffTime) { // 设置抑制时间 //
        if (this.holdoffTime == null) { // 如果抑制时间为空（首次设置） //
            this.holdoffTime = new RxStringWithSelect(holdoffTime); // 创建新对象 //
        } else { // 如果抑制时间已存在 //
            this.holdoffTime.setValue(holdoffTime); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.holdoffTime.setRxMsgSelect(true); // 标记抑制时间为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取触发模式
     * @return TopBeanChannel 触发模式
     */
    public TopBeanChannel getMode() { // 获取触发模式 //
        return mode; // 返回触发模式 //
    } // 方法结束 //

    /**
     * 设置触发模式
     * @param mode 触发模式
     */
    public void setMode(TopBeanChannel mode) { // 设置触发模式 //
        if (this.mode == null) { // 如果模式为空（首次设置） //
            this.mode = mode; // 直接设置 //
        } else { // 如果模式已存在 //
            this.mode = mode; // 设置模式 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.mode.setRxMsgSelect(true); // 标记模式为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 清除所有参数的RxMsg选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态 //
        holdoffTime.setRxMsgSelect(false); // 清除抑制时间选中 //
        mode.setRxMsgSelect(false); // 清除模式选中 //
    } // 方法结束 //

    @Override // 重写toString //
    public String toString() { // 转字符串方法 //
        return "TopMsgTriggerCommon{" + // 返回类名 //
                "holdoffTime='" + holdoffTime + '\'' + // 包含抑制时间 //
                ", mode=" + mode + // 包含模式 //
                '}'; // 结束 //
    } // 方法结束 //
} // 类结束 //
