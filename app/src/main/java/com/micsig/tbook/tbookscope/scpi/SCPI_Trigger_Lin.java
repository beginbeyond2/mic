package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类，用于数据类型转换
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串行总线工具类，用于数据格式转换

/**
 * Created by liwb on 2018/1/12.
 *
 * +=============================================================================================================+
 * |                                           SCPI_Trigger_Lin                                                  |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— LIN总线触发模块                                                               |
 * | 核心职责 : 解析并执行LIN总线触发相关的SCPI命令（设置/查询触发源、触发类型、                                   |
 * |            触发ID、触发数据、触发电平）                                                                    |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令读取当前值后仅修改目标字段，                           |
 * |            查询命令读取配置并转换为SCPI响应字符串                                                           |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                         |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值→字符串转换）、                                       |
 * |            Tools（十六进制字符串→整数转换）、SerialsUtils（串行总线数据格式转换）                             |
 * | 使用场景 : 仪器作为LIN总线分析设备时，用户通过SCPI命令配置LIN协议触发条件                                    |
 * +=============================================================================================================+
 */

public class SCPI_Trigger_Lin {
//      new SCPICommandStruct(":TRIGger:LIN:SOURce","SCPI_Trigger_Lin","Source"),//设置LIN触发的触发源
//            new SCPICommandStruct(":TRIGger:LIN:SOURce?","SCPI_Trigger_Lin","SourceQ"),//查询LIN触发的触发源
//            new SCPICommandStruct(":TRIGger:LIN:TYPE","SCPI_Trigger_Lin","Type"),//设置LIN触发的触发条件
//            new SCPICommandStruct(":TRIGger:LIN:TYPE?","SCPI_Trigger_Lin","TypeQ"),//查询LIN触发的触发条件
//            new SCPICommandStruct(":TRIGger:LIN:ID","SCPI_Trigger_Lin","Id"),//当LIN总线触发条件为FID或IDATa时，设置LIN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:LIN:ID?","SCPI_Trigger_Lin","IdQ"),//当LIN总线触发条件为FID或IDATa时，查询LIN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:LIN:DATA","SCPI_Trigger_Lin","Data"),//当LIN总线触发条件为IDATa时，设置LIN触发的触发数据
//            new SCPICommandStruct(":TRIGger:LIN:DATA?","SCPI_Trigger_Lin","DataQ"),//当LIN总线触发条件为IDATa时，查询LIN触发的触发数据
//            new SCPICommandStruct(":TRIGger:LIN:LEVel","SCPI_Trigger_Lin","Level"),//设置LIN触发时的触发电平
//            new SCPICommandStruct(":TRIGger:LIN:LEVel?","SCPI_Trigger_Lin","LevelQ"),//查询LIN触发时的触发电平

    /**
     * 设置LIN触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void Source(SCPIParam param){} // 设置LIN触发的触发源（当前为空实现）

    /**
     * 查询LIN触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void SourceQ(SCPIParam param){} // 查询LIN触发的触发源（当前为空实现）

    /**
     * 设置LIN触发的触发类型。
     * 读取当前ID和Data，仅更新触发类型，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为新的触发类型值
     */
    public static void Type(SCPIParam param){
        int id=Command.get().getTrigger_lin().getId(param.iParam1); // 获取当前LIN触发ID值
        long data=Command.get().getTrigger_lin().getData(param.iParam1); // 获取当前触发数据值
        Command.get().getTrigger_lin().setType(param.iParam1, param.iParam2, id,data,true); // 设置新的触发类型，保持其余参数不变，并通知硬件
    }

    /**
     * 查询LIN触发的触发类型。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发类型的SCPI字符串表示
     */
    public static String TypeQ(SCPIParam param){
        int i= Command.get().getTrigger_lin().getType(param.iParam1); // 获取当前LIN触发类型
        return ToolsSCPI.getLinTriggerType(i); // 将触发类型枚举值转换为SCPI字符串
    }

    /**
     * 设置LIN触发的触发ID。
     * 从总线配置获取当前触发类型，读取当前Data，仅更新ID值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为ID的十六进制字符串
     */
    public static void Id(SCPIParam param){
//        int type=Command.get().getTrigger_lin().getType(param.iParam1);
        int type=Command.get().getBus_lin().LinTypeQ(param.iParam1); // 从LIN总线配置获取当前触发类型
        long data=Command.get().getTrigger_lin().getData(param.iParam1); // 获取当前触发数据值
        int id= Tools.HexStringToInt(param.sParam1); // 将十六进制字符串参数转换为整数ID值
        Command.get().getTrigger_lin().setType(param.iParam1, type, id, data,true); // 设置新的ID值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询LIN触发的触发ID。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发ID的十六进制字符串
     */
    public static String IdQ(SCPIParam param){
        int id= Command.get().getTrigger_lin().getId(param.iParam1); // 获取当前LIN触发ID值
        return Integer.toHexString(id); // 将ID值转换为十六进制字符串返回
    }

    /**
     * 设置LIN触发的触发数据。
     * 根据ID值确定帧长度（2/4/8字节），截取数据字符串有效部分并转换为数值。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为数据的十六进制字符串
     */
    public static void Data(SCPIParam param){
//        int type=Command.get().getTrigger_lin().getType(param.iParam1);
        int type=Command.get().getBus_lin().LinTypeQ(param.iParam1); // 从LIN总线配置获取当前触发类型
        int id=Command.get().getTrigger_lin().getId(param.iParam1); // 获取当前ID值
        int frameLen=2*2; // 默认帧长度为4个十六进制字符（2字节）
        if (id>=0 && id<32){ // ID在0-31范围：2字节数据帧
            frameLen=2*2; // 帧长度为4个十六进制字符
        }else if (id>=32 && id<48){ // ID在32-47范围：4字节数据帧
            frameLen=4*2; // 帧长度为8个十六进制字符
        }else if (id>=48 && id<64){ // ID在48-63范围：8字节数据帧
            frameLen=8*2; // 帧长度为16个十六进制字符
        }
        String frame= param.sParam1.trim(); // 去除数据字符串首尾空白
        int start=frame.length()-frameLen<0?0:frame.length()-frameLen; // 计算截取起始位置（取最后frameLen个字符）
        frame=frame.substring(start); // 截取有效数据部分
        long data= SerialsUtils.toDLong(frame,16); // 将截取后的十六进制字符串转换为长整型数据值
        Command.get().getTrigger_lin().setType(param.iParam1, type,id, data, true); // 设置新的数据值，保持其余参数不变，并通知硬件

    }

    /**
     * 查询LIN触发的触发数据。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发数据的十六进制字符串
     */
    public static String DataQ(SCPIParam param){
        long data=Command.get().getTrigger_lin().getData(param.iParam1); // 获取当前触发数据值
        return Long.toHexString(data); // 将数据值转换为十六进制字符串返回
    }

    /**
     * 设置LIN触发的触发电平。
     * @param param SCPI命令参数，iParam1为通道索引，dParam1为触发电平值
     */
    public static void Level(SCPIParam param){
        Command.get().getTrigger_lin().setLevel(param.iParam1, param.dParam1, true); // 设置LIN触发电平，并通知硬件
    }

    /**
     * 查询LIN触发的触发电平。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发电平的字符串表示
     */
    public static String LevelQ(SCPIParam param){
        double d=Command.get().getTrigger_lin().getLevel(param.iParam1); // 获取当前触发电平
        return String.valueOf(d); // 将触发电平转换为字符串返回
    }

}
