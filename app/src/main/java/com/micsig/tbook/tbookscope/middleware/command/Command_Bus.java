package com.micsig.tbook.tbookscope.middleware.command;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBus;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusStruct;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:39
 */
public class Command_Bus {
//            new SCPICommandStruct(":BUS#:DISPlay","SCPI_Bus","Display"),
//            new SCPICommandStruct(":BUS#:DISPlay?","SCPI_Bus","DisplayQ"), //
//            new SCPICommandStruct(":BUS#:TYPE","SCPI_Bus","Type"),
//            new SCPICommandStruct(":BUS#:TYPE?","SCPI_Bus","TypeQ"),
//            new SCPICommandStruct(":BUS#:MODE","SCPI_Bus","Mode"),
//            new SCPICommandStruct(":BUS#:MODE?","SCPI_Bus","ModeQ"),
//            new SCPICommandStruct(":BUS#:LEVel","SCPI_Bus","Level"),
//            new SCPICommandStruct(":BUS#:LEVel?","SCPI_Bus","LevelQ"),
//            new SCPICommandStruct(":BUS#:HLEVel","SCPI_Bus","HLevel"),
//            new SCPICommandStruct(":BUS#:HLEVel?","SCPI_Bus","HLevelQ"),
//            new SCPICommandStruct(":BUS#:LLEVel","SCPI_Bus","LLevel"),
//            new SCPICommandStruct(":BUS#:LLEVel?","SCPI_Bus","LLevelQ"),

    public static final int  Type_UART=0;
    public static final int  Type_LIN=1;
    public static final int  Type_CAN=2;
    public static final int  Type_SPI=3;
    public static final int  Type_IIC=4;
    public static final int  Type_429=5;
    public static final int  Type_1553B=6;

    private int[] type=new int[ChannelFactory.SERIAL_CNT];
    private double[][] level=new double[ChannelFactory.SERIAL_CNT][ChannelFactory.CH_CNT];


    public void Display(int ch,boolean isOpen,boolean isUpdateUI){
        Command.get().getChannel().Display(ch,isOpen,isUpdateUI);
    }
    public String DisplayQ(int ch){
      boolean  b= Command.get().getChannel().DisplayQ(ch);
      return ToolsSCPI.getOpenState(b);
    }

    public  void Type(int ch,int type,boolean isUpdateUI){
        this.type[ch]=type;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Type);
            String param = String.valueOf(ch) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  int TypeQ(int ch){
        return type[ch];
    }
    public  void Mode(int sNum,int mode,boolean isUpdateUI){
//        this.mode[sNum]=mode;
//        if (isUpdateUI){
//            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
//            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Mode);
//            String param = String.valueOf(mode);
//            msgToUI.setParam(param);
//            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
//        }
        Command.get().getTrigger().SerialBus_Type(mode,isUpdateUI);
    }
    public int ModeQ(int sNum){
//        return mode[sNum];
        return Command.get().getTrigger().SerialBus_TypeQ();
    }
    public  void Level(int sNum,int ch,double level,boolean isUpdateUI){
        if (sNum != 0 && sNum != 1) return;
        if (!isCorrect(sNum, ch)) return;

        this.level[sNum][ch]=level;
        if (isUpdateUI) {
            post(sNum,ch,level);
        }
    }
    public  double LevelQ(int sNum,int ch){
        return getLevel(sNum,ch);
    }

    public void HLevel(int sNum,int ch,double level,boolean isUpdateUI){
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1);
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType();
        if (type!=SerialBusStruct.SerialBusType_429) return;
        Command.get().getTrigger_m429().setLevelHigh(sNum,ch,level,isUpdateUI);
    }
    public double HLevelQ(int sNum,int ch){
        return Command.get().getTrigger_m429().getLevelHigh(sNum);
    }
    public void LLevel(int sNum,int ch,double level,boolean isUpdateUI){
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1);
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType();
        if (type!=SerialBusStruct.SerialBusType_429) return;
        Command.get().getTrigger_m429().setLevelLow(sNum,ch,level,isUpdateUI);
    }
    public double LLevelQ(int sNum,int ch){
        return Command.get().getTrigger_m429().getLevelLow(sNum);
    }

    private boolean isCorrect(int sNum,int ch){
        boolean result=false;
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1);
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType();

        switch (type){
            case SerialBusStruct.SerialBusType_UART:{
                int channel= Command.get().getBus_uart().RxQ(sNum);
                if (ch==channel){
                    result= true;
                }
            }break;
            case SerialBusStruct.SerialBusType_LIN:{
                int channel= Command.get().getBus_lin().ChannelQ(sNum);
                if (ch==channel){
                    result= true;
                }
            }break;
            case SerialBusStruct.SerialBusType_CAN:{
                int channel=Command.get().getBus_can().ChannelQ(sNum);
                if (ch==channel){
                    result= true;
                }
            }break;
            case SerialBusStruct.SerialBusType_SPI:{
                int ch_clk=Command.get().getBus_spi().getClock(sNum);
                int ch_data=Command.get().getBus_spi().getData(sNum);
                int ch_cs= Command.get().getBus_spi().getCs(sNum);
                if (ch==ch_clk || ch==ch_data || ch==ch_cs){
                    result= true;
                }
            }break;
            case SerialBusStruct.SerialBusType_I2C:{
                int cl_clk=Command.get().getBus_iic().SCLQ(sNum);
                int cl_data=Command.get().getBus_iic().SDAQ(sNum);
                if (ch==cl_clk || ch==cl_data){
                    result =true;
                }
            }break;
            case SerialBusStruct.SerialBusType_429:{
                //int ch_h= Command.get().getBus_429().SourceQ(sNum);
                if (ch==0 || ch==1){
                    result =true;
                }
            }break;
            case SerialBusStruct.SerialBusType_1553B:{
                int channel=Command.get().getBus_1553B().ChannelQ(sNum);
                if (channel==ch){
                    result=true;
                }
            }break;
        }

        return result;
    }

    private void post(int sNum,int ch,double level){
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1);
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType();
        CommandMsgToUI msgToUI = Command.get().getMsgToUI();
        msgToUI.setParam(String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level)+CommandMsgToUI.PARAM_SPLIT+String.valueOf(ch));
        switch (type){
            case SerialBusStruct.SerialBusType_UART:{
                msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_LEVEL);
            }break;
            case SerialBusStruct.SerialBusType_LIN:{
                msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_LEVEL);
            }break;
            case SerialBusStruct.SerialBusType_CAN:{
                msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_LEVEL);
            }break;
            case SerialBusStruct.SerialBusType_SPI:{
                int ch_clk=Command.get().getBus_spi().getClock(sNum);
                int ch_data=Command.get().getBus_spi().getData(sNum);
                int ch_cs= Command.get().getBus_spi().getCs(sNum);
                if (ch==ch_clk) {
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCLOCK);
                }else if (ch==ch_data) {
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELDATA);
                }else if (ch==ch_cs) {
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCS);
                }
            }break;
            case SerialBusStruct.SerialBusType_I2C:{
                int cl_clk=Command.get().getBus_iic().SCLQ(sNum);
                int cl_data=Command.get().getBus_iic().SDAQ(sNum);
                if (ch==cl_clk) {
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_LEVELCLOCK);
                }else if (ch==cl_data) {
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_LEVELDATA);
                }
            }break;
            case SerialBusStruct.SerialBusType_429:{
                if (ch==0) {
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_LEVELHIGH);
                }else if (ch==1) {
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_LEVELLOW);
                }
            }break;
            case SerialBusStruct.SerialBusType_1553B:{
                msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_LEVEL);
            }break;
        }
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);

    }

    public static boolean isChannelExistOtherSerialNum(int curSNum,int ch){
        int otherSNum=curSNum==0?1:0;
        int otherType=Command.get().getBus().TypeQ(otherSNum);
        boolean result=false;
        Logger.i(Command.TAG,"otherNum:"+otherSNum+",otherType:"+otherType);

        int otherCh= otherSNum+ TChan.R8;
        boolean  b= Command.get().getChannel().DisplayQ(otherCh);
        if (!b){
            return b;
        }

        switch (otherType){
            case SerialBusStruct.SerialBusType_UART:{
                 int source =Command.get().getTrigger_uart().getSource(otherSNum);
                 result=ch==source;
            }break;
            case SerialBusStruct.SerialBusType_LIN:{
                int source=Command.get().getTrigger_lin().getSource(otherSNum);
                result=ch==source;
            }break;
            case SerialBusStruct.SerialBusType_CAN:{
                int source=Command.get().getTrigger_can().getSource(otherSNum);
                result=ch==source;
            }break;
            case SerialBusStruct.SerialBusType_SPI:{
                int ch_clk=Command.get().getBus_spi().getClock(otherSNum);
                int ch_data=Command.get().getBus_spi().getData(otherSNum);
                int ch_cs= Command.get().getBus_spi().getCs(otherSNum);
                result=(ch==ch_clk || ch==ch_data || ch==ch_cs) ;
            }break;
            case SerialBusStruct.SerialBusType_I2C:{
                int cl_clk=Command.get().getBus_iic().SCLQ(otherSNum);
                int cl_data=Command.get().getBus_iic().SDAQ(otherSNum);
                result = (ch== cl_clk || ch==cl_data);
            }break;
            case SerialBusStruct.SerialBusType_429:{
                int source= Command.get().getTrigger_m429().getSource(otherSNum);
                result=ch==source;
            }break;
            case SerialBusStruct.SerialBusType_1553B:{
                int source =Command.get().getTrigger_m1553B().getSource(otherSNum);
                result=ch==source;
            }break;
        }
        Logger.i(Command.TAG,"result:"+result);
        return result;
    }

    private double getLevel(int sNum,int ch){
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1);
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType();
        double level=0;

        switch (type){
            case SerialBusStruct.SerialBusType_UART:{
               level=Command.get().getTrigger_uart().getLevel(sNum);
            }break;
            case SerialBusStruct.SerialBusType_LIN:{
                level=Command.get().getTrigger_lin().getLevel(sNum);
            }break;
            case SerialBusStruct.SerialBusType_CAN:{
                level=Command.get().getTrigger_can().getLevel(sNum);
            }break;
            case SerialBusStruct.SerialBusType_SPI:{
                int ch_clk=Command.get().getBus_spi().getClock(sNum);
                int ch_data=Command.get().getBus_spi().getData(sNum);
                int ch_cs= Command.get().getBus_spi().getCs(sNum);
                if (ch==ch_clk) {
                    level= Command.get().getTrigger_spi().LevelCLKQ(sNum);
                }else if (ch==ch_data) {
                    level=Command.get().getTrigger_spi().LevelDataQ(sNum);
                }else if (ch==ch_cs) {
                    level=Command.get().getTrigger_spi().LevelCSQ(sNum);
                }
            }break;
            case SerialBusStruct.SerialBusType_I2C:{
                int cl_clk=Command.get().getBus_iic().SCLQ(sNum);
                int cl_data=Command.get().getBus_iic().SDAQ(sNum);
                if (ch==cl_clk) {
                    Command.get().getTrigger_iic().getLevelClock(sNum);
                }else if (ch==cl_data) {
                    Command.get().getTrigger_iic().getLevelData(sNum);
                }
            }break;
            case SerialBusStruct.SerialBusType_429:{
                if (ch==0) {
                    level= Command.get().getTrigger_m429().getLevelHigh(sNum);
                }else if (ch==1) {
                    level= Command.get().getTrigger_m429().getLevelLow(sNum);
                }
            }break;
            case SerialBusStruct.SerialBusType_1553B:{
                level=Command.get().getTrigger_m1553B().getLevel(sNum);
            }break;
        }
        return level;
    }

    public String DataQ(int sumNo){
        return getSerialBusData(sumNo);
    }
    //region  总线数据
    /**
     * 返回总线数据
     * @param chIdx
     * @return
     */
    private static String getSerialBusData(int chIdx){
        int type= Command.get().getTrigger().SerialBus_TypeQ();
        if (type== Command_Trigger.SerialBusType_TXT){
            return getSerialTxtBuffer(chIdx);
        }else {
            return getSerialImgBuffer(TChan.toUiChNo(chIdx));
        }
    }

    private static String getSerialTxtBuffer(int chIdx){
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(chIdx);
        if (serialChannel == null) return "Error";
        int type = serialChannel.getBusType();

        LinkedBlockingQueue<SerialBusTxtStruct.ISerialBusTxtCSV> list= SerialBusManage.getInstance().getSerialTxtBufferQueue(TChan.toUiChNo(chIdx),type,true);
        if (list==null) return "Error";
        Log.d(Command.TAG, "getSerialTxtBuffer list size: "+list.size());
        //返回记录条数
        int resultRecordCount=1000;

        int i=0;
        StringBuilder sb=new StringBuilder();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            SerialBusTxtStruct.ISerialBusTxtCSV uart = (SerialBusTxtStruct.ISerialBusTxtCSV) iter.next();
            i++;
            if ((list.size()-i)<resultRecordCount) {
                if (sb.length() == 0) {
                    sb.append(uart.toCSVHead() + ";");
                }
                sb.append(uart.toCSV() + ";");
            }
        }
        Log.d(Command.TAG, "getSerialTxtBuffer list size: "+list.size());
        Log.d(Command.TAG, "getSerialTxtBuffer length: "+sb.toString().length());
        return sb.toString();
    }

    /**
     * 图像数据
     * @param IWave_serialNum
     * @return
     */
    private static String getSerialImgBuffer(int IWave_serialNum){
        Class c= SerialBusManage.getInstance().getSerialBusType(IWave_serialNum);
        Log.d(Command.TAG, "getSerialImgBuffer class name: "+c.getSimpleName());
        List<SerialBusStruct.ISerialBusCSV> l= SerialBusManage.getInstance().getSerialImgBufferQueue(IWave_serialNum, c);
        if (l==null) return "Error";
        return toCsv(l);
    }

    private static <T extends SerialBusStruct.ISerialBusCSV> String toCsv(List<T> list){
        StringBuilder sb=new StringBuilder();
        if (list.size()>0){
            sb.append(list.get(0).toCsvHead()+";");
        }
        for(int i=0;i<list.size();i++){
            sb.append(list.get(i).toCSV()+";");
        }
        return sb.toString();
    }
    //endregion

}
