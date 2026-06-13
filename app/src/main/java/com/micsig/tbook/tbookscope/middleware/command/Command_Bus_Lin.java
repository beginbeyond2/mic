package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:55
 */
public class Command_Bus_Lin {
//     new SCPICommandStruct(":BUS#:LIN:CHANnel","SCPI_Bus_Lin","Channel"),
//            new SCPICommandStruct(":BUS#:LIN:CHANnel?","SCPI_Bus_Lin","ChannelQ"),
//            new SCPICommandStruct(":BUS#:LIN:IDLElvl","SCPI_Bus_Lin","IdLevel"),
//            new SCPICommandStruct(":BUS#:LIN:IDLElvl?","SCPI_Bus_Lin","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:LIN:BAUDrate","SCPI_Bus_Lin","BaudRate"),
//            new SCPICommandStruct(":BUS#:LIN:BAUDrate?","SCPI_Bus_Lin","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:LIN:USERbaud","SCPI_Bus_Lin","UserBaud"),
//            new SCPICommandStruct(":BUS#:LIN:USERbaud?","SCPI_Bus_Lin","UserBaudQ"),

    private final int[] src=new int[ChannelFactory.SERIAL_CNT];
    private final int[] idLevel=new int[ChannelFactory.SERIAL_CNT];
    private final int[] baudRate=new int[ChannelFactory.SERIAL_CNT];
    private final int[] userBaud=new int[ChannelFactory.SERIAL_CNT];
    private final int[] linType=new int[ChannelFactory.SERIAL_CNT];

    private final int minBaud=2400;
    private final int maxBaud=625000;

    public void LinType(int s_num,int linType,boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return;
        boolean b = ToolsSCPI.isCorrect(s_num, this.linType);
        if (b){
            this.linType[s_num]=linType;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_TYPE);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(linType);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }

    }

    public int LinTypeQ(int s_num) {
        boolean b = ToolsSCPI.isCorrect(s_num, this.linType);
        if (b) {
            return this.linType[s_num];
        }
        return this.linType[0];
    }

    public  void Channel(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.src);
        if (b){
            this.src[s_num]=source;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_Channel);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int ChannelQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.src);
        if (b){
            return this.src[s_num];
        }
        return this.src[0];
    }
    public  void IdLevel(int s_num,int id_level,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.idLevel);
        if (b){
            this.idLevel[s_num]=id_level;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_IdLevel);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(id_level);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int IdLevelQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.idLevel);
        if (b){
            return this.idLevel[s_num];
        }
        return this.idLevel[0];
    }
    public  void BaudRate(int s_num,int baudRateIndex,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.baudRate);
        if (b){
            this.baudRate[s_num]=baudRateIndex;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_BaudRate);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRateIndex);
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
    public  void UserBaud(int s_num,int userBaud,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return;
        if (userBaud!=-1) {
            if (userBaud < minBaud) userBaud = minBaud;
            if (userBaud > maxBaud) userBaud = maxBaud;
        }
        boolean b= ToolsSCPI.isCorrect(s_num,this.userBaud);
        if (b){
            this.userBaud[s_num]=userBaud;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_Userbaud);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(userBaud);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int UserBaudQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.userBaud);
        if (b){
            return this.userBaud[s_num];
        }
        return this.userBaud[0];
    }

}
