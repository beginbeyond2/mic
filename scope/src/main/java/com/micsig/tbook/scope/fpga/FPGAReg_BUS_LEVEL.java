package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA总线电平配置寄存器类 - 用于配置串行总线解码的电平阈值和源选择
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）</li>
 *   <li>职责类型：硬件配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置串行总线解码的电平阈值</li>
 *   <li>配置总线源选择（物理通道映射）</li>
 *   <li>配置总线源扩展（外部触发源）</li>
 *   <li>配置电平扩展（辅助电平阈值）</li>
 *   <li>封装FPGA电平寄存器的位操作</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供统一的总线电平配置接口</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 *   <li>支持多总线电平配置</li>
 *   <li>支持多通道源选择</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_BUS_LEVEL寄存器（32位，4字节）
 * │
 * ├── 位0-15：总线电平配置（每个总线4位，每位对应一个通道）
 * │     ├── 位0-3：总线0电平（CH1-CH4）
 * │     │     ├── 位0：CH1电平阈值
 * │     │     ├── 位1：CH2电平阈值
 * │     │     ├── 位2：CH3电平阈值
 * │     │     └── 位3：CH4电平阈值
 * │     ├── 位4-7：总线1电平（CH1-CH4）
 * │     ├── 位8-11：总线2电平（CH1-CH4）
 * │     └── 位12-15：总线3电平（CH1-CH4）
 * │
 * ├── 位8-23：总线源扩展配置（每个总线4位）
 * │     ├── 位8-11：总线0源扩展
 * │     ├── 位12-15：总线1源扩展
 * │     ├── 位16-19：总线2源扩展
 * │     └── 位20-23：总线3源扩展
 * │
 * ├── 位16-31：电平扩展配置（每个总线4位，每位对应一个通道）
 * │     ├── 位16-19：总线0电平扩展（CH1-CH4）
 * │     ├── 位20-23：总线1电平扩展（CH1-CH4）
 * │     ├── 位24-27：总线2电平扩展（CH1-CH4）
 * │     └── 位28-31：总线3电平扩展（CH1-CH4）
 * │
 * └── 位24-31：源配置（每个FPGA索引2位，每个总线2位）
 *       ├── 位24-25：FPGA0总线0源
 *       ├── 位26-27：FPGA0总线1源
 *       ├── 位28-29：FPGA1总线0源
 *       └── 位30-31：FPGA1总线1源
 * </pre>
 *
 * <p><b>电平阈值说明：</b>
 * <ul>
 *   <li>电平阈值用于判断串行总线信号的高低电平</li>
 *   <li>不同总线协议需要不同的电平阈值</li>
 *   <li>电平阈值通常根据信号幅度自动计算或手动设置</li>
 * </ul>
 *
 * <p><b>源选择说明：</b>
 * <ul>
 *   <li>源选择用于指定串行总线解码使用的物理通道</li>
 *   <li>不同总线可能使用不同的物理通道组合</li>
 *   <li>源选择影响解码数据的来源</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>串行总线解码配置：配置I2C/SPI/CAN/UART等总线解码参数</li>
 *   <li>电平阈值调整：根据信号幅度调整解码阈值</li>
 *   <li>源通道选择：选择解码使用的物理通道</li>
 *   <li>多总线解码：同时解码多个串行总线</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：SerialChannel（串行通道类，提供总线配置）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：ChannelFactory（通道工厂，提供物理通道）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>电平配置需要与总线解码同步</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建总线电平寄存器实例
 * FPGAReg_BUS_LEVEL regBusLevel = new FPGAReg_BUS_LEVEL();
 *
 * // 设置总线0的CH1电平阈值
 * regBusLevel.setLevel(0, 0, 1);  // 总线索引=0，通道索引=0，电平值=1
 *
 * // 设置总线0的源扩展
 * regBusLevel.setBusSrcExt(0, 0x5);  // 总线索引=0，源扩展值=0x5
 *
 * // 设置总线0的电平扩展
 * regBusLevel.setLevelExt(0, 1, 1);  // 总线索引=0，通道索引=1，电平值=1
 *
 * // 设置总线0的源
 * regBusLevel.setSrc(0, 2);  // 总线索引=0，源值=2
 *
 * // 发送寄存器配置到FPGA
 * fpgaCommand.sendCmd(regBusLevel);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018/5/30
 * @see FPGAReg FPGA寄存器基类
 * @see SerialChannel 串行通道类
 * @see FPGACommand FPGA命令处理类
 * @see FPGAReg_BUS 总线基类
 */
public class FPGAReg_BUS_LEVEL extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 构造函数 - 初始化总线电平寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_BUS_LEVEL（总线电平配置寄存器）</li>
     *   <li>寄存器大小：4字节（32位）</li>
     * </ul>
     *
     * <p><b>寄存器地址说明：</b>
     * <ul>
     *   <li>FPGA_BUS_LEVEL：用于配置串行总线解码的电平阈值和源选择</li>
     *   <li>地址值由FPGAReg基类定义</li>
     * </ul>
     *
     * <p><b>寄存器大小说明：</b>
     * <ul>
     *   <li>4字节（32位）足够存储4个总线的电平配置</li>
     *   <li>外加源扩展、电平扩展和源配置</li>
     * </ul>
     */
    public FPGAReg_BUS_LEVEL() {  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_BUS_LEVEL：总线电平寄存器地址
        // 4：寄存器大小为4字节（32位）
        super(FPGA_BUS_LEVEL, 4);  // 初始化总线电平寄存器，地址为FPGA_BUS_LEVEL，大小为4字节
    }

    /**
     * 设置总线电平阈值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置指定总线的指定通道的电平阈值</li>
     *   <li>电平阈值用于判断串行总线信号的高低电平</li>
     *   <li>寄存器位：位0-15（每个总线4位，每位对应一个通道）</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>busIdx：总线索引（0-3，对应S1-S4）</li>
     *   <li>chIdx：通道索引（0-3，对应CH1-CH4）</li>
     *   <li>val：电平值（0或1，表示低电平或高电平阈值）</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>I2C总线解码：设置SDA和SCL的电平阈值</li>
     *   <li>SPI总线解码：设置MOSI、MISO、SCK、CS的电平阈值</li>
     *   <li>CAN总线解码：设置CAN_H和CAN_L的电平阈值</li>
     *   <li>UART总线解码：设置TX和RX的电平阈值</li>
     * </ul>
     *
     * <p><b>计算公式：</b>
     * <pre>
     * 位位置 = busIdx * 4 + chIdx
     * </pre>
     *
     * @param busIdx 总线索引（0-3）
     * @param chIdx 通道索引（0-3）
     * @param val 电平值（0或1）
     */
    public void setLevel(int busIdx,int chIdx,int val){  // 设置总线电平阈值方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置 = busIdx * 4 + chIdx（总线索引×4 + 通道索引）
        // 参数2：位宽度 = 1（1位）
        // 参数3：电平值（0或1）
        setVal(busIdx * 4 + chIdx,1,val);  // 设置位(busIdx*4+chIdx)的值，宽度为1位，值为val
    }

    /**
     * 设置总线源扩展
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置总线的外部触发源</li>
     *   <li>源扩展用于指定总线解码使用的外部触发输入</li>
     *   <li>寄存器位：位8-23（每个总线4位）</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>busIdx：总线索引（0-3，对应S1-S4）</li>
     *   <li>val：源扩展值（4位，0x0-0xF）</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>外部触发解码：使用外部触发信号作为解码触发源</li>
     *   <li>多源解码：同时使用内部通道和外部触发源</li>
     *   <li>特殊解码场景：需要外部触发配合的解码</li>
     * </ul>
     *
     * <p><b>计算公式：</b>
     * <pre>
     * 位位置 = 8 + busIdx * 4
     * 位宽度 = 4
     * 有效值 = val & 0xF（取低4位）
     * </pre>
     *
     * @param busIdx 总线索引（0-3）
     * @param val 源扩展值（0x0-0xF）
     */
    public void setBusSrcExt(int busIdx,int val){  // 设置总线源扩展方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置 = 8 + busIdx * 4（位8开始，每个总线4位）
        // 参数2：位宽度 = 4（4位）
        // 参数3：源扩展值 & 0xF（取低4位，确保有效范围）
        setVal(8 + busIdx * 4,4,val & 0xF);  // 设置位(8+busIdx*4)的值，宽度为4位，值为val的低4位
    }

    /**
     * 设置电平扩展
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置指定总线的指定通道的辅助电平阈值</li>
     *   <li>电平扩展用于设置额外的电平判断阈值</li>
     *   <li>寄存器位：位16-31（每个总线4位，每位对应一个通道）</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>busIdx：总线索引（0-3，对应S1-S4）</li>
     *   <li>chIdx：通道索引（0-3，对应CH1-CH4）</li>
     *   <li>val：电平扩展值（0或1）</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>双阈值解码：使用两个电平阈值提高解码准确性</li>
     *   <li>差分信号解码：设置差分信号的正负电平阈值</li>
     *   <li>复杂信号解码：需要多个电平判断的信号</li>
     * </ul>
     *
     * <p><b>计算公式：</b>
     * <pre>
     * 位位置 = 16 + busIdx * 4 + chIdx
     * </pre>
     *
     * @param busIdx 总线索引（0-3）
     * @param chIdx 通道索引（0-3）
     * @param val 电平扩展值（0或1）
     */
    public void setLevelExt(int busIdx, int chIdx, int val) {  // 设置电平扩展方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置 = 16 + busIdx * 4 + chIdx（位16开始，总线索引×4 + 通道索引）
        // 参数2：位宽度 = 1（1位）
        // 参数3：电平扩展值（0或1）
        setVal(16 + busIdx * 4 + chIdx, 1, val);  // 设置位(16+busIdx*4+chIdx)的值，宽度为1位，值为val
    }

    /**
     * 设置总线源
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置总线解码使用的物理通道源</li>
     *   <li>源选择用于指定解码数据的来源通道</li>
     *   <li>寄存器位：位24-31（每个FPGA索引2位，每个总线2位）</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>busIdx：总线索引（0-3，对应S1-S4）</li>
     *   <li>val：源值（2位，0-3）</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>通道映射：将串行总线映射到物理通道</li>
     *   <li>多通道解码：选择不同的物理通道组合</li>
     *   <li>动态切换：在运行时切换解码源通道</li>
     * </ul>
     *
     * <p><b>计算公式：</b>
     * <pre>
     * 位位置 = 24 + fpgaIdx * 4 + busIdx * 2
     * 位宽度 = 2
     * </pre>
     *
     * <p><b>FPGA索引说明：</b>
     * <ul>
     *   <li>fpgaIdx：FPGA索引（0或1，取决于硬件配置）</li>
     *   <li>通过getFpgaIdx()方法获取当前FPGA索引</li>
     * </ul>
     *
     * @param busIdx 总线索引（0-3）
     * @param val 源值（0-3）
     */
    public void setSrc(int busIdx,int val){  // 设置总线源方法
        // 获取当前FPGA索引（0或1）
        int fpgaIdx = getFpgaIdx();  // 从父类获取FPGA索引，用于计算位位置
        
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置 = 24 + fpgaIdx * 4 + busIdx * 2（位24开始，FPGA索引×4 + 总线索引×2）
        // 参数2：位宽度 = 2（2位）
        // 参数3：源值（0-3）
        setVal(24 + fpgaIdx * 4 + busIdx * 2,2,val);  // 设置位(24+fpgaIdx*4+busIdx*2)的值，宽度为2位，值为val
    }

}