package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Can {
//    new SCPICommandStruct(":TRIGger:CAN:SOURce","SCPI_Trigger_Can","Source"),//设置CAN触发的触发源
//            new SCPICommandStruct(":TRIGger:CAN:SOURce?","SCPI_Trigger_Can","SourceQ"),//查询CAN触发的触发源
//            new SCPICommandStruct(":TRIGger:CAN:TYPE","SCPI_Trigger_Can","Type"),//设置CAN触发的触发条件
//            new SCPICommandStruct(":TRIGger:CAN:TYPE?","SCPI_Trigger_Can","TypeQ"),//查询CAN触发的触发条件
//            new SCPICommandStruct(":TRIGger:CAN:ID","SCPI_Trigger_Can","Id"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，设置CAN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:CAN:ID?","SCPI_Trigger_Can","IdQ"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，查询CAN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:CAN:DLC","SCPI_Trigger_Can","DLC"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的DLC值
//            new SCPICommandStruct(":TRIGger:CAN:DLC?","SCPI_Trigger_Can","DLCQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的DLC值
//            new SCPICommandStruct(":TRIGger:CAN:DATA","SCPI_Trigger_Can","Data"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的触发数据值
//            new SCPICommandStruct(":TRIGger:CAN:DATA?","SCPI_Trigger_Can","DataQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的触发数据值
//            new SCPICommandStruct(":TRIGger:CAN:LEVel","SCPI_Trigger_Can","Level"),//设置CAN触发时的触发电平
//            new SCPICommandStruct(":TRIGger:CAN:LEVel?","SCPI_Trigger_Can","LevelQ"),//查询CAN触发时的触发电平

    private final int[] source = new int[ChannelFactory.SERIAL_CNT];
    private final int[] idle = new int[ChannelFactory.SERIAL_CNT];
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT];
    /**
     * 帧起始
     * 远程帧ID
     * 数据帧ID
     * 远程帧/数据帧ID
     * 数据帧ID和数据
     * 错误帧
     * 所有错误
     * 确认错误
     * 过载帧
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];
    private final int[] id = new int[ChannelFactory.SERIAL_CNT];
    private final int[] dlc = new int[ChannelFactory.SERIAL_CNT];
    private final long[] data = new long[ChannelFactory.SERIAL_CNT];
    private final double[] level = new double[ChannelFactory.SERIAL_CNT];

    public int getSource(int serials) {
        return source[serials];
    }

    public void setSource(int serials, int source, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.source)) return;
        if (this.source[serials] == source) return;
        this.source[serials] = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_SOURCE);
            String params = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);
            msgToUI.setParam(params);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getIdle(int serials) {
        return idle[serials];
    }

    public void setIdle(int serials, int idle, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.idle)) return;
        if (this.idle[serials] == idle) return;
        this.idle[serials] = idle;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_IDLE);
            String params = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(idle);
            msgToUI.setParam(params);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getBaudRate(int serials) {
        return baudRate[serials];
    }

    public void setBaudRate(int serials, int baudRate, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.baudRate)) return;
        if (this.baudRate[serials] == baudRate) return;
        this.baudRate[serials] = baudRate;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_BAUDRATE);
            String params = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate);
            msgToUI.setParam(params);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getType(int serials) {
        return type[serials];
    }

    public int getId(int serials) {
        return id[serials];
    }

    public int getDlc(int serials) {
        return dlc[serials];
    }

    public long getData(int serials) {
        return data[serials];
    }

    public void setType(int serials, int type, long id, int dlc, long data, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;
        if(!ToolsSCPI.isCorrect(serials,this.id)) return;
        if(!ToolsSCPI.isCorrect(serials,this.dlc)) return;
        if(!ToolsSCPI.isCorrect(serials,this.data)) return;
//        if (this.type[serials] == type && this.id[serials] == id && this.dlc[serials] == dlc && this.data[serials] == data)
//            return;

        long minId=0;
        long maxId=0x1FFFFFFF;
        if (id<minId) id=minId;
        if (id>maxId) id=maxId;

        this.type[serials] = type;
        this.id[serials] = (int)id;
        this.dlc[serials] = dlc;
        this.data[serials] = data;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_TYPE);
            String params = String.valueOf(serials)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(id)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(dlc)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(data);
            msgToUI.setParam(params);
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_LEVEL);
            String params = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level);
            msgToUI.setParam(params);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
}
