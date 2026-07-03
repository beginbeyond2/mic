package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.view.View;

/*
 * +--------------------------------------------------------------------------+
 * |                       串口详情发送消息监听器                                |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— 串口配置变更的回调接口             |
 * | 核心职责: 当某个串口协议详情(UART/CAN/LIN等)配置项发生变更时，             |
 * |          通知上层(RightLayoutSerials)发送消息                              |
 * | 架构设计: 观察者模式的回调接口，由各 RightLayoutSerialsXxx 持有并调用，    |
 * |          RightLayoutSerials 统一实现并处理消息发送                         |
 * | 数据流向: RightLayoutSerialsXxx → OnSerialsDetailSendMsgListener          |
 * |          → RightLayoutSerials.onSerialsDetailSendMsgListener              |
 * |          → sendMsg() → RxBus                                              |
 * | 依赖关系: android.view.View                                               |
 * | 使用场景: 各串口协议子布局（Uart/Can/Lin等）内部参数变更时，              |
 * |          通过此监听器通知父布局 RightLayoutSerials 同步状态并广播          |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * 串口详情发送消息监听器
 * <p>
 * 当串口协议子布局中的参数（如波特率、信号源等）变更时，
 * 通过此回调通知父布局发送消息更新全局状态。
 * </p>
 */
public interface OnSerialsDetailSendMsgListener {

    /**
     * 当串口详情配置项被点击/变更时回调
     *
     * @param detailView    触发变更的详情视图，用于识别具体是哪个协议布局
     * @param isFromEventBus 是否来自EventBus事件（true=外部事件触发，false=用户UI操作触发）
     */
    void onClick(View detailView, boolean isFromEventBus);
}
