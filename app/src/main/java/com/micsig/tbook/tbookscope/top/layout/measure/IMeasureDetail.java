// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/IMeasureDetail.java
//  核心职责：定义测量详情的接口契约，作为测量子模块的统一抽象接口
//  架构设计：接口模式，为TopLayoutMeasure各子Fragment提供统一的类型约束
//  数据流向：由具体实现类（如TopLayoutMeasureCommon等）实现此接口
//  依赖关系：被TopLayoutMeasure、TopLayoutMeasureSetting等类引用
//  使用场景：测量详情页面的多态调用，统一管理不同测量子页面的行为
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该接口所属的包路径

/**
 * 测量详情接口 - 定义测量子页面的统一行为契约
 * Created by yangj on 2017/5/16.
 */
public interface IMeasureDetail { // 定义测量详情的公共接口，用于规范各测量子Fragment的行为
} // 接口体结束
