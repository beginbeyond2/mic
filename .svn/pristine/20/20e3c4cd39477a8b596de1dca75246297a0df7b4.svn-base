package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Pulse {
//     new SCPICommandStruct(":TRIGger:PULSe:SOURce","SCPI_Trigger_Pulse","Source"),//选择脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:PULSe:SOURce?","SCPI_Trigger_Pulse","SourceQ"),//查询脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:PULSe:POLarity","SCPI_Trigger_Pulse","Polarity"),//设置脉宽触发的极性
//            new SCPICommandStruct(":TRIGger:PULSe:POLarity?","SCPI_Trigger_Pulse","PolarityQ"),//查询脉宽触发的极性
//            new SCPICommandStruct(":TRIGger:PULSe:WIDTh","SCPI_Trigger_Pulse","Width"),//设置脉宽触发时的脉冲宽度值
//            new SCPICommandStruct(":TRIGger:PULSe:WIDTh?","SCPI_Trigger_Pulse","WidthQ"),//查询脉宽触发时的脉冲宽度值
//            new SCPICommandStruct(":TRIGger:PULSe:CONDition","SCPI_Trigger_Pulse","Condition"),//设置脉宽触发条件
//            new SCPICommandStruct(":TRIGger:PULSe:CONDition?","SCPI_Trigger_Pulse","ConditionQ"),//查询脉宽触发条件
//            new SCPICommandStruct(":TRIGger:PULSe:LEVel","SCPI_Trigger_Pulse","Level"),//设置脉宽触发时的触发电平
//            new SCPICommandStruct(":TRIGger:PULSe:PLUS:LEVel","SCPI_Trigger_Pulse","Plus_Level"),//设置脉宽触发时的触发电平
//            new SCPICommandStruct(":TRIGger:PULSe:LEVel?","SCPI_Trigger_Pulse","LevelQ"),//查询脉宽触发时的触发电平

    public static void Source(SCPIParam param) {
        Command.get().getTrigger_pulse().Source(param.iParam1,true);
    }

    public static String SourceQ(SCPIParam param) {
        int i=Command.get().getTrigger_pulse().SourceQ();
        return ToolsSCPI.getCh(i);
    }

    public static void Polarity(SCPIParam param) {
        Command.get().getTrigger_pulse().Polarity(param.iParam1,true);
    }

    public static String PolarityQ(SCPIParam param) {
        int i=Command.get().getTrigger_pulse().PolarityQ();
        return ToolsSCPI.getTriggerPulsePolarity(i);
    }

    public static void Width(SCPIParam param) {
        Command.get().getTrigger_pulse().Width(param.dParam1,true);
    }

    public static String WidthQ(SCPIParam param) {
        double d= Command.get().getTrigger_pulse().WidthQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Condition(SCPIParam param) {
        Command.get().getTrigger_pulse().Condition(param.iParam1,true);
    }

    public static String ConditionQ(SCPIParam param) {
        int i=Command.get().getTrigger_pulse().ConditionQ();
        return ToolsSCPI.getTriggerPulseCondition(i);
    }

    public static void Level(SCPIParam param) {
        Command.get().getTrigger_pulse().Level(param.dParam1,true);
    }

    public static void Plus_Level(SCPIParam param) {
        Command.get().getTrigger_pulse().Plus_Level(param.iParam1,true);
    }

    public static String LevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_pulse().LevelQ();
        return ToolsSCPI.getDouble(d);
    }

}
