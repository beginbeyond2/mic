package com.micsig.tbook.hardware; // 包声明：硬件管理类所属包路径

import android.content.Context; // 导入Android上下文类
import android.net.INetdEventCallback; // 导入网络事件回调接口
import android.util.Log; // 导入Android日志类

import java.nio.ByteBuffer; // 导入ByteBuffer类，用于SPI数据传输

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                         Hardware 类说明文档                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   硬件管理类 - MHO系列示波器硬件层的核心管理组件                              │
 * │   采用单例模式，负责示波器所有硬件资源的初始化和管理                            │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 硬件初始化：初始化GPIO、SPI、FPGA、时钟等硬件资源                        │
 * │   2. 电源管理：控制模拟电源、ADC电源、面板电源、探头电源的开关                 │
 * │   3. FPGA管理：加载FPGA固件、控制FPGA休眠和唤醒、发送FPGA命令                 │
 * │   4. E2PROM操作：读写E2PROM存储器                                           │
 * │   5. 温度管理：获取和设置系统温度、控制风扇转速                               │
 * │   6. 硬件信息：获取硬件版本、系统版本、机器UUID等                             │
 * │   7. 探头管理：获取探头衰减比例、控制探头IO                                   │
 * │   8. 触发和时钟控制：控制触发IO和10MHz时钟IO                                 │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                         Hardware                                 │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    硬件管理器层                            │   │       │
 * │   │  │  HwManager │ HwGpioManager │ SpiDevManager │             │   │       │
 * │   │  │  HwLoadFpgaCode │ ShiftRegister │ Clk                     │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    GPIO控制层                              │   │       │
 * │   │  │  fpgaSuspendIo │ oeIo │ powerIo │ powerAdc │ powerPanel  │   │       │
 * │   │  │  powerProbe │ probeIo │ triggerInOut │ clkInOut          │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    SPI通信层                              │   │       │
 * │   │  │  fpgaSpiDev[0] (FPGA1) │ fpgaSpiDev[1] (FPGA2)           │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    电源管理层                              │   │       │
 * │   │  │  powerAnalogOn() │ powerAnalogOff() │ standby() │ resume()│   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【单例模式】                                                                 │
 * │   采用双重检查锁定（Double-Check Locking）实现线程安全的单例模式              │
 * │   - volatile关键字：保证instance变量的可见性                                 │
 * │   - synchronized块：保证线程安全                                            │
 * │   - 双重检查：避免不必要的同步开销                                           │
 * │                                                                             │
 * │ 【电源管理说明】                                                             │
 * │   1. 模拟电源：为模拟电路供电（ADC、前端放大器等）                            │
 * │   2. ADC电源：为ADC芯片供电                                                 │
 * │   3. 面板电源：为前面板按键、旋钮等供电                                      │
 * │   4. 探头电源：为有源探头供电                                               │
 * │   5. FPGA电源：为FPGA芯片供电                                               │
 * │                                                                             │
 * │ 【FPGA管理说明】                                                             │
 * │   1. 加载固件：通过SPI接口加载FPGA比特流文件                                 │
 * │   2. 休眠控制：通过fpgaSuspendIo控制FPGA进入低功耗模式                       │
 * │   3. 命令发送：通过SPI接口向FPGA发送控制命令                                 │
 * │   4. 数据接收：通过SPI接口从FPGA读取数据                                    │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   应用层 → Hardware → HwManager/HwGpioManager/SpiDevManager → 硬件设备     │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   上游依赖：Context（Android上下文）                                        │
 * │   下游依赖：HwManager、HwGpioManager、SpiDevManager、HwLoadFpgaCode、        │
 * │            ShiftRegister、Clk、GpioDev、SpiDev                              │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 获取Hardware单例                                                        │
 * │   Hardware hardware = Hardware.getInstance(context);                        │
 * │   // 加载FPGA固件                                                            │
 * │   hardware.loadFpgaCode(fpgaBytes);                                         │
 * │   // 打开模拟电源                                                            │
 * │   hardware.powerAnalogOn();                                                 │
 * │   // 发送FPGA命令                                                            │
 * │   hardware.sendFpgaCmd(0, byteBuffer, length);                              │
 * │   // 获取硬件版本                                                            │
 * │   int version = hardware.getHwVersion();                                    │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. Hardware是单例类，全局只有一个实例                                      │
 * │   2. 初始化时会自动打开模拟电源和唤醒FPGA                                    │
 * │   3. 加载FPGA固件时需要先打开探头电源                                        │
 * │   4. 发送FPGA命令使用synchronized保证线程安全                                │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018/3/9                                               │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 硬件管理类
 * <p>
 * 采用单例模式，负责示波器所有硬件资源的初始化和管理。
 * 包括GPIO、SPI、FPGA、时钟、电源、E2PROM、温度等硬件资源。
 * <p>
 * 核心功能：
 * - 硬件初始化
 * - 电源管理
 * - FPGA管理
 * - E2PROM操作
 * - 温度管理
 * - 硬件信息获取
 * - 探头管理
 * - 触发和时钟控制
 */
public class Hardware {
    /**
     * 日志标签
     * 用途：用于日志输出时的标识
     */
    private static final String TAG = "Hardware";

    /**
     * Android上下文
     * 用途：访问Android系统服务
     */
    private Context mContext;

    /**
     * FPGA SPI设备数组
     * 长度：2（FPGA1和FPGA2）
     * 用途：通过SPI接口与FPGA通信
     */
    private SpiDev [] fpgaSpiDev = new SpiDev[2];

    /**
     * FPGA休眠控制GPIO
     * 用途：控制FPGA进入和退出休眠模式
     * 高电平：休眠
     * 低电平：唤醒
     */
    private GpioDev fpgaSuspendIo;

    /**
     * 输出使能GPIO
     * 用途：控制模拟电路输出使能
     * 低电平：使能输出
     * 高电平：禁用输出
     */
    private GpioDev oeIo;

    /**
     * 模拟电源控制GPIO
     * 用途：控制模拟电路电源开关
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    private GpioDev powerIo;

    /**
     * 模拟电源控制GPIO（5-8通道）
     * 用途：控制5-8通道模拟电路电源开关
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    private GpioDev powerIo2;

    /**
     * ADC 2.5V电源控制GPIO
     * 用途：控制ADC 2.5V电源开关
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    private GpioDev powerAdc2v5;

    /**
     * ADC电源控制GPIO
     * 用途：控制ADC电源开关
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    private GpioDev powerAdc;

    /**
     * 面板电源控制GPIO
     * 用途：控制前面板电源开关
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    private GpioDev powerPanel;

    /**
     * 面板电源控制GPIO（5-8通道）
     * 用途：控制5-8通道前面板电源开关
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    private GpioDev powerPanel2;

    /**
     * 探头电源控制GPIO
     * 用途：控制有源探头电源开关
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    private GpioDev powerProbe;

    /**
     * 探头电源控制GPIO（5-8通道）
     * 用途：控制5-8通道有源探头电源开关
     * 高电平：打开电源
     * 低电平：关闭电源
     */
    private GpioDev powerProbe2;

    /**
     * 探头IO控制GPIO
     * 用途：控制探头IO信号
     */
    private GpioDev probeIo;

    /**
     * 探头IO控制GPIO（5-8通道）
     * 用途：控制5-8通道探头IO信号
     */
    private GpioDev probeIo2;

    /**
     * 触发输入输出控制GPIO
     * 用途：控制触发信号的输入输出方向
     * 高电平：输入
     * 低电平：输出
     */
    private GpioDev triggerInOut;

    /**
     * 10MHz时钟输入输出控制GPIO
     * 用途：控制10MHz时钟信号的输入输出方向
     * 高电平：输出
     * 低电平：输入
     */
    private GpioDev clkInOut;

//    private GpioDev gpio_50VPwr; // 50V电源控制GPIO（已注释）

    /**
     * SPI设备管理器
     * 用途：管理所有SPI设备
     */
    private SpiDevManager mSpiDevManager;

    /**
     * 硬件管理器
     * 用途：管理硬件信息、E2PROM、温度等
     */
    private HwManager mHwManager;

    /**
     * FPGA固件加载器
     * 用途：加载FPGA比特流文件
     */
    private HwLoadFpgaCode mHwLoadFpgaCode;

    /**
     * GPIO管理器
     * 用途：管理所有GPIO设备
     */
    private HwGpioManager mHwGpioManager;

    /**
     * 移位寄存器
     * 用途：通过移位寄存器控制多路GPIO信号
     */
    private ShiftRegister mShiftRegister;

    /**
     * 时钟控制器
     * 用途：控制PLL时钟芯片
     */
    private Clk mClk;

    /**
     * Hardware单例实例
     * volatile关键字：保证多线程环境下的可见性
     */
    private static volatile Hardware instance = null;

    /**
     * 获取Hardware单例实例（无参数）
     * <p>
     * 功能：返回已创建的Hardware实例
     * 说明：如果实例未创建，返回null
     * 
     * @return Hardware实例，如果未创建则返回null
     */
    public static Hardware getInstance(){
        return instance; // 返回单例实例
    }

    /**
     * 获取Hardware单例实例（带Context参数）
     * <p>
     * 功能：创建或返回Hardware单例实例
     * 实现：双重检查锁定（Double-Check Locking）保证线程安全
     * 
     * @param context Android上下文
     * @return Hardware实例
     */
    public static Hardware getInstance(Context context) {
        // 第一次检查：避免不必要的同步
        if (instance == null) {
            synchronized (Hardware.class) { // 同步锁
                // 第二次检查：确保只创建一个实例
                if (instance == null && context != null) {
                    instance = new Hardware(context); // 创建单例实例
                }
            }
        }
        return instance; // 返回单例实例
    }

    /**
     * 私有构造函数
     * <p>
     * 功能：创建Hardware实例，初始化所有硬件资源
     * 说明：私有构造函数，防止外部直接创建实例
     * 
     * @param context Android上下文
     */
    private Hardware(Context context){
        mContext = context; // 保存Android上下文
        initHardware(); // 初始化硬件资源
    }

    /**
     * 读取E2PROM
     * <p>
     * 功能：从E2PROM指定地址读取数据
     * 
     * @param addr E2PROM地址
     * @param byteArray 数据缓冲区
     * @return 读取的字节数
     */
    public int readE2PROM(int addr, byte[] byteArray)
    {
        return  mHwManager.readE2PROM(addr,byteArray); // 调用HwManager读取E2PROM
    }

    /**
     * 写入E2PROM
     * <p>
     * 功能：向E2PROM指定地址写入数据
     * 
     * @param addr E2PROM地址
     * @param byteArray 要写入的数据
     * @return 写入的字节数
     */
    public int writeE2PROM(int addr,byte[] byteArray)
    {
        int len = 0; // 初始化写入长度
        len = mHwManager.writeE2PROM(addr,byteArray); // 调用HwManager写入E2PROM
        return len; // 返回写入的字节数
    }

    /**
     * 获取系统ID
     * <p>
     * 功能：获取系统唯一标识符
     * 
     * @return 系统ID字符串
     */
    public String getSysId(){
        return mHwManager.getSysId(); // 调用HwManager获取系统ID
    }

    /**
     * 设置FPGA DNA
     * <p>
     * 功能：设置FPGA的DNA（设备唯一标识符）
     * 
     * @param dna FPGA DNA字符串
     */
    public void setFpgaDna(String dna)
    {
        mHwManager.setFpgaDna(dna); // 调用HwManager设置FPGA DNA
    }

    /**
     * 进入待机模式
     * <p>
     * 功能：关闭模拟电源，进入低功耗待机模式
     */
    public void standby(){
        powerAnalogOff(); // 关闭模拟电源
    }

    /**
     * 从待机模式恢复
     * <p>
     * 功能：打开模拟电源，从待机模式恢复
     */
    public void resume(){
        powerAnalogOn(); // 打开模拟电源
    }

    /**
     * 获取系统温度
     * <p>
     * 功能：获取示波器内部温度
     * 
     * @return 温度值（单位：摄氏度）
     */
    public int getTemperature(){
        return mHwManager.getTemperature(); // 调用HwManager获取温度
    }

    /**
     * 获取CPU温度
     * <p>
     * 功能：获取CPU温度
     * 
     * @return 温度值（单位：摄氏度）
     */
    public int getCpuTemperature(){
        return mHwManager.getCpuTemperature(); // 调用HwManager获取CPU温度
    }

    /**
     * 设置温度
     * <p>
     * 功能：设置温度值（用于温度校准）
     * 
     * @param val 温度值
     */
    public void setTemperature(int val){
        mHwManager.setTemperature(val); // 调用HwManager设置温度
    }

    /**
     * 设置风扇转速
     * <p>
     * 功能：根据温度控制风扇转速
     * 
     * @param val 风扇转速值
     */
    public void setFanSpeed(int val){
        mHwManager.setFanSpeed(val); // 调用HwManager设置风扇转速
    }

    /**
     * 获取硬件版本
     * <p>
     * 功能：获取硬件版本号
     * 
     * @return 硬件版本号
     */
    public int getHwVersion(){
        return mHwManager.getHwVersion(); // 调用HwManager获取硬件版本
    }

    /**
     * 获取系统版本
     * <p>
     * 功能：获取系统版本号
     * 
     * @return 系统版本号
     */
    public int getSysVersion(){ return mHwManager.sysVersion(); } // 调用HwManager获取系统版本

    /**
     * 探头IO控制
     * <p>
     * 功能：设置探头IO为高电平
     */
    public void probeIo(){
        probeIo.setVal(GpioDev.GPIO_VAL_HIGH); // 设置探头IO为高电平
    }

    /**
     * 设置USB信息
     * <p>
     * 功能：设置USB设备信息（产品、序列号、版本）
     * 
     * @param product 产品名称
     * @param serial 序列号
     * @param ver 版本号
     */
    public void setUsbInfo(String product,String serial,String ver){
        mHwManager.setUsbInfo(product,serial,ver); // 调用HwManager设置USB信息
    }

    /**
     * 获取硬件通道数量
     * <p>
     * 功能：获取示波器的通道数量
     * 说明：固定返回8通道
     * 
     * @return 通道数量（8）
     */
    public int getChNum(){
        return 8; // 返回通道数量（固定为8通道）
    }

    /**
     * 判断是否使用ADC B12DJ3200NBB
     * <p>
     * 功能：判断硬件版本中ADC型号是否为B12DJ3200NBB
     * 说明：通过硬件版本最低位判断
     * 
     * @return true-使用B12DJ3200NBB，false-使用其他型号
     */
    public boolean isAdcB12DJ3200NBB(){
        return (mHwManager.getHwVersion() & 0x01) != 0; // 判断硬件版本最低位
    }

//    public boolean isDac80501(){
//        return true;
//    }

    /**
     * 获取机器UUID
     * <p>
     * 功能：获取机器唯一标识符
     * 
     * @return 机器UUID字符串
     */
    public String getMachineUUID(){
        return mHwManager.getMachineUUID(); // 调用HwManager获取机器UUID
    }

    /**
     * 加载FPGA固件
     * <p>
     * 功能：通过SPI接口加载FPGA比特流文件
     * 流程：
     * 1. 打开探头电源
     * 2. 加载FPGA固件
     * 3. 关闭探头电源
     * 
     * @param bytes FPGA比特流数据
     * @return true-加载成功，false-加载失败
     */
    public boolean loadFpgaCode(byte [] bytes){
        powerProbe.setVal(GpioDev.GPIO_VAL_HIGH); // 打开探头电源
        if(powerProbe2 != null){ // 检查5-8通道探头电源是否存在
            powerProbe2.setVal(GpioDev.GPIO_VAL_HIGH); // 打开5-8通道探头电源
        }
        boolean bRet = mHwLoadFpgaCode.LoadFpgaCode(bytes); // 加载FPGA固件
        powerProbe.setVal(GpioDev.GPIO_VAL_LOW); // 关闭探头电源
        if(powerProbe2 != null){ // 检查5-8通道探头电源是否存在
            powerProbe2.setVal(GpioDev.GPIO_VAL_LOW); // 关闭5-8通道探头电源
        }
        return bRet; // 返回加载结果
    }

    /**
     * 向FPGA发送命令
     * <p>
     * 功能：通过SPI接口向指定FPGA发送命令数据
     * 线程安全：使用synchronized保证线程安全
     * 
     * @param idx FPGA索引（0-FPGA1，1-FPGA2）
     * @param byteBuffer 命令数据缓冲区
     * @param length 数据长度
     * @return true-发送成功，false-发送失败
     */
    public synchronized boolean sendFpgaCmd(int idx,ByteBuffer byteBuffer,int length){
        if(fpgaSpiDev[idx] != null) { // 检查FPGA SPI设备是否存在
            fpgaSpiDev[idx].write(byteBuffer, 1); // 发送第一个字节（命令码）
            fpgaSpiDev[idx].write(byteBuffer, length); // 发送剩余数据
            return true; // 发送成功
        }
        return false; // FPGA SPI设备不存在，发送失败
    }

    /**
     * 从FPGA接收数据
     * <p>
     * 功能：通过SPI接口从指定FPGA读取数据
     * 线程安全：使用synchronized保证线程安全
     * 说明：读取时临时提高SPI速度，读取后恢复原速度
     * 
     * @param idx FPGA索引（0-FPGA1，1-FPGA2）
     * @param wbyteBuffer 写入数据缓冲区
     * @param length 数据长度
     * @param byteBuffer 读取数据缓冲区
     */
    public synchronized void recvFpgaCmd(int idx,ByteBuffer wbyteBuffer,int length,ByteBuffer byteBuffer){
        if(fpgaSpiDev[idx] != null) { // 检查FPGA SPI设备是否存在
            int s = fpgaSpiDev[idx].getSpeed(); // 获取当前SPI速度
            fpgaSpiDev[idx].setSpeed(15 * 1000 * 1000); // 设置SPI速度为15MHz
            fpgaSpiDev[idx].read(wbyteBuffer, length, byteBuffer); // 读取数据
            fpgaSpiDev[idx].setSpeed(s); // 恢复原SPI速度
        }
    }

    /**
     * 发送移位寄存器数据
     * <p>
     * 功能：通过移位寄存器控制多路GPIO信号
     * 
     * @param val 移位寄存器值
     * @param bits 数据位数
     */
    public void sendShiftRegister(long val,int bits){
        Log.d("MHO38V1", "sendShiftRegister() called with: val = [" + Long.toHexString(val) + "], bits = [" + bits + "]"); // 打印日志
        mShiftRegister.setVal(val,bits); // 调用移位寄存器设置值
    }

    /**
     * 复位FPGA
     * <p>
     * 功能：复位FPGA芯片
     */
    public void rstFpga(){
        mHwLoadFpgaCode.rstFpga(); // 调用FPGA加载器复位FPGA
    }

    /**
     * 休眠FPGA
     * <p>
     * 功能：使FPGA进入低功耗休眠模式
     */
    public void sleepFpga(){
        fpgaSuspendIo.setVal(GpioDev.GPIO_VAL_HIGH); // 设置休眠引脚为高电平
    }

    /**
     * 唤醒FPGA
     * <p>
     * 功能：使FPGA从休眠模式唤醒
     */
    public void wakeupFpga(){
        if(fpgaSuspendIo != null) { // 检查休眠引脚是否存在
            fpgaSuspendIo.setVal(GpioDev.GPIO_VAL_LOW); // 设置休眠引脚为低电平
        }
    }

    /**
     * 休眠时钟
     * <p>
     * 功能：使时钟芯片进入低功耗休眠模式
     * 说明：已注释，暂未使用
     */
    public void sleepClk(){
        //mClk.sleepClk(); // 调用时钟控制器休眠时钟（已注释）
    }

    /**
     * 唤醒时钟
     * <p>
     * 功能：使时钟芯片从休眠模式唤醒
     * 说明：已注释，暂未使用
     */
    public void wakeUpClk(){
        //mClk.wakeUpClk(); // 调用时钟控制器唤醒时钟（已注释）
    }

    /**
     * 打开FPGA电源
     * <p>
     * 功能：打开FPGA芯片电源
     */
    public void powerFpgaOn(){
        mHwLoadFpgaCode.powerOn(); // 调用FPGA加载器打开电源
    }

    /**
     * 关闭FPGA电源
     * <p>
     * 功能：关闭FPGA芯片电源
     */
    public void powerFpgaOff(){
        mHwLoadFpgaCode.powerOff(); // 调用FPGA加载器关闭电源
    }

    /**
     * 打开模拟电源
     * <p>
     * 功能：打开所有模拟电路电源
     * 流程：
     * 1. 打开FPGA电源（如果需要）
     * 2. 使能输出
     * 3. 打开模拟电源
     * 4. 打开ADC电源
     * 5. 打开面板电源
     * 
     * @param bFpga 是否同时打开FPGA电源
     */
    public void powerAnalogOn(boolean bFpga){
        if(bFpga){ // 判断是否打开FPGA电源
            powerFpgaOn(); // 打开FPGA电源
        }

        oeIo.setVal(GpioDev.GPIO_VAL_LOW); // 使能输出（低电平有效）
        powerIo.setVal(GpioDev.GPIO_VAL_HIGH); // 打开模拟电源
        if(powerIo2 != null){ // 检查5-8通道模拟电源是否存在
            probeIo2.setVal(GpioDev.GPIO_VAL_HIGH); // 打开5-8通道探头IO
        }
        if(powerAdc2v5 != null){ // 检查ADC 2.5V电源是否存在
            powerAdc2v5.setVal(GpioDev.GPIO_VAL_HIGH); // 打开ADC 2.5V电源
        }
        powerAdc.setVal(GpioDev.GPIO_VAL_HIGH); // 打开ADC电源

        powerPanel.setVal(GpioDev.GPIO_VAL_HIGH); // 打开面板电源
        if(powerPanel2 != null){ // 检查5-8通道面板电源是否存在
            powerPanel2.setVal(GpioDev.GPIO_VAL_HIGH); // 打开5-8通道面板电源
        }
    }

    /**
     * 打开模拟电源
     * <p>
     * 功能：打开所有模拟电路电源（包括FPGA电源）
     */
    public void powerAnalogOn(){
        powerAnalogOn(true); // 调用带参数的方法，默认打开FPGA电源
    }

    /**
     * 关闭模拟电源
     * <p>
     * 功能：关闭所有模拟电路电源
     * 流程：
     * 1. FPGA进入待机模式
     * 2. 关闭面板电源
     * 3. 关闭ADC电源
     * 4. 关闭模拟电源
     * 5. 禁用输出
     * 6. 关闭FPGA电源（如果需要）
     * 
     * @param bFpga 是否同时关闭FPGA电源
     */
    public void powerAnalogOff(boolean bFpga) {
        mHwLoadFpgaCode.standBy(); // FPGA进入待机模式
        powerPanel.setVal(GpioDev.GPIO_VAL_LOW); // 关闭面板电源
        if(powerPanel2 != null){ // 检查5-8通道面板电源是否存在
            powerPanel2.setVal(GpioDev.GPIO_VAL_LOW); // 关闭5-8通道面板电源
        }
        if(powerAdc2v5 != null){ // 检查ADC 2.5V电源是否存在
            powerAdc2v5.setVal(GpioDev.GPIO_VAL_LOW); // 关闭ADC 2.5V电源
        }
        powerAdc.setVal(GpioDev.GPIO_VAL_LOW); // 关闭ADC电源
        powerIo.setVal(GpioDev.GPIO_VAL_LOW); // 关闭模拟电源
        if(powerIo2 != null){ // 检查5-8通道模拟电源是否存在
            probeIo2.setVal(GpioDev.GPIO_VAL_LOW); // 关闭5-8通道探头IO
        }
        oeIo.setVal(GpioDev.GPIO_VAL_HIGH); // 禁用输出（高电平禁用）
        if(bFpga){ // 判断是否关闭FPGA电源
            mHwLoadFpgaCode.powerOff(); // 关闭FPGA电源
        }
    }

    /**
     * 关闭模拟电源
     * <p>
     * 功能：关闭所有模拟电路电源（包括FPGA电源）
     */
    public void powerAnalogOff(){
        powerAnalogOff(true); // 调用带参数的方法，默认关闭FPGA电源
    }

    /**
     * 电池保护
     * <p>
     * 功能：启用电池保护机制
     * 说明：已注释，暂未使用
     */
    public void batteryProtect(){
//        batteryIo.setVal(GpioDev.GPIO_VAL_HIGH); // 设置电池保护引脚为高电平（已注释）
    }

    /**
     * 获取通道探头衰减比例
     * <p>
     * 功能：根据探头ADC值判断探头衰减比例
     * 判断逻辑：
     * - 1050 <= val < 1340：10X衰减
     * - 840 <= val < 1050：100X衰减
     * - 其他：1X衰减
     * 
     * @param chIdx 通道索引
     * @return 探头衰减比例（1、10、100）
     */
    public double getChProbeRate(int chIdx){
        double ret = 1; // 默认衰减比例为1X
        int val = mHwManager.getChProbeVal(chIdx); // 获取探头ADC值
        if(val >= 1050 && val < 1340){ // 判断是否为10X衰减
            ret = 10; // 设置衰减比例为10X
        }else if(val >= 840 && val < 1050){ // 判断是否为100X衰减
            ret = 100; // 设置衰减比例为100X
        }
        return ret; // 返回衰减比例
    }

    /**
     * 判断是否为50欧姆阻抗
     * <p>
     * 功能：判断指定通道是否使用50欧姆阻抗
     * 说明：已注释，暂未使用，固定返回false
     * 
     * @param chIdx 通道索引
     * @return true-使用50欧姆阻抗，false-不使用
     */
    public boolean is5Oo(int chIdx){
//        if(chIdx == 0){
//            return (ch050o.getVal() == GpioDev.GPIO_VAL_HIGH);
//        }else if(chIdx == 1){
//            return (ch150o.getVal() == GpioDev.GPIO_VAL_HIGH);
//        }else{
//            return false;
//        }
        return false; // 固定返回false（已注释）
    }

    /**
     * 判断AD是否正常
     * <p>
     * 功能：判断AD转换器是否正常工作
     * 说明：已注释，暂未使用，固定返回false
     * 
     * @return true-AD正常，false-AD异常
     */
    public boolean isAdOK(){
        //return (AdState.getVal() == GpioDev.GPIO_VAL_LOW);
        return false; // 固定返回false（已注释）
    }

    /**
     * 初始化硬件
     * <p>
     * 功能：初始化所有硬件资源
     * 流程：
     * 1. 初始化硬件管理器
     * 2. 初始化SPI设备管理器
     * 3. 初始化GPIO管理器
     * 4. 初始化FPGA加载器
     * 5. 初始化移位寄存器
     * 6. 获取FPGA SPI设备
     * 7. 获取所有GPIO设备
     * 8. 打开模拟电源
     * 9. 唤醒FPGA
     */
    private void initHardware(){
        mHwManager = HwManager.getInstance(mContext); // 获取硬件管理器实例
        mSpiDevManager = SpiDevManager.getInstance(mContext); // 获取SPI设备管理器实例
        mHwGpioManager = HwGpioManager.getInstance(mContext); // 获取GPIO管理器实例
        mHwLoadFpgaCode = new HwLoadFpgaCode(); // 创建FPGA加载器实例
        mShiftRegister = new ShiftRegister(); // 创建移位寄存器实例
        fpgaSpiDev[0] = mSpiDevManager.getSpiDev(SpiDevManager.SPI_DEV_FPGA1_CMD); // 获取FPGA1 SPI设备
        fpgaSpiDev[1] = mSpiDevManager.getSpiDev(SpiDevManager.SPI_DEV_FPGA2_CMD); // 获取FPGA2 SPI设备
        fpgaSuspendIo = mHwGpioManager.getGpioDev(HwGpioManager.PIN_FPGA_SUSPEND); // 获取FPGA休眠引脚
        oeIo = mHwGpioManager.getGpioDev(HwGpioManager.PIN_CH_MODEL_OE); // 获取输出使能引脚
        powerIo = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER_ANALOG); // 获取模拟电源引脚
        powerIo2 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER_ANALOG58); // 获取5-8通道模拟电源引脚
        powerAdc = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER_ADC); // 获取ADC电源引脚
        powerAdc2v5 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_ADC2V5_EN); // 获取ADC 2.5V电源引脚
        powerPanel = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER_PANEL); // 获取面板电源引脚
        powerPanel2 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER58_PANEL); // 获取5-8通道面板电源引脚
        powerProbe = mHwGpioManager.getGpioDev(HwGpioManager.PIN_PROBE_EN); // 获取探头电源引脚
        powerProbe2 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_PROBE58_EN); // 获取5-8通道探头电源引脚
        probeIo = mHwGpioManager.getGpioDev(HwGpioManager.PIN_PROBE_IO); // 获取探头IO引脚
        probeIo2 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_PROBE58_IO); // 获取5-8通道探头IO引脚
        triggerInOut = mHwGpioManager.getGpioDev(HwGpioManager.PIN_TRIG_CTL); // 获取触发控制引脚
        clkInOut = mHwGpioManager.getGpioDev(HwGpioManager.PIN_10M_CLK_CTL); // 获取10MHz时钟控制引脚
//        gpio_50VPwr = mHwGpioManager.getGpioDev(HwGpioManager.PIN_50V_PWR); // 获取50V电源引脚（已注释）
        powerAnalogOn(); // 打开模拟电源（配置时钟需要先给模拟电打开）
        wakeupFpga(); // 唤醒FPGA
    }

    /**
     * 毫秒级延时
     * <p>
     * 功能：使当前线程休眠指定的毫秒数
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
     * 设置触发输入输出方向
     * <p>
     * 功能：控制触发信号的输入输出方向
     * 
     * @param bIn true-输入，false-输出
     */
    public void setTriggerInOut(boolean bIn){
        if(triggerInOut != null){ // 检查触发控制引脚是否存在
            triggerInOut.setVal(bIn ? GpioDev.GPIO_VAL_HIGH : GpioDev.GPIO_VAL_LOW); // 设置触发方向
        }
    }

    /**
     * 设置10MHz时钟输入输出方向
     * <p>
     * 功能：控制10MHz时钟信号的输入输出方向
     * 说明：与触发方向相反
     * 
     * @param bIn true-输入，false-输出
     */
    public void setClkInOut(boolean bIn){
        if(clkInOut != null){ // 检查时钟控制引脚是否存在
            clkInOut.setVal(bIn ? GpioDev.GPIO_VAL_LOW : GpioDev.GPIO_VAL_HIGH); // 设置时钟方向（与触发方向相反）
        }
    }
}
