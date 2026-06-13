package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.tbookscope.middleware.command.Command;

import java.util.Arrays;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Sample {
//    //采样命令 SAMP
//            new SCPICommandStruct(":SAMPle:TYPE","SCPI_Sample","Type"),//设置采样方式
//            new SCPICommandStruct(":SAMPle:TYPE?","SCPI_Sample","TypeQ"),//查询采样方式
//            new SCPICommandStruct(":SAMPle:MEAN","SCPI_Sample","Mean"),//设置平均采样次数。所设置的值为2的整数倍数。
//            new SCPICommandStruct(":SAMPle:MEAN?","SCPI_Sample","MeanQ"),//查询平均采样次数
//            new SCPICommandStruct(":SAMPle:ENVelop","SCPI_Sample","Envelop"),//设置包络采样次数。所设置的值为2的整数倍数或无穷
//            new SCPICommandStruct(":SAMPle:ENVelop?","SCPI_Sample","EnvelopQ"),//查询包络采样次数
//            new SCPICommandStruct(":SAMPle:SEGMented","SCPI_Sample","SegMented"),//设置分段存储的段数
//            new SCPICommandStruct(":SAMPle:SRATe?","SCPI_Sample","SrateQ"),//查询当前的采样率
//            new SCPICommandStruct(":ACQuire:MDEPth","SCPI_Sample","Mdepth"),//设置当前存储尝试
//            new SCPICommandStruct(":SAMPle:MDEPth?","SCPI_Sample","MdepthQ"),//查询示波器当前存储深度

    public static void Type(SCPIParam param) {
        Command.get().getSample().Type(param.iParam1, true);
    }

    public static String TypeQ(SCPIParam param) {
        int i= Command.get().getSample().TypeQ();
        return ToolsSCPI.getSampleType(i);
    }

    public static void Mean(SCPIParam param) {
        Command.get().getSample().Mean(param.iParam1, true);
    }

    public static String MeanQ(SCPIParam param) {
        int i= Command.get().getSample().MeanQ();
        return ToolsSCPI.getSampleEnvelop(i);
    }

    public static void Envelop(SCPIParam param) {
        Command.get().getSample().Envelop(param.iParam1, true);
    }

    public static String EnvelopQ(SCPIParam param) {
        int i= Command.get().getSample().EnvelopQ();
        return ToolsSCPI.getSampleEnvelop(i);
    }

    public static void SegMented(SCPIParam param) {
        Command.get().getSample().SegMented(param.bParam1, true);
    }

    public static String SegMentedQ(SCPIParam param){
        boolean i=Command.get().getSample().SegMentedQ();
        return ToolsSCPI.getOpenState(i);
    }

    //查询已存在段数
    public static String SegmentedNoQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedNoQ();
        return String.valueOf(i);
    }
    //设置段数
    public static void SegmentedQTY(SCPIParam param){
        Command.get().getSample().SegmentedQTY(param.iParam1,true);
    }
    //查询段数
    public static String SegmentedQTYQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedQTYQ();
        return String.valueOf(i);
    }
    public static String SegmentedIs10000Q(SCPIParam param){
        return ToolsSCPI.getOpenState(false);
    }
    public static String SegmentedMaxQ(SCPIParam param){
        int max=Command.get().getSample().SegmentedMaxQ();
        return String.valueOf(max);
    }
    //设置显示类型
    public static void SegmentedDisplayType(SCPIParam param){
        Command.get().getSample().SegmentedDisplayType(param.iParam1,true);
    }
    //查询显示类型
    public static String SegmentedDisplayTypeQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedDisplayTypeQ();
        return ToolsSCPI.getSegmentDisplayType(i);
    }

    public static void SegmentedOrder(SCPIParam param){
        Command.get().getSample().SegmentedOrder(param.iParam1,true);
    }
    public static String SegmentedOrderQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedOrderQ();
        return ToolsSCPI.getSegmentOrder(i);
    }
    public static void SegmentedPlay(SCPIParam param){
        Command.get().getSample().SegmentedPlay(true);
    }
    public static void SegmentedStop(SCPIParam param){
        Command.get().getSample().SegmentedStop(true);
    }
    public static void SegmentedFra1(SCPIParam param){
        Command.get().getSample().SegmentedFra1(param.iParam1,true);
    }
    public static String SegmentedFra1Q(SCPIParam param){
        int i=Command.get().getSample().SegmentedFra1Q();
        return String.valueOf(i);
    }
    public static void SegmentedFra2(SCPIParam param){
        Command.get().getSample().SegmentedFra2(param.iParam1,true);
    }
    public static String SegmentedFra2Q(SCPIParam param){
        int i=Command.get().getSample().SegmentedFra2Q();
        return String.valueOf(i);
    }
    public static void SegmentedFra3(SCPIParam param){
        Command.get().getSample().SegmentedFra3(param.iParam1,true);
    }
    public static String SegmentedFra3Q(SCPIParam param){
        int i=Command.get().getSample().SegmentedFra3Q();
        return String.valueOf(i);
    }
    public static void SegmentedPlaySpeed(SCPIParam param){
        Command.get().getSample().SegmentedPlaySpeed(param.iParam1,true);
    }
    public static String SegmentedPlaySpeedQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedPlaySpeedQ();
        return ToolsSCPI.getSegmentDisplaySpeed(i);
    }


    public static String SrateQ(SCPIParam param) {
        double d=Command.get().getSample().SrateQ();
        return String.valueOf(d);
    }

    public static void MdepthSelect(SCPIParam param){
        Command.get().getSample().Mdepth(param.sParam1, true);
    }
    public static String MdepthSelectQ(SCPIParam param) {
        int v = Command.get().getSample().MdepthQ();
        if (v==0){
            return "AUTO";
        }else {
            return String.valueOf(v);
        }
    }

    public static void MdepthSelectIndex(SCPIParam param){
        Command.get().getSample().Mdepth(param.sParam1, true);
    }
    public static String MdepthQ(SCPIParam param) {
        int i= MemDepthFactory.getSampleMemDepth();
        return String.valueOf(i);
    }
    public static String RangeQ(SCPIParam param){
        String s= Arrays.toString( MemDepthFactory.getMemDepth().getMemDepthItemName().toArray());
        return s.replace("[","").replace("]","");
    }

    public static String InitQ(SCPIParam param){
        String s = MemDepthFactory.getMemDepthObj(MemDepthFactory.getDefaultMemDepth()).getMemDepthInitName();
        return s;
    }
}
