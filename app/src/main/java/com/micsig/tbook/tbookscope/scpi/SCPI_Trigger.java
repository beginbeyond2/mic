package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 触发控制(Trigger)子模块                          |
 * | 核心职责：处理SCPI协议中:TRIGger相关命令的设置与查询                          |
 * | 架构设计：静态方法类，委托Command中间件的Trigger接口执行实际操作               |
 * | 数据流向：SCPIParam → 本类静态方法 → Command.get().getTrigger()             |
 * | 依赖关系：SCPIParam、Command、ToolsSCPI                                    |
 * | 使用场景：远程设置/查询触发类型、释抑时间、触发模式、触发状态等               |
 * +---------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger {
//     new SCPICommandStruct(":TRIGger:TYPE","SCPI_Trigger","Type"),//选择触发类型
//            new SCPICommandStruct(":TRIGger:TYPE?","SCPI_Trigger","TypeQ"),//查询返回当前使用的触发类型
//            new SCPICommandStruct(":TRIGger:HOLDoff","SCPI_Trigger","HoldOff"),//设置触发释抑时间
//            new SCPICommandStruct(":TRIGger:HOLDoff?","SCPI_Trigger","HoldOffQ"),//查询以科学计数形式返回触发释抑时间
//            new SCPICommandStruct(":TRIGger:MODE","SCPI_Trigger","Mode"),//设置触发方式：自动或普通
//            new SCPICommandStruct(":TRIGger:MODE?","SCPI_Trigger","ModeQ"),//查询触发方式
//            new SCPICommandStruct(":TRIGger:STATus?","SCPI_Trigger","StatusQ"),//查询当前的触发状态

    /**
     * 设置触发类型（边沿/脉宽/视频/延迟等）
     * @param param SCPI参数封装，iParam1为触发类型索引
     */
    public static void Type(SCPIParam param) {
        Command.get().getTrigger().Type(param.iParam1, true); // 委托Command中间件设置触发类型，true表示通知UI刷新
    }

    /**
     * 查询当前触发类型
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 触发类型名称字符串，如"EDGE"
     */
    public static String TypeQ(SCPIParam param) {
        int i= Command.get().getTrigger().TypeQ(); // 从Command中间件获取触发类型索引
        return ToolsSCPI.getTriggerType(i); // 将索引转换为触发类型名称字符串
    }

    /**
     * 设置触发释抑时间
     * @param param SCPI参数封装，dParam1为释抑时间值
     */
    public static void HoldOff(SCPIParam param) {
        Command.get().getTrigger().HoldOff(param.dParam1,true); // 委托Command中间件设置释抑时间，true表示通知UI刷新
    }

    /**
     * 查询触发释抑时间
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 释抑时间值的字符串表示
     */
    public static String HoldOffQ(SCPIParam param) {
        double d=Command.get().getTrigger().HoldOffQ(); // 从Command中间件获取释抑时间值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

    /**
     * 设置触发模式（自动/普通）
     * @param param SCPI参数封装，iParam1为模式索引
     */
    public static void Mode(SCPIParam param) {
        Command.get().getTrigger().Mode(param.iParam1,true); // 委托Command中间件设置触发模式，true表示通知UI刷新
    }

    /**
     * 查询触发模式
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 触发模式名称字符串，如"AUTO"或"NORMal"
     */
    public static String ModeQ(SCPIParam param) {
        int i=Command.get().getTrigger().ModeQ(); // 从Command中间件获取触发模式索引
        return ToolsSCPI.getTriggerMode(i); // 将索引转换为触发模式名称字符串
    }

    /**
     * 查询当前触发状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 触发状态名称字符串，如"TD"或"WAIT"
     */
    public static String StatusQ(SCPIParam param) {
        int i= Command.get().getTrigger().StatusQ(); // 从Command中间件获取触发状态索引
        return ToolsSCPI.getTriggerStatus(i); // 将索引转换为触发状态名称字符串
    }

    /**
     * 查询是否支持外部触发（固定返回ON）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"
     */
    public static String IsExternalTriggerQ(SCPIParam param){
        return ToolsSCPI.getOpenState(true); // 固定返回ON，表示支持外部触发
    }

    /**
     * 查询是否支持外部时钟（固定返回ON）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"
     */
    public static String IsExternalClockQ(SCPIParam param){
        return ToolsSCPI.getOpenState(true); // 固定返回ON，表示支持外部时钟
    }

    /**
     * 查询触发对话框是否有数据
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String HasDialogQ(SCPIParam param){
        boolean b= Command.get().getTrigger().HasDataQ(); // 从Command中间件获取触发对话框数据状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 设置触发对话框选项
     * @param param SCPI参数封装，bParam1为选项值
     */
    public static void DialogSet(SCPIParam param){
        Command.get().getTrigger().SelectData(param.bParam1,true); // 委托Command中间件设置触发对话框选项，true表示通知UI刷新
    }
}
