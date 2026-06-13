package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger {
//     new SCPICommandStruct(":TRIGger:TYPE","SCPI_Trigger","Type"),//选择触发类型
//            new SCPICommandStruct(":TRIGger:TYPE?","SCPI_Trigger","TypeQ"),//查询返回当前使用的触发类型
//            new SCPICommandStruct(":TRIGger:HOLDoff","SCPI_Trigger","HoldOff"),//设置触发释抑时间
//            new SCPICommandStruct(":TRIGger:HOLDoff?","SCPI_Trigger","HoldOffQ"),//查询以科学计数形式返回触发释抑时间
//            new SCPICommandStruct(":TRIGger:MODE","SCPI_Trigger","Mode"),//设置触发方式：自动或普通
//            new SCPICommandStruct(":TRIGger:MODE?","SCPI_Trigger","ModeQ"),//查询触发方式
//            new SCPICommandStruct(":TRIGger:STATus?","SCPI_Trigger","StatusQ"),//查询当前的触发状态

    public static void Type(SCPIParam param) {
        Command.get().getTrigger().Type(param.iParam1, true);
    }

    public static String TypeQ(SCPIParam param) {
        int i= Command.get().getTrigger().TypeQ();
        return ToolsSCPI.getTriggerType(i);
    }

    public static void HoldOff(SCPIParam param) {
        Command.get().getTrigger().HoldOff(param.dParam1,true);
    }

    public static String HoldOffQ(SCPIParam param) {
        double d=Command.get().getTrigger().HoldOffQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Mode(SCPIParam param) {
        Command.get().getTrigger().Mode(param.iParam1,true);
    }

    public static String ModeQ(SCPIParam param) {
        int i=Command.get().getTrigger().ModeQ();
        return ToolsSCPI.getTriggerMode(i);
    }

    public static String StatusQ(SCPIParam param) {
        int i= Command.get().getTrigger().StatusQ();
        return ToolsSCPI.getTriggerStatus(i);
    }
    public static String IsExternalTriggerQ(SCPIParam param){
        return ToolsSCPI.getOpenState(true);
    }
    public static String IsExternalClockQ(SCPIParam param){
        return ToolsSCPI.getOpenState(true);
    }

    public static String HasDialogQ(SCPIParam param){
        boolean b= Command.get().getTrigger().HasDataQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static void DialogSet(SCPIParam param){
        Command.get().getTrigger().SelectData(param.bParam1,true);
    }
}
