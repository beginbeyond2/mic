package com.micsig.tbook.scope.fpga;   // FPGA寄存器包，包含所有FPGA通信相关的寄存器类

import android.util.Log;   // 导入Android日志类，用于调试输出

import com.micsig.tbook.hardware.HardwareProduct;   // 导入硬件产品类，用于判断硬件型号
import com.micsig.tbook.scope.Auto.FreqCounter;   // 导入频率计数器类，用于频率测量功能
import com.micsig.tbook.scope.Calibrate.CabteRegister;   // 导入校准寄存器类，用于获取校准参数
import com.micsig.tbook.scope.Calibrate.HW;   // 导入硬件校准类，用于校准常量
import com.micsig.tbook.scope.Calibrate.HwConfig;   // 导入硬件配置类，用于获取硬件配置参数
import com.micsig.tbook.scope.Sample.Sample;   // 导入采样管理类，用于获取采样类型
import com.micsig.tbook.scope.Sample.SegmentSample;   // 导入分段采样管理类，用于分段采样功能
import com.micsig.tbook.scope.Scope;   // 导入示波器主类，用于获取示波器状态
import com.micsig.tbook.scope.channel.Channel;   // 导入通道类，用于获取通道参数
import com.micsig.tbook.scope.channel.ChannelFactory;   // 导入通道工厂类，用于获取通道实例

/**
 * FPGA采样模式寄存器类 - 控制示波器的核心采样参数
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    FPGA采样模式寄存器架构                        │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
 * │  │   Scope     │  │   Sample    │  │  Channel    │             │
 * │  │ (示波器状态)│  │ (采样管理)  │  │ (通道参数)  │             │
 * │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
 * │         │                │                │                     │
 * │         │ isAuto()       │ getSampleType()│ isInvert()          │
 * │         │ isInScrollMode│                │ getCoupleType()     │
 * │         │ isInSlowScale │                │                     │
 * │         ▼                ▼                ▼                     │
 * │  ┌─────────────────────────────────────────────────┐           │
 * │  │          FPGAReg_SAMPLE_MODE                    │           │
 * │  │           (采样模式寄存器)                       │           │
 * │  │                                                 │           │
 * │  │  ┌─────────────────────────────────────────┐   │           │
 * │  │  │  寄存器数据 (4字节 = 32位)              │   │           │
 * │  │  │  ┌───────────────────────────────────┐  │   │           │
 * │  │  │  │ 位2: 自动模式                     │  │   │           │
 * │  │  │  │ 位3: 显示模式(滚动)               │  │   │           │
 * │  │  │  │ 位4: 慢时基模式                   │  │   │           │
 * │  │  │  │ 位5: 高刷新模式                   │  │   │           │
 * │  │  │  │ 位6-9: 通道相位(4位)              │  │   │           │
 * │  │  │  │ 位10-12: 单通道模式(3位)          │  │   │           │
 * │  │  │  │ 位15: 频率计数器禁用              │  │   │           │
 * │  │  │  │ 位16-17: 采样类型(2位)            │  │   │           │
 * │  │  │  │ 位18: 波形类型                    │  │   │           │
 * │  │  │  │ 位19: 需要波形                    │  │   │           │
 * │  │  │  │ 位20: 高分辨率                    │  │   │           │
 * │  │  │  │ 位21-24: 耦合通道(4位)            │  │   │           │
 * │  │  │  │ 位25-26: 频率源通道(2位)          │  │   │           │
 * │  │  │  │ 位27-30: 通道使能(4位)            │  │   │           │
 * │  │  │  │ 位31: 分段采样使能                │  │   │           │
 * │  │  │  └───────────────────────────────────┘  │   │           │
 * │  │  └─────────────────────────────────────────┘   │           │
 * │  └─────────────────────────────────────────────────┘           │
 * │                       │                                       │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │   FPGA硬件 (0x00)   │                             │
 * │           │  采样模式综合控制   │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是FPGA采样系统的核心控制寄存器，负责配置示波器的所有采样模式参数。
 * 这是FPGA寄存器中最复杂、最重要的寄存器之一，几乎涵盖了示波器所有核心功能模式。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>模式配置</b>：设置自动模式、滚动模式、慢时基模式等</li>
 *   <li><b>采样类型</b>：配置普通、峰值、平均、包络采样模式</li>
 *   <li><b>通道控制</b>：设置通道相位、耦合方式、使能状态</li>
 *   <li><b>分段采样</b>：控制分段采样功能的启用</li>
 *   <li><b>频率计数</b>：配置频率计数器源通道</li>
 * </ul>
 * 
 * <h3>寄存器数据格式详解</h3>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │              寄存器数据结构 (4字节 = 32位)                      │
 * ├────────────────────────────────────────────────────────────────┤
 * │  位范围  │  字段名        │  说明                              │
 * ├────────────────────────────────────────────────────────────────┤
 * │  2       │  autoMode      │  自动模式标志                      │
 * │          │                │  0: 手动模式, 1: 自动模式          │
 * ├────────────────────────────────────────────────────────────────┤
 * │  3       │  dispMode      │  显示模式标志                      │
 * │          │                │  0: 正常模式, 1: 滚动模式          │
 * ├────────────────────────────────────────────────────────────────┤
 * │  4       │  slowScale     │  慢时基模式标志                    │
 * │          │                │  0: 正常时基, 1: 慢时基模式        │
 * ├────────────────────────────────────────────────────────────────┤
 * │  5       │  highRefresh   │  高刷新模式标志                    │
 * │          │                │  0: 正常刷新, 1: 高刷新率          │
 * ├────────────────────────────────────────────────────────────────┤
 * │  6-9     │  chPhase       │  通道相位 (4位)                    │
 * │          │                │  用于通道间相位校准                │
 * ├────────────────────────────────────────────────────────────────┤
 * │  10-12   │  singleCh      │  单通道模式 (3位)                  │
 * │          │                │  0: 4通道, 1: 2通道, 2: 单通道     │
 * │          │                │  4: 8通道模式                      │
 * ├────────────────────────────────────────────────────────────────┤
 * │  15      │  freqDis       │  频率计数器禁用标志                │
 * │          │                │  0: 启用, 1: 禁用                  │
 * ├────────────────────────────────────────────────────────────────┤
 * │  16-17   │  sampMode      │  采样类型 (2位)                    │
 * │          │                │  0: 普通, 1: 峰值, 2: 平均, 3: 包络│
 * ├────────────────────────────────────────────────────────────────┤
 * │  18      │  waveType      │  波形类型                          │
 * │          │                │  0: 正常波形                       │
 * ├────────────────────────────────────────────────────────────────┤
 * │  19      │  needWave      │  需要波形标志                      │
 * │          │                │  0: 不需要, 1: 需要波形数据        │
 * ├────────────────────────────────────────────────────────────────┤
 * │  20      │  highHr        │  高分辨率标志                      │
 * │          │                │  0: 正常分辨率, 1: 高分辨率        │
 * ├────────────────────────────────────────────────────────────────┤
 * │  21-24   │  coupCh        │  耦合通道 (4位)                    │
 * │          │                │  GND耦合标志                       │
 * ├────────────────────────────────────────────────────────────────┤
 * │  25-26   │  freSourcCh    │  频率源通道 (2位)                  │
 * │          │                │  0-3: 选择频率计数器源通道         │
 * ├────────────────────────────────────────────────────────────────┤
 * │  27-30   │  chEnable      │  通道使能 (4位)                    │
 * │          │                │  每位对应一个通道的采样使能        │
 * ├────────────────────────────────────────────────────────────────┤
 * │  31      │  segment       │  分段采样使能                      │
 * │          │                │  0: 禁用, 1: 启用分段采样          │
 * └────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>采样类型说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │                    采样类型详解                                │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  1. 普通采样 (SAMPLE_TYPE_NORMAL = 0)                         │
 * │     ┌─────────────────────────────────────────────┐           │
 * │     │  ADC按设定采样率直接采集信号                 │           │
 * │     │  每个采样点独立保存                          │           │
 * │     │  适用场景：通用测量                          │           │
 * │     │  特点：最常用的采样模式                      │           │
 * │     └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  2. 峰值检测 (SAMPLE_TYPE_PEAK = 1)                           │
 * │     ┌─────────────────────────────────────────────┐           │
 * │     │  在每个采样区间内记录最大值和最小值          │           │
 * │     │  确保不丢失快速变化的信号                    │           │
 * │     │  适用场景：捕获毛刺、快速信号                │           │
 * │     │  特点：适合慢时基下的信号捕获                │           │
 * │     └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  3. 平均采样 (SAMPLE_TYPE_AVERAGE = 2)                        │
 * │     ┌─────────────────────────────────────────────┐           │
 * │     │  对多次采样的数据进行平均                    │           │
 * │     │  降低随机噪声，提高测量精度                  │           │
 * │     │  适用场景：低频信号、噪声抑制                │           │
 * │     │  特点：存储深度受限                          │           │
 * │     └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  4. 包络采样 (SAMPLE_TYPE_ENVEL = 3)                          │
 * │     ┌─────────────────────────────────────────────┐           │
 * │     │  记录信号的包络（最大值和最小值）            │           │
 * │     │  适合观察调制信号和抖动                      │           │
 * │     │  适用场景：调制信号分析、抖动测量            │           │
 * │     │  特点：存储深度受限                          │           │
 * │     └─────────────────────────────────────────────┘           │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>显示模式说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │                    显示模式对比                                │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  ┌─────────────────┐  ┌─────────────────┐                    │
 * │  │   正常模式      │  │   滚动模式      │                    │
 * │  ├─────────────────┤  ├─────────────────┤                    │
 * │  │ 触发后捕获     │  │ 实时滚动显示   │                    │
 * │  │ 显示触发点附近 │  │ 波形从右向左   │                    │
 * │  │ 支持预触发     │  │ 类似纸带记录仪 │                    │
 * │  │ 快时基使用     │  │ 慢时基使用     │                    │
 * │  └─────────────────┘  └─────────────────┘                    │
 * │                                                               │
 * │  ┌─────────────────┐                                         │
 * │  │   慢时基模式    │                                         │
 * │  ├─────────────────┤                                         │
 * │  │ 慢时基非滚动    │                                         │
 * │  │ 支持触发        │                                         │
 * │  │ 支持缩放        │                                         │
 * │  │ 时基≥100ms/div │                                         │
 * │  └─────────────────┘                                         │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>通道数量与采样配置</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              通道数量与采样配置关系                            │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  通道数量  │  singleCh值  │  bitsWidth  │  bitsVal  │  说明  │
 * ├───────────────────────────────────────────────────────────────┤
 * │  2通道    │  1           │  4          │  0xF      │  单通道 │
 * │  4通道    │  0           │  2          │  0x03     │  双通道 │
 * │  8通道    │  4           │  1          │  0x01     │  四通道 │
 * │                                                               │
 * │  说明:                                                        │
 * │    - bitsWidth: 每个通道的相位位宽                            │
 * │    - bitsVal: 通道反相时的填充值                              │
 * │    - 通道数量影响采样速率和存储深度                            │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>模式切换</b>：切换自动/手动、滚动/正常、慢时基等模式</li>
 *   <li><b>采样配置</b>：设置采样类型（普通/峰值/平均/包络）</li>
 *   <li><b>通道控制</b>：配置通道使能、相位、耦合方式</li>
 *   <li><b>分段采样</b>：启用分段采样功能</li>
 *   <li><b>频率测量</b>：配置频率计数器源通道</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link FPGAReg} - 父类，提供寄存器基础功能</li>
 *   <li>{@link Scope} - 示波器主类，提供模式状态</li>
 *   <li>{@link Sample} - 采样管理类，提供采样类型</li>
 *   <li>{@link SegmentSample} - 分段采样管理类</li>
 *   <li>{@link Channel} - 通道类，提供通道参数</li>
 *   <li>{@link FreqCounter} - 频率计数器类</li>
 *   <li>{@link FPGACommand} - FPGA命令管理类，负责调用onCommand</li>
 * </ul>
 * 
 * <h3>寄存器地址</h3>
 * <p>寄存器ID: 0x00 (FPGA_SAMPLE_MODE) - 第一个FPGA寄存器</p>
 * 
 * @see FPGAReg
 * @see Scope
 * @see Sample
 * @see SegmentSample
 * @see Channel
 * @see FreqCounter
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class FPGAReg_SAMPLE_MODE extends FPGAReg {   // 继承FPGAReg基类，获得寄存器通用功能
    
    /**
     * 日志标签 - 用于调试输出
     * 
     * <p>定义日志输出的标签，方便在日志中识别采样模式寄存器的相关信息。</p>
     */
    private static final String TAG = "FPGAReg_SAMPLE_MODE";   // 日志标签：用于调试输出
    
    /**
     * 构造函数 - 初始化采样模式寄存器
     * 
     * <p>创建采样模式寄存器实例，配置寄存器ID和数据大小。</p>
     * 
     * <h4>初始化参数</h4>
     * <ul>
     *   <li><b>寄存器ID</b>: FPGA_SAMPLE_MODE (0x00)</li>
     *   <li><b>数据大小</b>: 4字节（32位数据）</li>
     * </ul>
     * 
     * <h4>寄存器地址说明</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器地址映射                         │
     * ├─────────────────────────────────────────┤
     * │  0x00 = FPGA_SAMPLE_MODE                │
     * │  功能: 综合采样模式控制                 │
     * │  大小: 4字节 (32位)                     │
     * │  访问: 只写 (由FPGA读取)                │
     * │                                         │
     * │  这是FPGA的第一个寄存器，               │
     * │  控制示波器所有核心采样参数             │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <p>在FPGACommand初始化时创建实例，存储在fpgaRegs数组中。
     * 当需要更新采样模式参数时，调用onCommand方法自动计算并设置值。</p>
     * 
     * @see FPGAReg#FPGAReg(int, int)
     * @see FPGACommand#initFpgaRegs()
     */
    public FPGAReg_SAMPLE_MODE() {   // 构造函数：初始化采样模式寄存器
        super(FPGAReg.FPGA_SAMPLE_MODE,4);   // 调用父类构造函数，设置寄存器ID为0x00，数据大小为4字节
    }   // 构造函数结束



    /**
     * 设置自动模式 - 配置示波器的自动测量模式
     * 
     * <p>此方法设置示波器的自动模式状态。自动模式下，示波器会自动调整
     * 时基、垂直刻度和触发参数，以获得最佳显示效果。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>自动模式标志，0=手动，1=自动</td></tr>
     * </table>
     * 
     * <h4>自动模式功能</h4>
     * <ul>
     *   <li>自动调整时基，使波形显示合适</li>
     *   <li>自动调整垂直刻度，使波形幅度适中</li>
     *   <li>自动设置触发电平，使波形稳定触发</li>
     *   <li>适合初学者和快速测量</li>
     * </ul>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位2: autoMode (1位)                    │
     * │                                         │
     * │  示例: val = 1 (自动模式)               │
     * │    存储位置: 位2                        │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 自动模式标志，0=手动模式，1=自动模式
     * @see Scope#isAuto()
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setAutoMode(int val){   // 方法：设置自动模式标志
        setVal(2,1,val);   // 设置寄存器值：从位2开始，长度1位，值为val（自动模式标志）
    }   // setAutoMode方法结束
    
    /**
     * 设置显示模式 - 配置滚动显示模式
     * 
     * <p>此方法设置示波器的显示模式。滚动模式下，波形从右向左滚动显示，
     * 类似纸带记录仪效果，适合观察长时间变化的信号。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>显示模式标志，0=正常，1=滚动</td></tr>
     * </table>
     * 
     * <h4>显示模式对比</h4>
     * <ul>
     *   <li><b>正常模式(0)</b>：触发后捕获，显示触发点附近波形</li>
     *   <li><b>滚动模式(1)</b>：实时滚动显示，波形从右向左移动</li>
     * </ul>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位3: dispMode (1位)                    │
     * │                                         │
     * │  示例: val = 1 (滚动模式)               │
     * │    存储位置: 位3                        │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 显示模式标志，0=正常模式，1=滚动模式
     * @see Scope#isInScrollMode()
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setDispMode(int val){   // 方法：设置显示模式标志
        setVal(3,1,val);   // 设置寄存器值：从位3开始，长度1位，值为val（显示模式标志）
    }   // setDispMode方法结束
    
    /**
     * 设置慢时基模式 - 配置慢时基缩放模式
     * 
     * <p>此方法设置示波器的慢时基模式。慢时基模式下，时基≥100ms/div，
     * 支持缩放功能，可以放大显示波形细节。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>慢时基标志，0=正常时基，1=慢时基</td></tr>
     * </table>
     * 
     * <h4>慢时基模式特点</h4>
     * <ul>
     *   <li>时基≥100ms/div</li>
     *   <li>支持缩放功能</li>
     *   <li>支持触发功能</li>
     *   <li>非滚动显示</li>
     * </ul>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位4: slowScale (1位)                   │
     * │                                         │
     * │  示例: val = 1 (慢时基模式)             │
     * │    存储位置: 位4                        │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 慢时基标志，0=正常时基，1=慢时基模式
     * @see Scope#isInSlowScaleMode()
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setSlowScale(int val){   // 方法：设置慢时基模式标志
        setVal(4,1,val);   // 设置寄存器值：从位4开始，长度1位，值为val（慢时基标志）
    }   // setSlowScale方法结束
    
    /**
     * 设置高刷新模式 - 配置高刷新率显示
     * 
     * <p>此方法设置示波器的高刷新模式。高刷新模式下，示波器会提高
     * 波形刷新率，使波形显示更加流畅。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>高刷新标志，0=正常刷新，1=高刷新</td></tr>
     * </table>
     * 
     * <h4>高刷新模式特点</h4>
     * <ul>
     *   <li>提高波形刷新率</li>
     *   <li>波形显示更流畅</li>
     *   <li>适合观察快速变化的信号</li>
     * </ul>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位5: highRefresh (1位)                 │
     * │                                         │
     * │  示例: val = 1 (高刷新模式)             │
     * │    存储位置: 位5                        │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 高刷新标志，0=正常刷新，1=高刷新模式
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setHighRefresh(int val){   // 方法：设置高刷新模式标志
        setVal(5,1,val);   // 设置寄存器值：从位5开始，长度1位，值为val（高刷新标志）
    }   // setHighRefresh方法结束
    
    /**
     * 设置通道相位 - 配置通道间的相位校准参数
     * 
     * <p>此方法设置通道间的相位校准参数。相位校准用于消除通道间的
     * 相位差，确保多通道测量时的时间精度。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>通道相位值，4位宽度</td></tr>
     * </table>
     * 
     * <h4>相位校准说明</h4>
     * <ul>
     *   <li>用于消除通道间的相位差</li>
     *   <li>确保多通道测量的时间精度</li>
     *   <li>每个通道占用bitsWidth位</li>
     *   <li>反相时填充bitsVal值</li>
     * </ul>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位6-9: chPhase (4位)                   │
     * │                                         │
     * │  示例: val = 0xF (通道反相)             │
     * │    存储位置: 位6-9                      │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 通道相位值，取值范围0-15
     * @see Channel#isInvert()
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setChPhase(int val){   // 方法：设置通道相位
        setVal(6,4,val);   // 设置寄存器值：从位6开始，长度4位，值为val（通道相位）
    }   // setChPhase方法结束
    
    /**
     * 设置单通道模式 - 配置通道数量模式
     * 
     * <p>此方法设置示波器的通道数量模式。通道数量影响采样速率和存储深度，
     * 不同模式下的采样性能不同。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>单通道模式值，3位宽度</td></tr>
     * </table>
     * 
     * <h4>通道模式值说明</h4>
     * <table border="1">
     *   <tr><th>val值</th><th>通道数量</th><th>说明</th></tr>
     *   <tr><td>0</td><td>4通道</td><td>标准4通道模式</td></tr>
     *   <tr><td>1</td><td>2通道</td><td>双通道模式</td></tr>
     *   <tr><td>2</td><td>单通道</td><td>单通道模式</td></tr>
     *   <tr><td>4</td><td>8通道</td><td>8通道模式</td></tr>
     * </table>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位10-12: singleCh (3位)                │
     * │                                         │
     * │  示例: val = 0 (4通道模式)              │
     * │    存储位置: 位10-12                    │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 单通道模式值，取值范围0-7
     * @see Scope#getChannelSampOnCnt(boolean)
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setSingleCh(int val){   // 方法：设置单通道模式
        setVal(10,3,val);   // 设置寄存器值：从位10开始，长度3位，值为val（单通道模式）
    }   // setSingleCh方法结束
    
    /**
     * 设置采样类型 - 配置示波器的采样方式
     * 
     * <p>此方法设置示波器的采样类型。不同的采样类型适用于不同的测量场景，
     * 影响波形捕获方式和显示效果。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>采样类型，2位宽度</td></tr>
     * </table>
     * 
     * <h4>采样类型值说明</h4>
     * <table border="1">
     *   <tr><th>val值</th><th>采样类型</th><th>说明</th></tr>
     *   <tr><td>0</td><td>SAMPLE_TYPE_NORMAL</td><td>普通采样</td></tr>
     *   <tr><td>1</td><td>SAMPLE_TYPE_PEAK</td><td>峰值检测</td></tr>
     *   <tr><td>2</td><td>SAMPLE_TYPE_AVERAGE</td><td>平均采样</td></tr>
     *   <tr><td>3</td><td>SAMPLE_TYPE_ENVEL</td><td>包络采样</td></tr>
     * </table>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位16-17: sampMode (2位)                │
     * │                                         │
     * │  示例: val = 2 (平均采样)               │
     * │    存储位置: 位16-17                    │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 采样类型，取值范围0-3
     * @see Sample#SAMPLE_TYPE_NORMAL
     * @see Sample#SAMPLE_TYPE_PEAK
     * @see Sample#SAMPLE_TYPE_AVERAGE
     * @see Sample#SAMPLE_TYPE_ENVEL
     * @see Sample#getSampleType()
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setSampMode(int val){   // 方法：设置采样类型
        setVal(16,2,val);   // 设置寄存器值：从位16开始，长度2位，值为val（采样类型）
    }   // setSampMode方法结束
    
    /**
     * 设置波形类型 - 配置波形显示类型
     * 
     * <p>此方法设置波形显示类型。目前固定设置为0，表示正常波形显示。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>波形类型，0=正常波形</td></tr>
     * </table>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位18: waveType (1位)                   │
     * │                                         │
     * │  示例: val = 0 (正常波形)               │
     * │    存储位置: 位18                       │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 波形类型，固定为0
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setWaveType(int val){   // 方法：设置波形类型
        setVal(18,1,val);   // 设置寄存器值：从位18开始，长度1位，值为val（波形类型）
    }   // setWaveType方法结束
    
    /**
     * 设置需要波形标志 - 配置是否需要波形数据
     * 
     * <p>此方法设置是否需要波形数据。设置为1时，FPGA会发送波形数据；
     * 设置为0时，FPGA不发送波形数据。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>需要波形标志，0=不需要，1=需要</td></tr>
     * </table>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位19: needWave (1位)                   │
     * │                                         │
     * │  示例: val = 1 (需要波形)               │
     * │    存储位置: 位19                       │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 需要波形标志，0=不需要，1=需要
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setNeedWave(int val){   // 方法：设置需要波形标志
        setVal(19,1,val);   // 设置寄存器值：从位19开始，长度1位，值为val（需要波形标志）
    }   // setNeedWave方法结束
    
    /**
     * 设置分段采样使能 - 配置分段采样功能
     * 
     * <p>此方法设置分段采样功能的启用状态。分段采样模式下，
     * 示波器可以捕获多个波形片段（帧），用于分析罕见事件。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>bEnable</td><td>boolean</td><td>分段采样使能，true=启用，false=禁用</td></tr>
     * </table>
     * 
     * <h4>分段采样功能</h4>
     * <ul>
     *   <li>捕获多个波形片段（帧）</li>
     *   <li>分析罕见事件或长时间信号变化</li>
     *   <li>支持单帧显示和拟合显示</li>
     *   <li>配合FPGAReg_SEGMENT_START和FPGAReg_SEGMENT_NUMS使用</li>
     * </ul>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位31: segment (1位)                    │
     * │                                         │
     * │  示例: bEnable = true (启用分段)        │
     * │    存储值: 1                            │
     * │    存储位置: 位31                       │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param bEnable 分段采样使能标志，true=启用，false=禁用
     * @see SegmentSample#isSegmentEnable()
     * @see FPGAReg_SEGMENT_START
     * @see FPGAReg_SEGMENT_NUMS
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setSegment(boolean bEnable){   // 方法：设置分段采样使能
        setVal(31,1,bEnable ? 1 : 0);   // 设置寄存器值：从位31开始，长度1位，值为1（启用）或0（禁用）
    }   // setSegment方法结束
    
    /**
     * 设置高分辨率标志 - 配置高分辨率采样
     * 
     * <p>此方法设置高分辨率采样标志。高分辨率模式下，采样精度更高，
     * 适合精密测量。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>高分辨率标志，0=正常分辨率，1=高分辨率</td></tr>
     * </table>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位20: highHr (1位)                     │
     * │                                         │
     * │  示例: val = 1 (高分辨率)               │
     * │    存储位置: 位20                       │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 高分辨率标志，0=正常分辨率，1=高分辨率
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setHighHr(int val){   // 方法：设置高分辨率标志
        setVal(20,1,val);   // 设置寄存器值：从位20开始，长度1位，值为val（高分辨率标志）
    }   // setHighHr方法结束
    
    /**
     * 设置耦合通道 - 配置通道的GND耦合状态
     * 
     * <p>此方法设置通道的GND耦合状态。当通道设置为GND耦合时，
     * 输入信号被断开，显示零电平，用于校准和参考。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>耦合通道值，4位宽度</td></tr>
     * </table>
     * 
     * <h4>耦合状态说明</h4>
     * <ul>
     *   <li>每个通道占用bitsWidth位</li>
     *   <li>GND耦合时填充bitsVal值</li>
     *   <li>非GND耦合时填充0</li>
     * </ul>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位21-24: coupCh (4位)                  │
     * │                                         │
     * │  示例: val = 0xF (GND耦合)              │
     * │    存储位置: 位21-24                    │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 耦合通道值，取值范围0-15
     * @see Channel#getCoupleType()
     * @see Channel#COUPLE_TYPE_GND
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setCoupCh(int val){   // 方法：设置耦合通道
        setVal(21,4,val);   // 设置寄存器值：从位21开始，长度4位，值为val（耦合通道）
    }   // setCoupCh方法结束
    
    /**
     * 设置频率源通道 - 配置频率计数器的源通道
     * 
     * <p>此方法设置频率计数器的源通道。频率计数器会测量指定通道的
     * 信号频率，显示在屏幕上。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>val</td><td>int</td><td>频率源通道索引，2位宽度</td></tr>
     * </table>
     * 
     * <h4>通道索引说明</h4>
     * <table border="1">
     *   <tr><th>val值</th><th>通道</th><th>说明</th></tr>
     *   <tr><td>0</td><td>CH1</td><td>通道1</td></tr>
     *   <tr><td>1</td><td>CH2</td><td>通道2</td></tr>
     *   <tr><td>2</td><td>CH3</td><td>通道3</td></tr>
     *   <tr><td>3</td><td>CH4</td><td>通道4</td></tr>
     * </table>
     * 
     * <h4>数据存储位置</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  寄存器位映射                           │
     * ├─────────────────────────────────────────┤
     * │  位25-26: freSourcCh (2位)              │
     * │                                         │
     * │  示例: val = 0 (CH1频率源)              │
     * │    存储位置: 位25-26                    │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param val 频率源通道索引，取值范围0-3
     * @see FreqCounter#getChIdx()
     * @see FreqCounter#isFreqCounterEnable()
     * @see FPGAReg#setVal(int, int, int)
     */
    public void setFreSourcCh(int val){   // 方法：设置频率源通道
        setVal(25,2,val);   // 设置寄存器值：从位25开始，长度2位，值为val（频率源通道）
    }   // setFreSourcCh方法结束

    /**
     * 命令回调方法 - 自动计算并设置所有采样模式参数
     * 
     * <p>此方法在每次需要更新FPGA采样模式参数时被调用，
     * 根据示波器状态自动计算并设置所有采样模式相关参数。</p>
     * 
     * <h4>计算逻辑详解</h4>
     * <pre>
     * ┌───────────────────────────────────────────────────────────────┐
     * │                    onCommand执行流程                          │
     * ├───────────────────────────────────────────────────────────────┤
     * │                                                               │
     * │  Step 1: 获取示波器和通道状态                                 │
     * │    └─ Scope.getInstance()                                     │
     * │    └─ scope.getChannelSampOnCnt()                             │
     * │    └─ FreqCounter.getInstance()                               │
     * │                                                               │
     * │  Step 2: 设置基础模式参数                                     │
     * │    ├─ setAutoMode(): 根据isAuto()设置                         │
     * │    ├─ setDispMode(): 根据isInScrollMode()设置                 │
     * │    ├─ setSlowScale(): 根据isInSlowScaleMode()设置             │
     * │    ├─ setHighRefresh(): 固定设置为0                           │
     * │    └─ 频率计数器禁用标志: 位15                                │
     * │                                                               │
     * │  Step 3: 根据通道数量设置参数                                 │
     * │    ├─ 2通道: cnt=1, bitsWidth=4, bitsVal=0xF, singleCh=2     │
     * │    ├─ 4通道: cnt=2, bitsWidth=2, bitsVal=0x03, singleCh=0    │
     * │    ├─ 8通道: cnt=4, bitsWidth=1, bitsVal=0x01, singleCh=4    │
     * │                                                               │
     * │  Step 4: 设置通道使能和相位参数                               │
     * │    ├─ 位27-30: 通道使能标志                                   │
     * │    ├─ 位6-9: 通道相位（反相标志）                             │
     * │    ├─ 位21-24: GND耦合标志                                    │
     * │    └─ 特殊处理: 1M阻抗非1档时反相翻转                         │
     * │                                                               │
     * │  Step 5: 设置采样和波形参数                                   │
     * │    ├─ setSampMode(): 根据Sample.getSampleType()设置           │
     * │    ├─ setWaveType(): 固定设置为0                              │
     * │    ├─ setNeedWave(): 固定设置为1                              │
     * │    └─ setSegment(): 根据SegmentSample.isSegmentEnable()设置   │
     * │                                                               │
     * │  Step 6: 设置频率计数器参数                                   │
     * │    ├─ 位15: 频率计数器禁用标志                                │
     * │    └─ 位25-26: 频率源通道索引                                 │
     * │                                                               │
     * └───────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>通道数量与参数映射</h4>
     * <table border="1">
     *   <tr><th>通道数</th><th>cnt</th><th>bitsWidth</th><th>bitsVal</th><th>singleCh</th></tr>
     *   <tr><td>2</td><td>1</td><td>4</td><td>0xF</td><td>2</td></tr>
     *   <tr><td>4</td><td>2</td><td>2</td><td>0x03</td><td>0</td></tr>
     *   <tr><td>8</td><td>4</td><td>1</td><td>0x01</td><td>4</td></tr>
     * </table>
     * 
     * <h4>反相处理逻辑</h4>
     * <pre>
     * ┌─────────────────────────────────────────────────────────────┐
     * │              通道反相处理逻辑                               │
     * ├─────────────────────────────────────────────────────────────┤
     * │                                                             │
     * │  1. 获取通道反相状态: channel.isInvert()                    │
     * │                                                             │
     * │  2. 特殊硬件判断:                                           │
     * │     if (!MHO38V1 && !MHO28V1) {                             │
     * │       检查阻抗类型和档位                                    │
     * │       if (1M阻抗 && 非1档) {                                │
     * │         反相状态翻转                                        │
     * │       }                                                     │
     * │     }                                                       │
     * │                                                             │
     * │  3. 设置相位值:                                             │
     * │     if (反相) {                                             │
     * │       val = bitsVal (填充反相标志)                          │
     * │     } else {                                                │
     * │       val = 0                                               │
     * │     }                                                       │
     * │                                                             │
     * └─────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>示波器模式切换时（自动/手动、滚动/正常、慢时基）</li>
     *   <li>通道状态改变时（开启/关闭、反相、耦合）</li>
     *   <li>采样类型改变时（普通/峰值/平均/包络）</li>
     *   <li>分段采样启用/禁用时</li>
     *   <li>频率计数器配置改变时</li>
     * </ul>
     * 
     * @see Scope#getInstance()
     * @see Scope#isAuto()
     * @see Scope#isInScrollMode()
     * @see Scope#isInSlowScaleMode()
     * @see Scope#getChannelSampOnCnt(boolean)
     * @see Scope#isChannelInSample(int, boolean)
     * @see FreqCounter#getInstance()
     * @see FreqCounter#isFreqCounterEnable()
     * @see FreqCounter#getChIdx()
     * @see Sample#getInstance()
     * @see Sample#getSampleType()
     * @see SegmentSample#getInstance()
     * @see SegmentSample#isSegmentEnable()
     * @see Channel#isInvert()
     * @see Channel#getCoupleType()
     * @see Channel#getResistanceType()
     * @see Channel#getVScaleVal()
     * @see Channel#getProbeRate()
     * @see HardwareProduct#isMHO38V1()
     * @see HardwareProduct#isMHO28V1()
     * @see CabteRegister#getRatioIdx(int, double)
     * @see HW#RATIO_DANG_1
     * @see HwConfig#getInstance()
     * @see HwConfig#getAdcMaxChNums()
     * @see ChannelFactory#getDynamicChannel(int)
     * @see ChannelFactory#CH_CNT
     * @see FPGACommand#beginChIdx(int)
     * @see FPGACommand#endChIdx(int)
     * @see FPGAReg#setVal(int, int, int)
     */
    @Override   // 重写父类FPGAReg的onCommand方法
    public void onCommand() {   // 命令回调方法：自动计算并设置所有采样模式参数
        Scope scope = Scope.getInstance();   // 获取示波器实例（单例模式）
        Channel channel = null;   // 通道对象，用于获取通道参数
        int val = 0;   // 临时变量，用于存储计算值
        int startBit = 0;   // 起始位位置，用于多通道参数设置
        int bitsWidth = 2;   // 位宽度，根据通道数量动态调整
        int bitsVal = 0x03;   // 位填充值，根据通道数量动态调整
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true));   // 获取参与采样的通道数量

        FreqCounter freqCounter = FreqCounter.getInstance();   // 获取频率计数器实例（单例模式）

        int freVal = 0;   // 频率值临时变量
        int freIdx = freqCounter.getChIdx();   // 获取频率计数器的源通道索引

        freIdx = freIdx % 4;   // 将通道索引限制在0-3范围内（4个通道）
        int nums = HwConfig.getInstance().getAdcMaxChNums();   // 获取ADC最大通道数量


        setVal(15,1, freqCounter.isFreqCounterEnable() ? 0 : 1);   // 设置频率计数器禁用标志：位15，启用时为0，禁用时为1
        setVal(25,2, freIdx );   // 设置频率源通道索引：位25-26，值为freIdx
        setAutoMode(scope.isAuto() ? 1 : 0);   // 设置自动模式：自动模式时为1，手动模式时为0
        setDispMode(scope.isInScrollMode() ? 1 : 0);   // 设置显示模式：滚动模式时为1，正常模式时为0
        setSlowScale(scope.isInSlowScaleMode() ? 1 : 0);   // 设置慢时基模式：慢时基模式时为1，正常时基时为0
        //setHighRefresh(scope.isInHighRefresh() ? 1 : 0);   // 已注释：原高刷新模式设置逻辑
        setHighRefresh(0);   // 设置高刷新模式：固定设置为0（禁用高刷新）

        switch(cnt){   // 根据通道数量设置参数
            case 2:   // 2通道模式
                cnt = 1;   // 设置计数器为1（实际处理通道数）
                bitsWidth = 4;   // 设置位宽度为4（每个通道占用4位）
                bitsVal = 0xF;   // 设置位填充值为0xF（反相标志）
                setSingleCh(2);   // 设置单通道模式值为2（表示2通道）
                freVal = (freIdx / nums) * nums;   // 计算频率值偏移
                break;   // 跳出case 2
            
            case 4:   // 4通道模式
                cnt = 2;   // 设置计数器为2（实际处理通道数）
                bitsWidth = 2;   // 设置位宽度为2（每个通道占用2位）
                bitsVal = 0x03;   // 设置位填充值为0x03（反相标志）
                setSingleCh(0);   // 设置单通道模式值为0（表示4通道）
                break;   // 跳出case 4
            
            case 8:   // 8通道模式
            default:   // 默认情况（8通道或更多）
                cnt = 4;   // 设置计数器为4（实际处理通道数）
                bitsWidth = 1;   // 设置位宽度为1（每个通道占用1位）
                bitsVal = 0x01;   // 设置位填充值为0x01（反相标志）
                setSingleCh(4);   // 设置单通道模式值为4（表示8通道）
                break;   // 跳出case 8/default
        }   // switch结束
        
        boolean [] en_array = new boolean[ChannelFactory.CH_CNT];   // 创建通道使能数组，长度为通道总数
        int beginIdx = FPGACommand.beginChIdx(getFpgaIdx());   // 获取当前FPGA的起始通道索引
        int endIdx = FPGACommand.endChIdx(getFpgaIdx());   // 获取当前FPGA的结束通道索引
        
        for(int i=beginIdx;i<endIdx;i++){   // 循环：遍历当前FPGA的所有通道
            boolean b = scope.isChannelInSample(i,scope.isRun(true));   // 判断：通道i是否参与采样
            setVal(27 + i - beginIdx,1,b ? 1:0);   // 设置通道使能标志：位27-30，值为1（启用）或0（禁用）
        }   // for循环结束
        
        scope.getChannelSampOnCnt(scope.isRun(true),en_array);   // 获取所有通道的采样使能状态，填充en_array
        
        for(int i=beginIdx;i<endIdx;i++){   // 循环：遍历当前FPGA的所有通道，处理通道参数
            if(en_array[i]){   // 判断：通道i是否启用采样
                channel = ChannelFactory.getDynamicChannel(i);   // 获取通道i的实例
                
                if(channel != null) {   // 判断：通道实例是否有效
                    boolean bInvert = channel.isInvert();   // 获取通道反相状态
                    
                    if(!(HardwareProduct.isMHO38V1()   // 判断：是否非MHO38V1型号
                            || HardwareProduct.isMHO28V1())){   // 判断：是否非MHO28V1型号
                        // 特殊硬件处理：非MHO38V1和MHO28V1型号
                        int dang = CabteRegister.getRatioIdx(Channel.RESISTANCE_1M,channel.getVScaleVal()/channel.getProbeRate());   // 计算档位索引
                        
                        if (channel.getResistanceType() == Channel.RESISTANCE_1M   // 判断：是否为1M阻抗
                                && dang != HW.RATIO_DANG_1) {   // 判断：档位是否非1档
                            // 1M阻抗且非1档时，反相状态需要翻转
                            bInvert = !bInvert;   // 翻转反相状态
                        }   // 档位判断结束
                    }   // 硬件型号判断结束
                    
                    val = bInvert ? bitsVal : 0;   // 计算相位值：反相时填充bitsVal，否则为0
                    setVal(6 + startBit, bitsWidth, val);   // 设置通道相位：位6开始，长度bitsWidth，值为val
                    
                    val = channel.getCoupleType() == Channel.COUPLE_TYPE_GND ? bitsVal : 0;   // 计算耦合值：GND耦合时填充bitsVal，否则为0
                    setVal(21 + startBit, bitsWidth, val);   // 设置耦合通道：位21开始，长度bitsWidth，值为val
                    
                    startBit += bitsWidth;   // 更新起始位位置：增加bitsWidth

                    {   // 频率计数器源通道处理
                        if (freIdx == (i-beginIdx)) {   // 判断：当前通道是否为频率计数器源通道
                            setVal(25, 2, freVal);   // 设置频率源通道：位25-26，值为freVal
                        }   // 频率源判断结束
                        freVal += bitsWidth;   // 更新频率值偏移：增加bitsWidth
                    }   // 频率计数器处理块结束
                }   // 通道实例判断结束
                
                cnt--;   // 减少计数器：已处理一个通道
            }   // 通道使能判断结束
            
            if(cnt <= 0)   // 判断：计数器是否已处理完所有通道
                break;   // 跳出循环：所有通道已处理完毕
        }   // for循环结束

        setSampMode(Sample.getInstance().getSampleType());   // 设置采样类型：根据Sample管理类获取当前采样类型
        setWaveType(0);   // 设置波形类型：固定设置为0（正常波形）
        setNeedWave(1);   // 设置需要波形标志：固定设置为1（需要波形数据）
        setSegment(SegmentSample.getInstance().isSegmentEnable());   // 设置分段采样使能：根据SegmentSample管理类获取分段采样状态
    }   // onCommand方法结束
}   // FPGAReg_SAMPLE_MODE类结束