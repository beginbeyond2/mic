package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:04
 */
public class Command_Bus_Spi {
//            new SCPICommandStruct(":BUS#:SPI:CLK","SCPI_Bus_Spi","Clk"),
//            new SCPICommandStruct(":BUS#:SPI:CLK?","SCPI_Bus_Spi","ClkQ"),
//            new SCPICommandStruct(":BUS#:SPI:DATA","SCPI_Bus_Spi","Data"),
//            new SCPICommandStruct(":BUS#:SPI:DATA?","SCPI_Bus_Spi","DataQ"),
//            new SCPICommandStruct(":BUS#:SPI:WIDTh","SCPI_Bus_Spi","Width"),
//            new SCPICommandStruct(":BUS#:SPI:WIDTh?","SCPI_Bus_Spi","WidthQ"),
//            new SCPICommandStruct(":BUS#:SPI:IDLElvl","SCPI_Bus_Spi","IdLevel"),
//            new SCPICommandStruct(":BUS#:SPI:IDLElvl?","SCPI_Bus_Spi","IdLevelQ"),




    private final int[] clock = new int[ChannelFactory.SERIAL_CNT];
    private final int[] clockSwitch = new int[ChannelFactory.SERIAL_CNT];
    private final int[] data = new int[ChannelFactory.SERIAL_CNT];
    private final int[] dataSwitch = new int[ChannelFactory.SERIAL_CNT];
    private final int[] cs = new int[ChannelFactory.SERIAL_CNT];
    private final int[] csSwitch = new int[ChannelFactory.SERIAL_CNT];
    private final boolean[] csEnable = new boolean[ChannelFactory.SERIAL_CNT];
    private final int[] bits = new int[ChannelFactory.SERIAL_CNT];
    /**
     * CS
     * 数据
     * X:数据
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];
    private final int[] triggerMask = new int[ChannelFactory.SERIAL_CNT];
    private final int[] triggerData = new int[ChannelFactory.SERIAL_CNT];
    private final double[] levelClock = new double[ChannelFactory.SERIAL_CNT];
    private final double[] levelData = new double[ChannelFactory.SERIAL_CNT];
    private final double[] levelCs = new double[ChannelFactory.SERIAL_CNT];

    public int getClock(int serials) {
        return clock[serials];
    }

    public void setClock(int serials, int clock, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.clock)) return;
//        if (this.clock[serials] == clock) return;
        this.clock[serials] = clock;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CLOCK);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(clock));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getClockSwitch(int serials) {
        return clockSwitch[serials];
    }

    public void setClockSwitch(int serials, int clockSwitch, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.clockSwitch)) return;
//        if (this.clockSwitch[serials] == clockSwitch) return;
        this.clockSwitch[serials] = clockSwitch;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CLOCKSWITCH);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(clockSwitch));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getData(int serials) {
        return data[serials];
    }

    public void setData(int serials, int data, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.data)) return;
//        if (this.data[serials] == data) return;
        this.data[serials] = data;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_DATA);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(data));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getDataSwitch(int serials) {
        return dataSwitch[serials];
    }

    public void setDataSwitch(int serials, int dataSwitch, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.dataSwitch)) return;
//        if (this.dataSwitch[serials] == dataSwitch) return;
        this.dataSwitch[serials] = dataSwitch;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_DATASWITCH);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(dataSwitch));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getCs(int serials) {
        return cs[serials];
    }

    public void setCs(int serials, int cs, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.cs)) return;
//        if (this.cs[serials] == cs) return;
        this.cs[serials] = cs;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CS);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(cs));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getCsSwitch(int serials) {
        return csSwitch[serials];
    }

    public void setCsSwitch(int serials, int csSwitch, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.csSwitch)) return;
//        if (this.csSwitch[serials] == csSwitch) return;
        this.csSwitch[serials] = csSwitch;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CSWITCH);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(csSwitch));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean getCsEnable(int serials) {
        return csEnable[serials];
    }

    public void setCsEnable(int serials, boolean csEnable, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.csEnable)) return;
//        if (this.csEnable[serials] == csEnable) return;
        this.csEnable[serials] = csEnable;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CSENABLE);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(csEnable));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getBits(int serials) {
        return bits[serials];
    }

    public void setBits(int serials, int bits, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.bits)) return;
//        if (this.bits[serials] == bits) return;
        this.bits[serials] = bits;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_BITS);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(bits));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int getType(int serials) {
        return type[serials];
    }

    public int getTriggerMask(int serials) {
        return triggerMask[serials];
    }

    public int getTriggerData(int serials) {
        return triggerData[serials];
    }

    public void setType(int serials, int type, int triggerMask, int triggerData, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.type)) return;
        if (!ToolsSCPI.isCorrect(serials, this.triggerMask)) return;
        if (!ToolsSCPI.isCorrect(serials, this.triggerData)) return;
//        if (this.type[serials] == type && this.triggerMask[serials] == triggerMask && this.triggerData[serials] == triggerData)
//            return;
        this.type[serials] = type;
        this.triggerMask[serials] = triggerMask;
        this.triggerData[serials] = triggerData;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_TYPE);
            msgToUI.setParam(String.valueOf(serials)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)
                    + CommandMsgToUI.PARAM_SPLIT + triggerMask
                    + CommandMsgToUI.PARAM_SPLIT + triggerData);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevelClock(int serials) {
        return levelClock[serials];
    }

    public void setLevelClock(int serials, double level, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.levelClock)) return;
//        if (this.levelClock[serials] == level) return;
        this.levelClock[serials] = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCLOCK);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevelData(int serials) {
        return levelData[serials];
    }

    public void setLevelData(int serials, double level, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.levelData)) return;
//        if (levelData[serials] == level) return;
        this.levelData[serials] = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELDATA);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double getLevelCs(int serials) {
        return levelCs[serials];
    }

    public void setLevelCs(int serials, double level, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return;
        if (!ToolsSCPI.isCorrect(serials, this.levelCs)) return;
//        if (levelCs[serials] == level) return;
        this.levelCs[serials] = level;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCS);
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

}
