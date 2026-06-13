package com.micsig.tbook.scope.Calibrate;  // 定义包名：示波器校准功能模块

/**
 * ADC操作接口 - 模数转换器硬件抽象层
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准功能模块）</li>
 *   <li>架构层级：硬件抽象层 - ADC接口定义</li>
 *   <li>设计模式：接口模式（Interface Pattern）</li>
 *   <li>职责类型：ADC初始化、偏移/增益设置、通道配置</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义ADC初始化接口</li>
 *   <li>定义ADC偏移和增益校准接口</li>
 *   <li>定义ADC通道选择接口</li>
 *   <li>定义ADC硬件参数查询接口</li>
 * </ul>
 * 
 * <p><b>接口方法分类：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   IADC ─ ADC操作接口方法分类                                              │
 * │                                                                          │
 * │   初始化方法：                                                            │
 * │   └── Init() ─ 初始化ADC硬件                                             │
 * │                                                                          │
 * │   校准方法：                                                              │
 * │   ├── setOffset() ─ 设置ADC偏移校准值                                    │
 * │   ├── setGain()   ─ 设置ADC增益校准值                                    │
 * │   └── setCalibrate() ─ 应用校准参数到硬件                                │
 * │                                                                          │
 * │   通道配置方法：                                                          │
 * │   ├── setChannel(cnt, sel) ─ 设置采样通道（指定数量）                     │
 * │   └── setChannel(sel)     ─ 设置采样通道                                 │
 * │                                                                          │
 * │   参数查询方法：                                                          │
 * │   ├── getSampleChannel()  ─ 获取采样通道数量                             │
 * │   ├── getMaxAdInClk()     ─ 获取最大ADC输入时钟                          │
 * │   ├── getMaxChNums()      ─ 获取最大通道数                               │
 * │   └── isChChangeReset()   ─ 检查通道变化是否需要复位                     │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>实现类：</b>
 * <ul>
 *   <li>ADC类：具体实现ADC硬件操作</li>
 *   <li>不同型号的示波器可能有不同的ADC实现</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>校准模块：设置ADC偏移和增益校准参数</li>
 *   <li>采样模块：配置采样通道</li>
 *   <li>初始化模块：初始化ADC硬件</li>
 * </ul>
 * 
 * <p><b>参数说明：</b>
 * <ul>
 *   <li>fpgaIdx：FPGA索引（多FPGA系统中标识哪个FPGA）</li>
 *   <li>adIdx：ADC索引（一个FPGA可能控制多个ADC）</li>
 *   <li>i0_q1：I/Q通道标志（0=I通道，1=Q通道，用于正交采样）</li>
 *   <li>vol：校准值（偏移或增益值）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see ADC ADC实现类
 */
public interface IADC {
    
    /**
     * 初始化ADC硬件
     * 
     * <p>执行ADC硬件的初始化操作，包括：
     * <ul>
     *   <li>复位ADC</li>
     *   <li>配置ADC工作模式</li>
     *   <li>加载校准参数</li>
     * </ul>
     */
    void Init();
    
//    void setOffset(int adIdx,int i0_q1, int vol);
    
    /**
     * 设置ADC偏移校准值
     * 
     * <p>设置指定FPGA和ADC的偏移校准参数。
     * 偏移校准用于消除ADC的零点偏移误差。
     * 
     * @param fpgaIdx FPGA索引（多FPGA系统中标识哪个FPGA，0-based）
     * @param adIdx ADC索引（一个FPGA可能控制多个ADC，0-based）
     * @param i0_q1 I/Q通道标志（0=I通道，1=Q通道）
     * @param vol 偏移校准值
     */
    void setOffset(int fpgaIdx,int adIdx,int i0_q1, int vol);
    
//    void setGain(int adIdx,int i0_q1, int vol);
    
    /**
     * 设置ADC增益校准值
     * 
     * <p>设置指定FPGA和ADC的增益校准参数。
     * 增益校准用于消除ADC的增益误差。
     * 
     * @param fpgaIdx FPGA索引（多FPGA系统中标识哪个FPGA，0-based）
     * @param adIdx ADC索引（一个FPGA可能控制多个ADC，0-based）
     * @param i0_q1 I/Q通道标志（0=I通道，1=Q通道）
     * @param vol 增益校准值
     */
    void setGain(int fpgaIdx,int adIdx,int i0_q1, int vol);
    
    /**
     * 设置采样通道（指定数量）
     * 
     * <p>配置ADC采样的通道数量和通道选择。
     * 
     * @param cnt 采样通道数量
     * @param sel 通道选择数组，sel[i]=true表示通道i被选中
     */
    void setChannel(int cnt,boolean [] sel);
    
    /**
     * 设置采样通道
     * 
     * <p>配置ADC采样的通道选择。
     * 
     * @param sel 通道选择数组，sel[i]=true表示通道i被选中
     */
    void setChannel(boolean [] sel);

    /**
     * 应用校准参数到硬件
     * 
     * <p>将设置好的偏移和增益校准参数写入ADC硬件寄存器。
     * 通常在所有校准参数设置完成后调用。
     */
    void setCalibrate();

    /**
     * 获取采样通道数量
     * 
     * <p>根据通道选择数组计算实际采样的通道数量。
     * 
     * @param sel 通道选择数组
     * @return 采样通道数量
     */
    int getSampleChannel(boolean [] sel);

    /**
     * 获取最大ADC输入时钟
     * 
     * <p>返回ADC的最大输入时钟频率，单位MHz。
     * 该参数决定了ADC的最大采样率。
     * 
     * @return 最大ADC输入时钟频率（MHz）
     */
    int getMaxAdInClk();

    /**
     * 获取最大通道数
     * 
     * <p>返回ADC支持的最大通道数量。
     * 
     * @return 最大通道数
     */
    int getMaxChNums();

    /**
     * 检查通道变化是否需要复位
     * 
     * <p>某些ADC在通道配置变化时需要复位才能正常工作。
     * 
     * @return true表示需要复位，false表示不需要复位
     */
    boolean isChChangeReset();

}
