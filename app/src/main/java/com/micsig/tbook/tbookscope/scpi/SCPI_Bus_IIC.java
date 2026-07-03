package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:11
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                  SCPI_Bus_IIC - I2C总线SCPI命令处理                      |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中BUS#:IIC子系统的Java层命令处理类                   |   *
 *   |  核心职责: 处理I2C总线的SDA/SCL通道设置相关SCPI命令                          |   *
 *   |  架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command中间件下发到设备层  |   *
 *   |  数据流向: SCPICommandDeal反射调用 → 本类静态方法 → Command中间件 → 设备层    |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、Command(中间件)、ToolsSCPI(结果格式化)        |   *
 *   |  使用场景: 上位机配置I2C总线解码SDA/SCL通道时调用                            |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPI_Bus_IIC {
//            new SCPICommandStruct(":BUS#:IIC:SDA","SCPI_Bus_IIC","SDA"),
//            new SCPICommandStruct(":BUS#:IIC:SDA?","SCPI_Bus_IIC","SDAQ"),
//            new SCPICommandStruct(":BUS#:IIC:SCL","SCPI_Bus_IIC","SCL"),
//            new SCPICommandStruct(":BUS#:IIC:SCL?","SCPI_Bus_IIC","SCLQ"),

    /**
     * 设置I2C总线的SDA(数据线)通道。
     * 对应SCPI命令: :BUS#:IIC:SDA
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void SDA(SCPIParam param){
        Command.get().getBus_iic().SDA(param.iParam1, param.iParam2, true); // 通过Command中间件设置I2C的SDA通道
    }

    /**
     * 查询I2C总线的SDA(数据线)通道。
     * 对应SCPI命令: :BUS#:IIC:SDA?
     * @param param SCPI参数，iParam1为总线编号
     * @return SDA通道名称字符串(如"CH1"、"CH2")
     */
    public static String SDAQ(SCPIParam param){
        int i=Command.get().getBus_iic().SDAQ(param.iParam1); // 查询I2C的SDA通道索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为通道名称字符串返回
    }

    /**
     * 设置I2C总线的SCL(时钟线)通道。
     * 对应SCPI命令: :BUS#:IIC:SCL
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void SCL(SCPIParam param){
        Command.get().getBus_iic().SCL(param.iParam1, param.iParam2, true); // 通过Command中间件设置I2C的SCL通道
    }

    /**
     * 查询I2C总线的SCL(时钟线)通道。
     * 对应SCPI命令: :BUS#:IIC:SCL?
     * @param param SCPI参数，iParam1为总线编号
     * @return SCL通道名称字符串(如"CH1"、"CH2")
     */
    public static String SCLQ(SCPIParam param){
        int i= Command.get().getBus_iic().SCLQ(param.iParam1); // 查询I2C的SCL通道索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为通道名称字符串返回
    }

}
