package com.micsig.tbook.scope.Action;  // 定义包名：示波器动作处理模块

import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类，封装事件类型
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂，用于发送事件
import com.micsig.tbook.scope.ScopeMessage;  // 导入ScopeMessage类：消息处理中心，管理FPGA/硬件/UI消息队列

/**
 * 动作代理类 - 消息发送与事件分发的统一入口
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Action（示波器动作处理模块）</li>
 *   <li>架构层级：业务逻辑层 - 动作代理层</li>
 *   <li>设计模式：外观模式（Facade Pattern）+ 代理模式（Proxy Pattern）</li>
 *   <li>职责类型：消息发送代理、事件分发代理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>提供统一的消息发送接口（FPGA消息、硬件消息、UI消息）</li>
 *   <li>提供统一的事件发送接口（同步/异步/延迟）</li>
 *   <li>封装ScopeMessage和EventFactory的调用细节</li>
 *   <li>简化上层业务代码的调用复杂度</li>
 * </ul>
 * 
 * <p><b>设计模式说明：</b>
 * <ul>
 *   <li><b>外观模式：</b>将ScopeMessage和EventFactory的复杂接口统一封装，提供简化的调用入口</li>
 *   <li><b>代理模式：</b>作为ScopeMessage和EventFactory的代理，控制访问并添加额外功能</li>
 * </ul>
 * 
 * <p><b>消息类型与路由：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   XAction ─ 统一消息发送入口                                              │
 * │                                                                          │
 * │   ┌─────────────────┐                                                    │
 * │   │   sendEvent()   │──────→ EventFactory ──────→ UI层事件处理           │
 * │   └─────────────────┘                                                    │
 * │                                                                          │
 * │   ┌─────────────────┐                                                    │
 * │   │  sendFpgaMsg()  │──────→ ScopeMessage ──────→ FPGAMessage.run()      │
 * │   └─────────────────┘                                                    │
 * │                                                                          │
 * │   ┌─────────────────┐                                                    │
 * │   │   sendHwMsg()   │──────→ ScopeMessage ──────→ HardwareMessage.run()  │
 * │   └─────────────────┘                                                    │
 * │                                                                          │
 * │   ┌─────────────────┐                                                    │
 * │   │   sendUiMsg()   │──────→ ScopeMessage ──────→ UiMessage.run()        │
 * │   └─────────────────┘                                                    │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>各种Action类（如HorizontalAction、VerticalAction等）通过XAction发送消息</li>
 *   <li>业务逻辑层需要触发FPGA操作时调用sendFpgaMsg</li>
 *   <li>业务逻辑层需要触发硬件操作时调用sendHwMsg</li>
 *   <li>业务逻辑层需要更新UI时调用sendUiMsg或sendEvent</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：ScopeMessage（消息处理中心，管理三类消息队列）</li>
 *   <li>依赖：EventFactory（事件工厂，发送UI事件）</li>
 *   <li>依赖：UiMessage（UI消息常量定义）</li>
 *   <li>被依赖：各种Action类（HorizontalAction、VerticalAction、TriggerAction等）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/15
 * @see ScopeMessage 消息处理中心
 * @see EventFactory 事件工厂
 * @see FPGAMessage FPGA消息处理
 * @see HardwareMessage 硬件消息处理
 * @see UiMessage UI消息处理
 */
public class XAction {
    
    // ==================== 成员变量 ====================
    
    /** 消息处理中心引用：用于发送FPGA/硬件/UI消息 */
    private ScopeMessage scopeMessage;  // 持有ScopeMessage单例引用
    
    /** 事件工厂引用：用于发送UI事件 */
    private EventFactory eventFactory;  // 持有EventFactory单例引用
    
    // ==================== 构造方法 ====================
    
    /**
     * 构造方法：初始化动作代理
     * 
     * <p>获取ScopeMessage和EventFactory的单例引用，
     * 用于后续的消息发送和事件分发操作。
     */
    public XAction(){
        scopeMessage = ScopeMessage.getInstance();  // 获取消息处理中心单例
        eventFactory = EventFactory.getInstance();  // 获取事件工厂单例
    }
    
    // ==================== 事件发送方法 ====================
    
    /**
     * 发送事件（同步方式）
     * 
     * <p>创建EventBase对象并通过EventFactory发送事件，
     * 使用同步方式，调用线程会阻塞直到事件处理完成。
     * 
     * @param eventBase 事件对象，包含事件类型和数据
     */
    public void sendEvent(EventBase eventBase){
        sendEvent(eventBase,false);  // 默认同步发送
    }
    
    /**
     * 发送事件（指定同步/异步方式）
     * 
     * <p>通过EventFactory发送事件，可选择同步或异步方式。
     * 
     * @param eventBase 事件对象，包含事件类型和数据
     * @param async true表示异步发送（不阻塞调用线程），false表示同步发送
     */
    public void sendEvent(EventBase eventBase,boolean async){
        EventFactory.sendEvent(eventBase,async);  // 委托EventFactory发送事件
    }
    
    /**
     * 发送事件（指定同步/异步方式和延迟时间）
     * 
     * <p>通过EventFactory发送事件，支持延迟发送。
     * 
     * @param eventBase 事件对象，包含事件类型和数据
     * @param async true表示异步发送，false表示同步发送
     * @param ms 延迟时间（毫秒），事件将在指定延迟后发送
     */
    public void sendEvent(EventBase eventBase,boolean async,long ms){
        EventFactory.sendEvent(eventBase,async,ms);  // 委托EventFactory延迟发送事件
    }
    
    /**
     * 发送事件（通过事件ID，同步方式）
     * 
     * <p>根据事件ID创建EventBase对象并发送，
     * 使用同步方式，适用于简单事件（无附加数据）。
     * 
     * @param event 事件ID（如EventFactory.EVENT_xxx常量）
     */
    public void sendEvent(int event){
        sendEvent(new EventBase(event));  // 创建EventBase对象并同步发送
    }
    
    /**
     * 发送事件（通过事件ID，指定同步/异步方式）
     * 
     * <p>根据事件ID创建EventBase对象并发送，
     * 可选择同步或异步方式。
     * 
     * @param event 事件ID（如EventFactory.EVENT_xxx常量）
     * @param async true表示异步发送，false表示同步发送
     */
    public void sendEvent(int event,boolean async){
        sendEvent(new EventBase(event),async);  // 创建EventBase对象并发送
    }

    // ==================== 硬件消息发送方法 ====================
    
    /**
     * 发送硬件消息（单参数）
     * 
     * <p>向硬件消息队列添加命令，用于控制外部硬件（继电器、PGA等）。
     * 第二个参数默认为0。
     * 
     * @param arg 硬件命令位掩码（如HardwareMessage.HARD_ID_xxx常量）
     */
    public void sendHwMsg(int arg){
        sendHwMsg(arg,0);  // 调用双参数版本，第二个参数为0
    }
    
    /**
     * 发送硬件消息（双参数）
     * 
     * <p>向硬件消息队列添加命令，委托给ScopeMessage处理。
     * 命令将在ScopeMessage的消息循环中被处理，最终调用HardwareMessage.run()。
     * 
     * @param arg1 硬件命令位掩码（主命令）
     * @param arg2 硬件命令位掩码（扩展命令，当前未使用）
     */
    public void sendHwMsg(int arg1,int arg2){
        scopeMessage.sendHwMsg(arg1,arg2);  // 委托ScopeMessage发送硬件消息
    }
    
    // ==================== FPGA消息发送方法 ====================
    
    /**
     * 发送FPGA消息（单参数）
     * 
     * <p>向FPGA消息队列添加命令，用于控制FPGA内部寄存器。
     * 第二个参数默认为0。
     * 
     * @param arg FPGA命令位掩码（如FPGAMessage.FPGA_CMD_xxx常量）
     */
    public void sendFpgaMsg(int arg){
        sendFpgaMsg(arg,0);  // 调用双参数版本，第二个参数为0
    }
    
    /**
     * 发送FPGA消息（双参数）
     * 
     * <p>向FPGA消息队列添加命令，委托给ScopeMessage处理。
     * 命令将在ScopeMessage的消息循环中被处理，最终调用FPGAMessage.run()。
     * 
     * @param arg1 FPGA命令位掩码（主命令，对应FPGA_CMD[0]）
     * @param arg2 FPGA命令位掩码（扩展命令，对应FPGA_CMD[1]）
     */
    public void sendFpgaMsg(int arg1,int arg2){
        scopeMessage.sendFpgaMsg(arg1,arg2);  // 委托ScopeMessage发送FPGA消息
    }
    
    // ==================== UI消息发送方法 ====================
    
    /**
     * 发送UI消息
     * 
     * <p>向UI消息队列添加命令，用于触发UI界面刷新。
     * 命令将在ScopeMessage的消息循环中被处理，最终调用UiMessage.run()。
     * 
     * @param arg1 UI命令位掩码（如UiMessage.UI_MESSAGE_xxx常量）
     */
    public void sendUiMsg(int arg1){
        scopeMessage.sendUiMsg(arg1);  // 委托ScopeMessage发送UI消息
    }
    
    // ==================== 延迟消息发送方法 ====================
    
    /**
     * 发送FPGA延迟消息（单参数）
     * 
     * <p>向FPGA消息队列添加延迟执行的命令，用于需要延时处理的FPGA操作。
     * 第二个参数默认为0。
     * 
     * @param arg FPGA命令位掩码
     * @param delayMillis 延迟时间（毫秒）
     */
    public void sendFpgaMsgDelayed(int arg,long delayMillis){
        sendFpgaMsgDelayed(arg,0,delayMillis);  // 调用三参数版本
    }

    /**
     * 发送FPGA延迟消息（双参数）
     * 
     * <p>向FPGA消息队列添加延迟执行的命令，委托给ScopeMessage处理。
     * 
     * @param arg1 FPGA命令位掩码（主命令）
     * @param arg2 FPGA命令位掩码（扩展命令）
     * @param delayMillis 延迟时间（毫秒）
     */
    public void sendFpgaMsgDelayed(int arg1,int arg2,long delayMillis){
         scopeMessage.sendFpgaMsgDelayed(arg1,arg2,delayMillis);  // 委托ScopeMessage延迟发送FPGA消息
    }
    
    /**
     * 发送硬件延迟消息（单参数）
     * 
     * <p>向硬件消息队列添加延迟执行的命令，用于需要延时处理的硬件操作。
     * 第二个参数默认为0。
     * 
     * @param arg 硬件命令位掩码
     * @param delayMillis 延迟时间（毫秒）
     */
    public void sendHwMsgDelayed(int arg,long delayMillis){
        sendHwMsgDelayed(arg,0,delayMillis);  // 调用三参数版本
    }
    
    /**
     * 发送硬件延迟消息（双参数）
     * 
     * <p>向硬件消息队列添加延迟执行的命令，委托给ScopeMessage处理。
     * 
     * @param arg1 硬件命令位掩码（主命令）
     * @param arg2 硬件命令位掩码（扩展命令）
     * @param delayMillis 延迟时间（毫秒）
     */
    public void sendHwMsgDelayed(int arg1,int arg2,long delayMillis){
         scopeMessage.sendHwMsgDelayed(arg1,arg2,delayMillis);  // 委托ScopeMessage延迟发送硬件消息
    }
    
    // ==================== 采样相关方法 ====================
    
    /**
     * 发送通道采样变化消息
     * 
     * <p>通知系统通道采样状态发生变化，同时触发UI刷新存储深度和采样率显示。
     * 通常在通道开启/关闭时调用。
     */
    public void sendChSample(){
        scopeMessage.sendChSample();  // 发送通道采样变化消息，触发HardwareMessage.chSampleChange()
        scopeMessage.sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);  // 发送UI消息刷新存储深度和采样率显示
    }

    // ==================== 段采样相关方法 ====================
    
    /**
     * 发送段采样帧数消息
     * 
     * <p>在段采样模式下，通知系统更新段帧数。
     * 仅当bSegment为true时才执行操作。
     * 
     * @param bSegment true表示需要处理段帧数，false表示不处理
     */
    public void sendSegment(boolean bSegment){
        if(bSegment) {  // 检查是否需要处理段帧数
            scopeMessage.segmentFrames();  // 发送段帧数消息，更新段采样帧数显示
        }
    }

    /**
     * 发送段采样命令
     * 
     * <p>触发段采样命令的执行，用于段采样模式下的帧采集控制。
     */
    public void cmdSegment(){
        scopeMessage.cmdSegment();  // 发送段采样命令
    }
}
