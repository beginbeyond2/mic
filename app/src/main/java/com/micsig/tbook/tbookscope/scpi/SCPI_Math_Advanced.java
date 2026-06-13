package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Math_Advanced {
//     new SCPICommandStruct(":MATH:ADVanced:EXPRession","SCPI_Math_Advanced","Expression"),//设置高级运算的表达式
//            new SCPICommandStruct(":MATH:ADVanced:EXPRession?","SCPI_Math_Advanced","ExpressionQ"),//查询高级运算的表达式
//            new SCPICommandStruct(":MATH:ADVanced:VAR1","SCPI_Math_Advanced","Var1"),//设置高级运算表达式中的变量1
//            new SCPICommandStruct(":MATH:ADVanced:VAR1?","SCPI_Math_Advanced","Var1Q"),//查询高级运算表达式中的变量1
//            new SCPICommandStruct(":MATH:ADVanced:VAR2","SCPI_Math_Advanced","Var2"),//设置高级运算表达式中的变量2
//            new SCPICommandStruct(":MATH:ADVanced:VAR2?","SCPI_Math_Advanced","Var2Q"),//查询高级运算表达式中的变量2
//            new SCPICommandStruct(":MATH:ADVanced:EXTent","SCPI_Math_Advanced","Extent"),//设置高级运算结果的垂直档位
//            new SCPICommandStruct(":MATH:ADVanced:EXTent?","SCPI_Math_Advanced","ExtentQ"),//查询高级运算结果的垂直档位
//            new SCPICommandStruct(":MATH:ADVanced:OFFSet","SCPI_Math_Advanced","Offset"),//设置高级运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:ADVanced:OFFSet?","SCPI_Math_Advanced","OffsetQ"),//查询高级运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:ADVanced:UNIT", "SCPI_Math_Advanced","Unit"),
//            new SCPICommandStruct(":MATH:ADVanced:UNIT?", "SCPI_Math_Advanced","UnitQ"),

    public static void Expression(SCPIParam param){
        Command.get().getMath_advanced().Expression(param.iParam1, param.sParam1,true);
    }
    public static String ExpressionQ(SCPIParam param){
        return Command.get().getMath_advanced().ExpressionQ();
    }
    public static void Var1(SCPIParam param)
    {
        Command.get().getMath_advanced().Var1(param.iParam1, param.dParam1, true);
    }
    public static String Var1Q(SCPIParam param){
        double d=Command.get().getMath_advanced().Var1Q();
        return ToolsSCPI.getDouble(d);
    }
    public static void Var2(SCPIParam param){
        Command.get().getMath_advanced().Var2(param.iParam1, param.dParam1, true);
    }
    public static String Var2Q(SCPIParam param){
        double d=Command.get().getMath_advanced().Var2Q();
        return ToolsSCPI.getDouble(d);
    }
    public static void Extent(SCPIParam param){
        Command.get().getMath_advanced().Extent(param.iParam1, param.dParam1, true);
    }
    public static String ExtentQ(SCPIParam param){
        double d=Command.get().getMath_advanced().ExtentQ();
        return ToolsSCPI.getDouble(d);
    }
    public static void Offset(SCPIParam param){
        Command.get().getMath_advanced().Offset(param.iParam1, param.dParam1, true);
    }
    public static String OffsetQ(SCPIParam param){
        return Command.get().getMath_advanced().OffsetQ(param.iParam1);
    }
    public static void Unit(SCPIParam param){
        Command.get().getMath_advanced().Unit(param.iParam1, param.sParam1,true);
    }
    public static String UnitQ(SCPIParam param){
        return Command.get().getMath_advanced().UnitQ();
    }

}
