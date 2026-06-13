package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import com.micsig.tbook.scope.Bus.CanBus;  // 导入CAN总线类，提供CAN总线配置参数
import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口类，定义总线通用接口

/**
 * FPGA CAN总线寄存器类 - 用于配置CAN总线解码和触发参数
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg_BUS基类）</li>
 *   <li>职责类型：CAN总线配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置CAN总线的信号源</li>
 *   <li>配置CAN波特率和采样点</li>
 *   <li>配置CAN触发模式和触发条件</li>
 *   <li>配置CAN FD（灵活数据速率）参数</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供CAN总线解码的硬件配置接口</li>
 *   <li>支持CAN和CAN FD协议</li>
 *   <li>支持ISO和非ISO模式</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * CAN总线寄存器（160位，20字节）
 * │
 * ├── 字节0：信号源配置
 * │     └── 位0-1：CAN信号源选择（0-3）
 * │
 * ├── 字节1：帧ID配置
 * │     └── 位0-31：CAN帧ID（标准帧11位或扩展帧29位）
 * │
 * ├── 字节2-3：数据配置
 * │     └── 位0-63：CAN数据（8字节）
 * │
 * ├── 字节4：波特率配置
 * │     ├── 位0-13：采样点位置
 * │     └── 位14-27：波特率分频值
 * │
 * └── 字节5-19：扩展配置
 *       ├── 位2：ISO模式选择
 *       ├── 位4：DLC（数据长度码）
 *       ├── 位8-11：触发模式
 *       └── 位16-31：波特率参数
 * </pre>
 *
 * <p><b>CAN波特率计算：</b>
 * <ul>
 *   <li>基准时钟：125MHz</li>
 *   <li>波特率分频值 = 125000000 / bit_rate - 1</li>
 *   <li>采样点位置 = 125000000 / bit_rate * s - 1</li>
 *   <li>波特率参数N = 16 * (125000000 / bit_rate - baud - 1)</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>CAN总线解码：配置CAN信号源和波特率</li>
 *   <li>CAN协议触发：设置帧ID、数据触发条件</li>
 *   <li>CAN FD分析：分析CAN FD高速数据</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg_BUS（总线寄存器基类）</li>
 *   <li>依赖：CanBus（CAN总线配置类）</li>
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
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-30
 * @see FPGAReg_BUS 总线寄存器基类
 * @see CanBus CAN总线配置类
 * @see FPGAReg_BUS_LEVEL 总线电平寄存器
 */
public class FPGAReg_BUS_CAN extends FPGAReg_BUS {  // 继承FPGAReg_BUS基类，复用总线配置方法

    /**
     * 构造函数 - 初始化CAN总线寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器大小：20字节（160位）</li>
     *   <li>支持CAN和CAN FD协议</li>
     * </ul>
     *
     * @param addr 寄存器地址（FPGA_BUS1_ADDR或FPGA_BUS2_ADDR）
     */
    public FPGAReg_BUS_CAN(int addr) {  // 构造函数，接收寄存器地址
        // 调用父类构造函数，传入寄存器地址和大小
        super(addr, 20);  // 初始化CAN总线寄存器，地址由参数指定，大小为20字节
    }

    /**
     * 设置信号源 - 配置CAN总线的信号源
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN总线信号源选择</li>
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
     * 设置ISO模式 - 配置CAN协议ISO模式
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN协议的ISO模式</li>
     *   <li>寄存器位：位2（1位）</li>
     *   <li>ISO模式影响CAN FD的解码方式</li>
     * </ul>
     *
     * <p><b>ISO模式说明：</b>
     * <ul>
     *   <li>true：ISO模式（ISO 11898-1标准）</li>
     *   <li>false：非ISO模式（Bosch CAN FD规范）</li>
     * </ul>
     *
     * @param bISO 是否为ISO模式（true=ISO，false=非ISO）
     */
    public void setISO(boolean bISO) {  // 设置ISO模式方法
        // 调用父类setVal方法，设置寄存器值
        // ISO模式：bISO=true时设置为0，bISO=false时设置为1
        setVal(2, 1, bISO ? 0 : 1);  // 设置位2的值，ISO模式为0，非ISO模式为1
    }

    /**
     * 设置DLC - 配置CAN数据长度码
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN帧的数据长度码（DLC）</li>
     *   <li>寄存器位：位4-7（4位）</li>
     *   <li>DLC决定数据域的字节数</li>
     * </ul>
     *
     * <p><b>DLC说明：</b>
     * <ul>
     *   <li>CAN：DLC 0-8，对应0-8字节</li>
     *   <li>CAN FD：DLC 9-15，对应12-64字节</li>
     * </ul>
     *
     * @param val DLC值（0-15）
     */
    public void setDlc(int val) {  // 设置DLC方法
        // 调用父类setVal方法，设置寄存器值
        setVal(4, 4, val);  // 设置位4-7的值，宽度为4位，值为val（DLC）
    }

    /**
     * 设置触发模式 - 配置CAN触发类型
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN总线的触发模式</li>
     *   <li>寄存器位：位8-11（4位）</li>
     *   <li>支持帧ID、数据等触发类型</li>
     * </ul>
     *
     * @param val 触发模式值（0-15）
     */
    public void setTriggerMode(int val) {  // 设置触发模式方法
        // 调用父类setVal方法，设置寄存器值
        setVal(8, 4, val);  // 设置位8-11的值，宽度为4位，值为val（触发模式）
    }

    /**
     * 设置波特率参数N - 配置CAN波特率参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN波特率参数N（第一组）</li>
     *   <li>寄存器位：位16-19（4位）</li>
     *   <li>用于波特率精细调节</li>
     * </ul>
     *
     * @param val 波特率参数N值（0-15）
     */
    public void setBaudN(int val) {  // 设置波特率参数N方法
        // 调用父类setVal方法，设置寄存器值
        setVal(16, 4, val);  // 设置位16-19的值，宽度为4位，值为val（波特率参数N）
    }

    /**
     * 设置波特率参数N2 - 配置CAN波特率参数（第二组）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN波特率参数N（第二组）</li>
     *   <li>寄存器位：位20-23（4位）</li>
     *   <li>用于多波特率配置</li>
     * </ul>
     *
     * @param val 波特率参数N2值（0-15）
     */
    public void setBaudN2(int val) {  // 设置波特率参数N2方法
        // 调用父类setVal方法，设置寄存器值
        setVal(20, 4, val);  // 设置位20-23的值，宽度为4位，值为val（波特率参数N2）
    }

    /**
     * 设置波特率参数N3 - 配置CAN FD波特率参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN FD高速数据阶段的波特率参数N</li>
     *   <li>寄存器位：位24-27（4位）</li>
     *   <li>用于CAN FD数据阶段波特率</li>
     * </ul>
     *
     * @param val 波特率参数N3值（0-15）
     */
    public void setBaudN3(int val) {  // 设置波特率参数N3方法
        // 调用父类setVal方法，设置寄存器值
        setVal(24, 4, val);  // 设置位24-27的值，宽度为4位，值为val（波特率参数N3）
    }

    /**
     * 设置帧ID - 配置CAN帧标识符
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN帧的标识符（ID）</li>
     *   <li>寄存器位：字节1（32位）</li>
     *   <li>支持标准帧（11位）和扩展帧（29位）</li>
     * </ul>
     *
     * @param val CAN帧ID值（0-0x1FFFFFFF）
     */
    public void setId(int val) {  // 设置帧ID方法
        // 调用父类setVal方法，设置寄存器值
        setVal(1, val);  // 设置字节1的值，值为val（帧ID）
    }

    /**
     * 设置数据 - 配置CAN帧数据
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN帧的数据域</li>
     *   <li>寄存器位：字节2-9（64位）</li>
     *   <li>支持最多8字节数据</li>
     * </ul>
     *
     * @param idx 数据索引（0-7，对应字节位置）
     * @param val 数据值（0-255）
     */
    public void setData(int idx, int val) {  // 设置数据方法
        // 调用父类setVal方法，设置寄存器值
        setVal(idx + 2, val);  // 设置字节(idx+2)的值，值为val（数据）
    }

    /**
     * 设置采样点位置 - 配置CAN采样点位置
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN位时间的采样点位置</li>
     *   <li>寄存器位：位0-13（14位）</li>
     *   <li>影响位定时参数</li>
     * </ul>
     *
     * @param val 采样点位置值（0-16383）
     */
    public void setSamplePlace(int val) {  // 设置采样点位置方法
        // 调用父类setVal方法，设置寄存器值
        setVal(4, 0, 14, val);  // 设置位0-13的值，宽度为14位，值为val（采样点位置）
    }

    /**
     * 设置波特率分频值 - 配置CAN波特率分频
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN波特率的分频值</li>
     *   <li>寄存器位：位14-27（14位）</li>
     *   <li>决定CAN位时间长度</li>
     * </ul>
     *
     * @param val 波特率分频值（0-16383）
     */
    public void setBaud(int val) {  // 设置波特率分频值方法
        // 调用父类setVal方法，设置寄存器值
        setVal(4, 14, 14, val);  // 设置位14-27的值，宽度为14位，值为val（波特率分频值）
    }

    /**
     * 设置波特率选择 - 配置波特率组选择
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置波特率组选择</li>
     *   <li>寄存器位：位28-31（4位）</li>
     *   <li>用于多波特率配置选择</li>
     * </ul>
     *
     * @param val 波特率选择值（0-15）
     */
    public void setSel(int val) {  // 设置波特率选择方法
        // 调用父类setVal方法，设置寄存器值
        setVal(4, 28, 4, val);  // 设置位28-31的值，宽度为4位，值为val（波特率选择）
    }

    /**
     * 设置CAN FD数据 - 配置CAN FD数据值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN FD触发数据值</li>
     *   <li>寄存器位：字节2（16位数据+16位索引）</li>
     *   <li>用于CAN FD数据触发</li>
     * </ul>
     *
     * @param idx 数据索引
     * @param val 数据值
     */
    public void setFDData(int idx, int val) {  // 设置CAN FD数据方法
        // 调用父类setVal方法，设置寄存器值
        setVal(2, 0, 16, val);  // 设置位0-15的值，宽度为16位，值为val（FD数据）
        setVal(2, 16, 16, idx);  // 设置位16-31的值，宽度为16位，值为idx（FD数据索引）
    }

    /**
     * 设置CAN FD掩码 - 配置CAN FD数据掩码
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置CAN FD触发数据掩码</li>
     *   <li>寄存器位：字节3（16位掩码+16位索引）</li>
     *   <li>用于CAN FD数据掩码触发</li>
     * </ul>
     *
     * @param idx 掩码索引
     * @param val 掩码值
     */
    public void setFDMask(int idx, int val) {  // 设置CAN FD掩码方法
        // 调用父类setVal方法，设置寄存器值
        setVal(3, 0, 16, val);  // 设置位0-15的值，宽度为16位，值为val（FD掩码）
        setVal(3, 0, 16, val);  // 设置位0-15的值（重复设置，可能是代码问题）
    }

    /**
     * 设置CAN波特率 - 内部方法，计算并设置波特率参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据波特率和采样点计算寄存器参数</li>
     *   <li>设置波特率分频值和采样点位置</li>
     *   <li>支持CAN和CAN FD波特率配置</li>
     * </ul>
     *
     * <p><b>计算公式：</b>
     * <ul>
     *   <li>波特率分频值 = 125000000 / bit_rate - 1</li>
     *   <li>采样点位置 = 125000000 / bit_rate * s - 1</li>
     *   <li>波特率参数N = 16 * (125000000 / bit_rate - baud - 1)</li>
     * </ul>
     *
     * @param val 波特率组选择值
     * @param bus CAN总线对象，包含波特率参数
     */
    private void Bus_Can_setBaudRate(int val, CanBus bus) {  // 设置CAN波特率内部方法
        // 获取CAN标准波特率
        int bit_rate = bus.getBaudRate();  // 获取CAN波特率
        
        // 初始化采样点位置和波特率分频值
        int samplePlace = 0;  // 采样点位置，初始为0
        int baud = 0;  // 波特率分频值，初始为0
        int baudN = 0;  // 波特率参数N，初始为0
        
        // 获取采样点位置比例
        double s1 = bus.getSamplePlace1();  // 获取采样点位置比例
        
        // 计算波特率参数（如果波特率>0）
        if (bit_rate > 0) {  // 如果波特率大于0
            // 计算波特率分频值：125MHz / bit_rate - 1
            baud = ((int) (125000000.0 / bit_rate)) - 1;  // 计算波特率分频值
            
            // 计算采样点位置：125MHz / bit_rate * s - 1
            samplePlace = ((int) (125000000.0 / bit_rate * s1)) - 1;  // 计算采样点位置
            
            // 计算波特率参数N：16 * (125MHz / bit_rate - baud - 1)
            baudN = (int) (16 * (125000000.0 / bit_rate - baud - 1));  // 计算波特率参数N
        }
        
        // 设置波特率分频值
        setBaud(baud);  // 设置波特率分频值
        
        // 设置采样点位置
        setSamplePlace(samplePlace);  // 设置采样点位置
        
        // 设置波特率参数N（第一组）
        setBaudN(baudN);  // 设置波特率参数N
        
        // 设置波特率参数N（第二组）
        setBaudN2(baudN);  // 设置波特率参数N2
        
        // 获取CAN FD高速波特率
        int fd_bit_rate = bus.getFDBandRate();  // 获取CAN FD波特率
        
        // 初始化CAN FD波特率参数
        int fdBand = 0;  // CAN FD波特率分频值，初始为0
        int bandN3 = 0;  // CAN FD波特率参数N，初始为0
        
        // 计算CAN FD波特率参数（如果波特率>0）
        if (fd_bit_rate > 0) {  // 如果CAN FD波特率大于0
            // 计算CAN FD波特率分频值：125MHz / fd_bit_rate - 1
            fdBand = ((int) (125000000.0 / fd_bit_rate)) - 1;  // 计算CAN FD波特率分频值
            
            // 计算CAN FD波特率参数N：16 * (125MHz / fd_bit_rate - fdBand - 1)
            bandN3 = (int) (16 * (125000000.0 / fd_bit_rate - fdBand - 1));  // 计算CAN FD波特率参数N
        }
        
        // 设置CAN FD波特率参数N3
        setBaudN3(bandN3);  // 设置CAN FD波特率参数N3
        
        // 设置波特率选择
        setSel(val);  // 设置波特率选择值
    }

    /**
     * 延时方法 - 内部方法，用于波特率配置延时
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>提供毫秒级延时功能</li>
     *   <li>用于波特率配置过程中的延时等待</li>
     *   <li>确保FPGA稳定接收配置</li>
     * </ul>
     *
     * @param ms 延时时间（毫秒）
     */
    private void ms_sleep(long ms) {  // 延时方法
        try {  // 尝试执行延时
            // 调用Thread.sleep方法，进行毫秒级延时
            Thread.sleep(ms);  // 线程休眠指定毫秒数
        } catch (InterruptedException e) {  // 捕获中断异常
            // 将中断异常转换为运行时异常抛出
            throw new RuntimeException(e);  // 抛出运行时异常
        }
    }

    /**
     * 配置CAN总线 - 实现总线配置接口
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从CanBus对象中获取配置参数</li>
     *   <li>配置信号源和ISO模式</li>
     *   <li>配置波特率和采样点</li>
     *   <li>配置触发条件和数据</li>
     *   <li>配置总线电平并发送命令</li>
     * </ul>
     *
     * @param fpgaIdx FPGA芯片索引（多FPGA架构时使用）
     * @param iBus CAN总线接口对象，包含CAN配置参数
     */
    @Override
    public void configBus(int fpgaIdx, IBus iBus) {  // 配置CAN总线方法，实现抽象方法

        // 将IBus接口转换为CanBus具体类型
        CanBus bus = (CanBus) iBus;  // 类型转换，获取CAN总线对象
        
        // 初始化空闲电平为高电平（CAN总线默认空闲高电平）
        int idleLevel = IBus.IDLE_LEVEL_HIGH;  // 空闲电平，初始为高
        
        // 获取CAN帧数据（64位）
        long data = bus.getData();  // 获取CAN帧数据
        
        // 获取触发类型
        int triggerType = bus.getTriggerType();  // 获取CAN触发类型
        
        // 获取信号源通道索引
        int src = bus.getSrcChIdx();  // 获取CAN信号所在的通道索引
        
        // 设置信号源
        setSrc(chIdx2BusSrc(src));  // 设置信号源，使用通道索引转总线源方法
        
        // 设置ISO模式
        setISO(bus.isISO());  // 设置ISO模式，从CAN总线对象获取
        
        // 设置触发模式
        setTriggerMode(triggerType);  // 设置触发模式
        
        // 设置DLC（数据长度码）
        setDlc(bus.getDlc());  // 设置DLC
        
        // 设置帧ID（根据触发类型）
        setId(bus.getFrameId(triggerType));  // 设置帧ID
        
        // 获取总线电平寄存器对象
        FPGAReg_BUS_LEVEL busLevel = (FPGAReg_BUS_LEVEL) FPGACommand.getReg(fpgaIdx, FPGA_BUS_LEVEL);  // 获取总线电平寄存器
        
        // 配置总线源扩展
        busLevel.setBusSrcExt(getBusIdx(), (src >> 2) & 1);  // 设置总线源扩展
        
        // 设置CAN波特率参数
        Bus_Can_setBaudRate(0, bus);  // 设置CAN波特率，选择波特率组0
        
        // 设置CAN数据（低32位）
        setData(0, (int) data);  // 设置数据字节0-3，取数据的低32位
        
        // 设置CAN数据（高32位）
        setData(1, (int) (data >>> 32));  // 设置数据字节4-7，取数据的高32位
        
        // 根据CAN信号类型设置空闲电平
        switch (bus.getSignal()) {  // 根据信号类型分支处理
            case CanBus.CAN_H:  // CAN_H信号（高线）
            case CanBus.CAN_H_L:  // CAN_H和CAN_L信号（差分）
                // CAN_H和差分信号空闲为低电平
                idleLevel = IBus.IDLE_LEVEL_LOW;  // 设置空闲电平为低
                break;  // 结束该分支
            default:  // 其他信号类型
                // 默认空闲为高电平
                idleLevel = IBus.IDLE_LEVEL_HIGH;  // 设置空闲电平为高
                break;  // 结束默认分支
        }
        
        // 设置总线电平
        setLevel(busLevel, src, idleLevel);  // 设置总线电平
        
        // 发送总线电平寄存器到FPGA
        FPGACommand.sendCmd(busLevel);  // 发送总线电平配置命令到FPGA
    }
}