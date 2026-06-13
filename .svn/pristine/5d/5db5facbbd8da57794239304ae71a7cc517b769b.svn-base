package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * Created by liwb on 2018/1/17.
 * 主菜单-> 自动
 */

public class Command_Auto {


    private int channelIndex;
    private double level;
    private int triggerSource;
    private int range;
    private int rangeVertical;
    private int rangeHorizoncal;
    private int rangeLevel;

    /**
     * 设置 主菜单-> 自动-> 自动设置-> 自动打开通道
     *
     * @param b          0： 开启  1：关闭
     * @param isUpdateUI 是否改变界面设置
     */
    public void setChannel(boolean b, boolean isUpdateUI) {
        if (b == (channelIndex == 0)) return;
        this.channelIndex = b ? 0 : 1;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_CHANNEL);
            msgToUI.setParam(String.valueOf(channelIndex));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean setChannelQuery() {
        return this.channelIndex == 0;
    }

    public void setLevel(double level, boolean isUpdateUI) {
        this.level = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_LEVEL);
            msgToUI.setParam(String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double setLevelQuery() {
        return this.level;
    }

    public void setSource(int source, boolean isUpdateUI) {
        if (this.triggerSource == source) return;
        this.triggerSource = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_SOURCE);
            msgToUI.setParam(String.valueOf(source));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int setSourceQuery() {
        return this.triggerSource;
    }

//     new SCPICommandStruct(":AUTO:RANge","SCPI_Auto","Range"),//设置自动量程
//            new SCPICommandStruct(":AUTO:RANge?","SCPI_Auto","RangeQ"),//查询自动量程
//            new SCPICommandStruct(":AUTO:RANge:VERtical","SCPI_Auto","Range_Vertical"),//设置自动垂直
//            new SCPICommandStruct(":AUTO:RANge:VERtical?","SCPI_Auto","Range_VerticalQ"),//查询自动垂直
//            new SCPICommandStruct(":AUTO:RANge:HORizoncal","SCPI_Auto","Range_Horizoncal"),//设置自动水平
//            new SCPICommandStruct(":AUTO:RANge:HORizoncal?","SCPI_Auto","Range_HorizoncalQ"),//查询自动水平
//            new SCPICommandStruct(":AUTO:RANge:LEVEl","SCPI_Auto","Range_Level"),//设置自动量程
//            new SCPICommandStruct(":AUTO:RANge:LEVEl?","SCPI_Auto","Range_LevelQ"),//查询自动量程

    public void range(boolean b, boolean isUpdateUI) {
        if ((range == 0) == b) return;
        this.range = b ? 0 : 1;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_RANGE);
            msgToUI.setParam(String.valueOf(range));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean rangeQuery() {
        return this.range == 0;
    }

    public void rangeVertical(boolean b, boolean isUpdateUI) {
        if ((rangeVertical == 0) == b) return;
        this.rangeVertical = b ? 0 : 1;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_RANGEVERTICAL);
            msgToUI.setParam(String.valueOf(rangeVertical));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean rangeVerticalQuery() {
        return this.rangeVertical == 0;
    }

    public void rangeHorizoncal(boolean b, boolean isUpdateUI) {
        if ((rangeHorizoncal == 0) == b) return;
        this.rangeHorizoncal = b ? 0 : 1;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_RANGEHORIZONTAL);
            msgToUI.setParam(String.valueOf(rangeHorizoncal));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean rangeHorizoncalQuery() {
        return this.rangeHorizoncal == 0;
    }

    public void rangeLevel(boolean b, boolean isUpdateUI) {
        if ((rangeLevel == 0) == b) return;
        this.rangeLevel = b ? 0 : 1;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_RANGELEVEL);
            msgToUI.setParam(String.valueOf(rangeLevel));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean rangeLevelQuery() {
        return this.rangeLevel == 0;
    }
}
