package com.micsig.tbook.tbookscope.top.layout.auto; // 自动功能消息数据类所在包

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入顶部标题选项数据Bean

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：自动功能(Auto)消息数据模型                                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：封装自动功能的标题和详情数据，作为RxBus消息载体                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：消息数据层，组合标题(Title)和详情(Detail)两个维度数据                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：TopLayoutAuto → TopMsgAuto → RxBus → 其他订阅模块                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：持有TopAllBeanTitle和IAutoDetail引用                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：当自动功能页面切换或参数变化时，封装完整消息通过RxBus广播             │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/5/16.
 */

public class TopMsgAuto { // 自动功能消息数据模型

    private TopAllBeanTitle autoTitle; // 自动功能标题选项数据，标识当前选中的子页面（自动设置/自动量程）
    private IAutoDetail autoDetail; // 自动功能详情数据，多态引用，实际为TopMsgAutoSet或TopMsgAutoRange
    private boolean isFromEventBus; // 是否来自EventBus事件，用于区分用户操作和外部事件触发

    /**
     * 判断消息是否来自EventBus事件
     *
     * @return true表示来自EventBus事件，false表示来自用户操作
     */
    public boolean isFromEventBus() { // 判断是否来自EventBus事件
        return isFromEventBus; // 返回EventBus来源标识
    }

    /**
     * 设置消息来源标识
     *
     * @param fromEventBus 是否来自EventBus事件
     */
    public void setFromEventBus(boolean fromEventBus) { // 设置消息来源标识
        isFromEventBus = fromEventBus; // 赋值EventBus来源标识
    }

    /**
     * 获取自动功能标题选项
     *
     * @return 当前选中的标题选项数据Bean
     */
    public TopAllBeanTitle getAutoTitle() { // 获取自动功能标题选项
        return autoTitle; // 返回标题选项数据
    }

    /**
     * 设置自动功能标题选项，同时更新选中状态
     *
     * @param autoTitle 标题选项数据Bean，null表示无选中项
     */
    public void setAutoTitle(TopAllBeanTitle autoTitle) { // 设置自动功能标题选项
        if (autoTitle == null) { // 判断传入的标题是否为空
            this.autoTitle = autoTitle; // 允许设置为null
        } else { // 标题不为空
            this.autoTitle = autoTitle; // 更新标题引用
            this.autoTitle.setRxMsgSelect(true); // 将标题设为选中状态，标识当前激活的子页面
        }
    }

    /**
     * 获取自动功能详情数据
     *
     * @return 自动功能详情数据，实际类型为TopMsgAutoSet或TopMsgAutoRange
     */
    public IAutoDetail getAutoDetail() { // 获取自动功能详情数据
        return autoDetail; // 返回详情数据
    }

    /**
     * 设置自动功能详情数据
     *
     * @param autoDetail 自动功能详情数据，实现IAutoDetail接口的对象
     */
    public void setAutoDetail(IAutoDetail autoDetail) { // 设置自动功能详情数据
        this.autoDetail = autoDetail; // 直接赋值详情数据引用
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     *
     * @return 包含标题和详情数据的字符串
     */
    @Override // 覆写Object的toString方法
    public String toString() { // 返回对象的字符串表示
        return "TopMsgAuto{" + // 返回类名和左花括号
                "autoTitle=" + autoTitle + // 拼接标题选项数据
                ", autoDetail=" + autoDetail + // 拼接详情数据
                '}'; // 拼接右花括号
    }
}
