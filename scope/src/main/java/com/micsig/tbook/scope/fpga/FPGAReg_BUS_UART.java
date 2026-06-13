package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口类，定义总线通用接口
import com.micsig.tbook.scope.Bus.UartBus;  // 导入UART总线类，提供UART总线配置参数
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入通道工厂类，获取通道数量信息

/**
 * FPGA UART总线寄存器类 - 用于配置UART总线解码和触发参数
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg_BUS基类）</li>
 *   <li>职责类型：UART总线配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置UART总线的信号源</li>
 *   <li>配置UART波特率</li>
 *   <li>配置UART数据位宽、校验位、停止位</li>
 *   <li>配置UART触发模式和触发条件</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供UART总线解码的硬件配置接口</li>
 *   <li>支持多种UART数据格式（5/6/7/8/9位）</li>
 *   <li>支持多种校验方式（无校验、奇校验、偶校验）</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * UART总线寄存器（160位，20字节）
 * │
 * ├── 字节0：信号源配置
 * │     └── 位0-1：UART信号源选择（0-3）
 * │
 * ├── 字节1：数据格式配置
 * │     ├── 位0：停止位（BS）
 * │     ├── 位1-3：数据位宽（BL）
 * │     └── 位4-5：校验方式（BV）
 * │
 * ├── 字节2：触发配置
 * │     ├── 位0-3：触发模式
 * │     ├── 位8-15：触发数据
 * │     └── 位16-17：触发关系
 * │
 * └── 字节4：波特率配置
 *       └── 位0-27：波特率参数（Rate）
 * </pre>
 *
 * <p><b>UART数据位宽映射：</b>
 * <ul>
 *   <li>8位：BL=0（默认）</li>
 *   <li>5位：BL=1</li>
 *   <li>6位：BL=2</li>
 *   <li>7位：BL=3</li>
 *   <li>9位：BL=7</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>UART总线解码：配置UART信号源和波特率</li>
 *   <li>UART协议触发：设置数据触发条件</li>
 *   <li>串口调试分析：分析UART通信数据</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg_BUS（总线寄存器基类）</li>
 *   <li>依赖：UartBus（UART总线配置类）</li>
 *   <li>依赖：FPGAReg_BUS_LEVEL（总线电平寄存器）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 * </ul>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-30
 * @see FPGAReg_BUS 总线寄存器基类
 * @see UartBus UART总线配置类
 * @see FPGAReg_BUS_LEVEL 总线电平寄存器
 */
public class FPGAReg_BUS_UART extends FPGAReg_BUS {  // 继承FPGAReg_BUS基类，复用总线配置方法

    /**
     * 构造函数 - 初始化UART总线寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器大小：20字节（160位）</li>
     *   <li>支持多种UART数据格式</li>
     * </ul>
     *
     * @param addr 寄存器地址（FPGA_BUS1_ADDR或FPGA_BUS2_ADDR）
     */
    public FPGAReg_BUS_UART(int addr) {  // 构造函数，接收寄存器地址
        // 调用父类构造函数，传入寄存器地址和大小
        super(addr, 20);  // 初始化UART总线寄存器，地址由参数指定，大小为20字节
    }

    /**
     * 设置信号源 - 配置UART总线的信号源
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置UART总线信号源选择</li>
     *   <li>寄存器位：位0-1（2位）</li>
     *   <li>信号源值：0-3，对应不同的通道</li>
     * </ul>
     *
     * @param val 信号源选择值（0-3）
     */
    public void setSrc(int val) {  // 设置信号源方法
        // 调用父类setVal方法，设置寄存器值
        setVal(0, 2, val);  // 设置位0-1的值，宽度为2位，值为val（信号源）
    }

    /**
     * 设置波特率 - 配置UART波特率参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置UART波特率参数</li>
     *   <li>寄存器位：位0-27（28位）</li>
     *   <li>波特率参数 = 1e9 / baud_rate / 8 - 1</li>
     * </ul>
     *
     * <p><b>波特率计算：</b>
     * <ul>
     *   <li>基准时钟：1GHz（1e9）</li>
     *   <li>波特率参数 = 1e9 / baud_rate / 8 - 1</li>
     *   <li>除以8是因为FPGA内部时钟分频</li>
     * </ul>
     *
     * @param val 波特率参数值（0-268435455）
     */
    public void setRate(int val) {  // 设置波特率方法
        // 调用父类setVal方法，设置寄存器值
        setVal(4, 28, val);  // 设置位0-27的值，宽度为28位，值为val（波特率参数）
    }

    /**
     * 设置停止位 - 配置UART停止位
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置UART停止位数量</li>
     *   <li>寄存器位：位0（1位）</li>
     *   <li>0：1个停止位，1：2个停止位</li>
     * </ul>
     *
     * @param val 停止位值（0=1位，1=2位）
     */
    public void setBS(int val) {  // 设置停止位方法
        // 调用父类setVal方法，设置寄存器值
        setVal(1, 0, 1, val);  // 设置位0的值，宽度为1位，值为val（停止位）
    }

    /**
     * 设置数据位宽 - 配置UART数据位宽
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置UART数据位宽</li>
     *   <li>寄存器位：位1-3（3位）</li>
     *   <li>支持5/6/7/8/9位数据宽度</li>
     * </ul>
     *
     * <p><b>数据位宽映射：</b>
     * <ul>
     *   <li>8位：BL=0（默认）</li>
     *   <li>5位：BL=1</li>
     *   <li>6位：BL=2</li>
     *   <li>7位：BL=3</li>
     *   <li>9位：BL=7</li>
     * </ul>
     *
     * @param val 数据位宽值（0-7）
     */
    public void setBL(int val) {  // 设置数据位宽方法
        // 调用父类setVal方法，设置寄存器值
        setVal(1, 1, 3, val);  // 设置位1-3的值，宽度为3位，值为val（数据位宽）
    }

    /**
     * 设置校验方式 - 配置UART校验位
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置UART校验方式</li>
     *   <li>寄存器位：位4-5（2位）</li>
     *   <li>支持无校验、奇校验、偶校验</li>
     * </ul>
     *
     * <p><b>校验方式映射：</b>
     * <ul>
     *   <li>0：无校验（None）</li>
     *   <li>1：奇校验（Odd）</li>
     *   <li>2：偶校验（Even）</li>
     * </ul>
     *
     * @param val 校验方式值（0-2）
     */
    public void setBV(int val) {  // 设置校验方式方法
        // 调用父类setVal方法，设置寄存器值
        setVal(1, 4, 2, val);  // 设置位4-5的值，宽度为2位，值为val（校验方式）
    }

    /**
     * 设置触发模式 - 配置UART触发类型
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置UART总线的触发模式</li>
     *   <li>寄存器位：位0-3（4位）</li>
     *   <li>支持起始位、数据等触发类型</li>
     * </ul>
     *
     * @param val 触发模式值（0-15）
     */
    public void setTriggerMode(int val) {  // 设置触发模式方法
        // 调用父类setVal方法，设置寄存器值
        setVal(2, 0, 4, val);  // 设置位0-3的值，宽度为4位，值为val（触发模式）
    }

    /**
     * 设置触发数据 - 配置UART触发数据值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置UART触发条件的数据值</li>
     *   <li>寄存器位：位8-15（8位）</li>
     *   <li>用于数据触发条件</li>
     * </ul>
     *
     * @param val 触发数据值（0-255）
     */
    public void setTriggerData(int val) {  // 设置触发数据方法
        // 调用父类setVal方法，设置寄存器值
        setVal(2, 8, 8, val);  // 设置位8-15的值，宽度为8位，值为val（触发数据）
    }

    /**
     * 设置触发关系 - 配置UART触发关系
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置UART触发条件的关系值</li>
     *   <li>寄存器位：位16-17（2位）</li>
     *   <li>用于复杂触发条件</li>
     * </ul>
     *
     * @param val 触发关系值（0-3）
     */
    public void setTriggerRelation(int val) {  // 设置触发关系方法
        // 调用父类setVal方法，设置寄存器值
        setVal(2, 16, 2, val);  // 设置位16-17的值，宽度为2位，值为val（触发关系）
    }

    /**
     * 配置UART总线 - 实现总线配置接口
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从UartBus对象中获取配置参数</li>
     *   <li>配置信号源和波特率</li>
     *   <li>配置数据格式（位宽、校验、停止位）</li>
     *   <li>配置触发条件和总线电平</li>
     * </ul>
     *
     * @param fpgaIdx FPGA芯片索引（多FPGA架构时使用）
     * @param iBus UART总线接口对象，包含UART配置参数
     */
    @Override
    public void configBus(int fpgaIdx, IBus iBus) {  // 配置UART总线方法，实现抽象方法
        
        // 初始化数据位宽变量
        int iBitWidth = 0;  // 数据位宽值，初始为0
        
        // 将IBus接口转换为UartBus具体类型
        UartBus bus = (UartBus) iBus;  // 类型转换，获取UART总线对象
        
        // 获取RX通道索引（接收线通道）
        int src = bus.getRxChIdx();  // 获取RX信号所在的通道索引
        
        // 获取总线电平寄存器对象
        FPGAReg_BUS_LEVEL busLevel = (FPGAReg_BUS_LEVEL) FPGACommand.getReg(fpgaIdx, FPGA_BUS_LEVEL);  // 获取总线电平寄存器
        
        // 设置信号源
        setSrc(chIdx2BusSrc(src));  // 设置信号源，使用通道索引转总线源方法
        
        // 配置总线源扩展
        busLevel.setBusSrcExt(getBusIdx(), ((src >> 2) & 0x1));  // 设置总线源扩展
        
        // 设置波特率参数
        // 波特率参数 = 1e9 / baud_rate / 8 - 1
        setRate((int) (1e9 / bus.getBaudRate() / 8 - 1));  // 设置波特率参数
        
        // 设置停止位（固定为0，表示1个停止位）
        setBS(0);  // 设置停止位为1位
        
        // 根据数据位宽设置BL值
        switch (bus.getBits()) {  // 根据位宽分支处理
            default:  // 默认情况
            case 8:  // 8位数据宽度
                iBitWidth = 0;  // 设置BL为0（8位）
                break;  // 结束该分支
            case 5:  // 5位数据宽度
                iBitWidth = 1;  // 设置BL为1（5位）
                break;  // 结束该分支
            case 6:  // 6位数据宽度
                iBitWidth = 2;  // 设置BL为2（6位）
                break;  // 结束该分支
            case 7:  // 7位数据宽度
                iBitWidth = 3;  // 设置BL为3（7位）
                break;  // 结束该分支
            case 9:  // 9位数据宽度
                iBitWidth = 7;  // 设置BL为7（9位）
                break;  // 结束该分支
        }
        
        // 设置数据位宽
        setBL(iBitWidth);  // 设置数据位宽
        
        // 设置校验方式
        setBV(bus.getVerify());  // 设置校验方式，从UART总线对象获取
        
        // 设置触发模式
        setTriggerMode(bus.getTriggerType());  // 设置触发模式
        
        // 设置触发数据
        setTriggerData(bus.getTriggerData());  // 设置触发数据
        
        // 设置触发关系
        setTriggerRelation(bus.getTriggerRelation());  // 设置触发关系
        
        // 设置总线电平
        setLevel(busLevel, src, bus.getIdleLevel());  // 设置总线电平，使用UART空闲电平
        
        // 发送总线电平寄存器到FPGA
        FPGACommand.sendCmd(busLevel);  // 发送总线电平配置命令到FPGA
    }

}