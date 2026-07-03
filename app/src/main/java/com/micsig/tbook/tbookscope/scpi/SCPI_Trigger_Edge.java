package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import android.util.Log; // 导入Android日志类（预留，当前未使用）

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象

/**
 * Created by liwb on 2018/1/12.
 *
 * +=============================================================================================================+
 * |                                          SCPI_Trigger_Edge                                                  |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— 边沿触发模块                                                                  |
 * | 核心职责 : 解析并执行边沿触发相关的SCPI命令（设置/查询触发源、边沿类型、                                       |
 * |            触发电平、触发耦合方式）                                                                        |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令委托Command中间件执行，                               |
 * |            查询命令读取配置并通过ToolsSCPI转换为SCPI响应字符串                                               |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                         |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值→字符串转换）                                         |
 * | 使用场景 : 仪器最常用的触发模式，用户通过SCPI命令设置边沿触发条件（上升沿/下降沿/双边沿）                    |
 * +=============================================================================================================+
 */

public class SCPI_Trigger_Edge {
//     new SCPICommandStruct(":TRIGger:EDGE:SOURce","SCPI_Trigger_Edge","Source"),//选择边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:EDGE:SOURce?","SCPI_Trigger_Edge","SourceQ"),//查询边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:EDGE:SLOPe","SCPI_Trigger_Edge","Slope"),//选择边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:EDGE:SLOPe?","SCPI_Trigger_Edge","SlopeQ"),//查询边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:EDGE:LEVel","SCPI_Trigger_Edge","Level"),//设置边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:PLUS:LEVel","SCPI_Trigger_Edge","Plus_Level"),//设置边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:LEVel?","SCPI_Trigger_Edge","LevelQ"),//查询边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:COUPle","SCPI_Trigger_Edge","Couple"),//设置边沿触发耦合方式。
//            new SCPICommandStruct(":TRIGger:EDGE:COUPle?","SCPI_Trigger_Edge","CoupleQ"),//查询边沿触发耦合方式。

    /**
     * 设置边沿触发的触发源。
     * @param param SCPI命令参数，iParam1为触发源通道索引
     */
    public static void Source(SCPIParam param) {
        Command.get().getTrigger_edge().Source(param.iParam1, true); // 设置边沿触发源，并通知硬件
    }

    /**
     * 查询边沿触发的触发源。
     * @param param SCPI命令参数
     * @return 触发源通道的SCPI字符串表示（支持全部通道格式）
     */
    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getTrigger_edge().SourceQ(); // 获取当前触发源通道索引
        return ToolsSCPI.getChAll(i); // 将通道索引转换为SCPI通道字符串（含ALL选项）
    }

    /**
     * 设置边沿触发的边沿类型（上升沿/下降沿/双边沿/任一边沿）。
     * @param param SCPI命令参数，iParam1为边沿类型枚举值
     */
    public static void Slope(SCPIParam param) {
        Command.get().getTrigger_edge().Slope(param.iParam1, true); // 设置边沿类型，并通知硬件
    }

    /**
     * 查询边沿触发的边沿类型。
     * @param param SCPI命令参数
     * @return 边沿类型的SCPI字符串表示
     */
    public static String SlopeQ(SCPIParam param) {
        int i=Command.get().getTrigger_edge().SlopeQ(); // 获取当前边沿类型
        return ToolsSCPI.getTriggerEdgeSlope(i); // 将边沿类型枚举值转换为SCPI字符串
    }

    /**
     * 设置边沿触发时的触发电平。
     * @param param SCPI命令参数，dParam1为触发电平值（伏特）
     */
    public static void Level(SCPIParam param) {
        Command.get().getTrigger_edge().Level(param.dParam1, true); // 设置触发电平，并通知硬件
    }

    /**
     * 设置边沿触发时的触发电平（Plus增量调整）。
     * @param param SCPI命令参数，iParam1为增量值
     */
    public static void Plus_Level(SCPIParam param) {
        Command.get().getTrigger_edge().Plus_Level(param.iParam1, true); // 以增量方式设置触发电平，并通知硬件
    }

    /**
     * 查询边沿触发时的触发电平。
     * @param param SCPI命令参数
     * @return 触发电平的字符串表示
     */
    public static String LevelQ(SCPIParam param) {
        double d= Command.get().getTrigger_edge().LevelQ(); // 获取当前触发电平
        return ToolsSCPI.getDouble(d); // 将触发电平格式化为字符串返回
    }

    /**
     * 设置边沿触发的耦合方式（直流/交流/低频抑制/高频抑制）。
     * @param param SCPI命令参数，iParam1为耦合方式枚举值
     */
    public static void Couple(SCPIParam param) {
        Command.get().getTrigger_edge().Couple(param.iParam1, true); // 设置耦合方式，并通知硬件
    }

    /**
     * 查询边沿触发的耦合方式。
     * @param param SCPI命令参数
     * @return 耦合方式的SCPI字符串表示
     */
    public static String CoupleQ(SCPIParam param) {
        int i=Command.get().getTrigger_edge().CoupleQ(); // 获取当前耦合方式
        return ToolsSCPI.getTriggerEdgeCouple(i); // 将耦合方式枚举值转换为SCPI字符串
    }


}
