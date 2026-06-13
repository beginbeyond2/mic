package com.micsig.tbook.scope.surface;

import android.content.Context;

import com.micsig.tbook.hardware.HardwareProduct;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   DeviceFactory - 硬件设备工厂类                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的硬件设备工厂类，负责创建和管理硬件设备实例。                   ║
 * ║   采用单例模式确保全局只有一个硬件设备实例，避免资源冲突。                    ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 创建硬件设备实例（HwDevice）                                            ║
 * ║   2. 管理硬件设备单例                                                        ║
 * ║   3. 提供全局硬件设备访问接口                                                ║
 * ║   4. 保证线程安全的设备创建                                                  ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   单例模式（Singleton Pattern）                                              ║
 * ║   - 使用类级别同步锁保证线程安全                                             ║
 * ║   - 延迟初始化（Lazy Initialization）                                        ║
 * ║   - 全局访问点通过静态方法提供                                               ║
 * ║                                                                              ║
 * ║ 【架构位置】                                                                 ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                         应用层                                       │   ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐  │   ║
 * ║   │   │                    Surface模块                               │  │   ║
 * ║   │   │  ┌─────────────────────────────────────────────────────┐   │  │   ║
 * ║   │   │  │              DeviceFactory（本类）                   │   │  │   ║
 * ║   │   │  │                    │                                │   │  │   ║
 * ║   │   │  │                    ▼                                │   │  │   ║
 * ║   │   │  │              ┌───────────┐                          │   │  │   ║
 * ║   │   │  │              │ HwDevice  │ ← 硬件设备抽象           │   │  │   ║
 * ║   │   │  │              └─────┬─────┘                          │   │  │   ║
 * ║   │   │  └────────────────────┼────────────────────────────────┘   │  │   ║
 * ║   │   │                       │                                     │  │   ║
 * ║   │   └───────────────────────┼─────────────────────────────────────┘  │   ║
 * ║   │                           │                                        │   ║
 * ║   │                           ▼                                        │   ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐  │   ║
 * ║   │   │                    Hardware模块                              │  │   ║
 * ║   │   │  ┌─────────────────────────────────────────────────────┐   │  │   ║
 * ║   │   │  │              XDmaDevManage                          │   │  │   ║
 * ║   │   │  │              (DMA设备管理)                           │   │  │   ║
 * ║   │   │  └─────────────────────────────────────────────────────┘   │  │   ║
 * ║   │   └─────────────────────────────────────────────────────────────┘  │   ║
 * ║   │                                                                     │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【单例模式实现】                                                             ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                                                                     │   ║
 * ║   │   1. 首次调用 allocDevice(context)                                  │   ║
 * ║   │      │                                                              │   ║
 * ║   │      ▼                                                              │   ║
 * ║   │   2. 检查 hwDevice == null ?                                        │   ║
 * ║   │      │                                                              │   ║
 * ║   │      ├── 是 ──► 创建新实例: new XDmaDevManage(context, 2)          │   ║
 * ║   │      │                                                              │   ║
 * ║   │      └── 否 ──► 返回已有实例                                        │   ║
 * ║   │                                                                     │   ║
 * ║   │   3. 后续调用 allocDevice() 或 allocDevice(context)                 │   ║
 * ║   │      │                                                              │   ║
 * ║   │      ▼                                                              │   ║
 * ║   │   4. 直接返回已创建的单例实例                                        │   ║
 * ║   │                                                                     │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【线程安全说明】                                                             ║
 * ║   使用 synchronized (DeviceFactory.class) 类级别锁：                        ║
 * ║   - 保证多线程环境下只创建一个实例                                          ║
 * ║   - 防止竞态条件导致的重复创建                                              ║
 * ║   - 类级别锁比对象锁更严格，适用于静态方法                                  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → HwDevice: 硬件设备抽象接口                                              ║
 * ║   → XDmaDevManage: DMA设备管理实现类                                        ║
 * ║   → Context: Android应用上下文                                              ║
 * ║   → HardwareProduct: 硬件产品相关（导入但未直接使用）                        ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 应用启动时初始化硬件设备                                               ║
 * ║   2. 各模块获取硬件设备实例进行数据采集                                     ║
 * ║   3. 示波器波形显示时访问硬件数据                                           ║
 * ║                                                                              ║
 * ║ 【注意事项】                                                                 ║
 * ║   1. 必须先调用 allocDevice(context) 初始化设备                            ║
 * ║   2. 单例生命周期与应用相同，不会自动释放                                   ║
 * ║   3. Context建议使用Application Context避免内存泄漏                        ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh                                                    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class DeviceFactory {

    /**
     * 硬件设备单例实例
     * 全局唯一的硬件设备对象
     * 默认值：null（未初始化）
     *
     * 使用类级别同步锁保护，保证线程安全
     */
    private static HwDevice hwDevice = null;

    /**
     * 获取硬件设备实例（无参版本）
     * 直接返回已创建的硬件设备实例，不进行初始化
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>设备已初始化后，各模块获取设备实例</li>
     *   <li>快速访问已存在的设备实例</li>
     * </ul>
     *
     * <p><b>线程安全：</b>
     * <ul>
     *   <li>使用 synchronized (DeviceFactory.class) 类级别锁</li>
     *   <li>保证读取操作的原子性</li>
     * </ul>
     *
     * @return 硬件设备实例
     *         如果未初始化，返回 null
     *         如果已初始化，返回全局唯一的 HwDevice 实例
     *
     * @warning 调用此方法前，必须确保已调用 allocDevice(context) 完成初始化
     *
     * @example
     *   // 获取已初始化的设备实例
     *   HwDevice device = DeviceFactory.allocDevice();
     *   if (device != null) {
     *       // 使用设备进行操作
     *   }
     */
    public static HwDevice allocDevice(){
        synchronized (DeviceFactory.class) {
            return hwDevice;
        }
    }

    /**
     * 获取或创建硬件设备实例（带参版本）
     * 如果设备未初始化，则创建新实例；否则返回已有实例
     *
     * <p><b>处理流程：</b>
     * <ol>
     *   <li>获取类级别同步锁</li>
     *   <li>检查 hwDevice 是否为 null</li>
     *   <li>如果为 null，创建 XDmaDevManage 实例</li>
     *   <li>返回硬件设备实例</li>
     * </ol>
     *
     * <p><b>线程安全：</b>
     * <ul>
     *   <li>使用 synchronized (DeviceFactory.class) 类级别锁</li>
     *   <li>防止多线程同时创建实例</li>
     *   <li>保证单例的唯一性</li>
     * </ul>
     *
     * @param context Android应用上下文
     *                建议使用 Application Context 避免内存泄漏
     *                用于硬件设备的初始化和资源访问
     *
     * @return 硬件设备实例
     *         始终返回非 null 的 HwDevice 实例
     *
     * @example
     *   // 在 Application 或 Activity 中初始化
     *   HwDevice device = DeviceFactory.allocDevice(getApplicationContext());
     *
     *   // 后续调用返回同一实例
     *   HwDevice sameDevice = DeviceFactory.allocDevice(getApplicationContext());
     *   // device == sameDevice // true
     */
    public static HwDevice allocDevice(Context context){
        synchronized (DeviceFactory.class){
            if(hwDevice == null){
                hwDevice = new XDmaDevManage(context,2);
            }
            return hwDevice;
        }
    }
}
