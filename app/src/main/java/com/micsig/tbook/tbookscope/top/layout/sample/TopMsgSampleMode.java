// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/TopMsgSampleMode.java
//  核心职责：采样模式子页面的消息数据类，携带采样类型、详情、启用状态
//  架构设计：消息数据类，作为RxBus消息载体，传递采样模式选择和各选项启用状态
//  数据流向：TopLayoutSampleMode → RxBus → TopLayoutSample（标题栏更新）
//  依赖关系：依赖TopBeanChannel通道数据Bean、StrUtil字符串工具
//  使用场景：采样模式页面发送状态消息时，作为消息体通过RxBus传递
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具

/**
 * 采样模式消息类 - 携带采样类型、详情文本、详情索引和各选项启用状态
 * Created by yangj on 2017/5/27.
 */
public class TopMsgSampleMode { // 采样模式消息类
    /** 是否来自EventBus事件 */
    private boolean isFromEventBus; // 事件来源标志
    /** 采样类型选择数据 */
    private TopBeanChannel sample; // 采样类型Bean
    /** 采样详情文本（如平均次数） */
    private String detail; // 详情文本
    /** 采样详情索引 */
    private int sampleDetailIndex; // 详情索引
    /** 各采样选项的启用状态数组（0=普通,1=平均,2=包络,3=峰值） */
    private boolean[] sampleEnable = new boolean[]{true, true, true, true}; // 采样选项启用状态

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
     * 获取采样类型选择数据
     * @return 采样类型Bean
     */
    public TopBeanChannel getSample() { // 获取采样类型
        return sample; // 返回采样类型Bean
    }

    /**
     * 设置采样类型选择数据，并处理英文缩写
     * @param sample 采样类型Bean
     */
    public void setSample(TopBeanChannel sample) { // 设置采样类型
        this.sample = sample; // 保存采样类型
        if (StrUtil.isLangEn() && sample.getIndex() == 1) { // 英文环境下且为平均模式
            sample.setSimpleText(sample.getText().substring(0, 3) + "."); // 截取前3个字符加缩写点
        }else if (StrUtil.isLangEn() && sample.getIndex() == 2){ // 英文环境下且为包络模式
            sample.setSimpleText(sample.getText().substring(0,3)+"."); // 截取前3个字符加缩写点
        } else { // 中文环境或其他模式
            sample.setSimpleText(""); // 清空缩写文本
        }
    }

    /**
     * 获取采样详情文本
     * @return 详情文本
     */
    public String getDetail() { // 获取详情文本
        return detail; // 返回详情文本
    }

    /**
     * 设置采样详情文本
     * @param detail 详情文本
     */
    public void setDetail(String detail) { // 设置详情文本
        this.detail = detail; // 保存详情文本
    }

    /**
     * 获取采样详情索引
     * @return 详情索引
     */
    public int getSampleDetailIndex() { // 获取详情索引
        return sampleDetailIndex; // 返回详情索引
    }

    /**
     * 设置采样详情索引
     * @param sampleDetailIndex 详情索引
     */
    public void setSampleDetailIndex(int sampleDetailIndex) { // 设置详情索引
        this.sampleDetailIndex = sampleDetailIndex; // 保存详情索引
    }

    /**
     * 获取各采样选项的启用状态数组
     * @return 启用状态数组
     */
    public boolean[] getSampleEnable() { // 获取启用状态数组
        return sampleEnable; // 返回启用状态数组
    }

    /**
     * 设置指定索引的采样选项启用状态
     * @param index 选项索引
     * @param sampleEnable 启用状态
     * @return true表示状态发生了变化
     */
    public boolean setSampleEnable(int index, boolean sampleEnable) { // 设置指定选项启用状态
        boolean change = false; // 变化标志
        if (this.sampleEnable[index] != sampleEnable) { // 如果状态不同
            change = true; // 标记有变化
        }
        this.sampleEnable[index] = sampleEnable; // 更新启用状态
        return change; // 返回是否变化
    }

    /**
     * 设置索引1和2的采样选项启用状态
     * @param sample1 索引1启用状态
     * @param sample2 索引2启用状态
     * @return true表示状态发生了变化
     */
    public boolean setSampleEnable(boolean sample1, boolean sample2) { // 设置索引1和2启用状态
        boolean change = false; // 变化标志
        if (this.sampleEnable[1] != sample1) { // 如果索引1状态不同
            change = true; // 标记有变化
        }
        if (this.sampleEnable[2] != sample2) { // 如果索引2状态不同
            change = true; // 标记有变化
        }
        this.sampleEnable[1] = sample1; // 更新索引1启用状态
        this.sampleEnable[2] = sample2; // 更新索引2启用状态
        return change; // 返回是否变化
    }

    /**
     * 设置索引1、2、3的采样选项启用状态
     * @param sample1 索引1启用状态
     * @param sample2 索引2启用状态
     * @param sample3 索引3启用状态
     * @return true表示状态发生了变化
     */
    public boolean setSampleEnable(boolean sample1, boolean sample2, boolean sample3) { // 设置索引1/2/3启用状态
        boolean change = false; // 变化标志
        if (this.sampleEnable[1] != sample1) { // 如果索引1状态不同
            change = true; // 标记有变化
        }
        if (this.sampleEnable[2] != sample2) { // 如果索引2状态不同
            change = true; // 标记有变化
        }
        if (this.sampleEnable[3] != sample3) { // 如果索引3状态不同
            change = true; // 标记有变化
        }
        this.sampleEnable[1] = sample1; // 更新索引1启用状态
        this.sampleEnable[2] = sample2; // 更新索引2启用状态
        this.sampleEnable[3] = sample3; // 更新索引3启用状态
        return change; // 返回是否变化
    }

    /**
     * 设置所有采样选项的启用状态
     * @param sample0 索引0启用状态
     * @param sample1 索引1启用状态
     * @param sample2 索引2启用状态
     * @param sample3 索引3启用状态
     * @return true表示状态发生了变化
     */
    public boolean setSampleEnable(boolean sample0, boolean sample1, boolean sample2, boolean sample3) { // 设置所有选项启用状态
        boolean change = false; // 变化标志
        if (this.sampleEnable[0] != sample0) { // 如果索引0状态不同
            change = true; // 标记有变化
        }
        if (this.sampleEnable[1] != sample1) { // 如果索引1状态不同
            change = true; // 标记有变化
        }
        if (this.sampleEnable[2] != sample2) { // 如果索引2状态不同
            change = true; // 标记有变化
        }
        if (this.sampleEnable[3] != sample3) { // 如果索引3状态不同
            change = true; // 标记有变化
        }
        this.sampleEnable[0] = sample0; // 更新索引0启用状态
        this.sampleEnable[1] = sample1; // 更新索引1启用状态
        this.sampleEnable[2] = sample2; // 更新索引2启用状态
        this.sampleEnable[3] = sample3; // 更新索引3启用状态
        return change; // 返回是否变化
    }

    /**
     * 返回消息的字符串表示
     * @return 包含采样类型、详情和索引的字符串
     */
    @Override
    public String toString() { // 转为字符串
        return "TopMsgSample{" + // 返回消息字符串
                "sample=" + sample + // 包含采样类型
                ", detail='" + detail + '\'' + // 包含详情文本
                ", sampleDetailIndex='" + sampleDetailIndex + '\'' + // 包含详情索引
                '}'; // 结束
    }
}
