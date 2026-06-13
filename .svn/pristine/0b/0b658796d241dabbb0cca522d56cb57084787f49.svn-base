package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Timeout {
//      new SCPICommandStruct(":TRIGger:TIMeout:SOURce","SCPI_Trigger_Timeout","Source"),//设置超时触发的触发源
//            new SCPICommandStruct(":TRIGger:TIMeout:SOURce?","SCPI_Trigger_Timeout","SourceQ"),//查询超时触发的触发源
//            new SCPICommandStruct(":TRIGger:TIMeout:POLarity","SCPI_Trigger_Timeout","Polarity"),//设置超时触发极性
//            new SCPICommandStruct(":TRIGger:TIMeout:POLarity?","SCPI_Trigger_Timeout","PolarityQ"),//查询超时触发极性
//            new SCPICommandStruct(":TRIGger:TIMeout:TIME","SCPI_Trigger_Timeout","Time"),//设置超时触发的超时时间
//            new SCPICommandStruct(":TRIGger:TIMeout:TIME?","SCPI_Trigger_Timeout","TimeQ"),//查询超时触发的超时时间

    public static void Source(SCPIParam param) {
        Command.get().getTrigger_timeout().Source(param.iParam1, true);
    }

    public static String SourceQ(SCPIParam param) {
        int i=Command.get().getTrigger_timeout().SourceQ();
        return ToolsSCPI.getCh(i);
    }

    public static void Polarity(SCPIParam param) {
        Command.get().getTrigger_timeout().Polarity(param.iParam1, true);
    }

    public static String PolarityQ(SCPIParam param) {
        int i=Command.get().getTrigger_timeout().PolarityQ();
        return ToolsSCPI.getTriggerTimeoutPolarity(i);
    }

    public static void Time(SCPIParam param) {
        Command.get().getTrigger_timeout().Time(param.dParam1, true);
    }

    public static String TimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_timeout().TimeQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Level(SCPIParam param){
        Command.get().getTrigger_timeout().Level(param.dParam1, true);
    }
    public static String LevelQ(SCPIParam param){
        double d=Command.get().getTrigger_timeout().LevelQ();
        return ToolsSCPI.getDouble(d);
    }

}
