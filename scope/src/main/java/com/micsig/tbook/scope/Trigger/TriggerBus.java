package com.micsig.tbook.scope.Trigger;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;

import java.util.Observable;
import java.util.Observer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    TriggerBus - 串行总线触发器类                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的串行总线触发器实现类，用于串行总线协议触发。                   ║
 * ║   支持多种串行总线协议：UART、LIN、CAN、SPI、I2C、ARINC429、MILSTD1553B、    ║
 * ║   CAN-FD。                                                                   ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理串行总线触发源（根据总线配置动态确定触发源通道）                     ║
 * ║   2. 监听总线配置变更事件，自动更新触发源                                     ║
 * ║   3. 支持多触发源模式（串行总线可能使用多个通道）                             ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │   Trigger       │ ← 抽象基类                      ║
 * ║                          │   (abstract)    │                                 ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║         ┌───────────┬─────────────┼─────────────┬───────────┐               ║
 * ║         │           │             │             │           │               ║
 * ║         ▼           ▼             ▼             ▼           ▼               ║
 * ║   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐         ║
 * ║   │Edge      │ │PulseWidth│ │Logic     │ │...       │ │Bus       │         ║
 * ║   │边沿触发  │ │脉宽触发  │ │逻辑触发  │ │          │ │串行触发  │ ← 本类   ║
 * ║   └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘         ║
 * ║                                                                              ║
 * ║ 【串行总线触发源说明】                                                       ║
 * ║   串行总线触发是一种特殊的触发方式，用于捕获串行通信协议的数据帧。            ║
 * ║   不同协议可能使用不同数量的通道：                                           ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 协议           │ 通道使用说明                                        │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ UART           │ 单线（TX或RX）                                      │  ║
 * ║   │ SPI            │ 四线（CLK、MOSI、MISO、CS）                         │  ║
 * ║   │ I2C            │ 双线（SDA、SCL）                                    │  ║
 * ║   │ CAN/LIN        │ 单线（CAN_H/LIN）                                   │  ║
 * ║   │ CAN-FD         │ 单线（CAN_H）                                       │  ║
 * ║   │ ARINC429       │ 单线或双线                                          │  ║
 * ║   │ MILSTD1553B    │ 双线（总线A、总线B）                                │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【触发源动态更新机制】                                                       ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐  ║
 * ║   │  用户配置串行总线通道（UI）                                         │  ║
 * ║   │       ↓                                                             │  ║
 * ║   │  SerialChannel.setBusChannel()                                     │  ║
 * ║   │       ↓                                                             │  ║
 * ║   │  EventFactory.sendEvent(EVENT_BUS_CH_CHANGE)                       │  ║
 * ║   │       ↓                                                             │  ║
 * ║   │  TriggerBus.update() ← Observer模式接收事件                        │  ║
 * ║   │       ↓                                                             │  ║
 * ║   │  TriggerBus.Init() ← 重新初始化触发源                              │  ║
 * ║   │       ↓                                                             │  ║
 * ║   │  setTriggerSource(idx, chIdx) ← 更新触发源数组                     │  ║
 * ║   └─────────────────────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → Trigger: 继承触发器基类                                                 ║
 * ║   → IBus: 串行总线接口，获取通道采样配置                                    ║
 * ║   → SerialChannel: 串行通道，获取总线对象                                   ║
 * ║   → ChannelFactory: 通道工厂，获取串行通道实例                              ║
 * ║   → EventFactory: 事件工厂，注册观察者监听事件                              ║
 * ║   → Observer: Java观察者接口，监听配置变更事件                              ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户选择串行触发类型（S1-S4）时，TriggerFactory创建TriggerBus实例     ║
 * ║   2. 用户配置串行总线通道时，自动更新触发源                                 ║
 * ║   3. 用户切换串行总线协议类型时，自动更新触发源                             ║
 * ║   4. 用户切换触发类型时，重新初始化触发源                                   ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 观察者模式（Observer Pattern）：监听总线配置变更事件                   ║
 * ║   - 模板方法模式（Template Method）：继承Trigger的抽象方法                 ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   触发源数组操作未加同步保护，应在UI线程操作。                               ║
 * ║   事件回调在事件发送线程执行，需要注意线程安全。                             ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-6-1                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerBus extends Trigger implements Observer {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造串行总线触发器实例
     * 初始化触发源并注册事件观察者
     *
     * @param triggerType 触发类型ID
     *                     TRIG_TYPE_SERIAL1 (8): 串行触发S1
     *                     TRIG_TYPE_SERIAL2 (9): 串行触发S2
     *                     TRIG_TYPE_SERIAL3 (10): 串行触发S3
     *                     TRIG_TYPE_SERIAL4 (11): 串行触发S4
     *
     * <p><b>初始化流程：</b>
     * <ol>
     *   <li>调用父类Trigger构造方法，设置触发类型</li>
     *   <li>调用Init()初始化触发源</li>
     *   <li>注册三个事件观察者：
     *     <ul>
     *       <li>EVENT_BUS_TYPE_UPDATE: 总线类型变更</li>
     *       <li>EVENT_BUS_CH_CHANGE: 总线通道变更</li>
     *       <li>EVENT_TRIGGER_TYPE: 触发类型变更</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @example
     *   TriggerBus trigger = new TriggerBus(Trigger.TRIG_TYPE_SERIAL1);
     */
    public TriggerBus(int triggerType) {
        super(triggerType);

        Init();
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_TYPE_UPDATE,this);
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_CH_CHANGE,this);
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_TYPE,this);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 抽象方法实现 - 继承自Trigger
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断是否支持多触发源
     * 串行总线触发支持多触发源，因为不同协议可能使用多个通道
     *
     * @return 始终返回true，表示支持多触发源
     *
     * @example
     *   SPI协议使用4个通道（CLK、MOSI、MISO、CS），需要多触发源支持
     *   I2C协议使用2个通道（SDA、SCL），需要多触发源支持
     */
    @Override
    protected boolean isMultitriggerSource() {
        return true;
    }

    /**
     * 获取触发源数量
     * 触发源数量由串行总线配置决定，不同协议使用不同数量的通道
     *
     * @return 触发源数量
     *         UART/LIN/CAN: 1
     *         I2C: 2
     *         SPI: 4
     *
     * @see IBus#getChSampleCnt()
     */
    @Override
    public int getTriggerSourceCnt() {
        return getBus().getChSampleCnt();
    }

    /**
     * 触发源变更通知
     * 调用父类方法发送FPGA消息和事件通知
     *
     * @note 当前实现与父类相同，预留扩展空间
     */
    @Override
    public void triggerSourceChange() {
        super.triggerSourceChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 私有方法 - 总线访问
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前触发器关联的串行总线对象
     * 根据触发类型ID计算对应的串行通道索引，获取总线对象
     *
     * @return 串行总线对象（IBus接口实现）
     *
     * <p><b>触发类型到串行通道的映射：</b>
     * <pre>
     * TRIG_TYPE_SERIAL1 (8) → S1 (24)
     * TRIG_TYPE_SERIAL2 (9) → S2 (25)
     * TRIG_TYPE_SERIAL3 (10) → S3 (26)
     * TRIG_TYPE_SERIAL4 (11) → S4 (27)
     *
     * 计算公式：
     * serialIdx = S1 + (triggerType - TRIG_TYPE_SERIAL1)
     *           = 24 + (triggerType - 8)
     *           = triggerType + 16
     * </pre>
     *
     * @see SerialChannel#getBus()
     * @see ChannelFactory#getSerialChannel(int)
     */
    private IBus getBus(){
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(
                ChannelFactory.S1 + getTriggerType()-Trigger.TRIG_TYPE_SERIAL1);
        return  serialChannel.getBus();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 私有方法 - 触发源初始化
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化触发源
     * 根据串行总线配置，设置触发源通道索引
     *
     * <p><b>初始化逻辑：</b>
     * <ol>
     *   <li>获取串行总线对象</li>
     *   <li>获取总线使用的通道数量</li>
     *   <li>遍历所有通道（CH1-CH8），查找参与总线采样的通道</li>
     *   <li>将参与采样的通道设置为触发源</li>
     * </ol>
     *
     * <p><b>示例：</b>
     * <pre>
     * 假设SPI总线配置：
     *   CH1: CLK  → 参与采样
     *   CH2: MOSI → 参与采样
     *   CH3: MISO → 参与采样
     *   CH4: CS   → 参与采样
     *
     * 初始化后：
     *   triggerSource[0] = 0 (CH1)
     *   triggerSource[1] = 1 (CH2)
     *   triggerSource[2] = 2 (CH3)
     *   triggerSource[3] = 3 (CH4)
     * </pre>
     *
     * @see IBus#getChSampleCnt()
     * @see IBus#isChInSample(int)
     */
    private void Init(){
        IBus bus = getBus();
        int idx = 0;
        int cnt = bus.getChSampleCnt();
        int maxIdx = ChannelFactory.getMaxChIdx();
        for(int i=ChannelFactory.CH1;i<maxIdx;i++){
            if(idx < cnt) {
                if (bus.isChInSample(i)) {
                    setTriggerSource(idx, i);
                    idx++;
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Observer接口实现 - 事件监听
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 事件回调方法 - Observer接口实现
     * 当监听的事件发生时，EventFactory会调用此方法
     *
     * @param observable 被观察对象（EventFactory）
     * @param data 事件数据对象（EventBase）
     *
     * <p><b>监听的事件：</b>
     * <ul>
     *   <li><b>EVENT_BUS_TYPE_UPDATE:</b> 总线类型变更
     *     <ul>
     *       <li>用户切换串行总线协议（如从UART切换到SPI）</li>
     *       <li>不同协议使用不同数量的通道，需要重新初始化触发源</li>
     *     </ul>
     *   </li>
     *   <li><b>EVENT_BUS_CH_CHANGE:</b> 总线通道变更
     *     <ul>
     *       <li>用户修改串行总线使用的通道（如将CLK从CH1改到CH2）</li>
     *       <li>需要更新触发源数组</li>
     *     </ul>
     *   </li>
     *   <li><b>EVENT_TRIGGER_TYPE:</b> 触发类型变更
     *     <ul>
     *       <li>用户切换触发类型（如从边沿触发切换到串行触发）</li>
     *       <li>需要重新初始化触发源</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>事件处理流程：</b>
     * <pre>
     * EventFactory.sendEvent(EVENT_BUS_CH_CHANGE)
     *     ↓
     * TriggerBus.update(observable, data)
     *     ↓
     * switch (eventBase.getId()):
     *     case EVENT_BUS_TYPE_UPDATE:
     *     case EVENT_BUS_CH_CHANGE:
     *     case EVENT_TRIGGER_TYPE:
     *         Init() ← 重新初始化触发源
     * </pre>
     *
     * @see EventFactory#EVENT_BUS_TYPE_UPDATE
     * @see EventFactory#EVENT_BUS_CH_CHANGE
     * @see EventFactory#EVENT_TRIGGER_TYPE
     */
    @Override
    public void update(Observable observable, Object data) {
        EventBase eventBase = (EventBase) data;
        switch (eventBase.getId()){
            case EventFactory.EVENT_BUS_TYPE_UPDATE:
            case EventFactory.EVENT_BUS_CH_CHANGE:
            case EventFactory.EVENT_TRIGGER_TYPE:
                Init();
                break;
        }
    }
}
