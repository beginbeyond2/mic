package com.micsig.tbook.scope.Calibrate;  // 定义包名：示波器校准功能模块

import android.util.Log;  // 导入Log类：Android日志输出工具

import com.micsig.tbook.scope.Auto.Auto;  // 导入Auto类：自动设置管理
import com.micsig.tbook.scope.Data.IDataBuffer;  // 导入IDataBuffer接口：数据缓冲区接口
import com.micsig.tbook.scope.Data.SyncHeader;  // 导入SyncHeader类：同步头管理
import com.micsig.tbook.scope.Data.WaveData;  // 导入WaveData类：波形数据
import com.micsig.tbook.scope.Display.Display;  // 导入Display类：显示管理
import com.micsig.tbook.scope.Sample.MemDepthFactory;  // 导入MemDepthFactory类：存储深度工厂
import com.micsig.tbook.scope.Sample.Sample;  // 导入Sample类：采样管理
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理
import com.micsig.tbook.scope.ScopeFrozen;  // 导入ScopeFrozen类：示波器冻结状态
import com.micsig.tbook.scope.ScopeMessage;  // 导入ScopeMessage类：示波器消息管理
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入Trigger类：触发基类
import com.micsig.tbook.scope.Trigger.TriggerCommon;  // 导入TriggerCommon类：触发通用设置
import com.micsig.tbook.scope.Trigger.TriggerEdge;  // 导入TriggerEdge类：边沿触发
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入TriggerFactory类：触发工厂
import com.micsig.tbook.scope.channel.Channel;  // 导入Channel类：通道管理
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂
import com.micsig.tbook.scope.channel.IChannel;  // 导入IChannel接口：通道接口
import com.micsig.tbook.scope.fpga.FPGACommand;  // 导入FPGACommand类：FPGA命令管理
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入HorizontalAxis类：水平轴管理
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入VerticalAxis类：垂直轴管理

import java.util.ArrayList;  // 导入ArrayList类：动态数组
import java.util.List;  // 导入List接口：列表接口

/**
 * 校准抽象基类 - 校准流程模板方法实现
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准功能模块）</li>
 *   <li>架构层级：业务逻辑层 - 校准流程模板</li>
 *   <li>设计模式：模板方法模式 + 抽象工厂模式</li>
 *   <li>职责类型：校准流程控制、波形数据管理、配置初始化</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义校准流程的模板方法（iniCalibrate → onCalibrate → endCalibrate）</li>
 *   <li>管理校准过程中的波形数据缓存</li>
 *   <li>初始化校准环境配置（触发/通道/显示等）</li>
 *   <li>提供子类需要实现的抽象方法</li>
 * </ul>
 * 
 * <p><b>校准流程生命周期：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   校准流程生命周期                                                        │
 * │                                                                          │
 * │   ┌─────────────────┐                                                    │
 * │   │  iniCalibrate   │  初始化阶段                                        │
 * │   │  ├─ setFlagCalibrate(true)                                          │
 * │   │  ├─ setCalibrateType(type)                                          │
 * │   │  ├─ iniCalibrateReg()        [抽象方法-子类实现]                     │
 * │   │  ├─ initCalibrateConfig()    初始化配置                              │
 * │   │  └─ calibratePrepare()       [抽象方法-子类实现]                     │
 * │   └────────┬────────┘                                                    │
 * │            │                                                             │
 * │            ▼                                                             │
 * │   ┌─────────────────┐                                                    │
 * │   │     begin       │  准备阶段                                          │
 * │   │  ├─ 获取波形数据                                                       │
 * │   │  └─ 验证同步头                                                         │
 * │   └────────┬────────┘                                                    │
 * │            │                                                             │
 * │            ▼                                                             │
 * │   ┌─────────────────┐                                                    │
 * │   │  onCalibrate    │  执行阶段 [抽象方法-子类实现]                       │
 * │   │  ├─ 执行校准算法                                                       │
 * │   │  ├─ 计算校准系数                                                       │
 * │   │  └─ 更新校准寄存器                                                     │
 * │   └────────┬────────┘                                                    │
 * │            │                                                             │
 * │            ▼                                                             │
 * │   ┌─────────────────┐                                                    │
 * │   │  endCalibrate   │  结束阶段                                          │
 * │   │  ├─ setFlagCalibrate(false)                                          │
 * │   │  └─ restoreCalibrateConfig()  恢复配置                               │
 * │   └─────────────────┘                                                    │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>校准环境初始化配置：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   initCalibrateConfig() 初始化配置                                       │
 * │                                                                          │
 * │   触发配置：                                                              │
 * │   ├── 触发类型：边沿触发                                                  │
 * │   ├── 触发模式：自动触发                                                  │
 * │   ├── 触发耦合：直流耦合                                                  │
 * │   ├── 触发边沿：上升沿                                                    │
 * │   └── 触发源：通道1                                                       │
 * │                                                                          │
 * │   采样配置：                                                              │
 * │   ├── 采样模式：正常模式                                                  │
 * │   ├── 存储深度：默认深度（14M）                                           │
 * │   └── 存储深度项：140k                                                    │
 * │                                                                          │
 * │   显示配置：                                                              │
 * │   ├── 显示模式：YT模式                                                    │
 * │   ├── 绘制类型：线模式                                                    │
 * │   ├── 时基：2us/div                                                      │
 * │   ├── 余辉时间：200ms                                                     │
 * │   └── Zoom：关闭                                                          │
 * │                                                                          │
 * │   通道配置：                                                              │
 * │   ├── 带宽：全带宽                                                        │
 * │   ├── 耦合：DC耦合                                                        │
 * │   ├── 输入阻抗：1MΩ                                                       │
 * │   ├── 档位：100mV/div                                                     │
 * │   ├── 探头：10X电压探头                                                   │
 * │   └── 位置：归零                                                          │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   Calibrate（抽象基类）
 *         │
 *         ├── MHO38v1_ZeroCalibrateEx      零点校准（MHO38v1）
 *         ├── MHO38v1_ChCofitCalibrateEx   系数校准（MHO38v1）
 *         ├── MHO38v1_ChGainCalibrate      增益校准（MHO38v1）
 *         ├── MHO38v1_ChCapCalibrate       电容校准（MHO38v1）
 *         ├── MHO68v1_ZeroCalibrateEx      零点校准（MHO68v1）
 *         ├── MHO68v1_ADOffsetCalibrate    AD偏移校准（MHO68v1）
 *         ├── MHO68v1_ChGainCalibrate      增益校准（MHO68v1）
 *         └── ...（其他型号校准实现）
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：CabteRegister（校准寄存器管理）</li>
 *   <li>依赖：FPGACommand（FPGA命令管理）</li>
 *   <li>依赖：Channel（通道管理）</li>
 *   <li>依赖：TriggerFactory（触发工厂）</li>
 *   <li>依赖：Display（显示管理）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see CalibrateService 校准服务
 * @see CabteRegister 校准寄存器管理
 */
public abstract class Calibrate {

    /** 校准类型ID：标识当前校准项 */
    private int calibrateType;  // 校准类型标识
    
    /** 校准进行中标志：使用volatile保证多线程可见性 */
    private volatile boolean bCalibrate = false;  // 校准进行中标志
    
    /** 存储深度设置备份：用于校准结束后恢复 */
    protected int memDepthSetBak;  // 存储深度设置备份
    
    /** 通道波形数据缓存：存储各通道的波形数据 */
    private IDataBuffer [] chWaves = new IDataBuffer[ChannelFactory.CH_CNT];  // 波形数据缓存数组
    
    /** 校准完成标志 */
    private boolean finished=false;  // 校准完成标志
    
    /** 是否为工厂校准模式：true=工厂校准，false=自校准 */
    protected boolean bFactorCalibrate = true;  // 工厂校准模式标志
    
    /** 通道数量 */
    protected final int channelNums;  // 通道数量
    
    /** 校准寄存器管理器引用 */
    protected CabteRegister cabteRegister;  // 校准寄存器管理器
    
    /** 通道对象数组：存储各通道的引用 */
    protected Channel [] channel=new Channel[ChannelFactory.CH_CNT];  // 通道对象数组
    
    /** 日志标签前缀 */
    protected final String TAG_PRI="calibrate";  // 日志标签前缀
    
    /** 校准结果字符串列表：存储校准过程中的输出信息 */
    protected List<String> resultString=new ArrayList();  // 校准结果字符串列表

    /**
     * 构造方法：初始化校准基类
     * 
     * <p>初始化通道引用和波形缓存。
     * 
     * @param calibrateType 校准类型ID
     */
    public Calibrate(int calibrateType){
        this.calibrateType = calibrateType;  // 保存校准类型

        ChannelFactory.forEachCh((ch)->{  // 遍历所有通道
            int idx = ch.getChId();  // 获取通道ID
            chWaves[idx] = null;  // 初始化波形缓存为空
            channel[idx] = ch;  // 保存通道引用
        });

        channelNums = Scope.getInstance().getChNum();  // 获取通道数量
        cabteRegister = CabteRegister.getInstance();  // 获取校准寄存器管理器单例
    }

    /**
     * 毫秒级延时
     * 
     * @param ms 延时时间（毫秒）
     */
    protected void ms_sleep(long ms){
        try{
            Thread.sleep(ms);  // 线程休眠
        }catch (Exception e){
            e.printStackTrace();  // 打印异常堆栈
        }
    }

    /**
     * 开始校准准备
     * 
     * <p>获取各通道的波形数据，验证同步头。
     * 波形数据用于后续的校准计算。
     * 
     * @return true表示准备成功，false表示准备失败
     */
    public boolean begin(){
        synchronized (this) {  // 同步锁
            int maxIdx = ChannelFactory.getMaxChIdx();  // 获取最大通道索引
            WaveData waveData;  // 波形数据临时变量
            int syncHeaer = SyncHeader.getSyncHeader();  // 获取当前同步头
            for (int i = ChannelFactory.CH1; i < maxIdx; i++) {  // 遍历所有通道
                waveData = (WaveData)channel[i].obtain();  // 从通道获取波形数据
                if(waveData != null){  // 检查数据是否有效
                    if(waveData.getSyncHeader() == syncHeaer) {  // 验证同步头
                        if(chWaves[i] != null){  // 检查是否有旧数据
                            channel[i].recycle(chWaves[i]);  // 回收旧数据
                        }
                        chWaves[i] = waveData;  // 保存新数据
                    }else{  // 同步头不匹配
                        channel[i].recycle(waveData);  // 回收数据
                        return false;  // 返回失败
                    }
                }
            }
            bCalibrate = true;  // 设置校准进行中标志
        }
        return true;  // 返回成功
    }

    /**
     * 结束校准
     * 
     * <p>释放波形数据缓存，清除校准标志。
     */
    public void end(){
        synchronized (this) {  // 同步锁
            int maxIdx = ChannelFactory.getMaxChIdx();  // 获取最大通道索引
            for (int i = ChannelFactory.CH1; i < maxIdx; i++) {  // 遍历所有通道
                if (chWaves[i] != null) {  // 检查是否有数据
                    channel[i].recycle(chWaves[i]);  // 回收数据
                    chWaves[i] = null;  // 清空引用
                }
            }
            bCalibrate = false;  // 清除校准进行中标志
        }
    }

    /**
     * 检查是否正在校准
     * 
     * @return true表示正在校准，false表示未在校准
     */
    public boolean isCalibrate(){

        return bCalibrate;  // 返回校准进行中标志
    }

    /**
     * 获取指定通道的波形数据
     * 
     * @param chIdx 通道索引
     * @return 波形数据缓冲区
     */
    protected IDataBuffer getWave(int chIdx){

        return chWaves[chIdx];  // 返回指定通道的波形数据
    }

    /**
     * 设置校准完成标志
     */
    protected void setFinished() {
        finished = true;  // 设置完成标志
    }


    /** 最大带宽备份：用于校准结束后恢复 */
    private double maxBandWidth = Channel.MAX_BANDWIDTH;  // 最大带宽备份

    /**
     * 初始化校准环境
     * 
     * <p>校准开始前执行，设置FPGA校准标志、初始化寄存器、配置环境。
     * 这是模板方法模式中的模板方法。
     */
    public void iniCalibrate(){
        FPGACommand.getInstance().setFlagCalibrate(true);  // 设置FPGA校准标志
        FPGACommand.getInstance().setCalibrateType(calibrateType);  // 设置校准类型
        SyncHeader.setCalibrate(true);  // 设置同步头校准模式
        maxBandWidth = Channel.getMaxBandWidth();  // 备份当前带宽限制
        Channel.setMaxBandWidth(Channel.MAX_BANDWIDTH);  // 设置为全带宽
        // 初始化校准寄存器
        iniCalibrateReg();  // 调用抽象方法，由子类实现
        // 进入运行状态
        Scope.getInstance().setRun(true);  // 设置示波器为运行状态
        // 初始化配置
        initCalibrateConfig();  // 初始化校准环境配置
        resultString.clear();  // 清空结果字符串列表

        // 校准前准备工作
        calibratePrepare();  // 调用抽象方法，由子类实现
        finished = false;  // 清除完成标志
    }

    /**
     * 结束校准
     * 
     * <p>校准结束后执行，清除FPGA校准标志、恢复配置。
     */
    public void endCalibrate() {
        finished = true;  // 设置完成标志
        Channel.setMaxBandWidth(maxBandWidth);  // 恢复带宽限制
        FPGACommand.getInstance().setFlagCalibrate(false);  // 清除FPGA校准标志
        FPGACommand.getInstance().setCalibrateType(-1);  // 清除校准类型
        SyncHeader.setCalibrate(false);  // 清除同步头校准模式
        restoreCalibrateConfig();  // 恢复校准配置
    }

    /**
     * 恢复校准配置
     * 
     * <p>校准结束后恢复之前的设置。
     */
    protected void restoreCalibrateConfig(){
        // 恢复存储深度设置
        MemDepthFactory.forceMemDepth(memDepthSetBak);  // 恢复存储深度强制类型
        // 存储深度大小设置为自动
        MemDepthFactory.getMemDepth().setMemDepthItem(0);  // 设置为自动

    }


    /** 初始输入阻抗类型：默认为1MΩ */
    private volatile int initResistanceType = Channel.RESISTANCE_1M;  // 初始阻抗类型

    /**
     * 设置初始阻抗类型
     * 
     * @param resistanceType 阻抗类型
     */
    protected synchronized void setInitResistanceType(int resistanceType){
        this.initResistanceType = resistanceType;  // 设置阻抗类型
    }

    /**
     * 获取初始阻抗类型
     * 
     * @return 阻抗类型
     */
    private synchronized int getInitResistanceType(){
        return initResistanceType;  // 返回阻抗类型
    }

    /**
     * 初始化校准配置
     * 
     * <p>设置校准环境的标准配置，包括触发、采样、显示、通道等。
     * 这是模板方法，子类可以覆盖以自定义配置。
     */
    public void initCalibrateConfig(){
        TriggerFactory triggerFactory=TriggerFactory.getInstance();  // 获取触发工厂单例
        // 修改为边沿触发
        if(TriggerFactory.getTriggerType() != Trigger.TRIG_TYPE_EDGE){  // 检查当前触发类型
            triggerFactory.setTriggerType(Trigger.TRIG_TYPE_EDGE);  // 设置为边沿触发
        }
        // 采用自动触发
        triggerFactory.getTriggerCommon().setTriggerMode(TriggerCommon.TM_AUTO);  // 设置自动触发模式
        // 触发抑制时间调小
        triggerFactory.getTriggerCommon().setTriggerHoldOffTime(200/4);  // 设置触发抑制时间
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));  // 获取边沿触发对象
        // 触发耦合：直流
        triggerEdge.setTriggerCouple(TriggerEdge.COUPLING_DIRECT);  // 设置直流耦合
        // 调整每个通道的触发位置到0
        for (int i=0; i<channelNums; i++) {  // 遍历所有通道
            triggerEdge.getTriggerLevel(i).setPos(0);  // 设置触发电平位置为0
        }
        // 上升沿触发
        triggerEdge.setTriggerEdge(TriggerEdge.TET_ASC);  // 设置上升沿触发
        // 触发源设置为通道1
        triggerEdge.setTriggerSource(defaultTriggerSource);  // 设置触发源
        // 退出自动
        Auto.getInstance().setAuto(false);  // 退出自动设置模式
        // 设置采样模式为正常模式
        Sample.getInstance().setSampleType(Sample.SAMPLE_TYPE_NORMAL);  // 设置正常采样模式
        // 设置存储深度强制类型为14M
        memDepthSetBak = MemDepthFactory.getMemDepthSet();  // 备份当前存储深度设置
        MemDepthFactory.forceMemDepth(MemDepthFactory.getDefaultMemDepth());  // 强制设置默认存储深度
        // 存储深度大小设置为140k
        MemDepthFactory.getMemDepth().setMemDepthItem(5);  // 设置存储深度项
        // 波形显示为线模式
        Display.getInstance().setDrawType(Display.DRAWTYPE_LINE);  // 设置线显示模式
        // YT模式
        Display.getInstance().setDisplayMode(Display.DISPLAY_YT);  // 设置YT显示模式
        // 退出Zoom
        Display.getInstance().setZoom(false);  // 关闭Zoom功能
        // 时基
        HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.TSI_2uS);  // 设置时基为2us/div
        // 触发时刻
        HorizontalAxis.getInstance().setTimePosOfView(0);  // 设置触发位置
        // 余辉时间500ms
        Display.getInstance().setPersistAdjustTime(200);  // 设置余辉时间
        // 余辉模式：自动
        Display.getInstance().setPersistType(Display.PERSIST_TYPE_AUTO);  // 设置自动余辉模式
        // 以屏幕中心进行x轴缩放
        Display.getInstance().setHorRef(Display.HORREF_CENTER);  // 设置水平参考为中心
        // 通道设置
        for(int i=0; i<channelNums; i++) {  // 遍历所有通道
            // 全带宽
            channel[i].setBandWidthType(Channel.BANDWIDTH_TYPE_FULL,Channel.MAX_BANDWIDTH);  // 设置全带宽

            channel[i].setInvert(false);  // 关闭反相
            channel[i].setCoupleType(Channel.COUPLE_TYPE_DC);  // DC耦合
            // 如果有输入阻抗控制，则设置为1M输入阻抗
            channel[i].setVerticalMode(Channel.VERTICAL_MODE_CH_ZERO);  // 设置垂直模式

            channel[i].setResistanceType(getInitResistanceType());  // 设置输入阻抗

            // 100mV档位
            channel[i].setVScaleId(VerticalAxis.DANG_100mV);  // 设置100mV档位

            channel[i].setFineScale(1.0);  // 精细电压档位为1.0
            channel[i].setProbeType(VerticalAxis.PROBE_TYPE_VOL);  // 类型为电压
            channel[i].setProbeRate(defaultProbeRate);  // 10X探头
            channel[i].setPos(0);  // 通道纵向位置归零
            channel[i].setDelay(0);  // 延迟归零
            channel[i].setChOffsetVal(0);  // 通道偏移归零
            for(int j=VerticalAxis.DANG_MIN;j<=VerticalAxis.DANG_MAX;j++){  // 遍历所有档位
                channel[i].setZero(j,0);  // 零点归零
            }
        }
        // 打开所有通道
        for(int i=0; i<channelNums; i++) {  // 遍历所有通道
            ChannelFactory.chOpen(i);  // 打开通道
        }
        // 关闭数学通道
        ChannelFactory.forEachMath(mathChannel -> {  // 遍历数学通道
            ChannelFactory.chClose(mathChannel.getChId());  // 关闭数学通道
        });
        // 关闭串口通道
        ChannelFactory.forEachSerial(serialChannel -> {  // 遍历串口通道
            ChannelFactory.chClose(serialChannel.getChId());  // 关闭串口通道
        });
    }

    /** 默认探头比率：10X */
    protected double defaultProbeRate = 10;  // 默认探头比率
    
    /** 默认触发源：通道1 */
    protected int defaultTriggerSource = 0;  // 默认触发源

    /** 延时计数器 */
    private int delay_cnt=0;  // 延时计数器
    
    /** 延时设置值 */
    private int delay_set=0;  // 延时设置值

    /**
     * 设置延时值
     * 
     * @param vol 延时值
     */
    public void delaySet(int vol) {
        delay_set = vol;  // 设置延时值
        delay_cnt = 0;  // 重置计数器
    }

    /**
     * 检查是否完成动作
     * 
     * <p>延时检查，用于等待校准操作完成。
     * 
     * @return true表示动作完成，false表示未完成
     */
    public boolean isFinishedAction() {
        //ScopeMessage.waitMsgFinished();  // 等待消息完成（已注释）
        //return true;  // 直接返回true（已注释）

        if(!finished && (++delay_cnt > delay_set)) {  // 检查延时是否到达
            delay_cnt = 0;  // 重置计数器
            return ScopeMessage.isEmpty();  // 检查消息队列是否为空
        }
        return false;  // 返回未完成
    }

    /**
     * 获取校准结果字符串列表
     * 
     * @return 结果字符串列表
     */
    public List getResultString(){
        return resultString;  // 返回结果字符串列表
    }

    /**
     * 获取日志标签
     * 
     * @return 日志标签
     */
    public String getTAG() {
        return TAG_PRI;  // 返回日志标签
    }

    /**
     * 设置校准参数
     * 
     * <p>用于传递校准所需的参数，如档位值等。
     * 子类可覆盖此方法。
     * 
     * @param vol 参数对象
     */
    public void setParam(Object vol) {

    }

    /**
     * 获取校准参数
     * 
     * @return 参数对象
     */
    public Object getParam() {
        return 0;  // 默认返回0
    }

    // ==================== 抽象方法：由子类实现 ====================

    /**
     * 初始化校准寄存器（抽象方法）
     * 
     * <p>由子类实现，初始化校准相关的寄存器。
     */
    public abstract void iniCalibrateReg();  // 初始化校准寄存器
    
    /**
     * 校准前准备工作（抽象方法）
     * 
     * <p>由子类实现，执行校准前的准备工作。
     */
    public abstract void calibratePrepare();  // 校准之前状态初始化
    
    /**
     * 设置错误码（抽象方法）
     * 
     * @param errcode 错误码
     */
    public abstract void setErrcode(int errcode);
    
    /**
     * 获取错误码（抽象方法）
     * 
     * @return 错误码，0表示无错误
     */
    public abstract int getErrcode();  // 判断校准结果错误代码，0为无错误
    
    /**
     * 执行校准（抽象方法）
     * 
     * <p>由子类实现，执行具体的校准算法。
     * 
     * @return true表示校准成功，false表示校准失败
     */
    public abstract boolean onCalibrate();

    /**
     * 检查是否为工厂校准模式
     * 
     * @return true表示工厂校准，false表示自校准
     */
    public boolean isFactorCalibrate(){
        return this.bFactorCalibrate;  // 返回工厂校准模式标志
    }

    /**
     * 设置工厂校准模式
     * 
     * @param bFactorCalibrate true表示工厂校准，false表示自校准
     */
    public void setFactorCalibrate(boolean bFactorCalibrate){
        this.bFactorCalibrate = bFactorCalibrate;  // 设置工厂校准模式标志

    }

    /**
     * 检查参数
     * 
     * <p>子类可覆盖此方法，用于校准过程中的参数检查。
     * 
     * @return true表示参数正常，false表示参数异常
     */
    public boolean checkParam(){
        return false;  // 默认返回false
    }


    /**
     * FPGA同步
     * 
     * <p>更新同步头并同步FPGA寄存器。
     */
    public synchronized void fpgaSync(){
        int syncheader = -1;  // 初始化同步头
        ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();  // 获取冻结状态单例
        Scope scope = Scope.getInstance();  // 获取示波器单例
        FPGACommand cmd = FPGACommand.getInstance();  // 获取FPGA命令单例
        if(scope.isRun(true)) {  // 检查是否在运行状态
            syncheader = SyncHeader.upSyncHeader();  // 更新同步头
            scopeFrozen.setSyncHeader(syncheader);  // 设置冻结状态同步头
            cmd.updataReg(1);  // 更新寄存器（同步）
            cmd.updataReg(0);  // 更新寄存器（正常）
        }
    }

    /**
     * 更新同步头
     * 
     * <p>仅更新同步头，不同步FPGA。
     */
    public synchronized void updateSync(){
        SyncHeader.upSyncHeader();  // 更新同步头
    }
}
