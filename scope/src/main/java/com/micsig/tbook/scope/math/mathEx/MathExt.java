package com.micsig.tbook.scope.math.mathEx;  // 定义包名：示波器数学运算扩展模块

import android.content.res.AssetManager;  // 导入AssetManager类：Android资源管理器，用于加载Native层所需的资源文件

import java.nio.ByteBuffer;  // 导入ByteBuffer类：字节缓冲区，用于Native层数据传输
import java.nio.IntBuffer;  // 导入IntBuffer类：整型缓冲区，用于Native层数据传输

/**
 * 数学运算扩展JNI接口类 - 提供Native层高性能数学运算的Java调用接口
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.math.mathEx（示波器数学运算扩展模块）</li>
 *   <li>架构层级：Native接口层 - JNI桥接层</li>
 *   <li>设计模式：外观模式（Facade Pattern）</li>
 *   <li>职责类型：封装Native库调用、提供高性能数学运算接口</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>加载Native数学运算库（libmath_ext.so）</li>
 *   <li>初始化OpenCL并行计算环境</li>
 *   <li>提供整数转十六进制字符串的Native方法</li>
 *   <li>提供双精度浮点数转ASCII字符串的Native方法</li>
 * </ul>
 * 
 * <p><b>Native库架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   MathExt - JNI接口层                                                    │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   Java层（MathExt.java）                                         │   │
 * │   │                                                                   │   │
 * │   │   initMathExprOpenCL() ───→ 初始化OpenCL环境                      │   │
 * │   │   IntToHex() ────────────→ 整数转十六进制字符串                   │   │
 * │   │   DoubleToASCII() ───────→ 双精度浮点数转ASCII字符串             │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                              │ JNI调用                                   │
 * │                              ▼                                           │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   Native层（libmath_ext.so）                                     │   │
 * │   │                                                                   │   │
 * │   │   ├── OpenCL内核：并行计算加速                                    │   │
 * │   │   ├── 整数处理：高效格式转换                                      │   │
 * │   │   └── 浮点处理：精确字符串转换                                    │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>OpenCL并行计算说明：</b>
 * <pre>
 * OpenCL（Open Computing Language）是一种开放的并行计算框架：
 *   ├── 利用GPU进行大规模并行计算
 *   ├── 适用于数学表达式的高速计算
 *   ├── 需要加载OpenCL内核文件（.cl文件）
 *   └── 通过AssetManager加载资源文件
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>数学表达式运算（MathExprWave）需要高性能计算时</li>
 *   <li>波形数据格式转换时（整数转十六进制显示）</li>
 *   <li>测量数据显示时（浮点数转ASCII字符串）</li>
 *   <li>需要GPU加速的复杂数学运算</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：libmath_ext.so（Native数学运算库）</li>
 *   <li>依赖：AssetManager（Android资源管理器）</li>
 *   <li>依赖：OpenCL运行时环境</li>
 *   <li>被依赖：MathExprWave（数学表达式运算）</li>
 * </ul>
 * 
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>Native方法通常是线程安全的</li>
 *   <li>OpenCL环境初始化只需执行一次</li>
 * </ul>
 * 
 * @author Liwb
 * @version 1.0
 * @since 2025-6-11
 * @see MathExprWave 数学表达式运算
 * @see MathNative 数学运算Native接口
 */
public class MathExt {
    
    /**
     * 静态初始化块：加载Native库
     * 
     * <p>在类加载时自动执行，加载名为"math_ext"的Native库。
     * 系统会自动查找并加载libmath_ext.so文件（Linux/Android）或math_ext.dll（Windows）。
     * 
     * <p><b>加载流程：</b>
     * <ol>
     *   <li>JVM加载MathExt类时触发static块</li>
     *   <li>System.loadLibrary()查找Native库</li>
     *   <li>加载libmath_ext.so到内存</li>
     *   <li>链接Native方法声明与实现</li>
     * </ol>
     * 
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果库文件不存在，抛出UnsatisfiedLinkError</li>
     *   <li>如果库文件加载失败，抛出UnsatisfiedLinkError</li>
     * </ul>
     */
    static {
        System.loadLibrary("math_ext");  // 加载Native数学运算库：libmath_ext.so
    }

    /**
     * 初始化数学表达式OpenCL并行计算环境
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>初始化OpenCL平台和设备</li>
     *   <li>加载OpenCL内核程序（.cl文件）</li>
     *   <li>创建命令队列和内存对象</li>
     *   <li>为后续的并行数学运算做准备</li>
     * </ul>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>应用启动时调用一次</li>
     *   <li>在使用数学表达式运算功能之前调用</li>
     * </ul>
     * 
     * <p><b>资源加载：</b>
     * <ul>
     *   <li>通过AssetManager加载assets目录下的OpenCL内核文件</li>
     *   <li>内核文件通常命名为math_expr.cl或类似名称</li>
     * </ul>
     * 
     * @param assetManager Android资源管理器，用于加载OpenCL内核资源文件
     */
    public static native void initMathExprOpenCL(AssetManager assetManager);  // 声明Native方法：初始化OpenCL环境


    /**
     * 整数转十六进制字符串
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将整型缓冲区中的数据转换为十六进制字符串格式</li>
     *   <li>支持指定占位值和头部长度</li>
     *   <li>用于波形数据的十六进制显示</li>
     * </ul>
     * 
     * <p><b>转换流程：</b>
     * <pre>
     * 输入：IntBuffer [1234, 5678, 9012]
     * 输出：ByteBuffer "4D2 162E 2334"（十六进制字符串）
     * </pre>
     * 
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>src: 源整型缓冲区，包含待转换的整数数据</li>
     *   <li>desFD: 目标字节缓冲区，存储转换后的十六进制字符串</li>
     *   <li>placeVal: 占位值，用于填充或格式化</li>
     *   <li>headLend: 头部长度，指定输出字符串的前导字符数</li>
     *   <li>waveLen: 波形长度，指定要转换的数据点数</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>波形数据的十六进制格式显示</li>
     *   <li>调试时查看原始数据</li>
     *   <li>数据导出为文本格式</li>
     * </ul>
     * 
     * @param src 源整型缓冲区，包含待转换的整数数据
     * @param desFD 目标字节缓冲区，存储转换后的十六进制字符串
     * @param placeVal 占位值，用于填充或格式化
     * @param headLend 头部长度，指定输出字符串的前导字符数
     * @param waveLen 波形长度，指定要转换的数据点数
     */
    public static native void IntToHex(IntBuffer src, ByteBuffer desFD, int placeVal, int headLend, int waveLen);  // 声明Native方法：整数转十六进制字符串
    
    /**
     * 双精度浮点数转ASCII字符串
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将整型缓冲区中的数据结合双精度值转换为ASCII字符串</li>
     *   <li>支持科学计数法格式</li>
     *   <li>用于测量数据的文本显示</li>
     * </ul>
     * 
     * <p><b>转换流程：</b>
     * <pre>
     * 输入：IntBuffer数据 + 双精度值vv
     * 输出：ByteBuffer "3.14159265358979"（ASCII字符串）
     * </pre>
     * 
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>src: 源整型缓冲区，包含待转换的整数数据</li>
     *   <li>desFD: 目标字节缓冲区，存储转换后的ASCII字符串</li>
     *   <li>vv: 双精度浮点数值，用于格式化输出</li>
     *   <li>headLend: 头部长度，指定输出字符串的前导字符数</li>
     *   <li>waveLen: 波形长度，指定要转换的数据点数</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>测量结果的文本显示（如频率、幅度、周期等）</li>
     *   <li>波形参数的字符串格式化</li>
     *   <li>数据导出为可读文本</li>
     * </ul>
     * 
     * @param src 源整型缓冲区，包含待转换的整数数据
     * @param desFD 目标字节缓冲区，存储转换后的ASCII字符串
     * @param vv 双精度浮点数值，用于格式化输出
     * @param headLend 头部长度，指定输出字符串的前导字符数
     * @param waveLen 波形长度，指定要转换的数据点数
     */
    public static native void DoubleToASCII(IntBuffer src,ByteBuffer desFD,double vv,int headLend, int waveLen);  // 声明Native方法：双精度浮点数转ASCII字符串
}
