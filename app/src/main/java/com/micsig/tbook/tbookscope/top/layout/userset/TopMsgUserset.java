package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题数据Bean

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 消息载体 → 数据模型                                  │
 * │ 核心职责：封装用户设置页面的标题选中状态，用于RxBus消息传递                 │
 * │ 架构设计：消息数据类，作为RxBus事件载体在TopLayoutUserset和其他模块间传递  │
 * │ 数据流向：TopLayoutUserset → TopMsgUserset(本类) → RxBus → 其他模块       │
 * │ 依赖关系：TopAllBeanTitle                                                  │
 * │ 使用场景：用户设置标题切换时，通过RxBus通知其他模块当前选中项              │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 用户设置消息类，封装用户设置页面的标题选中状态。
 * <p>
 * 当用户切换设置子页面时，通过RxBus发送此消息通知其他模块。
 * 非首次设置时，会标记为RxMsgSelect，表示需要同步选中状态。
 *
 * @author yangj
 * @since 2017/5/17
 */
public class TopMsgUserset {
    /** 用户设置标题选中项 */
    private TopAllBeanTitle usersetTitle; // 用户设置标题选中项

    /**
     * 获取用户设置标题选中项。
     *
     * @return 标题选中项
     */
    public TopAllBeanTitle getUsersetTitle() { // 获取标题选中项
        return usersetTitle; // 返回标题选中项
    }

    /**
     * 设置用户设置标题选中项。
     * <p>
     * 非首次设置时（usersetTitle不为null），会标记setRxMsgSelect(true)，
     * 表示这是一次需要同步的选中状态变化。
     *
     * @param usersetTitle 标题选中项
     */
    public void setUsersetTitle(TopAllBeanTitle usersetTitle) { // 设置标题选中项
        if (this.usersetTitle == null) { // 首次设置
            this.usersetTitle = usersetTitle; // 直接赋值
        } else { // 非首次设置
            this.usersetTitle = usersetTitle; // 赋值
            this.usersetTitle.setRxMsgSelect(true); // 标记为Rx消息选中，需要同步
        }
    }

    /**
     * 返回对象的字符串表示，用于调试日志。
     *
     * @return 包含usersetTitle的字符串
     */
    @Override // 覆写toString方法
    public String toString() { // 返回字符串表示
        return "TopMsgUserset{" + // 返回类名和左花括号
                "usersetTitle=" + usersetTitle + // 拼接标题选中项
                '}'; // 右花括号
    }
}
