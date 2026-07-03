package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象

/**
 * Created by liwb on 2018/1/12.
 *
 * +=============================================================================================================+
 * |                                          SCPI_Trigger_Nedge                                                 |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— 第N边沿触发模块                                                               |
 * | 核心职责 : 解析并执行第N边沿触发相关的SCPI命令（设置/查询触发源、边沿类型、                                    |
 * |            空闲时间、边沿计数值N、触发电平）                                                                |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令委托Command中间件执行，                               |
 * |            查询命令读取配置并通过ToolsSCPI转换为SCPI响应字符串                                               |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                         |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值/数值→字符串转换）                                     |
 * | 使用场景 : 仪器需要在第N个边沿而非第一个边沿触发采集时，用户通过SCPI命令配置第N边沿触发条件                    |
 * +=============================================================================================================+
 */

public class SCPI_Trigger_Nedge {
//     new SCPICommandStruct(":TRIGger:NEDGe:SOURce","SCPI_Trigger_Nedge","Source"),//设置第N边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:NEDGe:SOURce?","SCPI_Trigger_Nedge","SourceQ"),//查询第N边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe","SCPI_Trigger_Nedge","Slope"),//设置第N边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe?","SCPI_Trigger_Nedge","SlopeQ"),//查询第N边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:NEDGe:IDLE","SCPI_Trigger_Nedge","Idle"),//设置第N边沿触发中开始边沿计数之前的空闲时间
//            new SCPICommandStruct(":TRIGger:NEDGe:IDLE?","SCPI_Trigger_Nedge","IdleQ"),//查询第N边沿触发中开始边沿计数之前的空闲时间
//            new SCPICommandStruct(":TRIGger:NEDGe:EDGE","SCPI_Trigger_Nedge","Edge"),//设置第N边沿触发的N的数值
//            new SCPICommandStruct(":TRIGger:NEDGe:EDGE?","SCPI_Trigger_Nedge","EdgeQ"),//查询第N边沿触发的N的数值
//            new SCPICommandStruct(":TRIGger:NEDGe:LEVel","SCPI_Trigger_Nedge","Level"),//设置第N边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:NEDGe:PLUS:LEVel","SCPI_Trigger_Nedge","Plus_Level"),//设置第N边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:NEDGe:LEVel?","SCPI_Trigger_Nedge","LevelQ"),//查询第N边沿触发时的触发电平

    /**
     * 设置第N边沿触发的触发源。
     * @param param SCPI命令参数，iParam1为触发源通道索引
     */
    public static void Source(SCPIParam param) {
        Command.get().getTrigger_nedge().Source(param.iParam1, true); // 设置第N边沿触发源，并通知硬件
    }

    /**
     * 查询第N边沿触发的触发源。
     * @param param SCPI命令参数
     * @return 触发源通道的SCPI字符串表示
     */
    public static String SourceQ(SCPIParam param) {
        int i=Command.get().getTrigger_nedge().SourceQ(); // 获取当前触发源通道索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为SCPI通道字符串
    }

    /**
     * 设置第N边沿触发的边沿类型（上升沿/下降沿）。
     * @param param SCPI命令参数，iParam1为边沿类型枚举值
     */
    public static void Slope(SCPIParam param) {
        Command.get().getTrigger_nedge().Slope(param.iParam1, true); // 设置边沿类型，并通知硬件
    }

    /**
     * 查询第N边沿触发的边沿类型。
     * @param param SCPI命令参数
     * @return 边沿类型的SCPI字符串表示
     */
    public static String SlopeQ(SCPIParam param) {
        int i=Command.get().getTrigger_nedge().SlopeQ(); // 获取当前边沿类型
        return ToolsSCPI.getTriggerNedgeSlope(i); // 将边沿类型枚举值转换为SCPI字符串
    }

    /**
     * 设置第N边沿触发中开始边沿计数之前的空闲时间。
     * @param param SCPI命令参数，dParam1为空闲时间值（秒）
     */
    public static void Idle(SCPIParam param) {
        Command.get().getTrigger_nedge().Idle(param.dParam1, true); // 设置空闲时间，并通知硬件
    }

    /**
     * 查询第N边沿触发中开始边沿计数之前的空闲时间。
     * @param param SCPI命令参数
     * @return 空闲时间的字符串表示
     */
    public static String IdleQ(SCPIParam param) {
        double d=Command.get().getTrigger_nedge().IdleQ(); // 获取当前空闲时间
        return ToolsSCPI.getDouble(d); // 将空闲时间格式化为字符串返回
    }

    /**
     * 设置第N边沿触发的N值（第N个边沿触发采集）。
     * @param param SCPI命令参数，iParam1为边沿计数值N
     */
    public static void Edge(SCPIParam param) {
        Command.get().getTrigger_nedge().Edge(param.iParam1, true); // 设置边沿计数值N，并通知硬件
    }

    /**
     * 查询第N边沿触发的N值。
     * @param param SCPI命令参数
     * @return 边沿计数值N的字符串表示
     */
    public static String EdgeQ(SCPIParam param) {
        int i=Command.get().getTrigger_nedge().EdgeQ(); // 获取当前边沿计数值N
        return ToolsSCPI.getInt(i); // 将边沿计数值格式化为字符串返回
    }

    /**
     * 设置第N边沿触发时的触发电平。
     * @param param SCPI命令参数，dParam1为触发电平值（伏特）
     */
    public static void Level(SCPIParam param) {
        Command.get().getTrigger_nedge().Level(param.dParam1, true); // 设置触发电平，并通知硬件
    }

    /**
     * 设置第N边沿触发时的触发电平（Plus增量调整）。
     * @param param SCPI命令参数，iParam1为增量值
     */
    public static void Plus_Level(SCPIParam param) {
        Command.get().getTrigger_nedge().Plus_Level(param.iParam1, true); // 以增量方式设置触发电平，并通知硬件
    }

    /**
     * 查询第N边沿触发时的触发电平。
     * @param param SCPI命令参数
     * @return 触发电平的字符串表示
     */
    public static String LevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_nedge().LevelQ(); // 获取当前触发电平
        return ToolsSCPI.getDouble(d); // 将触发电平格式化为字符串返回
    }

}
