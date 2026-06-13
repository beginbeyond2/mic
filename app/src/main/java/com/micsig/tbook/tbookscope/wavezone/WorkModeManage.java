package com.micsig.tbook.tbookscope.wavezone; // 波形显示区域包，包含示波器波形显示的核心组件，1

import android.animation.Animator; // Android动画类，用于动画监听器接口，1
import android.content.Context; // Android上下文类，用于访问应用资源和系统服务，1
import android.graphics.Bitmap; // Android位图类，用于截图功能，1
import android.graphics.Canvas; // Android画布类，用于绘制位图，1
import android.view.View; // Android视图类，用于控制视图可见性，1
import android.widget.AbsoluteLayout; // Android绝对布局类，用于精确控制子视图位置，1

import com.micsig.base.Logger; // 日志工具类，用于输出调试日志，1
import com.micsig.tbook.scope.ScopeBase; // 示波器基类（未使用），1
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量管理类，用于获取波形区域尺寸等信息，1
import com.micsig.tbook.tbookscope.MainViewGroup; // 主视图容器类，用于获取布局中的视图引用，1
import com.micsig.tbook.tbookscope.R; // 资源文件类，用于访问布局资源ID，1
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线类，用于发送工作模式切换事件，1
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举类，定义事件类型常量，1
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类，用于读取串口文本模式状态，1

/**
 * 工作模式管理类 - 示波器工作模式切换的核心管理器
 * 
 * 【模块定位】
 * - 所属模块：wavezone（波形显示区域模块）
 * - 核心职责：管理示波器的三种工作模式（YT、XY、YTZOOM）的切换和状态管理
 * - 架构层级：业务逻辑层，位于UI层和显示组件层之间
 * - 设计模式：单例模式（静态内部类实现）
 * 
 * 【核心职责】
 * 1. 工作模式管理：管理YT、XY、YTZOOM三种工作模式的切换
 * 2. 显示区域控制：控制三个显示区域（YT、XY、YTZOOM）的可见性和层级
 * 3. 状态切换管理：管理模式切换过程中的状态标志（BeginSwitchMode、EndSwitchMode、Ready、Init）
 * 4. 生命周期管理：管理显示组件的onResume/onPause生命周期
 * 5. 截图功能：提供当前工作模式的截图功能
 * 6. 尺寸调整：响应波形区域尺寸变化，调整显示区域布局
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                        WorkModeManage（单例）                            │
 * │                         工作模式管理核心                                  │
 * │  ┌──────────────────────────────────────────────────────────────────┐  │
 * │  │  状态管理                                                         │  │
 * │  │  - mWorkMode: 当前工作模式                                        │  │
 * │  │  - WorkModeManageState: 状态切换过程标志                         │  │
 * │  │  - WorkModeChange: 模式切换中标志                                 │  │
 * │  └──────────────────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────────────────┐  │
 * │  │  显示区域管理                                                     │  │
 * │  │  - waveZoneDisplay_yt: YT模式显示区域                            │  │
 * │  │  - waveZoneDisplay_xy: XY模式显示区域                            │  │
 * │  │  - waveZoneDisplay_ytZoom: YT缩放模式显示区域                    │  │
 * │  └──────────────────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────────────────┐  │
 * │  │  布局容器管理                                                     │  │
 * │  │  - absoluteLayout_yt: YT模式布局容器                            │  │
 * │  │  - absoluteLayout_xy: XY模式布局容器                             │  │
 * │  │  - absoluteLayout_zoom: YT缩放模式布局容器                       │  │
 * │  └──────────────────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────────────┘
 *                              ↓ 实现接口
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                          IWorkMode接口                                  │
 * │  - switchWorkMode(): 模式切换方法（无动画，一步到位）                    │
 * └─────────────────────────────────────────────────────────────────────────┘
 *                              ↓ 管理显示组件
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      三个显示区域组件                                    │
 * │  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐          │
 * │  │WaveZoneDisplay │  │WaveZoneDisplay │  │WaveZoneDisplay │          │
 * │  │      _YT       │  │      _XY       │  │    _YTZoom     │          │
 * │  │  YT模式显示    │  │  XY模式显示    │  │  YT缩放显示    │          │
 * │  └────────────────┘  └────────────────┘  └────────────────┘          │
 * └─────────────────────────────────────────────────────────────────────────┘
 *                              ↓ 发送事件
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                          RxBus事件总线                                   │
 * │  - WAVEZONE_WORKMODE_CHANGE: 工作模式切换事件                           │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【三种工作模式说明】
 * 1. YT模式（WorkMode_YT = 0x00）：
 *    - 时域显示模式，显示电压随时间变化的波形
 *    - 最常用的示波器显示模式
 *    - 支持触发、测量、光标等功能
 *    - 显示区域：absoluteLayout_yt（全屏显示）
 *    - Z轴层级：40f（最上层）
 * 
 * 2. YTZOOM模式（WorkMode_YTZOOM = 0x01）：
 *    - YT模式的扩展，在YT波形上方显示缩放窗口
 *    - 用于观察波形的细节部分
 *    - 支持缩放区域的移动和调整
 *    - 显示区域：absoluteLayout_yt（下方，Y偏移为缩放窗口高度） + absoluteLayout_zoom（上方）
 *    - Z轴层级：absoluteLayout_yt(30f) + absoluteLayout_zoom(40f，最上层)
 * 
 * 3. XY模式（WorkMode_XY = 0x02）：
 *    - X-Y关系显示模式，显示两个通道的关系图
 *    - 用于观察两个信号的相位关系
 *    - 常用于李萨如图形显示
 *    - 显示区域：absoluteLayout_xy（居中显示）
 *    - Z轴层级：40f（最上层）
 * 
 * 【状态切换过程管理】
 * 状态切换过程通过WorkModeManageState标志位管理：
 * - WorkModeManage_Init = 0x00：初始状态，无模式切换进行中
 * - WorkModeManage_BeginSwitchMode = 0x01：开始切换模式标志
 * - WorkModeManage_EndSwitchMode = 0x02：结束切换模式标志
 * - WorkModeManage_Ready = 0x03：准备就绪状态（BeginSwitchMode | EndSwitchMode）
 * 
 * 状态切换流程：
 * 1. 开始切换：设置BeginSwitchMode标志
 * 2. 执行切换：调用setWorkMode()或switchWorkMode()
 * 3. 结束切换：设置EndSwitchMode标志
 * 4. 恢复初始：设置Init状态
 * 
 * 【依赖关系】
 * - 实现接口：IWorkMode（工作模式接口）
 * - 管理组件：WaveZoneDisplay_YT、WaveZoneDisplay_XY、WaveZoneDisplay_YTZoom
 * - 依赖工具：GlobalVar（全局变量）、RxBus（事件总线）、CacheUtil（缓存工具）
 * - 依赖布局：AbsoluteLayout（绝对布局）
 * - 事件通知：WorkModeBean（工作模式切换事件数据）
 * 
 * 【单例模式说明】
 * 使用静态内部类实现单例模式：
 * - 优点：线程安全、延迟加载、无需同步锁
 * - 实现：通过WorkModeManageHolder静态内部类持有单例实例
 * - 获取方式：WorkModeManage.getInstance()
 * - 适用场景：全局唯一的工作模式管理器
 * 
 * 【使用场景】
 * 1. 初始化：在应用启动时调用initWaveZoneDisplay()初始化显示区域
 * 2. 模式切换：用户切换工作模式时调用setWorkMode()
 * 3. 状态查询：通过isXyMode()、isYTMode()、isZoom()查询当前模式
 * 4. 截图功能：调用getBitmap()获取当前显示区域的截图
 * 5. 生命周期：在Activity/Fragment生命周期中调用onResume()、onPause()
 * 6. 尺寸调整：波形区域尺寸变化时调用changeWaveZoneSize()
 * 
 * 【数据流向】
 * 用户操作 → WorkModeManage.setWorkMode() 
 *          → 更新mWorkMode状态
 *          → 切换显示区域可见性
 *          → 调用显示组件的switchWorkMode()
 *          → 发送RxBus事件通知
 *          → 其他组件响应模式切换
 * 
 * 【性能考虑】
 * 1. 模式切换实时性：模式切换需要快速响应，避免卡顿
 * 2. OpenGL资源管理：切换时需要正确管理OpenGL资源（forceClearGlCanvas、forceDrawGlCanvas）
 * 3. 视图层级管理：通过setTranslationZ()控制视图层级，避免遮挡问题
 * 4. 状态同步：切换时需要同步更新所有组件状态
 * 
 * 【注意事项】
 * 1. 单例模式：全局唯一实例，通过getInstance()获取
 * 2. 线程安全：setWorkModeManageState()和setWorkModeChange()使用synchronized修饰
 * 3. 模式切换：setWorkMode()包含动画效果，switchWorkMode()无动画一步到位
 * 4. 初始化顺序：必须先调用initWaveZoneDisplay()初始化显示区域
 * 5. 生命周期：必须正确调用onResume()和onPause()管理显示组件生命周期
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/6/15
 * @see IWorkMode
 * @see WaveZoneDisplay_YT
 * @see WaveZoneDisplay_XY
 * @see WaveZoneDisplay_YTZoom
 * @see WorkModeBean
 */

public class WorkModeManage implements IWorkMode { // 工作模式管理类，实现IWorkMode接口，负责管理示波器的三种工作模式，1
    
    /**
     * 日志标签，用于标识日志来源
     */
    private static final String TAG = "WorkModeManage"; // 日志标签常量，值为"WorkModeManage"，1

    /**
     * 状态切换过程标志：开始切换模式
     * 用于标识模式切换过程的开始阶段
     */
    public static final int WorkModeManage_BeginSwitchMode = 0x01; // 开始切换模式标志常量，值为0x01，1
    
    /**
     * 状态切换过程标志：结束切换模式
     * 用于标识模式切换过程的结束阶段
     */
    public static final int WorkModeManage_EndSwitchMode = 0x02; // 结束切换模式标志常量，值为0x02，1
    
    /**
     * 状态切换过程标志：准备就绪状态
     * 通过位或运算组合BeginSwitchMode和EndSwitchMode标志
     * 用于标识模式切换已完成，系统处于就绪状态
     */
    public static final int WorkModeManage_Ready = WorkModeManage_BeginSwitchMode | WorkModeManage_EndSwitchMode; // 准备就绪状态常量，值为0x03（BeginSwitchMode | EndSwitchMode），1
    
    /**
     * 状态切换过程标志：初始状态
     * 用于标识无模式切换进行中，系统处于初始状态
     */
    public static final int WorkModeManage_Init = 0x00; // 初始状态常量，值为0x00，1

    //region 单例类实现
    /**
     * 静态内部类持有单例实例
     * 使用静态内部类实现单例模式，保证线程安全和延迟加载
     * 
     * 【设计模式】
     * - 单例模式：静态内部类实现方式
     * - 优点：线程安全、延迟加载、无需同步锁
     * - 原理：类加载机制保证线程安全，静态内部类延迟加载
     */
    private static class WorkModeManageHolder { // 静态内部类，用于持有单例实例，1
        /**
         * 单例实例，静态final修饰，保证全局唯一
         */
        public static final WorkModeManage instance = new WorkModeManage(); // 单例实例常量，静态final修饰，保证全局唯一，1
    } // 结束WorkModeManageHolder静态内部类定义，1

    /**
     * 获取单例实例
     * 
     * 【功能说明】
     * 获取WorkModeManage的单例实例，全局唯一
     * 
     * 【返回值】
     * @return WorkModeManage单例实例
     * 
     * 【调用时机】
     * - 需要访问工作模式管理器时调用
     * - 切换工作模式时调用
     * - 查询当前工作模式时调用
     * 
     * 【使用示例】
     * WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_XY, false);
     */
    public static WorkModeManage getInstance() { // 获取单例实例的静态方法，1
        return WorkModeManage.WorkModeManageHolder.instance; // 返回静态内部类持有的单例实例，1
    } // 结束getInstance方法，1
    //endregion


//region 属性
    /**
     * 状态切换过程标志
     * 用于标识当前模式切换的状态
     * 取值范围：WorkModeManage_Init、WorkModeManage_BeginSwitchMode、WorkModeManage_EndSwitchMode、WorkModeManage_Ready
     */
    private int WorkModeManageState = 0; // 状态切换过程标志变量，初始值为0（Init状态），1

    /**
     * 获取状态切换过程标志
     * 
     * 【功能说明】
     * 获取当前模式切换的状态标志
     * 
     * 【返回值】
     * @return 状态切换过程标志，取值范围见WorkModeManageState定义
     * 
     * 【调用时机】
     * - 查询模式切换状态时调用
     * - 判断是否可以进行模式切换时调用
     */
    public int getWorkModeManageState() { // 获取状态切换过程标志的方法，1
        return WorkModeManageState; // 返回当前状态切换过程标志，1
    } // 结束getWorkModeManageState方法，1

    /**
     * 设置状态切换过程标志
     * 
     * 【功能说明】
     * 设置当前模式切换的状态标志，使用synchronized保证线程安全
     * 
     * 【参数说明】
     * @param workModeManageState 状态切换过程标志，取值范围见WorkModeManageState定义
     * 
     * 【调用时机】
     * - 开始模式切换时设置BeginSwitchMode
     * - 结束模式切换时设置EndSwitchMode
     * - 模式切换完成后设置Init
     * 
     * 【线程安全】
     * 使用synchronized修饰，保证多线程环境下的线程安全
     */
    public synchronized void setWorkModeManageState(int workModeManageState) { // 设置状态切换过程标志的方法，synchronized保证线程安全，1
        WorkModeManageState = workModeManageState; // 设置状态切换过程标志，1
    } // 结束setWorkModeManageState方法，1

    /**
     * 工作模式切换中标志
     * 用于标识是否正在进行模式切换
     * true：正在进行模式切换
     * false：未进行模式切换
     */
    private boolean WorkModeChange = false; // 工作模式切换中标志变量，初始值为false，1

    /**
     * 判断是否正在进行工作模式切换
     * 
     * 【功能说明】
     * 判断当前是否正在进行工作模式切换
     * 
     * 【返回值】
     * @return true：正在进行模式切换；false：未进行模式切换
     * 
     * 【调用时机】
     * - 需要判断是否可以进行模式切换时调用
     * - 需要判断当前模式状态时调用
     */
    public boolean isWorkModeChange() { // 判断是否正在进行工作模式切换的方法，1
        return WorkModeChange; // 返回工作模式切换中标志，1
    } // 结束isWorkModeChange方法，1

    /**
     * 设置工作模式切换中标志
     * 
     * 【功能说明】
     * 设置工作模式切换中标志，使用synchronized保证线程安全
     * 
     * 【参数说明】
     * @param workModeChange true：正在进行模式切换；false：未进行模式切换
     * 
     * 【调用时机】
     * - 开始模式切换时设置为true
     * - 结束模式切换时设置为false
     * 
     * 【线程安全】
     * 使用synchronized修饰，保证多线程环境下的线程安全
     */
    public synchronized void setWorkModeChange(boolean workModeChange) { // 设置工作模式切换中标志的方法，synchronized保证线程安全，1
        WorkModeChange = workModeChange; // 设置工作模式切换中标志，1
    } // 结束setWorkModeChange方法，1

    /**
     * 当前工作模式
     * 使用@IWorkMode.WorkMode注解约束取值范围
     * 取值范围：WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     */
    private
    @IWorkMode.WorkMode
    int mWorkMode = IWorkMode.WorkMode_YT; // 当前工作模式变量，初始值为YT模式，使用@WorkMode注解约束取值范围，1

    /**
     * 获取当前工作模式
     * 
     * 【功能说明】
     * 获取当前的工作模式标识
     * 
     * 【返回值】
     * @return 当前工作模式，使用@WorkMode注解约束取值范围
     *         取值范围：WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * 
     * 【调用时机】
     * - 需要判断当前工作模式时调用
     * - 需要根据工作模式执行不同逻辑时调用
     */
    public
    @IWorkMode.WorkMode
    int getmWorkMode() { // 获取当前工作模式的方法，1
        return mWorkMode; // 返回当前工作模式，1
    } // 结束getmWorkMode方法，1

    /**
     * 仅用于初始化时外部调用使用，实际此时不做处理，所以需要成对使用，改变后再改回原来的值
     * 
     * 【功能说明】
     * 仅修改当前工作模式数据，不执行模式切换逻辑
     * 用于初始化场景，避免触发模式切换流程
     * 
     * 【参数说明】
     * @param nextWorkMode 目标工作模式，使用@WorkMode注解约束取值范围
     *                     取值范围：WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * 
     * 【调用时机】
     * - 初始化时设置初始工作模式
     * - 需要临时修改工作模式数据而不触发切换逻辑时
     * 
     * 【注意事项】
     * - 仅修改数据，不执行模式切换逻辑
     * - 需要成对使用，改变后再改回原来的值
     * - 不推荐在正常业务流程中使用
     */
    public void setWorkModeOnlyChangeData(@IWorkMode.WorkMode int nextWorkMode) { // 仅修改工作模式数据的方法，不执行切换逻辑，1
        this.mWorkMode = nextWorkMode; // 直接设置工作模式数据，不执行切换逻辑，1
    } // 结束setWorkModeOnlyChangeData方法，1

    /**
     * 设置工作模式（包含动画效果）
     * 
     * 【功能说明】
     * 切换示波器的工作模式，包含视图切换动画效果
     * 切换流程：隐藏所有显示区域 → 执行模式切换逻辑 → 显示目标显示区域 → 发送事件通知
     * 
     * 【参数说明】
     * @param nextWorkMode 目标工作模式，使用@WorkMode注解约束取值范围
     *                     取值范围：WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * @param isFromEventBus 是否来自EventBus事件，用于事件通知时判断是否需要再次发送事件
     *                       true：来自EventBus事件，不需要再次发送事件
     *                       false：非EventBus事件，需要发送事件通知
     * 
     * 【调用时机】
     * - 用户通过UI切换工作模式时调用
     * - 需要切换工作模式时调用
     * 
     * 【切换流程】
     * 1. 检查目标模式是否与当前模式相同，相同则直接返回
     * 2. 记录前一个工作模式
     * 3. 更新当前工作模式
     * 4. 隐藏所有显示区域
     * 5. 根据目标模式执行切换逻辑：
     *    - XY模式：切换XY显示组件，暂停YT和YTZOOM组件，设置Z轴层级，调整XY空间
     *    - YT模式：切换YT显示组件，暂停XY和YTZOOM组件，设置Z轴层级，重置YT位置
     *    - YTZOOM模式：切换YTZOOM显示组件，暂停XY组件，设置Z轴层级，设置YT偏移
     * 6. 设置显示区域可见性
     * 7. 发送RxBus事件通知
     * 
     * 【注意事项】
     * - 包含视图切换动画效果
     * - 需要正确管理显示组件的生命周期（onResume/onPause）
     * - 需要正确管理OpenGL资源（forceClearGlCanvas/forceDrawGlCanvas）
     * - 使用setTranslationZ()控制视图层级
     */
    public void setWorkMode(@IWorkMode.WorkMode int nextWorkMode, boolean isFromEventBus) { // 设置工作模式的方法，包含动画效果，1
        if (nextWorkMode == this.mWorkMode) return; // 如果目标模式与当前模式相同，直接返回，不执行切换，1
        int preWorkMode = this.mWorkMode; // 记录前一个工作模式，用于事件通知，1
        Logger.i(TAG, "setWorkMode,preWorkMode:" + preWorkMode + ",nextWorkMode:" + nextWorkMode); // 输出日志，记录模式切换信息，1
        this.mWorkMode = nextWorkMode; // 更新当前工作模式为目标模式，1
        absoluteLayout_xy.setVisibility(View.GONE); // 隐藏XY模式布局容器，1
        absoluteLayout_yt.setVisibility(View.GONE); // 隐藏YT模式布局容器，1
        absoluteLayout_zoom.setVisibility(View.GONE); // 隐藏YT缩放模式布局容器，1

        switch (nextWorkMode) { // 根据目标工作模式执行不同的切换逻辑，1
            case IWorkMode.WorkMode_XY: { // XY模式切换逻辑，1
                waveZoneDisplay_xy.switchWorkMode(nextWorkMode); // 调用XY显示组件的切换方法，1
                waveZoneDisplay_ytZoom.onPause(); // 暂停YT缩放显示组件，释放OpenGL资源，1
                waveZoneDisplay_xy.onResume(); // 恢复XY显示组件，初始化OpenGL资源，1
                waveZoneDisplay_yt.onPause(); // 暂停YT显示组件，释放OpenGL资源，1

                absoluteLayout_xy.setTranslationZ(40f); // 设置XY布局容器Z轴层级为40f（最上层），1
                absoluteLayout_yt.setTranslationZ(20f); // 设置YT布局容器Z轴层级为20f（中层），1
                absoluteLayout_zoom.setTranslationZ(10f); // 设置YT缩放布局容器Z轴层级为10f（下层），1
                changeXYSpace(); // 调整XY模式的显示空间，使其居中显示，1
            } // 结束XY模式切换逻辑块，1
            break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YT: { // YT模式切换逻辑，1
                waveZoneDisplay_yt.switchWorkMode(nextWorkMode); // 调用YT显示组件的切换方法，1
                waveZoneDisplay_ytZoom.onPause(); // 暂停YT缩放显示组件，释放OpenGL资源，1
                waveZoneDisplay_xy.onPause(); // 暂停XY显示组件，释放OpenGL资源，1
                waveZoneDisplay_yt.onResume(); // 恢复YT显示组件，初始化OpenGL资源，1

                absoluteLayout_yt.setTranslationZ(40f); // 设置YT布局容器Z轴层级为40f（最上层），1
                absoluteLayout_zoom.setTranslationZ(10f); // 设置YT缩放布局容器Z轴层级为10f（下层），1
                absoluteLayout_xy.setTranslationZ(20f); // 设置XY布局容器Z轴层级为20f（中层），1
                if (absoluteLayout_yt.getY() != 0) { // 如果YT布局容器的Y坐标不为0（之前在YTZOOM模式），1
                    waveZoneDisplay_yt.forceClearGlCanvas(); // 强制清除OpenGL画布，避免残留图像，1
                    absoluteLayout_yt.setY(0); // 设置YT布局容器的Y坐标为0，恢复到顶部位置，1
                    waveZoneDisplay_yt.forceDrawGlCanvas(); // 强制绘制OpenGL画布，重新渲染波形，1
                } // 结束Y坐标判断，1
            } // 结束YT模式切换逻辑块，1
            break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YTZOOM: { // YT缩放模式切换逻辑，1
                waveZoneDisplay_yt.switchWorkMode(nextWorkMode); // 调用YT显示组件的切换方法，1
                waveZoneDisplay_ytZoom.onResume(); // 恢复YT缩放显示组件，初始化OpenGL资源，1
                waveZoneDisplay_xy.onPause(); // 暂停XY显示组件，释放OpenGL资源，1
                waveZoneDisplay_yt.onResume(); // 恢复YT显示组件，初始化OpenGL资源，1

                absoluteLayout_zoom.setTranslationZ(40f); // 设置YT缩放布局容器Z轴层级为40f（最上层），1
                absoluteLayout_yt.setTranslationZ(30f); // 设置YT布局容器Z轴层级为30f（中上层），1
                absoluteLayout_xy.setTranslationZ(0f); // 设置XY布局容器Z轴层级为0f（最下层），1
                waveZoneDisplay_yt.forceClearGlCanvas(); // 强制清除OpenGL画布，避免残留图像，1
                absoluteLayout_yt.setY(GlobalVar.get().getRectZoom().height()); // 设置YT布局容器的Y坐标为缩放窗口高度，使其显示在缩放窗口下方，1
                waveZoneDisplay_yt.forceDrawGlCanvas(); // 强制绘制OpenGL画布，重新渲染波形，1
            } // 结束YT缩放模式切换逻辑块，1
            break; // 跳出switch语句，1
        } // 结束switch语句，1
        setWaveZoneVisible(nextWorkMode); // 设置显示区域的可见性，根据当前工作模式显示对应的显示区域，1
        RxBus.getInstance().post(RxEnum.WAVEZONE_WORKMODE_CHANGE, new WorkModeBean(preWorkMode, nextWorkMode, isFromEventBus)); // 发送RxBus事件通知，通知其他组件工作模式已切换，1
    } // 结束setWorkMode方法，1

    /**
     * 设置显示区域可见性
     * 
     * 【功能说明】
     * 根据当前工作模式设置三个显示区域的可见性
     * - XY模式：仅显示XY显示区域
     * - YT模式：仅显示YT显示区域
     * - YTZOOM模式：显示YT显示区域和YT缩放显示区域
     * 
     * 【参数说明】
     * @param mWorkMode 当前工作模式，使用@WorkMode注解约束取值范围
     *                  取值范围：WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * 
     * 【调用时机】
     * - setWorkMode()方法内部调用
     * - 模式切换完成后设置显示区域可见性
     * 
     * 【可见性规则】
     * - XY模式：absoluteLayout_xy(VISIBLE)，其他(GONE)
     * - YT模式：absoluteLayout_yt(VISIBLE)，其他(GONE)
     * - YTZOOM模式：absoluteLayout_yt(VISIBLE)，absoluteLayout_zoom(VISIBLE)，absoluteLayout_xy(GONE)
     */
    private void setWaveZoneVisible(@IWorkMode.WorkMode int mWorkMode) { // 设置显示区域可见性的方法，1
        switch (mWorkMode) { // 根据当前工作模式设置显示区域可见性，1
            case IWorkMode.WorkMode_XY: // XY模式可见性设置，1
                absoluteLayout_xy.setVisibility(View.VISIBLE); // 设置XY布局容器可见，1
                absoluteLayout_yt.setVisibility(View.GONE); // 设置YT布局容器不可见，1
                absoluteLayout_zoom.setVisibility(View.GONE); // 设置YT缩放布局容器不可见，1
                break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YT: // YT模式可见性设置，1
                absoluteLayout_xy.setVisibility(View.GONE); // 设置XY布局容器不可见，1
                absoluteLayout_yt.setVisibility(View.VISIBLE); // 设置YT布局容器可见，1
                absoluteLayout_zoom.setVisibility(View.GONE); // 设置YT缩放布局容器不可见，1
                break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YTZOOM: // YT缩放模式可见性设置，1
                absoluteLayout_xy.setVisibility(View.GONE); // 设置XY布局容器不可见，1
                absoluteLayout_yt.setVisibility(View.VISIBLE); // 设置YT布局容器可见，1
                absoluteLayout_zoom.setVisibility(View.VISIBLE); // 设置YT缩放布局容器可见，1
                break; // 跳出switch语句，1
        } // 结束switch语句，1
    } // 结束setWaveZoneVisible方法，1
    //endregion

    //region 三个模式的初始化
    /**
     * YT模式显示区域组件
     * 用于显示YT模式的波形
     */
    private WaveZoneDisplay_YT waveZoneDisplay_yt = null; // YT模式显示区域组件变量，初始值为null，1
    
    /**
     * XY模式显示区域组件
     * 用于显示XY模式的波形
     */
    private WaveZoneDisplay_XY waveZoneDisplay_xy = null; // XY模式显示区域组件变量，初始值为null，1
    
    /**
     * YT缩放模式显示区域组件
     * 用于显示YT缩放模式的波形缩放窗口
     */
    private WaveZoneDisplay_YTZoom waveZoneDisplay_ytZoom = null; // YT缩放模式显示区域组件变量，初始值为null，1
    
    /**
     * YT模式布局容器
     * 用于容纳YT模式显示区域组件
     */
    private AbsoluteLayout absoluteLayout_yt, absoluteLayout_xy, absoluteLayout_zoom; // 三个布局容器变量，分别用于YT、XY、YT缩放模式，1

    /**
     * 获取YT模式显示区域组件
     * 
     * 【功能说明】
     * 获取YT模式显示区域组件实例
     * 
     * 【返回值】
     * @return YT模式显示区域组件，如果未初始化则返回null
     * 
     * 【调用时机】
     * - 需要访问YT显示组件时调用
     * - 需要操作YT显示组件时调用
     */
    public WaveZoneDisplay_YT getWaveZoneDisplay_yt() { // 获取YT模式显示区域组件的方法，1
        return waveZoneDisplay_yt; // 返回YT模式显示区域组件，1
    } // 结束getWaveZoneDisplay_yt方法，1

    /**
     * 获取XY模式显示区域组件
     * 
     * 【功能说明】
     * 获取XY模式显示区域组件实例
     * 
     * 【返回值】
     * @return XY模式显示区域组件，如果未初始化则返回null
     * 
     * 【调用时机】
     * - 需要访问XY显示组件时调用
     * - 需要操作XY显示组件时调用
     */
    public WaveZoneDisplay_XY getWaveZoneDisplay_xy() { // 获取XY模式显示区域组件的方法，1
        return waveZoneDisplay_xy; // 返回XY模式显示区域组件，1
    } // 结束getWaveZoneDisplay_xy方法，1

    /**
     * 获取YT缩放模式显示区域组件
     * 
     * 【功能说明】
     * 获取YT缩放模式显示区域组件实例
     * 
     * 【返回值】
     * @return YT缩放模式显示区域组件，如果未初始化则返回null
     * 
     * 【调用时机】
     * - 需要访问YT缩放显示组件时调用
     * - 需要操作YT缩放显示组件时调用
     */
    public WaveZoneDisplay_YTZoom getWaveZoneDisplay_ytZoom() { // 获取YT缩放模式显示区域组件的方法，1
        return waveZoneDisplay_ytZoom; // 返回YT缩放模式显示区域组件，1
    } // 结束getWaveZoneDisplay_ytZoom方法，1
    //endregion

    /**
     * 初始化方法（空实现）
     * 
     * 【功能说明】
     * 初始化方法，当前为空实现
     * 预留给后续扩展使用
     * 
     * 【调用时机】
     * - 应用启动时调用（预留）
     */
    public void init() { // 初始化方法，当前为空实现，1

    } // 结束init方法，1

    /**
     * 初始化显示区域，默认显示YT模式
     * 
     * 【功能说明】
     * 初始化三个显示区域组件（YT、XY、YTZOOM），并将它们添加到对应的布局容器中
     * 初始化完成后，默认显示YT模式，XY和YTZOOM模式处于暂停状态
     * 
     * 【参数说明】
     * @param context Android上下文，用于创建显示区域组件
     * @param mainViewGroup 主视图容器，用于获取布局中的视图引用
     * 
     * 【调用时机】
     * - 应用启动时调用
     * - 需要初始化显示区域时调用
     * 
     * 【初始化流程】
     * 1. 从MainViewGroup获取三个布局容器的引用
     * 2. 创建YT显示区域组件
     * 3. 创建YT缩放显示区域组件
     * 4. 设置YT显示区域组件与YT缩放显示区域组件的关联
     * 5. 创建XY显示区域组件
     * 6. 暂停XY和YT缩放显示区域组件（默认显示YT模式）
     * 7. 将三个显示区域组件添加到对应的布局容器中
     * 8. 调整XY模式的显示空间
     * 
     * 【注意事项】
     * - 必须在使用显示区域组件前调用此方法
     * - 默认显示YT模式，XY和YTZOOM模式处于暂停状态
     * - 需要在主线程中调用
     */
    public void initWaveZoneDisplay(Context context, MainViewGroup mainViewGroup) { // 初始化显示区域的方法，默认显示YT模式，1
        absoluteLayout_yt = (AbsoluteLayout) mainViewGroup.findViewById(R.id.middlebar_wave_zone); // 从MainViewGroup获取YT模式布局容器，1
        absoluteLayout_xy = (AbsoluteLayout) mainViewGroup.findViewById(R.id.middlebar_wave_zone_xy); // 从MainViewGroup获取XY模式布局容器，1
        absoluteLayout_zoom = (AbsoluteLayout) mainViewGroup.findViewById(R.id.middlebar_wave_zoom); // 从MainViewGroup获取YT缩放模式布局容器，1
        waveZoneDisplay_yt = new WaveZoneDisplay_YT(context, mainViewGroup); // 创建YT模式显示区域组件，1
        waveZoneDisplay_ytZoom = new WaveZoneDisplay_YTZoom(context); // 创建YT缩放模式显示区域组件，1
        waveZoneDisplay_yt.setWaveZoneDisplayYtZoom(waveZoneDisplay_ytZoom); // 设置YT显示区域组件与YT缩放显示区域组件的关联，1
        waveZoneDisplay_xy = new WaveZoneDisplay_XY(context,mainViewGroup); // 创建XY模式显示区域组件，1

        waveZoneDisplay_xy.onPause(); // 暂停XY显示区域组件，释放OpenGL资源（默认显示YT模式），1
        waveZoneDisplay_ytZoom.onPause(); // 暂停YT缩放显示区域组件，释放OpenGL资源（默认显示YT模式），1

//        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(550, 450, 150, 50);
//        absoluteLayout_xy.addView(waveZoneDisplay_xy, layoutParams);
        absoluteLayout_xy.addView(waveZoneDisplay_xy); // 将XY显示区域组件添加到XY布局容器中，1
        absoluteLayout_zoom.addView(waveZoneDisplay_ytZoom); // 将YT缩放显示区域组件添加到YT缩放布局容器中，1
        absoluteLayout_yt.addView(waveZoneDisplay_yt); // 将YT显示区域组件添加到YT布局容器中，1
        changeXYSpace(); // 调整XY模式的显示空间，使其居中显示，1
    } // 结束initWaveZoneDisplay方法，1
    
    /**
     * 刷新所有显示区域
     * 
     * 【功能说明】
     * 刷新三个显示区域组件，请求重新渲染
     * 
     * 【调用时机】
     * - 需要刷新显示区域时调用
     * - 波形数据更新后调用
     * 
     * 【刷新范围】
     * - YT显示区域组件
     * - XY显示区域组件
     * - YT缩放显示区域组件
     */
    public void refresh(){ // 刷新所有显示区域的方法，1
        waveZoneDisplay_yt.requestRender(); // 请求YT显示区域组件重新渲染，1
        waveZoneDisplay_xy.requestRender(); // 请求XY显示区域组件重新渲染，1
        waveZoneDisplay_ytZoom.requestRender(); // 请求YT缩放显示区域组件重新渲染，1
    } // 结束refresh方法，1

    /**
     * 改变波形区域尺寸
     * 
     * 【功能说明】
     * 响应波形区域尺寸变化，调整三个显示区域的布局参数
     * - YT布局容器：高度调整为newH
     * - YT缩放布局容器：高度调整为newH/4
     * - YT布局容器Y坐标：根据当前模式调整（YTZOOM模式时为newH/4，其他模式为0）
     * - XY布局容器：调用changeXYSpace()调整显示空间
     * 
     * 【参数说明】
     * @param newH 新的波形区域高度（像素）
     * 
     * 【调用时机】
     * - 波形区域尺寸变化时调用
     * - 屏幕旋转时调用
     * - 窗口大小调整时调用
     * 
     * 【调整流程】
     * 1. 调整YT布局容器的高度
     * 2. 调整YT缩放布局容器的高度（为YT高度的1/4）
     * 3. 清除YT显示区域的OpenGL画布
     * 4. 根据当前模式调整YT布局容器的Y坐标
     * 5. 重新绘制YT显示区域的OpenGL画布
     * 6. 调整XY模式的显示空间
     * 
     * 【注意事项】
     * - 需要正确管理OpenGL资源（forceClearGlCanvas/forceDrawGlCanvas）
     * - YTZOOM模式时YT布局容器需要偏移
     */
    public void changeWaveZoneSize(int newH) { // 改变波形区域尺寸的方法，1
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) absoluteLayout_yt.getLayoutParams(); // 获取YT布局容器的布局参数，1
        layoutParams.height = newH; // 设置YT布局容器的高度为newH，1
        absoluteLayout_yt.setLayoutParams(layoutParams); // 应用YT布局容器的布局参数，1

        AbsoluteLayout.LayoutParams zoomLayoutParams = (AbsoluteLayout.LayoutParams) absoluteLayout_zoom.getLayoutParams(); // 获取YT缩放布局容器的布局参数，1
        zoomLayoutParams.height = newH >> 2; // 设置YT缩放布局容器的高度为newH/4（右移2位相当于除以4），1
        absoluteLayout_zoom.setLayoutParams(zoomLayoutParams); // 应用YT缩放布局容器的布局参数，1

        waveZoneDisplay_yt.forceClearGlCanvas(); // 强制清除YT显示区域的OpenGL画布，避免残留图像，1
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) { // 如果当前工作模式为YT缩放模式，1
            absoluteLayout_yt.setY(newH >> 2); // 设置YT布局容器的Y坐标为newH/4，使其显示在缩放窗口下方，1
        } else { // 如果当前工作模式不是YT缩放模式，1
            absoluteLayout_yt.setY(0); // 设置YT布局容器的Y坐标为0，使其显示在顶部，1
        } // 结束工作模式判断，1
        waveZoneDisplay_yt.forceDrawGlCanvas(); // 强制绘制YT显示区域的OpenGL画布，重新渲染波形，1

//        AbsoluteLayout.LayoutParams xyLayoutParams = (AbsoluteLayout.LayoutParams) absoluteLayout_xy.getLayoutParams();
//        xyLayoutParams.height = newH / 10 * 8;
//        xyLayoutParams.width = newH / 10 * 8;
//        absoluteLayout_xy.setLayoutParams(xyLayoutParams);
        changeXYSpace(); // 调整XY模式的显示空间，使其居中显示，1

    } // 结束changeWaveZoneSize方法，1

    /**
     * 判断当前是否为XY模式
     * 
     * 【功能说明】
     * 判断当前工作模式是否为XY模式
     * 
     * 【返回值】
     * @return true：当前为XY模式；false：当前不是XY模式
     * 
     * 【调用时机】
     * - 需要判断当前工作模式时调用
     * - 需要根据工作模式执行不同逻辑时调用
     */
    public boolean isXyMode(){ // 判断当前是否为XY模式的方法，1
        return this.mWorkMode==IWorkMode.WorkMode_XY; // 返回当前工作模式是否为XY模式，1
    } // 结束isXyMode方法，1
    
    /**
     * 判断当前是否为YT模式
     * 
     * 【功能说明】
     * 判断当前工作模式是否为YT模式
     * 
     * 【返回值】
     * @return true：当前为YT模式；false：当前不是YT模式
     * 
     * 【调用时机】
     * - 需要判断当前工作模式时调用
     * - 需要根据工作模式执行不同逻辑时调用
     */
    public boolean isYTMode(){ return this.mWorkMode==IWorkMode.WorkMode_YT;} // 判断当前是否为YT模式的方法，返回当前工作模式是否为YT模式，1
    
    /**
     * 判断当前是否为YT缩放模式
     * 
     * 【功能说明】
     * 判断当前工作模式是否为YT缩放模式
     * 
     * 【返回值】
     * @return true：当前为YT缩放模式；false：当前不是YT缩放模式
     * 
     * 【调用时机】
     * - 需要判断当前工作模式时调用
     * - 需要根据工作模式执行不同逻辑时调用
     */
    public boolean isZoom(){ // 判断当前是否为YT缩放模式的方法，1
        return this.mWorkMode==IWorkMode.WorkMode_YTZOOM; // 返回当前工作模式是否为YT缩放模式，1
    } // 结束isZoom方法，1
    
    /**
     * 判断当前是否为串口文本模式
     * 
     * 【功能说明】
     * 判断当前是否为串口文本模式，从缓存中读取状态
     * 
     * 【返回值】
     * @return true：当前为串口文本模式；false：当前不是串口文本模式
     * 
     * 【调用时机】
     * - 需要判断当前是否为串口文本模式时调用
     * - 需要根据串口文本模式执行不同逻辑时调用
     * 
     * 【数据来源】
     * 从CacheUtil缓存中读取MAIN_BOTTOM_SLIP_SERIALBUSTXT键的值
     */
    public boolean isSerialTextMode(){ // 判断当前是否为串口文本模式的方法，1
        boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT); // 从缓存中读取串口文本模式状态，1
        return isSerialsTxt; // 返回串口文本模式状态，1
    } // 结束isSerialTextMode方法，1

    /**
     * 获取当前显示区域在屏幕上的位置
     * 
     * 【功能说明】
     * 根据当前工作模式，获取对应显示区域在屏幕上的位置
     * 位置信息存储在outLocation数组中：
     * - outLocation[0]：X坐标
     * - outLocation[1]：Y坐标
     * 
     * 【参数说明】
     * @param outLocation 输出参数，用于存储位置信息，数组长度至少为2
     *                    outLocation[0]：X坐标
     *                    outLocation[1]：Y坐标
     * 
     * 【调用时机】
     * - 需要获取显示区域位置时调用
     * - 需要进行坐标转换时调用
     * 
     * 【位置获取规则】
     * - XY模式：获取XY显示区域的位置
     * - YT模式：获取YT显示区域的位置
     * - YTZOOM模式：获取YT缩放显示区域的位置
     * - 其他情况：位置设为(0, 0)
     */
    public void getLocationOnScreen( int[] outLocation){ // 获取当前显示区域在屏幕上的位置的方法，1

        switch (mWorkMode){ // 根据当前工作模式获取对应显示区域的位置，1
            case IWorkMode.WorkMode_XY: // XY模式，1
                waveZoneDisplay_xy.getLocationOnScreen(outLocation); // 获取XY显示区域在屏幕上的位置，1
                break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YT: // YT模式，1
                waveZoneDisplay_yt.getLocationOnScreen(outLocation); // 获取YT显示区域在屏幕上的位置，1
                break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YTZOOM: // YT缩放模式，1
                waveZoneDisplay_ytZoom.getLocationOnScreen(outLocation); // 获取YT缩放显示区域在屏幕上的位置，1
                break; // 跳出switch语句，1
            default: // 默认情况，1
                outLocation[0] = 0; // 设置X坐标为0，1
                outLocation[1] = 0; // 设置Y坐标为0，1
                break; // 跳出switch语句，1
        } // 结束switch语句，1
    } // 结束getLocationOnScreen方法，1
    
    /**
     * 获取当前显示区域的截图
     * 
     * 【功能说明】
     * 根据当前工作模式，获取对应显示区域的截图
     * - XY模式：获取XY显示区域的截图
     * - YT模式：获取YT显示区域的截图
     * - YTZOOM模式：合并YT显示区域和YT缩放显示区域的截图（YT缩放窗口在上，YT波形窗口在下）
     * 
     * 【返回值】
     * @return 当前显示区域的截图，如果获取失败则返回null
     * 
     * 【调用时机】
     * - 需要截图保存时调用
     * - 需要分享波形图像时调用
     * 
     * 【截图逻辑】
     * - XY模式：直接获取XY显示区域的截图
     * - YT模式：直接获取YT显示区域的截图
     * - YTZOOM模式：
     *   1. 创建一个与YT显示区域等大的位图
     *   2. 创建画布
     *   3. 获取YT缩放显示区域的截图，绘制到画布顶部
     *   4. 获取YT显示区域的截图，绘制到画布下方（Y偏移为YT缩放窗口高度）
     *   5. 回收位图资源
     * 
     * 【注意事项】
     * - YTZOOM模式需要合并两个显示区域的截图
     * - 需要手动回收位图资源
     */
    public Bitmap getBitmap(){ // 获取当前显示区域截图的方法，1
        Bitmap bitmap = null; // 初始化位图变量为null，1
        switch (mWorkMode){ // 根据当前工作模式获取对应的截图，1
            case IWorkMode.WorkMode_XY: // XY模式，1
                bitmap = waveZoneDisplay_xy.getBitmap(); // 获取XY显示区域的截图，1
                break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YT: // YT模式，1
                bitmap = waveZoneDisplay_yt.getBitmap(); // 获取YT显示区域的截图，1
                break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YTZOOM: // YT缩放模式，1
            { // 开始YT缩放模式截图逻辑块，1


                bitmap = Bitmap.createBitmap(waveZoneDisplay_yt.getWidth(),waveZoneDisplay_yt.getHeight(), Bitmap.Config.ARGB_8888); // 创建一个与YT显示区域等大的位图，配置为ARGB_8888，1
                Canvas canvas = new Canvas(bitmap); // 创建画布，绑定到位图，1
                Bitmap bp = waveZoneDisplay_ytZoom.getBitmap(); // 获取YT缩放显示区域的截图，1
                canvas.drawBitmap(bp,0, 0,null); // 将YT缩放显示区域的截图绘制到画布顶部（坐标为0,0），1
                bp.recycle(); // 回收YT缩放显示区域的截图位图资源，1
                bp = waveZoneDisplay_yt.getBitmap(); // 获取YT显示区域的截图，1
                canvas.drawBitmap(bp,0, waveZoneDisplay_ytZoom.getHeight(),null); // 将YT显示区域的截图绘制到画布下方（Y偏移为YT缩放窗口高度），1
                bp.recycle(); // 回收YT显示区域的截图位图资源，1
                canvas.save(); // 保存画布状态，1
                canvas.restore(); // 恢复画布状态，1
            } // 结束YT缩放模式截图逻辑块，1
                break; // 跳出switch语句，1
        } // 结束switch语句，1
        return bitmap; // 返回截图位图，1
    } // 结束getBitmap方法，1
    
    /**
     * 停止刷新所有显示区域
     * 
     * 【功能说明】
     * 停止刷新三个显示区域组件，暂停OpenGL渲染
     * 调用所有显示区域组件的onPause()方法，释放OpenGL资源
     * 
     * 【调用时机】
     * - Activity/Fragment暂停时调用
     * - 需要停止波形刷新时调用
     * - 应用进入后台时调用
     * 
     * 【注意事项】
     * - 调用此方法后，需要调用onResume()恢复刷新
     * - 会释放OpenGL资源，需要正确管理生命周期
     */
    public void stopRefresh() { // 停止刷新所有显示区域的方法，1
        waveZoneDisplay_yt.onPause(); // 暂停YT显示区域组件，释放OpenGL资源，1
        waveZoneDisplay_ytZoom.onPause(); // 暂停YT缩放显示区域组件，释放OpenGL资源，1
        waveZoneDisplay_xy.onPause(); // 暂停XY显示区域组件，释放OpenGL资源，1
    } // 结束stopRefresh方法，1

    /**
     * 切换工作模式（无动画，一步到位）
     * 
     * 【功能说明】
     * 切换示波器的工作模式，无动画效果，一步到位
     * 实现IWorkMode接口的switchWorkMode方法
     * 
     * 【参数说明】
     * @param workMode 目标工作模式，使用@WorkMode注解约束取值范围
     *                 取值范围：WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * 
     * 【调用时机】
     * - 会在刷新的线程中被调用
     * - 需要快速切换模式时调用（无动画）
     * - 实现IWorkMode接口时调用
     * 
     * 【切换流程】
     * 1. 根据目标模式执行切换逻辑：
     *    - XY模式：暂停YT和YTZOOM组件，恢复XY组件，设置Z轴层级，调整XY空间
     *    - YT模式：暂停YTZOOM和XY组件，恢复YT组件，设置Z轴层级，重置YT位置
     *    - YTZOOM模式：暂停XY组件，恢复YT和YTZOOM组件，设置Z轴层级，设置YT偏移
     * 2. 设置工作模式切换中标志为false
     * 3. 设置状态切换过程标志为Init
     * 
     * 【与setWorkMode的区别】
     * - setWorkMode：包含视图切换动画效果，发送RxBus事件通知
     * - switchWorkMode：无动画效果，一步到位，不发送事件通知
     * 
     * 【注意事项】
     * - 无动画效果，快速切换
     * - 不发送RxBus事件通知
     * - 需要正确管理显示组件的生命周期（onResume/onPause）
     * - 需要正确管理OpenGL资源（forceClearGlCanvas/forceDrawGlCanvas）
     */
    @Override // 标记为接口方法实现，1
    public void switchWorkMode(@IWorkMode.WorkMode int workMode) { // 切换工作模式的方法，无动画，一步到位，实现IWorkMode接口，1
        switch (workMode) { // 根据目标工作模式执行不同的切换逻辑，1
            case IWorkMode.WorkMode_XY: { // XY模式切换逻辑，1
                waveZoneDisplay_ytZoom.onPause(); // 暂停YT缩放显示组件，释放OpenGL资源，1
                waveZoneDisplay_xy.onResume(); // 恢复XY显示组件，初始化OpenGL资源，1
                waveZoneDisplay_yt.onPause(); // 暂停YT显示组件，释放OpenGL资源，1

                absoluteLayout_xy.setTranslationZ(40f); // 设置XY布局容器Z轴层级为40f（最上层），1
                absoluteLayout_yt.setTranslationZ(20f); // 设置YT布局容器Z轴层级为20f（中层），1
                absoluteLayout_zoom.setTranslationZ(10f); // 设置YT缩放布局容器Z轴层级为10f（下层），1
                changeXYSpace(); // 调整XY模式的显示空间，使其居中显示，1
            } // 结束XY模式切换逻辑块，1
            break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YT: { // YT模式切换逻辑，1
                waveZoneDisplay_ytZoom.onPause(); // 暂停YT缩放显示组件，释放OpenGL资源，1
                waveZoneDisplay_xy.onPause(); // 暂停XY显示组件，释放OpenGL资源，1
                waveZoneDisplay_yt.onResume(); // 恢复YT显示组件，初始化OpenGL资源，1

                absoluteLayout_yt.setTranslationZ(40f); // 设置YT布局容器Z轴层级为40f（最上层），1
                absoluteLayout_zoom.setTranslationZ(10f); // 设置YT缩放布局容器Z轴层级为10f（下层），1
                absoluteLayout_xy.setTranslationZ(20f); // 设置XY布局容器Z轴层级为20f（中层），1
                if (absoluteLayout_yt.getY() != 0) { // 如果YT布局容器的Y坐标不为0（之前在YTZOOM模式），1
                    waveZoneDisplay_yt.forceClearGlCanvas(); // 强制清除OpenGL画布，避免残留图像，1
                    // waveZoneDisplay_yt.setY(0);
                    absoluteLayout_yt.setY(0); // 设置YT布局容器的Y坐标为0，恢复到顶部位置，1
                    waveZoneDisplay_yt.forceDrawGlCanvas(); // 强制绘制OpenGL画布，重新渲染波形，1
                } // 结束Y坐标判断，1

            } // 结束YT模式切换逻辑块，1
            break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YTZOOM: { // YT缩放模式切换逻辑，1
                waveZoneDisplay_ytZoom.onResume(); // 恢复YT缩放显示组件，初始化OpenGL资源，1
                waveZoneDisplay_xy.onPause(); // 暂停XY显示组件，释放OpenGL资源，1
                waveZoneDisplay_yt.onResume(); // 恢复YT显示组件，初始化OpenGL资源，1

                absoluteLayout_zoom.setTranslationZ(40f); // 设置YT缩放布局容器Z轴层级为40f（最上层），1
                absoluteLayout_yt.setTranslationZ(30f); // 设置YT布局容器Z轴层级为30f（中上层），1
                absoluteLayout_xy.setTranslationZ(0f); // 设置XY布局容器Z轴层级为0f（最下层），1
                waveZoneDisplay_yt.forceClearGlCanvas(); // 强制清除OpenGL画布，避免残留图像，1
//                waveZoneDisplay_yt.setY(100);
                absoluteLayout_yt.setY(GlobalVar.get().getRectZoom().height()); // 设置YT布局容器的Y坐标为缩放窗口高度，使其显示在缩放窗口下方，1
                waveZoneDisplay_yt.forceDrawGlCanvas(); // 强制绘制OpenGL画布，重新渲染波形，1

            } // 结束YT缩放模式切换逻辑块，1
            break; // 跳出switch语句，1
        } // 结束switch语句，1
        WorkModeManage.getInstance().setWorkModeChange(false); // 设置工作模式切换中标志为false，表示切换完成，1
        WorkModeManage.getInstance().setWorkModeManageState(WorkModeManage.WorkModeManage_Init); // 设置状态切换过程标志为Init，表示无模式切换进行中，1
    } // 结束switchWorkMode方法，1

    /**
     * 恢复显示区域刷新
     * 
     * 【功能说明】
     * 根据当前工作模式，恢复对应显示区域组件的刷新
     * 调用显示区域组件的onResume()方法，初始化OpenGL资源
     * 
     * 【调用时机】
     * - Activity/Fragment恢复时调用
     * - 需要恢复波形刷新时调用
     * - 应用从后台返回前台时调用
     * 
     * 【恢复规则】
     * - XY模式：恢复XY显示区域组件
     * - YT模式：恢复YT显示区域组件
     * - YTZOOM模式：恢复YT显示区域组件和YT缩放显示区域组件
     * 
     * 【注意事项】
     * - 需要在stopRefresh()或onPause()之后调用
     * - 会初始化OpenGL资源，需要正确管理生命周期
     */
    public void onResume() { // 恢复显示区域刷新的方法，1
        switch (mWorkMode) { // 根据当前工作模式恢复对应的显示区域组件，1
            case IWorkMode.WorkMode_XY: { // XY模式恢复逻辑，1
                waveZoneDisplay_xy.onResume(); // 恢复XY显示区域组件，初始化OpenGL资源，1
            } // 结束XY模式恢复逻辑块，1
            break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YT: { // YT模式恢复逻辑，1
                waveZoneDisplay_yt.onResume(); // 恢复YT显示区域组件，初始化OpenGL资源，1
            } // 结束YT模式恢复逻辑块，1
            break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YTZOOM: { // YT缩放模式恢复逻辑，1
                waveZoneDisplay_yt.onResume(); // 恢复YT显示区域组件，初始化OpenGL资源，1
                waveZoneDisplay_yt.getWaveZoneDisplayYtZoom().onResume(); // 恢复YT缩放显示区域组件，初始化OpenGL资源，1
            } // 结束YT缩放模式恢复逻辑块，1
            break; // 跳出switch语句，1
        } // 结束switch语句，1
    } // 结束onResume方法，1

    /**
     * 暂停显示区域刷新
     * 
     * 【功能说明】
     * 根据当前工作模式，暂停对应显示区域组件的刷新
     * 调用显示区域组件的onPause()方法，释放OpenGL资源
     * 
     * 【调用时机】
     * - Activity/Fragment暂停时调用
     * - 需要暂停波形刷新时调用
     * - 应用进入后台时调用
     * 
     * 【暂停规则】
     * - XY模式：暂停XY显示区域组件
     * - YT模式：暂停YT显示区域组件
     * - YTZOOM模式：暂停XY显示区域组件和YT缩放显示区域组件
     * 
     * 【注意事项】
     * - 调用此方法后，需要调用onResume()恢复刷新
     * - 会释放OpenGL资源，需要正确管理生命周期
     * 
     * 【注意】
     * YTZOOM模式下，暂停的是XY和YT缩放组件，而不是YT组件
     * 这可能是一个bug，YT组件应该也需要暂停
     */
    public void onPause() { // 暂停显示区域刷新的方法，1
        switch (mWorkMode) { // 根据当前工作模式暂停对应的显示区域组件，1
            case IWorkMode.WorkMode_XY: { // XY模式暂停逻辑，1
                waveZoneDisplay_xy.onPause(); // 暂停XY显示区域组件，释放OpenGL资源，1
            } // 结束XY模式暂停逻辑块，1
            break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YT: { // YT模式暂停逻辑，1
                waveZoneDisplay_yt.onPause(); // 暂停YT显示区域组件，释放OpenGL资源，1
            } // 结束YT模式暂停逻辑块，1
            break; // 跳出switch语句，1
            case IWorkMode.WorkMode_YTZOOM: { // YT缩放模式暂停逻辑，1
                waveZoneDisplay_xy.onPause(); // 暂停XY显示区域组件，释放OpenGL资源，1
                waveZoneDisplay_yt.getWaveZoneDisplayYtZoom().onPause(); // 暂停YT缩放显示区域组件，释放OpenGL资源，1
            } // 结束YT缩放模式暂停逻辑块，1
            break; // 跳出switch语句，1
        } // 结束switch语句，1
    } // 结束onPause方法，1


    /**
     * 调整XY模式的显示空间
     * 
     * 【功能说明】
     * 调整XY模式布局容器的Y坐标，使其在垂直方向上居中显示
     * 计算公式：Y坐标 = (主波形区域Y坐标 - XY波形区域高度) / 2
     * 
     * 【调用时机】
     * - 初始化显示区域时调用（initWaveZoneDisplay）
     * - 切换到XY模式时调用（setWorkMode、switchWorkMode）
     * - 波形区域尺寸变化时调用（changeWaveZoneSize）
     * 
     * 【调整逻辑】
     * 1. 从GlobalVar获取主波形区域的Y坐标和XY波形区域的高度
     * 2. 计算XY布局容器的Y坐标，使其居中显示
     * 3. 如果Y坐标发生变化，则更新布局参数
     * 
     * 【注意事项】
     * - 只在XY模式下生效
     * - 需要GlobalVar提供正确的尺寸信息
     */
    public void changeXYSpace() { // 调整XY模式显示空间的方法，1
        int temp = (GlobalVar.get().getMainWave().y - GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY)) / 2; // 计算XY布局容器的Y坐标，使其居中显示，1
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) absoluteLayout_xy.getLayoutParams(); // 获取XY布局容器的布局参数，1
        if (layoutParams.y != temp) { // 如果Y坐标发生变化，1
            layoutParams.y = temp; // 更新布局参数的Y坐标，1
            absoluteLayout_xy.setLayoutParams(layoutParams); // 应用XY布局容器的布局参数，1
        } // 结束Y坐标变化判断，1
    } // 结束changeXYSpace方法，1

    //region 动画处理
    /**
     * 动画处理说明
     * 
     * 【动画方向】
     * 动画的处理应该包括几个方向：
     * - 从YT到YTZOOM：上下拉伸动画
     * - 从YTZOOM到YT：上下收缩动画
     * - 从YT到XY：左右移动动画
     * - 从XY到YT：左右移动动画
     * 
     * 【动画实现】
     * - XY到YT：可以简单一点，左右移动
     * - YT到YTZOOM：可以上下拉伸
     * 
     * 【注意事项】
     * 当前动画处理部分尚未完全实现，AnimatorStateListener为空实现
     * 后续可以根据需要添加动画效果
     */

    /**
     * 动画状态监听器
     * 
     * 【功能说明】
     * 实现Animator.AnimatorListener接口，用于监听动画状态变化
     * 当前为空实现，预留后续扩展使用
     * 
     * 【监听事件】
     * - onAnimationStart：动画开始时触发
     * - onAnimationEnd：动画结束时触发
     * - onAnimationCancel：动画取消时触发
     * - onAnimationRepeat：动画重复时触发
     * 
     * 【使用场景】
     * - 工作模式切换动画
     * - 显示区域过渡动画
     * 
     * 【注意事项】
     * 当前为空实现，需要根据实际需求添加具体逻辑
     */
    class AnimatorStateListener implements Animator.AnimatorListener { // 动画状态监听器内部类，实现Animator.AnimatorListener接口，1
        /**
         * 动画开始时触发
         * 
         * 【功能说明】
         * 当动画开始播放时触发此方法
         * 当前为空实现，预留后续扩展使用
         * 
         * 【参数说明】
         * @param animation 动画对象
         */
        @Override // 标记为接口方法实现，1
        public void onAnimationStart(Animator animation) { // 动画开始时触发的方法，1

        } // 结束onAnimationStart方法，1

        /**
         * 动画结束时触发
         * 
         * 【功能说明】
         * 当动画播放结束时触发此方法
         * 当前为空实现，预留后续扩展使用
         * 
         * 【参数说明】
         * @param animation 动画对象
         */
        @Override // 标记为接口方法实现，1
        public void onAnimationEnd(Animator animation) { // 动画结束时触发的方法，1

        } // 结束onAnimationEnd方法，1

        /**
         * 动画取消时触发
         * 
         * 【功能说明】
         * 当动画被取消时触发此方法
         * 当前为空实现，预留后续扩展使用
         * 
         * 【参数说明】
         * @param animation 动画对象
         */
        @Override // 标记为接口方法实现，1
        public void onAnimationCancel(Animator animation) { // 动画取消时触发的方法，1

        } // 结束onAnimationCancel方法，1

        /**
         * 动画重复时触发
         * 
         * 【功能说明】
         * 当动画重复播放时触发此方法
         * 当前为空实现，预留后续扩展使用
         * 
         * 【参数说明】
         * @param animation 动画对象
         */
        @Override // 标记为接口方法实现，1
        public void onAnimationRepeat(Animator animation) { // 动画重复时触发的方法，1

        } // 结束onAnimationRepeat方法，1
    } // 结束AnimatorStateListener内部类定义，1
    //endregion
} // 结束WorkModeManage类定义，1