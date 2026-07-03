package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象

/**
 * Created by liwb on 2018/1/12.
 *
 * +=============================================================================================================+
 * |                                          SCPI_Trigger_Pulse                                                 |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— 脉宽触发模块                                                                  |
 * | 核心职责 : 解析并执行脉宽触发相关的SCPI命令（设置/查询触发源、脉冲极性、                                       |
 * |            脉冲宽度、触发条件、触发电平）                                                                   |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令委托Command中间件执行，                               |
 * |            查询命令读取配置并通过ToolsSCPI转换为SCPI响应字符串                                               |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                         |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值/数值→字符串转换）                                     |
 * | 使用场景 : 仪器需要按脉冲宽度条件触发采集时（如等于/大于/小于/不在范围内），                                   |
 * |            用户通过SCPI命令配置脉宽触发条件                                                                  |
 * +=============================================================================================================+
 */

public class SCPI_Trigger_Pulse {
//     new SCPICommandStruct(":TRIGger:PULSe:SOURce","SCPI_Trigger_Pulse","Source"),//选择脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:PULSe:SOURce?","SCPI_Trigger_Pulse","SourceQ"),//查询脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:PULSe:POLarity","SCPI_Trigger_Pulse","Polarity"),//设置脉宽触发的极性
//            new SCPICommandStruct(":TRIGger:PULSe:POLarity?","SCPI_Trigger_Pulse","PolarityQ"),//查询脉宽触发的极性
//            new SCPICommandStruct(":TRIGger:PULSe:WIDTh","SCPI_Trigger_Pulse","Width"),//设置脉宽触发时的脉冲宽度值
//            new SCPICommandStruct(":TRIGger:PULSe:WIDTh?","SCPI_Trigger_Pulse","WidthQ"),//查询脉宽触发时的脉冲宽度值
//            new SCPICommandStruct(":TRIGger:PULSe:CONDition","SCPI_Trigger_Pulse","Condition"),//设置脉宽触发条件
//            new SCPICommandStruct(":TRIGger:PULSe:CONDition?","SCPI_Trigger_Pulse","ConditionQ"),//查询脉宽触发条件
//            new SCPICommandStruct(":TRIGger:PULSe:LEVel","SCPI_Trigger_Pulse","Level"),//设置脉宽触发时的触发电平
//            new SCPICommandStruct(":TRIGger:PULSe:PLUS:LEVel","SCPI_Trigger_Pulse","Plus_Level"),//设置脉宽触发时的触发电平
//            new SCPICommandStruct(":TRIGger:PULSe:LEVel?","SCPI_Trigger_Pulse","LevelQ"),//查询脉宽触发时的触发电平

    /**
     * 设置脉宽触发的触发源。
     * @param param SCPI命令参数，iParam1为触发源通道索引
     */
    public static void Source(SCPIParam param) {
        Command.get().getTrigger_pulse().Source(param.iParam1,true); // 设置脉宽触发源，并通知硬件
    }

    /**
     * 查询脉宽触发的触发源。
     * @param param SCPI命令参数
     * @return 触发源通道的SCPI字符串表示
     */
    public static String SourceQ(SCPIParam param) {
        int i=Command.get().getTrigger_pulse().SourceQ(); // 获取当前触发源通道索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为SCPI通道字符串
    }

    /**
     * 设置脉宽触发的脉冲极性（正/负）。
     * @param param SCPI命令参数，iParam1为极性枚举值
     */
    public static void Polarity(SCPIParam param) {
        Command.get().getTrigger_pulse().Polarity(param.iParam1,true); // 设置脉冲极性，并通知硬件
    }

    /**
     * 查询脉宽触发的脉冲极性。
     * @param param SCPI命令参数
     * @return 极性的SCPI字符串表示
     */
    public static String PolarityQ(SCPIParam param) {
        int i=Command.get().getTrigger_pulse().PolarityQ(); // 获取当前脉冲极性
        return ToolsSCPI.getTriggerPulsePolarity(i); // 将极性枚举值转换为SCPI字符串
    }

    /**
     * 设置脉宽触发时的脉冲宽度值。
     * @param param SCPI命令参数，dParam1为脉冲宽度值（秒）
     */
    public static void Width(SCPIParam param) {
        Command.get().getTrigger_pulse().Width(param.dParam1,true); // 设置脉冲宽度值，并通知硬件
    }

    /**
     * 查询脉宽触发时的脉冲宽度值。
     * @param param SCPI命令参数
     * @return 脉冲宽度的字符串表示
     */
    public static String WidthQ(SCPIParam param) {
        double d= Command.get().getTrigger_pulse().WidthQ(); // 获取当前脉冲宽度值
        return ToolsSCPI.getDouble(d); // 将脉冲宽度值格式化为字符串返回
    }

    /**
     * 设置脉宽触发条件（等于/不等于/大于/小于/在范围内/不在范围内）。
     * @param param SCPI命令参数，iParam1为触发条件枚举值
     */
    public static void Condition(SCPIParam param) {
        Command.get().getTrigger_pulse().Condition(param.iParam1,true); // 设置脉宽触发条件，并通知硬件
    }

    /**
     * 查询脉宽触发条件。
     * @param param SCPI命令参数
     * @return 触发条件的SCPI字符串表示
     */
    public static String ConditionQ(SCPIParam param) {
        int i=Command.get().getTrigger_pulse().ConditionQ(); // 获取当前脉宽触发条件
        return ToolsSCPI.getTriggerPulseCondition(i); // 将触发条件枚举值转换为SCPI字符串
    }

    /**
     * 设置脉宽触发时的触发电平。
     * @param param SCPI命令参数，dParam1为触发电平值（伏特）
     */
    public static void Level(SCPIParam param) {
        Command.get().getTrigger_pulse().Level(param.dParam1,true); // 设置触发电平，并通知硬件
    }

    /**
     * 设置脉宽触发时的触发电平（Plus增量调整）。
     * @param param SCPI命令参数，iParam1为增量值
     */
    public static void Plus_Level(SCPIParam param) {
        Command.get().getTrigger_pulse().Plus_Level(param.iParam1,true); // 以增量方式设置触发电平，并通知硬件
    }

    /**
     * 查询脉宽触发时的触发电平。
     * @param param SCPI命令参数
     * @return 触发电平的字符串表示
     */
    public static String LevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_pulse().LevelQ(); // 获取当前触发电平
        return ToolsSCPI.getDouble(d); // 将触发电平格式化为字符串返回
    }

}
