package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-24 10:09
 */
public class Command_Math_Base {

    //            new SCPICommandStruct(":MATH:BASE:S1","SCPI_Math_BASE","S1"),//选择加法运算的信源1
//            new SCPICommandStruct(":MATH:BASE:S1?","SCPI_Math_BASE","S1Q"),//查询加法运算的信源1
//            new SCPICommandStruct(":MATH:BASE:S2","SCPI_Math_BASE","S2"),//选择加法运算的信源2
//            new SCPICommandStruct(":MATH:BASE:S2?","SCPI_Math_BASE","S2Q"),//查询加法运算的信源2
//            new SCPICommandStruct(":MATH:BASE:EXTent","SCPI_Math_BASE","Extent"),//设置加法运算结果的垂直档位
//            new SCPICommandStruct(":MATH:BASE:EXTent?","SCPI_Math_BASE","ExtentQ"),//查询加法运算结果的垂直档位
//            new SCPICommandStruct(":MATH:BASE:OFFSet","SCPI_Math_BASE","Offset"),//设置加法运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:BASE:OFFSet?","SCPI_Math_BASE","OffsetQ"),//查询加法运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:BASE:OPERator","SCPI_Math_BASE","Operator"),//设置运算符
//            new SCPICommandStruct(":MATH:BASE:OPERator?","SCPI_Math_BASE","OperatorQ"),//查询运算符

    private int s1;
    private int s2;
    private double extent;
    private double offset;
    private int modeIndex;

    /**
     * 选择加法运算的信源1
     */
    public void S1(int mathIndex, int index, boolean isUpdateUI) {
        if (s1 == index) return;
        this.s1 = index;
        ChannelFactory.getMathChannel(mathIndex).getMathDualWave().setSource1(index);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_S1);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询加法运算的信源1
     */
    public int S1Q() {
        return this.s1;
    }

    /**
     * 选择加法运算的信源2
     */
    public void S2(int mathIndex, int index, boolean isUpdateUI) {
        if (s2 == index) return;
        this.s2 = index;
        ChannelFactory.getMathChannel(mathIndex).getMathDualWave().setSource2(index);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_S2);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询加法运算的信源2
     */
    public int S2Q() {
        return this.s2;
    }


    /**
     * 设置加法运算结果的垂直档位
     */
    public void Extent(int mathIndex, double extent, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_XY) return;
//        if (this.extent == extent) return;
        this.extent = extent;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_EXTENT);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询加法运算结果的垂直档位
     */
    public double ExtentQ() {
        return this.extent;
    }


    /**
     * 设置加法运算结果的垂直偏移
     */
    public void Offset(int mathIndex, double offset, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ()==Command_Display.RoutineTimeBaseMode_XY)return;
        this.offset = offset;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_OFFSET);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(offset);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }



    /**
     * 查询加法运算结果的垂直偏移
     */
    public String OffsetQ(int mathIndex) {
        MathChannel channel = ChannelFactory.getMathChannel(mathIndex);
        double pos;
//        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE)) {
//            case CacheUtil.MATHTYPE_DW:
                pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION + TChan.toUiChNo(mathIndex));
//                break;
//            case CacheUtil.MATHTYPE_FFT:
//                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE) == 0) {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION);
//                } else {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION);
//                }
//                break;
//            case CacheUtil.MATHTYPE_AXB:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION);
//                break;
//            default:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION);
//                break;
//        }
        pos = GlobalVar.get().getMainWave().y / 2 - pos;
        String unit = ChannelFactory.getProbeType(mathIndex);
        return TBookUtil.getFourFromD_Trim0(pos * channel.getADVerticalPerPix()) + unit;
    }

    public void Operator(int mathIndex, int modeIndex, boolean isUpdateUI) {
        if (this.modeIndex == modeIndex) return;
        this.modeIndex = modeIndex;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_OPERATOR);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(modeIndex);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int OperatorQ(){
        return this.modeIndex;
    }

}
