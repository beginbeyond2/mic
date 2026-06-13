package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
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


    public static void Source(SCPIParam param){
        Command.get().getWaveform().Source(param.iParam1, true);
    }
    public static String SourceQ(SCPIParam param){
        int i= Command.get().getWaveform().SourceQ();
        return ToolsSCPI.getCh(i);
    }
    public static void Mode(SCPIParam param){
        Command.get().getWaveform().Mode(param.iParam1, true);
    }
    public static String ModeQ(SCPIParam param){
        int i= Command.get().getWaveform().ModeQ();
        return ToolsSCPI.getWaveFormMode(i);
    }
    public static void Format(SCPIParam param){
        Command.get().getWaveform().Format(param.iParam1, true);
    }
    public static String FormatQ(SCPIParam param){
        int i=Command.get().getWaveform().FormatQ();
        return ToolsSCPI.getWaveFormFormat(i);
    }
    public static void Start(SCPIParam param){
        Command.get().getWaveform().Start(param.iParam1, true);
    }
    public static String StartQ(SCPIParam param){
        int i=Command.get().getWaveform().StartQ();
        return String.valueOf(i);
    }
    public static void Stop(SCPIParam param){
        Command.get().getWaveform().Stop(param.iParam1, true);
    }
    public static String StopQ(SCPIParam param){

        int i= Command.get().getWaveform().StopQ();
        return String.valueOf(i);
    }
    public static StringBuilder DataQ(SCPIParam param){
        StringBuilder sb = Command.get().getWaveform().DataQ();
        sb.append("\r\n");
        return sb;
    }
    public static StringBuilder DataBinQ(SCPIParam param){
        StringBuilder sb= Command.get().getWaveform().DataBinQ();
        return sb;
    }
    public static StringBuilder DataHexQ(SCPIParam param){
        StringBuilder sb=Command.get().getWaveform().DataHexQ();
        return sb;
    }
    public static StringBuilder DataAsciiQ(SCPIParam param){
        StringBuilder sb=Command.get().getWaveform().DataASCIIQ();
        return sb;
    }
    public static String PreambleQ(SCPIParam param){
        return Command.get().getWaveform().PreambleQ();
    }
    public static String XincrementQ(SCPIParam param){
        double d=Command.get().getWaveform().XincrementQ();
        return String.valueOf(d);
    }
    public static String XoriginQ(SCPIParam param){
        double d=Command.get().getWaveform().XoriginQ();
        return String.valueOf(d);
    }
    public static String XreferenceQ(SCPIParam param){
        double d=Command.get().getWaveform().XreferenceQ();
        return String.valueOf(d);
    }
    public static String YincrementQ(SCPIParam param){
        double d=Command.get().getWaveform().YincrementQ();
        return String.valueOf(d);
    }
    public static String YoriginQ(SCPIParam param){
        double d=Command.get().getWaveform().YOriginQ();
        return String.valueOf(d);
    }
    public static String YReferenceQ(SCPIParam param){
        double d=Command.get().getWaveform().YReferenceQ();
        return String.valueOf(d);
    }

}
