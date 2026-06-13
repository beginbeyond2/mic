package com.micsig.tbook.scope.Sample;

import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Action.FPGAMessage;
import com.micsig.tbook.scope.Action.HardwareMessage;
import com.micsig.tbook.scope.Action.UiMessage;
import com.micsig.tbook.scope.Action.XAction;
import com.micsig.tbook.scope.Event.EventFactory;

/**
 * 采样动作处理类
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
 *   <li>处理采样类型变化事件</li>
 *   <li>处理采样运行状态变化事件</li>
 *   <li>处理单次采样事件</li>
 *   <li>处理采样冻结事件</li>
 *   <li>协调FPGA命令发送</li>
 *   <li>管理采样类型与存储深度的关联</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>解耦采样状态管理与硬件控制</li>
 *   <li>统一处理采样变化后的连锁反应</li>
 *   <li>协调多个模块响应采样状态变化</li>
 *   <li>确保FPGA配置与软件状态同步</li>
 * </ul>
 * 
 * <p><b>继承结构：</b>
 * <pre>
 * XAction (抽象动作基类)
 *   │
 *   └── SampleAction (当前类 - 采样动作处理)
 *          │
 *          ├── 持有 Sample 引用
 *          ├── 使用 FPGAMessage 发送硬件命令
 *          ├──使用 HardwareMessage 发送硬件消息
 *          ├── 使用 UiMessage 发送UI更新
 *          └── 使用 EventFactory 发送事件通知
 * </pre>
 * 
 * <p><b>调用链路：</b>
 * <pre>
 * 用户切换采样类型
 *   │
 *   ▼
 * Sample.setSampleType()
 *   │
 *   ▼
 * SampleAction.SampleTypeChange()
 *   │
 *   ├─→ 存储深度调整（平均/包络模式）
 *   ├─→ FPGA命令发送
 *   ├─→ UI消息发送
 *   └─→ 事件通知发送
 * </pre>
 * 
 * <p><b>FPGA命令组合：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────┐
 * │ 方法              │ FPGA命令                               │
 * ├───────────────────┼────────────────────────────────────────┤
 * │ SampleTypeChange  │ SAMP_MODE | PJBL_NUM | SAMP_PLACE     │
 * │ frozenChange      │ RUN_STOP                               │
 * │ SampleRunChange   │ RUN_STOP | arg                        │
 * │ SampleSingle      │ RUN_STOP | SINGLE                     │
 * └───────────────────┴────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>采样类型与存储深度关联：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────┐
 * │ 采样类型              │ 存储深度处理                        │
 * ├───────────────────────┼────────────────────────────────────┤
 * │ SAMPLE_TYPE_AVERAGE   │ 限制为扩展存储深度                  │
 * │ SAMPLE_TYPE_ENVEL     │ 限制为扩展存储深度                  │
 * │ 其他类型              │ 恢复备份的存储深度                  │
 * └───────────────────────┴────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>持有：Sample（采样状态管理对象）</li>
 *   <li>依赖：MemDepthFactory（存储深度工厂）</li>
 *   <li>依赖：FPGAMessage（FPGA命令发送）</li>
 *   <li>依赖：HardwareMessage（硬件消息发送）</li>
 *   <li>依赖：UiMessage（UI消息发送）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户切换采样类型时</li>
 *   <li>用户启动/停止采样时</li>
 *   <li>用户启用单次采样模式时</li>
 *   <li>采样状态冻结时</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/15
 * @see XAction 动作基类
 * @see Sample 采样状态管理
 * @see MemDepthFactory 存储深度工厂
 * @see FPGAMessage FPGA命令
 * @see HardwareMessage 硬件消息
 * @see UiMessage UI消息
 * @see EventFactory 事件工厂
 */
public class SampleAction extends XAction {
    
    /**
     * 采样状态管理对象引用
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>持有当前采样配置的引用</li>
     *   <li>用于获取当前采样状态和类型</li>
     * </ul>
     * 
     * <p><b>生命周期：</b>
     * <ul>
     *   <li>在Sample构造函数中创建SampleAction时传入</li>
     *   <li>与Sample对象生命周期相同</li>
     * </ul>
     */
    private Sample sample;
    
    /**
     * 备份的存储深度值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>在切换到平均/包络模式前备份当前存储深度</li>
     *   <li>切换回其他模式时恢复此值</li>
     *   <li>值为0表示无需恢复</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>平均/包络模式需要限制存储深度</li>
     *   <li>退出这些模式时恢复原始配置</li>
     * </ul>
     */
    private int bakMemDepth = 0;
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>保存采样状态管理对象的引用</li>
     *   <li>建立Sample与SampleAction的关联</li>
     * </ul>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>Sample构造函数中创建SampleAction时调用</li>
     * </ul>
     * 
     * @param sample 采样状态管理对象
     */
    public SampleAction(Sample sample){
        this.sample = sample;
    }
    
    /**
     * 处理采样类型变化事件
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>响应采样类型变化，执行连锁更新操作</li>
     *   <li>根据采样类型调整存储深度</li>
     *   <li>发送FPGA命令更新硬件配置</li>
     *   <li>发送UI消息更新界面显示</li>
     *   <li>发送事件通知其他模块</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * ┌─────────────────────────────────────────────────────────────┐
     * │ 1. 检查采样类型                                              │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 2. 平均/包络模式处理                                         │
     * │    ├─→ 获取扩展存储深度限制值                                │
     * │    ├─→ 如果当前存储深度超过限制：                            │
     * │    │     ├─→ 备份当前存储深度                                │
     * │    │     └─→ 强制切换到扩展存储深度                         │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 3. 其他模式处理                                              │
     * │    └─→ 如果有备份的存储深度：                                │
     * │          ├─→ 恢复备份的存储深度                              │
     * │          └─→ 清空备份值                                      │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 4. 发送FPGA命令                                              │
     * │    ├─→ SAMP_MODE：采样模式配置                              │
     * │    ├─→ PJBL_NUM：平均/包络次数配置                          │
     * │    └─→ SAMP_PLACE：采样位置配置                             │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 5. 发送UI消息                                                │
     * │    └─→ UI_MESSAGE_DEPTH_SAMPFRE：更新采样率显示             │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 6. 发送事件通知                                              │
     * │    └─→ EVENT_SAMPLE_TYPE：通知采样类型已变化                │
     * └─────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>存储深度限制说明：</b>
     * <ul>
     *   <li>平均采样和包络采样模式需要较小的存储深度</li>
     *   <li>限制为扩展存储深度（getDefaultMemDepthEx）</li>
     *   <li>MHO68: 限制为18M</li>
     *   <li>MHO38/MHO28: 限制为36M</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户切换采样类型时触发</li>
     *   <li>设置采样次数时触发</li>
     *   <li>设置触发/时钟方向时触发</li>
     *   <li>设置滚屏帧率时触发</li>
     * </ul>
     */
    public void SampleTypeChange(){

        switch (sample.getSampleType()){
            case Sample.SAMPLE_TYPE_AVERAGE:
            case Sample.SAMPLE_TYPE_ENVEL: {
                int m = MemDepthFactory.getDefaultMemDepthEx();
                if (MemDepthFactory.getMemDepthSet() > m) {
                    bakMemDepth = MemDepthFactory.getMemDepthSet();
                    MemDepthFactory.forceMemDepth(m);
                }
            }
                break;
            default:
                if(bakMemDepth > 0){
                    MemDepthFactory.forceMemDepth(bakMemDepth);
                }
                bakMemDepth = 0;
                break;
        }
        sendFpgaMsg(FPGAMessage.FPGA_CMD_SAMP_MODE|FPGAMessage.FPGA_CMD_PJBL_NUM | FPGAMessage.FPGA_CMD_SAMP_PLACE);
        sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);
        sendEvent(EventFactory.EVENT_SAMPLE_TYPE);
    }

    /**
     * 处理采样冻结事件
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>响应采样冻结状态变化</li>
     *   <li>发送FPGA命令更新运行/停止状态</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>调用Sample.frozenSample()时触发</li>
     *   <li>示波器进入冻结状态时</li>
     * </ul>
     * 
     * <p><b>FPGA命令：</b>
     * <ul>
     *   <li>FPGA_CMD_RUN_STOP：控制FPGA的运行/停止状态</li>
     * </ul>
     */
    public void frozenChange(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_RUN_STOP);
    }
    
    /**
     * 处理采样运行状态变化事件
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>响应采样运行状态变化</li>
     *   <li>发送通道采样配置</li>
     *   <li>发送硬件消息</li>
     *   <li>发送FPGA命令</li>
     *   <li>发送事件通知</li>
     *   <li>发送UI消息</li>
     * </ul>
     */
    public void SampleRunChange(){
        SampleRunChange(0);
    }
    
    /**
     * 处理采样运行状态变化事件（带参数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>响应采样运行状态变化</li>
     *   <li>启动采样时发送通道配置和硬件消息</li>
     *   <li>发送FPGA命令更新运行状态</li>
     *   <li>发送事件通知和UI更新</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * ┌─────────────────────────────────────────────────────────────┐
     * │ 1. 检查是否为运行状态                                        │
     * │    └─→ 如果正在采样：                                        │
     * │          ├─→ 发送通道采样配置                                │
     * │          └─→ 发送硬件消息（偏移/垂直刻度/耦合）              │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 2. 发送FPGA命令                                              │
     * │    └─→ RUN_STOP | arg：控制运行/停止状态                    │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 3. 发送事件通知                                              │
     * │    └─→ EVENT_SCOPE_STATE：通知示波器状态变化                │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 4. 发送UI消息                                                │
     * │    ├─→ UI_MESSAGE_DEPTH_SAMPFRE：更新采样率显示             │
     * │    └─→ UI_MESSAGE_SIMPLE_WIN_DIS：更新简单窗口显示          │
     * └─────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>硬件消息说明：</b>
     * <ul>
     *   <li>HARD_ID_OFFSET_CH1：通道偏移配置</li>
     *   <li>HARD_ID_CH_vSCALE_CH1：通道垂直刻度配置</li>
     *   <li>HARD_ID_CH_COUP_CH1：通道耦合配置</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户按下Run/Stop按钮时触发</li>
     *   <li>启动或停止采样时</li>
     * </ul>
     * 
     * @param arg 额外的FPGA命令参数（如FPGA_CMD_SINGLE）
     */
    public void SampleRunChange(int arg){
        if(sample.isRunSample()){
            sendChSample();
            sendHwMsg(HardwareMessage.HARD_ID_OFFSET_CH1
                    |HardwareMessage.HARD_ID_CH_vSCALE_CH1
                    |HardwareMessage.HARD_ID_CH_COUP_CH1);
        }
        sendFpgaMsg(FPGAMessage.FPGA_CMD_RUN_STOP|arg);
        sendEvent(EventFactory.EVENT_SCOPE_STATE);
        sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);
        sendUiMsg(UiMessage.UI_MESSAGE_SIMPLE_WIN_DIS);
    }
    
    /**
     * 处理单次采样事件
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>响应单次采样模式启用</li>
     *   <li>调用SampleRunChange并发送单次采样命令</li>
     *   <li>发送单次采样事件通知</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 调用SampleRunChange(FPGA_CMD_SINGLE)
     *    └─→ 启动采样并发送单次采样命令
     * 2. 发送EVENT_SCOPE_SINGLE事件
     *    └─→ 通知其他模块进入单次采样模式
     * </pre>
     * 
     * <p><b>FPGA命令：</b>
     * <ul>
     *   <li>FPGA_CMD_RUN_STOP：运行命令</li>
     *   <li>FPGA_CMD_SINGLE：单次采样命令</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户按下Single按钮时触发</li>
     *   <li>启用单次采样模式时</li>
     * </ul>
     */
    public void SampleSingle(){
        SampleRunChange(FPGAMessage.FPGA_CMD_SINGLE);
        sendEvent(EventFactory.EVENT_SCOPE_SINGLE,true);
    }



}
