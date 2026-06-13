package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口类，定义总线通用接口
import com.micsig.tbook.scope.Bus.MILSTD1553BBus;  // 导入MIL-STD-1553B总线类，提供1553B总线配置参数

/**
 * FPGA MIL-STD-1553B总线寄存器类 - 用于配置1553B总线解码和触发参数
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg_BUS基类）</li>
 *   <li>职责类型：MIL-STD-1553B总线配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置1553B总线的信号源</li>
 *   <li>配置1553B总线触发模式</li>
 *   <li>配置1553B命令/状态字触发条件</li>
 *   <li>配置1553B数据字触发条件</li>
 *   <li>配置1553B远程终端地址触发条件</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供MIL-STD-1553B总线解码的硬件配置接口</li>
 *   <li>支持命令/状态字触发、数据字触发、RT地址触发</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * MIL-STD-1553B总线寄存器（160位，20字节）
 * │
 * ├── 字节0：信号源配置
 * │     └── 位2-3：1553B信号源选择（0-3）
 * │
 * ├── 字节1：触发配置
 * │     ├── 位0-3：触发模式（4位）
 * │     ├── 位8-23：数据字/命令状态字（16位）
 * │     └── 位19-23：远程终端地址（5位）
 * │
 * └── 字节2-19：保留字节
 * </pre>
 *
 * <p><b>MIL-STD-1553B总线说明：</b>
 * <ul>
 *   <li>MIL-STD-1553B是美国军用标准串行数据总线</li>
 *   <li>广泛应用于航空航天和军事领域</li>
 *   <li>采用时分多路复用（TDM）方式通信</li>
 *   <li>支持总线控制器（BC）、远程终端（RT）、总线监视器（BM）三种角色</li>
 * </ul>
 *
 * <p><b>触发模式说明：</b>
 * <ul>
 *   <li>命令/状态字触发：在特定命令或状态字时触发</li>
 *   <li>数据字触发：在特定数据字时触发</li>
 *   <li>RT地址触发：在特定远程终端地址时触发</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>1553B总线解码：配置1553B总线解码参数</li>
 *   <li>1553B总线触发：配置1553B总线触发条件</li>
 *   <li>命令字过滤：设置命令字触发条件</li>
 *   <li>数据字过滤：设置数据字触发条件</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg_BUS（总线寄存器基类）</li>
 *   <li>依赖：MILSTD1553BBus（1553B总线配置类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：FPGAReg_BUS_LEVEL（总线电平寄存器）</li>
 * </ul>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-30
 * @see FPGAReg_BUS 总线寄存器基类
 * @see MILSTD1553BBus MIL-STD-1553B总线配置类
 * @see IBus 总线接口类
 */
public class FPGAReg_BUS_1553B extends FPGAReg_BUS {  // 继承FPGAReg_BUS基类，复用总线寄存器操作方法
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 构造函数 - 初始化1553B总线寄存器
     *
     * <p><b>初始化参数：</b>
     * <ul>
     *   <li>寄存器地址：由参数addr指定</li>
     *   <li>寄存器大小：20字节（160位）</li>
     * </ul>
     *
     * @param addr 寄存器地址（FPGA总线寄存器地址）
     */
    public FPGAReg_BUS_1553B(int addr) {  // 构造方法：初始化1553B总线寄存器
        super(addr, 20);  // 调用父类构造函数，设置寄存器地址和大小
    }  // 构造方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 配置参数设置
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 设置信号源
     * 设置1553B总线的信号源通道
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制FPGA从哪个通道获取1553B总线信号</li>
     *   <li>信号源值：0-3（对应通道0-3）</li>
     *   <li>寄存器位：位2-3（2位）</li>
     * </ul>
     *
     * @param val 信号源值（0-3，对应通道0-3）
     */
    public void setSrc(int val){  // 公有方法：设置信号源
        setVal(0,2,val);  // 设置位2-3的值，宽度为2位，值为val（信号源）
    }  // 方法结束
    
    /**
     * 设置触发模式
     * 设置1553B总线的触发模式
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制1553B总线的触发方式</li>
     *   <li>触发模式：命令/状态字触发、数据字触发、RT地址触发</li>
     *   <li>寄存器位：位0-3（4位）</li>
     * </ul>
     *
     * @param val 触发模式值（0-15，对应不同触发模式）
     */
    public void setTriggerMode(int val){  // 公有方法：设置触发模式
        setVal(1,0,4,val);  // 设置位0-3的值，宽度为4位，值为val（触发模式）
    }  // 方法结束
    
    /**
     * 设置数据字/命令状态字
     * 设置1553B的数据字或命令状态字触发条件
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制1553B的数据字或命令状态字触发条件</li>
     *   <li>数据值：16位（0-65535）</li>
     *   <li>寄存器位：位8-23（16位）</li>
     * </ul>
     *
     * @param val 数据字/命令状态字值（0-65535，16位）
     */
    public void setData(int val){  // 公有方法：设置数据字/命令状态字
        setVal(1,8,16,val);  // 设置位8-23的值，宽度为16位，值为val（数据字/命令状态字）
    }  // 方法结束
    
    /**
     * 设置远程终端地址
     * 设置1553B的远程终端地址触发条件
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制1553B的远程终端地址触发条件</li>
     *   <li>地址值：5位（0-31）</li>
     *   <li>寄存器位：位19-23（5位）</li>
     * </ul>
     *
     * @param val 远程终端地址值（0-31，5位）
     */
    public void setData1(int val){  // 公有方法：设置远程终端地址
        setVal(1,19,5,val);  // 设置位19-23的值，宽度为5位，值为val（远程终端地址）
    }  // 方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 总线配置
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 配置1553B总线参数
     * 根据MILSTD1553BBus配置对象设置FPGA寄存器参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从MILSTD1553BBus对象获取配置参数</li>
     *   <li>根据触发类型设置不同的触发条件</li>
     *   <li>配置信号源和总线电平</li>
     *   <li>发送寄存器配置到FPGA</li>
     * </ul>
     *
     * <p><b>配置流程：</b>
     * <ol>
     *   <li>获取MILSTD1553BBus配置参数（触发类型、信号源等）</li>
     *   <li>设置信号源</li>
     *   <li>设置总线信号源扩展</li>
     *   <li>设置触发模式</li>
     *   <li>根据触发类型设置触发条件（命令/状态字、数据字、RT地址）</li>
     *   <li>配置总线电平</li>
     *   <li>发送寄存器配置到FPGA</li>
     * </ol>
     *
     * @param fpgaIdx FPGA索引（用于多FPGA系统）
     * @param iBus 1553B总线配置对象（MILSTD1553BBus类型）
     */
    @Override
    public void configBus(int fpgaIdx,IBus iBus) {  // 公有方法：配置1553B总线参数
        // 将IBus接口转换为MILSTD1553BBus类型
        MILSTD1553BBus bus = (MILSTD1553BBus)iBus;  // 类型转换：获取MILSTD1553BBus配置对象
        
        // 获取1553B总线配置参数
        int triggerType = bus.getTriggerType();  // 获取触发类型
        int src = (bus.getSrcChIdx());  // 获取信号源通道索引
        
        // 获取总线电平寄存器
        FPGAReg_BUS_LEVEL busLevel = (FPGAReg_BUS_LEVEL) FPGACommand.getReg(fpgaIdx,FPGA_BUS_LEVEL);  // 获取总线电平寄存器
        
        // 设置信号源
        setSrc(chIdx2BusSrc(src));  // 设置信号源（通道索引转换为总线信号源）
        
        // 设置总线信号源扩展
        busLevel.setBusSrcExt(getBusIdx(),((src>>2)&0x1));  // 设置总线信号源扩展（高位通道选择）
        
        // 设置触发模式
        setTriggerMode(triggerType);  // 设置触发模式
        
        // 根据触发类型设置触发条件
        switch(triggerType) {  // switch语句：根据触发类型选择触发条件
            case MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_WORD:  // case分支：命令/状态字触发
                // 设置命令/状态字触发条件
                setData(bus.getCmdStatus());  // 设置命令/状态字值
                break;  // 跳出switch语句
            case MILSTD1553BBus.MILSTD1553B_TRIGGER_DATA_WORD:  // case分支：数据字触发
                // 设置数据字触发条件（位8-23）
                setData(bus.getData());  // 设置数据字值
                break;  // 跳出switch语句
            case MILSTD1553BBus.MILSTD1553B_TRIGGER_RT_ADDRESS:  // case分支：远程终端地址触发
                // 设置远程终端地址触发条件（位19-23）
                setData1(bus.getAddr());  // 设置远程终端地址值
                break;  // 跳出switch语句
            default:  // default分支：默认情况
                // 设置默认触发条件
                setData(0);  // 设置数据值为0
                break;  // 跳出switch语句
        }  // switch语句结束
        
        // 配置总线电平（1553B总线空闲电平为高电平）
        setLevel(busLevel,src,IBus.IDLE_LEVEL_HIGH);  // 设置总线空闲电平为高电平
        
        // 发送总线电平寄存器配置到FPGA
        FPGACommand.sendCmd(busLevel);  // 发送总线电平寄存器配置
    }  // 方法结束
}  // FPGAReg_BUS_1553B类结束