package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI通道命令处理

import android.util.Log; // Android日志工具

import com.micsig.tbook.scope.ScopeBase; // 示波器基础配置
import com.micsig.tbook.scope.channel.Channel; // 通道对象
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，获取通道实例
import com.micsig.tbook.scope.probe.BaseProbe; // 探头基类
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.R; // 资源ID
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.tbookscope.util.App; // 应用上下文
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 工作模式管理
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 光标管理
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 波形管理
import com.micsig.tbook.ui.util.StrUtil; // 字符串工具
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 通道号定义

import java.util.ArrayList; // 动态数组
import java.util.List; // 列表接口
import java.util.stream.Collectors; // 流收集器

/**
 * Created by liwb on 2018/1/17.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                            Command_Channel                                   |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 通道(Channel)命令处理层                          |
 * | 核心职责：解析并执行示波器通道相关的SCPI指令，管理通道属性状态，             |
 * |          将状态变更通过RxBus通知UI层更新                                     |
 * | 架构设计：属于Command子模块，由Command单例统一调度；                         |
 * |          每个通道维护一个ChannelAttribute对象存储属性；                      |
 * |          设置方法采用"值变更检测→状态更新→UI通知"三段式流程                  |
 * | 数据流向：SCPI指令 → Command_Channel → ChannelAttribute(状态存储)           |
 * |                            → CommandMsgToUI → RxBus → UI层                  |
 * | 依赖关系：Command(单例入口)、ChannelFactory(通道工厂)、RxBus(事件总线)、     |
 * |           CursorManage(光标管理)、WaveManage(波形管理)、CacheUtil(缓存)      |
 * | 使用场景：远程SCPI控制、上位机通信、自动化测试中通道参数的设置与查询         |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Channel {
//有plus指令的没有实现完全。 // Plus增量指令尚未完全实现

    //    //通道命令 CHAN 1.0协议
//    new SCPICommandStruct(":CHANnel:DISPlay","SCPI_Channel","Display"),//通道的打开或关闭
//    new SCPICommandStruct(":CHANnel:DISPlay?","SCPI_Channel","DisplayQ"),//查询通道的打开或关闭
//    new SCPICommandStruct(":CHANnel:INVerse","SCPI_Channel","inverse"),//打开或关闭通道的反相显示
//    new SCPICommandStruct(":CHANnel:INVerse?","SCPI_Channel","InverseQ"),//查询通道的反相显示
//    new SCPICommandStruct(":CHANnel:INVert","SCPI_Channel","Invert"),//打开或关闭通道的反相显示
//    new SCPICommandStruct(":CHANnel:INVert?","SCPI_Channel","InvertQ"),//查询通道的反相显示
//    new SCPICommandStruct(":CHANnel:BAND","SCPI_Channel","Band"),//设置通道的带宽限制
//    new SCPICommandStruct(":CHANnel:BAND?","SCPI_Channel","BandQ"),//查询通道的带宽限制
//    new SCPICommandStruct(":CHANnel:PRTY","SCPI_Channel","prty"),//设置通道的探针类型
//    new SCPICommandStruct(":CHANnel:PRTY?","SCPI_Channel","PrtyQ"),//查询通道的探针类型
//    new SCPICommandStruct(":CHANnel:PROBe","SCPI_Channel","Probe"),//设置探头的衰减比
//    new SCPICommandStruct(":CHANnel:PROBe?","SCPI_Channel","ProbeQ"),//查询探头的衰减比
//    new SCPICommandStruct(":CHANnel:COUPle","SCPI_Channel","Couple"),//设置通道输入耦合方式
//    new SCPICommandStruct(":CHANnel:COUPle?","SCPI_Channel","CoupleQ"),//查询通道输入耦合方式
//    new SCPICommandStruct(":CHANnel:INPutres","SCPI_Channel","Inputres"),//设置通道的输入阻抗
//    new SCPICommandStruct(":CHANnel:INPutres?","SCPI_Channel","InputresQ"),//查询通道的输入阻抗
//    new SCPICommandStruct(":CHANnel:EXTent","SCPI_Channel","extent"),//设置指定通道波形显示的垂直档位
//    new SCPICommandStruct(":CHANnel:PLUS:EXTent","SCPI_Channel","Plus_Extent"),
//    new SCPICommandStruct(":CHANnel:EXTent?","SCPI_Channel","ExtentQ"),//查询指定通道波形显示的垂直档位
//    new SCPICommandStruct(":CHANnel:POSition","SCPI_Channel","Position"),//设置指定通道波形显示的垂直偏移
//    new SCPICommandStruct(":CHANnel:PLUS:POSition","SCPI_Channel","Plus_Position"),//设置指定通道波形显示的垂直偏移
//    new SCPICommandStruct(":CHANnel:POSition?","SCPI_Channel","PositionQ"),//查询指定通道波形显示的垂直偏移
//    new SCPICommandStruct(":CHANnel:VERNier","SCPI_Channel","Vernier"),//打开或关闭指定通道的垂直档位微调功能
//    new SCPICommandStruct(":CHANnel:VERNier?","SCPI_Channel","VernierQ"),//查询指定通道的垂直档位微调功能的打开或关闭
//    new SCPICommandStruct(":CHANnel:VREF","SCPI_Channel","Vref"),//设置垂直展开基准
//    new SCPICommandStruct(":CHANnel:VREF?","SCPI_Channel","VrefQ"),//查询垂直展开基准
//    new SCPICommandStruct(":CHANnel:CURRent","SCPI_Channel","Current"),//设置垂直展开基准
//    new SCPICommandStruct(":CHANnel:CURRent?","SCPI_Channel","CurrentQ"),//查询垂直展开基准

    //1.1 协议
//    new SCPICommandStruct(":CHANnel#:DISPlay","SCPI_Channel","Display"), //通道打开或关闭
//            new SCPICommandStruct(":CHANnel#:INVerse","SCPI_Channel","inverse"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:INVert","SCPI_Channel","Invert"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:PRTY","SCPI_Channel","prty"), //设置通道的探针类型
//            new SCPICommandStruct(":CHANnel#:PROBe","SCPI_Channel","Probe"),  //设置探头的衰减比
//            new SCPICommandStruct(":CHANnel#:COUPle","SCPI_Channel","Couple"),  //设置通道输入耦合方式
//     1.1 新增       new SCPICommandStruct(":CHANnel#:SCALe","SCPI_Channel","Scale"),  //设置通道波形显示的垂直档位
//            new SCPICommandStruct(":CHANnel#:POSition","SCPI_Channel","Position"),  //设置通道波形显示的垂直偏移
//            new SCPICommandStruct(":CHANnel#:VERNier","SCPI_Channel","Vernier"), //打开或关闭指定通道的垂直档位微调功能
//     1.1 新增       new SCPICommandStruct(":CHANnel#:PC","SCPI_Channel","pc"),   //获取通道波形到上位机
//            new SCPICommandStruct(":CHANnel#:INPutres","SCPI_Channel","Inputres"),   //设置阻抗
//
//            new SCPICommandStruct(":CHANnel#:DISPlay?","SCPI_Channel","DisplayQ"), //通道打开或关闭
//            new SCPICommandStruct(":CHANnel#:INVerse?","SCPI_Channel","InverseQ"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:INVert?","SCPI_Channel","InvertQ"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:PRTY?","SCPI_Channel","PrtyQ"), //设置通道的探针类型
//            new SCPICommandStruct(":CHANnel#:PROBe?","SCPI_Channel","ProbeQ"),  //设置探头的衰减比
//            new SCPICommandStruct(":CHANnel#:COUPle?","SCPI_Channel","CoupleQ"),  //设置通道输入耦合方式
//      1.1新增      new SCPICommandStruct(":CHANnel#:SCALe?","SCPI_Channel","ScaleQ"),  //设置通道波形显示的垂直档位
//            new SCPICommandStruct(":CHANnel#:POSition?","SCPI_Channel","PositionQ"),  //设置通道波形显示的垂直偏移
//            new SCPICommandStruct(":CHANnel#:VERNier?","SCPI_Channel","VernierQ"), //打开或关闭指定通道的垂直档位微调功能
//       1.1新增     new SCPICommandStruct(":CHANnel#:PC?","SCPI_Channel","PCQ"),   //获取通道波形到上位机
//            new SCPICommandStruct(":CHANnel#:INPutres?","SCPI_Channel","InputresQ"),   //查询阻抗状态


   /**
    * 通道属性内部类，存储单个通道的所有SCPI可配置属性
    */
   public class ChannelAttribute {
        /**
         * 显示
         */
        boolean display = false; // 通道显示开关，默认关闭
        /**
         * 反向
         */
        boolean inverse = false; // 通道反相开关，默认关闭
        /**
         * 通道的探针类型
         */
        int prty = 0; // 探针类型索引，默认0
        /**
         * 衰减比
         */
        double prob = 0; // 探头衰减比，默认0
        /**
         * 耦合方式
         */
        int coup = 0; // 耦合方式索引（0=DC,1=AC等），默认0
        /**
         * 设置通道波形显示的垂直档位 又名：EXT
         */
        double extent = 0; // 垂直档位值（V/div），默认0
        /**
         * 通道波形显示的垂直偏移
         */
        double pos = 0; // 垂直偏移值（像素），默认0
        /**
         * 打开或关闭指定通道的垂直档位微调功能
         */
        boolean Vern = false; // 微调开关，默认关闭
        /**
         * 获得通道波形到上位机
         */
        int pc = 0; // 波形上传上位机标志，默认0
        /**
         * 阻抗
         */
        int Inp = 0; // 输入阻抗索引，默认0
        /** 带宽 */
        /**
         * const char * chan_band[] = {
         * "FULL"
         * , "200M"
         * , "20M"
         * , "HIGH"
         * , "LOW"
         * , NULL
         * };
         */
        int band_param2; // 带宽限制类型索引（FULL/200M/20M/HIGH/LOW）
        /**
         * 高通低通 具体数据
         */
        double band_param3; // 高通/低通带宽具体数值
       /**
        * 垂直展开基准
        */
       int Vref=0; // 垂直展开基准索引（center/zero），默认0
       /**
        * 通道标签
        */
       String label; // 通道标签字符串
       double delay; // 通道延时（秒）
       double offset; // 通道偏移
       boolean vernier; // 垂直档位微调开关

       /**
        * 将通道属性格式化为字符串，用于调试输出
        * @return 属性拼接字符串
        */
       @Override
       public String toString() {
           final StringBuilder sb = new StringBuilder("ChannelAttribute{"); // 创建字符串构建器
           sb.append("display=").append(display); // 追加显示状态
           sb.append(", inverse=").append(inverse); // 追加反相状态
           sb.append(", prty=").append(prty); // 追加探针类型
           sb.append(", prob=").append(prob); // 追加衰减比
           sb.append(", coup=").append(coup); // 追加耦合方式
           sb.append(", extent=").append(extent); // 追加垂直档位
           sb.append(", pos=").append(pos); // 追加垂直偏移
           sb.append(", Vern=").append(Vern); // 追加微调状态
           sb.append(", pc=").append(pc); // 追加上位机标志
           sb.append(", Inp=").append(Inp); // 追加阻抗
           sb.append(", band_param2=").append(band_param2); // 追加带宽类型
           sb.append(", band_param3=").append(band_param3); // 追加带宽数值
           sb.append(", Vref=").append(Vref); // 追加垂直展开基准
           sb.append(", label='").append(label).append('\''); // 追加标签
           sb.append('}'); // 结束花括号
           return sb.toString(); // 返回拼接结果
       }
   }

    private List<ChannelAttribute> chDisplayList = new ArrayList<>(); // 通道属性列表，索引对应通道号
    private int currActiveChannel = TChan.Ch1; // 当前活动通道号，默认CH1
    private int currActiveObject = TChan.Ch1; // 当前活动对象号，默认CH1

    /**
     * 构造函数，初始化所有通道的属性对象
     */
    public Command_Channel() {
        TChan.foreachAllChan((chNo)->{ // 遍历所有通道号
            ChannelAttribute chAttr = new ChannelAttribute(); // 为每个通道创建属性对象
            chDisplayList.add(chAttr); // 添加到通道属性列表
        });
//        for (int i = 0; i <= IWave.S2; i++) {
//            ChannelAttribute chAttr = new ChannelAttribute();
//            chDisplayList.add(chAttr);
//        }


    }

    /**
     * 设置通道显示状态
     * 对应SCPI指令: :CHANnel#:DISPlay
     * @param chIndex 通道索引（从0开始）
     * @param isOpen 是否打开通道显示
     * @param isUpdateUI 是否通知UI更新
     */
    public void Display(int chIndex, boolean isOpen, boolean isUpdateUI) {
        if (!ChannelFactory.isDynamicCh(chIndex)) return; // 非动态通道则直接返回
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取指定通道的属性对象
//        Logger.i(Command.TAG,"chIndex:"+chIndex+",isOpen:"+isOpen+",scpi disPlay:"+chAttr.display);
        if (chAttr.display == isOpen) return; // 状态未变则直接返回
        chAttr.display = isOpen; // 更新显示状态
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_DISPLAY); // 设置消息标志为通道显示
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(isOpen); // 拼接参数：通道索引+分隔符+开关状态
            msgToUI.setParam(param); // 设置消息参数
            msgToUI.setObject(chDisplayList); // 设置消息附带对象为通道列表
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新事件
        }
    }

    /**
     * 查询通道显示状态
     * 对应SCPI指令: :CHANnel#:DISPlay?
     * @param chIndex 通道索引
     * @return 通道是否打开显示
     */
    public boolean DisplayQ(int chIndex) {
        return chDisplayList.get(chIndex).display; // 返回指定通道的显示状态
    }

    /**
     * 返回采样通道个数
     */
//    public int getSampleCount() {
//        int n = 0;
//        for (int i = IWave.Ch1; i <= IWave.Ch4; i++) {
//            if (chDisplayList.get(i).display) n++;
//        }
//        return n;
//    }

    /**
     * 设置通道反向显示
     * 对应SCPI指令: :CHANnel#:INVert
     * @param chIndex 通道索引
     * @param isOpen 是否开启反相
     * @param isUpdateUI 是否通知UI更新
     */
    public void Inverse(int chIndex, boolean isOpen, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        if (chAttr.inverse == isOpen) return; // 状态未变则直接返回
        chAttr.inverse = isOpen; // 更新反相状态
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_INVERSE); // 设置消息标志为通道反相
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(isOpen); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询通道反相状态
     * 对应SCPI指令: :CHANnel#:INVert?
     * @param chIndex 通道索引
     * @return 是否开启反相
     */
    public boolean InverseQ(int chIndex) {
        return chDisplayList.get(chIndex).inverse; // 返回指定通道的反相状态
    }

    /**
     * 设置通道带宽限制
     * 对应SCPI指令: :CHANnel#:BAND
     * @param chIndex 通道索引
     * @param bandIndex 带宽类型索引（FULL/200M/20M/HIGH/LOW）
     * @param bandDetail 高通/低通的具体带宽数值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Band(int chIndex, int bandIndex, double bandDetail, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
//        if (chAttr.band_param2 == bandIndex && chAttr.band_param3 == bandDetail) return;
        int min=30; // 高通/低通最小带宽值
        long max=(long) (Channel.getMaxBandWidth()); // 获取该通道支持的最大带宽
//        Logger.i(Command.TAG,"max:"+max+",channel:"+chIndex+",band:"+bandIndex);
        if ( (bandIndex==3 || bandIndex==4) ){ // 3=HIGH高通, 4=LOW低通，需要范围限制
            if (bandDetail<min) bandDetail=min; // 低于最小值则钳位到最小值
            if (bandDetail>max) bandDetail=max; // 超过最大值则钳位到最大值
        }

        chAttr.band_param2 = bandIndex; // 更新带宽类型索引
        chAttr.band_param3 = bandDetail; // 更新带宽具体数值
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_BANDWIDTH); // 设置消息标志为通道带宽
            String param = String.valueOf(chIndex) // 拼接参数：通道索引
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(bandIndex) // +分隔符+带宽类型
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(bandDetail); // +分隔符+带宽数值
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询通道带宽限制类型
     * 对应SCPI指令: :CHANnel#:BAND?
     * 返回的与通道无关，默认返回ch1的值
     * @param chIndex 通道索引
     * @return 带宽类型索引
     */
    public int BandQ(int chIndex) {
        return chDisplayList.get(chIndex).band_param2; // 返回指定通道的带宽类型索引
    }

    /**
     * 查询通道实际带宽值
     * @param chIdx 通道索引
     * @return 带宽值(Hz)
     */
    public double BandWidthValueQ(int chIdx){
        return ChannelFactory.getDynamicChannel(chIdx).getBandWidth(); // 从通道工厂获取实际带宽
    }

    /**
     * 设置通道探针类型
     * 对应SCPI指令: :CHANnel#:PRTY
     * @param chIndex 通道索引
     * @param prty 探针类型索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Prty(int chIndex, int prty, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        if (chAttr.prty == prty) return; // 类型未变则直接返回
        chAttr.prty = prty; // 更新探针类型
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_PROBETYPE); // 设置消息标志为探针类型
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(prty); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询通道探针类型
     * 对应SCPI指令: :CHANnel#:PRTY?
     * @param chIndex 通道索引
     * @return 探针类型索引
     */
    public int PrtyQ(int chIndex) {
        return chDisplayList.get(chIndex).prty; // 返回指定通道的探针类型
    }


    /**
     * 设置探头衰减比
     * 对应SCPI指令: :CHANnel#:PROBe
     * @param chIndex 通道索引
     * @param probe 衰减比值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Probe(int chIndex, double probe, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        if (chAttr.prob == probe) return; // 衰减比未变则直接返回
        chAttr.prob = probe; // 更新衰减比
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_PROBEMULTIPLE); // 设置消息标志为探头倍率
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(probe); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询探头衰减比
     * 对应SCPI指令: :CHANnel#:PROBe?
     * @param chIndex 通道索引
     * @return 衰减比值
     */
    public double ProbeQ(int chIndex) {
        return chDisplayList.get(chIndex).prob; // 返回指定通道的衰减比
    }

    /**
     * 设置通道输入耦合方式
     * 对应SCPI指令: :CHANnel#:COUPle
     * @param chIndex 通道索引
     * @param couple 耦合方式索引（0=DC,1=AC等）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Couple(int chIndex, int couple, boolean isUpdateUI) {
        int impedance = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_IMPED + TChan.toUiChNo(chIndex)); // 获取该通道当前阻抗设置
        if(impedance == 1 && couple == 1) return; // 阻抗为50Ω时不能设为AC耦合，直接返回
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        if (chAttr.coup == couple) return; // 耦合方式未变则直接返回
        chAttr.coup = couple; // 更新耦合方式
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_COUPLE); // 设置消息标志为耦合方式
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(couple); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询通道输入耦合方式
     * 对应SCPI指令: :CHANnel#:COUPle?
     * @param chIndex 通道索引
     * @return 耦合方式索引
     */
    public int CoupleQ(int chIndex) {
        return chDisplayList.get(chIndex).coup; // 返回指定通道的耦合方式
    }

    /**
     * 设置输入阻抗
     * 对应SCPI指令: :CHANnel#:INPutres
     * @param chIndex 通道索引
     * @param inp 阻抗索引（0=1MΩ,1=50Ω等）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Inputres(int chIndex,int inp, boolean isUpdateUI) {
        int chCouple = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_COUPLE + TChan.toUiChNo(chIndex)); // 获取该通道当前耦合方式
        if (chCouple == 1 && inp == 1) return; // AC耦合时不能设为50Ω阻抗，直接返回
//        Logger.i(Command.TAG,"ch:"+chIndex+",inp:"+inp);
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        if (chAttr.Inp == inp) return; // 阻抗未变则直接返回
        chAttr.Inp = inp; // 更新阻抗值
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_INPUTRES); // 设置消息标志为输入阻抗
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(inp); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询输入阻抗 目前不支持
     * 对应SCPI指令: :CHANnel#:INPutres?
     * @param chIndex 通道索引
     * @return 阻抗索引
     */
    public int InputresQ(int chIndex) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        return chAttr.Inp; // 返回阻抗索引
    }

    private double[] arrayExtent; // 垂直档位可选值数组，延迟初始化

    /**
     * 设置波形的垂直档位
     * 对应SCPI指令: :CHANnel#:SCALe / :CHANnel#:EXTent
     * @param chIndex 通道索引
     * @param extent 垂直档位值（V/div）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Extent(int chIndex, double extent, boolean isUpdateUI) {

        if (chIndex < 0 || chIndex > 7) return; // 通道索引越界则直接返回
        if (arrayExtent == null) { // 首次调用时初始化可选档位数组
            String[] arrayStr = App.get().getResources().getStringArray(R.array.channelProbeEveryDouble); // 从资源获取档位字符串数组
            arrayExtent = new double[arrayStr.length]; // 创建对应长度的double数组
            for (int i = 0; i < arrayStr.length; i++) { // 遍历字符串数组
                arrayExtent[i] = Double.parseDouble(arrayStr[i]); // 将字符串转换为double值
            }
        }
        String s=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + (TChan.toUiChNo(chIndex))); // 获取该通道的探头倍率缓存值
        double multiple = TBookUtil.getDoubleFromX(s); // 将字符串转换为double倍率
//        extent = DoubleUtil.divide(extent, multiple,5);


//        boolean isContinue = false;
//        for (double anArrayExtent : arrayExtent) {
//            if (DoubleUtil.compareTo(anArrayExtent, extent) == 0) {
//                isContinue = true;
//                break;
//            }
//        }
//        if (!isContinue) {
//            if (DoubleUtil.compareTo(extent, arrayExtent[0]) < 0) {
//                extent = arrayExtent[0];
//                isContinue = true;
//            } else if (DoubleUtil.compareTo(extent, arrayExtent[arrayExtent.length - 1]) > 0) {
//                extent = arrayExtent[arrayExtent.length - 1];
//                isContinue = true;
//            }
//        }
//        if (!isContinue) return;
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
//        if (chAttr.extent == extent) return;
        chAttr.extent = extent; // 更新垂直档位
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_EXTENT); // 设置消息标志为垂直档位
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            CursorManage.getInstance().setScpiChanIdx(chIndex); // 设置光标管理器的SCPI通道索引
            CursorManage.getInstance().setCursorTrace(true); // 启用光标跟踪模式
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
            CursorManage.setCursorByScaleTrace(); // 根据档位跟踪调整光标位置
            CursorManage.getInstance().setCursorTrace(false); // 关闭光标跟踪模式
        }
    }

    /**
     * 垂直档位递增/递减（Plus指令）
     * 对应SCPI指令: :CHANnel#:PLUS:EXTent
     * @param chIndex 通道索引
     * @param extentIndex 递增(1)或递减(-1)
     * @param isUpdateUI 是否通知UI更新
     */
    public void Plus_Extent(int chIndex, int extentIndex, boolean isUpdateUI) {
        if (extentIndex != 1 && extentIndex != -1) return; // 仅支持+1或-1，其他值直接返回
        if (arrayExtent == null) { // 延迟初始化可选档位数组
            String[] arrayStr = App.get().getResources().getStringArray(R.array.channelProbeEveryDouble); // 从资源获取档位字符串数组
            arrayExtent = new double[arrayStr.length]; // 创建对应长度的double数组
            for (int i = 0; i < arrayStr.length; i++) { // 遍历字符串数组
                arrayExtent[i] = Integer.parseInt(arrayStr[i]); // 将字符串转换为整数（注意：此处用Integer可能丢失小数精度）
            }
        }
        int index = 0; // 当前档位在数组中的索引
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        for (int i = 0; i < arrayExtent.length; i++) { // 遍历查找当前档位对应的索引
            if (Double.doubleToLongBits(arrayExtent[i]) == Double.doubleToLongBits(chAttr.extent)) { // 使用位比较确保精确匹配
                index = i; // 记录匹配的索引
                break; // 找到即退出循环
            }
        }
        if (index == 0 && extentIndex == -1) { // 已是最小档位且要递减
            index = arrayExtent.length - 1; // 循环到最大档位
        } else if (index == arrayExtent.length - 1 && extentIndex == 1) { // 已是最大档位且要递增
            index = 0; // 循环到最小档位
        } else {
            index = index + extentIndex; // 正常递增或递减
        }
        Extent(chIndex, arrayExtent[index], isUpdateUI); // 调用Extent设置新的档位
    }

    /**
     * 查询通道垂直档位
     * 对应SCPI指令: :CHANnel#:SCALe? / :CHANnel#:EXTent?
     * @param chIndex 通道索引
     * @return 垂直档位值（V/div）
     */
    public double ExtentQ(int chIndex) {
//        return chDisplayList.get(chIndex).extent * ProbeQ(chIndex);

        double chVal = ChannelFactory.getDynamicChannel(chIndex).getVScaleVal(); // 从通道工厂获取实际垂直档位值
        return chVal; // 返回垂直档位
    }

    /**
     * 直接设置通道的垂直位置（像素值）
     * @param chIndex 通道索引
     * @param posPix 垂直位置像素值
     */
    public void setPosition(int chIndex,double posPix){
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        chAttr.pos = posPix; // 设置垂直偏移像素值
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
    }

    /**
     * 设置波形的垂直位置
     * 对应SCPI指令: :CHANnel#:POSition
     * @param chIndex 通道 chindex 0开始
     * @param posV 垂直偏移（物理量，如V）
     * @param isUpdateUI 是否更新界面
     */
    public void Position(int chIndex, double posV, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        //档位转像素
         Channel ch= ChannelFactory.getDynamicChannel(chIndex); // 获取通道对象
        double d= 0;//每个像素对应的幅度值
        if (ch != null) { // 通道对象有效
            d = ch.getVerticalPerPix(); // 获取每像素对应的电压值
        }
//        Logger.i("Command"," d:"+d+"  vsid:"+ch.getVScaleId());
        double offsetPix = (Tools.isZoom() ? ScopeBase.getNewZoomHeight() : ScopeBase.getNewHeight()) / 2.0 - (posV / d); // 物理偏移转像素偏移：屏幕中心 - 物理值/像素单位值
        if (chAttr.pos == offsetPix) return; // 像素偏移未变则直接返回
        chAttr.pos = offsetPix; // 更新垂直偏移（像素）
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CursorManage.getInstance().setScpiChanIdx(chIndex); // 设置光标管理器的SCPI通道索引
            CursorManage.getInstance().setCursorTrace(true); // 启用光标跟踪模式
            WaveManage.get().setPositionY(chIndex + 1,offsetPix); // 设置波形管理器的垂直位置（通道号从1开始）
            CursorManage.setCursorByScaleTrace(); // 根据档位跟踪调整光标位置
            CursorManage.getInstance().setCursorTrace(false); // 关闭光标跟踪模式
        }
    }

    /**
     * 垂直位置递增/递减（Plus指令）
     * 对应SCPI指令: :CHANnel#:PLUS:POSition
     * @param chIndex 通道索引
     * @param posIndex 递增(1)或递减(-1)
     * @param isUpdateUI 是否通知UI更新
     */
    public void Plus_Position(int chIndex, int posIndex, boolean isUpdateUI) {
        if (posIndex == 1 || posIndex == -1) { // 仅支持+1或-1
            Channel ch= ChannelFactory.getDynamicChannel(chIndex); // 获取通道对象
            double d= 0; // 每像素对应的电压值
            if (ch != null) { // 通道对象有效
                d = ch.getVerticalPerPix(); // 获取每像素对应的电压值
            }
            Position(chIndex, this.PositionQ(chIndex)+posIndex*d,isUpdateUI); // 当前位置 ± 1个像素对应的电压值
        }
    }

    /**
     * 查询通道垂直位置（物理量）
     * 对应SCPI指令: :CHANnel#:POSition?
     * @param chIndex 通道索引
     * @return 垂直偏移物理量（V）
     */
    public double PositionQ(int chIndex) {
        Channel ch = ChannelFactory.getDynamicChannel(chIndex); // 获取通道对象
        if (ch != null) { // 通道对象有效
            double d = ch.getVerticalPerPix(); // 获取每像素对应的电压值
            return (GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) / 2 - WaveManage.get().getPositionY(chIndex + 1)) * d; // 像素偏移转物理量：(屏幕中心 - 波形位置) * 每像素电压
        }
        return 0; // 通道无效返回0
    }

    /**
     * 打开或关闭指定通道的垂直档位微调功能 目前不支持
     * 对应SCPI指令: :CHANnel#:VERNier（单参数版本，暂未实现）
     * @param index 通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Vernier(int index, boolean isUpdateUI) {
    }

    /**
     * 打开或关闭指定通道的垂直档位微调功能 目前不支持
     * 对应SCPI指令: :CHANnel#:VERNier?（无参版本，暂未实现）
     * @return 固定返回false
     */
    public boolean VernierQ() {
        return false; // 暂不支持，固定返回false
    }


    /**
     * 设置通道波形上传上位机标志
     * 对应SCPI指令: :CHANnel#:PC
     * @param chIndex 通道索引
     * @param pc 上位机标志值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Pc(int chIndex, int pc, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        if (chAttr.pc == pc) return; // 值未变则直接返回
        chAttr.pc = pc; // 更新上位机标志
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
        }
    }

    /**
     * 查询通道波形上传上位机标志
     * 对应SCPI指令: :CHANnel#:PC?
     * @param chIndex 通道索引
     * @return 上位机标志值
     */
    public int PCQ(int chIndex) {
        return chDisplayList.get(chIndex).pc; // 返回指定通道的上位机标志
    }


    /**
     * 设置通道标签
     * 对应SCPI指令: :CHANnel#:LABel
     * @param chIndex 通道索引
     * @param label 标签字符串
     * @param isUpdateUI 是否通知UI更新
     */
    public void Label(int chIndex,String label,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        chAttr.label = label; // 设置标签
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_LABEL); // 设置消息标志为通道标签
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(label); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            msgToUI.setObject(chDisplayList); // 附带整个通道属性列表
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询通道标签
     * 对应SCPI指令: :CHANnel#:LABel?
     * @param chIndex 通道索引
     * @return 标签字符串，空时返回"NONE"
     */
    public String LabelQ(int chIndex){
        if (StrUtil.isEmpty(chDisplayList.get(chIndex).label)){ // 标签为空
            return "NONE"; // 返回"NONE"
        }
        return chDisplayList.get(chIndex).label; // 返回标签字符串
    }

    /**
     * 清除通道标签
     * 对应SCPI指令: :CHANnel#:LABel:CLEar
     * @param chIndex 通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Clear(int chIndex,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        chAttr.label = ""; // 清空标签
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_LABEL_CLEAR); // 设置消息标志为清除标签
            String param = String.valueOf(chIndex) ; // 拼接参数（仅通道索引）
            msgToUI.setParam(param); // 设置消息参数
            msgToUI.setObject(chDisplayList); // 附带整个通道属性列表
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询通道总数
     * 对应SCPI指令: :CHANnel:COUNt?
     * @return 通道数量字符串
     */
    public String CountQ(){
        return String.valueOf(GlobalVar.get().getChannelsCount()); // 返回全局通道数量
    }

    /**
     * 设置当前活动通道
     * 包括：CH1，ch2,ch3,ch4,math ,ref1,ref2,ref3,ref4
     * @param chIndex 通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void setCurrActiveChannel(int chIndex, boolean isUpdateUI) {
        if (currActiveChannel == chIndex) return; // 活动通道未变则直接返回
        this.currActiveChannel = chIndex; // 更新当前活动通道
        if (isUpdateUI) { // 需要通知UI更新
        }
    }

    /**
     * 获取当前活动通道
     * @return 活动通道索引
     */
    public int getCurrActiveChannel() {
        return this.currActiveChannel; // 返回当前活动通道索引
    }

    /**
     * 设置当前活动对像
     * @param index      ch1,ch2,ch3,ch4,math,ref1,ref2,ref3,ref4,s1,s2,光标1，光标2，光标3，光标4，触发时刻
     * @param isUpdateUI 是否通知UI更新
     */
    public void setCurrActiveObject(int index, boolean isUpdateUI) {
        if (currActiveObject == index) return; // 活动对象未变则直接返回
        this.currActiveObject = index; // 更新当前活动对象
        if (isUpdateUI) { // 需要通知UI更新
        }
    }

    /**
     * 获取当前活动对象
     * @return 活动对象索引
     */
    public int getCurrActiveObject() {
        return this.currActiveObject; // 返回当前活动对象索引
    }


    /**
     * 设置垂直展开基准
     * 对应SCPI指令: :CHANnel#:VREF
     * @param chIndex 通道索引
     * @param vref 垂直展开基准索引（center/zero）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Vref(int chIndex,int vref,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        chAttr.Vref = vref; // 设置垂直展开基准
        chDisplayList.set(chIndex, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_VREF); // 设置消息标志为垂直展开基准
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(vref); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询垂直展开基准
     * 对应SCPI指令: :CHANnel#:VREF?
     * @param chIndex 通道索引
     * @return 垂直展开基准索引
     */
    public int VrefQ(int chIndex){
        ChannelAttribute chAttr = chDisplayList.get(chIndex); // 获取通道属性
        return chAttr.Vref; // 返回垂直展开基准

    }

    private int currentActivty; // 当前活动通道索引（Current指令使用）

    /**
     * 设置当前活动通道（Current指令）
     * 对应SCPI指令: :CHANnel#:CURRent
     * @param chIndex 通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Current(int chIndex,boolean isUpdateUI){
        this.currentActivty=chIndex; // 记录当前活动通道
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_CURRENT); // 设置消息标志为当前通道
            String param = String.valueOf(chIndex); // 拼接参数（仅通道索引）
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询当前活动通道
     * 对应SCPI指令: :CHANnel#:CURRent?
     * @return 通道索引
     */
    public int CurrentQ(){
       return TChan.toFpgaChNo( WaveManage.get().getCurCh()); // 从波形管理器获取当前通道并转为FPGA通道号
//        return currentActivty;
    }


    /**
     * 设置通道延时
     * 对应SCPI指令: :CHANnel#:DELay
     * @param chIdx 通道索引
     * @param d 延时值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Delay(int chIdx,double d,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIdx); // 获取通道属性
        chAttr.delay = d; // 设置延时值
        chDisplayList.set(chIdx, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_DELAY); // 设置消息标志为通道延时
            String param = String.valueOf(chIdx) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(d); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询通道延时
     * 对应SCPI指令: :CHANnel#:DELay?
     * @param chIdx 通道索引
     * @return 延时值（秒）
     */
    public double DelayQ(int chIdx){
        //以ps为单位
        int ps= ChannelFactory.getDynamicChannel(chIdx).getDelay(); // 获取通道延时（皮秒）
        return ps*1e-12; // 皮秒转秒
    }

    /**
     * 设置通道偏移
     * 对应SCPI指令: :CHANnel#:OFFSet
     * @param chIdx 通道索引
     * @param d 偏移值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Offset(int chIdx,double d,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIdx); // 获取通道属性
        chAttr.offset = d; // 设置偏移值
        chDisplayList.set(chIdx, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_OFFSET); // 设置消息标志为通道偏移
            String param = String.valueOf(chIdx) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(d); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询通道偏移
     * 对应SCPI指令: :CHANnel#:OFFSet?
     * @param chIdx 通道索引
     * @return 偏移值
     */
    public double OffsetQ(int chIdx){
       return   ChannelFactory.getDynamicChannel(chIdx).getChOffsetVal(); // 从通道工厂获取实际偏移值
    }

    /**
     * 打开或关闭指定通道的垂直档位微调功能
     * 对应SCPI指令: :CHANnel#:VERNier（三参数版本）
     * @param chIdx 通道索引
     * @param isOpen 是否开启微调
     * @param isUpdateUI 是否通知UI更新
     */
    public void Vernier(int chIdx,boolean isOpen, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIdx); // 获取通道属性
        chAttr.vernier = isOpen; // 设置微调开关
        chDisplayList.set(chIdx, chAttr); // 回写属性到列表
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_VERNIER); // 设置消息标志为垂直微调
            String param = String.valueOf(chIdx) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(isOpen); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询通道垂直档位微调状态
     * 对应SCPI指令: :CHANnel#:VERNier?（带参数版本）
     * @param chIdx 通道索引
     * @return 是否开启微调
     */
    public boolean VernierQ(int chIdx) {
//        return ChannelFactory.getDynamicChannel(chIdx).isFineExtent();
        ChannelAttribute chAttr = chDisplayList.get(chIdx); // 获取通道属性
        return chAttr.vernier; // 返回微调开关状态
    }


    /**
     * 查询探头信息
     * 对应SCPI指令: :CHANnel#:PROBe:INFo?
     * @param chIdx 通道索引
     * @return 探头信息字符串，格式为"名称:SN:版本:倍率列表:带宽:是否支持50Ω"
     */
    public String getProbeInfoQ(int chIdx){
        Channel chan= ChannelFactory.getDynamicChannel(chIdx); // 获取动态通道对象
        BaseProbe probe= chan.getProbe(); // 获取探头对象
        if (probe==null){ // 无探头连接
            return "None"; // 返回"None"
        }else {
//            String b= JSON.toJSONString(probe);
//            return b;
            StringBuilder sb=new StringBuilder(); // 创建字符串构建器
            sb.append(probe.getProbeName()).append(":"); // 追加探头名称
            sb.append(probe.getSN()).append(":"); // 追加序列号
            sb.append(probe.getVersion()).append(":"); // 追加版本号
            sb.append(probe.getProbeX().stream().collect(Collectors.joining(","))).append(":"); // 追加倍率列表（逗号分隔）
            sb.append(probe.getBandWidth()).append(":"); // 追加带宽
            sb.append(probe.isSupportImped50()).append(":"); // 追加是否支持50Ω阻抗
            return sb.toString(); // 返回拼接的探头信息字符串

        }

    }
}
