package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.ui.bean.RxStringWithSelect; // RxJava字符串选择Bean //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean类 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   逻辑触发器配置消息数据类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTriggerLogic                                                ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/5/17                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装逻辑触发器的8通道/逻辑类型/条件/时间/逻辑码参数                        ║
 * ║  2. 实现ITriggerDetail接口，支持克隆操作                                      ║
 * ║  3. 管理RxMsg选中状态，用于右侧面板识别变更参数                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: ITriggerDetail, TopBeanChannel, RxStringWithSelect                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTriggerLogic implements ITriggerDetail { // 逻辑触发器配置消息数据类 //
    /** 通道1逻辑条件 */ // 通道1 //
    private TopBeanChannel ch1; // 通道1逻辑条件 //
    /** 通道2逻辑条件 */ // 通道2 //
    private TopBeanChannel ch2; // 通道2逻辑条件 //
    /** 通道3逻辑条件 */ // 通道3 //
    private TopBeanChannel ch3; // 通道3逻辑条件 //
    /** 通道4逻辑条件 */ // 通道4 //
    private TopBeanChannel ch4; // 通道4逻辑条件 //
    /** 通道5逻辑条件 */ // 通道5 //
    private TopBeanChannel ch5; // 通道5逻辑条件 //
    /** 通道6逻辑条件 */ // 通道6 //
    private TopBeanChannel ch6; // 通道6逻辑条件 //
    /** 通道7逻辑条件 */ // 通道7 //
    private TopBeanChannel ch7; // 通道7逻辑条件 //
    /** 通道8逻辑条件 */ // 通道8 //
    private TopBeanChannel ch8; // 通道8逻辑条件 //
    /** 触发逻辑类型，如与/或/与非/或非 */ // 触发逻辑 //
    private TopBeanChannel triggerLogic; // 触发逻辑类型 //
    /** 触发条件，如大于/小于/等于 */ // 触发条件 //
    private TopBeanChannel condition; // 触发条件 //
    /** 时间上限详情 */ // 时间上限 //
    private RxStringWithSelect timeHighDetail; // 时间上限详情 //
    /** 时间下限详情 */ // 时间下限 //
    private RxStringWithSelect timeLowDetail; // 时间下限详情 //
    /** 逻辑码 */ // 逻辑码 //
    private RxStringWithSelect logic; // 逻辑码 //

    /**
     * 克隆当前对象，创建深拷贝
     * @return Object 克隆后的对象
     * @throws CloneNotSupportedException 如果不支持克隆
     */
    @Override // 重写clone方法 //
    public Object clone() throws CloneNotSupportedException { // 克隆方法 //
        TopMsgTriggerLogic topMsgTriggerLogic = (TopMsgTriggerLogic) super.clone(); // 浅拷贝当前对象 //
        topMsgTriggerLogic.ch1 = (TopBeanChannel) topMsgTriggerLogic.ch1.clone(); // 深拷贝通道1 //
        topMsgTriggerLogic.ch2 = (TopBeanChannel) topMsgTriggerLogic.ch2.clone(); // 深拷贝通道2 //
        topMsgTriggerLogic.ch3 = (TopBeanChannel) topMsgTriggerLogic.ch3.clone(); // 深拷贝通道3 //
        topMsgTriggerLogic.ch4 = (TopBeanChannel) topMsgTriggerLogic.ch4.clone(); // 深拷贝通道4 //
        topMsgTriggerLogic.ch5 = (TopBeanChannel) topMsgTriggerLogic.ch5.clone(); // 深拷贝通道5 //
        topMsgTriggerLogic.ch6 = (TopBeanChannel) topMsgTriggerLogic.ch6.clone(); // 深拷贝通道6 //
        topMsgTriggerLogic.ch7 = (TopBeanChannel) topMsgTriggerLogic.ch7.clone(); // 深拷贝通道7 //
        topMsgTriggerLogic.ch8 = (TopBeanChannel) topMsgTriggerLogic.ch8.clone(); // 深拷贝通道8 //
        topMsgTriggerLogic.triggerLogic = (TopBeanChannel) topMsgTriggerLogic.triggerLogic.clone(); // 深拷贝触发逻辑 //
        topMsgTriggerLogic.condition = (TopBeanChannel) topMsgTriggerLogic.condition.clone(); // 深拷贝触发条件 //
        topMsgTriggerLogic.timeHighDetail = (RxStringWithSelect) topMsgTriggerLogic.timeHighDetail.clone(); // 深拷贝时间上限 //
        topMsgTriggerLogic.timeLowDetail = (RxStringWithSelect) topMsgTriggerLogic.timeLowDetail.clone(); // 深拷贝时间下限 //
        topMsgTriggerLogic.logic = (RxStringWithSelect) topMsgTriggerLogic.logic.clone(); // 深拷贝逻辑码 //
        return topMsgTriggerLogic; // 返回深拷贝对象 //
    } // 方法结束 //

    /**
     * 获取触发源（逻辑触发器无单一触发源，返回null）
     * @return TopBeanChannel null
     */
    @Override // 重写接口方法 //
    public TopBeanChannel getTriggerSource() { // 获取触发源 //
        return null; // 逻辑触发器无单一触发源 //
    } // 方法结束 //

    /**
     * 获取通道1逻辑条件
     * @return TopBeanChannel 通道1
     */
    public TopBeanChannel getCh1() { // 获取通道1 //
        return ch1; // 返回通道1 //
    } // 方法结束 //

    /**
     * 设置通道1逻辑条件
     * @param ch1 通道1逻辑条件
     */
    public void setCh1(TopBeanChannel ch1) { // 设置通道1 //
        if (this.ch1 == null) { // 如果通道1为空（首次设置） //
            this.ch1 = ch1; // 直接设置 //
        } else { // 如果通道1已存在 //
            this.ch1 = ch1; // 设置通道1 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.ch1.setRxMsgSelect(true); // 标记通道1为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取通道2逻辑条件
     * @return TopBeanChannel 通道2
     */
    public TopBeanChannel getCh2() { // 获取通道2 //
        return ch2; // 返回通道2 //
    } // 方法结束 //

    /**
     * 设置通道2逻辑条件
     * @param ch2 通道2逻辑条件
     */
    public void setCh2(TopBeanChannel ch2) { // 设置通道2 //
        if (this.ch2 == null) { // 如果通道2为空（首次设置） //
            this.ch2 = ch2; // 直接设置 //
        } else { // 如果通道2已存在 //
            this.ch2 = ch2; // 设置通道2 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.ch2.setRxMsgSelect(true); // 标记通道2为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取通道3逻辑条件
     * @return TopBeanChannel 通道3
     */
    public TopBeanChannel getCh3() { // 获取通道3 //
        return ch3; // 返回通道3 //
    } // 方法结束 //

    /**
     * 设置通道3逻辑条件
     * @param ch3 通道3逻辑条件
     */
    public void setCh3(TopBeanChannel ch3) { // 设置通道3 //
        if (this.ch3 == null) { // 如果通道3为空（首次设置） //
            this.ch3 = ch3; // 直接设置 //
        } else { // 如果通道3已存在 //
            this.ch3 = ch3; // 设置通道3 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.ch3.setRxMsgSelect(true); // 标记通道3为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取通道4逻辑条件
     * @return TopBeanChannel 通道4
     */
    public TopBeanChannel getCh4() { // 获取通道4 //
        return ch4; // 返回通道4 //
    } // 方法结束 //

    /**
     * 设置通道4逻辑条件
     * @param ch4 通道4逻辑条件
     */
    public void setCh4(TopBeanChannel ch4) { // 设置通道4 //
        if (this.ch4 == null) { // 如果通道4为空（首次设置） //
            this.ch4 = ch4; // 直接设置 //
        } else { // 如果通道4已存在 //
            this.ch4 = ch4; // 设置通道4 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.ch4.setRxMsgSelect(true); // 标记通道4为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取通道5逻辑条件
     * @return TopBeanChannel 通道5
     */
    public TopBeanChannel getCh5() { // 获取通道5 //
        return ch5; // 返回通道5 //
    } // 方法结束 //

    /**
     * 设置通道5逻辑条件
     * @param ch5 通道5逻辑条件
     */
    public void setCh5(TopBeanChannel ch5) { // 设置通道5 //
        if (this.ch5 == null) { // 如果通道5为空（首次设置） //
            this.ch5 = ch5; // 直接设置 //
        } else { // 如果通道5已存在 //
            this.ch5 = ch5; // 设置通道5 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.ch5.setRxMsgSelect(true); // 标记通道5为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取通道6逻辑条件
     * @return TopBeanChannel 通道6
     */
    public TopBeanChannel getCh6() { // 获取通道6 //
        return ch6; // 返回通道6 //
    } // 方法结束 //

    /**
     * 设置通道6逻辑条件
     * @param ch6 通道6逻辑条件
     */
    public void setCh6(TopBeanChannel ch6) { // 设置通道6 //
        if (this.ch6 == null) { // 如果通道6为空（首次设置） //
            this.ch6 = ch6; // 直接设置 //
        } else { // 如果通道6已存在 //
            this.ch6 = ch6; // 设置通道6 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.ch6.setRxMsgSelect(true); // 标记通道6为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取通道7逻辑条件
     * @return TopBeanChannel 通道7
     */
    public TopBeanChannel getCh7() { // 获取通道7 //
        return ch7; // 返回通道7 //
    } // 方法结束 //

    /**
     * 设置通道7逻辑条件
     * @param ch7 通道7逻辑条件
     */
    public void setCh7(TopBeanChannel ch7) { // 设置通道7 //
        if (this.ch7 == null) { // 如果通道7为空（首次设置） //
            this.ch7 = ch7; // 直接设置 //
        } else { // 如果通道7已存在 //
            this.ch7 = ch7; // 设置通道7 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.ch7.setRxMsgSelect(true); // 标记通道7为选中 //
        } // 判断结束 //
    } // 方法结束 //


    /**
     * 获取通道8逻辑条件
     * @return TopBeanChannel 通道8
     */
    public TopBeanChannel getCh8() { // 获取通道8 //
        return ch8; // 返回通道8 //
    } // 方法结束 //

    /**
     * 设置通道8逻辑条件
     * @param ch8 通道8逻辑条件
     */
    public void setCh8(TopBeanChannel ch8) { // 设置通道8 //
        if (this.ch8 == null) { // 如果通道8为空（首次设置） //
            this.ch8 = ch8; // 直接设置 //
        } else { // 如果通道8已存在 //
            this.ch8 = ch8; // 设置通道8 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.ch8.setRxMsgSelect(true); // 标记通道8为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取触发逻辑类型
     * @return TopBeanChannel 触发逻辑类型
     */
    public TopBeanChannel getTriggerLogic() { // 获取触发逻辑 //
        return triggerLogic; // 返回触发逻辑 //
    } // 方法结束 //

    /**
     * 设置触发逻辑类型
     * @param triggerLogic 触发逻辑类型
     */
    public void setTriggerLogic(TopBeanChannel triggerLogic) { // 设置触发逻辑 //
        if (this.triggerLogic == null) { // 如果触发逻辑为空（首次设置） //
            this.triggerLogic = triggerLogic; // 直接设置 //
        } else { // 如果触发逻辑已存在 //
            this.triggerLogic = triggerLogic; // 设置触发逻辑 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.triggerLogic.setRxMsgSelect(true); // 标记触发逻辑为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取触发条件
     * @return TopBeanChannel 触发条件
     */
    public TopBeanChannel getCondition() { // 获取触发条件 //
        return condition; // 返回触发条件 //
    } // 方法结束 //

    /**
     * 设置触发条件
     * @param condition 触发条件
     */
    public void setCondition(TopBeanChannel condition) { // 设置触发条件 //
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
     * 获取逻辑码
     * @return RxStringWithSelect 逻辑码
     */
    public RxStringWithSelect getLogic() { // 获取逻辑码 //
        return logic; // 返回逻辑码 //
    } // 方法结束 //

    /**
     * 设置逻辑码
     * @param logic 逻辑码字符串
     */
    public void setLogic(String logic) { // 设置逻辑码 //
        if (this.logic == null) { // 如果逻辑码为空（首次设置） //
            this.logic = new RxStringWithSelect(logic); // 创建新对象 //
        } else { // 如果逻辑码已存在 //
            this.logic.setValue(logic); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.logic.setRxMsgSelect(true); // 标记逻辑码为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 清除所有参数的RxMsg选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态 //
        ch1.setRxMsgSelect(false); // 清除通道1选中 //
        ch2.setRxMsgSelect(false); // 清除通道2选中 //
        ch3.setRxMsgSelect(false); // 清除通道3选中 //
        ch4.setRxMsgSelect(false); // 清除通道4选中 //
        ch5.setRxMsgSelect(false); // 清除通道5选中 //
        ch6.setRxMsgSelect(false); // 清除通道6选中 //
        ch7.setRxMsgSelect(false); // 清除通道7选中 //
        ch8.setRxMsgSelect(false); // 清除通道8选中 //
        triggerLogic.setRxMsgSelect(false); // 清除触发逻辑选中 //
        condition.setRxMsgSelect(false); // 清除触发条件选中 //
        timeHighDetail.setRxMsgSelect(false); // 清除时间上限选中 //
        timeLowDetail.setRxMsgSelect(false); // 清除时间下限选中 //
        logic.setRxMsgSelect(false); // 清除逻辑码选中 //
    } // 方法结束 //

    @Override // 重写toString //
    public String toString() { // 转字符串方法 //
        return "TopMsgTriggerLogic{" + // 返回类名 //
                "ch1=" + ch1 + // 包含通道1 //
                ", ch2=" + ch2 + // 包含通道2 //
                ", ch3=" + ch3 + // 包含通道3 //
                ", ch4=" + ch4 + // 包含通道4 //
                ", ch5=" + ch5 + // 包含通道5 //
                ", ch6=" + ch6 + // 包含通道6 //
                ", ch7=" + ch7 + // 包含通道7 //
                ", ch8=" + ch8 + // 包含通道8 //
                ", triggerLogic=" + triggerLogic + // 包含触发逻辑 //
                ", condition=" + condition + // 包含触发条件 //
                ", logic='" + logic + // 包含逻辑码 //
                ", timeHighDetail='" + timeHighDetail + // 包含时间上限 //
                ", timeLowDetail='" + timeLowDetail + '\'' + // 包含时间下限 //
                '}'; // 结束 //
    } // 方法结束 //
} // 类结束 //
