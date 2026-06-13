package com.micsig.tbook.scope.Auto;  // 定义包名：示波器自动设置功能模块

import android.util.Log;  // 导入Log类：Android日志输出工具

import com.micsig.base.Logger;  // 导入Logger类：项目基础日志工具
import com.micsig.tbook.scope.Bus.ARINC429Bus;  // 导入ARINC429Bus类：ARINC429航空总线配置
import com.micsig.tbook.scope.Bus.CanBus;  // 导入CanBus类：CAN总线配置
import com.micsig.tbook.scope.Bus.I2CBus;  // 导入I2CBus类：I2C总线配置
import com.micsig.tbook.scope.Bus.IBus;  // 导入IBus类：总线接口定义
import com.micsig.tbook.scope.Bus.LinBus;  // 导入LinBus类：LIN总线配置
import com.micsig.tbook.scope.Bus.MILSTD1553BBus;  // 导入MILSTD1553BBus类：MIL-STD-1553B军用总线配置
import com.micsig.tbook.scope.Bus.SpiBus;  // 导入SpiBus类：SPI总线配置
import com.micsig.tbook.scope.Bus.UartBus;  // 导入UartBus类：UART总线配置
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入HwConfig类：硬件配置管理
import com.micsig.tbook.scope.Display.Display;  // 导入Display类：显示管理
import com.micsig.tbook.scope.Sample.MemDepthFactory;  // 导入MemDepthFactory类：存储深度工厂
import com.micsig.tbook.scope.Sample.Sample;  // 导入Sample类：采样管理
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理
import com.micsig.tbook.scope.ScopeBase;  // 导入ScopeBase类：示波器基础配置常量
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入Trigger类：触发管理
import com.micsig.tbook.scope.Trigger.TriggerCommon;  // 导入TriggerCommon类：通用触发配置
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入TriggerFactory类：触发工厂
import com.micsig.tbook.scope.channel.Channel;  // 导入Channel类：通道数据模型
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂
import com.micsig.tbook.scope.channel.SerialChannel;  // 导入SerialChannel类：串行通道（总线解码）
import com.micsig.tbook.scope.fpga.FPGA_Status;  // 导入FPGA_Status类：FPGA状态数据结构
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入HorizontalAxis类：水平轴管理
import com.micsig.tbook.scope.measure.Measure;  // 导入Measure类：测量功能
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入VerticalAxis类：垂直轴管理

import java.util.Arrays;  // 导入Arrays类：数组操作工具

/**
 * 串行总线自动调整策略 - 总线解码通道自动设置实现
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Auto（示波器自动设置功能模块）</li>
 *   <li>架构层级：业务逻辑层 - 自动设置策略层</li>
 *   <li>设计模式：策略模式（Strategy Pattern）</li>
 *   <li>职责类型：串行总线自动调整、多通道协调</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理所有通道的AutoChannel实例</li>
 *   <li>执行串行总线模式的自动调整</li>
 *   <li>处理用户手动修改通知</li>
 *   <li>检测小信号通道并关闭</li>
 *   <li>根据总线类型配置触发和时基</li>
 * </ul>
 * 
 * <p><b>支持的串行总线类型：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   SerialBusAutoAtion ─ 支持的总线类型                                     │
 * │                                                                          │
 * │   ├── UART     ─ 通用异步收发传输器                                       │
 * │   │   └── 触发类型：起始位                                                │
 * │   │                                                                      │
 * │   ├── LIN      ─ 本地互联网络                                            │
 * │   │   └── 触发类型：同步上升沿                                            │
 * │   │                                                                      │
 * │   ├── CAN      ─ 控制器局域网                                            │
 * │   │   └── 触发类型：帧起始                                                │
 * │   │                                                                      │
 * │   ├── SPI      ─ 串行外设接口                                            │
 * │   │   └── 触发类型：帧数据                                               │
 * │   │                                                                      │
 * │   ├── I2C      ─ 两线式串行总线                                           │
 * │   │   └── 触发类型：起始条件                                              │
 * │   │                                                                      │
 * │   ├── ARINC429 ─ 航空电子数字数据传输标准                                 │
 * │   │   └── 触发类型：字起始                                                │
 * │   │                                                                      │
 * │   └── MIL-STD-1553B ─ 军用串行数据总线标准                                │
 * │       └── 触发类型：命令/状态同步                                          │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>与EdgeAutoAtion的区别：</b>
 * <ul>
 *   <li>EdgeAutoAtion：处理普通模拟通道，基于信号周期和幅度调整</li>
 *   <li>SerialBusAutoAtion：处理串行总线通道，基于波特率调整时基</li>
 *   <li>SerialBusAutoAtion会根据总线类型自动配置触发类型</li>
 *   <li>SerialBusAutoAtion会保护总线通道不被关闭</li>
 * </ul>
 * 
 * <p><b>状态机设计：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   SerialBusAutoAtion 状态机                                               │
 * │                                                                          │
 * │   state = 0：初始状态，等待5次循环后进入 state = 1                         │
 * │   state = 1：调整中状态，调整完成后进入 state = 2                          │
 * │   state = 2：稳定状态，等待5次循环后进入 state = 3                         │
 * │   state = 3：完成状态（返回true，结束自动设置）                             │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：AutoAtion（自动调整策略基类）</li>
 *   <li>依赖：AutoChannel（单通道自动调整处理器）</li>
 *   <li>依赖：各种总线类（UartBus、CanBus、SpiBus等）</li>
 *   <li>依赖：SerialChannel（串行通道管理）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-7-3
 * @see AutoAtion 自动调整策略基类
 * @see AutoChannel 单通道自动调整处理器
 * @see EdgeAutoAtion 边沿触发自动调整策略
 */
public class SerialBusAutoAtion extends AutoAtion  {
    
    // ==================== 成员变量 ====================
    
    /** 通道自动调整处理器数组：每个通道对应一个AutoChannel实例 */
    AutoChannel [] autoChannels = new AutoChannel[ChannelFactory.CH_CNT];  // 数组大小为通道总数
    
    /** 状态机状态：0=初始，1=调整中，2=稳定，3=完成 */
    private int state = 0;  // 初始状态为0
    
    /** 自动调整计数器：用于状态转换的延时控制 */
    private int auto_cnt = 0;  // 初始为0
    
    // ==================== 构造方法 ====================
    
    /**
     * 构造方法：初始化串行总线自动调整策略
     * 
     * <p>创建所有通道的AutoChannel实例，用于后续的自动调整处理。
     * 
     * @param auto 自动设置配置对象
     */
    public SerialBusAutoAtion(Auto auto) {
        super(auto);  // 调用父类构造方法
        ChannelFactory.forEachCh((channel -> autoChannels[channel.getChId()] = new AutoChannel(auto,channel)));  // 为每个通道创建AutoChannel实例
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
//                    if(auto.isAutoRangeEnable()){
//                        state = 1;
//                    }else
                    {
                        if(++auto_cnt >5) {  // 等待5次循环
                            dealTiny();  // 处理小信号通道
                            state = 1;  // 进入调整中状态
                        }
                    }
                    break;  // 退出switch
                case 2:  // 稳定状态
                    if(++auto_cnt > 5) {  // 等待5次循环
//                        if(!auto.isAutoRangeEnable())
                        state = 3;  // 进入完成状态
                        auto_cnt = 0;  // 重置计数器
                    }
                    break;  // 退出switch
            }
        }
        return state == 3;  // 返回是否完成（state=3表示完成）
    }
    
    /**
     * 执行所有通道的自动调整
     * 
     * <p>遍历所有采样的通道，获取FPGA状态数据并执行自动调整。
     * 对于串行通道，传入bSerialCh=true参数。
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
                bRet[i] = autoChannels[i].autoWork(  // 执行通道自动调整（串行通道模式）
                        fpgaAuto.getMinVal(),  // 信号最小值
                        fpgaAuto.getMaxVal(),  // 信号最大值
                        fpgaAuto.getCycle()  // 信号周期
                        ,true  // bSerialCh=true，表示串行通道
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
     * 检查通道是否为总线通道
     * 
     * <p>遍历所有串行通道，检查指定通道是否被任何开启的总线使用。
     * 总线通道不应该被自动关闭。
     * 
     * @param chIdx 要检查的通道索引
     * @return true表示该通道是总线通道，false表示不是
     */
    boolean isBusChannel(int chIdx){
        boolean bUse = false;  // 是否为总线通道标志
        SerialChannel serialChannel = null;  // 串行通道引用
        int maxIdx = ChannelFactory.getMaxSerialIdx();  // 获取串行通道最大索引
        for(int i=ChannelFactory.S1;i<maxIdx;i++){  // 遍历所有串行通道
            serialChannel = ChannelFactory.getSerialChannel(i);  // 获取串行通道
            if(serialChannel != null && serialChannel.isOpen() ){  // 检查串行通道是否有效且已开启
                bUse = serialChannel.isChInSample(chIdx);  // 检查指定通道是否在该总线的采样中
            }
//            else{
//                Log.d("zhuzh","serial i:" + i  + "," + serialChannel.isOpen());
//            }
        }

        return bUse;  // 返回是否为总线通道
    }

    /**
     * 处理小信号通道
     * 
     * <p>检测信号幅度低于阈值的通道，并关闭这些通道（总线通道除外）。
     * 同时自动选择最佳触发源（信号幅度最大的通道）。
     * 
     * <p><b>处理逻辑：</b>
     * <ol>
     *   <li>遍历所有开启的通道，获取信号峰峰值</li>
     *   <li>标记信号幅度低于阈值的非总线通道为待关闭</li>
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
        boolean [] off = new boolean[ChannelFactory.CH_CNT];  // 通道关闭标志数组
        Arrays.fill(off,false);  // 初始化为false（不关闭）
        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
        for(int i=0;i<scope.getChNum();i++){  // 遍历所有通道
            if(ChannelFactory.isChOpen(i)){  // 检查通道是否开启
                val = autoChannels[i].getPeakVal();  // 获取通道的峰峰值
                if(auto.isAutoChannelEnable()){  // 检查是否启用自动通道检测
                    boolean isbus = isBusChannel(i);  // 检查是否为总线通道
                    Log.d("zhuzh","chidx:" + i + ",isbus:" + isbus);  // 输出调试日志
                    if((!isbus)  // 非总线通道
                            && (val < auto.getAutoThresholdLevel())){  // 信号低于阈值
                        off[i] = true;  // 标记为待关闭
                    }else{  // 总线通道或信号有效
                        cnt++;  // 有效通道计数加1
                    }
                }

                if(auto.getAutoTriggerSource() == 1){  // 检查是否启用自动触发源选择
                    if( val > MaxPeak){  // 如果当前峰值大于最大峰值
                        MaxPeak = val;  // 更新最大峰值
                        MaxCh = i;  // 更新最大通道索引
                    }
                }
            }
        }

        Trigger trigger = TriggerFactory.getTriggerObj();  // 获取触发对象
        int chIdx = trigger.getTriggerSource();  // 获取当前触发源通道索引
        if(auto.isAutoChannelEnable()) {  // 检查是否启用自动通道检测
            if (auto.getAutoTriggerSource() == 1) {  // 自动触发源选择模式
                if (chIdx != MaxCh) {  // 如果当前触发源不是最大通道
                    trigger.setTriggerSource(MaxCh);  // 设置触发源为最大通道
                }
                off[MaxCh] = false;  // 确保最大通道不被关闭
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
    
    /**
     * 启用通道的周期测量功能
     * 
     * <p>对于SPI和I2C总线，需要启用时钟通道的周期测量功能，
     * 用于自动计算时基档位。
     * 
     * @param chIdx 要启用周期测量的通道索引
     */
    private void EnableCalcWavePeriod(int chIdx){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取动态通道对象
        if(channel != null){  // 检查通道是否有效
            Measure measure = channel.getMeasure();  // 获取测量对象
            if(measure != null){  // 检查测量对象是否有效
                measure.MeasureItemEnable(Measure.MeasureType.MEASURE_PERIOD,true);  // 启用周期测量
            }
        }

    }

    // ==================== 自动设置准备方法 ====================
    
    /**
     * 自动设置准备方法
     * 
     * <p>在启动串行总线自动设置时调用，配置显示模式、触发类型、
     * 时基档位等参数。根据总线类型进行不同的配置。
     * 
     * <p><b>配置内容：</b>
     * <ul>
     *   <li>显示模式：YT模式，线条绘制</li>
     *   <li>通道配置：带宽、耦合、档位、探头比等</li>
     *   <li>触发配置：根据总线类型设置触发类型</li>
     *   <li>时基配置：根据波特率自动计算</li>
     * </ul>
     */
    @Override
    public void autoPrePared() {
        Logger.d("Serial autoPrePared");  // 输出日志
        Display display = Display.getInstance();  // 获取显示管理单例
        display.setDrawType(Display.DRAWTYPE_LINE);  // 设置绘制类型为线条
        if(display.isZoom()) {  // 检查是否处于缩放模式
            display.setZoom(false);  // 关闭缩放模式
        }
        display.setDisplayMode(Display.DISPLAY_YT);  // 设置显示模式为YT模式
        display.setHorRef(Display.HORREF_CENTER);  // 设置水平参考为中心
//        display.setCCT(false);

        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
        Channel channel;  // 通道引用
        for(int i=0;i<scope.getChNum();i++){  // 遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取动态通道

            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL,Channel.getMaxBandWidth());  // 设置全带宽

            channel.setInvert(false);  // 关闭反相
            if(channel.getCoupleType() == Channel.COUPLE_TYPE_GND) {  // 如果耦合方式为GND
                channel.setCoupleType(Channel.COUPLE_TYPE_DC);  // 改为DC耦合
            }
            if(channel.getVerticalMode() == Channel.VERTICAL_MODE_SCREEN_CENTER){  // 如果垂直模式为屏幕中心
                channel.setVerticalMode(Channel.VERTICAL_MODE_CH_ZERO);  // 改为通道零点
            }
            channel.setVScaleId(VerticalAxis.DANG_50mV);  // 设置垂直档位为50mV/格
            channel.setProbeRate(10);  // 设置探头比为10X
            channel.setFineScale(1.0);  // 设置微调为1.0
            if(auto.isAutoChannelEnable()  // 检查是否启用自动通道检测
                    || isBusChannel(i)){  // 或者是总线通道
                if(!ChannelFactory.isChOpen(i)){  // 检查通道是否未开启
                    ChannelFactory.chOpen(i);  // 开启通道
                }
            }
        }
        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();  // 获取通用触发配置
        triggerCommon.setTriggerMode(TriggerCommon.TM_AUTO);  // 设置触发模式为自动
        triggerCommon.setTriggerHoldOffTime(200);  // 设置触发释抑时间为200ns
        Sample.getInstance().setSampleType(Sample.SAMPLE_TYPE_NORMAL);  // 设置采样类型为正常
        MemDepthFactory.getMemDepth().setMemDepthItem(0);  // 设置存储深度为默认值

        HorizontalAxis.getInstance().setTimePosOfView(0);  // 设置时间位置为0

        int baudRate = 0;  // 波特率
        SerialChannel serialChannel;  // 串行通道引用
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();  // 获取水平轴管理单例
        int maxIdx = ChannelFactory.getMaxSerialIdx();  // 获取串行通道最大索引
        for(int i=ChannelFactory.S1;i<maxIdx;i++){  // 遍历所有串行通道
            serialChannel = ChannelFactory.getSerialChannel(i);  // 获取串行通道
            if(serialChannel != null && serialChannel.isOpen()){  // 检查串行通道是否有效且已开启
                triggerCommon.setTriggerType(Trigger.TRIG_TYPE_SERIAL1 + i - ChannelFactory.S1);  // 设置触发类型为串行触发
                IBus bus = serialChannel.getBus();  // 获取总线对象
                Logger.d("autoPrePared : busType = " + bus.getBusType());  // 输出总线类型日志
                switch (bus.getBusType()){  // 根据总线类型配置
                    case IBus.UART:  // UART总线
                    {
                        UartBus uartBus = (UartBus)bus;  // 转换为UART总线
                        uartBus.setTriggerType(UartBus.UART_TRIGGER_START_BIT);  // 设置触发类型为起始位
                        baudRate = uartBus.getBaudRate();  // 获取波特率
                        break;  // 退出case
                    }
                    case IBus.LIN:  // LIN总线
                    {
                        LinBus linBus = (LinBus) bus;  // 转换为LIN总线
                        linBus.setTriggerType(LinBus.LIN_TRIGGER_SYNC_RISING_EDGE);  // 设置触发类型为同步上升沿
                        baudRate = linBus.getBaudRate();  // 获取波特率
                        break;  // 退出case
                    }
                    case IBus.CAN:  // CAN总线
                    {
                        CanBus canBus = (CanBus)bus;  // 转换为CAN总线
                        canBus.setTriggerType(CanBus.CAN_TRIGGER_FRAME_START);  // 设置触发类型为帧起始
                        baudRate = canBus.getBaudRate();  // 获取波特率
                        break;  // 退出case
                    }
                    case IBus.SPI:  // SPI总线
                    {
                        SpiBus spiBus = (SpiBus)bus;  // 转换为SPI总线
                        spiBus.setTriggerType(SpiBus.SPI_TRIGGER_FRAME_DATA);  // 设置触发类型为帧数据
                        spiBus.setTriggerData(0);  // 设置触发数据为0
                        spiBus.setTriggerMask(0);  // 设置触发掩码为0
                        EnableCalcWavePeriod(spiBus.getClkChIdx());  // 启用时钟通道的周期测量
                        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.TSI_5uS);  // 设置时基为5us/格
                        break;  // 退出case
                    }
                    case IBus.I2C:  // I2C总线
                    {
                        I2CBus i2cBus = (I2CBus) bus;  // 转换为I2C总线
                        EnableCalcWavePeriod(i2cBus.getSclChIdx());  // 启用SCL通道的周期测量
                        i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_START_CONDITION);  // 设置触发类型为起始条件
                        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.TSI_100uS);  // 设置时基为100us/格
                        break;  // 退出case
                    }
                    case IBus.ARINC429:  // ARINC429航空总线
                    {
                        ARINC429Bus arinc429Bus = (ARINC429Bus)bus;  // 转换为ARINC429总线
                        arinc429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_WORD_BEGIN);  // 设置触发类型为字起始
                        baudRate = arinc429Bus.getBaudRate();  // 获取波特率
                        break;  // 退出case
                    }
                    case IBus.MILSTD1553B:  // MIL-STD-1553B军用总线
                    {
                        MILSTD1553BBus milstd1553BBus  = (MILSTD1553BBus) bus;  // 转换为MIL-STD-1553B总线
                        milstd1553BBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC);  // 设置触发类型为命令/状态同步
                        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.TSI_10uS);  // 设置时基为10us/格
                        break;  // 退出case
                    }
                }
                break;  // 找到第一个有效的串行通道后退出循环
            }
        }

        if(baudRate > 0) {  // 如果有波特率
            horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));  // 根据波特率计算时基档位
        }
        resetChPos();  // 重置所有通道的垂直位置
    }

    /**
     * 重置所有通道的垂直位置
     * 
     * <p>将所有开启的通道均匀分布在屏幕上。
     * 根据开启的通道数量计算每个通道的位置偏移。
     */
    @Override
    public void resetChPos() {
        Channel channel;  // 通道引用
        int offset = ScopeBase.getHeight()/(ChannelFactory.getDynamicChannelOpenCount() * 2);  // 计算通道间隔
        int nums = 0;  // 已处理通道计数
        Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
        for(int i=0;i<scope.getChNum();i++) {  // 遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取动态通道
            if(channel != null && channel.isOpen()){  // 检查通道是否有效且已开启
                double pos =  (ScopeBase.getHeight() / 2 - offset * (1 + nums * 2)) ;  // 计算通道位置
                channel.setPos(pos * ScopeBase.getToUICoff());  // 设置通道位置（转换为UI坐标）
                nums++;  // 计数加1
            }
        }
    }
}
