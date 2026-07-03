// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopMsgFrequencyMeter.java
//  核心职责：频率计页面的消息数据类，实现IMeasureDetail接口，携带频率计通道和启用状态
//  架构设计：简单数据类，作为RxBus消息载体，传递频率计页面状态
//  数据流向：TopLayoutFrequencyMeter → RxBus → TopLayoutMeasure（标题栏更新）
//  依赖关系：依赖IMeasureDetail接口、TopBeanChannel通道数据Bean
//  使用场景：频率计页面发送状态消息时，作为消息体通过RxBus传递
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean

/**
 * @auother Liwb
 * @description: 频率计消息类 - 实现IMeasureDetail接口，携带频率计通道选择和启用状态
 * @data:2022-2-14 10:26
 */
public class TopMsgFrequencyMeter implements IMeasureDetail { // 实现IMeasureDetail接口，频率计消息类
    /** 频率计通道选择数据 */
    private TopBeanChannel frequencyMeter; // 频率计通道选择Bean
    /** 频率计启用状态 */
    private boolean frequencyEnable; // 频率计启用标志

    /**
     * 获取频率计通道选择数据
     * @return 频率计通道Bean
     */
    public TopBeanChannel getFrequencyMeter() { // 获取频率计通道
        return frequencyMeter; // 返回频率计通道Bean
    }

    /**
     * 设置频率计通道选择数据
     * @param frequencyMeter 频率计通道Bean
     */
    public void setFrequencyMeter(TopBeanChannel frequencyMeter) { // 设置频率计通道
        if (this.frequencyMeter == null) { // 如果当前通道为空（首次设置）
            this.frequencyMeter = frequencyMeter; // 直接赋值
        } else { // 如果当前通道已有值（切换通道）
            this.frequencyMeter = frequencyMeter; // 更新通道
            this.frequencyMeter.setRxMsgSelect(true); // 标记Rx消息选中状态
        }
    }

    /**
     * 判断频率计是否启用
     * @return true表示启用，false表示禁用
     */
    public boolean isFrequencyEnable() { // 判断频率计是否启用
        return frequencyEnable; // 返回启用状态
    }

    /**
     * 设置频率计启用状态
     * @param frequencyEnable true启用，false禁用
     */
    public void setFrequencyEnable(boolean frequencyEnable) { // 设置频率计启用状态
        this.frequencyEnable = frequencyEnable; // 保存启用状态
    }
}
