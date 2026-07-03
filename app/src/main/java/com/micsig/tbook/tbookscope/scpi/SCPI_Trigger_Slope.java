package com.micsig.tbook.tbookscope.scpi; // 定义SCPI触发斜率模块的包路径

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发斜率配置对象

/**
 * +--------------------------------------------------------------------------+
 * |                       SCPI_Trigger_Slope                                 |
 * +--------------------------------------------------------------------------+
 * | 模块定位: SCPI协议 - 触发子系统 - 斜率(Slope)触发命令处理类                   |
 * | 核心职责: 解析并执行与斜率触发相关的SCPI命令(设置/查询触发源、边沿、条件、时间、电平) |
 * | 架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command单例访问底层触发配置 |
 * | 数据流向: SCPI命令字符串 → SCPIParam参数解析 → 本类静态方法 → Command中间件 → 底层触发引擎 |
 * | 依赖关系: Command(获取触发斜率配置), SCPIParam(命令参数), ToolsSCPI(格式化工具) |
 * | 使用场景: 远程控制/自动化测试时，通过SCPI协议设置示波器斜率触发的各项参数        |
 * +--------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */
public class SCPI_Trigger_Slope {
//     new SCPICommandStruct(":TRIGger:SLOPe:SOURce","SCPI_Trigger_Slope","Source"),//设置斜率触发的触发源
//            new SCPICommandStruct(":TRIGger:SLOPe:SOURce?","SCPI_Trigger_Slope","SourceQ"),//查询斜率触发的触发源
//            new SCPICommandStruct(":TRIGger:SLOPe:EDGE","SCPI_Trigger_Slope","Edge"),//设置斜率触发沿
//            new SCPICommandStruct(":TRIGger:SLOPe:EDGE?","SCPI_Trigger_Slope","EdgeQ"),//查询斜率触发沿
//            new SCPICommandStruct(":TRIGger:SLOPe:CONDition","SCPI_Trigger_Slope","Condition"),//设置斜率触发的限制条件
//            new SCPICommandStruct(":TRIGger:SLOPe:CONDition?","SCPI_Trigger_Slope","ConditionQ"),//查询斜率触发的限制条件
//            new SCPICommandStruct(":TRIGger:SLOPe:HTIMe","SCPI_Trigger_Slope","HTime"),//设置斜率触发时的时间上限
//            new SCPICommandStruct(":TRIGger:SLOPe:HTIMe?","SCPI_Trigger_Slope","HTimeQ"),//查询斜率触发时的时间上限
//            new SCPICommandStruct(":TRIGger:SLOPe:LTIMe","SCPI_Trigger_Slope","LTime"),//设置斜率触发时的时间下限
//            new SCPICommandStruct(":TRIGger:SLOPe:LTIMe?","SCPI_Trigger_Slope","LTimeQ"),//查询斜率触发时的时间下限
//            new SCPICommandStruct(":TRIGger:SLOPe:BTIMe","SCPI_Trigger_Slope","BTime"),//设置斜率触发时的时间区间
//            new SCPICommandStruct(":TRIGger:SLOPe:BTIMe?","SCPI_Trigger_Slope","BTimeQ"),//查询斜率触发时的时间上限或下限
//            new SCPICommandStruct(":TRIGger:SLOPe:HLEVel","SCPI_Trigger_Slope","HLevel"),//设置斜率触发时的高电平
//            new SCPICommandStruct(":TRIGger:SLOPe:PLUS:HLEVel","SCPI_Trigger_Slope","Plus_HLevel"),//设置斜率触发时的高电平
//            new SCPICommandStruct(":TRIGger:SLOPe:HLEVel?","SCPI_Trigger_Slope","HLevelQ"),//查询斜率触发时的高电平
//            new SCPICommandStruct(":TRIGger:SLOPe:LLEVel","SCPI_Trigger_Slope","LLevel"),//设置斜率触发时的低电平
//            new SCPICommandStruct(":TRIGger:SLOPe:PLUS:LLEVel","SCPI_Trigger_Slope","Plus_LLevel"),//设置斜率触发时的低电平
//            new SCPICommandStruct(":TRIGger:SLOPe:LLEVel?","SCPI_Trigger_Slope","LLevelQ"),//查询斜率触发时的低电平

    /**
     * 设置斜率触发的触发源
     * @param param SCPI命令参数对象，iParam1为触发源通道索引
     */
    public static void Source(SCPIParam param) {
        Command.get().getTrigger_slope().Source(param.iParam1, true); // 调用底层设置斜率触发源，iParam1=通道索引，true=立即生效
    }

    /**
     * 查询斜率触发的触发源
     * @param param SCPI命令参数对象
     * @return 触发源通道的字符串表示（如CH1、CH2）
     */
    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getTrigger_slope().SourceQ(); // 从底层获取斜率触发源通道索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为SCPI标准通道字符串
    }

    /**
     * 设置斜率触发的触发沿（上升沿/下降沿/任意沿）
     * @param param SCPI命令参数对象，iParam1为边沿类型枚举值
     */
    public static void Edge(SCPIParam param) {
        Command.get().getTrigger_slope().Edge(param.iParam1, true); // 调用底层设置斜率触发沿，iParam1=边沿类型，true=立即生效
    }

    /**
     * 查询斜率触发的触发沿
     * @param param SCPI命令参数对象
     * @return 触发沿类型的字符串表示
     */
    public static String EdgeQ(SCPIParam param) {
        int i= Command.get().getTrigger_slope().EdgeQ(); // 从底层获取斜率触发沿类型整数值
        return ToolsSCPI.getTriggerSlopeEdge(i); // 将整数边沿枚举转换为SCPI标准字符串
    }

    /**
     * 设置斜率触发的限制条件（大于/小于/介于等）
     * @param param SCPI命令参数对象，iParam1为条件类型枚举值
     */
    public static void Condition(SCPIParam param) {
        Command.get().getTrigger_slope().Condition(param.iParam1, true); // 调用底层设置斜率触发条件，iParam1=条件类型，true=立即生效
    }

    /**
     * 查询斜率触发的限制条件
     * @param param SCPI命令参数对象
     * @return 限制条件的字符串表示
     */
    public static String ConditionQ(SCPIParam param) {
        int i=Command.get().getTrigger_slope().ConditionQ(); // 从底层获取斜率触发条件类型整数值
        return ToolsSCPI.getTriggerSlopeCondition(i); // 将整数条件枚举转换为SCPI标准字符串
    }

    /**
     * 设置斜率触发时的时间上限
     * @param param SCPI命令参数对象，dParam1为时间上限值（秒）
     */
    public static void HTime(SCPIParam param) {
        Command.get().getTrigger_slope().HTime(param.dParam1, true); // 调用底层设置时间上限，dParam1=时间值，true=立即生效
    }

    /**
     * 查询斜率触发时的时间上限
     * @param param SCPI命令参数对象
     * @return 时间上限的字符串表示
     */
    public static String HTimeQ(SCPIParam param) {
        double d= Command.get().getTrigger_slope().HTimeQ(); // 从底层获取时间上限值
        return ToolsSCPI.getDouble(d); // 将double值格式化为SCPI标准字符串
    }

    /**
     * 设置斜率触发时的时间下限
     * @param param SCPI命令参数对象，dParam1为时间下限值（秒）
     */
    public static void LTime(SCPIParam param) {
        Command.get().getTrigger_slope().LTime(param.dParam1, true); // 调用底层设置时间下限，dParam1=时间值，true=立即生效
    }

    /**
     * 查询斜率触发时的时间下限
     * @param param SCPI命令参数对象
     * @return 时间下限的字符串表示
     */
    public static String LTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_slope().LTimeQ(); // 从底层获取时间下限值
        return ToolsSCPI.getDouble(d); // 将double值格式化为SCPI标准字符串

    }

    /**
     * 设置斜率触发时的时间区间（同时设置上限和下限）
     * @param param SCPI命令参数对象，dParam1为时间下限，dParam2为时间上限
     */
    public static void BTime(SCPIParam param) {
        Command.get().getTrigger_slope().BTime(param.dParam1,param.dParam2, true); // 调用底层设置时间区间，dParam1=下限，dParam2=上限，true=立即生效
    }

    /**
     * 查询斜率触发时的时间上限或下限
     * @param param SCPI命令参数对象，iParam1指定查询上限(0)或下限(1)
     * @return 时间值的字符串表示
     */
    public static String BTimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_slope().BTimeQ(param.iParam1); // 从底层获取时间值，iParam1=0查上限，iParam1=1查下限
        return ToolsSCPI.getDouble(d); // 将double值格式化为SCPI标准字符串
    }

    /**
     * 设置斜率触发时的高电平
     * @param param SCPI命令参数对象，dParam1为高电平值（伏特）
     */
    public static void HLevel(SCPIParam param) {
        Command.get().getTrigger_slope().HLevel(param.dParam1, true); // 调用底层设置高电平，dParam1=电平值，true=立即生效
    }

    /**
     * 设置斜率触发时的高电平（PLUS变体，用于双通道模式）
     * @param param SCPI命令参数对象，iParam1为高电平整数值
     */
    public static void Plus_HLevel(SCPIParam param) {
        Command.get().getTrigger_slope().Plus_HLevel(param.iParam1, true); // 调用底层PLUS变体设置高电平，iParam1=电平整数值，true=立即生效
    }

    /**
     * 查询斜率触发时的高电平
     * @param param SCPI命令参数对象
     * @return 高电平值的字符串表示
     */
    public static String HLevelQ(SCPIParam param) {
        double d= Command.get().getTrigger_slope().HLevelQ(); // 从底层获取高电平值
        return ToolsSCPI.getDouble(d); // 将double值格式化为SCPI标准字符串
    }

    /**
     * 设置斜率触发时的低电平
     * @param param SCPI命令参数对象，dParam1为低电平值（伏特）
     */
    public static void LLevel(SCPIParam param) {
        Command.get().getTrigger_slope().LLevel(param.dParam1, true); // 调用底层设置低电平，dParam1=电平值，true=立即生效
    }

    /**
     * 设置斜率触发时的低电平（PLUS变体，用于双通道模式）
     * @param param SCPI命令参数对象，iParam1为低电平整数值
     */
    public static void Plus_LLevel(SCPIParam param) {
        Command.get().getTrigger_slope().Plus_LLevel(param.iParam1, true); // 调用底层PLUS变体设置低电平，iParam1=电平整数值，true=立即生效
    }

    /**
     * 查询斜率触发时的低电平
     * @param param SCPI命令参数对象
     * @return 低电平值的字符串表示
     */
    public static String LLevelQ(SCPIParam param) {
        double d=Command.get().getTrigger_slope().LLevelQ(); // 从底层获取低电平值
        return ToolsSCPI.getDouble(d); // 将double值格式化为SCPI标准字符串
    }

}
