package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean类 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   边沿触发器配置消息数据类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTriggerEdge                                                 ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/5/17                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装边沿触发器的触发源/边沿类型/耦合方式参数                               ║
 * ║  2. 实现ITriggerDetail接口，支持克隆操作                                      ║
 * ║  3. 管理RxMsg选中状态，用于右侧面板识别变更参数                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: ITriggerDetail, TopBeanChannel                                    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTriggerEdge implements ITriggerDetail { // 边沿触发器配置消息数据类 //
    /** 触发源参数，指定哪个通道作为触发源 */ // 触发源 //
    private TopBeanChannel triggerSource; // 触发源 //
    /** 触发边沿类型，如上升沿/下降沿/双边沿 */ // 触发边沿 //
    private TopBeanChannel triggerEdge; // 触发边沿 //
    /** 触发耦合方式，如直流/交流/高频抑制/低频抑制 */ // 触发耦合 //
    private TopBeanChannel triggerCouple; // 触发耦合 //

    /**
     * 克隆当前对象，创建深拷贝
     * @return Object 克隆后的对象
     * @throws CloneNotSupportedException 如果不支持克隆
     */
    @Override // 重写clone方法 //
    public Object clone() throws CloneNotSupportedException { // 克隆方法 //
        TopMsgTriggerEdge topMsgTriggerEdge = (TopMsgTriggerEdge) super.clone(); // 浅拷贝当前对象 //
        topMsgTriggerEdge.triggerSource = (TopBeanChannel) topMsgTriggerEdge.triggerSource.clone(); // 深拷贝触发源 //
        topMsgTriggerEdge.triggerEdge = (TopBeanChannel) topMsgTriggerEdge.triggerEdge.clone(); // 深拷贝触发边沿 //
        topMsgTriggerEdge.triggerCouple = (TopBeanChannel) topMsgTriggerEdge.triggerCouple.clone(); // 深拷贝触发耦合 //
        return topMsgTriggerEdge; // 返回深拷贝对象 //
    } // 方法结束 //

    /**
     * 获取触发源
     * @return TopBeanChannel 触发源
     */
    public TopBeanChannel getTriggerSource() { // 获取触发源 //
        return triggerSource; // 返回触发源 //
    } // 方法结束 //

    /**
     * 设置触发源
     * @param triggerSource 触发源
     */
    public void setTriggerSource(TopBeanChannel triggerSource) { // 设置触发源 //
        if (this.triggerSource == null) { // 如果触发源为空（首次设置） //
            this.triggerSource = triggerSource; // 直接设置 //
        } else { // 如果触发源已存在 //
            this.triggerSource = triggerSource; // 设置触发源 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.triggerSource.setRxMsgSelect(true); // 标记触发源为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取触发边沿
     * @return TopBeanChannel 触发边沿
     */
    public TopBeanChannel getTriggerEdge() { // 获取触发边沿 //
        return triggerEdge; // 返回触发边沿 //
    } // 方法结束 //

    /**
     * 设置触发边沿
     * @param triggerEdge 触发边沿
     */
    public void setTriggerEdge(TopBeanChannel triggerEdge) { // 设置触发边沿 //
        if (this.triggerEdge == null) { // 如果触发边沿为空（首次设置） //
            this.triggerEdge = triggerEdge; // 直接设置 //
        } else { // 如果触发边沿已存在 //
            this.triggerEdge = triggerEdge; // 设置触发边沿 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.triggerEdge.setRxMsgSelect(true); // 标记触发边沿为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取触发耦合
     * @return TopBeanChannel 触发耦合
     */
    public TopBeanChannel getTriggerCouple() { // 获取触发耦合 //
        return triggerCouple; // 返回触发耦合 //
    } // 方法结束 //

    /**
     * 设置触发耦合
     * @param triggerCouple 触发耦合
     */
    public void setTriggerCouple(TopBeanChannel triggerCouple) { // 设置触发耦合 //
        if (this.triggerCouple == null) { // 如果触发耦合为空（首次设置） //
            this.triggerCouple = triggerCouple; // 直接设置 //
        } else { // 如果触发耦合已存在 //
            this.triggerCouple = triggerCouple; // 设置触发耦合 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.triggerCouple.setRxMsgSelect(true); // 标记触发耦合为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 清除所有参数的RxMsg选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态 //
        triggerSource.setRxMsgSelect(false); // 清除触发源选中 //
        triggerEdge.setRxMsgSelect(false); // 清除触发边沿选中 //
        triggerCouple.setRxMsgSelect(false); // 清除触发耦合选中 //
    } // 方法结束 //

    @Override // 重写toString //
    public String toString() { // 转字符串方法 //
        return "TopMsgTriggerEdge{" + // 返回类名 //
                "triggerSource=" + triggerSource + // 包含触发源 //
                ", triggerEdge=" + triggerEdge + // 包含触发边沿 //
                ", triggerCouple=" + triggerCouple + // 包含触发耦合 //
                '}'; // 结束 //
    } // 方法结束 //
} // 类结束 //
