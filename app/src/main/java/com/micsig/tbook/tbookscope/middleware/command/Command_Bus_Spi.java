package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，提供SERIAL_CNT常量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:04
 */
/*
 * +=============================================================================+
 * |                        Command_Bus_Spi 类                                    |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   SPI串行总线命令模块，管理SPI总线的CLK/DATA/CS三线通道分配、边沿开关、       |
 * |   CS使能、数据位宽、触发类型/掩码/数据、三线电平等参数                        |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 管理每个总线槽位的SPI CLK时钟通道及边沿开关                             |
 * |   2. 管理每个总线槽位的SPI DATA数据通道及边沿开关                            |
 * |   3. 管理每个总线槽位的SPI CS片选通道、边沿开关及使能状态                    |
 * |   4. 管理每个总线槽位的数据位宽                                              |
 * |   5. 管理每个总线槽位的触发类型、触发掩码、触发数据                          |
 * |   6. 管理每个总线槽位的三线(CLK/DATA/CS)阈值电平                             |
 * |   7. 设置前校验总线类型是否为SPI，参数索引是否合法                            |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单状态管理类，每个属性提供getter/setter方法，set方法含类型校验+参数校验   |
 * |   +UI同步。setType方法同时设置type/triggerMask/triggerData三参数。            |
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → Clock/Data/Cs/Bits/Type/Level → 校验+保存 → RxBus → UI更新      |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - Command: 获取总线类型和消息载体                                          |
 * |   - ChannelFactory: 提供SERIAL_CNT常量                                       |
 * |   - ToolsSCPI: 参数索引合法性校验                                            |
 * |   - RxBus: 事件总线，发送UI更新消息                                          |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 设置SPI总线的CLK时钟通道和边沿开关                                     |
 * |   2. 设置SPI总线的DATA数据通道和边沿开关                                    |
 * |   3. 设置SPI总线的CS片选通道、边沿开关和使能状态                            |
 * |   4. 设置SPI总线的数据位宽                                                  |
 * |   5. 设置SPI总线的触发类型、触发掩码和触发数据                              |
 * |   6. 设置SPI总线三线的阈值电平                                              |
 * |   7. SCPI远程控制查询SPI参数                                                |
 * +=============================================================================+
 */
public class Command_Bus_Spi {
//            new SCPICommandStruct(":BUS#:SPI:CLK","SCPI_Bus_Spi","Clk"),       // SCPI设置SPI时钟通道
//            new SCPICommandStruct(":BUS#:SPI:CLK?","SCPI_Bus_Spi","ClkQ"),     // SCPI查询SPI时钟通道
//            new SCPICommandStruct(":BUS#:SPI:DATA","SCPI_Bus_Spi","Data"),     // SCPI设置SPI数据通道
//            new SCPICommandStruct(":BUS#:SPI:DATA?","SCPI_Bus_Spi","DataQ"),   // SCPI查询SPI数据通道
//            new SCPICommandStruct(":BUS#:SPI:WIDTh","SCPI_Bus_Spi","Width"),   // SCPI设置SPI数据位宽
//            new SCPICommandStruct(":BUS#:SPI:WIDTh?","SCPI_Bus_Spi","WidthQ"), // SCPI查询SPI数据位宽
//            new SCPICommandStruct(":BUS#:SPI:IDLElvl","SCPI_Bus_Spi","IdLevel"),   // SCPI设置SPI空闲电平
//            new SCPICommandStruct(":BUS#:SPI:IDLElvl?","SCPI_Bus_Spi","IdLevelQ"), // SCPI查询SPI空闲电平

    /** CLK时钟通道索引数组，每个总线槽位对应一个通道索引 */
    private final int[] clock = new int[ChannelFactory.SERIAL_CNT]; // 时钟通道索引
    /** CLK时钟边沿开关数组，0=上升沿，1=下降沿 */
    private final int[] clockSwitch = new int[ChannelFactory.SERIAL_CNT]; // 时钟边沿开关
    /** DATA数据通道索引数组 */
    private final int[] data = new int[ChannelFactory.SERIAL_CNT]; // 数据通道索引
    /** DATA数据边沿开关数组 */
    private final int[] dataSwitch = new int[ChannelFactory.SERIAL_CNT]; // 数据边沿开关
    /** CS片选通道索引数组 */
    private final int[] cs = new int[ChannelFactory.SERIAL_CNT]; // CS片选通道索引
    /** CS片选边沿开关数组 */
    private final int[] csSwitch = new int[ChannelFactory.SERIAL_CNT]; // CS边沿开关
    /** CS使能状态数组，true=启用CS片选线 */
    private final boolean[] csEnable = new boolean[ChannelFactory.SERIAL_CNT]; // CS使能状态
    /** 数据位宽数组，SPI传输的位宽（如8位/16位等） */
    private final int[] bits = new int[ChannelFactory.SERIAL_CNT]; // 数据位宽
    /**
     * CS
     * 数据
     * X:数据
     */
    /** 触发类型数组，定义SPI触发条件类型 */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT]; // 触发类型
    /** 触发掩码数组，用于触发条件的数据掩码匹配 */
    private final int[] triggerMask = new int[ChannelFactory.SERIAL_CNT]; // 触发掩码
    /** 触发数据数组，用于触发条件的数据值匹配 */
    private final int[] triggerData = new int[ChannelFactory.SERIAL_CNT]; // 触发数据
    /** CLK时钟线阈值电平数组，单位V */
    private final double[] levelClock = new double[ChannelFactory.SERIAL_CNT]; // CLK电平阈值
    /** DATA数据线阈值电平数组，单位V */
    private final double[] levelData = new double[ChannelFactory.SERIAL_CNT]; // DATA电平阈值
    /** CS片选线阈值电平数组，单位V */
    private final double[] levelCs = new double[ChannelFactory.SERIAL_CNT]; // CS电平阈值

    /**
     * 获取指定总线槽位的CLK时钟通道索引
     *
     * @param serials 总线槽位索引
     * @return CLK时钟通道索引
     */
    public int getClock(int serials) {
        return clock[serials]; // 返回时钟通道索引
    }

    /**
     * 设置指定总线槽位的CLK时钟通道索引
     *
     * @param serials     总线槽位索引
     * @param clock       CLK时钟通道索引
     * @param isUpdateUI  是否同步更新UI
     */
    public void setClock(int serials, int clock, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI，不是则直接返回
        if (!ToolsSCPI.isCorrect(serials, this.clock)) return; // 校验参数索引是否合法，不合法则直接返回
//        if (this.clock[serials] == clock) return; // 值相同时跳过（已注释）
        this.clock[serials] = clock; // 保存时钟通道索引
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CLOCK); // 设置消息标志：SPI时钟通道
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(clock)); // 设置参数：槽位索引+时钟通道
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的CLK时钟边沿开关
     *
     * @param serials 总线槽位索引
     * @return CLK时钟边沿开关值
     */
    public int getClockSwitch(int serials) {
        return clockSwitch[serials]; // 返回时钟边沿开关
    }

    /**
     * 设置指定总线槽位的CLK时钟边沿开关
     *
     * @param serials      总线槽位索引
     * @param clockSwitch  CLK时钟边沿开关值
     * @param isUpdateUI   是否同步更新UI
     */
    public void setClockSwitch(int serials, int clockSwitch, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.clockSwitch)) return; // 校验参数索引是否合法
//        if (this.clockSwitch[serials] == clockSwitch) return; // 值相同时跳过（已注释）
        this.clockSwitch[serials] = clockSwitch; // 保存时钟边沿开关
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CLOCKSWITCH); // 设置消息标志：SPI时钟边沿开关
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(clockSwitch)); // 设置参数：槽位索引+边沿开关
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的DATA数据通道索引
     *
     * @param serials 总线槽位索引
     * @return DATA数据通道索引
     */
    public int getData(int serials) {
        return data[serials]; // 返回数据通道索引
    }

    /**
     * 设置指定总线槽位的DATA数据通道索引
     *
     * @param serials     总线槽位索引
     * @param data        DATA数据通道索引
     * @param isUpdateUI  是否同步更新UI
     */
    public void setData(int serials, int data, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.data)) return; // 校验参数索引是否合法
//        if (this.data[serials] == data) return; // 值相同时跳过（已注释）
        this.data[serials] = data; // 保存数据通道索引
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_DATA); // 设置消息标志：SPI数据通道
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(data)); // 设置参数：槽位索引+数据通道
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的DATA数据边沿开关
     *
     * @param serials 总线槽位索引
     * @return DATA数据边沿开关值
     */
    public int getDataSwitch(int serials) {
        return dataSwitch[serials]; // 返回数据边沿开关
    }

    /**
     * 设置指定总线槽位的DATA数据边沿开关
     *
     * @param serials      总线槽位索引
     * @param dataSwitch   DATA数据边沿开关值
     * @param isUpdateUI   是否同步更新UI
     */
    public void setDataSwitch(int serials, int dataSwitch, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.dataSwitch)) return; // 校验参数索引是否合法
//        if (this.dataSwitch[serials] == dataSwitch) return; // 值相同时跳过（已注释）
        this.dataSwitch[serials] = dataSwitch; // 保存数据边沿开关
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_DATASWITCH); // 设置消息标志：SPI数据边沿开关
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(dataSwitch)); // 设置参数：槽位索引+边沿开关
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的CS片选通道索引
     *
     * @param serials 总线槽位索引
     * @return CS片选通道索引
     */
    public int getCs(int serials) {
        return cs[serials]; // 返回CS片选通道索引
    }

    /**
     * 设置指定总线槽位的CS片选通道索引
     *
     * @param serials     总线槽位索引
     * @param cs          CS片选通道索引
     * @param isUpdateUI  是否同步更新UI
     */
    public void setCs(int serials, int cs, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.cs)) return; // 校验参数索引是否合法
//        if (this.cs[serials] == cs) return; // 值相同时跳过（已注释）
        this.cs[serials] = cs; // 保存CS片选通道索引
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CS); // 设置消息标志：SPI片选通道
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(cs)); // 设置参数：槽位索引+片选通道
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的CS片选边沿开关
     *
     * @param serials 总线槽位索引
     * @return CS片选边沿开关值
     */
    public int getCsSwitch(int serials) {
        return csSwitch[serials]; // 返回CS边沿开关
    }

    /**
     * 设置指定总线槽位的CS片选边沿开关
     *
     * @param serials      总线槽位索引
     * @param csSwitch     CS片选边沿开关值
     * @param isUpdateUI   是否同步更新UI
     */
    public void setCsSwitch(int serials, int csSwitch, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.csSwitch)) return; // 校验参数索引是否合法
//        if (this.csSwitch[serials] == csSwitch) return; // 值相同时跳过（已注释）
        this.csSwitch[serials] = csSwitch; // 保存CS边沿开关
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CSWITCH); // 设置消息标志：SPI片选边沿开关
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(csSwitch)); // 设置参数：槽位索引+边沿开关
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的CS使能状态
     *
     * @param serials 总线槽位索引
     * @return CS使能状态，true=启用CS片选线
     */
    public boolean getCsEnable(int serials) {
        return csEnable[serials]; // 返回CS使能状态
    }

    /**
     * 设置指定总线槽位的CS使能状态
     *
     * @param serials     总线槽位索引
     * @param csEnable    CS使能状态，true=启用CS片选线
     * @param isUpdateUI  是否同步更新UI
     */
    public void setCsEnable(int serials, boolean csEnable, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.csEnable)) return; // 校验参数索引是否合法
//        if (this.csEnable[serials] == csEnable) return; // 值相同时跳过（已注释）
        this.csEnable[serials] = csEnable; // 保存CS使能状态
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_CSENABLE); // 设置消息标志：SPI片选使能
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(csEnable)); // 设置参数：槽位索引+使能状态
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的数据位宽
     *
     * @param serials 总线槽位索引
     * @return 数据位宽值
     */
    public int getBits(int serials) {
        return bits[serials]; // 返回数据位宽
    }

    /**
     * 设置指定总线槽位的数据位宽
     *
     * @param serials     总线槽位索引
     * @param bits        数据位宽值
     * @param isUpdateUI  是否同步更新UI
     */
    public void setBits(int serials, int bits, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.bits)) return; // 校验参数索引是否合法
//        if (this.bits[serials] == bits) return; // 值相同时跳过（已注释）
        this.bits[serials] = bits; // 保存数据位宽
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_BITS); // 设置消息标志：SPI数据位宽
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(bits)); // 设置参数：槽位索引+数据位宽
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的触发类型
     *
     * @param serials 总线槽位索引
     * @return 触发类型值
     */
    public int getType(int serials) {
        return type[serials]; // 返回触发类型
    }

    /**
     * 获取指定总线槽位的触发掩码
     *
     * @param serials 总线槽位索引
     * @return 触发掩码值
     */
    public int getTriggerMask(int serials) {
        return triggerMask[serials]; // 返回触发掩码
    }

    /**
     * 获取指定总线槽位的触发数据
     *
     * @param serials 总线槽位索引
     * @return 触发数据值
     */
    public int getTriggerData(int serials) {
        return triggerData[serials]; // 返回触发数据
    }

    /**
     * 同时设置指定总线槽位的触发类型、触发掩码和触发数据
     * <p>三个参数需要原子性更新，因此合并在一个方法中</p>
     *
     * @param serials      总线槽位索引
     * @param type         触发类型值
     * @param triggerMask  触发掩码值
     * @param triggerData  触发数据值
     * @param isUpdateUI   是否同步更新UI
     */
    public void setType(int serials, int type, int triggerMask, int triggerData, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.type)) return; // 校验type参数索引是否合法
        if (!ToolsSCPI.isCorrect(serials, this.triggerMask)) return; // 校验triggerMask参数索引是否合法
        if (!ToolsSCPI.isCorrect(serials, this.triggerData)) return; // 校验triggerData参数索引是否合法
//        if (this.type[serials] == type && this.triggerMask[serials] == triggerMask && this.triggerData[serials] == triggerData)
//            return; // 三个值都相同时跳过（已注释）
        this.type[serials] = type; // 保存触发类型
        this.triggerMask[serials] = triggerMask; // 保存触发掩码
        this.triggerData[serials] = triggerData; // 保存触发数据
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_TYPE); // 设置消息标志：SPI触发类型
            msgToUI.setParam(String.valueOf(serials) // 拼接参数字符串
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type) // 槽位索引 + 触发类型
                    + CommandMsgToUI.PARAM_SPLIT + triggerMask // + 触发掩码
                    + CommandMsgToUI.PARAM_SPLIT + triggerData); // + 触发数据
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的CLK时钟线阈值电平
     *
     * @param serials 总线槽位索引
     * @return CLK阈值电平值，单位V
     */
    public double getLevelClock(int serials) {
        return levelClock[serials]; // 返回CLK电平阈值
    }

    /**
     * 设置指定总线槽位的CLK时钟线阈值电平
     *
     * @param serials     总线槽位索引
     * @param level       CLK阈值电平值，单位V
     * @param isUpdateUI  是否同步更新UI
     */
    public void setLevelClock(int serials, double level, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.levelClock)) return; // 校验参数索引是否合法
//        if (this.levelClock[serials] == level) return; // 值相同时跳过（已注释）
        this.levelClock[serials] = level; // 保存CLK电平阈值
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCLOCK); // 设置消息标志：SPI时钟电平
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level)); // 设置参数：槽位索引+电平值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的DATA数据线阈值电平
     *
     * @param serials 总线槽位索引
     * @return DATA阈值电平值，单位V
     */
    public double getLevelData(int serials) {
        return levelData[serials]; // 返回DATA电平阈值
    }

    /**
     * 设置指定总线槽位的DATA数据线阈值电平
     *
     * @param serials     总线槽位索引
     * @param level       DATA阈值电平值，单位V
     * @param isUpdateUI  是否同步更新UI
     */
    public void setLevelData(int serials, double level, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.levelData)) return; // 校验参数索引是否合法
//        if (levelData[serials] == level) return; // 值相同时跳过（已注释）
        this.levelData[serials] = level; // 保存DATA电平阈值
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELDATA); // 设置消息标志：SPI数据电平
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level)); // 设置参数：槽位索引+电平值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 获取指定总线槽位的CS片选线阈值电平
     *
     * @param serials 总线槽位索引
     * @return CS阈值电平值，单位V
     */
    public double getLevelCs(int serials) {
        return levelCs[serials]; // 返回CS电平阈值
    }

    /**
     * 设置指定总线槽位的CS片选线阈值电平
     *
     * @param serials     总线槽位索引
     * @param level       CS阈值电平值，单位V
     * @param isUpdateUI  是否同步更新UI
     */
    public void setLevelCs(int serials, double level, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(serials)!=Command_Bus.Type_SPI)return; // 校验总线类型是否为SPI
        if (!ToolsSCPI.isCorrect(serials, this.levelCs)) return; // 校验参数索引是否合法
//        if (levelCs[serials] == level) return; // 值相同时跳过（已注释）
        this.levelCs[serials] = level; // 保存CS电平阈值
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCS); // 设置消息标志：SPI片选电平
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level)); // 设置参数：槽位索引+电平值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

}
