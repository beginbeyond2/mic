package com.micsig.tbook.scope.Auto;  // 定义包名：示波器自动设置功能模块


import com.micsig.base.Logger;  // 导入Logger类：项目基础日志工具
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入HwConfig类：硬件配置管理，提供ADC通道数和FPGA数量
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理，提供通道采样状态
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入Trigger类：触发管理
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入TriggerFactory类：触发工厂
import com.micsig.tbook.scope.channel.Channel;  // 导入Channel类：通道数据模型
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂，提供通道遍历和索引常量
import com.micsig.tbook.scope.fpga.FPGA_Status;  // 导入FPGA_Status类：FPGA状态数据结构

import java.util.Arrays;  // 导入Arrays类：数组操作工具

/**
 * 边沿触发自动调整策略 - 普通通道自动设置实现
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Auto（示波器自动设置功能模块）</li>
 *   <li>架构层级：业务逻辑层 - 自动设置策略层</li>
 *   <li>设计模式：策略模式（Strategy Pattern）</li>
 *   <li>职责类型：边沿触发自动调整、多通道协调</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理所有通道的AutoChannel实例</li>
 *   <li>执行边沿触发模式的自动调整</li>
 *   <li>处理用户手动修改通知</li>
 *   <li>检测小信号通道并关闭</li>
 *   <li>自动选择最佳触发源</li>
 * </ul>
 * 
 * <p><b>状态机设计：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   EdgeAutoAtion 状态机                                                    │
 * │                                                                          │
 * │   state = 0：初始状态                                                     │
 * │   ├── 自动量程模式：直接进入 state = 1                                     │
 * │   └── 单次自动模式：等待5次循环后进入 state = 1                             │
 * │                                                                          │
 * │   state = 1：调整中状态                                                   │
 * │   ├── 调整完成（bRet=false）：进入 state = 2                              │
 * │   └── 继续调整                                                            │
 * │                                                                          │
 * │   state = 2：稳定状态                                                     │
 * │   └── 等待5次循环后进入 state = 3（完成）                                  │
 * │                                                                          │
 * │   state = 3：完成状态（返回true，结束自动设置）                             │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>自动调整流程：</b>
 * <ol>
 *   <li>遍历所有采样通道，获取FPGA状态数据</li>
 *   <li>调用各通道的AutoChannel.autoWork()执行调整</li>
 *   <li>检测小信号通道（低于阈值）并关闭</li>
 *   <li>自动选择最佳触发源（最大信号通道）</li>
 *   <li>重置通道位置以优化显示</li>
 * </ol>
 * 
 * <p><b>与SerialBusAutoAtion的区别：</b>
 * <ul>
 *   <li>EdgeAutoAtion：处理普通模拟通道，基于信号周期和幅度调整</li>
 *   <li>SerialBusAutoAtion：处理串行总线通道，基于波特率调整</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：AutoAtion（自动调整策略基类）</li>
 *   <li>依赖：AutoChannel（单通道自动调整处理器）</li>
 *   <li>依赖：Scope（示波器核心管理）</li>
 *   <li>依赖：Trigger（触发管理）</li>
 *   <li>依赖：HwConfig（硬件配置）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-7-3
 * @see AutoAtion 自动调整策略基类
 * @see AutoChannel 单通道自动调整处理器
 * @see SerialBusAutoAtion 串行总线自动调整策略
 */
public class EdgeAutoAtion extends AutoAtion {

    /** 通道自动调整处理器数组：每个通道对应一个AutoChannel实例 */
    AutoChannel [] autoChannels = new AutoChannel[ChannelFactory.CH_CNT];  // 数组大小为通道总数

    /** 状态机状态：0=初始，1=调整中，2=稳定，3=完成 */
    private int state = 0;  // 初始状态为0
    
    /** 自动调整计数器：用于状态转换的延时控制 */
    private int auto_cnt = 0;  // 初始为0


    // ==================== 构造方法 ====================
    
    /**
     * 构造方法：初始化边沿触发自动调整策略
     * 
     * <p>创建所有通道的AutoChannel实例，用于后续的自动调整处理。
     * 
     * @param auto 自动设置配置对象
     */
    public EdgeAutoAtion(Auto auto) {
        super(auto);  // 调用父类构造方法
        ChannelFactory.forEachCh(channel -> {  // 遍历所有通道
            autoChannels[channel.getChId()] = new AutoChannel(auto,channel);  // 为每个通道创建AutoChannel实例
        });

    }

    // ==================== 手动修改通知方法 ====================
    
    /**
     * 处理垂直档位手动修改通知
     * 
     * <p>当用户手动修改垂直档位时，通知对应通道的AutoChannel暂停垂直自动调整。
     * 仅处理动态通道（模拟通道）。
     * 
     * @param chIdx 被修改的通道索引
     */
    @Override
    public void onModifyVertical(int chIdx) {
        if(ChannelFactory.isDynamicCh(chIdx)){  // 检查是否为动态通道（模拟通道）
            autoChannels[chIdx].modifyVertical();  // 通知对应通道的AutoChannel
        }
    }

    /**
     * 处理时基手动修改通知
     * 
     * <p>当用户手动修改时基时，通知触发源通道的AutoChannel暂停水平自动调整。
     * 仅处理动态通道。
     */
    @Override
    public void onModifyTimebase() {
        Trigger trigger = TriggerFactory.getTriggerObj();  // 获取触发对象
        int chIdx = trigger.getTriggerSource();  // 获取当前触发源通道索引
        if(ChannelFactory.isDynamicCh(chIdx)){  // 检查是否为动态通道
            autoChannels[chIdx].modifyTimebase();  // 通知触发源通道的AutoChannel
        }
    }

    /**
     * 处理触发电平手动修改通知
     * 
     * <p>当用户手动修改触发电平时，通知触发源通道的AutoChannel暂停触发电平自动调整。
     * 仅处理动态通道。
     */
    @Override
    public void onModifyLevel() {
        Trigger trigger = TriggerFactory.getTriggerObj();  // 获取触发对象
        int chIdx = trigger.getTriggerSource();  // 获取当前触发源通道索引
        if(ChannelFactory.isDynamicCh(chIdx)){  // 检查是否为动态通道
            autoChannels[chIdx].modifyLevel();  // 通知触发源通道的AutoChannel
        }
    }



    // ==================== 核心自动调整方法 ====================
    
    /**
     * 执行自动调整动作
     * 
     * <p>在每次FPGA状态更新时调用，执行自动调整逻辑。
     * 使用状态机控制调整流程。
     * 
     * <p><b>状态机流程：</b>
     * <ul>
     *   <li>state 0 → 1：初始化，单次模式需等待5次循环</li>
     *   <li>state 1 → 2：调整完成，进入稳定状态</li>
     *   <li>state 2 → 3：稳定完成，结束自动设置</li>
     * </ul>
     * 
     * @param fpgaStatus FPGA状态数据，包含各通道的采样数据
     * @return true表示自动调整完成，false表示需要继续调整
     */
    @Override
    public boolean onAtion(FPGA_Status fpgaStatus) {

        boolean bRet = false;  // 自动调整结果标志
        if(fpgaStatus.getAuto(0).isVaild()){  // 检查自动设置数据是否有效

            bRet = autoWork(fpgaStatus);  // 执行自动调整，返回true表示所有通道调整完成

            switch (state){  // 状态机处理
                case 1:  // 调整中状态
                    if(!bRet){  // 如果调整完成（bRet=false表示无需再调整）
                        auto_cnt = 0;  // 重置计数器
                        state = 2;  // 进入稳定状态
                        break;  // 退出switch
                    }
                case 0:  // 初始状态（注意：case 1没有break会继续执行）
                    if(auto.isAutoRangeEnable()){  // 自动量程模式
                        state = 1;  // 直接进入调整中状态
                    }else{  // 单次自动模式
                        if(++auto_cnt >5) {  // 等待5次循环
                            dealTiny();  // 处理小信号通道
                            state = 1;  // 进入调整中状态
                        }
                    }
                    break;  // 退出switch
                case 2:  // 稳定状态
                    if(++auto_cnt > 5) {  // 等待5次循环
                        if(!auto.isAutoRangeEnable())  // 单次自动模式
                            state = 3;  // 进入完成状态
                        auto_cnt = 0;  // 重置计数器
                    }
                    break;  // 退出switch
            }
            if(auto.isAutoRangeEnable() && isAutoRangeEnd()){  // 自动量程模式且所有调整完成
                state = 3;  // 进入完成状态
            }
        }
        return state == 3;  // 返回是否完成（state=3表示完成）
    }

    /**
     * 执行所有通道的自动调整
     * 
     * <p>遍历所有采样的通道，获取FPGA状态数据并执行自动调整。
     * 根据通道数量和FPGA配置计算数据索引。
     * 
     * @param fpgaStatus FPGA状态数据
     * @return true表示所有通道调整完成，false表示有通道需要继续调整
     */
    boolean autoWork(FPGA_Status fpgaStatus){


        boolean [] bRet = new boolean[ChannelFactory.CH_CNT];  // 各通道调整结果数组
        Arrays.fill(bRet,true);  // 初始化为true（已完成）
        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
        boolean [] sel = new boolean[ChannelFactory.CH_CNT];  // 通道选择数组
        int cnt = scope.getChannelSampOnCnt(true,sel);  // 获取采样通道数量，并填充sel数组
        int idx = 0;  // FPGA数据索引
        int k = ChannelFactory.CH_CNT / cnt;  // 通道间隔（用于计算数据索引）
        int maxChIdx = ChannelFactory.getMaxChIdx();  // 获取最大通道索引
        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置单例
        int nums = hwConfig.getAdcMaxChNums();  // 获取每个FPGA的ADC通道数
        int mm = ChannelFactory.CH_CNT / hwConfig.getFpgaNums();  // 每个FPGA管理的通道数
        for(int i=0;i<maxChIdx;i++){  // 遍历所有通道
            if(sel[i]){  // 检查通道是否在采样列表中
                if(cnt <= 2){  // 如果采样通道数<=2，需要特殊计算索引
                    idx = ((i % mm) / nums) * nums + (i / mm) * mm;  // 计算FPGA数据索引
                }
                FPGA_Status.FpgaAuto fpgaAuto = null;  // FPGA自动设置数据引用
                fpgaAuto = fpgaStatus.getAuto(idx);  // 获取指定索引的FPGA自动设置数据
                bRet[i] = autoChannels[i].autoWork(  // 执行通道自动调整
                        fpgaAuto.getMinVal(),  // 信号最小值
                        fpgaAuto.getMaxVal(),  // 信号最大值
                        fpgaAuto.getCycle()  // 信号周期
                );
                idx += k;  // 更新索引，跳到下一个采样通道的数据
            }
        }

        for (boolean v:bRet){  // 遍历所有通道的调整结果
            if(!v){  // 如果有通道未完成调整
                return false;  // 返回false，表示需要继续调整
            }
        }
        return true;  // 所有通道都完成调整，返回true
    }

    /**
     * 检查自动量程是否结束
     * 
     * <p>检查所有采样通道的垂直、水平、触发电平自动调整是否都已禁用。
     * 当所有自动调整都被手动禁用时，表示自动量程结束。
     * 
     * @return true表示自动量程已结束，false表示仍在调整
     */
    boolean isAutoRangeEnd(){

        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
        Trigger trigger = TriggerFactory.getTriggerObj();  // 获取触发对象
        int chIdx = trigger.getTriggerSource();  // 获取触发源通道索引
        boolean[] bEnd = {!(autoChannels[chIdx].isTriggerLevelAuto()  // 检查触发源通道的触发电平自动调整是否禁用
                || autoChannels[chIdx].isHorizontalAuto())};  // 检查触发源通道的水平自动调整是否禁用

        if(bEnd[0]) {  // 如果触发源通道的自动调整都已禁用
            ChannelFactory.forEachCh(channel -> {  // 遍历所有通道
                int i = channel.getChId();  // 获取通道ID
                if (scope.isChannelInSample(i)) {  // 检查通道是否在采样列表中
                    bEnd[0] = bEnd[0] && !autoChannels[i].isVerticalAuto();  // 检查垂直自动调整是否禁用
                }
            });
        }

        return bEnd[0];  // 返回是否所有自动调整都已禁用
    }



    /**
     * 处理小信号通道
     * 
     * <p>检测信号幅度低于阈值的通道，并关闭这些通道。
     * 同时自动选择最佳触发源（信号幅度最大的通道）。
     * 
     * <p><b>处理逻辑：</b>
     * <ol>
     *   <li>遍历所有开启的通道，获取信号峰峰值</li>
     *   <li>标记信号幅度低于阈值的通道为待关闭</li>
     *   <li>选择信号幅度最大的通道作为触发源</li>
     *   <li>关闭小信号通道</li>
     *   <li>重置剩余通道的垂直位置</li>
     * </ol>
     */
    void dealTiny(){

        double MaxPeak = 0;  // 最大峰峰值
        int MaxCh = -1;  // 最大峰峰值对应的通道索引，初始为-1
        double val = 0;  // 当前通道的峰峰值
        int cnt = 0;  // 有效通道计数
        // 适配外部触发越界问题
        boolean[] off = {false, false, false, false, false, false, false, false, false};  // 通道关闭标志数组
        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例

        for(int i=0;i<scope.getChNum();i++){  // 遍历所有通道
            if(ChannelFactory.isChOpen(i)){  // 检查通道是否开启
                val = autoChannels[i].getPeakVal();  // 获取通道的峰峰值
                if(MaxCh < 0){  // 如果还没有记录最大通道
                    MaxCh = i;  // 记录当前通道为最大通道
                    MaxPeak = val;  // 记录当前峰值为最大峰值
                }
                if(auto.isAutoChannelEnable()){  // 检查是否启用自动通道检测
                    if(val < auto.getAutoThresholdLevel()){  // 检查信号是否低于阈值
                        off[i] = true;  // 标记为待关闭
                    }else{  // 信号有效
                        cnt++;  // 有效通道计数加1
                    }
                }

                if(auto.getAutoTriggerSource() == 1){  // 检查是否启用自动触发源选择
                    Logger.d("i:" + i + "val:" + val + "MaxPeak:" + MaxPeak);  // 输出调试日志
                    if( val > MaxPeak){  // 如果当前峰值大于最大峰值
                        MaxPeak = val;  // 更新最大峰值
                        MaxCh = i;  // 更新最大通道索引
                    }
                }
            }
        }

        Trigger trigger = TriggerFactory.getTriggerObj();  // 获取触发对象
        int chIdx = trigger.getTriggerSource();  // 获取当前触发源通道索引
//        if(auto.isAutoChannelEnable())
        {
            if (auto.getAutoTriggerSource() == 1) {  // 自动触发源选择模式
                if(ChannelFactory.isDynamicCh(MaxCh)) {  // 检查最大通道是否为动态通道
                    if (chIdx != MaxCh) {  // 如果当前触发源不是最大通道
                        trigger.setTriggerSource(MaxCh);  // 设置触发源为最大通道
                    }
                    off[MaxCh] = false;  // 确保最大通道不被关闭
                }
            } else {  // 保持当前触发源模式
                if (cnt == 0) {  // 如果没有有效通道
                    off[chIdx] = false;  // 保持当前触发源通道开启
                }
                if (auto.isAutoChannelEnable()) {  // 检查是否启用自动通道检测
                    if (off[chIdx]) {  // 如果当前触发源通道被标记为关闭
                        for (int i = 0; i < scope.getChNum(); i++) {  // 遍历所有通道
                            if (!off[i]) {  // 找到第一个有效通道
                                trigger.setTriggerSource(i);  // 设置为触发源
                                break;  // 退出循环
                            }
                        }
                    }
                }
            }
        }

        boolean bTz = false;  // 通道关闭标志
        if(auto.isAutoChannelEnable()) {  // 检查是否启用自动通道检测
            for(int i=0;i<scope.getChNum();i++) {  // 遍历所有通道
                if(ChannelFactory.isChOpen(i) && off[i]) {  // 检查通道是否开启且被标记为关闭
                    ChannelFactory.chClose(i);  // 关闭通道
                    bTz = true;  // 标记有通道被关闭
                }
            }
        }

        if(bTz) {  // 如果有通道被关闭
            resetChPos();  // 重置剩余通道的垂直位置（父类方法）
        }

    }
}
