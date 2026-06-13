package com.micsig.tbook.scope.Auto;  // 定义包名：示波器自动设置功能模块

import com.micsig.tbook.scope.Action.FPGAMessage;  // 导入FPGAMessage类：FPGA消息常量定义
import com.micsig.tbook.scope.Action.UiMessage;  // 导入UiMessage类：UI消息常量定义
import com.micsig.tbook.scope.Action.XAction;  // 导入XAction类：动作代理基类，提供消息发送方法
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂，用于发送事件
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理，提供通道采样状态查询
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂，提供通道类型判断

/**
 * 频率计数器 - 信号频率测量功能管理
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Auto（示波器自动设置功能模块）</li>
 *   <li>架构层级：业务逻辑层 - 功能管理层</li>
 *   <li>设计模式：单例模式（静态内部类持有者模式）</li>
 *   <li>职责类型：频率测量通道管理、频率值存储与通知</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理频率测量的目标通道</li>
 *   <li>存储和提供频率测量结果</li>
 *   <li>控制频率计数器功能的启用/禁用</li>
 *   <li>发送频率更新事件通知UI层</li>
 * </ul>
 * 
 * <p><b>频率计数器工作流程：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   FreqCounter ─ 频率计数器工作流程                                        │
 * │                                                                          │
 * │   1. 设置测量通道（setChIdx）：                                            │
 * │      ├── 检查是否为动态通道                                               │
 * │      ├── 启用/禁用频率计数器功能                                           │
 * │      ├── 发送FPGA采样模式命令                                             │
 * │      ├── 发送UI刷新消息                                                   │
 * │      └── 发送频率计数器事件                                               │
 * │                                                                          │
 * │   2. 频率测量（由FreqCounterManage执行）：                                 │
 * │      ├── 获取FPGA状态数据                                                 │
 * │      ├── 计算信号频率                                                     │
 * │      └── 调用setFreqVal()更新频率值                                       │
 * │                                                                          │
 * │   3. 频率值更新（setFreqVal）：                                           │
 * │      ├── 存储新的频率值                                                   │
 * │      └── 发送频率计数器事件通知UI刷新                                      │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <ul>
 *   <li>继承：XAction（动作代理基类，提供sendFpgaMsg/sendUiMsg/sendEvent方法）</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：AutoService（自动设置服务，控制频率计数器启用状态）</li>
 *   <li>依赖：Scope（示波器核心管理，查询通道采样状态）</li>
 *   <li>依赖：ChannelFactory（通道工厂，判断通道类型）</li>
 *   <li>依赖：EventFactory（事件工厂，发送频率更新事件）</li>
 *   <li>被依赖：FreqCounterManage（频率计数器管理器，调用setFreqVal）</li>
 *   <li>被依赖：UI层（监听EVENT_FREQ_COUNTER事件）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-7-13
 * @see XAction 动作代理基类
 * @see AutoService 自动设置服务
 * @see FreqCounterManage 频率计数器管理器
 */
public class FreqCounter extends XAction {
    
    // ==================== 单例模式实现（静态内部类持有者模式） ====================
    
    /**
     * 静态内部类：持有FreqCounter单例实例
     * 
     * <p>利用JVM类加载机制保证线程安全的懒加载单例。
     */
    private static class FreqCounterHolder {
        /** FreqCounter单例实例：final保证不可变，static保证类级别唯一 */
        public static final FreqCounter instance = new FreqCounter();  // 创建并持有唯一的FreqCounter实例
    }

    /**
     * 获取FreqCounter单例实例
     * 
     * <p>通过静态内部类持有者模式实现线程安全的懒加载单例。
     * 
     * @return FreqCounter单例实例
     */
    public static FreqCounter getInstance() {
        return FreqCounter.FreqCounterHolder.instance;  // 返回静态内部类持有的单例实例
    }
    
    // ==================== 成员变量 ====================
    
    /** 频率测量的目标通道索引：指定要测量哪个通道的信号频率 */
    private int chIdx = 0;  // 默认为通道0
    
    /** 频率测量结果：单位Hz，-1表示无效，>0表示有效频率值 */
    private double freqVal = 0;  // 初始为0，表示未测量


    // ==================== 通道管理方法 ====================
    
    /**
     * 获取频率测量的目标通道索引
     * 
     * @return 目标通道索引（0-based）
     */
    public int getChIdx() {
        return chIdx;  // 返回目标通道索引
    }

    /**
     * 设置频率测量的目标通道
     * 
     * <p>设置要测量频率的通道，并根据通道类型决定是否启用频率计数器功能。
     * 仅动态通道（模拟通道）支持频率测量。
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>检查通道是否为动态通道</li>
     *   <li>动态通道：设置通道索引并启用频率计数器</li>
     *   <li>非动态通道：禁用频率计数器并重置通道索引</li>
     *   <li>重置频率值为无效状态</li>
     *   <li>发送FPGA采样模式命令</li>
     *   <li>发送UI刷新消息</li>
     *   <li>发送频率计数器事件</li>
     * </ol>
     * 
     * @param chIdx 目标通道索引
     */
    public void setChIdx(int chIdx) {
        if(ChannelFactory.isDynamicCh(chIdx)){  // 检查是否为动态通道（模拟通道）
            this.chIdx = chIdx;  // 设置目标通道索引
            AutoService.setFreqCounter(true);  // 启用频率计数器功能
        }else{  // 非动态通道（如数字通道、总线通道等）
            AutoService.setFreqCounter(false);  // 禁用频率计数器功能
            this.chIdx = 0;  // 重置通道索引为默认值
        }
        freqVal = -1;  // 重置频率值为无效状态（-1表示待测量）
        sendFpgaMsg(FPGAMessage.FPGA_CMD_SAMP_MODE);  // 发送FPGA采样模式命令，触发重新采样
        sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);  // 发送UI消息刷新存储深度和采样率显示
        sendEvent(EventFactory.EVENT_FREQ_COUNTER);  // 发送频率计数器事件，通知UI更新
//        Logger.d("zhuzh","EVENT_FREQ_COUNTER");
    }

    // ==================== 频率值管理方法 ====================
    
    /**
     * 检查频率测量结果是否有效
     * 
     * <p>需要同时满足两个条件：
     * <ol>
     *   <li>目标通道在采样列表中</li>
     *   <li>频率值大于0（有效测量结果）</li>
     * </ol>
     * 
     * @return true表示频率测量结果有效，false表示无效
     */
    public boolean IsVaild(){

        return Scope.getInstance().isChannelInSample(chIdx) && freqVal > 0;  // 检查通道是否在采样中且频率值有效
    }

    /**
     * 获取频率测量结果
     * 
     * @return 频率值，单位Hz。-1表示无效，>0表示有效频率值
     */
    public double getFreqVal() {
        return freqVal;  // 返回频率测量结果
    }

    /**
     * 设置频率测量结果
     * 
     * <p>由FreqCounterManage在频率测量完成后调用，更新频率值并通知UI。
     * 
     * @param freqVal 频率值，单位Hz
     */
    public void setFreqVal(double freqVal) {
        this.freqVal = freqVal;  // 存储新的频率值
        //Logger.d("ch  = " + chIdx + ",freq = " + freqVal + " Hz");
        sendEvent(EventFactory.EVENT_FREQ_COUNTER);  // 发送频率计数器事件，通知UI刷新显示
    }

    // ==================== 状态查询方法 ====================
    
    /**
     * 检查频率计数器功能是否启用
     * 
     * <p>委托给AutoService查询频率计数器的启用状态。
     * 
     * @return true表示频率计数器已启用，false表示未启用
     */
    public boolean isFreqCounterEnable(){
        return AutoService.isFreqCounterEnable();  // 委托AutoService查询启用状态
    }
}
