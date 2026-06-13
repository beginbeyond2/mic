package com.micsig.tbook.scope.Auto;  // 定义包名：示波器自动设置功能模块

/**
 * 自动设置（Auto Set）功能配置管理类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Auto（示波器自动设置功能模块）</li>
 *   <li>架构层级：业务逻辑层 - 配置管理层</li>
 *   <li>设计模式：单例模式（静态内部类持有者模式，线程安全）</li>
 *   <li>职责类型：自动设置参数配置、状态管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理自动设置功能的各项配置参数</li>
 *   <li>提供自动设置功能的开关控制</li>
 *   <li>存储自动设置的阈值、触发源等参数</li>
 *   <li>协调AutoService实现自动设置功能</li>
 * </ul>
 * 
 * <p><b>自动设置功能说明：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   Auto Set ─ 自动设置功能                                                │
 * │                                                                          │
 * │   功能开关：                                                              │
 * │   ├── autoChannelEnable    ─ 自动通道开启/关闭检测                       │
 * │   ├── autoRangeEnable      ─ 自动量程调整                                │
 * │   ├── autoVerticalEnable   ─ 自动垂直档位调整                            │
 * │   ├── autoHorizontalEnable ─ 自动水平时基调整                            │
 * │   └── autoLevelEnable      ─ 自动触发电平调整                            │
 * │                                                                          │
 * │   参数配置：                                                              │
 * │   ├── autoThresholdLevel   ─ 自动检测阈值电平（默认0.01V）               │
 * │   └── autoTriggerSource    ─ 自动触发源选择（当前/最大）                  │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>单例模式实现：</b>
 * <ul>
 *   <li>使用静态内部类AutoHolder持有单例实例</li>
 *   <li>JVM保证类加载时的线程安全性</li>
 *   <li>实现懒加载，只有在首次调用getInstance()时才创建实例</li>
 * </ul>
 * 
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>所有getter/setter方法使用synchronized关键字</li>
 *   <li>保证多线程环境下的数据一致性</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：AutoService（自动设置服务，执行实际的自动设置逻辑）</li>
 *   <li>被依赖：Scope（示波器核心管理，读取自动设置状态）</li>
 *   <li>被依赖：UI层（设置界面，配置自动设置参数）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-6-29
 * @see AutoService 自动设置服务
 */
public class Auto {
    
    // ==================== 自动触发源常量定义 ====================
    
    /** 自动触发源：使用当前触发源（保持现有触发源设置） */
    public static int AUTO_TRIGGER_SOURCE_CURRENT = 0;  // 值为0，表示使用当前触发源
    
    /** 自动触发源：使用最大触发源（自动选择最佳触发源） */
    public static int AUTO_TRIGGER_SOURCE_MAX = 1;  // 值为1，表示自动选择最大/最佳触发源

    // ==================== 单例模式实现（静态内部类持有者模式） ====================
    
    /**
     * 静态内部类：持有Auto单例实例
     * 
     * <p>利用JVM类加载机制保证线程安全的懒加载单例。
     * 只有在首次访问AutoHolder.instance时才会加载此类并创建实例。
     */
    private static class AutoHolder {
        /** Auto单例实例：final保证不可变，static保证类级别唯一 */
        public static final Auto instance = new Auto();  // 创建并持有唯一的Auto实例
    }

    /**
     * 获取Auto单例实例
     * 
     * <p>通过静态内部类持有者模式实现线程安全的懒加载单例。
     * 
     * @return Auto单例实例
     */
    public static Auto getInstance() {
        return Auto.AutoHolder.instance;  // 返回静态内部类持有的单例实例
    }

    /**
     * 私有构造方法：防止外部直接实例化
     * 
     * <p>单例模式要求构造方法私有化，确保只能通过getInstance()获取实例。
     */
    private Auto() {
        // 私有构造方法，不执行任何初始化操作
    }

    // ==================== 自动设置配置参数 ====================
    
    /** 自动通道开启使能：true表示自动检测并开启有信号的通道 */
    private boolean autoChannelEnable = true;  // 默认开启自动通道检测
    
    /** 自动阈值电平：用于判断信号是否存在的电压阈值，单位V（伏特） */
    private double autoThresholdLevel = 0.01;// V，默认0.01V，低于此值视为无信号
    
    /** 自动触发源：AUTO_TRIGGER_SOURCE_CURRENT或AUTO_TRIGGER_SOURCE_MAX */
    private int autoTriggerSource = AUTO_TRIGGER_SOURCE_CURRENT;  // 默认使用当前触发源
    
    /** 自动量程使能：true表示自动调整量程范围 */
    private boolean autoRangeEnable = false;  // 默认关闭自动量程
    
    /** 自动垂直设置使能：true表示自动调整垂直档位 */
    private boolean autoVerticalEnable = true;  // 默认开启自动垂直调整
    
    /** 自动水平设置使能：true表示自动调整水平时基 */
    private boolean autoHorizontalEnable = true;  // 默认开启自动水平调整
    
    /** 自动电平设置使能：true表示自动调整触发电平 */
    private boolean autoLevelEnable = true;  // 默认开启自动电平调整
    
    /** 自动运行标志：true表示正在进行自动设置 */
    private boolean autoRun = false;  // 默认未运行

    // ==================== 自动通道使能配置 ====================
    
    /**
     * 获取自动通道使能状态
     * 
     * <p>返回是否启用自动通道检测功能。
     * 开启后，自动设置会检测并开启有信号的通道。
     * 
     * @return true表示启用自动通道检测，false表示禁用
     */
    public synchronized boolean isAutoChannelEnable() {
        return autoChannelEnable;  // 返回自动通道使能状态
    }

    /**
     * 设置自动通道使能状态
     * 
     * <p>配置是否启用自动通道检测功能。
     * 
     * @param autoChannelEnable true表示启用，false表示禁用
     */
    public synchronized void setAutoChannelEnable(boolean autoChannelEnable) {
        this.autoChannelEnable = autoChannelEnable;  // 设置自动通道使能状态
    }

    // ==================== 自动阈值电平配置 ====================
    
    /**
     * 获取自动阈值电平
     * 
     * <p>返回用于判断信号是否存在的电压阈值。
     * 当信号幅度低于此阈值时，视为无信号。
     * 
     * @return 自动阈值电平，单位V（伏特）
     */
    public synchronized double getAutoThresholdLevel() {
        return autoThresholdLevel;  // 返回自动阈值电平值
    }

    /**
     * 设置自动阈值电平
     * 
     * <p>配置用于判断信号是否存在的电压阈值。
     * 
     * @param autoThresholdLevel 阈值电平值，单位V（伏特）
     */
    public  synchronized void setAutoThresholdLevel(double autoThresholdLevel) {
        this.autoThresholdLevel = autoThresholdLevel;  // 设置自动阈值电平值
    }

    // ==================== 自动触发源配置 ====================
    
    /**
     * 获取自动触发源设置
     * 
     * <p>返回自动设置时的触发源选择策略。
     * CURRENT表示保持当前触发源，MAX表示自动选择最佳触发源。
     * 
     * @return 触发源设置值（AUTO_TRIGGER_SOURCE_CURRENT或AUTO_TRIGGER_SOURCE_MAX）
     */
    public synchronized int getAutoTriggerSource() {
        return autoTriggerSource;  // 返回自动触发源设置值
    }

    /**
     * 设置自动触发源
     * 
     * <p>配置自动设置时的触发源选择策略。
     * 
     * @param autoTriggerSource 触发源设置值
     */
    public synchronized void setAutoTriggerSource(int autoTriggerSource) {
        this.autoTriggerSource = autoTriggerSource;  // 设置自动触发源值
    }

    // ==================== 自动量程使能配置 ====================
    
    /**
     * 获取自动量程使能状态
     * 
     * <p>返回是否启用自动量程调整功能。
     * 
     * @return true表示启用自动量程，false表示禁用
     */
    public synchronized boolean isAutoRangeEnable() {
        return autoRangeEnable;  // 返回自动量程使能状态
    }

    /**
     * 设置自动量程使能状态
     * 
     * <p>配置是否启用自动量程调整功能。
     * 
     * @param autoRangeEnable true表示启用，false表示禁用
     */
    public synchronized void setAutoRangeEnable(boolean autoRangeEnable) {
        this.autoRangeEnable = autoRangeEnable;  // 设置自动量程使能状态
    }

    // ==================== 自动垂直设置使能配置 ====================
    
    /**
     * 获取自动垂直设置使能状态
     * 
     * <p>返回是否启用自动垂直档位调整功能。
     * 开启后，自动设置会根据信号幅度调整垂直档位。
     * 
     * @return true表示启用自动垂直调整，false表示禁用
     */
    public synchronized boolean isAutoVerticalEnable() {
        return autoVerticalEnable;  // 返回自动垂直设置使能状态
    }

    /**
     * 设置自动垂直设置使能状态
     * 
     * <p>配置是否启用自动垂直档位调整功能。
     * 
     * @param autoVerticalEnable true表示启用，false表示禁用
     */
    public synchronized void setAutoVerticalEnable(boolean autoVerticalEnable) {
        this.autoVerticalEnable = autoVerticalEnable;  // 设置自动垂直设置使能状态
    }

    // ==================== 自动水平设置使能配置 ====================
    
    /**
     * 获取自动水平设置使能状态
     * 
     * <p>返回是否启用自动水平时基调整功能。
     * 开启后，自动设置会根据信号频率调整时基档位。
     * 
     * @return true表示启用自动水平调整，false表示禁用
     */
    public synchronized boolean isAutoHorizontalEnable() {
        return autoHorizontalEnable;  // 返回自动水平设置使能状态
    }

    /**
     * 设置自动水平设置使能状态
     * 
     * <p>配置是否启用自动水平时基调整功能。
     * 
     * @param autoHorizontalEnable true表示启用，false表示禁用
     */
    public synchronized void setAutoHorizontalEnable(boolean autoHorizontalEnable) {
        this.autoHorizontalEnable = autoHorizontalEnable;  // 设置自动水平设置使能状态
    }

    // ==================== 自动电平设置使能配置 ====================
    
    /**
     * 获取自动电平设置使能状态
     * 
     * <p>返回是否启用自动触发电平调整功能。
     * 开启后，自动设置会根据信号幅度调整触发电平。
     * 
     * @return true表示启用自动电平调整，false表示禁用
     */
    public synchronized boolean isAutoLevelEnable() {
        return autoLevelEnable;  // 返回自动电平设置使能状态
    }

    /**
     * 设置自动电平设置使能状态
     * 
     * <p>配置是否启用自动触发电平调整功能。
     * 注意：方法名有拼写错误（setAutoLevleEnable应为setAutoLevelEnable）。
     * 
     * @param autoLevelEnable true表示启用，false表示禁用
     */
    public synchronized void setAutoLevleEnable(boolean autoLevelEnable) {
        this.autoLevelEnable = autoLevelEnable;  // 设置自动电平设置使能状态
    }

    // ==================== 自动设置状态查询与控制 ====================
    
    /**
     * 查询自动设置功能是否启用
     * 
     * <p>委托给AutoService查询自动设置功能的启用状态。
     * 
     * @return true表示自动设置功能已启用，false表示未启用
     */
    public boolean isAuto() {
        return AutoService.isAutoEnable();  // 委托AutoService查询自动设置启用状态
    }

    /**
     * 设置自动设置功能启用状态
     * 
     * <p>委托给AutoService控制自动设置功能的启用/禁用。
     * 
     * @param bAuto true表示启用自动设置功能，false表示禁用
     */
    public void setAuto(boolean bAuto) {
        AutoService.setAuto(bAuto);  // 委托AutoService设置自动设置启用状态
    }


}
