package com.micsig.tbook.scope.vertical;  // 定义包名：示波器垂直轴管理模块

/**
 * 数学双波形运算垂直轴管理类 - 管理数学通道双波形运算的垂直档位
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.vertical（示波器垂直轴管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 数学运算垂直轴管理</li>
 *   <li>设计模式：继承模式 + 查表法</li>
 *   <li>职责类型：定义数学双波形档位、提供档位转换</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义数学双波形运算的垂直档位常量（1fV ~ 500TV）</li>
 *   <li>提供档位ID与档位值的双向转换</li>
 *   <li>支持极宽范围的档位（从飞伏到太伏）</li>
 *   <li>为MathDualWave提供档位管理支持</li>
 * </ul>
 * 
 * <p><b>数学双波形档位架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   VerticalAxisMathDual - 数学双波形垂直轴                                 │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   档位范围（极宽范围：1fV ~ 500TV）                                │   │
 * │   │                                                                   │   │
 * │   │   单位    档位范围              索引范围                          │   │
 * │   │   ─────────────────────────────────────────────────              │   │
 * │   │   fV     1fV ~ 500fV           0 ~ 8                              │   │
 * │   │   pV     1pV ~ 500pV           9 ~ 17                             │   │
 * │   │   nV     1nV ~ 500nV           18 ~ 26                            │   │
 * │   │   μV     1μV ~ 500μV           27 ~ 35                            │   │
 * │   │   mV     1mV ~ 500mV           36 ~ 44                            │   │
 * │   │   V      1V ~ 500V             45 ~ 53                            │   │
 * │   │   kV     1kV ~ 500kV           54 ~ 62                            │   │
 * │   │   MV     1MV ~ 500MV           63 ~ 71                            │   │
 * │   │   GV     1GV ~ 500GV           72 ~ 80                            │   │
 * │   │   TV     1TV ~ 500TV           81 ~ 89                            │   │
 * │   │                                                                   │   │
 * │   │   总计：90个档位，覆盖10^27的动态范围                              │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   双波形运算示例                                                  │   │
 * │   │                                                                   │   │
 * │   │   CH1 = 5V/div, CH2 = 2V/div                                     │   │
 * │   │                                                                   │   │
 * │   │   加法（CH1 + CH2）：结果可能达到7V/div                          │   │
 * │   │   减法（CH1 - CH2）：结果可能为3V/div                            │   │
 * │   │   乘法（CH1 * CH2）：结果为10V²/div（需要特殊单位）              │   │
 * │   │   除法（CH1 / CH2）：结果为2.5（无量纲）                         │   │
 * │   │                                                                   │   │
 * │   │   因此数学运算需要极宽的档位范围来适应各种运算结果                │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   VerticalAxis（模拟通道垂直轴）
 *       │
 *       └── VerticalAxisMathDual（数学双波形垂直轴）
 *               │
 *               ├── 重写：setScaleId()、getScaleId()、getScaleVal()等
 *               └── 扩展：90个数学运算专用档位常量
 * </pre>
 * 
 * <p><b>与模拟通道档位的区别：</b>
 * <pre>
 *   模拟通道（VerticalAxis）：
 *   ├── 档位范围：250μV ~ 10V（15个档位）
 *   └── 限制：受硬件ADC和前端电路限制
 * 
 *   数学双波形（VerticalAxisMathDual）：
 *   ├── 档位范围：1fV ~ 500TV（90个档位）
 *   └── 原因：数学运算结果可能超出硬件范围
 *       ├── 加法：结果可能大于任一源通道
 *       ├── 减法：结果可能很小（接近零）
 *       ├── 乘法：结果单位变化（V²）
 *       └── 除法：结果无量纲
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>MathDualWave类管理数学双波形运算的垂直档位</li>
 *   <li>MathChannel切换到双波形运算模式时使用此类</li>
 *   <li>自动档位调整时根据运算结果选择合适档位</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：VerticalAxis（垂直轴基类）</li>
 *   <li>被依赖：MathDualWave（双波形运算实现）</li>
 *   <li>被依赖：MathChannel（数学通道）</li>
 * </ul>
 * 
 * @author Administrator
 * @version 1.0
 * @since 2018/9/7
 * @see VerticalAxis 垂直轴基类
 * @see MathDualWave 双波形运算实现
 * @see MathChannel 数学通道
 */
public class VerticalAxisMathDual extends VerticalAxis{

    /** 档位常量：无效档位 */
    public static final int DANG_DUAL_NONE    = -1;  // 无效档位标识
    
    /** 档位常量：1飞伏/div (1fV = 10⁻¹⁵V) */
    public static final int DANG_DUAL_1FV     =  0;  // 1fV档位（索引0）
    
    /** 档位常量：2飞伏/div */
    public static final int DANG_DUAL_2FV     =  1;  // 2fV档位（索引1）
    
    /** 档位常量：5飞伏/div */
    public static final int DANG_DUAL_5FV     =  2;  // 5fV档位（索引2）
    
    /** 档位常量：10飞伏/div */
    public static final int DANG_DUAL_10FV    =  3;  // 10fV档位（索引3）
    
    /** 档位常量：20飞伏/div */
    public static final int DANG_DUAL_20FV    =  4;  // 20fV档位（索引4）
    
    /** 档位常量：50飞伏/div */
    public static final int DANG_DUAL_50FV    =  5;  // 50fV档位（索引5）
    
    /** 档位常量：100飞伏/div */
    public static final int DANG_DUAL_100FV   =  6;  // 100fV档位（索引6）
    
    /** 档位常量：200飞伏/div */
    public static final int DANG_DUAL_200FV   =  7;  // 200fV档位（索引7）
    
    /** 档位常量：500飞伏/div */
    public static final int DANG_DUAL_500FV   =  8;  // 500fV档位（索引8）
    
    /** 档位常量：1皮伏/div (1pV = 10⁻¹²V) */
    public static final int DANG_DUAL_1PV     =  9;  // 1pV档位（索引9）
    
    /** 档位常量：2皮伏/div */
    public static final int DANG_DUAL_2PV     =  10;  // 2pV档位（索引10）
    
    /** 档位常量：5皮伏/div */
    public static final int DANG_DUAL_5PV     =  11;  // 5pV档位（索引11）
    
    /** 档位常量：10皮伏/div */
    public static final int DANG_DUAL_10PV    =  12;  // 10pV档位（索引12）
    
    /** 档位常量：20皮伏/div */
    public static final int DANG_DUAL_20PV    =  13;  // 20pV档位（索引13）
    
    /** 档位常量：50皮伏/div */
    public static final int DANG_DUAL_50PV    =  14;  // 50pV档位（索引14）
    
    /** 档位常量：100皮伏/div */
    public static final int DANG_DUAL_100PV   =  15;  // 100pV档位（索引15）
    
    /** 档位常量：200皮伏/div */
    public static final int DANG_DUAL_200PV   =  16;  // 200pV档位（索引16）
    
    /** 档位常量：500皮伏/div */
    public static final int DANG_DUAL_500PV   =  17;  // 500pV档位（索引17）
    
    /** 档位常量：1纳伏/div (1nV = 10⁻⁹V) */
    public static final int DANG_DUAL_1NV     =  18;  // 1nV档位（索引18）
    
    /** 档位常量：2纳伏/div */
    public static final int DANG_DUAL_2NV     = 19;  // 2nV档位（索引19）
    
    /** 档位常量：5纳伏/div */
    public static final int DANG_DUAL_5NV     = 20;  // 5nV档位（索引20）
    
    /** 档位常量：10纳伏/div */
    public static final int DANG_DUAL_10NV    = 21;  // 10nV档位（索引21）
    
    /** 档位常量：20纳伏/div */
    public static final int DANG_DUAL_20NV    = 22;  // 20nV档位（索引22）
    
    /** 档位常量：50纳伏/div */
    public static final int DANG_DUAL_50NV    = 23;  // 50nV档位（索引23）
    
    /** 档位常量：100纳伏/div */
    public static final int DANG_DUAL_100NV   = 24;  // 100nV档位（索引24）
    
    /** 档位常量：200纳伏/div */
    public static final int DANG_DUAL_200NV   = 25;  // 200nV档位（索引25）
    
    /** 档位常量：500纳伏/div */
    public static final int DANG_DUAL_500NV   = 26;  // 500nV档位（索引26）
    
    /** 档位常量：1微伏/div (1μV = 10⁻⁶V) */
    public static final int DANG_DUAL_1UV     = 27;  // 1μV档位（索引27）
    
    /** 档位常量：2微伏/div */
    public static final int DANG_DUAL_2UV     = 28;  // 2μV档位（索引28）
    
    /** 档位常量：5微伏/div */
    public static final int DANG_DUAL_5UV     = 29;  // 5μV档位（索引29）
    
    /** 档位常量：10微伏/div */
    public static final int DANG_DUAL_10UV    = 30;  // 10μV档位（索引30）
    
    /** 档位常量：20微伏/div */
    public static final int DANG_DUAL_20UV    = 31;  // 20μV档位（索引31）
    
    /** 档位常量：50微伏/div */
    public static final int DANG_DUAL_50UV    = 32;  // 50μV档位（索引32）
    
    /** 档位常量：100微伏/div */
    public static final int DANG_DUAL_100UV   = 33;  // 100μV档位（索引33）
    
    /** 档位常量：200微伏/div */
    public static final int DANG_DUAL_200UV   = 34;  // 200μV档位（索引34）
    
    /** 档位常量：500微伏/div */
    public static final int DANG_DUAL_500UV   = 35;  // 500μV档位（索引35）
    
    /** 档位常量：1毫伏/div (1mV = 10⁻³V) */
    public static final int DANG_DUAL_1mV     = 36;  // 1mV档位（索引36）
    
    /** 档位常量：2毫伏/div */
    public static final int DANG_DUAL_2mV     = 37;  // 2mV档位（索引37）
    
    /** 档位常量：5毫伏/div */
    public static final int DANG_DUAL_5mV     = 38;  // 5mV档位（索引38）
    
    /** 档位常量：10毫伏/div */
    public static final int DANG_DUAL_10mV    = 39;  // 10mV档位（索引39）
    
    /** 档位常量：20毫伏/div */
    public static final int DANG_DUAL_20mV    = 40;  // 20mV档位（索引40）
    
    /** 档位常量：50毫伏/div */
    public static final int DANG_DUAL_50mV    = 41;  // 50mV档位（索引41）
    
    /** 档位常量：100毫伏/div */
    public static final int DANG_DUAL_100mV   = 42;  // 100mV档位（索引42）
    
    /** 档位常量：200毫伏/div */
    public static final int DANG_DUAL_200mV   = 43;  // 200mV档位（索引43）
    
    /** 档位常量：500毫伏/div */
    public static final int DANG_DUAL_500mV   = 44;  // 500mV档位（索引44）
    
    /** 档位常量：1伏/div (1V) */
    public static final int DANG_DUAL_1V      = 45;  // 1V档位（索引45）
    
    /** 档位常量：2伏/div */
    public static final int DANG_DUAL_2V      = 46;  // 2V档位（索引46）
    
    /** 档位常量：5伏/div */
    public static final int DANG_DUAL_5V      = 47;  // 5V档位（索引47）
    
    /** 档位常量：10伏/div */
    public static final int DANG_DUAL_10V     = 48;  // 10V档位（索引48）
    
    /** 档位常量：20伏/div */
    public static final int DANG_DUAL_20V     = 49;  // 20V档位（索引49）
    
    /** 档位常量：50伏/div */
    public static final int DANG_DUAL_50V     = 50;  // 50V档位（索引50）
    
    /** 档位常量：100伏/div */
    public static final int DANG_DUAL_100V    = 51;  // 100V档位（索引51）
    
    /** 档位常量：200伏/div */
    public static final int DANG_DUAL_200V    = 52;  // 200V档位（索引52）
    
    /** 档位常量：500伏/div */
    public static final int DANG_DUAL_500V    = 53;  // 500V档位（索引53）
    
    /** 档位常量：1千伏/div (1kV = 10³V) */
    public static final int DANG_DUAL_1KV     = 54;  // 1kV档位（索引54）
    
    /** 档位常量：2千伏/div */
    public static final int DANG_DUAL_2KV     = 55;  // 2kV档位（索引55）
    
    /** 档位常量：5千伏/div */
    public static final int DANG_DUAL_5KV     = 56;  // 5kV档位（索引56）
    
    /** 档位常量：10千伏/div */
    public static final int DANG_DUAL_10KV    = 57;  // 10kV档位（索引57）
    
    /** 档位常量：20千伏/div */
    public static final int DANG_DUAL_20KV    = 58;  // 20kV档位（索引58）
    
    /** 档位常量：50千伏/div */
    public static final int DANG_DUAL_50KV    = 59;  // 50kV档位（索引59）
    
    /** 档位常量：100千伏/div */
    public static final int DANG_DUAL_100KV   = 60;  // 100kV档位（索引60）
    
    /** 档位常量：200千伏/div */
    public static final int DANG_DUAL_200KV   = 61;  // 200kV档位（索引61）
    
    /** 档位常量：500千伏/div */
    public static final int DANG_DUAL_500KV   = 62;  // 500kV档位（索引62）
    
    /** 档位常量：1兆伏/div (1MV = 10⁶V) */
    public static final int DANG_DUAL_1MV     = 63;  // 1MV档位（索引63）
    
    /** 档位常量：2兆伏/div */
    public static final int DANG_DUAL_2MV     = 64;  // 2MV档位（索引64）
    
    /** 档位常量：5兆伏/div */
    public static final int DANG_DUAL_5MV     = 65;  // 5MV档位（索引65）
    
    /** 档位常量：10兆伏/div */
    public static final int DANG_DUAL_10MV    = 66;  // 10MV档位（索引66）
    
    /** 档位常量：20兆伏/div */
    public static final int DANG_DUAL_20MV    = 67;  // 20MV档位（索引67）
    
    /** 档位常量：50兆伏/div */
    public static final int DANG_DUAL_50MV    = 68;  // 50MV档位（索引68）
    
    /** 档位常量：100兆伏/div */
    public static final int DANG_DUAL_100MV   = 69;  // 100MV档位（索引69）
    
    /** 档位常量：200兆伏/div */
    public static final int DANG_DUAL_200MV   = 70;  // 200MV档位（索引70）
    
    /** 档位常量：500兆伏/div */
    public static final int DANG_DUAL_500MV   = 71;  // 500MV档位（索引71）
    
    /** 档位常量：1吉伏/div (1GV = 10⁹V) */
    public static final int DANG_DUAL_1GV     = 72;  // 1GV档位（索引72）
    
    /** 档位常量：2吉伏/div */
    public static final int DANG_DUAL_2GV     = 73;  // 2GV档位（索引73）
    
    /** 档位常量：5吉伏/div */
    public static final int DANG_DUAL_5GV     = 74;  // 5GV档位（索引74）
    
    /** 档位常量：10吉伏/div */
    public static final int DANG_DUAL_10GV    = 75;  // 10GV档位（索引75）
    
    /** 档位常量：20吉伏/div */
    public static final int DANG_DUAL_20GV    = 76;  // 20GV档位（索引76）
    
    /** 档位常量：50吉伏/div */
    public static final int DANG_DUAL_50GV    = 77;  // 50GV档位（索引77）
    
    /** 档位常量：100吉伏/div */
    public static final int DANG_DUAL_100GV   = 78;  // 100GV档位（索引78）
    
    /** 档位常量：200吉伏/div */
    public static final int DANG_DUAL_200GV   = 79;  // 200GV档位（索引79）
    
    /** 档位常量：500吉伏/div */
    public static final int DANG_DUAL_500GV   = 80;  // 500GV档位（索引80）
    
    /** 档位常量：1太伏/div (1TV = 10¹²V) */
    public static final int DANG_DUAL_1TV     = 81;  // 1TV档位（索引81）
    
    /** 档位常量：2太伏/div */
    public static final int DANG_DUAL_2TV     = 82;  // 2TV档位（索引82）
    
    /** 档位常量：5太伏/div */
    public static final int DANG_DUAL_5TV     = 83;  // 5TV档位（索引83）
    
    /** 档位常量：10太伏/div */
    public static final int DANG_DUAL_10TV    = 84;  // 10TV档位（索引84）
    
    /** 档位常量：20太伏/div */
    public static final int DANG_DUAL_20TV    = 85;  // 20TV档位（索引85）
    
    /** 档位常量：50太伏/div */
    public static final int DANG_DUAL_50TV    = 86;  // 50TV档位（索引86）
    
    /** 档位常量：100太伏/div */
    public static final int DANG_DUAL_100TV   = 87;  // 100TV档位（索引87）
    
    /** 档位常量：200太伏/div */
    public static final int DANG_DUAL_200TV   = 88;  // 200TV档位（索引88）
    
    /** 档位常量：500太伏/div - 最大档位 */
    public static final int DANG_DUAL_500TV   = 89;  // 500TV档位（索引89）
    
    /** 档位常量：最小档位索引 */
    public static final int DANG_DUAL_MIN = DANG_DUAL_1FV;  // 最小档位：1fV
    
    /** 档位常量：最大档位索引 */
    public static final int DANG_DUAL_MAX = DANG_DUAL_500TV;  // 最大档位：500TV
    
    /** 档位常量：档位总数 */
    public static final int DANG_DUAL_CNT = DANG_DUAL_MAX-DANG_DUAL_MIN+1;  // 档位总数：90个


    /**
     * 档位值数组：存储所有档位对应的电压值
     * 
     * <p>数组索引与档位ID一一对应，使用查表法快速获取档位值。
     * 按单位分组：fV(0-8)、pV(9-17)、nV(18-26)、μV(27-35)、
     * mV(36-44)、V(45-53)、kV(54-62)、MV(63-71)、GV(72-80)、TV(81-89)
     */
    private static double scaleArray[] = {
            1e-15, 2e-15,  5e-15,  10e-15, 20e-15, 50e-15, 100e-15,200e-15,500e-15,  // fV: 飞伏 (10⁻¹⁵V)
            1e-12, 2e-12,  5e-12,  10e-12, 20e-12, 50e-12, 100e-12,200e-12,500e-12,  // pV: 皮伏 (10⁻¹²V)
            1e-9,  2e-9,   5e-9,   10e-9,  20e-9,  50e-9,  100e-9, 200e-9, 500e-9,   // nV: 纳伏 (10⁻⁹V)
            1e-6,  2e-6,   5e-6,   10e-6,  20e-6,  50e-6,  100e-6, 200e-6, 500e-6,   // μV: 微伏 (10⁻⁶V)
            1e-3,  2e-3,   5e-3,   10e-3,  20e-3,  50e-3,  100e-3, 200e-3, 500e-3,   // mV: 毫伏 (10⁻³V)
            1,     2,      5,      10,     20,     50,     100,    200,    500,      // V: 伏
            1e3,   2e3,    5e3,    10e3,   20e3,   50e3,   100e3,  200e3,  500e3,    // kV: 千伏 (10³V)
            1e6,   2e6,    5e6,    10e6,   20e6,   50e6,   100e6,  200e6,  500e6,    // MV: 兆伏 (10⁶V)
            1e9,   2e9,    5e9,    10e9,   20e9,   50e9,   100e9,  200e9,  500e9,    // GV: 吉伏 (10⁹V)
            1e12,  2e12,   5e12,   10e12,  20e12,  50e12,  100e12, 200e12, 500e12,   // TV: 太伏 (10¹²V)
    };
    
    /**
     * 检查档位ID是否有效
     * 
     * <p>检查给定的档位ID是否在有效范围内（DANG_DUAL_MIN ~ DANG_DUAL_MAX）。
     * 
     * @param scaleId 档位ID
     * @return true表示有效，false表示无效
     */
    public static boolean isValidScaleId(int scaleId){
        return (scaleId >= DANG_DUAL_MIN && scaleId <= DANG_DUAL_MAX);  // 检查是否在有效范围内
    }

    /** 当前档位ID */
    private int scaleId = DANG_DUAL_1V;  // 档位ID，默认1V/div
    
    /** 微调系数（范围：0.1 ~ 2.5） */
    private double fineScale = 1.0;  // 微调系数，默认1.0

    /**
     * 默认构造方法
     * 
     * <p>创建VerticalAxisMathDual实例，使用默认值：
     * <ul>
     *   <li>档位ID：DANG_DUAL_1V（1V/div）</li>
     *   <li>微调系数：1.0</li>
     * </ul>
     */
    public VerticalAxisMathDual(){}

    /**
     * 带参数的构造方法
     * 
     * <p>创建VerticalAxisMathDual实例并指定初始参数。
     * 
     * @param scaleId 档位ID
     * @param fine 微调系数
     */
    public VerticalAxisMathDual(int scaleId,float fine){
        this.scaleId = scaleId;  // 设置档位ID
        this.fineScale = fine;  // 设置微调系数
    }

    /**
     * 设置档位ID
     * 
     * <p>设置当前档位ID，会进行有效性检查。
     * 
     * @param scaleId 档位ID
     */
    @Override
    public void setScaleId(int scaleId){
        if(isValidScaleId(scaleId))  // 检查档位ID是否有效
            this.scaleId = scaleId;  // 设置档位ID
    }
    
    /**
     * 设置微调系数
     * 
     * <p>设置档位的微调系数，用于在固定档位之间进行精细调整。
     * 
     * @param fine 微调系数
     */
    @Override
    public void setFineScale(double fine) {
        fineScale = fine;  // 设置微调系数
    }
    
    /**
     * 获取档位ID
     * 
     * @return 当前档位ID
     */
    @Override
    public int getScaleId(){
        return scaleId;  // 返回档位ID
    }
    
    /**
     * 获取微调系数
     * 
     * @return 微调系数
     */
    @Override
    public double getFineScale() {
        return fineScale;  // 返回微调系数
    }
    
    /**
     * 获取实际档位值
     * 
     * <p>计算实际档位值 = 档位值 * 微调系数。
     * 注意：数学双波形不乘以探头倍数。
     * 
     * @return 实际档位值（单位：V/div）
     */
    @Override
    public double getScaleVal(){
        return getScaleIdVal() * getFineScale();  // 返回实际档位值（不含探头倍数）
    }
    
    /**
     * 根据档位ID获取档位值（静态方法）
     * 
     * <p>使用查表法从scaleArray数组获取档位值。
     * 主要用于数学双波形的档位扩展和查询。
     * 
     * <p><b>查表法优势：</b>
     * <ul>
     *   <li>时间复杂度O(1)，性能高</li>
     *   <li>避免大量switch-case分支</li>
     *   <li>易于维护和扩展</li>
     * </ul>
     * 
     * @param scaleId 档位ID
     * @return 档位值（单位：V/div）
     */
    public static double getScaleIdValById(int scaleId){
        if (scaleId < 0 || scaleId > scaleArray.length - 1) scaleId = 0;  // 边界检查，无效ID返回最小档位
        return scaleArray[scaleId];  // 从数组中获取档位值
    }
    
    /**
     * 根据档位值获取档位ID（静态方法）
     * 
     * <p>在档位表中查找最接近给定值的档位ID。
     * 用于自动档位调整时确定合适的档位。
     * 
     * <p><b>查找算法：</b>
     * <ol>
     *   <li>从最小档位开始遍历</li>
     *   <li>找到第一个大于目标值的档位</li>
     *   <li>返回该档位ID</li>
     * </ol>
     * 
     * @param scaleIdVal 档位值
     * @return 档位ID
     */
    public static int getScaleIdByValue(double scaleIdVal){
        int ret = DANG_DUAL_MAX;  // 默认返回最大档位
        for(int i=DANG_DUAL_MIN; i<DANG_DUAL_CNT; i++) {  // 遍历档位表
            if(scaleIdVal < (scaleArray[i]+scaleArray[i]*1e-3)) {  // 检查是否小于当前档位（含0.1%容差）
                ret = i; break;  // 找到后退出循环
            }
        }
        return ret;  // 返回档位ID
    }
    
    /**
     * 根据档位值获取档位ID（实例方法）
     * 
     * <p>调用静态方法getScaleIdByValue。
     * 
     * @param _scale 档位值
     * @return 档位ID
     */
    public int getScaleVal(double _scale){
        return getScaleIdByValue(_scale);  // 调用静态方法
    }
    
    /**
     * 获取当前档位值
     * 
     * <p>返回当前档位ID对应的档位值。
     * 
     * @return 档位值（单位：V/div）
     */
    @Override
    public double getScaleIdVal(){
        return getScaleIdVal(scaleId);  // 返回当前档位值
    }
    
    /**
     * 根据档位ID获取档位值（实例方法）
     * 
     * <p>调用静态方法getScaleIdValById。
     * 
     * @param id 档位ID
     * @return 档位值（单位：V/div）
     */
    @Override
    public double getScaleIdVal(int id) {
        return getScaleIdValById(id);  // 调用静态方法
    }

    /**
     * 检查档位值是否与档位ID匹配
     * 
     * <p>判断给定的档位值是否在档位ID对应值的±10%范围内。
     * 
     * @param scaleId 档位ID
     * @param scaleIdVal 档位值
     * @return true表示匹配，false表示不匹配
     */
    private boolean isValidScaleId(int scaleId, double scaleIdVal){
        double val = getScaleIdVal(scaleId);  // 获取档位ID对应的值
        return scaleIdVal > (val - val / 10) && scaleIdVal < (val + val / 10);  // 检查是否在±10%范围内
    }

    /**
     * 查找最接近的档位ID
     * 
     * <p>在两个相邻档位之间查找最接近给定值的档位ID。
     * 
     * @param scaleId 当前档位ID
     * @param scaleIdVal 目标档位值
     * @return 最接近的档位ID，无法确定返回-1
     */
    private int findNearScale(int scaleId, double scaleIdVal) {
        double valLeft = getScaleIdVal(scaleId);  // 获取当前档位值
        double valRight = getScaleIdVal(scaleId + 1);  // 获取下一档位值
        if (valLeft <= scaleIdVal && valRight >= scaleIdVal && Math.abs(valLeft - scaleIdVal) <= Math.abs(valRight - scaleIdVal)) {  // 目标值更接近当前档位
            return scaleId;  // 返回当前档位ID
        } else if (valLeft <= scaleIdVal && valRight >= scaleIdVal && Math.abs(valLeft - scaleIdVal) >= Math.abs(valRight - scaleIdVal)) {  // 目标值更接近下一档位
            return scaleId + 1;  // 返回下一档位ID
        } else {
            return -1;  // 无法确定
        }
    }

    /**
     * 根据档位值获取档位ID
     * 
     * <p>在档位表中查找与给定档位值匹配的档位ID。
     * 首先尝试在相邻档位之间查找最接近的，然后检查是否精确匹配。
     * 
     * @param scaleIdVal 档位值
     * @return 档位ID，未找到返回DANG_DUAL_MIN
     */
    @Override
    public int getScaleId(double scaleIdVal) {
        for (int i = DANG_DUAL_MIN; i <= DANG_DUAL_CNT; i++) {  // 遍历档位表
            if (i < DANG_DUAL_CNT && findNearScale(i, scaleIdVal) != -1) {  // 检查是否在两个档位之间
                return findNearScale(i, scaleIdVal);  // 返回最接近的档位ID
            } else if (isValidScaleId(i, scaleIdVal)) {  // 检查是否精确匹配
                return i;  // 返回匹配的档位ID
            }
        }
        return DANG_DUAL_MIN;  // 未找到返回最小档位
    }

}
