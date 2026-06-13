package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Edge {
//     new SCPICommandStruct(":TRIGger:EDGE:SOURce","SCPI_Trigger_Edge","Source"),//选择边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:EDGE:SOURce?","SCPI_Trigger_Edge","SourceQ"),//查询边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:EDGE:SLOPe","SCPI_Trigger_Edge","Slope"),//选择边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:EDGE:SLOPe?","SCPI_Trigger_Edge","SlopeQ"),//查询边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:EDGE:LEVel","SCPI_Trigger_Edge","Level"),//设置边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:PLUS:LEVel","SCPI_Trigger_Edge","Plus_Level"),//设置边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:LEVel?","SCPI_Trigger_Edge","LevelQ"),//查询边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:COUPle","SCPI_Trigger_Edge","Couple"),//设置边沿触发耦合方式。
//            new SCPICommandStruct(":TRIGger:EDGE:COUPle?","SCPI_Trigger_Edge","CoupleQ"),//查询边沿触发耦合方式。

    private int source;
    /**
     * const char * trig_edge_slop[] = {
     * "RISE",
     * "FALL",
     * "DUAL",
     * NULL
     * };
     */
    private int slope;
    private double level;
    /**
     * const char * trig_edge_coup[] = {
     * "DC",
     * "AC",
     * "HFRej",
     * "LFRej",
     * "NOISerej",
     * NULL
     * };
     */
    private int couple;

    /**
     * 选择边沿触发的触发源
     */
    public void Source(int index, boolean isUpdateUI) {
//        if (source == index) return;
        source = index;
        if (isUpdateUI) {
            index= TChan.ExtChToChIdxOfScpi(index);
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGEREDGE_SOURCE);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询边沿触发的触发源
     */
    public int SourceQ() {
        return source;
    }

    /**
     * 选择边沿触发的边沿类型
     */
    public void Slope(int index, boolean isUpdateUI) {
//        if (slope == index) return;
        slope = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGEREDGE_SLOPE);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询边沿触发的边沿类型
     */
    public int SlopeQ() {
        return slope;
    }

    /**
     * 设置边沿触发时的触发电平
     */
    public void Level(double level, boolean isUpdateUI) {
        if (this.level == level) return;
        this.level = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGEREDGE_LEVEL);
            msgToUI.setParam(String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置边沿触发时的触发电平
     */
    public void Plus_Level(int index, boolean isUpdateUI) {
        if (index == 1) {
            level++;
        } else if (index == -1) {
            level--;
        }
        Level(level,isUpdateUI);
    }

    /**
     * 查询边沿触发时的触发电平
     */
    public double LevelQ() {
        return level;
    }

    /**
     * 设置边沿触发耦合方式
     */
    public void Couple(int index, boolean isUpdateUI) {
        if (this.couple == index) return;
        couple = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGEREDGE_COUPLE);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询边沿触发耦合方式
     */
    public int CoupleQ() {
        return couple;
    }


}
