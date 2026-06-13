package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

public class SCPI_Common {
//            new SCPICommandStruct("*CLS","SCPI_Common","CLS"),//
//            new SCPICommandStruct("*ESE","SCPI_Common","ESE"),//
//            new SCPICommandStruct("*ESE?","SCPI_Common","ESEQ"),//
//            new SCPICommandStruct("*ESR","SCPI_Common","ESR"),//
//            new SCPICommandStruct("*IDN?","SCPI_Common","IDNQ"),//
//            new SCPICommandStruct("*OPC","SCPI_Common","OPC"),//
//            new SCPICommandStruct("*OPC?","SCPI_Common","OPCQ"),//
//            new SCPICommandStruct("*RST","SCPI_Common","RST"),//
//            new SCPICommandStruct("*SRE","SCPI_Common","SRE"),//
//            new SCPICommandStruct("*SRE?","SCPI_Common","SREQ"),//
//            new SCPICommandStruct("*STB?","SCPI_Common","STBQ"),//
//            new SCPICommandStruct("*TST?","SCPI_Common","TSTQ"),//
//            new SCPICommandStruct("*WAI","SCPI_Common","WAI"),//

    public static void CLS(SCPIParam param){

    }
    public static void ESE(SCPIParam param){

    }
    public static String ESEQ(SCPIParam param){
         return ToolsSCPI.getOKAY();
    }
    public static void ESR(SCPIParam param){

    }
    public static String IDNQ(SCPIParam param){
        return Command.get().getCommon().IDNQ();

    }
    public static void OPC(SCPIParam param){

    }
    public static String OPCQ(SCPIParam param){
        return ToolsSCPI.getOKAY();
    }
    public static void RST(SCPIParam param){

    }
    public static void SRE(SCPIParam param){

    }
    public static String SREQ(SCPIParam param){
        return ToolsSCPI.getOKAY();
    }
    public static String STBQ(SCPIParam param){
        return ToolsSCPI.getOKAY();
    }
    public static String TSTQ(SCPIParam param){
        return ToolsSCPI.getOKAY();
    }
    public static void WAI(SCPIParam param){

    }
}
