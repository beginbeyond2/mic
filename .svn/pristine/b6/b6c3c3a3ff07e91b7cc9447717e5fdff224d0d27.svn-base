package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:11
 */
public class SCPI_Bus_IIC {
//            new SCPICommandStruct(":BUS#:IIC:SDA","SCPI_Bus_IIC","SDA"),
//            new SCPICommandStruct(":BUS#:IIC:SDA?","SCPI_Bus_IIC","SDAQ"),
//            new SCPICommandStruct(":BUS#:IIC:SCL","SCPI_Bus_IIC","SCL"),
//            new SCPICommandStruct(":BUS#:IIC:SCL?","SCPI_Bus_IIC","SCLQ"),


    public static void SDA(SCPIParam param){
        Command.get().getBus_iic().SDA(param.iParam1, param.iParam2, true);
    }
    public static String SDAQ(SCPIParam param){
        int i=Command.get().getBus_iic().SDAQ(param.iParam1);
        return ToolsSCPI.getCh(i);
    }
    public static void SCL(SCPIParam param){
        Command.get().getBus_iic().SCL(param.iParam1, param.iParam2, true);
    }
    public static String SCLQ(SCPIParam param){
        int i= Command.get().getBus_iic().SCLQ(param.iParam1);
        return ToolsSCPI.getCh(i);
    }

}
