package com.micsig.tbook.scope.Action;  // 定义包名：示波器动作处理模块

import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂，用于发送UI刷新事件

/**
 * UI消息处理与界面刷新调度中心
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Action（示波器动作处理模块）</li>
 *   <li>架构层级：业务逻辑层 - UI命令调度层</li>
 *   <li>设计模式：命令模式 + 位掩码合并 + 事件驱动</li>
 *   <li>职责类型：UI命令定义、命令队列管理与界面刷新调度</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义和管理UI刷新命令常量（存储深度/采样率、波形示例图、时基变化、缩放进入）</li>
 *   <li>维护UI命令队列（ui_action），支持命令合并与批量执行</li>
 *   <li>执行UI命令调度，通过EventFactory发送事件通知UI层刷新</li>
 *   <li>避免频繁的UI刷新操作，提升界面响应性能</li>
 * </ul>
 * 
 * <p><b>UI命令寄存器布局（32位ui_action）：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   ui_action ─ UI命令寄存器 (32位)                                         │
 * │                                                                          │
 * │   bit[0] ─ 存储深度和采样率刷新 (UI_MESSAGE_DEPTH_SAMPFRE)                │
 * │   bit[1] ─ 波形示例图刷新 (UI_MESSAGE_SIMPLE_WIN_DIS)                    │
 * │   bit[2] ─ 时基档位变化 (UI_MESSAGE_TIME_SCALE_CHANGE)                   │
 * │   bit[3] ─ 进入缩放模式 (UI_MESSAGE_ZOOM_ENTER)                          │
 * │   bit[4-31] ─ 保留位（未使用）                                             │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>命令合并策略：</b>
 * <ul>
 *   <li>使用位或操作（|=）将多个命令合并到命令寄存器中</li>
 *   <li>延迟执行策略：先收集命令，在run()中批量执行</li>
 *   <li>通过mNum计数器控制首次添加时立即触发执行</li>
 * </ul>
 * 
 * <p><b>与其他Message类的区别：</b>
 * <ul>
 *   <li>FPGAMessage：管理FPGA内部寄存器命令（采样、触发、显示等）</li>
 *   <li>HardwareMessage：管理外部硬件控制命令（继电器、PGA、AD等）</li>
 *   <li>UiMessage：管理UI界面刷新命令（存储深度、波形示例、时基、缩放）</li>
 *   <li>三者协同工作，分别处理硬件、FPGA和UI三个层面的命令调度</li>
 * </ul>
 * 
 * <p><b>事件驱动架构：</b>
 * <ul>
 *   <li>通过EventFactory发送事件，解耦业务逻辑层和UI层</li>
 *   <li>UI层通过事件订阅机制接收刷新通知</li>
 *   <li>支持多个UI组件同时响应同一事件</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：EventFactory（事件工厂，发送UI刷新事件）</li>
 *   <li>被依赖：ScopeMessage（消息处理中心，调用add/run）</li>
 *   <li>被依赖：各种Action类（添加UI刷新命令）</li>
 * </ul>
 * 
 * @author xuj
 * @version 1.0
 * @since 2018/9/4
 * @see EventFactory 事件工厂
 * @see FPGAMessage FPGA消息处理
 * @see HardwareMessage 硬件消息处理
 */
public class UiMessage {

    private final String TAG="UiMessage";  // 日志标签：用于Log输出时标识来源
    
    // ==================== UI命令常量定义 (ui_action bit 0-3) ====================
    
    /** 存储深度和采样率刷新命令：通知UI更新存储深度和采样率显示 */
    public static final int UI_MESSAGE_DEPTH_SAMPFRE = 1<<0;  // bit[0] = 0x00000001
    
    /** 波形示例图刷新命令：通知UI更新波形示例窗口显示 */
    public static final int UI_MESSAGE_SIMPLE_WIN_DIS = 1<<1;  // bit[1] = 0x00000002
    
    /** 时基档位变化命令：通知UI时基档位已修改，需要更新相关显示 */
    public static final int UI_MESSAGE_TIME_SCALE_CHANGE = 1<<2;  // bit[2] = 0x00000004
    
    /** 进入缩放模式命令：通知UI进入缩放显示模式 */
    public static final int UI_MESSAGE_ZOOM_ENTER = 1<<3;  // bit[3] = 0x00000008

    // ==================== 成员变量 ====================
    
    /** UI命令寄存器：32位，存储待执行的UI命令位掩码 */
    private int ui_action=0;  // 初始值为0，表示无待执行命令
    
    /** 命令计数器：记录添加的命令次数，用于控制延迟执行时机 */
    private int mNum=0;  // 初始值为0

    // ==================== 构造方法 ====================
    
    /**
     * 构造方法：初始化UI消息处理器
     * 
     * <p>构造时清除命令寄存器和计数器，确保初始状态干净。
     */
    public UiMessage(){
        clr_Cmd();  // 清除命令寄存器和计数器
    }

    // ==================== 命令队列管理 ====================
    
    /**
     * 添加UI命令到命令队列
     * 
     * <p>使用位或操作将命令合并到命令寄存器中，支持多个命令的批量执行。
     * 每次添加命令时递增计数器，用于控制首次添加时的立即执行。
     * 
     * @param arg UI命令位掩码（如UI_MESSAGE_DEPTH_SAMPFRE等）
     */
    public void add(int arg){
        ui_action |= arg;  // 将命令位合并到命令寄存器
        mNum++;  // 递增命令计数器
    }
    
    /**
     * 检查是否需要延迟执行
     * 
     * <p>当命令寄存器非零且计数器为1时返回true，表示刚添加第一个命令，
     * 需要触发延迟执行机制。这样可以确保多个命令合并后再统一执行。
     * 
     * @return true表示需要延迟执行（刚添加第一个命令），false表示无需延迟
     */
    public boolean isDelayRun(){
        return (ui_action != 0 && mNum == 1);  // 有命令且是第一个命令时返回true
    }

    // ==================== 命令执行调度 ====================
    
    /**
     * 执行UI命令调度
     * 
     * <p>解析命令寄存器中的命令位，依次发送对应的UI刷新事件。
     * 执行完成后清除命令寄存器。
     * 
     * <p><b>执行的事件类型：</b>
     * <ul>
     *   <li>UI_MESSAGE_DEPTH_SAMPFRE → EVENT_UI_DEPTH_SAMPFRE_REFLASH</li>
     *   <li>UI_MESSAGE_SIMPLE_WIN_DIS → EVENT_UI_SAMPLE_GRAPH</li>
     *   <li>UI_MESSAGE_TIME_SCALE_CHANGE → EVENT_TIME_SCALE</li>
     *   <li>UI_MESSAGE_ZOOM_ENTER → EVENT_DISPLAY_ZOOM_ENTER</li>
     * </ul>
     */
    public void run(){
        if((ui_action & UI_MESSAGE_DEPTH_SAMPFRE) != 0){  // 检查是否有存储深度/采样率刷新命令
//            if(Scope.getInstance().isRun()) {  // 原逻辑：仅在运行状态下刷新（已注释）
                EventFactory.sendEvent(EventFactory.EVENT_UI_DEPTH_SAMPFRE_REFLASH);  // 发送存储深度和采样率刷新事件
//            }
            //Logger.i(TAG, "send UI_MESSAGE_DEPTH_SAMPFRE");  // 原日志输出（已注释）
        }
        if((ui_action & UI_MESSAGE_SIMPLE_WIN_DIS) != 0){  // 检查是否有波形示例图刷新命令
            EventFactory.sendEvent(EventFactory.EVENT_UI_SAMPLE_GRAPH);  // 发送波形示例图刷新事件
            //Logger.i(TAG, "send UI_MESSAGE_SIMPLE_WIN_DIS");  // 原日志输出（已注释）
        }
        if((ui_action & UI_MESSAGE_TIME_SCALE_CHANGE) != 0){  // 检查是否有时基变化命令
            EventFactory.sendEvent(EventFactory.EVENT_TIME_SCALE);  // 发送时基变化事件
            //Logger.i(TAG, "send UI_MESSAGE_TIME_SCALE_CHANGE");  // 原日志输出（已注释）
        }
        if((ui_action & UI_MESSAGE_ZOOM_ENTER) != 0){  // 检查是否有进入缩放模式命令
            EventFactory.sendEvent(EventFactory.EVENT_DISPLAY_ZOOM_ENTER);  // 发送进入缩放模式事件
            //Logger.i(TAG, "send UI_MESSAGE_ZOOM_ENTER");  // 原日志输出（已注释）
        }
        clr_Cmd();  // 清除命令寄存器和计数器，准备下一轮命令收集
    }
    
    /**
     * 清除命令寄存器和计数器
     * 
     * <p>重置ui_action和mNum为初始值，用于命令执行完成后的清理，
     * 或构造时的初始化。
     */
    private void clr_Cmd(){
        ui_action = 0;  // 清零命令寄存器
        mNum = 0;  // 清零命令计数器
    }

}
