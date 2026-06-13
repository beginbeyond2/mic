package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_FunctionMenu {
//     new SCPICommandStruct(":AUTO","SCPI_FunctionMenu","Auto"),//自动
//     new SCPICommandStruct(":RUN","SCPI_FunctionMenu","Run"),//使示波器开始运行，符合触发条件，开始采集数据
//     new SCPICommandStruct(":STOP","SCPI_FunctionMenu","Stop"),//使示波器停止运行，数据采集停止
//     new SCPICommandStruct(":SINGle","SCPI_FunctionMenu","Single"),//将示波器设置为单序列，示波器捕获并显示单次采集
//     new SCPICommandStruct(":MULTiple","SCPI_FunctionMenu","Multiple"),//将示波器设置为连续触发方式
//     new SCPICommandStruct(":BEEP","SCPI_FunctionMenu","Beep"),//设置示波器的蜂鸣状态

    public static void Auto(SCPIParam param) {
        Command.get().getFunctionMenu().Auto(true);
    }

    public static void Run(SCPIParam param) {
        Command.get().getFunctionMenu().Run(true);
    }

    public static void Stop(SCPIParam param) {
        Command.get().getFunctionMenu().Stop(true);
    }

    public static void Single(SCPIParam param) {
        Command.get().getFunctionMenu().Single(true);
    }

    public static void Multiple(SCPIParam param) {
        Command.get().getFunctionMenu().Multiple(true);
    }

    public static void Beep(SCPIParam param) {
        Command.get().getFunctionMenu().Beep(true);
    }
    public static String VersionQ(SCPIParam param){
        return Command.get().getFunctionMenu().Version();
    }
}
