package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象

/**
 * Created by liwb on 2018/1/12.
 *
 * +=============================================================================================================+
 * |                                          SCPI_Trigger_Logic                                                 |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— 逻辑触发模块                                                                  |
 * | 核心职责 : 解析并执行逻辑触发相关的SCPI命令（设置/查询通道逻辑状态、比较函数、                                 |
 * |            触发条件、时间上限/下限/区间/单值、各通道触发电平）                                               |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令委托Command中间件执行，                               |
 * |            查询命令读取配置并通过ToolsSCPI转换为SCPI响应字符串                                               |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                         |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值/数值→字符串转换）                                     |
 * | 使用场景 : 仪器需要根据多通道逻辑组合条件触发采集时，用户通过SCPI命令配置逻辑触发条件                          |
 * +=============================================================================================================+
 */

public class SCPI_Trigger_Logic {
//     new SCPICommandStruct(":TRIGger:LOGic:STATus","SCPI_Trigger_Logic","Status"),//设置逻辑触发中通道的逻辑状态
//            new SCPICommandStruct(":TRIGger:LOGic:STATus?","SCPI_Trigger_Logic","StatusQ"),//查询逻辑触发中通道的逻辑状态
//            new SCPICommandStruct(":TRIGger:LOGic:FUNCtion","SCPI_Trigger_Logic","Function"),//设置逻辑触发的比较函数
//            new SCPICommandStruct(":TRIGger:LOGic:FUNCtion?","SCPI_Trigger_Logic","FunctionQ"),//查询逻辑触发的比较函数
//            new SCPICommandStruct(":TRIGger:LOGic:CONDition","SCPI_Trigger_Logic","Condition"),//设置逻辑触发条件
//            new SCPICommandStruct(":TRIGger:LOGic:CONDition?","SCPI_Trigger_Logic","ConditionQ"),//查询逻辑触发条件
//            new SCPICommandStruct(":TRIGger:LOGic:TIME","SCPI_Trigger_Logic","Time"),//设置触发逻辑时间
//            new SCPICommandStruct(":TRIGger:LOGic:TIME?","SCPI_Trigger_Logic","TimeQ"),//查询触发逻辑时间
//            new SCPICommandStruct(":TRIGger:LOGic:LEVel","SCPI_Trigger_Logic","Level"),//设置逻辑触发时的各通道触发电平
//            new SCPICommandStruct(":TRIGger:LOGic:PLUS:LEVel","SCPI_Trigger_Logic","Plus_Level"),//设置逻辑触发时的各通道触发电平
//            new SCPICommandStruct(":TRIGger:LOGic:LEVel?","SCPI_Trigger_Logic","LevelQ"),//查询逻辑触发时的各通道触发电平

    /**
     * 设置逻辑触发中通道的逻辑状态（高/低/忽略）。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为逻辑状态值
     */
    public static void Status(SCPIParam param) {
        Command.get().getTrigger_logic().Status(param.iParam1, param.iParam2, true); // 设置指定通道的逻辑状态，并通知硬件
    }

    /**
     * 查询逻辑触发中通道的逻辑状态。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 逻辑状态的SCPI字符串表示
     */
    public static String StatusQ(SCPIParam param) {
        int i= Command.get().getTrigger_logic().StatusQ(param.iParam1); // 获取指定通道的当前逻辑状态
        return ToolsSCPI.getTriggerLogicStatus(i); // 将逻辑状态枚举值转换为SCPI字符串
    }

    /**
     * 设置逻辑触发的比较函数（与/或/与非/或非）。
     * @param param SCPI命令参数，iParam1为比较函数枚举值
     */
    public static void Function(SCPIParam param) {
        Command.get().getTrigger_logic().Function(param.iParam1, true); // 设置比较函数，并通知硬件
    }

    /**
     * 查询逻辑触发的比较函数。
     * @param param SCPI命令参数
     * @return 比较函数的SCPI字符串表示
     */
    public static String FunctionQ(SCPIParam param) {
        int i=Command.get().getTrigger_logic().FunctionQ(); // 获取当前比较函数
        return ToolsSCPI.getTriggerLogicFunction(i); // 将比较函数枚举值转换为SCPI字符串
    }

    /**
     * 设置逻辑触发条件（时间限定类型）。
     * @param param SCPI命令参数，iParam1为触发条件枚举值
     */
    public static void Condition(SCPIParam param) {
        Command.get().getTrigger_logic().Condition(param.iParam1, true); // 设置触发条件，并通知硬件
    }

    /**
     * 查询逻辑触发条件。
     * @param param SCPI命令参数
     * @return 触发条件的SCPI字符串表示
     */
    public static String ConditionQ(SCPIParam param) {
        int i= Command.get().getTrigger_logic().ConditionQ(); // 获取当前触发条件
        return ToolsSCPI.getTriggerLogicCondition(i); // 将触发条件枚举值转换为SCPI字符串
    }

    /**
     * 设置逻辑触发的时间上限。
     * @param param SCPI命令参数，dParam1为时间上限值（秒）
     */
    public static void HTime(SCPIParam param) {
        Command.get().getTrigger_logic().HTime(param.dParam1, true); // 设置时间上限，并通知硬件
    }

    /**
     * 查询逻辑触发的时间上限。
     * @param param SCPI命令参数
     * @return 时间上限的字符串表示
     */
    public static String HTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().HTimeQ(); // 获取当前时间上限
        return ToolsSCPI.getDouble(d); // 将时间上限格式化为字符串返回
    }

    /**
     * 设置逻辑触发的时间下限。
     * @param param SCPI命令参数，dParam1为时间下限值（秒）
     */
    public static void LTime(SCPIParam param) {
        Command.get().getTrigger_logic().LTime(param.dParam1, true); // 设置时间下限，并通知硬件
    }

    /**
     * 查询逻辑触发的时间下限。
     * @param param SCPI命令参数
     * @return 时间下限的字符串表示
     */
    public static String LTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().LTimeQ(); // 获取当前时间下限
        return ToolsSCPI.getDouble(d); // 将时间下限格式化为字符串返回
    }

    /**
     * 设置逻辑触发的时间区间（同时设置上限和下限）。
     * @param param SCPI命令参数，dParam1为时间下限，dParam2为时间上限
     */
    public static void BTime(SCPIParam param) {
        Command.get().getTrigger_logic().BTime(param.dParam1,param.dParam2, true); // 设置时间区间（下限+上限），并通知硬件
    }

    /**
     * 查询逻辑触发的时间区间。
     * @param param SCPI命令参数，iParam1指定查询上限或下限
     * @return 时间值的字符串表示
     */
    public static String BTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().BTimeQ(param.iParam1); // 获取时间区间的上限或下限
        return ToolsSCPI.getDouble(d); // 将时间值格式化为字符串返回
    }

    /**
     * 设置逻辑触发的单时间值。
     * @param param SCPI命令参数，dParam1为时间值（秒）
     */
    public static void Time(SCPIParam param) {
        Command.get().getTrigger_logic().Time(param.dParam1,  true); // 设置触发时间值，并通知硬件
    }

    /**
     * 查询逻辑触发的单时间值。
     * @param param SCPI命令参数
     * @return 时间值的字符串表示
     */
    public static String TimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().TimeQ(); // 获取当前触发时间值
        return ToolsSCPI.getDouble(d); // 将时间值格式化为字符串返回
    }

    /**
     * 设置逻辑触发时指定通道的触发电平。
     * @param param SCPI命令参数，iParam1为通道索引，dParam1为触发电平值
     */
    public static void Level(SCPIParam param) {
        Command.get().getTrigger_logic().Level(param.iParam1, param.dParam1, true); // 设置指定通道触发电平，并通知硬件
    }

    /**
     * 设置逻辑触发时指定通道的触发电平（Plus增量调整）。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为增量值
     */
    public static void Plus_Level(SCPIParam param) {
        Command.get().getTrigger_logic().Plus_Level(param.iParam1, param.iParam2, true); // 以增量方式设置指定通道触发电平，并通知硬件
    }

    /**
     * 查询逻辑触发时指定通道的触发电平。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发电平的字符串表示
     */
    public static String LevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_logic().LevelQ(param.iParam1); // 获取指定通道的当前触发电平
        return ToolsSCPI.getDouble(d); // 将触发电平格式化为字符串返回
    }

}
