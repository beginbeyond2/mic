package com.micsig.tbook.scope.fpga;                                                     // 包声明：示波器FPGA寄存器管理模块

import com.micsig.base.Logger;                                                           // 导入：日志类，用于调试输出

import java.nio.ByteBuffer;                                                              // 导入：字节缓冲区类，用于寄存器数据存储
import java.nio.ByteOrder;                                                               // 导入：字节顺序类，用于设置小端序
import java.nio.IntBuffer;                                                               // 导入：整数缓冲区类，用于32位数据操作
import java.nio.ShortBuffer;                                                             // 导入：短整数缓冲区类，用于16位数据操作

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║        FPGAReg - 示波器FPGA寄存器基类                                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   FPGA模块的寄存器基类，位于fpga包下，                                          ║
 * ║   是所有FPGA寄存器类的父类，提供寄存器地址定义和基础操作功能。                  ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 定义所有FPGA寄存器地址常量                                              ║
 * ║   2. 提供寄存器数据存储和操作方法                                            ║
 * ║   3. 提供寄存器命令构建和解析功能                                            ║
 * ║   4. 支持位级别的数据读写操作                                                ║
 * ║                                                                              ║
 * ║ 【架构位置】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        FPGA寄存器架构                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   ┌─────────────────┐                                                │ ║
 * ║   │   │    FPGAReg      │  ← 基类（本类）                                 │ ║
 * ║   │   │   (寄存器基类)   │                                                │ ║
 * ║   │   └─────────────────┘                                                │ ║
 * ║   │          │                                                            │ ║
 * ║   │          │ 继承                                                       │ ║
 * ║   │          ▼                                                            │ ║
 * ║   │   ┌─────────────────┐                                                │ ║
 * ║   │   │  FPGAReg_xxx    │  ← 子类（具体寄存器）                           │ ║
 * ║   │   │  (具体寄存器类)  │                                                │ ║
 * ║   │   └─────────────────┘                                                │ ║
 * ║   │          │                                                            │ ║
 * ║   │          ▼                                                            │ ║
 * ║   │   ┌─────────────────┐                                                │ ║
 * ║   │   │  FPGACommand    │  ← 命令管理类                                   │ ║
 * ║   │   │   (命令管理)      │                                                │ ║
 * ║   │   └─────────────────┘                                                │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【寄存器地址分类】                                                           ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        寄存器地址分类                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【采样控制寄存器】 0x00-0x18                                         │ ║
 * ║   │   - FPGA_SAMPLE_MODE: 采样模式                                       │ ║
 * ║   │   - FPGA_SEGMENT_FRAME: 分段存储帧空间                               │ ║
 * ║   │   - FPGA_ZUN_DEPTH: 存储深度                                         │ ║
 * ║   │   - FPGA_PRE_SAMP: 预采样深度                                        │ ║
 * ║   │   - FPGA_CH_Y_PLACE: 通道Y轴位置                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【触发控制寄存器】 0x20-0x2B                                         │ ║
 * ║   │   - FPGA_TRIG_MODE: 触发模式                                         │ ║
 * ║   │   - FPGA_TRIG_LEVEL: 触发电平                                        │ ║
 * ║   │   - FPGA_TRIG_COUPLE: 触发耦合                                       │ ║
 * ║   │   - FPGA_TRIG_AUTO_TRIG_TIME: 自动触发时间                           │ ║
 * ║   │   - FPGA_TRIG_RESTRAIN_TIME: 抑制时间                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   【串行解码寄存器】 0x30-0x3F                                         │ ║
 * ║   │   - FPGA_BUS_TYPE: 总线类型                                          │ ║
 * ║   │   - FPGA_BUS_LEVEL: 总线电平                                         │ ║
 * ║   │   - FPGA_BUS_PRIMARY: 主总线配置                                     │ ║
 * ║   │   - FPGA_BUS_SECONDARY: 次总线配置                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【显示控制寄存器】 0x40-0x5F                                         │ ║
 * ║   │   - FPGA_DIS_MODE: 显示模式                                          │ ║
 * ║   │   - FPGA_DISP_RESOLUTION: 显示分辨率                                 │ ║
 * ║   │   - FPGA_DISP_WAVE: 波形显示                                         │ ║
 * ║   │   - FPGA_DISP_SL: 缩略视图                                           │ ║
 * ║   │                                                                      │ ║
 * ║   │   【外设控制寄存器】 0x60-0x77                                         │ ║
 * ║   │   - FPGA_CH_OFFSET_DA12: 通道偏移DA                                  │ ║
 * ║   │   - FPGA_CH_RESISTANCE: 通道阻抗                                     │ ║
 * ║   │   - FPGA_TRIG_OFFSET: 触发偏移                                       │ ║
 * ║   │                                                                      │ ║
 * ║   │   【状态读取寄存器】 0x80-0x8F                                         │ ║
 * ║   │   - FPGA_STATUS_ID: FPGA状态ID                                       │ ║
 * ║   │   - FPGA_STATUS_STATE: FPGA状态                                      │ ║
 * ║   │   - FPGA_PROBE: 探头状态                                             │ ║
 * ║   │   - FPGA_TEMPERATURE: 温度                                           │ ║
 * ║   │                                                                      │ ║
 * ║   │   【命令控制寄存器】 0xA0-0xAD                                         │ ║
 * ║   │   - FPGA_COMMAND: FPGA命令                                           │ ║
 * ║   │   - FPGA_FORCE_TRIGGER: 强制触发                                     │ ║
 * ║   │   - FPGA_SPI_EXT: SPI扩展                                            │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【寄存器数据格式】                                                           ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        寄存器命令格式                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【发送命令格式】                                                     │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  字0: 命令头                                                  │   │ ║
 * ║   │   │    - 位0-15: 命令标识（0x8180表示发送，0x2421表示接收）        │   │ ║
 * ║   │   │    - 位16-31: 寄存器地址                                      │   │ ║
 * ║   │   ├─────────────────────────────────────────────────────────────┤   │ ║
 * ║   │   │  字1: 数据长度                                                │   │ ║
 * ║   │   │    - 值为数据长度/4（以32位字为单位）                          │   │ ║
 * ║   │   ├─────────────────────────────────────────────────────────────┤   │ ║
 * ║   │   │  字2-N: 寄存器数据                                            │   │ ║
 * ║   │   │    - 根据寄存器类型存储相应数据                                │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【接收命令格式】                                                     │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  字0: 响应头                                                  │   │ ║
 * ║   │   │    - 值为0x181118ff表示有效响应                               │   │ ║
 * ║   │   ├─────────────────────────────────────────────────────────────┤   │ ║
 * ║   │   │  字1: 响应确认                                                │   │ ║
 * ║   │   │    - 与字0相同表示响应有效                                    │   │ ║
 * ║   │   ├─────────────────────────────────────────────────────────────┤   │ ║
 * ║   │   │  字2-N: 寄存器数据                                            │   │ ║
 * ║   │   │    - 从FPGA读取的状态数据                                     │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 基类模式：作为所有寄存器类的父类，提供通用功能                           ║
 * ║   - 模板方法模式：onCommand()方法可被子类重写                               ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - Logger: 日志类，用于调试输出                                          ║
 * ║   - ByteBuffer: 字节缓冲区，用于数据存储                                  ║
 * ║   - FPGACommand: FPGA命令管理类，负责发送寄存器命令                        ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 寄存器操作在FPGACommand中同步执行                                     ║
 * ║   - 每个寄存器实例独立，不共享数据                                        ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ║ 【创建日期】 2018/3/13                                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器FPGA寄存器基类
 * 提供所有FPGA寄存器的地址定义和基础操作功能
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>定义所有FPGA寄存器地址常量</li>
 *   <li>提供寄存器数据存储和操作方法</li>
 *   <li>提供寄存器命令构建和解析功能</li>
 *   <li>支持位级别的数据读写操作</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 创建寄存器实例
 * FPGAReg reg = new FPGAReg(FPGAReg.FPGA_SAMPLE_MODE, 4);
 *
 * // 设置寄存器值
 * reg.setVal(0, 1);  // 设置字0的值为1
 * reg.setVal(0, 0, 8, 100);  // 设置字0的位0-7为100
 *
 * // 获取寄存器值
 * int val = reg.getVal(0);  // 获取字0的值
 *
 * // 获取命令数据
 * ByteBuffer cmd = reg.getCommand();  // 获取完整的命令数据
 *
 * // 发送命令到FPGA
 * FPGACommand.getInstance().sendCommand(reg);
 * </pre>
 *
 * @see FPGACommand
 * @see ByteBuffer
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/13
 */
public class FPGAReg {                                                                    // 类声明：FPGA寄存器基类

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于调试输出时标识此类
     */
    private static final String TAG = "FPGAReg";                                          // 静态常量：日志标签

    // ═══════════════════════════════════════════════════════════════════════════════
    // 采样控制寄存器地址常量 (0x00-0x18)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 采样模式寄存器地址
     * 用于配置示波器的采样模式参数
     * 地址：0x00
     */
    public static final int FPGA_SAMPLE_MODE = 0x00;                                      // 静态常量：采样模式寄存器地址

    /**
     * 分段存储帧空间寄存器地址
     * 用于配置分段采样时的帧空间大小
     * 地址：0x01
     */
    public static final int FPGA_SEGMENT_FRAME = 0x01;                                    // 静态常量：分段存储帧空间寄存器地址

    /**
     * 平均或包络次数寄存器地址
     * 用于设置平均采样或包络采样的次数
     * 地址：0x02
     */
    public static final int FPGA_PJBL_TIMES = 0x02;                                       // 静态常量：平均或包络次数寄存器地址

    /**
     * 存储深度寄存器地址
     * 用于设置示波器的存储深度（采样点数量）
     * 地址：0x03
     */
    public static final int FPGA_ZUN_DEPTH = 0x03;                                        // 静态常量：存储深度寄存器地址

    /**
     * 预采样深度寄存器地址
     * 用于设置触发点前的预采样数据量
     * 地址：0x04
     */
    public static final int FPGA_PRE_SAMP = 0x04;                                         // 静态常量：预采样深度寄存器地址

    /**
     * 抽样数寄存器地址（通道1-2）
     * 用于设置通道1和通道2的抽样数
     * 地址：0x07
     */
    public static final int FPGA_NUM_CY12 = 0x07;                                         // 静态常量：抽样数寄存器地址（通道1-2）

    /**
     * 抽样数寄存器地址（通道3-4）
     * 用于设置通道3和通道4的抽样数
     * 地址：0x08
     */
    public static final int FPGA_NUM_CY34 = 0x08;                                         // 静态常量：抽样数寄存器地址（通道3-4）

    /**
     * 抽样补充寄存器地址
     * 用于设置抽样补充参数
     * 地址：0x09
     */
    public static final int FPGA_CY_BUCONG = 0x09;                                        // 静态常量：抽样补充寄存器地址

    /**
     * 滚动行寄存器地址
     * 用于设置滚动模式下每次刷新的列数
     * 地址：0x0A
     */
    public static final int FPGA_SCROLL_ROW = 0x0A;                                       // 静态常量：滚动行寄存器地址

    /**
     * 串行解码采样深度寄存器地址
     * 用于设置串行解码时的采样深度
     * 地址：0x0B
     */
    public static final int FPGA_SERIAL_DEC_DEPTH = 0x0B;                                 // 静态常量：串行解码采样深度寄存器地址

    /**
     * 通道Y轴位置寄存器地址
     * 用于设置通道波形在屏幕上的垂直位置
     * 地址：0x0C
     */
    public static final int FPGA_CH_Y_PLACE = 0x0C;                                       // 静态常量：通道Y轴位置寄存器地址

    /**
     * 慢时基参数设置寄存器地址
     * 用于设置YT模式慢时基参数
     * 地址：0x10
     */
    public static final int FPGA_SLOW_SCALE_SET = 0x10;                                   // 静态常量：慢时基参数设置寄存器地址

    /**
     * AD零点寄存器地址
     * 用于设置AD转换器的零点校准值
     * 地址：0x15
     */
    public static final int FPGA_AD_ZERO = 0x15;                                          // 静态常量：AD零点寄存器地址

    /**
     * 通道Y轴位置补充寄存器地址
     * 用于设置通道Y轴位置的补充参数
     * 地址：0x16
     */
    public static final int FPGA_CH_Y_PLACE_BC = 0x16;                                    // 静态常量：通道Y轴位置补充寄存器地址

    /**
     * 通道Y轴位置补充寄存器地址1
     * 与FPGA_CH_Y_PLACE_BC相同
     */
    public static final int FPGA_CH_Y_PLACE_BC1 = FPGA_CH_Y_PLACE_BC;                     // 静态常量：通道Y轴位置补充寄存器地址1

    /**
     * 通道Y轴位置补充寄存器地址2
     * 用于设置通道Y轴位置的补充参数2
     * 地址：0x17
     */
    public static final int FPGA_CH_Y_PLACE_BC2 = 0x17;                                   // 静态常量：通道Y轴位置补充寄存器地址2

    /**
     * AD增益补偿寄存器地址
     * 用于设置AD转换器的增益补偿值
     * 地址：0x18
     */
    public static final int FPGA_GAIN_BC_AD = 0x18;                                       // 静态常量：AD增益补偿寄存器地址

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发控制寄存器地址常量 (0x20-0x2B)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发模式寄存器地址
     * 用于设置触发类型、触发源、触发极性等参数
     * 地址：0x20
     */
    public static final int FPGA_TRIG_MODE = 0x20;                                        // 静态常量：触发模式寄存器地址

    /**
     * 脉冲触发时间控制值寄存器地址（低位）
     * 用于设置脉冲触发的时间控制值（小值）或视频触发的行号
     * 地址：0x22
     */
    public static final int FPGA_TRIG_VAL_L = 0x22;                                       // 静态常量：脉冲触发时间控制值寄存器地址（低位）

    /**
     * 脉冲触发时间控制值寄存器地址（高位）
     * 用于设置脉冲触发的时间控制值（大值）或判断奇偶场
     * 地址：0x23
     */
    public static final int FPGA_TRIG_VAL_H = 0x23;                                       // 静态常量：脉冲触发时间控制值寄存器地址（高位）

    /**
     * 自动触发时间控制寄存器地址
     * 用于设置自动触发等待时间，单位为1ms
     * 地址：0x24
     */
    public static final int FPGA_TRIG_AUTO_TRIG_TIME = 0x24;                              // 静态常量：自动触发时间控制寄存器地址

    /**
     * 触发抑制时间寄存器地址
     * 用于设置触发后的抑制时间，防止重复触发
     * 地址：0x25
     */
    public static final int FPGA_TRIG_RESTRAIN_TIME = 0x25;                               // 静态常量：触发抑制时间寄存器地址

    /**
     * 触发耦合寄存器地址
     * 用于设置触发耦合方式（直流、交流、高频抑制等）
     * 地址：0x26
     */
    public static final int FPGA_TRIG_COUPLE = 0x26;                                      // 静态常量：触发耦合寄存器地址

    /**
     * 触发电平寄存器地址
     * 用于设置触发电平值
     * 地址：0x27
     */
    public static final int FPGA_TRIG_LEVEL = 0x27;                                       // 静态常量：触发电平寄存器地址

    /**
     * 主触发电平寄存器地址1
     * 用于设置触发源1的触发电平
     * 地址：0x27（与FPGA_TRIG_LEVEL相同）
     */
    public static final int FPGA_TRIG_LEVEL1 = 0x27;                                      // 静态常量：主触发电平寄存器地址1

    /**
     * 主触发电平寄存器地址2
     * 用于设置触发源2的触发电平
     * 地址：0x28
     */
    public static final int FPGA_TRIG_LEVEL2 = 0x28;                                      // 静态常量：主触发电平寄存器地址2

    /**
     * 主触发电平寄存器地址3
     * 用于设置触发源3的触发电平
     * 地址：0x29
     */
    public static final int FPGA_TRIG_LEVEL3 = 0x29;                                      // 静态常量：主触发电平寄存器地址3

    /**
     * 主触发电平寄存器地址4
     * 用于设置触发源4的触发电平
     * 地址：0x2A
     */
    public static final int FPGA_TRIG_LEVEL4 = 0x2A;                                      // 静态常量：主触发电平寄存器地址4

    /**
     * 次触发电平寄存器地址
     * 用于设置第二触发源的触发电平
     * 地址：0x2B
     */
    public static final int FPGA_TRIG_SNDLVL = 0x2B;                                      // 静态常量：次触发电平寄存器地址

    // ═══════════════════════════════════════════════════════════════════════════════
    // 串行解码寄存器地址常量 (0x30-0x3F, 0xC0-0xC8)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 总线类型寄存器地址
     * 用于设置串行解码的总线类型（UART、CAN、LIN等）
     * 地址：0x30
     */
    public static final int FPGA_BUS_TYPE = 0x30;                                         // 静态常量：总线类型寄存器地址

    /**
     * 总线电平寄存器地址
     * 用于设置串行解码的总线电平阈值
     * 地址：0x31
     */
    public static final int FPGA_BUS_LEVEL = 0x31;                                        // 静态常量：总线电平寄存器地址

    /**
     * 总线1地址寄存器地址
     * 用于设置总线1的地址参数（地址范围0x32-0x36）
     * 地址：0x32
     */
    public static final int FPGA_BUS1_ADDR = 0x32;                                        // 静态常量：总线1地址寄存器地址

    /**
     * 总线2地址寄存器地址
     * 用于设置总线2的地址参数（地址范围0x37-0x3B）
     * 地址：0x37
     */
    public static final int FPGA_BUS2_ADDR = 0x37;                                        // 静态常量：总线2地址寄存器地址

    /**
     * 主总线配置寄存器地址
     * 用于设置主总线的配置参数
     * 地址：0x3C
     */
    public static final int FPGA_BUS_PRIMARY = 0x3C;                                      // 静态常量：主总线配置寄存器地址

    /**
     * 次总线配置寄存器地址
     * 用于设置次总线的配置参数
     * 地址：0x3D
     */
    public static final int FPGA_BUS_SECONDARY = 0x3D;                                    // 静态常量：次总线配置寄存器地址

    /**
     * 主总线扩展配置寄存器地址
     * 用于设置主总线的扩展配置参数
     * 地址：0x3E
     */
    public static final int FPGA_BUS_PRIMARY_EXT = 0x3E;                                  // 静态常量：主总线扩展配置寄存器地址

    /**
     * 次总线扩展配置寄存器地址
     * 用于设置次总线的扩展配置参数
     * 地址：0x3F
     */
    public static final int FPGA_BUS_SECONDARY_EXT = 0x3F;                                // 静态常量：次总线扩展配置寄存器地址

    /**
     * 总线1 CAN扩展寄存器地址
     * 用于设置总线1的CAN FD协议扩展参数
     * 地址：0xC0
     */
    public static final int FPGA_BUS1_CAN_EXT = 0xC0;                                     // 静态常量：总线1 CAN扩展寄存器地址

    /**
     * 总线2 CAN扩展寄存器地址
     * 用于设置总线2的CAN FD协议扩展参数
     * 地址：0xC8
     */
    public static final int FPGA_BUS2_CAN_EXT = 0xC8;                                     // 静态常量：总线2 CAN扩展寄存器地址

    // ═══════════════════════════════════════════════════════════════════════════════
    // 显示控制寄存器地址常量 (0x40-0x5F)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 显示模式寄存器地址
     * 用于设置示波器的显示模式
     * 地址：0x40
     */
    public static final int FPGA_DIS_MODE = 0x40;                                         // 静态常量：显示模式寄存器地址

    /**
     * 主视图插值参数1寄存器地址
     * 用于设置主视图的插值参数1
     * 地址：0x41
     */
    public static final int FPGA_DISP_CHA1 = 0x41;                                        // 静态常量：主视图插值参数1寄存器地址

    /**
     * 主视图插值参数2寄存器地址
     * 用于设置主视图的插值参数2
     * 地址：0x42
     */
    public static final int FPGA_DISP_CHA2 = 0x42;                                        // 静态常量：主视图插值参数2寄存器地址

    /**
     * 主视图取数位置寄存器地址
     * 用于设置主视图取数的相对位置
     * 地址：0x43
     */
    public static final int FPGA_DISP_PLACE_MAIN = 0x43;                                  // 静态常量：主视图取数位置寄存器地址

    /**
     * 放大镜纵向乘法因子寄存器地址
     * 用于设置放大镜的纵向乘法因子
     * 地址：0x4B
     */
    public static final int FPGA_SEGMENT_NUMS = 0x4B;                                     // 静态常量：放大镜纵向乘法因子寄存器地址

    /**
     * 放大镜纵向显示范围寄存器地址
     * 用于设置放大镜的纵向显示范围
     * 地址：0x4C
     */
    public static final int FPGA_SEGMENT_START = 0x4C;                                    // 静态常量：放大镜纵向显示范围寄存器地址

    /**
     * 显示分辨率寄存器地址
     * 用于设置示波器的显示分辨率
     * 地址：0x4D
     */
    public static final int FPGA_DISP_RESOLUTION = 0x4D;                                  // 静态常量：显示分辨率寄存器地址

    /**
     * 缩略视图点阵合并寄存器地址
     * 用于设置缩略视图的点阵合并参数
     * 地址：0x4E
     */
    public static final int FPGA_DISP_PLACE_SL = 0x4E;                                    // 静态常量：缩略视图点阵合并寄存器地址

    /**
     * 串行取数相关寄存器地址
     * 用于设置串行取数相关参数
     * 地址：0x51
     */
    public static final int FPGA_DISP_SERI = 0x51;                                        // 静态常量：串行取数相关寄存器地址

    /**
     * 静态点阵纵向缩放使能寄存器地址
     * 用于设置静态点阵的纵向缩放使能
     * 地址：0x58
     */
    public static final int FPGA_DISP_Y_SCALE = 0x58;                                     // 静态常量：静态点阵纵向缩放使能寄存器地址

    /**
     * 波形抽样起始位置寄存器地址
     * 用于设置波形抽样的起始位置
     * 地址：0x5B
     */
    public static final int FPGA_DISP_WAVE = 0x5B;                                        // 静态常量：波形抽样起始位置寄存器地址

    /**
     * 余辉时间设置寄存器地址
     * 用于设置余辉显示的时间
     * 地址：0x5D
     */
    public static final int FPGA_DISP_YUHUI = 0x5D;                                        // 静态常量：余辉时间设置寄存器地址

    /**
     * 缩略视图显示列范围寄存器地址
     * 用于设置缩略视图的显示列范围
     * 地址：0x5E
     */
    public static final int FPGA_DISP_SL = 0x5E;                                          // 静态常量：缩略视图显示列范围寄存器地址

    /**
     * 波形数据列数寄存器地址
     * 用于设置波形数据每列的采样点数
     * 地址：0x5F
     */
    public static final int FPGA_DISP_WAVE_LIE_NUM = 0x5F;                                 // 静态常量：波形数据列数寄存器地址

    // ═══════════════════════════════════════════════════════════════════════════════
    // 外设控制寄存器地址常量 (0x60-0x77)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 写AD1寄存器地址
     * 用于向AD转换器1写入配置值
     * 地址：0x60
     */
    public static final int FPGA_WRITE_AD1 = 0x60;                                        // 静态常量：写AD1寄存器地址

    /**
     * 写AD2寄存器地址
     * 用于向AD转换器2写入配置值
     * 地址：0x61
     */
    public static final int FPGA_WRITE_AD2 = 0x61;                                        // 静态常量：写AD2寄存器地址

    /**
     * FPGA接口测试寄存器地址
     * 用于测试FPGA接口功能
     * 地址：0x63
     */
    public static final int FPGA_INTERFACE_TEST = 0x63;                                   // 静态常量：FPGA接口测试寄存器地址

    /**
     * 通道偏移DA寄存器地址（通道1-2）
     * 用于设置通道1和通道2的偏移DA值
     * 地址：0x64
     */
    public static final int FPGA_CH_OFFSET_DA12 = 0x64;                                   // 静态常量：通道偏移DA寄存器地址（通道1-2）

    /**
     * 通道偏移DA寄存器地址（通道3-4）
     * 用于设置通道3和通道4的偏移DA值
     * 地址：0x65
     */
    public static final int FPGA_CH_OFFSET_DA34 = 0x65;                                   // 静态常量：通道偏移DA寄存器地址（通道3-4）

    /**
     * 探头DA寄存器地址（通道1-2）
     * 用于设置探头1和探头2的DA值
     * 地址：0x66
     */
    public static final int FPGA_PROBE_DA12 = 0x66;                                       // 静态常量：探头DA寄存器地址（通道1-2）

    /**
     * 探头DA寄存器地址（通道3-4）
     * 用于设置探头3和探头4的DA值
     * 地址：0x67
     */
    public static final int FPGA_PROBE_DA34 = 0x67;                                       // 静态常量：探头DA寄存器地址（通道3-4）

    /**
     * 通道电容寄存器地址1
     * 用于设置通道1的电容参数
     * 地址：0x69
     */
    public static final int FPGA_CH_CAPACITANCE1 = 0x69;                                  // 静态常量：通道电容寄存器地址1

    /**
     * 通道电容寄存器地址2
     * 用于设置通道2的电容参数
     * 地址：0x6A
     */
    public static final int FPGA_CH_CAPACITANCE2 = 0x6A;                                  // 静态常量：通道电容寄存器地址2

    /**
     * 通道电容寄存器地址1（16位）
     * 用于设置通道1的16位电容参数
     * 地址：0x6B
     */
    public static final int FPGA_CH_CAPACITANCE1_16BIT = 0x6B;                             // 静态常量：通道电容寄存器地址1（16位）

    /**
     * 通道电容寄存器地址2（16位）
     * 用于设置通道2的16位电容参数
     * 地址：0x6C
     */
    public static final int FPGA_CH_CAPACITANCE2_16BIT = 0x6C;                             // 静态常量：通道电容寄存器地址2（16位）

    /**
     * 通道电容DA寄存器地址（通道1-2）
     * 用于设置通道1和通道2的电容DA值
     * 地址：0x6D
     */
    public static final int FPGA_CH_CAP_DA12 = 0x6D;                                      // 静态常量：通道电容DA寄存器地址（通道1-2）

    /**
     * 通道电容DA寄存器地址（通道3-4）
     * 用于设置通道3和通道4的电容DA值
     * 地址：0x6E
     */
    public static final int FPGA_CH_CAP_DA34 = 0x6E;                                      // 静态常量：通道电容DA寄存器地址（通道3-4）

    /**
     * 外部触发电平寄存器地址
     * 用于设置外部触发信号的电平阈值
     * 地址：0x6F
     */
    public static final int FPGA_EXT_GRIGGER_LEVEL = 0x6F;                                // 静态常量：外部触发电平寄存器地址

    /**
     * 通道阻抗寄存器地址
     * 用于设置通道的输入阻抗（1MΩ或50Ω）
     * 地址：0x68
     */
    public static final int FPGA_CH_RESISTANCE = 0x68;                                    // 静态常量：通道阻抗寄存器地址

    /**
     * 触发偏移寄存器地址1
     * 用于设置触发偏移校准值1
     * 地址：0x70
     */
    public static final int FPGA_TRIG_OFFSET1 = 0x70;                                     // 静态常量：触发偏移寄存器地址1

    /**
     * 触发偏移寄存器地址2
     * 用于设置触发偏移校准值2
     * 地址：0x71
     */
    public static final int FPGA_TRIG_OFFSET2 = 0x71;                                     // 静态常量：触发偏移寄存器地址2

    /**
     * 触发偏移寄存器地址3
     * 用于设置触发偏移校准值3
     * 地址：0x72
     */
    public static final int FPGA_TRIG_OFFSET3 = 0x72;                                     // 静态常量：触发偏移寄存器地址3

    /**
     * 触发偏移寄存器地址4
     * 用于设置触发偏移校准值4
     * 地址：0x73
     */
    public static final int FPGA_TRIG_OFFSET4 = 0x73;                                     // 静态常量：触发偏移寄存器地址4

    /**
     * 触发偏移寄存器地址5
     * 用于设置触发偏移校准值5
     * 地址：0x74
     */
    public static final int FPGA_TRIG_OFFSET5 = 0x74;                                     // 静态常量：触发偏移寄存器地址5

    /**
     * 触发偏移寄存器地址6
     * 用于设置触发偏移校准值6
     * 地址：0x75
     */
    public static final int FPGA_TRIG_OFFSET6 = 0x75;                                     // 静态常量：触发偏移寄存器地址6

    /**
     * 触发偏移寄存器地址7
     * 用于设置触发偏移校准值7
     * 地址：0x76
     */
    public static final int FPGA_TRIG_OFFSET7 = 0x76;                                     // 静态常量：触发偏移寄存器地址7

    /**
     * 触发偏移寄存器地址8
     * 用于设置触发偏移校准值8
     * 地址：0x77
     */
    public static final int FPGA_TRIG_OFFSET8 = 0x77;                                     // 静态常量：触发偏移寄存器地址8

    // ═══════════════════════════════════════════════════════════════════════════════
    // 中断和通道显示寄存器地址常量 (0x90, 0xB0-0xBA)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 中断寄存器地址
     * 用于控制FPGA中断功能
     * 地址：0x90
     */
    public static final int FPGA_INTRPT = 0x90;                                           // 静态常量：中断寄存器地址

    /**
     * 通道显示位置寄存器地址
     * 用于设置通道的显示位置
     * 地址：0xB0
     */
    public static final int FPGA_CH_DSIPLAY_POS = 0xB0;                                   // 静态常量：通道显示位置寄存器地址

    /**
     * 通道显示寄存器地址
     * 用于设置通道的显示参数
     * 地址：0xB8
     */
    public static final int FPGA_CH_DISPLAY = 0xB8;                                       // 静态常量：通道显示寄存器地址

    /**
     * 缩放小视图位置寄存器地址
     * 用于设置缩放小视图的位置
     * 地址：0xB9
     */
    public static final int FPGA_ZOOM_SMALL_PLACE = 0xB9;                                 // 静态常量：缩放小视图位置寄存器地址

    /**
     * 缩放小视图数量寄存器地址
     * 用于设置缩放小视图的数量
     * 地址：0xBA
     */
    public static final int FPGA_ZOOM_SMALL_NUM = 0xBA;                                   // 静态常量：缩放小视图数量寄存器地址

    // ═══════════════════════════════════════════════════════════════════════════════
    // 命令控制寄存器地址常量 (0xA0-0xAD)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * FPGA命令寄存器地址
     * 用于发送FPGA命令
     * 地址：0xA0
     */
    public static final int FPGA_COMMAND = 0xA0;                                          // 静态常量：FPGA命令寄存器地址

    /**
     * 点阵缩放寄存器地址
     * 用于设置点阵缩放参数
     * 地址：0xA1
     */
    public static final int FPGA_DOT_MATRIX = 0xA1;                                       // 静态常量：点阵缩放寄存器地址

    /**
     * 获取数据寄存器地址
     * 用于从FPGA获取数据
     * 地址：0xA2
     */
    public static final int FPGA_GET_DATA = 0xA2;                                         // 静态常量：获取数据寄存器地址

    /**
     * 插值系数寄存器地址
     * 用于重载插值系数
     * 地址：0xA3
     */
    public static final int FPGA_CHAZHI_COEF = 0xA3;                                      // 静态常量：插值系数寄存器地址

    /**
     * AD系数寄存器地址
     * 用于设置AD系数
     * 地址：0xA4
     */
    public static final int FPGA_AD_COEF = 0xA4;                                          // 静态常量：AD系数寄存器地址

    /**
     * 搜索系数寄存器地址
     * 用于设置搜索系数
     * 地址：0xA5
     */
    public static final int FPGA_SEACH_COEF = 0xA5;                                       // 静态常量：搜索系数寄存器地址

    /**
     * 信号频率寄存器地址
     * 用于设置信号频率参数
     * 地址：0xA7
     */
    public static final int FPGA_SIGNAL_FREQ = 0xA7;                                      // 静态常量：信号频率寄存器地址

    /**
     * 强制触发寄存器地址
     * 用于发送强制触发命令
     * 地址：0xA8
     */
    public static final int FPGA_FORCE_TRIGGER = 0xA8;                                    // 静态常量：强制触发寄存器地址

    /**
     * 灰度等级寄存器地址
     * 用于设置灰度等级
     * 地址：0xA9
     */
    public static final int FPGA_GRAY_LEVEL = 0xA9;                                       // 静态常量：灰度等级寄存器地址

    /**
     * 分段时间戳寄存器地址
     * 用于设置分段采样的时间戳
     * 地址：0xAA
     */
    public static final int FPGA_SEGMENT_TS = 0xAA;                                       // 静态常量：分段时间戳寄存器地址

    /**
     * SPI扩展寄存器地址
     * 用于设置SPI扩展参数
     * 地址：0xAB
     */
    public static final int FPGA_SPI_EXT = 0xAB;                                          // 静态常量：SPI扩展寄存器地址

    /**
     * SPI读取寄存器地址
     * 用于读取SPI数据
     * 地址：0xAC
     */
    public static final int FPGA_SPI_R = 0xAC;                                            // 静态常量：SPI读取寄存器地址

    /**
     * 垂直缩放寄存器地址
     * 用于设置垂直缩放参数
     * 地址：0xAD
     */
    public static final int FPGA_V_ZOOM = 0xAD;                                           // 静态常量：垂直缩放寄存器地址

    // ═══════════════════════════════════════════════════════════════════════════════
    // 状态读取寄存器地址常量 (0x80-0x8F, 0xD0-0xD8)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * FPGA状态ID寄存器地址
     * 用于读取FPGA的状态ID
     * 地址：0x80
     */
    public static final int FPGA_STATUS_ID = 0x80;                                        // 静态常量：FPGA状态ID寄存器地址

    /**
     * FPGA状态寄存器地址
     * 用于读取FPGA的当前状态
     * 地址：0x81
     */
    public static final int FPGA_STATUS_STATE = 0x81;                                     // 静态常量：FPGA状态寄存器地址

    /**
     * FPGA刷新状态寄存器地址
     * 用于读取FPGA的刷新状态
     * 地址：0x82
     */
    public static final int FPGA_STATUS_REFRESH = 0x82;                                   // 静态常量：FPGA刷新状态寄存器地址

    /**
     * 自动测量电压状态寄存器地址
     * 用于读取自动测量的电压值
     * 地址：0x84
     */
    public static final int FPGA_STATUS_AUTO_V = 0x84;                                    // 静态常量：自动测量电压状态寄存器地址

    /**
     * 自动测量电压状态寄存器地址1
     * 与FPGA_STATUS_AUTO_V相同
     */
    public static final int FPGA_STATUS_AUTO_V1 = 0x84;                                   // 静态常量：自动测量电压状态寄存器地址1

    /**
     * 自动测量电压状态寄存器地址2
     * 用于读取自动测量的电压值2
     * 地址：0x85
     */
    public static final int FPGA_STATUS_AUTO_V2 = 0x85;                                   // 静态常量：自动测量电压状态寄存器地址2

    /**
     * 自动测量周期状态寄存器地址1
     * 用于读取自动测量的周期值1
     * 地址：0x86
     */
    public static final int FPGA_STATUS_AUTO_CYCLE_1 = 0x86;                              // 静态常量：自动测量周期状态寄存器地址1

    /**
     * 自动测量周期状态寄存器地址2
     * 用于读取自动测量的周期值2
     * 地址：0x87
     */
    public static final int FPGA_STATUS_AUTO_CYCLE_2 = 0x87;                              // 静态常量：自动测量周期状态寄存器地址2

    /**
     * 自动测量周期状态寄存器地址3
     * 用于读取自动测量的周期值3
     * 地址：0x88
     */
    public static final int FPGA_STATUS_AUTO_CYCLE_3 = 0x88;                              // 静态常量：自动测量周期状态寄存器地址3

    /**
     * 自动测量周期状态寄存器地址4
     * 用于读取自动测量的周期值4
     * 地址：0x89
     */
    public static final int FPGA_STATUS_AUTO_CYCLE_4 = 0x89;                              // 静态常量：自动测量周期状态寄存器地址4

    /**
     * 探头状态寄存器地址
     * 用于读取探头连接状态和FIFO大小
     * 地址：0x8A
     */
    public static final int FPGA_PROBE = 0x8A;                                            // 静态常量：探头状态寄存器地址

    /**
     * FPGA版本寄存器地址
     * 用于读取FPGA的版本号
     * 地址：0x8B
     */
    public static final int FPGA_STATUS_VER = 0x8B;                                       // 静态常量：FPGA版本寄存器地址

    /**
     * FPGA测试寄存器地址
     * 用于读取FPGA的测试状态
     * 地址：0x8C
     */
    public static final int FPGA_STATUS_TEST = 0x8C;                                      // 静态常量：FPGA测试寄存器地址

    /**
     * FPGA帧数寄存器地址
     * 用于读取FPGA的帧计数
     * 地址：0x8D
     */
    public static final int FPGA_STATUS_FRAMES = 0x8D;                                    // 静态常量：FPGA帧数寄存器地址

    /**
     * 温度寄存器地址
     * 用于读取FPGA的温度值
     * 地址：0x8E
     */
    public static final int FPGA_TEMPERATURE = 0x8E;                                      // 静态常量：温度寄存器地址

    /**
     * 风扇速度寄存器地址
     * 用于读取风扇的速度值
     * 地址：0x8F
     */
    public static final int FPGA_FAN_SPEED = 0x8F;                                        // 静态常量：风扇速度寄存器地址

    /**
     * 通道温度状态寄存器地址
     * 用于读取通道的温度状态
     * 地址：0xD0
     */
    public static final int FPGA_STATUS_CH_T = 0xD0;                                      // 静态常量：通道温度状态寄存器地址

    /**
     * 通道数量状态寄存器地址
     * 用于读取通道数量状态
     * 地址：0xD1
     */
    public static final int FPGA_STATUS_CH_N = 0xD1;                                      // 静态常量：通道数量状态寄存器地址

    /**
     * 自动测量电压寄存器地址1
     * 用于读取自动测量的电压值1
     * 地址：0xD2
     */
    public static final int FPGA_AUTO_V1 = 0xD2;                                          // 静态常量：自动测量电压寄存器地址1

    /**
     * 自动测量电压寄存器地址2
     * 用于读取自动测量的电压值2
     * 地址：0xD3
     */
    public static final int FPGA_AUTO_V2 = 0xD3;                                          // 静态常量：自动测量电压寄存器地址2

    /**
     * 自动测量电压寄存器地址3
     * 用于读取自动测量的电压值3
     * 地址：0xD4
     */
    public static final int FPGA_AUTO_V3 = 0xD4;                                          // 静态常量：自动测量电压寄存器地址3

    /**
     * 自动测量电压寄存器地址4
     * 用于读取自动测量的电压值4
     * 地址：0xD5
     */
    public static final int FPGA_AUTO_V4 = 0xD5;                                          // 静态常量：自动测量电压寄存器地址4

    /**
     * 50Ω状态寄存器地址
     * 用于读取50Ω阻抗的状态
     * 地址：0xD8
     */
    public static final int FPGA_50O_STATUS = 0xD8;                                       // 静态常量：50Ω状态寄存器地址

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * FPGA寄存器命令头长度
     * 命令头占用8字节（2个32位字）
     * 字0：命令标识和寄存器地址
     * 字1：数据长度
     */
    public static final int FPGA_REG_HEADER_LEN = 8;                                      // 静态常量：寄存器命令头长度（8字节）

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 字节缓冲区
     * 用于存储寄存器命令数据
     * 包含命令头和数据部分
     */
    protected ByteBuffer byteBuffer;                                                      // 成员变量：字节缓冲区，存储寄存器数据

    /**
     * 整数缓冲区
     * 用于32位数据操作
     * 与byteBuffer共享同一数据
     */
    private IntBuffer intBuffer;                                                          // 成员变量：整数缓冲区，用于32位数据操作

    /**
     * 寄存器地址
     * 存储当前寄存器的地址值
     */
    private int addr;                                                                      // 成员变量：寄存器地址

    /**
     * 寄存器数据长度
     * 存储当前寄存器的数据长度（字节）
     */
    private int length;                                                                    // 成员变量：寄存器数据长度

    /**
     * FPGA索引
     * 用于多FPGA系统中的FPGA标识
     */
    private int fpgaIdx = 0;                                                               // 成员变量：FPGA索引

    /**
     * 接收模式标志
     * true表示读取寄存器，false表示写入寄存器
     */
    boolean bRecv = false;                                                                 // 成员变量：接收模式标志

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数（写入模式）
     * 创建写入类型的寄存器实例
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>addr：寄存器地址</li>
     *   <li>len：数据长度（字节）</li>
     * </ul>
     *
     * <p><b>调用示例：</b></p>
     * <pre>
     * // 创建采样模式寄存器（写入模式）
     * FPGAReg reg = new FPGAReg(FPGAReg.FPGA_SAMPLE_MODE, 4);
     * </pre>
     *
     * @param addr 寄存器地址
     * @param len 数据长度（字节）
     */
    public FPGAReg(int addr, int len){                                                    // 构造方法：创建写入模式寄存器
        // 调用完整构造函数，默认为写入模式（bRecv=false）
        this(addr, len, false);                                                            // 调用完整构造函数，设置写入模式
    }                                                                                     // 构造方法结束

    /**
     * 构造函数（完整参数）
     * 创建寄存器实例，可指定读取或写入模式
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>addr：寄存器地址</li>
     *   <li>len：数据长度（字节）</li>
     *   <li>bRecv：接收模式标志（true=读取，false=写入）</li>
     * </ul>
     *
     * <p><b>初始化流程：</b></p>
     * <ol>
     *   <li>保存寄存器地址和数据长度</li>
     *   <li>创建字节缓冲区（长度=数据长度+命令头长度）</li>
     *   <li>设置小端序（FPGA使用小端序）</li>
     *   <li>创建整数缓冲区视图</li>
     *   <li>初始化命令头</li>
     * </ol>
     *
     * <p><b>调用示例：</b></p>
     * <pre>
     * // 创建写入寄存器
     * FPGAReg writeReg = new FPGAReg(FPGAReg.FPGA_SAMPLE_MODE, 4, false);
     *
     * // 创建读取寄存器
     * FPGAReg readReg = new FPGAReg(FPGAReg.FPGA_STATUS_STATE, 4, true);
     * </pre>
     *
     * @param addr 寄存器地址
     * @param len 数据长度（字节）
     * @param bRecv 接收模式标志（true=读取，false=写入）
     */
    public FPGAReg(int addr, int len, boolean bRecv){                                     // 构造方法：创建寄存器实例（完整参数）
        // 保存寄存器地址
        this.addr = addr;                                                                  // 保存寄存器地址到成员变量

        // 保存数据长度
        this.length = len;                                                                 // 保存数据长度到成员变量

        // 保存接收模式标志
        this.bRecv = bRecv;                                                                // 保存接收模式标志到成员变量

        // 创建字节缓冲区
        // 分配大小 = 数据长度 + 命令头长度（8字节）
        byteBuffer = ByteBuffer.allocate(this.length + FPGA_REG_HEADER_LEN);              // 创建字节缓冲区，大小为数据长度+命令头长度

        // 设置字节顺序为小端序
        // FPGA使用小端序，低位字节在前
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);                                         // 设置字节顺序为小端序

        // 创建整数缓冲区视图
        // intBuffer与byteBuffer共享同一数据，便于32位数据操作
        intBuffer = byteBuffer.asIntBuffer();                                              // 创建整数缓冲区视图

        // 初始化命令头
        // 根据接收模式设置不同的命令标识
        reset(bRecv);                                                                      // 初始化命令头
    }                                                                                     // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - FPGA索引管理
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置FPGA索引
     * 用于多FPGA系统中的FPGA标识
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>在多FPGA系统中，需要标识命令发送到哪个FPGA</li>
     *   <li>FPGA索引用于区分不同的FPGA芯片</li>
     * </ul>
     *
     * @param fpgaIdx FPGA索引值
     */
    public void setFpgaIdx(int fpgaIdx){                                                  // 公有方法：设置FPGA索引
        this.fpgaIdx = fpgaIdx;                                                            // 保存FPGA索引到成员变量
    }                                                                                     // 方法结束

    /**
     * 获取FPGA索引
     * 返回当前寄存器关联的FPGA索引
     *
     * <p><b>返回值说明：</b></p>
     * <ul>
     *   <li>返回FPGA索引值</li>
     *   <li>默认值为0</li>
     * </ul>
     *
     * @return FPGA索引值
     */
    public int getFpgaIdx(){                                                              // 公有方法：获取FPGA索引
        return fpgaIdx;                                                                    // 返回FPGA索引值
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 命令长度管理
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置命令数据长度
     * 动态修改寄存器的数据长度
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>某些寄存器的数据长度可能需要动态调整</li>
     *   <li>此方法允许在运行时修改数据长度</li>
     * </ul>
     *
     * @param len 新的数据长度（字节）
     */
    public void setCommandLength(int len){                                                // 公有方法：设置命令数据长度
        this.length = len;                                                                 // 更新数据长度
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 调试输出
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 打印寄存器数据
     * 用于调试，输出寄存器的完整数据
     *
     * <p><b>输出格式：</b></p>
     * <pre>
     * fpga = [FPGA索引], Addr = [寄存器地址], len = [数据长度], data: [数据内容]
     * </pre>
     *
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>调试寄存器设置是否正确</li>
     *   <li>验证命令数据格式</li>
     *   <li>排查通信问题</li>
     * </ul>
     */
    public void Dump(){                                                                   // 公有方法：打印寄存器数据（调试用）
        // 创建字符串构建器
        StringBuilder stringBuffer = new StringBuilder();                                  // 创建字符串构建器

        // 添加FPGA索引
        stringBuffer.append("fpga = ").append(getFpgaIdx());                               // 添加FPGA索引信息

        // 添加寄存器地址（十六进制格式）
        stringBuffer.append(",Addr = ");                                                   // 添加地址标签
        stringBuffer.append(Integer.toHexString(addr));                                    // 添加寄存器地址（十六进制）

        // 添加数据长度
        stringBuffer.append(",len = ");                                                    // 添加长度标签
        stringBuffer.append(Integer.toString(length));                                     // 添加数据长度

        // 添加数据内容
        stringBuffer.append(",data:");                                                     // 添加数据标签

        // 循环添加每个字的数据（十六进制格式）
        for (int i = 0; i < intBuffer.capacity(); i++) {                                   // 循环：遍历所有数据字
            stringBuffer.append(Integer.toHexString(intBuffer.get(i)));                    // 添加当前字的十六进制值
            stringBuffer.append(" ");                                                       // 添加空格分隔
        }                                                                                  // 循环结束

        // 输出日志
        Logger.d(TAG, stringBuffer.toString());                                            // 输出调试日志
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 寄存器信息获取
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取寄存器地址
     * 返回当前寄存器的地址值
     *
     * @return 寄存器地址
     */
    public int getAddr(){                                                                 // 公有方法：获取寄存器地址
        return addr;                                                                       // 返回寄存器地址
    }                                                                                     // 方法结束

    /**
     * 获取数据长度
     * 返回当前寄存器的数据长度
     *
     * @return 数据长度（字节）
     */
    public int getLength(){                                                               // 公有方法：获取数据长度
        return length;                                                                     // 返回数据长度
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 命令数据获取
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取命令数据
     * 返回完整的寄存器命令数据（包含命令头和数据）
     *
     * <p><b>返回值说明：</b></p>
     * <ul>
     *   <li>返回ByteBuffer对象，包含完整的命令数据</li>
     *   <li>数据格式：命令头（8字节）+ 数据部分</li>
     *   <li>字节顺序：小端序</li>
     * </ul>
     *
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>FPGACommand发送命令时调用此方法获取数据</li>
     *   <li>用于SPI或UART通信发送</li>
     * </ul>
     *
     * @return 命令数据ByteBuffer
     */
    public ByteBuffer getCommand(){                                                       // 公有方法：获取命令数据
        // 调试输出（已注释）
        // Dump();                                                                         // 调试：打印寄存器数据

        // 返回字节缓冲区
        return byteBuffer;                                                                 // 返回字节缓冲区（包含完整命令数据）
    }                                                                                     // 方法结束

    /**
     * 获取命令长度
     * 返回需要发送的命令数据长度
     *
     * <p><b>计算规则：</b></p>
     * <ul>
     *   <li>写入模式：命令长度 = 数据长度 + 命令头长度</li>
     *   <li>读取模式：命令长度 = 命令头长度（只发送命令头，等待返回数据）</li>
     * </ul>
     *
     * @return 命令长度（字节）
     */
    public int getCommandLength(){                                                        // 公有方法：获取命令长度
        // 计算命令长度
        int cmdlen = length + FPGA_REG_HEADER_LEN;                                         // 默认：数据长度 + 命令头长度

        // 读取模式：只发送命令头
        if(bRecv)                                                                          // 判断：是否为读取模式
            cmdlen = FPGA_REG_HEADER_LEN;                                                  // 读取模式：命令长度 = 命令头长度

        // 返回命令长度
        return cmdlen;                                                                     // 返回计算后的命令长度
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 寄存器重置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 重置寄存器（写入模式）
     * 清空数据并初始化命令头为写入模式
     */
    public void reset(){                                                                  // 公有方法：重置寄存器（写入模式）
        reset(false);                                                                      // 调用完整重置方法，设置为写入模式
    }                                                                                     // 方法结束

    /**
     * 重置寄存器（完整参数）
     * 清空数据并初始化命令头
     *
     * <p><b>初始化内容：</b></p>
     * <ul>
     *   <li>清空整数缓冲区</li>
     *   <li>设置命令头字0：命令标识 + 寄存器地址</li>
     *   <li>设置命令头字1：数据长度（以32位字为单位）</li>
     *   <li>清零数据部分</li>
     * </ul>
     *
     * <p><b>命令头格式：</b></p>
     * <pre>
     * 写入模式：字0 = 0x8180 | (addr << 16)
     * 读取模式：字0 = 0x2421 | (addr << 16)
     * 字1 = 数据长度 / 4
     * </pre>
     *
     * @param bRecv 接收模式标志（true=读取，false=写入）
     */
    public void reset(boolean bRecv){                                                     // 公有方法：重置寄存器（完整参数）
        // 清空整数缓冲区
        intBuffer.clear();                                                                 // 清空整数缓冲区

        // 设置命令标识
        int val = 0x8180;                                                                  // 默认：写入命令标识（0x8180）

        // 读取模式：设置读取命令标识
        if(bRecv) {                                                                        // 判断：是否为读取模式
            val = 0x2421;                                                                  // 读取命令标识（0x2421）
        }                                                                                  // 判断结束

        // 设置命令头字0：命令标识 + 寄存器地址
        // 格式：位0-15为命令标识，位16-31为寄存器地址
        intBuffer.put(0, val | (addr << 16));                                              // 设置字0：命令标识 + 寄存器地址

        // 设置命令头字1：数据长度（以32位字为单位）
        intBuffer.put(1, length / 4);                                                      // 设置字1：数据长度/4

        // 清零数据部分
        // 循环设置每个数据字为0
        for(int i = 0; i < length / 4; i++){                                               // 循环：遍历所有数据字
            intBuffer.put(i + FPGA_REG_HEADER_LEN / 4, 0);                                 // 设置当前数据字为0
        }                                                                                  // 循环结束
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 命令长度设置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置命令长度
     * 动态修改命令的数据长度
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>修改命令头中的数据长度字段</li>
     *   <li>用于动态调整发送的数据量</li>
     * </ul>
     *
     * @param cmdLen 命令长度（字节）
     */
    public void setCmdLength(int cmdLen){                                                 // 公有方法：设置命令长度
        // 设置命令头字0：写入命令标识 + 寄存器地址
        intBuffer.put(0, 0x8180 | (addr << 16));                                            // 设置字0：写入命令标识 + 寄存器地址

        // 设置命令头字1：数据长度（以32位字为单位）
        intBuffer.put(1, cmdLen / 4);                                                      // 设置字1：命令长度/4
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 数据读取
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取数据值（字0）
     * 返回数据部分的第一个32位字的值
     *
     * @return 数据值（32位整数）
     */
    public int getVal(){                                                                  // 公有方法：获取数据值（字0）
        return getVal(0);                                                                  // 调用完整方法，获取字0的值
    }                                                                                     // 方法结束

    /**
     * 获取数据值（指定字）
     * 返回数据部分指定索引的32位字的值
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>idx：字索引（0表示第一个数据字）</li>
     * </ul>
     *
     * <p><b>数据位置：</b></p>
     * <pre>
     * 数据部分起始位置 = FPGA_REG_HEADER_LEN / 4 = 2
     * 实际位置 = idx + 2
     * </pre>
     *
     * @param idx 字索引
     * @return 数据值（32位整数）
     */
    public int getVal(int idx){                                                           // 公有方法：获取数据值（指定字）
        // 返回指定索引的数据值
        // 数据部分从索引FPGA_REG_HEADER_LEN/4开始
        return intBuffer.get(idx + FPGA_REG_HEADER_LEN / 4);                               // 返回指定字的数据值
    }                                                                                     // 方法结束

    /**
     * 检查响应头是否有效
     * 用于验证FPGA返回的响应数据是否有效
     *
     * <p><b>验证规则：</b></p>
     * <ul>
     *   <li>字0和字1必须相同</li>
     *   <li>字0必须等于0x181118ff</li>
     * </ul>
     *
     * <p><b>返回值说明：</b></p>
     * <ul>
     *   <li>true：响应有效</li>
     *   <li>false：响应无效</li>
     * </ul>
     *
     * @return 响应是否有效
     */
    public boolean isHeaderValid(){                                                       // 公有方法：检查响应头是否有效
        // 验证响应头
        // 条件：字0 == 字1 && 字0 == 0x181118ff
        return (intBuffer.get(0) == intBuffer.get(1)) && (intBuffer.get(0) == 0x181118ff);  // 返回响应头是否有效
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 数据写入（数组）
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置数据值（字节数组）
     * 将字节数组数据写入寄存器数据部分
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>datas：字节数组数据</li>
     * </ul>
     *
     * <p><b>写入位置：</b></p>
     * <ul>
     *   <li>数据从命令头之后开始写入</li>
     *   <li>起始位置 = FPGA_REG_HEADER_LEN</li>
     * </ul>
     *
     * @param datas 字节数组数据
     */
    public void setVal(byte [] datas){                                                    // 公有方法：设置数据值（字节数组）
        // 循环写入每个字节
        for (int i = 0; i < datas.length; i++){                                            // 循环：遍历字节数组
            // 写入字节到数据部分
            byteBuffer.put(i + FPGA_REG_HEADER_LEN, datas[i]);                              // 写入字节到指定位置
        }                                                                                  // 循环结束
    }                                                                                     // 方法结束

    /**
     * 设置数据值（短整数数组）
     * 将16位短整数数组数据写入寄存器数据部分
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>datas：短整数数组数据（16位）</li>
     * </ul>
     *
     * <p><b>写入位置：</b></p>
     * <ul>
     *   <li>数据从命令头之后开始写入</li>
     *   <li>起始位置 = FPGA_REG_HEADER_LEN / 2（以16位为单位）</li>
     * </ul>
     *
     * @param datas 短整数数组数据
     */
    public void setVal(short [] datas){                                                   // 公有方法：设置数据值（短整数数组）
        // 创建短整数缓冲区视图
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();                              // 创建短整数缓冲区视图

        // 循环写入每个短整数
        for (int i = 0; i < datas.length; i++){                                            // 循环：遍历短整数数组
            // 写入短整数到数据部分
            shortBuffer.put(i + FPGA_REG_HEADER_LEN / 2, datas[i]);                         // 写入短整数到指定位置
        }                                                                                  // 循环结束
    }                                                                                     // 方法结束

    /**
     * 设置数据值（整数数组）
     * 将32位整数数组数据写入寄存器数据部分
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>datas：整数数组数据（32位）</li>
     * </ul>
     *
     * <p><b>写入位置：</b></p>
     * <ul>
     *   <li>数据从命令头之后开始写入</li>
     *   <li>起始位置 = FPGA_REG_HEADER_LEN / 4（以32位为单位）</li>
     * </ul>
     *
     * @param datas 整数数组数据
     */
    public void setVal(int [] datas){                                                     // 公有方法：设置数据值（整数数组）
        // 循环写入每个整数
        for(int i = 0; i < datas.length; i++){                                             // 循环：遍历整数数组
            // 写入整数到数据部分
            intBuffer.put(i + FPGA_REG_HEADER_LEN / 4, datas[i]);                           // 写入整数到指定位置
        }                                                                                  // 循环结束
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 数据写入（单个值）
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置数据值（指定字，完整32位）
     * 将32位值写入指定字
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>idx：字索引</li>
     *   <li>val：32位值</li>
     * </ul>
     *
     * @param idx 字索引
     * @param val 32位值
     */
    public void setVal(int idx, int val){                                                 // 公有方法：设置数据值（指定字，完整32位）
        // 调用完整方法，设置32位值
        setVal(idx, 0, 32, val);                                                           // 调用位操作方法，设置完整32位值
    }                                                                                     // 方法结束

    /**
     * 设置数据值（字0，指定位范围）
     * 将值写入字0的指定位范围
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>startBit：起始位位置（0-31）</li>
     *   <li>bitsWidth：位宽度（1-32）</li>
     *   <li>val：要写入的值</li>
     * </ul>
     *
     * @param startBit 起始位位置
     * @param bitsWidth 位宽度
     * @param val 要写入的值
     */
    public void setVal(int startBit, int bitsWidth, int val){                             // 公有方法：设置数据值（字0，指定位范围）
        // 调用完整方法，写入字0
        setVal(0, startBit, bitsWidth, val);                                               // 调用完整位操作方法
    }                                                                                     // 方法结束

    /**
     * 设置数据值（指定字，指定位范围）
     * 将值写入指定字的指定位范围
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>支持位级别的精确数据写入</li>
     *   <li>保留其他位的原有值</li>
     *   <li>只修改指定范围内的位</li>
     * </ul>
     *
     * <p><b>操作流程：</b></p>
     * <ol>
     *   <li>创建位掩码（bitsWidth位全1）</li>
     *   <li>读取当前字的值</li>
     *   <li>清除目标位范围（用掩码清零）</li>
     *   <li>写入新值（用掩码写入）</li>
     *   <li>保存修改后的值</li>
     * </ol>
     *
     * <p><b>位掩码计算：</b></p>
     * <pre>
     * mask = 0
     * for(i = 0; i < bitsWidth; i++)
     *     mask |= 1 << i
     * 结果：bitsWidth位全1的掩码
     * </pre>
     *
     * <p><b>位操作公式：</b></p>
     * <pre>
     * 清除位：cmd &= ~(mask << startBit)
     * 设置位：cmd |= ((val & mask) << startBit)
     * </pre>
     *
     * @param idx 字索引
     * @param startBit 起始位位置
     * @param bitsWidth 位宽度
     * @param val 要写入的值
     */
    public void setVal(int idx, int startBit, int bitsWidth, int val){                    // 公有方法：设置数据值（指定字，指定位范围）
        // 临时变量：存储当前字的值
        int cmd;                                                                           // 临时变量：当前字的值

        // 创建位掩码
        // 掩码为bitsWidth位全1
        int mask = 0;                                                                      // 初始化掩码为0

        // 循环设置掩码的每一位为1
        for(int i = 0; i < bitsWidth; i++){                                                // 循环：设置掩码的bitsWidth位
            mask |= 1 << i;                                                                // 设置当前位为1
        }                                                                                  // 循环结束

        // 读取当前字的值
        // 数据部分从索引FPGA_REG_HEADER_LEN/4开始
        cmd = intBuffer.get(idx + FPGA_REG_HEADER_LEN / 4);                                // 获取当前字的值

        // 清除目标位范围
        // 使用掩码清除startBit开始的bitsWidth位
        cmd &= ~(mask << startBit);                                                        // 清除目标位：将目标位范围清零

        // 写入新值
        // 将val与掩码运算后，写入目标位范围
        cmd |= ((val & mask) << startBit);                                                 // 写入新值：将val写入目标位范围

        // 保存修改后的值
        intBuffer.put(idx + FPGA_REG_HEADER_LEN / 4, cmd);                                 // 保存修改后的值到缓冲区
    }                                                                                     // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 命令回调
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 命令发送前的回调方法
     * 子类可重写此方法，在命令发送前自动设置寄存器值
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>模板方法模式：子类重写此方法实现特定逻辑</li>
     *   <li>在FPGACommand发送命令前调用</li>
     *   <li>用于自动计算和设置寄存器参数</li>
     * </ul>
     *
     * <p><b>重写示例：</b></p>
     * <pre>
     * // 子类重写示例
     * public class FPGAReg_SAMPLE_MODE extends FPGAReg {
     *     @Override
     *     public void onCommand() {
     *         // 自动设置采样模式参数
     *         setVal(0, 0, 8, calculateSampleMode());
     *     }
     * }
     * </pre>
     *
     * <p><b>调用时机：</b></p>
     * <ul>
     *   <li>FPGACommand.sendCommand()发送命令前调用</li>
     *   <li>允许子类在发送前自动更新参数</li>
     * </ul>
     */
    public void onCommand(){                                                              // 公有方法：命令发送前的回调方法
        // 默认实现：空方法
        // 子类可重写此方法实现特定逻辑
    }                                                                                     // 方法结束

}                                                                                         // 类结束