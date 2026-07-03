package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入顶部标题数据Bean

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 显示消息数据封装类                             ║
 * ║  核心职责: 封装顶部Display菜单的消息数据，包含标题和详情两部分                  ║
 * ║  架构设计: 消息传递模式中的消息体，由子Fragment构建后传递给父Fragment            ║
 * ║  数据流向: TopLayoutDisplayXxx → TopMsgDisplay → RxBus → 主界面          ║
 * ║  依赖关系: 依赖TopAllBeanTitle（标题数据）、IDisplayDetail（详情接口）         ║
 * ║  使用场景: 顶部Display菜单切换时，携带当前选中的标题和详情数据通知主界面           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplay { // Display消息封装类，用于在Display子页面与主界面间传递数据

    private TopAllBeanTitle displayTitle; // 当前选中的Display标题项（如常规/波形/方格图等）
    private IDisplayDetail displayDetail; // 当前Display子页面的详情数据（多态引用）
    private boolean isFromEventBus; // 标记消息是否来自EventBus事件（而非用户直接操作）

    /**
     * 判断消息是否来自EventBus事件
     * @return true表示来自EventBus，false表示来自用户直接操作
     */
    public boolean isFromEventBus() { // 获取消息来源标记
        return isFromEventBus; // 返回是否来自EventBus
    }

    /**
     * 设置消息来源标记
     * @param fromEventBus true表示来自EventBus，false表示来自用户直接操作
     */
    public void setFromEventBus(boolean fromEventBus) { // 设置消息来源标记
        isFromEventBus = fromEventBus; // 赋值消息来源标记
    }

    /**
     * 获取当前Display标题项
     * @return 当前选中的标题数据Bean
     */
    public TopAllBeanTitle getDisplayTitle() { // 获取Display标题项
        return displayTitle; // 返回标题数据
    }

    /**
     * 设置Display标题项，非首次设置时自动标记为选中状态
     * @param displayTitle 要设置的标题数据Bean
     */
    public void setDisplayTitle(TopAllBeanTitle displayTitle) { // 设置Display标题项
        if (this.displayTitle == null) { // 首次设置标题
            this.displayTitle = displayTitle; // 直接赋值
        } else { // 非首次设置（切换标题时）
            this.displayTitle = displayTitle; // 更新标题引用
            this.displayTitle.setRxMsgSelect(true); // 标记新标题为选中状态，用于UI高亮
        }
    }

    /**
     * 获取Display详情数据
     * @return 当前Display子页面的详情数据
     */
    public IDisplayDetail getDisplayDetail() { // 获取Display详情数据
        return displayDetail; // 返回详情数据
    }

    /**
     * 设置Display详情数据
     * @param displayDetail 要设置的详情数据（实现IDisplayDetail接口的对象）
     */
    public void setDisplayDetail(IDisplayDetail displayDetail) { // 设置Display详情数据
        this.displayDetail = displayDetail; // 赋值详情数据
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     * @return 包含displayTitle和displayDetail的字符串
     */
    @Override // 重写toString方法
    public String toString() { // 返回字符串表示
        return "TopMsgDisplay{" + // 类名前缀
                "displayTitle=" + displayTitle + // 标题信息
                ", displayDetail=" + displayDetail + // 详情信息
                '}'; // 类名后缀
    }
}
