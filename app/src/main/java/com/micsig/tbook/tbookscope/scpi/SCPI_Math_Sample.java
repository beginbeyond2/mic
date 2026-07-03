package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import com.micsig.base.Logger; // 导入日志工具
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂，用于获取数学通道实例
import com.micsig.tbook.scope.channel.MathChannel; // 导入数学通道类，提供采样率和波形长度查询
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道转换工具类

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 数学运算采样信息查询子模块                      |
 * | 核心职责：处理SCPI协议中:SAMPleACQuire:MATH相关查询命令                      |
 * | 架构设计：静态方法类，通过ChannelFactory获取MathChannel实例完成查询           |
 * | 数据流向：SCPIParam → TChan通道转换 → ChannelFactory → MathChannel → 结果   |
 * | 依赖关系：SCPIParam、TChan(通道号转换)、ChannelFactory、MathChannel         |
 * | 使用场景：远程查询数学通道的采样率和存储深度                                 |
 * +---------------------------------------------------------------------------+
 *
 * @auother Liwb
 * @description: 数学运算采样信息SCPI查询
 * @data:2022-3-24 18:10
 */
public class SCPI_Math_Sample {
//           new SCPICommandStruct( ":SAMPleACQuire:MATH:SRATe?","SCPI_Math_Sample","SRateQ"),
//            new SCPICommandStruct( ":SAMPLeACQuire:MATH:MDEPth?", "SCPI_Math_Sample","MDepthQ"),

    /**
     * 查询数学运算通道的采样率
     * @param param SCPI参数封装，iParam1为源通道号（1-8）
     * @return 采样率字符串，若通道无效则返回空字符串
     */
    public static String SRateQ(SCPIParam param){

        int source = param.iParam1;//不确定是否为 1-8 // 获取源通道号参数
        int mathChan = TChan.toMathChan(source); // 将源通道号转换为数学通道号
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan)); // 通过通道工厂获取数学通道实例
        if (mathChannel != null) { // 判断数学通道实例是否有效
            double d = mathChannel.getSampleRate2display(); // 获取数学通道的采样率（用于显示）
            return String.valueOf(d); // 将采样率转换为字符串返回
        }
        return ""; // 通道无效时返回空字符串
    }

    /**
     * 查询数学运算通道的存储深度（波形长度）
     * @param param SCPI参数封装，iParam1为源通道号（1-8）
     * @return 存储深度字符串，若通道无效则返回空字符串
     */
    public static String MDepthQ(SCPIParam param){

        int source = param.iParam1;//不确定是否为 1-8 // 获取源通道号参数
        int mathChan = TChan.toMathChan(source); // 将源通道号转换为数学通道号
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan)); // 通过通道工厂获取数学通道实例
        if (mathChannel != null) { // 判断数学通道实例是否有效
            int i = mathChannel.getWaveLen(); // 获取数学通道的波形长度（存储深度）
            return String.valueOf(i); // 将存储深度转换为字符串返回
        }
        return ""; // 通道无效时返回空字符串
    }

}
