package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:04
 */
public class SCPI_Bus_Spi {
//            new SCPICommandStruct(":BUS#:SPI:CLK","SCPI_Bus_Spi","Clk"),
//            new SCPICommandStruct(":BUS#:SPI:CLK?","SCPI_Bus_Spi","ClkQ"),
//            new SCPICommandStruct(":BUS#:SPI:DATA","SCPI_Bus_Spi","Data"),
//            new SCPICommandStruct(":BUS#:SPI:DATA?","SCPI_Bus_Spi","DataQ"),
//            new SCPICommandStruct(":BUS#:SPI:WIDTh","SCPI_Bus_Spi","Width"),
//            new SCPICommandStruct(":BUS#:SPI:WIDTh?","SCPI_Bus_Spi","WidthQ"),
//            new SCPICommandStruct(":BUS#:SPI:IDLElvl","SCPI_Bus_Spi","IdLevel"),
//            new SCPICommandStruct(":BUS#:SPI:IDLElvl?","SCPI_Bus_Spi","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:SPI:SLOPe","SCPI_Bus_Spi","Slope"),
//            new SCPICommandStruct(":BUS#:SPI:SLOPe?","SCPI_Bus_Spi","SlopeQ"),
//            new SCPICommandStruct(":BUS#:SPI:CS","SCPI_Bus_Spi","CS"),
//            new SCPICommandStruct(":BUS#:SPI:CS?","SCPI_Bus_Spi","CSQ"),
//            new SCPICommandStruct(":BUS#:SPI:CS:SOURce","SCPI_Bus_Spi","Source"),
//            new SCPICommandStruct(":BUS#:SPI:CS:SOURce?","SCPI_Bus_Spi","SourceQ"),
//            new SCPICommandStruct(":BUS#:SPI:CS:IDLElvl","SCPI_Bus_Spi","Idlelvl"),
//            new SCPICommandStruct(":BUS#:SPI:CS:IDLElvl?","SCPI_Bus_Spi","IdlelvlQ"),

    public static void Clk(SCPIParam param){
        Command.get().getBus_spi().setClock(param.iParam1, param.iParam2, true);
    }
    public static String ClkQ(SCPIParam param){
        int clock = Command.get().getBus_spi().getClock(param.iParam1);
        return ToolsSCPI.getCh(clock);
    }
    public static void Data(SCPIParam param){
        Command.get().getBus_spi().setData(param.iParam1, param.iParam2, true);
    }
    public static String DataQ(SCPIParam param){
        int data = Command.get().getBus_spi().getData(param.iParam1);
        return ToolsSCPI.getCh(data);
    }
    public static void Width(SCPIParam param){
        Command.get().getBus_spi().setBits(param.iParam1, param.iParam2, true);
    }
    public static String WidthQ(SCPIParam param){
        int bits = Command.get().getBus_spi().getBits(param.iParam1);
        return ToolsSCPI.getSpiBits(bits);
    }
    public static void IdLevel(SCPIParam param){
        Command.get().getBus_spi().setDataSwitch(param.iParam1, param.iParam2, true);
    }
    public static String IdLevelQ(SCPIParam param){
        int dataSwitch = Command.get().getBus_spi().getDataSwitch(param.iParam1);
        return ToolsSCPI.getIdLevel(dataSwitch);
    }
    public static void Slope(SCPIParam param){
        Command.get().getBus_spi().setClockSwitch(param.iParam1, param.iParam2, true);
    }
    public static String SlopeQ(SCPIParam param){
        int clockSwitch = Command.get().getBus_spi().getClockSwitch(param.iParam1);
        return ToolsSCPI.getTriggerRiseFall(clockSwitch);
    }
    public static void CS(SCPIParam param){
        Command.get().getBus_spi().setCsEnable(param.iParam1, param.bParam1,true);
    }
    public static String CSQ(SCPIParam param){
        boolean csEnable = Command.get().getBus_spi().getCsEnable(param.iParam1);
        return ToolsSCPI.getOpenState(csEnable);
    }
    public static void Source(SCPIParam param){
        Command.get().getBus_spi().setCs(param.iParam1, param.iParam2, true);
    }
    public static String SourceQ(SCPIParam param){
        int cs = Command.get().getBus_spi().getCs(param.iParam1);
        return ToolsSCPI.getCh(cs);
    }
    public static void Idlelvl(SCPIParam param){
        Command.get().getBus_spi().setCsSwitch(param.iParam1, param.iParam2, true);
    }
    public static String IdlelvlQ(SCPIParam param){
        int csSwitch = Command.get().getBus_spi().getCsSwitch(param.iParam1);
        return ToolsSCPI.getIdLevel(csSwitch);
    }

}
