package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题Bean类

/**
 * Created by yangj on 2017/5/16.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → 消息对象 → 保存子容器消息（Store Msg）   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：作为保存子容器（Store）的消息载体，封装当前选中的保存标题、            │
 * │          保存详情和事件来源标识，用于在RxBus事件总线中传递保存子容器状态信息      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：POJO消息类，包含TopAllBeanTitle（标题选中状态）、                   │
 * │          ISaveDetail（保存详情接口）和isFromEventBus（事件来源标识），          │
 * │          通过RxBus在TopLayoutSaveStore与TopLayoutSave之间传递                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：子页面(Wav/Csv/Bin等状态变化) → TopMsgSaveStore(封装) →            │
 * │          RxBus.TOPLAYOUT_SAVE(事件发送) → TopLayoutSave(接收处理)             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：TopAllBeanTitle, ISaveDetail                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：当用户在保存子页面（Wav/Csv/Bin/Setting/Picture/Session）操作时，    │
 * │          TopLayoutSaveStore通过此消息对象将当前状态传递给父级TopLayoutSave      │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopMsgSaveStore {
    /** 当前选中的保存标题Bean，包含标题文本和选中状态 */
    private TopAllBeanTitle saveTitle; // 保存标题Bean
    /** 当前保存详情，由子页面提供具体的保存参数 */
    private ISaveDetail saveDetail; // 保存详情接口
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
     * 获取当前选中的保存标题
     * @return 保存标题Bean
     */
    public TopAllBeanTitle getSaveTitle() {
        return saveTitle; // 返回保存标题
    }

    /**
     * 设置保存标题，如果标题已存在则标记为Rx消息选中
     * @param saveTitle 要设置的保存标题Bean
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
     * 获取保存详情
     * @return 保存详情接口对象
     */
    public ISaveDetail getSaveDetail() {
        return saveDetail; // 返回保存详情
    }

    /**
     * 设置保存详情
     * @param saveDetail 保存详情接口对象
     */
    public void setSaveDetail(ISaveDetail saveDetail) {
        this.saveDetail = saveDetail; // 设置保存详情
    }

    /**
     * 返回消息对象的字符串表示，用于日志调试
     * @return 包含saveTitle和saveDetail的字符串
     */
    @Override
    public String toString() {
        return "TopMsgSaveStore{" + // 消息类名
                "saveTitle=" + saveTitle + // 保存标题
                ", saveDetail=" + saveDetail + // 保存详情
                '}'; // 结束大括号
    }
}
