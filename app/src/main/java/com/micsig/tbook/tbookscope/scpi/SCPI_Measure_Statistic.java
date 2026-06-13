package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.tools.Tools;

/**
 * @auother Liwb
 * @description:
 * @data:2022-12-1 11:32
 */
public class SCPI_Measure_Statistic {
//     new SCPICommandStruct(":MEASure:STATistic:DISPlay","SCPI_Measure_Statistic","Display"),//打开或关闭统计功能
//     new SCPICommandStruct(":MEASure:STATistic:DISPlay?","SCPI_Measure_Statistic","DisplayQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:RESet","SCPI_Measure_Statistic","Reset"),//重新统计
//     new SCPICommandStruct(":MEASure:STATistic:MEAN","SCPI_Measure_Statistic","Mean"),//打开或关闭平均值
//     new SCPICommandStruct(":MEASure:STATistic:MEAN?","SCPI_Measure_Statistic","MeanQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:MAX","SCPI_Measure_Statistic","Max"),//打开或关闭最大值
//     new SCPICommandStruct(":MEASure:STATistic:MAX?","SCPI_Measure_Statistic","MaxQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:MIN","SCPI_Measure_Statistic","Min"),//打开或关闭最小值
//     new SCPICommandStruct(":MEASure:STATistic:MIN?","SCPI_Measure_Statistic","MinQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:DEV","SCPI_Measure_Statistic","Dev"),//打开或关闭delta
//     new SCPICommandStruct(":MEASure:STATistic:DEV?","SCPI_Measure_Statistic","DevQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:COUNt","SCPI_Measure_Statistic","count"),//打开或关闭平均值
//     new SCPICommandStruct(":MEASure:STATistic:COUNt?","SCPI_Measure_Statistic","countQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:VIEW?","SCPI_Measure_Statistic","ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:MEAN:VIEW?","SCPI_Measure_Statistic","Mean_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:MAX:VIEW?","SCPI_Measure_Statistic","Max_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:MIN:VIEW?","SCPI_Measure_Statistic","Min_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:DEV:VIEW?","SCPI_Measure_Statistic","Dev_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:COUNt:VIEW?","SCPI_Measure_Statistic","Count_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:CURRent:VIEW?","SCPI_Measure_Statistic","Current_ViewQ"), //询问统计羡慕的所有数值

//            new SCPICommandStruct(":MEASure:SETTing:INDicator","SCPI_Measure_Statistic","Indicator"), //
//            new SCPICommandStruct(":MEASure:SETTing:INDicator?","SCPI_Measure_Statistic","IndicatorQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:RANGe","SCPI_Measure_Statistic","Range"), //
//            new SCPICommandStruct(":MEASure:SETTing:RANGe?","SCPI_Measure_Statistic","RangeQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:ThReshold","SCPI_Measure_Statistic","Threshold"), //
//            new SCPICommandStruct(":MEASure:SETTing:ThReshold?","SCPI_Measure_Statistic","ThresholdQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:HIGH","SCPI_Measure_Statistic","High"), //
//            new SCPICommandStruct(":MEASure:SETTing:HIGH?","SCPI_Measure_Statistic","HighQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:MID","SCPI_Measure_Statistic","Mid"), //
//            new SCPICommandStruct(":MEASure:SETTing:MID?","SCPI_Measure_Statistic","MidQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:LOW","SCPI_Measure_Statistic","Low"), //
//            new SCPICommandStruct(":MEASure:SETTing:LOW?","SCPI_Measure_Statistic","LowQ"), //

    public static void Display(SCPIParam param){
        Command.get().getMeasure_statistic().Display(param.bParam1, true);
    }
    public static String DisplayQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().DisplayQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static void Reset(SCPIParam param){
        Command.get().getMeasure_statistic().Reset();
    }
    public static void Mean(SCPIParam param){
        Command.get().getMeasure_statistic().Mean(param.bParam1, true);
    }
    public static String MeanQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().MeanQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static void Max(SCPIParam param){
        Command.get().getMeasure_statistic().Max(param.bParam1, true);
    }
    public static String MaxQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().MaxQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static void Min(SCPIParam param){
        Command.get().getMeasure_statistic().Min(param.bParam1, true);
    }
    public static String MinQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().MinQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static void Dev(SCPIParam param){
        Command.get().getMeasure_statistic().Dev(param.bParam1, true);
    }
    public static String DevQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().DevQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static void Count(SCPIParam param){
        Command.get().getMeasure_statistic().Count(param.bParam1, true);
    }
    public static String CountQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().CountQ();
        return ToolsSCPI.getOpenState(b);
    }

    public static String ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().ViewQ(param.iParam1, param.iParam2);
    }
    public static String Mean_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Mean_ViewQ(param.iParam1, param.iParam2);
    }
    public static String Max_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Max_ViewQ(param.iParam1, param.iParam2);
    }
    public static String Min_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Min_ViewQ(param.iParam1, param.iParam2);
    }
    public static String Dev_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Dev_ViewQ(param.iParam1, param.iParam2);
    }
    public static String Count_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Count_ViewQ(param.iParam1, param.iParam2);
    }
    public static String Current_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Current_ViewQ(param.iParam1, param.iParam2);
    }


    public static void Indicator(SCPIParam param){
        Command.get().getMeasure_setting().Indicator(param.bParam1, true);
    }
    public static void Range(SCPIParam param){
        Command.get().getMeasure_setting().Range(param.iParam1, true);
    }
    public static void Threshold(SCPIParam param){
        Command.get().getMeasure_setting().Threshold(param.iParam1,true);
    }
    public static void High(SCPIParam param){
        Command.get().getMeasure_setting().High(param.sParam1, true);
    }
    public static void Mid(SCPIParam param){
        Command.get().getMeasure_setting().Mid(param.sParam1, true);
    }
    public static void Low(SCPIParam param){
        Command.get().getMeasure_setting().Low(param.sParam1, true);
    }


    public static String IndicatorQ(SCPIParam param){
        boolean b=Command.get().getMeasure_setting().IndicatorQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static String RangeQ(SCPIParam param){
        int index=Command.get().getMeasure_setting().RangeQ();
        return ToolsSCPI.getMeasureSettingRange(index);
    }
    public static String ThresholdQ(SCPIParam param){
        int index=Command.get().getMeasure_setting().ThresholdQ();
        return ToolsSCPI.getMeasureSettingThreshold(index);
    }
    public static String HighQ(SCPIParam param){
        return Command.get().getMeasure_setting().HighQ();
    }
    public static String MidQ(SCPIParam param){
        return Command.get().getMeasure_setting().MidQ();
    }
    public static String LowQ(SCPIParam param){
        return Command.get().getMeasure_setting().LowQ();
    }

}
