package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                       Command_Trigger_Dwart                                  |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器矮脉宽触发命令处理模块                                        |
 * | 核心职责: 处理SCPI矮脉宽(DWARt)触发相关指令，包括触发源/脉冲极性/            |
 * |          脉宽限制条件/时间上下限/时间区间/高低电平的设置与查询                |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * |          高低电平设置时互相约束：高电平不能低于低电平，低电平不能高于高电平   |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层                        |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum                            |
 * | 使用场景: 远程控制矮脉宽触发参数、查询矮脉宽触发状态时使用                   |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Trigger_Dwart {

//   new SCPICommandStruct(":TRIGger:DWARt:SOURce","SCPI_Trigger_Dwart","Source"),//设置矮脉宽触发的触发源
//   new SCPICommandStruct(":TRIGger:DWARt:SOURce?","SCPI_Trigger_Dwart","SourceQ"),//查询矮脉宽触发的触发源
//   new SCPICommandStruct(":TRIGger:DWARt:POLarity","SCPI_Trigger_Dwart","Polarity"),//设置矮脉宽触发的脉冲极性
//   new SCPICommandStruct(":TRIGger:DWARt:POLarity?","SCPI_Trigger_Dwart","PolarityQ"),//查询矮脉宽触发的脉冲极性
//   new SCPICommandStruct(":TRIGger:DWARt:CONDition","SCPI_Trigger_Dwart","Condition"),//设置矮脉宽触发的脉宽限制条件
//   new SCPICommandStruct(":TRIGger:DWARt:CONDition?","SCPI_Trigger_Dwart","ConditionQ"),//查询矮脉宽触发的脉宽限制条件
//   new SCPICommandStruct(":TRIGger:DWARt:HTIMe","SCPI_Trigger_Dwart","HTime"),//设置矮脉宽触发时的时间上限
//   new SCPICommandStruct(":TRIGger:DWARt:HTIMe?","SCPI_Trigger_Dwart","HTimeQ"),//查询矮脉宽触发时的时间上限
//   new SCPICommandStruct(":TRIGger:DWARt:LTIMe","SCPI_Trigger_Dwart","LTime"),//设置矮脉宽触发时的时间下限
//   new SCPICommandStruct(":TRIGger:DWARt:LTIMe?","SCPI_Trigger_Dwart","LTimeQ"),//查询矮脉宽触发时的时间下限
//   new SCPICommandStruct(":TRIGger:DWARt:BTIMe","SCPI_Trigger_Dwart","BTime"),//设置矮脉宽触发时的时间区间
//   new SCPICommandStruct(":TRIGger:DWARt:BTIMe?","SCPI_Trigger_Dwart","BTimeQ"),//查询矮脉宽触发时的时间上限或下限
//   new SCPICommandStruct(":TRIGger:DWARt:HLEVel","SCPI_Trigger_Dwart","HLevel"),//设置矮脉宽触发时的高电平
//   new SCPICommandStruct(":TRIGger:DWARt:PLUS:HLEVel","SCPI_Trigger_Dwart","Plus_HLevel"),//设置矮脉宽触发时的高电平
//   new SCPICommandStruct(":TRIGger:DWARt:HLEVel?","SCPI_Trigger_Dwart","HLevelQ"),//查询矮脉宽触发时的高电平
//   new SCPICommandStruct(":TRIGger:DWARt:LLEVel","SCPI_Trigger_Dwart","LLevel"),//设置矮脉宽触发时的低电平
//   new SCPICommandStruct(":TRIGger:DWARt:PLUS:LLEVel","SCPI_Trigger_Dwart","Plus_LLevel"),//设置矮脉宽触发时的低电平
//   new SCPICommandStruct(":TRIGger:DWARt:LLEVel?","SCPI_Trigger_Dwart","LLevelQ"),//查询矮脉宽触发时的低电平

    private int source; // 触发源索引
    private int polar; // 脉冲极性索引
    private int condition; // 脉宽限制条件索引
    private double highTime; // 时间上限（8ns至10s）
    private double lowTime; // 时间下限（8ns至10s）
    private double highLevel; // 高电平值
    private double lowLevel; // 低电平值

    /**
     * 设置矮脉宽触发的触发源
     *
     * @param index       触发源索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Source(int index, boolean isUpdateUI) {
        if (source == index) return; // 值未变化则直接返回
        source = index; // 保存触发源
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_SOURCE); // 设置消息标志为矮脉宽触发源
            msgToUI.setParam(String.valueOf(index)); // 设置源参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询矮脉宽触发的触发源
     *
     * @return 触发源索引
     */
    public int SourceQ() {
        return source; // 返回触发源
    }

    /**
     * 设置矮脉宽触发的脉冲极性
     *
     * @param index       脉冲极性索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Polarity(int index, boolean isUpdateUI) {
        if (polar == index) return; // 值未变化则直接返回
        polar = index; // 保存脉冲极性
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_POLAR); // 设置消息标志为脉冲极性
            msgToUI.setParam(String.valueOf(index)); // 设置极性参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询矮脉宽触发的脉冲极性
     *
     * @return 脉冲极性索引
     */
    public int PolarityQ() {
        return polar; // 返回脉冲极性
    }

    /**
     * 设置脉宽限制条件
     *
     * @param index       限制条件索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Condition(int index, boolean isUpdateUI) {
        if (condition == index) return; // 值未变化则直接返回
        condition = index; // 保存限制条件
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_CONDITION); // 设置消息标志为限制条件
            msgToUI.setParam(String.valueOf(index)); // 设置条件参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询脉宽限制条件
     *
     * @return 限制条件索引
     */
    public int ConditionQ() {
        return condition; // 返回限制条件
    }

    /**
     * 设置矮脉宽触发时的时间上限
     *
     * @param highTime    时间上限值（8ns至10s）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void HTime(double highTime, boolean isUpdateUI) {
        this.highTime = highTime; // 保存时间上限
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_HTIME); // 设置消息标志为时间上限
            msgToUI.setParam(String.valueOf(highTime)); // 设置上限参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询矮脉宽触发时的时间上限
     *
     * @return 时间上限值
     */
    public double HTimeQ() {
        return highTime; // 返回时间上限
    }

    /**
     * 设置矮脉宽触发时的时间下限
     *
     * @param lowTime     时间下限值（8ns至10s）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void LTime(double lowTime, boolean isUpdateUI) {
        this.lowTime = lowTime; // 保存时间下限
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_LOWTIME); // 设置消息标志为时间下限
            msgToUI.setParam(String.valueOf(lowTime)); // 设置下限参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询矮脉宽触发时的时间下限
     *
     * @return 时间下限值
     */
    public double LTimeQ() {
        return lowTime; // 返回时间下限
    }

    /**
     * 设置矮脉宽触发时的时间区间（同时设置上限和下限）
     *
     * @param highTime    时间上限值（8ns至10s）
     * @param lowTime     时间下限值（8ns至10s）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void BTime(double highTime, double lowTime, boolean isUpdateUI) {
        this.highTime = highTime; // 保存时间上限
        this.lowTime = lowTime; // 保存时间下限
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_BTIME); // 设置消息标志为时间区间
            msgToUI.setParam(String.valueOf(highTime) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(lowTime)); // 拼接上限和下限参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询矮脉宽触发时的时间上限或下限
     *
     * @param highLow     0查询上限，1查询下限
     * @return 对应的时间值，无效参数返回0
     */
    public double BTimeQ(int highLow) {
        if (highLow == 0) { // 查询上限
            return highTime; // 返回时间上限
        } else if (highLow == 1) { // 查询下限
            return lowTime; // 返回时间下限
        }
        return 0; // 无效参数返回0
    }

    /**
     * 设置矮脉宽触发时的高电平。
     * 如果高电平低于低电平，会自动将低电平调整为高电平值，保证高电平>=低电平。
     *
     * @param highLevel   高电平值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void HLevel(double highLevel, boolean isUpdateUI) {
//        if (this.highLevel == highLevel) return;
        this.highLevel = highLevel; // 保存高电平
        if (isUpdateUI) { // 判断是否需要更新UI
            if (this.highLevel<this.lowLevel){ // 高电平低于低电平，违反约束
                LLevel(this.highLevel,isUpdateUI); // 自动将低电平调整为高电平值
            }

            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_HLEVEL); // 设置消息标志为高电平
            msgToUI.setParam(String.valueOf(highLevel)); // 设置高电平参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 递增/递减设置矮脉宽触发时的高电平
     *
     * @param index       1为加一个单位，-1为减一个单位
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_HLevel(int index, boolean isUpdateUI) {
        if (index == 1) { // 加一个单位
            highLevel++; // 高电平值加1
        } else if (index == -1) { // 减一个单位
            highLevel--; // 高电平值减1
        }
        HLevel(highLevel, isUpdateUI); // 调用HLevel设置新电平
    }

    /**
     * 查询矮脉宽触发时的高电平
     *
     * @return 高电平值
     */
    public double HLevelQ() {
        return highLevel; // 返回高电平
    }

    /**
     * 设置矮脉宽触发时的低电平。
     * 如果低电平高于高电平，会自动将高电平调整为低电平值，保证低电平<=高电平。
     *
     * @param lowLevel    低电平值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void LLevel(double lowLevel, boolean isUpdateUI) {
//        if (this.lowLevel == lowLevel) return;
        this.lowLevel = lowLevel; // 保存低电平
        if (isUpdateUI) { // 判断是否需要更新UI
            if (this.lowLevel>this.highLevel){ // 低电平高于高电平，违反约束
                HLevel(this.lowLevel,isUpdateUI); // 自动将高电平调整为低电平值
            }
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERRUNT_LLEVEL); // 设置消息标志为低电平
            msgToUI.setParam(String.valueOf(lowLevel)); // 设置低电平参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 递增/递减设置矮脉宽触发时的低电平
     *
     * @param index       1为加一个单位，-1为减一个单位
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_LLevel(int index, boolean isUpdateUI) {
        if (index == 1) { // 加一个单位
            lowLevel++; // 低电平值加1
        } else if (index == -1) { // 减一个单位
            lowLevel--; // 低电平值减1
        }
        LLevel(lowLevel, isUpdateUI); // 调用LLevel设置新电平
    }

    /**
     * 查询矮脉宽触发时的低电平
     *
     * @return 低电平值
     */
    public double LLevelQ() {
        return lowLevel; // 返回低电平
    }

}
