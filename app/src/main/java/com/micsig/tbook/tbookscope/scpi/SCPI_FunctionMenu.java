package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件，用于获取底层功能菜单操作接口

/*
 * +=============================================================================+
 * |  模块定位：SCPI功能菜单命令处理层                                             |
 * |  核心职责：将SCPI协议中运行控制命令（AUTO/RUN/STOP/SINGLE/MULTIPLE/BEEP）      |
 * |           解析并转发至底层功能菜单中间件                                        |
 * |  架构设计：纯静态方法类，作为SCPI命令分发器与Command中间件之间的桥接层           |
 * |  数据流向：SCPI解析器 → SCPI_FunctionMenu → Command.get().getFunctionMenu()   |
 * |  依赖关系：依赖SCPIParam(参数封装)、Command(命令中间件)                        |
 * |  使用场景：示波器运行模式控制（自动设置、运行、停止、单次触发、连续触发）、      |
 * |           蜂鸣器控制、固件版本查询                                              |
 * +=============================================================================+
 */

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

    /**
     * 自动设置命令：让示波器自动调整垂直档位、水平时基、触发等参数以适配输入信号
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Auto(SCPIParam param) {
        Command.get().getFunctionMenu().Auto(true); // 调用底层接口执行自动设置
    }

    /**
     * 运行命令：使示波器开始运行，满足触发条件时开始采集数据
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Run(SCPIParam param) {
        Command.get().getFunctionMenu().Run(true); // 调用底层接口启动示波器运行
    }

    /**
     * 停止命令：使示波器停止运行，数据采集停止
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Stop(SCPIParam param) {
        Command.get().getFunctionMenu().Stop(true); // 调用底层接口停止示波器运行
    }

    /**
     * 单次触发命令：将示波器设置为单次触发模式，捕获并显示一次采集后停止
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Single(SCPIParam param) {
        Command.get().getFunctionMenu().Single(true); // 调用底层接口设置为单次触发模式
    }

    /**
     * 连续触发命令：将示波器设置为连续触发模式（正常触发方式）
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Multiple(SCPIParam param) {
        Command.get().getFunctionMenu().Multiple(true); // 调用底层接口设置为连续触发模式
    }

    /**
     * 蜂鸣器控制命令：设置示波器的蜂鸣状态
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Beep(SCPIParam param) {
        Command.get().getFunctionMenu().Beep(true); // 调用底层接口控制蜂鸣器状态
    }

    /**
     * 查询固件版本号
     * @param param SCPI命令参数（本命令无参数）
     * @return 固件版本号字符串
     */
    public static String VersionQ(SCPIParam param){
        return Command.get().getFunctionMenu().Version(); // 调用底层接口查询固件版本号
    }
}
