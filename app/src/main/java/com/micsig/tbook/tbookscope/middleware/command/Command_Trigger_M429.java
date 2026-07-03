package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂，提供串口数量等常量
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;  // SCPI指令工具类，提供参数校验

/*
 * +=============================================================================+
 * |                    Command_Trigger_M429 - ARINC429总线触发命令模型                  |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，ARINC429总线触发器的参数存储与UI同步              |
 * | 核心职责 : 管理429协议触发器的全部可配置参数（触发源、编码格式、显示格式、波特率、       |
 * |            触发类型、LABEL、SDI、数据、SSM、高/低电平），参数变更时通过RxBus通知UI刷新 |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : ChannelFactory(串口数量)、ToolsSCPI(参数校验)、CommandMsgToUI(消息封装)、  |
 * |            RxBus/RxEnum(事件总线)                                                   |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置ARINC429触发参数时，由SCPI分发器调用对应方法 |
 * +=============================================================================+
 */

/**
 * Created by yangj on 2018/1/22.
 */

public class Command_Trigger_M429 {
    private final int[] source = new int[ChannelFactory.SERIAL_CNT];  // 429触发源通道索引数组
    private final int[] format = new int[ChannelFactory.SERIAL_CNT];  // 429编码格式数组
    private final int[] display = new int[ChannelFactory.SERIAL_CNT];  // 429显示格式数组
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT];  // 429波特率数组
    /**
     * 字起始
     * 字结束
     * LABEL
     * SDI
     * DATA
     * SSM
     * LABEL+SDI
     * label+Data
     * Label+SSM
     * 字错误
     * 字间隙错误
     * 校验错误
     * 所有错误
     * 所有0位
     * 所有1位
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];  // 429触发类型数组，对应上述枚举
    private final int[] label = new int[ChannelFactory.SERIAL_CNT];  // 429 LABEL字段数组
    private final int[] sdi = new int[ChannelFactory.SERIAL_CNT];  // 429 SDI字段数组
    private final long[] data = new long[ChannelFactory.SERIAL_CNT];  // 429数据字段数组
    private final int[] ssm = new int[ChannelFactory.SERIAL_CNT];  // 429 SSM字段数组
    private final double[] levelHigh = new double[ChannelFactory.SERIAL_CNT];  // 429高电平触发电平数组
    private final double[] levelLow = new double[ChannelFactory.SERIAL_CNT];  // 429低电平触发电平数组

    /**
     * 获取指定串口的429触发源
     *
     * @param serials 串口索引
     * @return 触发源值
     */
    public int getSource(int serials) {
        return source[serials];  // 返回指定串口的触发源
    }

    /**
     * 设置指定串口的429触发源，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param source      触发源值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setSource(int serials, int source, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.source)) return;  // 校验串口索引是否合法
        if (this.source[serials] == source) return;  // 值未变化则直接返回
        this.source[serials] = source;  // 更新触发源值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_SOURCE);  // 设置消息标志为429触发源变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的429编码格式
     *
     * @param serials 串口索引
     * @return 编码格式值
     */
    public int getFormat(int serials) {
        return format[serials];  // 返回指定串口的编码格式
    }

    /**
     * 设置指定串口的429编码格式，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param format      编码格式值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setFormat(int serials, int format, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.format)) return;  // 校验串口索引是否合法
        if (this.format[serials] == format) return;  // 值未变化则直接返回
        this.format[serials] = format;  // 更新编码格式值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_FORMAT);  // 设置消息标志为429编码格式变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(format));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的429显示格式
     *
     * @param serials 串口索引
     * @return 显示格式值
     */
    public int getDisplay(int serials) {
        return display[serials];  // 返回指定串口的显示格式
    }

    /**
     * 设置指定串口的429显示格式，值变化时通知UI刷新
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_DISPLAY);  // 设置消息标志为429显示格式变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的429波特率
     *
     * @param serials 串口索引
     * @return 波特率值
     */
    public int getBaudRate(int serials) {
        return baudRate[serials];  // 返回指定串口的波特率
    }

    /**
     * 设置指定串口的429波特率，值变化时通知UI刷新
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_BAUDRATE);  // 设置消息标志为429波特率变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的429触发类型
     *
     * @param serials 串口索引
     * @return 触发类型值
     */
    public int getType(int serials) {
        return type[serials];  // 返回指定串口的触发类型
    }

    /**
     * 获取指定串口的429 LABEL字段
     *
     * @param serials 串口索引
     * @return LABEL值
     */
    public int getLabel(int serials) {
        return label[serials];  // 返回指定串口的LABEL字段
    }

    /**
     * 获取指定串口的429 SDI字段
     *
     * @param serials 串口索引
     * @return SDI值
     */
    public int getSdi(int serials) {
        return sdi[serials];  // 返回指定串口的SDI字段
    }

    /**
     * 获取指定串口的429数据字段
     *
     * @param serials 串口索引
     * @return 数据值
     */
    public long getData(int serials) {
        return data[serials];  // 返回指定串口的数据字段
    }

    /**
     * 获取指定串口的429 SSM字段
     *
     * @param serials 串口索引
     * @return SSM值
     */
    public int getSsm(int serials) {
        return ssm[serials];  // 返回指定串口的SSM字段
    }

    /**
     * 批量设置指定串口的429触发类型、LABEL、SDI、数据和SSM，包含范围校验，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param type        触发类型
     * @param label       LABEL字段值（0-255）
     * @param sdi         SDI字段值
     * @param data        数据字段值（0-0xFFFFFF）
     * @param ssm         SSM字段值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setType(int serials, int type, int label, int sdi, long data, int ssm, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;  // 校验type数组索引
        if(!ToolsSCPI.isCorrect(serials,this.label)) return;  // 校验label数组索引
        if(!ToolsSCPI.isCorrect(serials,this.sdi)) return;  // 校验sdi数组索引
        if(!ToolsSCPI.isCorrect(serials,this.data)) return;  // 校验data数组索引
        if(!ToolsSCPI.isCorrect(serials,this.ssm)) return;  // 校验ssm数组索引
//        if (this.type[serials] == type && this.label[serials] == label && this.sdi[serials] == sdi
//                && this.data[serials] == data && this.ssm[serials] == ssm)
//            return;
        int minLabel=0;  // LABEL最小值
        int maxLabel=255; //八进制 377  // LABEL最大值
        if (label<minLabel) label=minLabel;  // LABEL低于下限则钳位到下限
        if (label>maxLabel) label=maxLabel;  // LABEL超过上限则钳位到上限

        int minData=0;  // 数据最小值
        int maxData=0xFFFFFF;  // 数据最大值（24位）
        if (data<minData) data=minData;  // 数据低于下限则钳位到下限
        if (data>maxData) data=maxData;  // 数据超过上限则钳位到上限


        this.type[serials] = type;  // 更新触发类型
        this.label[serials] = label;  // 更新LABEL字段
        this.sdi[serials] = sdi;  // 更新SDI字段
        this.data[serials] = data;  // 更新数据字段
        this.ssm[serials] = ssm;  // 更新SSM字段
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_TYPE);  // 设置消息标志为429触发类型变更
            msgToUI.setParam(String.valueOf(serials)  // 拼接参数：串口索引
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)  // + 分隔符 + 触发类型
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(label)  // + 分隔符 + LABEL
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(sdi)  // + 分隔符 + SDI
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(data)  // + 分隔符 + 数据
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(ssm));  // + 分隔符 + SSM
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的429高电平触发电平
     *
     * @param serials 串口索引
     * @return 高电平触发电平值
     */
    public double getLevelHigh(int serials) {
        return levelHigh[serials];  // 返回指定串口的高电平触发电平
    }

    /**
     * 设置指定串口的429高电平触发电平，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param ch          通道索引（附加参数）
     * @param level       高电平触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setLevelHigh(int serials,int ch, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.levelHigh)) return;  // 校验串口索引是否合法
//        if (this.levelHigh[serials] == level) return;
        this.levelHigh[serials] =  level;  // 更新高电平触发电平
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_LEVELHIGH);  // 设置消息标志为429高电平变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level)+CommandMsgToUI.PARAM_SPLIT+ch);  // 拼接参数：串口+电平+通道
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的429低电平触发电平
     *
     * @param serials 串口索引
     * @return 低电平触发电平值
     */
    public double getLevelLow(int serials) {
        return levelLow[serials];  // 返回指定串口的低电平触发电平
    }

    /**
     * 设置指定串口的429低电平触发电平，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param ch          通道索引（附加参数）
     * @param level       低电平触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setLevelLow(int serials, int ch, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.levelLow)) return;  // 校验串口索引是否合法
//        if (this.levelLow[serials] == level) return;
        this.levelLow[serials] =  level;  // 更新低电平触发电平
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_LEVELLOW);  // 设置消息标志为429低电平变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level)+CommandMsgToUI.PARAM_SPLIT+ch);  // 拼接参数：串口+电平+通道
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }
}
