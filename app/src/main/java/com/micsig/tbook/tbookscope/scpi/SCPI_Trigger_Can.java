package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串行总线工具类，用于数据格式转换

/**
 * Created by liwb on 2018/1/12.
 *
 * +=============================================================================================================+
 * |                                           SCPI_Trigger_Can                                                  |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— CAN总线触发模块                                                               |
 * | 核心职责 : 解析并执行CAN总线触发相关的SCPI命令（设置/查询触发源、触发类型、                                   |
 * |            触发ID、DLC、触发数据、触发电平）                                                               |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令读取当前值后仅修改目标字段，                           |
 * |            查询命令读取配置并通过ToolsSCPI转换为SCPI响应字符串                                               |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                         |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值→字符串转换、DLC转换）、                               |
 * |            SerialsUtils（串行总线数据格式转换）                                                             |
 * | 使用场景 : 仪器作为CAN总线分析设备时，用户通过SCPI命令配置CAN协议触发条件                                    |
 * +=============================================================================================================+
 */

public class SCPI_Trigger_Can {
//    new SCPICommandStruct(":TRIGger:CAN:SOURce","SCPI_Trigger_Can","Source"),//设置CAN触发的触发源
//            new SCPICommandStruct(":TRIGger:CAN:SOURce?","SCPI_Trigger_Can","SourceQ"),//查询CAN触发的触发源
//            new SCPICommandStruct(":TRIGger:CAN:TYPE","SCPI_Trigger_Can","Type"),//设置CAN触发的触发条件
//            new SCPICommandStruct(":TRIGger:CAN:TYPE?","SCPI_Trigger_Can","TypeQ"),//查询CAN触发的触发条件
//            new SCPICommandStruct(":TRIGger:CAN:ID","SCPI_Trigger_Can","Id"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，设置CAN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:CAN:ID?","SCPI_Trigger_Can","IdQ"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，查询CAN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:CAN:DLC","SCPI_Trigger_Can","DLC"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的DLC值
//            new SCPICommandStruct(":TRIGger:CAN:DLC?","SCPI_Trigger_Can","DLCQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的DLC值
//            new SCPICommandStruct(":TRIGger:CAN:DATA","SCPI_Trigger_Can","Data"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的触发数据值
//            new SCPICommandStruct(":TRIGger:CAN:DATA?","SCPI_Trigger_Can","DataQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的触发数据值
//            new SCPICommandStruct(":TRIGger:CAN:LEVel","SCPI_Trigger_Can","Level"),//设置CAN触发时的触发电平
//            new SCPICommandStruct(":TRIGger:CAN:LEVel?","SCPI_Trigger_Can","LevelQ"),//查询CAN触发时的触发电平

    /**
     * 设置CAN触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void Source(SCPIParam param){} // 设置CAN触发的触发源（当前为空实现）

    /**
     * 查询CAN触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void SourceQ(SCPIParam param){} // 查询CAN触发的触发源（当前为空实现）


    /**
     * 设置CAN触发的触发类型。
     * 读取当前ID、DLC、Data，仅更新触发类型，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为新的触发类型值
     */
    public static void Type(SCPIParam param){
        int id=Command.get().getTrigger_can().getId(param.iParam1); // 获取当前CAN触发ID值
        int dlc=Command.get().getTrigger_can().getDlc(param.iParam1); // 获取当前DLC值
        long data=Command.get().getTrigger_can().getData(param.iParam1); // 获取当前触发数据值
        Command.get().getTrigger_can().setType(param.iParam1, param.iParam2, id,dlc,data,true); // 设置新的触发类型，保持其余参数不变，并通知硬件
    }

    /**
     * 查询CAN触发的触发类型。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发类型的SCPI字符串表示
     */
    public static String TypeQ(SCPIParam param){
        int i= Command.get().getTrigger_can().getType(param.iParam1); // 获取当前CAN触发类型
        return ToolsSCPI.getCanTriggerType(i); // 将触发类型枚举值转换为SCPI字符串
    }

    /**
     * 设置CAN触发的触发ID。
     * 读取当前触发类型、DLC、Data，仅更新ID值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为ID的十六进制字符串
     */
    public static void Id(SCPIParam param){
        int type=Command.get().getTrigger_can().getType(param.iParam1); // 获取当前触发类型
        //int id=Command.get().getTrigger_can().getId(param.iParam1);
        int dlc=Command.get().getTrigger_can().getDlc(param.iParam1); // 获取当前DLC值
        long data=Command.get().getTrigger_can().getData(param.iParam1); // 获取当前触发数据值
        long id= SerialsUtils.toDLong(param.sParam1.trim(),16); // 将16位十六进制字符串参数转换为长整型ID值
        Command.get().getTrigger_can().setType(param.iParam1, type, id, dlc,data,true); // 设置新的ID值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询CAN触发的触发ID。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发ID的十六进制字符串
     */
    public static String IdQ(SCPIParam param){
        int i= Command.get().getTrigger_can().getId(param.iParam1); // 获取当前CAN触发ID值
        return Integer.toHexString(i); // 将ID值转换为十六进制字符串返回
    }

    /**
     * 设置CAN触发的DLC（数据长度码）。
     * DLC值以10进制进行传送与保存。读取当前触发类型、ID、Data，仅更新DLC值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为DLC的枚举索引值
     */
    //dlc的值为10进制进行传送与保存
    public static void DLC(SCPIParam param){
        int type=Command.get().getTrigger_can().getType(param.iParam1); // 获取当前触发类型
        int id=Command.get().getTrigger_can().getId(param.iParam1); // 获取当前ID值
        //int dlc=Command.get().getTrigger_can().getDlc(param.iParam1);
        long data=Command.get().getTrigger_can().getData(param.iParam1); // 获取当前触发数据值
        int dlc=ToolsSCPI.getCanDlc(param.iParam2); // 将枚举索引值转换为实际DLC数值
        Command.get().getTrigger_can().setType(param.iParam1, type, id, dlc, data,true); // 设置新的DLC值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询CAN触发的DLC（数据长度码）。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return DLC值的十进制字符串
     */
    public static String DLCQ(SCPIParam param){
        int i=Command.get().getTrigger_can().getDlc(param.iParam1); // 获取当前DLC值
        return String.valueOf(i); // 将DLC值以十进制字符串返回
        //return Integer.toHexString(i);
    }

    /**
     * 设置CAN触发的触发数据。
     * 读取当前触发类型、ID、DLC，仅更新数据值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为数据的十六进制字符串
     */
    public static void Data(SCPIParam param){
        int type=Command.get().getTrigger_can().getType(param.iParam1); // 获取当前触发类型
        int id=Command.get().getTrigger_can().getId(param.iParam1); // 获取当前ID值
        int dlc=Command.get().getTrigger_can().getDlc(param.iParam1); // 获取当前DLC值
        //long data=Command.get().getTrigger_can().getData(param.iParam1);
        long data=SerialsUtils.toDLong(param.sParam1.trim(),16); // 将16位十六进制字符串参数转换为长整型数据值
        Command.get().getTrigger_can().setType(param.iParam1,type, id, dlc, data, true); // 设置新的数据值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询CAN触发的触发数据。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发数据的十六进制字符串
     */
    public static String DataQ(SCPIParam param){
        long i=Command.get().getTrigger_can().getData(param.iParam1); // 获取当前触发数据值
        return Long.toHexString(i); // 将数据值转换为十六进制字符串返回
    }

    /**
     * 设置CAN触发的触发电平。
     * @param param SCPI命令参数，iParam1为通道索引，dParam1为触发电平值
     */
    public static void Level(SCPIParam param){
        Command.get().getTrigger_can().setLevel(param.iParam1, param.dParam1, true); // 设置CAN触发电平，并通知硬件
    }

    /**
     * 查询CAN触发的触发电平。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发电平的字符串表示
     */
    public static String LevelQ(SCPIParam param){
        double d= Command.get().getTrigger_can().getLevel(param.iParam1); // 获取当前触发电平
        return String.valueOf(d); // 将触发电平转换为字符串返回
    }
}
