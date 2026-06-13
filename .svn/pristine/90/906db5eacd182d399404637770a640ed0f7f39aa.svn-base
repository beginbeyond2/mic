package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.tools.Tools;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_IIC {
//     new SCPICommandStruct(":TRIGger:IIC:SOURce","SCPI_Trigger_IIC","Source"),//设置IIC触发的触发源
//            new SCPICommandStruct(":TRIGger:IIC:SOURce?","SCPI_Trigger_IIC","SourceQ"),//查询IIC触发的触发源
//            new SCPICommandStruct(":TRIGger:IIC:TYPE","SCPI_Trigger_IIC","Type"),//设置IIC触发的触发类型
//            new SCPICommandStruct(":TRIGger:IIC:TYPE?","SCPI_Trigger_IIC","TypeQ"),//查询IIC触发的触发类型
//            new SCPICommandStruct(":TRIGger:IIC:ADDRess","SCPI_Trigger_IIC","Address"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，设置IIC总线触发的触发地址
//            new SCPICommandStruct(":TRIGger:IIC:ADDRess?","SCPI_Trigger_IIC","AddressQ"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，查询IIC总线触发的触发地址
//            new SCPICommandStruct(":TRIGger:IIC:RELation","SCPI_Trigger_IIC","Relation"),//当IIC触发条件为RDATa时，设置IIC总线触发的触发关系
//            new SCPICommandStruct(":TRIGger:IIC:RELation?","SCPI_Trigger_IIC","RelationQ"),//当IIC触发条件为RDATa时，查询IIC总线触发的触发关系
//            new SCPICommandStruct(":TRIGger:IIC:DATA","SCPI_Trigger_IIC","Data"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，设置IIC总线触发的触发数据
//            new SCPICommandStruct(":TRIGger:IIC:DATA?","SCPI_Trigger_IIC","DataQ"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，查询IIC总线触发的触发数据
//            new SCPICommandStruct(":TRIGger:IIC:LEVel","SCPI_Trigger_IIC","Level"),//设置IIC触发时的触发电平
//            new SCPICommandStruct(":TRIGger:IIC:LEVel?","SCPI_Trigger_IIC","LevelQ"),//查询IIC触发时的触发电平
    public static void Source(SCPIParam param){}
    public static void SourceQ(SCPIParam param){}


    public static void Type(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1);
        int addr=Command.get().getTrigger_iic().getAddr(param.iParam1);
        int data1=Command.get().getTrigger_iic().getData1(param.iParam1);
        int data2=Command.get().getTrigger_iic().getData2(param.iParam1);
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1);
        Command.get().getTrigger_iic().setType(param.iParam1, param.iParam2, addr,data1,data2,condition,true);
    }
    public static String TypeQ(SCPIParam param){
        int i= Command.get().getTrigger_iic().getType(param.iParam1);
        return ToolsSCPI.getIICTriggerType(i);
    }
    public static void Address(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1);
        //int addr=Command.get().getTrigger_iic().getAddr(param.iParam1);
        int addr= Tools.HexStringToInt(param.sParam1);
        int data1=Command.get().getTrigger_iic().getData1(param.iParam1);
        int data2=Command.get().getTrigger_iic().getData2(param.iParam1);
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1);
        Command.get().getTrigger_iic().setType(param.iParam1, type, addr, data1,data2,condition,true);
    }
    public static String AddressQ(SCPIParam param){
        int i=Command.get().getTrigger_iic().getAddr(param.iParam1);
        return Integer.toHexString(i);
    }
    public static void Relation(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1);
        int addr=Command.get().getTrigger_iic().getAddr(param.iParam1);
        int data1=Command.get().getTrigger_iic().getData1(param.iParam1);
        int data2=Command.get().getTrigger_iic().getData2(param.iParam1);
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1);
        Command.get().getTrigger_iic().setType(param.iParam1, type, addr,data1,data2, param.iParam2, true);
    }
    public static String RelationQ(SCPIParam param){
        int i= Command.get().getTrigger_iic().getCondition(param.iParam1);
        return ToolsSCPI.getSerialTriggerCondition(i);
    }
    public static void Data1(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1);
        int addr=Command.get().getTrigger_iic().getAddr(param.iParam1);
        //int data1=Command.get().getTrigger_iic().getData1(param.iParam1);
        int data=Tools.HexStringToInt(param.sParam1);
        int data2=Command.get().getTrigger_iic().getData2(param.iParam1);
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1);
        Command.get().getTrigger_iic().setType(param.iParam1, type, addr, data, data2,condition,true);
    }
    public static String Data1Q(SCPIParam param){
        int i=Command.get().getTrigger_iic().getData1(param.iParam1);
        return Integer.toHexString(i);
    }

    public static void Data2(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1);
        int addr=Command.get().getTrigger_iic().getAddr(param.iParam1);
        int data1=Command.get().getTrigger_iic().getData1(param.iParam1);
        //int data2=Command.get().getTrigger_iic().getData2(param.iParam1);
        int data2=Tools.HexStringToInt(param.sParam1);
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1);
        Command.get().getTrigger_iic().setType(param.iParam1, type, addr,data1, data2, condition,true);
    }
    public static String Data2Q(SCPIParam param){
        int i=Command.get().getTrigger_iic().getData2(param.iParam1);
        return Integer.toHexString(i);
    }

    public static void LevelClock(SCPIParam param){
        Command.get().getTrigger_iic().setLevelClock(param.iParam1, param.dParam1, true);
    }
    public static String LevelClockQ(SCPIParam param){
        double d=Command.get().getTrigger_iic().getLevelClock(param.iParam1);
        return String.valueOf(d);
    }

    public static void LevelData(SCPIParam param){
        Command.get().getTrigger_iic().setLevelData(param.iParam1, param.dParam1, true);
    }
    public static String LevelDataQ(SCPIParam param){
        double d=Command.get().getTrigger_iic().getLevelData(param.iParam1);
        return String.valueOf(d);
    }
}
