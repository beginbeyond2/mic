package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Video {
//     new SCPICommandStruct(":TRIGger:VIDeo:SOURce","SCPI_Trigger_Video","Source"),//设置视频触发的触发源
//            new SCPICommandStruct(":TRIGger:VIDeo:SOURce?","SCPI_Trigger_Video","SourceQ"),//查询视频触发的触发源
//            new SCPICommandStruct(":TRIGger:VIDeo:POLarity","SCPI_Trigger_Video","Polarity"),//设置视频触发的极性
//            new SCPICommandStruct(":TRIGger:VIDeo:POLarity?","SCPI_Trigger_Video","PolarityQ"),//查询视频触发的极性
//            new SCPICommandStruct(":TRIGger:VIDeo:STANdard","SCPI_Trigger_Video","Standard"),//设置视频触发时的视频标准
//            new SCPICommandStruct(":TRIGger:VIDeo:STANdard?","SCPI_Trigger_Video","StandardQ"),//查询视频触发时的视频标准
//            new SCPICommandStruct(":TRIGger:VIDeo:AMODe","SCPI_Trigger_Video","Amode"),//设置触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:AMODe?","SCPI_Trigger_Video","AmodeQ"),//查询触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:BMODe","SCPI_Trigger_Video","Bmode"),//设置触发标准为720P、1080P时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:BMODe?","SCPI_Trigger_Video","BmodeQ"),//查询触发标准为720P、1080P时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:AFRequence","SCPI_Trigger_Video","Afrequence"),//设置触发标准为720P、1080I时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:AFRequence?","SCPI_Trigger_Video","AfrequenceQ"),//查询触发标准为720P、1080I时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:BFRequence","SCPI_Trigger_Video","Bfrequence"),//设置触发标准为1080P时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:BFRequence?","SCPI_Trigger_Video","BfrequenceQ"),//查询触发标准为1080P时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:LINE","SCPI_Trigger_Video","Line"),
//            new SCPICommandStruct(":TRIGger:VIDeo:LINE?","SCPI_Trigger_Video","LineQ"),

    public static void Source(SCPIParam param) {
        Command.get().getTrigger_video().Source(param.iParam1, true);
    }

    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getTrigger_video().SourceQ();
        return ToolsSCPI.getCh(i);
    }

    public static void Polarity(SCPIParam param) {
        Command.get().getTrigger_video().Polarity(param.iParam1, true);
    }

    public static String PolarityQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().PolarityQ();
        return ToolsSCPI.getTriggerVideoPolarity(i);
    }

    public static void Standard(SCPIParam param) {
        Command.get().getTrigger_video().Standard(param.iParam1, true);
    }

    public static String StandardQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().StandardQ();
        return ToolsSCPI.getTriggerVideoStandard(i);
    }

    public static void Amode(SCPIParam param) {
        Command.get().getTrigger_video().Amode(param.iParam1, true);
    }

    public static String AmodeQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().AmodeQ();
        int standard=Command.get().getTrigger_video().StandardQ();
        if ( standard==3 || standard==5){
            return ToolsSCPI.getTriggerVideoBmode(i);
        }else {
            return ToolsSCPI.getTriggerVideoAmode(i);
        }
    }

    public static void Bmode(SCPIParam param) {
        Command.get().getTrigger_video().Bmode(param.iParam1, true);
    }

    public static String BmodeQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().BmodeQ();
        return ToolsSCPI.getTriggerVideoBmode(i);
    }

    public static void Afrequence(SCPIParam param) {
        Command.get().getTrigger_video().Afrequence(param.iParam1, true);
    }

    public static String AfrequenceQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().AfrequenceQ();
        return ToolsSCPI.getTriggerVideoBfrequence(i);
    }

    public static void Bfrequence(SCPIParam param) {
        Command.get().getTrigger_video().Bfrequence(param.iParam1, true);
    }

    public static String BfrequenceQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().BfrequenceQ();
        return ToolsSCPI.getTriggerVideoBfrequence(i);
    }

    public static void Line(SCPIParam param){
        Command.get().getTrigger_video().Line(param.iParam1, true);
    }
    public static String LineQ(SCPIParam param){
        int i=Command.get().getTrigger_video().LineQ();
        return ToolsSCPI.getInt(i);
    }
}
