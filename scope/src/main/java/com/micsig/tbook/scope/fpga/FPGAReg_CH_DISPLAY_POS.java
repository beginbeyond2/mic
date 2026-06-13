package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA通道显示位置寄存器类 - 用于配置通道波形的显示位置参数
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）</li>
 *   <li>职责类型：显示配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置通道波形的垂直位置（Volm）</li>
 *   <li>配置通道波形的水平位置（Voln）</li>
 *   <li>支持多通道独立的位置配置</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供通道波形位置的精确控制</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 *   <li>支持多通道波形的独立显示和叠加显示</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_CH_DSIPLAY_POS寄存器（128位，16字节）
 * │
 * ├── 字节0-3：通道0位置配置（32位）
 * │     ├── 位0-17：Volm（垂直位置，18位）
 * │     └── 位18-28：Voln（水平位置，11位）
 * │
 * ├── 字节4-7：通道1位置配置（32位）
 * │     ├── 位0-17：Volm（垂直位置，18位）
 * │     └── 位18-28：Voln（水平位置，11位）
 * │
 * ├── 字节8-11：通道2位置配置（32位）
 * │     ├── 位0-17：Volm（垂直位置，18位）
 * │     └── 位18-28：Voln（水平位置，11位）
 * │
 * └── 字节12-15：通道3位置配置（32位）
 *       ├── 位0-17：Volm（垂直位置，18位）
 *       └── 位18-28：Voln（水平位置，11位）
 * </pre>
 *
 * <p><b>位置参数说明：</b>
 * <ul>
 *   <li>Volm（垂直位置）：控制波形在屏幕上的垂直位置，18位精度</li>
 *   <li>Voln（水平位置）：控制波形在屏幕上的水平位置，11位精度</li>
 *   <li>每个通道独立配置，支持最多4个通道</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>通道位置调整：用户手动调整通道波形的显示位置</li>
 *   <li>多通道叠加：将多个通道波形叠加显示</li>
 *   <li>自动布局：系统自动计算并配置各通道位置</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：Channel（通道类，提供位置参数）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>非线程安全，需在单线程环境中使用</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建通道显示位置寄存器实例
 * FPGAReg_CH_DISPLAY_POS regChDisplayPos = new FPGAReg_CH_DISPLAY_POS();
 *
 * // 设置通道0的垂直位置为100000，水平位置为200
 * regChDisplayPos.setVolm(0, 100000);
 * regChDisplayPos.setVoln(0, 200);
 *
 * // 设置通道1的垂直位置为150000，水平位置为300
 * regChDisplayPos.setVolm(1, 150000);
 * regChDisplayPos.setVoln(1, 300);
 *
 * // 发送寄存器配置到FPGA
 * fpgaCommand.sendCmd(regChDisplayPos);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/21
 * @see FPGAReg FPGA寄存器基类
 * @see FPGACommand FPGA命令处理类
 * @see Channel 通道类
 */
public class FPGAReg_CH_DISPLAY_POS extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 构造函数 - 初始化通道显示位置寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_CH_DSIPLAY_POS（通道显示位置寄存器）</li>
     *   <li>寄存器大小：16字节（128位）</li>
     * </ul>
     *
     * <p><b>寄存器地址说明：</b>
     * <ul>
     *   <li>FPGA_CH_DSIPLAY_POS：用于配置各通道波形的显示位置</li>
     *   <li>地址值由FPGAReg基类定义</li>
     * </ul>
     *
     * <p><b>寄存器大小说明：</b>
     * <ul>
     *   <li>16字节（128位）用于存储4个通道的位置参数</li>
     *   <li>每个通道占用4字节（32位）</li>
     * </ul>
     */
    public FPGAReg_CH_DISPLAY_POS() {  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_CH_DSIPLAY_POS：通道显示位置寄存器地址
        // 16：寄存器大小为16字节（128位）
        super(FPGA_CH_DSIPLAY_POS, 16);  // 初始化通道显示位置寄存器，地址为FPGA_CH_DSIPLAY_POS，大小为16字节
    }

    /**
     * 设置通道的垂直位置（Volm）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置指定通道波形的垂直显示位置</li>
     *   <li>寄存器位：位0-17（18位）</li>
     *   <li>影响波形在屏幕上的垂直位置</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>idx：通道索引（0=CH1，1=CH2，2=CH3，3=CH4）</li>
     *   <li>val：垂直位置值（0-262143，18位最大值）</li>
     *   <li>值越大，波形位置越靠下</li>
     * </ul>
     *
     * <p><b>垂直位置说明：</b>
     * <ul>
     *   <li>Volm用于精确控制波形的垂直位置</li>
     *   <li>18位精度提供高分辨率的位置控制</li>
     *   <li>配合垂直缩放和偏移使用</li>
     * </ul>
     *
     * @param idx 通道索引（0-3）
     * @param val 垂直位置值（0-262143）
     */
    public void setVolm(int idx, int val) {  // 设置垂直位置方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=idx（通道索引对应的字节偏移）
        // 参数2：起始位偏移=0（位0开始）
        // 参数3：位宽度=18（18位）
        // 参数4：值（垂直位置）
        setVal(idx, 0, 18, val);  // 设置指定通道的位0-17的值，宽度为18位，值为val
    }

    /**
     * 设置通道的水平位置（Voln）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置指定通道波形的水平显示位置</li>
     *   <li>寄存器位：位18-28（11位）</li>
     *   <li>影响波形在屏幕上的水平位置</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>idx：通道索引（0=CH1，1=CH2，2=CH3，3=CH4）</li>
     *   <li>val：水平位置值（0-2047，11位最大值）</li>
     *   <li>值越大，波形位置越靠右</li>
     * </ul>
     *
     * <p><b>水平位置说明：</b>
     * <ul>
     *   <li>Voln用于精确控制波形的水平位置</li>
     *   <li>11位精度提供适中的位置控制</li>
     *   <li>配合水平缩放和偏移使用</li>
     * </ul>
     *
     * @param idx 通道索引（0-3）
     * @param val 水平位置值（0-2047）
     */
    public void setVoln(int idx, int val) {  // 设置水平位置方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=idx（通道索引对应的字节偏移）
        // 参数2：起始位偏移=18（位18开始）
        // 参数3：位宽度=11（11位）
        // 参数4：值（水平位置）
        setVal(idx, 18, 11, val);  // 设置指定通道的位18-28的值，宽度为11位，值为val
    }

}