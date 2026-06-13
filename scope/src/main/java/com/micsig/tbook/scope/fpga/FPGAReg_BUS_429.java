package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import com.micsig.tbook.scope.Bus.ARINC429Bus;  // 导入ARINC429总线类，提供ARINC429总线配置参数
import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口类，定义总线通用接口

/**
 * FPGA ARINC429总线寄存器类 - 用于配置ARINC429总线解码和触发参数
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg_BUS基类）</li>
 *   <li>职责类型：ARINC429总线配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置ARINC429总线的信号源</li>
 *   <li>配置ARINC429总线波特率</li>
 *   <li>配置ARINC429数据格式</li>
 *   <li>配置ARINC429触发模式和触发条件</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供ARINC429总线解码的硬件配置接口</li>
 *   <li>支持多种触发模式（字触发、标签触发、数据触发等）</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * ARINC429总线寄存器（160位，20字节）
 * │
 * ├── 字节0：信号源配置
 * │     └── 位2-3：ARINC429信号源选择（0-3）
 * │
 * ├── 字节1：触发配置
 * │     └── 位0-4：触发模式（5位）
 * │
 * ├── 字节2-3：数据配置
 * │     ├── 字节2：数据0（标签、SDI等）
 * │     └── 字节3：数据1（数据、SSM等）
 * │
 * ├── 字节4：波特率配置
 * │     └── 位4-17：波特率分频值（14位）
 * │
 * └── 字节5：数据格式配置
 *       └── 位30-31：数据格式（2位）
 * </pre>
 *
 * <p><b>ARINC429总线说明：</b>
 * <ul>
 *   <li>ARINC429是航空电子数据总线标准</li>
 *   <li>广泛应用于民用航空电子系统</li>
 *   <li>采用单发送器多接收器架构</li>
 *   <li>数据格式：32位字，包含标签、SDI、数据、SSM等字段</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>ARINC429总线解码：配置ARINC429总线解码参数</li>
 *   <li>ARINC429总线触发：配置ARINC429总线触发条件</li>
 *   <li>波特率设置：设置ARINC429总线波特率</li>
 *   <li>标签过滤：设置标签触发条件</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg_BUS（总线寄存器基类）</li>
 *   <li>依赖：ARINC429Bus（ARINC429总线配置类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：FPGAReg_BUS_LEVEL（总线电平寄存器）</li>
 * </ul>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-30
 * @see FPGAReg_BUS 总线寄存器基类
 * @see ARINC429Bus ARINC429总线配置类
 * @see IBus 总线接口类
 */
public class FPGAReg_BUS_429 extends FPGAReg_BUS {  // 继承FPGAReg_BUS基类，复用总线寄存器操作方法
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 构造函数 - 初始化ARINC429总线寄存器
     *
     * <p><b>初始化参数：</b>
     * <ul>
     *   <li>寄存器地址：由参数addr指定</li>
     *   <li>寄存器大小：20字节（160位）</li>
     * </ul>
     *
     * @param addr 寄存器地址（FPGA总线寄存器地址）
     */
    public FPGAReg_BUS_429(int addr) {  // 构造方法：初始化ARINC429总线寄存器
        super(addr, 20);  // 调用父类构造函数，设置寄存器地址和大小
    }  // 构造方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 配置参数设置
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 设置信号源
     * 设置ARINC429总线的信号源通道
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制FPGA从哪个通道获取ARINC429总线信号</li>
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
     * 设置波特率分频值
     * 设置ARINC429总线的波特率分频参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制ARINC429总线的通信波特率</li>
     *   <li>波特率通过分频值计算</li>
     *   <li>寄存器位：位4-17（14位）</li>
     * </ul>
     *
     * <p><b>波特率计算：</b>
     * <ul>
     *   <li>波特率 = 125000000 / (分频值 + 1)</li>
     *   <li>分频值范围：0-16383（14位）</li>
     * </ul>
     *
     * @param val 波特率分频值（0-16383）
     */
    public void setRate(int val){  // 公有方法：设置波特率分频值
        setVal(4,14,val);  // 设置位4-17的值，宽度为14位，值为val（波特率分频值）
    }  // 方法结束
    
    /**
     * 设置数据格式
     * 设置ARINC429的数据格式
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制ARINC429的数据格式</li>
     *   <li>数据格式：LABEL_SDI_DATA_SSM、LABEL_DATA_SSM、LABEL_DATA</li>
     *   <li>寄存器位：位30-31（2位）</li>
     * </ul>
     *
     * @param val 数据格式值（0-2，对应不同数据格式）
     */
    public void setFormat(int val){  // 公有方法：设置数据格式
        setVal(30,2,val);  // 设置位30-31的值，宽度为2位，值为val（数据格式）
    }  // 方法结束
    
    /**
     * 设置触发模式
     * 设置ARINC429总线的触发模式
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制ARINC429总线的触发方式</li>
     *   <li>触发模式：字触发、标签触发、数据触发等</li>
     *   <li>寄存器位：位0-4（5位）</li>
     * </ul>
     *
     * @param val 触发模式值（0-31，对应不同触发模式）
     */
    public void setTriggerMode(int val){  // 公有方法：设置触发模式
        setVal(1,0,5,val);  // 设置位0-4的值，宽度为5位，值为val（触发模式）
    }  // 方法结束
    
    /**
     * 设置数据字段
     * 设置ARINC429的数据字段值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制ARINC429的数据字段值</li>
     *   <li>数据字段索引：0-1（对应字节0-1）</li>
     *   <li>寄存器位置：字索引 = 2 + idx</li>
     * </ul>
     *
     * @param idx 数据字段索引（0-1，对应字节0-1）
     * @param val 数据字段值（0-255，8位）
     */
    public void setData(int idx,int val){  // 公有方法：设置数据字段
        setVal(2+idx,val);  // 设置字2+idx的值，值为val（数据字段）
    }  // 方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 总线配置
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 配置ARINC429总线参数
     * 根据ARINC429Bus配置对象设置FPGA寄存器参数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从ARINC429Bus对象获取配置参数</li>
     *   <li>根据触发类型设置不同的触发条件</li>
     *   <li>配置信号源、波特率、数据格式等参数</li>
     *   <li>发送寄存器配置到FPGA</li>
     * </ul>
     *
     * <p><b>配置流程：</b>
     * <ol>
     *   <li>获取ARINC429Bus配置参数（格式、触发类型、数据等）</li>
     *   <li>设置信号源</li>
     *   <li>设置总线信号源扩展</li>
     *   <li>计算并设置波特率分频值</li>
     *   <li>设置数据格式</li>
     *   <li>设置触发模式</li>
     *   <li>根据触发类型设置触发条件</li>
     *   <li>配置总线电平</li>
     *   <li>发送寄存器配置到FPGA</li>
     * </ol>
     *
     * @param fpgaIdx FPGA索引（用于多FPGA系统）
     * @param iBus ARINC429总线配置对象（ARINC429Bus类型）
     */
    @Override
    public void configBus(int fpgaIdx,IBus iBus) {  // 公有方法：配置ARINC429总线参数
        // 将IBus接口转换为ARINC429Bus类型
        ARINC429Bus bus = (ARINC429Bus) iBus;  // 类型转换：获取ARINC429Bus配置对象
        
        // 获取ARINC429总线配置参数
        int format = bus.getFormat();  // 获取数据格式
        int triggerType = bus.getTriggerType();  // 获取触发类型
        int data = bus.getData(triggerType);  // 获取数据
        int label = bus.getLabel(triggerType);  // 获取标签
        int sdi = bus.getSdi(triggerType);  // 获取SDI（源/目标标识）
        int ssm = bus.getSSM(triggerType);  // 获取SSM（符号/状态矩阵）
        int src = (bus.getSrcChIdx());  // 获取信号源通道索引
        
        // 获取总线电平寄存器
        FPGAReg_BUS_LEVEL busLevel = (FPGAReg_BUS_LEVEL) FPGACommand.getReg(fpgaIdx,FPGA_BUS_LEVEL);  // 获取总线电平寄存器
        
        // 设置信号源
        setSrc(chIdx2BusSrc(src));  // 设置信号源（通道索引转换为总线信号源）
        
        // 设置总线信号源扩展
        busLevel.setBusSrcExt(getBusIdx(),((src>>2)&0x1));  // 设置总线信号源扩展（高位通道选择）
        
        // 计算并设置波特率分频值
        setRate(125000000/bus.getBaudRate() - 1);  // 设置波特率分频值：125000000 / 波特率 - 1
        
        // 设置数据格式
        setFormat(format);  // 设置数据格式
        
        // 设置触发模式
        setTriggerMode(triggerType);  // 设置触发模式
        
        // 根据触发类型设置触发条件
        switch (triggerType) {  // switch语句：根据触发类型选择触发条件
            case ARINC429Bus.ARINC429_TRIGGER_WORD:  // case分支：字触发
                // 设置完整字触发条件
                setData(1,bus.getWord());  // 设置数据字段1为完整字值
                break;  // 跳出switch语句
            case ARINC429Bus.ARINC429_TRIGGER_LABEL:  // case分支：标签触发
                // 设置标签触发条件
                setData(0,label);  // 设置数据字段0为标签值
                break;  // 跳出switch语句
            case ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI:  // case分支：标签+SDI触发
                // 设置标签触发条件
                setData(0,label);  // 设置数据字段0为标签值
                // 注意：此处没有break，继续执行下一个case
            case ARINC429Bus.ARINC429_TRIGGER_SDI:  // case分支：SDI触发
                // 设置SDI触发条件
                setData(1,sdi);  // 设置数据字段1为SDI值
                break;  // 跳出switch语句
            case ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA:  // case分支：标签+数据触发
                // 设置标签触发条件
                setData(0,label);  // 设置数据字段0为标签值
                // 注意：此处没有break，继续执行下一个case
            case ARINC429Bus.ARINC429_TRIGGER_DATA:  // case分支：数据触发
            {
                // 根据数据格式设置数据触发条件
                switch (format){  // switch语句：根据数据格式设置数据值
                    case ARINC429Bus.ARINC429_LABEL_SDI_DATA_SSM:  // case分支：LABEL_SDI_DATA_SSM格式
                        // 数据左移4位（因为包含SDI字段）
                        setData(1,data<<4);  // 设置数据字段1为数据值左移4位
                        break;  // 跳出switch语句
                    case ARINC429Bus.ARINC429_LABEL_DATA_SSM:  // case分支：LABEL_DATA_SSM格式
                        // 数据左移2位（因为不包含SDI字段）
                        setData(1,data<<2);  // 设置数据字段1为数据值左移2位
                        break;  // 跳出switch语句
                    case ARINC429Bus.ARINC429_LABEL_DATA:  // case分支：LABEL_DATA格式
                        // 数据不移位
                        setData(1,data);  // 设置数据字段1为数据值
                        break;  // 跳出switch语句
                }  // switch语句结束
            }  // case分支结束
            break;  // 跳出switch语句
            case ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM:  // case分支：标签+SSM触发
                // 设置标签触发条件
                setData(0,label);  // 设置数据字段0为标签值
                // 注意：此处没有break，继续执行下一个case
            case ARINC429Bus.ARINC429_TRIGGER_SSM:  // case分支：SSM触发
                // 设置SSM触发条件
                setData(1,ssm);  // 设置数据字段1为SSM值
                break;  // 跳出switch语句
            default:  // default分支：默认情况
                // 设置默认触发条件
                setData(0,0);  // 设置数据字段0为0
                setData(1,0);  // 设置数据字段1为0
                break;  // 跳出switch语句
        }  // switch语句结束
        
        // 配置总线电平（ARINC429总线空闲电平为高电平）
        setLevel(busLevel,src,IBus.IDLE_LEVEL_HIGH);  // 设置总线空闲电平为高电平
        
        // 设置总线信号源（再次确认）
        busLevel.setSrc(getBusIdx(),chIdx2BusSrc(src));  // 设置总线信号源
        
        // 发送总线电平寄存器配置到FPGA
        FPGACommand.sendCmd(busLevel);  // 发送总线电平寄存器配置
    }  // 方法结束
}  // FPGAReg_BUS_429类结束