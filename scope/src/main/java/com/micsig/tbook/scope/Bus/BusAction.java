package com.micsig.tbook.scope.Bus;  // 定义包名：示波器总线分析模块

import com.micsig.tbook.scope.Action.FPGAMessage;  // 导入FPGAMessage类：FPGA命令消息处理
import com.micsig.tbook.scope.Action.XAction;  // 导入XAction类：动作基类，提供FPGA命令发送能力
import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂，发送事件通知
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂，提供通道常量

/**
 * 总线动作处理类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Bus（示波器总线分析模块）</li>
 *   <li>架构层级：动作处理层 - 总线动作</li>
 *   <li>设计模式：继承模式（继承自XAction基类）</li>
 *   <li>职责类型：总线参数变化时的FPGA命令发送和事件通知</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>处理总线参数变化，发送FPGA命令</li>
 *   <li>处理通道采样变化，更新总线配置</li>
 *   <li>处理总线类型变化，通知FPGA</li>
 *   <li>发送总线相关的事件通知</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>将总线变化操作封装成独立的动作类</li>
 *   <li>继承XAction获得FPGA命令发送能力</li>
 *   <li>提供统一的总线变化处理接口</li>
 *   <li>解耦总线配置与底层硬件操作</li>
 * </ul>
 * 
 * <p><b>总线动作处理流程：</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                         BusAction（总线动作处理）                        │
 * │                                                                         │
 * │  ┌─────────────────────────────────────────────────────────────────┐   │
 * │  │                        触发入口方法                               │   │
 * │  │  busChange()        - 总线参数变化                               │   │
 * │  │  chSampleChange()   - 通道采样变化                               │   │
 * │  │  busTypeChange()    - 总线类型变化                               │   │
 * │  └─────────────────────────────────────────────────────────────────┘   │
 * │                              │                                          │
 * │                              ▼                                          │
 * │  ┌─────────────────────────────────────────────────────────────────┐   │
 * │  │                      处理流程                                    │   │
 * │  │  1. sendFpgaMsg()    - 发送FPGA命令                              │   │
 * │  │  2. sendEvent()      - 发送事件通知                              │   │
 * │  │  3. sendChSample()   - 发送通道采样命令（继承自XAction）         │   │
 * │  └─────────────────────────────────────────────────────────────────┘   │
 * │                              │                                          │
 * │                              ▼                                          │
 * │  ┌─────────────────────────────────────────────────────────────────┐   │
 * │  │                      下游组件                                    │   │
 * │  │  FPGAMessage        - FPGA命令队列                               │   │
 * │  │  EventFactory       - 事件分发系统                               │   │
 * │  └─────────────────────────────────────────────────────────────────┘   │
 * └─────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>事件类型说明：</b>
 * <pre>
 * ┌────────────────────────────────┬────────────────────────────────────────┐
 * │ 事件类型                       │ 触发场景                               │
 * ├────────────────────────────────┼────────────────────────────────────────┤
 * │ EVENT_BUS_PARAM                │ 总线参数变化（波特率、格式等）         │
 * │ EVENT_BUS_CH_CHANGE            │ 总线通道变化（源通道切换）             │
 * │ EVENT_BUS_TYPE                 │ 总线类型变化（CAN/LIN/ARINC429等）    │
 * └────────────────────────────────┴────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：XAction（动作基类）</li>
 *   <li>关联：IBus（总线接口）</li>
 *   <li>依赖：FPGAMessage（FPGA命令消息）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 *   <li>依赖：ChannelFactory（通道工厂）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <pre>
 * // 创建总线动作处理器
 * IBus bus = new CANBus(0);
 * BusAction action = new BusAction(bus);
 * 
 * // 总线参数变化时调用
 * action.busChange();
 * 
 * // 通道采样变化时调用
 * action.chSampleChange();
 * 
 * // 总线类型变化时调用
 * action.busTypeChange();
 * </pre>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-30
 * @see XAction 动作基类
 * @see IBus 总线接口
 */
public class BusAction extends XAction {

    /**
     * 总线对象引用
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>关联的总线配置对象</li>
     *   <li>通过此引用获取总线索引、类型等信息</li>
     *   <li>作为事件通知的数据载体</li>
     * </ul>
     */
    IBus bus;  // 总线对象引用，存储关联的总线配置

    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>初始化总线对象引用</li>
     *   <li>绑定具体的总线配置实例</li>
     * </ul>
     * 
     * @param bus 总线配置对象（CANBus、LINBus、ARINC429Bus等）
     */
    public BusAction(IBus bus){
        this.bus = bus;  // 保存总线对象引用
    }

    /**
     * 处理总线参数变化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送FPGA命令通知总线参数变化</li>
     *   <li>发送事件通知UI层更新显示</li>
     * </ul>
     * 
     * <p><b>FPGA命令说明：</b>
     * <ul>
     *   <li>命令格式：FPGA_CMD_BUS_S1左移(总线索引 - S1偏移)</li>
     *   <li>用于通知FPGA更新总线解码参数</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>波特率变化时调用</li>
     *   <li>数据格式变化时调用</li>
     *   <li>触发条件变化时调用</li>
     * </ul>
     */
    public void busChange(){
        sendFpgaMsg(0, (FPGAMessage.FPGA_CMD_BUS_S1) << (bus.getBusIdx() - ChannelFactory.S1));  // 发送FPGA总线参数更新命令
        sendEvent(EventFactory.EVENT_BUS_PARAM);  // 发送总线参数变化事件
    }

    /**
     * 处理通道采样变化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送通道变化事件通知</li>
     *   <li>发送通道采样命令到FPGA</li>
     *   <li>触发总线参数更新</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <ol>
     *   <li>发送EVENT_BUS_CH_CHANGE事件</li>
     *   <li>调用sendChSample()更新通道采样配置</li>
     *   <li>调用busChange()更新总线参数</li>
     * </ol>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>源通道切换时调用</li>
     *   <li>通道使能状态变化时调用</li>
     * </ul>
     */
    public void chSampleChange(){

        sendEvent(EventFactory.EVENT_BUS_CH_CHANGE);  // 发送通道变化事件
        sendChSample();  // 发送通道采样命令（继承自XAction）
        busChange();  // 更新总线参数
    }

    /**
     * 处理总线类型变化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>发送FPGA命令通知总线类型变化</li>
     *   <li>用于切换总线解码模式（CAN/LIN/ARINC429等）</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户切换总线类型时调用</li>
     *   <li>从CAN切换到LIN时调用</li>
     *   <li>从LIN切换到ARINC429时调用</li>
     * </ul>
     */
    public void busTypeChange(){
        sendFpgaMsg(0, FPGAMessage.FPGA_CMD_BUS_TYPE);  // 发送FPGA总线类型变化命令
    }

    /**
     * 发送事件通知（重写父类方法）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>创建包含总线对象的事件</li>
     *   <li>委托给父类的sendEvent方法发送</li>
     * </ul>
     * 
     * <p><b>重写目的：</b>
     * <ul>
     *   <li>自动将总线对象作为事件数据</li>
     *   <li>简化事件发送调用</li>
     * </ul>
     * 
     * @param event 事件类型常量
     */
    @Override
    public void sendEvent(int event){
        sendEvent(new EventBase(event,bus));  // 创建包含总线对象的事件并发送
    }
}
