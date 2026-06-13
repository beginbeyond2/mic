package com.micsig.tbook.scope.math;  // 定义包名：示波器数学运算模块

/**
 * 数学运算波形抽象基类 - 定义数学通道运算的核心行为接口
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.math（示波器数学运算模块）</li>
 *   <li>架构层级：业务逻辑层 - 数学运算抽象层</li>
 *   <li>设计模式：模板方法模式 + 策略模式</li>
 *   <li>职责类型：定义数学运算类型、提供档位管理接口</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义数学运算类型常量（双波形/FFT/表达式）</li>
 *   <li>管理数学运算类型状态</li>
 *   <li>定义档位管理抽象接口</li>
 *   <li>定义探头类型管理接口</li>
 * </ul>
 * 
 * <p><b>数学运算类型架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   MathWave - 数学运算波形抽象基类                                         │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   数学运算类型（mathType）                                        │   │
 * │   │                                                                   │   │
 * │   │   MATH_DUALWAVE = 0 ─── 双波形运算                                │   │
 * │   │       ├── 加法运算（CH1 + CH2）                                   │   │
 * │   │       ├── 减法运算（CH1 - CH2）                                   │   │
 * │   │       ├── 乘法运算（CH1 * CH2）                                   │   │
 * │   │       └── 除法运算（CH1 / CH2）                                   │   │
 * │   │                                                                   │   │
 * │   │   MATH_FFTWAVE = 1 ─── FFT频谱分析                                │   │
 * │   │       ├── RMS模式（线性刻度）                                     │   │
 * │   │       └── dB模式（对数刻度）                                      │   │
 * │   │                                                                   │   │
 * │   │   MATH_EXPR = 2 ─── 表达式运算                                    │   │
 * │   │       └── 自定义数学表达式计算                                    │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   继承体系（策略模式）                                             │   │
 * │   │                                                                   │   │
 * │   │               MathWave（抽象类）                                  │   │
 * │   │                     │                                            │   │
 * │   │       ┌─────────────┼─────────────┐                              │   │
 * │   │       │             │             │                              │   │
 * │   │   MathDualWave  MathFFTWave  MathExprWave                        │   │
 * │   │   （双波形运算） （FFT分析）  （表达式运算）                       │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>档位管理说明：</b>
 * <pre>
 * 数学通道的垂直档位管理与模拟通道类似，但有不同的档位表：
 *   ├── 双波形运算：使用VerticalAxisMathDual档位表
 *   ├── FFT运算：使用VerticalAxisMathFft档位表（支持RMS和dB两种模式）
 *   └── 表达式运算：根据运算结果动态调整
 * 
 * 档位微调（Fine Scale）：
 *   ├── 微调系数范围：0.1 ~ 2.5
 *   ├── 实际档位值 = 档位值 * 微调系数
 *   └── 用于精确调整波形显示幅度
 * </pre>
 * 
 * <p><b>探头类型说明：</b>
 * <pre>
 * 探头类型决定垂直轴的单位：
 *   ├── 线性模式（V）：直接显示电压值
 *   └── 对数模式（dB）：显示功率分贝值（仅FFT支持）
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>MathChannel创建数学运算实例时，根据类型创建对应子类</li>
 *   <li>切换数学运算类型时，销毁旧实例创建新实例</li>
 *   <li>调整数学通道档位时，调用档位相关方法</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>被继承：MathDualWave（双波形运算）</li>
 *   <li>被继承：MathFFTWave（FFT运算）</li>
 *   <li>被继承：MathExprWave（表达式运算）</li>
 *   <li>被依赖：MathChannel（数学通道）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-31
 * @see MathDualWave 双波形运算实现
 * @see MathFFTWave FFT运算实现
 * @see MathExprWave 表达式运算实现
 * @see MathChannel 数学通道
 */
public abstract class MathWave {
    
    /** 数学运算类型常量：双波形运算（加、减、乘、除） */
    public static final int MATH_DUALWAVE = 0;  // 双波形运算类型标识
    
    /** 数学运算类型常量：FFT频谱分析 */
    public static final int MATH_FFTWAVE = 1;  // FFT运算类型标识
    
    /** 数学运算类型常量：自定义表达式运算 */
    public static final int MATH_EXPR = 2;  // 表达式运算类型标识
    
    /** 数学运算类型最大值（用于类型有效性检查） */
    public static final int MATH_TYPE_MAX = 3;  // 数学类型数量上限

    /**
     * 检查数学运算类型是否有效
     * 
     * <p>判断给定的数学类型值是否在有效范围内（0~2）。
     * 
     * <p><b>有效范围：</b>
     * <ul>
     *   <li>MATH_DUALWAVE (0): 双波形运算</li>
     *   <li>MATH_FFTWAVE (1): FFT运算</li>
     *   <li>MATH_EXPR (2): 表达式运算</li>
     * </ul>
     * 
     * @param mathType 数学运算类型值
     * @return true表示类型有效，false表示类型无效
     */
    public static boolean isMathTypeVaild(int mathType){
        return mathType >= MATH_DUALWAVE && mathType< MATH_TYPE_MAX;  // 检查类型是否在有效范围内
    }
    
    /** 当前数学运算类型（MATH_DUALWAVE/MATH_FFTWAVE/MATH_EXPR） */
    private int mathType = MATH_EXPR;  // 数学运算类型，默认为表达式运算
    
    /**
     * 构造方法：初始化数学运算波形
     * 
     * <p>设置数学运算类型，由子类调用。
     * 
     * @param mathType 数学运算类型（MATH_DUALWAVE/MATH_FFTWAVE/MATH_EXPR）
     */
    public MathWave(int mathType){
        this.mathType = mathType;  // 保存数学运算类型
    }

    /**
     * 获取数学运算类型
     * 
     * <p>返回当前数学运算的类型标识。
     * 
     * @return 数学运算类型（MATH_DUALWAVE/MATH_FFTWAVE/MATH_EXPR）
     */
    public int getMathType() {
        return mathType;  // 返回数学运算类型
    }

    /**
     * 检查指定通道是否参与采样
     * 
     * <p>判断指定的通道索引是否是当前数学运算的源通道。
     * 用于确定通道采样状态变化时是否需要更新数学运算结果。
     * 
     * <p><b>子类实现：</b>
     * <ul>
     *   <li>MathDualWave: 检查两个源通道</li>
     *   <li>MathFFTWave: 检查单个源通道</li>
     *   <li>MathExprWave: 检查表达式中引用的所有通道</li>
     * </ul>
     * 
     * @param chIdx 通道索引（ChannelFactory.CH1~CH8）
     * @return true表示该通道参与运算，false表示不参与
     */
    public abstract boolean isChInSample(int chIdx);
    
    /**
     * 获取探头类型
     * 
     * <p>返回当前数学运算的探头类型（线性或对数）。
     * 主要用于FFT运算的RMS/dB模式切换。
     * 
     * <p><b>探头类型：</b>
     * <ul>
     *   <li>线性模式（V）：直接显示电压值</li>
     *   <li>对数模式（dB）：显示功率分贝值</li>
     * </ul>
     * 
     * @return 探头类型（参考VerticalAxis.PROBE_TYPE_*常量）
     */
    public abstract int getProbeType();
    
    /**
     * 设置探头类型
     * 
     * <p>设置数学运算的探头类型。
     * 对于FFT运算，切换RMS/dB模式会改变垂直轴刻度。
     * 
     * @param probeType 探头类型（参考VerticalAxis.PROBE_TYPE_*常量）
     */
    public abstract void setProbeType(int probeType);
    
    /**
     * 设置探头描述字符串
     * 
     * <p>设置数学运算探头的描述信息，用于UI显示。
     * 
     * @param probeStr 探头描述字符串（如"1X"、"10X"、"dB"等）
     */
    public abstract void setProbeStr(String probeStr);
    
    /**
     * 获取探头描述字符串
     * 
     * <p>返回当前数学运算探头的描述信息。
     * 
     * @return 探头描述字符串
     */
    public abstract String getProbeStr();
    
    /**
     * 获取垂直档位值
     * 
     * <p>返回当前数学运算的垂直档位值（每格的电压值或dB值）。
     * 实际档位值 = 档位值 * 微调系数。
     * 
     * @return 垂直档位值（单位：V/div 或 dB/div）
     */
    public abstract double getVScaleVal();
    
    /**
     * 生成探头类型
     * 
     * <p>根据当前运算状态自动生成合适的探头类型。
     * 主要用于FFT运算根据信号特性自动选择RMS或dB模式。
     * 
     * @return 生成的探头类型
     */
    public abstract int generateProbeType();

    /**
     * 获取档位值（不含微调系数）
     * 
     * <p>返回当前的档位值，不包含微调系数的影响。
     * 
     * @return 档位值（单位：V/div 或 dB/div）
     */
    public abstract double getVScaleIdVal();

    /**
     * 获取档位ID
     * 
     * <p>返回当前档位在档位表中的索引。
     * 不同的数学运算类型使用不同的档位表。
     * 
     * @return 档位ID（索引）
     */
    public abstract int getVScaleId();

    /**
     * 获取最大档位ID
     * 
     * <p>返回档位表中的最大索引值。
     * 用于档位调整时的边界检查。
     * 
     * @return 最大档位ID
     */
    public abstract int getVScaleIdMax();

    /**
     * 获取最小档位ID
     * 
     * <p>返回档位表中的最小索引值。
     * 用于档位调整时的边界检查。
     * 
     * @return 最小档位ID
     */
    public abstract int getVScaleIdMin();
    
    /**
     * 获取微调系数
     * 
     * <p>返回当前的档位微调系数。
     * 微调系数用于在固定档位之间进行精细调整。
     * 
     * <p><b>微调范围：</b> 0.1 ~ 2.5
     * 
     * @return 微调系数
     */
    public abstract double getFineScale();
    
    /**
     * 设置档位ID
     * 
     * <p>根据档位表索引设置档位值。
     * 设置后会重置微调系数为1.0。
     * 
     * @param scaleId 档位ID（索引）
     */
    public abstract void setVScaleId(int scaleId);
    
    /**
     * 设置档位值
     * 
     * <p>直接设置档位值，系统会自动查找最接近的档位ID，
     * 并计算相应的微调系数。
     * 
     * @param scaleIdVal 档位值（单位：V/div 或 dB/div）
     */
    public abstract void setVScaleVal(double scaleIdVal);
    
    /**
     * 设置微调系数
     * 
     * <p>设置档位的微调系数，用于精细调整波形显示幅度。
     * 
     * <p><b>微调范围：</b> 0.1 ~ 2.5
     * 
     * @param fineScale 微调系数
     */
    public abstract void setFineScale(double fineScale);
    
    /**
     * 根据档位ID获取档位值
     * 
     * <p>查询档位表中指定ID对应的档位值。
     * 
     * @param scaleId 档位ID（索引）
     * @return 档位值（单位：V/div 或 dB/div）
     */
    public abstract double getVScaleIdVal(int scaleId);
    
    /**
     * 根据档位值获取档位ID
     * 
     * <p>在档位表中查找最接近指定值的档位ID。
     * 用于自动档位调整时确定合适的档位。
     * 
     * @param scaleVal 档位值（单位：V/div 或 dB/div）
     * @return 档位ID（索引）
     */
    public abstract int getVScaleId(double scaleVal);

}
