package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * Created by yangj on 2018/1/22.
 */

public class Command_Trigger_M429 {
    private final int[] source = new int[ChannelFactory.SERIAL_CNT];
    private final int[] format = new int[ChannelFactory.SERIAL_CNT];
    private final int[] display = new int[ChannelFactory.SERIAL_CNT];
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT];
    /**
     * 字起始
     * 字结束
     * LABEL
     * SDI
     * DATA
     * SSM
     * LABEL+SDI
     * label+Data
     * Label+SSM
     * 字错误
     * 字间隙错误
     * 校验错误
     * 所有错误
     * 所有0位
     * 所有1位
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];
    private final int[] label = new int[ChannelFactory.SERIAL_CNT];
    private final int[] sdi = new int[ChannelFactory.SERIAL_CNT];
    private final long[] data = new long[ChannelFactory.SERIAL_CNT];
    private final int[] ssm = new int[ChannelFactory.SERIAL_CNT];
    private final double[] levelHigh = new double[ChannelFactory.SERIAL_CNT];
    private final double[] levelLow = new double[ChannelFactory.SERIAL_CNT];

    public int getSource(int serials) {
        return source[serials];
    }

    public void setSource(int serials, int source, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.source)) return;
        if (this.source[serials] == source) return;
        this.source[serials] = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_SOURCE);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getFormat(int serials) {
        return format[serials];
    }

    public void setFormat(int serials, int format, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.format)) return;
        if (this.format[serials] == format) return;
        this.format[serials] = format;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_FORMAT);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(format));
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_DISPLAY);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display));
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_BAUDRATE);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getType(int serials) {
        return type[serials];
    }

    public int getLabel(int serials) {
        return label[serials];
    }

    public int getSdi(int serials) {
        return sdi[serials];
    }

    public long getData(int serials) {
        return data[serials];
    }

    public int getSsm(int serials) {
        return ssm[serials];
    }

    public void setType(int serials, int type, int label, int sdi, long data, int ssm, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;
        if(!ToolsSCPI.isCorrect(serials,this.label)) return;
        if(!ToolsSCPI.isCorrect(serials,this.sdi)) return;
        if(!ToolsSCPI.isCorrect(serials,this.data)) return;
        if(!ToolsSCPI.isCorrect(serials,this.ssm)) return;
//        if (this.type[serials] == type && this.label[serials] == label && this.sdi[serials] == sdi
//                && this.data[serials] == data && this.ssm[serials] == ssm)
//            return;
        int minLabel=0;
        int maxLabel=255; //八进制 377
        if (label<minLabel) label=minLabel;
        if (label>maxLabel) label=maxLabel;

        int minData=0;
        int maxData=0xFFFFFF;
        if (data<minData) data=minData;
        if (data>maxData) data=maxData;


        this.type[serials] = type;
        this.label[serials] = label;
        this.sdi[serials] = sdi;
        this.data[serials] = data;
        this.ssm[serials] = ssm;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_TYPE);
            msgToUI.setParam(String.valueOf(serials)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(label)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(sdi)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(data)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(ssm));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevelHigh(int serials) {
        return levelHigh[serials];
    }

    public void setLevelHigh(int serials,int ch, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.levelHigh)) return;
//        if (this.levelHigh[serials] == level) return;
        this.levelHigh[serials] =  level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_LEVELHIGH);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level)+CommandMsgToUI.PARAM_SPLIT+ch);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevelLow(int serials) {
        return levelLow[serials];
    }

    public void setLevelLow(int serials, int ch, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.levelLow)) return;
//        if (this.levelLow[serials] == level) return;
        this.levelLow[serials] =  level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_LEVELLOW);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level)+CommandMsgToUI.PARAM_SPLIT+ch);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
}
