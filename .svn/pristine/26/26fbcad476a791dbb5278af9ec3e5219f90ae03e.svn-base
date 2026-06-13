package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Slope {
//     new SCPICommandStruct(":TRIGger:SLOPe:SOURce","SCPI_Trigger_Slope","Source"),//设置斜率触发的触发源
//     new SCPICommandStruct(":TRIGger:SLOPe:SOURce?","SCPI_Trigger_Slope","SourceQ"),//查询斜率触发的触发源
//     new SCPICommandStruct(":TRIGger:SLOPe:EDGE","SCPI_Trigger_Slope","Edge"),//设置斜率触发沿
//     new SCPICommandStruct(":TRIGger:SLOPe:EDGE?","SCPI_Trigger_Slope","EdgeQ"),//查询斜率触发沿
//     new SCPICommandStruct(":TRIGger:SLOPe:CONDition","SCPI_Trigger_Slope","Condition"),//设置斜率触发的限制条件
//     new SCPICommandStruct(":TRIGger:SLOPe:CONDition?","SCPI_Trigger_Slope","ConditionQ"),//查询斜率触发的限制条件
//     new SCPICommandStruct(":TRIGger:SLOPe:HTIMe","SCPI_Trigger_Slope","HTime"),//设置斜率触发时的时间上限
//     new SCPICommandStruct(":TRIGger:SLOPe:HTIMe?","SCPI_Trigger_Slope","HTimeQ"),//查询斜率触发时的时间上限
//     new SCPICommandStruct(":TRIGger:SLOPe:LTIMe","SCPI_Trigger_Slope","LTime"),//设置斜率触发时的时间下限
//     new SCPICommandStruct(":TRIGger:SLOPe:LTIMe?","SCPI_Trigger_Slope","LTimeQ"),//查询斜率触发时的时间下限
//     new SCPICommandStruct(":TRIGger:SLOPe:BTIMe","SCPI_Trigger_Slope","BTime"),//设置斜率触发时的时间区间
//     new SCPICommandStruct(":TRIGger:SLOPe:BTIMe?","SCPI_Trigger_Slope","BTimeQ"),//查询斜率触发时的时间上限或下限
//     new SCPICommandStruct(":TRIGger:SLOPe:HLEVel","SCPI_Trigger_Slope","HLevel"),//设置斜率触发时的高电平
//     new SCPICommandStruct(":TRIGger:SLOPe:PLUS:HLEVel","SCPI_Trigger_Slope","Plus_HLevel"),//设置斜率触发时的高电平
//     new SCPICommandStruct(":TRIGger:SLOPe:HLEVel?","SCPI_Trigger_Slope","HLevelQ"),//查询斜率触发时的高电平
//     new SCPICommandStruct(":TRIGger:SLOPe:LLEVel","SCPI_Trigger_Slope","LLevel"),//设置斜率触发时的低电平
//     new SCPICommandStruct(":TRIGger:SLOPe:PLUS:LLEVel","SCPI_Trigger_Slope","Plus_LLevel"),//设置斜率触发时的低电平
//     new SCPICommandStruct(":TRIGger:SLOPe:LLEVel?","SCPI_Trigger_Slope","LLevelQ"),//查询斜率触发时的低电平

    private int source;
    /**
     * const char * trig_slop_edge[] = {
     * "RISE",
     * "FALL",
     * "EITHer",
     * NULL
     * };
     */
    private int edge;
    /**
     * const char * trig_slop_cond[] = {
     * "LESS",
     * "GREater",
     * "BETWeen",
     * NULL
     * };
     */
    private int condition;
    private double hTime;
    private double lTime;
    private double hLevel;
    private double lLevel;

    /**
     * 设置斜率触发的触发源
     */
    public void Source(int source, boolean isUpdateUI) {
        if (this.source == source) return;
        this.source = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_SOURCE);
            msgToUI.setParam(String.valueOf(source));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询斜率触发的触发源
     */
    public int SourceQ() {
        return source;
    }

    /**
     * 设置斜率触发沿
     */
    public void Edge(int edge, boolean isUpdateUI) {
        if (this.edge == edge) return;
        this.edge = edge;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_EDGE);
            msgToUI.setParam(String.valueOf(edge));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询斜率触发沿
     */
    public int EdgeQ() {
        return edge;
    }

    /**
     * 设置斜率触发的限制条件
     */
    public void Condition(int condition, boolean isUpdateUI) {
        if (this.condition == condition) return;
        this.condition = condition;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_CONDITION);
            msgToUI.setParam(String.valueOf(condition));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询斜率触发的限制条件
     */
    public int ConditionQ() {
        return condition;
    }

    /**
     * 设置斜率触发时的时间上限
     *
     * @param highTime 8ns至10s
     */
    public void HTime(double highTime, boolean isUpdateUI) {
        this.hTime = highTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_HTIME);
            msgToUI.setParam(String.valueOf(highTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询斜率触发时的时间上限
     */
    public double HTimeQ() {
        return hTime;
    }

    /**
     * 设置斜率触发时的时间下限
     *
     * @param lowTime 8ns至10s
     */
    public void LTime(double lowTime, boolean isUpdateUI) {
        this.lTime = lowTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_LTIME);
            msgToUI.setParam(String.valueOf(lowTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询斜率触发时的时间下限
     */
    public double LTimeQ() {
        return lTime;
    }

    /**
     * 设置斜率触发时的时间区间
     *
     * @param highTime 8ns至10s
     * @param lowTime  8ns至10s
     */
    public void BTime(double highTime, double lowTime, boolean isUpdateUI) {
        this.hTime = highTime;
        this.lTime = lowTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_BTIME);
            msgToUI.setParam(String.valueOf(highTime) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(lowTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询斜率触发时的时间上限或下限
     */
    public double BTimeQ(int highLow) {
        if (highLow == 0) {
            return hTime;
        } else if (highLow == 1) {
            return lTime;
        }
        return 0;
    }

    /**
     * 设置斜率触发时的高电平
     */
    public void HLevel(double highLevel, boolean isUpdateUI) {
        this.hLevel = highLevel;
        if (isUpdateUI) {
            if (this.hLevel<this.lLevel){
                LLevel(this.hLevel,isUpdateUI);
            }
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_HLEVEL);
            msgToUI.setParam(String.valueOf(highLevel));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置斜率触发时的高电平
     */
    public void Plus_HLevel(int index, boolean isUpdateUI) {
        if (index == 1) {
            hLevel++;
        } else if (index == -1) {
            hLevel--;
        }
        HLevel(hLevel, isUpdateUI);
    }

    /**
     * 查询斜率触发时的高电平
     */
    public double HLevelQ() {
        return hLevel;
    }

    /**
     * 设置斜率触发时的低电平
     */
    public void LLevel(double lowLevel, boolean isUpdateUI) {
        this.lLevel = lowLevel;
        if (isUpdateUI) {
            if (this.lLevel>this.hLevel){
                HLevel(this.lLevel,isUpdateUI);
            }
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_LLEVEL);
            msgToUI.setParam(String.valueOf(lowLevel));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置斜率触发时的低电平
     */
    public void Plus_LLevel(int index, boolean isUpdateUI) {
        if (index == 1) {
            lLevel++;
        } else if (index == -1) {
            lLevel--;
        }
        LLevel(lLevel, isUpdateUI);
    }

    /**
     * 查询斜率触发时的低电平
     */
    public double LLevelQ() {
        return lLevel;
    }


}
