package com.micsig.tbook.tbookscope.scpi;

import android.os.SystemClock;
import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Measure {
    private static final String TAG="SCPI_Measure";
    //测量命令 MEAS
//            new SCPICommandStruct(":MEASure:PERiod?","SCPI_Measure","PeriodQ"),//查询指定通道波形的周期测量值
//            new SCPICommandStruct(":MEASure:FREQuency?","SCPI_Measure","FreQuencyQ"),//查询指定通道波形的频率测量值
//            new SCPICommandStruct(":MEASure:RISetime?","SCPI_Measure","RiseTimeQ"),//查询指定通道波形的上升时间测量值
//            new SCPICommandStruct(":MEASure:FALLtime?","SCPI_Measure","FallTimeQ"),//查询指定通道波形的下降时间测量值
//            new SCPICommandStruct(":MEASure:DELay?","SCPI_Measure","DelayQ"),//查询通道间延迟测量的结果
//            new SCPICommandStruct(":MEASure:PDUTy?","SCPI_Measure","PDutyQ"),//查询指定通道波形的正占空比测量值
//            new SCPICommandStruct(":MEASure:NDUTy?","SCPI_Measure","NDutyQ"),//查询指定通道波形的负占空比测量值
//            new SCPICommandStruct(":MEASure:PWIDth?","SCPI_Measure","PWidthQ"),//查询指定通道波形的正脉宽测量值
//            new SCPICommandStruct(":MEASure:NWIDth?","SCPI_Measure","NWidthQ"),//查询指定通道波形的负脉宽测量值
//            new SCPICommandStruct(":MEASure:BURStwidth?","SCPI_Measure","BurstWidthQ"),//查询指定通道波形的突发脉冲宽度测量值
//            new SCPICommandStruct(":MEASure:ROV?","SCPI_Measure","RovQ"),//查询指定通道波形的正向超调测量值
//            new SCPICommandStruct(":MEASure:FOV?","SCPI_Measure","FovQ"),//查询指定通道波形的负向超调测量值
//            new SCPICommandStruct(":MEASure:PHASe?","SCPI_Measure","PhaseQ"),//查询指定通道间相位差测量的结果
//            new SCPICommandStruct(":MEASure:PKPK?","SCPI_Measure","PkpkQ"),//查询指定通道波形的峰峰值
//            new SCPICommandStruct(":MEASure:AMP?","SCPI_Measure","AmpQ"),//查询指定通道波形的幅度测量值
//            new SCPICommandStruct(":MEASure:HIGH?","SCPI_Measure","HighQ"),//查询指定通道波形的高值
//            new SCPICommandStruct(":MEASure:LOW?","SCPI_Measure","LowQ"),//查询指定通道波形的低值
//            new SCPICommandStruct(":MEASure:MAX?","SCPI_Measure","MaxQ"),//查询指定通道波形的最大值
//            new SCPICommandStruct(":MEASure:MIN?","SCPI_Measure","MinQ"),//查询指定通道波形的最小值
//            new SCPICommandStruct(":MEASure:RMS?","SCPI_Measure","RmsQ"),//查询指定通道波形的均方根值
//            new SCPICommandStruct(":MEASure:CRMS?","SCPI_Measure","CrmsQ"),//查询指定通道波形的周期均方根值
//            new SCPICommandStruct(":MEASure:MEAN?","SCPI_Measure","MeanQ"),//查询指定通道波形的平均值
//            new SCPICommandStruct(":MEASure:CMEan?","SCPI_Measure","CMeanQ"),//查询指定通道波形的周期平均值
//            new SCPICommandStruct(":MEASure:ACRMs?","SCPI_Measure","ACRMs"),//
//            new SCPICommandStruct(":MEASure:PRATe?","SCPI_Measure","PRATe"),//
//            new SCPICommandStruct(":MEASure:NRATe?","SCPI_Measure","NRATe"),//
//            new SCPICommandStruct(":MEASure:COLVal?","SCPI_Measure","ColValQ"),

    public static String PeriodQ(SCPIParam param){
         return Command.get().getMeasure().PeriodQ(param.iParam1);
    }
    public static String FreQuencyQ(SCPIParam param){
         return Command.get().getMeasure().FreQuencyQ(param.iParam1);
    }
    public static String RiseTimeQ(SCPIParam param){
        return Command.get().getMeasure().RiseTimeQ(param.iParam1);

    }
    public static String FallTimeQ(SCPIParam param){
        return Command.get().getMeasure().FallTimeQ(param.iParam1);
    }
    public static String DelayQ(SCPIParam param){
        return Command.get().getMeasure().DelayQ(param.iParam1,param.iParam2);
    }
    public static String PDutyQ(SCPIParam param){
        return Command.get().getMeasure().PDutyQ(param.iParam1);
    }
    public static String NDutyQ(SCPIParam param){
        return Command.get().getMeasure().NDutyQ(param.iParam1);
    }
    public static String PWidthQ(SCPIParam param){
        return Command.get().getMeasure().PWidthQ(param.iParam1);
    }
    public static String NWidthQ(SCPIParam param){
        return Command.get().getMeasure().NWidthQ(param.iParam1);
    }
    public static String BurstWidthQ(SCPIParam param){
        return Command.get().getMeasure().BurstWidthQ(param.iParam1);
    }
    public static String RovQ(SCPIParam param){
        return Command.get().getMeasure().RovQ(param.iParam1);
    }
    public static String FovQ(SCPIParam param){
        return Command.get().getMeasure().FovQ(param.iParam1);
    }
    public static String PhaseQ(SCPIParam param){
        return Command.get().getMeasure().PhaseQ(param.iParam1,param.iParam2);
    }
    public static String PkpkQ(SCPIParam param){
        return Command.get().getMeasure().PkpkQ(param.iParam1);
    }
    public static String AmpQ(SCPIParam param){
        return Command.get().getMeasure().AmpQ(param.iParam1);
    }
    public static String HighQ(SCPIParam param){
        return Command.get().getMeasure().HighQ(param.iParam1);
    }
    public static String LowQ(SCPIParam param){
        return Command.get().getMeasure().LowQ(param.iParam1);
    }
    public static String MaxQ(SCPIParam param){
        return Command.get().getMeasure().MaxQ(param.iParam1);
    }
    public static String MinQ(SCPIParam param){
        return Command.get().getMeasure().MinQ(param.iParam1);
    }
    public static String RmsQ(SCPIParam param){
        return Command.get().getMeasure().RmsQ(param.iParam1);

    }
    public static String CrmsQ(SCPIParam param){
        return Command.get().getMeasure().CrmsQ(param.iParam1);
    }
    public static String MeanQ(SCPIParam param){
        return Command.get().getMeasure().MeanQ(param.iParam1);

    }
    public static String CMeanQ(SCPIParam param){
        return Command.get().getMeasure().CMeanQ(param.iParam1);
    }
    public static String ACRMSQ(SCPIParam param){
        return Command.get().getMeasure().ACRMSQ(param.iParam1);
    }
    public static String PRATEQ(SCPIParam param){
        return Command.get().getMeasure().PRATEQ(param.iParam1);
    }
    public static String NRATEQ(SCPIParam param){
        return Command.get().getMeasure().NRATEQ(param.iParam1);
    }

    public static String ColValQ(SCPIParam param){
        return Command.get().getMeasure().ColValQ(param.iParam1);
    }

//     new SCPICommandStruct(":MEASure:AREa?","SCPI_Measure","AreaQ"),//查询指定通道波形的面积
//            new SCPICommandStruct(":MEASure:CARea?","SCPI_Measure","CareaQ"),//查询指定通道波形的周期面积
//            new SCPICommandStruct(":MEASure:CLEar","SCPI_Measure","Clear"),//清除打开的测量项中的任一项或所有项
//            new SCPICommandStruct(":MEASure:OPEN","SCPI_Measure","Open"),//打开测量项
//            new SCPICommandStruct(":MEASure:CLOSe","SCPI_Measure","Close"),//关闭测量项
//            new SCPICommandStruct(":MEASure:STATistic:DISPlay","SCPI_Measure","Statistic_Display"),//打开或关闭统计功能
//            new SCPICommandStruct(":MEASure:STATistic:DISPlay?","SCPI_Measure","Statistic_DisplayQ"),//查询统计功能打开或关闭
//            new SCPICommandStruct(":MEASure:STATistic:RESet","SCPI_Measure","Statistic_Reset"),//清楚历史统计数据并重新统计
//            new SCPICommandStruct(":MEASure:ADISplay","SCPI_Measure","Adislay"),//打开或关闭全部测量
//            new SCPICommandStruct(":MEASure:ADISplay?","SCPI_Measure","AdisplayQ"),//查询全部测量打开或关闭
//            new SCPICommandStruct(":MEASure:SCOPe","SCPI_Measure","Scope"),//设置测量范围
//            new SCPICommandStruct(":MEASure:SCOPe?","SCPI_Measure","ScopeQ"),//查询测量范围
//            new SCPICommandStruct(":MEASure:COUNter:SOURce","SCPI_Measure","Counter_Source"),//设置源
//            new SCPICommandStruct(":MEASure:COUNter:SOURce?","SCPI_Measure","Counter_SourceQ"),//设置源
//           new SCPICommandStruct(":MEASure:COUNter:MODE","SCPI_Measure","Counter_Mode"),
//            new SCPICommandStruct(":MEASure:COUNter:MODE?","SCPI_Measure","Counter_ModeQ"),
//            new SCPICommandStruct(":MEASure:COUNter:VALue?","SCPI_Measure","Counter_ValueQ"),//查询频率计结果
//            new SCPICommandStruct(":MEASure:ITEM","SCPI_Measure","Item"),//设置信源
//            new SCPICommandStruct(":MEASure:ITEM?","SCPI_Measure","ItemQ"),//设置信源

    public static void AreaQ(SCPIParam param){}
    public static void CareaQ(SCPIParam param){}
    public static void Clear(SCPIParam param){
        if (param.iParam1 == GlobalVar.get().getMeasureItemCount()) {
            Command.get().getMeasure().ClearAllItem(true);
        } else {
            Command.get().getMeasure().Clear(param.iParam1, true);
        }
    }
    public static void Open(SCPIParam param){
        Command.get().getMeasure().Open(-1,param.iParam1,param.iParam2,param.iParam3,param.iParam4,param.iParam5,param.dParam1,true);
    }
    public static void Close(SCPIParam param){
        Command.get().getMeasure().Close(param.iParam1,param.iParam2,true);
    }
    public static void Statistic_Display(SCPIParam param){}
    public static void Statistic_DisplayQ(SCPIParam param){}
    public static void Statistic_Reset(SCPIParam param){}
    public static void Adislay(SCPIParam param){
        Command.get().getMeasure().Adislay(param.bParam1,true);
    }
    public static String AdisplayQ(SCPIParam param){
        boolean b= Command.get().getMeasure().AdisplayQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static void Scope(SCPIParam param){}
    public static void ScopeQ(SCPIParam param){}
    public static void Counter_Source(SCPIParam param){
        Command.get().getMeasure().Counter_Source(param.iParam1, true);
    }
    public static String Counter_SourceQ(SCPIParam param){
         int i= Command.get().getMeasure().Counter_SourceQ();
         return ToolsSCPI.getCounterCh(i);
    }

    public static void TVALue(SCPIParam param){
        param.iParam3 = param.iParam2;
        param.iParam2 = param.iParam1;
        param.iParam1 = MeasureManage.IMeasure.MeasureId_TVALUE;
        Open(param);
    }

    public static String  TVALueQ(SCPIParam param){
        return Command.get().getMeasure().TValueQ(param.iParam1);
    }

    public static void Counter_Mode(SCPIParam param){
            Command.get().getMeasure().Counter_Mode(param.iParam1, true);
    }
    public static String Counter_ModeQ(SCPIParam param){
        int i=Command.get().getMeasure().Counter_ModeQ();
        return ToolsSCPI.getCountMode(i);
    }

    public static String Counter_ValueQ(SCPIParam param){
        return Command.get().getMeasure().Counter_ValueQ();

    }

    private static String ss;
    public static void Item(SCPIParam param){
        long c= SystemClock.elapsedRealtime();

        int max=1024/2*256; //256k
        StringBuilder sb=new StringBuilder();
        for (int i=0;i<max;i++){
            sb.append( Integer.toHexString(i & 0xFF));
        }
        ss=sb.toString();
        c= SystemClock.elapsedRealtime()-c;
        Logger.i(Command.TAG,"create complete:"+c);
    }

    public static String ItemQ(SCPIParam param){
//        Item(param);
//        Logger.i(Command.TAG,"len:"+ss.length());
        StringBuilder sb=new StringBuilder();
        sb.append("#9000000000#");
        sb.replace(3,11,String.format("%08X",0x7FFFFFFF));
        return sb.toString();
    }

    public static String ListQ(SCPIParam param) {
        return Command.get().getMeasure().ListQ();
    }


    public static void AddNew(SCPIParam param){
        Command.get().getMeasure().AddNew(param.iParam1 - 1);
    }

    public static void Delete(SCPIParam param){
        Command.get().getMeasure().Delete(param.iParam1 - 1);
    }

    public static void XType(SCPIParam param){
        Command.get().getMeasure().XType(param);
    }
    public static String XTypeQ(SCPIParam param){
        return Command.get().getMeasure().XTypeQ(param.iParam1 - 1);
    }

    public static void XSOURce1(SCPIParam param){
        Command.get().getMeasure().XSOURce1(param.iParam1-1,param.iParam2);
    }
    public static String XSOURce1Q(SCPIParam param){
        return Command.get().getMeasure().XSOURce1Q(param.iParam1-1);
    }

    public static void XSOURce2(SCPIParam param){
        Command.get().getMeasure().XSOURce2(param.iParam1-1,param.iParam2);
    }
    public static String XSOURce2Q(SCPIParam param){
        return Command.get().getMeasure().XSOURce2Q(param.iParam1-1);
    }

    public static String XVALueQ(SCPIParam param){
        return Command.get().getMeasure().XVALueQ(param.iParam1-1);
    }
    public static String XUnitQ(SCPIParam param){
        return Command.get().getMeasure().XUnitQ(param.iParam1-1);
    }
    public static String XValidQ(SCPIParam param){
        return Command.get().getMeasure().XValidQ(param.iParam1-1);
    }
    public static void XEdge1(SCPIParam param){
        Command.get().getMeasure().XEdge1(param.iParam1-1, param.iParam2);
    }
    public static String XEdge1Q(SCPIParam param){
        return Command.get().getMeasure().XEdge1Q(param.iParam1-1);
    }
    public static void XEdge2(SCPIParam param){
        Command.get().getMeasure().XEdge2(param.iParam1-1, param.iParam2);
    }
    public static String XEdge2Q(SCPIParam param){
        return Command.get().getMeasure().XEdge2Q(param.iParam1-1);
    }
    public static void XCURSor(SCPIParam param){
        Command.get().getMeasure().XCURSor(param.iParam1-1,param.iParam2);
    }
    public static String XCURSorQ(SCPIParam param){
        return Command.get().getMeasure().XCURSorQ(param.iParam1-1);
    }
    public static void XVVLue(SCPIParam param){
        Command.get().getMeasure().XVVLue(param.iParam1-1,param.dParam1);
    }
    public static String XVVLueQ(SCPIParam param){
        return Command.get().getMeasure().XVVLueQ(param.iParam1-1);
    }
}
