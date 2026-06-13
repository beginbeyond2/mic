package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * Created by yangj on 2018/1/22.
 */

public class Command_Trigger_M1553B {
    private int[] source = new int[ChannelFactory.SERIAL_CNT];
    private int[] display = new int[ChannelFactory.SERIAL_CNT];
    /**
     * 指令/状态字同步头
     * 数据字同步头
     * 指令/状态字
     * 远程终端地址
     * 曼切斯特码错误
     * 数据字
     * 奇校验错误
     * 所有错误
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];
    private final int[] csWord = new int[ChannelFactory.SERIAL_CNT];
    private final int[] dataWord = new int[ChannelFactory.SERIAL_CNT];
    private final int[] rtAddr = new int[ChannelFactory.SERIAL_CNT];
    private final double[] level = new double[ChannelFactory.SERIAL_CNT];

    public int getSource(int serials) {
        return source[serials];
    }

    public void setSource(int serials, int source, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.source)) return;
        if (this.source[serials] == source) return;
        this.source[serials] = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_SOURCE);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source));
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_DISPLAY);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getType(int serials) {
        return type[serials];
    }

    public int getCsWord(int serials) {
        return csWord[serials];
    }

    public int getDataWord(int serials) {
        return dataWord[serials];
    }

    public int getRtAddr(int serials) {
        return rtAddr[serials];
    }

    public void setType(int serials, int type, int csWord, int rtAddr, int dataWord, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;
        if(!ToolsSCPI.isCorrect(serials,this.csWord)) return;
        if(!ToolsSCPI.isCorrect(serials,this.rtAddr)) return;
        if(!ToolsSCPI.isCorrect(serials,this.dataWord)) return;
//        if (this.type[serials] == type && this.csWord[serials] == csWord && this.rtAddr[serials] == rtAddr
//                && this.dataWord[serials] == dataWord)
//            return;
        this.type[serials] = type;
        this.csWord[serials] = csWord;
        this.rtAddr[serials] = rtAddr;
        this.dataWord[serials] = dataWord;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_TYPE);
            msgToUI.setParam(String.valueOf(serials)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(csWord)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(rtAddr)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(dataWord));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevel(int serials) {
        return level[serials];
    }

    public void setLevel(int serials, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.level)) return;
        if (this.level[serials] == level) return;
        this.level[serials] = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_LEVEL);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
}
