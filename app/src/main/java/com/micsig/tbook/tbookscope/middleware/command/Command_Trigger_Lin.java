package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Lin {
//      new SCPICommandStruct(":TRIGger:LIN:SOURce","SCPI_Trigger_Lin","Source"),//设置LIN触发的触发源
//            new SCPICommandStruct(":TRIGger:LIN:SOURce?","SCPI_Trigger_Lin","SourceQ"),//查询LIN触发的触发源
//            new SCPICommandStruct(":TRIGger:LIN:TYPE","SCPI_Trigger_Lin","Type"),//设置LIN触发的触发条件
//            new SCPICommandStruct(":TRIGger:LIN:TYPE?","SCPI_Trigger_Lin","TypeQ"),//查询LIN触发的触发条件
//            new SCPICommandStruct(":TRIGger:LIN:ID","SCPI_Trigger_Lin","Id"),//当LIN总线触发条件为FID或IDATa时，设置LIN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:LIN:ID?","SCPI_Trigger_Lin","IdQ"),//当LIN总线触发条件为FID或IDATa时，查询LIN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:LIN:DATA","SCPI_Trigger_Lin","Data"),//当LIN总线触发条件为IDATa时，设置LIN触发的触发数据
//            new SCPICommandStruct(":TRIGger:LIN:DATA?","SCPI_Trigger_Lin","DataQ"),//当LIN总线触发条件为IDATa时，查询LIN触发的触发数据
//            new SCPICommandStruct(":TRIGger:LIN:LEVel","SCPI_Trigger_Lin","Level"),//设置LIN触发时的触发电平
//            new SCPICommandStruct(":TRIGger:LIN:LEVel?","SCPI_Trigger_Lin","LevelQ"),//查询LIN触发时的触发电平

    private final int[] source = new int[ChannelFactory.SERIAL_CNT];
    private final int[] idle = new int[ChannelFactory.SERIAL_CNT];
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT];
    private final int[] linType = new int[ChannelFactory.SERIAL_CNT];
    /**
     * 同步上升沿
     * 帧ID
     * 帧ID和数据
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];
    private final int[] id = new int[ChannelFactory.SERIAL_CNT];
    private final long[] data = new long[ChannelFactory.SERIAL_CNT];
    private final double[] level = new double[ChannelFactory.SERIAL_CNT];

    public int getLinType(int serials) {
        return linType[serials];
    }

    public void setLinType(int serials, int linType, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.linType)) return;
        if (this.linType[serials] == serials) return;
        this.linType[serials] = linType;
        if (isUpdataUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_TYPE);
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(linType);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getSource(int serials) {
        return source[serials];
    }

    public void setSource(int serials, int source, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.source)) return;
        if (this.source[serials] == serials) return;
        this.source[serials] = source;
        if (isUpdataUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_SOURCE);
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getIdle(int serials) {
        return idle[serials];
    }

    public void setIdle(int serials, int idle, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.idle)) return;
        if (this.idle[serials] == idle) return;
        this.idle[serials] = idle;
        if (isUpdataUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_IDLE);
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(idle);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getBaudRate(int serials) {
        return baudRate[serials];
    }

    public void setBaudRate(int serials, int baudRate, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.baudRate)) return;
        if (this.baudRate[serials] == baudRate) return;
        this.baudRate[serials] = baudRate;
        if (isUpdataUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_BAUDRATE);
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getType(int serials) {
        return type[serials];
    }

    public int getId(int serials) {
        return id[serials];
    }

    public long getData(int serials) {
        return data[serials];
    }

    public void setType(int serials, int type, int id, long data, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;
        if(!ToolsSCPI.isCorrect(serials,this.id)) return;
        if(!ToolsSCPI.isCorrect(serials,this.data)) return;
//        if (this.type[serials] == type && this.id[serials] == id && this.data[serials] == data)
//            return;
        this.type[serials] = type;
        this.id[serials] = id;
        this.data[serials] = data;
        switch (type) {

        }
        if (isUpdataUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_TYPE);
            String param = String.valueOf(serials)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(id)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(data);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevel(int serials) {
        return level[serials];
    }

    public void setLevel(int serials, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.level)) return;
        if (this.level[serials] == level) return;
        this.level[serials] = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_LEVEL);
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
}
