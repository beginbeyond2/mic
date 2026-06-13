package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Timeout {
//      new SCPICommandStruct(":TRIGger:TIMeout:SOURce","SCPI_Trigger_Timeout","Source"),//设置超时触发的触发源
//            new SCPICommandStruct(":TRIGger:TIMeout:SOURce?","SCPI_Trigger_Timeout","SourceQ"),//查询超时触发的触发源
//            new SCPICommandStruct(":TRIGger:TIMeout:POLarity","SCPI_Trigger_Timeout","Polarity"),//设置超时触发极性
//            new SCPICommandStruct(":TRIGger:TIMeout:POLarity?","SCPI_Trigger_Timeout","PolarityQ"),//查询超时触发极性
//            new SCPICommandStruct(":TRIGger:TIMeout:TIME","SCPI_Trigger_Timeout","Time"),//设置超时触发的超时时间
//            new SCPICommandStruct(":TRIGger:TIMeout:TIME?","SCPI_Trigger_Timeout","TimeQ"),//查询超时触发的超时时间
//            new SCPICommandStruct(":TRIGger:TIMeout:LEVel","SCPI_Trigger_Timeout","Level"),
//            new SCPICommandStruct(":TRIGger:TIMeout:LEVel?","SCPI_Trigger_Timeout","LevelQ"),

    private int source;
    /**
     * const char * trig_tim_pol[] = {
     * "POSitive",
     * "NEGative",
     * "EITHer",
     * NULL
     * };
     */
    private int polarity;
    private double time;
    private double level;

    /**
     * 设置超时触发的触发源
     */
    public void Source(int source, boolean isUpdateUI) {
        if (this.source == source) return;
        this.source = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERTIMEOUT_SOURCE);
            msgToUI.setParam(String.valueOf(source));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询超时触发的触发源
     */
    public int SourceQ() {
        return source;
    }

    /**
     * 设置超时触发极性
     */
    public void Polarity(int polar, boolean isUpdateUI) {
        if (this.polarity == polar) return;
        this.polarity = polar;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERTIMEOUT_POLAR);
            msgToUI.setParam(String.valueOf(polar));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询超时触发极性
     */
    public int PolarityQ() {
        return polarity;
    }

    /**
     * 设置超时触发的超时时间
     *
     * @param time 8ns至10s
     */
    public void Time(double time, boolean isUpdateUI) {
        if (Double.compare(this.time, time) == 0) return;
        this.time = time;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERTIMEOUT_TIME);
            msgToUI.setParam(String.valueOf(time));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询超时触发的超时时间
     */
    public double TimeQ() {
        return time;
    }

    /**
     * 设置脉宽触发时的触发电平
     */
    public void Level(double level, boolean isUpdateUI) {
        if (Double.compare(this.level, level) == 0) return;
        this.level = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERTIMEOUT_LEVEL);
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
