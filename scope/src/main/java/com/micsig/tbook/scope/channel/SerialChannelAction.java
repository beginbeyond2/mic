package com.micsig.tbook.scope.channel;  // 定义包名：示波器通道管理模块

import com.micsig.tbook.scope.Action.FPGAMessage;  // 导入FPGAMessage类：FPGA消息常量定义
import com.micsig.tbook.scope.Action.XAction;  // 导入XAction类：动作代理基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂

/**
 * 串口通道动作代理类 - 串口通道消息发送与事件分发
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 通道动作代理</li>
 *   <li>设计模式：代理模式 + 继承模式</li>
 *   <li>职责类型：串口通道状态变化的消息发送、事件分发</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>处理串口通道总线类型变化时的消息发送</li>
 *   <li>通知FPGA更新总线类型配置</li>
 *   <li>通知UI层更新总线类型显示</li>
 * </ul>
 * 
 * <p><b>总线类型架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   串口通道支持的串行总线类型                                               │
 * │                                                                          │
 * │   SerialChannel                                                          │
 * │       │                                                                   │
 * │       ├── UartBus        通用异步收发传输器（UART）                        │
 * │       │                                                                   │
 * │       ├── LinBus         局域互联网络（LIN）                              │
 * │       │                                                                   │
 * │       ├── CanBus         控制器局域网（CAN）                              │
 * │       │                                                                   │
 * │       ├── SpiBus         串行外设接口（SPI）                              │
 * │       │                                                                   │
 * │       ├── I2CBus         两线式串行总线（I2C）                            │
 * │       │                                                                   │
 * │       ├── ARINC429Bus    航空电子数字总线（ARINC429）                     │
 * │       │                                                                   │
 * │       └── MILSTD1553BBus 军用标准总线（MIL-STD-1553B）                    │
 * │                                                                          │
 * │   总线类型切换流程：                                                      │
 * │   setBusType(busType)                                                    │
 * │       │                                                                   │
 * │       ▼                                                                   │
 * │   SerialChannelAction.busTypeChange()                                    │
 * │       │                                                                   │
 * │       ├── sendChSample()         更新通道采样状态                        │
 * │       ├── sendFpgaMsg()          通知FPGA更新总线类型                    │
 * │       └── sendEvent()            通知UI更新显示                          │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   XAction（动作代理基类）
 *       │
 *       └── SerialChannelAction（串口通道动作代理）
 *               │
 *               └── 继承sendFpgaMsg、sendEvent、sendChSample等方法
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>串口通道打开时调用busTypeChange()通知系统</li>
 *   <li>串口通道关闭时调用busTypeChange()通知系统</li>
 *   <li>切换总线类型时调用busTypeChange()更新配置</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：SerialChannel（串口通道，持有此Action引用）</li>
 *   <li>依赖：XAction（动作代理基类，提供消息发送方法）</li>
 *   <li>依赖：FPGAMessage（FPGA消息常量）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see SerialChannel 串口通道
 * @see XAction 动作代理基类
 * @see IBus 总线接口
 */
public class SerialChannelAction extends XAction {  // 继承XAction动作代理基类

    /** 关联的串口通道引用 */
    private SerialChannel channel;  // 持有SerialChannel引用，用于获取通道信息

    /**
     * 构造方法：初始化串口通道动作代理
     * 
     * <p>保存串口通道引用，用于后续操作。
     * 
     * @param channel 关联的串口通道对象
     */
    public SerialChannelAction(SerialChannel channel){
        this.channel = channel;  // 保存串口通道引用
    }

    /**
     * 总线类型变化处理
     * 
     * <p>当串口通道的总线类型发生变化时调用，执行以下操作：
     * <ol>
     *   <li>发送通道采样变化消息，更新采样状态</li>
     *   <li>发送FPGA命令，通知FPGA更新总线类型配置</li>
     *   <li>发送事件，通知UI层更新总线类型显示</li>
     * </ol>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>SerialChannel.setBusType() - 切换总线类型时</li>
     *   <li>SerialChannel.open() - 打开通道时</li>
     *   <li>SerialChannel.close() - 关闭通道时</li>
     * </ul>
     */
    public void busTypeChange(){
        sendChSample();  // 发送通道采样变化消息，更新硬件采样配置
        sendFpgaMsg(0, FPGAMessage.FPGA_CMD_BUS_TYPE);  // 发送FPGA命令，通知FPGA更新总线类型
        sendEvent(EventFactory.EVENT_BUS_TYPE_UPDATE);  // 发送事件，通知UI层更新总线类型显示
    }
}
