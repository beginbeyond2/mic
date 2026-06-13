package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA通道电容寄存器类 - 用于配置示波器通道的电容校准值
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）</li>
 *   <li>职责类型：硬件校准配置器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置通道的高位电容校准值</li>
 *   <li>配置通道的总电容校准值</li>
 *   <li>支持多通道电容配置</li>
 *   <li>封装FPGA电容寄存器的位操作</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>补偿通道输入电容的影响</li>
 *   <li>提高测量精度和准确性</li>
 *   <li>支持不同探头下的电容校准</li>
 *   <li>支持多通道独立校准</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_CH_CAPACITANCE1寄存器（8字节，2个4字节字段）
 * │
 * ├── 字段0（位0-31）：通道0和通道1的电容配置
 * │     ├── 位0-7：通道0高位电容（High0）
 * │     │     └── 用于补偿通道0的高位电容
 * │     │
 * │     ├── 位8-15：通道0总电容（Total0）
 * │     │     └── 用于补偿通道0的总电容
 * │     │
 * │     ├── 位16-23：通道1高位电容（High1）
 * │     │     └── 用于补偿通道1的高位电容
 * │     │
 * │     └── 位24-31：通道1总电容（Total1）
 * │           └── 用于补偿通道1的总电容
 * │
 * └── 字段1（位32-63）：通道2和通道3的电容配置
 *       ├── 位0-7：通道2高位电容（High2）
 *       │     └── 用于补偿通道2的高位电容
 *       │
 *       ├── 位8-15：通道2总电容（Total2）
 *       │     └── 用于补偿通道2的总电容
 *       │
 *       ├── 位16-23：通道3高位电容（High3）
 *       │     └── 用于补偿通道3的高位电容
 *       │
 *       └── 位24-31：通道3总电容（Total3）
 *             └── 用于补偿通道3的总电容
 * </pre>
 *
 * <p><b>电容校准说明：</b>
 * <ul>
 *   <li>高位电容（High）：用于补偿高位量程的电容误差</li>
 *   <li>总电容（Total）：用于补偿总电容误差</li>
 *   <li>电容值影响信号的频率响应和测量精度</li>
 *   <li>不同探头需要不同的电容校准值</li>
 * </ul>
 *
 * <p><b>通道映射说明：</b>
 * <ul>
 *   <li>通道索引：0=CH1，1=CH2，2=CH3，3=CH4</li>
 *   <li>每个通道占用8位（高位4位+总电容4位）</li>
 *   <li>每4字节字段包含2个通道的电容配置</li>
 *   <li>字段索引 = chIdx / 2（通道索引除以2）</li>
 *   <li>字段内偏移 = (chIdx % 2) * 16（通道索引模2乘16）</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>探头校准：根据探头类型配置电容值</li>
 *   <li>出厂校准：生产线上进行电容校准</li>
 *   <li>用户校准：用户手动执行自校准操作</li>
 *   <li>探头识别：根据探头自动切换电容值</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：Probe（探头类）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>电容配置需要确保硬件稳定</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建通道电容寄存器实例
 * FPGAReg_CH_CAPACITANCE regCapacitance = new FPGAReg_CH_CAPACITANCE();
 *
 * // 设置通道0的高位电容和总电容
 * regCapacitance.setHigh(0, 100);   // 设置CH1高位电容
 * regCapacitance.setTotal(0, 200);  // 设置CH1总电容
 *
 * // 设置通道1的高位电容和总电容
 * regCapacitance.setHigh(1, 150);   // 设置CH2高位电容
 * regCapacitance.setTotal(1, 250);  // 设置CH2总电容
 *
 * // 发送寄存器配置到FPGA
 * fpgaCommand.sendCmd(regCapacitance);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018
 * @see FPGAReg FPGA寄存器基类
 * @see FPGACommand FPGA命令处理类
 */
public class FPGAReg_CH_CAPACITANCE extends FPGAReg{  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 日志标签 - 用于日志输出和调试
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>标识日志来源类</li>
     *   <li>用于调试和故障诊断</li>
     * </ul>
     */
    private String TAG = "FPGAReg_CH_CAPACITANCE";  // 日志标签，用于日志输出和调试

    /**
     * 构造函数 - 初始化通道电容寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_CH_CAPACITANCE1（通道电容寄存器）</li>
     *   <li>寄存器大小：8字节（2个4字节字段）</li>
     * </ul>
     *
     * <p><b>寄存器地址说明：</b>
     * <ul>
     *   <li>FPGA_CH_CAPACITANCE1：用于配置通道的电容校准值</li>
     *   <li>地址值由FPGAReg基类定义</li>
     * </ul>
     *
     * <p><b>寄存器大小说明：</b>
     * <ul>
     *   <li>8字节（64位）用于存储4个通道的电容配置</li>
     *   <li>每个通道占用8位（高位4位+总电容4位）</li>
     * </ul>
     */
    public FPGAReg_CH_CAPACITANCE(){  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_CH_CAPACITANCE1：通道电容寄存器地址
        // 8：寄存器大小为8字节（2个4字节字段）
        super(FPGA_CH_CAPACITANCE1, 8);  // 初始化通道电容寄存器，地址为FPGA_CH_CAPACITANCE1，大小为8字节
    }

    /**
     * 设置高位电容 - 配置指定通道的高位电容校准值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置指定通道的高位电容校准值</li>
     *   <li>高位电容用于补偿高位量程的电容误差</li>
     *   <li>支持多通道配置</li>
     * </ul>
     *
     * <p><b>通道映射计算：</b>
     * <ul>
     *   <li>字段索引 = chIdx / 2（通道索引除以2）</li>
     *   <li>字段内偏移 = (chIdx % 2) * 16（通道索引模2乘16）</li>
     *   <li>例如：chIdx=0时，字段0，偏移0（位0-7）</li>
     *   <li>例如：chIdx=1时，字段0，偏移16（位16-23）</li>
     *   <li>例如：chIdx=2时，字段1，偏移0（位32-39）</li>
     *   <li>例如：chIdx=3时，字段1，偏移16（位48-55）</li>
     * </ul>
     *
     * <p><b>高位电容作用：</b>
     * <ul>
     *   <li>补偿高位量程的电容误差</li>
     *   <li>影响信号的频率响应</li>
     *   <li>提高测量精度</li>
     * </ul>
     *
     * @param chIdx 通道索引（0=CH1，1=CH2，2=CH3，3=CH4）
     * @param val 高位电容值（8位，0-255）
     */
    public void setHigh(int chIdx, int val){  // 设置高位电容方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：字段索引 = chIdx / 2（通道索引除以2）
        // 参数2：字段内偏移 = (chIdx % 2) * 16（通道索引模2乘16）
        // 参数3：位宽度 = 8位
        // 参数4：高位电容值
        setVal(chIdx / 2, (chIdx % 2) * 16, 8, val);  // 设置指定通道的高位电容值，字段索引为chIdx/2，偏移为(chIdx%2)*16，宽度为8位
    }

    /**
     * 设置总电容 - 配置指定通道的总电容校准值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置指定通道的总电容校准值</li>
     *   <li>总电容用于补偿总电容误差</li>
     *   <li>支持多通道配置</li>
     * </ul>
     *
     * <p><b>通道映射计算：</b>
     * <ul>
     *   <li>字段索引 = chIdx / 2（通道索引除以2）</li>
     *   <li>字段内偏移 = (chIdx % 2) * 16 + 8（高位电容偏移+8）</li>
     *   <li>例如：chIdx=0时，字段0，偏移8（位8-15）</li>
     *   <li>例如：chIdx=1时，字段0，偏移24（位24-31）</li>
     *   <li>例如：chIdx=2时，字段1，偏移8（位40-47）</li>
     *   <li>例如：chIdx=3时，字段1，偏移24（位56-63）</li>
     * </ul>
     *
     * <p><b>总电容作用：</b>
     * <ul>
     *   <li>补偿总电容误差</li>
     *   <li>影响信号的频率响应</li>
     *   <li>提高测量精度</li>
     * </ul>
     *
     * @param chIdx 通道索引（0=CH1，1=CH2，2=CH3，3=CH4）
     * @param val 总电容值（8位，0-255）
     */
    public void setTotal(int chIdx, int val){  // 设置总电容方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：字段索引 = chIdx / 2（通道索引除以2）
        // 参数2：字段内偏移 = (chIdx % 2) * 16 + 8（高位电容偏移+8）
        // 参数3：位宽度 = 8位
        // 参数4：总电容值
        setVal(chIdx / 2, (chIdx % 2) * 16 + 8, 8, val);  // 设置指定通道的总电容值，字段索引为chIdx/2，偏移为(chIdx%2)*16+8，宽度为8位
    }
}
