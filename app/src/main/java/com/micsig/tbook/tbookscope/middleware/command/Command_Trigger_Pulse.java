package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Pulse {
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

    private int source;
    /**
     * const char * trig_puls_pol[] = {
     * "POSitive",
     * "NEGative",
     * NULL
     * };
     */
    private int polarity;
    private double width;
    private double highTime;
    private double lowTime;
    /**
     * const char * trig_puls_cond[] = {
     * "LESS",
     * "GREat",
     * "EQUal",
     * "UNEQual",
     * NULL
     * };
     */
    private int condition;
    private double level;

    /**
     * 选择脉宽触发的触发源
     */
    public void Source(int source, boolean isUpdateUI) {
//        if (this.source == source) return;
        this.source = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_SOURCE);
            msgToUI.setParam(String.valueOf(source));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询脉宽触发的触发源
     */
    public int SourceQ() {
        return source;
    }

    /**
     * 设置脉宽触发的极性
     */
    public void Polarity(int polar, boolean isUpdateUI) {
        if (this.polarity == polar) return;
        this.polarity = polar;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_POLAR);
            msgToUI.setParam(String.valueOf(polar));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询脉宽触发的极性
     */
    public int PolarityQ() {
        return polarity;
    }


    /**
     * 设置脉宽触发时的时间上限
     *
     * @param highTime 8ns至10s
     */
    public void HTime(double highTime, boolean isUpdateUI) {
        this.highTime = highTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_HTIME);
            msgToUI.setParam(String.valueOf(highTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询脉宽触发时的时间上限
     */
    public double HTimeQ() {
        return highTime;
    }

    /**
     * 设置逻辑触发时的时间下限
     *
     * @param lowTime 8ns至10s
     */
    public void LTime(double lowTime, boolean isUpdateUI) {
        this.lowTime = lowTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_LTIME);
            msgToUI.setParam(String.valueOf(lowTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }


    /**
     * 查询脉宽触发时的时间下限
     */
    public double LTimeQ() {
        return lowTime;
    }


    /**
     * 设置脉宽触发时的脉冲宽度值
     *
     * @param width 40ns至10s
     */
    public void Width(double width, boolean isUpdateUI) {
//        if (Double.compare(this.width, width) == 0) return;
        this.width = width;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_WIDTH);
            msgToUI.setParam(String.valueOf(width));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询脉宽触发时的脉冲宽度值
     */
    public double WidthQ() {
        return width;
    }

    /**
     * 设置脉宽触发条件
     */
    public void Condition(int condition, boolean isUpdateUI) {
        if (this.condition == condition) return;
        this.condition = condition;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_CONDITION);
            msgToUI.setParam(String.valueOf(condition));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询脉宽触发条件
     */
    public int ConditionQ() {
        return condition;
    }

    /**
     * 设置脉宽触发时的触发电平
     */
    public void Level(double level, boolean isUpdateUI) {
        if (Double.compare(this.level,level) == 0) return;
        this.level = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_LEVEL);
            msgToUI.setParam(String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置脉宽触发时的触发电平
     */
    public void Plus_Level(int index, boolean isUpdateUI) {
        if (index == 1) {
            level++;
        } else if (index == -1) {
            level--;
        }
        Level(level, isUpdateUI);
    }

    /**
     * 查询脉宽触发时的触发电平
     */
    public double LevelQ() {
        return level;
    }
}
