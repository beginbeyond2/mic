package com.micsig.tbook.hardware; // 硬件层包名，包含示波器硬件相关操作类

import android.util.Log; // 导入Android日志工具类，用于调试输出

import java.lang.reflect.Array; // 导入反射数组工具类（本类未使用）
import java.nio.ByteBuffer; // 导入字节缓冲区类，用于SPI数据传输
import java.util.Arrays; // 导入数组工具类（本类未使用）

/**
 * ┌─────────────────────────────────────────────────────────────────────────────────────────┐
 * │                                    HwLoadFpgaCode                                       │
 * │                              FPGA固件代码加载控制器                                        │
 * ├─────────────────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                             │
 * │   硬件抽象层 - FPGA配置模块，负责FPGA芯片的固件加载和电源管理                                │
 * │                                                                                         │
 * │ 【核心职责】                                                                             │
 * │   1. FPGA电源控制：管理多路电源轨的开关时序（1.0V/1.2V/1.8V/3.3V）                          │
 * │   2. 固件加载：通过SPI接口将配置数据写入FPGA，实现被动串行(PS)配置模式                       │
 * │   3. 状态监控：检测nSTATUS和CONFIG_DONE信号，验证配置过程正确性                             │
 * │   4. 复位控制：提供FPGA复位功能，确保芯片正常工作                                           │
 * │                                                                                         │
 * │ 【架构设计】                                                                             │
 * │   ┌─────────────┐     SPI数据      ┌─────────────┐                                      │
 * │   │ SpiDev      │ ───────────────> │   FPGA      │                                      │
 * │   │ (SPI接口)   │                  │  (配置目标)  │                                      │
 * │   └─────────────┘                  └─────────────┘                                      │
 * │          ▲                               ▲                                              │
 * │          │ GPIO控制                      │ 状态反馈                                     │
 * │   ┌──────┴──────────────────────────────┴──────┐                                       │
 * │   │              HwLoadFpgaCode                │                                       │
 * │   │  ┌─────────────────────────────────────┐   │                                       │
 * │   │  │ GPIO控制: nCONFIG, nSTATUS,         │   │                                       │
 * │   │  │           CONFIG_DONE, FPGA_RST     │   │                                       │
 * │   │  │ 电源控制: VCC1.0/1.2/1.8/3.3V       │   │                                       │
 * │   │  └─────────────────────────────────────┘   │                                       │
 * │   └───────────────────────────────────────────┘                                       │
 * │                                                                                         │
 * │ 【数据流向】                                                                             │
 * │   固件文件(byte[]) → SPI分块传输 → FPGA配置存储器 → CONFIG_DONE确认                       │
 * │                                                                                         │
 * │ 【依赖关系】                                                                             │
 * │   - SpiDevManager: 获取SPI设备实例                                                       │
 * │   - HwGpioManager: 获取GPIO控制实例                                                      │
 * │   - SpiDev: SPI通信接口                                                                 │
 * │   - GpioDev: GPIO控制接口                                                               │
 * │                                                                                         │
 * │ 【使用示例】                                                                             │
 * │   HwLoadFpgaCode loader = new HwLoadFpgaCode();                                        │
 * │   loader.powerOn();                        // 开启FPGA电源                              │
 * │   byte[] fpgaCode = loadFromFile("fpga.rbf"); // 加载固件文件                           │
 * │   boolean success = loader.LoadFpgaCode(fpgaCode); // 加载固件到FPGA                    │
 * │   if (success) {                                                                        │
 * │       loader.rstFpga();  // 复位FPGA                                                    │
 * │   } else {                                                                              │
 * │       loader.powerOff(); // 加载失败，关闭电源                                           │
 * │   }                                                                                     │
 * │                                                                                         │
 * │ 【配置时序】                                                                             │
 * │   1. nCONFIG拉低 → nSTATUS应变为低（FPGA响应）                                           │
 * │   2. nCONFIG拉高 → nSTATUS应变为高（FPGA准备就绪）                                        │
 * │   3. 发送配置数据 → DATA[7:0] + DCLK                                                     │
 * │   4. CONFIG_DONE变高 → 配置成功                                                         │
 * │                                                                                         │
 * │ 【注意事项】                                                                             │
 * │   - 电源开启顺序：先低电压后高电压（1.0V→1.2V→1.8V→3.3V）                                 │
 * │   - 电源关闭顺序：先高电压后低电压（3.3V→1.8V→1.2V→1.0V）                                 │
 * │   - 配置失败会自动重试，最多300000次                                                      │
 * │                                                                                         │
 * │ 【作者】zhuzh                                                                            │
 * │ 【日期】2018/3/9                                                                         │
 * └─────────────────────────────────────────────────────────────────────────────────────────┘
 */

public class HwLoadFpgaCode {

    // ==================== 常量定义 ====================

    /**
     * 日志标签，用于标识本类的日志输出
     */
    private static final String TAG = "HwLoadFpgaCode"; // 日志TAG，便于日志过滤和调试

    /**
     * SPI单次传输的最大FPGA代码字节数
     * 限制为15872字节，避免缓冲区溢出
     */
    private final static int SPI_MAX_FPGA_CODE = 15872; // SPI单次最大传输字节数，约15.5KB

    // ==================== SPI通信相关 ====================

    /**
     * SPI设备实例，用于与FPGA进行SPI通信
     * 通过SpiDevManager获取，用于传输FPGA配置数据
     */
    private SpiDev mDev; // SPI设备对象，用于FPGA配置数据传输

    // ==================== FPGA配置控制GPIO ====================

    /**
     * FPGA配置启动信号（低电平有效）
     * 拉低启动配置过程，拉高后开始数据传输
     */
    private GpioDev gpio_nConfig; // nCONFIG信号，控制FPGA配置启动

    /**
     * FPGA状态信号（低电平有效）
     * 配置过程中出错时拉低，正常时为高
     */
    private GpioDev gpio_nStatus; // nSTATUS信号，指示FPGA配置状态

    /**
     * FPGA配置完成信号（高电平有效）
     * 配置成功完成后变高，用于验证配置结果
     */
    private GpioDev gpio_ConfigDone; // CONFIG_DONE信号，指示配置完成

    /**
     * FPGA复位信号（低电平有效）
     * 用于复位FPGA芯片，使其重新初始化
     */
    private GpioDev gpio_FpgaRst; // FPGA复位控制GPIO

    // ==================== FPGA电源控制GPIO ====================

    /**
     * FPGA核心电源1.0V控制GPIO
     * 控制FPGA核心电压的开关
     */
    private GpioDev gpio_vcc1v0; // 1.0V电源控制，FPGA核心电压

    /**
     * FPGA辅助电源1.2V控制GPIO
     * 控制FPGA辅助电压的开关
     */
    private GpioDev gpio_vcc1v2; // 1.2V电源控制，FPGA辅助电压

    /**
     * FPGA I/O电源1.8V控制GPIO
     * 控制FPGA I/O bank电压的开关
     */
    private GpioDev gpio_vcc1v8; // 1.8V电源控制，FPGA I/O电压

    /**
     * FPGA I/O电源3.3V控制GPIO
     * 控制FPGA I/O bank电压的开关
     */
    private GpioDev gpio_vcc3v3; // 3.3V电源控制，FPGA I/O电压

    /**
     * FPGA外部扩展电源3.3V控制GPIO
     * 控制FPGA外部扩展接口电压的开关
     */
    private GpioDev gpio_vcc3v3Ext; // 3.3V外部扩展电源控制

    // ==================== 构造方法 ====================

    /**
     * 构造函数 - 初始化SPI设备和GPIO控制
     * 
     * 【功能说明】
     *   从管理器获取SPI设备和GPIO实例，为FPGA配置做准备
     * 
     * 【初始化流程】
     *   1. 获取FPGA启动用的SPI设备
     *   2. 获取配置控制GPIO（nCONFIG, nSTATUS, CONFIG_DONE, FPGA_RST）
     *   3. 获取电源控制GPIO（1.0V, 1.2V, 1.8V, 3.3V, 3.3V_EXT）
     */
    public HwLoadFpgaCode(){ // 构造函数，初始化硬件资源
        mDev = SpiDevManager.getInstance().getSpiDev(SpiDevManager.SPI_DEV_FPGA_BOOT); // 获取FPGA启动SPI设备
        HwGpioManager hwGpio = HwGpioManager.getInstance(); // 获取GPIO管理器单例
        gpio_nConfig = hwGpio.getGpioDev(HwGpioManager.PIN_FPGA_nCONFIG); // 获取nCONFIG引脚GPIO
        gpio_nStatus = hwGpio.getGpioDev(HwGpioManager.PIN_FPGA_nSTATUS); // 获取nSTATUS引脚GPIO
        gpio_ConfigDone = hwGpio.getGpioDev(HwGpioManager.PIN_FPGA_CONFIG_DONE); // 获取CONFIG_DONE引脚GPIO
        gpio_FpgaRst = hwGpio.getGpioDev(HwGpioManager.PIN_FPGA_nRST); // 获取FPGA复位引脚GPIO

        gpio_vcc1v0 = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC1V0); // 获取1.0V电源控制GPIO
        gpio_vcc1v2 = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC1V2); // 获取1.2V电源控制GPIO
        gpio_vcc1v8 = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC1V8); // 获取1.8V电源控制GPIO
        gpio_vcc3v3 = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC3V3); // 获取3.3V电源控制GPIO
        gpio_vcc3v3Ext = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC3V3_EXT); // 获取3.3V扩展电源控制GPIO

    }

    // ==================== 电源控制方法 ====================

    /**
     * 开启FPGA电源
     * 
     * 【功能说明】
     *   按照正确的时序开启FPGA的所有电源轨
     *   电源开启顺序：低电压到高电压（1.0V → 1.2V → 1.8V → 3.3V → 3.3V_EXT）
     * 
     * 【注意事项】
     *   - 必须按照电压从低到高的顺序开启，避免损坏FPGA
     *   - 实际应用中可能需要在各路电源间添加延时
     */
    public void powerOn(){ // 开启FPGA所有电源
        gpio_vcc1v0.setVal(GpioDev.GPIO_VAL_HIGH); // 开启1.0V核心电源
        gpio_vcc1v2.setVal(GpioDev.GPIO_VAL_HIGH); // 开启1.2V辅助电源
        gpio_vcc1v8.setVal(GpioDev.GPIO_VAL_HIGH); // 开启1.8V I/O电源
        gpio_vcc3v3.setVal(GpioDev.GPIO_VAL_HIGH); // 开启3.3V I/O电源
        gpio_vcc3v3Ext.setVal(GpioDev.GPIO_VAL_HIGH); // 开启3.3V扩展电源
    } // 电源开启完成

    /**
     * 关闭FPGA电源
     * 
     * 【功能说明】
     *   按照正确的时序关闭FPGA的所有电源轨
     *   电源关闭顺序：高电压到低电压（3.3V_EXT → 3.3V → 1.8V → 1.2V → 1.0V）
     * 
     * 【注意事项】
     *   - 必须按照电压从高到低的顺序关闭，避免损坏FPGA
     *   - 与开启顺序相反，确保安全断电
     */
    public void powerOff(){ // 关闭FPGA所有电源
        gpio_vcc3v3Ext.setVal(GpioDev.GPIO_VAL_LOW); // 关闭3.3V扩展电源
        gpio_vcc3v3.setVal(GpioDev.GPIO_VAL_LOW); // 关闭3.3V I/O电源
        gpio_vcc1v8.setVal(GpioDev.GPIO_VAL_LOW); // 关闭1.8V I/O电源
        gpio_vcc1v2.setVal(GpioDev.GPIO_VAL_LOW); // 关闭1.2V辅助电源
        gpio_vcc1v0.setVal(GpioDev.GPIO_VAL_LOW); // 关闭1.0V核心电源
    } // 电源关闭完成

    // ==================== FPGA配置加载方法 ====================

    /**
     * 加载FPGA配置代码
     * 
     * 【功能说明】
     *   通过SPI接口将FPGA配置数据写入FPGA芯片
     *   实现Altera/Cyclone系列FPGA的被动串行(PS)配置模式
     * 
     * 【参数说明】
     *   @param bytes FPGA配置数据字节数组（通常为.rbf/.sof文件内容）
     * 
     * 【返回值】
     *   @return true - 配置成功；false - 配置失败
     * 
     * 【配置流程】
     *   1. 拉低nCONFIG启动配置
     *   2. 检测nSTATUS是否变低（FPGA响应）
     *   3. 拉高nCONFIG
     *   4. 检测nSTATUS是否变高（FPGA准备就绪）
     *   5. 通过SPI发送配置数据
     *   6. 检测CONFIG_DONE是否变高（配置完成）
     *   7. 复位FPGA
     * 
     * 【错误处理】
     *   - 配置失败会自动重试，最多300000次
     *   - 每次失败都会输出错误日志
     * 
     * 【时序要求】
     *   - nCONFIG低电平保持时间：> 2μs
     *   - nCONFIG变高后等待时间：> 500μs（实测需要）
     *   - 数据发送后等待时间：2ms
     */
    public boolean LoadFpgaCode(byte [] bytes){ // 加载FPGA配置代码，参数为配置数据字节数组

        int cnt = 300000; // 最大重试次数，防止无限循环
        boolean ok = false; // 配置成功标志，初始为失败
        int N = 2; // 延时系数，用于调整时序延时
//        Log.d(TAG,"len:" + bytes.length); // 调试日志：打印配置数据长度（已注释）
        do // 配置重试循环
        {

            gpio_nConfig.setVal(GpioDev.GPIO_VAL_HIGH); // 先将nCONFIG置高（确保初始状态）

            usleep(2 * N); // 延时4微秒，等待信号稳定
            gpio_nConfig.setVal(GpioDev.GPIO_VAL_LOW); // 拉低nCONFIG，启动配置过程

            usleep(10 * N ); // 延时20微秒，等待FPGA响应（要求>2us）
            if(gpio_nStatus.getVal() == GpioDev.GPIO_VAL_HIGH) // 检测nSTATUS是否为低
            {
                gpio_nConfig.setVal(GpioDev.GPIO_VAL_HIGH); // 恢复nCONFIG为高

                usleep(2000 * N); // 延时4毫秒
                Log.e(TAG,"fpga code load false,nstatus is high when nconfig go to low!"); // 错误日志：nSTATUS未响应
                continue; // 跳过本次循环，重试配置
            } // nSTATUS检测失败处理结束

            gpio_nConfig.setVal(GpioDev.GPIO_VAL_HIGH); // 拉高nCONFIG，开始数据传输阶段

            usleep(1000 * N * 5); // 延时10毫秒，等待FPGA准备就绪（要求>300us，实测需要>500us）
            if(gpio_nStatus.getVal() == GpioDev.GPIO_VAL_LOW) // 检测nSTATUS是否变高
            {
                usleep(2000 * N); // 延时4毫秒
                Log.e(TAG,"fpga code load false,nstatus is still low when nconfig go to high!"); // 错误日志：FPGA未就绪
                continue; // 跳过本次循环，重试配置
            } // nSTATUS就绪检测失败处理结束
            //Step2.配置数据 数据.低位先，高位后 // 步骤2：发送配置数据，低位优先
            //      DCLK为高前必须准备好数据 // SPI时钟上升沿前数据必须就绪
            usleep(2 * N); // 延时4微秒，等待>2us

            int wlen = 0; // 已写入字节数计数器
            int once = SPI_MAX_FPGA_CODE; // 单次传输字节数
            byte [] in = new byte[SPI_MAX_FPGA_CODE + 8]; // 发送缓冲区，额外8字节用于地址和填充
            int addr = 0; // SPI地址计数器
            int addrNBytes = mDev.getAddrNBytes(); // 获取地址字节数（用于FSPI设备）
            mDev.setAddr(addr); // 设置SPI起始地址
            while (wlen < bytes.length){ // 循环发送所有配置数据
                once = bytes.length - wlen; // 计算剩余字节数
                if(once > SPI_MAX_FPGA_CODE) { // 检查是否超过单次最大传输量
                    once = SPI_MAX_FPGA_CODE; // 限制为最大传输量
                } // 单次传输量限制处理结束
                System.arraycopy(bytes,wlen,in,addrNBytes,once); // 复制配置数据到发送缓冲区
                if((once & 0x3) != 0){ // 检查是否4字节对齐
                    once += 4 -(once & 0x3); // 填充到4字节对齐
                } // 4字节对齐处理结束
                mDev.write(ByteBuffer.wrap(in),once + addrNBytes); // 通过SPI发送数据
                wlen += once; // 更新已写入字节数
                mDev.setAddr(addr++); // 更新SPI地址
            } // 配置数据发送循环结束
            for(int i=0;i<8;i++){ // 初始化额外字节为0
                in[i] = 0; // 清零缓冲区头部
            } // 缓冲区初始化循环结束
            usleep(2000 * N); // 延时4毫秒，某些板子需要更长时间
            int nums = 0; // 额外时钟周期计数器
            while (gpio_ConfigDone.getVal() == GpioDev.GPIO_VAL_LOW && nums < 255){ // 等待CONFIG_DONE变高
                mDev.write(ByteBuffer.wrap(in),4 + addrNBytes); // 发送额外时钟周期
                mDev.setAddr(addr++); // 更新地址
                usleep(2000 * N); // 延时4毫秒
                nums++; // 增加计数
            } // 等待CONFIG_DONE循环结束

            if (gpio_ConfigDone.getVal() == GpioDev.GPIO_VAL_LOW) { // 检查CONFIG_DONE是否仍为低
                usleep(2000 * N); // 延时4毫秒
                Log.e(TAG, "fpga code load false,configDone is low when all datas is load!"); // 错误日志：配置未完成
                continue; // 跳过本次循环，重试配置
            } // CONFIG_DONE检测失败处理结束

            usleep(2000 * N); // 延时4毫秒，根据工程师建议修改为2ms
            rstFpga(); // 复位FPGA
            ok = true; // 标记配置成功

            Log.d(TAG,"fpga code load ok"); // 调试日志：配置成功
            usleep(5000); // 延时5毫秒，根据工程师建议添加
            break; // 退出重试循环
        }while(--cnt!=0); // 重试循环条件判断
        return ok; // 返回配置结果
    } // LoadFpgaCode方法结束

    // ==================== 辅助方法 ====================

    /**
     * 微秒级延时函数
     * 
     * 【功能说明】
     *   提供微秒级的精确延时，用于FPGA配置时序控制
     * 
     * 【参数说明】
     *   @param us 延时时间，单位：微秒
     * 
     * 【实现原理】
     *   使用Thread.sleep实现，将微秒转换为毫秒和纳秒
     *   Thread.sleep(ms, ns)参数：毫秒部分 + 纳秒部分
     */
    private void usleep(int us){ // 微秒级延时函数，参数为微秒数
        try { // 异常捕获块开始

            Thread.sleep(us/1000,(us % 1000)*1000); // 调用Thread.sleep，参数1为毫秒，参数2为纳秒
        } catch (InterruptedException e) { // 捕获中断异常
            e.printStackTrace(); // 打印异常堆栈
        } // 异常捕获块结束
    } // usleep方法结束

    /**
     * 复位FPGA
     * 
     * 【功能说明】
     *   通过nRST引脚复位FPGA芯片
     *   复位脉冲宽度约1毫秒
     * 
     * 【时序说明】
     *   1. nRST拉低（复位有效）
     *   2. 保持1毫秒
     *   3. nRST拉高（复位释放）
     */
    public void rstFpga(){ // 复位FPGA方法
        gpio_FpgaRst.setVal(GpioDev.GPIO_VAL_LOW); // 拉低复位信号，开始复位
        usleep(1000); // 保持复位状态1毫秒
        gpio_FpgaRst.setVal(GpioDev.GPIO_VAL_HIGH); // 拉高复位信号，释放复位
    } // rstFpga方法结束

    /**
     * 设置FPGA进入待机模式
     * 
     * 【功能说明】
     *   通过拉低nCONFIG使FPGA进入待机状态
     *   FPGA将释放配置，进入未配置状态
     */
    public void standBy(){ // FPGA待机模式设置
        gpio_nConfig.setVal(GpioDev.GPIO_VAL_LOW); // 拉低nCONFIG，FPGA进入待机
    } // standBy方法结束


} // HwLoadFpgaCode类结束
