package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:39
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                    SCPI_Bus - 通用总线SCPI命令处理                        |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中BUS#子系统的通用命令处理类                         |   *
 *   |  核心职责: 处理总线的显示开关、类型、模式、触发电平及数据查询等通用SCPI命令       |   *
 *   |  架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command中间件下发到设备层  |   *
 *   |  数据流向: SCPICommandDeal反射调用 → 本类静态方法 → Command中间件 → 设备层    |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、Command(中间件)、ToolsSCPI(结果格式化)        |   *
 *   |  使用场景: 上位机配置总线通用参数（显示/类型/模式/电平/数据）时调用               |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPI_Bus {
//            new SCPICommandStruct(":BUS#:DISPlay","SCPI_Bus","Display"),
//            new SCPICommandStruct(":BUS#:DISPlay?","SCPI_Bus","DisplayQ"), //
//            new SCPICommandStruct(":BUS#:TYPE","SCPI_Bus","Type"),
//            new SCPICommandStruct(":BUS#:TYPE?","SCPI_Bus","TypeQ"),
//            new SCPICommandStruct(":BUS#:MODE","SCPI_Bus","Mode"),
//            new SCPICommandStruct(":BUS#:MODE?","SCPI_Bus","ModeQ"),
//            new SCPICommandStruct(":BUS#:LEVel","SCPI_Bus","Level"),
//            new SCPICommandStruct(":BUS#:LEVel?","SCPI_Bus","LevelQ"),
//            new SCPICommandStruct(":BUS#:HLEVel","SCPI_Bus","HLevel"),
//            new SCPICommandStruct(":BUS#:HLEVel?","SCPI_Bus","HLevelQ"),
//            new SCPICommandStruct(":BUS#:LLEVel","SCPI_Bus","LLevel"),
//            new SCPICommandStruct(":BUS#:LLEVel?","SCPI_Bus","LLevelQ"),
//            new SCPICommandStruct(":BUS#:Data?","SCPI_Bus","DataQ"),

    /**
     * 设置总线显示开关。
     * 对应SCPI命令: :BUS#:DISPlay
     * @param param SCPI参数，iParam1为总线编号，bParam1为是否显示
     */
    public static void Display(SCPIParam param){
        Command.get().getBus().Display(param.iParam1, param.bParam1, true); // 通过Command中间件设置总线显示开关
    }

    /**
     * 查询总线显示状态。
     * 对应SCPI命令: :BUS#:DISPlay?
     * @param param SCPI参数，iParam1为总线编号
     * @return 显示状态字符串
     */
    public static String DisplayQ(SCPIParam param){
        return Command.get().getBus().DisplayQ(param.iParam1); // 查询总线显示状态并返回
    }

    /**
     * 设置总线类型（UART/LIN/CAN/SPI/I2C/429/1553B）。
     * 对应SCPI命令: :BUS#:TYPE
     * @param param SCPI参数，iParam1为总线编号，iParam2为总线类型索引
     */
    public static void Type(SCPIParam param) {
        Command.get().getBus().Type(param.iParam1, param.iParam2, true); // 通过Command中间件设置总线类型
    }

    /**
     * 查询总线类型。
     * 对应SCPI命令: :BUS#:TYPE?
     * @param param SCPI参数，iParam1为总线编号
     * @return 总线类型名称字符串(如"Uart"、"CAN"等)
     */
    public static String TypeQ(SCPIParam param) {
        int i= Command.get().getBus().TypeQ(param.iParam1); // 查询总线类型索引
        return ToolsSCPI.getBusType(i); // 将总线类型索引转换为名称字符串返回
    }

    /**
     * 设置总线显示模式（图形/文本）。
     * 对应SCPI命令: :BUS#:MODE
     * @param param SCPI参数，iParam1为总线编号，iParam2为模式索引
     */
    public static void Mode(SCPIParam param){
        Command.get().getBus().Mode(param.iParam1, param.iParam2, true); // 通过Command中间件设置总线显示模式
    }

    /**
     * 查询总线显示模式。
     * 对应SCPI命令: :BUS#:MODE?
     * @param param SCPI参数，iParam1为总线编号
     * @return 显示模式名称字符串(如"GRAP"、"TXT")
     */
    public static String ModeQ(SCPIParam param){
        int i = Command.get().getBus().ModeQ(param.iParam1); // 查询总线显示模式索引
        return ToolsSCPI.getSerialBusMode(i); // 将模式索引转换为名称字符串返回
    }

    /**
     * 设置总线触发电平。
     * 对应SCPI命令: :BUS#:LEVel
     * @param param SCPI参数，iParam1为总线编号，iParam2为电平索引，dParam1为电平值
     */
    public static void Level(SCPIParam param){
        Command.get().getBus().Level(param.iParam1, param.iParam2, param.dParam1, true); // 通过Command中间件设置总线触发电平
    }

    /**
     * 查询总线触发电平。
     * 对应SCPI命令: :BUS#:LEVel?
     * @param param SCPI参数，iParam1为总线编号，iParam2为电平索引
     * @return 电平值字符串
     */
    public static String LevelQ(SCPIParam param){
        double v = Command.get().getBus().LevelQ(param.iParam1, param.iParam2); // 查询总线触发电平值
        return String.valueOf(v); // 将double转换为字符串返回
    }

    /**
     * 设置总线高触发电平。
     * 对应SCPI命令: :BUS#:HLEVel
     * @param param SCPI参数，iParam1为总线编号，iParam2为电平索引，dParam1为电平值
     */
    public static void HLevel(SCPIParam param){
        Command.get().getBus().HLevel(param.iParam1, param.iParam2, param.dParam1, true); // 通过Command中间件设置总线高触发电平
    }

    /**
     * 查询总线高触发电平。
     * 对应SCPI命令: :BUS#:HLEVel?
     * @param param SCPI参数，iParam1为总线编号，iParam2为电平索引
     * @return 高电平值字符串
     */
    public static String HLevelQ(SCPIParam param){
        double d= Command.get().getBus().HLevelQ(param.iParam1, param.iParam2); // 查询总线高触发电平值
        return String.valueOf(d); // 将double转换为字符串返回
    }

    /**
     * 设置总线低触发电平。
     * 对应SCPI命令: :BUS#:LLEVel
     * @param param SCPI参数，iParam1为总线编号，iParam2为电平索引，dParam1为电平值
     */
    public static void LLevel(SCPIParam param){
        Command.get().getBus().LLevel(param.iParam1, param.iParam2, param.dParam1, true); // 通过Command中间件设置总线低触发电平
    }

    /**
     * 查询总线低触发电平。
     * 对应SCPI命令: :BUS#:LLEVel?
     * @param param SCPI参数，iParam1为总线编号，iParam2为电平索引
     * @return 低电平值字符串
     */
    public static String LLevelQ(SCPIParam param){
        double d= Command.get().getBus().LLevelQ(param.iParam1, param.iParam2); // 查询总线低触发电平值
        return String.valueOf(d); // 将double转换为字符串返回
    }

    /**
     * 查询总线解码数据。
     * 对应SCPI命令: :BUS#:Data?
     * @param param SCPI参数，iParam1为总线编号
     * @return 解码数据字符串
     */
    public static String DataQ(SCPIParam param){
        return Command.get().getBus().DataQ(param.iParam1); // 查询总线解码数据并返回
    }
}
