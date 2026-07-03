package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                   SCPI_Auto - 自动设置与自动量程SCPI命令处理              |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中AUTO子系统的Java层命令处理类                       |   *
 *   |  核心职责: 处理自动设置(:AUTO:SET)和自动量程(:AUTO:RANge)相关的SCPI命令       |   *
 *   |  架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command中间件下发到设备层  |   *
 *   |  数据流向: SCPICommandDeal反射调用 → 本类静态方法 → Command中间件 → 设备层    |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、Command(中间件)、ToolsSCPI(结果格式化)        |   *
 *   |  使用场景: 上位机发送自动设置/自动量程相关SCPI命令时，由SCPICommandDeal分发调用   |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPI_Auto {
//     new SCPICommandStruct(":AUTO:SET:CHANnel","SCPI_Auto","Set_Channel"),//设置自动打开通道
//            new SCPICommandStruct(":AUTO:SET:CHANnel?","SCPI_Auto","Set_ChannelQ"),//查询自动打开通道
//            new SCPICommandStruct(":AUTO:SET:LEVEl","SCPI_Auto","Set_Level"),//设置门限电平
//            new SCPICommandStruct(":AUTO:SET:LEVEl?","SCPI_Auto","Set_LevelQ"),//查询门限电平
//            new SCPICommandStruct(":AUTO:SET:SOURce","SCPI_Auto","Set_Source"),//设置触发源
//            new SCPICommandStruct(":AUTO:SET:SOURce?","SCPI_Auto","Set_SourceQ"),//查询触发源
//            new SCPICommandStruct(":AUTO:RANge","SCPI_Auto","Range"),//设置自动量程
//            new SCPICommandStruct(":AUTO:RANge?","SCPI_Auto","RangeQ"),//查询自动量程
//            new SCPICommandStruct(":AUTO:RANge:VERtical","SCPI_Auto","Range_Vertical"),//设置自动垂直
//            new SCPICommandStruct(":AUTO:RANge:VERtical?","SCPI_Auto","Range_VerticalQ"),//查询自动垂直
//            new SCPICommandStruct(":AUTO:RANge:HORizoncal","SCPI_Auto","Range_Horizoncal"),//设置自动水平
//            new SCPICommandStruct(":AUTO:RANge:HORizoncal?","SCPI_Auto","Range_HorizoncalQ"),//查询自动水平
//            new SCPICommandStruct(":AUTO:RANge:LEVEl","SCPI_Auto","Range_Level"),//设置自动量程
//            new SCPICommandStruct(":AUTO:RANge:LEVEl?","SCPI_Auto","Range_LevelQ"),//查询自动量程

    /**
     * 设置自动打开通道。
     * 对应SCPI命令: :AUTO:SET:CHANnel
     * @param param SCPI参数，bParam1为通道是否打开
     */
    public static void Set_Channel(SCPIParam param) {
        System.out.println("SCPI_AUTO_Set_Channel!"); // 打印调试日志
        Command.get().getAuto().setChannel(param.bParam1, true); // 通过Command中间件设置自动通道开关
    }

    /**
     * 查询自动打开通道状态。
     * 对应SCPI命令: :AUTO:SET:CHANnel?
     * @param param SCPI参数
     * @return 通道开关状态字符串("1"或"0")
     */
    public static String Set_ChannelQ(SCPIParam param) {
        boolean b= Command.get().getAuto().setChannelQuery(); // 查询自动通道开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"1"/"0"字符串返回
    }

    /**
     * 设置自动门限电平。
     * 对应SCPI命令: :AUTO:SET:LEVEl
     * @param param SCPI参数，dParam1为门限电平值
     */
    public static void Set_Level(SCPIParam param) {
        Command.get().getAuto().setLevel(param.dParam1, true); // 通过Command中间件设置门限电平
    }

    /**
     * 查询自动门限电平。
     * 对应SCPI命令: :AUTO:SET:LEVEl?
     * @param param SCPI参数
     * @return 门限电平值字符串
     */
    public static String Set_LevelQ(SCPIParam param) {
        double d= Command.get().getAuto().setLevelQuery(); // 查询门限电平值
        return ToolsSCPI.getDouble(d); // 将double转换为字符串返回
    }

    /**
     * 设置自动触发源。
     * 对应SCPI命令: :AUTO:SET:SOURce
     * @param param SCPI参数，iParam1为触发源索引
     */
    public static void Set_Source(SCPIParam param) {
        Command.get().getAuto().setSource(param.iParam1, true); // 通过Command中间件设置触发源
    }

    /**
     * 查询自动触发源。
     * 对应SCPI命令: :AUTO:SET:SOURce?
     * @param param SCPI参数
     * @return 触发源名称字符串("CURrent"或"Max")
     */
    public static String Set_SourceQ(SCPIParam param) {
        int i=Command.get().getAuto().setSourceQuery(); // 查询触发源索引
        return ToolsSCPI.getAutoSource(i); // 将索引转换为触发源名称返回
    }

    /**
     * 设置自动量程开关。
     * 对应SCPI命令: :AUTO:RANge
     * @param param SCPI参数，bParam1为是否开启自动量程
     */
    public static void Range(SCPIParam param) {
        Command.get().getAuto().range(param.bParam1, true); // 通过Command中间件设置自动量程开关
    }

    /**
     * 查询自动量程状态。
     * 对应SCPI命令: :AUTO:RANge?
     * @param param SCPI参数
     * @return 自动量程状态字符串("1"或"0")
     */
    public static String RangeQ(SCPIParam param) {
        boolean b= Command.get().getAuto().rangeQuery(); // 查询自动量程状态
        return  ToolsSCPI.getOpenState(b); // 将布尔值转换为"1"/"0"字符串返回
    }

    /**
     * 设置自动量程垂直方向开关。
     * 对应SCPI命令: :AUTO:RANge:VERtical
     * @param param SCPI参数，bParam1为是否开启
     */
    public static void Range_Vertical(SCPIParam param) {
        Command.get().getAuto().rangeVertical(param.bParam1, true); // 通过Command中间件设置自动垂直量程
    }

    /**
     * 查询自动量程垂直方向状态。
     * 对应SCPI命令: :AUTO:RANge:VERtical?
     * @param param SCPI参数
     * @return 状态字符串("1"或"0")
     */
    public static String Range_VerticalQ(SCPIParam param) {
        boolean b= Command.get().getAuto().rangeVerticalQuery(); // 查询自动垂直量程状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"1"/"0"字符串返回
    }

    /**
     * 设置自动量程水平方向开关。
     * 对应SCPI命令: :AUTO:RANge:HORizontal
     * @param param SCPI参数，bParam1为是否开启
     */
    public static void Range_Horizontal(SCPIParam param) {
        Command.get().getAuto().rangeHorizoncal(param.bParam1, true); // 通过Command中间件设置自动水平量程
    }

    /**
     * 查询自动量程水平方向状态。
     * 对应SCPI命令: :AUTO:RANge:HORizontal?
     * @param param SCPI参数
     * @return 状态字符串("1"或"0")
     */
    public static String Range_HorizontalQ(SCPIParam param) {
        boolean b= Command.get().getAuto().rangeHorizoncalQuery(); // 查询自动水平量程状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"1"/"0"字符串返回
    }

    /**
     * 设置自动量程触发电平开关。
     * 对应SCPI命令: :AUTO:RANge:LEVEl
     * @param param SCPI参数，bParam1为是否开启
     */
    public static void Range_Level(SCPIParam param) {
        Command.get().getAuto().rangeLevel(param.bParam1, true); // 通过Command中间件设置自动触发电平
    }

    /**
     * 查询自动量程触发电平状态。
     * 对应SCPI命令: :AUTO:RANge:LEVEl?
     * @param param SCPI参数
     * @return 状态字符串("1"或"0")
     */
    public static String Range_LevelQ(SCPIParam param) {
        boolean b= Command.get().getAuto().rangeLevelQuery(); // 查询自动触发电平状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"1"/"0"字符串返回
    }
}
