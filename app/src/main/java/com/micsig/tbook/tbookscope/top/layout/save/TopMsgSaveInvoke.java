package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题Bean类

/**
 * Created by yangj on 2017/5/16.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → 消息对象 → 调用消息（Invoke Message）    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：作为调用（Invoke）功能的消息载体，封装当前选中的调用标题、             │
 * │          调用详情和事件来源标识，用于在RxBus事件总线中传递调用状态信息           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：POJO消息类，包含TopAllBeanTitle（标题选中状态）、                   │
 * │          ISaveDetail（调用详情接口）和isFromEventBus（事件来源标识），          │
 * │          通过RxBus在TopLayoutSaveInvoke与子页面之间传递                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：子页面(状态变化) → TopMsgSaveInvoke(封装消息) →                    │
 * │          RxBus.TOPLAYOUT_SAVE(事件发送) → TopLayoutSaveInvoke(接收处理)       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：TopAllBeanTitle, ISaveDetail                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：当用户在调用子页面（Wav/Csv/Setting/Session）操作时，                │
 * │          子页面通过此消息对象将当前状态传递给父级TopLayoutSaveInvoke            │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopMsgSaveInvoke {
    /** 当前选中的调用标题Bean，包含标题文本和选中状态 */
    private TopAllBeanTitle saveTitle; // 调用标题Bean
    /** 当前调用详情，由子页面提供具体的调用参数 */
    private ISaveDetail saveDetail; // 调用详情接口
    /** 是否来自EventBus事件的标识，用于区分用户操作和事件触发 */
    private boolean isFromEventBus; // 是否来自EventBus

    /**
     * 判断消息是否来自EventBus事件
     * @return true表示来自EventBus，false表示来自用户操作
     */
    public boolean isFromEventBus() {
        return isFromEventBus; // 返回事件来源标识
    }

    /**
     * 设置消息是否来自EventBus事件
     * @param fromEventBus true表示来自EventBus，false表示来自用户操作
     */
    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus; // 设置事件来源标识
    }

    /**
     * 获取当前选中的调用标题
     * @return 调用标题Bean
     */
    public TopAllBeanTitle getSaveTitle() {
        return saveTitle; // 返回调用标题
    }

    /**
     * 设置调用标题，如果标题已存在则标记为Rx消息选中
     * @param saveTitle 要设置的调用标题Bean
     */
    public void setSaveTitle(TopAllBeanTitle saveTitle) {
        if (this.saveTitle == null) { // 如果当前标题为空（首次设置）
            this.saveTitle = saveTitle; // 直接设置标题
        } else { // 如果当前标题已存在（更新设置）
            this.saveTitle = saveTitle; // 更新标题引用
            this.saveTitle.setRxMsgSelect(true); // 标记为Rx消息选中状态
        }
    }

    /**
     * 获取调用详情
     * @return 调用详情接口对象
     */
    public ISaveDetail getSaveDetail() {
        return saveDetail; // 返回调用详情
    }

    /**
     * 设置调用详情
     * @param saveDetail 调用详情接口对象
     */
    public void setSaveDetail(ISaveDetail saveDetail) {
        this.saveDetail = saveDetail; // 设置调用详情
    }

    /**
     * 返回消息对象的字符串表示，用于日志调试
     * @return 包含saveTitle和saveDetail的字符串
     */
    @Override
    public String toString() {
        return "TopMsgSaveInvoke{" + // 消息类名
                "saveTitle=" + saveTitle + // 调用标题
                ", saveDetail=" + saveDetail + // 调用详情
                '}'; // 结束大括号
    }
}
