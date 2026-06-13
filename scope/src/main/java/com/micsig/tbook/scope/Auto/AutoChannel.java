package com.micsig.tbook.scope.Auto;  // 定义包名：示波器自动设置功能模块

import android.util.Log;  // 导入Log类：Android日志输出工具

import com.micsig.base.Logger;  // 导入Logger类：项目基础日志工具
import com.micsig.tbook.scope.Bus.I2CBus;  // 导入I2CBus类：I2C总线配置
import com.micsig.tbook.scope.Bus.IBus;  // 导入IBus类：总线接口定义
import com.micsig.tbook.scope.Bus.SpiBus;  // 导入SpiBus类：SPI总线配置
import com.micsig.tbook.scope.ScopeBase;  // 导入ScopeBase类：示波器基础配置常量
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入Trigger类：触发管理
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入TriggerFactory类：触发工厂
import com.micsig.tbook.scope.Trigger.TriggerLevel;  // 导入TriggerLevel类：触发电平管理
import com.micsig.tbook.scope.channel.Channel;  // 导入Channel类：通道数据模型
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂
import com.micsig.tbook.scope.channel.SerialChannel;  // 导入SerialChannel类：串行通道（总线解码）
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入HorizontalAxis类：水平轴管理
import com.micsig.tbook.scope.measure.Measure;  // 导入Measure类：测量功能

/**
 * 自动设置通道处理器 - 单通道自动调整核心实现
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Auto（示波器自动设置功能模块）</li>
 *   <li>架构层级：业务逻辑层 - 自动设置执行层</li>
 *   <li>设计模式：策略模式 + 模板方法</li>
 *   <li>职责类型：单通道自动调整、垂直/水平/触发电平自动计算</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>根据信号特征自动调整垂直档位（电压/格）</li>
 *   <li>根据信号周期自动调整水平时基（时间/格）</li>
 *   <li>根据信号幅度自动调整触发电平位置</li>
 *   <li>处理串行总线通道的特殊自动调整逻辑</li>
 *   <li>检测信号饱和并调整档位</li>
 * </ul>
 * 
 * <p><b>自动调整算法说明：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   AutoChannel ─ 自动调整流程                                             │
 * │                                                                          │
 * │   输入数据：                                                              │
 * │   ├── min/max ─ 信号最小/最大值（AD采样值）                               │
 * │   └── cycle  ─ 信号周期（纳秒）                                           │
 * │                                                                          │
 * │   调整策略：                                                              │
 * │   ├── 垂直档位调整（TiaoZenY）：                                          │
 * │   │   ├── 检测信号是否饱和（超出BAOHE_MAX_VAL）                            │
 * │   │   ├── 计算信号在屏幕上的显示范围                                       │
 * │   │   └── 选择使信号占屏幕70%-80%的档位                                    │
 * │   │                                                                      │
 * │   ├── 水平时基调整（TiaoZenX）：                                          │
 * │   │   ├── 检测是否为直流信号（周期>50ms或波动<20）                          │
 * │   │   ├── 计算使屏幕显示约3个周期的时基档位                                │
 * │   │   └── 添加回差机制避免频繁调整                                         │
 * │   │                                                                      │
 * │   └── 触发电平调整（Auto_TrigReset）：                                     │
 * │       ├── 检测信号是否饱和                                                │
 * │       ├── 计算信号中心位置                                                │
 * │       └── 设置触发电平为信号中心±5%容差                                    │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>串行通道特殊处理：</b>
 * <ul>
 *   <li>支持I2C、SPI、ARINC429等串行总线</li>
 *   <li>根据波特率自动调整时基</li>
 *   <li>设置总线触发电平（主/次电平）</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：Auto（自动设置配置管理）</li>
 *   <li>依赖：Channel（通道数据模型）</li>
 *   <li>依赖：HorizontalAxis（水平轴管理）</li>
 *   <li>依赖：Trigger/TriggerLevel（触发管理）</li>
 *   <li>依赖：SerialChannel/IBus（串行通道和总线）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-7-13
 * @see Auto 自动设置配置
 * @see Channel 通道数据模型
 */
public class AutoChannel {
    
    // ==================== 成员变量 ====================
    
    /** 当前通道引用：存储要自动调整的通道对象 */
    private Channel channel;  // 关联的通道对象
    
    /** 自动设置配置引用：获取自动设置参数 */
    private Auto auto;  // Auto单例引用

    /** 上次信号周期值：用于周期稳定性检测，单位纳秒 */
    private long lastSCyles = 0;  // 初始为0，首次使用时初始化

    /** 最大值（电压）：信号最大幅度，单位V */
    private double maxVal = 0;  // 信号正向最大值
    
    /** 峰峰值：信号峰峰值，单位V */
    private double peakVal = 0;  // 信号峰峰值（max-min）
    
    /** 垂直档位修改标志：true表示正在修改档位，延迟进入计算 */
    private volatile boolean bModifyVerticalGear = false;  // 档位调整延时进入计算
    
    /** 时基修改标志：true表示正在修改时基 */
    private volatile boolean bModifyTimebase = false;  // 时基调整标志
    
    /** 触发电平修改标志：true表示正在修改触发电平 */
    private volatile boolean bModifyLevel = false;  // 触发电平调整标志

    /** 饱和检测阈值：AD值超过此值认为信号饱和 */
    private static int BAOHE_MAX_VAL = 127;  // 默认值，构造函数中会更新为32767

    // ==================== 构造方法 ====================
    
    /**
     * 构造方法：初始化自动设置通道处理器
     * 
     * <p>关联Auto配置对象和要处理的通道对象，
     * 并设置饱和检测阈值。
     * 
     * @param auto 自动设置配置对象
     * @param channel 要自动调整的通道对象
     */
    public AutoChannel(Auto auto ,Channel channel){
        this.auto = auto;  // 保存Auto配置引用
        this.channel = channel;  // 保存通道引用

        BAOHE_MAX_VAL = 32767;  // 设置饱和检测阈值为15位AD最大值

    }
    
    // ==================== 值获取方法 ====================
    
    /**
     * 获取信号最大值
     * 
     * @return 信号最大幅度，单位V
     */
    public double getMaxVal(){
        return maxVal;  // 返回信号最大值
    }
    
    /**
     * 获取信号峰峰值
     * 
     * @return 信号峰峰值（max-min），单位V
     */
    public double getPeakVal(){
        return peakVal;  // 返回信号峰峰值
    }

    // ==================== 内部计算方法 ====================
    
    /**
     * 计算信号的最大值和峰峰值
     * 
     * <p>根据AD采样值的最大/最小值，结合通道的垂直分辨率，
     * 计算信号的实际电压值。
     * 
     * @param max AD采样最大值
     * @param min AD采样最小值
     */
    private void calcMaxMinVal(int max,int min){
        peakVal = Math.abs(max-min) * channel.getVerticalPerPix();  // 计算峰峰值 = (max-min) * 每像素电压
        if(Math.abs(max) < Math.abs(min)){  // 如果最小值绝对值更大
            max = min;  // 使用最小值作为最大值（取绝对值最大的）
        }
        maxVal = (double)(Math.abs(max) * channel.getVerticalPerPix());  // 计算最大值 = |max| * 每像素电压
        Logger.d("auto","chId:" + channel.getChId() + ",peakVal:" + peakVal + ",maxVal:" + maxVal);  // 输出日志
    }

    // ==================== 修改标志设置方法 ====================
    
    /**
     * 设置垂直档位修改标志
     * 
     * <p>标记正在修改垂直档位，延迟自动调整计算。
     */
    public synchronized void modifyVertical() {
        this.bModifyVerticalGear = true;  // 设置垂直档位修改标志
    }

    /**
     * 设置时基修改标志
     * 
     * <p>标记正在修改时基，延迟自动调整计算。
     */
    public synchronized void modifyTimebase() {
        this.bModifyTimebase = true;  // 设置时基修改标志
    }

    /**
     * 设置触发电平修改标志
     * 
     * <p>标记正在修改触发电平，延迟自动调整计算。
     */
    public synchronized void modifyLevel() {
        this.bModifyLevel = true;  // 设置触发电平修改标志
    }

    // ==================== 核心自动调整方法 ====================
    
    /**
     * 执行自动调整工作（非串行通道）
     * 
     * <p>根据信号特征执行垂直档位、水平时基、触发电平的自动调整。
     * 
     * @param min AD采样最小值
     * @param max AD采样最大值
     * @param cycle 信号周期（纳秒）
     * @return true表示进行了调整，false表示未调整
     */
    public boolean autoWork(int min,int max,long cycle){
        return autoWork(min,max,cycle,false);  // 调用完整版本，bSerialCh=false
    }
    
    /**
     * 执行自动调整工作（完整版本）
     * 
     * <p>根据信号特征和通道类型执行自动调整。
     * 支持普通通道和串行通道的不同处理逻辑。
     * 
     * @param min AD采样最小值
     * @param max AD采样最大值
     * @param cycle 信号周期（纳秒）
     * @param bSerialCh true表示串行通道，false表示普通通道
     * @return true表示进行了调整，false表示未调整
     */
    public boolean autoWork(int min, int max, long cycle,boolean bSerialCh){
        Log.d("auto", "ch = " + channel.getChId() + ",min=" + min + ", max=" + max + ",cycle=" + cycle + ",bSerialCh=" + bSerialCh  + "," +BAOHE_MAX_VAL);  // 输出调试日志
        int baohe = 0;  // 饱和标志：0=未饱和，1=饱和
        if(Math.abs(max) >= BAOHE_MAX_VAL  // 检查最大值是否超过饱和阈值
                || Math.abs(min) >= BAOHE_MAX_VAL){  // 检查最小值是否超过饱和阈值
            baohe = 1;  // 标记为饱和
        }

        int pos = (int) Math.round(channel.getPos());  // 获取通道垂直位置偏移
        int bc = channel.getPlaceVal();  // 获取通道位置校准值
        int m = channel.getM();  // 获取通道M参数（校准系数）
        int n = channel.getN();  // 获取通道N参数（校准系数）
        max = (int)(max/64);  // AD值缩放：除以64（可能是AD分辨率转换）
        min = (int)(min/64);  // AD值缩放：除以64
//            Logger.d("auto","ch = " + channel.getChId() + ",pos:" + pos + ",bc:" + bc + ",m:" + m + ",n:" +n);
//            max = (int) ((max/16 + m / 256.0) * n / 256.0/4 + bc + pos);
//            min = (int) ((min/16 + m / 256.0) * n / 256.0/4 + bc + pos);
//            Logger.d("auto","ch = " + channel.getChId() + ",min = " + min + " ,max = " + max + ",cycle = " + cycle);
        calcMaxMinVal(max - pos, min - pos);  // 计算并更新信号的最大值和峰峰值（去除位置偏移）

        if(bSerialCh){  // 如果是串行通道
            return serialWork(channel,baohe,max,min,cycle);  // 执行串行通道的自动调整
        }else{  // 普通通道
            if(cycle != 0) {  // 周期不为0表示有有效信号
                return work(channel, baohe, max,min, cycle);  // 执行普通通道的自动调整
            }
        }
        return false;  // 未执行调整
    }

    // ==================== 自动调整条件判断方法 ====================
    
    /**
     * 判断是否为单次自动模式
     * 
     * <p>单次自动模式指不启用自动量程模式，
     * 此时只执行一次自动调整。
     * 
     * @return true表示单次自动模式，false表示连续自动模式
     */
    private boolean isSingleAuto(){
        return !auto.isAutoRangeEnable();  // 自动量程禁用时为单次自动模式
    }

    /**
     * 判断是否允许垂直自动调整
     * 
     * <p>需要满足三个条件：
     * 1. 启用自动量程
     * 2. 启用垂直自动调整
     * 3. 当前未在手动修改垂直档位
     * 
     * @return true表示允许垂直自动调整，false表示不允许
     */
    public synchronized boolean isVerticalAuto(){
        return (auto.isAutoRangeEnable()  // 自动量程已启用
                && auto.isAutoVerticalEnable()  // 垂直自动调整已启用
                && (!bModifyVerticalGear));  // 未在手动修改档位
    }

    /**
     * 判断是否允许水平自动调整
     * 
     * <p>需要满足三个条件：
     * 1. 启用自动量程
     * 2. 启用水平自动调整
     * 3. 当前未在手动修改时基
     * 
     * @return true表示允许水平自动调整，false表示不允许
     */
    public synchronized boolean isHorizontalAuto(){
        return (auto.isAutoRangeEnable()  // 自动量程已启用
                && auto.isAutoHorizontalEnable()  // 水平自动调整已启用
                && (!bModifyTimebase));  // 未在手动修改时基

    }

    /**
     * 判断是否允许触发电平自动调整
     * 
     * <p>需要满足三个条件：
     * 1. 启用自动量程
     * 2. 启用触发电平自动调整
     * 3. 当前未在手动修改触发电平
     * 
     * @return true表示允许触发电平自动调整，false表示不允许
     */
    public synchronized boolean isTriggerLevelAuto(){
        return (auto.isAutoRangeEnable()  // 自动量程已启用
                && auto.isAutoLevelEnable()  // 触发电平自动调整已启用
                && (!bModifyLevel));  // 未在手动修改触发电平
    }


    // ==================== 普通通道自动调整 ====================
    
    /**
     * 执行普通通道的自动调整
     * 
     * <p>根据信号特征调整垂直档位、触发电平和水平时基。
     * 
     * @param channel 要调整的通道
     * @param baoHe 饱和标志（0=未饱和，1=饱和）
     * @param max AD采样最大值（已缩放）
     * @param min AD采样最小值（已缩放）
     * @param cycle 信号周期（纳秒）
     * @return true表示进行了垂直或水平调整，false表示未调整
     */
    boolean work(Channel channel,int baoHe, int max, int min, long cycle)
    {

        boolean bVertival = false;  // 垂直调整标志
        boolean bHorizontal = false;  // 水平调整标志
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();  // 获取水平轴管理单例
        int ix = channel.getVScaleId();  // 获取当前垂直档位ID
        int xd = horizontalAxis.getTimeScaleIdOfView();  // 获取当前时基档位ID
        if(isSingleAuto() || isVerticalAuto()) {  // 检查是否允许垂直自动调整
            if (baoHe == 1) {  // 如果信号饱和
                int ixx = ix + 3;  // 尝试增大3档
                ixx = channel.calcVScaleId(ix,3);  // 计算新的档位ID（考虑档位限制）
                if (ixx > channel.getMaxGear()) {  // 不超过最大档位
                    ixx = channel.getMaxGear();  // 限制到最大档位
                }
                if (ixx != ix)  // 如果档位有变化
                    channel.setVScaleId(ixx);  // 设置新档位
            } else {  // 信号未饱和，正常调整
                int dang = TiaoZenY(channel, max, min);  // 调用垂直档位调整算法

                if (ix != dang) {  // 如果计算出的档位与当前不同
                    channel.setVScaleId(dang);  // 设置新档位
                    bVertival = true;  // 标记进行了垂直调整
                }
            }
        }

        Trigger trigger = TriggerFactory.getTriggerObj();  // 获取触发对象
        if (trigger.getTriggerSource() == channel.getChId()) {  // 如果当前通道是触发源
            TriggerLevel triggerLevel = trigger.getTriggerLevel();  // 获取触发电平对象
            double trigPlace = Auto_TrigReset(channel, max, min);  // 计算新的触发电平位置
            double offset = trigPlace - triggerLevel.getPos();  // 计算触发电平偏移量
            if(isSingleAuto() || isTriggerLevelAuto()) {  // 检查是否允许触发电平自动调整
                if (offset != 0) {  // 如果触发电平需要调整
                    triggerLevel.setPos(trigPlace);  // 设置新的触发电平位置
                }
            }
            if(isSingleAuto() || isHorizontalAuto()) {  // 检查是否允许水平自动调整
                int xDang = TiaoZenX(cycle, max - min);  // 调用水平时基调整算法
                if (xDang < HorizontalAxis.getMinGear()) {  // 不小于最小档位
                    xDang = HorizontalAxis.getMinGear();  // 限制到最小档位
                }else if (xDang > HorizontalAxis.getMaxGear()) {  // 不超过最大档位
                    xDang = HorizontalAxis.getMaxGear();  // 限制到最大档位
                }
                if (xDang != xd) {  // 如果计算出的档位与当前不同
                    HorizontalAxis.getInstance().setTimeScaleIdOfView(xDang);  // 设置新时基档位
                    bHorizontal = true;  // 标记进行了水平调整
                }
            }
        }
        return (bVertival || bHorizontal) ;  // 返回是否进行了调整

    }
    
    // ==================== 串行通道自动调整 ====================
    
    /**
     * 执行串行通道的自动调整
     * 
     * <p>串行通道（I2C、SPI、ARINC429等）的特殊自动调整逻辑。
     * 只调整垂直档位和总线触发电平，时基根据波特率自动计算。
     * 
     * @param channel 要调整的通道
     * @param baoHe 饱和标志（0=未饱和，1=饱和）
     * @param max AD采样最大值（已缩放）
     * @param min AD采样最小值（已缩放）
     * @param cycle 信号周期（纳秒）
     * @return true表示进行了垂直调整，false表示未调整
     */
    boolean serialWork(Channel channel,int baoHe, int max, int min, long cycle)
    {

        boolean bVertival = false;  // 垂直调整标志
        int ix = channel.getVScaleId();  // 获取当前垂直档位ID
        if(isSingleAuto() || isVerticalAuto()) {  // 检查是否允许垂直自动调整
            if (baoHe == 1) {  // 如果信号饱和
                int ixx = ix + 3;  // 尝试增大3档
                ixx = channel.calcVScaleId(ix,3);  // 计算新的档位ID
                if (ixx > channel.getMaxGear()) {  // 不超过最大档位
                    ixx = channel.getMaxGear();  // 限制到最大档位
                }
                if (ixx != ix)  // 如果档位有变化
                    channel.setVScaleId(ixx);  // 设置新档位
            } else {  // 信号未饱和，正常调整
                int dang = TiaoZenY(channel, max, min);  // 调用垂直档位调整算法

                if (ix != dang) {  // 如果计算出的档位与当前不同
                    channel.setVScaleId(dang);  // 设置新档位
                    bVertival = true;  // 标记进行了垂直调整
                }
            }
        }
        Auto_ThreoldLevelReset(channel,max,min);  // 重置总线触发电平
        TiaoZenXBySerialCodeBaundRate();  // 根据波特率调整时基
        return (bVertival) ;  // 返回是否进行了垂直调整

    }

    /**
     * 重置串行通道的总线触发电平
     * 
     * <p>根据信号幅度计算并设置总线的主/次触发电平。
     * 对于ARINC429总线，设置高/低电平；对于其他总线，设置中间电平。
     * 
     * @param channel 通道对象
     * @param max AD采样最大值
     * @param min AD采样最小值
     */
    void Auto_ThreoldLevelReset(Channel channel ,int max, int min){

        int newValue = max;  // 初始化为最大值
        if (-min > max)  // 如果最小值的绝对值更大
            newValue = -min;  // 使用最小值的绝对值
        if(510 <= newValue)  // 检查是否饱和（510约为满量程的一半）
            return;  // 信号饱和，不再进行触发位置调整
        int pos = (int) Math.round(channel.getPos());  // 获取通道位置偏移
        int halfPos = (max + min)/2 - pos;  // 计算信号中间位置
        int highPos = min + Math.abs(max - min)*3/4 - pos;  // 计算高电平位置（75%处）
        int lowPos = min + Math.abs(max - min)/4 - pos;  // 计算低电平位置（25%处）

        //if(channel.)

        SerialChannel serialChannel;  // 串行通道引用
        int maxIdx = ChannelFactory.getMaxSerialIdx();  // 获取串行通道最大索引
        for(int i=ChannelFactory.S1;i<maxIdx;i++){  // 遍历所有串行通道
            serialChannel = ChannelFactory.getSerialChannel(i);  // 获取串行通道
            if(serialChannel != null && serialChannel.isOpen()){  // 检查串行通道是否有效且已开启
                IBus bus = serialChannel.getBus();  // 获取总线对象
                if(bus.isChInSample(channel.getChId())) {  // 检查当前通道是否在该总线的采样中
                    if (serialChannel.getBusType() == IBus.ARINC429) {  // ARINC429总线
                        channel.setBusPrimaryLevel(highPos);  // 设置主电平为高电平
                        channel.setBusSecondaryLevel(lowPos);  // 设置次电平为低电平
                    } else {  // 其他总线（I2C、SPI等）
                        channel.setBusPrimaryLevel(halfPos);  // 设置主电平为中间位置
                        channel.setBusSecondaryLevel(halfPos);  // 设置次电平为中间位置
                    }
                }
            }
        }
    }
    
    /**
     * 根据串行总线波特率自动调整X轴时基
     * 
     * <p>对于I2C和SPI总线，根据时钟信号的周期测量值，
     * 自动计算合适的时基档位。
     */
    void TiaoZenXBySerialCodeBaundRate() {

        IBus bus;  // 总线接口引用
        int chIdx =  -1;  // 时钟通道索引，初始为-1
        SerialChannel serialChannel;  // 串行通道引用
        int maxIdx = ChannelFactory.getMaxSerialIdx();  // 获取串行通道最大索引
        for(int i=ChannelFactory.S1;i<maxIdx;i++){  // 遍历所有串行通道
            serialChannel = ChannelFactory.getSerialChannel(i);  // 获取串行通道
            if(serialChannel != null && serialChannel.isOpen()) {  // 检查串行通道是否有效且已开启
                bus = serialChannel.getBus();  // 获取总线对象
                if (serialChannel.getBusType() == IBus.I2C ) {  // I2C总线
                    I2CBus i2cBus = (I2CBus)bus;  // 转换为I2C总线
                    chIdx = i2cBus.getSclChIdx();  // 获取SCL时钟通道索引
                }else if(serialChannel.getBusType() == IBus.SPI){  // SPI总线
                    SpiBus spiBus = (SpiBus) bus;  // 转换为SPI总线
                    chIdx = spiBus.getClkChIdx();  // 获取CLK时钟通道索引
                }
                break;  // 找到第一个有效的串行通道后退出循环
            }
        }
        if(chIdx >= 0){  // 如果找到了时钟通道
            Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取时钟通道对象
            if(channel != null){  // 检查通道是否有效
                Measure measure = channel.getMeasure();  // 获取测量对象
                if(measure != null){  // 检查测量对象是否有效
                    if(measure.isMeasureItemValid(Measure.MeasureType.MEASURE_PERIOD)){  // 检查周期测量是否有效
                        float period = measure.getMeasureItemVal(Measure.MeasureType.MEASURE_PERIOD) * 4;  // 获取周期值并乘以4（显示约4个周期）
                        int scaleId = HorizontalAxis.getInstance().timeValtoTimeScaleId(period);  // 将周期值转换为时基档位ID
                        if(!HorizontalAxis.isRoolScale(scaleId)) {  // 检查是否为滚屏模式档位
                            HorizontalAxis.getInstance().setTimeScaleIdOfView(scaleId);  // 设置时基档位
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 自动调整X轴（水平时基）档位
     * 
     * <p>根据信号周期计算最佳时基档位，目标是使屏幕显示约3个完整周期。
     * 包含直流检测、周期稳定性检测和回差机制。
     * 
     * @param cycle 信号周期（纳秒）
     * @param ditAD 信号幅度（AD值差值）
     * @return 计算出的时基档位ID
     */
    int TiaoZenX(long cycle, int ditAD)
    {
        int dang = HorizontalAxis.getInstance().getTimeScaleIdOfView();  // 获取当前时基档位
        // 如果是直流，则固定在30档：100ns档
        if(dang == HorizontalAxis.TSI_10mS)  // 当前是10ms档（直流检测档位）
        {
            // 此处25比20大，是为了设置回差
            // 此处22Hz比20Hz大，也是为了设置回差
            // >22Hz或波动很小认为还是直流
            if(cycle > (int)(1e9/22) || ditAD < 25) return HorizontalAxis.TSI_10mS;  // 周期>45ms或幅度<25，认为是直流
        }
        else  // 当前不是直流档位
        {
            // ≥20Hz或交流份量太小，认为是直流
            if(cycle >= (int)(1e9/20) || ditAD < 20) return HorizontalAxis.TSI_10mS;  // 周期≥50ms或幅度<20，认为是直流
        }
        if(cycle == 0) cycle = 1;  // 防止除零错误
        if(lastSCyles == 0) lastSCyles = cycle;  // 初始化lastSCyles为第一个周期值
        if(XiuZenX(cycle) == 0) return dang;  // 周期不稳定，返回当前档位

        // 找出使周期>=2.2，且更接近于3的档位
        int i;  // 档位索引
        double fx1 = 0,fx2 = 0;  // 用于计算显示周期数
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();  // 获取水平轴管理单例
        for(i=HorizontalAxis.getMaxGear();i>HorizontalAxis.TSI_100mS;i--)  // 从最大档位向小档位遍历
        {
            fx2 = fx1;  // 保存上一个计算值
            fx1 = (double) (horizontalAxis.getTimeScaleIdVal(i)  // 计算当前档位下的显示周期数
                    * ScopeBase.getHorizonGridCnt() * 1e9/cycle);  // = 时基值 * 格数 * 1e9 / 周期
            if(fx1 >= 3) break;  // 找到显示周期数>=3的档位
        }
        // 计算距离3个周期的绝对误差
        fx1 = (double) Math.abs(fx1-3.0);  // 当前档位与3个周期的误差

        // 看看上一档是否更接近于3个周期
        if(i != HorizontalAxis.getMaxGear())  // 不是最大档位
        {
            // 检测上一个档（<3），是否更接近于3
            if(i<HorizontalAxis.TSI_100mS) i = HorizontalAxis.TSI_100mS;  // 限制最小档位
            if(fx2 >= 2.2)  // 至少要显示2.2个周期
            {
                fx2 = (double)Math.abs(3.0-fx2);  // 计算上一档与3个周期的误差
                if(fx1 > fx2)  // 上一档更接近3个周期
                {
                    // 更接近，则采用上一个档
                    i++;  // 使用上一档（档位ID大表示时基大）
                    fx1 = fx2;  // 更新误差值
                }
            }
        }

        // 计算出的档位与现在的档位比较，看看那个更接近于3
        if(dang < HorizontalAxis.TSI_100mS) dang = HorizontalAxis.TSI_100mS;  // 限制当前档位最小值
        fx2 = (double)(HorizontalAxis.getInstance().getTimeScaleIdVal(dang)*ScopeBase.getHorizonGridCnt()*1e9/cycle);  // 计算当前档位的显示周期数
        fx2 = (double)Math.abs(fx2-3.0);  // 计算与3个周期的误差
        if(fx2>=0.2)  // 现在的档位，优惠0.2个周期
        {
            fx2 = (double)(fx2-0.2);  // 减去回差值
        }
        else
        {
            fx2 = 0;  // 误差小于回差，视为0
        }
        fx2 = Math.abs(fx2);  // 取绝对值

        if(fx2 <= fx1) return dang;  // 当前档位更优，保持不变
        else return i;  // 新档位更优，返回新档位
    }

    /** 周期稳定性检测的时间窗口（毫秒） */
    private static final int TIME_X_WENDING = 50;  // 未使用，保留
    
    /**
     * 检测周期稳定性
     * 
     * <p>比较当前周期与上次周期的差异，判断信号是否稳定。
     * 误差小于10%认为稳定。
     * 
     * @param cycle 当前信号周期（纳秒）
     * @return 1表示周期稳定，0表示周期不稳定
     */
    int XiuZenX(long cycle)
    {
        long del;  // 周期差值
        double fx;  // 相对误差

        // 计算与基准周期之差
        if(lastSCyles > cycle) del = lastSCyles-cycle;  // 计算绝对差值
        else del = cycle-lastSCyles;  // 计算绝对差值
        // 计算现在的周期与基准周期之间的误差
        fx = (double)del/lastSCyles;  // 计算相对误差

        if(fx < 0.1)  // 误差小于10%
        {
            lastSCyles = cycle;  // 更新基准周期
            return 1;  // 周期稳定
        }
        else
        {
            // 误差太大，周期无效，重设基准周期
            // delay = time.elapsed();
            lastSCyles = cycle;  // 重设基准周期
        }
        return 0;  // 周期不稳定
    }

    /**
     * 自动调整Y轴（垂直档位）
     * 
     * <p>根据信号幅度计算最佳垂直档位，目标是使信号占屏幕的70%-80%。
     * 包含饱和检测和档位限制。
     * 
     * @param channel 通道对象
     * @param maxAD AD采样最大值
     * @param minAD AD采样最小值
     * @return 计算出的垂直档位ID
     */
    int TiaoZenY(Channel channel,int maxAD, int minAD)
    {

        int offset = (int) Math.round(channel.getPos());  // 获取通道位置偏移
        int dang = channel.getVScaleId();  // 获取当前垂直档位

        // 取最大和最小值相距本通道Y轴位置的偏移量
        maxAD = maxAD-offset;  // 去除位置偏移
        minAD = minAD-offset;  // 去除位置偏移

        int num=ChannelFactory.getDynamicChannelOpenCount();  // 获取开启的动态通道数量
        if(num < 1) num = 1;  // 至少为1，防止除零

        int maxLimt,minLimt;  // 上下限值
        int n = ScopeBase.getVerticalPerGridPixels();  // 获取每格像素数
        if(Auto.getInstance().isAutoRangeEnable())  // 自动量程模式
        {
            maxLimt = (int)(((ScopeBase.getHeight()/2)-offset));  // 计算上限（屏幕上半部分）
            minLimt = (int)((-(ScopeBase.getHeight()/2)-offset));  // 计算下限（屏幕下半部分）

            if(maxLimt > 2*n){  // 上限大于2格
                maxLimt -= n;  // 减去1格作为余量
            }else{  // 上限较小
                maxLimt *= 0.8;  // 缩小到80%
            }
            if(minLimt < -2*n){  // 下限小于-2格
                minLimt += n;  // 加上1格作为余量
            }else{  // 下限较大
                minLimt *= 0.8;  // 缩小到80%
            }
        }
        else  // 单次自动模式
        {
            maxLimt = ((ScopeBase.getHeight()/(num*2)));//*0.9;  // 根据通道数分配屏幕空间
            minLimt = (-(ScopeBase.getHeight()/(num*2)));//*0.9;  // 根据通道数分配屏幕空间
            maxLimt *= 0.8;  // 缩小到80%
            minLimt *= 0.8;  // 缩小到80%
        }
        n = (int)(n * 0.2);  // 计算回差值（20%格）
        if(maxAD - maxLimt > n  || minLimt - minAD > n )  // 信号超出显示范围
        {
            if(dang < channel.getMaxGear())  // 未达到最大档位
            {

                dang = channel.calcVScaleId(dang,1);  // 增大一档
            }
        }
        else  // 信号在显示范围内
        {
            int i;  // 档位索引

            if(channel.getMinGear() == dang) return dang;  // 已是最小档位，直接返回

            i = dang;  // 从当前档位开始
            double maxV = maxAD*channel.getVerticalPerPix();  // 转换为电压值
            double minV = minAD*channel.getVerticalPerPix();  // 转换为电压值

            do  // 尝试减小档位
            {
                //i--;
                i = channel.calcVScaleId(i,-1);  // 减小一档

                maxAD = (int)(maxV/channel.getVerticalPerPix(i));// 换算成AD值
                minAD = (int)(minV/channel.getVerticalPerPix(i));// 换算成AD值


                if(maxAD > maxLimt || minAD < minLimt)  // 超出显示范围
                {
                    i = channel.calcVScaleId(i,1);  // 回退一档
                    break;  // 退出循环
                }
            }while(i>channel.getMinGear());  // 未达到最小档位
            dang = i;  // 更新档位
        }

        return dang;  // 返回计算出的档位
    }
    
    /**
     * 自动模式的触发电平校正
     * 
     * <p>根据信号幅度计算最佳触发电平位置。
     * 目标是将触发电平设置在信号中心位置。
     * 
     * @param channel 通道对象
     * @param max AD采样最大值
     * @param min AD采样最小值
     * @return 计算出的触发电平位置
     */
    double Auto_TrigReset(Channel channel,int max, int min)
    {
        int offset = (int) Math.round(channel.getPos());  // 获取通道位置偏移
        Trigger trigger = TriggerFactory.getTriggerObj();  // 获取触发对象
        TriggerLevel triggerLevel = trigger.getTriggerLevel();  // 获取触发电平对象

        double trigPlace = triggerLevel.getPos();  // 获取当前触发电平位置
        int newValue = max;  // 初始化为最大值
        if (-min > max)  // 如果最小值的绝对值更大
            newValue = -min;  // 使用最小值的绝对值
        if(510 <= newValue)  return trigPlace;  // 信号饱和，不再进行触发位置调整

        // 判断原触发位置是否在不调整范围内：中心位置的+-%5范围内
        max -= offset;  // 去零
        min -= offset;  // 去零

        if((trigPlace < max) && (trigPlace > min))  // 触发电平在信号范围内
        {
            // 触发位置允许有+-5%的误差
            if(Math.abs((double)(trigPlace-min)/(max - min)-0.5) < 0.05)  // 在中心±5%范围内
            {
                return trigPlace;  // 不调整
            }
        }

        // 计算新的触发位置
        // 新触发位置与旧触发位置的偏差在+-4个像素内则不调整触发位置，否则调整新位置
        newValue = (max+min)/2;  // 计算信号中心位置

        if(Math.abs(newValue - trigPlace) < 5)  // 触发位置允许误差+-4个像素
        {
            return trigPlace;  // 差异太小，不调整
        }
        return newValue;  // 返回新的触发电平位置
    }
}
