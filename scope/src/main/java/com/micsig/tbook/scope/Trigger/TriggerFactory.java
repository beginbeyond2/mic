package com.micsig.tbook.scope.Trigger;

import com.micsig.tbook.scope.channel.ChannelFactory;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   TriggerFactory - 触发器工厂类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的工厂类，负责创建和管理所有触发器实例。                         ║
 * ║   采用单例模式，确保全局只有一个触发器工厂实例。                              ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 创建和管理所有触发器实例（边沿/脉宽/逻辑/超时/视频/斜率/第N边沿/串行）  ║
 * ║   2. 管理触发器公共参数（TriggerCommon）                                     ║
 * ║   3. 提供触发器实例访问接口                                                  ║
 * ║   4. 提供触发类型切换接口                                                    ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 单例模式（Singleton Pattern）：确保全局只有一个工厂实例                 ║
 * ║   - 工厂模式（Factory Pattern）：创建和管理触发器实例                       ║
 * ║   - 双重检查锁定（Double-Checked Locking）：线程安全的单例实现              ║
 * ║                                                                              ║
 * ║ 【触发器实例管理】                                                           ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │  TriggerFactory                                                      │   ║
 * ║   │  ┌─────────────────────────────────────────────────────────────┐    │   ║
 * ║   │  │ triggers[] 数组                                              │    │   ║
 * ║   │  ├─────────────────────────────────────────────────────────────┤    │   ║
 * ║   │  │ [0]  TriggerEdge        → 边沿触发                          │    │   ║
 * ║   │  │ [1]  TriggerRunt        → 矮脉冲触发                        │    │   ║
 * ║   │  │ [2]  TriggerPulseWidth  → 脉宽触发                          │    │   ║
 * ║   │  │ [3]  TriggerLogic       → 逻辑触发                          │    │   ║
 * ║   │  │ [4]  TriggerTimeOut     → 超时触发                          │    │   ║
 * ║   │  │ [5]  TriggerVideo       → 视频触发                          │    │   ║
 * ║   │  │ [6]  TriggerSlope       → 斜率触发                          │    │   ║
 * ║   │  │ [7]  TriggerNEdge       → 第N边沿触发                       │    │   ║
 * ║   │  │ [8]  TriggerBus(S1)     → 串行触发S1                        │    │   ║
 * ║   │  │ [9]  TriggerBus(S2)     → 串行触发S2                        │    │   ║
 * ║   │  │ [10] TriggerBus(S3)     → 串行触发S3                        │    │   ║
 * ║   │  │ [11] TriggerBus(S4)     → 串行触发S4                        │    │   ║
 * ║   │  └─────────────────────────────────────────────────────────────┘    │   ║
 * ║   │                                                                      │   ║
 * ║   │  triggerCommon → TriggerCommon（公共参数管理）                       │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【单例实现说明】                                                             ║
 * ║   采用双重检查锁定（Double-Checked Locking）实现线程安全的单例：             ║
 * ║   1. 第一次检查：避免不必要的同步开销                                       ║
 * ║   2. 同步块：确保只有一个线程能创建实例                                     ║
 * ║   3. 第二次检查：防止多个线程同时通过第一次检查                             ║
 * ║   4. volatile关键字：防止指令重排序，确保实例完全初始化后对其他线程可见     ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → Trigger: 触发器基类                                                     ║
 * ║   → TriggerCommon: 触发器公共参数管理                                       ║
 * ║   → TriggerEdge: 边沿触发器                                                 ║
 * ║   → TriggerRunt: 矮脉冲触发器                                               ║
 * ║   → TriggerPulseWidth: 脉宽触发器                                           ║
 * ║   → TriggerLogic: 逻辑触发器                                                ║
 * ║   → TriggerTimeOut: 超时触发器                                              ║
 * ║   → TriggerVideo: 视频触发器                                                ║
 * ║   → TriggerSlope: 斜率触发器                                                ║
 * ║   → TriggerNEdge: 第N边沿触发器                                             ║
 * ║   → TriggerBus: 串行触发器                                                  ║
 * ║   → ChannelFactory: 获取串行通道数量                                        ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 获取当前触发器实例：TriggerFactory.getTriggerObj()                     ║
 * ║   2. 获取指定类型触发器：TriggerFactory.getTriggerObj(TRIG_TYPE_PULSE)      ║
 * ║   3. 切换触发类型：TriggerFactory.getInstance().setTriggerType()            ║
 * ║   4. 获取公共参数：TriggerFactory.getInstance().getTriggerCommon()          ║
 * ║   5. 判断通道是否为触发源：TriggerFactory.isTriggerSource(chIdx)             ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   单例实现使用双重检查锁定，保证线程安全。                                   ║
 * ║   触发器实例数组在构造时初始化，之后只读，无需同步。                         ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018/3/20                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerFactory {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例实例
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 单例实例
     * 使用volatile关键字防止指令重排序，确保实例完全初始化后对其他线程可见
     *
     * @note volatile保证：
     *       1. 可见性：一个线程修改后，其他线程立即可见
     *       2. 禁止指令重排序：防止对象在构造完成前就被其他线程使用
     */
    private static volatile TriggerFactory instance = null;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取触发器工厂单例实例
     * 采用双重检查锁定（Double-Checked Locking）实现线程安全的单例
     *
     * @return 触发器工厂单例实例
     *
     * <p><b>双重检查锁定流程：</b>
     * <pre>
     * 1. 第一次检查 instance == null
     *    → 如果不为null，直接返回，避免同步开销
     *
     * 2. 同步块 synchronized (TriggerFactory.class)
     *    → 确保只有一个线程能进入创建实例
     *
     * 3. 第二次检查 instance == null
     *    → 防止多个线程同时通过第一次检查后重复创建
     *
     * 4. 创建实例 instance = new TriggerFactory()
     *    → 初始化所有触发器实例
     * </pre>
     *
     * @example
     *   TriggerFactory factory = TriggerFactory.getInstance();
     */
    public static TriggerFactory getInstance() {
        if (instance == null) {
            synchronized (TriggerFactory.class) {
                if (instance == null) {
                    instance = new TriggerFactory();
                }
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 触发器公共参数管理对象
     * 管理所有触发类型共享的参数（触发模式、触发类型、释抑时间等）
     */
    private TriggerCommon triggerCommon;

    /**
     * 触发器实例数组
     * 索引对应触发类型ID，存储对应的触发器实例
     *
     * <p><b>数组映射：</b>
     * <pre>
     * triggers[0]  = TriggerEdge        (TRIG_TYPE_EDGE)
     * triggers[1]  = TriggerRunt        (TRIG_TYPE_LOW_PULSE)
     * triggers[2]  = TriggerPulseWidth  (TRIG_TYPE_PULSE)
     * triggers[3]  = TriggerLogic       (TRIG_TYPE_LOGIC)
     * triggers[4]  = TriggerTimeOut     (TRIG_TYPE_TIMEOUT)
     * triggers[5]  = TriggerVideo       (TRIG_TYPE_VIDEO)
     * triggers[6]  = TriggerSlope       (TRIG_TYPE_SLOPE)
     * triggers[7]  = TriggerNEdge       (TRIG_TYPE_NEDGE)
     * triggers[8]  = TriggerBus(S1)     (TRIG_TYPE_SERIAL1)
     * triggers[9]  = TriggerBus(S2)     (TRIG_TYPE_SERIAL2)
     * triggers[10] = TriggerBus(S3)     (TRIG_TYPE_SERIAL3)
     * triggers[11] = TriggerBus(S4)     (TRIG_TYPE_SERIAL4)
     * </pre>
     */
    private Trigger [] triggers = new Trigger[Trigger.TRIG_TYPE_MAX_ID];

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 私有构造方法
     * 初始化触发器公共参数和所有触发器实例
     *
     * <p><b>初始化流程：</b>
     * <ol>
     *   <li>创建TriggerCommon实例，管理公共参数</li>
     *   <li>创建8种基本触发器实例：
     *     <ul>
     *       <li>边沿触发（TriggerEdge）</li>
     *       <li>矮脉冲触发（TriggerRunt）</li>
     *       <li>脉宽触发（TriggerPulseWidth）</li>
     *       <li>逻辑触发（TriggerLogic）</li>
     *       <li>超时触发（TriggerTimeOut）</li>
     *       <li>视频触发（TriggerVideo）</li>
     *       <li>斜率触发（TriggerSlope）</li>
     *       <li>第N边沿触发（TriggerNEdge）</li>
     *     </ul>
     *   </li>
     *   <li>根据串行通道数量创建串行触发器实例（TriggerBus）</li>
     * </ol>
     *
     * <p><b>串行触发器动态创建：</b>
     * <pre>
     * 假设 ChannelFactory.getMaxSerialIdx() = 28 (S1=24, S2=25, S3=26, S4=27)
     * 则创建4个串行触发器：
     *   triggers[8]  = new TriggerBus(TRIG_TYPE_SERIAL1)  // S1
     *   triggers[9]  = new TriggerBus(TRIG_TYPE_SERIAL2)  // S2
     *   triggers[10] = new TriggerBus(TRIG_TYPE_SERIAL3)  // S3
     *   triggers[11] = new TriggerBus(TRIG_TYPE_SERIAL4)  // S4
     * </pre>
     *
     * @note 构造方法是私有的，只能通过getInstance()获取实例
     */
    private TriggerFactory(){
        triggerCommon = new TriggerCommon();
        triggers[Trigger.TRIG_TYPE_EDGE] = new TriggerEdge();
        triggers[Trigger.TRIG_TYPE_LOW_PULSE] = new TriggerRunt();
        triggers[Trigger.TRIG_TYPE_PULSE] = new TriggerPulseWidth();
        triggers[Trigger.TRIG_TYPE_LOGIC] = new TriggerLogic();
        triggers[Trigger.TRIG_TYPE_TIMEOUT] = new TriggerTimeOut();
        triggers[Trigger.TRIG_TYPE_VIDEO] = new TriggerVideo();
        triggers[Trigger.TRIG_TYPE_SLOPE] = new TriggerSlope();
        triggers[Trigger.TRIG_TYPE_NEDGE] = new TriggerNEdge();

        int maxIdx = ChannelFactory.getMaxSerialIdx();
        for (int i = ChannelFactory.S1; i < maxIdx; i++) {
            int j = i - ChannelFactory.S1;
            triggers[Trigger.TRIG_TYPE_SERIAL1 + j] = new TriggerBus(Trigger.TRIG_TYPE_SERIAL1 + j);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公共参数访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取触发器公共参数管理对象
     *
     * @return TriggerCommon实例
     *
     * @example
     *   TriggerCommon common = TriggerFactory.getInstance().getTriggerCommon();
     *   common.setTriggerMode(TM_AUTO);
     */
    public TriggerCommon getTriggerCommon(){
        return triggerCommon;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发器实例访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前触发类型的触发器实例
     * 根据TriggerCommon中保存的触发类型ID获取对应的触发器实例
     *
     * @return 当前触发类型的触发器实例，如果触发类型无效返回null
     *
     * @example
     *   Trigger trigger = TriggerFactory.getInstance().getTrigger();
     *   trigger.setTriggerSource(CH1);
     */
    public Trigger getTrigger(){
        int triggerType = triggerCommon.getTriggerType();
        return getTrigger(triggerType);
    }

    /**
     * 获取指定触发类型的触发器实例
     *
     * @param triggerType 触发类型ID
     *                    TRIG_TYPE_EDGE (0): 边沿触发
     *                    TRIG_TYPE_PULSE (2): 脉宽触发
     *                    TRIG_TYPE_LOGIC (3): 逻辑触发
     *                    等等...
     *
     * @return 指定触发类型的触发器实例，如果触发类型无效返回null
     *
     * @see #isValidTriggerType(int)
     */
    public Trigger getTrigger(int triggerType){
        Trigger trigger = null;
        if(isValidTriggerType(triggerType)){
            trigger = triggers[triggerType];
        }
        return trigger;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发类型设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置当前触发类型
     * 更新TriggerCommon中的触发类型，会触发FPGA消息和事件通知
     *
     * @param triggerType 触发类型ID
     *                    TRIG_TYPE_EDGE (0): 边沿触发
     *                    TRIG_TYPE_PULSE (2): 脉宽触发
     *                    TRIG_TYPE_LOGIC (3): 逻辑触发
     *                    等等...
     *
     * @note 设置后会通过TriggerCommonAction发送多个FPGA命令
     *
     * @see TriggerCommon#setTriggerType(int)
     */
    public void setTriggerType(int triggerType){
        if(isValidTriggerType(triggerType)){
            triggerCommon.setTriggerType(triggerType);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 工具方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断触发类型ID是否有效
     * 有效范围：TRIG_TYPE_START_ID ~ TRIG_TYPE_MAX_ID
     *
     * @param triggerType 触发类型ID
     * @return true: 触发类型有效
     *         false: 触发类型无效
     */
    public static boolean isValidTriggerType(int triggerType){
        return (triggerType>=Trigger.TRIG_TYPE_START_ID && triggerType <= Trigger.TRIG_TYPE_MAX_ID);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 静态便捷方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取指定触发类型的触发器实例（静态便捷方法）
     *
     * @param triggerType 触发类型ID
     * @return 指定触发类型的触发器实例，如果触发类型无效返回null
     *
     * @example
     *   Trigger trigger = TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_PULSE);
     */
    public static Trigger getTriggerObj(int triggerType){
        return getInstance().getTrigger(triggerType);
    }

    /**
     * 获取当前触发类型的触发器实例（静态便捷方法）
     *
     * @return 当前触发类型的触发器实例
     *
     * @example
     *   Trigger trigger = TriggerFactory.getTriggerObj();
     */
    public static Trigger getTriggerObj(){
        return getInstance().getTrigger();
    }

    /**
     * 获取当前触发类型ID（静态便捷方法）
     *
     * @return 当前触发类型ID
     *
     * @example
     *   int type = TriggerFactory.getTriggerType();
     */
    public static int getTriggerType(){
        return getInstance().getTriggerCommon().getTriggerType();
    }

    /**
     * 判断指定通道是否为当前触发源
     * 遍历当前触发器的所有触发源，检查是否包含指定通道
     *
     * @param chIdx 通道索引
     *              0-7: CH1-CH8
     *              8-15: MATH1-8
     *              16-23: REF1-8
     *              24-27: S1-4
     *
     * @return true: 该通道是当前触发源
     *         false: 该通道不是当前触发源
     *
     * @example
     *   if (TriggerFactory.isTriggerSource(CH1)) {
     *       // CH1是当前触发源
     *   }
     */
    public static boolean isTriggerSource(int chIdx){
        Trigger trigger = getInstance().getTrigger();
        for(int i=0;i<trigger.getTriggerSourceCnt();i++){
            if(trigger.getTriggerSource(i) == chIdx){
                return true;
            }
        }
        return false;
    }


}
