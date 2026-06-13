package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Dwart {

//   new SCPICommandStruct(":TRIGger:DWARt:SOURce","SCPI_Trigger_Dwart","Source"),//设置矮脉宽触发的触发源
//   new SCPICommandStruct(":TRIGger:DWARt:SOURce?","SCPI_Trigger_Dwart","SourceQ"),//查询矮脉宽触发的触发源
//   new SCPICommandStruct(":TRIGger:DWARt:POLarity","SCPI_Trigger_Dwart","Polarity"),//设置矮脉宽触发的脉冲极性
//   new SCPICommandStruct(":TRIGger:DWARt:POLarity?","SCPI_Trigger_Dwart","PolarityQ"),//查询矮脉宽触发的脉冲极性
//   new SCPICommandStruct(":TRIGger:DWARt:CONDition","SCPI_Trigger_Dwart","Condition"),//设置矮脉宽触发的脉宽限制条件
//   new SCPICommandStruct(":TRIGger:DWARt:CONDition?","SCPI_Trigger_Dwart","ConditionQ"),//查询矮脉宽触发的脉宽限制条件
//   new SCPICommandStruct(":TRIGger:DWARt:HTIMe","SCPI_Trigger_Dwart","HTime"),//设置矮脉宽触发时的时间上限
//   new SCPICommandStruct(":TRIGger:DWARt:HTIMe?","SCPI_Trigger_Dwart","HTimeQ"),//查询矮脉宽触发时的时间上限
//   new SCPICommandStruct(":TRIGger:DWARt:LTIMe","SCPI_Trigger_Dwart","LTime"),//设置矮脉宽触发时的时间下限
//   new SCPICommandStruct(":TRIGger:DWARt:LTIMe?","SCPI_Trigger_Dwart","LTimeQ"),//查询矮脉宽触发时的时间下限
//   new SCPICommandStruct(":TRIGger:DWARt:BTIMe","SCPI_Trigger_Dwart","BTime"),//设置矮脉宽触发时的时间区间
//   new SCPICommandStruct(":TRIGger:DWARt:BTIMe?","SCPI_Trigger_Dwart","BTimeQ"),//查询矮脉宽触发时的时间上限或下限
//   new SCPICommandStruct(":TRIGger:DWARt:HLEVel","SCPI_Trigger_Dwart","HLevel"),//设置矮脉宽触发时的高电平
//   new SCPICommandStruct(":TRIGger:DWARt:PLUS:HLEVel","SCPI_Trigger_Dwart","Plus_HLevel"),//设置矮脉宽触发时的高电平
//   new SCPICommandStruct(":TRIGger:DWARt:HLEVel?","SCPI_Trigger_Dwart","HLevelQ"),//查询矮脉宽触发时的高电平
//   new SCPICommandStruct(":TRIGger:DWARt:LLEVel","SCPI_Trigger_Dwart","LLevel"),//设置矮脉宽触发时的低电平
//   new SCPICommandStruct(":TRIGger:DWARt:PLUS:LLEVel","SCPI_Trigger_Dwart","Plus_LLevel"),//设置矮脉宽触发时的低电平
//   new SCPICommandStruct(":TRIGger:DWARt:LLEVel?","SCPI_Trigger_Dwart","LLevelQ"),//查询矮脉宽触发时的低电平

    private int source;
    private int polar;
    private int condition;
    private double highTime;
    private double lowTime;
    private double highLevel;
    private double lowLevel;

    /**
     * 设置矮脉宽触发的触发源
     */
    public void Source(int index, boolean isUpdateUI) {
        if (source == index) return;
        source = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_SOURCE);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询矮脉宽触发的触发源
     */
    public int SourceQ() {
        return source;
    }

    /**
     * 设置矮脉宽触发的脉冲极性
     */
    public void Polarity(int index, boolean isUpdateUI) {
        if (polar == index) return;
        polar = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_POLAR);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询矮脉宽触发的脉冲极性
     */
    public int PolarityQ() {
        return polar;
    }

    /**
     * 设置脉宽限制条件
     */
    public void Condition(int index, boolean isUpdateUI) {
        if (condition == index) return;
        condition = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_CONDITION);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询脉宽限制条件
     */
    public int ConditionQ() {
        return condition;
    }

    /**
     * 设置矮脉宽触发时的时间上限
     *
     * @param highTime 8ns至10s
     */
    public void HTime(double highTime, boolean isUpdateUI) {
        this.highTime = highTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_HTIME);
            msgToUI.setParam(String.valueOf(highTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询矮脉宽触发时的时间上限
     */
    public double HTimeQ() {
        return highTime;
    }

    /**
     * 设置矮脉宽触发时的时间下限
     *
     * @param lowTime 8ns至10s
     */
    public void LTime(double lowTime, boolean isUpdateUI) {
        this.lowTime = lowTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_LOWTIME);
            msgToUI.setParam(String.valueOf(lowTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询矮脉宽触发时的时间下限
     */
    public double LTimeQ() {
        return lowTime;
    }

    /**
     * 设置矮脉宽触发时的时间区间
     *
     * @param highTime 8ns至10s
     * @param lowTime  8ns至10s
     */
    public void BTime(double highTime, double lowTime, boolean isUpdateUI) {
        this.highTime = highTime;
        this.lowTime = lowTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_BTIME);
            msgToUI.setParam(String.valueOf(highTime) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(lowTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询矮脉宽触发时的时间上限或下限
     */
    public double BTimeQ(int highLow) {
        if (highLow == 0) {
            return highTime;
        } else if (highLow == 1) {
            return lowTime;
        }
        return 0;
    }

    /**
     * 设置矮脉宽触发时的高电平
     */
    public void HLevel(double highLevel, boolean isUpdateUI) {
//        if (this.highLevel == highLevel) return;
        this.highLevel = highLevel;
        if (isUpdateUI) {
            if (this.highLevel<this.lowLevel){
                LLevel(this.highLevel,isUpdateUI);
            }

            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_HLEVEL);
            msgToUI.setParam(String.valueOf(highLevel));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置矮脉宽触发时的高电平
     *
     * @param index 1为加一个单位，-1为减一个单位；
     */
    public void Plus_HLevel(int index, boolean isUpdateUI) {
        if (index == 1) {
            highLevel++;
        } else if (index == -1) {
            highLevel--;
        }
        HLevel(highLevel, isUpdateUI);
    }

    /**
     * 查询矮脉宽触发时的高电平
     */
    public double HLevelQ() {
        return highLevel;
    }

    /**
     * 设置矮脉宽触发时的低电平
     */
    public void LLevel(double lowLevel, boolean isUpdateUI) {
//        if (this.lowLevel == lowLevel) return;
        this.lowLevel = lowLevel;
        if (isUpdateUI) {
            if (this.lowLevel>this.highLevel){
                HLevel(this.lowLevel,isUpdateUI);
            }
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_LLEVEL);
            msgToUI.setParam(String.valueOf(lowLevel));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置矮脉宽触发时的低电平
     *
     * @param index 1为加一个单位，-1为减一个单位；
     */
    public void Plus_LLevel(int index, boolean isUpdateUI) {
        if (index == 1) {
            lowLevel++;
        } else if (index == -1) {
            lowLevel--;
        }
        LLevel(lowLevel, isUpdateUI);
    }

    /**
     * 查询矮脉宽触发时的低电平
     */
    public double LLevelQ() {
        return lowLevel;
    }

}
