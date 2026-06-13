package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:40
 */
public class SCPI_Bus_Uart {
//    new SCPICommandStruct(":BUS#:UART:RX","SCPI_Bus_Uart","Rx"),
//            new SCPICommandStruct(":BUS#:UART:RX?","SCPI_Bus_Uart","RxQ"),
//            new SCPICommandStruct(":BUS#:UART:IDLElvl","SCPI_Bus_Uart","IdLevel"),
//            new SCPICommandStruct(":BUS#:UART:IDLElvl?","SCPI_Bus_Uart","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:UART:BAUDrate","SCPI_Bus_Uart","BaudRate"),
//            new SCPICommandStruct(":BUS#:UART:BAUDrate?","SCPI_Bus_Uart","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:UART:CHECK","SCPI_Bus_Uart","Check"),
//            new SCPICommandStruct(":BUS#:UART:CHECK?","SCPI_Bus_Uart","CheckQ"),
//            new SCPICommandStruct(":BUS#:UART:USERbaud","SCPI_Bus_Uart","UserBaud"),
//            new SCPICommandStruct(":BUS#:UART:USERbaud?","SCPI_Bus_Uart","UserBaudQ"),
//            new SCPICommandStruct(":BUS#:UART:WIDTh","SCPI_Bus_Uart","Width"),
//            new SCPICommandStruct(":BUS#:UART:WIDTh?","SCPI_Bus_Uart","WidthQ"),

    public static void Rx(SCPIParam param){
        Command.get().getBus_uart().Rx(param.iParam1, param.iParam2, true);
    }
    public static String RxQ(SCPIParam param){
        int i= Command.get().getBus_uart().RxQ(param.iParam1);
        return ToolsSCPI.getCh(i);
    }
    public static void IdLevel(SCPIParam param){
        Command.get().getBus_uart().IdLevel(param.iParam1, param.iParam2, true);
    }
    public static String IdLevelQ(SCPIParam param){
        int i= Command.get().getBus_uart().IdLevelQ(param.iParam1);
        return ToolsSCPI.getIdLevel(i);
    }
    public static void BaudRate(SCPIParam param){
        Command.get().getBus_uart().BaudRate(param.iParam1, param.iParam2, true);
    }
    public static String BaudRateQ(SCPIParam param){
        int i= Command.get().getBus_uart().BaudRateQ(param.iParam1);
        return ""+i;
    }
    public static void Check(SCPIParam param){
        Command.get().getBus_uart().Check(param.iParam1, param.iParam2, true);
    }
    public static String CheckQ(SCPIParam param){
        int i=Command.get().getBus_uart().CheckQ(param.iParam1);
        return ToolsSCPI.getUartCheck(i);
    }
    public static void UserBaud(SCPIParam param){
        Command.get().getBus_uart().UserBaud(param.iParam1, param.iParam2, true);
    }
    public static String UserBaudQ(SCPIParam param){
        int i= Command.get().getBus_uart().UserBaudQ(param.iParam1);
        return i+"";
    }
    public static void Width(SCPIParam param){
        Command.get().getBus_uart().Width(param.iParam1, param.iParam2, true);
    }
    public static String WidthQ(SCPIParam param){
        int i=Command.get().getBus_uart().WidthQ(param.iParam1);
        return ToolsSCPI.getUartWidth(i);
    }

    public static void Display(SCPIParam param){
        Command.get().getBus_uart().Display(param.iParam1, param.iParam2,true);
    }
    public static String DisplayQ(SCPIParam param){
        int i= Command.get().getBus_uart().DisplayQ(param.iParam1);
        return ToolsSCPI.getUartDisplay(i);
    }

}
