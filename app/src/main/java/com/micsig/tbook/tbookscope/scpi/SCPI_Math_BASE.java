package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件，用于获取底层基础运算操作接口

/*
 * +=============================================================================+
 * |  模块定位：SCPI数学基础运算命令处理层                                         |
 * |  核心职责：将SCPI协议中:MATH:BASE子系统的命令解析并转发至底层基础运算中间件       |
 * |  架构设计：纯静态方法类，作为SCPI命令分发器与Command中间件之间的桥接层           |
 * |  数据流向：SCPI解析器 → SCPI_Math_BASE → Command.get().getMath_base() → 底层  |
 * |  依赖关系：依赖SCPIParam(参数封装)、Command(命令中间件)、ToolsSCPI(工具类)     |
 * |  使用场景：示波器基础数学运算功能（加/减/乘/除），包括信源1/2选择、              |
 * |           运算符设置、运算结果垂直档位/偏移设置与查询                            |
 * +=============================================================================+
 */

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

    /**
     * 设置基础运算的信源1
     * @param param SCPI命令参数，iParam1为运算索引，iParam2为信源1通道
     */
    public static void S1(SCPIParam param) {
        // if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 0) {
        Command.get().getMath_base().S1(param.iParam1, param.iParam2, true); // 调用底层接口设置基础运算信源1
        // }
    }

    /**
     * 查询基础运算的信源1
     * @param param SCPI命令参数（本命令无参数）
     * @return 信源1通道字符串
     */
    public static String S1Q(SCPIParam param) {
        int i = Command.get().getMath_base().S1Q(); // 调用底层接口查询基础运算信源1
        return ToolsSCPI.getCh(i); // 将整数类型值转换为通道标识字符串
    }

    /**
     * 设置基础运算的信源2
     * @param param SCPI命令参数，iParam1为运算索引，iParam2为信源2通道
     */
    public static void S2(SCPIParam param) {
        //if (Command.get().getMath().DisplayQ() && Command.get().getMath().ModeQ() == 0) {
        Command.get().getMath_base().S2(param.iParam1, param.iParam2, true); // 调用底层接口设置基础运算信源2
        //}
    }

    /**
     * 查询基础运算的信源2
     * @param param SCPI命令参数（本命令无参数）
     * @return 信源2通道字符串
     */
    public static String S2Q(SCPIParam param) {
        int i = Command.get().getMath_base().S2Q(); // 调用底层接口查询基础运算信源2
        return ToolsSCPI.getCh(i); // 将整数类型值转换为通道标识字符串
    }



    /**
     * 设置基础运算结果的垂直档位
     * @param param SCPI命令参数，iParam1为运算索引，dParam1为垂直档位值
     */
    public static void Extent(SCPIParam param) {
        Command.get().getMath_base().Extent(param.iParam1, param.dParam1, true); // 调用底层接口设置基础运算结果垂直档位
    }

    /**
     * 查询基础运算结果的垂直档位
     * @param param SCPI命令参数（本命令无参数）
     * @return 垂直档位值字符串
     */
    public static String ExtentQ(SCPIParam param) {
        double d = Command.get().getMath_base().ExtentQ(); // 调用底层接口查询基础运算结果垂直档位
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置基础运算结果的垂直偏移
     * @param param SCPI命令参数，iParam1为运算索引，dParam1为垂直偏移值
     */
    public static void Offset(SCPIParam param) {
        Command.get().getMath_base().Offset(param.iParam1, param.dParam1, true); // 调用底层接口设置基础运算结果垂直偏移
    }


    /**
     * 查询基础运算结果的垂直偏移
     * @param param SCPI命令参数，iParam1为运算索引
     * @return 垂直偏移值字符串
     */
    public static String OffsetQ(SCPIParam param) {
        return Command.get().getMath_base().OffsetQ(param.iParam1); // 调用底层接口查询基础运算结果垂直偏移

    }

    /**
     * 设置基础运算的运算符（加/减/乘/除）
     * @param param SCPI命令参数，iParam1为运算索引，iParam2为运算符类型
     */
    public static void Operator(SCPIParam param){

        Command.get().getMath_base().Operator(param.iParam1, param.iParam2, true); // 调用底层接口设置运算符
    }

    /**
     * 查询基础运算的运算符
     * @param param SCPI命令参数（本命令无参数）
     * @return 运算符字符串
     */
    public static String OperatorQ(SCPIParam param){
       int i= Command.get().getMath_base().OperatorQ(); // 调用底层接口查询运算符
       return ToolsSCPI.getMathBaseOperator(i); // 将整数类型值转换为运算符字符串
    }

}
