package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件，用于获取底层高级运算操作接口

/*
 * +=============================================================================+
 * |  模块定位：SCPI数学高级运算命令处理层                                         |
 * |  核心职责：将SCPI协议中:MATH:ADVanced子系统的命令解析并转发至底层高级运算中间件   |
 * |  架构设计：纯静态方法类，作为SCPI命令分发器与Command中间件之间的桥接层           |
 * |  数据流向：SCPI解析器 → SCPI_Math_Advanced → Command.get().getMath_advanced() |
 * |  依赖关系：依赖SCPIParam(参数封装)、Command(命令中间件)、ToolsSCPI(工具类)     |
 * |  使用场景：示波器高级数学运算功能，包括表达式设置、变量1/2设置、                |
 * |           运算结果垂直档位/偏移设置与查询、单位设置与查询                        |
 * +=============================================================================+
 */

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

    /**
     * 设置高级运算的表达式
     * @param param SCPI命令参数，iParam1为运算索引，sParam1为表达式字符串
     */
    public static void Expression(SCPIParam param){
        Command.get().getMath_advanced().Expression(param.iParam1, param.sParam1,true); // 调用底层接口设置高级运算表达式
    }

    /**
     * 查询高级运算的表达式
     * @param param SCPI命令参数（本命令无参数）
     * @return 表达式字符串
     */
    public static String ExpressionQ(SCPIParam param){
        return Command.get().getMath_advanced().ExpressionQ(); // 调用底层接口查询高级运算表达式
    }

    /**
     * 设置高级运算表达式中的变量1
     * @param param SCPI命令参数，iParam1为运算索引，dParam1为变量1的值
     */
    public static void Var1(SCPIParam param)
    {
        Command.get().getMath_advanced().Var1(param.iParam1, param.dParam1, true); // 调用底层接口设置变量1
    }

    /**
     * 查询高级运算表达式中的变量1
     * @param param SCPI命令参数（本命令无参数）
     * @return 变量1的值字符串
     */
    public static String Var1Q(SCPIParam param){
        double d=Command.get().getMath_advanced().Var1Q(); // 调用底层接口查询变量1
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置高级运算表达式中的变量2
     * @param param SCPI命令参数，iParam1为运算索引，dParam1为变量2的值
     */
    public static void Var2(SCPIParam param){
        Command.get().getMath_advanced().Var2(param.iParam1, param.dParam1, true); // 调用底层接口设置变量2
    }

    /**
     * 查询高级运算表达式中的变量2
     * @param param SCPI命令参数（本命令无参数）
     * @return 变量2的值字符串
     */
    public static String Var2Q(SCPIParam param){
        double d=Command.get().getMath_advanced().Var2Q(); // 调用底层接口查询变量2
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置高级运算结果的垂直档位
     * @param param SCPI命令参数，iParam1为运算索引，dParam1为垂直档位值
     */
    public static void Extent(SCPIParam param){
        Command.get().getMath_advanced().Extent(param.iParam1, param.dParam1, true); // 调用底层接口设置高级运算结果垂直档位
    }

    /**
     * 查询高级运算结果的垂直档位
     * @param param SCPI命令参数（本命令无参数）
     * @return 垂直档位值字符串
     */
    public static String ExtentQ(SCPIParam param){
        double d=Command.get().getMath_advanced().ExtentQ(); // 调用底层接口查询高级运算结果垂直档位
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置高级运算结果的垂直偏移
     * @param param SCPI命令参数，iParam1为运算索引，dParam1为垂直偏移值
     */
    public static void Offset(SCPIParam param){
        Command.get().getMath_advanced().Offset(param.iParam1, param.dParam1, true); // 调用底层接口设置高级运算结果垂直偏移
    }

    /**
     * 查询高级运算结果的垂直偏移
     * @param param SCPI命令参数，iParam1为运算索引
     * @return 垂直偏移值字符串
     */
    public static String OffsetQ(SCPIParam param){
        return Command.get().getMath_advanced().OffsetQ(param.iParam1); // 调用底层接口查询高级运算结果垂直偏移
    }

    /**
     * 设置高级运算结果的单位
     * @param param SCPI命令参数，iParam1为运算索引，sParam1为单位字符串
     */
    public static void Unit(SCPIParam param){
        Command.get().getMath_advanced().Unit(param.iParam1, param.sParam1,true); // 调用底层接口设置运算结果单位
    }

    /**
     * 查询高级运算结果的单位
     * @param param SCPI命令参数（本命令无参数）
     * @return 单位字符串
     */
    public static String UnitQ(SCPIParam param){
        return Command.get().getMath_advanced().UnitQ(); // 调用底层接口查询运算结果单位
    }

}
