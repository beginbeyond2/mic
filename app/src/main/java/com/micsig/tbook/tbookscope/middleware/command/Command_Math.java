package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI数学运算命令处理

import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 通道号定义

/**
 * Created by liwb on 2018/1/17.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                             Command_Math                                     |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 数学运算(Math)顶层命令处理层                     |
 * | 核心职责：管理数学运算通道的开关、运算类型（BASE/FFT/AX+B/ADV）、           |
 * |          垂直展开基准（center/zero），以及数学波形需求标志                   |
 * | 架构设计：属于Command子模块，由Command单例统一调度；                         |
 * |          运算类型由modeIndex区分（0=BASE, 1=FFT, 2=AX+B, 3=ADV）；          |
 * |          needMathWave标志用于保存波形时临时打开数学通道后还原现场            |
 * | 数据流向：SCPI指令 → Command_Math → 成员变量(状态存储)                     |
 * |                            → CommandMsgToUI → RxBus → UI层                  |
 * | 依赖关系：Command(单例入口)、RxBus(事件总线)                                |
 * | 使用场景：远程SCPI控制中打开/关闭数学通道、切换运算类型                     |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Math {
    //    new SCPICommandStruct(":MATH:DISPlay","SCPI_Math","Display"),//打开或关闭数学运算
//            new SCPICommandStruct(":MATH:DISPlay?","SCPI_Math","DisplayQ"),//查询数学运算打开或关闭
//            new SCPICommandStruct(":MATH:MODE","SCPI_Math","Mode"),//选择数学运算类型
//            new SCPICommandStruct(":MATH:MODE?","SCPI_Math","ModeQ"),//查询数学运算类型

    private boolean isOpen; // 数学通道显示开关
    /**
     * const char * math_mode[] = {
     * "BASE",
     * "FFT",
     * "AX+B",
     * "ADV",
     * NULL
     * };
     */
    private int modeIndex; // 数学运算类型索引（0=BASE, 1=FFT, 2=AX+B, 3=ADV）

    /**
     * center,zero
     */
    private int vRefIndex; // 垂直展开基准索引（0=center, 1=zero）

    /**
     * 是否需要数学波形,
     * 这与打开和关闭不同，在保存波形中，如果没有打开数学，要进行打开进行保存，之后再进行还原现场。
     */
    private boolean needMathWave = false; // 数学波形需求标志，用于保存波形场景

    /**
     * 打开或关闭数学运算
     * 对应SCPI指令: :MATH:DISPlay
     * @param mathIndex 数学通道索引
     * @param isOpen 是否打开
     * @param isUpdateUI 是否通知UI更新
     */
    public void Display(int mathIndex, boolean isOpen, boolean isUpdateUI) {
        if (this.isOpen == isOpen) return; // 状态未变则直接返回
        this.isOpen = isOpen; // 更新数学通道开关
        setNeedMathWave(this.isOpen); // 同步更新数学波形需求标志
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_DISPLAY); // 设置消息标志为通道显示（数学通道复用通道显示标志）
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(isOpen); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询数学运算打开或关闭
     * 对应SCPI指令: :MATH:DISPlay?
     * @return 是否打开数学运算
     */
    public boolean DisplayQ() {
        return this.isOpen; // 返回数学通道开关状态
    }

    /**
     * 选择数学运算类型
     * 对应SCPI指令: :MATH:MODE
     * @param mathIndex 数学通道索引
     * @param index 运算类型索引（0=BASE, 1=FFT, 2=AX+B, 3=ADV）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Mode(int mathIndex, int index, boolean isUpdateUI) {
        if (this.modeIndex == index) return; // 运算类型未变则直接返回
        this.modeIndex = index; // 更新运算类型

        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_MODE); // 设置消息标志为数学运算类型
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询数学运算类型
     * 对应SCPI指令: :MATH:MODE?
     * @return 运算类型索引
     */
    public int ModeQ() {
        return this.modeIndex; // 返回运算类型索引
    }


    /**
     * 查询是否需要数学波形
     * @return 是否需要数学波形
     */
    public boolean isNeedMathWave() {
        return needMathWave; // 返回数学波形需求标志
    }

    /**
     * 设置数学波形需求标志
     * 这与打开和关闭不同，在保存波形中，如果没有打开数学，要进行打开进行保存，之后再进行还原现场,即关闭。
     * @param needMathWave 是否需要数学波形
     */
    public void setNeedMathWave(boolean needMathWave) {
        if (this.needMathWave == needMathWave) return; // 标志未变则直接返回
        this.needMathWave = needMathWave; // 更新数学波形需求标志
    }

    /**
     * 设置数学通道垂直展开基准
     * 对应SCPI指令: :MATH:VREF
     * @param mathIndex 数学通道索引
     * @param index 垂直展开基准索引（0=center, 1=zero）
     * @param isUpdateUI 是否通知UI更新
     */
    public void VRef(int mathIndex, int index, boolean isUpdateUI) {
        if (this.vRefIndex==index) return; // 基准未变则直接返回
        this.vRefIndex=index; // 更新垂直展开基准
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_VREF); // 设置消息标志为数学垂直展开基准
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询数学通道垂直展开基准
     * 对应SCPI指令: :MATH:VREF?
     * @return 垂直展开基准索引
     */
    public int VRefQ(){
        return this.vRefIndex; // 返回垂直展开基准索引
    }
}
