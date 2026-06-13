package com.micsig.tbook.scope.fpga;                                                     // 包声明：FPGA模块

import android.content.Context;                                                          // 导入：上下文类
import android.os.Build;                                                                 // 导入：系统构建信息类
import android.os.Environment;                                                           // 导入：环境存储类
import android.os.Looper;                                                                // 导入：消息循环类
import android.util.Log;                                                                 // 导入：日志类

import com.micsig.tbook.hardware.Hardware;                                               // 导入：硬件操作类
import com.micsig.tbook.hardware.HardwareProduct;                                        // 导入：硬件产品定义类
import com.micsig.tbook.scope.BuildConfig;                                               // 导入：构建配置类
import com.micsig.tbook.scope.Event.EventBase;                                           // 导入：事件基类
import com.micsig.tbook.scope.Event.EventFactory;                                        // 导入：事件工厂类
import com.micsig.tbook.scope.R;                                                         // 导入：资源类
import com.micsig.tbook.scope.ScopeMessage;                                              // 导入：示波器消息类

import java.io.File;                                                                     // 导入：文件类
import java.io.FileInputStream;                                                          // 导入：文件输入流类
import java.io.FileNotFoundException;                                                    // 导入：文件未找到异常类
import java.io.IOException;                                                              // 导入：IO异常类
import java.io.InputStream;                                                              // 导入：输入流类

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              FPGABoot - 示波器FPGA固件加载管理类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   FPGA模块的固件加载管理类，位于fpga包下，                                     ║
 * ║   负责FPGA固件的加载、管理和状态维护。                                        ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理FPGABoot单例实例                                                    ║
 * ║   2. 加载FPGA固件到硬件                                                      ║
 * ║   3. 管理FPGA类型（普通模式、TXT模式）                                        ║
 * ║   4. 根据硬件产品型号选择正确的固件                                           ║
 * ║   5. 支持调试模式加载外部固件文件                                             ║
 * ║   6. 发送FPGA加载状态事件                                                    ║
 * ║                                                                              ║
 * ║ 【架构位置】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        FPGA模块架构                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │   ScopeMain     │────────▶│    FPGABoot     │                   │ ║
 * ║   │   │   (示波器主类)   │         │  (固件加载管理)  │                   │ ║
 * ║   │   └─────────────────┘         └────────┬────────┘                   │ ║
 * ║   │          │                             │                            │ ║
 * ║   │          ▼                             ▼                            │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │  ScopeMessage   │         │    Hardware     │                   │ ║
 * ║   │   │  (消息处理类)    │         │   (硬件操作类)   │                   │ ║
 * ║   │   └─────────────────┘         └─────────────────┘                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【FPGA固件加载流程】                                                         ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        FPGA固件加载流程                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   应用启动 ──▶ FPGABoot.getInstance() ──▶ loadFpga()                 │ ║
 * ║   │                                                │                      │ ║
 * ║   │                              ┌─────────────────┼─────────────────┐   │ ║
 * ║   │                              ▼                 ▼                 ▼   │ ║
 * ║   │                         主线程加载        子线程加载        已加载   │ ║
 * ║   │                              │                 │                 │   │ ║
 * ║   │                              ▼                 ▼                 ▼   │ ║
 * ║   │                      ScopeMessage        Hardware.loadFpgaCode   跳过 │ ║
 * ║   │                              │                 │                 │   │ ║
 * ║   │                              └─────────────────┼─────────────────┘   │ ║
 * ║   │                                                ▼                      │ ║
 * ║   │                                    setFpgaBootOK(true)              │ ║
 * ║   │                                                │                      │ ║
 * ║   │                                                ▼                      │ ║
 * ║   │                                    EVENT_FPGA_STATUS事件            │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【FPGA类型说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        FPGA类型定义                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【FPGA_TYPE_NONE = -1】                                              │ ║
 * ║   │   - 未加载状态                                                         │ ║
 * ║   │   - 初始状态或重置后的状态                                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   【FPGA_TYPE_NORMAL = 0】                                             │ ║
 * ║   │   - 普通模式固件                                                       │ ║
 * ║   │   - 用于正常示波器功能                                                 │ ║
 * ║   │   - 包含完整的示波器功能逻辑                                           │ ║
 * ║   │                                                                      │ ║
 * ║   │   【FPGA_TYPE_TXT = 1】                                                │ ║
 * ║   │   - TXT模式固件                                                       │ ║
 * ║   │   - 用于特殊测试或调试功能                                             │ ║
 * ║   │   - 可能包含额外的测试逻辑                                             │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【硬件产品型号与固件映射】                                                   ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                    硬件产品型号固件映射表                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   产品型号           │  普通模式固件              │  TXT模式固件       │ ║
 * ║   │   ──────────────────┼──────────────────────────┼──────────────────  │ ║
 * ║   │   RK3588_MHO68_V1   │  fpga_top_mho_10008       │  fpga_top_txt_mho_10008 │ ║
 * ║   │   RK3588_MHO68_V2   │  fpga_top_mho_10008_v2    │  fpga_top_mho_10008_v2  │ ║
 * ║   │   RK3588_MHO38_V1   │  fpga_top_mho_38          │  fpga_top_txt_mho_38    │ ║
 * ║   │   RK3588_MHO28_V1   │  fpga_top_mho_28          │  fpga_top_txt_mho_28    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【产品型号说明】                                                     │ ║
 * ║   │   - MHO68: 68系列示波器（带宽68MHz）                                  │ ║
 * ║   │   - MHO38: 38系列示波器（带宽38MHz）                                  │ ║
 * ║   │   - MHO28: 28系列示波器（带宽28MHz）                                  │ ║
 * ║   │   - V1/V2: 硬件版本号                                                 │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【调试模式说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        调试模式固件加载                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   【触发条件】                                                         │ ║
 * ║   │   - BuildConfig.DEBUG = true（调试版本）                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【固件文件路径】                                                     │ ║
 * ║   │   - 普通模式：/sdcard/debug/fpga_debug.bin                            │ ║
 * ║   │   - TXT模式：/sdcard/debug/fpga_debug_txt.bin                         │ ║
 * ║   │                                                                      │ ║
 * ║   │   【使用场景】                                                         │ ║
 * ║   │   - 开发调试阶段                                                      │ ║
 * ║   │   - 固件更新测试                                                      │ ║
 * ║   │   - 问题排查                                                          │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        线程安全设计                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【单例模式】                                                         │ ║
 * ║   │   - 使用双重检查锁定（Double-Check Locking）                          │ ║
 * ║   │   - volatile修饰instance保证可见性                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【线程判断】                                                         │ ║
 * ║   │   - isMainThread()判断当前是否在主线程                                │ ║
 * ║   │   - 主线程：通过ScopeMessage发送消息加载                              │ ║
 * ║   │   - 子线程：直接调用Hardware.loadFpgaCode()加载                       │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【事件通知】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        事件通知机制                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【EVENT_FPGA_STATUS】                                                │ ║
 * ║   │   - 事件ID：EventFactory.EVENT_FPGA_STATUS                           │ ║
 * ║   │   - 事件数据：fpgaBootOK（加载成功标志）                              │ ║
 * ║   │   - 触发时机：setFpgaBootOK()被调用时                                 │ ║
 * ║   │   - 接收者：所有注册了该事件的观察者                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【EVENT_FPGA_LOAD_OK】                                               │ ║
 * ║   │   - 事件ID：EventFactory.EVENT_FPGA_LOAD_OK                          │ ║
 * ║   │   - 含义：FPGA加载成功                                                │ ║
 * ║   │   - 状态：已注释，未使用                                               │ ║
 * ║   │                                                                      │ ║
 * ║   │   【EVENT_FPGA_LOAD_ERR】                                              │ ║
 * ║   │   - 事件ID：EventFactory.EVENT_FPGA_LOAD_ERR                         │ ║
 * ║   │   - 含义：FPGA加载失败                                                │ ║
 * ║   │   - 状态：已注释，未使用                                               │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 单例模式：使用双重检查锁定确保线程安全的单例实例                        ║
 * ║   - 策略模式：根据线程类型选择不同的加载策略                                ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - Hardware: 硬件操作类                                                   ║
 * ║   - HardwareProduct: 硬件产品定义类                                        ║
 * ║   - ScopeMessage: 示波器消息类                                             ║
 * ║   - EventFactory: 事件工厂类                                               ║
 * ║   - Context: Android上下文                                                 ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 应用启动：初始化FPGA，加载固件                                        ║
 * ║   2. 固件更新：加载新版本固件                                              ║
 * ║   3. 模式切换：切换普通模式和TXT模式                                       ║
 * ║   4. 调试测试：加载调试固件                                                ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ║ 【创建日期】 2018/3/16                                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器FPGA固件加载管理类
 * 负责FPGA固件的加载、管理和状态维护
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>FPGA固件加载：将固件加载到FPGA芯片</li>
 *   <li>固件类型管理：支持普通模式和TXT模式</li>
 *   <li>硬件适配：根据产品型号选择正确的固件</li>
 *   <li>调试支持：支持从外部文件加载调试固件</li>
 *   <li>状态通知：发送FPGA加载状态事件</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 获取FPGABoot实例
 * FPGABoot fpgaBoot = FPGABoot.getInstance(context);
 *
 * // 加载普通模式固件
 * fpgaBoot.loadFpga(FPGABoot.FPGA_TYPE_NORMAL);
 *
 * // 检查加载状态
 * if (fpgaBoot.isFpgaBootOK()) {
 *     // FPGA加载成功
 * }
 *
 * // 切换到TXT模式
 * fpgaBoot.loadFpga(FPGABoot.FPGA_TYPE_TXT);
 *
 * // 重置FPGA状态
 * fpgaBoot.reset();
 * </pre>
 *
 * @see Hardware
 * @see HardwareProduct
 * @see ScopeMessage
 * @see EventFactory
 */
public class FPGABoot {                                                                  // 类声明：FPGA固件加载管理类

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例实例
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 单例实例，使用volatile保证多线程可见性 */
    private static volatile FPGABoot instance = null;                                    // 静态变量：单例实例，volatile保证可见性

    // ═══════════════════════════════════════════════════════════════════════════════
    // FPGA类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** FPGA类型：未加载状态 */
    public static final int FPGA_TYPE_NONE = -1;                                         // 静态常量：未加载状态，值为-1

    /** FPGA类型：普通模式固件 */
    public static final int FPGA_TYPE_NORMAL = 0;                                        // 静态常量：普通模式固件，值为0

    /** FPGA类型：TXT模式固件 */
    public static final int FPGA_TYPE_TXT = 1;                                           // 静态常量：TXT模式固件，值为1

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** FPGA加载成功标志 */
    private boolean fpgaBootOK = false;                                                  // 成员变量：FPGA加载成功标志，初始值为false

    /** Android上下文对象 */
    private Context mContext;                                                            // 成员变量：Android上下文对象

    /** 当前FPGA类型 */
    private int fpgaType = FPGA_TYPE_NONE;                                               // 成员变量：当前FPGA类型，初始值为FPGA_TYPE_NONE

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例模式实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取FPGABoot单例实例（无参数版本）
     * 用于已经初始化后获取实例
     *
     * @return FPGABoot单例实例，可能为null（未初始化时）
     */
    public static FPGABoot getInstance(){                                                // 静态公有方法：获取单例实例（无参数）
        return instance;                                                                 // 返回单例实例
    }                                                                                       // 方法结束

    /**
     * 获取FPGABoot单例实例（带Context参数版本）
     * 使用双重检查锁定（Double-Check Locking）确保线程安全
     *
     * @param context Android上下文对象
     * @return FPGABoot单例实例
     */
    public static FPGABoot getInstance(Context context) {                                // 静态公有方法：获取单例实例（带Context）
        if (instance == null) {                                                           // 第一次检查：如果实例为null
            synchronized (FPGABoot.class) {                                               // 同步块：锁定类对象
                if (instance == null && context != null) {                                // 第二次检查：如果实例仍为null且context不为null
                    instance = new FPGABoot(context);                                     // 创建单例实例
                }                                                                           // if语句结束
            }                                                                               // 同步块结束
        }                                                                                   // if语句结束
        return instance;                                                                   // 返回单例实例
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 私有构造函数
     * 初始化FPGABoot实例
     *
     * @param context Android上下文对象
     */
    private FPGABoot(Context context){                                                    // 私有构造方法：初始化FPGABoot实例
        mContext = context;                                                               // 保存上下文对象引用
    }                                                                                       // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // FPGA类型管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前FPGA类型
     *
     * @return 当前FPGA类型（FPGA_TYPE_NONE、FPGA_TYPE_NORMAL、FPGA_TYPE_TXT）
     */
    public int getFpgaType(){                                                             // 公有方法：获取当前FPGA类型
        return fpgaType;                                                                  // 返回当前FPGA类型
    }                                                                                       // 方法结束

    /**
     * 重置FPGA状态
     * 将FPGA类型重置为未加载状态
     */
    public void reset(){                                                                  // 公有方法：重置FPGA状态
        fpgaType = FPGA_TYPE_NONE;                                                        // 将FPGA类型重置为未加载状态
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // FPGA加载方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 恢复加载FPGA
     * 如果当前FPGA类型为NONE，则加载普通模式固件
     * 否则加载当前类型的固件
     */
    public void resume(){                                                                 // 公有方法：恢复加载FPGA
        int xType = fpgaType;                                                             // 获取当前FPGA类型
        if(xType == FPGA_TYPE_NONE) {                                                     // 如果当前类型为NONE
            xType = FPGA_TYPE_NORMAL;                                                     // 设置为普通模式
        }                                                                                   // if语句结束
        loadFpga(xType);                                                                  // 加载FPGA固件
    }                                                                                       // 方法结束

    /**
     * 检查FPGA是否加载成功
     *
     * @return true=加载成功，false=加载失败或未加载
     */
    public boolean isFpgaBootOK(){                                                        // 公有方法：检查FPGA是否加载成功
        return fpgaBootOK;                                                                // 返回FPGA加载成功标志
    }                                                                                       // 方法结束

    /**
     * 加载FPGA固件
     * 根据当前线程类型选择不同的加载策略
     *
     * <p><b>加载策略：</b></p>
     * <ul>
     *   <li>主线程：通过ScopeMessage发送消息加载</li>
     *   <li>子线程：直接调用Hardware.loadFpgaCode()加载</li>
     * </ul>
     *
     * @param fpgaType FPGA类型（FPGA_TYPE_NORMAL或FPGA_TYPE_TXT）
     */
    public void loadFpga(int fpgaType){                                                   // 公有方法：加载FPGA固件
        // ─────────────────────────────────────────────────────────────────────────
        // 注释掉的MHO产品特殊处理逻辑
        // ─────────────────────────────────────────────────────────────────────────
        //        if(HardwareProduct.isMHO()){                                            // 如果是MHO产品
        //            HwDevice hwDevice = DeviceFactory.allocDevice();                   // 分配设备
        //            if(!hwDevice.isOpen()) {                                            // 如果设备未打开
        //                hwDevice.openDevice();                                          // 打开设备
        //            }                                                                   // if语句结束
        //            setFpgaBootOK(true);                                                // 设置加载成功
        //        }                                                                       // if语句结束
        //        else                                                                    // 否则
        {                                                                                   // 代码块：非MHO产品的通用处理逻辑
            fpgaBootOK = false;                                                            // 初始化加载成功标志为false
            setFpgaBootOK(fpgaBootOK);                                                     // 发送加载状态事件
            if (isMainThread()) {                                                          // 如果当前是主线程
                loadFpgaMainThread(fpgaType);                                              // 通过主线程加载
            } else {                                                                       // 否则（子线程）
                int ret = EventFactory.EVENT_FPGA_LOAD_ERR;                                // 初始化返回值为加载失败事件
                if(!fpgaBootOK || this.fpgaType != fpgaType) {                             // 如果未加载成功或类型不匹配
                    if (loadFpgaOtherThread(fpgaType)) {                                   // 通过子线程加载
                        ret = EventFactory.EVENT_FPGA_LOAD_OK;                             // 设置返回值为加载成功事件
                    }                                                                       // if语句结束
                }                                                                           // if语句结束
                //EventFactory.sendEvent(new EventBase(ret, fpgaType));                    // 注释掉的代码：发送加载结果事件
            }                                                                               // if-else语句结束
        }                                                                                   // 代码块结束
    }                                                                                       // 方法结束

    /**
     * 设置FPGA加载成功标志
     * 同时发送FPGA状态事件通知
     *
     * @param b true=加载成功，false=加载失败
     */
    public void setFpgaBootOK(boolean b){                                                 // 公有方法：设置FPGA加载成功标志
        fpgaBootOK = b;                                                                   // 设置加载成功标志
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_FPGA_STATUS, fpgaBootOK)); // 发送FPGA状态事件
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 内部加载方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 在主线程加载FPGA固件
     * 通过ScopeMessage发送加载消息
     *
     * @param fpgaType FPGA类型
     * @return true=加载成功，false=加载失败
     */
    private boolean loadFpgaMainThread(int fpgaType){                                     // 私有方法：在主线程加载FPGA固件
        return ScopeMessage.getInstance().sendFpgaBoot(fpgaType);                         // 通过ScopeMessage发送FPGA加载消息
    }                                                                                       // 方法结束

    /**
     * 在子线程加载FPGA固件
     * 直接调用Hardware.loadFpgaCode()加载固件
     *
     * <p><b>加载流程：</b></p>
     * <ol>
     *   <li>获取固件输入流</li>
     *   <li>读取固件数据到字节数组</li>
     *   <li>循环调用Hardware.loadFpgaCode()直到成功</li>
     *   <li>失败时等待1秒后重试</li>
     * </ol>
     *
     * @param fpgaType FPGA类型
     * @return true=加载成功，false=加载失败
     */
    public boolean loadFpgaOtherThread(int fpgaType){                                     // 公有方法：在子线程加载FPGA固件
        Log.d("HH","开始加载网表----------------------------");                            // 输出日志：开始加载网表
        boolean ret = false;                                                              // 初始化返回值为false
        InputStream is = getInputStream(fpgaType);                                        // 获取固件输入流
        if(is != null){                                                                   // 如果输入流不为null
            try{                                                                           // try块：处理IO异常
                byte [] fpga = new byte[is.available()];                                  // 创建字节数组，大小为输入流可用字节数
                int len = is.read(fpga);                                                  // 读取固件数据到字节数组
                is.close();                                                               // 关闭输入流
                this.fpgaType = FPGA_TYPE_NONE;                                           // 临时设置FPGA类型为NONE
                while(!ret){                                                              // 循环：直到加载成功
                    ret = Hardware.getInstance().loadFpgaCode(fpga);                      // 调用Hardware加载FPGA固件
                    if (ret) {                                                            // 如果加载成功
                        this.fpgaType = fpgaType;                                         // 设置当前FPGA类型
                        break;                                                            // 跳出循环
                    }                                                                       // if语句结束
                    try {                                                                  // try块：处理中断异常
                        Thread.sleep(1000);                                               // 线程休眠1秒
                    } catch (InterruptedException e) {                                    // 捕获中断异常
                        e.printStackTrace();                                              // 打印异常堆栈
                    }                                                                       // try-catch块结束
                }                                                                           // 循环结束
            }catch (IOException e){                                                        // 捕获IO异常
                e.printStackTrace();                                                      // 打印异常堆栈
            }                                                                               // try-catch块结束
        }                                                                                   // if语句结束
        return ret;                                                                       // 返回加载结果
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 固件输入流获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取FPGA固件输入流
     * 优先尝试从调试文件加载，失败则从资源文件加载
     *
     * <p><b>固件选择逻辑：</b></p>
     * <ol>
     *   <li>尝试从调试文件加载（仅DEBUG模式）</li>
     *   <li>根据FPGA类型和产品型号选择资源文件</li>
     *   <li>打开资源文件输入流</li>
     * </ol>
     *
     * @param fpgaType FPGA类型
     * @return 固件输入流，失败返回null
     */
    private InputStream getInputStream(int fpgaType){                                     // 私有方法：获取FPGA固件输入流

        InputStream inputStream = getDebugInputStream(fpgaType);                          // 尝试获取调试输入流
        if(inputStream == null) {                                                         // 如果调试输入流为null
            int fpga_top_id = R.raw.fpga_top_mho_10008_v2;                                // 默认固件资源ID
            switch (fpgaType) {                                                           // 根据FPGA类型选择固件
                default:                                                                  // 默认情况
                case FPGA_TYPE_NORMAL:                                                    // 普通模式
                    switch (Build.PRODUCT){                                               // 根据产品型号选择固件
                        case HardwareProduct.RK3588_MHO68_V1:                             // MHO68 V1版本
                            fpga_top_id = R.raw.fpga_top_mho_10008;break;                 // 设置固件资源ID
                        case HardwareProduct.RK3588_MHO68_V2:                             // MHO68 V2版本
                            fpga_top_id = R.raw.fpga_top_mho_10008_v2;break;              // 设置固件资源ID
                        case HardwareProduct.RK3588_MHO38_V1:                             // MHO38 V1版本
                            fpga_top_id = R.raw.fpga_top_mho_38;break;                    // 设置固件资源ID
                        case HardwareProduct.RK3588_MHO28_V1:                             // MHO28 V1版本
                            fpga_top_id = R.raw.fpga_top_mho_28;break;                    // 设置固件资源ID
                    }                                                                       // switch语句结束

                    break;                                                                // 跳出switch
                case FPGA_TYPE_TXT:                                                       // TXT模式
                    switch (Build.PRODUCT){                                               // 根据产品型号选择固件
                        case HardwareProduct.RK3588_MHO68_V1:                             // MHO68 V1版本
                            fpga_top_id = R.raw.fpga_top_txt_mho_10008;break;             // 设置固件资源ID
                        case HardwareProduct.RK3588_MHO68_V2:                             // MHO68 V2版本
                            fpga_top_id = R.raw.fpga_top_mho_10008_v2;break;              // 设置固件资源ID
                        case HardwareProduct.RK3588_MHO38_V1:                             // MHO38 V1版本
                            fpga_top_id = R.raw.fpga_top_txt_mho_38;break;                // 设置固件资源ID
                        case HardwareProduct.RK3588_MHO28_V1:                             // MHO28 V1版本
                            fpga_top_id = R.raw.fpga_top_txt_mho_28;break;                // 设置固件资源ID
                    }                                                                       // switch语句结束
                    break;                                                                // 跳出switch
            }                                                                               // switch语句结束
            return mContext.getResources().openRawResource(fpga_top_id);                  // 打开资源文件输入流
        }                                                                                   // if语句结束
        return inputStream;                                                               // 返回调试输入流
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断当前是否在主线程
     * 通过比较主线程Looper和当前线程Looper判断
     *
     * @return true=主线程，false=子线程
     */
    private boolean isMainThread() {                                                      // 私有方法：判断当前是否在主线程
        return Looper.getMainLooper() == Looper.myLooper();                               // 比较主线程Looper和当前线程Looper
    }                                                                                       // 方法结束

    /**
     * 获取调试固件输入流
     * 仅在DEBUG模式下尝试从外部文件加载固件
     *
     * <p><b>调试文件路径：</b></p>
     * <ul>
     *   <li>普通模式：/sdcard/debug/fpga_debug.bin</li>
     *   <li>TXT模式：/sdcard/debug/fpga_debug_txt.bin</li>
     * </ul>
     *
     * @param fpgaType FPGA类型
     * @return 调试固件输入流，失败返回null
     */
    private InputStream getDebugInputStream(int fpgaType){                                // 私有方法：获取调试固件输入流
        if (!BuildConfig.DEBUG) return null;                                              // 如果不是DEBUG模式，返回null
        String path = Environment.getExternalStorageDirectory().toString();                // 获取外部存储目录路径
        String fileName = "fpga_debug.bin";                                               // 默认调试文件名
        if (fpgaType == FPGA_TYPE_TXT) {                                                  // 如果是TXT模式
            fileName = "fpga_debug_txt.bin";                                              // 设置TXT模式调试文件名
        }                                                                                   // if语句结束
        File f = new File(path + File.separator + "debug" + File.separator + fileName);   // 创建调试文件对象
        if(f.exists()){                                                                    // 如果调试文件存在
            try {                                                                           // try块：处理文件未找到异常
                return new FileInputStream(f);                                            // 返回文件输入流
            } catch (FileNotFoundException e) {                                           // 捕获文件未找到异常
                e.printStackTrace();                                                      // 打印异常堆栈
            }                                                                               // try-catch块结束
        }                                                                                   // if语句结束
        return null;                                                                       // 返回null
    }                                                                                       // 方法结束

}                                                                                           // 类结束
