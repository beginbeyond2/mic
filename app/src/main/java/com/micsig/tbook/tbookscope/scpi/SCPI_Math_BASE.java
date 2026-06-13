package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-24 10:06
 */
public class SCPI_Math_BASE {

    //            new SCPICommandStruct(":MATH:BASE:S1","SCPI_Math_BASE","S1"),//选择加法运算的信源1
//            new SCPICommandStruct(":MATH:BASE:S1?","SCPI_Math_BASE","S1Q"),//查询加法运算的信源1
//            new SCPICommandStruct(":MATH:BASE:S2","SCPI_Math_BASE","S2"),//选择加法运算的信源2
//            new SCPICommandStruct(":MATH:BASE:S2?","SCPI_Math_BASE","S2Q"),//查询加法运算的信源2
//            new SCPICommandStruct(":MATH:BASE:EXTent","SCPI_Math_BASE","Extent"),//设置加法运算结果的垂直档位
//            new SCPICommandStruct(":MATH:BASE:EXTent?","SCPI_Math_BASE","ExtentQ"),//查询加法运算结果的垂直档位
//            new SCPICommandStruct(":MATH:BASE:OFFSet","SCPI_Math_BASE","Offset"),//设置加法运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:BASE:OFFSet?","SCPI_Math_BASE","OffsetQ"),//查询加法运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:BASE:OPERator","SCPI_Math_BASE","Operator"),//设置运算符
//            new SCPICommandStruct(":MATH:BASE:OPERator?","SCPI_Math_BASE","OperatorQ"),//查询运算符
    public static void S1(SCPIParam param) {
        // if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 0) {
        Command.get().getMath_base().S1(param.iParam1, param.iParam2, true);
        // }
    }

    public static String S1Q(SCPIParam param) {
        int i = Command.get().getMath_base().S1Q();
        return ToolsSCPI.getCh(i);
    }

    public static void S2(SCPIParam param) {
        //if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 0) {
        Command.get().getMath_base().S2(param.iParam1, param.iParam2, true);
        //}
    }

    public static String S2Q(SCPIParam param) {
        int i = Command.get().getMath_base().S2Q();
        return ToolsSCPI.getCh(i);
    }



    public static void Extent(SCPIParam param) {
        Command.get().getMath_base().Extent(param.iParam1, param.dParam1, true);
    }

    public static String ExtentQ(SCPIParam param) {
        double d = Command.get().getMath_base().ExtentQ();
        return ToolsSCPI.getDouble(d);
    }

    public static void Offset(SCPIParam param) {
        Command.get().getMath_base().Offset(param.iParam1, param.dParam1, true);
    }


    public static String OffsetQ(SCPIParam param) {
        return Command.get().getMath_base().OffsetQ(param.iParam1);

    }

    public static void Operator(SCPIParam param){

        Command.get().getMath_base().Operator(param.iParam1, param.iParam2, true);
    }
    public static String OperatorQ(SCPIParam param){
       int i= Command.get().getMath_base().OperatorQ();
       return ToolsSCPI.getMathBaseOperator(i);
    }

}
