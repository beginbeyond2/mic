package com.micsig.tbook.tbookscope.middleware.command;


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

    public void Source(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Source(index, isUpdateUI);
    }

    public int SourceQ() {
        return Command.get().getTrigger_dwart().SourceQ();
    }

    public void Polarity(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Polarity(index, isUpdateUI);
    }

    public int PolarityQ() {
        return Command.get().getTrigger_dwart().PolarityQ();
    }

    public void Condition(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Condition(index, isUpdateUI);
    }

    public int ConditionQ() {
        return Command.get().getTrigger_dwart().ConditionQ();
    }

    public void HTime(double hTime, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().HTime(hTime, isUpdateUI);
    }

    public double HTimeQ() {
        return Command.get().getTrigger_dwart().HTimeQ();
    }

    public void LTime(double lTime, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().LTime(lTime, isUpdateUI);
    }

    public double LTimeQ() {
        return Command.get().getTrigger_dwart().LTimeQ();
    }

    public void BTime(double hTime, double lTime, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().BTime(hTime, lTime, isUpdateUI);
    }

    public double BTimeQ(int highLow) {
        return Command.get().getTrigger_dwart().BTimeQ(highLow);
    }

    public void HLevel(double hLevel, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().HLevel(hLevel, isUpdateUI);
    }

    public void Plus_HLevel(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Plus_HLevel(index, isUpdateUI);
    }

    public  double HLevelQ() {
        return Command.get().getTrigger_dwart().HLevelQ();
    }

    public void LLevel(double lLevel, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().LLevel(lLevel, isUpdateUI);
    }

    public void Plus_LLevel(int index, boolean isUpdateUI) {
        Command.get().getTrigger_dwart().Plus_LLevel(index, isUpdateUI);
    }

    public double LLevelQ() {
        return Command.get().getTrigger_dwart().LLevelQ();
    }
}
