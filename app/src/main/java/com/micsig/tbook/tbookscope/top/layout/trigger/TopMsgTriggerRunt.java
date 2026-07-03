package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.ui.bean.RxStringWithSelect; // RxJava字符串选择Bean //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean类 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   欠幅触发器配置消息数据类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTriggerRunt                                                  ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/5/17                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装欠幅触发器的触发源/极性/条件/时间上下限参数                            ║
 * ║  2. 实现ITriggerDetail接口，支持克隆操作                                      ║
 * ║  3. 管理RxMsg选中状态，用于右侧面板识别变更参数                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: ITriggerDetail, TopBeanChannel, RxStringWithSelect                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTriggerRunt implements ITriggerDetail { // 欠幅触发器配置消息数据类 //
    /** 触发源参数 */ // 触发源 //
    private TopBeanChannel triggerSource; // 触发源 //
    /** 极性参数，如正脉冲/负脉冲 */ // 极性 //
    private TopBeanChannel polar; // 极性 //
    /** 条件参数，如大于/小于/等于/不等于 */ // 条件 //
    private TopBeanChannel condition; // 条件 //
    /** 时间上限详情 */ // 时间上限 //
    private RxStringWithSelect timeHighDetail; // 时间上限详情 //
    /** 时间下限详情 */ // 时间下限 //
    private RxStringWithSelect timeLowDetail; // 时间下限详情 //

    /**
     * 克隆当前对象，创建深拷贝
     * @return Object 克隆后的对象
     * @throws CloneNotSupportedException 如果不支持克隆
     */
    @Override // 重写clone方法 //
    public Object clone() throws CloneNotSupportedException { // 克隆方法 //
        TopMsgTriggerRunt topMsgTriggerRunt = (TopMsgTriggerRunt) super.clone(); // 浅拷贝当前对象 //
        topMsgTriggerRunt.triggerSource = (TopBeanChannel) topMsgTriggerRunt.triggerSource.clone(); // 深拷贝触发源 //
        topMsgTriggerRunt.polar = (TopBeanChannel) topMsgTriggerRunt.polar.clone(); // 深拷贝极性 //
        topMsgTriggerRunt.condition = (TopBeanChannel) topMsgTriggerRunt.condition.clone(); // 深拷贝条件 //
        topMsgTriggerRunt.timeHighDetail = (RxStringWithSelect) topMsgTriggerRunt.timeHighDetail.clone(); // 深拷贝时间上限 //
        topMsgTriggerRunt.timeLowDetail = (RxStringWithSelect) topMsgTriggerRunt.timeLowDetail.clone(); // 深拷贝时间下限 //
        return topMsgTriggerRunt; // 返回深拷贝对象 //
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
     * 获取极性
     * @return TopBeanChannel 极性
     */
    public TopBeanChannel getPolar() { // 获取极性 //
        return polar; // 返回极性 //
    } // 方法结束 //

    /**
     * 设置极性
     * @param polar 极性
     */
    public void setPolar(TopBeanChannel polar) { // 设置极性 //
        if (this.polar == null) { // 如果极性为空（首次设置） //
            this.polar = polar; // 直接设置 //
        } else { // 如果极性已存在 //
            this.polar = polar; // 设置极性 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.polar.setRxMsgSelect(true); // 标记极性为选中 //
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
     * 获取时间上限详情
     * @return RxStringWithSelect 时间上限详情
     */
    public RxStringWithSelect getTimeHighDetail() { // 获取时间上限 //
        return timeHighDetail; // 返回时间上限 //
    } // 方法结束 //

    /**
     * 设置时间上限详情
     * @param timeHighDetail 时间上限字符串
     */
    public void setTimeHighDetail(String timeHighDetail) { // 设置时间上限 //
        if (this.timeHighDetail == null) { // 如果时间上限为空（首次设置） //
            this.timeHighDetail = new RxStringWithSelect(timeHighDetail); // 创建新对象 //
        } else { // 如果时间上限已存在 //
            this.timeHighDetail.setValue(timeHighDetail); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.timeHighDetail.setRxMsgSelect(true); // 标记时间上限为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取时间下限详情
     * @return RxStringWithSelect 时间下限详情
     */
    public RxStringWithSelect getTimeLowDetail() { // 获取时间下限 //
        return timeLowDetail; // 返回时间下限 //
    } // 方法结束 //

    /**
     * 设置时间下限详情
     * @param timeLowDetail 时间下限字符串
     */
    public void setTimeLowDetail(String timeLowDetail) { // 设置时间下限 //
        if (this.timeLowDetail == null) { // 如果时间下限为空（首次设置） //
            this.timeLowDetail = new RxStringWithSelect(timeLowDetail); // 创建新对象 //
        } else { // 如果时间下限已存在 //
            this.timeLowDetail.setValue(timeLowDetail); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.timeLowDetail.setRxMsgSelect(true); // 标记时间下限为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 清除所有参数的RxMsg选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态 //
        triggerSource.setRxMsgSelect(false); // 清除触发源选中 //
        polar.setRxMsgSelect(false); // 清除极性选中 //
        condition.setRxMsgSelect(false); // 清除条件选中 //
        timeHighDetail.setRxMsgSelect(false); // 清除时间上限选中 //
        timeLowDetail.setRxMsgSelect(false); // 清除时间下限选中 //
    } // 方法结束 //

    @Override // 重写toString //
    public String toString() { // 转字符串方法 //
        return "TopMsgTriggerRunt{" + // 返回类名 //
                "triggerSource=" + triggerSource + // 包含触发源 //
                ", polar=" + polar + // 包含极性 //
                ", condition=" + condition + // 包含条件 //
                ", timeHighDetail='" + timeHighDetail + '\'' + // 包含时间上限 //
                ", timeLowDetail='" + timeLowDetail + '\'' + // 包含时间下限 //
                '}'; // 结束 //
    } // 方法结束 //
} // 类结束 //
