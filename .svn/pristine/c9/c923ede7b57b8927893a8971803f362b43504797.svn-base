package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Timebase {
//     new SCPICommandStruct(":TIMebase:EXTent","SCPI_Timebase","Extent"),//设置水平时基档位
//            new SCPICommandStruct(":TIMebase:PLUS:EXTent","SCPI_Timebase","Plus_Extent"),//设置水平时基档位
//            new SCPICommandStruct(":TIMebase:EXTent?","SCPI_Timebase","ExtentQ"),//查询水平时基档位
//            new SCPICommandStruct(":TIMebase:MODE","SCPI_Timebase","Mode"),//设置屏幕时基显示方式
//            new SCPICommandStruct(":TIMebase:MODE?","SCPI_Timebase","ModeQ"),//查询屏幕时基显示方式
//            new SCPICommandStruct(":TIMebase:ROLL:DISPlay","SCPI_Timebase","Roll_Display"),//滚屏设置
//            new SCPICommandStruct(":TIMebase:ROLL:DISPlay?","SCPI_Timebase","Roll_DisplayQ"),//滚屏查询
//            new SCPICommandStruct(":TIMebase:XY1:DISPlay","SCPI_Timebase","XY1_Display"),//打开或关闭通道1和通道2的XY模式显示
//            new SCPICommandStruct(":TIMebase:XY1:DISPlay?","SCPI_Timebase","XY1_DisplayQ"),//查询通道1和通道2的XY模式显示
//    //timerbase:offset 协议1.1 换名为 timerbase:position 2016.12.8
//            new SCPICommandStruct(":TIMebase:POSition","SCPI_Timebase","Position"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:OFFSet","SCPI_Timebase","Offset"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:PLUS:OFFSet","SCPI_Timebase","Plus_Offset"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:PLUS:POSition","SCPI_Timebase","Plus_Position"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:POSition?","SCPI_Timebase","PositionQ"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:OFFSet?","SCPI_Timebase","OffsetQ"),//查询波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:ZOOm:SCAle","SCPI_Timebase","Scale"),
//            new SCPICommandStruct(":TIMebase:ZOOm:SCAle?","SCPI_Timebase","ScaleQ"),

    public static void Extent(SCPIParam param){
        Command.get().getTimebase().Extent(param.iParam1, param.dParam1, true);
    }
    public static void Plus_Extent(SCPIParam param){
        Command.get().getTimebase().Plus_Extent(param.iParam1,true);
    }
    public static String ExtentQ(SCPIParam param){
        double d= Command.get().getTimebase().ExtentQ();
        return ToolsSCPI.getDouble(d);
    }
    public static void Mode(SCPIParam param){
        Command.get().getTimebase().Mode(param.iParam1,true);
    }
    public static String ModeQ(SCPIParam param){
        int i=Command.get().getTimebase().ModeQ();
        return ToolsSCPI.getTimebaseMode(i);
    }

    public static void Roll_Display(SCPIParam param){
        Command.get().getTimebase().Roll_Display(param.bParam1, true);
    }
    public static String Roll_DisplayQ(SCPIParam param){
        boolean b=Command.get().getTimebase().Roll_DisplayQ();
        return ToolsSCPI.getOpenState(b);
    }

    public static void XY1_Display(SCPIParam param){
        Command.get().getTimebase().XY1_Display(param.iParam1,true);
    }
    public static String XY1_DisplayQ(SCPIParam param){
         int i=Command.get().getTimebase().XY1_DisplayQ();
         return ToolsSCPI.getOpenState(i==0);

    }

    public static void Position(SCPIParam param){
        Command.get().getTimebase().Position(param.dParam1,true);
    }
    public static void Offset(SCPIParam param){
        Command.get().getTimebase().Offset(param.dParam1,true);
    }
    public static void Plus_Offset(SCPIParam param){
        Command.get().getTimebase().Plus_Offset(param.iParam1,true);
    }
    public static void Plus_Position(SCPIParam param){
        Command.get().getTimebase().Plus_Position(param.iParam1,true);
    }
    public static String PositionQ(SCPIParam param){
        double d=Command.get().getTimebase().PositionQ();
        return ToolsSCPI.getDouble(d);
    }
    public static String OffsetQ(SCPIParam param){
        double d= Command.get().getTimebase().OffsetQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Scale(SCPIParam param){
        Command.get().getTimebase().Scale(param.iParam1, param.dParam1, true);
    }
    public static String ScaleQ(SCPIParam param){
        double d=Command.get().getTimebase().ScaleQ();
        return ToolsSCPI.getDouble(d);
    }
    public static String ListQ(SCPIParam param){
        return Command.get().getTimebase().ListQ();
    }

}
