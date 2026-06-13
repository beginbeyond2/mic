package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import android.util.Log;  // 导入Android日志类，用于调试输出

/**
 * FPGA通道电容DAC寄存器类 - 用于配置通道电容补偿的DAC值
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）</li>
 *   <li>职责类型：通道电容补偿配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置通道电容补偿的高位DAC值</li>
 *   <li>支持多通道独立配置</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供通道电容补偿DAC值的配置接口</li>
 *   <li>支持高精度电容补偿</li>
 *   <li>实现精确的通道电容控制</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_CH_CAP_DA12寄存器（64位，8字节）
 * │
 * ├── 字节0-1：通道0和通道1的电容DAC值
 * │     ├── 位0-15：通道0电容DAC值（16位）
 * │     └── 位16-31：通道1电容DAC值（16位）
 * │
 * └── 字节2-3：通道2和通道3的电容DAC值
 *       ├── 位0-15：通道2电容DAC值（16位）
 *       └── 位16-31：通道3电容DAC值（16位）
 * </pre>
 *
 * <p><b>电容补偿说明：</b>
 * <ul>
 *   <li>电容补偿用于校正通道输入电容的影响</li>
 *   <li>DAC值控制电容补偿电路的输出</li>
 *   <li>高精度补偿需要高位DAC值</li>
 *   <li>补偿值通常在校准过程中确定</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>通道校准：设置通道电容补偿DAC值</li>
 *   <li>高精度测量：实现精确的电容补偿</li>
 *   <li>信号校正：校正通道输入电容的影响</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
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
 * @see FPGAReg_CH_CAPACITANCE 通道电容寄存器类
 */
public class FPGAReg_CH_CAP_DA extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 构造函数 - 初始化通道电容DAC寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_CH_CAP_DA12</li>
     *   <li>寄存器大小：8字节（64位）</li>
     * </ul>
     */
    public FPGAReg_CH_CAP_DA() {  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_CH_CAP_DA12：通道电容DAC寄存器地址
        // 8：寄存器大小为8字节（64位）
        super(FPGA_CH_CAP_DA12, 8);  // 初始化通道电容DAC寄存器，地址为FPGA_CH_CAP_DA12，大小为8字节
    }

    /**
     *设置高位DAC值 - 配置指定通道的电容补偿高位DAC值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置指定通道的电容补偿高位DAC值</li>
     *   <li>支持多通道独立配置</li>
     *   <li>用于高精度电容补偿</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b>
     * <ul>
     *   <li>字节偏移：chIdx / 2（通道索引除以2）</li>
     *   <li>位偏移：(chIdx % 2) * 16（偶数通道在低16位，奇数通道在高16位）</li>
     *   <li>位宽度：16位</li>
     *   <li>值：高位DAC值</li>
     * </ul>
     *
     * <p><b>高位DAC值说明：</b>
     * <ul>
     *   <li>高位DAC值范围：0-65535（16位精度）</li>
     *   <li>高位DAC值控制电容补偿的高精度部分</li>
     *   <li>与低位DAC值配合，实现更高精度的补偿</li>
     * </ul>
     *
     * @param chIdx 通道索引（0=CH1，1=CH2，2=CH3，3=CH4）
     * @param val 高位DAC值（0-65535）
     */
    public void setHigh(int chIdx, int val) {  // 设置高位DAC值方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：字节偏移=chIdx / 2（通道索引除以2，得到字节偏移）
        // 参数2：位偏移=(chIdx % 2) * 16（偶数通道在低16位，奇数通道在高16位）
        // 参数3：位宽度=16（16位精度）
        // 参数4：高位DAC值
        setVal(chIdx / 2, (chIdx % 2) * 16, 16, val);  // 设置指定通道的高位DAC值
    }

}