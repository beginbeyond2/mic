package com.micsig.tbook.scope.math;


import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                MathNative - 数学运算JNI Native方法类                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Math模块的JNI Native方法类，位于math包下，                                 ║
 * ║   提供示波器数学运算的底层C/C++实现接口。                                      ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 提供双通道数学运算（加、减、乘、除）的Native方法                         ║
 * ║   2. 提供FFT频谱分析的Native方法                                             ║
 * ║   3. 提供数学表达式解析和计算的Native方法                                     ║
 * ║   4. 提供数据统计（求和、平均值、最大值、最小值）的Native方法                 ║
 * ║   5. 提供ByteBuffer与基本类型数组的转换方法                                   ║
 * ║                                                                              ║
 * ║ 【运算类型】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        运算类型说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【双通道运算】                                                      │ ║
 * ║   │   MATH_ADD (0)   加法运算：CH1 + CH2                                 │ ║
 * ║   │   MATH_SUB (1)   减法运算：CH1 - CH2                                 │ ║
 * ║   │   MATH_MUL (2)   乘法运算：CH1 × CH2                                 │ ║
 * ║   │   MATH_DIV (3)   除法运算：CH1 ÷ CH2                                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   【FFT运算】                                                         │ ║
 * ║   │   MATH_FFT (4)   快速傅里叶变换                                       │ ║
 * ║   │                                                                      │ ║
 * ║   │   【表达式运算】                                                      │ ║
 * ║   │   自定义数学表达式解析和计算                                          │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【JNI调用流程】                                                              ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ Java方法    │───▶│ JNI层       │───▶│ C/C++实现   │                   ║
 * ║   │ MathNative  │    │ JNI接口     │    │ 底层算法    │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【数据传递】                                                                 ║
 * ║   - 使用Direct ByteBuffer进行高效数据传递                                   ║
 * ║   - 避免数据拷贝，提高性能                                                  ║
 * ║   - 支持大数据量的实时处理                                                  ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 实时数学运算：通道加减乘除、FFT变换                                    ║
 * ║   2. 自定义表达式：用户输入的数学表达式计算                                 ║
 * ║   3. 数据统计：波形数据的统计分析                                          ║
 * ║   4. 性能优化：使用Native代码提高计算效率                                   ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - Native方法需要保证线程安全                                              ║
 * ║   - ByteBuffer操作需要同步保护                                              ║
 * ║                                                                              ║
 * ║ 【性能考虑】                                                                 ║
 * ║   - 使用Direct ByteBuffer避免内存拷贝                                       ║
 * ║   - Native层使用SIMD指令优化                                                ║
 * ║   - 支持多线程并行计算                                                      ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - MathDualWave: 双通道数学运算类                                         ║
 * ║   - MathFFTWave: FFT频谱分析类                                             ║
 * ║   - MathExprWave: 数学表达式类                                             ║
 * ║   - MathExprError: 表达式错误类                                            ║
 * ║   - ByteBuffer: 数据传递缓冲区                                              ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 数学运算JNI Native方法类
 * 提供示波器数学运算的底层C/C++实现接口
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>双通道运算：加、减、乘、除</li>
 *   <li>FFT变换：快速傅里叶变换</li>
 *   <li>表达式计算：自定义数学表达式解析和计算</li>
 *   <li>数据统计：求和、平均值、最大值、最小值</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 执行双通道加法运算
 * boolean result = MathNative.CalcDual(MathNative.MATH_ADD, dst, src1, src2);
 *
 * // 执行FFT变换
 * boolean fftResult = MathNative.CalcFFT(chIdx, fftType, fftWindow, dst, src);
 *
 * // 计算数据总和
 * long sum = MathNative.calcSum(buffer);
 * </pre>
 *
 * @see MathDualWave
 * @see MathFFTWave
 * @see MathExprWave
 * @see MathExprError
 */
public class MathNative {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 运算类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 数学运算类型注解
     * 用于限制参数只能是定义的运算类型常量
     */
    @IntDef({MATH_ADD,MATH_SUB,MATH_MUL,MATH_DIV})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Math_Operation {}

    /** 加法运算：CH1 + CH2 */
    public static final int MATH_ADD = 0;

    /** 减法运算：CH1 - CH2 */
    public static final int MATH_SUB = 1;

    /** 乘法运算：CH1 × CH2 */
    public static final int MATH_MUL = 2;

    /** 除法运算：CH1 ÷ CH2 */
    public static final int MATH_DIV = 3;

    /** FFT运算：快速傅里叶变换 */
    private static final int MATH_FFT = 4;

    // ═══════════════════════════════════════════════════════════════════════════════
    // FFT结果访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取FFT的DC值（直流分量）
     *
     * @param chIdx 通道索引
     * @return DC值
     */
    public static double getFFTDCVal(int chIdx){
        return fftDcVal(chIdx);                                                      // 调用Native方法获取DC值
    }

    /**
     * 获取FFT的最大值
     *
     * @param chIdx 通道索引
     * @return 最大值
     */
    public static double getFFTMaxVal(int chIdx){
        return fftMaxVal(chIdx);                                                     // 调用Native方法获取最大值
    }

    /**
     * 获取FFT最大值对应的频率索引
     *
     * @param chIdx 通道索引
     * @return 频率索引
     */
    public static int getFFTMaxIdx(int chIdx){
        return fftMaxValIdx(chIdx);                                                  // 调用Native方法获取最大值索引
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 垂直轴设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置垂直轴电压值
     *
     * @param chIdx 通道索引
     * @param vol 电压值
     */
    public static void setVAd(int chIdx,double vol) {csetVAd(chIdx,vol);}           // 调用Native方法设置电压值

    // ═══════════════════════════════════════════════════════════════════════════════
    // FFT计算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行FFT变换
     * 将时域信号转换为频域信号
     *
     * @param chIdx 通道索引
     * @param fftType FFT类型（RMS或dB）
     * @param fftWindow 窗函数类型
     * @param dst 目标缓冲区（频域数据）
     * @param src 源缓冲区（时域数据）
     * @return true: 计算成功
     *         false: 计算失败
     */
    public static boolean CalcFFT(int chIdx,int fftType, int fftWindow,ByteBuffer dst,ByteBuffer src){
        boolean bCalc = false;                                                       // 初始化计算结果
        if(dst.isDirect() && src.isDirect()) {                                       // 检查缓冲区是否为Direct
            bCalc = fft(chIdx,fftType, fftWindow, dst, src);                         // 调用Native方法执行FFT
        }
        return bCalc;                                                                // 返回计算结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 双通道运算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行双通道数学运算
     * 支持加、减、乘、除四种运算
     *
     * @param oper 运算类型
     *             MATH_ADD: 加法
     *             MATH_SUB: 减法
     *             MATH_MUL: 乘法
     *             MATH_DIV: 除法
     * @param dst 目标缓冲区（运算结果）
     * @param src1 源缓冲区1（通道1数据）
     * @param src2 源缓冲区2（通道2数据）
     * @return true: 计算成功
     *         false: 计算失败
     */
    public static boolean CalcDual(@Math_Operation int oper ,ByteBuffer dst,ByteBuffer src1,ByteBuffer src2){
        boolean bCalc = false;                                                       // 初始化计算结果
        if(dst.isDirect() && src1.isDirect() && src2.isDirect()){                    // 检查缓冲区是否为Direct
            switch (oper){                                                           // 根据运算类型执行相应操作
                case MATH_ADD:                                                       // 加法
                    bCalc = add(dst,src1,src2);                                      // 调用Native加法方法
                    break;
                case MATH_SUB:                                                       // 减法
                    bCalc = sub(dst,src1,src2);                                      // 调用Native减法方法
                    break;
                case MATH_MUL:                                                       // 乘法
                    bCalc = mul(dst,src1,src2);                                      // 调用Native乘法方法
                    break;
                case MATH_DIV:                                                       // 除法
                    bCalc = div(dst,src1,src2);                                      // 调用Native除法方法
                    break;
            }
        }
        return bCalc;                                                                // 返回计算结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 表达式计算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置数学表达式
     * 将表达式字符串传递给Native层进行解析
     *
     * @param chIdx 通道索引
     * @param exprString 表达式字符串
     * @return true: 设置成功
     *         false: 设置失败
     */
    public static boolean setCalcExpr(int chIdx,String exprString){
        exprString=exprString.replace("μ","u");                                      // 替换希腊字母μ为u

        return setExpr(chIdx,exprString);                                            // 调用Native方法设置表达式
    }

    /**
     * 执行数学表达式计算
     * 根据表达式和通道数据计算结果
     *
     * @param chIdx 通道索引
     * @param dst 目标缓冲区（计算结果）
     * @param chArray 通道数据数组
     * @param time 时间参数
     * @param var1 变量1的值
     * @param var2 变量2的值
     * @return 计算结果值
     */
    public static double CalcExpr(int chIdx,ByteBuffer dst,
                                  ByteBuffer [] chArray,double time,
                                double var1,double var2){

        return calcExpr(chIdx,dst,chArray,time,var1,var2);                           // 调用Native方法执行表达式计算
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据转换方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 将ByteBuffer转换为int数组
     *
     * @param src 源ByteBuffer
     * @return int数组，如果转换失败返回null
     */
    @Nullable
    public static int[] byteBufferToIntArray(ByteBuffer src){
        if(src.isDirect())                                                           // 检查是否为Direct
            return ByteBufferToIntArray(src);                                        // 调用Native方法转换
        else
            return null;                                                             // 非Direct返回null
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据统计方法 - 求和
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 计算ByteBuffer中所有数据的总和
     *
     * @param src 源ByteBuffer
     * @return 数据总和，如果计算失败返回Long.MIN_VALUE
     */
    public static long calcSum(ByteBuffer src){
        if(src.isDirect()){                                                          // 检查是否为Direct
            return sum(src);                                                         // 调用Native方法求和
        }
        return Long.MIN_VALUE;                                                       // 非Direct返回最小值
    }

    /**
     * 计算ByteBuffer中指定范围数据的总和
     *
     * @param src 源ByteBuffer
     * @param idx 起始索引
     * @param len 数据长度
     * @return 数据总和，如果计算失败返回Long.MIN_VALUE
     */
    public static long calcSum(ByteBuffer src, int idx, int len){
        if(src.isDirect()){                                                          // 检查是否为Direct
            return sum1(src, idx, len);                                              // 调用Native方法求和
        }
        return Long.MIN_VALUE;                                                       // 非Direct返回最小值
    }

    /**
     * 计算ByteBuffer中指定范围数据的总和（带步长）
     *
     * @param src 源ByteBuffer
     * @param idx 起始索引
     * @param step 步长
     * @param len 数据长度
     * @return 数据总和，如果计算失败返回Long.MIN_VALUE
     */
    public static long calcSum(ByteBuffer src, int idx, int step, int len){
        if(src.isDirect()){                                                          // 检查是否为Direct
            return sum2(src, idx, step, len);                                        // 调用Native方法求和
        }
        return Long.MIN_VALUE;                                                       // 非Direct返回最小值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据统计方法 - 平均值
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 计算ByteBuffer中所有数据的平均值
     *
     * @param src 源ByteBuffer
     * @return 平均值，如果计算失败返回Integer.MIN_VALUE
     */
    public static int calcAverage(ByteBuffer src){
        if(src.isDirect()){                                                          // 检查是否为Direct
            return average(src);                                                     // 调用Native方法计算平均值
        }
        return Integer.MIN_VALUE;                                                    // 非Direct返回最小值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据统计方法 - 最大值
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 计算ByteBuffer中所有数据的最大值
     *
     * @param src 源ByteBuffer
     * @return 最大值，如果计算失败返回Integer.MIN_VALUE
     */
    public static int calcMax(ByteBuffer src){
        if(src.isDirect()){                                                          // 检查是否为Direct
            return max(src);                                                         // 调用Native方法计算最大值
        }
        return Integer.MIN_VALUE;                                                    // 非Direct返回最小值
    }

    /**
     * 计算ByteBuffer中指定范围数据的最大值
     *
     * @param src 源ByteBuffer
     * @param idx 起始索引
     * @param len 数据长度
     * @return 最大值，如果计算失败返回Integer.MIN_VALUE
     */
    public static int calcMax(ByteBuffer src, int idx, int len){
        if(src.isDirect()){                                                          // 检查是否为Direct
            return max1(src, idx, len);                                              // 调用Native方法计算最大值
        }
        return Integer.MIN_VALUE;                                                    // 非Direct返回最小值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据统计方法 - 最小值
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 计算ByteBuffer中所有数据的最小值
     *
     * @param src 源ByteBuffer
     * @return 最小值，如果计算失败返回Integer.MAX_VALUE
     */
    public static int calcMin(ByteBuffer src){
        if(src.isDirect()){                                                          // 检查是否为Direct
            return min(src);                                                         // 调用Native方法计算最小值
        }
        return Integer.MAX_VALUE;                                                    // 非Direct返回最大值
    }

    /**
     * 计算ByteBuffer中指定范围数据的最小值
     *
     * @param src 源ByteBuffer
     * @param idx 起始索引
     * @param len 数据长度
     * @return 最小值，如果计算失败返回Integer.MAX_VALUE
     */
    public static int calcMin(ByteBuffer src, int idx, int len){
        if(src.isDirect()){                                                          // 检查是否为Direct
            return min1(src, idx, len);                                              // 调用Native方法计算最小值
        }
        return Integer.MAX_VALUE;                                                    // 非Direct返回最大值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 表达式验证方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 验证数学表达式是否有效
     *
     * @param exprString 表达式字符串
     * @return MathExprError对象，包含验证结果和错误信息
     */
    public static MathExprError mathExprValid(String exprString){
        exprString=exprString.replace("μ","u");                                      // 替换希腊字母μ为u
//        Logger.d("limh", "exprString= " + exprString);
        return isExprValid(exprString);                                              // 调用Native方法验证表达式
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Native方法声明
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 计算FFT点数
     *
     * @param length 数据长度
     * @return FFT点数
     */
    public static native int CalFFTPointNum(int length);

    /**
     * 计算ByteBuffer中所有数据的总和
     *
     * @param src 源ByteBuffer
     * @return 数据总和
     */
    private static native long sum(ByteBuffer src);

    /**
     * 计算ByteBuffer中指定范围数据的总和
     *
     * @param src 源ByteBuffer
     * @param idx 起始索引
     * @param len 数据长度
     * @return 数据总和
     */
    private static native long sum1(ByteBuffer src, int idx, int len);

    /**
     * 计算ByteBuffer中指定范围数据的总和（带步长）
     *
     * @param src 源ByteBuffer
     * @param idx 起始索引
     * @param step 步长
     * @param len 数据长度
     * @return 数据总和
     */
    private static native long sum2(ByteBuffer src, int idx, int step, int len);

    /**
     * 计算ByteBuffer中所有数据的平均值
     *
     * @param src 源ByteBuffer
     * @return 平均值
     */
    private static native int average(ByteBuffer src);

    /**
     * 计算ByteBuffer中所有数据的最大值
     *
     * @param src 源ByteBuffer
     * @return 最大值
     */
    private static native int max(ByteBuffer src);

    /**
     * 计算ByteBuffer中指定范围数据的最大值
     *
     * @param src 源ByteBuffer
     * @param idx 起始索引
     * @param len 数据长度
     * @return 最大值
     */
    private static native int max1(ByteBuffer src, int idx, int len);

    /**
     * 计算ByteBuffer中所有数据的最小值
     *
     * @param src 源ByteBuffer
     * @return 最小值
     */
    private static native int min(ByteBuffer src);

    /**
     * 计算ByteBuffer中指定范围数据的最小值
     *
     * @param src 源ByteBuffer
     * @param idx 起始索引
     * @param len 数据长度
     * @return 最小值
     */
    private static native int min1(ByteBuffer src, int idx, int len);

    /**
     * 执行加法运算
     *
     * @param dst 目标缓冲区
     * @param src1 源缓冲区1
     * @param src2 源缓冲区2
     * @return true: 成功
     *         false: 失败
     */
    private static native boolean add(ByteBuffer dst,ByteBuffer src1,ByteBuffer src2);

    /**
     * 执行减法运算
     *
     * @param dst 目标缓冲区
     * @param src1 源缓冲区1
     * @param src2 源缓冲区2
     * @return true: 成功
     *         false: 失败
     */
    private static native boolean sub(ByteBuffer dst,ByteBuffer src1,ByteBuffer src2);

    /**
     * 执行乘法运算
     *
     * @param dst 目标缓冲区
     * @param src1 源缓冲区1
     * @param src2 源缓冲区2
     * @return true: 成功
     *         false: 失败
     */
    private static native boolean mul(ByteBuffer dst,ByteBuffer src1,ByteBuffer src2);

    /**
     * 执行除法运算
     *
     * @param dst 目标缓冲区
     * @param src1 源缓冲区1
     * @param src2 源缓冲区2
     * @return true: 成功
     *         false: 失败
     */
    private static native boolean div(ByteBuffer dst,ByteBuffer src1,ByteBuffer src2);

    /**
     * 执行FFT变换
     *
     * @param chIdx 通道索引
     * @param fftType FFT类型
     * @param fftWindow 窗函数类型
     * @param dst 目标缓冲区
     * @param src 源缓冲区
     * @return true: 成功
     *         false: 失败
     */
    private static native boolean fft(int chIdx,int fftType,int fftWindow,ByteBuffer dst,ByteBuffer src);

    /**
     * 获取FFT的DC值
     *
     * @param chIdx 通道索引
     * @return DC值
     */
    private static native double fftDcVal(int chIdx);

    /**
     * 获取FFT的最大值
     *
     * @param chIdx 通道索引
     * @return 最大值
     */
    private static native double fftMaxVal(int chIdx);

    /**
     * 获取FFT最大值对应的频率索引
     *
     * @param chIdx 通道索引
     * @return 频率索引
     */
    private static native int   fftMaxValIdx(int chIdx);

    /**
     * 设置垂直轴电压值
     *
     * @param chIdx 通道索引
     * @param vol 电压值
     */
    private static native void csetVAd(int chIdx,double vol);

    /**
     * 将ByteBuffer转换为int数组
     *
     * @param src 源ByteBuffer
     * @return int数组
     */
    private static native int[] ByteBufferToIntArray(ByteBuffer src);

    /**
     * 设置数学表达式
     *
     * @param chIdx 通道索引
     * @param exprString 表达式字符串
     * @return true: 成功
     *         false: 失败
     */
    private static native boolean setExpr(int chIdx,String exprString);

    /**
     * 执行数学表达式计算
     *
     * @param chIdx 通道索引
     * @param dst 目标缓冲区
     * @param chArray 通道数据数组
     * @param time 时间参数
     * @param var1 变量1
     * @param var2 变量2
     * @return 计算结果
     */
    private static native double calcExpr(int chIdx,ByteBuffer dst,ByteBuffer [] chArray,double time,double var1,double var2);

    /**
     * 验证表达式是否有效
     *
     * @param expString 表达式字符串
     * @return MathExprError对象
     */
    private static native MathExprError isExprValid(String expString);


}
