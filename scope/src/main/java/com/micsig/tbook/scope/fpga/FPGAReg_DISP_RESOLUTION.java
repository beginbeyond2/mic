package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA显示分辨率寄存器类 - 用于配置显示分辨率参数
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
 *   <li>配置显示区域的宽度分辨率</li>
 *   <li>配置显示区域的高度分辨率</li>
 *   <li>为FPGA提供显示分辨率参数</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>支持不同屏幕分辨率的适配</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 *   <li>提供统一的分辨率配置接口</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_DISP_RESOLUTION寄存器（32位，4字节）
 * │
 * ├── 位0-15：显示宽度（Width）
 * │     └── 显示区域的水平像素数（0-65535）
 * │
 * └── 位16-31：显示高度（Height）
 *       └── 显示区域的垂直像素数（0-65535）
 * </pre>
 *
 * <p><b>分辨率说明：</b>
 * <ul>
 *   <li>宽度：显示区域的水平像素数，影响波形显示的水平范围</li>
 *   <li>高度：显示区域的垂直像素数，影响波形显示的垂直范围</li>
 *   <li>分辨率决定了波形显示的精细程度</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>屏幕初始化：启动时配置显示分辨率</li>
 *   <li>分辨率切换：动态调整显示分辨率</li>
 *   <li>多屏适配：适配不同尺寸的显示屏</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：DisplayManager（显示管理类）</li>
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
 * // 创建显示分辨率寄存器实例
 * FPGAReg_DISP_RESOLUTION regResolution = new FPGAReg_DISP_RESOLUTION();
 *
 * // 设置显示宽度为800像素
 * regResolution.setWidth(800);
 *
 * // 设置显示高度为480像素
 * regResolution.setHeight(480);
 *
 * // 发送寄存器配置到FPGA
 * fpgaCommand.sendCmd(regResolution);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/21
 * @see FPGAReg FPGA寄存器基类
 * @see FPGACommand FPGA命令处理类
 */
public class FPGAReg_DISP_RESOLUTION extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 构造函数 - 初始化显示分辨率寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_DISP_RESOLUTION（显示分辨率寄存器）</li>
     *   <li>寄存器大小：4字节（32位）</li>
     * </ul>
     *
     * <p><b>寄存器地址说明：</b>
     * <ul>
     *   <li>FPGA_DISP_RESOLUTION：用于配置显示分辨率参数</li>
     *   <li>地址值由FPGAReg基类定义</li>
     * </ul>
     *
     * <p><b>寄存器大小说明：</b>
     * <ul>
     *   <li>4字节（32位）用于存储宽度和高度两个参数</li>
     *   <li>宽度占用低16位，高度占用高16位</li>
     * </ul>
     */
    public FPGAReg_DISP_RESOLUTION() {  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_DISP_RESOLUTION：显示分辨率寄存器地址
        // 4：寄存器大小为4字节（32位）
        super(FPGA_DISP_RESOLUTION, 4);  // 初始化显示分辨率寄存器，地址为FPGA_DISP_RESOLUTION，大小为4字节
    }

    /**
     * 设置显示宽度
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置显示区域的水平像素数</li>
     *   <li>寄存器位：位0-15（16位）</li>
     *   <li>影响波形显示的水平范围</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>典型值：800、1024、1280等（根据屏幕分辨率确定）</li>
     *   <li>值越大，水平显示范围越宽</li>
     *   <li>最大值：65535（16位无符号整数最大值）</li>
     * </ul>
     *
     * @param w 显示宽度（0-65535像素）
     */
    public void setWidth(int w) {  // 设置显示宽度方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=0（字节0开始）
        // 参数2：起始位偏移=0（位0开始）
        // 参数3：位宽度=16（16位）
        // 参数4：值（显示宽度）
        setVal(0, 0, 16, w);  // 设置位0-15的值，宽度为16位，值为w
    }

    /**
     * 设置显示高度
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置显示区域的垂直像素数</li>
     *   <li>寄存器位：位16-31（16位）</li>
     *   <li>影响波形显示的垂直范围</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>典型值：480、600、720等（根据屏幕分辨率确定）</li>
     *   <li>值越大，垂直显示范围越高</li>
     *   <li>最大值：65535（16位无符号整数最大值）</li>
     * </ul>
     *
     * @param h 显示高度（0-65535像素）
     */
    public void setHeight(int h) {  // 设置显示高度方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=0（字节0开始）
        // 参数2：起始位偏移=16（位16开始）
        // 参数3：位宽度=16（16位）
        // 参数4：值（显示高度）
        setVal(0, 16, 16, h);  // 设置位16-31的值，宽度为16位，值为h
    }

}