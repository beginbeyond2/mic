package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 通道常量定义

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                        Command_Trigger_Edge                                 |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器边沿触发命令处理模块                                         |
 * | 核心职责: 处理SCPI边沿触发相关指令，包括触发源设置/查询、边沿类型、触发电平   |
 * |          设置/查询、耦合方式设置/查询等                                      |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层                        |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum, TChan                     |
 * | 使用场景: 远程控制边沿触发参数、查询边沿触发状态时使用                       |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Trigger_Edge {
//     new SCPICommandStruct(":TRIGger:EDGE:SOURce","SCPI_Trigger_Edge","Source"),//选择边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:EDGE:SOURce?","SCPI_Trigger_Edge","SourceQ"),//查询边沿触发的触发源
//            new SCPICommandStruct(":TRIGger:EDGE:SLOPe","SCPI_Trigger_Edge","Slope"),//选择边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:EDGE:SLOPe?","SCPI_Trigger_Edge","SlopeQ"),//查询边沿触发的边沿类型
//            new SCPICommandStruct(":TRIGger:EDGE:LEVel","SCPI_Trigger_Edge","Level"),//设置边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:PLUS:LEVel","SCPI_Trigger_Edge","Plus_Level"),//设置边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:LEVel?","SCPI_Trigger_Edge","LevelQ"),//查询边沿触发时的触发电平
//            new SCPICommandStruct(":TRIGger:EDGE:COUPle","SCPI_Trigger_Edge","Couple"),//设置边沿触发耦合方式。
//            new SCPICommandStruct(":TRIGger:EDGE:COUPle?","SCPI_Trigger_Edge","CoupleQ"),//查询边沿触发耦合方式。

    private int source; // 触发源索引
    /**
     * const char * trig_edge_slop[] = {
     * "RISE",
     * "FALL",
     * "DUAL",
     * NULL
     * };
     */
    private int slope; // 边沿类型索引（0:RISE, 1:FALL, 2:DUAL）
    private double level; // 触发电平值
    /**
     * const char * trig_edge_coup[] = {
     * "DC",
     * "AC",
     * "HFRej",
     * "LFRej",
     * "NOISerej",
     * NULL
     * };
     */
    private int couple; // 耦合方式索引

    /**
     * 选择边沿触发的触发源
     *
     * @param index       触发源索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Source(int index, boolean isUpdateUI) {
//        if (source == index) return;
        source = index; // 保存触发源索引
        if (isUpdateUI) { // 判断是否需要更新UI
            index= TChan.ExtChToChIdxOfScpi(index); // 外部通道索引转换为SCPI通道索引
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGEREDGE_SOURCE); // 设置消息标志为边沿触发源
            msgToUI.setParam(String.valueOf(index)); // 设置源参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询边沿触发的触发源
     *
     * @return 触发源索引
     */
    public int SourceQ() {
        return source; // 返回触发源索引
    }

    /**
     * 选择边沿触发的边沿类型
     *
     * @param index       边沿类型索引（0:RISE, 1:FALL, 2:DUAL）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Slope(int index, boolean isUpdateUI) {
//        if (slope == index) return;
        slope = index; // 保存边沿类型
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGEREDGE_SLOPE); // 设置消息标志为边沿类型
            msgToUI.setParam(String.valueOf(index)); // 设置类型参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询边沿触发的边沿类型
     *
     * @return 边沿类型索引
     */
    public int SlopeQ() {
        return slope; // 返回边沿类型
    }

    /**
     * 设置边沿触发时的触发电平
     *
     * @param level       触发电平值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Level(double level, boolean isUpdateUI) {
        if (this.level == level) return; // 值未变化则直接返回
        this.level = level; // 保存触发电平
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGEREDGE_LEVEL); // 设置消息标志为触发电平
            msgToUI.setParam(String.valueOf(level)); // 设置电平参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 递增/递减设置边沿触发时的触发电平
     *
     * @param index       1为加一个单位，-1为减一个单位
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_Level(int index, boolean isUpdateUI) {
        if (index == 1) { // 加一个单位
            level++; // 电平值加1
        } else if (index == -1) { // 减一个单位
            level--; // 电平值减1
        }
        Level(level,isUpdateUI); // 调用Level设置新电平
    }

    /**
     * 查询边沿触发时的触发电平
     *
     * @return 触发电平值
     */
    public double LevelQ() {
        return level; // 返回触发电平
    }

    /**
     * 设置边沿触发耦合方式
     *
     * @param index       耦合方式索引（0:DC, 1:AC, 2:HFRej, 3:LFRej, 4:NOISerej）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Couple(int index, boolean isUpdateUI) {
        if (this.couple == index) return; // 值未变化则直接返回
        couple = index; // 保存耦合方式
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGEREDGE_COUPLE); // 设置消息标志为耦合方式
            msgToUI.setParam(String.valueOf(index)); // 设置方式参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询边沿触发耦合方式
     *
     * @return 耦合方式索引
     */
    public int CoupleQ() {
        return couple; // 返回耦合方式
    }


}
