package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:04
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                  SCPI_Bus_Spi - SPI总线SCPI命令处理                      |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中BUS#:SPI子系统的Java层命令处理类                   |   *
 *   |  核心职责: 处理SPI总线的时钟/数据通道、位宽、空闲电平、边沿、CS片选等SCPI命令    |   *
 *   |  架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command中间件下发到设备层  |   *
 *   |  数据流向: SCPICommandDeal反射调用 → 本类静态方法 → Command中间件 → 设备层    |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、Command(中间件)、ToolsSCPI(结果格式化)        |   *
 *   |  使用场景: 上位机配置SPI总线解码参数（时钟/数据/位宽/CS片选）时调用              |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPI_Bus_Spi {
//            new SCPICommandStruct(":BUS#:SPI:CLK","SCPI_Bus_Spi","Clk"),
//            new SCPICommandStruct(":BUS#:SPI:CLK?","SCPI_Bus_Spi","ClkQ"),
//            new SCPICommandStruct(":BUS#:SPI:DATA","SCPI_Bus_Spi","Data"),
//            new SCPICommandStruct(":BUS#:SPI:DATA?","SCPI_Bus_Spi","DataQ"),
//            new SCPICommandStruct(":BUS#:SPI:WIDTh","SCPI_Bus_Spi","Width"),
//            new SCPICommandStruct(":BUS#:SPI:WIDTh?","SCPI_Bus_Spi","WidthQ"),
//            new SCPICommandStruct(":BUS#:SPI:IDLElvl","SCPI_Bus_Spi","IdLevel"),
//            new SCPICommandStruct(":BUS#:SPI:IDLElvl?","SCPI_Bus_Spi","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:SPI:SLOPe","SCPI_Bus_Spi","Slope"),
//            new SCPICommandStruct(":BUS#:SPI:SLOPe?","SCPI_Bus_Spi","SlopeQ"),
//            new SCPICommandStruct(":BUS#:SPI:CS","SCPI_Bus_Spi","CS"),
//            new SCPICommandStruct(":BUS#:SPI:CS?","SCPI_Bus_Spi","CSQ"),
//            new SCPICommandStruct(":BUS#:SPI:CS:SOURce","SCPI_Bus_Spi","Source"),
//            new SCPICommandStruct(":BUS#:SPI:CS:SOURce?","SCPI_Bus_Spi","SourceQ"),
//            new SCPICommandStruct(":BUS#:SPI:CS:IDLElvl","SCPI_Bus_Spi","Idlelvl"),
//            new SCPICommandStruct(":BUS#:SPI:CS:IDLElvl?","SCPI_Bus_Spi","IdlelvlQ"),

    /**
     * 设置SPI总线的时钟(CLK)通道。
     * 对应SCPI命令: :BUS#:SPI:CLK
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void Clk(SCPIParam param){
        Command.get().getBus_spi().setClock(param.iParam1, param.iParam2, true); // 通过Command中间件设置SPI时钟通道
    }

    /**
     * 查询SPI总线的时钟(CLK)通道。
     * 对应SCPI命令: :BUS#:SPI:CLK?
     * @param param SCPI参数，iParam1为总线编号
     * @return 通道名称字符串(如"CH1"、"CH2")
     */
    public static String ClkQ(SCPIParam param){
        int clock = Command.get().getBus_spi().getClock(param.iParam1); // 查询SPI时钟通道索引
        return ToolsSCPI.getCh(clock); // 将通道索引转换为通道名称字符串返回
    }

    /**
     * 设置SPI总线的数据(DATA)通道。
     * 对应SCPI命令: :BUS#:SPI:DATA
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void Data(SCPIParam param){
        Command.get().getBus_spi().setData(param.iParam1, param.iParam2, true); // 通过Command中间件设置SPI数据通道
    }

    /**
     * 查询SPI总线的数据(DATA)通道。
     * 对应SCPI命令: :BUS#:SPI:DATA?
     * @param param SCPI参数，iParam1为总线编号
     * @return 通道名称字符串(如"CH1"、"CH2")
     */
    public static String DataQ(SCPIParam param){
        int data = Command.get().getBus_spi().getData(param.iParam1); // 查询SPI数据通道索引
        return ToolsSCPI.getCh(data); // 将通道索引转换为通道名称字符串返回
    }

    /**
     * 设置SPI总线的数据位宽度。
     * 对应SCPI命令: :BUS#:SPI:WIDTh
     * @param param SCPI参数，iParam1为总线编号，iParam2为位宽索引
     */
    public static void Width(SCPIParam param){
        Command.get().getBus_spi().setBits(param.iParam1, param.iParam2, true); // 通过Command中间件设置SPI数据位宽度
    }

    /**
     * 查询SPI总线的数据位宽度。
     * 对应SCPI命令: :BUS#:SPI:WIDTh?
     * @param param SCPI参数，iParam1为总线编号
     * @return 位宽名称字符串(如"4"、"8"、"16"、"24"、"32")
     */
    public static String WidthQ(SCPIParam param){
        int bits = Command.get().getBus_spi().getBits(param.iParam1); // 查询SPI数据位宽度索引
        return ToolsSCPI.getSpiBits(bits); // 将位宽索引转换为位宽名称字符串返回
    }

    /**
     * 设置SPI总线数据线的空闲电平。
     * 对应SCPI命令: :BUS#:SPI:IDLElvl
     * @param param SCPI参数，iParam1为总线编号，iParam2为空闲电平索引
     */
    public static void IdLevel(SCPIParam param){
        Command.get().getBus_spi().setDataSwitch(param.iParam1, param.iParam2, true); // 通过Command中间件设置SPI数据线空闲电平
    }

    /**
     * 查询SPI总线数据线的空闲电平。
     * 对应SCPI命令: :BUS#:SPI:IDLElvl?
     * @param param SCPI参数，iParam1为总线编号
     * @return 空闲电平名称字符串(如"high"、"low")
     */
    public static String IdLevelQ(SCPIParam param){
        int dataSwitch = Command.get().getBus_spi().getDataSwitch(param.iParam1); // 查询SPI数据线空闲电平索引
        return ToolsSCPI.getIdLevel(dataSwitch); // 将空闲电平索引转换为名称字符串返回
    }

    /**
     * 设置SPI总线时钟线的触发边沿。
     * 对应SCPI命令: :BUS#:SPI:SLOPe
     * @param param SCPI参数，iParam1为总线编号，iParam2为边沿索引
     */
    public static void Slope(SCPIParam param){
        Command.get().getBus_spi().setClockSwitch(param.iParam1, param.iParam2, true); // 通过Command中间件设置SPI时钟线边沿
    }

    /**
     * 查询SPI总线时钟线的触发边沿。
     * 对应SCPI命令: :BUS#:SPI:SLOPe?
     * @param param SCPI参数，iParam1为总线编号
     * @return 边沿名称字符串(如"RISE"、"FALL")
     */
    public static String SlopeQ(SCPIParam param){
        int clockSwitch = Command.get().getBus_spi().getClockSwitch(param.iParam1); // 查询SPI时钟线边沿索引
        return ToolsSCPI.getTriggerRiseFall(clockSwitch); // 将边沿索引转换为名称字符串返回
    }

    /**
     * 设置SPI总线的CS(片选)使能开关。
     * 对应SCPI命令: :BUS#:SPI:CS
     * @param param SCPI参数，iParam1为总线编号，bParam1为是否启用CS
     */
    public static void CS(SCPIParam param){
        Command.get().getBus_spi().setCsEnable(param.iParam1, param.bParam1,true); // 通过Command中间件设置SPI的CS使能开关
    }

    /**
     * 查询SPI总线的CS(片选)使能状态。
     * 对应SCPI命令: :BUS#:SPI:CS?
     * @param param SCPI参数，iParam1为总线编号
     * @return CS使能状态字符串("1"或"0")
     */
    public static String CSQ(SCPIParam param){
        boolean csEnable = Command.get().getBus_spi().getCsEnable(param.iParam1); // 查询SPI的CS使能状态
        return ToolsSCPI.getOpenState(csEnable); // 将布尔值转换为"1"/"0"字符串返回
    }

    /**
     * 设置SPI总线的CS(片选)通道源。
     * 对应SCPI命令: :BUS#:SPI:CS:SOURce
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void Source(SCPIParam param){
        Command.get().getBus_spi().setCs(param.iParam1, param.iParam2, true); // 通过Command中间件设置SPI的CS通道源
    }

    /**
     * 查询SPI总线的CS(片选)通道源。
     * 对应SCPI命令: :BUS#:SPI:CS:SOURce?
     * @param param SCPI参数，iParam1为总线编号
     * @return 通道名称字符串(如"CH1"、"CH2")
     */
    public static String SourceQ(SCPIParam param){
        int cs = Command.get().getBus_spi().getCs(param.iParam1); // 查询SPI的CS通道源索引
        return ToolsSCPI.getCh(cs); // 将通道索引转换为通道名称字符串返回
    }

    /**
     * 设置SPI总线CS线的空闲电平。
     * 对应SCPI命令: :BUS#:SPI:CS:IDLElvl
     * @param param SCPI参数，iParam1为总线编号，iParam2为空闲电平索引
     */
    public static void Idlelvl(SCPIParam param){
        Command.get().getBus_spi().setCsSwitch(param.iParam1, param.iParam2, true); // 通过Command中间件设置SPI的CS线空闲电平
    }

    /**
     * 查询SPI总线CS线的空闲电平。
     * 对应SCPI命令: :BUS#:SPI:CS:IDLElvl?
     * @param param SCPI参数，iParam1为总线编号
     * @return 空闲电平名称字符串(如"high"、"low")
     */
    public static String IdlelvlQ(SCPIParam param){
        int csSwitch = Command.get().getBus_spi().getCsSwitch(param.iParam1); // 查询SPI的CS线空闲电平索引
        return ToolsSCPI.getIdLevel(csSwitch); // 将空闲电平索引转换为名称字符串返回
    }

}
