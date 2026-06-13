package com.micsig.tbook.tbookscope.scpi;

import android.util.Log;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Edge {
//     new SCPICommandStruct(":TRIGger:EDGE:SOURce","SCPI_Trigger_Edge","Source"),//选择边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:EDGE:SOURce?","SCPI_Trigger_Edge","SourceQ"),//查询边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:EDGE:SLOPe","SCPI_Trigger_Edge","Slope"),//选择边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:EDGE:SLOPe?","SCPI_Trigger_Edge","SlopeQ"),//查询边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:EDGE:LEVel","SCPI_Trigger_Edge","Level"),//设置边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:PLUS:LEVel","SCPI_Trigger_Edge","Plus_Level"),//设置边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:LEVel?","SCPI_Trigger_Edge","LevelQ"),//查询边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:COUPle","SCPI_Trigger_Edge","Couple"),//设置边沿触发耦合方式。
//            new SCPICommandStruct(":TRIGger:EDGE:COUPle?","SCPI_Trigger_Edge","CoupleQ"),//查询边沿触发耦合方式。

    public static void Source(SCPIParam param) {
        Command.get().getTrigger_edge().Source(param.iParam1, true);
    }

    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getTrigger_edge().SourceQ();
        return ToolsSCPI.getChAll(i);
    }

    public static void Slope(SCPIParam param) {
        Command.get().getTrigger_edge().Slope(param.iParam1, true);
    }

    public static String SlopeQ(SCPIParam param) {
        int i=Command.get().getTrigger_edge().SlopeQ();
        return ToolsSCPI.getTriggerEdgeSlope(i);
    }

    public static void Level(SCPIParam param) {
        Command.get().getTrigger_edge().Level(param.dParam1, true);
    }

    public static void Plus_Level(SCPIParam param) {
        Command.get().getTrigger_edge().Plus_Level(param.iParam1, true);
    }

    public static String LevelQ(SCPIParam param) {
        double d= Command.get().getTrigger_edge().LevelQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Couple(SCPIParam param) {
        Command.get().getTrigger_edge().Couple(param.iParam1, true);
    }

    public static String CoupleQ(SCPIParam param) {
        int i=Command.get().getTrigger_edge().CoupleQ();
        return ToolsSCPI.getTriggerEdgeCouple(i);
    }


}
