package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Reference {
//     new SCPICommandStruct(":REFerence:DISPlay","SCPI_Reference","Display"),//打开或关闭REF功能
//            new SCPICommandStruct(":REFerence:DISPlay?","SCPI_Reference","DisplayQ"),//查询REF功能打开或关闭
//            new SCPICommandStruct(":REFerence:ENABle","SCPI_Reference","Enable"),//打开或关闭指定的参考通道
//            new SCPICommandStruct(":REFerence:ENABle?","SCPI_Reference","EnableQ"),//查询指定的参考通道打开或关闭
//            new SCPICommandStruct(":REFerence:HSCale","SCPI_Reference","Hscale"),//设置参考通道的水平档位
//            new SCPICommandStruct(":REFerence:PLUS:HSCale","SCPI_Reference","Plus_Hscale"),//设置参考通道的水平档位
//            new SCPICommandStruct(":REFerence:HSCale?","SCPI_Reference","HscaleQ"),//查询参考通道的水平档位
//            new SCPICommandStruct(":REFerence:VSCale","SCPI_Reference","Vscale"),//设置参考通道的垂直档位
//            new SCPICommandStruct(":REFerence:PLUS:VSCale","SCPI_Reference","Plus_Vscale"),//设置参考通道的垂直档位
//            new SCPICommandStruct(":REFerence:VSCale?","SCPI_Reference","VscaleQ"),//查询参考通道的垂直档位
//            new SCPICommandStruct(":REFerence:CURRent","SCPI_Reference","Current"),//选择当前参考通道
//            new SCPICommandStruct(":REFerence:PLUS:HOFFset","SCPI_Reference","Plus_Hoffset"),//选择当前参考通道
//            new SCPICommandStruct(":REFerence:PLUS:VOFFset","SCPI_Reference","Plus_Voffset"),//选择当前参考通道
//    //1.1新添加 2016.12.8
//            new SCPICommandStruct(":REFerence:POSition","SCPI_Reference","Position"),//设置垂直偏移
//            new SCPICommandStruct(":REFerence:POSition?","SCPI_Reference","PositionQ"),//查询垂直偏移
//            new SCPICommandStruct(":REFerence:TIMebase:POSition","SCPI_Reference","Timebase_Position"),//设置水平偏移
//            new SCPICommandStruct(":REFerence:TIMebase:POSition?","SCPI_Reference","Timebase_PositionQ"),//查询水平偏移
//            new SCPICommandStruct(":REFerence:PLUS:TIMebase:POSition","SCPI_Reference","Plus_Timebase_Position"),//设置水平偏移
//            new SCPICommandStruct(":REFerence:PLUS:POSition","SCPI_Reference","Plus_position"),//设置垂直偏移
//            new SCPICommandStruct(":REF:SRATe?","SCPI_Reference","REF_SRateQ"),//查询采样率
//            new SCPICommandStruct(":REF:MDEPth?","SCPI_Reference","REF_MDepthQ"),//查询存储深度

    public static void Display(SCPIParam param) {
        Command.get().getReference().Display(param.iParam1, param.bParam1, true);
    }

    public static String DisplayQ(SCPIParam param) {
        boolean b=Command.get().getReference().DisplayQ();
        return ToolsSCPI.getOpenState(b);
    }

    public static void Enable(SCPIParam param) {
        Command.get().getReference().Enable(param.iParam1, param.bParam1, true);
    }

    public static String EnableQ(SCPIParam param) {
        boolean b=Command.get().getReference().EnableQ(param.iParam1);
        return ToolsSCPI.getOpenState(b);
    }

    public static void Hscale(SCPIParam param) {
        Command.get().getReference().Hscale(param.iParam1, param.dParam1, true);
    }

    public static void Plus_Hscale(SCPIParam param) {
        Command.get().getReference().Plus_Hscale(param.iParam1, param.iParam2, true);
    }

    public static String HscaleQ(SCPIParam param) {
//        double d=Command.get().getReference().HscaleQ(param.iParam1);
//        return ToolsSCPI.getDouble(d);
        return CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.toUiChNo(param.iParam1));
    }

    public static void Vscale(SCPIParam param) {
        Command.get().getReference().Vscale(param.iParam1, param.dParam1, true);
    }

    public static void Plus_Vscale(SCPIParam param) {
        Command.get().getReference().Plus_Vscale(param.iParam1, param.iParam2, true);
    }

    public static String VscaleQ(SCPIParam param) {
//        double d=Command.get().getReference().VscaleQ(param.iParam1);
//        return ToolsSCPI.getDouble(d);
        if (ChannelFactory.isRefCh(param.iParam1)) {
            RefChannel ref = ChannelFactory.getRefChannel(param.iParam1);
            return String.valueOf(ref.getVScaleVal());
        }
        return "";
    }

    public static void Current(SCPIParam param) {
        Command.get().getReference().Current(param.iParam1, true);
    }

    public static void Plus_Hoffset(SCPIParam param) {
        Command.get().getReference().Plus_Hoffset(param.iParam1, param.iParam2, true);
    }

    public static void Plus_Voffset(SCPIParam param) {
        Command.get().getReference().Plus_Voffset(param.iParam1, param.iParam2, true);
    }

    public static void Position(SCPIParam param) {
        Command.get().getReference().Position(param.iParam1, param.dParam1, true);
    }
    public static String PositionQ(SCPIParam param){
        double d= Command.get().getReference().PositionQ(param.iParam1, true);
        return ToolsSCPI.getDouble(d);
    }

    public static void Timebase_Position(SCPIParam param) {
        Command.get().getReference().Timebase_Position(param.iParam1, param.dParam1, true);
    }
    public static String Timebase_PositionQ(SCPIParam param){
        double d= Command.get().getReference().Timebase_PositionQ(param.iParam1, true);
        return ToolsSCPI.getDouble(d);
    }
    public static void Plus_Timebase_Position(SCPIParam param) {
        Command.get().getReference().Plus_Timebase_Position(param.iParam1, param.iParam2, true);
    }

    public static void Plus_position(SCPIParam param) {
        Command.get().getReference().Plus_position(param.iParam1, param.iParam2, true);
    }
    public static String REF_SRateQ(SCPIParam param){
        return Command.get().getReference().REF_SRateQ(param.iParam1);

    }
    public static String REF_MDepthQ(SCPIParam param){
        return Command.get().getReference().REF_MDepthQ(param.iParam1);

    }

    public static void Curr_ref(SCPIParam param){
        Command.get().getChannel().Current(param.iParam1, true);
    }

}
