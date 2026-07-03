// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopMsgMeasureSetting.java
//  核心职责：测量设置页面的消息数据类，实现IMeasureDetail接口的空实现
//  架构设计：简单数据类，作为RxBus消息载体，传递测量设置页面状态
//  数据流向：TopLayoutMeasureSetting → RxBus → TopLayoutMeasure（标题栏更新）
//  依赖关系：依赖IMeasureDetail接口
//  使用场景：测量设置页面发送状态消息时，作为消息体通过RxBus传递
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

/**
 * 测量设置消息类 - 实现IMeasureDetail接口的空实现，作为测量设置页面的RxBus消息载体
 */
public class TopMsgMeasureSetting implements IMeasureDetail { // 实现IMeasureDetail接口，测量设置消息类
}
