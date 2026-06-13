package com.micsig.tbook.scope.channel;  // 定义包名：示波器通道管理模块

import com.micsig.tbook.scope.Data.IBufferQueue;  // 导入IBufferQueue接口：缓冲区队列接口
import com.micsig.tbook.scope.Data.IDataBuffer;  // 导入IDataBuffer接口：数据缓冲区接口
import com.micsig.tbook.scope.measure.Measure;  // 导入Measure类：测量管理

/**
 * 通道接口 - 示波器通道行为抽象
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道管理模块）</li>
 *   <li>架构层级：接口层 - 通道行为抽象</li>
 *   <li>设计模式：接口模式 + 策略模式</li>
 *   <li>职责类型：定义通道的基本行为契约</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义通道的生命周期方法（open/close/activate）</li>
 *   <li>定义通道的位置管理方法</li>
 *   <li>定义通道的数据缓冲区管理方法</li>
 *   <li>定义通道的显示属性方法</li>
 * </ul>
 * 
 * <p><b>接口方法分类：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   IChannel接口方法分类                                                    │
 * │                                                                          │
 * │   生命周期管理：                                                          │
 * │   ├── open()         打开通道                                            │
 * │   ├── close()        关闭通道                                            │
 * │   ├── activate()     激活通道                                            │
 * │   └── isOpen()       检查通道是否打开                                    │
 * │                                                                          │
 * │   位置管理：                                                              │
 * │   ├── setPos(double) 设置通道位置                                        │
 * │   ├── getPos()       获取通道位置（FPGA坐标）                            │
 * │   └── getPosUI()     获取通道位置（UI坐标）                              │
 * │                                                                          │
 * │   显示属性：                                                              │
 * │   ├── getChId()      获取通道ID                                         │
 * │   ├── getzOrder()    获取Z轴顺序                                        │
 * │   ├── setzOrder(int) 设置Z轴顺序                                        │
 * │   ├── getName()      获取通道名称                                       │
 * │   ├── getLabel()     获取通道标签                                       │
 * │   └── setLabel(String) 设置通道标签                                     │
 * │                                                                          │
 * │   垂直轴属性：                                                            │
 * │   ├── getVerticalPerPix()    获取每像素垂直单位                         │
 * │   └── getADVerticalPerPix()  获取AD每像素垂直单位                       │
 * │                                                                          │
 * │   采样属性：                                                              │
 * │   ├── getSampleRate()        获取采样率                                 │
 * │   └── getSampleRate2display() 获取显示用采样率                          │
 * │                                                                          │
 * │   数据存储：                                                              │
 * │   ├── save(String)   保存数据到文件                                     │
 * │   └── saveCSV(String) 保存数据为CSV格式                                 │
 * │                                                                          │
 * │   数据缓冲区：                                                            │
 * │   ├── setBuffer(IBufferQueue) 设置数据缓冲区队列                        │
 * │   ├── obtain()       获取空闲缓冲区                                     │
 * │   ├── recycle(IDataBuffer) 回收缓冲区                                   │
 * │   ├── dequeue()      取出已填充缓冲区                                   │
 * │   └── enqueue(IDataBuffer) 放入已填充缓冲区                             │
 * │                                                                          │
 * │   测量功能：                                                              │
 * │   └── getMeasure()   获取测量管理器                                     │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>实现类：</b>
 * <pre>
 *   IChannel（通道接口）
 *       │
 *       ├── Channel          模拟通道（物理通道，支持测量、采样等完整功能）
 *       │       └── 实现：MHO38v1_Channel、MHO68v1_Channel、MHO68v2_Channel
 *       │
 *       ├── MathChannel      数学通道（数学运算结果通道）
 *       │       └── 支持：加/减/乘/除/FFT等运算
 *       │
 *       ├── RefChannel       参考通道（参考波形通道）
 *       │       └── 用于存储和显示参考波形
 *       │
 *       └── SerialChannel    串口通道（串行总线解码通道）
 *               └── 支持：UART/LIN/CAN/SPI/I2C/ARINC429/MIL-STD-1553B
 * </pre>
 * 
 * <p><b>通道状态机：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   通道状态转换                                                            │
 * │                                                                          │
 * │   ┌─────────┐    open()     ┌─────────┐                                 │
 * │   │ 关闭    │ ───────────► │ 打开    │                                 │
 * │   │         │               │         │                                 │
 * │   │         │ ◄─────────── │         │                                 │
 * │   └─────────┘    close()    └─────────┘                                 │
 * │                              │                                           │
 * │                              │ activate()                                │
 * │                              ▼                                           │
 * │                        ┌─────────┐                                       │
 * │                        │ 激活    │                                       │
 * │                        │ (当前   │                                       │
 * │                        │  操作   │                                       │
 * │                        │  通道)  │                                       │
 * │                        └─────────┘                                       │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>数据缓冲区流程：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   数据缓冲区生命周期                                                       │
 * │                                                                          │
 * │   数据采集线程                      数据处理线程                          │
 * │       │                                   │                              │
 * │       │ obtain()                          │                              │
 * │       │ 获取空闲缓冲区                    │                              │
 * │       ▼                                   │                              │
 * │   ┌─────────┐                             │                              │
 * │   │ 填充    │                             │                              │
 * │   │ 数据    │                             │                              │
 * │   └─────────┘                             │                              │
 * │       │                                   │                              │
 * │       │ enqueue()                         │ dequeue()                    │
 * │       │ 放入已填充队列 ──────────────────►│ 取出已填充缓冲区             │
 * │       │                                   │                              │
 * │       │                                   ▼                              │
 * │       │                             ┌─────────┐                         │
 * │       │                             │ 处理    │                         │
 * │       │                             │ 数据    │                         │
 * │       │                             └─────────┘                         │
 * │       │                                   │                              │
 * │       ◄───────────────────────────────────┤ recycle()                    │
 * │       │         回收缓冲区                 │                              │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * @author zhuzh
 * @version 1.0
 * @see Channel 模拟通道实现
 * @see MathChannel 数学通道实现
 * @see RefChannel 参考通道实现
 * @see SerialChannel 串口通道实现
 */
public interface IChannel {

    /**
     * 获取测量管理器
     * 
     * <p>返回通道关联的测量管理器，用于执行测量功能。
     * 某些通道类型（如串口通道）可能不支持测量，返回null。
     * 
     * @return 测量管理器实例，不支持测量返回null
     */
    Measure getMeasure();

    /**
     * 设置数据缓冲区队列
     * 
     * <p>为通道设置数据缓冲区队列，用于存储波形数据。
     * 
     * @param dataBuffer 数据缓冲区队列
     */
    void setBuffer(IBufferQueue dataBuffer);

    /**
     * 打开通道
     * 
     * <p>打开通道，使其开始采集或显示数据。
     * 打开后通道处于可见状态。
     */
    void open();

    /**
     * 检查通道是否打开
     * 
     * @return true表示通道已打开，false表示通道已关闭
     */
    boolean isOpen();

    /**
     * 关闭通道
     * 
     * <p>关闭通道，停止采集或显示数据。
     * 关闭后通道处于不可见状态。
     */
    void close();

    /**
     * 激活通道
     * 
     * <p>将通道设置为当前活动通道，使其成为用户操作的目标。
     * 激活后通道通常会被移到显示层级的最顶层。
     */
    void activate();

    /**
     * 设置通道位置
     * 
     * <p>设置通道的垂直位置（Y轴偏移）。
     * 位置值使用FPGA坐标系统。
     * 
     * @param pos 位置值（FPGA坐标）
     */
    void setPos(double pos);

    /**
     * 获取通道位置
     * 
     * <p>返回通道的垂直位置，使用FPGA坐标系统。
     * 
     * @return 位置值（FPGA坐标）
     */
    double getPos();

    /**
     * 获取UI显示位置
     * 
     * <p>返回通道的垂直位置，使用UI坐标系统。
     * FPGA坐标和UI坐标之间存在转换关系。
     * 
     * @return 位置值（UI坐标）
     */
    double getPosUI();

    /**
     * 获取通道ID
     * 
     * <p>返回通道的唯一标识符。
     * ID值对应ChannelFactory中定义的通道索引常量。
     * 
     * @return 通道ID（0-27）
     */
    int getChId();

    /**
     * 获取Z轴顺序
     * 
     * <p>返回通道的显示层级顺序。
     * Z-Order值越大，通道显示越靠前（越靠近顶层）。
     * 
     * @return Z轴顺序值
     */
    int getzOrder();

    /**
     * 设置Z轴顺序
     * 
     * <p>设置通道的显示层级顺序。
     * 通常由ChannelFactory统一管理，不应直接调用。
     * 
     * @param zOrder Z轴顺序值
     */
    void setzOrder(int zOrder);

    /**
     * 获取每像素垂直单位
     * 
     * <p>返回通道垂直方向上每个像素代表的物理量值。
     * 用于将像素坐标转换为物理量。
     * 
     * @return 每像素垂直单位值
     */
    double getVerticalPerPix();

    /**
     * 获取AD每像素垂直单位
     * 
     * <p>返回AD采样值方向上每个像素代表的值。
     * 用于将像素坐标转换为AD值。
     * 
     * @return AD每像素垂直单位值
     */
    double getADVerticalPerPix();

    /**
     * 获取采样率
     * 
     * <p>返回通道的采样率（样本/秒）。
     * 某些通道类型（如参考通道、串口通道）可能不支持采样，返回0。
     * 
     * @return 采样率（Sa/s），不支持返回0
     */
    double getSampleRate();

    /**
     * 获取显示用采样率
     * 
     * <p>返回用于显示计算的采样率。
     * 可能与实际采样率不同，用于UI显示优化。
     * 
     * @return 显示用采样率
     */
    double getSampleRate2display();

    /**
     * 保存数据到文件
     * 
     * <p>将通道的波形数据保存到指定文件。
     * 文件格式由具体实现决定。
     * 
     * @param pathName 文件路径
     * @return true表示保存成功，false表示保存失败
     */
    boolean save(String pathName);

    /**
     * 保存数据为CSV格式
     * 
     * <p>将通道的波形数据保存为CSV格式文件。
     * CSV文件可用于后续数据分析。
     * 
     * @param pathName 文件路径
     * @return true表示保存成功，false表示保存失败
     */
    boolean saveCSV(String pathName);

    // ==================== 数据缓冲区操作方法 ====================

    /**
     * 获取空闲数据缓冲区
     * 
     * <p>从缓冲区队列获取一个空闲缓冲区，用于写入数据。
     * 获取后需要在使用完毕后调用recycle()归还。
     * 
     * @return 空闲数据缓冲区
     */
    IDataBuffer obtain();

    /**
     * 回收数据缓冲区
     * 
     * <p>将使用完毕的缓冲区归还到空闲队列。
     * 与obtain()配对使用。
     * 
     * @param dataBuffer 待回收的数据缓冲区
     */
    void recycle(IDataBuffer dataBuffer);

    /**
     * 取出已填充的数据缓冲区
     * 
     * <p>从已填充队列中取出一个缓冲区，用于读取数据。
     * 读取完毕后需要调用recycle()归还。
     * 
     * @return 已填充的数据缓冲区
     */
    IDataBuffer dequeue();

    /**
     * 放入已填充的数据缓冲区
     * 
     * <p>将已填充数据的缓冲区放入已填充队列，等待被读取。
     * 与dequeue()配对使用。
     * 
     * @param dataBuffer 已填充的数据缓冲区
     */
    void enqueue(IDataBuffer dataBuffer);

    /**
     * 获取通道名称
     * 
     * <p>返回通道的显示名称，如"CH1"、"MATH1"、"REF1"、"S1"等。
     * 
     * @return 通道名称字符串
     */
    String getName();

    /**
     * 设置通道标签
     * 
     * <p>设置用户自定义的通道标签，用于标识通道用途。
     * 
     * @param label 标签字符串
     */
    void setLabel(String label);

    /**
     * 获取通道标签
     * 
     * <p>返回用户自定义的通道标签。
     * 
     * @return 标签字符串，未设置返回空字符串
     */
    String getLabel();
}
