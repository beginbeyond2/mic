package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂，提供串口数量等常量
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;  // SCPI指令工具类，提供参数校验

/*
 * +=============================================================================+
 * |                   Command_Trigger_M1553B - MIL-STD-1553B总线触发命令模型            |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，MIL-STD-1553B总线触发器的参数存储与UI同步         |
 * | 核心职责 : 管理1553B协议触发器的全部可配置参数（触发源、显示格式、触发类型、            |
 * |            指令/状态字、远程终端地址、数据字、触发电平），参数变更时通过RxBus通知UI层刷新 |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : ChannelFactory(串口数量)、ToolsSCPI(参数校验)、CommandMsgToUI(消息封装)、  |
 * |            RxBus/RxEnum(事件总线)                                                   |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置1553B触发参数时，由SCPI分发器调用对应方法     |
 * +=============================================================================+
 */

/**
 * Created by yangj on 2018/1/22.
 */

public class Command_Trigger_M1553B {
    private int[] source = new int[ChannelFactory.SERIAL_CNT];  // 1553B触发源通道索引数组
    private int[] display = new int[ChannelFactory.SERIAL_CNT];  // 1553B显示格式数组
    /**
     * 指令/状态字同步头
     * 数据字同步头
     * 指令/状态字
     * 远程终端地址
     * 曼切斯特码错误
     * 数据字
     * 奇校验错误
     * 所有错误
     */
    private final int[] type = new int[ChannelFactory.SERIAL_CNT];  // 1553B触发类型数组
    private final int[] csWord = new int[ChannelFactory.SERIAL_CNT];  // 1553B指令/状态字数组
    private final int[] dataWord = new int[ChannelFactory.SERIAL_CNT];  // 1553B数据字数组
    private final int[] rtAddr = new int[ChannelFactory.SERIAL_CNT];  // 1553B远程终端地址数组
    private final double[] level = new double[ChannelFactory.SERIAL_CNT];  // 1553B触发电平数组

    /**
     * 获取指定串口的1553B触发源
     *
     * @param serials 串口索引
     * @return 触发源值
     */
    public int getSource(int serials) {
        return source[serials];  // 返回指定串口的触发源
    }

    /**
     * 设置指定串口的1553B触发源，值变化时通知UI刷新
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_SOURCE);  // 设置消息标志为1553B触发源变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的1553B显示格式
     *
     * @param serials 串口索引
     * @return 显示格式值
     */
    public int getDisplay(int serials) {
        return display[serials];  // 返回指定串口的显示格式
    }

    /**
     * 设置指定串口的1553B显示格式，值变化时通知UI刷新
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
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_DISPLAY);  // 设置消息标志为1553B显示格式变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的1553B触发类型
     *
     * @param serials 串口索引
     * @return 触发类型值
     */
    public int getType(int serials) {
        return type[serials];  // 返回指定串口的触发类型
    }

    /**
     * 获取指定串口的1553B指令/状态字
     *
     * @param serials 串口索引
     * @return 指令/状态字值
     */
    public int getCsWord(int serials) {
        return csWord[serials];  // 返回指定串口的指令/状态字
    }

    /**
     * 获取指定串口的1553B数据字
     *
     * @param serials 串口索引
     * @return 数据字值
     */
    public int getDataWord(int serials) {
        return dataWord[serials];  // 返回指定串口的数据字
    }

    /**
     * 获取指定串口的1553B远程终端地址
     *
     * @param serials 串口索引
     * @return 远程终端地址值
     */
    public int getRtAddr(int serials) {
        return rtAddr[serials];  // 返回指定串口的远程终端地址
    }

    /**
     * 批量设置指定串口的1553B触发类型、指令/状态字、远程终端地址和数据字，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param type        触发类型
     * @param csWord      指令/状态字
     * @param rtAddr      远程终端地址
     * @param dataWord    数据字
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setType(int serials, int type, int csWord, int rtAddr, int dataWord, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.type)) return;  // 校验type数组索引
        if(!ToolsSCPI.isCorrect(serials,this.csWord)) return;  // 校验csWord数组索引
        if(!ToolsSCPI.isCorrect(serials,this.rtAddr)) return;  // 校验rtAddr数组索引
        if(!ToolsSCPI.isCorrect(serials,this.dataWord)) return;  // 校验dataWord数组索引
//        if (this.type[serials] == type && this.csWord[serials] == csWord && this.rtAddr[serials] == rtAddr
//                && this.dataWord[serials] == dataWord)
//            return;
        this.type[serials] = type;  // 更新触发类型
        this.csWord[serials] = csWord;  // 更新指令/状态字
        this.rtAddr[serials] = rtAddr;  // 更新远程终端地址
        this.dataWord[serials] = dataWord;  // 更新数据字
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_TYPE);  // 设置消息标志为1553B触发类型变更
            msgToUI.setParam(String.valueOf(serials)  // 拼接参数：串口索引
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type)  // + 分隔符 + 触发类型
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(csWord)  // + 分隔符 + 指令/状态字
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(rtAddr)  // + 分隔符 + 远程终端地址
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(dataWord));  // + 分隔符 + 数据字
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 获取指定串口的1553B触发电平
     *
     * @param serials 串口索引
     * @return 触发电平值
     */
    public double getLevel(int serials) {
        return level[serials];  // 返回指定串口的触发电平
    }

    /**
     * 设置指定串口的1553B触发电平，值变化时通知UI刷新
     *
     * @param serials     串口索引
     * @param level       触发电平值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void setLevel(int serials, double level, boolean isUpdateUI) {
        if(!ToolsSCPI.isCorrect(serials,this.level)) return;  // 校验串口索引是否合法
        if (this.level[serials] == level) return;  // 值未变化则直接返回
        this.level[serials] = level;  // 更新触发电平值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_LEVEL);  // 设置消息标志为1553B触发电平变更
            msgToUI.setParam(String.valueOf(serials) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level));  // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }
}
