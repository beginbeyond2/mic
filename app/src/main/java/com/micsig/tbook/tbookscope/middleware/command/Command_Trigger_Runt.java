package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型


/*
 * +=============================================================================+
 * |                     Command_Trigger_Runt - 矮脉宽触发命令模型                      |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，矮脉宽(Runt)触发器的委托层                       |
 * | 核心职责 : 作为Runt触发器的适配层，将所有方法调用委托给Command中的trigger_dwart对象    |
 * |            （Runt由Dwart换名而来，保留原实现，本类仅做接口映射）                       |
 * | 架构设计 : 纯委托模式（Delegate），无自身状态，所有方法透传到trigger_dwart             |
 * | 数据流向 : 本类方法 → Command.get().getTrigger_dwart() → 实际逻辑处理               |
 * | 依赖关系 : Command(单例入口，持有trigger_dwart实例)                                  |
 * | 使用场景 : SCPI解析层通过本类间接操作矮脉宽触发器，实现接口兼容（Runt = Dwart重命名）  |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Runt {
//    //trigger dwart换名为trigger runt 2016.12.8
//            new SCPICommandStruct(":TRIGger:RUNT:SOURce","SCPI_Trigger_Runt","Source"),//设置矮脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:RUNT:SOURce?","SCPI_Trigger_Runt","SourceQ"),//查询矮脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:RUNT:POLarity","SCPI_Trigger_Runt","Polarity"),//设置矮脉宽触发的脉冲极性
//            new SCPICommandStruct(":TRIGger:RUNT:POLarity?","SCPI_Trigger_Runt","PolarityQ"),//查询矮脉宽触发的脉冲极性
//            new SCPICommandStruct(":TRIGger:RUNT:CONDition","SCPI_Trigger_Runt","Condition"),//设置矮脉宽触发的脉宽限制条件
//            new SCPICommandStruct(":TRIGger:RUNT:CONDition?","SCPI_Trigger_Runt","ConditionQ"),//查询矮脉宽触发的脉宽限制条件
//            new SCPICommandStruct(":TRIGger:RUNT:HTIMe","SCPI_Trigger_Runt","HTime"),//设置矮脉宽触发时的时间上限
//            new SCPICommandStruct(":TRIGger:RUNT:HTIMe?","SCPI_Trigger_Runt","HTimeQ"),//查询矮脉宽触发时的时间上限
//            new SCPICommandStruct(":TRIGger:RUNT:LTIMe","SCPI_Trigger_Runt","LTime"),//设置矮脉宽触发时的时间下限
//            new SCPICommandStruct(":TRIGger:RUNT:LTIMe?","SCPI_Trigger_Runt","LTimeQ"),//查询矮脉宽触发时的时间下限
//            new SCPICommandStruct(":TRIGger:RUNT:BTIMe","SCPI_Trigger_Runt","BTime"),//设置矮脉宽触发时的时间区间
//            new SCPICommandStruct(":TRIGger:RUNT:BTIMe?","SCPI_Trigger_Runt","BTimeQ"),//查询矮脉宽触发时的时间上限或下限
//            new SCPICommandStruct(":TRIGger:RUNT:HLEVel","SCPI_Trigger_Runt","HLevel"),//设置矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:RUNT:PLUS:HLEVel","SCPI_Trigger_Runt","Plus_HLevel"),//设置矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:RUNT:HLEVel?","SCPI_Trigger_Runt","HLevelQ"),//查询矮脉宽触发时的高电平
//            new SCPICommandStruct(":TRIGger:RUNT:LLEVel","SCPI_Trigger_Runt","LLevel"),//设置矮脉宽触发时的低电平
//            new SCPICommandStruct(":TRIGger:RUNT:PLUS:LLEVel","SCPI_Trigger_Runt","Plus_LLevel"),//设置矮脉宽触发时的低电平
//            new SCPICommandStruct(":TRIGger:RUNT:LLEVel?","SCPI_Trigger_Runt","LLevelQ"),//查询矮脉宽触发时的低电平

    /**
     * 设置矮脉宽触发的触发源（委托到trigger_dwart）
     *
     * @param index       触发源索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Source(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Source(index, isUpdateUI);  // 委托给trigger_dwart设置触发源
    }

    /**
     * 查询矮脉宽触发的触发源（委托到trigger_dwart）
     *
     * @return 触发源索引
     */
    public int SourceQ() {
        return Command.get().getTrigger_dwart().SourceQ();  // 委托给trigger_dwart查询触发源
    }

    /**
     * 设置矮脉宽触发的脉冲极性（委托到trigger_dwart）
     *
     * @param index       极性索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Polarity(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Polarity(index, isUpdateUI);  // 委托给trigger_dwart设置极性
    }

    /**
     * 查询矮脉宽触发的脉冲极性（委托到trigger_dwart）
     *
     * @return 极性索引
     */
    public int PolarityQ() {
        return Command.get().getTrigger_dwart().PolarityQ();  // 委托给trigger_dwart查询极性
    }

    /**
     * 设置矮脉宽触发的脉宽限制条件（委托到trigger_dwart）
     *
     * @param index       条件索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Condition(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Condition(index, isUpdateUI);  // 委托给trigger_dwart设置条件
    }

    /**
     * 查询矮脉宽触发的脉宽限制条件（委托到trigger_dwart）
     *
     * @return 条件索引
     */
    public int ConditionQ() {
        return Command.get().getTrigger_dwart().ConditionQ();  // 委托给trigger_dwart查询条件
    }

    /**
     * 设置矮脉宽触发时的时间上限（委托到trigger_dwart）
     *
     * @param hTime       时间上限值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void HTime(double hTime, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().HTime(hTime, isUpdateUI);  // 委托给trigger_dwart设置时间上限
    }

    /**
     * 查询矮脉宽触发时的时间上限（委托到trigger_dwart）
     *
     * @return 时间上限值
     */
    public double HTimeQ() {
        return Command.get().getTrigger_dwart().HTimeQ();  // 委托给trigger_dwart查询时间上限
    }

    /**
     * 设置矮脉宽触发时的时间下限（委托到trigger_dwart）
     *
     * @param lTime       时间下限值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void LTime(double lTime, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().LTime(lTime, isUpdateUI);  // 委托给trigger_dwart设置时间下限
    }

    /**
     * 查询矮脉宽触发时的时间下限（委托到trigger_dwart）
     *
     * @return 时间下限值
     */
    public double LTimeQ() {
        return Command.get().getTrigger_dwart().LTimeQ();  // 委托给trigger_dwart查询时间下限
    }

    /**
     * 设置矮脉宽触发时的时间区间（委托到trigger_dwart）
     *
     * @param hTime       时间上限值
     * @param lTime       时间下限值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void BTime(double hTime, double lTime, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().BTime(hTime, lTime, isUpdateUI);  // 委托给trigger_dwart设置时间区间
    }

    /**
     * 查询矮脉宽触发时的时间上限或下限（委托到trigger_dwart）
     *
     * @param highLow 0=查询上限，1=查询下限
     * @return 对应的时间值
     */
    public double BTimeQ(int highLow) {
        return Command.get().getTrigger_dwart().BTimeQ(highLow);  // 委托给trigger_dwart查询时间区间
    }

    /**
     * 设置矮脉宽触发时的高电平（委托到trigger_dwart）
     *
     * @param hLevel      高电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void HLevel(double hLevel, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().HLevel(hLevel, isUpdateUI);  // 委托给trigger_dwart设置高电平
    }

    /**
     * 设置矮脉宽触发时的高电平（步进调整，委托到trigger_dwart）
     *
     * @param index       步进方向：1=递增，-1=递减
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Plus_HLevel(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Plus_HLevel(index, isUpdateUI);  // 委托给trigger_dwart步进调整高电平
    }

    /**
     * 查询矮脉宽触发时的高电平（委托到trigger_dwart）
     *
     * @return 高电平值
     */
    public  double HLevelQ() {
        return Command.get().getTrigger_dwart().HLevelQ();  // 委托给trigger_dwart查询高电平
    }

    /**
     * 设置矮脉宽触发时的低电平（委托到trigger_dwart）
     *
     * @param lLevel      低电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void LLevel(double lLevel, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().LLevel(lLevel, isUpdateUI);  // 委托给trigger_dwart设置低电平
    }

    /**
     * 设置矮脉宽触发时的低电平（步进调整，委托到trigger_dwart）
     *
     * @param index       步进方向：1=递增，-1=递减
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Plus_LLevel(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Plus_LLevel(index, isUpdateUI);  // 委托给trigger_dwart步进调整低电平
    }

    /**
     * 查询矮脉宽触发时的低电平（委托到trigger_dwart）
     *
     * @return 低电平值
     */
    public double LLevelQ() {
        return Command.get().getTrigger_dwart().LLevelQ();  // 委托给trigger_dwart查询低电平
    }
}
