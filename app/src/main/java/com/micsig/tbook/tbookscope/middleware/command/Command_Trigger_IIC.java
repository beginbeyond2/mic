package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_IIC {
    //     new SCPICommandStruct(":TRIGger:IIC:SOURce","SCPI_Trigger_IIC","Source"),//设置IIC触发的触发源
//            new SCPICommandStruct(":TRIGger:IIC:SOURce?","SCPI_Trigger_IIC","SourceQ"),//查询IIC触发的触发源
//            new SCPICommandStruct(":TRIGger:IIC:TYPE","SCPI_Trigger_IIC","Type"),//设置IIC触发的触发类型
//            new SCPICommandStruct(":TRIGger:IIC:TYPE?","SCPI_Trigger_IIC","TypeQ"),//查询IIC触发的触发类型
//            new SCPICommandStruct(":TRIGger:IIC:ADDRess","SCPI_Trigger_IIC","Address"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，设置IIC总线触发的触发地址
//            new SCPICommandStruct(":TRIGger:IIC:ADDRess?","SCPI_Trigger_IIC","AddressQ"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，查询IIC总线触发的触发地址
//            new SCPICommandStruct(":TRIGger:IIC:RELation","SCPI_Trigger_IIC","Relation"),//当IIC触发条件为RDATa时，设置IIC总线触发的触发关系
//            new SCPICommandStruct(":TRIGger:IIC:RELation?","SCPI_Trigger_IIC","RelationQ"),//当IIC触发条件为RDATa时，查询IIC总线触发的触发关系
//            new SCPICommandStruct(":TRIGger:IIC:DATA","SCPI_Trigger_IIC","Data"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，设置IIC总线触发的触发数据
//            new SCPICommandStruct(":TRIGger:IIC:DATA?","SCPI_Trigger_IIC","DataQ"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，查询IIC总线触发的触发数据
//            new SCPICommandStruct(":TRIGger:IIC:LEVel","SCPI_Trigger_IIC","Level"),//设置IIC触发时的触发电平
//            new SCPICommandStruct(":TRIGger:IIC:LEVel?","SCPI_Trigger_IIC","LevelQ"),//查询IIC触发时的触发电平
    private final int[] source = new int[ChannelFactory.SERIAL_CNT];
    private final int[] clock = new int[ChannelFactory.SERIAL_CNT];
    /**
     * 启动条件
     * 停止条件
     * 确认丢失
     * 重新启动
     * 地址字段无确认
     * 帧型1
     * 帧型2
     * EEPROM数据读写
     * 10位写帧
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];
    private final int[] addr = new int[ChannelFactory.SERIAL_CNT];
    private final int[] data1 = new int[ChannelFactory.SERIAL_CNT];
    private final int[] data2 = new int[ChannelFactory.SERIAL_CNT];
    private final int[] condition = new int[ChannelFactory.SERIAL_CNT];
    private final double[] levelData = new double[ChannelFactory.SERIAL_CNT];
    private final double[] levelClock = new double[ChannelFactory.SERIAL_CNT];

    public int getSource(int serials) {
        return source[serials];
    }

    public void setSource(int serials, int source, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.source)) return;
        if (this.source[serials] == source) return;
        this.source[serials] = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_SOURCE);
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getClock(int serials) {
        return clock[serials];
    }

    public void setClock(int serials, int clock, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.clock)) return;
        if (this.clock[serials] == clock) return;
        this.clock[serials] = clock;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_CLOCK);
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(clock);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getType(int serials) {
        return type[serials];
    }

    public int getAddr(int serials) {
        return addr[serials];
    }

    public int getData1(int serials) {
        return data1[serials];
    }

    public int getData2(int serials) {
        return data2[serials];
    }

    public int getCondition(int serials) {
        return condition[serials];
    }

    public void setType(int serials, int type, int addr, int data1, int data2, int condition, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;
        if(!ToolsSCPI.isCorrect(serials,this.addr)) return;
        if(!ToolsSCPI.isCorrect(serials,this.data1)) return;
        if(!ToolsSCPI.isCorrect(serials,this.data2)) return;
        if(!ToolsSCPI.isCorrect(serials,this.condition)) return;
//        if (this.type[serials] == type && this.addr[serials] == addr && this.data1[serials] == data1
//                && this.data2[serials] == data2 && this.condition[serials] == condition)
//            return;
        this.type[serials] = type;
        this.addr[serials] = addr;
        this.data1[serials] = data1;
        this.data2[serials] = data2;
        this.condition[serials] = condition;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_TYPE);
            String param = String.valueOf(serials) +
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(type) +
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(addr) +
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(data1) +
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(data2) +
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(condition);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevelData(int serials) {
        return levelData[serials];
    }

    public void setLevelData(int serials, double levelData, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.levelData)) return;
//        if (this.levelData[serials] == levelData) return;
        this.levelData[serials] = levelData;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_LEVELDATA);
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(levelData);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevelClock(int serials) {
        return levelClock[serials];
    }

    public void setLevelClock(int serials, double levelClock, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.levelClock)) return;
//        if (this.levelClock[serials] == levelClock) return;
        this.levelClock[serials] = levelClock;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_LEVELCLOCK);
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(levelClock);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
}
