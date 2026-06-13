package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:55
 */
public class SCPI_Bus_Lin {
//     new SCPICommandStruct(":BUS#:LIN:CHANnel","SCPI_Bus_Lin","Channel"),
//            new SCPICommandStruct(":BUS#:LIN:CHANnel?","SCPI_Bus_Lin","ChannelQ"),
//            new SCPICommandStruct(":BUS#:LIN:IDLElvl","SCPI_Bus_Lin","IdLevel"),
//            new SCPICommandStruct(":BUS#:LIN:IDLElvl?","SCPI_Bus_Lin","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:LIN:BAUDrate","SCPI_Bus_Lin","BaudRate"),
//            new SCPICommandStruct(":BUS#:LIN:BAUDrate?","SCPI_Bus_Lin","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:LIN:USERbaud","SCPI_Bus_Lin","UserBaud"),
//            new SCPICommandStruct(":BUS#:LIN:USERbaud?","SCPI_Bus_Lin","UserBaudQ"),

    public static void LinType(SCPIParam param) {
        Command.get().getBus_lin().LinType(param.iParam1, param.iParam2, true);
    }

    public static String LinTypeQ(SCPIParam param) {
        int i = Command.get().getBus_lin().LinTypeQ(param.iParam1);
        return ToolsSCPI.getCh(i);
    }
    public static void Channel(SCPIParam param){
        Command.get().getBus_lin().Channel(param.iParam1, param.iParam2, true);
    }
    public static String ChannelQ(SCPIParam param){
        int i= Command.get().getBus_lin().ChannelQ(param.iParam1);
        return ToolsSCPI.getCh(i);
    }
    public static void IdLevel(SCPIParam param){
        Command.get().getBus_lin().IdLevel(param.iParam1, param.iParam2,true);
    }
    public static String IdLevelQ(SCPIParam param){
        int i= Command.get().getBus_lin().IdLevelQ(param.iParam1);
        return ToolsSCPI.getIdLevel(i);
    }
    public static void BaudRate(SCPIParam param){
        Command.get().getBus_lin().BaudRate(param.iParam1, param.iParam2, true);
    }
    public static String BaudRateQ(SCPIParam param){
        int i=Command.get().getBus_lin().BaudRateQ(param.iParam1);
        if (i==-1) return UserBaudQ(param);
        return ToolsSCPI.getLinBaudRate(i);
    }
    public static void UserBaud(SCPIParam param){
        Command.get().getBus_lin().UserBaud(param.iParam1, param.iParam2, true);
    }
    public static String UserBaudQ(SCPIParam param){
        int i=Command.get().getBus_lin().UserBaudQ(param.iParam1);
        if (i==-1) return BaudRateQ(param);
        return String.valueOf(i);
    }

}
