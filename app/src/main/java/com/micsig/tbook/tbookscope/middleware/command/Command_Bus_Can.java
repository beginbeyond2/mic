package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:07
 */
public class Command_Bus_Can {
//            new SCPICommandStruct(":BUS#:CAN:CHANnel","SCPI_Bus_Can","Channel"),
//            new SCPICommandStruct(":BUS#:CAN:CHANnel?","SCPI_Bus_Can","ChannelQ"),
//            new SCPICommandStruct(":BUS#:CAN:IDLElvl","SCPI_Bus_Can","IdLevel"),
//            new SCPICommandStruct(":BUS#:CAN:IDLElvl?","SCPI_Bus_Can","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:CAN:BAUDrate","SCPI_Bus_Can","BaudRate"),
//            new SCPICommandStruct(":BUS#:CAN:BAUDrate?","SCPI_Bus_Can","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:CAN:USERbaud","SCPI_Bus_Can","UserBaud"),
//            new SCPICommandStruct(":BUS#:CAN:USERbaud?","SCPI_Bus_Can","UserBaudQ"),
//
//            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint","SCPI_Bus_Can","SAMPlepoint"),
//            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint?","SCPI_Bus_Can","SAMPlepointQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDBAudrate","SCPI_Bus_Can","FDBAudrate"),
//            new SCPICommandStruct(":BUS#:CAN:FDBAudrate?","SCPI_Bus_Can","FDBAudrateQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud","SCPI_Bus_Can","FDUSerbaud"),
//            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud?","SCPI_Bus_Can","FDUSerbaudQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint","SCPI_Bus_Can","FDSAmplepoint"),
//            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint?","SCPI_Bus_Can","FDSAmplepointQ"),

    private final int[] ch =new int[ChannelFactory.SERIAL_CNT];
    private final int[] signal =new int[ChannelFactory.SERIAL_CNT];
    private final int[] baudRate =new int[ChannelFactory.SERIAL_CNT];
    private final int[] userBaud =new int[ChannelFactory.SERIAL_CNT];
    private final double[] samplePoint =new double[ChannelFactory.SERIAL_CNT];
    private final int[] FDBaudrate =new int[ChannelFactory.SERIAL_CNT];
    private final int[] FDUserBaudrate =new int[ChannelFactory.SERIAL_CNT];
    private final double[] FDSamplePoint =new double[ChannelFactory.SERIAL_CNT];
    private final int[] iso =new int[ChannelFactory.SERIAL_CNT];

    private final int minBaud=10000;
    private final int maxBaud=1000000;

    private final int FDminBaud=1000000;
    private final int FDmaxBaud=12000000;

    public  void Channel(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_CAN)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch);
        if (b){
            this.ch[s_num]=source;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_Channel);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }

    public  int ChannelQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch);
        if (b){
            return this.ch[s_num];
        }
        return this.ch[0];
    }

    public  void Signal(int s_num,int signal,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_CAN)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch);
        if (b){
            this.signal[s_num]=signal;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_Signal);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(signal);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }

    public  int SignalQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.signal);
        if (b){
            return this.signal[s_num];
        }
        return this.signal[0];
    }

    public  void BaudRate(int s_num,int baudRate,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_CAN)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.baudRate);
        if (b){
            this.baudRate[s_num]=baudRate;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_BaudRate);
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

    public  void UserBaud(int s_num,int userBaud,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_CAN)return;
        if (userBaud!=-1){
            if (userBaud<minBaud) userBaud=minBaud;
            if (userBaud>maxBaud) userBaud=maxBaud;
        }
        boolean b= ToolsSCPI.isCorrect(s_num,this.userBaud);
        if (b){
            this.userBaud[s_num]=userBaud;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_UserBaud);
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


    public  void   SAMPlepoint(int sNum,double percent,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return;
        if (percent<1) percent=1;
        if (percent>=100) percent=99.9;
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud);
        if (b==false) return;
        this.samplePoint[sNum]=percent;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_SamplePoint);
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(percent);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  double   SAMPlepointQ(int sNum){
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud);
        if (b==false) return this.samplePoint[0];
        return this.samplePoint[sNum];
    }
    public  void   FDBAudrate(int sNum,int baudrateIndex,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return;
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud);
        if (b==false) return;
        this.FDBaudrate[sNum]=baudrateIndex;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_FDBaudrate);
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudrateIndex);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  int   FDBAudrateQ(int sNum){
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud);
        if (b==false) return this.FDBaudrate[0];
        return this.FDBaudrate[sNum];
    }
    public  void   FDUSerbaud(int sNum,int baudrate,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return;
        if (baudrate!=-1){
            if (baudrate<FDminBaud) baudrate=FDminBaud;
            if (baudrate>FDmaxBaud) baudrate=FDmaxBaud;
        }

        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud);
        if (b==false) return;
        this.FDUserBaudrate[sNum]=baudrate;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_FDUserBaud);
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudrate);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int FDUSerbaudQ(int sNum) {
        if (!ToolsSCPI.isCorrect(sNum, this.userBaud)) {
            return this.FDUserBaudrate[0];
        } else {
            return this.FDUserBaudrate[sNum];
        }
    }

    public void FDSAmplepoint(int sNum, double percent, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return;
        if (percent<1) percent=1;
        if (percent>=100) percent=99.9;
        if (!ToolsSCPI.isCorrect(sNum,this.userBaud)) return;
        if (!ToolsSCPI.isCorrect(sNum,this.FDSamplePoint)) return;
        this.FDSamplePoint[sNum] = percent;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_FDSamplePoint);
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(percent);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  double   FDSAmplepointQ(int sNum){
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud);
        if (b==false) return this.FDSamplePoint[0];
        return this.FDSamplePoint[sNum];
    }

    public void ISO(int sNum,int index,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return;
        boolean b= ToolsSCPI.isCorrect(sNum,this.iso);
        if (b==false) return;
        this.iso[sNum]=index;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_ISO);
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int ISOQ(int sNum){
        boolean b= ToolsSCPI.isCorrect(sNum,this.iso);
        if (b==false) return this.iso[0];
        return this.iso[sNum];
    }


}
