package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Slope {
//     new SCPICommandStruct(":TRIGger:SLOPe:SOURce","SCPI_Trigger_Slope","Source"),//设置斜率触发的触发源
//            new SCPICommandStruct(":TRIGger:SLOPe:SOURce?","SCPI_Trigger_Slope","SourceQ"),//查询斜率触发的触发源
//            new SCPICommandStruct(":TRIGger:SLOPe:EDGE","SCPI_Trigger_Slope","Edge"),//设置斜率触发沿
//            new SCPICommandStruct(":TRIGger:SLOPe:EDGE?","SCPI_Trigger_Slope","EdgeQ"),//查询斜率触发沿
//            new SCPICommandStruct(":TRIGger:SLOPe:CONDition","SCPI_Trigger_Slope","Condition"),//设置斜率触发的限制条件
//            new SCPICommandStruct(":TRIGger:SLOPe:CONDition?","SCPI_Trigger_Slope","ConditionQ"),//查询斜率触发的限制条件
//            new SCPICommandStruct(":TRIGger:SLOPe:HTIMe","SCPI_Trigger_Slope","HTime"),//设置斜率触发时的时间上限
//            new SCPICommandStruct(":TRIGger:SLOPe:HTIMe?","SCPI_Trigger_Slope","HTimeQ"),//查询斜率触发时的时间上限
//            new SCPICommandStruct(":TRIGger:SLOPe:LTIMe","SCPI_Trigger_Slope","LTime"),//设置斜率触发时的时间下限
//            new SCPICommandStruct(":TRIGger:SLOPe:LTIMe?","SCPI_Trigger_Slope","LTimeQ"),//查询斜率触发时的时间下限
//            new SCPICommandStruct(":TRIGger:SLOPe:BTIMe","SCPI_Trigger_Slope","BTime"),//设置斜率触发时的时间区间
//            new SCPICommandStruct(":TRIGger:SLOPe:BTIMe?","SCPI_Trigger_Slope","BTimeQ"),//查询斜率触发时的时间上限或下限
//            new SCPICommandStruct(":TRIGger:SLOPe:HLEVel","SCPI_Trigger_Slope","HLevel"),//设置斜率触发时的高电平
//            new SCPICommandStruct(":TRIGger:SLOPe:PLUS:HLEVel","SCPI_Trigger_Slope","Plus_HLevel"),//设置斜率触发时的高电平
//            new SCPICommandStruct(":TRIGger:SLOPe:HLEVel?","SCPI_Trigger_Slope","HLevelQ"),//查询斜率触发时的高电平
//            new SCPICommandStruct(":TRIGger:SLOPe:LLEVel","SCPI_Trigger_Slope","LLevel"),//设置斜率触发时的低电平
//            new SCPICommandStruct(":TRIGger:SLOPe:PLUS:LLEVel","SCPI_Trigger_Slope","Plus_LLevel"),//设置斜率触发时的低电平
//            new SCPICommandStruct(":TRIGger:SLOPe:LLEVel?","SCPI_Trigger_Slope","LLevelQ"),//查询斜率触发时的低电平

    public static void Source(SCPIParam param) {
        Command.get().getTrigger_slope().Source(param.iParam1, true);
    }

    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getTrigger_slope().SourceQ();
        return ToolsSCPI.getCh(i);
    }

    public static void Edge(SCPIParam param) {
        Command.get().getTrigger_slope().Edge(param.iParam1, true);
    }

    public static String EdgeQ(SCPIParam param) {
        int i= Command.get().getTrigger_slope().EdgeQ();
        return ToolsSCPI.getTriggerSlopeEdge(i);
    }

    public static void Condition(SCPIParam param) {
        Command.get().getTrigger_slope().Condition(param.iParam1, true);
    }

    public static String ConditionQ(SCPIParam param) {
        int i=Command.get().getTrigger_slope().ConditionQ();
        return ToolsSCPI.getTriggerSlopeCondition(i);
    }

    public static void HTime(SCPIParam param) {
        Command.get().getTrigger_slope().HTime(param.dParam1, true);
    }

    public static String HTimeQ(SCPIParam param) {
        double d= Command.get().getTrigger_slope().HTimeQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void LTime(SCPIParam param) {
        Command.get().getTrigger_slope().LTime(param.dParam1, true);
    }

    public static String LTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_slope().LTimeQ();
        return ToolsSCPI.getDouble(d);

    }

    public static void BTime(SCPIParam param) {
        Command.get().getTrigger_slope().BTime(param.dParam1,param.dParam2, true);
    }

    public static String BTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_slope().BTimeQ(param.iParam1);
        return ToolsSCPI.getDouble(d);
    }

    public static void HLevel(SCPIParam param) {
        Command.get().getTrigger_slope().HLevel(param.dParam1, true);
    }

    public static void Plus_HLevel(SCPIParam param) {
        Command.get().getTrigger_slope().Plus_HLevel(param.iParam1, true);
    }

    public static String HLevelQ(SCPIParam param) {
        double d= Command.get().getTrigger_slope().HLevelQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void LLevel(SCPIParam param) {
        Command.get().getTrigger_slope().LLevel(param.dParam1, true);
    }

    public static void Plus_LLevel(SCPIParam param) {
        Command.get().getTrigger_slope().Plus_LLevel(param.iParam1, true);
    }

    public static String LLevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_slope().LLevelQ();
        return ToolsSCPI.getDouble(d);
    }

}
