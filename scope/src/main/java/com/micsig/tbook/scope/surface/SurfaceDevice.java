package com.micsig.tbook.scope.surface;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Data.DataFactory;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                SurfaceDevice - Surface设备管理类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的设备管理类，继承自Thread，                                     ║
 * ║   负责管理硬件设备的数据采集线程和SurfaceNative生命周期。                      ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 创建和管理HwDevice硬件设备实例                                          ║
 * ║   2. 管理SurfaceNative数组（多设备支持）                                     ║
 * ║   3. 运行数据采集线程                                                        ║
 * ║   4. 使用线程池并行读取多设备数据                                            ║
 * ║   5. 将数据分发给DataFactory处理                                              ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │     Thread      │ ← Java线程基类                  ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                                   ▼                                          ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │  SurfaceDevice  │ ← 本类：设备管理线程            ║
 * ║                          └─────────────────┘                                 ║
 * ║                                                                              ║
 * ║ 【数据采集流程】                                                             ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                      SurfaceDevice 线程                             │   ║
 * ║   │                                                                     │   ║
 * ║   │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐            │   ║
 * ║   │  │ openDevice  │───▶│  启动线程   │───▶│ 数据采集循环│            │   ║
 * ║   │  │ (打开设备)  │    │  start()    │    │  run()      │            │   ║
 * ║   │  └─────────────┘    └─────────────┘    └──────┬──────┘            │   ║
 * ║   │                                               │                     │   ║
 * ║   │                                               ▼                     │   ║
 * ║   │   ┌──────────────────────────────────────────────────────────┐    │   ║
 * ║   │   │              线程池 (2个线程)                            │    │   ║
 * ║   │   │  ┌─────────────┐           ┌─────────────┐             │    │   ║
 * ║   │   │  │ 设备0数据采集│           │ 设备1数据采集│             │    │   ║
 * ║   │   │  │ lockBuffer  │           │ lockBuffer  │             │    │   ║
 * ║   │   │  │ DataFactory │           │ DataFactory │             │    │   ║
 * ║   │   │  │ unlockBuffer│           │ unlockBuffer│             │    │   ║
 * ║   │   │  └─────────────┘           └─────────────┘             │    │   ║
 * ║   │   └──────────────────────────────────────────────────────────┘    │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【多设备支持】                                                               ║
 * ║   ┌────────────────────────────────────────────────────────────────────┐    ║
 * ║   │  设备索引  │  SurfaceNative        │  说明                        │    ║
 * ║   ├────────────────────────────────────────────────────────────────────┤    ║
 * ║   │  0        │  surfaceNative[0]      │  第一个设备/通道组           │    ║
 * ║   │  1        │  surfaceNative[1]      │  第二个设备/通道组           │    ║
 * ║   │  ...      │  ...                   │  更多设备                    │    ║
 * ║   └────────────────────────────────────────────────────────────────────┘    ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 示波器数据采集主线程                                                    ║
 * ║   2. 多通道同步采集                                                          ║
 * ║   3. PCIe/USB设备数据读取                                                   ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   使用synchronized关键字保护关键方法。                                       ║
 * ║   使用volatile修饰bOpen标志。                                                ║
 * ║   数据采集在独立线程中运行，与UI线程隔离。                                    ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - HwDevice: 硬件设备抽象类                                                 ║
 * ║   - DeviceFactory: 设备工厂，创建设备实例                                   ║
 * ║   - SurfaceNative: JNI原生Surface接口                                       ║
 * ║   - DataFactory: 数据工厂，处理采集的数据                                   ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-3-27                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class SurfaceDevice extends Thread {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于在Logcat中过滤和识别本类的日志输出
     */
    private static final String TAG = "SurfaceDevice";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Android应用上下文
     * 用于获取系统服务和资源
     */
    private Context mContext;

    /**
     * 硬件设备实例
     * 通过DeviceFactory分配，用于实际的数据采集操作
     */
    private HwDevice hwDevice;

    /**
     * 设备打开标志
     * true: 设备已打开，数据采集线程运行中
     * false: 设备已关闭，数据采集线程应退出
     * 使用volatile保证多线程可见性
     */
    private volatile boolean bOpen = false;

    /**
     * SurfaceNative数组
     * 每个设备对应一个SurfaceNative实例
     * 用于JNI层的数据传递和渲染
     */
    private SurfaceNative[] surfaceNative = null;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 线程池
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 固定大小线程池
     * 创建2个工作线程，用于并行读取多设备数据
     * 使用ExecutorService管理线程生命周期
     */
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造Surface设备管理实例
     * 初始化硬件设备和SurfaceNative数组
     *
     * <p><b>初始化流程：</b></p>
     * <ol>
     *   <li>保存应用上下文</li>
     *   <li>通过DeviceFactory分配硬件设备</li>
     *   <li>创建SurfaceNative数组</li>
     * </ol>
     *
     * @param context Android应用上下文
     */
    public SurfaceDevice(Context context) {
        mContext = context;                                                         // 保存应用上下文
        hwDevice = DeviceFactory.allocDevice();                                     // 通过设备工厂分配设备实例
        surfaceNative = new SurfaceNative[hwDevice.getDevCnt()];                    // 创建SurfaceNative数组，大小为设备数量
        Log.d("XDmaDev", "DevCnt:" + hwDevice.getDevCnt());                         // 打印设备数量日志
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 属性访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取设备数量
     *
     * @return 设备数量
     */
    public int getDevCnt() {
        return hwDevice.getDevCnt();                                                // 返回设备数量
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 设备管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 打开设备
     * 创建SurfaceNative并启动数据采集线程
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>创建SurfaceNative实例</li>
     *   <li>获取Surface并关联到设备</li>
     *   <li>如果是首次打开，启动数据采集线程</li>
     * </ol>
     *
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @param idx 设备索引，取值范围：0 ~ (设备数量-1)
     * @param surfaceTexture Android SurfaceTexture，用于渲染
     */
    public synchronized void openDevice(int idx, SurfaceTexture surfaceTexture) {
        // 打印调试日志，记录打开设备事件
        Log.d(TAG, "openDevice() called with: idx = [" + idx + "], surfaceTexture = [" + surfaceTexture + "]" + "," + this);

        // 创建SurfaceNative实例
        // 参数1: SurfaceTexture
        // 参数2: 缓冲区数量（7个缓冲区用于双缓冲/三缓冲机制）
        surfaceNative[idx] = new SurfaceNative(surfaceTexture, 7);
        
        // 获取Surface并设置尺寸
        // 参数: 宽度, 高度
        surfaceNative[idx].acquireSurface(ScopeBase.getWidth(), ScopeBase.getHeight());
        
        // 将SurfaceNative关联到硬件设备
        hwDevice.setSurfaceNative(idx, surfaceNative[idx]);

        // 检查是否首次打开
        if(!bOpen) {
            bOpen = true;                                                           // 设置打开标志
            start();                                                                // 启动数据采集线程
        }else{
            Log.e("zhuzh", "-------------------");                                  // 打印警告日志（非首次打开）
        }
    }

    /**
     * 检查设备是否已打开
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return true: 设备已打开
     *         false: 设备已关闭
     */
    public synchronized boolean isOpen() {
        return bOpen;                                                               // 返回打开状态
    }

    /**
     * 清除设备缓冲区
     * 清空硬件设备的数据缓冲区
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     */
    public synchronized void clear(){
        if(bOpen) {                                                                 // 检查设备是否打开
            hwDevice.clear();                                                      // 清除设备缓冲区
        }
    }

    /**
     * 关闭设备
     * 停止数据采集线程，释放所有资源
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>设置关闭标志，通知线程退出</li>
     *   <li>清除设备的SurfaceNative引用</li>
     *   <li>释放所有SurfaceNative资源</li>
     * </ol>
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     */
    public synchronized void closeDevice() {
        if (bOpen) {                                                                // 检查设备是否打开
            Log.d(TAG, "closeDevice() called");                                     // 打印调试日志
            bOpen = false;                                                          // 设置关闭标志，通知线程退出
            hwDevice.setSurfaceNative(null);                                        // 清除设备的SurfaceNative引用
            for (SurfaceNative aNative : surfaceNative) {                           // 遍历所有SurfaceNative
                aNative.releaseSurface();                                           // 释放Surface资源
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 线程主方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 线程主方法
     * 数据采集的主循环，使用线程池并行读取多设备数据
     *
     * <p><b>执行流程：</b></p>
     * <ol>
     *   <li>打开硬件设备（同步或异步）</li>
     *   <li>创建数据采集任务列表</li>
     *   <li>使用线程池并行执行数据采集</li>
     *   <li>循环读取数据并分发给DataFactory</li>
     *   <li>设备关闭时退出循环并关闭设备</li>
     * </ol>
     *
     * <p><b>数据采集模式：</b></p>
     * <ul>
     *   <li>异步打开模式：等待设备异步初始化完成</li>
     *   <li>同步打开模式：直接打开设备</li>
     * </ul>
     */
    @Override
    public void run() {
        super.run();                                                                // 调用父类run方法

        // 获取硬件设备实例（线程内部重新获取）
        HwDevice hwDevice = DeviceFactory.allocDevice();
        
        // 根据设备打开模式处理
        if (hwDevice.isAsyncOpen()) {                                               // 异步打开模式
            // 等待设备异步初始化完成
            while (!hwDevice.isOpen() && isOpen()) {                                // 循环等待设备打开
                try {
                    Thread.sleep(100);                                              // 休眠100ms
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);                                  // 抛出运行时异常
                }
            }
        } else {                                                                    // 同步打开模式
            if (!hwDevice.isOpen()) {                                               // 设备未打开
                hwDevice.openDevice();                                              // 打开设备
            }
        }
        
        // 打印日志，表示设备已就绪
        Logger.d(TAG, "--------------------------");
        
        // 获取设备数量
        int devcnt = hwDevice.getDevCnt();
        
        // 主数据采集循环
        while (isOpen()) {                                                          // 循环直到设备关闭
            // 创建任务列表
            List<Callable<Boolean>> list = new ArrayList<>();
            
            // 为每个设备创建数据采集任务
            for (int i = 0; i < devcnt; i++) {                                      // 遍历所有设备
                int finalI = i;                                                     // 最终索引（lambda表达式需要final变量）
                
                // 添加数据采集任务
                list.add(()->{
                    ByteBuffer byteBuffer;                                          // 数据缓冲区
                    DataFactory dataFactory = DataFactory.getInstance();            // 获取数据工厂单例
                    
                    // 数据采集内循环
                    while (isOpen()) {                                              // 循环直到设备关闭
                        // 锁定设备缓冲区，获取数据
                        byteBuffer = hwDevice.lockBuffer(finalI);
                        
                        // 检查缓冲区有效性和设备状态
                        if (byteBuffer != null && isOpen()) {                       // 缓冲区有效且设备打开
                            // 将数据分发给数据工厂处理
                            // 参数: 设备索引, 数据缓冲区, 数据大小
                            dataFactory.onRecv(finalI, byteBuffer, byteBuffer.capacity());
                            
                            // 解锁缓冲区，允许设备继续写入
                            hwDevice.unlockBuffer(finalI);
                        }
                    }
                    return true;                                                    // 任务完成，返回true
                });
            }
            
            // 使用线程池并行执行所有任务
            try {
                fixedThreadPool.invokeAll(list);                                    // 阻塞等待所有任务完成
            } catch (InterruptedException e) {
                throw new RuntimeException(e);                                      // 抛出运行时异常
            }
        }
        
        // 打印日志，表示线程结束
        Logger.d(TAG, "--------------------------end");
        
        // 关闭硬件设备
        hwDevice.closeDevice();
    }
}
