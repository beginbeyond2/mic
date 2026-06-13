package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Uart {

//     new SCPICommandStruct(":TRIGger:UART:SOURce","SCPI_Trigger_Uart","Source"),//设置UART触发的触发源
//     new SCPICommandStruct(":TRIGger:UART:SOURce?","SCPI_Trigger_Uart","SourceQ"),//查询UART触发的触发源
//     new SCPICommandStruct(":TRIGger:UART:TYPE","SCPI_Trigger_Uart","Type"),//设置UART触发的触发条件
//     new SCPICommandStruct(":TRIGger:UART:TYPE?","SCPI_Trigger_Uart","TypeQ"),//查询UART触发的触发条件
//     new SCPICommandStruct(":TRIGger:UART:RELation","SCPI_Trigger_Uart","Relation"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发关系
//     new SCPICommandStruct(":TRIGger:UART:RELation?","SCPI_Trigger_Uart","RelationQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发关系
//     new SCPICommandStruct(":TRIGger:UART:DATA","SCPI_Trigger_Uart","Data"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发数据。
//     new SCPICommandStruct(":TRIGger:UART:DATA?","SCPI_Trigger_Uart","DataQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发数据。
//     new SCPICommandStruct(":TRIGger:UART:LEVel","SCPI_Trigger_Uart","Level"),//设置UART触发时的触发电平
//     new SCPICommandStruct(":TRIGger:UART:LEVel?","SCPI_Trigger_Uart","LevelQ"),//查询UART触发时的触发电平

    private final int[] source = new int[ChannelFactory.SERIAL_CNT];
    private final int[] idle = new int[ChannelFactory.SERIAL_CNT];
    private final int[] check = new int[ChannelFactory.SERIAL_CNT];
    private final int[] bits = new int[ChannelFactory.SERIAL_CNT];
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT];
    private final int[] display = new int[ChannelFactory.SERIAL_CNT];
    private final double[] level = new double[ChannelFactory.SERIAL_CNT];
    /**
     * 开始位
     * 停止位
     * 数据
     * [0:数据]
     * [1:数据]
     * [x:数据]
     * 奇偶检验错误
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];
    private final int[] condition = new int[ChannelFactory.SERIAL_CNT];
    private final int[] number = new int[ChannelFactory.SERIAL_CNT];

    public int getSource(int serials) {
        return source[serials];
    }

    public void setSource(int serials, int source, boolean isUpdateUI) {
        if (!ToolsSCPI.isCorrect(serials, this.source)) return;
        if (this.source[serials] == source) return;
        this.source[serials] = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_SOURCE);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source));
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_IDLE);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(idle));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getCheck(int serials) {
        return check[serials];
    }

    public void setCheck(int serials, int check, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.check)) return;
        if (this.check[serials] == check) return;
        this.check[serials] = check;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_CHECK);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(check));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getBits(int serials) {
        return bits[serials];
    }

    public void setBits(int serials, int bits, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.bits)) return;
        if (this.bits[serials] == bits) return;
        this.bits[serials] = bits;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_BITS);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(bits));
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_BAUDRATE);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getDisplay(int serials) {
        return display[serials];
    }

    public void setDisplay(int serials, int display, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.display)) return;
        if (this.display[serials] == display) return;
        this.display[serials] = display;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_DISPLAY);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevel(int serials) {
        return level[serials];
    }

    public void setLevel(int serials, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.level)) return;
        //if (this.level[serials] == level) return;
        this.level[serials] = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_LEVEL);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public void setType(int serials, int type, int condition, int number, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;
        if(!ToolsSCPI.isCorrect(serials,this.condition)) return;
        if(!ToolsSCPI.isCorrect(serials,this.number)) return;
        if (this.type[serials] == type && this.condition[serials] == condition && this.number[serials] == number) return;
        this.type[serials] = type;
        this.condition[serials] = condition;
        this.number[serials] = number;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_TYPE);
            msgToUI.setParam(String.valueOf(serials)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(condition)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(number));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getType(int serials) {
        return type[serials];
    }

    public int getCondition(int serials) {
        return condition[serials];
    }

    public int getNumber(int serials) {
        return number[serials];
    }
}
