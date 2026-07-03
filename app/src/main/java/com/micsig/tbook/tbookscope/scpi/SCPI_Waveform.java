package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 波形数据读取(Waveform)子模块                     |
 * | 核心职责：处理SCPI协议中:WAVeform相关命令的设置与查询                          |
 * | 架构设计：静态方法类，委托Command中间件的Waveform接口执行实际操作               |
 * | 数据流向：SCPIParam → 本类静态方法 → Command.get().getWaveform()            |
 * | 依赖关系：SCPIParam、Command、ToolsSCPI                                    |
 * | 使用场景：远程读取波形原始数据、查询波形参数（X/Y增量/原点/参考点等）          |
 * +---------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Waveform {

//            new SCPICommandStruct(":WAVeform:SOURce","SCPI_Waveform","Source"),//设置波形读取的通道源
//            new SCPICommandStruct(":WAVeform:SOURce?","SCPI_Waveform","SourceQ"),//查询波形读取的通道源
//            new SCPICommandStruct(":WAVeform:MODE","SCPI_Waveform","Mode"),//设置波形的读取模式
//            new SCPICommandStruct(":WAVeform:MODE?","SCPI_Waveform","ModeQ"),//查询波形的读取模式
//            new SCPICommandStruct(":WAVeform:FORMat","SCPI_Waveform","Format"),//设置波形的读取模式
//            new SCPICommandStruct(":WAVeform:FORMat?","SCPI_Waveform","FormatQ"),//查询波形的读取模式
//            new SCPICommandStruct(":WAVeform:STARt","SCPI_Waveform","Start"),//设置内存中波形被读取的起始位置
//            new SCPICommandStruct(":WAVeform:STARt?","SCPI_Waveform","StartQ"),//查询内存中波形被读取的起始位置
//            new SCPICommandStruct(":WAVeform:STOP","SCPI_Waveform","Stop"),//设置内存中波形被读取的停止位置
//            new SCPICommandStruct(":WAVeform:STOP?","SCPI_Waveform","StopQ"),//查询内存中波形被读取的停止位置
//            new SCPICommandStruct(":WAVeform:DATA?","SCPI_Waveform","DataQ"),//读取波形数据
//            new SCPICommandStruct(":WAVeform:PREamble?","SCPI_Waveform","PreambleQ"),//查询全部的波形参数
//            new SCPICommandStruct(":WAVeform:XINCrement?","SCPI_Waveform","XincrementQ"),//查询指定源x方向上相邻两点的时间差
//            new SCPICommandStruct(":WAVeform:XORigin?","SCPI_Waveform","XoriginQ"),//查询指定源x方向从触发点到参考时间基准的时间
//            new SCPICommandStruct(":WAVeform:XREFerence?","SCPI_Waveform","XreferenceQ"),//查询指定源x方向上数据点的参考时间基准
//            new SCPICommandStruct(":WAVeform:YINCrement?","SCPI_Waveform","YincrementQ"),//查询指定源y方向上相邻两点的时间差
//            new SCPICommandStruct(":WAVeform:YORigin?","SCPI_Waveform","YoriginQ"),//查询指定源y方向从触发点到参考时间基准的时间
//            new SCPICommandStruct(":WAVeform:YREFerence?","SCPI_Waveform","YReferenceQ"),//查询指定源y方向上数据点的参考时间基准


    /**
     * 设置波形读取的通道源
     * @param param SCPI参数封装，iParam1为通道索引
     */
    public static void Source(SCPIParam param){
        Command.get().getWaveform().Source(param.iParam1, true); // 委托Command中间件设置波形源通道，true表示通知UI刷新
    }

    /**
     * 查询波形读取的通道源
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 通道名称字符串，如"CH1"
     */
    public static String SourceQ(SCPIParam param){
        int i= Command.get().getWaveform().SourceQ(); // 从Command中间件获取波形源通道索引
        return ToolsSCPI.getCh(i); // 将索引转换为通道名称字符串
    }

    /**
     * 设置波形读取模式（普通/最大/最小等）
     * @param param SCPI参数封装，iParam1为模式索引
     */
    public static void Mode(SCPIParam param){
        Command.get().getWaveform().Mode(param.iParam1, true); // 委托Command中间件设置读取模式，true表示通知UI刷新
    }

    /**
     * 查询波形读取模式
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 读取模式名称字符串
     */
    public static String ModeQ(SCPIParam param){
        int i= Command.get().getWaveform().ModeQ(); // 从Command中间件获取读取模式索引
        return ToolsSCPI.getWaveFormMode(i); // 将索引转换为读取模式名称字符串
    }

    /**
     * 设置波形数据格式（字节/字/ASCII等）
     * @param param SCPI参数封装，iParam1为格式索引
     */
    public static void Format(SCPIParam param){
        Command.get().getWaveform().Format(param.iParam1, true); // 委托Command中间件设置数据格式，true表示通知UI刷新
    }

    /**
     * 查询波形数据格式
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 数据格式名称字符串
     */
    public static String FormatQ(SCPIParam param){
        int i=Command.get().getWaveform().FormatQ(); // 从Command中间件获取数据格式索引
        return ToolsSCPI.getWaveFormFormat(i); // 将索引转换为格式名称字符串
    }

    /**
     * 设置波形读取的起始位置
     * @param param SCPI参数封装，iParam1为起始点索引
     */
    public static void Start(SCPIParam param){
        Command.get().getWaveform().Start(param.iParam1, true); // 委托Command中间件设置起始位置，true表示通知UI刷新
    }

    /**
     * 查询波形读取的起始位置
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 起始位置的字符串表示
     */
    public static String StartQ(SCPIParam param){
        int i=Command.get().getWaveform().StartQ(); // 从Command中间件获取起始位置
        return String.valueOf(i); // 将起始位置转换为字符串
    }

    /**
     * 设置波形读取的停止位置
     * @param param SCPI参数封装，iParam1为停止点索引
     */
    public static void Stop(SCPIParam param){
        Command.get().getWaveform().Stop(param.iParam1, true); // 委托Command中间件设置停止位置，true表示通知UI刷新
    }

    /**
     * 查询波形读取的停止位置
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 停止位置的字符串表示
     */
    public static String StopQ(SCPIParam param){

        int i= Command.get().getWaveform().StopQ(); // 从Command中间件获取停止位置
        return String.valueOf(i); // 将停止位置转换为字符串
    }

    /**
     * 读取波形数据（默认格式，追加换行符）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 波形数据的StringBuilder
     */
    public static StringBuilder DataQ(SCPIParam param){
        StringBuilder sb = Command.get().getWaveform().DataQ(); // 从Command中间件获取波形数据
        sb.append("\r\n"); // 追加SCPI标准换行符
        return sb; // 返回波形数据
    }

    /**
     * 读取波形数据（二进制格式）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 二进制波形数据的StringBuilder
     */
    public static StringBuilder DataBinQ(SCPIParam param){
        StringBuilder sb= Command.get().getWaveform().DataBinQ(); // 从Command中间件获取二进制格式波形数据
        return sb; // 返回二进制波形数据
    }

    /**
     * 读取波形数据（十六进制格式）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 十六进制波形数据的StringBuilder
     */
    public static StringBuilder DataHexQ(SCPIParam param){
        StringBuilder sb=Command.get().getWaveform().DataHexQ(); // 从Command中间件获取十六进制格式波形数据
        return sb; // 返回十六进制波形数据
    }

    /**
     * 读取波形数据（ASCII格式）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return ASCII波形数据的StringBuilder
     */
    public static StringBuilder DataAsciiQ(SCPIParam param){
        StringBuilder sb=Command.get().getWaveform().DataASCIIQ(); // 从Command中间件获取ASCII格式波形数据
        return sb; // 返回ASCII波形数据
    }

    /**
     * 查询波形参数前导信息（Preamble）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return Preamble字符串
     */
    public static String PreambleQ(SCPIParam param){
        return Command.get().getWaveform().PreambleQ(); // 委托Command中间件查询波形前导信息
    }

    /**
     * 查询X方向上相邻两点的时间增量
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return X增量值的字符串表示
     */
    public static String XincrementQ(SCPIParam param){
        double d=Command.get().getWaveform().XincrementQ(); // 从Command中间件获取X增量值
        return String.valueOf(d); // 将X增量转换为字符串
    }

    /**
     * 查询X方向的触发原点时间
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return X原点值的字符串表示
     */
    public static String XoriginQ(SCPIParam param){
        double d=Command.get().getWaveform().XoriginQ(); // 从Command中间件获取X原点值
        return String.valueOf(d); // 将X原点转换为字符串
    }

    /**
     * 查询X方向上数据点的参考时间基准
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return X参考点值的字符串表示
     */
    public static String XreferenceQ(SCPIParam param){
        double d=Command.get().getWaveform().XreferenceQ(); // 从Command中间件获取X参考点值
        return String.valueOf(d); // 将X参考点转换为字符串
    }

    /**
     * 查询Y方向上相邻两点的电压增量
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return Y增量值的字符串表示
     */
    public static String YincrementQ(SCPIParam param){
        double d=Command.get().getWaveform().YincrementQ(); // 从Command中间件获取Y增量值
        return String.valueOf(d); // 将Y增量转换为字符串
    }

    /**
     * 查询Y方向的电压原点
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return Y原点值的字符串表示
     */
    public static String YoriginQ(SCPIParam param){
        double d=Command.get().getWaveform().YOriginQ(); // 从Command中间件获取Y原点值
        return String.valueOf(d); // 将Y原点转换为字符串
    }

    /**
     * 查询Y方向上数据点的参考电压基准
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return Y参考点值的字符串表示
     */
    public static String YReferenceQ(SCPIParam param){
        double d=Command.get().getWaveform().YReferenceQ(); // 从Command中间件获取Y参考点值
        return String.valueOf(d); // 将Y参考点转换为字符串
    }

}
