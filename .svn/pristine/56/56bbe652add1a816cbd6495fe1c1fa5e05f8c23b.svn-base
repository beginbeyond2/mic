package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_SPI {
//      new SCPICommandStruct(":TRIGger:SPI:DATA","SCPI_Trigger_SPI","Data"),//设置SPI触发下的数据值
//            new SCPICommandStruct(":TRIGger:SPI:DATA?","SCPI_Trigger_SPI","DataQ"),//查询SPI触发下的数据值
//            new SCPICommandStruct(":TRIGger:SPI:SOURce","SCPI_Trigger_SPI","Source"),//设置SPI触发的触发源
//            new SCPICommandStruct(":TRIGger:SPI:SOURce?","SCPI_Trigger_SPI","SourceQ"),//查询SPI触发的触发源
//            new SCPICommandStruct(":TRIGger:SPI:LEVel","SCPI_Trigger_SPI","Level"),//设置SPI触发时的触发电平
//            new SCPICommandStruct(":TRIGger:SPI:LEVel?","SCPI_Trigger_SPI","LevelQ"),//查询SPI触发时的触发电平


    private final int[] type = new int[ChannelFactory.SERIAL_CNT];
    private final int[] triggerMask = new int[ChannelFactory.SERIAL_CNT];
    private final int[] triggerData = new int[ChannelFactory.SERIAL_CNT];

    private final double[] levelCLK = new double[ChannelFactory.SERIAL_CNT];
    private final double[] levelData = new double[ChannelFactory.SERIAL_CNT];
    private final double[] levelCS = new double[ChannelFactory.SERIAL_CNT];

    public void Type(int s_num,int type,boolean isUpdateUI){
        this.type[s_num]=type;
        Command.get().getBus_spi().setType(s_num,type,this.triggerMask[s_num],this.triggerData[s_num],isUpdateUI);
    }
    public int TypeQ(int s_num){
        return type[s_num];
    }

    public  void Data(int s_num,String data,boolean isUpdateUI){

        this.triggerMask[s_num]= (int)SerialsUtils.toDLong(SerialsUtils.getSpiMask(data.trim()),2);
        this.triggerData[s_num]=(int)SerialsUtils.toDLong(SerialsUtils.getSpiData(data.trim()),2);
        Command.get().getBus_spi().setType(s_num,this.type[s_num],this.triggerMask[s_num],this.triggerData[s_num],isUpdateUI);
    }

    public  String  DataQ(int s_num){
        return SerialsUtils.getSpiText(this.triggerMask[s_num],this.triggerData[s_num]);
    }


    public  void LevelCLK(int s_num,double level,boolean isUpdateUI){
        this.levelCLK[s_num]=level;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCLOCK);
            msgToUI.setParam(String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  double LevelCLKQ(int s_num){
        return this.levelCLK[s_num];
    }

    public  void LevelData(int s_num,double level,boolean isUpdateUI){
        this.levelData[s_num]=level;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELDATA);
            msgToUI.setParam(String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  double LevelDataQ(int s_num){
        return this.levelData[s_num];
    }

    public  void LevelCS(int s_num,double level,boolean isUpdateUI){
        this.levelCS[s_num]=level;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCS);
            msgToUI.setParam(String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  double LevelCSQ(int s_num){
        return this.levelCS[s_num];
    }



}
