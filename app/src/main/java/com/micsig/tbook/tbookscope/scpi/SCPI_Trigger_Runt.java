package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Runt {
//    //trigger dwart换名为trigger runt 2016.12.8
//            new SCPICommandStruct(":TRIGger:RUNT:SOURce","SCPI_Trigger_Runt","Source"),//设置矮脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:RUNT:SOURce?","SCPI_Trigger_Runt","SourceQ"),//查询矮脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:RUNT:POLarity","SCPI_Trigger_Runt","Polarity"),//设置矮脉宽触发的脉冲极性
//            new SCPICommandStruct(":TRIGger:RUNT:POLarity?","SCPI_Trigger_Runt","PolarityQ"),//查询矮脉宽触发的脉冲极性
//            new SCPICommandStruct(":TRIGger:RUNT:CONDition","SCPI_Trigger_Runt","Condition"),//设置矮脉宽触发的脉宽限制条件
//            new SCPICommandStruct(":TRIGger:RUNT:CONDition?","SCPI_Trigger_Runt","ConditionQ"),//查询矮脉宽触发的脉宽限制条件
//            new SCPICommandStruct(":TRIGger:RUNT:HTIMe","SCPI_Trigger_Runt","HTime"),//设置矮脉宽触发时的时间上限
//            new SCPICommandStruct(":TRIGger:RUNT:HTIMe?","SCPI_Trigger_Runt","HTimeQ"),//查询矮脉宽触发时的时间上限
//            new SCPICommandStruct(":TRIGger:RUNT:LTIMe","SCPI_Trigger_Runt","LTime"),//设置矮脉宽触发时的时间下限
//            new SCPICommandStruct(":TRIGger:RUNT:LTIMe?","SCPI_Trigger_Runt","LTimeQ"),//查询矮脉宽触发时的时间下限
//            new SCPICommandStruct(":TRIGger:RUNT:BTIMe","SCPI_Trigger_Runt","BTime"),//设置矮脉宽触发时的时间区间
//            new SCPICommandStruct(":TRIGger:RUNT:BTIMe?","SCPI_Trigger_Runt","BTimeQ"),//查询矮脉宽触发时的时间上限或下限
//            new SCPICommandStruct(":TRIGger:RUNT:HLEVel","SCPI_Trigger_Runt","HLevel"),//设置矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:RUNT:PLUS:HLEVel","SCPI_Trigger_Runt","Plus_HLevel"),//设置矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:RUNT:HLEVel?","SCPI_Trigger_Runt","HLevelQ"),//查询矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:RUNT:LLEVel","SCPI_Trigger_Runt","LLevel"),//设置矮脉宽触发时的低电平
//            new SCPICommandStruct(":TRIGger:RUNT:PLUS:LLEVel","SCPI_Trigger_Runt","Plus_LLevel"),//设置矮脉宽触发时的低电平
//            new SCPICommandStruct(":TRIGger:RUNT:LLEVel?","SCPI_Trigger_Runt","LLevelQ"),//查询矮脉宽触发时的低电平

    public static void Source(SCPIParam param) {
        Command.get().getTrigger_runt().Source(param.iParam1, true);
    }

    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getTrigger_dwart().SourceQ();
        return ToolsSCPI.getCh(i);
    }

    public static void Polarity(SCPIParam param) {
        Command.get().getTrigger_runt().Polarity(param.iParam1, true);
    }

    public static String PolarityQ(SCPIParam param) {
        int i=Command.get().getTrigger_dwart().PolarityQ();
        return ToolsSCPI.getTriggerPulsePolarity(i);
    }

    public static void Condition(SCPIParam param) {
        Command.get().getTrigger_runt().Condition(param.iParam1, true);
    }

    public static String ConditionQ(SCPIParam param) {
        int i= Command.get().getTrigger_dwart().ConditionQ();
        return ToolsSCPI.getTriggerDwartCondition(i);
    }

    public static void HTime(SCPIParam param) {
        Command.get().getTrigger_runt().HTime(param.dParam1, true);
    }

    public static String HTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().HTimeQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void LTime(SCPIParam param) {
        Command.get().getTrigger_runt().LTime(param.dParam1, true);
    }

    public static String LTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().LTimeQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void BTime(SCPIParam param) {
        Command.get().getTrigger_runt().BTime(param.dParam1,param.dParam2, true);
    }

    public static String BTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().BTimeQ(param.iParam1);
        return ToolsSCPI.getDouble(d);
    }

    public static void HLevel(SCPIParam param) {
        Command.get().getTrigger_runt().HLevel(param.dParam1, true);
    }

    public static void Plus_HLevel(SCPIParam param) {
        Command.get().getTrigger_runt().Plus_HLevel(param.iParam1, true);
    }

    public static String HLevelQ(SCPIParam param) {
        double d= Command.get().getTrigger_dwart().HLevelQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void LLevel(SCPIParam param) {
        Command.get().getTrigger_runt().LLevel(param.dParam1, true);
    }

    public static void Plus_LLevel(SCPIParam param) {
        Command.get().getTrigger_runt().Plus_LLevel(param.iParam1, true);
    }

    public static String LLevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().LLevelQ();
        return ToolsSCPI.getDouble(d);
    }
}
