package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义

/*
 * +=============================================================================+
 * |                     Command_Trigger_Logic - 逻辑触发命令模型                       |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，逻辑触发器的参数存储与UI同步                      |
 * | 核心职责 : 管理逻辑触发器的全部可配置参数（各通道逻辑状态、比较函数、触发条件、           |
 * |            时间参数、触发电平），参数变更时通过RxBus通知UI层刷新                      |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : CommandMsgToUI(消息封装)、RxBus/RxEnum(事件总线)                          |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置逻辑触发参数时，由SCPI分发器调用对应方法       |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Logic {
//     new SCPICommandStruct(":TRIGger:LOGic:STATus","SCPI_Trigger_Logic","Status"),//设置逻辑触发中通道的逻辑状态
//     new SCPICommandStruct(":TRIGger:LOGic:STATus?","SCPI_Trigger_Logic","StatusQ"),//查询逻辑触发中通道的逻辑状态
//     new SCPICommandStruct(":TRIGger:LOGic:FUNCtion","SCPI_Trigger_Logic","Function"),//设置逻辑触发的比较函数
//     new SCPICommandStruct(":TRIGger:LOGic:FUNCtion?","SCPI_Trigger_Logic","FunctionQ"),//查询逻辑触发的比较函数
//     new SCPICommandStruct(":TRIGger:LOGic:CONDition","SCPI_Trigger_Logic","Condition"),//设置逻辑触发条件
//     new SCPICommandStruct(":TRIGger:LOGic:CONDition?","SCPI_Trigger_Logic","ConditionQ"),//查询逻辑触发条件
//     new SCPICommandStruct(":TRIGger:LOGic:TIME","SCPI_Trigger_Logic","Time"),//设置触发逻辑时间
//     new SCPICommandStruct(":TRIGger:LOGic:TIME?","SCPI_Trigger_Logic","TimeQ"),//查询触发逻辑时间
//     new SCPICommandStruct(":TRIGger:LOGic:LEVel","SCPI_Trigger_Logic","Level"),//设置逻辑触发时的各通道触发电平
//     new SCPICommandStruct(":TRIGger:LOGic:PLUS:LEVel","SCPI_Trigger_Logic","Plus_Level"),//设置逻辑触发时的各通道触发电平
//     new SCPICommandStruct(":TRIGger:LOGic:LEVel?","SCPI_Trigger_Logic","LevelQ"),//查询逻辑触发时的各通道触发电平

    /**
     * const char * trig_log_stat[] = {
     * "HIGH",
     * "LOW",
     * "NONE",
     * NULL
     * };
     */
    private final int[] status = new int[]{2, 2, 2, 2, 2, 2, 2, 2};  // 各通道逻辑状态数组，0=HIGH, 1=LOW, 2=NONE，默认NONE
    /**
     * const char * trig_log_fun[] = {
     * "AND",
     * "OR",
     * "NAND",
     * "NOR",
     * NULL
     * };
     */
    private int function;  // 比较函数索引：0=AND, 1=OR, 2=NAND, 3=NOR
    /**
     * const char * trig_log_cond[] = {
     * "LESS",
     * "GREat",
     * "EQUal",
     * "UNEQual",
     * "TRUE",
     * "FALSe",
     * NULL
     * };
     */
    private int condition;  // 触发条件索引：0=LESS, 1=GREater, 2=EQUal, 3=UNEQual, 4=TRUE, 5=FALSe
    private double time;  // 触发逻辑时间值
    private double highTime;  // 逻辑触发时间上限
    private double lowTime;  // 逻辑触发时间下限
    private final double[] level = new double[]{0, 0, 0, 0, 0, 0, 0, 0};  // 各通道触发电平数组

    /**
     * 设置逻辑触发中通道的逻辑状态
     *
     * @param channel    通道索引（0-7）
     * @param status     逻辑状态值（0=HIGH, 1=LOW, 2=NONE）
     * @param isUpdateUI 是否通知UI刷新
     */
    public void Status(int channel, int status, boolean isUpdateUI) {
        if (this.status[channel] == status) return;  // 值未变化则直接返回
        this.status[channel] = status;  // 更新指定通道的逻辑状态
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_STATUS);  // 设置消息标志为逻辑状态变更
            msgToUI.setParam(String.valueOf(channel) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(status));  // 拼接参数：通道+状态
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询逻辑触发中通道的逻辑状态
     *
     * @param channnel 通道索引（0-7）
     * @return 逻辑状态值
     */
    public int StatusQ(int channnel) {
        return status[channnel];  // 返回指定通道的逻辑状态
    }

    /**
     * 设置逻辑触发的比较函数
     *
     * @param index      比较函数索引（0=AND, 1=OR, 2=NAND, 3=NOR）
     * @param isUpdateUI 是否通知UI刷新
     */
    public void Function(int index, boolean isUpdateUI) {
        if (this.function == index) return;  // 值未变化则直接返回
        function = index;  // 更新比较函数索引
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_FUNCTION);  // 设置消息标志为比较函数变更
            msgToUI.setParam(String.valueOf(index));  // 设置消息参数为函数索引
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询逻辑触发的比较函数
     *
     * @return 比较函数索引
     */
    public int FunctionQ() {
        return function;  // 返回当前比较函数索引
    }

    /**
     * 设置逻辑触发条件
     *
     * @param index      触发条件索引（0=LESS, 1=GREater, 2=EQUal, 3=UNEQual, 4=TRUE, 5=FALSe）
     * @param isUpdateUI 是否通知UI刷新
     */
    public void Condition(int index, boolean isUpdateUI) {
        if (condition == index) return;  // 值未变化则直接返回
        condition = index;  // 更新触发条件索引
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_CONDITION);  // 设置消息标志为触发条件变更
            msgToUI.setParam(String.valueOf(index));  // 设置消息参数为条件索引
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询逻辑触发条件
     *
     * @return 触发条件索引
     */
    public int ConditionQ() {
        return condition;  // 返回当前触发条件索引
    }

    /**
     * 设置触发逻辑时间
     *
     * @param time       时间值（200ns至10s）
     * @param isUpdateUI 是否通知UI刷新
     */
    public void Time(double time, boolean isUpdateUI) {
//        if (this.time == time) return;
        this.time = time;  // 更新触发逻辑时间
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_TIME);  // 设置消息标志为逻辑时间变更
            msgToUI.setParam(String.valueOf(time));  // 设置消息参数为时间值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询触发逻辑时间
     *
     * @return 触发逻辑时间值
     */
    public double TimeQ() {
        return time;  // 返回当前触发逻辑时间
    }

    /**
     * 设置逻辑触发时的时间上限
     *
     * @param highTime   时间上限值（8ns至10s）
     * @param isUpdateUI 是否通知UI刷新
     */
    public void HTime(double highTime, boolean isUpdateUI) {
        this.highTime = highTime;  // 更新时间上限
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_HTIME);  // 设置消息标志为时间上限变更
            msgToUI.setParam(String.valueOf(highTime));  // 设置消息参数为时间上限值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询逻辑触发时的时间上限
     *
     * @return 时间上限值
     */
    public double HTimeQ() {
        return highTime;  // 返回当前时间上限
    }

    /**
     * 设置逻辑触发时的时间下限
     *
     * @param lowTime    时间下限值（8ns至10s）
     * @param isUpdateUI 是否通知UI刷新
     */
    public void LTime(double lowTime, boolean isUpdateUI) {
        this.lowTime = lowTime;  // 更新时间下限
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_LOWTIME);  // 设置消息标志为时间下限变更
            msgToUI.setParam(String.valueOf(lowTime));  // 设置消息参数为时间下限值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }


    /**
     * 查询逻辑触发时的时间下限
     *
     * @return 时间下限值
     */
    public double LTimeQ() {
        return lowTime;  // 返回当前时间下限
    }


    /**
     * 设置逻辑触发时的时间区间
     *
     * @param highTime   时间上限值（8ns至10s）
     * @param lowTime    时间下限值（8ns至10s）
     * @param isUpdateUI 是否通知UI刷新
     */
    public void BTime(double highTime, double lowTime, boolean isUpdateUI) {
        this.highTime = highTime;  // 更新时间上限
        this.lowTime = lowTime;  // 更新时间下限
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_BTIME);  // 设置消息标志为时间区间变更
            msgToUI.setParam(String.valueOf(highTime) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(lowTime));  // 拼接参数：上限+下限
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询逻辑触发时的时间上限或下限
     *
     * @param highLow 0=查询上限，1=查询下限
     * @return 对应的时间值
     */
    public double BTimeQ(int highLow) {
        if (highLow == 0) {  // 查询上限
            return highTime;  // 返回时间上限
        } else if (highLow == 1) {  // 查询下限
            return lowTime;  // 返回时间下限
        }
        return 0;  // 无效参数返回0
    }


    /**
     * 设置逻辑触发时的各通道触发电平
     *
     * @param channel    通道索引（0-7）
     * @param level      触发电平值
     * @param isUpdateUI 是否通知UI刷新
     */
    public void Level(int channel, double level, boolean isUpdateUI) {
//        if (this.level[channel] == level) return;
        this.level[channel] = level;  // 更新指定通道的触发电平
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLOGIC_LEVEL);  // 设置消息标志为触发电平变更
            msgToUI.setParam(String.valueOf(channel) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));  // 拼接参数：通道+电平
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 设置逻辑触发时的各通道触发电平（步进调整）
     *
     * @param channel    通道索引（0-7）
     * @param plus       步进方向：1=递增，-1=递减
     * @param isUpdateUI 是否通知UI刷新
     */
    public void Plus_Level(int channel, int plus, boolean isUpdateUI) {
        if (plus == 1) {  // 步进方向为递增
            level[channel]++;  // 通道电平值加1
        } else if (plus == -1) {  // 步进方向为递减
            level[channel]--;  // 通道电平值减1
        }
        Level(channel, level[channel], isUpdateUI);  // 调用Level方法更新并通知UI
    }

    /**
     * 查询逻辑触发时的各通道触发电平
     *
     * @param channel 通道索引（0-7）
     * @return 触发电平值
     */
    public double LevelQ(int channel) {
        return level[channel];  // 返回指定通道的触发电平
    }


}
