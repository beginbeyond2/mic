package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:13
 */
public class Command_Bus_1553B {
//            new SCPICommandStruct(":BUS#:1553B:CHANnel","SCPI_Bus_1553B","Channel"),
//            new SCPICommandStruct(":BUS#:1553B:CHANnel?","SCPI_Bus_1553B","ChannelQ"),
//            new SCPICommandStruct(":BUS#:1553B:DISPlay","SCPI_Bus_1553B","Display"),
//            new SCPICommandStruct(":BUS#:1553B:DISPlay?","SCPI_Bus_1553B","DisplayQ"),

    private final int[] ch=new int[ChannelFactory.SERIAL_CNT];
    private final int[] display=new int[ChannelFactory.SERIAL_CNT];

    public  void Channel(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_1553B)return;
        boolean b = ToolsSCPI.isCorrect(s_num, this.ch);
        if (b){
            this.ch[s_num]=source;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_1553B_Channel);
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

    public void Display(int s_num,int display,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_1553B)return;
        boolean b= ToolsSCPI.isCorrect(s_num,this.display);
        if (b){
            this.display[s_num]=display;
            if (isUpdateUI){
                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_1553B_Display);
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
