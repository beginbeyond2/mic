package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * Created by liwb on 2018/1/17.
 */

public class Command_FunctionMenu {
    //     new SCPICommandStruct(":AUTO","SCPI_FunctionMenu","Auto"),//自动
//     new SCPICommandStruct(":RUN","SCPI_FunctionMenu","Run"),//使示波器开始运行，符合触发条件，开始采集数据
//     new SCPICommandStruct(":STOP","SCPI_FunctionMenu","Stop"),//使示波器停止运行，数据采集停止
//     new SCPICommandStruct(":SINGle","SCPI_FunctionMenu","Single"),//将示波器设置为单序列，示波器捕获并显示单次采集
//     new SCPICommandStruct(":MULTiple","SCPI_FunctionMenu","Multiple"),//将示波器设置为连续触发方式
//     new SCPICommandStruct(":BEEP","SCPI_FunctionMenu","Beep"),//设置示波器的蜂鸣状态

    public  void Auto(boolean isUpdateUI){
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUTO);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public  void Run(boolean isUpdateUI){
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_RUN);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public  void Stop(boolean isUpdateUI){
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_STOP);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public  void Single(boolean isUpdateUI){
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_SINGLE);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public  void Multiple(boolean isUpdateUI){
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_MULTIPLE);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  void Beep(boolean isUpdateUI){
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_BEEP);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    /**
     * 20250707 起始版本
     * 2025-9-17 关键channel1:label? 返回为空时，显示NONE.
     * @return 返回版本
     */
    public String Version(){
        return "20250917";
    }
}
