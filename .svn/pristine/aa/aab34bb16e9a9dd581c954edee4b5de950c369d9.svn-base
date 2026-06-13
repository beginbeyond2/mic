package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
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

    public static void Source(SCPIParam param) {
        //if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 4) {
        Command.get().getMath_fft().Source(param.iParam1, param.iParam2, true);
       // }
    }

    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getMath_fft().SourceQ();
        return ToolsSCPI.getCh(i);
    }

    public static void Window(SCPIParam param) {
       // if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 4) {
        Command.get().getMath_fft().Window(param.iParam1, param.iParam2, true);
       // }
    }

    public static String WindowQ(SCPIParam param) {
        int i=Command.get().getMath_fft().WindowQ();
        return ToolsSCPI.getMathWindow(i);
    }

    public static void Type(SCPIParam param) {
      //  if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 4) {
        Command.get().getMath_fft().Type(param.iParam1, param.iParam2, true);
      //  }
    }

    public static String TypeQ(SCPIParam param) {
        int i=Command.get().getMath_fft().TypeQ();
        return ToolsSCPI.getMathFftType(i);
    }

    public static void Extent(SCPIParam param) {
        Command.get().getMath_fft().Extent(param.iParam1, param.dParam1,true);
    }

    public static void Plus_Extent(SCPIParam param) {
    }

    public static String ExtentQ(SCPIParam param) {
        double d=Command.get().getMath_fft().ExtentQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Offset(SCPIParam param) {
        Command.get().getMath_fft().Offset(param.iParam1, param.dParam1, true);
    }

    public static void Plus_Offset(SCPIParam param) {
    }

    public static String OffsetQ(SCPIParam param) {
        return Command.get().getMath_fft().OffsetQ(param.iParam1);

    }

    public static void HsCale(SCPIParam param){
        Command.get().getMath_fft().HsCale(param.iParam1, param.dParam1, true);
    }
    public static String HsCaleQ(SCPIParam param){
        double d= Command.get().getMath_fft().HsCaleQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Position(SCPIParam param){
        Command.get().getMath_fft().Position(param.iParam1, param.dParam1, true);
    }
    public static String PositionQ(SCPIParam param)
    {
        double d=Command.get().getMath_fft().PositionQ();
        return ToolsSCPI.getDouble(d);
    }

}
