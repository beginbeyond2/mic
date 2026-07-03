package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 显示详情接口定义                              ║
 * ║  核心职责: 定义显示详情的统一接口，作为各显示子模块数据模型的抽象基类型          ║
 * ║  架构设计: 策略模式中的策略接口，TopMsgDisplay通过此接口持有具体显示详情       ║
 * ║  数据流向: TopLayoutDisplayXxx → TopMsgDisplay → TopLayoutDisplay       ║
 * ║  依赖关系: 无外部依赖，被TopMsgDisplayCommon/FftInfo/Graticule等实现         ║
 * ║  使用场景: 顶部Display菜单下各子页面的详情数据统一持有与传递                    ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/16.
 */

public interface IDisplayDetail { // 显示详情接口，各显示子模块数据模型的统一抽象
}
