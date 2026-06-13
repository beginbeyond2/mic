package com.micsig.tbook.scope.Calibrate;  // 定义包名：示波器校准与硬件控制模块

import java.util.Arrays;  // 导入Arrays类：数组操作工具

/**
 * EV12AQ60x ADC芯片驱动实现 - 4通道高速ADC驱动
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准与硬件控制模块）</li>
 *   <li>架构层级：硬件驱动层 - ADC驱动实现</li>
 *   <li>设计模式：策略模式（实现ADC抽象基类）</li>
 *   <li>芯片型号：EV12AQ60x（4通道高速ADC芯片）</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现EV12AQ60x ADC芯片的寄存器配置</li>
 *   <li>支持2/4/8通道采样模式切换</li>
 *   <li>提供偏移和增益校准功能</li>
 *   <li>管理通道选择和时钟模式配置</li>
 * </ul>
 * 
 * <p><b>EV12AQ60x芯片特性：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   EV12AQ60x - 4通道高速ADC芯片                                           │
 * │                                                                          │
 * │   主要特性：                                                              │
 *   │   ├── 采样率：最高6GSPS                                                │
 * │   │   ├── 分辨率：12位                                                   │
 * │   │   ├── 通道数：4通道（A/B/C/D）                                        │
 * │   │   └── 输入带宽：高达数GHz                                             │
 * │                                                                          │
 * │   通道映射（四通道模式）：                                                 │
 * │   ├── CH1,CH5 → IN3 → D通道 → 索引3                                      │
 * │   ├── CH2,CH6 → IN0 → A通道 → 索引0                                      │
 * │   ├── CH3,CH7 → IN2 → C通道 → 索引2                                      │
 * │   └── CH4,CH8 → IN1 → B通道 → 索引1                                      │
 * │                                                                          │
 * │   通道映射（双通道模式）：                                                 │
 * │   ├── CH1,CH5 → IN3 → A,B通道 → 索引0,1                                  │
 * │   └── CH2,CH6 → IN0 → C,D通道 → 索引2,3                                  │
 * │                                                                          │
 * │   通道映射（单通道模式）：                                                 │
 * │   ├── CH1,CH5 → IN3 → A,B,C,D通道 → 索引0,1,2,3                          │
 * │   └── CH2,CH6 → IN0 → A,B,C,D通道 → 索引0,1,2,3                          │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>寄存器地址映射：</b>
 * <pre>
 *   偏移寄存器地址：
 *   ├── 通道0: 0x0124
 *   ├── 通道1: 0x0324
 *   ├── 通道2: 0x0524
 *   └── 通道3: 0x0724
 *   
 *   增益寄存器地址：
 *   ├── 通道0: 0x0122
 *   ├── 通道1: 0x0322
 *   ├── 通道2: 0x0522
 *   └── 通道3: 0x0722
 *   
 *   通道选择寄存器: 0x000B
 *   时钟模式寄存器: 0x000A
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   IADC（接口）
 *       │
 *       └── ADC（抽象基类）
 *               │
 *               └── ADC_EV12AQ60x（EV12AQ60x驱动实现）
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>MHO系列示波器使用EV12AQ60x芯片时，通过工厂模式创建此驱动</li>
 *   <li>通道配置时调用setChannel()设置采样模式</li>
 *   <li>校准时调用setOffset()/setGain()调整偏移和增益</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：ADC（ADC驱动抽象基类）</li>
 *   <li>依赖：FPGACommand（通过父类引用发送SPI命令）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018
 * @see ADC ADC驱动抽象基类
 * @see IADC ADC接口定义
 */
public class ADC_EV12AQ60x extends ADC {
    
    /**
     * 构造方法：初始化EV12AQ60x ADC驱动
     * 
     * <p>创建EV12AQ60x驱动实例，调用父类构造方法。
     */
    public ADC_EV12AQ60x() {

    }

    /**
     * 初始化ADC芯片
     * 
     * <p>重写父类抽象方法，当前为空实现。
     * 可扩展用于ADC芯片的初始化配置。
     */
    @Override
    public void Init() {
    }

    /**
     * 设置ADC偏移校准值
     * 
     * <p>配置指定通道的偏移校准寄存器。
     * 偏移校准用于消除ADC的直流偏移误差。
     * 
     * <p><b>寄存器地址映射：</b>
     * <ul>
     *   <li>i0_q1=0: 地址0x0124</li>
     *   <li>i0_q1=1: 地址0x0324</li>
     *   <li>i0_q1=2: 地址0x0524</li>
     *   <li>i0_q1=3: 地址0x0724</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA设备索引（0=主FPGA，1=从FPGA）
     * @param adIdx ADC芯片索引（当前未使用）
     * @param i0_q1 通道选择索引（0-3，对应4个通道）
     * @param vol 偏移校准值
     */
    @Override
    public void setOffset(int fpgaIdx, int adIdx, int i0_q1, int vol) {
        final int[] addr = {0x0124, 0x0324, 0x0524, 0x0724};  // 偏移寄存器地址数组
        if (i0_q1 >= 0 && i0_q1 < addr.length) {  // 检查通道索引有效性
            SendADData(fpgaIdx, 0, addr[i0_q1], vol);  // 发送偏移校准值到对应寄存器
        }
    }

    /**
     * 设置ADC增益校准值
     * 
     * <p>配置指定通道的增益校准寄存器。
     * 增益校准用于调整ADC的增益误差，确保测量精度。
     * 
     * <p><b>寄存器地址映射：</b>
     * <ul>
     *   <li>i0_q1=0: 地址0x0122</li>
     *   <li>i0_q1=1: 地址0x0322</li>
     *   <li>i0_q1=2: 地址0x0522</li>
     *   <li>i0_q1=3: 地址0x0722</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA设备索引（0=主FPGA，1=从FPGA）
     * @param adIdx ADC芯片索引（当前未使用）
     * @param i0_q1 通道选择索引（0-3，对应4个通道）
     * @param vol 增益校准值
     */
    @Override
    public void setGain(int fpgaIdx, int adIdx, int i0_q1, int vol) {
        final int[] addr = {0x0122, 0x0322, 0x0522, 0x0722};  // 增益寄存器地址数组
        if (i0_q1 >= 0 && i0_q1 < addr.length) {  // 检查通道索引有效性
            SendADData(fpgaIdx, 0, addr[i0_q1], vol);  // 发送增益校准值到对应寄存器
        }
    }
    
    /**
     * 通道映射说明
     * 
     * <p>EV12AQ60x芯片支持三种通道模式，通道映射关系如下：
     * 
     * <p><b>四通道模式（cnt=4）：</b>
     * <ul>
     *   <li>CH1,CH5 → IN3 → D通道 → 索引3</li>
     *   <li>CH2,CH6 → IN0 → A通道 → 索引0</li>
     *   <li>CH3,CH7 → IN2 → C通道 → 索引2</li>
     *   <li>CH4,CH8 → IN1 → B通道 → 索引1</li>
     * </ul>
     * 
     * <p><b>双通道模式（cnt=2）：</b>
     * <ul>
     *   <li>CH1,CH5 → IN3 → A,B通道 → 索引0,1</li>
     *   <li>CH2,CH6 → IN0 → C,D通道 → 索引2,3</li>
     * </ul>
     * 
     * <p><b>单通道模式（cnt=8）：</b>
     * <ul>
     *   <li>CH1,CH5 → IN3 → A,B,C,D通道 → 索引0,1,2,3</li>
     *   <li>CH2,CH6 → IN0 → A,B,C,D通道 → 索引0,1,2,3</li>
     * </ul>
     */
    
    /**
     * 设置ADC采样通道
     * 
     * <p>根据通道数量配置ADC的通道选择和时钟模式。
     * 支持2通道、4通道、8通道三种模式。
     * 
     * <p><b>配置流程：</b>
     * <ol>
     *   <li>根据通道数量选择对应的配置参数</li>
     *   <li>发送通道选择命令到寄存器0x000B</li>
     *   <li>发送时钟模式命令到寄存器0x000A</li>
     *   <li>调用adSync()同步ADC配置</li>
     * </ol>
     * 
     * <p><b>模式配置：</b>
     * <ul>
     *   <li>2通道模式：通道选择=sel[1]/sel[5]选择，时钟模式=0</li>
     *   <li>4通道模式：通道选择=3，时钟模式=2</li>
     *   <li>8通道模式：通道选择=4，时钟模式=3</li>
     * </ul>
     * 
     * @param cnt 通道数量（2/4/8）
     * @param sel 通道选择数组，sel[i]=true表示通道i启用
     */
    @Override
    public void setChannel(int cnt, boolean[] sel) {

        switch (cnt) {  // 根据通道数量选择配置
            case 2:  // 双通道模式
                // 通道选择：根据sel[1]和sel[5]选择通道
                SendADData(0, 0, 0x000B , sel[1] ? 0 : 1);  // FPGA0通道选择
                SendADData(1, 0, 0x000B , sel[5] ? 0 : 1);  // FPGA1通道选择
                // 时钟模式：设置为模式0
                SendADData(0, 0, 0x000A , 0);  // FPGA0时钟模式
                SendADData(1, 0, 0x000A , 0);  // FPGA1时钟模式
                break;
            case 4:  // 四通道模式
                // 通道选择：设置为3（四通道模式）
                SendADData(0, 0, 0x000B , 3);  // FPGA0通道选择
                SendADData(1, 0, 0x000B , 3);  // FPGA1通道选择
                // 时钟模式：设置为模式2
                SendADData(0, 0, 0x000A , 2);  // FPGA0时钟模式
                SendADData(1, 0, 0x000A , 2);  // FPGA1时钟模式
                break;
            case 8:  // 八通道模式
            default:  // 默认使用八通道模式
                // 通道选择：设置为4（八通道模式）
                SendADData(0, 0, 0x000B , 4);  // FPGA0通道选择
                SendADData(1, 0, 0x000B , 4);  // FPGA1通道选择
                // 时钟模式：设置为模式3
                SendADData(0, 0, 0x000A , 3);  // FPGA0时钟模式
                SendADData(1, 0, 0x000A , 3);  // FPGA1时钟模式
                break;
        }
        if(fpgaCommand != null){  // 检查FPGA命令管理器是否有效
            fpgaCommand.adSync();  // 同步ADC配置
        }
    }

    /**
     * 发送ADC寄存器配置数据
     * 
     * <p>重写父类方法，添加地址高位标志位。
     * EV12AQ60x芯片的寄存器地址需要或上0x8000标志位。
     * 
     * <p><b>地址处理：</b>
     * <ul>
     *   <li>原始地址 | 0x8000：添加高位标志位</li>
     *   <li>标志位用于区分ADC寄存器访问类型</li>
     * </ul>
     * 
     * @param fpgaIdx FPGA设备索引
     * @param adIdx ADC芯片索引
     * @param addr ADC寄存器地址
     * @param val 寄存器值
     */
    @Override
    protected void SendADData(int fpgaIdx, int adIdx, int addr, int val) {
        super.SendADData(fpgaIdx, adIdx, addr| 0x8000, val);  // 调用父类方法，添加地址高位标志
    }


    /**
     * 获取实际采样通道数
     * 
     * <p>根据通道选择数组计算实际需要的采样通道数，并自动调整通道选择。
     * EV12AQ60x芯片的通道模式由开启的通道组合决定。
     * 
     * <p><b>通道模式判断逻辑：</b>
     * <ol>
     *   <li>如果CH3、CH4、CH7、CH8任意通道开启 → 8通道模式（强制所有通道开启）</li>
     *   <li>如果CH1和CH2同时开启，或CH5和CH6同时开启 → 4通道模式</li>
     *   <li>其他情况 → 2通道模式（自动配对）</li>
     * </ol>
     * 
     * <p><b>通道索引说明：</b>
     * <ul>
     *   <li>sel[0]: CH1</li>
     *   <li>sel[1]: CH2</li>
     *   <li>sel[2]: CH3</li>
     *   <li>sel[3]: CH4</li>
     *   <li>sel[4]: CH5</li>
     *   <li>sel[5]: CH6</li>
     *   <li>sel[6]: CH7</li>
     *   <li>sel[7]: CH8</li>
     * </ul>
     * 
     * @param sel 通道选择数组，sel[i]=true表示通道i启用
     * @return 实际采样通道数（2/4/8）
     */
    @Override
    public int getSampleChannel(boolean[] sel) {
        int nums = 0;  // 采样通道数
        // 通道索引：0,1,2,3,4,5,6,7 对应 CH1-CH8

        if (sel[2] || sel[3] || sel[6] || sel[7]) {  // 检查CH3、CH4、CH7、CH8是否开启
            // 2, 3, 6, 7 任意通道开启，进入8通道模式
            Arrays.fill(sel, true);  // 强制所有通道开启
            nums = 8;  // 设置为8通道模式

        } else if ((sel[0] && sel[1]) || (sel[4] && sel[5])) {  // 检查CH1&CH2或CH5&CH6是否同时开启
            // {0, 1}, {4, 5} 两组有一组开启，进入4通道模式，一个ADC管理两个通道
            sel[0] = sel[1] = sel[4] = sel[5] = true;  // 强制这四个通道开启
            nums = 4;  // 设置为4通道模式
        } else {  // 其他情况，进入2通道模式
            if (sel[0]) {  // CH1开启
                if (!sel[4] && !sel[5]) {  // CH5和CH6都未开启
                    sel[4] = true;  // 自动开启CH5配对
                }
            } else if (sel[1]) {  // CH2开启
                if (!sel[4] && !sel[5]) {  // CH5和CH6都未开启
                    sel[5] = true;  // 自动开启CH6配对
                }
            } else if (sel[4]) {  // CH5开启
                sel[0] = true;  // 自动开启CH1配对
            } else if (sel[5]) {  // CH6开启
                sel[1] = true;  // 自动开启CH2配对
            }
            nums = 2;  // 设置为2通道模式
        }
        return nums;  // 返回采样通道数
    }

    /**
     * 获取ADC最大输入时钟频率
     * 
     * <p>EV12AQ60x芯片支持最高6GHz的采样时钟。
     * 
     * @return 最大ADC输入时钟频率，单位MHz（返回6000表示6GHz）
     */
    @Override
    public int getMaxAdInClk() {
        return 6000;  // 返回6GHz
    }

    /**
     * 获取ADC最大物理通道数
     * 
     * <p>EV12AQ60x是4通道ADC芯片，每个芯片有4个物理通道（A/B/C/D）。
     * 
     * @return 单个ADC芯片的最大物理通道数（返回4）
     */
    @Override
    public int getMaxChNums() {
        return 4;  // 返回4通道
    }


}
