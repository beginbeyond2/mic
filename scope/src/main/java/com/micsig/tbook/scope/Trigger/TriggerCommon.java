package com.micsig.tbook.scope.Trigger;

import android.util.Log;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    TriggerCommon - 触发器公共参数管理类                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的公共参数管理类，管理所有触发类型共享的参数。                   ║
 * ║   包括触发模式、触发类型、触发释抑时间、外部触发参数等。                     ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理触发模式（自动触发/正常触发）                                       ║
 * ║   2. 管理当前触发类型（边沿/脉宽/逻辑等）                                    ║
 * ║   3. 管理触发释抑时间（两次触发之间的最小时间间隔）                          ║
 * ║   4. 管理外部触发参数（输入阻抗、触发电平）                                  ║
 * ║   5. 管理触发灵敏度                                                          ║
 * ║                                                                              ║
 * ║ 【触发模式说明】                                                             ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 模式           │ 说明                                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 自动触发       │ 无论是否满足触发条件，都定期强制触发采集            │  ║
 * ║   │ (TM_AUTO)      │ 适用于信号调试，波形始终刷新                        │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 正常触发       │ 只有满足触发条件时才触发采集                        │  ║
 * ║   │ (TM_NORMAL)    │ 适用于稳定信号捕获，波形稳定显示                    │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【触发释抑时间说明】                                                         ║
 * ║   触发释抑（Holdoff）是指两次触发之间的最小时间间隔。                        ║
 * ║   在释抑时间内，即使满足触发条件也不会触发。                                 ║
 * ║   用于稳定显示周期性复杂信号，避免误触发。                                   ║
 * ║                                                                              ║
 * ║   示例：                                                                     ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐  ║
 * ║   │ 信号：脉冲序列，周期100ns，每个周期有多个边沿                        │  ║
 * ║   │ 问题：如果每个边沿都触发，波形不稳定                                │  ║
 * ║   │ 解决：设置释抑时间 > 100ns，只在每个周期的第一个边沿触发            │  ║
 * ║   └─────────────────────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【外部触发说明】                                                             ║
 * ║   外部触发允许使用外部信号作为触发源。                                       ║
 * ║   - 输入阻抗：1MΩ 或 50Ω                                                   ║
 * ║   - 触发电平：外部信号的触发阈值电压                                        ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → Trigger: 触发类型常量定义                                               ║
 * ║   → TriggerCommonAction: 参数变更时的消息发送代理                           ║
 * ║   → TriggerFactory: 持有TriggerCommon单例                                   ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户切换触发模式（自动/正常）时                                        ║
 * ║   2. 用户切换触发类型（边沿/脉宽/逻辑等）时                                 ║
 * ║   3. 用户调整触发释抑时间时                                                 ║
 * ║   4. 用户配置外部触发参数时                                                 ║
 * ║   5. 用户调整触发灵敏度时                                                   ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   外部触发参数使用synchronized保护，其他参数未加同步保护。                   ║
 * ║   建议在UI线程操作。                                                        ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018/3/20                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerCommon {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发模式常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 自动触发模式
     * 无论是否满足触发条件，示波器都会定期强制触发采集
     * 适用于信号调试，波形始终刷新，便于观察信号变化
     */
    public static final int TM_AUTO = 0;

    /**
     * 正常触发模式
     * 只有满足触发条件时才触发采集
     * 适用于稳定信号捕获，波形稳定显示
     */
    public static final int TM_NORMAL = 1;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时间转换常量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * FPGA触发释抑时间单位：4纳秒
     * FPGA内部时钟周期，用于释抑时间值到FPGA单位的转换
     */
    private static final int TRIGGER_HOLDOFF_TIME_UNIT = 4;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 触发参数
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 进入自动触发的时间阈值
     * 单位：毫秒
     * 默认值：1000ms（1秒）
     *
     * 当在正常触发模式下，超过此时间未触发时，自动切换到自动触发模式
     */
    private int triggerEnterAutoTime = 1000;

    /**
     * 触发释抑时间
     * 单位：FPGA时间单位（4ns）
     * 默认值：200 * 1000 * 1000 / 4 = 50,000,000 (200ms)
     *
     * 释抑时间内，即使满足触发条件也不会触发
     * 用于稳定显示周期性复杂信号
     */
    private long triggerHoldOffTime = 200*1000*1000/TRIGGER_HOLDOFF_TIME_UNIT;

    /**
     * 当前触发模式
     * 取值：TM_AUTO 或 TM_NORMAL
     * 默认值：TM_AUTO（自动触发）
     */
    private int triggerMode = TM_AUTO;

    /**
     * 当前触发类型
     * 取值范围：Trigger.TRIG_TYPE_START_ID ~ Trigger.TRIG_TYPE_MAX_ID
     * 默认值：TRIG_TYPE_START_ID（边沿触发）
     *
     * @see Trigger#TRIG_TYPE_EDGE
     * @see Trigger#TRIG_TYPE_PULSE
     * @see Trigger#TRIG_TYPE_LOGIC
     */
    private int triggerType = Trigger.TRIG_TYPE_START_ID;

    /**
     * 触发器公共参数动作代理
     * 负责参数变更时发送FPGA消息和事件通知
     */
    private TriggerCommonAction triggerCommonAction;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时间转换工具方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 将触发释抑时间转换为FPGA单位
     * FPGA内部使用4ns为基本时间单位
     *
     * @param val 释抑时间（纳秒）
     * @return FPGA时间单位值
     *
     * @example
     *   trriggerHoldOffTime2FpgaUnit(200*1000*1000) = 200,000,000 / 4 = 50,000,000
     */
    public static long trriggerHoldOffTime2FpgaUnit(long val){
        return val/TRIGGER_HOLDOFF_TIME_UNIT;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造触发器公共参数管理对象
     * 创建TriggerCommonAction动作代理
     */
    public TriggerCommon() {
        triggerCommonAction = new TriggerCommonAction(this);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发释抑时间访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取触发释抑时间
     *
     * @return 触发释抑时间（FPGA时间单位，4ns）
     */
    public long getTriggerHoldOffTime() {
        return triggerHoldOffTime;
    }

    /**
     * 设置触发释抑时间
     * 更新释抑时间并发送FPGA消息
     *
     * @param triggerHoldOffTime 触发释抑时间（纳秒）
     *                           取值范围：根据示波器规格确定
     *
     * @note 设置后会发送FPGA_CMD_TRIG_RETRAIN命令更新FPGA
     */
    public void setTriggerHoldOffTime(long triggerHoldOffTime) {
        this.triggerHoldOffTime = triggerHoldOffTime;
        triggerCommonAction.TriggerHoldOffTimeChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发模式访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前触发模式
     *
     * @return 触发模式
     *         TM_AUTO: 自动触发
     *         TM_NORMAL: 正常触发
     */
    public int getTriggerMode() {
        return triggerMode;
    }

    /**
     * 设置触发模式
     * 更新触发模式并发送FPGA消息
     *
     * @param triggerMode 触发模式
     *                    TM_AUTO: 自动触发
     *                    TM_NORMAL: 正常触发
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令更新FPGA
     */
    public void setTriggerMode(int triggerMode) {
        if(triggerMode == TM_NORMAL){
        }
        this.triggerMode = triggerMode;
        triggerCommonAction.TriggerModeChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发类型访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前触发类型
     *
     * @return 触发类型ID
     *         TRIG_TYPE_EDGE (0): 边沿触发
     *         TRIG_TYPE_PULSE (2): 脉宽触发
     *         TRIG_TYPE_LOGIC (3): 逻辑触发
     *         等等...
     *
     * @see Trigger#TRIG_TYPE_EDGE
     * @see Trigger#TRIG_TYPE_PULSE
     * @see Trigger#TRIG_TYPE_LOGIC
     */
    public int getTriggerType() {
        return triggerType;
    }

    /**
     * 设置触发类型
     * 更新触发类型并发送复合FPGA消息
     *
     * @param triggerType 触发类型ID
     *                    取值范围：Trigger.TRIG_TYPE_START_ID ~ Trigger.TRIG_TYPE_MAX_ID
     *
     * @note 设置后会发送多个FPGA命令：
     *       - FPGA_CMD_SAMP_MODE: 采样模式
     *       - FPGA_CMD_COUY: 耦合
     *       - FPGA_CMD_TRIG_HUICHA: 触发回差
     *       - FPGA_CMD_TRIG_COUPLE: 触发耦合
     *       - FPGA_CMD_TRIG_LEVEL: 触发电平
     *       - FPGA_CMD_TRIG: 触发参数
     *       - FPGA_CMD_TRIG_OFFSET: 触发偏移
     *       - FPGA_CMD_AUTO_TRIG_T: 自动触发时间
     */
    public void setTriggerType(int triggerType) {
        this.triggerType = triggerType;
        triggerCommonAction.TriggerTypeChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 外部触发输入阻抗常量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 外部触发输入阻抗：1MΩ
     * 高阻抗输入，适用于一般信号测量
     */
    public static final int EXT_TRIGGER_1M = 0;

    /**
     * 外部触发输入阻抗：50Ω
     * 低阻抗输入，适用于高频信号测量
     */
    public static final int EXT_TRIGGER_50 = 1;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 外部触发参数
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 外部触发输入阻抗
     * 取值：EXT_TRIGGER_1M 或 EXT_TRIGGER_50
     * 默认值：EXT_TRIGGER_1M（1MΩ）
     */
    private int extInputRes = EXT_TRIGGER_1M;

    /**
     * 外部触发电平
     * 单位：伏特（V）
     * 默认值：2.5V
     *
     * 外部触发信号的触发阈值电压
     */
    private double extTriggerLevel = 2.5;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 外部触发输入阻抗访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置外部触发输入阻抗
     * 更新输入阻抗并发送FPGA消息
     *
     * @param inputRes 输入阻抗
     *                 EXT_TRIGGER_1M: 1MΩ
     *                 EXT_TRIGGER_50: 50Ω
     *
     * @note 线程安全：使用synchronized保护
     * @note 设置后会发送FPGA_CMD_EXT_RESISTANCE命令
     */
    public synchronized void setExtTriggerInputRes(int inputRes){
         this.extInputRes = inputRes;
        triggerCommonAction.extTriggerRes();
    }

    /**
     * 获取外部触发输入阻抗
     *
     * @return 输入阻抗
     *         EXT_TRIGGER_1M: 1MΩ
     *         EXT_TRIGGER_50: 50Ω
     *
     * @note 线程安全：使用synchronized保护
     */
    public synchronized int getExtTriggerInputRes(){
        return extInputRes;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 外部触发电平访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置外部触发电平
     * 更新触发电平并发送FPGA消息
     *
     * @param v 触发电平（伏特）
     *          取值范围：根据示波器规格确定，通常 -5V ~ +5V
     *
     * @note 线程安全：使用synchronized保护
     * @note 设置后会发送FPGA_CMD_EXT_TRIGGER_LEVEL命令
     */
    public synchronized void setExtTriggerLevle(double v){
        extTriggerLevel = v;
        triggerCommonAction.extTriggerLevel();
    }

    /**
     * 获取外部触发电平
     *
     * @return 触发电平（伏特）
     *
     * @note 线程安全：使用synchronized保护
     */
    public synchronized double getExtTriggerLevel(){
        return extTriggerLevel;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 触发灵敏度
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发灵敏度
     * 单位：格（div）
     * 默认值：0.5格
     *
     * 触发灵敏度用于设置触发系统的迟滞范围。
     * 灵敏度越高（值越小），触发越灵敏，但也越容易受噪声影响。
     */
    private double sensitivity = 0.5;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发灵敏度访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置触发灵敏度
     * 更新灵敏度并发送FPGA消息
     *
     * @param sensitivity 触发灵敏度（格）
     *                    取值范围：通常 0.1 ~ 1.0
     *
     * @note 线程安全：使用synchronized保护
     * @note 设置后会发送FPGA_CMD_TRIG_LEVEL命令
     */
    public synchronized void setTriggerSensitivity(double sensitivity){
        this.sensitivity = sensitivity;
        triggerCommonAction.triggersensitivity();
    }

    /**
     * 获取触发灵敏度
     *
     * @return 触发灵敏度（格）
     *
     * @note 线程安全：使用synchronized保护
     */
    public synchronized double getTriggerSensitivity(){
        return this.sensitivity;
    }
}
