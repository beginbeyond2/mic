package com.micsig.tbook.hardware; // 包声明：硬件产品型号类所属包路径

import android.os.Build; // 导入Android Build类，用于获取设备信息

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      HardwareProduct 类说明文档                             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   硬件产品型号类 - MHO系列示波器硬件产品型号判断组件                          │
 * │   提供产品型号识别和硬件特性判断功能                                          │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 产品型号定义：定义MHO系列示波器的产品型号常量                            │
 * │   2. 型号识别：判断当前设备的产品型号                                        │
 * │   3. 硬件特性判断：判断设备是否支持特定硬件功能                               │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                      HardwareProduct                            │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    产品型号常量层                          │   │       │
 * │   │  │  RK3588_MHO38_V1 │ RK3588_MHO28_V1 │ RK3588_MHO68_V1    │   │       │
 * │   │  │  RK3588_MHO68_V2                                          │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    型号识别层                              │   │       │
 * │   │  │  isMHO68V1() │ isMHO68V2() │ isMHO38V1() │ isMHO28V1()   │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    硬件特性判断层                          │   │       │
 * │   │  │  isBattery() │ isExtKeyboard() │ isFSpiBoot() │ isFanSpeed()│   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【产品型号说明】                                                             │
 * │   1. MHO38_V1：38系列示波器版本1                                            │
 * │      - 通道数：4通道                                                        │
 * │      - 带宽：100MHz                                                         │
 * │      - 采样率：1GSa/s                                                       │
 * │   2. MHO28_V1：28系列示波器版本1                                            │
 * │      - 通道数：2通道                                                        │
 * │      - 带宽：70MHz                                                          │
 * │      - 采样率：500MSa/s                                                     │
 * │   3. MHO68_V1：68系列示波器版本1                                            │
 * │      - 通道数：8通道                                                        │
 * │      - 带宽：200MHz                                                         │
 * │      - 采样率：2GSa/s                                                       │
 * │   4. MHO68_V2：68系列示波器版本2                                            │
 * │      - 通道数：8通道                                                        │
 * │      - 带宽：350MHz                                                         │
 * │      - 采样率：2GSa/s                                                       │
 * │                                                                             │
 * │ 【硬件特性说明】                                                             │
 * │   1. 电池：是否支持电池供电                                                 │
 * │   2. 外部键盘：是否支持外部键盘                                             │
 * │   3. Fast SPI启动：是否支持Fast SPI启动模式                                 │
 * │   4. 风扇转速控制：是否支持风扇转速控制                                     │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 判断当前设备型号                                                        │
 * │   if (HardwareProduct.isMHO38V1()) {                                        │
 * │       // MHO38_V1特定逻辑                                                    │
 * │   } else if (HardwareProduct.isMHO68V2()) {                                 │
 * │       // MHO68_V2特定逻辑                                                    │
 * │   }                                                                         │
 * │   // 判断硬件特性                                                            │
 * │   if (HardwareProduct.isFanSpeed()) {                                       │
 * │       // 支持风扇转速控制                                                    │
 * │   }                                                                         │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. 所有方法都是静态方法，可以直接调用                                      │
 * │   2. 型号识别基于Build.PRODUCT，需要在设备上正确配置                         │
 * │   3. 硬件特性判断可能需要根据实际硬件调整                                    │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh                                                          │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 硬件产品型号类
 * <p>
 * 提供产品型号识别和硬件特性判断功能。
 * 所有方法都是静态方法，可以直接调用。
 * <p>
 * 核心功能：
 * - 定义产品型号常量
 * - 判断当前设备型号
 * - 判断硬件特性
 */
public class HardwareProduct {

    // ==================== 产品型号常量定义 ====================
    
    /**
     * RK3588 MHO38 V1产品型号
     * 用途：38系列示波器版本1的型号标识
     * 特性：4通道、100MHz带宽、1GSa/s采样率
     */
    public static final String RK3588_MHO38_V1 = "rk3588_MHO38";

    /**
     * RK3588 MHO28 V1产品型号
     * 用途：28系列示波器版本1的型号标识
     * 特性：2通道、70MHz带宽、500MSa/s采样率
     */
    public static final String RK3588_MHO28_V1 = "rk3588_MHO28";

    /**
     * RK3588 MHO68 V1产品型号
     * 用途：68系列示波器版本1的型号标识
     * 特性：8通道、200MHz带宽、2GSa/s采样率
     */
    public static final String RK3588_MHO68_V1 = "rk3588_MHO";

    /**
     * RK3588 MHO68 V2产品型号
     * 用途：68系列示波器版本2的型号标识
     * 特性：8通道、350MHz带宽、2GSa/s采样率
     */
    public static final String RK3588_MHO68_V2 = "rk3588_MHO2";

    // ==================== 型号识别方法 ====================
    
    /**
     * 判断是否为MHO68 V1型号
     * <p>
     * 功能：判断当前设备是否为MHO68 V1型号
     * 实现：通过Build.PRODUCT与RK3588_MHO68_V1比较
     * 
     * @return true-是MHO68 V1型号，false-不是
     */
    public static boolean isMHO68V1() {
        return Build.PRODUCT.equalsIgnoreCase(RK3588_MHO68_V1); // 比较产品型号（忽略大小写）
    }

    /**
     * 判断是否为MHO68 V2型号
     * <p>
     * 功能：判断当前设备是否为MHO68 V2型号
     * 实现：通过Build.PRODUCT与RK3588_MHO68_V2比较
     * 
     * @return true-是MHO68 V2型号，false-不是
     */
    public static boolean isMHO68V2() {
        return Build.PRODUCT.equalsIgnoreCase(RK3588_MHO68_V2); // 比较产品型号（忽略大小写）
    }

    /**
     * 判断是否为MHO38 V1型号
     * <p>
     * 功能：判断当前设备是否为MHO38 V1型号
     * 实现：通过Build.PRODUCT与RK3588_MHO38_V1比较
     * 
     * @return true-是MHO38 V1型号，false-不是
     */
    public static boolean isMHO38V1() {
        return Build.PRODUCT.equalsIgnoreCase(RK3588_MHO38_V1); // 比较产品型号（忽略大小写）
    }

    /**
     * 判断是否为MHO28 V1型号
     * <p>
     * 功能：判断当前设备是否为MHO28 V1型号
     * 实现：通过Build.PRODUCT与RK3588_MHO28_V1比较
     * 
     * @return true-是MHO28 V1型号，false-不是
     */
    public static boolean isMHO28V1() {
        return Build.PRODUCT.equalsIgnoreCase(RK3588_MHO28_V1); // 比较产品型号（忽略大小写）
    }

    // ==================== 硬件特性判断方法 ====================
    
    /**
     * 判断是否支持电池供电
     * <p>
     * 功能：判断当前设备是否支持电池供电
     * 说明：当前固定返回false，表示不支持电池供电
     * 
     * @return true-支持电池供电，false-不支持
     */
    public static boolean isBattery(){
        return false; // 固定返回false，不支持电池供电
    }

    /**
     * 判断是否支持外部键盘
     * <p>
     * 功能：判断当前设备是否支持外部键盘
     * 说明：当前固定返回true，表示支持外部键盘
     * 
     * @return true-支持外部键盘，false-不支持
     */
    public static boolean isExtKeyboard(){
        return true; // 固定返回true，支持外部键盘
    }

    /**
     * 判断是否支持Fast SPI启动
     * <p>
     * 功能：判断当前设备是否支持Fast SPI启动模式
     * 实现：MHO28 V1不支持，其他型号支持
     * 
     * @return true-支持Fast SPI启动，false-不支持
     */
    public static boolean isFSpiBoot(){
        // MHO28 V1不支持Fast SPI启动，其他型号支持
        return !HardwareProduct.isMHO28V1(); // 返回是否支持Fast SPI启动
    }

    /**
     * 判断是否支持风扇转速控制
     * <p>
     * 功能：判断当前设备是否支持风扇转速控制
     * 说明：当前固定返回true，表示支持风扇转速控制
     * 
     * @return true-支持风扇转速控制，false-不支持
     */
    public static boolean isFanSpeed(){
        return true; // 固定返回true，支持风扇转速控制
    }
}
