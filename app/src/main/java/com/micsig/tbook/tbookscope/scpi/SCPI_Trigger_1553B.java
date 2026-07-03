package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类，用于数据类型转换

/**
 * @auother Liwb
 * @description:
 * @data:2022-6-23 14:41
 *
 * +=============================================================================================================+
 * |                                           SCPI_Trigger_1553B                                                |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— MIL-STD-1553B总线触发模块                                                      |
 * | 核心职责 : 解析并执行1553B总线触发相关的SCPI命令（设置/查询触发源、触发类型、                                 |
 * |            控制字、数据字、远程终端地址、触发电平）                                                          |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令修改底层Command配置，                                 |
 * |            查询命令读取配置并通过ToolsSCPI转换为SCPI响应字符串                                               |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                           |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值→字符串转换）、Tools（十六进制字符串→整数转换）        |
 * | 使用场景 : 仪器作为1553B总线分析设备时，用户通过SCPI命令配置1553B协议触发条件                                  |
 * +=============================================================================================================+
 */
class SCPI_Trigger_1553B {
//        new SCPICommandStruct(":TRIGger:1553B:SOURce","SCPI_Trigger_1553B","Source"),//设置1553B触发的触发源
//            new SCPICommandStruct(":TRIGger:1553B:SOURce?","SCPI_Trigger_1553B","SourceQ"),//查询1553B触发的触发源
//            new SCPICommandStruct(":TRIGger:1553B:TYPE","SCPI_Trigger_1553B","Type"),//设置1553B触发条件
//            new SCPICommandStruct(":TRIGger:1553B:TYPE?","SCPI_Trigger_1553B","TypeQ"),//查询1553B触发条件
//            new SCPICommandStruct(":TRIGger:1553B:CSWOrd","SCPI_Trigger_1553B","CsWord"),
//            new SCPICommandStruct(":TRIGger:1553B:CSWOrd?","SCPI_Trigger_1553B","CsWordQ"),
//            new SCPICommandStruct(":TRIGger:1553B:DWORd","SCPI_Trigger_1553B","DWord"),
//            new SCPICommandStruct(":TRIGger:1553B:DWORd?","SCPI_Trigger_1553B","DWordQ"),
//            new SCPICommandStruct(":TRIGger:1553B:RTADdress","SCPI_Trigger_1553B","RtAddress"),
//            new SCPICommandStruct(":TRIGger:1553B:RTADdress?","SCPI_Trigger_1553B","RtAddressQ"),
//            new SCPICommandStruct(":TRIGger:1553B:LEVEl","SCPI_Trigger_1553B","Level"),
//            new SCPICommandStruct(":TRIGger:1553B:LEVEl?","SCPI_Trigger_1553B","LevelQ"),


    /**
     * 设置1553B触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void Source(SCPIParam param){ } // 设置1553B触发的触发源（当前为空实现）

    /**
     * 查询1553B触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void SourceQ(SCPIParam param){ } // 查询1553B触发的触发源（当前为空实现）

    /**
     * 设置1553B触发的触发类型。
     * 读取当前控制字、远程终端地址和数据字，仅更新触发类型，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为新的触发类型值
     */
    public static void Type(SCPIParam param){
        int type=Command.get().getTrigger_m1553B().getType(param.iParam1); // 获取当前1553B触发类型
        int csword=Command.get().getTrigger_m1553B().getCsWord(param.iParam1); // 获取当前控制字
        int Raddress=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1); // 获取当前远程终端地址
        int dataWord=Command.get().getTrigger_m1553B().getDataWord(param.iParam1); // 获取当前数据字
        Command.get().getTrigger_m1553B().setType(param.iParam1, param.iParam2, csword,Raddress,dataWord,true); // 设置新的触发类型，保持其余参数不变，并通知硬件

    }

    /**
     * 查询1553B触发的触发类型。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发类型的SCPI字符串表示
     */
    public static String TypeQ(SCPIParam param){
        int i=Command.get().getTrigger_m1553B().getType(param.iParam1); // 获取当前1553B触发类型
        return ToolsSCPI.getB1553bTriggerType(i); // 将触发类型枚举值转换为SCPI字符串
    }

    /**
     * 设置1553B触发的控制字。
     * 读取当前触发类型、远程终端地址和数据字，仅更新控制字，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为十六进制控制字字符串
     */
    public static void CsWord(SCPIParam param){
        int type=Command.get().getTrigger_m1553B().getType(param.iParam1); // 获取当前触发类型
        //int csword=Command.get().getTrigger_m1553B().getCsWord(param.iParam1);
        int csWord=Tools.HexStringToInt(param.sParam1); // 将十六进制字符串参数转换为整数控制字
        int Raddress=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1); // 获取当前远程终端地址
        int dataWord=Command.get().getTrigger_m1553B().getDataWord(param.iParam1); // 获取当前数据字

        Command.get().getTrigger_m1553B().setType(param.iParam1, type, csWord, Raddress,dataWord,true); // 设置新的控制字，保持其余参数不变，并通知硬件

    }

    /**
     * 查询1553B触发的控制字。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 控制字的十六进制字符串
     */
    public static String CsWordQ(SCPIParam param){
        int i=Command.get().getTrigger_m1553B().getCsWord(param.iParam1); // 获取当前控制字
        return Integer.toHexString(i); // 将控制字转换为十六进制字符串返回
    }

    /**
     * 设置1553B触发的数据字。
     * 读取当前触发类型、控制字和远程终端地址，仅更新数据字，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为十六进制数据字字符串
     */
    public static void DWord(SCPIParam param){
        int type=Command.get().getTrigger_m1553B().getType(param.iParam1); // 获取当前触发类型
        int csword=Command.get().getTrigger_m1553B().getCsWord(param.iParam1); // 获取当前控制字
        int Raddress=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1); // 获取当前远程终端地址
        //int dataWord=Command.get().getTrigger_m1553B().getDataWord(param.iParam1);
        int dataWord=Tools.HexStringToInt(param.sParam1); // 将十六进制字符串参数转换为整数数据字
        Command.get().getTrigger_m1553B().setType(param.iParam1, type, csword,Raddress, dataWord, true); // 设置新的数据字，保持其余参数不变，并通知硬件

    }

    /**
     * 查询1553B触发的数据字。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 数据字的十六进制字符串
     */
    public static String DWordQ(SCPIParam param){
        int i=Command.get().getTrigger_m1553B().getDataWord(param.iParam1); // 获取当前数据字
        return Integer.toHexString(i); // 将数据字转换为十六进制字符串返回
    }

    /**
     * 设置1553B触发的远程终端地址。
     * 读取当前触发类型、控制字和数据字，仅更新远程终端地址，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为十六进制远程终端地址字符串
     */
    public static void RtAddress(SCPIParam param){
        int type=Command.get().getTrigger_m1553B().getType(param.iParam1); // 获取当前触发类型
        int csword=Command.get().getTrigger_m1553B().getCsWord(param.iParam1); // 获取当前控制字
        //int Raddress=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1);
        int rAddr= Tools.HexStringToInt(param.sParam1); // 将十六进制字符串参数转换为整数远程终端地址
        int dataWord=Command.get().getTrigger_m1553B().getDataWord(param.iParam1); // 获取当前数据字
        Command.get().getTrigger_m1553B().setType(param.iParam1, type, csword, rAddr, dataWord,true); // 设置新的远程终端地址，保持其余参数不变，并通知硬件
    }

    /**
     * 查询1553B触发的远程终端地址。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 远程终端地址的十六进制字符串
     */
    public static String RtAddressQ(SCPIParam param){
        int i=Command.get().getTrigger_m1553B().getRtAddr(param.iParam1); // 获取当前远程终端地址
        return Integer.toHexString(i); // 将远程终端地址转换为十六进制字符串返回

    }

    /**
     * 设置1553B触发的触发电平。
     * @param param SCPI命令参数，iParam1为通道索引，dParam1为触发电平值
     */
    public static void Level(SCPIParam param){
        Command.get().getTrigger_m1553B().setLevel(param.iParam1, param.dParam1, true); // 设置1553B触发电平，并通知硬件

    }

    /**
     * 查询1553B触发的触发电平。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发电平的字符串表示
     */
    public static String LevelQ(SCPIParam param){
        double d=Command.get().getTrigger_m1553B().getLevel(param.iParam1); // 获取当前触发电平
        return String.valueOf(d); // 将触发电平转换为字符串返回
    }
}
