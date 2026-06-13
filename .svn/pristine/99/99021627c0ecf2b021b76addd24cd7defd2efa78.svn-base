package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Nedge {
//     new SCPICommandStruct(":TRIGger:NEDGe:SOURce","SCPI_Trigger_Nedge","Source"),//设置第N边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:NEDGe:SOURce?","SCPI_Trigger_Nedge","SourceQ"),//查询第N边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe","SCPI_Trigger_Nedge","Slope"),//设置第N边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe?","SCPI_Trigger_Nedge","SlopeQ"),//查询第N边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:NEDGe:IDLE","SCPI_Trigger_Nedge","Idle"),//设置第N边沿触发中开始边沿计数之前的空闲时间
//            new SCPICommandStruct(":TRIGger:NEDGe:IDLE?","SCPI_Trigger_Nedge","IdleQ"),//查询第N边沿触发中开始边沿计数之前的空闲时间
//            new SCPICommandStruct(":TRIGger:NEDGe:EDGE","SCPI_Trigger_Nedge","Edge"),//设置第N边沿触发的N的数值
//            new SCPICommandStruct(":TRIGger:NEDGe:EDGE?","SCPI_Trigger_Nedge","EdgeQ"),//查询第N边沿触发的N的数值
//            new SCPICommandStruct(":TRIGger:NEDGe:LEVel","SCPI_Trigger_Nedge","Level"),//设置第N边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:NEDGe:PLUS:LEVel","SCPI_Trigger_Nedge","Plus_Level"),//设置第N边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:NEDGe:LEVel?","SCPI_Trigger_Nedge","LevelQ"),//查询第N边沿触发时的触发电平

    public static void Source(SCPIParam param) {
        Command.get().getTrigger_nedge().Source(param.iParam1, true);
    }

    public static String SourceQ(SCPIParam param) {
        int i=Command.get().getTrigger_nedge().SourceQ();
        return ToolsSCPI.getCh(i);
    }

    public static void Slope(SCPIParam param) {
        Command.get().getTrigger_nedge().Slope(param.iParam1, true);
    }

    public static String SlopeQ(SCPIParam param) {
        int i=Command.get().getTrigger_nedge().SlopeQ();
        return ToolsSCPI.getTriggerNedgeSlope(i);
    }

    public static void Idle(SCPIParam param) {
        Command.get().getTrigger_nedge().Idle(param.dParam1, true);
    }

    public static String IdleQ(SCPIParam param) {
        double d=Command.get().getTrigger_nedge().IdleQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Edge(SCPIParam param) {
        Command.get().getTrigger_nedge().Edge(param.iParam1, true);
    }

    public static String EdgeQ(SCPIParam param) {
        int i=Command.get().getTrigger_nedge().EdgeQ();
        return ToolsSCPI.getInt(i);
    }

    public static void Level(SCPIParam param) {
        Command.get().getTrigger_nedge().Level(param.dParam1, true);
    }

    public static void Plus_Level(SCPIParam param) {
        Command.get().getTrigger_nedge().Plus_Level(param.iParam1, true);
    }

    public static String LevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_nedge().LevelQ();
        return ToolsSCPI.getDouble(d);
    }

}
