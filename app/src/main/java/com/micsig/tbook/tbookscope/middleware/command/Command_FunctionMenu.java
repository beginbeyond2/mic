package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI功能菜单命令处理

import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举

/**
 * Created by liwb on 2018/1/17.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                          Command_FunctionMenu                                |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 功能菜单(FunctionMenu)命令处理层                 |
 * | 核心职责：实现示波器运行控制相关的SCPI指令，包括自动设置、运行、停止、      |
 * |          单次、连续、蜂鸣等控制命令，以及SCPI协议版本查询                   |
 * | 架构设计：属于Command子模块，由Command单例统一调度；                         |
 * |          所有方法均为命令式操作，通过RxBus发送UI事件驱动示波器状态变更       |
 * | 数据流向：SCPI指令 → Command_FunctionMenu → CommandMsgToUI → RxBus → UI层  |
 * | 依赖关系：Command(单例入口)、RxBus(事件总线)                                |
 * | 使用场景：远程SCPI控制示波器的运行/停止/自动/单次等基本操作                 |
 * +-----------------------------------------------------------------------------+
 */
public class Command_FunctionMenu {
    //     new SCPICommandStruct(":AUTO","SCPI_FunctionMenu","Auto"),//自动
//     new SCPICommandStruct(":RUN","SCPI_FunctionMenu","Run"),//使示波器开始运行，符合触发条件，开始采集数据
//     new SCPICommandStruct(":STOP","SCPI_FunctionMenu","Stop"),//使示波器停止运行，数据采集停止
//     new SCPICommandStruct(":SINGle","SCPI_FunctionMenu","Single"),//将示波器设置为单序列，示波器捕获并显示单次采集
//     new SCPICommandStruct(":MULTiple","SCPI_FunctionMenu","Multiple"),//将示波器设置为连续触发方式
//     new SCPICommandStruct(":BEEP","SCPI_FunctionMenu","Beep"),//设置示波器的蜂鸣状态

    /**
     * 执行自动设置
     * 对应SCPI指令: :AUTO
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Auto(boolean isUpdateUI){
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUTO); // 设置消息标志为自动设置
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 使示波器开始运行，符合触发条件开始采集数据
     * 对应SCPI指令: :RUN
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Run(boolean isUpdateUI){
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_RUN); // 设置消息标志为运行
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 使示波器停止运行，数据采集停止
     * 对应SCPI指令: :STOP
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Stop(boolean isUpdateUI){
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_STOP); // 设置消息标志为停止
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 将示波器设置为单次触发模式
     * 对应SCPI指令: :SINGle
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Single(boolean isUpdateUI){
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_SINGLE); // 设置消息标志为单次
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 将示波器设置为连续触发方式
     * 对应SCPI指令: :MULTiple
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Multiple(boolean isUpdateUI){
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_MULTIPLE); // 设置消息标志为连续
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 设置示波器的蜂鸣状态
     * 对应SCPI指令: :BEEP
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Beep(boolean isUpdateUI){
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_BEEP); // 设置消息标志为蜂鸣
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询SCPI协议版本号
     * 20250707 起始版本
     * 2025-9-17 关键channel1:label? 返回为空时，显示NONE.
     * @return 版本号字符串
     */
    public String Version(){
        return "20250917"; // 返回当前SCPI协议版本号
    }
}
