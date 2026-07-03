package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂，提供串口数量等常量
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;  // SCPI指令工具类，提供参数校验

/*
 * +=============================================================================+
 * |                     Command_Trigger_Uart - UART总线触发命令模型                    |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，UART总线触发器的参数存储与UI同步                  |
 * | 核心职责 : 管理UART协议触发器的全部可配置参数（触发源、空闲电平、校验位、数据位、       |
 * |            波特率、显示格式、触发电平、触发类型/条件/编号），参数变更时通过RxBus       |
 * |            通知UI层刷新                                                              |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : ChannelFactory(串口数量)、ToolsSCPI(参数校验)、CommandMsgToUI(消息封装)、  |
 * |            RxBus/RxEnum(事件总线)                                                   |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置UART触发参数时，由SCPI分发器调用对应方法     |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Uart {

//     new SCPICommandStruct(":TRIGger:UART:SOURce","SCPI_Trigger_Uart","Source"),//设置UART触发的触发源
//     new SCPICommandStruct(":TRIGger:UART:SOURce?","SCPI_Trigger_Uart","SourceQ"),//查询UART触发的触发源
//     new SCPICommandStruct(":TRIGger:UART:TYPE","SCPI_Trigger_Uart","Type"),//设置UART触发的触发条件
//     new SCPICommandStruct(":TRIGger:UART:TYPE?","SCPI_Trigger_Uart","TypeQ"),//查询UART触发的触发条件
//     new SCPICommandStruct(":TRIGger:UART:RELation","SCPI_Trigger_Uart","Relation"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发关系
//     new SCPICommandStruct(":TRIGger:UART:RELation?","SCPI_Trigger_Uart","RelationQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发关系
//     new SCPICommandStruct(":TRIGger:UART:DATA","SCPI_Trigger_Uart","Data"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发数据。
//     new SCPICommandStruct(":TRIGger:UART:DATA?","SCPI_Trigger_Uart","DataQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发数据。
//     new SCPICommandStruct(":TRIGger:UART:LEVel","SCPI_Trigger_Uart","Level"),//设置UART触发时的触发电平
//     new SCPICommandStruct(":TRIGger:UART:LEVel?","SCPI_Trigger_Uart","LevelQ"),//查询UART触发时的触发电平

    private final int[] source = new int[ChannelFactory.SERIAL_CNT];  // UART触发源通道索引数组
    private final int[] idle = new int[ChannelFactory.SERIAL_CNT];  // UART空闲电平数组
    private final int[] check = new int[ChannelFactory.SERIAL_CNT];  // UART校验位配置数组
    private final int[] bits = new int[ChannelFactory.SERIAL_CNT];  // UART数据位配置数组
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT];  // UART波特率数组
    private final int[] display = new int[ChannelFactory.SERIAL_CNT];  // UART显示格式数组
    private final double[] level = new double[ChannelFactory.SERIAL_CNT];  // UART触发电平数组
    /**
     * 开始位
     * 停止位
     * 数据
     * [0:数据]
     * [1:数据]
     * [x:数据]
     * 奇偶检验错误
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];  // UART触发类型数组
    private final int[] condition = new int[ChannelFactory.SERIAL_CNT];  // UART触发条件/关系数组
    private final int[] number = new int[ChannelFactory.SERIAL_CNT];  // UART触发数据编号数组

    /**
     * 获取指定串口的UART触发源
     *
     * @param serials 串口索引
     * @return 触发源值
     */
    public int getSource(int serials) {
        return source[serials];  // 返回指定串口的触发源
    }

    /**
     * 设置指定串口的UART触发源，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param source      触发源值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setSource(int serials, int source, boolean isUpdateUI) {
        if (!ToolsSCPI.isCorrect(serials, this.source)) return;  // 校验串口索引是否合法
        if (this.source[serials] == source) return;  // 值未变化则直接返回
        this.source[serials] = source;  // 更新触发源值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_SOURCE);  // 设置消息标志为UART触发源变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的UART空闲电平
     *
     * @param serials 串口索引
     * @return 空闲电平值
     */
    public int getIdle(int serials) {
        return idle[serials];  // 返回指定串口的空闲电平
    }

    /**
     * 设置指定串口的UART空闲电平，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param idle        空闲电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setIdle(int serials, int idle, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.idle)) return;  // 校验串口索引是否合法
        if (this.idle[serials] == idle) return;  // 值未变化则直接返回
        this.idle[serials] = idle;  // 更新空闲电平值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_IDLE);  // 设置消息标志为UART空闲电平变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(idle));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的UART校验位配置
     *
     * @param serials 串口索引
     * @return 校验位配置值
     */
    public int getCheck(int serials) {
        return check[serials];  // 返回指定串口的校验位配置
    }

    /**
     * 设置指定串口的UART校验位配置，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param check       校验位配置值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setCheck(int serials, int check, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.check)) return;  // 校验串口索引是否合法
        if (this.check[serials] == check) return;  // 值未变化则直接返回
        this.check[serials] = check;  // 更新校验位配置值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_CHECK);  // 设置消息标志为UART校验位变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(check));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的UART数据位配置
     *
     * @param serials 串口索引
     * @return 数据位配置值
     */
    public int getBits(int serials) {
        return bits[serials];  // 返回指定串口的数据位配置
    }

    /**
     * 设置指定串口的UART数据位配置，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param bits        数据位配置值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setBits(int serials, int bits, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.bits)) return;  // 校验串口索引是否合法
        if (this.bits[serials] == bits) return;  // 值未变化则直接返回
        this.bits[serials] = bits;  // 更新数据位配置值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_BITS);  // 设置消息标志为UART数据位变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(bits));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的UART波特率
     *
     * @param serials 串口索引
     * @return 波特率值
     */
    public int getBaudRate(int serials) {
        return baudRate[serials];  // 返回指定串口的波特率
    }

    /**
     * 设置指定串口的UART波特率，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param baudRate    波特率值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setBaudRate(int serials, int baudRate, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.baudRate)) return;  // 校验串口索引是否合法
        if (this.baudRate[serials] == baudRate) return;  // 值未变化则直接返回
        this.baudRate[serials] = baudRate;  // 更新波特率值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_BAUDRATE);  // 设置消息标志为UART波特率变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的UART显示格式
     *
     * @param serials 串口索引
     * @return 显示格式值
     */
    public int getDisplay(int serials) {
        return display[serials];  // 返回指定串口的显示格式
    }

    /**
     * 设置指定串口的UART显示格式，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param display     显示格式值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setDisplay(int serials, int display, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.display)) return;  // 校验串口索引是否合法
        if (this.display[serials] == display) return;  // 值未变化则直接返回
        this.display[serials] = display;  // 更新显示格式值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_DISPLAY);  // 设置消息标志为UART显示格式变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的UART触发电平
     *
     * @param serials 串口索引
     * @return 触发电平值
     */
    public double getLevel(int serials) {
        return level[serials];  // 返回指定串口的触发电平
    }

    /**
     * 设置指定串口的UART触发电平，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param level       触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setLevel(int serials, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.level)) return;  // 校验串口索引是否合法
        //if (this.level[serials] == level) return;
        this.level[serials] = level;  // 更新触发电平值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_LEVEL);  // 设置消息标志为UART触发电平变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 批量设置指定串口的UART触发类型、条件和编号，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param type        触发类型
     * @param condition   触发条件/关系
     * @param number      触发数据编号
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setType(int serials, int type, int condition, int number, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;  // 校验type数组索引
        if(!ToolsSCPI.isCorrect(serials,this.condition)) return;  // 校验condition数组索引
        if(!ToolsSCPI.isCorrect(serials,this.number)) return;  // 校验number数组索引
        if (this.type[serials] == type && this.condition[serials] == condition && this.number[serials] == number) return;  // 全部值未变化则直接返回
        this.type[serials] = type;  // 更新触发类型
        this.condition[serials] = condition;  // 更新触发条件
        this.number[serials] = number;  // 更新触发数据编号
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_TYPE);  // 设置消息标志为UART触发类型变更
            msgToUI.setParam(String.valueOf(serials)  // 拼接参数：串口索引
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)  // + 分隔符 + 触发类型
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(condition)  // + 分隔符 + 触发条件
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(number));  // + 分隔符 + 触发数据编号
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的UART触发类型
     *
     * @param serials 串口索引
     * @return 触发类型值
     */
    public int getType(int serials) {
        return type[serials];  // 返回指定串口的触发类型
    }

    /**
     * 获取指定串口的UART触发条件
     *
     * @param serials 串口索引
     * @return 触发条件值
     */
    public int getCondition(int serials) {
        return condition[serials];  // 返回指定串口的触发条件
    }

    /**
     * 获取指定串口的UART触发数据编号
     *
     * @param serials 串口索引
     * @return 触发数据编号值
     */
    public int getNumber(int serials) {
        return number[serials];  // 返回指定串口的触发数据编号
    }
}
