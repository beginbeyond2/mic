package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类，用于数据类型转换
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串行总线工具类，用于数据格式转换

/**
 * @auother Liwb
 * @description:
 * @data:2022-6-23 14:43
 *
 * +=============================================================================================================+
 * |                                            SCPI_Trigger_429                                                 |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— ARINC 429总线触发模块                                                         |
 * | 核心职责 : 解析并执行ARINC 429总线触发相关的SCPI命令（设置/查询触发源、触发类型、                               |
 * |            标签Label、SDI、数据Data、SSM、高/低触发电平）                                                   |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令读取当前值后仅修改目标字段，                             |
 * |            查询命令读取配置并通过ToolsSCPI/SerialsUtils转换为SCPI响应字符串                                   |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                           |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值→字符串转换）、                                       |
 * |            Tools（十六进制字符串→整数/长整型转换）、SerialsUtils（串行总线数据格式转换）                       |
 * | 使用场景 : 仪器作为ARINC 429总线分析设备时，用户通过SCPI命令配置429协议触发条件                                |
 * +=============================================================================================================+
 */
class SCPI_Trigger_429 {
//            new SCPICommandStruct(":TRIGger:429:SOURce","SCPI_Trigger_429","Source"),
//            new SCPICommandStruct(":TRIGger:429:SOURce?","SCPI_Trigger_429","SourceQ"),
//            new SCPICommandStruct(":TRIGger:429:TYPE","SCPI_Trigger_429","Type"),
//            new SCPICommandStruct(":TRIGger:429:TYPE?","SCPI_Trigger_429","TypeQ"),
//            new SCPICommandStruct(":TRIGger:429:WORD","SCPI_Trigger_429","Word"),
//            new SCPICommandStruct(":TRIGger:429:WORD?","SCPI_Trigger_429","WordQ"),
//            new SCPICommandStruct(":TRIGger:429:LABEl","SCPI_Trigger_429","Label"),
//            new SCPICommandStruct(":TRIGger:429:LABEl?","SCPI_Trigger_429","LabelQ"),
//            new SCPICommandStruct(":TRIGger:429:SDI","SCPI_Trigger_429","Sdi"),
//            new SCPICommandStruct(":TRIGger:429:SDI?","SCPI_Trigger_429","SdiQ"),
//            new SCPICommandStruct(":TRIGger:429:DATA","SCPI_Trigger_429","data"),
//            new SCPICommandStruct(":TRIGger:429:DATA?","SCPI_Trigger_429","dataQ"),
//            new SCPICommandStruct(":TRIGger:429:SSM","SCPI_Trigger_429","Ssm"),
//            new SCPICommandStruct(":TRIGger:429:SSM?","SCPI_Trigger_429","SsmQ"),
//            new SCPICommandStruct(":TRIGger:429:LEVEl","SCPI_Trigger_429","Level"),
//            new SCPICommandStruct(":TRIGger:429:LEVEl?","SCPI_Trigger_429","LevelQ"),

    /**
     * 设置ARINC 429触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void Source(SCPIParam param){} // 设置429触发的触发源（当前为空实现）

    /**
     * 查询ARINC 429触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void SourceQ(SCPIParam param){} // 查询429触发的触发源（当前为空实现）

    /**
     * 设置ARINC 429触发的触发类型。
     * 读取当前Label、SDI、Data、SSM，仅更新触发类型，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为新的触发类型值
     */
    public static void Type(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1); // 获取当前429触发类型
        int label=Command.get().getTrigger_m429().getLabel(param.iParam1); // 获取当前标签值
        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1); // 获取当前SDI值
        long data=Command.get().getTrigger_m429().getData(param.iParam1); // 获取当前数据值
        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1); // 获取当前SSM值
        Command.get().getTrigger_m429().setType(param.iParam1, param.iParam2, label,sdi,data,ssm,true); // 设置新的触发类型，保持其余参数不变，并通知硬件
    }

    /**
     * 查询ARINC 429触发的触发类型。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发类型的SCPI字符串表示
     */
    public static String TypeQ(SCPIParam param){
        int i=Command.get().getTrigger_m429().getType(param.iParam1); // 获取当前429触发类型
        return ToolsSCPI.getArinc429TriggerType(i); // 将触发类型枚举值转换为SCPI字符串
    }

    /**
     * 设置ARINC 429触发的完整字（Word）。
     * 当前为空实现，待后续完善。
     * @param param SCPI命令参数
     */
    public static void Word(SCPIParam param){
//        int type=Command.get().getTrigger_m429().getType(param.iParam1);
//        int label=Command.get().getTrigger_m429().getLabel(param.iParam1);
//        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1);
//        int data=Command.get().getTrigger_m429().getData(param.iParam1);
//        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1);
//        Command.get().getTrigger_m429().setType(param.iParam1, type, label,sdi,data,ssm,true);
    } // 设置429触发的完整字（当前为空实现）

    /**
     * 查询ARINC 429触发的完整字（Word）。
     * 当前为空实现，待后续完善。
     * @param param SCPI命令参数
     */
    public static void WordQ(SCPIParam param){} // 查询429触发的完整字（当前为空实现）

    /**
     * 设置ARINC 429触发的标签（Label）。
     * 读取当前触发类型、SDI、Data、SSM，仅更新标签值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为标签字符串（8位二进制）
     */
    public static void Label(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1); // 获取当前触发类型
        //int label=Command.get().getTrigger_m429().getLabel(param.iParam1);
//        int label= Tools.HexStringToInt(param.sParam1);
        int label= (int)SerialsUtils.toDLong(param.sParam1.trim(),8); // 将8位二进制字符串参数转换为整数标签值
        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1); // 获取当前SDI值
        long data=Command.get().getTrigger_m429().getData(param.iParam1); // 获取当前数据值
        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1); // 获取当前SSM值
        Command.get().getTrigger_m429().setType(param.iParam1, type, label, sdi,data,ssm,true); // 设置新的标签值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询ARINC 429触发的标签（Label）。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 标签值的8位二进制字符串表示
     */
    public static String LabelQ(SCPIParam param){
        int i=Command.get().getTrigger_m429().getLabel(param.iParam1); // 获取当前标签值
        return SerialsUtils.getHexBinFromInt(i,3,8); // 将标签值格式化为8位二进制字符串（3组显示）
    }

    /**
     * 设置ARINC 429触发的SDI（源/目标标识）。
     * 读取当前触发类型、Label、Data、SSM，仅更新SDI值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为SDI字符串（2位二进制）
     */
    public static void Sdi(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1); // 获取当前触发类型
        int label=Command.get().getTrigger_m429().getLabel(param.iParam1); // 获取当前标签值
        //int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1);
        //int sdi=Tools.HexStringToInt(param.sParam1);
        int sdi=(int)SerialsUtils.toDLong(param.sParam1.trim(),2); // 将2位二进制字符串参数转换为整数SDI值
        long data=Command.get().getTrigger_m429().getData(param.iParam1); // 获取当前数据值
        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1); // 获取当前SSM值
        Command.get().getTrigger_m429().setType(param.iParam1, type, label, sdi, data,ssm,true); // 设置新的SDI值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询ARINC 429触发的SDI（源/目标标识）。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return SDI值的2位二进制字符串表示
     */
    public static String SdiQ(SCPIParam param){
        int i=Command.get().getTrigger_m429().getSdi(param.iParam1); // 获取当前SDI值
        return SerialsUtils.getHexBinFromInt(i,2,2); // 将SDI值格式化为2位二进制字符串
    }

    /**
     * 设置ARINC 429触发的数据（Data）。
     * 读取当前触发类型、Label、SDI、SSM，仅更新数据值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为数据的十六进制字符串
     */
    public static void data(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1); // 获取当前触发类型
        int label=Command.get().getTrigger_m429().getLabel(param.iParam1); // 获取当前标签值
        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1); // 获取当前SDI值
        //int data=Command.get().getTrigger_m429().getData(param.iParam1);
        long data=Tools.HexStringToLong(param.sParam1); // 将十六进制字符串参数转换为长整型数据值
        int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1); // 获取当前SSM值
        Command.get().getTrigger_m429().setType(param.iParam1, type, label,sdi,data, ssm,true); // 设置新的数据值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询ARINC 429触发的数据（Data）。
     * 根据总线格式（BNR/BCD）决定数据的显示位数。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 数据值的二进制字符串表示（位数由格式决定）
     */
    public static String dataQ(SCPIParam param){
        long i=Command.get().getTrigger_m429().getData(param.iParam1); // 获取当前数据值
        int f= Command.get().getBus_429().FormatQ(param.iParam1); // 获取当前429总线的数据格式
        if (f==2) { // 如果格式为BCD（格式代码2）
            return SerialsUtils.getHexBinFromLong(i, 5, 16); // 以5组16位二进制格式返回数据
        }else{ // 否则格式为BNR或其他
            return SerialsUtils.getHexBinFromLong(i, 6, 16); // 以6组16位二进制格式返回数据
        }
    }

    /**
     * 设置ARINC 429触发的SSM（符号/状态矩阵）。
     * 读取当前触发类型、Label、SDI、Data，仅更新SSM值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为SSM字符串（2位二进制）
     */
    public static void Ssm(SCPIParam param){
        int type=Command.get().getTrigger_m429().getType(param.iParam1); // 获取当前触发类型
        int label=Command.get().getTrigger_m429().getLabel(param.iParam1); // 获取当前标签值
        int sdi=Command.get().getTrigger_m429().getSdi(param.iParam1); // 获取当前SDI值
        long data=Command.get().getTrigger_m429().getData(param.iParam1); // 获取当前数据值
        //int ssm=Command.get().getTrigger_m429().getSsm(param.iParam1);
        //int ssm=Tools.HexStringToInt(param.sParam1);
         int ssm=(int)SerialsUtils.toDLong(param.sParam1.trim(),2); // 将2位二进制字符串参数转换为整数SSM值
        Command.get().getTrigger_m429().setType(param.iParam1, type, label,sdi,data, ssm, true); // 设置新的SSM值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询ARINC 429触发的SSM（符号/状态矩阵）。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return SSM值的2位二进制字符串表示
     */
    public static String SsmQ(SCPIParam param){
        int i=Command.get().getTrigger_m429().getSsm(param.iParam1); // 获取当前SSM值
        return SerialsUtils.getHexBinFromInt(i,2,2); // 将SSM值格式化为2位二进制字符串
    }

    /**
     * 设置ARINC 429触发的高触发电平。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为电平类型，dParam1为电平值
     */
    public static void LevelHigh(SCPIParam param){
        Command.get().getTrigger_m429().setLevelHigh(param.iParam1,param.iParam2, param.dParam1, true); // 设置429高触发电平，并通知硬件
    }

    /**
     * 查询ARINC 429触发的高触发电平。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 高触发电平的字符串表示
     */
    public static String LevelHighQ(SCPIParam param){
        double d=Command.get().getTrigger_m429().getLevelHigh(param.iParam1); // 获取当前高触发电平
        return String.valueOf(d); // 将高触发电平转换为字符串返回
    }

    /**
     * 设置ARINC 429触发的低触发电平。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为电平类型，dParam1为电平值
     */
    public static void LevelLow(SCPIParam param){
        Command.get().getTrigger_m429().setLevelLow(param.iParam1,param.iParam2, param.dParam1, true); // 设置429低触发电平，并通知硬件
    }

    /**
     * 查询ARINC 429触发的低触发电平。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 低触发电平的字符串表示
     */
    public static String LevelLowQ(SCPIParam param){
        double d=Command.get().getTrigger_m429().getLevelLow(param.iParam1); // 获取当前低触发电平
        return String.valueOf(d); // 将低触发电平转换为字符串返回
    }

}
