package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义
import com.micsig.tbook.tbookscope.scpi.SCPIParam; // SCPI参数

/**
 * @auother Liwb
 * @description:
 * @data:2026-1-9 17:00
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                        Command_Measure_Setting                              |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器测量设置命令处理模块                                         |
 * | 核心职责: 处理SCPI测量设置相关指令，包括指示器显示、范围、阈值、高低电平等    |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层                        |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum                            |
 * | 使用场景: 远程控制或内部调用设置测量参数时使用                               |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Measure_Setting {

//    new SCPICommandStruct(":MEASure:SETTing:INDicator","SCPI_Measure_Statistic","Indicator"), //
//    new SCPICommandStruct(":MEASure:SETTing:INDicator?","SCPI_Measure_Statistic","IndicatorQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:RANGe","SCPI_Measure_Statistic","Range"), //
//    new SCPICommandStruct(":MEASure:SETTing:RANGe?","SCPI_Measure_Statistic","RangeQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:ThReshold","SCPI_Measure_Statistic","Threshold"), //
//    new SCPICommandStruct(":MEASure:SETTing:ThReshold?","SCPI_Measure_Statistic","ThresholdQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:HIGH","SCPI_Measure_Statistic","High"), //
//    new SCPICommandStruct(":MEASure:SETTing:HIGH?","SCPI_Measure_Statistic","HighQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:MID","SCPI_Measure_Statistic","Mid"), //
//    new SCPICommandStruct(":MEASure:SETTing:MID?","SCPI_Measure_Statistic","MidQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:LOW","SCPI_Measure_Statistic","Low"), //
//    new SCPICommandStruct(":MEASure:SETTing:LOW?","SCPI_Measure_Statistic","LowQ"), //


    private boolean isDisplay; // 指示器是否显示
    private int rangeIndex; // 范围索引
    private int thresholdIndex; // 阈值索引
    private String high; // 高电平值
    private String mid; // 中电平值
    private String low; // 低电平值

    /**
     * 设置测量指示器的显示状态
     *
     * @param bDisplay    是否显示指示器
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Indicator(boolean bDisplay, boolean isUpdateUI){
        this.isDisplay=bDisplay; // 保存指示器显示状态
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_Setting_Indicator); // 设置消息标志为测量设置指示器
            String param = String.valueOf(isDisplay); // 将显示状态转为字符串参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 设置测量范围索引
     *
     * @param index       范围索引值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Range(int index, boolean isUpdateUI){
        this.rangeIndex=index; // 保存范围索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_Setting_Range); // 设置消息标志为测量设置范围
            String param = String.valueOf(rangeIndex); // 将范围索引转为字符串参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 设置测量阈值索引
     *
     * @param index       阈值索引值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Threshold(int index, boolean isUpdateUI){
        this.thresholdIndex=index; // 保存阈值索引
        if (isUpdateUI){ // 判断是否需要更新UI（当前未实现具体逻辑）

        }
    }

    /**
     * 设置测量高电平值
     *
     * @param value       高电平值，需以%、V或Db结尾
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void High(String value, boolean isUpdateUI){
        if (value.endsWith("%") || value.endsWith("V") || value.endsWith("Db")) { // 校验值的单位是否合法
            this.high=value; // 保存高电平值
            if (isUpdateUI){ // 判断是否需要更新UI（当前未实现具体逻辑）

            }
        }
    }

    /**
     * 设置测量中电平值
     *
     * @param value       中电平值，需以%、V或Db结尾
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Mid(String value, boolean isUpdateUI){
        if (value.endsWith("%") || value.endsWith("V") || value.endsWith("Db")) { // 校验值的单位是否合法
            this.mid=value; // 保存中电平值
            if (isUpdateUI){ // 判断是否需要更新UI（当前未实现具体逻辑）

            }
        }
    }

    /**
     * 设置测量低电平值
     *
     * @param value       低电平值，需以%、V或Db结尾
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Low(String value, boolean isUpdateUI){
        if (value.endsWith("%") || value.endsWith("V") || value.endsWith("Db")) { // 校验值的单位是否合法
            this.low=value; // 保存低电平值
            if (isUpdateUI){ // 判断是否需要更新UI（当前未实现具体逻辑）

            }
        }
    }



    /**
     * 查询测量指示器的显示状态
     *
     * @return 指示器是否显示
     */
    public  boolean IndicatorQ(){
        return isDisplay; // 返回指示器显示状态
    }

    /**
     * 查询测量范围索引
     *
     * @return 范围索引值
     */
    public  int RangeQ(){
        return rangeIndex; // 返回范围索引
    }

    /**
     * 查询测量阈值索引
     *
     * @return 阈值索引值
     */
    public  int ThresholdQ(){
        return thresholdIndex; // 返回阈值索引
    }

    /**
     * 查询测量高电平值
     *
     * @return 高电平值字符串
     */
    public  String HighQ(){
        return high; // 返回高电平值
    }

    /**
     * 查询测量中电平值
     *
     * @return 中电平值字符串
     */
    public  String MidQ(){
        return mid; // 返回中电平值
    }

    /**
     * 查询测量低电平值
     *
     * @return 低电平值字符串
     */
    public  String LowQ(){
        return low; // 返回低电平值
    }
}
