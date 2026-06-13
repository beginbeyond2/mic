package com.micsig.tbook.tbookscope.scpi;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.middleware.command.Command;

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

    public static String DateQ(SCPIParam param){
        return Command.get().getCalibrate().DateQ(true);
    }
    public static void Start(SCPIParam param){
        Logger.i("command","Start");
       Command.get().getCalibrate().Start(true);
    }
    public static void Quit(SCPIParam param){
        Logger.i("command","Quit");
        Command.get().getCalibrate().Quit(true);
    }
    public static void Stop(SCPIParam param){
        Logger.i("command","Stop");
        Command.get().getCalibrate().Stop(true);
    }
    public static String ResultQ(SCPIParam param){
        return  Command.get().getCalibrate().ResultQ(true);
    }
    public static String ZeroPoint(SCPIParam param){
        Command.get().getCalibrate().ZeroPoint(true);
        return ToolsSCPI.getOKAY();
    }
    public static String ZeroPointQ(SCPIParam param){
        return Command.get().getCalibrate().ZeroPointQ(true);
    }
    public static String Chdf(SCPIParam param){
        Command.get().getCalibrate().Chdf(true);
        return ToolsSCPI.getOKAY();
    }
    public static String ChdfQ(SCPIParam param){
        return Command.get().getCalibrate().ChdfQ(true);
    }
    public static String Adpha(SCPIParam param){
        Command.get().getCalibrate().Adpha(param.dParam1,true);
        return ToolsSCPI.getOKAY();
    }
    public static void AdphaQ(SCPIParam param){
        Command.get().getCalibrate().AdphaQ(true);
    }
    public static String AdGain(SCPIParam param){
        Command.get().getCalibrate().AdGain(true);
        return ToolsSCPI.getOKAY();
    }
    public static String AdGinQ(SCPIParam param){
         return  Command.get().getCalibrate().AdGinQ(true);
    }
    public static String Offset(SCPIParam param){
        Command.get().getCalibrate().Offset(true);
        return ToolsSCPI.getOKAY();
    }
    public static String OffsetQ(SCPIParam param){
        return Command.get().getCalibrate().OffsetQ(true);
    }
    /** ch：这里的通道是从1开始的。通道1对应该1，所以为了与底层对应需要减1 */
    public static String ChGain(SCPIParam param){
         Command.get().getCalibrate().ChGain(param.dParam1-1,param.dParam2,param.dParam3,param.dParam4,true);
        return ToolsSCPI.getOKAY();
    }
    public static String ChGainQ(SCPIParam param){
       return Command.get().getCalibrate().ChGainQ(true);
    }
    public static String ExChGain(SCPIParam param){
        Command.get().getCalibrate().ExChGain(param.dParam1-1,param.dParam2,param.dParam3,param.dParam4,true);
        return ToolsSCPI.getOKAY();
    }
    public static String ExChGainQ(SCPIParam param){
        return Command.get().getCalibrate().ExChGainQ(true);
    }

    public static String ChSetVal(SCPIParam param){
        Command.get().getCalibrate().ChSetVal(param.dParam1-1,param.dParam2,true);
        return ToolsSCPI.getOKAY();
    }
    public static String ChValQ(SCPIParam param){
        return Command.get().getCalibrate().ChValQ(param.dParam1-1,true);
    }

    public static String ChCofit(SCPIParam param){
        Command.get().getCalibrate().ChCofit(param.dParam1-1,param.dParam2,param.dParam3,param.dParam4,true);
        return ToolsSCPI.getOKAY();
    }
    public static String ChCofitQ(SCPIParam param){
        return Command.get().getCalibrate().ChCofitQ(true);
    }

    /** ch：这里的通道是从1开始的。通道1对应该1，所以为了与底层对应需要减1 */
    public static String ChCap(SCPIParam param){
        Logger.i("scpitest"," param1:"+(int)param.dParam1+"  param2:"+(int)param.dParam2);
        Command.get().getCalibrate().ChCap(param.dParam1-1,param.dParam2,true);
        return ToolsSCPI.getOKAY();
    }
    public static String ChCapQ(SCPIParam param){
        return Command.get().getCalibrate().ChCapQ(true);

    }
    public static String CapVal(SCPIParam param){

        Command.get().getCalibrate().CapVal(param.iParam1-1,param.dParam1,param.iParam2,true);
        return ToolsSCPI.getOKAY();
    }
    public static String CapValQ(SCPIParam param){
        return Command.get().getCalibrate().CapValQ(param.iParam1-1,param.dParam1,true);
    }
    public static String UpCal(SCPIParam param){
        Command.get().getCalibrate().UpCal(true);
        return ToolsSCPI.getOKAY();
    }
    public static String UpCalQ(SCPIParam param){
        return Command.get().getCalibrate().UpCalQ(true);
    }

    public static String DownCal(SCPIParam param){
        Command.get().getCalibrate().DownCal(true);
        return ToolsSCPI.getOKAY();
    }
    public static String DownCalQ(SCPIParam param){
        return Command.get().getCalibrate().DownCalQ(true);
    }
    public static void Trigger_ZeroPoint(SCPIParam param){
        Command.get().getCalibrate().Trigger_ZeroPoint(true);
    }
    public static void Trigger_ZeroPointQ(SCPIParam param){
        Command.get().getCalibrate().Trigger_ZeroPointQ(true);
    }
    public static void Trigger_AC_ZeroPoint(SCPIParam param){
        Command.get().getCalibrate().Trigger_AC_ZeroPoint(true);
    }
    public static void Trigger_AC_ZeroPointQ(SCPIParam param){
        Command.get().getCalibrate().Trigger_AC_ZeroPointQ(true);
    }
    public static void Trigger_Coefficient(SCPIParam param){
        Command.get().getCalibrate().Trigger_Coefficient(true);
    }
    public static void Trigger_CoefficientQ(SCPIParam param){
        Command.get().getCalibrate().Trigger_CoefficientQ(true);
    }
    public static void Trigger_Precise(SCPIParam param){
        Command.get().getCalibrate().Trigger_Precise(true);
    }
    public static void Trigger_PreciseQ(SCPIParam param){
        Command.get().getCalibrate().Trigger_PreciseQ(true);
    }
    public static void Date_LengthQ(SCPIParam param){
        Command.get().getCalibrate().Date_LengthQ(true);
    }
    public static void Date_Get(SCPIParam param){
        Command.get().getCalibrate().Date_Get(true);
    }
    public static String DatFile_ResetQ(SCPIParam param){
        boolean b= Command.get().getCalibrate().DatFile_ResetQ(true);
        return ToolsSCPI.getSuccState(b);
    }


}
