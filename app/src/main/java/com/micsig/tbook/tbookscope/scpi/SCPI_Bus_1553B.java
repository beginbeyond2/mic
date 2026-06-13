package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:13
 */
public class SCPI_Bus_1553B {
//            new SCPICommandStruct(":BUS#:1553B:CHANnel","SCPI_Bus_1553B","Channel"),
//            new SCPICommandStruct(":BUS#:1553B:CHANnel?","SCPI_Bus_1553B","ChannelQ"),
//            new SCPICommandStruct(":BUS#:1553B:DISPlay","SCPI_Bus_1553B","Display"),
//            new SCPICommandStruct(":BUS#:1553B:DISPlay?","SCPI_Bus_1553B","DisplayQ"),

    public static void Channel(SCPIParam param){
        Command.get().getBus_1553B().Channel(param.iParam1, param.iParam2, true);
    }
    public static String ChannelQ(SCPIParam param){
        int i= Command.get().getBus_1553B().ChannelQ(param.iParam1);
        return ToolsSCPI.getCh(i);
    }

    public static void Display(SCPIParam param){
        Command.get().getBus_1553B().Display(param.iParam1, param.iParam2, true);
    }
    public static String DisplayQ(SCPIParam param){
        int i=Command.get().getBus_1553B().DisplayQ(param.iParam1);
        return ToolsSCPI.get429Display(i);
    }
}
