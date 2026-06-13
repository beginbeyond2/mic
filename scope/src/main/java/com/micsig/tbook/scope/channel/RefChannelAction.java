package com.micsig.tbook.scope.channel;  // 定义包名：示波器通道管理模块

import com.micsig.base.Logger;  // 导入Logger类：基础日志工具
import com.micsig.tbook.scope.Action.XAction;  // 导入XAction类：动作代理基类
import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂

/**
 * 参考通道动作代理类 - 处理参考通道的状态变化消息发送
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 动作代理层</li>
 *   <li>设计模式：代理模式（Proxy Pattern）</li>
 *   <li>职责类型：参考通道状态变化消息发送</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>代理参考通道的波形加载操作</li>
 *   <li>代理参考通道的时基变化操作</li>
 *   <li>封装事件发送逻辑，解耦RefChannel与事件系统</li>
 * </ul>
 * 
 * <p><b>参考通道架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   RefChannel - 参考通道                                                  │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   RefChannelAction - 动作代理                                    │   │
 * │   │                                                                   │   │
 * │   │   职责：封装状态变化的事件发送逻辑                                 │   │
 * │   │                                                                   │   │
 * │   │   ┌─────────────────┐      ┌─────────────────┐                   │   │
 * │   │   │   loadWave()    │──────→ EVENT_REF_WAVE_UPDATE              │   │
 * │   │   │   波形加载完成   │      │   波形更新事件    │                   │   │
 * │   │   └─────────────────┘      └─────────────────┘                   │   │
 * │   │                                                                   │   │
 * │   │   ┌─────────────────┐      ┌─────────────────┐                   │   │
 * │   │   │changeChTimeBase │──────→ EVENT_CHANGE_CH_TIMEBASE           │   │
 * │   │   │   时基变化       │      │   时基变化事件    │                   │   │
 * │   │   └─────────────────┘      └─────────────────┘                   │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   参考通道类型：                                                          │
 * │   ├── refType=0: 动态通道（CH1-CH8）                                     │
 * │   ├── refType=1: 数学通道-双波形运算                                     │
 * │   └── refType=2: 数学通道-FFT运算                                        │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>参考通道索引分配：</b>
 * <pre>
 *   REF1 = 16（ChannelFactory.REF1）
 *   REF2 = 17
 *   REF3 = 18
 *   ...
 *   REF8 = 23（ChannelFactory.REF8）
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   XAction（动作代理基类）
 *       │
 *       └── RefChannelAction（参考通道动作代理）
 *               │
 *               ├── 继承：sendEvent()方法
 *               └── 组合：RefChannel引用
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>参考通道加载波形文件后，调用loadWave()发送波形更新事件</li>
 *   <li>参考通道时基变化时，调用changeChTimeBase()发送时基变化事件</li>
 *   <li>RefChannel内部持有RefChannelAction实例，通过代理发送事件</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：XAction（动作代理基类，提供事件发送能力）</li>
 *   <li>组合：RefChannel（参考通道，被代理的对象）</li>
 *   <li>依赖：EventFactory（事件工厂，发送UI事件）</li>
 *   <li>依赖：Logger（日志工具）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-25
 * @see XAction 动作代理基类
 * @see RefChannel 参考通道
 * @see EventFactory 事件工厂
 */
public class RefChannelAction extends XAction {  // 继承XAction动作代理基类

    /** 日志标签 */
    private static final String TAG = "RefChannelAction";  // 日志输出标签
    
    /** 参考通道引用：被代理的参考通道对象 */
    RefChannel refChannel;  // 持有RefChannel引用，用于获取通道状态
    
    /**
     * 构造方法：初始化参考通道动作代理
     * 
     * <p>保存参考通道引用，用于后续操作中获取通道状态。
     * 父类XAction的构造方法会初始化ScopeMessage和EventFactory引用。
     * 
     * @param refChannel 参考通道对象，被代理的通道实例
     */
    public RefChannelAction(RefChannel refChannel){
        super();  // 调用父类构造方法，初始化消息发送能力
        this.refChannel = refChannel;  // 保存参考通道引用
    }
    
    /**
     * 加载波形完成处理
     * 
     * <p>当参考通道加载波形完成后调用，执行以下操作：
     * <ol>
     *   <li>设置波形有效标志为true</li>
     *   <li>发送EVENT_REF_WAVE_UPDATE事件，通知UI刷新参考波形显示</li>
     * </ol>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>RefChannel.loadWave()方法内部调用</li>
     *   <li>RefChannel.drawRef()方法内部调用（重绘时）</li>
     * </ul>
     * 
     * <p><b>事件处理：</b>
     * <ul>
     *   <li>EVENT_REF_WAVE_UPDATE：通知UI层参考波形已更新，需要刷新显示</li>
     * </ul>
     */
    public void loadWave(){
        this.refChannel.setWaveValid(true);  // 设置波形有效标志，表示参考通道有有效波形数据
        sendEvent(EventFactory.EVENT_REF_WAVE_UPDATE);  // 发送参考波形更新事件，通知UI刷新显示
    }

    /**
     * 改变通道时基
     * 
     * <p>当参考通道的时基档位发生变化时调用，执行以下操作：
     * <ol>
     *   <li>获取参考通道的当前时基值</li>
     *   <li>输出日志记录时基变化</li>
     *   <li>发送EVENT_CHANGE_CH_TIMEBASE事件，携带时基值和通道ID</li>
     * </ol>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>RefChannel.LoadRefConfig()方法内部调用（加载波形配置后）</li>
     * </ul>
     * 
     * <p><b>事件处理：</b>
     * <ul>
     *   <li>EVENT_CHANGE_CH_TIMEBASE：通知系统参考通道时基已变化，
     *       事件数据包含新的时基值和通道ID</li>
     * </ul>
     * 
     * <p><b>时基说明：</b>
     * <ul>
     *   <li>时基值表示每格的时间长度（如1ms/div、10μs/div等）</li>
     *   <li>参考通道的时基可以独立于主时基设置</li>
     *   <li>时基变化会影响波形的时间轴缩放</li>
     * </ul>
     */
    public void changeChTimeBase() {
        double timeBase = this.refChannel.getRefTimeScaleVal();  // 获取参考通道的当前时基值
        Logger.i(TAG, "channel= " + this.refChannel.getName() + " ,timeBase= " + timeBase);  // 输出日志：通道名和时基值
        sendEvent(new EventBase(EventFactory.EVENT_CHANGE_CH_TIMEBASE, timeBase, refChannel.getChId()));  // 发送时基变化事件，携带时基值和通道ID
    }
}
