package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   触发器通道变更消息数据类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTriggerChannel                                              ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2018/6/19                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装触发器通道变更事件数据                                                 ║
 * ║  2. 通过RxBus在多个触发器Fragment间同步通道选择                               ║
 * ║  3. 标记事件来源（EventBus或用户操作）                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                    ║
 * ║  触发器Fragment → TopMsgTriggerChannel → RxBus → 其他触发器Fragment           ║
 * ║  依赖关系: TopLayoutTrigger (触发器类型常量)                                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTriggerChannel { // 触发器通道变更消息数据类 //
    /**
     * 触发器类型，取值为TopLayoutTrigger.DETAIL_EDGE/PULSEWIDTH/NEDGE/RUNT/SLOPE/TIMEOUT/VIDEO
     */
    private int triggerType; // 触发器类型索引 //
    /**
     * 通道编号，取值为0,1,2,3
     */
    private int chNumber; // 通道编号 //

    /**
     * 构造方法（默认非EventBus来源）
     * @param triggerType 触发器类型索引
     * @param chNumber 通道编号
     */
    public TopMsgTriggerChannel(int triggerType, int chNumber) { // 构造方法 //
        this(triggerType,chNumber,false); // 委托给三参数构造方法 //
    } // 方法结束 //

    /**
     * 构造方法
     * @param triggerType 触发器类型索引
     * @param chNumber 通道编号
     * @param isFromEventBus 是否来自EventBus事件
     */
    public TopMsgTriggerChannel(int triggerType, int chNumber,boolean isFromEventBus) { // 完整构造方法 //
        this.triggerType = triggerType; // 保存触发器类型 //
        this.chNumber = chNumber; // 保存通道编号 //
        this.isFromEventBus = isFromEventBus; // 保存EventBus来源标记 //
    } // 方法结束 //

    /**
     * 获取触发器类型
     * @return int 触发器类型索引
     */
    public int getTriggerType() { // 获取触发器类型 //
        return triggerType; // 返回触发器类型 //
    } // 方法结束 //

    /**
     * 设置触发器类型
     * @param triggerType 触发器类型索引
     */
    public void setTriggerType(int triggerType) { // 设置触发器类型 //
        this.triggerType = triggerType; // 保存触发器类型 //
    } // 方法结束 //

    /**
     * 获取通道编号
     * @return int 通道编号
     */
    public int getChNumber() { // 获取通道编号 //
        return chNumber; // 返回通道编号 //
    } // 方法结束 //

    /**
     * 设置通道编号
     * @param chNumber 通道编号
     */
    public void setChNumber(int chNumber) { // 设置通道编号 //
        this.chNumber = chNumber; // 保存通道编号 //
    } // 方法结束 //

    @Override // 重写Object.toString() //
    public String toString() { // 转字符串方法 //
        return "TopMsgTriggerChannel{" + // 返回类名 //
                "triggerType=" + triggerType + // 包含触发器类型 //
                ", chNumber=" + chNumber + // 包含通道编号 //
                '}'; // 结束 //
    } // 方法结束 //

    /** 标记消息是否来自EventBus事件 */ // EventBus来源标记 //
    private boolean isFromEventBus; // 是否来自EventBus //

    /**
     * 判断消息是否来自EventBus事件
     * @return boolean true表示来自EventBus
     */
    public boolean isFromEventBus() { // 判断是否来自EventBus //
        return isFromEventBus; // 返回EventBus来源标记 //
    } // 方法结束 //

    /**
     * 设置消息来源标记
     * @param fromEventBus true表示来自EventBus
     */
    public void setFromEventBus(boolean fromEventBus) { // 设置EventBus来源标记 //
        isFromEventBus = fromEventBus; // 保存标记值 //
    } // 方法结束 //
} // 类结束 //
