package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令包

import com.micsig.base.Logger; // 日志工具类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件，用于获取底层校准操作接口

/*
 * +=============================================================================+
 * |  模块定位：SCPI校准命令处理层                                                |
 * |  核心职责：将SCPI协议中:CALibrate子系统的命令解析并转发至底层校准中间件          |
 * |  架构设计：纯静态方法类，作为SCPI命令分发器与Command中间件之间的桥接层           |
 * |  数据流向：SCPI解析器 → SCPI_Calibrate → Command.get().getCalibrate() → 底层  |
 * |  依赖关系：依赖SCPIParam(参数封装)、Command(命令中间件)、ToolsSCPI(工具类)     |
 * |  使用场景：示波器校准流程，包括零点校准、通道差异校准、AD相位/增益校准、       |
 * |           偏移量校准、通道增益校准、触发零点/系数/精准校准等                   |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Calibrate {
//    new SCPICommandStruct(":CALibrate:DATE?","SCPI_Calibrate","DateQ"),//查询上次校准时间
//    new SCPICommandStruct(":CALibrate:STARt","SCPI_Calibrate","Start"),//开始校准
//    new SCPICommandStruct(":CALibrate:QUIT","SCPI_Calibrate","Quit"),//退出校准，校准完成后的操作
//    new SCPICommandStruct(":CALibrate:STOP","SCPI_Calibrate","Stop"),//停止校准，强制停止
//    new SCPICommandStruct(":CALibrate:RESult?","SCPI_Calibrate","ResultQ"),//查询校准结果
//    new SCPICommandStruct(":CALibrate:ZERopoint","SCPI_Calibrate","ZeroPoint"),//零点校准
//    new SCPICommandStruct(":CALibrate:ZERopoint?","SCPI_Calibrate","ZeroPointQ"),//查询零点校准状态
//    new SCPICommandStruct(":CALibrate:CHDF","SCPI_Calibrate","Chdf"),//通道差异校准
//    new SCPICommandStruct(":CALibrate:CHDF?","SCPI_Calibrate","ChdfQ"),//查询通道差异校准状态
//    new SCPICommandStruct(":CALibrate:ADPHa","SCPI_Calibrate","Adpha"),//AD相位校准
//    new SCPICommandStruct(":CALibrate:ADPHa?","SCPI_Calibrate","AdphaQ"),//查询AD相位校准状态
//    new SCPICommandStruct(":CALibrate:ADGain","SCPI_Calibrate","AdGain"),//AD增益校准
//    new SCPICommandStruct(":CALibrate:ADGain?","SCPI_Calibrate","AdGinQ"),//查询AD增益校准状态
//    new SCPICommandStruct(":CALibrate:OFFSet","SCPI_Calibrate","Offset"),//偏移量校准
//    new SCPICommandStruct(":CALibrate:OFFSet?","SCPI_Calibrate","OffsetQ"),//查询偏移量校准状态
//    new SCPICommandStruct(":CALibrate:CHGain","SCPI_Calibrate","ChGain"),//通道增益校准
//    new SCPICommandStruct(":CALibrate:CHGain?","SCPI_Calibrate","ChGainQ"),//查询通道增益校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:ZERopoint","SCPI_Calibrate","Trigger_ZeroPoint"),//触发触发零点校准
//    new SCPICommandStruct(":CALibrate:TRIGger:ZERopoint?","SCPI_Calibrate","Trigger_ZeroPointQ"),//查询触发零点校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:AC:ZERopoint","SCPI_Calibrate","Trigger_AC_ZeroPoint"),//触发触发零点校准
//    new SCPICommandStruct(":CALibrate:TRIGger:AC:ZERopoint?","SCPI_Calibrate","Trigger_AC_ZeroPointQ"),//查询触发零点校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:COEFficient","SCPI_Calibrate","Trigger_Coefficient"),//触发系数校准
//    new SCPICommandStruct(":CALibrate:TRIGger:COEFficient?","SCPI_Calibrate","Trigger_CoefficientQ"),//查询触发系数校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:PRECise","SCPI_Calibrate","Trigger_Precise"),//精准触发校准
//    new SCPICommandStruct(":CALibrate:TRIGger:PRECise?","SCPI_Calibrate","Trigger_PreciseQ"),//查询精准触发校准状态
//    new SCPICommandStruct(":CALibrate:DATE:LENGth?","SCPI_Calibrate","Date_LengthQ"),//查询校准数据长度
//    new SCPICommandStruct(":CALibrate:DATE:GET","SCPI_Calibrate","Date_Get"),//获取校准数据
//    new SCPICommandStruct(":CALibrate:FILE:RESet?","SCPI_Calibrate","File_ResetQ"),//获取校准数据

    /**
     * 查询上次校准时间
     * @param param SCPI命令参数（本命令无参数）
     * @return 校准时间字符串
     */
    public static String DateQ(SCPIParam param){
        return Command.get().getCalibrate().DateQ(true); // 调用底层校准接口查询校准日期
    }

    /**
     * 开始校准流程
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Start(SCPIParam param){
        Logger.i("command","Start"); // 记录日志：开始校准命令
       Command.get().getCalibrate().Start(true); // 调用底层接口启动校准
    }

    /**
     * 退出校准流程（校准完成后的操作）
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Quit(SCPIParam param){
        Logger.i("command","Quit"); // 记录日志：退出校准命令
        Command.get().getCalibrate().Quit(true); // 调用底层接口退出校准
    }

    /**
     * 强制停止校准
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Stop(SCPIParam param){
        Logger.i("command","Stop"); // 记录日志：停止校准命令
        Command.get().getCalibrate().Stop(true); // 调用底层接口强制停止校准
    }

    /**
     * 查询校准结果
     * @param param SCPI命令参数（本命令无参数）
     * @return 校准结果字符串
     */
    public static String ResultQ(SCPIParam param){
        return  Command.get().getCalibrate().ResultQ(true); // 调用底层接口查询校准结果
    }

    /**
     * 执行零点校准
     * @param param SCPI命令参数（本命令无参数）
     * @return SCPI OK响应
     */
    public static String ZeroPoint(SCPIParam param){
        Command.get().getCalibrate().ZeroPoint(true); // 调用底层接口执行零点校准
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询零点校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 零点校准状态字符串
     */
    public static String ZeroPointQ(SCPIParam param){
        return Command.get().getCalibrate().ZeroPointQ(true); // 调用底层接口查询零点校准状态
    }

    /**
     * 执行通道差异校准
     * @param param SCPI命令参数（本命令无参数）
     * @return SCPI OK响应
     */
    public static String Chdf(SCPIParam param){
        Command.get().getCalibrate().Chdf(true); // 调用底层接口执行通道差异校准
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询通道差异校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 通道差异校准状态字符串
     */
    public static String ChdfQ(SCPIParam param){
        return Command.get().getCalibrate().ChdfQ(true); // 调用底层接口查询通道差异校准状态
    }

    /**
     * 执行AD相位校准
     * @param param SCPI命令参数，dParam1为AD相位校准参数
     * @return SCPI OK响应
     */
    public static String Adpha(SCPIParam param){
        Command.get().getCalibrate().Adpha(param.dParam1,true); // 调用底层接口执行AD相位校准，传入相位参数
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询AD相位校准状态
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void AdphaQ(SCPIParam param){
        Command.get().getCalibrate().AdphaQ(true); // 调用底层接口查询AD相位校准状态
    }

    /**
     * 执行AD增益校准
     * @param param SCPI命令参数（本命令无参数）
     * @return SCPI OK响应
     */
    public static String AdGain(SCPIParam param){
        Command.get().getCalibrate().AdGain(true); // 调用底层接口执行AD增益校准
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询AD增益校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return AD增益校准状态字符串
     */
    public static String AdGinQ(SCPIParam param){
         return  Command.get().getCalibrate().AdGinQ(true); // 调用底层接口查询AD增益校准状态
    }

    /**
     * 执行偏移量校准
     * @param param SCPI命令参数（本命令无参数）
     * @return SCPI OK响应
     */
    public static String Offset(SCPIParam param){
        Command.get().getCalibrate().Offset(true); // 调用底层接口执行偏移量校准
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询偏移量校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 偏移量校准状态字符串
     */
    public static String OffsetQ(SCPIParam param){
        return Command.get().getCalibrate().OffsetQ(true); // 调用底层接口查询偏移量校准状态
    }

    /** ch：这里的通道是从1开始的。通道1对应该1，所以为了与底层对应需要减1 */
    /**
     * 执行通道增益校准
     * @param param SCPI命令参数，dParam1为通道号（从1开始），dParam2/dParam3/dParam4为增益参数
     * @return SCPI OK响应
     */
    public static String ChGain(SCPIParam param){
         Command.get().getCalibrate().ChGain(param.dParam1-1,param.dParam2,param.dParam3,param.dParam4,true); // 调用底层接口执行通道增益校准，通道号减1以适配底层从0开始的索引
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询通道增益校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 通道增益校准状态字符串
     */
    public static String ChGainQ(SCPIParam param){
       return Command.get().getCalibrate().ChGainQ(true); // 调用底层接口查询通道增益校准状态
    }

    /**
     * 执行扩展通道增益校准
     * @param param SCPI命令参数，dParam1为通道号（从1开始），dParam2/dParam3/dParam4为增益参数
     * @return SCPI OK响应
     */
    public static String ExChGain(SCPIParam param){
        Command.get().getCalibrate().ExChGain(param.dParam1-1,param.dParam2,param.dParam3,param.dParam4,true); // 调用底层接口执行扩展通道增益校准，通道号减1适配底层
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询扩展通道增益校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 扩展通道增益校准状态字符串
     */
    public static String ExChGainQ(SCPIParam param){
        return Command.get().getCalibrate().ExChGainQ(true); // 调用底层接口查询扩展通道增益校准状态
    }

    /**
     * 设置通道校准值
     * @param param SCPI命令参数，dParam1为通道号（从1开始），dParam2为校准值
     * @return SCPI OK响应
     */
    public static String ChSetVal(SCPIParam param){
        Command.get().getCalibrate().ChSetVal(param.dParam1-1,param.dParam2,true); // 调用底层接口设置通道校准值，通道号减1适配底层
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询通道校准值
     * @param param SCPI命令参数，dParam1为通道号（从1开始）
     * @return 通道校准值字符串
     */
    public static String ChValQ(SCPIParam param){
        return Command.get().getCalibrate().ChValQ(param.dParam1-1,true); // 调用底层接口查询通道校准值，通道号减1适配底层
    }

    /**
     * 执行通道系数校准
     * @param param SCPI命令参数，dParam1为通道号（从1开始），dParam2/dParam3/dParam4为系数参数
     * @return SCPI OK响应
     */
    public static String ChCofit(SCPIParam param){
        Command.get().getCalibrate().ChCofit(param.dParam1-1,param.dParam2,param.dParam3,param.dParam4,true); // 调用底层接口执行通道系数校准，通道号减1适配底层
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询通道系数校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 通道系数校准状态字符串
     */
    public static String ChCofitQ(SCPIParam param){
        return Command.get().getCalibrate().ChCofitQ(true); // 调用底层接口查询通道系数校准状态
    }

    /** ch：这里的通道是从1开始的。通道1对应该1，所以为了与底层对应需要减1 */
    /**
     * 执行通道电容校准
     * @param param SCPI命令参数，dParam1为通道号（从1开始），dParam2为电容校准参数
     * @return SCPI OK响应
     */
    public static String ChCap(SCPIParam param){
        Logger.i("scpitest"," param1:"+(int)param.dParam1+"  param2:"+(int)param.dParam2); // 记录日志：通道电容校准参数
        Command.get().getCalibrate().ChCap(param.dParam1-1,param.dParam2,true); // 调用底层接口执行通道电容校准，通道号减1适配底层
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询通道电容校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 通道电容校准状态字符串
     */
    public static String ChCapQ(SCPIParam param){
        return Command.get().getCalibrate().ChCapQ(true); // 调用底层接口查询通道电容校准状态

    }

    /**
     * 设置电容校准值
     * @param param SCPI命令参数，iParam1为通道号（从1开始），dParam1为电容值，iParam2为索引
     * @return SCPI OK响应
     */
    public static String CapVal(SCPIParam param){

        Command.get().getCalibrate().CapVal(param.iParam1-1,param.dParam1,param.iParam2,true); // 调用底层接口设置电容校准值，通道号减1适配底层
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询电容校准值
     * @param param SCPI命令参数，iParam1为通道号（从1开始），dParam1为电容值
     * @return 电容校准值字符串
     */
    public static String CapValQ(SCPIParam param){
        return Command.get().getCalibrate().CapValQ(param.iParam1-1,param.dParam1,true); // 调用底层接口查询电容校准值，通道号减1适配底层
    }

    /**
     * 执行上校准
     * @param param SCPI命令参数（本命令无参数）
     * @return SCPI OK响应
     */
    public static String UpCal(SCPIParam param){
        Command.get().getCalibrate().UpCal(true); // 调用底层接口执行上校准
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询上校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 上校准状态字符串
     */
    public static String UpCalQ(SCPIParam param){
        return Command.get().getCalibrate().UpCalQ(true); // 调用底层接口查询上校准状态
    }

    /**
     * 执行下校准
     * @param param SCPI命令参数（本命令无参数）
     * @return SCPI OK响应
     */
    public static String DownCal(SCPIParam param){
        Command.get().getCalibrate().DownCal(true); // 调用底层接口执行下校准
        return ToolsSCPI.getOKAY(); // 返回SCPI命令执行成功响应
    }

    /**
     * 查询下校准状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 下校准状态字符串
     */
    public static String DownCalQ(SCPIParam param){
        return Command.get().getCalibrate().DownCalQ(true); // 调用底层接口查询下校准状态
    }

    /**
     * 执行触发零点校准
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Trigger_ZeroPoint(SCPIParam param){
        Command.get().getCalibrate().Trigger_ZeroPoint(true); // 调用底层接口执行触发零点校准
    }

    /**
     * 查询触发零点校准状态
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Trigger_ZeroPointQ(SCPIParam param){
        Command.get().getCalibrate().Trigger_ZeroPointQ(true); // 调用底层接口查询触发零点校准状态
    }

    /**
     * 执行触发AC零点校准
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Trigger_AC_ZeroPoint(SCPIParam param){
        Command.get().getCalibrate().Trigger_AC_ZeroPoint(true); // 调用底层接口执行触发AC零点校准
    }

    /**
     * 查询触发AC零点校准状态
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Trigger_AC_ZeroPointQ(SCPIParam param){
        Command.get().getCalibrate().Trigger_AC_ZeroPointQ(true); // 调用底层接口查询触发AC零点校准状态
    }

    /**
     * 执行触发系数校准
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Trigger_Coefficient(SCPIParam param){
        Command.get().getCalibrate().Trigger_Coefficient(true); // 调用底层接口执行触发系数校准
    }

    /**
     * 查询触发系数校准状态
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Trigger_CoefficientQ(SCPIParam param){
        Command.get().getCalibrate().Trigger_CoefficientQ(true); // 调用底层接口查询触发系数校准状态
    }

    /**
     * 执行精准触发校准
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Trigger_Precise(SCPIParam param){
        Command.get().getCalibrate().Trigger_Precise(true); // 调用底层接口执行精准触发校准
    }

    /**
     * 查询精准触发校准状态
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Trigger_PreciseQ(SCPIParam param){
        Command.get().getCalibrate().Trigger_PreciseQ(true); // 调用底层接口查询精准触发校准状态
    }

    /**
     * 查询校准数据长度
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Date_LengthQ(SCPIParam param){
        Command.get().getCalibrate().Date_LengthQ(true); // 调用底层接口查询校准数据长度
    }

    /**
     * 获取校准数据
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Date_Get(SCPIParam param){
        Command.get().getCalibrate().Date_Get(true); // 调用底层接口获取校准数据
    }

    /**
     * 查询校准数据文件重置状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 校准数据文件重置成功/失败状态字符串
     */
    public static String DatFile_ResetQ(SCPIParam param){
        boolean b= Command.get().getCalibrate().DatFile_ResetQ(true); // 调用底层接口查询校准数据文件重置状态
        return ToolsSCPI.getSuccState(b); // 将布尔结果转换为SCPI成功/失败状态字符串
    }


}
