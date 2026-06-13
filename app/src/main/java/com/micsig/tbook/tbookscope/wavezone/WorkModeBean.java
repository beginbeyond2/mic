package com.micsig.tbook.tbookscope.wavezone; // 波形显示区域包，包含示波器波形显示的核心组件，1

/**
 * 工作模式数据Bean - 示波器工作模式切换的数据载体
 * 
 * 【模块定位】
 * - 所属模块：wavezone（波形显示区域模块）
 * - 核心职责：封装工作模式切换的前后状态信息
 * - 架构层级：数据层，位于模式管理层和事件总线之间
 * 
 * 【核心职责】
 * 1. 模式状态封装：封装切换前后的工作模式状态
 * 2. 来源标识：标识模式切换请求的来源（EventBus或其他）
 * 3. 数据传递：作为事件总线的数据载体传递模式切换信息
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │            WorkModeManage（工作模式管理器）                   │
 * │                   (模式管理核心)                             │
 * │  - 处理模式切换请求                                          │
 * │  - 创建WorkModeBean实例                                     │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 创建Bean
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  WorkModeBean                                │
 * │               (工作模式数据载体)                              │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  数据字段                                             │  │
 * │  │  - preWorkMode    前一个工作模式                      │  │
 * │  │  - nextWorkMode   目标工作模式                        │  │
 * │  │  - isFromEventBus 是否来自EventBus                    │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 事件传递
 * ┌─────────────────────────────────────────────────────────────┐
 * │                   RxBus（事件总线）                           │
 * │                   (事件发布订阅)                             │
 * │  - 发布WAVEZONE_WORKMODE_CHANGE事件                        │
 * │  - 通知订阅者模式变化                                        │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 事件接收
 * ┌─────────────────────────────────────────────────────────────┐
 * │              模式变化订阅者                                  │
 * │  - UI组件更新                                               │
 * │  - 功能状态切换                                              │
 * │  - 日志记录                                                  │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【依赖关系】
 * - 创建方：WorkModeManage（工作模式管理器）
 * - 传递方：RxBus（事件总线）
 * - 接收方：模式变化订阅者（UI组件、功能模块等）
 * - 注解依赖：@WorkMode（工作模式注解）
 * - 无外部库依赖
 * 
 * 【使用场景】
 * 1. 模式切换通知：当工作模式切换时，创建Bean并通过EventBus通知订阅者
 * 2. 状态回溯：记录切换前的工作模式，用于状态回退或日志记录
 * 3. 来源追踪：标识切换请求来源，用于区分用户操作和程序内部切换
 * 4. 事件数据传递：作为RxJava事件的数据载体
 * 
 * 【数据流向】
 * WorkModeManage → 创建WorkModeBean → RxBus.post() → 订阅者接收 → 处理模式变化
 * 
 * 【字段说明】
 * 1. preWorkMode：切换前的工作模式，用于记录历史状态
 * 2. nextWorkMode：切换后的工作模式，标识目标状态
 * 3. isFromEventBus：标识切换来源，区分不同的触发方式
 * 
 * 【性能考虑】
 * - 对象创建：每次模式切换创建新实例，轻量级对象
 * - 事件传递：通过EventBus异步传递，不阻塞主线程
 * - 内存管理：Bean对象生命周期由EventBus管理
 * 
 * 【注意事项】
 * 1. 使用@WorkMode注解确保模式值合法性
 * 2. Bean对象为一次性使用，不缓存复用
 * 3. isFromEventBus标识需要正确设置
 * 4. 订阅者需要正确处理Bean数据
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/6/15
 * @see WorkModeManage
 * @see IWorkMode
 * @see RxBus
 */
public class WorkModeBean {
    /**
     * 切换前的工作模式标识
     * 记录模式切换前的状态，用于状态回溯和日志记录
     * 使用@WorkMode注解约束取值范围
     * 取值范围：WorkMode_None、WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     */
    @IWorkMode.WorkMode // 使用@WorkMode注解约束取值范围，1
    private int preWorkMode; // 切换前的工作模式标识，使用注解约束，1
    
    /**
     * 切换后的目标工作模式标识
     * 标识模式切换的目标状态
     * 使用@WorkMode注解约束取值范围
     * 取值范围：WorkMode_None、WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     */
    @IWorkMode.WorkMode // 使用@WorkMode注解约束取值范围，1
    private int nextWorkMode; // 切换后的目标工作模式标识，使用注解约束，1
    
    /**
     * 模式切换来源标识
     * 标识模式切换请求是否来自EventBus事件
     * true：来自EventBus事件触发
     * false：来自其他方式（如用户直接操作、程序内部切换等）
     * 取值范围：true或false
     */
    private boolean isFromEventBus; // 模式切换来源标识，true表示来自EventBus，1

    /**
     * 构造方法 - 创建工作模式数据Bean实例
     * 
     * 【功能说明】
     * 创建WorkModeBean实例，封装工作模式切换的前后状态和来源信息
     * 
     * 【参数说明】
     * @param preWorkMode 切换前的工作模式标识，使用@WorkMode注解约束取值范围
     *                    取值范围：WorkMode_None、WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * @param nextWorkMode 切换后的目标工作模式标识，使用@WorkMode注解约束取值范围
     *                     取值范围：WorkMode_None、WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * @param isFromEventBus 模式切换来源标识，标识是否来自EventBus事件
     *                       true：来自EventBus事件触发
     *                       false：来自其他方式
     * 
     * 【调用时机】
     * 在WorkModeManage.setWorkMode方法中创建，用于事件发布
     * 
     * 【使用示例】
     * WorkModeBean bean = new WorkModeBean(
     *     IWorkMode.WorkMode_YT,      // 前一个模式：YT模式
     *     IWorkMode.WorkMode_XY,      // 目标模式：XY模式
     *     false                       // 来源：非EventBus触发
     * );
     * RxBus.getInstance().post(RxEnum.WAVEZONE_WORKMODE_CHANGE, bean);
     */
    public WorkModeBean(@IWorkMode.WorkMode int preWorkMode, @IWorkMode.WorkMode int nextWorkMode, boolean isFromEventBus) {
        this.preWorkMode = preWorkMode; // 保存切换前的工作模式标识，1
        this.nextWorkMode = nextWorkMode; // 保存切换后的目标工作模式标识，1
        this.isFromEventBus = isFromEventBus; // 保存模式切换来源标识，1
    }

    /**
     * 获取切换前的工作模式标识
     * @return 返回切换前的工作模式标识，取值范围由@WorkMode注解约束
     */
    public int getPreWorkMode() {
        return preWorkMode; // 返回切换前的工作模式标识，1
    }

    /**
     * 设置切换前的工作模式标识
     * @param preWorkMode 切换前的工作模式标识，使用@WorkMode注解约束取值范围
     */
    public void setPreWorkMode(int preWorkMode) {
        this.preWorkMode = preWorkMode; // 设置切换前的工作模式标识，1
    }

    /**
     * 获取切换后的目标工作模式标识
     * 使用@WorkMode注解确保返回值合法性
     * @return 返回切换后的目标工作模式标识，取值范围由@WorkMode注解约束
     */
    @IWorkMode.WorkMode // 使用@WorkMode注解确保返回值合法性，1
    public int getNextWorkMode() {
        return nextWorkMode; // 返回切换后的目标工作模式标识，1
    }

    /**
     * 设置切换后的目标工作模式标识
     * @param nextWorkMode 切换后的目标工作模式标识，使用@WorkMode注解约束取值范围
     */
    public void setNextWorkMode(@IWorkMode.WorkMode int nextWorkMode) {
        this.nextWorkMode = nextWorkMode; // 设置切换后的目标工作模式标识，1
    }

    /**
     * 判断模式切换是否来自EventBus事件
     * @return 返回true表示来自EventBus事件触发，返回false表示来自其他方式
     */
    public boolean isFromEventBus() {
        return isFromEventBus; // 返回模式切换来源标识，1
    }

    /**
     * 设置模式切换来源标识
     * @param fromEventBus 模式切换来源标识，true表示来自EventBus，false表示来自其他方式
     */
    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus; // 设置模式切换来源标识，1
    }
} // 结束WorkModeBean类定义，1