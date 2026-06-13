package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.tools.Tools;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Uart {

//     new SCPICommandStruct(":TRIGger:UART:SOURce","SCPI_Trigger_Uart","Source"),//设置UART触发的触发源
//            new SCPICommandStruct(":TRIGger:UART:SOURce?","SCPI_Trigger_Uart","SourceQ"),//查询UART触发的触发源
//            new SCPICommandStruct(":TRIGger:UART:TYPE","SCPI_Trigger_Uart","Type"),//设置UART触发的触发条件
//            new SCPICommandStruct(":TRIGger:UART:TYPE?","SCPI_Trigger_Uart","TypeQ"),//查询UART触发的触发条件
//            new SCPICommandStruct(":TRIGger:UART:RELation","SCPI_Trigger_Uart","Relation"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发关系
//            new SCPICommandStruct(":TRIGger:UART:RELation?","SCPI_Trigger_Uart","RelationQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发关系
//            new SCPICommandStruct(":TRIGger:UART:DATA","SCPI_Trigger_Uart","Data"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发数据。
//            new SCPICommandStruct(":TRIGger:UART:DATA?","SCPI_Trigger_Uart","DataQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发数据。
//            new SCPICommandStruct(":TRIGger:UART:LEVel","SCPI_Trigger_Uart","Level"),//设置UART触发时的触发电平
//            new SCPICommandStruct(":TRIGger:UART:LEVel?","SCPI_Trigger_Uart","LevelQ"),//查询UART触发时的触发电平

    public static void Source(SCPIParam param){
        Command.get().getTrigger_uart().setSource(param.iParam1, param.iParam2, true);
    }
    public static String SourceQ(SCPIParam param){
        int i=Command.get().getTrigger_uart().getSource(param.iParam1);
        return String.valueOf(i);
    }
    public static void Type(SCPIParam param){
        int condition=Command.get().getTrigger_uart().getCondition(param.iParam1) ;
        int number=Command.get().getTrigger_uart().getNumber(param.iParam1);
        Command.get().getTrigger_uart().setType(param.iParam1, param.iParam2,condition,number,true );
    }
    public static String TypeQ(SCPIParam param){
        int i= Command.get().getTrigger_uart().getType(param.iParam1);
        return  ToolsSCPI.getuartTriggerType(i);
    }
    public static void Relation(SCPIParam param){
        int type=Command.get().getTrigger_uart().getType(param.iParam1);
        int number=Command.get().getTrigger_uart().getNumber(param.iParam1);
        Command.get().getTrigger_uart().setType(param.iParam1, type, param.iParam2, number,true);
    }
    public static String RelationQ(SCPIParam param){
        int i= Command.get().getTrigger_uart().getCondition(param.iParam1);
        return ToolsSCPI.getSerialTriggerCondition(i);
    }
    public static void Data(SCPIParam param){
        int type=Command.get().getTrigger_uart().getType(param.iParam1);
        int condition=Command.get().getTrigger_uart().getCondition(param.iParam1);
        int data= Tools.HexStringToInt(param.sParam1);

        Command.get().getTrigger_uart().setType(param.iParam1, type,condition, data, true);
    }
    public static String DataQ(SCPIParam param){
        int i=Command.get().getTrigger_uart().getNumber(param.iParam1);
        return Integer.toHexString(i);
    }
    public static void Level(SCPIParam param){
        Command.get().getTrigger_uart().setLevel(param.iParam1, param.dParam1, true);
    }
    public static String LevelQ(SCPIParam param){
        double i=Command.get().getTrigger_uart().getLevel(param.iParam1);
        return String.valueOf(i);
    }

}
