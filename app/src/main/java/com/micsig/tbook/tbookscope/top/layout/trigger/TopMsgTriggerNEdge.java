package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.ui.bean.RxStringWithSelect; // RxJava字符串选择Bean //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean类 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   N边沿触发器配置消息数据类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTriggerNEdge                                                 ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/5/17                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装N边沿触发器的触发源/边沿类型/时间/详情参数                             ║
 * ║  2. 实现ITriggerDetail接口，支持克隆操作                                      ║
 * ║  3. 管理RxMsg选中状态，用于右侧面板识别变更参数                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: ITriggerDetail, TopBeanChannel, RxStringWithSelect                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTriggerNEdge implements ITriggerDetail { // N边沿触发器配置消息数据类 //
    /** 触发源参数 */ // 触发源 //
    private TopBeanChannel triggerSource; // 触发源 //
    /** N边沿斜率类型 */ // N边沿斜率 //
    private TopBeanChannel nEdgeSlope; // N边沿斜率 //
    /** N边沿时间 */ // N边沿时间 //
    private RxStringWithSelect nEdgeTime; // N边沿时间 //
    /** N边沿详情 */ // N边沿详情 //
    private RxStringWithSelect nEdgeDetail; // N边沿详情 //

    /**
     * 克隆当前对象，创建深拷贝
     * @return Object 克隆后的对象
     * @throws CloneNotSupportedException 如果不支持克隆
     */
    @Override // 重写clone方法 //
    public Object clone() throws CloneNotSupportedException { // 克隆方法 //
        TopMsgTriggerNEdge topMsgTriggerNEdge = (TopMsgTriggerNEdge) super.clone(); // 浅拷贝当前对象 //
        topMsgTriggerNEdge.triggerSource = (TopBeanChannel) topMsgTriggerNEdge.triggerSource.clone(); // 深拷贝触发源 //
        topMsgTriggerNEdge.nEdgeSlope = (TopBeanChannel) topMsgTriggerNEdge.nEdgeSlope.clone(); // 深拷贝N边沿斜率 //
        topMsgTriggerNEdge.nEdgeTime = (RxStringWithSelect) topMsgTriggerNEdge.nEdgeTime.clone(); // 深拷贝N边沿时间 //
        topMsgTriggerNEdge.nEdgeDetail = (RxStringWithSelect) topMsgTriggerNEdge.nEdgeDetail.clone(); // 深拷贝N边沿详情 //
        return topMsgTriggerNEdge; // 返回深拷贝对象 //
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
     * 获取N边沿斜率
     * @return TopBeanChannel N边沿斜率
     */
    public TopBeanChannel getnEdgeSlope() { // 获取N边沿斜率 //
        return nEdgeSlope; // 返回N边沿斜率 //
    } // 方法结束 //

    /**
     * 设置N边沿斜率
     * @param nEdgeSlope N边沿斜率
     */
    public void setnEdgeSlope(TopBeanChannel nEdgeSlope) { // 设置N边沿斜率 //
        if (this.nEdgeSlope == null) { // 如果N边沿斜率为空（首次设置） //
            this.nEdgeSlope = nEdgeSlope; // 直接设置 //
        } else { // 如果N边沿斜率已存在 //
            this.nEdgeSlope = nEdgeSlope; // 设置N边沿斜率 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.nEdgeSlope.setRxMsgSelect(true); // 标记N边沿斜率为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取N边沿时间
     * @return RxStringWithSelect N边沿时间
     */
    public RxStringWithSelect getnEdgeTime() { // 获取N边沿时间 //
        return nEdgeTime; // 返回N边沿时间 //
    } // 方法结束 //

    /**
     * 设置N边沿时间
     * @param nEdgeTime N边沿时间字符串
     */
    public void setnEdgeTime(String nEdgeTime) { // 设置N边沿时间 //
        if (this.nEdgeTime == null) { // 如果N边沿时间为空（首次设置） //
            this.nEdgeTime = new RxStringWithSelect(nEdgeTime); // 创建新对象 //
        } else { // 如果N边沿时间已存在 //
            this.nEdgeTime.setValue(nEdgeTime); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.nEdgeTime.setRxMsgSelect(true); // 标记N边沿时间为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取N边沿详情
     * @return RxStringWithSelect N边沿详情
     */
    public RxStringWithSelect getnEdgeDetail() { // 获取N边沿详情 //
        return nEdgeDetail; // 返回N边沿详情 //
    } // 方法结束 //

    /**
     * 设置N边沿详情
     * @param nEdgeDetail N边沿详情字符串
     */
    public void setnEdgeDetail(String nEdgeDetail) { // 设置N边沿详情 //
        if (this.nEdgeDetail == null) { // 如果N边沿详情为空（首次设置） //
            this.nEdgeDetail = new RxStringWithSelect(nEdgeDetail); // 创建新对象 //
        } else { // 如果N边沿详情已存在 //
            this.nEdgeDetail.setValue(nEdgeDetail); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.nEdgeDetail.setRxMsgSelect(true); // 标记N边沿详情为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 清除所有参数的RxMsg选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态 //
        triggerSource.setRxMsgSelect(false); // 清除触发源选中 //
        nEdgeSlope.setRxMsgSelect(false); // 清除N边沿斜率选中 //
        nEdgeTime.setRxMsgSelect(false); // 清除N边沿时间选中 //
        nEdgeDetail.setRxMsgSelect(false); // 清除N边沿详情选中 //
    } // 方法结束 //

    @Override // 重写toString //
    public String toString() { // 转字符串方法 //
        return "TopMsgTriggerNEdge{" + // 返回类名 //
                "triggerSource=" + triggerSource + // 包含触发源 //
                ", nEdgeSlope=" + nEdgeSlope + // 包含N边沿斜率 //
                ", nEdgeTime='" + nEdgeTime + '\'' + // 包含N边沿时间 //
                ", nEdgeDetail='" + nEdgeDetail + '\'' + // 包含N边沿详情 //
                '}'; // 结束 //
    } // 方法结束 //
} // 类结束 //
