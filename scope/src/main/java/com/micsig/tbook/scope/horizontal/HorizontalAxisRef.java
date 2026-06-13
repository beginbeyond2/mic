package com.micsig.tbook.scope.horizontal;                                                // 包声明：水平轴模块

import com.micsig.tbook.scope.Data.WaveData;                                              // 导入：波形数据类
import com.micsig.tbook.scope.Display.Display;                                            // 导入：显示管理类
import com.micsig.tbook.scope.Event.EventBase;                                            // 导入：事件基类
import com.micsig.tbook.scope.Event.EventFactory;                                         // 导入：事件工厂类
import com.micsig.tbook.scope.Sample.IMemDepth;                                           // 导入：存储深度接口
import com.micsig.tbook.scope.Sample.MemDepthFactory;                                     // 导入：存储深度工厂类

import java.util.ArrayList;                                                               // 导入：动态数组类
import java.util.List;                                                                    // 导入：列表接口
import java.util.Observable;                                                              // 导入：观察者类
import java.util.Observer;                                                                // 导入：观察者接口

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              HorizontalAxisRef - 参考波形水平轴管理类                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   horizontal模块的参考波形水平轴管理类，位于horizontal包下，                   ║
 * ║   专门用于管理参考波形的时基和频率轴。                                         ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理参考波形的时基档位（时域）                                           ║
 * ║   2. 管理参考波形的频率档位（FFT频域）                                        ║
 * ║   3. 支持三种参考波形类型：动态通道、数学双通道运算、FFT                       ║
 * ║   4. 管理参考波形的时间位置                                                   ║
 * ║   5. 响应示波器事件更新水平轴                                                 ║
 * ║                                                                              ║
 * ║ 【参考波形类型】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        参考波形类型说明                               │ ║
 * ║   │                                                                      │ ║
 * ║   │   【REFTYPE_DYNAMICCH】动态通道参考波形（值=0）                        │ ║
 * ║   │   - 来源：实时采样的通道波形                                          │ ║
 * ║   │   - 时基：时域时基（39档，1KS ~ 250pS）                               │ ║
 * ║   │   - 用途：对比不同时间的波形                                          │ ║
 * ║   │                                                                      │ ║
 * ║   │   【REFTYPE_MATHDUAL】数学双通道运算参考波形（值=1）                   │ ║
 * ║   │   - 来源：数学运算结果波形（加、减、乘、除）                          │ ║
 * ║   │   - 时基：时域时基（39档，1KS ~ 250pS）                               │ ║
 * ║   │   - 用途：保存数学运算结果                                            │ ║
 * ║   │                                                                      │ ║
 * ║   │   【REFTYPE_MATHFFT】FFT频谱参考波形（值=2）                           │ ║
 * ║   │   - 来源：FFT变换结果波形                                             │ ║
 * ║   │   - 时基：频域频率档位（37档，1KGHz ~ 1Hz）                           │ ║
 * ║   │   - 用途：保存FFT频谱快照                                             │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【时基档位列表（时域）】                                                     ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        时基档位列表（39档）                           │ ║
 * ║   │                                                                      │ ║
 * ║   │   【秒级】1KS, 500S, 200S, 100S, 50S, 20S, 10S, 5S, 2S, 1S           │ ║
 * ║   │                                                                      │ ║
 * ║   │   【毫秒级】500mS, 200mS, 100mS, 50mS, 20mS, 10mS, 5mS, 2mS, 1mS    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【微秒级】500uS, 200uS, 100uS, 50uS, 20uS, 10uS, 5uS, 2uS, 1uS    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【纳秒级】500nS, 200nS, 100nS, 50nS, 20nS, 10nS, 5nS, 2nS, 1nS    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【皮秒级】500pS, 250pS                                             │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【频率档位列表（频域）】                                                     ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        频率档位列表（37档）                           │ ║
 * ║   │                                                                      │ ║
 * ║   │   【KGHz级】1KGHz                                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【GHz级】500GHz, 200GHz, 100GHz, 50GHz, 20GHz, 10GHz,              │ ║
 * ║   │            5GHz, 2GHz, 1GHz                                          │ ║
 * ║   │                                                                      │ ║
 * ║   │   【MHz级】500MHz, 200MHz, 100MHz, 50MHz, 20MHz, 10MHz,              │ ║
 * ║   │            5MHz, 2MHz, 1MHz                                          │ ║
 * ║   │                                                                      │ ║
 * ║   │   【KHz级】500KHz, 200KHz, 100KHz, 50KHz, 20KHz, 10KHz,              │ ║
 * ║   │            5KHz, 2KHz, 1KHz                                          │ ║
 * ║   │                                                                      │ ║
 * ║   │   【Hz级】500Hz, 200Hz, 100Hz, 50Hz, 20Hz, 10Hz, 5Hz, 2Hz, 1Hz       │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【与HorizontalAxis的区别】                                                  ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                    水平轴管理类对比                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【HorizontalAxis】主水平轴                                          │ ║
 * ║   │   - 管理实时波形的时基                                                │ ║
 * ║   │   - 单例模式                                                          │ ║
 * ║   │   - 与硬件采样同步                                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【HorizontalAxisRef】参考波形水平轴                                 │ ║
 * ║   │   - 管理参考波形的时基/频率                                           │ ║
 * ║   │   - 非单例模式，每个参考波形一个实例                                  │ ║
 * ║   │   - 独立于硬件采样                                                    │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【数据流】                                                                   ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 加载参考    │───▶│ 设置参考    │───▶│ 生成水平    │                   ║
 * ║   │ 波形数据    │    │ 类型和参数  │    │ 轴档位列表  │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 波形对比：将当前波形与历史波形对比                                     ║
 * ║   2. 数学运算保存：保存数学运算结果作为参考                                 ║
 * ║   3. FFT快照：保存FFT频谱快照用于对比                                       ║
 * ║   4. 故障诊断：对比正常波形和异常波形                                       ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 观察者模式：实现Observer接口，监听示波器事件                           ║
 * ║   - 非单例模式：每个参考波形独立管理                                       ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 使用synchronized保护xAxis和xAxisRatio列表访问                          ║
 * ║   - 成员变量无volatile修饰，需注意多线程访问                                ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - WaveData: 波形数据类                                                  ║
 * ║   - Display: 显示管理类                                                   ║
 * ║   - EventFactory: 事件工厂                                                ║
 * ║   - MemDepthFactory: 存储深度工厂                                         ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 参考波形水平轴管理类
 * 实现Observer接口，监听示波器事件
 * 专门用于管理参考波形的时基和频率轴
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>支持三种参考波形类型：动态通道、数学双通道运算、FFT</li>
 *   <li>时域时基管理：39个档位（1KS ~ 250pS）</li>
 *   <li>频域频率管理：37个档位（1KGHz ~ 1Hz）</li>
 *   <li>时间位置管理：支持标准窗口和缩放窗口</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 创建参考波形水平轴实例
 * HorizontalAxisRef hAxisRef = new HorizontalAxisRef();
 *
 * // 设置参考波形类型为FFT
 * hAxisRef.setRefType(HorizontalAxisRef.REFTYPE_MATHFFT);
 *
 * // 设置采样率和波形长度
 * hAxisRef.setSampleRate(1e9);
 * hAxisRef.setWaveLength(1000);
 *
 * // 获取时基值
 * double timeScale = hAxisRef.timeScaleIdVal(10);
 * </pre>
 *
 * @see HorizontalAxis
 * @see HorizontalAxisMath
 * @see WaveData
 */
public class HorizontalAxisRef implements Observer {                                       // 类声明：参考波形水平轴管理类，实现Observer接口

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签，用于日志输出时标识此类 */
    private static final String TAG = "HorizontalAxisRef";                                 // 静态常量：日志标签

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位常量定义（时域，39档）
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 最小时基档位索引 */
    public static final int TSI_MIN = 0;                                                   // 静态常量：最小时基档位索引

    /** 1000秒/格 */
    public static final int TSI_1KS = TSI_MIN;                                             // 静态常量：1000秒/格，索引0

    /** 500秒/格 */
    public static final int TSI_500S = 1;                                                  // 静态常量：500秒/格，索引1

    /** 200秒/格 */
    public static final int TSI_200S = 2;                                                  // 静态常量：200秒/格，索引2

    /** 100秒/格 */
    public static final int TSI_100S = 3;                                                  // 静态常量：100秒/格，索引3

    /** 50秒/格 */
    public static final int TSI_50S = 4;                                                   // 静态常量：50秒/格，索引4

    /** 20秒/格 */
    public static final int TSI_20S = 5;                                                   // 静态常量：20秒/格，索引5

    /** 10秒/格 */
    public static final int TSI_10S = 6;                                                   // 静态常量：10秒/格，索引6

    /** 5秒/格 */
    public static final int TSI_5S = 7;                                                    // 静态常量：5秒/格，索引7

    /** 2秒/格 */
    public static final int TSI_2S = 8;                                                    // 静态常量：2秒/格，索引8

    /** 1秒/格 */
    public static final int TSI_1S = 9;                                                    // 静态常量：1秒/格，索引9

    /** 500毫秒/格 */
    public static final int TSI_500mS = 10;                                                // 静态常量：500毫秒/格，索引10

    /** 200毫秒/格 */
    public static final int TSI_200mS = 11;                                                // 静态常量：200毫秒/格，索引11

    /** 100毫秒/格 */
    public static final int TSI_100mS = 12;                                                // 静态常量：100毫秒/格，索引12

    /** 50毫秒/格 */
    public static final int TSI_50mS = 13;                                                 // 静态常量：50毫秒/格，索引13

    /** 20毫秒/格 */
    public static final int TSI_20mS = 14;                                                 // 静态常量：20毫秒/格，索引14

    /** 10毫秒/格 */
    public static final int TSI_10mS = 15;                                                 // 静态常量：10毫秒/格，索引15

    /** 5毫秒/格 */
    public static final int TSI_5mS = 16;                                                  // 静态常量：5毫秒/格，索引16

    /** 2毫秒/格 */
    public static final int TSI_2mS = 17;                                                  // 静态常量：2毫秒/格，索引17

    /** 1毫秒/格 */
    public static final int TSI_1mS = 18;                                                  // 静态常量：1毫秒/格，索引18

    /** 500微秒/格 */
    public static final int TSI_500uS = 19;                                                // 静态常量：500微秒/格，索引19

    /** 200微秒/格 */
    public static final int TSI_200uS = 20;                                                // 静态常量：200微秒/格，索引20

    /** 100微秒/格 */
    public static final int TSI_100uS = 21;                                                // 静态常量：100微秒/格，索引21

    /** 50微秒/格 */
    public static final int TSI_50uS = 22;                                                 // 静态常量：50微秒/格，索引22

    /** 20微秒/格 */
    public static final int TSI_20uS = 23;                                                 // 静态常量：20微秒/格，索引23

    /** 10微秒/格 */
    public static final int TSI_10uS = 24;                                                 // 静态常量：10微秒/格，索引24

    /** 5微秒/格 */
    public static final int TSI_5uS = 25;                                                  // 静态常量：5微秒/格，索引25

    /** 2微秒/格 */
    public static final int TSI_2uS = 26;                                                  // 静态常量：2微秒/格，索引26

    /** 1微秒/格 */
    public static final int TSI_1uS = 27;                                                  // 静态常量：1微秒/格，索引27

    /** 500纳秒/格 */
    public static final int TSI_500nS = 28;                                                // 静态常量：500纳秒/格，索引28

    /** 200纳秒/格 */
    public static final int TSI_200nS = 29;                                                // 静态常量：200纳秒/格，索引29

    /** 100纳秒/格 */
    public static final int TSI_100nS = 30;                                                // 静态常量：100纳秒/格，索引30

    /** 50纳秒/格 */
    public static final int TSI_50nS = 31;                                                 // 静态常量：50纳秒/格，索引31

    /** 20纳秒/格 */
    public static final int TSI_20nS = 32;                                                 // 静态常量：20纳秒/格，索引32

    /** 10纳秒/格 */
    public static final int TSI_10nS = 33;                                                 // 静态常量：10纳秒/格，索引33

    /** 5纳秒/格 */
    public static final int TSI_5nS = 34;                                                  // 静态常量：5纳秒/格，索引34

    /** 2纳秒/格 */
    public static final int TSI_2nS = 35;                                                  // 静态常量：2纳秒/格，索引35

    /** 1纳秒/格 */
    public static final int TSI_1nS = 36;                                                  // 静态常量：1纳秒/格，索引36

    /** 500皮秒/格 */
    public static final int TSI_500pS = 37;                                                // 静态常量：500皮秒/格，索引37

    /** 250皮秒/格 */
    public static final int TSI_250pS = 38;                                                // 静态常量：250皮秒/格，索引38

    /** 最大时基档位索引 */
    public static final int TSI_MAX = TSI_250pS;                                           // 静态常量：最大时基档位索引，值为38

    // ═══════════════════════════════════════════════════════════════════════════════
    // 频率档位常量定义（频域，37档）
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 最小频率档位索引 */
    public static final int TSI_HZ_MIN = 0;                                                // 静态常量：最小频率档位索引

    /** 1KGHz（1000GHz） */
    public static final int TSI_HZ_1KG = TSI_HZ_MIN;                                       // 静态常量：1KGHz，索引0

    /** 500GHz */
    public static final int TSI_HZ_500G = 1 + TSI_HZ_1KG;                                  // 静态常量：500GHz，索引1

    /** 200GHz */
    public static final int TSI_HZ_200G = 2 + TSI_HZ_1KG;                                  // 静态常量：200GHz，索引2

    /** 100GHz */
    public static final int TSI_HZ_100G = 3 + TSI_HZ_1KG;                                  // 静态常量：100GHz，索引3

    /** 50GHz */
    public static final int TSI_HZ_50G = 4 + TSI_HZ_1KG;                                   // 静态常量：50GHz，索引4

    /** 20GHz */
    public static final int TSI_HZ_20G = 5 + TSI_HZ_1KG;                                   // 静态常量：20GHz，索引5

    /** 10GHz */
    public static final int TSI_HZ_10G = 6 + TSI_HZ_1KG;                                   // 静态常量：10GHz，索引6

    /** 5GHz */
    public static final int TSI_HZ_5G = 7 + TSI_HZ_1KG;                                    // 静态常量：5GHz，索引7

    /** 2GHz */
    public static final int TSI_HZ_2G = 8 + TSI_HZ_1KG;                                    // 静态常量：2GHz，索引8

    /** 1GHz */
    public static final int TSI_HZ_1G = 9 + TSI_HZ_1KG;                                    // 静态常量：1GHz，索引9

    /** 500MHz */
    public static final int TSI_HZ_500M = 1 + TSI_HZ_1G;                                   // 静态常量：500MHz，索引10

    /** 200MHz */
    public static final int TSI_HZ_200M = 2 + TSI_HZ_1G;                                   // 静态常量：200MHz，索引11

    /** 100MHz */
    public static final int TSI_HZ_100M = 3 + TSI_HZ_1G;                                   // 静态常量：100MHz，索引12

    /** 50MHz */
    public static final int TSI_HZ_50M = 4 + TSI_HZ_1G;                                    // 静态常量：50MHz，索引13

    /** 20MHz */
    public static final int TSI_HZ_20M = 5 + TSI_HZ_1G;                                    // 静态常量：20MHz，索引14

    /** 10MHz */
    public static final int TSI_HZ_10M = 6 + TSI_HZ_1G;                                    // 静态常量：10MHz，索引15

    /** 5MHz */
    public static final int TSI_HZ_5M = 7 + TSI_HZ_1G;                                     // 静态常量：5MHz，索引16

    /** 2MHz */
    public static final int TSI_HZ_2M = 8 + TSI_HZ_1G;                                     // 静态常量：2MHz，索引17

    /** 1MHz */
    public static final int TSI_HZ_1M = 9 + TSI_HZ_1G;                                     // 静态常量：1MHz，索引18

    /** 500KHz */
    public static final int TSI_HZ_500K = 1 + TSI_HZ_1M;                                   // 静态常量：500KHz，索引19

    /** 200KHz */
    public static final int TSI_HZ_200K = 2 + TSI_HZ_1M;                                   // 静态常量：200KHz，索引20

    /** 100KHz */
    public static final int TSI_HZ_100K = 3 + TSI_HZ_1M;                                   // 静态常量：100KHz，索引21

    /** 50KHz */
    public static final int TSI_HZ_50K = 4 + TSI_HZ_1M;                                    // 静态常量：50KHz，索引22

    /** 20KHz */
    public static final int TSI_HZ_20K = 5 + TSI_HZ_1M;                                    // 静态常量：20KHz，索引23

    /** 10KHz */
    public static final int TSI_HZ_10K = 6 + TSI_HZ_1M;                                    // 静态常量：10KHz，索引24

    /** 5KHz */
    public static final int TSI_HZ_5K = 7 + TSI_HZ_1M;                                     // 静态常量：5KHz，索引25

    /** 2KHz */
    public static final int TSI_HZ_2K = 8 + TSI_HZ_1M;                                     // 静态常量：2KHz，索引26

    /** 1KHz */
    public static final int TSI_HZ_1K = 9 + TSI_HZ_1M;                                     // 静态常量：1KHz，索引27

    /** 500Hz */
    public static final int TSI_HZ_500 = 1 + TSI_HZ_1K;                                    // 静态常量：500Hz，索引28

    /** 200Hz */
    public static final int TSI_HZ_200 = 2 + TSI_HZ_1K;                                    // 静态常量：200Hz，索引29

    /** 100Hz */
    public static final int TSI_HZ_100 = 3 + TSI_HZ_1K;                                    // 静态常量：100Hz，索引30

    /** 50Hz */
    public static final int TSI_HZ_50 = 4 + TSI_HZ_1K;                                     // 静态常量：50Hz，索引31

    /** 20Hz */
    public static final int TSI_HZ_20 = 5 + TSI_HZ_1K;                                     // 静态常量：20Hz，索引32

    /** 10Hz */
    public static final int TSI_HZ_10 = 6 + TSI_HZ_1K;                                     // 静态常量：10Hz，索引33

    /** 5Hz */
    public static final int TSI_HZ_5 = 7 + TSI_HZ_1K;                                      // 静态常量：5Hz，索引34

    /** 2Hz */
    public static final int TSI_HZ_2 = 8 + TSI_HZ_1K;                                      // 静态常量：2Hz，索引35

    /** 1Hz */
    public static final int TSI_HZ_1 = 9 + TSI_HZ_1K;                                      // 静态常量：1Hz，索引36

    /** 最大频率档位索引 */
    public static final int TSI_HZ_MAX = TSI_HZ_1;                                         // 静态常量：最大频率档位索引，值为36

    /** 频率档位总数 */
    public static final int TSI_HZ_CNT = TSI_HZ_MAX - TSI_HZ_MIN + 1;                      // 静态常量：频率档位总数，值为37

    // ═══════════════════════════════════════════════════════════════════════════════
    // 窗口模式常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 标准窗口模式 */
    public static final int WPI_STANDARD = 0;                                              // 静态常量：标准窗口模式，值为0

    /** 小窗口模式（与标准窗口相同） */
    public static final int WPI_SMALL = WPI_STANDARD;                                      // 静态常量：小窗口模式，值等于WPI_STANDARD

    /** 大窗口模式（放大窗口） */
    public static final int WPI_LARGE = 1;                                                 // 静态常量：大窗口模式，值为1

    // ═══════════════════════════════════════════════════════════════════════════════
    // 参考波形类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 动态通道参考波形 */
    public static final int REFTYPE_DYNAMICCH = 0;                                         // 静态常量：动态通道参考波形，值为0

    /** 数学双通道运算参考波形 */
    public static final int REFTYPE_MATHDUAL = 1;                                          // 静态常量：数学双通道运算参考波形，值为1

    /** FFT频谱参考波形 */
    public static final int REFTYPE_MATHFFT = 2;                                           // 静态常量：FFT频谱参考波形，值为2

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** X轴时基/频率值列表 */
    private List<Double> xAxis = new ArrayList<>();                                        // 成员变量：X轴时基/频率值列表

    /** X轴比率列表 */
    private List<Double> xAxisRatio = new ArrayList<>();                                   // 成员变量：X轴比率列表

    /** 参考波形类型：0-动态通道，1-数学双通道运算，2-FFT */
    private int refType = REFTYPE_DYNAMICCH;                                               // 成员变量：参考波形类型，默认为动态通道

    /** 采样率（Hz） */
    private double sampleRate = 0;                                                         // 成员变量：采样率，初始值为0

    /** 波形长度（点数） */
    private int waveLength = 0;                                                            // 成员变量：波形长度，初始值为0

    /** 主窗口时间位置（单位：0.1ps） */
    private long timePoseMain = 0;                                                         // 成员变量：主窗口时间位置，初始值为0

    /** 缩放窗口时间位置（单位：0.1ps） */
    private long timePoseZoom = 0;                                                         // 成员变量：缩放窗口时间位置，初始值为0

    /** 主窗口时基档位ID */
    private int timeScaleIdMain = TSI_5mS;                                                 // 成员变量：主窗口时基档位ID，默认为5mS

    /** 缩放窗口时基档位ID */
    private int timeScaleIdZoom = TSI_5mS;                                                 // 成员变量：缩放窗口时基档位ID，默认为5mS

    /** 波形起始X坐标 */
    private int startX = 0;                                                                // 成员变量：波形起始X坐标，初始值为0

    /** 波形结束X坐标 */
    private int endX = 0;                                                                  // 成员变量：波形结束X坐标，初始值为0

    /** 存储深度设置值 */
    private int memDepthSet = MemDepthFactory.getDefaultMemDepth();                        // 成员变量：存储深度设置值，使用默认值

    /** 存储深度项索引 */
    private int memDepthItemIdx = 0;                                                       // 成员变量：存储深度项索引，初始值为0

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数
     * 初始化参考波形水平轴，注册事件监听器
     */
    public HorizontalAxisRef() {                                                           // 构造方法：初始化参考波形水平轴
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OPEN, this);              // 注册通道打开事件监听器
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_CLOSE, this);             // 注册通道关闭事件监听器
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_ACTIVE, this);            // 注册通道激活事件监听器
        EventFactory.addEventObserver(EventFactory.EVENT_REF_WAVE_UPDATE, this);           // 注册参考波形更新事件监听器
        initAxis();                                                                        // 初始化水平轴
    }                                                                                       // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 参考波形类型设置和获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置参考波形类型
     *
     * @param refType 参考波形类型
     *                 REFTYPE_DYNAMICCH: 动态通道
     *                 REFTYPE_MATHDUAL: 数学双通道运算
     *                 REFTYPE_MATHFFT: FFT频谱
     */
    public void setRefType(int refType) {                                                  // 公有方法：设置参考波形类型
        this.refType = refType;                                                            // 更新参考波形类型
    }                                                                                       // 方法结束

    /**
     * 获取参考波形类型
     *
     * @return 参考波形类型
     */
    public int getRefType() {                                                              // 公有方法：获取参考波形类型
        return this.refType;                                                               // 返回参考波形类型
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 采样率和波形长度设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置采样率
     *
     * @param sampleRate 采样率（Hz）
     */
    public void setSampleRate(double sampleRate) {                                         // 公有方法：设置采样率
        this.sampleRate = sampleRate;                                                      // 更新采样率
    }                                                                                       // 方法结束

    /**
     * 设置波形长度
     *
     * @param waveLength 波形长度（点数）
     */
    public void setWaveLength(int waveLength) {                                            // 公有方法：设置波形长度
        this.waveLength = waveLength;                                                      // 更新波形长度
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 存储深度设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置存储深度值
     *
     * @param memDepth 存储深度值
     */
    public void setMemDepthSet(@MemDepthFactory.MEM_DEPTH int memDepth) {                  // 公有方法：设置存储深度值
        this.memDepthSet = memDepth;                                                       // 更新存储深度值
    }                                                                                       // 方法结束

    /**
     * 设置存储深度项索引
     *
     * @param memDepthItemIdx 存储深度项索引
     */
    public void setMemDepthItemIdx(int memDepthItemIdx) {                                  // 公有方法：设置存储深度项索引
        this.memDepthItemIdx = memDepthItemIdx;                                            // 更新存储深度项索引
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 水平轴初始化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化水平轴
     * 生成默认的水平轴列表
     */
    private void initAxis() {                                                              // 私有方法：初始化水平轴
        generateHorizontalAxis(0);                                                         // 调用generateHorizontalAxis()生成水平轴，参数为0
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // X轴访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取X轴时基/频率值列表
     * 返回列表的副本，避免外部修改
     *
     * @return X轴时基/频率值列表
     */
    public List<Double> getxAxis() {                                                       // 公有方法：获取X轴时基/频率值列表
        synchronized (this) {                                                              // 同步块：保护xAxis列表访问
            return new ArrayList<>(xAxis);                                                 // 返回xAxis的副本，避免外部修改
        }                                                                                   // 同步块结束
    }                                                                                       // 方法结束

    /**
     * 获取X轴比率列表
     *
     * @return X轴比率列表
     */
    public List<Double> getxAxisRatio() {                                                  // 公有方法：获取X轴比率列表
        synchronized (this) {                                                              // 同步块：保护xAxisRatio列表访问
            return xAxisRatio;                                                             // 返回xAxisRatio列表
        }                                                                                   // 同步块结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 水平轴生成方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 生成水平轴（使用当前参考类型）
     *
     * @param scaleVal 时基/频率值
     * @return 水平轴列表
     */
    public List<Double> generateHorizontalAxis(double scaleVal) {                           // 公有方法：生成水平轴，参数为时基/频率值
        return generateHorizontalAxis(scaleVal, refType);                                  // 调用重载方法，传入当前参考类型
    }                                                                                       // 方法结束

    /**
     * 生成水平轴（根据时基档位ID）
     *
     * @param baseScaleId 时基档位ID
     * @return 水平轴列表
     */
    public List<Double> generateHorizontalAxis(int baseScaleId) {                           // 公有方法：生成水平轴，参数为时基档位ID
        return generateHorizontalAxis(timeScaleIdVal(baseScaleId));                         // 将时基档位ID转换为时基值后调用重载方法
    }                                                                                       // 方法结束

    /**
     * 生成水平轴
     * 根据参考类型选择生成通道轴或FFT轴
     *
     * @param scaleVal 时基/频率值
     * @param refType  参考波形类型
     * @return 水平轴列表
     */
    public List<Double> generateHorizontalAxis(double scaleVal, int refType) {              // 公有方法：生成水平轴，参数为时基/频率值和参考类型
        if (refType == 2) {                                                                // 如果参考类型为FFT（值为2）
            return generateMathFftAxis(scaleVal, refType);                                 // 调用generateMathFftAxis()生成FFT轴
        } else {                                                                           // 否则（动态通道或数学双通道运算）
            return generateChannelAxis(scaleVal, refType);                                 // 调用generateChannelAxis()生成通道轴
        }                                                                                   // if-else语句结束
    }                                                                                       // 方法结束

    /**
     * 生成通道轴（时域）
     *
     * @param scaleVal 时基值
     * @param refType  参考波形类型
     * @return 通道轴列表
     */
    private List<Double> generateChannelAxis(double scaleVal, int refType) {                // 私有方法：生成通道轴（时域）
        xAxis.clear();                                                                     // 清空X轴列表
        double val = scaleVal;                                                             // 初始化时基值
        xAxis.add(val);                                                                    // 添加基准时基值到列表
        for (int i = TSI_MIN; i <= TSI_MAX; i++) {                                         // 循环：遍历所有时基档位
            val = timeScaleIdVal(i, refType);                                              // 获取当前档位的时基值
            if (val < scaleVal) {                                                          // 如果当前时基值小于基准值
                xAxis.add(val);                                                            // 添加到列表
            }                                                                               // if语句结束
        }                                                                                   // 循环结束
        return xAxis;                                                                      // 返回X轴列表
    }                                                                                       // 方法结束

    /**
     * 生成FFT轴（频域）
     *
     * @param freqVal 频率值
     * @param refType 参考波形类型
     * @return FFT轴列表
     */
    private List<Double> generateMathFftAxis(double freqVal, int refType) {                // 私有方法：生成FFT轴（频域）
        xAxis.clear();                                                                     // 清空X轴列表
        double val = freqVal;                                                              // 初始化频率值
        int i = TSI_HZ_MAX;                                                                // 初始化索引为最大频率档位
        for (; i >= TSI_HZ_MIN; i--) {                                                     // 循环：从高频到低频遍历
            val = fftTimeScaleIdVal(i);                                                    // 获取当前档位的频率值
            if (freqVal < val * 1.001) {                                                   // 如果频率值小于当前频率值（考虑0.1%误差）
                xAxis.add(val);                                                            // 添加到列表
                i++;                                                                       // 索引加1
                break;                                                                     // 跳出循环
            }                                                                               // if语句结束
        }                                                                                   // 循环结束
        for (; i <= TSI_HZ_MAX; i++) {                                                     // 循环：从当前索引到最大频率档位
            val = fftTimeScaleIdVal(i);                                                    // 获取当前档位的频率值
            xAxis.add(val);                                                                // 添加到列表
        }                                                                                   // 循环结束
        return xAxis;                                                                      // 返回X轴列表
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基值获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 根据时基档位ID获取时基值（使用当前参考类型）
     *
     * @param timeScaleId 时基档位ID
     * @return 时基值（秒/格）或频率值（Hz/格）
     */
    public double timeScaleIdVal(int timeScaleId) {                                        // 公有方法：根据时基档位ID获取时基值
        return timeScaleIdVal(timeScaleId, refType);                                       // 调用重载方法，传入当前参考类型
    }                                                                                       // 方法结束

    /**
     * 根据时基档位ID获取时基值
     * 根据参考类型选择返回时域时基值或频域频率值
     *
     * @param timeScaleId 时基档位ID
     * @param refType     参考波形类型
     * @return 时基值（秒/格）或频率值（Hz/格）
     */
    public double timeScaleIdVal(int timeScaleId, int refType) {                           // 公有方法：根据时基档位ID和参考类型获取时基值
        if (refType == WaveData.FFT_WAVE) {                                                // 如果参考类型为FFT波形
            return fftTimeScaleIdVal(timeScaleId);                                         // 返回FFT频率值
        } else {                                                                           // 否则（时域波形）
            return stdTimeScaleIdVal(timeScaleId);                                         // 返回标准时基值
        }                                                                                   // if-else语句结束
    }                                                                                       // 方法结束

    /**
     * 根据时基值获取标准化后的时基值（使用当前参考类型）
     *
     * @param timeScaleVal 时基值
     * @return 标准化后的时基值
     */
    public double timeScaleVal(double timeScaleVal) {                                      // 公有方法：根据时基值获取标准化后的时基值
        return timeScaleVal(timeScaleVal, refType);                                        // 调用重载方法，传入当前参考类型
    }                                                                                       // 方法结束

    /**
     * 根据时基值获取标准化后的时基值
     *
     * @param timeScaleVal 时基值
     * @param refType      参考波形类型
     * @return 标准化后的时基值
     */
    public double timeScaleVal(double timeScaleVal, int refType) {                         // 公有方法：根据时基值和参考类型获取标准化后的时基值
        if (refType == 2) {                                                                // 如果参考类型为FFT（值为2）
            return fftTimeScaleIdVal(fftTimeScaleId(timeScaleVal));                        // 返回FFT频率档位对应的频率值
        } else {                                                                           // 否则（时域波形）
            return stdTimeScaleIdVal(stdTimeScaleId(timeScaleVal));                        // 返回标准时基档位对应的时基值
        }                                                                                   // if-else语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位ID获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 根据时基值获取时基档位ID（使用当前参考类型）
     *
     * @param timeScaleVal 时基值
     * @return 时基档位ID
     */
    public int timeScaleId(double timeScaleVal) {                                          // 公有方法：根据时基值获取时基档位ID
        return timeScaleId(timeScaleVal, refType);                                         // 调用重载方法，传入当前参考类型
    }                                                                                       // 方法结束

    /**
     * 根据时基值获取时基档位ID
     *
     * @param timeScaleVal 时基值
     * @param refType      参考波形类型
     * @return 时基档位ID
     */
    public int timeScaleId(double timeScaleVal, int refType) {                             // 公有方法：根据时基值和参考类型获取时基档位ID
        if (refType == 2) {                                                                // 如果参考类型为FFT（值为2）
            return fftTimeScaleId(timeScaleVal);                                           // 返回FFT频率档位ID
        } else {                                                                           // 否则（时域波形）
            return stdTimeScaleId(timeScaleVal);                                           // 返回标准时基档位ID
        }                                                                                   // if-else语句结束
    }                                                                                       // 方法结束

    /**
     * 获取UI显示用的时基档位索引差值
     * 用于计算相对于基准档位的偏移量
     *
     * @param priTimeScaleVal 基准时基值
     * @param timeScaleVal    当前时基值
     * @param refType         参考波形类型
     * @return 时基档位索引差值
     */
    public int timeScaleId_ui(double priTimeScaleVal, double timeScaleVal, int refType) {  // 公有方法：获取UI显示用的时基档位索引差值
        if (refType == 2) {                                                                // 如果参考类型为FFT（值为2）
            return fftTimeScaleId(timeScaleVal) - fftTimeScaleId(priTimeScaleVal);         // 返回FFT频率档位索引差值
        } else {                                                                           // 否则（时域波形）
            return stdTimeScaleId(timeScaleVal) - stdTimeScaleId(priTimeScaleVal);         // 返回标准时基档位索引差值
        }                                                                                   // if-else语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 标准时基值转换方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 根据时基档位ID获取标准时基值（时域）
     *
     * @param timeScaleId 时基档位ID
     * @return 标准时基值（秒/格）
     */
    private double stdTimeScaleIdVal(int timeScaleId) {                                    // 私有方法：根据时基档位ID获取标准时基值
        double timeScaleArray[] = {                                                        // 时基值数组
                1000,                                                                      // 1000秒
                500, 200, 100, 50, 20, 10, 5, 2, 1,                                        // 秒级
                500e-3, 200e-3, 100e-3, 50e-3, 20e-3, 10e-3, 5e-3, 2e-3, 1e-3,             // 毫秒级
                500e-6, 200e-6, 100e-6, 50e-6, 20e-6, 10e-6, 5e-6, 2e-6, 1e-6,             // 微秒级
                500e-9, 200e-9, 100e-9, 50e-9, 20e-9, 10e-9, 5e-9, 2e-9, 1e-9,             // 纳秒级
                500e-12, 250e-12                                                           // 皮秒级
        };                                                                                  // 数组初始化结束
        if (timeScaleId < TSI_MIN) timeScaleId = TSI_MIN;                                  // 如果档位ID小于最小值，则设置为最小值
        if (timeScaleId > TSI_MAX) timeScaleId = TSI_MAX;                                  // 如果档位ID大于最大值，则设置为最大值
        return timeScaleArray[timeScaleId];                                                // 返回对应的时基值
    }                                                                                       // 方法结束

    /**
     * 根据时基值获取标准时基档位ID（时域）
     *
     * @param timeScaleVal 时基值
     * @return 标准时基档位ID
     */
    public int stdTimeScaleId(double timeScaleVal) {                                       // 公有方法：根据时基值获取标准时基档位ID
        double timeScaleArray[] = {                                                        // 时基值数组
                1000,                                                                      // 1000秒
                500, 200, 100, 50, 20, 10, 5, 2, 1,                                        // 秒级
                500e-3, 200e-3, 100e-3, 50e-3, 20e-3, 10e-3, 5e-3, 2e-3, 1e-3,             // 毫秒级
                500e-6, 200e-6, 100e-6, 50e-6, 20e-6, 10e-6, 5e-6, 2e-6, 1e-6,             // 微秒级
                500e-9, 200e-9, 100e-9, 50e-9, 20e-9, 10e-9, 5e-9, 2e-9, 1e-9,             // 纳秒级
                500e-12, 250e-12                                                           // 皮秒级
        };                                                                                  // 数组初始化结束
        int ret = TSI_MIN;                                                                 // 初始化返回值为最小档位ID
        for (int i = TSI_MIN; i <= TSI_MAX; i++) {                                         // 循环：遍历所有时基档位
            if (timeScaleVal > (timeScaleArray[i] - timeScaleArray[i] / 10)) {             // 如果时基值大于当前档位值（考虑10%误差）
                ret = i;                                                                   // 更新返回值为当前档位ID
                break;                                                                     // 跳出循环
            }                                                                               // if语句结束
        }                                                                                   // 循环结束
        return ret;                                                                        // 返回时基档位ID
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // FFT频率转换方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 计算FFT频率分辨率
     * 频率分辨率 = 采样率 / (波形长度 - 1)
     *
     * @return FFT频率分辨率（Hz）
     */
    public double fftFrequneceRatio() {                                                    // 公有方法：计算FFT频率分辨率
        return sampleRate / (waveLength - 1);                                              // 返回频率分辨率：采样率 / (波形长度 - 1)
    }                                                                                       // 方法结束

    /**
     * 根据频率档位ID获取频率值（频域）
     *
     * @param timeScaleId 频率档位ID
     * @return 频率值（Hz/格）
     */
    public double fftTimeScaleIdVal(int timeScaleId) {                                     // 公有方法：根据频率档位ID获取频率值
        double timeScaleArray[] = {                                                        // 频率值数组
                1000e9,                                                                    // 1KGHz
                500e9, 200e9, 100e9, 50e9, 20e9, 10e9, 5e9, 2e9, 1e9,                      // GHz级
                500e6, 200e6, 100e6, 50e6, 20e6, 10e6, 5e6, 2e6, 1e6,                      // MHz级
                500e3, 200e3, 100e3, 50e3, 20e3, 10e3, 5e3, 2e3, 1e3,                      // KHz级
                500, 200, 100, 50, 20, 10, 5, 2, 1                                         // Hz级
        };                                                                                  // 数组初始化结束
        if (timeScaleId < TSI_HZ_MIN) timeScaleId = TSI_HZ_MIN;                            // 如果档位ID小于最小值，则设置为最小值
        if (timeScaleId > TSI_HZ_MAX) timeScaleId = TSI_HZ_MAX;                            // 如果档位ID大于最大值，则设置为最大值
        return timeScaleArray[timeScaleId];                                                // 返回对应的频率值
    }                                                                                       // 方法结束

    /**
     * 根据频率值获取频率档位ID（频域）
     *
     * @param timeScaleVal 频率值
     * @return 频率档位ID
     */
    public int fftTimeScaleId(double timeScaleVal) {                                       // 公有方法：根据频率值获取频率档位ID
        double timeScaleArray[] = {                                                        // 频率值数组
                1000e9,                                                                    // 1KGHz
                500e9, 200e9, 100e9, 50e9, 20e9, 10e9, 5e9, 2e9, 1e9,                      // GHz级
                500e6, 200e6, 100e6, 50e6, 20e6, 10e6, 5e6, 2e6, 1e6,                      // MHz级
                500e3, 200e3, 100e3, 50e3, 20e3, 10e3, 5e3, 2e3, 1e3,                      // KHz级
                500, 200, 100, 50, 20, 10, 5, 2, 1                                         // Hz级
        };                                                                                  // 数组初始化结束
        int ret = 0;                                                                       // 初始化返回值为0
        for (int i = timeScaleArray.length - 1; i >= 0; i--) {                             // 循环：从低频到高频遍历
            if (timeScaleVal < timeScaleArray[i] * 1.001) {                                // 如果频率值小于当前频率值（考虑0.1%误差）
                ret = i;                                                                   // 更新返回值为当前档位ID
                break;                                                                     // 跳出循环
            }                                                                               // if语句结束
        }                                                                                   // 循环结束
        return ret;                                                                        // 返回频率档位ID
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 窗口模式判断方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前窗口模式ID
     *
     * @return 窗口模式ID
     *         WPI_STANDARD: 标准窗口
     *         WPI_LARGE: 放大窗口
     */
    public int getWPIId() {                                                                // 公有方法：获取当前窗口模式ID
        int id = WPI_STANDARD;                                                             // 初始化为标准窗口
        if (Display.getInstance().isZoom())                                                // 如果处于缩放模式
            id = WPI_LARGE;                                                                // 设置为大窗口模式
        return id;                                                                         // 返回窗口模式ID
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时基档位获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前视图的时基档位ID
     *
     * @return 时基档位ID
     */
    public int getTimeScaleIdOfView() {                                                    // 公有方法：获取当前视图的时基档位ID
        return getTimeScaleIdOfView(getWPIId());                                           // 调用重载方法，传入当前窗口模式ID
    }                                                                                       // 方法结束

    /**
     * 设置当前视图的时基档位ID
     *
     * @param timeScaleId 时基档位ID
     */
    public void setTimeScaleIdOfView(int timeScaleId) {                                    // 公有方法：设置当前视图的时基档位ID
        setTimeScaleIdOfView(getWPIId(), timeScaleId);                                     // 调用重载方法，传入当前窗口模式ID
    }                                                                                       // 方法结束

    /**
     * 设置指定窗口的时基档位ID
     *
     * @param WPIId      窗口模式ID
     * @param timeScaleId 时基档位ID
     */
    public void setTimeScaleIdOfView(int WPIId, int timeScaleId) {                         // 公有方法：设置指定窗口的时基档位ID
        if (WPIId == WPI_LARGE) {                                                          // 如果是大窗口模式
            timeScaleIdZoom = timeScaleId;                                                 // 设置缩放窗口时基档位ID
        } else {                                                                           // 否则（标准窗口模式）
            timeScaleIdMain = timeScaleId;                                                 // 设置主窗口时基档位ID
        }                                                                                   // if-else语句结束
    }                                                                                       // 方法结束

    /**
     * 获取指定窗口的时基档位ID
     *
     * @param WPIId 窗口模式ID
     * @return 时基档位ID
     */
    public int getTimeScaleIdOfView(int WPIId) {                                           // 公有方法：获取指定窗口的时基档位ID
        if (WPIId == WPI_LARGE) return timeScaleIdZoom;                                    // 如果是大窗口模式，返回缩放窗口时基档位ID
        return timeScaleIdMain;                                                            // 否则返回主窗口时基档位ID
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 时间位置获取和设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前视图的时间位置
     *
     * @return 时间位置（单位：0.1ps）
     */
    public long getTimePosOfView() {                                                       // 公有方法：获取当前视图的时间位置
        return getTimePosOfView(getWPIId());                                               // 调用重载方法，传入当前窗口模式ID
    }                                                                                       // 方法结束

    /**
     * 设置当前视图的时间位置
     *
     * @param timePos 时间位置（单位：0.1ps）
     */
    public void setTimePosOfView(long timePos) {                                           // 公有方法：设置当前视图的时间位置
        setTimePosOfView(getWPIId(), timePos);                                             // 调用重载方法，传入当前窗口模式ID
    }                                                                                       // 方法结束

    /**
     * 获取指定窗口的时间位置
     *
     * @param WPIId 窗口模式ID
     * @return 时间位置（单位：0.1ps）
     */
    public long getTimePosOfView(int WPIId) {                                              // 公有方法：获取指定窗口的时间位置
        if (WPIId == WPI_LARGE) {                                                          // 如果是大窗口模式
            return timePoseZoom;                                                           // 返回缩放窗口时间位置
        } else {                                                                           // 否则（标准窗口模式）
            return timePoseMain;                                                           // 返回主窗口时间位置
        }                                                                                   // if-else语句结束
    }                                                                                       // 方法结束

    /**
     * 设置指定窗口的时间位置
     *
     * @param WPIId   窗口模式ID
     * @param timePos 时间位置（单位：0.1ps）
     */
    public void setTimePosOfView(int WPIId, long timePos) {                                // 公有方法：设置指定窗口的时间位置
        if (WPIId == WPI_LARGE) {                                                          // 如果是大窗口模式
            timePoseZoom = timePos;                                                        // 设置缩放窗口时间位置
        } else {                                                                           // 否则（标准窗口模式）
            timePoseMain = timePos;                                                        // 设置主窗口时间位置
        }                                                                                   // if-else语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 波形起始和结束X坐标方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取波形起始X坐标
     *
     * @return 起始X坐标
     */
    public int getStartX() {                                                               // 公有方法：获取波形起始X坐标
        return startX;                                                                     // 返回起始X坐标
    }                                                                                       // 方法结束

    /**
     * 设置波形起始X坐标
     *
     * @param startX 起始X坐标
     */
    public void setStartX(int startX) {                                                    // 公有方法：设置波形起始X坐标
        this.startX = startX;                                                              // 设置起始X坐标
    }                                                                                       // 方法结束

    /**
     * 获取波形结束X坐标
     *
     * @return 结束X坐标
     */
    public int getEndX() {                                                                 // 公有方法：获取波形结束X坐标
        return endX;                                                                       // 返回结束X坐标
    }                                                                                       // 方法结束

    /**
     * 设置波形结束X坐标
     *
     * @param endX 结束X坐标
     */
    public void setEndX(int endX) {                                                        // 公有方法：设置波形结束X坐标
        this.endX = endX;                                                                  // 设置结束X坐标
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件观察者方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 事件观察者更新方法
     * 处理各类示波器事件
     *
     * @param observable 事件源
     * @param data       事件数据
     */
    @Override
    public void update(Observable observable, Object data) {                               // 重写方法：事件观察者更新方法
        EventBase eventBase = (EventBase) data;                                            // 将事件数据转换为EventBase类型
        switch (eventBase.getId()) {                                                       // 根据事件ID进行分支处理
            case EventFactory.EVENT_CHANNEL_OPEN:                                          // 通道打开事件
            case EventFactory.EVENT_CHANNEL_CLOSE:                                         // 通道关闭事件
            case EventFactory.EVENT_CHANNEL_ACTIVE:                                        // 通道激活事件
                break;                                                                     // 不做处理
            case EventFactory.EVENT_REF_WAVE_UPDATE:                                       // 参考波形更新事件
                // TODO 刷新xAxis内容                                                       // 待实现：刷新xAxis内容
                break;                                                                     // 跳出switch语句
        }                                                                                   // switch语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 档位标准化函数
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 档位标准化函数：分解原档位值
     * 将档位值分解为数值、量级和小量级
     *
     * @param scale       档位值
     * @param rank        量级（输出参数）
     * @param littleRank  小量级（输出参数）
     * @return 分解后的数值
     */
    static double decomposeStandardScale(double scale, int rank, int littleRank) {         // 静态方法：档位标准化函数，分解原档位值
        rank = 0;                                                                          // 初始化量级为0
        while (scale > 1e3) {                                                              // 循环：当数值大于1000时
            scale /= 1e3;                                                                  // 数值除以1000
            (rank)++;                                                                      // 量级加1
        }                                                                                   // 循环结束
        while (scale < 1 && scale != 0) {                                                  // 循环：当数值小于1且不为0时
            scale *= 1e3;                                                                  // 数值乘以1000
            (rank)--;                                                                      // 量级减1
        }                                                                                   // 循环结束
        littleRank = 0;                                                                    // 初始化小量级为0
        while (scale > 10) {                                                               // 循环：当数值大于10时
            scale /= 10;                                                                   // 数值除以10
            (littleRank)++;                                                                // 小量级加1
        }                                                                                   // 循环结束
        if (10 - scale < 1) {                                                              // 如果10减数值小于1（防止出现9.99的现象）
            scale /= 10;                                                                   // 数值除以10
            (littleRank)++;                                                                // 小量级加1
        }                                                                                   // if语句结束
        return scale;                                                                      // 返回分解后的数值
    }                                                                                       // 方法结束

    /**
     * 档位标准化函数：还原分解值
     * 将分解后的数值、量级和小量级还原为档位值
     *
     * @param scale      数值
     * @param rank       量级
     * @param littleRank 小量级
     * @return 还原后的档位值
     */
    static double combinateStandardScale(double scale, int rank, int littleRank) {         // 静态方法：档位标准化函数，还原分解值
        while (littleRank > 0) {                                                           // 循环：当小量级大于0时
            scale *= 10;                                                                   // 数值乘以10
            littleRank--;                                                                  // 小量级减1
        }                                                                                   // 循环结束
        while (rank != 0) {                                                                // 循环：当量级不为0时
            if (rank > 0) {                                                                // 如果量级大于0
                scale *= 1e3;                                                              // 数值乘以1000
                rank--;                                                                    // 量级减1
            } else {                                                                       // 否则（量级小于0）
                scale *= 1e-3;                                                             // 数值乘以0.001
                rank++;                                                                    // 量级加1
            }                                                                               // if-else语句结束
        }                                                                                   // 循环结束
        return scale;                                                                      // 返回还原后的档位值
    }                                                                                       // 方法结束
}                                                                                           // 类结束
