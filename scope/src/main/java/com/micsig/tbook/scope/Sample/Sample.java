package com.micsig.tbook.scope.Sample;

import androidx.annotation.IntDef;
import com.micsig.tbook.scope.ScopeMessage;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 采样状态管理类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 状态管理</li>
 *   <li>设计模式：单例模式</li>
 *   <li>职责类型：采样状态与配置管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理采样类型（普通/峰值/平均/包络）</li>
 *   <li>管理采样状态（停止/运行/瞬态）</li>
 *   <li>管理触发和时钟的输入输出方向</li>
 *   <li>管理单次采样模式</li>
 *   <li>管理滚屏帧率</li>
 *   <li>协调采样参数变化事件</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>集中管理示波器采样相关状态</li>
 *   <li>提供线程安全的采样状态访问</li>
 *   <li>协调采样参数变化时的硬件更新</li>
 *   <li>支持多种采样模式切换</li>
 * </ul>
 * 
 * <p><b>采样类型说明：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 采样类型              │ 值  │ 说明                              │
 * ├───────────────────────┼─────┼──────────────────────────────────┤
 * │ SAMPLE_TYPE_NORMAL    │  0  │ 普通采样模式                      │
 * │ SAMPLE_TYPE_PEAK      │  1  │ 峰值检测模式                      │
 * │ SAMPLE_TYPE_AVERAGE   │  2  │ 平均采样模式                      │
 * │ SAMPLE_TYPE_ENVEL     │  3  │ 包络采样模式                      │
 * └───────────────────────┴─────┴──────────────────────────────────┘
 * </pre>
 * 
 * <p><b>采样状态说明：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 采样状态                │ 值  │ 说明                            │
 * ├─────────────────────────┼─────┼────────────────────────────────┤
 * │ SAMPLE_STOP             │  0  │ 停止采样                        │
 * │ SAMPLE_RUN              │  1  │ 正常运行                        │
 * │ SAMPLE_TRANSIENT        │  2  │ 瞬态模式                        │
 * │ SAMPLE_TRANSIENT_DIAPLAY│  3  │ 瞬态显示模式                    │
 * │ SAMPLE_TRANSIENT_RUN    │  4  │ 瞬态运行模式                    │
 * └─────────────────────────┴─────┴────────────────────────────────┘
 * </pre>
 * 
 * <p><b>类结构图：</b>
 * <pre>
 * Sample (单例类)
 *   │
 *   ├── 持有 ──→ SampleAction (动作处理器)
 *   │
 *   ├── 依赖 ──→ MemDepthFactory (存储深度工厂)
 *   │
 *   └── 依赖 ──→ ScopeMessage (示波器消息)
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>持有：SampleAction（采样动作处理器）</li>
 *   <li>依赖：MemDepthFactory（获取存储深度）</li>
 *   <li>依赖：ScopeMessage（示波器消息通信）</li>
 * </ul>
 * 
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>单例创建使用双重检查锁定</li>
 *   <li>状态变量使用synchronized保护</li>
 *   <li>使用volatile保证可见性</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>
 * // 获取采样管理实例
 * Sample sample = Sample.getInstance();
 * 
 * // 设置采样类型
 * sample.setSampleType(Sample.SAMPLE_TYPE_AVERAGE);
 * 
 * // 启动采样
 * sample.setRunSample(true);
 * 
 * // 获取当前存储深度
 * int depth = sample.getSampleMemDepth();
 * </pre>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/13
 * @see SampleAction 采样动作处理器
 * @see MemDepthFactory 存储深度工厂
 * @see ScopeMessage 示波器消息
 */
public class Sample {
    
    /**
     * 单例实例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>全局唯一的采样管理实例</li>
     *   <li>使用volatile保证多线程可见性</li>
     * </ul>
     * 
     * <p><b>线程安全：</b>volatile关键字确保多线程环境下的可见性，防止指令重排序
     */
    private static volatile Sample instance = null;

    /**
     * 获取单例实例
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>使用双重检查锁定（Double-Check Locking）保证线程安全</li>
     *   <li>延迟初始化，首次调用时创建实例</li>
     *   <li>避免每次调用都进行同步，提高性能</li>
     * </ul>
     * 
     * <p><b>实现原理：</b>
     * <pre>
     * 第一次检查：避免不必要的同步开销
     * 同步块：确保只有一个线程能创建实例
     * 第二次检查：防止其他线程在等待锁期间实例已被创建
     * </pre>
     * 
     * @return Sample单例实例
     */
    public static Sample getInstance() {
        // 第一次检查：如果实例已存在，直接返回，避免同步开销
        if (instance == null) {
            // 同步块：确保线程安全，只有一个线程能进入
            synchronized (Sample.class) {
                // 第二次检查：防止其他线程在等待锁期间已创建实例
                if (instance == null ) {
                    // 创建单例实例
                    instance = new Sample();
                }
            }
        }
        // 返回单例实例
        return instance;
    }
    
    /**
     * 采样类型注解定义
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>使用@IntDef定义采样类型的合法值</li>
     *   <li>编译时检查参数类型，防止传入非法值</li>
     *   <li>替代枚举，减少内存开销</li>
     * </ul>
     * 
     * <p><b>使用方式：</b>
     * <pre>
     * public void setSampleType(@SAMPLE_TYPE int sampleType) { ... }
     * </pre>
     */
    @IntDef({SAMPLE_TYPE_NORMAL, SAMPLE_TYPE_PEAK, SAMPLE_TYPE_AVERAGE, SAMPLE_TYPE_ENVEL})
    // 注解保留策略：仅在源码级别保留，编译后不保留
    @Retention(RetentionPolicy.SOURCE)
    // 定义采样类型注解接口
    public @interface SAMPLE_TYPE {}
    
    /**
     * 普通采样模式常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标准采样模式，最常用的采样方式</li>
     *   <li>每个采样点独立存储，不进行额外处理</li>
     *   <li>适用于大多数测量场景</li>
     * </ul>
     * 
     * <p><b>采样原理：</b>ADC按设定采样率直接采集信号，每个采样点独立保存
     */
    public static final int SAMPLE_TYPE_NORMAL = 0;
    
    /**
     * 峰值检测模式常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>检测并记录采样区间内的最大值和最小值</li>
     *   <li>适用于捕获快速瞬态信号和毛刺</li>
     *   <li>在慢速时基下仍能捕获高频信号</li>
     * </ul>
     * 
     * <p><b>采样原理：</b>在每个采样区间内记录最大值和最小值，确保不丢失快速变化的信号
     */
    public static final int SAMPLE_TYPE_PEAK = 1;
    
    /**
     * 平均采样模式常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>对多个采样波形进行平均计算</li>
     *   <li>降低随机噪声，提高测量精度</li>
     *   <li>适用于重复信号的精确测量</li>
     * </ul>
     * 
     * <p><b>采样原理：</b>多次采集同一信号，对每个时间点的采样值求平均，消除随机噪声
     * 
     * <p><b>注意：</b>平均采样模式下存储深度受限，通常限制为扩展存储深度（如36M）
     */
    public static final int SAMPLE_TYPE_AVERAGE = 2;
    
    /**
     * 包络采样模式常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录信号的最大最小包络</li>
     *   <li>适用于调幅信号分析和信号稳定性观察</li>
     *   <li>显示信号随时间变化的幅度范围</li>
     * </ul>
     * 
     * <p><b>采样原理：</b>在多次采集中记录每个时间点的最大值和最小值，形成包络波形
     * 
     * <p><b>注意：</b>包络采样模式下存储深度受限，通常限制为扩展存储深度（如36M）
     */
    public static final int SAMPLE_TYPE_ENVEL = 3;
    
    /**
     * 采样类型数量常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义采样类型的总数量</li>
     *   <li>用于数组初始化和循环边界</li>
     * </ul>
     */
    public static final int SAMPLE_TYPE_MAX = 4;

    /**
     * 单次采样模式标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>true：单次采样模式，触发后只采集一次然后停止</li>
     *   <li>false：连续采样模式，持续采集直到用户停止</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>单次模式适用于捕获单次事件或瞬态信号</li>
     *   <li>连续模式适用于常规信号观测</li>
     * </ul>
     * 
     * <p><b>线程安全：</b>使用volatile保证多线程可见性
     */
    private volatile boolean bSingle = false;
    
    /**
     * 触发输入输出方向标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>false：触发输出模式（默认），示波器输出触发信号给外部设备</li>
     *   <li>true：触发输入模式，示波器接收外部触发信号</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>触发输出：多台仪器同步测量时，示波器作为触发源</li>
     *   <li>触发输入：外部设备控制示波器的触发时机</li>
     * </ul>
     * 
     * <p><b>线程安全：</b>使用volatile保证多线程可见性
     */
    private volatile boolean bTriggerInOut = false;
    
    /**
     * 时钟输入输出方向标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>false：时钟输出模式（默认），示波器输出采样时钟给外部设备</li>
     *   <li>true：时钟输入模式，示波器使用外部时钟进行采样</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>时钟输出：多台仪器同步采样时，示波器提供主时钟</li>
     *   <li>时钟输入：外部设备提供精确时钟，实现同步采样</li>
     * </ul>
     * 
     * <p><b>线程安全：</b>使用volatile保证多线程可见性
     */
    private volatile boolean bClkInOut = false;
    
    /**
     * 当前采样类型
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储当前激活的采样类型</li>
     *   <li>取值范围：SAMPLE_TYPE_NORMAL(0) ~ SAMPLE_TYPE_ENVEL(3)</li>
     *   <li>默认值：SAMPLE_TYPE_NORMAL（普通采样模式）</li>
     * </ul>
     */
    private int SampleType = SAMPLE_TYPE_NORMAL;
    
    /**
     * 各采样类型的采样次数配置数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>数组索引对应采样类型（0=普通, 1=峰值, 2=平均, 3=包络）</li>
     *   <li>数组值表示该采样类型下的采样次数</li>
     *   <li>主要用于平均采样和包络采样模式</li>
     * </ul>
     * 
     * <p><b>使用说明：</b>
     * <ul>
     *   <li>平均采样：采样次数决定平均的波形数量</li>
     *   <li>包络采样：采样次数决定包络的波形数量</li>
     * </ul>
     */
    private int [] SampleNum = new int[SAMPLE_TYPE_MAX];

    /**
     * 滚屏帧率
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>滚屏模式（Scroll Mode）下的屏幕刷新帧率</li>
     *   <li>单位：帧/秒（fps）</li>
     *   <li>默认值：40fps</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>滚屏模式用于观察慢速变化的信号</li>
     *   <li>帧率影响波形滚动的流畅度</li>
     * </ul>
     */
    private int roolFrameRate = 40;

    /**
     * 获取滚屏帧率
     * 
     * <p><b>功能说明：</b>返回当前配置的滚屏模式刷新帧率
     * 
     * @return 滚屏帧率（单位：帧/秒）
     */
    public int getRoolFrameRate() {
        // 直接返回滚屏帧率配置值
        return roolFrameRate;
    }

    /**
     * 设置滚屏帧率
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>更新滚屏帧率配置</li>
     *   <li>使用synchronized保证线程安全</li>
     *   <li>触发采样类型变化事件，通知硬件更新</li>
     * </ul>
     * 
     * @param roolFrameRate 滚屏帧率（单位：帧/秒）
     */
    public void setRoolFrameRate(int roolFrameRate) {
        // 同步块：确保线程安全地更新滚屏帧率
        synchronized (this) {
            // 更新滚屏帧率成员变量
            this.roolFrameRate = roolFrameRate;
        }
        // 触发采样类型变化事件，通知FPGA更新配置
        sampleAction.SampleTypeChange();
    }

    /**
     * 采样动作处理器引用
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>持有采样动作处理器实例</li>
     *   <li>用于在状态变化时触发相应的动作处理</li>
     *   <li>在构造函数中创建，与Sample对象生命周期相同</li>
     * </ul>
     */
    private SampleAction sampleAction;

    /**
     * 私有构造函数（单例模式）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>私有化构造函数，防止外部直接创建实例</li>
     *   <li>初始化采样次数数组为0</li>
     *   <li>创建采样动作处理器实例</li>
     * </ul>
     * 
     * <p><b>初始化流程：</b>
     * <pre>
     * 1. 初始化采样次数数组（所有类型默认采样次数为0）
     * 2. 创建SampleAction实例，传入当前Sample对象引用
     * </pre>
     */
    private Sample() {
        // 循环初始化各采样类型的采样次数为0
        for (int i = 0; i < SAMPLE_TYPE_MAX; i++) {
            // 将第i种采样类型的采样次数初始化为0
            SampleNum[i] = 0;
        }
        // 创建采样动作处理器，传入当前Sample实例引用
        sampleAction = new SampleAction(this);
    }
    
    /**
     * 获取当前实际采样存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>便捷方法，委托给MemDepthFactory处理</li>
     *   <li>自动获取当前开启的通道数</li>
     *   <li>根据通道数计算实际可用的存储深度</li>
     * </ul>
     * 
     * <p><b>计算逻辑：</b>
     * <pre>
     * 实际存储深度 = 总存储深度 / 采样通道数
     * 例如：360M总深度，4通道开启，每通道90M
     * </pre>
     * 
     * @return 实际采样存储深度（单位：采样点数）
     */
    public int getSampleMemDepth() {
        // 委托给MemDepthFactory，自动获取当前通道数计算存储深度
        return MemDepthFactory.getSampleMemDepth();
    }
    
    /**
     * 获取指定通道数下的实际采样存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>便捷方法，委托给MemDepthFactory处理</li>
     *   <li>根据指定的通道数计算存储深度</li>
     *   <li>用于预计算或配置验证场景</li>
     * </ul>
     * 
     * @param chCnt 采样通道数（1~8）
     * @return 实际采样存储深度（单位：采样点数）
     */
    public int getSampleMemDepth(int chCnt) {
        // 委托给MemDepthFactory，根据指定通道数计算存储深度
        return MemDepthFactory.getSampleMemDepth(chCnt);
    }

    /**
     * 设置触发输入输出方向
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置触发信号的方向（输入/输出）</li>
     *   <li>使用synchronized保证线程安全</li>
     *   <li>触发采样类型变化事件，通知硬件更新</li>
     * </ul>
     * 
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>true：触发输入模式，接收外部触发信号</li>
     *   <li>false：触发输出模式（默认），输出触发信号给外部设备</li>
     * </ul>
     * 
     * @param bIn 输入方向标志（true=输入，false=输出）
     */
    public void setTriggerInOut(boolean bIn) {
        // 同步块：确保线程安全地更新触发方向标志
        synchronized (this) {
            // 更新触发输入输出方向标志
            this.bTriggerInOut = bIn;
        }
        // 触发采样类型变化事件，通知FPGA更新触发配置
        sampleAction.SampleTypeChange();
    }
    
    /**
     * 设置时钟输入输出方向
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置时钟信号的方向（输入/输出）</li>
     *   <li>使用synchronized保证线程安全</li>
     *   <li>触发采样类型变化事件，通知硬件更新</li>
     * </ul>
     * 
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>true：时钟输入模式，使用外部时钟</li>
     *   <li>false：时钟输出模式（默认），输出时钟给外部设备</li>
     * </ul>
     * 
     * @param bIn 输入方向标志（true=输入，false=输出）
     */
    public void setClkInOut(boolean bIn) {
        // 同步块：确保线程安全地更新时钟方向标志
        synchronized (this) {
            // 更新时钟输入输出方向标志
            this.bClkInOut = bIn;
        }
        // 触发采样类型变化事件，通知FPGA更新时钟配置
        sampleAction.SampleTypeChange();
    }

    /**
     * 判断是否为触发输入模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查当前触发信号的方向配置</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return true表示触发输入模式，false表示触发输出模式
     */
    public synchronized boolean isTriggerInOut() {
        // 返回触发输入输出方向标志
        return this.bTriggerInOut;
    }
    
    /**
     * 判断是否为时钟输入模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查当前时钟信号的方向配置</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return true表示时钟输入模式，false表示时钟输出模式
     */
    public synchronized boolean isClkInOut() {
        // 返回时钟输入输出方向标志
        return this.bClkInOut;
    }

    /**
     * 设置采样类型
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>切换采样模式（普通/峰值/平均/包络）</li>
     *   <li>验证采样类型有效性，无效值将被忽略</li>
     *   <li>触发采样类型变化事件，通知硬件更新</li>
     * </ul>
     * 
     * <p><b>业务影响：</b>
     * <ul>
     *   <li>切换到平均/包络模式时，存储深度可能受限</li>
     *   <li>不同采样类型影响波形显示和测量结果</li>
     * </ul>
     * 
     * @param sampleType 采样类型（使用@SAMPLE_TYPE注解限制参数范围）
     */
    public void setSampleType(@SAMPLE_TYPE int sampleType) {
        // 验证采样类型是否在有效范围内
        if (isValidSampleType(sampleType)) {
            // 更新当前采样类型
            this.SampleType = sampleType;
            // 触发采样类型变化事件，通知FPGA更新采样配置
            sampleAction.SampleTypeChange();
        }
        // 无效的采样类型将被忽略，不做任何处理
    }

    /**
     * 获取当前采样类型
     * 
     * <p><b>功能说明：</b>返回当前激活的采样类型
     * 
     * @return 当前采样类型（0=普通, 1=峰值, 2=平均, 3=包络）
     */
    public int getSampleType() {
        // 返回当前采样类型
        return SampleType;
    }
    
    /**
     * 设置单次采样模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>启用单次采样模式时，发送采样运行状态</li>
     *   <li>触发单次采样事件，通知FPGA进入单次触发模式</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户按下Single按钮时调用此方法</li>
     *   <li>单次触发后自动停止采样</li>
     *   <li>适用于捕获单次事件或瞬态信号</li>
     * </ul>
     * 
     * @param bSingle true启用单次模式，false禁用单次模式
     */
    public void setSingle(boolean bSingle) {
        // 同步块：确保线程安全地更新单次采样标志
        synchronized (this) {
            // 更新单次采样模式标志
            this.bSingle = bSingle;
        }
        // 如果启用单次采样模式
        if (bSingle) {
            // 发送采样运行状态，启动采样
            sendSampleState(SAMPLE_RUN);
            // 触发单次采样事件，通知FPGA进入单次触发模式
            sampleAction.SampleSingle();
        }
    }
    
    /**
     * 判断是否为单次采样模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查当前是否处于单次采样模式</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return true表示单次采样模式，false表示连续采样模式
     */
    public synchronized boolean isSingle() {
        // 返回单次采样模式标志
        return bSingle;
    }
    
    /**
     * 设置采样运行状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制采样的启动和停止</li>
     *   <li>发送采样状态消息给硬件层</li>
     *   <li>触发采样运行变化事件</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户按下Run/Stop按钮时调用此方法</li>
     *   <li>启动采样：参数为true</li>
     *   <li>停止采样：参数为false</li>
     * </ul>
     * 
     * @param bRunSample true启动采样，false停止采样
     */
    public void setRunSample(boolean bRunSample) {
        // 根据参数发送采样状态消息（RUN或STOP）
        sendSampleState(bRunSample ? SAMPLE_RUN : SAMPLE_STOP);
        // 触发采样运行变化事件，通知FPGA更新运行状态
        sampleAction.SampleRunChange();
    }
    
    /**
     * 判断是否正在采样
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查采样状态是否为非停止状态</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return true表示正在采样（运行或瞬态），false表示已停止
     */
    public synchronized boolean isRunSample() {
        // 判断采样状态是否不等于停止状态
        return sampleState != SAMPLE_STOP;
    }
    
    /**
     * 冻结采样（停止）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>停止采样并保持当前状态</li>
     *   <li>默认使用SAMPLE_STOP状态</li>
     *   <li>是frozenSample(SAMPLE_STOP)的便捷方法</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>需要立即停止采样时调用</li>
     *   <li>波形捕获完成后停止</li>
     * </ul>
     */
    public void frozenSample() {
        // 调用带参数的frozenSample方法，使用SAMPLE_STOP状态
        frozenSample(SAMPLE_STOP);
    }
    
    /**
     * 冻结采样（指定状态）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置指定的采样状态</li>
     *   <li>使用synchronized保证线程安全</li>
     *   <li>触发冻结变化事件，通知硬件更新</li>
     * </ul>
     * 
     * <p><b>状态说明：</b>
     * <ul>
     *   <li>SAMPLE_STOP：停止采样</li>
     *   <li>SAMPLE_TRANSIENT：瞬态模式</li>
     *   <li>SAMPLE_TRANSIENT_DISPLAY：瞬态显示模式</li>
     * </ul>
     * 
     * @param sampleState 目标采样状态
     */
    public void frozenSample(int sampleState) {
        // 同步块：确保线程安全地更新采样状态
        synchronized (this) {
            // 更新采样状态
            this.sampleState = sampleState;
            // 触发冻结变化事件，通知FPGA更新状态
            sampleAction.frozenChange();
        }
    }
    
    /**
     * 获取当前采样类型的采样次数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回当前激活采样类型的采样次数配置</li>
     *   <li>主要用于平均采样和包络采样模式</li>
     * </ul>
     * 
     * @return 当前采样类型的采样次数
     */
    public int getSampleNum() {
        // 返回当前采样类型对应的采样次数
        return SampleNum[SampleType];
    }
    
    /**
     * 获取指定采样类型的采样次数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回指定采样类型的采样次数配置</li>
     *   <li>验证采样类型有效性，无效类型返回0</li>
     * </ul>
     * 
     * @param sampleType 采样类型（使用@SAMPLE_TYPE注解限制参数范围）
     * @return 指定类型的采样次数，无效类型返回0
     */
    public int getSampleNum(@SAMPLE_TYPE int sampleType) {
        // 验证采样类型是否在有效范围内
        if (isValidSampleType(sampleType)) {
            // 返回指定采样类型对应的采样次数
            return SampleNum[sampleType];
        }
        // 无效的采样类型返回0
        return 0;
    }

    /**
     * 设置指定采样类型的采样次数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>验证采样类型有效性，无效值将被忽略</li>
     *   <li>更新指定采样类型的采样次数配置</li>
     *   <li>触发采样类型变化事件，通知硬件更新</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>平均采样：设置平均的波形数量</li>
     *   <li>包络采样：设置包络的波形数量</li>
     * </ul>
     * 
     * @param sampleType 采样类型（使用@SAMPLE_TYPE注解限制参数范围）
     * @param sampleNum 采样次数
     */
    public void setSampleNum(@SAMPLE_TYPE int sampleType, int sampleNum) {
        // 验证采样类型是否在有效范围内
        if (isValidSampleType(sampleType)) {
            // 更新指定采样类型的采样次数
            SampleNum[sampleType] = sampleNum;
            // 触发采样类型变化事件，通知FPGA更新配置
            sampleAction.SampleTypeChange();
        }
        // 无效的采样类型将被忽略，不做任何处理
    }
    
    /**
     * 设置当前采样类型的采样次数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>更新当前激活采样类型的采样次数配置</li>
     *   <li>触发采样类型变化事件，通知硬件更新</li>
     * </ul>
     * 
     * @param sampleNum 采样次数
     */
    public void setSampleNum(int sampleNum) {
        // 更新当前采样类型的采样次数
        SampleNum[SampleType] = sampleNum;
        // 触发采样类型变化事件，通知FPGA更新配置
        sampleAction.SampleTypeChange();
    }

    /**
     * 验证采样类型是否有效
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查采样类型值是否在合法范围内</li>
     *   <li>有效范围：SAMPLE_TYPE_NORMAL(0) ~ SAMPLE_TYPE_MAX(4)</li>
     * </ul>
     * 
     * @param sampleType 采样类型
     * @return true表示有效，false表示无效
     */
    private boolean isValidSampleType(@SAMPLE_TYPE int sampleType) {
        // 检查采样类型是否在有效范围内 [0, 4]
        return (sampleType >= SAMPLE_TYPE_NORMAL && sampleType <= SAMPLE_TYPE_MAX);
    }

    /**
     * 采样停止状态常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>示波器停止采样</li>
     *   <li>波形保持当前状态，不再更新</li>
     * </ul>
     */
    public static final int SAMPLE_STOP = 0;
    
    /**
     * 采样运行状态常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>示波器正常运行采样</li>
     *   <li>波形持续更新显示</li>
     * </ul>
     */
    public static final int SAMPLE_RUN = 1;
    
    /**
     * 采样瞬态模式常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>瞬态采样模式</li>
     *   <li>用于捕获瞬态信号</li>
     * </ul>
     */
    public static final int SAMPLE_TRANSIENT = 2;

    /**
     * 采样瞬态显示模式常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>瞬态显示模式</li>
     *   <li>显示瞬态波形但不进行新的采样</li>
     * </ul>
     */
    public static final int SAMPLE_TRANSIENT_DIAPLAY = 3;
    
    /**
     * 采样瞬态运行模式常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>瞬态运行模式</li>
     *   <li>在瞬态模式下继续运行采样</li>
     * </ul>
     */
    public static final int SAMPLE_TRANSIENT_RUN = 4;

    /**
     * 当前采样状态
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储当前采样的运行状态</li>
     *   <li>取值范围：SAMPLE_STOP(0) ~ SAMPLE_TRANSIENT_RUN(4)</li>
     *   <li>默认值：SAMPLE_STOP（停止状态）</li>
     * </ul>
     */
    private int sampleState = SAMPLE_STOP;
    
    /**
     * 设置采样状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>更新采样状态</li>
     *   <li>状态转换有限制规则</li>
     *   <li>唤醒等待的线程（用于同步等待）</li>
     * </ul>
     * 
     * <p><b>状态转换规则：</b>
     * <ul>
     *   <li>非停止状态：可以切换到任意状态</li>
     *   <li>停止状态：只能切换到运行状态（SAMPLE_RUN）</li>
     * </ul>
     * 
     * <p><b>线程同步：</b>
     * <ul>
     *   <li>使用synchronized保证线程安全</li>
     *   <li>调用notifyAll()唤醒等待的线程</li>
     * </ul>
     * 
     * @param state 目标采样状态
     */
    public synchronized void setSampleState(int state) {
        // 如果当前不是停止状态，允许切换到任意状态
        if (sampleState != SAMPLE_STOP) {
            // 更新采样状态
            this.sampleState = state;
        } else {
            // 如果当前是停止状态，只允许切换到运行状态
            if (state == SAMPLE_RUN) {
                // 更新采样状态为运行
                this.sampleState = SAMPLE_RUN;
            }
            // 其他状态转换请求将被忽略
        }
        // 唤醒所有等待的线程（用于sendSampleState的同步等待）
        this.notifyAll();
    }

    /**
     * 发送采样状态消息
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>通过ScopeMessage发送采样状态给硬件层</li>
     *   <li>等待示波器响应（最多3秒）</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 调用ScopeMessage.setSampleState()发送状态
     * 2. 如果发送成功且示波器正在运行：
     *    └─→ 等待示波器响应（最多3000ms）
     * 3. 如果示波器未运行：
     *    └─→ 返回false
     * </pre>
     * 
     * <p><b>同步机制：</b>
     * <ul>
     *   <li>调用wait()等待硬件响应</li>
     *   <li>setSampleState()会调用notifyAll()唤醒等待</li>
     *   <li>超时时间3000ms，防止无限等待</li>
     * </ul>
     * 
     * @param state 采样状态（SAMPLE_RUN或SAMPLE_STOP）
     * @return true表示成功，false表示失败
     */
    public synchronized boolean sendSampleState(int state) {
        // 初始化返回值为成功
        boolean bRet = true;
        // 获取ScopeMessage单例实例
        ScopeMessage scopeMessage = ScopeMessage.getInstance();
        // 发送采样状态消息，获取发送结果
        bRet = scopeMessage.setSampleState(state);
        // 如果发送成功
        if (bRet) {
            // 检查示波器是否正在运行
            if (scopeMessage.isRun()) {
                try {
                    // 等待硬件响应，最多等待3000ms
                    // setSampleState()会调用notifyAll()唤醒此等待
                    this.wait(3000);
                } catch (InterruptedException e) {
                    // 等待被中断，打印异常堆栈
                    e.printStackTrace();
                    // 设置返回值为失败
                    bRet = false;
                }
            } else {
                // 示波器未运行，返回失败
                return false;
            }
        }
        // 返回操作结果
        return bRet;
    }

    /**
     * 获取当前采样状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回当前采样的运行状态</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return 当前采样状态（0=停止, 1=运行, 2=瞬态, 3=瞬态显示, 4=瞬态运行）
     */
    public synchronized int getSampleState() {
        // 返回当前采样状态
        return this.sampleState;
    }

    /**
     * 判断是否为瞬态模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查当前采样状态是否为瞬态模式</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return true表示瞬态模式，false表示非瞬态模式
     */
    public synchronized boolean isTransient() {
        // 判断采样状态是否等于瞬态模式
        return sampleState == SAMPLE_TRANSIENT;
    }
}
