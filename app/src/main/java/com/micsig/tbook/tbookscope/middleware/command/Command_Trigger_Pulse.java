package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义

/*
 * +=============================================================================+
 * |                    Command_Trigger_Pulse - 脉宽触发命令模型                        |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，脉宽触发器的参数存储与UI同步                      |
 * | 核心职责 : 管理脉宽触发器的全部可配置参数（触发源、极性、脉冲宽度、时间上限/下限、       |
 * |            触发条件、触发电平），参数变更时通过RxBus通知UI层刷新                      |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : CommandMsgToUI(消息封装)、RxBus/RxEnum(事件总线)                          |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置脉宽触发参数时，由SCPI分发器调用对应方法       |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Pulse {
//     new SCPICommandStruct(":TRIGger:PULSe:SOURce","SCPI_Trigger_Pulse","Source"),//选择脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:PULSe:SOURce?","SCPI_Trigger_Pulse","SourceQ"),//查询脉宽触发的触发源
//            new SCPICommandStruct(":TRIGger:PULSe:POLarity","SCPI_Trigger_Pulse","Polarity"),//设置脉宽触发的极性
//            new SCPICommandStruct(":TRIGger:PULSe:POLarity?","SCPI_Trigger_Pulse","PolarityQ"),//查询脉宽触发的极性
//            new SCPICommandStruct(":TRIGger:PULSe:WIDTh","SCPI_Trigger_Pulse","Width"),//设置脉宽触发时的脉冲宽度值
//            new SCPICommandStruct(":TRIGger:PULSe:WIDTh?","SCPI_Trigger_Pulse","WidthQ"),//查询脉宽触发时的脉冲宽度值
//            new SCPICommandStruct(":TRIGger:PULSe:CONDition","SCPI_Trigger_Pulse","Condition"),//设置脉宽触发条件
//            new SCPICommandStruct(":TRIGger:PULSe:CONDition?","SCPI_Trigger_Pulse","ConditionQ"),//查询脉宽触发条件
//            new SCPICommandStruct(":TRIGger:PULSe:LEVel","SCPI_Trigger_Pulse","Level"),//设置脉宽触发时的触发电平
//            new SCPICommandStruct(":TRIGger:PULSe:PLUS:LEVel","SCPI_Trigger_Pulse","Plus_Level"),//设置脉宽触发时的触发电平
//            new SCPICommandStruct(":TRIGger:PULSe:LEVel?","SCPI_Trigger_Pulse","LevelQ"),//查询脉宽触发时的触发电平

    private int source;  // 触发源通道索引
    /**
     * const char * trig_puls_pol[] = {
     * "POSitive",
     * "NEGative",
     * NULL
     * };
     */
    private int polarity;  // 脉冲极性：0=正极性(POSitive), 1=负极性(NEGative)
    private double width;  // 脉冲宽度值
    private double highTime;  // 脉宽时间上限
    private double lowTime;  // 脉宽时间下限
    /**
     * const char * trig_puls_cond[] = {
     * "LESS",
     * "GREat",
     * "EQUal",
     * "UNEQual",
     * NULL
     * };
     */
    private int condition;  // 触发条件：0=小于, 1=大于, 2=等于, 3=不等于
    private double level;  // 触发电平值

    /**
     * 选择脉宽触发的触发源
     *
     * @param source      触发源值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Source(int source, boolean isUpdateUI) {
//        if (this.source == source) return;
        this.source = source;  // 更新触发源值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_SOURCE);  // 设置消息标志为脉宽触发源变更
            msgToUI.setParam(String.valueOf(source));  // 设置消息参数为触发源值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询脉宽触发的触发源
     *
     * @return 触发源值
     */
    public int SourceQ() {
        return source;  // 返回当前触发源值
    }

    /**
     * 设置脉宽触发的极性
     *
     * @param polar       极性值（0=正极性, 1=负极性）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Polarity(int polar, boolean isUpdateUI) {
        if (this.polarity == polar) return;  // 值未变化则直接返回
        this.polarity = polar;  // 更新极性值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_POLAR);  // 设置消息标志为脉宽极性变更
            msgToUI.setParam(String.valueOf(polar));  // 设置消息参数为极性值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询脉宽触发的极性
     *
     * @return 极性值
     */
    public int PolarityQ() {
        return polarity;  // 返回当前极性值
    }


    /**
     * 设置脉宽触发时的时间上限
     *
     * @param highTime    时间上限值（8ns至10s）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void HTime(double highTime, boolean isUpdateUI) {
        this.highTime = highTime;  // 更新时间上限
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_HTIME);  // 设置消息标志为脉宽时间上限变更
            msgToUI.setParam(String.valueOf(highTime));  // 设置消息参数为时间上限值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询脉宽触发时的时间上限
     *
     * @return 时间上限值
     */
    public double HTimeQ() {
        return highTime;  // 返回当前时间上限值
    }

    /**
     * 设置逻辑触发时的时间下限
     *
     * @param lowTime     时间下限值（8ns至10s）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void LTime(double lowTime, boolean isUpdateUI) {
        this.lowTime = lowTime;  // 更新时间下限
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_LTIME);  // 设置消息标志为脉宽时间下限变更
            msgToUI.setParam(String.valueOf(lowTime));  // 设置消息参数为时间下限值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }


    /**
     * 查询脉宽触发时的时间下限
     *
     * @return 时间下限值
     */
    public double LTimeQ() {
        return lowTime;  // 返回当前时间下限值
    }


    /**
     * 设置脉宽触发时的脉冲宽度值
     *
     * @param width       脉冲宽度值（40ns至10s）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Width(double width, boolean isUpdateUI) {
//        if (Double.compare(this.width, width) == 0) return;
        this.width = width;  // 更新脉冲宽度值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_WIDTH);  // 设置消息标志为脉宽宽度变更
            msgToUI.setParam(String.valueOf(width));  // 设置消息参数为脉冲宽度值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询脉宽触发时的脉冲宽度值
     *
     * @return 脉冲宽度值
     */
    public double WidthQ() {
        return width;  // 返回当前脉冲宽度值
    }

    /**
     * 设置脉宽触发条件
     *
     * @param condition   触发条件（0=小于, 1=大于, 2=等于, 3=不等于）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Condition(int condition, boolean isUpdateUI) {
        if (this.condition == condition) return;  // 值未变化则直接返回
        this.condition = condition;  // 更新触发条件值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_CONDITION);  // 设置消息标志为脉宽条件变更
            msgToUI.setParam(String.valueOf(condition));  // 设置消息参数为条件值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询脉宽触发条件
     *
     * @return 触发条件值
     */
    public int ConditionQ() {
        return condition;  // 返回当前触发条件值
    }

    /**
     * 设置脉宽触发时的触发电平
     *
     * @param level       触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Level(double level, boolean isUpdateUI) {
        if (Double.compare(this.level,level) == 0) return;  // 使用Double.compare比较浮点数，值未变化则直接返回
        this.level = level;  // 更新触发电平值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERPULSE_LEVEL);  // 设置消息标志为脉宽触发电平变更
            msgToUI.setParam(String.valueOf(level));  // 设置消息参数为触发电平值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 设置脉宽触发时的触发电平（步进调整）
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
     * 查询脉宽触发时的触发电平
     *
     * @return 触发电平值
     */
    public double LevelQ() {
        return level;  // 返回当前触发电平值
    }
}
