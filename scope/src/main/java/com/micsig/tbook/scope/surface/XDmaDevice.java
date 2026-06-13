package com.micsig.tbook.scope.surface;

import android.content.Context;
import android.graphics.GraphicBuffer;
import android.hardware.DeviceManager;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.hardware.Hardware;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                XDmaDevice - PCIe XDMA设备驱动类                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的PCIe XDMA设备驱动实现类，继承自HwDevice，                      ║
 * ║   负责通过Xilinx DMA (XDMA)引擎实现高速数据采集和传输。                       ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理PCIe XDMA设备文件描述符                                             ║
 * ║   2. 实现高速DMA数据传输                                                      ║
 * ║   3. 管理FPGA状态和控制                                                       ║
 * ║   4. 提供用户空间缓冲区访问                                                   ║
 * ║   5. 支持设备状态保存/恢复                                                    ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │    HwDevice     │ ← 父类：硬件设备抽象基类        ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                                   ▼                                          ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │   XDmaDevice    │ ← 本类：PCIe XDMA设备          ║
 * ║                          └─────────────────┘                                 ║
 * ║                                                                              ║
 * ║ 【XDMA设备文件】                                                             ║
 * ║   ┌────────────────────────────────────────────────────────────────────┐    ║
 * ║   │  设备文件            │  功能说明                                   │    ║
 * ║   ├────────────────────────────────────────────────────────────────────┤    ║
 * ║   │  xdmaN_c2h_M         │  Card-to-Host DMA通道M（设备到主机）        │    ║
 * ║   │  xdmaN_h2c_M         │  Host-to-Card DMA通道M（主机到设备）        │    ║
 * ║   │  xdmaN_events_M      │  事件中断通道M                              │    ║
 * ║   │  xdmaN_user          │  用户空间寄存器访问                         │    ║
 * ║   └────────────────────────────────────────────────────────────────────┘    ║
 * ║                                                                              ║
 * ║ 【DMA数据流向】                                                              ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ FPGA        │───▶│ PCIe XDMA   │───▶│ DDR/内存    │                   ║
 * ║   │ (数据源)    │    │ (DMA引擎)   │    │ (目标缓冲)  │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║          │                                     │                            ║
 * ║          ▼                                     ▼                            ║
 * ║   ┌─────────────┐                      ┌─────────────┐                     ║
 * ║   │ xdma_c2h    │                      │ lockBuffer  │                     ║
 * ║   │ (C2H通道)   │                      │ (Java API)  │                     ║
 * ║   └─────────────┘                      └─────────────┘                     ║
 * ║                                                                              ║
 * ║ 【XDMA状态机】                                                               ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐  ║
 * ║   │                                                                     │  ║
 * ║   │   XDMA_NONE ──▶ XDMA_START ──▶ XDMA_DONE ──▶ XDMA_PAUSE            │  ║
 * ║   │       │              │              │              │                │  ║
 * ║   │       │              │              │              │                │  ║
 * ║   │       └──────────────┴──────────────┴──────────────┘                │  ║
 * ║   │                           ▲                                         │  ║
 * ║   │                           │                                         │  ║
 * ║   │                      openDevice()                                   │  ║
 * ║   └─────────────────────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 高速示波器数据采集（GS/s级别）                                          ║
 * ║   2. 多通道同步采集                                                          ║
 * ║   3. 实时波形显示                                                            ║
 * ║   4. FPGA寄存器访问                                                          ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   使用synchronized关键字保护关键方法。                                       ║
 * ║   使用volatile修饰共享状态变量。                                             ║
 * ║   Native方法实现需要保证线程安全。                                           ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - HwDevice: 父类，提供硬件设备抽象接口                                    ║
 * ║   - DeviceManager: Android设备管理器                                        ║
 * ║   - ParcelFileDescriptor: Android文件描述符封装                             ║
 * ║   - SurfaceNative: JNI原生Surface接口                                       ║
 * ║   - JNI Native层: C/C++实现                                                 ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class XDmaDevice extends HwDevice {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于在Logcat中过滤和识别本类的日志输出
     */
    private final static String TAG = "XDmaDevice";

    /**
     * XDMA状态常量：无状态/初始状态
     */
    public final static int XDMA_NONE = 0;

    /**
     * XDMA状态常量：启动中
     */
    public final static int XDMA_START = 1;

    /**
     * XDMA状态常量：暂停
     */
    public final static int XDMA_PAUSE = 2;

    /**
     * XDMA状态常量：完成/就绪
     */
    public final static int XDMA_DONE = 3;

    // ═══════════════════════════════════════════════════════════════════════════════
    // FPGA设备路径
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * FPGA状态设备路径
     * 用于读取和设置FPGA的工作状态
     * 路径格式：/sys/devices/platform/3c0800000.pcie/fpga
     */
    private String fpgaStatus = "/sys/devices/platform/3c0800000.pcie/fpga";

    /**
     * XDMA设备移除路径
     * 用于热插拔移除PCIe设备
     */
    private String xdmaRemove = "/sys/bus/pci/devices/0002:20:00.0/remove";

    /**
     * PCIe总线重新扫描路径
     * 用于重新扫描PCIe总线上的设备
     */
    private String xdmaRemove2 = "/sys/bus/pci/rescan";

    // ═══════════════════════════════════════════════════════════════════════════════
    // XDMA设备文件路径数组
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * XDMA Card-to-Host (C2H) DMA通道设备文件
     * 用于从FPGA读取数据到主机内存
     * 数组维度：[设备索引][通道索引]
     * 设备0: /dev/xdma0_c2h_0, /dev/xdma0_c2h_1
     * 设备1: /dev/xdma1_c2h_0, /dev/xdma1_c2h_1
     */
    private final static String [][] XDMA_C2H = {
            {"/dev/xdma0_c2h_0", "/dev/xdma0_c2h_1"},
            {"/dev/xdma1_c2h_0", "/dev/xdma1_c2h_1"},
    };

    /**
     * XDMA事件中断设备文件
     * 用于接收FPGA的中断事件通知
     * 数组维度：[设备索引][通道索引]
     */
    private final static String [][] XDMA_EVENT = {
            {"/dev/xdma0_events_0", "/dev/xdma0_events_1"},
            {"/dev/xdma1_events_0", "/dev/xdma1_events_1"},
    };

    /**
     * XDMA用户空间寄存器访问设备文件
     * 用于访问FPGA的用户自定义寄存器
     */
    private final static String []XDMA_USER = {
            "/dev/xdma0_user", "/dev/xdma1_user"
    };

    /**
     * XDMA C2H通道2设备文件
     * 用于额外的数据传输通道
     */
    private final static String[] XDMA_C2H2 = {
            "/dev/xdma0_c2h_2", "/dev/xdma1_c2h_2"
    };

    /**
     * XDMA Host-to-Card (H2C) DMA通道设备文件
     * 用于从主机发送数据到FPGA
     */
    private final static String[] XDMA_H2C = {
            "/dev/xdma0_h2c_0", "/dev/xdma1_h2c_0"
    };

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 缓冲区和文件描述符
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 用户空间缓冲区
     * 映射FPGA的BAR空间，用于直接访问寄存器
     */
    private ByteBuffer userBuffer = null;

    /**
     * XDMA数据通道文件描述符数组
     * 用于C2H DMA数据传输
     */
    private ParcelFileDescriptor [] XdmaDataFD = new ParcelFileDescriptor[2];

    /**
     * XDMA事件通道文件描述符数组
     * 用于接收中断事件
     */
    private ParcelFileDescriptor [] XdmaEventFD = new ParcelFileDescriptor[2];

    /**
     * XDMA用户空间文件描述符
     * 用于访问用户寄存器
     */
    private ParcelFileDescriptor XdmaUserFD = null;

    /**
     * XDMA C2H通道2文件描述符
     */
    private ParcelFileDescriptor XdmaC2HFD = null;

    /**
     * XDMA H2C通道文件描述符
     * 用于Host-to-Card数据传输
     */
    private ParcelFileDescriptor XdmaH2CFD = null;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 状态
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设备打开标志
     * true: 设备已打开
     * false: 设备已关闭
     * 使用volatile保证多线程可见性
     */
    volatile boolean bOpen = false;

    /**
     * XDMA状态
     * 取值：XDMA_NONE(0), XDMA_START(1), XDMA_PAUSE(2), XDMA_DONE(3)
     * 使用volatile保证多线程可见性
     */
    private volatile int xDmaStatus = XDMA_NONE;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造XDMA设备实例（默认设备路径）
     *
     * @param context Android应用上下文
     * @param idx 设备索引（0或1）
     */
    public XDmaDevice(Context context, int idx){
        super(context, idx);                                                        // 调用父类构造方法
    }

    /**
     * 构造XDMA设备实例（自定义设备路径）
     *
     * @param context Android应用上下文
     * @param idx 设备索引
     * @param fpgeDev FPGA状态设备路径
     * @param devRemove 设备移除路径
     * @param devRemove2 总线重新扫描路径
     */
    public XDmaDevice(Context context, int idx, String fpgeDev, String devRemove, String devRemove2){
        super(context, idx);                                                        // 调用父类构造方法
        this.fpgaStatus = fpgeDev;                                                  // 设置FPGA状态设备路径
        this.xdmaRemove = devRemove;                                                // 设置设备移除路径
        this.xdmaRemove2 = devRemove2;                                              // 设置总线重新扫描路径
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // FPGA状态控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置设备状态（默认值"1"）
     * 向sysfs节点写入"1"
     *
     * @param name 设备节点路径
     */
    public void setStatus(String name){
        setStatus(name, "1");                                                       // 调用带值的方法，默认值"1"
    }

    /**
     * 设置设备状态
     * 向sysfs节点写入指定值
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>通过DeviceManager打开设备端口</li>
     *   <li>写入状态值</li>
     *   <li>关闭文件描述符</li>
     * </ol>
     *
     * @param name 设备节点路径
     * @param val 要写入的值（字符串）
     */
    public void setStatus(String name, String val){
        Log.d(TAG, "setStatus() called with: name = [" + name + "], val = [" + val + "]"); // 打印调试日志
        try {
            ParcelFileDescriptor parcelFileDescriptor = deviceManager.openDevicePort(name); // 通过设备管理器打开端口
            if(parcelFileDescriptor != null) {                                       // 检查文件描述符是否有效
                FileOutputStream out = new FileOutputStream(parcelFileDescriptor.getFileDescriptor()); // 创建输出流
                OutputStreamWriter writer = new OutputStreamWriter(out);            // 创建写入器
                writer.write(val);                                                   // 写入状态值
                writer.flush();                                                      // 刷新缓冲区
                writer.close();                                                      // 关闭写入器
                out.close();                                                         // 关闭输出流
                parcelFileDescriptor.close();                                        // 关闭文件描述符
            }
        }catch (Exception e){
            e.printStackTrace();                                                    // 打印异常堆栈
        }
    }

    /**
     * 获取FPGA状态
     * 从sysfs节点读取状态值
     *
     * @param devName 设备节点路径
     * @return 状态值（整数），读取失败返回0
     */
    private int getFpgaState(String devName){
        int v = 0;                                                                  // 状态值初始化
        try {
            ParcelFileDescriptor parcelFileDescriptor = deviceManager.openDevicePort(devName); // 打开设备端口
            if (parcelFileDescriptor != null) {                                     // 检查文件描述符是否有效
                FileReader fileReader = new FileReader(parcelFileDescriptor.getFileDescriptor()); // 创建文件读取器
                BufferedReader bufferedReader = new BufferedReader(fileReader);     // 创建缓冲读取器
                String str = bufferedReader.readLine();                             // 读取一行
                if (str != null && str.length() > 0) {                              // 检查读取结果
                    str = str.replace("\n", "");                                     // 移除换行符
                    v = Integer.parseInt(str);                                       // 解析为整数
                }
                fileReader.close();                                                 // 关闭文件读取器
                bufferedReader.close();                                             // 关闭缓冲读取器
                parcelFileDescriptor.close();                                        // 关闭文件描述符
            }
        } catch (Exception e) {
            e.printStackTrace();                                                    // 打印异常堆栈
        }
        Log.d(TAG, "getFpgaState() called:" + v + "," + devName);                   // 打印调试日志
        return v;                                                                   // 返回状态值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 设备打开方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 打开FPGA
     * 初始化FPGA状态，准备进行数据采集
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>检查当前状态是否为PAUSE或NONE</li>
     *   <li>读取FPGA状态</li>
     *   <li>根据状态执行相应操作</li>
     *   <li>等待FPGA初始化完成</li>
     * </ol>
     *
     * @return true: 打开成功
     *         false: 打开失败（状态为0xFF表示错误）
     */
    @Override
    public boolean openFpga(){
        // 检查当前状态是否允许打开
        if(xDmaStatus == XDMA_PAUSE                                                 // 暂停状态
                || xDmaStatus == XDMA_NONE){                                        // 或初始状态
            int s = getFpgaState(fpgaStatus);                                       // 获取FPGA状态
            if(s == 0){                                                              // 状态为0，需要启动
                setStatus(fpgaStatus);                                              // 写入"1"启动FPGA
            }else if(s >= 3){                                                        // 状态>=3，需要重置
                setStatus(fpgaStatus, "4");                                         // 写入"4"触发重置
                s = getFpgaState(fpgaStatus);                                       // 再次读取状态
                if(s == 0xFF){                                                       // 状态为0xFF表示错误
                    bOpen = false;                                                   // 设置打开标志为false
                    return false;                                                    // 返回失败
                }
            }
            ms_sleep(200);                                                           // 等待200ms让FPGA初始化
        }
        return true;                                                                // 返回成功
    }

    /**
     * 打开设备
     * 初始化所有XDMA通道和Native资源
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>设置打开标志</li>
     *   <li>等待设备文件出现</li>
     *   <li>打开所有XDMA设备文件</li>
     *   <li>调用Native方法初始化</li>
     *   <li>获取用户缓冲区</li>
     * </ol>
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return true: 打开成功
     *         false: 打开失败
     */
    @Override
    public synchronized boolean openDevice(){
        Log.d(TAG, "openDevice() called:" + idx);                                   // 打印调试日志
        bOpen = true;                                                               // 设置打开标志
        
        // 等待设备文件出现
        while (bOpen){                                                              // 循环直到设备文件出现或关闭
            boolean bExists = true;                                                 // 存在标志
            ms_sleep(100);                                                          // 等待100ms

            // 检查所有C2H设备文件是否存在
            for (String string : XDMA_C2H[idx]) {                                   // 遍历C2H设备文件
                File f = new File(string);                                          // 创建文件对象
                if (!f.exists()) {                                                  // 检查文件是否存在
                    bExists = false;                                                // 设置不存在标志
                    break;                                                          // 跳出循环
                }
            }

            if(bExists){                                                            // 所有文件都存在
                break;                                                              // 跳出等待循环
            }
        }
        
        ms_sleep(200);                                                              // 额外等待200ms
        
        // 打开所有XDMA设备文件
        for(int i = 0; i < XdmaDataFD.length; i++) {                                // 遍历通道
            do {
                ms_sleep(100);                                                      // 等待100ms
                try {
                    // 打开数据通道
                    if (XdmaDataFD[i] == null) {                                    // 检查是否已打开
                        XdmaDataFD[i] = deviceManager.openDevicePort(XDMA_C2H[idx][i]); // 打开C2H数据通道
                    }
                    // 打开事件通道
                    if (XdmaEventFD[i] == null) {                                   // 检查是否已打开
                        XdmaEventFD[i] = deviceManager.openDevicePort(XDMA_EVENT[idx][i]); // 打开事件通道
                    }
                    // 打开用户空间通道
                    if(XdmaUserFD == null){                                         // 检查是否已打开
                        XdmaUserFD = deviceManager.openDevicePort(XDMA_USER[idx]);  // 打开用户空间通道
                    }
                    // 打开C2H通道2
                    if(XdmaC2HFD == null){                                          // 检查是否已打开
                        XdmaC2HFD = deviceManager.openDevicePort(XDMA_C2H2[idx]);    // 打开C2H通道2
                    }
                    // 打开H2C通道
                    if(XdmaH2CFD == null){                                          // 检查是否已打开
                        XdmaH2CFD = deviceManager.openDevicePort(XDMA_H2C[idx]);     // 打开H2C通道
                    }
                } catch (IOException e) {
                    e.printStackTrace();                                            // 打印异常堆栈
                }
            } while ((XdmaDataFD[i] == null                                        // 检查数据通道是否打开
                    || XdmaEventFD[i] == null                                       // 检查事件通道是否打开
                    || XdmaUserFD == null                                           // 检查用户空间通道是否打开
                    || XdmaC2HFD == null                                            // 检查C2H通道2是否打开
                    || XdmaH2CFD == null                                            // 检查H2C通道是否打开
            ) && bOpen);                                                            // 且设备仍然打开
        }

        // 调用Native方法初始化
        if(bOpen) {                                                                 // 检查设备是否仍然打开
            // 打开扩展通道（带SurfaceNative）
            boolean ret = native_openex(idx, XdmaDataFD[0].getFileDescriptor(), XdmaEventFD[0].getFileDescriptor(), getSurfaceNative());
            if(ret){                                                                // 扩展通道打开成功
                // 打开主数据通道
                ret = native_open(idx, XdmaDataFD[1].getFileDescriptor(), XdmaEventFD[1].getFileDescriptor());
                if(ret){                                                            // 主通道打开成功
                    // 打开用户空间通道
                    ret = native_openuser(idx, XdmaUserFD.getFileDescriptor(), XdmaC2HFD.getFileDescriptor(), XdmaH2CFD.getFileDescriptor());
                    if(ret) {                                                       // 用户空间通道打开成功
                        userBuffer = native_userbuffer(idx);                        // 获取用户缓冲区
                        xDmaStatus = XDMA_DONE;                                     // 设置状态为完成
                    }
                }
            }
            return ret;                                                             // 返回结果
        }
        return false;                                                               // 设备已关闭，返回失败
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 设备关闭方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 关闭设备
     * 释放所有XDMA资源
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>设置关闭标志</li>
     *   <li>调用Native关闭方法</li>
     *   <li>关闭所有文件描述符</li>
     * </ol>
     */
    @Override
    public void closeDevice(){
        bOpen = false;                                                              // 设置关闭标志
        synchronized (this) {                                                       // 同步块
            native_close(idx);                                                      // 调用Native关闭方法
            for(int i = 0; i < XdmaDataFD.length; i++) {                            // 遍历所有通道
                try {
                    // 关闭数据通道
                    if (XdmaDataFD[i] != null) {                                    // 检查是否已打开
                        XdmaDataFD[i].close();                                      // 关闭文件描述符
                        XdmaDataFD[i] = null;                                       // 清空引用
                    }
                    // 关闭事件通道
                    if (XdmaEventFD[i] != null) {                                   // 检查是否已打开
                        XdmaEventFD[i].close();                                     // 关闭文件描述符
                        XdmaEventFD[i] = null;                                      // 清空引用
                    }
                    // 关闭用户空间通道
                    if(XdmaUserFD != null){                                         // 检查是否已打开
                        XdmaUserFD.close();                                         // 关闭文件描述符
                        XdmaUserFD = null;                                          // 清空引用
                    }
                    // 关闭C2H通道2
                    if(XdmaC2HFD != null){                                          // 检查是否已打开
                        XdmaC2HFD.close();                                          // 关闭文件描述符
                        XdmaC2HFD = null;                                           // 清空引用
                    }
                    // 关闭H2C通道
                    if(XdmaH2CFD != null){                                          // 检查是否已打开
                        XdmaH2CFD.close();                                          // 关闭文件描述符
                        XdmaH2CFD = null;                                           // 清空引用
                    }
                } catch (IOException e) {
                    e.printStackTrace();                                            // 打印异常堆栈
                }
            }
        }

//        ms_sleep(100);
//        File f = new File(xdmaRemove2);
//        if(f.exists()) {
//            setStatus(xdmaRemove2);
//        }
        // [已注释] 重新扫描PCIe总线的代码
    }

    /**
     * 关闭FPGA
     * 移除PCIe设备并设置暂停状态
     *
     * @return true: 关闭成功
     */
    @Override
    public boolean closeFpga() {
        File f = new File(xdmaRemove);                                              // 创建移除路径文件对象
        if(f.exists()) {                                                            // 检查文件是否存在
            setStatus(xdmaRemove);                                                  // 写入移除命令
        }
        xDmaStatus = XDMA_PAUSE;                                                    // 设置状态为暂停
        return true;                                                                // 返回成功
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 缓冲区管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 锁定缓冲区
     * 获取DMA数据缓冲区用于读取数据
     *
     * @param idx 缓冲区索引
     * @return 数据缓冲区，失败返回null
     */
    @Override
    public ByteBuffer lockBuffer(int idx){
        ByteBuffer byteBuffer = null;                                               // 缓冲区初始化
        if(idx == this.idx) {                                                       // 检查索引是否匹配
            if (isOpen()) {                                                         // 检查设备是否打开
                byteBuffer = native_lock(idx);                                      // 调用Native锁定方法
            }
            if (byteBuffer == null) {                                               // 检查是否获取成功
                ms_sleep(10);                                                       // 失败则等待10ms
            }
        }
        return byteBuffer;                                                          // 返回缓冲区
    }

    /**
     * 解锁缓冲区
     * 释放之前锁定的缓冲区
     *
     * @param idx 缓冲区索引
     */
    @Override
    public void unlockBuffer(int idx){
        native_unlock(idx);                                                         // 调用Native解锁方法
    }

    /**
     * 清除缓冲区
     * 清空DMA缓冲区数据
     */
    @Override
    public void clear() {
        native_clear(idx);                                                          // 调用Native清除方法
    }

    /**
     * 获取BAR缓冲区
     * 返回用户空间缓冲区，用于访问FPGA寄存器
     *
     * @param idx 设备索引
     * @return 用户空间缓冲区
     */
    @Override
    public ByteBuffer getBarBuffer(int idx) {
        if(idx == this.idx) {                                                       // 检查索引是否匹配
            return userBuffer;                                                      // 返回用户缓冲区
        }
        return null;                                                                // 索引不匹配返回null
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 状态持久化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 保存设备状态
     * 将设备状态序列化到文件
     *
     * @param randomAccessFile 随机访问文件
     * @return true: 保存成功
     *         false: 保存失败
     */
    @Override
    public boolean store(RandomAccessFile randomAccessFile) {
        try {
            return native_store(idx, randomAccessFile.getFD());                     // 调用Native保存方法
        } catch (IOException e) {
            e.printStackTrace();                                                    // 打印异常堆栈
        }
        return false;                                                               // 返回失败
    }

    /**
     * 恢复设备状态
     * 从文件反序列化设备状态
     *
     * @param randomAccessFile 随机访问文件
     * @return true: 恢复成功
     *         false: 恢复失败
     */
    @Override
    public boolean restore(RandomAccessFile randomAccessFile) {
        try {
            return native_restore(idx, randomAccessFile.getFD());                   // 调用Native恢复方法
        } catch (IOException e) {
            e.printStackTrace();                                                    // 打印异常堆栈
        }
        return false;                                                               // 返回失败
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 设备控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 停止设备
     * 停止DMA数据传输
     */
    @Override
    public void stop() {
        native_stop(idx);                                                           // 调用Native停止方法
    }

    /**
     * 获取保存/恢复进度
     *
     * @return 进度值（0-100）
     */
    @Override
    public int getSaveRecoveryProgress() {
        return native_progress(idx);                                                // 调用Native进度方法
    }

    /**
     * 恢复设备运行
     * 等待SurfaceNative可用
     */
    @Override
    public void resume() {
        Log.d(TAG, "resume() called:" + isOpen());                                  // 打印调试日志
        if(!isOpen()) {                                                             // 检查设备是否打开
            SurfaceNative surfaceNative;                                            // SurfaceNative引用
            do {
                surfaceNative = getSurfaceNative();                                 // 获取SurfaceNative
                if(surfaceNative != null){                                          // 检查是否获取成功
                    break;                                                          // 成功则跳出循环
                }
                ms_sleep(100);                                                      // 等待100ms
            }while (true);                                                          // 循环直到获取成功
        }
    }

    /**
     * 挂起设备
     * 设置FPGA为低功耗状态
     */
    @Override
    public void suspend() {
        int s = getFpgaState(fpgaStatus);                                           // 获取FPGA状态
        if(s >= 2) {                                                                // 状态>=2时需要挂起
            setStatus(fpgaStatus, "3");                                             // 写入"3"触发挂起
        }
    }

    /**
     * 检查设备是否已打开
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return true: 设备已打开且状态为DONE
     *         false: 设备未打开或状态异常
     */
    @Override
    public synchronized boolean isOpen() {
        return bOpen && xDmaStatus == XDMA_DONE;                                    // 检查打开标志和状态
    }

    /**
     * 检查是否支持异步打开
     * XDMA设备支持异步打开
     *
     * @return true: 支持异步打开
     */
    @Override
    public boolean isAsyncOpen() {
        return true;                                                                // 返回true，支持异步打开
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // JNI Native方法声明
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * [JNI Native方法] 锁定缓冲区
     *
     * @param idx 设备索引
     * @return 数据缓冲区
     */
    private native ByteBuffer native_lock(int idx);

    /**
     * [JNI Native方法] 解锁缓冲区
     *
     * @param idx 设备索引
     */
    private native void native_unlock(int idx);

    /**
     * [JNI Native方法] 打开设备
     *
     * @param idx 设备索引
     * @param dataFd 数据通道文件描述符
     * @param eventFd 事件通道文件描述符
     * @return true: 成功
     */
    private native boolean native_open(int idx, FileDescriptor dataFd, FileDescriptor eventFd);

    /**
     * [JNI Native方法] 打开扩展设备
     *
     * @param idx 设备索引
     * @param dataFd 数据通道文件描述符
     * @param eventFd 事件通道文件描述符
     * @param surfaceNative SurfaceNative实例
     * @return true: 成功
     */
    private native boolean native_openex(int idx, FileDescriptor dataFd, FileDescriptor eventFd, SurfaceNative surfaceNative);

    /**
     * [JNI Native方法] 打开用户空间设备
     *
     * @param idx 设备索引
     * @param barfd BAR文件描述符
     * @param c2hfd C2H通道文件描述符
     * @param h2cfd H2C通道文件描述符
     * @return true: 成功
     */
    private native boolean native_openuser(int idx, FileDescriptor barfd, FileDescriptor c2hfd, FileDescriptor h2cfd);

    /**
     * [JNI Native方法] 保存状态
     *
     * @param idx 设备索引
     * @param fd 文件描述符
     * @return true: 成功
     */
    private native boolean native_store(int idx, FileDescriptor fd);

    /**
     * [JNI Native方法] 恢复状态
     *
     * @param idx 设备索引
     * @param fd 文件描述符
     * @return true: 成功
     */
    private native boolean native_restore(int idx, FileDescriptor fd);

    /**
     * [JNI Native方法] 停止设备
     *
     * @param idx 设备索引
     */
    private native void native_stop(int idx);

    /**
     * [JNI Native方法] 获取进度
     *
     * @param idx 设备索引
     * @return 进度值
     */
    private native int native_progress(int idx);

    /**
     * [JNI Native方法] 获取用户缓冲区
     *
     * @param idx 设备索引
     * @return 用户缓冲区
     */
    private native ByteBuffer native_userbuffer(int idx);

    /**
     * [JNI Native方法] 关闭设备
     *
     * @param idx 设备索引
     */
    private native void native_close(int idx);

    /**
     * [JNI Native方法] 清除缓冲区
     *
     * @param idx 设备索引
     */
    private native void native_clear(int idx);

}
