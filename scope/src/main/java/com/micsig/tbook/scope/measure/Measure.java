package com.micsig.tbook.scope.measure;


import android.util.Log;

import androidx.annotation.IntDef;

import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Data.SyncHeader;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                Measure - 示波器测量类                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Measure模块的示波器测量类，位于measure包下，                                  ║
 * ║   提供示波器的自动测量功能，包括时域测量、频域测量、相位测量等。                  ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 定义测量类型常量（周期、频率、上升时间等）                                 ║
 * ║   2. 管理测量参数配置（上限、中限、下限、范围等）                               ║
 * ║   3. 执行测量计算（通过JNI调用Native方法）                                     ║
 * ║   4. 提供测量结果访问接口                                                     ║
 * ║   5. 管理测量统计信息                                                         ║
 * ║                                                                              ║
 * ║ 【测量类型分类】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        测量类型分类                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【时域测量】                                                        │ ║
 * ║   │   - 周期 (Period)           - 频率 (Frequency)                       │ ║
 * ║   │   - 上升时间 (RiseTime)     - 下降时间 (FallTime)                    │ ║
 * ║   │   - 正占空比 (PosDuty)      - 负占空比 (NegDuty)                     │ ║
 * ║   │   - 正脉宽 (PosPulseWidth)  - 负脉宽 (NegPulseWidth)                 │ ║
 * ║   │   - 突发宽度 (BurstWidth)   - 延迟 (Delay)                           │ ║
 * ║   │   - 相位 (Phase)                                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【电压测量】                                                        │ ║
 * ║   │   - 峰峰值 (PkPk)           - 幅度 (Amplitude)                       │ ║
 * ║   │   - 高电平 (High)           - 低电平 (Low)                           │ ║
 * ║   │   - 最大值 (Max)            - 最小值 (Min)                           │ ║
 * ║   │   - 有效值 (RMS)            - 整流平均值 (CRMS)                      │ ║
 * ║   │   - 平均值 (Mean)           - 整流平均值 (CMEAN)                     │ ║
 * ║   │   - AC有效值 (AC_RMS)                                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   【过冲/下冲测量】                                                   │ ║
 * ║   │   - 正过冲 (PosOvershoot)   - 负过冲 (NegOvershoot)                  │ ║
 * ║   │   - 正下冲 (PosUndershoot)  - 负下冲 (NegUndershoot)                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   【边沿测量】                                                        │ ║
 * ║   │   - 第一个上升沿 (FirstRiseEdge)                                     │ ║
 * ║   │   - 第一个下降沿 (FirstFallEdge)                                     │ ║
 * ║   │   - 第二个上升沿 (SecondRiseEdge)                                    │ ║
 * ║   │   - 第二个下降沿 (SecondFallEdge)                                    │ ║
 * ║   │   - 最后上升沿 (LastRiseEdge)                                        │ ║
 * ║   │   - 最后下降沿 (LastFallEdge)                                        │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【数据结构】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                    ByteBuffer结构（单位：字节）                       │ ║
 * ║   │                                                                      │ ║
 * ║   │   【Header区域：0-63】                                                │ ║
 * ║   │   0-3    chIdx        通道索引 (32bit)                               │ ║
 * ║   │   4-11   enable       测量项使能位图 (64bit)                         │ ║
 * ║   │   12-19  valid        测量项有效位图 (64bit)                         │ ║
 * ║   │   20     phaseRefCh   相位参考通道 (8bit)                            │ ║
 * ║   │   21     delaySelfEdge 延迟自边沿 (8bit)                             │ ║
 * ║   │   22     delayRefCh   延迟参考通道 (8bit)                            │ ║
 * ║   │   23     delayRefEdge 延迟参考边沿 (8bit)                            │ ║
 * ║   │   24-25  col          列号 (16bit)                                  │ ║
 * ║   │   26     upper        上限百分比 (8bit)                              │ ║
 * ║   │   27     middle       中限百分比 (8bit)                             │ ║
 * ║   │   28     lower        下限百分比 (8bit)                             │ ║
 * ║   │   29     abs          绝对测量标志 (8bit)                            │ ║
 * ║   │   30-31  begin        开始位置 (16bit)                              │ ║
 * ║   │   32-33  end          结束位置 (16bit)                              │ ║
 * ║   │   34-35  height       波形高度 (16bit)                              │ ║
 * ║   │   36-39  vrate        垂直速率 (32bit float)                        │ ║
 * ║   │   40-43  hrate        水平速率 (32bit float)                        │ ║
 * ║   │   44-47  pos          通道位置 (32bit)                              │ ║
 * ║   │   48-51  vscale       垂直档位 (32bit float)                        │ ║
 * ║   │   52-55  absUpper     绝对上限 (32bit)                              │ ║
 * ║   │   56-59  absMiddle    绝对中限 (32bit)                              │ ║
 * ║   │   60-63  absLow       绝对下限 (32bit)                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【测量值区域：64-...】                                              │ ║
 * ║   │   每个测量项占4字节(float)                                           │ ║
 * ║   │   每个测量项的指示位置占16字节(4个方向×4字节)                          │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【测量流程】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 设置测量参数 │───▶│ 调用测量计算 │───▶│ 获取测量结果 │                   ║
 * ║   │ (上限/下限) │    │ MeasureCalc │    │ getMeasureVal│                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 自动测量功能：周期、频率、幅度等                                        ║
 * ║   2. 相位测量：两个通道之间的相位差                                          ║
 * ║   3. 延迟测量：两个通道之间的时间延迟                                        ║
 * ║   4. 光标测量：手动测量两点之间的差值                                        ║
 * ║   5. 统计测量：测量值的最小、最大、平均值                                    ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 部分方法使用synchronized保护                                            ║
 * ║   - ByteBuffer操作需要注意线程安全                                          ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - IChannel: 通道接口，获取波形数据                                        ║
 * ║   - WaveData: 波形数据类                                                   ║
 * ║   - MeasureStatics: 测量统计类                                              ║
 * ║   - HwConfig: 硬件配置类                                                   ║
 * ║   - Native方法: JNI测量计算                                                ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class Measure {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 内部类 - 测量类型定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 测量类型定义类
     * 使用@IntDef注解定义测量类型常量，提供编译时类型检查
     */
    public static class MeasureType {
        /**
         * 测量类型注解
         * 用于限制参数只能是定义的测量类型常量
         */
        @IntDef({MEASURE_FIRST,MEASURE_PERIOD, MEASURE_FREQ, MEASURE_RISETIME, MEASURE_FALLTIME, MEASURE_DELAY,
                MEASURE_POSITIVE_DUTY, MEASURE_NEGATIVE_DUTY, MEASURE_POSITIVE_PULSE_WIDTH,
                MEASURE_NEGATIVE_PULSE_WIDTH, MEASURE_BURST_WIDTH, MEASURE_POSITIVE_OVERSHOOT,
                MEASURE_NEGATIVE_OVERSHOOT, MEASURE_PHASE, MEASURE_PK_PK, MEASURE_AMPLITUDE,
                MEASURE_HIGH, MEASURE_LOW, MEASURE_MAX, MEASURE_MIN, MEASURE_RMS, MEASURE_CRMS,
                MEASURE_MEAN, MEASURE_CMEAN,MEASURE_POSITIVE_UNDERSHOOT,MEASURE_NEGATIVE_UNDERSHOOT,
                MEASURE_AC_RMS,MEASURE_POSITIVE_RATE,MEASURE_NEGATIVE_RATE,MEASURE_LAST,
                MEASURE_TVALUE,
                MEASURE_CURSOR_X1,MEASURE_CURSOR_X2,MEASURE_COLV,
                MEASURE_CLIPPING,
                MEASURE_TIME_POT, MEASURE_FIRST_RISE_EDGE, MEASURE_FIRST_FALL_EDGE,
                MEASURE_SECON_RISE_EDGE, MEASURE_SECON_FALL_EDGE, MEASURE_LAST_RISE_EDGE,
                MEASURE_LAST_FALL_EDGE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface MeasureItemType {
        }

        // ────────────────────────────────────────────────────────────────────────────
        // 时域测量类型常量
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * 测量类型起始值
         */
        public static final int MEASURE_FIRST = 16;

        /**
         * 周期测量
         * 测量信号的一个完整周期时间
         */
        public static final int MEASURE_PERIOD = MEASURE_FIRST;

        /**
         * 频率测量
         * 测量信号的频率（周期的倒数）
         */
        public static final int MEASURE_FREQ = 17;

        /**
         * 上升时间测量
         * 测量信号从10%上升到90%的时间
         */
        public static final int MEASURE_RISETIME = 18;

        /**
         * 下降时间测量
         * 测量信号从90%下降到10%的时间
         */
        public static final int MEASURE_FALLTIME = 19;

        /**
         * 延迟测量
         * 测量两个通道之间的时间延迟
         */
        public static final int MEASURE_DELAY = 20;

        /**
         * 正占空比测量
         * 测量信号高电平时间占周期的百分比
         */
        public static final int MEASURE_POSITIVE_DUTY = 21;

        /**
         * 负占空比测量
         * 测量信号低电平时间占周期的百分比
         */
        public static final int MEASURE_NEGATIVE_DUTY = 22;

        /**
         * 正脉宽测量
         * 测量信号高电平的脉冲宽度
         */
        public static final int MEASURE_POSITIVE_PULSE_WIDTH = 23;

        /**
         * 负脉宽测量
         * 测量信号低电平的脉冲宽度
         */
        public static final int MEASURE_NEGATIVE_PULSE_WIDTH = 24;

        /**
         * 突发宽度测量
         * 测量突发信号的宽度
         */
        public static final int MEASURE_BURST_WIDTH = 25;

        // ────────────────────────────────────────────────────────────────────────────
        // 过冲/相位测量类型常量
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * 正过冲测量
         * 测量信号正方向的过冲幅度
         */
        public static final int MEASURE_POSITIVE_OVERSHOOT = 26;

        /**
         * 负过冲测量
         * 测量信号负方向的过冲幅度
         */
        public static final int MEASURE_NEGATIVE_OVERSHOOT = 27;

        /**
         * 相位测量
         * 测量两个通道之间的相位差
         */
        public static final int MEASURE_PHASE = 28;

        // ────────────────────────────────────────────────────────────────────────────
        // 电压测量类型常量
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * 峰峰值测量
         * 测量信号的最大值与最小值之差
         */
        public static final int MEASURE_PK_PK = 29;

        /**
         * 幅度测量
         * 测量信号的幅度
         */
        public static final int MEASURE_AMPLITUDE = 30;

        /**
         * 高电平测量
         * 测量信号的高电平值
         */
        public static final int MEASURE_HIGH = 31;

        /**
         * 低电平测量
         * 测量信号的低电平值
         */
        public static final int MEASURE_LOW = 32;

        /**
         * 最大值测量
         * 测量信号的最大值
         */
        public static final int MEASURE_MAX = 33;

        /**
         * 最小值测量
         * 测量信号的最小值
         */
        public static final int MEASURE_MIN = 34;

        /**
         * 有效值测量（RMS）
         * 测量信号的均方根值
         */
        public static final int MEASURE_RMS = 35;

        /**
         * 整流平均值测量（CRMS）
         * 测量信号的整流平均值
         */
        public static final int MEASURE_CRMS = 36;

        /**
         * 平均值测量
         * 测量信号的平均值
         */
        public static final int MEASURE_MEAN = 37;

        /**
         * 整流平均值测量（CMEAN）
         * 测量信号的整流平均值
         */
        public static final int MEASURE_CMEAN = 38;

        /**
         * AC有效值测量
         * 测量信号的AC分量有效值
         */
        public static final int MEASURE_AC_RMS = 39;

        /**
         * 正斜率测量
         * 测量信号的正向斜率
         */
        public static final int MEASURE_POSITIVE_RATE = 40;

        /**
         * 负斜率测量
         * 测量信号的负向斜率
         */
        public static final int MEASURE_NEGATIVE_RATE = 41;

        /**
         * T值测量
         * 测量信号的T值
         */
        public static final int MEASURE_TVALUE = 42;

        /**
         * 测量类型结束值
         */
        public static final int MEASURE_LAST = MEASURE_TVALUE;


        // ────────────────────────────────────────────────────────────────────────────
        // 光标和边沿测量类型常量
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * 第一个上升沿位置
         * 选通区域最左侧=0，单位S[内部使用]，用于计算周期
         */
        public static final int MEASURE_CURSOR_X1 = 51;

        /**
         * 光标X2位置
         */
        public static final int MEASURE_CURSOR_X2 = 52;

        /**
         * 正下冲测量
         * 测量信号正方向的下冲幅度
         */
        public static final int MEASURE_POSITIVE_UNDERSHOOT = 53;

        /**
         * 负下冲测量
         * 测量信号负方向的下冲幅度
         */
        public static final int MEASURE_NEGATIVE_UNDERSHOOT = 54;

        /**
         * 列值测量
         */
        public static final int MEASURE_COLV = 55;

        /**
         * 削波测量
         * 0: 不削波, 1: 正削, 2: 负削, 3: 双削, 4: 无波
         */
        public static final int MEASURE_CLIPPING = 56;

        /**
         * 时间点测量
         */
        public static final int MEASURE_TIME_POT = 57;

        /**
         * 第一个上升沿位置
         */
        public static final int MEASURE_FIRST_RISE_EDGE = 58;

        /**
         * 第一个下降沿位置
         * 参见"第一个上升沿位置"
         */
        public static final int MEASURE_FIRST_FALL_EDGE = 59;

        /**
         * 第二个上升沿位置
         */
        public static final int MEASURE_SECON_RISE_EDGE = 60;

        /**
         * 第二个下降沿位置
         */
        public static final int MEASURE_SECON_FALL_EDGE = 61;

        /**
         * 最后一个上升沿位置
         */
        public static final int MEASURE_LAST_RISE_EDGE = 62;

        /**
         * 最后一个下降沿位置
         */
        public static final int MEASURE_LAST_FALL_EDGE = 63;

        /**
         * 测量项最大数量
         */
        public static final int MEASURE_ITEM_MAX = 64;

    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 指示位置常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 左侧指示位置
     */
    public static final int INDICATION_LEFT  =   (0);

    /**
     * 顶部指示位置
     */
    public static final int INDICATION_TOP   =   (1);

    /**
     * 右侧指示位置
     */
    public static final int INDICATION_RIGHT =   (2);

    /**
     * 底部指示位置
     */
    public static final int INDICATION_BOTTOM=   (3);

    // ═══════════════════════════════════════════════════════════════════════════════
    // 边沿类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 第一个上升沿
     */
    public static final int RL_FIRST_RISE_EDGE = 0;

    /**
     * 第一个下降沿
     */
    public static final int RL_FIRST_FALL_EDGE = 1;

    /**
     * 最后一个上升沿
     */
    public static final int RL_LAST_RISE_EDGE = 2;

    /**
     * 最后一个下降沿
     */
    public static final int RL_LAST_FALL_EDGE = 3;

    // ═══════════════════════════════════════════════════════════════════════════════
    // ByteBuffer结构说明
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * ByteBuffer数据结构说明
     * 
     * header 16 * 4  = 64 byte
     * 0,1,2,3 chIdx 32bit 4 byte
     * 4,5,6,7,8,9,10,11 enable 64bit 8 byte
     * 12,13,14,15,16,17,18,19 valid 64bit 8 byte
     * 20 phase relation to ch 1 byte
     * 21 delay edge 1byte
     * 22 delay relation to ch 1 byte
     * 23 delay relation to ch edge 1 byte
     * 24 col 2 byte
     * 26 high 1 byte
     * 27 middle 1 byte
     * 28 low 1 byte
     * 29 abs 1 byte
     * 30 begin 2 byte
     * 32 end 2 byte
     * 34 h 2 byte
     * 36 vrate 4 byte;
     * 40 hrate 4 byte
     * 44 pos 4 byte
     * 48 vscale 4 byte
     * 52 absHigh 4 byte
     * 56 absMiddle 4 byte
     * 60 absLow 4 byte
     */

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 字节缓冲区
     * 存储测量参数和测量结果
     * 使用直接内存，便于JNI访问
     */
    private ByteBuffer byteBuffer;

    /**
     * 浮点缓冲区
     * byteBuffer的FloatBuffer视图，用于访问浮点测量值
     */
    private FloatBuffer floatBuffer;

    /**
     * 通道接口
     * 关联的通道对象，用于获取波形数据
     */
    private IChannel channel;

    /**
     * 测量统计对象
     * 用于统计测量值的最小、最大、平均值等
     */
    private MeasureStatics measureStatics;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造Measure实例
     * 初始化测量缓冲区和默认参数
     * 
     * <p><b>缓冲区大小计算：</b></p>
     * <ul>
     *   <li>测量项数量：MEASURE_FIRST * 4</li>
     *   <li>指示位置：(MEASURE_ITEM_MAX - MEASURE_FIRST) * 4 * 5</li>
     *   <li>Header：64字节</li>
     * </ul>
     *
     * @param channel 关联的通道对象
     */
    public Measure(IChannel channel) {
        this.channel = channel;                                                      // 保存通道引用
        byteBuffer = ByteBuffer.allocateDirect(MeasureType.MEASURE_FIRST * 4       // 分配直接内存
                +  (MeasureType.MEASURE_ITEM_MAX - MeasureType.MEASURE_FIRST) * 4 * 5
                + 64);
        byteBuffer.order(ByteOrder.nativeOrder());                                  // 设置本地字节序
        floatBuffer = byteBuffer.asFloatBuffer();                                   // 创建FloatBuffer视图
        byteBuffer.putInt(0, channel.getChId() & 0xFF);                             // 设置通道索引
        byteBuffer.putLong(4, 0);                                                   // 初始化使能位图为0
        byteBuffer.putLong(12, 0);                                                  // 初始化有效位图为0

        MeasureUpper(90);                                                           // 设置默认上限90%
        MeasureMiddle(50);                                                          // 设置默认中限50%
        MeasureLower(10);                                                           // 设置默认下限10%
        MeasureBegin(0);                                                            // 设置默认开始位置0
        MeasureEnd(ScopeBase.getWidth()-1);                                         // 设置默认结束位置
        setMeasureAbs(false);                                                       // 设置默认非绝对测量
        measureStatics = new MeasureStatics(this);                                  // 创建测量统计对象
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 基本信息方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取通道索引
     *
     * @return 通道索引
     */
    public int getChIdx(){
        return channel.getChId();                                                   // 返回通道ID
    }

    /**
     * 获取测量统计信息
     *
     * @param measureType 测量类型
     * @return 测量统计Bean
     */
    public MeasureStaticsBean getMeasureStatics(@MeasureType.MeasureItemType int measureType){
        return measureStatics.getStatics(measureType);                              // 返回指定类型的统计信息
    }

    /**
     * 检查测量是否启用
     *
     * @return true: 已启用
     *         false: 未启用
     */
    public boolean isEnable() {
        long val = byteBuffer.getLong(4);                                           // 获取使能位图
        return val != 0;                                                            // 返回是否启用
    }

    /**
     * 获取测量统计对象
     *
     * @return MeasureStatics实例
     */
    public MeasureStatics getMeasureStatics(){
        return measureStatics;                                                      // 返回统计对象
    }

    /**
     * 设置统计项启用状态
     *
     * @param measureType 测量类型
     * @param bEnable true: 启用
     *                false: 禁用
     */
    public void setStaticsItemEnable(@MeasureType.MeasureItemType int measureType,boolean bEnable){
        measureStatics.setStaticsItemEnable(measureType,bEnable);                   // 设置统计项启用状态
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 相位和延迟测量配置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置相位参考通道
     *
     * @param chIdx 参考通道索引
     */
    public void MeasurePhase(int chIdx) {
        byteBuffer.put(20, (byte)(chIdx & 0xFF));                                   // 设置相位参考通道
    }

    /**
     * 延迟测量启用标志
     */
    private boolean bMeasureDelay = false;

    /**
     * 相位测量启用标志
     */
    private boolean bMeasurePhase = false;

    /**
     * 检查是否启用延迟测量
     *
     * @return true: 已启用
     *         false: 未启用
     */
    public boolean isMeasureDelay(){
        return  bMeasureDelay;                                                      // 返回延迟测量标志
    }

    /**
     * 检查是否启用相位测量
     *
     * @return true: 已启用
     *         false: 未启用
     */
    public boolean isMeasurePhase(){
        return  bMeasurePhase;                                                      // 返回相位测量标志
    }

    /**
     * 启用/禁用延迟测量
     *
     * @param bEnable true: 启用
     *                false: 禁用
     */
    public void MeasureDelayEnable(boolean bEnable){
        bMeasureDelay = bEnable;                                                    // 设置延迟测量标志
        MeasureItemEnable(MeasureType.MEASURE_DELAY,bEnable);                       // 启用/禁用延迟测量项
    }

    /**
     * 启用/禁用相位测量
     *
     * @param bEnable true: 启用
     *                false: 禁用
     */
    public void MeasurePhaseEnable(boolean bEnable){
        bMeasurePhase = bEnable;                                                    // 设置相位测量标志
        MeasureItemEnable(MeasureType.MEASURE_PHASE,bEnable);                       // 启用/禁用相位测量项
    }

    /**
     * 设置延迟测量参数
     *
     * @param selfEdge 自身边沿类型
     * @param chIdx 参考通道索引
     * @param chEdge 参考通道边沿类型
     */
    public void MeasureDelay(int selfEdge, int chIdx, int chEdge) {
        byteBuffer.put(21, (byte) (selfEdge & 0xFF));                               // 设置自身边沿
        byteBuffer.put(22, (byte)(chIdx& 0xFF));                                    // 设置参考通道
        byteBuffer.put(23, (byte)(chEdge& 0xFF));                                   // 设置参考边沿
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量参数配置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置测量列号
     * 自动测试使用
     *
     * @param col 列号
     */
    public void MeasureCol(int col){
        byteBuffer.putShort(24,(short)(col & 0xFFFF));                              // 设置列号
    }

    /**
     * 设置测量光标位置
     * 线程安全方法
     *
     * @param x1 光标X1位置
     * @param x2 光标X2位置
     */
    public synchronized void setMeasureCursor(int x1, int x2){
        this.x1 = x1;                                                               // 保存X1位置
        this.x2 = x2;                                                               // 保存X2位置
        int v = ((x1 & 0xFFF) << 20) | ((x2 & 0xFFF) << 8) | (channel.getChId() & 0xFF); // 打包光标位置
        byteBuffer.putInt(0,v);                                                     // 写入缓冲区
    }

    /**
     * 光标X1位置
     */
    int x1 = 0;

    /**
     * 光标X2位置
     */
    int x2 = 0;

    /**
     * 获取光标X1位置
     * 线程安全方法
     *
     * @return X1位置
     */
    public synchronized int getCursorX1(){
        return x1;                                                                  // 返回X1位置
    }

    /**
     * 获取光标X2位置
     * 线程安全方法
     *
     * @return X2位置
     */
    public synchronized int getCursorX2(){
        return x2;                                                                  // 返回X2位置
    }

    /**
     * 设置测量上限百分比
     *
     * @param val 上限百分比（0-100）
     */
    public void MeasureUpper(int val){
        byteBuffer.put(26,(byte) (val & 0xFF));                                     // 设置上限
    }

    /**
     * 设置测量中限百分比
     *
     * @param val 中限百分比（0-100）
     */
    public void MeasureMiddle(int val){
        byteBuffer.put(27,(byte) (val & 0xFF));                                     // 设置中限
    }

    /**
     * 设置测量下限百分比
     *
     * @param val 下限百分比（0-100）
     */
    public void MeasureLower(int val){
        byteBuffer.put(28,(byte) (val & 0xFF));                                     // 设置下限
    }

    /**
     * 设置是否使用绝对测量
     *
     * @param bAbs true: 绝对测量
     *             false: 相对测量
     */
    public void setMeasureAbs(boolean bAbs){
        byteBuffer.put(29,(byte) (bAbs ? 1 : 0));                                   // 设置绝对测量标志
    }

    /**
     * 检查是否使用绝对测量
     *
     * @return true: 绝对测量
     *         false: 相对测量
     */
    public boolean isAbsMeasure(){
        return (byteBuffer.get(29) & 0xFF) == 0x01;                                 // 返回绝对测量标志
    }

    /**
     * 设置光标测量范围标志
     * 线程安全方法
     *
     * @param bCursorMeasureRange true: 使用光标范围
     *                             false: 使用全屏范围
     */
    public synchronized void setCursorMeasureRange(boolean bCursorMeasureRange){
        this.bCursorMeasureRange = bCursorMeasureRange;                             // 设置光标测量范围标志
    }

    /**
     * 检查是否使用光标测量范围
     * 线程安全方法
     *
     * @return true: 使用光标范围
     *         false: 使用全屏范围
     */
    private synchronized boolean isCursorMeasureRange(){
        return bCursorMeasureRange;                                                 // 返回光标测量范围标志
    }

    /**
     * 光标测量范围标志
     */
    boolean bCursorMeasureRange = false;

    /**
     * 设置测量开始位置
     *
     * @param val 开始位置（像素）
     */
    public void MeasureBegin(int val){

        byteBuffer.putShort(30,(short) (val & 0xFFFF));                             // 设置开始位置
    }

    /**
     * 设置测量结束位置
     *
     * @param val 结束位置（像素）
     */
    public void MeasureEnd(int val){
        byteBuffer.putShort(32,(short) (val & 0xFFFF));                             // 设置结束位置
    }

    /**
     * 设置波形高度
     *
     * @param val 波形高度（像素）
     */
    public void WaveHeight(int val){
        byteBuffer.putShort(34,(short) (val & 0xFFFF));                             // 设置波形高度
    }

    /**
     * 设置波形高度比率
     *
     * @param val 高度比率（缩放时使用）
     */
    public void WaveHeightRate(float val){
        byteBuffer.putFloat(36,val);                                                // 设置高度比率
    }

    /**
     * 获取水平速率
     *
     * @return 水平速率（秒/像素）
     */
    public float getHorizontalRate(){
        return byteBuffer.getFloat(40);                                             // 返回水平速率
    }

    /**
     * 设置通道位置
     *
     * @param pos 通道位置（像素）
     */
    public void setChPos(int pos){
        byteBuffer.putInt(44,pos);                                                  // 设置通道位置
    }

    /**
     * 设置垂直速率
     *
     * @param val 垂直速率（伏/像素）
     */
    public void setVRate(float val){
        byteBuffer.putFloat(48,val);                                                // 设置垂直速率
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绝对测量方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 计算绝对测量参数
     * 将绝对电压值转换为像素值
     */
    private void abs(){
        HwConfig hwConfig = HwConfig.getInstance();                                 // 获取硬件配置实例
        //Log.d("zhuzh","absUpper:" + absUpper + ",absMiddle:" + absMiddle + ",absLow:" + absLow + ",adpix:" + channel.getADVerticalPerPix());
        byteBuffer.putInt(52, (int) Math.round((absUpper / (channel.getADVerticalPerPix() * hwConfig.getWavFactor())) * ScopeBase.getToFPGACoff())); // 计算绝对上限像素值
        byteBuffer.putInt(56, (int) Math.round((absMiddle / (channel.getADVerticalPerPix() * hwConfig.getWavFactor())) * ScopeBase.getToFPGACoff())); // 计算绝对中限像素值
        byteBuffer.putInt(60, (int) Math.round((absLow / (channel.getADVerticalPerPix() * hwConfig.getWavFactor())) * ScopeBase.getToFPGACoff())); // 计算绝对下限像素值

        byteBuffer.putFloat(1024,(float) (tvalue_v/(channel.getADVerticalPerPix() * hwConfig.getWavFactor()))); // 设置T值电压
        byteBuffer.putInt(1028,tvalue_n);                                           // 设置T值数量
    }

    /**
     * 绝对上限值
     * 单位：伏
     */
    double absUpper = 0.9;

    /**
     * 绝对中限值
     * 单位：伏
     */
    double absMiddle = 0.5;

    /**
     * 绝对下限值
     * 单位：伏
     */
    double absLow = 0.1;

    /**
     * 设置绝对上限值
     *
     * @param high 上限值（伏）
     */
    public void setAbsUpper(double high){
        this.absUpper = high;                                                       // 设置绝对上限
    }

    /**
     * 设置绝对中限值
     *
     * @param val 中限值（伏）
     */
    public void setAbsMiddle(double val){
        this.absMiddle = val;                                                       // 设置绝对中限
    }

    /**
     * 设置绝对下限值
     *
     * @param val 下限值（伏）
     */
    public void setAbsLow(double val){
        this.absLow = val;                                                          // 设置绝对下限
    }

    /**
     * T值电压
     */
    double tvalue_v = 0;

    /**
     * T值数量
     */
    int tvalue_n = 1;

    /**
     * 设置T值参数
     *
     * @param v 电压值
     * @param n 数量
     */
    public void setTValue(double v,int n){
        tvalue_v = v;                                                               // 设置T值电压
        tvalue_n = n;                                                               // 设置T值数量
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量参数获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取测量上限百分比
     *
     * @return 上限百分比
     */
    public int getMeasureUpper(){
        return byteBuffer.get(26) & 0xFF;                                           // 返回上限百分比
    }

    /**
     * 获取测量中限百分比
     *
     * @return 中限百分比
     */
    public int getMeasureMiddle(){
        return byteBuffer.get(27) & 0xFF;                                           // 返回中限百分比
    }

    /**
     * 获取测量下限百分比
     *
     * @return 下限百分比
     */
    public int getMeasureLower(){
        return byteBuffer.get(28) & 0xFF;                                           // 返回下限百分比
    }

    /**
     * 获取测量开始位置
     *
     * @return 开始位置
     */
    public int getMeasureBegin(){
        return byteBuffer.getShort(30) & 0xFFFF;                                    // 返回开始位置
    }

    /**
     * 获取测量结束位置
     *
     * @return 结束位置
     */
    public int getMeasureEnd(){
        return byteBuffer.getShort(32) & 0xFFFF;                                    // 返回结束位置
    }

    /**
     * 获取相位参考通道索引
     *
     * @return 参考通道索引
     */
    public int getPhaseRefChIdx() {
        return byteBuffer.get(20) & 0xFF;                                           // 返回相位参考通道
    }

    /**
     * 获取延迟自身边沿
     *
     * @return 自身边沿类型
     */
    public int getDelaySelfEdge() {
        return byteBuffer.getInt(21) & 0xFF;                                        // 返回自身边沿
    }

    /**
     * 获取延迟参考通道索引
     *
     * @return 参考通道索引
     */
    public int getDelayRefChIdx() {
        return byteBuffer.getInt(22) & 0xFF;                                        // 返回参考通道
    }

    /**
     * 获取延迟参考通道边沿
     *
     * @return 参考边沿类型
     */
    public int getDelayRefChEdge() {
        return byteBuffer.getInt(23) & 0xFF;                                        // 返回参考边沿
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量项启用/有效状态方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 检查测量项是否启用
     *
     * @param measureType 测量类型
     * @return true: 已启用
     *         false: 未启用
     */
    public boolean isMeasureItemEnable(@MeasureType.MeasureItemType int measureType) {
        long val = byteBuffer.getLong(4);                                           // 获取使能位图
        return ((val >>> measureType) & 0x01) != 0;                                 // 检查指定位是否为1
    }

    /**
     * 设置测量项启用状态
     *
     * @param measureType 测量类型
     * @param enable true: 启用
     *               false: 禁用
     */
    public void MeasureItemEnable(@MeasureType.MeasureItemType int measureType, boolean enable) {
        long mask = 1;                                                              // 初始化掩码
        long val = byteBuffer.getLong(4);                                           // 获取当前使能位图
        mask = mask << measureType;                                                 // 左移到指定位
        val &= ~(mask);                                                             // 清除该位
        if (enable)                                                                 // 如果启用
            val |= mask;                                                            // 设置该位
        byteBuffer.putLong(4, val);                                                 // 写回使能位图
    }

    /**
     * 设置测量项有效状态
     *
     * @param measureType 测量类型
     * @param bValid true: 有效
     *               false: 无效
     */
    public void setMeasureItemValid(@MeasureType.MeasureItemType int measureType, boolean bValid) {
        long mask = 1;                                                              // 初始化掩码
        long val = byteBuffer.getLong(12);                                          // 获取当前有效位图
        mask = mask << measureType;                                                 // 左移到指定位
        val &= ~(mask);                                                             // 清除该位
        if (bValid)                                                                 // 如果有效
            val |= mask;                                                            // 设置该位
        byteBuffer.putLong(12, val);                                                // 写回有效位图
    }

    /**
     * 检查测量项是否有效
     *
     * @param measureType 测量类型
     * @return true: 有效
     *         false: 无效
     */
    public boolean isMeasureItemValid(@MeasureType.MeasureItemType int measureType) {
        if(ChannelFactory.isMath_FFT_Ch(channel.getChId())){                        // FFT数学通道
            MathChannel mathChannel = (MathChannel)channel;                         // 转换为MathChannel
            switch (measureType){
                case MeasureType.MEASURE_CURSOR_X1:                                 // 光标X1
                    return mathChannel.isCursorX1Valid();                           // 返回X1有效状态
                case MeasureType.MEASURE_CURSOR_X2:                                 // 光标X2
                    return mathChannel.isCursorX2Valid();                           // 返回X2有效状态
            }
        }
        long val = byteBuffer.getLong(12);                                          // 获取有效位图
        return ((val >>> measureType) & 0x01) == 1;                                 // 检查指定位是否为1
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量值访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置测量值
     *
     * @param measureType 测量类型
     * @param val 测量值
     */
    public void setMeasureItemVal(@MeasureType.MeasureItemType int measureType, double val) {
        floatBuffer.put(measureType, (float)val);                                   // 设置测量值
    }

    /**
     * 获取测量值
     * 对于FFT通道和动态通道有特殊处理
     *
     * @param measureType 测量类型
     * @return 测量值
     */
    public float getMeasureItemVal(@MeasureType.MeasureItemType int measureType) {
        if(ChannelFactory.isMath_FFT_Ch(channel.getChId())){                        // FFT数学通道
            MathChannel mathChannel = (MathChannel)channel;                         // 转换为MathChannel
            switch (measureType){
                case MeasureType.MEASURE_CURSOR_X1:                                 // 光标X1
                    return (float) mathChannel.getCursorX1Value();                  // 返回X1值
                case MeasureType.MEASURE_CURSOR_X2:                                 // 光标X2
                    return (float) mathChannel.getCursorX2Value();                  // 返回X2值
            }
        }

        float val = floatBuffer.get(measureType);                                   // 获取测量值
        double tmpVal = 0;                                                          // 临时值
        if(ChannelFactory.isDynamicCh(channel.getChId()) && !isCursorMeasureRange()){ // 动态通道且非光标范围
            Channel ch = (Channel)channel;                                          // 转换为Channel
            switch (measureType){
                case MeasureType.MEASURE_MAX:                                       // 最大值
                    tmpVal = ch.getMaxVal();                                         // 获取通道最大值
                    if(val < tmpVal){                                               // 测量值小于通道最大值
                        val = (float)tmpVal;                                        // 使用通道最大值
                    }
                    break;
                case MeasureType.MEASURE_MIN:                                       // 最小值
                    tmpVal = ch.getMinVal();                                         // 获取通道最小值
                    if(val > tmpVal){                                               // 测量值大于通道最小值
                        val = (float)tmpVal;                                        // 使用通道最小值
                    }
                    break;
                case MeasureType.MEASURE_PK_PK:                                     // 峰峰值
                    tmpVal = (ch.getPKPKVal());                                     // 获取通道峰峰值

                    if(val < tmpVal) {                                              // 测量值小于通道峰峰值
                        val = (float) tmpVal;                                       // 使用通道峰峰值
                    }
                    break;
            }
        }



        return val;                                                                 // 返回测量值
    }

    /**
     * 获取削波状态
     *
     * @return 削波状态
     *         0: 不削波
     *         1: 正削
     *         2: 负削
     *         3: 双削
     *         4: 无波
     */
    public int getClipping(){
        double val = getMeasureItemVal(MeasureType.MEASURE_CLIPPING);               // 获取削波测量值
        return (int)(val + 0.1);                                                    // 返回整数值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 指示位置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取测量指示位置
     * 用于在屏幕上显示测量指示线
     *
     * @param measureType 测量类型
     * @param dir 方向（左/上/右/下）
     * @return 指示位置（像素）
     */
    public int getIndication(int measureType,int dir){

        int idx = measureType - MeasureType.MEASURE_FIRST;                          // 计算索引
        float val = floatBuffer.get(MeasureType.MEASURE_ITEM_MAX + idx * 4 + dir);  // 获取指示位置
        val = Math.round(ScopeBase.changeAccuracy(val * ScopeBase.getToUICoff()));  // 转换为UI坐标
        if(ChannelFactory.isDynamicCh(channel.getChId()) && !isCursorMeasureRange()){ // 动态通道且非光标范围
            Channel ch = (Channel)channel;                                          // 转换为Channel
            double maxVal = getMeasureItemVal(MeasureType.MEASURE_MAX);             // 获取最大值
            double minVal = getMeasureItemVal(MeasureType.MEASURE_MIN);             // 获取最小值
            WaveData dataBuffer = (WaveData) channel.obtain();                      // 获取波形数据
            if (dataBuffer==null){                                                  // 数据为空
                return Math.round(val);                                             // 返回原始值
            }
            double v = ch.getADVerticalPerPix() * dataBuffer.getProbeRate() / ch.getProbeRate(); // 计算电压/像素
            channel.recycle(dataBuffer);                                            // 回收波形数据
            maxVal = ScopeBase.getNewHeight() / 2 - ch.getPosUI() - maxVal / v;     // 计算最大值UI位置
            minVal = ScopeBase.getNewHeight() / 2 - ch.getPosUI() - minVal / v;     // 计算最小值UI位置

            if(Scope.getInstance().isZoom()){                                      // 缩放模式
                maxVal = maxVal * ScopeBase.getZoomHeight()/ScopeBase.getHeight();  // 缩放最大值位置
                minVal = minVal * ScopeBase.getZoomHeight()/ScopeBase.getHeight();  // 缩放最小值位置
            }

            switch (measureType){
                case MeasureType.MEASURE_MAX:                                       // 最大值
                    if(dir == INDICATION_TOP) {                                     // 顶部指示
                        val = (float) maxVal;                                       // 使用最大值位置
                    }
                    break;
                case MeasureType.MEASURE_MIN:                                       // 最小值
                    if(dir == INDICATION_BOTTOM) {                                  // 底部指示
                        val = (float) minVal;                                       // 使用最小值位置
                    }

                    break;
                case MeasureType.MEASURE_PK_PK:                                     // 峰峰值
                    if(dir == INDICATION_TOP) {                                     // 顶部指示
                        val = (float) maxVal;                                       // 使用最大值位置
                    }else if(dir == INDICATION_BOTTOM) {                            // 底部指示
                        val = (float) minVal;                                       // 使用最小值位置
                    }
                    break;
            }
        }
        return Math.round(val);                                                     // 返回指示位置
    }

    /**
     * 设置测量指示位置
     *
     * @param measureType 测量类型
     * @param dir 方向
     * @param val 位置值
     */
    public void setIndication(int measureType,int dir,float val){
        int idx = measureType - MeasureType.MEASURE_FIRST;                          // 计算索引
        floatBuffer.put(MeasureType.MEASURE_ITEM_MAX + idx * 4 + dir,val);          // 设置指示位置
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量计算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 波形X起始位置
     */
    private int waveX = 0;

    /**
     * 执行测量计算
     * 通过JNI调用Native方法进行测量计算
     *
     * @return true: 测量成功
     *         false: 测量失败
     */
    public boolean MeasureCalc() {
        boolean bMeasure = false;                                                   // 初始化返回值
        BaseChannel baseChannel = (BaseChannel)channel;                             // 转换为BaseChannel
        if(baseChannel != null) {                                                   // 通道有效
            WaveData dataBuffer = (WaveData) channel.obtain();                      // 获取波形数据
            if (dataBuffer != null) {                                               // 数据有效
                setChPos((int) Math.round(baseChannel.getPos()));                   // 设置通道位置
                setVRate((float) ((dataBuffer.getVScaleVal()/dataBuffer.getProbeRate())/(baseChannel.getFineScale()*baseChannel.getVScaleIdVal()))); // 设置垂直速率
                abs();                                                              // 计算绝对测量参数
                WaveHeightRate(
                        Scope.getInstance().isZoom()
                                ?(float) ScopeBase.getZoomHeight()/ScopeBase.getHeight()
                                :1.0f
                );                                                                  // 设置高度比率
                waveX = dataBuffer.getStartX();                                     // 保存波形起始位置
                if(ChannelFactory.isRefCh(channel.getChId())){                     // 参考通道
                    RefChannel refChannel = (RefChannel) channel;                   // 转换为RefChannel
                    synchronized (refChannel.surfaceLock){                          // 同步保护
                        int startx = dataBuffer.getStartX();                         // 保存起始位置
                        int endx = dataBuffer.getEndX();                             // 保存结束位置
                        refChannel.calcXOffset(dataBuffer);                         // 计算X偏移
                        bMeasure = MeasureNative(dataBuffer.getByteBuffer(), byteBuffer); // 执行测量
                        dataBuffer.setStartX(startx);                               // 恢复起始位置
                        dataBuffer.setEndX(endx);                                   // 恢复结束位置
                    }
                }else {                                                             // 非参考通道
                    bMeasure = MeasureNative(dataBuffer.getByteBuffer(), byteBuffer); // 执行测量
                }
                channel.recycle(dataBuffer);                                        // 回收波形数据
            }
        }
        return bMeasure;                                                            // 返回测量结果
    }

    /**
     * 计算测量统计
     */
    public void calcStatics(){
        measureStatics.calcStatics();                                               // 调用统计计算
    }

    /**
     * 计算相位测量
     * 计算两个通道之间的相位差
     *
     * @param refMeasure 参考通道的Measure对象
     * @return true: 计算成功
     *         false: 计算失败
     */
    public boolean MeasurePhase(Measure refMeasure) {

        if (this.isMeasureItemValid(MeasureType.MEASURE_FIRST_RISE_EDGE)
                && refMeasure.isMeasureItemValid(MeasureType.MEASURE_FIRST_RISE_EDGE)) { // 两个通道的第一个上升沿都有效
            double val;                                                              // 相位值
            double chFirstFallingEdge, refChFirstFallingEdge;                       // 第一个下降沿
            double chPhase, refChPhase;                                             // 周期
            double refTimePot,timePot;                                              // 时间点

            chFirstFallingEdge = getMeasureItemVal(MeasureType.MEASURE_FIRST_RISE_EDGE); // 获取本通道第一个上升沿
            refChFirstFallingEdge = refMeasure.getMeasureItemVal(MeasureType.MEASURE_FIRST_RISE_EDGE); // 获取参考通道第一个上升沿
            timePot = getMeasureItemVal(MeasureType.MEASURE_TIME_POT);              // 获取本通道时间点
            refTimePot = refMeasure.getMeasureItemVal(MeasureType.MEASURE_TIME_POT); // 获取参考通道时间点

            if(isMeasureItemValid(MeasureType.MEASURE_PERIOD)){                     // 周期有效
                chPhase = getMeasureItemVal(MeasureType.MEASURE_PERIOD);            // 获取周期（秒）
                val = (chFirstFallingEdge-refChFirstFallingEdge)*360.0*timePot/chPhase; // 计算相位差
                while (val > 180) {                                                 // 相位值大于180
                    val -= 360;                                                     // 减去360
                }
                while (val < -180) {                                                // 相位值小于-180
                    val += 360;                                                     // 加上360
                }

                double v = getHorizontalRate();                                     // 获取水平速率
                setIndication(MeasureType.MEASURE_PHASE,INDICATION_LEFT,Math.round(getStartX() + chFirstFallingEdge/v)); // 设置左侧指示
                setIndication(MeasureType.MEASURE_PHASE,INDICATION_RIGHT,Math.round(refMeasure.getStartX() + refChFirstFallingEdge/v)); // 设置右侧指示
                setMeasureItemVal(MeasureType.MEASURE_PHASE, -val);                 // 设置相位值
                return true;                                                        // 返回成功
            }
        }

        return false;                                                               // 返回失败
    }

    /**
     * 获取测量起始X位置
     *
     * @return 起始X位置
     */
    public int getStartX(){
        int begin = getMeasureBegin();                                              // 获取测量开始位置
        if(begin <= waveX){                                                         // 开始位置小于波形起始位置
            begin = waveX;                                                          // 使用波形起始位置
        }
        return begin;                                                               // 返回起始位置
    }

    /**
     * 计算延迟测量
     * 计算两个通道之间的时间延迟
     *
     * @param refMeasure 参考通道的Measure对象
     * @return true: 计算成功
     *         false: 计算失败
     */
    public boolean MeasureDelay(Measure refMeasure) {

        boolean bRet = false;                                                       // 初始化返回值
        double pos1, pos2, timePot, refTimePot, val;                                // 位置和时间变量
        int _selfEdge = getDelaySelfEdge();                                         // 获取自身边沿
        int relateEdge = getDelayRefChEdge();                                       // 获取参考边沿

        boolean selfEdge = false, selfEdgePos = false, refEdge = false, refEdgePos = false; // 边沿类型标志
        switch (_selfEdge) {                                                        // 根据自身边沿类型
            case RL_LAST_RISE_EDGE:
            case RL_FIRST_RISE_EDGE:
                selfEdge = false;//上升沿
                break;
            case RL_FIRST_FALL_EDGE:
            case RL_LAST_FALL_EDGE:
                selfEdge = true;//下降沿
                break;
        }
        switch (relateEdge) {                                                       // 根据参考边沿类型
            case RL_LAST_RISE_EDGE:
            case RL_FIRST_RISE_EDGE:
                refEdge = false;//上升沿
                break;
            case RL_FIRST_FALL_EDGE:
            case RL_LAST_FALL_EDGE:
                refEdge = true;//下降沿
                break;
        }
        switch (_selfEdge) {                                                        // 根据自身边沿位置
            case RL_LAST_RISE_EDGE:
            case RL_LAST_FALL_EDGE:
                selfEdgePos = true;//尾沿
                break;
            case RL_FIRST_RISE_EDGE:
            case RL_FIRST_FALL_EDGE:
                selfEdgePos = false;//首沿
                break;
        }

        switch (relateEdge) {                                                       // 根据参考边沿位置
            case RL_LAST_RISE_EDGE:
            case RL_LAST_FALL_EDGE:
                refEdgePos = true;//尾沿
                break;
            case RL_FIRST_RISE_EDGE:
            case RL_FIRST_FALL_EDGE:
                refEdgePos = false;//首沿
                break;
        }


        //取待测点位置
        if (selfEdge) {                                                             // 下降沿
            if (selfEdgePos)                                                        // 尾沿
                pos1 = getMeasureItemVal(MeasureType.MEASURE_LAST_FALL_EDGE);       // 获取最后下降沿
            else
                pos1 = getMeasureItemVal(MeasureType.MEASURE_FIRST_FALL_EDGE);      // 获取第一个下降沿
        } else {                                                                    // 上升沿
            if (selfEdgePos) pos1 = getMeasureItemVal(MeasureType.MEASURE_LAST_RISE_EDGE); // 获取最后上升沿
            else pos1 = getMeasureItemVal(MeasureType.MEASURE_FIRST_RISE_EDGE);     // 获取第一个上升沿
        }

        //取参考点位置
        if (refEdge) {                                                              // 下降沿
            if (refEdgePos) pos2 = refMeasure.getMeasureItemVal(MeasureType.MEASURE_LAST_FALL_EDGE); // 获取最后下降沿
            else pos2 = refMeasure.getMeasureItemVal(MeasureType.MEASURE_FIRST_FALL_EDGE); // 获取第一个下降沿
        } else {                                                                    // 上升沿
            if (refEdgePos) pos2 = refMeasure.getMeasureItemVal(MeasureType.MEASURE_LAST_RISE_EDGE); // 获取最后上升沿
            else pos2 = refMeasure.getMeasureItemVal(MeasureType.MEASURE_FIRST_RISE_EDGE); // 获取第一个上升沿
        }
        if (pos1 < 0 || pos2 < 0) {                                                 // 位置无效
            bRet = false;                                                           // 返回失败
        } else {
            timePot = getMeasureItemVal(MeasureType.MEASURE_TIME_POT);              // 获取本通道时间点
            refTimePot = refMeasure.getMeasureItemVal(MeasureType.MEASURE_TIME_POT); // 获取参考通道时间点

            val = pos1 * timePot - pos2 * refTimePot;                               // 计算延迟值

            double v = getHorizontalRate();                                         // 获取水平速率

            setIndication(MeasureType.MEASURE_DELAY,INDICATION_LEFT,Math.round(getStartX() + pos1/v)); // 设置左侧指示
            setIndication(MeasureType.MEASURE_DELAY,INDICATION_RIGHT,Math.round(refMeasure.getStartX() + pos2/v)); // 设置右侧指示
            setMeasureItemVal(MeasureType.MEASURE_DELAY, val);                      // 设置延迟值
            bRet = true;                                                            // 返回成功
        }
        return bRet;                                                                // 返回结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Native方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 调用Native测量方法
     *
     * @param chBuffer 通道波形数据缓冲区
     * @param measureItem 测量参数缓冲区
     * @return true: 测量成功
     *         false: 测量失败
     */
    private boolean MeasureNative(ByteBuffer chBuffer, ByteBuffer measureItem) {
        boolean bMeasure = false;                                                   // 初始化返回值
        if (chBuffer.isDirect() && measureItem.isDirect()) {                        // 两个缓冲区都是直接内存
            bMeasure = measure(chBuffer, measureItem);                              // 调用Native方法
        }
        return bMeasure;                                                            // 返回测量结果
    }

    /**
     * Native测量方法
     * 通过JNI调用C++实现的测量算法
     *
     * @param chBuffer 通道波形数据缓冲区
     * @param measureItem 测量参数缓冲区
     * @return true: 测量成功
     *         false: 测量失败
     */
    private static native boolean measure(ByteBuffer chBuffer, ByteBuffer measureItem);
}
