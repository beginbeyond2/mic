// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopMsgMeasureCounter.java
//  核心职责：计数器页面的消息数据类，实现IMeasureDetail接口的空实现（已弃用）
//  架构设计：简单数据类，作为RxBus消息载体，传递计数器页面状态
//  数据流向：TopLayoutMeasureCounter → RxBus → TopLayoutMeasure（标题栏更新）
//  依赖关系：依赖IMeasureDetail接口
//  使用场景：计数器页面发送状态消息时，作为消息体通过RxBus传递（当前已被频率计替代）
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

/**
 * 计数器测量消息类 - 实现IMeasureDetail接口的空实现，作为计数器页面的RxBus消息载体（已弃用）
 */
public class TopMsgMeasureCounter implements IMeasureDetail { // 实现IMeasureDetail接口，计数器消息类
}
