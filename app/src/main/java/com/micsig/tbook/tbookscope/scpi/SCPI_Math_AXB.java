package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-24 18:03
 */
public class SCPI_Math_AXB {
//    new SCPICommandStruct(":MATH:AX+B:SOURce","SCPI_Math_AXB","Source"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:SOURce?","SCPI_Math_AXB","SourceQ"),//查询AXB运算的信源
    //            new SCPICommandStruct(":MATH:AX+B:A","SCPI_Math_AXB","A"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:A?","SCPI_Math_AXB","AQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:B","SCPI_Math_AXB","B"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:B?","SCPI_Math_AXB","BQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:UNIT","SCPI_Math_AXB","Unit"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:UNIT?","SCPI_Math_AXB","UnitQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:EXTent","SCPI_Math_AXB","Extent"),//设置AXB运算结果的垂直档位
//            new SCPICommandStruct(":MATH:AX+B:EXTent?","SCPI_Math_AXB","ExtentQ"),//查询AXB运算结果的垂直档位
//            new SCPICommandStruct(":MATH:AX+B:OFFSet","SCPI_Math_AXB","Offset"),//设置AXB运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:AX+B:OFFSet?","SCPI_Math_AXB","OffsetQ"),//查询AXB运算结果的垂直偏移

    public static void Source(SCPIParam param){
        Command.get().getMath_axb().Source(param.iParam1, param.iParam2, true);
    }
    public static String SourceQ(SCPIParam param)
    {
        int i= Command.get().getMath_axb().SourceQ();
        return ToolsSCPI.getCh(i);
    }

    public static void A(SCPIParam param){
        Command.get().getMath_axb().A(param.iParam1, param.sParam1, true);
    }
    public static String AQ(SCPIParam param){
        return Command.get().getMath_axb().AQ();
    }
    public static void B(SCPIParam param){
        Command.get().getMath_axb().B(param.iParam1, param.sParam1, true);
    }
    public static String BQ(SCPIParam param){
        return Command.get().getMath_axb().BQ();
    }
    public static void Unit(SCPIParam param){
        Command.get().getMath_axb().Unit(param.iParam1, param.sParam1, true);
    }
    public static String UnitQ(SCPIParam param){
        return Command.get().getMath_axb().UnitQ();
    }

    public static void Extent(SCPIParam param){
        Command.get().getMath_axb().Extent(param.iParam1, param.dParam1,true);
    }
    public static String ExtentQ(SCPIParam param){
        double d=Command.get().getMath_axb().ExtentQ();
        return ToolsSCPI.getDouble(d);
    }
    public static void Offset(SCPIParam param){
        Command.get().getMath_axb().Offset(param.iParam1, param.dParam1, true);
    }
    public static String OffsetQ(SCPIParam param){
        return Command.get().getMath_axb().OffsetQ(param.iParam1);

    }

}
