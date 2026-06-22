package com.micsig.tbook.ui.top.view.scale; // 时间刻度工具类所属包，位于UI层顶部视图模块

import com.micsig.tbook.ui.util.StrUtil; // 字符串工具类，用于空值判断
import java.text.DecimalFormat; // 十进制格式化类，用于数值格式化
import java.text.DecimalFormatSymbols; // 十进制格式化符号类，用于设置区域符号
import java.util.Locale; // 区域设置类，用于指定中国区域

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                              TopUtilScale.java                              │
 * │                           时间刻度工具类（工程级注释）                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   所属层级：UI层 → 顶部视图 → 刻度模块                                         │
 * │   模块类型：工具类（Utility Class）                                           │
 * │   业务场景：示波器时间参数设置、时间刻度调节                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 提供时间单位转换功能（ns、μs、ms、s 之间的相互转换）                        │
 * │   2. 提供时间值与纳秒基准值的相互转换                                          │
 * │   3. 定义时间调节动作常量（大/小步进、左/右调节）                                │
 * │   4. 定义不同触发场景的时间范围限制                                            │
 * │   5. 提供时间范围检查与边界修正功能                                            │
 * │   6. 封装ScaleValue内部类用于时间值与单位的组合表示                             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────────┐   │
 * │   │                         TopUtilScale                                │   │
 * │   │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │   │
 * │   │  │  单位转换常量    │  │  调节动作常量    │  │  时间范围常量    │    │   │
 * │   │  │  TIME_S2NS      │  │  ACTION_SCALE_* │  │  TIME_*_MIN/MAX │    │   │
 * │   │  └─────────────────┘  └─────────────────┘  └─────────────────┘    │   │
 * │   │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │   │
 * │   │  │ getValueFromNS  │  │ getNSFromValue  │  │   checkTime     │    │   │
 * │   │  │  (ns→值+单位)   │  │  (值+单位→ns)   │  │  (范围检查)     │    │   │
 * │   │  └─────────────────┘  └─────────────────┘  └─────────────────┘    │   │
 * │   │  ┌─────────────────────────────────────────────────────────────┐  │   │
 * │   │  │                    ScaleValue (内部类)                       │  │   │
 * │   │  │         value(数值) + itemUnit(单位) + itemValue(步进值)      │  │   │
 * │   │  └─────────────────────────────────────────────────────────────┘  │   │
 * │   └─────────────────────────────────────────────────────────────────────┘   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   用户输入时间值 → getNSFromValue() → 纳秒基准值 → 业务处理                     │
 * │   业务处理结果 → getValueFromNS() → 带单位的时间值 → UI显示                     │
 * │   用户调节操作 → checkTime() → 边界修正后的时间值                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   外部依赖：StrUtil（字符串工具）、DecimalFormat（数值格式化）                   │
 * │   被依赖：TopScaleView、TopTimeSettingPanel等时间相关UI组件                    │
 * │   相似类：TopUtilBandWidthHz（频率单位转换工具类）                             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   // 示例1：纳秒转换为带单位的值                                              │
 * │   TopUtilScale topUtilScale = new TopUtilScale();                          │
 * │   ScaleValue scaleValue = topUtilScale.createScaleValue();                 │
 * │   TopUtilScale.getValueFromNS(1000000, scaleValue); // 1ms                 │
 * │                                                                             │
 * │   // 示例2：带单位的字符串转换为纳秒                                          │
 * │   long ns = TopUtilScale.getNSFromValue("1.5ms"); // 1500000ns             │
 * │                                                                             │
 * │   // 示例3：时间范围检查                                                      │
 * │   long checked = TopUtilScale.checkTime(5, 8, 100); // 返回8（最小值）       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【版本历史】                                                                 │
 * │   v1.0.0 - 2017/04/14 - yangj - 初始创建，实现基础时间刻度转换功能            │
 * │   v1.1.0 - 后续迭代 - 增加多种触发场景的时间范围常量                           │
 * └─────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author yangj
 * @version 1.1.0
 * @since 2017/04/14
 */
public class TopUtilScale {

    // ==================== 调节动作常量定义 ====================
    
    /**
     * 大时间向左调节动作标识
     * <p>
     * 功能说明：表示用户执行大步进向左（减小）调节时间的操作
     * 使用场景：旋钮大步进调节、快捷键调节
     * 数值含义：负数表示向左（减小），绝对值1表示大步进
     * </p>
     */
    public static final int ACTION_SCALE_LARGE_LEFT = -1; // 大步进向左调节，值为-1
    
    /**
     * 大时间向右调节动作标识
     * <p>
     * 功能说明：表示用户执行大步进向右（增大）调节时间的操作
     * 使用场景：旋钮大步进调节、快捷键调节
     * 数值含义：正数表示向右（增大），绝对值1表示大步进
     * </p>
     */
    public static final int ACTION_SCALE_LARGE_RIGHT = 1; // 大步进向右调节，值为1
    
    /**
     * 小时间向左调节动作标识
     * <p>
     * 功能说明：表示用户执行小步进向左（减小）调节时间的操作
     * 使用场景：旋钮小步进调节、精细调节
     * 数值含义：负数表示向左（减小），绝对值2表示小步进
     * </p>
     */
    public static final int ACTION_SCALE_SMALL_LEFT = -2; // 小步进向左调节，值为-2
    
    /**
     * 小时间向右调节动作标识
     * <p>
     * 功能说明：表示用户执行小步进向右（增大）调节时间的操作
     * 使用场景：旋钮小步进调节、精细调节
     * 数值含义：正数表示向右（增大），绝对值2表示小步进
     * </p>
     */
    public static final int ACTION_SCALE_SMALL_RIGHT = 2; // 小步进向右调节，值为2
    
    /**
     * 保存且关闭动作标识
     * <p>
     * 功能说明：表示用户确认当前时间设置并关闭设置面板的操作
     * 使用场景：时间参数设置完成、确认保存
     * 数值含义：0表示中性操作，既不增大也不减小
     * </p>
     */
    public static final int ACTION_SCALE_FINISH = 0; // 保存并关闭操作，值为0

    // ==================== 时间单位换算常量定义 ====================
    
    /**
     * 秒到纳秒的换算因子
     * <p>
     * 换算公式：1秒 = 1,000,000,000纳秒 = 10^9 纳秒
     * 使用场景：秒级时间值转换为纳秒基准值
     * </p>
     */
    public static final long TIME_S2NS = 1000 * 1000 * 1000; // 秒转纳秒：1s = 10^9 ns
    
    /**
     * 毫秒到纳秒的换算因子
     * <p>
     * 换算公式：1毫秒 = 1,000,000纳秒 = 10^6 纳秒
     * 使用场景：毫秒级时间值转换为纳秒基准值
     * </p>
     */
    public static final long TIME_MS2NS = 1000 * 1000; // 毫秒转纳秒：1ms = 10^6 ns
    
    /**
     * 微秒到纳秒的换算因子
     * <p>
     * 换算公式：1微秒 = 1,000纳秒 = 10^3 纳秒
     * 使用场景：微秒级时间值转换为纳秒基准值
     * </p>
     */
    public static final long TIME_US2NS = 1000; // 微秒转纳秒：1μs = 10^3 ns

    // ==================== 时间范围限制常量定义 ====================
    
    /**
     * 时间最小间隔步进值
     * <p>
     * 功能说明：定义时间调节的最小粒度，确保时间值为8的整数倍
     * 设计原因：示波器硬件限制，时间分辨率最小为8纳秒
     * 使用场景：时间值对齐、边界修正
     * </p>
     */
    public static final int TIME_MIN_INTERVAL = 8; // 最小时间间隔：8纳秒
    
    /**
     * 默认最小时间值（单位：纳秒）
     * <p>
     * 功能说明：定义系统支持的最小时间参数值
     * 数值说明：8纳秒，与TIME_MIN_INTERVAL一致
     * 使用场景：时间参数下限检查
     * </p>
     */
    public static final long DEFAULT_MIN_TIME = 8; // 默认最小时间：8纳秒
    
    /**
     * 默认最大时间值（单位：纳秒）
     * <p>
     * 功能说明：定义系统支持的最大时间参数值
     * 数值说明：10秒 = 10,000,000,000纳秒
     * 使用场景：时间参数上限检查
     * </p>
     */
    public static final long DEFAULT_MAX_TIME = 10 * TIME_S2NS; // 默认最大时间：10秒

    // ==================== 各触发场景的时间范围常量定义 ====================
    
    /**
     * 通用触发模式的最小时间值（单位：纳秒）
     * <p>
     * 功能说明：适用于常规触发场景的最小时间限制
     * 数值说明：200纳秒，比默认最小时间宽松
     * 使用场景：通用触发时间参数设置
     * </p>
     */
    public static final long TIME_COMMON_MIN = 200; // 通用模式最小时间：200纳秒
    
    /**
     * 通用触发模式的最大时间值（单位：纳秒）
     * <p>
     * 功能说明：适用于常规触发场景的最大时间限制
     * 数值说明：与DEFAULT_MAX_TIME一致，10秒
     * 使用场景：通用触发时间参数设置
     * </p>
     */
    public static final long TIME_COMMON_MAX = DEFAULT_MAX_TIME; // 通用模式最大时间：10秒
    
    /**
     * 脉宽触发模式的最小时间值（单位：纳秒）
     * <p>
     * 功能说明：脉宽触发时的时间下限
     * 数值说明：8纳秒，与默认最小时间一致
     * 使用场景：脉宽触发时间参数设置
     * </p>
     */
    public static final long TIME_PULSEWIDTH_MIN = DEFAULT_MIN_TIME; // 脉宽模式最小时间：8纳秒
    
    /**
     * 脉宽触发模式的最大时间值（单位：纳秒）
     * <p>
     * 功能说明：脉宽触发时的时间上限
     * 数值说明：10秒
     * 使用场景：脉宽触发时间参数设置
     * </p>
     */
    public static final long TIME_PULSEWIDTH_MAX = DEFAULT_MAX_TIME; // 脉宽模式最大时间：10秒
    
    /**
     * 逻辑触发模式的最小时间值（单位：纳秒）
     * <p>
     * 功能说明：逻辑触发时的时间下限
     * 数值说明：8纳秒
     * 使用场景：逻辑触发时间参数设置
     * </p>
     */
    public static final long TIME_LOGIC_MIN = DEFAULT_MIN_TIME; // 逻辑模式最小时间：8纳秒
    
    /**
     * 逻辑触发模式的最大时间值（单位：纳秒）
     * <p>
     * 功能说明：逻辑触发时的时间上限
     * 数值说明：10秒
     * 使用场景：逻辑触发时间参数设置
     * </p>
     */
    public static final long TIME_LOGIC_MAX = DEFAULT_MAX_TIME; // 逻辑模式最大时间：10秒
    
    /**
     * N边沿触发模式的最小时间值（单位：纳秒）
     * <p>
     * 功能说明：N边沿触发时的时间下限
     * 数值说明：8纳秒
     * 使用场景：N边沿触发时间参数设置
     * </p>
     */
    public static final long TIME_NEDGE_MIN = DEFAULT_MIN_TIME; // N边沿模式最小时间：8纳秒
    
    /**
     * N边沿触发模式的最大时间值（单位：纳秒）
     * <p>
     * 功能说明：N边沿触发时的时间上限
     * 数值说明：10秒
     * 使用场景：N边沿触发时间参数设置
     * </p>
     */
    public static final long TIME_NEDGE_MAX = DEFAULT_MAX_TIME; // N边沿模式最大时间：10秒
    
    /**
     * Runt触发模式的最小时间值（单位：纳秒）
     * <p>
     * 功能说明：Runt（矮脉冲）触发时的时间下限
     * 数值说明：8纳秒
     * 使用场景：Runt触发时间参数设置
     * </p>
     */
    public static final long TIME_RUNT_MIN = DEFAULT_MIN_TIME; // Runt模式最小时间：8纳秒
    
    /**
     * Runt触发模式的最大时间值（单位：纳秒）
     * <p>
     * 功能说明：Runt（矮脉冲）触发时的时间上限
     * 数值说明：10秒
     * 使用场景：Runt触发时间参数设置
     * </p>
     */
    public static final long TIME_RUNT_MAX = DEFAULT_MAX_TIME; // Runt模式最大时间：10秒
    
    /**
     * 斜率触发模式的最小时间值（单位：纳秒）
     * <p>
     * 功能说明：斜率触发时的时间下限
     * 数值说明：8纳秒
     * 使用场景：斜率触发时间参数设置
     * </p>
     */
    public static final long TIME_SLOPE_MIN = DEFAULT_MIN_TIME; // 斜率模式最小时间：8纳秒
    
    /**
     * 斜率触发模式的最大时间值（单位：纳秒）
     * <p>
     * 功能说明：斜率触发时的时间上限
     * 数值说明：10秒
     * 使用场景：斜率触发时间参数设置
     * </p>
     */
    public static final long TIME_SLOPE_MAX = DEFAULT_MAX_TIME; // 斜率模式最大时间：10秒
    
    /**
     * 超时触发模式的最小时间值（单位：纳秒）
     * <p>
     * 功能说明：超时触发时的时间下限
     * 数值说明：8纳秒
     * 使用场景：超时触发时间参数设置
     * </p>
     */
    public static final long TIME_TIMEOUT_MIN = DEFAULT_MIN_TIME; // 超时模式最小时间：8纳秒
    
    /**
     * 超时触发模式的最大时间值（单位：纳秒）
     * <p>
     * 功能说明：超时触发时的时间上限
     * 数值说明：10秒
     * 使用场景：超时触发时间参数设置
     * </p>
     */
    public static final long TIME_TIMEOUT_MAX = DEFAULT_MAX_TIME; // 超时模式最大时间：10秒

    // ==================== 时间单位字符串常量定义 ====================
    
    /**
     * 秒单位字符串标识
     * <p>
     * 功能说明：用于UI显示和字符串解析时的秒单位标识
     * 使用场景：时间值格式化输出、字符串解析
     * </p>
     */
    public static final String UNIT_S = "s"; // 秒单位标识
    
    /**
     * 毫秒单位字符串标识
     * <p>
     * 功能说明：用于UI显示和字符串解析时的毫秒单位标识
     * 使用场景：时间值格式化输出、字符串解析
     * </p>
     */
    public static final String UNIT_MS = "ms"; // 毫秒单位标识
    
    /**
     * 微秒单位字符串标识
     * <p>
     * 功能说明：用于UI显示和字符串解析时的微秒单位标识
     * 注意事项：使用希腊字母μ（mu）表示微
     * 使用场景：时间值格式化输出、字符串解析
     * </p>
     */
    public static final String UNIT_US = "μs"; // 微秒单位标识（希腊字母μ）
    
    /**
     * 纳秒单位字符串标识
     * <p>
     * 功能说明：用于UI显示和字符串解析时的纳秒单位标识
     * 使用场景：时间值格式化输出、字符串解析
     * </p>
     */
    public static final String UNIT_NS = "ns"; // 纳秒单位标识

    // ==================== 大步进调节预设值数组定义 ====================
    
    /**
     * 大步进调节的预设数值数组
     * <p>
     * 功能说明：定义大步进调节时的数值步进序列
     * 数组结构：{100ns, 1μs, 10μs, 100μs, 1ms, 10ms, 100ms, 1s}
     * 使用场景：旋钮大步进调节、快速调节时间参数
     * 设计原则：每档相差约10倍，覆盖从纳秒到秒的常用范围
     * </p>
     */
    public static final double[] LARGE_ITEM_VALUES = {100, 1, 10, 100, 1, 10, 100, 1}; // 大步进数值序列
    
    /**
     * 大步进调节的预设单位数组
     * <p>
     * 功能说明：定义大步进调节时的单位序列，与LARGE_ITEM_VALUES一一对应
     * 数组结构：{ns, μs, μs, μs, ms, ms, ms, s}
     * 使用场景：旋钮大步进调节、快速调节时间参数
     * 对应关系：索引i的单位对应LARGE_ITEM_VALUES[i]的数值
     * </p>
     */
    public static final String[] LARGE_ITEM_UNITS = {UNIT_NS, UNIT_US, UNIT_US, UNIT_US, UNIT_MS, UNIT_MS, UNIT_MS, UNIT_S}; // 大步进单位序列
    
    /**
     * 大步进调节的纳秒基准值数组
     * <p>
     * 功能说明：预计算的大步进调节纳秒值，避免运行时重复计算
     * 数组结构：{100ns, 100μs, 1ms, 10ms, 100ms, 1s, 10s, 100s}
     * 使用场景：快速获取大步进调节的纳秒基准值
     * 计算方式：LARGE_ITEM_VALUES[i] * 对应单位换算因子
     * </p>
     */
    public static final long[] LARGE_ITEM_NSS = {100, 100 * TIME_US2NS, 100 * 10 * TIME_US2NS, 100 * 100 * TIME_US2NS, 100 * TIME_MS2NS, 100 * 10 * TIME_MS2NS, 100 * 100 * TIME_MS2NS, 100 * TIME_S2NS}; // 大步进纳秒值序列

    // ==================== 核心转换方法 ====================
    
    /**
     * 将纳秒基准值转换为带单位的时间值
     * <p>
     * 功能说明：根据纳秒值的大小自动选择合适的单位（ns、μs、ms、s），
     *          并将转换后的数值和单位信息封装到ScaleValue对象中
     * </p>
     * 
     * <p><b>转换规则：</b></p>
     * <ul>
     *   <li>≥1秒：转换为秒单位，保留两位小数</li>
     *   <li>≥1毫秒：转换为毫秒单位，根据数值大小选择步进值（1/10/100）</li>
     *   <li>≥1微秒：转换为微秒单位，根据数值大小选择步进值（1/10/100）</li>
     *   <li><1微秒：保持纳秒单位，步进值固定为100</li>
     * </ul>
     * 
     * @param ns         纳秒基准值，必须为非负数
     * @param scaleValue 输出参数，用于存储转换结果；如果为null则直接返回
     * 
     * @see ScaleValue 时间值封装类
     * @see #getNSFromValue(String) 反向转换方法
     * 
     * @example
     * <pre>
     * TopUtilScale topUtilScale = new TopUtilScale();
     * ScaleValue scaleValue = topUtilScale.createScaleValue();
     * 
     * TopUtilScale.getValueFromNS(1000, scaleValue);      // 1.00μs
     * TopUtilScale.getValueFromNS(1000000, scaleValue);    // 1.00ms
     * TopUtilScale.getValueFromNS(1000000000, scaleValue); // 1.00s
     * </pre>
     */
    public static void getValueFromNS(long ns, ScaleValue scaleValue) { // 静态方法：纳秒转带单位值
        if (scaleValue == null) return; // 空值检查，避免空指针异常
        
        DecimalFormat df = new DecimalFormat("###0.00", new DecimalFormatSymbols(Locale.CHINA)); // 创建格式化器，保留两位小数，使用中国区域设置
        double value; // 声明转换后的数值变量
        
        if (ns / TIME_S2NS >= 1) { // 判断是否达到秒级别（≥1秒）
            value = Double.parseDouble(df.format(ns * 1.0 / TIME_S2NS)); // 转换为秒，格式化为两位小数
            scaleValue.setValue(value, UNIT_S, 1); // 设置秒单位，步进值为1
        } else if (ns / TIME_MS2NS >= 1) { // 判断是否达到毫秒级别（≥1毫秒）
            value = Double.parseDouble(df.format(ns * 1.0 / TIME_MS2NS)); // 转换为毫秒，格式化为两位小数
            if (value / 100 >= 1) { // 判断数值是否≥100毫秒
                scaleValue.setValue(value, UNIT_MS, 100); // 设置毫秒单位，步进值为100
            } else if (value / 10 >= 1) { // 判断数值是否≥10毫秒
                scaleValue.setValue(value, UNIT_MS, 10); // 设置毫秒单位，步进值为10
            } else { // 数值<10毫秒
                scaleValue.setValue(value, UNIT_MS, 1); // 设置毫秒单位，步进值为1
            }
        } else if (ns / TIME_US2NS >= 1) { // 判断是否达到微秒级别（≥1微秒）
            value = Double.parseDouble(df.format(ns * 1.0 / TIME_US2NS)); // 转换为微秒，格式化为两位小数
            if (value / 100 >= 1) { // 判断数值是否≥100微秒
                scaleValue.setValue(value, UNIT_US, 100); // 设置微秒单位，步进值为100
            } else if (value / 10 >= 1) { // 判断数值是否≥10微秒
                scaleValue.setValue(value, UNIT_US, 10); // 设置微秒单位，步进值为10
            } else { // 数值<10微秒
                scaleValue.setValue(value, UNIT_US, 1); // 设置微秒单位，步进值为1
            }
        } else { // 纳秒级别（<1微秒）
            String d = df.format(ns * 1.0); // 格式化纳秒值，保留两位小数
            value = Double.parseDouble(d); // 解析为double类型
            scaleValue.setValue(value, UNIT_NS, 100); // 设置纳秒单位，步进值固定为100
            // 注释掉的代码：原本根据数值大小选择步进值，现统一使用100
//            if (value / 100 >= 1) { // 判断数值是否≥100纳秒
//                scaleValue.setValue(value, UNIT_NS, 100); // 设置纳秒单位，步进值为100
//            } else if (value / 10 >= 1) { // 判断数值是否≥10纳秒
//                scaleValue.setValue(value, UNIT_NS, 10); // 设置纳秒单位，步进值为10
//            } else { // 数值<10纳秒
//                scaleValue.setValue(value, UNIT_NS, 1); // 设置纳秒单位，步进值为1
//            }
        }
    }

    /**
     * 将带单位的时间字符串转换为纳秒基准值
     * <p>
     * 功能说明：解析形如"1.5ms"、"100ns"、"2μs"、"0.5s"的时间字符串，
     *          根据单位标识转换为纳秒基准值
     * </p>
     * 
     * <p><b>解析规则：</b></p>
     * <ul>
     *   <li>字符串以"ns"结尾：直接取数值（单位已是纳秒）</li>
     *   <li>字符串以"μs"结尾：数值乘以1000转换为纳秒</li>
     *   <li>字符串以"ms"结尾：数值乘以1,000,000转换为纳秒</li>
     *   <li>字符串以"s"结尾：数值乘以1,000,000,000转换为纳秒</li>
     * </ul>
     * 
     * @param value 带单位的时间字符串，如"1.5ms"、"100ns"；如果为空或解析失败返回0
     * @return 转换后的纳秒基准值；解析失败时返回0
     * 
     * @see #getValueFromNS(long, ScaleValue) 反向转换方法
     * @see StrUtil#isEmpty(String) 空值判断工具
     * 
     * @example
     * <pre>
     * long ns1 = TopUtilScale.getNSFromValue("100ns");    // 100
     * long ns2 = TopUtilScale.getNSFromValue("1.5ms");    // 1500000
     * long ns3 = TopUtilScale.getNSFromValue("2μs");      // 2000
     * long ns4 = TopUtilScale.getNSFromValue("0.5s");     // 500000000
     * long ns5 = TopUtilScale.getNSFromValue("");         // 0（空字符串）
     * long ns6 = TopUtilScale.getNSFromValue("invalid");  // 0（解析失败）
     * </pre>
     */
    public static long getNSFromValue(String value) { // 静态方法：带单位字符串转纳秒
        if (StrUtil.isEmpty(value)) return 0; // 空值检查，空字符串返回0
        
        try { // 异常捕获块，解析失败时返回0
            value = value.toLowerCase(); // 转换为小写，统一处理大小写差异
            
            if (value.endsWith(UNIT_NS)) { // 判断是否以"ns"结尾
                return (long) (Double.valueOf(value.replace(UNIT_NS, "")) * 1); // 移除单位后解析数值，乘以1（已是纳秒）
            } else if (value.endsWith(UNIT_US)) { // 判断是否以"μs"结尾
                return (long) (Double.valueOf(value.replace(UNIT_US, "")) * TIME_US2NS); // 移除单位后解析数值，乘以1000转纳秒
            } else if (value.endsWith(UNIT_MS)) { // 判断是否以"ms"结尾
                return (long) (Double.valueOf(value.replace(UNIT_MS, "")) * TIME_MS2NS); // 移除单位后解析数值，乘以1000000转纳秒
            } else { // 默认按秒处理
                return (long) (Double.valueOf(value.replace(UNIT_S, "")) * TIME_S2NS); // 移除单位后解析数值，乘以1000000000转纳秒
            }
        } catch (Exception e) { // 捕获所有异常（NumberFormatException等）
            return 0; // 解析失败时返回0
        }
    }

    /**
     * 创建ScaleValue实例的工厂方法
     * <p>
     * 功能说明：创建一个新的ScaleValue对象，用于存储时间值和单位信息
     * 设计模式：简单工厂模式，便于外部获取ScaleValue实例
     * </p>
     * 
     * @return 新创建的ScaleValue实例，初始值为默认值（value=0, itemUnit=null, itemValue=0）
     * 
     * @see ScaleValue 时间值封装类
     * @see #getValueFromNS(long, ScaleValue) 配合使用方法
     * 
     * @example
     * <pre>
     * TopUtilScale topUtilScale = new TopUtilScale();
     * ScaleValue scaleValue = topUtilScale.createScaleValue();
     * TopUtilScale.getValueFromNS(1000000, scaleValue);
     * System.out.println(scaleValue.value + " " + scaleValue.itemUnit); // 1.0 ms
     * </pre>
     */
    public ScaleValue createScaleValue() { // 工厂方法：创建ScaleValue实例
        return new ScaleValue(); // 返回新创建的ScaleValue对象
    }

    // ==================== 内部类定义 ====================
    
    /**
     * 时间值封装类（内部类）
     * <p>
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                          ScaleValue                                  │
     * │                      时间值与单位的封装结构                            │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ 【类定义】非静态内部类，依赖外部类TopUtilScale                           │
     * │ 【职责】封装时间数值、单位字符串和步进值三个属性                          │
     * │ 【使用场景】时间参数设置、UI显示数据传递                                │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ 【属性说明】                                                          │
     * │   value     : double - 转换后的时间数值（如1.5）                       │
     * │   itemUnit  : String - 时间单位字符串（如"ms"）                        │
     * │   itemValue : double - 步进值/调节档位（如1/10/100）                   │
     * └─────────────────────────────────────────────────────────────────────┘
     * 
     * @author yangj
     * @since 2017/04/14
     */
    public class ScaleValue { // 非静态内部类：时间值封装
        
        /**
         * 转换后的时间数值
         * <p>
         * 功能说明：存储经过单位转换后的时间数值
         * 示例：1.5ms中的1.5，100ns中的100
         * 取值范围：正数，通常保留两位小数
         * </p>
         */
        public double value; // 时间数值（如1.5）
        
        /**
         * 时间单位字符串
         * <p>
         * 功能说明：存储时间单位标识
         * 可选值：UNIT_NS("ns")、UNIT_US("μs")、UNIT_MS("ms")、UNIT_S("s")
         * 示例：1.5ms中的"ms"
         * </p>
         */
        public String itemUnit; // 时间单位（如"ms"）
        
        /**
         * 步进值/调节档位
         * <p>
         * 功能说明：定义时间调节的步进大小，用于旋钮调节
         * 可选值：1、10、100，表示每步调节的数值增量
         * 示例：100ms档位时itemValue=100，每步调节增加/减少100ms
         * </p>
         */
        public double itemValue; // 步进值（如1/10/100）

        /**
         * 设置时间值封装对象的属性
         * <p>
         * 功能说明：一次性设置value、itemUnit、itemValue三个属性
         * 使用场景：getValueFromNS方法内部调用，封装转换结果
         * </p>
         * 
         * @param value     转换后的时间数值
         * @param itemUnit  时间单位字符串（ns/μs/ms/s）
         * @param itemValue 步进值/调节档位（1/10/100）
         * 
         * @example
         * <pre>
         * ScaleValue scaleValue = topUtilScale.new ScaleValue();
         * scaleValue.setValue(1.5, "ms", 10); // 设置为1.5ms，步进值10
         * </pre>
         */
        public void setValue(double value, String itemUnit, double itemValue) { // 设置方法：批量设置三个属性
            this.value = value; // 设置时间数值
            this.itemUnit = itemUnit; // 设置时间单位
            this.itemValue = itemValue; // 设置步进值
        }
    }

    // ==================== 辅助工具方法 ====================
    
    /**
     * 检查并修正时间值到有效范围
     * <p>
     * 功能说明：对输入的时间值进行边界检查和修正，确保：
     *          1. 不小于最小值（小于最小值则设为最小值）
     *          2. 不大于最大值（大于最大值则设为最大值）
     *          3. 对齐到最小时间间隔（8纳秒的整数倍）
     * </p>
     * 
     * <p><b>处理流程：</b></p>
     * <pre>
     * 输入时间值 → 下限检查 → 上限检查 → 间隔对齐 → 输出修正后的时间值
     * </pre>
     * 
     * @param curNs 当前时间值（单位：纳秒）
     * @param minNs 允许的最小时间值（单位：纳秒）
     * @param maxNs 允许的最大时间值（单位：纳秒）
     * @return 修正后的时间值（单位：纳秒），保证在[minNs, maxNs]范围内且为8的整数倍
     * 
     * @see #TIME_MIN_INTERVAL 最小时间间隔常量
     * @see #DEFAULT_MIN_TIME 默认最小时间
     * @see #DEFAULT_MAX_TIME 默认最大时间
     * 
     * @example
     * <pre>
     * // 示例1：小于最小值
     * long result1 = TopUtilScale.checkTime(5, 8, 100);    // 返回8（修正为最小值）
     * 
     * // 示例2：大于最大值
     * long result2 = TopUtilScale.checkTime(200, 8, 100);  // 返回100（修正为最大值）
     * 
     * // 示例3：正常范围但未对齐
     * long result3 = TopUtilScale.checkTime(15, 8, 100);   // 返回8（对齐到8的倍数）
     * 
     * // 示例4：正常范围且已对齐
     * long result4 = TopUtilScale.checkTime(24, 8, 100);   // 返回24（无需修正）
     * </pre>
     */
    public static long checkTime(long curNs, long minNs, long maxNs) { // 静态方法：时间范围检查与修正
        curNs = curNs < minNs ? minNs : curNs; // 下限检查：小于最小值则修正为最小值
        curNs = curNs > maxNs ? maxNs : curNs; // 上限检查：大于最大值则修正为最大值
        curNs = curNs - curNs % TIME_MIN_INTERVAL; // 间隔对齐：减去余数，确保为8的整数倍
        return curNs; // 返回修正后的时间值
    }
}
