package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:40
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                 SCPI_Bus_Uart - UART总线SCPI命令处理                     |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中BUS#:UART子系统的Java层命令处理类                  |   *
 *   |  核心职责: 处理UART总线的RX通道、空闲电平、波特率、校验位、数据位等SCPI命令      |   *
 *   |  架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command中间件下发到设备层  |   *
 *   |  数据流向: SCPICommandDeal反射调用 → 本类静态方法 → Command中间件 → 设备层    |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、Command(中间件)、ToolsSCPI(结果格式化)        |   *
 *   |  使用场景: 上位机配置UART总线解码参数（通道/波特率/校验/位宽/显示）时调用        |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPI_Bus_Uart {
//    new SCPICommandStruct(":BUS#:UART:RX","SCPI_Bus_Uart","Rx"),
//            new SCPICommandStruct(":BUS#:UART:RX?","SCPI_Bus_Uart","RxQ"),
//            new SCPICommandStruct(":BUS#:UART:IDLElvl","SCPI_Bus_Uart","IdLevel"),
//            new SCPICommandStruct(":BUS#:UART:IDLElvl?","SCPI_Bus_Uart","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:UART:BAUDrate","SCPI_Bus_Uart","BaudRate"),
//            new SCPICommandStruct(":BUS#:UART:BAUDrate?","SCPI_Bus_Uart","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:UART:CHECK","SCPI_Bus_Uart","Check"),
//            new SCPICommandStruct(":BUS#:UART:CHECK?","SCPI_Bus_Uart","CheckQ"),
//            new SCPICommandStruct(":BUS#:UART:USERbaud","SCPI_Bus_Uart","UserBaud"),
//            new SCPICommandStruct(":BUS#:UART:USERbaud?","SCPI_Bus_Uart","UserBaudQ"),
//            new SCPICommandStruct(":BUS#:UART:WIDTh","SCPI_Bus_Uart","Width"),
//            new SCPICommandStruct(":BUS#:UART:WIDTh?","SCPI_Bus_Uart","WidthQ"),

    /**
     * 设置UART总线的RX(接收)通道。
     * 对应SCPI命令: :BUS#:UART:RX
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void Rx(SCPIParam param){
        Command.get().getBus_uart().Rx(param.iParam1, param.iParam2, true); // 通过Command中间件设置UART的RX通道
    }

    /**
     * 查询UART总线的RX(接收)通道。
     * 对应SCPI命令: :BUS#:UART:RX?
     * @param param SCPI参数，iParam1为总线编号
     * @return 通道名称字符串(如"CH1"、"CH2")
     */
    public static String RxQ(SCPIParam param){
        int i= Command.get().getBus_uart().RxQ(param.iParam1); // 查询UART的RX通道索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为通道名称字符串返回
    }

    /**
     * 设置UART总线的空闲电平。
     * 对应SCPI命令: :BUS#:UART:IDLElvl
     * @param param SCPI参数，iParam1为总线编号，iParam2为空闲电平索引
     */
    public static void IdLevel(SCPIParam param){
        Command.get().getBus_uart().IdLevel(param.iParam1, param.iParam2, true); // 通过Command中间件设置UART空闲电平
    }

    /**
     * 查询UART总线的空闲电平。
     * 对应SCPI命令: :BUS#:UART:IDLElvl?
     * @param param SCPI参数，iParam1为总线编号
     * @return 空闲电平名称字符串(如"high"、"low")
     */
    public static String IdLevelQ(SCPIParam param){
        int i= Command.get().getBus_uart().IdLevelQ(param.iParam1); // 查询UART空闲电平索引
        return ToolsSCPI.getIdLevel(i); // 将空闲电平索引转换为名称字符串返回
    }

    /**
     * 设置UART总线的波特率。
     * 对应SCPI命令: :BUS#:UART:BAUDrate
     * @param param SCPI参数，iParam1为总线编号，iParam2为波特率索引
     */
    public static void BaudRate(SCPIParam param){
        Command.get().getBus_uart().BaudRate(param.iParam1, param.iParam2, true); // 通过Command中间件设置UART波特率
    }

    /**
     * 查询UART总线的波特率。
     * 对应SCPI命令: :BUS#:UART:BAUDrate?
     * @param param SCPI参数，iParam1为总线编号
     * @return 波特率数值字符串
     */
    public static String BaudRateQ(SCPIParam param){
        int i= Command.get().getBus_uart().BaudRateQ(param.iParam1); // 查询UART波特率值
        return ""+i; // 将波特率数值转换为字符串返回
    }

    /**
     * 设置UART总线的校验方式。
     * 对应SCPI命令: :BUS#:UART:CHECK
     * @param param SCPI参数，iParam1为总线编号，iParam2为校验方式索引
     */
    public static void Check(SCPIParam param){
        Command.get().getBus_uart().Check(param.iParam1, param.iParam2, true); // 通过Command中间件设置UART校验方式
    }

    /**
     * 查询UART总线的校验方式。
     * 对应SCPI命令: :BUS#:UART:CHECK?
     * @param param SCPI参数，iParam1为总线编号
     * @return 校验方式名称字符串(如"NONE"、"ODD"、"EVEN")
     */
    public static String CheckQ(SCPIParam param){
        int i=Command.get().getBus_uart().CheckQ(param.iParam1); // 查询UART校验方式索引
        return ToolsSCPI.getUartCheck(i); // 将校验方式索引转换为名称字符串返回
    }

    /**
     * 设置UART总线的自定义波特率。
     * 对应SCPI命令: :BUS#:UART:USERbaud
     * @param param SCPI参数，iParam1为总线编号，iParam2为自定义波特率值
     */
    public static void UserBaud(SCPIParam param){
        Command.get().getBus_uart().UserBaud(param.iParam1, param.iParam2, true); // 通过Command中间件设置UART自定义波特率
    }

    /**
     * 查询UART总线的自定义波特率。
     * 对应SCPI命令: :BUS#:UART:USERbaud?
     * @param param SCPI参数，iParam1为总线编号
     * @return 自定义波特率数值字符串
     */
    public static String UserBaudQ(SCPIParam param){
        int i= Command.get().getBus_uart().UserBaudQ(param.iParam1); // 查询UART自定义波特率值
        return i+""; // 将自定义波特率数值转换为字符串返回
    }

    /**
     * 设置UART总线的数据位宽度。
     * 对应SCPI命令: :BUS#:UART:WIDTh
     * @param param SCPI参数，iParam1为总线编号，iParam2为数据位宽度索引
     */
    public static void Width(SCPIParam param){
        Command.get().getBus_uart().Width(param.iParam1, param.iParam2, true); // 通过Command中间件设置UART数据位宽度
    }

    /**
     * 查询UART总线的数据位宽度。
     * 对应SCPI命令: :BUS#:UART:WIDTh?
     * @param param SCPI参数，iParam1为总线编号
     * @return 数据位宽度名称字符串(如"5"、"6"、"7"、"8"、"9")
     */
    public static String WidthQ(SCPIParam param){
        int i=Command.get().getBus_uart().WidthQ(param.iParam1); // 查询UART数据位宽度索引
        return ToolsSCPI.getUartWidth(i); // 将数据位宽度索引转换为名称字符串返回
    }

    /**
     * 设置UART总线的显示格式。
     * 对应SCPI命令: :BUS#:UART:DISPlay
     * @param param SCPI参数，iParam1为总线编号，iParam2为显示格式索引
     */
    public static void Display(SCPIParam param){
        Command.get().getBus_uart().Display(param.iParam1, param.iParam2,true); // 通过Command中间件设置UART显示格式
    }

    /**
     * 查询UART总线的显示格式。
     * 对应SCPI命令: :BUS#:UART:DISPlay?
     * @param param SCPI参数，iParam1为总线编号
     * @return 显示格式名称字符串(如"Hex"、"Bin"、"ASCII")
     */
    public static String DisplayQ(SCPIParam param){
        int i= Command.get().getBus_uart().DisplayQ(param.iParam1); // 查询UART显示格式索引
        return ToolsSCPI.getUartDisplay(i); // 将显示格式索引转换为名称字符串返回
    }

}
