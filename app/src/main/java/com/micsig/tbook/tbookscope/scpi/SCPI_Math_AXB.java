package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件，用于获取底层AX+B运算操作接口

/*
 * +=============================================================================+
 * |  模块定位：SCPI数学AX+B运算命令处理层                                         |
 * |  核心职责：将SCPI协议中:MATH:AX+B子系统的命令解析并转发至底层AX+B运算中间件      |
 * |  架构设计：纯静态方法类，作为SCPI命令分发器与Command中间件之间的桥接层           |
 * |  数据流向：SCPI解析器 → SCPI_Math_AXB → Command.get().getMath_axb() → 底层   |
 * |  依赖关系：依赖SCPIParam(参数封装)、Command(命令中间件)、ToolsSCPI(工具类)     |
 * |  使用场景：示波器AX+B线性运算功能，包括信源选择、系数A/B设置、                  |
 * |           单位设置、运算结果垂直档位/偏移设置与查询                              |
 * +=============================================================================+
 */

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

    /**
     * 设置AX+B运算的信源通道
     * @param param SCPI命令参数，iParam1为运算索引，iParam2为信源通道
     */
    public static void Source(SCPIParam param){
        Command.get().getMath_axb().Source(param.iParam1, param.iParam2, true); // 调用底层接口设置AX+B运算信源
    }

    /**
     * 查询AX+B运算的信源通道
     * @param param SCPI命令参数（本命令无参数）
     * @return 信源通道字符串
     */
    public static String SourceQ(SCPIParam param)
    {
        int i= Command.get().getMath_axb().SourceQ(); // 调用底层接口查询AX+B运算信源
        return ToolsSCPI.getCh(i); // 将整数类型值转换为通道标识字符串
    }

    /**
     * 设置AX+B运算的系数A
     * @param param SCPI命令参数，iParam1为运算索引，sParam1为系数A的值（字符串形式）
     */
    public static void A(SCPIParam param){
        Command.get().getMath_axb().A(param.iParam1, param.sParam1, true); // 调用底层接口设置系数A
    }

    /**
     * 查询AX+B运算的系数A
     * @param param SCPI命令参数（本命令无参数）
     * @return 系数A值字符串
     */
    public static String AQ(SCPIParam param){
        return Command.get().getMath_axb().AQ(); // 调用底层接口查询系数A
    }

    /**
     * 设置AX+B运算的系数B
     * @param param SCPI命令参数，iParam1为运算索引，sParam1为系数B的值（字符串形式）
     */
    public static void B(SCPIParam param){
        Command.get().getMath_axb().B(param.iParam1, param.sParam1, true); // 调用底层接口设置系数B
    }

    /**
     * 查询AX+B运算的系数B
     * @param param SCPI命令参数（本命令无参数）
     * @return 系数B值字符串
     */
    public static String BQ(SCPIParam param){
        return Command.get().getMath_axb().BQ(); // 调用底层接口查询系数B
    }

    /**
     * 设置AX+B运算结果的单位
     * @param param SCPI命令参数，iParam1为运算索引，sParam1为单位字符串
     */
    public static void Unit(SCPIParam param){
        Command.get().getMath_axb().Unit(param.iParam1, param.sParam1, true); // 调用底层接口设置运算结果单位
    }

    /**
     * 查询AX+B运算结果的单位
     * @param param SCPI命令参数（本命令无参数）
     * @return 单位字符串
     */
    public static String UnitQ(SCPIParam param){
        return Command.get().getMath_axb().UnitQ(); // 调用底层接口查询运算结果单位
    }

    /**
     * 设置AX+B运算结果的垂直档位
     * @param param SCPI命令参数，iParam1为运算索引，dParam1为垂直档位值
     */
    public static void Extent(SCPIParam param){
        Command.get().getMath_axb().Extent(param.iParam1, param.dParam1,true); // 调用底层接口设置AX+B运算结果垂直档位
    }

    /**
     * 查询AX+B运算结果的垂直档位
     * @param param SCPI命令参数（本命令无参数）
     * @return 垂直档位值字符串
     */
    public static String ExtentQ(SCPIParam param){
        double d=Command.get().getMath_axb().ExtentQ(); // 调用底层接口查询AX+B运算结果垂直档位
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置AX+B运算结果的垂直偏移
     * @param param SCPI命令参数，iParam1为运算索引，dParam1为垂直偏移值
     */
    public static void Offset(SCPIParam param){
        Command.get().getMath_axb().Offset(param.iParam1, param.dParam1, true); // 调用底层接口设置AX+B运算结果垂直偏移
    }

    /**
     * 查询AX+B运算结果的垂直偏移
     * @param param SCPI命令参数，iParam1为运算索引
     * @return 垂直偏移值字符串
     */
    public static String OffsetQ(SCPIParam param){
        return Command.get().getMath_axb().OffsetQ(param.iParam1); // 调用底层接口查询AX+B运算结果垂直偏移

    }

}
