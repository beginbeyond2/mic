package com.micsig.tbook.hardware; // 包声明：GPIO设备类所属包路径

import android.hardware.Gpio; // 导入Android GPIO硬件类

import androidx.annotation.IntDef; // 导入IntDef注解，用于定义整型常量集合

import java.lang.annotation.Retention; // 导入Retention注解
import java.lang.annotation.RetentionPolicy; // 导入RetentionPolicy枚举

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                          GpioDev 类说明文档                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   GPIO设备类 - MHO系列示波器硬件层的GPIO控制组件                              │
 * │   封装Android GPIO硬件接口，提供统一的GPIO控制方法                            │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. GPIO引脚定义：定义5组GPIO控制器（GPIO0-GPIO4），每组32个引脚             │
 * │   2. GPIO值定义：定义高电平和低电平                                          │
 * │   3. GPIO方向定义：定义输入、输出高、输出低三种方向                           │
 * │   4. GPIO上下拉定义：定义禁用、下拉、上拉三种上下拉模式                       │
 * │   5. GPIO驱动能力定义：定义4种驱动能力等级                                   │
 * │   6. GPIO控制：提供设置和获取GPIO值的方法                                    │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                         GpioDev                                  │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    GPIO引脚定义层                          │   │       │
 * │   │  │  GPIO0_A0~D7 │ GPIO1_A0~D7 │ GPIO2_A0~D7 │ ...          │   │       │
 * │   │  │  (共5组，每组32个引脚，共160个引脚)                        │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    GPIO配置常量层                          │   │       │
 * │   │  │  GPIO_VAL (值) │ GPIO_DIRECTION (方向) │ GPIO_PULL (上下拉)│   │       │
 * │   │  │  GPIO_DIRVE (驱动能力)                                    │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    GPIO控制层                              │   │       │
 * │   │  │  mGpio (Android GPIO对象) │ getVal() │ setVal()           │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【GPIO引脚编号规则】                                                         │
 * │   GPIO编号格式：GPIO[组号]_[端口][引脚号]                                    │
 * │   - 组号：0-4（共5组GPIO控制器）                                             │
 * │   - 端口：A、B、C、D（每组4个端口，每个端口8个引脚）                          │
 * │   - 引脚号：0-7（每个端口8个引脚）                                           │
 * │   编号计算公式：组号*32 + 端口偏移 + 引脚号                                  │
 * │   端口偏移：A=0, B=8, C=16, D=24                                            │
 * │                                                                             │
 * │ 【GPIO引脚分组】                                                             │
 * │   GPIO0：引脚0-31（组号0）                                                  │
 * │   GPIO1：引脚32-63（组号1）                                                 │
 * │   GPIO2：引脚64-95（组号2）                                                 │
 * │   GPIO3：引脚96-127（组号3）                                                │
 * │   GPIO4：引脚128-159（组号4）                                               │
 * │                                                                             │
 * │ 【注解说明】                                                                 │
 * │   @IntDef：定义整型常量集合，用于类型安全检查                                │
 * │   @Retention(RetentionPolicy.SOURCE)：注解仅在源码级别保留                  │
 * │   @GPIOIdx：GPIO引脚索引注解                                                │
 * │   @GPIO_VAL：GPIO值注解                                                    │
 * │   @GPIO_PULL：GPIO上下拉注解                                                │
 * │   @GPIO_DIRECTION：GPIO方向注解                                             │
 * │   @GPIO_DIRVE：GPIO驱动能力注解                                             │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 获取GPIO设备                                                           │
 * │   GpioDev gpio = HwGpioManager.getInstance().getGpioDev(GPIO0_A0);         │
 * │   // 设置GPIO为高电平                                                       │
 * │   gpio.setVal(GpioDev.GPIO_VAL_HIGH);                                      │
 * │   // 获取GPIO值                                                             │
 * │   int value = gpio.getVal();                                               │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. GPIO引脚编号从0开始，最大为159                                         │
 * │   2. 使用注解可以确保编译时类型安全                                          │
 * │   3. GPIO方向和上下拉在构造函数中设置，之后不可更改                          │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by zhuzh on 2018/3/9                                               │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * GPIO设备类
 * <p>
 * 封装Android GPIO硬件接口，提供统一的GPIO控制方法。
 * 定义了GPIO引脚编号、值、方向、上下拉、驱动能力等常量。
 * <p>
 * 核心功能：
 * - 定义GPIO引脚编号（共160个引脚）
 * - 定义GPIO配置常量（值、方向、上下拉、驱动能力）
 * - 提供GPIO值设置和获取方法
 */
public class GpioDev {

    // ==================== GPIO引脚索引注解定义 ====================
    
    /**
     * GPIO引脚索引注解
     * 用途：定义所有有效的GPIO引脚编号，用于类型安全检查
     * 范围：GPIO0_A0 ~ GPIO4_D7（共160个引脚）
     */
    @IntDef({
            GPIO0_A0, // GPIO0组A端口引脚0
            GPIO0_A1, // GPIO0组A端口引脚1
            GPIO0_A2, // GPIO0组A端口引脚2
            GPIO0_A3, // GPIO0组A端口引脚3
            GPIO0_A4, // GPIO0组A端口引脚4
            GPIO0_A5, // GPIO0组A端口引脚5
            GPIO0_A6, // GPIO0组A端口引脚6
            GPIO0_A7, // GPIO0组A端口引脚7
            GPIO0_B0, // GPIO0组B端口引脚0
            GPIO0_B1, // GPIO0组B端口引脚1
            GPIO0_B2 , // GPIO0组B端口引脚2
            GPIO0_B3 , // GPIO0组B端口引脚3
            GPIO0_B4 , // GPIO0组B端口引脚4
            GPIO0_B5 , // GPIO0组B端口引脚5
            GPIO0_B6 , // GPIO0组B端口引脚6
            GPIO0_B7 , // GPIO0组B端口引脚7
            GPIO0_C0 , // GPIO0组C端口引脚0
            GPIO0_C1 , // GPIO0组C端口引脚1
            GPIO0_C2 , // GPIO0组C端口引脚2
            GPIO0_C3 , // GPIO0组C端口引脚3
            GPIO0_C4 , // GPIO0组C端口引脚4
            GPIO0_C5 , // GPIO0组C端口引脚5
            GPIO0_C6 , // GPIO0组C端口引脚6
            GPIO0_C7 , // GPIO0组C端口引脚7
            GPIO0_D0 , // GPIO0组D端口引脚0
            GPIO0_D1 , // GPIO0组D端口引脚1
            GPIO0_D2 , // GPIO0组D端口引脚2
            GPIO0_D3 , // GPIO0组D端口引脚3
            GPIO0_D4 , // GPIO0组D端口引脚4
            GPIO0_D5 , // GPIO0组D端口引脚5
            GPIO0_D6 , // GPIO0组D端口引脚6
            GPIO0_D7 , // GPIO0组D端口引脚7
            GPIO1_A0, // GPIO1组A端口引脚0
            GPIO1_A1, // GPIO1组A端口引脚1
            GPIO1_A2, // GPIO1组A端口引脚2
            GPIO1_A3, // GPIO1组A端口引脚3
            GPIO1_A4, // GPIO1组A端口引脚4
            GPIO1_A5, // GPIO1组A端口引脚5
            GPIO1_A6, // GPIO1组A端口引脚6
            GPIO1_A7, // GPIO1组A端口引脚7
            GPIO1_B0, // GPIO1组B端口引脚0
            GPIO1_B1, // GPIO1组B端口引脚1
            GPIO1_B2 , // GPIO1组B端口引脚2
            GPIO1_B3 , // GPIO1组B端口引脚3
            GPIO1_B4 , // GPIO1组B端口引脚4
            GPIO1_B5 , // GPIO1组B端口引脚5
            GPIO1_B6 , // GPIO1组B端口引脚6
            GPIO1_B7 , // GPIO1组B端口引脚7
            GPIO1_C0 , // GPIO1组C端口引脚0
            GPIO1_C1 , // GPIO1组C端口引脚1
            GPIO1_C2 , // GPIO1组C端口引脚2
            GPIO1_C3 , // GPIO1组C端口引脚3
            GPIO1_C4 , // GPIO1组C端口引脚4
            GPIO1_C5 , // GPIO1组C端口引脚5
            GPIO1_C6 , // GPIO1组C端口引脚6
            GPIO1_C7 , // GPIO1组C端口引脚7
            GPIO1_D0 , // GPIO1组D端口引脚0
            GPIO1_D1 , // GPIO1组D端口引脚1
            GPIO1_D2 , // GPIO1组D端口引脚2
            GPIO1_D3 , // GPIO1组D端口引脚3
            GPIO1_D4 , // GPIO1组D端口引脚4
            GPIO1_D5 , // GPIO1组D端口引脚5
            GPIO1_D6 , // GPIO1组D端口引脚6
            GPIO1_D7 , // GPIO1组D端口引脚7
            GPIO2_A0, // GPIO2组A端口引脚0
            GPIO2_A1, // GPIO2组A端口引脚1
            GPIO2_A2, // GPIO2组A端口引脚2
            GPIO2_A3, // GPIO2组A端口引脚3
            GPIO2_A4, // GPIO2组A端口引脚4
            GPIO2_A5, // GPIO2组A端口引脚5
            GPIO2_A6, // GPIO2组A端口引脚6
            GPIO2_A7, // GPIO2组A端口引脚7
            GPIO2_B0, // GPIO2组B端口引脚0
            GPIO2_B1, // GPIO2组B端口引脚1
            GPIO2_B2 , // GPIO2组B端口引脚2
            GPIO2_B3 , // GPIO2组B端口引脚3
            GPIO2_B4 , // GPIO2组B端口引脚4
            GPIO2_B5 , // GPIO2组B端口引脚5
            GPIO2_B6 , // GPIO2组B端口引脚6
            GPIO2_B7 , // GPIO2组B端口引脚7
            GPIO2_C0 , // GPIO2组C端口引脚0
            GPIO2_C1 , // GPIO2组C端口引脚1
            GPIO2_C2 , // GPIO2组C端口引脚2
            GPIO2_C3 , // GPIO2组C端口引脚3
            GPIO2_C4 , // GPIO2组C端口引脚4
            GPIO2_C5 , // GPIO2组C端口引脚5
            GPIO2_C6 , // GPIO2组C端口引脚6
            GPIO2_C7 , // GPIO2组C端口引脚7
            GPIO2_D0 , // GPIO2组D端口引脚0
            GPIO2_D1 , // GPIO2组D端口引脚1
            GPIO2_D2 , // GPIO2组D端口引脚2
            GPIO2_D3 , // GPIO2组D端口引脚3
            GPIO2_D4 , // GPIO2组D端口引脚4
            GPIO2_D5 , // GPIO2组D端口引脚5
            GPIO2_D6 , // GPIO2组D端口引脚6
            GPIO2_D7 , // GPIO2组D端口引脚7
            GPIO3_A0, // GPIO3组A端口引脚0
            GPIO3_A1, // GPIO3组A端口引脚1
            GPIO3_A2, // GPIO3组A端口引脚2
            GPIO3_A3, // GPIO3组A端口引脚3
            GPIO3_A4, // GPIO3组A端口引脚4
            GPIO3_A5, // GPIO3组A端口引脚5
            GPIO3_A6, // GPIO3组A端口引脚6
            GPIO3_A7, // GPIO3组A端口引脚7
            GPIO3_B0, // GPIO3组B端口引脚0
            GPIO3_B1, // GPIO3组B端口引脚1
            GPIO3_B2 , // GPIO3组B端口引脚2
            GPIO3_B3 , // GPIO3组B端口引脚3
            GPIO3_B4 , // GPIO3组B端口引脚4
            GPIO3_B5 , // GPIO3组B端口引脚5
            GPIO3_B6 , // GPIO3组B端口引脚6
            GPIO3_B7 , // GPIO3组B端口引脚7
            GPIO3_C0 , // GPIO3组C端口引脚0
            GPIO3_C1 , // GPIO3组C端口引脚1
            GPIO3_C2 , // GPIO3组C端口引脚2
            GPIO3_C3 , // GPIO3组C端口引脚3
            GPIO3_C4 , // GPIO3组C端口引脚4
            GPIO3_C5 , // GPIO3组C端口引脚5
            GPIO3_C6 , // GPIO3组C端口引脚6
            GPIO3_C7 , // GPIO3组C端口引脚7
            GPIO3_D0 , // GPIO3组D端口引脚0
            GPIO3_D1 , // GPIO3组D端口引脚1
            GPIO3_D2 , // GPIO3组D端口引脚2
            GPIO3_D3 , // GPIO3组D端口引脚3
            GPIO3_D4 , // GPIO3组D端口引脚4
            GPIO3_D5 , // GPIO3组D端口引脚5
            GPIO3_D6 , // GPIO3组D端口引脚6
            GPIO3_D7 , // GPIO3组D端口引脚7
            GPIO4_A0, // GPIO4组A端口引脚0
            GPIO4_A1, // GPIO4组A端口引脚1
            GPIO4_A2, // GPIO4组A端口引脚2
            GPIO4_A3, // GPIO4组A端口引脚3
            GPIO4_A4, // GPIO4组A端口引脚4
            GPIO4_A5, // GPIO4组A端口引脚5
            GPIO4_A6, // GPIO4组A端口引脚6
            GPIO4_A7, // GPIO4组A端口引脚7
            GPIO4_B0, // GPIO4组B端口引脚0
            GPIO4_B1, // GPIO4组B端口引脚1
            GPIO4_B2 , // GPIO4组B端口引脚2
            GPIO4_B3 , // GPIO4组B端口引脚3
            GPIO4_B4 , // GPIO4组B端口引脚4
            GPIO4_B5 , // GPIO4组B端口引脚5
            GPIO4_B6 , // GPIO4组B端口引脚6
            GPIO4_B7 , // GPIO4组B端口引脚7
            GPIO4_C0 , // GPIO4组C端口引脚0
            GPIO4_C1 , // GPIO4组C端口引脚1
            GPIO4_C2 , // GPIO4组C端口引脚2
            GPIO4_C3 , // GPIO4组C端口引脚3
            GPIO4_C4 , // GPIO4组C端口引脚4
            GPIO4_C5 , // GPIO4组C端口引脚5
            GPIO4_C6 , // GPIO4组C端口引脚6
            GPIO4_C7 , // GPIO4组C端口引脚7
            GPIO4_D0 , // GPIO4组D端口引脚0
            GPIO4_D1 , // GPIO4组D端口引脚1
            GPIO4_D2 , // GPIO4组D端口引脚2
            GPIO4_D3 , // GPIO4组D端口引脚3
            GPIO4_D4 , // GPIO4组D端口引脚4
            GPIO4_D5 , // GPIO4组D端口引脚5
            GPIO4_D6 , // GPIO4组D端口引脚6
            GPIO4_D7 , // GPIO4组D端口引脚7
    })
    @Retention(RetentionPolicy.SOURCE) // 注解仅在源码级别保留
    public @interface GPIOIdx {} // GPIO引脚索引注解定义

    // ==================== GPIO0组引脚定义（引脚0-31） ====================
    
    // GPIO0组A端口引脚（引脚0-7）
    public static final int GPIO0_A0 = 0; // GPIO0组A端口引脚0，编号0
    public static final int GPIO0_A1 = 1; // GPIO0组A端口引脚1，编号1
    public static final int GPIO0_A2 = 2; // GPIO0组A端口引脚2，编号2
    public static final int GPIO0_A3 = 3; // GPIO0组A端口引脚3，编号3
    public static final int GPIO0_A4 = 4; // GPIO0组A端口引脚4，编号4
    public static final int GPIO0_A5 = 5; // GPIO0组A端口引脚5，编号5
    public static final int GPIO0_A6 = 6; // GPIO0组A端口引脚6，编号6
    public static final int GPIO0_A7 = 7; // GPIO0组A端口引脚7，编号7
    
    // GPIO0组B端口引脚（引脚8-15）
    public static final int GPIO0_B0 = 8; // GPIO0组B端口引脚0，编号8
    public static final int GPIO0_B1 = 9; // GPIO0组B端口引脚1，编号9
    public static final int GPIO0_B2 = 10; // GPIO0组B端口引脚2，编号10
    public static final int GPIO0_B3 = 11; // GPIO0组B端口引脚3，编号11
    public static final int GPIO0_B4 = 12; // GPIO0组B端口引脚4，编号12
    public static final int GPIO0_B5 = 13; // GPIO0组B端口引脚5，编号13
    public static final int GPIO0_B6 = 14; // GPIO0组B端口引脚6，编号14
    public static final int GPIO0_B7 = 15; // GPIO0组B端口引脚7，编号15
    
    // GPIO0组C端口引脚（引脚16-23）
    public static final int GPIO0_C0 = 16; // GPIO0组C端口引脚0，编号16
    public static final int GPIO0_C1 = 17; // GPIO0组C端口引脚1，编号17
    public static final int GPIO0_C2 = 18; // GPIO0组C端口引脚2，编号18
    public static final int GPIO0_C3 = 19; // GPIO0组C端口引脚3，编号19
    public static final int GPIO0_C4 = 20; // GPIO0组C端口引脚4，编号20
    public static final int GPIO0_C5 = 21; // GPIO0组C端口引脚5，编号21
    public static final int GPIO0_C6 = 22; // GPIO0组C端口引脚6，编号22
    public static final int GPIO0_C7 = 23; // GPIO0组C端口引脚7，编号23
    
    // GPIO0组D端口引脚（引脚24-31）
    public static final int GPIO0_D0 = 24; // GPIO0组D端口引脚0，编号24
    public static final int GPIO0_D1 = 25; // GPIO0组D端口引脚1，编号25
    public static final int GPIO0_D2 = 26; // GPIO0组D端口引脚2，编号26
    public static final int GPIO0_D3 = 27; // GPIO0组D端口引脚3，编号27
    public static final int GPIO0_D4 = 28; // GPIO0组D端口引脚4，编号28
    public static final int GPIO0_D5 = 29; // GPIO0组D端口引脚5，编号29
    public static final int GPIO0_D6 = 30; // GPIO0组D端口引脚6，编号30
    public static final int GPIO0_D7 = 31; // GPIO0组D端口引脚7，编号31
    
    // ==================== GPIO1组引脚定义（引脚32-63） ====================
    
    // GPIO1组A端口引脚（引脚32-39）
    public static final int GPIO1_A0 = 32 + 0; // GPIO1组A端口引脚0，编号32
    public static final int GPIO1_A1 = 32 + 1; // GPIO1组A端口引脚1，编号33
    public static final int GPIO1_A2 = 32 + 2; // GPIO1组A端口引脚2，编号34
    public static final int GPIO1_A3 = 32 + 3; // GPIO1组A端口引脚3，编号35
    public static final int GPIO1_A4 = 32 + 4; // GPIO1组A端口引脚4，编号36
    public static final int GPIO1_A5 = 32 + 5; // GPIO1组A端口引脚5，编号37
    public static final int GPIO1_A6 = 32 + 6; // GPIO1组A端口引脚6，编号38
    public static final int GPIO1_A7 = 32 + 7; // GPIO1组A端口引脚7，编号39
    
    // GPIO1组B端口引脚（引脚40-47）
    public static final int GPIO1_B0 = 32 + 8; // GPIO1组B端口引脚0，编号40
    public static final int GPIO1_B1 = 32 + 9; // GPIO1组B端口引脚1，编号41
    public static final int GPIO1_B2 = 32 + 10; // GPIO1组B端口引脚2，编号42
    public static final int GPIO1_B3 = 32 + 11; // GPIO1组B端口引脚3，编号43
    public static final int GPIO1_B4 = 32 + 12; // GPIO1组B端口引脚4，编号44
    public static final int GPIO1_B5 = 32 + 13; // GPIO1组B端口引脚5，编号45
    public static final int GPIO1_B6 = 32 + 14; // GPIO1组B端口引脚6，编号46
    public static final int GPIO1_B7 = 32 + 15; // GPIO1组B端口引脚7，编号47
    
    // GPIO1组C端口引脚（引脚48-55）
    public static final int GPIO1_C0 = 32 + 16; // GPIO1组C端口引脚0，编号48
    public static final int GPIO1_C1 = 32 + 17; // GPIO1组C端口引脚1，编号49
    public static final int GPIO1_C2 = 32 + 18; // GPIO1组C端口引脚2，编号50
    public static final int GPIO1_C3 = 32 + 19; // GPIO1组C端口引脚3，编号51
    public static final int GPIO1_C4 = 32 + 20; // GPIO1组C端口引脚4，编号52
    public static final int GPIO1_C5 = 32 + 21; // GPIO1组C端口引脚5，编号53
    public static final int GPIO1_C6 = 32 + 22; // GPIO1组C端口引脚6，编号54
    public static final int GPIO1_C7 = 32 + 23; // GPIO1组C端口引脚7，编号55
    
    // GPIO1组D端口引脚（引脚56-63）
    public static final int GPIO1_D0 = 32 + 24; // GPIO1组D端口引脚0，编号56
    public static final int GPIO1_D1 = 32 + 25; // GPIO1组D端口引脚1，编号57
    public static final int GPIO1_D2 = 32 + 26; // GPIO1组D端口引脚2，编号58
    public static final int GPIO1_D3 = 32 + 27; // GPIO1组D端口引脚3，编号59
    public static final int GPIO1_D4 = 32 + 28; // GPIO1组D端口引脚4，编号60
    public static final int GPIO1_D5 = 32 + 29; // GPIO1组D端口引脚5，编号61
    public static final int GPIO1_D6 = 32 + 30; // GPIO1组D端口引脚6，编号62
    public static final int GPIO1_D7 = 32 + 31; // GPIO1组D端口引脚7，编号63
    
    // ==================== GPIO2组引脚定义（引脚64-95） ====================
    
    // GPIO2组A端口引脚（引脚64-71）
    public static final int GPIO2_A0 = 64 + 0; // GPIO2组A端口引脚0，编号64
    public static final int GPIO2_A1 = 64 + 1; // GPIO2组A端口引脚1，编号65
    public static final int GPIO2_A2 = 64 + 2; // GPIO2组A端口引脚2，编号66
    public static final int GPIO2_A3 = 64 + 3; // GPIO2组A端口引脚3，编号67
    public static final int GPIO2_A4 = 64 + 4; // GPIO2组A端口引脚4，编号68
    public static final int GPIO2_A5 = 64 + 5; // GPIO2组A端口引脚5，编号69
    public static final int GPIO2_A6 = 64 + 6; // GPIO2组A端口引脚6，编号70
    public static final int GPIO2_A7 = 64 + 7; // GPIO2组A端口引脚7，编号71
    
    // GPIO2组B端口引脚（引脚72-79）
    public static final int GPIO2_B0 = 64 + 8; // GPIO2组B端口引脚0，编号72
    public static final int GPIO2_B1 = 64 + 9; // GPIO2组B端口引脚1，编号73
    public static final int GPIO2_B2 = 64 + 10; // GPIO2组B端口引脚2，编号74
    public static final int GPIO2_B3 = 64 + 11; // GPIO2组B端口引脚3，编号75
    public static final int GPIO2_B4 = 64 + 12; // GPIO2组B端口引脚4，编号76
    public static final int GPIO2_B5 = 64 + 13; // GPIO2组B端口引脚5，编号77
    public static final int GPIO2_B6 = 64 + 14; // GPIO2组B端口引脚6，编号78
    public static final int GPIO2_B7 = 64 + 15; // GPIO2组B端口引脚7，编号79
    
    // GPIO2组C端口引脚（引脚80-87）
    public static final int GPIO2_C0 = 64 + 16; // GPIO2组C端口引脚0，编号80
    public static final int GPIO2_C1 = 64 + 17; // GPIO2组C端口引脚1，编号81
    public static final int GPIO2_C2 = 64 + 18; // GPIO2组C端口引脚2，编号82
    public static final int GPIO2_C3 = 64 + 19; // GPIO2组C端口引脚3，编号83
    public static final int GPIO2_C4 = 64 + 20; // GPIO2组C端口引脚4，编号84
    public static final int GPIO2_C5 = 64 + 21; // GPIO2组C端口引脚5，编号85
    public static final int GPIO2_C6 = 64 + 22; // GPIO2组C端口引脚6，编号86
    public static final int GPIO2_C7 = 64 + 23; // GPIO2组C端口引脚7，编号87
    
    // GPIO2组D端口引脚（引脚88-95）
    public static final int GPIO2_D0 = 64 + 24; // GPIO2组D端口引脚0，编号88
    public static final int GPIO2_D1 = 64 + 25; // GPIO2组D端口引脚1，编号89
    public static final int GPIO2_D2 = 64 + 26; // GPIO2组D端口引脚2，编号90
    public static final int GPIO2_D3 = 64 + 27; // GPIO2组D端口引脚3，编号91
    public static final int GPIO2_D4 = 64 + 28; // GPIO2组D端口引脚4，编号92
    public static final int GPIO2_D5 = 64 + 29; // GPIO2组D端口引脚5，编号93
    public static final int GPIO2_D6 = 64 + 30; // GPIO2组D端口引脚6，编号94
    public static final int GPIO2_D7 = 64 + 31; // GPIO2组D端口引脚7，编号95
    
    // ==================== GPIO3组引脚定义（引脚96-127） ====================
    
    // GPIO3组A端口引脚（引脚96-103）
    public static final int GPIO3_A0 = 96 + 0; // GPIO3组A端口引脚0，编号96
    public static final int GPIO3_A1 = 96 + 1; // GPIO3组A端口引脚1，编号97
    public static final int GPIO3_A2 = 96 + 2; // GPIO3组A端口引脚2，编号98
    public static final int GPIO3_A3 = 96 + 3; // GPIO3组A端口引脚3，编号99
    public static final int GPIO3_A4 = 96 + 4; // GPIO3组A端口引脚4，编号100
    public static final int GPIO3_A5 = 96 + 5; // GPIO3组A端口引脚5，编号101
    public static final int GPIO3_A6 = 96 + 6; // GPIO3组A端口引脚6，编号102
    public static final int GPIO3_A7 = 96 + 7; // GPIO3组A端口引脚7，编号103
    
    // GPIO3组B端口引脚（引脚104-111）
    public static final int GPIO3_B0 = 96 + 8; // GPIO3组B端口引脚0，编号104
    public static final int GPIO3_B1 = 96 + 9; // GPIO3组B端口引脚1，编号105
    public static final int GPIO3_B2 = 96 + 10; // GPIO3组B端口引脚2，编号106
    public static final int GPIO3_B3 = 96 + 11; // GPIO3组B端口引脚3，编号107
    public static final int GPIO3_B4 = 96 + 12; // GPIO3组B端口引脚4，编号108
    public static final int GPIO3_B5 = 96 + 13; // GPIO3组B端口引脚5，编号109
    public static final int GPIO3_B6 = 96 + 14; // GPIO3组B端口引脚6，编号110
    public static final int GPIO3_B7 = 96 + 15; // GPIO3组B端口引脚7，编号111
    
    // GPIO3组C端口引脚（引脚112-119）
    public static final int GPIO3_C0 = 96 + 16; // GPIO3组C端口引脚0，编号112
    public static final int GPIO3_C1 = 96 + 17; // GPIO3组C端口引脚1，编号113
    public static final int GPIO3_C2 = 96 + 18; // GPIO3组C端口引脚2，编号114
    public static final int GPIO3_C3 = 96 + 19; // GPIO3组C端口引脚3，编号115
    public static final int GPIO3_C4 = 96 + 20; // GPIO3组C端口引脚4，编号116
    public static final int GPIO3_C5 = 96 + 21; // GPIO3组C端口引脚5，编号117
    public static final int GPIO3_C6 = 96 + 22; // GPIO3组C端口引脚6，编号118
    public static final int GPIO3_C7 = 96 + 23; // GPIO3组C端口引脚7，编号119
    
    // GPIO3组D端口引脚（引脚120-127）
    public static final int GPIO3_D0 = 96 + 24; // GPIO3组D端口引脚0，编号120
    public static final int GPIO3_D1 = 96 + 25; // GPIO3组D端口引脚1，编号121
    public static final int GPIO3_D2 = 96 + 26; // GPIO3组D端口引脚2，编号122
    public static final int GPIO3_D3 = 96 + 27; // GPIO3组D端口引脚3，编号123
    public static final int GPIO3_D4 = 96 + 28; // GPIO3组D端口引脚4，编号124
    public static final int GPIO3_D5 = 96 + 29; // GPIO3组D端口引脚5，编号125
    public static final int GPIO3_D6 = 96 + 30; // GPIO3组D端口引脚6，编号126
    public static final int GPIO3_D7 = 96 + 31; // GPIO3组D端口引脚7，编号127
    
    // ==================== GPIO4组引脚定义（引脚128-159） ====================
    
    // GPIO4组A端口引脚（引脚128-135）
    public static final int GPIO4_A0 = 128 + 0; // GPIO4组A端口引脚0，编号128
    public static final int GPIO4_A1 = 128 + 1; // GPIO4组A端口引脚1，编号129
    public static final int GPIO4_A2 = 128 + 2; // GPIO4组A端口引脚2，编号130
    public static final int GPIO4_A3 = 128 + 3; // GPIO4组A端口引脚3，编号131
    public static final int GPIO4_A4 = 128 + 4; // GPIO4组A端口引脚4，编号132
    public static final int GPIO4_A5 = 128 + 5; // GPIO4组A端口引脚5，编号133
    public static final int GPIO4_A6 = 128 + 6; // GPIO4组A端口引脚6，编号134
    public static final int GPIO4_A7 = 128 + 7; // GPIO4组A端口引脚7，编号135
    
    // GPIO4组B端口引脚（引脚136-143）
    public static final int GPIO4_B0 = 128 + 8; // GPIO4组B端口引脚0，编号136
    public static final int GPIO4_B1 = 128 + 9; // GPIO4组B端口引脚1，编号137
    public static final int GPIO4_B2 = 128 + 10; // GPIO4组B端口引脚2，编号138
    public static final int GPIO4_B3 = 128 + 11; // GPIO4组B端口引脚3，编号139
    public static final int GPIO4_B4 = 128 + 12; // GPIO4组B端口引脚4，编号140
    public static final int GPIO4_B5 = 128 + 13; // GPIO4组B端口引脚5，编号141
    public static final int GPIO4_B6 = 128 + 14; // GPIO4组B端口引脚6，编号142
    public static final int GPIO4_B7 = 128 + 15; // GPIO4组B端口引脚7，编号143
    
    // GPIO4组C端口引脚（引脚144-151）
    public static final int GPIO4_C0 = 128 + 16; // GPIO4组C端口引脚0，编号144
    public static final int GPIO4_C1 = 128 + 17; // GPIO4组C端口引脚1，编号145
    public static final int GPIO4_C2 = 128 + 18; // GPIO4组C端口引脚2，编号146
    public static final int GPIO4_C3 = 128 + 19; // GPIO4组C端口引脚3，编号147
    public static final int GPIO4_C4 = 128 + 20; // GPIO4组C端口引脚4，编号148
    public static final int GPIO4_C5 = 128 + 21; // GPIO4组C端口引脚5，编号149
    public static final int GPIO4_C6 = 128 + 22; // GPIO4组C端口引脚6，编号150
    public static final int GPIO4_C7 = 128 + 23; // GPIO4组C端口引脚7，编号151
    
    // GPIO4组D端口引脚（引脚152-159）
    public static final int GPIO4_D0 = 128 + 24; // GPIO4组D端口引脚0，编号152
    public static final int GPIO4_D1 = 128 + 25; // GPIO4组D端口引脚1，编号153
    public static final int GPIO4_D2 = 128 + 26; // GPIO4组D端口引脚2，编号154
    public static final int GPIO4_D3 = 128 + 27; // GPIO4组D端口引脚3，编号155
    public static final int GPIO4_D4 = 128 + 28; // GPIO4组D端口引脚4，编号156
    public static final int GPIO4_D5 = 128 + 29; // GPIO4组D端口引脚5，编号157
    public static final int GPIO4_D6 = 128 + 30; // GPIO4组D端口引脚6，编号158
    public static final int GPIO4_D7 = 128 + 31; // GPIO4组D端口引脚7，编号159

    // ==================== GPIO值注解定义 ====================
    
    /**
     * GPIO值注解
     * 用途：定义GPIO的有效值（高电平和低电平）
     */
    @IntDef({GPIO_VAL_LOW,GPIO_VAL_HIGH}) // 定义GPIO值常量集合
    @Retention(RetentionPolicy.SOURCE) // 注解仅在源码级别保留
    public @interface GPIO_VAL {} // GPIO值注解定义

    // ==================== GPIO值常量定义 ====================
    
    /**
     * GPIO低电平
     * 用途：设置GPIO输出低电平或读取到低电平
     */
    public static final int GPIO_VAL_LOW = 0; // GPIO低电平值

    /**
     * GPIO高电平
     * 用途：设置GPIO输出高电平或读取到高电平
     */
    public static final int GPIO_VAL_HIGH = 1; // GPIO高电平值

    // ==================== GPIO上下拉注解定义 ====================
    
    /**
     * GPIO上下拉注解
     * 用途：定义GPIO的有效上下拉模式
     */
    @IntDef({GPIO_PULL_DISABLE,GPIO_PULL_DOWN,GPIO_PULL_UP}) // 定义GPIO上下拉常量集合
    @Retention(RetentionPolicy.SOURCE) // 注解仅在源码级别保留
    public @interface GPIO_PULL {} // GPIO上下拉注解定义
    
    // ==================== GPIO上下拉常量定义 ====================
    
    /**
     * GPIO禁用上下拉
     * 用途：GPIO引脚不使用上下拉电阻
     */
    public static final int GPIO_PULL_DISABLE = 0; // 禁用上下拉

    /**
     * GPIO下拉
     * 用途：GPIO引脚使用下拉电阻，默认状态为低电平
     */
    public static final int GPIO_PULL_DOWN = 1; // 下拉

    /**
     * GPIO上拉
     * 用途：GPIO引脚使用上拉电阻，默认状态为高电平
     */
    public static final int GPIO_PULL_UP = 2; // 上拉

    // ==================== GPIO方向注解定义 ====================
    
    /**
     * GPIO方向注解
     * 用途：定义GPIO的有效方向
     */
    @IntDef({GPIO_DIRECTION_OUT_LOW,GPIO_DIRECTION_OUT_HIGH,GPIO_DIRECTION_IN}) // 定义GPIO方向常量集合
    @Retention(RetentionPolicy.SOURCE) // 注解仅在源码级别保留
    public @interface GPIO_DIRECTION {} // GPIO方向注解定义
    
    // ==================== GPIO方向常量定义 ====================
    
    /**
     * GPIO输出低电平
     * 用途：设置GPIO为输出模式，初始值为低电平
     */
    public static final int GPIO_DIRECTION_OUT_LOW = 0; // 输出低电平

    /**
     * GPIO输出高电平
     * 用途：设置GPIO为输出模式，初始值为高电平
     */
    public static final int GPIO_DIRECTION_OUT_HIGH = 1; // 输出高电平

    /**
     * GPIO输入
     * 用途：设置GPIO为输入模式
     */
    public static final int GPIO_DIRECTION_IN = 2; // 输入

    // ==================== GPIO驱动能力注解定义 ====================
    
    /**
     * GPIO驱动能力注解
     * 用途：定义GPIO的有效驱动能力等级
     */
    @IntDef({GPIO_DIRVE_0,GPIO_DIRVE_1,GPIO_DIRVE_2,GPIO_DIRVE_3}) // 定义GPIO驱动能力常量集合
    @Retention(RetentionPolicy.SOURCE) // 注解仅在源码级别保留
    public @interface GPIO_DIRVE {} // GPIO驱动能力注解定义
    
    // ==================== GPIO驱动能力常量定义 ====================
    
    /**
     * GPIO驱动能力等级0
     * 用途：最低驱动能力
     */
    public static final int GPIO_DIRVE_0 = 0; // 驱动能力等级0

    /**
     * GPIO驱动能力等级1
     * 用途：低驱动能力
     */
    public static final int GPIO_DIRVE_1 = 1; // 驱动能力等级1

    /**
     * GPIO驱动能力等级2
     * 用途：高驱动能力
     */
    public static final int GPIO_DIRVE_2 = 2; // 驱动能力等级2

    /**
     * GPIO驱动能力等级3
     * 用途：最高驱动能力
     */
    public static final int GPIO_DIRVE_3 = 3; // 驱动能力等级3

    // ==================== 成员变量定义 ====================
    
    /**
     * Android GPIO硬件对象
     * 用途：封装底层的GPIO硬件操作接口
     */
    private Gpio mGpio = null;

    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * <p>
     * 功能：创建GPIO设备实例，配置GPIO的方向和上下拉
     * 
     * @param gpio Android GPIO硬件对象
     * @param dir GPIO方向（GPIO_DIRECTION_OUT_LOW/GPIO_DIRECTION_OUT_HIGH/GPIO_DIRECTION_IN）
     * @param pull GPIO上下拉（GPIO_PULL_DISABLE/GPIO_PULL_DOWN/GPIO_PULL_UP）
     */
    public GpioDev(Gpio gpio, @GPIO_DIRECTION int dir, @GPIO_PULL int pull){
        mGpio = gpio; // 保存GPIO硬件对象
        mGpio.setPull(pull); // 设置GPIO上下拉模式
        mGpio.setDirection(dir); // 设置GPIO方向
    }

    // ==================== GPIO值操作方法 ====================
    
    /**
     * 获取GPIO值
     * <p>
     * 功能：读取GPIO的当前电平值
     * 
     * @return GPIO值（GPIO_VAL_LOW/GPIO_VAL_HIGH）
     */
    public int getVal(){
        return mGpio.getValue(); // 返回GPIO当前值
    }

    /**
     * 设置GPIO值
     * <p>
     * 功能：设置GPIO的输出电平值
     * 说明：仅对输出模式的GPIO有效
     * 
     * @param val GPIO值（GPIO_VAL_LOW/GPIO_VAL_HIGH）
     */
    public void setVal(@GPIO_VAL int val){
        mGpio.setValue(val); // 设置GPIO输出值
    }
}
