package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Channel {
//    //通道命令 CHAN 1.0协议
//    new SCPICommandStruct(":CHANnel:DISPlay","SCPI_Channel","Display"),//通道的打开或关闭
//    new SCPICommandStruct(":CHANnel:DISPlay?","SCPI_Channel","DisplayQ"),//查询通道的打开或关闭
//    new SCPICommandStruct(":CHANnel:INVerse","SCPI_Channel","Inverse"),//打开或关闭通道的反相显示
//    new SCPICommandStruct(":CHANnel:INVerse?","SCPI_Channel","InverseQ"),//查询通道的反相显示
//    new SCPICommandStruct(":CHANnel:INVert","SCPI_Channel","Invert"),//打开或关闭通道的反相显示
//    new SCPICommandStruct(":CHANnel:INVert?","SCPI_Channel","InvertQ"),//查询通道的反相显示
//    new SCPICommandStruct(":CHANnel:BAND","SCPI_Channel","Band"),//设置通道的带宽限制
//    new SCPICommandStruct(":CHANnel:BAND?","SCPI_Channel","BandQ"),//查询通道的带宽限制
//    new SCPICommandStruct(":CHANnel:PRTY","SCPI_Channel","Prty"),//设置通道的探针类型
//    new SCPICommandStruct(":CHANnel:PRTY?","SCPI_Channel","PrtyQ"),//查询通道的探针类型
//    new SCPICommandStruct(":CHANnel:PROBe","SCPI_Channel","Probe"),//设置探头的衰减比
//    new SCPICommandStruct(":CHANnel:PROBe?","SCPI_Channel","ProbeQ"),//查询探头的衰减比
//    new SCPICommandStruct(":CHANnel:COUPle","SCPI_Channel","Couple"),//设置通道输入耦合方式
//    new SCPICommandStruct(":CHANnel:COUPle?","SCPI_Channel","CoupleQ"),//查询通道输入耦合方式
//    new SCPICommandStruct(":CHANnel:INPutres","SCPI_Channel","Inputres"),//设置通道的输入阻抗
//    new SCPICommandStruct(":CHANnel:INPutres?","SCPI_Channel","InputresQ"),//查询通道的输入阻抗
//    new SCPICommandStruct(":CHANnel:EXTent","SCPI_Channel","Extent"),//设置指定通道波形显示的垂直档位
//    new SCPICommandStruct(":CHANnel:PLUS:EXTent","SCPI_Channel","Plus_Extent"),
//    new SCPICommandStruct(":CHANnel:EXTent?","SCPI_Channel","ExtentQ"),//查询指定通道波形显示的垂直档位
//    new SCPICommandStruct(":CHANnel:POSition","SCPI_Channel","Position"),//设置指定通道波形显示的垂直偏移
//    new SCPICommandStruct(":CHANnel:PLUS:POSition","SCPI_Channel","Plus_Position"),//设置指定通道波形显示的垂直偏移
//    new SCPICommandStruct(":CHANnel:POSition?","SCPI_Channel","PositionQ"),//查询指定通道波形显示的垂直偏移
//    new SCPICommandStruct(":CHANnel:VERNier","SCPI_Channel","Vernier"),//打开或关闭指定通道的垂直档位微调功能
//    new SCPICommandStruct(":CHANnel:VERNier?","SCPI_Channel","VernierQ"),//查询指定通道的垂直档位微调功能的打开或关闭
//    new SCPICommandStruct(":CHANnel:CURRent","SCPI_Channel","Current"),//设置垂直展开基准
//    new SCPICommandStruct(":CHANnel:CURRent?","SCPI_Channel","CurrentQ"),//查询垂直展开基准
//    new SCPICommandStruct(":CHANnel:LABel:CLEAr","SCPI_Channel","Clear"),
    //1.1 协议
//    new SCPICommandStruct(":CHANnel#:DISPlay","SCPI_Channel","Display"), //通道打开或关闭
//            new SCPICommandStruct(":CHANnel#:INVerse","SCPI_Channel","Inverse"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:INVert","SCPI_Channel","Inverse"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:PRTY","SCPI_Channel","Prty"), //设置通道的探针类型
//            new SCPICommandStruct(":CHANnel#:PROBe","SCPI_Channel","Probe"),  //设置探头的衰减比
//            new SCPICommandStruct(":CHANnel#:COUPle","SCPI_Channel","Couple"),  //设置通道输入耦合方式
//     1.1 新增       new SCPICommandStruct(":CHANnel#:SCALe","SCPI_Channel","Extent"),  //设置通道波形显示的垂直档位
//            new SCPICommandStruct(":CHANnel#:POSition","SCPI_Channel","Position"),  //设置通道波形显示的垂直偏移
//            new SCPICommandStruct(":CHANnel#:VERNier","SCPI_Channel","Vernier"), //打开或关闭指定通道的垂直档位微调功能
//     1.1 新增       new SCPICommandStruct(":CHANnel#:PC","SCPI_Channel","Pc"),   //获取通道波形到上位机
//            new SCPICommandStruct(":CHANnel#:INPutres","SCPI_Channel","Inputres"),   //设置阻抗
//
//            new SCPICommandStruct(":CHANnel#:DISPlay?","SCPI_Channel","DisplayQ"), //通道打开或关闭
//            new SCPICommandStruct(":CHANnel#:INVerse?","SCPI_Channel","InverseQ"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:INVert?","SCPI_Channel","InverseQ"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:PRTY?","SCPI_Channel","PrtyQ"), //设置通道的探针类型
//            new SCPICommandStruct(":CHANnel#:PROBe?","SCPI_Channel","ProbeQ"),  //设置探头的衰减比
//            new SCPICommandStruct(":CHANnel#:COUPle?","SCPI_Channel","CoupleQ"),  //设置通道输入耦合方式
//      1.1新增      new SCPICommandStruct(":CHANnel#:SCALe?","SCPI_Channel","ExtentQ"),  //设置通道波形显示的垂直档位
//            new SCPICommandStruct(":CHANnel#:POSition?","SCPI_Channel","PositionQ"),  //设置通道波形显示的垂直偏移
//            new SCPICommandStruct(":CHANnel#:VERNier?","SCPI_Channel","VernierQ"), //打开或关闭指定通道的垂直档位微调功能
//       1.1新增     new SCPICommandStruct(":CHANnel#:PC?","SCPI_Channel","PCQ"),   //获取通道波形到上位机
//            new SCPICommandStruct(":CHANnel#:INPutres?","SCPI_Channel","InputresQ"),   //查询阻抗状态
//            new SCPICommandStruct(":CHANnel#:LABel","SCPI_Channel","Label"),//设置标签
//            new SCPICommandStruct(":CHANnel#:LABel?","SCPI_Channel","LabelQ"),//查询标签
//            new SCPICommandStruct(":CHANnel#:LABel:CLEAr","SCPI_Channel","Clear"),//查询垂直展开基准

    public static void Display(SCPIParam param) {
        Command.get().getChannel().Display(param.iParam1, param.bParam1, true);
    }

    public static String DisplayQ(SCPIParam param) {
        boolean b=  Command.get().getChannel().DisplayQ(param.iParam1);
        return ToolsSCPI.getOpenState(b);
    }

    public static void Inverse(SCPIParam param) {
        Command.get().getChannel().Inverse(param.iParam1, param.bParam1, true);
    }

    public static String InverseQ(SCPIParam param) {
        boolean b= Command.get().getChannel().InverseQ(param.iParam1);
        return ToolsSCPI.getOpenState(b);
    }

    public static void Band(SCPIParam param) {
        Command.get().getChannel().Band(param.iParam1, param.iParam2, param.dParam1, true);
    }

    public static String BandQ(SCPIParam param) {
        int i= Command.get().getChannel().BandQ(param.iParam1);
        return ToolsSCPI.getBand(i);
    }
    public static String BandValueQ(SCPIParam param){
        int d=(int)Command.get().getChannel().BandWidthValueQ(param.iParam1);
        return String.valueOf(d);
    }

    public static void Prty(SCPIParam param) {
        Command.get().getChannel().Prty(param.iParam1, param.iParam2, true);
    }

    public static String PrtyQ(SCPIParam param) {
         int i=Command.get().getChannel().PrtyQ(param.iParam1);
         return ToolsSCPI.getPrty(i);
    }

    public static void Probe(SCPIParam param) {
        Command.get().getChannel().Probe(param.iParam1, param.dParam1, true);
    }

    public static String ProbeQ(SCPIParam param) {
        double d= Command.get().getChannel().ProbeQ(param.iParam1);
        return d+"";
    }

    public static void Couple(SCPIParam param) {
        Command.get().getChannel().Couple(param.iParam1, param.iParam2, true);
    }

    public static String CoupleQ(SCPIParam param) {
        int i= Command.get().getChannel().CoupleQ(param.iParam1);
        return ToolsSCPI.getCouple(i);
    }

    public static void Inputres(SCPIParam param) {
        Command.get().getChannel().Inputres(param.iParam1, param.iParam2,true);
    }

    public static String InputresQ(SCPIParam param) {
        int i=Command.get().getChannel().InputresQ(param.iParam1);
        return ToolsSCPI.getInputres(i);
    }

    public static void Extent(SCPIParam param) {
        Command.get().getChannel().Extent(param.iParam1, param.dParam1, true);
    }

    public static void Plus_Extent(SCPIParam param) {
        Command.get().getChannel().Plus_Extent(param.iParam1, param.iParam2, true);
    }

    public static String ExtentQ(SCPIParam param) {
        double d= Command.get().getChannel().ExtentQ(param.iParam1);
        return ToolsSCPI.getDouble(d);
    }

    public static void Position(SCPIParam param) {
        Command.get().getChannel().Position(param.iParam1, param.dParam1, true);
    }

    public static void Plus_Position(SCPIParam param) {
        Command.get().getChannel().Plus_Position(param.iParam1, param.iParam2, true);
    }

    public static String PositionQ(SCPIParam param) {
        double d= Command.get().getChannel().PositionQ(param.iParam1);
        return ToolsSCPI.getDouble(d);
    }

    public static void Delay(SCPIParam param){
        Command.get().getChannel().Delay(param.iParam1, param.dParam1, true);
    }
    public static String DelayQ(SCPIParam param){
        double d= Command.get().getChannel().DelayQ(param.iParam1);
        return ToolsSCPI.getDouble(d);
    }
    public static void Offset(SCPIParam param){
        Command.get().getChannel().Offset(param.iParam1, param.dParam1, true);
    }
    public static String OffsetQ(SCPIParam param){
        double d= Command.get().getChannel().OffsetQ(param.iParam1);
        return ToolsSCPI.getDouble(d);
    }
    public static void Vernier(SCPIParam param) {
        Command.get().getChannel().Vernier(param.iParam1, param.bParam1, true);
    }

    public static String VernierQ(SCPIParam param) {
        boolean b=  Command.get().getChannel().VernierQ(param.iParam1);
        return ToolsSCPI.getOpenState(b);
    }


    //    public static void Scale(SCPIParam param){}
//    public static void ScaleQ(SCPIParam param){}
    public static void Pc(SCPIParam param) {
    }

    public static void PCQ(SCPIParam param) {

    }
    public static void Vref(SCPIParam param){
        Command.get().getChannel().Vref(param.iParam1, param.iParam2, true);
    }
    public static String VrefQ(SCPIParam param){
        int i= Command.get().getChannel().VrefQ(param.iParam1);
        return ToolsSCPI.getMathVRef(i);
    }

    public static void Label(SCPIParam param){
        Command.get().getChannel().Label(param.iParam1, param.sParam1.trim(), true);
    }
    public static String LabelQ(SCPIParam param){
        return Command.get().getChannel().LabelQ(param.iParam1);
    }

    public static void Clear(SCPIParam param){
        Command.get().getChannel().Clear(param.iParam1, true);
    }

    public static String CountQ(SCPIParam param){
        return Command.get().getChannel().CountQ();
    }
    public static void Current(SCPIParam param){
        Command.get().getChannel().Current(param.iParam1, true);
    }
    public static String CurrentQ(SCPIParam param){
        int i = Command.get().getChannel().CurrentQ();
        return ToolsSCPI.getChAll(i);
    }
    public static String Is200MQ(SCPIParam param){
        return ToolsSCPI.getOpenState(true);
    }
    public static String MaxQ(SCPIParam param){
        long scopeBandWidth=(long) Channel.getMaxBandWidth();
        return String.valueOf(scopeBandWidth);
    }

    public static String ProbeInfoQ(SCPIParam param){
        String s= Command.get().getChannel().getProbeInfoQ(param.iParam1);
        return s;
    }
}
