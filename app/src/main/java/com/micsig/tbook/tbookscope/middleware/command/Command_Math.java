package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2018/1/17.
 */

public class Command_Math {
    //    new SCPICommandStruct(":MATH:DISPlay","SCPI_Math","Display"),//打开或关闭数学运算
//            new SCPICommandStruct(":MATH:DISPlay?","SCPI_Math","DisplayQ"),//查询数学运算打开或关闭
//            new SCPICommandStruct(":MATH:MODE","SCPI_Math","Mode"),//选择数学运算类型
//            new SCPICommandStruct(":MATH:MODE?","SCPI_Math","ModeQ"),//查询数学运算类型

    private boolean isOpen;
    /**
     * const char * math_mode[] = {
     * "BASE",
     * "FFT",
     * "AX+B",
     * "ADV",
     * NULL
     * };
     */
    private int modeIndex;

    /**
     * center,zero
     */
    private int vRefIndex;

    /**
     * 是否需要数学波形,
     * 这与打开和关闭不同，在保存波形中，如果没有打开数学，要进行打开进行保存，之后再进行还原现场。
     */
    private boolean needMathWave = false;

    /**
     * 打开或关闭数学运算
     */
    public void Display(int mathIndex, boolean isOpen, boolean isUpdateUI) {
        if (this.isOpen == isOpen) return;
        this.isOpen = isOpen;
        setNeedMathWave(this.isOpen);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_DISPLAY);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(isOpen);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询数学运算打开或关闭
     */
    public boolean DisplayQ() {
        return this.isOpen;
    }

    /**
     * 选择数学运算类型
     */
    public void Mode(int mathIndex, int index, boolean isUpdateUI) {
        if (this.modeIndex == index) return;
        this.modeIndex = index;

        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_MODE);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询数学运算类型
     */
    public int ModeQ() {
        return this.modeIndex;
    }


    public boolean isNeedMathWave() {
        return needMathWave;
    }

    /**
     * 这与打开和关闭不同，在保存波形中，如果没有打开数学，要进行打开进行保存，之后再进行还原现场,即关闭。
     */
    public void setNeedMathWave(boolean needMathWave) {
        if (this.needMathWave == needMathWave) return;
        this.needMathWave = needMathWave;
    }

    public void VRef(int mathIndex, int index, boolean isUpdateUI) {
        if (this.vRefIndex==index) return;
        this.vRefIndex=index;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_VREF);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int VRefQ(){
        return this.vRefIndex;
    }
}
