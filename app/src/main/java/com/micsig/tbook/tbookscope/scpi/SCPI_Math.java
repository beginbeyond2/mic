package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件，用于获取底层数学运算操作接口

/*
 * +=============================================================================+
 * |  模块定位：SCPI数学运算命令处理层                                             |
 * |  核心职责：将SCPI协议中:MATH子系统的命令解析并转发至底层数学运算中间件           |
 * |  架构设计：纯静态方法类，作为SCPI命令分发器与Command中间件之间的桥接层           |
 * |  数据流向：SCPI解析器 → SCPI_Math → Command.get().getMath() → 底层           |
 * |  依赖关系：依赖SCPIParam(参数封装)、Command(命令中间件)、ToolsSCPI(工具类)     |
 * |  使用场景：数学运算功能的开启/关闭、运算类型选择、垂直参考设置                   |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Math {
//    new SCPICommandStruct(":MATH:DISPlay","SCPI_Math","Display"),//打开或关闭数学运算
//            new SCPICommandStruct(":MATH:DISPlay?","SCPI_Math","DisplayQ"),//查询数学运算打开或关闭
//            new SCPICommandStruct(":MATH:MODE","SCPI_Math","Mode"),//选择数学运算类型
//            new SCPICommandStruct(":MATH:MODE?","SCPI_Math","ModeQ"),//查询数学运算类型
//    new SCPICommandStruct(":MATH:VREF","SCPI_Math","VRef"),//选择数学运算类型
//            new SCPICommandStruct(":MATH:VREF?","SCPI_Math","VRefQ"),//查询数学运算类型

    /**
     * 打开或关闭数学运算显示
     * @param param SCPI命令参数，iParam1为运算索引，bParam1为开启/关闭标志
     */
    public static void Display(SCPIParam param) {
        Command.get().getMath().Display(param.iParam1, param.bParam1, true); // 调用底层接口设置数学运算显示状态
    }

    /**
     * 查询数学运算显示状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 数学运算显示状态字符串（ON/OFF）
     */
    public static String DisplayQ(SCPIParam param) {
        boolean b= Command.get().getMath().DisplayQ(); // 调用底层接口查询数学运算显示状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }

    /**
     * 选择数学运算类型
     * @param param SCPI命令参数，iParam1为运算索引，iParam2为运算类型
     */
    public static void Mode(SCPIParam param) {
        Command.get().getMath().Mode(param.iParam1, param.iParam2, true); // 调用底层接口设置数学运算类型
    }

    /**
     * 查询数学运算类型
     * @param param SCPI命令参数（本命令无参数）
     * @return 数学运算类型字符串
     */
    public static String ModeQ(SCPIParam param) {
        int i= Command.get().getMath().ModeQ(); // 调用底层接口查询数学运算类型
        return ToolsSCPI.getMathMode(i); // 将整数类型值转换为数学运算模式字符串
    }

    /**
     * 设置数学运算的垂直参考
     * @param param SCPI命令参数，iParam1为运算索引，iParam2为垂直参考值
     */
    public static void VRef(SCPIParam param){
        Command.get().getMath().VRef(param.iParam1, param.iParam2, true); // 调用底层接口设置数学运算垂直参考
    }

    /**
     * 查询数学运算的垂直参考
     * @param param SCPI命令参数（本命令无参数）
     * @return 垂直参考状态字符串
     */
    public static String VRefQ(SCPIParam param)
    {
        int i=Command.get().getMath().VRefQ(); // 调用底层接口查询数学运算垂直参考
        return ToolsSCPI.getMathVRef(i); // 将整数类型值转换为垂直参考字符串
    }

}
