package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:39
 */
public class SCPI_Bus {
//            new SCPICommandStruct(":BUS#:DISPlay","SCPI_Bus","Display"),
//            new SCPICommandStruct(":BUS#:DISPlay?","SCPI_Bus","DisplayQ"), //
//            new SCPICommandStruct(":BUS#:TYPE","SCPI_Bus","Type"),
//            new SCPICommandStruct(":BUS#:TYPE?","SCPI_Bus","TypeQ"),
//            new SCPICommandStruct(":BUS#:MODE","SCPI_Bus","Mode"),
//            new SCPICommandStruct(":BUS#:MODE?","SCPI_Bus","ModeQ"),
//            new SCPICommandStruct(":BUS#:LEVel","SCPI_Bus","Level"),
//            new SCPICommandStruct(":BUS#:LEVel?","SCPI_Bus","LevelQ"),
//            new SCPICommandStruct(":BUS#:HLEVel","SCPI_Bus","HLevel"),
//            new SCPICommandStruct(":BUS#:HLEVel?","SCPI_Bus","HLevelQ"),
//            new SCPICommandStruct(":BUS#:LLEVel","SCPI_Bus","LLevel"),
//            new SCPICommandStruct(":BUS#:LLEVel?","SCPI_Bus","LLevelQ"),
//            new SCPICommandStruct(":BUS#:Data?","SCPI_Bus","DataQ"),

    public static void Display(SCPIParam param){
        Command.get().getBus().Display(param.iParam1, param.bParam1, true);
    }
    public static String DisplayQ(SCPIParam param){
        return Command.get().getBus().DisplayQ(param.iParam1);
    }
    public static void Type(SCPIParam param) {
        Command.get().getBus().Type(param.iParam1, param.iParam2, true);
    }

    public static String TypeQ(SCPIParam param) {
        int i= Command.get().getBus().TypeQ(param.iParam1);
        return ToolsSCPI.getBusType(i);
    }

    public static void Mode(SCPIParam param){
        Command.get().getBus().Mode(param.iParam1, param.iParam2, true);
    }
    public static String ModeQ(SCPIParam param){
        int i = Command.get().getBus().ModeQ(param.iParam1);
        return ToolsSCPI.getSerialBusMode(i);
    }
    public static void Level(SCPIParam param){
        Command.get().getBus().Level(param.iParam1, param.iParam2, param.dParam1, true);
    }
    public static String LevelQ(SCPIParam param){
        double v = Command.get().getBus().LevelQ(param.iParam1, param.iParam2);
        return String.valueOf(v);
    }

    public static void HLevel(SCPIParam param){
        Command.get().getBus().HLevel(param.iParam1, param.iParam2, param.dParam1, true);
    }
    public static String HLevelQ(SCPIParam param){
        double d= Command.get().getBus().HLevelQ(param.iParam1, param.iParam2);
        return String.valueOf(d);
    }
    public static void LLevel(SCPIParam param){
        Command.get().getBus().LLevel(param.iParam1, param.iParam2, param.dParam1, true);
    }
    public static String LLevelQ(SCPIParam param){
        double d= Command.get().getBus().LLevelQ(param.iParam1, param.iParam2);
        return String.valueOf(d);
    }
    public static String DataQ(SCPIParam param){
        return Command.get().getBus().DataQ(param.iParam1);
    }
}
