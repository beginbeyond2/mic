package com.micsig.tbook.ui.data;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗
 *                                  DTimeBaseGear - 时基档位数据管理类
 * ╠═══════════════════════════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 示波器数据管理模块，用于管理时基档位的枚举值和转换逻辑。
 *
 * 【核心职责】
 * 1. 定义示波器支持的时基档位常量（从1ns到1ks）
 * 2. 提供时基档位与字符串表示的转换
 * 3. 提供时基档位与单位字符串的转换
 * 4. 管理当前时基档位状态
 *
 * 【架构设计】
 * 采用单例模式（静态内部类实现），确保全局只有一个实例：
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    DTimeBaseGear                                 │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
 * │  │ 单例管理    │  │ 档位常量    │  │ 转换方法                │  │
 * │  │ 静态内部类  │  │ 37个档位   │  │ getTimeBaseGear_NumUnit │  │
 * │  │ getInstance │  │ ns~ks范围  │  │ getTimeBaseGear_Unit    │  │
 * │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【时基档位范围】
 * - 最快档位：1ns（每格1纳秒）
 * - 最慢档位：1ks（每格1千秒）
 * - 档位数量：37个
 * - 档位间隔：1-2-5序列（1, 2, 5, 10, 20, 50...）
 *
 * 【数据流向】
 * 外部获取实例 → getInstance() → 调用转换方法 → 返回字符串结果
 *
 * 【依赖关系】
 * 无外部依赖，纯数据管理类
 *
 * 【使用示例】
 * // 获取单例实例
 * DTimeBaseGear timeBaseGear = DTimeBaseGear.getInstance();
 *
 * // 设置当前档位
 * timeBaseGear.setCurrTimeBaseGear(DTimeBaseGear.TimeGear_1ms);
 *
 * // 获取档位字符串（如"1ms"）
 * String gearStr = timeBaseGear.getTimeBaseGear_NumUnit(timeBaseGear.getCurrTimeBaseGear());
 *
 * // 获取单位字符串（如"ms"）
 * String unit = timeBaseGear.getTimeBaseGear_Unit(timeBaseGear.getCurrTimeBaseGear());
 *
 * 【注意事项】
 * 1. 使用静态内部类实现单例，线程安全且延迟加载
 * 2. 档位常量为private，外部需通过方法访问
 * 3. 默认档位为200ns
 * 4. 未知档位返回默认值"50ms"
 *
 * @author liwb
 * @version 1.0
 * @since 2017/4/10
 */
public class DTimeBaseGear {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例模式实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 静态内部类持有单例实例
     * 利用类加载机制保证线程安全
     */
    private static class TimeBaseGearHolder{
        /** 单例实例，final保证不可变 */
        private static final DTimeBaseGear instance=new DTimeBaseGear();
    }

    /**
     * 默认构造方法
     * 公开构造方法，但推荐使用getInstance()获取实例
     */
    public DTimeBaseGear(){}

    /**
     * 获取单例实例
     * 推荐使用此方法获取DTimeBaseGear实例
     *
     * @return DTimeBaseGear单例实例
     */
    public static final DTimeBaseGear getInstance(){
        return TimeBaseGearHolder.instance;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位常量定义
    // 按时间单位分组，从慢到快排列
    // ═══════════════════════════════════════════════════════════════════════════════

    // ─────────────────────────────────────────────────────────────────────────────
    // 千秒级档位
    // ─────────────────────────────────────────────────────────────────────────────

    /** 时基档位：1ks（千秒） */
    private static final int TimeGear_1Ks=0x01;

    // ─────────────────────────────────────────────────────────────────────────────
    // 秒级档位
    // ─────────────────────────────────────────────────────────────────────────────

    /** 时基档位：500s */
    private static final int TimeGear_500s=0x02;

    /** 时基档位：200s */
    private static final int TimeGear_200s=0x03;

    /** 时基档位：100s */
    private static final int TimeGear_100s=0x04;

    /** 时基档位：50s */
    private static final int TimeGear_50s=0x05;

    /** 时基档位：20s */
    private static final int TimeGear_20s=0x06;

    /** 时基档位：10s */
    private static final int TimeGear_10s=0x07;

    /** 时基档位：5s */
    private static final int TimeGear_5s=0x08;

    /** 时基档位：2s */
    private static final int TimeGear_2s=0x09;

    /** 时基档位：1s */
    private static final int TimeGear_1s=0x0a;

    // ─────────────────────────────────────────────────────────────────────────────
    // 毫秒级档位
    // ─────────────────────────────────────────────────────────────────────────────

    /** 时基档位：500ms */
    private static final int TimeGear_500ms=0x0b;

    /** 时基档位：200ms */
    private static final int TimeGear_200ms=0x0c;

    /** 时基档位：100ms */
    private static final int TimeGear_100ms=0x0d;

    /** 时基档位：50ms */
    private static final int TimeGear_50ms=0x0e;

    /** 时基档位：20ms */
    private static final int TimeGear_20ms=0x0f;

    /** 时基档位：10ms */
    private static final int TimeGear_10ms=0x10;

    /** 时基档位：5ms */
    private static final int TimeGear_5ms=0x11;

    /** 时基档位：2ms */
    private static final int TimeGear_2ms=0x12;

    /** 时基档位：1ms */
    private static final int TimeGear_1ms=0x13;

    // ─────────────────────────────────────────────────────────────────────────────
    // 微秒级档位
    // ─────────────────────────────────────────────────────────────────────────────

    /** 时基档位：500us */
    private static final int TimeGear_500us=0x14;

    /** 时基档位：200us */
    private static final int TimeGear_200us=0x15;

    /** 时基档位：100us */
    private static final int TimeGear_100us=0x16;

    /** 时基档位：50us */
    private static final int TimeGear_50us=0x17;

    /** 时基档位：20us */
    private static final int TimeGear_20us=0x18;

    /** 时基档位：10us */
    private static final int TimeGear_10us=0x19;

    /** 时基档位：5us */
    private static final int TimeGear_5us=0x1a;

    /** 时基档位：2us */
    private static final int TimeGear_2us=0x1b;

    /** 时基档位：1us */
    private static final int TimeGear_1us=0x1c;

    // ─────────────────────────────────────────────────────────────────────────────
    // 纳秒级档位
    // ─────────────────────────────────────────────────────────────────────────────

    /** 时基档位：500ns */
    private static final int TimeGear_500ns=0x1d;

    /** 时基档位：200ns */
    private static final int TimeGear_200ns=0x1e;

    /** 时基档位：100ns */
    private static final int TimeGear_100ns=0x1f;

    /** 时基档位：50ns */
    private static final int TimeGear_50ns=0x20;

    /** 时基档位：20ns */
    private static final int TimeGear_20ns=0x21;

    /** 时基档位：10ns */
    private static final int TimeGear_10ns=0x22;

    /** 时基档位：5ns */
    private static final int TimeGear_5ns=0x23;

    /** 时基档位：2ns */
    private static final int TimeGear_2ns=0x24;

    /** 时基档位：1ns（最快档位） */
    private static final int TimeGear_1ns=0x25;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 当前档位状态
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 当前时基档位，默认为200ns */
    private int currTimeBaseGear=TimeGear_200ns;

    /**
     * 获取当前时基档位
     *
     * @return 当前时基档位值
     */
    public int getCurrTimeBaseGear() {
        return currTimeBaseGear;
    }

    /**
     * 设置当前时基档位
     *
     * @param currTimeBaseGear 时基档位值
     */
    public void setCurrTimeBaseGear(int currTimeBaseGear) {
        this.currTimeBaseGear = currTimeBaseGear;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 转换方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取时基档位的完整字符串表示
     * 返回格式为"数值+单位"，如"1ms"、"200ns"等
     *
     * @param timeBaseGear 时基档位值
     * @return 时基档位的字符串表示，未知档位返回"50ms"
     */
    public String getTimeBaseGear_NumUnit(int timeBaseGear){
        String timeGear="50ms";

        switch(timeBaseGear){
            // 千秒级
            case TimeGear_1Ks:timeGear="1ks";break;

            // 秒级
            case TimeGear_500s:timeGear="500s"; break;
            case TimeGear_200s:timeGear="200s"; break;
            case TimeGear_100s:timeGear="100s"; break;
            case TimeGear_50s: timeGear="50s";  break;
            case TimeGear_20s: timeGear="20s";  break;
            case TimeGear_10s: timeGear="10s";  break;
            case TimeGear_5s:  timeGear="5s";   break;
            case TimeGear_2s:  timeGear="2s";   break;
            case TimeGear_1s:  timeGear="1s";   break;

            // 毫秒级
            case TimeGear_500ms:timeGear="500ms";break;
            case TimeGear_200ms:timeGear="200ms";break;
            case TimeGear_100ms:timeGear="100ms";break;
            case TimeGear_50ms: timeGear="50ms"; break;
            case TimeGear_20ms: timeGear="20ms"; break;
            case TimeGear_10ms: timeGear="10ms"; break;
            case TimeGear_5ms:  timeGear="5ms";  break;
            case TimeGear_2ms:  timeGear="2ms";  break;
            case TimeGear_1ms:  timeGear="1ms";  break;

            // 微秒级
            case TimeGear_500us:timeGear="500us"; break;
            case TimeGear_200us:timeGear="200us"; break;
            case TimeGear_100us:timeGear="100us"; break;
            case TimeGear_50us: timeGear="50us";  break;
            case TimeGear_20us: timeGear="20us";  break;
            case TimeGear_10us: timeGear="10us";  break;
            case TimeGear_5us:  timeGear="5us";   break;
            case TimeGear_2us:  timeGear="2us";   break;
            case TimeGear_1us:  timeGear="1us";   break;

            // 纳秒级
            case TimeGear_500ns:timeGear="500ns"; break;
            case TimeGear_200ns:timeGear="200ns"; break;
            case TimeGear_100ns:timeGear="100ns"; break;
            case TimeGear_50ns: timeGear="50ns";  break;
            case TimeGear_20ns: timeGear="20ns";  break;
            case TimeGear_10ns: timeGear="10ns";  break;
            case TimeGear_5ns:  timeGear="5ns";   break;
            case TimeGear_2ns:  timeGear="2ns";   break;
            case TimeGear_1ns:  timeGear="1ns";   break;

            // 未知档位，返回默认值
            default:timeGear="50ms";break;
        }
        return timeGear;
    }

    /**
     * 获取时基档位的单位字符串
     * 只返回单位部分，如"ns"、"us"、"ms"、"s"、"ks"
     *
     * @param timeBaseGear 时基档位值
     * @return 单位字符串，未知档位返回"ms"
     */
    public String getTimeBaseGear_Unit(int timeBaseGear){
        String unit="ms";

        switch(timeBaseGear){
            // 千秒级
            case TimeGear_1Ks:unit="ks";break;

            // 秒级：所有秒级档位返回"s"
            case TimeGear_500s:
            case TimeGear_200s:
            case TimeGear_100s:
            case TimeGear_50s:
            case TimeGear_20s:
            case TimeGear_10s:
            case TimeGear_5s:
            case TimeGear_2s:
            case TimeGear_1s:  unit="s";   break;

            // 毫秒级：所有毫秒级档位返回"ms"
            case TimeGear_500ms:
            case TimeGear_200ms:
            case TimeGear_100ms:
            case TimeGear_50ms:
            case TimeGear_20ms:
            case TimeGear_10ms:
            case TimeGear_5ms:
            case TimeGear_2ms:
            case TimeGear_1ms:  unit="ms";  break;

            // 微秒级：所有微秒级档位返回"us"
            case TimeGear_500us:
            case TimeGear_200us:
            case TimeGear_100us:
            case TimeGear_50us:
            case TimeGear_20us:
            case TimeGear_10us:
            case TimeGear_5us:
            case TimeGear_2us:
            case TimeGear_1us:  unit="us";   break;

            // 纳秒级：所有纳秒级档位返回"ns"
            case TimeGear_500ns:
            case TimeGear_200ns:
            case TimeGear_100ns:
            case TimeGear_50ns:
            case TimeGear_20ns:
            case TimeGear_10ns:
            case TimeGear_5ns:
            case TimeGear_2ns:
            case TimeGear_1ns:  unit="ns";   break;

            // 未知档位，返回默认单位
            default:unit="ms";break;
        }
        return unit;
    }
}
