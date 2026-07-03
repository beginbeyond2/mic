// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/TopMsgSample.java
//  核心职责：采样功能的顶层消息数据类，携带标题栏状态和子页面详情
//  架构设计：消息数据类，作为RxBus消息载体，传递采样功能整体状态
//  数据流向：各子页面 → RxBus → TopLayoutSample（标题栏更新）
//  依赖关系：依赖ISampleDetail接口、TopAllBeanTitle标题数据Bean
//  使用场景：各采样子页面发送状态消息时，作为消息体通过RxBus传递给顶层容器
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题数据Bean

/**
 * 采样消息类 - 携带采样功能的标题栏状态和子页面详情
 */
public class TopMsgSample { // 采样功能顶层消息类
    /** 采样标题栏数据 */
    private TopAllBeanTitle sampleTitle; // 采样标题栏Bean
    /** 采样子页面详情 */
    private ISampleDetail sampleDetail; // 子页面详情接口
    /** 是否来自EventBus事件 */
    private boolean isFromEventBus; // 事件来源标志

    /**
     * 获取采样标题栏数据
     * @return 标题栏Bean
     */
    public TopAllBeanTitle getSampleTitle() { // 获取采样标题栏
        return sampleTitle; // 返回标题栏Bean
    }

    /**
     * 设置采样标题栏数据
     * @param sampleTitle 标题栏Bean
     */
    public void setSampleTitle(TopAllBeanTitle sampleTitle) { // 设置采样标题栏
        if (sampleTitle == null) { // 如果传入标题为空
            this.sampleTitle = sampleTitle; // 直接赋空值
        } else { // 如果传入标题非空
            this.sampleTitle = sampleTitle; // 更新标题
            setAllUnSelect(); // 先清除所有选中状态
            this.sampleTitle.setRxMsgSelect(true); // 标记当前标题为选中
        }
    }

    /**
     * 获取采样子页面详情
     * @return 子页面详情接口
     */
    public ISampleDetail getSampleDetail() { // 获取子页面详情
        return sampleDetail; // 返回子页面详情接口
    }

    /**
     * 设置采样子页面详情
     * @param sampleDetail 子页面详情接口
     */
    public void setSampleDetail(ISampleDetail sampleDetail) { // 设置子页面详情
        this.sampleDetail = sampleDetail; // 保存子页面详情
    }

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
     * 清除标题栏的选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态
        sampleTitle.setRxMsgSelect(false); // 取消标题栏选中
    }

    /**
     * 返回消息的字符串表示
     * @return 包含来源标志、标题和详情的字符串
     */
    @Override
    public String toString() { // 转为字符串
        return "TopMsgSample{" + // 返回消息字符串
                "isFromEventBus=" + isFromEventBus + // 包含来源标志
                ", sampleTitle=" + sampleTitle + // 包含标题栏
                ", sampleDetail=" + sampleDetail + // 包含子页面详情
                '}'; // 结束
    }
}
