package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:40
 */
public class Command_Bus_Uart {
//    new SCPICommandStruct(":BUS#:UART:RX","SCPI_Bus_Uart","Rx"),
//            new SCPICommandStruct(":BUS#:UART:RX?","SCPI_Bus_Uart","RxQ"),
//            new SCPICommandStruct(":BUS#:UART:IDLElvl","SCPI_Bus_Uart","IdLevel"),
//            new SCPICommandStruct(":BUS#:UART:IDLElvl?","SCPI_Bus_Uart","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:UART:BAUDrate","SCPI_Bus_Uart","BaudRate"),
//            new SCPICommandStruct(":BUS#:UART:BAUDrate?","SCPI_Bus_Uart","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:UART:CHECK","SCPI_Bus_Uart","Check"),
//            new SCPICommandStruct(":BUS#:UART:CHECK?","SCPI_Bus_Uart","CheckQ"),
//            new SCPICommandStruct(":BUS#:UART:USERbaud","SCPI_Bus_Uart","UserBaud"),
//            new SCPICommandStruct(":BUS#:UART:USERbaud?","SCPI_Bus_Uart","UserBaudQ"),
//            new SCPICommandStruct(":BUS#:UART:WIDTh","SCPI_Bus_Uart","Width"),
//            new SCPICommandStruct(":BUS#:UART:WIDTh?","SCPI_Bus_Uart","WidthQ"),

    private final int[] rx = new int[ChannelFactory.SERIAL_CNT];
    private final int[] idLevel = new int[ChannelFactory.SERIAL_CNT];
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT];
    private final int[] check = new int[ChannelFactory.SERIAL_CNT];
    private final int[] userBand = new int[ChannelFactory.SERIAL_CNT];
    private final int[] width = new int[ChannelFactory.SERIAL_CNT];
    private final int[] display = new int[ChannelFactory.SERIAL_CNT];

    private final int minBaund=1200;
    private final int maxBaund=8000000;


    public  void Rx(int s_num,int rx,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.rx);
        if (b){
            this.rx[s_num]=rx;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_Rx);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(rx);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int RxQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.rx);
        if (b){
            return this.rx[s_num];
        }
        return this.rx[0];
    }
    public  void IdLevel(int s_num,int idLevel,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.idLevel);
        if (b){
            this.idLevel[s_num]=idLevel;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_IdLevel);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(idLevel);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int IdLevelQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.rx);
        if (b){
            return this.idLevel[s_num];
        }
        return this.idLevel[0];
    }
    public  void BaudRate(int s_num,int baudRate,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return;
        if (baudRate<minBaund) baudRate= minBaund;
        if (baudRate>maxBaund) baudRate=maxBaund;

        boolean b=ToolsSCPI.isCorrect(s_num,this.baudRate);
        if (b){
            this.baudRate[s_num]=baudRate;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_BaudRate);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int BaudRateQ(int s_num){
        boolean b=ToolsSCPI.isCorrect(s_num,this.baudRate);
        if (b){
            return this.baudRate[s_num];
        }
        return this.baudRate[0];
    }
    public  void Check(int s_num,int check,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return;
        boolean b=ToolsSCPI.isCorrect(s_num,this.check);
        if (b){
            this.check[s_num]=check;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_Check);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(check);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public int CheckQ(int s_num){
        boolean b=ToolsSCPI.isCorrect(s_num,this.check);
        if (b){
            return this.check[s_num];
        }
        return this.check[0];
    }
    public  void UserBaud(int s_num,int userBaud,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return;
        if (userBaud<minBaund) userBaud= minBaund;
        if (userBaud>maxBaund) userBaud=maxBaund;

        boolean b=ToolsSCPI.isCorrect(s_num,this.userBand);
        if (b){
            this.userBand[s_num]=userBaud;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_UserBaud);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(userBaud);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int UserBaudQ(int s_num){
        boolean b=ToolsSCPI.isCorrect(s_num,this.userBand);
        if (b){
            return this.userBand[s_num];
        }
        return this.userBand[0];
    }
    public  void Width(int s_num,int width,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return;
        boolean b=ToolsSCPI.isCorrect(s_num,this.width);
        if (b){
            this.width[s_num]=width;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_Width);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(width);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int WidthQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.width);
        if (b){
            return this.width[s_num];
        }
        return this.width[0];
    }

    public void Display(int s_num,int display,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return;
        boolean b=ToolsSCPI.isCorrect(s_num,this.display);
        if (b){
            this.display[s_num]=display;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_Display);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }

    }
    public int DisplayQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.display);
        if (b){
            return this.display[s_num];
        }
        return this.display[0];
    }

}
