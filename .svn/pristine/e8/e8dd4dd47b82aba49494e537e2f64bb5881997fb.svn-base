package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Nedge {
//     new SCPICommandStruct(":TRIGger:NEDGe:SOURce","SCPI_Trigger_Nedge","Source"),//设置第N边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:NEDGe:SOURce?","SCPI_Trigger_Nedge","SourceQ"),//查询第N边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe","SCPI_Trigger_Nedge","Slope"),//设置第N边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe?","SCPI_Trigger_Nedge","SlopeQ"),//查询第N边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:NEDGe:IDLE","SCPI_Trigger_Nedge","Idle"),//设置第N边沿触发中开始边沿计数之前的空闲时间
//            new SCPICommandStruct(":TRIGger:NEDGe:IDLE?","SCPI_Trigger_Nedge","IdleQ"),//查询第N边沿触发中开始边沿计数之前的空闲时间
//            new SCPICommandStruct(":TRIGger:NEDGe:EDGE","SCPI_Trigger_Nedge","Edge"),//设置第N边沿触发的N的数值
//            new SCPICommandStruct(":TRIGger:NEDGe:EDGE?","SCPI_Trigger_Nedge","EdgeQ"),//查询第N边沿触发的N的数值
//            new SCPICommandStruct(":TRIGger:NEDGe:LEVel","SCPI_Trigger_Nedge","Level"),//设置第N边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:NEDGe:PLUS:LEVel","SCPI_Trigger_Nedge","Plus_Level"),//设置第N边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:NEDGe:LEVel?","SCPI_Trigger_Nedge","LevelQ"),//查询第N边沿触发时的触发电平

    private int source;
    /**
     * const char * trig_nedg_slop[] = {
     * "RISE",
     * "FALL",
     * NULL
     * };
     */
    private int slope;
    /**
     * const char * trig_nedg_slop[] = {
     * "RISE",
     * "FALL",
     * NULL
     * };
     */
    private double idle;
    private int edge;
    private double level;

    /**
     * 设置第N边沿触发的触发源
     */
    public void Source(int source, boolean isUpdateUI) {
        if (this.source == source) return;
        this.source = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_SOURCE);
            msgToUI.setParam(String.valueOf(source));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询第N边沿触发的触发源
     */
    public int SourceQ() {
        return source;
    }

    /**
     * 设置第N边沿触发的边沿类型
     */
    public void Slope(int slope, boolean isUpdateUI) {
//        if (this.slope == slope) return;
        this.slope = slope;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_SLOPE);
            msgToUI.setParam(String.valueOf(slope));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询第N边沿触发的边沿类型
     */
    public int SlopeQ() {
        return slope;
    }

    /**
     * 设置第N边沿触发中开始边沿计数之前的空闲时间
     *
     * @param idle 8ns至10s
     */
    public void Idle(double idle, boolean isUpdateUI) {
        if (this.idle == idle) return;
        this.idle = idle;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_IDLE);
            msgToUI.setParam(String.valueOf(idle));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询第N边沿触发中开始边沿计数之前的空闲时间
     */
    public double IdleQ() {
        return idle;
    }

    /**
     * 设置第N边沿触发的N的数值
     *
     * @param edge 1至65535
     */
    public void Edge(int edge, boolean isUpdateUI) {
        if (edge<=0) edge=1;
        if (edge>=65535) edge=65535;
        this.edge = edge;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_EDGE);
            msgToUI.setParam(String.valueOf(edge));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询第N边沿触发的N的数值
     */
    public int EdgeQ() {
        return edge;
    }

    /**
     * 设置第N边沿触发时的触发电平
     */
    public void Level(double level, boolean isUpdateUI) {
        if (Double.compare(this.level,level) == 0) return;
        this.level = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_LEVEL);
            msgToUI.setParam(String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置第N边沿触发时的触发电平
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
     * 查询第N边沿触发时的触发电平
     */
    public double LevelQ() {
        return level;
    }
}
