package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.ui.bean.RxStringWithSelect; // RxJava字符串选择Bean //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean类 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   斜率触发器配置消息数据类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTriggerSlope                                                 ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/5/17                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装斜率触发器的触发源/边沿/条件/时间上下限参数                            ║
 * ║  2. 实现ITriggerDetail接口，支持克隆操作                                      ║
 * ║  3. 管理RxMsg选中状态，用于右侧面板识别变更参数                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: ITriggerDetail, TopBeanChannel, RxStringWithSelect                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTriggerSlope implements ITriggerDetail { // 斜率触发器配置消息数据类 //
    /** 触发源参数 */ // 触发源 //
    private TopBeanChannel triggerSource; // 触发源 //
    /** 边沿类型，如上升/下降 */ // 边沿 //
    private TopBeanChannel edge; // 边沿 //
    /** 条件参数，如大于/小于/等于 */ // 条件 //
    private TopBeanChannel condition; // 条件 //
    /** 斜率时间上限详情 */ // 斜率时间上限 //
    private RxStringWithSelect slopeTimeHighDetail; // 斜率时间上限详情 //
    /** 斜率时间下限详情 */ // 斜率时间下限 //
    private RxStringWithSelect slopeTimeLowDetail; // 斜率时间下限详情 //

    /**
     * 克隆当前对象，创建深拷贝
     * @return Object 克隆后的对象
     * @throws CloneNotSupportedException 如果不支持克隆
     */
    @Override // 重写clone方法 //
    public Object clone() throws CloneNotSupportedException { // 克隆方法 //
        TopMsgTriggerSlope topMsgTriggerSlope = (TopMsgTriggerSlope) super.clone(); // 浅拷贝当前对象 //
        topMsgTriggerSlope.triggerSource = (TopBeanChannel) topMsgTriggerSlope.triggerSource.clone(); // 深拷贝触发源 //
        topMsgTriggerSlope.edge = (TopBeanChannel) topMsgTriggerSlope.edge.clone(); // 深拷贝边沿 //
        topMsgTriggerSlope.condition = (TopBeanChannel) topMsgTriggerSlope.condition.clone(); // 深拷贝条件 //
        topMsgTriggerSlope.slopeTimeHighDetail = (RxStringWithSelect) topMsgTriggerSlope.slopeTimeHighDetail.clone(); // 深拷贝斜率时间上限 //
        topMsgTriggerSlope.slopeTimeLowDetail = (RxStringWithSelect) topMsgTriggerSlope.slopeTimeLowDetail.clone(); // 深拷贝斜率时间下限 //
        return topMsgTriggerSlope; // 返回深拷贝对象 //
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
     * 获取边沿类型
     * @return TopBeanChannel 边沿
     */
    public TopBeanChannel getEdge() { // 获取边沿 //
        return edge; // 返回边沿 //
    } // 方法结束 //

    /**
     * 设置边沿类型
     * @param edge 边沿
     */
    public void setEdge(TopBeanChannel edge) { // 设置边沿 //
        if (this.edge == null) { // 如果边沿为空（首次设置） //
            this.edge = edge; // 直接设置 //
        } else { // 如果边沿已存在 //
            this.edge = edge; // 设置边沿 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.edge.setRxMsgSelect(true); // 标记边沿为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取条件
     * @return TopBeanChannel 条件
     */
    public TopBeanChannel getCondition() { // 获取条件 //
        return condition; // 返回条件 //
    } // 方法结束 //

    /**
     * 设置条件
     * @param condition 条件
     */
    public void setCondition(TopBeanChannel condition) { // 设置条件 //
        if (this.condition == null) { // 如果条件为空（首次设置） //
            this.condition = condition; // 直接设置 //
        } else { // 如果条件已存在 //
            this.condition = condition; // 设置条件 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.condition.setRxMsgSelect(true); // 标记条件为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取斜率时间上限详情
     * @return RxStringWithSelect 斜率时间上限详情
     */
    public RxStringWithSelect getSlopeTimeHighDetail() { // 获取斜率时间上限 //
        return slopeTimeHighDetail; // 返回斜率时间上限 //
    } // 方法结束 //

    /**
     * 设置斜率时间上限详情
     * @param slopeTimeHighDetail 斜率时间上限字符串
     */
    public void setSlopeTimeHighDetail(String slopeTimeHighDetail) { // 设置斜率时间上限 //
        if (this.slopeTimeHighDetail == null) { // 如果斜率时间上限为空（首次设置） //
            this.slopeTimeHighDetail = new RxStringWithSelect(slopeTimeHighDetail); // 创建新对象 //
        } else { // 如果斜率时间上限已存在 //
            this.slopeTimeHighDetail.setValue(slopeTimeHighDetail); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.slopeTimeHighDetail.setRxMsgSelect(true); // 标记斜率时间上限为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取斜率时间下限详情
     * @return RxStringWithSelect 斜率时间下限详情
     */
    public RxStringWithSelect getSlopeTimeLowDetail() { // 获取斜率时间下限 //
        return slopeTimeLowDetail; // 返回斜率时间下限 //
    } // 方法结束 //

    /**
     * 设置斜率时间下限详情
     * @param slopeTimeLowDetail 斜率时间下限字符串
     */
    public void setSlopeTimeLowDetail(String slopeTimeLowDetail) { // 设置斜率时间下限 //
        if (this.slopeTimeLowDetail == null) { // 如果斜率时间下限为空（首次设置） //
            this.slopeTimeLowDetail = new RxStringWithSelect(slopeTimeLowDetail); // 创建新对象 //
        } else { // 如果斜率时间下限已存在 //
            this.slopeTimeLowDetail.setValue(slopeTimeLowDetail); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.slopeTimeLowDetail.setRxMsgSelect(true); // 标记斜率时间下限为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 清除所有参数的RxMsg选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态 //
        triggerSource.setRxMsgSelect(false); // 清除触发源选中 //
        edge.setRxMsgSelect(false); // 清除边沿选中 //
        condition.setRxMsgSelect(false); // 清除条件选中 //
        slopeTimeHighDetail.setRxMsgSelect(false); // 清除斜率时间上限选中 //
        slopeTimeLowDetail.setRxMsgSelect(false); // 清除斜率时间下限选中 //
    } // 方法结束 //

    @Override // 重写toString //
    public String toString() { // 转字符串方法 //
        return "TopMsgTriggerSlope{" + // 返回类名 //
                "triggerSource=" + triggerSource + // 包含触发源 //
                ", edge=" + edge + // 包含边沿 //
                ", condition=" + condition + // 包含条件 //
                ", slopeTimeHighDetail='" + slopeTimeHighDetail + '\'' + // 包含斜率时间上限 //
                ", slopeTimeLowDetail='" + slopeTimeLowDetail + '\'' + // 包含斜率时间下限 //
                '}'; // 结束 //
    } // 方法结束 //
} // 类结束 //
