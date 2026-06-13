package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA通道阻抗配置寄存器类 - 用于配置示波器通道的输入阻抗
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
 *   <li>配置示波器通道的输入阻抗（1MΩ或50Ω）</li>
 *   <li>配置外部触发输入的阻抗</li>
 *   <li>配置阻抗选择模式</li>
 *   <li>封装FPGA阻抗寄存器的位操作</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供统一的阻抗配置接口</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 *   <li>支持多通道阻抗配置</li>
 *   <li>支持外部触发阻抗配置</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_CH_RESISTANCE寄存器（32位，4字节）
 * │
 * ├── 位0-3：通道阻抗配置（每位对应一个通道）
 * │     ├── 位0：CH1阻抗（0=1MΩ，1=50Ω）
 * │     ├── 位1：CH2阻抗（0=1MΩ，1=50Ω）
 * │     ├── 位2：CH3阻抗（0=1MΩ，1=50Ω）
 * │     └── 位3：CH4阻抗（0=1MΩ，1=50Ω）
 * │
 * ├── 位8：外部触发输入阻抗
 * │     └── 0=1MΩ，1=50Ω
 * │
 * └── 位16-19：阻抗选择配置（每位对应一个通道）
 *       ├── 位16：CH1阻抗选择
 *       ├── 位17：CH2阻抗选择
 *       ├── 位18：CH3阻抗选择
 *       └── 位19：CH4阻抗选择
 * </pre>
 *
 * <p><b>阻抗类型说明：</b>
 * <ul>
 *   <li>1MΩ阻抗：高阻抗输入，适用于一般测量，不影响被测电路</li>
 *   <li>50Ω阻抗：低阻抗输入，适用于高频信号测量，提供更好的带宽和匹配</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>通道阻抗切换：用户手动切换通道阻抗</li>
 *   <li>探头识别：根据探头类型自动切换阻抗</li>
 *   <li>外部触发配置：配置外部触发输入的阻抗</li>
 *   <li>校准过程：校准时需要配置特定阻抗</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：Channel（通道类，提供阻抗类型）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：TriggerCommon（触发配置类）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>阻抗切换需要硬件保护（50Ω→1MΩ需要延时）</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建阻抗寄存器实例
 * FPGAReg_Resistance regResistance = new FPGAReg_Resistance();
 *
 * // 设置CH1为50Ω阻抗
 * regResistance.setResistanceCh1(1);
 *
 * // 设置CH2为1MΩ阻抗
 * regResistance.setResistanceCh2(0);
 *
 * // 设置外部触发为50Ω阻抗
 * regResistance.setExtTrigger(1);
 *
 * // 发送寄存器配置到FPGA
 * fpgaCommand.sendCmd(regResistance);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/13
 * @see FPGAReg FPGA寄存器基类
 * @see Channel 通道类
 * @see FPGACommand FPGA命令处理类
 */
public class FPGAReg_Resistance extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 构造函数 - 初始化阻抗寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_CH_RESISTANCE（通道阻抗配置寄存器）</li>
     *   <li>寄存器大小：4字节（32位）</li>
     * </ul>
     *
     * <p><b>寄存器地址说明：</b>
     * <ul>
     *   <li>FPGA_CH_RESISTANCE：用于配置示波器通道的输入阻抗</li>
     *   <li>地址值由FPGAReg基类定义</li>
     * </ul>
     *
     * <p><b>寄存器大小说明：</b>
     * <ul>
     *   <li>4字节（32位）足够存储4个通道的阻抗配置</li>
     *   <li>外加外部触发和阻抗选择配置</li>
     * </ul>
     */
    FPGAReg_Resistance(){  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_CH_RESISTANCE：阻抗寄存器地址
        // 4：寄存器大小为4字节（32位）
        super(FPGAReg.FPGA_CH_RESISTANCE, 4);  // 初始化阻抗寄存器，地址为FPGA_CH_RESISTANCE，大小为4字节
    }

    /**
     * 设置通道1的阻抗值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置CH1通道的输入阻抗</li>
     *   <li>阻抗值：0=1MΩ，1=50Ω</li>
     *   <li>寄存器位：位0（最低位）</li>
     * </ul>
     *
     * <p><b>阻抗选择说明：</b>
     * <ul>
     *   <li>1MΩ：高阻抗输入，适用于一般测量</li>
     *   <li>50Ω：低阻抗输入，适用于高频信号测量</li>
     * </ul>
     *
     * @param vol 阻抗值（0=1MΩ，1=50Ω）
     */
    void setResistanceCh1(int vol){  // 设置通道1阻抗方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=0（位0）
        // 参数2：位宽度=1（1位）
        // 参数3：阻抗值（0或1）
        setVal(0, 1, vol);  // 设置位0的值，宽度为1位，值为vol
    }

    /**
     * 设置通道2的阻抗值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置CH2通道的输入阻抗</li>
     *   <li>阻抗值：0=1MΩ，1=50Ω</li>
     *   <li>寄存器位：位1</li>
     * </ul>
     *
     * @param vol 阻抗值（0=1MΩ，1=50Ω）
     */
    void setResistanceCh2(int vol){  // 设置通道2阻抗方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=1（位1）
        // 参数2：位宽度=1（1位）
        // 参数3：阻抗值（0或1）
        setVal(1, 1, vol);  // 设置位1的值，宽度为1位，值为vol
    }

    /**
     * 设置通道3的阻抗值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置CH3通道的输入阻抗</li>
     *   <li>阻抗值：0=1MΩ，1=50Ω</li>
     *   <li>寄存器位：位2</li>
     * </ul>
     *
     * @param vol 阻抗值（0=1MΩ，1=50Ω）
     */
    void setResistanceCh3(int vol){  // 设置通道3阻抗方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=2（位2）
        // 参数2：位宽度=1（1位）
        // 参数3：阻抗值（0或1）
        setVal(2, 1, vol);  // 设置位2的值，宽度为1位，值为vol
    }

    /**
     * 设置通道4的阻抗值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置CH4通道的输入阻抗</li>
     *   <li>阻抗值：0=1MΩ，1=50Ω</li>
     *   <li>寄存器位：位3</li>
     * </ul>
     *
     * @param vol 阻抗值（0=1MΩ，1=50Ω）
     */
    void setResistanceCh4(int vol){  // 设置通道4阻抗方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=3（位3）
        // 参数2：位宽度=1（1位）
        // 参数3：阻抗值（0或1）
        setVal(3, 1, vol);  // 设置位3的值，宽度为1位，值为vol
    }

    /**
     * 设置指定通道的阻抗值（通用方法）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>通用方法，可设置任意通道的阻抗</li>
     *   <li>通道索引：0=CH1，1=CH2，2=CH3，3=CH4</li>
     *   <li>阻抗值：0=1MΩ，1=50Ω</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>批量配置多个通道阻抗</li>
     *   <li>动态配置通道阻抗</li>
     *   <li>循环遍历配置阻抗</li>
     * </ul>
     *
     * @param chIdx 通道索引（0=CH1，1=CH2，2=CH3，3=CH4）
     * @param vol 阻抗值（0=1MΩ，1=50Ω）
     */
    void setResistance(int chIdx,int vol){  // 设置指定通道阻抗方法（通用）
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=chIdx（通道索引对应的位）
        // 参数2：位宽度=1（1位）
        // 参数3：阻抗值（0或1）
        setVal(chIdx, 1, vol);  // 设置指定通道位的值，宽度为1位，值为vol
    }

    /**
     * 设置外部触发输入阻抗
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置外部触发输入端的阻抗</li>
     *   <li>阻抗值：0=1MΩ，1=50Ω</li>
     *   <li>寄存器位：位8</li>
     * </ul>
     *
     * <p><b>外部触发说明：</b>
     * <ul>
     *   <li>外部触发输入用于接收外部触发信号</li>
     *   <li>阻抗配置影响触发信号的输入特性</li>
     *   <li>高频触发信号建议使用50Ω阻抗</li>
     * </ul>
     *
     * @param val 阻抗值（0=1MΩ，1=50Ω）
     */
    void setExtTrigger(int val){  // 设置外部触发阻抗方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=8（位8）
        // 参数2：位宽度=1（1位）
        // 参数3：阻抗值（0或1）
        setVal(8,1,val);  // 设置位8的值，宽度为1位，值为val
    }

    /**
     * 设置阻抗选择配置
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置通道的阻抗选择模式</li>
     *   <li>通道索引：0=CH1，1=CH2，2=CH3，3=CH4</li>
     *   <li>寄存器位：位16-19（每位对应一个通道）</li>
     * </ul>
     *
     * <p><b>阻抗选择说明：</b>
     * <ul>
     *   <li>阻抗选择用于控制阻抗切换电路的工作模式</li>
     *   <li>不同的选择值对应不同的阻抗切换策略</li>
     *   <li>通常设置为0（默认模式）</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>阻抗切换时需要设置阻抗选择</li>
     *   <li>硬件保护机制需要配置阻抗选择</li>
     *   <li>特殊测量场景需要特定阻抗选择</li>
     * </ul>
     *
     * @param chIdx 通道索引（0=CH1，1=CH2，2=CH3，3=CH4）
     * @param v 阻抗选择值（通常为0）
     */
    void setResistanceSel(int chIdx,int v){  // 设置阻抗选择方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=16+chIdx（位16-19）
        // 参数2：位宽度=1（1位）
        // 参数3：阻抗选择值
        setVal(16+chIdx,1,v);  // 设置位(16+chIdx)的值，宽度为1位，值为v
    }

}