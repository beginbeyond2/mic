package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:15
 */
public class Command_Bus_429 {
//    new SCPICommandStruct(":BUS#:429:SOURce","SCPI_Bus_429","Source"),
//            new SCPICommandStruct(":BUS#:429:SOURce?","SCPI_Bus_429","SourceQ"),
//            new SCPICommandStruct(":BUS#:429:FORMat","SCPI_Bus_429","Format"),
//            new SCPICommandStruct(":BUS#:429:FORMat?","SCPI_Bus_429","FormatQ"),
//            new SCPICommandStruct(":BUS#:429:DISPlay","SCPI_Bus_429","Display"),
//            new SCPICommandStruct(":BUS#:429:DISPlay?","SCPI_Bus_429","DisplayQ"),
//            new SCPICommandStruct(":BUS#:429:BANDrate","SCPI_Bus_429","BandRate"),
//            new SCPICommandStruct(":BUS#:429:BANDrate?","SCPI_Bus_429","BandRateQ"),


    private final int[] ch = new int[ChannelFactory.SERIAL_CNT];
    private final int[] format = new int[ChannelFactory.SERIAL_CNT];
    private final int[] display = new int[ChannelFactory.SERIAL_CNT];
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT];

    public  void Source(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_429)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch);
        if (b){
            this.ch[s_num]=source;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_429_Channel);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }

    public  int SourceQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch);
        if (b){
            return this.ch[s_num];
        }
        return this.ch[0];
    }

    public  void Format(int s_num,int format,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_429)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.format);
        if (b){
            this.format[s_num]=format;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_429_Format);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(format);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }

    public  int FormatQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.format);
        if (b){
            return this.format[s_num];
        }
        return this.format[0];
    }

    public  void Display(int s_num,int display ,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_429)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.display);
        if (b){
            this.display[s_num]=display;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_429_display);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int DisplayQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.display);
        if (b){
            return this.display[s_num];
        }
        return this.display[0];
    }
    public  void BaudRate(int s_num,int baudRate,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_429)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.baudRate);
        if (b){
            this.baudRate[s_num]=baudRate;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_429_Baudrate);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int BaudRateQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.baudRate);
        if (b){
            return this.baudRate[s_num];
        }
        return this.baudRate[0];
    }

}
