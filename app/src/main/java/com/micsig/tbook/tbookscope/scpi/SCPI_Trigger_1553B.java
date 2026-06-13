package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.tools.Tools;

/**
 * @auother Liwb
 * @description:
 * @data:2022-6-23 14:41
 */
class SCPI_Trigger_1553B {
//        new SCPICommandStruct(":TRIGger:1553B:SOURce","SCPI_Trigger_1553B","Source"),//设置1553B触发的触发源
//            new SCPICommandStruct(":TRIGger:1553B:SOURce?","SCPI_Trigger_1553B","SourceQ"),//查询1553B触发的触发源
//            new SCPICommandStruct(":TRIGger:1553B:TYPE","SCPI_Trigger_1553B","Type"),//设置1553B触发条件
//            new SCPICommandStruct(":TRIGger:1553B:TYPE?","SCPI_Trigger_1553B","TypeQ"),//查询1553B触发条件
//            new SCPICommandStruct(":TRIGger:1553B:CSWOrd","SCPI_Trigger_1553B","CsWord"),
//            new SCPICommandStruct(":TRIGger:1553B:CSWOrd?","SCPI_Trigger_1553B","CsWordQ"),
//            new SCPICommandStruct(":TRIGger:1553B:DWORd","SCPI_Trigger_1553B","DWord"),
//            new SCPICommandStruct(":TRIGger:1553B:DWORd?","SCPI_Trigger_1553B","DWordQ"),
//            new SCPICommandStruct(":TRIGger:1553B:RTADdress","SCPI_Trigger_1553B","RtAddress"),
//            new SCPICommandStruct(":TRIGger:1553B:RTADdress?","SCPI_Trigger_1553B","RtAddressQ"),
//            new SCPICommandStruct(":TRIGger:1553B:LEVEl","SCPI_Trigger_1553B","Level"),
//            new SCPICommandStruct(":TRIGger:1553B:LEVEl?","SCPI_Trigger_1553B","LevelQ"),


    public static void Source(SCPIParam param){ }
    public static void SourceQ(SCPIParam param){ }

    public static void Type(SCPIParam param){
        int type=Command.get().getTrigger_m1553B().getType(param.iParam1);
        int csword=Command.get().getTrigger_m1553B().getCsWord(param.iParam1);
        int Raddress=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1);
        int dataWord=Command.get().getTrigger_m1553B().getDataWord(param.iParam1);
        Command.get().getTrigger_m1553B().setType(param.iParam1, param.iParam2, csword,Raddress,dataWord,true);

    }
    public static String TypeQ(SCPIParam param){
        int i=Command.get().getTrigger_m1553B().getType(param.iParam1);
        return ToolsSCPI.getB1553bTriggerType(i);
    }
    public static void CsWord(SCPIParam param){
        int type=Command.get().getTrigger_m1553B().getType(param.iParam1);
        //int csword=Command.get().getTrigger_m1553B().getCsWord(param.iParam1);
        int csWord=Tools.HexStringToInt(param.sParam1);
        int Raddress=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1);
        int dataWord=Command.get().getTrigger_m1553B().getDataWord(param.iParam1);

        Command.get().getTrigger_m1553B().setType(param.iParam1, type, csWord, Raddress,dataWord,true);

    }
    public static String CsWordQ(SCPIParam param){
        int i=Command.get().getTrigger_m1553B().getCsWord(param.iParam1);
        return Integer.toHexString(i);
    }
    public static void DWord(SCPIParam param){
        int type=Command.get().getTrigger_m1553B().getType(param.iParam1);
        int csword=Command.get().getTrigger_m1553B().getCsWord(param.iParam1);
        int Raddress=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1);
        //int dataWord=Command.get().getTrigger_m1553B().getDataWord(param.iParam1);
        int dataWord=Tools.HexStringToInt(param.sParam1);
        Command.get().getTrigger_m1553B().setType(param.iParam1, type, csword,Raddress, dataWord, true);

    }
    public static String DWordQ(SCPIParam param){
        int i=Command.get().getTrigger_m1553B().getDataWord(param.iParam1);
        return Integer.toHexString(i);
    }
    public static void RtAddress(SCPIParam param){
        int type=Command.get().getTrigger_m1553B().getType(param.iParam1);
        int csword=Command.get().getTrigger_m1553B().getCsWord(param.iParam1);
        //int Raddress=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1);
        int rAddr= Tools.HexStringToInt(param.sParam1);
        int dataWord=Command.get().getTrigger_m1553B().getDataWord(param.iParam1);
        Command.get().getTrigger_m1553B().setType(param.iParam1, type, csword, rAddr, dataWord,true);
    }
    public static String RtAddressQ(SCPIParam param){
        int i=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1);
        return Integer.toHexString(i);

    }
    public static void Level(SCPIParam param){
        Command.get().getTrigger_m1553B().setLevel(param.iParam1, param.dParam1, true);

    }
    public static String LevelQ(SCPIParam param){
        double d=Command.get().getTrigger_m1553B().getLevel(param.iParam1);
        return String.valueOf(d);
    }
}
