package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义

/*
 * +=============================================================================+
 * |                     Command_Trigger_Slope - 斜率触发命令模型                        |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，斜率触发器的参数存储与UI同步                      |
 * | 核心职责 : 管理斜率触发器的全部可配置参数（触发源、触发沿、限制条件、                 |
 * |            时间上限/下限/区间、高/低电平），参数变更时通过RxBus通知UI层刷新           |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : CommandMsgToUI(消息封装)、RxBus/RxEnum(事件总线)                          |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置斜率触发参数时，由SCPI分发器调用对应方法       |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Slope {
//     new SCPICommandStruct(":TRIGger:SLOPe:SOURce","SCPI_Trigger_Slope","Source"),//设置斜率触发的触发源
//     new SCPICommandStruct(":TRIGger:SLOPe:SOURce?","SCPI_Trigger_Slope","SourceQ"),//查询斜率触发的触发源
//     new SCPICommandStruct(":TRIGger:SLOPe:EDGE","SCPI_Trigger_Slope","Edge"),//设置斜率触发沿
//     new SCPICommandStruct(":TRIGger:SLOPe:EDGE?","SCPI_Trigger_Slope","EdgeQ"),//查询斜率触发沿
//     new SCPICommandStruct(":TRIGger:SLOPe:CONDition","SCPI_Trigger_Slope","Condition"),//设置斜率触发的限制条件
//     new SCPICommandStruct(":TRIGger:SLOPe:CONDition?","SCPI_Trigger_Slope","ConditionQ"),//查询斜率触发的限制条件
//     new SCPICommandStruct(":TRIGger:SLOPe:HTIMe","SCPI_Trigger_Slope","HTime"),//设置斜率触发时的时间上限
//     new SCPICommandStruct(":TRIGger:SLOPe:HTIMe?","SCPI_Trigger_Slope","HTimeQ"),//查询斜率触发时的时间上限
//     new SCPICommandStruct(":TRIGger:SLOPe:LTIMe","SCPI_Trigger_Slope","LTime"),//设置斜率触发时的时间下限
//     new SCPICommandStruct(":TRIGger:SLOPe:LTIMe?","SCPI_Trigger_Slope","LTimeQ"),//查询斜率触发时的时间下限
//     new SCPICommandStruct(":TRIGger:SLOPe:BTIMe","SCPI_Trigger_Slope","BTime"),//设置斜率触发时的时间区间
//     new SCPICommandStruct(":TRIGger:SLOPe:BTIMe?","SCPI_Trigger_Slope","BTimeQ"),//查询斜率触发时的时间上限或下限
//     new SCPICommandStruct(":TRIGger:SLOPe:HLEVel","SCPI_Trigger_Slope","HLevel"),//设置斜率触发时的高电平
//     new SCPICommandStruct(":TRIGger:SLOPe:PLUS:HLEVel","SCPI_Trigger_Slope","Plus_HLevel"),//设置斜率触发时的高电平
//     new SCPICommandStruct(":TRIGger:SLOPe:HLEVel?","SCPI_Trigger_Slope","HLevelQ"),//查询斜率触发时的高电平
//     new SCPICommandStruct(":TRIGger:SLOPe:LLEVel","SCPI_Trigger_Slope","LLevel"),//设置斜率触发时的低电平
//     new SCPICommandStruct(":TRIGger:SLOPe:PLUS:LLEVel","SCPI_Trigger_Slope","Plus_LLevel"),//设置斜率触发时的低电平
//     new SCPICommandStruct(":TRIGger:SLOPe:LLEVel?","SCPI_Trigger_Slope","LLevelQ"),//查询斜率触发时的低电平

    private int source;  // 触发源通道索引
    /**
     * const char * trig_slop_edge[] = {
     * "RISE",
     * "FALL",
     * "EITHer",
     * NULL
     * };
     */
    private int edge;  // 触发沿类型：0=上升沿, 1=下降沿, 2=任意沿
    /**
     * const char * trig_slop_cond[] = {
     * "LESS",
     * "GREater",
     * "BETWeen",
     * NULL
     * };
     */
    private int condition;  // 限制条件：0=小于, 1=大于, 2=介于
    private double hTime;  // 斜率触发时间上限
    private double lTime;  // 斜率触发时间下限
    private double hLevel;  // 斜率触发高电平
    private double lLevel;  // 斜率触发低电平

    /**
     * 设置斜率触发的触发源
     *
     * @param source      触发源值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Source(int source, boolean isUpdateUI) {
        if (this.source == source) return;  // 值未变化则直接返回
        this.source = source;  // 更新触发源值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_SOURCE);  // 设置消息标志为斜率触发源变更
            msgToUI.setParam(String.valueOf(source));  // 设置消息参数为触发源值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询斜率触发的触发源
     *
     * @return 触发源值
     */
    public int SourceQ() {
        return source;  // 返回当前触发源值
    }

    /**
     * 设置斜率触发沿
     *
     * @param edge        触发沿类型（0=上升沿, 1=下降沿, 2=任意沿）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Edge(int edge, boolean isUpdateUI) {
        if (this.edge == edge) return;  // 值未变化则直接返回
        this.edge = edge;  // 更新触发沿类型值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_EDGE);  // 设置消息标志为斜率触发沿变更
            msgToUI.setParam(String.valueOf(edge));  // 设置消息参数为触发沿值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询斜率触发沿
     *
     * @return 触发沿类型值
     */
    public int EdgeQ() {
        return edge;  // 返回当前触发沿类型值
    }

    /**
     * 设置斜率触发的限制条件
     *
     * @param condition   限制条件（0=小于, 1=大于, 2=介于）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Condition(int condition, boolean isUpdateUI) {
        if (this.condition == condition) return;  // 值未变化则直接返回
        this.condition = condition;  // 更新限制条件值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_CONDITION);  // 设置消息标志为斜率条件变更
            msgToUI.setParam(String.valueOf(condition));  // 设置消息参数为条件值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询斜率触发的限制条件
     *
     * @return 限制条件值
     */
    public int ConditionQ() {
        return condition;  // 返回当前限制条件值
    }

    /**
     * 设置斜率触发时的时间上限
     *
     * @param highTime    时间上限值（8ns至10s）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void HTime(double highTime, boolean isUpdateUI) {
        this.hTime = highTime;  // 更新时间上限
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_HTIME);  // 设置消息标志为斜率时间上限变更
            msgToUI.setParam(String.valueOf(highTime));  // 设置消息参数为时间上限值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询斜率触发时的时间上限
     *
     * @return 时间上限值
     */
    public double HTimeQ() {
        return hTime;  // 返回当前时间上限值
    }

    /**
     * 设置斜率触发时的时间下限
     *
     * @param lowTime     时间下限值（8ns至10s）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void LTime(double lowTime, boolean isUpdateUI) {
        this.lTime = lowTime;  // 更新时间下限
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_LTIME);  // 设置消息标志为斜率时间下限变更
            msgToUI.setParam(String.valueOf(lowTime));  // 设置消息参数为时间下限值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询斜率触发时的时间下限
     *
     * @return 时间下限值
     */
    public double LTimeQ() {
        return lTime;  // 返回当前时间下限值
    }

    /**
     * 设置斜率触发时的时间区间
     *
     * @param highTime    时间上限值（8ns至10s）
     * @param lowTime     时间下限值（8ns至10s）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void BTime(double highTime, double lowTime, boolean isUpdateUI) {
        this.hTime = highTime;  // 更新时间上限
        this.lTime = lowTime;  // 更新时间下限
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_BTIME);  // 设置消息标志为斜率时间区间变更
            msgToUI.setParam(String.valueOf(highTime) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(lowTime));  // 拼接参数：上限+下限
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询斜率触发时的时间上限或下限
     *
     * @param highLow 0=查询上限，1=查询下限
     * @return 对应的时间值
     */
    public double BTimeQ(int highLow) {
        if (highLow == 0) {  // 查询上限
            return hTime;  // 返回时间上限
        } else if (highLow == 1) {  // 查询下限
            return lTime;  // 返回时间下限
        }
        return 0;  // 无效参数返回0
    }

    /**
     * 设置斜率触发时的高电平，若高电平低于低电平则自动调整低电平
     *
     * @param highLevel   高电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void HLevel(double highLevel, boolean isUpdateUI) {
        this.hLevel = highLevel;  // 更新高电平值
        if (isUpdateUI) {  // 需要通知UI刷新
            if (this.hLevel<this.lLevel){  // 高电平低于低电平，需要调整低电平
                LLevel(this.hLevel,isUpdateUI);  // 将低电平调整为与高电平相同
            }
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_HLEVEL);  // 设置消息标志为斜率高电平变更
            msgToUI.setParam(String.valueOf(highLevel));  // 设置消息参数为高电平值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 设置斜率触发时的高电平（步进调整）
     *
     * @param index       步进方向：1=递增，-1=递减
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Plus_HLevel(int index, boolean isUpdateUI) {
        if (index == 1) {  // 步进方向为递增
            hLevel++;  // 高电平值加1
        } else if (index == -1) {  // 步进方向为递减
            hLevel--;  // 高电平值减1
        }
        HLevel(hLevel, isUpdateUI);  // 调用HLevel方法更新并通知UI
    }

    /**
     * 查询斜率触发时的高电平
     *
     * @return 高电平值
     */
    public double HLevelQ() {
        return hLevel;  // 返回当前高电平值
    }

    /**
     * 设置斜率触发时的低电平，若低电平高于高电平则自动调整高电平
     *
     * @param lowLevel    低电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void LLevel(double lowLevel, boolean isUpdateUI) {
        this.lLevel = lowLevel;  // 更新低电平值
        if (isUpdateUI) {  // 需要通知UI刷新
            if (this.lLevel>this.hLevel){  // 低电平高于高电平，需要调整高电平
                HLevel(this.lLevel,isUpdateUI);  // 将高电平调整为与低电平相同
            }
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSLOPE_LLEVEL);  // 设置消息标志为斜率低电平变更
            msgToUI.setParam(String.valueOf(lowLevel));  // 设置消息参数为低电平值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 设置斜率触发时的低电平（步进调整）
     *
     * @param index       步进方向：1=递增，-1=递减
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Plus_LLevel(int index, boolean isUpdateUI) {
        if (index == 1) {  // 步进方向为递增
            lLevel++;  // 低电平值加1
        } else if (index == -1) {  // 步进方向为递减
            lLevel--;  // 低电平值减1
        }
        LLevel(lLevel, isUpdateUI);  // 调用LLevel方法更新并通知UI
    }

    /**
     * 查询斜率触发时的低电平
     *
     * @return 低电平值
     */
    public double LLevelQ() {
        return lLevel;  // 返回当前低电平值
    }


}
