// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/TopMsgSampleDepth.java
//  核心职责：记录长度子页面的消息数据类，携带记录长度索引和来源标志
//  架构设计：消息数据类，作为RxBus消息载体，传递记录长度选择状态
//  数据流向：TopLayoutSampleDepth → RxBus → TopLayoutSample（标题栏更新）
//  依赖关系：依赖RxIntWithSelect带选中标志的整型Bean
//  使用场景：记录长度页面发送状态消息时，作为消息体通过RxBus传递
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

import com.micsig.tbook.ui.bean.RxIntWithSelect; // 导入带选中标志的整型Bean

/**
 * 记录长度消息类 - 携带记录长度索引和来源标志
 * Created by yangj on 2017/5/27.
 */
public class TopMsgSampleDepth { // 记录长度消息类
    /** 记录长度索引（带选中标志） */
    private RxIntWithSelect depth; // 记录长度索引
    /** 是否来自EventBus事件 */
    private boolean isFromEventBus; // 事件来源标志

    /**
     * 构造函数
     * @param isFromEventBus 是否来自EventBus
     */
    public TopMsgSampleDepth(boolean isFromEventBus) { // 构造函数
        this.isFromEventBus = isFromEventBus; // 保存事件来源标志
    }

    /**
     * 获取记录长度索引
     * @return 带选中标志的整型Bean
     */
    public RxIntWithSelect getDepth() { // 获取记录长度索引
        return depth; // 返回记录长度索引
    }

    /**
     * 设置记录长度索引
     * @param depth 记录长度索引值
     */
    public void setDepth(int depth) { // 设置记录长度索引
        if (this.depth == null) { // 如果当前索引为空（首次设置）
            this.depth = new RxIntWithSelect(depth); // 创建新的带选中标志的整型
        } else { // 如果当前索引已有值
            this.depth.setValue(depth); // 更新索引值
        }
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
}
