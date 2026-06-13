package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import com.micsig.tbook.scope.Bus.I2CBus;  // 导入I2C总线类，提供I2C总线配置参数
import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口类，定义总线通用接口

/**
 * FPGA I2C总线寄存器类 - 用于配置I2C总线解码和触发参数
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg_BUS基类）</li>
 *   <li>职责类型：I2C总线配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置I2C总线的SCL（时钟线）信号源</li>
 *   <li>配置I2C总线的SDA（数据线）信号源</li>
 *   <li>配置I2C触发模式和触发条件</li>
 *   <li>配置I2C地址和数据触发参数</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供I2C总线解码的硬件配置接口</li>
 *   <li>支持多种I2C触发条件（地址、数据、帧等）</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 *   <li>支持双通道I2C总线解码</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * I2C总线寄存器（160位，20字节）
 * │
 * ├── 字节0：信号源配置
 * │     ├── 位0-1：SCL信号源选择（0-3）
 * │     └── 位2-3：SDA信号源选择（0-3）
 * │
 * ├── 字节1-3：触发配置
 * │     ├── 位0-3：触发模式（地址、数据、帧等）
 * │     ├── 位8-14：触发地址（7位I2C地址）
 * │     └── 位16-31：触发数据（根据触发类型）
 * │
 * └── 字节4-19：扩展数据区
 *       └── 用于存储多字节触发数据
 * </pre>
 *
 * <p><b>I2C触发类型说明：</b>
 * <ul>
 *   <li>I2C_TRIGGER_ADDRESS_NO_ACK：地址无应答触发</li>
 *   <li>I2C_TRIGGER_EEPROM_READ_DATA：EEPROM读数据触发</li>
 *   <li>I2C_TRIGGER_FRAME1：单帧触发</li>
 *   <li>I2C_TRIGGER_FRAME2：双帧触发</li>
 *   <li>I2C_TRIGGER_WRITE_FRAME：写帧触发</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>I2C总线解码：配置I2C信号源和触发条件</li>
 *   <li>I2C协议触发：设置地址、数据触发条件</li>
 *   <li>I2C调试分析：分析I2C通信数据</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg_BUS（总线寄存器基类）</li>
 *   <li>依赖：I2CBus（I2C总线配置类）</li>
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
 * // 创建I2C总线寄存器实例
 * FPGAReg_BUS_I2C regI2C = new FPGAReg_BUS_I2C(FPGA_BUS1_ADDR);
 *
 * // 配置I2C总线参数
 * regI2C.configBus(fpgaIdx, i2cBus);
 *
 * // 发送寄存器配置到FPGA
 * FPGACommand.sendCmd(regI2C);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-30
 * @see FPGAReg_BUS 总线寄存器基类
 * @see I2CBus I2C总线配置类
 * @see FPGAReg_BUS_LEVEL 总线电平寄存器
 */
public class FPGAReg_BUS_I2C extends FPGAReg_BUS {  // 继承FPGAReg_BUS基类，复用总线配置方法

    /**
     * 构造函数 - 初始化I2C总线寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器大小：20字节（160位）</li>
     *   <li>支持多种I2C触发条件配置</li>
     * </ul>
     *
     * @param addr 寄存器地址（FPGA_BUS1_ADDR或FPGA_BUS2_ADDR）
     */
    public FPGAReg_BUS_I2C(int addr) {  // 构造函数，接收寄存器地址
        // 调用父类构造函数，传入寄存器地址和大小
        // addr：寄存器地址（总线1或总线2）
        // 20：寄存器大小为20字节（160位）
        super(addr, 20);  // 初始化I2C总线寄存器，地址由参数指定，大小为20字节
    }

    /**
     * 设置SCL信号源 - 配置I2C时钟线的信号源
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置I2C总线SCL（时钟线）的信号源选择</li>
     *   <li>寄存器位：位0-1（2位）</li>
     *   <li>信号源值：0-3，对应不同的通道</li>
     * </ul>
     *
     * <p><b>SCL信号说明：</b>
     * <ul>
     *   <li>SCL是I2C总线的时钟信号线</li>
     *   <li>由主设备产生时钟信号</li>
     *   <li>需要选择正确的通道作为SCL源</li>
     * </ul>
     *
     * @param val SCL信号源选择值（0-3）
     */
    public void setScl(int val) {  // 设置SCL信号源方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=0（字节0）
        // 参数2：位宽度=2（2位）
        // 参数3：信号源值
        setVal(0, 2, val);  // 设置位0-1的值，宽度为2位，值为val（SCL信号源）
    }

    /**
     * 设置SDA信号源 - 配置I2C数据线的信号源
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置I2C总线SDA（数据线）的信号源选择</li>
     *   <li>寄存器位：位2-3（2位）</li>
     *   <li>信号源值：0-3，对应不同的通道</li>
     * </ul>
     *
     * <p><b>SDA信号说明：</b>
     * <ul>
     *   <li>SDA是I2C总线的双向数据信号线</li>
     *   <li>主设备和从设备都可以发送数据</li>
     *   <li>需要选择正确的通道作为SDA源</li>
     * </ul>
     *
     * @param val SDA信号源选择值（0-3）
     */
    public void setSda(int val) {  // 设置SDA信号源方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=2（字节0，位2开始）
        // 参数2：位宽度=2（2位）
        // 参数3：信号源值
        setVal(2, 2, val);  // 设置位2-3的值，宽度为2位，值为val（SDA信号源）
    }

    /**
     * 设置触发模式 - 配置I2C触发类型
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置I2C总线的触发模式</li>
     *   <li>寄存器位：位0-3（4位）</li>
     *   <li>支持多种触发类型</li>
     * </ul>
     *
     * <p><b>触发模式说明：</b>
     * <ul>
     *   <li>I2C_TRIGGER_ADDRESS_NO_ACK：地址无应答触发</li>
     *   <li>I2C_TRIGGER_EEPROM_READ_DATA：EEPROM读数据触发</li>
     *   <li>I2C_TRIGGER_FRAME1：单帧触发</li>
     *   <li>I2C_TRIGGER_FRAME2：双帧触发</li>
     *   <li>I2C_TRIGGER_WRITE_FRAME：写帧触发</li>
     * </ul>
     *
     * @param val 触发模式值（0-15）
     */
    public void setTriggerMode(int val) {  // 设置触发模式方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=1（字节1）
        // 参数2：起始位偏移=0（位0开始）
        // 参数3：位宽度=4（4位）
        // 参数4：触发模式值
        setVal(1, 0, 4, val);  // 设置字节1的位0-3的值，宽度为4位，值为val（触发模式）
    }

    /**
     * 设置触发地址 - 配置I2C触发地址
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置I2C触发条件的地址值</li>
     *   <li>寄存器位：位8-14（7位）</li>
     *   <li>I2C地址为7位（0-127）</li>
     * </ul>
     *
     * <p><b>地址说明：</b>
     * <ul>
     *   <li>I2C设备地址为7位，范围0-127</li>
     *   <li>地址触发时需要设置目标地址</li>
     *   <li>地址+读写位组成完整的8位地址帧</li>
     * </ul>
     *
     * @param val I2C地址值（0-127）
     */
    public void setAddr(int val) {  // 设置触发地址方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=1（字节1）
        // 参数2：起始位偏移=8（位8开始）
        // 参数3：位宽度=7（7位）
        // 参数4：地址值
        setVal(1, 8, 7, val);  // 设置字节1的位8-14的值，宽度为7位，值为val（触发地址）
    }

    /**
     * 设置触发数据 - 配置I2C触发数据值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置I2C触发条件的数据值</li>
     *   <li>寄存器位：位16+idx*8开始的8位</li>
     *   <li>支持多字节触发数据</li>
     * </ul>
     *
     * <p><b>数据说明：</b>
     * <ul>
     *   <li>idx=0：第一个数据字节</li>
     *   <li>idx=1：第二个数据字节</li>
     *   <li>根据触发类型配置不同的数据</li>
     * </ul>
     *
     * @param idx 数据索引（0或1）
     * @param val 数据值（0-255）
     */
    public void setData(int idx, int val) {  // 设置触发数据方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=1（字节1）
        // 参数2：起始位偏移=16+idx*8（位16开始，每个数据8位）
        // 参数3：位宽度=8（8位）
        // 参数4：数据值
        setVal(1, 16 + idx * 8, 8, val);  // 设置字节1的位(16+idx*8)开始的8位值，值为val（触发数据）
    }

    /**
     * 配置I2C总线 - 实现总线配置接口
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从I2CBus对象中获取配置参数</li>
     *   <li>配置SCL和SDA信号源</li>
     *   <li>根据触发类型配置触发参数</li>
     *   <li>配置总线电平和发送命令</li>
     * </ul>
     *
     * <p><b>配置流程：</b>
     * <ul>
     *   <li>步骤1：获取SCL和SDA通道索引</li>
     *   <li>步骤2：设置SCL和SDA信号源</li>
     *   <li>步骤3：配置总线源扩展</li>
     *   <li>步骤4：根据触发类型设置触发参数</li>
     *   <li>步骤5：设置触发模式</li>
     *   <li>步骤6：配置总线电平（I2C空闲为高电平）</li>
     *   <li>步骤7：发送总线电平寄存器</li>
     * </ul>
     *
     * @param fpgaIdx FPGA芯片索引（多FPGA架构时使用）
     * @param iBus I2C总线接口对象，包含I2C配置参数
     */
    @Override
    public void configBus(int fpgaIdx, IBus iBus) {  // 配置I2C总线方法，实现抽象方法

        // 将IBus接口转换为I2CBus具体类型
        I2CBus bus = (I2CBus) iBus;  // 类型转换，获取I2C总线对象
        
        // 获取SCL通道索引（时钟线通道）
        int scl = bus.getSclChIdx();  // 获取SCL信号所在的通道索引
        
        // 获取SDA通道索引（数据线通道）
        int sda = bus.getSdaChIdx();  // 获取SDA信号所在的通道索引
        
        // 获取总线电平寄存器对象
        // FPGACommand.getReg：根据FPGA索引和寄存器地址获取寄存器对象
        FPGAReg_BUS_LEVEL busLevel = (FPGAReg_BUS_LEVEL) FPGACommand.getReg(fpgaIdx, FPGA_BUS_LEVEL);  // 获取总线电平寄存器
        
        // 设置SCL信号源
        // chIdx2BusSrc：将通道索引转换为总线源值
        setScl(chIdx2BusSrc(scl));  // 设置SCL信号源，使用通道索引转总线源方法
        
        // 设置SDA信号源
        setSda(chIdx2BusSrc(sda));  // 设置SDA信号源，使用通道索引转总线源方法
        
        // 配置总线源扩展
        // ((scl>>2)&0x1)：获取SCL通道的高位（用于扩展选择）
        // ((sda>>1)&0x2)：获取SDA通道的高位（用于扩展选择）
        // 组合成4位的扩展源值
        busLevel.setBusSrcExt(getBusIdx(), ((scl >> 2) & 0x1) | ((sda >> 1) & 0x2));  // 设置总线源扩展
        
        // 获取触发类型
        int triggerType = bus.getTriggerType();  // 获取I2C触发类型
        
        // 获取触发地址（根据触发类型）
        int addr = bus.getTriggerAddr(triggerType);  // 获取触发地址
        
        // 根据触发类型配置不同的触发参数
        switch (triggerType) {  // 根据触发类型分支处理
            
            // 地址无应答触发：只设置地址
            case I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK:  // 地址无应答触发类型
                // 设置触发地址（取低7位）
                setAddr(addr & 0x7F);  // 设置地址，取低7位（I2C地址范围）
                break;  // 结束该分支
                
            // EEPROM读数据触发：设置关系和数据
            case I2CBus.I2C_TRIGGER_EEPROM_READ_DATA:  // EEPROM读数据触发类型
                // 设置第一个数据字节（关系值）
                setData(0, bus.getTriggerRelation());  // 设置数据0，为触发关系值
                // 设置第二个数据字节（数据值）
                setData(1, bus.getTriggerData1(triggerType));  // 设置数据1，为触发数据值
                break;  // 结束该分支
                
            // 单帧触发：设置地址和数据
            case I2CBus.I2C_TRIGGER_FRAME1:  // 单帧触发类型
                // 设置触发地址（取低7位）
                setAddr(addr & 0x7F);  // 设置地址，取低7位
                // 设置第二个数据字节
                setData(1, bus.getTriggerData1(triggerType));  // 设置数据1，为触发数据值
                break;  // 结束该分支
                
            // 双帧触发：设置地址和两个数据字节
            case I2CBus.I2C_TRIGGER_FRAME2:  // 双帧触发类型
                // 设置触发地址（取低7位）
                setAddr(addr & 0x7F);  // 设置地址，取低7位
                // 设置第一个数据字节
                setData(0, bus.getTriggerData1(triggerType));  // 设置数据0，为第一个触发数据
                // 设置第二个数据字节
                setData(1, bus.getTriggerData2());  // 设置数据1，为第二个触发数据
                break;  // 结束该分支
                
            // 写帧触发：设置10位地址和数据
            case I2CBus.I2C_TRIGGER_WRITE_FRAME:  // 写帧触发类型
                // 设置高字节地址（10位地址的高2位）
                setAddr((addr & 0x3FF) >>> 8);  // 设置地址高字节，取10位地址的高2位
                // 设置低字节地址（10位地址的低8位）
                setData(0, addr & 0xFF);  // 设置数据0，为地址低字节
                // 设置数据值
                setData(1, bus.getTriggerData1());  // 设置数据1，为触发数据值
                break;  // 结束该分支
                
            // 默认情况：不设置额外参数
            default:  // 默认分支
                break;  // 结束默认分支
        }
        
        // 设置触发模式
        setTriggerMode(triggerType);  // 设置触发模式，传入触发类型
        
        // 设置SCL总线电平
        // I2C总线空闲时为高电平（IDLE_LEVEL_HIGH）
        setLevel(busLevel, scl, IBus.IDLE_LEVEL_HIGH);  // 设置SCL电平为高（I2C空闲高电平）
        
        // 设置SDA总线电平
        // I2C总线空闲时为高电平（IDLE_LEVEL_HIGH）
        setLevel(busLevel, sda, IBus.IDLE_LEVEL_HIGH);  // 设置SDA电平为高（I2C空闲高电平）
        
        // 发送总线电平寄存器到FPGA
        FPGACommand.sendCmd(busLevel);  // 发送总线电平配置命令到FPGA
    }
}