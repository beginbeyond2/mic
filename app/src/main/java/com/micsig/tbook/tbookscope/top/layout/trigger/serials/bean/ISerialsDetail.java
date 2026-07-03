package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    ISerialsDetail（串行详情数据标记接口）                       ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/ISerialsDetail.java                                ║
 * ║ 核心职责: 作为所有串行触发详情数据类的标记接口（空接口）                            ║
 * ║ 架构设计: 空标记接口，用于多态统一处理各种串行详情数据                              ║
 * ║ 数据流向: 被TopMsgTriggerSerials持有，传递给上层触发页面                        ║
 * ║ 依赖关系: 无依赖，被所有SerialsDetail*类实现                                   ║
 * ║ 使用场景: 统一串行触发详情数据的类型，便于在TopMsgTriggerSerials中多态传递          ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public interface ISerialsDetail { // 串行详情数据标记接口，所有详情数据类实现此接口
}
