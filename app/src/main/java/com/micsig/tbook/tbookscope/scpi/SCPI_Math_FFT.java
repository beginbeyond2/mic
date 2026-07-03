package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件，用于获取FFT配置操作接口

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 数学运算FFT(快速傅里叶变换)子模块               |
 * | 核心职责：处理SCPI协议中:MATH:FFT相关命令的设置与查询                        |
 * | 架构设计：静态方法类，每个方法对应一条SCPI指令，委托Command中间件执行实际操作  |
 * | 数据流向：SCPI参数(SCPIParam) → 本类静态方法 → Command.get().getMath_fft()  |
 * | 依赖关系：SCPIParam(参数封装)、Command(命令中间件)、ToolsSCPI(结果格式化)    |
 * | 使用场景：远程控制/自动化测试时，通过SCPI指令设置FFT信源、窗函数、显示方式等  |
 * +---------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Math_FFT {
    //FFT
//            new SCPICommandStruct(":MATH:FFT:SOURce","SCPI_Math_FFT","Source"),//选择FFT运算的信源
//            new SCPICommandStruct(":MATH:FFT:SOURce?","SCPI_Math_FFT","SourceQ"),//查询FFT运算的信源
//            new SCPICommandStruct(":MATH:FFT:WINDow","SCPI_Math_FFT","Window"),//选择FFT运算的窗函数
//            new SCPICommandStruct(":MATH:FFT:WINDow?","SCPI_Math_FFT","WindowQ"),//查询FFT运算的窗函数
//            new SCPICommandStruct(":MATH:FFT:TYPE","SCPI_Math_FFT","Type"),//选择FFT波形的显示方式
//            new SCPICommandStruct(":MATH:FFT:TYPE?","SCPI_Math_FFT","TypeQ"),//查询FFT波形的显示方式
//            new SCPICommandStruct(":MATH:FFT:EXTent","SCPI_Math_FFT","Extent"),//设置FFT运算结果的垂直档位
//            new SCPICommandStruct(":MATH:FFT:PLUS:EXTent","SCPI_Math_FFT","Plus_Extent"),//设置FFT运算结果的垂直档位
//            new SCPICommandStruct(":MATH:FFT:EXTent?","SCPI_Math_FFT","ExtentQ"),//查询FFT运算结果的垂直档位
//            new SCPICommandStruct(":MATH:FFT:OFFSet","SCPI_Math_FFT","Offset"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:PLUS:OFFSet","SCPI_Math_FFT","Plus_Offset"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:OFFSet?","SCPI_Math_FFT","OffsetQ"),//查询FFT运算结果的垂直偏移
//           new SCPICommandStruct(":MATH:FFT:HSCAle","SCPI_Math_FFT","HsCale"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:HSCAle?","SCPI_Math_FFT","HsCaleQ"),//查询FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:POSition","SCPI_Math_FFT","Position"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:POSition?","SCPI_Math_FFT","PositionQ"),//查询FFT运算结果的垂直偏移

    /**
     * 设置FFT运算的信源通道
     * @param param SCPI参数封装，iParam1为通道索引，iParam2为信源类型
     */
    public static void Source(SCPIParam param) {
        //if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 4) {
        Command.get().getMath_fft().Source(param.iParam1, param.iParam2, true); // 委托Command中间件设置FFT信源，true表示通知UI刷新
       // }
    }

    /**
     * 查询FFT运算的信源通道
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 信源通道的字符串标识，如"CH1"
     */
    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getMath_fft().SourceQ(); // 从Command中间件获取当前FFT信源索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为通道名称字符串
    }

    /**
     * 设置FFT运算的窗函数类型
     * @param param SCPI参数封装，iParam1为窗函数类型索引，iParam2为窗函数参数
     */
    public static void Window(SCPIParam param) {
       // if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 4) {
        Command.get().getMath_fft().Window(param.iParam1, param.iParam2, true); // 委托Command中间件设置FFT窗函数，true表示通知UI刷新
       // }
    }

    /**
     * 查询FFT运算的窗函数类型
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 窗函数名称字符串，如"RECTangular"
     */
    public static String WindowQ(SCPIParam param) {
        int i=Command.get().getMath_fft().WindowQ(); // 从Command中间件获取当前FFT窗函数索引
        return ToolsSCPI.getMathWindow(i); // 将窗函数索引转换为窗函数名称字符串
    }

    /**
     * 设置FFT波形的显示方式（如幅度谱/相位谱）
     * @param param SCPI参数封装，iParam1为显示方式索引，iParam2为显示参数
     */
    public static void Type(SCPIParam param) {
      //  if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 4) {
        Command.get().getMath_fft().Type(param.iParam1, param.iParam2, true); // 委托Command中间件设置FFT显示方式，true表示通知UI刷新
      //  }
    }

    /**
     * 查询FFT波形的显示方式
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return FFT显示方式名称字符串，如"MAGNitude"
     */
    public static String TypeQ(SCPIParam param) {
        int i=Command.get().getMath_fft().TypeQ(); // 从Command中间件获取当前FFT显示方式索引
        return ToolsSCPI.getMathFftType(i); // 将显示方式索引转换为名称字符串
    }

    /**
     * 设置FFT运算结果的垂直档位
     * @param param SCPI参数封装，iParam1为通道索引，dParam1为档位值
     */
    public static void Extent(SCPIParam param) {
        Command.get().getMath_fft().Extent(param.iParam1, param.dParam1,true); // 委托Command中间件设置FFT垂直档位，true表示通知UI刷新
    }

    /**
     * 设置FFT运算结果的垂直档位（Plus增量方式）
     * @param param SCPI参数封装（本方法为占位实现，暂无操作）
     */
    public static void Plus_Extent(SCPIParam param) {
    }

    /**
     * 查询FFT运算结果的垂直档位
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 垂直档位值的字符串表示
     */
    public static String ExtentQ(SCPIParam param) {
        double d=Command.get().getMath_fft().ExtentQ(); // 从Command中间件获取当前FFT垂直档位值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

    /**
     * 设置FFT运算结果的垂直偏移
     * @param param SCPI参数封装，iParam1为通道索引，dParam1为偏移值
     */
    public static void Offset(SCPIParam param) {
        Command.get().getMath_fft().Offset(param.iParam1, param.dParam1, true); // 委托Command中间件设置FFT垂直偏移，true表示通知UI刷新
    }

    /**
     * 设置FFT运算结果的垂直偏移（Plus增量方式）
     * @param param SCPI参数封装（本方法为占位实现，暂无操作）
     */
    public static void Plus_Offset(SCPIParam param) {
    }

    /**
     * 查询FFT运算结果的垂直偏移
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 垂直偏移值的字符串表示
     */
    public static String OffsetQ(SCPIParam param) {
        return Command.get().getMath_fft().OffsetQ(param.iParam1); // 查询指定通道的FFT垂直偏移值并返回字符串

    }

    /**
     * 设置FFT运算结果的水平缩放比例
     * @param param SCPI参数封装，iParam1为通道索引，dParam1为缩放值
     */
    public static void HsCale(SCPIParam param){
        Command.get().getMath_fft().HsCale(param.iParam1, param.dParam1, true); // 委托Command中间件设置FFT水平缩放，true表示通知UI刷新
    }

    /**
     * 查询FFT运算结果的水平缩放比例
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 水平缩放值的字符串表示
     */
    public static String HsCaleQ(SCPIParam param){
        double d= Command.get().getMath_fft().HsCaleQ(); // 从Command中间件获取当前FFT水平缩放值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

    /**
     * 设置FFT运算结果的垂直位置
     * @param param SCPI参数封装，iParam1为通道索引，dParam1为位置值
     */
    public static void Position(SCPIParam param){
        Command.get().getMath_fft().Position(param.iParam1, param.dParam1, true); // 委托Command中间件设置FFT垂直位置，true表示通知UI刷新
    }

    /**
     * 查询FFT运算结果的垂直位置
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 垂直位置值的字符串表示
     */
    public static String PositionQ(SCPIParam param)
    {
        double d=Command.get().getMath_fft().PositionQ(); // 从Command中间件获取当前FFT垂直位置值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

}
