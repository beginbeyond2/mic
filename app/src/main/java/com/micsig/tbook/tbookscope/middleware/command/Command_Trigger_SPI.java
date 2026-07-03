package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂，提供串口数量等常量
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;  // 串口工具类，提供SPI数据解析

/*
 * +=============================================================================+
 * |                      Command_Trigger_SPI - SPI总线触发命令模型                     |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，SPI总线触发器的参数存储与UI同步                    |
 * | 核心职责 : 管理SPI协议触发器的全部可配置参数（触发类型、触发掩码/数据、                |
 * |            CLK/Data/CS三线触发电平），参数变更时通过RxBus通知UI层刷新                 |
 * | 架构设计 : 纯数据模型 + 观察者通知，SPI触发类型和数据的设置会同步调用                  |
 * |            Command.get().getBus_spi().setType()更新SPI总线解码配置                     |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : ChannelFactory(串口数量)、SerialsUtils(SPI数据解析)、                      |
 * |            CommandMsgToUI(消息封装)、RxBus/RxEnum(事件总线)                          |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置SPI触发参数时，由SCPI分发器调用对应方法       |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_SPI {
//      new SCPICommandStruct(":TRIGger:SPI:DATA","SCPI_Trigger_SPI","Data"),//设置SPI触发下的数据值
//            new SCPICommandStruct(":TRIGger:SPI:DATA?","SCPI_Trigger_SPI","DataQ"),//查询SPI触发下的数据值
//            new SCPICommandStruct(":TRIGger:SPI:SOURce","SCPI_Trigger_SPI","Source"),//设置SPI触发的触发源
//            new SCPICommandStruct(":TRIGger:SPI:SOURce?","SCPI_Trigger_SPI","SourceQ"),//查询SPI触发的触发源
//            new SCPICommandStruct(":TRIGger:SPI:LEVel","SCPI_Trigger_SPI","Level"),//设置SPI触发时的触发电平
//            new SCPICommandStruct(":TRIGger:SPI:LEVel?","SCPI_Trigger_SPI","LevelQ"),//查询SPI触发时的触发电平


    private final int[] type = new int[ChannelFactory.SERIAL_CNT];  // SPI触发类型数组
    private final int[] triggerMask = new int[ChannelFactory.SERIAL_CNT];  // SPI触发掩码数组（标识哪些位需要比较）
    private final int[] triggerData = new int[ChannelFactory.SERIAL_CNT];  // SPI触发数据数组（待比较的数据值）

    private final double[] levelCLK = new double[ChannelFactory.SERIAL_CNT];  // SPI时钟线触发电平数组
    private final double[] levelData = new double[ChannelFactory.SERIAL_CNT];  // SPI数据线触发电平数组
    private final double[] levelCS = new double[ChannelFactory.SERIAL_CNT];  // SPI片选线触发电平数组

    /**
     * 设置指定串口的SPI触发类型，同时同步更新SPI总线解码配置
     *
     * @param s_num       串口索引
     * @param type        触发类型值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Type(int s_num,int type,boolean isUpdateUI){
        this.type[s_num]=type;  // 更新SPI触发类型
        Command.get().getBus_spi().setType(s_num,type,this.triggerMask[s_num],this.triggerData[s_num],isUpdateUI);  // 同步更新SPI总线解码器的类型/掩码/数据
    }

    /**
     * 查询指定串口的SPI触发类型
     *
     * @param s_num 串口索引
     * @return 触发类型值
     */
    public int TypeQ(int s_num){
        return type[s_num];  // 返回指定串口的触发类型
    }

    /**
     * 设置指定串口的SPI触发数据，将字符串解析为掩码和数据后同步更新SPI总线解码配置
     *
     * @param s_num       串口索引
     * @param data        SPI触发数据字符串（含X占位符）
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void Data(int s_num,String data,boolean isUpdateUI){

        this.triggerMask[s_num]= (int)SerialsUtils.toDLong(SerialsUtils.getSpiMask(data.trim()),2);  // 解析字符串获取掩码，去除首尾空格后转为二进制长整型再强转int
        this.triggerData[s_num]=(int)SerialsUtils.toDLong(SerialsUtils.getSpiData(data.trim()),2);  // 解析字符串获取数据，去除首尾空格后转为二进制长整型再强转int
        Command.get().getBus_spi().setType(s_num,this.type[s_num],this.triggerMask[s_num],this.triggerData[s_num],isUpdateUI);  // 同步更新SPI总线解码器的类型/掩码/数据
    }

    /**
     * 查询指定串口的SPI触发数据，将掩码和数据组合为可读字符串
     *
     * @param s_num 串口索引
     * @return SPI触发数据字符串（含X占位符）
     */
    public  String  DataQ(int s_num){
        return SerialsUtils.getSpiText(this.triggerMask[s_num],this.triggerData[s_num]);  // 将掩码和数据转换为含X占位符的可读字符串
    }


    /**
     * 设置指定串口的SPI时钟线触发电平，值变化时通知UI刷新
     *
     * @param s_num       串口索引
     * @param level       时钟线触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void LevelCLK(int s_num,double level,boolean isUpdateUI){
        this.levelCLK[s_num]=level;  // 更新时钟线触发电平
        if (isUpdateUI){  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCLOCK);  // 设置消息标志为SPI时钟线电平变更
            msgToUI.setParam(String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));  // 拼接参数：串口索引+电平值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询指定串口的SPI时钟线触发电平
     *
     * @param s_num 串口索引
     * @return 时钟线触发电平值
     */
    public  double LevelCLKQ(int s_num){
        return this.levelCLK[s_num];  // 返回指定串口的时钟线触发电平
    }

    /**
     * 设置指定串口的SPI数据线触发电平，值变化时通知UI刷新
     *
     * @param s_num       串口索引
     * @param level       数据线触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void LevelData(int s_num,double level,boolean isUpdateUI){
        this.levelData[s_num]=level;  // 更新数据线触发电平
        if (isUpdateUI){  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELDATA);  // 设置消息标志为SPI数据线电平变更
            msgToUI.setParam(String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询指定串口的SPI数据线触发电平
     *
     * @param s_num 串口索引
     * @return 数据线触发电平值
     */
    public  double LevelDataQ(int s_num){
        return this.levelData[s_num];  // 返回指定串口的数据线触发电平
    }

    /**
     * 设置指定串口的SPI片选线触发电平，值变化时通知UI刷新
     *
     * @param s_num       串口索引
     * @param level       片选线触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void LevelCS(int s_num,double level,boolean isUpdateUI){
        this.levelCS[s_num]=level;  // 更新片选线触发电平
        if (isUpdateUI){  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCS);  // 设置消息标志为SPI片选线电平变更
            msgToUI.setParam(String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询指定串口的SPI片选线触发电平
     *
     * @param s_num 串口索引
     * @return 片选线触发电平值
     */
    public  double LevelCSQ(int s_num){
        return this.levelCS[s_num];  // 返回指定串口的片选线触发电平
    }



}
