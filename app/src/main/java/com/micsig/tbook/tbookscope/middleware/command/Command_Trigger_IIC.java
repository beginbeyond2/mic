package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂，提供串口数量等常量
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;  // SCPI指令工具类，提供参数校验

/*
 * +=============================================================================+
 * |                       Command_Trigger_IIC - IIC触发命令模型                       |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，IIC总线触发器的参数存储与UI同步                    |
 * | 核心职责 : 管理IIC协议触发器的全部可配置参数（触发源、时钟、类型、地址、数据、电平）， |
 * |            参数变更时通过RxBus通知UI层刷新                                           |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : ChannelFactory(串口数量)、ToolsSCPI(参数校验)、CommandMsgToUI(消息封装)、  |
 * |            RxBus/RxEnum(事件总线)                                                   |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置I2C触发参数时，由SCPI分发器调用对应方法       |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_IIC {
    //     new SCPICommandStruct(":TRIGger:IIC:SOURce","SCPI_Trigger_IIC","Source"),//设置IIC触发的触发源
//            new SCPICommandStruct(":TRIGger:IIC:SOURce?","SCPI_Trigger_IIC","SourceQ"),//查询IIC触发的触发源
//            new SCPICommandStruct(":TRIGger:IIC:TYPE","SCPI_Trigger_IIC","Type"),//设置IIC触发的触发类型
//            new SCPICommandStruct(":TRIGger:IIC:TYPE?","SCPI_Trigger_IIC","TypeQ"),//查询IIC触发的触发类型
//            new SCPICommandStruct(":TRIGger:IIC:ADDRess","SCPI_Trigger_IIC","Address"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，设置IIC总线触发的触发地址
//            new SCPICommandStruct(":TRIGger:IIC:ADDRess?","SCPI_Trigger_IIC","AddressQ"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，查询IIC总线触发的触发地址
//            new SCPICommandStruct(":TRIGger:IIC:RELation","SCPI_Trigger_IIC","Relation"),//当IIC触发条件为RDATa时，设置IIC总线触发的触发关系
//            new SCPICommandStruct(":TRIGger:IIC:RELation?","SCPI_Trigger_IIC","RelationQ"),//当IIC触发条件为RDATa时，查询IIC总线触发的触发关系
//            new SCPICommandStruct(":TRIGger:IIC:DATA","SCPI_Trigger_IIC","Data"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，设置IIC总线触发的触发数据
//            new SCPICommandStruct(":TRIGger:IIC:DATA?","SCPI_Trigger_IIC","DataQ"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，查询IIC总线触发的触发数据
//            new SCPICommandStruct(":TRIGger:IIC:LEVel","SCPI_Trigger_IIC","Level"),//设置IIC触发时的触发电平
//            new SCPICommandStruct(":TRIGger:IIC:LEVel?","SCPI_Trigger_IIC","LevelQ"),//查询IIC触发时的触发电平

    private final int[] source = new int[ChannelFactory.SERIAL_CNT];  // IIC触发源通道索引数组，每个串口一个
    private final int[] clock = new int[ChannelFactory.SERIAL_CNT];  // IIC时钟源通道索引数组
    /**
     * 启动条件
     * 停止条件
     * 确认丢失
     * 重新启动
     * 地址字段无确认
     * 帧型1
     * 帧型2
     * EEPROM数据读写
     * 10位写帧
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];  // IIC触发类型数组，对应上述枚举
    private final int[] addr = new int[ChannelFactory.SERIAL_CNT];  // IIC触发地址数组
    private final int[] data1 = new int[ChannelFactory.SERIAL_CNT];  // IIC触发数据1数组
    private final int[] data2 = new int[ChannelFactory.SERIAL_CNT];  // IIC触发数据2数组
    private final int[] condition = new int[ChannelFactory.SERIAL_CNT];  // IIC触发条件/关系数组
    private final double[] levelData = new double[ChannelFactory.SERIAL_CNT];  // IIC数据线触发电平数组
    private final double[] levelClock = new double[ChannelFactory.SERIAL_CNT];  // IIC时钟线触发电平数组

    /**
     * 获取指定串口的IIC触发源
     *
     * @param serials 串口索引
     * @return 触发源值
     */
    public int getSource(int serials) {
        return source[serials];  // 返回指定串口的触发源
    }

    /**
     * 设置指定串口的IIC触发源，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param source      触发源值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setSource(int serials, int source, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.source)) return;  // 校验串口索引是否合法，不合法则直接返回
        if (this.source[serials] == source) return;  // 值未变化则直接返回，避免冗余通知
        this.source[serials] = source;  // 更新触发源值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 从Command单例获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_SOURCE);  // 设置消息标志为IIC触发源变更
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source);  // 拼接参数：串口索引+分隔符+触发源值
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 通过RxBus广播消息到UI层
        }
    }

    /**
     * 获取指定串口的IIC时钟源
     *
     * @param serials 串口索引
     * @return 时钟源值
     */
    public int getClock(int serials) {
        return clock[serials];  // 返回指定串口的时钟源
    }

    /**
     * 设置指定串口的IIC时钟源，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param clock       时钟源值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setClock(int serials, int clock, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.clock)) return;  // 校验串口索引是否合法
        if (this.clock[serials] == clock) return;  // 值未变化则直接返回
        this.clock[serials] = clock;  // 更新时钟源值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_CLOCK);  // 设置消息标志为IIC时钟源变更
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(clock);  // 拼接参数
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的IIC触发类型
     *
     * @param serials 串口索引
     * @return 触发类型值
     */
    public int getType(int serials) {
        return type[serials];  // 返回指定串口的触发类型
    }

    /**
     * 获取指定串口的IIC触发地址
     *
     * @param serials 串口索引
     * @return 触发地址值
     */
    public int getAddr(int serials) {
        return addr[serials];  // 返回指定串口的触发地址
    }

    /**
     * 获取指定串口的IIC触发数据1
     *
     * @param serials 串口索引
     * @return 触发数据1值
     */
    public int getData1(int serials) {
        return data1[serials];  // 返回指定串口的触发数据1
    }

    /**
     * 获取指定串口的IIC触发数据2
     *
     * @param serials 串口索引
     * @return 触发数据2值
     */
    public int getData2(int serials) {
        return data2[serials];  // 返回指定串口的触发数据2
    }

    /**
     * 获取指定串口的IIC触发条件
     *
     * @param serials 串口索引
     * @return 触发条件值
     */
    public int getCondition(int serials) {
        return condition[serials];  // 返回指定串口的触发条件
    }

    /**
     * 批量设置指定串口的IIC触发类型、地址、数据和条件，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param type        触发类型
     * @param addr        触发地址
     * @param data1       触发数据1
     * @param data2       触发数据2
     * @param condition   触发条件
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setType(int serials, int type, int addr, int data1, int data2, int condition, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;  // 校验type数组索引
        if(!ToolsSCPI.isCorrect(serials,this.addr)) return;  // 校验addr数组索引
        if(!ToolsSCPI.isCorrect(serials,this.data1)) return;  // 校验data1数组索引
        if(!ToolsSCPI.isCorrect(serials,this.data2)) return;  // 校验data2数组索引
        if(!ToolsSCPI.isCorrect(serials,this.condition)) return;  // 校验condition数组索引
//        if (this.type[serials] == type && this.addr[serials] == addr && this.data1[serials] == data1
//                && this.data2[serials] == data2 && this.condition[serials] == condition)
//            return;
        this.type[serials] = type;  // 更新触发类型
        this.addr[serials] = addr;  // 更新触发地址
        this.data1[serials] = data1;  // 更新触发数据1
        this.data2[serials] = data2;  // 更新触发数据2
        this.condition[serials] = condition;  // 更新触发条件
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_TYPE);  // 设置消息标志为IIC触发类型变更
            String param = String.valueOf(serials) +  // 拼接参数：串口索引
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(type) +  // + 分隔符 + 触发类型
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(addr) +  // + 分隔符 + 触发地址
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(data1) +  // + 分隔符 + 触发数据1
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(data2) +  // + 分隔符 + 触发数据2
                    CommandMsgToUI.PARAM_SPLIT + String.valueOf(condition);  // + 分隔符 + 触发条件
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的IIC数据线触发电平
     *
     * @param serials 串口索引
     * @return 数据线触发电平值
     */
    public double getLevelData(int serials) {
        return levelData[serials];  // 返回指定串口的数据线触发电平
    }

    /**
     * 设置指定串口的IIC数据线触发电平，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param levelData   数据线触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setLevelData(int serials, double levelData, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.levelData)) return;  // 校验索引是否合法
//        if (this.levelData[serials] == levelData) return;
        this.levelData[serials] = levelData;  // 更新数据线触发电平
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_LEVELDATA);  // 设置消息标志为IIC数据线电平变更
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(levelData);  // 拼接参数
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的IIC时钟线触发电平
     *
     * @param serials 串口索引
     * @return 时钟线触发电平值
     */
    public double getLevelClock(int serials) {
        return levelClock[serials];  // 返回指定串口的时钟线触发电平
    }

    /**
     * 设置指定串口的IIC时钟线触发电平，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param levelClock  时钟线触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setLevelClock(int serials, double levelClock, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.levelClock)) return;  // 校验索引是否合法
//        if (this.levelClock[serials] == levelClock) return;
        this.levelClock[serials] = levelClock;  // 更新时钟线触发电平
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_LEVELCLOCK);  // 设置消息标志为IIC时钟线电平变更
            String param = String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(levelClock);  // 拼接参数
            msgToUI.setParam(param);  // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }
}
