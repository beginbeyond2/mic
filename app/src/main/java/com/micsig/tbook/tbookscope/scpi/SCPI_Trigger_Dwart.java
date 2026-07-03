package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象

/**
 * Created by liwb on 2018/1/12.
 *
 * +=============================================================================================================+
 * |                                          SCPI_Trigger_Dwart                                                 |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— 矮脉宽触发（Dwart）模块                                                       |
 * | 核心职责 : 解析并执行矮脉宽触发相关的SCPI命令（设置/查询触发源、脉冲极性、                                     |
 * |            脉宽限制条件、时间上限/下限/区间、高电平/低电平）                                                 |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令委托Command中间件执行，                               |
 * |            查询命令读取配置并通过ToolsSCPI转换为SCPI响应字符串                                               |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                         |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值/数值→字符串转换）                                     |
 * | 使用场景 : 仪器需要检测宽度低于设定阈值的矮脉冲信号时，用户通过SCPI命令配置矮脉宽触发条件                      |
 * +=============================================================================================================+
 */

public class SCPI_Trigger_Dwart {

//     new SCPICommandStruct(":TRIGger:DWARt:SOURce","SCPI_Trigger_Dwart","Source"),//设置矮脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:DWARt:SOURce?","SCPI_Trigger_Dwart","SourceQ"),//查询矮脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:DWARt:POLarity","SCPI_Trigger_Dwart","Polarity"),//设置矮脉宽触发的脉冲极性
//            new SCPICommandStruct(":TRIGger:DWARt:POLarity?","SCPI_Trigger_Dwart","PolarityQ"),//查询矮脉宽触发的脉冲极性
//            new SCPICommandStruct(":TRIGger:DWARt:CONDition","SCPI_Trigger_Dwart","Condition"),//设置矮脉宽触发的脉宽限制条件
//            new SCPICommandStruct(":TRIGger:DWARt:CONDition?","SCPI_Trigger_Dwart","ConditionQ"),//查询矮脉宽触发的脉宽限制条件
//            new SCPICommandStruct(":TRIGger:DWARt:HTIMe","SCPI_Trigger_Dwart","HTime"),//设置矮脉宽触发时的时间上限
//            new SCPICommandStruct(":TRIGger:DWARt:HTIMe?","SCPI_Trigger_Dwart","HTimeQ"),//查询矮脉宽触发时的时间上限
//            new SCPICommandStruct(":TRIGger:DWARt:LTIMe","SCPI_Trigger_Dwart","LTime"),//设置矮脉宽触发时的时间下限
//            new SCPICommandStruct(":TRIGger:DWARt:LTIMe?","SCPI_Trigger_Dwart","LTimeQ"),//查询矮脉宽触发时的时间下限
//            new SCPICommandStruct(":TRIGger:DWARt:BTIMe","SCPI_Trigger_Dwart","BTime"),//设置矮脉宽触发时的时间区间
//            new SCPICommandStruct(":TRIGger:DWARt:BTIMe?","SCPI_Trigger_Dwart","BTimeQ"),//查询矮脉宽触发时的时间上限或下限
//            new SCPICommandStruct(":TRIGger:DWARt:HLEVel","SCPI_Trigger_Dwart","HLevel"),//设置矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:DWARt:PLUS:HLEVel","SCPI_Trigger_Dwart","Plus_HLevel"),//设置矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:DWARt:HLEVel?","SCPI_Trigger_Dwart","HLevelQ"),//查询矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:DWARt:LLEVel","SCPI_Trigger_Dwart","LLevel"),//设置矮脉宽触发时的低电平
//            new SCPICommandStruct(":TRIGger:DWARt:PLUS:LLEVel","SCPI_Trigger_Dwart","Plus_LLevel"),//设置矮脉宽触发时的低电平
//            new SCPICommandStruct(":TRIGger:DWARt:LLEVel?","SCPI_Trigger_Dwart","LLevelQ"),//查询矮脉宽触发时的低电平

    /**
     * 设置矮脉宽触发的触发源。
     * @param param SCPI命令参数，iParam1为触发源通道索引
     */
    public static void Source(SCPIParam param) {
        Command.get().getTrigger_dwart().Source(param.iParam1, true); // 设置矮脉宽触发源，并通知硬件
    }

    /**
     * 查询矮脉宽触发的触发源。
     * @param param SCPI命令参数
     * @return 触发源通道的SCPI字符串表示
     */
    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getTrigger_dwart().SourceQ(); // 获取当前触发源通道索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为SCPI通道字符串
    }

    /**
     * 设置矮脉宽触发的脉冲极性。
     * @param param SCPI命令参数，iParam1为极性值（正/负）
     */
    public static void Polarity(SCPIParam param) {
        Command.get().getTrigger_dwart().Polarity(param.iParam1, true); // 设置矮脉宽触发极性，并通知硬件
    }

    /**
     * 查询矮脉宽触发的脉冲极性。
     * @param param SCPI命令参数
     * @return 极性的SCPI字符串表示
     */
    public static String PolarityQ(SCPIParam param) {
        int i=Command.get().getTrigger_dwart().PolarityQ(); // 获取当前脉冲极性
        return ToolsSCPI.getTriggerPulsePolarity(i); // 将极性枚举值转换为SCPI字符串
    }

    /**
     * 设置矮脉宽触发的脉宽限制条件。
     * @param param SCPI命令参数，iParam1为条件值
     */
    public static void Condition(SCPIParam param) {
        Command.get().getTrigger_dwart().Condition(param.iParam1, true); // 设置矮脉宽触发条件，并通知硬件
    }

    /**
     * 查询矮脉宽触发的脉宽限制条件。
     * @param param SCPI命令参数
     * @return 条件的SCPI字符串表示
     */
    public static String ConditionQ(SCPIParam param) {
        int i= Command.get().getTrigger_dwart().ConditionQ(); // 获取当前脉宽限制条件
        return ToolsSCPI.getTriggerDwartCondition(i); // 将条件枚举值转换为SCPI字符串
    }

    /**
     * 设置矮脉宽触发时的时间上限。
     * @param param SCPI命令参数，dParam1为时间上限值（秒）
     */
    public static void HTime(SCPIParam param) {
        Command.get().getTrigger_dwart().HTime(param.dParam1, true); // 设置时间上限，并通知硬件
    }

    /**
     * 查询矮脉宽触发时的时间上限。
     * @param param SCPI命令参数
     * @return 时间上限的字符串表示
     */
    public static String HTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().HTimeQ(); // 获取当前时间上限
        return ToolsSCPI.getDouble(d); // 将时间上限格式化为字符串返回
    }

    /**
     * 设置矮脉宽触发时的时间下限。
     * @param param SCPI命令参数，dParam1为时间下限值（秒）
     */
    public static void LTime(SCPIParam param) {
        Command.get().getTrigger_dwart().LTime(param.dParam1, true); // 设置时间下限，并通知硬件
    }

    /**
     * 查询矮脉宽触发时的时间下限。
     * @param param SCPI命令参数
     * @return 时间下限的字符串表示
     */
    public static String LTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().LTimeQ(); // 获取当前时间下限
        return ToolsSCPI.getDouble(d); // 将时间下限格式化为字符串返回
    }

    /**
     * 设置矮脉宽触发时的时间区间（同时设置上限和下限）。
     * @param param SCPI命令参数，dParam1为时间下限，dParam2为时间上限
     */
    public static void BTime(SCPIParam param) {
        Command.get().getTrigger_dwart().BTime(param.dParam1, param.dParam2, true); // 设置时间区间（下限+上限），并通知硬件
    }

    /**
     * 查询矮脉宽触发时的时间区间。
     * @param param SCPI命令参数，iParam1指定查询上限或下限
     * @return 时间值的字符串表示
     */
    public static String BTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().BTimeQ(param.iParam1); // 获取时间区间的上限或下限
        return ToolsSCPI.getDouble(d); // 将时间值格式化为字符串返回
    }

    /**
     * 设置矮脉宽触发时的高电平。
     * @param param SCPI命令参数，dParam1为高电平值
     */
    public static void HLevel(SCPIParam param) {
        Command.get().getTrigger_dwart().HLevel(param.dParam1, true); // 设置高电平，并通知硬件
    }

    /**
     * 设置矮脉宽触发时的高电平（Plus增量调整）。
     * @param param SCPI命令参数，iParam1为增量值
     */
    public static void Plus_HLevel(SCPIParam param) {
        Command.get().getTrigger_dwart().Plus_HLevel(param.iParam1, true); // 以增量方式设置高电平，并通知硬件
    }

    /**
     * 查询矮脉宽触发时的高电平。
     * @param param SCPI命令参数
     * @return 高电平的字符串表示
     */
    public static String HLevelQ(SCPIParam param) {
        double d= Command.get().getTrigger_dwart().HLevelQ(); // 获取当前高电平
        return ToolsSCPI.getDouble(d); // 将高电平格式化为字符串返回
    }

    /**
     * 设置矮脉宽触发时的低电平。
     * @param param SCPI命令参数，dParam1为低电平值
     */
    public static void LLevel(SCPIParam param) {
        Command.get().getTrigger_dwart().LLevel(param.dParam1, true); // 设置低电平，并通知硬件
    }

    /**
     * 设置矮脉宽触发时的低电平（Plus增量调整）。
     * @param param SCPI命令参数，iParam1为增量值
     */
    public static void Plus_LLevel(SCPIParam param) {
        Command.get().getTrigger_dwart().Plus_LLevel(param.iParam1, true); // 以增量方式设置低电平，并通知硬件
    }

    /**
     * 查询矮脉宽触发时的低电平。
     * @param param SCPI命令参数
     * @return 低电平的字符串表示
     */
    public static String LLevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_dwart().LLevelQ(); // 获取当前低电平
        return ToolsSCPI.getDouble(d); // 将低电平格式化为字符串返回
    }

}
