// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopMsgMeasure.java
//  核心职责：测量功能的顶层消息数据类，携带标题栏状态和子页面详情
//  架构设计：消息数据类，作为RxBus消息载体，传递测量功能整体状态
//  数据流向：各子页面 → RxBus → TopLayoutMeasure（标题栏更新）
//  依赖关系：依赖IMeasureDetail接口、TopAllBeanTitle标题数据Bean
//  使用场景：各测量子页面发送状态消息时，作为消息体通过RxBus传递给顶层容器
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题数据Bean

/**
 * 测量消息类 - 携带测量功能的标题栏状态和子页面详情
 * Created by yangj on 2017/5/16.
 */
public class TopMsgMeasure { // 测量功能顶层消息类
    /** 测量标题栏数据 */
    private TopAllBeanTitle measureTitle; // 测量标题栏Bean
    /** 测量子页面详情 */
    private IMeasureDetail measureDetail; // 子页面详情接口
    /** 是否来自EventBus事件 */
    private boolean isFromEventBus; // 事件来源标志

    /**
     * 判断消息是否来自EventBus
     * @return true表示来自EventBus，false表示来自用户操作
     */
    public boolean isFromEventBus() { // 判断是否来自EventBus
        return isFromEventBus; // 返回事件来源标志
    }

    /**
     * 设置消息来源标志
     * @param fromEventBus true表示来自EventBus，false表示来自用户操作
     */
    public void setFromEventBus(boolean fromEventBus) { // 设置事件来源标志
        isFromEventBus = fromEventBus; // 保存事件来源标志
    }

    /**
     * 获取测量标题栏数据
     * @return 标题栏Bean
     */
    public TopAllBeanTitle getMeasureTitle() { // 获取测量标题栏
        return measureTitle; // 返回标题栏Bean
    }

    /**
     * 设置测量标题栏数据
     * @param measureTitle 标题栏Bean
     */
    public void setMeasureTitle(TopAllBeanTitle measureTitle) { // 设置测量标题栏
        if (this.measureTitle == null) { // 如果当前标题为空（首次设置）
            this.measureTitle = measureTitle; // 直接赋值
        } else { // 如果当前标题已有值（切换标题）
            this.measureTitle = measureTitle; // 更新标题
            this.measureTitle.setRxMsgSelect(true); // 标记Rx消息选中状态
        }
    }

    /**
     * 获取测量子页面详情
     * @return 子页面详情接口
     */
    public IMeasureDetail getMeasureDetail() { // 获取子页面详情
        return measureDetail; // 返回子页面详情接口
    }

    /**
     * 设置测量子页面详情
     * @param measureDetail 子页面详情接口
     */
    public void setMeasureDetail(IMeasureDetail measureDetail) { // 设置子页面详情
        this.measureDetail = measureDetail; // 保存子页面详情
    }

    /**
     * 返回消息的字符串表示
     * @return 包含标题和详情的字符串
     */
    @Override
    public String toString() { // 转为字符串
        return "TopMsgMeasure{" + // 返回消息字符串
                "measureTitle=" + measureTitle + // 包含标题栏
                ", measureDetail=" + measureDetail + // 包含子页面详情
                '}'; // 结束
    }
}
