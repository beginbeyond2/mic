package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令包

import com.micsig.tbook.scope.channel.Channel; // 通道信息类，用于获取最大带宽等通道属性
import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件，用于获取底层通道操作接口

/*
 * +=============================================================================+
 * |  模块定位：SCPI通道命令处理层                                                 |
 * |  核心职责：将SCPI协议中:CHANnel子系统的命令解析并转发至底层通道中间件             |
 * |  架构设计：纯静态方法类，作为SCPI命令分发器与Command中间件之间的桥接层           |
 * |  数据流向：SCPI解析器 → SCPI_Channel → Command.get().getChannel() → 底层      |
 * |  依赖关系：依赖SCPIParam(参数封装)、Command(命令中间件)、ToolsSCPI(工具类)、   |
 * |           Channel(通道信息类)                                                  |
 * |  使用场景：示波器通道参数控制，包括通道开关、反相、带宽限制、探针类型、          |
 * |           探头衰减比、耦合方式、输入阻抗、垂直档位/偏移、微调、延迟、            |
 * |           偏移量、标签、垂直参考、通道计数、最大带宽查询等                      |
 * +=============================================================================+
 */

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

    /**
     * 设置通道显示开关
     * @param param SCPI命令参数，iParam1为通道号，bParam1为开启/关闭标志
     */
    public static void Display(SCPIParam param) {
        Command.get().getChannel().Display(param.iParam1, param.bParam1, true); // 调用底层接口设置通道显示开关
    }

    /**
     * 查询通道显示开关状态
     * @param param SCPI命令参数，iParam1为通道号
     * @return 通道显示状态字符串（ON/OFF）
     */
    public static String DisplayQ(SCPIParam param) {
        boolean b=  Command.get().getChannel().DisplayQ(param.iParam1); // 调用底层接口查询通道显示状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }

    /**
     * 设置通道反相显示
     * @param param SCPI命令参数，iParam1为通道号，bParam1为反相开启/关闭标志
     */
    public static void Inverse(SCPIParam param) {
        Command.get().getChannel().Inverse(param.iParam1, param.bParam1, true); // 调用底层接口设置通道反相显示
    }

    /**
     * 查询通道反相显示状态
     * @param param SCPI命令参数，iParam1为通道号
     * @return 通道反相状态字符串（ON/OFF）
     */
    public static String InverseQ(SCPIParam param) {
        boolean b= Command.get().getChannel().InverseQ(param.iParam1); // 调用底层接口查询通道反相状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }

    /**
     * 设置通道带宽限制
     * @param param SCPI命令参数，iParam1为通道号，iParam2为带宽类型，dParam1为带宽值
     */
    public static void Band(SCPIParam param) {
        Command.get().getChannel().Band(param.iParam1, param.iParam2, param.dParam1, true); // 调用底层接口设置通道带宽限制
    }

    /**
     * 查询通道带宽限制类型
     * @param param SCPI命令参数，iParam1为通道号
     * @return 带宽限制类型字符串
     */
    public static String BandQ(SCPIParam param) {
        int i= Command.get().getChannel().BandQ(param.iParam1); // 调用底层接口查询通道带宽限制类型
        return ToolsSCPI.getBand(i); // 将整数类型值转换为带宽限制字符串
    }

    /**
     * 查询通道带宽限制值
     * @param param SCPI命令参数，iParam1为通道号
     * @return 带宽限制值字符串
     */
    public static String BandValueQ(SCPIParam param){
        int d=(int)Command.get().getChannel().BandWidthValueQ(param.iParam1); // 调用底层接口查询通道带宽值，强制转换为整数
        return String.valueOf(d); // 将带宽值转换为字符串返回
    }

    /**
     * 设置通道探针类型
     * @param param SCPI命令参数，iParam1为通道号，iParam2为探针类型
     */
    public static void Prty(SCPIParam param) {
        Command.get().getChannel().Prty(param.iParam1, param.iParam2, true); // 调用底层接口设置通道探针类型
    }

    /**
     * 查询通道探针类型
     * @param param SCPI命令参数，iParam1为通道号
     * @return 探针类型字符串
     */
    public static String PrtyQ(SCPIParam param) {
         int i=Command.get().getChannel().PrtyQ(param.iParam1); // 调用底层接口查询通道探针类型
         return ToolsSCPI.getPrty(i); // 将整数类型值转换为探针类型字符串
    }

    /**
     * 设置探头衰减比
     * @param param SCPI命令参数，iParam1为通道号，dParam1为衰减比值
     */
    public static void Probe(SCPIParam param) {
        Command.get().getChannel().Probe(param.iParam1, param.dParam1, true); // 调用底层接口设置探头衰减比
    }

    /**
     * 查询探头衰减比
     * @param param SCPI命令参数，iParam1为通道号
     * @return 探头衰减比字符串
     */
    public static String ProbeQ(SCPIParam param) {
        double d= Command.get().getChannel().ProbeQ(param.iParam1); // 调用底层接口查询探头衰减比
        return d+""; // 将double值转换为字符串返回
    }

    /**
     * 设置通道输入耦合方式
     * @param param SCPI命令参数，iParam1为通道号，iParam2为耦合方式
     */
    public static void Couple(SCPIParam param) {
        Command.get().getChannel().Couple(param.iParam1, param.iParam2, true); // 调用底层接口设置通道耦合方式
    }

    /**
     * 查询通道输入耦合方式
     * @param param SCPI命令参数，iParam1为通道号
     * @return 耦合方式字符串
     */
    public static String CoupleQ(SCPIParam param) {
        int i= Command.get().getChannel().CoupleQ(param.iParam1); // 调用底层接口查询通道耦合方式
        return ToolsSCPI.getCouple(i); // 将整数类型值转换为耦合方式字符串
    }

    /**
     * 设置通道输入阻抗
     * @param param SCPI命令参数，iParam1为通道号，iParam2为阻抗值
     */
    public static void Inputres(SCPIParam param) {
        Command.get().getChannel().Inputres(param.iParam1, param.iParam2,true); // 调用底层接口设置通道输入阻抗
    }

    /**
     * 查询通道输入阻抗
     * @param param SCPI命令参数，iParam1为通道号
     * @return 输入阻抗字符串
     */
    public static String InputresQ(SCPIParam param) {
        int i=Command.get().getChannel().InputresQ(param.iParam1); // 调用底层接口查询通道输入阻抗
        return ToolsSCPI.getInputres(i); // 将整数类型值转换为输入阻抗字符串
    }

    /**
     * 设置通道垂直档位
     * @param param SCPI命令参数，iParam1为通道号，dParam1为垂直档位值
     */
    public static void Extent(SCPIParam param) {
        Command.get().getChannel().Extent(param.iParam1, param.dParam1, true); // 调用底层接口设置通道垂直档位
    }

    /**
     * 设置通道扩展垂直档位（PLUS模式）
     * @param param SCPI命令参数，iParam1为通道号，iParam2为档位参数
     */
    public static void Plus_Extent(SCPIParam param) {
        Command.get().getChannel().Plus_Extent(param.iParam1, param.iParam2, true); // 调用底层接口设置扩展垂直档位
    }

    /**
     * 查询通道垂直档位
     * @param param SCPI命令参数，iParam1为通道号
     * @return 垂直档位值字符串
     */
    public static String ExtentQ(SCPIParam param) {
        double d= Command.get().getChannel().ExtentQ(param.iParam1); // 调用底层接口查询通道垂直档位
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置通道垂直偏移
     * @param param SCPI命令参数，iParam1为通道号，dParam1为垂直偏移值
     */
    public static void Position(SCPIParam param) {
        Command.get().getChannel().Position(param.iParam1, param.dParam1, true); // 调用底层接口设置通道垂直偏移
    }

    /**
     * 设置通道扩展垂直偏移（PLUS模式）
     * @param param SCPI命令参数，iParam1为通道号，iParam2为偏移参数
     */
    public static void Plus_Position(SCPIParam param) {
        Command.get().getChannel().Plus_Position(param.iParam1, param.iParam2, true); // 调用底层接口设置扩展垂直偏移
    }

    /**
     * 查询通道垂直偏移
     * @param param SCPI命令参数，iParam1为通道号
     * @return 垂直偏移值字符串
     */
    public static String PositionQ(SCPIParam param) {
        double d= Command.get().getChannel().PositionQ(param.iParam1); // 调用底层接口查询通道垂直偏移
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置通道延迟时间
     * @param param SCPI命令参数，iParam1为通道号，dParam1为延迟值
     */
    public static void Delay(SCPIParam param){
        Command.get().getChannel().Delay(param.iParam1, param.dParam1, true); // 调用底层接口设置通道延迟
    }

    /**
     * 查询通道延迟时间
     * @param param SCPI命令参数，iParam1为通道号
     * @return 延迟值字符串
     */
    public static String DelayQ(SCPIParam param){
        double d= Command.get().getChannel().DelayQ(param.iParam1); // 调用底层接口查询通道延迟
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置通道偏移量
     * @param param SCPI命令参数，iParam1为通道号，dParam1为偏移值
     */
    public static void Offset(SCPIParam param){
        Command.get().getChannel().Offset(param.iParam1, param.dParam1, true); // 调用底层接口设置通道偏移量
    }

    /**
     * 查询通道偏移量
     * @param param SCPI命令参数，iParam1为通道号
     * @return 偏移值字符串
     */
    public static String OffsetQ(SCPIParam param){
        double d= Command.get().getChannel().OffsetQ(param.iParam1); // 调用底层接口查询通道偏移量
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串返回
    }

    /**
     * 设置通道垂直档位微调开关
     * @param param SCPI命令参数，iParam1为通道号，bParam1为微调开启/关闭标志
     */
    public static void Vernier(SCPIParam param) {
        Command.get().getChannel().Vernier(param.iParam1, param.bParam1, true); // 调用底层接口设置垂直档位微调开关
    }

    /**
     * 查询通道垂直档位微调开关状态
     * @param param SCPI命令参数，iParam1为通道号
     * @return 微调状态字符串（ON/OFF）
     */
    public static String VernierQ(SCPIParam param) {
        boolean b=  Command.get().getChannel().VernierQ(param.iParam1); // 调用底层接口查询垂直档位微调状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }


    //    public static void Scale(SCPIParam param){}
//    public static void ScaleQ(SCPIParam param){}
    /**
     * 获取通道波形数据到上位机（PC模式）
     * @param param SCPI命令参数（当前为空实现）
     */
    public static void Pc(SCPIParam param) {
        // 当前为空实现，PC波形获取功能待补充
    }

    /**
     * 查询通道波形数据（PC模式查询）
     * @param param SCPI命令参数（当前为空实现）
     */
    public static void PCQ(SCPIParam param) {
        // 当前为空实现，PC波形查询功能待补充
    }

    /**
     * 设置通道垂直参考
     * @param param SCPI命令参数，iParam1为通道号，iParam2为垂直参考值
     */
    public static void Vref(SCPIParam param){
        Command.get().getChannel().Vref(param.iParam1, param.iParam2, true); // 调用底层接口设置通道垂直参考
    }

    /**
     * 查询通道垂直参考
     * @param param SCPI命令参数，iParam1为通道号
     * @return 垂直参考字符串
     */
    public static String VrefQ(SCPIParam param){
        int i= Command.get().getChannel().VrefQ(param.iParam1); // 调用底层接口查询通道垂直参考
        return ToolsSCPI.getMathVRef(i); // 将整数类型值转换为垂直参考字符串
    }

    /**
     * 设置通道标签名称
     * @param param SCPI命令参数，iParam1为通道号，sParam1为标签文本
     */
    public static void Label(SCPIParam param){
        Command.get().getChannel().Label(param.iParam1, param.sParam1.trim(), true); // 调用底层接口设置通道标签，去除首尾空格
    }

    /**
     * 查询通道标签名称
     * @param param SCPI命令参数，iParam1为通道号
     * @return 通道标签字符串
     */
    public static String LabelQ(SCPIParam param){
        return Command.get().getChannel().LabelQ(param.iParam1); // 调用底层接口查询通道标签
    }

    /**
     * 清除通道标签
     * @param param SCPI命令参数，iParam1为通道号
     */
    public static void Clear(SCPIParam param){
        Command.get().getChannel().Clear(param.iParam1, true); // 调用底层接口清除通道标签
    }

    /**
     * 查询通道数量
     * @param param SCPI命令参数（本命令无参数）
     * @return 通道数量字符串
     */
    public static String CountQ(SCPIParam param){
        return Command.get().getChannel().CountQ(); // 调用底层接口查询通道数量
    }

    /**
     * 设置当前激活通道（垂直展开基准）
     * @param param SCPI命令参数，iParam1为通道号
     */
    public static void Current(SCPIParam param){
        Command.get().getChannel().Current(param.iParam1, true); // 调用底层接口设置当前激活通道
    }

    /**
     * 查询当前激活通道
     * @param param SCPI命令参数（本命令无参数）
     * @return 当前通道字符串
     */
    public static String CurrentQ(SCPIParam param){
        int i = Command.get().getChannel().CurrentQ(); // 调用底层接口查询当前激活通道
        return ToolsSCPI.getChAll(i); // 将整数类型值转换为通道标识字符串
    }

    /**
     * 查询是否为200M带宽型号（固定返回ON）
     * @param param SCPI命令参数（本命令无参数）
     * @return 固定返回ON
     */
    public static String Is200MQ(SCPIParam param){
        return ToolsSCPI.getOpenState(true); // 固定返回ON，表示当前为200M带宽型号
    }

    /**
     * 查询示波器最大带宽
     * @param param SCPI命令参数（本命令无参数）
     * @return 最大带宽值字符串（单位Hz）
     */
    public static String MaxQ(SCPIParam param){
        long scopeBandWidth=(long) Channel.getMaxBandWidth(); // 调用Channel类获取最大带宽值，转换为long类型
        return String.valueOf(scopeBandWidth); // 将带宽值转换为字符串返回
    }

    /**
     * 查询通道探头信息
     * @param param SCPI命令参数，iParam1为通道号
     * @return 探头信息字符串
     */
    public static String ProbeInfoQ(SCPIParam param){
        String s= Command.get().getChannel().getProbeInfoQ(param.iParam1); // 调用底层接口查询通道探头信息
        return s; // 返回探头信息字符串
    }
}
