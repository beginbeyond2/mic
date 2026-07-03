// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopMsgMeasureStatics.java
//  核心职责：统计测量页面的消息数据类，实现IMeasureDetail接口的空实现
//  架构设计：简单数据类，作为RxBus消息载体，传递统计测量页面状态
//  数据流向：TopLayoutMeasureStatics → RxBus → TopLayoutMeasure（标题栏更新）
//  依赖关系：依赖IMeasureDetail接口
//  使用场景：统计测量页面发送状态消息时，作为消息体通过RxBus传递
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

/**
 * 统计测量消息类 - 实现IMeasureDetail接口的空实现，作为统计测量页面的RxBus消息载体
 */
public class TopMsgMeasureStatics implements IMeasureDetail { // 实现IMeasureDetail接口，统计测量消息类
}
