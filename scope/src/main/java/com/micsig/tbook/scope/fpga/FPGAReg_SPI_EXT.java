package com.micsig.tbook.scope.fpga;                                                     // 包声明：FPGA模块

import com.micsig.tbook.hardware.Hardware;                                               // 导入Hardware类：硬件管理类
import com.micsig.tbook.hardware.HardwareProduct;                                        // 导入HardwareProduct类：硬件产品信息类

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║        FPGAReg_SPI_EXT - 示波器FPGA SPI扩展寄存器类                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   FPGA模块的SPI扩展寄存器类，位于fpga包下，                                    ║
 * ║   继承自FPGAReg基类，负责配置FPGA向外部SPI设备写入数据。                        ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 配置SPI写入的目标设备类型                                               ║
 * ║   2. 设置SPI写入的数据内容                                                   ║
 * ║   3. 设置SPI写入的数据长度                                                   ║
 * ║   4. 提供ADC芯片配置功能                                                     ║
 * ║   5. 提供通道增益、UART等设备的配置功能                                      ║
 * ║   6. 继承FPGAReg基类的寄存器操作功能                                          ║
 * ║                                                                              ║
 * ║ 【架构位置】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        FPGA寄存器架构                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │    FPGAReg      │────────▶│FPGAReg_SPI_     │                   │ ║
 * ║   │   │   (寄存器基类)   │         │    EXT          │                   │ ║
 * ║   │   └─────────────────┘         └────────┬────────┘                   │ ║
 * ║   │          │                             │                            │ ║
 * ║   │          ▼                             ▼                            │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │  寄存器操作方法  │         │  FPGACommand    │                   │ ║
 * ║   │   │  (setVal/getVal) │         │   (命令管理)     │                   │ ║
 * ║   │   └─────────────────┘         └─────────────────┘                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【寄存器数据结构】                                                           ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        寄存器数据格式                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   寄存器ID：FPGA_SPI_EXT (0xAB)                                       │ ║
 * ║   │   数据长度：40字节（默认）或自定义长度                                  │ ║
 * ║   │   寄存器类型：写寄存器（用于发送SPI写入命令）                            │ ║
 * ║   │                                                                      │ ║
 * ║   │   【字0位定义（控制字）】                                               │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  位范围   │  位宽  │  参数名称      │  说明                  │   │ ║
 * ║   │   ├─────────────────────────────────────────────────────────────┤   │ ║
 * ║   │   │    0-7    │   8   │  reserved      │  保留位（设置为0）     │   │ ║
 * ║   │   │   8-15    │   8   │  spiType       │  SPI设备类型           │   │ ║
 * ║   │   │  16-19    │   4   │  powerDown     │  AD掉电控制            │   │ ║
 * ║   │   │  20-23    │   4   │  powerDownQ    │  Q路AD掉电控制         │   │ ║
 * ║   │   │  24-29    │   6   │  dataLength    │  数据长度              │   │ ║
 * ║   │   │  30-31    │   2   │  byteValid     │  有效字节数            │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【字1位定义（数据字）】                                               │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  MHO68V2 + B12DJ3200NBB模式：                                  │   │ ║
 * ║   │   │  - 字1：ADC地址                                                │   │ ║
 * ║   │   │  - 字2：ADC数据值                                              │   │ ║
 * ║   │   │                                                                 │   │ ║
 * ║   │   │  其他模式：                                                     │   │ ║
 * ║   │   │  - 位0-15：ADC数据值（vmask掩码）                               │   │ ║
 * ║   │   │  - 位16-31：ADC地址（voffset偏移）                              │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【SPI外部设备类型】                                                          ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        SPI设备类型定义                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【设备类型常量】                                                     │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  常量名称        │  值    │  设备类型      │  说明              │   │ ║
 * ║   │   ├─────────────────────────────────────────────────────────────┤   │ ║
 * ║   │   │  SPI_EXT_PLL      │  1<<0  │  PLL芯片       │  锁相环芯片        │   │ ║
 * ║   │   │  SPI_EXT_AD       │  1<<1  │  AD芯片        │  ADC模数转换芯片   │   │ ║
 * ║   │   │  SPI_EXT_CHCTRL   │  1<<2  │  通道控制芯片  │  通道控制芯片      │   │ ║
 * ║   │   │  SPI_EXT_CHGAIN   │  1<<3  │  通道增益芯片  │  通道增益芯片      │   │ ║
 * ║   │   │  SPI_EXT_UART     │  1<<4  │  UART芯片      │  串口通信芯片      │   │ ║
 * ║   │   │  SPI_EXT_CHPD     │  1<<5  │  通道PD芯片    │  通道掉电控制芯片  │   │ ║
 * ║   │   │  SPI_EXT_CLKINOUT │  1<<6  │  时钟输入输出  │  时钟输入输出控制  │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【SPI写入流程】                                                              ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        SPI写入工作流程                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   需要向外部SPI设备写入数据                                           │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   FPGACommand判断需要写入数据                                         │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   FPGAReg_SPI_EXT reg = new FPGAReg_SPI_EXT()                        │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   reg.setType(SPI_EXT_AD)  // 设置设备类型                            │ ║
 * ║   │   reg.setAdData(addr, val)  // 设置数据                               │ ║
 * ║   │   reg.setDataLength(len)  // 设置数据长度                             │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   FPGA发送SPI写入命令                                                 │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   FPGA通过SPI总线写入数据                                             │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   SPI设备接收并处理数据                                               │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 继承模式：继承FPGAReg基类，复用寄存器操作功能                           ║
 * ║   - 常量模式：使用静态常量定义SPI设备类型                                  ║
 * ║   - 条件配置：根据硬件产品类型配置不同参数                                  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - FPGAReg: FPGA寄存器基类                                               ║
 * ║   - FPGACommand: FPGA命令管理类，负责发送SPI写入命令                      ║
 * ║   - FPGAReg_SPI_Read: SPI读取寄存器类                                     ║
 * ║   - Hardware: 硬件管理类                                                  ║
 * ║   - HardwareProduct: 硬件产品信息类                                       ║
 * ║   - ADC: ADC芯片管理类                                                    ║
 * ║                                                                              ║
 * ║ 【寄存器关系】                                                               ║
 * ║   - FPGA_SPI_EXT (0xAB): SPI扩展寄存器（本类）                            ║
 * ║   - FPGA_SPI_R (0xAC): SPI读取寄存器                                      ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. ADC配置：向ADC芯片写入配置数据                                        ║
 * ║   2. 通道增益设置：向通道增益芯片写入增益值                                ║
 * ║   3. UART数据写入：向UART芯片写入数据                                      ║
 * ║   ║   4. 通道掉电控制：向通道PD芯片写入掉电控制                            ║
 * ║   5. 时钟配置：向时钟输入输出芯片写入配置                                  ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ║ 【创建日期】 2018-4-8                                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器FPGA SPI扩展寄存器类
 * 继承自FPGAReg基类，负责配置FPGA向外部SPI设备写入数据
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>配置SPI写入的目标设备类型</li>
 *   <li>设置SPI写入的数据内容</li>
 *   <li>设置SPI写入的数据长度</li>
 *   <li>提供ADC芯片配置功能</li>
 *   <li>提供通道增益、UART等设备的配置功能</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b></p>
 * <ul>
 *   <li>寄存器ID：FPGA_SPI_EXT (0xAB)</li>
 *   <li>数据长度：40字节（默认）或自定义长度</li>
 *   <li>寄存器类型：写寄存器（用于发送SPI写入命令）</li>
 * </ul>
 *
 * <p><b>SPI设备类型：</b></p>
 * <ul>
 *   <li>SPI_EXT_PLL：PLL芯片（锁相环）</li>
 *   <li>SPI_EXT_AD：AD芯片（ADC）</li>
 *   <li>SPI_EXT_CHCTRL：通道控制芯片</li>
 *   <li>SPI_EXT_CHGAIN：通道增益芯片</li>
 *   <li>SPI_EXT_UART：UART芯片</li>
 *   <li>SPI_EXT_CHPD：通道PD芯片</li>
 *   <li>SPI_EXT_CLKINOUT：时钟输入输出</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 向ADC芯片写入配置数据
 * FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) fpgaCmd.getFPGAReg(fpgaIdx, FPGAReg.FPGA_SPI_EXT);
 * reg.setType(FPGAReg_SPI_EXT.SPI_EXT_AD);  // 设置类型为ADC
 * reg.setAdData(addr, val);                  // 设置ADC地址和数据值
 * fpgaCmd.sendCommand(reg);                  // 发送SPI写入命令
 *
 * // 向通道增益芯片写入增益值
 * FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) fpgaCmd.getFPGAReg(fpgaIdx, FPGAReg.FPGA_SPI_EXT);
 * reg.setType(FPGAReg_SPI_EXT.SPI_EXT_CHGAIN);  // 设置类型为通道增益
 * reg.setDataLength(4);                         // 设置数据长度为4字节
 * fpgaCmd.sendCommand(reg);                     // 发送SPI写入命令
 *
 * // 向UART芯片写入数据
 * FPGAReg_SPI_EXT reg = new FPGAReg_SPI_EXT(bytes.length + 4);  // 创建寄存器，指定数据长度
 * reg.setType(FPGAReg_SPI_EXT.SPI_EXT_UART);  // 设置类型为UART
 * reg.setDataLength(nums);                    // 设置数据长度
 * fpgaCmd.sendCommand(reg);                   // 发送SPI写入命令
 *
 * // 设置ADC模式
 * FPGAReg_SPI_EXT reg = (FPGAReg_SPI_EXT) fpgaCmd.getFPGAReg(fpgaIdx, FPGAReg.FPGA_SPI_EXT);
 * reg.setType(FPGAReg_SPI_EXT.SPI_EXT_AD);  // 设置类型为ADC
 * reg.setADMode(val, v);                    // 设置ADC模式
 * fpgaCmd.sendCommand(reg);                 // 发送SPI写入命令
 * </pre>
 *
 * @see FPGAReg
 * @see FPGAReg_SPI_Read
 * @see FPGACommand
 * @see Hardware
 * @see HardwareProduct
 */
public class FPGAReg_SPI_EXT extends FPGAReg{                                            // 类声明：FPGA SPI扩展寄存器类，继承FPGAReg

    // ═══════════════════════════════════════════════════════════════════════════════
    // 静态常量 - SPI外部设备类型
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * SPI外部PLL芯片类型常量
     * 用于设置SPI写入的目标为PLL芯片
     *
     * <p><b>PLL芯片说明：</b></p>
     * <ul>
     *   <li>PLL（Phase Locked Loop）锁相环芯片</li>
     *   <li>用于时钟同步和时钟生成</li>
     *   <li>提供高精度时钟信号</li>
     *   <li>用于示波器采样时钟生成</li>
     * </ul>
     */
    public final static int SPI_EXT_PLL = (1<<0);                                        // 静态常量：SPI外部PLL芯片类型，值为1（位0）

    /**
     * SPI外部AD芯片类型常量
     * 用于设置SPI写入的目标为ADC芯片
     *
     * <p><b>AD芯片说明：</b></p>
     * <ul>
     *   <li>AD（Analog to Digital）模数转换芯片</li>
     *   <li>将模拟信号转换为数字信号</li>
     *   <li>示波器的核心采样器件</li>
     *   <li>决定采样率和垂直分辨率</li>
     * </ul>
     */
    public final static int SPI_EXT_AD = (1<<1);                                         // 静态常量：SPI外部AD芯片类型，值为2（位1）

    /**
     * SPI外部通道控制芯片类型常量
     * 用于设置SPI写入的目标为通道控制芯片
     *
     * <p><b>通道控制芯片说明：</b></p>
     * <ul>
     *   <li>控制通道的开关和耦合方式</li>
     *   <li>管理通道的输入阻抗</li>
     *   <li>控制通道的带宽限制</li>
     * </ul>
     */
    public final static int SPI_EXT_CHCTRL = (1<<2);                                     // 静态常量：SPI外部通道控制芯片类型，值为4（位2）

    /**
     * SPI外部通道增益芯片类型常量
     * 用于设置SPI写入的目标为通道增益芯片
     *
     * <p><b>通道增益芯片说明：</b></p>
     * <ul>
     *   <li>控制通道的增益设置</li>
     *   <li>实现垂直档位的调节</li>
     *   <li>提供衰减和放大功能</li>
     * </ul>
     */
    public final static int SPI_EXT_CHGAIN = (1<<3);                                     // 静态常量：SPI外部通道增益芯片类型，值为8（位3）

    /**
     * SPI外部UART芯片类型常量
     * 用于设置SPI写入的目标为UART芯片
     *
     * <p><b>UART芯片说明：</b></p>
     * <ul>
     *   <li>UART（Universal Asynchronous Receiver Transmitter）串口通信芯片</li>
     *   <li>用于外部串口数据传输</li>
     *   <li>支持串行总线触发功能</li>
     * </ul>
     */
    public final static int SPI_EXT_UART = (1<<4);                                       // 静态常量：SPI外部UART芯片类型，值为16（位4）

    /**
     * SPI外部通道PD芯片类型常量
     * 用于设置SPI写入的目标为通道掉电控制芯片
     *
     * <p><b>通道PD芯片说明：</b></p>
     * <ul>
     *   <li>控制通道的掉电状态</li>
     *   <li>用于通道的电源管理</li>
     *   <li>实现通道的启用/禁用控制</li>
     * </ul>
     */
    public final static int SPI_EXT_CHPD = (1<<5);                                       // 静态常量：SPI外部通道PD芯片类型，值为32（位5）

    /**
     * SPI外部时钟输入输出芯片类型常量
     * 用于设置SPI写入的目标为时钟输入输出控制芯片
     *
     * <p><b>时钟输入输出芯片说明：</b></p>
     * <ul>
     *   <li>控制外部时钟输入输出</li>
     *   <li>用于时钟同步功能</li>
     *   <li>支持外部参考时钟输入</li>
     * </ul>
     */
    public final static int SPI_EXT_CLKINOUT = (1<<6);                                   // 静态常量：SPI外部时钟输入输出芯片类型，值为64（位6）

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - ADC数据格式配置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * ADC地址偏移量
     * 用于设置ADC数据字中地址的位置
     *
     * <p><b>偏移量说明：</b></p>
     * <ul>
     *   <li>默认值：16（其他硬件产品）</li>
     *   <li>MHO68V2：8（MHO68V2硬件产品）</li>
     *   <li>用于setAdData方法中地址的位置计算</li>
     * </ul>
     *
     * <p><b>数据格式：</b></p>
     * <pre>
     * 其他硬件产品：
     * - 字1 = (addr << 16) | (val & 0xFFFF)
     * - 地址在位16-31，数据在位0-15
     *
     * MHO68V2硬件产品：
     * - 字1 = addr
     * - 字2 = val
     * - 地址和数据分开存储
     * </pre>
     */
    int voffset = 16;                                                                    // 成员变量：ADC地址偏移量，默认值为16

    /**
     * ADC数据掩码
     * 用于设置ADC数据字中数据的掩码
     *
     * <p><b>掩码说明：</b></p>
     * <ul>
     *   <li>默认值：0xFFFF（其他硬件产品，16位数据）</li>
     *   <li>MHO68V2：0xFF（MHO68V2硬件产品，8位数据）</li>
     *   <li>用于setAdData方法中数据的掩码</li>
     * </ul>
     *
     * <p><b>数据格式：</b></p>
     * <pre>
     * 其他硬件产品：
     * - 数据掩码为0xFFFF，表示16位数据
     * - val & 0xFFFF 取低16位数据
     *
     * MHO68V2硬件产品：
     * - 数据掩码为0xFF，表示8位数据
     * - val & 0xFF 取低8位数据
     * </pre>
     */
    int vmask = 0xFFFF;                                                                  // 成员变量：ADC数据掩码，默认值为0xFFFF（16位）

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 默认构造函数
     * 初始化FPGA SPI扩展寄存器，数据长度为40字节
     *
     * <p><b>初始化参数：</b></p>
     * <ul>
     *   <li>寄存器ID：FPGA_SPI_EXT (0xAB)</li>
     *   <li>数据长度：40字节</li>
     * </ul>
     *
     * <p><b>硬件产品适配：</b></p>
     * <ul>
     *   <li>其他硬件产品：voffset=16, vmask=0xFFFF</li>
     *   <li>MHO68V2硬件产品：voffset=8, vmask=0xFF</li>
     * </ul>
     *
     * <p><b>使用说明：</b></p>
     * <ul>
     *   <li>创建寄存器对象后需要调用setType等方法设置参数</li>
     *   <li>然后通过FPGACommand发送命令到FPGA</li>
     *   <li>FPGA执行SPI写入操作</li>
     * </ul>
     */
    public FPGAReg_SPI_EXT() {                                                           // 构造方法：初始化FPGA SPI扩展寄存器，数据长度40字节
        super(FPGA_SPI_EXT, 40);                                                         // 调用父类构造函数，设置寄存器ID FPGA_SPI_EXT和数据长度40字节

        if(HardwareProduct.isMHO68V2()){                                                 // 条件判断：如果是MHO68V2硬件产品
            voffset = 8;                                                                 // 设置ADC地址偏移量为8（MHO68V2专用）
            vmask = 0xFF;                                                                // 设置ADC数据掩码为0xFF（MHO68V2专用，8位数据）
        }                                                                                // 条件判断结束
    }                                                                                    // 构造方法结束

    /**
     * 自定义长度构造函数
     * 初始化FPGA SPI扩展寄存器，指定数据长度
     *
     * <p><b>初始化参数：</b></p>
     * <ul>
     *   <li>寄存器ID：FPGA_SPI_EXT (0xAB)</li>
     *   <li>数据长度：由参数len指定</li>
     * </ul>
     *
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>UART数据写入：数据长度取决于数据缓冲区大小</li>
     *   <li>其他需要自定义数据长度的场景</li>
     * </ul>
     *
     * @param len 数据长度（字节数）
     */
    public FPGAReg_SPI_EXT(int len){                                                     // 构造方法：初始化FPGA SPI扩展寄存器，指定数据长度
        super(FPGA_SPI_EXT,len);                                                         // 调用父类构造函数，设置寄存器ID FPGA_SPI_EXT和自定义数据长度len
    }                                                                                    // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - SPI写入参数设置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置SPI写入类型
     * 设置SPI写入的目标设备类型
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置SPI写入的目标设备类型</li>
     *   <li>设备类型由静态常量定义</li>
     *   <li>支持PLL、AD、通道控制、通道增益、UART、通道PD、时钟输入输出等设备</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位0-7：设置为0（保留位）
     * 位8-15：设置为设备类型值t
     * </pre>
     *
     * @param t SPI设备类型（使用静态常量，如SPI_EXT_AD）
     */
    public void setType(int t){                                                          // 公有方法：设置SPI写入类型
        setVal(0,0);                                                                     // 清除字0的位0-7，设置为0（保留位）
        setVal(0,0,8,t);                                                                 // 设置字0的位8-15为设备类型值t
    }                                                                                    // 方法结束

    /**
     * 设置通道
     * 设置SPI写入的目标通道
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置SPI写入的目标通道索引</li>
     *   <li>用于多通道设备的通道选择</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位8-11：设置为通道索引值val（4位，范围0-15）
     * </pre>
     *
     * @param val 通道索引（范围0-15）
     */
    public void setCh(int val){                                                          // 公有方法：设置通道
        setVal(0,8,4,val);                                                               // 设置字0的位8-11为通道索引值val（4位）
    }                                                                                    // 方法结束

    /**
     * 设置VGA索引
     * 设置可变增益放大器索引
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置VGA（Variable Gain Amplifier）索引</li>
     *   <li>用于选择不同的增益放大器</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位12：设置为VGA索引值idx（1位，范围0-1）
     * </pre>
     *
     * @param idx VGA索引（范围0-1）
     */
    public void setVGAIdx(int idx){                                                      // 公有方法：设置VGA索引
        setVal(0,12,1,idx);                                                              // 设置字0的位12为VGA索引值idx（1位）
    }                                                                                    // 方法结束

    /**
     * 设置写入AD
     * 设置ADC写入操作参数
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置ADC写入操作参数</li>
     *   <li>用于ADC芯片的写入配置</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位8-11：设置为写入AD参数值val（4位，范围0-15）
     * </pre>
     *
     * <p><b>注释说明：</b></p>
     * <ul>
     *   <li>参数值范围：0, 1, 2, 3</li>
     *   <li>用于不同的ADC写入操作</li>
     * </ul>
     *
     * @param val 写入AD参数（范围0-15，常用值0,1,2,3）
     */
    // 0 ,1,2,3                                                                           // 注释：参数值范围0, 1, 2, 3
    public void setWriteAd(int val){                                                     // 公有方法：设置写入AD
        setVal(0,8,4,val);                                                               // 设置字0的位8-11为写入AD参数值val（4位）
    }                                                                                    // 方法结束

    /**
     * 设置校准AD
     * 设置ADC校准参数
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置ADC校准参数</li>
     *   <li>用于ADC芯片的校准配置</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位12-15：设置为校准AD参数值val（4位，范围0-15）
     * </pre>
     *
     * @param val 校准AD参数（范围0-15）
     */
    public void setCalibrationAd(int val){                                               // 公有方法：设置校准AD
        setVal(0,12,4, val);                                                             // 设置字0的位12-15为校准AD参数值val（4位）
    }                                                                                    // 方法结束

    /**
     * AD同步
     * 设置ADC同步标志
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置ADC同步标志</li>
     *   <li>用于ADC芯片的同步操作</li>
     *   <li>触发ADC同步更新</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位16：设置为1（同步标志）
     * </pre>
     */
    public void adSync(){                                                                // 公有方法：AD同步

        setVal(0,16,1,1);                                                                // 设置字0的位16为1，表示ADC同步标志
    }                                                                                    // 方法结束

    /**
     * 设置AD掉电
     * 设置ADC掉电控制参数
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置ADC掉电控制参数</li>
     *   <li>用于ADC芯片的掉电控制</li>
     *   <li>控制ADC通道的电源状态</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位16-19：设置为AD掉电参数值val（4位，范围0-15）
     * </pre>
     *
     * @param val AD掉电参数（范围0-15）
     */
    public void setPowerDownAd(int val){                                                 // 公有方法：设置AD掉电
        setVal(0,16,4,val);                                                              // 设置字0的位16-19为AD掉电参数值val（4位）
    }                                                                                    // 方法结束

    /**
     * 设置Q路AD掉电
     * 设置Q路ADC掉电控制参数
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置Q路ADC掉电控制参数</li>
     *   <li>用于Q路ADC芯片的掉电控制</li>
     *   <li>控制Q路ADC通道的电源状态</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位20-23：设置为Q路AD掉电参数值val（4位，范围0-15）
     * </pre>
     *
     * <p><b>Q路说明：</b></p>
     * <ul>
     *   <li>Q路：ADC的Q通道（第二通道）</li>
     *   <li>用于双通道ADC的Q路控制</li>
     * </ul>
     *
     * @param val Q路AD掉电参数（范围0-15）
     */
    public void setPowerDownQAd(int val){                                                // 公有方法：设置Q路AD掉电
        setVal(0,20,4,val);                                                              // 设置字0的位20-23为Q路AD掉电参数值val（4位）
    }                                                                                    // 方法结束

    /**
     * 设置数据长度
     * 设置SPI写入的数据长度
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置SPI写入的数据长度（字节数）</li>
     *   <li>数据长度决定了向SPI设备写入多少字节</li>
     *   <li>不同设备类型有不同的数据长度要求</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位24-29：设置为数据长度值len（6位，范围0-63）
     * </pre>
     *
     * @param len 数据长度（字节数，范围0-63）
     */
    public void setDataLength(int len){                                                  // 公有方法：设置数据长度
        setVal(0,24,6,len);                                                              // 设置字0的位24-29为数据长度值len（6位）
    }                                                                                    // 方法结束

    /**
     * 设置有效字节数
     * 设置SPI写入的有效字节数标志
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置SPI写入的有效字节数标志</li>
     *   <li>用于指示写入数据的有效性</li>
     *   <li>通常设置为0表示全部有效</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位30-31：设置为有效字节数标志n（2位，范围0-3）
     * </pre>
     *
     * @param n 有效字节数标志（范围0-3）
     */
    public void setByteValid(int n){                                                     // 公有方法：设置有效字节数
        setVal(0,30,2,n);                                                                // 设置字0的位30-31为有效字节数标志n（2位）
    }                                                                                    // 方法结束

    /**
     * 设置AD数据
     * 设置ADC芯片的地址和数据值
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置ADC芯片的寄存器地址和数据值</li>
     *   <li>用于ADC芯片的配置写入</li>
     *   <li>根据硬件产品类型使用不同的数据格式</li>
     * </ul>
     *
     * <p><b>数据格式：</b></p>
     * <pre>
     * MHO68V2 + 非B12DJ3200NBB模式：
     * - 字1：ADC地址
     * - 字2：ADC数据值
     * - 地址和数据分开存储
     *
     * 其他模式：
     * - 字1 = (addr << voffset) | (val & vmask)
     * - 地址在位voffset-voffset+15，数据在位0-15
     * - voffset默认为16，vmask默认为0xFFFF
     * </pre>
     *
     * <p><b>硬件产品适配：</b></p>
     * <ul>
     *   <li>MHO68V2 + 非B12DJ3200NBB：地址和数据分开存储</li>
     *   <li>其他硬件产品：地址和数据合并存储</li>
     * </ul>
     *
     * @param addr ADC寄存器地址
     * @param val ADC数据值
     */
    public void setAdData(int addr,int val)                                              // 公有方法：设置AD数据
    {                                                                                    // 方法体开始
        if(HardwareProduct.isMHO68V2()                                                   // 条件判断：如果是MHO68V2硬件产品
                && !Hardware.getInstance().isAdcB12DJ3200NBB()){                          // 条件判断：且ADC不是B12DJ3200NBB型号
            setVal(1, addr);                                                              // 设置字1为ADC地址（地址和数据分开存储）
            setVal(2, val);                                                               // 设置字2为ADC数据值（地址和数据分开存储）
        }else {                                                                          // 条件分支：其他硬件产品或ADC型号
            setVal(1, (addr << voffset) | (val & vmask));                                 // 设置字1为合并的地址和数据：地址左移voffset位，数据掩码后合并
        }                                                                                // 条件判断结束
    }                                                                                    // 方法结束



    /**
     * 设置AD模式
     * 设置ADC芯片的工作模式
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置ADC芯片的工作模式</li>
     *   <li>用于ADC芯片的模式配置</li>
     *   <li>控制ADC的工作状态和参数</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 字0位20-23：设置为AD模式值val（4位，范围0-15）
     * 字1位0-15：设置为AD模式参数v（16位）
     * </pre>
     *
     * @param val AD模式值（范围0-15）
     * @param v AD模式参数（16位）
     */
    public void setADMode(int val,int v){                                                // 公有方法：设置AD模式
        setVal(0,20,4,val);                                                              // 设置字0的位20-23为AD模式值val（4位）
        setVal(1,0,16,v);                                                                // 设置字1的位0-15为AD模式参数v（16位）
    }                                                                                    // 方法结束
}                                                                                        // 类结束