package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂（含SERIAL_CNT常量）
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                         Command_Trigger_Can                                  |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器CAN总线触发命令处理模块                                      |
 * | 核心职责: 处理SCPI CAN触发相关指令，包括触发源/空闲类型/波特率/触发条件/     |
 * |          触发ID/DLC/数据/触发电平的设置与查询                                |
 * | 架构设计: 命令模式，作为Command子模块，支持多路串行总线(SERIAL_CNT)           |
 * |          每个参数按串行总线索引存储，通过ToolsSCPI.isCorrect校验索引合法性    |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层                        |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum, ChannelFactory, ToolsSCPI |
 * | 使用场景: 远程控制CAN总线触发参数、查询CAN触发状态时使用                     |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Trigger_Can {
//    new SCPICommandStruct(":TRIGger:CAN:SOURce","SCPI_Trigger_Can","Source"),//设置CAN触发的触发源
//            new SCPICommandStruct(":TRIGger:CAN:SOURce?","SCPI_Trigger_Can","SourceQ"),//查询CAN触发的触发源
//            new SCPICommandStruct(":TRIGger:CAN:TYPE","SCPI_Trigger_Can","Type"),//设置CAN触发的触发条件
//            new SCPICommandStruct(":TRIGger:CAN:TYPE?","SCPI_Trigger_Can","TypeQ"),//查询CAN触发的触发条件
//            new SCPICommandStruct(":TRIGger:CAN:ID","SCPI_Trigger_Can","Id"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，设置CAN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:CAN:ID?","SCPI_Trigger_Can","IdQ"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，查询CAN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:CAN:DLC","SCPI_Trigger_Can","DLC"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的DLC值
//            new SCPICommandStruct(":TRIGger:CAN:DLC?","SCPI_Trigger_Can","DLCQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的DLC值
//            new SCPICommandStruct(":TRIGger:CAN:DATA","SCPI_Trigger_Can","Data"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的触发数据值
//            new SCPICommandStruct(":TRIGger:CAN:DATA?","SCPI_Trigger_Can","DataQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的触发数据值
//            new SCPICommandStruct(":TRIGger:CAN:LEVel","SCPI_Trigger_Can","Level"),//设置CAN触发时的触发电平
//            new SCPICommandStruct(":TRIGger:CAN:LEVel?","SCPI_Trigger_Can","LevelQ"),//查询CAN触发时的触发电平

    private final int[] source = new int[ChannelFactory.SERIAL_CNT]; // 触发源数组（按串行总线索引存储）
    private final int[] idle = new int[ChannelFactory.SERIAL_CNT]; // 空闲类型数组
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT]; // 波特率数组
    /**
     * 触发条件(type)枚举值：
     * 帧起始
     * 远程帧ID
     * 数据帧ID
     * 远程帧/数据帧ID
     * 数据帧ID和数据
     * 错误帧
     * 所有错误
     * 确认错误
     * 过载帧
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT]; // 触发条件数组
    private final int[] id = new int[ChannelFactory.SERIAL_CNT]; // 触发ID数组
    private final int[] dlc = new int[ChannelFactory.SERIAL_CNT]; // DLC（数据长度代码）数组
    private final long[] data = new long[ChannelFactory.SERIAL_CNT]; // 触发数据数组
    private final double[] level = new double[ChannelFactory.SERIAL_CNT]; // 触发电平数组

    /**
     * 查询CAN触发的触发源
     *
     * @param serials 串行总线索引
     * @return 触发源索引
     */
    public int getSource(int serials) {
        return source[serials]; // 返回指定串行总线的触发源
    }

    /**
     * 设置CAN触发的触发源
     *
     * @param serials     串行总线索引
     * @param source      触发源索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setSource(int serials, int source, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.source)) return; // 校验串行总线索引合法性
        if (this.source[serials] == source) return; // 值未变化则直接返回
        this.source[serials] = source; // 保存触发源
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_SOURCE); // 设置消息标志为CAN触发源
            String params = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source); // 拼接串行总线索引和触发源参数
            msgToUI.setParam(params); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询CAN触发的空闲类型
     *
     * @param serials 串行总线索引
     * @return 空闲类型索引
     */
    public int getIdle(int serials) {
        return idle[serials]; // 返回指定串行总线的空闲类型
    }

    /**
     * 设置CAN触发的空闲类型
     *
     * @param serials     串行总线索引
     * @param idle        空闲类型索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setIdle(int serials, int idle, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.idle)) return; // 校验串行总线索引合法性
        if (this.idle[serials] == idle) return; // 值未变化则直接返回
        this.idle[serials] = idle; // 保存空闲类型
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_IDLE); // 设置消息标志为CAN空闲类型
            String params = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(idle); // 拼接串行总线索引和空闲类型参数
            msgToUI.setParam(params); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询CAN触发的波特率
     *
     * @param serials 串行总线索引
     * @return 波特率值
     */
    public int getBaudRate(int serials) {
        return baudRate[serials]; // 返回指定串行总线的波特率
    }

    /**
     * 设置CAN触发的波特率
     *
     * @param serials     串行总线索引
     * @param baudRate    波特率值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setBaudRate(int serials, int baudRate, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.baudRate)) return; // 校验串行总线索引合法性
        if (this.baudRate[serials] == baudRate) return; // 值未变化则直接返回
        this.baudRate[serials] = baudRate; // 保存波特率
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_BAUDRATE); // 设置消息标志为CAN波特率
            String params = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate); // 拼接串行总线索引和波特率参数
            msgToUI.setParam(params); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询CAN触发的触发条件类型
     *
     * @param serials 串行总线索引
     * @return 触发条件类型索引
     */
    public int getType(int serials) {
        return type[serials]; // 返回指定串行总线的触发条件
    }

    /**
     * 查询CAN触发的触发ID值
     *
     * @param serials 串行总线索引
     * @return 触发ID值
     */
    public int getId(int serials) {
        return id[serials]; // 返回指定串行总线的触发ID
    }

    /**
     * 查询CAN触发的DLC（数据长度代码）值
     *
     * @param serials 串行总线索引
     * @return DLC值
     */
    public int getDlc(int serials) {
        return dlc[serials]; // 返回指定串行总线的DLC
    }

    /**
     * 查询CAN触发的触发数据值
     *
     * @param serials 串行总线索引
     * @return 触发数据值
     */
    public long getData(int serials) {
        return data[serials]; // 返回指定串行总线的触发数据
    }

    /**
     * 设置CAN触发的触发条件（同时设置type/id/dlc/data）
     * ID值限制在0~0x1FFFFFFF范围内（29位扩展帧）
     *
     * @param serials     串行总线索引
     * @param type        触发条件类型
     * @param id          触发ID值
     * @param dlc         DLC（数据长度代码）
     * @param data        触发数据值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setType(int serials, int type, long id, int dlc, long data, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return; // 校验type数组索引合法性
        if(!ToolsSCPI.isCorrect(serials,this.id)) return; // 校验id数组索引合法性
        if(!ToolsSCPI.isCorrect(serials,this.dlc)) return; // 校验dlc数组索引合法性
        if(!ToolsSCPI.isCorrect(serials,this.data)) return; // 校验data数组索引合法性
//        if (this.type[serials] == type && this.id[serials] == id && this.dlc[serials] == dlc && this.data[serials] == data)
//            return;

        long minId=0; // ID最小值
        long maxId=0x1FFFFFFF; // ID最大值（29位扩展帧）
        if (id<minId) id=minId; // ID不能小于0
        if (id>maxId) id=maxId; // ID不能超过29位最大值

        this.type[serials] = type; // 保存触发条件类型
        this.id[serials] = (int)id; // 保存触发ID（强转为int）
        this.dlc[serials] = dlc; // 保存DLC
        this.data[serials] = data; // 保存触发数据
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_TYPE); // 设置消息标志为CAN触发条件
            String params = String.valueOf(serials) // 拼接串行总线索引
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type) // 拼接触发条件类型
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(id) // 拼接触发ID
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(dlc) // 拼接DLC
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(data); // 拼接触发数据
            msgToUI.setParam(params); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询CAN触发的触发电平
     *
     * @param serials 串行总线索引
     * @return 触发电平值
     */
    public double getLevel(int serials) {
        return level[serials]; // 返回指定串行总线的触发电平
    }

    /**
     * 设置CAN触发的触发电平
     *
     * @param serials     串行总线索引
     * @param level       触发电平值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setLevel(int serials, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.level)) return; // 校验串行总线索引合法性
        if (this.level[serials] == level) return; // 值未变化则直接返回
        this.level[serials] = level; // 保存触发电平
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_LEVEL); // 设置消息标志为CAN触发电平
            String params = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level); // 拼接串行总线索引和电平参数
            msgToUI.setParam(params); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }
}
