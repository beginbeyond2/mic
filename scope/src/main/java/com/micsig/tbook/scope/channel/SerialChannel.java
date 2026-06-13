package com.micsig.tbook.scope.channel;  // 定义包名：示波器通道管理模块

import com.micsig.tbook.scope.Bus.ARINC429Bus;  // 导入ARINC429Bus类：航空电子数字总线
import com.micsig.tbook.scope.Bus.CanBus;  // 导入CanBus类：控制器局域网总线
import com.micsig.tbook.scope.Bus.I2CBus;  // 导入I2CBus类：两线式串行总线
import com.micsig.tbook.scope.Bus.IBus;  // 导入IBus接口：总线接口定义
import com.micsig.tbook.scope.Bus.LinBus;  // 导入LinBus类：局域互联网络总线
import com.micsig.tbook.scope.Bus.MILSTD1553BBus;  // 导入MILSTD1553BBus类：军用标准总线
import com.micsig.tbook.scope.Bus.SpiBus;  // 导入SpiBus类：串行外设接口总线
import com.micsig.tbook.scope.Bus.UartBus;  // 导入UartBus类：通用异步收发传输器总线
import com.micsig.tbook.scope.Data.DataFactory;  // 导入DataFactory类：数据缓冲区工厂
import com.micsig.tbook.scope.Data.IBufferQueue;  // 导入IBufferQueue接口：缓冲区队列接口
import com.micsig.tbook.scope.Data.IDataBuffer;  // 导入IDataBuffer接口：数据缓冲区接口
import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂
import com.micsig.tbook.scope.ScopeBase;  // 导入ScopeBase类：示波器基类
import com.micsig.tbook.scope.measure.Measure;  // 导入Measure类：测量管理

/**
 * 串口通道类 - 串行总线解码通道实现
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道管理模块）</li>
 *   <li>架构层级：数据层 - 串口通道实现</li>
 *   <li>设计模式：组合模式 + 策略模式</li>
 *   <li>职责类型：串行总线解码、多总线类型管理、数据缓冲</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现IChannel接口，提供串口通道的基本功能</li>
 *   <li>管理多种串行总线类型的解码（UART/LIN/CAN/SPI/I2C/ARINC429/MIL-STD-1553B）</li>
 *   <li>提供数据缓冲区队列管理</li>
 *   <li>处理通道打开/关闭事件</li>
 * </ul>
 * 
 * <p><b>串行总线类型架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   SerialChannel - 串行总线解码通道                                        │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │                        IBus[] 总线数组                           │   │
 * │   │                                                                   │   │
 * │   │   [0] UartBus          通用异步收发传输器                         │   │
 * │   │       └── 支持标准串口通信协议                                     │   │
 * │   │                                                                   │   │
 * │   │   [1] LinBus           局域互联网络                               │   │
 * │   │       └── 汽车电子低速网络协议                                     │   │
 * │   │                                                                   │   │
 * │   │   [2] CanBus           控制器局域网                               │   │
 * │   │       └── 汽车电子高速网络协议                                     │   │
 * │   │                                                                   │   │
 * │   │   [3] SpiBus           串行外设接口                               │   │
 * │   │       └── 高速同步串行通信协议                                     │   │
 * │   │                                                                   │   │
 * │   │   [4] I2CBus           两线式串行总线                             │   │
 * │   │       └── 低速双线串行通信协议                                     │   │
 * │   │                                                                   │   │
 * │   │   [5] ARINC429Bus      航空电子数字总线                           │   │
 * │   │       └── 航空电子标准通信协议                                     │   │
 * │   │                                                                   │   │
 * │   │   [6] MILSTD1553BBus   军用标准总线                               │   │
 * │   │       └── 军用航空电子通信协议                                     │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   当前总线类型：busType（默认为UART）                                    │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>通道状态管理：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   通道状态转换                                                            │
 * │                                                                          │
 * │   ┌─────────┐    open()     ┌─────────┐                                 │
 * │   │ 关闭    │ ───────────► │ 打开    │                                 │
 * │   │ bOpen   │               │ bOpen   │                                 │
 * │   │ =false  │ ◄─────────── │ =true   │                                 │
 * │   └─────────┘    close()    └─────────┘                                 │
 * │                                                                          │
 * │   打开/关闭操作流程：                                                     │
 * │   1. 设置bOpen状态                                                       │
 * │   2. 调用serialChannelAction.busTypeChange()                            │
 * │   3. 发送EVENT_CHANNEL_OPEN/CLOSE事件                                   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>数据缓冲区管理：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   数据缓冲区队列                                                          │
 * │                                                                          │
 * │   IBufferQueue（由DataFactory.allocateBufferSerial()创建）               │
 * │       │                                                                   │
 * │       ├── obtain()     获取空闲缓冲区                                    │
 * │       ├── recycle()    回收缓冲区                                        │
 * │       ├── dequeue()    取出已填充缓冲区                                  │
 * │       └── enqueue()    放入已填充缓冲区                                  │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>实现接口：</b>
 * <ul>
 *   <li>实现：IChannel（通道接口）</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：IBus（总线接口，7种总线实现）</li>
 *   <li>依赖：SerialChannelAction（串口通道动作代理）</li>
 *   <li>依赖：IBufferQueue（数据缓冲区队列）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see IChannel 通道接口
 * @see IBus 总线接口
 * @see SerialChannelAction 串口通道动作代理
 */
public class SerialChannel implements IChannel {  // 实现IChannel通道接口

    /** Z轴顺序：用于通道显示的层叠顺序 */
    private int zOrder = 0;  // Z轴顺序值
    
    /** 通道位置：对应1000的值，用于垂直位置计算 */
    private double pos = 0;  // 通道垂直位置
    
    /** 通道打开状态：true表示打开，false表示关闭 */
    private boolean bOpen = false;  // 通道打开标志
    
    /** 通道索引：唯一标识此通道 */
    private int chIdx = -1;  // 通道ID
    
    /** 通道名称：如"S0"、"S1"等 */
    private String name;  // 通道名称
    
    /** 数据缓冲区队列：用于存储解码后的数据 */
    private IBufferQueue dataBufferQueue = null;  // 数据缓冲区队列
    
    /** 总线数组：存储所有支持的串行总线实例 */
    private IBus[]bus = new IBus[IBus.BUS_CNT];  // 总线实例数组
    
    /** 当前总线类型：默认为UART */
    private int busType = IBus.UART;  // 当前总线类型
    
    /** 串口通道动作代理：用于发送消息和事件 */
    private SerialChannelAction serialChannelAction;  // 动作代理
    
    /** 通道标签：用户自定义标签 */
    private String label = "";  // 通道标签

    /**
     * 构造方法：初始化串口通道
     * 
     * <p>创建所有支持的串行总线实例，初始化动作代理和数据缓冲区。
     * 
     * @param chIdx 通道索引
     */
    public SerialChannel(int chIdx){
        this.chIdx = chIdx;  // 保存通道索引
        this.name = "S" + chIdx;  // 设置通道名称（如"S0"）
        bus[IBus.UART] = new UartBus(chIdx);  // 创建UART总线实例
        bus[IBus.LIN] = new LinBus(chIdx);  // 创建LIN总线实例
        bus[IBus.CAN] = new CanBus(chIdx);  // 创建CAN总线实例
        bus[IBus.SPI] = new SpiBus(chIdx);  // 创建SPI总线实例
        bus[IBus.I2C] = new I2CBus(chIdx);  // 创建I2C总线实例
        bus[IBus.ARINC429] = new ARINC429Bus(chIdx);  // 创建ARINC429总线实例
        bus[IBus.MILSTD1553B] = new MILSTD1553BBus(chIdx);  // 创建MIL-STD-1553B总线实例
        serialChannelAction = new SerialChannelAction(this);  // 创建动作代理
        setBuffer(DataFactory.allocateBufferSerial());  // 分配串口数据缓冲区
    }

    /**
     * 获取通道名称
     * 
     * @return 通道名称
     */
    @Override
    public String getName() {
        return name;  // 返回通道名称
    }

    /**
     * 设置通道标签
     * 
     * @param label 标签字符串
     */
    @Override
    public void setLabel(String label) {
        this.label = label;  // 设置标签
    }

    /**
     * 获取通道标签
     * 
     * @return 标签字符串
     */
    @Override
    public String getLabel() {
        return label;  // 返回标签
    }

    /**
     * 获取指定类型的总线实例
     * 
     * @param busIdx 总线类型索引
     * @return 总线实例，无效索引返回null
     */
    public IBus getBus(int busIdx){
        if(IBus.isValid(busIdx))  // 验证总线类型是否有效
            return bus[busIdx];  // 返回指定类型的总线实例
        return null;  // 无效索引返回null
    }

    /**
     * 获取当前总线类型的实例
     * 
     * @return 当前总线实例
     */
    public IBus getBus(){
        return getBus(busType);  // 返回当前总线类型的实例
    }

    /**
     * 设置总线类型
     * 
     * <p>切换当前使用的串行总线类型，触发总线类型变化通知。
     * 
     * @param busType 总线类型索引
     */
    public void setBusType(int busType){
        if(IBus.isValid(busType)) {  // 验证总线类型是否有效
            this.busType = busType;  // 设置总线类型
            serialChannelAction.busTypeChange();  // 通知总线类型变化
        }
    }

    /**
     * 获取当前总线类型
     * 
     * @return 总线类型索引
     */
    public int getBusType(){
        return busType;  // 返回当前总线类型
    }

    /**
     * 检查指定通道是否在当前总线采样中
     * 
     * @param chIdx 通道索引
     * @return true表示在采样中，false表示不在
     */
    public boolean isChInSample(int chIdx){
        IBus bus = getBus();  // 获取当前总线实例
        return bus.isChInSample(chIdx);  // 返回通道采样状态
    }

    /**
     * 检查指定通道是否被任何总线使用
     * 
     * <p>遍历所有总线类型，检查通道是否在任一总线的采样中。
     * 
     * @param chIdx 通道索引
     * @return true表示被使用，false表示未被使用
     */
    public boolean isChUse(int chIdx){
        boolean bChOpen = false;  // 初始化结果
        for (int i=0;i<IBus.BUS_CNT;i++){  // 遍历所有总线类型
            if(bus[i].isChInSample(chIdx)){  // 检查通道是否在采样中
                bChOpen = true;  // 设置为使用中
                break;  // 找到一个即可退出循环
            }
        }
        return  bChOpen;  // 返回使用状态
    }

    /**
     * 获取测量管理器
     * 
     * <p>串口通道不支持测量功能，返回null。
     * 
     * @return null
     */
    @Override
    public Measure getMeasure() {
        return null;  // 串口通道不支持测量
    }

    /**
     * 设置数据缓冲区队列
     * 
     * @param dataBufferQueue 缓冲区队列
     */
    @Override
    public void setBuffer(IBufferQueue dataBufferQueue) {
        this.dataBufferQueue = dataBufferQueue;  // 设置缓冲区队列
    }


    /**
     * 打开通道
     * 
     * <p>设置通道为打开状态，通知总线类型变化，发送通道打开事件。
     */
    @Override
    public void open() {
        synchronized (this) {  // 同步锁
            bOpen = true;  // 设置打开状态
        }
        serialChannelAction.busTypeChange();  // 通知总线类型变化
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_CHANNEL_OPEN,getChId()));  // 发送通道打开事件
    }

    /**
     * 检查通道是否打开
     * 
     * @return true表示打开，false表示关闭
     */
    @Override
    public boolean isOpen() {
        synchronized (this) {  // 同步锁
            return bOpen;  // 返回打开状态
        }
    }

    /**
     * 关闭通道
     * 
     * <p>设置通道为关闭状态，通知总线类型变化，发送通道关闭事件。
     */
    @Override
    public void close() {
        synchronized (this) {  // 同步锁
            bOpen = false;  // 设置关闭状态
        }
        serialChannelAction.busTypeChange();  // 通知总线类型变化
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_CHANNEL_CLOSE,getChId()));  // 发送通道关闭事件
    }

    /**
     * 激活通道
     * 
     * <p>当前为空实现。
     */
    @Override
    public void activate() {

    }

    /**
     * 设置通道位置
     * 
     * <p>将UI坐标转换为FPGA坐标并保存。
     * 
     * @param pos 位置值（UI坐标）
     */
    @Override
    public void setPos(double pos) {
        pos = pos * ScopeBase.getToFPGACoff();  // 转换为FPGA坐标
        this.pos = pos;  // 保存位置
    }

    /**
     * 获取UI显示位置
     * 
     * <p>将FPGA坐标转换为UI坐标并返回。
     * 
     * @return 位置值（UI坐标）
     */
    @Override
    public double getPosUI() {
        return ScopeBase.changeAccuracy(pos * ScopeBase.getToUICoff());  // 转换为UI坐标并返回
    }

    /**
     * 获取通道位置
     * 
     * @return 位置值（FPGA坐标）
     */
    @Override
    public double getPos() {
        return this.pos;  // 返回位置
    }

    /**
     * 获取通道ID
     * 
     * @return 通道索引
     */
    @Override
    public int getChId() {
        return chIdx;  // 返回通道索引
    }

    /**
     * 获取Z轴顺序
     * 
     * @return Z轴顺序值
     */
    @Override
    public int getzOrder() {
        return zOrder;  // 返回Z轴顺序
    }

    /**
     * 设置Z轴顺序
     * 
     * @param zOrder Z轴顺序值
     */
    @Override
    public void setzOrder(int zOrder) {
        this.zOrder = zOrder;  // 设置Z轴顺序
    }

    /**
     * 获取每像素垂直单位
     * 
     * <p>串口通道不支持此功能，返回0。
     * 
     * @return 0
     */
    @Override
    public double getVerticalPerPix() {
        return 0;  // 串口通道不支持
    }

    /**
     * 获取AD每像素垂直单位
     * 
     * <p>串口通道不支持此功能，返回0。
     * 
     * @return 0
     */
    @Override
    public double getADVerticalPerPix() {
        return 0;  // 串口通道不支持
    }

    /**
     * 获取采样率
     * 
     * <p>串口通道不支持此功能，返回0。
     * 
     * @return 0
     */
    @Override
    public double getSampleRate() {
        return 0;  // 串口通道不支持
    }

    /**
     * 获取显示用采样率
     * 
     * <p>串口通道不支持此功能，返回0。
     * 
     * @return 0
     */
    @Override
    public double getSampleRate2display(){return 0;}  // 串口通道不支持

    /**
     * 保存数据到文件
     * 
     * <p>串口通道不支持此功能，返回false。
     * 
     * @param pathName 文件路径
     * @return false
     */
    @Override
    public boolean save(String pathName) {
        return false;  // 串口通道不支持
    }

    /**
     * 保存数据为CSV格式
     * 
     * <p>串口通道不支持此功能，返回false。
     * 
     * @param pathName 文件路径
     * @return false
     */
    @Override
    public boolean saveCSV(String pathName) {
        return false;  // 串口通道不支持
    }


    /**
     * 获取空闲数据缓冲区
     * 
     * <p>从缓冲区队列获取一个空闲缓冲区用于写入数据。
     * 
     * @return 数据缓冲区
     */
    @Override
    public IDataBuffer obtain() {
        return dataBufferQueue.obtain();  // 从队列获取空闲缓冲区
    }

    /**
     * 回收数据缓冲区
     * 
     * <p>将使用完毕的缓冲区归还到队列。
     * 
     * @param dataBuffer 数据缓冲区
     */
    @Override
    public void recycle(IDataBuffer dataBuffer) {
        dataBufferQueue.recycle(dataBuffer);  // 回收缓冲区到队列
    }

    /**
     * 取出已填充的数据缓冲区
     * 
     * <p>从队列中取出一个已填充数据的缓冲区用于读取。
     * 
     * @return 数据缓冲区
     */
    @Override
    public IDataBuffer dequeue() {
        return dataBufferQueue.dequeue();  // 从队列取出已填充缓冲区
    }

    /**
     * 放入已填充的数据缓冲区
     * 
     * <p>将已填充数据的缓冲区放入队列等待读取。
     * 
     * @param dataBuffer 数据缓冲区
     */
    @Override
    public void enqueue(IDataBuffer dataBuffer) {
        dataBufferQueue.enqueue(dataBuffer);  // 将缓冲区放入队列
    }
}
