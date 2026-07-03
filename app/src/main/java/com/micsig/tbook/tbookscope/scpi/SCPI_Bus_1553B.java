package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:13
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                SCPI_Bus_1553B - MIL-STD-1553B总线SCPI命令处理            |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中BUS#:1553B子系统的Java层命令处理类                 |   *
 *   |  核心职责: 处理1553B总线通道源设置和显示模式相关的SCPI命令                    |   *
 *   |  架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command中间件下发到设备层  |   *
 *   |  数据流向: SCPICommandDeal反射调用 → 本类静态方法 → Command中间件 → 设备层    |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、Command(中间件)、ToolsSCPI(结果格式化)        |   *
 *   |  使用场景: 上位机配置1553B总线解码通道源或显示格式时调用                       |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPI_Bus_1553B {
//            new SCPICommandStruct(":BUS#:1553B:CHANnel","SCPI_Bus_1553B","Channel"),
//            new SCPICommandStruct(":BUS#:1553B:CHANnel?","SCPI_Bus_1553B","ChannelQ"),
//            new SCPICommandStruct(":BUS#:1553B:DISPlay","SCPI_Bus_1553B","Display"),
//            new SCPICommandStruct(":BUS#:1553B:DISPlay?","SCPI_Bus_1553B","DisplayQ"),

    /**
     * 设置1553B总线解码通道源。
     * 对应SCPI命令: :BUS#:1553B:SOURce
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void Channel(SCPIParam param){
        Command.get().getBus_1553B().Channel(param.iParam1, param.iParam2, true); // 通过Command中间件设置1553B通道源
    }

    /**
     * 查询1553B总线解码通道源。
     * 对应SCPI命令: :BUS#:1553B:SOURce?
     * @param param SCPI参数，iParam1为总线编号
     * @return 通道名称字符串(如"CH1"、"CH2")
     */
    public static String ChannelQ(SCPIParam param){
        int i= Command.get().getBus_1553B().ChannelQ(param.iParam1); // 查询1553B通道源索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为通道名称字符串返回
    }

    /**
     * 设置1553B总线显示格式。
     * 对应SCPI命令: :BUS#:1553B:DISPlay
     * @param param SCPI参数，iParam1为总线编号，iParam2为显示格式索引
     */
    public static void Display(SCPIParam param){
        Command.get().getBus_1553B().Display(param.iParam1, param.iParam2, true); // 通过Command中间件设置1553B显示格式
    }

    /**
     * 查询1553B总线显示格式。
     * 对应SCPI命令: :BUS#:1553B:DISPlay?
     * @param param SCPI参数，iParam1为总线编号
     * @return 显示格式名称字符串(如"Bin"、"Hex")
     */
    public static String DisplayQ(SCPIParam param){
        int i=Command.get().getBus_1553B().DisplayQ(param.iParam1); // 查询1553B显示格式索引
        return ToolsSCPI.get429Display(i); // 将显示格式索引转换为名称字符串返回（复用429的显示格式）
    }
}
