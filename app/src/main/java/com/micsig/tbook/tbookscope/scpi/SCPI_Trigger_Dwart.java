package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Dwart {

//     new SCPICommandStruct(":TRIGger:DWARt:SOURce","SCPI_Trigger_Dwart","Source"),//设置矮脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:DWARt:SOURce?","SCPI_Trigger_Dwart","SourceQ"),//查询矮脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:DWARt:POLarity","SCPI_Trigger_Dwart","Polarity"),//设置矮脉宽触发的脉冲极性
//            new SCPICommandStruct(":TRIGger:DWARt:POLarity?","SCPI_Trigger_Dwart","PolarityQ"),//查询矮脉宽触发的脉冲极性
//            new SCPICommandStruct(":TRIGger:DWARt:CONDition","SCPI_Trigger_Dwart","Condition"),//设置矮脉宽触发的脉宽限制条件
//            new SCPICommandStruct(":TRIGger:DWARt:CONDition?","SCPI_Trigger_Dwart","ConditionQ"),//查询矮脉宽触发的脉宽限制条件
//            new SCPICommandStruct(":TRIGger:DWARt:HTIMe","SCPI_Trigger_Dwart","HTime"),//设置矮脉宽触发时的时间上限
//            new SCPICommandStruct(":TRIGger:DWARt:HTIMe?","SCPI_Trigger_Dwart","HTimeQ"),//查询矮脉宽触发时的时间上限
//            new SCPICommandStruct(":TRIGger:DWARt:LTIMe","SCPI_Trigger_Dwart","LTime"),//设置矮脉宽触发时的时间下限
//            new SCPICommandStruct(":TRIGger:DWARt:LTIMe?","SCPI_Trigger_Dwart","LTimeQ"),//查询矮脉宽触发时的时间下限
//            new SCPICommandStruct(":TRIGger:DWARt:BTIMe","SCPI_Trigger_Dwart","BTime"),//设置矮脉宽触发时的时间区间
//            new SCPICommandStruct(":TRIGger:DWARt:BTIMe?","SCPI_Trigger_Dwart","BTimeQ"),//查询矮脉宽触发时的时间上限或下限
//            new SCPICommandStruct(":TRIGger:DWARt:HLEVel","SCPI_Trigger_Dwart","HLevel"),//设置矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:DWARt:PLUS:HLEVel","SCPI_Trigger_Dwart","Plus_HLevel"),//设置矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:DWARt:HLEVel?","SCPI_Trigger_Dwart","HLevelQ"),//查询矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:DWARt:LLEVel","SCPI_Trigger_Dwart","LLevel"),//设置矮脉宽触发时的低电平
//            new SCPICommandStruct(":TRIGger:DWARt:PLUS:LLEVel","SCPI_Trigger_Dwart","Plus_LLevel"),//设置矮脉宽触发时的低电平
//            new SCPICommandStruct(":TRIGger:DWARt:LLEVel?","SCPI_Trigger_Dwart","LLevelQ"),//查询矮脉宽触发时的低电平

    public static void Source(SCPIParam param) {
        Command.get().getTrigger_dwart().Source(param.iParam1, true);
    }

    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getTrigger_dwart().SourceQ();
        return ToolsSCPI.getCh(i);
    }

    public static void Polarity(SCPIParam param) {
        Command.get().getTrigger_dwart().Polarity(param.iParam1, true);
    }

    public static String PolarityQ(SCPIParam param) {
        int i=Command.get().getTrigger_dwart().PolarityQ();
        return ToolsSCPI.getTriggerPulsePolarity(i);
    }

    public static void Condition(SCPIParam param) {
        Command.get().getTrigger_dwart().Condition(param.iParam1, true);
    }

    public static String ConditionQ(SCPIParam param) {
        int i= Command.get().getTrigger_dwart().ConditionQ();
        return ToolsSCPI.getTriggerDwartCondition(i);
    }

    public static void HTime(SCPIParam param) {
        Command.get().getTrigger_dwart().HTime(param.dParam1, true);
    }

    public static String HTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().HTimeQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void LTime(SCPIParam param) {
        Command.get().getTrigger_dwart().LTime(param.dParam1, true);
    }

    public static String LTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().LTimeQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void BTime(SCPIParam param) {
        Command.get().getTrigger_dwart().BTime(param.dParam1, param.dParam2, true);
    }

    public static String BTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().BTimeQ(param.iParam1);
        return ToolsSCPI.getDouble(d);
    }

    public static void HLevel(SCPIParam param) {
        Command.get().getTrigger_dwart().HLevel(param.dParam1, true);
    }

    public static void Plus_HLevel(SCPIParam param) {
        Command.get().getTrigger_dwart().Plus_HLevel(param.iParam1, true);
    }

    public static String HLevelQ(SCPIParam param) {
        double d= Command.get().getTrigger_dwart().HLevelQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void LLevel(SCPIParam param) {
        Command.get().getTrigger_dwart().LLevel(param.dParam1, true);
    }

    public static void Plus_LLevel(SCPIParam param) {
        Command.get().getTrigger_dwart().Plus_LLevel(param.iParam1, true);
    }

    public static String LLevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().LLevelQ();
        return ToolsSCPI.getDouble(d);
    }

}
