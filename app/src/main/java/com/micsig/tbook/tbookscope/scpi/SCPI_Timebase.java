package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 水平时基(Timebase)子模块                        |
 * | 核心职责：处理SCPI协议中:TIMebase相关命令的设置与查询                         |
 * | 架构设计：静态方法类，委托Command中间件的Timebase接口执行实际操作             |
 * | 数据流向：SCPIParam → 本类静态方法 → Command.get().getTimebase()            |
 * | 依赖关系：SCPIParam、Command、ToolsSCPI                                    |
 * | 使用场景：远程设置/查询水平时基档位、模式、偏移、XY显示、滚屏、缩放窗口等      |
 * +---------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Timebase {
//     new SCPICommandStruct(":TIMebase:EXTent","SCPI_Timebase","Extent"),//设置水平时基档位
//            new SCPICommandStruct(":TIMebase:PLUS:EXTent","SCPI_Timebase","Plus_Extent"),//设置水平时基档位
//            new SCPICommandStruct(":TIMebase:EXTent?","SCPI_Timebase","ExtentQ"),//查询水平时基档位
//            new SCPICommandStruct(":TIMebase:MODE","SCPI_Timebase","Mode"),//设置屏幕时基显示方式
//            new SCPICommandStruct(":TIMebase:MODE?","SCPI_Timebase","ModeQ"),//查询屏幕时基显示方式
//            new SCPICommandStruct(":TIMebase:ROLL:DISPlay","SCPI_Timebase","Roll_Display"),//滚屏设置
//            new SCPICommandStruct(":TIMebase:ROLL:DISPlay?","SCPI_Timebase","Roll_DisplayQ"),//滚屏查询
//            new SCPICommandStruct(":TIMebase:XY1:DISPlay","SCPI_Timebase","XY1_Display"),//打开或关闭通道1和通道2的XY模式显示
//            new SCPICommandStruct(":TIMebase:XY1:DISPlay?","SCPI_Timebase","XY1_DisplayQ"),//查询通道1和通道2的XY模式显示
//    //timerbase:offset 协议1.1 换名为 timerbase:position 2016.12.8
//            new SCPICommandStruct(":TIMebase:POSition","SCPI_Timebase","Position"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:OFFSet","SCPI_Timebase","Offset"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:PLUS:OFFSet","SCPI_Timebase","Plus_Offset"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:PLUS:POSition","SCPI_Timebase","Plus_Position"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:POSition?","SCPI_Timebase","PositionQ"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:OFFSet?","SCPI_Timebase","OffsetQ"),//查询波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:ZOOm:SCAle","SCPI_Timebase","Scale"),
//            new SCPICommandStruct(":TIMebase:ZOOm:SCAle?","SCPI_Timebase","ScaleQ"),

    /**
     * 设置水平时基档位
     * @param param SCPI参数封装，iParam1为模式索引，dParam1为档位值
     */
    public static void Extent(SCPIParam param){
        Command.get().getTimebase().Extent(param.iParam1, param.dParam1, true); // 委托Command中间件设置时基档位，true表示通知UI刷新
    }

    /**
     * 设置水平时基档位（Plus增量方式）
     * @param param SCPI参数封装，iParam1为增量方向
     */
    public static void Plus_Extent(SCPIParam param){
        Command.get().getTimebase().Plus_Extent(param.iParam1,true); // 委托Command中间件增加时基档位，true表示通知UI刷新
    }

    /**
     * 查询水平时基档位
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 时基档位值的字符串表示
     */
    public static String ExtentQ(SCPIParam param){
        double d= Command.get().getTimebase().ExtentQ(); // 从Command中间件获取时基档位值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

    /**
     * 设置屏幕时基显示方式（YT/XY/Roll等）
     * @param param SCPI参数封装，iParam1为模式索引
     */
    public static void Mode(SCPIParam param){
        Command.get().getTimebase().Mode(param.iParam1,true); // 委托Command中间件设置时基模式，true表示通知UI刷新
    }

    /**
     * 查询屏幕时基显示方式
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 时基模式名称字符串
     */
    public static String ModeQ(SCPIParam param){
        int i=Command.get().getTimebase().ModeQ(); // 从Command中间件获取时基模式索引
        return ToolsSCPI.getTimebaseMode(i); // 将索引转换为模式名称字符串
    }

    /**
     * 设置滚屏显示开关
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Roll_Display(SCPIParam param){
        Command.get().getTimebase().Roll_Display(param.bParam1, true); // 委托Command中间件设置滚屏开关，true表示通知UI刷新
    }

    /**
     * 查询滚屏显示开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String Roll_DisplayQ(SCPIParam param){
        boolean b=Command.get().getTimebase().Roll_DisplayQ(); // 从Command中间件获取滚屏状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 设置XY模式显示
     * @param param SCPI参数封装，iParam1为XY模式索引
     */
    public static void XY1_Display(SCPIParam param){
        Command.get().getTimebase().XY1_Display(param.iParam1,true); // 委托Command中间件设置XY模式，true表示通知UI刷新
    }

    /**
     * 查询XY模式显示状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String XY1_DisplayQ(SCPIParam param){
         int i=Command.get().getTimebase().XY1_DisplayQ(); // 从Command中间件获取XY模式索引
         return ToolsSCPI.getOpenState(i==0); // 索引为0表示XY模式关闭，转换为"ON"/"OFF"

    }

    /**
     * 设置波形水平位置（偏移）
     * @param param SCPI参数封装，dParam1为位置值
     */
    public static void Position(SCPIParam param){
        Command.get().getTimebase().Position(param.dParam1,true); // 委托Command中间件设置水平位置，true表示通知UI刷新
    }

    /**
     * 设置波形水平偏移
     * @param param SCPI参数封装，dParam1为偏移值
     */
    public static void Offset(SCPIParam param){
        Command.get().getTimebase().Offset(param.dParam1,true); // 委托Command中间件设置水平偏移，true表示通知UI刷新
    }

    /**
     * 设置波形水平偏移（Plus增量方式）
     * @param param SCPI参数封装，iParam1为增量方向
     */
    public static void Plus_Offset(SCPIParam param){
        Command.get().getTimebase().Plus_Offset(param.iParam1,true); // 委托Command中间件增加水平偏移，true表示通知UI刷新
    }

    /**
     * 设置波形水平位置（Plus增量方式）
     * @param param SCPI参数封装，iParam1为增量方向
     */
    public static void Plus_Position(SCPIParam param){
        Command.get().getTimebase().Plus_Position(param.iParam1,true); // 委托Command中间件增加水平位置，true表示通知UI刷新
    }

    /**
     * 查询波形水平位置
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 水平位置值的字符串表示
     */
    public static String PositionQ(SCPIParam param){
        double d=Command.get().getTimebase().PositionQ(); // 从Command中间件获取水平位置值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

    /**
     * 查询波形水平偏移
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 水平偏移值的字符串表示
     */
    public static String OffsetQ(SCPIParam param){
        double d= Command.get().getTimebase().OffsetQ(); // 从Command中间件获取水平偏移值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

    /**
     * 设置缩放窗口的水平缩放比例
     * @param param SCPI参数封装，iParam1为参数，dParam1为缩放值
     */
    public static void Scale(SCPIParam param){
        Command.get().getTimebase().Scale(param.iParam1, param.dParam1, true); // 委托Command中间件设置缩放比例，true表示通知UI刷新
    }

    /**
     * 查询缩放窗口的水平缩放比例
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 缩放比例值的字符串表示
     */
    public static String ScaleQ(SCPIParam param){
        double d=Command.get().getTimebase().ScaleQ(); // 从Command中间件获取缩放比例值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

    /**
     * 查询时基选项列表
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 时基选项列表字符串
     */
    public static String ListQ(SCPIParam param){
        return Command.get().getTimebase().ListQ(); // 委托Command中间件查询时基选项列表
    }

}
