package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口类，定义总线通用接口

/**
 * FPGA总线地址寄存器类 - 用于管理多种总线类型的寄存器配置
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：工厂模式 + 组合模式</li>
 *   <li>职责类型：总线寄存器管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理多种总线类型的寄存器实例</li>
 *   <li>提供统一的总线配置接口</li>
 *   <li>根据总线类型选择对应的寄存器类</li>
 *   <li>协调总线寄存器的配置和发送</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供统一的总线寄存器管理接口</li>
 *   <li>支持多种总线类型（UART、LIN、CAN、SPI、I2C、ARINC429、1553B）</li>
 *   <li>简化总线配置的调用流程</li>
 *   <li>封装总线类型选择逻辑</li>
 * </ul>
 *
 * <p><b>总线类型说明：</b>
 * <pre>
 * 支持的总线类型：
 * ├── UART：通用异步收发传输器（Universal Asynchronous Receiver/Transmitter）
 * ├── LIN：本地互联网络（Local Interconnect Network）
 * ├── CAN：控制器局域网（Controller Area Network）
 * ├── SPI：串行外设接口（Serial Peripheral Interface）
 * ├── I2C：内部集成电路（Inter-Integrated Circuit）
 * ├── ARINC429：航空电子数据总线标准
 * └── MILSTD1553B：美国军用标准数据总线
 * </pre>
 *
 * <p><b>架构设计：</b>
 * <pre>
 * FPGAReg_BUS_ADDR架构：
 * │
 * ├── FPGAReg_BUS_ADDR（总线寄存器管理器）
 * │     ├── bus[UART] = FPGAReg_BUS_UART
 * │     ├── bus[LIN] = FPGAReg_BUS_LIN
 * │     ├── bus[CAN] = FPGAReg_BUS_CAN
 * │     ├── bus[SPI] = FPGAReg_BUS_SPI
 * │     ├── bus[I2C] = FPGAReg_BUS_I2C
 * │     ├── bus[ARINC429] = FPGAReg_BUS_429
 * │     └── bus[MILSTD1553B] = FPGAReg_BUS_1553B
 * │
 * └── 统一接口：
 *       ├── getFPGAReg(busType)：获取指定类型的寄存器实例
 *       ├── configBus(fpgaIdx, iBus)：配置总线参数
 *       └── setFpgaIdx(fpgaIdx)：设置FPGA索引
 * </pre>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>总线配置：根据总线类型配置对应的FPGA寄存器</li>
 *   <li>总线切换：切换不同总线类型时获取对应的寄存器实例</li>
 *   <li>多总线支持：支持多种总线类型的统一管理</li>
 *   <li>总线解码：配置总线解码参数</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>组合：FPGAReg_BUS_UART、FPGAReg_BUS_LIN、FPGAReg_BUS_CAN等</li>
 *   <li>依赖：IBus（总线接口类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建总线地址寄存器实例
 * FPGAReg_BUS_ADDR reg = new FPGAReg_BUS_ADDR(FPGA_BUS_ADDR);
 *
 * // 获取UART总线寄存器实例
 * FPGAReg uartReg = reg.getFPGAReg(IBus.UART);
 *
 * // 配置UART总线参数
 * UartBus uartBus = new UartBus();
 * uartBus.setBaudRate(9600);
 * uartBus.setDataBits(8);
 * reg.configBus(fpgaIdx, uartBus);
 *
 * // 配置LIN总线参数
 * LinBus linBus = new LinBus();
 * linBus.setBaudRate(19200);
 * reg.configBus(fpgaIdx, linBus);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-30
 * @see FPGAReg FPGA寄存器基类
 * @see FPGAReg_BUS 总线寄存器基类
 * @see IBus 总线接口类
 */
public class FPGAReg_BUS_ADDR extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 总线寄存器数组
     * 用于存储不同总线类型的寄存器实例
     *
     * <p><b>数组结构：</b>
     * <ul>
     *   <li>数组大小：BUS_CNT（总线类型数量）</li>
     *   <li>数组索引：总线类型（UART、LIN、CAN等）</li>
     *   <li>数组元素：FPGAReg_BUS实例</li>
     * </ul>
     *
     * <p><b>初始化时机：</b>
     * <ul>
     *   <li>在构造函数中初始化</li>
     *   <li>为每种总线类型创建对应的寄存器实例</li>
     * </ul>
     */
    private FPGAReg_BUS []bus = new FPGAReg_BUS[IBus.BUS_CNT];  // 成员变量：总线寄存器数组
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 构造函数 - 初始化总线地址寄存器
     *
     * <p><b>初始化参数：</b>
     * <ul>
     *   <li>寄存器地址：由参数addr指定</li>
     *   <li>寄存器大小：0（本类不直接操作寄存器数据）</li>
     * </ul>
     *
     * <p><b>初始化操作：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>创建各种总线类型的寄存器实例</li>
     *   <li>将寄存器实例存储到总线寄存器数组</li>
     * </ul>
     *
     * <p><b>总线寄存器初始化：</b>
     * <pre>
     * bus[UART] = new FPGAReg_BUS_UART(addr)
     * bus[LIN] = new FPGAReg_BUS_LIN(addr)
     * bus[CAN] = new FPGAReg_BUS_CAN(addr)
     * bus[SPI] = new FPGAReg_BUS_SPI(addr)
     * bus[I2C] = new FPGAReg_BUS_I2C(addr)
     * bus[ARINC429] = new FPGAReg_BUS_429(addr)
     * bus[MILSTD1553B] = new FPGAReg_BUS_1553B(addr)
     * </pre>
     *
     * @param addr 寄存器地址（FPGA总线寄存器地址）
     */
    public FPGAReg_BUS_ADDR(int addr) {  // 构造方法：初始化总线地址寄存器
        // 调用父类构造函数，设置寄存器地址和大小
        // addr：寄存器地址（由调用者指定）
        // 0：寄存器大小（本类不直接操作寄存器数据，由子寄存器类操作）
        super(addr, 0);  // 调用父类构造函数，设置寄存器地址和大小
        
        // 创建各种总线类型的寄存器实例
        // UART总线寄存器
        bus[IBus.UART] = new FPGAReg_BUS_UART(addr);  // 创建UART总线寄存器实例
        
        // LIN总线寄存器
        bus[IBus.LIN] = new FPGAReg_BUS_LIN(addr);  // 创建LIN总线寄存器实例
        
        // CAN总线寄存器
        bus[IBus.CAN] = new FPGAReg_BUS_CAN(addr);  // 创建CAN总线寄存器实例
        
        // SPI总线寄存器
        bus[IBus.SPI] = new FPGAReg_BUS_SPI(addr);  // 创建SPI总线寄存器实例
        
        // I2C总线寄存器
        bus[IBus.I2C] = new FPGAReg_BUS_I2C(addr);  // 创建I2C总线寄存器实例
        
        // ARINC429总线寄存器
        bus[IBus.ARINC429] = new FPGAReg_BUS_429(addr);  // 创建ARINC429总线寄存器实例
        
        // MIL-STD-1553B总线寄存器
        bus[IBus.MILSTD1553B] = new FPGAReg_BUS_1553B(addr);  // 创建1553B总线寄存器实例
    }  // 构造方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 寄存器获取
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 获取指定类型的总线寄存器实例
     * 根据总线类型返回对应的寄存器实例
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据总线类型从数组中获取对应的寄存器实例</li>
     *   <li>如果总线类型无效，返回null</li>
     *   <li>用于直接操作特定总线类型的寄存器</li>
     * </ul>
     *
     * <p><b>总线类型验证：</b>
     * <ul>
     *   <li>调用IBus.isValid(busType)验证总线类型</li>
     *   <li>如果总线类型有效，返回对应的寄存器实例</li>
     *   <li>如果总线类型无效，返回null</li>
     * </ul>
     *
     * @param busType 总线类型（UART、LIN、CAN、SPI、I2C、ARINC429、MILSTD1553B）
     * @return 总线寄存器实例（FPGAReg类型），如果总线类型无效则返回null
     */
    public FPGAReg getFPGAReg(int busType){  // 公有方法：获取指定类型的总线寄存器实例
        // 验证总线类型是否有效
        if(IBus.isValid(busType))  // 条件判断：总线类型有效
            // 返回对应的总线寄存器实例
            return bus[busType];  // 返回总线寄存器实例
        // 总线类型无效，返回null
        return null;  // 返回null：总线类型无效
    }  // 方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 总线配置
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 配置总线参数
     * 根据总线类型配置对应的FPGA寄存器参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从IBus对象获取总线类型</li>
     *   <li>验证总线类型是否有效</li>
     *   <li>调用对应总线寄存器的configBus方法</li>
     *   <li>发送寄存器配置到FPGA</li>
     * </ul>
     *
     * <p><b>配置流程：</b>
     * <ol>
     *   <li>获取总线类型</li>
     *   <li>验证总线类型是否有效</li>
     *   <li>调用对应总线寄存器的configBus方法配置参数</li>
     *   <li>发送寄存器配置到FPGA</li>
     * </ol>
     *
     * <p><b>总线类型判断：</b>
     * <ul>
     *   <li>调用IBus.isValid(busType)验证总线类型</li>
     *   <li>如果总线类型有效，执行配置操作</li>
     *   <li>如果总线类型无效，不执行任何操作</li>
     * </ul>
     *
     * @param fpgaIdx FPGA索引（用于多FPGA系统）
     * @param iBus 总线配置对象（IBus接口类型）
     */
    public void configBus(int fpgaIdx,IBus iBus){  // 公有方法：配置总线参数
        // 获取总线类型
        int busType = iBus.getBusType();  // 获取总线类型
        
        // 验证总线类型是否有效
        if(IBus.isValid(busType)){  // 条件判断：总线类型有效
            // 调用对应总线寄存器的configBus方法配置参数
            bus[busType].configBus(fpgaIdx,iBus);  // 配置总线参数
            
            // 发送寄存器配置到FPGA
            FPGACommand.sendCmd(bus[busType]);  // 发送总线寄存器配置
        }  // 条件判断结束
    }  // 方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 重写方法 - FPGA索引设置
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 设置FPGA索引
     * 为所有总线寄存器实例设置FPGA索引
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类的setFpgaIdx方法设置本类的FPGA索引</li>
     *   <li>遍历所有总线寄存器实例，设置它们的FPGA索引</li>
     *   <li>确保所有总线寄存器使用相同的FPGA索引</li>
     * </ul>
     *
     * <p><b>FPGA索引说明：</b>
     * <ul>
     *   <li>FPGA索引用于多FPGA系统</li>
     *   <li>不同的FPGA索引对应不同的FPGA芯片</li>
     *   <li>所有总线寄存器应使用相同的FPGA索引</li>
     * </ul>
     *
     * <p><b>遍历操作：</b>
     * <ul>
     *   <li>遍历总线寄存器数组</li>
     *   <li>检查寄存器实例是否为null</li>
     *   <li>如果不为null，设置其FPGA索引</li>
     * </ul>
     *
     * @param fpgaIdx FPGA索引（用于多FPGA系统）
     */
    @Override
    public void setFpgaIdx(int fpgaIdx) {  // 重写方法：设置FPGA索引
        // 调用父类的setFpgaIdx方法，设置本类的FPGA索引
        super.setFpgaIdx(fpgaIdx);  // 设置本类的FPGA索引
        
        // 遍历所有总线寄存器实例，设置它们的FPGA索引
        for (FPGAReg_BUS fpgaRegBus : bus) {  // 循环：遍历总线寄存器数组
            // 检查寄存器实例是否为null
            if(fpgaRegBus != null) {  // 条件判断：寄存器实例不为null
                // 设置寄存器实例的FPGA索引
                fpgaRegBus.setFpgaIdx(fpgaIdx);  // 设置总线寄存器的FPGA索引
            }  // 条件判断结束
        }  // 循环结束
    }  // 方法结束
}  // FPGAReg_BUS_ADDR类结束