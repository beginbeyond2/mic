package com.micsig.tbook.tbookscope.wavezone; // 波形显示区域包，包含示波器波形显示的核心组件，1


import androidx.annotation.IntDef; // Android整型定义注解，用于定义整型常量集合，1

import java.lang.annotation.Retention; // Java注解保留策略注解，1
import java.lang.annotation.RetentionPolicy; // Java注解保留策略枚举，1

/**
 * 工作模式接口 - 示波器波形显示区域的工作模式切换核心接口
 * 
 * 【模块定位】
 * - 所属模块：wavezone（波形显示区域模块）
 * - 核心职责：定义示波器的三种工作模式及其切换方法
 * - 架构层级：接口层，位于模式管理层和显示层之间
 * 
 * 【核心职责】
 * 1. 工作模式定义：定义YT、YTZOOM、XY三种工作模式常量
 * 2. 模式切换接口：提供统一的工作模式切换方法
 * 3. 模式标识注解：使用@IntDef注解约束模式参数的取值范围
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │            WorkModeManage（工作模式管理器）                   │
 * │                   (模式管理核心)                             │
 * │  - 管理当前工作模式                                          │
 * │  - 协调模式切换流程                                          │
 * │  - 控制显示组件状态                                          │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 实现接口
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  IWorkMode接口                               │
 * │                 (工作模式定义接口)                            │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  工作模式常量定义                                     │  │
 * │  │  - WorkMode_None  无模式                             │  │
 * │  │  - WorkMode_YT    YT模式                             │  │
 * │  │  - WorkMode_YTZOOM YT缩放模式                        │  │
 * │  │  - WorkMode_XY    XY模式                             │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  模式切换方法                                         │  │
 * │  │  - switchWorkMode()  切换工作模式                    │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 实现接口
 * ┌─────────────────────────────────────────────────────────────┐
 * │         WaveZoneDisplay_YT/XY/YTZoom                        │
 * │                   (显示组件实现类)                           │
 * │  - 实现模式切换逻辑                                          │
 * │  - 更新显示状态                                              │
 * │  - 初始化对应模式                                            │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【依赖关系】
 * - 实现类：WaveZoneDisplay_YT、WaveZoneDisplay_XY、WaveZoneDisplay_YTZoom
 * - 管理类：WorkModeManage（工作模式管理器）
 * - 注解依赖：@IntDef、@Retention（Android注解库）
 * - 无外部业务依赖
 * 
 * 【使用场景】
 * 1. YT模式切换：切换到YT模式，显示时域波形
 * 2. YTZOOM模式切换：切换到YT缩放模式，显示波形和缩放窗口
 * 3. XY模式切换：切换到XY模式，显示X-Y关系图
 * 4. 模式状态管理：管理当前工作模式状态
 * 
 * 【工作模式说明】
 * 1. WorkMode_YT（YT模式）：
 *    - 时域显示模式，显示电压随时间变化的波形
 *    - 最常用的示波器显示模式
 *    - 支持触发、测量、光标等功能
 * 
 * 2. WorkMode_YTZOOM（YT缩放模式）：
 *    - YT模式的扩展，在YT波形上方显示缩放窗口
 *    - 用于观察波形的细节部分
 *    - 支持缩放区域的移动和调整
 * 
 * 3. WorkMode_XY（XY模式）：
 *    - X-Y关系显示模式，显示两个通道的关系图
 *    - 用于观察两个信号的相位关系
 *    - 常用于李萨如图形显示
 * 
 * 【数据流向】
 * 用户操作 → WorkModeManage.setWorkMode() → IWorkMode.switchWorkMode() → 显示组件更新
 * 
 * 【注解说明】
 * - @IntDef：定义整型常量集合，限制参数取值范围
 * - @Retention(RetentionPolicy.SOURCE)：注解仅在源码级别保留
 * - @WorkMode：自定义注解，用于标记工作模式参数
 * 
 * 【性能考虑】
 * - 模式切换实时性：模式切换需要快速响应
 * - 状态同步：切换时需要同步更新所有组件状态
 * - 资源管理：切换时需要正确管理OpenGL资源
 * 
 * 【注意事项】
 * 1. 模式切换前需要确认当前模式状态
 * 2. 切换时需要正确处理显示组件的生命周期
 * 3. 使用@WorkMode注解确保参数合法性
 * 4. 切换过程需要线程同步保护
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/6/15
 * @see WorkModeManage
 * @see WaveZoneDisplay_YT
 * @see WaveZoneDisplay_XY
 * @see WaveZoneDisplay_YTZoom
 */

public interface IWorkMode {
    /**
     * 无工作模式标识常量
     * 用于标识当前无有效工作模式，通常用于初始化状态
     * 取值：-1
     */
    public static final int WorkMode_None = -1; // 无工作模式标识，值为-1，1
    
    /**
     * YT工作模式标识常量
     * YT（Y-Time）模式，显示电压随时间变化的波形
     * 这是示波器最常用的显示模式
     * 取值：0x00
     */
    public static final int WorkMode_YT = 0x00; // YT工作模式标识，值为0x00，1
    
    /**
     * YT缩放工作模式标识常量
     * YTZOOM模式，在YT波形上方显示缩放窗口
     * 用于观察波形的细节部分
     * 取值：0x01
     */
    public static final int WorkMode_YTZOOM = 0x01; // YT缩放工作模式标识，值为0x01，1
    
    /**
     * XY工作模式标识常量
     * XY模式，显示两个通道的X-Y关系图
     * 用于观察两个信号的相位关系
     * 取值：0x02
     */
    public static final int WorkMode_XY = 0x02; // XY工作模式标识，值为0x02，1

    /**
     * 工作模式注解定义
     * 使用@IntDef注解定义工作模式常量集合
     * 用于约束方法参数的取值范围，确保参数合法性
     * 
     * 【注解说明】
     * - @IntDef：定义整型常量集合，限制取值范围
     * - 包含的值：WorkMode_None、WorkMode_XY、WorkMode_YT、WorkMode_YTZOOM
     * - @Retention(SOURCE)：注解仅在源码级别保留，编译后不保留
     * 
     * 【使用场景】
     * 用于标记方法参数，确保传入的工作模式值合法
     * 
     * 【使用示例】
     * public void setMode(@WorkMode int mode) {
     *     // mode只能是定义的四种模式之一
     * }
     */
    @IntDef({WorkMode_None,WorkMode_XY, WorkMode_YT, WorkMode_YTZOOM}) // 定义工作模式常量集合，限制取值范围，1
    @Retention(RetentionPolicy.SOURCE) // 设置注解保留策略为源码级别，编译后不保留，1
    public @interface WorkMode { // 工作模式注解定义，用于标记工作模式参数，1
    } // 结束WorkMode注解定义，1


    /**
     * 工作模式切换方法 - 切换示波器的工作模式
     * 
     * 【功能说明】
     * 切换示波器的工作模式，更新显示组件和相关功能状态
     * 
     * 【参数说明】
     * @param workMode 目标工作模式标识，使用@WorkMode注解约束取值范围
     *                 取值范围：WorkMode_None、WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * 
     * 【调用时机】
     * - 用户通过UI切换工作模式时调用
     * - WorkModeManage管理器切换模式时调用
     * - 初始化显示组件时调用
     * 
     * 【实现要求】
     * 1. 更新显示组件的显示状态
     * 2. 初始化对应模式的显示内容
     * 3. 同步更新相关组件的工作模式
     * 4. 处理OpenGL资源的生命周期
     * 
     * 【使用示例】
     * @Override
     * public void switchWorkMode(@WorkMode int workMode) {
     *     switch (workMode) {
     *         case WorkMode_YT:
     *             // 切换到YT模式
     *             waveGridManage.switchWorkMode(workMode);
     *             cursorManage.switchWorkMode(workMode);
     *             waveManage.switchWorkMode(workMode);
     *             break;
     *         case WorkMode_XY:
     *             // 切换到XY模式
     *             waveGridManage.switchWorkMode(workMode);
     *             cursorManage.switchWorkMode(workMode);
     *             break;
     *         case WorkMode_YTZOOM:
     *             // 切换到YT缩放模式
     *             waveGridManage.switchWorkMode(workMode);
     *             break;
     *     }
     * }
     */
    void switchWorkMode(@WorkMode int workMode); // 工作模式切换方法，参数为目标工作模式标识，1

} // 结束IWorkMode接口定义，1