package com.micsig.tbook.scope.Auto;  // 定义包名：示波器自动设置功能模块



import com.micsig.base.Logger;  // 导入Logger类：基础日志工具
import com.micsig.tbook.scope.Display.Display;  // 导入Display类：显示管理
import com.micsig.tbook.scope.Sample.MemDepthFactory;  // 导入MemDepthFactory类：存储深度工厂
import com.micsig.tbook.scope.Sample.Sample;  // 导入Sample类：采样管理
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理
import com.micsig.tbook.scope.ScopeBase;  // 导入ScopeBase类：示波器基类常量
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入Trigger类：触发基类
import com.micsig.tbook.scope.Trigger.TriggerCommon;  // 导入TriggerCommon类：触发通用配置
import com.micsig.tbook.scope.Trigger.TriggerEdge;  // 导入TriggerEdge类：边沿触发
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入TriggerFactory类：触发工厂
import com.micsig.tbook.scope.Trigger.TriggerLevel;  // 导入TriggerLevel类：触发电平
import com.micsig.tbook.scope.channel.Channel;  // 导入Channel类：模拟通道
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂
import com.micsig.tbook.scope.fpga.FPGA_Status;  // 导入FPGA_Status类：FPGA状态
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入HorizontalAxis类：水平轴管理

/**
 * 自动设置动作抽象类 - 定义自动设置的核心行为模板
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Auto（示波器自动设置功能模块）</li>
 *   <li>架构层级：业务逻辑层 - 策略模式抽象层</li>
 *   <li>设计模式：模板方法模式 + 策略模式</li>
 *   <li>职责类型：定义自动设置流程、提供默认配置</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义自动设置的抽象行为接口</li>
 *   <li>提供自动设置的默认准备流程</li>
 *   <li>协调通道、触发、采样等模块进行自动设置</li>
 *   <li>为不同触发类型提供可扩展的策略接口</li>
 * </ul>
 * 
 * <p><b>自动设置架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   AutoAtion - 自动设置动作抽象类                                          │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   继承体系（策略模式）                                             │   │
 * │   │                                                                   │   │
 * │   │               AutoAtion（抽象类）                                 │   │
 * │   │                     │                                            │   │
 * │   │       ┌─────────────┴─────────────┐                              │   │
 * │   │       │                           │                              │   │
 * │   │   EdgeAutoAtion           SerialBusAutoAtion                     │   │
 * │   │   （边沿触发自动设置）      （串口总线自动设置）                    │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   抽象方法（子类实现）                                             │   │
 * │   │                                                                   │   │
 * │   │   onModifyVertical(chIdx)  - 垂直档位调整回调                     │   │
 * │   │   onModifyTimebase()       - 时基调整回调                         │   │
 * │   │   onModifyLevel()          - 触发电平调整回调                     │   │
 * │   │   onAtion(fpgaStatus)      - 自动设置执行（返回true表示完成）     │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   自动设置流程（模板方法）                                         │   │
 * │   │                                                                   │   │
 * │   │   autoPrePared() ───→ 重置显示模式                                │   │
 * │   │        │              重置通道参数                                │   │
 * │   │        │              重置触发配置                                │   │
 * │   │        │              重置采样配置                                │   │
 * │   │        │              重置通道位置                                │   │
 * │   │        │                                                          │   │
 * │   │   onAtion() ───→ 执行具体的自动设置逻辑                           │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>自动设置功能说明：</b>
 * <pre>
 *   Auto Set（自动设置）是示波器的智能功能，能够：
 *   1. 自动检测输入信号的幅度和频率
 *   2. 自动调整垂直档位使信号在屏幕上显示合适的大小
 *   3. 自动调整时基使信号在屏幕上显示合适的周期数
 *   4. 自动调整触发电平使波形稳定触发
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户按下Auto按键，触发自动设置功能</li>
 *   <li>用户打开新的信号源，需要快速获得合适的显示</li>
 *   <li>用户对当前设置不满意，希望快速恢复到最佳状态</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>组合：Auto（自动设置配置管理）</li>
 *   <li>依赖：Channel/ChannelFactory（通道管理）</li>
 *   <li>依赖：Trigger/TriggerFactory（触发管理）</li>
 *   <li>依赖：Sample（采样管理）</li>
 *   <li>依赖：Display（显示管理）</li>
 *   <li>依赖：FPGA_Status（FPGA状态反馈）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-7-3
 * @see Auto 自动设置配置管理
 * @see EdgeAutoAtion 边沿触发自动设置
 * @see SerialBusAutoAtion 串口总线自动设置
 */
public abstract class AutoAtion {
    
    /** 自动设置配置引用：持有Auto实例，用于读取自动设置参数 */
    protected Auto auto;  // 自动设置配置管理实例

    /**
     * 构造方法：初始化自动设置动作
     * 
     * <p>保存Auto实例引用，用于后续读取自动设置配置参数。
     * 
     * @param auto 自动设置配置管理实例
     */
    public AutoAtion(Auto auto){
        this.auto = auto;  // 保存Auto实例引用

    }
    
    /**
     * 垂直档位调整回调
     * 
     * <p>当自动设置过程中需要调整指定通道的垂直档位时调用。
     * 子类需要实现具体的调整逻辑。
     * 
     * @param chIdx 通道索引（0-based，对应CH1-CH8）
     */
    public abstract void onModifyVertical(int chIdx);

    /**
     * 时基调整回调
     * 
     * <p>当自动设置过程中需要调整时基时调用。
     * 子类需要实现具体的调整逻辑。
     */
    public abstract void onModifyTimebase();

    /**
     * 触发电平调整回调
     * 
     * <p>当自动设置过程中需要调整触发电平时调用。
     * 子类需要实现具体的调整逻辑。
     */
    public abstract void onModifyLevel() ;

    /**
     * 自动设置执行
     * 
     * <p>执行具体的自动设置逻辑，根据FPGA状态反馈进行调整。
     * 子类需要实现具体的自动设置算法。
     * 
     * @param fpgaStatus FPGA状态对象，包含信号幅度、频率等信息
     * @return true表示自动设置已完成，false表示需要继续调整
     */
    public abstract boolean onAtion(FPGA_Status fpgaStatus);
    
    /**
     * 自动设置准备
     * 
     * <p>在执行自动设置之前，重置示波器到默认状态：
     * <ol>
     *   <li>重置显示模式：线条显示、YT模式、水平参考居中</li>
     *   <li>重置通道参数：带宽全开、非反相、DC耦合、非屏幕中心模式、1M阻抗</li>
     *   <li>重置触发配置：边沿触发、自动触发模式、200ms释抑时间、直流耦合、上升沿</li>
     *   <li>重置采样配置：根据微分运算状态选择平均采样或普通采样</li>
     *   <li>重置存储深度：设置为最小值</li>
     *   <li>重置水平位置：居中显示</li>
     *   <li>重置通道位置：均匀分布在屏幕上</li>
     * </ol>
     */
    public void autoPrePared(){
        Logger.d("autoPrePared");  // 输出日志
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        Display display = Display.getInstance();  // 获取显示管理实例
        display.setDrawType(Display.DRAWTYPE_LINE);  // 设置为线条显示模式
        if(display.isZoom()) {  // 检查是否处于Zoom模式
            display.setZoom(false);  // 退出Zoom模式
        }
        display.setDisplayMode(Display.DISPLAY_YT);  // 设置为YT显示模式
        display.setHorRef(Display.HORREF_CENTER);  // 设置水平参考为居中
        //display.setCCT(false);  // 可选：关闭CCT功能


        Channel channel;  // 通道变量
        for(int i=0;i<scope.getChNum();i++){  // 遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取动态通道实例

            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL,Channel.getMaxBandWidth());  // 设置带宽为全带宽

            channel.setInvert(false);  // 关闭反相

            if(channel.getCoupleType() == Channel.COUPLE_TYPE_GND) {  // 检查是否为GND耦合
                channel.setCoupleType(Channel.COUPLE_TYPE_DC);  // 改为DC耦合
            }
            if(channel.getVerticalMode() == Channel.VERTICAL_MODE_SCREEN_CENTER){  // 检查是否为屏幕中心模式
                channel.setVerticalMode(Channel.VERTICAL_MODE_CH_ZERO);  // 改为通道零点模式
            }
            if(channel.getResistanceType() == Channel.RESISTANCE_50){  // 检查是否为50欧姆阻抗
                channel.setResistanceType(Channel.RESISTANCE_1M);  // 改为1M欧姆阻抗
            }
            if((!auto.isAutoRangeEnable())  // 检查是否禁用自动量程
                    && auto.isAutoChannelEnable()){  // 检查是否启用自动通道
                if(!ChannelFactory.isChOpen(i)){  // 检查通道是否已打开
                    ChannelFactory.chOpen(i);  // 打开通道
                }
            }
        }
        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();  // 获取触发通用配置
        triggerCommon.setTriggerType(Trigger.TRIG_TYPE_EDGE);  // 设置触发类型为边沿触发
        triggerCommon.setTriggerMode(TriggerCommon.TM_AUTO);  // 设置触发模式为自动
        triggerCommon.setTriggerHoldOffTime(200);  // 设置释抑时间为200ms
        TriggerEdge trigger = (TriggerEdge) TriggerFactory.getTriggerObj();  // 获取边沿触发实例

        trigger.setTriggerCouple(TriggerEdge.COUPLING_DIRECT);  // 设置触发耦合为直流
        trigger.setTriggerEdge(TriggerEdge.TET_ASC);  // 设置触发边沿为上升沿
        TriggerLevel triggerLevel = trigger.getTriggerLevel();  // 获取触发电平对象
        if(triggerLevel != null) {  // 检查触发电平对象是否有效
            triggerLevel.setPos(0);  // 设置触发电平位置为0（屏幕中心）
            int chIdx = trigger.getTriggerSource();  // 获取触发源通道索引
            if(!ChannelFactory.isChOpen(chIdx)){  // 检查触发源通道是否已打开
                ChannelFactory.chOpen(chIdx);  // 打开触发源通道
            }
        }

        Sample sample = Sample.getInstance();  // 获取采样管理实例
        boolean []bSmaple = {false};  // 采样类型标志（使用数组实现final局部变量的修改）
        ChannelFactory.forEachMath(mathChannel->{  // 遍历所有数学通道
            if(mathChannel.isOpen() && mathChannel.isDifferential()){  // 检查是否打开且为微分运算
                bSmaple[0] = true;  // 设置标志为true
            }
        });

        if(bSmaple[0]){  // 如果有微分运算
            sample.setSampleType(Sample.SAMPLE_TYPE_AVERAGE);  // 设置为平均采样
            sample.setSampleNum(Sample.SAMPLE_TYPE_AVERAGE,4);  // 设置平均次数为4
        }else{  // 没有微分运算
            sample.setSampleType(Sample.SAMPLE_TYPE_NORMAL);  // 设置为普通采样
        }

        MemDepthFactory.getMemDepth().setMemDepthItem(0);  // 设置存储深度为最小值

        HorizontalAxis.getInstance().setTimePosOfView(0);  // 设置水平位置为居中
        resetChPos();  // 重置通道位置
    }

    /**
     * 重置通道位置
     * 
     * <p>将所有打开的通道均匀分布在屏幕垂直方向上。
     * 计算方式：根据打开的通道数量，计算每个通道的位置偏移，
     * 使通道波形均匀分布在屏幕上，避免重叠。
     * 
     * <p><b>位置计算公式：</b>
     * <pre>
     *   offset = 屏幕高度 / (打开通道数 * 2)
     *   通道位置 = 屏幕高度/2 - offset * (1 + 序号 * 2)
     * </pre>
     */
    public void resetChPos(){
        Channel channel;  // 通道变量
        Scope scope = Scope.getInstance();  // 获取示波器核心实例
        int offset = ScopeBase.getHeight()/(ChannelFactory.getDynamicChannelOpenCount() * 2);  // 计算通道间距
        int nums = 0;  // 已处理的打开通道计数

        for (int i = 0; i < scope.getChNum(); i++) {  // 遍历所有通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取动态通道实例
            if (channel != null && channel.isOpen()) {  // 检查通道是否有效且已打开
                double pos = (ScopeBase.getHeight() / 2 - offset * (1 + nums * 2));  // 计算通道位置
                channel.setPos(pos * ScopeBase.getToUICoff());  // 设置通道位置（转换为UI坐标）
                nums++;  // 增加已处理通道计数
            }
        }
//        TriggerEdge trigger = (TriggerEdge) TriggerFactory.getTriggerObj();  // 获取边沿触发实例
//        int chIdx = trigger.getTriggerSource();  // 获取触发源通道索引
//        if(!ChannelFactory.isChActivate(chIdx)){  // 检查触发源通道是否已激活
//            ChannelFactory.chActivate(chIdx);  // 激活触发源通道
//        }
    }
}
