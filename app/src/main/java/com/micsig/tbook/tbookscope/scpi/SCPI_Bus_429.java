package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:15
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |               SCPI_Bus_429 - ARINC429总线SCPI命令处理                    |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中BUS#:429子系统的Java层命令处理类                   |   *
 *   |  核心职责: 处理ARINC429总线的通道源、格式、显示、波特率相关SCPI命令            |   *
 *   |  架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command中间件下发到设备层  |   *
 *   |  数据流向: SCPICommandDeal反射调用 → 本类静态方法 → Command中间件 → 设备层    |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、Command(中间件)、ToolsSCPI(结果格式化)        |   *
 *   |  使用场景: 上位机配置ARINC429总线解码参数时调用                               |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPI_Bus_429 {
//    new SCPICommandStruct(":BUS#:429:SOURce","SCPI_Bus_429","Source"),
//            new SCPICommandStruct(":BUS#:429:SOURce?","SCPI_Bus_429","SourceQ"),
//            new SCPICommandStruct(":BUS#:429:FORMat","SCPI_Bus_429","Format"),
//            new SCPICommandStruct(":BUS#:429:FORMat?","SCPI_Bus_429","FormatQ"),
//            new SCPICommandStruct(":BUS#:429:DISPlay","SCPI_Bus_429","Display"),
//            new SCPICommandStruct(":BUS#:429:DISPlay?","SCPI_Bus_429","DisplayQ"),
//            new SCPICommandStruct(":BUS#:429:BANDrate","SCPI_Bus_429","BandRate"),
//            new SCPICommandStruct(":BUS#:429:BANDrate?","SCPI_Bus_429","BandRateQ"),

    /**
     * 设置ARINC429总线的通道源。
     * 对应SCPI命令: :BUS#:429:SOURce
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void Source(SCPIParam param){
        Command.get().getBus_429().Source(param.iParam1, param.iParam2, true); // 通过Command中间件设置429通道源
    }

    /**
     * 查询ARINC429总线的通道源。
     * 对应SCPI命令: :BUS#:429:SOURce?
     * @param param SCPI参数，iParam1为总线编号
     * @return 通道名称字符串(如"CH1"、"CH2")
     */
    public static String SourceQ(SCPIParam param){
        int i= Command.get().getBus_429().SourceQ(param.iParam1); // 查询429通道源索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为通道名称字符串返回
    }

    /**
     * 设置ARINC429总线的解码格式。
     * 对应SCPI命令: :BUS#:429:FORMat
     * @param param SCPI参数，iParam1为总线编号，iParam2为格式索引
     */
    public static void Format(SCPIParam param){
        Command.get().getBus_429().Format(param.iParam1, param.iParam2, true); // 通过Command中间件设置429解码格式
    }

    /**
     * 查询ARINC429总线的解码格式。
     * 对应SCPI命令: :BUS#:429:FORMat?
     * @param param SCPI参数，iParam1为总线编号
     * @return 格式名称字符串(如"LABEL+DATA"、"L+D+SSM")
     */
    public static String FormatQ(SCPIParam param){
        int i=Command.get().getBus_429().FormatQ(param.iParam1); // 查询429解码格式索引
        return ToolsSCPI.get429Format(i); // 将格式索引转换为格式名称字符串返回
    }

    /**
     * 设置ARINC429总线的显示格式。
     * 对应SCPI命令: :BUS#:429:DISPlay
     * @param param SCPI参数，iParam1为总线编号，iParam2为显示格式索引
     */
    public static void Display(SCPIParam param){
        Command.get().getBus_429().Display(param.iParam1, param.iParam2, true); // 通过Command中间件设置429显示格式
    }

    /**
     * 查询ARINC429总线的显示格式。
     * 对应SCPI命令: :BUS#:429:DISPlay?
     * @param param SCPI参数，iParam1为总线编号
     * @return 显示格式名称字符串(如"Bin"、"Hex")
     */
    public static String DisplayQ(SCPIParam param){
        int i=Command.get().getBus_429().DisplayQ(param.iParam1); // 查询429显示格式索引
        return ToolsSCPI.get429Display(i); // 将显示格式索引转换为名称字符串返回
    }

    /**
     * 设置ARINC429总线的波特率。
     * 对应SCPI命令: :BUS#:429:BANDrate
     * @param param SCPI参数，iParam1为总线编号，iParam2为波特率索引
     */
    public static void BandRate(SCPIParam param){
        Command.get().getBus_429().BaudRate(param.iParam1, param.iParam2, true); // 通过Command中间件设置429波特率
    }

    /**
     * 查询ARINC429总线的波特率。
     * 对应SCPI命令: :BUS#:429:BANDrate?
     * @param param SCPI参数，iParam1为总线编号
     * @return 波特率名称字符串(如"12500"、"100000")
     */
    public static String BandRateQ(SCPIParam param){
        int i=Command.get().getBus_429().BaudRateQ(param.iParam1); // 查询429波特率索引
        return ToolsSCPI.get429BaudRate(i); // 将波特率索引转换为波特率名称字符串返回
    }

}
