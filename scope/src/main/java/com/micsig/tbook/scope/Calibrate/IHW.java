package com.micsig.tbook.scope.Calibrate;  // 定义包名：示波器校准功能模块

import java.util.List;  // 导入List类：列表集合，用于返回校准项目列表

/**
 * 硬件操作接口 - 通道硬件控制抽象层
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准功能模块）</li>
 *   <li>架构层级：硬件抽象层 - 硬件控制接口定义</li>
 *   <li>设计模式：接口模式（Interface Pattern）</li>
 *   <li>职责类型：通道硬件控制、校准状态管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义通道PGA（可编程增益放大器）控制接口</li>
 *   <li>定义AD增益控制接口</li>
 *   <li>定义通道电源控制接口</li>
 *   <li>定义校准状态管理接口</li>
 *   <li>定义校准数据存储接口</li>
 * </ul>
 * 
 * <p><b>接口方法分类：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   IHW ─ 硬件操作接口方法分类                                              │
 * │                                                                          │
 * │   PGA控制方法：                                                           │
 * │   ├── setChPga()       ─ 设置通道PGA配置                                 │
 * │   └── setChPgaGain()   ─ 设置通道PGA增益值                               │
 * │                                                                          │
 * │   AD增益控制方法：                                                        │
 * │   └── setAdGain()      ─ 设置AD增益                                      │
 * │                                                                          │
 * │   通道控制方法：                                                          │
 * │   ├── ChPowerEnable()     ─ 通道电源使能控制                             │
 * │   └── changeChVolScale()  ─ 切换通道电压档位                             │
 * │                                                                          │
 * │   校准状态管理方法：                                                      │
 * │   ├── clearCalibrationState()  ─ 清除校准状态                           │
 * │   ├── saveCalibrationState()   ─ 保存校准状态                           │
 * │   ├── setCalibrationState()    ─ 设置指定项校准状态                      │
 * │   ├── isCalibrationState()     ─ 查询指定项校准状态                      │
 * │   └── loadCalibrationState()   ─ 加载校准状态                           │
 * │                                                                          │
 * │   校准数据存储方法：                                                      │
 * │   ├── loadCalibration()        ─ 加载校准数据                           │
 * │   ├── saveCalibration()        ─ 保存校准数据                           │
 * │   ├── saveFactoryCalibration() ─ 保存出厂校准数据                        │
 * │   ├── saveUserCalibration()    ─ 保存用户校准数据                        │
 * │   ├── backUpCabteRegister()    ─ 备份校准寄存器                         │
 * │   └── restoreCabteRegister()   ─ 恢复校准寄存器                         │
 * │                                                                          │
 * │   查询方法：                                                              │
 * │   ├── getRatioDangCnt()      ─ 获取比例档位数量                         │
 * │   ├── isTopCalibration()     ─ 检查是否为顶层校准                       │
 * │   ├── getCalibrationItems()  ─ 获取校准项目列表                         │
 * │   └── getCalibrationItemState() ─ 获取校准项目状态                      │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>实现类：</b>
 * <ul>
 *   <li>ChannelHardw类：具体实现通道硬件操作</li>
 *   <li>不同型号的示波器可能有不同的硬件实现</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>校准模块：管理校准状态和数据存储</li>
 *   <li>通道控制模块：控制PGA、AD增益、电压档位</li>
 *   <li>电源管理模块：控制通道电源</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see ChannelHardw 通道硬件控制实现类
 */
public interface IHW {
    
    // ==================== PGA控制方法 ====================
    
    /**
     * 设置通道PGA配置
     * 
     * <p>根据当前通道配置设置PGA（可编程增益放大器）的工作模式。
     * PGA用于调整输入信号的幅度，以匹配ADC的输入范围。
     */
    public void setChPga();
    
    /**
     * 设置通道PGA增益值
     * 
     * <p>为每个通道设置PGA的增益值。
     * 增益值用于调整信号的放大倍数。
     * 
     * @param val 增益值数组，val[i]表示通道i的PGA增益值
     */
    public void setChPgaGain(int []val);

    // ==================== AD增益控制方法 ====================
    
    /**
     * 设置AD增益
     * 
     * <p>设置指定FPGA的AD（模数转换器）增益。
     * AD增益用于调整ADC的输入范围。
     * 
     * @param fpgaIdx FPGA索引（多FPGA系统中标识哪个FPGA，0-based）
     */
    public void setAdGain(int fpgaIdx);

    // ==================== 通道控制方法 ====================
    
    /**
     * 通道电源使能控制
     * 
     * <p>控制通道模拟电路的电源开关。
     * 关闭电源可以降低功耗，但需要重新初始化才能使用。
     * 
     * @param bEnable true表示使能电源，false表示关闭电源
     */
    public void ChPowerEnable( boolean bEnable);
    
    /**
     * 切换通道电压档位
     * 
     * <p>根据当前通道的电压档位设置，控制硬件继电器切换。
     * 不同的电压档位对应不同的衰减比例。
     */
    public  void changeChVolScale();



    // ==================== 校准状态管理方法 ====================
    
    /**
     * 清除校准状态
     * 
     * <p>将所有校准项目的状态清除为未校准状态。
     * 通常在开始新的校准流程前调用。
     */
    public void clearCalibrationState();
    
    /**
     * 保存校准状态
     * 
     * <p>将当前校准状态保存到持久化存储。
     * 通常在校准完成后调用。
     */
    public void saveCalibrationState();
    
    /**
     * 设置指定项的校准状态
     * 
     * <p>设置指定校准项目的完成状态。
     * 
     * @param idx 校准项目索引
     * @param bState true表示已校准，false表示未校准
     */
    public void setCalibrationState(int idx,boolean bState);
    
    /**
     * 查询指定项的校准状态
     * 
     * <p>检查指定校准项目是否已完成校准。
     * 
     * @param idx 校准项目索引
     * @return true表示已校准，false表示未校准
     */
    public boolean isCalibrationState(int idx);
    
    /**
     * 加载校准状态
     * 
     * <p>从持久化存储加载校准状态到内存。
     * 通常在系统启动时调用。
     */
    public void loadCalibrationState();
    
    /**
     * 加载校准数据
     * 
     * <p>从持久化存储加载校准系数数据。
     * 
     * @return 加载结果，0表示成功，非0表示失败
     */
    public int loadCalibration();
    
    /**
     * 保存校准数据
     * 
     * <p>将当前校准系数保存到持久化存储。
     * 
     * @return true表示保存成功，false表示保存失败
     */
    public boolean saveCalibration();
    
    /**
     * 保存出厂校准数据
     * 
     * <p>将当前校准系数保存为出厂校准数据。
     * 出厂校准数据通常存储在受保护区域，用户无法修改。
     * 
     * @return true表示保存成功，false表示保存失败
     */
    public boolean saveFactoryCalibration();
    
    /**
     * 保存用户校准数据
     * 
     * <p>将当前校准系数保存为用户校准数据。
     * 用户校准数据可以被用户修改和覆盖。
     * 
     * @return true表示保存成功，false表示保存失败
     */
    public boolean saveUserCalibration();
    
    /**
     * 获取比例档位数量
     * 
     * <p>返回支持的电压档位比例数量。
     * 
     * @return 比例档位数量
     */
    public int getRatioDangCnt();
    
    /**
     * 备份校准寄存器
     * 
     * <p>将当前校准系数备份到内存，用于校准失败时恢复。
     * 通常在开始校准前调用。
     */
    public void backUpCabteRegister();
    
    /**
     * 恢复校准寄存器
     * 
     * <p>从备份恢复校准系数，用于校准失败时回滚。
     * 通常在校准失败或被中断时调用。
     */
    public void restoreCabteRegister();

    /**
     * 检查是否为顶层校准
     * 
     * <p>检查当前是否处于顶层（工厂）校准模式。
     * 顶层校准通常在生产线上进行，权限更高。
     * 
     * @return true表示顶层校准模式，false表示用户校准模式
     */
    public boolean isTopCalibration();

    /**
     * 获取校准项目列表
     * 
     * <p>返回所有可用的校准项目名称列表。
     * 
     * @return 校准项目名称列表
     */
    public List<String> getCalibrationItems();
    
    /**
     * 获取校准项目状态
     * 
     * <p>获取指定校准项目的详细状态信息。
     * 
     * @param idx 校准项目索引
     * @param sb StringBuilder用于接收状态描述字符串
     * @return true表示已校准，false表示未校准
     */
    public boolean getCalibrationItemState(int idx,StringBuilder sb);

}
