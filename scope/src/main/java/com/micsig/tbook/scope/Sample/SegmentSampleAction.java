package com.micsig.tbook.scope.Sample;

import com.micsig.tbook.scope.Action.FPGAMessage;
import com.micsig.tbook.scope.Action.UiMessage;
import com.micsig.tbook.scope.Action.XAction;
import com.micsig.tbook.scope.Event.EventFactory;

/**
 * 段采样动作处理类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 动作处理层</li>
 *   <li>设计模式：命令模式 + 观察者模式</li>
 *   <li>职责类型：事件处理器</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>处理段采样启用/禁用事件</li>
 *   <li>处理段采样参数变化事件</li>
 *   <li>处理段帧数更新事件</li>
 *   <li>协调FPGA命令发送</li>
 *   <li>协调UI消息和事件通知</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>解耦段采样状态管理与硬件控制</li>
 *   <li>统一处理段采样变化后的连锁反应</li>
 *   <li>协调多个模块响应段采样状态变化</li>
 *   <li>确保FPGA配置与软件状态同步</li>
 * </ul>
 * 
 * <p><b>继承结构：</b>
 * <pre>
 * XAction (抽象动作基类)
 *   │
 *   └── SegmentSampleAction (当前类 - 段采样动作处理)
 *          │
 *          ├── 持有 SegmentSample 引用
 *          ├── 使用 FPGAMessage 发送硬件命令
 *          ├── 使用 UiMessage 发送UI更新
 *          └── 使用 EventFactory 发送事件通知
 * </pre>
 * 
 * <p><b>调用链路：</b>
 * <pre>
 * 用户切换段采样模式
 *   │
 *   ▼
 * SegmentSample.setSegmentEnable()
 *   │
 *   ▼
 * SegmentSampleAction.SegmentEnable()
 *   │
 *   ├─→ FPGA命令发送（存储深度/采样位置/耦合/显示等）
 *   ├─→ 段采样状态发送
 *   ├─→ UI消息发送
 *   └─→ 段变化事件
 * </pre>
 * 
 * <p><b>FPGA命令组合：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────┐
 * │ 方法           │ FPGA命令                                  │
 * ├────────────────┼───────────────────────────────────────────┤
 * │ SegmentEnable  │ SAMP_ZUN_DEPTH | SAMP_PLACE | COUY |     │
 * │                │ DIS | DIS_MODE | SAMP_MODE | SEGMENT |   │
 * │                │ DIS (重复)                                │
 * │ segmentChange  │ SEGMENT | DIS                             │
 * │ segmentFrames  │ 无FPGA命令（仅发送事件）                  │
 * └────────────────┴───────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>持有：SegmentSample（段采样状态管理对象）</li>
 *   <li>依赖：FPGAMessage（FPGA命令发送）</li>
 *   <li>依赖：UiMessage（UI消息发送）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户启用/禁用段采样模式时</li>
 *   <li>用户调整段数量时</li>
 *   <li>用户切换段显示模式时</li>
 *   <li>用户调整帧号或帧范围时</li>
 *   <li>段采样进度更新时</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see XAction 动作基类
 * @see SegmentSample 段采样状态管理
 * @see FPGAMessage FPGA命令
 * @see UiMessage UI消息
 * @see EventFactory 事件工厂
 */
public class SegmentSampleAction extends XAction {
    
    /**
     * 段采样状态管理对象引用
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>持有当前段采样配置的引用</li>
     *   <li>用于获取当前段采样状态</li>
     * </ul>
     * 
     * <p><b>生命周期：</b>
     * <ul>
     *   <li>在SegmentSample构造函数中创建SegmentSampleAction时传入</li>
     *   <li>与SegmentSample对象生命周期相同</li>
     * </ul>
     */
    private SegmentSample segmentSample;

    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>保存段采样状态管理对象的引用</li>
     *   <li>建立SegmentSample与SegmentSampleAction的关联</li>
     * </ul>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>SegmentSample构造函数中创建SegmentSampleAction时调用</li>
     * </ul>
     * 
     * @param segmentSample 段采样状态管理对象
     */
    public SegmentSampleAction(SegmentSample segmentSample){
        this.segmentSample = segmentSample;
    }
    
    /**
     * 处理段采样启用事件
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>响应段采样模式启用/禁用</li>
     *   <li>发送完整的FPGA命令组合更新硬件配置</li>
     *   <li>发送段采样状态</li>
     *   <li>发送UI更新消息</li>
     *   <li>触发段变化事件</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * ┌─────────────────────────────────────────────────────────────┐
     * │ 1. 发送FPGA命令组合                                          │
     * │    ├─→ SAMP_ZUN_DEPTH：采样存储深度配置                     │
     * │    ├─→ SAMP_PLACE：采样位置配置                             │
     * │    ├─→ COUY：耦合配置                                       │
     * │    ├─→ DIS：显示配置                                        │
     * │    ├─→ DIS_MODE：显示模式配置                               │
     * │    ├─→ SAMP_MODE：采样模式配置                              │
     * │    ├─→ SEGMENT：段采样配置                                  │
     * │    └─→ DIS：显示配置（重复发送确保生效）                    │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 2. 发送段采样状态                                            │
     * │    └─→ sendSegment(isSegmentEnable)                        │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 3. 发送UI消息                                                │
     * │    ├─→ UI_MESSAGE_DEPTH_SAMPFRE：更新采样率显示             │
     * │    └─→ UI_MESSAGE_SIMPLE_WIN_DIS：更新简单窗口显示          │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 4. 触发段变化事件                                            │
     * │    └─→ segmentChange()                                      │
     * └─────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>FPGA命令说明：</b>
     * <ul>
     *   <li>SAMP_ZUN_DEPTH：更新存储深度，段采样需要重新分配存储空间</li>
     *   <li>SAMP_PLACE：更新采样位置，段采样可能影响触发位置</li>
     *   <li>COUY：更新耦合配置</li>
     *   <li>DIS/DIS_MODE：更新显示配置，段采样显示模式可能变化</li>
     *   <li>SAMP_MODE：更新采样模式，切换到段采样模式</li>
     *   <li>SEGMENT：更新段采样参数（段数、帧号等）</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户启用/禁用段采样模式时触发</li>
     *   <li>用户调整段数量时触发</li>
     * </ul>
     */
    public void SegmentEnable(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_SAMP_ZUN_DEPTH|FPGAMessage.FPGA_CMD_SAMP_PLACE
                |FPGAMessage.FPGA_CMD_COUY|FPGAMessage.FPGA_CMD_DIS | FPGAMessage.FPGA_CMD_DIS_MODE
                |FPGAMessage.FPGA_CMD_SAMP_MODE
                |FPGAMessage.FPGA_CMD_SEGMENT
                |FPGAMessage.FPGA_CMD_DIS);
        sendSegment(segmentSample.isSegmentEnable());

        sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);
        sendUiMsg(UiMessage.UI_MESSAGE_SIMPLE_WIN_DIS);
        segmentChange();
    }

    /**
     * 处理段采样参数变化事件
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>响应段采样参数变化（帧号、显示模式等）</li>
     *   <li>发送FPGA命令更新段采样配置和显示</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 发送FPGA命令
     *    ├─→ SEGMENT：更新段采样参数（帧号、显示模式等）
     *    └─→ DIS：更新显示配置
     * </pre>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户切换段显示模式（单帧/拟合）时触发</li>
     *   <li>用户调整帧号时触发</li>
     *   <li>用户调整拟合帧范围时触发</li>
     *   <li>段采样启用后自动触发</li>
     * </ul>
     */
    public void segmentChange(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_SEGMENT|FPGAMessage.FPGA_CMD_DIS);
    }
    
    /**
     * 处理段帧数更新事件
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>响应段采样进度更新</li>
     *   <li>发送事件通知UI更新进度显示</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 发送事件通知
     *    └─→ EVENT_SEGMENT_FRAMES：通知段帧数已更新
     * </pre>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>段采样过程中捕获到新段时触发</li>
     *   <li>用于更新UI上的采样进度显示</li>
     * </ul>
     * 
     * <p><b>注意：</b>此方法不发送FPGA命令，仅通知UI更新
     */
    public void segmentFrames(){
        sendEvent(EventFactory.EVENT_SEGMENT_FRAMES);
    }

}
