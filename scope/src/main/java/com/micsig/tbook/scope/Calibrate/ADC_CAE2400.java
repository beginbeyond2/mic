package com.micsig.tbook.scope.Calibrate;

import java.util.Arrays;

/**
 * CAE2400型ADC芯片驱动实现类
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
 *   <li>控制CAE2400型高速ADC芯片</li>
 *   <li>管理ADC增益校准参数（FS寄存器）</li>
 *   <li>配置多通道采样模式（支持2/4/8通道）</li>
 *   <li>通过SPI扩展命令(0xAB)与ADC芯片通信</li>
 * </ul>
 * 
 * <p><b>硬件特性：</b>
 * <ul>
 *   <li>芯片型号：CAE2400（高速双通道ADC）</li>
 *   <li>最大采样时钟：6000（对应6GHz）</li>
 *   <li>最大物理通道数：2通道（通过交织可达4通道）</li>
 *   <li>FS寄存器：用于存储增益校准值</li>
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
public class ADC_CAE2400 extends ADC {
    
    /**
     * 构造函数
     * 
     * <p>创建CAE2400 ADC驱动实例，继承父类ADC的基础功能。
     * FPGACommand引用通过setFpgaCommand()方法注入。
     */
    public ADC_CAE2400() {
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
     * FS寄存器数组，用于存储增益校准值
     * 
     * <p><b>数组结构：</b>
     * <ul>
     *   <li>索引0: FPGA0的ADC0的FS值</li>
     *   <li>索引1: FPGA0的ADC1的FS值</li>
     *   <li>索引2: FPGA1的ADC0的FS值</li>
     *   <li>索引3: FPGA1的ADC1的FS值</li>
     * </ul>
     * 
     * <p><b>默认值：</b>0x9B9B（16位增益校准值）
     * 
     * <p><b>数据格式：</b>
     * <ul>
     *   <li>16位寄存器，分为高8位和低8位</li>
     *   <li>低8位（bit[7:0]）：I通道增益</li>
     *   <li>高8位（bit[15:8]）：Q通道增益</li>
     * </ul>
     */
    int[] fs = {0x9B9B, 0x9B9B, 0x9B9B, 0x9B9B};

    /**
     * 设置ADC增益校准值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置ADC内部FS（Full Scale）增益校准寄存器</li>
     *   <li>用于调整ADC的满量程增益，提高测量精度</li>
     *   <li>支持I/Q两路独立校准</li>
     *   <li>通过SPI扩展命令(0xAB)发送到ADC</li>
     * </ul>
     * 
     * <p><b>FS寄存器地址：</b>0x15D
     * 
     * <p><b>FS寄存器位结构：</b>
     * <pre>
     * bit[7:0]:   I通道增益值（i0_q1=0时写入）
     * bit[15:8]:  Q通道增益值（i0_q1=1时写入）
     * </pre>
     * 
     * <p><b>索引计算：</b>
     * <pre>
     * idx = fpgaIdx * 2 + adcIdx
     * 例如：
     *   FPGA0, ADC0 → idx=0
     *   FPGA0, ADC1 → idx=1  
     *   FPGA1, ADC0 → idx=2
     *   FPGA1, ADC1 → idx=3
     * </pre>
     * 
     * <p><b>调用链路：</b>
     * <pre>
     * setGain() → SendADData() → FPGACommand.SendADData() 
     *          → FPGAReg_SPI_EXT → Hardware.sendFpgaCmd() → SPI传输 → ADC
     * </pre>
     * 
     * @param fpgaIdx FPGA设备索引（0=主FPGA，1=从FPGA）
     * @param adcIdx ADC芯片索引（0=ADC1，1=ADC2）
     * @param i0_q1 通道选择（0=I通道，1=Q通道）
     * @param vol 增益校准值，8位无符号整数（0-255）
     */
    @Override
    public void setGain(int fpgaIdx, int adcIdx, int i0_q1, int vol) {
        // 计算FS数组索引：每个FPGA有2个ADC，所以索引 = fpgaIdx*2 + adcIdx
        int idx = fpgaIdx * 2 + adcIdx;
        
        // 参数有效性检查：确保通道索引和数组索引在有效范围内
        if (i0_q1 >= 0 && i0_q1 < 2 && idx < fs.length) {
            // 计算移位量：I通道偏移0位，Q通道偏移8位
            i0_q1 *= 8;
            
            int v = 0;
            // 同步保护：确保多线程环境下FS数组的原子性操作
            synchronized (this) {
                // 清除目标位，然后设置新值
                // fs[idx] & (~(0xFF << i0_q1)) 清除指定字节
                // (vol << i0_q1) 将新值移位到正确位置
                v = fs[idx] = (fs[idx] & (~(0xFF << i0_q1))) | (vol << i0_q1);
            }
            
            // 发送FS寄存器配置到ADC
            // 地址0x15D是CAE2400的FS寄存器地址
            SendADData(fpgaIdx, adcIdx, 0x15D, v);
        }
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
     *   <li>地址0x05: 模式配置寄存器</li>
     *   <li>地址0x02: 通道选择寄存器</li>
     * </ul>
     * 
     * <p><b>模式配置值：</b>
     * <ul>
     *   <li>0x429648: 标准模式（非交织）</li>
     *   <li>0x429648 | (1<<18): 交织模式（启用内部交织）</li>
     * </ul>
     * 
     * <p><b>通道选择值：</b>
     * <ul>
     *   <li>sel[i]=true: 发送0（选择通道A）</li>
     *   <li>sel[i]=false: 发送1<<20（选择通道B）</li>
     * </ul>
     * 
     * @param cnt 采样通道总数（2/4/8）
     * @param sel 通道选择数组，sel[i]=true表示通道i启用
     */
    @Override
    public void setChannel(int cnt, boolean[] sel) {
        // 根据通道数选择不同的配置策略
        switch (cnt) {
            case 2:  // 双通道模式
            case 4:  // 四通道模式
                // 遍历所有通道，每2个通道为一组
                for (int i = 0; i < sel.length; i += 2) {
                    // 计算FPGA索引和ADC索引
                    // i/4: 每4个通道对应一个FPGA
                    // (i%4)/2: 每2个通道对应一个ADC
                    SendADData(i / 4, (i % 4) / 2, 0x05, 0x429648);
                    
                    // 设置通道选择：启用则发送0，禁用则发送1<<20
                    SendADData(i / 4, (i % 4) / 2, 0x02, sel[i] ? 0 : 1 << 20);
                }
                break;
                
            case 8:  // 八通道模式（交织模式）
            default:
                // 配置所有FPGA和ADC为交织模式
                for (int i = 0; i < 2; i++) {  // FPGA索引
                    for (int j = 0; j < 2; j++) {  // ADC索引
                        // 发送交织模式配置：0x429648 | (1<<18)
                        // bit18=1 表示启用交织模式
                        SendADData(i, j, 0x05, 0x429648 | 1 << 18);
                    }
                }
                break;
        }
    }

    /**
     * 发送ADC寄存器配置数据
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>重写父类方法，添加日志或扩展功能</li>
     *   <li>通过SPI扩展命令(0xAB)发送ADC寄存器配置</li>
     *   <li>支持读写ADC内部寄存器</li>
     * </ul>
     * 
     * <p><b>调用链路：</b>
     * <pre>
     * SendADData() → super.SendADData() → FPGACommand.SendADData()
     *            → FPGAReg_SPI_EXT.setType(SPI_EXT_AD)
     *            → Hardware.sendFpgaCmd() → SPI传输
     * </pre>
     * 
     * @param fpgaIdx FPGA设备索引
     * @param adIdx ADC芯片索引
     * @param addr ADC寄存器地址
     * @val 寄存器值
     */
    @Override
    protected void SendADData(int fpgaIdx, int adIdx, int addr, int val) {
        // 调用父类方法发送ADC配置数据
        // 父类会通过FPGACommand.SendADData()发送SPI扩展命令
        super.SendADData(fpgaIdx, adIdx, addr, val);
    }


    /**
     * 获取实际采样通道配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据用户选择的通道，计算实际需要的采样通道数</li>
     *   <li>处理CAE2400的通道交织特性</li>
     *   <li>自动补充未使用的通道以满足ADC工作要求</li>
     * </ul>
     * 
     * <p><b>通道配置规则：</b>
     * <ul>
     *   <li>8通道模式：当任意ADC的两个通道同时启用时（如CH1和CH2），
     *       需要启用所有8个通道进行交织采样</li>
     *   <li>4通道模式：当同一ADC的两个通道启用时，需要4通道交织</li>
     *   <li>2通道模式：单通道或非交织模式，需要至少2个通道工作</li>
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
     *   <li>2通道模式：如果某个ADC没有通道启用，自动启用第一个通道</li>
     * </ul>
     * 
     * @param sel 通道选择数组（输入/输出），sel[i]=true表示通道i启用
     * @return 实际采样通道数（2/4/8）
     */
    @Override
    public int getSampleChannel(boolean[] sel) {
        int nums = 0;  // 实际采样通道数
        
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
            // 非8通道模式，计算每个ADC的启用通道数
            int a1, a2;  // a1=前4通道启用数，a2=后4通道启用数
            a1 = a2 = 0;
            
            // 统计前4个通道和后4个通道的启用数量
            for (int i = 0; i < 4; i++) {
                if (sel[i]) {
                    a1++;  // 统计CH1-CH4
                }
                if (sel[i + 4]) {
                    a2++;  // 统计CH5-CH8
                }
            }

            // 确定采样通道数
            // 如果任一ADC的两个通道都启用，需要4通道模式
            // 否则使用2通道模式
            nums = Math.max(a1, a2) < 2 ? 2 : 4;

            // 4通道模式：补充未使用的通道
            if (nums == 4) {
                // 遍历相邻通道对，如果两个都未启用，启用第一个
                for (int i = 0; i < sel.length; i += 2) {
                    if ((!sel[i]) && (!sel[i + 1])) {
                        sel[i] = true;  // 自动启用奇数通道
                    }
                }
            } else {
                // 2通道模式：确保每个ADC至少有一个通道工作
                if (a1 == 0) {
                    sel[0] = true;  // ADC1没有通道启用，启用CH1
                }
                if (a2 == 0) {
                    sel[4] = true;  // ADC3没有通道启用，启用CH5
                }
            }
        }

        return nums;
    }

    /**
     * 获取ADC最大输入时钟频率
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回CAE2400支持的最大采样时钟</li>
     *   <li>用于计算实际采样率和时基配置</li>
     *   <li>影响存储深度和采样率的计算</li>
     * </ul>
     * 
     * <p><b>硬件规格：</b>
     * <ul>
     *   <li>CAE2400最大采样率：6GHz</li>
     *   <li>返回值6000对应6GHz</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>计算采样率配置参数</li>
     *   <li>验证时基设置有效性</li>
     *   <li>确定最大带宽限制</li>
     * </ul>
     * 
     * @return 最大ADC输入时钟，单位MHz，返回6000表示6GHz
     */
    @Override
    public int getMaxAdInClk() {
        return 6000;  // 6GHz最大采样时钟
    }

    /**
     * 获取ADC最大物理通道数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回单个ADC芯片的物理通道数</li>
     *   <li>CAE2400是双通道ADC芯片</li>
     *   <li>通过交织技术可扩展到4通道</li>
     * </ul>
     * 
     * <p><b>硬件架构：</b>
     * <ul>
     *   <li>物理通道：2个（CH_A和CH_B）</li>
     *   <li>交织通道：最多4个（使用2片ADC）</li>
     *   <li>系统总通道：8个（使用4片ADC）</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>计算ADC分配策略</li>
     *   <li>确定通道交织模式</li>
     *   <li>配置采样参数</li>
     * </ul>
     * 
     * @return 单个ADC的最大物理通道数，返回2
     */
    @Override
    public int getMaxChNums() {
        return 2;  // CAE2400是双通道ADC
    }
}
