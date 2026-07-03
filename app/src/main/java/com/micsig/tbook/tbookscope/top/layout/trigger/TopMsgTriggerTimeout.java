package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.ui.bean.RxStringWithSelect; // RxJava字符串选择Bean //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean类 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   超时触发器配置消息数据类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTriggerTimeout                                               ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/5/17                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装超时触发器的触发源/极性/超时时间详情参数                               ║
 * ║  2. 实现ITriggerDetail接口，支持克隆操作                                      ║
 * ║  3. 管理RxMsg选中状态，用于右侧面板识别变更参数                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: ITriggerDetail, TopBeanChannel, RxStringWithSelect                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTriggerTimeout implements ITriggerDetail { // 超时触发器配置消息数据类 //
    /** 触发源参数 */ // 触发源 //
    private TopBeanChannel triggerSource; // 触发源 //
    /** 极性参数，如正脉冲/负脉冲 */ // 极性 //
    private TopBeanChannel polar; // 极性 //
    /** 超时时间详情 */ // 超时时间 //
    private RxStringWithSelect overTimeTimeDetail; // 超时时间详情 //

    /**
     * 克隆当前对象，创建深拷贝
     * @return Object 克隆后的对象
     * @throws CloneNotSupportedException 如果不支持克隆
     */
    @Override // 重写clone方法 //
    public Object clone() throws CloneNotSupportedException { // 克隆方法 //
        TopMsgTriggerTimeout topMsgTriggerTimeout = (TopMsgTriggerTimeout) super.clone(); // 浅拷贝当前对象 //
        topMsgTriggerTimeout.triggerSource = (TopBeanChannel) topMsgTriggerTimeout.triggerSource.clone(); // 深拷贝触发源 //
        topMsgTriggerTimeout.polar = (TopBeanChannel) topMsgTriggerTimeout.polar.clone(); // 深拷贝极性 //
        topMsgTriggerTimeout.overTimeTimeDetail = (RxStringWithSelect) topMsgTriggerTimeout.overTimeTimeDetail.clone(); // 深拷贝超时时间 //
        return topMsgTriggerTimeout; // 返回深拷贝对象 //
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
     * 获取超时时间详情
     * @return RxStringWithSelect 超时时间详情
     */
    public RxStringWithSelect getOverTimeTimeDetail() { // 获取超时时间 //
        return overTimeTimeDetail; // 返回超时时间 //
    } // 方法结束 //

    /**
     * 设置超时时间详情
     * @param overTimeTimeDetail 超时时间字符串
     */
    public void setOverTimeTimeDetail(String overTimeTimeDetail) { // 设置超时时间 //
        if (this.overTimeTimeDetail == null) { // 如果超时时间为空（首次设置） //
            this.overTimeTimeDetail = new RxStringWithSelect(overTimeTimeDetail); // 创建新对象 //
        } else { // 如果超时时间已存在 //
            this.overTimeTimeDetail.setValue(overTimeTimeDetail); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.overTimeTimeDetail.setRxMsgSelect(true); // 标记超时时间为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 清除所有参数的RxMsg选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态 //
        triggerSource.setRxMsgSelect(false); // 清除触发源选中 //
        polar.setRxMsgSelect(false); // 清除极性选中 //
        overTimeTimeDetail.setRxMsgSelect(false); // 清除超时时间选中 //
    } // 方法结束 //

    @Override // 重写toString //
    public String toString() { // 转字符串方法 //
        return "TopMsgTriggerTimeout{" + // 返回类名 //
                "triggerSource=" + triggerSource + // 包含触发源 //
                ", polar=" + polar + // 包含极性 //
                ", overTimeTimeDetail='" + overTimeTimeDetail + '\'' + // 包含超时时间 //
                '}'; // 结束 //
    } // 方法结束 //
} // 类结束 //
