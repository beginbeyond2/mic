package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义

/*
 * +=============================================================================+
 * |                   Command_Trigger_Nedge - 第N边沿触发命令模型                      |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，第N边沿触发器的参数存储与UI同步                   |
 * | 核心职责 : 管理第N边沿触发器的全部可配置参数（触发源、边沿类型、空闲时间、             |
 * |            边沿计数值N、触发电平），参数变更时通过RxBus通知UI层刷新                   |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : CommandMsgToUI(消息封装)、RxBus/RxEnum(事件总线)                          |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置第N边沿触发参数时，由SCPI分发器调用对应方法   |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Nedge {
//     new SCPICommandStruct(":TRIGger:NEDGe:SOURce","SCPI_Trigger_Nedge","Source"),//设置第N边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:NEDGe:SOURce?","SCPI_Trigger_Nedge","SourceQ"),//查询第N边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe","SCPI_Trigger_Nedge","Slope"),//设置第N边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe?","SCPI_Trigger_Nedge","SlopeQ"),//查询第N边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:NEDGe:IDLE","SCPI_Trigger_Nedge","Idle"),//设置第N边沿触发中开始边沿计数之前的空闲时间
//            new SCPICommandStruct(":TRIGger:NEDGe:IDLE?","SCPI_Trigger_Nedge","IdleQ"),//查询第N边沿触发中开始边沿计数之前的空闲时间
//            new SCPICommandStruct(":TRIGger:NEDGe:EDGE","SCPI_Trigger_Nedge","Edge"),//设置第N边沿触发的N的数值
//            new SCPICommandStruct(":TRIGger:NEDGe:EDGE?","SCPI_Trigger_Nedge","EdgeQ"),//查询第N边沿触发的N的数值
//            new SCPICommandStruct(":TRIGger:NEDGe:LEVel","SCPI_Trigger_Nedge","Level"),//设置第N边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:NEDGe:PLUS:LEVel","SCPI_Trigger_Nedge","Plus_Level"),//设置第N边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:NEDGe:LEVel?","SCPI_Trigger_Nedge","LevelQ"),//查询第N边沿触发时的触发电平

    private int source;  // 触发源通道索引
    /**
     * const char * trig_nedg_slop[] = {
     * "RISE",
     * "FALL",
     * NULL
     * };
     */
    private int slope;  // 边沿类型：0=上升沿(RISE), 1=下降沿(FALL)
    /**
     * const char * trig_nedg_slop[] = {
     * "RISE",
     * "FALL",
     * NULL
     * };
     */
    private double idle;  // 开始边沿计数之前的空闲时间（8ns至10s）
    private int edge;  // 第N边沿的N值（1至65535）
    private double level;  // 触发电平值

    /**
     * 设置第N边沿触发的触发源
     *
     * @param source      触发源值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Source(int source, boolean isUpdateUI) {
        if (this.source == source) return;  // 值未变化则直接返回
        this.source = source;  // 更新触发源值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_SOURCE);  // 设置消息标志为第N边沿触发源变更
            msgToUI.setParam(String.valueOf(source));  // 设置消息参数为触发源值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询第N边沿触发的触发源
     *
     * @return 触发源值
     */
    public int SourceQ() {
        return source;  // 返回当前触发源值
    }

    /**
     * 设置第N边沿触发的边沿类型
     *
     * @param slope       边沿类型（0=上升沿, 1=下降沿）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Slope(int slope, boolean isUpdateUI) {
//        if (this.slope == slope) return;
        this.slope = slope;  // 更新边沿类型值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_SLOPE);  // 设置消息标志为第N边沿边沿类型变更
            msgToUI.setParam(String.valueOf(slope));  // 设置消息参数为边沿类型值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询第N边沿触发的边沿类型
     *
     * @return 边沿类型值
     */
    public int SlopeQ() {
        return slope;  // 返回当前边沿类型值
    }

    /**
     * 设置第N边沿触发中开始边沿计数之前的空闲时间
     *
     * @param idle        空闲时间值（8ns至10s）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Idle(double idle, boolean isUpdateUI) {
        if (this.idle == idle) return;  // 值未变化则直接返回
        this.idle = idle;  // 更新空闲时间值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_IDLE);  // 设置消息标志为第N边沿空闲时间变更
            msgToUI.setParam(String.valueOf(idle));  // 设置消息参数为空闲时间值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询第N边沿触发中开始边沿计数之前的空闲时间
     *
     * @return 空闲时间值
     */
    public double IdleQ() {
        return idle;  // 返回当前空闲时间值
    }

    /**
     * 设置第N边沿触发的N的数值，包含范围校验（1-65535）
     *
     * @param edge        边沿计数值N（1至65535）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Edge(int edge, boolean isUpdateUI) {
        if (edge<=0) edge=1;  // 低于下限则钳位到1
        if (edge>=65535) edge=65535;  // 超过上限则钳位到65535
        this.edge = edge;  // 更新边沿计数值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_EDGE);  // 设置消息标志为第N边沿计数值变更
            msgToUI.setParam(String.valueOf(edge));  // 设置消息参数为边沿计数值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询第N边沿触发的N的数值
     *
     * @return 边沿计数值
     */
    public int EdgeQ() {
        return edge;  // 返回当前边沿计数值
    }

    /**
     * 设置第N边沿触发时的触发电平
     *
     * @param level       触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Level(double level, boolean isUpdateUI) {
        if (Double.compare(this.level,level) == 0) return;  // 使用Double.compare比较浮点数，值未变化则直接返回
        this.level = level;  // 更新触发电平值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERNEDGE_LEVEL);  // 设置消息标志为第N边沿触发电平变更
            msgToUI.setParam(String.valueOf(level));  // 设置消息参数为触发电平值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 设置第N边沿触发时的触发电平（步进调整）
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
     * 查询第N边沿触发时的触发电平
     *
     * @return 触发电平值
     */
    public double LevelQ() {
        return level;  // 返回当前触发电平值
    }
}
