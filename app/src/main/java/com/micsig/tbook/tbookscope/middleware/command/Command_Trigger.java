package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

import com.micsig.tbook.scope.Scope; // 示波器核心作用域
import com.micsig.tbook.tbookscope.main.dialog.DialogManage; // 对话框管理
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                          Command_Trigger                                    |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器触发命令处理模块                                             |
 * | 核心职责: 处理SCPI触发相关指令，包括触发类型设置/查询、释抑时间、触发方式      |
 * |          （自动/普通）、触发状态查询、串行总线类型、数据确认对话框等           |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层;                        |
 * |          状态查询时从Scope读取运行状态                                        |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum, Scope, DialogManage        |
 * | 使用场景: 远程控制触发参数、查询触发状态时使用                               |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Trigger {
//     new SCPICommandStruct(":TRIGger:TYPE","SCPI_Trigger","Type"),//选择触发类型
//            new SCPICommandStruct(":TRIGger:TYPE?","SCPI_Trigger","TypeQ"),//查询返回当前使用的触发类型
//            new SCPICommandStruct(":TRIGger:HOLDoff","SCPI_Trigger","HoldOff"),//设置触发释抑时间
//            new SCPICommandStruct(":TRIGger:HOLDoff?","SCPI_Trigger","HoldOffQ"),//查询以科学计数形式返回触发释抑时间
//            new SCPICommandStruct(":TRIGger:MODE","SCPI_Trigger","Mode"),//设置触发方式：自动或普通
//            new SCPICommandStruct(":TRIGger:MODE?","SCPI_Trigger","ModeQ"),//查询触发方式
//            new SCPICommandStruct(":TRIGger:STATus?","SCPI_Trigger","StatusQ"),//查询当前的触发状态

    /**
     * const char * trig_type[] = {
     * "EDGE",
     * "PULSe",
     * "LOGic",
     * "DWARt",
     * "SLOPe",
     * "TIMeout",
     * "NEDGe",
     * "VIDeo",
     * NULL
     * };
     */
    private int triggerType; // 触发类型索引
    private double holdOff; // 触发释抑时间（秒）
    /**
     * const char * trig_mode[] = {
     * "AUTO",
     * "NORMal",
     * NULL
     * };
     */
    private int mode; // 触发方式索引（0:AUTO, 1:NORMal）
    /**
     * const char * trig_stat[] = {
     * "STOP",
     * "RUN",
     * "WAIT",
     * "AUTO",
     * NULL
     * };
     */
    private int state; // 触发状态索引（0:STOP, 1:RUN, 2:WAIT, 3:AUTO）

    public static int SerialBusType_IMG=0; // 串行总线类型：图像
    public static int SerialBusType_TXT=1; // 串行总线类型：文本
    /**
     * const char * trig_serialbus_type={
     *     "IMG",
     *     "TXT"
     * };
     */
    private int serialBus_type; // 串行总线显示类型索引

    /**
     * 设置触发类型
     *
     * @param index       触发类型索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Type(int index, boolean isUpdateUI) {
//        if (triggerType == index) return;
        triggerType = index; // 保存触发类型索引
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGER_TYPE); // 设置消息标志为触发类型
            msgToUI.setParam(String.valueOf(index)); // 设置类型参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询触发类型
     *
     * @return 触发类型索引
     */
    public int TypeQ() {
        return triggerType; // 返回触发类型索引
    }

    /**
     * 设置触发释抑时间
     *
     * @param holdOff     释抑时间（200ns至10s，单位s）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void HoldOff(double holdOff, boolean isUpdateUI) {
//        if (this.holdOff == holdOff) return;
        this.holdOff = holdOff; // 保存释抑时间
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGER_HOLDOFF); // 设置消息标志为释抑时间
            msgToUI.setParam(String.valueOf(holdOff)); // 设置释抑时间参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询触发释抑时间
     *
     * @return 释抑时间（秒）
     */
    public double HoldOffQ() {
        return holdOff; // 返回释抑时间
    }

    /**
     * 设置触发方式：自动或普通
     *
     * @param index       触发方式索引（0:AUTO, 1:NORMal）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Mode(int index, boolean isUpdateUI) {
        if (this.mode == index) return; // 值未变化则直接返回
        mode = index; // 保存触发方式
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGER_MODE); // 设置消息标志为触发方式
            msgToUI.setParam(String.valueOf(index)); // 设置方式参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询触发方式
     *
     * @return 触发方式索引
     */
    public int ModeQ() {
        return mode; // 返回触发方式
    }

    /**
     * 设置触发状态（内部使用）
     *
     * @param state 触发状态索引
     */
    public synchronized void Status(int state){
        this.state=state; // 保存触发状态
    }

    /**
     * 查询当前的触发状态
     * "STOP","RUN","WAIT","AUTO"
     *
     * @return 触发状态索引
     */
    public int StatusQ() {
        Scope scope = Scope.getInstance(); // 获取Scope单例
        //FPGA返回
        if (scope.isSingle()){ // 单次模式
            //雅达客户要求这样
            if(scope.isInSlowScaleMode()){ // 慢扫模式
                return state; // 返回实际状态
            }
            return 2; // 单次模式下返回WAIT(2)
        }else if (scope.isAuto()){ // 自动模式
            return 3; // 返回AUTO(3)
        }else { // 其他模式
            return state; // 返回实际状态
        }
    }

    /**
     * 设置串行总线显示类型
     *
     * @param serialBus_type 显示类型索引（0:IMG, 1:TXT）
     * @param isUpdateUI     是否同步更新UI界面
     */
    public void SerialBus_Type(int serialBus_type, boolean isUpdateUI){
        if (this.serialBus_type==serialBus_type) return; // 值未变化则直接返回
        this.serialBus_type=serialBus_type; // 保存串行总线类型
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGER_SERIALBUS_TYPE); // 设置消息标志为串行总线类型
            msgToUI.setParam(String.valueOf(serialBus_type)); // 设置类型参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询串行总线显示类型
     *
     * @return 显示类型索引
     */
    public int SerialBus_TypeQ(){
        return this.serialBus_type; // 返回串行总线类型
    }

    /**
     * 查询是否有数据确认对话框显示
     *
     * @return 是否有确认对话框
     */
    public boolean HasDataQ(){
        return DialogManage.getIns().getDialogOkCancel().isShow(); // 查询确认对话框是否显示
    }

    /**
     * 选择确认对话框的结果
     *
     * @param IsYes       是否选择"是"
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void SelectData(boolean IsYes,boolean isUpdateUI){
        if (IsYes) { // 选择"是"
            DialogManage.getIns().getDialogOkCancel().pressOK(); // 按下确认按钮
        }else{ // 选择"否"
            DialogManage.getIns().getDialogOkCancel().PressCancel(); // 按下取消按钮
        }
    }

}
