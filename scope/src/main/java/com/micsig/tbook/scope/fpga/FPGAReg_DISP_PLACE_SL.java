package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA显示位置缩略寄存器类 - 用于配置缩略视图的显示位置参数
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
 *   <li>配置缩略视图每行的显示数量</li>
 *   <li>配置倒数行号参数</li>
 *   <li>配置Y轴耦合位置</li>
 *   <li>配置Y轴耦合使能</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>支持缩略视图（Thumbnail View）的显示布局配置</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 *   <li>提供统一的显示位置配置接口</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_DISP_PLACE_SL寄存器（96位，12字节）
 * │
 * ├── 字节0：每行显示数量（NumsPerLie）
 * │     └── 控制缩略视图中每行显示的波形数量
 * │
 * ├── 字节1：倒数行号（DaoShuLieNum）
 * │     └── 用于计算显示位置的倒数行号参数
 * │
 * └── 字节2-3：Y轴耦合配置（16位）
 *       ├── 位0-14：Y轴耦合位置（NumCouY）
 *       └── 位15：Y轴耦合使能（NeedCouY）
 * </pre>
 *
 * <p><b>缩略视图说明：</b>
 * <ul>
 *   <li>缩略视图用于在有限屏幕空间内显示多个波形</li>
 *   <li>每行显示数量决定了缩略图的排列方式</li>
 *   <li>Y轴耦合用于同步多个通道的垂直位置</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>缩略视图布局配置：设置每行显示的波形数量</li>
 *   <li>多通道显示同步：配置Y轴耦合参数</li>
 *   <li>显示位置调整：动态调整缩略视图的显示布局</li>
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
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建显示位置缩略寄存器实例
 * FPGAReg_DISP_PLACE_SL regDispPlaceSL = new FPGAReg_DISP_PLACE_SL();
 *
 * // 设置每行显示4个波形
 * regDispPlaceSL.setNumsPerLie(4);
 *
 * // 设置倒数行号为2
 * regDispPlaceSL.setDaoShuLieNum(2);
 *
 * // 设置Y轴耦合位置为100
 * regDispPlaceSL.setNumCouY(100);
 *
 * // 启用Y轴耦合
 * regDispPlaceSL.setNeedCouY(1);
 *
 * // 发送寄存器配置到FPGA
 * fpgaCommand.sendCmd(regDispPlaceSL);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/21
 * @see FPGAReg FPGA寄存器基类
 * @see FPGACommand FPGA命令处理类
 */
public class FPGAReg_DISP_PLACE_SL extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 构造函数 - 初始化显示位置缩略寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_DISP_PLACE_SL（显示位置缩略寄存器）</li>
     *   <li>寄存器大小：12字节（96位）</li>
     * </ul>
     *
     * <p><b>寄存器地址说明：</b>
     * <ul>
     *   <li>FPGA_DISP_PLACE_SL：用于配置缩略视图的显示位置参数</li>
     *   <li>地址值由FPGAReg基类定义</li>
     * </ul>
     *
     * <p><b>寄存器大小说明：</b>
     * <ul>
     *   <li>12字节（96位）用于存储多个显示位置参数</li>
     *   <li>包括每行数量、倒数行号、Y轴耦合配置等</li>
     * </ul>
     */
    public FPGAReg_DISP_PLACE_SL(){  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_DISP_PLACE_SL：显示位置缩略寄存器地址
        // 12：寄存器大小为12字节（96位）
        super(FPGAReg.FPGA_DISP_PLACE_SL, 12);  // 初始化显示位置缩略寄存器，地址为FPGA_DISP_PLACE_SL，大小为12字节
    }

    /**
     * 设置每行显示数量
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置缩略视图中每行显示的波形数量</li>
     *   <li>寄存器位：字节0（8位）</li>
     *   <li>影响缩略视图的布局排列</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>典型值：2、4、8等（根据屏幕宽度确定）</li>
     *   <li>值越大，每行显示的波形越多，每个波形越窄</li>
     * </ul>
     *
     * @param val 每行显示数量（0-255）
     */
    public void setNumsPerLie(int val){  // 设置每行显示数量方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=0（字节0）
        // 参数2：值（每行显示数量）
        setVal(0, val);  // 设置字节0的值，值为val
    }

    /**
     * 设置倒数行号
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置倒数行号参数</li>
     *   <li>寄存器位：字节1（8位）</li>
     *   <li>用于计算显示位置的倒数行号</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>倒数行号用于从底部开始计算行位置</li>
     *   <li>配合每行数量使用，确定波形的具体显示位置</li>
     * </ul>
     *
     * @param val 倒数行号（0-255）
     */
    public void setDaoShuLieNum(int val){  // 设置倒数行号方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=1（字节1）
        // 参数2：值（倒数行号）
        setVal(1, val);  // 设置字节1的值，值为val
    }

    /**
     * 设置Y轴耦合位置
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置Y轴耦合的位置值</li>
     *   <li>寄存器位：位0-14（15位）</li>
     *   <li>用于多通道显示时的垂直位置同步</li>
     * </ul>
     *
     * <p><b>Y轴耦合说明：</b>
     * <ul>
     *   <li>Y轴耦合用于同步多个通道的垂直位置</li>
     *   <li>使能后，各通道的垂直位置会根据耦合位置进行调整</li>
     *   <li>确保多通道波形在垂直方向上对齐</li>
     * </ul>
     *
     * @param val Y轴耦合位置值（0-32767）
     */
    public void setNumCouY(int val){  // 设置Y轴耦合位置方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=2（字节2开始）
        // 参数2：起始位偏移=0（位0开始）
        // 参数3：位宽度=15（15位）
        // 参数4：值（Y轴耦合位置）
        setVal(2, 0, 15, val);  // 设置位0-14的值，宽度为15位，值为val
    }

    /**
     * 设置Y轴耦合使能
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>启用或禁用Y轴耦合功能</li>
     *   <li>寄存器位：位15（1位）</li>
     *   <li>控制是否启用Y轴耦合</li>
     * </ul>
     *
     * <p><b>使能说明：</b>
     * <ul>
     *   <li>0：禁用Y轴耦合，各通道独立显示</li>
     *   <li>1：启用Y轴耦合，各通道垂直位置同步</li>
     * </ul>
     *
     * @param val 使能值（0=禁用，1=启用）
     */
    public void setNeedCouY(int val){  // 设置Y轴耦合使能方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=2（字节2开始）
        // 参数2：起始位偏移=15（位15）
        // 参数3：位宽度=1（1位）
        // 参数4：值（使能值）
        setVal(2, 15, 1, val);  // 设置位15的值，宽度为1位，值为val
    }

}