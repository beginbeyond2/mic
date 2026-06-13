package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Logic {
//     new SCPICommandStruct(":TRIGger:LOGic:STATus","SCPI_Trigger_Logic","Status"),//设置逻辑触发中通道的逻辑状态
//     new SCPICommandStruct(":TRIGger:LOGic:STATus?","SCPI_Trigger_Logic","StatusQ"),//查询逻辑触发中通道的逻辑状态
//     new SCPICommandStruct(":TRIGger:LOGic:FUNCtion","SCPI_Trigger_Logic","Function"),//设置逻辑触发的比较函数
//     new SCPICommandStruct(":TRIGger:LOGic:FUNCtion?","SCPI_Trigger_Logic","FunctionQ"),//查询逻辑触发的比较函数
//     new SCPICommandStruct(":TRIGger:LOGic:CONDition","SCPI_Trigger_Logic","Condition"),//设置逻辑触发条件
//     new SCPICommandStruct(":TRIGger:LOGic:CONDition?","SCPI_Trigger_Logic","ConditionQ"),//查询逻辑触发条件
//     new SCPICommandStruct(":TRIGger:LOGic:TIME","SCPI_Trigger_Logic","Time"),//设置触发逻辑时间
//     new SCPICommandStruct(":TRIGger:LOGic:TIME?","SCPI_Trigger_Logic","TimeQ"),//查询触发逻辑时间
//     new SCPICommandStruct(":TRIGger:LOGic:LEVel","SCPI_Trigger_Logic","Level"),//设置逻辑触发时的各通道触发电平
//     new SCPICommandStruct(":TRIGger:LOGic:PLUS:LEVel","SCPI_Trigger_Logic","Plus_Level"),//设置逻辑触发时的各通道触发电平
//     new SCPICommandStruct(":TRIGger:LOGic:LEVel?","SCPI_Trigger_Logic","LevelQ"),//查询逻辑触发时的各通道触发电平

    /**
     * const char * trig_log_stat[] = {
     * "HIGH",
     * "LOW",
     * "NONE",
     * NULL
     * };
     */
    private final int[] status = new int[]{2, 2, 2, 2, 2, 2, 2, 2};
    /**
     * const char * trig_log_fun[] = {
     * "AND",
     * "OR",
     * "NAND",
     * "NOR",
     * NULL
     * };
     */
    private int function;
    /**
     * const char * trig_log_cond[] = {
     * "LESS",
     * "GREat",
     * "EQUal",
     * "UNEQual",
     * "TRUE",
     * "FALSe",
     * NULL
     * };
     */
    private int condition;
    private double time;
    private double highTime;
    private double lowTime;
    private final double[] level = new double[]{0, 0, 0, 0, 0, 0, 0, 0};

    /**
     * 设置逻辑触发中通道的逻辑状态
     */
    public void Status(int channel, int status, boolean isUpdateUI) {
        if (this.status[channel] == status) return;
        this.status[channel] = status;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_STATUS);
            msgToUI.setParam(String.valueOf(channel) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(status));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询逻辑触发中通道的逻辑状态
     */
    public int StatusQ(int channnel) {
        return status[channnel];
    }

    /**
     * 设置逻辑触发的比较函数
     */
    public void Function(int index, boolean isUpdateUI) {
        if (this.function == index) return;
        function = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_FUNCTION);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询逻辑触发的比较函数
     */
    public int FunctionQ() {
        return function;
    }

    /**
     * 设置逻辑触发条件
     */
    public void Condition(int index, boolean isUpdateUI) {
        if (condition == index) return;
        condition = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_CONDITION);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询逻辑触发条件
     */
    public int ConditionQ() {
        return condition;
    }

    /**
     * 设置触发逻辑时间
     *
     * @param time 200ns至10s
     */
    public void Time(double time, boolean isUpdateUI) {
//        if (this.time == time) return;
        this.time = time;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_TIME);
            msgToUI.setParam(String.valueOf(time));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询触发逻辑时间
     */
    public double TimeQ() {
        return time;
    }

    /**
     * 设置逻辑触发时的时间上限
     *
     * @param highTime 8ns至10s
     */
    public void HTime(double highTime, boolean isUpdateUI) {
        this.highTime = highTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_HTIME);
            msgToUI.setParam(String.valueOf(highTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询逻辑触发时的时间上限
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_LOWTIME);
            msgToUI.setParam(String.valueOf(lowTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }


    /**
     * 查询逻辑触发时的时间下限
     */
    public double LTimeQ() {
        return lowTime;
    }


    /**
     * 设置逻辑触发时的时间区间
     *
     * @param highTime 8ns至10s
     * @param lowTime  8ns至10s
     */
    public void BTime(double highTime, double lowTime, boolean isUpdateUI) {
        this.highTime = highTime;
        this.lowTime = lowTime;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_BTIME);
            msgToUI.setParam(String.valueOf(highTime) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(lowTime));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询逻辑触发时的时间上限或下限
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
     * 设置逻辑触发时的各通道触发电平
     */
    public void Level(int channel, double level, boolean isUpdateUI) {
//        if (this.level[channel] == level) return;
        this.level[channel] = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_LEVEL);
            msgToUI.setParam(String.valueOf(channel) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置逻辑触发时的各通道触发电平
     */
    public void Plus_Level(int channel, int plus, boolean isUpdateUI) {
        if (plus == 1) {
            level[channel]++;
        } else if (plus == -1) {
            level[channel]--;
        }
        Level(channel, level[channel], isUpdateUI);
    }

    /**
     * 查询逻辑触发时的各通道触发电平
     */
    public double LevelQ(int channel) {
        return level[channel];
    }


}
