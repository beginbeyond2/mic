package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;

/**
 * @auother Liwb
 * @description:
 * @data:2022-6-23 14:43
 */
class SCPI_Trigger_429 {
//            new SCPICommandStruct(":TRIGger:429:SOURce","SCPI_Trigger_429","Source"),
//            new SCPICommandStruct(":TRIGger:429:SOURce?","SCPI_Trigger_429","SourceQ"),
//            new SCPICommandStruct(":TRIGger:429:TYPE","SCPI_Trigger_429","Type"),
//            new SCPICommandStruct(":TRIGger:429:TYPE?","SCPI_Trigger_429","TypeQ"),
//            new SCPICommandStruct(":TRIGger:429:WORD","SCPI_Trigger_429","Word"),
//            new SCPICommandStruct(":TRIGger:429:WORD?","SCPI_Trigger_429","WordQ"),
//            new SCPICommandStruct(":TRIGger:429:LABEl","SCPI_Trigger_429","Label"),
//            new SCPICommandStruct(":TRIGger:429:LABEl?","SCPI_Trigger_429","LabelQ"),
//            new SCPICommandStruct(":TRIGger:429:SDI","SCPI_Trigger_429","Sdi"),
//            new SCPICommandStruct(":TRIGger:429:SDI?","SCPI_Trigger_429","SdiQ"),
//            new SCPICommandStruct(":TRIGger:429:DATA","SCPI_Trigger_429","data"),
//            new SCPICommandStruct(":TRIGger:429:DATA?","SCPI_Trigger_429","dataQ"),
//            new SCPICommandStruct(":TRIGger:429:SSM","SCPI_Trigger_429","Ssm"),
//            new SCPICommandStruct(":TRIGger:429:SSM?","SCPI_Trigger_429","SsmQ"),
//            new SCPICommandStruct(":TRIGger:429:LEVEl","SCPI_Trigger_429","Level"),
//            new SCPICommandStruct(":TRIGger:429:LEVEl?","SCPI_Trigger_429","LevelQ"),

    public static void Source(SCPIParam param){}
    public static void SourceQ(SCPIParam param){}

    public static void Type(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1);
        int label=Command.get().getTrigger_m429().getLabel(param.iParam1);
        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1);
        long data=Command.get().getTrigger_m429().getData(param.iParam1);
        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1);
        Command.get().getTrigger_m429().setType(param.iParam1, param.iParam2, label,sdi,data,ssm,true);
    }
    public static String TypeQ(SCPIParam param){
        int i=Command.get().getTrigger_m429().getType(param.iParam1);
        return ToolsSCPI.getArinc429TriggerType(i);
    }
    public static void Word(SCPIParam param){
//        int type=Command.get().getTrigger_m429().getType(param.iParam1);
//        int label=Command.get().getTrigger_m429().getLabel(param.iParam1);
//        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1);
//        int data=Command.get().getTrigger_m429().getData(param.iParam1);
//        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1);
//        Command.get().getTrigger_m429().setType(param.iParam1, type, label,sdi,data,ssm,true);
    }
    public static void WordQ(SCPIParam param){}

    public static void Label(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1);
        //int label=Command.get().getTrigger_m429().getLabel(param.iParam1);
//        int label= Tools.HexStringToInt(param.sParam1);
        int label= (int)SerialsUtils.toDLong(param.sParam1.trim(),8);
        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1);
        long data=Command.get().getTrigger_m429().getData(param.iParam1);
        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1);
        Command.get().getTrigger_m429().setType(param.iParam1, type, label, sdi,data,ssm,true);
    }
    public static String LabelQ(SCPIParam param){
        int i=Command.get().getTrigger_m429().getLabel(param.iParam1);
        return SerialsUtils.getHexBinFromInt(i,3,8);
    }
    public static void Sdi(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1);
        int label=Command.get().getTrigger_m429().getLabel(param.iParam1);
        //int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1);
        //int sdi=Tools.HexStringToInt(param.sParam1);
        int sdi=(int)SerialsUtils.toDLong(param.sParam1.trim(),2);
        long data=Command.get().getTrigger_m429().getData(param.iParam1);
        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1);
        Command.get().getTrigger_m429().setType(param.iParam1, type, label, sdi, data,ssm,true);
    }
    public static String SdiQ(SCPIParam param){
        int i=Command.get().getTrigger_m429().getSdi(param.iParam1);
        return SerialsUtils.getHexBinFromInt(i,2,2);
    }
    public static void data(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1);
        int label=Command.get().getTrigger_m429().getLabel(param.iParam1);
        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1);
        //int data=Command.get().getTrigger_m429().getData(param.iParam1);
        long data=Tools.HexStringToLong(param.sParam1);
        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1);
        Command.get().getTrigger_m429().setType(param.iParam1, type, label,sdi,data, ssm,true);
    }
    public static String dataQ(SCPIParam param){
        long i=Command.get().getTrigger_m429().getData(param.iParam1);
        int f= Command.get().getBus_429().FormatQ(param.iParam1);
        if (f==2) {
            return SerialsUtils.getHexBinFromLong(i, 5, 16);
        }else{
            return SerialsUtils.getHexBinFromLong(i, 6, 16);
        }
    }
    public static void Ssm(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1);
        int label=Command.get().getTrigger_m429().getLabel(param.iParam1);
        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1);
        long data=Command.get().getTrigger_m429().getData(param.iParam1);
        //int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1);
        //int ssm=Tools.HexStringToInt(param.sParam1);
         int ssm=(int)SerialsUtils.toDLong(param.sParam1.trim(),2);
        Command.get().getTrigger_m429().setType(param.iParam1, type, label,sdi,data, ssm, true);
    }
    public static String SsmQ(SCPIParam param){
        int i=Command.get().getTrigger_m429().getSsm(param.iParam1);
        return SerialsUtils.getHexBinFromInt(i,2,2);
    }
    public static void LevelHigh(SCPIParam param){
        Command.get().getTrigger_m429().setLevelHigh(param.iParam1,param.iParam2, param.dParam1, true);
    }
    public static String LevelHighQ(SCPIParam param){
        double d=Command.get().getTrigger_m429().getLevelHigh(param.iParam1);
        return String.valueOf(d);
    }
    public static void LevelLow(SCPIParam param){
        Command.get().getTrigger_m429().setLevelLow(param.iParam1,param.iParam2, param.dParam1, true);
    }
    public static String LevelLowQ(SCPIParam param){
        double d=Command.get().getTrigger_m429().getLevelLow(param.iParam1);
        return String.valueOf(d);
    }

}
