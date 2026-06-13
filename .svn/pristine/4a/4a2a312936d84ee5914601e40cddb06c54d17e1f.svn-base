package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Can {
//    new SCPICommandStruct(":TRIGger:CAN:SOURce","SCPI_Trigger_Can","Source"),//设置CAN触发的触发源
//            new SCPICommandStruct(":TRIGger:CAN:SOURce?","SCPI_Trigger_Can","SourceQ"),//查询CAN触发的触发源
//            new SCPICommandStruct(":TRIGger:CAN:TYPE","SCPI_Trigger_Can","Type"),//设置CAN触发的触发条件
//            new SCPICommandStruct(":TRIGger:CAN:TYPE?","SCPI_Trigger_Can","TypeQ"),//查询CAN触发的触发条件
//            new SCPICommandStruct(":TRIGger:CAN:ID","SCPI_Trigger_Can","Id"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，设置CAN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:CAN:ID?","SCPI_Trigger_Can","IdQ"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，查询CAN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:CAN:DLC","SCPI_Trigger_Can","DLC"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的DLC值
//            new SCPICommandStruct(":TRIGger:CAN:DLC?","SCPI_Trigger_Can","DLCQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的DLC值
//            new SCPICommandStruct(":TRIGger:CAN:DATA","SCPI_Trigger_Can","Data"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的触发数据值
//            new SCPICommandStruct(":TRIGger:CAN:DATA?","SCPI_Trigger_Can","DataQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的触发数据值
//            new SCPICommandStruct(":TRIGger:CAN:LEVel","SCPI_Trigger_Can","Level"),//设置CAN触发时的触发电平
//            new SCPICommandStruct(":TRIGger:CAN:LEVel?","SCPI_Trigger_Can","LevelQ"),//查询CAN触发时的触发电平

    public static void Source(SCPIParam param){}
    public static void SourceQ(SCPIParam param){}


    public static void Type(SCPIParam param){
        int id=Command.get().getTrigger_can().getId(param.iParam1);
        int dlc=Command.get().getTrigger_can().getDlc(param.iParam1);
        long data=Command.get().getTrigger_can().getData(param.iParam1);
        Command.get().getTrigger_can().setType(param.iParam1, param.iParam2, id,dlc,data,true);
    }
    public static String TypeQ(SCPIParam param){
        int i= Command.get().getTrigger_can().getType(param.iParam1);
        return ToolsSCPI.getCanTriggerType(i);
    }
    public static void Id(SCPIParam param){
        int type=Command.get().getTrigger_can().getType(param.iParam1);
        //int id=Command.get().getTrigger_can().getId(param.iParam1);
        int dlc=Command.get().getTrigger_can().getDlc(param.iParam1);
        long data=Command.get().getTrigger_can().getData(param.iParam1);
        long id= SerialsUtils.toDLong(param.sParam1.trim(),16);
        Command.get().getTrigger_can().setType(param.iParam1, type, id, dlc,data,true);
    }
    public static String IdQ(SCPIParam param){
        int i= Command.get().getTrigger_can().getId(param.iParam1);
        return Integer.toHexString(i);
    }

    //dlc的值为10进制进行传送与保存
    public static void DLC(SCPIParam param){
        int type=Command.get().getTrigger_can().getType(param.iParam1);
        int id=Command.get().getTrigger_can().getId(param.iParam1);
        //int dlc=Command.get().getTrigger_can().getDlc(param.iParam1);
        long data=Command.get().getTrigger_can().getData(param.iParam1);
        int dlc=ToolsSCPI.getCanDlc(param.iParam2);
        Command.get().getTrigger_can().setType(param.iParam1, type, id, dlc, data,true);
    }
    public static String DLCQ(SCPIParam param){
        int i=Command.get().getTrigger_can().getDlc(param.iParam1);
        return String.valueOf(i);
        //return Integer.toHexString(i);
    }

    public static void Data(SCPIParam param){
        int type=Command.get().getTrigger_can().getType(param.iParam1);
        int id=Command.get().getTrigger_can().getId(param.iParam1);
        int dlc=Command.get().getTrigger_can().getDlc(param.iParam1);
        //long data=Command.get().getTrigger_can().getData(param.iParam1);
        long data=SerialsUtils.toDLong(param.sParam1.trim(),16);
        Command.get().getTrigger_can().setType(param.iParam1,type, id, dlc, data, true);
    }
    public static String DataQ(SCPIParam param){
        long i=Command.get().getTrigger_can().getData(param.iParam1);
        return Long.toHexString(i);
    }
    public static void Level(SCPIParam param){
        Command.get().getTrigger_can().setLevel(param.iParam1, param.dParam1, true);
    }
    public static String LevelQ(SCPIParam param){
        double d= Command.get().getTrigger_can().getLevel(param.iParam1);
        return String.valueOf(d);
    }
}
