package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Auto {
//     new SCPICommandStruct(":AUTO:SET:CHANnel","SCPI_Auto","Set_Channel"),//设置自动打开通道
//            new SCPICommandStruct(":AUTO:SET:CHANnel?","SCPI_Auto","Set_ChannelQ"),//查询自动打开通道
//            new SCPICommandStruct(":AUTO:SET:LEVEl","SCPI_Auto","Set_Level"),//设置门限电平
//            new SCPICommandStruct(":AUTO:SET:LEVEl?","SCPI_Auto","Set_LevelQ"),//查询门限电平
//            new SCPICommandStruct(":AUTO:SET:SOURce","SCPI_Auto","Set_Source"),//设置触发源
//            new SCPICommandStruct(":AUTO:SET:SOURce?","SCPI_Auto","Set_SourceQ"),//查询触发源
//            new SCPICommandStruct(":AUTO:RANge","SCPI_Auto","Range"),//设置自动量程
//            new SCPICommandStruct(":AUTO:RANge?","SCPI_Auto","RangeQ"),//查询自动量程
//            new SCPICommandStruct(":AUTO:RANge:VERtical","SCPI_Auto","Range_Vertical"),//设置自动垂直
//            new SCPICommandStruct(":AUTO:RANge:VERtical?","SCPI_Auto","Range_VerticalQ"),//查询自动垂直
//            new SCPICommandStruct(":AUTO:RANge:HORizoncal","SCPI_Auto","Range_Horizoncal"),//设置自动水平
//            new SCPICommandStruct(":AUTO:RANge:HORizoncal?","SCPI_Auto","Range_HorizoncalQ"),//查询自动水平
//            new SCPICommandStruct(":AUTO:RANge:LEVEl","SCPI_Auto","Range_Level"),//设置自动量程
//            new SCPICommandStruct(":AUTO:RANge:LEVEl?","SCPI_Auto","Range_LevelQ"),//查询自动量程

    public static void Set_Channel(SCPIParam param) {
        System.out.println("SCPI_AUTO_Set_Channel!");
        Command.get().getAuto().setChannel(param.bParam1, true);
    }

    public static String Set_ChannelQ(SCPIParam param) {
        boolean b= Command.get().getAuto().setChannelQuery();
        return ToolsSCPI.getOpenState(b);
    }

    public static void Set_Level(SCPIParam param) {
        Command.get().getAuto().setLevel(param.dParam1, true);
    }

    public static String Set_LevelQ(SCPIParam param) {
        double d= Command.get().getAuto().setLevelQuery();
        return ToolsSCPI.getDouble(d);
    }

    public static void Set_Source(SCPIParam param) {
        Command.get().getAuto().setSource(param.iParam1, true);
    }

    public static String Set_SourceQ(SCPIParam param) {
        int i=Command.get().getAuto().setSourceQuery();
        return ToolsSCPI.getAutoSource(i);
    }

    public static void Range(SCPIParam param) {
        Command.get().getAuto().range(param.bParam1, true);
    }

    public static String RangeQ(SCPIParam param) {
        boolean b= Command.get().getAuto().rangeQuery();
        return  ToolsSCPI.getOpenState(b);
    }

    public static void Range_Vertical(SCPIParam param) {
        Command.get().getAuto().rangeVertical(param.bParam1, true);
    }

    public static String Range_VerticalQ(SCPIParam param) {
        boolean b= Command.get().getAuto().rangeVerticalQuery();
        return ToolsSCPI.getOpenState(b);
    }

    public static void Range_Horizontal(SCPIParam param) {
        Command.get().getAuto().rangeHorizoncal(param.bParam1, true);
    }

    public static String Range_HorizontalQ(SCPIParam param) {
        boolean b= Command.get().getAuto().rangeHorizoncalQuery();
        return ToolsSCPI.getOpenState(b);
    }

    public static void Range_Level(SCPIParam param) {
        Command.get().getAuto().rangeLevel(param.bParam1, true);
    }

    public static String Range_LevelQ(SCPIParam param) {
        boolean b= Command.get().getAuto().rangeLevelQuery();
        return ToolsSCPI.getOpenState(b);
    }
}
