package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义

/*
 * +=============================================================================+
 * |                    Command_Trigger_Timeout - 超时触发命令模型                       |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，超时触发器的参数存储与UI同步                      |
 * | 核心职责 : 管理超时触发器的全部可配置参数（触发源、极性、超时时间、触发电平），         |
 * |            参数变更时通过RxBus通知UI层刷新                                           |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : CommandMsgToUI(消息封装)、RxBus/RxEnum(事件总线)                          |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置超时触发参数时，由SCPI分发器调用对应方法       |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Timeout {
//      new SCPICommandStruct(":TRIGger:TIMeout:SOURce","SCPI_Trigger_Timeout","Source"),//设置超时触发的触发源
//            new SCPICommandStruct(":TRIGger:TIMeout:SOURce?","SCPI_Trigger_Timeout","SourceQ"),//查询超时触发的触发源
//            new SCPICommandStruct(":TRIGger:TIMeout:POLarity","SCPI_Trigger_Timeout","Polarity"),//设置超时触发极性
//            new SCPICommandStruct(":TRIGger:TIMeout:POLarity?","SCPI_Trigger_Timeout","PolarityQ"),//查询超时触发极性
//            new SCPICommandStruct(":TRIGger:TIMeout:TIME","SCPI_Trigger_Timeout","Time"),//设置超时触发的超时时间
//            new SCPICommandStruct(":TRIGger:TIMeout:TIME?","SCPI_Trigger_Timeout","TimeQ"),//查询超时触发的超时时间
//            new SCPICommandStruct(":TRIGger:TIMeout:LEVel","SCPI_Trigger_Timeout","Level"),
//            new SCPICommandStruct(":TRIGger:TIMeout:LEVel?","SCPI_Trigger_Timeout","LevelQ"),

    private int source;  // 触发源通道索引
    /**
     * const char * trig_tim_pol[] = {
     * "POSitive",
     * "NEGative",
     * "EITHer",
     * NULL
     * };
     */
    private int polarity;  // 极性：0=正极性, 1=负极性, 2=任意
    private double time;  // 超时时间值
    private double level;  // 触发电平值

    /**
     * 设置超时触发的触发源
     *
     * @param source      触发源值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Source(int source, boolean isUpdateUI) {
        if (this.source == source) return;  // 值未变化则直接返回
        this.source = source;  // 更新触发源值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERTIMEOUT_SOURCE);  // 设置消息标志为超时触发源变更
            msgToUI.setParam(String.valueOf(source));  // 设置消息参数为触发源值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询超时触发的触发源
     *
     * @return 触发源值
     */
    public int SourceQ() {
        return source;  // 返回当前触发源值
    }

    /**
     * 设置超时触发极性
     *
     * @param polar       极性值（0=正极性, 1=负极性, 2=任意）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Polarity(int polar, boolean isUpdateUI) {
        if (this.polarity == polar) return;  // 值未变化则直接返回
        this.polarity = polar;  // 更新极性值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERTIMEOUT_POLAR);  // 设置消息标志为超时极性变更
            msgToUI.setParam(String.valueOf(polar));  // 设置消息参数为极性值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询超时触发极性
     *
     * @return 极性值
     */
    public int PolarityQ() {
        return polarity;  // 返回当前极性值
    }

    /**
     * 设置超时触发的超时时间
     *
     * @param time        超时时间值（8ns至10s）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Time(double time, boolean isUpdateUI) {
        if (Double.compare(this.time, time) == 0) return;  // 使用Double.compare比较浮点数，值未变化则直接返回
        this.time = time;  // 更新超时时间值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERTIMEOUT_TIME);  // 设置消息标志为超时时间变更
            msgToUI.setParam(String.valueOf(time));  // 设置消息参数为超时时间值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询超时触发的超时时间
     *
     * @return 超时时间值
     */
    public double TimeQ() {
        return time;  // 返回当前超时时间值
    }

    /**
     * 设置超时触发时的触发电平
     *
     * @param level       触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Level(double level, boolean isUpdateUI) {
        if (Double.compare(this.level, level) == 0) return;  // 使用Double.compare比较浮点数，值未变化则直接返回
        this.level = level;  // 更新触发电平值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERTIMEOUT_LEVEL);  // 设置消息标志为超时触发电平变更
            msgToUI.setParam(String.valueOf(level));  // 设置消息参数为触发电平值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 设置超时触发时的触发电平（步进调整）
     *
     * @param index       步进方向：1=递增，-1=递减
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Plus_Level(int index, boolean isUpdateUI) {
        if (index == 1) {  // 步进方向为递增
            level++;  // 触发电平值加1
        } else if (index == -1) {  // 步进方向为递减
            level--;  // 触发电平值减1
        }
        Level(level, isUpdateUI);  // 调用Level方法更新并通知UI
    }

    /**
     * 查询超时触发时的触发电平
     *
     * @return 触发电平值
     */
    public double LevelQ() {
        return level;  // 返回当前触发电平值
    }
}
