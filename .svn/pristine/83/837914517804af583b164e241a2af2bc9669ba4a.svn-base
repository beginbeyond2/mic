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
 * Created by liwb on 2018/1/12.
 */

public class Command_Math_FFT {
    //FFT
//            new SCPICommandStruct(":MATH:FFT:SOURce","SCPI_Math_FFT","Source"),//选择FFT运算的信源
//            new SCPICommandStruct(":MATH:FFT:SOURce?","SCPI_Math_FFT","SourceQ"),//查询FFT运算的信源
//            new SCPICommandStruct(":MATH:FFT:WINDow","SCPI_Math_FFT","Window"),//选择FFT运算的窗函数
//            new SCPICommandStruct(":MATH:FFT:WINDow?","SCPI_Math_FFT","WindowQ"),//查询FFT运算的窗函数
//            new SCPICommandStruct(":MATH:FFT:TYPE","SCPI_Math_FFT","Type"),//选择FFT波形的显示方式
//            new SCPICommandStruct(":MATH:FFT:TYPE?","SCPI_Math_FFT","TypeQ"),//查询FFT波形的显示方式
//            new SCPICommandStruct(":MATH:FFT:EXTent","SCPI_Math_FFT","Extent"),//设置FFT运算结果的垂直档位
//            new SCPICommandStruct(":MATH:FFT:PLUS:EXTent","SCPI_Math_FFT","Plus_Extent"),//设置FFT运算结果的垂直档位
//            new SCPICommandStruct(":MATH:FFT:EXTent?","SCPI_Math_FFT","ExtentQ"),//查询FFT运算结果的垂直档位
//            new SCPICommandStruct(":MATH:FFT:OFFSet","SCPI_Math_FFT","Offset"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:PLUS:OFFSet","SCPI_Math_FFT","Plus_Offset"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:OFFSet?","SCPI_Math_FFT","OffsetQ"),//查询FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:HSCAle","SCPI_Math_FFT","HsCale"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:HSCAle?","SCPI_Math_FFT","HsCaleQ"),//查询FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:HPOSition","SCPI_Math_FFT","Position"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:HPOSition?","SCPI_Math_FFT","PositionQ"),//查询FFT运算结果的垂直偏移

    private int source;
    private int window;
    private int type;
    private double extent;
    private double offset;
    private double hsCale;
    private double position;

    /**
     * 选择FFT运算的信源
     */
    public void Source(int mathIndex, int index, boolean isUpdateUI) {
        if (source == index) return;
        this.source = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFTSOURCE);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询FFT运算的信源
     */
    public int SourceQ() {
        return this.source;
    }

    /**
     * 选择FFT运算的窗函数
     */
    public void Window(int mathIndex, int index, boolean isUpdateUI) {
        if (window == index) return;
        this.window = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFTWINDOW);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询FFT运算的窗函数
     */
    public int WindowQ() {
        return this.window;
    }

    /**
     * 选择FFT波形的显示方式
     */
    public void Type(int mathIndex, int index, boolean isUpdateUI) {
        if (type == index) return;
        this.type = index;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFTTYPE);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询FFT波形的显示方式
     */
    public int TypeQ() {
        return this.type;
    }

    /**
     * 设置FFT运算结果的垂直档位
     */
    public void Extent(int mathIndex, double extent, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_XY)
            return;
//        if (this.extent == extent) return;
        this.extent = extent;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFT_Extent);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * TODO 数学多通道
     * 设置FFT运算结果的垂直档位
     */
    public void Plus_Extent(int index, boolean isUpdateUI) {
        double tem = ExtentQ();
//        tem+=getExtent(index);
        Extent(index, tem, isUpdateUI);
    }

    /**
     * 查询FFT运算结果的垂直档位
     */
    public double ExtentQ() {
        return this.extent;
    }

    /**
     * 设置FFT运算结果的垂直偏移
     */
    public void Offset(int mathIndex, double offset, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_XY)
            return;
//        if (this.offset == offset) return;
        this.offset = offset;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFT_Offset);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(offset);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置FFT运算结果的垂直偏移
     */
    public void Plus_Offset(int index, boolean isUpdateUI) {

    }

    /**
     * 查询FFT运算结果的垂直偏移
     */
    public String OffsetQ(int source) {

        int mathChan = TChan.toMathChan(source);
        MathChannel channel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
        double pos;
//        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE)) {
//            case CacheUtil.MATHTYPE_DW:
//        pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION);
//                break;
//            case CacheUtil.MATHTYPE_FFT:
                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID) == 0) {
                    pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION);
                } else {
                    pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION);
                }
//                break;
//            case CacheUtil.MATHTYPE_AXB:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION);
//                break;
//            default:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION);
//                break;
//        }
        pos = GlobalVar.get().getMainWave().y / 2 - pos;
        String unit = ChannelFactory.getProbeType(ChannelFactory.MATH1);
        return TBookUtil.getFourFromD_Trim0(pos * channel.getADVerticalPerPix()) + unit;
    }

    public void HsCale(int mathIndex, double hsCale,boolean isUpdateUI){
        if (!Command.get().getMath().DisplayQ() || Command.get().getMath().ModeQ()!= CacheUtil.MATHTYPE_FFT) return;
        int min=1;
        int max=100000000;
        if (hsCale<min) hsCale=min;
        if (hsCale>max) hsCale=max;

        this.hsCale=hsCale;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFT_HsCal);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(hsCale);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public double HsCaleQ(){
        return this.hsCale;
    }

    public void Position(int mathIndex, double position, boolean isUpdateUI) {
        if (!Command.get().getMath().DisplayQ() || Command.get().getMath().ModeQ()!= CacheUtil.MATHTYPE_FFT) return;
        this.position=position;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFT_Position);
//            msgToUI.setFlag(CommandMsgToUI.FLAG_TIMEBASE_POSITION);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(position);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public double PositionQ(){
        return this.position;
    }

}
