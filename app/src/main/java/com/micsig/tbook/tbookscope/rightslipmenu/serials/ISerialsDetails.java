package com.micsig.tbook.tbookscope.rightslipmenu.serials;

/*
 * +--------------------------------------------------------------------------+
 * |                          串口详情数据接口                                  |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— 串口总线配置的数据契约层            |
 * | 核心职责: 定义各串口协议(UART/CAN/LIN/SPI/I2C/M429/M1553B)详情消息的       |
 * |          公共抽象，实现多态传递                                             |
 * | 架构设计: 空标记接口(Marker Interface)，所有 RightMsgSerialsXxx 类实现     |
 * |          该接口，使 RightMsgSerials.serialsDetails 字段能以统一类型        |
 * |          持有不同协议的详情数据                                             |
 * | 数据流向: RightMsgSerialsXxx → ISerialsDetails → RightMsgSerials          |
 * |          → RxBus → 消费方                                                 |
 * | 依赖关系: 无外部依赖，仅作为类型标识                                       |
 * | 使用场景: 在 RightMsgSerials 中以 ISerialsDetails 类型持有具体的           |
 * |          协议详情对象，实现运行时多态                                       |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * 串口详情数据标记接口
 * <p>
 * 所有串口协议详情消息类（如 RightMsgSerialsUart、RightMsgSerialsCan 等）
 * 均实现此接口，以便在 {@link RightMsgSerials} 中以统一类型引用。
 * </p>
 */
public interface ISerialsDetails {
}
