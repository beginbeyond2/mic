package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import android.util.Log;  // 导入Android日志类，用于调试输出

import com.micsig.tbook.hardware.HardwareProduct;  // 导入硬件产品类，用于判断产品型号
import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口类，定义总线通用接口
import com.micsig.tbook.scope.Scope;  // 导入示波器类，获取示波器实例
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入通道工厂类，获取通道数量信息

/**
 * FPGA总线寄存器抽象基类 - 为各种串行总线解码提供统一的配置接口
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：抽象基类模式（为具体总线类提供模板）</li>
 *   <li>职责类型：总线配置管理器基类</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义总线配置的抽象接口（configBus方法）</li>
 *   <li>提供通道索引到总线源的转换方法</li>
 *   <li>提供总线索引获取方法</li>
 *   <li>提供总线电平配置辅助方法</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>统一各种串行总线（I2C、SPI、CAN、UART等）的配置接口</li>
 *   <li>封装通道索引与FPGA总线源的映射逻辑</li>
 *   <li>支持多FPGA芯片架构下的总线配置</li>
 *   <li>提供可扩展的总线配置框架</li>
 * </ul>
 *
 * <p><b>支持的总线类型：</b>
 * <ul>
 *   <li>I2C总线：两线制串行总线，用于短距离通信</li>
 *   <li>SPI总线：四线制串行总线，用于高速数据传输</li>
 *   <li>CAN总线：控制器局域网，用于汽车和工业控制</li>
 *   <li>UART总线：通用异步收发传输器，用于串口通信</li>
 *   <li>LIN总线：局域互联网络，用于汽车子系统</li>
 *   <li>1553B总线：军用航空总线标准</li>
 *   <li>ARINC429总线：航空数据总线标准</li>
 * </ul>
 *
 * <p><b>通道索引到总线源映射说明：</b>
 * <ul>
 *   <li>总线源（BusSrc）是FPGA内部的总线信号选择器</li>
 *   <li>根据采样通道数量和产品型号进行不同的映射</li>
 *   <li>MHO68V1产品有特殊的映射逻辑</li>
 *   <li>支持4通道以下和4通道以上两种模式</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>串行总线解码配置：配置各种串行总线的解码参数</li>
 *   <li>总线触发设置：设置总线触发条件和参数</li>
 *   <li>多通道总线分析：分析多通道上的总线信号</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：IBus（总线接口类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：HardwareProduct（硬件产品类）</li>
 *   <li>依赖：Scope（示波器类）</li>
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
 * // 具体总线类继承FPGAReg_BUS
 * public class FPGAReg_BUS_I2C extends FPGAReg_BUS {
 *     public void configBus(int fpgaIdx, IBus iBus) {
 *         // 具体I2C配置实现
 *     }
 * }
 *
 * // 使用总线寄存器
 * FPGAReg_BUS_I2C regI2C = new FPGAReg_BUS_I2C(FPGA_BUS1_ADDR);
 * regI2C.configBus(fpgaIdx, i2cBus);
 * FPGACommand.sendCmd(regI2C);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-30
 * @see FPGAReg FPGA寄存器基类
 * @see IBus 总线接口类
 * @see FPGAReg_BUS_I2C I2C总线寄存器类
 * @see FPGAReg_BUS_SPI SPI总线寄存器类
 * @see FPGAReg_BUS_CAN CAN总线寄存器类
 */
public abstract class FPGAReg_BUS extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法，定义为抽象类

    /**
     * 构造函数 - 初始化总线寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>由具体总线子类调用，传入具体参数</li>
     * </ul>
     *
     * @param addr 寄存器地址（由具体总线类指定）
     * @param len 寄存器大小（字节长度，由具体总线类指定）
     */
    public FPGAReg_BUS(int addr, int len) {  // 构造函数，接收寄存器地址和长度
        // 调用父类构造函数，传入寄存器地址和大小
        super(addr, len);  // 初始化总线寄存器，地址和大小由子类指定
    }

    /**
     * 配置总线参数 - 抽象方法，由具体总线类实现
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>抽象方法，必须由具体总线类实现</li>
     *   <li>根据总线类型配置相应的FPGA寄存器参数</li>
     *   <li>包括信号源选择、波特率、触发条件等</li>
     * </ul>
     *
     * <p><b>实现要求：</b>
     * <ul>
     *   <li>从IBus对象中获取总线配置参数</li>
     *   <li>调用各种set方法设置寄存器值</li>
     *   <li>配置总线电平和信号源</li>
     *   <li>发送配置到FPGA</li>
     * </ul>
     *
     * @param fpgaIdx FPGA芯片索引（多FPGA架构时使用）
     * @param iBus 总线接口对象，包含总线配置参数
     */
    public abstract void configBus(int fpgaIdx, IBus iBus);  // 抽象方法，由子类实现具体的总线配置逻辑

    /**
     * 通道索引转总线源 - 将通道索引转换为FPGA总线源选择值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据通道索引计算对应的FPGA总线源选择值</li>
     *   <li>考虑采样通道数量和产品型号</li>
     *   <li>支持MHO68V1产品的特殊映射逻辑</li>
     * </ul>
     *
     * <p><b>映射逻辑说明：</b>
     * <ul>
     *   <li>采样通道数≤4时：根据产品型号进行特殊映射</li>
     *   <li>MHO68V1产品：遍历已选通道，计算源值</li>
     *   <li>其他产品：根据通道偏移计算源值</li>
     *   <li>采样通道数>4时：直接使用通道偏移作为源值</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>chIdx：通道索引（0=CH1，1=CH2，...）</li>
     *   <li>返回值：总线源选择值（0-3）</li>
     * </ul>
     *
     * @param chIdx 通道索引（0-7）
     * @return 总线源选择值（0-3）
     */
    public int chIdx2BusSrc(int chIdx) {  // 通道索引转总线源方法
        // 获取示波器实例
        Scope scope = Scope.getInstance();  // 获取示波器单例实例
        
        // 创建通道选择数组，用于记录哪些通道被选中
        boolean[] sel = new boolean[ChannelFactory.CH_CNT];  // 创建布尔数组，长度为通道总数
        
        // 获取当前采样开启的通道数量和选择状态
        // 参数1：是否运行状态（true表示运行中）
        // 参数2：选择数组，返回哪些通道被选中
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true), sel);  // 获取采样开启的通道数量和选择状态
        
        // 将通道索引转换为FPGA索引（多FPGA架构下）
        int fpgaIdx = FPGACommand.chIdxToFpgaIdx(chIdx);  // 通道索引转FPGA索引
        
        // 获取当前FPGA的起始通道索引
        int beginIdx = FPGACommand.beginChIdx(fpgaIdx);  // 获取FPGA起始通道索引
        
        // 获取当前FPGA的结束通道索引
        int endIdx = FPGACommand.endChIdx(fpgaIdx);  // 获取FPGA结束通道索引
        
        // 初始化总线源值
        int src = 0;  // 总线源选择值，初始为0
        
        // 根据采样通道数量选择不同的映射逻辑
        if (cnt <= 4) {  // 如果采样通道数≤4
            // 判断是否为MHO68V1产品
            if (HardwareProduct.isMHO68V1()) {  // 如果是MHO68V1产品
                // MHO68V1特殊映射逻辑：遍历已选通道
                for (int i = beginIdx; i < endIdx; i++) {  // 遍历当前FPGA的通道范围
                    if (sel[i]) {  // 如果该通道被选中
                        if (i == chIdx) {  // 如果找到目标通道
                            break;  // 找到目标通道，退出循环
                        }
                        src = 2;  // 未找到目标通道，源值设为2
                    }
                }
            } else {  // 如果不是MHO68V1产品
                // 其他产品：根据通道偏移计算源值
                // (chIdx - beginIdx) / 2：计算通道在FPGA内的相对位置
                // * 2：乘以2得到源值
                src = ((chIdx - beginIdx) / 2) * 2;  // 计算总线源值
            }
        } else {  // 如果采样通道数>4
            // 通道数>4时：直接使用通道偏移作为源值
            src = chIdx - beginIdx;  // 总线源值等于通道索引减去起始索引
        }
        
        // 返回计算得到的总线源值
        return src;  // 返回总线源选择值
    }

    /**
     * 获取总线索引 - 根据寄存器地址判断总线索引
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据寄存器地址判断是总线1还是总线2</li>
     *   <li>FPGA_BUS1_ADDR对应总线索引0</li>
     *   <li>FPGA_BUS2_ADDR对应总线索引1</li>
     * </ul>
     *
     * <p><b>总线索引说明：</b>
     * <ul>
     *   <li>总线索引用于区分两条独立的总线解码通道</li>
     *   <li>索引0：总线1（主总线）</li>
     *   <li>索引1：总线2（副总线）</li>
     * </ul>
     *
     * @return 总线索引（0或1）
     */
    public int getBusIdx() {  // 获取总线索引方法
        // 初始化总线索引为0（默认总线1）
        int busIdx = 0;  // 总线索引，初始为0
        
        // 判断寄存器地址是否为总线2地址
        if (getAddr() == FPGA_BUS2_ADDR) {  // 如果寄存器地址等于FPGA_BUS2_ADDR
            busIdx = 1;  // 总线索引设为1（总线2）
        }
        
        // 返回总线索引
        return busIdx;  // 返回总线索引（0或1）
    }

    /**
     * 设置总线电平 - 辅助方法，用于配置总线信号的空闲电平
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置总线信号的空闲电平（高或低）</li>
     *   <li>根据通道索引选择不同的设置方法</li>
     *   <li>通道<CH5使用setLevel方法</li>
     *   <li>通道≥CH5使用setLevelExt方法</li>
     * </ul>
     *
     * <p><b>空闲电平说明：</b>
     * <ul>
     *   <li>IDLE_LEVEL_HIGH：空闲时为高电平</li>
     *   <li>IDLE_LEVEL_LOW：空闲时为低电平</li>
     *   <li>不同的总线协议有不同的空闲电平要求</li>
     * </ul>
     *
     * @param busLevel 总线电平寄存器对象
     * @param chIdx 通道索引（0-7）
     * @param idleLevel 空闲电平（IDLE_LEVEL_HIGH或IDLE_LEVEL_LOW）
     */
    protected void setLevel(FPGAReg_BUS_LEVEL busLevel, int chIdx, int idleLevel) {  // 设置总线电平方法（受保护方法）
        // 判断通道索引是否小于CH5（前4个通道）
        if (chIdx < ChannelFactory.CH5) {  // 如果通道索引小于CH5
            // 使用setLevel方法设置前4个通道的电平
            // 参数1：总线索引
            // 参数2：总线源（通过chIdx2BusSrc转换）
            // 参数3：空闲电平值
            busLevel.setLevel(getBusIdx(), chIdx2BusSrc(chIdx), idleLevel);  // 设置前4通道的电平
        } else {  // 如果通道索引≥CH5（后4个通道）
            // 使用setLevelExt方法设置后4个通道的电平
            // 参数1：总线索引
            // 参数2：总线源（通过chIdx2BusSrc转换）
            // 参数3：空闲电平值
            busLevel.setLevelExt(getBusIdx(), chIdx2BusSrc(chIdx), idleLevel);  // 设置后4通道的电平
        }
    }
}