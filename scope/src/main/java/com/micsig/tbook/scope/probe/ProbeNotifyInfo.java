package com.micsig.tbook.scope.probe;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                ProbeNotifyInfo - 探头通知信息类                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Probe模块的探头通知信息类，位于probe包下，                                    ║
 * ║   用于封装探头事件通知的数据，包括通道索引和事件ID。                              ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 定义探头报警类型常量                                                     ║
 * ║   2. 定义探头校准状态常量                                                     ║
 * ║   3. 定义探头零点状态常量                                                     ║
 * ║   4. 封装通知信息数据（通道索引、事件ID）                                      ║
 * ║   5. 提供线程安全的数据访问                                                   ║
 * ║                                                                              ║
 * ║ 【通知类型分类】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        通知类型分类                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【报警类型】                                                        │ ║
 * ║   │   ALARM_RANGE_OUT (0x01)        - 量程超限报警                       │ ║
 * ║   │   ALARM_BATTERY_LOW (0x02)      - 电池电量低报警                     │ ║
 * ║   │   ALARM_COMM_ABNORMAL (0x03)    - 通信异常报警                       │ ║
 * ║   │   ALARM_50_SWITCH (0x04)        - 50Ω开关报警                        │ ║
 * ║   │   ALARM_CONV_HIGH_TEMP (0x05)   - 转换器高温报警                     │ ║
 * ║   │   ALARM_COMP_HIGH_TEMP (0x06)   - 补偿器高温报警                     │ ║
 * ║   │   ALARM_ACDC_ABNORMAL (0x07)    - AC/DC异常报警                      │ ║
 * ║   │   ALARM_REPLACE_BATTERY (0x0A)  - 更换电池报警                       │ ║
 * ║   │   ALARM_MISMATCH (0x0B)         - 不匹配报警                         │ ║
 * ║   │   ALARM_ATTENUATOR_ERROR (0x0C) - 衰减器错误报警                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【校准状态】                                                        │ ║
 * ║   │   ADJUST_SQUAREWAVE_CLOSE (0x02) - 方波关闭                         │ ║
 * ║   │   ADJUST_SQUAREWAVE_OPEN (0x01)  - 方波打开                         │ ║
 * ║   │   ADJUST_REQ (0x00)              - 校准请求                         │ ║
 * ║   │   ADJUST_SUCCESS (0xA1)          - 校准成功                         │ ║
 * ║   │   ADJUST_FAIL1 (0xA2)            - 校准失败1                        │ ║
 * ║   │   ADJUST_FAIL2 (0xA3)            - 校准失败2                        │ ║
 * ║   │   ADJUST_ING (0xA0)              - 校准进行中                       │ ║
 * ║   │                                                                      │ ║
 * ║   │   【零点状态】                                                        │ ║
 * ║   │   ZERO_SUCCESS (0xA1)            - 零点校准成功                     │ ║
 * ║   │   ZERO_FAIL1 (0xA2)              - 零点校准失败1                    │ ║
 * ║   │   ZERO_FAIL2 (0xA3)              - 零点校准失败2                    │ ║
 * ║   │   ZERO_ING (0xA0)                - 零点校准进行中                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【使用流程】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 探头事件发生 │───▶│ 创建通知对象 │───▶│ 发送事件通知 │                   ║
 * ║   │ (报警/校准)  │    │ProbeNotifyInfo│   │EventFactory │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. BaseProbe发送探头报警事件                                               ║
 * ║   2. BaseProbe发送探头校准事件                                               ║
 * ║   3. BaseProbe发送探头零点事件                                               ║
 * ║   4. EventFactory传递通知信息                                                ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 成员变量使用volatile保证多线程可见性                                      ║
 * ║   - 不可变对象，创建后不应修改                                                ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - BaseProbe: 创建ProbeNotifyInfo实例发送事件                              ║
 * ║   - EventFactory: 使用ProbeNotifyInfo作为事件数据                           ║
 * ║   - EventBase: 封装ProbeNotifyInfo作为事件参数                              ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class ProbeNotifyInfo {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 报警类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 量程超限报警
     * 当输入信号超出探头量程范围时触发
     */
    public static final int ALARM_RANGE_OUT = 0x01;

    /**
     * 电池电量低报警
     * 当探头电池电量低于阈值时触发
     */
    public static final int ALARM_BATTERY_LOW = 0x02;

    /**
     * 通信异常报警
     * 当探头与示波器通信异常时触发
     */
    public static final int ALARM_COMM_ABNORMAL = 0x03;

    /**
     * 50Ω开关报警
     * 当50Ω阻抗开关状态异常时触发
     */
    public static final int ALARM_50_SWITCH = 0x04;

    /**
     * 转换器高温报警
     * 当探头转换器温度过高时触发
     */
    public static final int ALARM_CONV_HIGH_TEMP = 0x05;

    /**
     * 补偿器高温报警
     * 当探头补偿器温度过高时触发
     */
    public static final int ALARM_COMP_HIGH_TEMP = 0x06;

    /**
     * AC/DC异常报警
     * 当探头AC/DC耦合模式异常时触发
     */
    public static final int ALARM_ACDC_ABNORMAL = 0x07;

    /**
     * 更换电池报警
     * 当探头电池需要更换时触发
     */
    public static final int ALARM_REPLACE_BATTERY = 0x0A;

    /**
     * 不匹配报警
     * 当探头与示波器不匹配时触发
     */
    public static final int ALARM_MISMATCH = 0x0B;

    /**
     * 衰减器错误报警
     * 当探头衰减器出现错误时触发
     */
    public static final int ALARM_ATTENUATOR_ERROR = 0x0C;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 校准状态常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 方波关闭状态
     * 探头方波输出已关闭
     */
    public static final int ADJUST_SQUAREWAVE_CLOSE = 0x02;

    /**
     * 方波打开状态
     * 探头方波输出已打开，用于校准
     */
    public static final int ADJUST_SQUAREWAVE_OPEN = 0x01;

    /**
     * 校准请求
     * 请求进行探头校准
     */
    public static final int ADJUST_REQ = 0x00;

    /**
     * 校准成功
     * 探头校准成功完成
     */
    public static final int ADJUST_SUCCESS = 0xA1;

    /**
     * 校准失败1
     * 探头校准失败（原因1）
     */
    public static final int ADJUST_FAIL1 = 0xA2;

    /**
     * 校准失败2
     * 探头校准失败（原因2）
     */
    public static final int ADJUST_FAIL2 = 0xA3;

    /**
     * 校准进行中
     * 探头校准正在进行中
     */
    public static final int ADJUST_ING = 0xA0;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 零点状态常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 零点校准成功
     * 探头零点校准成功完成
     */
    public static final int ZERO_SUCCESS = 0xA1;

    /**
     * 零点校准失败1
     * 探头零点校准失败（原因1）
     */
    public static final int ZERO_FAIL1 = 0xA2;

    /**
     * 零点校准失败2
     * 探头零点校准失败（原因2）
     */
    public static final int ZERO_FAIL2 = 0xA3;

    /**
     * 零点校准进行中
     * 探头零点校准正在进行中
     */
    public static final int ZERO_ING = 0xA0;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 通道索引
     * 标识事件所属的通道（0-3）
     * 使用volatile保证多线程可见性
     */
    public volatile int chIdx = 0;

    /**
     * 事件ID
     * 标识具体的事件类型（报警类型、校准状态、零点状态等）
     * 使用volatile保证多线程可见性
     */
    public volatile int Id = 0;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造ProbeNotifyInfo实例
     * 创建通知信息对象，包含通道索引和事件ID
     *
     * @param chIdx 通道索引（0-3）
     * @param Id 事件ID（报警类型、校准状态、零点状态等）
     */
    public ProbeNotifyInfo(int chIdx, int Id){
        this.chIdx = chIdx;                                                         // 设置通道索引
        this.Id = Id;                                                               // 设置事件ID
    }
}
