package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:55
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                  SCPI_Bus_Lin - LIN总线SCPI命令处理                      |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中BUS#:LIN子系统的Java层命令处理类                   |   *
 *   |  核心职责: 处理LIN总线的类型、通道、空闲电平、波特率及自定义波特率相关SCPI命令    |   *
 *   |  架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command中间件下发到设备层  |   *
 *   |  数据流向: SCPICommandDeal反射调用 → 本类静态方法 → Command中间件 → 设备层    |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、Command(中间件)、ToolsSCPI(结果格式化)        |   *
 *   |  使用场景: 上位机配置LIN总线解码参数（类型/通道/电平/波特率）时调用              |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPI_Bus_Lin {
//     new SCPICommandStruct(":BUS#:LIN:CHANnel","SCPI_Bus_Lin","Channel"),
//            new SCPICommandStruct(":BUS#:LIN:CHANnel?","SCPI_Bus_Lin","ChannelQ"),
//            new SCPICommandStruct(":BUS#:LIN:IDLElvl","SCPI_Bus_Lin","IdLevel"),
//            new SCPICommandStruct(":BUS#:LIN:IDLElvl?","SCPI_Bus_Lin","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:LIN:BAUDrate","SCPI_Bus_Lin","BaudRate"),
//            new SCPICommandStruct(":BUS#:LIN:BAUDrate?","SCPI_Bus_Lin","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:LIN:USERbaud","SCPI_Bus_Lin","UserBaud"),
//            new SCPICommandStruct(":BUS#:LIN:USERbaud?","SCPI_Bus_Lin","UserBaudQ"),

    /**
     * 设置LIN总线类型。
     * 对应SCPI命令: :BUS#:LIN:TYPE（内部方法）
     * @param param SCPI参数，iParam1为总线编号，iParam2为类型索引
     */
    public static void LinType(SCPIParam param) {
        Command.get().getBus_lin().LinType(param.iParam1, param.iParam2, true); // 通过Command中间件设置LIN总线类型
    }

    /**
     * 查询LIN总线类型。
     * 对应SCPI命令: :BUS#:LIN:TYPE?（内部方法）
     * @param param SCPI参数，iParam1为总线编号
     * @return 通道名称字符串
     */
    public static String LinTypeQ(SCPIParam param) {
        int i = Command.get().getBus_lin().LinTypeQ(param.iParam1); // 查询LIN总线类型索引
        return ToolsSCPI.getCh(i); // 将类型索引转换为通道名称字符串返回
    }

    /**
     * 设置LIN总线的通道源。
     * 对应SCPI命令: :BUS#:LIN:CHANnel
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void Channel(SCPIParam param){
        Command.get().getBus_lin().Channel(param.iParam1, param.iParam2, true); // 通过Command中间件设置LIN通道源
    }

    /**
     * 查询LIN总线的通道源。
     * 对应SCPI命令: :BUS#:LIN:CHANnel?
     * @param param SCPI参数，iParam1为总线编号
     * @return 通道名称字符串(如"CH1"、"CH2")
     */
    public static String ChannelQ(SCPIParam param){
        int i= Command.get().getBus_lin().ChannelQ(param.iParam1); // 查询LIN通道源索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为通道名称字符串返回
    }

    /**
     * 设置LIN总线的空闲电平。
     * 对应SCPI命令: :BUS#:LIN:IDLElvl
     * @param param SCPI参数，iParam1为总线编号，iParam2为空闲电平索引
     */
    public static void IdLevel(SCPIParam param){
        Command.get().getBus_lin().IdLevel(param.iParam1, param.iParam2,true); // 通过Command中间件设置LIN空闲电平
    }

    /**
     * 查询LIN总线的空闲电平。
     * 对应SCPI命令: :BUS#:LIN:IDLElvl?
     * @param param SCPI参数，iParam1为总线编号
     * @return 空闲电平名称字符串(如"high"、"low")
     */
    public static String IdLevelQ(SCPIParam param){
        int i= Command.get().getBus_lin().IdLevelQ(param.iParam1); // 查询LIN空闲电平索引
        return ToolsSCPI.getIdLevel(i); // 将空闲电平索引转换为名称字符串返回
    }

    /**
     * 设置LIN总线的波特率。
     * 对应SCPI命令: :BUS#:LIN:BAUDrate
     * @param param SCPI参数，iParam1为总线编号，iParam2为波特率索引
     */
    public static void BaudRate(SCPIParam param){
        Command.get().getBus_lin().BaudRate(param.iParam1, param.iParam2, true); // 通过Command中间件设置LIN波特率
    }

    /**
     * 查询LIN总线的波特率。如果为自定义波特率(索引-1)，则转查自定义波特率。
     * 对应SCPI命令: :BUS#:LIN:BAUDrate?
     * @param param SCPI参数，iParam1为总线编号
     * @return 波特率名称字符串或自定义波特率数值字符串
     */
    public static String BaudRateQ(SCPIParam param){
        int i=Command.get().getBus_lin().BaudRateQ(param.iParam1); // 查询LIN波特率索引
        if (i==-1) return UserBaudQ(param); // 索引为-1表示自定义波特率，转查自定义值
        return ToolsSCPI.getLinBaudRate(i); // 将波特率索引转换为名称字符串返回
    }

    /**
     * 设置LIN总线的自定义波特率。
     * 对应SCPI命令: :BUS#:LIN:USERbaud
     * @param param SCPI参数，iParam1为总线编号，iParam2为自定义波特率值
     */
    public static void UserBaud(SCPIParam param){
        Command.get().getBus_lin().UserBaud(param.iParam1, param.iParam2, true); // 通过Command中间件设置LIN自定义波特率
    }

    /**
     * 查询LIN总线的自定义波特率。如果未设置(索引-1)，则转查标准波特率。
     * 对应SCPI命令: :BUS#:LIN:USERbaud?
     * @param param SCPI参数，iParam1为总线编号
     * @return 自定义波特率数值字符串或标准波特率名称字符串
     */
    public static String UserBaudQ(SCPIParam param){
        int i=Command.get().getBus_lin().UserBaudQ(param.iParam1); // 查询LIN自定义波特率值
        if (i==-1) return BaudRateQ(param); // 索引为-1表示未设置自定义值，转查标准波特率
        return String.valueOf(i); // 将自定义波特率数值转换为字符串返回
    }

}
