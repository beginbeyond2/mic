package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:15
 */
public class SCPI_Bus_429 {
//    new SCPICommandStruct(":BUS#:429:SOURce","SCPI_Bus_429","Source"),
//            new SCPICommandStruct(":BUS#:429:SOURce?","SCPI_Bus_429","SourceQ"),
//            new SCPICommandStruct(":BUS#:429:FORMat","SCPI_Bus_429","Format"),
//            new SCPICommandStruct(":BUS#:429:FORMat?","SCPI_Bus_429","FormatQ"),
//            new SCPICommandStruct(":BUS#:429:DISPlay","SCPI_Bus_429","Display"),
//            new SCPICommandStruct(":BUS#:429:DISPlay?","SCPI_Bus_429","DisplayQ"),
//            new SCPICommandStruct(":BUS#:429:BANDrate","SCPI_Bus_429","BandRate"),
//            new SCPICommandStruct(":BUS#:429:BANDrate?","SCPI_Bus_429","BandRateQ"),

    public static void Source(SCPIParam param){
        Command.get().getBus_429().Source(param.iParam1, param.iParam2, true);
    }
    public static String SourceQ(SCPIParam param){
        int i= Command.get().getBus_429().SourceQ(param.iParam1);
        return ToolsSCPI.getCh(i);
    }
    public static void Format(SCPIParam param){
        Command.get().getBus_429().Format(param.iParam1, param.iParam2, true);
    }
    public static String FormatQ(SCPIParam param){
        int i=Command.get().getBus_429().FormatQ(param.iParam1);
        return ToolsSCPI.get429Format(i);
    }
    public static void Display(SCPIParam param){
        Command.get().getBus_429().Display(param.iParam1, param.iParam2, true);
    }
    public static String DisplayQ(SCPIParam param){
        int i=Command.get().getBus_429().DisplayQ(param.iParam1);
        return ToolsSCPI.get429Display(i);
    }
    public static void BandRate(SCPIParam param){
        Command.get().getBus_429().BaudRate(param.iParam1, param.iParam2, true);
    }
    public static String BandRateQ(SCPIParam param){
        int i=Command.get().getBus_429().BaudRateQ(param.iParam1);
        return ToolsSCPI.get429BaudRate(i);
    }

}
