package com.micsig.tbook.hardware; // 硬件层包名，包含示波器硬件相关操作类

import android.util.Log; // 导入Android日志工具类，用于调试输出

/**
 * ┌─────────────────────────────────────────────────────────────────────────────────────────┐
 * │                                  ShiftRegister                                          │
 * │                              移位寄存器硬件控制类                                         │
 * ├─────────────────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                             │
 * │   硬件抽象层 - 移位寄存器控制模块，用于扩展GPIO输出控制                                    │
 * │                                                                                         │
 * │ 【核心职责】                                                                             │
 * │   1. 数据移位：通过GPIO模拟SPI时序，将数据串行移入寄存器                                   │
 * │   2. 通道模式控制：控制示波器通道的耦合模式、衰减比等设置                                  │
 * │   3. 级联控制：支持多片移位寄存器级联，实现64位输出控制                                    │
 * │                                                                                         │
 * │ 【架构设计】                                                                             │
 * │   ┌─────────────────────────────────────────────────────────────────────────────────┐   │
 * │   │                          ShiftRegister (移位寄存器控制器)                        │   │
 * │   │  ┌───────────────────────────────────────────────────────────────────────────┐  │   │
 * │   │  │                        GPIO控制信号                                        │  │   │
 * │   │  │   DS(数据) ──→ [移位寄存器] ──→ SRCLK(移位时钟) ──→ RCLK(锁存时钟)        │  │   │
 * │   │  │                     ↑                                                    │  │   │
 * │   │  │                SRCLR(清零)                                               │  │   │
 * │   │  └───────────────────────────────────────────────────────────────────────────┘  │   │
 * │   │                                    │                                            │   │
 * │   │                                    ▼                                            │   │
 * │   │  ┌───────────────────────────────────────────────────────────────────────────┐  │   │
 * │   │  │                        移位寄存器链 (64位)                                 │  │   │
 * │   │  │   [74HC595 #1] → [74HC595 #2] → [74HC595 #3] → [74HC595 #4]              │  │   │
 * │   │  │       ↓              ↓              ↓              ↓                       │  │   │
 * │   │  │    CH1模式       CH2模式        CH3模式        CH4模式                    │  │   │
 * │   │  └───────────────────────────────────────────────────────────────────────────┘  │   │
 * │   └─────────────────────────────────────────────────────────────────────────────────┘   │
 * │                                                                                         │
 * │ 【时序图】                                                                               │
 * │   ┌──────────────────────────────────────────────────────────────────────────────────┐│
 * │   │  SRCLR ───┐     ┌─────────────────────────────────────────────────────────────── ││
 * │   │           └─────┘                                                                ││
 * │   │  DS    ───┬───┬───┬───┬───┬───┬───┬───┬───┐                                      ││
 * │   │           │D0 │D1 │D2 │D3 │D4 │D5 │D6 │D7 │                                      ││
 * │   │           └───┴───┴───┴───┴───┴───┴───┴───┘                                      ││
 * │   │  SRCLK ───┘   └───┘   └───┘   └───┘   └───┘   └───┘   └───┘   └───┘              ││
 * │   │  RCLK  ───────────────────────────────────────────────────────┐                  ││
 * │   │                                                                └─────────────────││
 * │   └──────────────────────────────────────────────────────────────────────────────────┘│
 * │                                                                                         │
 * │ 【数据流向】                                                                             │
 * │   64位数据 → 逐位移位 → 移位寄存器 → 锁存输出 → 通道模式控制电路                          │
 * │                                                                                         │
 * │ 【依赖关系】                                                                             │
 * │   - HwGpioManager: 获取GPIO设备实例                                                     │
 * │   - GpioDev: GPIO控制接口                                                              │
 * │                                                                                         │
 * │ 【使用示例】                                                                             │
 * │   ShiftRegister sr = new ShiftRegister();                                              │
 * │   long channelMode = 0x0000000000000001L;  // 设置通道模式                             │
 * │   sr.setVal(channelMode, 64);  // 写入64位数据                                         │
 * │                                                                                         │
 * │ 【硬件说明】                                                                             │
 * │   使用74HC595移位寄存器，4片级联实现64位输出                                            │
 * │   控制示波器4个通道的：                                                                  │
 * │   - 耦合模式（AC/DC/GND）                                                               │
 * │   - 衰减比（1X/10X）                                                                    │
 * │   - 带宽限制                                                                            │
 * │                                                                                         │
 * │ 【作者】zhuzh                                                                            │
 * │ 【日期】2018/3/12                                                                        │
 * └─────────────────────────────────────────────────────────────────────────────────────────┘
 */

public class ShiftRegister {

    // ==================== GPIO控制引脚定义 ====================

    /**
     * 数据输入引脚（DS - Data Serial）
     * 
     * 【功能说明】
     *   串行数据输入，每位数据通过此引脚移入寄存器
     *   数据在SRCLK上升沿被采样
     */
    private GpioDev mChModelDs; // 数据输入GPIO，对应74HC595的DS引脚

    /**
     * 移位寄存器清零引脚（SRCLR - Shift Register Clear）
     * 
     * 【功能说明】
     *   低电平有效，清零移位寄存器内容
     *   正常工作时保持高电平
     */
    private GpioDev mChModelSrclr; // 清零控制GPIO，对应74HC595的SRCLR引脚

    /**
     * 移位时钟引脚（SRCLK - Shift Register Clock）
     * 
     * 【功能说明】
     *   移位时钟，上升沿时将DS上的数据移入寄存器
     *   每个上升沿移位一次
     */
    private GpioDev mChModelSrclk; // 移位时钟GPIO，对应74HC595的SRCLK引脚

    /**
     * 锁存时钟引脚（RCLK - Register Clock）
     * 
     * 【功能说明】
     *   锁存时钟，上升沿时将移位寄存器内容锁存到输出寄存器
     *   数据移位完成后，通过此引脚更新输出
     */
    private GpioDev mChModelRclk; // 锁存时钟GPIO，对应74HC595的RCLK引脚

    // ==================== 构造方法 ====================

    /**
     * 构造函数 - 初始化移位寄存器GPIO控制
     * 
     * 【功能说明】
     *   从GPIO管理器获取移位寄存器控制引脚
     *   初始化移位寄存器为默认状态
     * 
     * 【初始化流程】
     *   1. 获取GPIO管理器实例
     *   2. 获取DS、SRCLR、SRCLK、RCLK引脚GPIO
     *   3. 调用init()初始化移位寄存器
     */
    public ShiftRegister(){ // 构造函数，初始化移位寄存器
        HwGpioManager hwgpio = HwGpioManager.getInstance(); // 获取GPIO管理器单例
        mChModelDs = hwgpio.getGpioDev(HwGpioManager.PIN_CH_MODEL_DS); // 获取数据输入GPIO
        mChModelSrclr = hwgpio.getGpioDev(HwGpioManager.PIN_CH_MODEL_SRCLR); // 获取清零控制GPIO
        mChModelSrclk = hwgpio.getGpioDev(HwGpioManager.PIN_CH_MODEL_SRCLK); // 获取移位时钟GPIO
        mChModelRclk = hwgpio.getGpioDev(HwGpioManager.PIN_CH_MODEL_RCLK); // 获取锁存时钟GPIO
        init(); // 初始化移位寄存器
    } // 构造函数结束

    // ==================== 数据写入方法 ====================

    /**
     * 设置移位寄存器输出值
     * 
     * 【功能说明】
     *   将64位数据串行移入移位寄存器链
     *   通过GPIO模拟SPI时序实现数据传输
     * 
     * 【参数说明】
     *   @param val  要写入的64位数据值
     *   @param bits 数据位数（通常为64位，对应4片74HC595级联）
     * 
     * 【时序流程】
     *   1. 清零移位寄存器（SRCLR拉低再拉高）
     *   2. 准备时钟信号（SRCLK和RCLK拉低）
     *   3. 逐位移入数据（低位优先）
     *      - 设置DS数据
     *      - SRCLK上升沿移位
     *   4. 锁存输出（RCLK上升沿）
     * 
     * 【数据格式】
     *   - 低位先移入，高位后移入
     *   - 使用无符号右移(>>>)确保正确处理高位
     */
    public void setVal(long val,int bits){ // 设置移位寄存器输出值
        Log.d("ShiftRegister", "setVal() called with: val = [" + Long.toHexString(val) + "], bits = [" + bits + "]"); // 调试日志：打印设置值
        mChModelSrclr.setVal(GpioDev.GPIO_VAL_LOW); // 拉低清零信号，清空移位寄存器
        mChModelSrclr.setVal(GpioDev.GPIO_VAL_HIGH); // 拉高清零信号，恢复正常工作模式
        mChModelSrclk.setVal(GpioDev.GPIO_VAL_LOW); // 拉低移位时钟，准备产生上升沿
        mChModelRclk.setVal(GpioDev.GPIO_VAL_LOW); // 拉低锁存时钟，准备产生上升沿

        for(int i=0;i<bits;i++){ // 循环移入每一位数据
            mChModelDs.setVal(((val>>>i) & 0x01) == 0 ? GpioDev.GPIO_VAL_LOW : GpioDev.GPIO_VAL_HIGH); // 设置数据位（低位优先，使用无符号右移）
            mChModelSrclk.setVal(GpioDev.GPIO_VAL_HIGH); // 拉高移位时钟，产生上升沿，数据移入
            mChModelSrclk.setVal(GpioDev.GPIO_VAL_LOW); // 拉低移位时钟，为下一次移位做准备
        } // 移位循环结束
        mChModelRclk.setVal(GpioDev.GPIO_VAL_HIGH); // 拉高锁存时钟，产生上升沿，锁存数据到输出
        mChModelRclk.setVal(GpioDev.GPIO_VAL_LOW); // 拉低锁存时钟，恢复初始状态
    } // setVal方法结束

    // ==================== 初始化方法 ====================

    /**
     * 初始化移位寄存器
     * 
     * 【功能说明】
     *   将移位寄存器初始化为全1状态
     *   用于设置通道模式的默认状态
     * 
     * 【初始值说明】
     *   0xFFFFFFFFFFFFFFFFL = 64位全1
     *   通常表示所有通道处于默认/安全模式
     */
    private void init(){ // 初始化移位寄存器（私有方法）
        setVal(0xFFFFFFFFFFFFFFFFL,64); // 设置64位全1，初始化为默认状态
    } // init方法结束
} // ShiftRegister类结束
