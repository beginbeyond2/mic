package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:07
 */
public class SCPI_Bus_Can {
//            new SCPICommandStruct(":BUS#:CAN:CHANnel","SCPI_Bus_Can","Channel"),
//            new SCPICommandStruct(":BUS#:CAN:CHANnel?","SCPI_Bus_Can","ChannelQ"),
//            new SCPICommandStruct(":BUS#:CAN:SIGNal","SCPI_Bus_Can","Signal"),
//            new SCPICommandStruct(":BUS#:CAN:SIGNal?","SCPI_Bus_Can","signalQ"),
//            new SCPICommandStruct(":BUS#:CAN:BAUDrate","SCPI_Bus_Can","BaudRate"),
//            new SCPICommandStruct(":BUS#:CAN:BAUDrate?","SCPI_Bus_Can","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:CAN:USERbaud","SCPI_Bus_Can","UserBaud"),
//            new SCPICommandStruct(":BUS#:CAN:USERbaud?","SCPI_Bus_Can","UserBaudQ"),

//            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint","SCPI_Bus_Can","SAMPlepoint"),
//            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint?","SCPI_Bus_Can","SAMPlepointQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDBAudrate","SCPI_Bus_Can","FDBAudrate"),
//            new SCPICommandStruct(":BUS#:CAN:FDBAudrate?","SCPI_Bus_Can","FDBAudrateQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud","SCPI_Bus_Can","FDUSerbaud"),
//            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud?","SCPI_Bus_Can","FDUSerbaudQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint","SCPI_Bus_Can","FDSAmplepoint"),
//            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint?","SCPI_Bus_Can","FDSAmplepointQ"),


    public static void Channel(SCPIParam param){
        Command.get().getBus_can().Channel(param.iParam1, param.iParam2,true);
    }
    public static String ChannelQ(SCPIParam param){
        int i=Command.get().getBus_can().ChannelQ(param.iParam1);
        return ToolsSCPI.getCh(i);
    }
    public static void Signal(SCPIParam param){
        Command.get().getBus_can().Signal(param.iParam1, param.iParam2,true);
    }
    public static String SignalQ(SCPIParam param){
        int i=Command.get().getBus_can().SignalQ(param.iParam1);
        return ToolsSCPI.getCanSignal(i);
    }
    public static void BaudRate(SCPIParam param){
        Command.get().getBus_can().BaudRate(param.iParam1, param.iParam2, true);
    }
    public static String BaudRateQ(SCPIParam param){
        int i=Command.get().getBus_can().BaudRateQ(param.iParam1);
        if (i==-1) return UserBaudQ(param);
        return ToolsSCPI.getCanBaudRate(i);
    }
    public static void UserBaud(SCPIParam param){
        Command.get().getBus_can().UserBaud(param.iParam1, param.iParam2, true);
    }
    public static String UserBaudQ(SCPIParam param){
        int i=Command.get().getBus_can().UserBaudQ(param.iParam1);
        if (i==-1){
            return BaudRateQ(param);
        }
        return i+"";
    }


  public static void   SAMPlepoint(SCPIParam param){
        Command.get().getBus_can().SAMPlepoint(param.iParam1, param.dParam1, true);
  }
  public static String   SAMPlepointQ(SCPIParam param){
        double i= Command.get().getBus_can().SAMPlepointQ(param.iParam1);
        return String.valueOf(i);
  }
  public static void   FDBAudrate(SCPIParam param){
        Command.get().getBus_can().FDBAudrate(param.iParam1, param.iParam2, true);
  }
  public static String   FDBAudrateQ(SCPIParam param){
      int i = Command.get().getBus_can().FDBAudrateQ(param.iParam1);
      if (i==-1){
          return FDUSerbaudQ(param);
      }
      return ToolsSCPI.getCanFDBaudrate(i);
  }
  public static void   FDUSerbaud(SCPIParam param){
        Command.get().getBus_can().FDUSerbaud(param.iParam1, param.iParam2, true);
  }
  public static String   FDUSerbaudQ(SCPIParam param){
        int i=Command.get().getBus_can().FDUSerbaudQ(param.iParam1);
        if (i==-1){
            return FDBAudrateQ(param);
        }
        return i+"";
  }
  public static void   FDSAmplepoint(SCPIParam param){
        Command.get().getBus_can().FDSAmplepoint(param.iParam1, param.dParam1, true);
  }
  public static String   FDSAmplepointQ(SCPIParam param){
      double i = Command.get().getBus_can().FDSAmplepointQ(param.iParam1);
      return String.valueOf(i);
  }
    public static void ISO(SCPIParam param){
        Command.get().getBus_can().ISO(param.iParam1, param.iParam2, true);
    }
    public static String ISOQ(SCPIParam param){
        int i=Command.get().getBus_can().ISOQ(param.iParam1);
        return ToolsSCPI.getCanIso(i);
    }

}
