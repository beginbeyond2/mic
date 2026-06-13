package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import com.micsig.tbook.hardware.HardwareProduct;  // 导入硬件产品类，用于判断产品型号

/**
 * FPGA探头DAC寄存器类 - 用于配置探头补偿的DAC值
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）</li>
 *   <li>职责类型：探头补偿配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置探头补偿的DAC值</li>
 *   <li>支持多通道独立配置</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供探头补偿DAC值的配置接口</li>
 *   <li>支持不同探头的补偿需求</li>
 *   <li>实现精确的探头补偿控制</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_PROBE_DA12寄存器（64位，8字节）
 * │
 * ├── 字节0-1：通道2和通道3的DAC值
 * │     ├── 位0-15：通道3 DAC值（16位）
 * │     └── 位16-31：通道2 DAC值（16位）
 * │
 * └── 字节2-3：通道0和通道1的DAC值
 *       ├── 位0-15：通道1 DAC值（16位）
 *       └── 位16-31：通道0 DAC值（16位）
 * </pre>
 *
 * <p><b>探头补偿说明：</b>
 * <ul>
 *   <li>探头补偿用于校正探头带来的信号失真</li>
 *   <li>DAC值控制补偿电路的输出</li>
 *   <li>不同探头需要不同的补偿值</li>
 *   <li>补偿值通常在校准过程中确定</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>探头校准：设置探头补偿DAC值</li>
 *   <li>探头更换：更换探头后重新配置补偿</li>
 *   <li>信号校正：校正探头带来的信号失真</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：HardwareProduct（硬件产品类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>非线程安全，需在单线程环境中使用</li>
 * </ul>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/21
 * @see FPGAReg FPGA寄存器基类
 * @see HardwareProduct 硬件产品类
 */
public class FPGAReg_PROBE_DA12 extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 构造函数 - 初始化探头DAC寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_PROBE_DA12</li>
     *   <li>寄存器大小：8字节（64位）</li>
     * </ul>
     */
    public FPGAReg_PROBE_DA12() {  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_PROBE_DA12：探头DAC寄存器地址
        // 8：寄存器大小为8字节（64位）
        super(FPGA_PROBE_DA12, 8);  // 初始化探头DAC寄存器，地址为FPGA_PROBE_DA12，大小为8字节
    }

    /**
     * 设置DAC值 - 配置指定通道的探头补偿DAC值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置指定通道的探头补偿DAC值</li>
     *   <li>支持多通道独立配置</li>
     *   <li>通道索引经过转换以适应寄存器布局</li>
     * </ul>
     *
     * <p><b>通道索引转换：</b>
     * <ul>
     *   <li>原始索引：0=CH1，1=CH2，2=CH3，3=CH4</li>
     *   <li>转换后：chIdx = 3 - chIdx</li>
     *   <li>转换后索引：3=CH1，2=CH2，1=CH3，0=CH4</li>
     *   <li>转换是为了适应寄存器的位布局</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b>
     * <ul>
     *   <li>字节偏移：chIdx >> 1（通道索引除以2）</li>
     *   <li>位偏移：(chIdx & 0x01) * 16（奇偶通道决定位偏移）</li>
     *   <li>位宽度：16位</li>
     *   <li>值：DAC值（16位精度）</li>
     * </ul>
     *
     * <p><b>DAC值说明：</b>
     * <ul>
     *   <li>DAC值范围：0-65535（16位精度）</li>
     *   <li>DAC值控制补偿电路的输出电压</li>
     *   <li>值越大，补偿电压越高</li>
     * </ul>
     *
     * @param chIdx 通道索引（0=CH1，1=CH2，2=CH3，3=CH4）
     * @param val DAC值（0-65535）
     */
    public void setDaValue(int chIdx, int val) {  // 设置DAC值方法
        // 通道索引转换：3 - chIdx
        // 原始索引：0=CH1，1=CH2，2=CH3，3=CH4
        // 转换后：3=CH1，2=CH2，1=CH3，0=CH4
        chIdx = 3 - chIdx;  // 转换通道索引，适应寄存器布局
        
        // 调用父类setVal方法，设置寄存器值
        // 参数1：字节偏移=chIdx >> 1（通道索引除以2，得到字节偏移）
        // 参数2：位偏移=(chIdx & 0x01) * 16（奇偶通道决定位偏移，偶数通道在低16位，奇数通道在高16位）
        // 参数3：位宽度=16（16位精度）
        // 参数4：DAC值
        setVal(chIdx >> 1, (chIdx & 0x01) * 16, 16, val);  // 设置指定通道的DAC值
    }

}