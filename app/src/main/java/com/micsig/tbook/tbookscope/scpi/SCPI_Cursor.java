package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令包

import com.micsig.base.Logger; // 日志工具类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件，用于获取底层光标操作接口

/*
 * +=============================================================================+
 * |  模块定位：SCPI光标测量命令处理层                                             |
 * |  核心职责：将SCPI协议中:CURSor子系统的命令解析并转发至底层光标中间件              |
 * |  架构设计：纯静态方法类，作为SCPI命令分发器与Command中间件之间的桥接层           |
 * |  数据流向：SCPI解析器 → SCPI_Cursor → Command.get().getCursor() → 底层        |
 * |  依赖关系：依赖SCPIParam(参数封装)、Command(命令中间件)、ToolsSCPI(工具类)、   |
 * |           Logger(日志工具)                                                    |
 * |  使用场景：示波器光标测量功能，包括水平/垂直光标开关、光标位置设置、            |
 * |           光标X/Y值查询、光标差值查询、比率查询、通道源设置、频率查询、         |
 * |           光标追踪模式等                                                      |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Cursor {
    private static final String TAG="SCPI_Cursor"; // 日志标签，用于标识光标模块日志
//     new SCPICommandStruct(":CURSor:HORizontal","SCPI_Cursor","Horizontal"),//打开或关闭水平光标功能
//            new SCPICommandStruct(":CURSor:HORizontal?","SCPI_Cursor","HorizontalQ"),//查询水平光标功能
//            new SCPICommandStruct(":CURSor:VERTical","SCPI_Cursor","Vertical"),//打开或关闭垂直光标功能
//            new SCPICommandStruct(":CURSor:VERTical?","SCPI_Cursor","VerticalQ"),//查询垂直光标功能
//            new SCPICommandStruct(":CURSor:CX1","SCPI_Cursor","Cx1"),//设置垂直光标A的位置
//            new SCPICommandStruct(":CURSor:PLUS:CXA","SCPI_Cursor","Plus_Cxa"),//设置垂直光标A的位置
//            new SCPICommandStruct(":CURSor:CX1?","SCPI_Cursor","Cx1Q"),//查询垂直光标A的位置
//            new SCPICommandStruct(":CURSor:CX2","SCPI_Cursor","Cx2"),//设置垂直光标B的位置
//            new SCPICommandStruct(":CURSor:PLUS:CXB","SCPI_Cursor","Plus_Cxb"),//设置垂直光标B的位置
//            new SCPICommandStruct(":CURSor:CX2?","SCPI_Cursor","Cx2Q"),//查询垂直光标B的位置
//            new SCPICommandStruct(":CURSor:CY1","SCPI_Cursor","CY1"),//设置水平光标A的位置
//            new SCPICommandStruct(":CURSor:PLUS:CYA","SCPI_Cursor","PLUS_CYA"),//设置水平光标A的位置
//            new SCPICommandStruct(":CURSor:CY1?","SCPI_Cursor","CY1Q"),//查询水平光标A的位置
//            new SCPICommandStruct(":CURSor:CY2","SCPI_Cursor","CY2"),//设置水平光标B的位置
//            new SCPICommandStruct(":CURSor:PLUS:CYB","SCPI_Cursor","PLUS_CYB"),//设置水平光标B的位置
//            new SCPICommandStruct(":CURSor:CY2?","SCPI_Cursor","CY2Q"),//查询水平光标B的位置
//            new SCPICommandStruct(":CURSor:X1Value?","SCPI_Cursor","X1ValueQ"),//查询垂直光标A的X值
//            new SCPICommandStruct(":CURSor:X2Value?","SCPI_Cursor","X2ValueQ"),//查询垂直光标B的X值
//            new SCPICommandStruct(":CURSor:Y1Value?","SCPI_Cursor","Y1ValueQ"),//查询水平光标A的Y值
//            new SCPICommandStruct(":CURSor:Y2Value?","SCPI_Cursor","Y2ValueQ"),//查询水平光标B的Y值
//            new SCPICommandStruct(":CURSor:XDELta?","SCPI_Cursor","XdeltaQ"),//查询垂直光标A和B之间的差值，单位与水平单位相同
//            new SCPICommandStruct(":CURSor:YDELta?","SCPI_Cursor","YdeltaQ"),//查询水平光标A和B之间的差值，单位与垂直单位相同
//            new SCPICommandStruct(":CURSor:RATio?","SCPI_Cursor","RatioQ"),//查询水平光标A和B之间的差值与垂直光标A和B之间的差值之间的比值
//            new SCPICommandStruct(":CURSor:SOURce","SCPI_Cursor","Source"),//设置光标测量的通道源
//            new SCPICommandStruct(":CURSor:SOURce?","SCPI_Cursor","SourceQ"),//查询光标测量的通道源
//            new SCPICommandStruct(":CURSor:FREQ?","SCPI_Cursor","FreqQ"),//查询垂直光标x1和x2之间的1/x，单位HZ

    /**
     * 打开或关闭水平光标功能
     * @param param SCPI命令参数，bParam1为开启/关闭标志
     */
    public static void Horizontal(SCPIParam param) {
        Command.get().getCursor().Horizontal(param.bParam1, true); // 调用底层接口设置水平光标开关
    }

    /**
     * 查询水平光标功能状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 水平光标状态字符串（ON/OFF）
     */
    public static String HorizontalQ(SCPIParam param) {
        boolean b=Command.get().getCursor().HorizontalQ(); // 调用底层接口查询水平光标状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }

    /**
     * 打开或关闭垂直光标功能
     * @param param SCPI命令参数，bParam1为开启/关闭标志
     */
    public static void Vertical(SCPIParam param) {
        Command.get().getCursor().Vertical(param.bParam1, true); // 调用底层接口设置垂直光标开关
    }

    /**
     * 查询垂直光标功能状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 垂直光标状态字符串（ON/OFF）
     */
    public static String VerticalQ(SCPIParam param) {
        boolean b=Command.get().getCursor().VerticalQ(); // 调用底层接口查询垂直光标状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }

    /**
     * 设置垂直光标A的位置
     * @param param SCPI命令参数，iParam1为光标A位置值
     */
    public static void Cx1(SCPIParam param) {
        Command.get().getCursor().Cx1(param.iParam1, true); // 调用底层接口设置垂直光标A位置
    }

    /**
     * 设置垂直光标A的位置（PLUS模式）
     * @param param SCPI命令参数，iParam1为光标A位置值
     */
    public static void Plus_Cxa(SCPIParam param) {
        Command.get().getCursor().Plus_Cxa(param.iParam1, true); // 调用底层接口设置垂直光标A位置（PLUS模式）
    }

    /**
     * 查询垂直光标A的位置
     * @param param SCPI命令参数（本命令无参数）
     * @return 光标A位置字符串
     */
    public static String Cx1Q(SCPIParam param) {
        int i=Command.get().getCursor().Cx1Q(); // 调用底层接口查询垂直光标A位置
        return ToolsSCPI.getInt(i); // 将整数值转换为字符串返回
    }

    /**
     * 设置垂直光标B的位置
     * @param param SCPI命令参数，iParam1为光标B位置值
     */
    public static void Cx2(SCPIParam param) {
        Command.get().getCursor().Cx2(param.iParam1, true); // 调用底层接口设置垂直光标B位置
    }

    /**
     * 设置垂直光标B的位置（PLUS模式）
     * @param param SCPI命令参数，iParam1为光标B位置值
     */
    public static void Plus_Cxb(SCPIParam param) {
        Command.get().getCursor().Plus_Cxb(param.iParam1, true); // 调用底层接口设置垂直光标B位置（PLUS模式）
    }

    /**
     * 查询垂直光标B的位置
     * @param param SCPI命令参数（本命令无参数）
     * @return 光标B位置字符串
     */
    public static String Cx2Q(SCPIParam param) {
        int i=Command.get().getCursor().Cx2Q(); // 调用底层接口查询垂直光标B位置
        return ToolsSCPI.getInt(i); // 将整数值转换为字符串返回
    }

    /**
     * 设置水平光标A的位置
     * @param param SCPI命令参数，iParam1为光标A位置值
     */
    public static void CY1(SCPIParam param) {
        Command.get().getCursor().CY1(param.iParam1, true); // 调用底层接口设置水平光标A位置
    }

    /**
     * 设置水平光标A的位置（PLUS模式）
     * @param param SCPI命令参数，iParam1为光标A位置值
     */
    public static void PLUS_CYA(SCPIParam param) {
        Command.get().getCursor().PLUS_CYA(param.iParam1, true); // 调用底层接口设置水平光标A位置（PLUS模式）
    }

    /**
     * 查询水平光标A的位置
     * @param param SCPI命令参数（本命令无参数）
     * @return 光标A位置字符串（四舍五入取整）
     */
    public static String CY1Q(SCPIParam param) {
        int i = (int) Math.round(Command.get().getCursor().CY1Q()); // 调用底层接口查询水平光标A位置，四舍五入取整
        return ToolsSCPI.getInt(i); // 将整数值转换为字符串返回
    }

    /**
     * 设置水平光标B的位置
     * @param param SCPI命令参数，iParam1为光标B位置值
     */
    public static void CY2(SCPIParam param) {
        Command.get().getCursor().CY2(param.iParam1, true); // 调用底层接口设置水平光标B位置
    }

    /**
     * 设置水平光标B的位置（PLUS模式）
     * @param param SCPI命令参数，iParam1为光标B位置值
     */
    public static void PLUS_CYB(SCPIParam param) {
        Command.get().getCursor().PLUS_CYB(param.iParam1, true); // 调用底层接口设置水平光标B位置（PLUS模式）
    }

    /**
     * 查询水平光标B的位置
     * @param param SCPI命令参数（本命令无参数）
     * @return 光标B位置字符串（四舍五入取整）
     */
    public static String CY2Q(SCPIParam param) {
        int i= (int) Math.round(Command.get().getCursor().CY2Q()); // 调用底层接口查询水平光标B位置，四舍五入取整
        return ToolsSCPI.getInt(i); // 将整数值转换为字符串返回
    }

    /**
     * 设置垂直光标A的X值
     * @param param SCPI命令参数，dParam1为X值
     */
    public static void X1Value(SCPIParam param) {
        Command.get().getCursor().X1Value(param.dParam1, true); // 调用底层接口设置垂直光标A的X值
    }

    /**
     * 设置垂直光标B的X值
     * @param param SCPI命令参数，dParam1为X值
     */
    public static void X2Value(SCPIParam param) {
        Command.get().getCursor().X2Value(param.dParam1, true); // 调用底层接口设置垂直光标B的X值
    }

    /**
     * 查询垂直光标A的X值
     * @param param SCPI命令参数（本命令无参数）
     * @return X值字符串
     */
    public static String X1ValueQ(SCPIParam param) {
        double d=Command.get().getCursor().X1ValueQ(); // 调用底层接口查询垂直光标A的X值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 查询垂直光标B的X值
     * @param param SCPI命令参数（本命令无参数）
     * @return X值字符串
     */
    public static String X2ValueQ(SCPIParam param) {
        double d=Command.get().getCursor().X2ValueQ(); // 调用底层接口查询垂直光标B的X值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置水平光标A的Y值
     * @param param SCPI命令参数，dParam1为Y值
     */
    public static void Y1Value(SCPIParam param) {
        Command.get().getCursor().Y1Value(param.dParam1, true); // 调用底层接口设置水平光标A的Y值
    }

    /**
     * 设置水平光标B的Y值
     * @param param SCPI命令参数，dParam1为Y值
     */
    public static void Y2Value(SCPIParam param) {
        Command.get().getCursor().Y2Value(param.dParam1, true); // 调用底层接口设置水平光标B的Y值
    }

    /**
     * 查询水平光标A的Y值
     * @param param SCPI命令参数（本命令无参数）
     * @return Y值字符串
     */
    public static String Y1ValueQ(SCPIParam param) {
        double d=Command.get().getCursor().Y1ValueQ(); // 调用底层接口查询水平光标A的Y值
        return  ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 查询水平光标B的Y值
     * @param param SCPI命令参数（本命令无参数）
     * @return Y值字符串
     */
    public static String Y2ValueQ(SCPIParam param) {
        double d=Command.get().getCursor().Y2ValueQ(); // 调用底层接口查询水平光标B的Y值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 查询垂直光标A和B之间的X差值
     * @param param SCPI命令参数（本命令无参数）
     * @return X差值字符串
     */
    public static String XdeltaQ(SCPIParam param) {
        double d=Command.get().getCursor().XdeltaQ(); // 调用底层接口查询垂直光标X差值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 查询水平光标A和B之间的Y差值
     * @param param SCPI命令参数（本命令无参数）
     * @return Y差值字符串
     */
    public static String YdeltaQ(SCPIParam param) {
        double d=Command.get().getCursor().YdeltaQ(); // 调用底层接口查询水平光标Y差值
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 查询X差值与Y差值的比率
     * @param param SCPI命令参数（本命令无参数）
     * @return 比率字符串
     */
    public static String RatioQ(SCPIParam param) {
        double d=Command.get().getCursor().RatioQ(); // 调用底层接口查询光标比率
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置光标测量的通道源
     * @param param SCPI命令参数，iParam1为通道源编号
     */
    public static void Source(SCPIParam param) {
        Logger.i(TAG,"scpi_source:"+param.iParam1); // 记录日志：光标通道源设置
        Command.get().getCursor().Source(param.iParam1, true); // 调用底层接口设置光标通道源
    }

    /**
     * 查询光标测量的通道源
     * @param param SCPI命令参数（本命令无参数）
     * @return 通道源字符串
     */
    public static String SourceQ(SCPIParam param) {
        int i=Command.get().getCursor().SourceQ(); // 调用底层接口查询光标通道源
        return ToolsSCPI.getChAll(i); // 将整数类型值转换为通道标识字符串
    }

    /**
     * 查询垂直光标X1和X2之间的频率（1/ΔX），单位Hz
     * @param param SCPI命令参数（本命令无参数）
     * @return 频率字符串
     */
    public static String FreqQ(SCPIParam param) {
        double d=Command.get().getCursor().FreqQ(); // 调用底层接口查询光标频率
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置光标追踪模式开关
     * @param param SCPI命令参数，bParam1为追踪模式开启/关闭标志
     */
    public static void Trace(SCPIParam param){
        Command.get().getCursor().Trace(param.bParam1,true); // 调用底层接口设置光标追踪模式
    }

    /**
     * 查询光标追踪模式状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 追踪模式状态字符串（ON/OFF）
     */
    public static String TraceQ(SCPIParam param){
        boolean b= Command.get().getCursor().TraceQ(); // 调用底层接口查询光标追踪模式状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }
}
