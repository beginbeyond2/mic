package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA外部触发电平寄存器类 - 用于配置示波器外部触发输入的触发电平
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）</li>
 *   <li>职责类型：硬件触发配置器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置外部触发输入的触发电平</li>
 *   <li>设置外部触发阈值电压</li>
 *   <li>封装FPGA触发电平寄存器的位操作</li>
 *   <li>支持外部触发信号的精确控制</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供外部触发电平配置接口</li>
 *   <li>支持外部触发信号的精确触发</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 *   <li>支持不同电压范围的触发配置</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_EXT_GRIGGER_LEVEL寄存器（4字节，32位）
 * │
 * ├── 位0-15：触发电平值（16位）
 * │     └── 外部触发电平的DAC值
 * │     └── 用于设置外部触发阈值电压
 * │
 * └── 位16-31：保留字段（16位）
 *       └── 未使用，保留为0
 * </pre>
 *
 * <p><b>触发电平说明：</b>
 * <ul>
 *   <li>触发电平：外部触发信号达到该电压时触发采集</li>
 *   <li>DAC值：数字模拟转换器的设置值</li>
 *   <li>电压范围：取决于外部触发输入的硬件设计</li>
 *   <li>精度：16位DAC提供高精度的电平设置</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>外部触发配置：用户设置外部触发电平</li>
 *   <li>自动触发：根据信号特征自动设置触发电平</li>
 *   <li>触发调试：调整触发电平以优化触发效果</li>
 *   <li>特殊应用：特定电压点的触发采集</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：TriggerCommon（触发配置类）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>触发电平配置需要确保硬件稳定</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建外部触发电平寄存器实例
 * FPGAReg_ExtTriggerLevel regExtTriggerLevel = new FPGAReg_ExtTriggerLevel();
 *
 * // 设置外部触发电平（DAC值）
 * regExtTriggerLevel.setLevel(32768);  // 设置为中间值
 *
 * // 发送寄存器配置到FPGA
 * fpgaCommand.sendCmd(regExtTriggerLevel);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018
 * @see FPGAReg FPGA寄存器基类
 * @see FPGACommand FPGA命令处理类
 * @see TriggerCommon 触发配置类
 */
public class FPGAReg_ExtTriggerLevel extends FPGAReg{  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 构造函数 - 初始化外部触发电平寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_EXT_GRIGGER_LEVEL（外部触发电平寄存器）</li>
     *   <li>寄存器大小：4字节（32位）</li>
     * </ul>
     *
     * <p><b>寄存器地址说明：</b>
     * <ul>
     *   <li>FPGA_EXT_GRIGGER_LEVEL：用于配置外部触发输入的触发电平</li>
     *   <li>地址值由FPGAReg基类定义</li>
     *   <li>注意：地址名称中的"GRIGGER"可能是"TRIGGER"的拼写错误</li>
     * </ul>
     *
     * <p><b>寄存器大小说明：</b>
     * <ul>
     *   <li>4字节（32位）足够存储16位触发电平值</li>
     *   <li>高16位保留未使用</li>
     * </ul>
     */
    FPGAReg_ExtTriggerLevel(){  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_EXT_GRIGGER_LEVEL：外部触发电平寄存器地址
        // 4：寄存器大小为4字节（32位）
        super(FPGAReg.FPGA_EXT_GRIGGER_LEVEL, 4);  // 初始化外部触发电平寄存器，地址为FPGA_EXT_GRIGGER_LEVEL，大小为4字节
    }

    /**
     * 设置触发电平 - 配置外部触发输入的触发电平值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置外部触发电平的DAC值</li>
     *   <li>数据位置：位0-15（16位）</li>
     *   <li>用于设置外部触发阈值电压</li>
     * </ul>
     *
     * <p><b>DAC值说明：</b>
     * <ul>
     *   <li>DAC值：数字模拟转换器的设置值</li>
     *   <li>16位DAC提供高精度的电平设置</li>
     *   <li>DAC值转换为实际电压由硬件电路决定</li>
     *   <li>值范围：0-65535（16位）</li>
     * </ul>
     *
     * <p><b>触发电平作用：</b>
     * <ul>
     *   <li>外部触发信号达到该电平时触发采集</li>
     *   <li>电平值越高，触发阈值电压越高</li>
     *   <li>电平值越低，触发阈值电压越低</li>
     * </ul>
     *
     * @param v 触发电平DAC值（16位，0-65535）
     */
    void setLevel(int v){  // 设置触发电平方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=0（位0开始）
        // 参数2：位宽度=16位
        // 参数3：触发电平DAC值
        setVal(0, 16, v);  // 设置位0-15的值，宽度为16位，值为v
    }
}
