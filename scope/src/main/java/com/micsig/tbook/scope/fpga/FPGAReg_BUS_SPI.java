package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口类，定义总线通用接口
import com.micsig.tbook.scope.Bus.SpiBus;  // 导入SPI总线类，提供SPI总线配置参数

/**
 * FPGA SPI总线寄存器类 - 用于配置SPI总线解码和触发参数
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg_BUS基类）</li>
 *   <li>职责类型：SPI总线配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置SPI总线的CS（片选）信号源</li>
 *   <li>配置SPI总线的SCK（时钟）信号源</li>
 *   <li>配置SPI总线的数据信号源</li>
 *   <li>配置SPI数据位宽和触发条件</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供SPI总线解码的硬件配置接口</li>
 *   <li>支持多种SPI数据位宽（4/8/16/24/32位）</li>
 *   <li>支持SPI帧数据触发和模式触发</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * SPI总线寄存器（160位，20字节）
 * │
 * ├── 字节0：信号源配置
 * │     ├── 位0-1：CS信号源选择（0-3）
 * │     └── 位2-3：SCK信号源选择（0-3）
 * │
 * ├── 字节1：触发配置
 * │     ├── 位0-2：触发模式（0-7）
 * │     └── 位3-7：扩展配置
 * │
 * ├── 字节2：数据源和触发掩码
 * │     ├── 位0-1：数据信号源选择（0-3）
 * │     └── 位8-15：触发掩码值
 * │
 * ├── 字节3：触发值
 * │     └── 位0-7：触发数据值
 * │
 * ├── 字节4-15：波特率和扩展配置
 * │     ├── 位16-18：数据位宽（BW）
 * │     ├── 位20：时钟空闲电平
 * │     └── 位21：CS使能
 * │
 * └── 字节16-19：扩展配置区
 * </pre>
 *
 * <p><b>SPI数据位宽说明：</b>
 * <ul>
 *   <li>4位：BW=0</li>
 *   <li>8位：BW=1（默认）</li>
 *   <li>16位：BW=2</li>
 *   <li>24位：BW=3</li>
 *   <li>32位：BW=4</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>SPI总线解码：配置SPI信号源和触发条件</li>
 *   <li>SPI协议触发：设置数据、掩码触发条件</li>
 *   <li>SPI调试分析：分析SPI通信数据</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg_BUS（总线寄存器基类）</li>
 *   <li>依赖：SpiBus（SPI总线配置类）</li>
 *   <li>依赖：FPGAReg_BUS_LEVEL（总线电平寄存器）</li>
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
 * // 创建SPI总线寄存器实例
 * FPGAReg_BUS_SPI regSPI = new FPGAReg_BUS_SPI(FPGA_BUS1_ADDR);
 *
 * // 配置SPI总线参数
 * regSPI.configBus(fpgaIdx, spiBus);
 *
 * // 发送寄存器配置到FPGA
 * FPGACommand.sendCmd(regSPI);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-30
 * @see FPGAReg_BUS 总线寄存器基类
 * @see SpiBus SPI总线配置类
 * @see FPGAReg_BUS_LEVEL 总线电平寄存器
 */
public class FPGAReg_BUS_SPI extends FPGAReg_BUS {  // 继承FPGAReg_BUS基类，复用总线配置方法

    /**
     * 构造函数 - 初始化SPI总线寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器大小：20字节（160位）</li>
     *   <li>支持SPI四线制信号配置</li>
     * </ul>
     *
     * @param addr 寄存器地址（FPGA_BUS1_ADDR或FPGA_BUS2_ADDR）
     */
    public FPGAReg_BUS_SPI(int addr) {  // 构造函数，接收寄存器地址
        // 调用父类构造函数，传入寄存器地址和大小
        // addr：寄存器地址（总线1或总线2）
        // 20：寄存器大小为20字节（160位）
        super(addr, 20);  // 初始化SPI总线寄存器，地址由参数指定，大小为20字节
    }

    /**
     * 设置CS信号源 - 配置SPI片选线的信号源
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置SPI总线CS（片选）的信号源选择</li>
     *   <li>寄存器位：位0-1（2位）</li>
     *   <li>信号源值：0-3，对应不同的通道</li>
     * </ul>
     *
     * <p><b>CS信号说明：</b>
     * <ul>
     *   <li>CS是SPI总线的片选信号</li>
     *   <li>用于选择通信的目标设备</li>
     *   <li>通常为低电平有效</li>
     * </ul>
     *
     * @param val CS信号源选择值（0-3）
     */
    // CS源选择
    public void setCS(int val) {  // 设置CS信号源方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=0（字节0）
        // 参数2：位宽度=2（2位）
        // 参数3：信号源值
        setVal(0, 2, val);  // 设置位0-1的值，宽度为2位，值为val（CS信号源）
    }

    /**
     * 设置SCK信号源 - 配置SPI时钟线的信号源
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置SPI总线SCK（时钟）的信号源选择</li>
     *   <li>寄存器位：位2-3（2位）</li>
     *   <li>信号源值：0-3，对应不同的通道</li>
     * </ul>
     *
     * <p><b>SCK信号说明：</b>
     * <ul>
     *   <li>SCK是SPI总线的时钟信号</li>
     *   <li>由主设备产生时钟信号</li>
     *   <li>时钟极性和相位决定采样时机</li>
     * </ul>
     *
     * @param val SCK信号源选择值（0-3）
     */
    // SCK源选择
    public void setSCK(int val) {  // 设置SCK信号源方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=2（字节0，位2开始）
        // 参数2：位宽度=2（2位）
        // 参数3：信号源值
        setVal(2, 2, val);  // 设置位2-3的值，宽度为2位，值为val（SCK信号源）
    }

    /**
     * 设置数据信号源 - 配置SPI数据线的信号源
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置SPI总线数据线（MISO/MOSI）的信号源选择</li>
     *   <li>寄存器位：位4-5（2位）</li>
     *   <li>信号源值：0-3，对应不同的通道</li>
     * </ul>
     *
     * <p><b>数据信号说明：</b>
     * <ul>
     *   <li>MOSI：主设备输出，从设备输入</li>
     *   <li>MISO：主设备输入，从设备输出</li>
     *   <li>根据配置选择其中一个作为数据源</li>
     * </ul>
     *
     * @param val 数据信号源选择值（0-3）
     */
    // 数据口源选择
    public void setDSrc(int val) {  // 设置数据信号源方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=4（字节0，位4开始）
        // 参数2：位宽度=2（2位）
        // 参数3：信号源值
        setVal(4, 2, val);  // 设置位4-5的值，宽度为2位，值为val（数据信号源）
    }

    /**
     * 设置数据位宽 - 配置SPI数据字长
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置SPI数据传输的字长（位宽）</li>
     *   <li>寄存器位：位16-18（3位）</li>
     *   <li>支持4/8/16/24/32位数据宽度</li>
     * </ul>
     *
     * <p><b>位宽映射：</b>
     * <ul>
     *   <li>0：4位数据宽度</li>
     *   <li>1：8位数据宽度（默认）</li>
     *   <li>2：16位数据宽度</li>
     *   <li>3：24位数据宽度</li>
     *   <li>4：32位数据宽度</li>
     * </ul>
     *
     * @param val 数据位宽值（0-4）
     */
    // 数据字长
    public void setBW(int val) {  // 设置数据位宽方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=16（字节2，位16开始）
        // 参数2：位宽度=3（3位）
        // 参数3：位宽值
        setVal(16, 3, val);  // 设置位16-18的值，宽度为3位，值为val（数据位宽）
    }

    /**
     * 设置时钟空闲电平 - 配置SPI时钟空闲时的电平
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置SPI时钟空闲时的电平状态</li>
     *   <li>寄存器位：位20（1位）</li>
     *   <li>影响SPI时钟极性（CPOL）</li>
     * </ul>
     *
     * <p><b>空闲电平说明：</b>
     * <ul>
     *   <li>0：空闲时为低电平（CPOL=0）</li>
     *   <li>1：空闲时为高电平（CPOL=1）</li>
     * </ul>
     *
     * @param val 时钟空闲电平（0=低，1=高）
     */
    public void setClkIdle(int val) {  // 设置时钟空闲电平方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=20（字节2，位20开始）
        // 参数2：位宽度=1（1位）
        // 参数3：空闲电平值
        setVal(20, 1, val);  // 设置位20的值，宽度为1位，值为val（时钟空闲电平）
    }

    /**
     * 设置CS使能 - 配置是否启用CS片选信号
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置是否启用SPI片选信号</li>
     *   <li>寄存器位：位21（1位）</li>
     *   <li>某些SPI模式不需要CS信号</li>
     * </ul>
     *
     * <p><b>CS使能说明：</b>
     * <ul>
     *   <li>0：禁用CS信号</li>
     *   <li>1：启用CS信号</li>
     * </ul>
     *
     * @param val CS使能值（0=禁用，1=启用）
     */
    public void setEanbleCS(int val) {  // 设置CS使能方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=21（字节2，位21开始）
        // 参数2：位宽度=1（1位）
        // 参数3：使能值
        setVal(21, 1, val);  // 设置位21的值，宽度为1位，值为val（CS使能）
    }

    /**
     * 设置触发模式 - 配置SPI触发类型
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置SPI总线的触发模式</li>
     *   <li>寄存器位：位0-2（3位）</li>
     *   <li>支持帧数据触发和模式触发</li>
     * </ul>
     *
     * <p><b>触发模式说明：</b>
     * <ul>
     *   <li>SPI_TRIGGER_FRAME_DATA：帧数据触发</li>
     *   <li>SPI_TRIGGER_FRAME_X_DATA：扩展帧数据触发</li>
     * </ul>
     *
     * @param val 触发模式值（0-7）
     */
    public void setTriggerMode(int val) {  // 设置触发模式方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=1（字节1）
        // 参数2：起始位偏移=0（位0开始）
        // 参数3：位宽度=3（3位）
        // 参数4：触发模式值
        setVal(1, 0, 3, val);  // 设置字节1的位0-2的值，宽度为3位，值为val（触发模式）
    }

    /**
     * 设置触发掩码 - 配置SPI触发数据掩码
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置SPI触发数据的掩码值</li>
     *   <li>寄存器位：字节2（8位）</li>
     *   <li>掩码用于屏蔽某些位的触发条件</li>
     * </ul>
     *
     * <p><b>掩码说明：</b>
     * <ul>
     *   <li>掩码位为1：该位参与触发判断</li>
     *   <li>掩码位为0：该位不参与触发判断</li>
     *   <li>用于实现灵活的触发条件</li>
     * </ul>
     *
     * @param val 触发掩码值（0-255）
     */
    public void setTriggerMask(int val) {  // 设置触发掩码方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=2（字节2）
        // 参数2：掩码值
        setVal(2, val);  // 设置字节2的值，值为val（触发掩码）
    }

    /**
     * 设置触发值 - 配置SPI触发数据值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置SPI触发条件的数据值</li>
     *   <li>寄存器位：字节3（8位）</li>
     *   <li>与掩码配合使用，实现精确触发</li>
     * </ul>
     *
     * <p><b>触发值说明：</b>
     * <ul>
     *   <li>触发值与掩码进行AND操作</li>
     *   <li>结果与接收数据比较</li>
     *   <li>匹配时产生触发</li>
     * </ul>
     *
     * @param val 触发数据值（0-255）
     */
    public void setTriggerVal(int val) {  // 设置触发值方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=3（字节3）
        // 参数2：触发值
        setVal(3, val);  // 设置字节3的值，值为val（触发值）
    }

    /**
     * 配置SPI总线 - 实现总线配置接口
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从SpiBus对象中获取配置参数</li>
     *   <li>配置CS、SCK、数据信号源</li>
     *   <li>配置数据位宽和触发条件</li>
     *   <li>配置总线电平并发送命令</li>
     * </ul>
     *
     * <p><b>配置流程：</b>
     * <ul>
     *   <li>步骤1：获取时钟、数据、CS通道索引</li>
     *   <li>步骤2：设置各信号源</li>
     *   <li>步骤3：配置总线源扩展</li>
     *   <li>步骤4：根据位宽设置BW值</li>
     *   <li>步骤5：配置时钟空闲电平</li>
     *   <li>步骤6：配置CS使能和电平</li>
     *   <li>步骤7：配置触发模式和触发数据</li>
     *   <li>步骤8：配置各信号电平</li>
     *   <li>步骤9：发送总线电平寄存器</li>
     * </ul>
     *
     * @param fpgaIdx FPGA芯片索引（多FPGA架构时使用）
     * @param iBus SPI总线接口对象，包含SPI配置参数
     */
    @Override
    public void configBus(int fpgaIdx, IBus iBus) {  // 配置SPI总线方法，实现抽象方法
        
        // 将IBus接口转换为SpiBus具体类型
        SpiBus bus = (SpiBus) iBus;  // 类型转换，获取SPI总线对象
        
        // 获取触发类型
        int triggerType = bus.getTriggerType();  // 获取SPI触发类型
        
        // 获取时钟通道索引
        int clk = bus.getClkChIdx();  // 获取SCK信号所在的通道索引
        
        // 获取数据通道索引
        int data = bus.getDataChIdx();  // 获取数据信号所在的通道索引
        
        // 获取CS通道索引
        int cs = bus.getCsChIdx();  // 获取CS信号所在的通道索引
        
        // 获取总线电平寄存器对象
        FPGAReg_BUS_LEVEL busLevel = (FPGAReg_BUS_LEVEL) FPGACommand.getReg(fpgaIdx, FPGA_BUS_LEVEL);  // 获取总线电平寄存器
        
        // 设置CS信号源
        setCS(chIdx2BusSrc(cs));  // 设置CS信号源，使用通道索引转总线源方法
        
        // 设置SCK信号源
        setSCK(chIdx2BusSrc(clk));  // 设置SCK信号源，使用通道索引转总线源方法
        
        // 设置数据信号源
        setDSrc(chIdx2BusSrc(data));  // 设置数据信号源，使用通道索引转总线源方法
        
        // 配置总线源扩展
        // ((cs>>2)&0x1)：获取CS通道的高位
        // ((clk>>1)&0x2)：获取时钟通道的高位
        // (data&0x4)：获取数据通道的高位
        busLevel.setBusSrcExt(getBusIdx(), ((cs >> 2) & 0x1) | ((clk >> 1) & 0x2) | (data & 0x4));  // 设置总线源扩展
        
        // 根据数据位宽设置BW值
        switch (bus.getBits()) {  // 根据位宽分支处理
            case 4:  // 4位数据宽度
                setBW(0);  // 设置BW为0（4位）
                break;  // 结束该分支
            case 8:  // 8位数据宽度
                setBW(1);  // 设置BW为1（8位）
                break;  // 结束该分支
            case 16:  // 16位数据宽度
                setBW(2);  // 设置BW为2（16位）
                break;  // 结束该分支
            case 24:  // 24位数据宽度
                setBW(3);  // 设置BW为3（24位）
                break;  // 结束该分支
            case 32:  // 32位数据宽度
                setBW(4);  // 设置BW为4（32位）
                break;  // 结束该分支
            default:  // 默认情况
                setBW(1);  // 默认设置为8位
                break;  // 结束默认分支
        }
        
        // 设置时钟空闲电平为高
        setClkIdle(IBus.IDLE_LEVEL_HIGH);  // 设置时钟空闲电平为高
        
        // 配置CS使能和电平
        if (bus.isCsValid()) {  // 如果CS信号有效
            // 设置CS电平（根据CS空闲电平设置反向电平）
            // CS通常为低电平有效，所以空闲高电平时设置为低
            setLevel(busLevel, cs, bus.getCsIdleLevel() == IBus.IDLE_LEVEL_HIGH ? IBus.IDLE_LEVEL_LOW : IBus.IDLE_LEVEL_HIGH);  // 设置CS电平
            // 启用CS信号
            setEanbleCS(1);  // 启用CS
        } else {  // 如果CS信号无效
            // 禁用CS信号
            setEanbleCS(0);  // 禁用CS
        }
        
        // 配置触发模式和触发数据
        if (SpiBus.SPI_TRIGGER_FRAME_X_DATA == triggerType) {  // 如果是扩展帧数据触发
            // 转换为帧数据触发模式
            triggerType = SpiBus.SPI_TRIGGER_FRAME_DATA;  // 设置触发类型为帧数据触发
            // 清除触发掩码
            setTriggerMask(0);  // 设置掩码为0
            // 清除触发值
            setTriggerVal(0);  // 设置触发值为0
        } else {  // 其他触发类型
            // 设置触发掩码
            setTriggerMask(bus.getTriggerMask());  // 设置触发掩码
            // 设置触发值
            setTriggerVal(bus.getTriggerData());  // 设置触发数据值
        }
        
        // 设置触发模式
        setTriggerMode(triggerType);  // 设置触发模式
        
        // 设置时钟电平
        // 根据时钟采样边沿设置时钟电平
        // 下降沿采样：时钟空闲为低电平
        // 上升沿采样：时钟空闲为高电平
        setLevel(busLevel, clk, bus.getClkSample() == SpiBus.SPI_CLK_FALL_EDGE ? IBus.IDLE_LEVEL_LOW : IBus.IDLE_LEVEL_HIGH);  // 设置时钟电平
        
        // 设置数据电平
        setLevel(busLevel, data, bus.getDataIdleLevel());  // 设置数据线电平
        
        // 发送总线电平寄存器到FPGA
        FPGACommand.sendCmd(busLevel);  // 发送总线电平配置命令到FPGA
    }

}