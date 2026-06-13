package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:11
 */
public class Command_Bus_IIC {
//            new SCPICommandStruct(":BUS#:IIC:SDA","SCPI_Bus_IIC","SDA"),
//            new SCPICommandStruct(":BUS#:IIC:SDA?","SCPI_Bus_IIC","SDAQ"),
//            new SCPICommandStruct(":BUS#:IIC:SCL","SCPI_Bus_IIC","SCL"),
//            new SCPICommandStruct(":BUS#:IIC:SCL?","SCPI_Bus_IIC","SCLQ"),

    private final int[] sda=new int[ChannelFactory.SERIAL_CNT];
    private final int[] scl=new int[ChannelFactory.SERIAL_CNT];

    public  void SDA(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_IIC)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.sda);
        if (b){
            this.sda[s_num]=source;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_IIC_SDA);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int SDAQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.sda);
        if (b){
            return this.sda[s_num];
        }
        return this.sda[0];
    }
    public  void SCL(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_IIC)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.scl);
        if (b){
            this.scl[s_num]=source;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_IIC_SCL);
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);
                msgToUI.setParam(param);
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            }
        }
    }
    public  int SCLQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.scl);
        if (b){
            return this.scl[s_num];
        }
        return this.scl[0];
    }

}
