package com.micsig.tbook.scope.Calibrate;

import java.util.Arrays;

/**
 * MXT2022型ADC芯片驱动实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准与硬件控制模块）</li>
 *   <li>架构层级：硬件驱动层 - ADC驱动子层</li>
 *   <li>设计模式：继承ADC抽象类，实现IADC接口</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>控制MXT2022型高速ADC芯片</li>
 *   <li>管理ADC增益校准参数</li>
 *   <li>配置多通道采样模式（支持2/4/8通道）</li>
 *   <li>提供ADC校准功能</li>
 *   <li>通过SPI扩展命令(0xAB)与ADC芯片通信</li>
 * </ul>
 * 
 * <p><b>硬件特性：</b>
 * <ul>
 *   <li>芯片型号：MXT2022（高速双通道ADC）</li>
 *   <li>增益控制：I通道(0x03)、Q通道(0x0B)</li>
 *   <li>最大物理通道数：2通道</li>
 *   <li>支持校准功能</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：ADC抽象类（提供SendADData等基础方法）</li>
 *   <li>调用：FPGACommand（发送SPI扩展命令到ADC）</li>
 *   <li>被调用：HW_MHO68V2（硬件控制层调用ADC配置）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>示波器启动时初始化ADC芯片</li>
 *   <li>用户切换通道数量时重新配置ADC</li>
 *   <li>校准时调整ADC增益参数</li>
 *   <li>采样模式切换时配置ADC工作模式</li>
 *   <li>执行ADC自校准功能</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018
 * @see ADC ADC抽象基类
 * @see IADC ADC接口定义
 * @see FPGACommand FPGA命令管理器
 * @see HW_MHO68V2 硬件控制实现
 */
public class ADC_MXT2022 extends ADC {
    
    /**
     * 构造函数
     * 
     * <p>创建MXT2022 ADC驱动实例，继承父类ADC的基础功能。
     * FPGACommand引用通过setFpgaCommand()方法注入。
     */
    public ADC_MXT2022() {
        // 调用父类构造函数，初始化基础属性
    }

    /**
     * 初始化ADC芯片
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>执行ADC芯片上电初始化序列</li>
     *   <li>配置ADC默认工作参数</li>
     *   <li>设置初始采样模式和通道配置</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>示波器开机时调用</li>
     *   <li>FPGA重新配置后调用</li>
     * </ul>
     * 
     * <p><b>注意：</b>当前实现为空，具体初始化逻辑待补充
     */
    @Override
    public void Init() {
        // TODO: 实现ADC初始化序列
        // 1. 配置ADC寄存器默认值
        // 2. 设置采样时钟模式
        // 3. 配置输入通道选择
    }

    /**
     * 设置ADC增益校准值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置ADC内部增益校准寄存器</li>
     *   <li>用于调整ADC的增益，提高测量精度</li>
     *   <li>支持I/Q两路独立校准</li>
     *   <li>通过SPI扩展命令(0xAB)发送到ADC</li>
     * </ul>
     * 
     * <p><b>寄存器地址映射：</b>
     * <ul>
     *   <li>I通道增益寄存器: 0x03（地址计算：(0x03<<1) | 0x40 = 0x07）</li>
     *   <li>Q通道增益寄存器: 0x0B（地址计算：(0x0B<<1) | 0x40 = 0x17）</li>
     * </ul>
     * 
     * <p><b>寄存器地址计算：</b>
     * <pre>
     * 地址格式：bit[6]=1（写操作）| bit[5:1]=寄存器地址<<1 | bit[0]=0
     * 
     * I通道：0x03 → (0x03<<1) | (1<<6) = 0x06 | 0x40 = 0x46
     * Q通道：0x0B → (0x0B<<1) | (1<<6) = 0x16 | 0x40 = 0x56
     * </pre>
     * 
     * <p><b>数据格式：</b>
     * <ul>
     *   <li>15位增益值：vol & 0x7FFF</li>
     *   <li>最高位为符号位</li>
     * </ul>
     * 
     * <p><b>调用链路：</b>
     * <pre>
     * setGain() → SendADData() → FPGACommand.SendADData() 
     *          → FPGAReg_SPI_EXT → Hardware.sendFpgaCmd() → SPI传输 → ADC
     * </pre>
     * 
     * @param fpgaIdx FPGA设备索引（0=主FPGA，1=从FPGA）
     * @param adIdx ADC芯片索引（0=ADC1，1=ADC2）
     * @param i0_q1 通道选择（0=I通道，1=Q通道）
     * @param vol 增益校准值，15位无符号整数
     */
    @Override
    public void setGain(int fpgaIdx, int adIdx, int i0_q1, int vol) {
        int addr = 0;
        
        // 根据通道选择确定寄存器地址
        // I通道（i0_q1=0）：地址0x03
        // Q通道（i0_q1=1）：地址0x0B
        if (i0_q1 == 0) {
            addr = 0x03;  // I通道（In-Phase）寄存器地址
        } else {
            addr = 0x0B;  // Q通道（Quadrature）寄存器地址
        }

        // 构建SPI寄存器地址
        // 格式：bit[6]=1（写操作使能）| bit[5:1]=寄存器地址<<1 | bit[0]=0
        // 例如：0x03 → 0x46, 0x0B → 0x56
        addr = (addr << 1) | (1 << 6);

        // 发送增益值到ADC
        // 取低15位（bit0-bit14），bit15保留为符号位
        SendADData(fpgaIdx, adIdx, addr, vol & 0x7FFF);
    }

    /**
     * 设置ADC采样通道配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置ADC的通道工作模式</li>
     *   <li>支持双通道、四通道、八通道模式</li>
     *   <li>通过配置ADC内部寄存器实现通道选择</li>
     * </ul>
     * 
     * <p><b>通道模式说明：</b>
     * <ul>
     *   <li>cnt=2: 双通道模式（非交织，全带宽）</li>
     *   <li>cnt=4: 四通道模式（双ADC交织）</li>
     *   <li>cnt=8: 八通道模式（四ADC交织）</li>
     * </ul>
     * 
     * <p><b>寄存器配置：</b>
     * <ul>
     *   <li>地址: 1<<6 = 0x40（模式控制寄存器）</li>
     *   <li>值: (1<<13) | (1<<14) = 0x6000（通道使能）</li>
     * </ul>
     * 
     * <p><b>通道控制位：</b>
     * <ul>
     *   <li>bit[7]: 交织模式选择</li>
     *   <li>bit[6]: 通道选择（0=A通道，1=B通道）</li>
     *   <li>bit[14]: 通道B使能</li>
     *   <li>bit[13]: 通道A使能</li>
     * </ul>
     * 
     * @param cnt 采样通道总数（2/4/8）
     * @param sel 通道选择数组，sel[i]=true表示通道i启用
     */
    @Override
    public void setChannel(int cnt, boolean[] sel) {
        // 模式控制寄存器地址：bit6=1表示写操作
        int addr = 1 << 6;
        
        // 基础配置值：bit13和bit14置1，使能通道A和通道B
        int val = (1 << 13) | (1 << 14);
        
        // 根据通道数选择不同的配置策略
        switch (cnt) {
            case 2:  // 双通道模式
            case 4:  // 四通道模式
                // 遍历所有通道，每2个通道为一组
                for (int i = 0; i < sel.length; i += 2) {
                    // 计算FPGA索引和ADC索引
                    // i/4: 每4个通道对应一个FPGA
                    // (i%4)/2: 每2个通道对应一个ADC
                    
                    // 构建通道选择值：
                    // bit7=1: 启用交织模式
                    // bit6: sel[i]=true选择通道A(0)，sel[i]=false选择通道B(1)
                    int channelVal = val | (1 << 7) | (sel[i] ? (1 << 6) : 0);
                    
                    SendADData(i / 4, (i % 4) / 2, addr, channelVal);
                }
                break;
                
            case 8:  // 八通道模式（交织模式）
            default:
                // 配置所有FPGA和ADC为双通道模式
                for (int i = 0; i < 2; i++) {  // FPGA索引
                    for (int j = 0; j < 2; j++) {  // ADC索引
                        // 发送双通道模式配置
                        SendADData(i, j, addr, val);
                    }
                }
                break;
        }
    }

    /**
     * 执行ADC校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>执行ADC自校准功能</li>
     *   <li>校准内部参考电压和偏移</li>
     *   <li>提高ADC测量精度</li>
     * </ul>
     * 
     * <p><b>校准流程：</b>
     * <ol>
     *   <li>发送校准使能信号（forceAdCablition(1)）</li>
     *   <li>发送校准完成信号（forceAdCablition(0)）</li>
     *   <li>ADC内部执行校准序列</li>
     * </ol>
     * 
     * <p><b>调用链路：</b>
     * <pre>
     * setCalibrate() → fpgaCommand.forceAdCablition(1) 
     *               → FPGACommand.forceAdCablition() 
     *               → SPI扩展命令 → FPGA → ADC
     * </pre>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>示波器开机后首次使用前校准</li>
     *   <li>环境温度变化较大时校准</li>
     *   <li>用户手动触发校准</li>
     *   <li>长时间使用后定期校准</li>
     * </ul>
     */
    @Override
    public void setCalibrate() {
        // 确保FPGACommand已初始化
        if (fpgaCommand != null) {
            // 发送校准使能信号
            // 参数1表示开始校准
            fpgaCommand.forceAdCablition(1);
            
            // 发送校准完成信号
            // 参数0表示校准完成，ADC开始正常工作
            fpgaCommand.forceAdCablition(0);
        }
    }


    /**
     * 获取实际采样通道配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据用户选择的通道，计算实际需要的采样通道数</li>
     *   <li>处理MXT2022的通道交织特性</li>
     *   <li>自动补充未使用的通道以满足ADC工作要求</li>
     * </ul>
     * 
     * <p><b>通道配置规则：</b>
     * <ul>
     *   <li>8通道模式：当任意ADC的两个通道同时启用时（如CH1和CH2），
     *       需要启用所有8个通道进行交织采样</li>
     *   <li>4通道模式：当同一ADC的两个通道启用时，需要4通道交织</li>
     * </ul>
     * 
     * <p><b>通道分组（按ADC）：</b>
     * <pre>
     * ADC1: CH1(0), CH2(1)
     * ADC2: CH3(2), CH4(3)
     * ADC3: CH5(4), CH6(5)
     * ADC4: CH7(6), CH8(7)
     * </pre>
     * 
     * <p><b>自动补充逻辑：</b>
     * <ul>
     *   <li>4通道模式：如果相邻两个通道都未启用，自动启用第一个通道</li>
     * </ul>
     * 
     * @param sel 通道选择数组（输入/输出），sel[i]=true表示通道i启用
     * @return 实际采样通道数（4或8）
     */
    @Override
    public int getSampleChannel(boolean[] sel) {
        int nums = 0;
        
        // 检查是否需要8通道交织模式
        // 条件：任意ADC的两个通道同时启用
        if ((sel[0] && sel[1])        // ADC1: CH1和CH2同时启用
                || (sel[2] && sel[3]) // ADC2: CH3和CH4同时启用
                || (sel[4] && sel[5]) // ADC3: CH5和CH6同时启用
                || (sel[6] && sel[7]) // ADC4: CH7和CH8同时启用
        ) {
            // 8通道模式：启用所有通道
            Arrays.fill(sel, true);
            nums = 8;
        } else {
            // 4通道模式：补充未使用的通道
            // 遍历相邻通道对，如果两个都未启用，启用第一个
            for (int i = 0; i < sel.length; i += 2) {
                if ((!sel[i]) && (!sel[i + 1])) {
                    sel[i] = true;  // 自动启用奇数通道
                }
            }
            nums = 4;
        }

        return nums;
    }
}
