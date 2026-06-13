package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Logic {
//     new SCPICommandStruct(":TRIGger:LOGic:STATus","SCPI_Trigger_Logic","Status"),//设置逻辑触发中通道的逻辑状态
//            new SCPICommandStruct(":TRIGger:LOGic:STATus?","SCPI_Trigger_Logic","StatusQ"),//查询逻辑触发中通道的逻辑状态
//            new SCPICommandStruct(":TRIGger:LOGic:FUNCtion","SCPI_Trigger_Logic","Function"),//设置逻辑触发的比较函数
//            new SCPICommandStruct(":TRIGger:LOGic:FUNCtion?","SCPI_Trigger_Logic","FunctionQ"),//查询逻辑触发的比较函数
//            new SCPICommandStruct(":TRIGger:LOGic:CONDition","SCPI_Trigger_Logic","Condition"),//设置逻辑触发条件
//            new SCPICommandStruct(":TRIGger:LOGic:CONDition?","SCPI_Trigger_Logic","ConditionQ"),//查询逻辑触发条件
//            new SCPICommandStruct(":TRIGger:LOGic:TIME","SCPI_Trigger_Logic","Time"),//设置触发逻辑时间
//            new SCPICommandStruct(":TRIGger:LOGic:TIME?","SCPI_Trigger_Logic","TimeQ"),//查询触发逻辑时间
//            new SCPICommandStruct(":TRIGger:LOGic:LEVel","SCPI_Trigger_Logic","Level"),//设置逻辑触发时的各通道触发电平
//            new SCPICommandStruct(":TRIGger:LOGic:PLUS:LEVel","SCPI_Trigger_Logic","Plus_Level"),//设置逻辑触发时的各通道触发电平
//            new SCPICommandStruct(":TRIGger:LOGic:LEVel?","SCPI_Trigger_Logic","LevelQ"),//查询逻辑触发时的各通道触发电平

    public static void Status(SCPIParam param) {
        Command.get().getTrigger_logic().Status(param.iParam1, param.iParam2, true);
    }

    public static String StatusQ(SCPIParam param) {
        int i= Command.get().getTrigger_logic().StatusQ(param.iParam1);
        return ToolsSCPI.getTriggerLogicStatus(i);
    }

    public static void Function(SCPIParam param) {
        Command.get().getTrigger_logic().Function(param.iParam1, true);
    }

    public static String FunctionQ(SCPIParam param) {
        int i=Command.get().getTrigger_logic().FunctionQ();
        return ToolsSCPI.getTriggerLogicFunction(i);
    }

    public static void Condition(SCPIParam param) {
        Command.get().getTrigger_logic().Condition(param.iParam1, true);
    }

    public static String ConditionQ(SCPIParam param) {
        int i= Command.get().getTrigger_logic().ConditionQ();
        return ToolsSCPI.getTriggerLogicCondition(i);
    }

    public static void HTime(SCPIParam param) {
        Command.get().getTrigger_logic().HTime(param.dParam1, true);
    }

    public static String HTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().HTimeQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void LTime(SCPIParam param) {
        Command.get().getTrigger_logic().LTime(param.dParam1, true);
    }

    public static String LTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().LTimeQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void BTime(SCPIParam param) {
        Command.get().getTrigger_logic().BTime(param.dParam1,param.dParam2, true);
    }

    public static String BTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().BTimeQ(param.iParam1);
        return ToolsSCPI.getDouble(d);
    }

    public static void Time(SCPIParam param) {
        Command.get().getTrigger_logic().Time(param.dParam1,  true);
    }

    public static String TimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().TimeQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Level(SCPIParam param) {
        Command.get().getTrigger_logic().Level(param.iParam1, param.dParam1, true);
    }

    public static void Plus_Level(SCPIParam param) {
        Command.get().getTrigger_logic().Plus_Level(param.iParam1, param.iParam2, true);
    }

    public static String LevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().LevelQ(param.iParam1);
        return ToolsSCPI.getDouble(d);
    }

}
