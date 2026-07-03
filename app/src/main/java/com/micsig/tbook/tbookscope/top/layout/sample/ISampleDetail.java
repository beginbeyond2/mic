// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/ISampleDetail.java
//  核心职责：采样详情的空接口，作为各采样子页面消息数据的统一类型标记
//  架构设计：空接口，用于多态消息传递，类似IMeasureDetail
//  数据流向：各子页面消息类实现此接口 → TopMsgSample引用 → RxBus传递
//  依赖关系：无外部依赖
//  使用场景：采样子页面（模式/记录长度/分段存储）的消息类统一实现此接口
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

/**
 * 采样详情接口 - 空接口，作为各采样子页面消息数据的统一类型标记
 * Created by yangj on 2017/5/16.
 */
public interface ISampleDetail { // 采样详情空接口
}
