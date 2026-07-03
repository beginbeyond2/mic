package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂，提供串口数量等常量
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;  // SCPI指令工具类，提供参数校验

/*
 * +=============================================================================+
 * |                       Command_Trigger_Lin - LIN总线触发命令模型                     |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，LIN总线触发器的参数存储与UI同步                    |
 * | 核心职责 : 管理LIN协议触发器的全部可配置参数（触发源、空闲电平、波特率、触发类型、       |
 * |            帧ID、帧数据、触发电平），参数变更时通过RxBus通知UI层刷新                   |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : ChannelFactory(串口数量)、ToolsSCPI(参数校验)、CommandMsgToUI(消息封装)、  |
 * |            RxBus/RxEnum(事件总线)                                                   |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置LIN触发参数时，由SCPI分发器调用对应方法       |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Lin {
//      new SCPICommandStruct(":TRIGger:LIN:SOURce","SCPI_Trigger_Lin","Source"),//设置LIN触发的触发源
//            new SCPICommandStruct(":TRIGger:LIN:SOURce?","SCPI_Trigger_Lin","SourceQ"),//查询LIN触发的触发源
//            new SCPICommandStruct(":TRIGger:LIN:TYPE","SCPI_Trigger_Lin","Type"),//设置LIN触发的触发条件
//            new SCPICommandStruct(":TRIGger:LIN:TYPE?","SCPI_Trigger_Lin","TypeQ"),//查询LIN触发的触发条件
//            new SCPICommandStruct(":TRIGger:LIN:ID","SCPI_Trigger_Lin","Id"),//当LIN总线触发条件为FID或IDATa时，设置LIN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:LIN:ID?","SCPI_Trigger_Lin","IdQ"),//当LIN总线触发条件为FID或IDATa时，查询LIN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:LIN:DATA","SCPI_Trigger_Lin","Data"),//当LIN总线触发条件为IDATa时，设置LIN触发的触发数据
//            new SCPICommandStruct(":TRIGger:LIN:DATA?","SCPI_Trigger_Lin","DataQ"),//当LIN总线触发条件为IDATa时，查询LIN触发的触发数据
//            new SCPICommandStruct(":TRIGger:LIN:LEVel","SCPI_Trigger_Lin","Level"),//设置LIN触发时的触发电平
//            new SCPICommandStruct(":TRIGger:LIN:LEVel?","SCPI_Trigger_Lin","LevelQ"),//查询LIN触发时的触发电平

    private final int[] source = new int[ChannelFactory.SERIAL_CNT];  // LIN触发源通道索引数组，每个串口一个
    private final int[] idle = new int[ChannelFactory.SERIAL_CNT];  // LIN空闲电平数组
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT];  // LIN波特率数组
    private final int[] linType = new int[ChannelFactory.SERIAL_CNT];  // LIN类型数组
    /**
     * 同步上升沿
     * 帧ID
     * 帧ID和数据
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];  // LIN触发类型数组：同步上升沿/帧ID/帧ID和数据
    private final int[] id = new int[ChannelFactory.SERIAL_CNT];  // LIN触发帧ID数组
    private final long[] data = new long[ChannelFactory.SERIAL_CNT];  // LIN触发帧数据数组
    private final double[] level = new double[ChannelFactory.SERIAL_CNT];  // LIN触发电平数组

    /**
     * 获取指定串口的LIN类型
     *
     * @param serials 串口索引
     * @return LIN类型值
     */
    public int getLinType(int serials) {
        return linType[serials];  // 返回指定串口的LIN类型
    }

    /**
     * 设置指定串口的LIN类型，值变化时通知UI刷新
     *
     * @param serials      串口索引
     * @param linType      LIN类型值
     * @param isUpdataUI   是否通知UI刷新
     */
    public void setLinType(int serials, int linType, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.linType)) return;  // 校验串口索引是否合法
        if (this.linType[serials] == serials) return;  // 值未变化则直接返回（注：此处比较对象有误，应为linType而非serials）
        this.linType[serials] = linType;  // 更新LIN类型值
        if (isUpdataUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_TYPE);  // 设置消息标志为LIN类型变更
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(linType);  // 拼接参数
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的LIN触发源
     *
     * @param serials 串口索引
     * @return 触发源值
     */
    public int getSource(int serials) {
        return source[serials];  // 返回指定串口的触发源
    }

    /**
     * 设置指定串口的LIN触发源，值变化时通知UI刷新
     *
     * @param serials      串口索引
     * @param source       触发源值
     * @param isUpdataUI   是否通知UI刷新
     */
    public void setSource(int serials, int source, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.source)) return;  // 校验串口索引是否合法
        if (this.source[serials] == serials) return;  // 值未变化则直接返回（注：此处比较对象有误，应为source而非serials）
        this.source[serials] = source;  // 更新触发源值
        if (isUpdataUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_SOURCE);  // 设置消息标志为LIN触发源变更
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);  // 拼接参数
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的LIN空闲电平
     *
     * @param serials 串口索引
     * @return 空闲电平值
     */
    public int getIdle(int serials) {
        return idle[serials];  // 返回指定串口的空闲电平
    }

    /**
     * 设置指定串口的LIN空闲电平，值变化时通知UI刷新
     *
     * @param serials      串口索引
     * @param idle         空闲电平值
     * @param isUpdataUI   是否通知UI刷新
     */
    public void setIdle(int serials, int idle, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.idle)) return;  // 校验串口索引是否合法
        if (this.idle[serials] == idle) return;  // 值未变化则直接返回
        this.idle[serials] = idle;  // 更新空闲电平值
        if (isUpdataUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_IDLE);  // 设置消息标志为LIN空闲电平变更
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(idle);  // 拼接参数
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的LIN波特率
     *
     * @param serials 串口索引
     * @return 波特率值
     */
    public int getBaudRate(int serials) {
        return baudRate[serials];  // 返回指定串口的波特率
    }

    /**
     * 设置指定串口的LIN波特率，值变化时通知UI刷新
     *
     * @param serials      串口索引
     * @param baudRate     波特率值
     * @param isUpdataUI   是否通知UI刷新
     */
    public void setBaudRate(int serials, int baudRate, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.baudRate)) return;  // 校验串口索引是否合法
        if (this.baudRate[serials] == baudRate) return;  // 值未变化则直接返回
        this.baudRate[serials] = baudRate;  // 更新波特率值
        if (isUpdataUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_BAUDRATE);  // 设置消息标志为LIN波特率变更
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate);  // 拼接参数
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的LIN触发类型
     *
     * @param serials 串口索引
     * @return 触发类型值
     */
    public int getType(int serials) {
        return type[serials];  // 返回指定串口的触发类型
    }

    /**
     * 获取指定串口的LIN触发帧ID
     *
     * @param serials 串口索引
     * @return 帧ID值
     */
    public int getId(int serials) {
        return id[serials];  // 返回指定串口的帧ID
    }

    /**
     * 获取指定串口的LIN触发帧数据
     *
     * @param serials 串口索引
     * @return 帧数据值
     */
    public long getData(int serials) {
        return data[serials];  // 返回指定串口的帧数据
    }

    /**
     * 批量设置指定串口的LIN触发类型、帧ID和帧数据，值变化时通知UI刷新
     *
     * @param serials      串口索引
     * @param type         触发类型
     * @param id           帧ID
     * @param data         帧数据
     * @param isUpdataUI   是否通知UI刷新
     */
    public void setType(int serials, int type, int id, long data, boolean isUpdataUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;  // 校验type数组索引
        if(!ToolsSCPI.isCorrect(serials,this.id)) return;  // 校验id数组索引
        if(!ToolsSCPI.isCorrect(serials,this.data)) return;  // 校验data数组索引
//        if (this.type[serials] == type && this.id[serials] == id && this.data[serials] == data)
//            return;
        this.type[serials] = type;  // 更新触发类型
        this.id[serials] = id;  // 更新帧ID
        this.data[serials] = data;  // 更新帧数据
        switch (type) {  // 根据触发类型进行分支处理（目前无具体逻辑）

        }
        if (isUpdataUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_TYPE);  // 设置消息标志为LIN触发类型变更
            String param = String.valueOf(serials)  // 拼接参数：串口索引
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)  // + 分隔符 + 触发类型
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(id)  // + 分隔符 + 帧ID
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(data);  // + 分隔符 + 帧数据
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的LIN触发电平
     *
     * @param serials 串口索引
     * @return 触发电平值
     */
    public double getLevel(int serials) {
        return level[serials];  // 返回指定串口的触发电平
    }

    /**
     * 设置指定串口的LIN触发电平，值变化时通知UI刷新
     *
     * @param serials      串口索引
     * @param level        触发电平值
     * @param isUpdateUI   是否通知UI刷新
     */
    public void setLevel(int serials, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.level)) return;  // 校验串口索引是否合法
        if (this.level[serials] == level) return;  // 值未变化则直接返回
        this.level[serials] = level;  // 更新触发电平值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_LEVEL);  // 设置消息标志为LIN触发电平变更
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level);  // 拼接参数
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }
}
