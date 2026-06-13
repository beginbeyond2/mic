package com.micsig.tbook.hardware; // 硬件层包名，包含示波器硬件相关操作类

import android.annotation.SuppressLint; // 导入注解类，用于抑制lint警告
import android.content.Context; // 导入Android上下文类，用于获取系统服务
import android.hardware.DeviceManager; // 导入设备管理器，用于打开设备端口
import android.hardware.SpiManager; // 导入SPI管理器，用于管理SPI设备
import android.hardware.SpiPort; // 导入SPI端口类，提供底层SPI通信接口
import android.os.ParcelFileDescriptor; // 导入文件描述符包装类，用于设备文件访问
import android.util.Log; // 导入Android日志工具类，用于调试输出

import androidx.annotation.IntDef; // 导入整型注解定义，用于类型安全的常量定义

import java.io.FileOutputStream; // 导入文件输出流，用于写入设备文件
import java.io.IOException; // 导入IO异常类，用于处理设备操作异常
import java.lang.annotation.Retention; // 导入注解保留策略
import java.lang.annotation.RetentionPolicy; // 导入注解保留策略常量
import java.nio.ByteBuffer; // 导入字节缓冲区类，用于SPI数据传输
import java.nio.ByteOrder; // 导入字节序类（本类未使用）
import java.nio.channels.FileChannel; // 导入文件通道类，用于高效文件写入
import java.util.logging.Logger; // 导入日志类（本类未使用）

/**
 * ┌─────────────────────────────────────────────────────────────────────────────────────────┐
 * │                                  SpiDevManager                                          │
 * │                              SPI设备管理器                                              │
 * ├─────────────────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                             │
 * │   硬件抽象层核心 - SPI设备统一管理器，负责SPI设备的创建、初始化和访问                       │
 * │                                                                                         │
 * │ 【核心职责】                                                                             │
 * │   1. 设备管理：管理多个SPI设备实例（FPGA启动、FPGA命令、时钟等）                           │
 * │   2. 设备初始化：根据硬件产品类型初始化对应的SPI设备                                       │
 * │   3. 设备访问：提供统一的SPI设备获取接口                                                  │
 * │   4. 类型安全：使用@IntDef注解确保设备类型参数安全                                        │
 * │                                                                                         │
 * │ 【架构设计】                                                                             │
 * │   ┌─────────────────────────────────────────────────────────────────────────────────┐   │
 * │   │                        SpiDevManager (单例)                                      │   │
 * │   │  ┌───────────────────────────────────────────────────────────────────────────┐  │   │
 * │   │  │                      SPI设备数组 (mSpiDev[])                               │  │   │
 * │   │  │  ┌─────────────┬─────────────┬─────────────┬─────────────┐               │  │   │
 * │   │  │  │ [0] FPGA_BOOT │ [1] FPGA1_CMD│ [2] FPGA2_CMD│ [3] CLK     │               │  │   │
 * │   │  │  │ (FPGA启动)   │ (FPGA1命令)  │ (FPGA2命令)  │ (时钟控制)  │               │  │   │
 * │   │  │  └─────────────┴─────────────┴─────────────┴─────────────┘               │  │   │
 * │   │  └───────────────────────────────────────────────────────────────────────────┘  │   │
 * │   │                                    ▲                                            │   │
 * │   │                                    │ 创建                                       │   │
 * │   │  ┌───────────────────────────────────────────────────────────────────────────┐  │   │
 * │   │  │                      内部类 (SpiDev子类)                                   │  │   │
 * │   │  │  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐              │  │   │
 * │   │  │  │ NullSpiDev      │ │ FPGA_BOOT_SpiDev│ │ FPGA_CMD_SpiDev │              │  │   │
 * │   │  │  │ (空设备)        │ │ (FPGA启动SPI)   │ │ (FPGA命令SPI)   │              │  │   │
 * │   │  │  └─────────────────┘ └─────────────────┘ └─────────────────┘              │  │   │
 * │   │  │  ┌─────────────────┐ ┌─────────────────┐                                  │  │   │
 * │   │  │  │ CLK_SpiDev      │ │FPGA_BOOT_FSpiDev│                                  │  │   │
 * │   │  │  │ (时钟SPI)       │ │ (快速SPI)       │                                  │  │   │
 * │   │  │  └─────────────────┘ └─────────────────┘                                  │  │   │
 * │   │  └───────────────────────────────────────────────────────────────────────────┘  │   │
 * │   └─────────────────────────────────────────────────────────────────────────────────┘   │
 * │                                                                                         │
 * │ 【设备类型映射】                                                                         │
 * │   ┌──────────────────┬─────────────────────────────────────────────────────────────┐   │
 * │   │      类型         │                      说明                                  │   │
 * │   ├──────────────────┼─────────────────────────────────────────────────────────────┤   │
 * │   │ SPI_DEV_FPGA_BOOT │ FPGA启动设备，用于加载FPGA配置数据                          │   │
 * │   │ SPI_DEV_FPGA1_CMD │ FPGA1命令设备，用于与FPGA1通信                             │   │
 * │   │ SPI_DEV_FPGA2_CMD │ FPGA2命令设备，用于与FPGA2通信                             │   │
 * │   │ SPI_DEV_CLK       │ 时钟控制设备，用于时钟芯片配置                              │   │
 * │   └──────────────────┴─────────────────────────────────────────────────────────────┘   │
 * │                                                                                         │
 * │ 【依赖关系】                                                                             │
 * │   - Context: Android应用上下文                                                          │
 * │   - SpiManager: SPI设备管理服务                                                        │
 * │   - SpiDev: SPI设备抽象基类                                                            │
 * │   - HwServiceName: 服务名称常量                                                        │
 * │   - HardwareProduct: 硬件产品识别                                                      │
 * │                                                                                         │
 * │ 【使用示例】                                                                             │
 * │   // 初始化（通常在Application中）                                                       │
 * │   SpiDevManager.getInstance(context);                                                  │
 * │                                                                                         │
 * │   // 获取SPI设备                                                                        │
 * │   SpiDev fpgaBoot = SpiDevManager.getInstance()                                        │
 * │       .getSpiDev(SpiDevManager.SPI_DEV_FPGA_BOOT);                                     │
 * │                                                                                         │
 * │   // 使用SPI设备                                                                        │
 * │   fpgaBoot.write(buffer, length);                                                      │
 * │                                                                                         │
 * │ 【线程安全】                                                                             │
 * │   采用双重检查锁定(DCL)实现单例，保证线程安全                                             │
 * │                                                                                         │
 * │ 【作者】zhuzh                                                                            │
 * │ 【日期】2018/3/9                                                                         │
 * └─────────────────────────────────────────────────────────────────────────────────────────┘
 */

public class SpiDevManager {

    // ==================== 成员变量定义 ====================

    /**
     * SPI管理器实例
     * 
     * 【功能说明】
     *   Android SpiManager服务，用于打开SPI端口
     */
    private  SpiManager mSpiManager; // SPI管理器，用于管理SPI设备

    /**
     * Android应用上下文
     * 
     * 【功能说明】
     *   用于获取系统服务和访问应用资源
     */
    private  Context mContext; // 应用上下文对象

    // ==================== 设备类型注解定义 ====================

    /**
     * SPI设备类型注解定义
     * 
     * 【功能说明】
     *   使用@IntDef定义设备类型常量集合
     *   配合@Retention(SOURCE)实现编译时类型检查
     */
    @IntDef({ SPI_DEV_FPGA_BOOT,SPI_DEV_FPGA1_CMD,SPI_DEV_FPGA2_CMD,SPI_DEV_CLK}) // 定义允许的常量值
    @Retention(RetentionPolicy.SOURCE) // 注解仅在源码保留，不编译到class文件
    public @interface SpiDevType {} // SPI设备类型注解接口

    // ==================== 设备类型常量定义 ====================

    /**
     * FPGA启动SPI设备索引
     * 
     * 【功能说明】
     *   用于FPGA配置数据加载
     *   根据硬件版本可能是普通SPI或快速SPI(FSPI)
     */
    public final static int SPI_DEV_FPGA_BOOT = 0; // FPGA启动设备索引

    /**
     * FPGA1命令SPI设备索引
     * 
     * 【功能说明】
     *   用于与FPGA1芯片进行命令交互
     *   SPI Mode 3，10MHz时钟
     */
    public final static int SPI_DEV_FPGA1_CMD = 1; // FPGA1命令设备索引

    /**
     * FPGA2命令SPI设备索引
     * 
     * 【功能说明】
     *   用于与FPGA2芯片进行命令交互
     *   SPI Mode 3，10MHz时钟
     */
    public final static int SPI_DEV_FPGA2_CMD = 2; // FPGA2命令设备索引

    /**
     * 时钟控制SPI设备索引
     * 
     * 【功能说明】
     *   用于时钟芯片配置
     *   当前使用NullSpiDev占位
     */
    public final static int SPI_DEV_CLK = 3; // 时钟控制设备索引

    /**
     * SPI设备数量上限
     * 
     * 【功能说明】
     *   定义SPI设备数组的最大长度
     */
    public final static int SPI_DEV_MAX = 4; // SPI设备最大数量

    // ==================== SPI模式常量定义 ====================

    /**
     * SPI时钟相位标志
     * 
     * 【说明】
     *   CPHA=1表示在第二个时钟边沿采样
     */
    private final static int SPI_CPHA = 0x01; // 时钟相位标志位

    /**
     * SPI时钟极性标志
     * 
     * 【说明】
     *   CPOL=1表示空闲时时钟为高电平
     */
    private final static int SPI_CPOL = 0x02; // 时钟极性标志位

    /**
     * SPI模式0
     * 
     * 【说明】
     *   CPOL=0, CPHA=0
     *   空闲低电平，第一边沿采样
     */
    private final static int SPI_MODE_0 = 		(0|0); // SPI模式0：CPOL=0, CPHA=0

    /**
     * SPI模式1
     * 
     * 【说明】
     *   CPOL=0, CPHA=1
     *   空闲低电平，第二边沿采样
     */
    private final static int SPI_MODE_1	=	(0|SPI_CPHA); // SPI模式1：CPOL=0, CPHA=1

    /**
     * SPI模式2
     * 
     * 【说明】
     *   CPOL=1, CPHA=0
     *   空闲高电平，第一边沿采样
     */
    private final static int SPI_MODE_2	=	(SPI_CPOL|0); // SPI模式2：CPOL=1, CPHA=0

    /**
     * SPI模式3
     * 
     * 【说明】
     *   CPOL=1, CPHA=1
     *   空闲高电平，第二边沿采样
     */
    private final static int SPI_MODE_3	=	(SPI_CPOL|SPI_CPHA); // SPI模式3：CPOL=1, CPHA=1

    // ==================== SPI设备数组 ====================

    /**
     * SPI设备实例数组
     * 
     * 【功能说明】
     *   存储所有SPI设备实例
     *   索引对应SPI_DEV_*常量
     */
    private SpiDev mSpiDev[] = new SpiDev[SPI_DEV_MAX]; // SPI设备数组，存储所有设备实例

    // ==================== 单例实现 ====================

    /**
     * 单例实例
     * 
     * 【功能说明】
     *   使用volatile保证多线程环境下的可见性
     */
    private static volatile SpiDevManager instance = null; // 单例实例，volatile保证线程安全

    /**
     * 获取单例实例（无参数版本）
     * 
     * 【功能说明】
     *   获取已初始化的SpiDevManager实例
     *   必须先调用getInstance(Context)进行初始化
     * 
     * 【返回值】
     *   @return SpiDevManager实例，如果未初始化则返回null
     */
    public static SpiDevManager getInstance(){ // 获取单例实例（无参数）
        return instance; // 返回当前实例
    } // getInstance方法结束

    /**
     * 获取单例实例（带初始化版本）
     * 
     * 【功能说明】
     *   获取SpiDevManager单例实例，如果未初始化则创建新实例
     *   采用双重检查锁定(DCL)模式，保证线程安全
     * 
     * 【参数说明】
     *   @param context Android应用上下文，用于获取系统服务
     * 
     * 【返回值】
     *   @return SpiDevManager单例实例
     * 
     * 【线程安全】
     *   使用双重检查锁定，避免多线程环境下创建多个实例
     */
    public static SpiDevManager getInstance(Context context) { // 获取单例实例（带初始化）
        if (instance == null) { // 第一次检查：避免不必要的同步
            synchronized (SpiDevManager.class) { // 同步锁：保证线程安全
                if (instance == null && context != null) { // 第二次检查：确保只创建一个实例
                    instance = new SpiDevManager(context); // 创建新实例
                } // 第二次检查结束
            } // 同步块结束
        } // 第一次检查结束
        return instance; // 返回单例实例
    } // getInstance方法结束

    // ==================== 构造方法 ====================

    /**
     * 私有构造函数 - 初始化SPI设备管理器
     * 
     * 【功能说明】
     *   获取SpiManager服务并初始化所有SPI设备
     * 
     * 【参数说明】
     *   @param context Android应用上下文
     * 
     * 【初始化流程】
     *   1. 保存应用上下文
     *   2. 获取SpiManager系统服务
     *   3. 调用init()初始化所有SPI设备
     */
    @SuppressLint("WrongConstant") // 抑制lint警告：使用自定义服务常量
    private SpiDevManager(Context context){ // 私有构造函数，防止外部实例化
        mContext = context; // 保存应用上下文
        mSpiManager = (SpiManager)mContext.getSystemService(HwServiceName.SPI_SERVICE); // 获取SPI管理器服务

        init(); // 初始化所有SPI设备
    } // 构造函数结束

    // ==================== 初始化方法 ====================

    /**
     * 初始化所有SPI设备
     * 
     * 【功能说明】
     *   根据硬件产品类型创建对应的SPI设备实例
     *   打开SPI端口并配置参数
     * 
     * 【初始化流程】
     *   1. 根据硬件类型创建FPGA启动设备（FSPI或普通SPI）
     *   2. 创建FPGA命令设备
     *   3. 创建时钟控制设备
     *   4. 遍历所有设备，打开SPI端口并初始化
     */
    private void init(){ // 初始化所有SPI设备
        SpiPort spiPort = null; // SPI端口临时变量
        if(HardwareProduct.isFSpiBoot()){ // 检查是否使用快速SPI启动
            mSpiDev[SPI_DEV_FPGA_BOOT] = new FPGA_BOOT_FSpiDev(mContext); // 创建FSPI设备
        }else { // 使用普通SPI启动
            mSpiDev[SPI_DEV_FPGA_BOOT] = new FPGA_BOOT_SpiDev(); // 创建普通SPI设备
        } // 设备类型选择结束
        mSpiDev[SPI_DEV_FPGA1_CMD] = new FPGA_CMD_SpiDev(); // 创建FPGA1命令设备

        mSpiDev[SPI_DEV_FPGA2_CMD] = new FPGA2_CMD_SpiDev(); // 创建FPGA2命令设备
        mSpiDev[SPI_DEV_CLK] = new NullSpiDev(); // 创建空设备（时钟控制暂未实现）

        for(int i=0;i<SPI_DEV_MAX;i++){ // 遍历所有SPI设备
            if(mSpiDev[i] != null // 检查设备实例是否存在
                    && !mSpiDev[i].isFSpiDev()) { // 检查是否为非FSPI设备（FSPI使用不同的初始化方式）
                try { // 异常捕获块开始
                    Log.d("zhuzh","spi dev name:" + mSpiDev[i].getDevName()); // 调试日志：打印设备名称
                    String devName = mSpiDev[i].getDevName(); // 获取设备名称
                    if(devName != null && devName.length() > 0) { // 检查设备名称是否有效
                        spiPort = mSpiManager.openSpiPort(devName); // 打开SPI端口
                    } // 设备名称有效性检查结束
                } catch (IOException e) { // 捕获IO异常
                    e.printStackTrace(); // 打印异常堆栈
                } // 异常捕获块结束
                if(spiPort != null) { // 检查端口是否成功打开
                    mSpiDev[i].onInitDev(spiPort); // 调用设备初始化回调
                } // 端口有效性检查结束
            } // 设备类型检查结束
        } // 设备遍历循环结束
    } // init方法结束

    // ==================== 设备访问方法 ====================

    /**
     * 获取SPI设备实例
     * 
     * 【功能说明】
     *   根据设备类型索引获取对应的SPI设备实例
     * 
     * 【参数说明】
     *   @param idx 设备类型索引，使用@SpiDevType注解确保类型安全
     * 
     * 【返回值】
     *   @return SpiDev实例，无效索引返回null
     * 
     * 【使用示例】
     *   SpiDev dev = manager.getSpiDev(SpiDevManager.SPI_DEV_FPGA_BOOT);
     */
    public SpiDev getSpiDev(@SpiDevType int idx ){ // 获取SPI设备实例

        if(idx>=0 && idx <SPI_DEV_MAX) { // 检查索引有效性
            return mSpiDev[idx]; // 返回对应的设备实例
        } // 索引有效性检查结束
        return null; // 无效索引返回null
    } // getSpiDev方法结束


    // ==================== 内部类：空SPI设备 ====================

    /**
     * 空SPI设备实现
     * 
     * 【功能说明】
     *   占位设备，不执行实际操作
     *   用于未实现或不需要的SPI设备槽位
     */
    private class NullSpiDev extends SpiDev{ // 空SPI设备内部类
        /**
         * 空初始化实现
         * @param spiPort SPI端口（不使用）
         */
        @Override
        public void onInitDev(SpiPort spiPort) { // 初始化回调（空实现）

        } // onInitDev方法结束

        /**
         * 返回null设备名称
         * @return null
         */
        @Override
        public String getDevName() { // 获取设备名称
            return null; // 返回null表示无设备
        } // getDevName方法结束

    } // NullSpiDev类结束

    // ==================== 内部类：时钟SPI设备 ====================

    /**
     * 时钟控制SPI设备实现
     * 
     * 【功能说明】
     *   用于配置时钟芯片
     *   SPI Mode 0，20MHz时钟
     * 
     * 【设备路径】
     *   /dev/spidev3.0
     */
    private class CLK_SpiDev extends SpiDev{ // 时钟SPI设备内部类


        /**
         * 初始化时钟SPI设备
         * @param spiPort SPI端口
         */
        @Override
        public void onInitDev(SpiPort spiPort) { // 初始化回调
            initDev(spiPort,SPI_MODE_0,8,20*1000*1000,0,0); // 配置：Mode0, 8位, 20MHz, MSB优先
        } // onInitDev方法结束

        /**
         * 获取时钟SPI设备名称
         * @return 设备路径"/dev/spidev3.0"
         */
        @Override
        public String getDevName() { // 获取设备名称
            return "/dev/spidev3.0"; // 返回设备节点路径
        } // getDevName方法结束
    } // CLK_SpiDev类结束

    // ==================== 内部类：FPGA快速SPI设备 ====================

    /**
     * FPGA启动快速SPI设备实现
     * 
     * 【功能说明】
     *   使用FSPI（快速SPI）接口加载FPGA配置
     *   通过DeviceManager打开设备文件，使用FileChannel写入
     *   支持地址自动递增，适合大容量数据传输
     * 
     * 【设备路径】
     *   /dev/fspidev5.0
     * 
     * 【特点】
     *   - 使用FileChannel进行高效写入
     *   - 支持地址字段（3字节）
     *   - 适用于高速FPGA配置
     */
    private class FPGA_BOOT_FSpiDev extends SpiDev{ // FPGA快速SPI设备内部类

        /**
         * 文件描述符包装对象
         * 用于访问设备文件
         */
        ParcelFileDescriptor parcelFileDescriptor; // 文件描述符包装类

        /**
         * 文件输出流
         * 用于写入设备文件
         */
        FileOutputStream fileOutputStream; // 文件输出流

        /**
         * 文件通道
         * 用于高效写入操作
         */
        FileChannel fileChannel; // 文件通道，用于高效IO


        /**
         * 构造函数 - 初始化FSPI设备
         * 
         * @param context 应用上下文，用于获取DeviceManager
         */
        public FPGA_BOOT_FSpiDev(Context context) { // 构造函数
            super(); // 调用父类构造函数
            DeviceManager deviceManager = (DeviceManager)context.getSystemService("device"); // 获取设备管理器
            try { // 异常捕获块开始
                parcelFileDescriptor = deviceManager.openDevicePort(getDevName()); // 打开设备端口
                if(parcelFileDescriptor != null) { // 检查端口是否成功打开
                    fileOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor()); // 创建文件输出流
                    fileChannel = fileOutputStream.getChannel(); // 获取文件通道
                } // 端口有效性检查结束
            } catch (IOException e) { // 捕获IO异常
                e.printStackTrace(); // 打印异常堆栈
            } // 异常捕获块结束
        } // 构造函数结束

        /**
         * 标识为FSPI设备
         * @return true
         */
        @Override
        public boolean isFSpiDev() { // 判断是否为FSPI设备
            return true; // 返回true表示是FSPI设备
        } // isFSpiDev方法结束

        /**
         * FSPI设备初始化（空实现）
         * @param spiPort SPI端口（不使用）
         */
        @Override
        public void onInitDev(SpiPort spiPort) { // 初始化回调（空实现）

        } // onInitDev方法结束

        /**
         * 获取FSPI设备名称
         * @return 设备路径"/dev/fspidev5.0"
         */
        @Override
        public String getDevName() { // 获取设备名称
            return "/dev/fspidev5.0"; // 返回FSPI设备节点路径
        } // getDevName方法结束



        /**
         * FSPI读操作（未实现）
         * 
         * @param wbyteBuffer 发送缓冲区
         * @param length      数据长度
         * @param byteBuffer  接收缓冲区
         * @return 0
         */
        @Override
        public int read(ByteBuffer wbyteBuffer, int length, ByteBuffer byteBuffer) { // 读操作（未实现）
            return 0; // 返回0表示未实现
        } // read方法结束

        /**
         * FSPI写操作
         * 
         * 【功能说明】
         *   将数据写入FSPI设备
         *   自动添加3字节地址前缀
         * 
         * @param byteBuffer 数据缓冲区
         * @param length     数据长度
         */
        @Override
        public void write(ByteBuffer byteBuffer, int length) { // 写操作

            if(fileChannel != null){ // 检查文件通道是否有效
                byteBuffer.put(0,(byte) ((addr >>> 16) & 0xFF)); // 写入地址高字节
                byteBuffer.put(1,(byte) ((addr >>> 8) & 0xFF)); // 写入地址中字节
                byteBuffer.put(2,(byte) ((addr) & 0xFF)); // 写入地址低字节
                byteBuffer.position(0); // 重置缓冲区位置
                byteBuffer.limit(length); // 设置缓冲区限制

                try { // 异常捕获块开始
                    fileChannel.write(byteBuffer); // 通过文件通道写入数据
                    fileOutputStream.flush(); // 刷新输出流
                } catch (IOException e) { // 捕获IO异常
                    e.printStackTrace(); // 打印异常堆栈
                } // 异常捕获块结束
            } // 文件通道有效性检查结束
        } // write方法结束

        /**
         * 获取地址字节数
         * @return 3（24位地址）
         */
        @Override
        public int getAddrNBytes() { // 获取地址字节数
            return 3; // FSPI使用3字节地址
        } // getAddrNBytes方法结束
    } // FPGA_BOOT_FSpiDev类结束

    // ==================== 内部类：FPGA启动SPI设备 ====================

    /**
     * FPGA启动普通SPI设备实现
     * 
     * 【功能说明】
     *   使用普通SPI接口加载FPGA配置
     *   SPI Mode 0，50MHz高速时钟
     * 
     * 【设备路径】
     *   - MHO28V1产品：/dev/spidev0.2
     *   - 其他产品：/dev/spidev0.0
     */
    private class FPGA_BOOT_SpiDev extends SpiDev{ // FPGA启动SPI设备内部类

        /**
         * 初始化FPGA启动SPI设备
         * @param spiPort SPI端口
         */
        @Override
        public void onInitDev(SpiPort spiPort) { // 初始化回调
            initDev(spiPort,SPI_MODE_0,8,50*1000*1000,0,0); // 配置：Mode0, 8位, 50MHz, MSB优先
        } // onInitDev方法结束

        /**
         * 获取FPGA启动SPI设备名称
         * @return 根据硬件产品返回对应设备路径
         */
        @Override
        public String getDevName() { // 获取设备名称
            if(HardwareProduct.isMHO28V1()){ // 检查是否为MHO28V1产品
                return "/dev/spidev0.2"; // MHO28V1使用spidev0.2
            }else { // 其他产品
                return "/dev/spidev0.0"; // 默认使用spidev0.0
            } // 产品类型判断结束
        } // getDevName方法结束
    } // FPGA_BOOT_SpiDev类结束

    // ==================== 内部类：FPGA1命令SPI设备 ====================

    /**
     * FPGA1命令SPI设备实现
     * 
     * 【功能说明】
     *   用于与FPGA1芯片进行命令交互
     *   SPI Mode 3，10MHz时钟
     * 
     * 【设备路径】
     *   /dev/spidev0.0
     */
    private class FPGA_CMD_SpiDev extends SpiDev { // FPGA1命令SPI设备内部类

        /**
         * 初始化FPGA命令SPI设备
         * @param spiPort SPI端口
         */
        @Override
        public void onInitDev(SpiPort spiPort) { // 初始化回调
            initDev(spiPort,SPI_MODE_3,8,10*1000*1000,0,0); // 配置：Mode3, 8位, 10MHz, MSB优先
        } // onInitDev方法结束

        /**
         * 获取FPGA命令SPI设备名称
         * @return 设备路径"/dev/spidev0.0"
         */
        @Override
        public String getDevName() { // 获取设备名称
            return "/dev/spidev0.0"; // 返回设备节点路径
        } // getDevName方法结束
    } // FPGA_CMD_SpiDev类结束

    // ==================== 内部类：FPGA2命令SPI设备 ====================

    /**
     * FPGA2命令SPI设备实现
     * 
     * 【功能说明】
     *   用于与FPGA2芯片进行命令交互
     *   SPI Mode 3，10MHz时钟
     * 
     * 【设备路径】
     *   /dev/spidev0.1
     */
    private class FPGA2_CMD_SpiDev extends SpiDev { // FPGA2命令SPI设备内部类

        /**
         * 初始化FPGA2命令SPI设备
         * @param spiPort SPI端口
         */
        @Override
        public void onInitDev(SpiPort spiPort) { // 初始化回调
            initDev(spiPort,SPI_MODE_3,8,10*1000*1000,0,0); // 配置：Mode3, 8位, 10MHz, MSB优先
        } // onInitDev方法结束

        /**
         * 获取FPGA2命令SPI设备名称
         * @return 设备路径"/dev/spidev0.1"
         */
        @Override
        public String getDevName() { // 获取设备名称
            return "/dev/spidev0.1"; // 返回设备节点路径
        } // getDevName方法结束
    } // FPGA2_CMD_SpiDev类结束

} // SpiDevManager类结束
