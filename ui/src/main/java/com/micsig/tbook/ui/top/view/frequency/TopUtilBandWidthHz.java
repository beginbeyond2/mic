package com.micsig.tbook.ui.top.view.frequency; // 包声明：示波器顶部视图频率相关工具类 //

import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类 //
import com.micsig.tbook.ui.util.TBookUtil; // 导入示波器通用工具类 //

import java.text.DecimalFormat; // 导入十进制格式化类 //
import java.text.DecimalFormatSymbols; // 导入十进制格式化符号类 //
import java.util.Locale; // 导入地区设置类 //

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                     TopUtilBandWidthHz - 带宽/频率/时间单位转换工具类            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                   │
 * │   所属模块: ui.top.view.frequency                                            │
 * │   模块职责: 示波器顶部视图频率相关的时间参数转换与处理                                │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                   │
 * │   1. 时间单位转换：支持纳秒(ns)、微秒(μs)、毫秒(ms)、秒(s)之间的相互转换           │
 * │   2. 时间值与纳秒的双向转换：将带单位的时间字符串转换为纳秒值，反之亦然              │
 * │   3. 时间范围校验：确保时间值在有效范围内，并进行对齐处理                           │
 * │   4. 频率与时间转换：根据时间计算对应频率，或根据频率计算对应时间                    │
 * │   5. ScaleValue封装：提供时间值和单位的封装对象，便于UI显示                       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                   │
 * │   - 工具类模式：所有核心方法为静态方法，无需实例化即可使用                          │
 * │   - 内部类设计：ScaleValue作为内部类封装时间值和单位信息                          │
 * │   - 常量集中管理：所有时间转换因子、单位字符串、范围限制统一定义为静态常量            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                   │
 * │   用户输入(时间字符串) → 解析转换 → 纳秒值 → 范围校验 → UI显示封装                 │
 * │   用户输入(频率字符串) → 解析转换 → 纳秒值 → 时间格式化 → UI显示                  │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                   │
 * │   - StrUtil: 字符串判空工具                                                    │
 * │   - TBookUtil: 示波器通用时间/频率转换工具                                      │
 * │   - DecimalFormat: 数值格式化                                                  │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                   │
 * │   // 将纳秒值转换为带单位的时间值                                                │
 * │   ScaleValue scaleValue = new TopUtilBandWidthHz().createScaleValue();       │
 * │   TopUtilBandWidthHz.getValueFromNS(1000000, scaleValue);                    │
 * │   System.out.println(scaleValue.value + " " + scaleValue.itemUnit);          │
 * │                                                                              │
 * │   // 将时间字符串转换为纳秒值                                                    │
 * │   long ns = TopUtilBandWidthHz.getNSFromValue("1ms");                        │
 * │                                                                              │
 * │   // 校验时间范围                                                              │
 * │   long validNs = TopUtilBandWidthHz.checkTime(500, 100, 10000);             │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * @author yangj
 * @since 2017/4/14
 * @version 1.0.0
 */
public class TopUtilBandWidthHz {

    // ==================== 调节动作常量定义 ==================== //

    /**
     * 大时间向左调节动作标识
     * <p>用于时间参数调节时，表示大幅度减小时间值</p>
     * <p>值: -1</p>
     */
    public static final int ACTION_SCALE_LARGE_LEFT = -1; // 大时间向左调节动作标识 //

    /**
     * 大时间向右调节动作标识
     * <p>用于时间参数调节时，表示大幅度增加时间值</p>
     * <p>值: 1</p>
     */
    public static final int ACTION_SCALE_LARGE_RIGHT = 1; // 大时间向右调节动作标识 //

    /**
     * 小时间向左调节动作标识
     * <p>用于时间参数调节时，表示小幅度减小时间值</p>
     * <p>值: -2</p>
     */
    public static final int ACTION_SCALE_SMALL_LEFT = -2; // 小时间向左调节动作标识 //

    /**
     * 小时间向右调节动作标识
     * <p>用于时间参数调节时，表示小幅度增加时间值</p>
     * <p>值: 2</p>
     */
    public static final int ACTION_SCALE_SMALL_RIGHT = 2; // 小时间向右调节动作标识 //

    /**
     * 保存且关闭动作标识
     * <p>用于时间参数调节完成，保存设置并关闭调节界面</p>
     * <p>值: 0</p>
     */
    public static final int ACTION_SCALE_FINISH = 0; // 保存且关闭动作标识 //

    // ==================== 时间单位转换因子常量定义 ==================== //

    /**
     * 秒到纳秒的转换因子
     * <p>1秒 = 1,000,000,000纳秒</p>
     * <p>值: 1_000_000_000</p>
     */
    public static final long TIME_S2NS = 1000 * 1000 * 1000; // 秒到纳秒转换因子: 10^9 //

    /**
     * 毫秒到纳秒的转换因子
     * <p>1毫秒 = 1,000,000纳秒</p>
     * <p>值: 1_000_000</p>
     */
    public static final long TIME_MS2NS = 1000 * 1000; // 毫秒到纳秒转换因子: 10^6 //

    /**
     * 微秒到纳秒的转换因子
     * <p>1微秒 = 1,000纳秒</p>
     * <p>值: 1_000</p>
     */
    public static final long TIME_US2NS = 1000; // 微秒到纳秒转换因子: 10^3 //

    // ==================== 时间范围限制常量定义 ==================== //

    /**
     * 时间最小间隔（纳秒）
     * <p>用于时间值对齐，确保时间值为8的整数倍</p>
     * <p>值: 8ns</p>
     */
    public static final int TIME_MIN_INTERVAL = 8; // 时间最小间隔: 8纳秒 //

    /**
     * 默认最小时间值（纳秒）
     * <p>对应频率约30Hz，用于触发条件等场景的最小时间限制</p>
     * <p>值: 300ns</p>
     */
    public static final long DEFAULT_MIN_TIME = 300; // 默认最小时间: 300纳秒 //

    /**
     * 默认最大时间值（纳秒）
     * <p>对应频率约0.1Hz，用于触发条件等场景的最大时间限制</p>
     * <p>值: 10秒 = 10,000,000,000纳秒</p>
     */
    public static final long DEFAULT_MAX_TIME = 10 * TIME_S2NS; // 默认最大时间: 10秒 //

    // ==================== 各触发类型时间范围常量定义 ==================== //

    /**
     * 通用时间最小值（纳秒）
     * <p>值: 200ns</p>
     */
    public static final long TIME_COMMON_MIN = 200; // 通用时间最小值: 200纳秒 //

    /**
     * 通用时间最大值（纳秒）
     * <p>值: 10秒</p>
     */
    public static final long TIME_COMMON_MAX = DEFAULT_MAX_TIME; // 通用时间最大值: 10秒 //

    /**
     * 脉宽触发时间最小值（纳秒）
     * <p>值: 300ns</p>
     */
    public static final long TIME_PULSEWIDTH_MIN = DEFAULT_MIN_TIME; // 脉宽触发时间最小值: 300纳秒 //

    /**
     * 脉宽触发时间最大值（纳秒）
     * <p>值: 10秒</p>
     */
    public static final long TIME_PULSEWIDTH_MAX = DEFAULT_MAX_TIME; // 脉宽触发时间最大值: 10秒 //

    /**
     * 逻辑触发时间最小值（纳秒）
     * <p>值: 300ns</p>
     */
    public static final long TIME_LOGIC_MIN = DEFAULT_MIN_TIME; // 逻辑触发时间最小值: 300纳秒 //

    /**
     * 逻辑触发时间最大值（纳秒）
     * <p>值: 10秒</p>
     */
    public static final long TIME_LOGIC_MAX = DEFAULT_MAX_TIME; // 逻辑触发时间最大值: 10秒 //

    /**
     * 边沿触发时间最小值（纳秒）
     * <p>值: 300ns</p>
     */
    public static final long TIME_NEDGE_MIN = DEFAULT_MIN_TIME; // 边沿触发时间最小值: 300纳秒 //

    /**
     * 边沿触发时间最大值（纳秒）
     * <p>值: 10秒</p>
     */
    public static final long TIME_NEDGE_MAX = DEFAULT_MAX_TIME; // 边沿触发时间最大值: 10秒 //

    /**
     * Runt触发时间最小值（纳秒）
     * <p>值: 300ns</p>
     */
    public static final long TIME_RUNT_MIN = DEFAULT_MIN_TIME; // Runt触发时间最小值: 300纳秒 //

    /**
     * Runt触发时间最大值（纳秒）
     * <p>值: 10秒</p>
     */
    public static final long TIME_RUNT_MAX = DEFAULT_MAX_TIME; // Runt触发时间最大值: 10秒 //

    /**
     * 斜率触发时间最小值（纳秒）
     * <p>值: 300ns</p>
     */
    public static final long TIME_SLOPE_MIN = DEFAULT_MIN_TIME; // 斜率触发时间最小值: 300纳秒 //

    /**
     * 斜率触发时间最大值（纳秒）
     * <p>值: 10秒</p>
     */
    public static final long TIME_SLOPE_MAX = DEFAULT_MAX_TIME; // 斜率触发时间最大值: 10秒 //

    /**
     * 超时触发时间最小值（纳秒）
     * <p>值: 300ns</p>
     */
    public static final long TIME_TIMEOUT_MIN = DEFAULT_MIN_TIME; // 超时触发时间最小值: 300纳秒 //

    /**
     * 超时触发时间最大值（纳秒）
     * <p>值: 10秒</p>
     */
    public static final long TIME_TIMEOUT_MAX = DEFAULT_MAX_TIME; // 超时触发时间最大值: 10秒 //

    // ==================== 时间单位字符串常量定义 ==================== //

    /**
     * 秒单位字符串标识
     * <p>用于时间值显示和解析</p>
     */
    public static final String UNIT_S = "s"; // 秒单位字符串: "s" //

    /**
     * 毫秒单位字符串标识
     * <p>用于时间值显示和解析</p>
     */
    public static final String UNIT_MS = "ms"; // 毫秒单位字符串: "ms" //

    /**
     * 微秒单位字符串标识
     * <p>使用希腊字母μ表示微秒</p>
     */
    public static final String UNIT_US = "μs"; // 微秒单位字符串: "μs" //

    /**
     * 纳秒单位字符串标识
     * <p>用于时间值显示和解析</p>
     */
    public static final String UNIT_NS = "ns"; // 纳秒单位字符串: "ns" //

    // ==================== 大调节项常量定义 ==================== //

    /**
     * 大调节项的数值数组
     * <p>用于时间参数大幅度调节时的预设值</p>
     * <p>数组元素对应不同单位下的基准值</p>
     */
    public static final double[] LARGE_ITEM_VALUES = {100, 1, 10, 100, 1, 10, 100, 1}; // 大调节项数值数组 //

    /**
     * 大调节项的单位数组
     * <p>与LARGE_ITEM_VALUES一一对应，表示每个数值对应的单位</p>
     */
    public static final String[] LARGE_ITEM_UNITS = {UNIT_NS, UNIT_US, UNIT_US, UNIT_US, UNIT_MS, UNIT_MS, UNIT_MS, UNIT_S}; // 大调节项单位数组 //

    /**
     * 大调节项的纳秒值数组
     * <p>每个调节项对应的纳秒值，便于快速计算</p>
     */
    public static final long[] LARGE_ITEM_NSS = {100, 100 * TIME_US2NS, 100 * 10 * TIME_US2NS, 100 * 100 * TIME_US2NS, 100 * TIME_MS2NS, 100 * 10 * TIME_MS2NS, 100 * 100 * TIME_MS2NS, 100 * TIME_S2NS}; // 大调节项纳秒值数组 //

    // ==================== 核心转换方法 ==================== //

    /**
     * 将纳秒值转换为带单位的时间值
     * <p>
     * 根据纳秒值的大小自动选择合适的单位（秒、毫秒、微秒、纳秒），
     * 并将转换后的值封装到ScaleValue对象中。
     * </p>
     *
     * <h3>转换规则：</h3>
     * <ul>
     *   <li>≥1秒：使用秒(s)为单位</li>
     *   <li>≥1毫秒：使用毫秒(ms)为单位</li>
     *   <li>≥1微秒：使用微秒(μs)为单位</li>
     *   <li><1微秒：使用纳秒(ns)为单位</li>
     * </ul>
     *
     * @param ns         纳秒值，需要转换的时间值（单位：纳秒）
     * @param scaleValue 输出参数，用于存储转换后的时间值、单位和调节步长
     *                   如果为null则直接返回，不做任何处理
     *
     * @see ScaleValue 时间值封装类
     */
    public static void getValueFromNS(long ns, ScaleValue scaleValue) { // 从纳秒值获取带单位的时间值 //
        if (scaleValue == null) return; // 空值检查，避免空指针异常 //
        DecimalFormat df = new DecimalFormat("###0.00", new DecimalFormatSymbols(Locale.CHINA)); // 创建格式化器，保留两位小数 //
        double value; // 声明转换后的数值变量 //
        if (ns / TIME_S2NS >= 1) { // 判断是否大于等于1秒 //
            value = Double.parseDouble(df.format(ns * 1.0 / TIME_S2NS)); // 转换为秒值并格式化 //
            scaleValue.setValue(value, UNIT_S, 1); // 设置秒单位，调节步长为1 //
        } else if (ns / TIME_MS2NS >= 1) { // 判断是否大于等于1毫秒 //
            value = Double.parseDouble(df.format(ns * 1.0 / TIME_MS2NS)); // 转换为毫秒值并格式化 //
            if (value / 100 >= 1) { // 判断是否大于等于100毫秒 //
                scaleValue.setValue(value, UNIT_MS, 100); // 设置毫秒单位，调节步长为100 //
            } else if (value / 10 >= 1) { // 判断是否大于等于10毫秒 //
                scaleValue.setValue(value, UNIT_MS, 10); // 设置毫秒单位，调节步长为10 //
            } else { // 小于10毫秒的情况 //
                scaleValue.setValue(value, UNIT_MS, 1); // 设置毫秒单位，调节步长为1 //
            }
        } else if (ns / TIME_US2NS >= 1) { // 判断是否大于等于1微秒 //
            value = Double.parseDouble(df.format(ns * 1.0 / TIME_US2NS)); // 转换为微秒值并格式化 //
            if (value / 100 >= 1) { // 判断是否大于等于100微秒 //
                scaleValue.setValue(value, UNIT_US, 100); // 设置微秒单位，调节步长为100 //
            } else if (value / 10 >= 1) { // 判断是否大于等于10微秒 //
                scaleValue.setValue(value, UNIT_US, 10); // 设置微秒单位，调节步长为10 //
            } else { // 小于10微秒的情况 //
                scaleValue.setValue(value, UNIT_US, 1); // 设置微秒单位，调节步长为1 //
            }
        } else { // 小于1微秒，使用纳秒单位 //
            String d = df.format(ns * 1.0); // 格式化纳秒值 //
            value = Double.parseDouble(d); // 解析为double类型 //
            scaleValue.setValue(value, UNIT_NS, 100); // 设置纳秒单位，调节步长为100 //
//            if (value / 100 >= 1) { // 已注释：原逻辑判断是否大于等于100纳秒 //
//                scaleValue.setValue(value, UNIT_NS, 100); // 已注释：设置纳秒单位，调节步长为100 //
//            } else if (value / 10 >= 1) { // 已注释：原逻辑判断是否大于等于10纳秒 //
//                scaleValue.setValue(value, UNIT_NS, 10); // 已注释：设置纳秒单位，调节步长为10 //
//            } else { // 已注释：原逻辑小于10纳秒的情况 //
//                scaleValue.setValue(value, UNIT_NS, 1); // 已注释：设置纳秒单位，调节步长为1 //
//            }
        }
    }

    /**
     * 将带单位的时间字符串转换为纳秒值
     * <p>
     * 解析时间字符串（如"1ms"、"100ns"、"1.5s"等），
     * 根据单位转换为对应的纳秒值。
     * </p>
     *
     * <h3>支持的单位：</h3>
     * <ul>
     *   <li>ns - 纳秒</li>
     *   <li>μs - 微秒</li>
     *   <li>ms - 毫秒</li>
     *   <li>s - 秒</li>
     * </ul>
     *
     * @param value 时间字符串，格式为"数值+单位"，如"1ms"、"100ns"
     * @return 转换后的纳秒值；如果输入为空或解析失败，返回0
     *
     * @see #UNIT_NS 纳秒单位
     * @see #UNIT_US 微秒单位
     * @see #UNIT_MS 毫秒单位
     * @see #UNIT_S 秒单位
     */
    public static long getNSFromValue(String value) { // 从带单位的时间字符串获取纳秒值 //
        if (StrUtil.isEmpty(value)) return 0; // 空值检查，返回0 //
        try { // 尝试解析时间字符串 //
            value = value.toLowerCase(); // 转换为小写，便于匹配 //
            if (value.endsWith(UNIT_NS)) { // 判断是否以纳秒单位结尾 //
                return (long) (Double.valueOf(value.replace(UNIT_NS, "")) * 1); // 提取数值并转换为纳秒 //
            } else if (value.endsWith(UNIT_US)) { // 判断是否以微秒单位结尾 //
                return (long) (Double.valueOf(value.replace(UNIT_US, "")) * TIME_US2NS); // 提取数值并转换为纳秒 //
            } else if (value.endsWith(UNIT_MS)) { // 判断是否以毫秒单位结尾 //
                return (long) (Double.valueOf(value.replace(UNIT_MS, "")) * TIME_MS2NS); // 提取数值并转换为纳秒 //
            } else { // 默认按秒处理 //
                return (long) (Double.valueOf(value.replace(UNIT_S, "")) * TIME_S2NS); // 提取数值并转换为纳秒 //
            }
        } catch (Exception e) { // 捕获解析异常 //
            return 0; // 解析失败返回0 //
        }
    }

    /**
     * 创建ScaleValue对象实例
     * <p>
     * 工厂方法，用于创建时间值封装对象。
     * 需要通过TopUtilBandWidthHz实例调用。
     * </p>
     *
     * @return 新创建的ScaleValue对象实例
     *
     * @see ScaleValue 时间值封装类
     */
    public ScaleValue createScaleValue() { // 创建ScaleValue对象实例 //
        return new ScaleValue(); // 返回新创建的ScaleValue对象 //
    }

    // ==================== 内部类定义 ==================== //

    /**
     * 时间值封装类
     * <p>
     * 用于封装时间值、单位和调节步长信息，
     * 便于在UI组件之间传递和显示时间参数。
     * </p>
     *
     * <h3>字段说明：</h3>
     * <ul>
     *   <li>value - 转换后的时间数值</li>
     *   <li>itemUnit - 时间单位字符串（如"ms"、"μs"等）</li>
     *   <li>itemValue - 调节步长值，用于UI调节时的增量</li>
     * </ul>
     */
    public class ScaleValue { // 时间值封装内部类 //

        /**
         * 时间数值
         * <p>转换后的时间值，与itemUnit配合表示完整时间</p>
         */
        public double value; // 时间数值 //

        /**
         * 时间单位字符串
         * <p>可选值：UNIT_NS、UNIT_US、UNIT_MS、UNIT_S</p>
         */
        public String itemUnit; // 时间单位字符串 //

        /**
         * 调节步长值
         * <p>用于UI调节时的增量，如100表示每次调节增加/减少100个单位</p>
         */
        public double itemValue; // 调节步长值 //

        /**
         * 设置时间值参数
         * <p>
         * 一次性设置时间值、单位和调节步长
         * </p>
         *
         * @param value     时间数值
         * @param itemUnit  时间单位字符串
         * @param itemValue 调节步长值
         */
        public void setValue(double value, String itemUnit, double itemValue) { // 设置时间值参数 //
            this.value = value; // 设置时间数值 //
            this.itemUnit = itemUnit; // 设置时间单位 //
            this.itemValue = itemValue; // 设置调节步长 //
        }
    }

    // ==================== 时间范围校验方法 ==================== //

    /**
     * 校验并修正时间值范围
     * <p>
     * 确保时间值在指定的最小值和最大值范围内，
     * 并将时间值对齐到最小间隔（8纳秒）的整数倍。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>如果当前值小于最小值，则设置为最小值</li>
     *   <li>如果当前值大于最大值，则设置为最大值</li>
     *   <li>将值对齐到TIME_MIN_INTERVAL（8纳秒）的整数倍</li>
     * </ol>
     *
     * @param curNs 当前时间值（纳秒）
     * @param minNs 允许的最小时间值（纳秒）
     * @param maxNs 允许的最大时间值（纳秒）
     * @return 校验并修正后的时间值（纳秒）
     *
     * @see #TIME_MIN_INTERVAL 时间最小间隔常量
     */
    public static long checkTime(long curNs, long minNs, long maxNs) { // 校验并修正时间值范围 //
        curNs = curNs < minNs ? minNs : curNs; // 如果小于最小值，则设置为最小值 //
        curNs = curNs > maxNs ? maxNs : curNs; // 如果大于最大值，则设置为最大值 //
        curNs = curNs - curNs % TIME_MIN_INTERVAL; // 对齐到8纳秒的整数倍 //
        return curNs; // 返回修正后的时间值 //
    }

    // ==================== 频率与时间转换方法 ==================== //

    /**
     * 根据时间值计算对应的频率值
     * <p>
     * 用于Top Trigger页面的时间框与Right Channel页面的频率显示之间的联动。
     * 将时间值转换为对应的频率字符串。
     * </p>
     *
     * <h3>转换公式：</h3>
     * <p>频率(Hz) = 1 / 时间(s) = 10^9 / 时间(ns) / 10</p>
     *
     * @param s 时间字符串，如"1ms"、"100ns"
     * @return 格式化的频率字符串，如"1.000kHz"
     *
     * @see TBookUtil#get_nsFromTime(String) 时间字符串转纳秒
     * @see TBookUtil#getHz3FromHz(double) 频率值格式化
     */
    public static String getHzFromS(String s) { // 从时间值获取频率值 //
        long ns = TBookUtil.get_nsFromTime(s); // 将时间字符串转换为纳秒值 //
        return TBookUtil.getHz3FromHz(1.0 * ns / 10); // 计算频率并格式化返回 //
    }

    /**
     * 根据频率值计算对应的时间值
     * <p>
     * 用于Right Channel页面的频率显示与Top Trigger页面的时间框之间的联动。
     * 将频率值转换为对应的时间字符串。
     * </p>
     *
     * <h3>转换公式：</h3>
     * <p>时间(ps) = 10^12 / 频率(Hz)</p>
     * <p>时间(ns) = 时间(ps) / 1000 / 10</p>
     *
     * @param hz 频率字符串，如"1kHz"、"100Hz"
     * @return 格式化的时间字符串，如"1.000ms"
     *
     * @see TBookUtil#getHzFromHz3(String) 频率字符串解析
     * @see TBookUtil#getTime3FromPs(long) 皮秒转时间字符串
     */
    public static String getSFromHz(String hz) { // 从频率值获取时间值 //
        long hzInt = TBookUtil.getHzFromHz3(hz); // 将频率字符串转换为频率整数值 //
        return TBookUtil.getTime3FromPs(hzInt * 10 * 1000 * 10); // 计算时间并格式化返回 //
        // 转换说明：hz和ns的换算因子为10，皮秒到纳秒的转换因子为1000*10 //
    }
}
