package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_SPI {
//      new SCPICommandStruct(":TRIGger:SPI:DATA","SCPI_Trigger_SPI","Data"),//设置SPI触发下的数据值
//            new SCPICommandStruct(":TRIGger:SPI:DATA?","SCPI_Trigger_SPI","DataQ"),//查询SPI触发下的数据值
//            new SCPICommandStruct(":TRIGger:SPI:SOURce","SCPI_Trigger_SPI","Source"),//设置SPI触发的触发源
//            new SCPICommandStruct(":TRIGger:SPI:SOURce?","SCPI_Trigger_SPI","SourceQ"),//查询SPI触发的触发源
//            new SCPICommandStruct(":TRIGger:SPI:LEVel","SCPI_Trigger_SPI","Level"),//设置SPI触发时的触发电平
//            new SCPICommandStruct(":TRIGger:SPI:LEVel?","SCPI_Trigger_SPI","LevelQ"),//查询SPI触发时的触发电平

    public static void Source(SCPIParam param){}
    public static void SourceQ(SCPIParam param){}

    public static void Type(SCPIParam param){
        Command.get().getTrigger_spi().Type(param.iParam1, param.iParam2, true);
    }
    public static String TypeQ(SCPIParam param){
        int i=Command.get().getTrigger_spi().TypeQ(param.iParam1);
        return ToolsSCPI.getSpiTriggerType(i);
    }
    public static void Data(SCPIParam param){
        Command.get().getTrigger_spi().Data(param.iParam1, param.sParam1.toUpperCase(), true);
    }
    public static String DataQ(SCPIParam param){
        String s=Command.get().getTrigger_spi().DataQ(param.iParam1);
        return s;
    }



    public static void LevelCLK(SCPIParam param){
        Command.get().getTrigger_spi().LevelCLK(param.iParam1, param.dParam1, true);
    }
    public static String LevelCLKQ(SCPIParam param){
        double d=Command.get().getTrigger_spi().LevelCLKQ(param.iParam1);
        return String.valueOf(d);
    }

    public static void LevelData(SCPIParam param){
        Command.get().getTrigger_spi().LevelData(param.iParam1, param.dParam1, true);
    }
    public static String LevelDataQ(SCPIParam param){
        double d=Command.get().getTrigger_spi().LevelDataQ(param.iParam1);
        return String.valueOf(d);
    }

    public static void LevelCS(SCPIParam param){
        Command.get().getTrigger_spi().LevelCS(param.iParam1, param.dParam1, true);
    }
    public static String LevelCSQ(SCPIParam param){
        double d=Command.get().getTrigger_spi().LevelCSQ(param.iParam1);
        return String.valueOf(d);
    }

}
