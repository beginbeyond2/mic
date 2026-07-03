package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 标题栏数据Bean类 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     触发器配置消息数据类                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTrigger                                                     ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/5/16                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装触发器配置的标题和详情数据                                             ║
 * ║  2. 支持深拷贝，用于RxBus消息传递                                             ║
 * ║  3. 标记消息来源（EventBus或用户操作）                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向: TopLayoutTrigger → TopMsgTrigger → RxBus → 右侧面板               ║
 * ║  依赖关系: TopAllBeanTitle, ITriggerDetail                                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTrigger implements Cloneable { // 触发器配置消息数据类，支持克隆 //
    /** 触发器标题数据，包含触发器类型索引和名称 */ // 触发器标题 //
    private TopAllBeanTitle triggerTitle; // 触发器标题 //
    /** 触发器详情数据，包含各触发类型的具体参数 */ // 触发器详情 //
    private ITriggerDetail triggerDetail; // 触发器详情 //
    /** 标记消息是否来自EventBus事件 */ // EventBus来源标记 //
    private boolean isFromEventBus; // 是否来自EventBus //

    /**
     * 克隆当前对象，创建深拷贝
     * @return Object 克隆后的TopMsgTrigger对象
     * @throws CloneNotSupportedException 如果对象不支持克隆
     */
    @Override // 重写Object.clone() //
    public Object clone() throws CloneNotSupportedException { // 克隆方法 //
        TopMsgTrigger topMsgTrigger = (TopMsgTrigger) super.clone(); // 浅拷贝当前对象 //
        topMsgTrigger.triggerTitle = (TopAllBeanTitle) topMsgTrigger.triggerTitle.clone(); // 深拷贝标题 //
        topMsgTrigger.triggerDetail = (ITriggerDetail) topMsgTrigger.triggerDetail.clone(); // 深拷贝详情 //
        return topMsgTrigger; // 返回深拷贝对象 //
    } // 方法结束 //

    /**
     * 判断消息是否来自EventBus事件
     * @return boolean true表示来自EventBus，false表示来自用户操作
     */
    public boolean isFromEventBus() { // 判断是否来自EventBus //
        return isFromEventBus; // 返回EventBus来源标记 //
    } // 方法结束 //

    /**
     * 设置消息来源标记
     * @param fromEventBus true表示来自EventBus，false表示来自用户操作
     */
    public void setFromEventBus(boolean fromEventBus) { // 设置EventBus来源标记 //
        isFromEventBus = fromEventBus; // 保存标记值 //
    } // 方法结束 //

    /**
     * 获取触发器标题数据
     * @return TopAllBeanTitle 触发器标题数据
     */
    public TopAllBeanTitle getTriggerTitle() { // 获取触发器标题 //
        return triggerTitle; // 返回标题数据 //
    } // 方法结束 //

    /**
     * 设置触发器标题数据
     * @param triggerTitle 触发器标题数据
     */
    public void setTriggerTitle(TopAllBeanTitle triggerTitle) { // 设置触发器标题 //

        if (this.triggerDetail == null) { // 如果详情为空（首次设置） //
            this.triggerTitle = triggerTitle; // 直接设置标题 //
        } else { // 如果详情已存在 //
            this.triggerTitle = triggerTitle; // 设置标题 //
            this.triggerTitle.setRxMsgSelect(true); // 标记标题为RxMsg选中状态 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取触发器详情数据
     * @return ITriggerDetail 触发器详情数据
     */
    public ITriggerDetail getTriggerDetail() { // 获取触发器详情 //
        return triggerDetail; // 返回详情数据 //
    } // 方法结束 //

    /**
     * 设置触发器详情数据
     * @param triggerDetail 触发器详情数据
     */
    public void setTriggerDetail(ITriggerDetail triggerDetail) { // 设置触发器详情 //
        this.triggerDetail = triggerDetail; // 保存详情数据 //
    } // 方法结束 //

    @Override // 重写Object.toString() //
    public String toString() { // 转字符串方法 //
        return "TopMsgTrigger{" + // 返回类名 //
                "triggerTitle=" + triggerTitle + // 包含标题 //
                ", triggerDetail=" + triggerDetail + // 包含详情 //
                '}'; // 结束 //
    } // 方法结束 //
} // 类结束 //
