package com.micsig.tbook.hardware; // 包声明：时钟控制类所属包路径

import java.nio.ByteBuffer; // 导入ByteBuffer类，用于SPI数据传输

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                           Clk 类说明文档                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   时钟控制类 - MHO系列示波器硬件层的时钟芯片控制组件                           │
 * │   负责PLL时钟芯片的初始化、休眠和唤醒控制                                      │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. PLL时钟芯片初始化：配置时钟芯片的寄存器参数                               │
 * │   2. 时钟休眠控制：关闭时钟芯片以节省功耗                                      │
 * │   3. 时钟唤醒控制：唤醒时钟芯片并重新初始化                                    │
 * │   4. SPI通信：通过SPI接口向时钟芯片发送配置数据                                │
 * │   5. GPIO控制：控制时钟芯片的LE（锁存使能）和CE（芯片使能）信号                │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                          Clk                                     │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    GPIO控制层                              │   │       │
 * │   │  │  mClkLE (锁存使能) │ mClkCE (芯片使能)                    │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    SPI通信层                              │   │       │
 * │   │  │  mClkDev (SPI设备) │ sendClkRegister() (发送寄存器数据)   │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    时钟控制层                              │   │       │
 * │   │  │  clk_ini() (初始化) │ sleepClk() (休眠) │ wakeUpClk()     │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【PLL时钟芯片说明】                                                          │
 * │   PLL（Phase Locked Loop）锁相环时钟芯片：                                   │
 * │   - 功能：产生高精度、高稳定性的时钟信号                                      │
 * │   - 应用：为示波器的ADC、FPGA等提供采样时钟                                  │
 * │   - 控制信号：                                                               │
 * │     * LE（Latch Enable）：锁存使能，上升沿锁存SPI数据                        │
 * │     * CE（Chip Enable）：芯片使能，高电平有效                                │
 * │   - SPI接口：3线制SPI，用于配置寄存器                                        │
 * │                                                                             │
 * │ 【初始化流程】                                                               │
 * │   1. 设置CE为高电平，使能时钟芯片                                            │
 * │   2. 发送寄存器0xC9（复位寄存器）                                            │
 * │   3. 发送寄存器0x4FF1A0（配置寄存器1）                                       │
 * │   4. 延时30ms                                                               │
 * │   5. 发送寄存器0xBB22（配置寄存器2，设置输出频率）                            │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   Clk构造函数 → clk_ini() → sendClkRegister() → SPI设备 → 时钟芯片         │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   上游依赖：HwGpioManager（GPIO管理器）、SpiDevManager（SPI设备管理器）       │
 * │   下游依赖：GpioDev（GPIO设备）、SpiDev（SPI设备）                           │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 创建时钟控制实例                                                        │
 * │   Clk clk = new Clk();                                                      │
 * │   // 休眠时钟芯片                                                            │
 * │   clk.sleepClk();                                                           │
 * │   // 唤醒时钟芯片                                                            │
 * │   clk.wakeUpClk();                                                          │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. 初始化后需要延时30ms等待时钟稳定                                        │
 * │   2. 唤醒后需要重新初始化时钟芯片                                            │
 * │   3. SPI数据为3字节，高位在前                                                │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018/3/12                                              │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 时钟控制类
 * <p>
 * 负责PLL时钟芯片的初始化、休眠和唤醒控制。
 * 通过SPI接口配置时钟芯片的寄存器参数，通过GPIO控制LE和CE信号。
 * <p>
 * 核心功能：
 * - PLL时钟芯片初始化
 * - 时钟休眠控制
 * - 时钟唤醒控制
 * - SPI通信
 * - GPIO控制
 */
public class Clk {

    /**
     * 日志标签
     * 用途：用于日志输出时的标识
     */
    private static final String TAG = "CLK";

    /**
     * 时钟锁存使能GPIO设备
     * 用途：控制PLL时钟芯片的LE（Latch Enable）信号
     * 说明：上升沿锁存SPI数据到时钟芯片寄存器
     */
    private GpioDev mClkLE;

    /**
     * 时钟芯片使能GPIO设备
     * 用途：控制PLL时钟芯片的CE（Chip Enable）信号
     * 说明：高电平使能时钟芯片，低电平关闭时钟芯片
     */
    private GpioDev mClkCE;

    /**
     * 时钟SPI设备
     * 用途：通过SPI接口向时钟芯片发送配置数据
     * 说明：3线制SPI，数据格式为3字节
     */
    private SpiDev mClkDev;

    /**
     * 构造函数
     * <p>
     * 功能：创建时钟控制实例，初始化GPIO和SPI设备，并配置时钟芯片
     * 流程：
     * 1. 从HwGpioManager获取LE和CE的GPIO设备
     * 2. 从SpiDevManager获取时钟SPI设备
     * 3. 调用clk_ini()初始化时钟芯片
     */
    public Clk(){
        HwGpioManager hwgpio = HwGpioManager.getInstance(); // 获取GPIO管理器单例
        mClkLE = hwgpio.getGpioDev(HwGpioManager.PIN_CLK_PLL_LE); // 获取时钟LE引脚的GPIO设备
        mClkCE = hwgpio.getGpioDev(HwGpioManager.PIN_CLK_PLL_CE); // 获取时钟CE引脚的GPIO设备
        mClkDev = SpiDevManager.getInstance().getSpiDev(SpiDevManager.SPI_DEV_CLK); // 获取时钟SPI设备
        clk_ini(); // 初始化时钟芯片
    }

    /**
     * 时钟初始化
     * <p>
     * 功能：配置PLL时钟芯片的寄存器参数
     * 流程：
     * 1. 设置CE为高电平，使能时钟芯片
     * 2. 发送寄存器0xC9（复位寄存器）
     * 3. 发送寄存器0x4FF1A0（配置寄存器1）
     * 4. 延时30ms
     * 5. 发送寄存器0xBB22（配置寄存器2，设置输出频率）
     */
    private void clk_ini(){
        mClkCE.setVal(GpioDev.GPIO_VAL_HIGH); // 设置CE为高电平，使能时钟芯片

        // 发送复位寄存器
        mClkLE.setVal(GpioDev.GPIO_VAL_LOW); // 设置LE为低电平，准备发送数据
        sendClkRegister(0xC9); // 发送寄存器值0xC9（复位寄存器）
        mClkLE.setVal(GpioDev.GPIO_VAL_HIGH); // 设置LE为高电平，锁存数据

        // 发送配置寄存器1
        mClkLE.setVal(GpioDev.GPIO_VAL_LOW); // 设置LE为低电平，准备发送数据
        sendClkRegister(0x4FF1A0); // 发送寄存器值0x4FF1A0（配置寄存器1）
        mClkLE.setVal(GpioDev.GPIO_VAL_HIGH); // 设置LE为高电平，锁存数据

        // 发送配置寄存器2
        mClkLE.setVal(GpioDev.GPIO_VAL_LOW); // 设置LE为低电平，准备发送数据
        msleep(30); // 延时30ms，等待时钟稳定
        sendClkRegister(0xBB22); // 发送寄存器值0xBB22（配置寄存器2，设置输出频率）
//        sendClkRegister(0x7D02); //1G // 备用配置：1GHz输出频率
        mClkLE.setVal(GpioDev.GPIO_VAL_HIGH); // 设置LE为高电平，锁存数据

        mClkLE.setVal(GpioDev.GPIO_VAL_LOW); // 设置LE为低电平，完成初始化
    }

    /**
     * 发送时钟寄存器数据
     * <p>
     * 功能：通过SPI接口向时钟芯片发送3字节的寄存器配置数据
     * 数据格式：高位在前，3字节
     * 
     * @param val 寄存器值（24位）
     */
    private void sendClkRegister(int val){
        byte [] buf = new byte[3]; // 创建3字节的缓冲区
        buf[0] = (byte)((val >>16) & 0xFF); // 提取高8位数据
        buf[1] = (byte)((val >>8) & 0xFF); // 提取中8位数据
        buf[2] = (byte)(val & 0xFF); // 提取低8位数据

        mClkDev.write(ByteBuffer.wrap(buf),3); // 通过SPI发送3字节数据
    }

    /**
     * 毫秒级延时
     * <p>
     * 功能：使当前线程休眠指定的毫秒数
     * 用途：在时钟初始化过程中提供必要的延时
     * 
     * @param ms 延时时间（单位：毫秒）
     */
    private void msleep(long ms){
        try {
            Thread.sleep(ms); // 使当前线程休眠指定的毫秒数
        } catch (InterruptedException e) { // 捕获中断异常
            e.printStackTrace(); // 打印异常堆栈信息
        }
    }

    /**
     * 休眠时钟芯片
     * <p>
     * 功能：关闭时钟芯片以节省功耗
     * 实现：设置CE为低电平，禁用时钟芯片
     */
    public void sleepClk(){
        mClkCE.setVal(GpioDev.GPIO_VAL_LOW); // 设置CE为低电平，关闭时钟芯片
    }

    /**
     * 唤醒时钟芯片
     * <p>
     * 功能：唤醒时钟芯片并重新初始化
     * 流程：
     * 1. 设置CE为高电平，使能时钟芯片
     * 2. 延时10ms
     * 3. 重新初始化时钟芯片
     */
    public void wakeUpClk(){
        mClkCE.setVal(GpioDev.GPIO_VAL_HIGH); // 设置CE为高电平，使能时钟芯片
        msleep(10); // 延时10ms，等待时钟芯片稳定
        clk_ini(); // 重新初始化时钟芯片
    }
}
