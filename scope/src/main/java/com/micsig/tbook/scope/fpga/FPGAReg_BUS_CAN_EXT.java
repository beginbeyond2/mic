package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import android.util.Log;  // 导入Android日志类，用于调试输出

import com.micsig.tbook.scope.Bus.CanBus;  // 导入CAN总线类，提供CAN总线配置参数
import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口类，定义总线通用接口

/**
 * FPGA CAN扩展总线寄存器类 - 用于配置CAN FD波特率参数
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）</li>
 *   <li>职责类型：CAN FD波特率配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置CAN FD高速数据阶段的波特率</li>
 *   <li>配置CAN FD采样点位置</li>
 *   <li>辅助0x32命令发送CAN FD配置信息</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供CAN FD波特率的独立配置接口</li>
 *   <li>支持CAN FD高速数据传输</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * CAN扩展总线寄存器（160位，20字节）
 * │
 * ├── 位0-13：采样点位置（SamplePlace）
 * │     └── CAN FD数据阶段的采样点位置
 * │
 * ├── 位14-27：波特率分频值（Baud）
 * │     └── CAN FD数据阶段的波特率分频值
 * │
 * ├── 位24-27：波特率参数N3（BaudN3）
 * │     └── CAN FD波特率精细调节参数
 * │
 * └── 位28-31：波特率选择（Sel）
 *       └── 波特率组选择值
 * </pre>
 *
 * <p><b>CAN FD波特率计算：</b>
 * <ul>
 *   <li>基准时钟：125MHz</li>
 *   <li>波特率分频值 = 125000000 / bit_rate - 1</li>
 *   <li>采样点位置 = 125000000 / bit_rate * s - 1</li>
 *   <li>波特率参数N = 16 * (125000000 / bit_rate - baud - 1)</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>CAN FD波特率配置：配置高速数据阶段的波特率</li>
 *   <li>CAN FD调试：调试CAN FD通信问题</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：CanBus（CAN总线配置类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>非线程安全，需在单线程环境中使用</li>
 * </ul>
 *
 * @author limh
 * @version 1.0
 * @since 2024-12-23
 * @see FPGAReg FPGA寄存器基类
 * @see CanBus CAN总线配置类
 */
public class FPGAReg_BUS_CAN_EXT extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 构造函数 - 初始化CAN扩展总线寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器大小：20字节（160位）</li>
     *   <li>用于CAN FD波特率配置</li>
     * </ul>
     *
     * @param addr 寄存器地址
     */
    public FPGAReg_BUS_CAN_EXT(int addr) {  // 构造函数，接收寄存器地址
        // 调用父类构造函数，传入寄存器地址和大小
        super(addr, 20);  // 初始化CAN扩展总线寄存器，地址由参数指定，大小为20字节
    }

    /**
     * 设置采样点位置 - 配置CAN FD采样点位置
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN FD数据阶段的采样点位置</li>
     *   <li>寄存器位：位0-13（14位）</li>
     *   <li>影响CAN FD位定时参数</li>
     * </ul>
     *
     * @param val 采样点位置值（0-16383）
     */
    public void setSamplePlace(int val) {  // 设置采样点位置方法
        // 调用父类setVal方法，设置寄存器值
        setVal(0, 14, val);  // 设置位0-13的值，宽度为14位，值为val（采样点位置）
    }

    /**
     * 设置波特率分频值 - 配置CAN FD波特率分频
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN FD数据阶段的波特率分频值</li>
     *   <li>寄存器位：位14-27（14位）</li>
     *   <li>决定CAN FD位时间长度</li>
     * </ul>
     *
     * @param val 波特率分频值（0-16383）
     */
    public void setBaud(int val) {  // 设置波特率分频值方法
        // 调用父类setVal方法，设置寄存器值
        setVal(14, 14, val);  // 设置位14-27的值，宽度为14位，值为val（波特率分频值）
    }

    /**
     * 设置波特率参数N3 - 配置CAN FD波特率参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN FD波特率精细调节参数</li>
     *   <li>寄存器位：位24-27（4位）</li>
     *   <li>用于波特率微调</li>
     * </ul>
     *
     * @param val 波特率参数N3值（0-15）
     */
    public void setBaudN3(int val) {  // 设置波特率参数N3方法
        // 调用父类setVal方法，设置寄存器值
        setVal(24, 4, val);  // 设置位24-27的值，宽度为4位，值为val（波特率参数N3）
    }

    /**
     * 设置波特率选择 - 配置波特率组选择
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置波特率组选择值</li>
     *   <li>寄存器位：位28-31（4位）</li>
     *   <li>用于多波特率配置选择</li>
     * </ul>
     *
     * @param val 波特率选择值（0-15）
     */
    public void setSel(int val) {  // 设置波特率选择方法
        // 调用父类setVal方法，设置寄存器值
        setVal(28, 4, val);  // 设置位28-31的值，宽度为4位，值为val（波特率选择）
    }

    /**
     * 配置总线 - 配置CAN FD波特率参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从CanBus对象中获取CAN FD波特率参数</li>
     *   <li>计算并设置波特率分频值和采样点位置</li>
     *   <li>发送配置到FPGA</li>
     * </ul>
     *
     * @param fpgaIdx FPGA芯片索引（多FPGA架构时使用）
     * @param iBus CAN总线接口对象，包含CAN FD配置参数
     */
    public void configBus(int fpgaIdx, IBus iBus) {  // 配置总线方法
        // 获取总线类型
        int busType = iBus.getBusType();  // 获取总线类型
        
        // 判断是否为CAN总线且总线类型有效
        if (iBus instanceof CanBus && IBus.isValid(busType)) {  // 如果是CAN总线且类型有效
            // 将IBus接口转换为CanBus具体类型
            CanBus bus = (CanBus) iBus;  // 类型转换，获取CAN总线对象
            
            // 设置CAN FD波特率参数
            // 参数1：CAN FD波特率
            // 参数2：波特率组选择值（2表示CAN FD）
            // 参数3：采样点位置比例
            Bus_Can_setBaudRate(bus.getFDBandRate(), 2, bus.getSamplePlace2());  // 设置CAN FD波特率
            
            // 输出调试日志
            Log.d("FPGAReg", "fpgaIdx:" + fpgaIdx + ",bus:" + Integer.toHexString(this.getAddr()));  // 打印FPGA索引和寄存器地址
            
            // 打印寄存器内容（调试用）
            this.Dump();  // 打印寄存器内容
            
            // 发送寄存器配置到FPGA
            FPGACommand.sendCmd(this);  // 发送CAN FD配置命令到FPGA
        }
    }

    /**
     * 设置CAN波特率 - 内部方法，计算并设置波特率参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据波特率和采样点计算寄存器参数</li>
     *   <li>设置波特率分频值和采样点位置</li>
     *   <li>支持CAN FD高速波特率配置</li>
     * </ul>
     *
     * <p><b>计算公式：</b>
     * <ul>
     *   <li>波特率分频值 = 125000000 / bit_rate - 1</li>
     *   <li>采样点位置 = 125000000 / bit_rate * s - 1</li>
     *   <li>波特率参数N = 16 * (125000000 / bit_rate - baud - 1)</li>
     * </ul>
     *
     * @param bit_rate CAN FD波特率（bps）
     * @param val 波特率组选择值
     * @param s 采样点位置比例（0.0-1.0）
     */
    private void Bus_Can_setBaudRate(int bit_rate, int val, double s) {  // 设置CAN波特率内部方法
        // 初始化波特率参数
        int baud = 0;  // 波特率分频值，初始为0
        int samplePlace = 0;  // 采样点位置，初始为0
        int baudN = 0;  // 波特率参数N，初始为0
        
        // 计算波特率参数（如果波特率>0）
        if (bit_rate > 0) {  // 如果波特率大于0
            // 计算波特率分频值：125MHz / bit_rate - 1
            baud = ((int) (125000000.0 / bit_rate)) - 1;  // 计算波特率分频值
            
            // 计算采样点位置：125MHz / bit_rate * s - 1
            samplePlace = ((int) (125000000.0 / bit_rate * s)) - 1;  // 计算采样点位置
            
            // 计算波特率参数N：16 * (125MHz / bit_rate - baud - 1)
            baudN = (int) (16 * (125000000.0 / bit_rate - baud - 1));  // 计算波特率参数N
        }
        
        // 设置波特率分频值
        setBaud(baud);  // 设置波特率分频值
        
        // 设置采样点位置
        setSamplePlace(samplePlace);  // 设置采样点位置
        
        // 注意：此处未设置波特率参数N3，可能是代码注释错误
        // setBaudN3(baudN);  // 设置波特率参数N3（注释掉的代码）
        
        // 设置波特率选择
        setSel(val);  // 设置波特率选择值
    }

}