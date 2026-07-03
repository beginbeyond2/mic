package com.micsig.tbook.tbookscope.wavezone.measure; // 光标测量功能包，包含YT/XY等不同模式的光标测量实现

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                       CursorMeasure_YT                                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：示波器 Y-T 工作模式下的光标测量组件                                 │
 * │ 核心职责：提供 Y-T 模式（时域）下的光标测量绘制与参数显示功能                    │
 * │ 架构设计：继承 CurSorMeasureBase，采用模板方法模式；基类封装通用的               │
 * │          光标绘制、参数设置、位图渲染逻辑，子类可按需重写 draw() 等方法          │
 * │ 数据流向：外部设置光标参数 → setParam() → draw() 绘制位图 → draw(Canvas)      │
 * │          渲染到屏幕                                                          │
 * │ 依赖关系：继承 CurSorMeasureBase（实现 IMeasure、IWorkMode 接口）              │
 * │          被 CursorMeasureManage 持有和调度                                    │
 * │ 使用场景：Y-T / Y-T ZOOM 模式下显示光标（Y1/Y2/ΔY/X1/X2/ΔX/1/ΔX/S）测量值   │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * <p>本类当前未重写任何方法，直接复用基类 {@link CurSorMeasureBase} 的完整实现，
 * 包括行光标（Y1/Y2/ΔY）、列光标（X1/X2/ΔX/1/ΔX）及面积（S）的绘制逻辑。
 * 对比 {@link CursorMeasure_XY} 重写了 draw() 方法以适配 XY 模式的不同显示需求。</p>
 *
 * Created by liwb on 2017/11/6.
 */
public class CursorMeasure_YT extends CurSorMeasureBase { // Y-T模式光标测量类，继承基类复用全部光标测量与绘制逻辑
}
